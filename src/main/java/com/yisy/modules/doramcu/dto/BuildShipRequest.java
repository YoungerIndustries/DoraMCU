package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BuildShipRequest {

    @NotBlank(message = "Ship type is required")
    private String shipType;
    @Min(value = 1, message = "Target fleet ID must be at least 1")
    private Integer targetFleetId;

    /** Hull shape for the ship's hull module (e.g., "CUBE", "WEDGE", "DOME", "PYRAMID", "CONE"). Default is CUBE. */
    private String hullShape;

    /** Exterior material value for hull cells (0-5). Default is 1 (Aluminum). */
    private Integer exteriorMaterial;

    /** Interior material value for hull cells (0-5). Default is 0 (Empty). */
    private Integer interiorMaterial;

    public BuildShipRequest() {
    }

    public BuildShipRequest(String shipType, Integer targetFleetId) {
        this.shipType = shipType;
        this.targetFleetId = targetFleetId;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public Integer getTargetFleetId() {
        return targetFleetId;
    }

    public void setTargetFleetId(Integer targetFleetId) {
        this.targetFleetId = targetFleetId;
    }

    public String getHullShape() {
        return hullShape;
    }

    public void setHullShape(String hullShape) {
        this.hullShape = hullShape;
    }

    public Integer getExteriorMaterial() {
        return exteriorMaterial;
    }

    public void setExteriorMaterial(Integer exteriorMaterial) {
        this.exteriorMaterial = exteriorMaterial;
    }

    public Integer getInteriorMaterial() {
        return interiorMaterial;
    }

    public void setInteriorMaterial(Integer interiorMaterial) {
        this.interiorMaterial = interiorMaterial;
    }
}
