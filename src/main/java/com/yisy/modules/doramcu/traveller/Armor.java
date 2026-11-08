package com.yisy.modules.doramcu.traveller;

/**
 * Armor types from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Armor reduces damage from melee attacks. Each armor type has a protection
 * level that is subtracted from the damage roll. Different armor types offer
 * different levels of protection and may impose movement or other penalties.
 * </p>
 * <p>
 * The protection value is subtracted from the damage after a successful hit.
 * If the protection equals or exceeds the damage, no damage is taken.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public enum Armor {

    /**
     * No armor — civilian clothing.
     * <p>
     * Provides no protection against melee attacks.
     * </p>
     */
    NONE("No Armor", 0, 0, 0,
            "Civilian clothing — no protection"),

    /**
     * Leather armor — basic protection.
     * <p>
     * Light armor made of treated leather. Provides minimal protection
     * but does not significantly impede movement.
     * </p>
     */
    LEATHER("Leather Armor", 1, 1, 1,
            "Treated leather — minimal protection"),

    /**
     * Padded armor — quilted fabric protection.
     * <p>
     * Thick quilted fabric that absorbs some impact.
     * Better than leather against blunt weapons.
     * </p>
     */
    PADDED("Padded Armor", 1, 1, 2,
            "Quilted fabric — better against blunt weapons"),

    /**
     * Chain mail — interlocking metal rings.
     * <p>
     * Traditional armor that provides good protection against
     * cutting attacks but less against thrusting.
     * </p>
     */
    CHAIN_MAIL("Chain Mail", 2, 1, 3,
            "Interlocking rings — good against cutting"),

    /**
     * Battle dress — military combat armor.
     * <p>
     * Standard military armor with ceramic or composite plates.
     * Good all-around protection.
     * </p>
     */
    BATTLE_DRESS("Battle Dress", 2, 2, 3,
            "Military composite — good all-around"),

    /**
     * Flak armor — explosive fragmentation protection.
     * <p>
     * Designed to protect against shrapnel and small arms.
     * Provides solid melee protection as a side effect.
     * </p>
     */
    FLAK("Flak Armor", 3, 2, 3,
            "Anti-fragmentation — solid melee protection"),

    /**
     * Powered armor — exoskeleton combat suit.
     * <p>
     * Full-body powered armor with integrated weapons systems.
     * Maximum melee protection but expensive and heavy.
     * Grants +1 to Endurance for damage purposes.
     * </p>
     */
    POWERED("Powered Armor", 4, 4, 4,
            "Exoskeleton suit — maximum protection, +1 End for damage");

    private final String name;
    private final int cutProtection;
    private final int thrustProtection;
    private final int impactProtection;
    private final String description;

    Armor(String name, int cutProtection, int thrustProtection,
          int impactProtection, String description) {
        this.name = name;
        this.cutProtection = cutProtection;
        this.thrustProtection = thrustProtection;
        this.impactProtection = impactProtection;
        this.description = description;
    }

    /**
     * Returns the display name of the armor.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the protection value against cut attacks.
     * <p>
     * This value is subtracted from cut damage after a hit.
     * </p>
     *
     * @return the cut protection value
     */
    public int getCutProtection() {
        return cutProtection;
    }

    /**
     * Returns the protection value against thrust attacks.
     * <p>
     * This value is subtracted from thrust damage after a hit.
     * </p>
     *
     * @return the thrust protection value
     */
    public int getThrustProtection() {
        return thrustProtection;
    }

    /**
     * Returns the protection value against impact attacks.
     * <p>
     * This value is subtracted from impact damage after a hit.
     * </p>
     *
     * @return the impact protection value
     */
    public int getImpactProtection() {
        return impactProtection;
    }

    /**
     * Returns the description of the armor.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the protection value for a given attack type.
     *
     * @param attackType the type of attack (CUT, THRUST, or IMPACT)
     * @return the protection value
     */
    public int getProtection(AttackType attackType) {
        switch (attackType) {
            case CUT:
                return cutProtection;
            case THRUST:
                return thrustProtection;
            case IMPACT:
                return impactProtection;
            default:
                return 0;
        }
    }

    /**
     * Returns the maximum protection value across all attack types.
     *
     * @return the max protection
     */
    public int getMaxProtection() {
        return Math.max(cutProtection, Math.max(thrustProtection, impactProtection));
    }

    /**
     * Checks if this armor provides any protection.
     *
     * @return true if any protection value > 0
     */
    public boolean providesProtection() {
        return cutProtection > 0 || thrustProtection > 0 || impactProtection > 0;
    }

    /**
     * Checks if this is powered armor.
     * <p>
     * Powered armor grants +1 to Endurance for damage purposes.
     * </p>
     *
     * @return true if this is powered armor
     */
    public boolean isPowered() {
        return this == POWERED;
    }

    /**
     * Types of melee attacks for armor protection lookup.
     */
    public enum AttackType {
        /**
         * Cutting/slashing attack.
         */
        CUT,

        /**
         * Thrusting/piercing attack.
         */
        THRUST,

        /**
         * Impact/blunt attack.
         */
        IMPACT
    }
}
