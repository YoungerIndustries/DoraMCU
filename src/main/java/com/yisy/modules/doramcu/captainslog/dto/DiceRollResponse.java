package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;

/**
 * DTO representing a 2d20 dice roll result.
 * <p>
 * In the Modiphius 2d20 system, two d20 dice are rolled:
 * - One determines success/failure (compared to target number)
 * - One determines degree of success (boons/threats modify this)
 * - Critical success (20+), success, failure, critical failure (5 or less)
 * </p>
 */
public class DiceRollResponse {

    /** The two d20 dice values. */
    private int die1;
    private int die2;

    /** Total roll value (die1 + die2 + modifiers). */
    private int total;

    /** Target number for the check. */
    private int targetNumber;

    /** Number of boons (+1 dice rolled, keep highest 2). */
    private int boons;

    /** Number of threats (-1 dice rolled, keep highest 2). */
    private int threats;

    /** Net modifier from boons and threats. */
    private int modifier;

    /** Whether the check succeeded. */
    private boolean success;

    /** Whether this is a critical success (total >= 20). */
    private boolean criticalSuccess;

    /** Whether this is a critical failure (total <= 5). */
    private boolean criticalFailure;

    /** Oracle answer if this was an oracle query. */
    private CaptainLogEntry.OracleAnswer oracleAnswer;

    /** Human-readable description of the result. */
    private String description;

    public int getDie1() {
        return die1;
    }

    public void setDie1(int die1) {
        this.die1 = die1;
    }

    public int getDie2() {
        return die2;
    }

    public void setDie2(int die2) {
        this.die2 = die2;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public void setTargetNumber(int targetNumber) {
        this.targetNumber = targetNumber;
    }

    public int getBoons() {
        return boons;
    }

    public void setBoons(int boons) {
        this.boons = boons;
    }

    public int getThreats() {
        return threats;
    }

    public void setThreats(int threats) {
        this.threats = threats;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCriticalSuccess() {
        return criticalSuccess;
    }

    public void setCriticalSuccess(boolean criticalSuccess) {
        this.criticalSuccess = criticalSuccess;
    }

    public boolean isCriticalFailure() {
        return criticalFailure;
    }

    public void setCriticalFailure(boolean criticalFailure) {
        this.criticalFailure = criticalFailure;
    }

    public CaptainLogEntry.OracleAnswer getOracleAnswer() {
        return oracleAnswer;
    }

    public void setOracleAnswer(CaptainLogEntry.OracleAnswer oracleAnswer) {
        this.oracleAnswer = oracleAnswer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DiceRollResponse{d1=" + die1 + ", d2=" + die2 + ", total=" + total +
                ", target=" + targetNumber + ", success=" + success + "}";
    }
}
