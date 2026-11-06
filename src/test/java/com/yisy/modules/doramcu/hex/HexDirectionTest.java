package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the HexDirection enum.
 */
class HexDirectionTest {

    @Test
    @DisplayName("Should have 6 directions")
    void testDirectionCount() {
        assertEquals(6, HexDirection.values().length);
    }

    @Test
    @DisplayName("Should return correct offsets for EAST")
    void testEastOffsets() {
        assertEquals(1, HexDirection.EAST.getDq());
        assertEquals(0, HexDirection.EAST.getDr());
    }

    @Test
    @DisplayName("Should return correct offsets for WEST")
    void testWestOffsets() {
        assertEquals(-1, HexDirection.WEST.getDq());
        assertEquals(0, HexDirection.WEST.getDr());
    }

    @Test
    @DisplayName("Should return correct offsets for NORTH_EAST")
    void testNorthEastOffsets() {
        assertEquals(1, HexDirection.NORTH_EAST.getDq());
        assertEquals(-1, HexDirection.NORTH_EAST.getDr());
    }

    @Test
    @DisplayName("Should return correct offsets for SOUTH_WEST")
    void testSouthWestOffsets() {
        assertEquals(-1, HexDirection.SOUTH_WEST.getDq());
        assertEquals(1, HexDirection.SOUTH_WEST.getDr());
    }

    @Test
    @DisplayName("Should return correct offsets for NORTH_WEST")
    void testNorthWestOffsets() {
        assertEquals(0, HexDirection.NORTH_WEST.getDq());
        assertEquals(-1, HexDirection.NORTH_WEST.getDr());
    }

    @Test
    @DisplayName("Should return correct offsets for SOUTH_EAST")
    void testSouthEastOffsets() {
        assertEquals(0, HexDirection.SOUTH_EAST.getDq());
        assertEquals(1, HexDirection.SOUTH_EAST.getDr());
    }

    @Test
    @DisplayName("Should return opposite direction for EAST")
    void testOppositeEast() {
        assertEquals(HexDirection.WEST, HexDirection.EAST.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for WEST")
    void testOppositeWest() {
        assertEquals(HexDirection.EAST, HexDirection.WEST.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for NORTH_EAST")
    void testOppositeNorthEast() {
        assertEquals(HexDirection.SOUTH_WEST, HexDirection.NORTH_EAST.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for SOUTH_WEST")
    void testOppositeSouthWest() {
        assertEquals(HexDirection.NORTH_EAST, HexDirection.SOUTH_WEST.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for NORTH_WEST")
    void testOppositeNorthWest() {
        assertEquals(HexDirection.SOUTH_EAST, HexDirection.NORTH_WEST.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for SOUTH_EAST")
    void testOppositeSouthEast() {
        assertEquals(HexDirection.NORTH_WEST, HexDirection.SOUTH_EAST.opposite());
    }

    @Test
    @DisplayName("Should be symmetric: opposite of opposite is self")
    void testOppositeSymmetric() {
        for (HexDirection dir : HexDirection.values()) {
            assertEquals(dir, dir.opposite().opposite(),
                    "Opposite of opposite of " + dir + " should be " + dir);
        }
    }

    @Test
    @DisplayName("Should have opposite offsets for opposite directions")
    void testOppositeOffsets() {
        for (HexDirection dir : HexDirection.values()) {
            HexDirection opposite = dir.opposite();
            assertEquals(-dir.getDq(), opposite.getDq(),
                    "q offset of " + opposite + " should be negative of " + dir);
            assertEquals(-dir.getDr(), opposite.getDr(),
                    "r offset of " + opposite + " should be negative of " + dir);
        }
    }

    @Test
    @DisplayName("Should use offsets correctly in neighbor calculation")
    void testOffsetsInNeighbor() {
        Hex center = new Hex(0, 0);

        for (HexDirection dir : HexDirection.values()) {
            Hex neighbor = center.neighbor(dir);
            assertEquals(dir.getDq(), neighbor.getQ());
            assertEquals(dir.getDr(), neighbor.getR());
        }
    }
}
