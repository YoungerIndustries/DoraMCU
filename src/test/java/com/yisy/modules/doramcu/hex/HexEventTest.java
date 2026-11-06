package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HexEvent Enum Tests")
class HexEventTest {

    @Test
    @DisplayName("values - should have 8 events")
    void values_shouldHaveEightEvents() {
        HexEvent[] events = HexEvent.values();

        assertEquals(8, events.length);
    }

    @Test
    @DisplayName("PLACE - should exist")
    void place_shouldExist() {
        assertSame(HexEvent.PLACE, HexEvent.valueOf("PLACE"));
    }

    @Test
    @DisplayName("REMOVE - should exist")
    void remove_shouldExist() {
        assertSame(HexEvent.REMOVE, HexEvent.valueOf("REMOVE"));
    }

    @Test
    @DisplayName("BLOCK - should exist")
    void block_shouldExist() {
        assertSame(HexEvent.BLOCK, HexEvent.valueOf("BLOCK"));
    }

    @Test
    @DisplayName("UNBLOCK - should exist")
    void unblock_shouldExist() {
        assertSame(HexEvent.UNBLOCK, HexEvent.valueOf("UNBLOCK"));
    }

    @Test
    @DisplayName("SELECT - should exist")
    void select_shouldExist() {
        assertSame(HexEvent.SELECT, HexEvent.valueOf("SELECT"));
    }

    @Test
    @DisplayName("DESELECT - should exist")
    void deselect_shouldExist() {
        assertSame(HexEvent.DESELECT, HexEvent.valueOf("DESELECT"));
    }

    @Test
    @DisplayName("DESTROY - should exist")
    void destroy_shouldExist() {
        assertSame(HexEvent.DESTROY, HexEvent.valueOf("DESTROY"));
    }

    @Test
    @DisplayName("REPAIR - should exist")
    void repair_shouldExist() {
        assertSame(HexEvent.REPAIR, HexEvent.valueOf("REPAIR"));
    }

    @Test
    @DisplayName("All values should be unique")
    void allValues_shouldBeUnique() {
        HexEvent[] events = HexEvent.values();

        for (int i = 0; i < events.length; i++) {
            for (int j = i + 1; j < events.length; j++) {
                assertNotSame(events[i], events[j]);
            }
        }
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> HexEvent.valueOf("INVALID"));
    }
}
