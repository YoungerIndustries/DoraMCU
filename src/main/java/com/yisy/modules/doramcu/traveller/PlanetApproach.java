package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * Planet approach and landing mechanics from Classic Traveller Book 2.
 * <p>
 * In Classic Traveller, entering a gravity well requires fuel based on the
 * world's gravity level. The gravity well depth is determined by the UWP
 * gravity code (0-10, where A=10). Landing requires additional fuel and
 * may require a landing skill check for difficult worlds.
 * </p>
 * <p>
 * Gravity well fuel costs:
 * </p>
 * <ul>
 *   <li>Gravity 0-2: 1 point of fuel to enter orbit</li>
 *   <li>Gravity 3-6: 2 points of fuel to enter orbit</li>
 *   <li>Gravity 7-8: 3 points of fuel to enter orbit</li>
 *   <li>Gravity 9-A: 4 points of fuel to enter orbit</li>
 * </ul>
 * <p>
 * Landing requires an additional 1 point of fuel.
 * Takeoff requires 1 point of fuel to leave the gravity well.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   World with gravity 4 (1G): 2 fuel points to orbit, 1 more to land
 *   Total: 3 fuel points = 3% of ship tonnage
 * </pre>
 */
public class PlanetApproach {

    /**
     * The target world for approach/landing.
     */
    private final TravellerWorld targetWorld;

    /**
     * The gravity well depth (from UWP gravity code).
     */
    private final int gravityWellDepth;

    /**
     * Fuel points required to enter orbit.
     */
    private final int orbitFuelPoints;

    /**
     * Fuel points required to land (additional to orbit).
     */
    private final int landingFuelPoints;

    /**
     * Fuel points required to take off from surface.
     */
    private final int takeoffFuelPoints;

    /**
     * Fuel points required to leave the gravity well completely.
     */
    private final int departureFuelPoints;

    /**
     * True if landing is possible on this world.
     */
    private final boolean landingPossible;

    /**
     * True if the world has a starport for refueling.
     */
    private final boolean hasStarport;

    /**
     * Starport class (0-10, lower is better).
     */
    private final int starportClass;

    /**
     * Creates a new planet approach for the given world.
     *
     * @param targetWorld the world to approach
     */
    public PlanetApproach(TravellerWorld targetWorld) {
        this.targetWorld = Objects.requireNonNull(targetWorld, "Target world must not be null");

        UniversalWorldProfile uwp = targetWorld.getUwp();
        this.gravityWellDepth = uwp.getGravity();
        this.orbitFuelPoints = calculateOrbitFuelPoints(gravityWellDepth);
        this.landingFuelPoints = 1; // Always 1 point to land from orbit
        this.takeoffFuelPoints = 1; // Always 1 point to take off
        this.departureFuelPoints = calculateDepartureFuelPoints(gravityWellDepth);

        // Landing is possible if the world has a surface (not a gas giant)
        this.landingPossible = uwp.getPlanetSize() <= 6; // 0-6 = solid surface

        // Starport information
        this.hasStarport = uwp.hasStarport();
        this.starportClass = uwp.getStarport();
    }

    /**
     * Calculates the fuel points needed to enter orbit.
     * <p>
     * Based on gravity well depth from Classic Traveller rules.
     * </p>
     *
     * @param gravity the UWP gravity code (0-10)
     * @return fuel points required
     */
    private int calculateOrbitFuelPoints(int gravity) {
        if (gravity <= 2) return 1;
        if (gravity <= 6) return 2;
        if (gravity <= 8) return 3;
        return 4; // Gravity 9-A
    }

    /**
     * Calculates the fuel points needed to leave the gravity well.
     * <p>
     * Same as orbit fuel points — you need the same thrust to escape
     * as you need to enter.
     * </p>
     *
     * @param gravity the UWP gravity code (0-10)
     * @return fuel points required
     */
    private int calculateDepartureFuelPoints(int gravity) {
        return calculateOrbitFuelPoints(gravity);
    }

    // ---- Accessors ----

    /**
     * @return the target world
     */
    public TravellerWorld getTargetWorld() {
        return targetWorld;
    }

    /**
     * @return the gravity well depth (0-10)
     */
    public int getGravityWellDepth() {
        return gravityWellDepth;
    }

    /**
     * @return fuel points to enter orbit
     */
    public int getOrbitFuelPoints() {
        return orbitFuelPoints;
    }

    /**
     * @return fuel points to land (additional to orbit)
     */
    public int getLandingFuelPoints() {
        return landingFuelPoints;
    }

    /**
     * @return fuel points to take off from surface
     */
    public int getTakeoffFuelPoints() {
        return takeoffFuelPoints;
    }

    /**
     * @return fuel points to leave the gravity well completely
     */
    public int getDepartureFuelPoints() {
        return departureFuelPoints;
    }

    /**
     * @return true if landing is possible
     */
    public boolean isLandingPossible() {
        return landingPossible;
    }

    /**
     * @return true if the world has a starport
     */
    public boolean hasStarport() {
        return hasStarport;
    }

    /**
     * @return the starport class (0-10, lower is better)
     */
    public int getStarportClass() {
        return starportClass;
    }

    // ---- Derived Properties ----

    /**
     * Returns the total fuel points to land from free space.
     * <p>
     * Orbit + Landing fuel points.
     * </p>
     *
     * @return total fuel points
     */
    public int getTotalLandingFuelPoints() {
        return orbitFuelPoints + landingFuelPoints;
    }

    /**
     * Returns the total fuel points to depart from surface to free space.
     * <p>
     * Takeoff + Departure fuel points.
     * </p>
     *
     * @return total fuel points
     */
    public int getTotalDepartureFuelPoints() {
        return takeoffFuelPoints + departureFuelPoints;
    }

    /**
     * Calculates the fuel cost in tons for entering orbit.
     *
     * @param totalTonnage the ship's total tonnage
     * @return fuel cost in tons
     */
    public int getOrbitFuelTons(int totalTonnage) {
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        return orbitFuelPoints * tonsPerFuel;
    }

    /**
     * Calculates the fuel cost in tons for landing.
     *
     * @param totalTonnage the ship's total tonnage
     * @return fuel cost in tons
     */
    public int getLandingFuelTons(int totalTonnage) {
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        return landingFuelPoints * tonsPerFuel;
    }

    /**
     * Calculates the total fuel cost in tons to land from free space.
     *
     * @param totalTonnage the ship's total tonnage
     * @return fuel cost in tons
     */
    public int getTotalLandingFuelTons(int totalTonnage) {
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        return getTotalLandingFuelPoints() * tonsPerFuel;
    }

    /**
     * Calculates the fuel cost in tons to depart from surface.
     *
     * @param totalTonnage the ship's total tonnage
     * @return fuel cost in tons
     */
    public int getTotalDepartureFuelTons(int totalTonnage) {
        int tonsPerFuel = Math.max(1000, totalTonnage / 100);
        return getTotalDepartureFuelPoints() * tonsPerFuel;
    }

    /**
     * Checks if a ship has enough fuel to land.
     *
     * @param fuelAvailable the available fuel in tons
     * @param totalTonnage the ship's total tonnage
     * @return true if the ship can land
     */
    public boolean canLand(int fuelAvailable, int totalTonnage) {
        return landingPossible && fuelAvailable >= getTotalLandingFuelTons(totalTonnage);
    }

    /**
     * Checks if a ship has enough fuel to enter orbit.
     *
     * @param fuelAvailable the available fuel in tons
     * @param totalTonnage the ship's total tonnage
     * @return true if the ship can enter orbit
     */
    public boolean canEnterOrbit(int fuelAvailable, int totalTonnage) {
        return fuelAvailable >= getOrbitFuelTons(totalTonnage);
    }

    /**
     * Checks if a ship has enough fuel to depart.
     *
     * @param fuelAvailable the available fuel in tons
     * @param totalTonnage the ship's total tonnage
     * @return true if the ship can depart
     */
    public boolean canDepart(int fuelAvailable, int totalTonnage) {
        return fuelAvailable >= getTotalDepartureFuelTons(totalTonnage);
    }

    /**
     * Returns the difficulty of landing on this world.
     * <p>
     * Higher gravity and poor starport increase difficulty.
     * </p>
     *
     * @return difficulty level (1-5, higher is harder)
     */
    public int getLandingDifficulty() {
        int difficulty = 1;

        // Gravity increases difficulty
        if (gravityWellDepth >= 7) difficulty += 2;
        else if (gravityWellDepth >= 4) difficulty += 1;

        // Poor starport increases difficulty
        if (starportClass >= 3) difficulty += 1;
        if (starportClass >= 5) difficulty += 1; // No port

        // No atmosphere but trying to land on solid world
        if (!targetWorld.getUwp().hasBreathableAtmosphere() && landingPossible) {
            difficulty += 1;
        }

        return Math.min(5, difficulty);
    }

    /**
     * Returns a description of the starport quality.
     *
     * @return starport quality description
     */
    public String getStarportQuality() {
        if (!hasStarport) return "No starport";
        switch (starportClass) {
            case 0: return "Excellent";
            case 1: return "Good";
            case 2: return "Average";
            case 3: return "Poor";
            case 4: return "Terrible";
            default: return "None";
        }
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(targetWorld.getName()).append(" (G").append(gravityWellDepth).append(")");
        sb.append(": Orbit=").append(orbitFuelPoints).append("pt")
                .append(", Land=").append(landingFuelPoints).append("pt")
                .append(", Depart=").append(departureFuelPoints).append("pt");
        if (!landingPossible) {
            sb.append(" [NO LANDING]");
        }
        if (hasStarport) {
            sb.append(" [Starport: ").append(getStarportQuality()).append("]");
        }
        return sb.toString();
    }
}
