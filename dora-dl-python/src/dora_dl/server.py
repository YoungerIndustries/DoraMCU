"""FastAPI REST API for the DoraMCU Deep Learning AI service.

Exposes the neural network prediction, training, and learning management
as REST endpoints that the Java Spring Boot backend can call.

Endpoints:
    POST /api/predict        — Predict best action for a game state
    POST /api/record         — Record an action-reward experience
    POST /api/reward         — Compute reward from before/after states
    POST /api/train          — Trigger training on accumulated experience
    POST /api/save           — Save the current model
    GET  /api/learning       — Get learning status and statistics
    PUT  /api/learning        — Enable/disable learning
    DELETE /api/learning/clear — Clear experience buffer
    PUT  /api/blend          — Update blend factor
    GET  /api/status         — Check service health
"""

import logging
import os
import time
import threading
from collections import deque
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

import uvicorn

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, field_validator

from dora_dl.config import DlConfig
from dora_dl.dl_game_ai_service import DlGameAiService
from dora_dl.feature_extractor import GameStateFeatureExtractor
from dora_dl.learning_service import DoraAiLearningService
from dora_dl.network import StrategyNetwork

log = logging.getLogger(__name__)

# ---- Rate limiter ----

class RateLimiter:
    """Simple token bucket rate limiter."""

    def __init__(self, max_requests: int, window_seconds: float):
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        self.timestamps: deque = deque()
        self.lock = threading.Lock()

    def allow(self) -> bool:
        now = time.time()
        with self.lock:
            # Remove old timestamps
            while self.timestamps and self.timestamps[0] <= now - self.window_seconds:
                self.timestamps.popleft()
            if len(self.timestamps) < self.max_requests:
                self.timestamps.append(now)
                return True
            return False


# Rate limiters for DL service endpoints
predict_limiter = RateLimiter(100, 1.0)  # 100 predictions/sec
record_limiter = RateLimiter(200, 1.0)   # 200 records/sec
train_limiter = RateLimiter(5, 1.0)      # 5 training cycles/sec


# ---- Pydantic request/response models ----


class PredictRequest(BaseModel):
    """Prediction request with pre-extracted feature vector."""
    features: List[float]
    playerId: int = Field(ge=0)

    model_config = {"json_schema_extra": {
        "examples": [{"features": [0.0] * 64, "playerId": 1}]
    }}

    @field_validator("features")
    @classmethod
    def validate_features(cls, v: List[float]) -> List[float]:
        if len(v) != 64:
            raise ValueError(f"features must have exactly 64 elements, got {len(v)}")
        return v


class RecordRequest(BaseModel):
    """Record experience with pre-extracted feature vector."""
    features: List[float]
    playerId: int = Field(ge=0)
    action: str
    reward: float = Field(ge=-1.0, le=1.0)

    model_config = {"json_schema_extra": {
        "examples": [{"features": [0.0] * 64, "playerId": 1, "action": "BUILD_SHIP", "reward": 0.5}]
    }}

    @field_validator("features")
    @classmethod
    def validate_features(cls, v: List[float]) -> List[float]:
        if len(v) != 64:
            raise ValueError(f"features must have exactly 64 elements, got {len(v)}")
        return v


class RewardRequest(BaseModel):
    """Compute reward from before/after feature vectors."""
    featuresBefore: List[float]
    featuresAfter: List[float]
    playerId: int = Field(ge=0)

    model_config = {"json_schema_extra": {
        "examples": [{"featuresBefore": [0.0] * 64, "featuresAfter": [0.0] * 64, "playerId": 1}]
    }}

    @field_validator("featuresBefore", "featuresAfter")
    @classmethod
    def validate_features(cls, v: List[float]) -> List[float]:
        if len(v) != 64:
            raise ValueError(f"features must have exactly 64 elements, got {len(v)}")
        return v


class BlendRequest(BaseModel):
    blendFactor: float = Field(ge=0.0, le=1.0)

    @field_validator("blendFactor")
    @classmethod
    def validate_blend_factor(cls, v: float) -> float:
        if not (0.0 <= v <= 1.0):
            raise ValueError("blendFactor must be between 0.0 and 1.0")
        return v


class TrainingDataRequest(BaseModel):
    """Direct training data submission with feature vectors."""
    features: List[List[float]]
    actions: List[int]
    rewards: List[float]


# ---- App setup ----

app = FastAPI(
    title="DoraMCU Deep Learning AI Service",
    description="PyTorch neural network for game strategy prediction",
    version="0.1.0",
)

# Global service instances
config = DlConfig(
    model_dir=os.getenv("DL_MODEL_DIR", "dl-models"),
    blend_factor=float(os.getenv("DL_BLEND_FACTOR", "0.5")),
)
network = StrategyNetwork(config)
extractor = GameStateFeatureExtractor()
dl_service = DlGameAiService(network, extractor, config)
learning_service = DoraAiLearningService(network, extractor, config)


def main():
    """Entry point for uvicorn."""
    port = int(os.getenv("DORA_DL_PORT", "8083"))
    log.info("Starting DoraMCU DL service on port %d", port)
    
    # Start scheduled training background task
    scheduled_training_thread = threading.Thread(
        target=_scheduled_training_loop, daemon=True
    )
    scheduled_training_thread.start()
    log.info("Scheduled training loop started (every 60s)")
    
    uvicorn.run(app, host="0.0.0.0", port=port)


def _scheduled_training_loop():
    """Periodically trains the model on accumulated experience (every 60s)."""
    while True:
        time.sleep(60)
        try:
            if learning_service.learning_enabled and learning_service.experience_buffer_size > 0:
                trained = learning_service.train_on_accumulated_experience()
                if trained > 0:
                    log.info("Scheduled training: trained on %d samples", trained)
        except Exception as e:
            log.warning("Scheduled training failed: %s", e)


# ---- Health check ----


@app.get("/api/status")
def status():
    """Check service health and network readiness."""
    return {
        "status": "ok",
        "networkReady": dl_service.is_network_ready,
        "learningEnabled": learning_service.learning_enabled,
    }


# ---- Prediction ----


@app.post("/api/predict")
def predict(request: PredictRequest):
    """Predict the best action for a given feature vector and player."""
    if not predict_limiter.allow():
        raise HTTPException(status_code=429, detail="Rate limit exceeded for predictions")
    from dora_dl.models import GameStateFeatures
    try:
        features = GameStateFeatures(request.features)
        result = dl_service.predict_action_from_features(features, request.playerId)
        return JSONResponse(content=result)
    except Exception as e:
        log.error("Prediction failed: %s", e, exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# ---- Learning ----


@app.post("/api/record")
def record(request: RecordRequest):
    """Record an action-reward experience for training."""
    if not record_limiter.allow():
        raise HTTPException(status_code=429, detail="Rate limit exceeded for recording")
    from dora_dl.models import GameStateFeatures
    features = GameStateFeatures(request.features)
    learning_service.record_action_from_features(
        features, request.playerId, request.action, request.reward
    )
    return {
        "recorded": True,
        "bufferSize": learning_service.experience_buffer_size,
    }


@app.post("/api/reward")
def compute_reward(request: RewardRequest):
    """Compute reward from before/after feature vectors."""
    from dora_dl.models import GameStateFeatures
    features_before = GameStateFeatures(request.featuresBefore)
    features_after = GameStateFeatures(request.featuresAfter)
    reward = learning_service.compute_reward_from_features(
        features_before, features_after, request.playerId
    )
    return {"reward": reward}


@app.post("/api/train")
def trigger_training():
    """Trigger training on accumulated experience."""
    if not train_limiter.allow():
        raise HTTPException(status_code=429, detail="Rate limit exceeded for training")
    trained = learning_service.train_on_accumulated_experience()
    return {
        "samplesTrained": trained,
        "totalTrainingCycles": learning_service.total_training_cycles,
    }


@app.post("/api/save")
def save_model():
    """Force save the current model."""
    try:
        path = learning_service.save_model()
        return {"message": "Model saved successfully", "path": path}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/learning")
def get_learning_status():
    """Get learning status and statistics."""
    last_training = learning_service.last_training_time
    return {
        "learningEnabled": learning_service.learning_enabled,
        "experienceBufferSize": learning_service.experience_buffer_size,
        "totalActionsRecorded": learning_service.total_actions_recorded,
        "totalTrainingCycles": learning_service.total_training_cycles,
        "lastTrainingTime": (
            datetime.fromtimestamp(last_training / 1000, tz=timezone.utc).isoformat()
            if last_training > 0
            else "Never"
        ),
    }


@app.put("/api/learning")
def update_learning(enabled: bool = True):
    """Enable or disable learning."""
    learning_service.learning_enabled = enabled
    return {
        "message": f"Learning {'enabled' if enabled else 'disabled'}",
        "learningEnabled": learning_service.learning_enabled,
    }


@app.delete("/api/learning/clear")
def clear_experience():
    """Clear all accumulated experience."""
    learning_service.clear_experience()
    return {"message": "Experience buffer cleared"}


# ---- Blend factor ----


@app.put("/api/blend")
def update_blend(request: BlendRequest):
    """Update the blend factor between NN and heuristic scores."""
    dl_service.blend_factor = request.blendFactor
    return {
        "blendFactor": dl_service.blend_factor,
        "message": f"Blend factor set to {dl_service.blend_factor}",
    }


# ---- Direct training (for batch data) ----


@app.post("/api/train/direct")
def train_direct(request: TrainingDataRequest):
    """Train directly on provided feature vectors and action labels."""
    from dora_dl.models import Action, ActionReward, GameStateFeatures

    if len(request.features) != len(request.actions):
        raise HTTPException(
            status_code=400,
            detail="features and actions must have the same length",
        )

    try:
        samples = []
        for i in range(len(request.features)):
            state = GameStateFeatures(request.features[i])
            action = Action.from_index(request.actions[i])
            reward = request.rewards[i] if i < len(request.rewards) else 0.0
            samples.append(ActionReward(state, action, reward))
    except (ValueError, IndexError) as e:
        raise HTTPException(status_code=400, detail=str(e))

    if not network.is_initialized:
        network.initialize()

    network.train(samples)
    network.save_model()

    return {
        "samplesTrained": len(samples),
        "message": f"Trained on {len(samples)} samples",
    }
