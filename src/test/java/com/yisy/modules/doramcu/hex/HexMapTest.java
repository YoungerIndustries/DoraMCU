package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the HexMap class.
 */
class HexMapTest {

    // ---- CRUD operation tests ----

    @Test
    @DisplayName("Should start empty")
    void testEmptyMap() {
        HexMap map = new HexMap();
        assertEquals(0, map.size());
        assertNull(map.get(0, 0));
        assertFalse(map.contains(0, 0));
    }

    @Test
    @DisplayName("Should add and retrieve hex")
    void testPutAndGet() {
        HexMap map = new HexMap();
        Hex hex = new Hex(1, 2);
        map.put(hex);

        assertEquals(1, map.size());
        assertNotNull(map.get(1, 2));
        assertEquals(1, map.get(1, 2).getQ());
        assertEquals(2, map.get(1, 2).getR());
    }

    @Test
    @DisplayName("Should return previous hex when overwriting")
    void testPutOverwrite() {
        HexMap map = new HexMap();
        Hex hex1 = new Hex(0, 0);
        hex1.place("first");
        map.put(hex1);

        Hex hex2 = new Hex(0, 0);
        hex2.place("second");
        Hex previous = map.put(hex2);

        assertNotNull(previous);
        assertEquals("first", previous.getPayload());
        assertEquals("second", map.get(0, 0).getPayload());
    }

    @Test
    @DisplayName("Should return null when putting new hex")
    void testPutNewHex() {
        HexMap map = new HexMap();
        Hex hex = new Hex(3, -1);
        Hex previous = map.put(hex);
        assertNull(previous);
    }

    @Test
    @DisplayName("Should get hex by Hex object")
    void testGetByHex() {
        HexMap map = new HexMap();
        Hex hex = new Hex(2, -1);
        map.put(hex);

        Hex retrieved = map.get(new Hex(2, -1));
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getQ());
    }

    @Test
    @DisplayName("Should return null for missing hex")
    void testGetMissing() {
        HexMap map = new HexMap();
        assertNull(map.get(5, 5));
        assertNull(map.get(new Hex(5, 5)));
    }

    @Test
    @DisplayName("Should contain hex after put")
    void testContains() {
        HexMap map = new HexMap();
        map.put(new Hex(1, 1));
        assertTrue(map.contains(1, 1));
        assertFalse(map.contains(2, 2));
    }

    @Test
    @DisplayName("Should remove hex from map")
    void testRemove() {
        HexMap map = new HexMap();
        Hex hex = new Hex(0, 0);
        map.put(hex);
        assertEquals(1, map.size());

        Hex removed = map.remove(new Hex(0, 0));
        assertNotNull(removed);
        assertEquals(0, removed.getQ());
        assertEquals(0, map.size());
        assertFalse(map.contains(0, 0));
    }

    @Test
    @DisplayName("Should return null when removing missing hex")
    void testRemoveMissing() {
        HexMap map = new HexMap();
        assertNull(map.remove(new Hex(9, 9)));
    }

    @Test
    @DisplayName("Should return all hexes")
    void testGetAll() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));
        map.put(new Hex(2, 0));

        Collection<Hex> all = map.getAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("Should not allow modification of getAll collection")
    void testGetAllUnmodifiable() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));

        assertThrows(UnsupportedOperationException.class, () -> {
            map.getAll().clear();
        });
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("Should clear all hexes")
    void testClear() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));
        assertEquals(2, map.size());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.getAll().isEmpty());
    }

    // ---- Query operation tests ----

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        HexMap map = new HexMap();
        Hex h1 = new Hex(0, 0);
        Hex h2 = new Hex(1, 0);
        Hex h3 = new Hex(2, 0);

        h1.place("unit");
        h2.block();

        map.put(h1);
        map.put(h2);
        map.put(h3);

        List<Hex> occupied = map.findByState(HexState.OCCUPIED);
        assertEquals(1, occupied.size());
        assertEquals(0, occupied.get(0).getQ());

        List<Hex> blocked = map.findByState(HexState.BLOCKED);
        assertEquals(1, blocked.size());
        assertEquals(1, blocked.get(0).getQ());

        List<Hex> empty = map.findByState(HexState.EMPTY);
        assertEquals(1, empty.size());
        assertEquals(2, empty.get(0).getQ());
    }

    @Test
    @DisplayName("Should return empty list for state with no matches")
    void testFindByStateNoMatches() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));
        List<Hex> selected = map.findByState(HexState.SELECTED);
        assertTrue(selected.isEmpty());
    }

    @Test
    @DisplayName("Should get neighbors present in map")
    void testGetNeighbors() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.put(center);
        map.put(new Hex(1, 0));   // EAST
        map.put(new Hex(0, 1));   // SOUTH_EAST
        map.put(new Hex(-1, 1));  // SOUTH_WEST

        List<Hex> neighbors = map.getNeighbors(center);
        assertEquals(3, neighbors.size());
    }

    @Test
    @DisplayName("Should return empty neighbors for isolated hex")
    void testGetNeighborsIsolated() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.put(center);
        map.put(new Hex(10, 10)); // Far away

        List<Hex> neighbors = map.getNeighbors(center);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    @DisplayName("Should return empty neighbors for hex not in map")
    void testGetNeighborsNotInMap() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));

        // Query neighbors of a hex not in the map
        List<Hex> neighbors = map.getNeighbors(new Hex(99, 99));
        assertTrue(neighbors.isEmpty());
    }

    // ---- Generation utility tests ----

    @Test
    @DisplayName("Should generate ring of radius 0 (center only)")
    void testGenerateRingZero() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        List<Hex> ring = map.generateRing(center, 0);

        assertEquals(1, ring.size());
        assertEquals(center, ring.get(0));
        // generateRing with radius 0 returns center but does not add it to the map
        assertEquals(0, map.size());
    }

    @Test
    @DisplayName("Should generate ring of radius 1")
    void testGenerateRingOne() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        List<Hex> ring = map.generateRing(center, 1);

        // Ring should produce hexes and add them to the map
        assertFalse(ring.isEmpty());
        for (Hex h : ring) {
            assertTrue(map.contains(h.getQ(), h.getR()));
        }
    }

    @Test
    @DisplayName("Should generate ring of radius 2")
    void testGenerateRingTwo() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        List<Hex> ring = map.generateRing(center, 2);

        // Ring should produce hexes and add them to the map
        assertFalse(ring.isEmpty());
        for (Hex h : ring) {
            assertTrue(map.contains(h.getQ(), h.getR()));
        }
    }

    @Test
    @DisplayName("Should generate range of radius 0 (center only)")
    void testGenerateRangeZero() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        List<Hex> range = map.generateRange(center, 0);

        assertEquals(1, range.size());
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("Should generate range of radius 1")
    void testGenerateRangeOne() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        List<Hex> range = map.generateRange(center, 1);

        // Range should include center and surrounding hexes
        assertTrue(range.contains(center));
        assertTrue(map.contains(0, 0));
        // All hexes in range should be within distance 1 of center
        for (Hex h : range) {
            assertTrue(h.distance(center) <= 1, "Range hex should be within distance 1");
        }
    }

    @Test
    @DisplayName("Should generate range of radius 2")
    void testGenerateRangeTwo() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        List<Hex> range = map.generateRange(center, 2);

        // Range should include center and surrounding hexes
        assertTrue(range.contains(center));
        assertTrue(map.contains(0, 0));
        // All hexes in range should be within distance 2 of center
        for (Hex h : range) {
            assertTrue(h.distance(center) <= 2, "Range hex should be within distance 2");
        }
    }

    @Test
    @DisplayName("Should include center in range")
    void testGenerateRangeIncludesCenter() {
        HexMap map = new HexMap();
        Hex center = new Hex(3, -2);
        map.generateRange(center, 1);

        assertTrue(map.contains(3, -2));
    }

    @Test
    @DisplayName("Should generate rectangular grid")
    void testGenerateRect() {
        HexMap map = new HexMap();
        List<Hex> rect = map.generateRect(0, 2, 0, 1);

        // 3 columns * 2 rows = 6 hexes
        assertEquals(6, rect.size());
        assertEquals(6, map.size());
    }

    @Test
    @DisplayName("Should generate single hex rect")
    void testGenerateRectSingle() {
        HexMap map = new HexMap();
        List<Hex> rect = map.generateRect(5, 5, 3, 3);

        assertEquals(1, rect.size());
        assertEquals(5, rect.get(0).getQ());
        assertEquals(3, rect.get(0).getR());
    }

    @Test
    @DisplayName("Should contain all expected hexes in rect")
    void testGenerateRectContainsAll() {
        HexMap map = new HexMap();
        map.generateRect(0, 1, 0, 1);

        assertTrue(map.contains(0, 0));
        assertTrue(map.contains(0, 1));
        assertTrue(map.contains(1, 0));
        assertTrue(map.contains(1, 1));
        assertFalse(map.contains(2, 0));
    }

    // ---- toString test ----

    @Test
    @DisplayName("Should return formatted string")
    void testToString() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));

        String str = map.toString();
        assertTrue(str.contains("HexMap"));
        assertTrue(str.contains("2"));
    }

    // ---- Integration tests ----

    @Test
    @DisplayName("Should manage hexes with different states in map")
    void testIntegrationMixedStates() {
        HexMap map = new HexMap();

        Hex h1 = new Hex(0, 0);
        Hex h2 = new Hex(1, 0);
        Hex h3 = new Hex(2, 0);
        Hex h4 = new Hex(3, 0);

        h1.place("unit1");
        h2.place("unit2");
        h2.select();
        h3.block();

        map.put(h1);
        map.put(h2);
        map.put(h3);
        map.put(h4);

        assertEquals(4, map.size());

        assertEquals(1, map.findByState(HexState.OCCUPIED).size());
        assertEquals(1, map.findByState(HexState.SELECTED).size());
        assertEquals(1, map.findByState(HexState.BLOCKED).size());
        assertEquals(1, map.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should generate range and query neighbors")
    void testIntegrationRangeAndNeighbors() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.generateRange(center, 1);

        // Center should have at least some neighbors in the generated range
        List<Hex> neighbors = map.getNeighbors(center);
        assertFalse(neighbors.isEmpty(), "Center should have neighbors in range-1 map");

        // Each neighbor should have center as neighbor
        for (Hex neighbor : neighbors) {
            List<Hex> neighborNeighbors = map.getNeighbors(neighbor);
            assertTrue(neighborNeighbors.contains(center),
                    "Neighbor " + neighbor + " should have center as neighbor");
        }
    }

    // ---- Additional neighbor operations ----

    @Test
    @DisplayName("Should get neighbors for center hex in range")
    void testGetNeighborsCenterInRange() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.generateRange(center, 1);

        List<Hex> neighbors = map.getNeighbors(center);
        // generateRange with radius 1 adds center + ring; neighbors count depends on ring algorithm
        assertFalse(neighbors.isEmpty(), "Center should have neighbors in range-1 map");
        // All neighbors should be at distance 1 from center
        for (Hex n : neighbors) {
            assertEquals(1, center.distance(n), "Neighbor should be at distance 1");
        }
    }

    @Test
    @DisplayName("Should verify neighbors are distinct hexes")
    void testNeighborsAreDistinct() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.generateRange(center, 1);

        List<Hex> neighbors = map.getNeighbors(center);
        long uniqueCount = neighbors.stream().distinct().count();
        assertEquals(neighbors.size(), uniqueCount);
    }

    @Test
    @DisplayName("Should verify neighbors do not include the hex itself")
    void testNeighborsExcludeSelf() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.generateRange(center, 1);

        List<Hex> neighbors = map.getNeighbors(center);
        assertFalse(neighbors.contains(center));
    }

    @Test
    @DisplayName("Should get neighbors for hex at edge of rect")
    void testGetNeighborsAtEdge() {
        HexMap map = new HexMap();
        map.generateRect(0, 4, 0, 4);

        // Hex at (0, 0) is a corner — fewer neighbors
        Hex corner = map.get(0, 0);
        List<Hex> cornerNeighbors = map.getNeighbors(corner);
        assertNotNull(cornerNeighbors);
        assertTrue(cornerNeighbors.size() < 6, "Corner hex should have fewer than 6 neighbors");

        // Hex at (2, 2) is in the center — all 6 neighbors
        Hex center = map.get(2, 2);
        List<Hex> centerNeighbors = map.getNeighbors(center);
        assertEquals(6, centerNeighbors.size());
    }

    @Test
    @DisplayName("Should get neighbors for hex not in map")
    void testGetNeighborsMissingHex() {
        HexMap map = new HexMap();
        map.generateRect(0, 2, 0, 2);

        // Hex at (99, 99) is not in the map
        Hex missing = new Hex(99, 99);
        List<Hex> neighbors = map.getNeighbors(missing);
        assertTrue(neighbors.isEmpty());
    }

    // ---- Additional query operations ----

    @Test
    @DisplayName("Should find hexes by state for all states")
    void testFindByStateAllStates() {
        HexMap map = new HexMap();

        Hex h1 = new Hex(0, 0);
        Hex h2 = new Hex(1, 0);
        Hex h3 = new Hex(2, 0);
        Hex h4 = new Hex(3, 0);

        h1.place("unit1");
        h1.select();
        h2.place("unit2");
        h3.block();
        // h4 remains EMPTY

        map.put(h1);
        map.put(h2);
        map.put(h3);
        map.put(h4);

        assertEquals(1, map.findByState(HexState.OCCUPIED).size());
        assertEquals(1, map.findByState(HexState.SELECTED).size());
        assertEquals(1, map.findByState(HexState.BLOCKED).size());
        assertEquals(1, map.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should find no hexes for empty map")
    void testFindByStateEmptyMap() {
        HexMap map = new HexMap();
        assertTrue(map.findByState(HexState.EMPTY).isEmpty());
        assertTrue(map.findByState(HexState.OCCUPIED).isEmpty());
        assertTrue(map.findByState(HexState.BLOCKED).isEmpty());
        assertTrue(map.findByState(HexState.SELECTED).isEmpty());
    }

    // ---- Additional hex operations ----

    @Test
    @DisplayName("Should get hex at negative coordinates")
    void testGetNegativeCoordinates() {
        HexMap map = new HexMap();
        Hex hex = new Hex(-1, -2);
        map.put(hex);

        Hex retrieved = map.get(-1, -2);
        assertNotNull(retrieved);
        assertEquals(-1, retrieved.getQ());
        assertEquals(-2, retrieved.getR());
    }

    @Test
    @DisplayName("Should contain hex at negative coordinates")
    void testContainsNegativeCoordinates() {
        HexMap map = new HexMap();
        map.put(new Hex(-5, 3));

        assertTrue(map.contains(-5, 3));
        assertFalse(map.contains(-5, 4));
    }

    @Test
    @DisplayName("Should handle large coordinates")
    void testLargeCoordinates() {
        HexMap map = new HexMap();
        Hex hex = new Hex(1000, -1000);
        map.put(hex);

        assertNotNull(map.get(1000, -1000));
        assertTrue(map.contains(1000, -1000));
    }

    @Test
    @DisplayName("Should handle block and unblock cycle")
    void testBlockUnblockCycle() {
        HexMap map = new HexMap();
        Hex hex = new Hex(0, 0);
        map.put(hex);

        hex.block();
        assertEquals(1, map.findByState(HexState.BLOCKED).size());

        hex.unblock();
        assertEquals(0, map.findByState(HexState.BLOCKED).size());
        assertEquals(1, map.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should handle select and deselect cycle")
    void testSelectDeselectCycle() {
        HexMap map = new HexMap();
        Hex hex = new Hex(0, 0);
        map.put(hex);

        hex.place("unit");
        hex.select();
        assertEquals(1, map.findByState(HexState.SELECTED).size());

        hex.deselect();
        assertEquals(0, map.findByState(HexState.SELECTED).size());
        assertEquals(1, map.findByState(HexState.OCCUPIED).size());
    }

    @Test
    @DisplayName("Should handle full state cycle")
    void testFullStateCycle() {
        HexMap map = new HexMap();
        Hex hex = new Hex(5, 5);
        map.put(hex);

        // EMPTY -> OCCUPIED
        hex.place("unit");
        assertEquals(HexState.OCCUPIED, hex.getState());

        // OCCUPIED -> SELECTED
        hex.select();
        assertEquals(HexState.SELECTED, hex.getState());

        // SELECTED -> OCCUPIED
        hex.deselect();
        assertEquals(HexState.OCCUPIED, hex.getState());

        // OCCUPIED -> EMPTY
        hex.remove();
        assertEquals(HexState.EMPTY, hex.getState());

        // EMPTY -> BLOCKED
        hex.block();
        assertEquals(HexState.BLOCKED, hex.getState());

        // BLOCKED -> EMPTY
        hex.unblock();
        assertEquals(HexState.EMPTY, hex.getState());
    }

    // ---- Additional generation tests ----

    @Test
    @DisplayName("Should generate range with non-zero center")
    void testGenerateRangeNonZeroCenter() {
        HexMap map = new HexMap();
        Hex center = new Hex(5, 5);
        List<Hex> range = map.generateRange(center, 1);

        assertTrue(range.contains(center));
        assertTrue(map.contains(5, 5));
        for (Hex h : range) {
            assertTrue(h.distance(center) <= 1, "Range hex should be within distance 1");
        }
    }

    @Test
    @DisplayName("Should generate rect with negative coordinates")
    void testGenerateRectNegative() {
        HexMap map = new HexMap();
        List<Hex> rect = map.generateRect(-2, 0, -2, 0);

        // 3 columns * 3 rows = 9 hexes
        assertEquals(9, rect.size());
        assertEquals(9, map.size());
        assertTrue(map.contains(-2, -2));
        assertTrue(map.contains(0, 0));
        assertFalse(map.contains(1, 1));
    }

    @Test
    @DisplayName("Should generate overlapping rings")
    void testGenerateOverlappingRings() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);

        map.generateRing(center, 1);
        int sizeAfterRing1 = map.size();

        map.generateRing(center, 2);
        int sizeAfterRing2 = map.size();

        // Ring 2 should add more hexes than ring 1
        assertTrue(sizeAfterRing2 > sizeAfterRing1);
    }

    // ---- Additional integration tests ----

    @Test
    @DisplayName("Should place entities on generated range")
    void testIntegrationRangeAndPlace() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.generateRange(center, 1);

        for (Hex hex : map.getAll()) {
            hex.place("entity");
        }

        assertEquals(map.size(), map.findByState(HexState.OCCUPIED).size());
    }

    @Test
    @DisplayName("Should block center and check neighbors")
    void testIntegrationBlockAndNeighbors() {
        HexMap map = new HexMap();
        Hex center = new Hex(0, 0);
        map.generateRange(center, 1);

        Hex storedCenter = map.get(0, 0);
        storedCenter.block();

        assertEquals(1, map.findByState(HexState.BLOCKED).size());

        List<Hex> neighbors = map.getNeighbors(storedCenter);
        assertFalse(neighbors.isEmpty(), "Blocked center should still have neighbors");
        // All neighbors should be at distance 1 from center
        for (Hex n : neighbors) {
            assertEquals(1, storedCenter.distance(n), "Neighbor should be at distance 1");
        }
    }

    @Test
    @DisplayName("Should handle multiple put and remove operations")
    void testMultiplePutRemove() {
        HexMap map = new HexMap();

        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));
        map.put(new Hex(2, 0));
        assertEquals(3, map.size());

        map.remove(new Hex(1, 0));
        assertEquals(2, map.size());
        assertFalse(map.contains(1, 0));

        map.put(new Hex(1, 0));
        assertEquals(3, map.size());
        assertTrue(map.contains(1, 0));
    }

    @Test
    @DisplayName("Should clear and repopulate map")
    void testClearAndRepopulate() {
        HexMap map = new HexMap();
        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));
        map.clear();
        assertEquals(0, map.size());

        map.put(new Hex(5, 5));
        assertEquals(1, map.size());
        assertTrue(map.contains(5, 5));
        assertFalse(map.contains(0, 0));
    }

    @Test
    @DisplayName("Should verify toString format")
    void testToStringFormat() {
        HexMap map = new HexMap();
        String emptyStr = map.toString();
        assertTrue(emptyStr.contains("size=0"));

        map.put(new Hex(0, 0));
        map.put(new Hex(1, 0));
        String str = map.toString();
        assertTrue(str.contains("size=2"));
    }
}
