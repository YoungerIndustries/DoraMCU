package com.yisy.modules.doradl.dl.service;

import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionReward and GameStateFeatures models.
 */
class ActionRewardTest {

    @Test
    void testActionRewardCreation() {
        GameStateFeatures features = new GameStateFeatures(new double[GameStateFeatures.FEATURE_DIMENSION]);
        ActionReward sample = new ActionReward(features, ActionReward.Action.BUILD_SHIP, 0.75);

        assertEquals(ActionReward.Action.BUILD_SHIP, sample.getAction());
        assertEquals(0.75, sample.getReward(), 0.001);
        assertNotNull(sample.getState());
    }

    @Test
    void testActionRewardSetters() {
        ActionReward sample = new ActionReward();
        sample.setAction(ActionReward.Action.ENGAGE_COMBAT);
        sample.setReward(-0.5);

        assertEquals(ActionReward.Action.ENGAGE_COMBAT, sample.getAction());
        assertEquals(-0.5, sample.getReward(), 0.001);
    }

    @Test
    void testActionRewardEqualsAndHashCode() {
        GameStateFeatures features = new GameStateFeatures(new double[GameStateFeatures.FEATURE_DIMENSION]);
        ActionReward a = new ActionReward(features, ActionReward.Action.BUILD_SHIP, 0.5);
        ActionReward b = new ActionReward(features, ActionReward.Action.BUILD_SHIP, 0.5);
        ActionReward c = new ActionReward(features, ActionReward.Action.ENGAGE_COMBAT, 0.5);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void testGameStateFeaturesDimension() {
        assertEquals(64, GameStateFeatures.FEATURE_DIMENSION);
        assertEquals(32, GameStateFeatures.ORIGINAL_DIMENSION);
        assertEquals(32, GameStateFeatures.EMPIRE_FEATURE_START);
    }

    @Test
    void testGameStateFeaturesDefaultConstructor() {
        GameStateFeatures features = new GameStateFeatures();
        double[] arr = features.getFeatures();
        assertEquals(64, arr.length);
        for (double v : arr) {
            assertEquals(0.0, v, 0.001);
        }
    }

    @Test
    void testGameStateFeaturesInvalidDimension() {
        assertThrows(IllegalArgumentException.class, () -> new GameStateFeatures(new double[32]));
        assertThrows(IllegalArgumentException.class, () -> {
            GameStateFeatures f = new GameStateFeatures();
            f.setFeatures(new double[32]);
        });
    }

    @Test
    void testGameStateFeaturesGetSet() {
        GameStateFeatures features = new GameStateFeatures();
        features.setFeature(0, 0.5);
        features.setFeature(63, 1.0);

        assertEquals(0.5, features.getFeature(0), 0.001);
        assertEquals(1.0, features.getFeature(63), 0.001);
    }

    @Test
    void testGameStateFeaturesIndexOutOfBounds() {
        GameStateFeatures features = new GameStateFeatures();
        assertThrows(IndexOutOfBoundsException.class, () -> features.getFeature(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> features.getFeature(64));
        assertThrows(IndexOutOfBoundsException.class, () -> features.setFeature(-1, 0.0));
        assertThrows(IndexOutOfBoundsException.class, () -> features.setFeature(64, 0.0));
    }

    @Test
    void testGameStateFeaturesClear() {
        GameStateFeatures features = new GameStateFeatures();
        features.setFeature(0, 0.5);
        features.setFeature(32, 1.0);
        features.clear();

        for (int i = 0; i < 64; i++) {
            assertEquals(0.0, features.getFeature(i), 0.001);
        }
    }

    @Test
    void testGameStateFeaturesDomainSetters() {
        GameStateFeatures features = new GameStateFeatures();

        features.setResources(0.5, 0.3, 0.8, 0.2, 0.1);
        assertEquals(0.5, features.getFeature(0), 0.001);
        assertEquals(0.3, features.getFeature(1), 0.001);
        assertEquals(0.8, features.getFeature(2), 0.001);
        assertEquals(0.2, features.getFeature(3), 0.001);
        assertEquals(0.1, features.getFeature(4), 0.001);

        features.setAssets(0.4, 0.6, 0.7);
        assertEquals(0.4, features.getFeature(5), 0.001);
        assertEquals(0.6, features.getFeature(6), 0.001);
        assertEquals(0.7, features.getFeature(7), 0.001);

        features.setPersonality(0.8, 0.3);
        assertEquals(0.8, features.getFeature(32), 0.001);
        assertEquals(0.3, features.getFeature(33), 0.001);
    }

    @Test
    void testGameStateFeaturesCloneSafety() {
        double[] original = new double[64];
        original[0] = 0.5;
        GameStateFeatures features = new GameStateFeatures(original);

        // Modifying original should not affect features
        original[0] = 0.0;
        assertEquals(0.5, features.getFeature(0), 0.001);

        // getFeatures returns a clone
        double[] retrieved = features.getFeatures();
        retrieved[0] = 0.0;
        assertEquals(0.5, features.getFeature(0), 0.001);
    }
}
