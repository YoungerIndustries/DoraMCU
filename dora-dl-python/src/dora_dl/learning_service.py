"""Manages the reinforcement learning feedback loop for Dora AI.

Mirrors the Java DoraAiLearningService — collects experience, computes rewards,
trains the network, and saves models.
"""

import gc
import logging
import os
import time
from collections import deque
from typing import Any, Dict, List, Optional

from dora_dl.config import DlConfig
from dora_dl.dl_game_ai_service import DlGameAiService
from dora_dl.feature_extractor import GameStateFeatureExtractor
from dora_dl.models import Action, ActionReward, GameStateFeatures
from dora_dl.network import StrategyNetwork

log = logging.getLogger(__name__)

MAX_EXPERIENCE_BUFFER_SIZE = 200
TRAINING_THRESHOLD = 50


class DoraAiLearningService:
    """Reinforcement learning service with experience buffer and reward computation."""

    def __init__(self, network: StrategyNetwork, extractor: GameStateFeatureExtractor,
                 config: DlConfig):
        self.network = network
        self.extractor = extractor
        self.config = config
        self.experience_buffer: deque = deque(maxlen=config.max_experience_buffer_size)
        self.learning_enabled = True
        self.total_actions_recorded = 0
        self.total_training_cycles = 0
        self.last_training_time = 0

        # Try to load existing model
        model_path = os.path.join(config.model_dir, "strategy-network.pt")
        if os.path.exists(model_path):
            try:
                network.load_model(model_path)
                log.info("Loaded existing strategy model from %s", model_path)
            except Exception as e:
                log.warning("Failed to load existing model: %s", e)
        else:
            log.info("No existing model found. Starting with random weights.")

    def record_action(self, game_state: Dict[str, Any], player_id: int,
                      action_name: str, reward: float) -> None:
        if not self.learning_enabled:
            return

        try:
            features = self.extractor.extract(game_state, player_id)
            self.record_action_from_features(features, player_id, action_name, reward)
        except Exception as e:
            log.warning("Failed to record action for player %s: %s", player_id, e)

    def record_action_from_features(self, features: GameStateFeatures, player_id: int,
                                     action_name: str, reward: float) -> None:
        """Record experience using pre-extracted feature vector (called via REST)."""
        if not self.learning_enabled:
            return

        try:
            action = Action[action_name]
            sample = ActionReward(features, action, reward)
            self.experience_buffer.append(sample)
            self.total_actions_recorded += 1

            log.debug(
                "Recorded action: %s (reward: %.2f), buffer size: %d",
                action_name, reward, len(self.experience_buffer),
            )

            if len(self.experience_buffer) >= TRAINING_THRESHOLD:
                self.train_on_accumulated_experience()
        except Exception as e:
            log.warning("Failed to record action for player %s: %s", player_id, e)

    def compute_reward(self, game_state_before: Dict[str, Any],
                       game_state_after: Dict[str, Any],
                       player_id: int) -> float:
        """Compute reward from full game states (local mode)."""
        reward = 0.0
        if not game_state_before or not game_state_after:
            return reward

        player_before = self._find_player(game_state_before, player_id)
        player_after = self._find_player(game_state_after, player_id)
        if not player_before or not player_after:
            return reward

        # Resource change reward
        reward += self._compute_resource_reward(player_before, player_after) * 0.3

        # Fleet strength change reward
        fleet_delta = player_after.get("fleetCount", 0) - player_before.get("fleetCount", 0)
        reward += max(-1.0, min(1.0, fleet_delta / 5.0)) * 0.25

        # Planet count change reward
        planet_delta = player_after.get("planetCount", 0) - player_before.get("planetCount", 0)
        reward += planet_delta * 0.2

        # Relative strength change
        strength_before = self._compute_player_strength(player_before)
        strength_after = self._compute_player_strength(player_after)
        strength_delta = (strength_after - strength_before) / (strength_before + 1.0)
        reward += strength_delta * 0.15

        return max(-1.0, min(1.0, reward))

    def compute_reward_from_features(self, features_before: GameStateFeatures,
                                      features_after: GameStateFeatures,
                                      player_id: int) -> float:
        """Compute reward from pre-extracted feature vectors (called via REST).
        
        Uses the resource and asset deltas encoded in the feature vectors.
        """
        reward = 0.0
        
        # Resource change reward (features 0-4: minerals, energy, food, research, total)
        for i in range(4):
            delta = features_after.get_feature(i) - features_before.get_feature(i)
            reward += delta * 0.075  # 0.3 / 4 resources
        
        # Fleet strength change (feature 7: fleetCount)
        fleet_delta = features_after.get_feature(7) - features_before.get_feature(7)
        reward += max(-1.0, min(1.0, fleet_delta / 5.0)) * 0.25
        
        # Planet count change (feature 5: planetCount)
        planet_delta = features_after.get_feature(5) - features_before.get_feature(5)
        reward += planet_delta * 0.2
        
        # Relative strength change (feature 17: relativeStrength)
        strength_delta = features_after.get_feature(17) - features_before.get_feature(17)
        reward += strength_delta * 0.15
        
        return max(-1.0, min(1.0, reward))

    def _compute_resource_reward(self, before: Dict[str, Any],
                                  after: Dict[str, Any]) -> float:
        res_before = before.get("resources") or {}
        res_after = after.get("resources") or {}
        minerals_delta = res_after.get("minerals", 0) - res_before.get("minerals", 0)
        energy_delta = res_after.get("energy", 0) - res_before.get("energy", 0)
        food_delta = res_after.get("food", 0) - res_before.get("food", 0)
        normalized = (minerals_delta + energy_delta + food_delta) / 1000.0
        return max(-1.0, min(1.0, normalized))

    def _compute_player_strength(self, player: Dict[str, Any]) -> float:
        strength = player.get("planetCount", 0) * 10.0 + player.get("fleetCount", 0) * 5.0
        resources = player.get("resources") or {}
        strength += resources.get("minerals", 0) * 0.1
        strength += resources.get("energy", 0) * 0.2
        return strength

    def _find_player(self, game_state: Dict[str, Any],
                     player_id: int) -> Optional[Dict[str, Any]]:
        if not game_state:
            return None
        for p in game_state.get("players") or []:
            if p.get("id") == player_id:
                return p
        return None

    def train_on_accumulated_experience(self) -> int:
        if not self.learning_enabled:
            return 0

        if not self.experience_buffer:
            return 0

        training_data = list(self.experience_buffer)
        self.experience_buffer.clear()

        if not training_data:
            return 0

        try:
            if not self.network.is_initialized:
                self.network.initialize()

            self.network.train(training_data)
            self.total_training_cycles += 1
            self.last_training_time = int(time.time() * 1000)

            # Save model
            try:
                model_path = self.network.save_model()
                log.info(
                    "Trained on %d samples (cycle #%d), model saved to %s",
                    len(training_data), self.total_training_cycles, model_path,
                )
            except Exception as e:
                log.warning("Failed to save model: %s", e)

            return len(training_data)
        except Exception as e:
            log.warning("Training failed: %s", e)
            # Put samples back
            for sample in training_data:
                self.experience_buffer.append(sample)
            return 0

    def save_model(self) -> str:
        return self.network.save_model()

    def clear_experience(self) -> None:
        self.experience_buffer.clear()
        log.info("Experience buffer cleared")

    # Statistics
    @property
    def experience_buffer_size(self) -> int:
        return len(self.experience_buffer)
