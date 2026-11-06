package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doradl.model.ActionReward;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiTrainingDataGenerator Tests")
class AiTrainingDataGeneratorTest {

    @Test
    @DisplayName("should generate samples for all 17 actions")
    void generateAllActions() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(10);

        Map<ActionReward.Action, Long> actionCounts = samples.stream()
                .collect(Collectors.groupingBy(ActionReward::getAction, Collectors.counting()));

        assertEquals(17, actionCounts.size(), "Should have samples for all 17 actions");
        for (ActionReward.Action action : ActionReward.Action.values()) {
            assertTrue(actionCounts.containsKey(action), "Missing samples for action: " + action);
            assertEquals(10, actionCounts.get(action), "Should have 10 samples for " + action);
        }
    }

    @Test
    @DisplayName("should generate correct total samples")
    void generateTotalSamples() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(5);

        assertEquals(17 * 5, samples.size(), "Should have 17 actions × 5 samples");
    }

    @Test
    @DisplayName("should generate valid feature vectors")
    void validFeatureVectors() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(5);

        for (ActionReward sample : samples) {
            double[] features = sample.getState().getFeatures();
            assertEquals(64, features.length, "Feature vector should have 64 dimensions");
        }
    }

    @Test
    @DisplayName("should generate valid rewards")
    void validRewards() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(5);

        for (ActionReward sample : samples) {
            assertTrue(sample.getReward() > 0.0 && sample.getReward() <= 1.0,
                    "Reward should be in (0, 1] range");
        }
    }

    @Test
    @DisplayName("should generate valid one-hot vectors")
    void validOneHot() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(5);

        for (ActionReward sample : samples) {
            double[] oneHot = sample.toOneHot();
            assertEquals(17, oneHot.length, "One-hot vector should have 17 dimensions");

            int ones = 0;
            for (double val : oneHot) {
                if (val == 1.0) ones++;
            }
            assertEquals(1, ones, "One-hot vector should have exactly one 1.0");
        }
    }

    @Test
    @DisplayName("should produce reproducible results with same seed")
    void reproducibleResults() {
        AiTrainingDataGenerator gen1 = new AiTrainingDataGenerator();
        AiTrainingDataGenerator gen2 = new AiTrainingDataGenerator();

        List<ActionReward> samples1 = gen1.generate(10);
        List<ActionReward> samples2 = gen2.generate(10);

        assertEquals(samples1.size(), samples2.size());
        for (int i = 0; i < samples1.size(); i++) {
            assertEquals(samples1.get(i).getAction(), samples2.get(i).getAction());
        }
    }

    @Test
    @DisplayName("should generate zero samples when count is zero")
    void zeroSamples() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(0);

        assertTrue(samples.isEmpty());
    }

    @Test
    @DisplayName("combat samples should have high aggression")
    void combatSamplesHaveHighAggression() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(20);

        List<ActionReward> combatSamples = samples.stream()
                .filter(s -> s.getAction() == ActionReward.Action.ENGAGE_COMBAT)
                .collect(Collectors.toList());

        for (ActionReward sample : combatSamples) {
            double aggression = sample.getState().getFeature(32);
            assertTrue(aggression >= 0.7, "Combat samples should have high aggression");
        }
    }

    @Test
    @DisplayName("retreat samples should have low relative strength")
    void retreatSamplesHaveLowStrength() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(20);

        List<ActionReward> retreatSamples = samples.stream()
                .filter(s -> s.getAction() == ActionReward.Action.RETREAT)
                .collect(Collectors.toList());

        for (ActionReward sample : retreatSamples) {
            double strength = sample.getState().getFeature(17);
            assertTrue(strength <= 0.4, "Retreat samples should have low relative strength");
        }
    }

    @Test
    @DisplayName("wait samples should have low resources")
    void waitSamplesHaveLowResources() {
        AiTrainingDataGenerator generator = new AiTrainingDataGenerator();
        List<ActionReward> samples = generator.generate(20);

        List<ActionReward> waitSamples = samples.stream()
                .filter(s -> s.getAction() == ActionReward.Action.WAIT)
                .collect(Collectors.toList());

        for (ActionReward sample : waitSamples) {
            double minerals = sample.getState().getFeature(0);
            assertTrue(minerals <= 0.25, "Wait samples should have low minerals");
        }
    }
}
