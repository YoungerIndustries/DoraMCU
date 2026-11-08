package com.yisy.modules.doraai.dto;

import java.util.List;

/**
 * Request for code analysis (bug detection or code review).
 */
public class AnalysisRequest {

    private String projectId;
    private List<String> filePaths;
    private String description;
    private String code;

    public AnalysisRequest() {
    }

    public AnalysisRequest(String projectId, List<String> filePaths, String description) {
        this.projectId = projectId;
        this.filePaths = filePaths;
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
