package com.yisy.modules.doramcu.hex;

/**
 * Defines the possible shapes for a {@link HullModule}.
 * <p>
 * Each shape uses the 10×10×10 grid of M3space cells but includes only those cells
 * that fall within the shape's geometric boundaries. All shapes have at least one
 * side that is a full 10×10 face (the base at Z=0).
 * <p>
 * Shape descriptions:
 * <ul>
 *   <li><b>CUBE</b> — Full 10×10×10 cube; all 1000 cells are included.</li>
 *   <li><b>WEDGE</b> — Triangular prism; 10×10 base at Z=0, depth decreases linearly
 *       as Z increases. At Z=0 all X values (0–9) are included; at Z=9 only X=0 is included.</li>
 *   <li><b>DOME</b> — Hemispherical dome; 10×10 base at Z=0, cells included if their
 *       distance from the base center (4.5, 4.5, 0) is less than or equal to the radius (9).</li>
 *   <li><b>PYRAMID</b> — Square pyramid; 10×10 base at Z=0, tapering to a point at the top.
 *       The included X-Y range shrinks linearly as Z increases.</li>
 *   <li><b>CONE</b> — Circular cone; 10×10 base at Z=0, tapering to a point at the top.
 *       Cells are included if their distance from the center axis is within the cone's radius at that Z level.</li>
 * </ul>
 */
public enum HullShape {

    /**
     * Full 10×10×10 cube — all cells included.
     */
    CUBE,

    /**
     * Triangular wedge — 10×10 base, depth tapers along the X axis as Z increases.
     * At Z=0, all X (0–9) are included; at Z=9, only X=0 is included.
     */
    WEDGE,

    /**
     * Hemispherical dome — 10×10 base, curved top.
     * Cells within radius 9 from the base center (4.5, 4.5, 0) are included.
     */
    DOME,

    /**
     * Square pyramid — 10×10 base, tapering to a point at Z=9.
     * The X-Y square shrinks by 1 unit on each side per Z level.
     */
    PYRAMID,

    /**
     * Circular cone — 10×10 base, tapering to a point at Z=9.
     * Cells within the cone's radius at each Z level are included.
     */
    CONE;

    /**
     * Checks whether the given cell coordinates fall within this shape.
     *
     * @param x the x coordinate (0–9)
     * @param y the y coordinate (0–9)
     * @param z the z coordinate (0–9)
     * @return true if the cell is part of this shape
     */
    public boolean contains(int x, int y, int z) {
        if (x < 0 || x >= HullModule.WIDTH || y < 0 || y >= HullModule.HEIGHT || z < 0 || z >= HullModule.DEPTH) {
            return false;
        }

        switch (this) {
            case CUBE:
                return true;

            case WEDGE:
                // Triangular prism: at Z=0, all X included; at Z=9, only X=0 included
                // Max X at given Z = (DEPTH - 1 - Z), so x <= (9 - z)
                return x <= (HullModule.DEPTH - 1 - z);

            case DOME:
                // Hemisphere centered at (4.5, 4.5, 0) with radius 9
                // (x - 4.5)^2 + (y - 4.5)^2 + z^2 <= 9^2
                double dx = x - 4.5;
                double dy = y - 4.5;
                double dz = z;
                return (dx * dx + dy * dy + dz * dz) <= 81.0;

            case PYRAMID:
                // Square pyramid: at Z=0, full 10×10; at Z=9, only center cell(s)
                // The half-size shrinks by Z/2 each level
                // At Z=0: halfSize = 4.5, range [0, 9]
                // At Z=9: halfSize = 0, range [4.5, 4.5] → center
                double halfSize = (HullModule.DEPTH - 1 - z) * 4.5 / (HullModule.DEPTH - 1);
                double center = 4.5;
                return x >= (center - halfSize) && x <= (center + halfSize) &&
                       y >= (center - halfSize) && y <= (center + halfSize);

            case CONE:
                // Circular cone: at Z=0, radius 4.5; at Z=9, radius 0
                double radius = (HullModule.DEPTH - 1 - z) * 4.5 / (HullModule.DEPTH - 1);
                double coneDx = x - 4.5;
                double coneDy = y - 4.5;
                return (coneDx * coneDx + coneDy * coneDy) <= (radius * radius);

            default:
                return false;
        }
    }

    /**
     * Checks whether the given cell is on the exterior surface of this shape.
     * A cell is exterior if it is within the shape and at least one of its 6 face neighbors
     * (±X, ±Y, ±Z) is outside the shape.
     *
     * @param x the x coordinate (0–9)
     * @param y the y coordinate (0–9)
     * @param z the z coordinate (0–9)
     * @return true if the cell is part of the shape and on its exterior surface
     */
    public boolean isExterior(int x, int y, int z) {
        if (!contains(x, y, z)) {
            return false;
        }
        // A cell is exterior if ANY of its 6 face neighbors is outside the shape
        if (!contains(x - 1, y, z)) return true;
        if (!contains(x + 1, y, z)) return true;
        if (!contains(x, y - 1, z)) return true;
        if (!contains(x, y + 1, z)) return true;
        if (!contains(x, y, z - 1)) return true;
        if (!contains(x, y, z + 1)) return true;
        return false;
    }

    /**
     * Checks whether the given cell is an interior cell of this shape.
     * A cell is interior if it is within the shape and all 6 face neighbors
     * (±X, ±Y, ±Z) are also within the shape.
     *
     * @param x the x coordinate (0–9)
     * @param y the y coordinate (0–9)
     * @param z the z coordinate (0–9)
     * @return true if the cell is part of the shape and surrounded on all 6 face sides
     */
    public boolean isInterior(int x, int y, int z) {
        if (!contains(x, y, z)) {
            return false;
        }
        // All 6 face neighbors must be within the shape
        return contains(x - 1, y, z) && contains(x + 1, y, z) &&
               contains(x, y - 1, z) && contains(x, y + 1, z) &&
               contains(x, y, z - 1) && contains(x, y, z + 1);
    }

    /**
     * Returns an estimate of the number of cells included in this shape.
     *
     * @return the approximate cell count
     */
    public int estimatedCellCount() {
        switch (this) {
            case CUBE:
                return 1000;
            case WEDGE:
                return 550; // Sum of (10-z)*10 for z=0..9 = 10*(10+9+8+...+1) = 10*55 = 550
            case DOME:
                return 400; // Approximate hemisphere volume
            case PYRAMID:
                return 220; // Approximate pyramid volume
            case CONE:
                return 200; // Approximate cone volume
            default:
                return 0;
        }
    }
}
