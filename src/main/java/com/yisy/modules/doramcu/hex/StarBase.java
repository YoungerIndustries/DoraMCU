package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a starbase — a large orbital or planetary facility built from HullModules.
 * <p>
 * Following Traveller Highguard principles, a starbase is essentially a very large
 * "ship" that serves as a permanent installation. It uses the same HullModule and
 * ModuleType system as ships, but with additional capabilities:
 * <ul>
 *   <li>Multiple dock slots for visiting ships</li>
 *   <li>Defensive weapon batteries</li>
 *   <li>Repair and refueling facilities</li>
 *   <li>Administrative and trade functions</li>
 * </ul>
 * </p>
 * <p>
 * Starbases are categorized by size:
 * <ul>
 *   <li><b>Outpost</b> — 1-4 HullModules, basic functions</li>
 *   <li><b>Station</b> — 5-16 HullModules, moderate capabilities</li>
 *   <li><b>Fortress</b> — 17-64 HullModules, heavy defense</li>
 *   <li><b>Megastructure</b> — 65+ HullModules, massive installation</li>
 * </ul>
 * </p>
 */
public class StarBase {

    /** Minimum modules for a Station. */
    public static final int STATION_MIN_MODULES = 5;

    /** Minimum modules for a Fortress. */
    public static final int FORTRESS_MIN_MODULES = 17;

    /** Minimum modules for a Megastructure. */
    public static final int MEGASTRUCTURE_MIN_MODULES = 65;

    /** Cost multiplier for starbase construction vs ships. */
    public static final int COST_MULTIPLIER = 15;

    private final int id;
    private final String name;
    private final StarBaseType type;
    private final int moduleCount;
    private final List<HullModule> hullModules;
    
    /** Module configuration mapping module indices to ModuleTypes. */
    private final Map<Integer, ModuleType> moduleConfiguration;
    
    /** Number of dock slots for visiting ships. */
    private final int dockSlots;
    
    /** Current number of ships docked. */
    private int shipsDocked;
    
    /** Defensive capability rating. */
    private final int defenseRating;
    
    /** Repair capability (HP per turn). */
    private final int repairCapability;
    
    /** Owner player ID. */
    private final int ownerId;
    
    /** Planet ID if orbiting a planet, 0 for independent. */
    private final int planetId;
    
    /** Total hit points. */
    private int currentHitPoints;
    private final int maxHitPoints;

    /**
     * Type of starbase installation.
     */
    public enum StarBaseType {
        /** Small outpost — basic functions, 1-4 modules. */
        OUTPOST,
        
        /** Medium station — moderate capabilities, 5-16 modules. */
        STATION,
        
        /** Large fortress — heavy defense, 17-64 modules. */
        FORTRESS,
        
        /** Massive megastructure — 65+ modules. */
        MEGASTRUCTURE
    }

    /**
     * Creates a new starbase using the builder pattern.
     * @param builder the builder with configuration
     */
    private StarBase(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.moduleCount = builder.moduleCount;
        this.type = determineType(moduleCount);
        this.hullModules = new ArrayList<>();
        this.moduleConfiguration = new HashMap<>(builder.moduleConfiguration);
        this.ownerId = builder.ownerId;
        this.planetId = builder.planetId;
        
        // Initialize hull modules
        for (int i = 0; i < moduleCount; i++) {
            hullModules.add(new HullModule(name + "-Module-" + i));
        }
        
        // Calculate derived stats
        this.dockSlots = calculateDockSlots();
        this.shipsDocked = 0;
        this.defenseRating = calculateDefenseRating();
        this.repairCapability = calculateRepairCapability();
        this.maxHitPoints = calculateMaxHitPoints();
        this.currentHitPoints = maxHitPoints;
    }

    /**
     * Creates a builder for constructing a StarBase.
     * @param name the starbase name
     * @param moduleCount number of HullModules
     * @return a new builder
     */
    public static Builder builder(String name, int moduleCount) {
        return new Builder(name, moduleCount);
    }

    // ---- Calculations ----

    private StarBaseType determineType(int modules) {
        if (modules >= MEGASTRUCTURE_MIN_MODULES) return StarBaseType.MEGASTRUCTURE;
        if (modules >= FORTRESS_MIN_MODULES) return StarBaseType.FORTRESS;
        if (modules >= STATION_MIN_MODULES) return StarBaseType.STATION;
        return StarBaseType.OUTPOST;
    }

    private int calculateDockSlots() {
        // Each HANGAR module provides 5 dock slots
        long hangars = moduleConfiguration.values().stream()
                .filter(type -> type == ModuleType.HANGAR)
                .count();
        return (int) (hangars * 5 + moduleCount); // Base 1 slot per module
    }

    private int calculateDefenseRating() {
        return moduleConfiguration.values().stream()
                .filter(type -> type.getCategory() == ModuleType.ModuleCategory.WEAPONS
                        || type.getCategory() == ModuleType.ModuleCategory.DEFENSE)
                .mapToInt(type -> type.getBaseCostPerTon())
                .sum();
    }

    private int calculateRepairCapability() {
        long factories = moduleConfiguration.values().stream()
                .filter(type -> type == ModuleType.FACTORY)
                .count();
        return (int) (factories * 100);
    }

    private int calculateMaxHitPoints() {
        int baseHP = moduleCount * 1000; // 1000 HP per module
        long armor = moduleConfiguration.values().stream()
                .filter(type -> type == ModuleType.ARMOR)
                .count();
        return baseHP + (int) (armor * 500);
    }

    // ---- Operations ----

    /**
     * Docks a ship at this starbase.
     * @param shipId the ship's ID
     * @return true if successful
     */
    public boolean dockShip(int shipId) {
        if (shipsDocked >= dockSlots) {
            return false;
        }
        shipsDocked++;
        return true;
    }

    /**
     * Undocks a ship from this starbase.
     * @param shipId the ship's ID
     * @return true if successful
     */
    public boolean undockShip(int shipId) {
        if (shipsDocked > 0) {
            shipsDocked--;
            return true;
        }
        return false;
    }

    /**
     * Repairs a docked ship.
     * @param ship the ship to repair
     * @return amount of HP repaired
     */
    public int repairShip(Ship ship) {
        if (shipsDocked <= 0 || repairCapability <= 0) {
            return 0;
        }
        
        int currentHP = ship.getCurrentHullPoints();
        int maxHP = ship.getShipType().getBaseHullPoints();
        int damage = maxHP - currentHP;
        
        int repairAmount = Math.min(damage, repairCapability);
        // Note: Actually repairing the ship would require modifying Ship class
        return repairAmount;
    }

    /**
     * Takes damage from an attack.
     * @param damage amount of damage
     * @return true if destroyed
     */
    public boolean takeDamage(int damage) {
        currentHitPoints = Math.max(0, currentHitPoints - damage);
        return currentHitPoints <= 0;
    }

    /**
     * Repairs this starbase.
     * @param amount amount of HP to restore
     */
    public void repair(int amount) {
        currentHitPoints = Math.min(maxHitPoints, currentHitPoints + amount);
    }

    // ---- Accessors ----

    public int getId() { return id; }
    public String getName() { return name; }
    public StarBaseType getType() { return type; }
    public int getModuleCount() { return moduleCount; }
    public List<HullModule> getHullModules() { return new ArrayList<>(hullModules); }
    
    /**
     * Returns the module configuration.
     */
    public Map<Integer, ModuleType> getModuleConfiguration() {
        return Map.copyOf(moduleConfiguration);
    }
    
    public int getDockSlots() { return dockSlots; }
    public int getShipsDocked() { return shipsDocked; }
    public int getAvailableDockSlots() { return dockSlots - shipsDocked; }
    public int getDefenseRating() { return defenseRating; }
    public int getRepairCapability() { return repairCapability; }
    public int getOwnerId() { return ownerId; }
    public int getPlanetId() { return planetId; }
    public int getCurrentHitPoints() { return currentHitPoints; }
    public int getMaxHitPoints() { return maxHitPoints; }
    
    /**
     * Returns the hit point ratio (0.0 to 1.0).
     */
    public double getHitPointRatio() {
        if (maxHitPoints == 0) return 0.0;
        return currentHitPoints / (double) maxHitPoints;
    }
    
    /**
     * Returns the count of each ModuleType.
     */
    public Map<ModuleType, Long> getModuleCounts() {
        Map<ModuleType, Long> counts = new EnumMap<>(ModuleType.class);
        moduleConfiguration.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(type -> type, java.util.stream.Collectors.counting()))
                .forEach(counts::put);
        return counts;
    }
    
    /**
     * Calculates the cost to build this starbase.
     * @return cost in credits
     */
    public int getBuildCost() {
        int baseCost = moduleCount * 1000 * COST_MULTIPLIER;
        
        int moduleCost = moduleConfiguration.values().stream()
                .mapToInt(type -> type.getBaseCostPerTon() * 1000)
                .sum();
        
        return baseCost + moduleCost;
    }

    @Override
    public String toString() {
        return "StarBase{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", modules=" + moduleCount +
                ", docks=" + shipsDocked + "/" + dockSlots +
                ", hp=" + currentHitPoints + "/" + maxHitPoints +
                ", defense=" + defenseRating +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarBase starBase = (StarBase) o;
        return id == starBase.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Builder for constructing StarBase instances.
     */
    public static class Builder {
        private int id;
        private final String name;
        private final int moduleCount;
        private final Map<Integer, ModuleType> moduleConfiguration;
        private int ownerId;
        private int planetId;
        
        Builder(String name, int moduleCount) {
            if (moduleCount <= 0) {
                throw new IllegalArgumentException("Module count must be positive: " + moduleCount);
            }
            this.name = name;
            this.moduleCount = moduleCount;
            this.moduleConfiguration = new HashMap<>();
        }
        
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        
        public Builder ownerId(int ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        
        public Builder planetId(int planetId) {
            this.planetId = planetId;
            return this;
        }
        
        /**
         * Assigns a ModuleType to a specific HullModule index.
         */
        public Builder assignModule(int moduleIndex, ModuleType moduleType) {
            if (moduleIndex < 0 || moduleIndex >= moduleCount) {
                throw new IllegalArgumentException("Module index out of range: " + moduleIndex);
            }
            moduleConfiguration.put(moduleIndex, moduleType);
            return this;
        }
        
        public StarBase build() {
            return new StarBase(this);
        }
    }
}
