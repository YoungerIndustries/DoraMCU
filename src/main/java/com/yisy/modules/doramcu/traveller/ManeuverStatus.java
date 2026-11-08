package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tracks a ship's current maneuver state within a star system.
 * <p>
 * In Classic Traveller Book 2, a ship's maneuver state determines what actions
 * are available. A ship at rest can begin a new maneuver plan. A ship in motion
 * is executing burns and consuming fuel.
 * </p>
 * <p>
 * The maneuver status tracks:
 * </p>
 * <ul>
 *   <li>Current position (celestial body or coordinates)</li>
 *   <li>Current velocity (AU per week)</li>
 *   <li>Current acceleration (G)</li>
 *   <li>Active maneuver plan, if any</li>
 *   <li>Progress through the current plan (current week/burn)</li>
 *   <li>Fuel tank status</li>
 *   <li>Travel history within the system</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Ship "Explorer" at 5.2 AU from star, V=3.0, accelerating at 3G
 *   Week 2 of 4-week plan to Planet B
 *   Fuel: 1500/3000 tons
 * </pre>
 */
public class ManeuverStatus {

    /**
     * The current star system.
     */
    private final TravellerWorld starSystem;

    /**
     * The ship's fusion drive rating.
     */
    private final int fusionRating;

    /**
     * The ship's total tonnage.
     */
    private final int totalTonnage;

    /**
     * Current distance from the star (in AU).
     */
    private double distanceFromStar;

    /**
     * Current velocity relative to the star (AU per week).
     * Positive = moving away, negative = moving toward.
     */
    private double velocity;

    /**
     * Current acceleration in G (0 to fusion rating).
     */
    private int currentAcceleration;

    /**
     * The ship's fuel tank.
     */
    private final FuelTank fuelTank;

    /**
     * The active maneuver plan, if any.
     */
    private ManeuverPlan currentPlan;

    /**
     * Current week index in the active plan (0-based).
     * -1 if no active plan.
     */
    private int currentWeekIndex;

    /**
     * True if the ship is currently in a gravity well (orbited or landed).
     */
    private boolean inGravityWell;

    /**
     * The celestial body currently orbited, if any.
     */
    private String orbitedBody;

    /**
     * True if the ship is currently landed on a surface.
     */
    private boolean landed;

    /**
     * History of visited locations within the system.
     */
    private final List<String> visitedLocations;

    /**
     * Creates a new maneuver status for a ship.
     *
     * @param starSystem the current star system
     * @param fusionRating the ship's fusion drive rating
     * @param totalTonnage the ship's total tonnage
     * @param fuelTank the ship's fuel tank
     * @param startingDistance the starting distance from the star (AU)
     */
    public ManeuverStatus(TravellerWorld starSystem, int fusionRating,
                           int totalTonnage, FuelTank fuelTank,
                           double startingDistance) {
        this.starSystem = Objects.requireNonNull(starSystem, "Star system must not be null");
        this.fusionRating = fusionRating;
        this.totalTonnage = totalTonnage;
        this.fuelTank = Objects.requireNonNull(fuelTank, "Fuel tank must not be null");
        this.distanceFromStar = startingDistance;
        this.velocity = 0;
        this.currentAcceleration = 0;
        this.currentPlan = null;
        this.currentWeekIndex = -1;
        this.inGravityWell = false;
        this.orbitedBody = null;
        this.landed = false;
        this.visitedLocations = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * @return the current star system
     */
    public TravellerWorld getStarSystem() {
        return starSystem;
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
     * @return the current distance from the star (AU)
     */
    public double getDistanceFromStar() {
        return distanceFromStar;
    }

    /**
     * @return the current velocity (AU/week)
     */
    public double getVelocity() {
        return velocity;
    }

    /**
     * @return the current acceleration (G)
     */
    public int getCurrentAcceleration() {
        return currentAcceleration;
    }

    /**
     * @return the ship's fuel tank
     */
    public FuelTank getFuelTank() {
        return fuelTank;
    }

    /**
     * @return the active maneuver plan, or null if none
     */
    public ManeuverPlan getCurrentPlan() {
        return currentPlan;
    }

    /**
     * @return the current week index in the plan, or -1 if no active plan
     */
    public int getCurrentWeekIndex() {
        return currentWeekIndex;
    }

    /**
     * @return true if the ship is in a gravity well
     */
    public boolean isInGravityWell() {
        return inGravityWell;
    }

    /**
     * @return the orbited body name, or null if not orbiting
     */
    public String getOrbitedBody() {
        return orbitedBody;
    }

    /**
     * @return true if the ship is currently landed
     */
    public boolean isLanded() {
        return landed;
    }

    /**
     * @return the list of visited locations (immutable)
     */
    public List<String> getVisitedLocations() {
        return Collections.unmodifiableList(visitedLocations);
    }

    // ---- Derived Properties ----

    /**
     * @return true if the ship is at rest (velocity ≈ 0)
     */
    public boolean isAtRest() {
        return Math.abs(velocity) < 0.1;
    }

    /**
     * @return true if the ship is in motion
     */
    public boolean isInMotion() {
        return Math.abs(velocity) >= 0.1;
    }

    /**
     * @return true if the ship can start a new maneuver plan
     */
    public boolean canStartManeuver() {
        return isAtRest() && !landed && currentPlan == null;
    }

    /**
     * @return true if the ship can perform a burn this week
     */
    public boolean canBurn() {
        return currentPlan != null && currentWeekIndex >= 0 &&
                currentWeekIndex < currentPlan.getTotalWeeks() &&
                !fuelTank.isEmpty();
    }

    /**
     * @return the number of weeks remaining in the current plan
     */
    public int getWeeksRemaining() {
        if (currentPlan == null) return 0;
        return currentPlan.getTotalWeeks() - currentWeekIndex;
    }

    /**
     * @return the number of weeks completed in the current plan
     */
    public int getWeeksCompleted() {
        if (currentPlan == null) return 0;
        return currentWeekIndex;
    }

    /**
     * @return true if the ship is stranded (no fuel and cannot maneuver)
     */
    public boolean isStranded() {
        return fuelTank.isEmpty() && !isInMotion();
    }

    // ---- Maneuver Operations ----

    /**
     * Starts a new maneuver plan.
     * <p>
     * The ship must be at rest and not landed.
     * </p>
     *
     * @param plan the maneuver plan to execute
     * @return true if the plan was started, false if conditions not met
     */
    public boolean startManeuver(ManeuverPlan plan) {
        Objects.requireNonNull(plan, "Maneuver plan must not be null");

        if (!canStartManeuver()) {
            return false;
        }

        if (!fuelTank.hasEnoughFuelForPlan(plan)) {
            return false;
        }

        this.currentPlan = plan;
        this.currentWeekIndex = 0;
        return true;
    }

    /**
     * Executes the next burn in the current maneuver plan.
     * <p>
     * Advances one week, applies thrust, updates velocity and position,
     * and consumes fuel.
     * </p>
     *
     * @return true if the burn was executed, false if conditions not met
     */
    public boolean executeNextBurn() {
        if (!canBurn()) {
            return false;
        }

        BurnSequence burn = currentPlan.getBurnForWeek(currentWeekIndex + 1);
        if (burn == null) {
            completeManeuver();
            return false;
        }

        // Apply thrust
        this.currentAcceleration = Math.abs(burn.getThrust());
        this.velocity = burn.getVelocityEnd();
        this.distanceFromStar = burn.getCumulativeDistance();

        // Consume fuel
        if (!fuelTank.consumeFuel(burn.getFuelConsumed())) {
            // Out of fuel — abort
            this.currentAcceleration = 0;
            abortManeuver();
            return false;
        }

        // Advance week
        currentWeekIndex++;

        // Check if plan is complete
        if (currentWeekIndex >= currentPlan.getTotalWeeks()) {
            completeManeuver();
        }

        return true;
    }

    /**
     * Completes the current maneuver plan.
     */
    private void completeManeuver() {
        String destination = currentPlan.getDestination();
        visitedLocations.add(destination);

        this.currentAcceleration = 0;
        this.velocity = 0;
        this.currentPlan = null;
        this.currentWeekIndex = -1;
    }

    /**
     * Aborts the current maneuver plan.
     * <p>
     * The ship continues at current velocity (coasting).
     * </p>
     */
    public void abortManeuver() {
        this.currentPlan = null;
        this.currentWeekIndex = -1;
        this.currentAcceleration = 0;
    }

    /**
     * Enters orbit around a celestial body.
     *
     * @param body the name of the celestial body
     */
    public void enterOrbit(String body) {
        Objects.requireNonNull(body, "Body must not be null");
        this.orbitedBody = body;
        this.inGravityWell = true;
        this.velocity = 0;
        this.currentAcceleration = 0;
        visitedLocations.add("Orbit: " + body);
    }

    /**
     * Lands on a celestial body's surface.
     *
     * @param body the name of the celestial body
     */
    public void land(String body) {
        Objects.requireNonNull(body, "Body must not be null");
        this.orbitedBody = body;
        this.inGravityWell = true;
        this.landed = true;
        this.velocity = 0;
        this.currentAcceleration = 0;
        visitedLocations.add("Landed: " + body);
    }

    /**
     * Takes off from the current location.
     * <p>
     * The ship leaves the gravity well and returns to free space.
     * </p>
     */
    public void takeOff() {
        if (orbitedBody != null) {
            visitedLocations.add("Departed: " + orbitedBody);
        }
        this.landed = false;
        this.inGravityWell = false;
        this.orbitedBody = null;
        this.velocity = 0;
        this.currentAcceleration = 0;
    }

    /**
     * Refuels the ship.
     * <p>
     * The ship must be at rest or in a gravity well.
     * </p>
     *
     * @param tons the amount of fuel to add
     * @return the actual amount added
     */
    public int refuel(int tons) {
        if (isInMotion() && currentPlan != null) {
            return 0; // Cannot refuel during active maneuver
        }
        return fuelTank.refuel(tons);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("System: ").append(starSystem.getName())
                .append(", Distance: ").append(String.format("%.1f", distanceFromStar)).append(" AU")
                .append(", V: ").append(String.format("%.1f", velocity))
                .append(", Accel: ").append(currentAcceleration).append("G");

        if (inGravityWell) {
            sb.append(", Orbit: ").append(orbitedBody);
            if (landed) {
                sb.append(" [LANDED]");
            }
        }

        if (currentPlan != null) {
            sb.append(", Plan: Week ").append(currentWeekIndex + 1)
                    .append("/").append(currentPlan.getTotalWeeks());
        }

        sb.append(", Fuel: ").append(fuelTank);
        if (isStranded()) {
            sb.append(" [STRANDED]");
        }

        return sb.toString();
    }
}
