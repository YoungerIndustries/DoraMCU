package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to create a new fleet.
 */
public class CreateFleetRequest {

    @NotBlank
    private String fleetName;

    public String getFleetName() {
        return fleetName;
    }

    public void setFleetName(String fleetName) {
        this.fleetName = fleetName;
    }
}
