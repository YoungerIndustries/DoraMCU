package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dl.network.FeatureExtractor;
import com.yisy.modules.doradl.dl.network.FeatureExtractorRegistry;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import com.yisy.modules.doradl.monitor.MemoryMonitor;
import com.yisy.modules.doradl.monitor.MemoryMonitor.MemoryLevel;
import com.yisy.modules.doradl.network.StrategyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the reinforcement learning feedback loop for Dora AI.
 * <p>
 * This is the local DL4J-based implementation. When dl.rest-client.enabled=true,
 * the REST-backed implementation (DoraAiLearningServiceRest) is used instead.
 * </p>
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
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "false", matchIfMissing = true)
public class DoraAiLearningService {

    private static final Logger log = LoggerFactory.getLogger(DoraAiLearningService.class);

    /** Maximum number of experience samples to keep in the buffer — hard cap to prevent unbounded growth. */
    private static final int MAX_EXPERIENCE_BUFFER_SIZE = 200;

    /** Number of samples to trigger an automatic training cycle. */
    private static final int TRAINING_THRESHOLD = 50;

    /** Maximum batch size for training to avoid OOM errors. */
    private static final int MAX_TRAINING_BATCH_SIZE = 16;

    /** Minimum batch size when memory is critical. */
    private static final int MIN_BATCH_SIZE = 1;

    /** Circuit breaker: skip training if last training threw OOM. */
    private volatile boolean trainingCircuitOpen = false;

    /** Timestamp when circuit breaker was opened (epoch ms). */
    private volatile long circuitOpenedAt = 0;

    /** Circuit breaker cooldown in milliseconds (5 minutes). */
    private static final long CIRCUIT_COOLDOWN_MS = 5 * 60 * 1000;

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
    private final FeatureExtractorRegistry extractorRegistry;
    private final DlConfig config;
    private final MemoryMonitor memoryMonitor;

    public DoraAiLearningService(StrategyNetwork network, FeatureExtractorRegistry extractorRegistry, DlConfig config, MemoryMonitor memoryMonitor) {
        this.network = network;
        this.extractorRegistry = extractorRegistry;
        this.config = config;
        this.memoryMonitor = memoryMonitor;

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
            log.warn("Failed to load existing model: {}", e.getMessage(), e);
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
        recordAction(gameStateBefore, playerId, action, reward,
                FeatureExtractorRegistry.SupportedGameType.DORAMCU_4X);
    }

    /**
     * Records an AI action and its outcome as a training sample, with explicit game type.
     *
     * @param gameStateBefore the game state before the action
     * @param playerId the AI player's session ID
     * @param action the action that was taken
     * @param reward the computed reward for the action (positive = good, negative = bad)
     * @param gameType the game type for feature extraction
     */
    public void recordAction(GameStateResponse gameStateBefore, Long playerId,
                              ActionReward.Action action, double reward,
                              FeatureExtractorRegistry.SupportedGameType gameType) {
        if (!learningEnabled) {
            return;
        }

        // Drop oldest samples if buffer is at capacity — prevents unbounded growth
        if (experienceBuffer.size() >= MAX_EXPERIENCE_BUFFER_SIZE) {
            experienceBuffer.poll();
            log.debug("Experience buffer full — dropped oldest sample to make room");
        }

        try {
            FeatureExtractor extractor = extractorRegistry.getExtractor(gameType);
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
            log.warn("Failed to record action for player {}: {}", playerId, e.getMessage(), e);
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
     * This method drains the experience buffer, checks memory pressure,
     * trains the network with memory-aware batch sizing,
     * and saves the updated model to disk.
     * </p>
     *
     * @return the number of samples trained on
     */
    public int trainOnAccumulatedExperience() {
        if (!learningEnabled) {
            return 0;
        }

        // Check memory pressure before training
        MemoryLevel memoryLevel = memoryMonitor.getMemoryLevel();
        if (memoryLevel == MemoryLevel.CRITICAL) {
            log.warn("Skipping training — memory level is CRITICAL (heap: {:.1f}%, JavaCPP: {:.1f}%)",
                    memoryMonitor.getHeapUsageFraction() * 100,
                    memoryMonitor.getJavacppUsageFraction() * 100);
            return 0;
        }

        // Force GC if memory is elevated
        if (memoryLevel == MemoryLevel.WARNING) {
            memoryMonitor.forceGcAndLog();
            // Re-check after GC
            memoryLevel = memoryMonitor.getMemoryLevel();
            if (memoryLevel == MemoryLevel.CRITICAL) {
                log.warn("Skipping training — memory still CRITICAL after GC");
                return 0;
            }
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

            // Train the network with memory-aware batch sizing
            network.train(meaningfulSamples, memoryMonitor);
            totalTrainingCycles++;
            lastTrainingTime = System.currentTimeMillis();

            // Save the model
            try {
                String modelPath = network.saveModel();
                log.info("Trained on {} samples (cycle #{}), model saved to {}",
                        meaningfulSamples.size(), totalTrainingCycles, modelPath);
            } catch (Exception e) {
                log.warn("Failed to save model after training: {}", e.getMessage(), e);
            }

            return meaningfulSamples.size();
        } catch (OutOfMemoryError e) {
            log.error("OutOfMemoryError during training - opening circuit breaker for {}ms. Buffer size: {}",
                    CIRCUIT_COOLDOWN_MS, trainingData.size(), e);
            trainingCircuitOpen = true;
            circuitOpenedAt = System.currentTimeMillis();
            // Force GC on OOM
            memoryMonitor.forceGcAndLog();
            // Put samples back in buffer for next attempt
            for (ActionReward sample : trainingData) {
                if (experienceBuffer.size() < MAX_EXPERIENCE_BUFFER_SIZE) {
                    experienceBuffer.offer(sample);
                }
            }
            return 0;
        } catch (Exception e) {
            log.warn("Training failed: {}", e.getMessage(), e);
            // Put samples back in buffer for next attempt
            for (ActionReward sample : trainingData) {
                if (experienceBuffer.size() < MAX_EXPERIENCE_BUFFER_SIZE) {
                    experienceBuffer.offer(sample);
                }
            }
            return 0;
        }
    }

    /**
     * Scheduled task: periodically trains the network on accumulated experience.
     * Runs every 60 seconds to ensure learning happens even if the threshold isn't reached.
     */
    @Scheduled(fixedRate = 60000)
    public void scheduledTraining() {
        // Check circuit breaker
        if (trainingCircuitOpen) {
            long elapsed = System.currentTimeMillis() - circuitOpenedAt;
            if (elapsed < CIRCUIT_COOLDOWN_MS) {
                log.debug("Training circuit breaker open - skipping training ({}ms remaining)",
                        CIRCUIT_COOLDOWN_MS - elapsed);
                return;
            }
            log.info("Training circuit breaker cooldown expired - attempting training");
            trainingCircuitOpen = false;
        }

        // Log memory state periodically
        memoryMonitor.checkAndWarn();

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
