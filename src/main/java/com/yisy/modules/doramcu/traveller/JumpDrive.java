package com.yisy.modules.doramcu.traveller;

/**
 * Jump drive ratings from Classic Traveller Book 2.
 * <p>
 * The Jump drive enables faster-than-light travel between star systems.
 * Rating determines the maximum jump distance in parsecs.
 * Each rating consumes a fixed percentage of total ship tonnage.
 * </p>
 * <p>
 * Tonnage cost: Jump rating × 1% of total tonnage (minimum 1000 tons per rating).
 * </p>
 */
public enum JumpDrive {

    /** No jump drive — ship cannot perform FTL jumps. */
    NONE(0, 0),

    /** J-1 — minimum jump capability, 1 parsec range. */
    J1(1, 1),

    /** J-2 — 2 parsec range. */
    J2(2, 1),

    /** J-3 — 3 parsec range. */
    J3(3, 1),

    /** J-4 — 4 parsec range. */
    J4(4, 1),

    /** J-5 — 5 parsec range. */
    J5(5, 1),

    /** J-6 — 6 parsec range. */
    J6(6, 1),

    /** J-7 — 7 parsec range. */
    J7(7, 1),

    /** J-8 — 8 parsec range. */
    J8(8, 1),

    /** J-9 — maximum jump range, 9 parsecs. */
    J9(9, 1);

    private final int rating;
    private final int tonsPerRating;

    JumpDrive(int rating, int tonsPerRating) {
        this.rating = rating;
        this.tonsPerRating = tonsPerRating;
    }

    /**
     * Returns the jump rating (0–9).
     *
     * @return the jump rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * Returns the tons consumed per rating point.
     *
     * @return tons per rating
     */
    public int getTonsPerRating() {
        return tonsPerRating;
    }

    /**
     * Calculates the total tons required for this jump drive on a ship of the given tonnage.
     * Each jump rating consumes 1% of total tonnage, minimum 1000 tons per rating.
     *
     * @param totalTonnage the total ship tonnage in tons
     * @return tons consumed by the jump drive
     */
    public int getTonsRequired(int totalTonnage) {
        if (rating == 0) return 0;
        int tonsPerJump = Math.max(1000, totalTonnage / 100);
        return tonsPerJump * rating;
    }

    /**
     * Returns the maximum jump distance in parsecs.
     *
     * @return maximum jump distance in parsecs
     */
    public int getMaxJumpParsecs() {
        return rating;
    }

    /**
     * Finds the jump drive with the given rating.
     *
     * @param rating the jump rating (0–9)
     * @return the matching drive, or NONE if not found
     */
    public static JumpDrive fromRating(int rating) {
        for (JumpDrive drive : values()) {
            if (drive.rating == rating) {
                return drive;
            }
        }
        return NONE;
    }
}
