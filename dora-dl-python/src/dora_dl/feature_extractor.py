"""Converts game state data into feature vectors for the neural network.

Mirrors the Java GameStateFeatureExtractor.
"""

import logging
from typing import Any, Dict, List, Optional

from dora_dl.models import GameStateFeatures

log = logging.getLogger(__name__)

# Normalization constants
MAX_TURNS = 50
MAX_PLAYERS = 8
MAX_PLANETS = 100
MAX_FLEETS = 50
MAX_SHIPS = 500
MAX_RESOURCES = 10000
MAX_DISTANCE = 100


def _normalize(value: int, max_val: int) -> float:
    return min(1.0, max(0.0, value / max_val))


def _estimate_total_ships(player: Dict[str, Any]) -> float:
    resources = player.get("resources") or {}
    minerals = resources.get("minerals", 0)
    energy = resources.get("energy", 0)
    built_ships = min(minerals // 100, energy // 50)
    return max(built_ships, player.get("fleetCount", 0))


def _calculate_player_power(player: Dict[str, Any]) -> int:
    resources = player.get("resources") or {}
    power = 0
    power += resources.get("minerals", 0)
    power += resources.get("energy", 0) * 2
    power += resources.get("food", 0)
    power += player.get("planetCount", 0) * 100
    power += player.get("fleetCount", 0) * 50
    return power


def _calculate_player_rank(players: List[Dict[str, Any]], player: Dict[str, Any]) -> int:
    player_power = _calculate_player_power(player)
    rank = 1
    for other in players:
        if _calculate_player_power(other) > player_power:
            rank += 1
    return rank


def _find_player(players: Optional[List[Dict[str, Any]]], player_id: int) -> Optional[Dict[str, Any]]:
    if not players:
        return None
    for p in players:
        if p.get("id") == player_id:
            return p
    return None


class GameStateFeatureExtractor:
    """Thread-safe feature extractor for concurrent game sessions."""

    def extract(self, game_state: Dict[str, Any], player_id: int) -> GameStateFeatures:
        if game_state is None:
            raise ValueError("Game state cannot be null")
        if player_id is None:
            raise ValueError("Player ID cannot be null")

        features = GameStateFeatures()
        players = game_state.get("players") or []
        player = _find_player(players, player_id)
        if player is None:
            return features

        self._extract_resources(features, player)
        self._extract_assets(features, player)
        self._extract_fleet_position(features, game_state, player)
        self._extract_enemy_info(features, game_state, player)
        self._extract_game_state(features, game_state, player)
        self._extract_relative_strength(features, game_state, player)
        self._extract_resource_trends(features, player)
        self._extract_strategic_context(features, game_state, player)
        self._extract_difficulty(features, player)
        self._extract_turn_phase(features, game_state)

        return features

    def _extract_resources(self, features: GameStateFeatures,
                           player: Dict[str, Any]) -> None:
        resources = player.get("resources") or {}
        minerals = _normalize(resources.get("minerals", 0), MAX_RESOURCES)
        energy = _normalize(resources.get("energy", 0), MAX_RESOURCES)
        food = _normalize(resources.get("food", 0), MAX_RESOURCES)
        research = _normalize(resources.get("research", 0), MAX_RESOURCES)
        influence = _normalize(resources.get("influence", 0), MAX_RESOURCES)
        features.set_resources(minerals, energy, food, research, influence)

    def _extract_assets(self, features: GameStateFeatures,
                        player: Dict[str, Any]) -> None:
        planet_count = _normalize(player.get("planetCount", 0), MAX_PLANETS)
        fleet_count = _normalize(player.get("fleetCount", 0), MAX_FLEETS)
        total_ships = _estimate_total_ships(player)
        features.set_assets(planet_count, fleet_count, total_ships)

    def _extract_fleet_position(self, features: GameStateFeatures,
                                game_state: Dict[str, Any],
                                player: Dict[str, Any]) -> None:
        sub_sectors = game_state.get("subSectors") or []
        x, y, z = 0.5, 0.5, 0.5
        player_id = player.get("id")
        for sub in sub_sectors:
            if sub.get("generated") and sub.get("systems"):
                for system in sub["systems"]:
                    if isinstance(system, dict):
                        fleets = system.get("fleets") or []
                        for fleet in fleets:
                            if isinstance(fleet, dict) and fleet.get("ownerId") == player_id:
                                row = system.get("sectorRow", 0)
                                col = system.get("sectorCol", 0)
                                x = _normalize(row, MAX_DISTANCE)
                                y = _normalize(col, MAX_DISTANCE)
                                break
        features.set_fleet_position(x, y, z)

    def _extract_enemy_info(self, features: GameStateFeatures,
                            game_state: Dict[str, Any],
                            player: Dict[str, Any]) -> None:
        players = game_state.get("players") or []
        player_id = player.get("id")
        enemy_fleet_count = 0
        enemy_total_ships = 0
        for other in players:
            if other.get("id") == player_id:
                continue
            enemy_fleet_count += other.get("fleetCount", 0)
            enemy_total_ships += _estimate_total_ships(other)
        features.set_enemy_info(
            1.0,
            _normalize(enemy_fleet_count, MAX_FLEETS),
            _normalize(enemy_total_ships, MAX_SHIPS),
        )

    def _extract_game_state(self, features: GameStateFeatures,
                            game_state: Dict[str, Any],
                            player: Dict[str, Any]) -> None:
        current_turn = game_state.get("currentTurn", 0)
        player_count = len(game_state.get("players") or [])
        rank = _calculate_player_rank(game_state.get("players") or [], player)
        features.set_game_state(
            _normalize(current_turn, MAX_TURNS),
            _normalize(player_count, MAX_PLAYERS),
            _normalize(rank, MAX_PLAYERS),
        )

    def _extract_relative_strength(self, features: GameStateFeatures,
                                   game_state: Dict[str, Any],
                                   player: Dict[str, Any]) -> None:
        players = game_state.get("players") or []
        if len(players) <= 1:
            features.set_relative_strength(1.0, 1.0)
            return
        player_power = _calculate_player_power(player)
        total_enemy_power = 0
        enemy_count = 0
        player_id = player.get("id")
        for other in players:
            if other.get("id") != player_id:
                total_enemy_power += _calculate_player_power(other)
                enemy_count += 1
        avg_enemy_power = total_enemy_power / enemy_count if enemy_count > 0 else 1
        features.set_relative_strength(
            player_power / (avg_enemy_power + 1),
            player_power / (max(avg_enemy_power, 1) + 1),
        )

    def _extract_resource_trends(self, features: GameStateFeatures,
                                 player: Dict[str, Any]) -> None:
        resources = player.get("resources") or {}
        minerals = resources.get("minerals", 0)
        energy = resources.get("energy", 0)
        features.set_resource_trends(
            1.0 if minerals > MAX_RESOURCES * 0.5 else 0.0,
            1.0 if energy > MAX_RESOURCES * 0.5 else 0.0,
        )

    def _extract_strategic_context(self, features: GameStateFeatures,
                                   game_state: Dict[str, Any],
                                   player: Dict[str, Any]) -> None:
        players = game_state.get("players") or []
        player_id = player.get("id")
        has_adjacent_enemy = any(p.get("id") != player_id for p in players)
        resources = player.get("resources") or {}
        has_buildable = (
            resources.get("minerals", 0) >= 100 and resources.get("energy", 0) >= 50
        )
        has_idle_fleet = player.get("fleetCount", 0) > 0
        features.set_strategic_context(
            1.0 if has_adjacent_enemy else 0.0,
            1.0 if has_buildable else 0.0,
            1.0 if has_idle_fleet else 0.0,
        )

    def _extract_difficulty(self, features: GameStateFeatures,
                            player: Dict[str, Any]) -> None:
        difficulty = (player.get("difficulty") or "MEDIUM").upper()
        bias_map = {"EASY": 0.0, "MEDIUM": 0.5, "HARD": 1.0}
        features.set_difficulty_bias(bias_map.get(difficulty, 0.5))

    def _extract_turn_phase(self, features: GameStateFeatures,
                            game_state: Dict[str, Any]) -> None:
        turn = game_state.get("currentTurn", 0)
        is_early = turn < MAX_TURNS / 3
        is_mid = MAX_TURNS / 3 <= turn < (2 * MAX_TURNS) / 3
        features.set_turn_phase(is_early, is_mid)
