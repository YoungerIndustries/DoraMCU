package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request DTO for initiating a ship-to-ship battle.
 * <p>
 * Contains the parameters needed to create and start a battle
 * between two fleets of Traveller-designed ships.
 * </p>
 */
public class BattleRequest {

    /**
     * Name of the battle (for logging/display purposes).
     */
    @NotNull
    private String battleName;

    /**
     * ID of the attacker's ship design.
     */
    @Positive
    private int attackerDesignId;

    /**
     * ID of the defender's ship design.
     */
    @Positive
    private int defenderDesignId;

    /**
     * Number of attacker ships (creates a fleet of this size).
     */
    @Positive
    private int attackerShipCount = 1;

    /**
     * Number of defender ships (creates a fleet of this size).
     */
    @Positive
    private int defenderShipCount = 1;

    /**
     * If true, fights the battle to completion immediately.
     * If false, creates the battle for round-by-round play.
     */
    private boolean fightToCompletion = true;

    // ---- Default constructor for Jackson ----

    public BattleRequest() {
    }

    public BattleRequest(String battleName, int attackerDesignId, int defenderDesignId,
                          int attackerShipCount, int defenderShipCount, boolean fightToCompletion) {
        this.battleName = battleName;
        this.attackerDesignId = attackerDesignId;
        this.defenderDesignId = defenderDesignId;
        this.attackerShipCount = attackerShipCount;
        this.defenderShipCount = defenderShipCount;
        this.fightToCompletion = fightToCompletion;
    }

    // ---- Accessors ----

    public String getBattleName() {
        return battleName;
    }

    public void setBattleName(String battleName) {
        this.battleName = battleName;
    }

    public int getAttackerDesignId() {
        return attackerDesignId;
    }

    public void setAttackerDesignId(int attackerDesignId) {
        this.attackerDesignId = attackerDesignId;
    }

    public int getDefenderDesignId() {
        return defenderDesignId;
    }

    public void setDefenderDesignId(int defenderDesignId) {
        this.defenderDesignId = defenderDesignId;
    }

    public int getAttackerShipCount() {
        return attackerShipCount;
    }

    public void setAttackerShipCount(int attackerShipCount) {
        this.attackerShipCount = attackerShipCount;
    }

    public int getDefenderShipCount() {
        return defenderShipCount;
    }

    public void setDefenderShipCount(int defenderShipCount) {
        this.defenderShipCount = defenderShipCount;
    }

    public boolean isFightToCompletion() {
        return fightToCompletion;
    }

    public void setFightToCompletion(boolean fightToCompletion) {
        this.fightToCompletion = fightToCompletion;
    }
}
