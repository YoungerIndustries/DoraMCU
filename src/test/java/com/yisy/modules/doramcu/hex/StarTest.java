package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Star Tests")
class StarTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create star with default radius")
    void testConstructorDefaultRadius() {
        Star star = new Star(1);
        assertEquals(1, star.getId());
        assertEquals(Star.DEFAULT_RADIUS, star.getRadius());
        assertTrue(star.size() > 1, "Default radius star should have multiple hexes");
    }

    @Test
    @DisplayName("Should create star with custom radius")
    void testConstructorCustomRadius() {
        Star star = new Star(2, 3);
        assertEquals(2, star.getId());
        assertEquals(3, star.getRadius());
        assertTrue(star.size() > 1);
    }

    @Test
    @DisplayName("Should create star with radius 0 (single center hex)")
    void testConstructorRadiusZero() {
        Star star = new Star(3, 0);
        assertEquals(3, star.getId());
        assertEquals(0, star.getRadius());
        assertEquals(1, star.size());
    }

    @Test
    @DisplayName("Should throw on negative radius")
    void testConstructorNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> new Star(4, -1));
    }

    @Test
    @DisplayName("Should allow negative ID")
    void testConstructorNegativeId() {
        Star star = new Star(-1, 2);
        assertEquals(-1, star.getId());
        assertEquals(2, star.getRadius());
    }

    // ---- Accessors ----

    @Test
    @DisplayName("Should return correct size for radius 1")
    void testSizeRadius1() {
        Star star = new Star(10, 1);
        // Center + sphere shell at radius 1
        assertTrue(star.size() > 1);
    }

    @Test
    @DisplayName("Should return correct size for radius 2")
    void testSizeRadius2() {
        Star star = new Star(11, 2);
        assertTrue(star.size() > new Star(12, 1).size(), "Radius 2 should have more hexes than radius 1");
    }

    // ---- Hex operations ----

    @Test
    @DisplayName("Should get center hex")
    void testGetCenter() {
        Star star = new Star(20, 3);
        Hex3D center = star.getCenter();
        assertNotNull(center);
        assertEquals(0, center.getX());
        assertEquals(0, center.getY());
        assertEquals(0, center.getZ());
    }

    @Test
    @DisplayName("Should get hex at valid coordinates")
    void testGetValidCoordinates() {
        Star star = new Star(21, 3);
        Hex3D hex = star.get(1, -1);
        assertNotNull(hex);
        assertEquals(1, hex.getX());
        assertEquals(-1, hex.getY());
        assertEquals(0, hex.getZ());
    }

    @Test
    @DisplayName("Should return null for out-of-bounds coordinates")
    void testGetOutOfBounds() {
        Star star = new Star(22, 2);
        Hex3D hex = star.get(10, -10);
        assertNull(hex);
    }

    @Test
    @DisplayName("Should get hex by Hex3D instance")
    void testGetByHex3D() {
        Star star = new Star(23, 3);
        Hex3D key = new Hex3D(1, 1, -2);
        Hex3D hex = star.get(key);
        assertNotNull(hex);
        assertEquals(1, hex.getX());
        assertEquals(1, hex.getY());
        assertEquals(-2, hex.getZ());
    }

    @Test
    @DisplayName("Should contain valid coordinates")
    void testContainsValid() {
        Star star = new Star(24, 3);
        assertTrue(star.contains(0, 0));
        assertTrue(star.contains(1, -1));
    }

    @Test
    @DisplayName("Should not contain out-of-bounds coordinates")
    void testContainsOutOfBounds() {
        Star star = new Star(25, 2);
        assertFalse(star.contains(10, -10));
    }

    @Test
    @DisplayName("Should contain hex by Hex3D instance")
    void testContainsHex3D() {
        Star star = new Star(26, 3);
        Hex3D hex = new Hex3D(1, -1, 0);
        assertTrue(star.contains(hex));
    }

    @Test
    @DisplayName("Should not contain hex outside star")
    void testContainsHex3DOutside() {
        Star star = new Star(27, 2);
        Hex3D hex = new Hex3D(10, -10, 0);
        assertFalse(star.contains(hex));
    }

    @Test
    @DisplayName("Should return unmodifiable collection")
    void testGetAllUnmodifiable() {
        Star star = new Star(28, 1);
        Collection<Hex3D> all = star.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(new Hex3D(99, 99, -198)));
    }

    @Test
    @DisplayName("Should get all hexes count matches size")
    void testGetAllSize() {
        Star star = new Star(29, 2);
        assertEquals(star.size(), star.getAll().size());
    }

    // ---- Query operations ----

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        Star star = new Star(30, 1);
        Hex3D center = star.getCenter();
        center.place("sun");

        assertEquals(1, star.findByState(HexState.OCCUPIED).size());
        assertEquals(star.size() - 1, star.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should find no hexes for empty map state")
    void testFindByStateNoMatches() {
        Star star = new Star(31, 1);
        assertTrue(star.findByState(HexState.BLOCKED).isEmpty());
        assertTrue(star.findByState(HexState.SELECTED).isEmpty());
    }

    @Test
    @DisplayName("Should find hexes by all states")
    void testFindByStateAllStates() {
        Star star = new Star(32, 1);
        Hex3D center = star.getCenter();
        center.place("core");
        center.select();

        // Get a neighbor to block
        List<Hex3D> neighbors = star.getNeighbors(center);
        assertFalse(neighbors.isEmpty());
        neighbors.get(0).block();

        assertEquals(0, star.findByState(HexState.OCCUPIED).size());
        assertEquals(1, star.findByState(HexState.SELECTED).size());
        assertEquals(1, star.findByState(HexState.BLOCKED).size());
    }

    @Test
    @DisplayName("Should get neighbors for center hex")
    void testGetNeighborsCenter() {
        Star star = new Star(33, 3);
        Hex3D center = star.getCenter();
        List<Hex3D> neighbors = star.getNeighbors(center);
        // Center should have all 6 neighbors in a star with radius >= 1
        assertEquals(6, neighbors.size());
        for (Hex3D n : neighbors) {
            assertEquals(1, center.distance(n), "Neighbor should be at distance 1");
        }
    }

    @Test
    @DisplayName("Should get fewer neighbors for surface hex")
    void testGetNeighborsSurface() {
        Star star = new Star(34, 2);
        List<Hex3D> surface = star.getSurface();
        assertFalse(surface.isEmpty());
        Hex3D surfaceHex = surface.get(0);
        List<Hex3D> neighbors = star.getNeighbors(surfaceHex);
        // Surface hex should have fewer than 6 neighbors (some are outside the star)
        assertTrue(neighbors.size() <= 6);
    }

    @Test
    @DisplayName("Should get empty neighbors for hex not in star")
    void testGetNeighborsMissing() {
        Star star = new Star(35, 2);
        Hex3D outside = new Hex3D(10, -10, 0);
        List<Hex3D> neighbors = star.getNeighbors(outside);
        assertTrue(neighbors.isEmpty());
    }

    // ---- Occupancy statistics ----

    @Test
    @DisplayName("Should return correct occupied count")
    void testGetOccupiedCount() {
        Star star = new Star(36, 1);
        Hex3D center = star.getCenter();
        center.place("sun");

        assertEquals(1, star.getOccupiedCount());
    }

    @Test
    @DisplayName("Should return correct empty count")
    void testGetEmptyCount() {
        Star star = new Star(37, 1);
        Hex3D center = star.getCenter();
        center.place("sun");

        assertEquals(star.size() - 1, star.getEmptyCount());
    }

    @Test
    @DisplayName("Should return correct blocked count")
    void testGetBlockedCount() {
        Star star = new Star(38, 1);
        Hex3D center = star.getCenter();
        center.block();

        assertEquals(1, star.getBlockedCount());
    }

    @Test
    @DisplayName("Should return correct selected count")
    void testGetSelectedCount() {
        Star star = new Star(39, 1);
        Hex3D center = star.getCenter();
        center.place("sun");
        center.select();

        assertEquals(1, star.getSelectedCount());
    }

    @Test
    @DisplayName("Should return correct occupancy ratio")
    void testGetOccupancyRatio() {
        Star star = new Star(40, 1);
        assertEquals(0.0, star.getOccupancyRatio(), 0.001);

        Hex3D center = star.getCenter();
        center.place("sun");

        double ratio = star.getOccupancyRatio();
        assertTrue(ratio > 0.0, "Occupancy ratio should be > 0 after placing");
        assertTrue(ratio <= 1.0, "Occupancy ratio should be <= 1");
    }

    @Test
    @DisplayName("Should return 0 occupancy ratio for radius 0 star with empty hex")
    void testOccupancyRatioRadiusZero() {
        Star star = new Star(41, 0);
        assertEquals(0.0, star.getOccupancyRatio(), 0.001);
    }

    // ---- Coordinate conversion ----

    @Test
    @DisplayName("Should convert local to global coordinates")
    void testToGlobal() {
        Star star = new Star(42, 3);
        int[] global = star.toGlobal(1, -1);
        assertEquals(1, global[0]);
        assertEquals(-1, global[1]);
        assertEquals(0, global[2]);
    }

    @Test
    @DisplayName("Should convert center coordinates")
    void testToGlobalCenter() {
        Star star = new Star(43, 3);
        int[] global = star.toGlobal(0, 0);
        assertEquals(0, global[0]);
        assertEquals(0, global[1]);
        assertEquals(0, global[2]);
    }

    @Test
    @DisplayName("Should check in bounds for valid coordinates")
    void testIsInBoundsValid() {
        Star star = new Star(44, 3);
        assertTrue(star.isInBounds(0, 0, 0));
        assertTrue(star.isInBounds(1, -1, 0));
        assertTrue(star.isInBounds(3, -3, 0));
    }

    @Test
    @DisplayName("Should check not in bounds for out-of-range coordinates")
    void testIsInBoundsOutOfRange() {
        Star star = new Star(45, 2);
        assertFalse(star.isInBounds(5, -5, 0));
    }

    @Test
    @DisplayName("Should return false for invalid cubic coordinates")
    void testIsInBoundsInvalidCubic() {
        Star star = new Star(46, 3);
        assertFalse(star.isInBounds(1, 1, 1));
    }

    // ---- Surface and Core ----

    @Test
    @DisplayName("Should get surface hexes")
    void testGetSurface() {
        Star star = new Star(47, 2);
        List<Hex3D> surface = star.getSurface();
        assertFalse(surface.isEmpty());
        Hex3D center = new Hex3D(0, 0, 0);
        for (Hex3D h : surface) {
            assertEquals(2, center.distance(h), "Surface hex should be at radius distance");
        }
    }

    @Test
    @DisplayName("Should get empty surface for radius 0")
    void testGetSurfaceRadiusZero() {
        Star star = new Star(48, 0);
        List<Hex3D> surface = star.getSurface();
        // Radius 0: center is at distance 0, which equals radius 0
        assertEquals(1, surface.size());
        assertEquals(0, surface.get(0).getX());
    }

    @Test
    @DisplayName("Should get core hexes")
    void testGetCore() {
        Star star = new Star(49, 4);
        List<Hex3D> core = star.getCore();
        assertFalse(core.isEmpty());
        Hex3D center = new Hex3D(0, 0, 0);
        int coreRadius = 4 / 2; // 2
        for (Hex3D h : core) {
            assertTrue(center.distance(h) <= coreRadius, "Core hex should be within half radius");
        }
    }

    @Test
    @DisplayName("Should get single core for radius 1")
    void testGetCoreRadius1() {
        Star star = new Star(50, 1);
        List<Hex3D> core = star.getCore();
        // Core radius = 1/2 = 0, so only center
        assertEquals(1, core.size());
        assertEquals(0, core.get(0).getX());
    }

    @Test
    @DisplayName("Should have surface plus interior equal total size")
    void testSurfaceAndInterior() {
        Star star = new Star(51, 3);
        List<Hex3D> surface = star.getSurface();
        Hex3D center = new Hex3D(0, 0, 0);
        long interiorCount = star.getAll().stream()
                .filter(h -> center.distance(h) < star.getRadius())
                .count();
        assertEquals(star.size(), surface.size() + interiorCount);
    }

    // ---- Object contract ----

    @Test
    @DisplayName("Should equal same ID")
    void testEqualsSameId() {
        Star s1 = new Star(100, 2);
        Star s2 = new Star(100, 3);
        assertEquals(s1, s2);
    }

    @Test
    @DisplayName("Should not equal different ID")
    void testEqualsDifferentId() {
        Star s1 = new Star(101, 2);
        Star s2 = new Star(102, 2);
        assertNotEquals(s1, s2);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        Star s1 = new Star(103, 2);
        Star s2 = new Star(103, 3);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    @DisplayName("Should work as HashMap key")
    void testHashMapKey() {
        java.util.Map<Star, String> map = new java.util.HashMap<>();
        Star s1 = new Star(104, 2);
        map.put(s1, "Alpha");
        assertEquals("Alpha", map.get(new Star(104, 5)));
    }

    @Test
    @DisplayName("Should return correct toString")
    void testToString() {
        Star star = new Star(105, 2);
        String str = star.toString();
        assertTrue(str.contains("id=105"));
        assertTrue(str.contains("radius=2"));
        assertTrue(str.contains("type=YELLOW"));
        assertTrue(str.contains("energy=10"));
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should place entities on all hexes")
    void testIntegrationPlaceAll() {
        Star star = new Star(110, 1);
        for (Hex3D hex : star.getAll()) {
            hex.place("matter");
        }
        assertEquals(star.size(), star.getOccupiedCount());
        assertEquals(1.0, star.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should handle mixed states")
    void testIntegrationMixedStates() {
        Star star = new Star(111, 2);
        Hex3D center = star.getCenter();
        center.place("core");
        center.select();

        List<Hex3D> neighbors = star.getNeighbors(center);
        assertFalse(neighbors.isEmpty());
        neighbors.get(0).block();

        assertEquals(0, star.findByState(HexState.OCCUPIED).size());
        assertEquals(1, star.findByState(HexState.SELECTED).size());
        assertEquals(1, star.findByState(HexState.BLOCKED).size());
        assertTrue(star.findByState(HexState.EMPTY).size() > 0);
    }

    @Test
    @DisplayName("Should handle place and remove cycle")
    void testIntegrationPlaceRemoveCycle() {
        Star star = new Star(112, 1);
        Hex3D center = star.getCenter();

        center.place("sun");
        assertEquals(1, star.getOccupiedCount());

        center.remove();
        assertEquals(0, star.getOccupiedCount());
        assertEquals(star.size(), star.getEmptyCount());
    }

    @Test
    @DisplayName("Should handle block and unblock cycle")
    void testIntegrationBlockUnblockCycle() {
        Star star = new Star(113, 1);
        Hex3D center = star.getCenter();

        center.block();
        assertEquals(1, star.getBlockedCount());

        center.unblock();
        assertEquals(0, star.getBlockedCount());
    }

    @Test
    @DisplayName("Should handle select and deselect cycle")
    void testIntegrationSelectDeselectCycle() {
        Star star = new Star(114, 1);
        Hex3D center = star.getCenter();

        center.place("sun");
        center.select();
        assertEquals(1, star.getSelectedCount());

        center.deselect();
        assertEquals(0, star.getSelectedCount());
        assertEquals(1, star.getOccupiedCount());
    }

    @Test
    @DisplayName("Should create multiple independent stars")
    void testMultipleStars() {
        Star s1 = new Star(200, 2);
        Star s2 = new Star(201, 3);

        assertNotEquals(s1, s2);
        assertTrue(s2.size() > s1.size(), "Larger radius star should have more hexes");

        // Modify s1 should not affect s2
        s1.getCenter().place("sun1");
        assertEquals(0, s2.getOccupiedCount());
    }

    @Test
    @DisplayName("Should verify all hexes are within radius")
    void testAllHexesWithinRadius() {
        Star star = new Star(202, 3);
        Hex3D center = new Hex3D(0, 0, 0);
        for (Hex3D hex : star.getAll()) {
            assertTrue(center.distance(hex) <= star.getRadius(),
                    "Hex " + hex + " should be within radius " + star.getRadius());
        }
    }

    @Test
    @DisplayName("Should verify neighbors are distinct")
    void testNeighborsAreDistinct() {
        Star star = new Star(203, 3);
        Hex3D center = star.getCenter();
        List<Hex3D> neighbors = star.getNeighbors(center);
        long uniqueCount = neighbors.stream().distinct().count();
        assertEquals(neighbors.size(), uniqueCount);
    }

    @Test
    @DisplayName("Should verify neighbors exclude self")
    void testNeighborsExcludeSelf() {
        Star star = new Star(204, 3);
        Hex3D center = star.getCenter();
        List<Hex3D> neighbors = star.getNeighbors(center);
        assertFalse(neighbors.contains(center));
    }

    @Test
    @DisplayName("Should verify constant DEFAULT_RADIUS")
    void testDefaultRadiusConstant() {
        assertEquals(5, Star.DEFAULT_RADIUS);
    }

    // ---- Additional Hex operations ----

    @Test
    @DisplayName("Should get hex at negative coordinates")
    void testGetNegativeCoordinates() {
        Star star = new Star(300, 3);
        Hex3D hex = star.get(-1, 1);
        assertNotNull(hex);
        assertEquals(-1, hex.getX());
        assertEquals(1, hex.getY());
        assertEquals(0, hex.getZ());
    }

    @Test
    @DisplayName("Should contain negative coordinates within star")
    void testContainsNegativeCoordinates() {
        Star star = new Star(301, 3);
        assertTrue(star.contains(-1, 1));
        assertTrue(star.contains(-2, 2));
        assertFalse(star.contains(-10, 10));
    }

    @Test
    @DisplayName("Should get hex at boundary coordinates")
    void testGetBoundaryCoordinates() {
        Star star = new Star(302, 2);
        // At radius 2, (2, -2, 0) should be in bounds
        Hex3D hex = star.get(2, -2);
        assertNotNull(hex);
    }

    @Test
    @DisplayName("Should return null just outside boundary")
    void testGetJustOutsideBoundary() {
        Star star = new Star(303, 2);
        // At radius 2, (3, -3, 0) is at distance 3, outside
        Hex3D hex = star.get(3, -3);
        assertNull(hex);
    }

    @Test
    @DisplayName("Should handle large coordinates")
    void testGetLargeCoordinates() {
        Star star = new Star(304, 5);
        Hex3D hex = star.get(100, -100);
        assertNull(hex);
    }

    @Test
    @DisplayName("Should handle block and unblock cycle")
    void testBlockUnblockCycle() {
        Star star = new Star(305, 1);
        Hex3D center = star.getCenter();

        center.block();
        assertEquals(1, star.getBlockedCount());
        assertEquals(0, star.getOccupiedCount());

        center.unblock();
        assertEquals(0, star.getBlockedCount());
        assertEquals(star.size(), star.getEmptyCount());
    }

    @Test
    @DisplayName("Should handle select and deselect cycle")
    void testSelectDeselectCycle() {
        Star star = new Star(306, 1);
        Hex3D center = star.getCenter();

        center.place("sun");
        center.select();
        assertEquals(1, star.getSelectedCount());
        assertEquals(0, star.getOccupiedCount());

        center.deselect();
        assertEquals(0, star.getSelectedCount());
        assertEquals(1, star.getOccupiedCount());
    }

    @Test
    @DisplayName("Should handle full state cycle")
    void testFullStateCycle() {
        Star star = new Star(307, 1);
        Hex3D center = star.getCenter();

        // EMPTY -> OCCUPIED
        center.place("entity");
        assertEquals(1, star.getOccupiedCount());

        // OCCUPIED -> SELECTED
        center.select();
        assertEquals(1, star.getSelectedCount());
        assertEquals(0, star.getOccupiedCount());

        // SELECTED -> OCCUPIED
        center.deselect();
        assertEquals(1, star.getOccupiedCount());
        assertEquals(0, star.getSelectedCount());

        // OCCUPIED -> EMPTY
        center.remove();
        assertEquals(0, star.getOccupiedCount());
        assertEquals(star.size(), star.getEmptyCount());

        // EMPTY -> BLOCKED
        center.block();
        assertEquals(1, star.getBlockedCount());

        // BLOCKED -> EMPTY
        center.unblock();
        assertEquals(0, star.getBlockedCount());
        assertEquals(star.size(), star.getEmptyCount());
    }

    // ---- Additional Neighbor operations ----

    @Test
    @DisplayName("Should get neighbors for surface hex with fewer than 6")
    void testGetNeighborsSurfaceFewer() {
        Star star = new Star(308, 2);
        List<Hex3D> surface = star.getSurface();
        assertFalse(surface.isEmpty());
        Hex3D surfaceHex = surface.get(0);
        List<Hex3D> neighbors = star.getNeighbors(surfaceHex);
        // Surface hex should have fewer than 6 neighbors inside the star
        assertTrue(neighbors.size() < 6, "Surface hex should have fewer than 6 neighbors");
    }

    @Test
    @DisplayName("Should get neighbors for hex at edge of radius 1 star")
    void testGetNeighborsEdgeRadius1() {
        Star star = new Star(309, 1);
        // All non-center hexes in radius 1 are surface hexes
        List<Hex3D> surface = star.getSurface();
        assertFalse(surface.isEmpty());
        Hex3D edgeHex = surface.get(0);
        List<Hex3D> neighbors = star.getNeighbors(edgeHex);
        // Should have at least 1 neighbor (the center)
        assertTrue(neighbors.size() >= 1, "Edge hex should have at least the center as neighbor");
        assertTrue(neighbors.size() <= 6);
    }

    @Test
    @DisplayName("Should get empty neighbors for isolated hex not in star")
    void testGetNeighborsExternalHex() {
        Star star = new Star(310, 2);
        Hex3D external = new Hex3D(100, -100, 0);
        List<Hex3D> neighbors = star.getNeighbors(external);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    @DisplayName("Should get neighbors for single-hex star (radius 0)")
    void testGetNeighborsRadiusZero() {
        Star star = new Star(311, 0);
        Hex3D center = star.getCenter();
        List<Hex3D> neighbors = star.getNeighbors(center);
        assertTrue(neighbors.isEmpty(), "Radius 0 star has no neighbors");
    }

    @Test
    @DisplayName("Should verify neighbor distance is always 1")
    void testNeighborDistanceAlwaysOne() {
        Star star = new Star(312, 3);
        for (Hex3D hex : star.getAll()) {
            List<Hex3D> neighbors = star.getNeighbors(hex);
            for (Hex3D neighbor : neighbors) {
                assertEquals(1, hex.distance(neighbor),
                        "Neighbor of " + hex + " should be at distance 1");
            }
        }
    }

    @Test
    @DisplayName("Should verify bidirectional neighbors")
    void testBidirectionalNeighbors() {
        Star star = new Star(313, 2);
        Hex3D center = star.getCenter();
        List<Hex3D> neighbors = star.getNeighbors(center);
        for (Hex3D neighbor : neighbors) {
            List<Hex3D> reverseNeighbors = star.getNeighbors(neighbor);
            assertTrue(reverseNeighbors.contains(center),
                    "Neighbor " + neighbor + " should have center as neighbor");
        }
    }

    // ---- Additional Occupancy statistics ----

    @Test
    @DisplayName("Should return 0 counts for fresh star")
    void testInitialCounts() {
        Star star = new Star(314, 2);
        assertEquals(0, star.getOccupiedCount());
        assertEquals(star.size(), star.getEmptyCount());
        assertEquals(0, star.getBlockedCount());
        assertEquals(0, star.getSelectedCount());
    }

    @Test
    @DisplayName("Should return correct occupancy ratio after multiple operations")
    void testOccupancyRatioMultipleOperations() {
        Star star = new Star(315, 2);
        int initialSize = star.size();

        // Place on center
        star.getCenter().place("core");
        double ratio1 = star.getOccupancyRatio();
        assertEquals(1.0 / initialSize, ratio1, 0.001);

        // Place on a neighbor
        List<Hex3D> neighbors = star.getNeighbors(star.getCenter());
        neighbors.get(0).place("satellite");
        double ratio2 = star.getOccupancyRatio();
        assertEquals(2.0 / initialSize, ratio2, 0.001);
    }

    @Test
    @DisplayName("Should return 1.0 occupancy ratio when all occupied")
    void testOccupancyRatioFull() {
        Star star = new Star(316, 1);
        for (Hex3D hex : star.getAll()) {
            hex.place("matter");
        }
        assertEquals(1.0, star.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should count blocked hexes separately from occupied")
    void testBlockedExcludedFromOccupied() {
        Star star = new Star(317, 1);
        Hex3D center = star.getCenter();
        center.block();

        assertEquals(0, star.getOccupiedCount());
        assertEquals(1, star.getBlockedCount());
    }

    @Test
    @DisplayName("Should count selected hexes separately from occupied")
    void testSelectedExcludedFromOccupied() {
        Star star = new Star(318, 1);
        Hex3D center = star.getCenter();
        center.place("sun");
        center.select();

        assertEquals(0, star.getOccupiedCount());
        assertEquals(1, star.getSelectedCount());
    }

    @Test
    @DisplayName("Should handle occupancy ratio with mixed states")
    void testOccupancyRatioMixedStates() {
        Star star = new Star(319, 2);
        Hex3D center = star.getCenter();
        center.place("core");
        center.select(); // SELECTED, not OCCUPIED

        List<Hex3D> neighbors = star.getNeighbors(center);
        neighbors.get(0).block(); // BLOCKED

        // Only place another hex to get OCCUPIED count
        neighbors.get(1).place("satellite");

        assertEquals(1, star.getOccupiedCount());
        assertEquals(1, star.getSelectedCount());
        assertEquals(1, star.getBlockedCount());
        double ratio = star.getOccupancyRatio();
        assertTrue(ratio > 0.0);
        assertTrue(ratio < 1.0);
    }

    // ---- Additional Surface and Core tests ----

    @Test
    @DisplayName("Should have core subset of all hexes")
    void testCoreIsSubset() {
        Star star = new Star(320, 4);
        List<Hex3D> core = star.getCore();
        for (Hex3D h : core) {
            assertTrue(star.getAll().contains(h), "Core hex should be in getAll()");
        }
    }

    @Test
    @DisplayName("Should have surface subset of all hexes")
    void testSurfaceIsSubset() {
        Star star = new Star(321, 3);
        List<Hex3D> surface = star.getSurface();
        for (Hex3D h : surface) {
            assertTrue(star.getAll().contains(h), "Surface hex should be in getAll()");
        }
    }

    @Test
    @DisplayName("Should have no overlap between core and surface for large radius")
    void testCoreAndSurfaceNoOverlap() {
        Star star = new Star(322, 6);
        List<Hex3D> core = star.getCore();
        List<Hex3D> surface = star.getSurface();
        for (Hex3D c : core) {
            assertFalse(surface.contains(c), "Core and surface should not overlap");
        }
    }

    @Test
    @DisplayName("Should have core equal to surface for radius 0")
    void testCoreEqualsSurfaceRadiusZero() {
        Star star = new Star(323, 0);
        List<Hex3D> core = star.getCore();
        List<Hex3D> surface = star.getSurface();
        assertEquals(core, surface);
    }

    @Test
    @DisplayName("Should have core size less than total size for radius 2")
    void testCoreSizeLessThanTotal() {
        Star star = new Star(324, 2);
        List<Hex3D> core = star.getCore();
        assertTrue(core.size() < star.size(), "Core should be smaller than total");
    }

    @Test
    @DisplayName("Should have surface size greater than 0 for radius 1")
    void testSurfaceSizeRadius1() {
        Star star = new Star(325, 1);
        List<Hex3D> surface = star.getSurface();
        assertEquals(6, surface.size(), "Radius 1 surface should have 6 hexes");
    }

    // ---- Additional Coordinate conversion tests ----

    @Test
    @DisplayName("Should convert negative local coordinates to global")
    void testToGlobalNegative() {
        Star star = new Star(326, 3);
        int[] global = star.toGlobal(-2, 1);
        assertEquals(-2, global[0]);
        assertEquals(1, global[1]);
        assertEquals(1, global[2]);
    }

    @Test
    @DisplayName("Should check in bounds for boundary coordinates")
    void testIsInBoundsBoundary() {
        Star star = new Star(327, 3);
        // Exactly at radius 3
        assertTrue(star.isInBounds(3, -3, 0));
        assertTrue(star.isInBounds(3, 0, -3));
        assertTrue(star.isInBounds(0, 3, -3));
    }

    @Test
    @DisplayName("Should check not in bounds just outside radius")
    void testIsInBoundsJustOutside() {
        Star star = new Star(328, 2);
        assertFalse(star.isInBounds(3, -3, 0));
    }

    @Test
    @DisplayName("Should return false for invalid cubic sum")
    void testIsInBoundsInvalidSum() {
        Star star = new Star(329, 5);
        assertFalse(star.isInBounds(1, 1, 1)); // sum = 3
        assertFalse(star.isInBounds(0, 0, 1)); // sum = 1
    }

    // ---- Additional Integration scenarios ----

    @Test
    @DisplayName("Should place entities on generated surface")
    void testIntegrationPlaceOnSurface() {
        Star star = new Star(330, 2);
        List<Hex3D> surface = star.getSurface();
        for (Hex3D hex : surface) {
            hex.place("shell");
        }
        assertEquals(surface.size(), star.getOccupiedCount());
    }

    @Test
    @DisplayName("Should place entities on generated core")
    void testIntegrationPlaceOnCore() {
        Star star = new Star(331, 4);
        List<Hex3D> core = star.getCore();
        for (Hex3D hex : core) {
            hex.place("core-matter");
        }
        assertEquals(core.size(), star.getOccupiedCount());
    }

    @Test
    @DisplayName("Should verify all hexes have valid cubic coordinates")
    void testAllHexesValidCubic() {
        Star star = new Star(332, 3);
        for (Hex3D hex : star.getAll()) {
            assertEquals(0, hex.getX() + hex.getY() + hex.getZ(),
                    "Hex " + hex + " should have valid cubic coordinates");
        }
    }

    @Test
    @DisplayName("Should handle multiple independent stars with different radii")
    void testMultipleStarsDifferentRadii() {
        Star s1 = new Star(400, 1);
        Star s2 = new Star(401, 2);
        Star s3 = new Star(402, 3);

        assertTrue(s2.size() > s1.size());
        assertTrue(s3.size() > s2.size());

        // Modify each independently
        s1.getCenter().place("star1");
        s2.getCenter().block();
        s3.getCenter().place("star3");
        s3.getCenter().select();

        assertEquals(1, s1.getOccupiedCount());
        assertEquals(1, s2.getBlockedCount());
        assertEquals(1, s3.getSelectedCount());
    }

    @Test
    @DisplayName("Should handle full state cycle across multiple hexes")
    void testIntegrationFullCycleMultipleHexes() {
        Star star = new Star(403, 1);
        Hex3D center = star.getCenter();
        List<Hex3D> neighbors = star.getNeighbors(center);

        // Place on center
        center.place("core");
        assertEquals(1, star.getOccupiedCount());

        // Block a neighbor
        neighbors.get(0).block();
        assertEquals(1, star.getBlockedCount());

        // Place and select another neighbor
        neighbors.get(1).place("satellite");
        neighbors.get(1).select();
        assertEquals(1, star.getSelectedCount());

        // Remove center
        center.remove();
        assertEquals(0, star.getOccupiedCount());
        assertEquals(star.size() - 2, star.getEmptyCount()); // 2 non-empty: blocked + selected

        // Unblock and deselect
        neighbors.get(0).unblock();
        neighbors.get(1).deselect();
        assertEquals(star.size() - 1, star.getEmptyCount()); // 1 occupied (deselected -> occupied)
    }

    @Test
    @DisplayName("Should verify toString format")
    void testToStringFormat() {
        Star star = new Star(404, 3);
        String str = star.toString();
        assertTrue(str.startsWith("Star{"));
        assertTrue(str.contains("id=404"));
        assertTrue(str.contains("radius=3"));
        assertTrue(str.contains("type=YELLOW"));
        assertTrue(str.contains("energy=10"));
        assertTrue(str.contains("stations=0/2"));
        assertTrue(str.endsWith("}"));
    }

    @Test
    @DisplayName("Should handle radius 1 star neighbors for all hexes")
    void testNeighborsRadius1AllHexes() {
        Star star = new Star(405, 1);
        // Center has 6 neighbors, surface hexes have fewer
        Hex3D center = star.getCenter();
        List<Hex3D> centerNeighbors = star.getNeighbors(center);
        assertEquals(6, centerNeighbors.size());

        // Each surface hex should have at least 1 neighbor (the center)
        List<Hex3D> surface = star.getSurface();
        for (Hex3D surfaceHex : surface) {
            List<Hex3D> neighbors = star.getNeighbors(surfaceHex);
            assertTrue(neighbors.size() >= 1, "Surface hex should have at least center as neighbor");
            assertTrue(neighbors.contains(center), "Surface hex should have center as neighbor");
        }
    }

    @Test
    @DisplayName("Should find hexes by state EMPTY after all operations")
    void testFindByStateEmptyAfterOperations() {
        Star star = new Star(406, 1);
        Hex3D center = star.getCenter();
        center.place("sun");
        center.remove();

        // All should be empty again
        assertEquals(star.size(), star.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should handle get by Hex3D for missing hex")
    void testGetByHex3DMissing() {
        Star star = new Star(407, 2);
        Hex3D missing = new Hex3D(100, -100, 0);
        assertNull(star.get(missing));
    }

    @Test
    @DisplayName("Should verify equals reflexivity")
    void testEqualsReflexive() {
        Star star = new Star(408, 2);
        assertEquals(star, star);
    }

    @Test
    @DisplayName("Should verify equals symmetry")
    void testEqualsSymmetric() {
        Star s1 = new Star(409, 2);
        Star s2 = new Star(409, 3);
        assertEquals(s1, s2);
        assertEquals(s2, s1);
    }

    @Test
    @DisplayName("Should verify equals transitivity")
    void testEqualsTransitive() {
        Star s1 = new Star(410, 1);
        Star s2 = new Star(410, 2);
        Star s3 = new Star(410, 3);
        assertEquals(s1, s2);
        assertEquals(s2, s3);
        assertEquals(s1, s3);
    }

    @Test
    @DisplayName("Should not equal null")
    void testEqualsNull() {
        Star star = new Star(411, 2);
        assertNotEquals(null, star);
    }

    @Test
    @DisplayName("Should not equal different class")
    void testEqualsDifferentClass() {
        Star star = new Star(412, 2);
        assertNotEquals("Star", star);
        assertNotEquals(412, star);
    }

    // ---- StarType enum tests ----

    @Test
    @DisplayName("Should have 4 star types")
    void testStarTypeCount() {
        assertEquals(4, Star.StarType.values().length);
    }

    @Test
    @DisplayName("RED star should have Durantium")
    void testRedStarStrategicResource() {
        assertEquals(StrategicResource.DURANTIUM, Star.StarType.RED.getStrategicResource());
    }

    @Test
    @DisplayName("BLUE star should have Thulium")
    void testBlueStarStrategicResource() {
        assertEquals(StrategicResource.THULIUM, Star.StarType.BLUE.getStrategicResource());
    }

    @Test
    @DisplayName("PURPLE star should have Promethion")
    void testPurpleStarStrategicResource() {
        assertEquals(StrategicResource.PROMETHION, Star.StarType.PURPLE.getStrategicResource());
    }

    @Test
    @DisplayName("YELLOW star should have no strategic resource")
    void testYellowStarStrategicResource() {
        assertNull(Star.StarType.YELLOW.getStrategicResource());
    }

    @Test
    @DisplayName("RED star should have lower energy yield")
    void testRedStarEnergyYield() {
        assertEquals(8, Star.StarType.RED.getBaseEnergyYield());
    }

    @Test
    @DisplayName("BLUE star should have highest energy yield")
    void testBlueStarEnergyYield() {
        assertEquals(15, Star.StarType.BLUE.getBaseEnergyYield());
    }

    @Test
    @DisplayName("YELLOW star should have balanced energy yield")
    void testYellowStarEnergyYield() {
        assertEquals(10, Star.StarType.YELLOW.getBaseEnergyYield());
    }

    @Test
    @DisplayName("PURPLE star should have moderate energy yield")
    void testPurpleStarEnergyYield() {
        assertEquals(12, Star.StarType.PURPLE.getBaseEnergyYield());
    }

    @Test
    @DisplayName("RED star should have lowest radiation")
    void testRedStarRadiation() {
        assertEquals(0.05, Star.StarType.RED.getRadiationDamage(), 0.001);
    }

    @Test
    @DisplayName("BLUE star should have highest radiation")
    void testBlueStarRadiation() {
        assertEquals(0.2, Star.StarType.BLUE.getRadiationDamage(), 0.001);
    }

    @Test
    @DisplayName("RED star should have lowest gravity well")
    void testRedStarGravityWell() {
        assertEquals(0.2, Star.StarType.RED.getGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("BLUE star should have highest gravity well")
    void testBlueStarGravityWell() {
        assertEquals(0.4, Star.StarType.BLUE.getGravityWellBonus(), 0.001);
    }

    // ---- Constructor with StarType tests ----

    @Test
    @DisplayName("Should create star with StarType")
    void testConstructorWithStarType() {
        Star star = new Star(500, Star.StarType.RED);
        assertEquals(500, star.getId());
        assertEquals(Star.StarType.RED, star.getStarType());
        assertEquals(Star.DEFAULT_RADIUS, star.getRadius());
    }

    @Test
    @DisplayName("Should create star with StarType and custom radius")
    void testConstructorWithStarTypeAndRadius() {
        Star star = new Star(501, 3, Star.StarType.BLUE);
        assertEquals(501, star.getId());
        assertEquals(Star.StarType.BLUE, star.getStarType());
        assertEquals(3, star.getRadius());
    }

    @Test
    @DisplayName("Should throw on null StarType")
    void testConstructorNullStarType() {
        assertThrows(IllegalArgumentException.class, () -> new Star(502, 3, null));
    }

    @Test
    @DisplayName("Default constructor should create YELLOW star")
    void testDefaultConstructorStarType() {
        Star star = new Star(503);
        assertEquals(Star.StarType.YELLOW, star.getStarType());
    }

    @Test
    @DisplayName("Two-arg constructor should create YELLOW star")
    void testTwoArgConstructorStarType() {
        Star star = new Star(504, 3);
        assertEquals(Star.StarType.YELLOW, star.getStarType());
    }

    // ---- Name tests ----

    @Test
    @DisplayName("Should have default name")
    void testDefaultName() {
        Star star = new Star(505);
        assertEquals("Star-505", star.getName());
    }

    @Test
    @DisplayName("Should set custom name")
    void testSetName() {
        Star star = new Star(506);
        star.setName("Alpha Centauri");
        assertEquals("Alpha Centauri", star.getName());
    }

    // ---- Energy yield tests ----

    @Test
    @DisplayName("Should return energy yield from star type")
    void testEnergyYieldFromType() {
        Star star = new Star(507, Star.StarType.BLUE);
        assertEquals(15, star.getEnergyYield());
    }

    @Test
    @DisplayName("Should set custom energy yield")
    void testSetEnergyYield() {
        Star star = new Star(508);
        star.setEnergyYield(20);
        assertEquals(20, star.getEnergyYield());
    }

    @Test
    @DisplayName("Should throw on negative energy yield")
    void testSetNegativeEnergyYield() {
        Star star = new Star(509);
        assertThrows(IllegalArgumentException.class, () -> star.setEnergyYield(-1));
    }

    @Test
    @DisplayName("Should calculate effective energy yield with no stations")
    void testEffectiveEnergyYieldNoStations() {
        Star star = new Star(510);
        assertEquals(10, star.getEffectiveEnergyYield());
    }

    @Test
    @DisplayName("Should calculate effective energy yield with one station")
    void testEffectiveEnergyYieldOneStation() {
        Star star = new Star(511);
        star.addStation("Solar Collector");
        // 10 * 1.15 = 11.5, rounded to 12
        assertEquals(12, star.getEffectiveEnergyYield());
    }

    @Test
    @DisplayName("Should calculate effective energy yield with multiple stations")
    void testEffectiveEnergyYieldMultipleStations() {
        Star star = new Star(512);
        star.addStation("Collector-1");
        star.addStation("Collector-2");
        // 10 * 1.30 = 13
        assertEquals(13, star.getEffectiveEnergyYield());
    }

    // ---- Station management tests ----

    @Test
    @DisplayName("Should have default station slots")
    void testDefaultStationSlots() {
        Star star = new Star(513);
        assertEquals(Star.DEFAULT_STATION_SLOTS, star.getStationSlots());
    }

    @Test
    @DisplayName("Should set custom station slots")
    void testSetStationSlots() {
        Star star = new Star(514);
        star.setStationSlots(5);
        assertEquals(5, star.getStationSlots());
    }

    @Test
    @DisplayName("Should throw on negative station slots")
    void testSetNegativeStationSlots() {
        Star star = new Star(515);
        assertThrows(IllegalArgumentException.class, () -> star.setStationSlots(-1));
    }

    @Test
    @DisplayName("Should return available station slots")
    void testAvailableStationSlots() {
        Star star = new Star(516);
        assertEquals(2, star.getAvailableStationSlots());
        star.addStation("Station-1");
        assertEquals(1, star.getAvailableStationSlots());
    }

    @Test
    @DisplayName("Should check has available station slot")
    void testHasAvailableStationSlot() {
        Star star = new Star(517);
        assertTrue(star.hasAvailableStationSlot());
        star.addStation("Station-1");
        star.addStation("Station-2");
        assertFalse(star.hasAvailableStationSlot());
    }

    @Test
    @DisplayName("Should add station successfully")
    void testAddStation() {
        Star star = new Star(518);
        assertTrue(star.addStation("Solar Array"));
        assertEquals(1, star.getOccupiedStations().size());
        assertTrue(star.getOccupiedStations().contains("Solar Array"));
    }

    @Test
    @DisplayName("Should fail to add station when full")
    void testAddStationWhenFull() {
        Star star = new Star(519);
        star.addStation("Station-1");
        star.addStation("Station-2");
        assertFalse(star.addStation("Station-3"));
    }

    @Test
    @DisplayName("Should remove station")
    void testRemoveStation() {
        Star star = new Star(520);
        star.addStation("Station-1");
        assertTrue(star.removeStation("Station-1"));
        assertEquals(0, star.getOccupiedStations().size());
    }

    @Test
    @DisplayName("Should fail to remove non-existent station")
    void testRemoveNonExistentStation() {
        Star star = new Star(521);
        assertFalse(star.removeStation("NonExistent"));
    }

    @Test
    @DisplayName("Should return unmodifiable occupied stations list")
    void testOccupiedStationsUnmodifiable() {
        Star star = new Star(522);
        assertThrows(UnsupportedOperationException.class, () -> star.getOccupiedStations().add("Hack"));
    }

    // ---- Radiation damage tests ----

    @Test
    @DisplayName("Should return radiation damage from star type")
    void testRadiationDamageFromType() {
        Star star = new Star(523, Star.StarType.BLUE);
        assertEquals(0.2, star.getRadiationDamage(), 0.001);
    }

    @Test
    @DisplayName("Should set custom radiation damage")
    void testSetRadiationDamage() {
        Star star = new Star(524);
        star.setRadiationDamage(0.5);
        assertEquals(0.5, star.getRadiationDamage(), 0.001);
    }

    @Test
    @DisplayName("Should throw on radiation damage below 0")
    void testSetNegativeRadiationDamage() {
        Star star = new Star(525);
        assertThrows(IllegalArgumentException.class, () -> star.setRadiationDamage(-0.1));
    }

    @Test
    @DisplayName("Should throw on radiation damage above 1")
    void testSetRadiationDamageAboveOne() {
        Star star = new Star(526);
        assertThrows(IllegalArgumentException.class, () -> star.setRadiationDamage(1.5));
    }

    @Test
    @DisplayName("Should calculate radiation hull damage")
    void testRadiationHullDamage() {
        Star star = new Star(527, Star.StarType.YELLOW);
        // 0.1 radiation * 100 hull = 10 damage
        assertEquals(10, star.getRadiationHullDamage(100));
    }

    @Test
    @DisplayName("Should calculate hull after radiation")
    void testHullAfterRadiation() {
        Star star = new Star(528, Star.StarType.YELLOW);
        // 100 hull - 10 damage = 90
        assertEquals(90, star.getHullAfterRadiation(100));
    }

    @Test
    @DisplayName("Should not reduce hull below zero")
    void testHullAfterRadiationMinimumZero() {
        Star star = new Star(529, Star.StarType.BLUE);
        // 0.2 radiation * 5 hull = 1 damage, 5 - 1 = 4
        assertEquals(4, star.getHullAfterRadiation(5));
    }

    @Test
    @DisplayName("Should calculate radiation for RED star")
    void testRadiationRedStar() {
        Star star = new Star(530, Star.StarType.RED);
        // 0.05 radiation * 200 hull = 10 damage
        assertEquals(10, star.getRadiationHullDamage(200));
    }

    // ---- Gravity well tests ----

    @Test
    @DisplayName("Should return gravity well bonus from star type")
    void testGravityWellBonusFromType() {
        Star star = new Star(531, Star.StarType.BLUE);
        assertEquals(0.4, star.getGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should set custom gravity well bonus")
    void testSetGravityWellBonus() {
        Star star = new Star(532);
        star.setGravityWellBonus(0.5);
        assertEquals(0.5, star.getGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should throw on gravity well below 0")
    void testSetNegativeGravityWell() {
        Star star = new Star(533);
        assertThrows(IllegalArgumentException.class, () -> star.setGravityWellBonus(-0.1));
    }

    @Test
    @DisplayName("Should throw on gravity well above 1")
    void testSetGravityWellAboveOne() {
        Star star = new Star(534);
        assertThrows(IllegalArgumentException.class, () -> star.setGravityWellBonus(1.5));
    }

    @Test
    @DisplayName("Should calculate slingshot speed")
    void testSlingshotSpeed() {
        Star star = new Star(535, Star.StarType.YELLOW);
        // 10 speed * 1.3 = 13
        assertEquals(13, star.getSlingshotSpeed(10));
    }

    @Test
    @DisplayName("Should calculate slingshot speed for BLUE star")
    void testSlingshotSpeedBlueStar() {
        Star star = new Star(536, Star.StarType.BLUE);
        // 10 speed * 1.4 = 14
        assertEquals(14, star.getSlingshotSpeed(10));
    }

    // ---- Strategic resource tests ----

    @Test
    @DisplayName("Should have strategic deposit for RED star")
    void testStrategicDepositRedStar() {
        Star star = new Star(537, Star.StarType.RED);
        assertTrue(star.hasStrategicDeposit());
        assertEquals(StrategicResource.DURANTIUM, star.getStrategicResource());
    }

    @Test
    @DisplayName("Should not have strategic deposit for YELLOW star")
    void testStrategicDepositYellowStar() {
        Star star = new Star(538, Star.StarType.YELLOW);
        assertFalse(star.hasStrategicDeposit());
        assertNull(star.getStrategicResource());
    }

    @Test
    @DisplayName("Should set strategic deposit")
    void testsetStrategicResource() {
        Star star = new Star(539, Star.StarType.YELLOW);
        star.setStrategicResource(StrategicResource.ELERIUM);
        assertTrue(star.hasStrategicDeposit());
        assertEquals(StrategicResource.ELERIUM, star.getStrategicResource());
    }

    @Test
    @DisplayName("Should clear strategic deposit")
    void testClearStrategicDeposit() {
        Star star = new Star(540, Star.StarType.RED);
        star.setStrategicResource(null);
        assertFalse(star.hasStrategicDeposit());
        assertNull(star.getStrategicResource());
    }

    // ---- Set star type tests ----

    @Test
    @DisplayName("Should update properties when changing star type")
    void testSetStarType() {
        Star star = new Star(541, Star.StarType.YELLOW);
        assertEquals(10, star.getEnergyYield());
        assertEquals(0.1, star.getRadiationDamage(), 0.001);

        star.setStarType(Star.StarType.BLUE);
        assertEquals(15, star.getEnergyYield());
        assertEquals(0.2, star.getRadiationDamage(), 0.001);
        assertEquals(StrategicResource.THULIUM, star.getStrategicResource());
    }

    @Test
    @DisplayName("Should throw on null star type")
    void testSetStarTypeNull() {
        Star star = new Star(542);
        assertThrows(IllegalArgumentException.class, () -> star.setStarType(null));
    }

    // ---- Constants tests ----

    @Test
    @DisplayName("Should have correct default constants")
    void testDefaultConstants() {
        assertEquals(5, Star.DEFAULT_RADIUS);
        assertEquals(10, Star.DEFAULT_ENERGY_YIELD);
        assertEquals(2, Star.DEFAULT_STATION_SLOTS);
        assertEquals(0.1, Star.DEFAULT_RADIATION_DAMAGE, 0.001);
        assertEquals(0.3, Star.DEFAULT_GRAVITY_WELL_BONUS, 0.001);
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should build complete star system")
    void testIntegrationCompleteStarSystem() {
        Star star = new Star(600, 5, Star.StarType.BLUE);
        star.setName("Rigel");

        // Add stations
        assertTrue(star.addStation("Mining Platform"));
        assertTrue(star.addStation("Research Outpost"));
        assertFalse(star.addStation("Overflow Station"));

        // Check energy
        assertEquals(15, star.getEnergyYield());
        // 15 * 1.30 = 19.5, rounded to 20
        assertEquals(20, star.getEffectiveEnergyYield());

        // Check strategic resource
        assertTrue(star.hasStrategicDeposit());
        assertEquals(StrategicResource.THULIUM, star.getStrategicResource());

        // Check radiation
        assertEquals(0.2, star.getRadiationDamage(), 0.001);
        assertEquals(20, star.getRadiationHullDamage(100));

        // Check slingshot
        assertEquals(0.4, star.getGravityWellBonus(), 0.001);
        assertEquals(14, star.getSlingshotSpeed(10));
    }

    @Test
    @DisplayName("Should compare different star types")
    void testIntegrationCompareStarTypes() {
        Star red = new Star(601, Star.StarType.RED);
        Star yellow = new Star(602, Star.StarType.YELLOW);
        Star blue = new Star(603, Star.StarType.BLUE);
        Star purple = new Star(604, Star.StarType.PURPLE);

        // Energy yield comparison
        assertTrue(red.getEnergyYield() < yellow.getEnergyYield());
        assertTrue(yellow.getEnergyYield() < purple.getEnergyYield());
        assertTrue(purple.getEnergyYield() < blue.getEnergyYield());

        // Radiation comparison
        assertTrue(red.getRadiationDamage() < yellow.getRadiationDamage());
        assertTrue(yellow.getRadiationDamage() < purple.getRadiationDamage());
        assertTrue(purple.getRadiationDamage() < blue.getRadiationDamage());

        // Strategic resources
        assertNotNull(red.getStrategicResource());
        assertNull(yellow.getStrategicResource());
        assertNotNull(blue.getStrategicResource());
        assertNotNull(purple.getStrategicResource());
    }

    @Test
    @DisplayName("Should handle station removal and re-add")
    void testIntegrationStationCycle() {
        Star star = new Star(605);
        star.addStation("Station-A");
        star.addStation("Station-B");
        assertEquals(2, star.getOccupiedStations().size());

        star.removeStation("Station-A");
        assertEquals(1, star.getOccupiedStations().size());
        assertTrue(star.hasAvailableStationSlot());

        star.addStation("Station-C");
        assertEquals(2, star.getOccupiedStations().size());
        assertFalse(star.hasAvailableStationSlot());
    }

    @Test
    @DisplayName("Should verify toString includes all fields")
    void testToStringComplete() {
        Star star = new Star(606, 3, Star.StarType.RED);
        star.setName("Proxima");
        star.addStation("Collector");

        String str = star.toString();
        assertTrue(str.contains("name='Proxima'"));
        assertTrue(str.contains("id=606"));
        assertTrue(str.contains("type=RED"));
        assertTrue(str.contains("radius=3"));
        assertTrue(str.contains("energy=8"));
        assertTrue(str.contains("stations=1/2"));
        assertTrue(str.contains("radiation=5%"));
        assertTrue(str.contains("slingshot=20%"));
    }

    // ---- StarSize enum tests ----

    @Test
    @DisplayName("Should have 5 StarSize values")
    void testStarSizeValues() {
        Star.StarSize[] sizes = Star.StarSize.values();
        assertEquals(5, sizes.length);
    }

    @Test
    @DisplayName("Should have correct TINY star size properties")
    void testStarSizeTiny() {
        Star.StarSize size = Star.StarSize.TINY;
        assertEquals(2, size.getBaseRadius());
        assertEquals(4, size.getBaseEnergyYield());
        assertEquals(1, size.getBaseStationSlots());
        assertEquals(0.03, size.getBaseRadiationDamage(), 0.001);
        assertEquals(0.1, size.getBaseGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should have correct SMALL star size properties")
    void testStarSizeSmall() {
        Star.StarSize size = Star.StarSize.SMALL;
        assertEquals(3, size.getBaseRadius());
        assertEquals(6, size.getBaseEnergyYield());
        assertEquals(1, size.getBaseStationSlots());
        assertEquals(0.05, size.getBaseRadiationDamage(), 0.001);
        assertEquals(0.15, size.getBaseGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should have correct MEDIUM star size properties")
    void testStarSizeMedium() {
        Star.StarSize size = Star.StarSize.MEDIUM;
        assertEquals(5, size.getBaseRadius());
        assertEquals(10, size.getBaseEnergyYield());
        assertEquals(2, size.getBaseStationSlots());
        assertEquals(0.1, size.getBaseRadiationDamage(), 0.001);
        assertEquals(0.3, size.getBaseGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should have correct LARGE star size properties")
    void testStarSizeLarge() {
        Star.StarSize size = Star.StarSize.LARGE;
        assertEquals(8, size.getBaseRadius());
        assertEquals(15, size.getBaseEnergyYield());
        assertEquals(3, size.getBaseStationSlots());
        assertEquals(0.15, size.getBaseRadiationDamage(), 0.001);
        assertEquals(0.4, size.getBaseGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should have correct MASSIVE star size properties")
    void testStarSizeMassive() {
        Star.StarSize size = Star.StarSize.MASSIVE;
        assertEquals(12, size.getBaseRadius());
        assertEquals(20, size.getBaseEnergyYield());
        assertEquals(4, size.getBaseStationSlots());
        assertEquals(0.2, size.getBaseRadiationDamage(), 0.001);
        assertEquals(0.5, size.getBaseGravityWellBonus(), 0.001);
    }

    // ---- StarSize constructors ----

    @Test
    @DisplayName("Should create star with StarSize only")
    void testConstructorStarSize() {
        Star star = new Star(700, Star.StarSize.LARGE);
        assertEquals(700, star.getId());
        assertEquals(Star.StarSize.LARGE, star.getStarSize());
        assertEquals(8, star.getRadius());
        assertEquals(15, star.getEnergyYield());
        assertEquals(3, star.getStationSlots());
        assertEquals(0.15, star.getRadiationDamage(), 0.001);
        assertEquals(0.4, star.getGravityWellBonus(), 0.001);
        assertEquals(Star.StarType.YELLOW, star.getStarType());
    }

    @Test
    @DisplayName("Should create star with StarSize and StarType")
    void testConstructorStarSizeAndType() {
        Star star = new Star(701, Star.StarSize.MASSIVE, Star.StarType.BLUE);
        assertEquals(701, star.getId());
        assertEquals(Star.StarSize.MASSIVE, star.getStarSize());
        assertEquals(Star.StarType.BLUE, star.getStarType());
        assertEquals(12, star.getRadius());
        assertEquals(20, star.getEnergyYield());
        assertEquals(4, star.getStationSlots());
        assertEquals(StrategicResource.THULIUM, star.getStrategicResource());
    }

    @Test
    @DisplayName("Should create star with TINY size")
    void testConstructorTinySize() {
        Star star = new Star(702, Star.StarSize.TINY);
        assertEquals(2, star.getRadius());
        assertEquals(4, star.getEnergyYield());
        assertEquals(1, star.getStationSlots());
    }

    @Test
    @DisplayName("Should throw on null StarSize")
    void testConstructorNullStarSize() {
        assertThrows(IllegalArgumentException.class, () -> new Star(703, (Star.StarSize) null));
    }

    @Test
    @DisplayName("Should throw on null StarType with StarSize constructor")
    void testConstructorStarSizeNullType() {
        assertThrows(IllegalArgumentException.class, () -> new Star(704, Star.StarSize.MEDIUM, null));
    }

    // ---- StarSize setter ----

    @Test
    @DisplayName("Should default to MEDIUM size")
    void testDefaultStarSize() {
        Star star = new Star(710);
        assertEquals(Star.StarSize.MEDIUM, star.getStarSize());
    }

    @Test
    @DisplayName("Should update properties when setting star size")
    void testSetStarSize() {
        Star star = new Star(711, Star.StarSize.SMALL);
        assertEquals(3, star.getRadius());
        assertEquals(6, star.getEnergyYield());

        star.setStarSize(Star.StarSize.LARGE);
        assertEquals(Star.StarSize.LARGE, star.getStarSize());
        assertEquals(15, star.getEnergyYield());
        assertEquals(3, star.getStationSlots());
        assertEquals(0.15, star.getRadiationDamage(), 0.001);
        assertEquals(0.4, star.getGravityWellBonus(), 0.001);
    }

    @Test
    @DisplayName("Should throw on null star size setter")
    void testSetStarSizeNull() {
        Star star = new Star(712);
        assertThrows(IllegalArgumentException.class, () -> star.setStarSize(null));
    }

    @Test
    @DisplayName("Should scale energy yield with size")
    void testStarSizeEnergyScaling() {
        Star tiny = new Star(720, Star.StarSize.TINY);
        Star small = new Star(721, Star.StarSize.SMALL);
        Star medium = new Star(722, Star.StarSize.MEDIUM);
        Star large = new Star(723, Star.StarSize.LARGE);
        Star massive = new Star(724, Star.StarSize.MASSIVE);

        assertTrue(tiny.getEnergyYield() < small.getEnergyYield());
        assertTrue(small.getEnergyYield() < medium.getEnergyYield());
        assertTrue(medium.getEnergyYield() < large.getEnergyYield());
        assertTrue(large.getEnergyYield() < massive.getEnergyYield());
    }

    @Test
    @DisplayName("Should scale radiation with size")
    void testStarSizeRadiationScaling() {
        Star tiny = new Star(730, Star.StarSize.TINY);
        Star massive = new Star(731, Star.StarSize.MASSIVE);

        assertTrue(tiny.getRadiationDamage() < massive.getRadiationDamage());
        assertEquals(0.03, tiny.getRadiationDamage(), 0.001);
        assertEquals(0.2, massive.getRadiationDamage(), 0.001);
    }

    @Test
    @DisplayName("Should scale gravity well with size")
    void testStarSizeGravityScaling() {
        Star tiny = new Star(740, Star.StarSize.TINY);
        Star massive = new Star(741, Star.StarSize.MASSIVE);

        assertTrue(tiny.getGravityWellBonus() < massive.getGravityWellBonus());
        assertEquals(0.1, tiny.getGravityWellBonus(), 0.001);
        assertEquals(0.5, massive.getGravityWellBonus(), 0.001);
    }

    // ---- StarSize integration ----

    @Test
    @DisplayName("Should combine StarSize and StarType properties")
    void testIntegrationSizeAndType() {
        Star star = new Star(750, Star.StarSize.LARGE, Star.StarType.RED);
        assertEquals(Star.StarSize.LARGE, star.getStarSize());
        assertEquals(Star.StarType.RED, star.getStarType());
        assertEquals(8, star.getRadius());
        assertEquals(15, star.getEnergyYield());
        assertEquals(3, star.getStationSlots());
        assertEquals(StrategicResource.DURANTIUM, star.getStrategicResource());
        assertTrue(star.hasStrategicDeposit());
    }

    @Test
    @DisplayName("Should compute effective energy with size-based stations")
    void testIntegrationEffectiveEnergySize() {
        Star star = new Star(760, Star.StarSize.MASSIVE);
        assertEquals(20, star.getEnergyYield());
        assertEquals(4, star.getStationSlots());

        star.addStation("Platform-1");
        star.addStation("Platform-2");
        // 20 * 1.30 = 26
        assertEquals(26, star.getEffectiveEnergyYield());
    }

    @Test
    @DisplayName("Should handle size change preserving type")
    void testIntegrationSizeChangePreservesType() {
        Star star = new Star(770, Star.StarSize.SMALL, Star.StarType.PURPLE);
        assertEquals(Star.StarType.PURPLE, star.getStarType());
        assertEquals(StrategicResource.PROMETHION, star.getStrategicResource());

        star.setStarSize(Star.StarSize.MASSIVE);
        assertEquals(Star.StarType.PURPLE, star.getStarType());
        assertEquals(StrategicResource.PROMETHION, star.getStrategicResource());
        assertEquals(20, star.getEnergyYield());
    }

    // ---- Luminosity classification ----

    @Test
    @DisplayName("Should return Dwarf for TINY star size")
    void testLuminosityTiny() {
        Star star = new Star(800, Star.StarSize.TINY);
        assertEquals("Dwarf", star.getLuminosityClass());
    }

    @Test
    @DisplayName("Should return Dwarf for SMALL star size")
    void testLuminositySmall() {
        Star star = new Star(801, Star.StarSize.SMALL);
        assertEquals("Dwarf", star.getLuminosityClass());
    }

    @Test
    @DisplayName("Should return Main Sequence for MEDIUM star size")
    void testLuminosityMedium() {
        Star star = new Star(802, Star.StarSize.MEDIUM);
        assertEquals("Main Sequence", star.getLuminosityClass());
    }

    @Test
    @DisplayName("Should return Giant for LARGE star size")
    void testLuminosityLarge() {
        Star star = new Star(803, Star.StarSize.LARGE);
        assertEquals("Giant", star.getLuminosityClass());
    }

    @Test
    @DisplayName("Should return Supergiant for MASSIVE star size")
    void testLuminosityMassive() {
        Star star = new Star(804, Star.StarSize.MASSIVE);
        assertEquals("Supergiant", star.getLuminosityClass());
    }

    // ---- Spectral class ----

    @Test
    @DisplayName("Should return M for RED star type")
    void testSpectralRed() {
        Star star = new Star(810, Star.StarSize.MEDIUM, Star.StarType.RED);
        assertEquals("M", star.getSpectralClass());
    }

    @Test
    @DisplayName("Should return G for YELLOW star type")
    void testSpectralYellow() {
        Star star = new Star(811, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals("G", star.getSpectralClass());
    }

    @Test
    @DisplayName("Should return O for BLUE star type")
    void testSpectralBlue() {
        Star star = new Star(812, Star.StarSize.MEDIUM, Star.StarType.BLUE);
        assertEquals("O", star.getSpectralClass());
    }

    @Test
    @DisplayName("Should return A for PURPLE star type")
    void testSpectralPurple() {
        Star star = new Star(813, Star.StarSize.MEDIUM, Star.StarType.PURPLE);
        assertEquals("A", star.getSpectralClass());
    }

    // ---- Full classification ----

    @Test
    @DisplayName("Should combine spectral and luminosity for full classification")
    void testFullClassificationRedDwarf() {
        Star star = new Star(820, Star.StarSize.SMALL, Star.StarType.RED);
        assertEquals("M Dwarf", star.getFullClassification());
    }

    @Test
    @DisplayName("Should classify yellow medium star as G Main Sequence")
    void testFullClassificationYellowMainSequence() {
        Star star = new Star(821, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals("G Main Sequence", star.getFullClassification());
    }

    @Test
    @DisplayName("Should classify blue massive star as O Supergiant")
    void testFullClassificationBlueSupergiant() {
        Star star = new Star(822, Star.StarSize.MASSIVE, Star.StarType.BLUE);
        assertEquals("O Supergiant", star.getFullClassification());
    }

    @Test
    @DisplayName("Should classify purple large star as A Giant")
    void testFullClassificationPurpleGiant() {
        Star star = new Star(823, Star.StarSize.LARGE, Star.StarType.PURPLE);
        assertEquals("A Giant", star.getFullClassification());
    }

    @Test
    @DisplayName("Should update classification when size changes")
    void testClassificationUpdateOnSizeChange() {
        Star star = new Star(830, Star.StarSize.SMALL, Star.StarType.RED);
        assertEquals("M Dwarf", star.getFullClassification());

        star.setStarSize(Star.StarSize.MASSIVE);
        assertEquals("M Supergiant", star.getFullClassification());
    }

    @Test
    @DisplayName("Should update classification when type changes")
    void testClassificationUpdateOnTypeChange() {
        Star star = new Star(831, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals("G Main Sequence", star.getFullClassification());

        star.setStarType(Star.StarType.BLUE);
        assertEquals("O Main Sequence", star.getFullClassification());
    }

    @Test
    @DisplayName("Should have all luminosity classes distinct")
    void testAllLuminosityClasses() {
        Star tiny = new Star(840, Star.StarSize.TINY);
        Star small = new Star(841, Star.StarSize.SMALL);
        Star medium = new Star(842, Star.StarSize.MEDIUM);
        Star large = new Star(843, Star.StarSize.LARGE);
        Star massive = new Star(844, Star.StarSize.MASSIVE);

        assertEquals("Dwarf", tiny.getLuminosityClass());
        assertEquals("Dwarf", small.getLuminosityClass());
        assertEquals("Main Sequence", medium.getLuminosityClass());
        assertEquals("Giant", large.getLuminosityClass());
        assertEquals("Supergiant", massive.getLuminosityClass());
    }

    @Test
    @DisplayName("Should have all spectral classes distinct")
    void testAllSpectralClasses() {
        Star red = new Star(850, Star.StarSize.MEDIUM, Star.StarType.RED);
        Star yellow = new Star(851, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        Star blue = new Star(852, Star.StarSize.MEDIUM, Star.StarType.BLUE);
        Star purple = new Star(853, Star.StarSize.MEDIUM, Star.StarType.PURPLE);

        assertEquals("M", red.getSpectralClass());
        assertEquals("G", yellow.getSpectralClass());
        assertEquals("O", blue.getSpectralClass());
        assertEquals("A", purple.getSpectralClass());
    }

    // ---- Surface temperature ----

    @Test
    @DisplayName("Should return correct surface temperature for RED star")
    void testSurfaceTemperatureRed() {
        Star star = new Star(900, Star.StarSize.MEDIUM, Star.StarType.RED);
        assertEquals(3000, star.getSurfaceTemperature());
    }

    @Test
    @DisplayName("Should return correct surface temperature for YELLOW star")
    void testSurfaceTemperatureYellow() {
        Star star = new Star(901, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals(5778, star.getSurfaceTemperature());
    }

    @Test
    @DisplayName("Should return correct surface temperature for BLUE star")
    void testSurfaceTemperatureBlue() {
        Star star = new Star(902, Star.StarSize.MEDIUM, Star.StarType.BLUE);
        assertEquals(30000, star.getSurfaceTemperature());
    }

    @Test
    @DisplayName("Should return correct surface temperature for PURPLE star")
    void testSurfaceTemperaturePurple() {
        Star star = new Star(903, Star.StarSize.MEDIUM, Star.StarType.PURPLE);
        assertEquals(7500, star.getSurfaceTemperature());
    }

    @Test
    @DisplayName("Should update temperature when type changes")
    void testTemperatureUpdateOnTypeChange() {
        Star star = new Star(904, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals(5778, star.getSurfaceTemperature());
        
        star.setStarType(Star.StarType.BLUE);
        assertEquals(30000, star.getSurfaceTemperature());
    }

    // ---- Absolute magnitude ----

    @Test
    @DisplayName("Should return Sun-like magnitude for MEDIUM YELLOW star")
    void testAbsoluteMagnitudeSun() {
        Star star = new Star(910, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals(4.8, star.getAbsoluteMagnitude(), 0.01);
    }

    @Test
    @DisplayName("Should return brighter magnitude for MASSIVE star")
    void testAbsoluteMagnitudeMassive() {
        Star star = new Star(911, Star.StarSize.MASSIVE, Star.StarType.BLUE);
        // MASSIVE base: -10.0, BLUE adjustment: -3.0 = -13.0
        assertEquals(-13.0, star.getAbsoluteMagnitude(), 0.01);
    }

    @Test
    @DisplayName("Should return dimmer magnitude for TINY star")
    void testAbsoluteMagnitudeTiny() {
        Star star = new Star(912, Star.StarSize.TINY, Star.StarType.RED);
        // TINY base: 16.0, RED adjustment: 1.0 = 17.0
        assertEquals(17.0, star.getAbsoluteMagnitude(), 0.01);
    }

    @Test
    @DisplayName("Should have magnitude decrease with size")
    void testMagnitudeSizeProgression() {
        Star tiny = new Star(913, Star.StarSize.TINY, Star.StarType.YELLOW);
        Star small = new Star(914, Star.StarSize.SMALL, Star.StarType.YELLOW);
        Star medium = new Star(915, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        Star large = new Star(916, Star.StarSize.LARGE, Star.StarType.YELLOW);
        Star massive = new Star(917, Star.StarSize.MASSIVE, Star.StarType.YELLOW);

        assertTrue(tiny.getAbsoluteMagnitude() > small.getAbsoluteMagnitude());
        assertTrue(small.getAbsoluteMagnitude() > medium.getAbsoluteMagnitude());
        assertTrue(medium.getAbsoluteMagnitude() > large.getAbsoluteMagnitude());
        assertTrue(large.getAbsoluteMagnitude() > massive.getAbsoluteMagnitude());
    }

    // ---- Lifespan ----

    @Test
    @DisplayName("Should return correct lifespan for MEDIUM RED star")
    void testLifespanMediumRed() {
        Star star = new Star(920, Star.StarSize.MEDIUM, Star.StarType.RED);
        // RED base: 100,000 MY, MEDIUM modifier: 1.0 = 100,000 MY
        assertEquals(100000, star.getLifespan());
    }

    @Test
    @DisplayName("Should return correct lifespan for MEDIUM YELLOW star")
    void testLifespanMediumYellow() {
        Star star = new Star(921, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        // YELLOW base: 10,000 MY, MEDIUM modifier: 1.0 = 10,000 MY
        assertEquals(10000, star.getLifespan());
    }

    @Test
    @DisplayName("Should return shorter lifespan for MASSIVE star")
    void testLifespanMassive() {
        Star star = new Star(922, Star.StarSize.MASSIVE, Star.StarType.BLUE);
        // BLUE base: 1,000 MY, MASSIVE modifier: 0.25 = 250 MY
        assertEquals(250, star.getLifespan());
    }

    @Test
    @DisplayName("Should return longer lifespan for TINY star")
    void testLifespanTiny() {
        Star star = new Star(923, Star.StarSize.TINY, Star.StarType.RED);
        // RED base: 100,000 MY, TINY modifier: 2.0 = 200,000 MY
        assertEquals(200000, star.getLifespan());
    }

    @Test
    @DisplayName("Should have lifespan decrease with size")
    void testLifespanSizeProgression() {
        Star tiny = new Star(924, Star.StarSize.TINY, Star.StarType.YELLOW);
        Star small = new Star(925, Star.StarSize.SMALL, Star.StarType.YELLOW);
        Star medium = new Star(926, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        Star large = new Star(927, Star.StarSize.LARGE, Star.StarType.YELLOW);
        Star massive = new Star(928, Star.StarSize.MASSIVE, Star.StarType.YELLOW);

        assertTrue(tiny.getLifespan() > small.getLifespan());
        assertTrue(small.getLifespan() > medium.getLifespan());
        assertTrue(medium.getLifespan() > large.getLifespan());
        assertTrue(large.getLifespan() > massive.getLifespan());
    }

    @Test
    @DisplayName("Should update lifespan when size changes")
    void testLifespanUpdateOnSizeChange() {
        Star star = new Star(929, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        assertEquals(10000, star.getLifespan());
        
        star.setStarSize(Star.StarSize.MASSIVE);
        assertEquals(2500, star.getLifespan());
    }

    // ---- Habitable zone ----

    @Test
    @DisplayName("Should return habitable zone for Sun-like star")
    void testHabitableZoneSun() {
        Star star = new Star(930, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        // Sun-like: inner ~1, outer ~1
        int inner = star.getHabitableZoneInner();
        int outer = star.getHabitableZoneOuter();
        assertTrue(inner >= 1);
        assertTrue(outer >= inner);
    }

    @Test
    @DisplayName("Should have wider habitable zone for brighter stars")
    void testHabitableZoneBrighterStar() {
        Star sun = new Star(931, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        Star bright = new Star(932, Star.StarSize.MASSIVE, Star.StarType.BLUE);
        
        assertTrue(bright.getHabitableZoneInner() > sun.getHabitableZoneInner());
        assertTrue(bright.getHabitableZoneOuter() > sun.getHabitableZoneOuter());
    }

    @Test
    @DisplayName("Should have narrower habitable zone for dimmer stars")
    void testHabitableZoneDimmerStar() {
        Star sun = new Star(933, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        Star dim = new Star(934, Star.StarSize.TINY, Star.StarType.RED);
        
        assertTrue(dim.getHabitableZoneInner() <= sun.getHabitableZoneInner());
        assertTrue(dim.getHabitableZoneOuter() <= sun.getHabitableZoneOuter());
    }

    @Test
    @DisplayName("Should check if distance is in habitable zone")
    void testIsInHabitableZone() {
        Star star = new Star(935, Star.StarSize.MEDIUM, Star.StarType.YELLOW);
        int inner = star.getHabitableZoneInner();
        int outer = star.getHabitableZoneOuter();
        
        assertFalse(star.isInHabitableZone(inner - 1));
        assertTrue(star.isInHabitableZone(inner));
        assertTrue(star.isInHabitableZone(outer));
        assertFalse(star.isInHabitableZone(outer + 1));
    }

    @Test
    @DisplayName("Should have outer zone >= inner zone")
    void testHabitableZoneBounds() {
        for (Star.StarSize size : Star.StarSize.values()) {
            for (Star.StarType type : Star.StarType.values()) {
                Star star = new Star(940, size, type);
                assertTrue(star.getHabitableZoneOuter() >= star.getHabitableZoneInner(),
                    "Outer zone should be >= inner zone for " + size + " " + type);
            }
        }
    }

    // ---- StarType enum properties ----

    @Test
    @DisplayName("Should have correct surface temperatures for all types")
    void testStarTypeSurfaceTemperatures() {
        assertEquals(3000, Star.StarType.RED.getSurfaceTemperature());
        assertEquals(5778, Star.StarType.YELLOW.getSurfaceTemperature());
        assertEquals(30000, Star.StarType.BLUE.getSurfaceTemperature());
        assertEquals(7500, Star.StarType.PURPLE.getSurfaceTemperature());
    }

    @Test
    @DisplayName("Should have correct lifespans for all types")
    void testStarTypeLifespans() {
        assertEquals(100000, Star.StarType.RED.getLifespanMillionYears());
        assertEquals(10000, Star.StarType.YELLOW.getLifespanMillionYears());
        assertEquals(1000, Star.StarType.BLUE.getLifespanMillionYears());
        assertEquals(5000, Star.StarType.PURPLE.getLifespanMillionYears());
    }

    @Test
    @DisplayName("Should have RED stars coolest and BLUE stars hottest")
    void testTemperatureOrder() {
        assertTrue(Star.StarType.RED.getSurfaceTemperature() < Star.StarType.YELLOW.getSurfaceTemperature());
        assertTrue(Star.StarType.YELLOW.getSurfaceTemperature() < Star.StarType.PURPLE.getSurfaceTemperature());
        assertTrue(Star.StarType.PURPLE.getSurfaceTemperature() < Star.StarType.BLUE.getSurfaceTemperature());
    }

    @Test
    @DisplayName("Should have RED stars longest-lived and BLUE stars shortest-lived")
    void testLifespanOrder() {
        assertTrue(Star.StarType.BLUE.getLifespanMillionYears() < Star.StarType.PURPLE.getLifespanMillionYears());
        assertTrue(Star.StarType.PURPLE.getLifespanMillionYears() < Star.StarType.YELLOW.getLifespanMillionYears());
        assertTrue(Star.StarType.YELLOW.getLifespanMillionYears() < Star.StarType.RED.getLifespanMillionYears());
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should calculate habitable zone for red dwarf")
    void testRedDwarfHabitableZone() {
        Star star = new Star(950, Star.StarSize.SMALL, Star.StarType.RED);
        
        assertEquals(3000, star.getSurfaceTemperature());
        assertEquals("M Dwarf", star.getFullClassification());
        assertTrue(star.getLifespan() > 100000, "Red dwarfs should live very long");
        assertTrue(star.getHabitableZoneInner() >= 1);
        assertTrue(star.getHabitableZoneOuter() >= star.getHabitableZoneInner());
    }

    @Test
    @DisplayName("Should calculate habitable zone for blue supergiant")
    void testBlueSupergiantHabitableZone() {
        Star star = new Star(951, Star.StarSize.MASSIVE, Star.StarType.BLUE);
        
        assertEquals(30000, star.getSurfaceTemperature());
        assertEquals("O Supergiant", star.getFullClassification());
        assertTrue(star.getLifespan() < 1000, "Blue supergiants should have short lifespan");
        assertTrue(star.getHabitableZoneOuter() > 5, "Bright stars should have wide habitable zone");
    }

    @Test
    @DisplayName("Should integrate all astronomical properties")
    void testAstronomicalPropertiesIntegration() {
        Star star = new Star(952, Star.StarSize.LARGE, Star.StarType.PURPLE);
        
        assertEquals(7500, star.getSurfaceTemperature());
        assertEquals("A", star.getSpectralClass());
        assertEquals("Giant", star.getLuminosityClass());
        assertEquals("A Giant", star.getFullClassification());
        assertEquals(StrategicResource.PROMETHION, star.getStrategicResource());
        assertTrue(star.getLifespan() > 0);
        assertTrue(star.getHabitableZoneOuter() >= star.getHabitableZoneInner());
    }
}
