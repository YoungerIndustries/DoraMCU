package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a campaign — a group of related missions
 * that form a continuous narrative arc in the Captain's Log system.
 * <p>
 * Campaigns connect multiple missions with shared NPCs, locations,
 * and ongoing story threads.
 * </p>
 */
@Entity
@Table(name = "cl_campaigns", indexes = {
        @Index(name = "idx_cl_campaign_character", columnList = "character_id")
})
public class CaptainLogCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private CaptainLogCharacter character;

    /** Campaign title. */
    @Column(nullable = false, length = 200)
    private String title;

    /** Brief description of the campaign arc. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Campaign status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    /** Ongoing NPCs involved in this campaign (comma-separated names and roles). */
    @Column(columnDefinition = "TEXT")
    private String ongoingNpcs;

    /** Key locations in this campaign (comma-separated). */
    @Column(columnDefinition = "TEXT")
    private String keyLocations;

    /** Unresolved plot threads (comma-separated). */
    @Column(columnDefinition = "TEXT")
    private String plotThreads;

    /** When the campaign was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** When the campaign was concluded (if applicable). */
    private LocalDateTime concludedAt;

    /** Missions belonging to this campaign. */
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CaptainLogMission> missions = new ArrayList<>();

    public enum CampaignStatus {
        ACTIVE, CONCLUDED, ABANDONED
    }

    public CaptainLogCampaign() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public String getOngoingNpcs() {
        return ongoingNpcs;
    }

    public void setOngoingNpcs(String ongoingNpcs) {
        this.ongoingNpcs = ongoingNpcs;
    }

    public String getKeyLocations() {
        return keyLocations;
    }

    public void setKeyLocations(String keyLocations) {
        this.keyLocations = keyLocations;
    }

    public String getPlotThreads() {
        return plotThreads;
    }

    public void setPlotThreads(String plotThreads) {
        this.plotThreads = plotThreads;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConcludedAt() {
        return concludedAt;
    }

    public void setConcludedAt(LocalDateTime concludedAt) {
        this.concludedAt = concludedAt;
    }

    public List<CaptainLogMission> getMissions() {
        return missions;
    }

    public void setMissions(List<CaptainLogMission> missions) {
        this.missions = missions;
    }
}
