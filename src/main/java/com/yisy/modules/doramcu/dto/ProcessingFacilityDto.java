package com.yisy.modules.doramcu.dto;

import java.util.Map;

/**
 * Status of a processing facility.
 */
public class ProcessingFacilityDto {

    /** Raw resource type being consumed */
    private String inputResource;

    /** Refined resource type being produced */
    private String outputResource;

    /** Amount of input consumed per turn */
    private int inputPerTurn;

    /** Amount of output produced per turn */
    private int outputPerTurn;

    /** Whether the facility is currently processing */
    private boolean processing;

    /** Turns remaining until next output (0 if producing now) */
    private int turnsRemaining;

    public ProcessingFacilityDto() {
    }

    public String getInputResource() {
        return inputResource;
    }

    public void setInputResource(String inputResource) {
        this.inputResource = inputResource;
    }

    public String getOutputResource() {
        return outputResource;
    }

    public void setOutputResource(String outputResource) {
        this.outputResource = outputResource;
    }

    public int getInputPerTurn() {
        return inputPerTurn;
    }

    public void setInputPerTurn(int inputPerTurn) {
        this.inputPerTurn = inputPerTurn;
    }

    public int getOutputPerTurn() {
        return outputPerTurn;
    }

    public void setOutputPerTurn(int outputPerTurn) {
        this.outputPerTurn = outputPerTurn;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public void setTurnsRemaining(int turnsRemaining) {
        this.turnsRemaining = turnsRemaining;
    }
}
