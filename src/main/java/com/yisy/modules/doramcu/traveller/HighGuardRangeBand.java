package com.yisy.modules.doramcu.traveller;

/**
 * High Guard range bands for ship-to-ship combat.
 * <p>
 * In High Guard, ships are separated by range bands rather than exact distances.
 * Each band has a minimum thrust required to change bands, and weapons have
 * different effectiveness at different ranges.
 * </p>
 * <p>
 * Range bands and their properties:
 * </p>
 * <table>
 *   <tr><th>Range</th><th>Distance</th><th>Thrust to Change</th><th>Example</th></tr>
 *   <tr><td>Adjacent</td><td>&lt;1 km</td><td>1</td><td>Docked ships</td></tr>
 *   <tr><td>Close</td><td>1 to 10 km</td><td>1</td><td>Nearby vessels</td></tr>
 *   <tr><td>Short</td><td>10 to 1,250 km</td><td>2</td><td>Ships in same orbital path</td></tr>
 *   <tr><td>Medium</td><td>1,250 to 10,000 km</td><td>5</td><td>Surface to orbit</td></tr>
 *   <tr><td>Long</td><td>10,000 to 25,000 km</td><td>10</td><td>Near a planet</td></tr>
 *   <tr><td>Very Long</td><td>25,000 to 50,000 km</td><td>25</td><td>Within jump limit</td></tr>
 *   <tr><td>Distant</td><td>50,000 km+</td><td>50</td><td>Distant ships</td></tr>
 * </table>
 * <p>
 * Adjacent and Close are considered "Dogfighting Range."
 * </p>
 */
public enum HighGuardRangeBand {

    /**
     * Adjacent — less than 1 km.
     * <p>
     * Thrust to change: 1. Ships are essentially touching.
     * Boarding actions can be attempted from this range.
     * </p>
     */
    ADJACENT("Adjacent", 0, 1, 1, true,
            "Less than 1 km — docked ships, boarding range"),

    /**
     * Close — 1 to 10 km.
     * <p>
     * Thrust to change: 1. Very close combat range.
     * Dogfighting range.
     * </p>
     */
    CLOSE("Close", 1, 10, 1, true,
            "1 to 10 km — nearby vessels, dogfighting range"),

    /**
     * Short — 10 to 1,250 km.
     * <p>
     * Thrust to change: 2. Optimal range for pulse lasers.
     * </p>
     */
    SHORT("Short", 10, 1250, 2, false,
            "10 to 1,250 km — ships in same orbital path"),

    /**
     * Medium — 1,250 to 10,000 km.
     * <p>
     * Thrust to change: 5. Optimal range for beam lasers and fusion guns.
     * </p>
     */
    MEDIUM("Medium", 1250, 10000, 5, false,
            "1,250 to 10,000 km — surface to orbit"),

    /**
     * Long — 10,000 to 25,000 km.
     * <p>
     * Thrust to change: 10. Optimal range for particle beams and meson guns.
     * </p>
     */
    LONG("Long", 10000, 25000, 10, false,
            "10,000 to 25,000 km — near a planet"),

    /**
     * Very Long — 25,000 to 50,000 km.
     * <p>
     * Thrust to change: 25. Long-range missile engagement.
     * </p>
     */
    VERY_LONG("Very Long", 25000, 50000, 25, false,
            "25,000 to 50,000 km — within jump limit"),

    /**
     * Distant — 50,000 km and beyond.
     * <p>
     * Thrust to change: 50. Maximum engagement range.
     * Beyond this, weapons and sensors are ineffective.
     * </p>
     */
    DISTANT("Distant", 50000, 350000, 50, false,
            "50,000+ km — distant ships, maximum engagement");

    private final String name;
    private final int minDistanceKm;
    private final int maxDistanceKm;
    private final int thrustToChange;
    private final boolean isDogfighting;

    HighGuardRangeBand(String name, int minDistanceKm, int maxDistanceKm,
                       int thrustToChange, boolean isDogfighting, String description) {
        this.name = name;
        this.minDistanceKm = minDistanceKm;
        this.maxDistanceKm = maxDistanceKm;
        this.thrustToChange = thrustToChange;
        this.isDogfighting = isDogfighting;
    }

    /**
     * Returns the display name of this range band.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the minimum distance in km for this range band.
     */
    public int getMinDistanceKm() {
        return minDistanceKm;
    }

    /**
     * Returns the maximum distance in km for this range band.
     */
    public int getMaxDistanceKm() {
        return maxDistanceKm;
    }

    /**
     * Returns the thrust required to change to the next range band.
     * <p>
     * A ship must spend this many thrust points to move one range band
     * closer or farther from its target.
     * </p>
     */
    public int getThrustToChange() {
        return thrustToChange;
    }

    /**
     * Returns whether this is a dogfighting range band.
     * <p>
     * Adjacent and Close are dogfighting ranges.
     * In dogfighting, ships use agility-based combat rather than
     * standard Gunnery rolls.
     * </p>
     */
    public boolean isDogfighting() {
        return isDogfighting;
    }

    /**
     * Returns the range band index (0 = Adjacent, 6 = Distant).
     */
    public int getIndex() {
        return ordinal();
    }

    /**
     * Returns the range band that is one step closer.
     *
     * @return the closer range band, or ADJACENT if already at closest
     */
    public HighGuardRangeBand closer() {
        if (this == ADJACENT) {
            return ADJACENT;
        }
        return values()[ordinal() - 1];
    }

    /**
     * Returns the range band that is one step farther.
     *
     * @return the farther range band, or DISTANT if already at farthest
     */
    public HighGuardRangeBand farther() {
        if (this == DISTANT) {
            return DISTANT;
        }
        return values()[ordinal() + 1];
    }

    /**
     * Calculates the new range band after both ships maneuver.
     * <p>
     * If both ships charge each other, their thrust is combined.
     * If one flees, the difference in thrust determines the change.
     * </p>
     *
     * @param currentBand the current range band
     * @param attackerThrust the attacker's thrust spent on movement
     * @param defenderThrust the defender's thrust spent on movement
     * @param attackerCharging whether the attacker is charging (true) or fleeing (false)
     * @param defenderCharging whether the defender is charging (true) or fleeing (false)
     * @return the new range band
     */
    public static HighGuardRangeBand calculateNewRange(
            HighGuardRangeBand currentBand,
            int attackerThrust, int defenderThrust,
            boolean attackerCharging, boolean defenderCharging) {

        int effectiveThrust;
        if (attackerCharging && defenderCharging) {
            // Both charging — thrust combines
            effectiveThrust = attackerThrust + defenderThrust;
        } else if (attackerCharging && !defenderCharging) {
            // Attacker charges, defender flees — difference
            effectiveThrust = attackerThrust - defenderThrust;
        } else if (!attackerCharging && defenderCharging) {
            // Attacker flees, defender charges — difference (negative = closer)
            effectiveThrust = defenderThrust - attackerThrust;
        } else {
            // Both fleeing — difference
            effectiveThrust = defenderThrust - attackerThrust;
        }

        // Calculate how many range bands to move
        int bandsToMove = 0;
        int remainingThrust = Math.abs(effectiveThrust);
        boolean movingCloser = effectiveThrust > 0;

        HighGuardRangeBand band = currentBand;
        while (remainingThrust > 0) {
            HighGuardRangeBand nextBand = movingCloser ? band.closer() : band.farther();
            if (nextBand == band) {
                break; // Already at limit
            }
            int thrustNeeded = band.getThrustToChange();
            if (remainingThrust >= thrustNeeded) {
                remainingThrust -= thrustNeeded;
                bandsToMove += movingCloser ? -1 : 1;
                band = nextBand;
            } else {
                break;
            }
        }

        return band;
    }

    /**
     * Determines the range band for a given distance in km.
     *
     * @param distanceKm the distance in kilometers
     * @return the corresponding range band
     */
    public static HighGuardRangeBand fromDistance(int distanceKm) {
        for (HighGuardRangeBand band : values()) {
            if (distanceKm >= band.minDistanceKm && distanceKm <= band.maxDistanceKm) {
                return band;
            }
        }
        if (distanceKm < 1) {
            return ADJACENT;
        }
        return DISTANT;
    }
}
