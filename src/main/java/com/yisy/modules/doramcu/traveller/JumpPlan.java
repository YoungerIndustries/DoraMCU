package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A complete jump route plan from origin to destination.
 * <p>
 * In Classic Traveller Book 2, a voyage may consist of multiple jump legs,
 * each consuming fuel and time. A jump plan represents the entire route,
 * including all intermediate stops and the total fuel required.
 * </p>
 * <p>
 * The plan includes:
 * </p>
 * <ul>
 *   <li>Origin and final destination worlds</li>
 *   <li>List of jump legs ({@link JumpRoute} instances)</li>
 *   <li>Total distance in parsecs</li>
 *   <li>Total fuel required in tons</li>
 *   <li>Estimated total travel time</li>
 *   <li>Safety assessment of the route</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Tukkayj → Mesna → Core → Algea
 *   Total: 9 parsecs, 3000 tons fuel, 21 hours
 * </pre>
 */
public class JumpPlan {

    /**
     * The origin world of the entire voyage.
     */
    private final TravellerWorld origin;

    /**
     * The final destination world of the entire voyage.
     */
    private final TravellerWorld destination;

    /**
     * Ordered list of jump legs from origin to destination.
     */
    private final List<JumpRoute> jumpLegs;

    /**
     * Total distance of all jump legs in parsecs.
     */
    private final int totalDistanceParsecs;

    /**
     * Total fuel required for the entire voyage in tons.
     */
    private final int totalFuelTons;

    /**
     * Estimated total travel time in hours.
     */
    private final int estimatedHours;

    /**
     * True if all jump legs are safe.
     */
    private final boolean isSafe;

    /**
     * The ship's jump drive rating used for this plan.
     */
    private final int jumpRating;

    /**
     * Creates a new jump plan from a list of jump legs.
     *
     * @param jumpLegs the ordered list of jump legs
     * @param jumpRating the ship's jump drive rating
     * @throws IllegalArgumentException if jump legs list is empty
     */
    public JumpPlan(List<JumpRoute> jumpLegs, int jumpRating) {
        if (jumpLegs == null || jumpLegs.isEmpty()) {
            throw new IllegalArgumentException("Jump legs must not be empty");
        }
        this.jumpLegs = new ArrayList<>(jumpLegs);
        this.origin = jumpLegs.get(0).getOrigin();
        this.destination = jumpLegs.get(jumpLegs.size() - 1).getDestination();
        this.jumpRating = jumpRating;

        // Calculate totals
        int totalDistance = 0;
        int totalFuel = 0;
        boolean allSafe = true;

        for (JumpRoute leg : jumpLegs) {
            totalDistance += leg.getDistanceParsecs();
            totalFuel += leg.getFuelCostTons();
            if (!leg.isSafe()) {
                allSafe = false;
            }
        }

        this.totalDistanceParsecs = totalDistance;
        this.totalFuelTons = totalFuel;
        this.estimatedHours = jumpLegs.size() * 7; // 7 hours per jump (average 2D6)
        this.isSafe = allSafe;
    }

    /**
     * Creates a simple single-leg jump plan.
     *
     * @param origin the origin world
     * @param destination the destination world
     * @param distance the distance in parsecs
     * @param fuelTons the fuel cost in tons
     * @param jumpRating the ship's jump drive rating
     */
    public JumpPlan(TravellerWorld origin, TravellerWorld destination,
                     int distance, int fuelTons, int jumpRating) {
        this.origin = Objects.requireNonNull(origin, "Origin must not be null");
        this.destination = Objects.requireNonNull(destination, "Destination must not be null");
        this.jumpRating = jumpRating;

        JumpRoute leg = new JumpRoute(origin, destination, distance, fuelTons, true);
        this.jumpLegs = Collections.singletonList(leg);
        this.totalDistanceParsecs = distance;
        this.totalFuelTons = fuelTons;
        this.estimatedHours = 7;
        this.isSafe = true;
    }

    // ---- Accessors ----

    /**
     * @return the origin world
     */
    public TravellerWorld getOrigin() {
        return origin;
    }

    /**
     * @return the final destination world
     */
    public TravellerWorld getDestination() {
        return destination;
    }

    /**
     * @return the ordered list of jump legs (immutable)
     */
    public List<JumpRoute> getJumpLegs() {
        return Collections.unmodifiableList(jumpLegs);
    }

    /**
     * @return the number of jump legs
     */
    public int getJumpCount() {
        return jumpLegs.size();
    }

    /**
     * @return the total distance in parsecs
     */
    public int getTotalDistanceParsecs() {
        return totalDistanceParsecs;
    }

    /**
     * @return the total fuel required in tons
     */
    public int getTotalFuelTons() {
        return totalFuelTons;
    }

    /**
     * @return the estimated total travel time in hours
     */
    public int getEstimatedHours() {
        return estimatedHours;
    }

    /**
     * @return true if all jump legs are safe
     */
    public boolean isSafe() {
        return isSafe;
    }

    /**
     * @return the ship's jump drive rating used for this plan
     */
    public int getJumpRating() {
        return jumpRating;
    }

    // ---- Derived Properties ----

    /**
     * Returns the list of intermediate worlds (excluding origin and final destination).
     *
     * @return list of intermediate worlds
     */
    public List<TravellerWorld> getIntermediateWorlds() {
        List<TravellerWorld> intermediates = new ArrayList<>();
        for (int i = 0; i < jumpLegs.size() - 1; i++) {
            intermediates.add(jumpLegs.get(i).getDestination());
        }
        return Collections.unmodifiableList(intermediates);
    }

    /**
     * Checks if the ship has enough fuel for this plan.
     *
     * @param fuelAvailable the available fuel in tons
     * @return true if the ship has enough fuel
     */
    public boolean hasEnoughFuel(int fuelAvailable) {
        return fuelAvailable >= totalFuelTons;
    }

    /**
     * Checks if a ship with the given jump rating can execute this plan.
     *
     * @param shipJumpRating the ship's jump drive rating
     * @return true if all jumps are reachable
     */
    public boolean isExecutable(int shipJumpRating) {
        for (JumpRoute leg : jumpLegs) {
            if (!leg.isReachable(shipJumpRating)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the fuel remaining after executing this plan.
     *
     * @param fuelAvailable the available fuel in tons
     * @return fuel remaining in tons (may be negative if insufficient)
     */
    public int getFuelRemaining(int fuelAvailable) {
        return fuelAvailable - totalFuelTons;
    }

    /**
     * Returns the estimated number of days for the voyage.
     * <p>
     * Assumes 24 hours per day and includes refueling stops if needed.
     * </p>
     *
     * @return estimated days
     */
    public int getEstimatedDays() {
        return (estimatedHours + 23) / 24; // Ceiling division
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(origin.getName()).append(" → ");

        for (int i = 0; i < jumpLegs.size(); i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(jumpLegs.get(i).getDestination().getName());
        }

        sb.append(" | ").append(jumpLegs.size()).append(" jumps, ")
                .append(totalDistanceParsecs).append(" pc, ")
                .append(totalFuelTons).append(" tons fuel, ")
                .append(estimatedHours).append(" hrs");

        if (!isSafe) {
            sb.append(" [UNSAFE]");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JumpPlan jumpPlan = (JumpPlan) o;
        return origin.equals(jumpPlan.origin) && destination.equals(jumpPlan.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }
}
