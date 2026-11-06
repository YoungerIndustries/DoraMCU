/* DoraMCU 4X Space Strategy - REST API Client */

const GameAPI = (function() {
    let baseUrl = localStorage.getItem('doramcu_server_url') || '';
    let authToken = null;

    function init(base) {
        if (typeof base === 'string') {
            baseUrl = base;
        } else {
            baseUrl = localStorage.getItem('doramcu_server_url') || '';
        }
        authToken = localStorage.getItem('token');
    }

    function setServerUrl(url) {
        baseUrl = (url || '').trim().replace(/\/+$/, '');
        localStorage.setItem('doramcu_server_url', baseUrl);
    }

    function getServerUrl() {
        return baseUrl;
    }

    function getToken() {
        if (!authToken) {
            authToken = localStorage.getItem('token');
        }
        return authToken;
    }

    function setToken(token) {
        authToken = token;
        localStorage.setItem('token', token);
    }

    function clearAuth() {
        authToken = null;
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('playerPin');
        localStorage.removeItem('empireName');
    }

    function getHeaders(json = true) {
        const headers = {};
        if (json) {
            headers['Content-Type'] = 'application/json';
        }
        const token = getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return headers;
    }

    async function request(endpoint, options = {}) {
        const targetUrl = (endpoint.startsWith('http://') || endpoint.startsWith('https://'))
            ? endpoint
            : baseUrl + endpoint;

        const defaults = {
            headers: getHeaders(options.body ? true : false),
        };
        if (options.body && typeof options.body === 'object') {
            options.body = JSON.stringify(options.body);
        }
        const response = await fetch(targetUrl, { ...defaults, ...options, headers: { ...defaults.headers, ...options.headers } });
        if (response.status === 401 || response.status === 403) {
            clearAuth();
            window.dispatchEvent(new CustomEvent('auth-expired'));
            throw new Error('Authentication expired');
        }
        const text = await response.text();
        let data = null;
        try {
            data = text ? JSON.parse(text) : null;
        } catch (e) {
            // Not JSON
        }
        if (!response.ok) {
            const message = data?.message || data?.error || response.statusText || 'Request failed';
            const err = new Error(message);
            err.status = response.status;
            throw err;
        }
        return data;
    }

    // Server discovery
    async function getServerInfo() {
        return request('/api/v1/auth/server-info');
    }

    // Authentication
    async function login(username, password) {
        const data = await request('/api/v1/auth/login', {
            method: 'POST',
            body: { username, password },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('username', data.username || username);
            if (data.pin) localStorage.setItem('playerPin', data.pin);
            if (data.empireName) localStorage.setItem('empireName', data.empireName);
        }
        return data;
    }

    async function pinLogin(pin) {
        const data = await request('/api/v1/auth/pin-login', {
            method: 'POST',
            body: { pin: String(pin).trim() },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('username', data.username || data.empireName || 'Commander');
            if (data.pin) localStorage.setItem('playerPin', data.pin);
            if (data.empireName) localStorage.setItem('empireName', data.empireName);
        }
        return data;
    }

    // Register new player
    async function register(username, password) {
        const data = await request('/api/v1/auth/register', {
            method: 'POST',
            body: { username, password },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('username', data.username || username);
            if (data.pin) localStorage.setItem('playerPin', data.pin);
            if (data.empireName) localStorage.setItem('empireName', data.empireName);
        }
        return data;
    }

    // Session management
    async function createSession(sessionName) {
        return request('/api/v1/sessions', {
            method: 'POST',
            body: { sessionName }
        });
    }

    async function joinSession(sessionId) {
        try {
            return await request(`/api/v1/sessions/${sessionId}/join`, {
                method: 'POST',
                body: {}
            });
        } catch (err) {
            // Re-throw with status info so the UI can handle "already in session" gracefully
            if (err.status === 409) {
                err.alreadyInSession = true;
            }
            throw err;
        }
    }

    async function leaveSession(sessionId) {
        return request(`/api/v1/sessions/${sessionId}/leave`, {
            method: 'POST',
            body: {}
        });
    }

    async function listSessions() {
        return request('/api/v1/sessions');
    }

    // Game state
    async function getGameState(sessionId) {
        return request(`/api/v1/sessions/${sessionId}/state`);
    }

    // Game actions
    async function moveFleet(sessionId, fleetId, targetX, targetY, solarSystemId) {
        return request(`/api/v1/sessions/${sessionId}/actions/move-fleet`, {
            method: 'POST',
            body: { fleetId, targetX, targetY, solarSystemId }
        });
    }

    async function buildShip(sessionId, shipType, targetFleetId) {
        return request(`/api/v1/sessions/${sessionId}/actions/build-ship`, {
            method: 'POST',
            body: { shipType, targetFleetId }
        });
    }

    async function engageCombat(sessionId, attackerFleetId, defenderFleetId) {
        return request(`/api/v1/sessions/${sessionId}/actions/engage-combat`, {
            method: 'POST',
            body: { attackerFleetId, defenderFleetId }
        });
    }

    async function advanceTurn(sessionId) {
        return request(`/api/v1/sessions/${sessionId}/actions/advance-turn`, {
            method: 'POST',
            body: {}
        });
    }

    // Public API
    return {
        init,
        setServerUrl,
        getServerUrl,
        getServerInfo,
        login,
        pinLogin,
        register,
        createSession,
        joinSession,
        leaveSession,
        listSessions,
        getGameState,
        moveFleet,
        buildShip,
        engageCombat,
        advanceTurn,
        clearAuth,
        getToken,
        getUsername: () => localStorage.getItem('username'),
        getPlayerPin: () => localStorage.getItem('playerPin'),
        getEmpireName: () => localStorage.getItem('empireName')
    };
})();

// Auto-initialize on load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => GameAPI.init());
} else {
    GameAPI.init();
}
