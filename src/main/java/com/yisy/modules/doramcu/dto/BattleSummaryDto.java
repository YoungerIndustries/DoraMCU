package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Summary of a battle for display in the UI.
 */
public class BattleSummaryDto {

    private int battleId;
    private String state;          // INITIATED, IN_PROGRESS, COMPLETED
    private String outcome;        // ATTACKER_VICTORY, DEFENDER_VICTORY, etc. (null if in progress)
    private int currentRound;
    private int maxRounds;
    private String attackerName;
    private int attackerShipCount;
    private int attackerRemainingHull;
    private int attackerRemainingShields;
    private String defenderName;
    private int defenderShipCount;
    private int defenderRemainingHull;
    private int defenderRemainingShields;
    private int totalAttackerDamage;
    private int totalDefenderDamage;
    private String location;       // "r,c" hex position
    private List<String> events;   // Last few battle event descriptions

    public int getBattleId() {
        return battleId;
    }

    public void setBattleId(int battleId) {
        this.battleId = battleId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public String getAttackerName() {
        return attackerName;
    }

    public void setAttackerName(String attackerName) {
        this.attackerName = attackerName;
    }

    public int getAttackerShipCount() {
        return attackerShipCount;
    }

    public void setAttackerShipCount(int attackerShipCount) {
        this.attackerShipCount = attackerShipCount;
    }

    public int getAttackerRemainingHull() {
        return attackerRemainingHull;
    }

    public void setAttackerRemainingHull(int attackerRemainingHull) {
        this.attackerRemainingHull = attackerRemainingHull;
    }

    public int getAttackerRemainingShields() {
        return attackerRemainingShields;
    }

    public void setAttackerRemainingShields(int attackerRemainingShields) {
        this.attackerRemainingShields = attackerRemainingShields;
    }

    public String getDefenderName() {
        return defenderName;
    }

    public void setDefenderName(String defenderName) {
        this.defenderName = defenderName;
    }

    public int getDefenderShipCount() {
        return defenderShipCount;
    }

    public void setDefenderShipCount(int defenderShipCount) {
        this.defenderShipCount = defenderShipCount;
    }

    public int getDefenderRemainingHull() {
        return defenderRemainingHull;
    }

    public void setDefenderRemainingHull(int defenderRemainingHull) {
        this.defenderRemainingHull = defenderRemainingHull;
    }

    public int getDefenderRemainingShields() {
        return defenderRemainingShields;
    }

    public void setDefenderRemainingShields(int defenderRemainingShields) {
        this.defenderRemainingShields = defenderRemainingShields;
    }

    public int getTotalAttackerDamage() {
        return totalAttackerDamage;
    }

    public void setTotalAttackerDamage(int totalAttackerDamage) {
        this.totalAttackerDamage = totalAttackerDamage;
    }

    public int getTotalDefenderDamage() {
        return totalDefenderDamage;
    }

    public void setTotalDefenderDamage(int totalDefenderDamage) {
        this.totalDefenderDamage = totalDefenderDamage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
}
