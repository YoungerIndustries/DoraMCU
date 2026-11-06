package com.yisy.modules.doramcu.hex;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents an order to transport raw resources between two locations.
 * <p>
 * Transport orders move resources from a source (e.g., a mining operation's
 * local storage) to a destination (e.g., a processing facility's storage).
 * Transport takes time based on distance, measured in turns.
 * </p>
 */
public class TransportOrder {

    private final int id;
    private final int ownerEmpireId;

    // Source and destination
    private final int sourceBodyId;
    private final int destinationBodyId;

    // Resource being transported
    private final RawResourceType resourceType;
    private final int totalAmount;
    private final int amountPerTurn;

    // Transport state
    private int turnsRemaining;
    private int turnsTotal;
    private int amountLoaded;
    private boolean completed;
    private boolean cancelled;

    /**
     * Creates a new transport order.
     *
     * @param id the unique order ID
     * @param ownerEmpireId the empire ID that owns this order
     * @param sourceBodyId the celestial body ID where resources are loaded
     * @param destinationBodyId the celestial body ID where resources are delivered
     * @param resourceType the raw resource type being transported
     * @param totalAmount the total amount to transport
     * @param distance the distance between source and destination (affects transport time)
     */
    public TransportOrder(int id, int ownerEmpireId, int sourceBodyId, int destinationBodyId,
                           RawResourceType resourceType, int totalAmount, int distance) {
        this.id = id;
        this.ownerEmpireId = ownerEmpireId;
        this.sourceBodyId = sourceBodyId;
        this.destinationBodyId = destinationBodyId;
        this.resourceType = resourceType;
        this.totalAmount = Math.max(1, totalAmount);
        this.amountPerTurn = Math.max(1, totalAmount / Math.max(1, distance));
        this.turnsTotal = Math.max(1, (totalAmount + amountPerTurn - 1) / amountPerTurn);
        this.turnsRemaining = this.turnsTotal;
        this.amountLoaded = 0;
        this.completed = false;
        this.cancelled = false;
    }

    /** @return the unique order ID */
    public int getId() {
        return id;
    }

    /** @return the empire ID that owns this order */
    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /** @return the source celestial body ID */
    public int getSourceBodyId() {
        return sourceBodyId;
    }

    /** @return the destination celestial body ID */
    public int getDestinationBodyId() {
        return destinationBodyId;
    }

    /** @return the raw resource type being transported */
    public RawResourceType getResourceType() {
        return resourceType;
    }

    /** @return the total amount to transport */
    public int getTotalAmount() {
        return totalAmount;
    }

    /** @return the amount transported per turn */
    public int getAmountPerTurn() {
        return amountPerTurn;
    }

    /** @return the total number of turns for this transport */
    public int getTurnsTotal() {
        return turnsTotal;
    }

    /** @return the number of turns remaining */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return the amount already loaded and in transit */
    public int getAmountLoaded() {
        return amountLoaded;
    }

    /** @return true if the transport is complete */
    public boolean isCompleted() {
        return completed;
    }

    /** @return true if the transport is cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    /** @return true if the transport is still in progress */
    public boolean isInProgress() {
        return !completed && !cancelled;
    }

    /**
     * Returns the progress as a fraction between 0.0 and 1.0.
     */
    public double getProgress() {
        if (turnsTotal <= 0) {
            return 1.0;
        }
        return 1.0 - ((double) turnsRemaining / turnsTotal);
    }

    /**
     * Processes one turn of transport.
     *
     * @return the amount delivered this turn (non-zero only on the final turn)
     */
    public int processTurn() {
        if (!isInProgress()) {
            return 0;
        }

        turnsRemaining--;

        if (turnsRemaining <= 0) {
            completed = true;
            amountLoaded = totalAmount;
            return totalAmount;
        }

        // Track partial progress
        amountLoaded = totalAmount - (turnsRemaining * amountPerTurn);
        return 0;
    }

    /**
     * Cancels this transport order.
     */
    public void cancel() {
        if (isInProgress()) {
            cancelled = true;
            turnsRemaining = 0;
        }
    }

    /**
     * Returns a summary of this transport order.
     */
    public String getSummary() {
        String status;
        if (completed) {
            status = "completed";
        } else if (cancelled) {
            status = "cancelled";
        } else {
            status = String.format("%d/%d turns", turnsTotal - turnsRemaining, turnsTotal);
        }
        return String.format("Transport{from=%d, to=%d, resource=%s, amount=%d, %s}",
                sourceBodyId, destinationBodyId, resourceType, totalAmount, status);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
