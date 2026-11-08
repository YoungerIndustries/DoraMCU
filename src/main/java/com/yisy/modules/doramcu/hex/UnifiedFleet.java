package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A unified fleet that combines DoraMCU's Fleet with Traveller's NavalFleet.
 * <p>
 * This class extends DoraMCU's flat ship list with Traveller's hierarchical
 * squadron structure, fleet types, formations, and commander system.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Squadron-based organization (2-4 ships per squadron)</li>
 *   <li>Fleet types: Sub-Sectoral, Sectoral, Sub-Sector, Sector</li>
 *   <li>Formation bonuses from FleetFormation enum</li>
 *   <li>NavalOfficer commander with competence rating</li>
 *   <li>Fleet actions from FleetAction enum</li>
 *   <li>Hex3D positioning from DoraMCU Fleet</li>
 *   <li>Shield recharge and hull repair from DoraMCU Fleet</li>
 * </ul>
 */
public class UnifiedFleet extends Fleet {

    private final List<UnifiedSquadron> squadrons;
    private NavalOfficer commander;
    private NavalFleet.FleetType fleetType;
    private FleetFormation formation;
    private FleetAction currentAction;
    private TravellerWorld currentSystem;
    private final List<String> fleetLog;

    /**
     * Creates a new unified fleet with the given ID, name, and fleet type.
     *
     * @param id the unique identifier for this fleet
     * @param name the display name for this fleet
     * @param fleetType the Traveller fleet type
     */
    public UnifiedFleet(int id, String name, NavalFleet.FleetType fleetType) {
        super(id, name);
        this.squadrons = new ArrayList<>();
        this.commander = null;
        this.fleetType = Objects.requireNonNull(fleetType, "Fleet type must not be null");
        this.formation = FleetFormation.WEDGE;
        this.currentAction = FleetAction.HOLD;
        this.currentSystem = null;
        this.fleetLog = new ArrayList<>();
        logEvent("Fleet created: " + name + " (" + fleetType.getName() + ")");
    }

    /**
     * Creates a new unified fleet with default Sub-Sectoral type.
     *
     * @param id the unique identifier for this fleet
     * @param name the display name for this fleet
     */
    public UnifiedFleet(int id, String name) {
        this(id, name, NavalFleet.FleetType.SUB_SECTORAL);
    }

    // ---- Accessors ----

    /**
     * Returns the list of squadrons in this fleet (immutable).
     *
     * @return the squadrons list
     */
    public List<UnifiedSquadron> getSquadrons() {
        return Collections.unmodifiableList(squadrons);
    }

    /**
     * Returns the number of squadrons in this fleet.
     *
     * @return the squadron count
     */
    public int getSquadronCount() {
        return squadrons.size();
    }

    /**
     * Returns the fleet's commander, or null if unassigned.
     *
     * @return the commander officer
     */
    public NavalOfficer getCommander() {
        return commander;
    }

    /**
     * Sets the fleet's commander.
     *
     * @param officer the commanding officer
     */
    public void setCommander(NavalOfficer officer) {
        this.commander = Objects.requireNonNull(officer, "Commander must not be null");
        if (officer.getRank().getLevel() < fleetType.getMinCommandRank().getLevel()) {
            logEvent("Warning: Commander rank below minimum for fleet type");
        }
        logEvent("Commander assigned: " + officer.getName());
    }

    /**
     * Returns the fleet type.
     *
     * @return the fleet type
     */
    public NavalFleet.FleetType getFleetType() {
        return fleetType;
    }

    /**
     * Sets the fleet type.
     *
     * @param fleetType the new fleet type
     */
    public void setFleetType(NavalFleet.FleetType fleetType) {
        this.fleetType = Objects.requireNonNull(fleetType, "Fleet type must not be null");
        logEvent("Fleet type changed to: " + fleetType.getName());
    }

    /**
     * Returns the current formation of this fleet.
     *
     * @return the formation
     */
    public FleetFormation getFormation() {
        return formation;
    }

    /**
     * Changes the fleet's formation.
     *
     * @param newFormation the new formation
     */
    public void setFormation(FleetFormation newFormation) {
        this.formation = Objects.requireNonNull(newFormation, "Formation must not be null");
        logEvent("Formation changed to: " + newFormation.getName());
    }

    /**
     * Returns the current fleet action.
     *
     * @return the current action
     */
    public FleetAction getCurrentAction() {
        return currentAction;
    }

    /**
     * Sets the current fleet action for the next combat round.
     *
     * @param action the action to perform
     */
    public void setCurrentAction(FleetAction action) {
        this.currentAction = Objects.requireNonNull(action, "Action must not be null");
    }

    /**
     * Returns the current star system where this fleet is located.
     *
     * @return the current system, or null if not at a system
     */
    public TravellerWorld getCurrentSystem() {
        return currentSystem;
    }

    /**
     * Moves the fleet to a new star system.
     *
     * @param system the destination system
     */
    public void moveToSystem(TravellerWorld system) {
        this.currentSystem = Objects.requireNonNull(system, "System must not be null");
        logEvent("Fleet moved to: " + system.getName());
    }

    /**
     * Returns the home port of this fleet.
     *
     * @return the home port system, or null if not set
     */
    public TravellerWorld getHomePort() {
        return null;
    }

    /**
     * Returns the fleet's log (immutable).
     *
     * @return the fleet log
     */
    public List<String> getFleetLog() {
        return Collections.unmodifiableList(fleetLog);
    }

    // ---- Squadron Management ----

    /**
     * Adds a squadron to this fleet.
     *
     * @param squadron the squadron to add
     * @return true if the squadron was added, false if the fleet is full
     */
    public boolean addSquadron(UnifiedSquadron squadron) {
        Objects.requireNonNull(squadron, "Squadron must not be null");
        if (squadrons.size() >= fleetType.getMaxSquadrons()) {
            return false;
        }
        squadrons.add(squadron);
        logEvent("Squadron added: " + squadron.getName());
        return true;
    }

    /**
     * Removes a squadron from this fleet.
     *
     * @param squadron the squadron to remove
     * @return true if the squadron was removed
     */
    public boolean removeSquadron(UnifiedSquadron squadron) {
        boolean removed = squadrons.remove(squadron);
        if (removed) {
            logEvent("Squadron removed: " + squadron.getName());
        }
        return removed;
    }

    /**
     * Creates a new squadron and adds it to this fleet.
     *
     * @param name the squadron name
     * @return the new squadron
     */
    public UnifiedSquadron createSquadron(String name) {
        UnifiedSquadron squadron = new UnifiedSquadron(squadrons.size(), name);
        addSquadron(squadron);
        return squadron;
    }

    // ---- Derived Properties ----

    /**
     * Returns the total number of ships in all squadrons.
     *
     * @return the total ship count
     */
    public int getTotalSquadronShipCount() {
        int total = 0;
        for (UnifiedSquadron squadron : squadrons) {
            total += squadron.getShipCount();
        }
        return total;
    }

    /**
     * Returns the total tonnage of all ships in the fleet.
     *
     * @return the total tonnage
     */
    public int getTotalTonnage() {
        int total = 0;
        for (UnifiedSquadron squadron : squadrons) {
            total += squadron.getTotalTonnage();
        }
        // Also count ships not in squadrons
        for (Ship ship : getShips()) {
            if (ship instanceof UnifiedShip us) {
                total += us.getTotalTonnage();
            }
        }
        return total;
    }

    /**
     * Returns the total combat power of the fleet.
     * <p>
     * Combat power is derived from ship count, tonnage, weapons,
     * formation bonuses, and commander competence.
     * </p>
     *
     * @return the total combat power
     */
    public int getTotalCombatPower() {
        int power = getTotalDamage();
        // Formation bonus
        power += formation.getFrontArcBonus() * getActiveShipCount();
        // Commander bonus
        if (commander != null) {
            power += commander.getCompetenceRating() * 2;
        }
        return power;
    }

    /**
     * Returns the total defense rating of the fleet.
     *
     * @return the total defense rating
     */
    public int getTotalDefenseRating() {
        int defense = getTotalHullPoints();
        // Formation defense bonus
        defense += formation.getDefenseBonus() * getActiveShipCount();
        // Commander bonus
        if (commander != null) {
            defense += commander.getCompetenceRating();
        }
        return defense;
    }

    /**
     * Checks if the fleet is combat-ready.
     * <p>
     * A fleet is combat-ready if it has at least one ship and a commander.
     * </p>
     *
     * @return true if combat-ready
     */
    public boolean isCombatReady() {
        return !isEmpty() && commander != null;
    }

    /**
     * Returns the fleet's strength status.
     *
     * @return the strength status description
     */
    public String getStrengthStatus() {
        int shipCount = getShipCount();
        if (shipCount == 0) {
            return "Empty";
        } else if (shipCount < 4) {
            return "Weak";
        } else if (shipCount < 12) {
            return "Moderate";
        } else if (shipCount < 24) {
            return "Strong";
        } else {
            return "Overwhelming";
        }
    }

    /**
     * Returns the combat modifier from the current action.
     *
     * @return the combat modifier
     */
    public int getActionCombatModifier() {
        return currentAction.getCombatModifier();
    }

    /**
     * Returns the defense modifier from the current action.
     *
     * @return the defense modifier
     */
    public int getActionDefenseModifier() {
        return currentAction.getDefenseModifier();
    }

    private void logEvent(String event) {
        fleetLog.add(event);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" (").append(fleetType.getName()).append(")");
        sb.append(": ").append(squadrons.size()).append(" squadrons, ")
          .append(getShipCount()).append(" ships");
        if (commander != null) {
            sb.append(", Cmdr: ").append(commander.getName());
        }
        sb.append(", Formation: ").append(formation.getName());
        return sb.toString();
    }
}
