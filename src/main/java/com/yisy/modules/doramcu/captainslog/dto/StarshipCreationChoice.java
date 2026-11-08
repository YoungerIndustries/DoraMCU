package com.yisy.modules.doramcu.captainslog.dto;

import java.util.List;
import java.util.Map;

/**
 * A choice option in the starship creation wizard.
 */
public class StarshipCreationChoice {

    private String id;
    private String title;
    private String description;
    private Map<String, Integer> systems;
    private Map<String, Integer> departments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Integer> getSystems() {
        return systems;
    }

    public void setSystems(Map<String, Integer> systems) {
        this.systems = systems;
    }

    public Map<String, Integer> getDepartments() {
        return departments;
    }

    public void setDepartments(Map<String, Integer> departments) {
        this.departments = departments;
    }
}
