package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EngageCombatRequest {

    @NotNull(message = "Attacker fleet ID is required")
    @Min(value = 1, message = "Attacker fleet ID must be at least 1")
    private Integer attackerFleetId;
    @NotNull(message = "Defender fleet ID is required")
    @Min(value = 1, message = "Defender fleet ID must be at least 1")
    private Integer defenderFleetId;

    public EngageCombatRequest() {
    }

    public EngageCombatRequest(Integer attackerFleetId, Integer defenderFleetId) {
        this.attackerFleetId = attackerFleetId;
        this.defenderFleetId = defenderFleetId;
    }

    public Integer getAttackerFleetId() {
        return attackerFleetId;
    }

    public void setAttackerFleetId(Integer attackerFleetId) {
        this.attackerFleetId = attackerFleetId;
    }

    public Integer getDefenderFleetId() {
        return defenderFleetId;
    }

    public void setDefenderFleetId(Integer defenderFleetId) {
        this.defenderFleetId = defenderFleetId;
    }
}
