package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic training data for the AI strategy neural network.
 * <p>
 * Creates ActionReward samples with deterministic feature vectors based on
 * the action type, ensuring each action has characteristic feature patterns
 * (e.g., combat actions have high aggression, retreat actions have low strength).
 * </p>
 */
public class AiTrainingDataGenerator {

    private static final int FEATURE_DIMENSIONS = 64;
    private final Random random;

    /**
     * Creates a new generator with a fixed seed for reproducible results.
     */
    public AiTrainingDataGenerator() {
        this.random = new Random(42);
    }

    /**
     * Generates training samples for all actions.
     *
     * @param samplesPerAction the number of samples to generate per action
     * @return a list of ActionReward samples (actions × samplesPerAction)
     */
    public List<ActionReward> generate(int samplesPerAction) {
        if (samplesPerAction <= 0) {
            return new ArrayList<>();
        }

        List<ActionReward> samples = new ArrayList<>();
        ActionReward.Action[] actions = ActionReward.Action.values();

        for (ActionReward.Action action : actions) {
            for (int i = 0; i < samplesPerAction; i++) {
                samples.add(generateSample(action));
            }
        }

        return samples;
    }

    private ActionReward generateSample(ActionReward.Action action) {
        double[] features = new double[FEATURE_DIMENSIONS];
        generateFeaturesForAction(action, features);

        GameStateFeatures state = new GameStateFeatures(features);
        double reward = 0.1 + random.nextDouble() * 0.9; // (0.1, 1.0]
        return new ActionReward(state, action, reward);
    }

    private void generateFeaturesForAction(ActionReward.Action action, double[] features) {
        // Fill base features with random values
        for (int i = 0; i < FEATURE_DIMENSIONS; i++) {
            features[i] = random.nextDouble();
        }

        // Set action-specific characteristic features
        switch (action) {
            case ENGAGE_COMBAT:
                // Feature 32 = aggression (high for combat)
                features[32] = 0.7 + random.nextDouble() * 0.3;
                // Feature 17 = relative strength (moderate to high)
                features[17] = 0.5 + random.nextDouble() * 0.5;
                break;

            case RETREAT:
                // Feature 17 = relative strength (low for retreat)
                features[17] = random.nextDouble() * 0.4;
                break;

            case WAIT:
                // Feature 0 = minerals (low for wait)
                features[0] = random.nextDouble() * 0.25;
                break;

            case BUILD_SHIP:
                // Feature 0 = minerals (high for building)
                features[0] = 0.5 + random.nextDouble() * 0.5;
                // Feature 1 = energy (high for building)
                features[1] = 0.5 + random.nextDouble() * 0.5;
                break;

            case SCOUT_AREA:
                // Feature 33 = exploration drive (high for scouting)
                features[33] = 0.6 + random.nextDouble() * 0.4;
                break;

            case RESEARCH_TECH:
                // Feature 34 = scientific focus (high for research)
                features[34] = 0.6 + random.nextDouble() * 0.4;
                break;

            case DIPLOMATIC_STANCE:
                // Feature 35 = diplomatic stance (high for diplomacy)
                features[35] = 0.6 + random.nextDouble() * 0.4;
                break;

            default:
                // Other actions get random features
                break;
        }
    }
}
