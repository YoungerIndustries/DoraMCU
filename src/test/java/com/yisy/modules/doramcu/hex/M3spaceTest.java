package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the M3space class.
 */
class M3spaceTest {

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create M3space at origin with default constructor")
    void testDefaultConstructor() {
        M3space space = new M3space();
        assertEquals(0, space.getX());
        assertEquals(0, space.getY());
        assertEquals(0, space.getZ());
        assertEquals(HexState.EMPTY, space.getState());
        assertEquals(Material.Aluminum, space.getMaterial());
        assertEquals(1, space.getHitpoints());
    }

    @Test
    @DisplayName("Should create M3space with explicit coordinates")
    void testThreeArgConstructor() {
        M3space space = new M3space(3, 5, 7);
        assertEquals(3, space.getX());
        assertEquals(5, space.getY());
        assertEquals(7, space.getZ());
        assertEquals(Material.Aluminum, space.getMaterial());
        assertEquals(1, space.getHitpoints());
    }

    @Test
    @DisplayName("Should create M3space with explicit material")
    void testFourArgConstructor() {
        M3space space = new M3space(1, 2, 3, Material.Steel);
        assertEquals(1, space.getX());
        assertEquals(2, space.getY());
        assertEquals(3, space.getZ());
        assertEquals(Material.Steel, space.getMaterial());
        assertEquals(2, space.getHitpoints());
    }

    @Test
    @DisplayName("Should create M3space with each material type")
    void testConstructorWithAllMaterials() {
        assertEquals(0, new M3space(0, 0, 0, Material.Empty).getHitpoints());
        assertEquals(1, new M3space(0, 0, 0, Material.Aluminum).getHitpoints());
        assertEquals(2, new M3space(0, 0, 0, Material.Steel).getHitpoints());
        assertEquals(3, new M3space(0, 0, 0, Material.Titanium).getHitpoints());
        assertEquals(4, new M3space(0, 0, 0, Material.Composite).getHitpoints());
        assertEquals(5, new M3space(0, 0, 0, Material.Explosive).getHitpoints());
    }

    // ---- Coordinate accessor tests ----

    @Test
    @DisplayName("Should return correct coordinates")
    void testCoordinateAccessors() {
        M3space space = new M3space(5, 3, 8);
        assertEquals(5, space.getX());
        assertEquals(3, space.getY());
        assertEquals(8, space.getZ());
    }

    @Test
    @DisplayName("Should handle negative coordinates")
    void testNegativeCoordinates() {
        M3space space = new M3space(-1, -2, -3);
        assertEquals(-1, space.getX());
        assertEquals(-2, space.getY());
        assertEquals(-3, space.getZ());
    }

    // ---- Material and hitpoints tests ----

    @Test
    @DisplayName("Should get and set material")
    void testSetMaterial() {
        M3space space = new M3space(0, 0, 0, Material.Aluminum);
        space.setMaterial(Material.Titanium);
        assertEquals(Material.Titanium, space.getMaterial());
        assertEquals(3, space.getHitpoints());
    }

    @Test
    @DisplayName("Should reset hitpoints when changing material")
    void testSetMaterialResetsHitpoints() {
        M3space space = new M3space(0, 0, 0, Material.Explosive);
        assertEquals(5, space.getHitpoints());
        space.setMaterial(Material.Empty);
        assertEquals(0, space.getHitpoints());
    }

    @Test
    @DisplayName("Should get max hitpoints from material value")
    void testGetMaxHitpoints() {
        M3space space = new M3space(0, 0, 0, Material.Steel);
        assertEquals(2, space.getMaxHitpoints());
    }

    @Test
    @DisplayName("Should set hitpoints within range")
    void testSetHitpoints() {
        M3space space = new M3space(0, 0, 0, Material.Titanium);
        space.setHitpoints(1);
        assertEquals(1, space.getHitpoints());
    }

    @Test
    @DisplayName("Should clamp hitpoints to max")
    void testSetHitpointsClampToMax() {
        M3space space = new M3space(0, 0, 0, Material.Steel);
        space.setHitpoints(100);
        assertEquals(2, space.getHitpoints());
    }

    @Test
    @DisplayName("Should clamp hitpoints to zero")
    void testSetHitpointsClampToZero() {
        M3space space = new M3space(0, 0, 0, Material.Steel);
        space.setHitpoints(-10);
        assertEquals(0, space.getHitpoints());
    }

    // ---- Damage tests ----

    @Test
    @DisplayName("Should reduce hitpoints on damage")
    void testDamage() {
        M3space space = new M3space(0, 0, 0, Material.Titanium);
        assertFalse(space.damage(1));
        assertEquals(2, space.getHitpoints());
    }

    @Test
    @DisplayName("Should destroy cell when damage reduces hitpoints to zero")
    void testDamageDestroysAtZero() {
        M3space space = new M3space(0, 0, 0, Material.Aluminum);
        assertTrue(space.damage(1));
        assertEquals(0, space.getHitpoints());
        assertTrue(space.isDestroyed());
    }

    @Test
    @DisplayName("Should destroy cell when damage exceeds hitpoints")
    void testDamageDestroysWhenExceeding() {
        M3space space = new M3space(0, 0, 0, Material.Steel);
        assertTrue(space.damage(10));
        assertEquals(0, space.getHitpoints());
        assertTrue(space.isDestroyed());
    }

    @Test
    @DisplayName("Should not destroy cell when damage is less than hitpoints")
    void testDamageNotDestroyed() {
        M3space space = new M3space(0, 0, 0, Material.Explosive);
        assertFalse(space.damage(3));
        assertEquals(2, space.getHitpoints());
        assertFalse(space.isDestroyed());
    }

    @Test
    @DisplayName("Should return true for isDamaged when hitpoints reduced")
    void testIsDamaged() {
        M3space space = new M3space(0, 0, 0, Material.Titanium);
        assertFalse(space.isDamaged());
        space.damage(1);
        assertTrue(space.isDamaged());
    }

    @Test
    @DisplayName("Should not be damaged when at full hitpoints")
    void testIsNotDamagedAtFull() {
        M3space space = new M3space(0, 0, 0, Material.Steel);
        assertFalse(space.isDamaged());
    }

    // ---- State machine tests ----

    @Test
    @DisplayName("Should start in EMPTY state")
    void testInitialState() {
        M3space space = new M3space(0, 0, 0);
        assertEquals(HexState.EMPTY, space.getState());
        assertTrue(space.isInState(HexState.EMPTY));
    }

    @Test
    @DisplayName("Should transition EMPTY to OCCUPIED on place")
    void testPlaceTransition() {
        M3space space = new M3space(0, 0, 0);
        assertTrue(space.place("unit"));
        assertEquals(HexState.OCCUPIED, space.getState());
        assertEquals("unit", space.getPayload());
    }

    @Test
    @DisplayName("Should fail to place on already occupied cell")
    void testPlaceOnOccupied() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit1");
        assertFalse(space.place("unit2"));
        assertEquals("unit1", space.getPayload());
    }

    @Test
    @DisplayName("Should fail to place on blocked cell")
    void testPlaceOnBlocked() {
        M3space space = new M3space(0, 0, 0);
        space.block();
        assertFalse(space.place("unit"));
        assertEquals(HexState.BLOCKED, space.getState());
    }

    @Test
    @DisplayName("Should transition OCCUPIED to EMPTY on remove")
    void testRemoveTransition() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        Object removed = space.remove();
        assertEquals("unit", removed);
        assertEquals(HexState.EMPTY, space.getState());
        assertNull(space.getPayload());
    }

    @Test
    @DisplayName("Should return null when removing from empty cell")
    void testRemoveFromEmpty() {
        M3space space = new M3space(0, 0, 0);
        assertNull(space.remove());
        assertEquals(HexState.EMPTY, space.getState());
    }

    @Test
    @DisplayName("Should transition EMPTY to BLOCKED on block")
    void testBlockTransition() {
        M3space space = new M3space(0, 0, 0);
        assertTrue(space.block());
        assertEquals(HexState.BLOCKED, space.getState());
    }

    @Test
    @DisplayName("Should fail to block already occupied cell")
    void testBlockOnOccupied() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        assertFalse(space.block());
        assertEquals(HexState.OCCUPIED, space.getState());
    }

    @Test
    @DisplayName("Should transition BLOCKED to EMPTY on unblock")
    void testUnblockTransition() {
        M3space space = new M3space(0, 0, 0);
        space.block();
        assertTrue(space.unblock());
        assertEquals(HexState.EMPTY, space.getState());
    }

    @Test
    @DisplayName("Should fail to unblock empty cell")
    void testUnblockOnEmpty() {
        M3space space = new M3space(0, 0, 0);
        assertFalse(space.unblock());
        assertEquals(HexState.EMPTY, space.getState());
    }

    @Test
    @DisplayName("Should transition OCCUPIED to SELECTED on select")
    void testSelectTransition() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        assertTrue(space.select());
        assertEquals(HexState.SELECTED, space.getState());
    }

    @Test
    @DisplayName("Should fail to select empty cell")
    void testSelectOnEmpty() {
        M3space space = new M3space(0, 0, 0);
        assertFalse(space.select());
        assertEquals(HexState.EMPTY, space.getState());
    }

    @Test
    @DisplayName("Should transition SELECTED to OCCUPIED on deselect")
    void testDeselectTransition() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        space.select();
        assertTrue(space.deselect());
        assertEquals(HexState.OCCUPIED, space.getState());
        assertEquals("unit", space.getPayload());
    }

    @Test
    @DisplayName("Should fail to deselect non-selected cell")
    void testDeselectOnOccupied() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        assertFalse(space.deselect());
        assertEquals(HexState.OCCUPIED, space.getState());
    }

    @Test
    @DisplayName("Should return correct possible events for EMPTY state")
    void testPossibleEventsEmpty() {
        M3space space = new M3space(0, 0, 0);
        List<HexEvent> events = space.getPossibleEvents();
        assertTrue(events.contains(HexEvent.PLACE));
        assertTrue(events.contains(HexEvent.BLOCK));
        assertTrue(events.contains(HexEvent.DESTROY));
    }

    @Test
    @DisplayName("Should return correct possible events for OCCUPIED state")
    void testPossibleEventsOccupied() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        List<HexEvent> events = space.getPossibleEvents();
        assertTrue(events.contains(HexEvent.REMOVE));
        assertTrue(events.contains(HexEvent.SELECT));
        assertTrue(events.contains(HexEvent.DESTROY));
    }

    @Test
    @DisplayName("Should reset cell to EMPTY state and clear payload")
    void testReset() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        space.reset();
        assertEquals(HexState.EMPTY, space.getState());
        assertNull(space.getPayload());
    }

    @Test
    @DisplayName("Should send raw event and return true for valid transition")
    void testSendEvent() {
        M3space space = new M3space(0, 0, 0);
        assertTrue(space.sendEvent(HexEvent.PLACE));
        assertEquals(HexState.OCCUPIED, space.getState());
    }

    @Test
    @DisplayName("Should send raw event and return false for invalid transition")
    void testSendEventInvalid() {
        M3space space = new M3space(0, 0, 0);
        assertFalse(space.sendEvent(HexEvent.REMOVE));
        assertEquals(HexState.EMPTY, space.getState());
    }

    @Test
    @DisplayName("Should complete full place-select-deselect-remove cycle")
    void testFullCycle() {
        M3space space = new M3space(1, 2, 3);
        assertTrue(space.place("entity"));
        assertEquals(HexState.OCCUPIED, space.getState());

        assertTrue(space.select());
        assertEquals(HexState.SELECTED, space.getState());

        assertTrue(space.deselect());
        assertEquals(HexState.OCCUPIED, space.getState());

        Object removed = space.remove();
        assertEquals("entity", removed);
        assertEquals(HexState.EMPTY, space.getState());
    }

    // ---- Destroy and repair tests ----

    @Test
    @DisplayName("Should destroy from EMPTY state")
    void testDestroyFromEmpty() {
        M3space space = new M3space(0, 0, 0);
        assertTrue(space.destroy());
        assertEquals(HexState.DESTROYED, space.getState());
        assertTrue(space.isDestroyed());
    }

    @Test
    @DisplayName("Should destroy from OCCUPIED state and clear payload")
    void testDestroyFromOccupied() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        assertTrue(space.destroy());
        assertEquals(HexState.DESTROYED, space.getState());
        assertNull(space.getPayload());
    }

    @Test
    @DisplayName("Should destroy from BLOCKED state")
    void testDestroyFromBlocked() {
        M3space space = new M3space(0, 0, 0);
        space.block();
        assertTrue(space.destroy());
        assertEquals(HexState.DESTROYED, space.getState());
    }

    @Test
    @DisplayName("Should destroy from SELECTED state")
    void testDestroyFromSelected() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        space.select();
        assertTrue(space.destroy());
        assertEquals(HexState.DESTROYED, space.getState());
    }

    @Test
    @DisplayName("Should fail to destroy already destroyed cell")
    void testDestroyAlreadyDestroyed() {
        M3space space = new M3space(0, 0, 0);
        space.destroy();
        assertFalse(space.destroy());
        assertEquals(HexState.DESTROYED, space.getState());
    }

    @Test
    @DisplayName("Should repair destroyed cell to EMPTY")
    void testRepair() {
        M3space space = new M3space(0, 0, 0);
        space.destroy();
        assertTrue(space.repair());
        assertEquals(HexState.EMPTY, space.getState());
        assertFalse(space.isDestroyed());
    }

    @Test
    @DisplayName("Should fail to repair non-destroyed cell")
    void testRepairNonDestroyed() {
        M3space space = new M3space(0, 0, 0);
        assertFalse(space.repair());
        assertEquals(HexState.EMPTY, space.getState());
    }

    @Test
    @DisplayName("Should complete destroy-repair cycle")
    void testDestroyRepairCycle() {
        M3space space = new M3space(0, 0, 0);
        space.place("unit");
        space.destroy();
        assertTrue(space.isDestroyed());
        space.repair();
        assertEquals(HexState.EMPTY, space.getState());
        // Should be able to place again after repair
        assertTrue(space.place("newUnit"));
        assertEquals(HexState.OCCUPIED, space.getState());
    }

    // ---- Neighbor tests ----

    @Test
    @DisplayName("Should return neighbor at correct offset")
    void testNeighbor() {
        M3space space = new M3space(5, 5, 5);
        M3space n = space.neighbor(1, 0, 0);
        assertEquals(6, n.getX());
        assertEquals(5, n.getY());
        assertEquals(5, n.getZ());
    }

    @Test
    @DisplayName("Should return 6 face neighbors")
    void testFaceNeighbors() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.faceNeighbors();
        assertEquals(6, neighbors.size());
    }

    @Test
    @DisplayName("Should return 12 edge neighbors")
    void testEdgeNeighbors() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.edgeNeighbors();
        assertEquals(12, neighbors.size());
    }

    @Test
    @DisplayName("Should return 8 corner neighbors")
    void testCornerNeighbors() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.cornerNeighbors();
        assertEquals(8, neighbors.size());
    }

    @Test
    @DisplayName("Should return 26 total neighbors")
    void testAllNeighbors() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.allNeighbors();
        assertEquals(26, neighbors.size());
    }

    @Test
    @DisplayName("Should have distinct face neighbors")
    void testFaceNeighborsDistinct() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.faceNeighbors();
        assertEquals(6, new java.util.HashSet<>(neighbors).size());
    }

    @Test
    @DisplayName("Should have distinct edge neighbors")
    void testEdgeNeighborsDistinct() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.edgeNeighbors();
        assertEquals(12, new java.util.HashSet<>(neighbors).size());
    }

    @Test
    @DisplayName("Should have distinct corner neighbors")
    void testCornerNeighborsDistinct() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.cornerNeighbors();
        assertEquals(8, new java.util.HashSet<>(neighbors).size());
    }

    @Test
    @DisplayName("Should have all 26 neighbors distinct")
    void testAllNeighborsDistinct() {
        M3space space = new M3space(0, 0, 0);
        List<M3space> neighbors = space.allNeighbors();
        assertEquals(26, new java.util.HashSet<>(neighbors).size());
    }

    // ---- Distance tests ----

    @Test
    @DisplayName("Should return Manhattan distance")
    void testDistance() {
        M3space a = new M3space(0, 0, 0);
        M3space b = new M3space(3, 4, 5);
        assertEquals(12, a.distance(b));
    }

    @Test
    @DisplayName("Should return 0 distance to self")
    void testDistanceToSelf() {
        M3space a = new M3space(1, 2, 3);
        assertEquals(0, a.distance(a));
    }

    @Test
    @DisplayName("Should return symmetric Manhattan distance")
    void testDistanceSymmetric() {
        M3space a = new M3space(1, 2, 3);
        M3space b = new M3space(4, 5, 6);
        assertEquals(a.distance(b), b.distance(a));
    }

    @Test
    @DisplayName("Should return Chebyshev distance")
    void testChebyshevDistance() {
        M3space a = new M3space(0, 0, 0);
        M3space b = new M3space(3, 4, 5);
        assertEquals(5, a.chebyshevDistance(b));
    }

    @Test
    @DisplayName("Should return 0 Chebyshev distance to self")
    void testChebyshevDistanceToSelf() {
        M3space a = new M3space(1, 2, 3);
        assertEquals(0, a.chebyshevDistance(a));
    }

    @Test
    @DisplayName("Should return symmetric Chebyshev distance")
    void testChebyshevDistanceSymmetric() {
        M3space a = new M3space(1, 2, 3);
        M3space b = new M3space(4, 5, 6);
        assertEquals(a.chebyshevDistance(b), b.chebyshevDistance(a));
    }

    @Test
    @DisplayName("Should return correct Chebyshev distance for same-axis offset")
    void testChebyshevDistanceSameAxis() {
        M3space a = new M3space(0, 0, 0);
        M3space b = new M3space(5, 0, 0);
        assertEquals(5, a.chebyshevDistance(b));
    }

    // ---- Object contract tests ----

    @Test
    @DisplayName("Should equal itself")
    void testEqualsReflexive() {
        M3space space = new M3space(1, 2, 3);
        assertEquals(space, space);
    }

    @Test
    @DisplayName("Should equal cell with same coordinates")
    void testEqualsSameCoordinates() {
        M3space a = new M3space(1, 2, 3);
        M3space b = new M3space(1, 2, 3);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Should not equal cell with different coordinates")
    void testEqualsDifferentCoordinates() {
        M3space a = new M3space(1, 2, 3);
        M3space b = new M3space(2, 1, 3);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Should not equal null")
    void testEqualsNull() {
        M3space space = new M3space(0, 0, 0);
        assertNotEquals(null, space);
    }

    @Test
    @DisplayName("Should not equal different class")
    void testEqualsDifferentClass() {
        M3space space = new M3space(0, 0, 0);
        assertNotEquals(space, new Object());
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        M3space a = new M3space(1, 2, 3);
        M3space b = new M3space(1, 2, 3);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Should work as HashMap key")
    void testHashMapKey() {
        Map<M3space, String> map = new HashMap<>();
        M3space key = new M3space(1, 2, 3);
        map.put(key, "value");

        M3space sameKey = new M3space(1, 2, 3);
        assertEquals("value", map.get(sameKey));
    }

    @Test
    @DisplayName("Should be equal symmetrically")
    void testEqualsSymmetric() {
        M3space a = new M3space(3, 2, 1);
        M3space b = new M3space(3, 2, 1);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    @DisplayName("Should be equal transitively")
    void testEqualsTransitive() {
        M3space a = new M3space(1, 1, 1);
        M3space b = new M3space(1, 1, 1);
        M3space c = new M3space(1, 1, 1);
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    // ---- toString tests ----

    @Test
    @DisplayName("Should return toString with coordinates and state")
    void testToString() {
        M3space space = new M3space(1, 2, 3, Material.Steel);
        String str = space.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
        assertTrue(str.contains("EMPTY"));
        assertTrue(str.contains("Steel"));
    }

    @Test
    @DisplayName("Should return toString with OCCUPIED state")
    void testToStringOccupied() {
        M3space space = new M3space(0, 0, 0);
        space.place("entity");
        String str = space.toString();
        assertTrue(str.contains("OCCUPIED"));
    }

    // ---- Negative coordinate tests ----

    @Test
    @DisplayName("Should handle negative coordinates in neighbors")
    void testNeighborsWithNegativeCoordinates() {
        M3space space = new M3space(-1, -1, -1);
        List<M3space> neighbors = space.allNeighbors();
        assertEquals(26, neighbors.size());
    }

    @Test
    @DisplayName("Should handle negative coordinates in distance")
    void testDistanceWithNegativeCoordinates() {
        M3space a = new M3space(-3, -2, -1);
        M3space b = new M3space(3, 2, 1);
        assertEquals(12, a.distance(b));
        assertEquals(6, a.chebyshevDistance(b));
    }
}
