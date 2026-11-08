"""Combines neural network predictions with heuristic scores for game AI decisions.

Mirrors the Java DlGameAiService.
"""

import logging
from typing import Any, Dict, List, Optional

from dora_dl.config import DlConfig
from dora_dl.feature_extractor import GameStateFeatureExtractor
from dora_dl.models import Action, ActionReward, GameStateFeatures
from dora_dl.network import StrategyNetwork

log = logging.getLogger(__name__)


class DlGameAiService:
    """Blends neural network predictions with heuristic scores."""

    def __init__(self, network: StrategyNetwork, extractor: GameStateFeatureExtractor,
                 config: DlConfig):
        self.network = network
        self.extractor = extractor
        self.config = config

    def predict_action(self, game_state: Dict[str, Any], player_id: int) -> Dict[str, Any]:
        """Predict action using full game state (local mode)."""
        if not self.network.is_initialized:
            self.network.initialize()

        features = self.extractor.extract(game_state, player_id)
        nn_probabilities = self.network.predict(features)
        nn_action = self.network.predict_action(features)

        action_count = len(Action)
        heuristic_scores = self._calculate_heuristic_scores(game_state, player_id, action_count)
        heuristic_action = int(max(range(len(heuristic_scores)), key=lambda i: heuristic_scores[i]))

        blend = self.config.blend_factor
        blended = [
            blend * nn_probabilities[i] + (1.0 - blend) * heuristic_scores[i]
            for i in range(action_count)
        ]

        best_action = int(max(range(len(blended)), key=lambda i: blended[i]))
        action = Action.from_index(best_action)

        log.debug(
            "DL AI prediction for player %s: %s (NN: %s, Heuristic: %s, Blend: %.2f)",
            player_id, action.name,
            Action.from_index(nn_action).name,
            Action.from_index(heuristic_action).name,
            blend,
        )

        return {
            "action": action.name,
            "actionIndex": best_action,
            "actionCategory": action.category,
            "confidence": blended[best_action],
            "nnProbabilities": nn_probabilities,
            "heuristicScores": heuristic_scores,
            "blendedScores": blended,
            "blendFactor": blend,
        }

    def predict_action_from_features(self, features: GameStateFeatures, player_id: int) -> Dict[str, Any]:
        """Predict using pre-extracted feature vector (called via REST)."""
        if not self.network.is_initialized:
            self.network.initialize()

        nn_probabilities = self.network.predict(features)
        nn_action = self.network.predict_action(features)

        action_count = len(Action)
        # When features are pre-extracted, we use default heuristic scores
        # since the full game state is not available
        heuristic_scores = self._default_heuristic_scores(action_count)
        heuristic_action = int(max(range(len(heuristic_scores)), key=lambda i: heuristic_scores[i]))

        blend = self.config.blend_factor
        blended = [
            blend * nn_probabilities[i] + (1.0 - blend) * heuristic_scores[i]
            for i in range(action_count)
        ]

        best_action = int(max(range(len(blended)), key=lambda i: blended[i]))
        action = Action.from_index(best_action)

        log.debug(
            "DL AI prediction for player %s: %s (NN: %s, Heuristic: %s, Blend: %.2f)",
            player_id, action.name,
            Action.from_index(nn_action).name,
            Action.from_index(heuristic_action).name,
            blend,
        )

        return {
            "action": action.name,
            "actionIndex": best_action,
            "actionCategory": action.category,
            "confidence": blended[best_action],
            "nnProbabilities": nn_probabilities,
            "heuristicScores": heuristic_scores,
            "blendedScores": blended,
            "blendFactor": blend,
        }

    def _calculate_heuristic_scores(
        self, game_state: Dict[str, Any], player_id: int, action_count: int
    ) -> List[float]:
        scores = [0.0] * action_count
        wait_index = Action.WAIT.index

        if not game_state or not game_state.get("players"):
            scores[wait_index] = 1.0
            return scores

        players = game_state.get("players") or []
        player = None
        for p in players:
            if p.get("id") == player_id:
                player = p
                break
        if player is None:
            scores[wait_index] = 1.0
            return scores

        resources = player.get("resources") or {}
        minerals = resources.get("minerals", 0)
        energy = resources.get("energy", 0)
        food = resources.get("food", 0)
        research = resources.get("research", 0)
        fleet_count = player.get("fleetCount", 0)
        planet_count = player.get("planetCount", 0)

        # Strategic
        if minerals >= self.config.heuristic_minerals_build_ship and energy >= self.config.heuristic_energy_build_ship:
            scores[Action.BUILD_SHIP.index] = 0.7
        if minerals >= self.config.heuristic_minerals_build_station and energy >= self.config.heuristic_energy_build_station:
            scores[Action.BUILD_STATION.index] = 0.5
        if planet_count < self.config.heuristic_max_planets_for_expansion and food >= self.config.heuristic_food_expand_colony:
            scores[Action.EXPAND_COLONY.index] = 0.6
        if research >= self.config.heuristic_research_tech:
            scores[Action.RESEARCH_TECH.index] = 0.5
        scores[Action.DIPLOMATIC_STANCE.index] = 0.2

        # Tactical
        if fleet_count > 0:
            scores[Action.MOVE_FLEET.index] = 0.5
        if fleet_count >= self.config.heuristic_fleet_engage_combat:
            scores[Action.ENGAGE_COMBAT.index] = 0.6
        if fleet_count > 0 and minerals < self.config.heuristic_minerals_retreat:
            scores[Action.RETREAT.index] = 0.7
        if fleet_count >= self.config.heuristic_fleet_flank_ambush:
            scores[Action.FLANK_ENEMY.index] = 0.3
            scores[Action.AMBUSH.index] = 0.2

        # Economic
        if minerals < self.config.heuristic_minerals_optimize_mining:
            scores[Action.OPTIMIZE_MINING.index] = 0.6
        scores[Action.OPTIMIZE_TRADE.index] = 0.3
        if fleet_count > 0:
            scores[Action.REPAIR_FLEET.index] = 0.3
        scores[Action.UPGRADE_STATION.index] = 0.2

        # Defensive
        if minerals >= self.config.heuristic_minerals_fortify:
            scores[Action.FORTIFY_PLANET.index] = 0.4
        if fleet_count > 0:
            scores[Action.SCOUT_AREA.index] = 0.4

        # Wait
        if minerals < self.config.heuristic_minerals_wait or energy < self.config.heuristic_energy_wait:
            scores[wait_index] = 0.8
        else:
            scores[wait_index] = 0.1

        # Normalize
        total = sum(scores)
        if total > 0:
            scores = [s / total for s in scores]
        return scores

    @property
    def blend_factor(self) -> float:
        return self.config.blend_factor

    @blend_factor.setter
    def blend_factor(self, value: float) -> None:
        self.config.blend_factor = max(0.0, min(1.0, value))

    @property
    def is_network_ready(self) -> bool:
        return self.network.is_initialized

    def _default_heuristic_scores(self, action_count: int) -> List[float]:
        """Return uniform heuristic scores when full game state is not available."""
        return [1.0 / action_count] * action_count
