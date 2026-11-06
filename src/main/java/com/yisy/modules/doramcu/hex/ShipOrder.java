package com.yisy.modules.doramcu.hex;

/**
 * Represents a pending ship construction order in a player's queue.
 * <p>
 * A ship order tracks the ship template, remaining build time, and the fleet
 * that will receive the completed ship.
 * </p>
 */
public class ShipOrder {
    private final Ship shipTemplate;
    private final int buildTime;
    private final int targetFleetId;
    private int turnsRemaining;

    /**
     * Creates a new ship order.
     *
     * @param shipTemplate the ship to build
     * @param buildTime the number of turns to build
     * @param targetFleetId the fleet ID to add the completed ship to
     */
    public ShipOrder(Ship shipTemplate, int buildTime, int targetFleetId) {
        if (shipTemplate == null) {
            throw new IllegalArgumentException("Ship template must not be null");
        }
        if (buildTime <= 0) {
            throw new IllegalArgumentException("Build time must be positive: " + buildTime);
        }
        this.shipTemplate = shipTemplate;
        this.buildTime = buildTime;
        this.targetFleetId = targetFleetId;
        this.turnsRemaining = buildTime;
    }

    /** Returns the ship template for this order. */
    public Ship getShipTemplate() {
        return shipTemplate;
    }

    /** Returns the total build time in turns. */
    public int getBuildTime() {
        return buildTime;
    }

    /** Returns the target fleet ID. */
    public int getTargetFleetId() {
        return targetFleetId;
    }

    /** Returns the remaining turns until completion. */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /**
     * Advances this order by one turn.
     *
     * @return true if the order is now complete
     */
    public boolean advanceTurn() {
        turnsRemaining--;
        return turnsRemaining <= 0;
    }

    /** Returns the mineral cost for this ship. */
    public int getMineralCost() {
        return shipTemplate.getMineralCost();
    }

    /** Returns the energy cost for this ship. */
    public int getEnergyCost() {
        return shipTemplate.getEnergyCost();
    }

    @Override
    public String toString() {
        return String.format("ShipOrder{ship=%s, turnsRemaining=%d/%d}",
                shipTemplate.getName(), turnsRemaining, buildTime);
    }
}
