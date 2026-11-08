/**
 * DoraMCU Login Page Module
 * Handles login page UI logic: health status, empire list, tab switching,
 * server selection, registration, password/PIN login, and session management.
 */
const LoginPanel = (function() {
    'use strict';

    // DOM Elements
    let loginForm, pinLoginForm, tabEmpireBtn, tabPasswordBtn, tabPinBtn, empireListEl;
    let serverSelect, customServerContainer, customServerUrl;
    let serverStatusDot, serverStatusLabel;
    let errorMessage, errorText, pinErrorMessage, pinErrorText;
    let submitLogin, submitPinLogin;
    let loginView, dashboardView, registerView;
    let displayUsername, logoutBtn, newEmpireBtn, backToLoginBtn;
    let registerForm, registerErrorMessage, registerErrorText, submitRegister;
    let registrationSuccessBox, generatedPinDisplay;
    let healthOverall, healthDatabase, healthAi, healthDisk, healthRefreshBtn;
    let healthIntervalId = null;

    // Government and Ethos icons mapping
    const govIcons = {
        'DEMOCRACY': '🏛️', 'AUTHORITARIAN': '⚡', 'MONARCHY': '👑',
        'OLIGARCHY': '🏴', 'THEOCRACY': '⛪', 'DICTATORSHIP': '🔨',
        'CONFEDERATION': '🤝', 'CORPORATE': '🏢'
    };
    const ethosIcons = {
        'MATERIALIST': '🔬', 'SPIRITUALIST': '✨', 'LIBERTARIAN': '🕊️',
        'EQUALIST': '⚖️', 'MILITARIST': '⚔️', 'PACIFIST': '🕊️'
    };

    function getGovIcon(govType) { return govIcons[govType] || '🏛️'; }
    function getEthosIcon(ethos) { return ethosIcons[ethos] || '🔬'; }

    function init() {
        cacheElements();
        initServerSelection();
        bindTabEvents();
        bindFormEvents();
        bindNavigationEvents();
        loadHealthStatus();
        startHealthCheck();
        loadEmpireList();
        loadGalaxies();
        checkAuth();
    }

    function cacheElements() {
        loginForm = document.getElementById('loginForm');
        pinLoginForm = document.getElementById('pinLoginForm');
        tabEmpireBtn = document.getElementById('tabEmpireBtn');
        tabPasswordBtn = document.getElementById('tabPasswordBtn');
        tabPinBtn = document.getElementById('tabPinBtn');
        empireListEl = document.getElementById('empireList');
        serverSelect = document.getElementById('serverSelect');
        customServerContainer = document.getElementById('customServerContainer');
        customServerUrl = document.getElementById('customServerUrl');
        serverStatusDot = document.getElementById('serverStatusDot');
        serverStatusLabel = document.getElementById('serverStatusLabel');
        errorMessage = document.getElementById('errorMessage');
        errorText = document.getElementById('errorText');
        pinErrorMessage = document.getElementById('pinErrorMessage');
        pinErrorText = document.getElementById('pinErrorText');
        submitLogin = document.getElementById('submitLogin');
        submitPinLogin = document.getElementById('submitPinLogin');
        loginView = document.getElementById('loginView');
        dashboardView = document.getElementById('dashboardView');
        registerView = document.getElementById('registerView');
        displayUsername = document.getElementById('displayUsername');
        logoutBtn = document.getElementById('logoutBtn');
        newEmpireBtn = document.getElementById('newEmpireBtn');
        backToLoginBtn = document.getElementById('backToLoginBtn');
        registerForm = document.getElementById('registerForm');
        registerErrorMessage = document.getElementById('registerErrorMessage');
        registerErrorText = document.getElementById('registerErrorText');
        submitRegister = document.getElementById('submitRegister');
        registrationSuccessBox = document.getElementById('registrationSuccessBox');
        generatedPinDisplay = document.getElementById('generatedPinDisplay');
        healthOverall = document.getElementById('healthOverall');
        healthDatabase = document.getElementById('healthDatabase');
        healthAi = document.getElementById('healthAi');
        healthDisk = document.getElementById('healthDisk');
        healthRefreshBtn = document.getElementById('healthRefreshBtn');
    }

    // ============================================================
    // Server Selection
    // ============================================================

    function initServerSelection() {
        const storedServer = localStorage.getItem('doramcu_server_url') || '';
        if (!storedServer) {
            serverSelect.value = '';
            customServerContainer.style.display = 'none';
        } else if (storedServer === 'http://localhost:8080') {
            serverSelect.value = 'http://localhost:8080';
            customServerContainer.style.display = 'none';
        } else {
            serverSelect.value = 'custom';
            customServerUrl.value = storedServer;
            customServerContainer.style.display = 'block';
        }
        updateServerStatus();
    }

    function bindServerEvents() {
        serverSelect.addEventListener('change', function() {
            if (serverSelect.value === 'custom') {
                customServerContainer.style.display = 'block';
            } else {
                customServerContainer.style.display = 'none';
                GameAPI.setServerUrl(serverSelect.value);
                updateServerStatus();
                loadGalaxies();
            }
        });

        customServerUrl.addEventListener('change', function() {
            GameAPI.setServerUrl(customServerUrl.value);
            updateServerStatus();
            loadGalaxies();
        });
    }

    function updateServerStatus() {
        const base = GameAPI.getServerUrl();
        const url = base || window.location.origin;

        serverStatusDot.className = 'status-dot checking';
        serverStatusLabel.textContent = 'Checking cluster...';

        fetchWithTimeout(url + '/actuator/health', 3000)
            .then(r => r.json())
            .then(data => {
                serverStatusDot.className = 'status-dot online';
                serverStatusLabel.textContent = 'Cluster Online';
            })
            .catch(() => {
                serverStatusDot.className = 'status-dot offline';
                serverStatusLabel.textContent = 'Cluster Offline';
            });
    }

    function fetchWithTimeout(url, timeoutMs) {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), timeoutMs);
        return fetch(url, { signal: controller.signal }).finally(() => clearTimeout(timer));
    }

    // ============================================================
    // Health Status
    // ============================================================

    function loadHealthStatus() {
        fetch('/actuator/health')
            .then(r => r.json())
            .then(data => {
                // Overall status
                const status = data.status || 'UNKNOWN';
                healthOverall.textContent = status;
                healthOverall.className = 'health-value ' + (status === 'UP' ? 'up' : 'down');

                // Database
                const db = data.components && data.components.database;
                if (db) {
                    const dbStatus = db.status || (db.details && db.details.status);
                    const dbVersion = db.details && db.details.database;
                    healthDatabase.textContent = dbVersion || dbStatus || '-';
                    healthDatabase.className = 'health-value ' + (dbStatus === 'UP' ? 'up' : 'down');
                }

                // AI Service (Ollama)
                const ollama = data.components && data.components.ollama;
                if (ollama) {
                    const aiStatus = ollama.status || (ollama.details && ollama.details.status);
                    healthAi.textContent = aiStatus || '-';
                    healthAi.className = 'health-value ' + (aiStatus === 'UP' ? 'up' : 'down');
                }

                // Disk space
                const disk = data.components && data.components.diskSpace;
                if (disk && disk.details) {
                    const freeGb = Math.round(disk.details.free / (1024 * 1024 * 1024));
                    const totalGb = Math.round(disk.details.total / (1024 * 1024 * 1024));
                    healthDisk.textContent = `${freeGb} GB / ${totalGb} GB`;
                    healthDisk.className = 'health-value up';
                }
            })
            .catch(() => {
                healthOverall.textContent = 'Offline';
                healthOverall.className = 'health-value down';
            });
    }

    function startHealthCheck() {
        stopHealthCheck();
        healthIntervalId = setInterval(loadHealthStatus, 30000);
    }

    function stopHealthCheck() {
        if (healthIntervalId) {
            clearInterval(healthIntervalId);
            healthIntervalId = null;
        }
    }

    // ============================================================
    // Tab Switching
    // ============================================================

    function showTab(tab) {
        // Hide all forms
        loginForm.style.display = 'none';
        pinLoginForm.style.display = 'none';
        if (empireListEl) empireListEl.style.display = 'none';

        // Remove active from all tabs
        if (tabEmpireBtn) tabEmpireBtn.classList.remove('active');
        tabPasswordBtn.classList.remove('active');
        tabPinBtn.classList.remove('active');

        // Clear errors
        errorMessage.style.display = 'none';
        pinErrorMessage.style.display = 'none';

        if (tab === 'empire') {
            if (tabEmpireBtn) tabEmpireBtn.classList.add('active');
            if (empireListEl) empireListEl.style.display = 'block';
        } else if (tab === 'password') {
            tabPasswordBtn.classList.add('active');
            loginForm.style.display = 'block';
        } else if (tab === 'pin') {
            tabPinBtn.classList.add('active');
            pinLoginForm.style.display = 'block';
        }
    }

    function bindTabEvents() {
        if (tabEmpireBtn) {
            tabEmpireBtn.addEventListener('click', function() { showTab('empire'); });
        }
        tabPasswordBtn.addEventListener('click', function() { showTab('password'); });
        tabPinBtn.addEventListener('click', function() { showTab('pin'); });
    }

    // ============================================================
    // Empire List
    // ============================================================

    function loadEmpireList() {
        if (!empireListEl) return;
        empireListEl.innerHTML = '<div class="empire-list-loading">Loading empires...</div>';
        empireListEl.style.display = 'block';

        GameAPI.listPlayers()
            .then(players => {
                if (!players || players.length === 0) {
                    empireListEl.innerHTML = `
                        <div class="empty-state">
                            <div style="font-size: 2rem; margin-bottom: 8px;">🌌</div>
                            <div style="color: var(--text-secondary); font-size: 0.9rem;">No empires found.</div>
                            <div style="color: var(--text-muted); font-size: 0.8rem; margin-top: 4px;">Be the first to create an empire!</div>
                        </div>`;
                    return;
                }
                let html = '';
                players.forEach(p => {
                    const govIcon = getGovIcon(p.governmentType);
                    const ethosIcon = getEthosIcon(p.ethos);
                    html += `
                        <div class="empire-card">
                            <div class="empire-card-header">
                                <span class="empire-icon">${govIcon}</span>
                                <div class="empire-card-info">
                                    <div class="empire-name">${escapeHtml(p.empireName)}</div>
                                    <div class="empire-meta">
                                        <span class="empire-badge">${govIcon} ${escapeHtml(p.governmentType)}</span>
                                        <span class="empire-badge">${ethosIcon} ${escapeHtml(p.ethos)}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="empire-card-footer">
                                <span class="empire-commander">👤 ${escapeHtml(p.commanderName)}</span>
                                <button class="empire-login-btn" data-player-id="${p.id}">
                                    ▶ Login
                                </button>
                            </div>
                        </div>`;
                });
                empireListEl.innerHTML = html;

                // Attach login button handlers via event delegation
                empireListEl.querySelectorAll('.empire-login-btn').forEach(btn => {
                    btn.addEventListener('click', function() {
                        const playerId = this.getAttribute('data-player-id');
                        loginWithPlayer(playerId);
                    });
                });
            })
            .catch(err => {
                empireListEl.innerHTML = `
                    <div class="empty-state">
                        <div style="color: var(--text-secondary); font-size: 0.9rem;">Unable to load empires.</div>
                        <div style="color: var(--text-muted); font-size: 0.8rem; margin-top: 4px;">${escapeHtml(err.message)}</div>
                    </div>`;
            });
    }

    function loginWithPlayer(playerId) {
        const btn = empireListEl.querySelector(`[data-player-id="${playerId}"]`);
        if (btn) {
            btn.disabled = true;
            btn.textContent = '⏳';
        }
        GameAPI.playerLogin(playerId)
            .then(() => {
                goToGame();
            })
            .catch(error => {
                if (btn) {
                    btn.disabled = false;
                    btn.textContent = '▶ Login';
                }
                alert('Login failed: ' + error.message);
            });
    }

    // ============================================================
    // Galaxy Loading
    // ============================================================

    function loadGalaxies() {
        const galaxySelect = document.getElementById('registerGalaxy');
        const base = GameAPI.getServerUrl();
        fetch((base ? base : '') + '/api/v1/auth/galaxies')
            .then(response => response.json())
            .then(galaxies => {
                galaxySelect.innerHTML = '';
                galaxies.forEach(name => {
                    const option = document.createElement('option');
                    option.value = name;
                    option.textContent = name;
                    galaxySelect.appendChild(option);
                });
            })
            .catch(() => {
                galaxySelect.innerHTML = '<option value="Milky Way">Milky Way</option>';
            });
    }

    // ============================================================
    // Form Events
    // ============================================================

    function bindFormEvents() {
        bindServerEvents();

        // Registration form
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            registerErrorMessage.style.display = 'none';

            const username = document.getElementById('registerUsername').value.trim();
            const password = document.getElementById('registerPassword').value;
            const empireName = document.getElementById('registerEmpireName').value.trim();
            const governmentType = document.getElementById('registerGovernmentType').value;
            const ethos = document.getElementById('registerEthos').value;
            const galaxyName = document.getElementById('registerGalaxy').value;

            if (!username || !password) {
                showRegisterError('Please fill in all required fields.');
                return;
            }

            if (username.length < 3) {
                showRegisterError('Commander name must be at least 3 characters.');
                return;
            }

            if (password.length < 4) {
                showRegisterError('Access code must be at least 4 characters.');
                return;
            }

            submitRegister.disabled = true;
            submitRegister.innerHTML = '⏳ Creating Empire...';

            const base = GameAPI.getServerUrl();
            fetch((base ? base : '') + '/api/v1/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: username,
                    password: password,
                    empireName: empireName || undefined,
                    governmentType: governmentType,
                    ethos: ethos,
                    galaxyName: galaxyName
                })
            })
            .then(response => {
                if (response.ok || response.status === 201) {
                    return response.json();
                } else if (response.status === 400) {
                    throw new Error('Username already exists. Choose another name.');
                } else {
                    throw new Error('Registration failed. Please try again.');
                }
            })
            .then(data => {
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                if (data.pin) localStorage.setItem('playerPin', data.pin);
                if (data.empireName) localStorage.setItem('empireName', data.empireName);

                if (data.pin && registrationSuccessBox) {
                    generatedPinDisplay.textContent = data.pin;
                    registrationSuccessBox.style.display = 'block';
                    setTimeout(() => goToGame(), 2500);
                } else {
                    goToGame();
                }
            })
            .catch(error => {
                showRegisterError(error.message);
            })
            .finally(() => {
                submitRegister.disabled = false;
                submitRegister.innerHTML = '🚀 Create Empire & Get PIN';
            });
        });

        // Password login
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            errorMessage.style.display = 'none';

            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            if (!username || !password) {
                showError('Please enter both username and password.');
                return;
            }

            submitLogin.disabled = true;
            submitLogin.innerHTML = '⏳ Signing in...';

            GameAPI.login(username, password)
                .then(() => {
                    loginForm.reset();
                    goToGame();
                })
                .catch(() => {
                    showError('Invalid username or password. Check your credentials and try again.');
                })
                .finally(() => {
                    submitLogin.disabled = false;
                    submitLogin.innerHTML = '🚀 Sign In';
                });
        });

        // PIN login
        pinLoginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            pinErrorMessage.style.display = 'none';

            const pin = document.getElementById('playerPinInput').value.trim();
            if (!pin) {
                showPinError('Please enter your 4-6 digit Player PIN.');
                return;
            }

            submitPinLogin.disabled = true;
            submitPinLogin.innerHTML = '⏳ Validating PIN...';

            GameAPI.pinLogin(pin)
                .then(() => {
                    pinLoginForm.reset();
                    goToGame();
                })
                .catch(error => {
                    showPinError(error.message || 'Invalid or unregistered Player PIN code.');
                })
                .finally(() => {
                    submitPinLogin.disabled = false;
                    submitPinLogin.innerHTML = '🛡️ Sign In with PIN';
                });
        });

        // Health refresh button
        if (healthRefreshBtn) {
            healthRefreshBtn.addEventListener('click', loadHealthStatus);
        }
    }

    // ============================================================
    // Navigation Events
    // ============================================================

    function bindNavigationEvents() {
        newEmpireBtn.addEventListener('click', function() {
            loginView.style.display = 'none';
            registerView.style.display = 'block';
            loadGalaxies();
        });

        backToLoginBtn.addEventListener('click', function() {
            registerView.style.display = 'none';
            loginView.style.display = 'block';
            registerForm.reset();
            registerErrorMessage.style.display = 'none';
        });

        logoutBtn.addEventListener('click', function() {
            GameAPI.clearAuth();
            checkAuth();
        });
    }

    // ============================================================
    // Session Management
    // ============================================================

    async function goToGame() {
        const storedSession = localStorage.getItem('doramcu_session_id');
        if (storedSession) {
            try {
                const state = await GameAPI.getGameState(storedSession, true);
                if (state) {
                    window.location.href = 'game.html';
                    return;
                }
            } catch (e) {
                localStorage.removeItem('doramcu_session_id');
            }
        }
        // No valid session — create one
        const username = localStorage.getItem('username') || 'Commander';
        try {
            const result = await GameAPI.createSession(username + "'s Galaxy");
            localStorage.setItem('doramcu_session_id', result.sessionId);
            window.location.href = 'game.html';
        } catch (err) {
            alert('Failed to create session: ' + err.message);
        }
    }

    function checkAuth() {
        const token = GameAPI.getToken();
        const username = GameAPI.getUsername();

        if (token && username) {
            // Already logged in — go straight to the game
            goToGame();
        } else {
            // Not logged in — show login view
            loginView.style.display = 'block';
            registerView.style.display = 'none';
            stopHealthCheck();
        }
    }

    // ============================================================
    // Error Display Helpers
    // ============================================================

    function showError(message) {
        errorText.textContent = message;
        errorMessage.style.display = 'block';
    }

    function showPinError(message) {
        pinErrorText.textContent = message;
        pinErrorMessage.style.display = 'block';
    }

    function showRegisterError(message) {
        registerErrorText.textContent = message;
        registerErrorMessage.style.display = 'block';
    }

    // ============================================================
    // Utility
    // ============================================================

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text || '';
        return div.innerHTML;
    }

    // ============================================================
    // Public API
    // ============================================================

    return {
        init,
        showTab,
        loadEmpireList,
        loadHealthStatus,
        updateServerStatus,
        checkAuth,
        goToGame
    };
})();

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', LoginPanel.init);
} else {
    LoginPanel.init();
}
