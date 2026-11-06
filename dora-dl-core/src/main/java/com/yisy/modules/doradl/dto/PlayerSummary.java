package com.yisy.modules.doradl.dto;

import java.util.Map;

public class PlayerSummary {

    private Long id;
    private String name;
    private Map<String, Integer> resources;
    private int planetCount;
    private int fleetCount;
    private boolean isAi;
    private String difficulty;

    public PlayerSummary() {
    }

    public PlayerSummary(Long id, String name, Map<String, Integer> resources, int planetCount, int fleetCount) {
        this.id = id;
        this.name = name;
        this.resources = resources;
        this.planetCount = planetCount;
        this.fleetCount = fleetCount;
    }

    public boolean isAi() {
        return isAi;
    }

    public void setAi(boolean ai) {
        isAi = ai;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Integer> getResources() {
        return resources;
    }

    public void setResources(Map<String, Integer> resources) {
        this.resources = resources;
    }

    public int getPlanetCount() {
        return planetCount;
    }

    public void setPlanetCount(int planetCount) {
        this.planetCount = planetCount;
    }

    public int getFleetCount() {
        return fleetCount;
    }

    public void setFleetCount(int fleetCount) {
        this.fleetCount = fleetCount;
    }
}
