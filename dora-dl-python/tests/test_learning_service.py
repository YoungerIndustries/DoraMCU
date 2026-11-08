"""Tests for DoraAiLearningService — experience buffer, rewards, training."""

import os
import tempfile
import pytest
from dora_dl.config import DlConfig
from dora_dl.feature_extractor import GameStateFeatureExtractor
from dora_dl.learning_service import DoraAiLearningService, TRAINING_THRESHOLD
from dora_dl.models import Action
from dora_dl.network import StrategyNetwork


def make_game_state(**overrides):
    """Create a default game state dict for testing."""
    state = {
        "currentTurn": 10,
        "players": [
            {
                "id": 1,
                "name": "Player1",
                "resources": {
                    "minerals": 500,
                    "energy": 300,
                    "food": 100,
                    "research": 50,
                    "influence": 20,
                },
                "planetCount": 3,
                "fleetCount": 2,
                "difficulty": "MEDIUM",
            },
        ],
        "subSectors": [],
    }
    state.update(overrides)
    return state


@pytest.fixture
def tmp_model_dir(tmp_path):
    return str(tmp_path / "models")


@pytest.fixture
def config(tmp_model_dir):
    return DlConfig(model_dir=tmp_model_dir, max_experience_buffer_size=200)


@pytest.fixture
def network(config):
    return StrategyNetwork(config)


@pytest.fixture
def extractor():
    return GameStateFeatureExtractor()


@pytest.fixture
def service(config, network, extractor):
    return DoraAiLearningService(network, extractor, config)


class TestDoraAiLearningService:
    """Test the reinforcement learning service."""

    def test_initial_state(self, service):
        assert service.learning_enabled is True
        assert service.total_actions_recorded == 0
        assert service.total_training_cycles == 0
        assert service.experience_buffer_size == 0

    def test_record_action_increases_buffer(self, service):
        state = make_game_state()
        service.record_action(state, 1, "BUILD_SHIP", 0.5)
        assert service.experience_buffer_size == 1
        assert service.total_actions_recorded == 1

    def test_record_action_disabled(self, service):
        service.learning_enabled = False
        state = make_game_state()
        service.record_action(state, 1, "BUILD_SHIP", 0.5)
        assert service.experience_buffer_size == 0
        assert service.total_actions_recorded == 0

    def test_record_action_invalid_action_name(self, service):
        state = make_game_state()
        # Invalid action name should be caught silently
        service.record_action(state, 1, "INVALID_ACTION", 0.5)
        assert service.experience_buffer_size == 0

    def test_record_action_auto_trains(self, service):
        # Record enough actions to trigger auto-training
        state = make_game_state()
        for i in range(TRAINING_THRESHOLD):
            action = Action.from_index(i % 17)
            service.record_action(state, 1, action.name, 0.5)
        # Should have triggered training
        assert service.total_training_cycles >= 1

    def test_train_on_accumulated_experience(self, service):
        state = make_game_state()
        for i in range(10):
            action = Action.from_index(i % 17)
            service.record_action(state, 1, action.name, 0.5)
        trained = service.train_on_accumulated_experience()
        assert trained == 10
        assert service.total_training_cycles == 1
        assert service.experience_buffer_size == 0

    def test_train_on_empty_buffer(self, service):
        trained = service.train_on_accumulated_experience()
        assert trained == 0

    def test_train_disabled(self, service):
        state = make_game_state()
        service.record_action(state, 1, "BUILD_SHIP", 0.5)
        service.learning_enabled = False
        trained = service.train_on_accumulated_experience()
        assert trained == 0

    def test_clear_experience(self, service):
        state = make_game_state()
        service.record_action(state, 1, "BUILD_SHIP", 0.5)
        service.record_action(state, 1, "ENGAGE_COMBAT", 0.3)
        service.clear_experience()
        assert service.experience_buffer_size == 0

    def test_save_model(self, service, tmp_model_dir):
        service.network.initialize()
        path = service.save_model()
        assert os.path.exists(path)


class TestComputeReward:
    """Test reward computation from before/after game states."""

    @pytest.fixture
    def reward_service(self, config, network, extractor):
        return DoraAiLearningService(network, extractor, config)

    def test_reward_resource_gain(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        after["players"][0]["resources"]["minerals"] = 800  # +300
        after["players"][0]["resources"]["energy"] = 500    # +200
        after["players"][0]["resources"]["food"] = 150      # +50
        reward = reward_service.compute_reward(before, after, 1)
        # Resources increased → positive reward
        assert reward > 0

    def test_reward_resource_loss(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        after["players"][0]["resources"]["minerals"] = 200  # -300
        after["players"][0]["resources"]["energy"] = 100    # -200
        after["players"][0]["resources"]["food"] = 50       # -50
        reward = reward_service.compute_reward(before, after, 1)
        # Resources decreased → negative reward
        assert reward < 0

    def test_reward_fleet_gain(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        after["players"][0]["fleetCount"] = 5  # +3
        reward = reward_service.compute_reward(before, after, 1)
        assert reward > 0

    def test_reward_fleet_loss(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        after["players"][0]["fleetCount"] = 0  # -2
        reward = reward_service.compute_reward(before, after, 1)
        # Fleet loss should reduce reward
        assert reward < 0

    def test_reward_planet_gain(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        after["players"][0]["planetCount"] = 5  # +2
        reward = reward_service.compute_reward(before, after, 1)
        assert reward > 0

    def test_reward_planet_loss(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        after["players"][0]["planetCount"] = 1  # -2
        reward = reward_service.compute_reward(before, after, 1)
        assert reward < 0

    def test_reward_no_change(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        reward = reward_service.compute_reward(before, after, 1)
        assert reward == pytest.approx(0.0, abs=0.01)

    def test_reward_null_before(self, reward_service):
        reward = reward_service.compute_reward(None, make_game_state(), 1)
        assert reward == 0.0

    def test_reward_null_after(self, reward_service):
        reward = reward_service.compute_reward(make_game_state(), None, 1)
        assert reward == 0.0

    def test_reward_nonexistent_player(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        reward = reward_service.compute_reward(before, after, 999)
        assert reward == 0.0

    def test_reward_clamped_range(self, reward_service):
        before = make_game_state()
        after = make_game_state()
        # Extreme changes
        after["players"][0]["resources"]["minerals"] = 100000
        after["players"][0]["resources"]["energy"] = 100000
        after["players"][0]["resources"]["food"] = 100000
        after["players"][0]["fleetCount"] = 100
        after["players"][0]["planetCount"] = 100
        reward = reward_service.compute_reward(before, after, 1)
        assert -1.0 <= reward <= 1.0
