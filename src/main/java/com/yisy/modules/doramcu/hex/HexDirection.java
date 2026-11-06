package com.yisy.modules.doramcu.hex;

/**
 * Represents the six cardinal directions in an axial-coordinate hex grid.
 * <p>
 * Using axial coordinates (q, r), each direction corresponds to a fixed offset:
 * <ul>
 *   <li>EAST     → (+1,  0)</li>
 *   <li>WEST     → (-1,  0)</li>
 *   <li>NORTH_EAST → (+1, -1)</li>
 *   <li>SOUTH_WEST → (-1, +1)</li>
 *   <li>NORTH_WEST → ( 0, -1)</li>
 *   <li>SOUTH_EAST → ( 0, +1)</li>
 * </ul>
 */
public enum HexDirection {
    EAST(1, 0),
    WEST(-1, 0),
    NORTH_EAST(1, -1),
    SOUTH_WEST(-1, 1),
    NORTH_WEST(0, -1),
    SOUTH_EAST(0, 1);

    private final int dq;
    private final int dr;

    HexDirection(int dq, int dr) {
        this.dq = dq;
        this.dr = dr;
    }

    /**
     * @return the q offset for this direction
     */
    public int getDq() {
        return dq;
    }

    /**
     * @return the r offset for this direction
     */
    public int getDr() {
        return dr;
    }

    /**
     * Returns the opposite direction.
     */
    public HexDirection opposite() {
        return switch (this) {
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH_EAST -> SOUTH_WEST;
            case SOUTH_WEST -> NORTH_EAST;
            case NORTH_WEST -> SOUTH_EAST;
            case SOUTH_EAST -> NORTH_WEST;
        };
    }
}
