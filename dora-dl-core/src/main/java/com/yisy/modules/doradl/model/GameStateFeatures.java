package com.yisy.modules.doradl.model;

import java.util.Arrays;

/**
 * Represents a 64-dimensional feature vector extracted from a game state.
 * Used as input to the neural network for strategy prediction.
 * <p>
 * The first 32 dimensions are the original global game-state features.
 * Dimensions 32-63 are empire-specific features that encode personality,
 * government/ethos, research level, fleet composition, station coverage,
 * economic health, diplomatic context, tactical situation, technology edge,
 * resource pipeline, fleet readiness, and strategic depth.
 * </p>
 * 
 * Feature layout:
 * - [0-4]: Player resources (minerals, energy, food, research, influence) — normalized
 * - [5-7]: Player assets (planet count, fleet count, total ship count) — normalized
 * - [8-10]: Fleet positions (nearest fleet x, y, z) — normalized
 * - [11-13]: Enemy info (nearest enemy distance, enemy fleet count, enemy total ships) — normalized
 * - [14-16]: Game state (current turn, number of players, player rank) — normalized
 * - [17-18]: Relative strength (player vs avg enemy, player vs nearest enemy)
 * - [19-20]: Resource trends (minerals delta, energy delta) — normalized
 * - [21-23]: Strategic context (has adjacent enemy, has buildable resources, has idle fleet)
 * - [24]: Difficulty bias (easy=0.0, medium=0.5, hard=1.0)
 * - [25-26]: Turn phase (early, mid, late) — one-hot encoded
 * - [27-31]: Reserved (zeros)
 * - [32-33]: Personality (aggression, expansion drive)
 * - [34-35]: Government/Ethos encoding (government index/7, ethos index/5)
 * - [36-37]: Research level (normalized 0-1), AI core level (normalized 0-1)
 * - [38-39]: Fleet composition (fighter ratio, capital ship ratio)
 * - [40-41]: Station coverage (military stations normalized, research stations normalized)
 * - [42-43]: Economic health (credit flow normalized, upkeep ratio)
 * - [44-45]: Diplomatic context (allies count/players, enemies count/players)
 * - [46-47]: Tactical situation (active battles, threatened planets)
 * - [48-49]: Technology edge (weapon level normalized, shield level normalized)
 * - [50-51]: Resource pipeline status (mining rate normalized, processing rate normalized)
 * - [52-53]: Fleet readiness (average hull%, average shield%)
 * - [54-55]: Strategic depth (planets behind front line, supply line integrity)
 * - [56-63]: Reserved for future expansion (zeros)
 */
public class GameStateFeatures {

    public static final int FEATURE_DIMENSION = 64;
    public static final int ORIGINAL_DIMENSION = 32;
    public static final int EMPIRE_FEATURE_START = 32;

    private double[] features;

    public GameStateFeatures() {
        this.features = new double[FEATURE_DIMENSION];
    }

    public GameStateFeatures(double[] features) {
        if (features.length != FEATURE_DIMENSION) {
            throw new IllegalArgumentException(
                "Feature vector must have exactly " + FEATURE_DIMENSION + " dimensions, got " + features.length);
        }
        this.features = features.clone();
    }

    public double[] getFeatures() {
        return features.clone();
    }

    public void setFeatures(double[] features) {
        if (features.length != FEATURE_DIMENSION) {
            throw new IllegalArgumentException(
                "Feature vector must have exactly " + FEATURE_DIMENSION + " dimensions, got " + features.length);
        }
        System.arraycopy(features, 0, this.features, 0, FEATURE_DIMENSION);
    }

    public double getFeature(int index) {
        if (index < 0 || index >= FEATURE_DIMENSION) {
            throw new IndexOutOfBoundsException("Feature index " + index + " out of range [0, " + FEATURE_DIMENSION + ")");
        }
        return features[index];
    }

    public void setFeature(int index, double value) {
        if (index < 0 || index >= FEATURE_DIMENSION) {
            throw new IndexOutOfBoundsException("Feature index " + index + " out of range [0, " + FEATURE_DIMENSION + ")");
        }
        features[index] = value;
    }

    /**
     * Sets the player resources (indices 0-4).
     * Values should be normalized to [0, 1].
     */
    public void setResources(double minerals, double energy, double food, double research, double influence) {
        features[0] = minerals;
        features[1] = energy;
        features[2] = food;
        features[3] = research;
        features[4] = influence;
    }

    /**
     * Sets the player assets (indices 5-7).
     * Values should be normalized to [0, 1].
     */
    public void setAssets(double planetCount, double fleetCount, double totalShipCount) {
        features[5] = planetCount;
        features[6] = fleetCount;
        features[7] = totalShipCount;
    }

    /**
     * Sets the fleet positions (indices 8-10).
     * Values should be normalized to [0, 1].
     */
    public void setFleetPosition(double x, double y, double z) {
        features[8] = x;
        features[9] = y;
        features[10] = z;
    }

    /**
     * Sets the enemy info (indices 11-13).
     * Values should be normalized to [0, 1].
     */
    public void setEnemyInfo(double nearestDistance, double enemyFleetCount, double enemyTotalShips) {
        features[11] = nearestDistance;
        features[12] = enemyFleetCount;
        features[13] = enemyTotalShips;
    }

    /**
     * Sets the game state (indices 14-16).
     * Values should be normalized to [0, 1].
     */
    public void setGameState(double currentTurn, double playerCount, double playerRank) {
        features[14] = currentTurn;
        features[15] = playerCount;
        features[16] = playerRank;
    }

    /**
     * Sets the relative strength (indices 17-18).
     */
    public void setRelativeStrength(double vsAverageEnemy, double vsNearestEnemy) {
        features[17] = vsAverageEnemy;
        features[18] = vsNearestEnemy;
    }

    /**
     * Sets the resource trends (indices 19-20).
     */
    public void setResourceTrends(double mineralsDelta, double energyDelta) {
        features[19] = mineralsDelta;
        features[20] = energyDelta;
    }

    /**
     * Sets the strategic context (indices 21-23).
     * Values are binary: 0.0 or 1.0.
     */
    public void setStrategicContext(double hasAdjacentEnemy, double hasBuildableResources, double hasIdleFleet) {
        features[21] = hasAdjacentEnemy;
        features[22] = hasBuildableResources;
        features[23] = hasIdleFleet;
    }

    /**
     * Sets the difficulty bias (index 24).
     * easy=0.0, medium=0.5, hard=1.0
     */
    public void setDifficultyBias(double difficulty) {
        features[24] = difficulty;
    }

    /**
     * Sets the turn phase (indices 25-26).
     * One-hot encoded: early=[1,0], mid=[0,1], late=[0,0]
     */
    public void setTurnPhase(boolean isEarly, boolean isMid) {
        features[25] = isEarly ? 1.0 : 0.0;
        features[26] = isMid ? 1.0 : 0.0;
    }

    // ---- Empire-Specific Features (indices 32-63) ----

    /**
     * Sets the personality features (indices 32-33).
     * Values should be normalized to [0, 1].
     */
    public void setPersonality(double aggression, double expansionDrive) {
        features[32] = aggression;
        features[33] = expansionDrive;
    }

    /**
     * Sets the government/ethos encoding (indices 34-35).
     * Values should be normalized to [0, 1].
     */
    public void setGovernmentEthos(double governmentIndex, double ethosIndex) {
        features[34] = governmentIndex;
        features[35] = ethosIndex;
    }

    /**
     * Sets the research level and AI core level (indices 36-37).
     * Values should be normalized to [0, 1].
     */
    public void setResearchLevel(double researchLevel, double aiCoreLevel) {
        features[36] = researchLevel;
        features[37] = aiCoreLevel;
    }

    /**
     * Sets the fleet composition (indices 38-39).
     * Values should be normalized to [0, 1].
     */
    public void setFleetComposition(double fighterRatio, double capitalShipRatio) {
        features[38] = fighterRatio;
        features[39] = capitalShipRatio;
    }

    /**
     * Sets the station coverage (indices 40-41).
     * Values should be normalized to [0, 1].
     */
    public void setStationCoverage(double militaryStations, double researchStations) {
        features[40] = militaryStations;
        features[41] = researchStations;
    }

    /**
     * Sets the economic health (indices 42-43).
     * Values should be normalized to [0, 1].
     */
    public void setEconomicHealth(double creditFlow, double upkeepRatio) {
        features[42] = creditFlow;
        features[43] = upkeepRatio;
    }

    /**
     * Sets the diplomatic context (indices 44-45).
     * Values should be normalized to [0, 1].
     */
    public void setDiplomaticContext(double alliesRatio, double enemiesRatio) {
        features[44] = alliesRatio;
        features[45] = enemiesRatio;
    }

    /**
     * Sets the tactical situation (indices 46-47).
     * Values should be normalized to [0, 1].
     */
    public void setTacticalSituation(double activeBattles, double threatenedPlanets) {
        features[46] = activeBattles;
        features[47] = threatenedPlanets;
    }

    /**
     * Sets the technology edge (indices 48-49).
     * Values should be normalized to [0, 1].
     */
    public void setTechnologyEdge(double weaponLevel, double shieldLevel) {
        features[48] = weaponLevel;
        features[49] = shieldLevel;
    }

    /**
     * Sets the resource pipeline status (indices 50-51).
     * Values should be normalized to [0, 1].
     */
    public void setResourcePipeline(double miningRate, double processingRate) {
        features[50] = miningRate;
        features[51] = processingRate;
    }

    /**
     * Sets the fleet readiness (indices 52-53).
     * Values should be normalized to [0, 1].
     */
    public void setFleetReadiness(double averageHull, double averageShield) {
        features[52] = averageHull;
        features[53] = averageShield;
    }

    /**
     * Sets the strategic depth (indices 54-55).
     * Values should be normalized to [0, 1].
     */
    public void setStrategicDepth(double planetsBehindFront, double supplyLineIntegrity) {
        features[54] = planetsBehindFront;
        features[55] = supplyLineIntegrity;
    }

    /**
     * Resets all features to zero.
     */
    public void clear() {
        Arrays.fill(features, 0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameStateFeatures that = (GameStateFeatures) o;
        return Arrays.equals(features, that.features);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(features);
    }

    @Override
    public String toString() {
        return "GameStateFeatures{" +
                "features=" + Arrays.toString(features) +
                '}';
    }
}
