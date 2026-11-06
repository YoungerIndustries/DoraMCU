package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HexState Enum Tests")
class HexStateTest {

    @Test
    @DisplayName("values - should have 5 states")
    void values_shouldHaveFiveStates() {
        HexState[] states = HexState.values();

        assertEquals(5, states.length);
    }

    @Test
    @DisplayName("EMPTY - should exist")
    void empty_shouldExist() {
        assertSame(HexState.EMPTY, HexState.valueOf("EMPTY"));
    }

    @Test
    @DisplayName("OCCUPIED - should exist")
    void occupied_shouldExist() {
        assertSame(HexState.OCCUPIED, HexState.valueOf("OCCUPIED"));
    }

    @Test
    @DisplayName("BLOCKED - should exist")
    void blocked_shouldExist() {
        assertSame(HexState.BLOCKED, HexState.valueOf("BLOCKED"));
    }

    @Test
    @DisplayName("SELECTED - should exist")
    void selected_shouldExist() {
        assertSame(HexState.SELECTED, HexState.valueOf("SELECTED"));
    }

    @Test
    @DisplayName("DESTROYED - should exist")
    void destroyed_shouldExist() {
        assertSame(HexState.DESTROYED, HexState.valueOf("DESTROYED"));
    }

    @Test
    @DisplayName("All values should be unique")
    void allValues_shouldBeUnique() {
        HexState[] states = HexState.values();

        for (int i = 0; i < states.length; i++) {
            for (int j = i + 1; j < states.length; j++) {
                assertNotSame(states[i], states[j]);
            }
        }
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> HexState.valueOf("INVALID"));
    }
}
