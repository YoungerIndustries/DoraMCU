package com.yisy.modules.doramcu.shared.model;

import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SharedCharacterTravelLog Model Tests")
class SharedCharacterTravelLogModelTest {

    // ==================== Constructors ====================

    @Test
    @DisplayName("Default constructor - should create empty travel log")
    void defaultConstructor_createsEmptyLog() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();

        assertNull(log.getId());
        assertNull(log.getCharacter());
        assertNull(log.getFromSystem());
        assertNull(log.getToSystem());
        assertNull(log.getTravelNotes());
        assertNull(log.getTraveledAt());
    }

    @Test
    @DisplayName("Parameterized constructor - should set character, from/to system, and timestamp")
    void constructor_setsFields() {
        SharedCharacter character = new SharedCharacter("Test", GameType.TRAVELLER);
        character.setId(1L);

        LocalDateTime before = LocalDateTime.now();
        SharedCharacterTravelLog log = new SharedCharacterTravelLog(character, "TRAVELLER", "CAPTAINS_LOG");
        LocalDateTime after = LocalDateTime.now();

        assertSame(character, log.getCharacter());
        assertEquals("TRAVELLER", log.getFromSystem());
        assertEquals("CAPTAINS_LOG", log.getToSystem());
        assertNull(log.getTravelNotes());
        assertNotNull(log.getTraveledAt());
        assertTrue(log.getTraveledAt().isAfter(before) || log.getTraveledAt().isEqual(before));
        assertTrue(log.getTraveledAt().isBefore(after) || log.getTraveledAt().isEqual(after));
    }

    // ==================== Getters and Setters ====================

    @Test
    @DisplayName("Getters and Setters - should set and get ID")
    void settersAndGetters_id() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();
        log.setId(42L);
        assertEquals(42L, log.getId());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get character reference")
    void settersAndGetters_character() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();
        SharedCharacter character = new SharedCharacter("Test", GameType.CAPTAINS_LOG);
        character.setId(1L);

        log.setCharacter(character);

        assertSame(character, log.getCharacter());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get from system")
    void settersAndGetters_fromSystem() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();
        log.setFromSystem("TRAVELLER");
        assertEquals("TRAVELLER", log.getFromSystem());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get to system")
    void settersAndGetters_toSystem() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();
        log.setToSystem("CAPTAINS_LOG");
        assertEquals("CAPTAINS_LOG", log.getToSystem());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get travel notes")
    void settersAndGetters_travelNotes() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();
        log.setTravelNotes("First contact mission");
        assertEquals("First contact mission", log.getTravelNotes());
    }

    @Test
    @DisplayName("Getters and Setters - should set and get traveled at timestamp")
    void settersAndGetters_traveledAt() {
        SharedCharacterTravelLog log = new SharedCharacterTravelLog();
        LocalDateTime timestamp = LocalDateTime.of(2024, 6, 15, 14, 30);
        log.setTraveledAt(timestamp);
        assertEquals(timestamp, log.getTraveledAt());
    }

    // ==================== Complete Setup ====================

    @Test
    @DisplayName("Complete setup - should configure all fields")
    void completeSetup_configuresAllFields() {
        SharedCharacter character = new SharedCharacter("Traveler", GameType.TRAVELLER);
        character.setId(1L);

        SharedCharacterTravelLog log = new SharedCharacterTravelLog(character, "TRAVELLER", "CAPTAINS_LOG");
        log.setId(1L);
        log.setTravelNotes("Exploring new systems");
        log.setTraveledAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        assertEquals(1L, log.getId());
        assertSame(character, log.getCharacter());
        assertEquals("TRAVELLER", log.getFromSystem());
        assertEquals("CAPTAINS_LOG", log.getToSystem());
        assertEquals("Exploring new systems", log.getTravelNotes());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), log.getTraveledAt());
    }

    // ==================== Round-Trip Travel ====================

    @Test
    @DisplayName("Round-trip travel - should support TRAVELLER to CAPTAINS_LOG and back")
    void roundTripTravel_supportsBothDirections() {
        SharedCharacter character = new SharedCharacter("Traveler", GameType.TRAVELLER);
        character.setId(1L);

        SharedCharacterTravelLog trip1 = new SharedCharacterTravelLog(character, "TRAVELLER", "CAPTAINS_LOG");
        trip1.setId(1L);
        trip1.setTravelNotes("Outbound");

        SharedCharacterTravelLog trip2 = new SharedCharacterTravelLog(character, "CAPTAINS_LOG", "TRAVELLER");
        trip2.setId(2L);
        trip2.setTravelNotes("Return");

        assertEquals("TRAVELLER", trip1.getFromSystem());
        assertEquals("CAPTAINS_LOG", trip1.getToSystem());
        assertEquals("CAPTAINS_LOG", trip2.getFromSystem());
        assertEquals("TRAVELLER", trip2.getToSystem());
        assertNotEquals(trip1.getFromSystem(), trip2.getFromSystem());
    }
}
