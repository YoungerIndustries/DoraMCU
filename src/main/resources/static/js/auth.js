/**
 * DoraMCU Shared Authentication Module
 * Provides unified authentication across all pages (login, game, admin, health)
 * Uses consistent localStorage keys: 'token' and 'username'
 */
const Auth = (function() {
    'use strict';

    // Consistent localStorage keys across all pages
    const TOKEN_KEY = 'token';
    const USERNAME_KEY = 'username';

    /**
     * Get the base API URL from the current page or stored server URL
     * @returns {string} Base API URL
     */
    function getBaseUrl() {
        const storedServer = localStorage.getItem('serverUrl');
        if (storedServer && storedServer !== '') {
            return storedServer.replace(/\/$/, ''); // Remove trailing slash
        }
        // Default to current origin
        return window.location.origin;
    }

    /**
     * Store authentication credentials
     * @param {string} token - Authentication token
     * @param {string} username - Username
     */
    function setCredentials(token, username) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USERNAME_KEY, username);
    }

    /**
     * Get stored authentication credentials
     * @returns {{token: string|null, username: string|null}}
     */
    function getCredentials() {
        return {
            token: localStorage.getItem(TOKEN_KEY),
            username: localStorage.getItem(USERNAME_KEY)
        };
    }

    /**
     * Check if user is authenticated
     * @returns {boolean}
     */
    function isAuthenticated() {
        const { token } = getCredentials();
        return token !== null && token !== '';
    }

    /**
     * Clear authentication credentials
     */
    function clearCredentials() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USERNAME_KEY);
    }

    /**
     * Get auth header for API requests
     * @returns {Object} Header object with Authorization if authenticated
     */
    function getAuthHeader() {
        const { token } = getCredentials();
        if (token) {
            return { 'Authorization': `Bearer ${token}` };
        }
        return {};
    }

    /**
     * Login with username and password
     * @param {string} username - Username
     * @param {string} password - Password
     * @returns {Promise<Object>} Login response
     */
    async function login(username, password) {
        const baseUrl = getBaseUrl();
        const response = await fetch(`${baseUrl}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Login failed' }));
            throw new Error(error.message || 'Login failed');
        }

        const data = await response.json();
        if (data.token) {
            setCredentials(data.token, username);
        }
        return data;
    }

    /**
     * Login with player PIN
     * @param {string} pin - Player PIN
     * @returns {Promise<Object>} Login response
     */
    async function loginWithPin(pin) {
        const baseUrl = getBaseUrl();
        const response = await fetch(`${baseUrl}/api/auth/pin`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ pin })
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'PIN login failed' }));
            throw new Error(error.message || 'PIN login failed');
        }

        const data = await response.json();
        if (data.token) {
            setCredentials(data.token, data.username || 'Player');
        }
        return data;
    }

    /**
     * Register a new user
     * @param {Object} data - Registration data
     * @returns {Promise<Object>} Registration response
     */
    async function register(data) {
        const baseUrl = getBaseUrl();
        const response = await fetch(`${baseUrl}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Registration failed' }));
            throw new Error(error.message || 'Registration failed');
        }

        const result = await response.json();
        if (result.token) {
            setCredentials(result.token, data.username);
        }
        return result;
    }

    /**
     * Logout and clear credentials
     */
    function logout() {
        clearCredentials();
    }

    /**
     * Verify token validity
     * @returns {Promise<boolean>} True if token is valid
     */
    async function verifyToken() {
        if (!isAuthenticated()) {
            return false;
        }

        try {
            const baseUrl = getBaseUrl();
            const response = await fetch(`${baseUrl}/api/auth/verify`, {
                headers: getAuthHeader()
            });
            return response.ok;
        } catch (error) {
            return false;
        }
    }

    /**
     * Check server health/status
     * @returns {Promise<Object>} Server status
     */
    async function checkServerStatus() {
        const baseUrl = getBaseUrl();
        try {
            const response = await fetch(`${baseUrl}/actuator/health`, {
                method: 'GET'
            });
            if (response.ok) {
                const data = await response.json();
                return { status: 'online', data };
            }
            return { status: 'offline' };
        } catch (error) {
            return { status: 'offline', error: error.message };
        }
    }

    /**
     * Migrate old localStorage keys to new consistent keys
     * Handles authToken -> token and adminUsername -> username migration
     */
    function migrateLegacyKeys() {
        // Migrate authToken to token
        const legacyToken = localStorage.getItem('authToken');
        if (legacyToken && !localStorage.getItem(TOKEN_KEY)) {
            localStorage.setItem(TOKEN_KEY, legacyToken);
            localStorage.removeItem('authToken');
        }

        // Migrate adminUsername to username
        const legacyUsername = localStorage.getItem('adminUsername');
        if (legacyUsername && !localStorage.getItem(USERNAME_KEY)) {
            localStorage.setItem(USERNAME_KEY, legacyUsername);
            localStorage.removeItem('adminUsername');
        }
    }

    // Run migration on module load
    migrateLegacyKeys();

    // Public API
    return {
        getBaseUrl,
        setCredentials,
        getCredentials,
        isAuthenticated,
        clearCredentials,
        getAuthHeader,
        login,
        loginWithPin,
        register,
        logout,
        verifyToken,
        checkServerStatus,
        migrateLegacyKeys
    };
})();
