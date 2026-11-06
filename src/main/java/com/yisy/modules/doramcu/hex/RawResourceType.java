package com.yisy.modules.doramcu.hex;

/**
 * Represents raw resource types extracted directly from celestial bodies.
 * <p>
 * Raw resources are the primary output of mining operations and must be
 * processed into refined resources before they can be used for construction.
 * </p>
 */
public enum RawResourceType {

    /**
     * Basic metal ore mined from planets and asteroids.
     * Primary input for metal alloy production.
     */
    ORE("Ore", "Basic metal ore from planets and asteroids"),

    /**
     * Energy crystals harvested from comets and certain planets.
     * Primary input for power cell and fuel production.
     */
    CRYSTAL("Crystal", "Energy crystals from comets and certain planets"),

    /**
     * Volatile gases collected from gas giants.
     * Primary input for fuel production.
     */
    GAS("Gas", "Volatile gases from gas giants"),

    /**
     * Cosmic dust gathered from nebulae and asteroid belts.
     * Primary input for composite material production.
     */
    DUST("Dust", "Cosmic dust from nebulae and asteroid belts"),

    /**
     * Organic material harvested from habitable planets.
     * Primary input for nutrient production.
     */
    BIO_MASS("Bio-Mass", "Organic material from habitable planets");

    private final String displayName;
    private final String description;

    RawResourceType(String displayName, String description) {
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
