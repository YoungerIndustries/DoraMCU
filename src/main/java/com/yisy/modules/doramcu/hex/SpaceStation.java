package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.ai.StationAiCore;

import java.util.*;

/**
 * A space station — a fixed orbital structure with combat capabilities.
 * <p>
 * Space stations serve as defensive fortifications, shipyards, and resource
 * extraction hubs. Unlike ships, stations cannot move but provide significant
 * defensive bonuses to their home system. Stations are built by players and
 * assigned to specific hex positions in a solar system.
 * </p>
 * <p>
 * Based on implementations in Stellaris (Defense Grid, Shipyard stations),
 * Galactic Civilizations IV (Starbases), and FreeOrion.
 * </p>
 * <p>
 * This class acts as a thin coordinator, delegating to helper classes:
 * </p>
 * <ul>
 *   <li>{@link SpaceStationCombatStats} — combat calculations and damage/repair</li>
 *   <li>{@link SpaceStationEconomy} — economic methods, resource costs, strategic resources</li>
 *   <li>{@link SpaceStationDefense} — fleet and ship bay management</li>
 * </ul>
 */
public class SpaceStation {

    public static final int DEFAULT_HULL_POINTS = 500;
    public static final int DEFAULT_SHIELD_POINTS = 100;
    public static final int DEFAULT_MODULE_SLOTS = 4;
    public static final double DEFAULT_SHIELD_RECHARGE_RATE = 0.1;
    public static final double DEFAULT_HULL_REPAIR_RATE = 0.05;

    private final int id;
    private final SpaceStationType stationType;
    private String name;
    private Hex3D position;
    private int hullPoints;
    private int currentHullPoints;
    private int shieldPoints;
    private int currentShieldPoints;
    private int moduleSlots;
    private final List<StationModule> modules;
    private double shieldRechargeRate;
    private double hullRepairRate;
    private Player owner;
    private SolarSystemEntity homeSystem;
    final Set<StrategicResource> strategicResources;
    final Set<StartingFacility> startingFacilities;
    private int maxDockedShips;

    // Lazy-initialized helper delegates
    private SpaceStationCombatStats combatStats;
    private SpaceStationEconomy economy;
    private SpaceStationDefense defense;

    // AI Core integration
    private StationAiCore aiCore;

    /**
     * Creates a new space station with the given ID and default type (MILITARY).
     *
     * @param id the unique identifier for this station
     */
    public SpaceStation(int id) {
        this(id, SpaceStationType.MILITARY);
    }

    /**
     * Creates a new space station with the given ID and type.
     *
     * @param id the unique identifier for this station
     * @param stationType the classification of this station
     */
    public SpaceStation(int id, SpaceStationType stationType) {
        if (stationType == null) {
            throw new IllegalArgumentException("Station type must not be null");
        }
        this.id = id;
        this.stationType = stationType;
        this.name = stationType.name() + "Station-" + id;
        this.position = null;
        this.hullPoints = stationType.getBaseHullPoints();
        this.currentHullPoints = this.hullPoints;
        this.shieldPoints = stationType.getBaseShieldPoints();
        this.currentShieldPoints = this.shieldPoints;
        this.moduleSlots = stationType.getBaseModuleSlots();
        this.modules = new ArrayList<>();
        this.shieldRechargeRate = DEFAULT_SHIELD_RECHARGE_RATE;
        this.hullRepairRate = DEFAULT_HULL_REPAIR_RATE;
        this.owner = null;
        this.homeSystem = null;
        this.strategicResources = new HashSet<>();
        this.startingFacilities = new HashSet<>();
        this.maxDockedShips = stationType.getBaseShipBayCapacity();
    }

    // ---- Helper Delegates ----

    /**
     * Returns the combat stats helper, lazily initialized.
     */
    public SpaceStationCombatStats getCombatStats() {
        if (combatStats == null) {
            combatStats = new SpaceStationCombatStats(this);
        }
        return combatStats;
    }

    /**
     * Returns the economy helper, lazily initialized.
     */
    public SpaceStationEconomy getEconomy() {
        if (economy == null) {
            economy = new SpaceStationEconomy(this);
        }
        return economy;
    }

    /**
     * Returns the defense helper, lazily initialized.
     */
    public SpaceStationDefense getDefense() {
        if (defense == null) {
            defense = new SpaceStationDefense(this);
        }
        return defense;
    }

    // ---- AI Core Integration ----

    /**
     * Returns the AI Core installed on this station, or null if none.
     *
     * @return the AI Core, or null
     */
    public StationAiCore getAiCore() {
        if (aiCore == null) {
            aiCore = new StationAiCore(this);
        }
        return aiCore;
    }

    /**
     * Sets the AI Core level for this station.
     * <p>
     * Creates a new StationAiCore if one doesn't exist, or updates the level
     * of the existing one.
     * </p>
     *
     * @param level the AI Core level to install
     */
    public void setAiCoreLevel(StationAiCore.Level level) {
        if (level == null) {
            throw new IllegalArgumentException("AI Core level must not be null");
        }
        getAiCore().setAiCoreLevel(level);
    }

    /**
     * Returns true if this station has an AI Core installed.
     *
     * @return true if an AI Core is installed
     */
    public boolean hasAiCore() {
        return getAiCore().hasAiCore();
    }

    /**
     * Returns the AI Core level value (0-3), or 0 if no AI Core is installed.
     *
     * @return the AI Core level value
     */
    public int getAiCoreLevel() {
        return getAiCore().getLevelValue();
    }

    /**
     * Returns the tactical support radius provided by this station's AI Core.
     * <p>
     * Military stations with AI Cores provide tactical support to fleets
     * within this radius.
     * </p>
     *
     * @return the support radius in hexes (0 if no AI Core)
     */
    public int getAiSupportRadius() {
        return getAiCore().getSupportRadius();
    }

    /**
     * Returns the defense bonus provided to stationed fleets by this station's AI Core.
     *
     * @return the defense bonus multiplier (0.0 if no AI Core)
     */
    public double getAiDefenseBonus() {
        return getAiCore().getDefenseBonus();
    }

    // ---- Pipeline Bonuses ----

    /**
     * Returns the mining production bonus multiplier for this station.
     * Mining stations double mining output; others provide no bonus.
     *
     * @return the mining bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getMiningBonus() {
        return stationType.getMiningBonus();
    }

    /**
     * Returns the processing speed bonus multiplier for this station.
     * Research stations double processing speed; others provide no bonus.
     *
     * @return the processing bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getProcessingBonus() {
        return stationType.getProcessingBonus();
    }

    /**
     * Returns the assembly speed bonus multiplier for this station.
     * Shipyards double assembly speed; others provide no bonus.
     *
     * @return the assembly bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getAssemblyBonus() {
        return stationType.getAssemblyBonus();
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this station
     */
    public int getId() {
        return id;
    }

    /**
     * @return the station type classification
     */
    public SpaceStationType getStationType() {
        return stationType;
    }

    /**
     * @return the display name of this station
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this station.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the hex position of this station, or null if not positioned
     */
    public Hex3D getPosition() {
        return position;
    }

    /**
     * Sets the hex position of this station.
     *
     * @param position the new position
     */
    public void setPosition(Hex3D position) {
        this.position = position;
    }

    /**
     * @return the maximum hull points
     */
    public int getHullPoints() {
        return hullPoints;
    }

    /**
     * @return the current hull points
     */
    public int getCurrentHullPoints() {
        return currentHullPoints;
    }

    /**
     * @return the maximum shield points
     */
    public int getShieldPoints() {
        return shieldPoints;
    }

    /**
     * @return the current shield points
     */
    public int getCurrentShieldPoints() {
        return currentShieldPoints;
    }

    /**
     * @return the hull health ratio (0.0 to 1.0)
     */
    public double getHullHealthRatio() {
        if (hullPoints <= 0) {
            return 0.0;
        }
        return (double) currentHullPoints / hullPoints;
    }

    /**
     * @return the shield health ratio (0.0 to 1.0)
     */
    public double getShieldHealthRatio() {
        if (shieldPoints <= 0) {
            return 0.0;
        }
        return (double) currentShieldPoints / shieldPoints;
    }

    /**
     * @return true if the station is destroyed (hull at 0)
     */
    public boolean isDestroyed() {
        return currentHullPoints <= 0;
    }

    /**
     * @return the number of module slots
     */
    public int getModuleSlots() {
        return moduleSlots;
    }

    /**
     * @return the number of occupied module slots
     */
    public int getOccupiedSlots() {
        return modules.size();
    }

    /**
     * @return the number of available module slots
     */
    public int getAvailableSlots() {
        return moduleSlots - modules.size();
    }

    /**
     * @return unmodifiable list of installed modules
     */
    public List<StationModule> getModules() {
        return List.copyOf(modules);
    }

    /**
     * @return the shield recharge rate per turn (0.0 to 1.0)
     */
    public double getShieldRechargeRate() {
        return shieldRechargeRate;
    }

    /**
     * Sets the shield recharge rate per turn.
     *
     * @param rate the recharge rate (0.0 to 1.0)
     */
    public void setShieldRechargeRate(double rate) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("Shield recharge rate must be between 0.0 and 1.0: " + rate);
        }
        this.shieldRechargeRate = rate;
    }

    /**
     * @return the hull repair rate per turn (0.0 to 1.0)
     */
    public double getHullRepairRate() {
        return hullRepairRate;
    }

    /**
     * Sets the hull repair rate per turn.
     *
     * @param rate the repair rate (0.0 to 1.0)
     */
    public void setHullRepairRate(double rate) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("Hull repair rate must be between 0.0 and 1.0: " + rate);
        }
        this.hullRepairRate = rate;
    }

    /**
     * @return the owning player, or null if unowned
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Sets the owning player.
     *
     * @param owner the new owner
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * @return the home solar system, or null if not assigned
     */
    public SolarSystemEntity getHomeSystem() {
        return homeSystem;
    }

    /**
     * Sets the home solar system.
     *
     * @param homeSystem the home system
     */
    public void setHomeSystem(SolarSystemEntity homeSystem) {
        this.homeSystem = homeSystem;
    }

    /**
     * @return unmodifiable set of strategic resources required/produced
     */
    public Set<StrategicResource> getStrategicResources() {
        return Set.copyOf(strategicResources);
    }

    // ---- Module Management ----

    /**
     * Adds a module to the station.
     *
     * @param module the module to add
     * @return true if added successfully
     * @throws IllegalStateException if no slots available
     */
    public boolean addModule(StationModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Module must not be null");
        }
        if (modules.size() >= moduleSlots) {
            throw new IllegalStateException("No available module slots");
        }
        return modules.add(module);
    }

    /**
     * Removes a module from the station.
     *
     * @param module the module to remove
     * @return true if removed successfully
     */
    public boolean removeModule(StationModule module) {
        return modules.remove(module);
    }


    // ---- Combat Capabilities (delegated to SpaceStationCombatStats) ----

    /**
     * Returns the total damage output from all weapon modules.
     *
     * @return the total damage value
     */
    public int getTotalDamage() {
        return getCombatStats().getTotalDamage();
    }

    /**
     * Returns the effective damage output modified by hull health.
     *
     * @return the effective damage value
     */
    public int getEffectiveDamage() {
        return getCombatStats().getEffectiveDamage();
    }

    /**
     * Returns the total shield strength from all shield modules.
     *
     * @return the total shield value
     */
    public int getTotalShield() {
        return getCombatStats().getTotalShield();
    }

    /**
     * Returns the effective shield strength.
     *
     * @return the effective shield value
     */
    public int getEffectiveShield() {
        return getCombatStats().getEffectiveShield();
    }

    /**
     * Returns the total point defense value from all point defense modules.
     *
     * @return the total point defense value (0.0 to 1.0)
     */
    public double getTotalPointDefense() {
        return getCombatStats().getTotalPointDefense();
    }

    /**
     * Returns the total accuracy bonus from all targeting modules.
     *
     * @return the total accuracy bonus (0.0 to 1.0)
     */
    public double getTotalAccuracyBonus() {
        return getCombatStats().getTotalAccuracyBonus();
    }

    /**
     * Returns the total hull reinforcement from all hull modules.
     *
     * @return the total bonus hull points
     */
    public int getTotalHullBonus() {
        return getCombatStats().getTotalHullBonus();
    }

    /**
     * Returns the effective hull points including reinforcement modules.
     *
     * @return the effective hull points
     */
    public int getEffectiveHull() {
        return getCombatStats().getEffectiveHull();
    }

    // ---- Damage and Repair (delegated to SpaceStationCombatStats) ----

    /**
     * Takes damage, absorbing with shields first, then hull.
     *
     * @param damage the amount of damage to take
     */
    public void takeDamage(int damage) {
        getCombatStats().takeDamage(damage);
    }

    /**
     * Repairs shields by the given amount.
     *
     * @param amount the amount to repair
     */
    public void repairShield(int amount) {
        if (amount <= 0) {
            // Allow negative amounts for damage
            currentShieldPoints = Math.max(0, currentShieldPoints + amount);
            return;
        }
        currentShieldPoints = Math.min(shieldPoints, currentShieldPoints + amount);
    }

    /**
     * Repairs hull by the given amount.
     *
     * @param amount the amount to repair
     */
    public void repairHull(int amount) {
        if (amount <= 0) {
            // Allow negative amounts for damage
            currentHullPoints = Math.max(0, currentHullPoints + amount);
            return;
        }
        currentHullPoints = Math.min(hullPoints, currentHullPoints + amount);
    }

    /**
     * Recharges shields by the configured rate.
     * <p>
     * Called each turn to gradually restore shields.
     * </p>
     */
    public void rechargeShield() {
        if (shieldPoints <= 0) {
            return;
        }
        int recharge = Math.max(1, (int) Math.ceil(shieldPoints * shieldRechargeRate));
        currentShieldPoints = Math.min(shieldPoints, currentShieldPoints + recharge);
    }

    /**
     * Repairs hull by the configured rate.
     * <p>
     * Called each turn to gradually restore hull.
     * </p>
     */
    public void repairHull() {
        if (hullPoints <= 0) {
            return;
        }
        int repair = Math.max(1, (int) Math.ceil(hullPoints * hullRepairRate));
        currentHullPoints = Math.min(hullPoints, currentHullPoints + repair);
    }

    /**
     * Fully repairs both shields and hull.
     */
    public void fullRepair() {
        currentShieldPoints = shieldPoints;
        currentHullPoints = hullPoints;
    }

    // ---- Facility Management ----

    /**
     * Adds a starting facility to this station.
     *
     * @param facility the facility to add
     * @return true if the facility was added successfully
     */
    public boolean addStartingFacility(StartingFacility facility) {
        if (facility == null) {
            throw new IllegalArgumentException("Facility must not be null");
        }
        return startingFacilities.add(facility);
    }

    /**
     * Returns the set of starting facilities installed on this station.
     *
     * @return unmodifiable set of starting facilities
     */
    public Set<StartingFacility> getStartingFacilities() {
        return Set.copyOf(startingFacilities);
    }

    // ---- Ship Bay Management (delegated to SpaceStationDefense) ----

    /**
     * Returns the list of ships currently docked at this station.
     *
     * @return unmodifiable list of docked ships
     */
    public List<Ship> getDockedShips() {
        return getDefense().getDockedShips();
    }

    /**
     * Returns the maximum number of ships that can dock at this station.
     *
     * @return the maximum docked ship capacity
     */
    public int getMaxDockedShips() {
        return maxDockedShips;
    }

    /**
     * Sets the maximum number of ships that can dock at this station.
     *
     * @param maxDockedShips the new maximum capacity
     */
    public void setMaxDockedShips(int maxDockedShips) {
        if (maxDockedShips < 0) {
            throw new IllegalArgumentException("Max docked ships must be non-negative: " + maxDockedShips);
        }
        this.maxDockedShips = maxDockedShips;
    }

    /**
     * Returns the number of ships currently docked.
     *
     * @return the docked ship count
     */
    public int getDockedShipCount() {
        return getDefense().getDockedShipCount();
    }

    /**
     * Returns the number of available docking slots.
     *
     * @return the number of available slots
     */
    public int getAvailableDockingSlots() {
        return getDefense().getAvailableDockingSlots();
    }

    /**
     * Checks if the ship bay is full.
     *
     * @return true if no more ships can dock
     */
    public boolean isShipBayFull() {
        return getDefense().isShipBayFull();
    }

    /**
     * Docks a ship at this station.
     *
     * @param ship the ship to dock
     * @return true if the ship was docked successfully
     * @throws IllegalStateException if the ship bay is full
     */
    public boolean dockShip(Ship ship) {
        return getDefense().dockShip(ship);
    }

    /**
     * Undocks a ship from this station.
     *
     * @param ship the ship to undock
     * @return true if the ship was undocked successfully
     */
    public boolean undockShip(Ship ship) {
        return getDefense().undockShip(ship);
    }

    /**
     * Checks if a ship is currently docked at this station.
     *
     * @param ship the ship to check
     * @return true if the ship is docked
     */
    public boolean hasDockedShip(Ship ship) {
        return getDefense().hasDockedShip(ship);
    }

    /**
     * Undocks all ships from this station.
     */
    public void undockAllShips() {
        getDefense().undockAllShips();
    }

    /**
     * Returns the hull repair bonus multiplier for docked ships.
     *
     * @return the hull repair bonus multiplier (default 2.0x)
     */
    public double getDockedHullRepairBonus() {
        return getDefense().getDockedHullRepairBonus();
    }

    /**
     * Returns the shield recharge bonus multiplier for docked ships.
     *
     * @return the shield recharge bonus multiplier (default 2.0x)
     */
    public double getDockedShieldRechargeBonus() {
        return getDefense().getDockedShieldRechargeBonus();
    }

    /**
     * Repairs all docked ships' hull and shields.
     */
    public void repairDockedShips() {
        getDefense().repairDockedShips();
    }

    /**
     * Returns the total damage output from all docked ships.
     *
     * @return the total damage value
     */
    public int getDockedShipDamage() {
        return getDefense().getDockedShipDamage();
    }

    /**
     * Returns the total shield strength from all docked ships.
     *
     * @return the total shield value
     */
    public int getDockedShipShields() {
        return getDefense().getDockedShipShields();
    }

    // ---- Fleet Management (delegated to SpaceStationDefense) ----

    /**
     * Returns the list of fleets currently stationed at this station.
     *
     * @return unmodifiable list of stationed fleets
     */
    public List<Fleet> getStationedFleets() {
        return getDefense().getStationedFleets();
    }

    /**
     * Returns the number of fleets currently stationed at this station.
     *
     * @return the stationed fleet count
     */
    public int getStationedFleetCount() {
        return getDefense().getStationedFleetCount();
    }

    /**
     * Stations a fleet at this space station.
     *
     * @param fleet the fleet to station
     * @return true if the fleet was stationed successfully
     * @throws IllegalArgumentException if fleet is null
     */
    public boolean stationFleet(Fleet fleet) {
        return getDefense().stationFleet(fleet);
    }

    /**
     * Removes a fleet from this station.
     *
     * @param fleet the fleet to remove
     * @return true if the fleet was removed successfully
     */
    public boolean removeFleet(Fleet fleet) {
        return getDefense().removeFleet(fleet);
    }

    /**
     * Checks if a fleet is currently stationed at this station.
     *
     * @param fleet the fleet to check
     * @return true if the fleet is stationed
     */
    public boolean hasStationedFleet(Fleet fleet) {
        return getDefense().hasStationedFleet(fleet);
    }

    /**
     * Removes all fleets from this station.
     */
    public void clearStationedFleets() {
        getDefense().clearStationedFleets();
    }

    /**
     * Returns the defense bonus provided by this station to stationed fleets.
     *
     * @return the defense bonus multiplier (0.0 to 1.0)
     */
    public double getDefenseBonus() {
        return getDefense().getDefenseBonus();
    }

    /**
     * Returns the total damage output from all stationed fleets.
     *
     * @return the total damage value
     */
    public int getStationedFleetDamage() {
        return getDefense().getStationedFleetDamage();
    }

    /**
     * Returns the total shield strength from all stationed fleets.
     *
     * @return the total shield value
     */
    public int getStationedFleetShields() {
        return getDefense().getStationedFleetShields();
    }

    /**
     * Returns the total hull points from all stationed fleets.
     *
     * @return the total hull value
     */
    public int getStationedFleetHull() {
        return getDefense().getStationedFleetHull();
    }

    /**
     * Returns the total number of ships across all stationed fleets.
     *
     * @return the total ship count
     */
    public int getStationedShipCount() {
        return getDefense().getStationedShipCount();
    }

    /**
     * Repairs all stationed fleets' shields and hull.
     */
    public void repairStationedFleets() {
        getDefense().repairStationedFleets();
    }

    // ---- Object Contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpaceStation that = (SpaceStation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("SpaceStation{name='%s', type=%s, hull=%d/%d, shields=%d/%d, damage=%d, modules=%d/%d, docked=%d/%d, fleets=%d, facilities=%s, aiCore=%s}",
                name, stationType, currentHullPoints, hullPoints,
                currentShieldPoints, shieldPoints,
                getTotalDamage(), getOccupiedSlots(), moduleSlots,
                getDockedShipCount(), maxDockedShips, getStationedFleetCount(), startingFacilities,
                getAiCore().getAiCoreLevel());
    }

}
