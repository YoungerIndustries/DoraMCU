package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dto.PlayerSummary;
import com.yisy.modules.doradl.dl.network.GameStateFeatureExtractor;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import com.yisy.modules.doradl.network.StrategyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Combines neural network predictions with heuristic scores for game AI decisions.
 */
@Service
public class DlGameAiService {

    private static final Logger log = LoggerFactory.getLogger(DlGameAiService.class);

    private final StrategyNetwork network;
    private final GameStateFeatureExtractor extractor;
    private final DlConfig config;

    public DlGameAiService(StrategyNetwork network, GameStateFeatureExtractor extractor, DlConfig config) {
        this.network = network;
        this.extractor = extractor;
        this.config = config;
    }

    public Map<String, Object> predictAction(GameStateResponse gameState, Long playerId) {
        if (!network.isInitialized()) {
            network.initialize();
        }

        GameStateFeatures features = extractor.extract(gameState, playerId);
        double[] nnProbabilities = network.predict(features);
        int nnAction = network.predictAction(features);

        int actionCount = ActionReward.Action.values().length;
        double[] heuristicScores = calculateHeuristicScores(gameState, playerId, actionCount);
        int heuristicAction = argmax(heuristicScores);

        double blendFactor = config.getBlendFactor();
        double[] blendedScores = new double[actionCount];
        for (int i = 0; i < actionCount; i++) {
            blendedScores[i] = blendFactor * nnProbabilities[i] + (1.0 - blendFactor) * heuristicScores[i];
        }

        int bestAction = argmax(blendedScores);
        ActionReward.Action action = ActionReward.Action.fromIndex(bestAction);

        log.debug("DL AI prediction for player {}: {} (NN: {}, Heuristic: {}, Blend: {})",
                playerId, action,
                ActionReward.Action.fromIndex(nnAction),
                ActionReward.Action.fromIndex(heuristicAction),
                blendFactor);

        return Map.of(
                "action", action.name(),
                "actionIndex", bestAction,
                "actionCategory", action.getCategory().name(),
                "confidence", blendedScores[bestAction],
                "nnProbabilities", nnProbabilities,
                "heuristicScores", heuristicScores,
                "blendedScores", blendedScores,
                "blendFactor", blendFactor
        );
    }

    private double[] calculateHeuristicScores(GameStateResponse gameState, Long playerId, int actionCount) {
        double[] scores = new double[actionCount];
        int waitIndex = ActionReward.Action.WAIT.getIndex();

        if (gameState == null || gameState.getPlayers() == null) {
            scores[waitIndex] = 1.0;
            return scores;
        }

        PlayerSummary player = gameState.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
        if (player == null) {
            scores[waitIndex] = 1.0;
            return scores;
        }

        Map<String, Integer> resources = player.getResources();
        if (resources == null) {
            scores[waitIndex] = 1.0;
            return scores;
        }

        int minerals = resources.getOrDefault("minerals", 0);
        int energy = resources.getOrDefault("energy", 0);
        int food = resources.getOrDefault("food", 0);
        int research = resources.getOrDefault("research", 0);
        int fleetCount = player.getFleetCount();
        int planetCount = player.getPlanetCount();

        // Strategic actions
        if (minerals >= 100 && energy >= 50) scores[ActionReward.Action.BUILD_SHIP.getIndex()] = 0.7;
        if (minerals >= 200 && energy >= 100) scores[ActionReward.Action.BUILD_STATION.getIndex()] = 0.5;
        if (planetCount < 5 && food >= 30) scores[ActionReward.Action.EXPAND_COLONY.getIndex()] = 0.6;
        if (research >= 50) scores[ActionReward.Action.RESEARCH_TECH.getIndex()] = 0.5;
        scores[ActionReward.Action.DIPLOMATIC_STANCE.getIndex()] = 0.2;

        // Tactical actions
        if (fleetCount > 0) scores[ActionReward.Action.MOVE_FLEET.getIndex()] = 0.5;
        if (fleetCount >= 3) scores[ActionReward.Action.ENGAGE_COMBAT.getIndex()] = 0.6;
        if (fleetCount > 0 && minerals < 30) scores[ActionReward.Action.RETREAT.getIndex()] = 0.7;
        if (fleetCount >= 2) scores[ActionReward.Action.FLANK_ENEMY.getIndex()] = 0.3;
        if (fleetCount >= 2) scores[ActionReward.Action.AMBUSH.getIndex()] = 0.2;

        // Economic actions
        if (minerals < 100) scores[ActionReward.Action.OPTIMIZE_MINING.getIndex()] = 0.6;
        scores[ActionReward.Action.OPTIMIZE_TRADE.getIndex()] = 0.3;
        if (fleetCount > 0) scores[ActionReward.Action.REPAIR_FLEET.getIndex()] = 0.3;
        scores[ActionReward.Action.UPGRADE_STATION.getIndex()] = 0.2;

        // Defensive actions
        if (minerals >= 50) scores[ActionReward.Action.FORTIFY_PLANET.getIndex()] = 0.4;
        if (fleetCount > 0) scores[ActionReward.Action.SCOUT_AREA.getIndex()] = 0.4;

        // Wait action
        if (minerals < 50 || energy < 25) scores[waitIndex] = 0.8;
        else scores[waitIndex] = 0.1;

        // Normalize
        double sum = 0;
        for (double s : scores) sum += s;
        if (sum > 0) {
            for (int i = 0; i < scores.length; i++) scores[i] /= sum;
        }
        return scores;
    }

    private int argmax(double[] array) {
        int maxIndex = 0;
        double maxVal = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public double getBlendFactor() {
        return config.getBlendFactor();
    }

    public void setBlendFactor(double blendFactor) {
        config.setBlendFactor(Math.max(0.0, Math.min(1.0, blendFactor)));
    }

    public boolean isNetworkReady() {
        return network.isInitialized();
    }
}
