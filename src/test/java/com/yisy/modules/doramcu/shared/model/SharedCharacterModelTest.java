package com.yisy.modules.doramcu.shared.model;

import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SharedCharacter Model Tests")
class SharedCharacterModelTest {

    private SharedCharacter character;

    @BeforeEach
    void setUp() {
        character = new SharedCharacter("Test Character", GameType.CAPTAINS_LOG);
        character.setId(1L);
    }

    // ==================== Constructors ====================

    @Test
    @DisplayName("Constructor - should set name and origin system")
    void constructor_setsNameAndOriginSystem() {
        assertEquals("Test Character", character.getName());
        assertEquals(GameType.CAPTAINS_LOG, character.getOriginSystem());
    }

    @Test
    @DisplayName("Constructor - should set createdAt and updatedAt timestamps")
    void constructor_setsTimestamps() {
        assertNotNull(character.getCreatedAt());
        assertNotNull(character.getUpdatedAt());
    }

    @Test
    @DisplayName("Default constructor - should create empty character")
    void defaultConstructor_createsEmptyCharacter() {
        SharedCharacter empty = new SharedCharacter();

        assertNull(empty.getId());
        assertNull(empty.getName());
        assertNull(empty.getOriginSystem());
        assertEquals(0, empty.getDexterity());
        assertEquals(0, empty.getTechnical());
        assertEquals(0, empty.getStrength());
        assertEquals(0, empty.getIntelligence());
        assertEquals(0, empty.getWisdom());
        assertEquals(0, empty.getCharisma());
        assertEquals(0, empty.getConstitution());
        assertEquals(0, empty.getAge());
        assertNull(empty.getRace());
        assertNull(empty.getPronouns());
        assertNull(empty.getDescription());
        assertNull(empty.getNotes());
        assertNotNull(empty.getSkills());
        assertNotNull(empty.getTravelLogs());
    }

    // ==================== GameType Integration ====================

    @Test
    @DisplayName("GameType - should have TRAVELLER, CAPTAINS_LOG, and DORAMCU_4X values")
    void gameType_hasValues() {
        GameType[] values = GameType.values();
        assertEquals(3, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(GameType.TRAVELLER));
        assertTrue(java.util.Arrays.asList(values).contains(GameType.CAPTAINS_LOG));
        assertTrue(java.util.Arrays.asList(values).contains(GameType.DORAMCU_4X));
    }

    // ==================== Getters and Setters ====================

    @Test
    @DisplayName("Getters and Setters - should set and get ID")
    void settersAndGetters_id() {
        character.setId(42L);
        assertEquals(42L, character.getId());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get name")
    void settersAndGetters_name() {
        character.setName("New Name");
        assertEquals("New Name", character.getName());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get pronouns")
    void settersAndGetters_pronouns() {
        character.setPronouns("they/them");
        assertEquals("they/them", character.getPronouns());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get description")
    void settersAndGetters_description() {
        character.setDescription("A brave explorer");
        assertEquals("A brave explorer", character.getDescription());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get all attributes")
    void settersAndGetters_attributes() {
        character.setDexterity(8);
        character.setTechnical(6);
        character.setStrength(7);
        character.setIntelligence(9);
        character.setWisdom(5);
        character.setCharisma(10);
        character.setConstitution(6);

        assertEquals(8, character.getDexterity());
        assertEquals(6, character.getTechnical());
        assertEquals(7, character.getStrength());
        assertEquals(9, character.getIntelligence());
        assertEquals(5, character.getWisdom());
        assertEquals(10, character.getCharisma());
        assertEquals(6, character.getConstitution());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get age")
    void settersAndGetters_age() {
        character.setAge(25);
        assertEquals(25, character.getAge());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get race")
    void settersAndGetters_race() {
        character.setRace("Human");
        assertEquals("Human", character.getRace());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get notes")
    void settersAndGetters_notes() {
        character.setNotes("Some notes");
        assertEquals("Some notes", character.getNotes());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get origin system")
    void settersAndGetters_originSystem() {
        character.setOriginSystem(GameType.TRAVELLER);
        assertEquals(GameType.TRAVELLER, character.getOriginSystem());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get CL character ID")
    void settersAndGetters_clCharacterId() {
        character.setClCharacterId(100L);
        assertEquals(100L, character.getClCharacterId());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get Traveller character ID")
    void settersAndGetters_travellerCharacterId() {
        character.setTravellerCharacterId(200);
        assertEquals(200, character.getTravellerCharacterId());
    }

    // ==================== addSkill ====================

    @Test
    @DisplayName("addSkill - should add skill to skills list")
    void addSkill_addsToSkillsList() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 2);

        character.addSkill(skill);

        assertEquals(1, character.getSkills().size());
        assertEquals("Pistol", character.getSkills().get(0).getSkillName());
    }

    @Test
    @DisplayName("addSkill - should set character reference on skill")
    void addSkill_setsCharacterReference() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 2);

        character.addSkill(skill);

        assertSame(character, skill.getCharacter());
    }

    @Test
    @DisplayName("addSkill - should add multiple skills")
    void addSkill_addsMultipleSkills() {
        SharedCharacterSkill skill1 = new SharedCharacterSkill("Pistol", 2);
        SharedCharacterSkill skill2 = new SharedCharacterSkill("Navigation", 1);

        character.addSkill(skill1);
        character.addSkill(skill2);

        assertEquals(2, character.getSkills().size());
        assertEquals("Pistol", character.getSkills().get(0).getSkillName());
        assertEquals("Navigation", character.getSkills().get(1).getSkillName());
    }

    // ==================== addTravelLog ====================

    @Test
    @DisplayName("addTravelLog - should add travel log to travel logs list")
    void addTravelLog_addsToTravelLogsList() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog(
                character, "TRAVELLER", "CAPTAINS_LOG");

        character.addTravelLog(log);

        assertEquals(1, character.getTravelLogs().size());
        assertEquals("TRAVELLER", character.getTravelLogs().get(0).getFromSystem());
        assertEquals("CAPTAINS_LOG", character.getTravelLogs().get(0).getToSystem());
    }

    @Test
    @DisplayName("addTravelLog - should set character reference on travel log")
    void addTravelLog_setsCharacterReference() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog(
                character, "TRAVELLER", "CAPTAINS_LOG");

        character.addTravelLog(log);

        assertSame(character, log.getCharacter());
    }

    @Test
    @DisplayName("addTravelLog - should add multiple travel logs")
    void addTravelLog_addsMultipleLogs() {
        SharedCharacterTravelLog log1 = new SharedCharacterTravelLog(
                character, "TRAVELLER", "CAPTAINS_LOG");
        SharedCharacterTravelLog log2 = new SharedCharacterTravelLog(
                character, "CAPTAINS_LOG", "TRAVELLER");

        character.addTravelLog(log1);
        character.addTravelLog(log2);

        assertEquals(2, character.getTravelLogs().size());
        assertEquals("TRAVELLER", character.getTravelLogs().get(0).getFromSystem());
        assertEquals("CAPTAINS_LOG", character.getTravelLogs().get(1).getFromSystem());
    }

    // ==================== onUpdate ====================

    @Test
    @DisplayName("onUpdate - should update updatedAt timestamp")
    void onUpdate_updatesTimestamp() {
        LocalDateTime oldTimestamp = LocalDateTime.of(2020, 1, 1, 0, 0);
        character.setUpdatedAt(oldTimestamp);

        character.onUpdate();

        assertNotEquals(oldTimestamp, character.getUpdatedAt());
        assertNotNull(character.getUpdatedAt());
    }

    // ==================== Default Values ====================

    @Test
    @DisplayName("Default values - attributes should default to 0")
    void defaultValues_attributesZero() {
        SharedCharacter empty = new SharedCharacter();
        assertEquals(0, empty.getDexterity());
        assertEquals(0, empty.getTechnical());
        assertEquals(0, empty.getStrength());
        assertEquals(0, empty.getIntelligence());
        assertEquals(0, empty.getWisdom());
        assertEquals(0, empty.getCharisma());
        assertEquals(0, empty.getConstitution());
    }

    @Test
    @DisplayName("Default values - age should default to 0")
    void defaultValues_ageZero() {
        SharedCharacter empty = new SharedCharacter();
        assertEquals(0, empty.getAge());
    }

    @Test
    @DisplayName("Default values - skills list should be empty but not null")
    void defaultValues_skillsEmpty() {
        SharedCharacter empty = new SharedCharacter();
        assertNotNull(empty.getSkills());
        assertEquals(0, empty.getSkills().size());
    }

    @Test
    @DisplayName("Default values - travel logs list should be empty but not null")
    void defaultValues_travelLogsEmpty() {
        SharedCharacter empty = new SharedCharacter();
        assertNotNull(empty.getTravelLogs());
        assertEquals(0, empty.getTravelLogs().size());
    }
}
