package com.yisy.modules.doradl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the deep learning neural network.
 * Controls hyperparameters like learning rate, layer sizes, and batch size.
 */
@Configuration
@ConfigurationProperties(prefix = "dl")
public class DlConfig {

    // Network architecture
    // Input: 64-dimensional feature vector (32 original + 32 empire-specific)
    // Output: 17 action types (strategic, tactical, economic, defensive)
    private int inputSize = 64;
    private int hiddenLayer1Size = 128;
    private int hiddenLayer2Size = 256;
    private int hiddenLayer3Size = 128;
    private int outputSize = 17;

    // Training parameters
    private double learningRate = 0.001;
    private int batchSize = 32;
    private int epochs = 10;
    private double dropoutRate = 0.3;

    // Seed for reproducibility
    private int seed = 42;

    // Model persistence
    private String modelDir = "dl-models";

    // Blend factor: 0.0 = pure heuristic, 1.0 = pure neural network
    private double blendFactor = 0.5;

    public int getInputSize() {
        return inputSize;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    public int getHiddenLayer1Size() {
        return hiddenLayer1Size;
    }

    public void setHiddenLayer1Size(int hiddenLayer1Size) {
        this.hiddenLayer1Size = hiddenLayer1Size;
    }

    public int getHiddenLayer2Size() {
        return hiddenLayer2Size;
    }

    public void setHiddenLayer2Size(int hiddenLayer2Size) {
        this.hiddenLayer2Size = hiddenLayer2Size;
    }

    public int getHiddenLayer3Size() {
        return hiddenLayer3Size;
    }

    public void setHiddenLayer3Size(int hiddenLayer3Size) {
        this.hiddenLayer3Size = hiddenLayer3Size;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(int outputSize) {
        this.outputSize = outputSize;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public double getDropoutRate() {
        return dropoutRate;
    }

    public void setDropoutRate(double dropoutRate) {
        this.dropoutRate = dropoutRate;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public String getModelDir() {
        return modelDir;
    }

    public void setModelDir(String modelDir) {
        this.modelDir = modelDir;
    }

    public double getBlendFactor() {
        return blendFactor;
    }

    public void setBlendFactor(double blendFactor) {
        this.blendFactor = blendFactor;
    }
}
