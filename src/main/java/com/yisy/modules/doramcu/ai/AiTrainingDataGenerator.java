package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic training data for the strategy neural network.
 * <p>
 * Creates labeled game state scenarios covering all 17 action types across
 * diverse personality combinations, resource levels, and tactical situations.
 * Each sample is a {@link com.yisy.modules.doradl.model.ActionReward} containing
 * a 64-dimensional feature vector and the correct action label.
 * </p>
 * <p>
 * The labeling uses heuristic rules that mirror the AI core's decision logic:
 * aggressive personalities with strong fleets should engage combat,
 * economic personalities with resources should build ships, etc.
 * </p>
 */
public class AiTrainingDataGenerator {

    private final Random random;

    public AiTrainingDataGenerator() {
        this(new Random(42));
    }

    public AiTrainingDataGenerator(Random random) {
        this.random = random;
    }

    /**
     * Generates a complete training dataset.
     *
     * @param samplesPerScenario number of samples per scenario type
     * @return list of labeled training samples
     */
    public List<com.yisy.modules.doradl.model.ActionReward> generate(int samplesPerScenario) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();

        // Strategic scenarios
        samples.addAll(generateBuildShipScenarios(samplesPerScenario));
        samples.addAll(generateBuildStationScenarios(samplesPerScenario));
        samples.addAll(generateExpandColonyScenarios(samplesPerScenario));
        samples.addAll(generateResearchTechScenarios(samplesPerScenario));
        samples.addAll(generateDiplomaticStanceScenarios(samplesPerScenario));

        // Tactical scenarios
        samples.addAll(generateMoveFleetScenarios(samplesPerScenario));
        samples.addAll(generateEngageCombatScenarios(samplesPerScenario));
        samples.addAll(generateRetreatScenarios(samplesPerScenario));
        samples.addAll(generateFlankEnemyScenarios(samplesPerScenario));
        samples.addAll(generateAmbushScenarios(samplesPerScenario));

        // Economic scenarios
        samples.addAll(generateOptimizeMiningScenarios(samplesPerScenario));
        samples.addAll(generateOptimizeTradeScenarios(samplesPerScenario));
        samples.addAll(generateRepairFleetScenarios(samplesPerScenario));
        samples.addAll(generateUpgradeStationScenarios(samplesPerScenario));

        // Defensive scenarios
        samples.addAll(generateFortifyPlanetScenarios(samplesPerScenario));
        samples.addAll(generateScoutAreaScenarios(samplesPerScenario));
        samples.addAll(generateWaitScenarios(samplesPerScenario));

        return samples;
    }

    /**
     * Generates scenarios where BUILD_SHIP is the optimal action.
     * High economic focus, sufficient resources, low fleet count.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateBuildShipScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.5 + random.nextDouble() * 0.4,  // minerals: 0.5-0.9
                    0.4 + random.nextDouble() * 0.4,  // energy: 0.4-0.8
                    0.3 + random.nextDouble() * 0.3,  // food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setAssets(
                    0.3 + random.nextDouble() * 0.3,  // planets
                    0.1 + random.nextDouble() * 0.2,  // few fleets
                    0.1 + random.nextDouble() * 0.2   // few ships
            );
            features.setPersonality(
                    0.3 + random.nextDouble() * 0.4,  // moderate aggression
                    0.3 + random.nextDouble() * 0.3   // moderate expansion
            );
            features.setEconomicHealth(
                    0.5 + random.nextDouble() * 0.4,  // good credit flow
                    0.2 + random.nextDouble() * 0.3   // low upkeep
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy
                    1.0,  // has buildable resources
                    random.nextDouble()               // maybe idle fleet
            );
            double reward = 0.7 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.BUILD_SHIP,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where BUILD_STATION is the optimal action.
     * High economic focus, low aggression, sufficient resources.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateBuildStationScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.6 + random.nextDouble() * 0.3,  // minerals
                    0.5 + random.nextDouble() * 0.3,  // energy
                    0.3 + random.nextDouble() * 0.3,  // food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setPersonality(
                    0.1 + random.nextDouble() * 0.3,  // low aggression
                    0.2 + random.nextDouble() * 0.3   // low-moderate expansion
            );
            features.setEconomicHealth(
                    0.6 + random.nextDouble() * 0.3,  // good credit flow
                    0.2 + random.nextDouble() * 0.3   // low upkeep
            );
            features.setStationCoverage(
                    0.0 + random.nextDouble() * 0.2,  // few military stations
                    0.0 + random.nextDouble() * 0.2   // few research stations
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy
                    1.0,  // has buildable resources
                    random.nextDouble()
            );
            double reward = 0.6 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.BUILD_STATION,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where EXPAND_COLONY is the optimal action.
     * High expansion drive, moderate resources.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateExpandColonyScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.4 + random.nextDouble() * 0.4,  // minerals
                    0.3 + random.nextDouble() * 0.4,  // energy
                    0.3 + random.nextDouble() * 0.3,  // food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setPersonality(
                    0.2 + random.nextDouble() * 0.4,  // moderate aggression
                    0.7 + random.nextDouble() * 0.3   // high expansion drive
            );
            features.setAssets(
                    0.2 + random.nextDouble() * 0.3,  // few planets
                    0.3 + random.nextDouble() * 0.3,  // some fleets
                    0.3 + random.nextDouble() * 0.3   // some ships
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy
                    0.5 + random.nextDouble() * 0.4,  // has buildable resources
                    1.0   // has idle fleet for expansion
            );
            double reward = 0.6 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.EXPAND_COLONY,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where RESEARCH_TECH is the optimal action.
     * High scientific focus, surplus resources.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateResearchTechScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.7 + random.nextDouble() * 0.2,  // surplus minerals
                    0.6 + random.nextDouble() * 0.3,  // surplus energy
                    0.5 + random.nextDouble() * 0.3,  // surplus food
                    0.3 + random.nextDouble() * 0.3,  // some research
                    random.nextDouble() * 0.2          // influence
            );
            features.setPersonality(
                    0.1 + random.nextDouble() * 0.3,  // low aggression
                    0.2 + random.nextDouble() * 0.3   // low expansion
            );
            features.setResearchLevel(
                    0.3 + random.nextDouble() * 0.3,  // moderate research level
                    random.nextDouble() * 0.5         // AI core level
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy
                    0.5 + random.nextDouble() * 0.4,  // has buildable resources
                    random.nextDouble()
            );
            double reward = 0.5 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.RESEARCH_TECH,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where DIPLOMATIC_STANCE is the optimal action.
     * High diplomatic stance, multiple enemies or allies.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateDiplomaticStanceScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.1 + random.nextDouble() * 0.2,  // low aggression
                    0.2 + random.nextDouble() * 0.3   // low expansion
            );
            features.setDiplomaticContext(
                    0.2 + random.nextDouble() * 0.3,  // some allies
                    0.3 + random.nextDouble() * 0.4   // some enemies
            );
            features.setStrategicContext(
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble()
            );
            double reward = 0.4 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.DIPLOMATIC_STANCE,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where MOVE_FLEET is the optimal action.
     * Moderate aggression, enemy at distance.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateMoveFleetScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.4 + random.nextDouble() * 0.4,  // moderate-high aggression
                    0.5 + random.nextDouble() * 0.4   // high expansion
            );
            features.setEnemyInfo(
                    0.5 + random.nextDouble() * 0.4,  // enemy at medium distance
                    0.3 + random.nextDouble() * 0.4,  // some enemy fleets
                    0.3 + random.nextDouble() * 0.4   // some enemy ships
            );
            features.setRelativeStrength(
                    0.5 + random.nextDouble() * 0.4,  // comparable strength
                    0.5 + random.nextDouble() * 0.4   // comparable to nearest
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy (not yet)
                    random.nextDouble(),
                    1.0   // has idle fleet to move
            );
            double reward = 0.6 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.MOVE_FLEET,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where ENGAGE_COMBAT is the optimal action.
     * High aggression, strong fleet, enemy adjacent.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateEngageCombatScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.7 + random.nextDouble() * 0.3,  // high aggression
                    0.4 + random.nextDouble() * 0.4   // moderate expansion
            );
            features.setEnemyInfo(
                    0.0 + random.nextDouble() * 0.15, // enemy very close
                    0.2 + random.nextDouble() * 0.4,  // some enemy fleets
                    0.2 + random.nextDouble() * 0.4   // some enemy ships
            );
            features.setRelativeStrength(
                    0.7 + random.nextDouble() * 0.3,  // stronger than enemy
                    0.7 + random.nextDouble() * 0.3   // stronger than nearest
            );
            features.setStrategicContext(
                    1.0,  // has adjacent enemy
                    random.nextDouble(),
                    random.nextDouble()
            );
            features.setFleetReadiness(
                    0.7 + random.nextDouble() * 0.3,  // high hull
                    0.6 + random.nextDouble() * 0.3   // high shield
            );
            double reward = 0.8 + random.nextDouble() * 0.2;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.ENGAGE_COMBAT,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where RETREAT is the optimal action.
     * Low risk tolerance, weak fleet, strong enemy adjacent.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateRetreatScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.1 + random.nextDouble() * 0.3,  // low aggression
                    0.2 + random.nextDouble() * 0.3   // low expansion
            );
            features.setEnemyInfo(
                    0.0 + random.nextDouble() * 0.15, // enemy very close
                    0.5 + random.nextDouble() * 0.4,  // many enemy fleets
                    0.6 + random.nextDouble() * 0.3   // many enemy ships
            );
            features.setRelativeStrength(
                    0.1 + random.nextDouble() * 0.3,  // weaker than enemy
                    0.1 + random.nextDouble() * 0.3   // weaker than nearest
            );
            features.setStrategicContext(
                    1.0,  // has adjacent enemy
                    random.nextDouble(),
                    random.nextDouble()
            );
            features.setFleetReadiness(
                    0.3 + random.nextDouble() * 0.3,  // low hull
                    0.2 + random.nextDouble() * 0.3   // low shield
            );
            double reward = 0.7 + random.nextDouble() * 0.2;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.RETREAT,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where FLANK_ENEMY is the optimal action.
     * High risk tolerance, moderate strength, enemy at medium distance.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateFlankEnemyScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.6 + random.nextDouble() * 0.3,  // high aggression
                    0.5 + random.nextDouble() * 0.3   // moderate expansion
            );
            features.setEnemyInfo(
                    0.3 + random.nextDouble() * 0.3,  // enemy at medium distance
                    0.3 + random.nextDouble() * 0.3,  // some enemy fleets
                    0.3 + random.nextDouble() * 0.3   // some enemy ships
            );
            features.setRelativeStrength(
                    0.6 + random.nextDouble() * 0.3,  // comparable or stronger
                    0.5 + random.nextDouble() * 0.3   // comparable to nearest
            );
            features.setResearchLevel(
                    0.5 + random.nextDouble() * 0.4,  // high research for flanking
                    random.nextDouble()
            );
            double reward = 0.6 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.FLANK_ENEMY,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where AMBUSH is the optimal action.
     * High risk tolerance, defensive posture, enemy approaching.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateAmbushScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.5 + random.nextDouble() * 0.4,  // moderate-high aggression
                    0.3 + random.nextDouble() * 0.3   // moderate expansion
            );
            features.setEnemyInfo(
                    0.2 + random.nextDouble() * 0.3,  // enemy approaching
                    0.2 + random.nextDouble() * 0.3,  // some enemy fleets
                    0.2 + random.nextDouble() * 0.3   // some enemy ships
            );
            features.setRelativeStrength(
                    0.5 + random.nextDouble() * 0.3,  // comparable strength
                    0.5 + random.nextDouble() * 0.3   // comparable to nearest
            );
            features.setResearchLevel(
                    0.6 + random.nextDouble() * 0.3,  // high research for ambush
                    random.nextDouble()
            );
            double reward = 0.6 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.AMBUSH,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where OPTIMIZE_MINING is the optimal action.
     * High economic focus, low minerals, good mining infrastructure.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateOptimizeMiningScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.1 + random.nextDouble() * 0.2,  // low minerals
                    0.4 + random.nextDouble() * 0.3,  // moderate energy
                    0.3 + random.nextDouble() * 0.3,  // moderate food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setPersonality(
                    0.2 + random.nextDouble() * 0.3,  // low aggression
                    0.3 + random.nextDouble() * 0.3   // moderate expansion
            );
            features.setEconomicHealth(
                    0.3 + random.nextDouble() * 0.3,  // low credit flow
                    0.4 + random.nextDouble() * 0.3   // moderate upkeep
            );
            features.setResourcePipeline(
                    0.3 + random.nextDouble() * 0.3,  // low mining rate
                    random.nextDouble() * 0.5         // moderate processing
            );
            double reward = 0.5 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.OPTIMIZE_MINING,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where OPTIMIZE_TRADE is the optimal action.
     * High economic focus, surplus resources, good diplomatic context.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateOptimizeTradeScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.7 + random.nextDouble() * 0.2,  // surplus minerals
                    0.6 + random.nextDouble() * 0.3,  // surplus energy
                    0.5 + random.nextDouble() * 0.3,  // surplus food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setPersonality(
                    0.2 + random.nextDouble() * 0.3,  // low aggression
                    0.3 + random.nextDouble() * 0.3   // moderate expansion
            );
            features.setDiplomaticContext(
                    0.4 + random.nextDouble() * 0.4,  // many allies for trade
                    0.1 + random.nextDouble() * 0.2   // few enemies
            );
            double reward = 0.5 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.OPTIMIZE_TRADE,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where REPAIR_FLEET is the optimal action.
     * Low fleet readiness, no immediate threat.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateRepairFleetScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.4 + random.nextDouble() * 0.3,  // moderate minerals
                    0.4 + random.nextDouble() * 0.3,  // moderate energy
                    0.3 + random.nextDouble() * 0.3,  // moderate food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setFleetReadiness(
                    0.1 + random.nextDouble() * 0.2,  // very low hull
                    0.1 + random.nextDouble() * 0.2   // very low shield
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy
                    random.nextDouble(),
                    random.nextDouble()
            );
            double reward = 0.7 + random.nextDouble() * 0.2;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.REPAIR_FLEET,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where UPGRADE_STATION is the optimal action.
     * High scientific focus, stations without AI cores.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateUpgradeStationScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.5 + random.nextDouble() * 0.3,  // moderate minerals
                    0.4 + random.nextDouble() * 0.3,  // moderate energy
                    0.3 + random.nextDouble() * 0.3,  // moderate food
                    random.nextDouble() * 0.3,         // research
                    random.nextDouble() * 0.2          // influence
            );
            features.setPersonality(
                    0.2 + random.nextDouble() * 0.3,  // low aggression
                    0.3 + random.nextDouble() * 0.3   // moderate expansion
            );
            features.setResearchLevel(
                    0.4 + random.nextDouble() * 0.4,  // moderate research
                    0.2 + random.nextDouble() * 0.3   // low AI core level
            );
            features.setStationCoverage(
                    0.3 + random.nextDouble() * 0.3,  // some military stations
                    0.3 + random.nextDouble() * 0.3   // some research stations
            );
            double reward = 0.5 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.UPGRADE_STATION,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where FORTIFY_PLANET is the optimal action.
     * Low aggression, threatened planets, defensive posture.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateFortifyPlanetScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.1 + random.nextDouble() * 0.2,  // low aggression
                    0.2 + random.nextDouble() * 0.3   // low expansion
            );
            features.setTacticalSituation(
                    random.nextDouble() * 0.3,         // few active battles
                    0.5 + random.nextDouble() * 0.4   // threatened planets
            );
            features.setStrategicContext(
                    1.0,  // has adjacent enemy
                    0.5 + random.nextDouble() * 0.4,  // has buildable resources
                    random.nextDouble()
            );
            double reward = 0.6 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.FORTIFY_PLANET,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where SCOUT_AREA is the optimal action.
     * High expansion drive, unknown areas, idle fleets.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateScoutAreaScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setPersonality(
                    0.3 + random.nextDouble() * 0.4,  // moderate aggression
                    0.7 + random.nextDouble() * 0.3   // high expansion drive
            );
            features.setEnemyInfo(
                    0.8 + random.nextDouble() * 0.2,  // enemy far away
                    0.1 + random.nextDouble() * 0.2,  // few enemy fleets
                    0.1 + random.nextDouble() * 0.2   // few enemy ships
            );
            features.setStrategicContext(
                    0.0,  // no adjacent enemy
                    random.nextDouble(),
                    1.0   // has idle fleet
            );
            double reward = 0.5 + random.nextDouble() * 0.3;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.SCOUT_AREA,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Generates scenarios where WAIT is the optimal action.
     * Low resources, no clear action, or low risk tolerance.
     */
    private List<com.yisy.modules.doradl.model.ActionReward> generateWaitScenarios(int count) {
        List<com.yisy.modules.doradl.model.ActionReward> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.yisy.modules.doradl.model.GameStateFeatures features = createFeatures();
            features.setResources(
                    0.1 + random.nextDouble() * 0.15, // very low minerals
                    0.1 + random.nextDouble() * 0.15, // very low energy
                    0.1 + random.nextDouble() * 0.15, // very low food
                    random.nextDouble() * 0.1,         // low research
                    random.nextDouble() * 0.1          // low influence
            );
            features.setPersonality(
                    0.1 + random.nextDouble() * 0.2,  // low aggression
                    0.1 + random.nextDouble() * 0.2   // low expansion
            );
            features.setStrategicContext(
                    random.nextDouble(),
                    0.0 + random.nextDouble() * 0.1,  // no buildable resources
                    random.nextDouble()
            );
            double reward = 0.3 + random.nextDouble() * 0.2;
            samples.add(new com.yisy.modules.doradl.model.ActionReward(
                    features,
                    com.yisy.modules.doradl.model.ActionReward.Action.WAIT,
                    reward
            ));
        }
        return samples;
    }

    /**
     * Creates a fresh GameStateFeatures instance with default values.
     */
    private com.yisy.modules.doradl.model.GameStateFeatures createFeatures() {
        com.yisy.modules.doradl.model.GameStateFeatures features =
                new com.yisy.modules.doradl.model.GameStateFeatures();
        // Set default game state
        features.setGameState(
                random.nextDouble() * 0.5,  // current turn (normalized)
                0.3 + random.nextDouble() * 0.2, // player count
                0.3 + random.nextDouble() * 0.2  // player rank
        );
        features.setDifficultyBias(0.5); // medium difficulty
        features.setTurnPhase(
                random.nextDouble() > 0.5,  // isEarly
                random.nextDouble() > 0.5   // isMid
        );
        return features;
    }
}
