package com.yisy.modules.doramcu.shared.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a skill owned by a shared character.
 * <p>
 * Skills are stored universally so they can be mapped to either
 * Traveller skills or Captain's Log focuses when a character travels.
 * </p>
 */
@Entity
@Table(name = "shared_character_skills", indexes = {
        @Index(name = "idx_shared_skill_character", columnList = "character_id")
})
public class SharedCharacterSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private SharedCharacter character;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "skill_level", nullable = false)
    private int skillLevel = 1;

    @Column(name = "system_source", length = 20)
    private String systemSource;

    @Column(columnDefinition = "TEXT")
    private String description;

    public SharedCharacterSkill() {
    }

    public SharedCharacterSkill(String skillName, int skillLevel) {
        this.skillName = skillName;
        this.skillLevel = skillLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SharedCharacter getCharacter() {
        return character;
    }

    public void setCharacter(SharedCharacter character) {
        this.character = character;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getSystemSource() {
        return systemSource;
    }

    public void setSystemSource(String systemSource) {
        this.systemSource = systemSource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
