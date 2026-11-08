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
    private int batchSize = 16;
    private int epochs = 10;
    private double dropoutRate = 0.3;

    // Seed for reproducibility
    private int seed = 42;

    // Model persistence
    private String modelDir = "dl-models";

    // Blend factor: 0.0 = pure heuristic, 1.0 = pure neural network
    private double blendFactor = 0.5;

    // Memory safeguard settings
    /** Heap usage fraction (0..1) at which training is skipped. Default 0.90. */
    private double memoryCriticalThreshold = 0.90;

    /** Heap usage fraction (0..1) at which batch size is halved. Default 0.75. */
    private double memoryWarningThreshold = 0.75;

    /** Maximum experience buffer size to prevent unbounded memory growth. Default 200. */
    private int maxExperienceBufferSize = 200;

    /** Enable periodic GC before training when memory is elevated. Default true. */
    private boolean gcBeforeTraining = true;

    // Heuristic thresholds for AI decision-making
    private int heuristicMineralsBuildShip = 100;
    private int heuristicEnergyBuildShip = 50;
    private int heuristicMineralsBuildStation = 200;
    private int heuristicEnergyBuildStation = 100;
    private int heuristicFoodExpandColony = 30;
    private int heuristicMaxPlanetsForExpansion = 5;
    private int heuristicResearchTech = 50;
    private int heuristicFleetEngageCombat = 3;
    private int heuristicMineralsRetreat = 30;
    private int heuristicFleetFlankAmbush = 2;
    private int heuristicMineralsOptimizeMining = 100;
    private int heuristicMineralsFortify = 50;
    private int heuristicMineralsWait = 50;
    private int heuristicEnergyWait = 25;

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

    // Heuristic threshold getters and setters

    public int getHeuristicMineralsBuildShip() {
        return heuristicMineralsBuildShip;
    }

    public void setHeuristicMineralsBuildShip(int heuristicMineralsBuildShip) {
        this.heuristicMineralsBuildShip = heuristicMineralsBuildShip;
    }

    public int getHeuristicEnergyBuildShip() {
        return heuristicEnergyBuildShip;
    }

    public void setHeuristicEnergyBuildShip(int heuristicEnergyBuildShip) {
        this.heuristicEnergyBuildShip = heuristicEnergyBuildShip;
    }

    public int getHeuristicMineralsBuildStation() {
        return heuristicMineralsBuildStation;
    }

    public void setHeuristicMineralsBuildStation(int heuristicMineralsBuildStation) {
        this.heuristicMineralsBuildStation = heuristicMineralsBuildStation;
    }

    public int getHeuristicEnergyBuildStation() {
        return heuristicEnergyBuildStation;
    }

    public void setHeuristicEnergyBuildStation(int heuristicEnergyBuildStation) {
        this.heuristicEnergyBuildStation = heuristicEnergyBuildStation;
    }

    public int getHeuristicFoodExpandColony() {
        return heuristicFoodExpandColony;
    }

    public void setHeuristicFoodExpandColony(int heuristicFoodExpandColony) {
        this.heuristicFoodExpandColony = heuristicFoodExpandColony;
    }

    public int getHeuristicMaxPlanetsForExpansion() {
        return heuristicMaxPlanetsForExpansion;
    }

    public void setHeuristicMaxPlanetsForExpansion(int heuristicMaxPlanetsForExpansion) {
        this.heuristicMaxPlanetsForExpansion = heuristicMaxPlanetsForExpansion;
    }

    public int getHeuristicResearchTech() {
        return heuristicResearchTech;
    }

    public void setHeuristicResearchTech(int heuristicResearchTech) {
        this.heuristicResearchTech = heuristicResearchTech;
    }

    public int getHeuristicFleetEngageCombat() {
        return heuristicFleetEngageCombat;
    }

    public void setHeuristicFleetEngageCombat(int heuristicFleetEngageCombat) {
        this.heuristicFleetEngageCombat = heuristicFleetEngageCombat;
    }

    public int getHeuristicMineralsRetreat() {
        return heuristicMineralsRetreat;
    }

    public void setHeuristicMineralsRetreat(int heuristicMineralsRetreat) {
        this.heuristicMineralsRetreat = heuristicMineralsRetreat;
    }

    public int getHeuristicFleetFlankAmbush() {
        return heuristicFleetFlankAmbush;
    }

    public void setHeuristicFleetFlankAmbush(int heuristicFleetFlankAmbush) {
        this.heuristicFleetFlankAmbush = heuristicFleetFlankAmbush;
    }

    public int getHeuristicMineralsOptimizeMining() {
        return heuristicMineralsOptimizeMining;
    }

    public void setHeuristicMineralsOptimizeMining(int heuristicMineralsOptimizeMining) {
        this.heuristicMineralsOptimizeMining = heuristicMineralsOptimizeMining;
    }

    public int getHeuristicMineralsFortify() {
        return heuristicMineralsFortify;
    }

    public void setHeuristicMineralsFortify(int heuristicMineralsFortify) {
        this.heuristicMineralsFortify = heuristicMineralsFortify;
    }

    public int getHeuristicMineralsWait() {
        return heuristicMineralsWait;
    }

    public void setHeuristicMineralsWait(int heuristicMineralsWait) {
        this.heuristicMineralsWait = heuristicMineralsWait;
    }

    public int getHeuristicEnergyWait() {
        return heuristicEnergyWait;
    }

    public void setHeuristicEnergyWait(int heuristicEnergyWait) {
        this.heuristicEnergyWait = heuristicEnergyWait;
    }

    // Memory safeguard getters and setters

    public double getMemoryCriticalThreshold() {
        return memoryCriticalThreshold;
    }

    public void setMemoryCriticalThreshold(double memoryCriticalThreshold) {
        this.memoryCriticalThreshold = memoryCriticalThreshold;
    }

    public double getMemoryWarningThreshold() {
        return memoryWarningThreshold;
    }

    public void setMemoryWarningThreshold(double memoryWarningThreshold) {
        this.memoryWarningThreshold = memoryWarningThreshold;
    }

    public int getMaxExperienceBufferSize() {
        return maxExperienceBufferSize;
    }

    public void setMaxExperienceBufferSize(int maxExperienceBufferSize) {
        this.maxExperienceBufferSize = maxExperienceBufferSize;
    }

    public boolean isGcBeforeTraining() {
        return gcBeforeTraining;
    }

    public void setGcBeforeTraining(boolean gcBeforeTraining) {
        this.gcBeforeTraining = gcBeforeTraining;
    }
}
