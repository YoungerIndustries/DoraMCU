package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Hex3DMap class.
 */
class Hex3DMapTest {

    // ---- CRUD operations ----

    @Test
    @DisplayName("Should start empty")
    void testEmptyMap() {
        Hex3DMap map = new Hex3DMap();
        assertEquals(0, map.size());
        assertNull(map.get(0, 0));
        assertFalse(map.contains(0, 0));
    }

    @Test
    @DisplayName("Should add and get hex by coordinates")
    void testPutAndGet() {
        Hex3DMap map = new Hex3DMap();
        Hex3D hex = new Hex3D(1, 2, -3);
        map.put(hex);

        Hex3D retrieved = map.get(1, 2);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getX());
        assertEquals(2, retrieved.getY());
        assertEquals(-3, retrieved.getZ());
    }

    @Test
    @DisplayName("Should get hex by Hex3D instance")
    void testGetByHex() {
        Hex3DMap map = new Hex3DMap();
        Hex3D hex = new Hex3D(1, 2, -3);
        map.put(hex);

        Hex3D retrieved = map.get(new Hex3D(1, 2, -3));
        assertNotNull(retrieved);
        assertEquals(hex, retrieved);
    }

    @Test
    @DisplayName("Should return null for missing hex")
    void testGetMissing() {
        Hex3DMap map = new Hex3DMap();
        assertNull(map.get(0, 0));
        assertNull(map.get(new Hex3D(5, 5, -10)));
    }

    @Test
    @DisplayName("Should return null when putting new hex")
    void testPutNewHex() {
        Hex3DMap map = new Hex3DMap();
        Hex3D hex = new Hex3D(3, -1, -2);
        Hex3D previous = map.put(hex);
        assertNull(previous);
    }

    @Test
    @DisplayName("Should return null when removing missing hex")
    void testRemoveMissing() {
        Hex3DMap map = new Hex3DMap();
        assertNull(map.remove(new Hex3D(9, 9, -18)));
    }

    @Test
    @DisplayName("Should return unmodifiable collection from getAll")
    void testGetAllUnmodifiable() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(0, 0, 0));

        assertThrows(UnsupportedOperationException.class, () -> {
            map.getAll().clear();
        });
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("Should remove hex from map")
    void testRemove() {
        Hex3DMap map = new Hex3DMap();
        Hex3D hex = new Hex3D(1, 2, -3);
        map.put(hex);
        assertEquals(1, map.size());

        Hex3D removed = map.remove(hex);
        assertNotNull(removed);
        assertEquals(0, map.size());
    }

    @Test
    @DisplayName("Should check contains")
    void testContains() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(1, 2, -3));

        assertTrue(map.contains(1, 2));
        assertFalse(map.contains(0, 0));
    }

    @Test
    @DisplayName("Should return all hexes")
    void testGetAll() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(1, 0, -1));
        map.put(new Hex3D(0, 1, -1));
        map.put(new Hex3D(-1, 1, 0));

        assertEquals(3, map.getAll().size());
    }

    @Test
    @DisplayName("Should clear all hexes")
    void testClear() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(1, 0, -1));
        map.put(new Hex3D(0, 1, -1));
        map.clear();

        assertEquals(0, map.size());
        assertTrue(map.getAll().isEmpty());
    }

    @Test
    @DisplayName("Should overwrite existing hex")
    void testPutOverwrite() {
        Hex3DMap map = new Hex3DMap();
        Hex3D hex1 = new Hex3D(1, 2, -3);
        Hex3D hex2 = new Hex3D(1, 2, -3);
        hex2.place("unit");

        map.put(hex1);
        Hex3D previous = map.put(hex2);
        assertNotNull(previous);
        assertEquals(1, map.size());
        assertEquals(HexState.OCCUPIED, map.get(1, 2).getState());
    }

    // ---- Query operations ----

    @Test
    @DisplayName("Should return empty list for state with no matches")
    void testFindByStateNoMatches() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(0, 0, 0));
        List<Hex3D> selected = map.findByState(HexState.SELECTED);
        assertTrue(selected.isEmpty());
    }

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        Hex3DMap map = new Hex3DMap();
        Hex3D h1 = new Hex3D(1, 0, -1);
        Hex3D h2 = new Hex3D(0, 1, -1);
        Hex3D h3 = new Hex3D(-1, 1, 0);

        h1.place("unit1");
        h2.place("unit2");
        // h3 remains EMPTY

        map.put(h1);
        map.put(h2);
        map.put(h3);

        List<Hex3D> occupied = map.findByState(HexState.OCCUPIED);
        assertEquals(2, occupied.size());

        List<Hex3D> empty = map.findByState(HexState.EMPTY);
        assertEquals(1, empty.size());
    }

    @Test
    @DisplayName("Should get neighbors present in map")
    void testGetNeighbors() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        Hex3D n1 = new Hex3D(1, -1, 0);
        Hex3D n2 = new Hex3D(1, 0, -1);

        map.put(center);
        map.put(n1);
        map.put(n2);

        List<Hex3D> neighbors = map.getNeighbors(center);
        assertEquals(2, neighbors.size());
    }

    @Test
    @DisplayName("Should return empty neighbors for isolated hex")
    void testGetNeighborsIsolated() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        map.put(center);
        map.put(new Hex3D(10, 10, -20)); // Far away

        List<Hex3D> neighbors = map.getNeighbors(center);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    @DisplayName("Should return empty neighbors for hex not in map")
    void testGetNeighborsNotInMap() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(0, 0, 0));
        map.put(new Hex3D(1, -1, 0));

        // Query neighbors of a hex not in the map
        List<Hex3D> neighbors = map.getNeighbors(new Hex3D(99, 99, -198));
        assertTrue(neighbors.isEmpty());
    }

    // ---- Generation utilities ----

    @Test
    @DisplayName("Should generate sphere of radius 0")
    void testGenerateSphereZero() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> sphere = map.generateSphere(center, 0);

        assertEquals(1, sphere.size());
        assertEquals(center, sphere.get(0));
    }

    @Test
    @DisplayName("Should generate sphere of radius 1")
    void testGenerateSphereOne() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> sphere = map.generateSphere(center, 1);

        assertEquals(6, sphere.size());
        for (Hex3D hex : sphere) {
            assertEquals(1, center.distance(hex));
        }
    }

    @Test
    @DisplayName("Should generate sphere at distance radius")
    void testGenerateSphereDistance() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> sphere = map.generateSphere(center, 2);

        assertTrue(sphere.size() > 0);
        for (Hex3D hex : sphere) {
            assertEquals(2, center.distance(hex),
                    "All hexes in sphere of radius 2 should be at distance 2");
        }
    }

    @Test
    @DisplayName("Should generate range of radius 0 (center only)")
    void testGenerateRangeZero() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> range = map.generateRange(center, 0);

        assertEquals(1, range.size());
        assertEquals(1, map.size());
        assertEquals(center, range.get(0));
    }

    @Test
    @DisplayName("Should generate range including center and all spheres")
    void testGenerateRange() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> range = map.generateRange(center, 1);

        // Center + 6 neighbors = 7
        assertEquals(7, range.size());
        assertTrue(map.contains(0, 0));
    }

    @Test
    @DisplayName("Should generate range with all hexes within radius")
    void testGenerateRangeAllWithinRadius() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        List<Hex3D> range = map.generateRange(center, 2);

        assertTrue(range.size() > 7);
        for (Hex3D hex : range) {
            assertTrue(center.distance(hex) <= 2,
                    "All hexes in range should be within radius 2");
        }
    }

    @Test
    @DisplayName("Should generate cube grid")
    void testGenerateCube() {
        Hex3DMap map = new Hex3DMap();
        List<Hex3D> cube = map.generateCube(-1, 1, -1, 1);

        // 3 x 3 = 9 hexes
        assertEquals(9, cube.size());
        assertEquals(9, map.size());
    }

    @Test
    @DisplayName("Should generate cube with valid coordinates")
    void testGenerateCubeValidCoords() {
        Hex3DMap map = new Hex3DMap();
        List<Hex3D> cube = map.generateCube(-2, 2, -2, 2);

        for (Hex3D hex : cube) {
            assertEquals(0, hex.getX() + hex.getY() + hex.getZ(),
                    "All generated hexes should have valid cubic coordinates");
        }
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should manage hexes with different states in map")
    void testIntegrationMixedStates() {
        Hex3DMap map = new Hex3DMap();

        Hex3D h1 = new Hex3D(0, 0, 0);
        Hex3D h2 = new Hex3D(1, -1, 0);
        Hex3D h3 = new Hex3D(1, 0, -1);
        Hex3D h4 = new Hex3D(0, 1, -1);

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
    @DisplayName("Should generate range and verify bidirectional neighbors")
    void testIntegrationRangeAndNeighbors() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, 1);

        // Center should have neighbors in the generated range
        List<Hex3D> neighbors = map.getNeighbors(center);
        assertFalse(neighbors.isEmpty(), "Center should have neighbors in range-1 map");

        // Each neighbor should have center as neighbor
        for (Hex3D neighbor : neighbors) {
            List<Hex3D> neighborNeighbors = map.getNeighbors(neighbor);
            assertTrue(neighborNeighbors.contains(center),
                    "Neighbor " + neighbor + " should have center as neighbor");
        }
    }

    @Test
    @DisplayName("Should place entities on generated range")
    void testIntegrationRangeAndPlace() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, 1);

        for (Hex3D hex : map.getAll()) {
            hex.place("entity");
        }

        assertEquals(7, map.findByState(HexState.OCCUPIED).size());
    }

    @Test
    @DisplayName("Should block center and check neighbors")
    void testIntegrationBlockAndNeighbors() {
        Hex3DMap map = new Hex3DMap();
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, 1);

        Hex3D storedCenter = map.get(0, 0);
        storedCenter.block();

        assertEquals(1, map.findByState(HexState.BLOCKED).size());
        assertEquals(6, map.findByState(HexState.EMPTY).size());

        List<Hex3D> neighbors = map.getNeighbors(storedCenter);
        assertEquals(6, neighbors.size());
    }

    @Test
    @DisplayName("Should return correct toString")
    void testToString() {
        Hex3DMap map = new Hex3DMap();
        map.put(new Hex3D(0, 0, 0));
        String str = map.toString();
        assertTrue(str.contains("size=1"));
    }
}
