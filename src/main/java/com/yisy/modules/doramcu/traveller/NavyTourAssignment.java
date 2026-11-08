package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.Fleet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Navy character's tour of duty assigned to a fleet.
 * <p>
 * A navy character does not control the fleet — they serve as an observer
 * who records the fleet's experiences in their life history and book.
 * The tour lasts for a fixed number of turns, after which the character
 * may be reassigned or retire.
 * </p>
 * <p>
 * Each turn, the fleet's activities (battles, deployments, ship losses,
 * reinforcements) are captured and converted into character experiences.
 * </p>
 */
public class NavyTourAssignment {

    private final int id;
    private final TravellerCharacter character;
    private final Fleet assignedFleet;
    private final int fleetId;
    private final String fleetName;
    private final String empireName;
    private final int totalTurns;
    private int turnsServed;
    private final LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    /** Events recorded during this tour. */
    private final List<TourEvent> events;

    /**
     * Creates a new navy tour assignment.
     *
     * @param id the unique tour ID
     * @param character the Traveller character serving the tour
     * @param fleet the fleet assigned to
     * @param empireName the name of the empire
     * @param totalTurns the number of turns in this tour
     */
    public NavyTourAssignment(int id, TravellerCharacter character, Fleet fleet,
                               String empireName, int totalTurns) {
        if (character == null) {
            throw new IllegalArgumentException("Character must not be null");
        }
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet must not be null");
        }
        if (totalTurns <= 0) {
            throw new IllegalArgumentException("Total turns must be positive");
        }
        this.id = id;
        this.character = character;
        this.assignedFleet = fleet;
        this.fleetId = fleet.getId();
        this.fleetName = fleet.getName();
        this.empireName = empireName != null ? empireName : "Unknown Empire";
        this.totalTurns = totalTurns;
        this.turnsServed = 0;
        this.startDate = LocalDate.now();
        this.active = true;
        this.events = new ArrayList<>();
    }

    // ---- Accessors ----

    /** @return the unique tour ID */
    public int getId() {
        return id;
    }

    /** @return the character serving this tour */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /** @return the assigned fleet (may be null if fleet was destroyed) */
    public Fleet getAssignedFleet() {
        return assignedFleet;
    }

    /** @return the fleet ID at assignment time */
    public int getFleetId() {
        return fleetId;
    }

    /** @return the fleet name at assignment time */
    public String getFleetName() {
        return fleetName;
    }

    /** @return the empire name */
    public String getEmpireName() {
        return empireName;
    }

    /** @return the total number of turns in this tour */
    public int getTotalTurns() {
        return totalTurns;
    }

    /** @return the number of turns served so far */
    public int getTurnsServed() {
        return turnsServed;
    }

    /** @return the remaining turns in this tour */
    public int getRemainingTurns() {
        return Math.max(0, totalTurns - turnsServed);
    }

    /** @return the start date of this tour */
    public LocalDate getStartDate() {
        return startDate;
    }

    /** @return the end date of this tour, or null if still active */
    public LocalDate getEndDate() {
        return endDate;
    }

    /** @return true if the tour is still active */
    public boolean isActive() {
        return active;
    }

    /** @return true if the tour is completed (all turns served) */
    public boolean isCompleted() {
        return turnsServed >= totalTurns;
    }

    /** @return an unmodifiable list of events recorded during this tour */
    public List<TourEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    // ---- Tour progression ----

    /**
     * Advances the tour by one turn.
     *
     * @return true if the tour is completed after this turn
     */
    public boolean advanceTurn() {
        if (!active) {
            throw new IllegalStateException("Tour is not active");
        }
        turnsServed++;
        if (turnsServed >= totalTurns) {
            complete();
            return true;
        }
        return false;
    }

    /**
     * Records an event during this tour.
     *
     * @param eventType the type of event
     * @param description a description of the event
     */
    public void recordEvent(TourEventType eventType, String description) {
        if (!active) {
            throw new IllegalStateException("Cannot record events on inactive tour");
        }
        events.add(new TourEvent(turnsServed, eventType, description));
    }

    /**
     * Completes the tour early.
     */
    public void complete() {
        this.active = false;
        this.endDate = LocalDate.now();
    }

    /**
     * Terminates the tour due to fleet destruction or character death.
     */
    public void terminate(String reason) {
        this.active = false;
        this.endDate = LocalDate.now();
        if (reason != null) {
            events.add(new TourEvent(turnsServed, TourEventType.TOUR_TERMINATED, reason));
        }
    }

    // ---- Tour Event ----

    /**
     * Types of events that can occur during a navy tour.
     */
    public enum TourEventType {
        /** The fleet engaged in a battle. */
        BATTLE_ENGAGED,
        /** The fleet won a battle. */
        BATTLE_VICTORY,
        /** The fleet lost a battle. */
        BATTLE_DEFEAT,
        /** The fleet retreated from combat. */
        RETREAT,
        /** Ships were lost in combat. */
        SHIPS_LOST,
        /** New ships were added to the fleet. */
        REINFORCEMENT,
        /** The fleet was deployed to a new position. */
        DEPLOYMENT,
        /** The fleet suffered damage. */
        DAMAGE_TAKEN,
        /** The fleet repaired ships or systems. */
        REPAIR,
        /** The fleet was destroyed. */
        FLEET_DESTROYED,
        /** A significant discovery was made. */
        DISCOVERY,
        /** The tour was terminated early. */
        TOUR_TERMINATED,
        /** Other notable event. */
        OTHER
    }

    /**
     * Represents a single event recorded during a tour.
     */
    public static class TourEvent {
        private final int turnNumber;
        private final TourEventType eventType;
        private final String description;

        TourEvent(int turnNumber, TourEventType eventType, String description) {
            this.turnNumber = turnNumber;
            this.eventType = eventType;
            this.description = description;
        }

        /** @return the turn number when this event occurred */
        public int getTurnNumber() {
            return turnNumber;
        }

        /** @return the type of event */
        public TourEventType getEventType() {
            return eventType;
        }

        /** @return a description of the event */
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return String.format("TourEvent{turn=%d, type=%s, desc='%s'}",
                    turnNumber, eventType, description);
        }
    }

    @Override
    public String toString() {
        return String.format("NavyTour{id=%d, char='%s', fleet='%s', turns=%d/%d, active=%s, events=%d}",
                id, character.getName(), fleetName, turnsServed, totalTurns, active, events.size());
    }
}
