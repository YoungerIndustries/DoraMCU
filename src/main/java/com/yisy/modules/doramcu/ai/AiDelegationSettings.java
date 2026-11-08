package com.yisy.modules.doramcu.ai;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores the AI delegation settings for a player's empire.
 * <p>
 * When {@link #isEmpireAiEnabled()} is true, the AI takes over all delegated areas
 * of empire management. The player can fine-tune by enabling/disabling individual
 * {@link AiDelegationArea} areas.
 * </p>
 * <p>
 * This is a value object — once created, use the builder to create modified copies.
 * </p>
 */
public class AiDelegationSettings {

    private final boolean empireAiEnabled;
    private final Map<AiDelegationArea, Boolean> areaSettings;

    private AiDelegationSettings(boolean empireAiEnabled, Map<AiDelegationArea, Boolean> areaSettings) {
        this.empireAiEnabled = empireAiEnabled;
        this.areaSettings = new EnumMap<>(areaSettings);
    }

    /**
     * Returns true if the empire-level AI switch is on.
     * <p>
     * When true, the AI manages all areas that are individually enabled.
     * When false, no AI delegation occurs regardless of individual area settings.
     * </p>
     */
    public boolean isEmpireAiEnabled() {
        return empireAiEnabled;
    }

    /**
     * Returns true if the specified area is delegated to AI.
     * <p>
     * Note: This checks only the individual area setting. To check if the area
     * is actually delegated, use {@link #isDelegated(AiDelegationArea)} which
     * also checks the empire-level switch.
     * </p>
     */
    public boolean isAreaEnabled(AiDelegationArea area) {
        Objects.requireNonNull(area, "Area must not be null");
        return areaSettings.getOrDefault(area, false);
    }

    /**
     * Returns true if the specified area is actually delegated to AI.
     * <p>
     * This checks both the empire-level switch and the individual area setting.
     * Both must be true for the area to be delegated.
     * </p>
     */
    public boolean isDelegated(AiDelegationArea area) {
        Objects.requireNonNull(area, "Area must not be null");
        return empireAiEnabled && isAreaEnabled(area);
    }

    /**
     * Returns the set of areas currently delegated to AI.
     * <p>
     * Only includes areas where both the empire switch and the individual area
     * setting are enabled.
     * </p>
     */
    public Set<AiDelegationArea> getDelegatedAreas() {
        if (!empireAiEnabled) {
            return Collections.emptySet();
        }
        return areaSettings.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the set of areas that are individually enabled, regardless of
     * the empire-level switch.
     */
    public Set<AiDelegationArea> getEnabledAreas() {
        return areaSettings.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the total number of delegation areas.
     */
    public int getTotalAreas() {
        return AiDelegationArea.values().length;
    }

    /**
     * Returns the number of areas currently delegated.
     */
    public int getDelegatedCount() {
        return getDelegatedAreas().size();
    }

    /**
     * Returns the raw area settings map (unmodifiable).
     */
    public Map<AiDelegationArea, Boolean> getAreaSettings() {
        return Collections.unmodifiableMap(areaSettings);
    }

    /**
     * Creates a builder for AiDelegationSettings.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates default settings with all areas enabled.
     */
    public static AiDelegationSettings allEnabled() {
        return builder().enableAll().build();
    }

    /**
     * Creates default settings with all areas disabled.
     */
    public static AiDelegationSettings allDisabled() {
        return builder().disableAll().build();
    }

    /**
     * Creates a copy with the empire-level AI switch toggled.
     */
    public AiDelegationSettings withEmpireAiEnabled(boolean enabled) {
        return builder()
                .empireAiEnabled(enabled)
                .areaSettings(new EnumMap<>(this.areaSettings))
                .build();
    }

    /**
     * Creates a copy with the specified area toggled.
     */
    public AiDelegationSettings withAreaToggled(AiDelegationArea area, boolean enabled) {
        Objects.requireNonNull(area, "Area must not be null");
        Map<AiDelegationArea, Boolean> newSettings = new EnumMap<>(this.areaSettings);
        newSettings.put(area, enabled);
        return new AiDelegationSettings(this.empireAiEnabled, newSettings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiDelegationSettings that = (AiDelegationSettings) o;
        return empireAiEnabled == that.empireAiEnabled &&
                areaSettings.equals(that.areaSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empireAiEnabled, areaSettings);
    }

    @Override
    public String toString() {
        return "AiDelegationSettings{" +
                "empireAiEnabled=" + empireAiEnabled +
                ", delegatedAreas=" + getDelegatedAreas() +
                '}';
    }

    /**
     * Builder for AiDelegationSettings.
     */
    public static class Builder {
        private boolean empireAiEnabled = false;
        private final Map<AiDelegationArea, Boolean> areaSettings = new EnumMap<>(AiDelegationArea.class);

        private Builder() {
            // Initialize all areas to false
            for (AiDelegationArea area : AiDelegationArea.values()) {
                areaSettings.put(area, false);
            }
        }

        /**
         * Sets the empire-level AI switch.
         */
        public Builder empireAiEnabled(boolean enabled) {
            this.empireAiEnabled = enabled;
            return this;
        }

        /**
         * Enables or disables a specific area.
         */
        public Builder setArea(AiDelegationArea area, boolean enabled) {
            Objects.requireNonNull(area, "Area must not be null");
            areaSettings.put(area, enabled);
            return this;
        }

        /**
         * Enables a specific area.
         */
        public Builder enableArea(AiDelegationArea area) {
            return setArea(area, true);
        }

        /**
         * Enables all areas.
         */
        public Builder enableAll() {
            for (AiDelegationArea area : AiDelegationArea.values()) {
                areaSettings.put(area, true);
            }
            return this;
        }

        /**
         * Disables all areas.
         */
        public Builder disableAll() {
            for (AiDelegationArea area : AiDelegationArea.values()) {
                areaSettings.put(area, false);
            }
            return this;
        }

        /**
         * Sets the area settings map directly.
         */
        public Builder areaSettings(Map<AiDelegationArea, Boolean> settings) {
            Objects.requireNonNull(settings, "Settings must not be null");
            this.areaSettings.clear();
            this.areaSettings.putAll(settings);
            // Ensure all areas are present
            for (AiDelegationArea area : AiDelegationArea.values()) {
                areaSettings.putIfAbsent(area, false);
            }
            return this;
        }

        /**
         * Builds the AiDelegationSettings.
         */
        public AiDelegationSettings build() {
            return new AiDelegationSettings(empireAiEnabled, areaSettings);
        }
    }
}
