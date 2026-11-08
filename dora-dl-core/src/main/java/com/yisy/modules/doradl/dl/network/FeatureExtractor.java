package com.yisy.modules.doradl.dl.network;

import com.yisy.modules.doradl.model.GameStateFeatures;

/**
 * Extracts a feature vector from a game state for neural network input.
 * <p>
 * Implementations are game-type-specific: each game system (4X, Traveller,
 * Captain's Log) provides its own extractor that maps domain-specific state
 * into a normalized feature vector suitable for the DL model.
 * </p>
 * <p>
 * The interface accepts {@link Object} as the game state parameter to avoid
 * coupling the DL core module to any specific game's DTO types. Implementations
 * cast to their expected type internally.
 * </p>
 *
 * @see FeatureExtractorRegistry for runtime lookup by game type
 */
public interface FeatureExtractor {

    /**
     * Extracts a feature vector from the given game state for the specified player.
     *
     * @param gameState the game state object (type depends on the implementing extractor)
     * @param playerId  the player to extract features for
     * @return a populated GameStateFeatures vector, never null
     * @throws IllegalArgumentException if gameState or playerId is null
     */
    GameStateFeatures extract(Object gameState, Long playerId);

    /**
     * Returns the expected input type for this extractor.
     * <p>
     * Used by the registry for runtime type validation when extracting features.
     * </p>
     *
     * @return the Class of the expected game state input
     */
    Class<?> getInputType();

    /**
     * Returns the number of feature dimensions this extractor produces.
     * <p>
     * Default is 64 (the standard neural network input size). Subclasses may
     * override if they use a different dimensionality.
     * </p>
     *
     * @return the feature vector dimension count
     */
    default int getFeatureDimension() {
        return 64;
    }
}
