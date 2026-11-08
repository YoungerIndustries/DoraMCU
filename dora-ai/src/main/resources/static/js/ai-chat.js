/**
 * DoraAI - Code & Game Assistant
 * Handles tab navigation, chat, code analysis, game analysis, tasks, file browser, and status panels.
 */

(function() {
    'use strict';

    // ==================== State ====================
    let sessionId = null;
    let isStreaming = false;
    let currentLanguage = null;
    let lastInputType = 'text';
    let currentTab = 'chat';
    let projectFiles = [];
    let selectedFiles = new Set();
    let tasks = [];
    let currentFilter = 'all';
    let selectedTaskId = null;

    // Status panel state
    let taskStatusLog = [];
    let ollamaStatusLog = [];
    const MAX_LOG_ENTRIES = 50;

    // ==================== DOM Elements ====================
    const messageContainer = document.getElementById('messageContainer');
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const micBtn = document.getElementById('micBtn');
    const projectSelect = document.getElementById('projectSelect');
    const statusIndicator = document.getElementById('statusIndicator');
    const statusDot = statusIndicator.querySelector('.status-dot');
    const statusText = statusIndicator.querySelector('.status-text');
    const registerProjectBtn = document.getElementById('registerProjectBtn');
    const clearChatBtn = document.getElementById('clearChatBtn');
    const registerModal = document.getElementById('registerModal');
    const sessionIdDisplay = document.getElementById('sessionIdDisplay');
    const recordingIndicator = document.getElementById('recordingIndicator');
    const languageBadge = document.getElementById('languageBadge');

    // ==================== Initialization ====================
    async function init() {
        await checkStatus();
        await loadProviders();
        await loadProjects();
        setupTabNavigation();
        setupEventListeners();
        setupVoice();
        setupStatusPanels();
        setupAnalysisDialog();
        await loadTasks();
        setupTaskSSE();
    }

    // ==================== Tab Navigation ====================
    function setupTabNavigation() {
        document.querySelectorAll('.nav-item').forEach(btn => {
            btn.addEventListener('click', () => {
                const tab = btn.dataset.tab;
                switchTab(tab);
            });
        });
    }

    function switchTab(tab) {
        currentTab = tab;
        // Update nav
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.toggle('active', item.dataset.tab === tab);
        });
        // Update panels
        document.querySelectorAll('.tab-panel').forEach(panel => {
            panel.classList.toggle('active', panel.id === tab);
        });
        // Load tasks when switching to tasks tab
        if (tab === 'tasks') {
            loadTasks();
        }
    }

    // ==================== Status Check ====================
    async function checkStatus() {
        try {
            const status = await AiApi.getStatus();
            const online = status.ollamaAvailable || status.geminiAvailable;
            if (online) {
                statusDot.className = 'status-dot online';
                statusText.textContent = 'Online';
                if (status.ollamaAvailable) {
                    addOllamaStatus('success', 'Ollama is online and ready');
                }
                if (status.geminiAvailable) {
                    addOllamaStatus('success', 'Gemini is online and ready');
                }
            } else {
                statusDot.className = 'status-dot offline';
                statusText.textContent = 'Offline';
                addOllamaStatus('warning', 'All AI providers are offline');
            }
        } catch (error) {
            statusDot.className = 'status-dot offline';
            statusText.textContent = 'Error';
            addOllamaStatus('error', 'Connection failed: ' + error.message);
        }
    }

    // ==================== AI Providers ====================
    async function loadProviders() {
        try {
            const providers = await AiApi.getProviders();
            const providerSelect = document.getElementById('providerSelect');
            providerSelect.innerHTML = '';
            providers.forEach(provider => {
                const option = document.createElement('option');
                option.value = provider;
                option.textContent = provider.charAt(0).toUpperCase() + provider.slice(1);
                providerSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Failed to load providers:', error);
        }
    }

    function getSelectedProvider() {
        const providerSelect = document.getElementById('providerSelect');
        return providerSelect ? providerSelect.value : 'ollama';
    }

    function getSelectedMode() {
        const modeSelect = document.getElementById('modeSelect');
        return modeSelect ? modeSelect.value : 'code';
    }

    function onProviderChanged(providerId) {
        const modeGroup = document.getElementById('modeSelectorGroup');
        if (modeGroup) {
            modeGroup.style.display = (providerId === 'openhands') ? 'block' : 'none';
        }
    }

    // ==================== Projects ====================
    async function loadProjects() {
        try {
            const projects = await AiApi.listProjects();
            projectSelect.innerHTML = '<option value="">No project selected</option>';
            let defaultProject = null;
            projects.forEach(project => {
                const option = document.createElement('option');
                option.value = project.id;
                option.textContent = `${project.name} (${project.fileCount} files)` + (project.default ? ' (default)' : '');
                projectSelect.appendChild(option);
                if (project.default) {
                    defaultProject = project.id;
                }
            });
            if (defaultProject) {
                projectSelect.value = defaultProject;
                onProjectSelected(defaultProject);
            }
        } catch (error) {
            console.error('Failed to load projects:', error);
        }
    }

    function onProjectSelected(projectId) {
        const filesPanel = document.getElementById('filesPanel');
        const bugDetectBtn = document.getElementById('bugDetectBtn');
        const codeReviewBtn = document.getElementById('codeReviewBtn');
        const codeGenBtn = document.getElementById('codeGenBtn');

        if (projectId) {
            filesPanel.style.display = 'block';
            bugDetectBtn.disabled = false;
            codeReviewBtn.disabled = false;
            codeGenBtn.disabled = false;
            loadProjectFiles(projectId);
        } else {
            filesPanel.style.display = 'none';
            bugDetectBtn.disabled = true;
            codeReviewBtn.disabled = true;
            codeGenBtn.disabled = true;
        }
    }

    // ==================== File Browser ====================
    async function loadProjectFiles(projectId) {
        try {
            projectFiles = await AiApi.listFiles(projectId);
            renderFilesList();
        } catch (error) {
            console.error('Failed to load files:', error);
        }
    }

    function renderFilesList(filter = '') {
        const filesList = document.getElementById('filesList');
        const filtered = projectFiles.filter(f =>
            !filter || f.name.toLowerCase().includes(filter.toLowerCase()) ||
            f.path.toLowerCase().includes(filter.toLowerCase())
        );

        filesList.innerHTML = filtered.map(f => {
            const ext = f.name.split('.').pop().toLowerCase();
            const icon = ext === 'java' ? '☕' : ext === 'kt' ? '🟣' : ext === 'js' ? '📜' : ext === 'html' ? '🌐' : ext === 'css' ? '🎨' : '📄';
            const isSelected = selectedFiles.has(f.path);
            return `
                <div class="file-item ${isSelected ? 'selected' : ''}" data-path="${escapeHtml(f.path)}">
                    <span class="file-icon">${icon}</span>
                    <span class="file-name">${escapeHtml(f.name)}</span>
                    <span class="file-meta">${f.lineCount || '?'} lines</span>
                </div>
            `;
        }).join('');

        // Attach click handlers
        filesList.querySelectorAll('.file-item').forEach(item => {
            item.addEventListener('click', () => {
                const path = item.dataset.path;
                toggleFileSelection(path);
            });
        });
    }

    function toggleFileSelection(path) {
        if (selectedFiles.has(path)) {
            selectedFiles.delete(path);
        } else {
            selectedFiles.add(path);
        }
        renderFilesList(document.getElementById('filesSearchInput').value);
        renderSelectedFiles();
        updateSelectedCount();
    }

    function renderSelectedFiles() {
        const container = document.getElementById('selectedFilesList');
        if (selectedFiles.size === 0) {
            container.innerHTML = '<span class="empty-hint">No files selected. Click files above to add them.</span>';
            return;
        }

        container.innerHTML = Array.from(selectedFiles).map(path => {
            const name = path.split('/').pop();
            return `<span class="selected-file-tag">${escapeHtml(name)} <span class="remove-file" data-path="${escapeHtml(path)}">&times;</span></span>`;
        }).join('');

        container.querySelectorAll('.remove-file').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                selectedFiles.delete(btn.dataset.path);
                renderFilesList(document.getElementById('filesSearchInput').value);
                renderSelectedFiles();
                updateSelectedCount();
            });
        });
    }

    function updateSelectedCount() {
        document.getElementById('selectedFilesCount').textContent = `${selectedFiles.size} selected`;
    }

    function getFileContext() {
        if (selectedFiles.size === 0) return null;
        const projectId = projectSelect.value;
        return Array.from(selectedFiles).map(path => {
            const file = projectFiles.find(f => f.path === path);
            return file ? `${file.name}:\n${file.content || ''}` : '';
        }).join('\n\n---\n\n');
    }

    // ==================== Event Listeners ====================
    function setupEventListeners() {
        // Chat
        sendBtn.addEventListener('click', sendMessage);
        messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        // Project
        registerProjectBtn.addEventListener('click', () => {
            registerModal.classList.add('active');
        });
        projectSelect.addEventListener('change', (e) => {
            onProjectSelected(e.target.value);
        });

        // Provider change — show/hide OpenHands mode selector
        const providerSelectEl = document.getElementById('providerSelect');
        if (providerSelectEl) {
            providerSelectEl.addEventListener('change', (e) => {
                onProviderChanged(e.target.value);
            });
        }

        // Modals
        document.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', () => {
                btn.closest('.modal').classList.remove('active');
            });
        });
        document.getElementById('cancelRegisterBtn').addEventListener('click', () => {
            registerModal.classList.remove('active');
        });
        document.getElementById('confirmRegisterBtn').addEventListener('click', registerProject);
        registerModal.addEventListener('click', (e) => {
            if (e.target === registerModal) registerModal.classList.remove('active');
        });

        // Voice
        if (micBtn) micBtn.addEventListener('click', toggleRecording);

        // Clear chat
        clearChatBtn.addEventListener('click', clearChat);

        // File browser
        document.getElementById('filesSearchInput').addEventListener('input', (e) => {
            renderFilesList(e.target.value);
        });
        document.getElementById('selectAllFilesCheckbox').addEventListener('change', (e) => {
            if (e.target.checked) {
                projectFiles.forEach(f => selectedFiles.add(f.path));
            } else {
                selectedFiles.clear();
            }
            renderFilesList(document.getElementById('filesSearchInput').value);
            renderSelectedFiles();
            updateSelectedCount();
        });
        document.getElementById('clearSelectedFilesBtn').addEventListener('click', () => {
            selectedFiles.clear();
            document.getElementById('selectAllFilesCheckbox').checked = false;
            renderFilesList(document.getElementById('filesSearchInput').value);
            renderSelectedFiles();
            updateSelectedCount();
        });

        // Code analysis actions
        document.getElementById('bugDetectBtn').addEventListener('click', () => showAnalysisDialog('Bug Detection', 'bugs'));
        document.getElementById('codeReviewBtn').addEventListener('click', () => showAnalysisDialog('Code Review', 'review'));
        document.getElementById('codeGenBtn').addEventListener('click', () => showAnalysisDialog('Code Generation', 'generate'));

        // Game analysis actions
        document.getElementById('strategyAnalysisBtn').addEventListener('click', runGameAnalysis('strategy'));
        document.getElementById('trainingStatusBtn').addEventListener('click', runGameAnalysis('training'));
        document.getElementById('gameReviewBtn').addEventListener('click', runGameAnalysis('review'));

        // Tasks
        document.getElementById('refreshTasksBtn').addEventListener('click', loadTasks);
        document.getElementById('clearCompletedTasksBtn').addEventListener('click', clearCompletedTasks);
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentFilter = btn.dataset.filter;
                renderTasks();
            });
        });
        document.getElementById('closeTaskDetailBtn').addEventListener('click', closeTaskDetail);
        document.getElementById('taskDetailAcceptBtn').addEventListener('click', () => performTaskAction('accept'));
        document.getElementById('taskDetailStartBtn').addEventListener('click', () => performTaskAction('start'));
        document.getElementById('taskDetailCompleteBtn').addEventListener('click', () => performTaskAction('complete'));
        document.getElementById('taskDetailDeclineBtn').addEventListener('click', () => performTaskAction('decline'));
        document.getElementById('taskDetailSaveNoteBtn').addEventListener('click', saveTaskNote);
    }

    // ==================== Analysis Dialog ====================
    function setupAnalysisDialog() {
        const dialog = document.getElementById('analysisDialog');
        document.getElementById('dialogCancelBtn').addEventListener('click', () => {
            dialog.classList.remove('active');
        });
        dialog.addEventListener('click', (e) => {
            if (e.target === dialog) dialog.classList.remove('active');
        });
        dialog.querySelector('.modal-close').addEventListener('click', () => {
            dialog.classList.remove('active');
        });
        document.getElementById('dialogConfirmBtn').addEventListener('click', runAnalysis);
    }

    let currentAnalysisType = 'bugs';
    function showAnalysisDialog(title, type) {
        currentAnalysisType = type;
        document.getElementById('dialogTitle').textContent = title;
        document.getElementById('analysisDescription').value = '';
        document.getElementById('filePathsGroup').style.display = type === 'generate' ? 'block' : 'none';
        document.getElementById('analysisDialog').classList.add('active');
    }

    async function runAnalysis() {
        const description = document.getElementById('analysisDescription').value.trim();
        const projectId = projectSelect.value;
        const fileContext = getFileContext();

        if (!projectId) {
            addSystemMessage('Please select a project first.');
            return;
        }

        document.getElementById('analysisDialog').classList.remove('active');
        const resultsContainer = document.getElementById('codeResults');
        resultsContainer.innerHTML = '<div class="status-indicator-inline"><span class="status-spinner"></span><span class="status-text-inline">Analyzing...</span></div>';

        addOllamaStatus('processing', 'Request sent to Ollama');

        try {
            let response;
            if (currentAnalysisType === 'bugs') {
                response = await AiApi.analyzeBugs(projectId, null, description, fileContext);
            } else if (currentAnalysisType === 'review') {
                response = await AiApi.reviewCode(projectId, null, description, fileContext);
            } else {
                response = await AiApi.generateCode(projectId, description, null, null, fileContext);
            }

            if (!response.ok) throw new Error(`HTTP error: ${response.status}`);

            const contentEl = document.createElement('div');
            contentEl.className = 'message-content';
            resultsContainer.innerHTML = '';
            resultsContainer.appendChild(contentEl);
            await streamResponseToElement(response, contentEl);
            addOllamaStatus('success', 'Response received successfully');
        } catch (error) {
            resultsContainer.innerHTML = `<div class="message system-message"><div class="message-content"><p>Error: ${escapeHtml(error.message)}</p></div></div>`;
            addOllamaStatus('error', 'Stream error: ' + error.message);
        }
    }

    // ==================== Game Analysis ====================
    function runGameAnalysis(type) {
        return async function() {
            const projectId = projectSelect.value || null;
            const fileContext = getFileContext();
            const resultsContainer = document.getElementById('gameResults');

            let message;
            if (type === 'strategy') {
                message = 'Analyze the game structure and provide strategy recommendations for DoraMCU.';
            } else if (type === 'training') {
                message = 'Show the current deep learning model training status and progress.';
            } else {
                message = 'Review recent game performance and provide insights.';
            }

            resultsContainer.innerHTML = '<div class="status-indicator-inline"><span class="status-spinner"></span><span class="status-text-inline">Analyzing...</span></div>';
            addOllamaStatus('processing', 'Game analysis request sent');

            try {
                const response = await AiApi.sendChatMessage(null, message, getSelectedMode(), projectId, null, 'text', fileContext, getSelectedProvider());
                if (!response.ok) throw new Error(`HTTP error: ${response.status}`);

                const contentEl = document.createElement('div');
                contentEl.className = 'message-content';
                resultsContainer.innerHTML = '';
                resultsContainer.appendChild(contentEl);
                await streamResponseToElement(response, contentEl);
                addOllamaStatus('success', 'Game analysis complete');
            } catch (error) {
                resultsContainer.innerHTML = `<div class="message system-message"><div class="message-content"><p>Error: ${escapeHtml(error.message)}</p></div></div>`;
                addOllamaStatus('error', 'Game analysis error: ' + error.message);
            }
        };
    }

    // ==================== Chat ====================
    async function sendMessage() {
        if (isStreaming) return;

        const message = messageInput.value.trim();
        if (!message) return;

        const projectId = projectSelect.value || null;
        const fileContext = getFileContext();

        addUserMessage(message);
        messageInput.value = '';

        const typingIndicator = showTypingIndicator();
        isStreaming = true;
        sendBtn.disabled = true;
        messageInput.disabled = true;

        addOllamaStatus('processing', 'Request sent to Ollama');

        try {
            const response = await AiApi.sendChatMessage(sessionId, message, getSelectedMode(), projectId, currentLanguage, lastInputType, fileContext, getSelectedProvider());

            if (!response.ok) throw new Error(`HTTP error: ${response.status}`);

            typingIndicator.remove();
            const aiMessageEl = createAiMessageElement();
            const contentEl = aiMessageEl.querySelector('.message-content');
            messageContainer.appendChild(aiMessageEl);

            await streamResponse(response, contentEl);

            if (!sessionId) {
                sessionId = generateSessionId();
                sessionIdDisplay.textContent = sessionId.substring(0, 8);
            }
        } catch (error) {
            typingIndicator.remove();
            addSystemMessage(`Error: ${error.message}. Make sure Ollama is running.`);
            addOllamaStatus('error', 'Chat error: ' + error.message);
        } finally {
            isStreaming = false;
            sendBtn.disabled = false;
            messageInput.disabled = false;
            messageInput.focus();
        }
    }

    async function streamResponse(response, contentEl) {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let statusEl = null;
        let accumulatedText = '';

        try {
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('event: ')) continue;
                    if (line.startsWith('data: ')) {
                        let data = line.substring(6);
                        if (data) {
                            data = stripJsonQuotes(data);

                            if (isStatusData(data)) {
                                const statusText = parseStatus(data);
                                if (statusText) {
                                    const statusType = data === 'done' ? 'success' : 'processing';
                                    addOllamaStatus(statusType, statusText);
                                    if (statusEl) statusEl.remove();
                                    statusEl = addStatusMessage(statusText);
                                    continue;
                                }
                            }
                            if (statusEl) {
                                statusEl.remove();
                                statusEl = null;
                            }
                            accumulatedText += data;
                            const rendered = renderMarkdown(accumulatedText);
                            if (rendered.text !== null) {
                                contentEl.textContent = rendered.text;
                            } else {
                                contentEl.innerHTML = rendered.html;
                            }
                            scrollToBottom();
                        }
                    }
                }
            }
            if (statusEl) statusEl.remove();
            addOllamaStatus('success', 'Response received successfully');
        } catch (error) {
            console.error('Stream error:', error);
            if (statusEl) statusEl.remove();
            addOllamaStatus('error', 'Stream error: ' + error.message);
        }
    }

    async function streamResponseToElement(response, contentEl) {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let accumulatedText = '';

        try {
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('event: ')) continue;
                    if (line.startsWith('data: ')) {
                        let data = line.substring(6);
                        if (data) {
                            data = stripJsonQuotes(data);
                            if (isStatusData(data)) continue;
                            accumulatedText += data;
                            const rendered = renderMarkdown(accumulatedText);
                            if (rendered.text !== null) {
                                contentEl.textContent = rendered.text;
                            } else {
                                contentEl.innerHTML = rendered.html;
                            }
                        }
                    }
                }
            }
        } catch (error) {
            console.error('Stream error:', error);
        }
    }

    // ==================== Tasks ====================
    async function loadTasks() {
        try {
            tasks = await AiApi.listTasks();
            renderTasks();
            updateTaskBadge();
            addTaskStatus('info', `Loaded ${tasks.length} tasks`);
        } catch (error) {
            console.error('Failed to load tasks:', error);
            addTaskStatus('error', 'Failed to load tasks: ' + error.message);
        }
    }

    function renderTasks() {
        const taskList = document.getElementById('taskList');
        const filtered = currentFilter === 'all' ? tasks : tasks.filter(t => t.status === currentFilter);

        if (filtered.length === 0) {
            taskList.innerHTML = `
                <div class="tasks-placeholder">
                    <span class="placeholder-icon">📋</span>
                    <p>No tasks yet. Dora AI will send you tasks here.</p>
                </div>
            `;
            return;
        }

        taskList.innerHTML = filtered.map(task => `
            <div class="task-card ${task.id === selectedTaskId ? 'selected' : ''}" data-id="${task.id}">
                <div class="task-priority-dot ${task.priority}"></div>
                <div class="task-info">
                    <div class="task-title">${escapeHtml(task.title || 'Untitled Task')}</div>
                    <div class="task-description">${escapeHtml(task.description || '')}</div>
                    <div class="task-meta">
                        <span class="task-badge status-${task.status}">${task.status}</span>
                        <span class="task-badge type-${task.type}">${task.type}</span>
                        <span class="task-time">${formatTime(task.createdAt)}</span>
                    </div>
                </div>
            </div>
        `).join('');

        taskList.querySelectorAll('.task-card').forEach(card => {
            card.addEventListener('click', () => {
                openTaskDetail(card.dataset.id);
            });
        });

        // Update counts
        updateTaskCounts();
    }

    function updateTaskBadge() {
        const badge = document.getElementById('taskBadge');
        const pending = tasks.filter(t => t.status === 'PENDING').length;
        if (pending > 0) {
            badge.textContent = pending;
            badge.style.display = 'inline';
        } else {
            badge.style.display = 'none';
        }
    }

    function updateTaskCounts() {
        const countsEl = document.getElementById('tasksCounts');
        const counts = {
            all: tasks.length,
            PENDING: tasks.filter(t => t.status === 'PENDING').length,
            ACCEPTED: tasks.filter(t => t.status === 'ACCEPTED').length,
            IN_PROGRESS: tasks.filter(t => t.status === 'IN_PROGRESS').length,
            COMPLETED: tasks.filter(t => t.status === 'COMPLETED').length,
            DECLINED: tasks.filter(t => t.status === 'DECLINED').length
        };
        countsEl.innerHTML = Object.entries(counts).map(([status, count]) =>
            `<span class="count-badge">${status === 'all' ? 'Total' : status}: ${count}</span>`
        ).join('');
    }

    function openTaskDetail(taskId) {
        selectedTaskId = taskId;
        const task = tasks.find(t => t.id === taskId);
        if (!task) return;

        const panel = document.getElementById('taskDetailPanel');
        panel.style.display = 'block';

        document.getElementById('taskDetailTitle').textContent = task.title || 'Untitled Task';
        document.getElementById('taskDetailType').textContent = task.type || '-';
        document.getElementById('taskDetailPriority').textContent = task.priority || '-';
        document.getElementById('taskDetailStatus').textContent = task.status || '-';
        document.getElementById('taskDetailSource').textContent = task.source || 'Dora AI';
        document.getElementById('taskDetailCreated').textContent = formatTime(task.createdAt);
        document.getElementById('taskDetailDescription').textContent = task.description || '-';
        document.getElementById('taskDetailNote').value = task.userNote || '';

        // Update task card selection
        document.querySelectorAll('.task-card').forEach(card => {
            card.classList.toggle('selected', card.dataset.id === taskId);
        });
    }

    function closeTaskDetail() {
        selectedTaskId = null;
        document.getElementById('taskDetailPanel').style.display = 'none';
    }

    async function performTaskAction(action) {
        if (!selectedTaskId) return;

        const labels = {
            'accept': 'Accepting task...',
            'start': 'Starting task...',
            'complete': 'Completing task...',
            'decline': 'Declining task...'
        };
        const label = labels[action] || 'Processing...';
        showTaskDetailStatus(label);
        addTaskStatus('processing', label);

        try {
            const updated = await AiApi.taskAction(selectedTaskId, action);
            // Update local task
            const idx = tasks.findIndex(t => t.id === selectedTaskId);
            if (idx >= 0) tasks[idx] = updated;
            renderTasks();
            updateTaskBadge();
            openTaskDetail(selectedTaskId);
            hideTaskDetailStatus();
            addTaskStatus('success', `Task ${action}ed → ${updated.status}`);
        } catch (error) {
            hideTaskDetailStatus();
            addTaskStatus('error', `Failed to ${action} task: ${error.message}`);
        }
    }

    async function saveTaskNote() {
        if (!selectedTaskId) return;
        const note = document.getElementById('taskDetailNote').value.trim();
        showTaskDetailStatus('Saving note...');
        addTaskStatus('processing', 'Saving task note...');

        try {
            const updated = await AiApi.setTaskNote(selectedTaskId, note);
            const idx = tasks.findIndex(t => t.id === selectedTaskId);
            if (idx >= 0) tasks[idx] = updated;
            hideTaskDetailStatus();
            addTaskStatus('success', 'Note saved');
        } catch (error) {
            hideTaskDetailStatus();
            addTaskStatus('error', 'Failed to save note: ' + error.message);
        }
    }

    async function clearCompletedTasks() {
        showTaskDetailStatus('Clearing completed tasks...');
        addTaskStatus('processing', 'Clearing completed tasks...');
        try {
            await AiApi.clearCompletedTasks();
            await loadTasks();
            hideTaskDetailStatus();
            addTaskStatus('success', 'Completed tasks cleared');
        } catch (error) {
            hideTaskDetailStatus();
            addTaskStatus('error', 'Failed to clear: ' + error.message);
        }
    }

    function showTaskDetailStatus(text) {
        const el = document.getElementById('taskDetailProcessingStatus');
        document.getElementById('taskDetailProcessingText').textContent = text;
        el.style.display = 'flex';
    }

    function hideTaskDetailStatus() {
        document.getElementById('taskDetailProcessingStatus').style.display = 'none';
    }

    // ==================== Task SSE ====================
    function setupTaskSSE() {
        try {
            const eventSource = new EventSource('/api/ai/tasks/subscribe');
            eventSource.addEventListener('new-task', (e) => {
                addTaskStatus('info', 'New task received');
                loadTasks();
            });
            eventSource.addEventListener('task-updated', (e) => {
                addTaskStatus('info', 'Task updated');
                loadTasks();
            });
            eventSource.onerror = () => {
                addTaskStatus('warning', 'SSE connection lost, will retry');
            };
        } catch (error) {
            addTaskStatus('warning', 'SSE not available, using polling');
            // Fallback to polling
            setInterval(loadTasks, 10000);
        }
    }

    // ==================== Status Panels ====================
    function setupStatusPanels() {
        // Task Status Panel
        const taskToggleBtn = document.getElementById('taskStatusToggleBtn');
        const taskPanel = document.getElementById('taskStatusPanel');
        if (taskToggleBtn && taskPanel) {
            taskToggleBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleStatusPanel(taskPanel, taskToggleBtn);
            });
        }
        const taskHeader = document.getElementById('taskStatusPanelHeader');
        if (taskHeader && taskPanel) {
            taskHeader.addEventListener('click', (e) => {
                if (e.target.classList.contains('status-panel-clear')) return;
                toggleStatusPanel(taskPanel, taskToggleBtn);
            });
        }
        const taskClearBtn = document.getElementById('clearTaskStatusBtn');
        if (taskClearBtn) {
            taskClearBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                clearStatusLog('task');
            });
        }

        // Ollama Status Panel
        const ollamaToggleBtn = document.getElementById('ollamaToggleBtn');
        const ollamaPanel = document.getElementById('ollamaStatusPanel');
        if (ollamaToggleBtn && ollamaPanel) {
            ollamaToggleBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleStatusPanel(ollamaPanel, ollamaToggleBtn);
            });
        }
        const ollamaHeader = document.getElementById('ollamaStatusPanelHeader');
        if (ollamaHeader && ollamaPanel) {
            ollamaHeader.addEventListener('click', (e) => {
                if (e.target.classList.contains('status-panel-clear')) return;
                toggleStatusPanel(ollamaPanel, ollamaToggleBtn);
            });
        }
        const ollamaClearBtn = document.getElementById('clearOllamaStatusBtn');
        if (ollamaClearBtn) {
            ollamaClearBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                clearStatusLog('ollama');
            });
        }

        addTaskStatus('info', 'Task monitoring started');
        addOllamaStatus('info', 'Communication monitoring started');
    }

    function toggleStatusPanel(panel, toggleBtn) {
        panel.classList.toggle('collapsed');
        if (toggleBtn) toggleBtn.classList.toggle('collapsed');
    }

    function addTaskStatus(type, text) {
        const entry = { time: new Date(), type, text };
        taskStatusLog.push(entry);
        if (taskStatusLog.length > MAX_LOG_ENTRIES) taskStatusLog.shift();
        renderStatusEntry('taskStatusBody', entry, 'taskStatusCount', taskStatusLog);
    }

    function addOllamaStatus(type, text) {
        const entry = { time: new Date(), type, text };
        ollamaStatusLog.push(entry);
        if (ollamaStatusLog.length > MAX_LOG_ENTRIES) ollamaStatusLog.shift();
        renderStatusEntry('ollamaStatusBody', entry, 'ollamaStatusCount', ollamaStatusLog);
    }

    function renderStatusEntry(bodyId, entry, countId, log) {
        const body = document.getElementById(bodyId);
        if (!body) return;

        const emptyEl = body.querySelector('.status-log-empty');
        if (emptyEl) emptyEl.remove();

        const icons = { 'info': 'ℹ️', 'success': '✅', 'warning': '⚠️', 'error': '❌', 'processing': '⏳' };
        const div = document.createElement('div');
        div.className = 'status-log-entry status-' + entry.type;
        div.innerHTML =
            '<span class="status-log-time">' + formatLogTime(entry.time) + '</span>' +
            '<span class="status-log-icon">' + (icons[entry.type] || '•') + '</span>' +
            '<span class="status-log-text">' + escapeHtml(entry.text) + '</span>';
        body.appendChild(div);
        body.scrollTop = body.scrollHeight;

        const countEl = document.getElementById(countId);
        if (countEl) countEl.textContent = log.length;
    }

    function clearStatusLog(panel) {
        const bodyId = panel === 'task' ? 'taskStatusBody' : 'ollamaStatusBody';
        const countId = panel === 'task' ? 'taskStatusCount' : 'ollamaStatusCount';
        const body = document.getElementById(bodyId);
        if (body) {
            body.innerHTML = '<div class="status-log-empty">' +
                (panel === 'task' ? 'No task activity yet.' : 'No communication yet.') + '</div>';
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

    // ==================== Utility Functions ====================
    function stripJsonQuotes(data) {
        // Spring's SseEmitter JSON-encodes string data, so we need to properly decode it
        // to preserve newlines, spaces, and other special characters
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

    function isStatusData(data) {
        return ['receiving', 'processing', 'generating', 'done'].includes(data);
    }

    function parseStatus(data) {
        const statuses = {
            'receiving': 'Receiving your message...',
            'processing': 'Processing request...',
            'generating': 'Generating response...',
            'done': 'Done'
        };
        return statuses[data] || null;
    }

    function normalizeText(text) {
        // Client-side text normalization to fix missing spaces and newlines.
        // Runs on the full accumulated text so it can see across chunk boundaries.
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

    function displayText(text) {
        // Final display-level text analyzer: ensures spaces and newlines are correct
        // right before rendering to the DOM. This is the last line of defense.
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

    function renderMarkdown(text) {
        // Normalize text first to ensure proper spacing and newlines
        text = normalizeText(text);

        // Use textContent to set the message content so that whitespace is preserved.
        // The .message-content container has white-space: pre-wrap in CSS.
        // Only use innerHTML when there are code blocks that need HTML rendering.

        // Protect code blocks by replacing them with placeholders
        const codeBlocks = [];
        let hasCodeBlocks = false;
        let processed = text.replace(/```(\w*)\n([\s\S]*?)```/g, (m, lang, code) => {
            hasCodeBlocks = true;
            const placeholder = `\x00CODEBLOCK_${codeBlocks.length}\x00`;
            codeBlocks.push(`<pre><code class="language-${lang}">${escapeHtml(code.trimEnd())}</code></pre>`);
            return placeholder;
        });

        if (!hasCodeBlocks) {
            // No code blocks: apply displayText analyzer and return plain text for textContent
            text = displayText(text);
            return { html: null, text: text };
        }

        // Has code blocks: need to use innerHTML with proper escaping
        // Apply displayText analyzer to the processed text before HTML escaping
        processed = displayText(processed);
        let html = processed;
        // Escape HTML entities in the remaining text
        html = html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

        // Restore code block placeholders
        codeBlocks.forEach((block, i) => {
            html = html.replace(`\x00CODEBLOCK_${i}\x00`, block);
        });

        // Handle inline code
        html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
        // Handle bold
        html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        // Handle italic
        html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');

        return { html: html, text: null };
    }

    function addUserMessage(text) {
        const el = document.createElement('div');
        el.className = 'message user-message';
        el.innerHTML = `<div class="message-content"><p>${escapeHtml(text)}</p></div>`;
        messageContainer.appendChild(el);
        scrollToBottom();
    }

    function createAiMessageElement() {
        const el = document.createElement('div');
        el.className = 'message ai-message';
        el.innerHTML = '<div class="message-content"></div>';
        return el;
    }

    function addSystemMessage(text) {
        const el = document.createElement('div');
        el.className = 'message system-message';
        el.innerHTML = `<div class="message-content"><p>${escapeHtml(text)}</p></div>`;
        messageContainer.appendChild(el);
        scrollToBottom();
    }

    function addStatusMessage(text) {
        const el = document.createElement('div');
        el.className = 'message ai-message status-message';
        el.innerHTML = '<div class="status-indicator-inline"><span class="status-spinner"></span><span class="status-text-inline">' + escapeHtml(text) + '</span></div>';
        messageContainer.appendChild(el);
        scrollToBottom();
        return el;
    }

    function showTypingIndicator() {
        const el = document.createElement('div');
        el.className = 'message ai-message';
        el.innerHTML = '<div class="typing-indicator"><span></span><span></span><span></span></div>';
        messageContainer.appendChild(el);
        scrollToBottom();
        return el;
    }

    async function clearChat() {
        if (sessionId) {
            try { await AiApi.clearConversation(sessionId); } catch (e) { console.error(e); }
        }
        sessionId = null;
        sessionIdDisplay.textContent = '';
        messageContainer.innerHTML = '<div class="message system-message"><div class="message-content"><p>Chat cleared. Start a new conversation.</p></div></div>';
    }

    function scrollToBottom() {
        messageContainer.scrollTop = messageContainer.scrollHeight;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function generateSessionId() {
        return 'session-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
    }

    function formatTime(dateStr) {
        if (!dateStr) return '-';
        const d = new Date(dateStr);
        return d.toLocaleDateString() + ' ' + d.toLocaleTimeString();
    }

    // ==================== Register Project ====================
    async function registerProject() {
        const name = document.getElementById('projectName').value.trim();
        const path = document.getElementById('projectPath').value.trim();

        if (!name || !path) {
            alert('Please enter both project name and path');
            return;
        }

        try {
            const result = await AiApi.registerProject(name, path);
            addSystemMessage(`Project "${result.name}" registered with ${result.fileCount} files.`);
            registerModal.classList.remove('active');
            document.getElementById('projectName').value = '';
            document.getElementById('projectPath').value = '';
            await loadProjects();
            projectSelect.value = result.id;
            onProjectSelected(result.id);
        } catch (error) {
            alert('Failed to register project: ' + error.message);
        }
    }

    // ==================== Voice ====================
    function setupVoice() {
        if (!AiVoice.isSupported()) {
            if (micBtn) micBtn.title = 'Voice input not supported in this browser';
            return;
        }
        if (micBtn) micBtn.disabled = false;

        window.onTranscriptionComplete = function(text, language) {
            if (text && text.trim()) {
                messageInput.value = text.trim();
                currentLanguage = language;
                lastInputType = 'voice';
                updateLanguageBadge(language);
                messageInput.focus();
                addSystemMessage(`🎤 Transcribed (${getLanguageName(language)}): "${text.trim()}"`);
            }
        };

        window.onTranscriptionError = function(error) {
            addSystemMessage(`⚠️ Transcription error: ${error}`);
        };
    }

    async function toggleRecording() {
        if (AiVoice.getRecordingState()) {
            AiVoice.stopRecording();
            recordingIndicator.style.display = 'none';
            micBtn.classList.remove('recording');
            micBtn.innerHTML = '<span class="mic-icon">🎤</span>';
        } else {
            try {
                await AiVoice.startRecording();
                recordingIndicator.style.display = 'flex';
                micBtn.classList.add('recording');
                micBtn.innerHTML = '<span class="mic-icon">⏹️</span>';
            } catch (error) {
                addSystemMessage(`⚠️ ${error.message}`);
            }
        }
    }

    function updateLanguageBadge(language) {
        if (languageBadge && language) {
            languageBadge.textContent = getLanguageName(language);
            currentLanguage = language;
        }
    }

    function getLanguageName(code) {
        const names = {
            'en': 'English', 'zh': 'Chinese', 'ja': 'Japanese', 'ko': 'Korean',
            'fr': 'French', 'de': 'German', 'es': 'Spanish', 'pt': 'Portuguese',
            'ru': 'Russian', 'ar': 'Arabic', 'hi': 'Hindi', 'it': 'Italian',
            'nl': 'Dutch', 'tr': 'Turkish', 'zh-TW': 'Chinese (Trad)'
        };
        return names[code] || code || 'Auto';
    }

    // Initialize on page load
    document.addEventListener('DOMContentLoaded', init);
})();
