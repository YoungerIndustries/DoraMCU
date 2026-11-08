package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Objects;

/**
 * Coordinates maneuver operations within a star system.
 * <p>
 * The ManeuverResolver is the main entry point for within-system travel.
 * It combines {@link ManeuverPlan} for travel between points,
 * {@link PlanetApproach} for landing/takeoff, and {@link ExplorationActivity}
 * for system exploration into a unified interface.
 * </p>
 * <p>
 * Usage:
 * </p>
 * <pre>
 *   // Create a resolver for a ship at a star system
 *   ManeuverResolver resolver = new ManeuverResolver(shipDesign, starSystem);
 *
 *   // Plan travel to a planet
 *   ManeuverPlan plan = resolver.planTravel("Star", "Planet A", 1.0);
 *
 *   // Execute the plan week by week
 *   while (resolver.hasActivePlan()) {
 *       resolver.executeNextWeek();
 *   }
 *
 *   // Land on the planet
 *   resolver.landOnPlanet("Planet A");
 *
 *   // Explore the system
 *   ExplorationActivity result = resolver.exploreSystem();
 * </pre>
 */
public class ManeuverResolver {

    /**
     * The ship's fusion drive rating.
     */
    private final int fusionRating;

    /**
     * The ship's total tonnage.
     */
    private final int totalTonnage;

    /**
     * The ship's jump drive rating (for reference).
     */
    private final int jumpRating;

    /**
     * The ship's computer class (affects exploration).
     */
    private final int computerClass;

    /**
     * The current star system.
     */
    private final TravellerWorld starSystem;

    /**
     * The ship's fuel tank.
     */
    private final FuelTank fuelTank;

    /**
     * The current maneuver status.
     */
    private final ManeuverStatus maneuverStatus;

    /**
     * Random number generator for exploration.
     */
    private final Random random;

    /**
     * History of completed activities.
     */
    private final List<String> activityLog;

    /**
     * Creates a new maneuver resolver.
     *
     * @param fusionRating the ship's fusion drive rating
     * @param jumpRating the ship's jump drive rating
     * @param totalTonnage the ship's total tonnage
     * @param computerClass the ship's computer class (0-4)
     * @param starSystem the current star system
     * @param fuelCapacityTons the fuel tank capacity in tons
     * @param seed random seed for reproducibility (0 for random)
     */
    public ManeuverResolver(int fusionRating, int jumpRating, int totalTonnage,
                             int computerClass, TravellerWorld starSystem,
                             int fuelCapacityTons, long seed) {
        this.fusionRating = fusionRating;
        this.jumpRating = jumpRating;
        this.totalTonnage = totalTonnage;
        this.computerClass = computerClass;
        this.starSystem = Objects.requireNonNull(starSystem, "Star system must not be null");

        int fuelPerJump = Math.max(1000, totalTonnage / 100);
        this.fuelTank = new FuelTank(fuelCapacityTons, fuelCapacityTons, fuelPerJump);
        this.maneuverStatus = new ManeuverStatus(starSystem, fusionRating, totalTonnage,
                fuelTank, 0);
        this.random = seed == 0 ? new Random() : new Random(seed);
        this.activityLog = new ArrayList<>();
    }

    /**
     * Creates a new maneuver resolver from a ShipDesign.
     *
     * @param design the ship design
     * @param starSystem the current star system
     * @param seed random seed (0 for random)
     */
    public ManeuverResolver(ShipDesign design, TravellerWorld starSystem, long seed) {
        this(
                design.getFusionDrive().getRating(),
                design.getJumpDrive().getRating(),
                design.getTotalTonnage(),
                design.getComputer() != null ? design.getComputer().getClassLevel() : 0,
                Objects.requireNonNull(starSystem),
                design.getFuelTons(),
                seed
        );
    }

    // ---- Travel Planning ----

    /**
     * Plans travel between two points within the system.
     *
     * @param origin the origin description
     * @param destination the destination description
     * @param distanceAU the distance in AU
     * @return a maneuver plan
     */
    public ManeuverPlan planTravel(String origin, String destination, double distanceAU) {
        return ManeuverPlan.createPointToPoint(origin, destination, starSystem,
                distanceAU, fusionRating, totalTonnage);
    }

    /**
     * Plans travel with landing at the destination.
     *
     * @param origin the origin description
     * @param destination the destination description
     * @param distanceAU the distance in AU
     * @return a maneuver plan including landing
     */
    public ManeuverPlan planTravelWithLanding(String origin, String destination, double distanceAU) {
        return ManeuverPlan.createPointToPoint(origin, destination, starSystem,
                distanceAU, fusionRating, totalTonnage, true);
    }

    /**
     * Starts executing a maneuver plan.
     *
     * @param plan the plan to execute
     * @return true if the plan was started successfully
     */
    public boolean startTravel(ManeuverPlan plan) {
        boolean started = maneuverStatus.startManeuver(plan);
        if (started) {
            activityLog.add("Started travel: " + plan.getOrigin() + " → " + plan.getDestination());
        }
        return started;
    }

    /**
     * Executes the next week of the current maneuver plan.
     *
     * @return true if a burn was executed, false if no active plan or complete
     */
    public boolean executeNextWeek() {
        boolean executed = maneuverStatus.executeNextBurn();
        if (executed) {
            BurnSequence burn = maneuverStatus.getCurrentPlan() != null ?
                    maneuverStatus.getCurrentPlan().getBurnForWeek(maneuverStatus.getCurrentWeekIndex()) : null;
            if (burn != null) {
                activityLog.add(burn.toString());
            }
        }
        return executed;
    }

    /**
     * Aborts the current maneuver plan.
     */
    public void abortTravel() {
        maneuverStatus.abortManeuver();
        activityLog.add("Aborted current travel plan");
    }

    // ---- Planet Approach ----

    /**
     * Creates a planet approach for the given world.
     *
     * @return the planet approach
     */
    public PlanetApproach getPlanetApproach() {
        return new PlanetApproach(starSystem);
    }

    /**
     * Enters orbit around the current system's primary world.
     *
     * @return true if orbit was entered successfully
     */
    public boolean enterOrbit() {
        PlanetApproach approach = getPlanetApproach();
        int fuelNeeded = approach.getOrbitFuelTons(totalTonnage);

        if (fuelTank.getCurrentFuelTons() < fuelNeeded) {
            activityLog.add("Failed to enter orbit: insufficient fuel");
            return false;
        }

        if (!fuelTank.consumeFuel(fuelNeeded)) {
            activityLog.add("Failed to enter orbit: fuel consumption failed");
            return false;
        }

        maneuverStatus.enterOrbit(starSystem.getName());
        activityLog.add("Entered orbit around " + starSystem.getName() +
                " (fuel cost: " + fuelNeeded + " tons)");
        return true;
    }

    /**
     * Lands on the current system's primary world.
     *
     * @return true if landing was successful
     */
    public boolean landOnPlanet() {
        return landOnPlanet(starSystem.getName());
    }

    /**
     * Lands on a named celestial body.
     *
     * @param bodyName the name of the body to land on
     * @return true if landing was successful
     */
    public boolean landOnPlanet(String bodyName) {
        Objects.requireNonNull(bodyName, "Body name must not be null");

        // Must be in orbit first
        if (!maneuverStatus.isInGravityWell()) {
            activityLog.add("Cannot land: not in orbit");
            return false;
        }

        PlanetApproach approach = getPlanetApproach();
        if (!approach.isLandingPossible()) {
            activityLog.add("Cannot land: no solid surface");
            return false;
        }

        int fuelNeeded = approach.getLandingFuelTons(totalTonnage);
        if (fuelTank.getCurrentFuelTons() < fuelNeeded) {
            activityLog.add("Cannot land: insufficient fuel");
            return false;
        }

        if (!fuelTank.consumeFuel(fuelNeeded)) {
            activityLog.add("Landing failed: fuel consumption error");
            return false;
        }

        maneuverStatus.land(bodyName);
        activityLog.add("Landed on " + bodyName + " (fuel cost: " + fuelNeeded + " tons)");
        return true;
    }

    /**
     * Takes off from the current location.
     *
     * @return true if takeoff was successful
     */
    public boolean takeOff() {
        PlanetApproach approach = getPlanetApproach();
        int fuelNeeded;

        if (maneuverStatus.isLanded()) {
            fuelNeeded = approach.getTakeoffFuelPoints() * Math.max(1000, totalTonnage / 100);
        } else if (maneuverStatus.isInGravityWell()) {
            fuelNeeded = approach.getDepartureFuelPoints() * Math.max(1000, totalTonnage / 100);
        } else {
            activityLog.add("Cannot take off: already in free space");
            return false;
        }

        if (fuelTank.getCurrentFuelTons() < fuelNeeded) {
            activityLog.add("Cannot take off: insufficient fuel");
            return false;
        }

        if (!fuelTank.consumeFuel(fuelNeeded)) {
            activityLog.add("Takeoff failed: fuel consumption error");
            return false;
        }

        maneuverStatus.takeOff();
        activityLog.add("Took off (fuel cost: " + fuelNeeded + " tons)");
        return true;
    }

    // ---- Exploration ----

    /**
     * Performs a full system survey.
     *
     * @return the exploration result
     */
    public ExplorationActivity exploreSystem() {
        ExplorationActivity activity = ExplorationActivity.createFullSystemSurvey(
                starSystem, computerClass >= 2);
        ExplorationActivity result = activity.execute(random);
        activityLog.add(result.toString());
        return result;
    }

    /**
     * Surveys a specific celestial body.
     *
     * @param bodyName the name of the body to survey
     * @return the exploration result
     */
    public ExplorationActivity surveyBody(String bodyName) {
        Objects.requireNonNull(bodyName, "Body name must not be null");
        ExplorationActivity activity = ExplorationActivity.createSurvey(
                bodyName, starSystem, computerClass >= 2);
        ExplorationActivity result = activity.execute(random);
        activityLog.add(result.toString());
        return result;
    }

    /**
     * Scans for resources in a specific area.
     *
     * @param area the area to scan
     * @return the exploration result
     */
    public ExplorationActivity scanResources(String area) {
        Objects.requireNonNull(area, "Area must not be null");
        ExplorationActivity activity = ExplorationActivity.createResourceScan(
                area, starSystem, computerClass >= 2);
        ExplorationActivity result = activity.execute(random);
        activityLog.add(result.toString());
        return result;
    }

    /**
     * Scans for hazards in a specific area.
     *
     * @param area the area to scan
     * @return the exploration result
     */
    public ExplorationActivity scanHazards(String area) {
        Objects.requireNonNull(area, "Area must not be null");
        ExplorationActivity activity = ExplorationActivity.createHazardDetection(
                area, starSystem, computerClass >= 2);
        ExplorationActivity result = activity.execute(random);
        activityLog.add(result.toString());
        return result;
    }

    // ---- Refueling ----

    /**
     * Refuels the ship at the current location.
     * <p>
     * Requires a starport at the current system.
     * </p>
     *
     * @param tons the amount of fuel to add
     * @return the actual amount added
     */
    public int refuel(int tons) {
        PlanetApproach approach = getPlanetApproach();
        if (!approach.hasStarport()) {
            activityLog.add("Cannot refuel: no starport available");
            return 0;
        }
        int added = maneuverStatus.refuel(tons);
        if (added > 0) {
            activityLog.add("Refueled: +" + added + " tons");
        }
        return added;
    }

    /**
     * Refuels the ship to maximum capacity.
     *
     * @return the amount added
     */
    public int refuelToMax() {
        int space = fuelTank.getMaxCapacityTons() - fuelTank.getCurrentFuelTons();
        return refuel(space);
    }

    // ---- Status Queries ----

    /**
     * @return true if there is an active maneuver plan
     */
    public boolean hasActivePlan() {
        return maneuverStatus.getCurrentPlan() != null;
    }

    /**
     * @return the current maneuver status
     */
    public ManeuverStatus getManeuverStatus() {
        return maneuverStatus;
    }

    /**
     * @return the current fuel level in tons
     */
    public int getCurrentFuel() {
        return fuelTank.getCurrentFuelTons();
    }

    /**
     * @return the fuel capacity in tons
     */
    public int getFuelCapacity() {
        return fuelTank.getMaxCapacityTons();
    }

    /**
     * @return the activity log (immutable)
     */
    public List<String> getActivityLog() {
        return Collections.unmodifiableList(activityLog);
    }

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
     * Checks if the ship can reach a given distance with current fuel.
     *
     * @param distanceAU the distance in AU
     * @return true if the ship has enough fuel
     */
    public boolean canReachDistance(double distanceAU) {
        try {
            ManeuverPlan plan = planTravel("Current", "Target", distanceAU);
            return fuelTank.getCurrentFuelTons() >= plan.getTotalFuelTons();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return "ManeuverResolver{" +
                "system=" + starSystem.getName() +
                ", fusion=" + fusionRating + "M" +
                ", fuel=" + getCurrentFuel() + "/" + getFuelCapacity() + "t" +
                ", status=" + maneuverStatus +
                '}';
    }
}
