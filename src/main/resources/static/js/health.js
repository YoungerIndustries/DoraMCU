/* DoraMCU - Health Dashboard Logic
 *
 * Module structure:
 *   - Utilities (formatBytes, formatUptime, setStatus)
 *   - Dashboard Loading
 *   - Auto-refresh
 *   - Start Game
 */

const HealthDashboard = (function() {
    let autoRefreshInterval = null;
    let refreshSeconds = 10;

    function init() {
        setupEventListeners();
        toggleAutoRefresh();
        loadDashboard();
    }

    function setupEventListeners() {
        const refreshBtn = document.getElementById('autoRefreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', toggleAutoRefresh);
        }

        const startGameBtn = document.getElementById('startGameBtn');
        if (startGameBtn) {
            startGameBtn.addEventListener('click', startGame);
        }
    }

    // ============================================================
    // Utilities
    // ============================================================
    function formatBytes(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    }

    function formatUptime(ms) {
        const seconds = Math.floor(ms / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);
        if (days > 0) return days + 'd ' + (hours % 24) + 'h';
        if (hours > 0) return hours + 'h ' + (minutes % 60) + 'm';
        if (minutes > 0) return minutes + 'm ' + (seconds % 60) + 's';
        return seconds + 's';
    }

    function setStatus(elementId, status) {
        const el = document.getElementById(elementId);
        if (!el) return;
        const upper = (status || '').toUpperCase();
        el.textContent = upper;
        el.className = 'status-badge';
        if (upper === 'UP' || upper === 'ACTIVE' || upper === 'RUNNING' || upper === 'TRUE') {
            el.classList.add('status-up');
        } else if (upper === 'DOWN' || upper === 'ERROR' || upper === 'FALSE') {
            el.classList.add('status-down');
        } else {
            el.classList.add('status-warning');
        }
    }

    // ============================================================
    // Auto-refresh
    // ============================================================
    function toggleAutoRefresh() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
            autoRefreshInterval = null;
            const label = document.getElementById('refreshLabel');
            if (label) label.textContent = 'OFF';
        } else {
            autoRefreshInterval = setInterval(loadDashboard, refreshSeconds * 1000);
            const label = document.getElementById('refreshLabel');
            if (label) label.textContent = refreshSeconds + 's';
        }
    }

    // ============================================================
    // Dashboard Loading
    // ============================================================
    async function loadDashboard() {
        try {
            const response = await fetch('/api/v1/health/dashboard');
            if (!response.ok) throw new Error('HTTP ' + response.status);
            const data = await response.json();
            updateDashboard(data);
        } catch (err) {
            console.error('Failed to load dashboard:', err);
            const dot = document.getElementById('overallStatus');
            const label = document.getElementById('overallLabel');
            if (dot) dot.className = 'status-dot offline';
            if (label) label.textContent = 'Connection failed';
        }
    }

    function updateDashboard(data) {
        // App info
        const appVersion = document.getElementById('appVersion');
        const appUptime = document.getElementById('appUptime');
        const lastUpdated = document.getElementById('lastUpdated');
        if (appVersion) appVersion.textContent = data.appVersion || '-';
        if (appUptime) appUptime.textContent = formatUptime(data.uptime || 0);
        if (lastUpdated) lastUpdated.textContent = new Date().toLocaleTimeString();

        // Overall status
        const allUp = data.components && data.components.every(c => c.status === 'UP');
        const overallDot = document.getElementById('overallStatus');
        const overallLabel = document.getElementById('overallLabel');
        if (allUp) {
            if (overallDot) overallDot.className = 'status-dot';
            if (overallLabel) overallLabel.textContent = 'All Systems Operational';
        } else {
            if (overallDot) overallDot.className = 'status-dot offline';
            if (overallLabel) overallLabel.textContent = 'Some Systems Degraded';
        }

        // JVM
        if (data.jvm) {
            const heapPct = data.jvm.heapPercent || 0;
            const heapUsage = document.getElementById('heapUsage');
            const heapBar = document.getElementById('heapBar');
            if (heapUsage) heapUsage.textContent =
                formatBytes(data.jvm.heapUsed) + ' / ' + formatBytes(data.jvm.heapMax) + ' (' + heapPct + '%)';
            if (heapBar) {
                heapBar.style.width = heapPct + '%';
                if (heapPct > 90) {
                    heapBar.style.background = 'var(--accent-red)';
                } else if (heapPct > 70) {
                    heapBar.style.background = 'var(--accent-orange)';
                } else {
                    heapBar.style.background = 'var(--accent-green)';
                }
            }
            const threadCount = document.getElementById('threadCount');
            if (threadCount) threadCount.textContent = data.jvm.threadCount;
            const nonHeapUsage = document.getElementById('nonHeapUsage');
            if (nonHeapUsage) nonHeapUsage.textContent = formatBytes(data.jvm.nonHeapUsed);
            setStatus('jvmStatus', 'UP');
        }

        // OS
        if (data.os) {
            const osName = document.getElementById('osName');
            const osArch = document.getElementById('osArch');
            const osProcessors = document.getElementById('osProcessors');
            const osLoad = document.getElementById('osLoad');
            if (osName) osName.textContent = data.os.name || '-';
            if (osArch) osArch.textContent = data.os.arch || '-';
            if (osProcessors) osProcessors.textContent = data.os.processors || '-';
            if (osLoad) osLoad.textContent =
                data.os.loadAverage != null ? data.os.loadAverage.toFixed(2) : '-';
        }

        // Components (Database, Ollama, etc.)
        if (data.components) {
            for (const comp of data.components) {
                const name = comp.name.toLowerCase();
                if (name.includes('database')) {
                    setStatus('dbStatus', comp.status);
                    if (comp.details) {
                        const dbType = document.getElementById('dbType');
                        const dbVersion = document.getElementById('dbVersion');
                        const dbConnection = document.getElementById('dbConnection');
                        if (dbType) dbType.textContent = comp.details.database || '-';
                        if (dbVersion) dbVersion.textContent = comp.details.version || '-';
                        if (dbConnection) dbConnection.textContent = comp.details.connection || '-';
                    }
                } else if (name.includes('ollama')) {
                    setStatus('ollamaStatus', comp.status);
                    if (comp.details) {
                        const ollamaService = document.getElementById('ollamaService');
                        const ollamaConnection = document.getElementById('ollamaConnection');
                        if (ollamaService) ollamaService.textContent = comp.details.service || '-';
                        if (ollamaConnection) ollamaConnection.textContent = comp.details.status || '-';
                    }
                }
            }
        }

        // Python Server
        if (data.pythonServer) {
            setStatus('pythonStatus', data.pythonServer.status);
            const pythonPort = document.getElementById('pythonPort');
            const pythonUrl = document.getElementById('pythonUrl');
            const pythonError = document.getElementById('pythonError');
            if (pythonPort) pythonPort.textContent = data.pythonServer.port || '-';
            if (pythonUrl) pythonUrl.textContent = data.pythonServer.url || '-';
            if (pythonError) pythonError.textContent =
                data.pythonServer.error ? data.pythonServer.error : 'None';
        }

        // Game Sessions
        if (data.gameSessions) {
            const activeSessions = document.getElementById('activeSessions');
            const aiPlayers = document.getElementById('aiPlayers');
            const currentTurn = document.getElementById('currentTurn');
            if (activeSessions) activeSessions.textContent = data.gameSessions.activeSessions || 0;
            if (aiPlayers) aiPlayers.textContent = data.gameSessions.aiPlayers || 0;
            if (currentTurn) currentTurn.textContent = data.gameSessions.currentTurn || '-';
            setStatus('gameStatus', (data.gameSessions.activeSessions > 0) ? 'UP' : 'DOWN');
        }

        // Show/hide Start Game overlay based on gameStarted flag
        const overlay = document.getElementById('startGameOverlay');
        if (overlay) {
            if (data.gameStarted) {
                overlay.innerHTML = `
                    <div class="start-game-card">
                        <svg class="icon start-game-icon"><use href="#icon-check"></use></svg>
                        <h2>Game Running</h2>
                        <p>The galaxy is active with ${data.gameSessions?.activeSessions || 0} session(s) and ${data.gameSessions?.aiPlayers || 0} AI players.</p>
                        <button class="btn-start-game" onclick="window.location.href='/index.html'">
                            <svg class="icon"><use href="#icon-rocket"></use></svg>
                            Go to Game
                        </button>
                    </div>
                `;
                overlay.style.display = 'flex';
            } else {
                overlay.style.display = 'flex';
            }
        }

        // Turn Scheduler
        if (data.turnScheduler) {
            setStatus('schedulerStatus', data.turnScheduler.running ? 'UP' : 'DOWN');
            const schedulerRunning = document.getElementById('schedulerRunning');
            const schedulerInterval = document.getElementById('schedulerInterval');
            if (schedulerRunning) schedulerRunning.textContent =
                data.turnScheduler.running ? 'Yes' : 'No';
            if (schedulerInterval) schedulerInterval.textContent =
                (data.turnScheduler.intervalSeconds || '-') + 's';
        }

        // DL Circuit Breaker
        if (data.dlCircuitBreaker) {
            setStatus('dlStatus', data.dlCircuitBreaker.status);
            const dlStatusText = document.getElementById('dlStatusText');
            const dlNote = document.getElementById('dlNote');
            if (dlStatusText) dlStatusText.textContent = data.dlCircuitBreaker.status || '-';
            if (dlNote) dlNote.textContent = data.dlCircuitBreaker.note || '-';
        }

        // Junie Agent
        if (data.junieAgent) {
            setStatus('junieStatus', data.junieAgent.status);
            const junieAgent = document.getElementById('junieAgent');
            const junieModel = document.getElementById('junieModel');
            const junieProvider = document.getElementById('junieProvider');
            const junieCli = document.getElementById('junieCli');
            const junieSessions = document.getElementById('junieSessions');
            if (junieAgent) junieAgent.textContent = data.junieAgent.agent || '-';
            if (junieModel) junieModel.textContent = data.junieAgent.model || '-';
            if (junieProvider) junieProvider.textContent = data.junieAgent.provider || '-';
            if (junieCli) junieCli.textContent =
                data.junieAgent.cliRunning ? 'Yes' : 'No';
            const sessions = data.junieAgent.sessionFiles != null ? data.junieAgent.sessionFiles : '-';
            if (junieSessions) junieSessions.textContent = sessions;
        }
    }

    // ============================================================
    // Start Game
    // ============================================================
    async function startGame() {
        const btn = document.getElementById('startGameBtn');
        const status = document.getElementById('startGameStatus');
        if (!btn || !status) return;

        btn.disabled = true;
        btn.innerHTML = '<svg class="icon"><use href="#icon-refresh"></use></svg> Starting...';
        status.textContent = 'Generating galaxy and creating game session...';
        status.className = 'start-game-status start-game-loading';

        try {
            const response = await fetch('/api/v1/health/start-game', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            const result = await response.json();

            if (result.success) {
                status.textContent = 'Game started! Redirecting to login...';
                status.className = 'start-game-status start-game-success';
                btn.innerHTML = '<svg class="icon"><use href="#icon-check"></use></svg> Game Started';
                setTimeout(() => window.location.href = '/index.html', 1000);
            } else {
                status.textContent = result.message || 'Failed to start game.';
                status.className = 'start-game-status start-game-error';
                btn.disabled = false;
                btn.innerHTML = '<svg class="icon"><use href="#icon-rocket"></use></svg> Start Game';
            }
        } catch (err) {
            status.textContent = 'Error: ' + err.message;
            status.className = 'start-game-status start-game-error';
            btn.disabled = false;
            btn.innerHTML = '<svg class="icon"><use href="#icon-rocket"></use></svg> Start Game';
        }
    }

    // ============================================================
    // PUBLIC API
    // ============================================================
    return {
        init
    };
})();

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', HealthDashboard.init);
} else {
    HealthDashboard.init();
}
