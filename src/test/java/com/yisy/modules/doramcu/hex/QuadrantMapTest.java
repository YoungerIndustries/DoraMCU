package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuadrantMap Tests")
class QuadrantMapTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create quadrant with default 10x10 size")
    void testDefaultConstructor() {
        QuadrantMap quadrant = new QuadrantMap(0);
        assertEquals(0, quadrant.getId());
        assertEquals(10, quadrant.getWidth());
        assertEquals(10, quadrant.getHeight());
        assertEquals(100, quadrant.size());
        assertEquals(10000, quadrant.totalHexes());
    }

    @Test
    @DisplayName("Should create quadrant with custom dimensions")
    void testCustomConstructor() {
        QuadrantMap quadrant = new QuadrantMap(1, 5, 3);
        assertEquals(1, quadrant.getId());
        assertEquals(5, quadrant.getWidth());
        assertEquals(3, quadrant.getHeight());
        assertEquals(15, quadrant.size());
    }

    @Test
    @DisplayName("Should create quadrant with negative ID")
    void testNegativeId() {
        QuadrantMap quadrant = new QuadrantMap(-1);
        assertEquals(-1, quadrant.getId());
        assertEquals(100, quadrant.size());
    }

    @Test
    @DisplayName("Should create single sector quadrant (1x1)")
    void testSingleSectorQuadrant() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);
        assertEquals(1, quadrant.size());
        assertEquals(1, quadrant.getWidth());
        assertEquals(1, quadrant.getHeight());

        Sector sector = quadrant.get(0, 0);
        assertNotNull(sector);
        assertNull(quadrant.get(1, 0));
        assertNull(quadrant.get(0, 1));
    }

    // ---- Accessors ----

    @Test
    @DisplayName("Should return correct width and height")
    void testAccessors() {
        QuadrantMap quadrant = new QuadrantMap(5, 8, 6);
        assertEquals(5, quadrant.getId());
        assertEquals(8, quadrant.getWidth());
        assertEquals(6, quadrant.getHeight());
        assertEquals(48, quadrant.size());
    }

    @Test
    @DisplayName("Should return total hexes count")
    void testTotalHexes() {
        QuadrantMap quadrant = new QuadrantMap(0, 3, 4);
        // 3x4 sectors, each with 10x10 hexes = 12 * 100 = 1200
        assertEquals(1200, quadrant.totalHexes());
    }

    // ---- Sector operations ----

    @Test
    @DisplayName("Should get sector by coordinates")
    void testGetSector() {
        QuadrantMap quadrant = new QuadrantMap(0);
        Sector sector = quadrant.get(0, 0);
        assertNotNull(sector);
        assertEquals(0, sector.getId());
    }

    @Test
    @DisplayName("Should return null for out of bounds sector")
    void testGetSectorOutOfBounds() {
        QuadrantMap quadrant = new QuadrantMap(0);
        assertNull(quadrant.get(10, 0));
        assertNull(quadrant.get(0, 10));
        assertNull(quadrant.get(-1, 0));
    }

    @Test
    @DisplayName("Should get sector at corner positions")
    void testGetCornerSectors() {
        QuadrantMap quadrant = new QuadrantMap(0);
        assertNotNull(quadrant.get(0, 0));
        assertNotNull(quadrant.get(9, 0));
        assertNotNull(quadrant.get(0, 9));
        assertNotNull(quadrant.get(9, 9));
    }

    @Test
    @DisplayName("Should check contains for valid coordinates")
    void testContains() {
        QuadrantMap quadrant = new QuadrantMap(0);
        assertTrue(quadrant.contains(0, 0));
        assertTrue(quadrant.contains(9, 9));
        assertFalse(quadrant.contains(10, 0));
        assertFalse(quadrant.contains(0, 10));
        assertFalse(quadrant.contains(-1, 0));
    }

    @Test
    @DisplayName("Should return all sectors as unmodifiable collection")
    void testGetAll() {
        QuadrantMap quadrant = new QuadrantMap(0, 3, 3);
        Collection<Sector> all = quadrant.getAll();
        assertEquals(9, all.size());

        assertThrows(UnsupportedOperationException.class, () -> all.add(new Sector(999)));
    }

    @Test
    @DisplayName("Should verify all sectors have correct IDs")
    void testSectorIds() {
        QuadrantMap quadrant = new QuadrantMap(0, 3, 3);
        // Sector IDs are computed as q + r * width
        assertEquals(0, quadrant.get(0, 0).getId());
        assertEquals(1, quadrant.get(1, 0).getId());
        assertEquals(3, quadrant.get(0, 1).getId());
        assertEquals(4, quadrant.get(1, 1).getId());
        assertEquals(8, quadrant.get(2, 2).getId());
    }

    // ---- Hex operations (cross-sector) ----

    @Test
    @DisplayName("Should get hex by global coordinates")
    void testGetHex() {
        QuadrantMap quadrant = new QuadrantMap(0);
        Hex hex = quadrant.getHex(0, 0);
        assertNotNull(hex);
        assertEquals(0, hex.getQ());
        assertEquals(0, hex.getR());
    }

    @Test
    @DisplayName("Should get hex at sector boundary")
    void testGetHexAtBoundary() {
        QuadrantMap quadrant = new QuadrantMap(0);
        // (9, 0) is the last hex in sector (0, 0)
        Hex hex1 = quadrant.getHex(9, 0);
        assertNotNull(hex1);
        assertEquals(9, hex1.getQ());

        // (10, 0) is the first hex in sector (1, 0)
        Hex hex2 = quadrant.getHex(10, 0);
        assertNotNull(hex2);
        assertEquals(0, hex2.getQ());
    }

    @Test
    @DisplayName("Should get hex across sectors")
    void testGetHexAcrossSectors() {
        QuadrantMap quadrant = new QuadrantMap(0);
        // Global (15, 15) should be in sector (1, 1) at local (5, 5)
        Hex hex = quadrant.getHex(15, 15);
        assertNotNull(hex);
        assertEquals(5, hex.getQ());
        assertEquals(5, hex.getR());
    }

    @Test
    @DisplayName("Should return null for hex out of bounds")
    void testGetHexOutOfBounds() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // 2x2 sectors = 20x20 global hexes (0-19)
        assertNull(quadrant.getHex(20, 0));
        assertNull(quadrant.getHex(0, 20));
    }

    @Test
    @DisplayName("Should check containsHex for valid coordinates")
    void testContainsHex() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        assertTrue(quadrant.containsHex(0, 0));
        assertTrue(quadrant.containsHex(19, 19));
        assertFalse(quadrant.containsHex(20, 0));
        assertFalse(quadrant.containsHex(0, 20));
    }

    @Test
    @DisplayName("Should place unit via global hex coordinates")
    void testPlaceViaGlobalHex() {
        QuadrantMap quadrant = new QuadrantMap(0);
        Hex hex = quadrant.getHex(5, 5);
        hex.place("unit");
        assertEquals(HexState.OCCUPIED, hex.getState());
    }

    // ---- Query operations ----

    @Test
    @DisplayName("Should find hexes by state across all sectors")
    void testFindByState() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(15, 15).place("unit2");

        List<Hex> occupied = quadrant.findByState(HexState.OCCUPIED);
        assertEquals(2, occupied.size());
    }

    @Test
    @DisplayName("Should count occupied hexes across all sectors")
    void testOccupiedCount() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(1, 0).place("unit2");
        quadrant.getHex(15, 15).place("unit3");

        assertEquals(3, quadrant.getOccupiedCount());
    }

    @Test
    @DisplayName("Should count empty hexes across all sectors")
    void testEmptyCount() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);
        // 1 sector = 100 hexes
        assertEquals(100, quadrant.getEmptyCount());

        quadrant.getHex(0, 0).place("unit");
        assertEquals(99, quadrant.getEmptyCount());
    }

    @Test
    @DisplayName("Should compute occupancy ratio")
    void testOccupancyRatio() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);
        assertEquals(0.0, quadrant.getOccupancyRatio(), 0.001);

        quadrant.getHex(0, 0).place("unit");
        assertEquals(1.0 / 100.0, quadrant.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should count sectors by state")
    void testSectorCountByState() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(15, 15).place("unit2");

        // Two different sectors have occupied hexes
        assertEquals(2, quadrant.getSectorCountByState(HexState.OCCUPIED));
    }

    @Test
    @DisplayName("Should return empty list for state with no matches")
    void testFindByStateNoMatches() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        List<Hex> blocked = quadrant.findByState(HexState.BLOCKED);
        assertTrue(blocked.isEmpty());
    }

    // ---- Coordinate conversion utilities ----

    @Test
    @DisplayName("Should convert global Q to sector Q")
    void testToSectorQ() {
        assertEquals(0, QuadrantMap.toSectorQ(0));
        assertEquals(0, QuadrantMap.toSectorQ(9));
        assertEquals(1, QuadrantMap.toSectorQ(10));
        assertEquals(2, QuadrantMap.toSectorQ(20));
    }

    @Test
    @DisplayName("Should convert global R to sector R")
    void testToSectorR() {
        assertEquals(0, QuadrantMap.toSectorR(0));
        assertEquals(0, QuadrantMap.toSectorR(9));
        assertEquals(1, QuadrantMap.toSectorR(10));
        assertEquals(2, QuadrantMap.toSectorR(20));
    }

    @Test
    @DisplayName("Should convert global Q to local hex Q")
    void testToLocalHexQ() {
        assertEquals(0, QuadrantMap.toLocalHexQ(0));
        assertEquals(9, QuadrantMap.toLocalHexQ(9));
        assertEquals(0, QuadrantMap.toLocalHexQ(10));
        assertEquals(5, QuadrantMap.toLocalHexQ(15));
    }

    @Test
    @DisplayName("Should convert global R to local hex R")
    void testToLocalHexR() {
        assertEquals(0, QuadrantMap.toLocalHexR(0));
        assertEquals(9, QuadrantMap.toLocalHexR(9));
        assertEquals(0, QuadrantMap.toLocalHexR(10));
        assertEquals(5, QuadrantMap.toLocalHexR(15));
    }

    @Test
    @DisplayName("Should compute global Q from sector and local")
    void testToGlobalQ() {
        assertEquals(0, QuadrantMap.toGlobalQ(0, 0));
        assertEquals(15, QuadrantMap.toGlobalQ(1, 5));
        assertEquals(99, QuadrantMap.toGlobalQ(9, 9));
    }

    @Test
    @DisplayName("Should compute global R from sector and local")
    void testToGlobalR() {
        assertEquals(0, QuadrantMap.toGlobalR(0, 0));
        assertEquals(15, QuadrantMap.toGlobalR(1, 5));
        assertEquals(99, QuadrantMap.toGlobalR(9, 9));
    }

    @Test
    @DisplayName("Should round-trip coordinate conversion")
    void testCoordinateRoundTrip() {
        int globalQ = 47;
        int globalR = 83;

        int sectorQ = QuadrantMap.toSectorQ(globalQ);
        int sectorR = QuadrantMap.toSectorR(globalR);
        int localQ = QuadrantMap.toLocalHexQ(globalQ);
        int localR = QuadrantMap.toLocalHexR(globalR);

        assertEquals(globalQ, QuadrantMap.toGlobalQ(sectorQ, localQ));
        assertEquals(globalR, QuadrantMap.toGlobalR(sectorR, localR));
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should handle mixed states across sectors")
    void testMixedStatesIntegration() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        // Place units in different sectors
        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(0, 0).select();
        quadrant.getHex(15, 15).place("unit2");
        quadrant.getHex(5, 5).block();

        assertEquals(1, quadrant.getOccupiedCount());
        assertEquals(1, quadrant.findByState(HexState.SELECTED).size());
        assertEquals(1, quadrant.findByState(HexState.BLOCKED).size());
    }

    @Test
    @DisplayName("Should support multiple independent quadrants")
    void testMultipleQuadrants() {
        QuadrantMap q1 = new QuadrantMap(0, 2, 2);
        QuadrantMap q2 = new QuadrantMap(1, 2, 2);

        q1.getHex(0, 0).place("unit1");
        q2.getHex(0, 0).place("unit2");

        assertEquals(1, q1.getOccupiedCount());
        assertEquals(1, q2.getOccupiedCount());
    }

    @Test
    @DisplayName("Should verify all sectors are initially empty")
    void testAllSectorsInitiallyEmpty() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        for (Sector sector : quadrant.getAll()) {
            assertEquals(100, sector.size());
            assertEquals(100, sector.getEmptyCount());
            assertEquals(0, sector.getOccupiedCount());
        }
    }

    @Test
    @DisplayName("Should support place and remove cycle across sectors")
    void testPlaceRemoveCycle() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit");
        assertEquals(1, quadrant.getOccupiedCount());

        quadrant.getHex(0, 0).remove();
        assertEquals(0, quadrant.getOccupiedCount());

        // Re-place in different sector
        quadrant.getHex(15, 15).place("anotherUnit");
        assertEquals(1, quadrant.getOccupiedCount());
    }

    @Test
    @DisplayName("Should toString include dimensions")
    void testToString() {
        QuadrantMap quadrant = new QuadrantMap(7, 5, 3);
        String str = quadrant.toString();
        assertTrue(str.contains("id=7"));
        assertTrue(str.contains("size=5x3"));
        assertTrue(str.contains("sectors=15"));
    }

    @Test
    @DisplayName("Should handle large quadrant ID")
    void testLargeQuadrantId() {
        QuadrantMap quadrant = new QuadrantMap(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, quadrant.getId());
        assertEquals(100, quadrant.size());
    }

    @Test
    @DisplayName("Should verify sector constants")
    void testConstants() {
        assertEquals(10, QuadrantMap.DEFAULT_WIDTH);
        assertEquals(10, QuadrantMap.DEFAULT_HEIGHT);
    }

    @Test
    @DisplayName("Should handle single row of sectors")
    void testSingleRowSectors() {
        QuadrantMap quadrant = new QuadrantMap(0, 5, 1);
        assertEquals(5, quadrant.size());
        assertEquals(5, quadrant.getWidth());
        assertEquals(1, quadrant.getHeight());

        for (int q = 0; q < 5; q++) {
            assertNotNull(quadrant.get(q, 0));
        }
        assertNull(quadrant.get(0, 1));
    }

    @Test
    @DisplayName("Should handle single column of sectors")
    void testSingleColumnSectors() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 5);
        assertEquals(5, quadrant.size());
        assertEquals(1, quadrant.getWidth());
        assertEquals(5, quadrant.getHeight());

        for (int r = 0; r < 5; r++) {
            assertNotNull(quadrant.get(0, r));
        }
        assertNull(quadrant.get(1, 0));
    }

    // ---- Neighbor operations ----

    @Test
    @DisplayName("Should get neighbors for hex in center of sector")
    void testGetNeighborsCenter() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // Hex at global (15, 15) is local (5, 5) in sector (1, 1) — far from boundaries
        Hex hex = quadrant.getHex(15, 15);
        List<Hex> neighbors = quadrant.getNeighbors(hex);
        assertEquals(6, neighbors.size());
    }

    @Test
    @DisplayName("Should get neighbors for hex at sector boundary")
    void testGetNeighborsAtBoundary() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // Hex at global (9, 5) is the last column in sector (0, 0)
        Hex hex = quadrant.getHex(9, 5);
        List<Hex> neighbors = quadrant.getNeighbors(hex);
        // Some neighbors may cross into sector (1, 0), some may be out of bounds depending on hex shape
        assertNotNull(neighbors);
        assertTrue(neighbors.size() >= 1);
    }

    @Test
    @DisplayName("Should get neighbors for hex at corner of quadrant")
    void testGetNeighborsAtCorner() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // Hex at global (0, 0) is the top-left corner
        Hex hex = quadrant.getHex(0, 0);
        List<Hex> neighbors = quadrant.getNeighbors(hex);
        // Corner hex has fewer neighbors within bounds
        assertNotNull(neighbors);
        assertTrue(neighbors.size() >= 1);
        assertTrue(neighbors.size() < 6);
    }

    @Test
    @DisplayName("Should get neighbors for hex at edge of quadrant")
    void testGetNeighborsAtEdge() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // Hex at global (0, 10) is on the left edge, second sector row
        Hex hex = quadrant.getHex(0, 10);
        List<Hex> neighbors = quadrant.getNeighbors(hex);
        assertNotNull(neighbors);
        assertTrue(neighbors.size() >= 1);
    }

    @Test
    @DisplayName("Should get neighbors crossing sector boundary")
    void testGetNeighborsCrossSector() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // Hex at global (9, 0) is at the right edge of sector (0, 0)
        Hex hex = quadrant.getHex(9, 0);
        List<Hex> neighbors = quadrant.getNeighbors(hex);
        // Neighbors should include hexes from both the current sector and potentially the next sector
        assertNotNull(neighbors);
        assertTrue(neighbors.size() >= 1, "Should have at least one neighbor");
    }

    @Test
    @DisplayName("Should return empty neighbors for hex not in quadrant")
    void testGetNeighborsNotInQuadrant() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // Create a hex that doesn't exist in any sector
        Hex externalHex = new Hex(99, 99);
        List<Hex> neighbors = quadrant.getNeighbors(externalHex);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    @DisplayName("Should get neighbors for hex in single sector quadrant")
    void testGetNeighborsSingleSector() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);
        // Hex at (5, 5) in a single sector — all neighbors should be within bounds
        Hex hex = quadrant.getHex(5, 5);
        List<Hex> neighbors = quadrant.getNeighbors(hex);
        assertEquals(6, neighbors.size());
    }

    // ---- Additional query operations ----

    @Test
    @DisplayName("Should find hexes by state for all states")
    void testFindByStateAllStates() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(0, 1).place("unit2");
        quadrant.getHex(0, 1).select();
        quadrant.getHex(1, 0).block();

        assertEquals(1, quadrant.findByState(HexState.OCCUPIED).size());
        assertEquals(1, quadrant.findByState(HexState.SELECTED).size());
        assertEquals(1, quadrant.findByState(HexState.BLOCKED).size());
        assertEquals(397, quadrant.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should count sectors by state for all states")
    void testSectorCountByStateAll() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(15, 15).place("unit2");
        quadrant.getHex(5, 5).block();

        // unit1 is in sector (0,0), unit2 in sector (1,1), blocked in sector (0,0)
        assertEquals(2, quadrant.getSectorCountByState(HexState.OCCUPIED));
        assertEquals(1, quadrant.getSectorCountByState(HexState.BLOCKED));
        assertEquals(0, quadrant.getSectorCountByState(HexState.SELECTED));
    }

    @Test
    @DisplayName("Should compute occupancy ratio with multiple operations")
    void testOccupancyRatioMultipleOperations() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);
        // 100 hexes total

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(0, 1).place("unit2");
        quadrant.getHex(0, 2).place("unit3");

        assertEquals(3.0 / 100.0, quadrant.getOccupancyRatio(), 0.001);

        quadrant.getHex(0, 0).remove();
        assertEquals(2.0 / 100.0, quadrant.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should count empty hexes after multiple operations")
    void testEmptyCountMultipleOperations() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);

        quadrant.getHex(0, 0).place("unit1");
        quadrant.getHex(0, 1).block();
        quadrant.getHex(0, 2).place("unit2");

        // 100 - 2 occupied - 1 blocked = 97 empty
        assertEquals(97, quadrant.getEmptyCount());
    }

    // ---- Additional hex operations ----

    @Test
    @DisplayName("Should get hex at negative global coordinates")
    void testGetHexNegativeCoordinates() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        assertNull(quadrant.getHex(-1, 0));
        assertNull(quadrant.getHex(0, -1));
    }

    @Test
    @DisplayName("Should check containsHex for negative coordinates")
    void testContainsHexNegative() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // In Java: -1 / 10 = 0, so containsHex(-1, 0) checks contains(0, 0) which is true
        assertTrue(quadrant.containsHex(-1, 0));
        // -11 / 10 = -1, so sector (-1, 0) doesn't exist
        assertFalse(quadrant.containsHex(-11, 0));
        assertFalse(quadrant.containsHex(0, -11));
    }

    @Test
    @DisplayName("Should get hex at maximum global coordinates")
    void testGetHexMaxCoordinates() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        // 2x2 sectors = 20x20 global hexes (0-19)
        Hex hex = quadrant.getHex(19, 19);
        assertNotNull(hex);
        assertEquals(9, hex.getQ());
        assertEquals(9, hex.getR());
    }

    @Test
    @DisplayName("Should handle block and unblock cycle across sectors")
    void testBlockUnblockCycle() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).block();
        assertEquals(1, quadrant.findByState(HexState.BLOCKED).size());

        quadrant.getHex(0, 0).unblock();
        assertEquals(0, quadrant.findByState(HexState.BLOCKED).size());

        // Re-block in different sector
        quadrant.getHex(15, 15).block();
        assertEquals(1, quadrant.findByState(HexState.BLOCKED).size());
    }

    @Test
    @DisplayName("Should handle select and deselect cycle across sectors")
    void testSelectDeselectCycle() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);

        quadrant.getHex(0, 0).place("unit");
        quadrant.getHex(0, 0).select();
        assertEquals(1, quadrant.findByState(HexState.SELECTED).size());

        quadrant.getHex(0, 0).deselect();
        assertEquals(0, quadrant.findByState(HexState.SELECTED).size());
        assertEquals(1, quadrant.findByState(HexState.OCCUPIED).size());
    }

    @Test
    @DisplayName("Should verify sector sizes in getAll collection")
    void testGetAllSectorSizes() {
        QuadrantMap quadrant = new QuadrantMap(0, 3, 3);
        Collection<Sector> all = quadrant.getAll();
        assertEquals(9, all.size());

        for (Sector sector : all) {
            assertEquals(100, sector.size());
        }
    }

    @Test
    @DisplayName("Should handle full state cycle across sectors")
    void testFullStateCycle() {
        QuadrantMap quadrant = new QuadrantMap(0, 1, 1);
        Hex hex = quadrant.getHex(5, 5);

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

    @Test
    @DisplayName("Should handle large default quadrant")
    void testLargeDefaultQuadrant() {
        QuadrantMap quadrant = new QuadrantMap(0);
        assertEquals(100, quadrant.size());
        assertEquals(10000, quadrant.totalHexes());
        assertEquals(10000, quadrant.getEmptyCount());
        assertEquals(0, quadrant.getOccupiedCount());
    }

    @Test
    @DisplayName("Should verify toString includes hex count")
    void testToStringHexCount() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        String str = quadrant.toString();
        assertTrue(str.contains("hexes=400"));
    }

    @Test
    @DisplayName("Should handle coordinate conversion for boundary values")
    void testCoordinateConversionBoundary() {
        // Exactly at sector boundary
        assertEquals(0, QuadrantMap.toSectorQ(0));
        assertEquals(0, QuadrantMap.toLocalHexQ(0));

        // Just before next sector
        assertEquals(0, QuadrantMap.toSectorQ(9));
        assertEquals(9, QuadrantMap.toLocalHexQ(9));

        // Just after sector boundary
        assertEquals(1, QuadrantMap.toSectorQ(10));
        assertEquals(0, QuadrantMap.toLocalHexQ(10));

        // End of quadrant
        assertEquals(9, QuadrantMap.toSectorQ(99));
        assertEquals(9, QuadrantMap.toLocalHexQ(99));
    }

    @Test
    @DisplayName("Should verify neighbors are distinct hexes")
    void testNeighborsAreDistinct() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        Hex hex = quadrant.getHex(15, 15);
        List<Hex> neighbors = quadrant.getNeighbors(hex);

        // All neighbors should be distinct
        long uniqueCount = neighbors.stream().distinct().count();
        assertEquals(neighbors.size(), uniqueCount);
    }

    @Test
    @DisplayName("Should verify neighbors do not include the hex itself")
    void testNeighborsExcludeSelf() {
        QuadrantMap quadrant = new QuadrantMap(0, 2, 2);
        Hex hex = quadrant.getHex(15, 15);
        List<Hex> neighbors = quadrant.getNeighbors(hex);

        assertFalse(neighbors.contains(hex));
    }
}
