package com.yisy.modules.doramcu.dto;

import java.util.Map;

/**
 * Detailed information about a construction order.
 * <p>
 * Includes the type of facility being built, module requirements,
 * target location, and progress tracking.
 * </p>
 */
public class ConstructionOrderDto {

    /** Type of construction (SPACE_STATION or PLANET_FACILITY) */
    public enum ConstructionType {
        SPACE_STATION,
        PLANET_FACILITY
    }

    /** Unique order ID */
    private int orderId;

    /** Empire ID that owns this order */
    private int ownerEmpireId;

    /** Type of construction */
    private ConstructionType constructionType;

    /** Station type being built (for SPACE_STATION) */
    private String stationType;

    /** Facility type being built (for PLANET_FACILITY) */
    private String planetFacility;

    /** Planet ID to build on (for PLANET_FACILITY) */
    private Integer targetPlanetId;

    /** Solar system ID to build in (for SPACE_STATION) */
    private Integer targetSystemId;

    /** Module requirements consumed for this order */
    private Map<String, Integer> moduleRequirements;

    /** Total turns required to complete */
    private int totalTurns;

    /** Turns completed so far */
    private int turnsCompleted;

    /** Progress fraction between 0.0 and 1.0 */
    private double progress;

    /** Whether construction is complete */
    private boolean completed;

    /** Whether construction is cancelled */
    private boolean cancelled;

    public ConstructionOrderDto() {
    }

    public ConstructionOrderDto(int orderId, int ownerEmpireId, ConstructionType constructionType,
                                 String stationType, String planetFacility,
                                 Integer targetPlanetId, Integer targetSystemId,
                                 Map<String, Integer> moduleRequirements,
                                 int totalTurns, int turnsCompleted,
                                 double progress, boolean completed, boolean cancelled) {
        this.orderId = orderId;
        this.ownerEmpireId = ownerEmpireId;
        this.constructionType = constructionType;
        this.stationType = stationType;
        this.planetFacility = planetFacility;
        this.targetPlanetId = targetPlanetId;
        this.targetSystemId = targetSystemId;
        this.moduleRequirements = moduleRequirements;
        this.totalTurns = totalTurns;
        this.turnsCompleted = turnsCompleted;
        this.progress = progress;
        this.completed = completed;
        this.cancelled = cancelled;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(int ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public ConstructionType getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(ConstructionType constructionType) {
        this.constructionType = constructionType;
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public String getPlanetFacility() {
        return planetFacility;
    }

    public void setPlanetFacility(String planetFacility) {
        this.planetFacility = planetFacility;
    }

    public Integer getTargetPlanetId() {
        return targetPlanetId;
    }

    public void setTargetPlanetId(Integer targetPlanetId) {
        this.targetPlanetId = targetPlanetId;
    }

    public Integer getTargetSystemId() {
        return targetSystemId;
    }

    public void setTargetSystemId(Integer targetSystemId) {
        this.targetSystemId = targetSystemId;
    }

    public Map<String, Integer> getModuleRequirements() {
        return moduleRequirements;
    }

    public void setModuleRequirements(Map<String, Integer> moduleRequirements) {
        this.moduleRequirements = moduleRequirements;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }

    public int getTurnsCompleted() {
        return turnsCompleted;
    }

    public void setTurnsCompleted(int turnsCompleted) {
        this.turnsCompleted = turnsCompleted;
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
}
