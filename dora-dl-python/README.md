# DoraMCU Deep Learning AI Service

Python microservice that replaces the `dora-dl-core` Java module, providing the same neural network game strategy prediction via a REST API.

## Architecture

The original `dora-dl-core` used **DeepLearning4j** (DL4J) for neural network training and inference. This Python service replaces it with **PyTorch**, offering:

- Superior ML ecosystem and community support
- Easier model training and debugging
- Lower memory footprint (no JVM overhead for DL4J native libraries)
- Standard PyTorch model format (`.pt` instead of DL4J `.zip`)

## Components

| File | Description |
|------|-------------|
| `config.py` | Neural network hyperparameters (mirrors `DlConfig.java`) |
| `models.py` | `GameStateFeatures`, `ActionReward`, `Action` enum |
| `network.py` | PyTorch `StrategyNetwork` (replaces DL4J `MultiLayerNetwork`) |
| `feature_extractor.py` | Converts game state to 64-dim feature vectors |
| `dl_game_ai_service.py` | Blends NN predictions with heuristic scores |
| `learning_service.py` | Experience buffer, reward computation, training loop |
| `server.py` | FastAPI REST API (port 8083) |

## API Endpoints

The service runs on **port 8083** by default.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/status` | Health check |
| `POST` | `/api/predict` | Predict best action for a game state |
| `POST` | `/api/record` | Record an action-reward experience |
| `POST` | `/api/reward` | Compute reward from before/after states |
| `POST` | `/api/train` | Train on accumulated experience |
| `POST` | `/api/save` | Save current model |
| `GET` | `/api/learning` | Get learning statistics |
| `PUT` | `/api/learning` | Enable/disable learning |
| `DELETE` | `/api/learning/clear` | Clear experience buffer |
| `PUT` | `/api/blend` | Update blend factor |
| `POST` | `/api/train/direct` | Train on direct feature vectors |

## Running Locally

```bash
# Install dependencies
cd dora-dl-python
pip install -e .

# Run the service
python -m dora_dl
```

Or with `uv`:

```bash
cd dora-dl-python
uv run python -m dora_dl
```

## Running with Docker

```bash
docker build -f Dockerfile.dl -t dora-dl:latest .
docker run -p 8083:8083 -v $(pwd)/dl-models:/app/dl-models dora-dl:latest
```

## Docker Compose Integration

Add to `docker-compose.yml`:

```yaml
dl:
  build:
    context: ./dora-dl-python
    dockerfile: Dockerfile.dl
  container_name: doramcu-dl
  ports:
    - "8083:8083"
  volumes:
    - ./dl-models:/app/dl-models
  environment:
    - DL_BLEND_FACTOR=0.5
    - DL_MODEL_DIR=/app/dl-models
  restart: unless-stopped
```

## Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `DORA_DL_PORT` | `8083` | Service port |
| `DL_MODEL_DIR` | `dl-models` | Model persistence directory |
| `DL_BLEND_FACTOR` | `0.5` | NN vs heuristic blend (0.0-1.0) |

## Model Format

Models are saved as PyTorch checkpoints (`.pt`) containing:
- Network state dict (weights and biases)
- Architecture configuration (layer sizes)

To convert from the old DL4J `.zip` format, retrain from scratch — the architectures are equivalent but the weight formats are incompatible.

## Integration with Java Backend

The Java `AiPlayerService` and `AdminController` call this service via HTTP instead of direct Spring bean injection. A `DlRestClient` proxy class handles the REST calls, providing the same interface as the original `DlGameAiService` and `DoraAiLearningService`.
