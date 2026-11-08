package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * A combatant in a melee combat encounter.
 * <p>
 * Wraps a {@link TravellerCharacter} with combat-specific state:
 * weapon, armor, hit points, and combat status. Each combatant
 * enters combat with a weapon and armor, and tracks their
 * remaining hit points and wound status.
 * </p>
 * <p>
 * Hit points are calculated as Endurance × 2. When hit points
 * reach 0, the combatant is knocked out. Negative hit points
 * indicate the combatant is dying.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public class MeleeCombatant {

    private final TravellerCharacter character;
    private MeleeWeapon weapon;
    private Armor armor;
    private int hitPoints;
    private int maxHitPoints;
    private boolean knockedOut;
    private boolean dead;
    private final String combatantId;

    /**
     * Creates a new melee combatant from a character.
     * <p>
     * The combatant starts with full hit points (Endurance × 2),
     * no weapon (unarmed), and no armor.
     * </p>
     *
     * @param character the character entering combat
     * @param combatantId unique identifier for this combatant instance
     */
    public MeleeCombatant(TravellerCharacter character, String combatantId) {
        this.character = Objects.requireNonNull(character, "Character must not be null");
        this.combatantId = Objects.requireNonNull(combatantId, "Combatant ID must not be null");
        this.weapon = MeleeWeapon.UNARMED;
        this.armor = Armor.NONE;
        this.maxHitPoints = calculateMaxHitPoints();
        this.hitPoints = maxHitPoints;
        this.knockedOut = false;
        this.dead = false;
    }

    /**
     * Returns the underlying character.
     *
     * @return the character
     */
    public TravellerCharacter getCharacter() {
        return character;
    }

    /**
     * Returns the unique combatant ID.
     *
     * @return the combatant ID
     */
    public String getCombatantId() {
        return combatantId;
    }

    /**
     * Returns the character's name.
     *
     * @return the name
     */
    public String getName() {
        return character.getName();
    }

    /**
     * Returns the currently equipped weapon.
     *
     * @return the weapon
     */
    public MeleeWeapon getWeapon() {
        return weapon;
    }

    /**
     * Equips the combatant with a weapon.
     *
     * @param weapon the weapon to equip
     */
    public void setWeapon(MeleeWeapon weapon) {
        this.weapon = Objects.requireNonNull(weapon, "Weapon must not be null");
    }

    /**
     * Returns the currently worn armor.
     *
     * @return the armor
     */
    public Armor getArmor() {
        return armor;
    }

    /**
     * Equips the combatant with armor.
     *
     * @param armor the armor to wear
     */
    public void setArmor(Armor armor) {
        this.armor = Objects.requireNonNull(armor, "Armor must not be null");
    }

    /**
     * Returns the current hit points.
     *
     * @return the hit points
     */
    public int getHitPoints() {
        return hitPoints;
    }

    /**
     * Returns the maximum hit points.
     *
     * @return the max hit points
     */
    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    /**
     * Returns the character's Dexterity score.
     * <p>
     * Used for attack and parry rolls.
     * </p>
     *
     * @return the Dexterity score
     */
    public int getDexterity() {
        return character.getAbility(AbilityScore.DEX);
    }

    /**
     * Returns the character's Endurance score.
     * <p>
     * Used for damage calculation and hit points.
     * If the combatant wears powered armor, +1 is added.
     * </p>
     *
     * @return the effective Endurance score
     */
    public int getEffectiveEndurance() {
        int end = character.getAbility(AbilityScore.END);
        if (armor.isPowered()) {
            end += 1;
        }
        return end;
    }

    /**
     * Returns the character's Saving Throw score.
     * <p>
     * Used for wound and death checks.
     * </p>
     *
     * @return the Saving Throw score
     */
    public int getSavingThrow() {
        return character.getAbility(AbilityScore.ST);
    }

    /**
     * Checks if the combatant has the Melee skill.
     *
     * @return true if the character has Melee skill
     */
    public boolean hasMeleeSkill() {
        return character.hasSkill(Skill.MELEE);
    }

    /**
     * Returns the attack value for this combatant.
     * <p>
     * Attack value = Dexterity + weapon parry bonus.
     * If the combatant has the Melee skill, +2 is added.
     * </p>
     *
     * @return the attack value
     */
    public int getAttackValue() {
        int value = getDexterity() + weapon.getParry();
        if (hasMeleeSkill()) {
            value += 2;
        }
        return value;
    }

    /**
     * Returns the parry value for this combatant.
     * <p>
     * Parry value = Dexterity + weapon parry bonus.
     * If the combatant has the Melee skill, +2 is added.
     * Shield or off-hand weapon bonuses could be added here.
     * </p>
     *
     * @return the parry value
     */
    public int getParryValue() {
        int value = getDexterity() + weapon.getParry();
        if (hasMeleeSkill()) {
            value += 2;
        }
        return value;
    }

    /**
     * Calculates the maximum hit points based on Endurance.
     * <p>
     * Hit points = Endurance × 2.
     * </p>
     *
     * @return the max hit points
     */
    private int calculateMaxHitPoints() {
        return character.getAbility(AbilityScore.END) * 2;
    }

    /**
     * Applies damage to this combatant.
     * <p>
     * Damage is reduced by armor protection. If hit points
     * reach 0 or below, the combatant is knocked out.
     * If hit points reach -Endurance or below, the combatant dies.
     * </p>
     *
     * @param rawDamage the raw damage before armor reduction
     * @param attackType the type of attack (for armor lookup)
     * @return the actual damage dealt (after armor)
     */
    public int applyDamage(int rawDamage, Armor.AttackType attackType) {
        if (rawDamage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative: " + rawDamage);
        }

        // Reduce damage by armor
        int protection = armor.getProtection(attackType);
        int actualDamage = Math.max(0, rawDamage - protection);

        hitPoints -= actualDamage;

        // Check status
        if (hitPoints <= 0) {
            knockedOut = true;
        }
        if (hitPoints <= -getEffectiveEndurance()) {
            dead = true;
            knockedOut = true;
        }

        return actualDamage;
    }

    /**
     * Checks if the combatant is knocked out.
     * <p>
     * A combatant is knocked out when hit points reach 0 or below.
     * </p>
     *
     * @return true if knocked out
     */
    public boolean isKnockedOut() {
        return knockedOut;
    }

    /**
     * Checks if the combatant is dead.
     *
     * @return true if dead
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Checks if the combatant can still fight.
     * <p>
     * A combatant can fight if they are not knocked out or dead.
     * </p>
     *
     * @return true if can fight
     */
    public boolean canFight() {
        return !knockedOut && !dead;
    }

    /**
     * Heals the combatant by the given amount.
     * <p>
     * Hit points cannot exceed max hit points.
     * If healed above 0, the knocked out status is cleared.
     * </p>
     *
     * @param amount the amount to heal
     */
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative: " + amount);
        }
        if (dead) {
            return; // Cannot heal a dead combatant
        }

        hitPoints = Math.min(maxHitPoints, hitPoints + amount);
        if (hitPoints > 0) {
            knockedOut = false;
        }
    }

    /**
     * Returns a summary of the combatant's status.
     *
     * @return the status string
     */
    public String getStatus() {
        if (dead) {
            return "DEAD";
        }
        if (knockedOut) {
            return "KNOCKED OUT";
        }
        int hpPercent = (hitPoints * 100) / maxHitPoints;
        if (hpPercent >= 75) {
            return "FULLY OPERATIONAL";
        }
        if (hpPercent >= 50) {
            return "MINOR WOUNDS";
        }
        if (hpPercent >= 25) {
            return "MODERATE WOUNDS";
        }
        return "CRITICAL";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append(" [")
                .append(weapon.getName()).append(" / ").append(armor.getName())
                .append("] HP:").append(hitPoints).append("/").append(maxHitPoints)
                .append(" (").append(getStatus()).append(")");
        return sb.toString();
    }

    private String name() {
        return character.getName();
    }
}
