/* DoraMCU 4X Space Strategy - SSE Event Handler */

const GameSSE = (function() {
    let eventSource = null;
    let listeners = {};
    let currentSessionId = null;
    let reconnectAttempts = 0;
    let reconnectTimer = null;
    const MAX_RECONNECT_DELAY = 30000; // 30 seconds max
    const INITIAL_RECONNECT_DELAY = 1000; // 1 second initial

    function on(eventType, callback) {
        if (!listeners[eventType]) {
            listeners[eventType] = [];
        }
        listeners[eventType].push(callback);
    }

    function off(eventType, callback) {
        if (!listeners[eventType]) return;
        if (!callback) {
            listeners[eventType] = [];
            return;
        }
        listeners[eventType] = listeners[eventType].filter(cb => cb !== callback);
    }

    function emit(eventType, data) {
        const cbs = listeners[eventType] || [];
        cbs.forEach(cb => {
            try {
                cb(data);
            } catch (e) {
                console.error(`Error in SSE listener for ${eventType}:`, e);
            }
        });
        // Also emit to wildcard listeners
        const all = listeners['*'] || [];
        all.forEach(cb => {
            try {
                cb(eventType, data);
            } catch (e) {
                console.error('Error in wildcard SSE listener:', e);
            }
        });
    }

    function connect(sessionId) {
        // Close existing connection without emitting disconnect event (we're reconnecting)
        if (reconnectTimer) {
            clearTimeout(reconnectTimer);
            reconnectTimer = null;
        }
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
        
        currentSessionId = sessionId;
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('No auth token for SSE connection');
            return;
        }

        // Use URL constructor to properly encode the query parameter
        const url = new URL(`/ws/v1/game/${sessionId}`, window.location.origin);
        url.searchParams.set('token', token);
        console.log('Connecting to SSE:', url.toString());
        eventSource = new EventSource(url.toString());

        eventSource.addEventListener('open', () => {
            console.log('SSE connection established for session:', sessionId);
            reconnectAttempts = 0; // Reset reconnect count on successful connection
            emit('connected', { sessionId });
        });

        eventSource.addEventListener('message', (event) => {
            try {
                // Handle heartbeat events silently
                if (event.type === 'heartbeat' || event.lastEventId === 'heartbeat') {
                    return; // Silently ignore heartbeat
                }
                const data = JSON.parse(event.data);
                // Also ignore heartbeat data
                if (data && typeof data === 'number') {
                    return; // Server sends raw timestamp for heartbeat
                }
                console.log('SSE event received:', data.type, data);
                emit(data.type || 'GAME_STATE', data);
            } catch (e) {
                // Ignore heartbeat parsing errors
                if (event.data && event.data.length < 20) {
                    return; // Likely a heartbeat timestamp
                }
                console.error('Error parsing SSE message:', e, event.data);
            }
        });

        // Handle named events (connected, heartbeat, etc.)
        eventSource.addEventListener('connected', (event) => {
            console.log('SSE connected event:', event.data);
            reconnectAttempts = 0;
            emit('connected', { sessionId });
        });

        eventSource.addEventListener('heartbeat', (event) => {
            // Silently ignore named heartbeat events
        });

        eventSource.onerror = () => {
            const readyState = eventSource.readyState;
            if (readyState === EventSource.CONNECTING) {
                // Browser is already trying to reconnect, don't interfere
                console.log('SSE connection lost, browser reconnecting...');
                return;
            }
            if (readyState === EventSource.CLOSED) {
                console.warn('SSE connection closed by server');
                emit('disconnected', { sessionId, message: 'SSE connection closed' });
                // Close the current source to prevent browser auto-reconnect
                if (eventSource) {
                    eventSource.close();
                    eventSource = null;
                }
                // Manual reconnect with exponential backoff
                scheduleReconnect(sessionId);
            }
        };
    }

    function scheduleReconnect(sessionId) {
        if (reconnectTimer) {
            clearTimeout(reconnectTimer);
        }
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s, 30s, 30s...
        const delay = Math.min(INITIAL_RECONNECT_DELAY * Math.pow(2, reconnectAttempts), MAX_RECONNECT_DELAY);
        reconnectAttempts++;
        console.log(`SSE reconnect attempt ${reconnectAttempts} in ${delay}ms...`);
        reconnectTimer = setTimeout(() => {
            if (currentSessionId === sessionId) {
                connect(sessionId);
            }
        }, delay);
    }

    function disconnect() {
        if (reconnectTimer) {
            clearTimeout(reconnectTimer);
            reconnectTimer = null;
        }
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
        reconnectAttempts = 0;
        currentSessionId = null;
        emit('disconnected', null);
    }

    function isConnected() {
        return eventSource && eventSource.readyState === EventSource.OPEN;
    }

    function getSessionId() {
        return currentSessionId;
    }

    return {
        on,
        off,
        connect,
        disconnect,
        isConnected,
        getSessionId,
        emit // For manual emission from tests
    };
})();
