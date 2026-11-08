package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A naval fleet from Classic Traveller Book 5: High Guard.
 * <p>
 * A fleet is a large naval formation consisting of multiple squadrons
 * under a single flag officer. Fleets are organized hierarchically:
 * </p>
 * <ul>
 *   <li>Sub-Sectoral Fleet: 1–2 squadrons, commanded by a Commodore</li>
 *   <li>Sectoral Fleet: 3–6 squadrons, commanded by a Rear Admiral</li>
 *   <li>Sub-Sector Fleet: 6–12 squadrons, commanded by a Vice Admiral</li>
 *   <li>Sector Fleet: 12+ squadrons, commanded by an Admiral</li>
 * </ul>
 * <p>
 * Fleets can include support ships, carriers, and capital ships in addition
 * to combat squadrons.
 * </p>
 */
public class NavalFleet {

    private final int id;
    private String name;
    private final List<NavalSquadron> squadrons;
    private NavalOfficer commander;
    private FleetType fleetType;
    private TravellerWorld homePort;
    private TravellerWorld currentSystem;
    private final List<String> fleetLog;

    /**
     * The type of fleet, determining size and command structure.
     */
    public enum FleetType {
        /**
         * Sub-Sectoral Fleet — smallest fleet, 1–2 squadrons.
         */
        SUB_SECTORAL("Sub-Sectoral Fleet", 1, 2, NavalRank.COMMANDER),

        /**
         * Sectoral Fleet — medium fleet, 3–6 squadrons.
         */
        SECTORAL("Sectoral Fleet", 3, 6, NavalRank.REAR_ADMIRAL),

        /**
         * Sub-Sector Fleet — large fleet, 6–12 squadrons.
         */
        SUB_SECTOR("Sub-Sector Fleet", 6, 12, NavalRank.VICE_ADMIRAL),

        /**
         * Sector Fleet — largest fleet, 12+ squadrons.
         */
        SECTOR("Sector Fleet", 12, 50, NavalRank.ADMIRAL);

        private final String name;
        private final int minSquadrons;
        private final int maxSquadrons;
        private final NavalRank minCommandRank;

        FleetType(String name, int minSquadrons, int maxSquadrons, NavalRank minCommandRank) {
            this.name = name;
            this.minSquadrons = minSquadrons;
            this.maxSquadrons = maxSquadrons;
            this.minCommandRank = minCommandRank;
        }

        public String getName() {
            return name;
        }

        public int getMinSquadrons() {
            return minSquadrons;
        }

        public int getMaxSquadrons() {
            return maxSquadrons;
        }

        public NavalRank getMinCommandRank() {
            return minCommandRank;
        }
    }

    /**
     * Creates a new fleet with the given ID, name, and type.
     *
     * @param id the unique fleet identifier
     * @param name the fleet's name
     * @param fleetType the type of fleet
     */
    public NavalFleet(int id, String name, FleetType fleetType) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.squadrons = new ArrayList<>();
        this.commander = null;
        this.fleetType = Objects.requireNonNull(fleetType, "Fleet type must not be null");
        this.homePort = null;
        this.currentSystem = null;
        this.fleetLog = new ArrayList<>();
    }

    // ---- Accessors ----

    /**
     * Returns the fleet's unique ID.
     *
     * @return the fleet ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the fleet's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the fleet's name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /**
     * Returns the list of squadrons in this fleet (immutable).
     *
     * @return the squadrons list
     */
    public List<NavalSquadron> getSquadrons() {
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
     * Adds a squadron to this fleet.
     *
     * @param squadron the squadron to add
     * @return true if the squadron was added, false if the fleet is full
     */
    public boolean addSquadron(NavalSquadron squadron) {
        Objects.requireNonNull(squadron, "Squadron must not be null");
        if (squadrons.size() >= fleetType.getMaxSquadrons()) {
            return false;
        }
        squadrons.add(squadron);
        fleetLog.add("Squadron added: " + squadron.getName());
        return true;
    }

    /**
     * Removes a squadron from this fleet.
     *
     * @param squadron the squadron to remove
     * @return true if the squadron was removed
     */
    public boolean removeSquadron(NavalSquadron squadron) {
        boolean removed = squadrons.remove(squadron);
        if (removed) {
            fleetLog.add("Squadron removed: " + squadron.getName());
        }
        return removed;
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
            fleetLog.add("Warning: Commander rank below minimum for fleet type");
        }
        fleetLog.add("Commander assigned: " + officer.getName());
    }

    /**
     * Returns the fleet type.
     *
     * @return the fleet type
     */
    public FleetType getFleetType() {
        return fleetType;
    }

    /**
     * Sets the fleet type.
     *
     * @param fleetType the new fleet type
     */
    public void setFleetType(FleetType fleetType) {
        this.fleetType = Objects.requireNonNull(fleetType, "Fleet type must not be null");
        fleetLog.add("Fleet type changed to: " + fleetType.getName());
    }

    /**
     * Returns the home port of this fleet.
     *
     * @return the home port system, or null if not set
     */
    public TravellerWorld getHomePort() {
        return homePort;
    }

    /**
     * Sets the home port of this fleet.
     *
     * @param system the home port system
     */
    public void setHomePort(TravellerWorld system) {
        this.homePort = Objects.requireNonNull(system, "Home port must not be null");
        fleetLog.add("Home port set to: " + system.getName());
    }

    /**
     * Returns the current system where this fleet is located.
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
        fleetLog.add("Fleet moved to: " + system.getName());
    }

    /**
     * Returns the fleet's log (immutable).
     *
     * @return the fleet log
     */
    public List<String> getFleetLog() {
        return Collections.unmodifiableList(fleetLog);
    }

    // ---- Derived Properties ----

    /**
     * Returns the total number of ships in all squadrons.
     *
     * @return the total ship count
     */
    public int getTotalShipCount() {
        int total = 0;
        for (NavalSquadron squadron : squadrons) {
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
        for (NavalSquadron squadron : squadrons) {
            total += squadron.getTotalTonnage();
        }
        return total;
    }

    /**
     * Returns the total combat power of the fleet.
     *
     * @return the total combat power
     */
    public int getTotalCombatPower() {
        int power = 0;
        for (NavalSquadron squadron : squadrons) {
            power += squadron.getCombatPower();
        }
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
        int defense = 0;
        for (NavalSquadron squadron : squadrons) {
            defense += squadron.getDefenseRating();
        }
        if (commander != null) {
            defense += commander.getCompetenceRating();
        }
        return defense;
    }

    /**
     * Checks if the fleet is combat-ready.
     * <p>
     * A fleet is combat-ready if it has at least one squadron
     * and a commander.
     * </p>
     *
     * @return true if combat-ready
     */
    public boolean isCombatReady() {
        return !squadrons.isEmpty() && commander != null;
    }

    /**
     * Returns the fleet's strength status.
     *
     * @return the strength status description
     */
    public String getStrengthStatus() {
        int shipCount = getTotalShipCount();
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
     * Records an event in the fleet log.
     *
     * @param event the event description
     */
    public void logEvent(String event) {
        fleetLog.add(event);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(fleetType.getName()).append(")");
        sb.append(": ").append(squadrons.size()).append(" squadrons, ")
          .append(getTotalShipCount()).append(" ships");
        if (commander != null) {
            sb.append(", Cmdr: ").append(commander.getName());
        }
        return sb.toString();
    }
}
