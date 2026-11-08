package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a group of ships operating together in a 4X empire game.
 * <p>
 * A fleet aggregates the combat capabilities of all its ships, providing fleet-level
 * statistics for damage, shields, and hull points. Fleets can be positioned at specific
 * coordinates in space and engage in combat with enemy fleets.
 * </p>
 * <p>
 * Fleet combat follows a turn-based system where ships fire in order of range (longest
 * first), and damage is distributed across the enemy fleet's shields and hull.
 * </p>
 */
public class Fleet {

    /** Default shield recharge rate per turn. */
    public static final int DEFAULT_SHIELD_RECHARGE_RATE = 10;

    /** Default hull repair rate per turn. */
    public static final int DEFAULT_HULL_REPAIR_RATE = 5;

    private final int id;
    private final List<Ship> ships;
    private String name;
    private Hex3D position;
    private Planet homeSystem;
    private int shieldRechargeRate;
    private int hullRepairRate;
    private Player owner;
    private SpaceStation homeStation;

    /**
     * Creates a new empty fleet with the given ID.
     *
     * @param id the unique identifier for this fleet
     */
    public Fleet(int id) {
        this(id, "Fleet-" + id);
    }

    /**
     * Creates a new empty fleet with the given ID and name.
     *
     * @param id the unique identifier for this fleet
     * @param name the display name for this fleet
     */
    public Fleet(int id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Fleet name must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.ships = new ArrayList<>();
        this.shieldRechargeRate = DEFAULT_SHIELD_RECHARGE_RATE;
        this.hullRepairRate = DEFAULT_HULL_REPAIR_RATE;
    }

    // ---- Accessors ----

    /**
     * Returns the unique identifier of this fleet.
     *
     * @return the fleet ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the display name of this fleet.
     *
     * @return the fleet name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this fleet.
     *
     * @param name the new name
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Fleet name must not be null or blank");
        }
        this.name = name;
    }

    /**
     * Returns the list of ships in this fleet.
     *
     * @return unmodifiable list of ships
     */
    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    /**
     * Returns the number of ships in this fleet.
     *
     * @return the ship count
     */
    public int getShipCount() {
        return ships.size();
    }

    /**
     * Returns true if this fleet has no ships.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return ships.isEmpty();
    }

    /**
     * Returns the current position of this fleet in space.
     *
     * @return the position, or null if not positioned
     */
    public Hex3D getPosition() {
        return position;
    }

    /**
     * Sets the position of this fleet in space.
     *
     * @param position the new position
     */
    public void setPosition(Hex3D position) {
        this.position = position;
    }

    /**
     * Moves this fleet to a new position.
     *
     * @param newPosition the new position
     * @return the distance moved
     */
    public int move(Hex3D newPosition) {
        if (position == null) {
            this.position = newPosition;
            return 0;
        }
        int distance = position.distance(newPosition);
        this.position = newPosition;
        return distance;
    }

    /**
     * Returns the home system planet of this fleet, or null if not assigned.
     *
     * @return the home system
     */
    public Planet getHomeSystem() {
        return homeSystem;
    }

    /**
     * Sets the home system planet for this fleet.
     *
     * @param planet the home system planet
     */
    public void setHomeSystem(Planet planet) {
        this.homeSystem = planet;
    }

    /**
     * Returns the owning player, or null if unowned.
     *
     * @return the owner
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
     * Returns the home space station, or null if not assigned.
     *
     * @return the home station
     */
    public SpaceStation getHomeStation() {
        return homeStation;
    }

    /**
     * Sets the home space station for this fleet.
     *
     * @param homeStation the home station
     */
    public void setHomeStation(SpaceStation homeStation) {
        this.homeStation = homeStation;
    }

    /**
     * Returns the shield recharge rate per turn.
     *
     * @return the shield recharge rate
     */
    public int getShieldRechargeRate() {
        return shieldRechargeRate;
    }

    /**
     * Sets the shield recharge rate per turn.
     *
     * @param rate the shield recharge rate
     */
    public void setShieldRechargeRate(int rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Shield recharge rate must be non-negative: " + rate);
        }
        this.shieldRechargeRate = rate;
    }

    /**
     * Returns the hull repair rate per turn.
     *
     * @return the hull repair rate
     */
    public int getHullRepairRate() {
        return hullRepairRate;
    }

    /**
     * Sets the hull repair rate per turn.
     *
     * @param rate the hull repair rate
     */
    public void setHullRepairRate(int rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Hull repair rate must be non-negative: " + rate);
        }
        this.hullRepairRate = rate;
    }

    // ---- Ship management ----

    /**
     * Adds a ship to this fleet.
     *
     * @param ship the ship to add
     * @return true if the ship was added
     */
    public boolean addShip(Ship ship) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship must not be null");
        }
        return ships.add(ship);
    }

    /**
     * Removes a ship from this fleet.
     *
     * @param ship the ship to remove
     * @return true if the ship was removed
     */
    public boolean removeShip(Ship ship) {
        return ships.remove(ship);
    }

    /**
     * Removes all ships from this fleet.
     */
    public void clearShips() {
        ships.clear();
    }

    /**
     * Returns the ships of a specific type in this fleet.
     *
     * @param shipType the ship type to filter by
     * @return list of ships matching the type
     */
    public List<Ship> getShipsByType(ShipType shipType) {
        return ships.stream()
                .filter(s -> s.getShipType() == shipType)
                .collect(Collectors.toList());
    }

    /**
     * Returns the destroyed ships in this fleet.
     *
     * @return list of destroyed ships
     */
    public List<Ship> getDestroyedShips() {
        return ships.stream()
                .filter(Ship::isDestroyed)
                .collect(Collectors.toList());
    }

    /**
     * Returns the active (non-destroyed) ships in this fleet.
     *
     * @return list of active ships
     */
    public List<Ship> getActiveShips() {
        return ships.stream()
                .filter(s -> !s.isDestroyed())
                .collect(Collectors.toList());
    }

    /**
     * Removes all destroyed ships from this fleet.
     *
     * @return the number of ships removed
     */
    public int removeDestroyedShips() {
        int before = ships.size();
        ships.removeIf(Ship::isDestroyed);
        return before - ships.size();
    }

    // ---- Fleet-level combat stats ----

    /**
     * Returns the total damage output of all active ships in this fleet.
     *
     * @return the total fleet damage
     */
    public int getTotalDamage() {
        return getActiveShips().stream()
                .mapToInt(Ship::getTotalDamage)
                .sum();
    }

    /**
     * Returns the effective damage of all active ships, including planet bonuses.
     *
     * @return the effective fleet damage
     */
    public int getEffectiveDamage() {
        return getActiveShips().stream()
                .mapToInt(Ship::getEffectiveDamage)
                .sum();
    }

    /**
     * Returns the total shield strength of all active ships.
     *
     * @return the total fleet shield points
     */
    public int getTotalShieldPoints() {
        return getActiveShips().stream()
                .mapToInt(Ship::getMaxShieldPoints)
                .sum();
    }

    /**
     * Returns the current remaining shield points of all active ships.
     *
     * @return the current fleet shield points
     */
    public int getCurrentShieldPoints() {
        return getActiveShips().stream()
                .mapToInt(Ship::getCurrentShieldPoints)
                .sum();
    }

    /**
     * Returns the total hull points of all active ships.
     *
     * @return the total fleet hull points
     */
    public int getTotalHullPoints() {
        return getActiveShips().stream()
                .mapToInt(Ship::getMaxHullPoints)
                .sum();
    }

    /**
     * Returns the current remaining hull points of all active ships.
     *
     * @return the current fleet hull points
     */
    public int getCurrentHullPoints() {
        return getActiveShips().stream()
                .mapToInt(Ship::getCurrentHullPoints)
                .sum();
    }

    /**
     * Returns the maximum combat range of all ships in this fleet.
     *
     * @return the max range tier
     */
    public int getMaxRange() {
        return getActiveShips().stream()
                .mapToInt(Ship::getMaxRange)
                .max()
                .orElse(0);
    }

    /**
     * Returns the fleet health ratio (current hull / max hull).
     *
     * @return ratio between 0.0 and 1.0
     */
    public double getFleetHealthRatio() {
        int max = getTotalHullPoints();
        if (max <= 0) return 0.0;
        return (double) getCurrentHullPoints() / max;
    }

    /**
     * Returns the minimum MDrive speed of all active ships in this fleet.
     * <p>
     * Fleet MDrive speed is limited by the slowest ship.
     * MDrive is used for movement within a solar system and during combat.
     * </p>
     *
     * @return the minimum MDrive speed, or 0 if no active ships
     */
    public int getFleetMDriveSpeed() {
        return getActiveShips().stream()
                .mapToInt(Ship::getMDriveSpeed)
                .min()
                .orElse(0);
    }

    /**
     * Returns the minimum JDrive speed of all active ships in this fleet.
     * <p>
     * Fleet JDrive speed is limited by the slowest ship.
     * JDrive is used for faster-than-light travel between solar systems via hyperlanes.
     * </p>
     *
     * @return the minimum JDrive speed, or 0 if no active ships
     */
    public int getFleetJDriveSpeed() {
        return getActiveShips().stream()
                .mapToInt(Ship::getJDriveSpeed)
                .min()
                .orElse(0);
    }

    /**
     * Returns the effective MDrive speed of this fleet, reduced by damage.
     * <p>
     * Speed is based on the slowest active ship, further reduced by fleet health.
     * </p>
     *
     * @return the effective MDrive speed (minimum 1 if fleet has ships)
     */
    public int getEffectiveFleetMDriveSpeed() {
        int base = getFleetMDriveSpeed();
        if (base <= 0) return 0;
        double healthRatio = getFleetHealthRatio();
        int speed = (int) Math.round(base * Math.max(0.1, healthRatio));
        return Math.max(1, speed);
    }

    /**
     * Returns the effective JDrive speed of this fleet, reduced by damage.
     * <p>
     * JDrive requires minimum fleet health to function.
     * If fleet health is below 20%, JDrive cannot operate.
     * </p>
     *
     * @return the effective JDrive speed (0 if fleet is too damaged)
     */
    public int getEffectiveFleetJDriveSpeed() {
        int base = getFleetJDriveSpeed();
        if (base <= 0) return 0;
        double healthRatio = getFleetHealthRatio();

        // JDrive requires minimum 20% fleet health
        if (healthRatio < 0.2) {
            return 0;
        }

        int speed = (int) Math.round(base * Math.max(0.1, healthRatio));
        return speed;
    }

    /**
     * Returns the minimum speed of all active ships in this fleet (alias for MDrive speed).
     * <p>
     * Fleet movement speed is limited by the slowest ship.
     * </p>
     *
     * @return the minimum base speed, or 0 if no active ships
     * @deprecated Use {@link #getFleetMDriveSpeed()} instead
     */
    @Deprecated
    public int getFleetSpeed() {
        return getFleetMDriveSpeed();
    }

    /**
     * Returns the effective movement speed of this fleet, reduced by damage (alias for MDrive speed).
     * <p>
     * Speed is based on the slowest active ship, further reduced by fleet health.
     * </p>
     *
     * @return the effective speed (minimum 1 if fleet has ships)
     * @deprecated Use {@link #getEffectiveFleetMDriveSpeed()} instead
     */
    @Deprecated
    public int getEffectiveFleetSpeed() {
        return getEffectiveFleetMDriveSpeed();
    }

    /**
     * Returns the maximum evasion chance of all active ships in this fleet.
     * <p>
     * The best evasion represents the fleet's ability to dodge incoming fire.
     * </p>
     *
     * @return the maximum base evasion (0.0 to 1.0)
     */
    public double getFleetEvasion() {
        return getActiveShips().stream()
                .mapToDouble(Ship::getBaseEvasion)
                .max()
                .orElse(0.0);
    }

    /**
     * Returns the weighted average evasion of all active ships.
     * <p>
     * Evasion is weighted by each ship's hull points, so larger ships contribute more.
     * </p>
     *
     * @return the weighted average evasion (0.0 to 1.0)
     */
    public double getAverageFleetEvasion() {
        List<Ship> active = getActiveShips();
        if (active.isEmpty()) return 0.0;

        double totalHull = active.stream().mapToDouble(Ship::getMaxHullPoints).sum();
        if (totalHull <= 0) return 0.0;

        double weightedSum = active.stream()
                .mapToDouble(s -> s.getBaseEvasion() * s.getMaxHullPoints())
                .sum();

        return weightedSum / totalHull;
    }

    /**
     * Returns the effective evasion modified by shields and damage.
     * <p>
     * Active shields provide a bonus; damage reduces evasion.
     * </p>
     *
     * @return the effective evasion (0.0 to 1.0)
     */
    public double getEffectiveFleetEvasion() {
        double base = getAverageFleetEvasion();
        if (base <= 0) return 0.0;

        // Shield bonus: if any ship has shields active, add 50% of base
        boolean hasShields = getCurrentShieldPoints() > 0;
        double evasion = base;
        if (hasShields) {
            evasion *= 1.5;
        }

        // Damage penalty: reduce evasion proportionally to damage
        double healthRatio = getFleetHealthRatio();
        evasion *= healthRatio;

        return Math.min(1.0, evasion);
    }

    /**
     * Returns the defense bonus from the home space station.
     * <p>
     * If the fleet has a home station, the station's point defense modules
     * provide a damage reduction bonus.
     * </p>
     *
     * @return the defense bonus (0.0 to 1.0)
     */
    public double getStationDefenseBonus() {
        if (homeStation == null || homeStation.isDestroyed()) {
            return 0.0;
        }
        return homeStation.getTotalPointDefense();
    }

    /**
     * Returns the effective damage received after applying station defense bonus.
     * <p>
     * Station point defense reduces incoming damage.
     * </p>
     *
     * @param incomingDamage the raw incoming damage
     * @return the reduced damage value
     */
    public int getEffectiveDamageReceived(int incomingDamage) {
        double bonus = getStationDefenseBonus();
        if (bonus <= 0) {
            return incomingDamage;
        }
        return (int) Math.round(incomingDamage * (1.0 - bonus));
    }

    /**
     * Returns true if all ships in the fleet are destroyed.
     *
     * @return true if the fleet is completely destroyed
     */
    public boolean isDestroyed() {
        return ships.isEmpty() || ships.stream().allMatch(Ship::isDestroyed);
    }

    // ---- Resource costs ----

    /**
     * Returns the total mineral cost to build all ships in this fleet.
     *
     * @return the total mineral cost
     */
    public int getTotalMineralCost() {
        return ships.stream().mapToInt(Ship::getMineralCost).sum();
    }

    /**
     * Returns the total energy cost to build all ships in this fleet.
     *
     * @return the total energy cost
     */
    public int getTotalEnergyCost() {
        return ships.stream().mapToInt(Ship::getEnergyCost).sum();
    }

    /**
     * Returns the strategic resources required to build all ships in this fleet.
     *
     * @return map of strategic resources to total required counts
     */
    public Map<StrategicResource, Integer> getTotalRequiredStrategicResources() {
        Map<StrategicResource, Integer> resources = new EnumMap<>(StrategicResource.class);
        for (Ship ship : ships) {
            Map<StrategicResource, Integer> shipResources = ship.getRequiredStrategicResources();
            for (Map.Entry<StrategicResource, Integer> entry : shipResources.entrySet()) {
                resources.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return Map.copyOf(resources);
    }

    /**
     * Returns the set of unique strategic resources required by all ships.
     *
     * @return set of required strategic resources
     */
    public Set<StrategicResource> getTotalRequiredResourceTypes() {
        return ships.stream()
                .flatMap(s -> s.getRequiredResourceTypes().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Checks if all ships in this fleet can be built given the available strategic resources.
     *
     * @param availableResources the available strategic resource deposits
     * @return true if all ships can be built
     */
    public boolean canBuildAllWith(Set<StrategicResource> availableResources) {
        return ships.stream().allMatch(s -> s.canBuildWith(availableResources));
    }

    // ---- Fleet operations ----

    /**
     * Recharges shields and repairs hull for all active ships by the configured rates.
     */
    public void endTurnRepair() {
        for (Ship ship : getActiveShips()) {
            ship.rechargeShields(shieldRechargeRate);
            ship.repairHull(hullRepairRate);
        }
    }

    /**
     * Fully repairs all ships in the fleet.
     */
    public void fullRepairAll() {
        for (Ship ship : ships) {
            ship.fullRepair();
        }
    }

    // ---- Combat simulation helpers (used by Battle) ----

    /**
     * Distributes damage across the fleet's ships.
     * <p>
     * Damage is first absorbed by shields, then hull. Ships are targeted in order
     * of lowest health ratio first (weakest ships take damage first).
     * </p>
     *
     * @param damage the total damage to distribute
     * @return the actual damage dealt
     */
    public int distributeDamage(int damage) {
        if (damage <= 0 || isDestroyed()) {
            return 0;
        }

        int remaining = damage;

        // Sort ships by health ratio (weakest first)
        List<Ship> sortedShips = getActiveShips().stream()
                .sorted((a, b) -> Double.compare(a.getHullHealthRatio(), b.getHullHealthRatio()))
                .collect(Collectors.toList());

        for (Ship ship : sortedShips) {
            if (remaining <= 0) break;
            int dealt = ship.takeDamage(remaining);
            remaining -= dealt;
        }

        return damage - remaining;
    }

    /**
     * Creates a deep copy of this fleet for simulation purposes.
     * Each ship is copied with the same components and current stats.
     *
     * @return a copy of this fleet with independent ship copies
     */
    Fleet copyFleet() {
        Fleet copy = new Fleet(id, name + " (copy)");
        copy.position = this.position;
        copy.homeSystem = this.homeSystem;
        copy.homeStation = this.homeStation;
        copy.shieldRechargeRate = this.shieldRechargeRate;
        copy.hullRepairRate = this.hullRepairRate;
        copy.owner = this.owner;
        for (Ship original : this.ships) {
            Ship shipCopy = new Ship(original.getId(), original.getShipType());
            shipCopy.setName(original.getName());
            for (ShipComponent comp : original.getComponents()) {
                shipCopy.addComponent(comp);
            }
            copy.ships.add(shipCopy);
        }
        return copy;
    }

    /**
     * Returns the number of active (non-destroyed) ships.
     *
     * @return the active ship count
     */
    int getActiveShipCount() {
        return getActiveShips().size();
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fleet fleet = (Fleet) o;
        return id == fleet.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Fleet{name='%s', ships=%d, damage=%d, shields=%d/%d, hull=%d/%d, mDrive=%d, jDrive=%d, evasion=%.0f%%, position=%s}",
                name, getShipCount(), getTotalDamage(),
                getCurrentShieldPoints(), getTotalShieldPoints(),
                getCurrentHullPoints(), getTotalHullPoints(),
                getFleetMDriveSpeed(), getFleetJDriveSpeed(), getFleetEvasion() * 100,
                position);
    }
}
