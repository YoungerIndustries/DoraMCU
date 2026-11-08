package com.yisy.modules.doramcu.captainslog.dto;

/**
 * DTO representing the result of a single combat round.
 */
public class CombatRoundResponse {

    /** Combat ID. */
    private Long combatId;

    /** Round number. */
    private int round;

    /** Task attempt result for this round. */
    private TaskAttemptResponse taskAttempt;

    /** Character hits after this round. */
    private int characterHits;

    /** Character max hits. */
    private int characterMaxHits;

    /** Opponent hits after this round. */
    private int opponentHits;

    /** Opponent max hits. */
    private int opponentMaxHits;

    /** Whether the combat has ended. */
    private boolean combatEnded;

    /** Combat status: ACTIVE, VICTORY, DEFEAT, RETREAT. */
    private String status;

    /** Narrative description of the round. */
    private String description;

    // --- Getters and Setters ---

    public Long getCombatId() {
        return combatId;
    }

    public void setCombatId(Long combatId) {
        this.combatId = combatId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public TaskAttemptResponse getTaskAttempt() {
        return taskAttempt;
    }

    public void setTaskAttempt(TaskAttemptResponse taskAttempt) {
        this.taskAttempt = taskAttempt;
    }

    public int getCharacterHits() {
        return characterHits;
    }

    public void setCharacterHits(int characterHits) {
        this.characterHits = characterHits;
    }

    public int getCharacterMaxHits() {
        return characterMaxHits;
    }

    public void setCharacterMaxHits(int characterMaxHits) {
        this.characterMaxHits = characterMaxHits;
    }

    public int getOpponentHits() {
        return opponentHits;
    }

    public void setOpponentHits(int opponentHits) {
        this.opponentHits = opponentHits;
    }

    public int getOpponentMaxHits() {
        return opponentMaxHits;
    }

    public void setOpponentMaxHits(int opponentMaxHits) {
        this.opponentMaxHits = opponentMaxHits;
    }

    public boolean isCombatEnded() {
        return combatEnded;
    }

    public void setCombatEnded(boolean combatEnded) {
        this.combatEnded = combatEnded;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
