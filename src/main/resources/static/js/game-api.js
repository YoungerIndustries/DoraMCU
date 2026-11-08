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
        // If baseUrl is empty, use relative URL (browser resolves against current origin)
        // If baseUrl is set, prepend it to the endpoint
        // If endpoint is already absolute, use it as-is
        let targetUrl;
        if (endpoint.startsWith('http://') || endpoint.startsWith('https://')) {
            targetUrl = endpoint;
        } else if (baseUrl && baseUrl.trim() !== '') {
            targetUrl = baseUrl + endpoint;
        } else {
            // Relative URL - browser will resolve against window.location.origin
            targetUrl = endpoint;
        }

        const defaults = {
            headers: getHeaders(options.body ? true : false),
        };
        if (options.body && typeof options.body === 'object') {
            options.body = JSON.stringify(options.body);
        }

        // Add timeout to prevent hanging requests (30s default, overridable via options.timeout)
        const timeoutMs = options.timeout || 30000;
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), timeoutMs);
        const fetchOptions = { ...defaults, ...options, headers: { ...defaults.headers, ...options.headers }, signal: controller.signal };

        let response;
        try {
            response = await fetch(targetUrl, fetchOptions);
        } catch (err) {
            clearTimeout(timer);
            if (err.name === 'AbortError') {
                const timeoutErr = new Error(`Request timeout after ${timeoutMs}ms: ${targetUrl}`);
                timeoutErr.timeout = true;
                throw timeoutErr;
            }
            throw err;
        }
        clearTimeout(timer);
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

    // List all available empires for selection
    async function listPlayers() {
        return request('/api/v1/auth/players');
    }

    // Login by selecting a player from the empire list
    async function playerLogin(playerId) {
        const data = await request(`/api/v1/auth/player-login/${playerId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('username', data.username || 'Commander');
            if (data.pin) localStorage.setItem('playerPin', data.pin);
            if (data.empireName) localStorage.setItem('empireName', data.empireName);
            if (data.governmentType) localStorage.setItem('governmentType', data.governmentType);
            if (data.ethos) localStorage.setItem('ethos', data.ethos);
            if (data.isAdmin) localStorage.setItem('isAdmin', 'true');
        }
        return data;
    }

    // Register new player
    async function register(username, password, empireName, governmentType, ethos, galaxyName) {
        const data = await request('/api/v1/auth/register', {
            method: 'POST',
            body: { username, password, empireName, governmentType, ethos, galaxyName },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('username', data.username || username);
            if (data.pin) localStorage.setItem('playerPin', data.pin);
            if (data.empireName) localStorage.setItem('empireName', data.empireName);
            if (data.governmentType) localStorage.setItem('governmentType', data.governmentType);
            if (data.ethos) localStorage.setItem('ethos', data.ethos);
        }
        return data;
    }

    // Dev bypass: auto-login as Butch Younger without credentials (dev profile only)
    async function devLogin() {
        const data = await request('/api/v1/auth/dev-login', {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('username', data.username || 'Butch Younger');
            if (data.pin) localStorage.setItem('playerPin', data.pin);
            if (data.empireName) localStorage.setItem('empireName', data.empireName);
        }
        return data;
    }

    // Session management — unified auth + session lifecycle

    /**
     * Checks if a stored session ID is still valid on the server.
     * @returns {Promise<boolean>} true if the session exists and is accessible
     */
    async function validateSession(sessionId) {
        try {
            const state = await getGameState(sessionId, true);
            return !!state;
        } catch (e) {
            return false;
        }
    }

    /**
     * Ensures the user is authenticated and has a valid session.
     * Returns an object with { sessionId, needsSessionSelection } indicating the result.
     * 
     * Flow:
     * 1. If token + valid stored session → return session
     * 2. If token + invalid/missing session → return needsSessionSelection
     * 3. If no token → show login screen
     */
    async function ensureSession() {
        const token = getToken();
        const username = localStorage.getItem('username');
        const storedSession = localStorage.getItem('doramcu_session_id');

        // Case 1: Has token and stored session — validate it
        if (token && username && storedSession) {
            const isValid = await validateSession(storedSession);
            if (isValid) {
                return { sessionId: storedSession, needsSessionSelection: false };
            }
            // Session expired — clear it and fall through
            localStorage.removeItem('doramcu_session_id');
        }

        // Case 2: Has token but no valid session — needs session selection
        if (token && username) {
            return { sessionId: null, needsSessionSelection: true };
        }

        // Case 3: No token — show login screen
        return { sessionId: null, needsSessionSelection: false };
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
    async function getGameState(sessionId, summary) {
        // Default to summary=true to reduce response size (14MB full vs ~2MB summary)
        const summaryParam = (summary === undefined || summary) ? '?summary=true' : '';
        return request(`/api/v1/sessions/${sessionId}/state${summaryParam}`);
    }

    // System details - fetch full celestial body data for a specific system
    async function getSystemDetails(sessionId, systemId) {
        return request(`/api/v1/sessions/${sessionId}/systems/${systemId}`);
    }

    // ---- Traveller RPG API Methods ----

    async function getCharacter(characterId) {
        return request(`/api/v1/characters/${characterId}`);
    }

    async function createCharacter(name) {
        return request(`/api/v1/characters?name=${encodeURIComponent(name)}`, {
            method: 'POST'
        });
    }

    async function generateCharacter(name) {
        return request(`/api/v1/characters/generate?name=${encodeURIComponent(name)}`, {
            method: 'POST'
        });
    }

    async function serveCareer(characterId, career) {
        return request(`/api/v1/characters/${characterId}/career?career=${encodeURIComponent(career)}`, {
            method: 'POST'
        });
    }

    async function getAllShipDesigns() {
        return request('/api/v1/shipdesigns');
    }

    async function createShipDesign(design) {
        return request('/api/v1/shipdesigns', {
            method: 'POST',
            body: design
        });
    }

    async function createPresetDesign(preset) {
        return request(`/api/v1/shipdesigns/preset?preset=${encodeURIComponent(preset)}`, {
            method: 'POST'
        });
    }

    async function buildFromDesign(sessionId, design) {
        return request(`/api/v1/sessions/${sessionId}/actions/build-from-design`, {
            method: 'POST',
            body: design
        });
    }

    async function getTradeMarket(sessionId, systemId) {
        return request(`/api/v1/sessions/${sessionId}/trade/market?systemId=${systemId}`);
    }

    async function buyCommodity(sessionId, request) {
        return request(`/api/v1/traveller/trade/buy`, {
            method: 'POST',
            body: request
        });
    }

    async function sellCommodity(sessionId, request) {
        return request(`/api/v1/traveller/trade/sell`, {
            method: 'POST',
            body: request
        });
    }

    async function getMarketPrices(worldId) {
        return request(`/api/v1/traveller/trade/prices/${worldId}`);
    }

    async function getCommodities() {
        return request('/api/v1/traveller/trade/commodities');
    }

    async function getTradeCareerSummary(sessionId) {
        return request(`/api/v1/traveller/trade/career-summary?sessionId=${sessionId}`);
    }

    async function getExchangeSummary(sessionId) {
        return request(`/api/v1/traveller/trade/exchange-summary?sessionId=${sessionId}`);
    }

    async function getEconomicEvents(sessionId) {
        return request(`/api/v1/traveller/trade/economic-events?sessionId=${sessionId}`);
    }

    async function getTradeRoutes(sessionId) {
        return request(`/api/v1/traveller/trade/routes?sessionId=${sessionId}`);
    }

    async function executeTradeMission(sessionId, tradeRequest) {
        return request(`/api/v1/traveller/trade/mission`, {
            method: 'POST',
            body: tradeRequest
        });
    }

    // Psionics (B03 Worlds & Adventures)
    async function getPsionicDisciplines() {
        return request('/api/v1/traveller/psionics/disciplines');
    }

    async function getPsionicDifficulties() {
        return request('/api/v1/traveller/psionics/difficulties');
    }

    async function getPsionicEncounterTypes() {
        return request('/api/v1/traveller/psionics/encounter-types');
    }

    async function getPsionicProfile(characterId) {
        return request(`/api/v1/traveller/psionics/characters/${characterId}`);
    }

    async function detectPsionic(characterId) {
        return request(`/api/v1/traveller/psionics/characters/${characterId}/detect`, {
            method: 'POST'
        });
    }

    async function performPowerCheck(inScore, disciplineLevel, difficulty) {
        return request(`/api/v1/traveller/psionics/check?inScore=${inScore}&disciplineLevel=${disciplineLevel}&difficulty=${difficulty}`, {
            method: 'POST'
        });
    }

    async function generateEncounter(detectionLevel) {
        return request(`/api/v1/traveller/psionics/encounter?detectionLevel=${detectionLevel}`, {
            method: 'POST'
        });
    }

    async function addDiscipline(characterId, discipline, level) {
        return request(`/api/v1/traveller/psionics/characters/${characterId}/discipline?discipline=${discipline}&level=${level}`, {
            method: 'POST'
        });
    }

    async function restPsionic(characterId, hours) {
        return request(`/api/v1/traveller/psionics/characters/${characterId}/rest?hours=${hours}`, {
            method: 'POST'
        });
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
        devLogin,
        listPlayers,
        playerLogin,
        register,
        ensureSession,
        validateSession,
        createSession,
        joinSession,
        leaveSession,
        listSessions,
        getGameState,
        getSystemDetails,
        moveFleet,
        buildShip,
        engageCombat,
        advanceTurn,
        // Traveller RPG methods
        getCharacter,
        createCharacter,
        generateCharacter,
        serveCareer,
        getAllShipDesigns,
        createShipDesign,
        createPresetDesign,
        buildFromDesign,
        getTradeMarket,
        buyCommodity,
        sellCommodity,
        getMarketPrices,
        getCommodities,
        getTradeCareerSummary,
        getExchangeSummary,
        getEconomicEvents,
        getTradeRoutes,
        executeTradeMission,
        // Psionics (B03)
        getPsionicDisciplines,
        getPsionicDifficulties,
        getPsionicEncounterTypes,
        getPsionicProfile,
        detectPsionic,
        performPowerCheck,
        generateEncounter,
        addDiscipline,
        restPsionic,
        clearAuth,
        getToken,
        setToken,
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
