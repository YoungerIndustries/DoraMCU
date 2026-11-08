package com.yisy.modules.doraai.dto;

/**
 * Request to register a new project for AI code analysis.
 */
public class ProjectRequest {

    private String name;
    private String path;

    public ProjectRequest() {
    }

    public ProjectRequest(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
