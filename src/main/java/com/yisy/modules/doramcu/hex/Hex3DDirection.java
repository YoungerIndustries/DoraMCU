package com.yisy.modules.doramcu.hex;

/**
 * Represents the face-adjacent directions for a 3D hex (cube) cell.
 * <p>
 * A 3D hex grid uses cubic coordinates (x, y, z) where x + y + z = 0.
 * Each cube has 6 face neighbors, reached by moving ±1 along one axis
 * while adjusting another axis to maintain the constraint.
 * <p>
 * These 6 directions correspond to the face-adjacent neighbors in a
 * 3D hexagonal (cubic) grid, analogous to the 6 directions in 2D hex grids.
 */
public enum Hex3DDirection {
    /** Move +X, -Y */
    POS_X_NEG_Y(1, -1, 0),
    /** Move +X, -Z */
    POS_X_NEG_Z(1, 0, -1),
    /** Move -X, +Y */
    NEG_X_POS_Y(-1, 1, 0),
    /** Move -X, +Z */
    NEG_X_POS_Z(-1, 0, 1),
    /** Move +Y, -Z */
    POS_Y_NEG_Z(0, 1, -1),
    /** Move -Y, +Z */
    NEG_Y_POS_Z(0, -1, 1);

    private final int dx;
    private final int dy;
    private final int dz;

    Hex3DDirection(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    /** @return the x offset for this direction */
    public int getDx() {
        return dx;
    }

    /** @return the y offset for this direction */
    public int getDy() {
        return dy;
    }

    /** @return the z offset for this direction */
    public int getDz() {
        return dz;
    }

    /**
     * Returns the opposite direction (negated offsets).
     *
     * @return the direction that reverses this one
     */
    public Hex3DDirection opposite() {
        switch (this) {
            case POS_X_NEG_Y: return NEG_X_POS_Y;
            case POS_X_NEG_Z: return NEG_X_POS_Z;
            case NEG_X_POS_Y: return POS_X_NEG_Y;
            case NEG_X_POS_Z: return POS_X_NEG_Z;
            case POS_Y_NEG_Z: return NEG_Y_POS_Z;
            case NEG_Y_POS_Z: return POS_Y_NEG_Z;
            default: throw new IllegalStateException("Unknown direction: " + this);
        }
    }
}
