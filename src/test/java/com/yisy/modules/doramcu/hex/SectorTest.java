package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sector Tests")
class SectorTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create default 10x10 sector with given ID")
    void testDefaultConstructor() {
        Sector sector = new Sector(0);
        assertEquals(0, sector.getId());
        assertEquals(10, sector.getWidth());
        assertEquals(10, sector.getHeight());
        assertEquals(100, sector.size());
    }

    @Test
    @DisplayName("Should create custom size sector")
    void testCustomSizeConstructor() {
        Sector sector = new Sector(5, 5, 3);
        assertEquals(5, sector.getId());
        assertEquals(5, sector.getWidth());
        assertEquals(3, sector.getHeight());
        assertEquals(15, sector.size());
    }

    // ---- Accessors ----

    @Test
    @DisplayName("Should return correct ID")
    void testGetId() {
        Sector sector = new Sector(42);
        assertEquals(42, sector.getId());
    }

    @Test
    @DisplayName("Should return correct dimensions")
    void testGetDimensions() {
        Sector sector = new Sector(1, 8, 6);
        assertEquals(8, sector.getWidth());
        assertEquals(6, sector.getHeight());
    }

    @Test
    @DisplayName("Should return total hex count")
    void testSize() {
        Sector sector = new Sector(0);
        assertEquals(100, sector.size());
    }

    // ---- Hex operations ----

    @Test
    @DisplayName("Should get hex at valid coordinates")
    void testGetValidHex() {
        Sector sector = new Sector(0);
        Hex hex = sector.get(0, 0);
        assertNotNull(hex);
        assertEquals(0, hex.getQ());
        assertEquals(0, hex.getR());
    }

    @Test
    @DisplayName("Should get hex at corner coordinates")
    void testGetCornerHex() {
        Sector sector = new Sector(0);
        Hex hex = sector.get(9, 9);
        assertNotNull(hex);
        assertEquals(9, hex.getQ());
        assertEquals(9, hex.getR());
    }

    @Test
    @DisplayName("Should return null for out of bounds coordinates")
    void testGetOutOfBounds() {
        Sector sector = new Sector(0);
        assertNull(sector.get(10, 0));
        assertNull(sector.get(0, 10));
        assertNull(sector.get(-1, 0));
        assertNull(sector.get(0, -1));
    }

    @Test
    @DisplayName("Should contain valid coordinates")
    void testContains() {
        Sector sector = new Sector(0);
        assertTrue(sector.contains(0, 0));
        assertTrue(sector.contains(9, 9));
        assertTrue(sector.contains(5, 5));
        assertFalse(sector.contains(10, 0));
        assertFalse(sector.contains(0, 10));
        assertFalse(sector.contains(-1, 0));
    }

    @Test
    @DisplayName("Should return all hexes as unmodifiable collection")
    void testGetAll() {
        Sector sector = new Sector(0);
        Collection<Hex> all = sector.getAll();
        assertEquals(100, all.size());

        assertThrows(UnsupportedOperationException.class, () -> {
            all.clear();
        });
    }

    // ---- Query operations ----

    @Test
    @DisplayName("Should find hexes by state")
    void testFindByState() {
        Sector sector = new Sector(0);

        // Initially all hexes are EMPTY
        List<Hex> empty = sector.findByState(HexState.EMPTY);
        assertEquals(100, empty.size());

        // Place some hexes
        Hex h1 = sector.get(0, 0);
        Hex h2 = sector.get(1, 1);
        h1.place("unit1");
        h2.place("unit2");
        h2.select();

        assertEquals(1, sector.findByState(HexState.OCCUPIED).size());
        assertEquals(1, sector.findByState(HexState.SELECTED).size());
        assertEquals(98, sector.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should return empty list for state with no matches")
    void testFindByStateNoMatches() {
        Sector sector = new Sector(0);
        List<Hex> blocked = sector.findByState(HexState.BLOCKED);
        assertTrue(blocked.isEmpty());
    }

    @Test
    @DisplayName("Should get neighbors within sector bounds")
    void testGetNeighbors() {
        Sector sector = new Sector(0);

        // Center hex should have 6 neighbors
        Hex center = sector.get(5, 5);
        List<Hex> neighbors = sector.getNeighbors(center);
        assertEquals(6, neighbors.size());

        // Corner hex should have fewer neighbors within bounds
        Hex corner = sector.get(0, 0);
        List<Hex> cornerNeighbors = sector.getNeighbors(corner);
        assertTrue(cornerNeighbors.size() < 6, "Corner should have fewer than 6 neighbors in sector");
    }

    @Test
    @DisplayName("Should get neighbors for edge hex")
    void testGetNeighborsEdge() {
        Sector sector = new Sector(0);

        // Edge hex (not corner) should have fewer neighbors
        Hex edge = sector.get(0, 5);
        List<Hex> edgeNeighbors = sector.getNeighbors(edge);
        assertTrue(edgeNeighbors.size() < 6, "Edge should have fewer than 6 neighbors in sector");
        assertFalse(edgeNeighbors.isEmpty(), "Edge should have some neighbors");
    }

    // ---- Occupancy stats ----

    @Test
    @DisplayName("Should return zero occupied count for new sector")
    void testGetOccupiedCountEmpty() {
        Sector sector = new Sector(0);
        assertEquals(0, sector.getOccupiedCount());
    }

    @Test
    @DisplayName("Should return correct occupied count")
    void testGetOccupiedCount() {
        Sector sector = new Sector(0);

        sector.get(0, 0).place("unit1");
        sector.get(1, 0).place("unit2");
        sector.get(2, 0).place("unit3");

        assertEquals(3, sector.getOccupiedCount());
    }

    @Test
    @DisplayName("Should return correct empty count")
    void testGetEmptyCount() {
        Sector sector = new Sector(0);
        assertEquals(100, sector.getEmptyCount());

        sector.get(0, 0).place("unit");
        assertEquals(99, sector.getEmptyCount());
    }

    @Test
    @DisplayName("Should return zero occupancy ratio for empty sector")
    void testGetOccupancyRatioEmpty() {
        Sector sector = new Sector(0);
        assertEquals(0.0, sector.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should return correct occupancy ratio")
    void testGetOccupancyRatio() {
        Sector sector = new Sector(0);

        // Occupy 10 hexes out of 100
        for (int i = 0; i < 10; i++) {
            sector.get(i, 0).place("unit" + i);
        }

        assertEquals(0.1, sector.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should return 1.0 occupancy ratio when all hexes occupied")
    void testGetOccupancyRatioFull() {
        Sector sector = new Sector(0, 3, 3); // 9 hexes for simplicity

        for (int q = 0; q < 3; q++) {
            for (int r = 0; r < 3; r++) {
                sector.get(q, r).place("unit");
            }
        }

        assertEquals(1.0, sector.getOccupancyRatio(), 0.001);
    }

    // ---- Static utility methods ----

    @Test
    @DisplayName("Should convert global Q to local Q")
    void testToLocalQ() {
        assertEquals(0, Sector.toLocalQ(0));
        assertEquals(5, Sector.toLocalQ(5));
        assertEquals(9, Sector.toLocalQ(9));
        assertEquals(0, Sector.toLocalQ(10));
        assertEquals(3, Sector.toLocalQ(13));
    }

    @Test
    @DisplayName("Should convert global R to local R")
    void testToLocalR() {
        assertEquals(0, Sector.toLocalR(0));
        assertEquals(7, Sector.toLocalR(7));
        assertEquals(9, Sector.toLocalR(19));
        assertEquals(0, Sector.toLocalR(20));
    }

    @Test
    @DisplayName("Should compute sector ID from global coordinates")
    void testSectorId() {
        // Coordinates within first sector
        assertEquals(0, Sector.sectorId(0, 0));
        assertEquals(0, Sector.sectorId(5, 5));
        assertEquals(0, Sector.sectorId(9, 9));

        // Coordinates in next sector horizontally
        assertEquals(1, Sector.sectorId(10, 0));
        assertEquals(1, Sector.sectorId(15, 5));

        // Coordinates in next sector vertically
        assertEquals(100, Sector.sectorId(0, 10));
        assertEquals(100, Sector.sectorId(5, 15));

        // Coordinates in diagonal sector
        assertEquals(101, Sector.sectorId(10, 10));
    }

    @Test
    @DisplayName("Should handle negative global coordinates")
    void testSectorIdNegative() {
        assertEquals(-1, Sector.sectorId(-1, 0));
        assertEquals(-100, Sector.sectorId(0, -1));
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should support placing units across sector")
    void testIntegrationPlaceUnits() {
        Sector sector = new Sector(0);

        // Place units in a pattern
        for (int q = 0; q < 10; q++) {
            sector.get(q, 0).place("wall");
        }

        assertEquals(10, sector.getOccupiedCount());
        assertEquals(90, sector.getEmptyCount());
        assertEquals(0.1, sector.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should support mixed states in sector")
    void testIntegrationMixedStates() {
        Sector sector = new Sector(0);

        sector.get(0, 0).place("unit1");
        sector.get(1, 0).place("unit2");
        sector.get(1, 0).select();
        sector.get(2, 0).block();

        assertEquals(1, sector.findByState(HexState.OCCUPIED).size());
        assertEquals(1, sector.findByState(HexState.SELECTED).size());
        assertEquals(1, sector.findByState(HexState.BLOCKED).size());
        assertEquals(97, sector.findByState(HexState.EMPTY).size());
    }

    @Test
    @DisplayName("Should toString include sector info")
    void testToString() {
        Sector sector = new Sector(5);
        String str = sector.toString();
        assertTrue(str.contains("id=5"));
        assertTrue(str.contains("size=10x10"));
        assertTrue(str.contains("hexes=100"));
    }

    @Test
    @DisplayName("Should create multiple independent sectors")
    void testMultipleSectors() {
        Sector s1 = new Sector(0);
        Sector s2 = new Sector(1);

        s1.get(0, 0).place("unit1");
        s2.get(0, 0).place("unit2");

        assertEquals(1, s1.getOccupiedCount());
        assertEquals(1, s2.getOccupiedCount());

        // Modifying s1 shouldn't affect s2
        s1.get(1, 0).place("unit3");
        assertEquals(2, s1.getOccupiedCount());
        assertEquals(1, s2.getOccupiedCount());
    }

    // ---- Edge cases ----

    @Test
    @DisplayName("Should create sector with negative ID")
    void testNegativeId() {
        Sector sector = new Sector(-1);
        assertEquals(-1, sector.getId());
        assertEquals(100, sector.size());
    }

    @Test
    @DisplayName("Should create single hex sector (1x1)")
    void testSingleHexSector() {
        Sector sector = new Sector(0, 1, 1);
        assertEquals(1, sector.size());
        assertEquals(1, sector.getWidth());
        assertEquals(1, sector.getHeight());

        Hex hex = sector.get(0, 0);
        assertNotNull(hex);
        assertNull(sector.get(1, 0));
        assertNull(sector.get(0, 1));
    }

    @Test
    @DisplayName("Should create single row sector")
    void testSingleRowSector() {
        Sector sector = new Sector(0, 5, 1);
        assertEquals(5, sector.size());
        assertEquals(5, sector.getWidth());
        assertEquals(1, sector.getHeight());

        for (int q = 0; q < 5; q++) {
            assertNotNull(sector.get(q, 0));
        }
        assertNull(sector.get(0, 1));
    }

    @Test
    @DisplayName("Should create single column sector")
    void testSingleColumnSector() {
        Sector sector = new Sector(0, 1, 5);
        assertEquals(5, sector.size());
        assertEquals(1, sector.getWidth());
        assertEquals(5, sector.getHeight());

        for (int r = 0; r < 5; r++) {
            assertNotNull(sector.get(0, r));
        }
        assertNull(sector.get(1, 0));
    }

    @Test
    @DisplayName("Should return empty neighbors for hex not in sector")
    void testGetNeighborsNotInSector() {
        Sector sector = new Sector(0);
        // A hex outside the sector bounds
        Hex outsideHex = new Hex(99, 99);
        List<Hex> neighbors = sector.getNeighbors(outsideHex);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    @DisplayName("Should return empty neighbors for isolated hex in small sector")
    void testGetNeighborsIsolated() {
        Sector sector = new Sector(0, 1, 1);
        Hex hex = sector.get(0, 0);
        List<Hex> neighbors = sector.getNeighbors(hex);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    @DisplayName("Should count blocked hexes separately from occupied")
    void testOccupiedCountExcludesBlocked() {
        Sector sector = new Sector(0, 3, 3);

        sector.get(0, 0).place("unit");
        sector.get(1, 0).block();

        assertEquals(1, sector.getOccupiedCount());
        assertEquals(1, sector.findByState(HexState.BLOCKED).size());
        assertEquals(7, sector.getEmptyCount());
    }

    @Test
    @DisplayName("Should count selected hexes separately from occupied")
    void testOccupiedCountExcludesSelected() {
        Sector sector = new Sector(0, 3, 3);

        sector.get(0, 0).place("unit");
        sector.get(0, 0).select();
        sector.get(1, 0).place("unit2");

        assertEquals(1, sector.getOccupiedCount());
        assertEquals(1, sector.findByState(HexState.SELECTED).size());
        assertEquals(7, sector.getEmptyCount());
    }

    @Test
    @DisplayName("Should handle occupancy with mixed states")
    void testOccupancyMixedStates() {
        Sector sector = new Sector(0, 3, 3); // 9 hexes

        sector.get(0, 0).place("unit1");
        sector.get(1, 0).place("unit2");
        sector.get(1, 0).select();
        sector.get(2, 0).block();

        // 1 OCCUPIED + 1 SELECTED + 1 BLOCKED + 6 EMPTY = 9
        assertEquals(1, sector.getOccupiedCount());
        assertEquals(6, sector.getEmptyCount());
        assertEquals(1.0 / 9.0, sector.getOccupancyRatio(), 0.001);
    }

    @Test
    @DisplayName("Should convert negative global Q to local Q")
    void testToLocalQNegative() {
        // Java modulo can return negative for negative operands
        int local = Sector.toLocalQ(-1);
        assertTrue(local >= -9 && local <= 9, "Local Q should be within reasonable range");
    }

    @Test
    @DisplayName("Should convert negative global R to local R")
    void testToLocalRNegative() {
        int local = Sector.toLocalR(-1);
        assertTrue(local >= -9 && local <= 9, "Local R should be within reasonable range");
    }

    @Test
    @DisplayName("Should compute sector ID for large coordinates")
    void testSectorIdLarge() {
        assertEquals(2, Sector.sectorId(20, 0));
        assertEquals(200, Sector.sectorId(0, 20));
        assertEquals(202, Sector.sectorId(20, 20));
    }

    @Test
    @DisplayName("Should compute sector ID for negative diagonal coordinates")
    void testSectorIdNegativeDiagonal() {
        assertEquals(-101, Sector.sectorId(-10, -10));
    }

    @Test
    @DisplayName("Should verify neighbor bidirectional relationship")
    void testNeighborsBidirectional() {
        Sector sector = new Sector(0);
        Hex center = sector.get(5, 5);
        List<Hex> neighbors = sector.getNeighbors(center);

        // Each neighbor should have center as its neighbor
        for (Hex neighbor : neighbors) {
            List<Hex> reverseNeighbors = sector.getNeighbors(neighbor);
            assertTrue(reverseNeighbors.contains(center),
                    "Neighbor " + neighbor + " should have center as neighbor");
        }
    }

    @Test
    @DisplayName("Should verify all hexes in getAll are within bounds")
    void testGetAllHexesWithinBounds() {
        Sector sector = new Sector(0, 5, 5);
        Collection<Hex> all = sector.getAll();

        for (Hex hex : all) {
            assertTrue(hex.getQ() >= 0 && hex.getQ() < 5, "Q should be within bounds");
            assertTrue(hex.getR() >= 0 && hex.getR() < 5, "R should be within bounds");
        }
    }

    @Test
    @DisplayName("Should have all hexes initially empty")
    void testAllHexesInitiallyEmpty() {
        Sector sector = new Sector(0, 3, 3);
        Collection<Hex> all = sector.getAll();

        for (Hex hex : all) {
            assertEquals(HexState.EMPTY, hex.getState());
        }
    }

    @Test
    @DisplayName("Should support placing and removing units")
    void testPlaceAndRemoveCycle() {
        Sector sector = new Sector(0, 3, 3);

        sector.get(0, 0).place("unit");
        assertEquals(1, sector.getOccupiedCount());

        sector.get(0, 0).remove();
        assertEquals(0, sector.getOccupiedCount());
        assertEquals(9, sector.getEmptyCount());

        // Re-place after remove
        sector.get(0, 0).place("anotherUnit");
        assertEquals(1, sector.getOccupiedCount());
    }

    @Test
    @DisplayName("Should support blocking and unblocking hexes")
    void testBlockAndUnblockCycle() {
        Sector sector = new Sector(0, 3, 3);

        sector.get(0, 0).block();
        assertEquals(1, sector.findByState(HexState.BLOCKED).size());

        sector.get(0, 0).unblock();
        assertEquals(0, sector.findByState(HexState.BLOCKED).size());
        assertEquals(9, sector.getEmptyCount());
    }

    @Test
    @DisplayName("Should support select and deselect cycle")
    void testSelectAndDeselectCycle() {
        Sector sector = new Sector(0, 3, 3);

        sector.get(0, 0).place("unit");
        sector.get(0, 0).select();
        assertEquals(1, sector.findByState(HexState.SELECTED).size());

        sector.get(0, 0).deselect();
        assertEquals(0, sector.findByState(HexState.SELECTED).size());
        assertEquals(1, sector.getOccupiedCount());
    }

    @Test
    @DisplayName("Should toString include custom dimensions")
    void testToStringCustomSize() {
        Sector sector = new Sector(7, 5, 3);
        String str = sector.toString();
        assertTrue(str.contains("id=7"));
        assertTrue(str.contains("size=5x3"));
        assertTrue(str.contains("hexes=15"));
    }

    @Test
    @DisplayName("Should handle large sector ID")
    void testLargeSectorId() {
        Sector sector = new Sector(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, sector.getId());
        assertEquals(100, sector.size());
    }

    @Test
    @DisplayName("Should find neighbors at corner has correct count")
    void testCornerNeighborCount() {
        Sector sector = new Sector(0);
        Hex corner = sector.get(0, 0);
        List<Hex> neighbors = sector.getNeighbors(corner);
        // In a 10x10 sector, corner (0,0) should have 2-3 neighbors depending on direction offsets
        assertFalse(neighbors.isEmpty(), "Corner should have at least 1 neighbor");
        assertTrue(neighbors.size() <= 3, "Corner should have at most 3 neighbors in sector");
    }

    @Test
    @DisplayName("Should find neighbors at opposite corner")
    void testOppositeCornerNeighbors() {
        Sector sector = new Sector(0);
        Hex corner = sector.get(9, 9);
        List<Hex> neighbors = sector.getNeighbors(corner);
        assertFalse(neighbors.isEmpty(), "Opposite corner should have at least 1 neighbor");
        assertTrue(neighbors.size() <= 3, "Opposite corner should have at most 3 neighbors in sector");
    }

    @Test
    @DisplayName("Should verify sector constants")
    void testConstants() {
        assertEquals(10, Sector.DEFAULT_WIDTH);
        assertEquals(10, Sector.DEFAULT_HEIGHT);
    }
}
