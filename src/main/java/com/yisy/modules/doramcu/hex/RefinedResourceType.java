package com.yisy.modules.doramcu.hex;

/**
 * Represents refined resource types produced by processing raw resources.
 * <p>
 * Refined resources are the intermediate products between raw materials and
 * construction modules. They are produced at processing facilities using
 * recipes that combine one or more raw resource types.
 * </p>
 */
public enum RefinedResourceType {

    /**
     * Metal alloy produced from ore and energy.
     * Primary input for hull and weapon modules.
     */
    METAL_ALLOY("Metal Alloy", "Processed from ore and energy"),

    /**
     * Power cell produced from crystals and ore.
     * Primary input for weapon, shield, and engine modules.
     */
    POWER_CELL("Power Cell", "Processed from crystals and ore"),

    /**
     * Fuel produced from gas and crystals.
     * Primary input for engine modules.
     */
    FUEL("Fuel", "Processed from gas and crystals"),

    /**
     * Composite material produced from dust, ore, and energy.
     * Primary input for hull and shield modules.
     */
    COMPOSITE("Composite", "Processed from dust, ore, and energy"),

    /**
     * Nutrient paste produced from bio-mass and energy.
     * Primary input for facility modules.
     */
    NUTRIENT("Nutrient", "Processed from bio-mass and energy");

    private final String displayName;
    private final String description;

    RefinedResourceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the description of this resource type */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
