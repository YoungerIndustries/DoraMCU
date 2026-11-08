package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an ongoing combat encounter in the Captain's Log system.
 * <p>
 * Combat follows the simplified Captain's Log rules:
 * <ul>
 *   <li>Characters and opponents have hits (health)</li>
 *   <li>If no initiative, both start with hits based on scale</li>
 *   <li>Each round: perform a task attempt</li>
 *   <li>Success = deal 1 hit to opponent</li>
 *   <li>Failure = take 1 hit yourself</li>
 *   <li>Natural 20 = complication</li>
 *   <li>Extra success = gain Momentum</li>
 *   <li>Repeat until one side is defeated</li>
 * </ul>
 * </p>
 * <p>
 * For starship combat, hits are based on scale:
 * Small=2, Medium=3, Large=4, Capital=5, Station=6.
 * Two extra hits represent critical damage to systems.
 * </p>
 */
@Entity
@Table(name = "cl_combats", indexes = {
        @Index(name = "idx_cl_combat_mission", columnList = "mission_id")
})
public class CaptainLogCombat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private CaptainLogMission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private CaptainLogCharacter character;

    /** Name of the opponent (e.g., "Klingon Warrior", "Romulan Warbird"). */
    @Column(nullable = false, length = 200)
    private String opponentName;

    /** Type of combat: PHYSICAL or STARSHIP. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CombatType type;

    /** Scale of the opponent (affects hit points in starship combat). */
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private OpponentScale opponentScale;

    /** Current hits remaining for the character. */
    @Column(nullable = false)
    private int characterHits;

    /** Maximum hits for the character. */
    @Column(nullable = false)
    private int characterMaxHits;

    /** Current hits remaining for the opponent. */
    @Column(nullable = false)
    private int opponentHits;

    /** Maximum hits for the opponent. */
    @Column(nullable = false)
    private int opponentMaxHits;

    /** Current round number. */
    @Column(nullable = false)
    private int currentRound = 0;

    /** Combat status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CombatStatus status;

    /** Attribute used for combat task attempts (e.g., FITNESS for hand-to-hand). */
    @Column(length = 20)
    private String characterAttribute;

    /** Discipline used for combat task attempts (e.g., SECURITY for combat). */
    @Column(length = 20)
    private String characterDiscipline;

    /** System used for starship combat (e.g., WEAPONS). */
    @Column(length = 20)
    private String shipSystem;

    /** Department used for starship combat (e.g., SECURITY). */
    @Column(length = 20)
    private String shipDepartment;

    /** Narrative description of the combat. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Result summary after combat ends. */
    @Column(columnDefinition = "TEXT")
    private String resultSummary;

    /** When the combat was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** When the combat ended (if applicable). */
    private LocalDateTime endedAt;

    /**
     * Types of combat encounters.
     */
    public enum CombatType {
        PHYSICAL, STARSHIP
    }

    /**
     * Scale of the opponent — determines hit points in starship combat.
     */
    public enum OpponentScale {
        SMALL(2), MEDIUM(3), LARGE(4), CAPITAL(5), STATION(6);

        private final int hits;

        OpponentScale(int hits) {
            this.hits = hits;
        }

        public int getHits() {
            return hits;
        }
    }

    /**
     * Status of a combat encounter.
     */
    public enum CombatStatus {
        ACTIVE, VICTORY, DEFEAT, RETREAT
    }

    public CaptainLogCombat() {
    }

    /**
     * Creates a new physical combat encounter.
     */
    public CaptainLogCombat(CaptainLogMission mission, CaptainLogCharacter character,
                            String opponentName, String description) {
        this.mission = mission;
        this.character = character;
        this.opponentName = opponentName;
        this.type = CombatType.PHYSICAL;
        this.description = description;
        this.status = CombatStatus.ACTIVE;
        this.characterMaxHits = 1;
        this.characterHits = 1;
        this.opponentMaxHits = 1;
        this.opponentHits = 1;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new starship combat encounter.
     */
    public CaptainLogCombat(CaptainLogMission mission, CaptainLogCharacter character,
                            String opponentName, OpponentScale scale, String description) {
        this.mission = mission;
        this.character = character;
        this.opponentName = opponentName;
        this.type = CombatType.STARSHIP;
        this.opponentScale = scale;
        this.description = description;
        this.status = CombatStatus.ACTIVE;
        this.opponentMaxHits = scale.getHits();
        this.opponentHits = scale.getHits();
        // Character ship defaults to MEDIUM scale (3 hits)
        this.characterMaxHits = 3;
        this.characterHits = 3;
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

    public CaptainLogCharacter getCharacter() {
        return character;
    }

    public void setCharacter(CaptainLogCharacter character) {
        this.character = character;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public CombatType getType() {
        return type;
    }

    public void setType(CombatType type) {
        this.type = type;
    }

    public OpponentScale getOpponentScale() {
        return opponentScale;
    }

    public void setOpponentScale(OpponentScale opponentScale) {
        this.opponentScale = opponentScale;
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

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public CombatStatus getStatus() {
        return status;
    }

    public void setStatus(CombatStatus status) {
        this.status = status;
    }

    public String getCharacterAttribute() {
        return characterAttribute;
    }

    public void setCharacterAttribute(String characterAttribute) {
        this.characterAttribute = characterAttribute;
    }

    public String getCharacterDiscipline() {
        return characterDiscipline;
    }

    public void setCharacterDiscipline(String characterDiscipline) {
        this.characterDiscipline = characterDiscipline;
    }

    public String getShipSystem() {
        return shipSystem;
    }

    public void setShipSystem(String shipSystem) {
        this.shipSystem = shipSystem;
    }

    public String getShipDepartment() {
        return shipDepartment;
    }

    public void setShipDepartment(String shipDepartment) {
        this.shipDepartment = shipDepartment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    @Override
    public String toString() {
        return "CaptainLogCombat{id=" + id + ", opponent='" + opponentName +
                "', type=" + type + ", status=" + status + ", round=" + currentRound + "}";
    }
}
