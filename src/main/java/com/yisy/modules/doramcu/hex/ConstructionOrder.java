package com.yisy.modules.doramcu.hex;

import java.util.Map;

/**
 * Represents a queued construction order that progresses over multiple turns.
 * <p>
 * Construction orders are created when a player starts building a facility
 * (space station or planetary upgrade). The order tracks:
 * <ul>
 *   <li>The type of facility being built</li>
 *   <li>Module requirements (consumed upfront)</li>
 *   <li>Construction time in turns</li>
 *   <li>Current progress (turns completed)</li>
 * </ul>
 * <p>
 * When progress reaches the required turns, the order is complete and the
 * facility is created by the caller.
 * </p>
 */
public class ConstructionOrder {

    /**
     * Type of construction being queued.
     */
    public enum ConstructionType {
        SPACE_STATION,
        PLANET_FACILITY
    }

    private final int orderId;
    private final int ownerEmpireId;
    private final ConstructionType constructionType;

    /** For SPACE_STATION: the station type being built */
    private final SpaceStationType stationType;

    /** For PLANET_FACILITY: the facility type being built */
    private final StartingFacility planetFacility;

    /** The planet to build on (for PLANET_FACILITY only) */
    private final Integer targetPlanetId;

    /** The solar system entity to build in (for SPACE_STATION only) */
    private final SolarSystemEntity targetSystem;

    /** Module requirements (consumed when order is created) */
    private final Map<ConstructionModuleType, Integer> moduleRequirements;

    /** Total turns required to complete construction */
    private final int totalTurns;

    /** Turns completed so far */
    private int turnsCompleted;

    /** Whether this order has been completed */
    private boolean completed;

    /** Whether this order has been cancelled */
    private boolean cancelled;

    /**
     * Creates a new construction order for a space station.
     *
     * @param orderId the unique order ID
     * @param ownerEmpireId the empire ordering construction
     * @param stationType the type of station to build
     * @param targetSystem the solar system entity where the station will be placed
     * @param moduleRequirements the modules consumed for this order
     * @param totalTurns the number of turns to complete construction
     */
    public ConstructionOrder(int orderId, int ownerEmpireId, SpaceStationType stationType,
                              SolarSystemEntity targetSystem,
                              Map<ConstructionModuleType, Integer> moduleRequirements,
                              int totalTurns) {
        this.orderId = orderId;
        this.ownerEmpireId = ownerEmpireId;
        this.constructionType = ConstructionType.SPACE_STATION;
        this.stationType = stationType;
        this.planetFacility = null;
        this.targetPlanetId = null;
        this.targetSystem = targetSystem;
        this.moduleRequirements = moduleRequirements;
        this.totalTurns = totalTurns;
        this.turnsCompleted = 0;
        this.completed = false;
        this.cancelled = false;
    }

    /**
     * Creates a new construction order for a planetary facility.
     *
     * @param orderId the unique order ID
     * @param ownerEmpireId the empire ordering construction
     * @param facility the facility type to build
     * @param targetPlanetId the planet to build on
     * @param moduleRequirements the modules consumed for this order
     * @param totalTurns the number of turns to complete construction
     */
    public ConstructionOrder(int orderId, int ownerEmpireId, StartingFacility facility,
                              Integer targetPlanetId,
                              Map<ConstructionModuleType, Integer> moduleRequirements,
                              int totalTurns) {
        this.orderId = orderId;
        this.ownerEmpireId = ownerEmpireId;
        this.constructionType = ConstructionType.PLANET_FACILITY;
        this.stationType = null;
        this.planetFacility = facility;
        this.targetPlanetId = targetPlanetId;
        this.targetSystem = null;
        this.moduleRequirements = moduleRequirements;
        this.totalTurns = totalTurns;
        this.turnsCompleted = 0;
        this.completed = false;
        this.cancelled = false;
    }

    /** @return the unique order ID */
    public int getOrderId() {
        return orderId;
    }

    /** @return the empire that owns this order */
    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /** @return the type of construction */
    public ConstructionType getConstructionType() {
        return constructionType;
    }

    /** @return the station type (for SPACE_STATION orders) */
    public SpaceStationType getStationType() {
        return stationType;
    }

    /** @return the facility type (for PLANET_FACILITY orders) */
    public StartingFacility getPlanetFacility() {
        return planetFacility;
    }

    /** @return the target planet ID (for PLANET_FACILITY orders) */
    public Integer getTargetPlanetId() {
        return targetPlanetId;
    }

    /** @return the target solar system entity (for SPACE_STATION orders) */
    public SolarSystemEntity getTargetSystem() {
        return targetSystem;
    }

    /** @return the module requirements consumed for this order */
    public Map<ConstructionModuleType, Integer> getModuleRequirements() {
        return moduleRequirements;
    }

    /** @return the total turns required to complete */
    public int getTotalTurns() {
        return totalTurns;
    }

    /** @return the number of turns completed so far */
    public int getTurnsCompleted() {
        return turnsCompleted;
    }

    /**
     * @return the progress as a fraction between 0.0 and 1.0
     */
    public double getProgress() {
        if (totalTurns == 0) {
            return 1.0;
        }
        return (double) turnsCompleted / totalTurns;
    }

    /** @return true if construction is complete */
    public boolean isCompleted() {
        return completed;
    }

    /** @return true if this order was cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Advances the construction by one turn.
     * <p>
     * When the order reaches the required turns, it is marked as complete.
     * </p>
     */
    public void processTurn() {
        if (completed || cancelled) {
            return;
        }
        turnsCompleted++;
        if (turnsCompleted >= totalTurns) {
            completed = true;
        }
    }

    /**
     * Cancels this construction order.
     * <p>
     * Modules are NOT refunded when an order is cancelled.
     * </p>
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Returns a human-readable description of this order.
     */
    @Override
    public String toString() {
        String type;
        if (constructionType == ConstructionType.SPACE_STATION) {
            type = "Station(" + stationType + ")";
        } else {
            type = "Facility(" + planetFacility + ")";
        }
        return String.format("ConstructionOrder{id=%d, empire=%d, type=%s, progress=%d/%d, completed=%s}",
                orderId, ownerEmpireId, type, turnsCompleted, totalTurns, completed);
    }
}
