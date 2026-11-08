package com.yisy.modules.doramcu.captainslog.dto;

import java.util.List;

/**
 * DTO representing a 2d20 task attempt result.
 * <p>
 * In the Captain's Log / Star Trek Adventures system:
 * <ul>
 *   <li>Each die is compared individually to the Target Number (Attribute + Discipline)</li>
 *   <li>A die ≤ Target Number counts as 1 success</li>
 *   <li>A die rolling exactly 1 always counts as 2 successes</li>
 *   <li>If a Focus applies, a die ≤ Discipline value counts as 2 successes</li>
 *   <li>A die rolling exactly 20 is a complication (generates Threat), not a success</li>
 *   <li>Successes ≥ Difficulty = task succeeded</li>
 *   <li>Excess successes generate Momentum (max 1 in Captain's Log)</li>
 * </ul>
 * </p>
 */
public class TaskAttemptResponse {

    /** The target number (Attribute + Discipline). */
    private int targetNumber;

    /** Number of successes needed (difficulty). */
    private int difficulty;

    /** List of all dice rolled. */
    private List<Integer> dice;

    /** Number of boon dice rolled (extra dice, keep best 2). */
    private int boons;

    /** Number of threat dice rolled (extra dice, keep best 2). */
    private int threats;

    /** Total successes counted. */
    private int successes;

    /** Whether the task succeeded (successes ≥ difficulty). */
    private boolean success;

    /** Momentum generated from excess successes (0 or 1, capped at 1 in Captain's Log). */
    private int momentumGenerated;

    /** Threat generated from complications (natural 20s). */
    private int threatGenerated;

    /** Whether a natural 20 was rolled (complication occurred). */
    private boolean complication;

    /** Description of the complication if any. */
    private String complicationDescription;

    /** Human-readable description of the result. */
    private String description;

    /** Suggested difficulty based on mission context (mission number, rank, type). Null if not calculated. */
    private Integer suggestedDifficulty;

    /** Suggested threats based on mission context. Null if not calculated. */
    private Integer suggestedThreats;

    /** Human-readable explanation of the difficulty scaling. Null if not calculated. */
    private String difficultyDescription;

    // --- Getters and Setters ---

    public int getTargetNumber() {
        return targetNumber;
    }

    public void setTargetNumber(int targetNumber) {
        this.targetNumber = targetNumber;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public List<Integer> getDice() {
        return dice;
    }

    public void setDice(List<Integer> dice) {
        this.dice = dice;
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

    public int getSuccesses() {
        return successes;
    }

    public void setSuccesses(int successes) {
        this.successes = successes;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getMomentumGenerated() {
        return momentumGenerated;
    }

    public void setMomentumGenerated(int momentumGenerated) {
        this.momentumGenerated = momentumGenerated;
    }

    public int getThreatGenerated() {
        return threatGenerated;
    }

    public void setThreatGenerated(int threatGenerated) {
        this.threatGenerated = threatGenerated;
    }

    public boolean isComplication() {
        return complication;
    }

    public void setComplication(boolean complication) {
        this.complication = complication;
    }

    public String getComplicationDescription() {
        return complicationDescription;
    }

    public void setComplicationDescription(String complicationDescription) {
        this.complicationDescription = complicationDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSuggestedDifficulty() {
        return suggestedDifficulty;
    }

    public void setSuggestedDifficulty(Integer suggestedDifficulty) {
        this.suggestedDifficulty = suggestedDifficulty;
    }

    public Integer getSuggestedThreats() {
        return suggestedThreats;
    }

    public void setSuggestedThreats(Integer suggestedThreats) {
        this.suggestedThreats = suggestedThreats;
    }

    public String getDifficultyDescription() {
        return difficultyDescription;
    }

    public void setDifficultyDescription(String difficultyDescription) {
        this.difficultyDescription = difficultyDescription;
    }

    @Override
    public String toString() {
        return "TaskAttemptResponse{target=" + targetNumber + ", difficulty=" + difficulty +
                ", dice=" + dice + ", successes=" + successes + ", success=" + success +
                ", momentum=" + momentumGenerated + ", threat=" + threatGenerated + "}";
    }
}
