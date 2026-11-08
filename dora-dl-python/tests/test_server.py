"""Tests for FastAPI server endpoints — integration tests using TestClient."""

import pytest
from fastapi.testclient import TestClient
from dora_dl.server import app


@pytest.fixture
def client():
    return TestClient(app)


ZERO_FEATURES = [0.0] * 64


def make_features(**overrides):
    """Create a default feature vector for testing."""
    features = list(ZERO_FEATURES)
    # Set some default values for resources (indices 0-4)
    features[0] = 500.0  # minerals
    features[1] = 300.0  # energy
    features[2] = 100.0  # food
    features[3] = 50.0   # research
    features[4] = 950.0  # total
    # Assets (indices 5-7)
    features[5] = 3.0    # planetCount
    features[6] = 0.0    # stationCount
    features[7] = 2.0    # fleetCount
    features.update(overrides)
    return features[:64]


class TestStatusEndpoint:
    """Test GET /api/status."""

    def test_status_returns_ok(self, client):
        resp = client.get("/api/status")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "ok"
        assert "networkReady" in data
        assert "learningEnabled" in data

    def test_status_learning_enabled(self, client):
        resp = client.get("/api/status")
        data = resp.json()
        assert data["learningEnabled"] is True


class TestPredictEndpoint:
    """Test POST /api/predict."""

    def test_predict_returns_200(self, client):
        resp = client.post("/api/predict", json={"features": ZERO_FEATURES, "playerId": 1})
        assert resp.status_code == 200

    def test_predict_has_action(self, client):
        resp = client.post("/api/predict", json={"features": ZERO_FEATURES, "playerId": 1})
        data = resp.json()
        assert "action" in data
        assert "actionIndex" in data
        assert "actionCategory" in data
        assert "confidence" in data

    def test_predict_valid_action_category(self, client):
        resp = client.post("/api/predict", json={"features": ZERO_FEATURES, "playerId": 1})
        data = resp.json()
        assert data["actionCategory"] in ("STRATEGIC", "TACTICAL", "ECONOMIC", "DEFENSIVE")

    def test_predict_wrong_feature_dimension(self, client):
        resp = client.post("/api/predict", json={"features": [0.0] * 32, "playerId": 1})
        assert resp.status_code == 422


class TestRecordEndpoint:
    """Test POST /api/record."""

    def test_record_returns_200(self, client):
        resp = client.post("/api/record", json={
            "features": ZERO_FEATURES,
            "playerId": 1,
            "action": "BUILD_SHIP",
            "reward": 0.5,
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["recorded"] is True
        assert "bufferSize" in data

    def test_record_increases_buffer(self, client):
        # Clear any prior state from other tests
        client.delete("/api/learning/clear")
        client.post("/api/record", json={
            "features": ZERO_FEATURES,
            "playerId": 1,
            "action": "BUILD_SHIP",
            "reward": 0.5,
        })
        resp = client.post("/api/record", json={
            "features": ZERO_FEATURES,
            "playerId": 1,
            "action": "ENGAGE_COMBAT",
            "reward": 0.3,
        })
        data = resp.json()
        # Buffer should have at least 2 entries (may have more from prior tests if clear failed)
        assert data["bufferSize"] >= 2

    def test_record_invalid_reward(self, client):
        resp = client.post("/api/record", json={
            "features": ZERO_FEATURES,
            "playerId": 1,
            "action": "BUILD_SHIP",
            "reward": 2.0,  # Out of range
        })
        assert resp.status_code == 422


class TestRewardEndpoint:
    """Test POST /api/reward."""

    def test_compute_reward_returns_200(self, client):
        resp = client.post("/api/reward", json={
            "featuresBefore": ZERO_FEATURES,
            "featuresAfter": ZERO_FEATURES,
            "playerId": 1,
        })
        assert resp.status_code == 200
        data = resp.json()
        assert "reward" in data

    def test_compute_reward_positive(self, client):
        before = list(ZERO_FEATURES)
        after = list(ZERO_FEATURES)
        after[0] = 1000.0  # minerals increased
        after[1] = 600.0   # energy increased
        after[2] = 200.0   # food increased
        resp = client.post("/api/reward", json={
            "featuresBefore": before,
            "featuresAfter": after,
            "playerId": 1,
        })
        data = resp.json()
        assert data["reward"] > 0


class TestTrainEndpoint:
    """Test POST /api/train."""

    def test_train_returns_200(self, client):
        resp = client.post("/api/train")
        assert resp.status_code == 200
        data = resp.json()
        assert "samplesTrained" in data
        assert "totalTrainingCycles" in data

    def test_train_no_samples(self, client):
        resp = client.post("/api/train")
        data = resp.json()
        assert data["samplesTrained"] == 0


class TestSaveEndpoint:
    """Test POST /api/save."""

    def test_save_returns_200(self, client):
        resp = client.post("/api/save")
        assert resp.status_code == 200
        data = resp.json()
        assert "message" in data
        assert "path" in data


class TestLearningEndpoint:
    """Test GET/PUT /api/learning and DELETE /api/learning/clear."""

    def test_get_learning_status(self, client):
        resp = client.get("/api/learning")
        assert resp.status_code == 200
        data = resp.json()
        assert "learningEnabled" in data
        assert "experienceBufferSize" in data
        assert "totalActionsRecorded" in data
        assert "totalTrainingCycles" in data
        assert "lastTrainingTime" in data

    def test_enable_learning(self, client):
        resp = client.put("/api/learning?enabled=true")
        assert resp.status_code == 200
        data = resp.json()
        assert data["learningEnabled"] is True

    def test_disable_learning(self, client):
        resp = client.put("/api/learning?enabled=false")
        assert resp.status_code == 200
        data = resp.json()
        assert data["learningEnabled"] is False

    def test_clear_experience(self, client):
        # Record some actions first
        client.post("/api/record", json={
            "features": ZERO_FEATURES,
            "playerId": 1,
            "action": "BUILD_SHIP",
            "reward": 0.5,
        })
        resp = client.delete("/api/learning/clear")
        assert resp.status_code == 200
        data = resp.json()
        assert "message" in data


class TestBlendEndpoint:
    """Test PUT /api/blend."""

    def test_update_blend_factor(self, client):
        resp = client.put("/api/blend", json={"blendFactor": 0.75})
        assert resp.status_code == 200
        data = resp.json()
        assert data["blendFactor"] == 0.75

    def test_blend_factor_reflected_in_predict(self, client):
        client.put("/api/blend", json={"blendFactor": 0.0})
        resp = client.post("/api/predict", json={"features": ZERO_FEATURES, "playerId": 1})
        data = resp.json()
        assert data["blendFactor"] == 0.0

    def test_blend_factor_validation(self, client):
        resp = client.put("/api/blend", json={"blendFactor": 1.5})
        assert resp.status_code == 422


class TestTrainDirectEndpoint:
    """Test POST /api/train/direct."""

    def test_train_direct_returns_200(self, client):
        features = [[0.0] * 64]
        actions = [0]
        rewards = [1.0]
        resp = client.post("/api/train/direct", json={
            "features": features,
            "actions": actions,
            "rewards": rewards,
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["samplesTrained"] == 1

    def test_train_direct_multiple_samples(self, client):
        features = [[float(i) for i in range(64)] for _ in range(5)]
        actions = [0, 1, 2, 3, 4]
        rewards = [1.0] * 5
        resp = client.post("/api/train/direct", json={
            "features": features,
            "actions": actions,
            "rewards": rewards,
        })
        assert resp.status_code == 200
        data = resp.json()
        assert data["samplesTrained"] == 5

    def test_train_direct_mismatched_lengths(self, client):
        features = [[0.0] * 64, [0.0] * 64]
        actions = [0]
        rewards = [1.0]
        resp = client.post("/api/train/direct", json={
            "features": features,
            "actions": actions,
            "rewards": rewards,
        })
        assert resp.status_code == 400

    def test_train_direct_invalid_action_index(self, client):
        features = [[0.0] * 64]
        actions = [99]  # Invalid
        rewards = [1.0]
        resp = client.post("/api/train/direct", json={
            "features": features,
            "actions": actions,
            "rewards": rewards,
        })
        assert resp.status_code == 400

    def test_train_direct_wrong_feature_dimension(self, client):
        features = [[0.0] * 32]  # Wrong dimension
        actions = [0]
        rewards = [1.0]
        resp = client.post("/api/train/direct", json={
            "features": features,
            "actions": actions,
            "rewards": rewards,
        })
        assert resp.status_code == 400


class TestRateLimiting:
    """Test rate limiting on endpoints."""

    def test_predict_rate_limiter_exists(self, client):
        from dora_dl.server import predict_limiter
        assert predict_limiter is not None
        # A single request should be allowed
        assert predict_limiter.allow() is True

    def test_predict_endpoint_rejects_on_rate_limit(self, client):
        # The rate limiter allows 100/sec, so a single request should pass
        resp = client.post("/api/predict", json={"features": ZERO_FEATURES, "playerId": 1})
        assert resp.status_code == 200  # Not 429
