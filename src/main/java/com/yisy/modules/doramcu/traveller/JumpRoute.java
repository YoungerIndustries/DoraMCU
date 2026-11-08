package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * A single jump leg between two star systems in a jump route.
 * <p>
 * In Classic Traveller Book 2, each jump consumes fuel based on the distance
 * and the ship's jump drive rating. A jump route represents one segment of
 * a multi-jump voyage from origin to destination.
 * </p>
 * <p>
 * Each jump leg has:
 * </p>
 * <ul>
 *   <li>Origin and destination star systems</li>
 *   <li>Distance in parsecs</li>
 *   <li>Fuel cost in tons (depends on ship's fusion drive)</li>
 *   <li>Safety status of the hyperlane</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Tukkayj → Mesna: 3 parsecs, 1000 tons fuel (J-3 drive, 100kt ship)
 * </pre>
 */
public class JumpRoute {

    /**
     * The origin star system of this jump leg.
     */
    private final TravellerWorld origin;

    /**
     * The destination star system of this jump leg.
     */
    private final TravellerWorld destination;

    /**
     * The distance of this jump in parsecs.
     */
    private final int distanceParsecs;

    /**
     * The hyperlane used for this jump, if any.
     * Null if the jump is off-chart (no pre-defined hyperlane).
     */
    private final Hyperlane hyperlane;

    /**
     * Fuel cost in tons for this jump.
     * Calculated based on the ship's fusion drive and distance.
     */
    private final int fuelCostTons;

    /**
     * True if this jump is considered safe.
     */
    private final boolean isSafe;

    /**
     * Creates a new jump route between two worlds.
     * <p>
     * The distance is calculated from sector map coordinates.
     * Fuel cost is estimated based on the ship's fusion drive rating.
     * </p>
     *
     * @param origin the origin world
     * @param destination the destination world
     * @param fusionDrive the ship's fusion drive (determines fuel cost)
     * @param totalTonnage the ship's total tonnage
     */
    public JumpRoute(TravellerWorld origin, TravellerWorld destination,
                      FusionDrive fusionDrive, int totalTonnage) {
        this.origin = Objects.requireNonNull(origin, "Origin world must not be null");
        this.destination = Objects.requireNonNull(destination, "Destination world must not be null");
        this.distanceParsecs = origin.getJumpDistanceTo(destination);
        this.hyperlane = null;
        this.fuelCostTons = calculateFuelCost(fusionDrive, totalTonnage, distanceParsecs);
        this.isSafe = true;
    }

    /**
     * Creates a new jump route using an existing hyperlane.
     * <p>
     * The hyperlane provides the distance and safety status.
     * Fuel cost is calculated based on the ship's fusion drive.
     * </p>
     *
     * @param hyperlane the hyperlane for this jump
     * @param fusionDrive the ship's fusion drive (determines fuel cost)
     * @param totalTonnage the ship's total tonnage
     */
    public JumpRoute(Hyperlane hyperlane, FusionDrive fusionDrive, int totalTonnage) {
        this.hyperlane = Objects.requireNonNull(hyperlane, "Hyperlane must not be null");
        this.origin = hyperlane.getFrom();
        this.destination = hyperlane.getTo();
        this.distanceParsecs = hyperlane.getDistanceParsecs();
        this.fuelCostTons = calculateFuelCost(fusionDrive, totalTonnage, distanceParsecs);
        this.isSafe = hyperlane.isSafe();
    }

    /**
     * Creates a new jump route with explicit parameters.
     *
     * @param origin the origin world
     * @param destination the destination world
     * @param distanceParsecs the distance in parsecs
     * @param fuelCostTons the fuel cost in tons
     * @param isSafe true if the jump is safe
     */
    public JumpRoute(TravellerWorld origin, TravellerWorld destination,
                      int distanceParsecs, int fuelCostTons, boolean isSafe) {
        this.origin = Objects.requireNonNull(origin, "Origin world must not be null");
        this.destination = Objects.requireNonNull(destination, "Destination world must not be null");
        if (distanceParsecs < 1) {
            throw new IllegalArgumentException("Distance must be at least 1 parsec");
        }
        if (fuelCostTons < 0) {
            throw new IllegalArgumentException("Fuel cost must be non-negative");
        }
        this.distanceParsecs = distanceParsecs;
        this.fuelCostTons = fuelCostTons;
        this.isSafe = isSafe;
        this.hyperlane = null;
    }

    /**
     * Calculates the fuel cost for a jump.
     * <p>
     * In Classic Traveller, each jump consumes 1% of ship tonnage in fuel
     * per point of maneuver rating. For simplicity, we use:
     * </p>
     * <p>
     * Fuel = (totalTonnage / 100) × distance
     * </p>
     *
     * @param fusionDrive the ship's fusion drive
     * @param totalTonnage the ship's total tonnage
     * @param distance the jump distance in parsecs
     * @return fuel cost in tons
     */
    private int calculateFuelCost(FusionDrive fusionDrive, int totalTonnage, int distance) {
        if (fusionDrive == null || fusionDrive.getRating() == 0) {
            // No fusion drive: minimum fuel cost
            return Math.max(1000, totalTonnage / 100);
        }
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        return tonsPerFuel * distance;
    }

    // ---- Accessors ----

    /**
     * @return the origin world
     */
    public TravellerWorld getOrigin() {
        return origin;
    }

    /**
     * @return the destination world
     */
    public TravellerWorld getDestination() {
        return destination;
    }

    /**
     * @return the distance in parsecs
     */
    public int getDistanceParsecs() {
        return distanceParsecs;
    }

    /**
     * @return the hyperlane used, or null if off-chart
     */
    public Hyperlane getHyperlane() {
        return hyperlane;
    }

    /**
     * @return the fuel cost in tons
     */
    public int getFuelCostTons() {
        return fuelCostTons;
    }

    /**
     * @return true if this jump is safe
     */
    public boolean isSafe() {
        return isSafe;
    }

    /**
     * Checks if a ship with the given jump rating can make this jump.
     *
     * @param jumpRating the ship's jump drive rating
     * @return true if the jump is possible
     */
    public boolean isReachable(int jumpRating) {
        return jumpRating >= distanceParsecs;
    }

    /**
     * Returns the estimated time for this jump.
     * <p>
     * In Classic Traveller, a jump takes 2D6 hours (average 7 hours).
     * This method returns the average time.
     * </p>
     *
     * @return average jump time in hours
     */
    public int getEstimatedJumpHours() {
        return 7; // Average of 2D6
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return origin.getName() + " → " + destination.getName() +
                " (" + distanceParsecs + " pc, " + fuelCostTons + " tons fuel)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JumpRoute jumpRoute = (JumpRoute) o;
        return origin.equals(jumpRoute.origin) && destination.equals(jumpRoute.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }
}
