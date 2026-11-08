"""PyTorch strategy neural network.

Replaces the DL4J MultiLayerNetwork with a PyTorch implementation.
Architecture: input → hidden1 (ReLU) → hidden2 (ReLU) → hidden3 (ReLU) → output (logits)
Note: Softmax is applied only at inference time; training uses raw logits with CrossEntropyLoss.
"""

import glob
import logging
import os
from typing import List, Optional

import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim

from dora_dl.config import DlConfig
from dora_dl.models import ActionReward, GameStateFeatures

log = logging.getLogger(__name__)


class StrategyNet(nn.Module):
    """PyTorch neural network for game strategy prediction."""

    def __init__(self, config: DlConfig):
        super().__init__()
        self.config = config
        self.network = nn.Sequential(
            nn.Linear(config.input_size, config.hidden_layer1_size),
            nn.ReLU(),
            nn.Linear(config.hidden_layer1_size, config.hidden_layer2_size),
            nn.ReLU(),
            nn.Linear(config.hidden_layer2_size, config.hidden_layer3_size),
            nn.ReLU(),
            nn.Linear(config.hidden_layer3_size, config.output_size),
        )

    def forward(self, x):
        return self.network(x)


class StrategyNetwork:
    """Manages the strategy neural network lifecycle: init, train, predict, save, load."""

    def __init__(self, config: DlConfig):
        self.config = config
        self.model: Optional[StrategyNet] = None
        self.initialized = False
        self.device = torch.device("cpu")

    def initialize(self) -> None:
        if self.initialized:
            return

        torch.manual_seed(self.config.seed)
        np.random.seed(self.config.seed)

        self.model = StrategyNet(self.config).to(self.device)
        self.initialized = True
        log.info(
            "Strategy network initialized: %d → %d → %d → %d → %d",
            self.config.input_size,
            self.config.hidden_layer1_size,
            self.config.hidden_layer2_size,
            self.config.hidden_layer3_size,
            self.config.output_size,
        )

    def predict(self, features: GameStateFeatures) -> List[float]:
        if not self.initialized:
            self.initialize()

        self.model.eval()
        input_tensor = torch.tensor(
            [features.to_list()], dtype=torch.float32, device=self.device
        )
        with torch.no_grad():
            logits = self.model(input_tensor)
            probs = torch.softmax(logits, dim=1)[0].tolist()
        return probs

    def predict_action(self, features: GameStateFeatures) -> int:
        probs = self.predict(features)
        best_action = int(np.argmax(probs))
        return best_action

    def train(self, training_data: List[ActionReward]) -> None:
        if not self.initialized:
            self.initialize()

        if not training_data:
            log.warning("No training data provided")
            return

        num_samples = len(training_data)
        batch_size = min(self.config.batch_size, num_samples)

        # Build input/label arrays
        all_input = np.zeros((num_samples, self.config.input_size), dtype=np.float32)
        all_labels = np.zeros((num_samples, self.config.output_size), dtype=np.float32)

        for i, sample in enumerate(training_data):
            all_input[i] = sample.state.to_list()
            one_hot = sample.to_one_hot()
            all_labels[i] = one_hot

        input_tensor = torch.tensor(all_input, device=self.device)
        label_tensor = torch.tensor(all_labels, device=self.device)

        optimizer = optim.Adam(self.model.parameters(), lr=self.config.learning_rate)
        criterion = nn.CrossEntropyLoss()

        self.model.train()
        batches_trained = 0

        for offset in range(0, num_samples, batch_size):
            end = min(offset + batch_size, num_samples)
            batch_input = input_tensor[offset:end]
            # CrossEntropyLoss expects class indices, not one-hot
            batch_labels = torch.argmax(label_tensor[offset:end], dim=1)

            optimizer.zero_grad()
            outputs = self.model(batch_input)
            loss = criterion(outputs, batch_labels)
            loss.backward()
            optimizer.step()
            batches_trained += 1

        log.info(
            "Trained network on %d samples in %d batches (batch size: %d)",
            num_samples, batches_trained, batch_size,
        )

    def save_model(self) -> str:
        if not self.initialized:
            raise RuntimeError("Network not initialized")

        model_dir = self.config.model_dir
        os.makedirs(model_dir, exist_ok=True)
        
        # Versioned model with timestamp
        import time
        timestamp = int(time.time())
        versioned_path = os.path.join(model_dir, f"strategy-network-{timestamp}.pt")
        
        torch.save(
            {
                "model_state_dict": self.model.state_dict(),
                "config": {
                    "input_size": self.config.input_size,
                    "hidden_layer1_size": self.config.hidden_layer1_size,
                    "hidden_layer2_size": self.config.hidden_layer2_size,
                    "hidden_layer3_size": self.config.hidden_layer3_size,
                    "output_size": self.config.output_size,
                },
                "timestamp": timestamp,
            },
            versioned_path,
        )
        
        # Update the default symlink/path
        model_path = os.path.join(model_dir, "strategy-network.pt")
        torch.save(
            {
                "model_state_dict": self.model.state_dict(),
                "config": {
                    "input_size": self.config.input_size,
                    "hidden_layer1_size": self.config.hidden_layer1_size,
                    "hidden_layer2_size": self.config.hidden_layer2_size,
                    "hidden_layer3_size": self.config.hidden_layer3_size,
                    "output_size": self.config.output_size,
                },
                "timestamp": timestamp,
            },
            model_path,
        )
        
        # Keep only the last 5 checkpoints
        self._cleanup_old_models(model_dir)
        
        log.info("Model saved to %s (versioned: %s)", model_path, versioned_path)
        return model_path

    def _cleanup_old_models(self, model_dir: str) -> None:
        """Keep only the last 5 versioned checkpoints."""
        pattern = os.path.join(model_dir, "strategy-network-*.pt")
        models = sorted(glob.glob(pattern))
        max_checkpoints = 5
        while len(models) > max_checkpoints:
            oldest = models.pop(0)
            os.remove(oldest)
            log.info("Removed old checkpoint: %s", oldest)

    def load_model(self, model_path: str) -> None:
        checkpoint = torch.load(model_path, map_location=self.device, weights_only=False)

        config = checkpoint.get("config", {})
        self.config.input_size = config.get("input_size", self.config.input_size)
        self.config.hidden_layer1_size = config.get(
            "hidden_layer1_size", self.config.hidden_layer1_size
        )
        self.config.hidden_layer2_size = config.get(
            "hidden_layer2_size", self.config.hidden_layer2_size
        )
        self.config.hidden_layer3_size = config.get(
            "hidden_layer3_size", self.config.hidden_layer3_size
        )
        self.config.output_size = config.get("output_size", self.config.output_size)

        self.model = StrategyNet(self.config).to(self.device)
        self.model.load_state_dict(checkpoint["model_state_dict"])
        self.initialized = True
        log.info("Model loaded from %s", model_path)

    @property
    def is_initialized(self) -> bool:
        return self.initialized
