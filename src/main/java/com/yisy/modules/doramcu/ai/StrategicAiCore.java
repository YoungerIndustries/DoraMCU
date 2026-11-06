package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SpaceStationType;

import java.util.List;

/**
 * Strategic AI core responsible for empire-level decisions.
 * <p>
 * This core handles high-level strategy including resource allocation,
 * expansion planning, fleet deployment, and production scheduling.
 * Its behavior is shaped by the empire's personality axes:
 * <ul>
 *   <li><b>Economic focus</b>: prioritizes resource accumulation and efficient production</li>
 *   <li><b>Expansion drive</b>: prioritizes territorial growth and scouting</li>
 *   <li><b>Scientific focus</b>: prioritizes research and technology</li>
 *   <li><b>Diplomatic stance</b>: influences willingness to coexist with neighbors</li>
 * </ul>
 * </p>
 * <p>
 * The strategic core returns a decision only when a strategic priority
 * (such as building ships) outweighs tactical concerns. Otherwise it
 * returns null, allowing the tactical core to handle the turn.
 * </p>
 */
public class StrategicAiCore {

    private final EmpireAiCore empireCore;

    /**
     * Creates a new StrategicAiCore.
     *
     * @param empireCore the parent empire AI core
     */
    public StrategicAiCore(EmpireAiCore empireCore) {
        this.empireCore = empireCore;
    }

    /**
     * Makes a strategic decision based on the current context.
     * <p>
     * Returns a decision only if a strategic priority is identified.
     * Returns null if no strategic action is needed, allowing the
     * tactical core to handle the turn.
     * </p>
     *
     * @param context the current decision context
     * @return a strategic decision, or null if tactical decisions should take precedence
     */
    public AiDecision decide(AiDecisionContext context) {
        if (context == null) {
            return null;
        }

        // Check if we should build ships
        AiDecision buildDecision = evaluateBuildPriority(context);
        if (buildDecision != null) {
            return buildDecision;
        }

        // Check if we should build a station
        AiDecision stationDecision = evaluateBuildStationPriority(context);
        if (stationDecision != null) {
            return stationDecision;
        }

        // Check if we should upgrade a station's AI Core
        AiDecision upgradeDecision = evaluateUpgradeStationPriority(context);
        if (upgradeDecision != null) {
            return upgradeDecision;
        }

        // Check if we should expand
        AiDecision expandDecision = evaluateExpansionPriority(context);
        if (expandDecision != null) {
            return expandDecision;
        }

        // No strategic priority — let tactical core decide
        return null;
    }

    /**
     * Evaluates whether building ships is the current priority.
     */
    private AiDecision evaluateBuildPriority(AiDecisionContext context) {
        double economicFocus = empireCore.getPersonality().getEconomicFocus();
        double aggression = empireCore.getPersonality().getAggression();
        int minerals = context.getMinerals();
        int energy = context.getEnergy();

        // Build priority is high when economic focus or aggression is high
        double buildPriority = (economicFocus + aggression) / 2.0;

        // Need sufficient resources to build
        if (minerals < 20 || energy < 10) {
            return null;
        }

        // If we have few fleets, prioritize building
        List<Fleet> ownedFleets = context.getOwnedFleets();
        int totalShips = ownedFleets.stream()
                .mapToInt(f -> f.getShips().size())
                .sum();

        // Low ship count — build regardless of personality
        if (totalShips <= 2 && minerals >= 20 && energy >= 10) {
            ShipType shipType = empireCore.chooseShipType(context);
            return AiDecision.buildShip(
                    shipType,
                    0.8,
                    "Low ship count. Building " + shipType + " to reinforce fleet."
            );
        }

        // High build priority and sufficient resources
        if (buildPriority > 0.6 && minerals >= 30 && energy >= 15) {
            ShipType shipType = empireCore.chooseShipType(context);
            return AiDecision.buildShip(
                    shipType,
                    buildPriority,
                    "Strategic build priority. Building " + shipType + " for expansion."
            );
        }

        return null;
    }

    /**
     * Evaluates whether building a station is the current priority.
     */
    private AiDecision evaluateBuildStationPriority(AiDecisionContext context) {
        double economicFocus = empireCore.getPersonality().getEconomicFocus();
        double scientificFocus = empireCore.getPersonality().getScientificFocus();
        double aggression = empireCore.getPersonality().getAggression();
        int minerals = context.getMinerals();
        int energy = context.getEnergy();

        // Need substantial resources to build a station
        if (minerals < 50 || energy < 30) {
            return null;
        }

        // Determine which station type to build based on personality
        SpaceStationType stationType;
        double priority;

        if (aggression > 0.7) {
            // Aggressive empires prioritize military stations
            stationType = SpaceStationType.MILITARY;
            priority = aggression;
        } else if (scientificFocus > 0.6) {
            // Scientific empires prioritize research stations
            stationType = SpaceStationType.RESEARCH;
            priority = scientificFocus;
        } else if (economicFocus > 0.6) {
            // Economic empires prioritize trade or mining stations
            stationType = SpaceStationType.TRADE;
            priority = economicFocus;
        } else {
            // Default to military for defense
            stationType = SpaceStationType.MILITARY;
            priority = 0.5;
        }

        // Only build if priority is high enough
        if (priority > 0.5 && minerals >= stationType.getMineralCost() / 2 && energy >= stationType.getEnergyCost() / 2) {
            // Find a position near a fleet for the station
            Hex3D position = findStationPosition(context);
            if (position != null) {
                return AiDecision.buildStation(
                        stationType,
                        position,
                        priority,
                        "Strategic station build priority. Building " + stationType + " for " +
                                (stationType == SpaceStationType.MILITARY ? "defense" :
                                        stationType == SpaceStationType.RESEARCH ? "research" : "economy") + "."
                );
            }
        }

        return null;
    }

    /**
     * Evaluates whether upgrading a station's AI Core is the current priority.
     */
    private AiDecision evaluateUpgradeStationPriority(AiDecisionContext context) {
        // Station upgrade priority is based on research level and available resources
        int researchLevel = empireCore.getResearchLevel();
        int minerals = context.getMinerals();
        int energy = context.getEnergy();

        // Need research level and resources to upgrade
        if (researchLevel < 3 || minerals < 30 || energy < 20) {
            return null;
        }

        // Check if context has stations to upgrade
        List<SpaceStation> stations = context.getStations();
        if (stations == null || stations.isEmpty()) {
            return null;
        }

        // Find a station without an AI Core or with a low-level one
        for (SpaceStation station : stations) {
            if (!station.hasAiCore() || station.getAiCoreLevel() < 2) {
                double priority = (researchLevel / 10.0) * 0.8;
                if (priority > 0.3) {
                    return AiDecision.upgradeStation(
                            station.getId(),
                            priority,
                            "Upgrading AI Core on " + station.getName() + " to improve station operations."
                    );
                }
            }
        }

        return null;
    }

    /**
     * Finds a suitable position for building a new station.
     */
    private Hex3D findStationPosition(AiDecisionContext context) {
        List<Fleet> ownedFleets = context.getOwnedFleets();
        if (ownedFleets.isEmpty()) {
            return null;
        }

        // Build near the first fleet that has a position
        for (Fleet fleet : ownedFleets) {
            if (fleet.getPosition() != null) {
                return fleet.getPosition();
            }
        }

        return null;
    }

    /**
     * Evaluates whether expansion is the current priority.
     */
    private AiDecision evaluateExpansionPriority(AiDecisionContext context) {
        double expansionDrive = empireCore.getPersonality().getExpansionDrive();
        List<Fleet> ownedFleets = context.getOwnedFleets();
        List<Fleet> enemyFleets = context.getEnemyFleets();

        // Only expand if no immediate threats
        if (!enemyFleets.isEmpty()) {
            // Check if enemies are far away
            boolean threatNearby = false;
            for (Fleet enemy : enemyFleets) {
                for (Fleet friendly : ownedFleets) {
                    if (friendly.getPosition() != null && enemy.getPosition() != null) {
                        int dist = hexDistance(friendly.getPosition(), enemy.getPosition());
                        if (dist <= 3) {
                            threatNearby = true;
                            break;
                        }
                    }
                }
                if (threatNearby) break;
            }
            if (threatNearby) {
                return null;
            }
        }

        // High expansion drive and no threats — move outward
        if (expansionDrive > 0.6 && !ownedFleets.isEmpty()) {
            Fleet bestFleet = empireCore.selectBestFleet(context);
            if (bestFleet != null && bestFleet.getPosition() != null) {
                // Find a fleet that's not near an enemy
                return AiDecision.moveFleet(
                        bestFleet.getId(),
                        bestFleet.getPosition(), // Tactical core will refine the target
                        expansionDrive,
                        "Expansion priority. Moving fleet outward."
                );
            }
        }

        return null;
    }

    /**
     * Calculates the strategic priority scores for the current situation.
     * <p>
     * Useful for debugging and analysis.
     * </p>
     *
     * @param context the current context
     * @return array of [buildPriority, expandPriority, combatPriority, researchPriority]
     */
    public double[] getPriorityScores(AiDecisionContext context) {
        double buildPriority = (empireCore.getPersonality().getEconomicFocus() +
                empireCore.getPersonality().getAggression()) / 2.0;
        double expandPriority = empireCore.getPersonality().getExpansionDrive();
        double combatPriority = empireCore.getPersonality().getAggression();
        double researchPriority = empireCore.getPersonality().getScientificFocus();

        // Adjust based on resource availability
        if (context.getMinerals() < 20) {
            buildPriority *= 0.5;
        }
        if (context.getOwnedFleets().isEmpty()) {
            expandPriority *= 0.3;
        }

        return new double[]{buildPriority, expandPriority, combatPriority, researchPriority};
    }

    /**
     * Calculates hex distance between two positions.
     */
    private int hexDistance(com.yisy.modules.doramcu.hex.Hex3D a, com.yisy.modules.doramcu.hex.Hex3D b) {
        return (Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ())) / 2;
    }
}
