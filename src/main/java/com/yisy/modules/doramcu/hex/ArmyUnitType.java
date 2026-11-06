package com.yisy.modules.doramcu.hex;

/**
 * Types of ground army units in the 4X empire game.
 * <p>
 * Army units operate on planetary surfaces for invasion, defense, and occupation.
 * Unlike space fleets, army units cannot move between systems without naval transport.
 * </p>
 * <p>
 * Based on 4X game design patterns from Stellaris (army types), Stellaris:
 * Galactic Civilizations IV (ground forces), and Endless Space 2.
 * </p>
 */
public enum ArmyUnitType {

    /**
     * Basic infantry unit. Cheap, versatile, forms the backbone of ground forces.
     * Effective in urban and jungle terrain.
     */
    INFANTRY("Infantry", 10, 5, 3, 1, 1, "Basic ground combat unit"),

    /**
     * Mechanized infantry with armored vehicles. Balanced offense and defense.
     * Effective in open terrain and deserts.
     */
    MECHANIZED("Mechanized Infantry", 25, 15, 10, 2, 2, "Armored mobile infantry"),

    /**
     * Heavy tank division. High offense and defense, slow movement.
     * Devastating in open terrain, weak in mountains.
     */
    TANK_DIVISION("Tank Division", 50, 35, 25, 3, 1, "Heavy armored assault unit"),

    /**
     * Artillery battery. Long-range bombardment support.
     * Cannot move while firing, provides area damage.
     */
    ARTILLERY("Artillery Battery", 30, 40, 8, 1, 2, "Long-range bombardment support"),

    /**
     * Missile battery. Strategic strike capability.
     * Can target orbital assets and enemy fortifications.
     */
    MISSILE_BATTERY("Missile Battery", 60, 50, 12, 1, 3, "Strategic missile strike unit"),

    /**
     * Air support wing. Close air support and reconnaissance.
     * Provides mobility and strike capability to adjacent units.
     */
    AIR_SUPPORT("Air Support Wing", 45, 30, 20, 4, 2, "Close air support and recon"),

    /**
     * Engineer corps. Specialized in fortification construction and repair.
     * Can build defenses and repair damaged structures.
     */
    ENGINEER("Engineer Corps", 15, 8, 6, 2, 1, "Fortification and repair specialists"),

    /**
     * Special forces. Elite units for sabotage and precision strikes.
     * High cost but exceptional combat effectiveness.
     */
    SPECIAL_FORCES("Special Forces", 70, 55, 30, 3, 3, "Elite precision strike unit"),

    /**
     * Naval marine unit. Amphibious assault capability.
     * Can deploy from naval vessels and defend coastal positions.
     */
    MARINES("Naval Marines", 35, 25, 15, 2, 2, "Amphibious assault troops");

    private final String displayName;
    private final int buildCost;
    private final int offense;
    private final int defense;
    private final int mobility;
    private final int supplyCost;
    private final String description;

    ArmyUnitType(String displayName, int buildCost, int offense, int defense,
                 int mobility, int supplyCost, String description) {
        this.displayName = displayName;
        this.buildCost = buildCost;
        this.offense = offense;
        this.defense = defense;
        this.mobility = mobility;
        this.supplyCost = supplyCost;
        this.description = description;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the credit cost to build one unit. */
    public int getBuildCost() {
        return buildCost;
    }

    /** Returns the base offense stat. */
    public int getOffense() {
        return offense;
    }

    /** Returns the base defense stat. */
    public int getDefense() {
        return defense;
    }

    /** Returns the mobility rating (movement range). */
    public int getMobility() {
        return mobility;
    }

    /** Returns the per-turn supply cost in credits. */
    public int getSupplyCost() {
        return supplyCost;
    }

    /** Returns the description. */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
