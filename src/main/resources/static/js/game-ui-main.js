/**
 * DoraMCU Game UI - Main Module
 * Contains all game UI logic: auth, game state, galaxy map, turn phases, etc.
 * Depends on: game-ui-core.js, auth.js, game-api.js, game-sse.js
 */
(function() {
    'use strict';
    function init() {
        cacheElements();
        bindEvents();
        checkAuth();
        setupSSEListeners();
        setupPanelCollapse();
        setupCommandBar();
        setupKeyboardShortcuts();
        setupEventLogFilter();
        setupMinimap();
    }

    // ============================================================
    // GALAXY PROGRESS
    // ============================================================
    function checkGalaxyProgress() {
        // Close any existing connection first
        if (GameUIState.galaxyProgressEventSource) {
            GameUIState.galaxyProgressEventSource.close();
            GameUIState.galaxyProgressEventSource = null;
        }

        // Only connect if authenticated
        const token = localStorage.getItem('token');
        if (!token) {
            return;
        }

        // Connect to the galaxy progress SSE endpoint
        const baseUrl = window.location.origin;
        try {
            GameUIState.galaxyProgressEventSource = new EventSource(baseUrl + '/api/v1/galaxy/progress');

            GameUIState.galaxyProgressEventSource.addEventListener('start', (e) => {
                showGalaxyCreationModal();
                const data = JSON.parse(e.data);
                updateGalaxyProgress(0, data.totalSystems);
                addGalaxyLog('Galaxy generation started. Target: ' + data.totalSystems + ' solar systems.');
                updateGalaxyStatus('Ã°Å¸Å’Å’ Generating galaxy...');
            });

            GameUIState.galaxyProgressEventSource.addEventListener('progress', (e) => {
                const data = JSON.parse(e.data);
                updateGalaxyProgress(data.systemsGenerated, data.totalSystems);
            });

            GameUIState.galaxyProgressEventSource.addEventListener('log', (e) => {
                const data = JSON.parse(e.data);
                addGalaxyLog(data.message);
            });

            GameUIState.galaxyProgressEventSource.addEventListener('complete', (e) => {
                const data = JSON.parse(e.data);
                updateGalaxyStatus('Ã¢Å“â€¦ Galaxy generation complete!');
                addGalaxyLog('Galaxy generation complete!');
                // Show the Done button so the user can close the modal manually
                const closeBtn = document.getElementById('galaxyCloseBtn');
                if (closeBtn) closeBtn.classList.remove('hidden');
            });

            GameUIState.galaxyProgressEventSource.addEventListener('galaxy_ready', (e) => {
                // Galaxy already exists, no need to show modal
                // Silently close the connection since galaxy is already ready
                if (GameUIState.galaxyProgressEventSource) {
                    GameUIState.galaxyProgressEventSource.close();
                    GameUIState.galaxyProgressEventSource = null;
                }
            });

            let errorCount = 0;
            GameUIState.galaxyProgressEventSource.onerror = (e) => {
                errorCount++;
                // After 3 consecutive errors, close the connection to prevent infinite reconnection spam
                if (errorCount >= 3) {
                    if (GameUIState.galaxyProgressEventSource) {
                        GameUIState.galaxyProgressEventSource.close();
                        GameUIState.galaxyProgressEventSource = null;
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
        if (GameUIState.galaxyProgressEventSource) {
            GameUIState.galaxyProgressEventSource.close();
            GameUIState.galaxyProgressEventSource = null;
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
        GameUIState.elements = {
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
            sectorSearchInput: document.getElementById('sectorSearchInput'),
            sectorSearchResults: document.getElementById('sectorSearchResults'),
            subsectorViewStats: document.getElementById('subsectorViewStats'),
            showOnlyOwnedSubsectors: document.getElementById('showOnlyOwnedSubsectors'),
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
            // New card-based galaxy views
            sectorCardsView: document.getElementById('sectorCardsView'),
            subsectorCardsView: document.getElementById('subsectorCardsView'),
            subsectorHexGridView: document.getElementById('subsectorHexGridView'),
            systemDetailView: document.getElementById('systemDetailView'),
            sectorCardsGrid: document.getElementById('sectorCardsGrid'),
            subsectorCardsGrid: document.getElementById('subsectorCardsGrid'),
            subsectorViewTitle: document.getElementById('subsectorViewTitle'),
            subsectorHexGridTitle: document.getElementById('subsectorHexGridTitle'),
            subsectorHexGridStats: document.getElementById('subsectorHexGridStats'),
            subsectorHexGridCanvas: document.getElementById('subsectorHexGridCanvas'),
            hexGridContainer: document.getElementById('hexGridContainer'),
            hexGridLegend: document.getElementById('hexGridLegend'),
            systemDetailTitle: document.getElementById('systemDetailTitle'),
            systemDetailStats: document.getElementById('systemDetailStats'),
            systemMapCanvas: document.getElementById('systemMapCanvas'),
            systemMapLegend: document.getElementById('systemMapLegend'),
            systemInfoPanel: document.getElementById('systemInfoPanel'),
            planetList: document.getElementById('planetList'),
            fleetListSystem: document.getElementById('fleetListSystem'),
            galaxyViewStats: document.getElementById('galaxyViewStats'),
            backToSectorsBtn: document.getElementById('backToSectorsBtn'),
            backToSubsectorCardsBtn: document.getElementById('backToSubsectorCardsBtn'),
            backToSubsectorsBtn2: document.getElementById('backToSubsectorsBtn2'),
            systemSelector: document.getElementById('systemSelector'),
            // Legacy sector detail view elements (kept for backward compat)
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
        if (GameUIState.elements.loginForm) {
            GameUIState.elements.loginForm.addEventListener('submit', handleLogin);
        }
        if (GameUIState.elements.pinLoginForm) {
            GameUIState.elements.pinLoginForm.addEventListener('submit', handlePinLogin);
        }
        if (GameUIState.elements.tabCommanderLogin && GameUIState.elements.tabPinLogin) {
            GameUIState.elements.tabCommanderLogin.addEventListener('click', () => {
                GameUIState.elements.tabCommanderLogin.classList.add('primary');
                GameUIState.elements.tabPinLogin.classList.remove('primary');
                if (GameUIState.elements.loginForm) GameUIState.elements.loginForm.style.display = 'block';
                if (GameUIState.elements.pinLoginForm) GameUIState.elements.pinLoginForm.style.display = 'none';
                hideLoginError();
            });
            GameUIState.elements.tabPinLogin.addEventListener('click', () => {
                GameUIState.elements.tabPinLogin.classList.add('primary');
                GameUIState.elements.tabCommanderLogin.classList.remove('primary');
                if (GameUIState.elements.pinLoginForm) GameUIState.elements.pinLoginForm.style.display = 'block';
                if (GameUIState.elements.loginForm) GameUIState.elements.loginForm.style.display = 'none';
                hideLoginError();
            });
        }

        // Server selection
        if (GameUIState.elements.gameServerSelect) {
            const stored = localStorage.getItem('doramcu_server_url') || '';
            if (stored === '' || stored === 'http://localhost:8080') {
                GameUIState.elements.gameServerSelect.value = stored;
            } else if (stored) {
                GameUIState.elements.gameServerSelect.value = 'custom';
                if (GameUIState.elements.gameCustomServerContainer) GameUIState.elements.gameCustomServerContainer.style.display = 'block';
                if (GameUIState.elements.gameCustomServerUrl) GameUIState.elements.gameCustomServerUrl.value = stored;
            }

            GameUIState.elements.gameServerSelect.addEventListener('change', () => {
                if (GameUIState.elements.gameServerSelect.value === 'custom') {
                    if (GameUIState.elements.gameCustomServerContainer) GameUIState.elements.gameCustomServerContainer.style.display = 'block';
                } else {
                    if (GameUIState.elements.gameCustomServerContainer) GameUIState.elements.gameCustomServerContainer.style.display = 'none';
                    GameAPI.setServerUrl(GameUIState.elements.gameServerSelect.value);
                    updateSessionServerIndicator();
                }
            });
        }
        if (GameUIState.elements.gameCustomServerUrl) {
            GameUIState.elements.gameCustomServerUrl.addEventListener('change', () => {
                GameAPI.setServerUrl(GameUIState.elements.gameCustomServerUrl.value);
                updateSessionServerIndicator();
            });
        }

        // Join with PIN
        if (GameUIState.elements.joinByPinBtn) {
            GameUIState.elements.joinByPinBtn.addEventListener('click', handleJoinByPin);
        }

        if (GameUIState.elements.logoutBtn) {
            GameUIState.elements.logoutBtn.addEventListener('click', handleLogout);
        }

        // Session management
        if (GameUIState.elements.createSessionBtn) {
            GameUIState.elements.createSessionBtn.addEventListener('click', handleCreateSession);
        }
        if (GameUIState.elements.backToLoginBtn) {
            GameUIState.elements.backToLoginBtn.addEventListener('click', handleBackToLogin);
        }
        if (GameUIState.elements.registerPlayerBtn) {
            GameUIState.elements.registerPlayerBtn.addEventListener('click', handleRegisterPlayer);
        }
        if (GameUIState.elements.joinByPinBtn) {
            GameUIState.elements.joinByPinBtn.addEventListener('click', handleJoinByPin);
        }

        // Game actions
        if (GameUIState.elements.advanceTurnBtn) {
            GameUIState.elements.advanceTurnBtn.addEventListener('click', handleAdvanceTurn);
        }
        if (GameUIState.elements.buildShipBtn) {
            GameUIState.elements.buildShipBtn.addEventListener('click', handleBuildShipClick);
        }
        if (GameUIState.elements.moveFleetBtn) {
            GameUIState.elements.moveFleetBtn.addEventListener('click', handleMoveFleetClick);
        }
        if (GameUIState.elements.leaveSessionBtn) {
            GameUIState.elements.leaveSessionBtn.addEventListener('click', handleLeaveSession);
        }

        // New empire actions
        if (GameUIState.elements.buildStationBtn) {
            GameUIState.elements.buildStationBtn.addEventListener('click', handleBuildStationClick);
        }
        if (GameUIState.elements.buildFacilityBtn) {
            GameUIState.elements.buildFacilityBtn.addEventListener('click', handleBuildFacilityClick);
        }
        if (GameUIState.elements.createFleetBtn) {
            GameUIState.elements.createFleetBtn.addEventListener('click', handleCreateFleetClick);
        }
        if (GameUIState.elements.viewBattlesBtn) {
            GameUIState.elements.viewBattlesBtn.addEventListener('click', handleViewBattlesClick);
        }

        // Build Station modal
        if (GameUIState.elements.confirmBuildStation) {
            GameUIState.elements.confirmBuildStation.addEventListener('click', handleBuildStationConfirm);
        }
        if (GameUIState.elements.cancelBuildStation) {
            GameUIState.elements.cancelBuildStation.addEventListener('click', () => GameUI.hideModal('buildStationModal'));
        }
        if (GameUIState.elements.closeBuildStationModal) {
            GameUIState.elements.closeBuildStationModal.addEventListener('click', () => GameUI.hideModal('buildStationModal'));
        }

        // Build Facility modal
        if (GameUIState.elements.confirmBuildFacility) {
            GameUIState.elements.confirmBuildFacility.addEventListener('click', handleBuildFacilityConfirm);
        }
        if (GameUIState.elements.cancelBuildFacility) {
            GameUIState.elements.cancelBuildFacility.addEventListener('click', () => GameUI.hideModal('buildFacilityModal'));
        }
        if (GameUIState.elements.closeBuildFacilityModal) {
            GameUIState.elements.closeBuildFacilityModal.addEventListener('click', () => GameUI.hideModal('buildFacilityModal'));
        }

        // Create Fleet modal
        if (GameUIState.elements.confirmCreateFleet) {
            GameUIState.elements.confirmCreateFleet.addEventListener('click', handleCreateFleetConfirm);
        }
        if (GameUIState.elements.cancelCreateFleet) {
            GameUIState.elements.cancelCreateFleet.addEventListener('click', () => GameUI.hideModal('createFleetModal'));
        }
        if (GameUIState.elements.closeCreateFleetModal) {
            GameUIState.elements.closeCreateFleetModal.addEventListener('click', () => GameUI.hideModal('createFleetModal'));
        }

        // Battle Status modal
        if (GameUIState.elements.closeBattleStatus) {
            GameUIState.elements.closeBattleStatus.addEventListener('click', () => GameUI.hideModal('battleStatusModal'));
        }
        if (GameUIState.elements.closeBattleStatusModal) {
            GameUIState.elements.closeBattleStatusModal.addEventListener('click', () => GameUI.hideModal('battleStatusModal'));
        }

        // Build ship modal
        if (GameUIState.elements.buildShipConfirm) {
            GameUIState.elements.buildShipConfirm.addEventListener('click', handleBuildShipConfirm);
        }
        if (GameUIState.elements.buildShipCancel) {
            GameUIState.elements.buildShipCancel.addEventListener('click', () => GameUI.hideModal('buildShipModal'));
        }

        // Turn phase modals - Resource Collection
        if (GameUIState.elements.resourcePhaseSkip) {
            GameUIState.elements.resourcePhaseSkip.addEventListener('click', () => nextPhase());
        }
        if (GameUIState.elements.resourcePhaseNext) {
            GameUIState.elements.resourcePhaseNext.addEventListener('click', () => nextPhase());
        }
        // Pipeline details toggle
        if (GameUIState.elements.viewPipelineDetailsBtn) {
            GameUIState.elements.viewPipelineDetailsBtn.addEventListener('click', loadPipelineDetails);
        }

        // Turn phase modals - Ship Building
        if (GameUIState.elements.shipBuildPhaseSkip) {
            GameUIState.elements.shipBuildPhaseSkip.addEventListener('click', () => nextPhase());
        }
        if (GameUIState.elements.shipBuildPhaseNext) {
            GameUIState.elements.shipBuildPhaseNext.addEventListener('click', () => nextPhase());
        }

        // Turn phase modals - Fleet Movement
        if (GameUIState.elements.fleetMovePhaseSkip) {
            GameUIState.elements.fleetMovePhaseSkip.addEventListener('click', () => nextPhase());
        }
        if (GameUIState.elements.fleetMovePhaseNext) {
            GameUIState.elements.fleetMovePhaseNext.addEventListener('click', () => nextPhase());
        }

        // Turn phase modals - Combat
        if (GameUIState.elements.combatPhaseSkip) {
            GameUIState.elements.combatPhaseSkip.addEventListener('click', () => endTurnPhases());
        }
        if (GameUIState.elements.combatPhaseNext) {
            GameUIState.elements.combatPhaseNext.addEventListener('click', () => endTurnPhases());
        }

        // Phase option item clicks
        document.addEventListener('click', (e) => {
            const optionItem = e.target.closest('.phase-option-item');
            if (optionItem) {
                const action = optionItem.dataset.action;
                handlePhaseAction(action);
            }
        });


        // Sector search input
        if (GameUIState.elements.sectorSearchInput) {
            GameUIState.elements.sectorSearchInput.addEventListener('input', () => {
                filterSectorCards(GameUIState.elements.sectorSearchInput.value);
            });
        }

        // Zoom controls
        if (GameUIState.elements.zoomInBtn) {
            GameUIState.elements.zoomInBtn.addEventListener('click', () => zoomGalaxy(1.2));
        }
        if (GameUIState.elements.zoomOutBtn) {
            GameUIState.elements.zoomOutBtn.addEventListener('click', () => zoomGalaxy(0.8));
        }
        if (GameUIState.elements.resetViewBtn) {
            GameUIState.elements.resetViewBtn.addEventListener('click', () => resetGalaxyView());
        }

        // Auth expired
        window.addEventListener('auth-expired', () => {
            showLogin();
        });
    }

    // ============================================================
    // AUTH & SESSIONS
    // ============================================================
    async function checkAuth() {
        const result = await GameAPI.ensureSession();

        if (result.GameUIState.sessionId) {
            // Valid stored session Ã¢â‚¬â€ enter the game directly
            const username = localStorage.getItem('username');
            if (username) {
                GameUIState.elements.playerName.textContent = username;
            }
            checkGalaxyProgress();
            GameUIState.sessionId = result.GameUIState.sessionId;
            enterSession();
            return;
        }

        // Check if needs session selection (authenticated but no session)
        if (result.needsSessionSelection) {
            const username = localStorage.getItem('username');
            if (username) {
                GameUIState.elements.playerName.textContent = username;
            }
            checkGalaxyProgress();
            showSessionSelection();
            return;
        }

        // Not authenticated Ã¢â‚¬â€ show login screen
        showLogin();
    }

    function showLogin() {
        if (GameUIState.elements.loginOverlay) GameUIState.elements.loginOverlay.classList.remove('hidden');
        if (GameUIState.elements.sessionOverlay) GameUIState.elements.sessionOverlay.classList.add('hidden');
        if (GameUIState.elements.gameContainer) GameUIState.elements.gameContainer.classList.add('hidden');
    }

    function showSessionSelection() {
        if (GameUIState.elements.loginOverlay) GameUIState.elements.loginOverlay.classList.add('hidden');
        if (GameUIState.elements.sessionOverlay) GameUIState.elements.sessionOverlay.classList.remove('hidden');
        if (GameUIState.elements.gameContainer) GameUIState.elements.gameContainer.classList.add('hidden');
        updateSessionServerIndicator();
        loadSessions();
    }

    function updateSessionServerIndicator() {
        if (!GameUIState.elements.sessionServerIndicator) return;
        const base = GameAPI.getServerUrl();
        GameUIState.elements.sessionServerIndicator.textContent = 'Server: ' + (base || 'Default Host');
    }

    function showGame() {
        if (GameUIState.elements.loginOverlay) GameUIState.elements.loginOverlay.classList.add('hidden');
        if (GameUIState.elements.sessionOverlay) GameUIState.elements.sessionOverlay.classList.add('hidden');
        if (GameUIState.elements.gameContainer) GameUIState.elements.gameContainer.classList.remove('hidden');
    }

    // Login handler
    async function handleLogin(e) {
        e.preventDefault();
        const username = GameUIState.elements.loginUsername.value.trim();
        const password = GameUIState.elements.loginPassword.value;
        if (!username || !password) {
            showLoginError('Please enter username and password');
            return;
        }
        GameUIState.elements.loginBtn.disabled = true;
        GameUIState.elements.loginBtn.textContent = 'Signing in...';
        hideLoginError();

        try {
            const data = await GameAPI.login(username, password);
            GameUIState.elements.playerName.textContent = username;
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            showLoginError('Invalid username or password. Check your credentials and try again.');
        } finally {
            GameUIState.elements.loginBtn.disabled = false;
            GameUIState.elements.loginBtn.textContent = 'Sign In';
        }
    }

    // Player PIN Login handler
    async function handlePinLogin(e) {
        e.preventDefault();
        const pin = GameUIState.elements.loginPinInput ? GameUIState.elements.loginPinInput.value.trim() : '';
        if (!pin) {
            showLoginError('Please enter your 4-6 digit Player PIN code');
            return;
        }
        if (GameUIState.elements.loginPinBtn) {
            GameUIState.elements.loginPinBtn.disabled = true;
            GameUIState.elements.loginPinBtn.textContent = 'Validating PIN...';
        }
        hideLoginError();

        try {
            const data = await GameAPI.pinLogin(pin);
            GameUIState.elements.playerName.textContent = data.username || data.empireName || 'Commander';
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            showLoginError(err.message || 'Invalid or unregistered Player PIN code');
        } finally {
            if (GameUIState.elements.loginPinBtn) {
                GameUIState.elements.loginPinBtn.disabled = false;
                GameUIState.elements.loginPinBtn.textContent = 'Sign In with PIN';
            }
        }
    }

    // Join Session with Player PIN
    async function handleJoinByPin() {
        const pin = GameUIState.elements.joinByPinInput ? GameUIState.elements.joinByPinInput.value.trim() : '';
        if (!pin) {
            if (GameUIState.elements.joinByPinError) {
                GameUIState.elements.joinByPinError.textContent = 'Please enter a 4-6 digit Player PIN code';
                GameUIState.elements.joinByPinError.style.display = 'block';
            }
            return;
        }
        try {
            if (GameUIState.elements.joinByPinError) GameUIState.elements.joinByPinError.style.display = 'none';
            const data = await GameAPI.pinLogin(pin);
            GameUIState.elements.playerName.textContent = data.username || data.empireName || 'Commander';
            GameUI.showToast('Player PIN connected: ' + (data.empireName || data.username), 'success');
            loadSessions();
        } catch (err) {
            if (GameUIState.elements.joinByPinError) {
                GameUIState.elements.joinByPinError.textContent = err.message || 'Failed to authenticate PIN';
                GameUIState.elements.joinByPinError.style.display = 'block';
            }
        }
    }

    function handleLogout() {
        GameSSE.disconnect();
        GameAPI.clearAuth();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        showLogin();
    }

    function handleBackToLogin() {
        GameSSE.disconnect();
        GameAPI.clearAuth();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        showLogin();
    }

    // Register new player handler
    async function handleRegisterPlayer() {
        const username = GameUIState.elements.registerUsername ? GameUIState.elements.registerUsername.value.trim() : '';
        const password = GameUIState.elements.registerPassword ? GameUIState.elements.registerPassword.value : '';
        const errorEl = GameUIState.elements.registerError;

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
            GameUIState.elements.playerName.textContent = data.username || username;
            if (data.pin) {
                GameUI.showToast('Registered! Your Player PIN is: ' + data.pin, 'success');
            }
            checkGalaxyProgress();
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
        if (GameUIState.elements.loginError && GameUIState.elements.loginForm.style.display !== 'none') {
            GameUIState.elements.loginError.textContent = message;
            GameUIState.elements.loginError.style.display = 'block';
        }
        if (GameUIState.elements.loginPinError && GameUIState.elements.pinLoginForm && GameUIState.elements.pinLoginForm.style.display !== 'none') {
            GameUIState.elements.loginPinError.textContent = message;
            GameUIState.elements.loginPinError.style.display = 'block';
        }
    }

    function hideLoginError() {
        if (GameUIState.elements.loginError) {
            GameUIState.elements.loginError.style.display = 'none';
        }
        if (GameUIState.elements.loginPinError) {
            GameUIState.elements.loginPinError.style.display = 'none';
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
        if (!GameUIState.elements.sessionList) return;
        GameUIState.elements.sessionList.innerHTML = '';
        if (!sessions.length) {
            GameUIState.elements.sessionList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No active sessions. Create one to start playing.</div>';
            return;
        }
        sessions.forEach(session => {
            const item = document.createElement('div');
            item.className = 'session-item';
            item.innerHTML = `
                <div>
                    <div class="session-name">${GameUI.escapeHtml(session.name || session.GameUIState.sessionId)}</div>
                    <div class="session-players">${session.playerIds ? session.playerIds.length : 0} player(s)</div>
                </div>
                <div class="session-actions">
                    <button class="btn-small primary" data-session-id="${session.GameUIState.sessionId}">Join</button>
                </div>
            `;
            GameUIState.elements.sessionList.appendChild(item);
        });

        // Bind join buttons
        GameUIState.elements.sessionList.querySelectorAll('[data-session-id]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-session-id');
                await joinSession(id);
            });
        });
    }

    async function handleCreateSession() {
        const name = GameUIState.elements.sessionNameInput.value.trim();
        if (!name) {
            alert('Please enter a session name');
            return;
        }
        GameUIState.elements.createSessionBtn.disabled = true;
        try {
            const result = await GameAPI.createSession(name);
            GameUIState.sessionId = result.GameUIState.sessionId;
            enterSession();
        } catch (err) {
            alert('Failed to create session: ' + err.message);
        } finally {
            GameUIState.elements.createSessionBtn.disabled = false;
        }
    }

    async function joinSession(id) {
        try {
            await GameAPI.joinSession(id);
            GameUIState.sessionId = id;
            enterSession();
        } catch (err) {
            if (err.alreadyInSession) {
                // Player is already in this session Ã¢â‚¬â€ proceed directly to the game
                GameUIState.sessionId = id;
                enterSession();
            } else {
                alert('Failed to join session: ' + err.message);
            }
        }
    }

    function enterSession() {
        showGame();
        GameSSE.connect(GameUIState.sessionId);
        loadGameState();
    }

    async function handleLeaveSession() {
        if (!GameUIState.sessionId) return;
        stopSubsectorPolling();
        try {
            await GameAPI.leaveSession(GameUIState.sessionId);
        } catch (err) {
            console.error('Leave session error:', err);
        }
        GameSSE.disconnect();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        showSessionSelection();
    }

    // ============================================================
    // GAME STATE
    // ============================================================


    // Connection status indicator
    let connectionStatusEl = null;
    let connectionStatusTextEl = null;
    let connectionProgressBarEl = null;
    let connectionErrorEl = null;
    let retryConnectionBtnEl = null;

    function showConnectionStatus(message, progress) {
        if (!connectionStatusEl) {
            connectionStatusEl = document.getElementById('connectionStatus');
            connectionStatusTextEl = document.getElementById('connectionStatusText');
            connectionProgressBarEl = document.getElementById('connectionProgressBar');
            connectionErrorEl = document.getElementById('connectionError');
            retryConnectionBtnEl = document.getElementById('retryConnectionBtn');
        }
        if (connectionStatusEl) connectionStatusEl.style.display = 'flex';
        if (connectionStatusTextEl) connectionStatusTextEl.textContent = message || 'Connecting...';
        if (connectionProgressBarEl && progress !== undefined) connectionProgressBarEl.style.width = progress + '%';
        if (connectionErrorEl) connectionErrorEl.style.display = 'none';
        if (retryConnectionBtnEl) retryConnectionBtnEl.style.display = 'none';
    }

    function hideConnectionStatus() {
        if (connectionStatusEl) connectionStatusEl.style.display = 'none';
    }

    function showConnectionError(message, retryCallback) {
        showConnectionStatus('Connection Failed', 100);
        if (connectionErrorEl) {
            connectionErrorEl.textContent = message;
            connectionErrorEl.style.display = 'block';
        }
        if (retryConnectionBtnEl) {
            retryConnectionBtnEl.style.display = 'inline-block';
            retryConnectionBtnEl.onclick = function() {
                hideConnectionStatus();
                if (retryCallback) retryCallback();
            };
        }
    }

    async function loadGameState() {
        if (!GameUIState.sessionId) return;
        showConnectionStatus('Loading game data...', 20);
        try {
            const state = await GameAPI.getGameState(GameUIState.sessionId, true);
            showConnectionStatus('Rendering galaxy...', 70);
            if (state) {
                GameUIState.gameState = state;
                renderGameState();
                hideConnectionStatus();
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
            hideConnectionStatus();
            if (err.timeout) {
                showConnectionError('Request timed out. The server may be slow. Please try again.', function() { loadGameState(); });
            } else if (err.message && err.message.includes('Failed to fetch')) {
                showConnectionError('Cannot reach the server. Make sure the game server is running on port 8080.', function() { loadGameState(); });
            } else if (err.message && err.message.includes('Session not found')) {
                // Session was deleted (e.g., server restart with in-memory DB) Ã¢â‚¬â€ refresh via ensureSession
                console.warn('[DEV] Session not found, refreshing session...');
                localStorage.removeItem('doramcu_session_id');
                GameUIState.sessionId = null;
                showConnectionError('Session expired. Refreshing connection...', function() { checkAuth(); });
            } else {
                showConnectionError('Failed to load game data: ' + err.message, function() { loadGameState(); });
            }
            addEventLog('ERROR', 'Failed to load game state: ' + err.message);
        }
    }

    function startSubsectorPolling() {
        stopSubsectorPolling();
        let attempts = 0;
        const maxAttempts = 30; // Poll for up to 30 seconds
        GameUIState.gameStatePollTimer = setInterval(async () => {
            attempts++;
            if (attempts >= maxAttempts) {
                stopSubsectorPolling();
                return;
            }
            try {
                const state = await GameAPI.getGameState(GameUIState.sessionId);
                if (state && state.subSectors && state.subSectors.length > 0) {
                    console.log('SubSectors now available:', state.subSectors.length, 'subsectors');
                    GameUIState.gameState = state;
                    renderGameState();
                    stopSubsectorPolling();
                }
            } catch (err) {
                console.error('Polling error:', err);
            }
        }, 1000);
    }

    function stopSubsectorPolling() {
        if (GameUIState.gameStatePollTimer) {
            clearInterval(GameUIState.gameStatePollTimer);
            GameUIState.gameStatePollTimer = null;
        }
    }

    function renderGameState() {
        if (!GameUIState.gameState) return;

        // Turn number
        if (GameUIState.elements.turnNumber) GameUIState.elements.turnNumber.textContent = GameUIState.gameState.currentTurn;
        if (GameUIState.elements.currentTurn) {
            // Keep the icon, update the number span
            const turnNumEl = GameUIState.elements.currentTurn.querySelector('#turnNumber');
            if (turnNumEl) turnNumEl.textContent = GameUIState.gameState.currentTurn;
        }

        // Simultaneous turns: all players can act at any time
        if (GameUIState.elements.currentPlayerLabel) GameUIState.elements.currentPlayerLabel.textContent = 'All Players';
        if (GameUIState.elements.currentPlayerName) {
            const labelEl = GameUIState.elements.currentPlayerName.querySelector('#currentPlayerLabel');
            if (labelEl) labelEl.textContent = 'All Players';
        }

        // Turn indicator - always active for simultaneous turns
        if (GameUIState.elements.turnIndicator) {
            if (GameUIState.elements.turnIndicatorText) GameUIState.elements.turnIndicatorText.textContent = 'ALL PLAYERS ACTIVE';
            GameUIState.elements.turnIndicator.className = 'turn-indicator my-turn';
        }

        // Turn timer and progress bar
        if (GameUIState.gameState.schedulerEnabled && GameUIState.gameState.turnIntervalSeconds > 0) {
            GameUIState.turnTimerTotal = GameUIState.gameState.turnIntervalSeconds;
            GameUIState.turnTimerRemaining = Math.max(0, GameUIState.turnTimerTotal - GameUIState.gameState.accumulatedSeconds);
            updateTurnTimerDisplay();
            updateTurnProgressBar();
            updateTurnIntervalDisplay();
            updateSchedulerStatus();
            startTurnTimerCountdown();
        } else {
            stopTurnTimerCountdown();
            if (GameUIState.elements.turnTimerValue) GameUIState.elements.turnTimerValue.textContent = '--';
            if (GameUIState.elements.turnProgressFill) GameUIState.elements.turnProgressFill.style.width = '0%';
        }

        // Resources Ã¢â‚¬â€ use pipeline data if available, otherwise fall back to player resources
        const myPlayer = GameUIState.gameState.players?.find(p => p.id === getCurrentPlayerId());
        const pipeline = GameUIState.gameState.pipelineSummary;
        if (pipeline) {
            // Pipeline modules displayed as resources
            const modules = pipeline.modules || {};
            const raw = pipeline.rawResources || {};
            const refined = pipeline.refinedResources || {};
            if (GameUIState.elements.resourceMinerals) GameUIState.elements.resourceMinerals.textContent = modules.HULL_MODULE || 0;
            if (GameUIState.elements.resourceEnergy) GameUIState.elements.resourceEnergy.textContent = modules.WEAPON_MODULE || 0;
            if (GameUIState.elements.resourceInfluence) GameUIState.elements.resourceInfluence.textContent = modules.SHIELD_MODULE || 0;
            if (GameUIState.elements.resourceScience) GameUIState.elements.resourceScience.textContent = modules.ENGINE_MODULE || 0;
            if (GameUIState.elements.resourceFood) GameUIState.elements.resourceFood.textContent = modules.FACILITY_MODULE || 0;
            // Update resource change indicators with mining rates
            const mining = pipeline.miningRate || {};
            if (GameUIState.elements.resourceMineralsChange) {
                GameUIState.elements.resourceMineralsChange.textContent = '+' + (mining.ORE || 0);
                GameUIState.elements.resourceMineralsChange.className = 'resource-change positive';
            }
            if (GameUIState.elements.resourceEnergyChange) {
                GameUIState.elements.resourceEnergyChange.textContent = '+' + (mining.GAS || 0);
                GameUIState.elements.resourceEnergyChange.className = 'resource-change positive';
            }
            if (GameUIState.elements.resourceInfluenceChange) {
                GameUIState.elements.resourceInfluenceChange.textContent = pipeline.activeProcessing + '/' + pipeline.miningOperations;
                GameUIState.elements.resourceInfluenceChange.className = 'resource-change';
            }
            if (GameUIState.elements.resourceScienceChange) {
                GameUIState.elements.resourceScienceChange.textContent = pipeline.constructionQueueSize + ' queue';
                GameUIState.elements.resourceScienceChange.className = 'resource-change';
            }
            if (GameUIState.elements.resourceFoodChange) {
                GameUIState.elements.resourceFoodChange.textContent = pipeline.activeAssemblers + ' assemblers';
                GameUIState.elements.resourceFoodChange.className = 'resource-change';
            }
        } else if (myPlayer && myPlayer.resources) {
            // Fallback: show pipeline resources from player summary
            if (GameUIState.elements.resourceMinerals) GameUIState.elements.resourceMinerals.textContent = myPlayer.resources.HULL_MODULE || myPlayer.resources.ORE || 0;
            if (GameUIState.elements.resourceEnergy) GameUIState.elements.resourceEnergy.textContent = myPlayer.resources.WEAPON_MODULE || myPlayer.resources.GAS || 0;
            if (GameUIState.elements.resourceInfluence) GameUIState.elements.resourceInfluence.textContent = myPlayer.resources.SHIELD_MODULE || 0;
            if (GameUIState.elements.resourceScience) GameUIState.elements.resourceScience.textContent = myPlayer.resources.ENGINE_MODULE || 0;
            if (GameUIState.elements.resourceFood) GameUIState.elements.resourceFood.textContent = myPlayer.resources.FACILITY_MODULE || 0;
        }

        // Empire Info Panel
        const empire = GameUIState.gameState.empireSummary;
        if (empire) {
            if (GameUIState.elements.empireName) GameUIState.elements.empireName.textContent = empire.empireName || 'Ã¢â‚¬â€';
            if (GameUIState.elements.empireGovernment) GameUIState.elements.empireGovernment.textContent = empire.governmentType || 'Ã¢â‚¬â€';
            if (GameUIState.elements.empireEthos) GameUIState.elements.empireEthos.textContent = empire.ethos || 'Ã¢â‚¬â€';
            if (GameUIState.elements.empirePlanets) GameUIState.elements.empirePlanets.textContent = empire.planetCount || 0;
            if (GameUIState.elements.empireFleets) GameUIState.elements.empireFleets.textContent = empire.fleetCount || 0;
            if (GameUIState.elements.empireCapital) GameUIState.elements.empireCapital.textContent = empire.capitalPlanetId != null ? 'Planet #' + empire.capitalPlanetId : 'Ã¢â‚¬â€';

            // Location information
            if (GameUIState.elements.empireSector) {
                if (empire.capitalSectorRow != null && empire.capitalSectorCol != null) {
                    GameUIState.elements.empireSector.textContent = '(' + empire.capitalSectorRow + ', ' + empire.capitalSectorCol + ')';
                } else {
                    GameUIState.elements.empireSector.textContent = 'Ã¢â‚¬â€';
                }
            }
            if (GameUIState.elements.empireSubSector) {
                if (empire.capitalSubSectorRow != null && empire.capitalSubSectorCol != null) {
                    GameUIState.elements.empireSubSector.textContent = '(' + empire.capitalSubSectorRow + ', ' + empire.capitalSubSectorCol + ')';
                } else {
                    GameUIState.elements.empireSubSector.textContent = 'Ã¢â‚¬â€';
                }
            }
            if (GameUIState.elements.empireSystem) {
                if (empire.capitalSystemId != null) {
                    GameUIState.elements.empireSystem.textContent = 'System #' + empire.capitalSystemId;
                } else {
                    GameUIState.elements.empireSystem.textContent = 'Ã¢â‚¬â€';
                }
            }

            // Army component
            if (GameUIState.elements.armyUnitCount) GameUIState.elements.armyUnitCount.textContent = empire.armyUnitCount || 0;
            if (GameUIState.elements.armyOffense) GameUIState.elements.armyOffense.textContent = empire.armyTotalOffense || 0;
            if (GameUIState.elements.armyDefense) GameUIState.elements.armyDefense.textContent = empire.armyTotalDefense || 0;
            if (GameUIState.elements.armySupplyCost) GameUIState.elements.armySupplyCost.textContent = empire.armySupplyCost || 0;

            // Navy component
            if (GameUIState.elements.navyFleetCount) GameUIState.elements.navyFleetCount.textContent = empire.navyFleetCount || 0;
            if (GameUIState.elements.navyShipCount) GameUIState.elements.navyShipCount.textContent = empire.navyShipCount || 0;
            if (GameUIState.elements.navyDamage) GameUIState.elements.navyDamage.textContent = empire.navyTotalDamage || 0;
            if (GameUIState.elements.navyShields) GameUIState.elements.navyShields.textContent = empire.navyTotalShieldPoints || 0;
            if (GameUIState.elements.navyHull) GameUIState.elements.navyHull.textContent = empire.navyTotalHullPoints || 0;

            // Facilities component
            if (GameUIState.elements.facilityCount) GameUIState.elements.facilityCount.textContent = empire.facilityCount || 0;
            if (GameUIState.elements.spaceStationCount) GameUIState.elements.spaceStationCount.textContent = empire.spaceStationCount || 0;
            if (GameUIState.elements.mineralProduction) GameUIState.elements.mineralProduction.textContent = empire.mineralProduction || 0;
            if (GameUIState.elements.energyProduction) GameUIState.elements.energyProduction.textContent = empire.energyProduction || 0;
            if (GameUIState.elements.foodProduction) GameUIState.elements.foodProduction.textContent = empire.foodProduction || 0;
            if (GameUIState.elements.researchOutput) GameUIState.elements.researchOutput.textContent = empire.researchOutput || 0;
            if (GameUIState.elements.facilityDefenseBonus) GameUIState.elements.facilityDefenseBonus.textContent = empire.facilityDefenseBonus || 0;
            if (GameUIState.elements.creditIncome) GameUIState.elements.creditIncome.textContent = empire.creditIncome || 0;

            // Empire Status Bar
            if (GameUIState.elements.empireIntegrity) {
                const integrity = Math.round(empire.empireIntegrity || 100);
                GameUIState.elements.empireIntegrity.textContent = integrity + '%';
                if (GameUIState.elements.empireIntegrityFill) {
                    GameUIState.elements.empireIntegrityFill.style.width = integrity + '%';
                    GameUIState.elements.empireIntegrityFill.className = 'empire-integrity-fill' +
                        (integrity < 30 ? ' danger' : integrity < 60 ? ' warning' : '');
                }
            }
            if (GameUIState.elements.empireMilitaryPower) GameUIState.elements.empireMilitaryPower.textContent = empire.militaryPower || 0;
            if (GameUIState.elements.empireShipQueue) GameUIState.elements.empireShipQueue.textContent = empire.shipQueueSize || 0;
            if (GameUIState.elements.empireActiveBattles) GameUIState.elements.empireActiveBattles.textContent = empire.activeBattlesCount || 0;

            // Battle count badge
            if (GameUIState.elements.battleCountBadge) {
                const battleCount = empire.activeBattlesCount || 0;
                if (battleCount > 0) {
                    GameUIState.elements.battleCountBadge.style.display = 'inline-flex';
                    GameUIState.elements.battleCountBadge.textContent = battleCount;
                } else {
                    GameUIState.elements.battleCountBadge.style.display = 'none';
                }
            }

            // Traveller RPG: Commander display
            if (typeof CharacterUI !== 'undefined' && CharacterUI.displayCommander) {
                CharacterUI.displayCommander(GameUIState.gameState.commander);
            }

            // Traveller RPG: Mercenary display
            if (typeof TradeUI !== 'undefined' && TradeUI.displayMercenary) {
                TradeUI.displayMercenary(empire.mercenarySummary);
            }
        }

        // Player list
        if (GameUIState.elements.playerList) {
            GameUIState.elements.playerList.innerHTML = '';
            // Simultaneous turns: no current player highlighting
            (GameUIState.gameState.players || []).forEach(player => {
                const item = document.createElement('div');
                const isAi = player.isAi === true;
                let className = 'item-list-item';
                if (isAi) className += ' ai-player';
                item.className = className;

                let namePrefix = '';
                if (isAi) {
                    const diff = player.difficulty || 'MEDIUM';
                    namePrefix = `<span class="ai-badge" title="AI - ${diff}">Ã°Å¸Â¤â€“ ${diff}</span> `;
                }
                item.innerHTML = `
                    <div class="player-item-header">
                        ${namePrefix}<span class="item-name">${GameUI.escapeHtml(player.name)}</span>
                    </div>
                    <span class="item-detail">${player.planetCount} planets, ${player.fleetCount} fleets</span>
                `;
                GameUIState.elements.playerList.appendChild(item);
            });
        }

        // Fleet list Ã¢â‚¬â€ use fleet data from game state
        if (GameUIState.elements.fleetList) {
            GameUIState.elements.fleetList.innerHTML = '';
            const fleets = GameUIState.gameState.fleets || [];
            if (fleets.length > 0) {
                fleets.forEach(fleet => {
                    const item = document.createElement('div');
                    item.className = 'item-list-item' + (GameUIState.selectedFleet === fleet.id ? ' selected' : '');
                    const healthPercent = Math.round(fleet.healthRatio * 100);
                    let healthClass = 'health-good';
                    if (healthPercent < 20) healthClass = 'health-critical';
                    else if (healthPercent < 50) healthClass = 'health-warning';
                    else if (healthPercent < 80) healthClass = 'health-damaged';
                    const jDriveStatus = fleet.effectiveJDriveSpeed > 0
                        ? '<span class="drive-status drive-ok" title="JDrive operational">Ã°Å¸Å¡â‚¬</span>'
                        : '<span class="drive-status drive-disabled" title="JDrive disabled Ã¢â‚¬â€ health too low">Ã°Å¸Å¡Â«</span>';
                    item.innerHTML = `
                        <div class="fleet-item-header">
                            <span class="item-name">${GameUI.escapeHtml(fleet.name)}</span>
                            ${jDriveStatus}
                        </div>
                        <div class="fleet-item-stats">
                            <span class="item-detail">Ã°Å¸Å¡Â¢ ${fleet.shipCount} ship(s)</span>
                            <span class="item-detail">Ã¢Å¡â€Ã¯Â¸Â ${fleet.totalDamage} dmg</span>
                        </div>
                        <div class="fleet-item-drives">
                            <span class="drive-info mdrive" title="MDrive (within-system): ${fleet.effectiveMDriveSpeed} range">
                                Ã°Å¸Å½Â¯ M:${fleet.effectiveMDriveSpeed}
                            </span>
                            <span class="drive-info jdrive" title="JDrive (between-system): ${fleet.effectiveJDriveSpeed}">
                                Ã°Å¸Å’Å’ J:${fleet.effectiveJDriveSpeed}
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
                        GameUIState.selectedFleet = fleet.id;
                        renderGameState();
                        showFleetDetail(fleet);
                    });
                    GameUIState.elements.fleetList.appendChild(item);
                });
            } else {
                GameUIState.elements.fleetList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No fleets</div>';
            }
        }

        // Simultaneous turns: all action buttons are always enabled
        if (GameUIState.elements.advanceTurnBtn) GameUIState.elements.advanceTurnBtn.disabled = false;
        if (GameUIState.elements.buildShipBtn) GameUIState.elements.buildShipBtn.disabled = false;
        if (GameUIState.elements.moveFleetBtn) GameUIState.elements.moveFleetBtn.disabled = false;
        if (GameUIState.elements.engageCombatBtn) GameUIState.elements.engageCombatBtn.disabled = false;
        if (GameUIState.elements.buildStationBtn) GameUIState.elements.buildStationBtn.disabled = false;
        if (GameUIState.elements.buildFacilityBtn) GameUIState.elements.buildFacilityBtn.disabled = false;
        if (GameUIState.elements.createFleetBtn) GameUIState.elements.createFleetBtn.disabled = false;
        if (GameUIState.elements.viewBattlesBtn) GameUIState.elements.viewBattlesBtn.disabled = false;

        // Render card-based galaxy view
        renderSectorCardsView();

        // Render subsector panel
        renderSubsectorPanel();
    }

    // ============================================================
    // SUBSECTOR PANEL
    // ============================================================


    function renderSubsectorPanel() {
        if (!GameUIState.gameState || !GameUIState.elements.subsectorList) {
            console.log('renderSubsectorPanel: GameUIState.gameState or subsectorList not available');
            return;
        }

        const subSectors = GameUIState.gameState.subSectors || [];
        const totalSystems = subSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);

        console.log('renderSubsectorPanel:', subSectors.length, 'subsectors,', totalSystems, 'total systems');

        if (GameUIState.elements.subsectorGeneratedCount) {
            GameUIState.elements.subsectorGeneratedCount.textContent = subSectors.length;
        }
        if (GameUIState.elements.subsectorSystemCount) {
            GameUIState.elements.subsectorSystemCount.textContent = totalSystems;
        }

        GameUIState.elements.subsectorList.innerHTML = '';
        if (subSectors.length === 0) {
            GameUIState.elements.subsectorList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">Ã¢ÂÂ³ Waiting for galaxy data...</div>';
            return;
        }

        subSectors.forEach(subSector => {
            const item = document.createElement('div');
            item.className = 'subsector-item' + (GameUIState.selectedSubSector === subSector.id ? ' selected' : '');
            const systemCount = subSector.systemCount || 0;
            item.innerHTML = `
                <div class="subsector-name">
                    <span>Ã°Å¸â€Â¹</span>
                    <span>${GameUI.escapeHtml(subSector.name || 'SubSector ' + subSector.id)}</span>
                </div>
                <div class="subsector-coords">[${subSector.subRow}, ${subSector.subCol}]</div>
                <div class="subsector-systems">Ã¢Â­Â ${systemCount} system(s)</div>
            `;
            item.addEventListener('click', () => {
                GameUIState.selectedSubSector = subSector.id;
                renderSubsectorPanel();
                showSubSectorDetail(subSector);
            });
            GameUIState.elements.subsectorList.appendChild(item);
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
        if (!GameUIState.sessionId || !GameUIState.gameState?.isMyTurn) return;
        if (!GameUIState.elements.advanceTurnBtn) return;
        GameUIState.elements.advanceTurnBtn.disabled = true;
        try {
            // Start turn phases instead of directly advancing
            startTurnPhases();
        } catch (err) {
            addEventLog('ERROR', 'Advance turn failed: ' + err.message);
        } finally {
            if (GameUIState.gameState?.isMyTurn) {
                GameUIState.elements.advanceTurnBtn.disabled = false;
            }
        }
    }

    // ============================================================
    // NEW EMPIRE ACTIONS
    // ============================================================

    function handleBuildStationClick() {
        if (!GameUIState.elements.buildStationModal) return;
        // Populate planet dropdown
        if (GameUIState.elements.stationPlanetSelect) {
            GameUIState.elements.stationPlanetSelect.innerHTML = '<option value="">Home Planet (default)</option>';
            if (GameUIState.gameState?.empireSummary) {
                // We don't have planet list here, so just use default
            }
        }
        if (GameUIState.elements.buildStationError) {
            GameUIState.elements.buildStationError.style.display = 'none';
        }
        GameUIState.elements.buildStationModal.classList.remove('hidden');
    }

    async function handleBuildStationConfirm() {
        if (!GameUIState.sessionId) return;
        const stationType = document.getElementById('stationTypeSelect')?.value || 'MILITARY';
        const planetId = document.getElementById('stationPlanetSelect')?.value;

        try {
            const body = { stationType };
            if (planetId) body.planetId = parseInt(planetId);
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/actions/build-station', body);
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.hideModal('buildStationModal');
            GameUI.showToast('Station ' + stationType + ' built successfully!', 'success');
        } catch (err) {
            const errorEl = document.getElementById('buildStationError');
            if (errorEl) {
                errorEl.textContent = err.message || 'Failed to build station';
                errorEl.style.display = 'block';
            }
        }
    }

    function handleBuildFacilityClick() {
        if (!GameUIState.elements.buildFacilityModal) return;
        // Populate planet dropdown from subsector data
        if (GameUIState.elements.facilityPlanetSelect) {
            GameUIState.elements.facilityPlanetSelect.innerHTML = '<option value="">Select a planet...</option>';
            // Collect owned planets from subsector data
            const ownedPlanets = [];
            if (GameUIState.gameState?.subSectors) {
                GameUIState.gameState.subSectors.forEach(ss => {
                    if (ss.systems) {
                        ss.systems.forEach(sys => {
                            if (sys.planets) {
                                sys.planets.forEach(p => {
                                    if (p.ownerEmpireId !== null && p.ownerEmpireId !== undefined) {
                                        ownedPlanets.push(p);
                                    }
                                });
                            }
                        });
                    }
                });
            }
            // Deduplicate by id
            const seen = new Set();
            ownedPlanets.forEach(p => {
                if (!seen.has(p.id)) {
                    seen.add(p.id);
                    const opt = document.createElement('option');
                    opt.value = p.id;
                    opt.textContent = (p.name || 'Planet #' + p.id) + ' (' + (p.planetType || 'Unknown') + ')';
                    GameUIState.elements.facilityPlanetSelect.appendChild(opt);
                }
            });
        }
        if (GameUIState.elements.buildFacilityError) {
            GameUIState.elements.buildFacilityError.style.display = 'none';
        }
        GameUIState.elements.buildFacilityModal.classList.remove('hidden');
    }

    async function handleBuildFacilityConfirm() {
        if (!GameUIState.sessionId) return;
        const planetId = document.getElementById('facilityPlanetSelect')?.value;
        const facilityType = document.getElementById('facilityTypeSelect')?.value || 'COLONY_DOME';

        if (!planetId) {
            const errorEl = document.getElementById('buildFacilityError');
            if (errorEl) {
                errorEl.textContent = 'Please select a planet';
                errorEl.style.display = 'block';
            }
            return;
        }

        try {
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/actions/build-facility',
                { planetId: parseInt(planetId), facilityType });
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.hideModal('buildFacilityModal');
            GameUI.showToast('Facility ' + facilityType + ' built successfully!', 'success');
        } catch (err) {
            const errorEl = document.getElementById('buildFacilityError');
            if (errorEl) {
                errorEl.textContent = err.message || 'Failed to build facility';
                errorEl.style.display = 'block';
            }
        }
    }

    function handleCreateFleetClick() {
        if (!GameUIState.elements.createFleetModal) return;
        if (GameUIState.elements.fleetNameInput) GameUIState.elements.fleetNameInput.value = '';
        if (GameUIState.elements.createFleetError) GameUIState.elements.createFleetError.style.display = 'none';
        GameUIState.elements.createFleetModal.classList.remove('hidden');
    }

    async function handleCreateFleetConfirm() {
        if (!GameUIState.sessionId) return;
        const fleetName = document.getElementById('fleetNameInput')?.value?.trim();

        if (!fleetName) {
            const errorEl = document.getElementById('createFleetError');
            if (errorEl) {
                errorEl.textContent = 'Please enter a fleet name';
                errorEl.style.display = 'block';
            }
            return;
        }

        try {
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/actions/create-fleet',
                { fleetName });
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.hideModal('createFleetModal');
            GameUI.showToast('Fleet "' + fleetName + '" created!', 'success');
        } catch (err) {
            const errorEl = document.getElementById('createFleetError');
            if (errorEl) {
                errorEl.textContent = err.message || 'Failed to create fleet';
                errorEl.style.display = 'block';
            }
        }
    }

    async function handleViewBattlesClick() {
        if (!GameUIState.sessionId || !GameUIState.elements.battleStatusModal) return;
        try {
            const battles = await GameAPI.get('/api/v1/sessions/' + GameUIState.sessionId + '/battles');
            renderBattleStatus(battles);
            GameUIState.elements.battleStatusModal.classList.remove('hidden');
        } catch (err) {
            GameUI.showToast('Failed to load battles: ' + err.message, 'error');
        }
    }

    function renderBattleStatus(battles) {
        const list = document.getElementById('battleStatusList');
        if (!list) return;

        if (!battles || battles.length === 0) {
            list.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No active battles</div>';
            return;
        }

        list.innerHTML = battles.map(b => {
            const atkHullPct = b.attackerRemainingHull > 0 ? Math.round((b.attackerRemainingHull / Math.max(b.attackerRemainingHull + 100, 1)) * 100) : 0;
            const defHullPct = b.defenderRemainingHull > 0 ? Math.round((b.defenderRemainingHull / Math.max(b.defenderRemainingHull + 100, 1)) * 100) : 0;
            const stateClass = b.state === 'IN_PROGRESS' ? 'in-progress' : 'completed';
            const stateLabel = b.state === 'IN_PROGRESS' ? 'In Progress' : (b.outcome || 'Completed');

            let eventsHtml = '';
            if (b.events && b.events.length > 0) {
                eventsHtml = '<div class="battle-card-events">' +
                    b.events.map(e => '<div class="battle-event-item">' + GameUI.escapeHtml(e) + '</div>').join('') +
                    '</div>';
            }

            let actionsHtml = '';
            if (b.state === 'IN_PROGRESS') {
                actionsHtml = '<div class="battle-card-actions">' +
                    '<button class="btn-small" onclick="GameUI.reinforceBattle(' + b.battleId + ')">Reinforce</button>' +
                    '<button class="btn-small danger" onclick="GameUI.retreatBattle(' + b.battleId + ')">Retreat</button>' +
                    '</div>';
            }

            return '<div class="battle-card">' +
                '<div class="battle-card-header">' +
                    '<span class="battle-card-title">Battle #' + b.battleId + ' (Round ' + b.currentRound + '/' + b.maxRounds + ')</span>' +
                    '<span class="battle-card-state ' + stateClass + '">' + stateLabel + '</span>' +
                '</div>' +
                '<div class="battle-card-body">' +
                    '<div class="battle-fleet-row">' +
                        '<span class="battle-fleet-name attacker">Ã¢Å¡â€Ã¯Â¸Â ' + GameUI.escapeHtml(b.attackerName) + '</span>' +
                        '<div class="battle-hull-bar"><div class="battle-hull-fill attacker" style="width:' + atkHullPct + '%"></div></div>' +
                        '<span class="battle-fleet-stats">' + b.attackerShipCount + ' ships, ' + b.totalAttackerDamage + ' dmg</span>' +
                    '</div>' +
                    '<div class="battle-fleet-row">' +
                        '<span class="battle-fleet-name defender">Ã°Å¸â€ºÂ¡Ã¯Â¸Â ' + GameUI.escapeHtml(b.defenderName) + '</span>' +
                        '<div class="battle-hull-bar"><div class="battle-hull-fill defender" style="width:' + defHullPct + '%"></div></div>' +
                        '<span class="battle-fleet-stats">' + b.defenderShipCount + ' ships, ' + b.totalDefenderDamage + ' dmg</span>' +
                    '</div>' +
                '</div>' +
                eventsHtml +
                actionsHtml +
            '</div>';
        }).join('');
    }

    // ============================================================
    // TURN GameUIState.PHASES
    // ============================================================
    function startTurnPhases() {
        GameUIState.currentPhase = 1;
        showPhaseModal(GameUIState.PHASES[0]);
    }

    function showPhaseModal(phaseName) {
        // Hide all phase modals
        Object.values(GameUIState.PHASE_MODAL_IDS).forEach(id => GameUI.hideModal(id));

        const modalId = GameUIState.PHASE_MODAL_IDS[phaseName];
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

        addEventLog('PHASE', `Entered ${GameUIState.PHASE_NAMES[phaseName]} phase`);
    }

    function nextPhase() {
        if (GameUIState.currentPhase >= GameUIState.PHASES.length) {
            endTurnPhases();
            return;
        }
        const nextPhaseName = GameUIState.PHASES[GameUIState.currentPhase];
        GameUIState.currentPhase++;
        showPhaseModal(nextPhaseName);
    }

    async function endTurnPhases() {
        // Hide all phase modals
        Object.values(GameUIState.PHASE_MODAL_IDS).forEach(id => GameUI.hideModal(id));
        GameUIState.currentPhase = 0;

        // Now advance the turn on the server
        if (!GameUIState.sessionId) return;
        try {
            await GameAPI.advanceTurn(GameUIState.sessionId);
            addEventLog('ACTION', 'Turn completed and advanced');
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'End turn failed: ' + err.message);
        }
    }

    function handlePhaseAction(action) {
        const phaseName = GameUIState.PHASES[GameUIState.currentPhase - 1];
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
        if (!shipType || !GameUIState.sessionId) return;

        try {
            await GameAPI.buildShip(GameUIState.sessionId, shipType, GameUIState.selectedFleet);
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
                if (!GameUIState.selectedFleet) {
                    addEventLog('WARN', 'No fleet selected');
                    return;
                }
                const coords = GameUIState.elements.targetCoords?.value;
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

    // Combat state
    let selectedAttackerFleet = null;
    let selectedDefenderFleet = null;

    function handleCombatPhaseAction(action) {
        switch (action) {
            case 'engage-combat':
                showCombatModal();
                break;
            case 'defensive-stance':
                addEventLog('INFO', 'Defensive stance activated for all fleets');
                GameUI.showToast('Defensive stance activated', 'info');
                break;
        }
    }

    function showCombatModal() {
        // Create combat modal dynamically
        let modal = document.getElementById('combatModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'combatModal';
            modal.className = 'modal-overlay hidden';
            modal.innerHTML = `
                <div class="modal-content combat-modal">
                    <div class="modal-header">
                        <h3><svg class="icon"><use href="#icon-sword"></use></svg> Engage Combat</h3>
                        <button class="modal-close" id="combatModalClose">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="combat-selectors">
                            <div class="combat-selector-group">
                                <label>Attacker Fleet (Yours)</label>
                                <select class="form-input" id="combatAttackerSelect">
                                    <option value="">Ã¢â‚¬â€ Select attacker fleet Ã¢â‚¬â€</option>
                                </select>
                            </div>
                            <div class="combat-vs-divider">VS</div>
                            <div class="combat-selector-group">
                                <label>Defender Fleet (Enemy)</label>
                                <select class="form-input" id="combatDefenderSelect">
                                    <option value="">Ã¢â‚¬â€ Select enemy fleet Ã¢â‚¬â€</option>
                                </select>
                            </div>
                        </div>
                        <div class="combat-odds" id="combatOdds">
                            <div class="combat-odds-header">Combat Odds</div>
                            <div class="combat-odds-body" id="combatOddsBody">
                                <p class="text-center" style="color: var(--text-muted);">Select both fleets to see combat odds</p>
                            </div>
                        </div>
                        <div class="combat-result" id="combatResult" style="display:none;"></div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn-small" id="combatModalCancel">Cancel</button>
                        <button class="btn-small danger" id="combatModalEngage" disabled>Ã¢Å¡â€Ã¯Â¸Â Engage Combat</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            // Bind events
            document.getElementById('combatModalClose').addEventListener('click', () => GameUI.hideModal('combatModal'));
            document.getElementById('combatModalCancel').addEventListener('click', () => GameUI.hideModal('combatModal'));
            document.getElementById('combatModalEngage').addEventListener('click', () => executeCombat());
            document.getElementById('combatAttackerSelect').addEventListener('change', () => onCombatFleetChange());
            document.getElementById('combatDefenderSelect').addEventListener('change', () => onCombatFleetChange());
        }
        // Populate fleet selectors
        populateCombatSelectors();
        // Reset state
        selectedAttackerFleet = null;
        selectedDefenderFleet = null;
        document.getElementById('combatResult').style.display = 'none';
        document.getElementById('combatModalEngage').disabled = true;
        document.getElementById('combatOddsBody').innerHTML = '<p class="text-center" style="color: var(--text-muted);">Select both fleets to see combat odds</p>';
        showModal('combatModal');
    }

    function populateCombatSelectors() {
        const attackerSelect = document.getElementById('combatAttackerSelect');
        const defenderSelect = document.getElementById('combatDefenderSelect');
        if (!attackerSelect || !defenderSelect) return;

        const myPlayerId = getCurrentPlayerId();
        const allFleets = GameUIState.gameState?.fleets || [];
        const myFleets = allFleets.filter(f => f.ownerId === myPlayerId);
        const enemyFleets = allFleets.filter(f => f.ownerId !== myPlayerId);

        // Populate attacker dropdown
        attackerSelect.innerHTML = '<option value="">Ã¢â‚¬â€ Select attacker fleet Ã¢â‚¬â€</option>';
        myFleets.forEach(fleet => {
            const option = document.createElement('option');
            option.value = fleet.id;
            const healthPercent = Math.round((fleet.healthRatio || 0) * 100);
            option.textContent = `${fleet.name || 'Fleet ' + fleet.id} (${fleet.shipCount || 0} ships, ${healthPercent}% HP)`;
            attackerSelect.appendChild(option);
        });

        // Populate defender dropdown
        defenderSelect.innerHTML = '<option value="">Ã¢â‚¬â€ Select enemy fleet Ã¢â‚¬â€</option>';
        enemyFleets.forEach(fleet => {
            const option = document.createElement('option');
            option.value = fleet.id;
            const healthPercent = Math.round((fleet.healthRatio || 0) * 100);
            option.textContent = `${fleet.name || 'Enemy Fleet ' + fleet.id} (${fleet.shipCount || '?'} ships, ${healthPercent}% HP)`;
            defenderSelect.appendChild(option);
        });

        if (myFleets.length === 0) {
            attackerSelect.innerHTML = '<option value="">Ã¢â‚¬â€ No fleets available Ã¢â‚¬â€</option>';
        }
        if (enemyFleets.length === 0) {
            defenderSelect.innerHTML = '<option value="">Ã¢â‚¬â€ No enemies detected Ã¢â‚¬â€</option>';
        }
    }

    function onCombatFleetChange() {
        const attackerSelect = document.getElementById('combatAttackerSelect');
        const defenderSelect = document.getElementById('combatDefenderSelect');
        const engageBtn = document.getElementById('combatModalEngage');

        selectedAttackerFleet = attackerSelect?.value ? parseInt(attackerSelect.value) : null;
        selectedDefenderFleet = defenderSelect?.value ? parseInt(defenderSelect.value) : null;

        engageBtn.disabled = !(selectedAttackerFleet && selectedDefenderFleet);

        if (selectedAttackerFleet && selectedDefenderFleet) {
            calculateCombatOdds();
        } else {
            document.getElementById('combatOddsBody').innerHTML = '<p class="text-center" style="color: var(--text-muted);">Select both fleets to see combat odds</p>';
        }
    }

    function calculateCombatOdds() {
        const allFleets = GameUIState.gameState?.fleets || [];
        const attacker = allFleets.find(f => f.id === selectedAttackerFleet);
        const defender = allFleets.find(f => f.id === selectedDefenderFleet);
        if (!attacker || !defender) return;

        const attackerDamage = attacker.totalDamage || 0;
        const defenderDamage = defender.totalDamage || 0;
        const attackerShips = attacker.shipCount || 0;
        const defenderShips = defender.shipCount || 0;
        const attackerShields = attacker.currentShieldPoints || 0;
        const defenderShields = defender.currentShieldPoints || 0;
        const attackerHull = attacker.currentHullPoints || 0;
        const defenderHull = defender.currentHullPoints || 0;

        // Calculate combat strength score
        const attackerStrength = attackerDamage * attackerShips + attackerShields * 0.5 + attackerHull * 0.3;
        const defenderStrength = defenderDamage * defenderShips + defenderShields * 0.5 + defenderHull * 0.3;
        const totalStrength = attackerStrength + defenderStrength;
        const winChance = totalStrength > 0 ? Math.round((attackerStrength / totalStrength) * 100) : 50;

        // Determine advantage
        let advantageClass = 'even';
        let advantageText = 'Even match';
        if (winChance >= 60) {
            advantageClass = 'favorable';
            advantageText = 'Favorable odds';
        } else if (winChance <= 40) {
            advantageClass = 'unfavorable';
            advantageText = 'Unfavorable odds';
        }

        document.getElementById('combatOddsBody').innerHTML = `
            <div class="combat-odds-comparison">
                <div class="combat-side attacker-side">
                    <div class="combat-side-label">Attacker</div>
                    <div class="combat-side-name">${GameUI.escapeHtml(attacker.name || 'Fleet')}</div>
                    <div class="combat-stat-row"><span>Ships:</span><span>${attackerShips}</span></div>
                    <div class="combat-stat-row"><span>Damage:</span><span>${attackerDamage}</span></div>
                    <div class="combat-stat-row"><span>Shields:</span><span>${attackerShields}</span></div>
                    <div class="combat-stat-row"><span>Hull:</span><span>${attackerHull}</span></div>
                    <div class="combat-stat-row"><span>Strength:</span><span>${Math.round(attackerStrength)}</span></div>
                </div>
                <div class="combat-odds-center">
                    <div class="combat-win-chance ${advantageClass}">${winChance}%</div>
                    <div class="combat-advantage ${advantageClass}">${advantageText}</div>
                </div>
                <div class="combat-side defender-side">
                    <div class="combat-side-label">Defender</div>
                    <div class="combat-side-name">${GameUI.escapeHtml(defender.name || 'Enemy Fleet')}</div>
                    <div class="combat-stat-row"><span>Ships:</span><span>${defenderShips}</span></div>
                    <div class="combat-stat-row"><span>Damage:</span><span>${defenderDamage}</span></div>
                    <div class="combat-stat-row"><span>Shields:</span><span>${defenderShields}</span></div>
                    <div class="combat-stat-row"><span>Hull:</span><span>${defenderHull}</span></div>
                    <div class="combat-stat-row"><span>Strength:</span><span>${Math.round(defenderStrength)}</span></div>
                </div>
            </div>
        `;
    }

    async function executeCombat() {
        if (!GameUIState.sessionId || !selectedAttackerFleet || !selectedDefenderFleet) return;

        const engageBtn = document.getElementById('combatModalEngage');
        engageBtn.disabled = true;
        engageBtn.textContent = 'Ã¢Å¡â€Ã¯Â¸Â Engaging...';

        try {
            addEventLog('ACTION', `Combat: Fleet ${selectedAttackerFleet} attacks Fleet ${selectedDefenderFleet}`);
            await GameAPI.engageCombat(GameUIState.sessionId, selectedAttackerFleet, selectedDefenderFleet);
            await loadGameState();

            // Show battle result
            showBattleResult();
            GameUI.showToast('Combat resolved!', 'success');
        } catch (err) {
            addEventLog('ERROR', 'Combat failed: ' + err.message);
            GameUI.showToast('Combat failed: ' + err.message, 'error');
        } finally {
            engageBtn.disabled = false;
            engageBtn.textContent = 'Ã¢Å¡â€Ã¯Â¸Â Engage Combat';
        }
    }

    function showBattleResult() {
        const resultDiv = document.getElementById('combatResult');
        const allFleets = GameUIState.gameState?.fleets || [];
        const attacker = allFleets.find(f => f.id === selectedAttackerFleet);
        const defender = allFleets.find(f => f.id === selectedDefenderFleet);

        if (!attacker && !defender) {
            resultDiv.style.display = 'none';
            return;
        }

        const attackerAlive = attacker && attacker.shipCount > 0;
        const defenderAlive = defender && defender.shipCount > 0;

        let outcomeText = 'Battle Complete';
        let outcomeClass = 'draw';
        if (attackerAlive && !defenderAlive) {
            outcomeText = 'Victory!';
            outcomeClass = 'victory';
        } else if (!attackerAlive && defenderAlive) {
            outcomeText = 'Defeat!';
            outcomeClass = 'defeat';
        } else if (!attackerAlive && !defenderAlive) {
            outcomeText = 'Mutual Destruction';
            outcomeClass = 'draw';
        } else {
            outcomeClass = 'draw';
        }

        resultDiv.style.display = 'block';
        resultDiv.innerHTML = `
            <div class="battle-result ${outcomeClass}">
                <div class="battle-result-outcome">${outcomeText}</div>
                <div class="battle-result-details">
                    ${attacker ? `
                        <div class="battle-fleet-status">
                            <div class="battle-fleet-name">${GameUI.escapeHtml(attacker.name || 'Attacker')}</div>
                            <div class="battle-fleet-stats">
                                <span>Ships: ${attacker.shipCount || 0}</span>
                                <span>HP: ${Math.round((attacker.healthRatio || 0) * 100)}%</span>
                                <span>Damage: ${attacker.totalDamage || 0}</span>
                            </div>
                        </div>
                    ` : ''}
                    ${defender ? `
                        <div class="battle-fleet-status">
                            <div class="battle-fleet-name">${GameUI.escapeHtml(defender.name || 'Defender')}</div>
                            <div class="battle-fleet-stats">
                                <span>Ships: ${defender.shipCount || 0}</span>
                                <span>HP: ${Math.round((defender.healthRatio || 0) * 100)}%</span>
                                <span>Damage: ${defender.totalDamage || 0}</span>
                            </div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }

    function populateResourcePhase() {
        const container = GameUIState.elements.phaseResourceSummary;
        if (!container || !GameUIState.gameState) return;

        const pipeline = GameUIState.gameState.pipelineSummary;
        if (pipeline) {
            const modules = pipeline.modules || {};
            const raw = pipeline.rawResources || {};
            const refined = pipeline.refinedResources || {};
            const mining = pipeline.miningRate || {};
            container.innerHTML = `
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â© Hull Modules</span>
                    <span class="phase-resource-value">${modules.HULL_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â« Weapon Modules</span>
                    <span class="phase-resource-value">${modules.WEAPON_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€ºÂ¡Ã¯Â¸Â Shield Modules</span>
                    <span class="phase-resource-value">${modules.SHIELD_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢Å¡â„¢Ã¯Â¸Â Engine Modules</span>
                    <span class="phase-resource-value">${modules.ENGINE_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸Ââ€”Ã¯Â¸Â Facility Modules</span>
                    <span class="phase-resource-value">${modules.FACILITY_MODULE || 0}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢â€ºÂÃ¯Â¸Â Raw Resources</span>
                    <span class="phase-resource-value">${Object.values(raw).reduce((a, b) => a + b, 0)}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â§ Refined Resources</span>
                    <span class="phase-resource-value">${Object.values(refined).reduce((a, b) => a + b, 0)}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸ÂÂ­ Mining Ops</span>
                    <span class="phase-resource-value">${pipeline.miningOperations} (+${Object.values(mining).reduce((a, b) => a + b, 0)}/turn)</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â¬ Processing</span>
                    <span class="phase-resource-value">${pipeline.activeProcessing}/${pipeline.miningOperations}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€œÂ¦ Construction Queue</span>
                    <span class="phase-resource-value">${pipeline.constructionQueueSize}</span>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â© Hull Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â« Weapon Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€ºÂ¡Ã¯Â¸Â Shield Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢Å¡â„¢Ã¯Â¸Â Engine Modules</span>
                    <span class="phase-resource-value">0</span>
                </div>
            `;
        }
    }

    async function loadPipelineDetails() {
        const container = GameUIState.elements.phasePipelineDetails;
        if (!container || !currentSessionId) return;

        const isHidden = container.classList.contains('hidden');
        if (!isHidden) {
            container.classList.add('hidden');
            return;
        }

        try {
            const response = await gameApi.get(`/sessions/${currentSessionId}/pipeline/details`);
            const details = response.data;

            // Calculate pipeline rates for bottleneck analysis
            const miningOps = details.miningOperations || [];
            const transportOrders = details.transportOrders || [];
            const constructionOrders = details.constructionOrders || [];

            const totalMiningRate = miningOps.reduce((sum, op) => {
                const rates = op.productionRate || {};
                return sum + Object.values(rates).reduce((a, b) => a + b, 0);
            }, 0);
            const activeTransports = transportOrders.length;
            const activeConstruction = constructionOrders.length;

            // Identify bottlenecks
            const bottlenecks = [];
            if (miningOps.length > 0 && activeTransports === 0) {
                bottlenecks.push('Mining output not being transported');
            }
            if (activeTransports > 0 && activeConstruction === 0) {
                bottlenecks.push('Resources in transit but no construction');
            }
            if (miningOps.length === 0) {
                bottlenecks.push('No mining operations active');
            }

            let html = '';

            // Pipeline Flow Diagram
            html += '<div class="pipeline-flow-diagram">';
            html += `
                <div class="pipeline-flow-stage ${miningOps.length > 0 ? 'active' : 'inactive'}">
                    <div class="pipeline-flow-icon">Ã¢â€ºÂÃ¯Â¸Â</div>
                    <div class="pipeline-flow-name">Mining</div>
                    <div class="pipeline-flow-count">${miningOps.length} ops</div>
                    <div class="pipeline-flow-rate">${totalMiningRate.toFixed(1)}/turn</div>
                </div>
                <div class="pipeline-flow-arrow">Ã¢â€ â€™</div>
                <div class="pipeline-flow-stage ${activeTransports > 0 ? 'active' : 'inactive'}">
                    <div class="pipeline-flow-icon">Ã°Å¸Å¡Å¡</div>
                    <div class="pipeline-flow-name">Transport</div>
                    <div class="pipeline-flow-count">${activeTransports} orders</div>
                </div>
                <div class="pipeline-flow-arrow">Ã¢â€ â€™</div>
                <div class="pipeline-flow-stage ${activeConstruction > 0 ? 'active' : 'inactive'}">
                    <div class="pipeline-flow-icon">Ã°Å¸Ââ€”Ã¯Â¸Â</div>
                    <div class="pipeline-flow-name">Construction</div>
                    <div class="pipeline-flow-count">${activeConstruction} items</div>
                </div>
            `;
            html += '</div>';

            // Bottleneck warnings
            if (bottlenecks.length > 0) {
                html += '<div class="pipeline-bottlenecks">';
                bottlenecks.forEach(bn => {
                    html += `<div class="pipeline-bottleneck">Ã¢Å¡Â Ã¯Â¸Â ${bn}</div>`;
                });
                html += '</div>';
            }

            let detailsHtml = '<div class="pipeline-details-grid">';

            // Mining Operations
            detailsHtml += '<div class="pipeline-section"><h4>Ã¢â€ºÂÃ¯Â¸Â Mining Operations</h4>';
            if (miningOps.length > 0) {
                miningOps.forEach(op => {
                    detailsHtml += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${op.celestialBodyName} (${op.celestialBodyType})</span>
                        <span class="pipeline-item-value">`;
                    const rates = op.productionRate || {};
                    detailsHtml += Object.entries(rates).map(([k, v]) => `${k}=${v}`).join(', ');
                    detailsHtml += `</span>
                        <span class="pipeline-item-meta">Storage: ${op.storage ? Object.entries(op.storage).map(([k,v]) => `${k}=${v}`).join(', ') : 'N/A'}</span>
                    </div>`;
                });
            } else {
                detailsHtml += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active mining</div>';
            }
            detailsHtml += '</div>';

            // Transport Orders
            detailsHtml += '<div class="pipeline-section"><h4>Ã°Å¸Å¡Å¡ Transport Orders</h4>';
            if (transportOrders.length > 0) {
                transportOrders.forEach(order => {
                    const progress = Math.round((order.progress || 0) * 100);
                    detailsHtml += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${order.resourceType}: ${order.totalAmount}</span>
                        <span class="pipeline-item-value">${order.turnsCompleted || 0}/${order.turnsTotal} turns</span>
                        <span class="pipeline-item-meta">${progress}%</span>
                        <div class="pipeline-progress-bar"><div class="pipeline-progress-fill" style="width: ${progress}%"></div></div>
                    </div>`;
                });
            } else {
                detailsHtml += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active transports</div>';
            }
            detailsHtml += '</div>';

            // Construction Queue
            detailsHtml += '<div class="pipeline-section"><h4>Ã°Å¸Ââ€”Ã¯Â¸Â Construction Queue</h4>';
            if (constructionOrders.length > 0) {
                constructionOrders.forEach(order => {
                    const name = order.stationType || order.planetFacility || 'Unknown';
                    const progress = Math.round((order.progress || 0) * 100);
                    detailsHtml += `<div class="pipeline-item">
                        <span class="pipeline-item-name">${name} (${order.constructionType})</span>
                        <span class="pipeline-item-value">${order.turnsCompleted}/${order.totalTurns} turns</span>
                        <span class="pipeline-item-meta">${progress}%</span>
                        <div class="pipeline-progress-bar"><div class="pipeline-progress-fill" style="width: ${progress}%"></div></div>
                    </div>`;
                });
            } else {
                detailsHtml += '<div class="text-center" style="color: var(--text-muted); padding: 10px;">No active construction</div>';
            }
            detailsHtml += '</div></div>';

            container.innerHTML = html + detailsHtml;
            container.classList.remove('hidden');
        } catch (err) {
            addEventLog('ERROR', 'Failed to load pipeline details: ' + err.message);
        }
    }

    // Ship type definitions matching backend ShipType enum
    const SHIP_TYPES = [
        { id: 'FRIGATE', name: 'Frigate', icon: 'Ã°Å¸Å¡â‚¬', hull: 50, slots: 3, buildTime: 2, mDrive: 10, jDrive: 12, evasion: 20, modules: 1, desc: 'Light, fast scout ship' },
        { id: 'DESTROYER', name: 'Destroyer', icon: 'Ã¢Å¡â€Ã¯Â¸Â', hull: 100, slots: 5, buildTime: 3, mDrive: 8, jDrive: 10, evasion: 15, modules: 2, desc: 'Medium escort ship' },
        { id: 'CRUISER', name: 'Cruiser', icon: 'Ã°Å¸â€ºÂ¡Ã¯Â¸Â', hull: 200, slots: 8, buildTime: 5, mDrive: 6, jDrive: 8, evasion: 10, modules: 8, desc: 'Heavy multi-role combat ship' },
        { id: 'BATTLESHIP', name: 'Battleship', icon: 'Ã°Å¸â€™Â¥', hull: 400, slots: 12, buildTime: 8, mDrive: 4, jDrive: 5, evasion: 5, modules: 27, desc: 'Capital warship' },
        { id: 'CARRIER', name: 'Carrier', icon: 'Ã¢Å“Ë†Ã¯Â¸Â', hull: 300, slots: 6, buildTime: 7, mDrive: 5, jDrive: 6, evasion: 8, modules: 12, desc: 'Deploys fighter wings' },
        { id: 'MOTHERSHIP', name: 'Mothership', icon: 'Ã°Å¸Å’Å¸', hull: 800, slots: 20, buildTime: 15, mDrive: 2, jDrive: 3, evasion: 2, modules: 64, desc: 'Ultimate superweapon' }
    ];

    function populateShipBuildPhase() {
        const container = GameUIState.elements.phaseShipQueue;
        if (!container) return;

        const pipeline = GameUIState.gameState?.pipelineSummary;
        const modules = pipeline?.modules || {};
        const hullModules = modules.HULL_MODULE || 0;
        const weaponModules = modules.WEAPON_MODULE || 0;
        const shieldModules = modules.SHIELD_MODULE || 0;
        const engineModules = modules.ENGINE_MODULE || 0;

        // Calculate total available modules for cost estimation
        const totalModules = hullModules + weaponModules + shieldModules + engineModules;

        let html = `
            <div class="ship-build-resources">
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â© Hull</span>
                    <span class="phase-resource-value">${hullModules}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€Â« Weapon</span>
                    <span class="phase-resource-value">${weaponModules}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã°Å¸â€ºÂ¡Ã¯Â¸Â Shield</span>
                    <span class="phase-resource-value">${shieldModules}</span>
                </div>
                <div class="phase-resource-item">
                    <span class="phase-resource-name">Ã¢Å¡â„¢Ã¯Â¸Â Engine</span>
                    <span class="phase-resource-value">${engineModules}</span>
                </div>
                <div class="ship-build-total">Total modules: ${totalModules}</div>
            </div>
            <div class="ship-type-cards">
        `;

        SHIP_TYPES.forEach(ship => {
            // Estimate cost as hull modules needed (simplified: 1 module per hull point / 10)
            const estimatedCost = Math.max(1, Math.ceil(ship.modules / 2));
            const canAfford = totalModules >= estimatedCost;
            const affordableClass = canAfford ? '' : ' unaffordable';

            html += `
                <div class="ship-type-card${affordableClass}" data-ship-type="${ship.id}" data-cost="${estimatedCost}">
                    <div class="ship-type-header">
                        <span class="ship-type-icon">${ship.icon}</span>
                        <span class="ship-type-name">${ship.name}</span>
                        <span class="ship-type-cost">${estimatedCost} modules</span>
                    </div>
                    <div class="ship-type-desc">${ship.desc}</div>
                    <div class="ship-type-stats">
                        <div class="ship-stat"><span class="ship-stat-label">Hull:</span><span>${ship.hull}</span></div>
                        <div class="ship-stat"><span class="ship-stat-label">Slots:</span><span>${ship.slots}</span></div>
                        <div class="ship-stat"><span class="ship-stat-label">Build:</span><span>${ship.buildTime} turns</span></div>
                        <div class="ship-stat drive-info mdrive">Ã°Å¸Å½Â¯ M:${ship.mDrive}</div>
                        <div class="ship-stat drive-info jdrive">Ã°Å¸Å’Å’ J:${ship.jDrive}</div>
                        <div class="ship-stat"><span class="ship-stat-label">Evasion:</span><span>${ship.evasion}%</span></div>
                    </div>
                    <button class="btn-small primary ship-build-btn${canAfford ? '' : ' disabled'}" ${canAfford ? '' : 'disabled'}>
                        Build ${ship.name}
                    </button>
                </div>
            `;
        });

        html += '</div>';
        container.innerHTML = html;

        // Bind build button clicks
        container.querySelectorAll('.ship-build-btn:not(.disabled)').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const card = btn.closest('.ship-type-card');
                const shipType = card.dataset.shipType;
                handleBuildShipFromPhase(shipType);
            });
        });
    }

    async function handleBuildShipFromPhase(shipType) {
        if (!GameUIState.sessionId) return;
        try {
            await GameAPI.buildShip(GameUIState.sessionId, shipType, GameUIState.selectedFleet);
            addEventLog('ACTION', `Building ${shipType}`);
            GameUI.showToast(`Queued ${shipType} for construction`, 'success');
            await loadGameState();
            // Refresh the phase if still open
            if (GameUIState.currentPhase > 0 && GameUIState.PHASES[GameUIState.currentPhase - 1] === 'shipBuild') {
                populateShipBuildPhase();
            }
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
            GameUI.showToast('Build failed: ' + err.message, 'error');
        }
    }

    function populateFleetMovePhase() {
        const container = GameUIState.elements.phaseFleetList;
        if (!container) return;

        const fleets = GameUIState.gameState?.fleets || [];
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
            item.className = 'phase-fleet-item' + (GameUIState.selectedFleet === fleet.id ? ' selected' : '');
            item.innerHTML = `
                <div class="phase-fleet-header">
                    <span class="phase-option-name">Ã°Å¸Å¡â‚¬ ${GameUI.escapeHtml(fleet.name || 'Fleet ' + fleet.id)}</span>
                    <span class="phase-option-desc">${fleet.shipCount || 0} ships</span>
                </div>
                <div class="phase-fleet-stats">
                    <span class="drive-badge mdrive" title="MDrive range: ${fleet.effectiveMDriveSpeed || 0}">
                        Ã°Å¸Å½Â¯ M:${fleet.effectiveMDriveSpeed || 0}
                    </span>
                    <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}" title="JDrive: ${fleet.effectiveJDriveSpeed || 0}">
                        Ã°Å¸Å’Å’ J:${fleet.effectiveJDriveSpeed || 0}
                    </span>
                    <span class="phase-option-desc">Ã¢Å¡â€Ã¯Â¸Â ${fleet.totalDamage || 0} dmg</span>
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
                GameUIState.selectedFleet = fleet.id;
                populateFleetMovePhase();
                addEventLog('SYSTEM', `Selected fleet: ${fleet.name || fleet.id}`);
            });
            container.appendChild(item);
        });
    }

    function populateCombatPhase() {
        const container = GameUIState.elements.phaseEnemyList;
        if (!container) return;

        // Show enemy fleets (from other players)
        const allFleets = GameUIState.gameState?.fleets || [];
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
                <span class="phase-option-name">Ã°Å¸â€˜Â¾ ${GameUI.escapeHtml(fleet.name || 'Enemy Fleet')}</span>
                <span class="phase-option-desc">${fleet.shipCount || '?'} ships</span>
            `;
            container.appendChild(item);
        });
    }

    // ============================================================
    // BUILD SHIP & FLEET MOVE
    // ============================================================
    function handleBuildShipClick() {
        // Open the Traveller ship design editor instead of the old modal
        if (typeof ShipDesignUI !== 'undefined' && ShipDesignUI.showShipDesignModal) {
            ShipDesignUI.showShipDesignModal(GameUIState.sessionId);
        } else if (GameUIState.elements.buildShipModal) {
            // Fallback to old modal if ship design module is unavailable
            GameUIState.elements.buildShipModal.classList.remove('hidden');
            if (GameUIState.elements.shipTypeSelect) {
                GameUIState.elements.shipTypeSelect.value = 'FRIGATE';
            }
        }
    }

    async function handleBuildShipConfirm() {
        if (!GameUIState.sessionId) return;
        const shipType = GameUIState.elements.shipTypeSelect?.value || 'FRIGATE';
        GameUI.hideModal('buildShipModal');
        try {
            await GameAPI.buildShip(GameUIState.sessionId, shipType, GameUIState.selectedFleet);
            addEventLog('ACTION', `Building ${shipType}`);
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'Build ship failed: ' + err.message);
        }
    }

    function handleMoveFleetClick() {
        if (!GameUIState.selectedFleet) {
            GameUI.showToast('Select a fleet first', 'warning');
            return;
        }
        // Find the selected fleet data
        const fleet = (GameUIState.gameState.fleets || []).find(f => f.id === GameUIState.selectedFleet);
        if (!fleet) {
            GameUI.showToast('Selected fleet not found', 'error');
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
                                <span class="move-type-icon">Ã°Å¸Å½Â¯</span>
                                <span class="move-type-label">MDrive</span>
                                <span class="move-type-desc">Within-system movement</span>
                            </button>
                            <button class="move-type-btn" data-type="jdrive" id="moveTypeJDrive">
                                <span class="move-type-icon">Ã°Å¸Å’Å’</span>
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
                                    <label for="moveTargetSystemSelect">Target Solar System</label>
                                    <select class="form-input" id="moveTargetSystemSelect">
                                        <option value="">Ã¢â‚¬â€ Select a target system Ã¢â‚¬â€</option>
                                    </select>
                                    <div class="form-help" id="moveSystemHelp">Choose a system from the dropdown or enter ID manually below</div>
                                </div>
                                <div class="form-group">
                                    <label for="moveTargetSystem">Or enter System ID</label>
                                    <input type="number" class="form-input" id="moveTargetSystem" min="1" placeholder="System ID">
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
            document.getElementById('moveFleetModalClose').addEventListener('click', () => GameUI.hideModal('moveFleetModal'));
            document.getElementById('moveFleetCancel').addEventListener('click', () => GameUI.hideModal('moveFleetModal'));
            document.getElementById('moveFleetExecute').addEventListener('click', () => executeMoveFleet(fleet));
            document.getElementById('moveTypeMDrive').addEventListener('click', () => setMoveType('mdrive'));
            document.getElementById('moveTypeJDrive').addEventListener('click', () => setMoveType('jdrive'));
        }
        // Populate fleet info
        document.getElementById('moveFleetName').textContent = fleet.name;
        document.getElementById('moveFleetPosition').textContent = `Position: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})`;
        const jDriveOk = fleet.effectiveJDriveSpeed > 0;
        document.getElementById('moveFleetDrives').innerHTML = `
            <span class="drive-badge mdrive">Ã°Å¸Å½Â¯ MDrive: ${fleet.effectiveMDriveSpeed} range</span>
            <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}">Ã°Å¸Å’Å’ JDrive: ${fleet.effectiveJDriveSpeed}${jDriveOk ? '' : ' (disabled)'}</span>
        `;
        // Populate JDrive target dropdown from game state subSectors
        populateJDriveTargets();
        // Set initial move type
        setMoveType('mdrive');
        // Set default target coords
        document.getElementById('moveTargetX').value = fleet.positionX;
        document.getElementById('moveTargetY').value = fleet.positionY;
        showModal('moveFleetModal');
    }

    function populateJDriveTargets() {
        const select = document.getElementById('moveTargetSystemSelect');
        if (!select) return;
        select.innerHTML = '<option value="">Ã¢â‚¬â€ Select a target system Ã¢â‚¬â€</option>';
        
        const subSectors = GameUIState.gameState?.subSectors || [];
        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;
        
        // Group systems by subsector for organized dropdown
        subSectors.forEach(subSector => {
            if (!subSector.systems || subSector.systems.length === 0) return;
            
            // Add a group label for each subsector
            const group = document.createElement('optgroup');
            const isOwned = subSector.ownerEmpireIds && Array.isArray(subSector.ownerEmpireIds) 
                && subSector.ownerEmpireIds.includes(myEmpireId);
            const contestedLabel = subSector.contested ? ' Ã¢Å¡â€Ã¯Â¸Â' : '';
            const ownedLabel = isOwned ? ' Ã°Å¸ÂÂ ' : '';
            group.label = `${subSector.name} [${subSector.subRow},${subSector.subCol}]${ownedLabel}${contestedLabel}`;
            
            subSector.systems.forEach(sys => {
                const option = document.createElement('option');
                option.value = sys.id || sys.systemId || '';
                const name = sys.name || `System ${sys.id}`;
                const planetCount = sys.planetCount || sys.planets?.length || 0;
                option.textContent = `${name} (${planetCount} planets)`;
                option.title = `ID: ${sys.id}, Planets: ${planetCount}`;
                group.appendChild(option);
            });
            
            select.appendChild(group);
        });
        
        // If no systems found, show a message
        if (select.options.length <= 1) {
            select.innerHTML = '<option value="">Ã¢â‚¬â€ No systems available Ã¢â‚¬â€</option>';
        }
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
            const fleet = (GameUIState.gameState.fleets || []).find(f => f.id === GameUIState.selectedFleet);
            if (fleet) {
                rangeInfo.textContent = `MDrive range: ${fleet.effectiveMDriveSpeed} hexes from current position`;
                rangeInfo.className = 'move-range-info';
            }
        } else {
            coordRow.classList.add('hidden');
            systemRow.classList.remove('hidden');
            const fleet = (GameUIState.gameState.fleets || []).find(f => f.id === GameUIState.selectedFleet);
            if (fleet) {
                if (fleet.effectiveJDriveSpeed <= 0) {
                    rangeInfo.textContent = 'Ã¢Å¡Â Ã¯Â¸Â JDrive is disabled Ã¢â‚¬â€ fleet health is below 20%';
                    rangeInfo.className = 'move-range-info warning';
                } else {
                    rangeInfo.textContent = 'JDrive: Enter target solar system ID (must have hyperlane connection)';
                    rangeInfo.className = 'move-range-info';
                }
            }
        }
    }

    async function executeMoveFleet(fleet) {
        GameUI.hideModal('moveFleetModal');
        if (!GameUIState.sessionId) return;
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
                // Prefer dropdown selection, fall back to manual ID input
                const select = document.getElementById('moveTargetSystemSelect');
                const manualInput = document.getElementById('moveTargetSystem');
                solarSystemId = select && select.value ? parseInt(select.value) 
                    : (manualInput && manualInput.value ? parseInt(manualInput.value) : null);
                if (!solarSystemId) {
                    GameUI.showToast('Select a target system or enter a valid ID', 'warning');
                    return;
                }
                // Look up the system name for the log
                const systemName = findSystemName(solarSystemId);
                addEventLog('ACTION', `JDrive: Jumping ${fleet.name} to ${systemName || 'system ' + solarSystemId}`);
            }
            await GameAPI.moveFleet(GameUIState.sessionId, GameUIState.selectedFleet, targetX, targetY, solarSystemId);
            await loadGameState();
        } catch (err) {
            addEventLog('ERROR', 'Move fleet failed: ' + err.message);
            GameUI.showToast('Move failed: ' + err.message, 'error');
        }
    }

    function findSystemName(systemId) {
        const subSectors = GameUIState.gameState?.subSectors || [];
        for (const subSector of subSectors) {
            for (const sys of (subSector.systems || [])) {
                if ((sys.id || sys.systemId) === systemId) {
                    return sys.name || `System ${systemId}`;
                }
            }
        }
        return null;
    }

    function showFleetDetail(fleet) {
        // Update the right panel with fleet details
        const healthPercent = Math.round(fleet.healthRatio * 100);
        const shieldPercent = fleet.totalShieldPoints > 0 ? Math.round(fleet.currentShieldPoints / fleet.totalShieldPoints * 100) : 0;
        addEventLog('INFO', `Selected ${fleet.name}: MDrive=${fleet.effectiveMDriveSpeed}, JDrive=${fleet.effectiveJDriveSpeed}, Health=${healthPercent}%, Shields=${shieldPercent}%, Damage=${fleet.totalDamage}`);
        
        // Update the target panel to show fleet detail
        const targetPanel = document.getElementById('targetPanel');
        if (!targetPanel) return;
        
        const jDriveOk = fleet.effectiveJDriveSpeed > 0;
        const healthClass = healthPercent < 20 ? 'health-critical' 
            : healthPercent < 50 ? 'health-warning' 
            : healthPercent < 80 ? 'health-damaged' : 'health-good';
        
        let shipsHtml = '';
        if (fleet.ships && fleet.ships.length > 0) {
            shipsHtml = '<div class="fleet-detail-ships"><h4>Ships</h4>';
            fleet.ships.forEach(ship => {
                const shipHealth = Math.round(ship.hullHealthRatio * 100);
                const destroyedClass = ship.destroyed ? ' destroyed' : '';
                shipsHtml += `
                    <div class="ship-detail-item${destroyedClass}">
                        <div class="ship-detail-name">${GameUI.escapeHtml(ship.name || ship.shipType)}</div>
                        <div class="ship-detail-stats">
                            <span class="drive-info mdrive">Ã°Å¸Å½Â¯ M:${ship.mDriveSpeed}</span>
                            <span class="drive-info jdrive">Ã°Å¸Å’Å’ J:${ship.jDriveSpeed}</span>
                        </div>
                        <div class="ship-detail-health">
                            <div class="health-bar-bg">
                                <div class="health-bar-fill" style="width: ${shipHealth}%"></div>
                            </div>
                            <span class="health-text">${shipHealth}%</span>
                        </div>
                        <div class="ship-detail-points">
                            <span>Ã°Å¸â€ºÂ¡Ã¯Â¸Â ${ship.currentShieldPoints}/${ship.totalShieldPoints}</span>
                            <span>Ã°Å¸â€Â§ ${ship.currentHullPoints}/${ship.totalHullPoints}</span>
                        </div>
                        ${ship.totalDamage > 0 ? `<div class="ship-detail-damage">Ã¢Å¡â€Ã¯Â¸Â ${ship.totalDamage} dmg</div>` : ''}
                    </div>
                `;
            });
            shipsHtml += '</div>';
        }
        
        targetPanel.innerHTML = `
            <h3><svg class="icon"><use href="#icon-shield"></use></svg> Fleet Detail</h3>
            <div class="fleet-detail-panel">
                <div class="fleet-detail-name">${GameUI.escapeHtml(fleet.name)}</div>
                <div class="fleet-detail-position">Position: (${fleet.positionX}, ${fleet.positionY}, ${fleet.positionZ})</div>
                <div class="fleet-detail-drives">
                    <span class="drive-badge mdrive">Ã°Å¸Å½Â¯ MDrive: ${fleet.effectiveMDriveSpeed} range</span>
                    <span class="drive-badge jdrive ${jDriveOk ? '' : 'disabled'}">Ã°Å¸Å’Å’ JDrive: ${fleet.effectiveJDriveSpeed}${jDriveOk ? '' : ' (disabled)'}</span>
                </div>
                <div class="fleet-detail-stats">
                    <div class="stat-row">
                        <span class="stat-label">Ships:</span>
                        <span class="stat-value">${fleet.shipCount}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Damage:</span>
                        <span class="stat-value">${fleet.totalDamage}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Shields:</span>
                        <span class="stat-value">${fleet.currentShieldPoints}/${fleet.totalShieldPoints} (${shieldPercent}%)</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Hull:</span>
                        <span class="stat-value">${fleet.currentHullPoints}/${fleet.totalHullPoints} (${healthPercent}%)</span>
                    </div>
                </div>
                <div class="fleet-detail-health ${healthClass}">
                    <div class="health-bar-bg">
                        <div class="health-bar-fill" style="width: ${healthPercent}%"></div>
                    </div>
                    <span class="health-text">Health: ${healthPercent}%</span>
                </div>
                ${shipsHtml}
            </div>
        `;
    }

    async function handleLeaveSession() {
        if (!GameUIState.sessionId) return;
        stopTurnTimerCountdown();
        try {
            await GameAPI.leaveSession(GameUIState.sessionId);
        } catch (err) {
            console.error('Leave session error:', err);
        }
        GameSSE.disconnect();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
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
                GameUI.showToast(`Ã¢ÂÂ© Auto-advanced to Turn ${turn}: ${playerName}`, 'info');
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
            if (data && data.GameUIState.sessionId) {
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
        if (!GameUIState.elements.eventLog) return;
        const entry = document.createElement('div');
        entry.className = 'event-entry fade-in';
        const now = new Date();
        const time = now.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
        entry.innerHTML = `
            <span class="event-time">${time}</span>
            <span class="event-type ${type}">${type}</span>
            <span class="event-message">${GameUI.escapeHtml(message)}</span>
        `;
        GameUIState.elements.eventLog.appendChild(entry);
        GameUIState.elements.eventLog.scrollTop = GameUIState.elements.eventLog.scrollHeight;

        // Limit log entries
        while (GameUIState.elements.eventLog.children.length > 100) {
            GameUIState.elements.eventLog.removeChild(GameUIState.elements.eventLog.firstChild);
        }
    }

    // ============================================================
    // TURN TIMER
    // ============================================================
    function startTurnTimerCountdown() {
        stopTurnTimerCountdown();
        if (GameUIState.turnTimerTotal <= 0) return;
        
        GameUIState.turnTimerInterval = setInterval(() => {
            GameUIState.turnTimerRemaining--;
            if (GameUIState.turnTimerRemaining <= 0) {
                GameUIState.turnTimerRemaining = 0;
            }
            updateTurnTimerDisplay();
            updateTurnProgressBar();
        }, 1000);
    }

    function stopTurnTimerCountdown() {
        if (GameUIState.turnTimerInterval) {
            clearInterval(GameUIState.turnTimerInterval);
            GameUIState.turnTimerInterval = null;
        }
    }

    function updateTurnTimerDisplay() {
        const timerEl = GameUIState.elements.turnTimerValue;
        const timerContainer = GameUIState.elements.turnTimer;
        if (!timerEl) return;
        
        timerEl.textContent = GameUIState.turnTimerRemaining;
        
        // Pulse effect when timer is about to expire (last 2 seconds)
        if (timerContainer) {
            if (GameUIState.turnTimerRemaining <= 2 && GameUIState.turnTimerRemaining > 0) {
                timerContainer.classList.add('urgent');
            } else {
                timerContainer.classList.remove('urgent');
            }
        }
    }

    function updateTurnProgressBar() {
        const fillEl = GameUIState.elements.turnProgressFill;
        if (!fillEl || GameUIState.turnTimerTotal <= 0) return;
        
        const elapsed = GameUIState.turnTimerTotal - GameUIState.turnTimerRemaining;
        const percent = Math.min(100, (elapsed / GameUIState.turnTimerTotal) * 100);
        fillEl.style.width = percent + '%';
        
        // Change color when approaching turn end
        if (GameUIState.turnTimerRemaining <= 2 && GameUIState.turnTimerRemaining > 0) {
            fillEl.classList.add('urgent');
        } else {
            fillEl.classList.remove('urgent');
        }
    }

    function updateTurnIntervalDisplay() {
        const el = GameUIState.elements.turnIntervalValue;
        if (el && GameUIState.turnTimerTotal > 0) {
            el.textContent = GameUIState.turnTimerTotal;
        }
    }

    function updateSchedulerStatus() {
        const dotEl = GameUIState.elements.schedulerDot;
        const labelEl = GameUIState.elements.schedulerLabel;
        if (!dotEl || !labelEl) return;
        
        if (GameUIState.gameState && GameUIState.gameState.schedulerEnabled) {
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







    let galaxyMouseX = 0;
    let galaxyMouseY = 0;

    function setupGalaxyMap() {
        const canvas = GameUIState.elements.galaxyCanvas;
        if (!canvas) return;

        canvas.addEventListener('mousedown', (e) => {
            GameUIState.isDragging = true;
            GameUIState.dragStartX = e.clientX - GameUIState.mapOffsetX;
            GameUIState.dragStartY = e.clientY - GameUIState.mapOffsetY;
        });

        canvas.addEventListener('mousemove', (e) => {
            if (GameUIState.isDragging) {
                GameUIState.mapOffsetX = e.clientX - GameUIState.dragStartX;
                GameUIState.mapOffsetY = e.clientY - GameUIState.dragStartY;
            }
            // Track mouse position for hover tooltips
            const rect = canvas.getBoundingClientRect();
            galaxyMouseX = (e.clientX - rect.left - GameUIState.mapOffsetX) / GameUIState.mapScale;
            galaxyMouseY = (e.clientY - rect.top - GameUIState.mapOffsetY) / GameUIState.mapScale;
            renderGalaxyMap();
        });

        canvas.addEventListener('mouseup', () => { GameUIState.isDragging = false; });
        canvas.addEventListener('mouseleave', () => { GameUIState.isDragging = false; });

        // Back to sector map button (legacy)
        if (GameUIState.elements.backToSectorMapBtn) {
            GameUIState.elements.backToSectorMapBtn.addEventListener('click', () => {
                showSectorMapView();
            });
        }

        // New card-based navigation buttons
        if (GameUIState.elements.backToSectorsBtn) {
            GameUIState.elements.backToSectorsBtn.addEventListener('click', () => {
                showSectorCardsView();
            });
        }
        // Back from hex grid to subsector cards
        if (GameUIState.elements.backToSubsectorCardsBtn) {
            GameUIState.elements.backToSubsectorCardsBtn.addEventListener('click', () => {
                showSubsectorCardsView(currentSectorKey);
            });
        }
        // Back from system detail to hex grid
        if (GameUIState.elements.backToSubsectorsBtn2) {
            GameUIState.elements.backToSubsectorsBtn2.addEventListener('click', () => {
                // Hide trade button when leaving system detail
                const tradeBtn = document.getElementById('tradeSystemBtn');
                if (tradeBtn) tradeBtn.style.display = 'none';
                if (currentHexGridSubSector) {
                    showSubsectorHexGridView(currentHexGridSubSector);
                } else {
                    showSubsectorCardsView(currentSectorKey);
                }
            });
        }
        // System selector dropdown — switch between systems in the current subsector
        if (GameUIState.elements.systemSelector) {
            GameUIState.elements.systemSelector.addEventListener('change', () => {
                const sysId = parseInt(GameUIState.elements.systemSelector.value);
                const sys = currentSubSectorSystems.find(s => s.id === sysId);
                if (sys && currentSubSectorRef) {
                    GameUIState.currentSystemId = sys.id;
                    renderSystemDetailView(sys, currentSubSectorRef);
                }
            });
        }
        // Trade button in system detail view
        const tradeSystemBtn = document.getElementById('tradeSystemBtn');
        if (tradeSystemBtn) {
            tradeSystemBtn.addEventListener('click', () => {
                if (typeof TradeUI !== 'undefined' && TradeUI.showTradeModal && GameUIState.sessionId && GameUIState.currentSystemId) {
                    TradeUI.showTradeModal(GameUIState.sessionId, GameUIState.currentSystemId);
                } else {
                    GameUI.showToast('Navigate to a system first', 'warning');
                }
            });
        }
        // Show Only Owned toggle — re-render subsector cards when changed
        if (GameUIState.elements.showOnlyOwnedSubsectors) {
            GameUIState.elements.showOnlyOwnedSubsectors.addEventListener('change', () => {
                if (currentSectorKey) {
                    renderSubsectorCardsView(currentSectorKey);
                }
            });
        }

        // Back to subsectors button
        if (GameUIState.elements.backToSubsectorsBtn) {
            GameUIState.elements.backToSubsectorsBtn.addEventListener('click', () => {
                showSubsectorListInView();
            });
        }

        canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const factor = e.deltaY < 0 ? 1.1 : 0.9;
            zoomGalaxy(factor);
        });

        canvas.addEventListener('click', (e) => {
            if (Math.abs(e.clientX - GameUIState.dragStartX - GameUIState.mapOffsetX) > 5 || Math.abs(e.clientY - GameUIState.dragStartY - GameUIState.mapOffsetY) > 5) return;
            // Handle hex click
            const rect = canvas.getBoundingClientRect();
            const x = (e.clientX - rect.left - GameUIState.mapOffsetX) / GameUIState.mapScale;
            const y = (e.clientY - rect.top - GameUIState.mapOffsetY) / GameUIState.mapScale;
            handleHexClick(x, y);
        });

        window.addEventListener('resize', () => {
            resizeCanvas();
            renderGalaxyMap();
        });

        resizeCanvas();
    }

    function resizeCanvas() {
        const canvas = GameUIState.elements.galaxyCanvas;
        if (!canvas) return;
        const parent = canvas.parentElement;
        canvas.width = parent.clientWidth;
        canvas.height = parent.clientHeight;
    }

    function renderGalaxyMap() {
        const canvas = GameUIState.elements.galaxyCanvas;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Clear
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        ctx.save();
        ctx.translate(GameUIState.mapOffsetX, GameUIState.mapOffsetY);
        ctx.scale(GameUIState.mapScale, GameUIState.mapScale);

        // Draw hex grid
        const hexSize = 30;
        const centerX = w / 2 / GameUIState.mapScale - GameUIState.mapOffsetX / GameUIState.mapScale;
        const centerY = h / 2 / GameUIState.mapScale - GameUIState.mapOffsetY / GameUIState.mapScale;
        const radius = 8;

        for (let q = -radius; q <= radius; q++) {
            for (let r = -radius; r <= radius; r++) {
                if (Math.abs(q + r) > radius) continue;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);
                drawHex(ctx, x, y, hexSize, '#1a1535', '#2a2050');
            }
        }

        // Draw subsectors from real game state data
        const subSectors = GameUIState.gameState?.subSectors || [];
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
                
                // Draw subsector name inside the hex
                const subSectorName = subSector.name || '';
                if (subSectorName) {
                    ctx.fillStyle = '#ffffff';
                    ctx.font = 'bold 10px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    // Text stroke for contrast
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 3;
                    // Truncate long names to fit the hex
                    const maxLen = 10;
                    const displayName = subSectorName.length > maxLen ? subSectorName.substring(0, maxLen) + 'Ã¢â‚¬Â¦' : subSectorName;
                    ctx.strokeText(displayName, x, y - 4);
                    ctx.fillStyle = '#ffffff';
                    ctx.fillText(displayName, x, y - 4);
                    ctx.lineWidth = 0.5;
                }

                // Draw system count below the name
                if (systemCount > 0) {
                    ctx.fillStyle = '#fbbf24';
                    ctx.font = 'bold 9px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 3;
                    ctx.strokeText(systemCount.toString(), x, y + 6);
                    ctx.fillStyle = '#fbbf24';
                    ctx.fillText(systemCount.toString(), x, y + 6);
                    ctx.lineWidth = 0.5;
                }

                // Draw trade type label below the hex
                if (subSector.tradeType) {
                    ctx.fillStyle = '#a78bfa';
                    ctx.font = '8px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'top';
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 3;
                    ctx.strokeText(subSector.tradeType, x, y + hexSize + 2);
                    ctx.fillStyle = '#a78bfa';
                    ctx.fillText(subSector.tradeType, x, y + hexSize + 2);
                    ctx.lineWidth = 0.5;
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
        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;
        if (myEmpireId != null && subSectors.length > 0) {
            subSectors.forEach((subSector) => {
                const ownerIds = subSector.ownerEmpireIds;
                if (ownerIds && Array.isArray(ownerIds) && ownerIds.includes(myEmpireId)) {
                    // This subsector has the player's facilities Ã¢â‚¬â€ draw a gold outline
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

        // Draw MDrive range circle for selected fleet
        const allFleets = GameUIState.gameState?.fleets || [];
        if (GameUIState.selectedFleet && allFleets.length > 0) {
            const selectedFleetData = allFleets.find(f => f.id === GameUIState.selectedFleet);
            if (selectedFleetData && selectedFleetData.effectiveMDriveSpeed > 0) {
                const sq = (selectedFleetData.subSectorCol || 5) - 5;
                const sr = (selectedFleetData.subSectorRow || 5) - 5;
                const sx = centerX + hexSize * (3/2 * sq);
                const sy = centerY + hexSize * (Math.sqrt(3)/2 * sq + Math.sqrt(3) * sr);
                const rangeRadius = selectedFleetData.effectiveMDriveSpeed * hexSize * 1.5;

                // Draw range circle with dashed line
                ctx.beginPath();
                ctx.arc(sx, sy, rangeRadius, 0, Math.PI * 2);
                ctx.strokeStyle = 'rgba(0, 212, 255, 0.4)';
                ctx.lineWidth = 2;
                ctx.setLineDash([6, 4]);
                ctx.stroke();
                ctx.setLineDash([]);
                ctx.lineWidth = 0.5;

                // Draw range fill
                ctx.beginPath();
                ctx.arc(sx, sy, rangeRadius, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(0, 212, 255, 0.05)';
                ctx.fill();

                // Draw range label
                ctx.fillStyle = 'rgba(0, 212, 255, 0.7)';
                ctx.font = 'bold 7px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText(`MDrive: ${selectedFleetData.effectiveMDriveSpeed}`, sx, sy - rangeRadius - 6);
            }
        }

        // Draw player fleets from game state at their actual subsector positions
        const fleets = GameUIState.gameState?.fleets || [];
        if (fleets.length > 0) {
            fleets.forEach((fleet) => {
                const q = (fleet.subSectorCol || 5) - 5;
                const r = (fleet.subSectorRow || 5) - 5;
                const fx = centerX + hexSize * (3/2 * q);
                const fy = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Highlight selected fleet
                const isSelected = fleet.id === GameUIState.selectedFleet;
                if (isSelected) {
                    ctx.beginPath();
                    ctx.arc(fx, fy, 14, 0, Math.PI * 2);
                    ctx.strokeStyle = 'rgba(0, 212, 255, 0.6)';
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }

                // Draw fleet triangle
                ctx.beginPath();
                ctx.moveTo(fx, fy - 10);
                ctx.lineTo(fx - 7, fy + 5);
                ctx.lineTo(fx + 7, fy + 5);
                ctx.closePath();
                ctx.fillStyle = isSelected ? '#00d4ff' : '#06b6d4';
                ctx.fill();
                ctx.strokeStyle = '#fff';
                ctx.lineWidth = isSelected ? 2 : 1;
                ctx.stroke();
                ctx.lineWidth = 0.5;

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
        const enemies = GameUIState.gameState?.players?.filter(p => p.id !== getCurrentPlayerId()) || [];
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

        // Draw enemy fleets when fleet data IS available
        if (fleets.length > 0) {
            const myFleetIds = new Set(fleets.filter(f => f.ownerId === getCurrentPlayerId()).map(f => f.id));
            const enemyFleets = fleets.filter(f => !myFleetIds.has(f.id));
            enemyFleets.forEach((fleet) => {
                const q = (fleet.subSectorCol || 5) - 5;
                const r = (fleet.subSectorRow || 5) - 5;
                const fx = centerX + hexSize * (3/2 * q);
                const fy = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Draw enemy fleet triangle (red)
                ctx.beginPath();
                ctx.moveTo(fx, fy - 10);
                ctx.lineTo(fx - 7, fy + 5);
                ctx.lineTo(fx + 7, fy + 5);
                ctx.closePath();
                ctx.fillStyle = '#ef4444';
                ctx.fill();
                ctx.strokeStyle = '#fff';
                ctx.lineWidth = 1;
                ctx.stroke();
                ctx.lineWidth = 0.5;

                // Draw ship count label
                if (fleet.shipCount > 0) {
                    ctx.fillStyle = '#ef4444';
                    ctx.font = 'bold 8px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(fleet.shipCount.toString(), fx, fy + 12);
                }
            });
        }

        // Hover tooltip for subsectors
        GameUIState.hoveredSubSector = null;
        if (subSectors.length > 0) {
            subSectors.forEach((subSector) => {
                const q = subSector.subCol - 5;
                const r = subSector.subRow - 5;
                const x = centerX + hexSize * (3/2 * q);
                const y = centerY + hexSize * (Math.sqrt(3)/2 * q + Math.sqrt(3) * r);

                // Check if mouse is near this hex
                const dx = galaxyMouseX - x;
                const dy = galaxyMouseY - y;
                const dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < hexSize) {
                    GameUIState.hoveredSubSector = subSector;
                    // Highlight the hovered hex
                    drawHex(ctx, x, y, hexSize, 'rgba(255, 255, 255, 0.15)', '#ffffff');
                    ctx.lineWidth = 2;
                    ctx.stroke();
                    ctx.lineWidth = 0.5;
                }
            });
        }

        ctx.restore();

        // Draw hover tooltip (outside transform so it stays at screen coords)
        if (GameUIState.hoveredSubSector) {
            drawSubSectorTooltip(ctx, GameUIState.hoveredSubSector, w, h);
        }

        // Update minimap
        renderMinimap();
    }

    function drawSubSectorTooltip(ctx, subSector, canvasW, canvasH) {
        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;
        const isOwned = subSector.ownerEmpireIds && Array.isArray(subSector.ownerEmpireIds)
            && subSector.ownerEmpireIds.includes(myEmpireId);
        const systemCount = subSector.systemCount || 0;
        const contested = subSector.contested === true;
        const name = subSector.name || `SubSector [${subSector.subRow},${subSector.subCol}]`;

        // Build tooltip lines
        const lines = [];
        lines.push(name);
        lines.push(`Systems: ${systemCount}`);
        if (subSector.tradeType) lines.push(`Trade: ${subSector.tradeType}`);
        if (isOwned) lines.push('Ã°Å¸ÂÂ  Your Territory');
        if (contested) lines.push('Ã¢Å¡â€Ã¯Â¸Â Contested');

        // Measure tooltip size
        ctx.font = 'bold 11px sans-serif';
        let maxWidth = 0;
        lines.forEach(line => {
            const metrics = ctx.measureText(line);
            if (metrics.width > maxWidth) maxWidth = metrics.width;
        });

        const padding = 8;
        const tooltipW = maxWidth + padding * 2;
        const tooltipH = lines.length * 14 + padding * 2;

        // Position tooltip near cursor (in screen coords)
        let tx = galaxyMouseX * GameUIState.mapScale + GameUIState.mapOffsetX + 15;
        let ty = galaxyMouseY * GameUIState.mapScale + GameUIState.mapOffsetY - 10;

        // Keep tooltip within canvas bounds
        if (tx + tooltipW > canvasW - 10) tx = tx - tooltipW - 30;
        if (ty + tooltipH > canvasH - 10) ty = ty - tooltipH;
        if (ty < 10) ty = 10;

        // Draw tooltip background
        ctx.fillStyle = 'rgba(10, 14, 23, 0.95)';
        ctx.strokeStyle = contested ? '#ef4444' : (isOwned ? '#f59e0b' : '#06b6d4');
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.roundRect(tx, ty, tooltipW, tooltipH, 4);
        ctx.fill();
        ctx.stroke();
        ctx.lineWidth = 0.5;

        // Draw tooltip text
        ctx.fillStyle = '#e5e7eb';
        ctx.font = 'bold 11px sans-serif';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'top';
        lines.forEach((line, i) => {
            if (i === 0) {
                ctx.fillStyle = '#06b6d4';
                ctx.font = 'bold 11px sans-serif';
            } else {
                ctx.fillStyle = '#e5e7eb';
                ctx.font = '10px sans-serif';
            }
            ctx.fillText(line, tx + padding, ty + padding + i * 14);
        });
    }

    function renderMinimap() {
        const minimapCanvas = document.getElementById('minimapCanvas');
        if (!minimapCanvas) return;
        const ctx = minimapCanvas.getContext('2d');
        const w = minimapCanvas.width;
        const h = minimapCanvas.height;

        // Clear
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        const subSectors = GameUIState.gameState?.subSectors || [];
        if (subSectors.length === 0) return;

        // Find bounds
        let minCol = Infinity, maxCol = -Infinity, minRow = Infinity, maxRow = -Infinity;
        subSectors.forEach(ss => {
            if (ss.subCol < minCol) minCol = ss.subCol;
            if (ss.subCol > maxCol) maxCol = ss.subCol;
            if (ss.subRow < minRow) minRow = ss.subRow;
            if (ss.subRow > maxRow) maxRow = ss.subRow;
        });

        const rangeCol = maxCol - minCol || 1;
        const rangeRow = maxRow - minRow || 1;
        const padding = 10;
        const drawW = w - padding * 2;
        const drawH = h - padding * 2;

        const myEmpireId = GameUIState.gameState?.empireSummary?.empireId;

        // Draw subsector dots
        subSectors.forEach(ss => {
            const nx = padding + ((ss.subCol - minCol) / rangeCol) * drawW;
            const ny = padding + ((ss.subRow - minRow) / rangeRow) * drawH;
            const isOwned = ss.ownerEmpireIds && Array.isArray(ss.ownerEmpireIds) && ss.ownerEmpireIds.includes(myEmpireId);
            const contested = ss.contested === true;

            ctx.beginPath();
            ctx.arc(nx, ny, 3, 0, Math.PI * 2);
            ctx.fillStyle = contested ? '#ef4444' : (isOwned ? '#f59e0b' : '#3b82f6');
            ctx.fill();
        });

        // Draw fleets
        const fleets = GameUIState.gameState?.fleets || [];
        fleets.forEach(fleet => {
            const col = fleet.subSectorCol || 5;
            const row = fleet.subSectorRow || 5;
            const nx = padding + ((col - minCol) / rangeCol) * drawW;
            const ny = padding + ((row - minRow) / rangeRow) * drawH;

            ctx.beginPath();
            ctx.arc(nx, ny, 2, 0, Math.PI * 2);
            ctx.fillStyle = fleet.ownerId === getCurrentPlayerId() ? '#00d4ff' : '#ef4444';
            ctx.fill();
        });

        // Draw viewport rectangle
        const galaxyCanvas = GameUIState.elements.galaxyCanvas;
        if (galaxyCanvas) {
            const hexSize = 30;
            const centerX = galaxyCanvas.width / 2 / GameUIState.mapScale - GameUIState.mapOffsetX / GameUIState.mapScale;
            const centerY = galaxyCanvas.height / 2 / GameUIState.mapScale - GameUIState.mapOffsetY / GameUIState.mapScale;

            // Calculate visible area in hex coords
            const visibleLeft = (-GameUIState.mapOffsetX / GameUIState.mapScale - centerX) / (hexSize * 1.5) + 5;
            const visibleTop = (-GameUIState.mapOffsetY / GameUIState.mapScale - centerY) / (hexSize * Math.sqrt(3)) + 5;
            const visibleW = (galaxyCanvas.width / GameUIState.mapScale) / (hexSize * 1.5);
            const visibleH = (galaxyCanvas.height / GameUIState.mapScale) / (hexSize * Math.sqrt(3));

            const vx = padding + ((visibleLeft - minCol) / rangeCol) * drawW;
            const vy = padding + ((visibleTop - minRow) / rangeRow) * drawH;
            const vw = (visibleW / rangeCol) * drawW;
            const vh = (visibleH / rangeRow) * drawH;

            ctx.strokeStyle = 'rgba(255, 255, 255, 0.5)';
            ctx.lineWidth = 1;
            ctx.strokeRect(vx, vy, vw, vh);
        }
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
        const centerX = (GameUIState.elements.galaxyCanvas.width / 2 / GameUIState.mapScale - GameUIState.mapOffsetX / GameUIState.mapScale);
        const centerY = (GameUIState.elements.galaxyCanvas.height / 2 / GameUIState.mapScale - GameUIState.mapOffsetY / GameUIState.mapScale);
        
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
        const subSectors = GameUIState.gameState?.subSectors || [];
        const matched = subSectors.find(s => s.subCol === subCol && s.subRow === subRow);
        
        if (matched) {
            // Show sector detail view with this subsector's data
            showSectorDetailView(matched);
        } else {
            // No subsector at this hex, show all subsectors for the sector
            addEventLog('SYSTEM', `Selected hex (${col}, ${row}) - no subsector data`);
            if (GameUIState.elements.targetCoords) {
                GameUIState.elements.targetCoords.value = `${col}, ${row}`;
            }
        }
    }

    // ============================================================
    // SECTOR DETAIL VIEW
    // ============================================================

    function showSectorDetailView(subSector) {
        if (!GameUIState.elements.sectorMapView || !GameUIState.elements.sectorDetailView) return;
        
        // Hide map, show detail
        GameUIState.elements.sectorMapView.classList.add('hidden');
        GameUIState.elements.sectorDetailView.classList.remove('hidden');
        
        // Set title
        const sectorName = subSector.name || `Sector [${subSector.sectorRow},${subSector.sectorCol}]`;
        if (GameUIState.elements.sectorDetailTitle) {
            GameUIState.elements.sectorDetailTitle.textContent = sectorName;
        }
        
        // Show subsector list, hide systems
        if (GameUIState.elements.sectorSubsectors) GameUIState.elements.sectorSubsectors.classList.remove('hidden');
        if (GameUIState.elements.sectorSystems) GameUIState.elements.sectorSystems.classList.add('hidden');
        
        // Find all subsectors in the same sector
        const subSectors = GameUIState.gameState?.subSectors || [];
        const sectorSubSectors = subSectors.filter(s => 
            s.sectorRow === subSector.sectorRow && s.sectorCol === subSector.sectorCol
        );
        
        // Render subsector cards
        if (GameUIState.elements.sectorSubsectors) {
            GameUIState.elements.sectorSubsectors.innerHTML = '';
            
            if (sectorSubSectors.length === 0) {
                GameUIState.elements.sectorSubsectors.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No subsectors in this sector</div>';
                return;
            }
            
            sectorSubSectors.forEach(ss => {
                const card = document.createElement('div');
                card.className = 'sector-subsector-card' + (ss.contested ? ' contested' : '');
                const systemCount = ss.systemCount || 0;
                const contestedIcon = ss.contested ? ' Ã¢Å¡Â Ã¯Â¸Â' : '';
                card.innerHTML = `
                    <div class="sector-subsector-name">Ã°Å¸â€Â¹ ${GameUI.escapeHtml(ss.name || 'SubSector ' + ss.id)}${contestedIcon}</div>
                    <div class="sector-subsector-coords">[${ss.subRow}, ${ss.subCol}]</div>
                    <div class="sector-subsector-systems">Ã¢Â­Â ${systemCount} system(s)</div>
                    ${ss.contested ? '<div class="sector-subsector-contested">Ã¢Å¡â€Ã¯Â¸Â Contested Territory</div>' : ''}
                `;
                card.addEventListener('click', () => {
                    showSubsectorGraphicView(ss);
                });
                GameUIState.elements.sectorSubsectors.appendChild(card);
            });
        }
        
        addEventLog('INFO', `Viewing sector ${sectorName} with ${sectorSubSectors.length} subsectors`);
    }

    function showSystemsInView(subSector) {
        if (!GameUIState.elements.sectorSubsectors || !GameUIState.elements.sectorSystems) return;
        
        // Hide subsector list, show systems
        GameUIState.elements.sectorSubsectors.classList.add('hidden');
        GameUIState.elements.sectorSystems.classList.remove('hidden');
        
        if (GameUIState.elements.systemSubsectorTitle) {
            GameUIState.elements.systemSubsectorTitle.textContent = subSector.name || 'SubSector ' + subSector.id;
        }
        
        // Render systems
        if (GameUIState.elements.systemsList) {
            GameUIState.elements.systemsList.innerHTML = '';
            
            const systems = subSector.systems || [];
            if (systems.length === 0) {
                GameUIState.elements.systemsList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No systems generated yet</div>';
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
                    <div class="system-name">Ã¢Â­Â ${GameUI.escapeHtml(systemName)}</div>
                    <div class="system-details">
                        <div class="system-detail">
                            <span class="system-detail-label">Type:</span> ${GameUI.escapeHtml(systemType)}
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
                                Ã°Å¸ÂªÂ ${GameUI.escapeHtml(planetName)}
                                <span class="planet-type-badge">${GameUI.escapeHtml(planetType)}</span>
                            </div>
                        `;
                    });
                    html += '</div>';
                }
                
                card.innerHTML = html;
                GameUIState.elements.systemsList.appendChild(card);
            });
        }
        
        addEventLog('INFO', `Viewing ${systems.length} systems in ${subSector.name || subSector.id}`);
    }

    // ============================================================
    // SUBSECTOR GRAPHIC VIEW
    // ============================================================

    function showSubsectorGraphicView(subSector) {
        if (!GameUIState.elements.sectorSubsectors || !GameUIState.elements.sectorSystems) return;
        
        // Hide subsector list, show systems/graphic view
        GameUIState.elements.sectorSubsectors.classList.add('hidden');
        GameUIState.elements.sectorSystems.classList.remove('hidden');
        
        if (GameUIState.elements.systemSubsectorTitle) {
            GameUIState.elements.systemSubsectorTitle.textContent = subSector.name || 'SubSector ' + subSector.id;
        }
        
        // Render the subsector graphic view with hex grid
        renderSubsectorGraphic(subSector);
        
        addEventLog('INFO', `Viewing subsector ${subSector.name || subSector.id} graphic`);
    }

    function renderSubsectorGraphic(subSector) {
        const container = GameUIState.elements.systemsList;
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
            <div class="legend-row"><span class="legend-item">Ã¢Â­Â Systems: ${systemCount}</span><span class="legend-item">Ã¢Ëœâ‚¬Ã¯Â¸Â Stars: ${totalStars}</span><span class="legend-item">Ã°Å¸ÂªÂ Planets: ${totalPlanets}</span><span class="legend-item">Ã°Å¸Å’Å’ Bodies: ${totalBodies}</span></div>
            ${subSector.contested ? '<div class="legend-row contested-warning">Ã¢Å¡Â Ã¯Â¸Â Contested Territory Ã¢â‚¬â€ Enemy empires have systems here</div>' : ''}
        `;
        wrapper.appendChild(legend);
        
        container.appendChild(wrapper);
        
        // Draw the subsector hex grid
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;
        
        // Background
        ctx.fillStyle = '#0c0a1a';
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
                drawHexOnCtx(ctx, x, y, hexSize, '#1a1535', '#2a2050');
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
                ctx.fillText('Ã¢Ëœâ‚¬', sx, sy - 5);
            }
            
            // Draw planet icon if has planets
            if (planetCount > 0) {
                ctx.fillStyle = '#10b981';
                ctx.font = 'bold 10px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('Ã¢â€”Â', sx, sy + 6);
            }
            
            // Draw system name label
            const systemName = sys.name || `System ${sys.id || idx + 1}`;
            ctx.fillStyle = '#ffffff';
            ctx.font = 'bold 9px sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'top';
            // Text stroke for contrast against background
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 3;
            ctx.strokeText(systemName, sx, sy + hexSize + 4);
            ctx.fillStyle = '#ffffff';
            ctx.fillText(systemName, sx, sy + hexSize + 4);
        });
        
        // Draw title
        ctx.fillStyle = '#c084fc';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(subSector.name || `SubSector [${subSector.subRow},${subSector.subCol}]`, centerX, 20);
        
        // Draw contested warning if applicable
        if (subSector.contested) {
            ctx.fillStyle = '#ef4444';
            ctx.font = 'bold 11px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('Ã¢Å¡Â Ã¯Â¸Â CONTESTED TERRITORY Ã¢Å¡Â Ã¯Â¸Â', centerX, 40);
        }
        
        // Draw scale indicator
        ctx.fillStyle = '#6b7280';
        ctx.font = '9px sans-serif';
        ctx.textAlign = 'left';
        ctx.fillText(`[${subSector.subRow}, ${subSector.subCol}]`, 10, h - 10);
        
        // Draw ownership info
        if (subSector.ownerEmpireIds && subSector.ownerEmpireIds.size > 0) {
            const empireIds = Array.from(subSector.ownerEmpireIds);
            ctx.fillStyle = '#fcd34d';
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
        if (!GameUIState.elements.sectorMapView || !GameUIState.elements.sectorDetailView) return;
        GameUIState.elements.sectorMapView.classList.remove('hidden');
        GameUIState.elements.sectorDetailView.classList.add('hidden');
        
        // Reset systems view if visible
        if (GameUIState.elements.sectorSubsectors) GameUIState.elements.sectorSubsectors.classList.remove('hidden');
        if (GameUIState.elements.sectorSystems) GameUIState.elements.sectorSystems.classList.add('hidden');
        
        // Re-render the galaxy map
        renderGalaxyMap();
    }

    function showSubsectorListInView() {
        if (!GameUIState.elements.sectorSubsectors || !GameUIState.elements.sectorSystems) return;
        GameUIState.elements.sectorSubsectors.classList.remove('hidden');
        GameUIState.elements.sectorSystems.classList.add('hidden');
    }

    function zoomGalaxy(factor) {
        GameUIState.mapScale = Math.max(0.3, Math.min(3, GameUIState.mapScale * factor));
        renderGalaxyMap();
    }

    function resetGalaxyView() {
        GameUIState.mapScale = 1;
        GameUIState.mapOffsetX = 0;
        GameUIState.mapOffsetY = 0;
        renderGalaxyMap();
    }

    // ============================================================
    // CARD-BASED GALAXY DISPLAY
    // ============================================================
    let currentSectorKey = null; // Tracks which sector's subsectors are being shown
    let currentSubSectorSystems = []; // Systems in the current subsector for the switcher
    let currentSubSectorRef = null; // Reference to current subsector for system switcher
    let currentHexGridSubSector = null; // Reference to the subsector shown in the hex grid view

    // Generate a sector name from row/col (mirrors backend SectorBuilder.generateSectorName)
    function generateSectorName(row, col) {
        const prefixes = ['Alpha', 'Beta', 'Gamma', 'Delta', 'Epsilon', 'Zeta', 'Eta', 'Theta',
                          'Iota', 'Kappa', 'Lambda', 'Mu', 'Nu', 'Xi', 'Omicron', 'Pi',
                          'Rho', 'Sigma', 'Tau', 'Upsilon', 'Phi', 'Chi', 'Psi', 'Omega'];
        const suffixes = ['Prime', 'Nova', 'Major', 'Minor', 'Centauri', 'Vega', 'Rigel', 'Altair',
                          'Deneb', 'Sirius', 'Polaris', 'Antares', 'Betelgeuse', 'Aldebaran',
                          'Capella', 'Arcturus', 'Procyon', 'Achernar', 'Canopus', 'Fomalhaut',
                          'Regulus', 'Castor', 'Pollux', 'Spica'];
        const prefixIdx = (row * 4 + col) % prefixes.length;
        const suffixIdx = (row + col * 3) % suffixes.length;
        return `${prefixes[prefixIdx]}-${suffixes[suffixIdx]}`;
    }

    // Group subsectors by sector key
    function groupSubSectorsBySector(subSectors) {
        const map = {};
        subSectors.forEach(ss => {
            const key = `${ss.sectorRow}-${ss.sectorCol}`;
            if (!map[key]) {
                map[key] = {
                    key: key,
                    sectorRow: ss.sectorRow,
                    sectorCol: ss.sectorCol,
                    name: generateSectorName(ss.sectorRow, ss.sectorCol),
                    subSectors: [],
                    systemCount: 0,
                    contested: false,
                    ownerEmpireIds: new Set(),
                    tradeType: null
                };
            }
            map[key].subSectors.push(ss);
            map[key].systemCount += (ss.systemCount || 0);
            if (ss.contested) map[key].contested = true;
            if (ss.ownerEmpireIds) {
                ss.ownerEmpireIds.forEach(id => map[key].ownerEmpireIds.add(id));
            }
            // Use the trade type from the first subsector (all subsectors in a sector share the same trade type)
            if (!map[key].tradeType && ss.tradeType) {
                map[key].tradeType = ss.tradeType;
            }
        });
        return Object.values(map).sort((a, b) => {
            if (a.sectorRow !== b.sectorRow) return a.sectorRow - b.sectorRow;
            return a.sectorCol - b.sectorCol;
        });
    }

    // Show the sector cards view (top-level galaxy view)
    function showSectorCardsView() {
        if (!GameUIState.elements.sectorCardsView) return;
        GameUIState.elements.sectorCardsView.classList.remove('hidden');
        if (GameUIState.elements.subsectorCardsView) GameUIState.elements.subsectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorHexGridView) GameUIState.elements.subsectorHexGridView.classList.add('hidden');
        if (GameUIState.elements.systemDetailView) GameUIState.elements.systemDetailView.classList.add('hidden');
        currentSectorKey = null;
        currentHexGridSubSector = null;
        // Clear search when returning to sector view
        if (GameUIState.elements.sectorSearchInput) {
            GameUIState.elements.sectorSearchInput.value = '';
            filterSectorCards('');
        }
    }

    // Show the subsector cards view (drill down from a sector)
    function showSubsectorCardsView(sectorKey) {
        if (!GameUIState.elements.subsectorCardsView) return;
        if (GameUIState.elements.sectorCardsView) GameUIState.elements.sectorCardsView.classList.add('hidden');
        GameUIState.elements.subsectorCardsView.classList.remove('hidden');
        if (GameUIState.elements.subsectorHexGridView) GameUIState.elements.subsectorHexGridView.classList.add('hidden');
        if (GameUIState.elements.systemDetailView) GameUIState.elements.systemDetailView.classList.add('hidden');
        currentSectorKey = sectorKey;
        currentHexGridSubSector = null;
        renderSubsectorCardsView(sectorKey);
    }

    // Show the 10x10 hex grid view (drill down from a subsector card)
    function showSubsectorHexGridView(subSector) {
        if (!GameUIState.elements.subsectorHexGridView) return;
        if (GameUIState.elements.sectorCardsView) GameUIState.elements.sectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorCardsView) GameUIState.elements.subsectorCardsView.classList.add('hidden');
        GameUIState.elements.subsectorHexGridView.classList.remove('hidden');
        if (GameUIState.elements.systemDetailView) GameUIState.elements.systemDetailView.classList.add('hidden');
        currentHexGridSubSector = subSector;
        renderSubsectorHexGridView(subSector);
    }

    // Show the system detail view (drill down from hex grid)
    async function showSystemDetailView(system, subSector) {
        if (!GameUIState.elements.systemDetailView) return;
        if (GameUIState.elements.sectorCardsView) GameUIState.elements.sectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorCardsView) GameUIState.elements.subsectorCardsView.classList.add('hidden');
        if (GameUIState.elements.subsectorHexGridView) GameUIState.elements.subsectorHexGridView.classList.add('hidden');
        GameUIState.elements.systemDetailView.classList.remove('hidden');
        // Store the current system ID for trade and other actions
        GameUIState.currentSystemId = system.id;
        // Show/hide trade button based on system availability
        const tradeBtn = document.getElementById('tradeSystemBtn');
        if (tradeBtn) {
            tradeBtn.style.display = 'inline-flex';
        }
        // Store the systems list for the switcher
        currentSubSectorSystems = subSector?.systems || [];
        currentSubSectorRef = subSector;
        // Populate the system selector dropdown
        if (GameUIState.elements.systemSelector && currentSubSectorSystems.length > 1) {
            GameUIState.elements.systemSelector.style.display = 'inline-block';
            GameUIState.elements.systemSelector.innerHTML = '';
            currentSubSectorSystems.forEach(sys => {
                const opt = document.createElement('option');
                opt.value = sys.id;
                opt.textContent = sys.name || `System ${sys.id}`;
                if (sys.id === system.id) opt.selected = true;
                GameUIState.elements.systemSelector.appendChild(opt);
            });
        } else if (GameUIState.elements.systemSelector) {
            GameUIState.elements.systemSelector.style.display = 'none';
        }
        // Fetch full system details from the backend
        try {
            const sessionId = localStorage.getItem('doramcu_session_id');
            if (GameUIState.sessionId) {
                const details = await GameAPI.getSystemDetails(GameUIState.sessionId, system.id);
                if (details) {
                    // Merge fetched details with the summary system data
                    const merged = { ...system, ...details };
                    renderSystemDetailView(merged, subSector);
                    return;
                }
            }
        } catch (e) {
            console.warn('Failed to fetch system details, using summary data:', e.message);
        }
        // Fallback to summary data
        renderSystemDetailView(system, subSector);
    }

    // Render the sector cards grid
    function renderSectorCardsView() {
        if (!GameUIState.elements.sectorCardsGrid || !GameUIState.gameState) return;
        const subSectors = GameUIState.gameState.subSectors || [];
        if (subSectors.length === 0) {
            GameUIState.elements.sectorCardsGrid.innerHTML = '<div class="empty-galaxy">Ã¢ÂÂ³ Galaxy is being generated...</div>';
            if (GameUIState.elements.galaxyViewStats) GameUIState.elements.galaxyViewStats.innerHTML = '';
            return;
        }

        const sectors = groupSubSectorsBySector(subSectors);
        const totalSystems = subSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);

        // Update stats
        if (GameUIState.elements.galaxyViewStats) {
            GameUIState.elements.galaxyViewStats.innerHTML = `
                <span class="galaxy-stat">Ã°Å¸Å’Å’ ${sectors.length} Sectors</span>
                <span class="galaxy-stat">Ã°Å¸â€Â¹ ${subSectors.length} Subsectors</span>
                <span class="galaxy-stat">Ã¢Â­Â ${totalSystems} Systems</span>
            `;
        }

        // Render sector cards
        GameUIState.elements.sectorCardsGrid.innerHTML = '';
        sectors.forEach(sector => {
            const card = document.createElement('div');

            // Determine ownership status with empire names
            let ownershipBadge = '';
            let ownershipClass = '';
            if (sector.ownerEmpireIds.size > 0) {
                const empireIds = Array.from(sector.ownerEmpireIds);
                const playerId = getCurrentPlayerId();
                const empireNames = empireIds.map(id => {
                    const name = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[id]) || `Empire #${id}`;
                    const isPlayer = id === playerId;
                    return { name, isPlayer, id };
                });
                const playerOwns = empireIds.includes(playerId);
                if (playerOwns && empireIds.length === 1) {
                    ownershipBadge = `<span class="ownership-badge owned" title="${GameUI.escapeHtml(empireNames[0].name)}">Ã°Å¸ÂÂ  ${GameUI.escapeHtml(empireNames[0].name)}</span>`;
                    ownershipClass = ' owned';
                } else if (playerOwns) {
                    const names = empireNames.map(e => e.isPlayer ? 'You' : GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge shared" title="${GameUI.escapeHtml(empireNames.map(e => e.name).join(', '))}">Ã°Å¸Â¤Â ${names}</span>`;
                    ownershipClass = ' shared';
                } else {
                    const names = empireNames.map(e => GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge enemy" title="${names}">Ã¢Å¡â€Ã¯Â¸Â ${names}</span>`;
                    ownershipClass = ' enemy';
                }
            } else {
                ownershipBadge = '<span class="ownership-badge neutral">Ã°Å¸Å’â€˜ Unclaimed</span>';
            }

            card.className = 'sector-card' + (sector.contested ? ' contested' : '') + ownershipClass;

            const contestedBadge = sector.contested ? '<span class="contested-badge">Ã¢Å¡Â Ã¯Â¸Â Contested</span>' : '';
            const tradeTypeBadge = sector.tradeType ? `<span class="sector-trade-type">Ã°Å¸â€œÂ¦ ${GameUI.escapeHtml(sector.tradeType)}</span>` : '';

            card.innerHTML = `
                <div class="sector-card-header">
                    <div class="sector-card-name">${GameUI.escapeHtml(sector.name)}</div>
                    ${ownershipBadge}
                </div>
                <div class="sector-card-coords">[${sector.sectorRow}, ${sector.sectorCol}]</div>
                <div class="sector-card-stats">
                    <span class="sector-stat">Ã°Å¸â€Â¹ ${sector.subSectors.length} subsector(s)</span>
                    <span class="sector-stat">Ã¢Â­Â ${sector.systemCount} system(s)</span>
                </div>
                ${tradeTypeBadge}
                ${contestedBadge}
            `;
            // Store data for search filtering
            card.dataset.searchText = (sector.name + ' ' + sector.sectorRow + ' ' + sector.sectorCol + ' ' + (sector.tradeType || '')).toLowerCase();

            card.addEventListener('click', () => {
                showSubsectorCardsView(sector.key);
            });

            GameUIState.elements.sectorCardsGrid.appendChild(card);
        });
    }

    // Filter sector cards by search query
    function filterSectorCards(query) {
        if (!GameUIState.elements.sectorCardsGrid) return;
        const cards = GameUIState.elements.sectorCardsGrid.querySelectorAll('.sector-card');
        const q = query.toLowerCase().trim();
        let visible = 0;
        cards.forEach(card => {
            if (!q || card.dataset.searchText.includes(q)) {
                card.classList.remove('galaxy-card-hidden');
                visible++;
            } else {
                card.classList.add('galaxy-card-hidden');
            }
        });
        if (GameUIState.elements.sectorSearchResults) {
            if (q) {
                GameUIState.elements.sectorSearchResults.textContent = visible + ' of ' + cards.length + ' sectors';
            } else {
                GameUIState.elements.sectorSearchResults.textContent = '';
            }
        }
    }

    // Render the subsector cards grid for a specific sector
    function renderSubsectorCardsView(sectorKey) {
        if (!GameUIState.elements.subsectorCardsGrid || !GameUIState.gameState) return;
        const subSectors = GameUIState.gameState.subSectors || [];

        // Find the sector
        const [rowStr, colStr] = sectorKey.split('-');
        const sectorRow = parseInt(rowStr);
        const sectorCol = parseInt(colStr);
        const sectorSubSectors = subSectors.filter(s =>
            s.sectorRow === sectorRow && s.sectorCol === sectorCol
        );

        // Filter: show only owned subsectors if toggle is checked
        const showOnlyOwned = GameUIState.elements.showOnlyOwnedSubsectors && GameUIState.elements.showOnlyOwnedSubsectors.checked;
        let filteredSubSectors = sectorSubSectors;
        if (showOnlyOwned) {
            const playerId = getCurrentPlayerId();
            filteredSubSectors = sectorSubSectors.filter(ss => {
                const ownerIds = (ss.ownerEmpireIds && ss.ownerEmpireIds.length > 0) ? ss.ownerEmpireIds : [];
                return ownerIds.includes(playerId);
            });
        }

        // Set title
        if (GameUIState.elements.subsectorViewTitle) {
            GameUIState.elements.subsectorViewTitle.textContent = generateSectorName(sectorRow, sectorCol) + ' Ã¢â‚¬â€ Subsectors';
        }

        // Set stats bar
        if (GameUIState.elements.subsectorViewStats) {
            const displaySubSectors = showOnlyOwned && GameUIState.elements.showOnlyOwnedSubsectors.checked ? filteredSubSectors : sectorSubSectors;
            const totalSystems = displaySubSectors.reduce((sum, s) => sum + (s.systemCount || 0), 0);
            const contestedCount = displaySubSectors.filter(s => s.contested).length;
            GameUIState.elements.subsectorViewStats.innerHTML = `
                <span class="galaxy-stat">Ã°Å¸â€Â¹ ${displaySubSectors.length} Subsectors</span>
                <span class="galaxy-stat">Ã¢Â­Â ${totalSystems} Systems</span>
                ${contestedCount > 0 ? '<span class="galaxy-stat">Ã¢Å¡Â Ã¯Â¸Â ' + contestedCount + ' Contested</span>' : ''}
            `;
        }

        if (filteredSubSectors.length === 0) {
            GameUIState.elements.subsectorCardsGrid.innerHTML = '<div class="empty-galaxy">No subsectors in this sector</div>';
            return;
        }

        // Render subsector cards
        GameUIState.elements.subsectorCardsGrid.innerHTML = '';
        filteredSubSectors.forEach(ss => {
            const card = document.createElement('div');
            card.className = 'subsector-card' + (ss.contested ? ' contested' : '');

            // Ownership Ã¢â‚¬â€ use .length for JSON arrays (backend Set serializes to array)
            let ownershipBadge = '';
            const ownerIds = (ss.ownerEmpireIds && ss.ownerEmpireIds.length > 0) ? ss.ownerEmpireIds : [];
            if (ownerIds.length > 0) {
                const playerId = getCurrentPlayerId();
                const empireNames = ownerIds.map(id => {
                    const name = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[id]) || `Empire #${id}`;
                    const isPlayer = id === playerId;
                    return { name, isPlayer, id };
                });
                const playerOwns = ownerIds.includes(playerId);
                if (playerOwns && ownerIds.length === 1) {
                    ownershipBadge = `<span class="ownership-badge owned" title="${GameUI.escapeHtml(empireNames[0].name)}">Ã°Å¸ÂÂ  ${GameUI.escapeHtml(empireNames[0].name)}</span>`;
                } else if (playerOwns) {
                    const names = empireNames.map(e => e.isPlayer ? 'You' : GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge shared" title="${GameUI.escapeHtml(empireNames.map(e => e.name).join(', '))}">Ã°Å¸Â¤Â ${names}</span>`;
                } else {
                    const names = empireNames.map(e => GameUI.escapeHtml(e.name)).join(', ');
                    ownershipBadge = `<span class="ownership-badge enemy" title="${names}">Ã¢Å¡â€Ã¯Â¸Â ${names}</span>`;
                }
            } else {
                ownershipBadge = '<span class="ownership-badge neutral">Ã°Å¸Å’â€˜ Unclaimed</span>';
            }

            const contestedBadge = ss.contested ? '<span class="contested-badge">Ã¢Å¡Â Ã¯Â¸Â Contested</span>' : '';
            const systemCount = ss.systemCount || 0;
            const systems = ss.systems || [];

            // Collect system types for display
            const systemTypes = systems.map(s => s.systemType || 'Unknown');
            const dominantType = systemTypes.length > 0 ? systemTypes[0] : '';
            const systemTypeBadge = dominantType ? `<span class="system-type-badge">${GameUI.escapeHtml(dominantType)}</span>` : '';

            card.innerHTML = `
                <div class="subsector-card-header">
                    <div class="subsector-card-name">Ã°Å¸â€Â¹ ${GameUI.escapeHtml(ss.name || 'SubSector ' + ss.id)}</div>
                    ${ownershipBadge}
                </div>
                <div class="subsector-card-coords">[${ss.subRow}, ${ss.subCol}]</div>
                <div class="subsector-card-stats">
                    <span class="subsector-stat">Ã¢Â­Â ${systemCount} system(s)</span>
                    ${ss.tradeType ? `<span class="subsector-stat">Ã°Å¸â€œÂ¦ ${GameUI.escapeHtml(ss.tradeType)}</span>` : ''}
                </div>
                ${systemTypeBadge}
                ${contestedBadge}
                ${systems.length > 0 ? `
                    <div class="subsector-systems-preview">
                        ${systems.map(sys => {
                            const sysName = sys.name || `System ${sys.id || '?'}`;
                            const planetCount = sys.planetCount || 0;
                            // Determine system ownership from its planets
                            const sysPlanets = sys.planets || [];
                            const playerId = getCurrentPlayerId();
                            let sysOwned = false;
                            let sysContested = false;
                            let sysOwnerName = null;
                            const sysOwnerIds = new Set();
                            sysPlanets.forEach(p => {
                                if (p.ownerEmpireId !== null && p.ownerEmpireId !== undefined) {
                                    sysOwnerIds.add(p.ownerEmpireId);
                                    if (p.ownerEmpireId === playerId) sysOwned = true;
                                    else sysContested = true;
                                }
                            });
                            // Pick the dominant owner name
                            if (sysOwnerIds.size > 0) {
                                const dominantId = Array.from(sysOwnerIds)[0];
                                sysOwnerName = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[dominantId]) || `Empire #${dominantId}`;
                            }
                            let ownerIcon = 'Ã°Å¸Å’â€˜';
                            if (sysOwned && !sysContested) ownerIcon = 'Ã°Å¸ÂÂ ';
                            else if (sysOwned) ownerIcon = 'Ã°Å¸Â¤Â';
                            else if (sysContested) ownerIcon = 'Ã¢Å¡â€Ã¯Â¸Â';
                            const title = sysOwnerName ? `${GameUI.escapeHtml(sysName)} Ã¢â‚¬â€ ${ownerIcon} ${GameUI.escapeHtml(sysOwnerName)}` : GameUI.escapeHtml(sysName);
                            return `<span class="system-preview-item" data-system-id="${sys.id}" title="${title}">${ownerIcon} ${GameUI.escapeHtml(sysName)} (${planetCount})</span>`;
                        }).join('')}
                    </div>
                ` : ''}
            `;

            // Make individual system preview items clickable to open that specific system
            const systemItems = card.querySelectorAll('.system-preview-item');
            systemItems.forEach(item => {
                item.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const sysId = parseInt(item.getAttribute('data-system-id'));
                    const sys = systems.find(s => s.id === sysId);
                    if (sys) {
                        showSystemDetailView(sys, ss);
                    }
                });
                item.style.cursor = 'pointer';
            });

            // Clicking the card itself opens the hex grid view for this subsector
            card.addEventListener('click', () => {
                showSubsectorHexGridView(ss);
            });

            GameUIState.elements.subsectorCardsGrid.appendChild(card);
        });
    }

    // ============================================================
    // 10x10 HEX GRID VIEW Ã¢â‚¬â€ Shows all systems in a subsector
    // ============================================================

    // Render the 10x10 hex grid for a subsector
    function renderSubsectorHexGridView(subSector) {
        const canvas = GameUIState.elements.subsectorHexGridCanvas;
        if (!canvas) return;

        const subSectorName = subSector.name || `SubSector ${subSector.id}`;
        if (GameUIState.elements.subsectorHexGridTitle) {
            GameUIState.elements.subsectorHexGridTitle.textContent = `${subSectorName} Ã¢â‚¬â€ System Map`;
        }
        const systems = subSector.systems || [];
        const systemCount = systems.length;
        const totalPlanets = systems.reduce((sum, s) => sum + (s.planetCount || 0), 0);
        const totalStars = systems.reduce((sum, s) => sum + (s.starCount || 0), 0);
        if (GameUIState.elements.subsectorHexGridStats) {
            GameUIState.elements.subsectorHexGridStats.innerHTML = `
                <span class="system-stat">Ã¢Â­Â ${systemCount} Systems</span>
                <span class="system-stat">Ã¢Ëœâ‚¬Ã¯Â¸Â ${totalStars} Stars</span>
                <span class="system-stat">Ã°Å¸ÂªÂ ${totalPlanets} Planets</span>
            `;
        }

        // Size the canvas
        const container = GameUIState.elements.hexGridContainer;
        if (!container) return;
        const rect = container.getBoundingClientRect();
        canvas.width = Math.max(500, rect.width - 20);
        canvas.height = Math.max(400, rect.height - 20);

        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Background
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        // Starfield
        for (let i = 0; i < 200; i++) {
            const sx = Math.random() * w;
            const sy = Math.random() * h;
            const brightness = Math.random() * 150 + 50;
            ctx.fillStyle = `rgba(${brightness}, ${brightness}, ${brightness + 50}, 0.3)`;
            ctx.fillRect(sx, sy, 1, 1);
        }

        // Draw 10x10 hex grid
        const gridSize = 10;
        const hexSize = Math.min(w, h) / (gridSize * 2.2) + 10;
        const offsetX = (w - gridSize * hexSize * 1.8) / 2;
        const offsetY = (h - gridSize * hexSize * 1.8) / 2;

        // Build a map of systems by position for quick lookup
        const systemByPos = {};
        systems.forEach(sys => {
            const x = sys.posX ?? -1;
            const y = sys.posY ?? -1;
            const z = sys.posZ ?? 0;
            const key = `${x},${y},${z}`;
            if (x >= 0 && y >= 0) {
                systemByPos[key] = sys;
            }
        });

        // Draw hex grid cells
        for (let row = 0; row < gridSize; row++) {
            for (let col = 0; col < gridSize; col++) {
                const x = offsetX + col * hexSize * 1.8 + (row % 2) * hexSize * 0.9;
                const y = offsetY + row * hexSize * 1.6;

                // Check if there's a system at this position (any z level)
                let hasSystem = false;
                let sysAtPos = null;
                for (let z = 0; z < gridSize; z++) {
                    const key = `${col},${row},${z}`;
                    if (systemByPos[key]) {
                        hasSystem = true;
                        sysAtPos = systemByPos[key];
                        break;
                    }
                }

                // Draw hex
                drawHex(ctx, x, y, hexSize * 0.55,
                    hasSystem ? 'rgba(99, 102, 241, 0.3)' : 'rgba(26, 21, 53, 0.5)',
                    hasSystem ? 'rgba(139, 92, 246, 0.6)' : 'rgba(42, 32, 80, 0.3)');

                if (hasSystem && sysAtPos) {
                    // System name on the hex
                    const sysName = sysAtPos.name || `System ${sysAtPos.id}`;
                    ctx.fillStyle = '#ffffff';
                    ctx.font = `bold ${Math.max(8, hexSize * 0.28)}px sans-serif`;
                    ctx.textAlign = 'center';
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 2;
                    ctx.strokeText(sysName, x, y - 2);
                    ctx.fillText(sysName, x, y - 2);

                    // Planet count
                    const pc = sysAtPos.planetCount || 0;
                    const sc = sysAtPos.starCount || 0;
                    ctx.fillStyle = '#a78bfa';
                    ctx.font = `${Math.max(7, hexSize * 0.22)}px sans-serif`;
                    ctx.strokeText(`Ã¢Ëœâ‚¬${sc} Ã°Å¸ÂªÂ${pc}`, x, y + hexSize * 0.2);
                    ctx.fillText(`Ã¢Ëœâ‚¬${sc} Ã°Å¸ÂªÂ${pc}`, x, y + hexSize * 0.2);
                    ctx.lineWidth = 0.5;

                    // Ownership indicator
                    const planets = sysAtPos.planets || [];
                    const playerId = getCurrentPlayerId();
                    let owned = false;
                    let contested = false;
                    let isCapital = false;
                    planets.forEach(p => {
                        if (p.ownerEmpireId !== null && p.ownerEmpireId !== undefined) {
                            if (p.ownerEmpireId === playerId) owned = true;
                            else contested = true;
                            if (p.isCapital) isCapital = true;
                        }
                    });
                    if (isCapital) {
                        // Capital system Ã¢â‚¬â€ pulsing red ring
                        ctx.strokeStyle = '#ef4444';
                        ctx.lineWidth = 3;
                        drawHexStroke(ctx, x, y, hexSize * 0.55);
                        // Inner glow
                        ctx.beginPath();
                        ctx.arc(x, y, hexSize * 0.3, 0, Math.PI * 2);
                        ctx.fillStyle = 'rgba(239, 68, 68, 0.15)';
                        ctx.fill();
                        // Crown icon
                        ctx.fillStyle = '#ef4444';
                        ctx.font = `bold ${Math.max(10, hexSize * 0.35)}px sans-serif`;
                        ctx.textAlign = 'center';
                        ctx.fillText('Ã°Å¸â€˜â€˜', x, y - hexSize * 0.4);
                        ctx.lineWidth = 0.5;
                    } else if (owned) {
                        ctx.strokeStyle = '#fbbf24';
                        ctx.lineWidth = 2;
                        drawHexStroke(ctx, x, y, hexSize * 0.55);
                        ctx.lineWidth = 0.5;
                    } else if (contested) {
                        ctx.strokeStyle = '#ef4444';
                        ctx.lineWidth = 2;
                        drawHexStroke(ctx, x, y, hexSize * 0.55);
                        ctx.lineWidth = 0.5;
                    }

                    // Make hex clickable
                    canvas.onclick = (e) => {
                        const canvasRect = canvas.getBoundingClientRect();
                        const mx = e.clientX - canvasRect.left;
                        const my = e.clientY - canvasRect.top;
                        // Check all hexes for click
                        for (let r = 0; r < gridSize; r++) {
                            for (let c = 0; c < gridSize; c++) {
                                const hx = offsetX + c * hexSize * 1.8 + (r % 2) * hexSize * 0.9;
                                const hy = offsetY + r * hexSize * 1.6;
                                const dist = Math.sqrt((mx - hx) ** 2 + (my - hy) ** 2);
                                if (dist < hexSize * 0.55) {
                                    // Find system at this position
                                    for (let z = 0; z < gridSize; z++) {
                                        const key = `${c},${r},${z}`;
                                        if (systemByPos[key]) {
                                            showSystemDetailView(systemByPos[key], subSector);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    };
                } else {
                    // Empty hex label
                    ctx.fillStyle = 'rgba(100, 100, 140, 0.4)';
                    ctx.font = `${Math.max(7, hexSize * 0.2)}px sans-serif`;
                    ctx.textAlign = 'center';
                    ctx.fillText(`${col},${row}`, x, y + 3);
                }
            }
        }

        // Legend
        if (GameUIState.elements.hexGridLegend) {
            GameUIState.elements.hexGridLegend.innerHTML = `
                <div class="legend-item"><div class="legend-dot" style="background: rgba(99, 102, 241, 0.5);"></div><span>System</span></div>
                <div class="legend-item"><div class="legend-dot" style="border: 3px solid #ef4444;"></div><span>Ã°Å¸â€˜â€˜ Capital</span></div>
                <div class="legend-item"><div class="legend-dot" style="border: 2px solid #fbbf24;"></div><span>Owned</span></div>
                <div class="legend-item"><div class="legend-dot" style="border: 2px solid #ef4444;"></div><span>Contested</span></div>
                <div class="legend-item"><div class="legend-dot" style="background: rgba(26, 21, 53, 0.5);"></div><span>Empty</span></div>
            `;
        }
    }

    // Draw a filled hexagon
    function drawHex(ctx, cx, cy, size, fillColor, strokeColor) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const x = cx + size * Math.cos(angle);
            const y = cy + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.fillStyle = fillColor;
        ctx.fill();
        ctx.strokeStyle = strokeColor;
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    // Draw just the hex stroke outline
    function drawHexStroke(ctx, cx, cy, size) {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 180 * (60 * i - 30);
            const x = cx + size * Math.cos(angle);
            const y = cy + size * Math.sin(angle);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.stroke();
    }

    // Render the system detail view with ALL galactic entities
    function renderSystemDetailView(system, subSector) {
        if (!GameUIState.elements.systemDetailTitle || !GameUIState.elements.systemMapCanvas) return;

        const systemName = system.name || `System ${system.id || '?'}`;
        const systemType = system.systemType || 'Unknown';
        const starCount = system.starCount || 0;
        const planetCount = system.planetCount || 0;
        const bodyCount = system.bodyCount || 0;
        const planets = system.planets || [];
        const stars = system.stars || [];
        const moons = system.moons || [];
        const asteroids = system.asteroids || [];
        const comets = system.comets || [];
        const systemFleets = system.fleets || [];
        const stations = system.stations || [];
        const gasGiants = system.gasGiants || [];
        const asteroidBelts = system.asteroidBelts || [];
        const nebulas = system.nebulas || [];
        const blackHoles = system.blackHoles || [];

        // Use DB enrichment counts when in-memory lists are empty
        const dbMoons = system.dbMoonCount || 0;
        const dbAsteroids = system.dbAsteroidCount || 0;
        const dbComets = system.dbCometCount || 0;
        const dbGasGiants = system.dbGasGiantCount || 0;
        const dbAsteroidBelts = system.dbAsteroidBeltCount || 0;
        const dbNebulas = system.dbNebulaCount || 0;
        const dbBlackHoles = system.dbBlackHoleCount || 0;
        const dbStations = system.dbSpaceStationCount || 0;
        const dbStars = system.dbStarCount || 0;

        // Display counts: use in-memory list lengths if available, otherwise DB counts
        const displayMoons = moons.length > 0 ? moons.length : dbMoons;
        const displayAsteroids = asteroids.length > 0 ? asteroids.length : dbAsteroids;
        const displayComets = comets.length > 0 ? comets.length : dbComets;
        const displayGasGiants = gasGiants.length > 0 ? gasGiants.length : dbGasGiants;
        const displayAsteroidBelts = asteroidBelts.length > 0 ? asteroidBelts.length : dbAsteroidBelts;
        const displayNebulas = nebulas.length > 0 ? nebulas.length : dbNebulas;
        const displayBlackHoles = blackHoles.length > 0 ? blackHoles.length : dbBlackHoles;
        const displayStations = stations.length > 0 ? stations.length : dbStations;
        const displayStars = stars.length > 0 ? stars.length : dbStars;

        // Set title
        GameUIState.elements.systemDetailTitle.textContent = systemName;

        // Set stats
        if (GameUIState.elements.systemDetailStats) {
            GameUIState.elements.systemDetailStats.innerHTML = `
                <span class="system-stat">Ã°Å¸â€œâ€¹ ${GameUI.escapeHtml(systemType)}</span>
                <span class="system-stat">Ã¢Ëœâ‚¬Ã¯Â¸Â ${displayStars} Stars</span>
                <span class="system-stat">Ã°Å¸ÂªÂ ${planetCount} Planets</span>
                <span class="system-stat">Ã°Å¸Å’â€˜ ${displayMoons} Moons</span>
                <span class="system-stat">Ã°Å¸ÂªÂ¨ ${displayAsteroids} Asteroids</span>
                <span class="system-stat">Ã¢Ëœâ€žÃ¯Â¸Â ${displayComets} Comets</span>
                <span class="system-stat">Ã°Å¸Å’â‚¬ ${displayGasGiants} Gas Giants</span>
                <span class="system-stat">Ã°Å¸â€™Â« ${displayAsteroidBelts} Asteroid Belts</span>
                <span class="system-stat">Ã°Å¸Å’Å’ ${displayNebulas} Nebulas</span>
                <span class="system-stat">Ã°Å¸â€¢Â³Ã¯Â¸Â ${displayBlackHoles} Black Holes</span>
                <span class="system-stat">Ã°Å¸Å¡â‚¬ ${systemFleets.length} Fleets</span>
                <span class="system-stat">Ã°Å¸â€ºÂ°Ã¯Â¸Â ${displayStations} Stations</span>
            `;
        }

        // Render all entities in the info panel
        if (GameUIState.elements.systemInfoPanel) {
            let panelHtml = '';

            // Stars
            if (stars.length > 0) {
                panelHtml += '<h3>Ã¢Ëœâ‚¬Ã¯Â¸Â Stars</h3><div class="entity-list">';
                stars.forEach(star => {
                    const name = star.name || `Star ${star.id}`;
                    panelHtml += `<div class="entity-item star-entity" title="Position: (${star.x}, ${star.y}, ${star.z})">
                        <span class="entity-icon">Ã¢Ëœâ‚¬Ã¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${star.x}, ${star.y}, ${star.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Planets
            if (planets.length > 0) {
                panelHtml += '<h3>Ã°Å¸ÂªÂ Planets</h3><div class="entity-list">';
                planets.forEach(planet => {
                    const name = planet.name || `Planet ${planet.id}`;
                    const type = planet.planetType || 'Unknown';
                    const playerId = getCurrentPlayerId();
                    const ownerEmpireId = planet.ownerEmpireId;
                    let ownerIcon = 'Ã°Å¸Å’â€˜';
                    let ownerName = 'Unclaimed';
                    if (ownerEmpireId !== null && ownerEmpireId !== undefined) {
                        ownerName = (GameUIState.gameState.empireNameMap && GameUIState.gameState.empireNameMap[ownerEmpireId]) || `Empire #${ownerEmpireId}`;
                        if (ownerEmpireId === playerId) ownerIcon = 'Ã°Å¸ÂÂ ';
                        else ownerIcon = 'Ã¢Å¡â€Ã¯Â¸Â';
                    }
                    const isCapital = planet.isCapital === true || (ownerEmpireId === playerId && planet.isCapital);
                    const sizeBadge = planet.planetSize ? `<span class="planet-size-badge">${GameUI.escapeHtml(planet.planetSize)}</span>` : '';
                    const mineralBadge = planet.mineralQuality ? `<span class="planet-mineral-badge">${GameUI.escapeHtml(planet.mineralQuality)}</span>` : '';
                    const capitalBadge = isCapital ? '<span class="planet-capital-badge">Ã°Å¸â€˜â€˜ CAPITAL</span>' : '';
                    const ownerBadge = `<span class="planet-owner-badge" title="${GameUI.escapeHtml(ownerName)}">${ownerIcon} ${GameUI.escapeHtml(ownerName)}</span>`;
                    panelHtml += `<div class="entity-item planet-entity" title="Position: (${planet.x}, ${planet.y}, ${planet.z})">
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-type">${GameUI.escapeHtml(type)}</span>
                        <span class="planet-detail-info">
                            ${capitalBadge}${sizeBadge}${mineralBadge}
                        </span>
                        <span class="planet-owner-info">${ownerBadge}</span>
                        <span class="entity-pos">(${planet.x}, ${planet.y}, ${planet.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Moons
            if (moons.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â€˜ Moons</h3><div class="entity-list">';
                moons.forEach(moon => {
                    const name = moon.name || `Moon ${moon.id}`;
                    panelHtml += `<div class="entity-item moon-entity" title="Position: (${moon.x}, ${moon.y}, ${moon.z})">
                        <span class="entity-icon">Ã°Å¸Å’â€˜</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${moon.x}, ${moon.y}, ${moon.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Asteroids
            if (asteroids.length > 0) {
                panelHtml += '<h3>Ã°Å¸ÂªÂ¨ Asteroids</h3><div class="entity-list">';
                asteroids.forEach(ast => {
                    const name = ast.name || `Asteroid ${ast.id}`;
                    panelHtml += `<div class="entity-item asteroid-entity" title="Position: (${ast.x}, ${ast.y}, ${ast.z})">
                        <span class="entity-icon">Ã°Å¸ÂªÂ¨</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${ast.x}, ${ast.y}, ${ast.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Comets
            if (comets.length > 0) {
                panelHtml += '<h3>Ã¢Ëœâ€žÃ¯Â¸Â Comets</h3><div class="entity-list">';
                comets.forEach(comet => {
                    const name = comet.name || `Comet ${comet.id}`;
                    panelHtml += `<div class="entity-item comet-entity" title="Position: (${comet.x}, ${comet.y}, ${comet.z})">
                        <span class="entity-icon">Ã¢Ëœâ€žÃ¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${comet.x}, ${comet.y}, ${comet.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Fleets
            if (systemFleets.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å¡â‚¬ Fleets</h3><div class="entity-list">';
                systemFleets.forEach(fleet => {
                    const name = fleet.name || `Fleet ${fleet.id}`;
                    panelHtml += `<div class="entity-item fleet-entity" title="Position: (${fleet.x}, ${fleet.y}, ${fleet.z})">
                        <span class="entity-icon">Ã°Å¸Å¡â‚¬</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${fleet.x}, ${fleet.y}, ${fleet.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Space Stations
            if (stations.length > 0) {
                panelHtml += '<h3>Ã°Å¸â€ºÂ°Ã¯Â¸Â Space Stations</h3><div class="entity-list">';
                stations.forEach(station => {
                    const name = station.name || `Station ${station.id}`;
                    panelHtml += `<div class="entity-item station-entity" title="Position: (${station.x}, ${station.y}, ${station.z})">
                        <span class="entity-icon">Ã°Å¸â€ºÂ°Ã¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${station.x}, ${station.y}, ${station.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Gas Giants
            if (gasGiants.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â‚¬ Gas Giants</h3><div class="entity-list">';
                gasGiants.forEach(gg => {
                    const name = gg.name || `Gas Giant ${gg.id}`;
                    panelHtml += `<div class="entity-item gas-giant-entity" title="Position: (${gg.x}, ${gg.y}, ${gg.z})">
                        <span class="entity-icon">Ã°Å¸Å’â‚¬</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${gg.x}, ${gg.y}, ${gg.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Asteroid Belts
            if (asteroidBelts.length > 0) {
                panelHtml += '<h3>Ã°Å¸â€™Â« Asteroid Belts</h3><div class="entity-list">';
                asteroidBelts.forEach(belt => {
                    const name = belt.name || `Asteroid Belt ${belt.id}`;
                    panelHtml += `<div class="entity-item asteroid-belt-entity" title="Position: (${belt.x}, ${belt.y}, ${belt.z})">
                        <span class="entity-icon">Ã°Å¸â€™Â«</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${belt.x}, ${belt.y}, ${belt.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Nebulas
            if (nebulas.length > 0) {
                panelHtml += '<h3>Ã°Å¸Å’Å’ Nebulas</h3><div class="entity-list">';
                nebulas.forEach(neb => {
                    const name = neb.name || `Nebula ${neb.id}`;
                    panelHtml += `<div class="entity-item nebula-entity" title="Position: (${neb.x}, ${neb.y}, ${neb.z})">
                        <span class="entity-icon">Ã°Å¸Å’Å’</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${neb.x}, ${neb.y}, ${neb.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Black Holes
            if (blackHoles.length > 0) {
                panelHtml += '<h3>Ã°Å¸â€¢Â³Ã¯Â¸Â Black Holes</h3><div class="entity-list">';
                blackHoles.forEach(bh => {
                    const name = bh.name || `Black Hole ${bh.id}`;
                    panelHtml += `<div class="entity-item black-hole-entity" title="Position: (${bh.x}, ${bh.y}, ${bh.z})">
                        <span class="entity-icon">Ã°Å¸â€¢Â³Ã¯Â¸Â</span>
                        <span class="entity-name">${GameUI.escapeHtml(name)}</span>
                        <span class="entity-pos">(${bh.x}, ${bh.y}, ${bh.z})</span>
                    </div>`;
                });
                panelHtml += '</div>';
            }

            // Show DB enrichment info when in-memory lists are empty
            if (moons.length === 0 && dbMoons > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â€˜ Moons</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸Å’â€˜</span><span class="entity-name">${dbMoons} moon(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (asteroids.length === 0 && dbAsteroids > 0) {
                panelHtml += '<h3>Ã°Å¸ÂªÂ¨ Asteroids</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸ÂªÂ¨</span><span class="entity-name">${dbAsteroids} asteroid(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (comets.length === 0 && dbComets > 0) {
                panelHtml += '<h3>Ã¢Ëœâ€žÃ¯Â¸Â Comets</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã¢Ëœâ€žÃ¯Â¸Â</span><span class="entity-name">${dbComets} comet(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (gasGiants.length === 0 && dbGasGiants > 0) {
                panelHtml += '<h3>Ã°Å¸Å’â‚¬ Gas Giants</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸Å’â‚¬</span><span class="entity-name">${dbGasGiants} gas giant(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (asteroidBelts.length === 0 && dbAsteroidBelts > 0) {
                panelHtml += '<h3>Ã°Å¸â€™Â« Asteroid Belts</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸â€™Â«</span><span class="entity-name">${dbAsteroidBelts} asteroid belt(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (nebulas.length === 0 && dbNebulas > 0) {
                panelHtml += '<h3>Ã°Å¸Å’Å’ Nebulas</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸Å’Å’</span><span class="entity-name">${dbNebulas} nebula(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (blackHoles.length === 0 && dbBlackHoles > 0) {
                panelHtml += '<h3>Ã°Å¸â€¢Â³Ã¯Â¸Â Black Holes</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸â€¢Â³Ã¯Â¸Â</span><span class="entity-name">${dbBlackHoles} black hole(s) in this system</span></div>`;
                panelHtml += '</div>';
            }
            if (stations.length === 0 && dbStations > 0) {
                panelHtml += '<h3>Ã°Å¸â€ºÂ°Ã¯Â¸Â Space Stations</h3><div class="entity-list">';
                panelHtml += `<div class="entity-item" title="Data from database"><span class="entity-icon">Ã°Å¸â€ºÂ°Ã¯Â¸Â</span><span class="entity-name">${dbStations} station(s) in this system</span></div>`;
                panelHtml += '</div>';
            }

            // Empty state
            if (planets.length === 0 && stars.length === 0 && moons.length === 0 &&
                asteroids.length === 0 && comets.length === 0 && systemFleets.length === 0 &&
                stations.length === 0 && gasGiants.length === 0 && asteroidBelts.length === 0 &&
                nebulas.length === 0 && blackHoles.length === 0 &&
                dbMoons === 0 && dbAsteroids === 0 && dbComets === 0 && dbGasGiants === 0 &&
                dbAsteroidBelts === 0 && dbNebulas === 0 && dbBlackHoles === 0 && dbStations === 0) {
                panelHtml = '<div class="empty-list">No entity data for this system</div>';
            }

            GameUIState.elements.systemInfoPanel.innerHTML = panelHtml;
        }

        // Render system map with ALL entities (deferred for layout)
        requestAnimationFrame(() => renderSystemMap(system, subSector));
    }

    // Render the system map canvas showing ALL galactic entities
    function renderSystemMap(system, subSector) {
        const canvas = GameUIState.elements.systemMapCanvas;
        if (!canvas) return;

        const container = canvas.parentElement;
        if (!container) return;

        // Size the canvas to fit the container
        const rect = container.getBoundingClientRect();
        canvas.width = Math.max(400, rect.width - 20);
        canvas.height = Math.max(300, rect.height - 20);

        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        // Background
        ctx.fillStyle = '#0c0a1a';
        ctx.fillRect(0, 0, w, h);

        // Draw starfield
        for (let i = 0; i < 150; i++) {
            const sx = Math.random() * w;
            const sy = Math.random() * h;
            const brightness = Math.random() * 150 + 50;
            ctx.fillStyle = `rgba(${brightness}, ${brightness}, ${brightness + 50}, 0.4)`;
            ctx.fillRect(sx, sy, 1, 1);
        }

        const centerX = w / 2;
        const centerY = h / 2;
        const systemName = system.name || `System ${system.id}`;

        // Collect all entities from the system data
        const stars = system.stars || [];
        const planets = system.planets || [];
        const moons = system.moons || [];
        const asteroids = system.asteroids || [];
        const comets = system.comets || [];
        const systemFleets = system.fleets || [];
        const stations = system.stations || [];
        const gasGiants = system.gasGiants || [];
        const asteroidBelts = system.asteroidBelts || [];
        const nebulas = system.nebulas || [];
        const blackHoles = system.blackHoles || [];

        // Calculate a scale factor based on entity positions
        // Use the max coordinate range to determine scale
        let maxCoord = 1;
        const allEntities = [...stars, ...planets, ...moons, ...asteroids, ...comets, ...systemFleets, ...stations, ...gasGiants, ...asteroidBelts, ...nebulas, ...blackHoles];
        allEntities.forEach(e => {
            if (e.x !== undefined) maxCoord = Math.max(maxCoord, Math.abs(e.x));
            if (e.y !== undefined) maxCoord = Math.max(maxCoord, Math.abs(e.y));
        });
        const scale = Math.min(w, h) / 4 / Math.max(maxCoord, 1);
        const toX = (x) => centerX + x * scale;
        const toY = (y) => centerY + y * scale;

        // Draw orbit rings for visual structure
        const orbitRings = new Set();
        planets.forEach(p => {
            if (p.x !== undefined && p.y !== undefined) {
                const dist = Math.sqrt(p.x * p.x + p.y * p.y);
                if (dist > 0) orbitRings.add(dist);
            }
        });
        orbitRings.forEach(dist => {
            ctx.beginPath();
            ctx.arc(centerX, centerY, dist * scale, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.06)';
            ctx.lineWidth = 0.5;
            ctx.stroke();
        });

        // Draw asteroids (small gray dots)
        asteroids.forEach((ast, idx) => {
            const ax = ast.x !== undefined ? toX(ast.x) : centerX + (idx - asteroids.length / 2) * 15;
            const ay = ast.y !== undefined ? toY(ast.y) : centerY + (idx % 3 - 1) * 15;
            ctx.beginPath();
            ctx.arc(ax, ay, 2, 0, Math.PI * 2);
            ctx.fillStyle = '#9ca3af';
            ctx.fill();
        });

        // Draw comets (small blue dots with tail)
        comets.forEach((comet, idx) => {
            const cx = comet.x !== undefined ? toX(comet.x) : centerX + (idx - comets.length / 2) * 20;
            const cy = comet.y !== undefined ? toY(comet.y) : centerY + (idx % 3 - 1) * 20;
            // Tail
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.lineTo(cx + 8, cy - 4);
            ctx.strokeStyle = 'rgba(96, 165, 250, 0.5)';
            ctx.lineWidth = 1;
            ctx.stroke();
            // Body
            ctx.beginPath();
            ctx.arc(cx, cy, 3, 0, Math.PI * 2);
            ctx.fillStyle = '#60a5fa';
            ctx.fill();
        });

        // Draw stars
        stars.forEach((star, idx) => {
            const sx = star.x !== undefined ? toX(star.x) : centerX;
            const sy = star.y !== undefined ? toY(star.y) : centerY;
            // Star glow
            ctx.beginPath();
            ctx.arc(sx, sy, 20, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(252, 211, 77, 0.1)';
            ctx.fill();
            // Star body
            ctx.beginPath();
            ctx.arc(sx, sy, 10, 0, Math.PI * 2);
            const starGrad = ctx.createRadialGradient(sx, sy, 0, sx, sy, 10);
            starGrad.addColorStop(0, '#fcd34d');
            starGrad.addColorStop(0.5, '#fb923c');
            starGrad.addColorStop(1, 'rgba(251, 146, 60, 0)');
            ctx.fillStyle = starGrad;
            ctx.fill();
            // Star name
            const starName = star.name || `Star ${star.id}`;
            ctx.fillStyle = '#fcd34d';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(starName, sx, sy - 14);
            ctx.fillText(starName, sx, sy - 14);
            ctx.lineWidth = 0.5;
        });

        // Draw planets
        planets.forEach((planet, idx) => {
            const px = planet.x !== undefined ? toX(planet.x) : centerX + Math.cos((idx / Math.max(planets.length, 1)) * Math.PI * 2 - Math.PI / 2) * (50 + idx * 35);
            const py = planet.y !== undefined ? toY(planet.y) : centerY + Math.sin((idx / Math.max(planets.length, 1)) * Math.PI * 2 - Math.PI / 2) * (50 + idx * 35);
            const planetName = planet.name || `Planet ${planet.id}`;
            const planetType = planet.planetType || 'Unknown';

            // Planet color by type
            let planetColor = '#6366f1';
            if (planetType.includes('Desert')) planetColor = '#fb923c';
            else if (planetType.includes('Water')) planetColor = '#3b82f6';
            else if (planetType.includes('Ice')) planetColor = '#22d3ee';
            else if (planetType.includes('Lava')) planetColor = '#f87171';
            else if (planetType.includes('Garden')) planetColor = '#34d399';
            else if (planetType.includes('Gas')) planetColor = '#a78bfa';

            // Planet body
            const planetSize = Math.max(4, Math.min(8, (planet.size || 5)));
            ctx.beginPath();
            ctx.arc(px, py, planetSize, 0, Math.PI * 2);
            ctx.fillStyle = planetColor;
            ctx.fill();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)';
            ctx.lineWidth = 1;
            ctx.stroke();

            // Ownership ring
            if (planet.ownerEmpireId !== null && planet.ownerEmpireId !== undefined) {
                const isPlayer = planet.ownerEmpireId === getCurrentPlayerId();
                ctx.beginPath();
                ctx.arc(px, py, planetSize + 3, 0, Math.PI * 2);
                ctx.strokeStyle = isPlayer ? '#fbbf24' : '#ef4444';
                ctx.lineWidth = 1.5;
                ctx.stroke();
                ctx.lineWidth = 0.5;
            }

            // Planet name
            ctx.fillStyle = '#ffffff';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(planetName, px, py - planetSize - 5);
            ctx.fillText(planetName, px, py - planetSize - 5);
            ctx.lineWidth = 0.5;
        });

        // Draw moons (small circles near their planets)
        moons.forEach((moon, idx) => {
            const mx = moon.x !== undefined ? toX(moon.x) : centerX + (idx - moons.length / 2) * 12;
            const my = moon.y !== undefined ? toY(moon.y) : centerY + (idx % 3 - 1) * 12;
            ctx.beginPath();
            ctx.arc(mx, my, 2.5, 0, Math.PI * 2);
            ctx.fillStyle = '#d1d5db';
            ctx.fill();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)';
            ctx.lineWidth = 0.5;
            ctx.stroke();
        });

        // Draw space stations (diamond shape)
        stations.forEach((station) => {
            const sx = station.x !== undefined ? toX(station.x) : centerX + 40;
            const sy = station.y !== undefined ? toY(station.y) : centerY + 40;
            const size = 5;
            ctx.beginPath();
            ctx.moveTo(sx, sy - size);
            ctx.lineTo(sx + size, sy);
            ctx.lineTo(sx, sy + size);
            ctx.lineTo(sx - size, sy);
            ctx.closePath();
            ctx.fillStyle = '#f59e0b';
            ctx.fill();
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 1;
            ctx.stroke();
            // Station name
            const stationName = station.name || `Station ${station.id}`;
            ctx.fillStyle = '#f59e0b';
            ctx.font = '7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(stationName, sx, sy - size - 4);
            ctx.fillText(stationName, sx, sy - size - 4);
            ctx.lineWidth = 0.5;
        });

        // Draw gas giants (large striped circles)
        gasGiants.forEach((gg) => {
            const gx = gg.x !== undefined ? toX(gg.x) : centerX + 60;
            const gy = gg.y !== undefined ? toY(gg.y) : centerY + 60;
            const ggSize = 12;
            // Glow
            ctx.beginPath();
            ctx.arc(gx, gy, ggSize + 6, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(168, 85, 247, 0.1)';
            ctx.fill();
            // Body
            ctx.beginPath();
            ctx.arc(gx, gy, ggSize, 0, Math.PI * 2);
            const ggGrad = ctx.createRadialGradient(gx, gy, 0, gx, gy, ggSize);
            ggGrad.addColorStop(0, '#c084fc');
            ggGrad.addColorStop(0.5, '#a855f7');
            ggGrad.addColorStop(1, '#7e22ce');
            ctx.fillStyle = ggGrad;
            ctx.fill();
            // Bands
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
            ctx.lineWidth = 1;
            for (let b = -1; b <= 1; b++) {
                ctx.beginPath();
                ctx.ellipse(gx, gy + b * 4, ggSize * 0.8, 2, 0, 0, Math.PI * 2);
                ctx.stroke();
            }
            ctx.lineWidth = 0.5;
            // Name
            const ggName = gg.name || `Gas Giant ${gg.id}`;
            ctx.fillStyle = '#c084fc';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(ggName, gx, gy - ggSize - 5);
            ctx.fillText(ggName, gx, gy - ggSize - 5);
            ctx.lineWidth = 0.5;
        });

        // Draw asteroid belts (ring of small dots)
        asteroidBelts.forEach((belt) => {
            const bx = belt.x !== undefined ? toX(belt.x) : centerX + 80;
            const by = belt.y !== undefined ? toY(belt.y) : centerY + 80;
            const beltRadius = 15;
            // Ring
            ctx.beginPath();
            ctx.ellipse(bx, by, beltRadius, beltRadius * 0.4, 0.3, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(156, 163, 175, 0.4)';
            ctx.lineWidth = 3;
            ctx.stroke();
            // Dots along the ring
            for (let d = 0; d < 12; d++) {
                const angle = (d / 12) * Math.PI * 2;
                const dx = bx + Math.cos(angle + 0.3) * beltRadius;
                const dy = by + Math.sin(angle + 0.3) * beltRadius * 0.4;
                ctx.beginPath();
                ctx.arc(dx, dy, 1.5, 0, Math.PI * 2);
                ctx.fillStyle = '#9ca3af';
                ctx.fill();
            }
            ctx.lineWidth = 0.5;
            // Name
            const beltName = belt.name || `Asteroid Belt ${belt.id}`;
            ctx.fillStyle = '#9ca3af';
            ctx.font = '7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(beltName, bx, by - beltRadius - 5);
            ctx.fillText(beltName, bx, by - beltRadius - 5);
            ctx.lineWidth = 0.5;
        });

        // Draw nebulas (cloud-like shapes)
        nebulas.forEach((neb) => {
            const nx = neb.x !== undefined ? toX(neb.x) : centerX - 70;
            const ny = neb.y !== undefined ? toY(neb.y) : centerY - 70;
            // Cloud layers
            for (let layer = 3; layer >= 0; layer--) {
                ctx.beginPath();
                ctx.arc(nx + layer * 3 - 6, ny + layer * 2 - 4, 18 - layer * 2, 0, Math.PI * 2);
                const nebGrad = ctx.createRadialGradient(nx, ny, 0, nx, ny, 18 - layer * 2);
                nebGrad.addColorStop(0, `rgba(56, 189, 248, ${0.15 - layer * 0.03})`);
                nebGrad.addColorStop(1, 'rgba(56, 189, 248, 0)');
                ctx.fillStyle = nebGrad;
                ctx.fill();
            }
            // Name
            const nebName = neb.name || `Nebula ${neb.id}`;
            ctx.fillStyle = '#38bdf8';
            ctx.font = '7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(nebName, nx, ny - 20);
            ctx.fillText(nebName, nx, ny - 20);
            ctx.lineWidth = 0.5;
        });

        // Draw black holes (dark circles with accretion disk)
        blackHoles.forEach((bh) => {
            const bhx = bh.x !== undefined ? toX(bh.x) : centerX - 90;
            const bhy = bh.y !== undefined ? toY(bh.y) : centerY + 90;
            // Accretion disk
            ctx.beginPath();
            ctx.ellipse(bhx, bhy, 20, 6, 0.5, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(251, 146, 60, 0.5)';
            ctx.lineWidth = 2;
            ctx.stroke();
            // Event horizon
            ctx.beginPath();
            ctx.arc(bhx, bhy, 8, 0, Math.PI * 2);
            ctx.fillStyle = '#000000';
            ctx.fill();
            ctx.strokeStyle = 'rgba(251, 146, 60, 0.8)';
            ctx.lineWidth = 1.5;
            ctx.stroke();
            ctx.lineWidth = 0.5;
            // Name
            const bhName = bh.name || `Black Hole ${bh.id}`;
            ctx.fillStyle = '#fb923c';
            ctx.font = 'bold 7px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(bhName, bhx, bhy - 14);
            ctx.fillText(bhName, bhx, bhy - 14);
            ctx.lineWidth = 0.5;
        });

        // Draw fleets with MDrive range
        systemFleets.forEach((fleet, idx) => {
            const fx = fleet.x !== undefined ? toX(fleet.x) : centerX + Math.cos((idx / Math.max(systemFleets.length, 1)) * Math.PI * 2) * 60;
            const fy = fleet.y !== undefined ? toY(fleet.y) : centerY + Math.sin((idx / Math.max(systemFleets.length, 1)) * Math.PI * 2) * 60;

            // Fleet marker (triangle)
            ctx.save();
            ctx.translate(fx, fy);
            ctx.beginPath();
            ctx.moveTo(0, -7);
            ctx.lineTo(-5, 5);
            ctx.lineTo(5, 5);
            ctx.closePath();
            ctx.fillStyle = '#34d399';
            ctx.fill();
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 1.5;
            ctx.stroke();
            ctx.restore();

            // Fleet name
            const fleetName = fleet.name || `Fleet ${fleet.id}`;
            ctx.fillStyle = '#34d399';
            ctx.font = 'bold 8px sans-serif';
            ctx.textAlign = 'center';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 2;
            ctx.strokeText(fleetName, fx, fy + 14);
            ctx.fillText(fleetName, fx, fy + 14);
            ctx.lineWidth = 0.5;
        });

        // Draw system name
        ctx.fillStyle = '#c084fc';
        ctx.font = 'bold 14px sans-serif';
        ctx.textAlign = 'center';
        ctx.strokeStyle = '#000000';
        ctx.lineWidth = 3;
        ctx.strokeText(systemName, centerX, 20);
        ctx.fillStyle = '#c084fc';
        ctx.fillText(systemName, centerX, 20);
        ctx.lineWidth = 0.5;

        // Draw legend
        if (GameUIState.elements.systemMapLegend) {
            let legendHtml = '';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #fcd34d;"></div><span>Star</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #6366f1;"></div><span>Planet</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #d1d5db;"></div><span>Moon</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #9ca3af;"></div><span>Asteroid</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #60a5fa;"></div><span>Comet</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #34d399;"></div><span>Fleet</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="background: #f59e0b;"></div><span>Station</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="border: 2px solid #fbbf24;"></div><span>Owned</span></div>';
            legendHtml += '<div class="legend-item"><div class="legend-dot" style="border: 2px solid #ef4444;"></div><span>Enemy</span></div>';
            GameUIState.elements.systemMapLegend.innerHTML = legendHtml;
        }
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
            success: 'Ã¢Å“â€¦',
            error: 'Ã¢ÂÅ’',
            warning: 'Ã¢Å¡Â Ã¯Â¸Â',
            info: 'Ã¢â€žÂ¹Ã¯Â¸Â'
        };

        const toast = document.createElement('div');
        toast.className = `toast ${type || 'info'}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[type] || 'Ã¢â€žÂ¹Ã¯Â¸Â'}</span>
            <div class="toast-content">
                <div class="toast-message">${GameUI.escapeHtml(message)}</div>
            </div>
            <button class="toast-close">&times;</button>
            <div class="toast-progress"></div>
        `;

        toast.querySelector('.toast-close').addEventListener('click', () => {
            GameUI.removeToast(toast);
        });

        container.appendChild(toast);

        setTimeout(() => {
            GameUI.removeToast(toast);
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
            GameUIState.commandBarVisible = true;
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
            GameUIState.commandBarVisible = false;
        }
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
    // Command Bar Setup (Ctrl+K)
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
            GameUIState.commandBarSelectedIndex = 0;
        });

        input.addEventListener('keydown', (e) => {
            const visibleItems = Array.from(results.querySelectorAll('.command-bar-item'))
                .filter(item => item.style.display !== 'none');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                GameUIState.commandBarSelectedIndex = Math.min(GameUIState.commandBarSelectedIndex + 1, visibleItems.length - 1);
                updateCommandBarSelection(visibleItems);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                GameUIState.commandBarSelectedIndex = Math.max(GameUIState.commandBarSelectedIndex - 1, 0);
                updateCommandBarSelection(visibleItems);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                const selected = visibleItems[GameUIState.commandBarSelectedIndex];
                if (selected) {
                    executeCommand(selected.dataset.action);
                    GameUI.hideCommandBar();
                }
            } else if (e.key === 'Escape') {
                GameUI.hideCommandBar();
            }
        });

        results.addEventListener('click', (e) => {
            const item = e.target.closest('.command-bar-item');
            if (item) {
                executeCommand(item.dataset.action);
                GameUI.hideCommandBar();
            }
        });

        commandBar.addEventListener('click', (e) => {
            if (e.target === commandBar) {
                GameUI.hideCommandBar();
            }
        });
    }

    function updateCommandBarSelection(items) {
        items.forEach((item, i) => {
            item.classList.toggle('selected', i === GameUIState.commandBarSelectedIndex);
        });
        if (items[GameUIState.commandBarSelectedIndex]) {
            items[GameUIState.commandBarSelectedIndex].scrollIntoView({ block: 'nearest' });
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
            case 'develop-character':
                if (typeof CharacterUI !== 'undefined' && CharacterUI.showCharacterModal) {
                    CharacterUI.showCharacterModal();
                }
                break;
            case 'trade-market':
                if (typeof TradeUI !== 'undefined' && TradeUI.showTradeModal && GameUIState.sessionId && GameUIState.currentSystemId) {
                    TradeUI.showTradeModal(GameUIState.sessionId, GameUIState.currentSystemId);
                } else {
                    GameUI.showToast('Navigate to a system first', 'warning');
                }
                break;
        }
    }

    // --------------------------------------------------------
    // Keyboard Shortcuts
    // --------------------------------------------------------
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                if (e.key === 'Escape' && GameUIState.commandBarVisible) {
                    GameUI.hideCommandBar();
                    return;
                }
                return;
            }

            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                if (GameUIState.commandBarVisible) {
                    GameUI.hideCommandBar();
                } else {
                    GameUI.showCommandBar();
                }
                return;
            }

            if (e.key === 'Escape') {
                if (GameUIState.commandBarVisible) {
                    GameUI.hideCommandBar();
                    return;
                }
                const modals = document.querySelectorAll('.modal-overlay:not(.hidden)');
                if (modals.length > 0) {
                    modals[modals.length - 1].classList.add('hidden');
                    return;
                }
                return;
            }

            if (e.key === 'Enter' && !GameUIState.commandBarVisible) {
                const modals = document.querySelectorAll('.modal-overlay:not(.hidden)');
                if (modals.length === 0) {
                    const advanceBtn = document.getElementById('advanceTurnBtn');
                    if (advanceBtn && !advanceBtn.disabled) {
                        advanceBtn.click();
                    }
                }
                return;
            }

            if (e.key === 'b' || e.key === 'B') {
                const buildBtn = document.getElementById('buildShipBtn');
                if (buildBtn && !buildBtn.disabled) buildBtn.click();
                return;
            }

            if (e.key === 'm' || e.key === 'M') {
                const moveBtn = document.getElementById('moveFleetBtn');
                if (moveBtn && !moveBtn.disabled) moveBtn.click();
                return;
            }

            if (e.key === 'c' || e.key === 'C') {
                const combatBtn = document.getElementById('engageCombatBtn');
                if (combatBtn && !combatBtn.disabled) combatBtn.click();
                return;
            }

            if (e.key === '+' || e.key === '=') {
                const zoomInBtn = document.getElementById('zoomInBtn');
                if (zoomInBtn) zoomInBtn.click();
                return;
            }

            if (e.key === '-') {
                const zoomOutBtn = document.getElementById('zoomOutBtn');
                if (zoomOutBtn) zoomOutBtn.click();
                return;
            }

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
            const ratioX = (e.clientX - rect.left) / rect.width;
            const ratioY = (e.clientY - rect.top) / rect.height;

            const galaxyCanvas = document.getElementById('galaxyCanvas');
            if (!galaxyCanvas) return;

            const targetX = -(ratioX * galaxyCanvas.width - galaxyCanvas.width / 2);
            const targetY = -(ratioY * galaxyCanvas.height - galaxyCanvas.height / 2);

            const startX = window._mapOffsetX || 0;
            const startY = window._mapOffsetY || 0;
            const duration = 300;
            const startTime = performance.now();

            function animatePan(time) {
                const elapsed = time - startTime;
                const progress = Math.min(1, elapsed / duration);
                const ease = 1 - Math.pow(1 - progress, 3);

                window._mapOffsetX = startX + (targetX - startX) * ease;
                window._mapOffsetY = startY + (targetY - startY) * ease;

                mapOffsetXDirect(targetX * ease + startX * (1 - ease));
                mapOffsetYDirect(targetY * ease + startY * (1 - ease));

                if (progress < 1) {
                    requestAnimationFrame(animatePan);
                }
            }
            requestAnimationFrame(animatePan);
        });
    }

    function mapOffsetXDirect(value) {
        const canvas = document.getElementById('galaxyCanvas');
        if (canvas) canvas.dataset.mapOffsetX = value;
    }

    function mapOffsetYDirect(value) {
        const canvas = document.getElementById('galaxyCanvas');
        if (canvas) canvas.dataset.mapOffsetY = value;
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

        changeEl.style.transform = 'scale(1.3)';
        changeEl.style.opacity = '1';
        setTimeout(() => {
            changeEl.style.transform = 'scale(1)';
        }, 300);
    }

    // ============================================================
    // BATTLE MANAGEMENT (called from battle card onclick handlers)
    // ============================================================

    async function reinforceBattle(battleId) {
        if (!GameUIState.sessionId) return;
        // Find a fleet to reinforce with
        const fleetId = GameUIState.selectedFleet || (GameUIState.gameState?.fleets?.[0]?.id);
        if (!fleetId) {
            GameUI.showToast('Select a fleet first to reinforce with', 'error');
            return;
        }
        try {
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/battles/' + battleId + '/reinforce',
                { fleetId });
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.showToast('Battle reinforced!', 'success');
            // Refresh battle modal
            handleViewBattlesClick();
        } catch (err) {
            GameUI.showToast('Failed to reinforce: ' + err.message, 'error');
        }
    }

    async function retreatBattle(battleId) {
        if (!GameUIState.sessionId) return;
        try {
            const response = await GameAPI.post('/api/v1/sessions/' + GameUIState.sessionId + '/battles/' + battleId + '/retreat');
            GameUIState.gameState = response;
            updateGameStateDisplay();
            GameUI.showToast('Retreated from battle!', 'success');
            // Refresh battle modal
            handleViewBattlesClick();
        } catch (err) {
            GameUI.showToast('Failed to retreat: ' + err.message, 'error');
        }
    }

    // Expose public API
    GameUI.loadGameState = loadGameState;
    GameUI.updateGameStateDisplay = updateGameStateDisplay;
    GameUI.addEventLog = addEventLog;
    GameUI.showLogin = showLogin;
    GameUI.showSessionSelection = showSessionSelection;
    GameUI.showGame = showGame;
    GameUI.cacheElements = cacheElements;
    GameUI.bindEvents = bindEvents;
    GameUI.checkAuth = checkAuth;
    GameUI.setupSSEListeners = setupSSEListeners;
    GameUI.setupPanelCollapse = setupPanelCollapse;
    GameUI.setupCommandBar = setupCommandBar;
    GameUI.setupKeyboardShortcuts = setupKeyboardShortcuts;
    GameUI.setupEventLogFilter = setupEventLogFilter;
    GameUI.setupMinimap = setupMinimap;
    GameUI.reinforceBattle = reinforceBattle;
    GameUI.retreatBattle = retreatBattle;
    GameUI.checkGalaxyProgress = checkGalaxyProgress;
    GameUI.showGalaxyCreationModal = showGalaxyCreationModal;
    GameUI.hideGalaxyCreationModal = hideGalaxyCreationModal;
    GameUI.updateGalaxyProgress = updateGalaxyProgress;
    GameUI.addGalaxyLog = addGalaxyLog;
    GameUI.updateGalaxyStatus = updateGalaxyStatus;

    GameUI.init = function() {
        GameUI.cacheElements();
        GameUI.bindEvents();
        GameUI.checkAuth();
        GameUI.setupSSEListeners();
        GameUI.setupPanelCollapse();
        GameUI.setupCommandBar();
        GameUI.setupKeyboardShortcuts();
        GameUI.setupEventLogFilter();
        GameUI.setupMinimap();
    };
})();

if (typeof GameUI !== 'undefined' && GameUI.init) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', GameUI.init);
    } else {
        GameUI.init();
    }
}