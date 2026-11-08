package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for trade operations.
 * <p>
 * Supports buying, selling, and trade mission execution.
 * </p>
 */
public class TradeRequest {

    /**
     * Type of trade operation.
     */
    public enum Operation {
        /** Buy cargo at a world. */
        BUY,
        /** Sell cargo at a world. */
        SELL,
        /** Execute a complete trade mission between two worlds. */
        TRADE_MISSION
    }

    /**
     * The operation to perform.
     */
    @NotNull
    private Operation operation;

    /**
     * Ship design ID (for cargo capacity and trade missions).
     */
    @Positive
    private int shipDesignId;

    /**
     * World ID (for buy/sell operations) or source world (for trade missions).
     */
    @Positive
    private int worldId;

    /**
     * Destination world ID (for trade missions only).
     */
    @Positive
    private int destinationWorldId;

    /**
     * Commodity name (for buy/sell operations).
     */
    private String commodity;

    /**
     * Quantity of units (for buy/sell operations).
     */
    @Positive
    private int quantity = 1;

    // ---- Default constructor for Jackson ----

    public TradeRequest() {
    }

    // ---- Accessors ----

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public int getShipDesignId() {
        return shipDesignId;
    }

    public void setShipDesignId(int shipDesignId) {
        this.shipDesignId = shipDesignId;
    }

    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }

    public int getDestinationWorldId() {
        return destinationWorldId;
    }

    public void setDestinationWorldId(int destinationWorldId) {
        this.destinationWorldId = destinationWorldId;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
