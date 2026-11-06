package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doradl.model.GameStateFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Extracts empire-specific features (dimensions 32-63) from game domain objects
 * and populates them into a {@link GameStateFeatures} vector.
 * <p>
 * This bridge connects the DoraMCU game domain (Player, Fleet, Ship, SpaceStation)
 * with the dora-dl-core neural network feature vector. The first 32 dimensions
 * are populated by {@link com.yisy.modules.doradl.dl.network.GameStateFeatureExtractor}
 * from DTOs; this class fills dimensions 32-63 with empire-specific data.
 * </p>
 * <p>
 * All values are normalized to [0, 1] range for neural network input.
 * </p>
 */
public class EmpireFeatureExtractor {

    private static final Logger log = LoggerFactory.getLogger(EmpireFeatureExtractor.class);

    // Normalization constants
    private static final int MAX_FLEETS = 50;
    private static final int MAX_STATIONS = 30;
    private static final int MAX_TECH_LEVEL = 10;
    private static final int MAX_RESOURCE_RATE = 1000;
    private static final int MAX_PLANETS = 100;

    /**
     * Extracts empire-specific features and populates dimensions 32-63 of the given feature vector.
     *
     * @param features the feature vector to populate (must be 64 dimensions)
     * @param player the player/empire to extract features from
     * @param context the current AI decision context with fleets and stations
     */
    public void extractEmpireFeatures(GameStateFeatures features, Player player, AiDecisionContext context) {
        if (features == null || player == null) {
            return;
        }

        extractPersonality(features, player);
        extractGovernmentEthos(features, player);
        extractResearchLevel(features, player, context);
        extractFleetComposition(features, player, context);
        extractStationCoverage(features, context);
        extractEconomicHealth(features, player, context);
        extractDiplomaticContext(features, player, context);
        extractTacticalSituation(features, context);
        extractTechnologyEdge(features, player);
        extractResourcePipeline(features, player);
        extractFleetReadiness(features, context);
        extractStrategicDepth(features, player, context);
    }

    /**
     * Sets personality features (indices 32-33): aggression and expansion drive.
     */
    private void extractPersonality(GameStateFeatures features, Player player) {
        AiPersonality personality = AiPersonality.fromPlayer(player);
        features.setPersonality(
                personality.getAggression(),
                personality.getExpansionDrive()
        );
    }

    /**
     * Sets government/ethos encoding (indices 34-35).
     * Normalizes the enum ordinal to [0, 1].
     */
    private void extractGovernmentEthos(GameStateFeatures features, Player player) {
        double governmentIndex = player.getGovernmentType() != null
                ? (double) player.getGovernmentType().ordinal() / (Player.GovernmentType.values().length - 1)
                : 0.0;
        double ethosIndex = player.getEthos() != null
                ? (double) player.getEthos().ordinal() / (Player.Ethos.values().length - 1)
                : 0.0;
        features.setGovernmentEthos(governmentIndex, ethosIndex);
    }

    /**
     * Sets research level and AI core level (indices 36-37).
     */
    private void extractResearchLevel(GameStateFeatures features, Player player, AiDecisionContext context) {
        // Research level from context if available, otherwise estimate from player
        double researchLevel = 0.0;
        double aiCoreLevel = 0.0;

        if (context != null && context.getOwnedFleets() != null) {
            // Estimate research level from fleet sophistication
            int totalShips = 0;
            int capitalShips = 0;
            for (Fleet fleet : context.getOwnedFleets()) {
                for (Ship ship : fleet.getShips()) {
                    totalShips++;
                    if (ship.getShipType() == ShipType.BATTLESHIP || ship.getShipType() == ShipType.MOTHERSHIP) {
                        capitalShips++;
                    }
                }
            }
            // Higher capital ship ratio suggests higher research
            researchLevel = totalShips > 0 ? (double) capitalShips / totalShips : 0.0;
        }

        // AI core level based on player's AI research
        aiCoreLevel = Math.min(1.0, researchLevel * 2.0);

        features.setResearchLevel(
                normalize(researchLevel, 1.0),
                normalize(aiCoreLevel, 1.0)
        );
    }

    /**
     * Sets fleet composition (indices 38-39): fighter ratio and capital ship ratio.
     */
    private void extractFleetComposition(GameStateFeatures features, Player player, AiDecisionContext context) {
        if (context == null || context.getOwnedFleets() == null) {
            features.setFleetComposition(0.0, 0.0);
            return;
        }

        int totalShips = 0;
        int fighterShips = 0;  // Frigates are considered fighters
        int capitalShips = 0;   // Battleships and Motherships

        for (Fleet fleet : context.getOwnedFleets()) {
            for (Ship ship : fleet.getShips()) {
                totalShips++;
                if (ship.getShipType() == ShipType.FRIGATE) {
                    fighterShips++;
                }
                if (ship.getShipType() == ShipType.BATTLESHIP || ship.getShipType() == ShipType.MOTHERSHIP) {
                    capitalShips++;
                }
            }
        }

        double fighterRatio = totalShips > 0 ? (double) fighterShips / totalShips : 0.0;
        double capitalRatio = totalShips > 0 ? (double) capitalShips / totalShips : 0.0;

        features.setFleetComposition(fighterRatio, capitalRatio);
    }

    /**
     * Sets station coverage (indices 40-41): military and research station counts.
     */
    private void extractStationCoverage(GameStateFeatures features, AiDecisionContext context) {
        if (context == null || context.getStations() == null) {
            features.setStationCoverage(0.0, 0.0);
            return;
        }

        int militaryStations = 0;
        int researchStations = 0;

        for (SpaceStation station : context.getStations()) {
            if (station.getStationType() == SpaceStationType.MILITARY) {
                militaryStations++;
            }
            if (station.getStationType() == SpaceStationType.RESEARCH) {
                researchStations++;
            }
        }

        features.setStationCoverage(
                normalize(militaryStations, MAX_STATIONS),
                normalize(researchStations, MAX_STATIONS)
        );
    }

    /**
     * Sets economic health (indices 42-43): credit flow and upkeep ratio.
     */
    private void extractEconomicHealth(GameStateFeatures features, Player player, AiDecisionContext context) {
        if (context == null) {
            features.setEconomicHealth(0.0, 0.0);
            return;
        }

        // Estimate credit flow from resources
        double creditFlow = 0.0;
        if (context.getResources() != null) {
            int credits = context.getResources().getOrDefault("credits", 0);
            creditFlow = normalize(credits, MAX_RESOURCE_RATE);
        }

        // Estimate upkeep ratio from fleet count vs resources
        double upkeepRatio = 0.0;
        if (context.getOwnedFleets() != null && context.getResources() != null) {
            int totalShips = 0;
            for (Fleet fleet : context.getOwnedFleets()) {
                totalShips += fleet.getShipCount();
            }
            int minerals = context.getResources().getOrDefault("minerals", 0);
            int energy = context.getResources().getOrDefault("energy", 0);
            // Upkeep ratio: higher when fleet is large relative to resources
            upkeepRatio = Math.min(1.0, (double) totalShips * 10 / (minerals + energy + 1));
        }

        features.setEconomicHealth(creditFlow, upkeepRatio);
    }

    /**
     * Sets diplomatic context (indices 44-45): allies and enemies ratios.
     */
    private void extractDiplomaticContext(GameStateFeatures features, Player player, AiDecisionContext context) {
        if (context == null || context.getEnemyFleets() == null) {
            features.setDiplomaticContext(0.0, 0.0);
            return;
        }

        // Count unique enemy players
        int enemyPlayerCount = 0;
        if (context.getEnemyFleets() != null) {
            for (Fleet enemy : context.getEnemyFleets()) {
                if (enemy.getOwner() != null && enemy.getOwner().getId() != player.getId()) {
                    enemyPlayerCount++;
                }
            }
        }

        // Estimate total players from context
        int totalPlayers = Math.max(2, context.getOwnedFleets() != null ? context.getOwnedFleets().size() + enemyPlayerCount : 2);

        // Allies are estimated as players who are neither self nor enemy
        int allyCount = Math.max(0, totalPlayers - 1 - enemyPlayerCount);

        features.setDiplomaticContext(
                normalize(allyCount, totalPlayers),
                normalize(enemyPlayerCount, totalPlayers)
        );
    }

    /**
     * Sets tactical situation (indices 46-47): active battles and threatened planets.
     */
    private void extractTacticalSituation(GameStateFeatures features, AiDecisionContext context) {
        if (context == null) {
            features.setTacticalSituation(0.0, 0.0);
            return;
        }

        // Count active battles (fleets near enemies)
        int activeBattles = 0;
        if (context.getOwnedFleets() != null && context.getEnemyFleets() != null) {
            for (Fleet fleet : context.getOwnedFleets()) {
                for (Fleet enemy : context.getEnemyFleets()) {
                    if (fleet.getPosition() != null && enemy.getPosition() != null) {
                        int distance = fleet.getPosition().distance(enemy.getPosition());
                        if (distance <= 3) {
                            activeBattles++;
                        }
                    }
                }
            }
        }

        // Count threatened planets (planets near enemy fleets)
        int threatenedPlanets = 0;
        if (context.getEnemyFleets() != null && playerHasPlanets(context)) {
            // Estimate threatened planets based on enemy proximity
            threatenedPlanets = Math.min(context.getEnemyFleets().size(), 5);
        }

        features.setTacticalSituation(
                normalize(activeBattles, 10),
                normalize(threatenedPlanets, MAX_PLANETS)
        );
    }

    /**
     * Sets technology edge (indices 48-49): weapon and shield levels.
     */
    private void extractTechnologyEdge(GameStateFeatures features, Player player) {
        // Estimate technology level from player's research points
        double weaponLevel = 0.0;
        double shieldLevel = 0.0;

        if (player != null) {
            // Use government type as a proxy for tech focus
            Player.GovernmentType gov = player.getGovernmentType();
            if (gov != null) {
                // Different governments have different tech focuses
                switch (gov) {
                    case DEMOCRACY:
                        weaponLevel = 0.5;
                        shieldLevel = 0.6;
                        break;
                    case DICTATORSHIP:
                        weaponLevel = 0.7;
                        shieldLevel = 0.4;
                        break;
                    case THEOCRACY:
                        weaponLevel = 0.4;
                        shieldLevel = 0.7;
                        break;
                    case CORPORATE:
                        weaponLevel = 0.6;
                        shieldLevel = 0.5;
                        break;
                    default:
                        weaponLevel = 0.5;
                        shieldLevel = 0.5;
                }
            }
        }

        features.setTechnologyEdge(
                normalize(weaponLevel, 1.0),
                normalize(shieldLevel, 1.0)
        );
    }

    /**
     * Sets resource pipeline status (indices 50-51): mining and processing rates.
     */
    private void extractResourcePipeline(GameStateFeatures features, Player player) {
        // Estimate mining and processing rates from player's planet count
        int miningRate = 0;
        int processingRate = 0;

        if (player != null) {
            int planetCount = player.getOwnedPlanetIds() != null ? player.getOwnedPlanetIds().size() : 0;
            miningRate = planetCount * 20;  // Estimated mining per planet
            processingRate = planetCount * 15;  // Estimated processing per planet
        }

        features.setResourcePipeline(
                normalize(miningRate, MAX_RESOURCE_RATE),
                normalize(processingRate, MAX_RESOURCE_RATE)
        );
    }

    /**
     * Sets fleet readiness (indices 52-53): average hull and shield percentages.
     */
    private void extractFleetReadiness(GameStateFeatures features, AiDecisionContext context) {
        if (context == null || context.getOwnedFleets() == null || context.getOwnedFleets().isEmpty()) {
            features.setFleetReadiness(1.0, 1.0);
            return;
        }

        double totalHull = 0;
        double totalShield = 0;
        int shipCount = 0;

        for (Fleet fleet : context.getOwnedFleets()) {
            for (Ship ship : fleet.getShips()) {
                if (ship.getShipType() != null) {
                    int maxHull = ship.getShipType().getBaseHullPoints();
                    double hullRatio = maxHull > 0 ? (double) ship.getCurrentHullPoints() / maxHull : 1.0;
                    totalHull += hullRatio;

                    // Estimate shield ratio from ship's shield component
                    double shieldRatio = 1.0;  // Default to full shields
                    totalShield += shieldRatio;

                    shipCount++;
                }
            }
        }

        double avgHull = shipCount > 0 ? totalHull / shipCount : 1.0;
        double avgShield = shipCount > 0 ? totalShield / shipCount : 1.0;

        features.setFleetReadiness(
                normalize(avgHull, 1.0),
                normalize(avgShield, 1.0)
        );
    }

    /**
     * Sets strategic depth (indices 54-55): planets behind front line and supply line integrity.
     */
    private void extractStrategicDepth(GameStateFeatures features, Player player, AiDecisionContext context) {
        if (player == null) {
            features.setStrategicDepth(0.0, 0.0);
            return;
        }

        int planetCount = player.getOwnedPlanetIds() != null ? player.getOwnedPlanetIds().size() : 0;

        // Planets behind front line: estimate based on total planets minus those near enemies
        int frontLinePlanets = 0;
        if (context != null && context.getEnemyFleets() != null) {
            frontLinePlanets = Math.min(planetCount, context.getEnemyFleets().size());
        }
        int behindFront = Math.max(0, planetCount - frontLinePlanets);

        // Supply line integrity: higher when more planets are connected
        double supplyIntegrity = planetCount > 0
                ? (double) behindFront / planetCount
                : 0.0;

        features.setStrategicDepth(
                normalize(behindFront, MAX_PLANETS),
                normalize(supplyIntegrity, 1.0)
        );
    }

    /**
     * Checks if the player has any planets in the context.
     */
    private boolean playerHasPlanets(AiDecisionContext context) {
        // This is a placeholder; actual implementation would check player's planets
        return true;
    }

    /**
     * Normalizes a value to [0, 1] range.
     */
    private double normalize(double value, double max) {
        if (max <= 0) return 0.0;
        return Math.min(1.0, Math.max(0.0, value / max));
    }

    /**
     * Normalizes an integer value to [0, 1] range.
     */
    private double normalize(int value, int max) {
        return normalize((double) value, (double) max);
    }
}
