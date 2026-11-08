"""Tests for DlConfig dataclass — defaults, validation, and clamping."""

import pytest
from dora_dl.config import DlConfig


class TestDlConfigDefaults:
    """Verify default configuration values match the Java DlConfig."""

    def test_default_input_size(self):
        config = DlConfig()
        assert config.input_size == 64

    def test_default_output_size(self):
        config = DlConfig()
        assert config.output_size == 17

    def test_default_hidden_layers(self):
        config = DlConfig()
        assert config.hidden_layer1_size == 128
        assert config.hidden_layer2_size == 256
        assert config.hidden_layer3_size == 128

    def test_default_training_params(self):
        config = DlConfig()
        assert config.learning_rate == 0.001
        assert config.batch_size == 16
        assert config.epochs == 10
        assert config.dropout_rate == 0.3

    def test_default_seed(self):
        config = DlConfig()
        assert config.seed == 42

    def test_default_model_dir(self):
        config = DlConfig()
        assert config.model_dir == "dl-models"

    def test_default_blend_factor(self):
        config = DlConfig()
        assert config.blend_factor == 0.5

    def test_default_memory_thresholds(self):
        config = DlConfig()
        assert config.memory_critical_threshold == 0.90
        assert config.memory_warning_threshold == 0.75
        assert config.max_experience_buffer_size == 200
        assert config.gc_before_training is True

    def test_default_heuristic_thresholds(self):
        config = DlConfig()
        assert config.heuristic_minerals_build_ship == 100
        assert config.heuristic_energy_build_ship == 50
        assert config.heuristic_minerals_build_station == 200
        assert config.heuristic_energy_build_station == 100
        assert config.heuristic_food_expand_colony == 30
        assert config.heuristic_max_planets_for_expansion == 5
        assert config.heuristic_research_tech == 50
        assert config.heuristic_fleet_engage_combat == 3
        assert config.heuristic_minerals_retreat == 30
        assert config.heuristic_fleet_flank_ambush == 2
        assert config.heuristic_minerals_optimize_mining == 100
        assert config.heuristic_minerals_fortify == 50
        assert config.heuristic_minerals_wait == 50
        assert config.heuristic_energy_wait == 25


class TestDlConfigValidation:
    """Verify __post_init__ clamps invalid values."""

    def test_blend_factor_clamped_high(self):
        config = DlConfig(blend_factor=2.0)
        assert config.blend_factor == 1.0

    def test_blend_factor_clamped_low(self):
        config = DlConfig(blend_factor=-0.5)
        assert config.blend_factor == 0.0

    def test_dropout_rate_clamped_high(self):
        config = DlConfig(dropout_rate=1.5)
        assert config.dropout_rate == 1.0

    def test_dropout_rate_clamped_low(self):
        config = DlConfig(dropout_rate=-0.2)
        assert config.dropout_rate == 0.0

    def test_valid_blend_factor_preserved(self):
        config = DlConfig(blend_factor=0.75)
        assert config.blend_factor == 0.75

    def test_valid_dropout_rate_preserved(self):
        config = DlConfig(dropout_rate=0.5)
        assert config.dropout_rate == 0.5

    def test_custom_values(self):
        config = DlConfig(
            input_size=32,
            output_size=10,
            learning_rate=0.01,
            batch_size=32,
            seed=123,
            model_dir="/tmp/models",
        )
        assert config.input_size == 32
        assert config.output_size == 10
        assert config.learning_rate == 0.01
        assert config.batch_size == 32
        assert config.seed == 123
        assert config.model_dir == "/tmp/models"
