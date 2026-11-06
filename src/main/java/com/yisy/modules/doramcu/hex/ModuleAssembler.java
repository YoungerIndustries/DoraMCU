package com.yisy.modules.doramcu.hex;

/**
 * Assembles modules from refined resources.
 * <p>
 * A module assembler can assemble one module type at a time. When assembly
 * is complete, the module is added to the module storage.
 * </p>
 */
public class ModuleAssembler {

    private final int id;
    private final String name;
    private final int assemblyRate;

    // Default module type for auto-restart when idle
    private final ConstructionModuleType defaultModule;

    // Current assembly state
    private ConstructionModuleType currentModule;
    private int turnsRemaining;

    /**
     * Creates a new module assembler.
     *
     * @param id the unique assembler ID
     * @param name the assembler name
     * @param assemblyRate the assembly rate multiplier (1 = normal, 2 = double speed)
     */
    public ModuleAssembler(int id, String name, int assemblyRate) {
        this.id = id;
        this.name = name;
        this.assemblyRate = Math.max(1, assemblyRate);
        this.defaultModule = null;
        this.currentModule = null;
        this.turnsRemaining = 0;
    }

    /**
     * Creates a new module assembler configured for the given module type.
     * The module type is used as the default for auto-restart when idle.
     *
     * @param moduleType the module type this assembler will produce
     */
    public ModuleAssembler(ConstructionModuleType moduleType) {
        this.id = 0;
        this.name = moduleType.name();
        this.assemblyRate = 1;
        this.defaultModule = moduleType;
        this.currentModule = null;
        this.turnsRemaining = 0;
    }

    /** @return the unique assembler ID */
    public int getId() {
        return id;
    }

    /** @return the assembler name */
    public String getName() {
        return name;
    }

    /** @return the assembly rate multiplier */
    public int getAssemblyRate() {
        return assemblyRate;
    }

    /** @return the current module being assembled, or null if idle */
    public ConstructionModuleType getCurrentModule() {
        return currentModule;
    }

    /** @return the number of turns remaining for the current assembly */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return true if the assembler is currently assembling */
    public boolean isAssembling() {
        return currentModule != null;
    }

    /**
     * Starts assembling the given module type if the assembler is idle and
     * has enough refined resources.
     *
     * @param moduleType the module type to assemble
     * @param refinedStorage the refined resource storage to consume from
     * @return true if assembly started successfully
     */
    public boolean startAssembly(ConstructionModuleType moduleType, ResourceStorage<RefinedResourceType> refinedStorage) {
        if (isAssembling()) {
            return false;
        }
        if (!moduleType.canProduce(refinedStorage.getInventory())) {
            return false;
        }
        // Consume refined resources
        for (var entry : moduleType.getRecipe().entrySet()) {
            refinedStorage.remove(entry.getKey(), entry.getValue());
        }
        currentModule = moduleType;
        turnsRemaining = (moduleType.getAssemblyTimeTurns() + assemblyRate - 1) / assemblyRate;
        return true;
    }

    /**
     * Processes one turn of the current assembly.
     * If idle, automatically starts assembling the default module type if resources are available.
     *
     * @param refinedStorage the refined resource storage (not used during processing)
     * @param moduleStorage the module storage to add completed modules to
     */
    public void processTurn(ResourceStorage<RefinedResourceType> refinedStorage,
                            ResourceStorage<ConstructionModuleType> moduleStorage) {
        if (!isAssembling()) {
            // Auto-restart: if idle and a default module is assigned, try to start it
            if (defaultModule != null && startAssembly(defaultModule, refinedStorage)) {
                // Started assembly; continue below
            } else {
                return;
            }
        }

        turnsRemaining--;

        if (turnsRemaining <= 0) {
            // Assembly complete: add module
            moduleStorage.add(currentModule, 1);
            currentModule = null;
        }
    }

    /**
     * Stops the current assembly (if any).
     */
    public void stopAssembly() {
        currentModule = null;
        turnsRemaining = 0;
    }

    @Override
    public String toString() {
        if (isAssembling()) {
            return String.format("ModuleAssembler{id=%d, name=%s, assembling=%s, turnsLeft=%d}",
                    id, name, currentModule, turnsRemaining);
        }
        return String.format("ModuleAssembler{id=%d, name=%s, idle}", id, name);
    }
}
