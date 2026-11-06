package com.yisy.modules.doramcu.hex;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages resource costs for building a ship.
 * <p>
 * This class handles all build cost calculations including mineral cost, energy cost,
 * strategic resource requirements, and build time. Costs are derived from the ship's
 * components and hull module exterior cells.
 * <p>
 * Strategic resources are determined by the components mounted on the ship.
 * A ship cannot be built unless the owning empire controls planets with the required
 * strategic resource deposits.
 */
public class ShipResourceCosts {

    /** Base mineral cost multiplier per component slot. */
    public static final int MINERAL_COST_PER_SLOT = 50;

    /** Base energy cost multiplier per component slot. */
    public static final int ENERGY_COST_PER_SLOT = 30;

    /** Mineral cost per unit of material value for exterior hull cells. */
    public static final int MINERAL_COST_PER_MATERIAL = 10;

    /** Energy cost per exterior hull cell. */
    public static final int ENERGY_COST_PER_EXTERIOR_CELL = 5;

    private final Ship ship;

    /**
     * Creates resource costs management for the given ship.
     *
     * @param ship the ship this belongs to
     */
    public ShipResourceCosts(Ship ship) {
        this.ship = ship;
    }

    // ---- Mineral and Energy costs ----

    /**
     * Returns the mineral cost to build this ship.
     * Cost = occupied slots × MINERAL_COST_PER_SLOT + hull material cost.
     */
    public int getMineralCost() {
        int componentCost = ship.getOccupiedSlots() * MINERAL_COST_PER_SLOT;
        int hullCost = getHullMaterialCost();
        return componentCost + hullCost;
    }

    /**
     * Returns the energy cost to build this ship.
     * Cost = occupied slots × ENERGY_COST_PER_SLOT + hull energy cost.
     */
    public int getEnergyCost() {
        int componentCost = ship.getOccupiedSlots() * ENERGY_COST_PER_SLOT;
        int hullCost = getHullEnergyCost();
        return componentCost + hullCost;
    }

    /**
     * Returns the mineral cost contributed by all hull modules' exterior materials.
     */
    public int getHullMaterialCost() {
        int totalCost = 0;
        for (HullModule module : ship.getHullModules()) {
            totalCost += module.getExteriorCells().stream()
                    .mapToInt(cell -> cell.getMaterial().getValue() * MINERAL_COST_PER_MATERIAL)
                    .sum();
        }
        return totalCost;
    }

    /**
     * Returns the energy cost contributed by all hull modules' exterior cells.
     */
    public int getHullEnergyCost() {
        int totalExteriorCells = 0;
        for (HullModule module : ship.getHullModules()) {
            totalExteriorCells += module.getExteriorCellCount();
        }
        return totalExteriorCells * ENERGY_COST_PER_EXTERIOR_CELL;
    }

    // ---- Strategic resources ----

    /**
     * Returns the strategic resources required to build this ship.
     * Each component contributes its required strategic resource.
     */
    public Map<StrategicResource, Integer> getRequiredStrategicResources() {
        Map<StrategicResource, Integer> resources = new EnumMap<>(StrategicResource.class);
        for (ShipComponent component : ship.getComponents()) {
            StrategicResource res = component.getRequiredResource();
            if (res != null) {
                resources.merge(res, component.getQuantity(), Integer::sum);
            }
        }
        return Map.copyOf(resources);
    }

    /**
     * Returns the set of unique strategic resources required by this ship.
     */
    public Set<StrategicResource> getRequiredResourceTypes() {
        return ship.getComponents().stream()
                .map(ShipComponent::getRequiredResource)
                .filter(r -> r != null)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if this ship can be built given the available strategic resources.
     */
    public boolean canBuildWith(Set<StrategicResource> availableResources) {
        Set<StrategicResource> required = getRequiredResourceTypes();
        return availableResources.containsAll(required);
    }

    // ---- Build time ----

    /**
     * Returns the build time in turns for this ship.
     * Base build time from ship type, modified by the home planet's Diligence stat.
     */
    public int getBuildTime() {
        int base = ship.getShipType().getBuildTimeTurns();
        Planet homePlanet = ship.getHomePlanet();
        if (homePlanet != null) {
            int diligence = homePlanet.getPopulationStat(PopulationAttribute.DILIGENCE);
            base -= (diligence - 5);
        }
        return Math.max(1, base);
    }
}
