package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Response DTO for naval operations.
 */
public class NavalResponse {

    private String operation;
    private String fleetName;
    private String outcome;
    private int totalRounds;
    private int attackerDamage;
    private int defenderDamage;
    private int attackerShipsLost;
    private int defenderShipsLost;
    private int attackerShipsRemaining;
    private int defenderShipsRemaining;
    private List<String> squadronNames;
    private String formation;
    private String action;
    private List<String> eventLog;
    private String message;

    public NavalResponse() {
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getFleetName() { return fleetName; }
    public void setFleetName(String fleetName) { this.fleetName = fleetName; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public int getAttackerDamage() { return attackerDamage; }
    public void setAttackerDamage(int attackerDamage) { this.attackerDamage = attackerDamage; }

    public int getDefenderDamage() { return defenderDamage; }
    public void setDefenderDamage(int defenderDamage) { this.defenderDamage = defenderDamage; }

    public int getAttackerShipsLost() { return attackerShipsLost; }
    public void setAttackerShipsLost(int attackerShipsLost) { this.attackerShipsLost = attackerShipsLost; }

    public int getDefenderShipsLost() { return defenderShipsLost; }
    public void setDefenderShipsLost(int defenderShipsLost) { this.defenderShipsLost = defenderShipsLost; }

    public int getAttackerShipsRemaining() { return attackerShipsRemaining; }
    public void setAttackerShipsRemaining(int attackerShipsRemaining) { this.attackerShipsRemaining = attackerShipsRemaining; }

    public int getDefenderShipsRemaining() { return defenderShipsRemaining; }
    public void setDefenderShipsRemaining(int defenderShipsRemaining) { this.defenderShipsRemaining = defenderShipsRemaining; }

    public List<String> getSquadronNames() { return squadronNames; }
    public void setSquadronNames(List<String> squadronNames) { this.squadronNames = squadronNames; }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public List<String> getEventLog() { return eventLog; }
    public void setEventLog(List<String> eventLog) { this.eventLog = eventLog; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
