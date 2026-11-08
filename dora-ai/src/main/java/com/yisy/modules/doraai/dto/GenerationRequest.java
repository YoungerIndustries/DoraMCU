package com.yisy.modules.doraai.dto;

/**
 * Request for AI-assisted code generation.
 */
public class GenerationRequest {

    private String projectId;
    private String description;
    private String language;
    private String context;

    public GenerationRequest() {
    }

    public GenerationRequest(String projectId, String description) {
        this.projectId = projectId;
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
