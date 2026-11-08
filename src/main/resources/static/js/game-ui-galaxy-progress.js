/**
 * DoraMCU Game UI - Galaxy Progress Module
 * Handles galaxy creation progress SSE monitoring.
 */
(function() {
    'use strict';

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
                updateGalaxyStatus('🌌 Generating galaxy...');
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
                updateGalaxyStatus('✅ Galaxy generation complete!');
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

    // Register on GameUI
    GameUI.checkGalaxyProgress = checkGalaxyProgress;
    GameUI.showGalaxyCreationModal = showGalaxyCreationModal;
    GameUI.hideGalaxyCreationModal = hideGalaxyCreationModal;
    GameUI.updateGalaxyProgress = updateGalaxyProgress;
    GameUI.addGalaxyLog = addGalaxyLog;
    GameUI.updateGalaxyStatus = updateGalaxyStatus;
})();
