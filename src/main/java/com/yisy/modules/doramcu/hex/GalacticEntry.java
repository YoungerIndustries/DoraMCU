package com.yisy.modules.doramcu.hex;

/**
 * Represents a solar system entry with its 3D position within a sector.
 * <p>
 * Used to track the location of solar systems within the galaxy hierarchy:
 * Galaxy → Sector → SubSector → SolarSystem (at position x, y, z).
 */
public class GalacticEntry {
    private final SolarSystem system;
    private final int x;
    private final int y;
    private final int z;
    private final int sectorRow;
    private final int sectorCol;
    private final int subSectorRow;
    private final int subSectorCol;

    public GalacticEntry(SolarSystem system, int x, int y, int z) {
        this(system, x, y, z, -1, -1, -1, -1);
    }

    public GalacticEntry(SolarSystem system, int x, int y, int z, int sectorRow, int sectorCol) {
        this(system, x, y, z, sectorRow, sectorCol, -1, -1);
    }

    public GalacticEntry(SolarSystem system, int x, int y, int z, int sectorRow, int sectorCol, int subSectorRow, int subSectorCol) {
        if (system == null) {
            throw new IllegalArgumentException("System must not be null");
        }
        this.system = system;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sectorRow = sectorRow;
        this.sectorCol = sectorCol;
        this.subSectorRow = subSectorRow;
        this.subSectorCol = subSectorCol;
    }

    /**
     * @return the solar system
     */
    public SolarSystem getSystem() {
        return system;
    }

    /**
     * @return the x coordinate within the subsector (0-9)
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y coordinate within the subsector (0-9)
     */
    public int getY() {
        return y;
    }

    /**
     * @return the z coordinate within the subsector (0-9)
     */
    public int getZ() {
        return z;
    }

    /**
     * @return the sector row (0-9), or -1 if not assigned to a sector
     */
    public int getSectorRow() {
        return sectorRow;
    }

    /**
     * @return the sector column (0-9), or -1 if not assigned to a sector
     */
    public int getSectorCol() {
        return sectorCol;
    }

    /**
     * @return the subsector row (0-9), or -1 if not assigned to a subsector
     */
    public int getSubSectorRow() {
        return subSectorRow;
    }

    /**
     * @return the subsector column (0-9), or -1 if not assigned to a subsector
     */
    public int getSubSectorCol() {
        return subSectorCol;
    }

    /**
     * @return the sector ID (row * 10 + col), or -1 if not assigned
     */
    public int getSectorId() {
        if (sectorRow < 0 || sectorCol < 0) {
            return -1;
        }
        return sectorRow * Galaxy.GRID_SIZE + sectorCol;
    }

    @Override
    public String toString() {
        return String.format("GalacticEntry{system=%s, pos=(%d,%d,%d), sector=(%d,%d), subsector=(%d,%d)}",
                system, x, y, z, sectorRow, sectorCol, subSectorRow, subSectorCol);
    }
}
