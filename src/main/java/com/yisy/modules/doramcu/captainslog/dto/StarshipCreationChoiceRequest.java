package com.yisy.modules.doramcu.captainslog.dto;

import java.util.List;
import java.util.Map;

/**
 * Request to make a choice in the starship creation wizard.
 */
public class StarshipCreationChoiceRequest {

    private String choice;
    private String name;
    private String registry;
    private Map<String, Integer> systems;
    private Map<String, Integer> departments;
    private List<String> traits;
    private List<String> talents;

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
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

    public List<String> getTraits() {
        return traits;
    }

    public void setTraits(List<String> traits) {
        this.traits = traits;
    }

    public List<String> getTalents() {
        return talents;
    }

    public void setTalents(List<String> talents) {
        this.talents = talents;
    }
}
