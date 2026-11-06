package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for interior/exterior cell determination in HullShape.
 */
@DisplayName("HullShape Interior/Exterior Tests")
class HullShapeInteriorExteriorTest {

    // ---- CUBE interior/exterior ----

    @Test
    @DisplayName("CUBE - corners are exterior")
    void cube_cornersAreExterior() {
        assertTrue(HullShape.CUBE.isExterior(0, 0, 0));
        assertTrue(HullShape.CUBE.isExterior(9, 0, 0));
        assertTrue(HullShape.CUBE.isExterior(0, 9, 0));
        assertTrue(HullShape.CUBE.isExterior(0, 0, 9));
        assertTrue(HullShape.CUBE.isExterior(9, 9, 9));
    }

    @Test
    @DisplayName("CUBE - center cells are interior")
    void cube_centerCellsAreInterior() {
        assertTrue(HullShape.CUBE.isInterior(1, 1, 1));
        assertTrue(HullShape.CUBE.isInterior(4, 4, 4));
        assertTrue(HullShape.CUBE.isInterior(8, 8, 8));
    }

    @Test
    @DisplayName("CUBE - boundary cells are exterior")
    void cube_boundaryCellsAreExterior() {
        assertTrue(HullShape.CUBE.isExterior(0, 5, 5));
        assertTrue(HullShape.CUBE.isExterior(9, 5, 5));
        assertTrue(HullShape.CUBE.isExterior(5, 0, 5));
        assertTrue(HullShape.CUBE.isExterior(5, 9, 5));
        assertTrue(HullShape.CUBE.isExterior(5, 5, 0));
        assertTrue(HullShape.CUBE.isExterior(5, 5, 9));
    }

    @Test
    @DisplayName("CUBE - cells just inside boundary are interior")
    void cube_justInsideBoundaryAreInterior() {
        assertTrue(HullShape.CUBE.isInterior(1, 5, 5));
        assertTrue(HullShape.CUBE.isInterior(8, 5, 5));
        assertTrue(HullShape.CUBE.isInterior(5, 1, 5));
        assertTrue(HullShape.CUBE.isInterior(5, 8, 5));
        assertTrue(HullShape.CUBE.isInterior(5, 5, 1));
        assertTrue(HullShape.CUBE.isInterior(5, 5, 8));
    }

    @Test
    @DisplayName("CUBE - out of bounds is not exterior or interior")
    void cube_outOfBounds() {
        assertFalse(HullShape.CUBE.isExterior(-1, 0, 0));
        assertFalse(HullShape.CUBE.isInterior(-1, 0, 0));
        assertFalse(HullShape.CUBE.isExterior(10, 0, 0));
        assertFalse(HullShape.CUBE.isInterior(10, 0, 0));
    }

    @Test
    @DisplayName("CUBE - exterior + interior counts")
    void cube_exteriorInteriorCounts() {
        int exterior = 0;
        int interior = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.CUBE.isExterior(x, y, z)) exterior++;
                    if (HullShape.CUBE.isInterior(x, y, z)) interior++;
                }
            }
        }
        // Cube: 1000 total, interior is 8x8x8 = 512, exterior = 1000 - 512 = 488
        assertEquals(488, exterior, "CUBE exterior count");
        assertEquals(512, interior, "CUBE interior count");
        assertEquals(1000, exterior + interior, "CUBE exterior + interior = total");
    }

    // ---- WEDGE interior/exterior ----

    @Test
    @DisplayName("WEDGE - base layer corners are exterior")
    void wedge_baseCornersAreExterior() {
        assertTrue(HullShape.WEDGE.isExterior(0, 0, 0));
        assertTrue(HullShape.WEDGE.isExterior(9, 0, 0));
        assertTrue(HullShape.WEDGE.isExterior(0, 9, 0));
        assertTrue(HullShape.WEDGE.isExterior(9, 9, 0));
    }

    @Test
    @DisplayName("WEDGE - tip cell (0,5,9) is exterior")
    void wedge_tipIsExterior() {
        // At Z=9, only X=0 is in the shape
        assertTrue(HullShape.WEDGE.isExterior(0, 5, 9));
        assertFalse(HullShape.WEDGE.isInterior(0, 5, 9));
    }

    @Test
    @DisplayName("WEDGE - cells not in shape are neither exterior nor interior")
    void wedge_notInShape() {
        // At Z=9, X=5 is not in the wedge
        assertFalse(HullShape.WEDGE.isExterior(5, 5, 9));
        assertFalse(HullShape.WEDGE.isInterior(5, 5, 9));
    }

    @Test
    @DisplayName("WEDGE - interior cells exist in lower layers")
    void wedge_interiorExists() {
        // At Z=0, z-1=-1 is out of bounds → all Z=0 cells are exterior
        // Interior cells start at Z=1 where all 6 neighbors can be in the shape
        assertTrue(HullShape.WEDGE.isInterior(1, 1, 1));
        assertTrue(HullShape.WEDGE.isInterior(5, 5, 1));
    }

    @Test
    @DisplayName("WEDGE - exterior cells on the slanted face")
    void wedge_slantedFaceExterior() {
        // At Z=5, max X is 4. X=4 is on the slanted face → exterior (x+1=5 is not in shape).
        assertTrue(HullShape.WEDGE.isExterior(4, 5, 5));
        // X=3 at Z=5: all 6 face neighbors are in the shape → interior.
        // x-1=2 (in, 2<=4), x+1=4 (in, 4<=4)
        // y-1=4 (in), y+1=6 (in)
        // z-1=4: x<=5, so 3 is in shape at z=4
        // z+1=6: x<=3, so 3 is in shape at z=6
        assertTrue(HullShape.WEDGE.isInterior(3, 5, 5));
    }

    @Test
    @DisplayName("WEDGE - exterior + interior = total shape cells")
    void wedge_exteriorPlusInteriorEqualsTotal() {
        int exterior = 0;
        int interior = 0;
        int total = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.WEDGE.contains(x, y, z)) {
                        total++;
                        if (HullShape.WEDGE.isExterior(x, y, z)) exterior++;
                        if (HullShape.WEDGE.isInterior(x, y, z)) interior++;
                    }
                }
            }
        }
        assertEquals(550, total, "WEDGE total shape cells");
        assertEquals(total, exterior + interior, "WEDGE exterior + interior = total");
    }

    // ---- DOME interior/exterior ----

    @Test
    @DisplayName("DOME - center of base is interior")
    void dome_centerBaseIsInterior() {
        // (4,4,0): z-1=-1 is out of bounds → not in shape → exterior
        // Actually z=0, so z-1=-1 is not contained → exterior
        assertTrue(HullShape.DOME.isExterior(4, 4, 0));
        // (4,4,1): all neighbors should be in the dome
        assertTrue(HullShape.DOME.isInterior(4, 4, 1));
        assertTrue(HullShape.DOME.isInterior(5, 5, 1));
    }

    @Test
    @DisplayName("DOME - edge of dome surface is exterior")
    void dome_surfaceIsExterior() {
        // Cells near the dome's curved boundary are exterior
        // (0,0,0): distance from center = sqrt(40.5) ≈ 6.36 < 9 → in shape
        // But x-1=-1 is out → exterior
        assertTrue(HullShape.DOME.isExterior(0, 0, 0));
    }

    @Test
    @DisplayName("DOME - cells not in shape are neither exterior nor interior")
    void dome_notInShape() {
        assertFalse(HullShape.DOME.isExterior(5, 5, 9));
        assertFalse(HullShape.DOME.isInterior(5, 5, 9));
    }

    @Test
    @DisplayName("DOME - exterior + interior = total shape cells")
    void dome_exteriorPlusInteriorEqualsTotal() {
        int exterior = 0;
        int interior = 0;
        int total = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.DOME.contains(x, y, z)) {
                        total++;
                        if (HullShape.DOME.isExterior(x, y, z)) exterior++;
                        if (HullShape.DOME.isInterior(x, y, z)) interior++;
                    }
                }
            }
        }
        assertTrue(total > 0, "DOME has cells");
        assertEquals(total, exterior + interior, "DOME exterior + interior = total");
    }

    // ---- PYRAMID interior/exterior ----

    @Test
    @DisplayName("PYRAMID - base center is exterior (z=0, z-1 out of bounds)")
    void pyramid_baseCenterIsExterior() {
        // At Z=0, z-1=-1 is not in shape → all base cells are exterior
        assertTrue(HullShape.PYRAMID.isExterior(4, 4, 0));
        assertTrue(HullShape.PYRAMID.isExterior(5, 5, 0));
    }

    @Test
    @DisplayName("PYRAMID - interior cells exist at mid levels")
    void pyramid_interiorAtMidLevels() {
        // At Z=1, halfSize = 8*4.5/9 = 4.0, range [0.5, 8.5]
        // (1,1,1): x-1=0 → 0 >= 0.5? No → not in shape at Z=1
        // Let's check (4,4,1): halfSize=4.0, range [0.5, 8.5]
        // x-1=3: 3 >= 0.5 && 3 <= 8.5 → yes
        // x+1=5: 5 >= 0.5 && 5 <= 8.5 → yes
        // z-1=0: full 10x10 → yes
        // z+1=2: halfSize = 7*4.5/9 = 3.5, range [1.0, 8.0]
        // 4 >= 1.0 && 4 <= 8.0 → yes
        assertTrue(HullShape.PYRAMID.isInterior(4, 4, 1));
    }

    @Test
    @DisplayName("PYRAMID - exterior + interior = total shape cells")
    void pyramid_exteriorPlusInteriorEqualsTotal() {
        int exterior = 0;
        int interior = 0;
        int total = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.PYRAMID.contains(x, y, z)) {
                        total++;
                        if (HullShape.PYRAMID.isExterior(x, y, z)) exterior++;
                        if (HullShape.PYRAMID.isInterior(x, y, z)) interior++;
                    }
                }
            }
        }
        assertTrue(total > 0, "PYRAMID has cells");
        assertEquals(total, exterior + interior, "PYRAMID exterior + interior = total");
    }

    // ---- CONE interior/exterior ----

    @Test
    @DisplayName("CONE - base center is exterior (z=0, z-1 out of bounds)")
    void cone_baseCenterIsExterior() {
        assertTrue(HullShape.CONE.isExterior(4, 4, 0));
    }

    @Test
    @DisplayName("CONE - interior cells exist at mid levels")
    void cone_interiorAtMidLevels() {
        // At Z=1, radius = 8*4.5/9 = 4.0
        // (4,4,1): distance from center = sqrt(0.25+0.25) = sqrt(0.5) ≈ 0.707 < 4.0
        // Neighbors: (3,4,1), (5,4,1), (4,3,1), (4,5,1) — all within radius
        // z-1=0: radius=4.5, (4,4,0) is in
        // z+1=2: radius=3.5, (4,4,2) is in
        assertTrue(HullShape.CONE.isInterior(4, 4, 1));
    }

    @Test
    @DisplayName("CONE - exterior + interior = total shape cells")
    void cone_exteriorPlusInteriorEqualsTotal() {
        int exterior = 0;
        int interior = 0;
        int total = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.CONE.contains(x, y, z)) {
                        total++;
                        if (HullShape.CONE.isExterior(x, y, z)) exterior++;
                        if (HullShape.CONE.isInterior(x, y, z)) interior++;
                    }
                }
            }
        }
        assertTrue(total > 0, "CONE has cells");
        assertEquals(total, exterior + interior, "CONE exterior + interior = total");
    }

    // ---- Cross-shape: every shape cell is exactly exterior or interior ----

    @Test
    @DisplayName("All shapes - every shape cell is exactly exterior or interior, never both")
    void allShapes_exactlyOneCategory() {
        for (HullShape shape : HullShape.values()) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    for (int z = 0; z < 10; z++) {
                        if (shape.contains(x, y, z)) {
                            boolean isExt = shape.isExterior(x, y, z);
                            boolean isInt = shape.isInterior(x, y, z);
                            assertNotEquals(isExt, isInt,
                                    shape + " at (" + x + "," + y + "," + z +
                                    ") should not be both exterior and interior");
                            assertTrue(isExt || isInt,
                                    shape + " at (" + x + "," + y + "," + z +
                                    ") should be either exterior or interior");
                        } else {
                            assertFalse(shape.isExterior(x, y, z),
                                    shape + " at (" + x + "," + y + "," + z +
                                    ") is not in shape, should not be exterior");
                            assertFalse(shape.isInterior(x, y, z),
                                    shape + " at (" + x + "," + y + "," + z +
                                    ") is not in shape, should not be interior");
                        }
                    }
                }
            }
        }
    }

    // ---- Mutual exclusivity ----

    @Test
    @DisplayName("All shapes - exterior and interior are mutually exclusive")
    void allShapes_mutuallyExclusive() {
        for (HullShape shape : HullShape.values()) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    for (int z = 0; z < 10; z++) {
                        assertFalse(shape.isExterior(x, y, z) && shape.isInterior(x, y, z),
                                shape + " at (" + x + "," + y + "," + z +
                                ") cannot be both exterior and interior");
                    }
                }
            }
        }
    }
}
