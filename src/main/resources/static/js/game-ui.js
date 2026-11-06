/* DoraMCU 4X Space Strategy - UI Logic
 * 
 * Module structure:
 *   - Galaxy Progress (lines ~40-147)
 *   - Element Cache & Events (lines ~149-322)
 *   - Auth & Sessions (lines ~324-495)
 *   - Game State (lines ~497-640)
 *   - Subsector Panel (lines ~642-696)
 *   - Game Actions (lines ~714-729)
 *   - Turn Phases (lines ~731-975)
 *   - Build Ship & Fleet Move (lines ~977-1018)
 *   - SSE Listeners (lines ~1033-1076)
 *   - Event Log (lines ~1078-1097)
 *   - Galaxy Map (lines ~1099-1312)
 *   - Utilities (lines ~1314-1326)
 */

const GameUI = (function() {
    let gameState = null;
    let sessionId = null;
    let selectedFleet = null;
    let selectedPlanet = null;

    // Turn phase state
    let currentPhase = 0; // 0 = none, 1-4 = phase index
    const PHASES = ['resource', 'shipBuild', 'fleetMove', 'combat'];
    const PHASE_NAMES = {
        resource: '⚡ Resource Collection',
        shipBuild: '🔨 Ship Building',
        fleetMove: '🎯 Fleet Movement',
        combat: '⚔️ Combat Phase'
    };
    const PHASE_MODAL_IDS = {
        resource: 'resourcePhaseModal',
        shipBuild: 'shipBuildPhaseModal',
        fleetMove: 'fleetMovePhaseModal',
        combat: 'combatPhaseModal'
    };

    // Turn timer state
    let turnTimerInterval = null;
    let turnTimerRemaining = 0;
    let turnTimerTotal = 5;

    // DOM elements
    let elements = {};

    // Galaxy creation progress state
    let galaxyProgressEventSource = null;

    function init() {
        cacheElements();
        bindEvents();
        checkAuth();
        setupSSEListeners();
    }

    // ============================================================
    // GALAXY PROGRESS
    // ============================================================
    function checkGalaxyProgress() {
        // Close any existing connection first
        if (galaxyProgressEventSource) {
            galaxyProgressEventSource.close();
            galaxyProgressEventSource = null;
        }

        // Only connect if authenticated
        const token = localStorage.getItem('token');
        if (!token) {
            return;
        }

        // Connect to the galaxy progress SSE endpoint
        const baseUrl = window.location.origin;
        try {
            galaxyProgressEventSource = new EventSource(baseUrl + '/api/v1/galaxy/progress');

            galaxyProgressEventSource.addEventListener('start', (e) => {
                showGalaxyCreationModal();
                const data = JSON.parse(e.data);
                updateGalaxyProgress(0, data.totalSystems);
                addGalaxyLog('Galaxy generation started. Target: ' + data.totalSystems + ' solar systems.');
                updateGalaxyStatus('🌌 Generating galaxy...');
            });

            galaxyProgressEventSource.addEventListener('progress', (e) => {
                const data = JSON.parse(e.data);
                updateGalaxyProgress(data.systemsGenerated, data.totalSystems);
            });

            galaxyProgressEventSource.addEventListener('log', (e) => {
                const data = JSON.parse(e.data);
                addGalaxyLog(data.message);
            });

            galaxyProgressEventSource.addEventListener('complete', (e) => {
                const data = JSON.parse(e.data);
                updateGalaxyStatus('✅ Galaxy generation complete!');
                addGalaxyLog('Galaxy generation complete!');
                // Show the Done button so the user can close the modal manually
                const closeBtn = document.getElementById('galaxyCloseBtn');
                if (closeBtn) closeBtn.classList.remove('hidden');
            });

            galaxyProgressEventSource.addEventListener('galaxy_ready', (e) => {
                // Galaxy already exists, no need to show modal
                // Silently close the connection since galaxy is already ready
                if (galaxyProgressEventSource) {
                    galaxyProgressEventSource.close();
                    galaxyProgressEventSource = null;
                }
            });

            let errorCount = 0;
            galaxyProgressEventSource.onerror = (e) => {
                errorCount++;
                // After 3 consecutive errors, close the connection to prevent infinite reconnection spam
                if (errorCount >= 3) {
                    if (galaxyProgressEventSource) {
                        galaxyProgressEventSource.close();
                        galaxyProgressEventSource = null;
                    }
                }
            };
        } catch (err) {
            console.error('Failed to connect to galaxy progress:', err);
        }
    }

    function showGalaxyCreationModal() {
        const modal = document.getElementById('galaxyCreationModal');
        if (modal) {
            modal.classList.remove('hidden');
            // Reset state
            updateGalaxyProgress(0, 0);
            const log = document.getElementById('galaxyCreationLog');
            if (log) log.innerHTML = '';
        }
    }

    function hideGalaxyCreationModal() {
        const modal = document.getElementById('galaxyCreationModal');
        if (modal) {
            modal.classList.add('hidden');
        }
        // Close the SSE connection
        if (galaxyProgressEventSource) {
            galaxyProgressEventSource.close();
            galaxyProgressEventSource = null;
        }
    }

    function updateGalaxyProgress(generated, total) {
        const percent = total > 0 ? Math.round((generated / total) * 100) : 0;

        const percentEl = document.getElementById('galaxyProgressPercent');
        if (percentEl) percentEl.textContent = percent + '%';

        const fillEl = document.getElementById('galaxyProgressFill');
        if (fillEl) fillEl.style.width = percent + '%';

        const genEl = document.getElementById('galaxySystemsGenerated');
        if (genEl) genEl.textContent = generated + ' systems';

        const totalEl = document.getElementById('galaxySystemsTotal');
        if (totalEl) totalEl.textContent = 'of ' + total + ' total';
    }

    function addGalaxyLog(message) {
        const log = document.getElementById('galaxyCreationLog');
        if (!log) return;

        const entry = document.createElement('div');
        entry.className = 'galaxy-log-entry';
        const now = new Date();
        const time = now.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
        entry.textContent = '[' + time + '] ' + message;
        log.appendChild(entry);
        log.scrollTop = log.scrollHeight;

        // Limit log entries
        while (log.children.length > 50) {
            log.removeChild(log.firstChild);
        }
    }

    function updateGalaxyStatus(message) {
        const statusEl = document.getElementById('galaxyCreationStatus');
        if (statusEl) statusEl.textContent = message;
    }

    // ============================================================
    // ELEMENT CACHE & EVENT BINDING
    // ============================================================
    function cacheElements() {
        elements = {
            loginOverlay: document.getElementById('loginOverlay'),
            loginForm: document.getElementById('loginForm'),
            loginError: document.getElementById('loginError'),
            loginUsername: document.getElementById('loginUsername'),
            loginPassword: document.getElementById('loginPassword'),
            loginBtn: document.getElementById('loginBtn'),
            gameServerSelect: document.getElementById('gameServerSelect'),
            gameCustomServerContainer: document.getElementById('gameCustomServerContainer'),
            gameCustomServerUrl: document.getElementById('gameCustomServerUrl'),
            tabCommanderLogin: document.getElementById('tabCommanderLogin'),
            tabPinLogin: document.getElementById('tabPinLogin'),
            pinLoginForm: document.getElementById('pinLoginForm'),
            loginPinInput: document.getElementById('loginPinInput'),
            loginPinBtn: document.getElementById('loginPinBtn'),
            loginPinError: document.getElementById('loginPinError'),
            sessionServerIndicator: document.getElementById('sessionServerIndicator'),
            joinByPinInput: document.getElementById('joinByPinInput'),
            joinByPinBtn: document.getElementById('joinByPinBtn'),
            joinByPinError: document.getElementById('joinByPinError'),
            logoutBtn: document.getElementById('logoutBtn'),
            sessionOverlay: document.getElementById('sessionOverlay'),
            gameContainer: document.getElementById('gameContainer'),
            sessionList: document.getElementById('sessionList'),
            sessionNameInput: document.getElementById('sessionNameInput'),
            createSessionBtn: document.getElementById('createSessionBtn'),
            backToLoginBtn: document.getElementById('backToLoginBtn'),
            registerUsername: document.getElementById('registerUsername'),
            registerPassword: document.getElementById('registerPassword'),
            registerPlayerBtn: document.getElementById('registerPlayerBtn'),
            registerError: document.getElementById('registerError'),
            turnIndicator: document.getElementById('turnIndicator'),
            turnIndicatorText: document.getElementById('turnIndicatorText'),
            currentPlayerName: document.getElementById('currentPlayerName'),
            currentPlayerLabel: document.getElementById('currentPlayerLabel'),
            currentTurn: document.getElementById('currentTurn'),
            turnNumber: document.getElementById('turnNumber'),
            turnTimer: document.getElementById('turnTimer'),
            turnTimerValue: document.getElementById('turnTimerValue'),
            turnInterval: document.getElementById('turnInterval'),
            turnIntervalValue: document.getElementById('turnIntervalValue'),
            turnProgressBar: document.getElementById('turnProgressBar'),
            turnProgressFill: document.getElementById('turnProgressFill'),
            schedulerStatus: document.getElementById('schedulerStatus'),
            schedulerDot: document.getElementById('schedulerDot'),
            schedulerLabel: document.getElementById('schedulerLabel'),
            playerName: document.getElementById('playerName'),
            empireName: document.getElementById('empireName'),
            empireGovernment: document.getElementById('empireGovernment'),
            empireEthos: document.getElementById('empireEthos'),
            empirePlanets: document.getElementById('empirePlanets'),
            empireFleets: document.getElementById('empireFleets'),
            empireCapital: document.getElementById('empireCapital'),
            // Army component
            armyUnitCount: document.getElementById('armyUnitCount'),
            armyOffense: document.getElementById('armyOffense'),
            armyDefense: document.getElementById('armyDefense'),
            armySupplyCost: document.getElementById('armySupplyCost'),
            // Navy component
            navyFleetCount: document.getElementById('navyFleetCount'),
            navyShipCount: document.getElementById('navyShipCount'),
            navyDamage: document.getElementById('navyDamage'),
            navyShields: document.getElementById('navyShields'),
            navyHull: document.getElementById('navyHull'),
            // Facilities component
            facilityCount: document.getElementById('facilityCount'),
            spaceStationCount: document.getElementById('spaceStationCount'),
            mineralProduction: document.getElementById('mineralProduction'),
            energyProduction: document.getElementById('energyProduction'),
            foodProduction: document.getElementById('foodProduction'),
            researchOutput: document.getElementById('researchOutput'),
            facilityDefenseBonus: document.getElementById('facilityDefenseBonus'),
            creditIncome: document.getElementById('creditIncome'),
            resourceMinerals: document.getElementById('resourceMinerals'),
            resourceEnergy: document.getElementById('resourceEnergy'),
            resourceInfluence: document.getElementById('resourceInfluence'),
            resourceScience: document.getElementById('resourceScience'),
            resourceFood: document.getElementById('resourceFood'),
            playerList: document.getElementById('playerList'),
            fleetList: document.getElementById('fleetList'),
            galaxyCanvas: document.getElementById('galaxyCanvas'),
            eventLog: document.getElementById('eventLog'),
            advanceTurnBtn: document.getElementById('advanceTurnBtn'),
            buildShipBtn: document.getElementById('buildShipBtn'),
            moveFleetBtn: document.getElementById('moveFleetBtn'),
            engageCombatBtn: document.getElementById('engageCombatBtn'),
            leaveSessionBtn: document.getElementById('leaveSessionBtn'),
            zoomInBtn: document.getElementById('zoomInBtn'),
            zoomOutBtn: document.getElementById('zoomOutBtn'),
            resetViewBtn: document.getElementById('resetViewBtn'),
            subsectorList: document.getElementById('subsectorList'),
            subsectorGeneratedCount: document.getElementById('subsectorGeneratedCount'),
            subsectorSystemCount: document.getElementById('subsectorSystemCount'),
            sectorMapView: document.getElementById('sectorMapView'),
            sectorDetailView: document.getElementById('sectorDetailView'),
            sectorDetailTitle: document.getElementById('sectorDetailTitle'),
            sectorSubsectors: document.getElementById('sectorSubsectors'),
            sectorSystems: document.getElementById('sectorSystems'),
            systemsList: document.getElementById('systemsList'),
            systemSubsectorTitle: document.getElementById('systemSubsectorTitle'),
            backToSectorMapBtn: document.getElementById('backToSectorMapBtn'),
            backToSubsectorsBtn: document.getElementById('backToSubsectorsBtn'),
            buildShipModal: document.getElementById('buildShipModal'),
            shipTypeSelect: document.getElementById('shipTypeSelect'),
            buildShipConfirm: document.getElementById('buildShipConfirm'),
            buildShipCancel: document.getElementById('buildShipCancel'),
            targetCoords: document.getElementById('targetCoords'),
            // Turn phase modals
            turnPhaseModal: document.getElementById('turnPhaseModal'),
            turnPhaseTitle: document.getElementById('turnPhaseTitle'),
            turnPhaseStep: document.getElementById('turnPhaseStep'),
            phaseProgressFill: document.getElementById('phaseProgressFill'),
            turnPhaseBody: document.getElementById('turnPhaseBody'),
            turnPhaseSkip: document.getElementById('turnPhaseSkip'),
            turnPhaseNext: document.getElementById('turnPhaseNext'),
            // Phase-specific modals
            resourcePhaseModal: document.getElementById('resourcePhaseModal'),
            shipBuildPhaseModal: document.getElementById('shipBuildPhaseModal'),
            fleetMovePhaseModal: document.getElementById('fleetMovePhaseModal'),
            combatPhaseModal: document.getElementById('combatPhaseModal'),
            // Phase action buttons
            resourcePhaseSkip: document.getElementById('resourcePhaseSkip'),
            resourcePhaseNext: document.getElementById('resourcePhaseNext'),
            shipBuildPhaseSkip: document.getElementById('shipBuildPhaseSkip'),
            shipBuildPhaseNext: document.getElementById('shipBuildPhaseNext'),
            fleetMovePhaseSkip: document.getElementById('fleetMovePhaseSkip'),
            fleetMovePhaseNext: document.getElementById('fleetMovePhaseNext'),
            combatPhaseSkip: document.getElementById('combatPhaseSkip'),
            combatPhaseNext: document.getElementById('combatPhaseNext'),
            // Phase data containers
            phaseResourceSummary: document.getElementById('phaseResourceSummary'),
            phaseShipQueue: document.getElementById('phaseShipQueue'),
            phaseFleetList: document.getElementById('phaseFleetList'),
            phaseEnemyList: document.getElementById('phaseEnemyList'),
            phasePipelineDetails: document.getElementById('phasePipelineDetails'),
            viewPipelineDetailsBtn: document.getElementById('viewPipelineDetailsBtn'),
        };
    }

    function bindEvents() {
        // Login & PIN tabs
        if (elements.loginForm) {
            elements.loginForm.addEventListener('submit', handleLogin);
        }
        if (elements.pinLoginForm) {
            elements.pinLoginForm.addEventListener('submit', handlePinLogin);
        }
        if (elements.tabCommanderLogin && elements.tabPinLogin) {
            elements.tabCommanderLogin.addEventListener('click', () => {
                elements.tabCommanderLogin.classList.add('primary');
                elements.tabPinLogin.classList.remove('primary');
                if (elements.loginForm) elements.loginForm.style.display = 'block';
                if (elements.pinLoginForm) elements.pinLoginForm.style.display = 'none';
                hideLoginError();
            });
            elements.tabPinLogin.addEventListener('click', () => {
                elements.tabPinLogin.classList.add('primary');
                elements.tabCommanderLogin.classList.remove('primary');
                if (elements.pinLoginForm) elements.pinLoginForm.style.display = 'block';
                if (elements.loginForm) elements.loginForm.style.display = 'none';
                hideLoginError();
            });
        }

        // Server selection
        if (elements.gameServerSelect) {
            const stored = localStorage.getItem('doramcu_server_url') || '';
            if (stored === '' || stored === 'http://localhost:8080') {
                elements.gameServerSelect.value = stored;
            } else if (stored) {
                elements.gameServerSelect.value = 'custom';
                if (elements.gameCustomServerContainer) elements.gameCustomServerContainer.style.display = 'block';
                if (elements.gameCustomServerUrl) elements.gameCustomServerUrl.value = stored;
            }

            elements.gameServerSelect.addEventListener('change', () => {
                if (elements.gameServerSelect.value === 'custom') {
                    if (elements.gameCustomServerContainer) elements.gameCustomServerContainer.style.display = 'block';
                } else {
                    if (elements.gameCustomServerContainer) elements.gameCustomServerContainer.style.display = 'none';
                    GameAPI.setServerUrl(elements.gameServerSelect.value);
                    updateSessionServerIndicator();
                }
            });
        }
        if (elements.gameCustomServerUrl) {
            elements.gameCustomServerUrl.addEventListener('change', () => {
                GameAPI.setServerUrl(elements.gameCustomServerUrl.value);
                updateSessionServerIndicator();
            });
        }

        // Join with PIN
        if (elements.joinByPinBtn) {
            elements.joinByPinBtn.addEventListener('click', handleJoinByPin);
        }

        if (elements.logoutBtn) {
            elements.logoutBtn.addEventListener('click', handleLogout);
        }

        // Session management
        if (elements.createSessionBtn) {
            elements.createSessionBtn.addEventListener('click', handleCreateSession);
        }
        if (elements.backToLoginBtn) {
            elements.backToLoginBtn.addEventListener('click', handleBackToLogin);
        }
        if (elements.registerPlayerBtn) {
            elements.registerPlayerBtn.addEventListener('click', handleRegisterPlayer);
        }
        if (elements.joinByPinBtn) {
            elements.joinByPinBtn.addEventListener('click', handleJoinByPin);
        }

        // Game actions
        if (elements.advanceTurnBtn) {
            elements.advanceTurnBtn.addEventListener('click', handleAdvanceTurn);
        }
        if (elements.buildShipBtn) {
            elements.buildShipBtn.addEventListener('click', handleBuildShipClick);
        }
        if (elements.moveFleetBtn) {
            elements.moveFleetBtn.addEventListener('click', handleMoveFleetClick);
        }
        if (elements.leaveSessionBtn) {
            elements.leaveSessionBtn.addEventListener('click', handleLeaveSession);
        }

        // Build ship modal
        if (elements.buildShipConfirm) {
            elements.buildShipConfirm.addEventListener('click', handleBuildShipConfirm);
        }
        if (elements.buildShipCancel) {
            elements.buildShipCancel.addEventListener('click', () => hideModal('buildShipModal'));
        }

        // Turn phase modals - Resource Collection
        if (elements.resourcePhaseSkip) {
            elements.resourcePhaseSkip.addEventListener('click', () => nextPhase());
        }
        if (elements.resourcePhaseNext) {
            elements.resourcePhaseNext.addEventListener('click', () => nextPhase());
        }
        // Pipeline details toggle
        if (elements.viewPipelineDetailsBtn) {
            elements.viewPipelineDetailsBtn.addEventListener('click', loadPipelineDetails);
        }

        // Turn phase modals - Ship Building
        if (elements.shipBuildPhaseSkip) {
            elements.shipBuildPhaseSkip.addEventListener('click', () => nextPhase());
        }
        if (elements.shipBuildPhaseNext) {
            elements.shipBuildPhaseNext.addEventListener('click', () => nextPhase());
        }

        // Turn phase modals - Fleet Movement
        if (elements.fleetMovePhaseSkip) {
            elements.fleetMovePhaseSkip.addEventListener('click', () => nextPhase());
        }
        if (elements.fleetMovePhaseNext) {
            elements.fleetMovePhaseNext.addEventListener('click', () => nextPhase());
        }

        // Turn phase modals - Combat
        if (elements.combatPhaseSkip) {
            elements.combatPhaseSkip.addEventListener('click', () => endTurnPhases());
        }
        if (elements.combatPhaseNext) {
            elements.combatPhaseNext.addEventListener('click', () => endTurnPhases());
        }

        // Phase option item clicks
        document.addEventListener('click', (e) => {
            const optionItem = e.target.closest('.phase-option-item');
            if (optionItem) {
                const action = optionItem.dataset.action;
                handlePhaseAction(action);
            }
        });

        // Galaxy map
        if (elements.galaxyCanvas) {
            setupGalaxyMap();
        }

        // Zoom controls
        if (elements.zoomInBtn) {
            elements.zoomInBtn.addEventListener('click', () => zoomGalaxy(1.2));
        }
        if (elements.zoomOutBtn) {
            elements.zoomOutBtn.addEventListener('click', () => zoomGalaxy(0.8));
        }
        if (elements.resetViewBtn) {
            elements.resetViewBtn.addEventListener('click', () => resetGalaxyView());
        }

        // Auth expired
        window.addEventListener('auth-expired', () => {
            showLogin();
        });
    }

    // ============================================================
    // AUTH & SESSIONS
    // ============================================================
    function checkAuth() {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        if (token && username) {
            elements.playerName.textContent = username;
            checkGalaxyProgress();
            showSessionSelection();
        } else {
            showLogin();
        }
    }

    function showLogin() {
        if (elements.loginOverlay) elements.loginOverlay.classList.remove('hidden');
        if (elements.sessionOverlay) elements.sessionOverlay.classList.add('hidden');
        if (elements.gameContainer) elements.gameContainer.classList.add('hidden');
    }

    function showSessionSelection() {
        if (elements.loginOverlay) elements.loginOverlay.classList.add('hidden');
        if (elements.sessionOverlay) elements.sessionOverlay.classList.remove('hidden');
        if (elements.gameContainer) elements.gameContainer.classList.add('hidden');
        updateSessionServerIndicator();
        loadSessions();
    }

    function updateSessionServerIndicator() {
        if (!elements.sessionServerIndicator) return;
        const base = GameAPI.getServerUrl();
        elements.sessionServerIndicator.textContent = 'Server: ' + (base || 'Default Host');
    }

    function showGame() {
        if (elements.loginOverlay) elements.loginOverlay.classList.add('hidden');
        if (elements.sessionOverlay) elements.sessionOverlay.classList.add('hidden');
        if (elements.gameContainer) elements.gameContainer.classList.remove('hidden');
    }

    // Login handler
    async function handleLogin(e) {
        e.preventDefault();
        const username = elements.loginUsername.value.trim();
        const password = elements.loginPassword.value;
        if (!username || !password) {
            showLoginError('Please enter username and password');
            return;
        }
        elements.loginBtn.disabled = true;
        elements.loginBtn.textContent = 'Signing in...';
        hideLoginError();

        try {
            await GameAPI.login(username, password);
            elements.playerName.textContent = username;
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            showLoginError('Invalid username or password. Check your credentials and try again.');
        } finally {
            elements.loginBtn.disabled = false;
            elements.loginBtn.textContent = 'Sign In';
        }
    }

    // Player PIN Login handler
    async function handlePinLogin(e) {
        e.preventDefault();
        const pin = elements.loginPinInput ? elements.loginPinInput.value.trim() : '';
        if (!pin) {
            showLoginError('Please enter your 4-6 digit Player PIN code');
            return;
        }
        if (elements.loginPinBtn) {
            elements.loginPinBtn.disabled = true;
            elements.loginPinBtn.textContent = 'Validating PIN...';
        }
        hideLoginError();

        try {
            const data = await GameAPI.pinLogin(pin);
            elements.playerName.textContent = data.username || data.empireName || 'Commander';
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            showLoginError(err.message || 'Invalid or unregistered Player PIN code');
        } finally {
            if (elements.loginPinBtn) {
                elements.loginPinBtn.disabled = false;
                elements.loginPinBtn.textContent = 'Sign In with PIN';
            }
        }
    }

    // Join Session with Player PIN
    async function handleJoinByPin() {
        const pin = elements.joinByPinInput ? elements.joinByPinInput.value.trim() : '';
        if (!pin) {
            if (elements.joinByPinError) {
                elements.joinByPinError.textContent = 'Please enter a 4-6 digit Player PIN code';
                elements.joinByPinError.style.display = 'block';
            }
            return;
        }
        try {
            if (elements.joinByPinError) elements.joinByPinError.style.display = 'none';
            const data = await GameAPI.pinLogin(pin);
            elements.playerName.textContent = data.username || data.empireName || 'Commander';
            showToast('Player PIN connected: ' + (data.empireName || data.username), 'success');
            loadSessions();
        } catch (err) {
            if (elements.joinByPinError) {
                elements.joinByPinError.textContent = err.message || 'Failed to authenticate PIN';
                elements.joinByPinError.style.display = 'block';
            }
        }
    }

    function handleLogout() {
        GameSSE.disconnect();
        GameAPI.clearAuth();
        sessionId = null;
        gameState = null;
        showLogin();
    }

    function handleBackToLogin() {
        GameSSE.disconnect();
        GameAPI.clearAuth();
        sessionId = null;
        gameState = null;
        showLogin();
    }

    // Register new player handler
    async function handleRegisterPlayer() {
        const username = elements.registerUsername ? elements.registerUsername.value.trim() : '';
        const password = elements.registerPassword ? elements.registerPassword.value : '';
        const errorEl = elements.registerError;

        if (!username || !password) {
            if (errorEl) {
                errorEl.textContent = 'Please enter both username and password';
                errorEl.style.display = 'block';
            }
            return;
        }

        if (username.length < 3) {
            if (errorEl) {
                errorEl.textContent = 'Username must be at least 3 characters long';
                errorEl.style.display = 'block';
            }
            return;
        }

        if (password.length < 4) {
            if (errorEl) {
                errorEl.textContent = 'Password must be at least 4 characters long';
                errorEl.style.display = 'block';
            }
            return;
        }

        if (errorEl) {
            errorEl.textContent = '';
            errorEl.style.display = 'none';
        }

        try {
            const data = await GameAPI.register(username, password);
            elements.playerName.textContent = data.username || username;
            if (data.pin) {
                showToast('Registered! Your Player PIN is: ' + data.pin, 'success');
            }
            showSessionSelection();
        } catch (err) {
            if (errorEl) {
                // Provide helpful error message based on status code
                let message = err.message || 'Registration failed';
                if (err.status === 400) {
                    if (message.toLowerCase().includes('username')) {
                        message = 'Username is already taken or invalid (min 3 characters)';
                    } else if (message.toLowerCase().includes('password')) {
                        message = 'Password must be at least 4 characters long';
                    } else {
                        message = 'Invalid registration data. Username: min 3 chars. Password: min 4 chars.';
                    }
                }
                errorEl.textContent = message;
                errorEl.style.display = 'block';
            }
        }
    }

    function showLoginError(message) {
        if (elements.loginError && elements.loginForm.style.display !== 'none') {
            elements.loginError.textContent = message;
            elements.loginError.style.display = 'block';
        }
        if (elements.loginPinError && elements.pinLoginForm && elements.pinLoginForm.style.display !== 'none') {
            elements.loginPinError.textContent = message;
            elements.loginPinError.style.display = 'block';
        }
    }

    function hideLoginError() {
        if (elements.loginError) {
            elements.loginError.style.display = 'none';
        }
        if (elements.loginPinError) {
            elements.loginPinError.style.display = 'none';
        }
    }

    // Session management
    async function loadSessions() {
        try {
            const sessions = await GameAPI.listSessions();
            renderSessionList(sessions || []);
        } catch (err) {
            addEventLog('ERROR', 'Failed to load sessions: ' + err.message);
        }
    }

    function renderSessionList(sessions) {
        if (!elements.sessionList) return;
        elements.sessionList.innerHTML = '';
        if (!sessions.length) {
            elements.sessionList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No active sessions. Create one to start playing.</div>';
            return;
        }
        sessions.forEach(session => {
            const item = document.createElement('div');
            item.className = 'session-item';
            item.innerHTML = `
                <div>
                    <div class="session-name">${escapeHtml(session.name || session.sessionId)}</div>
                    <div class="session-players">${session.playerIds ? session.playerIds.length : 0} player(s)</div>
                </div>
                <div class="session-actions">
                    <button class="btn-small primary" data-session-id="${session.sessionId}">Join</button>
                </div>
            `;
            elements.sessionList.appendChild(item);
        });

        // Bind join buttons
        elements.sessionList.querySelectorAll('[data-session-id]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-session-id');
                await joinSession(id);
            });
        });
    }

    async function handleCreateSession() {
        const name = elements.sessionNameInput.value.trim();
        if (!name) {
            alert('Please enter a session name');
            return;
        }
        elements.createSessionBtn.disabled = true;
        try {
            const result = await GameAPI.createSession(name);
            sessionId = result.sessionId;
            enterSession();
        } catch (err) {
            alert('Failed to create session: ' + err.message);
        } finally {
            elements.createSessionBtn.disabled = false;
        }
    }

    async function joinSession(id) {
        try {
            await GameAPI.joinSession(id);
            sessionId = id;
            enterSession();
        } catch (err) {
            if (err.alreadyInSession) {
                // Player is already in this session — proceed directly to the game
                sessionId = id;
                enterSession();
            } else {
                alert('Failed to join session: ' + err.message);
            }
        }
    }

    function enterSession() {
        showGame();
        // Resize canvas now that the game container is visible (was 0x0 while hidden)
        resizeCanvas();
        GameSSE.connect(sessionId);
        loadGameState();
    }

    async function handleLeaveSession() {
        if (!sessionId) return;
        stopSubsectorPolling();
        try {
            await GameAPI.leaveSession(sessionId);
        } catch (err) {
            console.error('Leave session error:', err);
        }
        GameSSE.disconnect();
        sessionId = null;
        gameState = null;
        showSessionSelection();
    }

    // ============================================================
    // GAME STATE
    // ============================================================
    let gameStatePollTimer = null;

    async function loadGameState() {
        if (!sessionId) return;
        try {
            const state = await GameAPI.getGameState(sessionId);
            if (state) {
                gameState = state;
                renderGameState();
                // If subSectors are still empty, poll for up to 30 seconds (galaxy may be generating)
                if (!state.subSectors || state.subSectors.length === 0) {
                    console.log('SubSectors empty, polling for galaxy generation...');
                    startSubsectorPolling();
                } else {
                    console.log('SubSectors loaded:', state.subSectors.length, 'subsectors');
                    stopSubsectorPolling();
                }
            }
        } catch (err) {
            console.error('Failed to load game state:', err);
            addEventLog('ERROR', 'Failed to load game state: ' + err.message);
        }
    }

    function startSubsectorPolling() {
        stopSubsectorPolling();
        let attempts = 0;
        const maxAttempts = 30; // Poll for up to 30 seconds
        gameStatePollTimer = setInterval(async () => {
            attempts++;
            if (attempts >= maxAttempts) {
                stopSubsectorPolling();
                return;
            }
            try {
                const state = await GameAPI.getGameState(sessionId);
                if (state && state.subSectors && state.subSectors.length > 0) {
                    console.log('SubSectors now available:', state.subSectors.length, 'subsectors');
                    gameState = state;
                    renderGameState();
                    stopSubsectorPolling();
                }
            } catch (err) {
                console.error('Polling error:', err);
            }
        }, 1000);
    }

    function stopSubsectorPolling() {
        if (gameStatePollTimer) {
            clearInterval(gameStatePollTimer);
            gameStatePollTimer = null;
        }
    }

    function renderGameState() {
        if (!gameState) return;

        // Turn number
        if (elements.turnNumber) elements.turnNumber.textContent = gameState.currentTurn;
        if (elements.currentTurn) {
            // Keep the icon, update the number span
            const turnNumEl = elements.currentTurn.querySelector('#turnNumber');
            if (turnNumEl) turnNumEl.textContent = gameState.currentTurn;
        }

        // Simultaneous turns: all players can act at any time
        if (elements.currentPlayerLabel) elements.currentPlayerLabel.textContent = 'All Players';
        if (elements.currentPlayerName) {
            const labelEl = elements.currentPlayerName.querySelector('#currentPlayerLabel');
            if (labelEl) labelEl.textContent = 'All Players';
        }

        // Turn indicator - always active for simultaneous turns
        if (elements.turnIndicator) {
            if (elements.turnIndicatorText) elements.turnIndicatorText.textContent = 'ALL PLAYERS ACTIVE';
            elements.turnIndicator.className = 'turn-indicator my-turn';
        }

        // Turn timer and progress bar
        if (gameState.schedulerEnabled && gameState.turnIntervalSeconds > 0) {
            turnTimerTotal = gameState.turnIntervalSeconds;
            turnTimerRemaining = Math.max(0, turnTimerTotal - gameState.accumulatedSeconds);
            updateTurnTimerDisplay();
            updateTurnProgressBar();
            updateTurnIntervalDisplay();
            updateSchedulerStatus();
            startTurnTimerCountdown();
        } else {
            stopTurnTimerCountdown();
            if (elements.turnTimerValue) elements.turnTimerValue.textContent = '--';
            if (elements.turnProgressFill) elements.turnProgressFill.style.width = '0%';
        }

        // Resources — use pipeline data if available, otherwise fall back to player resources
        const myPlayer = gameState.players?.find(p => p.id === getCurrentPlayerId());
        const pipeline = gameState.pipelineSummary;
        if (pipeline) {
            // Pipeline modules displayed as resources
            const modules = pipeline.modules || {};
            const raw = pipeline.rawResources || {};
            const refined = pipeline.refinedResources || {};
            if (elements.resourceMinerals) elements.resourceMinerals.textContent = modules.HULL_MODULE || 0;
            if (elements.resourceEnergy) elements.resourceEnergy.textContent = modules.WEAPON_MODULE || 0;
            if (elements.resourceInfluence) elements.resourceInfluence.textContent = modules.SHIELD_MODULE || 0;
            if (elements.resourceScience) elements.resourceScience.textContent = modules.ENGINE_MODULE || 0;
            if (elements.resourceFood) elements.resourceFood.textContent = modules.FACILITY_MODULE || 0;
            // Update resource change indicators with mining rates
            const mining = pipeline.miningRate || {};
            if (elements.resourceMineralsChange) {
                elements.resourceMineralsChange.textContent = '+' + (mining.ORE || 0);
                elements.resourceMineralsChange.className = 'resource-change positive';
            }
            if (elements.resourceEnergyChange) {
                elements.resourceEnergyChange.textContent = '+' + (mining.GAS || 0);
                elements.resourceEnergyChange.className = 'resource-change positive';
            }
            if (elements.resourceInfluenceChange) {
                elements.resourceInfluenceChange.textContent = pipeline.activeProcessing + '/' + pipeline.miningOperations;
                elements.resourceInfluenceChange.className = 'resource-change';
            }
            if (elements.resourceScienceChange) {
                elements.resourceScienceChange.textContent = pipeline.constructionQueueSize + ' queue';
                elements.resourceScienceChange.className = 'resource-change';
            }
            if (elements.resourceFoodChange) {
                elements.resourceFoodChange.textContent = pipeline.activeAssemblers + ' assemblers';
                elements.resourceFoodChange.className = 'resource-change';
            }
        } else if (myPlayer && myPlayer.resources) {
            // Fallback: show pipeline resources from player summary
            if (elements.resourceMinerals) elements.resourceMinerals.textContent = myPlayer.resources.HULL_MODULE || myPlayer.resources.ORE || 0;
            if (elements.resourceEnergy) elements.resourceEnergy.textContent = myPlayer.resources.WEAPON_MODULE || myPlayer.resources.GAS || 0;
            if (elements.resourceInfluence) elements.resourceInfluence.textContent = myPlayer.resources.SHIELD_MODULE || 0;
            if (elements.resourceScience) elements.resourceScience.textContent = myPlayer.resources.ENGINE_MODULE || 0;
            if (elements.resourceFood) elements.resourceFood.textContent = myPlayer.resources.FACILITY_MODULE || 0;
        }

        // Empire Info Panel
        const empire = gameState.empireSummary;
        if (empire) {
            if (elements.empireName) elements.empireName.textContent = empire.empireName || '—';
            if (elements.empireGovernment) elements.empireGovernment.textContent = empire.governmentType || '—';
            if (elements.empireEthos) elements.empireEthos.textContent = empire.ethos || '—';
            if (elements.empirePlanets) elements.empirePlanets.textContent = empire.planetCount || 0;
            if (elements.empireFleets) elements.empireFleets.textContent = empire.fleetCount || 0;
            if (elements.empireCapital) elements.empireCapital.textContent = empire.capitalPlanetId != null ? 'Planet #' + empire.capitalPlanetId : '—';

            // Army component
            if (elements.armyUnitCount) elements.armyUnitCount.textContent = empire.armyUnitCount || 0;
            if (elements.armyOffense) elements.armyOffense.textContent = empire.armyTotalOffense || 0;
            if (elements.armyDefense) elements.armyDefense.textContent = empire.armyTotalDefense || 0;
            if (elements.armySupplyCost) elements.armySupplyCost.textContent = empire.armySupplyCost || 0;

            // Navy component
            if (elements.navyFleetCount) elements.navyFleetCount.textContent = empire.navyFleetCount || 0;
            if (elements.navyShipCount) elements.navyShipCount.textContent = empire.navyShipCount || 0;
            if (elements.navyDamage) elements.navyDamage.textContent = empire.navyTotalDamage || 0;
            if (elements.navyShields) elements.navyShields.textContent = empire.navyTotalShieldPoints || 0;
            if (elements.navyHull) elements.navyHull.textContent = empire.navyTotalHullPoints || 0;

            // Facilities component
            if (elements.facilityCount) elements.facilityCount.textContent = empire.facilityCount || 0;
            if (elements.spaceStationCount) elements.spaceStationCount.textContent = empire.spaceStationCount || 0;
            if (elements.mineralProduction) elements.mineralProduction.textContent = empire.mineralProduction || 0;
            if (elements.energyProduction) elements.energyProduction.textContent = empire.energyProduction || 0;
            if (elements.foodProduction) elements.foodProduction.textContent = empire.foodProduction || 0;
            if (elements.researchOutput) elements.researchOutput.textContent = empire.researchOutput || 0;
            if (elements.facilityDefenseBonus) elements.facilityDefenseBonus.textContent = empire.facilityDefenseBonus || 0;
            if (elements.creditIncome) elements.creditIncome.textContent = empire.creditIncome || 0;
        }

        // Player list
        if (elements.playerList) {
            elements.playerList.innerHTML = '';
            // Simultaneous turns: no current player highlighting
            (gameState.players || []).forEach(player => {
                const item = document.createElement('div');
                const isAi = player.isAi === true;
                let className = 'item-list-item';
                if (isAi) className += ' ai-player';
                item.className = className;

                let namePrefix = '';
                if (isAi) {
                    const diff = player.difficulty || 'MEDIUM';
                    namePrefix = `<span class="ai-badge" title="AI - ${diff}">🤖 ${diff}</span> `;
                }
                item.innerHTML = `
                    <div class="player-item-header">
                        ${namePrefix}<span class="item-name">${escapeHtml(player.name)}</span>
                    </div>
                    <span class="item-detail">${player.planetCount} planets, ${player.fleetCount} fleets</span>
                `;
                elements.playerList.appendChild(item);
            });
        }

        // Fleet list — use fleet data from game state
        if (elements.fleetList) {
            elements.fleetList.innerHTML = '';
            const fleets = gameState.fleets || [];
            if (fleets.length > 0) {
                fleets.forEach(fleet => {
                    const item = document.createElement('div');
                    item.className = 'item-list-item' + (selectedFleet === fleet.id ? ' selected' : '');
                    const healthPercent = Math.round(fleet.healthRatio * 100);
                    let healthClass = 'health-good';
                    if (healthPercent < 20) healthClass = 'health-critical';
                    else if (healthPercent < 50) healthClass = 'health-warning';
                    else if (healthPercent < 80) healthClass = 'health-damaged';
                    const jDriveStatus = fleet.effectiveJDriveSpeed > 0
                        ? '<span class="drive-status drive-ok" title="JDrive operational">🚀</span>'
                        : '<span class="drive-status drive-disabled" title="JDrive disabled — health too low">🚫</span>';
                    item.innerHTML = `
                        <div class="fleet-item-header">
                            <span class="item-name">${escapeHtml(fleet.name)}</span>
                            ${jDriveStatus}
                        </div>
                        <div class="fleet-item-stats">
                            <span class="item-detail">🚢 ${fleet.shipCount} ship(s)</span>
                            <span class="item-detail">⚔️ ${fleet.totalDamage} dmg</span>
                        </div>
                        <div class="fleet-item-drives">
                            <span class="drive-info mdrive" title="MDrive (within-system): ${fleet.effectiveMDriveSpeed} range">
                                🎯 M:${fleet.effectiveMDriveSpeed}
                            </span>
                            <span class="drive-info jdrive" title="JDrive (between-system): ${fleet.effectiveJDriveSpeed}">
                                🌌 J:${fleet.effectiveJDriveSpeed}
                            </span>
                        </div>
                        <div class="fleet-item-health ${healthClass}">
                            <div class="health-bar-bg">
                                <div class="health-bar-fill" style="width: ${healthPercent}%"></div>
                            </div>
                            <span class="health-text">${healthPercent}%</span>
                        </div>
                        <div class="fleet-item-position">
                            <span class="item-detail">Pos: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})</span>
                        </div>
                    `;
                    item.addEventListener('click', () => {
                        selectedFleet = fleet.id;
                        renderGameState();
                        showFleetDetail(fleet);
                    });
                    elements.fleetList.appendChild(item);
                });
            } else {
                elements.fleetList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No fleets</div>';
            }
        }

        // Simultaneous turns: all action buttons are always enabled
        if (elements.advanceTurnBtn) elements.advanceTurnBtn.disabled = false;
        if (elements.buildShipBtn) elements.buildShipBtn.disabled = false;
        if (elements.moveFleetBtn) elements.moveFleetBtn.disabled = false;
        if (elements.engageCombatBtn) elements.engageCombatBtn.disabled = false;

        // Render galaxy map
        renderGalaxyMap();

        // Render subsector panel
        renderSubsectorPanel();
    }

    // ============================================================
    // SUBSECTOR PANEL
    // ============================================================
    let selectedSubSector = null;

    function renderSubsectorPanel() {
        if (!gameState || !elements.subsectorList) {
            console.log('renderSubsectorPanel: gameState or subsectorList not available');
            return;
        }

        const subSectors = gameState.subSectors || [];
        const totalSystems = subSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);

        console.log('renderSubsectorPanel:', subSectors.length, 'subsectors,', totalSystems, 'total systems');

        if (elements.subsectorGeneratedCount) {
            elements.subsectorGeneratedCount.textContent = subSectors.length;
        }
        if (elements.subsectorSystemCount) {
            elements.subsectorSystemCount.textContent = totalSystems;
        }

        elements.subsectorList.innerHTML = '';
        if (subSectors.length === 0) {
            elements.subsectorList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">⏳ Waiting for galaxy data...</div>';
            return;
        }

        subSectors.forEach(subSector => {
            const item = document.createElement('div');
            item.className = 'subsector-item' + (selectedSubSector === subSector.id ? ' selected' : '');
            const systemCount = subSector.systemCount || 0;
            item.innerHTML = `
                <div class="subsector-name">
                    <span>🔹</span>
                    <span>${escapeHtml(subSector.name || 'SubSector ' + subSector.id)}</span>
                </div>
                <div class="subsector-coords">[${subSector.subRow}, ${subSector.subCol}]</div>
                <div class="subsector-systems">⭐ ${systemCount} system(s)</div>
            `;
            item.addEventListener('click', () => {
                selectedSubSector = subSector.id;
                renderSubsectorPanel();
                showSubSectorDetail(subSector);
            });
            elements.subsectorList.appendChild(item);
        });
    }

    function showSubSectorDetail(subSector) {
        if (!subSector || !subSector.systems) return;
        addEventLog('INFO', `Viewing subsector ${subSector.name || subSector.id} with ${subSector.systemCount} systems`);
        subSector.systems.forEach(sys => {
            addEventLog('INFO', `  System ${sys.id}: ${sys.starCount || 0} stars, ${sys.planetCount || 0} planets`);
        });
    }

    function getCurrentPlayerId() {
        // Extract from username or use a hash
        const username = GameAPI.getUsername();
        if (!username) return null;
        // Try to parse as number first
        const num = parseInt(username);
        if (!isNaN(num)) return num;
        // Fallback: hash
        let hash = 0;
        for (let i = 0; i < username.length; i++) {
            hash = ((hash << 5) - hash) + username.charCodeAt(i);
            hash |= 0;
        }
        return Math.abs(hash);
    }

    // ============================================================
    // GAME ACTIONS
    // ============================================================
    async function handleAdvanceTurn() {
        if (!sessionId || !gameState?.isMyTurn) return;
        if (!elements.advanceTurnBtn) return;
        elements.advanceTurnBtn.disabled = true;
        try {
            // Start turn phases instead of directly advancing
            startTurnPhases();
        } catch (err) {
            addEventLog('ERROR', 'Advance turn failed: ' + err.message);
        } finally {
            if (gameState?.isMyTurn) {
                elements.advanceTurnBtn.disabled = false;
            }
        }
    }

    // ============================================================
    // TURN PHASES
    // ============================================================
    function startTurnPhases() {
        currentPhase = 1;
        showPhaseModal(PHASES[0]);
    }

    function showPhaseModal(phaseName) {
        // Hide all phase modals
        Object.values(PHASE_MODAL_IDS).forEach(id => hideModal(id));

        const modalId = PHASE_MODAL_IDS[phaseName];
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('hidden');
        }

        // Populate phase data
        switch (phaseName) {
            case 'resource':
                populateResourcePhase();
                break;
            case 'shipBuild':
                populateShipBuildPhase();
                break;
            case 'fleetMove':
                populateFleetMovePhase();
                break;
            case 'combat':
                populateCombatPhase();
                break;
        }

        addEventLog('PHASE', `Entered ${PHASE_NAMES[phaseName]} phase`);
    }

    function nextPhase() {
        if (currentPhase >= PHASES.length) {
            endTurnPhases();
            return;
        }
        const nextPhaseName = PHASES[currentPhase];
        currentPhase++;
        showPhaseModal(nextPhaseName);
    }

    async function endTurnPhases() {
        // Hide all phase modals
        Object.values(PHASE_MODAL_IDS).forEach(id => hideModal(id));
        currentPhase = 0;

        // Now advance the turn on the server
        if (!sessionId) return;
        try {
            await GameAPI.advanceTurn(sessionId);
            addEventLog('ACTION', 'Turn completed and advanced');
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'End turn failed: ' + err.message);
        }
    }

    function handlePhaseAction(action) {
        const phaseName = PHASES[currentPhase - 1];
        if (!phaseName) return;

        switch (phaseName) {
            case 'resource':
                handleResourcePhaseAction(action);
                break;
            case 'shipBuild':
                handleShipBuildPhaseAction(action);
                break;
            case 'fleetMove':
                handleFleetMovePhaseAction(action);
                break;
            case 'combat':
                handleCombatPhaseAction(action);
                break;
        }
    }

    function handleResourcePhaseAction(action) {
        switch (action) {
            case 'view-production':
                addEventLog('INFO', 'Viewing production details...');
                break;
            case 'allocate-resources':
                addEventLog('INFO', 'Resource allocation coming soon...');
                break;
        }
    }

    async function handleShipBuildPhaseAction(action) {
        const shipTypeMap = {
            'build-frigate': 'FRIGATE',
            'build-destroyer': 'DESTROYER',
            'build-cruiser': 'CRUISER',
            'build-battleship': 'BATTLESHIP'
        };
        const shipType = shipTypeMap[action];
        if (!shipType || !sessionId) return;

        try {
            await GameAPI.buildShip(sessionId, shipType, selectedFleet);
            addEventLog('ACTION', `Queued ${shipType} for construction`);
            await loadGameState();
            populateShipBuildPhase();
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
        }
    }

    function handleFleetMovePhaseAction(action) {
        switch (action) {
            case 'move-selected-fleet':
                if (!selectedFleet) {
                    addEventLog('WARN', 'No fleet selected');
                    return;
                }
                const coords = elements.targetCoords?.value;
                if (!coords) {
                    addEventLog('WARN', 'Select a target on the map first');
                    return;
                }
                const parts = coords.split(',').map(s => parseInt(s.trim()));
                if (parts.length === 2) {
                    moveFleetAction(parts[0], parts[1]);
                }
                break;
            case 'scout-sector':
                addEventLog('INFO', 'Scout sector coming soon...');
                break;
        }
    }

    function handleCombatPhaseAction(action) {
        switch (action) {
            case 'engage-combat':
                addEventLog('INFO', 'Select enemy fleet to engage');
                break;
            case 'defensive-stance':
                addEventLog('INFO', 'Defensive stance coming soon...');
                break;
        }
    }

    function populateResourcePhase() {
        const container = elements.phaseResourceSummary;
        if (!container || !gameState) return;

        const pipeline = gameState.pipelineSummary;
        if (pipeline) {
            const modules = pipeline.modules || {};
            const raw = pipeline.rawResources || {};
            const refined = pipeline.refinedResources || {};
            const mining = pipeline.miningRate || {};
            container.innerHTML = `
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🔩 Hull Modules</span>
                    <span class="phase-resource-value">${modules.HULL_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🔫 Weapon Modules</span>
                    <span class="phase-resource-value">${modules.WEAPON_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🛡️ Shield Modules</span>
                    <span class="phase-resource-value">${modules.SHIELD_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">⚙️ Engine Modules</span>
                    <span class="phase-resource-value">${modules.ENGINE_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🏗️ Facility Modules</span>
                    <span class="phase-resource-value">${modules.FACILITY_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">⛏️ Raw Resources</span>
                    <span class="phase-resource-value">${Object.values(raw).reduce((a, b) => a + b, 0)}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🔧 Refined Resources</span>
                    <span class="phase-resource-value">${Object.values(refined).reduce((a, b) => a + b, 0)}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🏭 Mining Ops</span>
                    <span class="phase-resource-value">${pipeline.miningOperations} (+${Object.values(mining).reduce((a, b) => a + b, 0)}/turn)</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🔬 Processing</span>
                    <span class="phase-resource-value">${pipeline.activeProcessing}/${pipeline.miningOperations}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">📦 Construction Queue</span>
                    <span class="phase-resource-value">${pipeline.constructionQueueSize}</span>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🔩 Hull Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🔫 Weapon Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">🛡️ Shield Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">⚙️ Engine Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
            `;
        }
    }

    async function loadPipelineDetails() {
        const container = elements.phasePipelineDetails;
        if (!container || !currentSessionId) return;

        const isHidden = container.classList.contains('hidden');
        if (!isHidden) {
            container.classList.add('hidden');
            return;
        }

        try {
            const response = await gameApi.get(`/sessions/${currentSessionId}/pipeline/details`);
            const details = response.data;

            let html = '<div class="pipeline-details-grid">';

            // Mining Operations
            html += '<div class="pipeline-section"><h4>⛏️ Mining Operations</h4>';
            if (details.miningOperations && details.miningOperations.length > 0) {
                details.miningOperations.forEach(op => {
                    html += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${op.celestialBodyName} (${op.celestialBodyType})</span>
                        <span class="pipeline-item-value">`;
                    const rates = op.productionRate || {};
                    html += Object.entries(rates).map(([k, v]) => `${k}=${v}`).join(', ');
                    html += `</span>
                        <span class="pipeline-item-meta">Storage: ${op.storage ? Object.entries(op.storage).map(([k,v]) => `${k}=${v}`).join(', ') : 'N/A'}</span>
                    </div>`;
                });
            } else {
                html += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active mining</div>';
            }
            html += '</div>';

            // Transport Orders
            html += '<div class="pipeline-section"><h4>🚚 Transport Orders</h4>';
            if (details.transportOrders && details.transportOrders.length > 0) {
                details.transportOrders.forEach(order => {
                    html += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${order.resourceType}: ${order.totalAmount}</span>
                        <span class="pipeline-item-value">${order.turnsCompleted || 0}/${order.turnsTotal} turns</span>
                        <span class="pipeline-item-meta">${Math.round(order.progress * 100)}% complete</span>
                    </div>`;
                });
            } else {
                html += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active transports</div>';
            }
            html += '</div>';

            // Construction Queue
            html += '<div class="pipeline-section"><h4>🏗️ Construction Queue</h4>';
            if (details.constructionOrders && details.constructionOrders.length > 0) {
                details.constructionOrders.forEach(order => {
                    const name = order.stationType || order.planetFacility || 'Unknown';
                    html += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${name} (${order.constructionType})</span>
                        <span class="pipeline-item-value">${order.turnsCompleted}/${order.totalTurns} turns</span>
                        <span class="pipeline-item-meta">${Math.round(order.progress * 100)}% complete</span>
                    </div>`;
                });
            } else {
                html += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active construction</div>';
            }
            html += '</div></div>';

            container.innerHTML = html;
            container.classList.remove('hidden');
        } catch (err) {
            addEventLog('ERROR', 'Failed to load pipeline details: ' + err.message);
        }
    }

    function populateShipBuildPhase() {
        const container = elements.phaseShipQueue;
        if (!container) return;

        // Show available modules for building
        const pipeline = gameState?.pipelineSummary;
        const modules = pipeline?.modules || {};
        container.innerHTML = `
            <div class="phase-resource-item">
                <span class="phase-resource-name">🔩 Hull Modules</span>
                <span class="phase-resource-value">${modules.HULL_MODULE || 0}</span>
            </div>
            <div class="phase-resource-item">
                <span class="phase-resource-name">🔫 Weapon Modules</span>
                <span class="phase-resource-value">${modules.WEAPON_MODULE || 0}</span>
            </div>
            <div class="phase-resource-item">
                <span class="phase-resource-name">🛡️ Shield Modules</span>
                <span class="phase-resource-value">${modules.SHIELD_MODULE || 0}</span>
            </div>
            <div class="phase-resource-item">
                <span class="phase-resource-name">⚙️ Engine Modules</span>
                <span class="phase-resource-value">${modules.ENGINE_MODULE || 0}</span>
            </div>
        `;
    }

    function populateFleetMovePhase() {
        const container = elements.phaseFleetList;
        if (!container) return;

        const fleets = gameState?.fleets || [];
        if (!fleets.length) {
            container.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No fleets available</div>';
            return;
        }

        container.innerHTML = '';
        fleets.forEach(fleet => {
            const item = document.createElement('div');
            const healthPercent = Math.round((fleet.healthRatio || 0) * 100);
            let healthClass = 'health-good';
            if (healthPercent < 20) healthClass = 'health-critical';
            else if (healthPercent < 50) healthClass = 'health-warning';
            else if (healthPercent < 80) healthClass = 'health-damaged';
            const jDriveOk = (fleet.effectiveJDriveSpeed || 0) > 0;
            item.className = 'phase-fleet-item' + (selectedFleet === fleet.id ? ' selected' : '');
            item.innerHTML = `
                <div class="phase-fleet-header">
                    <span class="phase-option-name">🚀 ${escapeHtml(fleet.name || 'Fleet ' + fleet.id)}</span>
                    <span class="phase-option-desc">${fleet.shipCount || 0} ships</span>
                </div>
                <div class="phase-fleet-stats">
                    <span class="drive-badge mdrive" title="MDrive range: ${fleet.effectiveMDriveSpeed || 0}">
                        🎯 M:${fleet.effectiveMDriveSpeed || 0}
                    </span>
                    <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}" title="JDrive: ${fleet.effectiveJDriveSpeed || 0}">
                        🌌 J:${fleet.effectiveJDriveSpeed || 0}
                    </span>
                    <span class="phase-option-desc">⚔️ ${fleet.totalDamage || 0} dmg</span>
                </div>
                <div class="phase-fleet-health ${healthClass}">
                    <div class="health-bar-bg">
                        <div class="health-bar-fill" style="width: ${healthPercent}%"></div>
                    </div>
                    <span class="health-text">${healthPercent}% hull</span>
                </div>
                <div class="phase-fleet-position">
                    <span class="phase-option-desc">Pos: (${fleet.positionX || 0}, ${fleet.positionY || 0}, ${fleet.positionZ || 0})</span>
                </div>
            `;
            item.addEventListener('click', () => {
                selectedFleet = fleet.id;
                populateFleetMovePhase();
                addEventLog('SYSTEM', `Selected fleet: ${fleet.name || fleet.id}`);
            });
            container.appendChild(item);
        });
    }

    function populateCombatPhase() {
        const container = elements.phaseEnemyList;
        if (!container) return;

        // Show enemy fleets (from other players)
        const allFleets = gameState?.fleets || [];
        const myFleetIds = new Set(allFleets.filter(f => f.ownerId === getCurrentPlayerId()).map(f => f.id));
        const enemyFleets = allFleets.filter(f => !myFleetIds.has(f.id));

        if (!enemyFleets.length) {
            container.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No enemy fleets detected</div>';
            return;
        }

        container.innerHTML = '';
        enemyFleets.forEach(fleet => {
            const item = document.createElement('div');
            item.className = 'phase-enemy-item';
            item.innerHTML = `
                <span class="phase-option-name">👾 ${escapeHtml(fleet.name || 'Enemy Fleet')}</span>
                <span class="phase-option-desc">${fleet.shipCount || '?'} ships</span>
            `;
            container.appendChild(item);
        });
    }

    // ============================================================
    // BUILD SHIP & FLEET MOVE
    // ============================================================
    function handleBuildShipClick() {
        if (!elements.buildShipModal) return;
        elements.buildShipModal.classList.remove('hidden');
        if (elements.shipTypeSelect) {
            elements.shipTypeSelect.value = 'FRIGATE';
        }
    }

    async function handleBuildShipConfirm() {
        if (!sessionId) return;
        const shipType = elements.shipTypeSelect?.value || 'FRIGATE';
        hideModal('buildShipModal');
        try {
            await GameAPI.buildShip(sessionId, shipType, selectedFleet);
            addEventLog('ACTION', `Building ${shipType}`);
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
        }
    }

    function handleMoveFleetClick() {
        if (!selectedFleet) {
            showToast('Select a fleet first', 'warning');
            return;
        }
        // Find the selected fleet data
        const fleet = (gameState.fleets || []).find(f => f.id === selectedFleet);
        if (!fleet) {
            showToast('Selected fleet not found', 'error');
            return;
        }
        showMoveFleetModal(fleet);
    }

    function showMoveFleetModal(fleet) {
        // Create modal if it doesn't exist
        let modal = document.getElementById('moveFleetModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'moveFleetModal';
            modal.className = 'modal-overlay hidden';
            modal.innerHTML = `
                <div class="modal-content move-fleet-modal">
                    <div class="modal-header">
                        <h3><svg class="icon"><use href="#icon-target"></use></svg> Move Fleet</h3>
                        <button class="modal-close" id="moveFleetModalClose">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="fleet-move-info">
                            <div class="fleet-move-name" id="moveFleetName"></div>
                            <div class="fleet-move-position" id="moveFleetPosition"></div>
                            <div class="fleet-move-drives" id="moveFleetDrives"></div>
                        </div>
                        <div class="move-type-selector">
                            <button class="move-type-btn active" data-type="mdrive" id="moveTypeMDrive">
                                <span class="move-type-icon">🎯</span>
                                <span class="move-type-label">MDrive</span>
                                <span class="move-type-desc">Within-system movement</span>
                            </button>
                            <button class="move-type-btn" data-type="jdrive" id="moveTypeJDrive">
                                <span class="move-type-icon">🌌</span>
                                <span class="move-type-label">JDrive</span>
                                <span class="move-type-desc">Between-system jump</span>
                            </button>
                        </div>
                        <div class="move-form" id="moveForm">
                            <div class="form-row" id="moveCoordRow">
                                <div class="form-group">
                                    <label for="moveTargetX">Target X</label>
                                    <input type="number" class="form-input" id="moveTargetX" value="0">
                                </div>
                                <div class="form-group">
                                    <label for="moveTargetY">Target Y</label>
                                    <input type="number" class="form-input" id="moveTargetY" value="0">
                                </div>
                            </div>
                            <div class="form-row hidden" id="moveSystemRow">
                                <div class="form-group">
                                    <label for="moveTargetSystem">Target Solar System ID</label>
                                    <input type="number" class="form-input" id="moveTargetSystem" min="1" placeholder="Enter system ID">
                                </div>
                            </div>
                            <div class="move-range-info" id="moveRangeInfo"></div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn-small" id="moveFleetCancel">Cancel</button>
                        <button class="btn-small primary" id="moveFleetExecute">Execute Move</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            // Bind events
            document.getElementById('moveFleetModalClose').addEventListener('click', () => hideModal('moveFleetModal'));
            document.getElementById('moveFleetCancel').addEventListener('click', () => hideModal('moveFleetModal'));
            document.getElementById('moveFleetExecute').addEventListener('click', () => executeMoveFleet(fleet));
            document.getElementById('moveTypeMDrive').addEventListener('click', () => setMoveType('mdrive'));
            document.getElementById('moveTypeJDrive').addEventListener('click', () => setMoveType('jdrive'));
        }
        // Populate fleet info
        document.getElementById('moveFleetName').textContent = fleet.name;
        document.getElementById('moveFleetPosition').textContent = `Position: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})`;
        const jDriveOk = fleet.effectiveJDriveSpeed > 0;
        document.getElementById('moveFleetDrives').innerHTML = `
            <span class="drive-badge mdrive">🎯 MDrive: ${fleet.effectiveMDriveSpeed} range</span>
            <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}">🌌 JDrive: ${fleet.effectiveJDriveSpeed}${jDriveOk ? '' : ' (disabled)'}</span>
        `;
        // Set initial move type
        setMoveType('mdrive');
        // Set default target coords
        document.getElementById('moveTargetX').value = fleet.positionX;
        document.getElementById('moveTargetY').value = fleet.positionY;
        showModal('moveFleetModal');
    }

    let currentMoveType = 'mdrive';

    function setMoveType(type) {
        currentMoveType = type;
        document.querySelectorAll('.move-type-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.type === type);
        });
        const coordRow = document.getElementById('moveCoordRow');
        const systemRow = document.getElementById('moveSystemRow');
        const rangeInfo = document.getElementById('moveRangeInfo');
        if (type === 'mdrive') {
            coordRow.classList.remove('hidden');
            systemRow.classList.add('hidden');
            const fleet = (gameState.fleets || []).find(f => f.id === selectedFleet);
            if (fleet) {
                rangeInfo.textContent = `MDrive range: ${fleet.effectiveMDriveSpeed} hexes from current position`;
                rangeInfo.className = 'move-range-info';
            }
        } else {
            coordRow.classList.add('hidden');
            systemRow.classList.remove('hidden');
            const fleet = (gameState.fleets || []).find(f => f.id === selectedFleet);
            if (fleet) {
                if (fleet.effectiveJDriveSpeed <= 0) {
                    rangeInfo.textContent = '⚠️ JDrive is disabled — fleet health is below 20%';
                    rangeInfo.className = 'move-range-info warning';
                } else {
                    rangeInfo.textContent = 'JDrive: Enter target solar system ID (must have hyperlane connection)';
                    rangeInfo.className = 'move-range-info';
                }
            }
        }
    }

    async function executeMoveFleet(fleet) {
        hideModal('moveFleetModal');
        if (!sessionId) return;
        try {
            let targetX, targetY, solarSystemId;
            if (currentMoveType === 'mdrive') {
                targetX = parseInt(document.getElementById('moveTargetX').value) || 0;
                targetY = parseInt(document.getElementById('moveTargetY').value) || 0;
                solarSystemId = null;
                addEventLog('ACTION', `MDrive: Moving ${fleet.name} to (${targetX}, ${targetY})`);
            } else {
                targetX = 0;
                targetY = 0;
                solarSystemId = parseInt(document.getElementById('moveTargetSystem').value) || null;
                if (!solarSystemId) {
                    showToast('Enter a valid target system ID', 'warning');
                    return;
                }
                addEventLog('ACTION', `JDrive: Jumping ${fleet.name} to system ${solarSystemId}`);
            }
            await GameAPI.moveFleet(sessionId, selectedFleet, targetX, targetY, solarSystemId);
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'Move fleet failed: ' + err.message);
            showToast('Move failed: ' + err.message, 'error');
        }
    }

    function showFleetDetail(fleet) {
        // Update the right panel or show a toast with fleet details
        const healthPercent = Math.round(fleet.healthRatio * 100);
        const shieldPercent = fleet.totalShieldPoints > 0 ? Math.round(fleet.currentShieldPoints / fleet.totalShieldPoints * 100) : 0;
        addEventLog('INFO', `Selected ${fleet.name}: MDrive=${fleet.effectiveMDriveSpeed}, JDrive=${fleet.effectiveJDriveSpeed}, Health=${healthPercent}%, Shields=${shieldPercent}%, Damage=${fleet.totalDamage}`);
    }

    async function handleLeaveSession() {
        if (!sessionId) return;
        stopTurnTimerCountdown();
        try {
            await GameAPI.leaveSession(sessionId);
        } catch (err) {
            console.error('Leave session error:', err);
        }
        GameSSE.disconnect();
        sessionId = null;
        gameState = null;
        showSessionSelection();
    }

    // ============================================================
    // SSE LISTENERS
    // ============================================================
    function setupSSEListeners() {
        GameSSE.on('TURN_CHANGED', (data) => {
            const payload = data.payload || {};
            const turn = payload.turn || '?';
            const playerName = payload.currentPlayerName || payload.playerName || '?';
            const autoAdvanced = payload.autoAdvanced === true;
            
            if (autoAdvanced) {
                showToast(`⏩ Auto-advanced to Turn ${turn}: ${playerName}`, 'info');
            }
            addEventLog('TURN_CHANGED', `Turn ${turn}: ${playerName}${autoAdvanced ? ' (auto)' : ''}`);
            loadGameState();
        });

        GameSSE.on('ACTION_RESULT', (data) => {
            addEventLog('ACTION_RESULT', data.payload?.message || 'Action completed');
            loadGameState();
        });

        GameSSE.on('PLAYER_JOINED', (data) => {
            addEventLog('PLAYER_JOINED', `${data.payload?.playerName || 'Player'} joined the game`);
            loadGameState();
        });

        GameSSE.on('PLAYER_LEFT', (data) => {
            addEventLog('PLAYER_LEFT', `${data.payload?.playerName || 'Player'} left the game`);
            loadGameState();
        });

        GameSSE.on('GAME_STATE', (data) => {
            loadGameState();
        });

        GameSSE.on('connected', () => {
            addEventLog('SYSTEM', 'Connected to game server');
        });

        GameSSE.on('disconnected', (data) => {
            // Only log disconnection if it's not a clean disconnect (e.g., user leaving)
            if (data && data.sessionId) {
                addEventLog('WARNING', 'Disconnected from game server, reconnecting...');
            }
        });

        GameSSE.on('error', (data) => {
            // Suppress error logs during reconnection attempts
            if (data && data.message) {
                addEventLog('WARNING', 'SSE connection issue, reconnecting...');
            }
        });
    }

    // ============================================================
    // EVENT LOG
    // ============================================================
    function addEventLog(type, message) {
        if (!elements.eventLog) return;
        const entry = document.createElement('div');
        entry.className = 'event-entry fade-in';
        const now = new Date();
        const time = now.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
        entry.innerHTML = `
            <span class="event-time">${time}</span>
            <span class="event-type ${type}">${type}</span>
            <span class="event-message">${escapeHtml(message)}</span>
        `;
        elements.eventLog.appendChild(entry);
        elements.eventLog.scrollTop = elements.eventLog.scrollHeight;

        // Limit log entries
        while (elements.eventLog.children.length > 100) {
            elements.eventLog.removeChild(elements.eventLog.firstChild);
        }
    }

    // ============================================================
    // TURN TIMER
    // ============================================================
    function startTurnTimerCountdown() {
        stopTurnTimerCountdown();
        if (turnTimerTotal <= 0) return;
        
        turnTimerInterval = setInterval(() => {
            turnTimerRemaining--;
            if (turnTimerRemaining <= 0) {
                turnTimerRemaining = 0;
            }
            updateTurnTimerDisplay();
            updateTurnProgressBar();
        }, 1000);
    }

    function stopTurnTimerCountdown() {
        if (turnTimerInterval) {
            clearInterval(turnTimerInterval);
            turnTimerInterval = null;
        }
    }

    function updateTurnTimerDisplay() {
        const timerEl = elements.turnTimerValue;
        const timerContainer = elements.turnTimer;
        if (!timerEl) return;
        
        timerEl.textContent = turnTimerRemaining;
        
        // Pulse effect when timer is about to expire (last 2 seconds)
        if (timerContainer) {
            if (turnTimerRemaining <= 2 && turnTimerRemaining > 0) {
                timerContainer.classList.add('urgent');
            } else {
                timerContainer.classList.remove('urgent');
            }
        }
    }

    function updateTurnProgressBar() {
        const fillEl = elements.turnProgressFill;
        if (!fillEl || turnTimerTotal <= 0) return;
        
        const elapsed = turnTimerTotal - turnTimerRemaining;
        const percent = Math.min(100, (elapsed / turnTimerTotal) * 100);
        fillEl.style.width = percent + '%';
        
        // Change color when approaching turn end
        if (turnTimerRemaining <= 2 && turnTimerRemaining > 0) {
            fillEl.classList.add('urgent');
        } else {
            fillEl.classList.remove('urgent');
        }
    }

    function updateTurnIntervalDisplay() {
        const el = elements.turnIntervalValue;
        if (el && turnTimerTotal > 0) {
            el.textContent = turnTimerTotal;
        }
    }

    function updateSchedulerStatus() {
        const dotEl = elements.schedulerDot;
        const labelEl = elements.schedulerLabel;
        if (!dotEl || !labelEl) return;
        
        if (gameState && gameState.schedulerEnabled) {
            dotEl.classList.remove('disabled');
            labelEl.textContent = 'Auto';
        } else {
            dotEl.classList.add('disabled');
            labelEl.textContent = 'Manual';
        }
    }

    // ============================================================
    // GALAXY MAP
    // ============================================================
    let mapScale = 1;
    let mapOffsetX = 0;
    let mapOffsetY = 0;
    let isDragging = false;
    let dragStartX = 0;
    let dragStartY = 0;

    function setupGalaxyMap() {
        const canvas = elements.galaxyCanvas;
        if (!canvas) return;

        canvas.addEventListener('mousedown', (e) => {
            isDragging = true;
            dragStartX = e.clientX - mapOffsetX;
            dragStartY = e.clientY - mapOffsetY;
        });

        canvas.addEventListener('mousemove', (e) => {
            if (!isDragging) return;
            mapOffsetX = e.clientX - dragStartX;
            mapOffsetY = e.clientY - dragStartY;
            renderGalaxyMap();
        });

        canvas.addEventListener('mouseup', () => { isDragging = false; });
        canvas.addEventListener('mouseleave', () => { isDragging = false; });

        // Back to sector map button
        if (elements.backToSectorMapBtn) {
            elements.backToSectorMapBtn.addEventListener('click', () => {
                showSectorMapView();
            });
        }

        // Back to subsectors button
        if (elements.backToSubsectorsBtn) {
            elements.backToSubsectorsBtn.addEventListener('click', () => {
                showSubsectorListInView();
            });
        }

        canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const factor = e.deltaY < 0 ? 1.1 : 0.9;
            zoomGalaxy(factor);
        });

        canvas.addEventListener('click', (e) => {
            if (Math.abs(e.clientX - dragStartX - mapOffsetX) > 5 || Math.abs(e.clientY - dragStartY - mapOffsetY) > 5) return;
            // Handle hex click
            const rect = canvas.getBoundingClientRect();
            const x = (e.clientX - rect.left - mapOffsetX) / mapScale;
            const y = (e.clientY - rect.top - mapOffsetY) / mapScale;
            handleHexClick(x, y);
        });

        window.addEventListener('resize', () => {
            resizeCanvas();
            renderGalaxyMap();
        });

        resizeCanvas();
    }

    function resizeCanvas() {
        const canvas = elements.galaxyCanvas;
        if (!canvas) return;
        const parent = canvas.parentElement;
        canvas.width = parent.clientWidth;
        canvas.height = parent.clientHeight;
    }

    function renderGalaxyMap() {
        const canvas = elements.galaxyCanvas;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Clear
        ctx.fillStyle = '#0a0e17';
        ctx.fillRect(0, 0, w, h);

        ctx.save();
        ctx.translate(mapOffsetX, mapOffsetY);
        ctx.scale(mapScale, mapScale);

        // Draw hex grid
        const hexSize = 30;
        const centerX = w / 2 / mapScale - mapOffsetX / mapScale;
        const centerY = h / 2 / mapScale - mapOffsetY / mapScale;
        const radius = 8;

        for (let q = -radius; q <= radius; q++) {
            for (let r = -radius; r <= radius; r++) {
                if (Math.abs(q + r) > radius) continue;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                drawHex(ctx, x, y, hexSize, '#111827', '#1f2937');
            }
        }

        // Draw subsectors from real game state data
        const subSectors = gameState?.subSectors || [];
        if (subSectors.length > 0) {
            // Color palette for subsectors
            const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'];
            
            subSectors.forEach((subSector, index) => {
                // Map subsector coordinates to hex positions
                const q = subSector.subCol - 5; // Center around (5,5)
                const r = subSector.subRow - 5;
                
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                
                // Color based on system count
                const systemCount = subSector.systemCount || 0;
                let color = colors[index % colors.length];
                let alpha = 0.3 + Math.min(0.7, systemCount * 0.2); // More systems = brighter
                
                // Contested subsectors get a red border
                const isContested = subSector.contested === true;
                const borderColor = isContested ? '#ef4444' : color;
                
                // Draw subsector hex with color
                drawHex(ctx, x, y, hexSize, 
                    color + Math.round(alpha * 255).toString(16).padStart(2, '0'), 
                    borderColor);
                
                // Draw a thicker red outline for contested subsectors
                if (isContested) {
                    drawHex(ctx, x, y, hexSize - 2, 'transparent', '#ef4444');
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }
                
                // Draw system count label
                if (systemCount > 0) {
                    ctx.fillStyle = '#e5e7eb';
                    ctx.font = 'bold 9px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(systemCount.toString(), x, y);
                }

                // Draw trade type label below the hex
                if (subSector.tradeType) {
                    ctx.fillStyle = '#9ca3af';
                    ctx.font = '7px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'top';
                    ctx.fillText(subSector.tradeType, x, y + hexSize + 2);
                }
            });
        } else {
            // Fallback: show loading message
            ctx.fillStyle = '#6b7280';
            ctx.font = '14px sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText('Waiting for galaxy data...', centerX, centerY);
        }

        // Outline sectors containing the player's facilities
        const myEmpireId = gameState?.empireSummary?.empireId;
        if (myEmpireId != null && subSectors.length > 0) {
            subSectors.forEach((subSector) => {
                const ownerIds = subSector.ownerEmpireIds;
                if (ownerIds && Array.isArray(ownerIds) && ownerIds.includes(myEmpireId)) {
                    // This subsector has the player's facilities — draw a gold outline
                    const q = subSector.subCol - 5;
                    const r = subSector.subRow - 5;
                    const x = centerX + hexSize * (3/2 * q);
                    const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                    drawHex(ctx, x, y, hexSize - 1, 'transparent', '#f59e0b');
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }
            });
        }

        // Draw player fleets from game state at their actual subsector positions
        const fleets = gameState?.fleets || [];
        if (fleets.length > 0) {
            fleets.forEach((fleet) => {
                const q = (fleet.subSectorCol || 5) - 5;
                const r = (fleet.subSectorRow || 5) - 5;
                const fx = centerX + hexSize * (3/2 * q);
                const fy = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Draw fleet triangle
                ctx.beginPath();
                ctx.moveTo(fx, fy - 10);
                ctx.lineTo(fx - 7, fy + 5);
                ctx.lineTo(fx + 7, fy + 5);
                ctx.closePath();
                ctx.fillStyle = '#06b6d4';
                ctx.fill();
                ctx.strokeStyle = '#fff';
                ctx.lineWidth = 1;
                ctx.stroke();

                // Draw ship count label
                if (fleet.shipCount > 0) {
                    ctx.fillStyle = '#fff';
                    ctx.font = 'bold 8px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(fleet.shipCount.toString(), fx, fy + 12);
                }
            });
        }

        // Draw enemy indicators at their actual fleet positions
        const enemies = gameState?.players?.filter(p => p.id !== getCurrentPlayerId()) || [];
        if (enemies.length > 0 && fleets.length === 0) {
            // Fallback: show enemy indicators around center when no fleet data available
            enemies.forEach((enemy, i) => {
                const angle = (i / enemies.length) * Math.PI * 2;
                const dist = hexSize * 4;
                const ex = centerX + Math.cos(angle) * dist;
                const ey = centerY + Math.sin(angle) * dist;

                ctx.beginPath();
                ctx.moveTo(ex, ey - 8);
                ctx.lineTo(ex - 6, ey + 4);
                ctx.lineTo(ex + 6, ey + 4);
                ctx.closePath();
                ctx.fillStyle = '#ef4444';
                ctx.fill();

                // Label
                ctx.fillStyle = '#ef4444';
                ctx.font = '9px sans-serif';
                ctx.textAlign = 'center';
                ctx.fillText(enemy.name ? enemy.name.substring(0, 6) : 'Enemy', ex, ey + 14);
            });
        }

        ctx.restore();
    }

    function drawHex(ctx, x, y, size, fill, stroke) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const hx = x + size * Math.cos(angle);
            const hy = y + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(hx, hy);
            else ctx.lineTo(hx, hy);
        }
        ctx.closePath();
        ctx.fillStyle = fill;
        ctx.fill();
        ctx.strokeStyle = stroke;
        ctx.lineWidth = 0.5;
        ctx.stroke();
    }

    function handleHexClick(x, y) {
        // Convert pixel to hex coordinates
        const hexSize = 30;
        const centerX = (elements.galaxyCanvas.width / 2 / mapScale - mapOffsetX / mapScale);
        const centerY = (elements.galaxyCanvas.height / 2 / mapScale - mapOffsetY / mapScale);
        
        // Calculate relative position from center
        const relX = x - centerX;
        const relY = y - centerY;
        
        // Convert to axial hex coordinates
        const col = Math.round(relX / (hexSize * 1.5));
        const row = Math.round(relY / (hexSize * Math.sqrt(3)) - col / 2);
        
        // Map back to subsector coordinates (centered around 5,5)
        const subCol = col + 5;
        const subRow = row + 5;
        
        // Find matching subsector
        const subSectors = gameState?.subSectors || [];
        const matched = subSectors.find(s => s.subCol === subCol && s.subRow === subRow);
        
        if (matched) {
            // Show sector detail view with this subsector's data
            showSectorDetailView(matched);
        } else {
            // No subsector at this hex, show all subsectors for the sector
            addEventLog('SYSTEM', `Selected hex (${col}, ${row}) - no subsector data`);
            if (elements.targetCoords) {
                elements.targetCoords.value = `${col}, ${row}`;
            }
        }
    }

    // ============================================================
    // SECTOR DETAIL VIEW
    // ============================================================

    function showSectorDetailView(subSector) {
        if (!elements.sectorMapView || !elements.sectorDetailView) return;
        
        // Hide map, show detail
        elements.sectorMapView.classList.add('hidden');
        elements.sectorDetailView.classList.remove('hidden');
        
        // Set title
        const sectorName = subSector.name || `Sector [${subSector.sectorRow},${subSector.sectorCol}]`;
        if (elements.sectorDetailTitle) {
            elements.sectorDetailTitle.textContent = sectorName;
        }
        
        // Show subsector list, hide systems
        if (elements.sectorSubsectors) elements.sectorSubsectors.classList.remove('hidden');
        if (elements.sectorSystems) elements.sectorSystems.classList.add('hidden');
        
        // Find all subsectors in the same sector
        const subSectors = gameState?.subSectors || [];
        const sectorSubSectors = subSectors.filter(s => 
            s.sectorRow === subSector.sectorRow && s.sectorCol === subSector.sectorCol
        );
        
        // Render subsector cards
        if (elements.sectorSubsectors) {
            elements.sectorSubsectors.innerHTML = '';
            
            if (sectorSubSectors.length === 0) {
                elements.sectorSubsectors.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No subsectors in this sector</div>';
                return;
            }
            
            sectorSubSectors.forEach(ss => {
                const card = document.createElement('div');
                card.className = 'sector-subsector-card' + (ss.contested ? ' contested' : '');
                const systemCount = ss.systemCount || 0;
                const contestedIcon = ss.contested ? ' ⚠️' : '';
                card.innerHTML = `
                    <div class="sector-subsector-name">🔹 ${escapeHtml(ss.name || 'SubSector ' + ss.id)}${contestedIcon}</div>
                    <div class="sector-subsector-coords">[${ss.subRow}, ${ss.subCol}]</div>
                    <div class="sector-subsector-systems">⭐ ${systemCount} system(s)</div>
                    ${ss.contested ? '<div class="sector-subsector-contested">⚔️ Contested Territory</div>' : ''}
                `;
                card.addEventListener('click', () => {
                    showSubsectorGraphicView(ss);
                });
                elements.sectorSubsectors.appendChild(card);
            });
        }
        
        addEventLog('INFO', `Viewing sector ${sectorName} with ${sectorSubSectors.length} subsectors`);
    }

    function showSystemsInView(subSector) {
        if (!elements.sectorSubsectors || !elements.sectorSystems) return;
        
        // Hide subsector list, show systems
        elements.sectorSubsectors.classList.add('hidden');
        elements.sectorSystems.classList.remove('hidden');
        
        if (elements.systemSubsectorTitle) {
            elements.systemSubsectorTitle.textContent = subSector.name || 'SubSector ' + subSector.id;
        }
        
        // Render systems
        if (elements.systemsList) {
            elements.systemsList.innerHTML = '';
            
            const systems = subSector.systems || [];
            if (systems.length === 0) {
                elements.systemsList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No systems generated yet</div>';
                return;
            }
            
            systems.forEach(sys => {
                const card = document.createElement('div');
                card.className = 'system-card';
                
                const starCount = sys.starCount || 0;
                const planetCount = sys.planetCount || 0;
                const systemName = sys.name || `System ${sys.id || '?'}`;
                const systemType = sys.systemType || 'Unknown';
                
                let html = `
                    <div class="system-name">⭐ ${escapeHtml(systemName)}</div>
                    <div class="system-details">
                        <div class="system-detail">
                            <span class="system-detail-label">Type:</span> ${escapeHtml(systemType)}
                        </div>
                        <div class="system-detail">
                            <span class="system-detail-label">Stars:</span> ${starCount}
                        </div>
                        <div class="system-detail">
                            <span class="system-detail-label">Planets:</span> ${planetCount}
                        </div>
                    </div>
                `;
                
                // Show planets if available
                if (sys.planets && sys.planets.length > 0) {
                    html += '<div class="system-planets"><div class="system-planets-title">Planets</div>';
                    sys.planets.forEach(planet => {
                        const planetName = planet.name || `Planet ${planet.id || '?'}`;
                        const planetType = planet.planetType || planet.type || 'Unknown';
                        html += `
                            <div class="planet-item">
                                🪐 ${escapeHtml(planetName)}
                                <span class="planet-type-badge">${escapeHtml(planetType)}</span>
                            </div>
                        `;
                    });
                    html += '</div>';
                }
                
                card.innerHTML = html;
                elements.systemsList.appendChild(card);
            });
        }
        
        addEventLog('INFO', `Viewing ${systems.length} systems in ${subSector.name || subSector.id}`);
    }

    // ============================================================
    // SUBSECTOR GRAPHIC VIEW
    // ============================================================

    function showSubsectorGraphicView(subSector) {
        if (!elements.sectorSubsectors || !elements.sectorSystems) return;
        
        // Hide subsector list, show systems/graphic view
        elements.sectorSubsectors.classList.add('hidden');
        elements.sectorSystems.classList.remove('hidden');
        
        if (elements.systemSubsectorTitle) {
            elements.systemSubsectorTitle.textContent = subSector.name || 'SubSector ' + subSector.id;
        }
        
        // Render the subsector graphic view with hex grid
        renderSubsectorGraphic(subSector);
        
        addEventLog('INFO', `Viewing subsector ${subSector.name || subSector.id} graphic`);
    }

    function renderSubsectorGraphic(subSector) {
        const container = elements.systemsList;
        if (!container) return;
        
        container.innerHTML = '';
        
        // Create a canvas for the subsector graphic
        const wrapper = document.createElement('div');
        wrapper.className = 'subsector-graphic-wrapper';
        
        const canvas = document.createElement('canvas');
        canvas.id = 'subsectorCanvas';
        canvas.className = 'subsector-canvas';
        canvas.width = 500;
        canvas.height = 400;
        wrapper.appendChild(canvas);
        
        // Add legend below the canvas
        const legend = document.createElement('div');
        legend.className = 'subsector-legend';
        
        const systems = subSector.systems || [];
        const systemCount = systems.length || 0;
        const totalStars = systems.reduce((sum, s) => sum + (s.starCount || 0), 0);
        const totalPlanets = systems.reduce((sum, s) => sum + (s.planetCount || 0), 0);
        const totalBodies = systems.reduce((sum, s) => sum + (s.bodyCount || 0), 0);
        
        legend.innerHTML = `
            <div class="legend-row"><span class="legend-item">⭐ Systems: ${systemCount}</span><span class="legend-item">☀️ Stars: ${totalStars}</span><span class="legend-item">🪐 Planets: ${totalPlanets}</span><span class="legend-item">🌌 Bodies: ${totalBodies}</span></div>
            ${subSector.contested ? '<div class="legend-row contested-warning">⚠️ Contested Territory — Enemy empires have systems here</div>' : ''}
        `;
        wrapper.appendChild(legend);
        
        container.appendChild(wrapper);
        
        // Draw the subsector hex grid
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;
        
        // Background
        ctx.fillStyle = '#0a0e17';
        ctx.fillRect(0, 0, w, h);
        
        // Draw starfield background
        for (let i = 0; i < 100; i++) {
            const sx = Math.random() * w;
            const sy = Math.random() * h;
            const brightness = Math.random() * 150 + 50;
            ctx.fillStyle = `rgba(${brightness}, ${brightness}, ${brightness + 50}, 0.5)`;
            ctx.fillRect(sx, sy, 1, 1);
        }
        
        const hexSize = 22;
        const centerX = w / 2;
        const centerY = h / 2 - 20;
        
        // Draw a 3-ring hex grid representing the subsector
        const radius = 3;
        for (let q = -radius; q <= radius; q++) {
            for (let r = -radius; r <= radius; r++) {
                if (Math.abs(q + r) > radius) continue;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                drawHexOnCtx(ctx, x, y, hexSize, '#111827', '#1f2937');
            }
        }
        
        // Draw systems as colored hexes at positions based on system data
        const systemColors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'];
        systems.forEach((sys, idx) => {
            // Position systems in a ring pattern based on their index
            const angle = (idx / Math.max(systems.length, 1)) * Math.PI * 2 - Math.PI / 2;
            const dist = hexSize * (1 + Math.floor(idx / 3) * 1.5);
            const sx = centerX + Math.cos(angle) * dist;
            const sy = centerY + Math.sin(angle) * dist;
            
            const starCount = sys.starCount || 0;
            const planetCount = sys.planetCount || 0;
            const color = systemColors[idx % systemColors.length];
            
            // Draw system hex
            drawHexOnCtx(ctx, sx, sy, hexSize, color + '99', color);
            
            // Draw star icon if has stars
            if (starCount > 0) {
                ctx.fillStyle = '#fbbf24';
                ctx.font = 'bold 12px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('☀', sx, sy - 5);
            }
            
            // Draw planet icon if has planets
            if (planetCount > 0) {
                ctx.fillStyle = '#10b981';
                ctx.font = 'bold 10px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('●', sx, sy + 6);
            }
            
            // Draw system label
            ctx.fillStyle = '#e5e7eb';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText(`S${sys.id || idx + 1}`, sx, sy + hexSize + 10);
        });
        
        // Draw title
        ctx.fillStyle = '#06b6d4';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(subSector.name || `SubSector [${subSector.subRow},${subSector.subCol}]`, centerX, 20);
        
        // Draw contested warning if applicable
        if (subSector.contested) {
            ctx.fillStyle = '#ef4444';
            ctx.font = 'bold 11px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('⚠️ CONTESTED TERRITORY ⚠️', centerX, 40);
        }
        
        // Draw scale indicator
        ctx.fillStyle = '#6b7280';
        ctx.font = '9px sans-serif';
        ctx.textAlign = 'left';
        ctx.fillText(`[${subSector.subRow}, ${subSector.subCol}]`, 10, h - 10);
        
        // Draw ownership info
        if (subSector.ownerEmpireIds && subSector.ownerEmpireIds.size > 0) {
            const empireIds = Array.from(subSector.ownerEmpireIds);
            ctx.fillStyle = '#f59e0b';
            ctx.font = '9px sans-serif';
            ctx.textAlign = 'right';
            ctx.fillText(`Empires: ${empireIds.join(', ')}`, w - 10, h - 10);
        }
    }

    function drawHexOnCtx(ctx, x, y, size, fill, stroke) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const hx = x + size * Math.cos(angle);
            const hy = y + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(hx, hy);
            else ctx.lineTo(hx, hy);
        }
        ctx.closePath();
        ctx.fillStyle = fill;
        ctx.fill();
        ctx.strokeStyle = stroke;
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    function showSectorMapView() {
        if (!elements.sectorMapView || !elements.sectorDetailView) return;
        elements.sectorMapView.classList.remove('hidden');
        elements.sectorDetailView.classList.add('hidden');
        
        // Reset systems view if visible
        if (elements.sectorSubsectors) elements.sectorSubsectors.classList.remove('hidden');
        if (elements.sectorSystems) elements.sectorSystems.classList.add('hidden');
        
        // Re-render the galaxy map
        renderGalaxyMap();
    }

    function showSubsectorListInView() {
        if (!elements.sectorSubsectors || !elements.sectorSystems) return;
        elements.sectorSubsectors.classList.remove('hidden');
        elements.sectorSystems.classList.add('hidden');
    }

    function zoomGalaxy(factor) {
        mapScale = Math.max(0.3, Math.min(3, mapScale * factor));
        renderGalaxyMap();
    }

    function resetGalaxyView() {
        mapScale = 1;
        mapOffsetX = 0;
        mapOffsetY = 0;
        renderGalaxyMap();
    }

    // ============================================================
    // UTILITIES
    // ============================================================
    function hideModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.classList.add('hidden');
    }

    // Utility
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // --------------------------------------------------------
    // Toast Notifications
    // --------------------------------------------------------
    function showToast(message, type) {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };

        const toast = document.createElement('div');
        toast.className = `toast ${type || 'info'}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[type] || 'ℹ️'}</span>
            <div class="toast-content">
                <div class="toast-message">${escapeHtml(message)}</div>
            </div>
            <button class="toast-close">&times;</button>
            <div class="toast-progress"></div>
        `;

        toast.querySelector('.toast-close').addEventListener('click', () => {
            removeToast(toast);
        });

        container.appendChild(toast);

        setTimeout(() => {
            removeToast(toast);
        }, 4000);
    }

    function removeToast(toast) {
        if (!toast || !toast.parentNode) return;
        toast.style.animation = 'toastSlideOut 0.3s ease-in forwards';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }

    // --------------------------------------------------------
    // Command Bar (Ctrl+K)
    // --------------------------------------------------------
    function showCommandBar() {
        const commandBar = document.getElementById('commandBar');
        const input = document.getElementById('commandBarInput');
        if (commandBar) {
            commandBar.classList.remove('hidden');
            if (input) {
                input.value = '';
                input.focus();
                const items = commandBar.querySelectorAll('.command-bar-item');
                items.forEach(item => item.style.display = 'flex');
            }
        }
    }

    function hideCommandBar() {
        const commandBar = document.getElementById('commandBar');
        if (commandBar) {
            commandBar.classList.add('hidden');
        }
    }

    // ============================================================
    // PUBLIC API
    // ============================================================
    return {
        init,
        loadGameState,
        addEventLog,
        showLogin,
        showSessionSelection,
        showGame,
        showToast,
        showCommandBar,
        hideCommandBar
    };
})();

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', GameUI.init);
} else {
    GameUI.init();
}

// ============================================================
// Enhanced UI Features
// ============================================================

const GameUIEnhanced = (function() {
    let commandBarVisible = false;
    let commandBarSelectedIndex = 0;

    function init() {
        setupPanelCollapse();
        setupCommandBar();
        setupKeyboardShortcuts();
        setupEventLogFilter();
        setupMinimap();
    }

    // --------------------------------------------------------
    // Panel Collapse
    // --------------------------------------------------------
    function setupPanelCollapse() {
        document.addEventListener('click', (e) => {
            const header = e.target.closest('.panel-section h3');
            if (header) {
                const section = header.closest('.panel-section');
                if (section) {
                    section.classList.toggle('collapsed');
                }
            }
        });
    }

    // --------------------------------------------------------
    // Command Bar (Ctrl+K)
    // --------------------------------------------------------
    function setupCommandBar() {
        const commandBar = document.getElementById('commandBar');
        const input = document.getElementById('commandBarInput');
        const results = document.getElementById('commandBarResults');

        if (!commandBar || !input) return;

        input.addEventListener('input', () => {
            const query = input.value.toLowerCase().trim();
            const items = results.querySelectorAll('.command-bar-item');
            items.forEach(item => {
                const title = item.querySelector('.command-bar-item-title')?.textContent?.toLowerCase() || '';
                const desc = item.querySelector('.command-bar-item-desc')?.textContent?.toLowerCase() || '';
                const action = item.dataset.action || '';
                const match = !query || title.includes(query) || desc.includes(query) || action.includes(query);
                item.style.display = match ? 'flex' : 'none';
            });
            commandBarSelectedIndex = 0;
        });

        input.addEventListener('keydown', (e) => {
            const visibleItems = Array.from(results.querySelectorAll('.command-bar-item'))
                .filter(item => item.style.display !== 'none');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                commandBarSelectedIndex = Math.min(commandBarSelectedIndex + 1, visibleItems.length - 1);
                updateCommandBarSelection(visibleItems);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                commandBarSelectedIndex = Math.max(commandBarSelectedIndex - 1, 0);
                updateCommandBarSelection(visibleItems);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                const selected = visibleItems[commandBarSelectedIndex];
                if (selected) {
                    executeCommand(selected.dataset.action);
                    hideCommandBar();
                }
            } else if (e.key === 'Escape') {
                hideCommandBar();
            }
        });

        results.addEventListener('click', (e) => {
            const item = e.target.closest('.command-bar-item');
            if (item) {
                executeCommand(item.dataset.action);
                hideCommandBar();
            }
        });

        commandBar.addEventListener('click', (e) => {
            if (e.target === commandBar) {
                hideCommandBar();
            }
        });
    }

    function updateCommandBarSelection(items) {
        items.forEach((item, i) => {
            item.classList.toggle('selected', i === commandBarSelectedIndex);
        });
        if (items[commandBarSelectedIndex]) {
            items[commandBarSelectedIndex].scrollIntoView({ block: 'nearest' });
        }
    }

    function showCommandBar() {
        const commandBar = document.getElementById('commandBar');
        const input = document.getElementById('commandBarInput');
        if (commandBar) {
            commandBar.classList.remove('hidden');
            commandBarVisible = true;
            if (input) {
                input.value = '';
                input.focus();
                // Reset visibility
                const items = commandBar.querySelectorAll('.command-bar-item');
                items.forEach(item => item.style.display = 'flex');
            }
        }
    }

    function hideCommandBar() {
        const commandBar = document.getElementById('commandBar');
        if (commandBar) {
            commandBar.classList.add('hidden');
            commandBarVisible = false;
        }
    }

    function executeCommand(action) {
        switch (action) {
            case 'advance-turn':
                const advanceBtn = document.getElementById('advanceTurnBtn');
                if (advanceBtn && !advanceBtn.disabled) advanceBtn.click();
                break;
            case 'build-ship':
                const buildBtn = document.getElementById('buildShipBtn');
                if (buildBtn && !buildBtn.disabled) buildBtn.click();
                break;
            case 'move-fleet':
                const moveBtn = document.getElementById('moveFleetBtn');
                if (moveBtn && !moveBtn.disabled) moveBtn.click();
                break;
            case 'engage-combat':
                const combatBtn = document.getElementById('engageCombatBtn');
                if (combatBtn && !combatBtn.disabled) combatBtn.click();
                break;
            case 'zoom-in':
                const zoomInBtn = document.getElementById('zoomInBtn');
                if (zoomInBtn) zoomInBtn.click();
                break;
            case 'zoom-out':
                const zoomOutBtn = document.getElementById('zoomOutBtn');
                if (zoomOutBtn) zoomOutBtn.click();
                break;
            case 'reset-view':
                const resetBtn = document.getElementById('resetViewBtn');
                if (resetBtn) resetBtn.click();
                break;
        }
    }

    // --------------------------------------------------------
    // Keyboard Shortcuts
    // --------------------------------------------------------
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Don't trigger shortcuts when typing in inputs
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                // Allow Escape to close command bar from input
                if (e.key === 'Escape' && commandBarVisible) {
                    hideCommandBar();
                    return;
                }
                return;
            }

            // Ctrl+K or Cmd+K: Command bar
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                if (commandBarVisible) {
                    hideCommandBar();
                } else {
                    showCommandBar();
                }
                return;
            }

            // Escape: Close command bar or modals
            if (e.key === 'Escape') {
                if (commandBarVisible) {
                    hideCommandBar();
                    return;
                }
                // Close any open modals
                const modals = document.querySelectorAll('.modal-overlay:not(.hidden)');
                if (modals.length > 0) {
                    modals[modals.length - 1].classList.add('hidden');
                    return;
                }
                return;
            }

            // Enter: Advance turn (when not in modal)
            if (e.key === 'Enter' && !commandBarVisible) {
                const modals = document.querySelectorAll('.modal-overlay:not(.hidden)');
                if (modals.length === 0) {
                    const advanceBtn = document.getElementById('advanceTurnBtn');
                    if (advanceBtn && !advanceBtn.disabled) {
                        advanceBtn.click();
                    }
                }
                return;
            }

            // B: Build ship
            if (e.key === 'b' || e.key === 'B') {
                const buildBtn = document.getElementById('buildShipBtn');
                if (buildBtn && !buildBtn.disabled) buildBtn.click();
                return;
            }

            // M: Move fleet
            if (e.key === 'm' || e.key === 'M') {
                const moveBtn = document.getElementById('moveFleetBtn');
                if (moveBtn && !moveBtn.disabled) moveBtn.click();
                return;
            }

            // C: Engage combat
            if (e.key === 'c' || e.key === 'C') {
                const combatBtn = document.getElementById('engageCombatBtn');
                if (combatBtn && !combatBtn.disabled) combatBtn.click();
                return;
            }

            // + or =: Zoom in
            if (e.key === '+' || e.key === '=') {
                const zoomInBtn = document.getElementById('zoomInBtn');
                if (zoomInBtn) zoomInBtn.click();
                return;
            }

            // -: Zoom out
            if (e.key === '-') {
                const zoomOutBtn = document.getElementById('zoomOutBtn');
                if (zoomOutBtn) zoomOutBtn.click();
                return;
            }

            // R: Reset view
            if (e.key === 'r' || e.key === 'R') {
                const resetBtn = document.getElementById('resetViewBtn');
                if (resetBtn) resetBtn.click();
                return;
            }
        });
    }

    // --------------------------------------------------------
    // Event Log Filter
    // --------------------------------------------------------
    function setupEventLogFilter() {
        const filterBtns = document.querySelectorAll('.event-log-filter-btn');
        filterBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                filterBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                const filter = btn.dataset.filter;
                const entries = document.querySelectorAll('.event-entry');
                entries.forEach(entry => {
                    if (filter === 'all') {
                        entry.style.display = 'flex';
                    } else {
                        const typeEl = entry.querySelector('.event-type');
                        if (typeEl && typeEl.textContent === filter) {
                            entry.style.display = 'flex';
                        } else {
                            entry.style.display = 'none';
                        }
                    }
                });
            });
        });
    }

    // --------------------------------------------------------
    // Minimap
    // --------------------------------------------------------
    function setupMinimap() {
        const minimapCanvas = document.getElementById('minimapCanvas');
        if (!minimapCanvas) return;

        const minimap = document.getElementById('minimap');
        minimapCanvas.width = minimap.offsetWidth;
        minimapCanvas.height = minimap.offsetHeight;

        minimap.addEventListener('click', (e) => {
            const rect = minimap.getBoundingClientRect();
            const x = (e.clientX - rect.left) / rect.width;
            const y = (e.clientY - rect.top) / rect.height;
            // Pan the galaxy map to the clicked position
            // This would need to be connected to the galaxy map's pan function
        });
    }

    // --------------------------------------------------------
    // Resource Change Animation
    // --------------------------------------------------------
    function updateResourceChange(resourceId, oldValue, newValue) {
        const changeEl = document.getElementById(resourceId + 'Change');
        if (!changeEl) return;

        const diff = newValue - oldValue;
        if (diff === 0) {
            changeEl.textContent = '+0';
            changeEl.className = 'resource-change positive';
            return;
        }

        const sign = diff > 0 ? '+' : '';
        changeEl.textContent = sign + diff;
        changeEl.className = `resource-change ${diff > 0 ? 'positive' : 'negative'}`;

        // Animate the change indicator
        changeEl.style.transform = 'scale(1.3)';
        changeEl.style.opacity = '1';
        setTimeout(() => {
            changeEl.style.transform = 'scale(1)';
        }, 300);
    }

    return {
        init,
        showCommandBar,
        hideCommandBar,
        updateResourceChange
    };
})();

// Initialize enhanced UI after DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', GameUIEnhanced.init);
} else {
    GameUIEnhanced.init();
}
