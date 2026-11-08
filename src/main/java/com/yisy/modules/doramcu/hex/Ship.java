package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a warship in a 4X empire game.
 * <p>
 * A ship is defined by its {@link ShipType} (which determines base hull, slots,
 * and build time) and a collection of {@link ShipComponent} instances mounted
 * in its component slots. This class serves as a thin coordinator that delegates to:
 * </p>
 * <ul>
 *   <li>{@link ShipHull} — 3D hull module grid, component installation, integrity</li>
 *   <li>{@link ShipCombatStats} — Damage, shields, hull, speed, evasion, repair</li>
 *   <li>{@link ShipResourceCosts} — Mineral/energy cost, strategic resources, build time</li>
 * </ul>
 * <p>
 * The Ship class itself manages identity (ID, name, type), component slots,
 * and current hull/shield points. All complex logic is delegated to the helper classes.
 * </p>
 */
public class Ship {

    private final int id;
    private final ShipType shipType;
    private final List<ShipComponent> components;
    private final int maxComponentSlots;
    private String name;
    private int currentHullPoints;
    private int currentShieldPoints;
    private Planet homePlanet;
    private AiCoreComponent aiCore;

    // ---- Delegated helpers ----

    private final ShipHull hull;
    private final ShipCombatStats combatStats;
    private final ShipResourceCosts resourceCosts;

    // ---- Package-private methods for delegated helpers ----

    /**
     * Package-private access for ShipHull to avoid circular calls.
     */
    List<ShipComponent> getComponentsInternal() {
        return components;
    }

    /**
     * Package-private access for ShipHull to avoid circular calls.
     */
    void recalculateStatsInternal() {
        recalculateStats();
    }

    /**
     * Creates a new ship with the given ID and type.
     * Initializes hull modules based on the ship type's cubic meters.
     *
     * @param id the unique identifier for this ship
     * @param shipType the classification of this ship
     */
    public Ship(int id, ShipType shipType) {
        if (shipType == null) {
            throw new IllegalArgumentException("Ship type must not be null");
        }
        this.id = id;
        this.shipType = shipType;
        this.maxComponentSlots = shipType.getComponentSlots();
        this.components = new ArrayList<>();
        this.name = shipType.name() + "-" + id;
        this.currentHullPoints = shipType.getBaseHullPoints();
        this.currentShieldPoints = 0;
        this.hull = new ShipHull(this);
        this.combatStats = new ShipCombatStats(this);
        this.resourceCosts = new ShipResourceCosts(this);
    }

    // ---- Accessors ----

    /**
     * Returns the unique identifier of this ship.
     *
     * @return the ship ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the ship type classification.
     *
     * @return the ship type
     */
    public ShipType getShipType() {
        return shipType;
    }

    /**
     * Returns the display name of this ship.
     *
     * @return the ship name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this ship.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of mounted components.
     *
     * @return unmodifiable list of components
     */
    public List<ShipComponent> getComponents() {
        return List.copyOf(components);
    }

    /**
     * Returns the maximum number of component slots available.
     *
     * @return the max component slots
     */
    public int getMaxComponentSlots() {
        return maxComponentSlots;
    }

    /**
     * Returns the number of currently occupied component slots.
     *
     * @return the number of occupied slots
     */
    public int getOccupiedSlots() {
        return components.size();
    }

    /**
     * Returns the number of available (empty) component slots.
     *
     * @return the number of available slots
     */
    public int getAvailableSlots() {
        return maxComponentSlots - components.size();
    }

    /**
     * Returns the home planet where this ship was built, or null if not assigned.
     *
     * @return the home planet
     */
    public Planet getHomePlanet() {
        return homePlanet;
    }

    /**
     * Sets the home planet where this ship was built.
     *
     * @param planet the home planet
     */
    public void setHomePlanet(Planet planet) {
        this.homePlanet = planet;
    }

    // ---- AI Core integration ----

    /**
     * Returns the AI Core component installed on this ship, or null if none.
     *
     * @return the AI Core component, or null
     */
    public AiCoreComponent getAiCore() {
        return aiCore;
    }

    /**
     * Installs an AI Core component on this ship.
     * <p>
     * If an AI Core is already installed, it will be replaced.
     * </p>
     *
     * @param aiCore the AI Core to install
     */
    public void setAiCore(AiCoreComponent aiCore) {
        this.aiCore = aiCore;
    }

    /**
     * Removes the AI Core component from this ship.
     */
    public void removeAiCore() {
        this.aiCore = null;
    }

    /**
     * Returns true if this ship has an AI Core installed.
     *
     * @return true if an AI Core is installed
     */
    public boolean hasAiCore() {
        return aiCore != null;
    }

    /**
     * Returns the AI Core level (1-3), or 0 if no AI Core is installed.
     *
     * @return the AI Core level (1-based: BASIC=1, ADVANCED=2, ELITE=3)
     */
    public int getAiCoreLevel() {
        return aiCore != null ? aiCore.getTier().ordinal() + 1 : 0;
    }

    // ---- HullModule integration (delegated to ShipHull) ----

    public List<HullModule> getHullModules() {
        return hull.getHullModules();
    }

    public void setHullModules(List<HullModule> modules) {
        hull.setHullModules(modules);
    }

    public int[] getModuleGridDimensions() {
        return hull.getModuleGridDimensions();
    }

    public int getHullModuleCount() {
        return hull.getHullModuleCount();
    }

    public HullModule getModuleAt(int mx, int my, int mz) {
        return hull.getModuleAt(mx, my, mz);
    }

    public HullModule getModuleAt(int index) {
        return hull.getModuleAt(index);
    }

    public java.util.Map<String, Object> findModuleForPosition(int globalX, int globalY, int globalZ) {
        return hull.findModuleForPosition(globalX, globalY, globalZ);
    }

    public java.util.Map<ShipComponent, List<M3space>> getComponentSpaces() {
        return hull.getComponentSpaces();
    }

    public boolean installComponent(ShipComponent component, List<M3space> spaces) {
        return hull.installComponent(component, spaces);
    }

    public boolean installComponent(ShipComponent component, int x, int y, int z) {
        return hull.installComponent(component, x, y, z);
    }

    public boolean installComponent(ShipComponent component, int moduleIndex, int x, int y, int z) {
        return hull.installComponent(component, moduleIndex, x, y, z);
    }

    public boolean removeComponent(ShipComponent component) {
        return hull.removeComponent(component);
    }

    public List<M3space> getOccupiedSpaces(ShipComponent component) {
        return hull.getOccupiedSpaces(component);
    }

    public List<M3space> getAllOccupiedSpaces() {
        return hull.getAllOccupiedSpaces();
    }

    public boolean isSpaceAvailable(int x, int y, int z) {
        return hull.isSpaceAvailable(x, y, z);
    }

    public boolean isSpaceAvailable(int moduleIndex, int x, int y, int z) {
        return hull.isSpaceAvailable(moduleIndex, x, y, z);
    }

    public int getAvailableSpaces() {
        return hull.getAvailableSpaces();
    }

    public double getHullIntegrity() {
        return hull.getHullIntegrity();
    }

    public int getDestroyedCellCount() {
        return hull.getDestroyedCellCount();
    }

    public int getDamagedCellCount() {
        return hull.getDamagedCellCount();
    }

    /**
     * Repairs all destroyed M3space cells across all hull modules.
     * @return number of cells repaired
     */
    public int repairHull() {
        return hull.repairHull();
    }

    // ---- Hull and Shield (delegated to ShipCombatStats) ----

    public int getMaxHullPoints() {
        return combatStats.getMaxHullPoints();
    }

    public int getCurrentHullPoints() {
        return currentHullPoints;
    }

    public int getCurrentShieldPoints() {
        return currentShieldPoints;
    }

    public int getMaxShieldPoints() {
        return combatStats.getMaxShieldPoints();
    }

    public boolean isDestroyed() {
        return combatStats.isDestroyed();
    }

    public double getHullHealthRatio() {
        return combatStats.getHullHealthRatio();
    }

    public double getShieldHealthRatio() {
        return combatStats.getShieldHealthRatio();
    }

    // ---- Component management ----

    /**
     * Adds a component to the ship.
     * @throws IllegalStateException if no slots are available
     */
    public boolean addComponent(ShipComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null");
        }
        if (components.size() >= maxComponentSlots) {
            throw new IllegalStateException("No available component slots: " + maxComponentSlots + " slots full");
        }
        boolean added = components.add(component);
        recalculateStats();
        return added;
    }

    /**
     * Removes all components from the ship and frees their occupied M3space cells.
     */
    public void clearComponents() {
        hull.clearAllComponents();
        components.clear();
        recalculateStats();
    }

    private void recalculateStats() {
        int newMaxHull = shipType.getBaseHullPoints()
                + components.stream().mapToInt(ShipComponent::getHullBonus).sum();
        int newMaxShield = components.stream().mapToInt(ShipComponent::getShieldStrength).sum();
        currentHullPoints = Math.min(currentHullPoints, newMaxHull);
        currentShieldPoints = Math.min(currentShieldPoints, newMaxShield);
        currentHullPoints = Math.max(0, currentHullPoints);
        currentShieldPoints = Math.max(0, currentShieldPoints);
    }

    // ---- Combat calculations (delegated to ShipCombatStats) ----

    public int getTotalDamage() {
        return combatStats.getTotalDamage();
    }

    public int getEffectiveDamage() {
        return combatStats.getEffectiveDamage();
    }

    public int getEffectiveShieldStrength() {
        return combatStats.getEffectiveShieldStrength();
    }

    public int getEffectiveHullPoints() {
        return combatStats.getEffectiveHullPoints();
    }

    public int getMaxRange() {
        return combatStats.getMaxRange();
    }

    // ---- Speed and Evasion (delegated to ShipCombatStats) ----

    /**
     * Returns the MDrive (Maneuver Drive) base speed for this ship.
     * <p>
     * MDrive is used for movement within a solar system and during combat.
     * </p>
     *
     * @return the MDrive base speed
     */
    public int getMDriveSpeed() {
        return combatStats.getMDriveSpeed();
    }

    /**
     * Returns the JDrive (Jump Drive) base speed for this ship.
     * <p>
     * JDrive is used for faster-than-light travel between solar systems via hyperlanes.
     * </p>
     *
     * @return the JDrive base speed
     */
    public int getJDriveSpeed() {
        return combatStats.getJDriveSpeed();
    }

    /**
     * Returns the effective MDrive speed, reduced by damage.
     *
     * @return the effective MDrive speed
     */
    public int getEffectiveMDriveSpeed() {
        return combatStats.getEffectiveMDriveSpeed();
    }

    /**
     * Returns the effective JDrive speed, reduced by damage.
     *
     * @return the effective JDrive speed
     */
    public int getEffectiveJDriveSpeed() {
        return combatStats.getEffectiveJDriveSpeed();
    }

    /**
     * Returns the base speed for this ship (alias for MDrive speed).
     *
     * @return the MDrive base speed
     * @deprecated Use {@link #getMDriveSpeed()} instead
     */
    @Deprecated
    public int getBaseSpeed() {
        return combatStats.getBaseSpeed();
    }

    /**
     * Returns the effective speed for this ship (alias for effective MDrive speed).
     *
     * @return the effective MDrive speed
     * @deprecated Use {@link #getEffectiveMDriveSpeed()} instead
     */
    @Deprecated
    public int getEffectiveSpeed() {
        return combatStats.getEffectiveSpeed();
    }

    public double getBaseEvasion() {
        return combatStats.getBaseEvasion();
    }

    public double getEffectiveEvasion() {
        return combatStats.getEffectiveEvasion();
    }

    // ---- Resource costs (delegated to ShipResourceCosts) ----

    public int getMineralCost() {
        return resourceCosts.getMineralCost();
    }

    public int getEnergyCost() {
        return resourceCosts.getEnergyCost();
    }

    public int getHullMaterialCost() {
        return resourceCosts.getHullMaterialCost();
    }

    public int getHullEnergyCost() {
        return resourceCosts.getHullEnergyCost();
    }

    public java.util.Map<Material, Long> getHullMaterialBreakdown() {
        return hull.getHullMaterialBreakdown();
    }

    public int getHullCellCount() {
        return hull.getHullCellCount();
    }

    public HullShape getHullShape() {
        return hull.getHullShape();
    }

    public Material getExteriorMaterial() {
        return hull.getExteriorMaterial();
    }

    public int getCubicMeters() {
        return shipType.getCubicMeters();
    }

    public void setHullShape(HullShape shape) {
        hull.setHullShape(shape);
    }

    public void setExteriorMaterial(Material material) {
        hull.setExteriorMaterial(material);
    }

    public void setInteriorMaterial(Material material) {
        hull.setInteriorMaterial(material);
    }

    public java.util.Map<StrategicResource, Integer> getRequiredStrategicResources() {
        return resourceCosts.getRequiredStrategicResources();
    }

    public java.util.Set<StrategicResource> getRequiredResourceTypes() {
        return resourceCosts.getRequiredResourceTypes();
    }

    public boolean canBuildWith(java.util.Set<StrategicResource> availableResources) {
        return resourceCosts.canBuildWith(availableResources);
    }

    public int getBuildTime() {
        return resourceCosts.getBuildTime();
    }

    // ---- Ongoing Resource Consumption ----

    /**
     * Returns the fuel consumption per turn for this ship.
     * Fuel consumption is based on ship type size.
     *
     * @return the fuel consumed per turn (in raw resource GAS units)
     */
    public int getFuelConsumptionPerTurn() {
        // Base fuel consumption from ship type
        return switch (shipType) {
            case FRIGATE -> 1;
            case DESTROYER -> 2;
            case CRUISER -> 3;
            case BATTLESHIP -> 4;
            case CARRIER -> 3;
            case MOTHERSHIP -> 5;
        };
    }

    /**
     * Returns the maintenance module cost per turn for this ship.
     * Maintenance is needed to keep hull integrity and shields functional.
     *
     * @return map of construction module types to maintenance quantities
     */
    public java.util.Map<ConstructionModuleType, Integer> getMaintenanceCostPerTurn() {
        java.util.Map<ConstructionModuleType, Integer> cost = new java.util.EnumMap<>(ConstructionModuleType.class);
        // Hull maintenance based on hull integrity
        double integrity = getHullIntegrity();
        if (integrity < 0.8) {
            cost.put(ConstructionModuleType.HULL_MODULE, 1);
        }
        // Shield maintenance if shields are damaged
        if (getShieldHealthRatio() < 0.5 && getMaxShieldPoints() > 0) {
            cost.put(ConstructionModuleType.SHIELD_MODULE, 1);
        }
        return cost;
    }

    // ---- Damage and repair (delegated to ShipCombatStats) ----

    /**
     * Applies damage to this ship. Shields absorb damage first, then hull.
     * @return the actual damage dealt
     */
    public int takeDamage(int damage) {
        return combatStats.takeDamage(damage);
    }

    public void repairHull(int amount) {
        currentHullPoints = Math.min(getMaxHullPoints(), currentHullPoints + amount);
    }

    public void rechargeShields(int amount) {
        currentShieldPoints = Math.min(getMaxShieldPoints(), currentShieldPoints + amount);
    }

    public void fullRepair() {
        currentHullPoints = getMaxHullPoints();
        currentShieldPoints = getMaxShieldPoints();
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return id == ship.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Ship{name='%s', type=%s, hull=%d/%d, shields=%d/%d, damage=%d, mDrive=%d, jDrive=%d, evasion=%.0f%%, slots=%d/%d}",
                name, shipType, currentHullPoints, getMaxHullPoints(),
                currentShieldPoints, getMaxShieldPoints(),
                getTotalDamage(), getMDriveSpeed(), getJDriveSpeed(), getBaseEvasion() * 100,
                getOccupiedSlots(), maxComponentSlots);
    }
}
