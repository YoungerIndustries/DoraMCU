package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for naval operations.
 */
public class NavalRequest {

    public enum Operation {
        CREATE_FLEET,
        CREATE_SQUADRON,
        FIGHT_BATTLE,
        GET_FLEET_STATUS
    }

    @NotNull
    private Operation operation;

    private String fleetName;
    private String squadronName;

    @Positive
    private int attackerFleetId;

    @Positive
    private int defenderFleetId;

    @Positive
    private int fleetId;

    private String formation;
    private String action;

    @Positive
    private int shipDesignId;

    @Positive
    private int shipCount = 1;

    public NavalRequest() {
    }

    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }

    public String getFleetName() { return fleetName; }
    public void setFleetName(String fleetName) { this.fleetName = fleetName; }

    public String getSquadronName() { return squadronName; }
    public void setSquadronName(String squadronName) { this.squadronName = squadronName; }

    public int getAttackerFleetId() { return attackerFleetId; }
    public void setAttackerFleetId(int attackerFleetId) { this.attackerFleetId = attackerFleetId; }

    public int getDefenderFleetId() { return defenderFleetId; }
    public void setDefenderFleetId(int defenderFleetId) { this.defenderFleetId = defenderFleetId; }

    public int getFleetId() { return fleetId; }
    public void setFleetId(int fleetId) { this.fleetId = fleetId; }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getShipDesignId() { return shipDesignId; }
    public void setShipDesignId(int shipDesignId) { this.shipDesignId = shipDesignId; }

    public int getShipCount() { return shipCount; }
    public void setShipCount(int shipCount) { this.shipCount = shipCount; }
}
