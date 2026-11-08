package com.yisy.modules.doramcu.unified;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TravellerCharacterType Tests")
class TravellerCharacterTypeTest {

    @Test
    @DisplayName("Should have 5 character types")
    void testEnumSize() {
        assertEquals(5, TravellerCharacterType.values().length);
    }

    @Test
    @DisplayName("Each type should have non-empty display name")
    void testDisplayNamesNotEmpty() {
        for (TravellerCharacterType type : TravellerCharacterType.values()) {
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isBlank());
        }
    }

    @Test
    @DisplayName("Each type should have non-empty short description")
    void testShortDescriptionsNotEmpty() {
        for (TravellerCharacterType type : TravellerCharacterType.values()) {
            assertNotNull(type.getShortDescription());
            assertFalse(type.getShortDescription().isBlank());
        }
    }

    @Test
    @DisplayName("Each type should have non-empty full description")
    void testFullDescriptionsNotEmpty() {
        for (TravellerCharacterType type : TravellerCharacterType.values()) {
            assertNotNull(type.getFullDescription());
            assertFalse(type.getFullDescription().isBlank());
        }
    }

    @Test
    @DisplayName("EMPIRE_LEADER should be a management role")
    void testEmpireLeader() {
        assertTrue(TravellerCharacterType.EMPIRE_LEADER.isManagementRole());
        assertFalse(TravellerCharacterType.EMPIRE_LEADER.isFullCharacter());
        assertEquals("Empire Leader", TravellerCharacterType.EMPIRE_LEADER.getDisplayName());
    }

    @Test
    @DisplayName("SECTOR_GOVERNOR should be a management role")
    void testSectorGovernor() {
        assertTrue(TravellerCharacterType.SECTOR_GOVERNOR.isManagementRole());
        assertFalse(TravellerCharacterType.SECTOR_GOVERNOR.isFullCharacter());
        assertEquals("Sector Governor", TravellerCharacterType.SECTOR_GOVERNOR.getDisplayName());
    }

    @Test
    @DisplayName("SUBSECTOR_CHIEF should be a management role")
    void testSubsectorChief() {
        assertTrue(TravellerCharacterType.SUBSECTOR_CHIEF.isManagementRole());
        assertFalse(TravellerCharacterType.SUBSECTOR_CHIEF.isFullCharacter());
        assertEquals("Subsector Chief", TravellerCharacterType.SUBSECTOR_CHIEF.getDisplayName());
    }

    @Test
    @DisplayName("SYSTEM_MANAGER should be a management role")
    void testSystemManager() {
        assertTrue(TravellerCharacterType.SYSTEM_MANAGER.isManagementRole());
        assertFalse(TravellerCharacterType.SYSTEM_MANAGER.isFullCharacter());
        assertEquals("System Manager", TravellerCharacterType.SYSTEM_MANAGER.getDisplayName());
    }

    @Test
    @DisplayName("FULL_TRAVELLER should be a full character")
    void testFullTraveller() {
        assertFalse(TravellerCharacterType.FULL_TRAVELLER.isManagementRole());
        assertTrue(TravellerCharacterType.FULL_TRAVELLER.isFullCharacter());
        assertEquals("Full Traveller Character", TravellerCharacterType.FULL_TRAVELLER.getDisplayName());
    }

    @Test
    @DisplayName("Display names should be unique")
    void testDisplayNamesUnique() {
        TravellerCharacterType[] values = TravellerCharacterType.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getDisplayName(), values[j].getDisplayName());
            }
        }
    }

    @Test
    @DisplayName("FULL_TRAVELLER description should mention Traveller books")
    void testFullTravellerDescription() {
        String desc = TravellerCharacterType.FULL_TRAVELLER.getFullDescription();
        assertTrue(desc.contains("Traveller books") || desc.contains("careers"));
    }

    @Test
    @DisplayName("EMPIRE_LEADER description should mention empire")
    void testEmpireLeaderDescription() {
        String desc = TravellerCharacterType.EMPIRE_LEADER.getFullDescription();
        assertTrue(desc.toLowerCase().contains("empire") || desc.toLowerCase().contains("imperium"));
    }
}
