package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A complete maneuver plan from Classic Traveller Book 2.
 * <p>
 * A maneuver plan represents a within-system voyage from one point to another,
 * consisting of multiple burn sequences. Each burn accelerates, decelerates,
 * or coasts for one week. The plan ensures the ship arrives at the destination
 * with zero velocity.
 * </p>
 * <p>
 * The plan includes:
 * </p>
 * <ul>
 *   <li>Origin and destination (celestial bodies or coordinates)</li>
 *   <li>Ordered list of burn sequences</li>
 *   <li>Total distance in AU</li>
 *   <li>Total fuel required in tons</li>
 *   <li>Total travel time in weeks</li>
 *   <li>Maximum velocity reached</li>
 * </ul>
 * <p>
 * Example — 3M ship traveling 13.5 AU:
 * </p>
 * <pre>
 *   Week 1: Accel +3 → V=3, D=1.5 AU
 *   Week 2: Accel +3 → V=6, D=4.5 AU
 *   Week 3: Decel -3 → V=3, D=4.5 AU
 *   Week 4: Decel -3 → V=0, D=1.5 AU
 *   Total: 13.5 AU in 4 weeks, max V=6
 * </pre>
 */
public class ManeuverPlan {

    /**
     * The origin description (e.g., planet name or "L4 point").
     */
    private final String origin;

    /**
     * The destination description.
     */
    private final String destination;

    /**
     * The Traveller world (star system) where this maneuver takes place.
     */
    private final TravellerWorld starSystem;

    /**
     * Ordered list of burn sequences.
     */
    private final List<BurnSequence> burns;

    /**
     * Total distance of the maneuver in AU.
     */
    private final double totalDistanceAU;

    /**
     * Total fuel required for the entire maneuver in tons.
     */
    private final int totalFuelTons;

    /**
     * Total travel time in weeks.
     */
    private final int totalWeeks;

    /**
     * Maximum velocity reached during the maneuver (AU/week).
     */
    private final double maxVelocity;

    /**
     * The ship's fusion drive rating used for this plan.
     */
    private final int fusionRating;

    /**
     * The ship's total tonnage.
     */
    private final int totalTonnage;

    /**
     * True if the plan includes a landing at the destination.
     */
    private final boolean includesLanding;

    /**
     * Creates a new maneuver plan from a list of burn sequences.
     *
     * @param origin the origin description
     * @param destination the destination description
     * @param starSystem the star system where this maneuver takes place
     * @param burns the ordered list of burn sequences
     * @param fusionRating the ship's fusion drive rating
     * @param totalTonnage the ship's total tonnage
     * @param includesLanding true if the plan includes landing
     */
    public ManeuverPlan(String origin, String destination, TravellerWorld starSystem,
                         List<BurnSequence> burns, int fusionRating, int totalTonnage,
                         boolean includesLanding) {
        if (burns == null || burns.isEmpty()) {
            throw new IllegalArgumentException("Burns must not be empty");
        }
        this.origin = Objects.requireNonNull(origin, "Origin must not be null");
        this.destination = Objects.requireNonNull(destination, "Destination must not be null");
        this.starSystem = starSystem;
        this.burns = new ArrayList<>(burns);
        this.fusionRating = fusionRating;
        this.totalTonnage = totalTonnage;
        this.includesLanding = includesLanding;

        // Calculate totals
        double maxVel = 0;
        for (BurnSequence burn : burns) {
            maxVel = Math.max(maxVel, burn.getVelocityEnd());
        }

        this.totalWeeks = burns.size();
        this.totalDistanceAU = burns.get(burns.size() - 1).getCumulativeDistance();
        this.maxVelocity = maxVel;

        int totalFuel = 0;
        for (BurnSequence burn : burns) {
            totalFuel += burn.getFuelConsumed();
        }
        this.totalFuelTons = totalFuel;
    }

    /**
     * Creates a simple point-to-point maneuver plan.
     * <p>
     * This convenience method creates a symmetric accelerate-decelerate plan
     * for the given distance and fusion rating.
     * </p>
     *
     * @param origin the origin description
     * @param destination the destination description
     * @param starSystem the star system
     * @param distanceAU the distance in AU
     * @param fusionRating the ship's fusion drive rating
     * @param totalTonnage the ship's total tonnage
     * @return a new maneuver plan
     */
    public static ManeuverPlan createPointToPoint(String origin, String destination,
                                                   TravellerWorld starSystem,
                                                   double distanceAU, int fusionRating,
                                                   int totalTonnage) {
        return createPointToPoint(origin, destination, starSystem, distanceAU,
                fusionRating, totalTonnage, false);
    }

    /**
     * Creates a point-to-point maneuver plan with optional landing.
     *
     * @param origin the origin description
     * @param destination the destination description
     * @param starSystem the star system
     * @param distanceAU the distance in AU
     * @param fusionRating the ship's fusion drive rating
     * @param totalTonnage the ship's total tonnage
     * @param includesLanding true if landing at destination
     * @return a new maneuver plan
     */
    public static ManeuverPlan createPointToPoint(String origin, String destination,
                                                   TravellerWorld starSystem,
                                                   double distanceAU, int fusionRating,
                                                   int totalTonnage, boolean includesLanding) {
        if (distanceAU < 0) {
            throw new IllegalArgumentException("Distance must be non-negative");
        }
        if (fusionRating < 1) {
            throw new IllegalArgumentException("Fusion rating must be >= 1 for maneuver");
        }

        List<BurnSequence> burns = new ArrayList<>();
        double velocity = 0;
        double cumulativeDistance = 0;
        int week = 1;

        // Calculate the midpoint distance
        double halfDistance = distanceAU / 2.0;

        // Acceleration phase
        while (cumulativeDistance + velocity + fusionRating <= distanceAU - 0.01) {
            // Check if we should start decelerating
            double remaining = distanceAU - cumulativeDistance;
            double velocityAfterAccel = velocity + fusionRating;
            double distanceAfterAccel = (velocity + velocityAfterAccel) / 2.0;
            double distanceNeededToStop = velocityAfterAccel * velocityAfterAccel / (2.0 * fusionRating);

            if (distanceAfterAccel + distanceNeededToStop > remaining + 0.01) {
                // Need to start decelerating or coast
                break;
            }

            burns.add(new BurnSequence(week, fusionRating, velocity, cumulativeDistance, totalTonnage));
            velocity = velocityAfterAccel;
            cumulativeDistance += distanceAfterAccel;
            week++;
        }

        // Deceleration phase — reduce velocity to 0 at destination
        while (velocity > 0.01 && cumulativeDistance < distanceAU - 0.01) {
            int thrust = Math.min(fusionRating, (int) Math.ceil(velocity));
            double decelThrust = -thrust;
            double velocityAfterDecel = Math.max(0, velocity + decelThrust);
            double distanceThisBurn = (velocity + velocityAfterDecel) / 2.0;

            // Adjust thrust if we'd overshoot
            if (cumulativeDistance + distanceThisBurn > distanceAU + 0.01) {
                // Reduce thrust to arrive exactly
                double neededDistance = distanceAU - cumulativeDistance;
                // Solve: (v + v+t)/2 = d, where t is negative
                // v + v + t = 2d → t = 2d - 2v
                decelThrust = 2.0 * neededDistance - 2.0 * velocity;
                thrust = (int) Math.round(-decelThrust);
                if (thrust > fusionRating) thrust = fusionRating;
                decelThrust = -thrust;
                velocityAfterDecel = Math.max(0, velocity + decelThrust);
                distanceThisBurn = (velocity + velocityAfterDecel) / 2.0;
            }

            burns.add(new BurnSequence(week, (int) decelThrust, velocity, cumulativeDistance, totalTonnage));
            velocity = velocityAfterDecel;
            cumulativeDistance += distanceThisBurn;
            week++;
        }

        if (burns.isEmpty()) {
            throw new IllegalArgumentException("Cannot create maneuver plan: distance too small or fusion rating too low");
        }

        return new ManeuverPlan(origin, destination, starSystem, burns, fusionRating, totalTonnage, includesLanding);
    }

    // ---- Accessors ----

    /**
     * @return the origin description
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @return the destination description
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @return the star system where this maneuver takes place
     */
    public TravellerWorld getStarSystem() {
        return starSystem;
    }

    /**
     * @return the ordered list of burn sequences (immutable)
     */
    public List<BurnSequence> getBurns() {
        return Collections.unmodifiableList(burns);
    }

    /**
     * @return the total distance in AU
     */
    public double getTotalDistanceAU() {
        return totalDistanceAU;
    }

    /**
     * @return the total fuel required in tons
     */
    public int getTotalFuelTons() {
        return totalFuelTons;
    }

    /**
     * @return the total travel time in weeks
     */
    public int getTotalWeeks() {
        return totalWeeks;
    }

    /**
     * @return the maximum velocity reached (AU/week)
     */
    public double getMaxVelocity() {
        return maxVelocity;
    }

    /**
     * @return the ship's fusion drive rating
     */
    public int getFusionRating() {
        return fusionRating;
    }

    /**
     * @return the ship's total tonnage
     */
    public int getTotalTonnage() {
        return totalTonnage;
    }

    /**
     * @return true if the plan includes landing at the destination
     */
    public boolean includesLanding() {
        return includesLanding;
    }

    // ---- Derived Properties ----

    /**
     * Returns the burn sequence for a given week.
     *
     * @param week the week number (1-based)
     * @return the burn sequence, or null if out of range
     */
    public BurnSequence getBurnForWeek(int week) {
        if (week < 1 || week > burns.size()) {
            return null;
        }
        return burns.get(week - 1);
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
     * Returns the fuel remaining after executing this plan.
     *
     * @param fuelAvailable the available fuel in tons
     * @return fuel remaining in tons (may be negative if insufficient)
     */
    public int getFuelRemaining(int fuelAvailable) {
        return fuelAvailable - totalFuelTons;
    }

    /**
     * Returns the total thrust used across all burns.
     * <p>
     * This is the sum of absolute thrust values, useful for estimating
     * engine wear or stress.
     * </p>
     *
     * @return total absolute thrust
     */
    public int getTotalThrust() {
        int total = 0;
        for (BurnSequence burn : burns) {
            total += Math.abs(burn.getThrust());
        }
        return total;
    }

    /**
     * Returns the number of acceleration burns.
     *
     * @return count of acceleration burns
     */
    public int getAccelerationBurnCount() {
        int count = 0;
        for (BurnSequence burn : burns) {
            if (burn.isAcceleration()) count++;
        }
        return count;
    }

    /**
     * Returns the number of deceleration burns.
     *
     * @return count of deceleration burns
     */
    public int getDecelerationBurnCount() {
        int count = 0;
        for (BurnSequence burn : burns) {
            if (burn.isDeceleration()) count++;
        }
        return count;
    }

    /**
     * Returns the estimated travel time in days.
     * <p>
     * Assumes 7 days per week.
     * </p>
     *
     * @return estimated travel time in days
     */
    public int getEstimatedDays() {
        return totalWeeks * 7;
    }

    /**
     * Validates that the plan arrives at zero velocity.
     *
     * @return true if the final velocity is approximately zero
     */
    public boolean arrivesAtRest() {
        if (burns.isEmpty()) return false;
        double finalVelocity = burns.get(burns.size() - 1).getVelocityEnd();
        return Math.abs(finalVelocity) < 0.1;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(origin).append(" → ").append(destination)
                .append(" (").append(String.format("%.1f", totalDistanceAU)).append(" AU)");
        sb.append("\n  Weeks: ").append(totalWeeks)
                .append(", Max V: ").append(String.format("%.1f", maxVelocity))
                .append(", Fuel: ").append(totalFuelTons).append("t");
        if (includesLanding) {
            sb.append(" [includes landing]");
        }
        return sb.toString();
    }
}
