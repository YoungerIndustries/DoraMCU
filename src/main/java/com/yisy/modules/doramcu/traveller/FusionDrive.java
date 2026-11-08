package com.yisy.modules.doramcu.traveller;

/**
 * Fusion drive ratings from Classic Traveller Book 2.
 * <p>
 * The Fusion drive provides within-system maneuvering capability.
 * Rating determines the maximum acceleration in M (maneuver) units.
 * Higher ratings allow faster travel within a star system and better combat positioning.
 * </p>
 * <p>
 * Tonnage cost: Fusion rating × 1% of total tonnage (minimum 1000 tons per rating).
 * Fuel cost: Fusion rating × 1% of total tonnage per point of fuel loaded.
 * </p>
 */
public enum FusionDrive {

    /** No fusion drive — ship cannot maneuver within a system. */
    NONE(0),

    /** 0M — minimal maneuvering, essentially drift-only. */
    M0(0),

    /** 1M — basic maneuvering capability. */
    M1(1),

    /** 2M — standard maneuvering for merchant ships. */
    M2(2),

    /** 3M — good maneuvering for light military vessels. */
    M3(3),

    /** 4M — solid maneuvering for destroyers. */
    M4(4),

    /** 5M — heavy maneuvering for cruisers. */
    M5(5),

    /** 6M — excellent maneuvering for capital ships. */
    M6(6),

    /** 7M — superior maneuvering. */
    M7(7),

    /** 8M — extreme maneuvering. */
    M8(8),

    /** 9M+ — maximum maneuvering rating. */
    M9(9);

    private final int rating;

    FusionDrive(int rating) {
        this.rating = rating;
    }

    /**
     * Returns the fusion drive rating (0–9).
     *
     * @return the maneuver rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * Calculates the total tons required for this fusion drive on a ship of the given tonnage.
     * Each fusion rating consumes 1% of total tonnage, minimum 1000 tons per rating.
     *
     * @param totalTonnage the total ship tonnage in tons
     * @return tons consumed by the fusion drive
     */
    public int getTonsRequired(int totalTonnage) {
        if (rating == 0) return 0;
        int tonsPerFusion = Math.max(1000, totalTonnage / 100);
        return tonsPerFusion * rating;
    }

    /**
     * Calculates the fuel capacity in tons for a given fuel allocation.
     * Each point of maneuver requires 1% of ship tonnage in fuel.
     *
     * @param totalTonnage the total ship tonnage in tons
     * @param fuelLoad the number of fuel points loaded (0–rating)
     * @return tons of fuel required
     */
    public int getFuelTons(int totalTonnage, int fuelLoad) {
        if (fuelLoad <= 0) return 0;
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        return tonsPerFuel * fuelLoad;
    }

    /**
     * Returns the maximum number of fuel points this drive can use.
     *
     * @return maximum fuel points (equal to rating)
     */
    public int getMaxFuelPoints() {
        return rating;
    }

    /**
     * Finds the fusion drive with the given rating.
     *
     * @param rating the maneuver rating (0–9)
     * @return the matching drive, or NONE if not found
     */
    public static FusionDrive fromRating(int rating) {
        for (FusionDrive drive : values()) {
            if (drive.rating == rating) {
                return drive;
            }
        }
        return NONE;
    }
}
