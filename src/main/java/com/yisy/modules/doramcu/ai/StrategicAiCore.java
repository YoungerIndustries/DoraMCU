package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategic AI core that handles empire-level strategy decisions.
 * <p>
 * Evaluates long-term priorities such as expansion, economy, research,
 * and diplomatic stance. Returns null when no strategic action takes
 * priority, allowing the tactical layer to handle immediate concerns.
 * </p>
 */
public class StrategicAiCore {

    private static final Logger log = LoggerFactory.getLogger(StrategicAiCore.class);

    private final EmpireAiCore empireCore;

    /**
     * Creates a new StrategicAiCore for the given empire.
     *
     * @param empireCore the parent empire AI core
     */
    public StrategicAiCore(EmpireAiCore empireCore) {
        this.empireCore = empireCore;
    }

    /**
     * Makes a strategic decision based on the current context.
     * <p>
     * Prioritizes expansion when the empire has few planets, then economy
     * and research. Returns null if no strategic action is appropriate,
     * allowing the tactical layer to decide.
     * </p>
     *
     * @param context the current decision context
     * @return the chosen strategic decision, or null to defer to tactical layer
     */
    public AiDecision decide(AiDecisionContext context) {
        if (context == null) {
            return null;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return null;
        }

        int planetCount = player.getPlanetCount();

        // Expansion priority: if we have few planets, prioritize building stations
        if (planetCount < 3) {
            double buildScore = empireCore.evaluateAction(AiDecision.ActionType.BUILD_STATION, context);
            if (buildScore > 0.4 && context.getMinerals() >= 200 && context.getEnergy() >= 100) {
                return AiDecision.buildStation("Prioritizing expansion — building infrastructure");
            }
        }

        // Build ships if we have resources and few ships
        int fleetCount = context.getOwnedFleets().size();
        if (fleetCount < 5 && context.getMinerals() >= 30 && context.getEnergy() >= 20) {
            double buildScore = empireCore.evaluateAction(AiDecision.ActionType.BUILD_SHIP, context);
            if (buildScore > 0.3) {
                Fleet fleet = empireCore.selectBestFleet(context);
                if (fleet != null) {
                    com.yisy.modules.doramcu.hex.ShipType shipType = empireCore.chooseShipType(context);
                    return AiDecision.buildShip(shipType, fleet.getId(), "Expanding fleet capacity");
                }
            }
        }

        // Research priority: invest in tech when resources are abundant
        double researchScore = empireCore.evaluateAction(AiDecision.ActionType.UPGRADE_STATION, context);
        if (researchScore > 0.6 && context.getMinerals() >= 100 && context.getCredits() >= 50) {
            return AiDecision.wait("Investing in research and upgrades");
        }

        // Fortify if under threat
        if (!context.getEnemyFleets().isEmpty()) {
            int enemyCount = context.getEnemyFleets().size();
            int friendlyCount = context.getOwnedFleets().size();
            if (enemyCount > friendlyCount) {
                double fortifyScore = empireCore.evaluateAction(AiDecision.ActionType.FORTIFY, context);
                if (fortifyScore > 0.5) {
                    Fleet fleet = empireCore.selectBestFleet(context);
                    if (fleet != null) {
                        return AiDecision.fortify(fleet.getId(), "Outnumbered — fortifying position");
                    }
                }
            }
        }

        // No strategic priority — defer to tactical layer
        return null;
    }
}
