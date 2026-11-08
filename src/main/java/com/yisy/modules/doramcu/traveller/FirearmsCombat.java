package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Firearms combat engine from Classic Traveller Book 1: Characters & Combat (1981).
 * <p>
 * Resolves firearms combat between two combatants round by round. Each round
 * follows the Classic Traveller firearms combat sequence:
 * </p>
 * <ol>
 *   <li><b>Range Check</b> — determine the range bracket and hit modifier</li>
 *   <li><b>Cover Check</b> — apply cover penalty to hit</li>
 *   <li><b>Attack Roll</b> — attacker rolls 2D6; if ≤ attack value + modifiers, the attack hits</li>
 *   <li><b>Misfire Check</b> — some weapons (plasma) can misfire on a roll of 12</li>
 *   <li><b>Damage Roll</b> — if the attack lands, damage is rolled from the weapon's damage code</li>
 *   <li><b>Armor Penetration</b> — if weapon AP ≥ armor class, armor is ignored</li>
 *   <li><b>Cover Reduction</b> — cover damage reduction is applied</li>
 *   <li><b>Status Check</b> — check if target is knocked out or dead</li>
 * </ol>
 * <p>
 * Each combatant can fire multiple shots per round based on their weapon's
 * rate of fire. Combat continues until one combatant is eliminated or
 * a maximum number of rounds is reached.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1, Chapter 4: Combat.
 * </p>
 */
public class FirearmsCombat {

    private static final int MAX_ROUNDS = 30;

    private final FirearmsCombatant shooter1;
    private final FirearmsCombatant shooter2;
    private final List<CombatRound> rounds;
    private final List<String> combatLog;
    private int currentRound;
    private boolean isActive;
    private CombatOutcome outcome;

    /**
     * Possible outcomes of a firearms combat encounter.
     */
    public enum CombatOutcome {
        IN_PROGRESS("In Progress"),
        SHOOTER1_VICTORY("Shooter 1 Victory"),
        SHOOTER2_VICTORY("Shooter 2 Victory"),
        MUTUAL_DEFEAT("Mutual Defeat"),
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
     * Result of a single shot attempt.
     */
    public enum ShotResult {
        MISS("Miss"),
        MISFIRE("Misfire"),
        HIT("Hit"),
        CRITICAL("Critical Hit"),
        OUT_OF_RANGE("Out of Range");

        private final String description;

        ShotResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A single shot within a combat round.
     */
    public static class Shot {
        private final int shotNumber;
        private final String shooterName;
        private final String targetName;
        private final FirearmWeapon weapon;
        private final int distance;
        private final FirearmWeapon.RangeBracket rangeBracket;
        private final int attackRoll;
        private final int attackValue;
        private final int hitModifier;
        private final boolean hit;
        private final boolean misfire;
        private final int rawDamage;
        private final int coverReduction;
        private final int actualDamage;
        private final ShotResult result;
        private final int targetHpAfter;
        private final String description;

        Shot(int shotNumber, String shooterName, String targetName,
             FirearmWeapon weapon, int distance, FirearmWeapon.RangeBracket rangeBracket,
             int attackRoll, int attackValue, int hitModifier, boolean hit,
             boolean misfire, int rawDamage, int coverReduction, int actualDamage,
             ShotResult result, int targetHpAfter, String description) {
            this.shotNumber = shotNumber;
            this.shooterName = shooterName;
            this.targetName = targetName;
            this.weapon = weapon;
            this.distance = distance;
            this.rangeBracket = rangeBracket;
            this.attackRoll = attackRoll;
            this.attackValue = attackValue;
            this.hitModifier = hitModifier;
            this.hit = hit;
            this.misfire = misfire;
            this.rawDamage = rawDamage;
            this.coverReduction = coverReduction;
            this.actualDamage = actualDamage;
            this.result = result;
            this.targetHpAfter = targetHpAfter;
            this.description = description;
        }

        public int getShotNumber() { return shotNumber; }
        public String getShooterName() { return shooterName; }
        public String getTargetName() { return targetName; }
        public FirearmWeapon getWeapon() { return weapon; }
        public int getDistance() { return distance; }
        public FirearmWeapon.RangeBracket getRangeBracket() { return rangeBracket; }
        public int getAttackRoll() { return attackRoll; }
        public int getAttackValue() { return attackValue; }
        public int getHitModifier() { return hitModifier; }
        public boolean isHit() { return hit; }
        public boolean isMisfire() { return misfire; }
        public int getRawDamage() { return rawDamage; }
        public int getCoverReduction() { return coverReduction; }
        public int getActualDamage() { return actualDamage; }
        public ShotResult getResult() { return result; }
        public int getTargetHpAfter() { return targetHpAfter; }
        public String getDescription() { return description; }
    }

    /**
     * A single round of firearms combat.
     */
    public static class CombatRound {
        private final int roundNumber;
        private final List<Shot> shots;
        private final String description;

        CombatRound(int roundNumber, List<Shot> shots, String description) {
            this.roundNumber = roundNumber;
            this.shots = Collections.unmodifiableList(shots);
            this.description = description;
        }

        public int getRoundNumber() { return roundNumber; }
        public List<Shot> getShots() { return shots; }
        public String getDescription() { return description; }
    }

    /**
     * Creates a new firearms combat encounter.
     *
     * @param shooter1 the first shooter
     * @param shooter2 the second shooter
     * @param distance the distance between combatants in yards
     */
    public FirearmsCombat(FirearmsCombatant shooter1, FirearmsCombatant shooter2, int distance) {
        this.shooter1 = Objects.requireNonNull(shooter1, "Shooter 1 must not be null");
        this.shooter2 = Objects.requireNonNull(shooter2, "Shooter 2 must not be null");
        this.rounds = new ArrayList<>();
        this.combatLog = new ArrayList<>();
        this.currentRound = 0;
        this.isActive = true;
        this.outcome = CombatOutcome.IN_PROGRESS;

        combatLog.add("Firearms combat starts: " + shooter1.getName() + " vs " + shooter2.getName());
        combatLog.add("Distance: " + distance + " yards");
        combatLog.add(shooter1.getName() + " wields " + shooter1.getWeapon().getName() +
                " with " + shooter1.getCover().getName());
        combatLog.add(shooter2.getName() + " wields " + shooter2.getWeapon().getName() +
                " with " + shooter2.getCover().getName());
    }

    /**
     * Executes a single round of combat.
     * <p>
     * Both combatants fire simultaneously. Each fires their weapon's rate of fire
     * number of shots.
     * </p>
     *
     * @return the combat round result
     */
    public CombatRound executeRound() {
        if (!isActive) {
            throw new IllegalStateException("Combat is not active");
        }

        currentRound++;
        List<Shot> roundShots = new ArrayList<>();

        // Determine distance (can change if combatants move)
        int distance = getCurrentDistance();

        // Shooter 1 fires at Shooter 2
        List<Shot> shots1 = executeShooter(shooter1, shooter2, distance);
        roundShots.addAll(shots1);

        // Check if shooter2 is still able to fight
        if (!shooter2.canFight()) {
            endCombat();
            return new CombatRound(currentRound, roundShots,
                    shooter1.getName() + " eliminates " + shooter2.getName() + " in round " + currentRound);
        }

        // Shooter 2 fires at Shooter 1
        List<Shot> shots2 = executeShooter(shooter2, shooter1, distance);
        roundShots.addAll(shots2);

        // Check if shooter1 is still able to fight
        if (!shooter1.canFight()) {
            endCombat();
        }

        // Check max rounds
        if (currentRound >= MAX_ROUNDS) {
            outcome = CombatOutcome.DRAW;
            isActive = false;
            combatLog.add("Combat ended: draw after " + MAX_ROUNDS + " rounds");
        }

        return new CombatRound(currentRound, roundShots,
                "Round " + currentRound + ": " + shots1.size() + " shots from " +
                shooter1.getName() + ", " + shots2.size() + " shots from " + shooter2.getName());
    }

    /**
     * Runs the combat to completion.
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
     * Executes all shots for a shooter in a round.
     *
     * @param shooter the shooter
     * @param target the target
     * @param distance the distance in yards
     * @return list of shots fired
     */
    private List<Shot> executeShooter(FirearmsCombatant shooter, FirearmsCombatant target,
                                       int distance) {
        List<Shot> shots = new ArrayList<>();
        FirearmWeapon weapon = shooter.getWeapon();
        int rateOfFire = weapon.getRateOfFire();

        for (int i = 0; i < rateOfFire && shooter.canFight(); i++) {
            Shot shot = resolveShot(shooter, target, distance, i + 1);
            shots.add(shot);

            // If target is down, stop firing
            if (!target.canFight()) {
                break;
            }
        }

        return shots;
    }

    /**
     * Resolves a single shot.
     *
     * @param shooter the shooter
     * @param target the target
     * @param distance the distance in yards
     * @param shotNumber the shot number within the round
     * @return the shot result
     */
    private Shot resolveShot(FirearmsCombatant shooter, FirearmsCombatant target,
                              int distance, int shotNumber) {
        String shooterName = shooter.getName();
        String targetName = target.getName();
        FirearmWeapon weapon = shooter.getWeapon();

        // Determine range bracket
        FirearmWeapon.RangeBracket rangeBracket = weapon.getRangeBracket(distance);

        // Check if out of range
        if (rangeBracket == FirearmWeapon.RangeBracket.OUT_OF_RANGE) {
            String desc = String.format("%s fires at %s (%s) — OUT OF RANGE at %d yards",
                    shooterName, targetName, weapon.getName(), distance);
            combatLog.add(desc);
            return new Shot(shotNumber, shooterName, targetName, weapon, distance, rangeBracket,
                    -1, -1, 0, false, false, 0, 0, 0,
                    ShotResult.OUT_OF_RANGE, target.getHitPoints(), desc);
        }

        // Calculate hit modifier
        int rangeModifier = weapon.getHitModifier(rangeBracket);
        int coverPenalty = target.getCover().getHitPenalty();
        int totalModifier = rangeModifier + coverPenalty;

        // Attack roll
        int attackRoll = DiceUtil.roll2D6();
        int attackValue = shooter.getAttackValue();
        int modifiedAttackValue = attackValue + totalModifier;

        // Check for misfire (plasma guns)
        boolean misfire = weapon.canMisfire() && attackRoll == 12;

        if (misfire) {
            String desc = String.format("%s fires at %s (%s) — MISFIRE! (rolled 12)",
                    shooterName, targetName, weapon.getName());
            combatLog.add(desc);
            return new Shot(shotNumber, shooterName, targetName, weapon, distance, rangeBracket,
                    attackRoll, attackValue, totalModifier, false, true, 0, 0, 0,
                    ShotResult.MISFIRE, target.getHitPoints(), desc);
        }

        boolean hit = attackRoll <= modifiedAttackValue;

        if (!hit) {
            String desc = String.format("%s fires at %s (%s) — rolls %d (needs ≤%d, %s %s) — MISS",
                    shooterName, targetName, weapon.getName(),
                    attackRoll, modifiedAttackValue,
                    rangeBracket.getName(),
                    formatModifier(totalModifier));
            combatLog.add(desc);
            return new Shot(shotNumber, shooterName, targetName, weapon, distance, rangeBracket,
                    attackRoll, attackValue, totalModifier, false, false, 0, 0, 0,
                    ShotResult.MISS, target.getHitPoints(), desc);
        }

        // Hit — calculate damage
        int rawDamage = weapon.rollDamage(null);
        int coverReduction = target.getCover().getDamageReduction();
        int actualDamage = target.applyDamage(rawDamage);

        // Determine if critical (natural 2 or 3)
        ShotResult result = (attackRoll <= 3) ? ShotResult.CRITICAL : ShotResult.HIT;

        String desc = String.format("%s fires at %s (%s) — rolls %d (≤%d, %s %s) — HIT, " +
                "damage %d (cover -%d = %d actual), HP: %d",
                shooterName, targetName, weapon.getName(),
                attackRoll, modifiedAttackValue,
                rangeBracket.getName(), formatModifier(totalModifier),
                rawDamage, coverReduction, actualDamage, target.getHitPoints());

        if (result == ShotResult.CRITICAL) {
            desc += " — CRITICAL HIT!";
        }
        combatLog.add(desc);

        return new Shot(shotNumber, shooterName, targetName, weapon, distance, rangeBracket,
                attackRoll, attackValue, totalModifier, true, false,
                rawDamage, coverReduction, actualDamage,
                result, target.getHitPoints(), desc);
    }

    /**
     * Returns the current distance between combatants.
     * <p>
     * In a simple implementation, distance is fixed. In a more complex
     * implementation, combatants could move each round.
     * </p>
     */
    private int getCurrentDistance() {
        // Default distance — could be made configurable
        return 50;
    }

    /**
     * Formats a modifier value with a + sign for positive values.
     */
    private String formatModifier(int modifier) {
        if (modifier >= 0) {
            return "+" + modifier;
        }
        return String.valueOf(modifier);
    }

    /**
     * Ends the combat and determines the outcome.
     */
    private void endCombat() {
        isActive = false;

        if (!shooter1.canFight() && !shooter2.canFight()) {
            outcome = CombatOutcome.MUTUAL_DEFEAT;
            combatLog.add("Combat ended: mutual defeat — both combatants eliminated");
        } else if (!shooter2.canFight()) {
            outcome = CombatOutcome.SHOOTER1_VICTORY;
            String status = shooter2.isAlive() ? "knocked out" : "killed";
            combatLog.add("Combat ended: " + shooter1.getName() + " wins — " +
                    shooter2.getName() + " is " + status);
        } else if (!shooter1.canFight()) {
            outcome = CombatOutcome.SHOOTER2_VICTORY;
            String status = shooter1.isAlive() ? "knocked out" : "killed";
            combatLog.add("Combat ended: " + shooter2.getName() + " wins — " +
                    shooter1.getName() + " is " + status);
        }
    }

    /**
     * Returns the first shooter.
     */
    public FirearmsCombatant getShooter1() {
        return shooter1;
    }

    /**
     * Returns the second shooter.
     */
    public FirearmsCombatant getShooter2() {
        return shooter2;
    }

    /**
     * Returns the list of combat rounds (immutable).
     */
    public List<CombatRound> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    /**
     * Returns the combat log (immutable).
     */
    public List<String> getCombatLog() {
        return Collections.unmodifiableList(combatLog);
    }

    /**
     * Returns the current round number.
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Checks if the combat is still active.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the combat outcome.
     */
    public CombatOutcome getOutcome() {
        return outcome;
    }

    /**
     * Returns a summary of the combat.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(shooter1.getName()).append(" vs ").append(shooter2.getName())
                .append(" — ").append(outcome.getDescription()).append("\n");
        sb.append("Rounds: ").append(currentRound).append("\n");

        // Count shots, hits, misses, misfires
        int totalShots = 0, hits = 0, misses = 0, misfires = 0, totalDamage = 0;
        for (CombatRound round : rounds) {
            for (Shot shot : round.getShots()) {
                totalShots++;
                switch (shot.getResult()) {
                    case HIT:
                    case CRITICAL:
                        hits++;
                        totalDamage += shot.getActualDamage();
                        break;
                    case MISS:
                        misses++;
                        break;
                    case MISFIRE:
                        misfires++;
                        break;
                    default:
                        break;
                }
            }
        }

        sb.append("Shots: ").append(totalShots)
                .append(" (Hits: ").append(hits)
                .append(", Misses: ").append(misses)
                .append(", Misfires: ").append(misfires)
                .append(")\n");
        sb.append("Total Damage Dealt: ").append(totalDamage).append("\n");
        sb.append(shooter1.getName()).append(" HP: ").append(shooter1.getHitPoints())
                .append("/").append(shooter1.getMaxHitPoints()).append("\n");
        sb.append(shooter2.getName()).append(" HP: ").append(shooter2.getHitPoints())
                .append("/").append(shooter2.getMaxHitPoints());

        return sb.toString();
    }

}
