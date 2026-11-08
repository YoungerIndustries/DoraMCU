package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a mission in the Captain's Log Solo RPG system.
 * <p>
 * Each mission follows a 3-act structure:
 * Act 1 (Opening Log), Act 2 (Supplementary Logs), Act 3 (Closing Log).
 * Missions are numbered 1-15, with career events at milestones.
 * </p>
 */
@Entity
@Table(name = "cl_missions", indexes = {
        @Index(name = "idx_cl_mission_character", columnList = "character_id")
})
public class CaptainLogMission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private CaptainLogCharacter character;

    /** Mission number (1-15 in a career). */
    @Column(nullable = false)
    private int missionNumber;

    /** Mission/episode name. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Mission type (e.g., EXPLORATION, DIPLOMACY, COMBAT, SCIENCE, RESCUE). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MissionType type;

    /** Mission status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    /** The current act of the mission (1, 2, or 3). */
    @Column(nullable = false)
    private int currentAct = 1;

    /** Log entry for Act 1 (Opening Log). */
    @Column(columnDefinition = "TEXT")
    private String act1Log;

    /** Log entry for Act 2 (Supplementary Logs). */
    @Column(columnDefinition = "TEXT")
    private String act2Log;

    /** Log entry for Act 3 (Closing Log). */
    @Column(columnDefinition = "TEXT")
    private String act3Log;

    /** Summary of the mission outcome. */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** Development notes — character growth during this mission. */
    @Column(columnDefinition = "TEXT")
    private String development;

    /** Which values were used during this mission (comma-separated descriptions). */
    @Column(length = 500)
    private String valuesUsed;

    /** Which values were challenged during this mission (comma-separated descriptions). */
    @Column(length = 500)
    private String valuesChallenged;

    /** Current Momentum pool (max 1 in Captain's Log). */
    @Column(nullable = false)
    private int momentum = 0;

    /** Current Threat pool (accumulates from complications). */
    @Column(nullable = false)
    private int threat = 0;

    /** The current narrative beat within the active act (1-5). */
    @Column(nullable = false)
    private int currentBeat = 1;

    /** Whether a career event occurred after this mission. */
    @Column(nullable = false)
    private boolean careerEvent = false;

    /** Description of the career event, if any. */
    @Column(columnDefinition = "TEXT")
    private String careerEventDescription;

    /** When the mission was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** When the mission was completed (if applicable). */
    private LocalDateTime completedAt;

    /** The campaign this mission belongs to (optional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @JsonIgnore
    private CaptainLogCampaign campaign;

    /** Log entries for this mission. */
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaptainLogEntry> logEntries = new ArrayList<>();

    /**
     * Types of missions that can occur.
     */
    public enum MissionType {
        EXPLORATION, DIPLOMACY, COMBAT, SCIENCE, RESCUE,
        INVESTIGATION, TRADE, FIRST_CONTACT, CRISIS, PERSONAL
    }

    /**
     * Status of a mission.
     */
    public enum MissionStatus {
        PLANNED, IN_PROGRESS, COMPLETED, ABANDONED
    }

    public CaptainLogMission() {
    }

    public CaptainLogMission(CaptainLogCharacter character, int missionNumber, String name) {
        this.character = character;
        this.missionNumber = missionNumber;
        this.name = name;
        this.status = MissionStatus.PLANNED;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CaptainLogCharacter getCharacter() {
        return character;
    }

    public void setCharacter(CaptainLogCharacter character) {
        this.character = character;
    }

    public int getMissionNumber() {
        return missionNumber;
    }

    public void setMissionNumber(int missionNumber) {
        this.missionNumber = missionNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MissionType getType() {
        return type;
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public int getCurrentAct() {
        return currentAct;
    }

    public void setCurrentAct(int currentAct) {
        this.currentAct = currentAct;
    }

    public String getAct1Log() {
        return act1Log;
    }

    public void setAct1Log(String act1Log) {
        this.act1Log = act1Log;
    }

    public String getAct2Log() {
        return act2Log;
    }

    public void setAct2Log(String act2Log) {
        this.act2Log = act2Log;
    }

    public String getAct3Log() {
        return act3Log;
    }

    public void setAct3Log(String act3Log) {
        this.act3Log = act3Log;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDevelopment() {
        return development;
    }

    public void setDevelopment(String development) {
        this.development = development;
    }

    public String getValuesUsed() {
        return valuesUsed;
    }

    public void setValuesUsed(String valuesUsed) {
        this.valuesUsed = valuesUsed;
    }

    public String getValuesChallenged() {
        return valuesChallenged;
    }

    public void setValuesChallenged(String valuesChallenged) {
        this.valuesChallenged = valuesChallenged;
    }

    public int getMomentum() {
        return momentum;
    }

    public void setMomentum(int momentum) {
        this.momentum = Math.max(0, Math.min(1, momentum));
    }

    public int getThreat() {
        return threat;
    }

    public void setThreat(int threat) {
        this.threat = Math.max(0, threat);
    }

    public int getCurrentBeat() {
        return currentBeat;
    }

    public void setCurrentBeat(int currentBeat) {
        this.currentBeat = Math.max(1, Math.min(5, currentBeat));
    }

    /**
     * Advances to the next narrative beat. If beat exceeds 5, advances to the next act.
     * @return true if the act advanced, false otherwise
     */
    public boolean advanceBeat() {
        this.currentBeat++;
        if (this.currentBeat > 5) {
            this.currentBeat = 1;
            this.currentAct = Math.min(3, this.currentAct + 1);
            return true;
        }
        return false;
    }

    public boolean isCareerEvent() {
        return careerEvent;
    }

    public void setCareerEvent(boolean careerEvent) {
        this.careerEvent = careerEvent;
    }

    public String getCareerEventDescription() {
        return careerEventDescription;
    }

    public void setCareerEventDescription(String careerEventDescription) {
        this.careerEventDescription = careerEventDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<CaptainLogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<CaptainLogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public CaptainLogCampaign getCampaign() {
        return campaign;
    }

    public void setCampaign(CaptainLogCampaign campaign) {
        this.campaign = campaign;
    }

    @Override
    public String toString() {
        return "CaptainLogMission{id=" + id + ", missionNumber=" + missionNumber +
                ", name='" + name + "', status=" + status + "}";
    }
}
