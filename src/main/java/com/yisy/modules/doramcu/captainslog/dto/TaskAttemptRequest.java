package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

/**
 * DTO for requesting a task attempt using the 2d20 system.
 * <p>
 * In the Captain's Log / Star Trek Adventures system, a task attempt uses:
 * one Attribute + one Discipline to calculate the Target Number.
 * The difficulty determines how many successes are needed.
 * </p>
 */
public class TaskAttemptRequest {

    /** Character ID to look up stats for. */
    private Long characterId;

    /** Optional mission ID — if provided, the mission's momentum/threat pools are updated. */
    private Long missionId;

    /** Attribute used for this task (e.g., FITNESS, TECHNICAL, etc.). */
    private CaptainLogCharacter.Attribute attribute;

    /** Discipline used for this task (e.g., COMMAND, SCIENCE, etc.). */
    private CaptainLogCharacter.Discipline discipline;

    /** Number of successes needed (default 1). */
    private int difficulty = 1;

    /** Optional focus name — if it matches a character focus, dice ≤ discipline value count as 2 successes. */
    private String focus;

    /** Number of boons (extra dice, keep best 2). */
    private int boons = 0;

    /** Number of threats (extra dice, keep best 2). */
    private int threats = 0;

    /** Whether to spend 1 momentum to reroll one die. */
    private boolean spendMomentumReroll = false;

    /** Whether to accept a complication (spend 1 threat for a boon). */
    private boolean spendThreatForBoon = false;

    // --- Getters and Setters ---

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public CaptainLogCharacter.Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(CaptainLogCharacter.Attribute attribute) {
        this.attribute = attribute;
    }

    public CaptainLogCharacter.Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(CaptainLogCharacter.Discipline discipline) {
        this.discipline = discipline;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
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

    public boolean isSpendMomentumReroll() {
        return spendMomentumReroll;
    }

    public void setSpendMomentumReroll(boolean spendMomentumReroll) {
        this.spendMomentumReroll = spendMomentumReroll;
    }

    public boolean isSpendThreatForBoon() {
        return spendThreatForBoon;
    }

    public void setSpendThreatForBoon(boolean spendThreatForBoon) {
        this.spendThreatForBoon = spendThreatForBoon;
    }
}
