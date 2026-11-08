"""Tests for the PyTorch StrategyNetwork — init, predict, train, save, load."""

import os
import tempfile
import pytest
import torch
import numpy as np
from dora_dl.config import DlConfig
from dora_dl.models import Action, ActionReward, GameStateFeatures, FEATURE_DIMENSION
from dora_dl.network import StrategyNetwork, StrategyNet


class TestStrategyNet:
    """Test the raw PyTorch module."""

    def test_forward_output_shape(self):
        config = DlConfig()
        net = StrategyNet(config)
        x = torch.randn(1, config.input_size)
        output = net(x)
        assert output.shape == (1, config.output_size)

    def test_forward_softmax_sum(self):
        config = DlConfig()
        net = StrategyNet(config)
        net.eval()
        x = torch.randn(1, config.input_size)
        with torch.no_grad():
            logits = net(x)
            # Model outputs raw logits; softmax is applied at inference time
            probs = torch.softmax(logits, dim=1)
        # Softmax output should sum to ~1.0
        assert abs(probs.sum().item() - 1.0) < 1e-5

    def test_forward_non_negative(self):
        config = DlConfig()
        net = StrategyNet(config)
        net.eval()
        x = torch.randn(1, config.input_size)
        with torch.no_grad():
            logits = net(x)
            # Raw logits can be negative; softmax produces non-negative
            probs = torch.softmax(logits, dim=1)
        assert (probs >= 0).all()


class TestStrategyNetwork:
    """Test the high-level StrategyNetwork manager."""

    def test_not_initialized_by_default(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        assert network.is_initialized is False

    def test_initialize_sets_flag(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        assert network.is_initialized is True

    def test_initialize_idempotent(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        first_model = network.model
        network.initialize()
        assert network.model is first_model

    def test_predict_returns_probabilities(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        features = GameStateFeatures()
        probs = network.predict(features)
        assert len(probs) == config.output_size
        # Probabilities should sum to ~1.0 (softmax)
        assert abs(sum(probs) - 1.0) < 0.01
        # All probabilities should be non-negative
        assert all(p >= 0 for p in probs)

    def test_predict_auto_initializes(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        assert network.is_initialized is False
        features = GameStateFeatures()
        network.predict(features)
        assert network.is_initialized is True

    def test_predict_action_returns_valid_index(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        features = GameStateFeatures()
        action_idx = network.predict_action(features)
        assert 0 <= action_idx < config.output_size

    def test_predict_deterministic_with_seed(self):
        config = DlConfig(seed=42)
        network1 = StrategyNetwork(config)
        network2 = StrategyNetwork(config)
        features = GameStateFeatures([1.0] * FEATURE_DIMENSION)
        probs1 = network1.predict(features)
        probs2 = network2.predict(features)
        assert probs1 == probs2

    def test_train_empty_data_no_error(self, caplog):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        # Should not raise; just log a warning
        network.train([])

    def test_train_single_sample(self):
        config = DlConfig(batch_size=1)
        network = StrategyNetwork(config)
        state = GameStateFeatures()
        state.set_resources(100.0, 50.0, 30.0, 20.0, 10.0)
        sample = ActionReward(state, Action.BUILD_SHIP, 1.0)
        network.train([sample])
        # Should not raise

    def test_train_multiple_samples(self):
        config = DlConfig(batch_size=4)
        network = StrategyNetwork(config)
        samples = []
        for i in range(8):
            state = GameStateFeatures([float(j % 100) for j in range(FEATURE_DIMENSION)])
            action = Action.from_index(i % 17)
            samples.append(ActionReward(state, action, 1.0))
        network.train(samples)
        # Should not raise

    def test_train_auto_initializes(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        assert network.is_initialized is False
        state = GameStateFeatures()
        sample = ActionReward(state, Action.BUILD_SHIP, 1.0)
        network.train([sample])
        assert network.is_initialized is True

    def test_save_model_creates_file(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        with tempfile.TemporaryDirectory() as tmpdir:
            config.model_dir = tmpdir
            path = network.save_model()
            assert os.path.exists(path)
            assert path.endswith("strategy-network.pt")

    def test_save_model_creates_directory(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        with tempfile.TemporaryDirectory() as tmpdir:
            nested = os.path.join(tmpdir, "nested", "models")
            config.model_dir = nested
            path = network.save_model()
            assert os.path.exists(path)
            assert os.path.exists(nested)

    def test_save_uninitialized_raises(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        with pytest.raises(RuntimeError, match="Network not initialized"):
            network.save_model()

    def test_load_model(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        with tempfile.TemporaryDirectory() as tmpdir:
            config.model_dir = tmpdir
            path = network.save_model()
            # Load into a new network
            config2 = DlConfig()
            network2 = StrategyNetwork(config2)
            network2.load_model(path)
            assert network2.is_initialized is True

    def test_load_model_preserves_predictions(self):
        config = DlConfig()
        network = StrategyNetwork(config)
        network.initialize()
        features = GameStateFeatures([float(i) for i in range(FEATURE_DIMENSION)])
        probs_before = network.predict(features)
        with tempfile.TemporaryDirectory() as tmpdir:
            config.model_dir = tmpdir
            path = network.save_model()
            config2 = DlConfig()
            network2 = StrategyNetwork(config2)
            network2.load_model(path)
            probs_after = network2.predict(features)
            # Predictions should match after save/load
            assert np.allclose(probs_before, probs_after, atol=1e-6)

    def test_train_changes_predictions(self):
        config = DlConfig(learning_rate=0.1, batch_size=2)
        network = StrategyNetwork(config)
        # Create training data strongly biased toward one action
        samples = []
        for _ in range(20):
            state = GameStateFeatures([1.0] * FEATURE_DIMENSION)
            sample = ActionReward(state, Action.BUILD_SHIP, 1.0)
            samples.append(sample)
        # Train multiple times
        for _ in range(10):
            network.train(samples)
        # Predict with the same features
        features = GameStateFeatures([1.0] * FEATURE_DIMENSION)
        probs = network.predict(features)
        # BUILD_SHIP (index 0) should have highest probability after training
        best = int(np.argmax(probs))
        assert best == Action.BUILD_SHIP.index
