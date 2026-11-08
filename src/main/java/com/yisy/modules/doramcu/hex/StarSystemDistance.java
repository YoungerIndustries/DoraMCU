package com.yisy.modules.doramcu.hex;

import java.util.Objects;

/**
 * Calculates distances between star systems in the galaxy.
 * <p>
 * Converts the 3D hex grid coordinates used by {@link GalacticEntry} into
 * physical distances measured in light-years, which are then used by
 * {@link TravelCalculator} to compute travel times.
 * </p>
 * <p>
 * The galaxy hierarchy maps to physical distances as follows:
 * <ul>
 *   <li>Each subsector (10×10×10 hex grid) spans ~5 light-years per unit</li>
 *   <li>Each sector (10×10 subsectors) spans ~50 light-years per subsector boundary</li>
 *   <li>Cross-sector distances scale with the sector grid positions</li>
 * </ul>
 * </p>
 * <p>
 * Distance calculation uses a weighted 3D Euclidean formula that accounts for:
 * <ul>
 *   <li>Within-subsector distance (x, y, z coordinates 0-9)</li>
 *   <li>Between-subsector distance (subsector row/col within a sector)</li>
 *   <li>Between-sector distance (sector row/col in the 10×10 galaxy grid)</li>
 * </ul>
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * StarSystemDistance distance = new StarSystemDistance();
 *
 * // Same subsector
 * double d1 = distance.lightYearsBetween(
 *     new GalacticEntry(sys1, 2, 3, 4, 0, 0, 1, 1),
 *     new GalacticEntry(sys2, 8, 7, 6, 0, 0, 1, 1)
 * );
 *
 * // Different sectors
 * double d2 = distance.lightYearsBetween(
 *     new GalacticEntry(sys1, 0, 0, 0, 0, 0, 0, 0),
 *     new GalacticEntry(sys2, 9, 9, 9, 9, 9, 9, 9)
 * );
 * </pre>
 */
public class StarSystemDistance {

    /** Light-years per subsector unit (x, y, z within a subsector). */
    public static final double LIGHT_YEARS_PER_SUBSECTOR_UNIT = 5.0;

    /** Light-years per subsector boundary (moving to an adjacent subsector within a sector). */
    public static final double LIGHT_YEARS_PER_SUBSECTOR_BOUNDARY = 50.0;

    /** Light-years per sector boundary (moving to an adjacent sector). */
    public static final double LIGHT_YEARS_PER_SECTOR_BOUNDARY = 500.0;

    /** Minimum distance between any two distinct star systems (in light-years). */
    public static final double MIN_DISTANCE_LIGHT_YEARS = 0.5;

    /** Average distance between adjacent star systems in the same subsector. */
    public static final double AVG_DISTANCE_SAME_SUBSECTOR = 15.0;

    /** Average distance between systems in adjacent subsectors. */
    public static final double AVG_DISTANCE_ADJACENT_SUBSECTOR = 50.0;

    /** Average distance between systems in adjacent sectors. */
    public static final double AVG_DISTANCE_ADJACENT_SECTOR = 500.0;

    /**
     * Default constructor.
     */
    public StarSystemDistance() {
    }

    // ---- Distance Calculations ----

    /**
     * Calculates the distance in light-years between two galactic entries.
     * <p>
     * The distance is computed as a weighted 3D Euclidean distance that accounts
     * for sector, subsector, and within-subsector positions.
     * </p>
     *
     * @param from the source galactic entry
     * @param to the destination galactic entry
     * @return the distance in light-years (minimum {@link #MIN_DISTANCE_LIGHT_YEARS})
     */
    public double lightYearsBetween(GalacticEntry from, GalacticEntry to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Galactic entries must not be null");
        }

        // If both entries have sector/subsector info, use full calculation
        if (from.getSectorRow() >= 0 && to.getSectorRow() >= 0) {
            return calculateFullDistance(from, to);
        }

        // Fallback: use only 3D coordinates
        return calculate3DDistance(from.getX(), from.getY(), from.getZ(),
                to.getX(), to.getY(), to.getZ());
    }

    /**
     * Calculates the distance in light-years between two systems using full
     * sector, subsector, and 3D position information.
     *
     * @param from the source entry
     * @param to the destination entry
     * @return the distance in light-years
     */
    public double calculateFullDistance(GalacticEntry from, GalacticEntry to) {
        // Sector-level distance (largest scale)
        double sectorDeltaRow = from.getSectorRow() - to.getSectorRow();
        double sectorDeltaCol = from.getSectorCol() - to.getSectorCol();
        double sectorDistance = Math.sqrt(sectorDeltaRow * sectorDeltaRow + sectorDeltaCol * sectorDeltaCol)
                * LIGHT_YEARS_PER_SECTOR_BOUNDARY;

        // Subsector-level distance (medium scale)
        double subDeltaRow;
        double subDeltaCol;

        if (from.getSectorRow() == to.getSectorRow() && from.getSectorCol() == to.getSectorCol()) {
            // Same sector: use actual subsector positions
            subDeltaRow = from.getSubSectorRow() - to.getSubSectorRow();
            subDeltaCol = from.getSubSectorCol() - to.getSubSectorCol();
        } else {
            // Different sectors: use within-sector offset from boundary
            subDeltaRow = Math.abs(from.getSubSectorRow() - to.getSubSectorRow());
            subDeltaCol = Math.abs(from.getSubSectorCol() - to.getSubSectorCol());
        }

        double subsectorDistance = Math.sqrt(subDeltaRow * subDeltaRow + subDeltaCol * subDeltaCol)
                * LIGHT_YEARS_PER_SUBSECTOR_BOUNDARY;

        // Within-subsector distance (smallest scale)
        double withinDistance;
        if (from.getSectorRow() == to.getSectorRow() && from.getSectorCol() == to.getSectorCol()
                && from.getSubSectorRow() == to.getSubSectorRow() && from.getSubSectorCol() == to.getSubSectorCol()) {
            // Same subsector: use actual 3D positions
            withinDistance = calculate3DDistance(from.getX(), from.getY(), from.getZ(),
                    to.getX(), to.getY(), to.getZ());
        } else {
            // Different subsectors: use within-subsector offset from boundary
            double dx = Math.abs(from.getX() - to.getX());
            double dy = Math.abs(from.getY() - to.getY());
            double dz = Math.abs(from.getZ() - to.getZ());
            withinDistance = Math.sqrt(dx * dx + dy * dy + dz * dz)
                    * LIGHT_YEARS_PER_SUBSECTOR_UNIT;
        }

        // Combine all scales
        double totalDistance = sectorDistance + subsectorDistance + withinDistance;

        // Enforce minimum distance
        return Math.max(MIN_DISTANCE_LIGHT_YEARS, totalDistance);
    }

    /**
     * Calculates the distance in light-years between two 3D positions within
     * the same subsector.
     *
     * @param x1 the x coordinate of the first position (0-9)
     * @param y1 the y coordinate of the first position (0-9)
     * @param z1 the z coordinate of the first position (0-9)
     * @param x2 the x coordinate of the second position (0-9)
     * @param y2 the y coordinate of the second position (0-9)
     * @param z2 the z coordinate of the second position (0-9)
     * @return the distance in light-years
     */
    public double calculate3DDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz) * LIGHT_YEARS_PER_SUBSECTOR_UNIT;

        // If positions are identical, return 0
        if (dx == 0 && dy == 0 && dz == 0) {
            return 0.0;
        }

        return Math.max(MIN_DISTANCE_LIGHT_YEARS, distance);
    }

    /**
     * Calculates the distance in light-years between two systems given their
     * sector and subsector positions.
     *
     * @param fromSectorRow the source sector row (0-9)
     * @param fromSectorCol the source sector column (0-9)
     * @param fromSubRow the source subsector row (0-9)
     * @param fromSubCol the source subsector column (0-9)
     * @param toSectorRow the destination sector row (0-9)
     * @param toSectorCol the destination sector column (0-9)
     * @param toSubRow the destination subsector row (0-9)
     * @param toSubCol the destination subsector column (0-9)
     * @return the distance in light-years
     */
    public double calculateSectorDistance(int fromSectorRow, int fromSectorCol,
                                          int fromSubRow, int fromSubCol,
                                          int toSectorRow, int toSectorCol,
                                          int toSubRow, int toSubCol) {
        double sectorDeltaRow = fromSectorRow - toSectorRow;
        double sectorDeltaCol = fromSectorCol - toSectorCol;
        double sectorDistance = Math.sqrt(sectorDeltaRow * sectorDeltaRow + sectorDeltaCol * sectorDeltaCol)
                * LIGHT_YEARS_PER_SECTOR_BOUNDARY;

        double subDeltaRow = fromSubRow - toSubRow;
        double subDeltaCol = fromSubCol - toSubCol;
        double subsectorDistance = Math.sqrt(subDeltaRow * subDeltaRow + subDeltaCol * subDeltaCol)
                * LIGHT_YEARS_PER_SUBSECTOR_BOUNDARY;

        return Math.max(MIN_DISTANCE_LIGHT_YEARS, sectorDistance + subsectorDistance);
    }

    /**
     * Estimates the distance to a random system in a given quadrant.
     * <p>
     * This is useful for generating approximate travel times when the exact
     * destination is not yet known.
     * </p>
     *
     * @param fromSectorRow the source sector row
     * @param fromSectorCol the source sector column
     * @param targetQuadrant the target quadrant name
     * @return the estimated average distance in light-years
     */
    public double estimateQuadrantDistance(int fromSectorRow, int fromSectorCol, String targetQuadrant) {
        // Estimate center of target quadrant
        int targetRow, targetCol;

        switch (targetQuadrant) {
            case "Alpha Quadrant":
                targetRow = 2;
                targetCol = 2;
                break;
            case "Beta Quadrant":
                targetRow = 2;
                targetCol = 7;
                break;
            case "Gamma Quadrant":
                targetRow = 7;
                targetCol = 2;
                break;
            case "Delta Quadrant":
                targetRow = 7;
                targetCol = 7;
                break;
            default:
                targetRow = 4;
                targetCol = 4;
                break;
        }

        double sectorDeltaRow = fromSectorRow - targetRow;
        double sectorDeltaCol = fromSectorCol - targetCol;
        double sectorDistance = Math.sqrt(sectorDeltaRow * sectorDeltaRow + sectorDeltaCol * sectorDeltaCol)
                * LIGHT_YEARS_PER_SECTOR_BOUNDARY;

        // Add average subsector and within-subsector distance
        return sectorDistance + AVG_DISTANCE_ADJACENT_SUBSECTOR + AVG_DISTANCE_SAME_SUBSECTOR;
    }

    /**
     * Calculates the distance in light-years between two systems using
     * simplified sector IDs.
     *
     * @param fromSectorId the source sector ID (0-99)
     * @param toSectorId the destination sector ID (0-99)
     * @return the approximate distance in light-years
     */
    public double calculateSectorIdDistance(int fromSectorId, int toSectorId) {
        int fromRow = fromSectorId / Galaxy.GRID_SIZE;
        int fromCol = fromSectorId % Galaxy.GRID_SIZE;
        int toRow = toSectorId / Galaxy.GRID_SIZE;
        int toCol = toSectorId % Galaxy.GRID_SIZE;

        double sectorDeltaRow = fromRow - toRow;
        double sectorDeltaCol = fromCol - toCol;
        double sectorDistance = Math.sqrt(sectorDeltaRow * sectorDeltaRow + sectorDeltaCol * sectorDeltaCol)
                * LIGHT_YEARS_PER_SECTOR_BOUNDARY;

        return Math.max(MIN_DISTANCE_LIGHT_YEARS, sectorDistance);
    }

    /**
     * Formats a distance in light-years into a human-readable string.
     *
     * @param lightYears the distance in light-years
     * @return a formatted string (e.g., "1,250.5 light-years")
     */
    public static String formatDistance(double lightYears) {
        if (lightYears < 0) {
            throw new IllegalArgumentException("Distance must be non-negative: " + lightYears);
        }
        if (lightYears < 1.0) {
            return String.format("%.2f light-years", lightYears);
        }
        if (lightYears < 100.0) {
            return String.format("%.1f light-years", lightYears);
        }
        if (lightYears < 10_000.0) {
            return String.format("%d light-years", (int) Math.round(lightYears));
        }
        // Large distances
        return String.format("%s light-years", String.format("%,d", (int) Math.round(lightYears)));
    }
}
