package com.yisy.modules.doramcu.shared.model;

import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SharedCharacterSkill Model Tests")
class SharedCharacterSkillModelTest {

    // ==================== Constructors ====================

    @Test
    @DisplayName("Default constructor - should create empty skill")
    void defaultConstructor_createsEmptySkill() {
        SharedCharacterSkill skill = new SharedCharacterSkill();

        assertNull(skill.getId());
        assertNull(skill.getCharacter());
        assertNull(skill.getSkillName());
        assertEquals(1, skill.getSkillLevel());
        assertNull(skill.getSystemSource());
        assertNull(skill.getDescription());
    }

    @Test
    @DisplayName("Parameterized constructor - should set skill name and level")
    void constructor_setsNameAndLevel() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 3);

        assertEquals("Pistol", skill.getSkillName());
        assertEquals(3, skill.getSkillLevel());
        assertNull(skill.getId());
        assertNull(skill.getCharacter());
        assertNull(skill.getSystemSource());
        assertNull(skill.getDescription());
    }

    // ==================== Getters and Setters ====================

    @Test
    @DisplayName("Getters and Setters - should set and get ID")
    void settersAndGetters_id() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        skill.setId(42L);
        assertEquals(42L, skill.getId());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get character reference")
    void settersAndGetters_character() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        SharedCharacter character = new SharedCharacter("Test", GameType.CAPTAINS_LOG);
        character.setId(1L);

        skill.setCharacter(character);

        assertSame(character, skill.getCharacter());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get skill name")
    void settersAndGetters_skillName() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        skill.setSkillName("Navigation");
        assertEquals("Navigation", skill.getSkillName());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get skill level")
    void settersAndGetters_skillLevel() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        skill.setSkillLevel(5);
        assertEquals(5, skill.getSkillLevel());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get system source")
    void settersAndGetters_systemSource() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        skill.setSystemSource("TRAVELLER");
        assertEquals("TRAVELLER", skill.getSystemSource());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get description")
    void settersAndGetters_description() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        skill.setDescription("Firearm skill");
        assertEquals("Firearm skill", skill.getDescription());
    }

    // ==================== Default Values ====================

    @Test
    @DisplayName("Default values - skill level should default to 1")
    void defaultValues_skillLevelOne() {
        SharedCharacterSkill skill = new SharedCharacterSkill();
        assertEquals(1, skill.getSkillLevel());
    }

    // ==================== Complete Setup ====================

    @Test
    @DisplayName("Complete setup - should configure all fields")
    void completeSetup_configuresAllFields() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 2);
        skill.setId(1L);
        skill.setSystemSource("TRAVELLER");
        skill.setDescription("Firearm proficiency");

        SharedCharacter character = new SharedCharacter("Test", GameType.TRAVELLER);
        character.setId(1L);
        skill.setCharacter(character);

        assertEquals(1L, skill.getId());
        assertEquals("Pistol", skill.getSkillName());
        assertEquals(2, skill.getSkillLevel());
        assertEquals("TRAVELLER", skill.getSystemSource());
        assertEquals("Firearm proficiency", skill.getDescription());
        assertSame(character, skill.getCharacter());
    }
}
