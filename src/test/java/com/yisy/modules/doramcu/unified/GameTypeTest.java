package com.yisy.modules.doramcu.unified;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameType Tests")
class GameTypeTest {

    @Test
    @DisplayName("Should have 3 game types")
    void testEnumSize() {
        assertEquals(3, GameType.values().length);
    }

    @Test
    @DisplayName("Each type should have a non-empty display name")
    void testDisplayNamesNotEmpty() {
        for (GameType type : GameType.values()) {
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isBlank());
        }
    }

    @Test
    @DisplayName("Each type should have a non-empty description")
    void testDescriptionsNotEmpty() {
        for (GameType type : GameType.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isBlank());
        }
    }

    @Test
    @DisplayName("TRAVELLER should have correct values")
    void testTraveller() {
        assertEquals("Traveller", GameType.TRAVELLER.getDisplayName());
        assertTrue(GameType.TRAVELLER.getDescription().contains("space-faring"));
    }

    @Test
    @DisplayName("CAPTAINS_LOG should have correct values")
    void testCaptainsLog() {
        assertEquals("Captain's Log", GameType.CAPTAINS_LOG.getDisplayName());
        assertTrue(GameType.CAPTAINS_LOG.getDescription().contains("Star Trek"));
    }

    @Test
    @DisplayName("DORAMCU_4X should have correct values")
    void testDoraMCU4X() {
        assertEquals("DoraMCU 4X", GameType.DORAMCU_4X.getDisplayName());
        assertTrue(GameType.DORAMCU_4X.getDescription().contains("empire"));
    }

    @Test
    @DisplayName("Display names should be unique")
    void testDisplayNamesUnique() {
        GameType[] values = GameType.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getDisplayName(), values[j].getDisplayName());
            }
        }
    }
}
