package com.yisy.modules.doradl.dl.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that maps game types to their corresponding {@link FeatureExtractor} implementations.
 * <p>
 * Auto-discovers all {@code FeatureExtractor} beans at startup and registers them
 * by the game type they support. At extraction time, the registry looks up the
 * correct extractor for the given game type and delegates the call.
 * </p>
 * <p>
 * If no extractor is registered for a game type, falls back to the default extractor
 * (typically the 4X extractor) and logs a warning.
 * </p>
 */
@Component
public class FeatureExtractorRegistry {

    private static final Logger log = LoggerFactory.getLogger(FeatureExtractorRegistry.class);

    /**
     * Simple game type identifier used when the full GameType enum is not available
     * in the DL core module. Matches the enum names in {@code com.yisy.modules.doramcu.unified.GameType}.
     */
    public enum SupportedGameType {
        DORAMCU_4X,
        TRAVELLER,
        CAPTAINS_LOG
    }

    private final Map<SupportedGameType, FeatureExtractor> extractors = new EnumMap<>(SupportedGameType.class);
    private FeatureExtractor defaultExtractor;

    /**
     * Auto-registers all FeatureExtractor beans on startup.
     */
    public FeatureExtractorRegistry(List<FeatureExtractor> extractorBeans) {
        log.info("Initializing FeatureExtractorRegistry with {} beans", extractorBeans.size());

        for (FeatureExtractor extractor : extractorBeans) {
            // Each extractor declares its supported game type via a method
            if (extractor instanceof GameTypedExtractor) {
                GameTypedExtractor typed = (GameTypedExtractor) extractor;
                SupportedGameType gameType = typed.getSupportedGameType();
                extractors.put(gameType, extractor);
                log.info("Registered extractor {} for game type {}", extractor.getClass().getSimpleName(), gameType);

                // Mark as default if annotated
                if (typed.isDefaultExtractor()) {
                    defaultExtractor = extractor;
                }
            } else {
                log.warn("Skipping FeatureExtractor {} — does not implement GameTypedExtractor",
                        extractor.getClass().getSimpleName());
            }
        }

        if (defaultExtractor == null && !extractors.isEmpty()) {
            // Fallback: use the 4X extractor as default
            defaultExtractor = extractors.get(SupportedGameType.DORAMCU_4X);
            if (defaultExtractor == null) {
                // Last resort: use the first registered extractor
                defaultExtractor = extractors.values().iterator().next();
            }
            log.info("No default extractor marked — using {} as fallback", defaultExtractor.getClass().getSimpleName());
        }

        log.info("FeatureExtractorRegistry initialized with {} extractors, default: {}",
                extractors.size(),
                defaultExtractor != null ? defaultExtractor.getClass().getSimpleName() : "none");
    }

    /**
     * Gets the feature extractor for the specified game type.
     * <p>
     * If no extractor is registered for the game type, returns the default extractor
     * and logs a warning.
     * </p>
     *
     * @param gameType the game type to look up
     * @return the FeatureExtractor for the game type, or the default if not found
     * @throws IllegalStateException if no extractors are registered at all
     */
    public FeatureExtractor getExtractor(SupportedGameType gameType) {
        FeatureExtractor extractor = extractors.get(gameType);
        if (extractor == null) {
            log.warn("No extractor registered for game type {}, falling back to default ({})",
                    gameType,
                    defaultExtractor != null ? defaultExtractor.getClass().getSimpleName() : "none");
            if (defaultExtractor == null) {
                throw new IllegalStateException("No feature extractors registered");
            }
            return defaultExtractor;
        }
        return extractor;
    }

    /**
     * Returns all registered extractors.
     */
    public Map<SupportedGameType, FeatureExtractor> getAllExtractors() {
        return Collections.unmodifiableMap(extractors);
    }

    /**
     * Returns the default extractor.
     */
    public FeatureExtractor getDefaultExtractor() {
        return defaultExtractor;
    }

    /**
     * Extension interface that FeatureExtractor implementations should implement
     * to declare which game type they support.
     */
    public interface GameTypedExtractor extends FeatureExtractor {
        SupportedGameType getSupportedGameType();
        default boolean isDefaultExtractor() {
            return false;
        }
    }
}
