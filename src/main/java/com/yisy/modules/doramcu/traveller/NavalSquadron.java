package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A naval squadron from Classic Traveller Book 5: High Guard.
 * <p>
 * A squadron is a group of 2–4 ships under a single commander.
 * Squadrons are the basic tactical unit of the Imperial Navy and
 * can operate independently or as part of a larger fleet.
 * </p>
 * <p>
 * Squadron types:
 * </p>
 * <ul>
 *   <li>Destroyer Squadron: 2–4 destroyers</li>
 *   <li>Cruiser Squadron: 2–4 cruisers</li>
 *   <li>Mixed Squadron: combination of ship types</li>
 * </ul>
 * <p>
 * A squadron is commanded by a Commodore (flag officer) or, for
 * smaller squadrons, a Captain.
 * </p>
 */
public class NavalSquadron {

    private final int id;
    private String name;
    private final List<ShipDesign> ships;
    private NavalOfficer commander;
    private FleetFormation formation;
    private TravellerWorld currentSystem;
    private final List<String> battleLog;

    /**
     * Creates a new squadron with the given ID and name.
     *
     * @param id the unique squadron identifier
     * @param name the squadron's name
     */
    public NavalSquadron(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.ships = new ArrayList<>();
        this.commander = null;
        this.formation = FleetFormation.WEDGE;
        this.currentSystem = null;
        this.battleLog = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * Returns the squadron's unique ID.
     *
     * @return the squadron ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the squadron's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the squadron's name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /**
     * Returns the list of ships in this squadron (immutable).
     *
     * @return the ships list
     */
    public List<ShipDesign> getShips() {
        return Collections.unmodifiableList(ships);
    }

    /**
     * Returns the number of ships in this squadron.
     *
     * @return the ship count
     */
    public int getShipCount() {
        return ships.size();
    }

    /**
     * Adds a ship to this squadron.
     *
     * @param ship the ship design to add
     * @return true if the ship was added, false if the squadron is full (4 ships max)
     */
    public boolean addShip(ShipDesign ship) {
        Objects.requireNonNull(ship, "Ship must not be null");
        if (ships.size() >= 4) {
            return false;
        }
        ships.add(ship);
        battleLog.add("Ship added: " + ship.getName());
        return true;
    }

    /**
     * Removes a ship from this squadron.
     *
     * @param ship the ship design to remove
     * @return true if the ship was removed
     */
    public boolean removeShip(ShipDesign ship) {
        boolean removed = ships.remove(ship);
        if (removed) {
            battleLog.add("Ship removed: " + ship.getName());
        }
        return removed;
    }

    /**
     * Returns the squadron's commander, or null if unassigned.
     *
     * @return the commander officer
     */
    public NavalOfficer getCommander() {
        return commander;
    }

    /**
     * Sets the squadron's commander.
     *
     * @param officer the commanding officer
     */
    public void setCommander(NavalOfficer officer) {
        this.commander = Objects.requireNonNull(officer, "Commander must not be null");
        if (!officer.isFlagOfficer() && ships.size() > 1) {
            battleLog.add("Warning: Non-flag officer commanding multi-ship squadron");
        }
        battleLog.add("Commander assigned: " + officer.getName());
    }

    /**
     * Returns the current formation of this squadron.
     *
     * @return the formation
     */
    public FleetFormation getFormation() {
        return formation;
    }

    /**
     * Changes the squadron's formation.
     *
     * @param newFormation the new formation
     */
    public void setFormation(FleetFormation newFormation) {
        this.formation = Objects.requireNonNull(newFormation, "Formation must not be null");
        battleLog.add("Formation changed to: " + newFormation.getName());
    }

    /**
     * Returns the star system where this squadron is currently located.
     *
     * @return the current system, or null if not at a system
     */
    public TravellerWorld getCurrentSystem() {
        return currentSystem;
    }

    /**
     * Moves the squadron to a new star system.
     *
     * @param system the destination system
     */
    public void moveToSystem(TravellerWorld system) {
        this.currentSystem = Objects.requireNonNull(system, "System must not be null");
        battleLog.add("Moved to system: " + system.getName());
    }

    /**
     * Returns the squadron's battle log (immutable).
     *
     * @return the battle log
     */
    public List<String> getBattleLog() {
        return Collections.unmodifiableList(battleLog);
    }

    // ---- Derived Properties ----

    /**
     * Returns the total tonnage of all ships in the squadron.
     *
     * @return the total tonnage
     */
    public int getTotalTonnage() {
        int total = 0;
        for (ShipDesign ship : ships) {
            total += ship.getTotalTonnage();
        }
        return total;
    }

    /**
     * Returns the total number of laser banks across all ships.
     *
     * @return the total laser count
     */
    public int getTotalLasers() {
        int total = 0;
        for (ShipDesign ship : ships) {
            for (ShipWeaponSystem weapon : ship.getWeapons()) {
                if (weapon.getWeaponType().isLaser()) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Returns the total number of missile launchers across all ships.
     *
     * @return the total missile launcher count
     */
    public int getTotalMissileLaunchers() {
        int total = 0;
        for (ShipDesign ship : ships) {
            for (ShipWeaponSystem weapon : ship.getWeapons()) {
                if (weapon.getWeaponType().isMissile()) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Returns the combined hull points of all ships.
     *
     * @return the total hull points
     */
    public int getTotalHullPoints() {
        int total = 0;
        for (ShipDesign ship : ships) {
            total += ship.getHullPoints();
        }
        return total;
    }

    /**
     * Returns the squadron's combat power rating.
     * <p>
     * Combat power is derived from ship count, tonnage, weapons,
     * and commander competence.
     * </p>
     *
     * @return the combat power rating
     */
    public int getCombatPower() {
        int power = getTotalTonnage() / 1000;
        power += getTotalLasers() * 2;
        power += getTotalMissileLaunchers() * 3;
        if (commander != null) {
            power += commander.getCompetenceRating();
        }
        return power;
    }

    /**
     * Returns the squadron's defense rating.
     * <p>
     * Defense is derived from formation, ship hull points,
     * and commander competence.
     * </p>
     *
     * @return the defense rating
     */
    public int getDefenseRating() {
        int defense = getTotalHullPoints();
        defense += formation.getDefenseBonus() * ships.size();
        if (commander != null) {
            defense += commander.getCompetenceRating() / 2;
        }
        return defense;
    }

    /**
     * Checks if the squadron is combat-ready.
     * <p>
     * A squadron is combat-ready if it has at least one ship
     * and a commander.
     * </p>
     *
     * @return true if combat-ready
     */
    public boolean isCombatReady() {
        return !ships.isEmpty() && commander != null;
    }

    /**
     * Records an event in the battle log.
     *
     * @param event the event description
     */
    public void logEvent(String event) {
        battleLog.add(event);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" Squadron (").append(ships.size()).append(" ships)");
        if (commander != null) {
            sb.append(", Cmdr: ").append(commander.getName());
        }
        sb.append(", Formation: ").append(formation.getName());
        return sb.toString();
    }
}
