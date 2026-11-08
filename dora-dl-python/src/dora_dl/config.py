"""Configuration for the deep learning neural network.

Mirrors the Java DlConfig class — controls hyperparameters like learning rate,
layer sizes, batch size, and heuristic thresholds.
"""

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class DlConfig:
    """Configuration properties for the strategy neural network."""

    # Network architecture
    # Input: 64-dimensional feature vector (32 original + 32 empire-specific)
    # Output: 17 action types (strategic, tactical, economic, defensive)
    input_size: int = 64
    hidden_layer1_size: int = 128
    hidden_layer2_size: int = 256
    hidden_layer3_size: int = 128
    output_size: int = 17

    # Training parameters
    learning_rate: float = 0.001
    batch_size: int = 16
    epochs: int = 10
    dropout_rate: float = 0.3

    # Seed for reproducibility
    seed: int = 42

    # Model persistence
    model_dir: str = "dl-models"

    # Blend factor: 0.0 = pure heuristic, 1.0 = pure neural network
    blend_factor: float = 0.5

    # Memory safeguard settings
    memory_critical_threshold: float = 0.90
    memory_warning_threshold: float = 0.75
    max_experience_buffer_size: int = 200
    gc_before_training: bool = True

    # Heuristic thresholds for AI decision-making
    heuristic_minerals_build_ship: int = 100
    heuristic_energy_build_ship: int = 50
    heuristic_minerals_build_station: int = 200
    heuristic_energy_build_station: int = 100
    heuristic_food_expand_colony: int = 30
    heuristic_max_planets_for_expansion: int = 5
    heuristic_research_tech: int = 50
    heuristic_fleet_engage_combat: int = 3
    heuristic_minerals_retreat: int = 30
    heuristic_fleet_flank_ambush: int = 2
    heuristic_minerals_optimize_mining: int = 100
    heuristic_minerals_fortify: int = 50
    heuristic_minerals_wait: int = 50
    heuristic_energy_wait: int = 25

    def __post_init__(self):
        """Validate configuration values."""
        if not 0.0 <= self.blend_factor <= 1.0:
            self.blend_factor = max(0.0, min(1.0, self.blend_factor))
        if not 0.0 <= self.dropout_rate <= 1.0:
            self.dropout_rate = max(0.0, min(1.0, self.dropout_rate))
