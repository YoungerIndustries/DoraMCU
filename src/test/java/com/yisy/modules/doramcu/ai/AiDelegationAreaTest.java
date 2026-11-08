package com.yisy.modules.doramcu.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiDelegationArea Tests")
class AiDelegationAreaTest {

    @Test
    @DisplayName("should have 9 delegation areas")
    void totalAreas() {
        assertEquals(9, AiDelegationArea.values().length);
    }

    @Test
    @DisplayName("should include all expected areas")
    void allAreasPresent() {
        AiDelegationArea[] areas = AiDelegationArea.values();
        String[] names = new String[areas.length];
        for (int i = 0; i < areas.length; i++) {
            names[i] = areas[i].name();
        }

        assertTrue(Set.of(names).contains("FLEET_MANAGEMENT"));
        assertTrue(Set.of(names).contains("COMBAT"));
        assertTrue(Set.of(names).contains("CONSTRUCTION"));
        assertTrue(Set.of(names).contains("RESOURCE_MANAGEMENT"));
        assertTrue(Set.of(names).contains("TRADE"));
        assertTrue(Set.of(names).contains("EXPANSION"));
        assertTrue(Set.of(names).contains("RESEARCH"));
        assertTrue(Set.of(names).contains("DIPLOMACY"));
        assertTrue(Set.of(names).contains("COMMANDER_ASSIGNMENT"));
    }

    @Test
    @DisplayName("should have display names for all areas")
    void displayNamesNotBlank() {
        for (AiDelegationArea area : AiDelegationArea.values()) {
            assertNotNull(area.getDisplayName());
            assertFalse(area.getDisplayName().isBlank());
        }
    }

    @Test
    @DisplayName("should have descriptions for all areas")
    void descriptionsNotBlank() {
        for (AiDelegationArea area : AiDelegationArea.values()) {
            assertNotNull(area.getDescription());
            assertFalse(area.getDescription().isBlank());
        }
    }

    @Test
    @DisplayName("should return correct display name for FLEET_MANAGEMENT")
    void fleetManagementDisplayName() {
        assertEquals("Fleet Management", AiDelegationArea.FLEET_MANAGEMENT.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for COMBAT")
    void combatDisplayName() {
        assertEquals("Combat", AiDelegationArea.COMBAT.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for TRADE")
    void tradeDisplayName() {
        assertEquals("Trade", AiDelegationArea.TRADE.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for CONSTRUCTION")
    void constructionDisplayName() {
        assertEquals("Construction", AiDelegationArea.CONSTRUCTION.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for RESOURCE_MANAGEMENT")
    void resourceManagementDisplayName() {
        assertEquals("Resource Management", AiDelegationArea.RESOURCE_MANAGEMENT.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for EXPANSION")
    void expansionDisplayName() {
        assertEquals("Expansion", AiDelegationArea.EXPANSION.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for RESEARCH")
    void researchDisplayName() {
        assertEquals("Research", AiDelegationArea.RESEARCH.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for DIPLOMACY")
    void diplomacyDisplayName() {
        assertEquals("Diplomacy", AiDelegationArea.DIPLOMACY.getDisplayName());
    }

    @Test
    @DisplayName("should return correct display name for COMMANDER_ASSIGNMENT")
    void commanderAssignmentDisplayName() {
        assertEquals("Commander Assignment", AiDelegationArea.COMMANDER_ASSIGNMENT.getDisplayName());
    }

    // ---- Description Content Tests ----

    @Test
    @DisplayName("should have meaningful descriptions for all areas")
    void descriptionContent() {
        String fleetDesc = AiDelegationArea.FLEET_MANAGEMENT.getDescription();
        assertTrue(fleetDesc.toLowerCase().contains("fleet"));

        String combatDesc = AiDelegationArea.COMBAT.getDescription();
        assertTrue(combatDesc.toLowerCase().contains("combat") || combatDesc.toLowerCase().contains("tactical") || combatDesc.toLowerCase().contains("battle"));

        String constructionDesc = AiDelegationArea.CONSTRUCTION.getDescription();
        assertTrue(constructionDesc.toLowerCase().contains("build"));

        String resourceDesc = AiDelegationArea.RESOURCE_MANAGEMENT.getDescription();
        assertTrue(resourceDesc.toLowerCase().contains("resource") || resourceDesc.toLowerCase().contains("mining"));

        String tradeDesc = AiDelegationArea.TRADE.getDescription();
        assertTrue(tradeDesc.toLowerCase().contains("trade"));

        String expansionDesc = AiDelegationArea.EXPANSION.getDescription();
        assertTrue(expansionDesc.toLowerCase().contains("colon") || expansionDesc.toLowerCase().contains("expand"));

        String researchDesc = AiDelegationArea.RESEARCH.getDescription();
        assertTrue(researchDesc.toLowerCase().contains("research"));

        String diplomacyDesc = AiDelegationArea.DIPLOMACY.getDescription();
        assertTrue(diplomacyDesc.toLowerCase().contains("diplomat") || diplomacyDesc.toLowerCase().contains("relation"));

        String commanderDesc = AiDelegationArea.COMMANDER_ASSIGNMENT.getDescription();
        assertTrue(commanderDesc.toLowerCase().contains("commander"));
    }

    // ---- Enum ValueOf Tests ----

    @Test
    @DisplayName("valueOf should resolve all area names")
    void valueOfAllAreas() {
        assertEquals(AiDelegationArea.FLEET_MANAGEMENT, AiDelegationArea.valueOf("FLEET_MANAGEMENT"));
        assertEquals(AiDelegationArea.COMBAT, AiDelegationArea.valueOf("COMBAT"));
        assertEquals(AiDelegationArea.CONSTRUCTION, AiDelegationArea.valueOf("CONSTRUCTION"));
        assertEquals(AiDelegationArea.RESOURCE_MANAGEMENT, AiDelegationArea.valueOf("RESOURCE_MANAGEMENT"));
        assertEquals(AiDelegationArea.TRADE, AiDelegationArea.valueOf("TRADE"));
        assertEquals(AiDelegationArea.EXPANSION, AiDelegationArea.valueOf("EXPANSION"));
        assertEquals(AiDelegationArea.RESEARCH, AiDelegationArea.valueOf("RESEARCH"));
        assertEquals(AiDelegationArea.DIPLOMACY, AiDelegationArea.valueOf("DIPLOMACY"));
        assertEquals(AiDelegationArea.COMMANDER_ASSIGNMENT, AiDelegationArea.valueOf("COMMANDER_ASSIGNMENT"));
    }

    @Test
    @DisplayName("valueOf should throw on invalid name")
    void valueOfInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> AiDelegationArea.valueOf("INVALID"));
    }

    @Test
    @DisplayName("valueOf should throw on null")
    void valueOfNullThrows() {
        assertThrows(NullPointerException.class, () -> AiDelegationArea.valueOf((String) null));
    }

    // ---- Display Name Uniqueness ----

    @Test
    @DisplayName("all display names should be unique")
    void displayNamesUnique() {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (AiDelegationArea area : AiDelegationArea.values()) {
            assertTrue(names.add(area.getDisplayName()),
                    "Duplicate display name: " + area.getDisplayName());
        }
        assertEquals(9, names.size());
    }
}
