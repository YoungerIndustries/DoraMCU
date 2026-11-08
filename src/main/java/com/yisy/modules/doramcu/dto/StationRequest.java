package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for space station operations.
 */
public class StationRequest {

    /**
     * Type of station operation.
     */
    public enum Operation {
        /** Dock a ship at the station. */
        DOCK,
        /** Undock a ship from the station. */
        UNDOCK,
        /** Refuel a ship at the station. */
        REFUEL,
        /** Repair a ship at the station. */
        REPAIR,
        /** Get station information. */
        GET_INFO
    }

    /**
     * The operation to perform.
     */
    @NotNull
    private Operation operation;

    /**
     * World ID (the station orbits this world).
     */
    @Positive
    private int worldId;

    /**
     * Ship name (for docking/undocking).
     */
    private String shipName;

    /**
     * Ship tonnage (for docking).
     */
    @Positive
    private int shipTonnage;

    /**
     * Ship design ID (for refueling/repair).
     */
    @Positive
    private int shipDesignId;

    /**
     * Whether to fill the fuel tank completely (for refueling).
     */
    private boolean fullTank = true;

    /**
     * Current hull points (for repair).
     */
    @Positive
    private int currentHull;

    /**
     * Maximum hull points (for repair).
     */
    @Positive
    private int maxHull;

    // ---- Default constructor for Jackson ----

    public StationRequest() {
    }

    // ---- Accessors ----

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public int getShipTonnage() {
        return shipTonnage;
    }

    public void setShipTonnage(int shipTonnage) {
        this.shipTonnage = shipTonnage;
    }

    public int getShipDesignId() {
        return shipDesignId;
    }

    public void setShipDesignId(int shipDesignId) {
        this.shipDesignId = shipDesignId;
    }

    public boolean isFullTank() {
        return fullTank;
    }

    public void setFullTank(boolean fullTank) {
        this.fullTank = fullTank;
    }

    public int getCurrentHull() {
        return currentHull;
    }

    public void setCurrentHull(int currentHull) {
        this.currentHull = currentHull;
    }

    public int getMaxHull() {
        return maxHull;
    }

    public void setMaxHull(int maxHull) {
        this.maxHull = maxHull;
    }
}
