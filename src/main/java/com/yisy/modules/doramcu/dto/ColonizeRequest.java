package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to colonize (claim ownership of) a planet.
 */
public class ColonizeRequest {

    @NotNull
    private Integer planetId;

    public Integer getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Integer planetId) {
        this.planetId = planetId;
    }
}
