package com.yisy.modules.doramcu.traveller;

/**
 * High Guard ship-to-ship weapons from Classic Traveller Book 5: High Guard.
 * <p>
 * High Guard expands the Book 2 weapon system with new weapon types,
 * range-dependent modifiers, and advanced weapons like particle beams
 * and meson guns. Each weapon has an optimal range band and varying
 * effectiveness at other ranges.
 * </p>
 * <p>
 * Weapon range modifiers (from Traveller SRD):
 * </p>
 * <table>
 *   <tr><th>Weapon</th><th>Adjacent</th><th>Close</th><th>Short</th><th>Medium</th><th>Long</th><th>Very Long</th><th>Distant</th></tr>
 *   <tr><td>Pulse Laser</td><td>-1</td><td>-1</td><td>+0</td><td>-1</td><td>-2</td><td>-3</td><td>Out</td></tr>
 *   <tr><td>Beam Laser</td><td>-2</td><td>-1</td><td>-1</td><td>+0</td><td>-1</td><td>-1</td><td>-2</td></tr>
 *   <tr><td>Particle Beam</td><td>-3</td><td>-2</td><td>-1</td><td>-1</td><td>+0</td><td>-1</td><td>-1</td></tr>
 *   <tr><td>Fusion Gun</td><td>-2</td><td>-2</td><td>-1</td><td>+0</td><td>-1</td><td>-1</td><td>-2</td></tr>
 *   <tr><td>Meson Gun</td><td>-4</td><td>-3</td><td>-2</td><td>-1</td><td>+0</td><td>-1</td><td>-2</td></tr>
 *   <tr><td>Sandcaster</td><td>-2</td><td>+0</td><td>-2</td><td>Out</td><td>Out</td><td>Out</td><td>Out</td></tr>
 * </table>
 * <p>
 * Missiles have flight time delays:
 * </p>
 * <table>
 *   <tr><th>Range</th><th>Turns to Impact</th></tr>
 *   <tr><td>Short</td><td>1</td></tr>
 *   <tr><td>Medium</td><td>1</td></tr>
 *   <tr><td>Long</td><td>2</td></tr>
 *   <tr><td>Very Long</td><td>5</td></tr>
 *   <tr><td>Distant</td><td>10</td></tr>
 * </table>
 */
public enum HighGuardWeapon {

    // ---- Lasers ----

    /**
     * Pulse Laser — standard ship-mounted laser.
     * <p>
     * Optimal range: Short. Damage: 2D6.
     * Available in single, double, and triple turrets.
     * </p>
     */
    PULSE_LASER("Pulse Laser", WeaponCategory.BEAM, 2, 1,
            -1, -1, 0, -1, -2, -3, Integer.MIN_VALUE,
            "Standard laser — optimal at short range"),

    /**
     * Beam Laser — continuous beam weapon.
     * <p>
     * Optimal range: Medium. Damage: 2D6.
     * More effective at longer ranges than pulse lasers.
     * </p>
     */
    BEAM_LASER("Beam Laser", WeaponCategory.BEAM, 2, 1,
            -2, -1, -1, 0, -1, -1, -2,
            "Continuous beam — optimal at medium range"),

    // ---- Advanced Weapons ----

    /**
     * Particle Beam — advanced directed energy weapon.
     * <p>
     * Optimal range: Long. Damage: 2D6.
     * TL11+ weapon that excels at long range.
     * </p>
     */
    PARTICLE_BEAM("Particle Beam", WeaponCategory.BEAM, 2, 1,
            -3, -2, -1, -1, 0, -1, -1,
            "Advanced energy — optimal at long range, TL11+"),

    /**
     * Fusion Gun — fires superheated fusion pellets.
     * <p>
     * Optimal range: Medium. Damage: 2D6.
     * TL12+ weapon with high armor penetration.
     * </p>
     */
    FUSION_GUN("Fusion Gun", WeaponCategory.BEAM, 2, 1,
            -2, -2, -1, 0, -1, -1, -2,
            "Fusion pellets — high armor penetration, TL12+"),

    /**
     * Meson Gun — penetrates meson screens.
     * <p>
     * Optimal range: Long. Damage: 2D6.
     * TL12+ weapon designed to penetrate nuclear dampers and meson screens.
     * </p>
     */
    MESON_GUN("Meson Gun", WeaponCategory.BEAM, 2, 1,
            -4, -3, -2, -1, 0, -1, -2,
            "Screen penetrator — optimal at long range, TL12+"),

    /**
     * Sandcaster — fires abrasive sand at close range.
     * <p>
     * Optimal range: Close. Damage: 2D6.
     * Blinds sensors and damages hull at very close range.
     * Effective only at Adjacent and Close range.
     * </p>
     */
    SANDCASTER("Sandcaster", WeaponCategory.SPECIAL, 2, 1,
            -2, 0, -2, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE,
            "Abrasive sand — blinds sensors at close range"),

    // ---- Missiles ----

    /**
     * Missile (HE) — high explosive missile.
     * <p>
     * Damage: 1D6 × effect. Flight time varies by range.
     * Standard missile available at TL6+.
     * </p>
     */
    MISSILE_HE("Missile (HE)", WeaponCategory.MISSILE, 1, 1,
            0, 0, 0, 0, 0, 0, 0,
            "High explosive — standard missile, TL6+"),

    /**
     * Missile (Nuclear) — nuclear warhead missile.
     * <p>
     * Damage: 1D1D (1D6 × 1D6). Flight time varies by range.
     * TL8+ weapon of mass destruction.
     * </p>
     */
    MISSILE_NUCLEAR("Missile (Nuclear)", WeaponCategory.MISSILE, 1, 1,
            0, 0, 0, 0, 0, 0, 0,
            "Nuclear warhead — 1D1D damage, TL8+"),

    /**
     * Missile (Smart) — homing missile.
     * <p>
     * Damage: 1D6 × effect. Better hit probability.
     * TL10+ missile with improved guidance.
     * </p>
     */
    MISSILE_SMART("Missile (Smart)", WeaponCategory.MISSILE, 1, 1,
            0, 0, 0, 0, 0, 0, 0,
            "Homing missile — better accuracy, TL10+"),

    /**
     * Missile (Shockwave) — shockwave warhead.
     * <p>
     * Damage: 1D6 × effect. Disables systems without hull damage.
     * TL10+ non-lethal missile.
     * </p>
     */
    MISSILE_SHOCKWAVE("Missile (Shockwave)", WeaponCategory.MISSILE, 1, 1,
            0, 0, 0, 0, 0, 0, 0,
            "Shockwave — disables systems, TL10+"),

    // ---- Torpedoes (Missile Bay weapons) ----

    /**
     * Torpedo (Basic) — heavy missile from missile bay.
     * <p>
     * Damage: 2D6 × effect. Fired from missile bays.
     * Standard torpedo for capital ships.
     * </p>
     */
    TORPEDO_BASIC("Torpedo (Basic)", WeaponCategory.TORPEDO, 2, 1,
            -2, -3, -2, -1, 0, 0, 0,
            "Heavy missile — bay-fired, 2D6 × effect"),

    /**
     * Torpedo (Nuclear) — nuclear warhead torpedo.
     * <p>
     * Damage: 2D1D (2D6 × 1D6). Fired from missile bays.
     * TL8+ capital ship weapon.
     * </p>
     */
    TORPEDO_NUCLEAR("Torpedo (Nuclear)", WeaponCategory.TORPEDO, 2, 1,
            -2, -3, -2, -1, 0, 0, 0,
            "Nuclear torpedo — 2D1D damage, TL8+"),

    /**
     * Ortillery — orbital artillery shell.
     * <p>
     * Damage: 2D6 × effect. Can target planet surface.
     * TL10+ weapon for orbital bombardment.
     * </p>
     */
    ORTILLERY("Ortillery", WeaponCategory.TORPEDO, 2, 1,
            -2, -3, -2, -1, 0, 0, 0,
            "Orbital artillery — can target surface, TL10+");

    private final String name;
    private final WeaponCategory category;
    private final int baseDamageDice;
    private final int damageMultiplier;
    private final int adjacentModifier;
    private final int closeModifier;
    private final int shortModifier;
    private final int mediumModifier;
    private final int longModifier;
    private final int veryLongModifier;
    private final int distantModifier;
    private final String description;

    HighGuardWeapon(String name, WeaponCategory category, int baseDamageDice, int damageMultiplier,
                    int adjacentModifier, int closeModifier, int shortModifier,
                    int mediumModifier, int longModifier, int veryLongModifier,
                    int distantModifier, String description) {
        this.name = name;
        this.category = category;
        this.baseDamageDice = baseDamageDice;
        this.damageMultiplier = damageMultiplier;
        this.adjacentModifier = adjacentModifier;
        this.closeModifier = closeModifier;
        this.shortModifier = shortModifier;
        this.mediumModifier = mediumModifier;
        this.longModifier = longModifier;
        this.veryLongModifier = veryLongModifier;
        this.distantModifier = distantModifier;
        this.description = description;
    }

    /**
     * Returns the display name of this weapon.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the category of this weapon.
     */
    public WeaponCategory getCategory() {
        return category;
    }

    /**
     * Returns the base damage dice (e.g., 2 for 2D6).
     */
    public int getBaseDamageDice() {
        return baseDamageDice;
    }

    /**
     * Returns the damage multiplier for effect-based damage.
     * <p>
     * For missiles, damage = baseDamageDice × effect × this multiplier.
     * For beam weapons, damage = baseDamageDice + effect.
     * </p>
     */
    public int getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Returns the hit modifier for the given range band.
     *
     * @param rangeBand the current range band
     * @return the modifier to add to the Gunnery roll
     */
    public int getHitModifier(HighGuardRangeBand rangeBand) {
        switch (rangeBand) {
            case ADJACENT:
                return adjacentModifier;
            case CLOSE:
                return closeModifier;
            case SHORT:
                return shortModifier;
            case MEDIUM:
                return mediumModifier;
            case LONG:
                return longModifier;
            case VERY_LONG:
                return veryLongModifier;
            case DISTANT:
                return distantModifier;
            default:
                return 0;
        }
    }

    /**
     * Returns the flight time in turns for missiles at the given range.
     *
     * @param rangeBand the current range band
     * @return the number of turns before impact (0 for beam weapons)
     */
    public int getFlightTime(HighGuardRangeBand rangeBand) {
        if (!isMissile()) {
            return 0;
        }
        switch (rangeBand) {
            case ADJACENT:
            case CLOSE:
                return 0;
            case SHORT:
            case MEDIUM:
                return 1;
            case LONG:
                return 2;
            case VERY_LONG:
                return 5;
            case DISTANT:
                return 10;
            default:
                return 0;
        }
    }

    /**
     * Returns whether this weapon is in range at the given range band.
     *
     * @param rangeBand the current range band
     * @return true if the weapon can fire at this range
     */
    public boolean isInRange(HighGuardRangeBand rangeBand) {
        return getHitModifier(rangeBand) != Integer.MIN_VALUE;
    }

    /**
     * Returns whether this is a beam weapon (laser, particle beam, etc.).
     */
    public boolean isBeam() {
        return category == WeaponCategory.BEAM;
    }

    /**
     * Returns whether this is a missile weapon.
     */
    public boolean isMissile() {
        return category == WeaponCategory.MISSILE;
    }

    /**
     * Returns whether this is a torpedo (missile bay weapon).
     */
    public boolean isTorpedo() {
        return category == WeaponCategory.TORPEDO;
    }

    /**
     * Returns whether this is a special weapon (sandcaster, etc.).
     */
    public boolean isSpecial() {
        return category == WeaponCategory.SPECIAL;
    }

    /**
     * Returns whether this is a meson gun.
     * <p>
     * Meson guns can partially penetrate armor (50% bypass).
     * </p>
     */
    public boolean isMeson() {
        return this == MESON_GUN;
    }

    /**
     * Returns the optimal range band for this weapon.
     */
    public HighGuardRangeBand getOptimalRange() {
        int bestModifier = Integer.MIN_VALUE;
        HighGuardRangeBand bestRange = HighGuardRangeBand.SHORT;

        for (HighGuardRangeBand band : HighGuardRangeBand.values()) {
            int modifier = getHitModifier(band);
            if (modifier != Integer.MIN_VALUE && modifier > bestModifier) {
                bestModifier = modifier;
                bestRange = band;
            }
        }
        return bestRange;
    }

    /**
     * Returns the description of this weapon.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the minimum Tech Level required for this weapon.
     */
    public int getMinTechLevel() {
        switch (this) {
            case PULSE_LASER:
            case BEAM_LASER:
            case MISSILE_HE:
                return 6;
            case MISSILE_NUCLEAR:
            case TORPEDO_NUCLEAR:
                return 8;
            case MISSILE_SMART:
            case MISSILE_SHOCKWAVE:
            case ORTILLERY:
                return 10;
            case PARTICLE_BEAM:
                return 11;
            case FUSION_GUN:
            case MESON_GUN:
                return 12;
            case SANDCASTER:
                return 9;
            case TORPEDO_BASIC:
                return 7;
            default:
                return 6;
        }
    }

    /**
     * Returns the tonnage cost for a single turret of this weapon.
     * <p>
     * Tonnage varies by weapon type and turret size.
     * </p>
     *
     * @param turretSize the turret size (SINGLE, DOUBLE, TRIPLE)
     * @return the tons required for the turret
     */
    public int getTurretTonnage(TurretSize turretSize) {
        int baseTons;
        switch (this) {
            case PULSE_LASER:
            case BEAM_LASER:
                baseTons = 100;
                break;
            case PARTICLE_BEAM:
            case FUSION_GUN:
                baseTons = 200;
                break;
            case MESON_GUN:
                baseTons = 300;
                break;
            case SANDCASTER:
                baseTons = 150;
                break;
            default:
                baseTons = 200;
        }
        return baseTons * turretSize.getMultiplier();
    }

    /**
     * Returns the tonnage cost for a missile bay of this weapon.
     *
     * @param baySize the bay size (50 or 100 tons)
     * @return the tons required for the bay
     */
    public int getBayTonnage(BaySize baySize) {
        if (!isMissile() && !isTorpedo()) {
            return 0;
        }
        return baySize.getTons();
    }

    /**
     * Categories of High Guard weapons.
     */
    public enum WeaponCategory {
        /**
         * Beam weapons — lasers, particle beams, fusion guns, meson guns.
         */
        BEAM("Beam Weapon"),

        /**
         * Missile weapons — fired from turrets.
         */
        MISSILE("Missile"),

        /**
         * Torpedo weapons — fired from missile bays.
         */
        TORPEDO("Torpedo"),

        /**
         * Special weapons — sandcasters and other unique weapons.
         */
        SPECIAL("Special");

        private final String name;

        WeaponCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Turret sizes for High Guard weapons.
     */
    public enum TurretSize {
        /**
         * Single turret — one weapon per turret.
         */
        SINGLE("Single", 1),

        /**
         * Double turret — two weapons per turret.
         */
        DOUBLE("Double", 2),

        /**
         * Triple turret — three weapons per turret.
         */
        TRIPLE("Triple", 3);

        private final String name;
        private final int multiplier;

        TurretSize(String name, int multiplier) {
            this.name = name;
            this.multiplier = multiplier;
        }

        public String getName() {
            return name;
        }

        public int getMultiplier() {
            return multiplier;
        }
    }

    /**
     * Missile bay sizes for High Guard torpedoes.
     */
    public enum BaySize {
        /**
         * Small bay — 50 tons.
         */
        SMALL("Small Bay", 50),

        /**
         * Large bay — 100 tons.
         */
        LARGE("Large Bay", 100);

        private final String name;
        private final int tons;

        BaySize(String name, int tons) {
            this.name = name;
            this.tons = tons;
        }

        public String getName() {
            return name;
        }

        public int getTons() {
            return tons;
        }
    }
}
