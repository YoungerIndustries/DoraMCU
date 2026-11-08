package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.ArmyUnit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an Army character's tour of duty assigned to a military unit.
 * <p>
 * An army character does not control the unit — they serve as an observer
 * who records the unit's experiences in their life history and book.
 * The tour lasts for a fixed number of turns, after which the character
 * may be reassigned or retire.
 * </p>
 * <p>
 * Each turn, the unit's activities (combat, deployment, casualties,
 * experience gains) are captured and converted into character experiences.
 * </p>
 */
public class ArmyUnitAssignment {

    private final int id;
    private final TravellerCharacter character;
    private final ArmyUnit assignedUnit;
    private final int unitId;
    private final String unitName;
    private final String empireName;
    private final String planetName;
    private final int totalTurns;
    private int turnsServed;
    private final LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    /** Events recorded during this assignment. */
    private final List<UnitEvent> events;

    /**
     * Creates a new army unit assignment.
     *
     * @param id the unique assignment ID
     * @param character the Traveller character serving the assignment
     * @param unit the army unit assigned to
     * @param empireName the name of the empire
     * @param planetName the planet where the unit is stationed
     * @param totalTurns the number of turns in this assignment
     */
    public ArmyUnitAssignment(int id, TravellerCharacter character, ArmyUnit unit,
                               String empireName, String planetName, int totalTurns) {
        if (character == null) {
            throw new IllegalArgumentException("Character must not be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (totalTurns <= 0) {
            throw new IllegalArgumentException("Total turns must be positive");
        }
        this.id = id;
        this.character = character;
        this.assignedUnit = unit;
        this.unitId = unit.getId();
        this.unitName = unit.getName();
        this.empireName = empireName != null ? empireName : "Unknown Empire";
        this.planetName = planetName != null ? planetName : "Unknown Planet";
        this.totalTurns = totalTurns;
        this.turnsServed = 0;
        this.startDate = LocalDate.now();
        this.active = true;
        this.events = new ArrayList<>();
    }

    // ---- Accessors ----

    /** @return the unique assignment ID */
    public int getId() {
        return id;
    }

    /** @return the character serving this assignment */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /** @return the assigned unit (may be null if unit was destroyed) */
    public ArmyUnit getAssignedUnit() {
        return assignedUnit;
    }

    /** @return the unit ID at assignment time */
    public int getUnitId() {
        return unitId;
    }

    /** @return the unit name at assignment time */
    public String getUnitName() {
        return unitName;
    }

    /** @return the empire name */
    public String getEmpireName() {
        return empireName;
    }

    /** @return the planet name where the unit is stationed */
    public String getPlanetName() {
        return planetName;
    }

    /** @return the total number of turns in this assignment */
    public int getTotalTurns() {
        return totalTurns;
    }

    /** @return the number of turns served so far */
    public int getTurnsServed() {
        return turnsServed;
    }

    /** @return the remaining turns in this assignment */
    public int getRemainingTurns() {
        return Math.max(0, totalTurns - turnsServed);
    }

    /** @return the start date of this assignment */
    public LocalDate getStartDate() {
        return startDate;
    }

    /** @return the end date of this assignment, or null if still active */
    public LocalDate getEndDate() {
        return endDate;
    }

    /** @return true if the assignment is still active */
    public boolean isActive() {
        return active;
    }

    /** @return true if the assignment is completed (all turns served) */
    public boolean isCompleted() {
        return turnsServed >= totalTurns;
    }

    /** @return an unmodifiable list of events recorded during this assignment */
    public List<UnitEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    // ---- Assignment progression ----

    /**
     * Advances the assignment by one turn.
     *
     * @return true if the assignment is completed after this turn
     */
    public boolean advanceTurn() {
        if (!active) {
            throw new IllegalStateException("Assignment is not active");
        }
        turnsServed++;
        if (turnsServed >= totalTurns) {
            complete();
            return true;
        }
        return false;
    }

    /**
     * Records an event during this assignment.
     *
     * @param eventType the type of event
     * @param description a description of the event
     */
    public void recordEvent(UnitEventType eventType, String description) {
        if (!active) {
            throw new IllegalStateException("Cannot record events on inactive assignment");
        }
        events.add(new UnitEvent(turnsServed, eventType, description));
    }

    /**
     * Completes the assignment early.
     */
    public void complete() {
        this.active = false;
        this.endDate = LocalDate.now();
    }

    /**
     * Terminates the assignment due to unit destruction or character death.
     */
    public void terminate(String reason) {
        this.active = false;
        this.endDate = LocalDate.now();
        if (reason != null) {
            events.add(new UnitEvent(turnsServed, UnitEventType.ASSIGNMENT_TERMINATED, reason));
        }
    }

    // ---- Unit Event ----

    /**
     * Types of events that can occur during an army unit assignment.
     */
    public enum UnitEventType {
        /** The unit engaged in combat. */
        COMBAT_ENGAGED,
        /** The unit won a combat engagement. */
        COMBAT_VICTORY,
        /** The unit lost a combat engagement. */
        COMBAT_DEFEAT,
        /** The unit retreated from combat. */
        RETREAT,
        /** The unit suffered casualties. */
        CASUALTIES,
        /** The unit was reinforced. */
        REINFORCEMENT,
        /** The unit was deployed to a new location. */
        DEPLOYMENT,
        /** The unit took damage. */
        DAMAGE_TAKEN,
        /** The unit recovered strength. */
        RECOVERY,
        /** The unit was destroyed. */
        UNIT_DESTROYED,
        /** The unit gained experience. */
        EXPERIENCE_GAINED,
        /** The unit's morale changed. */
        MORALE_CHANGE,
        /** A significant discovery was made. */
        DISCOVERY,
        /** The assignment was terminated early. */
        ASSIGNMENT_TERMINATED,
        /** Other notable event. */
        OTHER
    }

    /**
     * Represents a single event recorded during an assignment.
     */
    public static class UnitEvent {
        private final int turnNumber;
        private final UnitEventType eventType;
        private final String description;

        UnitEvent(int turnNumber, UnitEventType eventType, String description) {
            this.turnNumber = turnNumber;
            this.eventType = eventType;
            this.description = description;
        }

        /** @return the turn number when this event occurred */
        public int getTurnNumber() {
            return turnNumber;
        }

        /** @return the type of event */
        public UnitEventType getEventType() {
            return eventType;
        }

        /** @return a description of the event */
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return String.format("UnitEvent{turn=%d, type=%s, desc='%s'}",
                    turnNumber, eventType, description);
        }
    }

    @Override
    public String toString() {
        return String.format("ArmyUnitAssignment{id=%d, char='%s', unit='%s', turns=%d/%d, active=%s, events=%d}",
                id, character.getName(), unitName, turnsServed, totalTurns, active, events.size());
    }
}
