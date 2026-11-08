package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to build a space station.
 */
public class BuildStationRequest {

    @NotNull
    private String stationType;  // MILITARY, SHIPYARD, MINING, RESEARCH, TRADE, COLONY

    private Integer planetId;    // Optional: planet in the system where the station will orbit

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public Integer getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Integer planetId) {
        this.planetId = planetId;
    }
}
