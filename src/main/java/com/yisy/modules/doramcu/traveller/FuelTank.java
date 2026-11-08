package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * Fuel tank management for a Traveller starship.
 * <p>
 * In Classic Traveller Book 2, fuel is stored in tons and consumed during
 * jump travel. The fuel tank capacity depends on the ship's fusion drive
 * rating and total tonnage. Each point of maneuver rating allows 1% of
 * ship tonnage in fuel storage.
 * </p>
 * <p>
 * The fuel tank tracks:
 * </p>
 * <ul>
 *   <li>Current fuel level in tons</li>
 *   <li>Maximum capacity in tons</li>
 *   <li>Fuel consumption during jumps</li>
 *   <li>Refueling operations</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   100kt ship with 3M fusion drive:
 *   Fuel capacity: 3 × (100000 / 100) = 3000 tons
 *   Each jump consumes: 1000 tons (1% of tonnage)
 *   Maximum jumps: 3
 * </pre>
 */
public class FuelTank {

    /**
     * Current fuel level in tons.
     */
    private int currentFuelTons;

    /**
     * Maximum fuel capacity in tons.
     */
    private final int maxCapacityTons;

    /**
     * The ship's fusion drive (determines fuel capacity).
     */
    private final FusionDrive fusionDrive;

    /**
     * The ship's total tonnage.
     */
    private final int totalTonnage;

    /**
     * Fuel consumed per jump (1% of ship tonnage, minimum 1000 tons).
     */
    private final int fuelPerJump;

    /**
     * Creates a new fuel tank for a ship.
     * <p>
     * The capacity is calculated from the fusion drive rating and ship tonnage.
     * The tank starts full.
     * </p>
     *
     * @param fusionDrive the ship's fusion drive
     * @param totalTonnage the ship's total tonnage in tons
     */
    public FuelTank(FusionDrive fusionDrive, int totalTonnage) {
        this.fusionDrive = Objects.requireNonNull(fusionDrive, "Fusion drive must not be null");
        if (totalTonnage <= 0) {
            throw new IllegalArgumentException("Total tonnage must be positive");
        }
        this.totalTonnage = totalTonnage;
        this.maxCapacityTons = fusionDrive.getFuelTons(totalTonnage, fusionDrive.getMaxFuelPoints());
        this.fuelPerJump = Math.max(1000, totalTonnage / 100);
        this.currentFuelTons = maxCapacityTons; // Start full
    }

    /**
     * Creates a new fuel tank with explicit capacity.
     *
     * @param maxCapacityTons the maximum fuel capacity in tons
     * @param currentFuelTons the initial fuel level in tons
     * @param fuelPerJump the fuel consumed per jump
     */
    public FuelTank(int maxCapacityTons, int currentFuelTons, int fuelPerJump) {
        if (maxCapacityTons <= 0) {
            throw new IllegalArgumentException("Max capacity must be positive");
        }
        if (currentFuelTons < 0) {
            throw new IllegalArgumentException("Current fuel must be non-negative");
        }
        if (fuelPerJump <= 0) {
            throw new IllegalArgumentException("Fuel per jump must be positive");
        }
        this.maxCapacityTons = maxCapacityTons;
        this.currentFuelTons = Math.min(currentFuelTons, maxCapacityTons);
        this.fuelPerJump = fuelPerJump;
        this.fusionDrive = FusionDrive.NONE;
        this.totalTonnage = 0;
    }

    // ---- Accessors ----

    /**
     * @return the current fuel level in tons
     */
    public int getCurrentFuelTons() {
        return currentFuelTons;
    }

    /**
     * @return the maximum fuel capacity in tons
     */
    public int getMaxCapacityTons() {
        return maxCapacityTons;
    }

    /**
     * @return the ship's fusion drive
     */
    public FusionDrive getFusionDrive() {
        return fusionDrive;
    }

    /**
     * @return the ship's total tonnage
     */
    public int getTotalTonnage() {
        return totalTonnage;
    }

    /**
     * @return the fuel consumed per jump in tons
     */
    public int getFuelPerJump() {
        return fuelPerJump;
    }

    // ---- Derived Properties ----

    /**
     * @return the fuel level as a percentage of capacity (0-100)
     */
    public int getFuelPercentage() {
        return (int) ((double) currentFuelTons / maxCapacityTons * 100);
    }

    /**
     * @return the number of jumps possible with current fuel
     */
    public int getJumpsRemaining() {
        return currentFuelTons / fuelPerJump;
    }

    /**
     * @return true if the tank is empty
     */
    public boolean isEmpty() {
        return currentFuelTons <= 0;
    }

    /**
     * @return true if the tank is full
     */
    public boolean isFull() {
        return currentFuelTons >= maxCapacityTons;
    }

    /**
     * Checks if there is enough fuel for the given number of jumps.
     *
     * @param jumpCount the number of jumps
     * @return true if enough fuel is available
     */
    public boolean hasEnoughFuelForJumps(int jumpCount) {
        return currentFuelTons >= fuelPerJump * jumpCount;
    }

    /**
     * Checks if there is enough fuel for the given jump plan.
     *
     * @param plan the jump plan
     * @return true if enough fuel is available
     */
    public boolean hasEnoughFuelForPlan(JumpPlan plan) {
        Objects.requireNonNull(plan, "Jump plan must not be null");
        return currentFuelTons >= plan.getTotalFuelTons();
    }

    /**
     * Checks if there is enough fuel for a ManeuverPlan.
     *
     * @param plan the maneuver plan
     * @return true if enough fuel is available
     */
    public boolean hasEnoughFuelForPlan(ManeuverPlan plan) {
        Objects.requireNonNull(plan, "Maneuver plan must not be null");
        return currentFuelTons >= plan.getTotalFuelTons();
    }

    // ---- Fuel Operations ----

    /**
     * Consumes fuel for a single jump.
     *
     * @return true if fuel was consumed, false if insufficient fuel
     * @throws IllegalStateException if the tank is empty
     */
    public boolean consumeFuelForJump() {
        if (currentFuelTons < fuelPerJump) {
            return false;
        }
        currentFuelTons -= fuelPerJump;
        return true;
    }

    /**
     * Consumes fuel for a specific amount.
     *
     * @param tons the amount of fuel to consume in tons
     * @return true if fuel was consumed, false if insufficient fuel
     */
    public boolean consumeFuel(int tons) {
        if (tons <= 0) {
            return true;
        }
        if (currentFuelTons < tons) {
            return false;
        }
        currentFuelTons -= tons;
        return true;
    }

    /**
     * Refuels the tank by the given amount.
     * <p>
     * The tank cannot exceed its maximum capacity.
     * </p>
     *
     * @param tons the amount of fuel to add in tons
     * @return the actual amount added (may be less than requested if tank fills)
     */
    public int refuel(int tons) {
        if (tons <= 0) {
            return 0;
        }
        int spaceAvailable = maxCapacityTons - currentFuelTons;
        int actualAdd = Math.min(tons, spaceAvailable);
        currentFuelTons += actualAdd;
        return actualAdd;
    }

    /**
     * Refuels the tank to full capacity.
     *
     * @return the amount of fuel added
     */
    public int refuelToFull() {
        return refuel(maxCapacityTons);
    }

    /**
     * Sets the fuel level to a specific value.
     * <p>
     * Use this for initialization or emergency situations.
     * </p>
     *
     * @param tons the new fuel level in tons
     * @throws IllegalArgumentException if tons is negative or exceeds capacity
     */
    public void setFuelLevel(int tons) {
        if (tons < 0) {
            throw new IllegalArgumentException("Fuel level must be non-negative");
        }
        if (tons > maxCapacityTons) {
            throw new IllegalArgumentException("Fuel level exceeds capacity: " + tons + " > " + maxCapacityTons);
        }
        this.currentFuelTons = tons;
    }

    /**
     * Returns the fuel remaining after executing a jump plan.
     *
     * @param plan the jump plan
     * @return fuel remaining in tons (may be negative if insufficient)
     */
    public int getFuelAfterPlan(JumpPlan plan) {
        Objects.requireNonNull(plan, "Jump plan must not be null");
        return currentFuelTons - plan.getTotalFuelTons();
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return currentFuelTons + "/" + maxCapacityTons + " tons (" +
                getFuelPercentage() + "%, " + getJumpsRemaining() + " jumps)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuelTank fuelTank = (FuelTank) o;
        return maxCapacityTons == fuelTank.maxCapacityTons &&
                fusionDrive == fuelTank.fusionDrive &&
                totalTonnage == fuelTank.totalTonnage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxCapacityTons, fusionDrive, totalTonnage);
    }
}
