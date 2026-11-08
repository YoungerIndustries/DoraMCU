"""Data models for the deep learning module.

Mirrors the Java GameStateFeatures and ActionReward classes.
"""

import copy
from enum import Enum
from typing import List, Optional


from dora_dl.config import DlConfig

# Feature dimension derived from config default
DEFAULT_CONFIG = DlConfig()
FEATURE_DIMENSION = DEFAULT_CONFIG.input_size  # 64
ORIGINAL_DIMENSION = 32
EMPIRE_FEATURE_START = 32


class Action(Enum):
    """Action types the neural network can predict.
    
    17 actions covering strategic, tactical, economic, and defensive categories.
    """
    # Strategic (Empire level)
    BUILD_SHIP = 0
    BUILD_STATION = 1
    EXPAND_COLONY = 2
    RESEARCH_TECH = 3
    DIPLOMATIC_STANCE = 4

    # Tactical (Fleet level)
    MOVE_FLEET = 5
    ENGAGE_COMBAT = 6
    RETREAT = 7
    FLANK_ENEMY = 8
    AMBUSH = 9

    # Economic (Facility level)
    OPTIMIZE_MINING = 10
    OPTIMIZE_TRADE = 11
    REPAIR_FLEET = 12
    UPGRADE_STATION = 13

    # Defensive
    FORTIFY_PLANET = 14
    SCOUT_AREA = 15
    WAIT = 16

    @property
    def index(self) -> int:
        return self.value

    @classmethod
    def from_index(cls, index: int) -> "Action":
        for action in cls:
            if action.index == index:
                return action
        raise ValueError(
            f"Unknown action index: {index} (valid range: 0-{len(cls) - 1})"
        )

    @property
    def category(self) -> str:
        if self.index <= 4:
            return "STRATEGIC"
        if self.index <= 9:
            return "TACTICAL"
        if self.index <= 13:
            return "ECONOMIC"
        return "DEFENSIVE"


class GameStateFeatures:
    """64-dimensional feature vector extracted from a game state.
    
    Feature layout:
    - [0-4]: Player resources (minerals, energy, food, research, influence)
    - [5-7]: Player assets (planet count, fleet count, total ship count)
    - [8-10]: Fleet positions (x, y, z)
    - [11-13]: Enemy info (distance, fleet count, total ships)
    - [14-16]: Game state (turn, players, rank)
    - [17-18]: Relative strength
    - [19-20]: Resource trends
    - [21-23]: Strategic context
    - [24]: Difficulty bias
    - [25-26]: Turn phase (one-hot)
    - [27-31]: Reserved
    - [32-63]: Empire-specific features
    """

    def __init__(self, features: Optional[List[float]] = None):
        if features is None:
            self.features = [0.0] * FEATURE_DIMENSION
        else:
            if len(features) != FEATURE_DIMENSION:
                raise ValueError(
                    f"Feature vector must have exactly {FEATURE_DIMENSION} "
                    f"dimensions, got {len(features)}"
                )
            self.features = list(features)

    def get_feature(self, index: int) -> float:
        if index < 0 or index >= FEATURE_DIMENSION:
            raise IndexError(
                f"Feature index {index} out of range [0, {FEATURE_DIMENSION})"
            )
        return self.features[index]

    def set_feature(self, index: int, value: float) -> None:
        if index < 0 or index >= FEATURE_DIMENSION:
            raise IndexError(
                f"Feature index {index} out of range [0, {FEATURE_DIMENSION})"
            )
        self.features[index] = value

    def set_resources(self, minerals: float, energy: float, food: float,
                      research: float, influence: float) -> None:
        self.features[0] = minerals
        self.features[1] = energy
        self.features[2] = food
        self.features[3] = research
        self.features[4] = influence

    def set_assets(self, planet_count: float, fleet_count: float,
                   total_ship_count: float) -> None:
        self.features[5] = planet_count
        self.features[6] = fleet_count
        self.features[7] = total_ship_count

    def set_fleet_position(self, x: float, y: float, z: float) -> None:
        self.features[8] = x
        self.features[9] = y
        self.features[10] = z

    def set_enemy_info(self, nearest_distance: float, enemy_fleet_count: float,
                       enemy_total_ships: float) -> None:
        self.features[11] = nearest_distance
        self.features[12] = enemy_fleet_count
        self.features[13] = enemy_total_ships

    def set_game_state(self, current_turn: float, player_count: float,
                       player_rank: float) -> None:
        self.features[14] = current_turn
        self.features[15] = player_count
        self.features[16] = player_rank

    def set_relative_strength(self, vs_average_enemy: float,
                              vs_nearest_enemy: float) -> None:
        self.features[17] = vs_average_enemy
        self.features[18] = vs_nearest_enemy

    def set_resource_trends(self, minerals_delta: float, energy_delta: float) -> None:
        self.features[19] = minerals_delta
        self.features[20] = energy_delta

    def set_strategic_context(self, has_adjacent_enemy: float,
                              has_buildable_resources: float,
                              has_idle_fleet: float) -> None:
        self.features[21] = has_adjacent_enemy
        self.features[22] = has_buildable_resources
        self.features[23] = has_idle_fleet

    def set_difficulty_bias(self, difficulty: float) -> None:
        self.features[24] = difficulty

    def set_turn_phase(self, is_early: bool, is_mid: bool) -> None:
        self.features[25] = 1.0 if is_early else 0.0
        self.features[26] = 1.0 if is_mid else 0.0

    # Empire-specific features
    def set_personality(self, aggression: float, expansion_drive: float) -> None:
        self.features[32] = aggression
        self.features[33] = expansion_drive

    def set_government_ethos(self, government_index: float,
                             ethos_index: float) -> None:
        self.features[34] = government_index
        self.features[35] = ethos_index

    def set_research_level(self, research_level: float, ai_core_level: float) -> None:
        self.features[36] = research_level
        self.features[37] = ai_core_level

    def set_fleet_composition(self, fighter_ratio: float,
                              capital_ship_ratio: float) -> None:
        self.features[38] = fighter_ratio
        self.features[39] = capital_ship_ratio

    def set_station_coverage(self, military_stations: float,
                             research_stations: float) -> None:
        self.features[40] = military_stations
        self.features[41] = research_stations

    def set_economic_health(self, credit_flow: float, upkeep_ratio: float) -> None:
        self.features[42] = credit_flow
        self.features[43] = upkeep_ratio

    def set_diplomatic_context(self, allies_ratio: float,
                               enemies_ratio: float) -> None:
        self.features[44] = allies_ratio
        self.features[45] = enemies_ratio

    def set_tactical_situation(self, active_battles: float,
                               threatened_planets: float) -> None:
        self.features[46] = active_battles
        self.features[47] = threatened_planets

    def set_technology_edge(self, weapon_level: float, shield_level: float) -> None:
        self.features[48] = weapon_level
        self.features[49] = shield_level

    def set_resource_pipeline(self, mining_rate: float,
                              processing_rate: float) -> None:
        self.features[50] = mining_rate
        self.features[51] = processing_rate

    def set_fleet_readiness(self, average_hull: float,
                            average_shield: float) -> None:
        self.features[52] = average_hull
        self.features[53] = average_shield

    def set_strategic_depth(self, planets_behind_front: float,
                            supply_line_integrity: float) -> None:
        self.features[54] = planets_behind_front
        self.features[55] = supply_line_integrity

    def clear(self) -> None:
        """Reset all features to zero."""
        for i in range(FEATURE_DIMENSION):
            self.features[i] = 0.0

    def to_list(self) -> List[float]:
        """Return a copy of the features list."""
        return list(self.features)

    def __eq__(self, other):
        if not isinstance(other, GameStateFeatures):
            return False
        return self.features == other.features

    def __repr__(self):
        return f"GameStateFeatures(features={self.features})"


class ActionReward:
    """A single training data point: game state, action taken, and reward received."""

    def __init__(self, state: Optional[GameStateFeatures] = None,
                 action: Optional[Action] = None, reward: float = 0.0):
        self.state = state
        self.action = action
        self.reward = reward

    def to_one_hot(self) -> List[float]:
        """Convert the action to a one-hot encoded vector."""
        one_hot = [0.0] * len(Action)
        if self.action is not None:
            one_hot[self.action.index] = 1.0
        return one_hot

    def __repr__(self):
        return f"ActionReward(action={self.action}, reward={self.reward})"
