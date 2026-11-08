package com.yisy.modules.doramcu.captainslog.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a character talent (special ability).
 * <p>
 * Character talents provide unique capabilities that modify gameplay.
 * In the Captain's Log system, talents are gained through:
 * <ul>
 *   <li>Lifepath choices during character creation</li>
 *   <li>Career progression and advancement</li>
 *   <li>Species-specific innate abilities</li>
 *   <li>Training and education backgrounds</li>
 * </ul>
 * </p>
 * <p>
 * Each talent has a name, description, and optional mechanical effect
 * (e.g., "+1 to Fitness checks in combat", "Can reroll one die per mission").
 * </p>
 */
@Entity
@Table(name = "cl_character_talents", indexes = {
        @Index(name = "idx_cl_character_talent_character", columnList = "character_id")
})
public class CaptainLogCharacterTalent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private CaptainLogCharacter character;

    /** Name of the talent (e.g., "Enhanced Senses", "Tactical Genius"). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Description of what the talent does narratively. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Mechanical effect of the talent (e.g., "+1 to Fitness checks"). */
    @Column(length = 200)
    private String mechanicalEffect;

    /** Source of the talent (e.g., "Species", "Lifepath", "Career", "Training"). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TalentSource source;

    /** Whether this talent is currently active (can be toggled for narrative reasons). */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Sources of character talents.
     */
    public enum TalentSource {
        SPECIES("Innate ability from species biology."),
        LIFEPATH("Gained through life experiences."),
        CAREER("Earned through career advancement."),
        TRAINING("Learned through formal or informal training."),
        MISSION("Acquired during a mission event."),
        OTHER("Other source.");

        private final String description;

        TalentSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public CaptainLogCharacterTalent() {
    }

    public CaptainLogCharacterTalent(String name, String description, String mechanicalEffect, TalentSource source) {
        this.name = name;
        this.description = description;
        this.mechanicalEffect = mechanicalEffect;
        this.source = source;
    }

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

    public String getMechanicalEffect() {
        return mechanicalEffect;
    }

    public void setMechanicalEffect(String mechanicalEffect) {
        this.mechanicalEffect = mechanicalEffect;
    }

    public TalentSource getSource() {
        return source;
    }

    public void setSource(TalentSource source) {
        this.source = source;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "CaptainLogCharacterTalent{name='" + name + "', source=" + source + "}";
    }
}
