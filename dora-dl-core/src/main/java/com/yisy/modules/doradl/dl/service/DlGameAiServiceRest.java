package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.rest.DlRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * REST-backed implementation of DlGameAiService that delegates to the Python DL service.
 * <p>
 * This service replaces the local DL4J-based DlGameAiService when the Python microservice
 * is available. It calls the Python service via REST for predictions.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "true")
public class DlGameAiServiceRest {

    private static final Logger log = LoggerFactory.getLogger(DlGameAiServiceRest.class);

    private final DlRestClient restClient;
    private final DlConfig config;

    public DlGameAiServiceRest(DlRestClient restClient, DlConfig config) {
        this.restClient = restClient;
        this.config = config;
    }

    public Map<String, Object> predictAction(GameStateResponse gameState, Long playerId) {
        return restClient.predictAction(gameState, playerId);
    }

    public double getBlendFactor() {
        return config.getBlendFactor();
    }

    public void setBlendFactor(double blendFactor) {
        restClient.setBlendFactor(blendFactor);
        config.setBlendFactor(blendFactor);
    }

    public boolean isNetworkReady() {
        return restClient.isServiceAvailable();
    }
}
