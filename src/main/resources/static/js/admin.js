/* DoraMCU - Admin Panel Logic
 *
 * Module structure:
 *   - Modal System
 *   - Authentication & Login
 *   - Server Status
 *   - Device Management
 *   - User Management
 *   - Server Configuration
 *   - Galaxy Management
 *   - Turn Management
 *   - System Logs
 *   - Utilities
 */

const AdminPanel = (function() {
    // Authentication state
    let authToken = null;
    let loggedUsername = null;

    // Data stores
    let devices = [];
    let users = [];

    // Auto-refresh interval
    let statusRefreshInterval = null;

    function init() {
        setupModalCloseButtons();
        setupCardClickHandlers();
        setupFormHandlers();
        setupServerStatusObserver();
        checkAuthOnLoad();
    }

    // ============================================================
    // Modal System
    // ============================================================
    function openModal(id) {
        const modal = document.getElementById(id);
        if (!modal) return;
        const existingBackdrop = document.getElementById(id + '-backdrop');
        if (existingBackdrop) existingBackdrop.remove();

        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop';
        backdrop.id = id + '-backdrop';
        modal.classList.add('show');
        document.body.appendChild(backdrop);
        document.body.style.overflow = 'hidden';
        backdrop.addEventListener('click', () => closeModal(id));
    }

    function closeModal(id) {
        const modal = document.getElementById(id);
        if (!modal) return;
        const backdrop = document.getElementById(id + '-backdrop');
        modal.classList.remove('show');
        if (backdrop) backdrop.remove();
        document.body.style.overflow = '';
    }

    function setupModalCloseButtons() {
        document.addEventListener('click', (e) => {
            const closeBtn = e.target.closest('[data-modal-close]');
            if (closeBtn) {
                const modalId = closeBtn.dataset.modalClose;
                closeModal(modalId);
            }
        });
    }

    // ============================================================
    // Card Click Handlers (event delegation)
    // ============================================================
    function setupCardClickHandlers() {
        document.addEventListener('click', (e) => {
            const card = e.target.closest('[data-admin-card]');
            if (card) {
                const action = card.dataset.adminCard;
                switch (action) {
                    case 'server-status': openServerStatus(); break;
                    case 'device-management': openDeviceManagement(); break;
                    case 'user-management': openUserManagement(); break;
                    case 'server-config': openServerConfig(); break;
                    case 'galaxy-management': openGalaxyManagement(); break;
                    case 'turn-management': openTurnManagement(); break;
                    case 'logs-viewer': openLogsViewer(); break;
                }
            }

            // Button handlers
            const btn = e.target.closest('[data-admin-action]');
            if (btn) {
                const action = btn.dataset.adminAction;
                const param = btn.dataset.adminParam;
                handleAdminAction(action, param);
            }
        });
    }

    function handleAdminAction(action, param) {
        switch (action) {
            case 'logout': handleLogout(); break;
            case 'add-device': showAddDeviceForm(); break;
            case 'hide-device-form': hideDeviceForm(); break;
            case 'edit-device': editDevice(parseInt(param)); break;
            case 'delete-device': deleteDevice(parseInt(param)); break;
            case 'add-user': showAddUserForm(); break;
            case 'hide-user-form': hideUserForm(); break;
            case 'delete-user': deleteUser(parseInt(param)); break;
            case 'update-log-level': updateLogLevel(); break;
            case 'rebuild-galaxy': rebuildGalaxy(param); break;
            case 'rebuild-all-galaxies': rebuildAllGalaxies(); break;
            case 'refresh-turn': loadTurnStatus(); break;
            case 'toggle-scheduler': toggleScheduler(param === 'true'); break;
            case 'set-turn-interval': setTurnInterval(); break;
            case 'advance-turn': advanceTurn(param); break;
            case 'refresh-logs': refreshLogs(); break;
        }
    }

    // ============================================================
    // Form Handlers
    // ============================================================
    function setupFormHandlers() {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', handleLoginSubmit);
        }

        const deviceForm = document.getElementById('deviceForm');
        if (deviceForm) {
            deviceForm.addEventListener('submit', handleDeviceFormSubmit);
        }

        const userForm = document.getElementById('userForm');
        if (userForm) {
            userForm.addEventListener('submit', handleUserFormSubmit);
        }

        const logLinesSelect = document.getElementById('logLines');
        if (logLinesSelect) {
            logLinesSelect.addEventListener('change', refreshLogs);
        }
    }

    // ============================================================
    // Authentication & Login
    // ============================================================
    function showLoginGate() {
        const gate = document.getElementById('loginGate');
        const dashboard = document.getElementById('adminDashboard');
        if (gate) gate.style.display = 'flex';
        if (dashboard) dashboard.style.display = 'none';
    }

    function showDashboard(username) {
        const gate = document.getElementById('loginGate');
        const dashboard = document.getElementById('adminDashboard');
        if (gate) gate.style.display = 'none';
        if (dashboard) dashboard.style.display = 'block';
        if (username) {
            const userEl = document.getElementById('loggedInUser');
            if (userEl) userEl.textContent = 'Logged in as: ' + username;
        }
    }

    async function checkAdminRights(token) {
        try {
            const response = await fetch('/api/v1/admin/check-admin', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const data = await response.json();
                return data.isAdmin === true;
            }
            return false;
        } catch (e) {
            return false;
        }
    }

    async function checkAuthOnLoad() {
        // Try unified token first, then legacy authToken
        const storedToken = localStorage.getItem('token') || localStorage.getItem('authToken');
        const storedUsername = localStorage.getItem('username') || localStorage.getItem('adminUsername');

        if (storedToken) {
            const hasAdminRights = await checkAdminRights(storedToken);
            if (hasAdminRights) {
                authToken = storedToken;
                loggedUsername = storedUsername || 'Admin';
                showDashboard(loggedUsername);
                return;
            } else {
                localStorage.removeItem('token');
                localStorage.removeItem('authToken');
                localStorage.removeItem('username');
                localStorage.removeItem('adminUsername');
            }
        }
        showLoginGate();
    }

    function handleLogout() {
        authToken = null;
        loggedUsername = null;
        localStorage.removeItem('token');
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        localStorage.removeItem('adminUsername');
        showLoginGate();
        const usernameInput = document.getElementById('loginUsername');
        const passwordInput = document.getElementById('loginPassword');
        const errorEl = document.getElementById('loginError');
        if (usernameInput) usernameInput.value = '';
        if (passwordInput) passwordInput.value = '';
        if (errorEl) errorEl.style.display = 'none';
        showToast('Logged out successfully', 'info');
    }

    async function handleLoginSubmit(e) {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;
        const errorEl = document.getElementById('loginError');

        try {
            const response = await fetch('/api/v1/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                authToken = data.token;
                loggedUsername = data.username || username;
                // Store in unified keys
                localStorage.setItem('token', authToken);
                localStorage.setItem('username', loggedUsername);

                const hasAdminRights = await checkAdminRights(authToken);
                if (hasAdminRights || data.isAdmin) {
                    showDashboard(loggedUsername);
                    showToast('Login successful! Welcome, ' + loggedUsername, 'success');
                } else {
                    localStorage.removeItem('token');
                    localStorage.removeItem('username');
                    authToken = null;
                    loggedUsername = null;
                    if (errorEl) {
                        errorEl.textContent = 'Access denied: Admin rights required to view this panel.';
                        errorEl.style.display = 'block';
                    }
                }
            } else {
                let errorMsg = 'Login failed. Please check your credentials.';
                try {
                    const error = await response.json();
                    errorMsg = error.message || errorMsg;
                } catch (_) {}
                if (errorEl) {
                    errorEl.textContent = errorMsg;
                    errorEl.style.display = 'block';
                }
            }
        } catch (error) {
            if (errorEl) {
                errorEl.textContent = 'Network error. Could not connect to server.';
                errorEl.style.display = 'block';
            }
        }
    }

    function getAuthHeaders() {
        if (!authToken) {
            showLoginGate();
            throw new Error('Not authenticated');
        }
        return { 'Authorization': `Bearer ${authToken}` };
    }

    // ============================================================
    // Toast Notification
    // ============================================================
    function showToast(message, type = 'success') {
        const toastEl = document.getElementById('toastNotification');
        const toastBody = document.getElementById('toastMessage');
        if (!toastEl || !toastBody) return;
        toastEl.className = `toast show bg-${type}`;
        toastBody.textContent = message;
        setTimeout(() => toastEl.classList.remove('show'), 3000);
    }

    // ============================================================
    // Server Status
    // ============================================================
    async function openServerStatus() {
        openModal('serverStatusModal');
        await loadServerStatus();
    }

    async function loadServerStatus() {
        try {
            const response = await fetch('/api/v1/admin/server/status', { headers: getAuthHeaders() });
            const data = await response.json();

            const memoryPercent = parseFloat(data.memory.heapPercent);
            const memoryUsedMB = (data.memory.heapUsed / 1024 / 1024).toFixed(1);
            const memoryMaxMB = (data.memory.heapMax / 1024 / 1024).toFixed(1);

            document.getElementById('serverStatusContent').innerHTML = `
                <div class="row">
                    <div class="col-md-6">
                        <h6><svg class="icon"><use href="#icon-clock"></use></svg> Uptime</h6>
                        <p class="lead">${data.uptime}</p>
                    </div>
                    <div class="col-md-6">
                        <h6><svg class="icon"><use href="#icon-robot"></use></svg> CPU Cores</h6>
                        <p class="lead">${data.cpuCores} cores (Load: ${data.loadAverage.toFixed(2)})</p>
                    </div>
                </div>
                <div class="row mt-3">
                    <div class="col-12">
                        <h6><svg class="icon"><use href="#icon-robot"></use></svg> Memory Usage</h6>
                        <div class="d-flex justify-content-between mb-2">
                            <span>Heap: ${memoryUsedMB} MB / ${memoryMaxMB} MB</span>
                            <span>${memoryPercent}%</span>
                        </div>
                        <div class="progress memory-bar">
                            <div class="progress-bar bg-primary" role="progressbar" 
                                 style="width: ${memoryPercent}%">${memoryPercent}%</div>
                        </div>
                    </div>
                </div>
                <div class="row mt-3">
                    <div class="col-md-4">
                        <div class="stat-card">
                            <div class="stat-value">${data.serverPort}</div>
                            <div class="stat-label">Server Port</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="stat-card">
                            <div class="stat-value">${data.javaVersion}</div>
                            <div class="stat-label">Java Version</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="stat-card">
                            <div class="stat-value">${data.userCount}</div>
                            <div class="stat-label">Users</div>
                        </div>
                    </div>
                </div>
                <div class="row mt-3">
                    <div class="col-12">
                        <div class="stat-card">
                            <div class="stat-value">${data.osName}</div>
                            <div class="stat-label">Operating System ${data.osVersion}</div>
                        </div>
                    </div>
                </div>
            `;
        } catch (error) {
            document.getElementById('serverStatusContent').innerHTML = `
                <div class="alert alert-danger">Failed to load server status. Please login first.</div>
            `;
        }
    }

    function setupServerStatusObserver() {
        const serverStatusModal = document.getElementById('serverStatusModal');
        if (!serverStatusModal) return;
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.attributeName === 'class') {
                    if (serverStatusModal.classList.contains('show')) {
                        statusRefreshInterval = setInterval(loadServerStatus, 30000);
                    } else {
                        clearInterval(statusRefreshInterval);
                        statusRefreshInterval = null;
                    }
                }
            });
        });
        observer.observe(serverStatusModal, { attributes: true });
    }

    // ============================================================
    // Device Management
    // ============================================================
    async function openDeviceManagement() {
        openModal('deviceManagementModal');
        await loadDevices();
    }

    async function loadDevices() {
        try {
            const response = await fetch('/api/v1/devices', { headers: getAuthHeaders() });
            devices = await response.json();
            renderDevicesTable();
        } catch (error) {
            document.getElementById('devicesTableContainer').innerHTML = `
                <div class="alert alert-danger">Failed to load devices. Please login first.</div>
            `;
        }
    }

    function renderDevicesTable() {
        if (devices.length === 0) {
            document.getElementById('devicesTableContainer').innerHTML = `
                <div class="text-center py-4 text-muted">No devices found. Add your first device!</div>
            `;
            return;
        }

        const statusColors = {
            'active': 'success',
            'inactive': 'secondary',
            'maintenance': 'warning'
        };

        let html = `
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Status</th>
                        <th>Location</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
        `;

        devices.forEach(device => {
            const color = statusColors[device.status] || 'secondary';
            html += `
                <tr>
                    <td>${device.id}</td>
                    <td>${device.name}</td>
                    <td><span class="badge bg-${color}">${device.status}</span></td>
                    <td>${device.location || '-'}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-1" data-admin-action="edit-device" data-admin-param="${device.id}">
                            <svg class="icon"><use href="#icon-wrench"></use></svg>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" data-admin-action="delete-device" data-admin-param="${device.id}">
                            <svg class="icon"><use href="#icon-x"></use></svg>
                        </button>
                    </td>
                </tr>
            `;
        });

        html += '</tbody></table>';
        document.getElementById('devicesTableContainer').innerHTML = html;
    }

    function showAddDeviceForm() {
        document.getElementById('deviceFormContainer').style.display = 'block';
        document.getElementById('deviceFormTitle').textContent = 'Add New Device';
        document.getElementById('deviceForm').reset();
        document.getElementById('deviceId').value = '';
    }

    function hideDeviceForm() {
        document.getElementById('deviceFormContainer').style.display = 'none';
    }

    function editDevice(id) {
        const device = devices.find(d => d.id === id);
        if (!device) return;

        document.getElementById('deviceFormContainer').style.display = 'block';
        document.getElementById('deviceFormTitle').textContent = 'Edit Device';
        document.getElementById('deviceId').value = device.id;
        document.getElementById('deviceName').value = device.name;
        document.getElementById('deviceStatus').value = device.status;
        document.getElementById('deviceLocation').value = device.location || '';
    }

    async function handleDeviceFormSubmit(e) {
        e.preventDefault();
        const id = document.getElementById('deviceId').value;
        const deviceData = {
            name: document.getElementById('deviceName').value,
            status: document.getElementById('deviceStatus').value,
            location: document.getElementById('deviceLocation').value
        };

        try {
            let response;
            if (id) {
                response = await fetch(`/api/v1/devices/${id}`, {
                    method: 'PUT',
                    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                    body: JSON.stringify(deviceData)
                });
            } else {
                response = await fetch('/api/v1/devices', {
                    method: 'POST',
                    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                    body: JSON.stringify(deviceData)
                });
            }

            if (response.ok) {
                showToast(id ? 'Device updated!' : 'Device created!', 'success');
                hideDeviceForm();
                await loadDevices();
            }
        } catch (error) {
            showToast('Failed to save device', 'danger');
        }
    }

    async function deleteDevice(id) {
        if (!confirm('Are you sure you want to delete this device?')) return;

        try {
            const response = await fetch(`/api/v1/devices/${id}`, {
                method: 'DELETE',
                headers: getAuthHeaders()
            });

            if (response.ok) {
                showToast('Device deleted!', 'success');
                await loadDevices();
            }
        } catch (error) {
            showToast('Failed to delete device', 'danger');
        }
    }

    // ============================================================
    // User Management
    // ============================================================
    async function openUserManagement() {
        openModal('userManagementModal');
        await loadUsers();
    }

    async function loadUsers() {
        try {
            const response = await fetch('/api/v1/admin/users', { headers: getAuthHeaders() });
            users = await response.json();
            renderUsersTable();
        } catch (error) {
            document.getElementById('usersTableContainer').innerHTML = `
                <div class="alert alert-danger">Failed to load users. Please login first.</div>
            `;
        }
    }

    function renderUsersTable() {
        if (users.length === 0) {
            document.getElementById('usersTableContainer').innerHTML = `
                <div class="text-center py-4 text-muted">No users found. Add your first user!</div>
            `;
            return;
        }

        let html = `
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
        `;

        users.forEach(user => {
            html += `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-danger" data-admin-action="delete-user" data-admin-param="${user.id}">
                            <svg class="icon"><use href="#icon-x"></use></svg> Delete
                        </button>
                    </td>
                </tr>
            `;
        });

        html += '</tbody></table>';
        document.getElementById('usersTableContainer').innerHTML = html;
    }

    function showAddUserForm() {
        document.getElementById('userFormContainer').style.display = 'block';
        document.getElementById('userForm').reset();
    }

    function hideUserForm() {
        document.getElementById('userFormContainer').style.display = 'none';
    }

    async function handleUserFormSubmit(e) {
        e.preventDefault();
        const userData = {
            username: document.getElementById('username').value,
            password: document.getElementById('password').value
        };

        try {
            const response = await fetch('/api/v1/admin/users', {
                method: 'POST',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify(userData)
            });

            const data = await response.json();
            if (response.ok) {
                showToast('User created!', 'success');
                hideUserForm();
                await loadUsers();
            } else {
                showToast(data.error || 'Failed to create user', 'danger');
            }
        } catch (error) {
            showToast('Failed to create user', 'danger');
        }
    }

    async function deleteUser(id) {
        if (!confirm('Are you sure you want to delete this user?')) return;

        try {
            const response = await fetch(`/api/v1/admin/users/${id}`, {
                method: 'DELETE',
                headers: getAuthHeaders()
            });

            if (response.ok) {
                showToast('User deleted!', 'success');
                await loadUsers();
            }
        } catch (error) {
            showToast('Failed to delete user', 'danger');
        }
    }

    // ============================================================
    // Server Configuration
    // ============================================================
    async function openServerConfig() {
        openModal('serverConfigModal');
        await loadServerConfig();
    }

    async function loadServerConfig() {
        try {
            const response = await fetch('/api/v1/admin/config', { headers: getAuthHeaders() });
            const config = await response.json();

            document.getElementById('serverConfigContent').innerHTML = `
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-robot"></use></svg> Application Name</div>
                    <div class="config-value">${config.appName}</div>
                </div>
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-star"></use></svg> Version</div>
                    <div class="config-value">${config.appVersion}</div>
                </div>
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-antenna"></use></svg> Server Port</div>
                    <div class="config-value">${config.serverPort}</div>
                </div>
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-robot"></use></svg> Database</div>
                    <div class="config-value">${config.databaseUrl}</div>
                </div>
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-lightning"></use></svg> Cache Type</div>
                    <div class="config-value">${config.cacheType}</div>
                </div>
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-info"></use></svg> Log File</div>
                    <div class="config-value">${config.logFileName}</div>
                </div>
                <div class="config-item">
                    <div class="config-label"><svg class="icon"><use href="#icon-antenna"></use></svg> Logging Level</div>
                    <div class="d-flex align-items-center gap-2">
                        <select class="form-select form-select-sm w-auto" id="loggingLevel">
                            <option value="TRACE" ${config.loggingLevel === 'TRACE' ? 'selected' : ''}>TRACE</option>
                            <option value="DEBUG" ${config.loggingLevel === 'DEBUG' ? 'selected' : ''}>DEBUG</option>
                            <option value="INFO" ${config.loggingLevel === 'INFO' ? 'selected' : ''}>INFO</option>
                            <option value="WARN" ${config.loggingLevel === 'WARN' ? 'selected' : ''}>WARN</option>
                            <option value="ERROR" ${config.loggingLevel === 'ERROR' ? 'selected' : ''}>ERROR</option>
                        </select>
                        <button class="btn btn-sm btn-info text-white" data-admin-action="update-log-level">Update</button>
                    </div>
                </div>
            `;
        } catch (error) {
            document.getElementById('serverConfigContent').innerHTML = `
                <div class="alert alert-danger">Failed to load configuration. Please login first.</div>
            `;
        }
    }

    async function updateLogLevel() {
        const level = document.getElementById('loggingLevel').value;
        try {
            const response = await fetch('/api/v1/admin/config/logging-level', {
                method: 'PUT',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify({ level })
            });

            const data = await response.json();
            if (response.ok) {
                showToast(data.message, 'success');
            } else {
                showToast(data.error || 'Failed to update log level', 'danger');
            }
        } catch (error) {
            showToast('Failed to update log level', 'danger');
        }
    }

    // ============================================================
    // Galaxy Management
    // ============================================================
    async function openGalaxyManagement() {
        openModal('galaxyManagementModal');
        await loadGalaxies();
    }

    async function loadGalaxies() {
        try {
            const response = await fetch('/api/v1/admin/galaxies', { headers: getAuthHeaders() });
            const galaxies = await response.json();

            if (galaxies && galaxies.length > 0) {
                let html = '<div class="alert alert-info"><svg class="icon"><use href="#icon-info"></use></svg> Galaxies are generated once at startup and persisted in the database. Use the buttons below to rebuild them.</div>';
                html += '<div class="table-responsive"><table class="table table-striped"><thead><tr><th>Name</th><th>Sectors</th><th>Solar Systems</th><th>Hyperlane Density</th><th>Actions</th></tr></thead><tbody>';
                galaxies.forEach(g => {
                    html += `<tr>
                        <td><strong>${escapeHtml(g.name)}</strong></td>
                        <td>${g.totalSectors}</td>
                        <td>${g.solarSystems}</td>
                        <td>${g.hyperlaneDensity}</td>
                        <td><button class="btn btn-sm btn-danger" data-admin-action="rebuild-galaxy" data-admin-param="${escapeHtml(g.name)}">Rebuild</button></td>
                    </tr>`;
                });
                html += '</tbody></table></div>';
                html += '<div class="mt-3"><button class="btn btn-danger" data-admin-action="rebuild-all-galaxies">Rebuild All Galaxies</button></div>';
                document.getElementById('galaxyManagementContent').innerHTML = html;
            } else {
                document.getElementById('galaxyManagementContent').innerHTML = `
                    <div class="text-center text-muted py-4">
                        <p>No galaxies found. Galaxies will be generated automatically at startup.</p>
                    </div>
                `;
            }
        } catch (error) {
            document.getElementById('galaxyManagementContent').innerHTML = `
                <div class="alert alert-danger">Failed to load galaxies. Please login first.</div>
            `;
        }
    }

    async function rebuildGalaxy(name) {
        if (!confirm(`Are you sure you want to rebuild galaxy "${name}"? This will delete and regenerate all data.`)) {
            return;
        }
        try {
            const response = await fetch('/api/v1/admin/galaxies/rebuild', {
                method: 'POST',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify({ galaxies: [name] })
            });
            const data = await response.json();
            if (response.ok) {
                showToast(data.message, 'success');
                await loadGalaxies();
            } else {
                showToast(data.error || 'Failed to rebuild galaxy', 'danger');
            }
        } catch (error) {
            showToast('Failed to rebuild galaxy', 'danger');
        }
    }

    async function rebuildAllGalaxies() {
        if (!confirm('Are you sure you want to rebuild ALL galaxies? This will delete and regenerate all galaxy data.')) {
            return;
        }
        try {
            const response = await fetch('/api/v1/admin/galaxies/rebuild', {
                method: 'POST',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify({})
            });
            const data = await response.json();
            if (response.ok) {
                showToast(data.message, 'success');
                await loadGalaxies();
            } else {
                showToast(data.error || 'Failed to rebuild galaxies', 'danger');
            }
        } catch (error) {
            showToast('Failed to rebuild galaxies', 'danger');
        }
    }

    // ============================================================
    // Turn Management
    // ============================================================
    async function openTurnManagement() {
        openModal('turnManagementModal');
        await loadTurnStatus();
    }

    async function loadTurnStatus() {
        try {
            const response = await fetch('/api/v1/admin/turn/status', { headers: getAuthHeaders() });
            const data = await response.json();

            let html = '';

            html += '<div class="row mb-4">';
            html += '<div class="col-md-6">';
            html += '<div class="card">';
            html += '<div class="card-body">';
            html += '<h6 class="card-title">Scheduler Controls</h6>';
            html += `<p class="mb-2">Status: <span class="badge ${data.enabled ? 'bg-success' : 'bg-secondary'}">${data.enabled ? 'Running' : 'Stopped'}</span></p>`;
            html += `<p class="mb-3">Turn Interval: <strong>${data.turnIntervalSeconds}</strong> seconds</p>`;
            html += '<div class="d-flex gap-2 flex-wrap">';
            html += `<button class="btn btn-sm ${data.enabled ? 'btn-warning' : 'btn-success'}" data-admin-action="toggle-scheduler" data-admin-param="${!data.enabled}">${data.enabled ? 'Stop Scheduler' : 'Start Scheduler'}</button>`;
            html += '</div>';
            html += '</div></div></div>';

            html += '<div class="col-md-6">';
            html += '<div class="card">';
            html += '<div class="card-body">';
            html += '<h6 class="card-title">Set Turn Interval</h6>';
            html += '<div class="input-group">';
            html += '<input type="number" class="form-control form-control-sm" id="turnIntervalInput" min="1" max="3600" value="' + data.turnIntervalSeconds + '" placeholder="Seconds">';
            html += '<button class="btn btn-sm btn-outline-primary" data-admin-action="set-turn-interval">Apply</button>';
            html += '</div>';
            html += '</div></div></div>';
            html += '</div></div>';

            if (data.sessions && data.sessions.length > 0) {
                html += '<h6 class="mb-3">Active Sessions</h6>';
                html += '<div class="table-container"><table class="table table-dark table-sm">';
                html += '<thead><tr><th>Session</th><th>Turn</th><th>Time (s)</th><th>Players</th><th>Current Player</th><th>Progress</th><th>Actions</th></tr></thead><tbody>';
                data.sessions.forEach(s => {
                    const progress = s.accumulatedSeconds + '/' + data.turnIntervalSeconds + 's';
                    html += `<tr>
                        <td>${escapeHtml(s.sessionName || s.sessionId.substring(0, 8))}</td>
                        <td>${s.currentTurn}</td>
                        <td>${s.currentTime}</td>
                        <td>${s.playerCount}</td>
                        <td>${escapeHtml(s.currentPlayer || '-')}</td>
                        <td>${progress}</td>
                        <td><button class="btn btn-sm btn-warning" data-admin-action="advance-turn" data-admin-param="${s.sessionId}">Advance Turn</button></td>
                    </tr>`;
                });
                html += '</tbody></table></div>';
            } else {
                html += '<div class="text-center text-muted py-4"><p>No active game sessions.</p></div>';
            }

            document.getElementById('turnManagementContent').innerHTML = html;
        } catch (error) {
            document.getElementById('turnManagementContent').innerHTML = `
                <div class="alert alert-danger">Failed to load turn status. Please login first.</div>
            `;
        }
    }

    async function advanceTurn(sessionId) {
        try {
            const response = await fetch('/api/v1/admin/turn/advance', {
                method: 'POST',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify({ sessionId: sessionId })
            });
            const data = await response.json();
            if (response.ok) {
                showToast(`Turn advanced to ${data.turn} (${data.currentPlayer})`, 'success');
                await loadTurnStatus();
            } else {
                showToast(data.error || 'Failed to advance turn', 'danger');
            }
        } catch (error) {
            showToast('Failed to advance turn', 'danger');
        }
    }

    async function setTurnInterval() {
        const input = document.getElementById('turnIntervalInput');
        const seconds = parseInt(input.value);
        if (isNaN(seconds) || seconds < 1) {
            showToast('Interval must be at least 1 second', 'danger');
            return;
        }
        try {
            const response = await fetch('/api/v1/admin/turn/interval', {
                method: 'POST',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify({ seconds: seconds })
            });
            const data = await response.json();
            if (response.ok) {
                showToast(data.message, 'success');
                await loadTurnStatus();
            } else {
                showToast(data.error || 'Failed to set interval', 'danger');
            }
        } catch (error) {
            showToast('Failed to set turn interval', 'danger');
        }
    }

    async function toggleScheduler(enabled) {
        try {
            const response = await fetch('/api/v1/admin/turn/toggle', {
                method: 'POST',
                headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
                body: JSON.stringify({ enabled: enabled })
            });
            const data = await response.json();
            if (response.ok) {
                showToast(data.message, 'success');
                await loadTurnStatus();
            } else {
                showToast(data.error || 'Failed to toggle scheduler', 'danger');
            }
        } catch (error) {
            showToast('Failed to toggle scheduler', 'danger');
        }
    }

    // ============================================================
    // System Logs
    // ============================================================
    async function openLogsViewer() {
        openModal('logsViewerModal');
        await refreshLogs();
    }

    async function refreshLogs() {
        const lines = document.getElementById('logLines').value;
        try {
            const response = await fetch(`/api/v1/admin/logs?lines=${lines}`, { headers: getAuthHeaders() });
            const data = await response.json();

            if (data.logs && data.logs.length > 0) {
                let html = '';
                data.logs.forEach(log => {
                    html += `<div class="log-line">${escapeHtml(log)}</div>`;
                });
                document.getElementById('logsContent').innerHTML = html;
            } else {
                document.getElementById('logsContent').innerHTML = `
                    <div class="text-center text-muted">No logs found</div>
                `;
            }
        } catch (error) {
            document.getElementById('logsContent').innerHTML = `
                <div class="alert alert-danger">Failed to load logs. Please login first.</div>
            `;
        }
    }

    // ============================================================
    // Utilities
    // ============================================================
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
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
    document.addEventListener('DOMContentLoaded', AdminPanel.init);
} else {
    AdminPanel.init();
}
