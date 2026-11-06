package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Hex3D class.
 */
class Hex3DTest {

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create hex at origin with default constructor")
    void testDefaultConstructor() {
        Hex3D hex = new Hex3D();
        assertEquals(0, hex.getX());
        assertEquals(0, hex.getY());
        assertEquals(0, hex.getZ());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should create hex with derived z coordinate")
    void testTwoArgConstructor() {
        Hex3D hex = new Hex3D(3, -1);
        assertEquals(3, hex.getX());
        assertEquals(-1, hex.getY());
        assertEquals(-2, hex.getZ()); // z = -x - y = -3 - (-1) = -2
    }

    @Test
    @DisplayName("Should create hex with explicit z coordinate")
    void testThreeArgConstructor() {
        Hex3D hex = new Hex3D(1, 2, -3);
        assertEquals(1, hex.getX());
        assertEquals(2, hex.getY());
        assertEquals(-3, hex.getZ());
    }

    @Test
    @DisplayName("Should reject invalid cubic coordinates")
    void testInvalidCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Hex3D(1, 2, 3));
    }

    @Test
    @DisplayName("Should accept valid coordinates summing to zero")
    void testValidCoordinatesSumZero() {
        Hex3D hex = new Hex3D(5, -3, -2);
        assertEquals(5, hex.getX());
        assertEquals(-3, hex.getY());
        assertEquals(-2, hex.getZ());
    }

    // ---- State machine tests ----

    @Test
    @DisplayName("Should start in EMPTY state")
    void testInitialState() {
        Hex3D hex = new Hex3D(0, 0);
        assertEquals(HexState.EMPTY, hex.getState());
        assertTrue(hex.isInState(HexState.EMPTY));
    }

    @Test
    @DisplayName("Should transition EMPTY to OCCUPIED on PLACE")
    void testPlaceTransition() {
        Hex3D hex = new Hex3D(0, 0);
        assertTrue(hex.place("unit"));
        assertEquals(HexState.OCCUPIED, hex.getState());
        assertEquals("unit", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to place on already occupied hex")
    void testPlaceOnOccupied() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit1");
        assertFalse(hex.place("unit2"));
        assertEquals("unit1", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to place on blocked hex")
    void testPlaceOnBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        assertFalse(hex.place("unit"));
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should transition OCCUPIED to EMPTY on REMOVE")
    void testRemoveTransition() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        Object removed = hex.remove();
        assertEquals("unit", removed);
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should return null when removing from empty hex")
    void testRemoveFromEmpty() {
        Hex3D hex = new Hex3D(0, 0);
        assertNull(hex.remove());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should fail to remove from selected hex")
    void testRemoveFromSelected() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.select();
        assertNull(hex.remove());
        assertEquals(HexState.SELECTED, hex.getState());
    }

    @Test
    @DisplayName("Should transition EMPTY to BLOCKED on BLOCK")
    void testBlockTransition() {
        Hex3D hex = new Hex3D(0, 0);
        assertTrue(hex.block());
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to block already occupied hex")
    void testBlockOnOccupied() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        assertFalse(hex.block());
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should transition BLOCKED to EMPTY on UNBLOCK")
    void testUnblockTransition() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        assertTrue(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should fail to unblock empty hex")
    void testUnblockOnEmpty() {
        Hex3D hex = new Hex3D(0, 0);
        assertFalse(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should transition OCCUPIED to SELECTED on SELECT")
    void testSelectTransition() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        assertTrue(hex.select());
        assertEquals(HexState.SELECTED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to select empty hex")
    void testSelectOnEmpty() {
        Hex3D hex = new Hex3D(0, 0);
        assertFalse(hex.select());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should transition SELECTED to OCCUPIED on DESELECT")
    void testDeselectTransition() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.select();
        assertTrue(hex.deselect());
        assertEquals(HexState.OCCUPIED, hex.getState());
        assertEquals("unit", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to deselect non-selected hex")
    void testDeselectOnOccupied() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        assertFalse(hex.deselect());
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should return correct possible events for EMPTY state")
    void testPossibleEventsEmpty() {
        Hex3D hex = new Hex3D(0, 0);
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.PLACE));
        assertTrue(events.contains(HexEvent.BLOCK));
        assertTrue(events.contains(HexEvent.DESTROY));
        assertEquals(3, events.size());
    }

    @Test
    @DisplayName("Should return correct possible events for OCCUPIED state")
    void testPossibleEventsOccupied() {
        Hex3D hex = new Hex3D(0, 0);
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
        Hex3D hex = new Hex3D(0, 0);
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
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.UNBLOCK));
        assertTrue(events.contains(HexEvent.DESTROY));
        assertEquals(2, events.size());
    }

    @Test
    @DisplayName("Should return correct possible events for DESTROYED state")
    void testPossibleEventsDestroyed() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.sendEvent(HexEvent.DESTROY);
        assertEquals(HexState.DESTROYED, hex.getState());
        List<HexEvent> events = hex.getPossibleEvents();
        assertTrue(events.contains(HexEvent.REPAIR));
        assertEquals(1, events.size());
    }

    @Test
    @DisplayName("Should reset hex to EMPTY state and clear payload")
    void testReset() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.reset();
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should reset selected hex to EMPTY")
    void testResetFromSelected() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.select();
        hex.reset();
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should send raw event and return true for valid transition")
    void testSendEvent() {
        Hex3D hex = new Hex3D(0, 0);
        assertTrue(hex.sendEvent(HexEvent.PLACE));
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should send raw event and return false for invalid transition")
    void testSendEventInvalid() {
        Hex3D hex = new Hex3D(0, 0);
        assertFalse(hex.sendEvent(HexEvent.REMOVE));
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should complete full place-select-deselect-remove cycle")
    void testFullCycle() {
        Hex3D hex = new Hex3D(1, 0);
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
        Hex3D hex = new Hex3D(0, 1);
        assertTrue(hex.block());
        assertEquals(HexState.BLOCKED, hex.getState());

        assertTrue(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    // ---- 3D Hex arithmetic tests ----

    @Test
    @DisplayName("Should return correct neighbor in each direction")
    void testNeighbor() {
        Hex3D center = new Hex3D(0, 0, 0);

        Hex3D n1 = center.neighbor(Hex3DDirection.POS_X_NEG_Y);
        assertEquals(1, n1.getX());
        assertEquals(-1, n1.getY());
        assertEquals(0, n1.getZ());

        Hex3D n2 = center.neighbor(Hex3DDirection.POS_X_NEG_Z);
        assertEquals(1, n2.getX());
        assertEquals(0, n2.getY());
        assertEquals(-1, n2.getZ());

        Hex3D n3 = center.neighbor(Hex3DDirection.POS_Y_NEG_Z);
        assertEquals(0, n3.getX());
        assertEquals(1, n3.getY());
        assertEquals(-1, n3.getZ());
    }

    @Test
    @DisplayName("Should return all 6 neighbors")
    void testNeighbors() {
        Hex3D center = new Hex3D(0, 0);
        List<Hex3D> neighbors = center.neighbors();
        assertEquals(6, neighbors.size());
    }

    @Test
    @DisplayName("Should return correct distance between hexes")
    void testDistance() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(3, 0, -3);
        assertEquals(3, a.distance(b));

        // Distance to self
        assertEquals(0, a.distance(a));

        Hex3D c = new Hex3D(1, -1, 0);
        assertEquals(1, a.distance(c));
    }

    @Test
    @DisplayName("Should return symmetric distance")
    void testDistanceSymmetric() {
        Hex3D a = new Hex3D(2, -3, 1);
        Hex3D b = new Hex3D(-1, 5, -4);
        assertEquals(a.distance(b), b.distance(a));
    }

    @Test
    @DisplayName("Should lerp at t=0 return this hex")
    void testLerpAtZero() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(5, 3, -8);
        Hex3D result = a.lerp(b, 0.0);
        assertEquals(a.getX(), result.getX());
        assertEquals(a.getY(), result.getY());
        assertEquals(a.getZ(), result.getZ());
    }

    @Test
    @DisplayName("Should lerp at t=1 return other hex")
    void testLerpAtOne() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(5, 3, -8);
        Hex3D result = a.lerp(b, 1.0);
        assertEquals(b.getX(), result.getX());
        assertEquals(b.getY(), result.getY());
        assertEquals(b.getZ(), result.getZ());
    }

    @Test
    @DisplayName("Should lerp at t=0.5 return midpoint")
    void testLerpAtHalf() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(4, 2, -6);
        Hex3D result = a.lerp(b, 0.5);
        assertEquals(2, result.getX());
        assertEquals(1, result.getY());
        assertEquals(-3, result.getZ());
    }

    @Test
    @DisplayName("Should return line with correct number of hexes")
    void testLine() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(3, 0, -3);
        List<Hex3D> line = a.line(b);
        assertEquals(4, line.size()); // distance + 1
        assertEquals(a, line.get(0));
        assertEquals(b, line.get(line.size() - 1));
    }

    @Test
    @DisplayName("Should return line of size 1 for same hex")
    void testLineToSelf() {
        Hex3D a = new Hex3D(0, 0, 0);
        List<Hex3D> line = a.line(a);
        assertEquals(1, line.size());
        assertEquals(a, line.get(0));
    }

    @Test
    @DisplayName("Should return line with valid cubic coordinates")
    void testLineValidCoordinates() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(3, 1, -4);
        List<Hex3D> line = a.line(b);
        for (Hex3D hex : line) {
            assertEquals(0, hex.getX() + hex.getY() + hex.getZ(),
                    "Each hex in line should have coordinates summing to 0");
        }
    }

    // ---- Object contract tests ----

    @Test
    @DisplayName("Should equal itself")
    void testEqualsReflexive() {
        Hex3D hex = new Hex3D(1, 2, -3);
        assertEquals(hex, hex);
    }

    @Test
    @DisplayName("Should equal hex with same coordinates")
    void testEqualsSameCoordinates() {
        Hex3D a = new Hex3D(1, 2, -3);
        Hex3D b = new Hex3D(1, 2, -3);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Should not equal hex with different coordinates")
    void testEqualsDifferentCoordinates() {
        Hex3D a = new Hex3D(1, 2, -3);
        Hex3D b = new Hex3D(2, 1, -3);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Should not equal null")
    void testEqualsNull() {
        Hex3D hex = new Hex3D(0, 0);
        assertNotEquals(null, hex);
    }

    @Test
    @DisplayName("Should not equal different class")
    void testEqualsDifferentClass() {
        Hex3D hex = new Hex3D(0, 0);
        assertNotEquals(hex, new Object());
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        Hex3D a = new Hex3D(1, 2, -3);
        Hex3D b = new Hex3D(1, 2, -3);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Should work as HashMap key")
    void testHashMapKey() {
        Map<Hex3D, String> map = new HashMap<>();
        Hex3D key = new Hex3D(1, 2, -3);
        map.put(key, "value");

        Hex3D sameKey = new Hex3D(1, 2, -3);
        assertEquals("value", map.get(sameKey));
    }

    @Test
    @DisplayName("Should return correct toString")
    void testToString() {
        Hex3D hex = new Hex3D(1, 2, -3);
        String str = hex.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("-3"));
        assertTrue(str.contains("EMPTY"));
    }

    // ---- Additional state machine negative cases ----

    @Test
    @DisplayName("Should fail to place on selected hex")
    void testPlaceOnSelected() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.select();
        assertFalse(hex.place("other"));
        assertEquals(HexState.SELECTED, hex.getState());
        assertEquals("unit", hex.getPayload());
    }

    @Test
    @DisplayName("Should fail to block on selected hex")
    void testBlockOnSelected() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.select();
        assertFalse(hex.block());
        assertEquals(HexState.SELECTED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to block on already blocked hex")
    void testBlockOnBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        assertFalse(hex.block());
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to remove from blocked hex")
    void testRemoveFromBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        assertNull(hex.remove());
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to select on blocked hex")
    void testSelectOnBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        assertFalse(hex.select());
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to deselect on empty hex")
    void testDeselectOnEmpty() {
        Hex3D hex = new Hex3D(0, 0);
        assertFalse(hex.deselect());
        assertEquals(HexState.EMPTY, hex.getState());
    }

    @Test
    @DisplayName("Should fail to deselect on blocked hex")
    void testDeselectOnBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        assertFalse(hex.deselect());
        assertEquals(HexState.BLOCKED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to unblock on occupied hex")
    void testUnblockOnOccupied() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        assertFalse(hex.unblock());
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should fail to unblock on selected hex")
    void testUnblockOnSelected() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.select();
        assertFalse(hex.unblock());
        assertEquals(HexState.SELECTED, hex.getState());
    }

    // ---- Additional reset tests ----

    @Test
    @DisplayName("Should reset from blocked state")
    void testResetFromBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        hex.reset();
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    @Test
    @DisplayName("Should reset from occupied state")
    void testResetFromOccupied() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        hex.reset();
        assertEquals(HexState.EMPTY, hex.getState());
        assertNull(hex.getPayload());
    }

    // ---- Additional neighbor tests ----

    @Test
    @DisplayName("Should return neighbor for all 6 directions")
    void testNeighborAllSixDirections() {
        Hex3D center = new Hex3D(0, 0, 0);
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            Hex3D neighbor = center.neighbor(dir);
            assertEquals(1, center.distance(neighbor));
            assertEquals(0, neighbor.getX() + neighbor.getY() + neighbor.getZ());
        }
    }

    @Test
    @DisplayName("Should return 6 distinct neighbors")
    void testNeighborsDistinct() {
        Hex3D center = new Hex3D(0, 0);
        List<Hex3D> neighbors = center.neighbors();
        assertEquals(6, new java.util.HashSet<>(neighbors).size());
    }

    @Test
    @DisplayName("Should have all neighbors at distance 1")
    void testNeighborsDistanceOne() {
        Hex3D center = new Hex3D(5, -3, -2);
        List<Hex3D> neighbors = center.neighbors();
        for (Hex3D neighbor : neighbors) {
            assertEquals(1, center.distance(neighbor));
        }
    }

    // ---- Additional distance tests ----

    @Test
    @DisplayName("Should return distance with negative coordinates")
    void testDistanceNegative() {
        Hex3D a = new Hex3D(-3, 2, 1);
        Hex3D b = new Hex3D(2, -5, 3);
        int dist = a.distance(b);
        assertTrue(dist > 0);
        assertEquals(dist, b.distance(a));
    }

    @Test
    @DisplayName("Should return distance 1 for adjacent hexes")
    void testDistanceAdjacent() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(1, -1, 0);
        assertEquals(1, a.distance(b));
    }

    // ---- Additional lerp tests ----

    @Test
    @DisplayName("Should lerp with negative coordinates")
    void testLerpNegativeCoordinates() {
        Hex3D a = new Hex3D(-3, 2, 1);
        Hex3D b = new Hex3D(3, -2, -1);
        Hex3D result = a.lerp(b, 0.5);
        assertEquals(0, result.getX());
        assertEquals(0, result.getY());
        assertEquals(0, result.getZ());
    }

    @Test
    @DisplayName("Should lerp return valid cubic hex")
    void testLerpValidCubic() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(5, 3, -8);
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            Hex3D result = a.lerp(b, t);
            assertEquals(0, result.getX() + result.getY() + result.getZ(),
                    "Lerp result should have valid cubic coordinates");
        }
    }

    // ---- Additional line tests ----

    @Test
    @DisplayName("Should return line of size 2 for adjacent hexes")
    void testLineAdjacent() {
        Hex3D a = new Hex3D(0, 0, 0);
        Hex3D b = new Hex3D(1, -1, 0);
        List<Hex3D> line = a.line(b);
        assertEquals(2, line.size());
        assertEquals(a, line.get(0));
        assertEquals(b, line.get(1));
    }

    @Test
    @DisplayName("Should return line with all valid hexes")
    void testLineAllValidHexes() {
        Hex3D a = new Hex3D(-5, 3, 2);
        Hex3D b = new Hex3D(5, -3, -2);
        List<Hex3D> line = a.line(b);
        for (Hex3D hex : line) {
            assertEquals(0, hex.getX() + hex.getY() + hex.getZ(),
                    "Each hex in line should have valid cubic coordinates");
        }
    }

    // ---- Additional equals/hashCode tests ----

    @Test
    @DisplayName("Should be equal symmetrically")
    void testEqualsSymmetric() {
        Hex3D a = new Hex3D(3, -2, -1);
        Hex3D b = new Hex3D(3, -2, -1);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    @DisplayName("Should be equal transitively")
    void testEqualsTransitive() {
        Hex3D a = new Hex3D(1, -1, 0);
        Hex3D b = new Hex3D(1, -1, 0);
        Hex3D c = new Hex3D(1, -1, 0);
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    @DisplayName("Should have consistent hashCode across multiple calls")
    void testHashCodeConsistent() {
        Hex3D hex = new Hex3D(7, -4, -3);
        int h1 = hex.hashCode();
        int h2 = hex.hashCode();
        assertEquals(h1, h2);
    }

    // ---- Additional payload tests ----

    @Test
    @DisplayName("Should preserve payload after deselect")
    void testPayloadPreservedAfterDeselect() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("myEntity");
        hex.select();
        hex.deselect();
        assertEquals("myEntity", hex.getPayload());
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    @Test
    @DisplayName("Should have null payload after remove")
    void testPayloadNullAfterRemove() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("myEntity");
        hex.remove();
        assertNull(hex.getPayload());
    }

    // ---- Additional isInState tests ----

    @Test
    @DisplayName("Should return false for isInState with wrong state")
    void testIsInStateFalse() {
        Hex3D hex = new Hex3D(0, 0);
        assertFalse(hex.isInState(HexState.OCCUPIED));
        assertFalse(hex.isInState(HexState.BLOCKED));
        assertFalse(hex.isInState(HexState.SELECTED));
    }

    @Test
    @DisplayName("Should return true for isInState with correct state after place")
    void testIsInStateTrueAfterPlace() {
        Hex3D hex = new Hex3D(0, 0);
        hex.place("unit");
        assertTrue(hex.isInState(HexState.OCCUPIED));
        assertFalse(hex.isInState(HexState.EMPTY));
    }

    // ---- Additional constructor tests ----

    @Test
    @DisplayName("Should create hex with negative coordinates")
    void testConstructorNegativeCoordinates() {
        Hex3D hex = new Hex3D(-5, 3, 2);
        assertEquals(-5, hex.getX());
        assertEquals(3, hex.getY());
        assertEquals(2, hex.getZ());
    }

    @Test
    @DisplayName("Should create hex with large coordinates")
    void testConstructorLargeCoordinates() {
        Hex3D hex = new Hex3D(1000, -500, -500);
        assertEquals(1000, hex.getX());
        assertEquals(-500, hex.getY());
        assertEquals(-500, hex.getZ());
    }

    @Test
    @DisplayName("Should reject coordinates not summing to zero")
    void testInvalidCoordinatesPositiveSum() {
        assertThrows(IllegalArgumentException.class, () -> new Hex3D(1, 1, 1));
    }

    @Test
    @DisplayName("Should reject coordinates with negative sum")
    void testInvalidCoordinatesNegativeSum() {
        assertThrows(IllegalArgumentException.class, () -> new Hex3D(-1, -1, -1));
    }

    // ---- Additional toString tests ----

    @Test
    @DisplayName("Should return toString with OCCUPIED state")
    void testToStringOccupied() {
        Hex3D hex = new Hex3D(1, 2, -3);
        hex.place("entity");
        String str = hex.toString();
        assertTrue(str.contains("OCCUPIED"));
    }

    @Test
    @DisplayName("Should return toString with BLOCKED state")
    void testToStringBlocked() {
        Hex3D hex = new Hex3D(0, 0);
        hex.block();
        String str = hex.toString();
        assertTrue(str.contains("BLOCKED"));
    }

    // ---- Full state cycle with all states ----

    @Test
    @DisplayName("Should complete full cycle through all states")
    void testFullStateCycle() {
        Hex3D hex = new Hex3D(0, 0);
        // EMPTY -> OCCUPIED
        assertTrue(hex.place("entity"));
        assertEquals(HexState.OCCUPIED, hex.getState());
        // OCCUPIED -> SELECTED
        assertTrue(hex.select());
        assertEquals(HexState.SELECTED, hex.getState());
        // SELECTED -> OCCUPIED
        assertTrue(hex.deselect());
        assertEquals(HexState.OCCUPIED, hex.getState());
        // OCCUPIED -> EMPTY
        hex.remove();
        assertEquals(HexState.EMPTY, hex.getState());
        // EMPTY -> BLOCKED
        assertTrue(hex.block());
        assertEquals(HexState.BLOCKED, hex.getState());
        // BLOCKED -> EMPTY
        assertTrue(hex.unblock());
        assertEquals(HexState.EMPTY, hex.getState());
    }
}
