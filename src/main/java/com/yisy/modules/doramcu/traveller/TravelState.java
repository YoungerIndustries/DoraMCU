package com.yisy.modules.doramcu.traveller;

/**
 * Travel state enumeration for a ship's jump travel status.
 * <p>
 * In Classic Traveller Book 2, a ship's travel state determines what actions
 * are available. A ship at a system can refuel, repair, or prepare for jump.
 * A ship in transit cannot be intercepted and is consuming fuel.
 * </p>
 */
public enum TravelState {
    /**
     * Ship is at a star system, not in transit.
     */
    AT_SYSTEM,

    /**
     * Ship is preparing for jump (charging jump drive).
     */
    PREPARING_JUMP,

    /**
     * Ship is in jump space, between star systems.
     */
    IN_TRANSIT,

    /**
     * Ship has arrived at a system but needs to cool down.
     */
    COOLING_DOWN,

    /**
     * Ship needs to refuel before continuing.
     */
    NEEDS_REFUEL,

    /**
     * Ship is stranded due to insufficient fuel.
     */
    STRANDED
}
