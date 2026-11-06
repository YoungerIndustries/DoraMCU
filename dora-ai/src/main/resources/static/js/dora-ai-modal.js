/**
 * Dora AI Modal - Client-side logic for the startup modal interface.
 * Handles tab switching, project management, code analysis, game analysis, and chat.
 */

(function() {
    'use strict';

    // Configuration
    const API_BASE = (window.AI_SERVER_URL || 'http://localhost:8081') + '/api/ai';
    const DORAMCU_SERVER_URL = window.DORAMCU_SERVER_URL || 'http://localhost:8080';
    const OLLAMA_SERVER_URL = window.OLLAMA_SERVER_URL || 'http://localhost:11434';
    const ollamaAvailable = window.OLLAMA_AVAILABLE !== false;

    // State
    let currentProject = null;
    let sessionId = generateSessionId();
    let currentAnalysisType = null;
    let projectFiles = [];
    let selectedFiles = new Set();

    // Tasks state
    let tasks = [];
    let currentTaskFilter = 'all';
    let selectedTaskId = null;
    let taskSseEventSource = null;

    // Status panel state
    let taskStatusLog = [];
    let ollamaStatusLog = [];
    const MAX_LOG_ENTRIES = 50;

    // DOM Elements
    const statusIndicator = document.getElementById('statusIndicator');
    const statusDot = statusIndicator.querySelector('.status-dot');
    const statusText = statusIndicator.querySelector('.status-text');
    const serverUrlDisplay = document.getElementById('serverUrlDisplay');
    const codeProjectSelect = document.getElementById('codeProjectSelect');
    const codeResults = document.getElementById('codeResults');
    const gameResults = document.getElementById('gameResults');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const analysisDialog = document.getElementById('analysisDialog');

    // Initialize
    document.addEventListener('DOMContentLoaded', init);

    function init() {
        // Update server URL displays
        const serverUrl = window.AI_SERVER_URL || 'http://localhost:8081';
        serverUrlDisplay.textContent = serverUrl.replace('http://', '');

        const doraMcuDisplay = document.getElementById('doraMcuServerDisplay');
        if (doraMcuDisplay) {
            doraMcuDisplay.textContent = DORAMCU_SERVER_URL.replace('http://', '');
        }

        const ollamaDisplay = document.getElementById('ollamaServerDisplay');
        if (ollamaDisplay) {
            ollamaDisplay.textContent = OLLAMA_SERVER_URL.replace('http://', '');
        }

        // Check status
        checkStatus();

        // Load projects
        loadProjects();

        // Setup tab navigation
        setupTabs();

        // Setup action buttons
        setupActionButtons();

        // Setup file browser
        setupFileBrowser();

        // Setup chat
        setupChat();

        // Setup tasks
        setupTasks();

        // Setup status panels
        setupStatusPanels();

        // Setup dialog
        setupDialog();

        // Setup close/skip buttons
        document.getElementById('closeModalBtn').addEventListener('click', closeModal);
        document.getElementById('skipBtn').addEventListener('click', closeModal);
    }

    // ==================== Status ====================

    async function checkStatus() {
        try {
            const response = await fetch(API_BASE + '/chat/status');
            if (response.ok) {
                const data = await response.json();
                const available = data.ollamaAvailable;
                statusDot.className = 'status-dot ' + (available ? 'online' : 'offline');
                statusText.textContent = available ? 'Online' : 'Offline';
                addOllamaStatus(available ? 'success' : 'warning',
                    available ? 'Ollama is online and ready' : 'Ollama is offline');
            } else {
                setOffline();
                addOllamaStatus('error', 'Server unreachable');
            }
        } catch (e) {
            setOffline();
            addOllamaStatus('error', 'Connection failed: ' + e.message);
        }
    }

    function setOffline() {
        statusDot.className = 'status-dot offline';
        statusText.textContent = 'Server Unreachable';
    }

    // ==================== Status Panels ====================

    function setupStatusPanels() {
        // Task Status Panel toggle
        const taskToggleBtn = document.getElementById('taskStatusToggleBtn');
        const taskPanel = document.getElementById('taskStatusPanel');
        if (taskToggleBtn && taskPanel) {
            taskToggleBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleStatusPanel(taskPanel, taskToggleBtn);
            });
        }

        // Task Status Panel header click to toggle
        const taskHeader = document.getElementById('taskStatusPanelHeader');
        if (taskHeader && taskPanel) {
            taskHeader.addEventListener('click', (e) => {
                if (e.target.classList.contains('status-panel-clear')) return;
                toggleStatusPanel(taskPanel, taskToggleBtn);
            });
        }

        // Task Status Panel clear button
        const taskClearBtn = document.getElementById('clearTaskStatusBtn');
        if (taskClearBtn) {
            taskClearBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                clearStatusLog('task');
            });
        }

        // Ollama Status Panel toggle
        const ollamaToggleBtn = document.getElementById('ollamaStatusToggleBtn');
        const ollamaPanel = document.getElementById('ollamaStatusPanel');
        if (ollamaToggleBtn && ollamaPanel) {
            ollamaToggleBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleStatusPanel(ollamaPanel, ollamaToggleBtn);
            });
        }

        // Ollama Status Panel header click to toggle
        const ollamaHeader = document.getElementById('ollamaStatusPanelHeader');
        if (ollamaHeader && ollamaPanel) {
            ollamaHeader.addEventListener('click', (e) => {
                if (e.target.classList.contains('status-panel-clear')) return;
                toggleStatusPanel(ollamaPanel, ollamaToggleBtn);
            });
        }

        // Ollama Status Panel clear button
        const ollamaClearBtn = document.getElementById('clearOllamaStatusBtn');
        if (ollamaClearBtn) {
            ollamaClearBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                clearStatusLog('ollama');
            });
        }

        // Log initial status
        addTaskStatus('info', 'Task monitoring started');
        addOllamaStatus('info', 'Communication monitoring started');
    }

    function toggleStatusPanel(panel, toggleBtn) {
        panel.classList.toggle('collapsed');
        if (toggleBtn) {
            toggleBtn.classList.toggle('collapsed');
        }
    }

    function addTaskStatus(type, text) {
        const entry = { time: new Date(), type: type, text: text };
        taskStatusLog.push(entry);
        if (taskStatusLog.length > MAX_LOG_ENTRIES) {
            taskStatusLog.shift();
        }
        renderStatusEntry('taskStatusBody', entry, 'taskStatusCount');
    }

    function addOllamaStatus(type, text) {
        const entry = { time: new Date(), type: type, text: text };
        ollamaStatusLog.push(entry);
        if (ollamaStatusLog.length > MAX_LOG_ENTRIES) {
            ollamaStatusLog.shift();
        }
        renderStatusEntry('ollamaStatusBody', entry, 'ollamaStatusCount');
    }

    function renderStatusEntry(bodyId, entry, countId) {
        const body = document.getElementById(bodyId);
        const countEl = document.getElementById(countId);
        if (!body) return;

        // Remove empty message if present
        const emptyEl = body.querySelector('.status-log-empty');
        if (emptyEl) emptyEl.remove();

        const icons = {
            'info': 'ℹ️',
            'success': '✅',
            'warning': '⚠️',
            'error': '❌',
            'processing': '⏳'
        };

        const div = document.createElement('div');
        div.className = 'status-log-entry status-' + entry.type;

        const timeStr = formatLogTime(entry.time);
        div.innerHTML =
            '<span class="status-log-time">' + timeStr + '</span>' +
            '<span class="status-log-icon">' + (icons[entry.type] || '•') + '</span>' +
            '<span class="status-log-text">' + escapeHtml(entry.text) + '</span>';

        body.appendChild(div);
        body.scrollTop = body.scrollHeight;

        // Update count
        if (countId) {
            const countElRef = document.getElementById(countId);
            if (countElRef) {
                const log = bodyId === 'taskStatusBody' ? taskStatusLog : ollamaStatusLog;
                countElRef.textContent = log.length;
            }
        }
    }

    function clearStatusLog(panel) {
        const bodyId = panel === 'task' ? 'taskStatusBody' : 'ollamaStatusBody';
        const countId = panel === 'task' ? 'taskStatusCount' : 'ollamaStatusCount';
        const body = document.getElementById(bodyId);
        if (body) {
            body.innerHTML = '<div class="status-log-empty">' +
                (panel === 'task' ? 'No task activity yet.' : 'No communication activity yet.') +
                '</div>';
        }
        if (panel === 'task') taskStatusLog = [];
        else ollamaStatusLog = [];

        const countEl = document.getElementById(countId);
        if (countEl) countEl.textContent = '0';
    }

    function formatLogTime(date) {
        const h = date.getHours().toString().padStart(2, '0');
        const m = date.getMinutes().toString().padStart(2, '0');
        const s = date.getSeconds().toString().padStart(2, '0');
        return h + ':' + m + ':' + s;
    }

    // ==================== Projects ====================

    async function loadProjects() {
        try {
            const response = await fetch(API_BASE + '/projects');
            if (response.ok) {
                const projects = await response.json();
                codeProjectSelect.innerHTML = '<option value="">Select a project...</option>';
                let defaultProject = null;
                projects.forEach(p => {
                    const option = document.createElement('option');
                    option.value = p.id;
                    option.textContent = p.name + ' (' + p.fileCount + ' files)' + (p.default ? ' (default)' : '');
                    codeProjectSelect.appendChild(option);
                    if (p.default) {
                        defaultProject = p.id;
                    }
                });
                // Auto-select the default project
                if (defaultProject) {
                    codeProjectSelect.value = defaultProject;
                }
                updateCodeActions();
            }
        } catch (e) {
            console.error('Failed to load projects:', e);
        }
    }

    function updateCodeActions() {
        const projectId = codeProjectSelect.value;
        currentProject = projectId || null;
        const buttons = ['bugDetectBtn', 'codeReviewBtn', 'codeGenBtn'];
        buttons.forEach(id => {
            document.getElementById(id).disabled = !projectId;
        });

        // Show/hide files section
        const filesSection = document.getElementById('filesSection');
        if (projectId) {
            filesSection.style.display = 'block';
            loadProjectFiles(projectId);
        } else {
            filesSection.style.display = 'none';
            projectFiles = [];
            selectedFiles.clear();
            renderFilesList();
            renderSelectedFiles();
        }
    }

    // ==================== Tabs ====================

    function setupTabs() {
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                // Update active tab button
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                // Update active panel
                document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
                const tabId = btn.getAttribute('data-tab');
                document.getElementById(tabId).classList.add('active');
            });
        });
    }

    // ==================== Action Buttons ====================

    function setupActionButtons() {
        // Project selection change
        codeProjectSelect.addEventListener('change', updateCodeActions);

        // Refresh projects
        document.getElementById('refreshProjectsBtn').addEventListener('click', loadProjects);

        // Code analysis actions
        document.getElementById('bugDetectBtn').addEventListener('click', () => {
            currentAnalysisType = 'bugs';
            showDialog('Bug Detection', true);
        });

        document.getElementById('codeReviewBtn').addEventListener('click', () => {
            currentAnalysisType = 'review';
            showDialog('Code Review', true);
        });

        document.getElementById('codeGenBtn').addEventListener('click', () => {
            currentAnalysisType = 'generate';
            showDialog('Code Generation', false);
        });

        // Game analysis actions
        document.getElementById('strategyAnalysisBtn').addEventListener('click', async () => {
            const fileContext = await buildGameFileContext();
            sendChatMessageWithFileContext('Analyze my current game strategy and provide recommendations for improvement.', 'game-analysis', fileContext);
        });

        document.getElementById('trainingStatusBtn').addEventListener('click', () => {
            fetchTrainingStatus();
        });

        document.getElementById('gameReviewBtn').addEventListener('click', async () => {
            const fileContext = await buildGameFileContext();
            sendChatMessageWithFileContext('Review my recent game performance and suggest improvements.', 'game-analysis', fileContext);
        });
    }

    // ==================== File Browser ====================

    function setupFileBrowser() {
        const filesSearchInput = document.getElementById('filesSearchInput');
        const selectAllCheckbox = document.getElementById('selectAllFilesCheckbox');
        const clearSelectedBtn = document.getElementById('clearSelectedFilesBtn');

        filesSearchInput.addEventListener('input', () => {
            renderFilesList();
        });

        selectAllCheckbox.addEventListener('change', () => {
            const filtered = getFilteredFiles();
            if (selectAllCheckbox.checked) {
                filtered.forEach(f => selectedFiles.add(f.path));
            } else {
                filtered.forEach(f => selectedFiles.delete(f.path));
            }
            renderFilesList();
            renderSelectedFiles();
        });

        clearSelectedBtn.addEventListener('click', () => {
            selectedFiles.clear();
            selectAllCheckbox.checked = false;
            renderFilesList();
            renderSelectedFiles();
        });
    }

    async function loadProjectFiles(projectId) {
        try {
            const response = await fetch(API_BASE + '/projects/' + projectId + '/files');
            if (response.ok) {
                projectFiles = await response.json();
                selectedFiles.clear();
                document.getElementById('selectAllFilesCheckbox').checked = false;
                renderFilesList();
                renderSelectedFiles();
            }
        } catch (e) {
            console.error('Failed to load project files:', e);
        }
    }

    function getFilteredFiles() {
        const query = document.getElementById('filesSearchInput').value.toLowerCase().trim();
        if (!query) return projectFiles;
        return projectFiles.filter(f => f.path.toLowerCase().includes(query));
    }

    function getFileIcon(extension) {
        switch (extension) {
            case 'java': return '☕';
            case 'kt': return '🟣';
            case 'js': return '📜';
            case 'ts': return '🔷';
            case 'html': return '🌐';
            case 'css': return '🎨';
            case 'xml': return '📄';
            case 'json': return '📊';
            case 'properties': return '⚙️';
            case 'md': return '📝';
            case 'gradle': return '🏗️';
            default: return '📄';
        }
    }

    function renderFilesList() {
        const container = document.getElementById('filesList');
        const filtered = getFilteredFiles();
        const countEl = document.getElementById('selectedFilesCount');

        container.innerHTML = '';
        filtered.forEach(file => {
            const item = document.createElement('div');
            item.className = 'file-item' + (selectedFiles.has(file.path) ? ' selected' : '');
            item.innerHTML =
                '<input type="checkbox" class="file-checkbox" ' + (selectedFiles.has(file.path) ? 'checked' : '') + '>' +
                '<span class="file-icon">' + getFileIcon(file.extension) + '</span>' +
                '<span class="file-path" title="' + escapeHtml(file.path) + '">' + escapeHtml(file.path) + '</span>' +
                '<span class="file-meta">' + file.lineCount + 'L</span>';
            item.addEventListener('click', (e) => {
                if (e.target.classList.contains('file-checkbox')) return;
                toggleFileSelection(file.path);
            });
            item.querySelector('.file-checkbox').addEventListener('change', () => {
                toggleFileSelection(file.path);
            });
            container.appendChild(item);
        });

        countEl.textContent = selectedFiles.size + ' selected';
    }

    function toggleFileSelection(path) {
        if (selectedFiles.has(path)) {
            selectedFiles.delete(path);
        } else {
            selectedFiles.add(path);
        }
        renderFilesList();
        renderSelectedFiles();
    }

    function renderSelectedFiles() {
        const container = document.getElementById('selectedFilesList');
        container.innerHTML = '';

        if (selectedFiles.size === 0) {
            container.innerHTML = '<span class="empty-hint">No files selected. Click files above to add them.</span>';
            return;
        }

        selectedFiles.forEach(path => {
            const file = projectFiles.find(f => f.path === path);
            if (!file) return;
            const tag = document.createElement('span');
            tag.className = 'selected-file-tag';
            tag.innerHTML =
                getFileIcon(file.extension) + ' ' + escapeHtml(file.name) +
                '<span class="remove-file" title="Remove">&times;</span>';
            tag.querySelector('.remove-file').addEventListener('click', () => {
                selectedFiles.delete(path);
                renderFilesList();
                renderSelectedFiles();
            });
            container.appendChild(tag);
        });
    }

    /**
     * Returns the list of selected file paths as an array, or null if none selected.
     */
    function getSelectedFilePaths() {
        if (selectedFiles.size === 0) return null;
        return Array.from(selectedFiles);
    }

    // ==================== Dialog ====================

    function setupDialog() {
        document.querySelectorAll('.dialog-close').forEach(btn => {
            btn.addEventListener('click', hideDialog);
        });

        document.getElementById('dialogCancelBtn').addEventListener('click', hideDialog);
        document.getElementById('dialogConfirmBtn').addEventListener('click', confirmAnalysis);
    }

    function showDialog(title, showFilePaths) {
        document.getElementById('dialogTitle').textContent = title;
        document.getElementById('analysisDescription').value = '';
        document.getElementById('analysisFilePaths').value = '';
        document.getElementById('filePathsGroup').style.display = showFilePaths ? 'block' : 'none';
        analysisDialog.classList.add('active');
        document.getElementById('analysisDescription').focus();
    }

    function hideDialog() {
        analysisDialog.classList.remove('active');
        currentAnalysisType = null;
    }

    async function confirmAnalysis() {
        const description = document.getElementById('analysisDescription').value.trim();
        const filePathsStr = document.getElementById('analysisFilePaths').value.trim();
        const manualFilePaths = filePathsStr ? filePathsStr.split(',').map(s => s.trim()).filter(s => s) : null;

        // Combine manually entered paths with selected files
        let filePaths = getSelectedFilePaths();
        if (manualFilePaths && manualFilePaths.length > 0) {
            if (filePaths) {
                filePaths = filePaths.concat(manualFilePaths);
            } else {
                filePaths = manualFilePaths;
            }
        }

        if (!description && currentAnalysisType !== 'generate') {
            return;
        }

        hideDialog();

        if (currentAnalysisType === 'bugs') {
            await analyzeBugs(filePaths, description);
        } else if (currentAnalysisType === 'review') {
            await reviewCode(filePaths, description);
        } else if (currentAnalysisType === 'generate') {
            await generateCode(description);
        }
    }

    // ==================== Code Analysis ====================

    /**
     * Fetches file contents for the given file paths and builds a context string.
     */
    async function fetchFileContext(filePaths) {
        if (!filePaths || filePaths.length === 0 || !currentProject) {
            return '';
        }

        let context = '';
        for (const filePath of filePaths) {
            try {
                const response = await fetch(
                    API_BASE + '/projects/' + currentProject + '/file?filePath=' + encodeURIComponent(filePath)
                );
                if (response.ok) {
                    const content = await response.text();
                    context += '--- FILE: ' + filePath + ' ---\n' + content + '\n\n';
                }
            } catch (e) {
                console.warn('Failed to fetch file content for ' + filePath + ':', e);
            }
        }
        return context;
    }

    async function analyzeBugs(filePaths, description) {
        clearResults(codeResults);
        const thinkingEl = addStreamingResult(codeResults, 'Analyzing code for bugs...');

        // Fetch file contents for context
        const fileContext = await fetchFileContext(filePaths);

        try {
            const response = await fetch(API_BASE + '/analyze/bugs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    projectId: currentProject,
                    filePaths: filePaths,
                    description: description,
                    fileContext: fileContext
                })
            });

            if (!response.ok) {
                addResult(codeResults, 'Failed to analyze bugs: ' + response.statusText, 'error');
                return;
            }

            // Handle SSE streaming response
            await streamResponse(response, codeResults, false, thinkingEl);
        } catch (e) {
            addResult(codeResults, 'Error: ' + e.message, 'error');
        }
    }

    async function reviewCode(filePaths, description) {
        clearResults(codeResults);
        const thinkingEl = addStreamingResult(codeResults, 'Performing code review...');

        // Fetch file contents for context
        const fileContext = await fetchFileContext(filePaths);

        try {
            const response = await fetch(API_BASE + '/analyze/review', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    projectId: currentProject,
                    filePaths: filePaths,
                    description: description,
                    fileContext: fileContext
                })
            });

            if (!response.ok) {
                addResult(codeResults, 'Failed to review code: ' + response.statusText, 'error');
                return;
            }

            await streamResponse(response, codeResults, false, thinkingEl);
        } catch (e) {
            addResult(codeResults, 'Error: ' + e.message, 'error');
        }
    }

    async function generateCode(description) {
        clearResults(codeResults);
        const thinkingEl = addStreamingResult(codeResults, 'Generating code...');

        // Fetch selected file contents for context
        const selectedPaths = getSelectedFilePaths();
        const fileContext = await fetchFileContext(selectedPaths);

        try {
            const response = await fetch(API_BASE + '/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    projectId: currentProject,
                    description: description,
                    context: fileContext,
                    language: 'java'
                })
            });

            if (!response.ok) {
                addResult(codeResults, 'Failed to generate code: ' + response.statusText, 'error');
                return;
            }

            await streamResponse(response, codeResults, false, thinkingEl);
        } catch (e) {
            addResult(codeResults, 'Error: ' + e.message, 'error');
        }
    }

    // ==================== Game Analysis ====================

    async function fetchTrainingStatus() {
        clearResults(gameResults);
        addStreamingResult(gameResults, 'Fetching training status...');

        try {
            const response = await fetch(API_BASE + '/dl/training/status');
            if (response.ok) {
                const data = await response.json();
                clearResults(gameResults);
                addResult(gameResults, formatTrainingStatus(data), 'info');
            } else {
                addResult(gameResults, 'Training service not available.', 'warning');
            }
        } catch (e) {
            addResult(gameResults, 'Could not reach training service. Is the DL module running?', 'warning');
        }
    }

    function formatTrainingStatus(data) {
        let status = 'Training Status:\n\n';
        if (data.epoch) status += 'Current Epoch: ' + data.epoch + '\n';
        if (data.loss) status += 'Current Loss: ' + data.loss.toFixed(6) + '\n';
        if (data.accuracy) status += 'Accuracy: ' + (data.accuracy * 100).toFixed(2) + '%\n';
        if (data.status) status += 'Status: ' + data.status + '\n';
        if (!data.epoch) status += 'No training data available yet.';
        return status;
    }

    /**
     * Builds a file context string from selected files for game analysis.
     */
    async function buildGameFileContext() {
        const selectedPaths = getSelectedFilePaths();
        return await fetchFileContext(selectedPaths);
    }

    // ==================== Chat ====================

    function setupChat() {
        document.getElementById('chatSendBtn').addEventListener('click', sendChat);
        chatInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendChat();
            }
        });
    }

    function sendChat() {
        const message = chatInput.value.trim();
        if (!message) return;

        addUserMessage(message);
        chatInput.value = '';

        sendChatMessage(message, 'chat');
    }

    async function sendChatMessage(message, targetContainer) {
        return sendChatMessageWithFileContext(message, targetContainer, '');
    }

    async function sendChatMessageWithFileContext(message, targetContainer, fileContext) {
        if (targetContainer === 'chat') {
            addUserMessage(message);
        }

        const container = targetContainer === 'chat' ? chatMessages : gameResults;
        // Add a temporary "Thinking..." indicator that streamResponse will replace with status messages
        const thinkingEl = addStreamingResult(container, 'Thinking...');

        try {
            const response = await fetch(API_BASE + '/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: sessionId,
                    message: message,
                    mode: 'chat',
                    projectId: currentProject,
                    fileContext: fileContext || undefined
                })
            });

            if (!response.ok) {
                if (targetContainer === 'chat') {
                    addAiMessage('Sorry, I could not process your request.');
                } else {
                    addResult(gameResults, 'Failed to get response.', 'error');
                }
                return;
            }

            await streamResponse(response, container, targetContainer === 'chat', thinkingEl);
        } catch (e) {
            if (targetContainer === 'chat') {
                addAiMessage('Error: Could not connect to AI server.');
            } else {
                addResult(gameResults, 'Error: ' + e.message, 'error');
            }
        }
    }

    // ==================== SSE Streaming ====================

    async function streamResponse(response, container, isChat, thinkingEl) {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let resultElement = null;
        let statusElement = null;
        let accumulatedText = '';

        // Remove the "Thinking..." placeholder when streaming starts
        if (thinkingEl && thinkingEl.parentNode) {
            thinkingEl.remove();
        }

        // Log Ollama communication start
        addOllamaStatus('processing', 'Request sent to Ollama');

        try {
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });

                // Parse SSE data
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        let data = line.slice(6);
                        if (data === '[DONE]') continue;

                        // Strip JSON quotes if Spring serialized the string with quotes
                        data = stripJsonQuotes(data);

                        // Check if this is a status event
                        if (isStatusData(data)) {
                            const statusText = parseStatus(data);
                            if (statusText) {
                                // Log to Ollama status panel
                                const statusType = data === 'done' ? 'success' : 'processing';
                                addOllamaStatus(statusType, statusText);

                                // Remove previous status element if exists
                                if (statusElement) {
                                    statusElement.remove();
                                }
                                // Create the streaming element if not yet created
                                if (!resultElement) {
                                    if (isChat) {
                                        statusElement = addStatusMessage(container, statusText);
                                    } else {
                                        statusElement = addStreamingResult(container, statusText);
                                    }
                                } else {
                                    // Update existing status element
                                    if (isChat) {
                                        statusElement = addStatusMessage(container, statusText);
                                    } else {
                                        statusElement = addStreamingResult(container, statusText);
                                    }
                                }
                                scrollToBottom(container);
                            }
                            continue;
                        }

                        try {
                            const parsed = JSON.parse(data);
                            const content = parsed.content || parsed.choices?.[0]?.delta?.content || data;
                            if (content) {
                                // Remove status element when content starts
                                if (statusElement) {
                                    statusElement.remove();
                                    statusElement = null;
                                }
                                // Create result element if not yet created
                                if (!resultElement) {
                                    if (isChat) {
                                        resultElement = addAiMessage('');
                                    } else {
                                        resultElement = addStreamingResult(container, '');
                                    }
                                }
                                accumulatedText += content;
                                if (resultElement) {
                                    if (isChat) {
                                        // Use markdown parsing for rich text display
                                        resultElement.innerHTML = parseMarkdown(accumulatedText);
                                    } else {
                                        resultElement.textContent = displayText(normalizeText(accumulatedText));
                                    }
                                    scrollToBottom(container);
                                }
                            }
                        } catch (e) {
                            // Not JSON, treat as plain text
                            const text = data;
                            if (text) {
                                // Remove status element when content starts
                                if (statusElement) {
                                    statusElement.remove();
                                    statusElement = null;
                                }
                                // Create result element if not yet created
                                if (!resultElement) {
                                    if (isChat) {
                                        resultElement = addAiMessage('');
                                    } else {
                                        resultElement = addStreamingResult(container, '');
                                    }
                                }
                                accumulatedText += text;
                                if (resultElement) {
                                    if (isChat) {
                                        // Use markdown parsing for rich text display
                                        resultElement.innerHTML = parseMarkdown(accumulatedText);
                                    } else {
                                        resultElement.textContent = displayText(normalizeText(accumulatedText));
                                    }
                                    scrollToBottom(container);
                                }
                            }
                        }
                    }
                }
            }
            // Clean up status element when stream ends
            if (statusElement) {
                statusElement.remove();
            }
            addOllamaStatus('success', 'Response received successfully');
        } catch (e) {
            console.error('Stream error:', e);
            addOllamaStatus('error', 'Stream error: ' + e.message);
            if (statusElement) {
                statusElement.remove();
            }
        }
    }

    /**
     * Normalize text to ensure proper spacing and newlines.
     * Runs on the full accumulated text so it can see across chunk boundaries.
     */
    function normalizeText(text) {
        if (!text) return text;
        // Add space after punctuation if missing
        text = text.replace(/([.,;:!?\)\]}])([A-Za-z])/g, '$1 $2');
        // Add space after sentence-ending punctuation followed by uppercase
        text = text.replace(/([.!?])([A-Z])/g, '$1 $2');
        // Add space between lowercase and uppercase (camelCase)
        text = text.replace(/([a-z])([A-Z])/g, '$1 $2');
        // Normalize 3+ consecutive spaces to single space (preserve newlines)
        text = text.replace(/[^\n] {3,}/g, (m) => m.charAt(0) + ' ');
        return text;
    }

    /**
     * Lightweight markdown parser for chat display.
     * Converts markdown-formatted text to HTML for proper rendering.
     * Handles: code blocks, inline code, bold, italic, strikethrough,
     * links, images, headers, lists, blockquotes, horizontal rules, tables.
     */
    function parseMarkdown(text) {
        if (!text) return '';

        let html = text;

        // Escape HTML entities first to prevent XSS
        html = html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

        // Code blocks (```lang ... ```) - must be processed before other rules
        html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (match, lang, code) => {
            const language = lang || 'text';
            const codeContent = code.replace(/^\n+|\n+$/g, ''); // trim leading/trailing newlines
            return '<pre class="code-block"><code class="language-' + language + '">' + codeContent + '</code></pre>';
        });

        // Inline code (`code`)
        html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

        // Bold and italic combined (***text***)
        html = html.replace(/\*\*\*([^*]+)\*\*\*/g, '<strong><em>$1</em></strong>');

        // Bold (**text** or __text__)
        html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>');

        // Italic (*text* or _text_)
        html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
        html = html.replace(/(?<!\w)_([^_]+)_(?!\w)/g, '<em>$1</em>');

        // Strikethrough (~~text~~)
        html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>');

        // Headers (### H3, ## H2, # H1)
        html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
        html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
        html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');

        // Horizontal rules (---, ***, ___)
        html = html.replace(/^(---+|___+|\*\*\*+)\s*$/gm, '<hr class="markdown-hr">');

        // Blockquotes (> text)
        html = html.replace(/^&gt;\s*(.+)$/gm, '<blockquote class="markdown-blockquote">$1</blockquote>');

        // Images (![alt](url))
        html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="markdown-image">');

        // Links ([text](url))
        html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer" class="markdown-link">$1</a>');

        // Unordered lists (- item, * item)
        html = html.replace(/^[\-\*]\s+(.+)$/gm, '<li class="markdown-li">$1</li>');
        html = html.replace(/((?:<li class="markdown-li">.+<\/li>\n?)+)/g, '<ul class="markdown-ul">$1</ul>');

        // Ordered lists (1. item)
        html = html.replace(/^\d+\.\s+(.+)$/gm, '<li class="markdown-li">$1</li>');

        // Tables
        html = html.replace(/^(\|.+\|)\n(\|[-:\s|]+\|)\n((?:\|.+\|\n?)*)/gm, (match, header, separator, body) => {
            const headerCells = header.split('|').filter(c => c.trim()).map(c => '<th>' + c.trim() + '</th>').join('');
            const rows = body.trim().split('\n').map(row => {
                const cells = row.split('|').filter(c => c.trim()).map(c => '<td>' + c.trim() + '</td>').join('');
                return '<tr>' + cells + '</tr>';
            }).join('');
            return '<table class="markdown-table"><thead><tr>' + headerCells + '</tr></thead><tbody>' + rows + '</tbody></table>';
        });

        // Paragraphs: convert double newlines to paragraph breaks
        html = html.split(/\n\n+/).map(block => {
            // Don't wrap blocks that are already wrapped in block elements
            if (/^<(pre|ul|ol|li|blockquote|div|p|h[1-6]|table|hr)/.test(block.trim())) {
                return block.trim();
            }
            // Convert single newlines within a paragraph to <br>
            return '<p>' + block.replace(/\n/g, '<br>') + '</p>';
        }).join('');

        return html;
    }

    /**
     * Final display-level text analyzer: ensures spaces and newlines are correct
     * right before rendering to the DOM. This is the last line of defense.
     * Used for plain text fallback; for chat messages, parseMarkdown is preferred.
     */
    function displayText(text) {
        if (!text) return text;

        // 1. Add missing space after punctuation marks
        text = text.replace(/([.,;:!?\)\]}])([A-Za-z\d])/g, '$1 $2');

        // 2. Add missing space before opening brackets/parentheses if missing
        text = text.replace(/([A-Za-z\d])([\(\[<])/g, '$1 $2');

        // 3. Add missing space after closing brackets/parentheses if missing
        text = text.replace(/([\)\]>])([A-Za-z\d])/g, '$1 $2');

        // 4. Add missing space around slashes (but not URLs or paths like C:\)
        text = text.replace(/([A-Za-z])\/([A-Za-z])/g, '$1 / $2');

        // 5. Add missing space between digits and letters (e.g., "3players" → "3 players")
        text = text.replace(/(\d)([A-Za-z])/g, '$1 $2');

        // 6. Add missing space between letters and digits (e.g., "players3" → "players 3")
        text = text.replace(/([A-Za-z])(\d)/g, '$1 $2');

        // 7. Add missing space after sentence-ending punctuation followed by uppercase
        text = text.replace(/([.!?])\s*([A-Z])/g, '$1 $2');

        // 8. Add missing space between words (lowercase followed by uppercase without space)
        text = text.replace(/([a-z])([A-Z])/g, '$1 $2');

        // 9. Add missing space between consecutive uppercase letters followed by lowercase
        // (e.g., "APIendpoint" → "API endpoint", "XMLParser" → "XML Parser")
        text = text.replace(/([A-Z]+)([A-Z][a-z])/g, '$1 $2');

        // 10. Add missing space after quotes
        text = text.replace(/(['"])([A-Za-z\d])/g, '$1 $2');
        text = text.replace(/([A-Za-z\d])(['"])/g, '$1 $2');

        // 11. Add missing space after hyphens and dashes (but not in compound words like "well-known")
        text = text.replace(/(-)([A-Z])/g, '$1 $2');

        // 12. Fix missing newlines after sentence-ending punctuation followed by uppercase
        text = text.replace(/([.!?])([A-Z])/g, (match, punct, letter) => {
            return punct + '\n' + letter;
        });

        // 13. Ensure blank line between paragraphs (double newline after sentence ending)
        text = text.replace(/([.!?])\n([A-Z])/g, '$1\n\n$2');

        // 14. Add space after colons if missing (but not in time formats like 12:30)
        text = text.replace(/(:)([A-Za-z])/g, '$1 $2');

        // 15. Add space after semicolons if missing
        text = text.replace(/(;)([A-Za-z\d])/g, '$1 $2');

        // 16. Handle bullet points and numbered lists - ensure proper spacing
        text = text.replace(/([-*•])\s*([A-Z])/g, '$1 $2');
        text = text.replace(/(\d+\.)\s*([A-Z])/g, '$1 $2');

        // 17. Normalize excessive spaces (3+ spaces to single space, preserve newlines)
        text = text.replace(/ {3,}/g, ' ');

        // 18. Normalize excessive newlines (3+ newlines to double newline)
        text = text.replace(/\n{3,}/g, '\n\n');

        // 19. Trim trailing whitespace on each line
        text = text.replace(/[ \t]+$/gm, '');

        // 20. Trim leading/trailing whitespace of the entire text
        text = text.trim();

        return text;
    }

    /**
     * Strip JSON quotes from a string and unescape JSON-encoded content
     * Spring's SseEmitter JSON-encodes string data, so we need to properly decode it
     * to preserve newlines, spaces, and other special characters
     */
    function stripJsonQuotes(data) {
        if (data.startsWith('"') && data.endsWith('"')) {
            try {
                return JSON.parse(data);
            } catch (e) {
                // Fallback if JSON.parse fails
                return data.slice(1, -1);
            }
        }
        return data;
    }

    /**
     * Check if data is a status event
     */
    function isStatusData(data) {
        return ['receiving', 'processing', 'generating', 'done'].includes(data);
    }

    /**
     * Parse status from data
     */
    function parseStatus(data) {
        const statuses = {
            'receiving': 'Receiving your message...',
            'processing': 'Processing request...',
            'generating': 'Generating response...',
            'done': 'Done'
        };
        return statuses[data] || null;
    }

    /**
     * Add a status message to the chat
     */
    function addStatusMessage(container, text) {
        const div = document.createElement('div');
        div.className = 'message ai-message status-message';
        const content = document.createElement('div');
        content.className = 'message-content status-content';
        content.innerHTML = '<span class="status-spinner"></span> ' + escapeHtml(text);
        div.appendChild(content);
        container.appendChild(div);
        return div;
    }

    // ==================== UI Helpers ====================

    function addUserMessage(text) {
        const div = document.createElement('div');
        div.className = 'message user-message';
        div.innerHTML = '<div class="message-content">' + escapeHtml(text) + '</div>';
        chatMessages.appendChild(div);
        scrollToBottom(chatMessages);
    }

    function addAiMessage(text) {
        const div = document.createElement('div');
        div.className = 'message ai-message';
        const content = document.createElement('div');
        content.className = 'message-content';
        // Use markdown parsing for rich text display
        content.innerHTML = text ? parseMarkdown(text) : '';
        div.appendChild(content);
        chatMessages.appendChild(div);
        scrollToBottom(chatMessages);
        return content;
    }

    function addResult(container, text, type) {
        const div = document.createElement('div');
        div.className = 'result-item ' + (type || '');
        div.textContent = text;
        container.appendChild(div);
        scrollToBottom(container);
        return div;
    }

    function addStreamingResult(container, text) {
        const div = document.createElement('div');
        div.className = 'result-streaming';
        div.textContent = text;
        container.appendChild(div);
        scrollToBottom(container);
        return div;
    }

    function clearResults(container) {
        container.innerHTML = '';
    }

    function scrollToBottom(container) {
        container.scrollTop = container.scrollHeight;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function generateSessionId() {
        return 'modal-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    }

    // ==================== Tasks ====================

    function setupTasks() {
        // Load initial tasks
        loadTasks();
        loadTaskCounts();

        // Setup filter buttons
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentTaskFilter = btn.getAttribute('data-filter');
                renderTasks();
            });
        });

        // Setup action buttons
        document.getElementById('refreshTasksBtn').addEventListener('click', () => {
            loadTasks();
            loadTaskCounts();
        });

        document.getElementById('clearCompletedTasksBtn').addEventListener('click', clearCompletedTasks);

        // Setup detail panel
        document.getElementById('closeTaskDetailBtn').addEventListener('click', closeTaskDetail);
        document.getElementById('taskDetailAcceptBtn').addEventListener('click', () => actionTask('accept'));
        document.getElementById('taskDetailStartBtn').addEventListener('click', () => actionTask('start'));
        document.getElementById('taskDetailCompleteBtn').addEventListener('click', () => actionTask('complete'));
        document.getElementById('taskDetailDeclineBtn').addEventListener('click', () => actionTask('decline'));
        document.getElementById('taskDetailSaveNoteBtn').addEventListener('click', saveTaskNote);

        // Subscribe to SSE notifications
        subscribeToTaskUpdates();
    }

    async function loadTasks() {
        try {
            const url = currentTaskFilter === 'all'
                ? API_BASE + '/tasks'
                : API_BASE + '/tasks?status=' + currentTaskFilter;
            const response = await fetch(url);
            if (response.ok) {
                tasks = await response.json();
                renderTasks();
                updateTaskBadge();
                addTaskStatus('info', 'Loaded ' + tasks.length + ' tasks');
            }
        } catch (e) {
            console.error('Failed to load tasks:', e);
            addTaskStatus('error', 'Failed to load tasks: ' + e.message);
        }
    }

    async function loadTaskCounts() {
        try {
            const response = await fetch(API_BASE + '/tasks/counts');
            if (response.ok) {
                const counts = await response.json();
                renderTaskCounts(counts);
            }
        } catch (e) {
            console.error('Failed to load task counts:', e);
        }
    }

    function renderTaskCounts(counts) {
        const container = document.getElementById('tasksCounts');
        const labels = {
            'PENDING': 'Pending',
            'ACCEPTED': 'Accepted',
            'IN_PROGRESS': 'In Progress',
            'COMPLETED': 'Done',
            'DECLINED': 'Declined'
        };
        let html = '';
        for (const [key, label] of Object.entries(labels)) {
            const count = counts[key] || 0;
            if (count > 0 || key === 'PENDING') {
                html += '<span class="count-badge">' + label + ': <span class="count-number">' + count + '</span></span>';
            }
        }
        html += '<span class="count-badge">Total: <span class="count-number">' + (counts.total || 0) + '</span></span>';
        container.innerHTML = html;
    }

    function renderTasks() {
        const container = document.getElementById('taskList');

        if (tasks.length === 0) {
            container.innerHTML =
                '<div class="tasks-placeholder">' +
                '<span class="placeholder-icon">📋</span>' +
                '<p>No tasks yet. Dora AI will send you tasks here.</p>' +
                '</div>';
            return;
        }

        container.innerHTML = '';
        tasks.forEach(task => {
            const card = document.createElement('div');
            card.className = 'task-card status-' + task.status.toLowerCase() +
                (task.id === selectedTaskId ? ' selected' : '');

            const priorityClass = 'priority-' + task.priority.toLowerCase();
            const statusLabel = task.status.replace('_', ' ');

            card.innerHTML =
                '<div class="task-priority-dot ' + priorityClass + '"></div>' +
                '<div class="task-card-body">' +
                '<div class="task-card-title">' + escapeHtml(task.title) + '</div>' +
                '<div class="task-card-desc">' + escapeHtml(task.description || '') + '</div>' +
                '<div class="task-card-meta">' +
                '<span class="task-type-badge">' + escapeHtml(task.type) + '</span>' +
                '<span class="task-status-badge status-' + task.status.toLowerCase() + '">' + escapeHtml(statusLabel) + '</span>' +
                '<span class="task-card-time">' + formatTime(task.createdAt) + '</span>' +
                '</div>' +
                '</div>' +
                '<div class="task-card-actions">' +
                (task.isActive ?
                    '<button class="task-action-btn accept" title="Accept">✓</button>' +
                    '<button class="task-action-btn complete" title="Complete">⚡</button>' +
                    '<button class="task-action-btn decline" title="Decline">✕</button>'
                    : '') +
                '</div>';

            // Click on card opens detail
            card.addEventListener('click', (e) => {
                if (e.target.closest('.task-action-btn')) return;
                openTaskDetail(task.id);
            });

            // Action buttons
            const acceptBtn = card.querySelector('.task-action-btn.accept');
            if (acceptBtn) {
                acceptBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    performTaskAction(task.id, 'accept');
                });
            }
            const completeBtn = card.querySelector('.task-action-btn.complete');
            if (completeBtn) {
                completeBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    performTaskAction(task.id, 'complete');
                });
            }
            const declineBtn = card.querySelector('.task-action-btn.decline');
            if (declineBtn) {
                declineBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    performTaskAction(task.id, 'decline');
                });
            }

            container.appendChild(card);
        });
    }

    async function performTaskAction(taskId, action) {
        const actionLabels = {
            'accept': 'Accepting task...',
            'start': 'Starting task...',
            'complete': 'Completing task...',
            'decline': 'Declining task...'
        };
        const label = actionLabels[action] || 'Processing...';
        showTaskDetailStatus(label);
        addTaskStatus('processing', label);
        try {
            const response = await fetch(API_BASE + '/tasks/' + taskId + '/' + action, {
                method: 'POST'
            });
            if (response.ok) {
                const updated = await response.json();
                // Update local task
                const idx = tasks.findIndex(t => t.id === taskId);
                if (idx !== -1) {
                    tasks[idx] = { ...tasks[idx], status: updated.status, completedAt: updated.completedAt };
                }
                renderTasks();
                loadTaskCounts();
                updateTaskBadge();
                // Update detail panel if open
                if (selectedTaskId === taskId) {
                    openTaskDetail(taskId);
                }
                hideTaskDetailStatus();
                addTaskStatus('success', 'Task ' + action + 'ed → ' + updated.status);
            }
        } catch (e) {
            console.error('Failed to perform task action:', e);
            hideTaskDetailStatus();
            addTaskStatus('error', 'Failed to ' + action + ' task: ' + e.message);
        }
    }

    function showTaskDetailStatus(text) {
        const statusEl = document.getElementById('taskDetailProcessingStatus');
        const statusTextEl = document.getElementById('taskDetailProcessingText');
        if (statusEl && statusTextEl) {
            statusTextEl.textContent = text;
            statusEl.style.display = 'flex';
        }
    }

    function hideTaskDetailStatus() {
        const statusEl = document.getElementById('taskDetailProcessingStatus');
        if (statusEl) {
            statusEl.style.display = 'none';
        }
    }

    async function openTaskDetail(taskId) {
        const task = tasks.find(t => t.id === taskId);
        if (!task) return;

        selectedTaskId = taskId;

        // Hide any previous status message
        hideTaskDetailStatus();
        document.getElementById('taskDetailPanel').style.display = 'block';
        document.getElementById('taskDetailTitle').textContent = task.title;
        document.getElementById('taskDetailType').textContent = task.type;
        document.getElementById('taskDetailPriority').textContent = task.priority;
        document.getElementById('taskDetailStatus').textContent = task.status.replace('_', ' ');
        document.getElementById('taskDetailSource').textContent = task.source || 'system';
        document.getElementById('taskDetailCreated').textContent = formatTime(task.createdAt);
        document.getElementById('taskDetailDescription').textContent = task.description || 'No description.';
        document.getElementById('taskDetailNote').value = task.userNote || '';

        // Show/hide action buttons based on status
        const acceptBtn = document.getElementById('taskDetailAcceptBtn');
        const startBtn = document.getElementById('taskDetailStartBtn');
        const completeBtn = document.getElementById('taskDetailCompleteBtn');
        const declineBtn = document.getElementById('taskDetailDeclineBtn');

        acceptBtn.style.display = task.isActive ? '' : 'none';
        startBtn.style.display = task.isActive ? '' : 'none';
        completeBtn.style.display = task.isActive ? '' : 'none';
        declineBtn.style.display = task.isActive ? '' : 'none';

        // Highlight selected card
        renderTasks();
    }

    function closeTaskDetail() {
        selectedTaskId = null;
        hideTaskDetailStatus();
        document.getElementById('taskDetailPanel').style.display = 'none';
        renderTasks();
    }

    function actionTask(action) {
        if (!selectedTaskId) return;
        performTaskAction(selectedTaskId, action);
    }

    async function saveTaskNote() {
        if (!selectedTaskId) return;
        const note = document.getElementById('taskDetailNote').value.trim();
        showTaskDetailStatus('Saving note...');
        addTaskStatus('processing', 'Saving task note...');
        try {
            const response = await fetch(API_BASE + '/tasks/' + selectedTaskId + '/note', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ note: note })
            });
            if (response.ok) {
                const updated = await response.json();
                const idx = tasks.findIndex(t => t.id === selectedTaskId);
                if (idx !== -1) {
                    tasks[idx].userNote = updated.userNote;
                }
                showTaskDetailStatus('Note saved.');
                addTaskStatus('success', 'Task note saved');
                setTimeout(hideTaskDetailStatus, 1500);
            }
        } catch (e) {
            console.error('Failed to save note:', e);
            hideTaskDetailStatus();
            addTaskStatus('error', 'Failed to save note: ' + e.message);
        }
    }

    async function clearCompletedTasks() {
        showTaskDetailStatus('Clearing completed tasks...');
        addTaskStatus('processing', 'Clearing completed tasks...');
        try {
            const response = await fetch(API_BASE + '/tasks/clear-completed', {
                method: 'DELETE'
            });
            if (response.ok) {
                loadTasks();
                loadTaskCounts();
                updateTaskBadge();
                hideTaskDetailStatus();
                addTaskStatus('success', 'Completed tasks cleared');
            }
        } catch (e) {
            console.error('Failed to clear completed tasks:', e);
            hideTaskDetailStatus();
            addTaskStatus('error', 'Failed to clear completed tasks: ' + e.message);
        }
    }

    function updateTaskBadge() {
        const badge = document.getElementById('taskBadge');
        const pendingCount = tasks.filter(t => t.status === 'PENDING').length;
        if (pendingCount > 0) {
            badge.style.display = 'inline-flex';
            badge.textContent = pendingCount;
        } else {
            badge.style.display = 'none';
        }
    }

    // ==================== SSE Task Notifications ====================

    function subscribeToTaskUpdates() {
        if (typeof EventSource === 'undefined') {
            console.warn('SSE not supported, falling back to polling');
            pollTasks();
            return;
        }

        try {
            taskSseEventSource = new EventSource(API_BASE + '/tasks/subscribe');

            taskSseEventSource.addEventListener('TASK_CREATED', (event) => {
                const task = JSON.parse(event.data);
                handleNewTask(task);
            });

            taskSseEventSource.addEventListener('TASK_UPDATED', (event) => {
                const task = JSON.parse(event.data);
                handleTaskUpdate(task);
            });

            taskSseEventSource.addEventListener('TASK_REMOVED', (event) => {
                const task = JSON.parse(event.data);
                handleTaskRemoved(task.id);
            });

            taskSseEventSource.onopen = () => {
                console.log('Task SSE connection opened');
                addTaskStatus('success', 'SSE connection opened');
                addOllamaStatus('success', 'Task SSE subscription active');
            };

            taskSseEventSource.onerror = (e) => {
                console.warn('Task SSE error, reconnecting...');
                addTaskStatus('warning', 'SSE connection lost, reconnecting...');
                addOllamaStatus('warning', 'Task SSE disconnected');
                taskSseEventSource.close();
                // Retry after 5 seconds
                setTimeout(subscribeToTaskUpdates, 5000);
            };
        } catch (e) {
            console.warn('Failed to connect to task SSE, falling back to polling:', e);
            addTaskStatus('warning', 'SSE unavailable, using polling');
            pollTasks();
        }
    }

    function handleNewTask(task) {
        // Add to local list if it matches current filter
        if (currentTaskFilter === 'all' || task.status === currentTaskFilter) {
            tasks.unshift(task);
            renderTasks();
        }
        loadTaskCounts();
        updateTaskBadge();
        showTaskNotification(task);
        addTaskStatus('info', 'New task: ' + task.title + ' (' + task.status + ')');
        addOllamaStatus('info', 'Task notification received: ' + task.title);
    }

    function handleTaskUpdate(task) {
        const idx = tasks.findIndex(t => t.id === task.id);
        if (idx !== -1) {
            tasks[idx] = task;
        } else if (currentTaskFilter === 'all' || task.status === currentTaskFilter) {
            tasks.unshift(task);
        }
        renderTasks();
        loadTaskCounts();
        updateTaskBadge();
        addTaskStatus('info', 'Task updated: ' + task.title + ' → ' + task.status);
    }

    function handleTaskRemoved(taskId) {
        tasks = tasks.filter(t => t.id !== taskId);
        if (selectedTaskId === taskId) {
            closeTaskDetail();
        }
        renderTasks();
        loadTaskCounts();
        updateTaskBadge();
        addTaskStatus('warning', 'Task removed: ' + taskId.substring(0, 8) + '...');
    }

    function showTaskNotification(task) {
        const notif = document.createElement('div');
        notif.className = 'task-notification';
        notif.innerHTML =
            '<div class="notif-title">📋 New Task: ' + escapeHtml(task.title) + '</div>' +
            '<div class="notif-desc">' + escapeHtml((task.description || '').substring(0, 80)) + '</div>';
        notif.addEventListener('click', () => {
            // Switch to tasks tab
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelector('[data-tab="tasks"]').classList.add('active');
            document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
            document.getElementById('tasks').classList.add('active');
            notif.remove();
        });
        document.body.appendChild(notif);
        setTimeout(() => {
            if (notif.parentNode) notif.remove();
        }, 8000);
    }

    // Fallback polling for environments without SSE support
    function pollTasks() {
        setInterval(async () => {
            try {
                const response = await fetch(API_BASE + '/tasks/counts');
                if (response.ok) {
                    const counts = await response.json();
                    const currentTotal = tasks.length;
                    if (counts.total !== tasks.length) {
                        await loadTasks();
                        loadTaskCounts();
                        updateTaskBadge();
                    }
                }
            } catch (e) {
                // Silently fail
            }
        }, 10000); // Poll every 10 seconds
    }

    // ==================== Utility ====================

    function formatTime(isoString) {
        if (!isoString) return '';
        try {
            const date = new Date(isoString);
            const now = new Date();
            const diff = now - date;
            if (diff < 60000) return 'Just now';
            if (diff < 3600000) return Math.floor(diff / 60000) + 'm ago';
            if (diff < 86400000) return Math.floor(diff / 3600000) + 'h ago';
            return date.toLocaleDateString();
        } catch (e) {
            return isoString;
        }
    }

    function closeModal() {
        // Close SSE connection
        if (taskSseEventSource) {
            taskSseEventSource.close();
        }
        // Try to notify the Java bridge if available
        if (window.javaBridge) {
            window.javaBridge.closeModal();
        }
        // Fallback: close the window
        window.close();
        // If window.close() doesn't work, hide the content
        document.body.style.display = 'none';
    }

})();
