package com.yisy.modules.doramcu.dto;

/**
 * Detailed information about a transport order.
 * <p>
 * Includes source and destination, resource type and amount,
 * progress tracking, and status.
 * </p>
 */
public class TransportOrderDto {

    /** Unique order ID */
    private int id;

    /** Empire ID that owns this order */
    private int ownerEmpireId;

    /** Source celestial body ID */
    private int sourceBodyId;

    /** Destination celestial body ID */
    private int destinationBodyId;

    /** Raw resource type being transported */
    private String resourceType;

    /** Total amount to transport */
    private int totalAmount;

    /** Amount transported per turn */
    private int amountPerTurn;

    /** Total turns for this transport */
    private int turnsTotal;

    /** Turns remaining */
    private int turnsRemaining;

    /** Amount already loaded and in transit */
    private int amountLoaded;

    /** Progress fraction between 0.0 and 1.0 */
    private double progress;

    /** Whether the transport is complete */
    private boolean completed;

    /** Whether the transport is cancelled */
    private boolean cancelled;

    /** Whether the transport is still in progress */
    private boolean inProgress;

    public TransportOrderDto() {
    }

    public TransportOrderDto(int id, int ownerEmpireId, int sourceBodyId, int destinationBodyId,
                              String resourceType, int totalAmount, int amountPerTurn,
                              int turnsTotal, int turnsRemaining, int amountLoaded,
                              double progress, boolean completed, boolean cancelled, boolean inProgress) {
        this.id = id;
        this.ownerEmpireId = ownerEmpireId;
        this.sourceBodyId = sourceBodyId;
        this.destinationBodyId = destinationBodyId;
        this.resourceType = resourceType;
        this.totalAmount = totalAmount;
        this.amountPerTurn = amountPerTurn;
        this.turnsTotal = turnsTotal;
        this.turnsRemaining = turnsRemaining;
        this.amountLoaded = amountLoaded;
        this.progress = progress;
        this.completed = completed;
        this.cancelled = cancelled;
        this.inProgress = inProgress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(int ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public int getSourceBodyId() {
        return sourceBodyId;
    }

    public void setSourceBodyId(int sourceBodyId) {
        this.sourceBodyId = sourceBodyId;
    }

    public int getDestinationBodyId() {
        return destinationBodyId;
    }

    public void setDestinationBodyId(int destinationBodyId) {
        this.destinationBodyId = destinationBodyId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getAmountPerTurn() {
        return amountPerTurn;
    }

    public void setAmountPerTurn(int amountPerTurn) {
        this.amountPerTurn = amountPerTurn;
    }

    public int getTurnsTotal() {
        return turnsTotal;
    }

    public void setTurnsTotal(int turnsTotal) {
        this.turnsTotal = turnsTotal;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public void setTurnsRemaining(int turnsRemaining) {
        this.turnsRemaining = turnsRemaining;
    }

    public int getAmountLoaded() {
        return amountLoaded;
    }

    public void setAmountLoaded(int amountLoaded) {
        this.amountLoaded = amountLoaded;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
