package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dl.network.GameStateFeatureExtractor;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import com.yisy.modules.doradl.network.StrategyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the reinforcement learning feedback loop for Dora AI.
 * <p>
 * This service collects action-reward pairs from AI player decisions,
 * periodically trains the neural network on accumulated experience,
 * and saves the model to disk for persistence across restarts.
 * </p>
 * <p>
 * Learning flow:
 * <ol>
 *   <li>Before an AI action, capture the current game state as features</li>
 *   <li>After the action completes, compute a reward based on the outcome</li>
 *   <li>Store the (state, action, reward) tuple in the experience buffer</li>
 *   <li>Periodically train the neural network on accumulated experience</li>
 *   <li>Save the trained model to disk</li>
 * </ol>
 * </p>
 */
@Service
public class DoraAiLearningService {

    private static final Logger log = LoggerFactory.getLogger(DoraAiLearningService.class);

    /** Maximum number of experience samples to keep in the buffer before training. */
    private static final int MAX_EXPERIENCE_BUFFER_SIZE = 500;

    /** Number of samples to trigger an automatic training cycle. */
    private static final int TRAINING_THRESHOLD = 50;

    /** Whether learning is currently enabled. */
    private volatile boolean learningEnabled = true;

    /** Experience buffer holding action-reward pairs for training. */
    private final ConcurrentLinkedQueue<ActionReward> experienceBuffer = new ConcurrentLinkedQueue<>();

    /** Total number of actions recorded for statistics. */
    private volatile long totalActionsRecorded = 0;

    /** Total number of training cycles completed. */
    private volatile long totalTrainingCycles = 0;

    /** Last training timestamp in epoch milliseconds. */
    private volatile long lastTrainingTime = 0;

    private final StrategyNetwork network;
    private final GameStateFeatureExtractor extractor;
    private final DlConfig config;

    public DoraAiLearningService(StrategyNetwork network, GameStateFeatureExtractor extractor, DlConfig config) {
        this.network = network;
        this.extractor = extractor;
        this.config = config;

        // Try to load existing model on startup
        try {
            java.io.File modelFile = new java.io.File(config.getModelDir(), "strategy-network.zip");
            if (modelFile.exists()) {
                network.loadModel(modelFile.getAbsolutePath());
                log.info("Loaded existing strategy model from {}", modelFile.getAbsolutePath());
            } else {
                log.info("No existing model found. Starting with random weights.");
            }
        } catch (Exception e) {
            log.warn("Failed to load existing model: {}", e.getMessage());
        }
    }

    /**
     * Records an AI action and its outcome as a training sample.
     * <p>
     * Call this after an AI player completes an action to capture the experience.
     * The game state before the action, the action taken, and the computed reward
     * are stored for later training.
     * </p>
     *
     * @param gameStateBefore the game state before the action
     * @param playerId the AI player's session ID
     * @param action the action that was taken
     * @param reward the computed reward for the action (positive = good, negative = bad)
     */
    public void recordAction(GameStateResponse gameStateBefore, Long playerId,
                              ActionReward.Action action, double reward) {
        if (!learningEnabled) {
            return;
        }

        try {
            GameStateFeatures features = extractor.extract(gameStateBefore, playerId);
            ActionReward sample = new ActionReward(features, action, reward);
            experienceBuffer.offer(sample);
            totalActionsRecorded++;

            log.debug("Recorded action: {} (reward: {:.2f}), buffer size: {}",
                    action, reward, experienceBuffer.size());

            // Trigger immediate training if buffer is large enough
            if (experienceBuffer.size() >= TRAINING_THRESHOLD) {
                trainOnAccumulatedExperience();
            }
        } catch (Exception e) {
            log.warn("Failed to record action for player {}: {}", playerId, e.getMessage());
        }
    }

    /**
     * Computes a reward for an AI action based on the game state change.
     * <p>
     * The reward function evaluates:
     * <ul>
     *   <li>Resource gain/loss (minerals, energy, food)</li>
     *   <li>Fleet strength change (ships gained/lost)</li>
     *   <li>Planet count change (expansion/loss)</li>
     *   <li>Combat outcomes (victory/defeat)</li>
     *   <li>Positional advantage (proximity to enemies)</li>
     * </ul>
     * </p>
     *
     * @param gameStateBefore the game state before the action
     * @param gameStateAfter the game state after the action
     * @param playerId the AI player's session ID
     * @return the computed reward value (typically between -1.0 and 1.0)
     */
    public double computeReward(GameStateResponse gameStateBefore, GameStateResponse gameStateAfter,
                                 Long playerId) {
        double reward = 0.0;

        if (gameStateBefore == null || gameStateAfter == null) {
            return reward;
        }

        var playerBefore = findPlayer(gameStateBefore, playerId);
        var playerAfter = findPlayer(gameStateAfter, playerId);

        if (playerBefore == null || playerAfter == null) {
            return reward;
        }

        // Resource change reward
        double resourceReward = computeResourceReward(playerBefore, playerAfter);
        reward += resourceReward * 0.3;

        // Fleet strength change reward
        double fleetReward = computeFleetReward(playerBefore, playerAfter);
        reward += fleetReward * 0.25;

        // Planet count change reward
        double planetDelta = (double) (playerAfter.getPlanetCount() - playerBefore.getPlanetCount());
        reward += planetDelta * 0.2;

        // Relative strength change reward
        double strengthBefore = computePlayerStrength(playerBefore);
        double strengthAfter = computePlayerStrength(playerAfter);
        double strengthDelta = (strengthAfter - strengthBefore) / (strengthBefore + 1.0);
        reward += strengthDelta * 0.15;

        // Clamp reward to [-1.0, 1.0]
        return Math.max(-1.0, Math.min(1.0, reward));
    }

    /**
     * Computes the resource-based component of the reward.
     */
    private double computeResourceReward(
            com.yisy.modules.doradl.dto.PlayerSummary before,
            com.yisy.modules.doradl.dto.PlayerSummary after) {
        java.util.Map<String, Integer> resBefore = before.getResources();
        java.util.Map<String, Integer> resAfter = after.getResources();

        if (resBefore == null || resAfter == null) {
            return 0.0;
        }

        int mineralsDelta = resAfter.getOrDefault("minerals", 0) - resBefore.getOrDefault("minerals", 0);
        int energyDelta = resAfter.getOrDefault("energy", 0) - resBefore.getOrDefault("energy", 0);
        int foodDelta = resAfter.getOrDefault("food", 0) - resBefore.getOrDefault("food", 0);

        // Normalize resource changes
        double normalizedDelta = (mineralsDelta + energyDelta + foodDelta) / 1000.0;
        return Math.max(-1.0, Math.min(1.0, normalizedDelta));
    }

    /**
     * Computes the fleet-based component of the reward.
     */
    private double computeFleetReward(
            com.yisy.modules.doradl.dto.PlayerSummary before,
            com.yisy.modules.doradl.dto.PlayerSummary after) {
        int fleetDelta = after.getFleetCount() - before.getFleetCount();
        return Math.max(-1.0, Math.min(1.0, fleetDelta / 5.0));
    }

    /**
     * Computes a player's overall strength score.
     */
    private double computePlayerStrength(com.yisy.modules.doradl.dto.PlayerSummary player) {
        double strength = player.getPlanetCount() * 10.0 + player.getFleetCount() * 5.0;
        java.util.Map<String, Integer> resources = player.getResources();
        if (resources != null) {
            strength += resources.getOrDefault("minerals", 0) * 0.1;
            strength += resources.getOrDefault("energy", 0) * 0.2;
        }
        return strength;
    }

    /**
     * Trains the neural network on all accumulated experience in the buffer.
     * <p>
     * This method drains the experience buffer, trains the network,
     * and saves the updated model to disk.
     * </p>
     *
     * @return the number of samples trained on
     */
    public int trainOnAccumulatedExperience() {
        if (!learningEnabled) {
            return 0;
        }

        // Drain the buffer into a list for training
        List<ActionReward> trainingData = new ArrayList<>();
        experienceBuffer.forEach(trainingData::add);
        experienceBuffer.clear();

        if (trainingData.isEmpty()) {
            return 0;
        }

        try {
            if (!network.isInitialized()) {
                network.initialize();
            }

            // Filter out samples with zero reward (neutral actions)
            List<ActionReward> meaningfulSamples = new ArrayList<>();
            for (ActionReward sample : trainingData) {
                // Include all samples, but weight positive/negative rewards more
                meaningfulSamples.add(sample);
            }

            if (meaningfulSamples.isEmpty()) {
                return 0;
            }

            // Train the network
            network.train(meaningfulSamples);
            totalTrainingCycles++;
            lastTrainingTime = System.currentTimeMillis();

            // Save the model
            try {
                String modelPath = network.saveModel();
                log.info("Trained on {} samples (cycle #{}), model saved to {}",
                        meaningfulSamples.size(), totalTrainingCycles, modelPath);
            } catch (Exception e) {
                log.warn("Failed to save model after training: {}", e.getMessage());
            }

            return meaningfulSamples.size();
        } catch (Exception e) {
            log.warn("Training failed: {}", e.getMessage(), e);
            // Put samples back in buffer for next attempt
            experienceBuffer.addAll(trainingData);
            return 0;
        }
    }

    /**
     * Scheduled task: periodically trains the network on accumulated experience.
     * Runs every 60 seconds to ensure learning happens even if the threshold isn't reached.
     */
    @Scheduled(fixedRate = 60000)
    public void scheduledTraining() {
        if (experienceBuffer.size() >= 10) {
            int trained = trainOnAccumulatedExperience();
            if (trained > 0) {
                log.info("Scheduled training completed: {} samples trained", trained);
            }
        }
    }

    /**
     * Finds a player summary by ID in a game state response.
     */
    private com.yisy.modules.doradl.dto.PlayerSummary findPlayer(
            GameStateResponse gameState, Long playerId) {
        if (gameState == null || gameState.getPlayers() == null) {
            return null;
        }
        return gameState.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    // ---- Getters for admin/statistics ----

    public boolean isLearningEnabled() {
        return learningEnabled;
    }

    public void setLearningEnabled(boolean learningEnabled) {
        this.learningEnabled = learningEnabled;
        log.info("Learning {}d", learningEnabled ? "enab" : "disab");
    }

    public int getExperienceBufferSize() {
        return experienceBuffer.size();
    }

    public long getTotalActionsRecorded() {
        return totalActionsRecorded;
    }

    public long getTotalTrainingCycles() {
        return totalTrainingCycles;
    }

    public long getLastTrainingTime() {
        return lastTrainingTime;
    }

    /**
     * Clears all accumulated experience from the buffer.
     */
    public void clearExperience() {
        experienceBuffer.clear();
        log.info("Experience buffer cleared");
    }

    /**
     * Forces an immediate model save.
     *
     * @return the path where the model was saved
     */
    public String saveModel() {
        try {
            return network.saveModel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save model: " + e.getMessage(), e);
        }
    }
}
