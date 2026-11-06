package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipType Cubic Meters Tests")
class ShipTypeCubicMetersTest {

    @Test
    @DisplayName("Should have correct cubic meters for all ship types")
    void testCubicMeters() {
        assertEquals(1000, ShipType.FRIGATE.getCubicMeters());
        assertEquals(2000, ShipType.DESTROYER.getCubicMeters());
        assertEquals(8000, ShipType.CRUISER.getCubicMeters());
        assertEquals(27000, ShipType.BATTLESHIP.getCubicMeters());
        assertEquals(12000, ShipType.CARRIER.getCubicMeters());
        assertEquals(64000, ShipType.MOTHERSHIP.getCubicMeters());
    }

    @Test
    @DisplayName("Should have correct hull module count for all ship types")
    void testHullModuleCount() {
        assertEquals(1, ShipType.FRIGATE.getHullModuleCount());
        assertEquals(2, ShipType.DESTROYER.getHullModuleCount());
        assertEquals(8, ShipType.CRUISER.getHullModuleCount());
        assertEquals(27, ShipType.BATTLESHIP.getHullModuleCount());
        assertEquals(12, ShipType.CARRIER.getHullModuleCount());
        assertEquals(64, ShipType.MOTHERSHIP.getHullModuleCount());
    }

    @Test
    @DisplayName("Should return correct module grid dimensions for FRIGATE (1 module)")
    void testModuleGridDimensions_Frigate() {
        int[] dims = ShipType.FRIGATE.getModuleGridDimensions();
        assertArrayEquals(new int[]{1, 1, 1}, dims);
    }

    @Test
    @DisplayName("Should return correct module grid dimensions for DESTROYER (2 modules)")
    void testModuleGridDimensions_Destroyer() {
        int[] dims = ShipType.DESTROYER.getModuleGridDimensions();
        assertEquals(2, dims[0] * dims[1] * dims[2]);
    }

    @Test
    @DisplayName("Should return correct module grid dimensions for CRUISER (8 modules)")
    void testModuleGridDimensions_Cruiser() {
        int[] dims = ShipType.CRUISER.getModuleGridDimensions();
        assertArrayEquals(new int[]{2, 2, 2}, dims);
        assertEquals(8, dims[0] * dims[1] * dims[2]);
    }

    @Test
    @DisplayName("Should return correct module grid dimensions for BATTLESHIP (27 modules)")
    void testModuleGridDimensions_Battleship() {
        int[] dims = ShipType.BATTLESHIP.getModuleGridDimensions();
        assertArrayEquals(new int[]{3, 3, 3}, dims);
        assertEquals(27, dims[0] * dims[1] * dims[2]);
    }

    @Test
    @DisplayName("Should return correct module grid dimensions for CARRIER (12 modules)")
    void testModuleGridDimensions_Carrier() {
        int[] dims = ShipType.CARRIER.getModuleGridDimensions();
        assertEquals(12, dims[0] * dims[1] * dims[2]);
    }

    @Test
    @DisplayName("Should return correct module grid dimensions for MOTHERSHIP (64 modules)")
    void testModuleGridDimensions_Mothership() {
        int[] dims = ShipType.MOTHERSHIP.getModuleGridDimensions();
        assertArrayEquals(new int[]{4, 4, 4}, dims);
        assertEquals(64, dims[0] * dims[1] * dims[2]);
    }

    @Test
    @DisplayName("Should have module grid product equal to module count")
    void testModuleGridProductEqualsCount() {
        for (ShipType type : ShipType.values()) {
            int[] dims = type.getModuleGridDimensions();
            int product = dims[0] * dims[1] * dims[2];
            assertEquals(type.getHullModuleCount(), product,
                    "Grid product should match module count for " + type);
        }
    }

    @Test
    @DisplayName("Should have compact grid dimensions (no dimension is 0)")
    void testModuleGridNoZeroDimensions() {
        for (ShipType type : ShipType.values()) {
            int[] dims = type.getModuleGridDimensions();
            assertTrue(dims[0] > 0, "X dimension should be > 0 for " + type);
            assertTrue(dims[1] > 0, "Y dimension should be > 0 for " + type);
            assertTrue(dims[2] > 0, "Z dimension should be > 0 for " + type);
        }
    }

    @Test
    @DisplayName("Should have larger ships with more cubic meters")
    void testCubicMetersOrdering() {
        assertTrue(ShipType.FRIGATE.getCubicMeters() < ShipType.DESTROYER.getCubicMeters());
        assertTrue(ShipType.DESTROYER.getCubicMeters() < ShipType.CRUISER.getCubicMeters());
        assertTrue(ShipType.CRUISER.getCubicMeters() < ShipType.BATTLESHIP.getCubicMeters());
        assertTrue(ShipType.BATTLESHIP.getCubicMeters() < ShipType.MOTHERSHIP.getCubicMeters());
    }

    @Test
    @DisplayName("Should have all 6 ship types")
    void testShipTypeCount() {
        assertEquals(6, ShipType.values().length);
    }
}
