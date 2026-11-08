package com.yisy.modules.doradl.network;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
import com.yisy.modules.doradl.monitor.MemoryMonitor;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Builds, trains, and manages the strategy neural network using Deeplearning4j.
 * The network predicts the best action given a game state feature vector.
 * <p>
 * This is the local DL4J implementation. When dl.rest-client.enabled=true,
 * the Python microservice handles all neural network operations.
 * </p>
 * <p>
 * Network architecture: 32 → 64 → 128 → 64 → 5 (configurable via DlConfig)
 * - Input: 32-dimensional feature vector representing game state
 * - Hidden layers: 64, 128, 64 neurons with ReLU
 * - Output: 5 action probabilities with Softmax
 * </p>
 */
@Component
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "false", matchIfMissing = true)
public class StrategyNetwork {

    private static final Logger log = LoggerFactory.getLogger(StrategyNetwork.class);

    private final DlConfig config;
    private MultiLayerNetwork network;
    private boolean initialized = false;

    public StrategyNetwork(DlConfig config) {
        this.config = config;
    }

    /**
     * Initializes the neural network with the configured architecture.
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        Nd4j.getRandom().setSeed(config.getSeed());

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(config.getSeed())
                .l2(1e-4)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(config.getInputSize())
                        .nOut(config.getHiddenLayer1Size())
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(config.getHiddenLayer1Size())
                        .nOut(config.getHiddenLayer2Size())
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .nIn(config.getHiddenLayer2Size())
                        .nOut(config.getHiddenLayer3Size())
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .nIn(config.getHiddenLayer3Size())
                        .nOut(config.getOutputSize())
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        network = new MultiLayerNetwork(conf);
        network.init();
        initialized = true;
        log.info("Strategy network initialized with architecture: " +
                config.getInputSize() + " → " +
                config.getHiddenLayer1Size() + " → " +
                config.getHiddenLayer2Size() + " → " +
                config.getHiddenLayer3Size() + " → " +
                config.getOutputSize());
    }

    /**
     * Predicts action probabilities for a given game state.
     *
     * @param features the game state feature vector
     * @return array of 5 probabilities, one per action type
     */
    public double[] predict(GameStateFeatures features) {
        if (!initialized) {
            initialize();
        }

        INDArray input = Nd4j.create(features.getFeatures(), new int[]{1, config.getInputSize()});
        INDArray output = network.output(input, false);

        double[] probabilities = new double[config.getOutputSize()];
        for (int i = 0; i < config.getOutputSize(); i++) {
            probabilities[i] = output.getDouble(0, i);
        }
        return probabilities;
    }

    /**
     * Predicts the best action index for a given game state.
     *
     * @param features the game state feature vector
     * @return the index of the best action (0-4)
     */
    public int predictAction(GameStateFeatures features) {
        double[] probabilities = predict(features);
        int bestAction = 0;
        double bestProbability = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > bestProbability) {
                bestProbability = probabilities[i];
                bestAction = i;
            }
        }
        return bestAction;
    }

    /**
     * Trains the network on a list of action-reward pairs.
     * Uses batched training with explicit memory management to avoid OOM errors.
     *
     * @param trainingData list of game states, actions, and rewards
     */
    public void train(List<ActionReward> trainingData) {
        train(trainingData, null);
    }

    /**
     * Trains the network on a list of action-reward pairs with memory monitoring.
     * Uses batched training with explicit memory management to avoid OOM errors.
     * When a MemoryMonitor is provided, adjusts batch size based on memory pressure
     * and checks memory before each batch.
     *
     * @param trainingData list of game states, actions, and rewards
     * @param memoryMonitor optional memory monitor for adaptive batch sizing
     */
    public void train(List<ActionReward> trainingData, MemoryMonitor memoryMonitor) {
        if (!initialized) {
            initialize();
        }

        if (trainingData.isEmpty()) {
            log.warn("No training data provided");
            return;
        }

        int numSamples = trainingData.size();
        int batchSize = Math.min(config.getBatchSize(), numSamples);

        // Adjust batch size based on memory pressure
        if (memoryMonitor != null) {
            batchSize = memoryMonitor.suggestBatchSize(batchSize);
            log.debug("Memory-aware batch size: {} (original: {})", batchSize, config.getBatchSize());
        }

        // Build full input/label arrays once
        INDArray allInput = Nd4j.create(numSamples, config.getInputSize());
        INDArray allLabels = Nd4j.create(numSamples, config.getOutputSize());

        for (int i = 0; i < numSamples; i++) {
            ActionReward sample = trainingData.get(i);
            double[] features = sample.getState().getFeatures();
            for (int j = 0; j < config.getInputSize(); j++) {
                allInput.putScalar(i, j, features[j]);
            }
            double[] oneHot = sample.toOneHot();
            for (int j = 0; j < config.getOutputSize(); j++) {
                allLabels.putScalar(i, j, oneHot[j]);
            }
        }

        // Train in batches to reduce memory pressure
        int batchesTrained = 0;
        for (int offset = 0; offset < numSamples; offset += batchSize) {
            // Check memory before each batch
            if (memoryMonitor != null && !memoryMonitor.isTrainingSafe()) {
                log.warn("Memory critical at batch {}/{} — stopping training early to avoid OOM",
                        batchesTrained + 1, (numSamples + batchSize - 1) / batchSize);
                break;
            }

            int end = Math.min(offset + batchSize, numSamples);

            INDArray batchInput = allInput.get(NDArrayIndex.interval(offset, end - 1));
            INDArray batchLabels = allLabels.get(NDArrayIndex.interval(offset, end - 1));

            try {
                network.fit(batchInput, batchLabels);
                batchesTrained++;
            } finally {
                // Explicitly release batch memory after each batch
                batchInput.close();
                batchLabels.close();
            }
        }

        // Clear full arrays after training completes
        allInput.close();
        allLabels.close();

        log.info("Trained network on {} samples in {} batches (batch size: {})",
                numSamples, batchesTrained, batchSize);
    }

    /**
     * Saves the trained model to disk.
     *
     * @return the file path where the model was saved
     */
    public String saveModel() throws IOException {
        if (!initialized) {
            throw new IllegalStateException("Network not initialized");
        }

        File modelDir = new File(config.getModelDir());
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }

        File modelFile = new File(modelDir, "strategy-network.zip");
        ModelSerializer.writeModel(network, modelFile, true);
        log.info("Model saved to {}", modelFile.getAbsolutePath());
        return modelFile.getAbsolutePath();
    }

    /**
     * Loads a trained model from disk.
     *
     * @param modelPath the path to the saved model file
     */
    public void loadModel(String modelPath) throws IOException {
        network = ModelSerializer.restoreMultiLayerNetwork(new File(modelPath));
        initialized = true;
        log.info("Model loaded from {}", modelPath);
    }

    /**
     * Returns whether the network is initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the underlying network instance.
     */
    public MultiLayerNetwork getNetwork() {
        return network;
    }
}
