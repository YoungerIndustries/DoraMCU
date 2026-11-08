package com.yisy.modules.doramcu.shared.model;

import com.yisy.modules.doramcu.unified.GameType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a shared character that can travel between
 * any game systems in the DoraMCU universe (Traveller, Captain's Log, DoraMCU 4X).
 * <p>
 * This entity serves as the universal character identity, storing common
 * attributes and references to system-specific character records.
 * When a character travels between systems, the shared record is updated
 * and a travel log entry is created.
 * </p>
 */
@Entity
@Table(name = "shared_characters", indexes = {
        @Index(name = "idx_shared_char_name", columnList = "name"),
        @Index(name = "idx_shared_char_origin", columnList = "originSystem"),
        @Index(name = "idx_shared_char_cl_ref", columnList = "clCharacterId"),
        @Index(name = "idx_shared_char_traveller_ref", columnList = "travellerCharacterId"),
        @Index(name = "idx_shared_char_4x_ref", columnList = "doramcu4xPlayerId")
})
public class SharedCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String pronouns;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Universal attributes common to all systems
    @Column(nullable = false)
    private int dexterity = 0;

    @Column(nullable = false)
    private int technical = 0;

    @Column(nullable = false)
    private int strength = 0;

    @Column(nullable = false)
    private int intelligence = 0;

    @Column(nullable = false)
    private int wisdom = 0;

    @Column(nullable = false)
    private int charisma = 0;

    @Column(nullable = false)
    private int constitution = 0;

    // System-specific references
    @Enumerated(EnumType.STRING)
    @Column(name = "origin_system", nullable = false, length = 20)
    private GameType originSystem;

    @Column(name = "cl_character_id")
    private Long clCharacterId;

    @Column(name = "traveller_character_id")
    private Integer travellerCharacterId;

    @Column(name = "doramcu4x_player_id")
    private Long doramcu4xPlayerId;

    // Character metadata
    private int age;
    private String race;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedCharacterSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedCharacterTravelLog> travelLogs = new ArrayList<>();

    public SharedCharacter() {
    }

    public SharedCharacter(String name, GameType originSystem) {
        this.name = name;
        this.originSystem = originSystem;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getTechnical() {
        return technical;
    }

    public void setTechnical(int technical) {
        this.technical = technical;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public int getCharisma() {
        return charisma;
    }

    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public GameType getOriginSystem() {
        return originSystem;
    }

    public void setOriginSystem(GameType originSystem) {
        this.originSystem = originSystem;
    }

    public Long getClCharacterId() {
        return clCharacterId;
    }

    public void setClCharacterId(Long clCharacterId) {
        this.clCharacterId = clCharacterId;
    }

    public Integer getTravellerCharacterId() {
        return travellerCharacterId;
    }

    public void setTravellerCharacterId(Integer travellerCharacterId) {
        this.travellerCharacterId = travellerCharacterId;
    }

    public Long getDoramcu4xPlayerId() {
        return doramcu4xPlayerId;
    }

    public void setDoramcu4xPlayerId(Long doramcu4xPlayerId) {
        this.doramcu4xPlayerId = doramcu4xPlayerId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SharedCharacterSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<SharedCharacterSkill> skills) {
        this.skills = skills;
    }

    public List<SharedCharacterTravelLog> getTravelLogs() {
        return travelLogs;
    }

    public void setTravelLogs(List<SharedCharacterTravelLog> travelLogs) {
        this.travelLogs = travelLogs;
    }

    /**
     * Adds a skill to this character.
     */
    public void addSkill(SharedCharacterSkill skill) {
        skills.add(skill);
        skill.setCharacter(this);
    }

    /**
     * Adds a travel log entry.
     */
    public void addTravelLog(SharedCharacterTravelLog travelLog) {
        travelLogs.add(travelLog);
        travelLog.setCharacter(this);
    }
}
