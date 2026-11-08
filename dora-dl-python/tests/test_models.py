"""Tests for data models — Action enum, GameStateFeatures, and ActionReward."""

import pytest
from dora_dl.models import (
    Action,
    ActionReward,
    GameStateFeatures,
    FEATURE_DIMENSION,
)


class TestActionEnum:
    """Verify Action enum values, categories, and conversions."""

    def test_action_count(self):
        assert len(Action) == 17

    def test_action_indices_sequential(self):
        for i, action in enumerate(Action):
            assert action.index == i

    def test_strategic_actions(self):
        assert Action.BUILD_SHIP.index == 0
        assert Action.BUILD_STATION.index == 1
        assert Action.EXPAND_COLONY.index == 2
        assert Action.RESEARCH_TECH.index == 3
        assert Action.DIPLOMATIC_STANCE.index == 4

    def test_tactical_actions(self):
        assert Action.MOVE_FLEET.index == 5
        assert Action.ENGAGE_COMBAT.index == 6
        assert Action.RETREAT.index == 7
        assert Action.FLANK_ENEMY.index == 8
        assert Action.AMBUSH.index == 9

    def test_economic_actions(self):
        assert Action.OPTIMIZE_MINING.index == 10
        assert Action.OPTIMIZE_TRADE.index == 11
        assert Action.REPAIR_FLEET.index == 12
        assert Action.UPGRADE_STATION.index == 13

    def test_defensive_actions(self):
        assert Action.FORTIFY_PLANET.index == 14
        assert Action.SCOUT_AREA.index == 15
        assert Action.WAIT.index == 16

    def test_from_index_valid(self):
        for action in Action:
            retrieved = Action.from_index(action.index)
            assert retrieved is action

    def test_from_index_invalid_raises(self):
        with pytest.raises(ValueError, match="Unknown action index"):
            Action.from_index(-1)
        with pytest.raises(ValueError, match="Unknown action index"):
            Action.from_index(17)

    def test_category_strategic(self):
        for i in range(5):
            assert Action.from_index(i).category == "STRATEGIC"

    def test_category_tactical(self):
        for i in range(5, 10):
            assert Action.from_index(i).category == "TACTICAL"

    def test_category_economic(self):
        for i in range(10, 14):
            assert Action.from_index(i).category == "ECONOMIC"

    def test_category_defensive(self):
        for i in range(14, 17):
            assert Action.from_index(i).category == "DEFENSIVE"


class TestGameStateFeatures:
    """Verify feature vector construction, accessors, and validation."""

    def test_default_constructor_all_zeros(self):
        features = GameStateFeatures()
        assert len(features.features) == FEATURE_DIMENSION
        assert all(f == 0.0 for f in features.features)

    def test_constructor_with_features(self):
        values = [float(i) for i in range(FEATURE_DIMENSION)]
        features = GameStateFeatures(values)
        assert features.features == values

    def test_constructor_wrong_dimension_raises(self):
        with pytest.raises(ValueError, match="Feature vector must have exactly"):
            GameStateFeatures([0.0] * 32)

    def test_get_feature(self):
        features = GameStateFeatures()
        features.set_feature(10, 3.14)
        assert features.get_feature(10) == 3.14

    def test_get_feature_out_of_range_raises(self):
        features = GameStateFeatures()
        with pytest.raises(IndexError):
            features.get_feature(-1)
        with pytest.raises(IndexError):
            features.get_feature(64)

    def test_set_feature_out_of_range_raises(self):
        features = GameStateFeatures()
        with pytest.raises(IndexError):
            features.set_feature(-1, 1.0)
        with pytest.raises(IndexError):
            features.set_feature(64, 1.0)

    def test_set_resources(self):
        features = GameStateFeatures()
        features.set_resources(100.0, 50.0, 30.0, 20.0, 10.0)
        assert features.get_feature(0) == 100.0
        assert features.get_feature(1) == 50.0
        assert features.get_feature(2) == 30.0
        assert features.get_feature(3) == 20.0
        assert features.get_feature(4) == 10.0

    def test_set_assets(self):
        features = GameStateFeatures()
        features.set_assets(5.0, 3.0, 20.0)
        assert features.get_feature(5) == 5.0
        assert features.get_feature(6) == 3.0
        assert features.get_feature(7) == 20.0

    def test_set_fleet_position(self):
        features = GameStateFeatures()
        features.set_fleet_position(10.0, 20.0, 30.0)
        assert features.get_feature(8) == 10.0
        assert features.get_feature(9) == 20.0
        assert features.get_feature(10) == 30.0

    def test_set_enemy_info(self):
        features = GameStateFeatures()
        features.set_enemy_info(5.0, 2.0, 15.0)
        assert features.get_feature(11) == 5.0
        assert features.get_feature(12) == 2.0
        assert features.get_feature(13) == 15.0

    def test_set_game_state(self):
        features = GameStateFeatures()
        features.set_game_state(10.0, 4.0, 2.0)
        assert features.get_feature(14) == 10.0
        assert features.get_feature(15) == 4.0
        assert features.get_feature(16) == 2.0

    def test_set_relative_strength(self):
        features = GameStateFeatures()
        features.set_relative_strength(1.5, 2.0)
        assert features.get_feature(17) == 1.5
        assert features.get_feature(18) == 2.0

    def test_set_resource_trends(self):
        features = GameStateFeatures()
        features.set_resource_trends(0.5, -0.3)
        assert features.get_feature(19) == 0.5
        assert features.get_feature(20) == -0.3

    def test_set_strategic_context(self):
        features = GameStateFeatures()
        features.set_strategic_context(1.0, 1.0, 0.0)
        assert features.get_feature(21) == 1.0
        assert features.get_feature(22) == 1.0
        assert features.get_feature(23) == 0.0

    def test_set_difficulty_bias(self):
        features = GameStateFeatures()
        features.set_difficulty_bias(0.75)
        assert features.get_feature(24) == 0.75

    def test_set_turn_phase_early(self):
        features = GameStateFeatures()
        features.set_turn_phase(True, False)
        assert features.get_feature(25) == 1.0
        assert features.get_feature(26) == 0.0

    def test_set_turn_phase_mid(self):
        features = GameStateFeatures()
        features.set_turn_phase(False, True)
        assert features.get_feature(25) == 0.0
        assert features.get_feature(26) == 1.0

    def test_set_personality(self):
        features = GameStateFeatures()
        features.set_personality(0.8, 0.6)
        assert features.get_feature(32) == 0.8
        assert features.get_feature(33) == 0.6

    def test_set_government_ethos(self):
        features = GameStateFeatures()
        features.set_government_ethos(2.0, 1.0)
        assert features.get_feature(34) == 2.0
        assert features.get_feature(35) == 1.0

    def test_set_research_level(self):
        features = GameStateFeatures()
        features.set_research_level(3.0, 2.0)
        assert features.get_feature(36) == 3.0
        assert features.get_feature(37) == 2.0

    def test_set_fleet_composition(self):
        features = GameStateFeatures()
        features.set_fleet_composition(0.7, 0.3)
        assert features.get_feature(38) == 0.7
        assert features.get_feature(39) == 0.3

    def test_set_station_coverage(self):
        features = GameStateFeatures()
        features.set_station_coverage(2.0, 1.0)
        assert features.get_feature(40) == 2.0
        assert features.get_feature(41) == 1.0

    def test_set_economic_health(self):
        features = GameStateFeatures()
        features.set_economic_health(0.9, 0.4)
        assert features.get_feature(42) == 0.9
        assert features.get_feature(43) == 0.4

    def test_set_diplomatic_context(self):
        features = GameStateFeatures()
        features.set_diplomatic_context(0.5, 0.3)
        assert features.get_feature(44) == 0.5
        assert features.get_feature(45) == 0.3

    def test_set_tactical_situation(self):
        features = GameStateFeatures()
        features.set_tactical_situation(1.0, 2.0)
        assert features.get_feature(46) == 1.0
        assert features.get_feature(47) == 2.0

    def test_set_technology_edge(self):
        features = GameStateFeatures()
        features.set_technology_edge(3.0, 2.0)
        assert features.get_feature(48) == 3.0
        assert features.get_feature(49) == 2.0

    def test_set_resource_pipeline(self):
        features = GameStateFeatures()
        features.set_resource_pipeline(50.0, 30.0)
        assert features.get_feature(50) == 50.0
        assert features.get_feature(51) == 30.0

    def test_set_fleet_readiness(self):
        features = GameStateFeatures()
        features.set_fleet_readiness(0.8, 0.9)
        assert features.get_feature(52) == 0.8
        assert features.get_feature(53) == 0.9

    def test_set_strategic_depth(self):
        features = GameStateFeatures()
        features.set_strategic_depth(3.0, 0.7)
        assert features.get_feature(54) == 3.0
        assert features.get_feature(55) == 0.7

    def test_clear(self):
        features = GameStateFeatures([float(i) for i in range(FEATURE_DIMENSION)])
        features.clear()
        assert all(f == 0.0 for f in features.features)

    def test_to_list_returns_copy(self):
        features = GameStateFeatures()
        lst = features.to_list()
        lst[0] = 999.0
        assert features.get_feature(0) == 0.0

    def test_equality(self):
        a = GameStateFeatures([1.0] * FEATURE_DIMENSION)
        b = GameStateFeatures([1.0] * FEATURE_DIMENSION)
        c = GameStateFeatures([0.0] * FEATURE_DIMENSION)
        assert a == b
        assert a != c

    def test_equality_with_non_features(self):
        features = GameStateFeatures()
        assert features != "not features"
        assert features != 42

    def test_repr(self):
        features = GameStateFeatures()
        repr_str = repr(features)
        assert "GameStateFeatures" in repr_str
        assert "features=" in repr_str


class TestActionReward:
    """Verify ActionReward construction and one-hot encoding."""

    def test_default_constructor(self):
        ar = ActionReward()
        assert ar.state is None
        assert ar.action is None
        assert ar.reward == 0.0

    def test_full_constructor(self):
        state = GameStateFeatures()
        ar = ActionReward(state, Action.BUILD_SHIP, 0.5)
        assert ar.state is state
        assert ar.action is Action.BUILD_SHIP
        assert ar.reward == 0.5

    def test_to_one_hot_with_action(self):
        ar = ActionReward(action=Action.ENGAGE_COMBAT)
        one_hot = ar.to_one_hot()
        assert len(one_hot) == 17
        assert one_hot[Action.ENGAGE_COMBAT.index] == 1.0
        assert sum(one_hot) == 1.0

    def test_to_one_hot_without_action(self):
        ar = ActionReward()
        one_hot = ar.to_one_hot()
        assert len(one_hot) == 17
        assert all(v == 0.0 for v in one_hot)

    def test_to_one_hot_last_action(self):
        ar = ActionReward(action=Action.WAIT)
        one_hot = ar.to_one_hot()
        assert one_hot[16] == 1.0

    def test_repr(self):
        ar = ActionReward(action=Action.BUILD_SHIP, reward=0.75)
        repr_str = repr(ar)
        assert "ActionReward" in repr_str
        assert "BUILD_SHIP" in repr_str
