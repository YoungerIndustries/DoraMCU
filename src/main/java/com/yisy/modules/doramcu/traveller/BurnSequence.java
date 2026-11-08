package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * A single maneuver burn from Classic Traveller Book 2.
 * <p>
 * In Classic Traveller, within-system travel uses fusion drives to accelerate
 * at 1G per point of fusion rating. Each burn lasts one week (the standard
 * maneuver period), and a ship can accelerate, decelerate, or coast each week.
 * </p>
 * <p>
 * Each burn has:
 * </p>
 * <ul>
 *   <li>Thrust applied (0 to fusion rating)</li>
 *   <li>Direction: acceleration toward target or deceleration away from it</li>
 *   <li>Distance covered during the burn (in AU)</li>
 *   <li>Velocity change (in AU per week)</li>
 *   <li>Fuel consumed (1% of ship tonnage per point of thrust)</li>
 * </ul>
 * <p>
 * The distance covered during a burn follows the formula:
 * </p>
 * <p>
 * Distance = (Velocity_start + Velocity_end) / 2
 * </p>
 * <p>
 * Where velocity changes by the thrust amount each week.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Week 1: Accelerate 3M from rest → V=3, Distance=1.5 AU
 *   Week 2: Accelerate 3M → V=6, Distance=4.5 AU
 *   Week 3: Decelerate 3M → V=3, Distance=4.5 AU
 *   Week 4: Decelerate 3M → V=0, Distance=1.5 AU (arrival)
 * </pre>
 */
public class BurnSequence {

    /**
     * The thrust applied during this burn (0 to fusion rating).
     * Positive values accelerate toward the target.
     * Negative values decelerate (reduce velocity).
     */
    private final int thrust;

    /**
     * The velocity at the start of this burn (in AU per week).
     */
    private final double velocityStart;

    /**
     * The velocity at the end of this burn (in AU per week).
     */
    private final double velocityEnd;

    /**
     * The distance covered during this burn (in AU).
     */
    private final double distanceCovered;

    /**
     * The cumulative distance from the starting point after this burn (in AU).
     */
    private final double cumulativeDistance;

    /**
     * Fuel consumed during this burn (in tons).
     */
    private final int fuelConsumed;

    /**
     * The week number of this burn (1-based).
     */
    private final int weekNumber;

    /**
     * True if this burn is an acceleration (positive thrust).
     */
    private final boolean isAcceleration;

    /**
     * Creates a new burn sequence.
     *
     * @param weekNumber the week number (1-based)
     * @param thrust the thrust applied (0 to fusion rating; negative for deceleration)
     * @param velocityStart the velocity at the start of this burn
     * @param cumulativeDistanceBefore the cumulative distance before this burn
     * @param totalTonnage the ship's total tonnage (for fuel calculation)
     */
    public BurnSequence(int weekNumber, int thrust, double velocityStart,
                         double cumulativeDistanceBefore, int totalTonnage) {
        if (weekNumber < 1) {
            throw new IllegalArgumentException("Week number must be >= 1");
        }
        this.weekNumber = weekNumber;
        this.thrust = thrust;
        this.velocityStart = velocityStart;
        this.isAcceleration = thrust > 0;

        // Calculate velocity at end of burn
        this.velocityEnd = velocityStart + thrust;

        // Distance = average velocity × 1 week
        this.distanceCovered = (velocityStart + velocityEnd) / 2.0;

        // Cumulative distance
        this.cumulativeDistance = cumulativeDistanceBefore + distanceCovered;

        // Fuel: 1% of tonnage per point of thrust (minimum 1000 tons per point)
        int absThrust = Math.abs(thrust);
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        this.fuelConsumed = tonsPerFuel * absThrust;
    }

    /**
     * Creates a burn sequence with zero fuel consumption (for planning purposes).
     *
     * @param weekNumber the week number (1-based)
     * @param thrust the thrust applied
     * @param velocityStart the velocity at the start
     * @param cumulativeDistanceBefore the cumulative distance before this burn
     */
    public BurnSequence(int weekNumber, int thrust, double velocityStart,
                         double cumulativeDistanceBefore) {
        this(weekNumber, thrust, velocityStart, cumulativeDistanceBefore, 100000);
    }

    // ---- Accessors ----

    /**
     * @return the week number (1-based)
     */
    public int getWeekNumber() {
        return weekNumber;
    }

    /**
     * @return the thrust applied (positive for acceleration, negative for deceleration)
     */
    public int getThrust() {
        return thrust;
    }

    /**
     * @return the velocity at the start of this burn (AU/week)
     */
    public double getVelocityStart() {
        return velocityStart;
    }

    /**
     * @return the velocity at the end of this burn (AU/week)
     */
    public double getVelocityEnd() {
        return velocityEnd;
    }

    /**
     * @return the distance covered during this burn (AU)
     */
    public double getDistanceCovered() {
        return distanceCovered;
    }

    /**
     * @return the cumulative distance from start after this burn (AU)
     */
    public double getCumulativeDistance() {
        return cumulativeDistance;
    }

    /**
     * @return the fuel consumed during this burn (tons)
     */
    public int getFuelConsumed() {
        return fuelConsumed;
    }

    /**
     * @return true if this burn accelerates toward the target
     */
    public boolean isAcceleration() {
        return isAcceleration;
    }

    /**
     * @return true if this burn decelerates (reduces velocity)
     */
    public boolean isDeceleration() {
        return thrust < 0;
    }

    /**
     * @return true if this burn is a coast (zero thrust)
     */
    public boolean isCoast() {
        return thrust == 0;
    }

    // ---- Derived Properties ----

    /**
     * Returns the average velocity during this burn.
     *
     * @return average velocity (AU/week)
     */
    public double getAverageVelocity() {
        return distanceCovered; // Distance = avg velocity × 1 week
    }

    /**
     * Returns the velocity change during this burn.
     *
     * @return velocity change (AU/week)
     */
    public double getVelocityChange() {
        return velocityEnd - velocityStart;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Week ").append(weekNumber).append(": ");
        if (isAcceleration) {
            sb.append("Accel +").append(thrust);
        } else if (isDeceleration()) {
            sb.append("Decel ").append(thrust);
        } else {
            sb.append("Coast");
        }
        sb.append(" | V:").append(String.format("%.1f", velocityStart))
                .append("→").append(String.format("%.1f", velocityEnd))
                .append(" | D:").append(String.format("%.1f", distanceCovered))
                .append(" AU | Fuel:").append(fuelConsumed).append("t");
        return sb.toString();
    }
}
