package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to build a planetary facility.
 */
public class BuildFacilityRequest {

    @NotNull
    private Integer planetId;

    @NotNull
    private String facilityType;  // COLONY_DOME, SHIPYARD, SENSOR_ARRAY, MINING_FACILITY,
                                  // POWER_PLANT, DEFENSE_TURRET, RESEARCH_LAB, TRADE_PORT,
                                  // MILITARY_BASE, AGRICULTURAL_CENTER

    public Integer getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Integer planetId) {
        this.planetId = planetId;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }
}
