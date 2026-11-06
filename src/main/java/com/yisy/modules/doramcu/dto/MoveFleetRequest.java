package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request to move a fleet to a new position.
 * <p>
 * Supports two movement modes:
 * <ul>
 *   <li><b>Within-system movement (MDrive)</b> — Move fleet to a different hex within the same solar system.
 *       Set {@code solarSystemId} to null or omit it. Range is limited by the fleet's effective MDrive speed.</li>
 *   <li><b>Between-system movement (JDrive)</b> — Jump fleet to a different solar system via hyperlane.
 *       Set {@code solarSystemId} to the target system ID. Range is limited by the fleet's effective JDrive speed.</li>
 * </ul>
 * </p>
 */
public class MoveFleetRequest {

    @NotNull(message = "Fleet ID is required")
    @Min(value = 1, message = "Fleet ID must be at least 1")
    private Integer fleetId;
    @NotNull(message = "Target row is required")
    private Integer targetRow;
    @NotNull(message = "Target column is required")
    private Integer targetCol;

    /**
     * Target solar system ID for between-system movement (JDrive).
     * If null, the fleet moves within the current solar system (MDrive).
     */
    private Integer solarSystemId;

    public MoveFleetRequest() {
    }

    public MoveFleetRequest(Integer fleetId, Integer targetRow, Integer targetCol) {
        this.fleetId = fleetId;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public MoveFleetRequest(Integer fleetId, Integer targetRow, Integer targetCol, Integer solarSystemId) {
        this.fleetId = fleetId;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.solarSystemId = solarSystemId;
    }

    public Integer getFleetId() {
        return fleetId;
    }

    public void setFleetId(Integer fleetId) {
        this.fleetId = fleetId;
    }

    public Integer getTargetRow() {
        return targetRow;
    }

    public void setTargetRow(Integer targetRow) {
        this.targetRow = targetRow;
    }

    public Integer getTargetCol() {
        return targetCol;
    }

    public void setTargetCol(Integer targetCol) {
        this.targetCol = targetCol;
    }

    /**
     * Returns the target solar system ID for between-system movement.
     * If null, the fleet moves within the current solar system.
     *
     * @return the target solar system ID, or null for within-system movement
     */
    public Integer getSolarSystemId() {
        return solarSystemId;
    }

    public void setSolarSystemId(Integer solarSystemId) {
        this.solarSystemId = solarSystemId;
    }
}
