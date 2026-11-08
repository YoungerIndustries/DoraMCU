package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for jump travel operations.
 */
public class JumpTravelRequest {

    /**
     * Type of jump operation.
     */
    public enum Operation {
        /** Find a route between two worlds. */
        FIND_ROUTE,
        /** Get current jump status. */
        GET_STATUS,
        /** Execute a jump to the next system. */
        EXECUTE_JUMP,
        /** Refuel at current system. */
        REFUEL
    }

    /**
     * The operation to perform.
     */
    @NotNull
    private Operation operation;

    /**
     * Source world ID.
     */
    @Positive
    private int sourceWorldId;

    /**
     * Destination world ID.
     */
    @Positive
    private int destinationWorldId;

    /**
     * Ship design ID (for fuel tank and jump drive capacity).
     */
    @Positive
    private int shipDesignId;

    /**
     * Maximum jump distance in parsecs (0 = use ship's jump drive rating).
     */
    @Positive
    private int maxJumpDistance;

    /**
     * Prefer safe routes (within hyperlanes) over off-chart jumps.
     */
    private boolean preferSafeRoutes = true;

    // ---- Default constructor for Jackson ----

    public JumpTravelRequest() {
    }

    // ---- Accessors ----

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public int getSourceWorldId() {
        return sourceWorldId;
    }

    public void setSourceWorldId(int sourceWorldId) {
        this.sourceWorldId = sourceWorldId;
    }

    public int getDestinationWorldId() {
        return destinationWorldId;
    }

    public void setDestinationWorldId(int destinationWorldId) {
        this.destinationWorldId = destinationWorldId;
    }

    public int getShipDesignId() {
        return shipDesignId;
    }

    public void setShipDesignId(int shipDesignId) {
        this.shipDesignId = shipDesignId;
    }

    public int getMaxJumpDistance() {
        return maxJumpDistance;
    }

    public void setMaxJumpDistance(int maxJumpDistance) {
        this.maxJumpDistance = maxJumpDistance;
    }

    public boolean isPreferSafeRoutes() {
        return preferSafeRoutes;
    }

    public void setPreferSafeRoutes(boolean preferSafeRoutes) {
        this.preferSafeRoutes = preferSafeRoutes;
    }
}
