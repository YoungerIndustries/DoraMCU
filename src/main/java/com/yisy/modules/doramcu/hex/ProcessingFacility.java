package com.yisy.modules.doramcu.hex;

import java.util.Map;

/**
 * Represents a facility that processes raw resources into refined resources.
 * <p>
 * A processing facility can process one recipe at a time. When processing is
 * complete, the refined resource output is added to the refined storage.
 * </p>
 */
public class ProcessingFacility {

    private final int id;
    private final String name;
    private final int processingRate;

    // Default recipe for auto-restart when idle
    private final ProcessingRecipe defaultRecipe;

    // Current processing state
    private ProcessingRecipe currentRecipe;
    private int turnsRemaining;

    /**
     * Creates a new processing facility.
     *
     * @param id the unique facility ID
     * @param name the facility name
     * @param processingRate the processing rate multiplier (1 = normal, 2 = double speed)
     */
    public ProcessingFacility(int id, String name, int processingRate) {
        this.id = id;
        this.name = name;
        this.processingRate = Math.max(1, processingRate);
        this.defaultRecipe = null;
        this.currentRecipe = null;
        this.turnsRemaining = 0;
    }

    /**
     * Creates a new processing facility configured for the given recipe.
     * The recipe is used as the default for auto-restart when idle.
     *
     * @param recipe the recipe this facility will process
     */
    public ProcessingFacility(ProcessingRecipe recipe) {
        this.id = 0;
        this.name = recipe.getOutput().name();
        this.processingRate = 1;
        this.defaultRecipe = recipe;
        this.currentRecipe = recipe;
        this.turnsRemaining = recipe.getProcessingTimeTurns();
    }

    /** @return the unique facility ID */
    public int getId() {
        return id;
    }

    /** @return the facility name */
    public String getName() {
        return name;
    }

    /** @return the processing rate multiplier */
    public int getProcessingRate() {
        return processingRate;
    }

    /** @return the current recipe being processed, or null if idle */
    public ProcessingRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    /** @return the number of turns remaining for the current recipe */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return true if the facility is currently processing */
    public boolean isProcessing() {
        return currentRecipe != null;
    }

    /**
     * Starts processing the given recipe if the facility is idle and has enough raw resources.
     *
     * @param recipe the recipe to process
     * @param rawStorage the raw resource storage to consume from
     * @return true if processing started successfully
     */
    public boolean startProcessing(ProcessingRecipe recipe, ResourceStorage<RawResourceType> rawStorage) {
        if (isProcessing()) {
            return false;
        }
        if (!recipe.canStart(rawStorage.getInventory())) {
            return false;
        }
        // Consume inputs
        for (Map.Entry<RawResourceType, Integer> entry : recipe.getInputs().entrySet()) {
            rawStorage.remove(entry.getKey(), entry.getValue());
        }
        currentRecipe = recipe;
        turnsRemaining = (recipe.getProcessingTimeTurns() + processingRate - 1) / processingRate;
        return true;
    }

    /**
     * Processes one turn of the current recipe.
     * If idle, automatically starts processing the assigned recipe if resources are available.
     *
     * @param rawStorage the raw resource storage (used for starting new recipes)
     * @param refinedStorage the refined resource storage to add output to
     */
    public void processTurn(ResourceStorage<RawResourceType> rawStorage,
                            ResourceStorage<RefinedResourceType> refinedStorage) {
        if (!isProcessing()) {
            // Auto-restart: if idle and a default recipe is assigned, try to start it
            if (defaultRecipe != null && startProcessing(defaultRecipe, rawStorage)) {
                // Started processing; continue below
            } else {
                return;
            }
        }

        turnsRemaining--;

        if (turnsRemaining <= 0) {
            // Processing complete: add output
            refinedStorage.add(currentRecipe.getOutput(), currentRecipe.getOutputAmount());
            currentRecipe = null;
        }
    }

    /**
     * Stops the current processing (if any).
     */
    public void stopProcessing() {
        currentRecipe = null;
        turnsRemaining = 0;
    }

    @Override
    public String toString() {
        if (isProcessing()) {
            return String.format("ProcessingFacility{id=%d, name=%s, processing=%s, turnsLeft=%d}",
                    id, name, currentRecipe.getOutput(), turnsRemaining);
        }
        return String.format("ProcessingFacility{id=%d, name=%s, idle}", id, name);
    }
}
