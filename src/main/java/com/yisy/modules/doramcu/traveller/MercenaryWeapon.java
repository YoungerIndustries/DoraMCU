package com.yisy.modules.doramcu.traveller;

/**
 * Mercenary weapons from Classic Traveller Book 4: Mercenary (1980) Ironmongery section.
 * <p>
 * Covers small arms, support weapons, and high energy weapons available
 * at various tech levels. Each weapon has range modifiers, damage levels,
 * and special rules.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MercenaryWeapon {

    // ---- Slug Throwers ----

    /**
     * Assault Rifle — TL7, 6mm 5g bullet at 900 m/s.
     * Semi-auto or 4-round burst. 30-round magazine.
     */
    ASSAULT_RIFLE("Assault Rifle", 7, WeaponClass.SLUG_THROWER, 300,
            850, 3000, 30, 300,
            "Short: E, Medium: -1, Long: -2, Very Long: -4"),

    /**
     * Advanced Combat Rifle (ACR) — TL10, 9mm HE or 6mm DS at 900/2200 m/s.
     * Electronic battlefield sight, laser rangefinder, gyro-stabilized.
     * Extreme range: 1000m.
     */
    ACR("ACR", 10, WeaponClass.SLUG_THROWER, 1000,
            750, 3500, 20, 1000,
            "Short: +1, Medium: E, Long: -1, Very Long: -3, Extreme: 1000m"),

    /**
     * Gauss Rifle — TL12, 4mm 4g needle at 1500 m/s.
     * 1/4/10 round bursts. 40-round magazine. Extreme range: 1260m.
     */
    GAUSS_RIFLE("Gauss Rifle", 12, WeaponClass.SLUG_THROWER, 1500,
            750, 3500, 40, 1260,
            "Short: +2, Medium: E, Long: -1, Very Long: -3, Extreme: 1260m"),

    /**
     * Snub Pistol — TL8, 10mm 7g at 100-150 m/s.
     * Zero-G weapon. 6-round revolver.
     */
    SNUB_PISTOL("Snub Pistol", 8, WeaponClass.SLUG_THROWER, 150,
            100, 250, 6, 0,
            "Zero-G weapon, Short: E, Medium: -2"),

    /**
     * Accelerator Rifle — TL9, 6mm 5g at 100-150 m/s initial, 700-800 m/s accelerated.
     * Zero-G weapon. 15-round magazine.
     */
    ACCELERATOR_RIFLE("Accelerator Rifle", 9, WeaponClass.SLUG_THROWER, 900,
            800, 2500, 15, 0,
            "Zero-G weapon, Short: +1, Medium: E, Long: -1"),

    // ---- High Energy Weapons ----

    /**
     * PGMP-12 — TL12, man-portable plasma gun.
     * Fires every other round. 40 shots per charge. Extreme range: 750m.
     */
    PGMP_12("PGMP-12", 12, WeaponClass.HIGH_ENERGY, 100000,
            800, 6000, 40, 750,
            "Fires every other round, Short: E, Medium: -1, Long: -3"),

    /**
     * PGMP-13 — TL13, battle dress plasma gun.
     * Requires Battle Dress skill. Fires every round. Extreme range: 1000m.
     */
    PGMP_13("PGMP-13", 13, WeaponClass.HIGH_ENERGY, 65000,
            900, 9000, 999, 1000,
            "Battle dress only, Short: +1, Medium: E, Long: -2"),

    /**
     * PGMP-14 — TL14, gravitic field plasma gun.
     * Can be used without battle dress. Extreme range: 1000m.
     */
    PGMP_14("PGMP-14", 14, WeaponClass.HIGH_ENERGY, 300000,
            800, 9000, 999, 1000,
            "Gravitic field, Short: +1, Medium: E, Long: -2"),

    /**
     * FGMP-14 — TL14, battle dress fusion gun.
     * Requires Battle Dress skill. Extreme range: 1500m.
     */
    FGMP_14("FGMP-14", 14, WeaponClass.HIGH_ENERGY, 200000,
            900, 10000, 999, 1500,
            "Battle dress only, Short: +2, Medium: E, Long: -1"),

    /**
     * FGMP-15 — TL15, gravitic field fusion gun.
     * Most powerful man-portable weapon. Extreme range: 1500m.
     */
    FGMP_15("FGMP-15", 15, WeaponClass.HIGH_ENERGY, 400000,
            800, 10000, 999, 1500,
            "Gravitic field, Short: +2, Medium: E, Long: -1"),

    // ---- Support Weapons ----

    /**
     * Light Assault Gun (LAG) — TL8, 20mm 30g at 400-500 m/s.
     * Single shot. 5-round magazine. Strength 9+ required.
     */
    LAG("LAG", 8, WeaponClass.SUPPORT, 600,
            900, 4000, 5, 0,
            "Strength 9+ required, Short: E, Medium: -1, Long: -3"),

    /**
     * Light Machine Gun (LMG) — TL6, 6mm 5g at 900 m/s.
     * 10-round bursts, up to 5 per round. Belt-fed.
     * Extreme range (tripod): 1000m.
     */
    LMG("LMG", 6, WeaponClass.SUPPORT, 1200,
            1100, 5500, 300, 1000,
            "Burst fire, Short: +1, Medium: E, Long: -2"),

    /**
     * Auto-Cannon — TL9, 300g at 1200 m/s.
     * 10-round bursts. Turreted, towed, or pintle mount.
     * Extreme range: 2000m (pintle: 1000m).
     */
    AUTO_CANNON("Auto-Cannon", 9, WeaponClass.SUPPORT, 12000,
            1500, 400000, 200, 2000,
            "Vehicle mounted, Short: +2, Medium: +1, Long: E"),

    /**
     * Grenade Launcher — TL8, 40mm RAM grenades.
     * Attached to ACR or standalone.
     */
    GRENADE_LAUNCHER("Grenade Launcher", 8, WeaponClass.SUPPORT, 800,
            600, 2000, 6, 0,
            "HE grenades, Short: E, Medium: -2"),

    /**
     * Mortar — TL7, indirect fire weapon.
     * Requires Forward Observer for correction.
     */
    MORTAR("Mortar", 7, WeaponClass.ARTILLERY, 2000,
            0, 10000, 0, 5000,
            "Indirect fire, requires FO"),

    /**
     * Multiple Rocket Launcher (MRL) — TL12.
     * Area bombardment weapon.
     */
    MRL("MRL", 12, WeaponClass.ARTILLERY, 50000,
            0, 500000, 0, 20000,
            "Area bombardment"),

    /**
     * Recoilless Rifle — TL8, anti-armor weapon.
     */
    RECOILLESS_RIFLE("Recoilless Rifle", 8, WeaponClass.ANTI_ARMOR, 3000,
            1200, 8000, 1, 0,
            "Anti-armor, Short: E, Medium: -2, Long: -4"),

    /**
     * AT Missile — TL10, anti-tank missile.
     */
    AT_MISSILE("AT Missile", 10, WeaponClass.ANTI_ARMOR, 5000,
            1000, 5000, 1, 0,
            "Anti-armor missile, Short: +1, Medium: E, Long: -2"),

    /**
     * AA Missile — TL10, anti-aircraft missile.
     */
    AA_MISSILE("AA Missile", 10, WeaponClass.ANTI_AIRCRAFT, 8000,
            800, 4000, 2, 0,
            "Anti-aircraft missile");

    private final String name;
    private final int techLevel;
    private final WeaponClass weaponClass;
    private final int basePrice;
    private final int lengthMm;
    private final int weightGrams;
    private final int ammoCapacity;
    private final int extremeRange;
    private final String rangeNotes;

    MercenaryWeapon(String name, int techLevel, WeaponClass weaponClass,
                    int basePrice, int lengthMm, int weightGrams,
                    int ammoCapacity, int extremeRange, String rangeNotes) {
        this.name = name;
        this.techLevel = techLevel;
        this.weaponClass = weaponClass;
        this.basePrice = basePrice;
        this.lengthMm = lengthMm;
        this.weightGrams = weightGrams;
        this.ammoCapacity = ammoCapacity;
        this.extremeRange = extremeRange;
        this.rangeNotes = rangeNotes;
    }

    public String getName() { return name; }
    public int getTechLevel() { return techLevel; }
    public WeaponClass getWeaponClass() { return weaponClass; }
    public int getBasePrice() { return basePrice; }
    public int getLengthMm() { return lengthMm; }
    public int getWeightGrams() { return weightGrams; }
    public int getAmmoCapacity() { return ammoCapacity; }
    public int getExtremeRange() { return extremeRange; }
    public String getRangeNotes() { return rangeNotes; }

    /**
     * Returns true if this weapon requires Battle Dress skill.
     *
     * @return true if battle dress required
     */
    public boolean requiresBattleDress() {
        return this == PGMP_13 || this == FGMP_14;
    }

    /**
     * Returns true if this weapon is a zero-G weapon.
     *
     * @return true if zero-G weapon
     */
    public boolean isZeroGWeapon() {
        return this == SNUB_PISTOL || this == ACCELERATOR_RIFLE;
    }

    /**
     * Returns the short range modifier.
     *
     * @return the short range DM
     */
    public int getShortRangeDm() {
        switch (this) {
            case ACR: case GAUSS_RIFLE: case PGMP_13: case PGMP_14:
            case FGMP_14: case FGMP_15: case LMG: case AUTO_CANNON: case AT_MISSILE:
                return 1;
            case ASSAULT_RIFLE: case PGMP_12: case LAG: case GRENADE_LAUNCHER:
            case RECOILLESS_RIFLE: case SNUB_PISTOL: case ACCELERATOR_RIFLE:
                return 0;
            default:
                return 0;
        }
    }

    /**
     * Returns the medium range modifier.
     *
     * @return the medium range DM
     */
    public int getMediumRangeDm() {
        return 0; // Most weapons have E at medium
    }

    /**
     * Returns the long range modifier.
     *
     * @return the long range DM
     */
    public int getLongRangeDm() {
        switch (this) {
            case ACR: case GAUSS_RIFLE: case PGMP_13: case PGMP_14:
            case FGMP_14: case FGMP_15: case AT_MISSILE:
                return -1;
            case ASSAULT_RIFLE: case LMG: case ACCELERATOR_RIFLE:
                return -2;
            case PGMP_12: case LAG: case RECOILLESS_RIFLE:
                return -3;
            default:
                return -2;
        }
    }

    /**
     * Classes of mercenary weapons.
     */
    public enum WeaponClass {
        /** Slug-throwing small arms */
        SLUG_THROWER("Slug Thrower"),
        /** High energy weapons (plasma, fusion) */
        HIGH_ENERGY("High Energy"),
        /** Support weapons (LMG, auto-cannon, etc.) */
        SUPPORT("Support"),
        /** Artillery weapons */
        ARTILLERY("Artillery"),
        /** Anti-armor weapons */
        ANTI_ARMOR("Anti-Armor"),
        /** Anti-aircraft weapons */
        ANTI_AIRCRAFT("Anti-Aircraft");

        private final String name;

        WeaponClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
