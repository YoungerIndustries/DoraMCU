package com.yisy.modules.doramcu.traveller;

/**
 * Firearms from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Each firearm has the following statistics:
 * </p>
 * <ul>
 *   <li><b>Damage</b> — damage code (e.g., "2D6+2") applied when hit</li>
 *   <li><b>Range</b> — effective range in yards (short/medium/long range brackets)</li>
 *   <li><b>Armor Penetration</b> — armor class penetrated (0-9, where 9 is maximum)</li>
 *   <li><b>Rate of Fire</b> — number of shots per combat round</li>
 *   <li><b>Weight</b> — weapon weight in kg</li>
 * </ul>
 * <p>
 * Firearms use range brackets for hit probability:
 * </p>
 * <ul>
 *   <li><b>Short Range</b> — +2 to hit</li>
 *   <li><b>Medium Range</b> — no modifier</li>
 *   <li><b>Long Range</b> — -2 to hit</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public enum FirearmWeapon {

    // ---- Pistols ----

    /**
     * .22 Pistol — small caliber pistol.
     * <p>
     * Low damage, low recoil. Good for training or close-range defense.
     * </p>
     */
    PISTOL_22(".22 Pistol", FirearmType.PISTOL, "1D6", 50, 200, 400, 0, 1, 1.5,
            "Small caliber — low damage, low recoil"),

    /**
     * .356 Mag — medium caliber magnum pistol.
     * <p>
     * Standard sidearm with good stopping power.
     * </p>
     */
    PISTOL_356_MAG(".356 Mag", FirearmType.PISTOL, "2D6", 80, 300, 600, 2, 1, 2.0,
            "Standard sidearm — good stopping power"),

    /**
     * .479 Big Bores — large caliber pistol.
     * <p>
     * High damage pistol with significant recoil.
     * </p>
     */
    PISTOL_479_BIG_BORES(".479 Big Bores", FirearmType.PISTOL, "3D6", 100, 350, 700, 3, 1, 2.5,
            "Large caliber — high damage, significant recoil"),

    /**
     * Laser Pistol — energy sidearm.
     * <p>
     * Standard energy weapon. Good armor penetration, no ammunition needed.
     * </p>
     */
    LASER_PISTOL("Laser Pistol", FirearmType.PISTOL, "2D6+2", 100, 400, 800, 4, 1, 3.0,
            "Energy sidearm — good armor penetration"),

    // ---- Rifles ----

    /**
     * .22 Rifle — small caliber rifle.
     * <p>
     * Low damage but accurate at long range. Good for hunting small game.
     * </p>
     */
    RIFLE_22(".22 Rifle", FirearmType.RIFLE, "1D6", 80, 300, 600, 0, 1, 3.0,
            "Small caliber rifle — accurate at long range"),

    /**
     * .356 Mag Rifle — magnum rifle.
     * <p>
     * Powerful rifle with good range and damage.
     * </p>
     */
    RIFLE_356_MAG(".356 Mag Rifle", FirearmType.RIFLE, "2D6", 100, 400, 800, 2, 1, 4.0,
            "Magnum rifle — powerful and accurate"),

    /**
     * .479 Big Bores Rifle — large caliber rifle.
     * <p>
     * High damage rifle for hunting large game or anti-personnel use.
     * </p>
     */
    RIFLE_479_BIG_BORES(".479 Big Bores Rifle", FirearmType.RIFLE, "3D6", 120, 500, 1000, 3, 1, 5.0,
            "Large caliber — high damage, anti-personnel"),

    /**
     * Laser Rifle — energy rifle.
     * <p>
     * Standard military energy weapon. Excellent armor penetration.
     * </p>
     */
    LASER_RIFLE("Laser Rifle", FirearmType.RIFLE, "3D6+3", 150, 600, 1200, 5, 1, 5.5,
            "Military energy rifle — excellent armor penetration"),

    /**
     * Repeating Rifle — magazine-fed rifle.
     * <p>
     * High rate of fire rifle. Can fire 2 shots per round.
     * </p>
     */
    REPEATING_RIFLE("Repeating Rifle", FirearmType.RIFLE, "2D6", 100, 400, 800, 2, 2, 4.5,
            "Magazine-fed — high rate of fire (2 shots/round)"),

    // ---- Submachine Guns ----

    /**
     * .356 Mag SMG — magnum submachine gun.
     * <p>
     * Automatic version of the .356 Mag. High rate of fire.
     * </p>
     */
    SMG_356_MAG(".356 Mag SMG", FirearmType.SUBMACHINE_GUN, "2D6", 60, 200, 400, 2, 2, 3.5,
            "Automatic magnum — high rate of fire"),

    /**
     * .479 Big Bores SMG — large caliber submachine gun.
     * <p>
     * Heavy automatic weapon with high damage per burst.
     * </p>
     */
    SMG_479_BIG_BORES(".479 Big Bores SMG", FirearmType.SUBMACHINE_GUN, "3D6", 80, 250, 500, 3, 2, 4.5,
            "Heavy automatic — high damage per burst"),

    // ---- Machine Guns ----

    /**
     * .356 Mag MG — magnum machine gun.
     * <p>
     * Sustained fire weapon. Can fire 3 shots per round.
     * </p>
     */
    MG_356_MAG(".356 Mag MG", FirearmType.MACHINE_GUN, "2D6", 100, 400, 800, 2, 3, 8.0,
            "Sustained fire — 3 shots per round"),

    /**
     * .479 Big Bores MG — large caliber machine gun.
     * <p>
     * Heavy machine gun for suppression and area denial.
     * </p>
     */
    MG_479_BIG_BORES(".479 Big Bores MG", FirearmType.MACHINE_GUN, "3D6", 120, 500, 1000, 3, 3, 10.0,
            "Heavy machine gun — suppression and area denial"),

    // ---- Shotguns ----

    /**
     * Shotgun — smoothbore shotgun.
     * <p>
     * Devastating at close range. Fires multiple pellets.
     * Limited effective range.
     * </p>
     */
    SHOTGUN("Shotgun", FirearmType.SHOTGUN, "4D6", 30, 100, 200, 1, 1, 3.5,
            "Smoothbore — devastating at close range"),

    // ---- Heavy Weapons ----

    /**
     * Laser Cannon — heavy energy weapon.
     * <p>
     * Mounted or two-handed energy weapon. Massive damage and armor penetration.
     * </p>
     */
    LASER_CANNON("Laser Cannon", FirearmType.HEAVY, "4D6+4", 200, 800, 1600, 6, 1, 15.0,
            "Heavy energy weapon — massive damage"),

    /**
     * Missile Launcher — portable missile system.
     * <p>
     * Anti-vehicle weapon. Extremely high damage but slow rate of fire.
     * Each missile has high armor penetration.
     * </p>
     */
    MISSILE_LAUNCHER("Missile Launcher", FirearmType.HEAVY, "6D6", 300, 1000, 2000, 8, 1, 20.0,
            "Anti-vehicle — extremely high damage"),

    // ---- Exotic ----

    /**
     * Plasma Gun — experimental energy weapon.
     * <p>
     * Unstable but devastating. Risk of misfire on roll of 12.
     * </p>
     */
    PLASMA_GUN("Plasma Gun", FirearmType.EXOTIC, "4D6+2", 150, 600, 1200, 7, 1, 6.0,
            "Experimental — misfire on roll of 12"),

    /**
     * Particle Beam — advanced directed energy weapon.
     * <p>
     * Military-grade weapon with maximum armor penetration.
     * </p>
     */
    PARTICLE_BEAM("Particle Beam", FirearmType.EXOTIC, "3D6+4", 200, 800, 1600, 9, 1, 8.0,
            "Military-grade — maximum armor penetration");

    private final String name;
    private final FirearmType type;
    private final String damageCode;
    private final int shortRange;
    private final int mediumRange;
    private final int longRange;
    private final int armorPenetration;
    private final int rateOfFire;
    private final double weight;
    private final String description;

    FirearmWeapon(String name, FirearmType type, String damageCode,
                  int shortRange, int mediumRange, int longRange,
                  int armorPenetration, int rateOfFire, double weight,
                  String description) {
        this.name = name;
        this.type = type;
        this.damageCode = damageCode;
        this.shortRange = shortRange;
        this.mediumRange = mediumRange;
        this.longRange = longRange;
        this.armorPenetration = armorPenetration;
        this.rateOfFire = rateOfFire;
        this.weight = weight;
        this.description = description;
    }

    /**
     * Returns the display name of the firearm.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the firearm.
     */
    public FirearmType getType() {
        return type;
    }

    /**
     * Returns the damage code string (e.g., "2D6+2").
     */
    public String getDamageCode() {
        return damageCode;
    }

    /**
     * Returns the short range limit in yards.
     * <p>
     * Within short range, the shooter gets +2 to hit.
     * </p>
     */
    public int getShortRange() {
        return shortRange;
    }

    /**
     * Returns the medium range limit in yards.
     * <p>
     * Between short and medium range, no modifier to hit.
     * </p>
     */
    public int getMediumRange() {
        return mediumRange;
    }

    /**
     * Returns the long range limit in yards.
     * <p>
     * Between medium and long range, the shooter gets -2 to hit.
     * Beyond long range, the weapon is ineffective.
     * </p>
     */
    public int getLongRange() {
        return longRange;
    }

    /**
     * Returns the armor penetration value (0-9).
     * <p>
     * This value is compared against the target's armor class.
     * If the penetration equals or exceeds the armor class,
     * the armor provides no protection.
     * </p>
     */
    public int getArmorPenetration() {
        return armorPenetration;
    }

    /**
     * Returns the rate of fire (shots per combat round).
     */
    public int getRateOfFire() {
        return rateOfFire;
    }

    /**
     * Returns the weapon weight in kg.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Returns the description of the firearm.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines the range bracket for a given distance.
     *
     * @param distance the distance in yards
     * @return the range bracket
     */
    public RangeBracket getRangeBracket(int distance) {
        if (distance <= shortRange) {
            return RangeBracket.SHORT;
        } else if (distance <= mediumRange) {
            return RangeBracket.MEDIUM;
        } else if (distance <= longRange) {
            return RangeBracket.LONG;
        } else {
            return RangeBracket.OUT_OF_RANGE;
        }
    }

    /**
     * Returns the hit modifier for a given range bracket.
     *
     * @param bracket the range bracket
     * @return the modifier (+2, 0, -2, or -99 for out of range)
     */
    public int getHitModifier(RangeBracket bracket) {
        switch (bracket) {
            case SHORT:
                return 2;
            case MEDIUM:
                return 0;
            case LONG:
                return -2;
            case OUT_OF_RANGE:
                return -99;
            default:
                return 0;
        }
    }

    /**
     * Calculates the damage value from the damage code.
     * <p>
     * Parses codes like "2D6+2" and rolls the dice.
     * </p>
     *
     * @param random the random number generator
     * @return the rolled damage
     */
    public int rollDamage(java.util.Random random) {
        // Parse damage code like "2D6+2", "3D6", "4D6+4"
        int totalDice = 0;
        int bonus = 0;

        // Extract dice count
        int dIndex = damageCode.indexOf('D');
        if (dIndex > 0) {
            totalDice = Integer.parseInt(damageCode.substring(0, dIndex));
        }

        // Extract bonus
        int plusIndex = damageCode.indexOf('+');
        if (plusIndex > 0) {
            bonus = Integer.parseInt(damageCode.substring(plusIndex + 1));
        }

        // Roll dice
        int damage = 0;
        for (int i = 0; i < totalDice; i++) {
            damage += random.nextInt(6) + 1;
        }
        damage += bonus;

        return Math.max(1, damage);
    }

    /**
     * Checks if this weapon can misfire.
     * <p>
     * Plasma guns misfire on a roll of 12.
     * </p>
     *
     * @return true if this weapon can misfire
     */
    public boolean canMisfire() {
        return this == PLASMA_GUN;
    }

    /**
     * Types of firearms in Classic Traveller.
     */
    public enum FirearmType {
        /**
         * Handheld pistols — one-handed sidearms.
         */
        PISTOL("Pistol"),

        /**
         * Long guns — two-handed rifles.
         */
        RIFLE("Rifle"),

        /**
         * Submachine guns — automatic short-range weapons.
         */
        SUBMACHINE_GUN("Submachine Gun"),

        /**
         * Machine guns — sustained fire weapons.
         */
        MACHINE_GUN("Machine Gun"),

        /**
         * Shotguns — close-range spread weapons.
         */
        SHOTGUN("Shotgun"),

        /**
         * Heavy weapons — mounted or two-handed heavy weapons.
         */
        HEAVY("Heavy Weapon"),

        /**
         * Exotic weapons — experimental or advanced technology.
         */
        EXOTIC("Exotic");

        private final String name;

        FirearmType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Range brackets for firearms combat.
     */
    public enum RangeBracket {
        /**
         * Short range — +2 to hit.
         */
        SHORT("Short Range", 2),

        /**
         * Medium range — no modifier.
         */
        MEDIUM("Medium Range", 0),

        /**
         * Long range — -2 to hit.
         */
        LONG("Long Range", -2),

        /**
         * Beyond effective range — cannot hit.
         */
        OUT_OF_RANGE("Out of Range", -99);

        private final String name;
        private final int hitModifier;

        RangeBracket(String name, int hitModifier) {
            this.name = name;
            this.hitModifier = hitModifier;
        }

        public String getName() {
            return name;
        }

        public int getHitModifier() {
            return hitModifier;
        }
    }
}
