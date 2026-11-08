"""Tests for DlGameAiService — blending NN predictions with heuristic scores."""

import pytest
from dora_dl.config import DlConfig
from dora_dl.dl_game_ai_service import DlGameAiService
from dora_dl.feature_extractor import GameStateFeatureExtractor
from dora_dl.models import Action, FEATURE_DIMENSION
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
def config():
    return DlConfig()


@pytest.fixture
def network(config):
    return StrategyNetwork(config)


@pytest.fixture
def extractor():
    return GameStateFeatureExtractor()


@pytest.fixture
def service(config, network, extractor):
    return DlGameAiService(network, extractor, config)


class TestDlGameAiService:
    """Test the blended prediction service."""

    def test_predict_action_returns_dict(self, service):
        state = make_game_state()
        result = service.predict_action(state, 1)
        assert isinstance(result, dict)

    def test_predict_action_has_required_fields(self, service):
        state = make_game_state()
        result = service.predict_action(state, 1)
        assert "action" in result
        assert "actionIndex" in result
        assert "actionCategory" in result
        assert "confidence" in result
        assert "nnProbabilities" in result
        assert "heuristicScores" in result
        assert "blendedScores" in result
        assert "blendFactor" in result

    def test_predict_action_valid_action_index(self, service):
        state = make_game_state()
        result = service.predict_action(state, 1)
        assert 0 <= result["actionIndex"] < 17

    def test_predict_action_valid_category(self, service):
        state = make_game_state()
        result = service.predict_action(state, 1)
        assert result["actionCategory"] in ("STRATEGIC", "TACTICAL", "ECONOMIC", "DEFENSIVE")

    def test_predict_action_probabilities_sum(self, service):
        state = make_game_state()
        result = service.predict_action(state, 1)
        nn_probs = result["nnProbabilities"]
        assert abs(sum(nn_probs) - 1.0) < 0.01

    def test_predict_action_blended_scores_length(self, service):
        state = make_game_state()
        result = service.predict_action(state, 1)
        assert len(result["blendedScores"]) == 17
        assert len(result["heuristicScores"]) == 17

    def test_predict_action_blend_factor_reflected(self, service):
        state = make_game_state()
        service.blend_factor = 0.8
        result = service.predict_action(state, 1)
        assert result["blendFactor"] == 0.8

    def test_blend_factor_property(self, service):
        assert service.blend_factor == 0.5
        service.blend_factor = 0.7
        assert service.blend_factor == 0.7

    def test_blend_factor_clamped(self, service):
        service.blend_factor = 2.0
        assert service.blend_factor == 1.0
        service.blend_factor = -0.5
        assert service.blend_factor == 0.0

    def test_is_network_ready(self, service):
        assert service.is_network_ready is False
        service.network.initialize()
        assert service.is_network_ready is True

    def test_predict_with_pure_heuristic(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        result = service.predict_action(state, 1)
        # With blend=0, blended scores should equal heuristic scores
        for i in range(17):
            assert result["blendedScores"][i] == pytest.approx(
                result["heuristicScores"][i], abs=1e-6
            )

    def test_predict_with_pure_nn(self, service):
        service.blend_factor = 1.0
        state = make_game_state()
        result = service.predict_action(state, 1)
        # With blend=1, blended scores should equal NN probabilities
        for i in range(17):
            assert result["blendedScores"][i] == pytest.approx(
                result["nnProbabilities"][i], abs=1e-6
            )

    def test_predict_empty_state_returns_wait(self, service):
        state = {"currentTurn": 0, "players": [], "subSectors": []}
        result = service.predict_action(state, 1)
        # With no players, heuristic should default to WAIT
        assert result["action"] == "WAIT"

    def test_predict_nonexistent_player_returns_wait(self, service):
        state = make_game_state()
        result = service.predict_action(state, 999)
        assert result["action"] == "WAIT"

    def test_heuristic_build_ship_with_resources(self, service):
        service.blend_factor = 0.0  # Pure heuristic
        state = make_game_state()
        # Player has minerals=500 >= 100 and energy=300 >= 50
        result = service.predict_action(state, 1)
        # BUILD_SHIP should have a non-zero heuristic score
        assert result["heuristicScores"][Action.BUILD_SHIP.index] > 0

    def test_heuristic_wait_with_low_resources(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["resources"]["minerals"] = 10  # < 50
        players[0]["resources"]["energy"] = 10   # < 25
        result = service.predict_action(state, 1)
        # WAIT should have highest score
        assert result["action"] == "WAIT"

    def test_heuristic_expand_colony(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["planetCount"] = 1  # < 5
        players[0]["resources"]["food"] = 100  # >= 30
        result = service.predict_action(state, 1)
        assert result["heuristicScores"][Action.EXPAND_COLONY.index] > 0

    def test_heuristic_engage_combat(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["fleetCount"] = 5  # >= 3
        result = service.predict_action(state, 1)
        assert result["heuristicScores"][Action.ENGAGE_COMBAT.index] > 0

    def test_heuristic_research_tech(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["resources"]["research"] = 100  # >= 50
        result = service.predict_action(state, 1)
        assert result["heuristicScores"][Action.RESEARCH_TECH.index] > 0

    def test_heuristic_optimize_mining_low_minerals(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["resources"]["minerals"] = 50  # < 100
        result = service.predict_action(state, 1)
        assert result["heuristicScores"][Action.OPTIMIZE_MINING.index] > 0

    def test_heuristic_fortify_planet(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["resources"]["minerals"] = 200  # >= 50
        result = service.predict_action(state, 1)
        assert result["heuristicScores"][Action.FORTIFY_PLANET.index] > 0

    def test_heuristic_retreat_low_minerals(self, service):
        service.blend_factor = 0.0
        state = make_game_state()
        players = state["players"]
        players[0]["fleetCount"] = 2  # > 0
        players[0]["resources"]["minerals"] = 20  # < 30
        result = service.predict_action(state, 1)
        assert result["heuristicScores"][Action.RETREAT.index] > 0
