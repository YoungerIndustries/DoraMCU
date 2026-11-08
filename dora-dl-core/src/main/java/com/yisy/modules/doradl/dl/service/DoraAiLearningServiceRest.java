package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.rest.DlRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * REST-backed implementation of DoraAiLearningService that delegates to the Python DL service.
 * <p>
 * This service replaces the local DL4J-based learning service when the Python microservice
 * is available. All experience recording, training, and model management is handled
 * by the Python service.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "true")
public class DoraAiLearningServiceRest {

    private static final Logger log = LoggerFactory.getLogger(DoraAiLearningServiceRest.class);

    private final DlRestClient restClient;

    public DoraAiLearningServiceRest(DlRestClient restClient) {
        this.restClient = restClient;
    }

    public void recordAction(GameStateResponse gameStateBefore, Long playerId,
                              ActionReward.Action action, double reward) {
        restClient.recordAction(gameStateBefore, playerId, action, reward);
    }

    public double computeReward(GameStateResponse gameStateBefore, GameStateResponse gameStateAfter,
                                 Long playerId) {
        return restClient.computeReward(gameStateBefore, gameStateAfter, playerId);
    }

    public int trainOnAccumulatedExperience() {
        return restClient.trainOnAccumulatedExperience();
    }

    public String saveModel() {
        return restClient.saveModel();
    }

    public boolean isLearningEnabled() {
        try {
            var status = restClient.getLearningStatus();
            return (boolean) status.getOrDefault("learningEnabled", true);
        } catch (Exception e) {
            return true;
        }
    }

    public void setLearningEnabled(boolean enabled) {
        restClient.setLearningEnabled(enabled);
    }

    public int getExperienceBufferSize() {
        try {
            var status = restClient.getLearningStatus();
            return ((Number) status.getOrDefault("experienceBufferSize", 0)).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public long getTotalActionsRecorded() {
        try {
            var status = restClient.getLearningStatus();
            return ((Number) status.getOrDefault("totalActionsRecorded", 0)).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public long getTotalTrainingCycles() {
        try {
            var status = restClient.getLearningStatus();
            return ((Number) status.getOrDefault("totalTrainingCycles", 0)).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public long getLastTrainingTime() {
        try {
            var status = restClient.getLearningStatus();
            Object last = status.get("lastTrainingTime");
            if (last instanceof Number) {
                return ((Number) last).longValue();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void clearExperience() {
        restClient.clearExperience();
    }
}
