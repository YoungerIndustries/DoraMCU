package com.yisy.modules.doramcu.traveller;

import java.time.LocalDateTime;

/**
 * Represents a 4X event that a Traveller character can witness.
 * <p>
 * When a character is traveling in the 4X universe, they may encounter
 * events like battles, fleet movements, and planet conquests. Each event
 * is recorded with its type, location, description, and distance from
 * the character.
 * </p>
 */
public class AdventureEvent {

    /**
     * Type of 4X event.
     */
    public enum EventType {
        /** A battle between fleets. */
        BATTLE,
        /** A fleet moving through a system. */
        FLEET_MOVEMENT,
        /** A planet changing ownership. */
        PLANET_CONQUEST,
        /** A trade deal or economic event. */
        TRADE_DEAL,
        /** A ship being destroyed. */
        SHIP_DESTROYED,
        /** A new system or planet being discovered. */
        DISCOVERY
    }

    private final int eventId;
    private final EventType eventType;
    private final int systemId;
    private final String systemName;
    private final String description;
    private final int distanceHex;
    private final LocalDateTime timestamp;

    /**
     * Creates an adventure event.
     *
     * @param eventId unique event identifier
     * @param eventType the type of event
     * @param systemId the solar system where the event occurred
     * @param systemName the name of the solar system
     * @param description human-readable description of the event
     * @param distanceHex hex distance from the character's position
     */
    public AdventureEvent(int eventId, EventType eventType, int systemId, String systemName,
                          String description, int distanceHex) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.systemId = systemId;
        this.systemName = systemName;
        this.description = description;
        this.distanceHex = distanceHex;
        this.timestamp = LocalDateTime.now();
    }

    /** @return the unique event identifier */
    public int getEventId() {
        return eventId;
    }

    /** @return the type of event */
    public EventType getEventType() {
        return eventType;
    }

    /** @return the solar system ID where the event occurred */
    public int getSystemId() {
        return systemId;
    }

    /** @return the name of the solar system */
    public String getSystemName() {
        return systemName;
    }

    /** @return a human-readable description of the event */
    public String getDescription() {
        return description;
    }

    /** @return the hex distance from the character's position */
    public int getDistanceHex() {
        return distanceHex;
    }

    /** @return when the event occurred */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the ExperienceType that best matches this event type.
     * Used when recording the event as a CharacterExperience.
     *
     * @return the matching ExperienceType
     */
    public ExperienceType toExperienceType() {
        switch (eventType) {
            case BATTLE:
                return ExperienceType.BATTLE;
            case FLEET_MOVEMENT:
                return ExperienceType.EXPLORATION;
            case PLANET_CONQUEST:
                return ExperienceType.SOCIAL;
            case TRADE_DEAL:
                return ExperienceType.TRADE;
            case SHIP_DESTROYED:
                return ExperienceType.LOSS;
            case DISCOVERY:
                return ExperienceType.DISCOVERY;
            default:
                return ExperienceType.OTHER;
        }
    }

    @Override
    public String toString() {
        return "AdventureEvent{" +
                "id=" + eventId +
                ", type=" + eventType +
                ", system=" + systemName +
                ", distance=" + distanceHex +
                ", description='" + description + '\'' +
                '}';
    }
}
