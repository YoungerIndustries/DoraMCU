package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an individual log entry within a mission.
 * <p>
 * Log entries capture narrative moments, oracle queries, skill checks,
 * and story developments during a mission's acts.
 * </p>
 */
@Entity
@Table(name = "cl_log_entries", indexes = {
        @Index(name = "idx_cl_log_entry_mission", columnList = "mission_id")
})
public class CaptainLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private CaptainLogMission mission;

    /** Which act this entry belongs to (1, 2, or 3). */
    @Column(nullable = false)
    private int act;

    /** Entry type (NARRATIVE, ORACLE_QUERY, SKILL_CHECK, COMBAT, DISCOVERY). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntryType type;

    /** The log entry text — narrative description or query. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    /** If this is a skill check, which attribute was used. */
    @Column(length = 20)
    private String attribute;

    /** If this is a skill check, which discipline was used. */
    @Column(length = 20)
    private String discipline;

    /** If this is a skill check, the target number (attribute + discipline). */
    private Integer targetNumber;

    /** If this is a skill check, the difficulty (successes needed). */
    private Integer difficulty;

    /** If this is a skill check, the dice values rolled (comma-separated). */
    @Column(length = 200)
    private String diceRolled;

    /** If this is a skill check, number of boon dice applied. */
    private Integer boons;

    /** If this is a skill check, number of threat dice applied. */
    private Integer threats;

    /** If this is a skill check, total successes counted. */
    private Integer successes;

    /** If this is a skill check, the roll result (sum of base dice, for legacy compatibility). */
    private Integer rollResult;

    /** If this is a skill check, whether it succeeded. */
    private Boolean success;

    /** Momentum generated from excess successes (0 or 1). */
    private Integer momentumGenerated;

    /** Threat generated from complications (natural 20s). */
    private Integer threatGenerated;

    /** Whether a complication occurred (natural 20 rolled). */
    private Boolean complication;

    /** Description of the complication, if any. */
    @Column(length = 500)
    private String complicationDescription;

    /** If this is an oracle query, the question asked. */
    @Column(length = 500)
    private String oracleQuestion;

    /** If this is an oracle query, the answer (YES, NO, BOTH, NEITHER, PLUS, MINUS, VARIABLE). */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private OracleAnswer oracleAnswer;

    /** When this entry was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Types of log entries.
     */
    public enum EntryType {
        NARRATIVE, ORACLE_QUERY, SKILL_CHECK, COMBAT, DISCOVERY, CHARACTER_MOMENT
    }

    /**
     * Oracle answers in the 2d20 solo RPG system.
     */
    public enum OracleAnswer {
        YES, NO, BOTH, NEITHER, YES_PLUS, YES_MINUS, NO_PLUS, NO_MINUS, VARIABLE
    }

    public CaptainLogEntry() {
    }

    public CaptainLogEntry(int act, EntryType type, String text) {
        this.act = act;
        this.type = type;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CaptainLogMission getMission() {
        return mission;
    }

    public void setMission(CaptainLogMission mission) {
        this.mission = mission;
    }

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public Integer getTargetNumber() {
        return targetNumber;
    }

    public void setTargetNumber(Integer targetNumber) {
        this.targetNumber = targetNumber;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getDiceRolled() {
        return diceRolled;
    }

    public void setDiceRolled(String diceRolled) {
        this.diceRolled = diceRolled;
    }

    public Integer getBoons() {
        return boons;
    }

    public void setBoons(Integer boons) {
        this.boons = boons;
    }

    public Integer getThreats() {
        return threats;
    }

    public void setThreats(Integer threats) {
        this.threats = threats;
    }

    public Integer getSuccesses() {
        return successes;
    }

    public void setSuccesses(Integer successes) {
        this.successes = successes;
    }

    public Integer getRollResult() {
        return rollResult;
    }

    public void setRollResult(Integer rollResult) {
        this.rollResult = rollResult;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getMomentumGenerated() {
        return momentumGenerated;
    }

    public void setMomentumGenerated(Integer momentumGenerated) {
        this.momentumGenerated = momentumGenerated;
    }

    public Integer getThreatGenerated() {
        return threatGenerated;
    }

    public void setThreatGenerated(Integer threatGenerated) {
        this.threatGenerated = threatGenerated;
    }

    public Boolean getComplication() {
        return complication;
    }

    public void setComplication(Boolean complication) {
        this.complication = complication;
    }

    public String getComplicationDescription() {
        return complicationDescription;
    }

    public void setComplicationDescription(String complicationDescription) {
        this.complicationDescription = complicationDescription;
    }

    public String getOracleQuestion() {
        return oracleQuestion;
    }

    public void setOracleQuestion(String oracleQuestion) {
        this.oracleQuestion = oracleQuestion;
    }

    public OracleAnswer getOracleAnswer() {
        return oracleAnswer;
    }

    public void setOracleAnswer(OracleAnswer oracleAnswer) {
        this.oracleAnswer = oracleAnswer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CaptainLogEntry{act=" + act + ", type=" + type + "}";
    }
}
