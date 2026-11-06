package com.yisy.modules.doramcu.dto;

import java.util.Map;

/**
 * Summary of a player's resource pipeline state.
 * <p>
 * Includes raw resources, refined resources, construction modules,
 * mining production rates, and construction queue status.
 * </p>
 */
public class ResourcePipelineSummary {

    /** Raw resource inventory (ORE, CRYSTAL, GAS, DUST, BIO_MASS). */
    private Map<String, Integer> rawResources;

    /** Refined resource inventory (METAL_ALLOY, POWER_CELL, FUEL, COMPOSITE, NUTRIENT). */
    private Map<String, Integer> refinedResources;

    /** Construction module inventory (HULL, WEAPON, SHIELD, ENGINE, FACILITY). */
    private Map<String, Integer> modules;

    /** Mining production rate per turn by raw resource type. */
    private Map<String, Integer> miningRate;

    /** Number of active mining operations. */
    private int miningOperations;

    /** Number of processing facilities currently processing. */
    private int activeProcessing;

    /** Number of module assemblers currently assembling. */
    private int activeAssemblers;

    /** Number of active construction orders in the queue. */
    private int constructionQueueSize;

    public ResourcePipelineSummary() {
    }

    public ResourcePipelineSummary(Map<String, Integer> rawResources,
                                   Map<String, Integer> refinedResources,
                                   Map<String, Integer> modules,
                                   Map<String, Integer> miningRate,
                                   int miningOperations,
                                   int activeProcessing,
                                   int activeAssemblers,
                                   int constructionQueueSize) {
        this.rawResources = rawResources;
        this.refinedResources = refinedResources;
        this.modules = modules;
        this.miningRate = miningRate;
        this.miningOperations = miningOperations;
        this.activeProcessing = activeProcessing;
        this.activeAssemblers = activeAssemblers;
        this.constructionQueueSize = constructionQueueSize;
    }

    public Map<String, Integer> getRawResources() {
        return rawResources;
    }

    public void setRawResources(Map<String, Integer> rawResources) {
        this.rawResources = rawResources;
    }

    public Map<String, Integer> getRefinedResources() {
        return refinedResources;
    }

    public void setRefinedResources(Map<String, Integer> refinedResources) {
        this.refinedResources = refinedResources;
    }

    public Map<String, Integer> getModules() {
        return modules;
    }

    public void setModules(Map<String, Integer> modules) {
        this.modules = modules;
    }

    public Map<String, Integer> getMiningRate() {
        return miningRate;
    }

    public void setMiningRate(Map<String, Integer> miningRate) {
        this.miningRate = miningRate;
    }

    public int getMiningOperations() {
        return miningOperations;
    }

    public void setMiningOperations(int miningOperations) {
        this.miningOperations = miningOperations;
    }

    public int getActiveProcessing() {
        return activeProcessing;
    }

    public void setActiveProcessing(int activeProcessing) {
        this.activeProcessing = activeProcessing;
    }

    public int getActiveAssemblers() {
        return activeAssemblers;
    }

    public void setActiveAssemblers(int activeAssemblers) {
        this.activeAssemblers = activeAssemblers;
    }

    public int getConstructionQueueSize() {
        return constructionQueueSize;
    }

    public void setConstructionQueueSize(int constructionQueueSize) {
        this.constructionQueueSize = constructionQueueSize;
    }
}
