package com.yisy.modules.doramcu.hex;

/**
 * Represents the type of weapon system mounted on a ship.
 * <p>
 * Each weapon type requires a specific strategic resource to function,
 * following the Galactic Civilizations IV model where strategic resources
 * gate advanced military capabilities. A ship design that excels in one
 * area may be impossible to build without controlling specific resource nodes.
 * </p>
 */
public enum WeaponType {
    /**
     * Beam weapons — continuous energy damage.
     * Requires Elerium strategic resource.
     * Base damage: 10, range: short.
     */
    BEAM(StrategicResource.ELERIUM, 10, 1),

    /**
     * Missile weapons — high burst damage with projectiles.
     * Requires Anti-Matter strategic resource.
     * Base damage: 15, range: long.
     */
    MISSILE(StrategicResource.ANTI_MATTER, 15, 3),

    /**
     * Mass driver weapons — kinetic impact damage.
     * Requires Thulium strategic resource.
     * Base damage: 12, range: medium.
     */
    MASS_DRIVER(StrategicResource.THULIUM, 12, 2);

    private final StrategicResource requiredResource;
    private final int baseDamage;
    private final int range;

    WeaponType(StrategicResource requiredResource, int baseDamage, int range) {
        this.requiredResource = requiredResource;
        this.baseDamage = baseDamage;
        this.range = range;
    }

    /**
     * Returns the strategic resource required to operate this weapon type.
     *
     * @return the required strategic resource
     */
    public StrategicResource getRequiredResource() {
        return requiredResource;
    }

    /**
     * Returns the base damage value for a single component of this weapon type.
     *
     * @return the base damage per component
     */
    public int getBaseDamage() {
        return baseDamage;
    }

    /**
     * Returns the combat range tier for this weapon type.
     * <p>
     * Range tiers: 1 = short, 2 = medium, 3 = long.
     * </p>
     *
     * @return the range tier (1-3)
     */
    public int getRange() {
        return range;
    }
}
