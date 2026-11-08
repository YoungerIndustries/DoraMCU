package com.yisy.modules.doradl.rest;

import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dl.network.FeatureExtractor;
import com.yisy.modules.doradl.dl.network.FeatureExtractorRegistry;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * REST client that calls the Python DL service instead of using DL4J directly.
 * <p>
 * When enabled, this client replaces the local StrategyNetwork and DlGameAiService
 * with HTTP calls to the Python microservice running on port 8083.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "true")
public class DlRestClient {

    private static final Logger log = LoggerFactory.getLogger(DlRestClient.class);

    private final RestTemplate restTemplate;
    private final FeatureExtractorRegistry extractorRegistry;

    @Value("${dl.rest-client.base-url:http://localhost:8083}")
    private String baseUrl;

    /** Circuit breaker: tracks consecutive failures. */
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicBoolean circuitOpen = new AtomicBoolean(false);
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_RESET_MS = 30_000;
    private volatile long lastFailureTime = 0;

    public DlRestClient(FeatureExtractorRegistry extractorRegistry) {
        this.restTemplate = new RestTemplate();
        this.extractorRegistry = extractorRegistry;
    }

    /**
     * Startup health check: verify the Python DL service is reachable.
     */
    @PostConstruct
    public void init() {
        if (isServiceAvailable()) {
            log.info("DL Python service is reachable at {}", baseUrl);
        } else {
            log.warn("DL Python service is NOT reachable at {} — predictions will fail until it starts", baseUrl);
        }
    }

    /**
     * Converts a double[] feature array to a List<Double> for JSON serialization.
     */
    private List<Double> toDoubleList(double[] features) {
        List<Double> list = new java.util.ArrayList<>();
        for (double f : features) {
            list.add(f);
        }
        return list;
    }

    /**
     * Executes a REST call with retry logic (3 attempts, exponential backoff)
     * and circuit breaker protection.
     */
    private <T> T withRetry(String operation, Supplier<T> supplier) {
        // Circuit breaker check
        if (circuitOpen.get()) {
            long elapsed = System.currentTimeMillis() - lastFailureTime;
            if (elapsed < CIRCUIT_RESET_MS) {
                log.warn("Circuit breaker OPEN for {}; skipping {}", baseUrl, operation);
                throw new DlServiceException("Circuit breaker open — DL service unavailable", null);
            }
            // Half-open: allow one probe request
            log.debug("Circuit breaker half-open for {}; probing with {}", baseUrl, operation);
            circuitOpen.set(false);
        }

        Throwable lastException = null;
        int maxAttempts = 3;
        long baseDelayMs = 500;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = supplier.get();
                consecutiveFailures.set(0);
                if (attempt > 1) {
                    log.debug("{} succeeded on attempt {}/{}", operation, attempt, maxAttempts);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                if (attempt >= maxAttempts) {
                    break;
                }
                long delay = baseDelayMs * (1L << (attempt - 1)) + (long) (Math.random() * baseDelayMs);
                log.warn("{} failed (attempt {}/{}): {}. Retrying in {}ms...",
                        operation, attempt, maxAttempts, e.getMessage(), delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DlServiceException(operation + " interrupted", ie);
                }
            }
        }

        // Update circuit breaker
        int failures = consecutiveFailures.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitOpen.set(true);
            log.error("Circuit breaker OPEN after {} consecutive failures", failures);
        }

        throw new DlServiceException(operation + " failed after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Predicts the best action for a given game state.
     */
    public Map<String, Object> predictAction(GameStateResponse gameState, Long playerId) {
        FeatureExtractor extractor = extractorRegistry.getDefaultExtractor();
        GameStateFeatures features = extractor.extract(gameState, playerId);
        return withRetry("predictAction", () -> {
            String url = baseUrl + "/api/predict";
            Map<String, Object> body = Map.of(
                    "features", toDoubleList(features.getFeatures()),
                    "playerId", playerId
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            log.debug("DL prediction for player {}: {}", playerId, response.getBody());
            return response.getBody();
        });
    }

    /**
     * Records an action-reward experience for training.
     */
    public void recordAction(GameStateResponse gameState, Long playerId,
                              ActionReward.Action action, double reward) {
        FeatureExtractor extractor = extractorRegistry.getDefaultExtractor();
        GameStateFeatures features = extractor.extract(gameState, playerId);
        withRetry("recordAction", () -> {
            String url = baseUrl + "/api/record";
            Map<String, Object> body = Map.of(
                    "features", toDoubleList(features.getFeatures()),
                    "playerId", playerId,
                    "action", action.name(),
                    "reward", reward
            );
            restTemplate.postForEntity(url, body, Map.class);
            log.debug("Recorded action {} for player {} (reward: {})", action, playerId, reward);
            return null;
        });
    }

    /**
     * Computes reward from before/after game states.
     */
    public double computeReward(GameStateResponse before, GameStateResponse after, Long playerId) {
        FeatureExtractor extractor = extractorRegistry.getDefaultExtractor();
        GameStateFeatures featuresBefore = extractor.extract(before, playerId);
        GameStateFeatures featuresAfter = extractor.extract(after, playerId);
        return withRetry("computeReward", () -> {
            String url = baseUrl + "/api/reward";
            Map<String, Object> body = Map.of(
                    "featuresBefore", toDoubleList(featuresBefore.getFeatures()),
                    "featuresAfter", toDoubleList(featuresAfter.getFeatures()),
                    "playerId", playerId
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            return ((Number) response.getBody().get("reward")).doubleValue();
        });
    }

    /**
     * Triggers training on accumulated experience.
     */
    public int trainOnAccumulatedExperience() {
        return withRetry("trainOnAccumulatedExperience", () -> {
            String url = baseUrl + "/api/train";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return ((Number) response.getBody().get("samplesTrained")).intValue();
        });
    }

    /**
     * Saves the current model.
     */
    public String saveModel() {
        return withRetry("saveModel", () -> {
            String url = baseUrl + "/api/save";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return (String) response.getBody().get("path");
        });
    }

    /**
     * Gets learning status and statistics.
     */
    public Map<String, Object> getLearningStatus() {
        return withRetry("getLearningStatus", () -> {
            String url = baseUrl + "/api/learning";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        });
    }

    /**
     * Enables or disables learning.
     */
    public void setLearningEnabled(boolean enabled) {
        withRetry("setLearningEnabled", () -> {
            String url = baseUrl + "/api/learning?enabled=" + enabled;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            log.info("Learning {}d via REST", enabled ? "enab" : "disab");
            return null;
        });
    }

    /**
     * Clears the experience buffer.
     */
    public void clearExperience() {
        withRetry("clearExperience", () -> {
            String url = baseUrl + "/api/learning/clear";
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);
            log.info("Experience buffer cleared via REST");
            return null;
        });
    }

    /**
     * Updates the blend factor.
     */
    public void setBlendFactor(double blendFactor) {
        withRetry("setBlendFactor", () -> {
            String url = baseUrl + "/api/blend";
            Map<String, Object> body = Map.of("blendFactor", blendFactor);
            restTemplate.put(url, body);
            log.info("Blend factor set to {} via REST", blendFactor);
            return null;
        });
    }

    /**
     * Checks if the Python DL service is reachable.
     */
    public boolean isServiceAvailable() {
        String url = baseUrl + "/api/status";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("DL service unavailable: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Exception thrown when the DL REST service call fails.
     */
    public static class DlServiceException extends RuntimeException {
        public DlServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
