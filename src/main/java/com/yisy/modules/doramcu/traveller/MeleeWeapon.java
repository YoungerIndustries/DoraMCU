package com.yisy.modules.doramcu.traveller;

/**
 * Melee weapons from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Each weapon has three key statistics:
 * </p>
 * <ul>
 *   <li><b>Cut</b> — damage dealt with a slashing attack (D6 × cut value)</li>
 *   <li><b>Thrust</b> — damage dealt with a thrusting attack (D6 × thrust value)</li>
 *   <li><b>Parry</b> — parry bonus added when defending with this weapon</li>
 * </ul>
 * <p>
 * Weapons are organized by type: unarmed, edged, impact, polearms, and exotic.
 * The weapon type determines which attacks are available and any special rules.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public enum MeleeWeapon {

    // ---- Unarmed ----

    /**
     * Unarmed combat — fists, kicks, and grappling.
     * <p>
     * No weapon required. Low damage but always available.
     * </p>
     */
    UNARMED("Unarmed", WeaponType.UNARMED, 1, 1, 0,
            "Fists and kicks — no weapon required"),

    // ---- Edged Weapons ----

    /**
     * Knife — short blade for thrusting and cutting.
     * <p>
     * The most common melee weapon. Balanced cut and thrust damage.
     * </p>
     */
    KNIFE("Knife", WeaponType.EDGED, 1, 2, 1,
            "Short blade — balanced cut and thrust"),

    /**
     * Dirk — thrusting dagger.
     * <p>
     * Optimized for thrusting attacks. Higher thrust than a knife.
     * </p>
     */
    DIRK("Dirk", WeaponType.EDGED, 1, 3, 1,
            "Thrusting dagger — optimized for thrusting"),

    /**
     * Cutlass — curved slashing blade.
     * <p>
     * Superior cutting power. Popular with sailors and soldiers.
     * </p>
     */
    CUTLASS("Cutlass", WeaponType.EDGED, 2, 1, 1,
            "Curved blade — superior cutting power"),

    /**
     * Saber — military sabre.
     * <p>
     * Better than a cutlass. Standard military sidearm.
     * </p>
     */
    SABER("Saber", WeaponType.EDGED, 2, 1, 2,
            "Military sabre — standard sidearm"),

    /**
     * Broadsword — two-handed cutting sword.
     * <p>
     * Heavy cutting weapon. Requires two hands to wield effectively.
     * </p>
     */
    BROADSWORD("Broadsword", WeaponType.EDGED, 3, 1, 2,
            "Two-handed cutting sword — heavy damage"),

    /**
     * Rapier — thrusting sword.
     * <p>
     * Fine thrusting weapon with excellent parry value.
     * Favored by duellists and nobles.
     * </p>
     */
    RAPIER("Rapier", WeaponType.EDGED, 1, 3, 3,
            "Thrusting sword — excellent parry value"),

    /**
     * Small Sword — light duelling sword.
     * <p>
     * Light thrusting weapon with the best parry value of any sword.
     * </p>
     */
    SMALL_SWORD("Small Sword", WeaponType.EDGED, 1, 2, 4,
            "Light duelling sword — best parry value"),

    /**
     * Long Sword — versatile two-handed sword.
     * <p>
     * Excellent cutting and good thrusting. Requires two hands.
     * </p>
     */
    LONG_SWORD("Long Sword", WeaponType.EDGED, 3, 2, 2,
            "Two-handed sword — excellent cutting"),

    /**
     * Scimitar — curved eastern blade.
     * <p>
     * Similar to a cutlass but with better cutting power.
     * </p>
     */
    SCIMITAR("Scimitar", WeaponType.EDGED, 3, 1, 1,
            "Curved eastern blade — heavy cutting"),

    // ---- Impact Weapons ----

    /**
     * Club — simple impact weapon.
     * <p>
     * A stick or bat used for blunt force trauma.
     * </p>
     */
    CLUB("Club", WeaponType.IMPACT, 2, 0, 0,
            "Simple impact weapon — blunt force"),

    /**
     * Mace — spiked impact weapon.
     * <p>
     * Armor-piercing impact weapon. Ignores some armor protection.
     * </p>
     */
    MACE("Mace", WeaponType.IMPACT, 3, 0, 1,
            "Spiked impact — armor-piercing"),

    /**
     * Morning Star — flanged impact weapon.
     * <p>
     * Heavy impact weapon with good armor penetration.
     * </p>
     */
    MORNING_STAR("Morning Star", WeaponType.IMPACT, 3, 0, 1,
            "Flanged impact — heavy armor penetration"),

    /**
     * Staff — two-handed pole used for striking.
     * <p>
     * Versatile impact weapon with good reach.
     * </p>
     */
    STAFF("Staff", WeaponType.IMPACT, 2, 0, 1,
            "Two-handed pole — good reach"),

    // ---- Polearms ----

    /**
     * Spear — thrusting polearm.
     * <p>
     * The most common polearm. Excellent thrusting damage and reach.
     * </p>
     */
    SPEAR("Spear", WeaponType.POLEARM, 1, 3, 1,
            "Thrusting polearm — excellent reach"),

    /**
     * Halberd — polearm with axe and spike.
     * <p>
     * Versatile polearm with cutting and thrusting capability.
     * </p>
     */
    HALBERD("Halberd", WeaponType.POLEARM, 2, 3, 2,
            "Polearm with axe — versatile"),

    /**
     * Lance — cavalry weapon.
     * <p>
     * Heavy thrusting polearm. Bonus damage when charging.
     * </p>
     */
    LANCE("Lance", WeaponType.POLEARM, 1, 4, 0,
            "Heavy thrusting — bonus when charging"),

    // ---- Exotic Weapons ----

    /**
     * Laser Cutter — industrial tool used as a weapon.
     * <p>
     * Energy weapon that cuts through armor.
     * Very high damage but rare.
     * </p>
     */
    LASER_CUTTER("Laser Cutter", WeaponType.EXOTIC, 4, 4, 0,
            "Energy weapon — cuts through armor"),

    /**
     * Monomolecular Wire — ultra-thin cutting wire.
     * <p>
     * Invisible cutting weapon. Extremely dangerous in close combat.
     * </p>
     */
    MONO_WIRE("Mono Wire", WeaponType.EXOTIC, 4, 0, 0,
            "Invisible cutting wire — extremely dangerous");

    private final String name;
    private final WeaponType type;
    private final int cut;
    private final int thrust;
    private final int parry;
    private final String description;

    MeleeWeapon(String name, WeaponType type, int cut, int thrust, int parry,
                String description) {
        this.name = name;
        this.type = type;
        this.cut = cut;
        this.thrust = thrust;
        this.parry = parry;
        this.description = description;
    }

    /**
     * Returns the display name of the weapon.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the weapon.
     *
     * @return the weapon type
     */
    public WeaponType getType() {
        return type;
    }

    /**
     * Returns the cut damage multiplier.
     * <p>
     * When a cut attack hits, damage is rolled as D6 × cut value.
     * A value of 0 means this weapon cannot cut.
     * </p>
     *
     * @return the cut multiplier (0 if cannot cut)
     */
    public int getCut() {
        return cut;
    }

    /**
     * Returns the thrust damage multiplier.
     * <p>
     * When a thrust attack hits, damage is rolled as D6 × thrust value.
     * A value of 0 means this weapon cannot thrust.
     * </p>
     *
     * @return the thrust multiplier (0 if cannot thrust)
     */
    public int getThrust() {
        return thrust;
    }

    /**
     * Returns the parry bonus of this weapon.
     * <p>
     * Added to Dexterity when parrying with this weapon.
     * Higher parry values make it easier to block incoming attacks.
     * </p>
     *
     * @return the parry bonus
     */
    public int getParry() {
        return parry;
    }

    /**
     * Returns the description of the weapon.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this weapon can perform a cut attack.
     *
     * @return true if cut value > 0
     */
    public boolean canCut() {
        return cut > 0;
    }

    /**
     * Checks if this weapon can perform a thrust attack.
     *
     * @return true if thrust value > 0
     */
    public boolean canThrust() {
        return thrust > 0;
    }

    /**
     * Returns the maximum damage multiplier for this weapon.
     * <p>
     * Useful for comparing weapon power.
     * </p>
     *
     * @return the max of cut and thrust
     */
    public int getMaxDamage() {
        return Math.max(cut, thrust);
    }

    /**
     * Types of melee weapons in Classic Traveller.
     */
    public enum WeaponType {
        /**
         * Unarmed combat — no weapon.
         */
        UNARMED("Unarmed"),

        /**
         * Edged weapons — swords, knives, and blades.
         */
        EDGED("Edged"),

        /**
         * Impact weapons — clubs, maces, and blunt instruments.
         */
        IMPACT("Impact"),

        /**
         * Polearms — spears, halberds, and lances.
         */
        POLEARM("Polearm"),

        /**
         * Exotic weapons — energy weapons and advanced technology.
         */
        EXOTIC("Exotic");

        private final String name;

        WeaponType(String name) {
            this.name = name;
        }

        /**
         * Returns the display name of the weapon type.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }
    }
}
