package com.yisy.modules.doramcu.traveller;

/**
 * A combatant in a firearms combat encounter.
 * <p>
 * Wraps a {@link TravellerCharacter} with a firearm weapon, cover level,
 * and hit points for firearms combat. The combatant's hit points are
 * based on their Endurance ability, and their attack value is based
 * on their Dexterity and Firearms skill.
 * </p>
 * <p>
 * Cover provides protection against incoming fire. Different cover levels
 * give different protection values that are subtracted from damage.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public class FirearmsCombatant {

    private final TravellerCharacter character;
    private final String combatantId;
    private FirearmWeapon weapon;
    private CoverLevel cover;
    private int hitPoints;
    private int maxHitPoints;
    private boolean isAlive;
    private boolean isKnockedOut;

    /**
     * Levels of cover in firearms combat.
     * <p>
     * Cover provides both a hit penalty for attackers and damage reduction.
     * </p>
     */
    public enum CoverLevel {
        /**
         * No cover — fully exposed.
         */
        NONE("No Cover", 0, 0),

        /**
         * Light cover — small obstacles, partial concealment.
         * <p>
         * -1 to hit, -1 damage reduction.
         * </p>
         */
        LIGHT("Light Cover", -1, 1),

        /**
         * Medium cover — walls, barriers, vehicles.
         * <p>
         * -2 to hit, -2 damage reduction.
         * </p>
         */
        MEDIUM("Medium Cover", -2, 2),

        /**
         * Heavy cover — thick walls, bunkers.
         * <p>
         * -3 to hit, -3 damage reduction.
         * </p>
         */
        HEAVY("Heavy Cover", -3, 3),

        /**
         * Fortified cover — armored positions, blast doors.
         * <p>
         * -4 to hit, -4 damage reduction.
         * </p>
         */
        FORTIFIED("Fortified Cover", -4, 4);

        private final String name;
        private final int hitPenalty;
        private final int damageReduction;

        CoverLevel(String name, int hitPenalty, int damageReduction) {
            this.name = name;
            this.hitPenalty = hitPenalty;
            this.damageReduction = damageReduction;
        }

        public String getName() {
            return name;
        }

        public int getHitPenalty() {
            return hitPenalty;
        }

        public int getDamageReduction() {
            return damageReduction;
        }
    }

    /**
     * Creates a new firearms combatant.
     *
     * @param character the traveller character
     * @param combatantId unique identifier for this combatant
     */
    public FirearmsCombatant(TravellerCharacter character, String combatantId) {
        this.character = character;
        this.combatantId = combatantId;
        this.weapon = FirearmWeapon.PISTOL_22;
        this.cover = CoverLevel.NONE;
        this.maxHitPoints = character.getAbility(AbilityScore.END) * 2;
        this.hitPoints = maxHitPoints;
        this.isAlive = true;
        this.isKnockedOut = false;
    }

    /**
     * Returns the character's name.
     */
    public String getName() {
        return character.getName();
    }

    /**
     * Returns the underlying traveller character.
     */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /**
     * Returns the combatant's unique ID.
     */
    public String getCombatantId() {
        return combatantId;
    }

    /**
     * Returns the current weapon.
     */
    public FirearmWeapon getWeapon() {
        return weapon;
    }

    /**
     * Sets the weapon.
     */
    public void setWeapon(FirearmWeapon weapon) {
        this.weapon = weapon;
    }

    /**
     * Returns the current cover level.
     */
    public CoverLevel getCover() {
        return cover;
    }

    /**
     * Sets the cover level.
     */
    public void setCover(CoverLevel cover) {
        this.cover = cover;
    }

    /**
     * Returns the current hit points.
     */
    public int getHitPoints() {
        return hitPoints;
    }

    /**
     * Returns the maximum hit points.
     */
    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    /**
     * Checks if the combatant is alive.
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Checks if the combatant is knocked out.
     */
    public boolean isKnockedOut() {
        return isKnockedOut;
    }

    /**
     * Checks if the combatant can still fight.
     * <p>
     * A combatant can fight if they are alive and not knocked out.
     * </p>
     */
    public boolean canFight() {
        return isAlive && !isKnockedOut && hitPoints > 0;
    }

    /**
     * Returns the attack value for firearms combat.
     * <p>
     * Attack value = Dexterity + Firearms skill bonus.
     * The Firearms skill grants a +2 bonus.
     * </p>
     */
    public int getAttackValue() {
        int dex = character.getAbility(AbilityScore.DEX);
        int bonus = 0;

        // Check for Firearms skill
        for (Skill skill : character.getSkills()) {
            if (skill == Skill.FIREARMS) {
                bonus += 2;
                break;
            }
        }

        return dex + bonus;
    }

    /**
     * Applies damage to this combatant.
     * <p>
     * Damage is reduced by cover. If damage reduces hit points to 0 or below,
     * the combatant is knocked out. If damage reduces hit points to -Endurance
     * or below, the combatant is killed.
     * </p>
     *
     * @param rawDamage the raw damage dealt
     * @return the actual damage after cover reduction
     */
    public int applyDamage(int rawDamage) {
        int reduction = cover.getDamageReduction();
        int actualDamage = Math.max(0, rawDamage - reduction);

        hitPoints -= actualDamage;

        // Check status
        if (hitPoints <= 0) {
            isKnockedOut = true;
            if (hitPoints <= -character.getAbility(AbilityScore.END)) {
                isAlive = false;
            }
        }

        return actualDamage;
    }

    /**
     * Returns the combatant's status description.
     */
    public String getStatus() {
        if (!isAlive) {
            return "DEAD";
        } else if (isKnockedOut) {
            return "KNOCKED OUT";
        } else if (hitPoints < maxHitPoints * 0.25) {
            return "CRITICAL";
        } else if (hitPoints < maxHitPoints * 0.5) {
            return "WOUNDED";
        } else {
            return "OK";
        }
    }

    /**
     * Returns a summary of the combatant.
     */
    public String getSummary() {
        return String.format("%s (%s) — HP: %d/%d, Weapon: %s, Cover: %s, Status: %s",
                getName(), combatantId, hitPoints, maxHitPoints,
                weapon.getName(), cover.getName(), getStatus());
    }
}
