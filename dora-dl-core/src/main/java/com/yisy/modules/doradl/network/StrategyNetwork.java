package com.yisy.modules.doradl.network;

import com.yisy.modules.doradl.config.DlConfig;
import com.yisy.modules.doradl.model.ActionReward;
import com.yisy.modules.doradl.model.GameStateFeatures;
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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Builds, trains, and manages the strategy neural network using Deeplearning4j.
 * The network predicts the best action given a game state feature vector.
 * <p>
 * Network architecture: 64 → 128 → 256 → 128 → 17
 * - Input: 64-dimensional feature vector (32 original + 32 empire-specific)
 * - Hidden layers: 128, 256, 128 neurons with ReLU
 * - Output: 17 action probabilities with Softmax (strategic, tactical, economic, defensive)
 * </p>
 */
@Component
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
     *
     * @param trainingData list of game states, actions, and rewards
     */
    public void train(List<ActionReward> trainingData) {
        if (!initialized) {
            initialize();
        }

        if (trainingData.isEmpty()) {
            log.warn("No training data provided");
            return;
        }

        int numSamples = trainingData.size();
        INDArray input = Nd4j.create(numSamples, config.getInputSize());
        INDArray labels = Nd4j.create(numSamples, config.getOutputSize());

        for (int i = 0; i < numSamples; i++) {
            ActionReward sample = trainingData.get(i);
            double[] features = sample.getState().getFeatures();
            for (int j = 0; j < config.getInputSize(); j++) {
                input.putScalar(i, j, features[j]);
            }
            double[] oneHot = sample.toOneHot();
            for (int j = 0; j < config.getOutputSize(); j++) {
                labels.putScalar(i, j, oneHot[j]);
            }
        }

        network.fit(input, labels);
        log.info("Trained network on {} samples", numSamples);
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
