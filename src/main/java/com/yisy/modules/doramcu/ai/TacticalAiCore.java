package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Ship;

import java.util.List;
import java.util.Random;

/**
 * Tactical AI core responsible for ship and fleet-level decisions.
 * <p>
 * This core handles combat evaluation, target selection, movement decisions,
 * and tactical mode selection. Its behavior is influenced by the empire's
 * personality and research level.
 * </p>
 * <p>
 * Higher research levels unlock more sophisticated tactics:
 * <ul>
 *   <li>Level 0-2: Basic target priority and movement</li>
 *   <li>Level 3-5: Formation awareness and flanking maneuvers</li>
 *   <li>Level 6-8: Predictive combat simulation</li>
 *   <li>Level 9-10: Adaptive tactics based on enemy behavior</li>
 * </ul>
 * </p>
 */
public class TacticalAiCore {

    private static final Random random = new Random();

    /**
     * Tactical modes that affect fleet behavior.
     */
    public enum TacticalMode {
        /** Aggressive: close range, high damage */
        AGGRESSIVE,
        /** Defensive: maintain distance, protect assets */
        DEFENSIVE,
        /** Evasive: retreat and avoid combat */
        EVASIVE,
        /** Ambush: position for surprise attacks */
        AMBUSH,
        /** Flanking: maneuver around enemy */
        FLANKING
    }

    private final EmpireAiCore empireCore;

    /**
     * Creates a new TacticalAiCore.
     *
     * @param empireCore the parent empire AI core
     */
    public TacticalAiCore(EmpireAiCore empireCore) {
        this.empireCore = empireCore;
    }

    /**
     * Makes a tactical decision based on the current context.
     * <p>
     * Evaluates whether to engage in combat, move, or wait based on
     * the personality's aggression level and the tactical situation.
     </p>
     *
     * @param context the current decision context
     * @return the tactical decision
     */
    public AiDecision decide(AiDecisionContext context) {
        Fleet bestFleet = empireCore.selectBestFleet(context);
        if (bestFleet == null || bestFleet.getPosition() == null) {
            return AiDecision.wait("No valid fleet available");
        }

        Fleet nearestEnemy = empireCore.findNearestEnemy(context);

        // No enemies found — expand or wait
        if (nearestEnemy == null) {
            return decideNoEnemy(context, bestFleet);
        }

        // Evaluate combat vs movement
        return decideWithEnemy(context, bestFleet, nearestEnemy);
    }

    /**
     * Decides what to do when an enemy is present.
     */
    private AiDecision decideWithEnemy(AiDecisionContext context, Fleet myFleet, Fleet enemy) {
        if (enemy.getPosition() == null) {
            return AiDecision.wait("Enemy position unknown");
        }

        int distance = hexDistance(myFleet.getPosition(), enemy.getPosition());
        double aggression = empireCore.getPersonality().getAggression();
        double riskTolerance = empireCore.getPersonality().getRiskTolerance();

        // Evaluate combat strength
        int myStrength = calculateFleetStrength(myFleet);
        int enemyStrength = calculateFleetStrength(enemy);
        double strengthRatio = enemyStrength > 0 ? (double) myStrength / enemyStrength : 1.0;

        // Adjacent and strong enough — engage
        if (distance <= 1 && strengthRatio >= 0.7 && aggression > 0.4) {
            double confidence = aggression * strengthRatio;
            return AiDecision.engageCombat(
                    myFleet.getId(),
                    enemy.getId(),
                    confidence,
                    "Engaging enemy fleet at close range. Strength ratio: " + String.format("%.2f", strengthRatio)
            );
        }

        // Adjacent but weak — retreat if low risk tolerance
        if (distance <= 1 && strengthRatio < 0.5 && riskTolerance < 0.4) {
            // Move away from enemy
            Hex3D retreatPos = calculateRetreatPosition(myFleet.getPosition(), enemy.getPosition());
            return AiDecision.moveFleet(
                    myFleet.getId(),
                    retreatPos,
                    1.0 - riskTolerance,
                    "Retreating from stronger enemy. Strength ratio: " + String.format("%.2f", strengthRatio)
            );
        }

        // Not adjacent — move toward enemy if aggressive
        if (aggression > 0.5 || (aggression > 0.3 && strengthRatio > 0.8)) {
            Hex3D moveTarget = calculateApproachPosition(myFleet.getPosition(), enemy.getPosition());
            double confidence = aggression * 0.8;
            return AiDecision.moveFleet(
                    myFleet.getId(),
                    moveTarget,
                    confidence,
                    "Moving toward enemy fleet. Distance: " + distance
            );
        }

        // Default: hold position
        return AiDecision.wait("Holding position. Distance to enemy: " + distance + ", strength ratio: " + String.format("%.2f", strengthRatio));
    }

    /**
     * Decides what to do when no enemy is nearby.
     */
    private AiDecision decideNoEnemy(AiDecisionContext context, Fleet myFleet) {
        double expansionDrive = empireCore.getPersonality().getExpansionDrive();

        // High expansion drive — scout outward
        if (expansionDrive > 0.5) {
            Hex3D scoutPos = calculateScoutPosition(myFleet.getPosition());
            return AiDecision.scout(
                    myFleet.getId(),
                    scoutPos,
                    expansionDrive,
                    "Scouting for expansion. No enemies detected."
            );
        }

        // Otherwise, hold position
        return AiDecision.wait("No enemies detected. Holding position.");
    }

    /**
     * Selects the tactical mode based on personality and situation.
     */
    public TacticalMode selectMode(AiDecisionContext context) {
        double aggression = empireCore.getPersonality().getAggression();
        double riskTolerance = empireCore.getPersonality().getRiskTolerance();
        int researchLevel = empireCore.getResearchLevel();

        Fleet nearestEnemy = empireCore.findNearestEnemy(context);
        boolean enemyNearby = nearestEnemy != null;

        // Research level unlocks more modes
        if (researchLevel >= 6 && enemyNearby && aggression > 0.6) {
            return TacticalMode.FLANKING;
        }
        if (researchLevel >= 4 && enemyNearby && aggression > 0.4) {
            return TacticalMode.AMBUSH;
        }

        if (aggression > 0.7) {
            return TacticalMode.AGGRESSIVE;
        }
        if (riskTolerance < 0.3) {
            return TacticalMode.EVASIVE;
        }
        if (aggression < 0.4) {
            return TacticalMode.DEFENSIVE;
        }

        return TacticalMode.AGGRESSIVE;
    }

    /**
     * Selects the highest priority target from a list of enemy ships.
     */
    public Ship selectTarget(List<Ship> enemyShips) {
        if (enemyShips == null || enemyShips.isEmpty()) {
            return null;
        }

        // High aggression: target ships with highest hull ratio (healthiest)
        if (empireCore.getPersonality().getAggression() > 0.6) {
            return enemyShips.stream()
                    .max((a, b) -> Double.compare(
                            (double) a.getCurrentHullPoints() / Math.max(1, a.getMaxHullPoints()),
                            (double) b.getCurrentHullPoints() / Math.max(1, b.getMaxHullPoints())))
                    .orElse(enemyShips.get(0));
        }

        // Low aggression: target most damaged ships
        return enemyShips.stream()
                .min((a, b) -> Double.compare(
                        (double) a.getCurrentHullPoints() / Math.max(1, a.getMaxHullPoints()),
                        (double) b.getCurrentHullPoints() / Math.max(1, b.getMaxHullPoints())))
                .orElse(enemyShips.get(0));
    }

    /**
     * Calculates the total combat strength of a fleet.
     */
    private int calculateFleetStrength(Fleet fleet) {
        if (fleet == null || fleet.getShips().isEmpty()) {
            return 0;
        }
        return fleet.getShips().stream()
                .mapToInt(ship -> {
                    double hullRatio = (double) ship.getCurrentHullPoints() / Math.max(1, ship.getMaxHullPoints());
                    // Use max hull as a proxy for combat power
                    return (int) (ship.getMaxHullPoints() * hullRatio);
                })
                .sum();
    }

    /**
     * Calculates hex distance between two positions.
     */
    private int hexDistance(Hex3D a, Hex3D b) {
        return (Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ())) / 2;
    }

    /**
     * Calculates a position one step closer to the target.
     */
    private Hex3D calculateApproachPosition(Hex3D current, Hex3D target) {
        int dx = Integer.compare(target.getX(), current.getX());
        int dy = Integer.compare(target.getY(), current.getY());

        // Move in the dominant axis
        if (Math.abs(target.getX() - current.getX()) >= Math.abs(target.getY() - current.getY())) {
            int newZ = -(current.getX() + dx + current.getY());
            return new Hex3D(current.getX() + dx, current.getY(), newZ);
        } else {
            int newZ = -(current.getX() + current.getY() + dy);
            return new Hex3D(current.getX(), current.getY() + dy, newZ);
        }
    }

    /**
     * Calculates a position one step away from the enemy.
     */
    private Hex3D calculateRetreatPosition(Hex3D current, Hex3D enemy) {
        int dx = -Integer.compare(enemy.getX(), current.getX());
        int dy = -Integer.compare(enemy.getY(), current.getY());

        if (Math.abs(enemy.getX() - current.getX()) >= Math.abs(enemy.getY() - current.getY())) {
            int newZ = -(current.getX() + dx + current.getY());
            return new Hex3D(current.getX() + dx, current.getY(), newZ);
        } else {
            int newZ = -(current.getX() + current.getY() + dy);
            return new Hex3D(current.getX(), current.getY() + dy, newZ);
        }
    }

    /**
     * Calculates a scout position outward from the center.
     */
    private Hex3D calculateScoutPosition(Hex3D current) {
        int range = 3 + empireCore.getResearchLevel() / 2;
        int dx = random.nextInt(range * 2) - range;
        int dy = random.nextInt(range * 2) - range;
        int dz = -(current.getX() + dx + current.getY() + dy);
        return new Hex3D(current.getX() + dx, current.getY() + dy, dz);
    }
}
