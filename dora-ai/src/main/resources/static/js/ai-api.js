/**
 * DoraAI API Client
 * Handles all REST API communication with the backend.
 */

const AiApi = {
    baseUrl: '/api/ai',

    /**
     * Check Ollama status
     */
    async getStatus() {
        const response = await fetch(`${this.baseUrl}/chat/status`);
        return response.json();
    },

    /**
     * Get available AI providers
     */
    async getProviders() {
        const response = await fetch(`${this.baseUrl}/chat/providers`);
        return response.json();
    },

    /**
     * List registered projects
     */
    async listProjects() {
        const response = await fetch(`${this.baseUrl}/projects`);
        return response.json();
    },

    /**
     * Register a new project
     */
    async registerProject(name, path) {
        const response = await fetch(`${this.baseUrl}/projects`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, path })
        });
        if (!response.ok) {
            throw new Error('Failed to register project');
        }
        return response.json();
    },

    /**
     * Remove a project
     */
    async removeProject(projectId) {
        const response = await fetch(`${this.baseUrl}/projects/${projectId}`, {
            method: 'DELETE'
        });
        return response.ok;
    },

    /**
     * Search symbols in a project
     */
    async searchSymbols(projectId, query) {
        const response = await fetch(
            `${this.baseUrl}/projects/${projectId}/search?query=${encodeURIComponent(query)}`
        );
        return response.json();
    },

    /**
     * Get file content from a project
     */
    async getFileContent(projectId, filePath) {
        const response = await fetch(
            `${this.baseUrl}/projects/${projectId}/file?filePath=${encodeURIComponent(filePath)}`
        );
        if (!response.ok) return null;
        return response.text();
    },

    /**
     * List files in a project
     */
    async listFiles(projectId) {
        const response = await fetch(`${this.baseUrl}/projects/${projectId}/files`);
        return response.json();
    },

    /**
     * Get file preview
     */
    async getFilePreview(projectId, filePath, maxLines) {
        const response = await fetch(
            `${this.baseUrl}/projects/${projectId}/file/preview?filePath=${encodeURIComponent(filePath)}&maxLines=${maxLines || 50}`
        );
        if (!response.ok) return null;
        return response.json();
    },

    /**
     * Send a chat message and return an EventSource for streaming
     */
    sendChatMessage(sessionId, message, mode, projectId, sourceLanguage, inputType, fileContext, providerId) {
        return fetch(`${this.baseUrl}/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId, message, mode, projectId, sourceLanguage, inputType, fileContext, providerId })
        });
    },

    /**
     * Clear conversation history
     */
    async clearConversation(sessionId) {
        const response = await fetch(`${this.baseUrl}/chat/${sessionId}`, {
            method: 'DELETE'
        });
        return response.ok;
    },

    /**
     * Analyze code for bugs
     */
    analyzeBugs(projectId, filePaths, description, fileContext) {
        return fetch(`${this.baseUrl}/analyze/bugs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ projectId, filePaths, description, mode: 'bugs', fileContext })
        });
    },

    /**
     * Review code
     */
    reviewCode(projectId, filePaths, description, fileContext) {
        return fetch(`${this.baseUrl}/analyze/review`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ projectId, filePaths, description, mode: 'review', fileContext })
        });
    },

    /**
     * Generate code
     */
    generateCode(projectId, description, context, language, fileContext) {
        return fetch(`${this.baseUrl}/generate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ projectId, description, context, language, fileContext })
        });
    },

    /**
     * Transcribe audio to text
     */
    async transcribeAudio(audioBase64, language) {
        const response = await fetch(`${this.baseUrl}/voice/transcribe`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ audioBase64, language })
        });
        if (!response.ok) throw new Error('Transcription failed');
        return response.json();
    },

    /**
     * Translate text between languages
     */
    async translateText(text, sourceLanguage, targetLanguage) {
        const response = await fetch(`${this.baseUrl}/voice/translate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text, sourceLanguage, targetLanguage })
        });
        if (!response.ok) throw new Error('Translation failed');
        return response.json();
    },

    /**
     * Get supported languages
     */
    async getLanguages() {
        const response = await fetch(`${this.baseUrl}/voice/languages`);
        return response.json();
    },

    // ==================== Task API ====================

    /**
     * List all tasks
     */
    async listTasks() {
        const response = await fetch(`${this.baseUrl}/tasks`);
        return response.json();
    },

    /**
     * Get a single task
     */
    async getTask(taskId) {
        const response = await fetch(`${this.baseUrl}/tasks/${taskId}`);
        return response.json();
    },

    /**
     * Create a task
     */
    async createTask(task) {
        const response = await fetch(`${this.baseUrl}/tasks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(task)
        });
        if (!response.ok) throw new Error('Failed to create task');
        return response.json();
    },

    /**
     * Perform task action (accept, start, complete, decline)
     */
    async taskAction(taskId, action) {
        const response = await fetch(`${this.baseUrl}/tasks/${taskId}/${action}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error(`Failed to ${action} task`);
        return response.json();
    },

    /**
     * Update task status
     */
    async updateTaskStatus(taskId, status) {
        const response = await fetch(`${this.baseUrl}/tasks/${taskId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if (!response.ok) throw new Error('Failed to update status');
        return response.json();
    },

    /**
     * Set task note
     */
    async setTaskNote(taskId, note) {
        const response = await fetch(`${this.baseUrl}/tasks/${taskId}/note`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ note })
        });
        if (!response.ok) throw new Error('Failed to set note');
        return response.json();
    },

    /**
     * Remove a task
     */
    async removeTask(taskId) {
        const response = await fetch(`${this.baseUrl}/tasks/${taskId}`, {
            method: 'DELETE'
        });
        return response.ok;
    },

    /**
     * Clear completed tasks
     */
    async clearCompletedTasks() {
        const response = await fetch(`${this.baseUrl}/tasks/clear-completed`, {
            method: 'DELETE'
        });
        return response.ok;
    },

    /**
     * Get task counts
     */
    async getTaskCounts() {
        const response = await fetch(`${this.baseUrl}/tasks/counts`);
        return response.json();
    },

    /**
     * Get tasks by status
     */
    async getTasksByStatus(status) {
        const response = await fetch(`${this.baseUrl}/tasks/status/${status}`);
        return response.json();
    }
};
