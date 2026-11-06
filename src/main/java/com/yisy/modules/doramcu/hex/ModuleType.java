package com.yisy.modules.doramcu.hex;

/**
 * Represents ship module types for ship design and starbase configuration.
 * <p>
 * Each module type belongs to a category and has power requirements,
 * cost, and criticality flags used in ship design and construction.
 * </p>
 */
public enum ModuleType {

    // ---- Propulsion ----
    ENGINE("Engine", ModuleCategory.PROPULSION, 10, 500, false),
    THRUSTER("Thruster", ModuleCategory.PROPULSION, 5, 300, false),
    WARP_DRIVE("Warp Drive", ModuleCategory.PROPULSION, 50, 5000, true),

    // ---- Command ----
    BRIDGE("Bridge", ModuleCategory.COMMAND, 15, 1000, true),
    SENSORS("Sensors", ModuleCategory.COMMAND, 8, 600, false),
    COMMS("Comms", ModuleCategory.COMMAND, 5, 400, false),
    AI_CORE_BASIC("AI Core Mk.I", ModuleCategory.COMMAND, 10, 3000, false),
    AI_CORE_ADVANCED("AI Core Mk.II", ModuleCategory.COMMAND, 20, 8000, false),
    AI_CORE_ELITE("AI Core Mk.III", ModuleCategory.COMMAND, 40, 20000, true),

    // ---- Weapons ----
    LASER("Laser", ModuleCategory.WEAPONS, 20, 800, false),
    MISSILE("Missile Launcher", ModuleCategory.WEAPONS, 15, 1200, false),
    PLASMA_CANNON("Plasma Cannon", ModuleCategory.WEAPONS, 30, 2000, false),

    // ---- Defense ----
    SHIELD_GENERATOR("Shield Generator", ModuleCategory.DEFENSE, 25, 1500, true),
    ARMOR("Armor Plating", ModuleCategory.DEFENSE, 0, 700, false),
    POINT_DEFENSE("Point Defense", ModuleCategory.DEFENSE, 10, 900, false),

    // ---- Support ----
    POWER_PLANT("Power Plant", ModuleCategory.SUPPORT, 0, 2000, true),
    HANGAR("Hangar", ModuleCategory.SUPPORT, 5, 3000, false),
    FACTORY("Factory", ModuleCategory.SUPPORT, 10, 2500, false),
    MEDICAL("Medical Bay", ModuleCategory.SUPPORT, 8, 800, false);

    private final String displayName;
    private final ModuleCategory category;
    private final int powerRequirement;
    private final int baseCostPerTon;
    private final boolean critical;

    ModuleType(String displayName, ModuleCategory category, int powerRequirement,
               int baseCostPerTon, boolean critical) {
        this.displayName = displayName;
        this.category = category;
        this.powerRequirement = powerRequirement;
        this.baseCostPerTon = baseCostPerTon;
        this.critical = critical;
    }

    /** Category of this module type. */
    public enum ModuleCategory {
        PROPULSION, COMMAND, WEAPONS, DEFENSE, SUPPORT
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the category of this module type */
    public ModuleCategory getCategory() {
        return category;
    }

    /** @return the power requirement for this module */
    public int getPowerRequirement() {
        return powerRequirement;
    }

    /** @return the base cost per ton for this module */
    public int getBaseCostPerTon() {
        return baseCostPerTon;
    }

    /** @return true if this module is critical (required for ship operation) */
    public boolean isCritical() {
        return critical;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
