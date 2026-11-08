package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;

/**
 * DTO for requesting a starship system task attempt using the 2d20 system.
 * <p>
 * In the Captain's Log system, starship task attempts use:
 * one System + one Department to calculate the Target Number.
 * A failed task attempt adds stress to the system being used.
 * </p>
 */
public class StarshipTaskAttemptRequest {

    /** Starship ID to look up stats for. */
    private Long starshipId;

    /** Optional mission ID — if provided, the mission's momentum/threat pools are updated. */
    private Long missionId;

    /** System used for this task (e.g., COMMS, SENSORS, WEAPONS, etc.). */
    private CaptainLogStarship.StarshipSystem system;

    /** Department used for this task (e.g., COMMAND, SCIENCE, etc.). */
    private CaptainLogStarship.StarshipDepartment department;

    /** Number of successes needed (default 1). */
    private int difficulty = 1;

    /** Number of boons (extra dice, keep best 2). */
    private int boons = 0;

    /** Number of threats (extra dice, keep best 2). */
    private int threats = 0;

    /** Whether to spend 1 momentum to reroll one die. */
    private boolean spendMomentumReroll = false;

    /** Whether to accept a complication (spend 1 threat for a boon). */
    private boolean spendThreatForBoon = false;

    /**
     * If true, a failed task attempt adds stress to the system being used.
     * Default is true — systems take stress on failure per the rulebook.
     */
    private boolean applyStressOnFailure = true;

    // --- Getters and Setters ---

    public Long getStarshipId() {
        return starshipId;
    }

    public void setStarshipId(Long starshipId) {
        this.starshipId = starshipId;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public CaptainLogStarship.StarshipSystem getSystem() {
        return system;
    }

    public void setSystem(CaptainLogStarship.StarshipSystem system) {
        this.system = system;
    }

    public CaptainLogStarship.StarshipDepartment getDepartment() {
        return department;
    }

    public void setDepartment(CaptainLogStarship.StarshipDepartment department) {
        this.department = department;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
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

    public boolean isApplyStressOnFailure() {
        return applyStressOnFailure;
    }

    public void setApplyStressOnFailure(boolean applyStressOnFailure) {
        this.applyStressOnFailure = applyStressOnFailure;
    }
}
