package com.yisy.modules.doramcu.hex;

/**
 * Tactical maneuvers available to ships during hex combat.
 * <p>
 * In a hex-based combat system, ships can perform tactical maneuvers
 * to gain advantages: evasive actions to avoid damage, flanking to
 * increase damage, formation flying for defense bonuses, and
 * concentrated fire to focus damage on a single target.
 * </p>
 * <p>
 * Each tactic provides specific combat modifiers:
 * </p>
 * <ul>
 *   <li>Evasive Maneuvers — reduce incoming damage at the cost of own damage</li>
 *   <li>Flanking — increase damage by attacking from advantageous positions</li>
 *   <li>Formation Flying — improve defense through coordinated positioning</li>
 *   <li>Concentrated Fire — focus all weapons on a single target</li>
 *   <li>Shield Rotation — redirect shields to absorb damage on vulnerable sides</li>
 *   <li>Decoy Deployment — deploy decoys to draw enemy fire</li>
 * </ul>
 */
public enum ShipTactic {

    /**
     * Standard combat posture — no modifiers.
     * <p>
     * The ship fights normally without any tactical advantage or disadvantage.
     * </p>
     */
    STANDARD("Standard", 1.0, 1.0, 0, 0, 0),

    /**
     * Evasive maneuvers — reduce incoming damage.
     * <p>
     * The ship performs unpredictable movements to make itself a harder target.
     * Incoming damage is reduced by 25%, but own damage output is reduced by 15%
     * due to the difficulty of aiming while maneuvering.
     * </p>
     */
    EVASIVE("Evasive Maneuvers", 0.85, 0.75, 0, 0, 15),

    /**
     * Flanking maneuver — increase damage output.
     * <p>
     * The ship maneuvers to attack from the enemy's weak side, increasing
     * damage output by 20% but taking 10% more damage due to exposure.
     * Requires higher MDrive capacity to execute effectively.
     * </p>
     */
    FLANKING("Flanking", 1.20, 1.10, 0, 0, -10),

    /**
     * Formation flying — improve defense through coordination.
     * <p>
     * Ships fly in a coordinated formation, providing mutual support.
     * Incoming damage is reduced by 15% and shield effectiveness is improved
     * by 10%, but damage output is reduced by 10% due to formation constraints.
     * </p>
     */
    FORMATION("Formation Flying", 0.90, 0.85, 10, 0, 0),

    /**
     * Concentrated fire — focus all weapons on a single target.
     * <p>
     * All weapons are focused on a single target, increasing damage by 30%
     * but leaving the ship vulnerable to counter-attacks (15% more damage taken).
     * Most effective against high-value targets like capital ships.
     * </p>
     */
    CONCENTRATED_FIRE("Concentrated Fire", 1.30, 1.15, 0, 0, -15),

    /**
     * Shield rotation — redirect shields to vulnerable sides.
     * <p>
     * The ship actively rotates its shields to absorb incoming fire,
     * improving shield effectiveness by 20% but reducing damage output
     * by 10% due to power diversion to shield generators.
     * </p>
     */
    SHIELD_ROTATION("Shield Rotation", 0.90, 0.80, 20, 0, 0),

    /**
     * Decoy deployment — deploy decoys to draw enemy fire.
     * <p>
     * The ship deploys electronic decoys to confuse enemy targeting,
     * reducing incoming damage by 20% but with no offensive benefit.
     * Effective against missile-heavy opponents.
     * </p>
     */
    DECOY("Decoy Deployment", 1.0, 0.80, 0, 20, 0),

    /**
     * Aggressive assault — all-out attack.
     * <p>
     * The ship commits to an all-out attack, increasing damage by 25%
     * but taking 25% more damage. High risk, high reward tactic.
     * </p>
     */
    ASSAULT("Aggressive Assault", 1.25, 1.25, 0, 0, -20),

    /**
     * Defensive hold — prioritize survival.
     * <p>
     * The ship adopts a purely defensive posture, reducing incoming damage
     * by 30% but reducing damage output by 25%. Used when preserving the ship
     * is more important than destroying the enemy.
     * </p>
     */
    DEFENSIVE_HOLD("Defensive Hold", 0.75, 0.70, 15, 10, 10);

    private final String name;
    private final double damageMultiplier;
    private final double incomingDamageMultiplier;
    private final int shieldBonusPercent;
    private final int evasionBonusPercent;
    private final int mDriveRequirement;

    ShipTactic(String name, double damageMultiplier, double incomingDamageMultiplier,
               int shieldBonusPercent, int evasionBonusPercent, int mDriveRequirement) {
        this.name = name;
        this.damageMultiplier = damageMultiplier;
        this.incomingDamageMultiplier = incomingDamageMultiplier;
        this.shieldBonusPercent = shieldBonusPercent;
        this.evasionBonusPercent = evasionBonusPercent;
        this.mDriveRequirement = mDriveRequirement;
    }

    /**
     * Returns the human-readable name of this tactic.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the damage output multiplier for this tactic.
     * <p>
     * Values greater than 1.0 increase damage, values less than 1.0 reduce damage.
     * </p>
     *
     * @return the damage multiplier
     */
    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Returns the incoming damage multiplier for this tactic.
     * <p>
     * Values greater than 1.0 increase incoming damage, values less than 1.0 reduce it.
     * </p>
     *
     * @return the incoming damage multiplier
     */
    public double getIncomingDamageMultiplier() {
        return incomingDamageMultiplier;
    }

    /**
     * Returns the shield bonus percentage for this tactic.
     * <p>
     * Positive values increase shield effectiveness, negative values decrease it.
     * </p>
     *
     * @return the shield bonus percentage
     */
    public int getShieldBonusPercent() {
        return shieldBonusPercent;
    }

    /**
     * Returns the evasion bonus percentage for this tactic.
     * <p>
     * Positive values increase the chance to evade incoming attacks.
     * </p>
     *
     * @return the evasion bonus percentage
     */
    public int getEvasionBonusPercent() {
        return evasionBonusPercent;
    }

    /**
     * Returns the minimum MDrive capacity required to execute this tactic.
     * <p>
     * Some tactics require higher maneuverability to execute effectively.
     * Ships without sufficient MDrive capacity cannot use these tactics.
     * </p>
     *
     * @return the minimum MDrive requirement
     */
    public int getMDriveRequirement() {
        return mDriveRequirement;
    }

    /**
     * Checks if a ship with the given MDrive capacity can execute this tactic.
     *
     * @param mDriveCapacity the ship's MDrive capacity
     * @return true if the ship can execute this tactic
     */
    public boolean canExecute(int mDriveCapacity) {
        return mDriveCapacity >= mDriveRequirement;
    }

    /**
     * Calculates the effective damage for a ship using this tactic.
     *
     * @param baseDamage the ship's base damage
     * @return the effective damage after applying the tactic modifier
     */
    public int calculateEffectiveDamage(int baseDamage) {
        return (int) Math.round(baseDamage * damageMultiplier);
    }

    /**
     * Calculates the effective incoming damage for a ship using this tactic.
     *
     * @param baseIncomingDamage the base incoming damage
     * @return the effective incoming damage after applying the tactic modifier
     */
    public int calculateEffectiveIncomingDamage(int baseIncomingDamage) {
        return (int) Math.round(baseIncomingDamage * incomingDamageMultiplier);
    }

    /**
     * Calculates the effective shield strength for a ship using this tactic.
     *
     * @param baseShieldStrength the ship's base shield strength
     * @return the effective shield strength after applying the tactic modifier
     */
    public int calculateEffectiveShield(int baseShieldStrength) {
        double multiplier = 1.0 + (shieldBonusPercent / 100.0);
        return (int) Math.round(baseShieldStrength * multiplier);
    }

    @Override
    public String toString() {
        return name;
    }
}
