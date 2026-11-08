package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to reinforce a battle with a fleet.
 */
public class ReinforceBattleRequest {

    @NotNull
    private Integer fleetId;

    public Integer getFleetId() {
        return fleetId;
    }

    public void setFleetId(Integer fleetId) {
        this.fleetId = fleetId;
    }
}
