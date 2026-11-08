package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides refueling, repair, and maintenance services at a Traveller space station.
 * <p>
 * The service provider handles all ship maintenance operations:
 * </p>
 * <ul>
 *   <li>Refueling — filling fusion fuel tanks for jump travel</li>
 *   <li>Hull repair — restoring damaged hull points</li>
 *   <li>System repair — fixing damaged weapon systems and drives</li>
 *   <li>Maintenance — routine upkeep to prevent future damage</li>
 * </ul>
 * <p>
 * Service costs vary by station type and world price level. Alpha stations
 * offer the best rates, while outposts charge premium prices.
 * </p>
 * <p>
 * In Classic Traveller, repair costs are based on the percentage of damage
 * and the ship's construction cost. Refueling costs are based on fuel tonnage
 * and local fuel prices.
 * </p>
 */
public class StationService {

    /**
     * Base fuel cost per ton, in credits.
     */
    private static final int BASE_FUEL_COST_PER_TON = 100;

    /**
     * Base hull repair cost per hull point, in credits.
     */
    private static final int BASE_HULL_REPAIR_COST = 500;

    /**
     * Base system repair cost per system, in credits.
     */
    private static final int BASE_SYSTEM_REPAIR_COST = 10000;

    /**
     * Base maintenance cost per 1000 tons, in credits per week.
     */
    private static final int BASE_MAINTENANCE_COST_PER_TON = 5;

    private final SpaceStation station;
    private final List<ServiceRecord> serviceLog;

    /**
     * Creates a new service provider for the given station.
     *
     * @param station the space station
     */
    public StationService(SpaceStation station) {
        this.station = station;
        this.serviceLog = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * @return the space station this service belongs to
     */
    public SpaceStation getStation() {
        return station;
    }

    /**
     * Returns the service rate multiplier for this station.
     * <p>
     * Lower values mean cheaper services. Alpha stations have the best rates (0.8×),
     * while outposts have the worst (2.0×).
     * </p>
     *
     * @return the service rate multiplier
     */
    public double getServiceRateMultiplier() {
        return station.getStationType().getServiceRateMultiplier();
    }

    /**
     * Returns the repair rate multiplier for this station.
     *
     * @return the repair rate multiplier
     */
    public double getRepairRateMultiplier() {
        return station.getStationType().getRepairRateMultiplier();
    }

    /**
     * Returns the service log.
     *
     * @return unmodifiable list of service records
     */
    public List<ServiceRecord> getServiceLog() {
        return Collections.unmodifiableList(serviceLog);
    }

    // ---- Refueling ----

    /**
     * Refuels a ship's fuel tank at this station.
     * <p>
     * The cost is based on the amount of fuel added, the station type,
     * and the world's price level.
     * </p>
     *
     * @param fuelTank the ship's fuel tank
     * @param fullTank true to fill the tank completely, false to add one jump's worth
     * @return the cost in credits
     */
    public int refuel(FuelTank fuelTank, boolean fullTank) {
        if (fuelTank == null) {
            throw new IllegalArgumentException("Fuel tank must not be null");
        }

        int currentFuel = fuelTank.getCurrentFuelTons();
        int maxCapacity = fuelTank.getMaxCapacityTons();
        int fuelPerJump = fuelTank.getFuelPerJump();

        int fuelToAdd;
        if (fullTank) {
            fuelToAdd = maxCapacity - currentFuel;
        } else {
            // Add enough for one jump
            fuelToAdd = fuelPerJump;
            int spaceAvailable = maxCapacity - currentFuel;
            fuelToAdd = Math.min(fuelToAdd, spaceAvailable);
        }

        if (fuelToAdd <= 0) {
            logService("Refuel", 0, "Tank already full");
            return 0;
        }

        // Calculate cost
        double priceMultiplier = getServiceRateMultiplier();
        int baseCost = fuelToAdd * BASE_FUEL_COST_PER_TON;
        int cost = (int) (baseCost * priceMultiplier);

        // Apply refueling
        fuelTank.refuel(fuelToAdd);

        logService("Refuel", cost, "Added " + fuelToAdd + " tons of fuel");
        return cost;
    }

    // ---- Hull Repair ----

    /**
     * Repairs a ship's hull at this station.
     * <p>
     * The cost is based on the amount of damage repaired and the station type.
     * Repair restores the hull to maximum capacity.
     * </p>
     *
     * @param currentHull the ship's current hull points
     * @param maxHull the ship's maximum hull points
     * @return the cost in credits
     */
    public int repairHull(int currentHull, int maxHull) {
        if (currentHull >= maxHull) {
            logService("Hull Repair", 0, "Hull already at maximum");
            return 0;
        }

        int damage = maxHull - currentHull;
        double repairMultiplier = getRepairRateMultiplier();
        int cost = (int) (damage * BASE_HULL_REPAIR_COST * repairMultiplier);

        logService("Hull Repair", cost, "Repaired " + damage + " hull points");
        return cost;
    }

    /**
     * Partially repairs a ship's hull.
     * <p>
     * Repairs only the specified amount of damage, useful for emergency
     * repairs when full repair is not affordable.
     * </p>
     *
     * @param currentHull the ship's current hull points
     * @param maxHull the ship's maximum hull points
     * @param repairAmount the amount of hull points to repair
     * @return the cost in credits
     */
    public int repairHullPartial(int currentHull, int maxHull, int repairAmount) {
        if (currentHull >= maxHull) {
            logService("Hull Repair (Partial)", 0, "Hull already at maximum");
            return 0;
        }

        int damage = maxHull - currentHull;
        int actualRepair = Math.min(repairAmount, damage);

        double repairMultiplier = getRepairRateMultiplier();
        int cost = (int) (actualRepair * BASE_HULL_REPAIR_COST * repairMultiplier);

        logService("Hull Repair (Partial)", cost, "Repaired " + actualRepair + " of " + damage + " hull points");
        return cost;
    }

    // ---- System Repair ----

    /**
     * Repairs a damaged system at this station.
     * <p>
     * System repair fixes damaged weapon systems, drives, computers, etc.
     * The cost depends on the system type and station type.
     * </p>
     *
     * @param system the damaged system
     * @return the cost in credits
     */
    public int repairSystem(HitLocation.SystemType system) {
        if (system == null) {
            throw new IllegalArgumentException("System type must not be null");
        }

        double repairMultiplier = getRepairRateMultiplier();
        int cost = (int) (BASE_SYSTEM_REPAIR_COST * repairMultiplier * getSystemComplexityMultiplier(system));

        logService("System Repair", cost, "Repaired " + system + " system");
        return cost;
    }

    /**
     * Returns the complexity multiplier for a system type.
     * <p>
     * More complex systems cost more to repair.
     * </p>
     *
     * @param system the system type
     * @return the complexity multiplier
     */
    private double getSystemComplexityMultiplier(HitLocation.SystemType system) {
        switch (system) {
            case JUMP_DRIVE:
                return 3.0;
            case FUSION_DRIVE:
                return 2.5;
            case POWER_PLANT:
                return 2.0;
            case COMPUTER:
                return 1.5;
            case MISSILE:
                return 1.2;
            case LASER:
                return 1.0;
            case LIFE_SUPPORT:
                return 1.0;
            case CARGO:
                return 0.5;
            case PASSENGERS:
                return 0.5;
            case CREW:
                return 0.8;
            case HULL:
                return 0.3;
            case EMPTY:
                return 0.0;
            default:
                return 1.0;
        }
    }

    // ---- Maintenance ----

    /**
     * Performs routine maintenance on a ship.
     * <p>
     * Maintenance reduces the risk of future damage and keeps systems
     * operating at peak efficiency. Cost is based on ship tonnage.
     * </p>
     *
     * @param tonnage the ship's tonnage
     * @return the cost in credits
     */
    public int maintenance(int tonnage) {
        if (tonnage <= 0) {
            throw new IllegalArgumentException("Tonnage must be positive");
        }

        double serviceMultiplier = getServiceRateMultiplier();
        int cost = (int) ((tonnage / 1000) * BASE_MAINTENANCE_COST_PER_TON * serviceMultiplier);

        logService("Maintenance", cost, "Routine maintenance on " + tonnage + "kt ship");
        return cost;
    }

    /**
     * Calculates the total cost for a full service package.
     * <p>
     * A full service package includes:
     * </p>
     * <ul>
     *   <li>Full refuel</li>
     *   <li>Hull repair to maximum</li>
     *   <li>System repair for all damaged systems</li>
     *   <li>Routine maintenance</li>
     * </ul>
     *
     * @param fuelTank the ship's fuel tank
     * @param currentHull the ship's current hull points
     * @param maxHull the ship's maximum hull points
     * @param tonnage the ship's tonnage
     * @param damagedSystems the number of damaged systems
     * @return the total cost in credits
     */
    public int fullServiceCost(FuelTank fuelTank, int currentHull, int maxHull,
                                int tonnage, int damagedSystems) {
        int fuelCost = 0;
        if (fuelTank != null && !fuelTank.isFull()) {
            int fuelNeeded = fuelTank.getMaxCapacityTons() - fuelTank.getCurrentFuelTons();
            fuelCost = (int) (fuelNeeded * BASE_FUEL_COST_PER_TON * getServiceRateMultiplier());
        }

        int hullCost = repairHull(currentHull, maxHull);
        int systemCost = damagedSystems * (int) (BASE_SYSTEM_REPAIR_COST * getRepairRateMultiplier());
        int maintenanceCost = maintenance(tonnage);

        return fuelCost + hullCost + systemCost + maintenanceCost;
    }

    private void logService(String serviceType, int cost, String description) {
        serviceLog.add(new ServiceRecord(serviceType, cost, description));
    }

    // ---- Inner Classes ----

    /**
     * A recorded service operation.
     */
    public static class ServiceRecord {
        private final String serviceType;
        private final int cost;
        private final String description;
        private final long timestamp;

        ServiceRecord(String serviceType, int cost, String description) {
            this.serviceType = serviceType;
            this.cost = cost;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }

        public String getServiceType() {
            return serviceType;
        }

        public int getCost() {
            return cost;
        }

        public String getDescription() {
            return description;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return serviceType + ": " + description + " (" + cost + " credits)";
        }
    }
}
