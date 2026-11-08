package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Hex class.
 */
class HexTest {

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create hex at origin with default constructor")
    void testDefaultConstructor() {
        Hex hex = new Hex();
        assertEquals(0, hex.getQ());
        assertEquals(0, hex.getR());
        assertEquals(0, hex.getS());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should create hex with derived s coordinate")
    void testTwoArgConstructor() {
        Hex hex = new Hex(3, -1);
        assertEquals(3, hex.getQ());
        assertEquals(-1, hex.getR());
        assertEquals(-2, hex.getS()); // s = -q - r = -3 - (-1) = -2
    }

    @Test
    @DisplayName("Should create hex with explicit s coordinate")
    void testThreeArgConstructor() {
        Hex hex = new Hex(1, 2, -3);
        assertEquals(1, hex.getQ());
        assertEquals(2, hex.getR());
        assertEquals(-3, hex.getS());
    }

    // ---- State machine tests ----

    @Test
    @DisplayName("Should start in EMPTY state")
    void testInitialState() {
        Hex hex = new Hex(0, 0);
        assertEquals(HexState.EMPTY, hex.getState());
        assertTrue(hex.isInState(HexState.EMPTY));
    }

    @Test
    @DisplayName("Should transition EMPTY to OCCUPIED on PLACE")
    void testPlaceTransition() {
        Hex hex = new Hex(0, 0);
        assertTrue(hex.place("unit"));
        assertEquals(HexState.OCCUPIED, hex.getState());
        assertEquals("unit", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to place on already occupied hex")
    void testPlaceOnOccupied() {
        Hex hex = new Hex(0, 0);
        hex.place("unit1");
        assertFalse(hex.place("unit2"));
        assertEquals("unit1", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to place on blocked hex")
    void testPlaceOnBlocked() {
        Hex hex = new Hex(0, 0);
        hex.block();
        assertFalse(hex.place("unit"));
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should transition OCCUPIED to EMPTY on REMOVE")
    void testRemoveTransition() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        Object removed = hex.remove();
        assertEquals("unit", removed);
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should return null when removing from empty hex")
    void testRemoveFromEmpty() {
        Hex hex = new Hex(0, 0);
        assertNull(hex.remove());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should fail to remove from selected hex")
    void testRemoveFromSelected() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        hex.select();
        assertNull(hex.remove());
        assertEquals(HexState.SELECTED, hex.getState());
    }

    @Test
    @DisplayName("Should transition EMPTY to BLOCKED on BLOCK")
    void testBlockTransition() {
        Hex hex = new Hex(0, 0);
        assertTrue(hex.block());
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to block already occupied hex")
    void testBlockOnOccupied() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        assertFalse(hex.block());
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should transition BLOCKED to EMPTY on UNBLOCK")
    void testUnblockTransition() {
        Hex hex = new Hex(0, 0);
        hex.block();
        assertTrue(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should fail to unblock empty hex")
    void testUnblockOnEmpty() {
        Hex hex = new Hex(0, 0);
        assertFalse(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should transition OCCUPIED to SELECTED on SELECT")
    void testSelectTransition() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        assertTrue(hex.select());
        assertEquals(HexState.SELECTED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to select empty hex")
    void testSelectOnEmpty() {
        Hex hex = new Hex(0, 0);
        assertFalse(hex.select());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should transition SELECTED to OCCUPIED on DESELECT")
    void testDeselectTransition() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        hex.select();
        assertTrue(hex.deselect());
        assertEquals(HexState.OCCUPIED, hex.getState());
        assertEquals("unit", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to deselect non-selected hex")
    void testDeselectOnOccupied() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        assertFalse(hex.deselect());
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should return correct possible events for EMPTY state")
    void testPossibleEventsEmpty() {
        Hex hex = new Hex(0, 0);
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.PLACE));
        assertTrue(events.contains(HexEvent.BLOCK));
        assertTrue(events.contains(HexEvent.DESTROY));
        assertEquals(3, events.size());
    }

    @Test
    @DisplayName("Should return correct possible events for OCCUPIED state")
    void testPossibleEventsOccupied() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.REMOVE));
        assertTrue(events.contains(HexEvent.SELECT));
        assertTrue(events.contains(HexEvent.DESTROY));
        assertEquals(3, events.size());
    }

    @Test
    @DisplayName("Should return correct possible events for SELECTED state")
    void testPossibleEventsSelected() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        hex.select();
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.DESELECT));
        assertTrue(events.contains(HexEvent.DESTROY));
        assertEquals(2, events.size());
    }

    @Test
    @DisplayName("Should return correct possible events for BLOCKED state")
    void testPossibleEventsBlocked() {
        Hex hex = new Hex(0, 0);
        hex.block();
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.UNBLOCK));
        assertTrue(events.contains(HexEvent.DESTROY));
        assertEquals(2, events.size());
    }

    @Test
    @DisplayName("Should reset hex to EMPTY state and clear payload")
    void testReset() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        hex.reset();
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should reset selected hex to EMPTY")
    void testResetFromSelected() {
        Hex hex = new Hex(0, 0);
        hex.place("unit");
        hex.select();
        hex.reset();
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should send raw event and return true for valid transition")
    void testSendEvent() {
        Hex hex = new Hex(0, 0);
        assertTrue(hex.sendEvent(HexEvent.PLACE));
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should send raw event and return false for invalid transition")
    void testSendEventInvalid() {
        Hex hex = new Hex(0, 0);
        assertFalse(hex.sendEvent(HexEvent.REMOVE));
        assertEquals(HexState.EMPTY, hex.getState());
    }

    // ---- Full state cycle tests ----

    @Test
    @DisplayName("Should complete full place-select-deselect-remove cycle")
    void testFullCycle() {
        Hex hex = new Hex(1, 0);
        assertTrue(hex.place("entity"));
        assertEquals(HexState.OCCUPIED, hex.getState());

        assertTrue(hex.select());
        assertEquals(HexState.SELECTED, hex.getState());

        assertTrue(hex.deselect());
        assertEquals(HexState.OCCUPIED, hex.getState());

        Object removed = hex.remove();
        assertEquals("entity", removed);
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should complete full block-unblock cycle")
    void testBlockUnblockCycle() {
        Hex hex = new Hex(0, 1);
        assertTrue(hex.block());
        assertEquals(HexState.BLOCKED, hex.getState());

        assertTrue(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    // ---- Hex arithmetic tests ----

    @Test
    @DisplayName("Should return correct neighbor in each direction")
    void testNeighbor() {
        Hex center = new Hex(0, 0);

        Hex east = center.neighbor(HexDirection.EAST);
        assertEquals(1, east.getQ());
        assertEquals(0, east.getR());

        Hex west = center.neighbor(HexDirection.WEST);
        assertEquals(-1, west.getQ());
        assertEquals(0, west.getR());

        Hex northEast = center.neighbor(HexDirection.NORTH_EAST);
        assertEquals(1, northEast.getQ());
        assertEquals(-1, northEast.getR());

        Hex southWest = center.neighbor(HexDirection.SOUTH_WEST);
        assertEquals(-1, southWest.getQ());
        assertEquals(1, southWest.getR());

        Hex northWest = center.neighbor(HexDirection.NORTH_WEST);
        assertEquals(0, northWest.getQ());
        assertEquals(-1, northWest.getR());

        Hex southEast = center.neighbor(HexDirection.SOUTH_EAST);
        assertEquals(0, southEast.getQ());
        assertEquals(1, southEast.getR());
    }

    @Test
    @DisplayName("Should return all 6 neighbors")
    void testNeighbors() {
        Hex center = new Hex(0, 0);
        List<Hex> neighbors = center.neighbors();
        assertEquals(6, neighbors.size());
    }

    @Test
    @DisplayName("Should return correct distance between hexes")
    void testDistance() {
        Hex a = new Hex(0, 0);
        Hex b = new Hex(3, 0);
        assertEquals(3, a.distance(b));

        Hex c = new Hex(1, 1);
        Hex d = new Hex(4, 4);
        // |1-4| + |1-4| + |(-2)-(-8)| = 3+3+6 = 12, /2 = 6
        assertEquals(6, c.distance(d));

        // Distance to self
        assertEquals(0, a.distance(a));
    }

    @Test
    @DisplayName("Should return symmetric distance")
    void testDistanceSymmetric() {
        Hex a = new Hex(2, -3);
        Hex b = new Hex(-1, 5);
        assertEquals(a.distance(b), b.distance(a));
    }

    @Test
    @DisplayName("Should lerp at t=0 return this hex")
    void testLerpAtZero() {
        Hex a = new Hex(0, 0);
        Hex b = new Hex(5, 3);
        Hex result = a.lerp(b, 0.0);
        assertEquals(a.getQ(), result.getQ());
        assertEquals(a.getR(), result.getR());
    }

    @Test
    @DisplayName("Should lerp at t=1 return other hex")
    void testLerpAtOne() {
        Hex a = new Hex(0, 0);
        Hex b = new Hex(5, 3);
        Hex result = a.lerp(b, 1.0);
        assertEquals(b.getQ(), result.getQ());
        assertEquals(b.getR(), result.getR());
    }

    @Test
    @DisplayName("Should lerp at t=0.5 return midpoint hex")
    void testLerpAtMidpoint() {
        Hex a = new Hex(0, 0);
        Hex b = new Hex(4, 0);
        Hex result = a.lerp(b, 0.5);
        assertEquals(2, result.getQ());
        assertEquals(0, result.getR());
    }

    @Test
    @DisplayName("Should return line between two hexes")
    void testLine() {
        Hex a = new Hex(0, 0);
        Hex b = new Hex(3, 0);
        List<Hex> line = a.line(b);
        assertEquals(4, line.size()); // distance + 1
        assertEquals(a, line.get(0));
        assertEquals(b, line.get(3));
    }

    @Test
    @DisplayName("Should return single hex line to self")
    void testLineToSelf() {
        Hex a = new Hex(2, 3);
        List<Hex> line = a.line(a);
        assertEquals(1, line.size());
        assertEquals(a, line.get(0));
    }

    // ---- Equals and hashCode tests ----

    @Test
    @DisplayName("Should be equal to self")
    void testEqualsReflexive() {
        Hex hex = new Hex(1, 2);
        assertEquals(hex, hex);
    }

    @Test
    @DisplayName("Should be equal to hex with same coordinates")
    void testEqualsSameCoordinates() {
        Hex a = new Hex(3, -1);
        Hex b = new Hex(3, -1);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Should not be equal to hex with different coordinates")
    void testEqualsDifferentCoordinates() {
        Hex a = new Hex(3, -1);
        Hex b = new Hex(3, 0);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testEqualsNull() {
        Hex hex = new Hex(0, 0);
        assertNotEquals(hex, null);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void testEqualsDifferentType() {
        Hex hex = new Hex(0, 0);
        assertNotEquals(hex, "string");
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        Hex a = new Hex(5, -2);
        Hex b = new Hex(5, -2);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Should work correctly in HashMap as key")
    void testAsMapKey() {
        java.util.Map<Hex, String> map = new java.util.HashMap<>();
        Hex key = new Hex(1, 2);
        map.put(key, "value");
        assertEquals("value", map.get(new Hex(1, 2)));
    }

    // ---- toString test ----

    @Test
    @DisplayName("Should return formatted string representation")
    void testToString() {
        Hex hex = new Hex(3, -1);
        String str = hex.toString();
        assertTrue(str.contains("3"));
        assertTrue(str.contains("-1"));
        assertTrue(str.contains("EMPTY"));
    }
}
