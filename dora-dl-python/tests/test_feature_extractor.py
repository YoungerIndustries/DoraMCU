"""Tests for GameStateFeatureExtractor — converting game state dicts to feature vectors."""

import pytest
from dora_dl.feature_extractor import (
    GameStateFeatureExtractor,
    _normalize,
    _estimate_total_ships,
    _calculate_player_power,
    _calculate_player_rank,
    _find_player,
)
from dora_dl.models import FEATURE_DIMENSION


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
            {
                "id": 2,
                "name": "Player2",
                "resources": {
                    "minerals": 400,
                    "energy": 200,
                    "food": 80,
                    "research": 30,
                    "influence": 10,
                },
                "planetCount": 2,
                "fleetCount": 1,
                "difficulty": "EASY",
            },
        ],
        "subSectors": [],
    }
    state.update(overrides)
    return state


class TestNormalize:
    """Test the normalization helper."""

    def test_normalize_within_range(self):
        assert _normalize(500, 1000) == 0.5

    def test_normalize_clamped_high(self):
        assert _normalize(1500, 1000) == 1.0

    def test_normalize_clamped_low(self):
        assert _normalize(-100, 1000) == 0.0

    def test_normalize_zero(self):
        assert _normalize(0, 1000) == 0.0

    def test_normalize_exact_max(self):
        assert _normalize(1000, 1000) == 1.0


class TestEstimateTotalShips:
    """Test ship estimation from player resources."""

    def test_estimates_from_resources(self):
        player = {
            "resources": {"minerals": 500, "energy": 200},
            "fleetCount": 1,
        }
        ships = _estimate_total_ships(player)
        # min(500//100, 200//50) = min(5, 4) = 4
        assert ships == 4.0

    def test_estimates_fallback_to_fleet_count(self):
        player = {
            "resources": {"minerals": 10, "energy": 5},
            "fleetCount": 5,
        }
        ships = _estimate_total_ships(player)
        # min(0, 0) = 0, max(0, 5) = 5
        assert ships == 5.0


class TestCalculatePlayerPower:
    """Test player power calculation."""

    def test_power_includes_all_components(self):
        player = {
            "resources": {"minerals": 100, "energy": 50, "food": 30},
            "planetCount": 2,
            "fleetCount": 3,
        }
        power = _calculate_player_power(player)
        expected = 100 + 50 * 2 + 30 + 2 * 100 + 3 * 50
        assert power == expected

    def test_power_zero_for_empty_player(self):
        player = {
            "resources": {},
            "planetCount": 0,
            "fleetCount": 0,
        }
        power = _calculate_player_power(player)
        assert power == 0


class TestCalculatePlayerRank:
    """Test player rank calculation."""

    def test_rank_first(self):
        players = [
            {"resources": {"minerals": 500, "energy": 300, "food": 100}, "planetCount": 5, "fleetCount": 3},
            {"resources": {"minerals": 100, "energy": 50, "food": 20}, "planetCount": 1, "fleetCount": 0},
        ]
        rank = _calculate_player_rank(players, players[0])
        assert rank == 1

    def test_rank_second(self):
        players = [
            {"resources": {"minerals": 500, "energy": 300, "food": 100}, "planetCount": 5, "fleetCount": 3},
            {"resources": {"minerals": 100, "energy": 50, "food": 20}, "planetCount": 1, "fleetCount": 0},
        ]
        rank = _calculate_player_rank(players, players[1])
        assert rank == 2


class TestFindPlayer:
    """Test player lookup helper."""

    def test_find_existing_player(self):
        players = [{"id": 1}, {"id": 2}]
        found = _find_player(players, 2)
        assert found["id"] == 2

    def test_find_nonexistent_player(self):
        players = [{"id": 1}, {"id": 2}]
        found = _find_player(players, 99)
        assert found is None

    def test_find_in_empty_list(self):
        found = _find_player([], 1)
        assert found is None

    def test_find_in_none(self):
        found = _find_player(None, 1)
        assert found is None


class TestGameStateFeatureExtractor:
    """Test the full feature extraction pipeline."""

    def setup_method(self):
        self.extractor = GameStateFeatureExtractor()

    def test_extract_returns_features(self):
        state = make_game_state()
        features = self.extractor.extract(state, 1)
        assert features is not None
        assert len(features.features) == FEATURE_DIMENSION

    def test_extract_null_state_raises(self):
        with pytest.raises(ValueError, match="Game state cannot be null"):
            self.extractor.extract(None, 1)

    def test_extract_null_player_id_raises(self):
        with pytest.raises(ValueError, match="Player ID cannot be null"):
            self.extractor.extract({}, None)

    def test_extract_nonexistent_player_returns_zeros(self):
        state = make_game_state()
        features = self.extractor.extract(state, 999)
        assert all(f == 0.0 for f in features.features)

    def test_extract_resources_normalized(self):
        state = make_game_state()
        features = self.extractor.extract(state, 1)
        # Minerals: 500 / 10000 = 0.05
        assert features.get_feature(0) == pytest.approx(0.05)
        # Energy: 300 / 10000 = 0.03
        assert features.get_feature(1) == pytest.approx(0.03)

    def test_extract_assets(self):
        state = make_game_state()
        features = self.extractor.extract(state, 1)
        # Planet count: 3 / 100 = 0.03
        assert features.get_feature(5) == pytest.approx(0.03)
        # Fleet count: 2 / 50 = 0.04
        assert features.get_feature(6) == pytest.approx(0.04)

    def test_extract_game_state(self):
        state = make_game_state(currentTurn=25)
        features = self.extractor.extract(state, 1)
        # Turn: 25 / 50 = 0.5
        assert features.get_feature(14) == pytest.approx(0.5)
        # Player count: 2 / 8 = 0.25
        assert features.get_feature(15) == pytest.approx(0.25)

    def test_extract_enemy_info(self):
        state = make_game_state()
        features = self.extractor.extract(state, 1)
        # Enemy fleet count: 1 / 50 = 0.02
        assert features.get_feature(12) == pytest.approx(0.02)

    def test_extract_difficulty_easy(self):
        state = make_game_state()
        # Player 1 is MEDIUM
        features = self.extractor.extract(state, 1)
        assert features.get_feature(24) == pytest.approx(0.5)

    def test_extract_difficulty_hard(self):
        state = make_game_state()
        players = state["players"]
        players[0]["difficulty"] = "HARD"
        features = self.extractor.extract(state, 1)
        assert features.get_feature(24) == pytest.approx(1.0)

    def test_extract_difficulty_easy_explicit(self):
        state = make_game_state()
        players = state["players"]
        players[0]["difficulty"] = "EASY"
        features = self.extractor.extract(state, 1)
        assert features.get_feature(24) == pytest.approx(0.0)

    def test_extract_turn_phase_early(self):
        state = make_game_state(currentTurn=5)
        features = self.extractor.extract(state, 1)
        assert features.get_feature(25) == pytest.approx(1.0)  # is_early
        assert features.get_feature(26) == pytest.approx(0.0)  # is_mid

    def test_extract_turn_phase_mid(self):
        state = make_game_state(currentTurn=17)
        features = self.extractor.extract(state, 1)
        assert features.get_feature(25) == pytest.approx(0.0)  # is_early
        assert features.get_feature(26) == pytest.approx(1.0)  # is_mid

    def test_extract_turn_phase_late(self):
        state = make_game_state(currentTurn=40)
        features = self.extractor.extract(state, 1)
        assert features.get_feature(25) == pytest.approx(0.0)
        assert features.get_feature(26) == pytest.approx(0.0)

    def test_extract_strategic_context_with_enemy(self):
        state = make_game_state()
        features = self.extractor.extract(state, 1)
        # Has adjacent enemy (player 2 exists)
        assert features.get_feature(21) == pytest.approx(1.0)
        # Has buildable resources (minerals >= 100, energy >= 50)
        assert features.get_feature(22) == pytest.approx(1.0)
        # Has idle fleet (fleetCount > 0)
        assert features.get_feature(23) == pytest.approx(1.0)

    def test_extract_resource_trends_high(self):
        state = make_game_state()
        players = state["players"]
        players[0]["resources"]["minerals"] = 6000  # > 5000
        players[0]["resources"]["energy"] = 6000
        features = self.extractor.extract(state, 1)
        assert features.get_feature(19) == pytest.approx(1.0)
        assert features.get_feature(20) == pytest.approx(1.0)

    def test_extract_resource_trends_low(self):
        state = make_game_state()
        players = state["players"]
        players[0]["resources"]["minerals"] = 100  # < 5000
        players[0]["resources"]["energy"] = 50
        features = self.extractor.extract(state, 1)
        assert features.get_feature(19) == pytest.approx(0.0)
        assert features.get_feature(20) == pytest.approx(0.0)

    def test_extract_relative_strength_single_player(self):
        state = make_game_state()
        players = state["players"]
        # Remove player 2
        state["players"] = [players[0]]
        features = self.extractor.extract(state, 1)
        assert features.get_feature(17) == pytest.approx(1.0)
        assert features.get_feature(18) == pytest.approx(1.0)

    def test_extract_fleet_position_default(self):
        state = make_game_state(subSectors=[])
        features = self.extractor.extract(state, 1)
        # Default position when no fleet found
        assert features.get_feature(8) == pytest.approx(0.5)
        assert features.get_feature(9) == pytest.approx(0.5)
        assert features.get_feature(10) == pytest.approx(0.5)

    def test_extract_fleet_position_from_subsector(self):
        state = make_game_state(
            subSectors=[
                {
                    "generated": True,
                    "systems": [
                        {
                            "sectorRow": 10,
                            "sectorCol": 20,
                            "fleets": [{"ownerId": 1}],
                        }
                    ],
                }
            ]
        )
        features = self.extractor.extract(state, 1)
        assert features.get_feature(8) == pytest.approx(10 / 100)
        assert features.get_feature(9) == pytest.approx(20 / 100)

    def test_extract_empty_players_list(self):
        state = {"currentTurn": 0, "players": [], "subSectors": []}
        features = self.extractor.extract(state, 1)
        assert all(f == 0.0 for f in features.features)

    def test_extract_missing_players_key(self):
        state = {"currentTurn": 0}
        features = self.extractor.extract(state, 1)
        assert all(f == 0.0 for f in features.features)
