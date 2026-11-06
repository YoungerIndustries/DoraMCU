package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Hex3DDirection enum.
 */
class Hex3DDirectionTest {

    @Test
    @DisplayName("Should have 6 directions")
    void testDirectionCount() {
        assertEquals(6, Hex3DDirection.values().length);
    }

    @Test
    @DisplayName("Should return correct offsets for POS_X_NEG_Y")
    void testPosXNegYOffsets() {
        assertEquals(1, Hex3DDirection.POS_X_NEG_Y.getDx());
        assertEquals(-1, Hex3DDirection.POS_X_NEG_Y.getDy());
        assertEquals(0, Hex3DDirection.POS_X_NEG_Y.getDz());
    }

    @Test
    @DisplayName("Should return correct offsets for POS_X_NEG_Z")
    void testPosXNegZOffsets() {
        assertEquals(1, Hex3DDirection.POS_X_NEG_Z.getDx());
        assertEquals(0, Hex3DDirection.POS_X_NEG_Z.getDy());
        assertEquals(-1, Hex3DDirection.POS_X_NEG_Z.getDz());
    }

    @Test
    @DisplayName("Should return correct offsets for NEG_X_POS_Y")
    void testNegXPosYOffsets() {
        assertEquals(-1, Hex3DDirection.NEG_X_POS_Y.getDx());
        assertEquals(1, Hex3DDirection.NEG_X_POS_Y.getDy());
        assertEquals(0, Hex3DDirection.NEG_X_POS_Y.getDz());
    }

    @Test
    @DisplayName("Should return correct offsets for NEG_X_POS_Z")
    void testNegXPosZOffsets() {
        assertEquals(-1, Hex3DDirection.NEG_X_POS_Z.getDx());
        assertEquals(0, Hex3DDirection.NEG_X_POS_Z.getDy());
        assertEquals(1, Hex3DDirection.NEG_X_POS_Z.getDz());
    }

    @Test
    @DisplayName("Should return correct offsets for POS_Y_NEG_Z")
    void testPosYNegZOffsets() {
        assertEquals(0, Hex3DDirection.POS_Y_NEG_Z.getDx());
        assertEquals(1, Hex3DDirection.POS_Y_NEG_Z.getDy());
        assertEquals(-1, Hex3DDirection.POS_Y_NEG_Z.getDz());
    }

    @Test
    @DisplayName("Should return correct offsets for NEG_Y_POS_Z")
    void testNegYPosZOffsets() {
        assertEquals(0, Hex3DDirection.NEG_Y_POS_Z.getDx());
        assertEquals(-1, Hex3DDirection.NEG_Y_POS_Z.getDy());
        assertEquals(1, Hex3DDirection.NEG_Y_POS_Z.getDz());
    }

    @Test
    @DisplayName("Should return opposite direction for POS_X_NEG_Y")
    void testOppositePosXNegY() {
        assertEquals(Hex3DDirection.NEG_X_POS_Y, Hex3DDirection.POS_X_NEG_Y.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for POS_X_NEG_Z")
    void testOppositePosXNegZ() {
        assertEquals(Hex3DDirection.NEG_X_POS_Z, Hex3DDirection.POS_X_NEG_Z.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for POS_Y_NEG_Z")
    void testOppositePosYNegZ() {
        assertEquals(Hex3DDirection.NEG_Y_POS_Z, Hex3DDirection.POS_Y_NEG_Z.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for NEG_X_POS_Y")
    void testOppositeNegXPosY() {
        assertEquals(Hex3DDirection.POS_X_NEG_Y, Hex3DDirection.NEG_X_POS_Y.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for NEG_X_POS_Z")
    void testOppositeNegXPosZ() {
        assertEquals(Hex3DDirection.POS_X_NEG_Z, Hex3DDirection.NEG_X_POS_Z.opposite());
    }

    @Test
    @DisplayName("Should return opposite direction for NEG_Y_POS_Z")
    void testOppositeNegYPosZ() {
        assertEquals(Hex3DDirection.POS_Y_NEG_Z, Hex3DDirection.NEG_Y_POS_Z.opposite());
    }

    @Test
    @DisplayName("Should be symmetric: opposite of opposite is self")
    void testOppositeSymmetric() {
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            assertEquals(dir, dir.opposite().opposite(),
                    "Opposite of opposite of " + dir + " should be " + dir);
        }
    }

    @Test
    @DisplayName("Should have opposite offsets for opposite directions")
    void testOppositeOffsets() {
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            Hex3DDirection opposite = dir.opposite();
            assertEquals(-dir.getDx(), opposite.getDx(),
                    "x offset of " + opposite + " should be negative of " + dir);
            assertEquals(-dir.getDy(), opposite.getDy(),
                    "y offset of " + opposite + " should be negative of " + dir);
            assertEquals(-dir.getDz(), opposite.getDz(),
                    "z offset of " + opposite + " should be negative of " + dir);
        }
    }

    @Test
    @DisplayName("Should have offsets summing to 0")
    void testOffsetsSumZero() {
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            assertEquals(0, dir.getDx() + dir.getDy() + dir.getDz(),
                    "Offsets of " + dir + " should sum to 0");
        }
    }

    @Test
    @DisplayName("Should use offsets correctly in neighbor calculation")
    void testOffsetsInNeighbor() {
        Hex3D center = new Hex3D(0, 0, 0);

        for (Hex3DDirection dir : Hex3DDirection.values()) {
            Hex3D neighbor = center.neighbor(dir);
            assertEquals(dir.getDx(), neighbor.getX());
            assertEquals(dir.getDy(), neighbor.getY());
            assertEquals(dir.getDz(), neighbor.getZ());
        }
    }
}
