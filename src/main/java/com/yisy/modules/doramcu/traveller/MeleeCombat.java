package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Melee combat engine from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Resolves melee combat between two combatants round by round. Each round
 * follows the Classic Traveller combat sequence:
 * </p>
 * <ol>
 *   <li><b>Attack Roll</b> — attacker rolls 2D6; if ≤ attack value, the attack hits</li>
 *   <li><b>Parry Roll</b> — defender rolls 2D6; if ≤ parry value, the attack is blocked</li>
 *   <li><b>Damage Roll</b> — if the attack lands, damage is D6 × weapon multiplier</li>
 *   <li><b>Armor Reduction</b> — armor protection is subtracted from damage</li>
 *   <li><b>Wound Check</b> — if damage is severe, a wound or death check may be required</li>
 * </ol>
 * <p>
 * Combat continues until one combatant is knocked out or dead, or a maximum
 * number of rounds is reached.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public class MeleeCombat {

    private static final int MAX_ROUNDS = 50;

    private final MeleeCombatant attacker;
    private final MeleeCombatant defender;
    private final List<CombatRound> rounds;
    private final List<String> combatLog;
    private int currentRound;
    private boolean isActive;
    private CombatOutcome outcome;

    /**
     * Possible outcomes of a melee combat encounter.
     */
    public enum CombatOutcome {
        /**
         * Combat is in progress.
         */
        IN_PROGRESS("In Progress"),

        /**
         * Attacker won — defender is knocked out or dead.
         */
        ATTACKER_VICTORY("Attacker Victory"),

        /**
         * Defender won — attacker is knocked out or dead.
         */
        DEFENDER_VICTORY("Defender Victory"),

        /**
         * Both combatants are knocked out — mutual defeat.
         */
        MUTUAL_DEFEAT("Mutual Defeat"),

        /**
         * Combat ended due to maximum rounds reached.
         */
        DRAW("Draw");

        private final String description;

        CombatOutcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Result of a single attack attempt within a combat round.
     */
    public enum AttackResult {
        /**
         * Attack missed — failed to hit.
         */
        MISS("Miss"),

        /**
         * Attack hit but was parried by the defender.
         */
        PARRIED("Parried"),

        /**
         * Attack hit and dealt damage.
         */
        HIT("Hit"),

        /**
         * Attack dealt critical damage.
         */
        CRITICAL("Critical Hit");

        private final String description;

        AttackResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A single round of melee combat.
     */
    public static class CombatRound {
        private final int roundNumber;
        private final String attackerName;
        private final String defenderName;
        private final MeleeWeapon attackerWeapon;
        private final AttackType attackerAttackType;
        private final int attackerRoll;
        private final int attackerValue;
        private final boolean attackerHit;
        private final int defenderParryRoll;
        private final int defenderParryValue;
        private final boolean defenderParried;
        private final int rawDamage;
        private final int armorReduction;
        private final int actualDamage;
        private final AttackResult result;
        private final int defenderHpAfter;
        private final String description;

        CombatRound(int roundNumber, String attackerName, String defenderName,
                    MeleeWeapon attackerWeapon, AttackType attackerAttackType,
                    int attackerRoll, int attackerValue, boolean attackerHit,
                    int defenderParryRoll, int defenderParryValue, boolean defenderParried,
                    int rawDamage, int armorReduction, int actualDamage,
                    AttackResult result, int defenderHpAfter, String description) {
            this.roundNumber = roundNumber;
            this.attackerName = attackerName;
            this.defenderName = defenderName;
            this.attackerWeapon = attackerWeapon;
            this.attackerAttackType = attackerAttackType;
            this.attackerRoll = attackerRoll;
            this.attackerValue = attackerValue;
            this.attackerHit = attackerHit;
            this.defenderParryRoll = defenderParryRoll;
            this.defenderParryValue = defenderParryValue;
            this.defenderParried = defenderParried;
            this.rawDamage = rawDamage;
            this.armorReduction = armorReduction;
            this.actualDamage = actualDamage;
            this.result = result;
            this.defenderHpAfter = defenderHpAfter;
            this.description = description;
        }

        public int getRoundNumber() { return roundNumber; }
        public String getAttackerName() { return attackerName; }
        public String getDefenderName() { return defenderName; }
        public MeleeWeapon getAttackerWeapon() { return attackerWeapon; }
        public AttackType getAttackerAttackType() { return attackerAttackType; }
        public int getAttackerRoll() { return attackerRoll; }
        public int getAttackerValue() { return attackerValue; }
        public boolean isAttackerHit() { return attackerHit; }
        public int getDefenderParryRoll() { return defenderParryRoll; }
        public int getDefenderParryValue() { return defenderParryValue; }
        public boolean isDefenderParried() { return defenderParried; }
        public int getRawDamage() { return rawDamage; }
        public int getArmorReduction() { return armorReduction; }
        public int getActualDamage() { return actualDamage; }
        public AttackResult getResult() { return result; }
        public int getDefenderHpAfter() { return defenderHpAfter; }
        public String getDescription() { return description; }
    }

    /**
     * Types of attacks a combatant can make.
     */
    public enum AttackType {
        /**
         * Cutting/slashing attack.
         */
        CUT("Cut"),

        /**
         * Thrusting/piercing attack.
         */
        THRUST("Thrust"),

        /**
         * Impact/blunt attack.
         */
        IMPACT("Impact");

        private final String name;

        AttackType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Creates a new melee combat encounter.
     *
     * @param attacker the attacking combatant
     * @param defender the defending combatant
     */
    public MeleeCombat(MeleeCombatant attacker, MeleeCombatant defender) {
        this.attacker = Objects.requireNonNull(attacker, "Attacker must not be null");
        this.defender = Objects.requireNonNull(defender, "Defender must not be null");
        this.rounds = new ArrayList<>();
        this.combatLog = new ArrayList<>();
        this.currentRound = 0;
        this.isActive = true;
        this.outcome = CombatOutcome.IN_PROGRESS;

        combatLog.add("Combat starts: " + attacker.getName() + " vs " + defender.getName());
        combatLog.add(attacker.getName() + " wields " + attacker.getWeapon().getName() +
                " in " + attacker.getArmor().getName());
        combatLog.add(defender.getName() + " wields " + defender.getWeapon().getName() +
                " in " + defender.getArmor().getName());
    }

    /**
     * Executes a single round of combat.
     * <p>
     * Both combatants attack simultaneously. The attacker goes first,
     * then the defender counter-attacks.
     * </p>
     *
     * @return the combat round result
     */
    public CombatRound executeRound() {
        if (!isActive) {
            throw new IllegalStateException("Combat is not active");
        }

        currentRound++;

        // Attacker attacks defender
        CombatRound attackRound = resolveAttack(attacker, defender, currentRound);
        rounds.add(attackRound);

        // Check if combat should end
        if (!defender.canFight()) {
            endCombat();
            return attackRound;
        }

        // Defender counter-attacks
        CombatRound counterRound = resolveAttack(defender, attacker, currentRound);
        rounds.add(counterRound);

        // Check if combat should end
        if (!attacker.canFight()) {
            endCombat();
        }

        // Check max rounds
        if (currentRound >= MAX_ROUNDS) {
            outcome = CombatOutcome.DRAW;
            isActive = false;
            combatLog.add("Combat ended: draw after " + MAX_ROUNDS + " rounds");
        }

        return counterRound;
    }

    /**
     * Runs the combat to completion.
     * <p>
     * Executes rounds until one combatant is defeated or max rounds reached.
     * </p>
     *
     * @return the final outcome
     */
    public CombatOutcome fightToCompletion() {
        while (isActive) {
            executeRound();
        }
        return outcome;
    }

    /**
     * Resolves a single attack within a round.
     *
     * @param attacker the attacking combatant
     * @param defender the defending combatant
     * @param roundNumber the current round number
     * @return the combat round result
     */
    private CombatRound resolveAttack(MeleeCombatant attacker, MeleeCombatant defender,
                                       int roundNumber) {
        String attackerName = attacker.getName();
        String defenderName = defender.getName();
        MeleeWeapon weapon = attacker.getWeapon();

        // Choose attack type based on weapon capabilities
        AttackType attackType = chooseAttackType(weapon);

        // Step 1: Attack roll — 2D6 ≤ attack value means hit
        int attackRoll = DiceUtil.roll2D6();
        int attackValue = attacker.getAttackValue();
        boolean hit = attackRoll <= attackValue;

        int parryRoll = 0;
        int parryValue = 0;
        boolean parried = false;
        int rawDamage = 0;
        int armorReduction = 0;
        int actualDamage = 0;
        AttackResult result;
        String description;

        if (!hit) {
            // Miss
            result = AttackResult.MISS;
            parryRoll = -1;
            parryValue = -1;
            description = String.format("%s attacks %s with %s (%s) — rolls %d (needs ≤%d) — MISS",
                    attackerName, defenderName, weapon.getName(), attackType.getName(),
                    attackRoll, attackValue);
            combatLog.add(description);
        } else {
            // Hit — defender tries to parry
            parryRoll = DiceUtil.roll2D6();
            parryValue = defender.getParryValue();
            parried = parryRoll <= parryValue;

            if (parried) {
                result = AttackResult.PARRIED;
                description = String.format("%s attacks %s with %s (%s) — rolls %d (≤%d) — HIT, " +
                        "but %s parries (rolls %d ≤ %d)",
                        attackerName, defenderName, weapon.getName(), attackType.getName(),
                        attackRoll, attackValue, defenderName, parryRoll, parryValue);
                combatLog.add(description);
            } else {
                // Damage calculation
                rawDamage = calculateDamage(weapon, attackType, attacker);
                armorReduction = defender.getArmor().getProtection(
                        convertAttackType(attackType));
                actualDamage = defender.applyDamage(rawDamage, convertAttackType(attackType));

                // Determine if critical
                result = (attackRoll <= 3) ? AttackResult.CRITICAL : AttackResult.HIT;

                int hpAfter = defender.getHitPoints();
                description = String.format("%s attacks %s with %s (%s) — rolls %d (≤%d) — HIT, " +
                        "%s fails parry (rolls %d > %d) — damage %d (armor -%d = %d actual), " +
                        "HP: %d",
                        attackerName, defenderName, weapon.getName(), attackType.getName(),
                        attackRoll, attackValue, defenderName, parryRoll, parryValue,
                        rawDamage, armorReduction, actualDamage, hpAfter);

                if (result == AttackResult.CRITICAL) {
                    description += " — CRITICAL HIT!";
                }
                combatLog.add(description);
            }
        }

        return new CombatRound(
                roundNumber, attackerName, defenderName, weapon, attackType,
                attackRoll, attackValue, hit,
                parryRoll, parryValue, parried,
                rawDamage, armorReduction, actualDamage,
                result, defender.getHitPoints(), description);
    }

    /**
     * Chooses the attack type based on the weapon's capabilities.
     * <p>
     * Prefers the attack type with the highest damage multiplier.
     * </p>
     *
     * @param weapon the weapon being used
     * @return the chosen attack type
     */
    private AttackType chooseAttackType(MeleeWeapon weapon) {
        if (weapon.getType() == MeleeWeapon.WeaponType.IMPACT) {
            return AttackType.IMPACT;
        }
        if (weapon.canThrust() && weapon.getThrust() >= weapon.getCut()) {
            return AttackType.THRUST;
        }
        if (weapon.canCut()) {
            return AttackType.CUT;
        }
        // Fallback
        return AttackType.THRUST;
    }

    /**
     * Calculates the raw damage for an attack.
     * <p>
     * Damage = D6 × weapon multiplier.
     * </p>
     *
     * @param weapon the weapon used
     * @param attackType the type of attack
     * @param attacker the attacking combatant
     * @return the raw damage
     */
    private int calculateDamage(MeleeWeapon weapon, AttackType attackType,
                                 MeleeCombatant attacker) {
        int multiplier;
        switch (attackType) {
            case CUT:
                multiplier = weapon.getCut();
                break;
            case THRUST:
                multiplier = weapon.getThrust();
                break;
            case IMPACT:
                multiplier = weapon.getCut(); // Impact weapons use cut value
                break;
            default:
                multiplier = 1;
        }

        // Roll D6 × multiplier
        int damage = 0;
        for (int i = 0; i < multiplier; i++) {
            damage += DiceUtil.rollD6();
        }

        return Math.max(1, damage); // Minimum 1 damage
    }

    /**
     * Converts combat AttackType to armor AttackType.
     */
    private Armor.AttackType convertAttackType(AttackType attackType) {
        switch (attackType) {
            case CUT:
                return Armor.AttackType.CUT;
            case THRUST:
                return Armor.AttackType.THRUST;
            case IMPACT:
                return Armor.AttackType.IMPACT;
            default:
                return Armor.AttackType.CUT;
        }
    }

    /**
     * Ends the combat and determines the outcome.
     */
    private void endCombat() {
        isActive = false;

        if (!attacker.canFight() && !defender.canFight()) {
            outcome = CombatOutcome.MUTUAL_DEFEAT;
            combatLog.add("Combat ended: mutual defeat — both combatants fallen");
        } else if (!defender.canFight()) {
            outcome = CombatOutcome.ATTACKER_VICTORY;
            String status = defender.isDead() ? "killed" : "knocked out";
            combatLog.add("Combat ended: " + attacker.getName() + " wins — " +
                    defender.getName() + " is " + status);
        } else if (!attacker.canFight()) {
            outcome = CombatOutcome.DEFENDER_VICTORY;
            String status = attacker.isDead() ? "killed" : "knocked out";
            combatLog.add("Combat ended: " + defender.getName() + " wins — " +
                    attacker.getName() + " is " + status);
        }
    }

    /**
     * Returns the attacker.
     *
     * @return the attacker
     */
    public MeleeCombatant getAttacker() {
        return attacker;
    }

    /**
     * Returns the defender.
     *
     * @return the defender
     */
    public MeleeCombatant getDefender() {
        return defender;
    }

    /**
     * Returns the list of combat rounds (immutable).
     *
     * @return the rounds
     */
    public List<CombatRound> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    /**
     * Returns the combat log (immutable).
     *
     * @return the log entries
     */
    public List<String> getCombatLog() {
        return Collections.unmodifiableList(combatLog);
    }

    /**
     * Returns the current round number.
     *
     * @return the round number
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Checks if the combat is still active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the combat outcome.
     *
     * @return the outcome
     */
    public CombatOutcome getOutcome() {
        return outcome;
    }

    /**
     * Returns a summary of the combat.
     *
     * @return the summary
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(attacker.getName()).append(" vs ").append(defender.getName())
                .append(" — ").append(outcome.getDescription()).append("\n");
        sb.append("Rounds: ").append(currentRound).append("\n");

        // Count hits, parries, misses
        int hits = 0, parries = 0, misses = 0, totalDamage = 0;
        for (CombatRound round : rounds) {
            switch (round.getResult()) {
                case HIT:
                case CRITICAL:
                    hits++;
                    totalDamage += round.getActualDamage();
                    break;
                case PARRIED:
                    parries++;
                    break;
                case MISS:
                    misses++;
                    break;
            }
        }

        sb.append("Attacks: ").append(hits + parries + misses)
                .append(" (Hits: ").append(hits)
                .append(", Parries: ").append(parries)
                .append(", Misses: ").append(misses)
                .append(")\n");
        sb.append("Total Damage Dealt: ").append(totalDamage).append("\n");
        sb.append(attacker.getName()).append(" HP: ").append(attacker.getHitPoints())
                .append("/").append(attacker.getMaxHitPoints()).append("\n");
        sb.append(defender.getName()).append(" HP: ").append(defender.getHitPoints())
                .append("/").append(defender.getMaxHitPoints());

        return sb.toString();
    }

}
