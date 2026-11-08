package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA entity representing a piece of equipment owned by a Captain's Log character.
 * <p>
 * Equipment can provide bonuses on specific types of task attempts,
 * replacing the plain-text equipment field with structured, queryable data.
 * </p>
 */
@Entity
@Table(name = "cl_equipment", indexes = {
        @Index(name = "idx_cl_equipment_character", columnList = "character_id")
})
public class CaptainLogEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private CaptainLogCharacter character;

    /** Equipment name (e.g., "Phaser Type-2", "Tricorder"). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Detailed description of the equipment. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 
     * Equipment category for filtering and organization.
     * Examples: WEAPON, TOOL, COMMUNICATION, MEDICAL, SHIELD, UTILITY
     */
    @Column(length = 30)
    private String category;

    /**
     * Optional boon bonus — this equipment provides a boon on specific check types.
     * Empty string means no special bonus.
     * Example: "SECURITY checks" or "Science tasks"
     */
    @Column(length = 200)
    private String boonOn;

    /** Whether this equipment is currently in use/active. */
    @Column(nullable = false)
    private boolean active = true;

    public CaptainLogEquipment() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBoonOn() {
        return boonOn;
    }

    public void setBoonOn(String boonOn) {
        this.boonOn = boonOn;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
