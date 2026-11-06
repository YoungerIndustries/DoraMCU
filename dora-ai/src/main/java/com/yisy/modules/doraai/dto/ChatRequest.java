package com.yisy.modules.doraai.dto;

/**
 * Request to send a chat message to the AI.
 */
public class ChatRequest {

    private String sessionId;
    private String message;
    private String mode; // chat | code-gen | bug-detect
    private String projectId;
    private String sourceLanguage; // e.g., "en", "zh" — detected or user-specified language
    private String inputType; // "text" | "voice" — how the input was provided
    private String fileContext; // Pre-fetched file contents for AI context
    private String providerId; // AI provider ID (e.g., "ollama", "gemini")

    public ChatRequest() {
    }

    public ChatRequest(String sessionId, String message, String mode, String projectId) {
        this.sessionId = sessionId;
        this.message = message;
        this.mode = mode;
        this.projectId = projectId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getFileContext() {
        return fileContext;
    }

    public void setFileContext(String fileContext) {
        this.fileContext = fileContext;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
