package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * A crew system that manages TravellerCharacter assignments to ships and stations.
 * <p>
 * This class provides crew management for UnifiedShip and UnifiedSpaceStation,
 * combining Traveller's character skills with DoraMCU's ship operations.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Crew assignments to ships and stations</li>
 *   <li>Skill bonuses: pilot affects MDrive speed, gunnery affects damage</li>
 *   <li>Career bonuses: Naval career provides fleet command bonuses</li>
 *   <li>Crew quarters management in HullModule cells</li>
 *   <li>Crew casualty tracking</li>
 * </ul>
 */
public class CrewSystem {

    private final Map<TravellerCharacter, CrewAssignment> assignments;
    private final List<CrewAssignment> casualties;
    private final List<String> eventLog;

    /**
     * Represents a crew member's assignment.
     */
    public static class CrewAssignment {
        private final TravellerCharacter character;
        private final AssignmentType type;
        private Ship assignedShip;
        private SpaceStation assignedStation;
        private boolean isCasualty;

        public enum AssignmentType {
            CAPTAIN,
            PILOT,
            GUNNER,
            ENGINEER,
            MEDIC,
            MARINE,
            CREW
        }

        CrewAssignment(TravellerCharacter character, AssignmentType type) {
            this.character = Objects.requireNonNull(character);
            this.type = type;
            this.isCasualty = false;
        }

        public TravellerCharacter getCharacter() {
            return character;
        }

        public AssignmentType getType() {
            return type;
        }

        public Ship getAssignedShip() {
            return assignedShip;
        }

        public void setAssignedShip(Ship ship) {
            this.assignedShip = ship;
        }

        public SpaceStation getAssignedStation() {
            return assignedStation;
        }

        public void setAssignedStation(SpaceStation station) {
            this.assignedStation = station;
        }

        public boolean isCasualty() {
            return isCasualty;
        }

        public void setCasualty(boolean casualty) {
            this.isCasualty = casualty;
        }

        public boolean isActive() {
            return !isCasualty;
        }

        @Override
        public String toString() {
            return String.format("CrewAssignment{character=%s, type=%s, ship=%s, casualty=%s}",
                    character.getName(), type,
                    assignedShip != null ? assignedShip.getName() : "none",
                    isCasualty);
        }
    }

    /**
     * Creates a new crew system.
     */
    public CrewSystem() {
        this.assignments = new HashMap<>();
        this.casualties = new ArrayList<>();
        this.eventLog = new ArrayList<>();
    }

    // ---- Crew Management ----

    /**
     * Assigns a character to a ship.
     *
     * @param character the character to assign
     * @param type the assignment type
     * @param ship the ship to assign to
     */
    public void assignToShip(TravellerCharacter character, CrewAssignment.AssignmentType type, Ship ship) {
        Objects.requireNonNull(character, "Character must not be null");
        Objects.requireNonNull(ship, "Ship must not be null");

        CrewAssignment assignment = assignments.get(character);
        if (assignment == null) {
            assignment = new CrewAssignment(character, type);
            assignments.put(character, assignment);
            logEvent(character.getName() + " assigned as " + type + " to " + ship.getName());
        }
        assignment.assignedShip = ship;
        assignment.assignedStation = null;
        assignment.isCasualty = false;
    }

    /**
     * Assigns a character to a station.
     *
     * @param character the character to assign
     * @param type the assignment type
     * @param station the station to assign to
     */
    public void assignToStation(TravellerCharacter character, CrewAssignment.AssignmentType type, SpaceStation station) {
        Objects.requireNonNull(character, "Character must not be null");
        Objects.requireNonNull(station, "Station must not be null");

        CrewAssignment assignment = assignments.get(character);
        if (assignment == null) {
            assignment = new CrewAssignment(character, type);
            assignments.put(character, assignment);
            logEvent(character.getName() + " assigned as " + type + " to " + station.getName());
        }
        assignment.assignedStation = station;
        assignment.assignedShip = null;
        assignment.isCasualty = false;
    }

    /**
     * Removes a character's assignment.
     *
     * @param character the character to remove
     * @return the removed assignment, or null if not found
     */
    public CrewAssignment removeAssignment(TravellerCharacter character) {
        CrewAssignment removed = assignments.remove(character);
        if (removed != null) {
            logEvent(character.getName() + " removed from crew");
        }
        return removed;
    }

    /**
     * Returns the assignment for a character, or null if not assigned.
     */
    public CrewAssignment getAssignment(TravellerCharacter character) {
        return assignments.get(character);
    }

    /**
     * Returns all assignments (immutable).
     */
    public Map<TravellerCharacter, CrewAssignment> getAllAssignments() {
        return Collections.unmodifiableMap(assignments);
    }

    /**
     * Returns the crew assigned to a specific ship.
     */
    public List<CrewAssignment> getCrewForShip(Ship ship) {
        List<CrewAssignment> crew = new ArrayList<>();
        for (CrewAssignment assignment : assignments.values()) {
            if (assignment.assignedShip == ship && assignment.isActive()) {
                crew.add(assignment);
            }
        }
        return Collections.unmodifiableList(crew);
    }

    /**
     * Returns the crew assigned to a specific station.
     */
    public List<CrewAssignment> getCrewForStation(SpaceStation station) {
        List<CrewAssignment> crew = new ArrayList<>();
        for (CrewAssignment assignment : assignments.values()) {
            if (assignment.assignedStation == station && assignment.isActive()) {
                crew.add(assignment);
            }
        }
        return Collections.unmodifiableList(crew);
    }

    /**
     * Returns the crew of a specific type.
     */
    public List<CrewAssignment> getCrewByType(CrewAssignment.AssignmentType type) {
        List<CrewAssignment> crew = new ArrayList<>();
        for (CrewAssignment assignment : assignments.values()) {
            if (assignment.type == type && assignment.isActive()) {
                crew.add(assignment);
            }
        }
        return Collections.unmodifiableList(crew);
    }

    // ---- Skill Bonuses ----

    /**
     * Returns the pilot skill bonus for a ship.
     * <p>
     * Pilot skill affects MDrive speed.
     * </p>
     *
     * @param ship the ship
     * @return the pilot bonus
     */
    public int getPilotBonus(Ship ship) {
        List<CrewAssignment> pilots = getCrewByType(CrewAssignment.AssignmentType.PILOT);
        int bonus = 0;
        for (CrewAssignment assignment : pilots) {
            if (assignment.assignedShip == ship) {
                bonus += 1;
            }
        }
        return bonus;
    }

    /**
     * Returns the gunnery skill bonus for a ship.
     * <p>
     * Gunnery skill affects damage output.
     * </p>
     *
     * @param ship the ship
     * @return the gunnery bonus
     */
    public int getGunneryBonus(Ship ship) {
        List<CrewAssignment> gunners = getCrewByType(CrewAssignment.AssignmentType.GUNNER);
        int bonus = 0;
        for (CrewAssignment assignment : gunners) {
            if (assignment.assignedShip == ship) {
                bonus += 1;
            }
        }
        return bonus;
    }

    /**
     * Returns the engineering skill bonus for a ship.
     * <p>
     * Engineering skill affects repair and shield recharge rates.
     * </p>
     *
     * @param ship the ship
     * @return the engineering bonus
     */
    public int getEngineeringBonus(Ship ship) {
        List<CrewAssignment> engineers = getCrewByType(CrewAssignment.AssignmentType.ENGINEER);
        int bonus = 0;
        for (CrewAssignment assignment : engineers) {
            if (assignment.assignedShip == ship) {
                bonus += 1;
            }
        }
        return bonus;
    }

    /**
     * Returns the captain's command bonus for a fleet.
     * <p>
     * Naval career provides fleet command bonuses.
     * </p>
     *
     * @param fleet the fleet
     * @return the command bonus
     */
    public int getCommandBonus(Fleet fleet) {
        List<CrewAssignment> captains = getCrewByType(CrewAssignment.AssignmentType.CAPTAIN);
        int bonus = 0;
        for (CrewAssignment assignment : captains) {
            if (assignment.assignedShip != null && fleet.getShips().contains(assignment.assignedShip)) {
                bonus += 1;
            }
        }
        return bonus;
    }

    // ---- Casualty Management ----

    /**
     * Records a crew casualty.
     *
     * @param character the character who became a casualty
     */
    public void recordCasualty(TravellerCharacter character) {
        CrewAssignment assignment = assignments.get(character);
        if (assignment != null) {
            assignment.isCasualty = true;
            casualties.add(assignment);
            logEvent(character.getName() + " is a casualty");
        }
    }

    /**
     * Returns the list of casualties (immutable).
     */
    public List<CrewAssignment> getCasualties() {
        return Collections.unmodifiableList(casualties);
    }

    /**
     * Returns the number of active crew members.
     */
    public int getActiveCrewCount() {
        int count = 0;
        for (CrewAssignment assignment : assignments.values()) {
            if (assignment.isActive()) count++;
        }
        return count;
    }

    /**
     * Returns the total crew size.
     */
    public int getTotalCrewSize() {
        return assignments.size();
    }

    /**
     * Returns the event log (immutable).
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }

    @Override
    public String toString() {
        return String.format("CrewSystem{total=%d, active=%d, casualties=%d}",
                getTotalCrewSize(), getActiveCrewCount(), casualties.size());
    }
}
