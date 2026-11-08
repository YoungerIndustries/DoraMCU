package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Tactical AI core that handles ship and fleet-level decisions.
 * <p>
 * Evaluates combat, movement, and scouting options based on the current
 * game state and the empire's personality.
 * </p>
 */
public class TacticalAiCore {

    /**
     * Tactical modes that influence decision making.
     */
    public enum TacticalMode {
        OFFENSIVE,
        DEFENSIVE,
        SCOUTING,
        ECONOMIC,
        BALANCED
    }

    private static final Logger log = LoggerFactory.getLogger(TacticalAiCore.class);

    private final EmpireAiCore empireCore;

    /**
     * Creates a new TacticalAiCore for the given empire.
     *
     * @param empireCore the parent empire AI core
     */
    public TacticalAiCore(EmpireAiCore empireCore) {
        this.empireCore = empireCore;
    }

    /**
     * Makes a tactical decision based on the current context.
     * <p>
     * Prioritizes combat when enemies are nearby, then scouting, then building.
     * </p>
     *
     * @param context the current decision context
     * @return the chosen tactical decision, or null if no tactical action is appropriate
     */
    public AiDecision decide(AiDecisionContext context) {
        if (context == null || context.getOwnedFleets().isEmpty()) {
            return AiDecision.wait("No fleets available");
        }

        // Check for nearby enemies to engage
        Fleet nearestEnemy = empireCore.findNearestEnemy(context);
        if (nearestEnemy != null && nearestEnemy.getPosition() != null) {
            Fleet bestFleet = empireCore.selectBestFleet(context);
            if (bestFleet != null && bestFleet.getPosition() != null) {
                // Check if adjacent for combat
                if (isAdjacent(bestFleet.getPosition(), nearestEnemy.getPosition())) {
                    double combatScore = empireCore.evaluateAction(AiDecision.ActionType.ENGAGE_COMBAT, context);
                    if (combatScore > 0.4) {
                        return AiDecision.engageCombat(
                                bestFleet.getId(),
                                nearestEnemy.getId(),
                                "Engaging nearest enemy fleet at distance 1");
                    }
                }

                // Move toward enemy
                double moveScore = empireCore.evaluateAction(AiDecision.ActionType.MOVE_FLEET, context);
                if (moveScore > 0.3) {
                    return AiDecision.moveFleet(
                            bestFleet.getId(),
                            nearestEnemy.getPosition(),
                            "Moving toward enemy fleet");
                }
            }
        }

        // Scout if we have fleets and no immediate threats
        Fleet scoutFleet = empireCore.selectBestFleet(context);
        if (scoutFleet != null && scoutFleet.getPosition() != null) {
            double scoutScore = empireCore.evaluateAction(AiDecision.ActionType.SCOUT, context);
            if (scoutScore > 0.3) {
                // Move to a position adjacent to current for scouting
                Hex3D current = scoutFleet.getPosition();
                Hex3D scoutTarget = new Hex3D(current.getX() + 1, current.getY(), current.getZ() - 1);
                return AiDecision.scout(
                        scoutFleet.getId(),
                        scoutTarget,
                        "Scouting adjacent sector");
            }
        }

        // Build ship if resources allow
        double buildScore = empireCore.evaluateAction(AiDecision.ActionType.BUILD_SHIP, context);
        if (buildScore > 0.4 && context.getMinerals() >= 30 && context.getEnergy() >= 20) {
            Fleet fleet = empireCore.selectBestFleet(context);
            if (fleet != null) {
                ShipType shipType = empireCore.chooseShipType(context);
                return AiDecision.buildShip(
                        shipType,
                        fleet.getId(),
                        "Building " + shipType + " to expand fleet");
            }
        }

        // Default: wait
        return AiDecision.wait("No tactical action available");
    }

    /**
     * Checks if two hex positions are adjacent.
     */
    private boolean isAdjacent(Hex3D a, Hex3D b) {
        if (a == null || b == null) {
            return false;
        }
        int distance = (Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ())) / 2;
        return distance <= 1;
    }

    /**
     * Selects the best target ship from a list of enemy ships.
     * <p>
     * Prioritizes ships with the highest hull points (healthiest first).
     * </p>
     *
     * @param ships the list of enemy ships to choose from
     * @return the best target ship, or null if the list is empty
     */
    public Ship selectTarget(List<Ship> ships) {
        if (ships == null || ships.isEmpty()) {
            return null;
        }
        return ships.stream()
                .filter(s -> !s.isDestroyed())
                .max(Comparator.comparingDouble(Ship::getHullHealthRatio))
                .orElse(ships.get(0));
    }
}
