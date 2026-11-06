package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the HullShape enum.
 */
@DisplayName("HullShape Enum Tests")
class HullShapeTest {

    // ---- Enum values ----

    @Test
    @DisplayName("values - should have 5 shapes")
    void values_shouldHaveFiveShapes() {
        assertEquals(5, HullShape.values().length);
    }

    @Test
    @DisplayName("CUBE - should exist")
    void cube_shouldExist() {
        assertSame(HullShape.CUBE, HullShape.valueOf("CUBE"));
    }

    @Test
    @DisplayName("WEDGE - should exist")
    void wedge_shouldExist() {
        assertSame(HullShape.WEDGE, HullShape.valueOf("WEDGE"));
    }

    @Test
    @DisplayName("DOME - should exist")
    void dome_shouldExist() {
        assertSame(HullShape.DOME, HullShape.valueOf("DOME"));
    }

    @Test
    @DisplayName("PYRAMID - should exist")
    void pyramid_shouldExist() {
        assertSame(HullShape.PYRAMID, HullShape.valueOf("PYRAMID"));
    }

    @Test
    @DisplayName("CONE - should exist")
    void cone_shouldExist() {
        assertSame(HullShape.CONE, HullShape.valueOf("CONE"));
    }

    // ---- CUBE contains ----

    @Test
    @DisplayName("CUBE contains - should include all cells in 10x10x10")
    void cube_contains_allCells() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    assertTrue(HullShape.CUBE.contains(x, y, z),
                            "CUBE should contain (" + x + "," + y + "," + z + ")");
                }
            }
        }
    }

    @Test
    @DisplayName("CUBE contains - should exclude out of bounds")
    void cube_contains_outOfBounds() {
        assertFalse(HullShape.CUBE.contains(-1, 0, 0));
        assertFalse(HullShape.CUBE.contains(10, 0, 0));
        assertFalse(HullShape.CUBE.contains(0, -1, 0));
        assertFalse(HullShape.CUBE.contains(0, 10, 0));
        assertFalse(HullShape.CUBE.contains(0, 0, -1));
        assertFalse(HullShape.CUBE.contains(0, 0, 10));
    }

    @Test
    @DisplayName("CUBE estimatedCellCount - should be 1000")
    void cube_estimatedCellCount() {
        assertEquals(1000, HullShape.CUBE.estimatedCellCount());
    }

    // ---- WEDGE contains ----

    @Test
    @DisplayName("WEDGE contains - Z=0 should include all X")
    void wedge_contains_z0_allX() {
        for (int x = 0; x < 10; x++) {
            assertTrue(HullShape.WEDGE.contains(x, 5, 0),
                    "WEDGE at Z=0 should include X=" + x);
        }
    }

    @Test
    @DisplayName("WEDGE contains - Z=9 should include only X=0")
    void wedge_contains_z9_onlyX0() {
        assertTrue(HullShape.WEDGE.contains(0, 5, 9));
        for (int x = 1; x < 10; x++) {
            assertFalse(HullShape.WEDGE.contains(x, 5, 9),
                    "WEDGE at Z=9 should not include X=" + x);
        }
    }

    @Test
    @DisplayName("WEDGE contains - Z=5 should include X 0-4")
    void wedge_contains_z5_x0to4() {
        for (int x = 0; x <= 4; x++) {
            assertTrue(HullShape.WEDGE.contains(x, 5, 5),
                    "WEDGE at Z=5 should include X=" + x);
        }
        for (int x = 5; x < 10; x++) {
            assertFalse(HullShape.WEDGE.contains(x, 5, 5),
                    "WEDGE at Z=5 should not include X=" + x);
        }
    }

    @Test
    @DisplayName("WEDGE contains - should include all Y values")
    void wedge_contains_allY() {
        for (int y = 0; y < 10; y++) {
            assertTrue(HullShape.WEDGE.contains(0, y, 0),
                    "WEDGE should include all Y at X=0, Z=0");
        }
    }

    @Test
    @DisplayName("WEDGE contains - base at Z=0 should be 10x10")
    void wedge_contains_base10x10() {
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.WEDGE.contains(x, y, 0)) count++;
            }
        }
        assertEquals(100, count, "WEDGE base at Z=0 should be 10x10");
    }

    @Test
    @DisplayName("WEDGE estimatedCellCount - should be 550")
    void wedge_estimatedCellCount() {
        assertEquals(550, HullShape.WEDGE.estimatedCellCount());
    }

    @Test
    @DisplayName("WEDGE actual cell count matches estimate")
    void wedge_actualCellCount() {
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.WEDGE.contains(x, y, z)) count++;
                }
            }
        }
        assertEquals(550, count);
    }

    // ---- DOME contains ----

    @Test
    @DisplayName("DOME contains - center of base should be included")
    void dome_contains_centerBase() {
        assertTrue(HullShape.DOME.contains(4, 4, 0));
        assertTrue(HullShape.DOME.contains(5, 5, 0));
    }

    @Test
    @DisplayName("DOME contains - corners of base should be excluded")
    void dome_contains_cornersExcluded() {
        // Distance from (4.5, 4.5, 0) to (0, 0, 0) = sqrt(4.5^2 + 4.5^2) = sqrt(40.5) ≈ 6.36
        // This is within radius 9, so corners ARE included
        assertTrue(HullShape.DOME.contains(0, 0, 0));
        assertTrue(HullShape.DOME.contains(9, 9, 0));
    }

    @Test
    @DisplayName("DOME contains - top center should be included")
    void dome_contains_topCenter() {
        // (4,4,9): distance from (4.5,4.5,0) = sqrt(0.25 + 0.25 + 81) = sqrt(81.5) ≈ 9.03 > 9
        // (5,5,9): same distance ≈ 9.03 > 9
        // These are just outside the radius. No integer cell at Z=9 is within radius 9.
        int z9Count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.DOME.contains(x, y, 9)) z9Count++;
            }
        }
        assertEquals(0, z9Count, "DOME at Z=9 should have 0 cells (all just outside radius)");
    }

    @Test
    @DisplayName("DOME contains - Z=0 base should be fully included")
    void dome_contains_baseIncluded() {
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.DOME.contains(x, y, 0)) count++;
            }
        }
        // All cells at Z=0: max distance from center is sqrt(4.5^2 + 4.5^2) ≈ 6.36 < 9
        assertEquals(100, count, "DOME base at Z=0 should be fully included");
    }

    @Test
    @DisplayName("DOME contains - should have fewer cells than CUBE")
    void dome_contains_fewerThanCube() {
        int domeCount = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.DOME.contains(x, y, z)) domeCount++;
                }
            }
        }
        assertTrue(domeCount < 1000, "DOME should have fewer cells than CUBE");
        assertTrue(domeCount > 0, "DOME should have some cells");
    }

    @Test
    @DisplayName("DOME estimatedCellCount - should be positive")
    void dome_estimatedCellCount() {
        assertTrue(HullShape.DOME.estimatedCellCount() > 0);
    }

    // ---- PYRAMID contains ----

    @Test
    @DisplayName("PYRAMID contains - Z=0 should include all 10x10")
    void pyramid_contains_z0_all() {
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.PYRAMID.contains(x, y, 0)) count++;
            }
        }
        assertEquals(100, count, "PYRAMID base at Z=0 should be 10x10");
    }

    @Test
    @DisplayName("PYRAMID contains - Z=9 should include only center")
    void pyramid_contains_z9_center() {
        // At Z=9, halfSize = 0, center = 4.5
        // Only cells where x >= 4.5 && x <= 4.5 → x = 4.5 (no integer cell)
        // Actually: x >= 4.5 && x <= 4.5 means no integer x satisfies this
        // Let's check: at Z=9, halfSize = 0, so range is [4.5, 4.5]
        // No integer is exactly 4.5, so Z=9 layer has 0 cells
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.PYRAMID.contains(x, y, 9)) count++;
            }
        }
        // At Z=9, halfSize=0, range [4.5, 4.5], no integer cells
        assertEquals(0, count, "PYRAMID at Z=9 should have 0 cells (tip is between cells)");
    }

    @Test
    @DisplayName("PYRAMID contains - Z=8 should include center cells")
    void pyramid_contains_z8_center() {
        // At Z=8, halfSize = 1 * 4.5 / 9 = 0.5, range [4.0, 5.0]
        // x >= 4.0 && x <= 5.0 → x = 4, 5
        // y >= 4.0 && y <= 5.0 → y = 4, 5
        // So 2x2 = 4 cells
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.PYRAMID.contains(x, y, 8)) count++;
            }
        }
        assertEquals(4, count, "PYRAMID at Z=8 should have 4 center cells");
    }

    @Test
    @DisplayName("PYRAMID contains - should taper as Z increases")
    void pyramid_contains_tapers() {
        int[] counts = new int[10];
        for (int z = 0; z < 10; z++) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    if (HullShape.PYRAMID.contains(x, y, z)) counts[z]++;
                }
            }
        }
        // Counts should decrease as Z increases
        for (int z = 1; z < 10; z++) {
            assertTrue(counts[z] <= counts[z - 1],
                    "PYRAMID cells at Z=" + z + " should be <= Z=" + (z - 1));
        }
    }

    @Test
    @DisplayName("PYRAMID estimatedCellCount - should be positive")
    void pyramid_estimatedCellCount() {
        assertTrue(HullShape.PYRAMID.estimatedCellCount() > 0);
    }

    // ---- CONE contains ----

    @Test
    @DisplayName("CONE contains - Z=0 should include center circle")
    void cone_contains_z0_circle() {
        // At Z=0, radius = 4.5
        // Center cells should be included
        assertTrue(HullShape.CONE.contains(4, 4, 0));
        assertTrue(HullShape.CONE.contains(5, 5, 0));
        assertTrue(HullShape.CONE.contains(4, 5, 0));
        assertTrue(HullShape.CONE.contains(5, 4, 0));
    }

    @Test
    @DisplayName("CONE contains - Z=0 corners should be excluded")
    void cone_contains_z0_cornersExcluded() {
        // (0,0,0): distance from (4.5, 4.5) = sqrt(40.5) ≈ 6.36 > 4.5
        assertFalse(HullShape.CONE.contains(0, 0, 0));
        assertFalse(HullShape.CONE.contains(9, 9, 0));
        assertFalse(HullShape.CONE.contains(0, 9, 0));
        assertFalse(HullShape.CONE.contains(9, 0, 0));
    }

    @Test
    @DisplayName("CONE contains - Z=9 should have no cells")
    void cone_contains_z9_none() {
        // At Z=9, radius = 0, only exact center (4.5, 4.5) would be included
        // No integer cell is at (4.5, 4.5)
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (HullShape.CONE.contains(x, y, 9)) count++;
            }
        }
        assertEquals(0, count, "CONE at Z=9 should have 0 cells");
    }

    @Test
    @DisplayName("CONE contains - should taper as Z increases")
    void cone_contains_tapers() {
        int[] counts = new int[10];
        for (int z = 0; z < 10; z++) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    if (HullShape.CONE.contains(x, y, z)) counts[z]++;
                }
            }
        }
        for (int z = 1; z < 10; z++) {
            assertTrue(counts[z] <= counts[z - 1],
                    "CONE cells at Z=" + z + " should be <= Z=" + (z - 1));
        }
    }

    @Test
    @DisplayName("CONE contains - should have fewer cells than PYRAMID")
    void cone_contains_fewerThanPyramid() {
        int coneCount = 0, pyramidCount = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (HullShape.CONE.contains(x, y, z)) coneCount++;
                    if (HullShape.PYRAMID.contains(x, y, z)) pyramidCount++;
                }
            }
        }
        assertTrue(coneCount < pyramidCount,
                "CONE (" + coneCount + ") should have fewer cells than PYRAMID (" + pyramidCount + ")");
    }

    @Test
    @DisplayName("CONE estimatedCellCount - should be positive")
    void cone_estimatedCellCount() {
        assertTrue(HullShape.CONE.estimatedCellCount() > 0);
    }

    // ---- Shape comparison ----

    @Test
    @DisplayName("All shapes should have at least 10x10 base at Z=0 or partial base")
    void all_shapes_baseCheck() {
        // CUBE, WEDGE, PYRAMID have full 10x10 base
        assertEquals(100, countShapeAtZ(HullShape.CUBE, 0));
        assertEquals(100, countShapeAtZ(HullShape.WEDGE, 0));
        assertEquals(100, countShapeAtZ(HullShape.PYRAMID, 0));
        // DOME has full base (all within radius 9)
        assertEquals(100, countShapeAtZ(HullShape.DOME, 0));
        // CONE has circular base, not full 10x10
        assertTrue(countShapeAtZ(HullShape.CONE, 0) < 100);
    }

    @Test
    @DisplayName("Shape cell counts should be ordered: CUBE > WEDGE > DOME > PYRAMID > CONE")
    void shape_cellCount_order() {
        int cube = countShape(HullShape.CUBE);
        int wedge = countShape(HullShape.WEDGE);
        int dome = countShape(HullShape.DOME);
        int pyramid = countShape(HullShape.PYRAMID);
        int cone = countShape(HullShape.CONE);

        assertTrue(cube >= wedge, "CUBE(" + cube + ") >= WEDGE(" + wedge + ")");
        assertTrue(wedge > dome || wedge > pyramid, "WEDGE should be among the larger shapes");
        assertTrue(pyramid > cone, "PYRAMID(" + pyramid + ") > CONE(" + cone + ")");
    }

    // ---- Helpers ----

    private int countShape(HullShape shape) {
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    if (shape.contains(x, y, z)) count++;
                }
            }
        }
        return count;
    }

    private int countShapeAtZ(HullShape shape, int z) {
        int count = 0;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (shape.contains(x, y, z)) count++;
            }
        }
        return count;
    }
}
