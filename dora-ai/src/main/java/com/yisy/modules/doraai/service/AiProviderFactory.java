package com.yisy.modules.doraai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Factory for selecting the active AI provider.
 * <p>
 * Supports multiple AI providers (Ollama, Gemini) and allows switching
 * between them via configuration or API request.
 * </p>
 */
@Component
public class AiProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(AiProviderFactory.class);

    private final List<AiProvider> providers;
    private final String defaultProvider;

    public AiProviderFactory(List<AiProvider> providers,
                             @Value("${ai.provider.default:ollama}") String defaultProvider) {
        this.providers = providers;
        this.defaultProvider = defaultProvider;
        log.info("Initialized AI provider factory with {} providers, default: {}", providers.size(), defaultProvider);
        for (AiProvider provider : providers) {
            log.info("Registered AI provider: {}", provider.getProviderId());
        }
    }

    /**
     * Gets the AI provider by ID.
     *
     * @param providerId the provider ID (e.g., "ollama", "gemini")
     * @return the AI provider
     * @throws IllegalArgumentException if the provider is not found
     */
    public AiProvider getProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return getProvider(defaultProvider);
        }

        return providers.stream()
                .filter(p -> p.getProviderId().equals(providerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("AI provider not found: " + providerId));
    }

    /**
     * Gets the default AI provider.
     *
     * @return the default AI provider
     */
    public AiProvider getDefaultProvider() {
        return getProvider(defaultProvider);
    }

    /**
     * Gets the best available AI provider, falling back gracefully if the preferred
     * provider is unavailable or offline.
     *
     * @param preferredProviderId the requested provider ID (can be null/empty)
     * @return the resolved AI provider
     */
    public AiProvider getProviderWithFallback(String preferredProviderId) {
        if (preferredProviderId != null && !preferredProviderId.isBlank()) {
            Optional<AiProvider> preferred = providers.stream()
                    .filter(p -> p.getProviderId().equalsIgnoreCase(preferredProviderId))
                    .findFirst();
            if (preferred.isPresent() && preferred.get().isAvailable()) {
                return preferred.get();
            }
            log.warn("Preferred AI provider '{}' is not available. Attempting fallback.", preferredProviderId);
        }

        // Try default provider
        Optional<AiProvider> def = providers.stream()
                .filter(p -> p.getProviderId().equalsIgnoreCase(defaultProvider))
                .findFirst();
        if (def.isPresent() && def.get().isAvailable()) {
            return def.get();
        }

        // Try any available provider in registered order
        Optional<AiProvider> available = providers.stream()
                .filter(AiProvider::isAvailable)
                .findFirst();
        if (available.isPresent()) {
            log.info("Fell back to available AI provider: {}", available.get().getProviderId());
            return available.get();
        }

        // Fall back to default provider even if unavailable (will handle error during call)
        log.warn("All AI providers are unavailable. Falling back to default provider: {} (may fail)", defaultProvider);
        return getDefaultProvider();
    }

    /**
     * Returns all available provider IDs.
     *
     * @return list of provider IDs
     */
    public List<String> getAvailableProviders() {
        return providers.stream()
                .map(AiProvider::getProviderId)
                .toList();
    }

    /**
     * Checks if a provider with the given ID is available.
     *
     * @param providerId the provider ID
     * @return true if the provider exists and is reachable
     */
    public boolean isProviderAvailable(String providerId) {
        return providers.stream()
                .filter(p -> p.getProviderId().equals(providerId))
                .findFirst()
                .map(AiProvider::isAvailable)
                .orElse(false);
    }
}
