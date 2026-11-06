package com.yisy.modules.doramcu.hex;

/**
 * Configuration for galaxy generation parameters.
 * <p>
 * Extracted from GalaxyGenerator to provide a single value object for all
 * configurable generation parameters like system count, planet count, and
 * celestial body probabilities.
 * </p>
 */
public class GalaxyGenerationConfig {

    public static final int DEFAULT_SYSTEM_COUNT = SectorBuilder.recommendedSystemCount();
    public static final int DEFAULT_PLANETS_PER_SYSTEM = 5;
    public static final double DEFAULT_MOON_CHANCE = 0.4;
    public static final double DEFAULT_ASTEROID_BELT_CHANCE = 0.5;
    public static final double DEFAULT_GAS_GIANT_CHANCE = 0.3;
    public static final double DEFAULT_NEBULA_CHANCE = 0.15;
    public static final double DEFAULT_BLACK_HOLE_CHANCE = 0.05;
    public static final double DEFAULT_COMET_CHANCE = 0.2;
    public static final double DEFAULT_SPACE_STATION_CHANCE = 0.1;
    public static final double DEFAULT_HYPERLANE_DENSITY = 1.0;

    private final int systemCount;
    private final int planetsPerSystem;
    private final double moonChance;
    private final double asteroidBeltChance;
    private final double gasGiantChance;
    private final double nebulaChance;
    private final double blackHoleChance;
    private final double cometChance;
    private final double spaceStationChance;
    private final double hyperlaneDensity;

    private GalaxyGenerationConfig(Builder builder) {
        this.systemCount = builder.systemCount;
        this.planetsPerSystem = builder.planetsPerSystem;
        this.moonChance = builder.moonChance;
        this.asteroidBeltChance = builder.asteroidBeltChance;
        this.gasGiantChance = builder.gasGiantChance;
        this.nebulaChance = builder.nebulaChance;
        this.blackHoleChance = builder.blackHoleChance;
        this.cometChance = builder.cometChance;
        this.spaceStationChance = builder.spaceStationChance;
        this.hyperlaneDensity = builder.hyperlaneDensity;
    }

    /**
     * Creates a builder with default values.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public int getSystemCount() {
        return systemCount;
    }

    public int getPlanetsPerSystem() {
        return planetsPerSystem;
    }

    public double getMoonChance() {
        return moonChance;
    }

    public double getAsteroidBeltChance() {
        return asteroidBeltChance;
    }

    public double getGasGiantChance() {
        return gasGiantChance;
    }

    public double getNebulaChance() {
        return nebulaChance;
    }

    public double getBlackHoleChance() {
        return blackHoleChance;
    }

    public double getCometChance() {
        return cometChance;
    }

    public double getSpaceStationChance() {
        return spaceStationChance;
    }

    public double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    @Override
    public String toString() {
        return String.format("GalaxyGenerationConfig{systems=%d, planets=%d, moon=%.0f%%, asteroid=%.0f%%, gasGiant=%.0f%%, nebula=%.0f%%, blackHole=%.0f%%, comet=%.0f%%, station=%.0f%%, hyperlanes=%.1fx}",
                systemCount, planetsPerSystem,
                moonChance * 100, asteroidBeltChance * 100, gasGiantChance * 100,
                nebulaChance * 100, blackHoleChance * 100, cometChance * 100,
                spaceStationChance * 100, hyperlaneDensity);
    }

    /**
     * Builder for GalaxyGenerationConfig with default values.
     */
    public static class Builder {
        private int systemCount = DEFAULT_SYSTEM_COUNT;
        private int planetsPerSystem = DEFAULT_PLANETS_PER_SYSTEM;
        private double moonChance = DEFAULT_MOON_CHANCE;
        private double asteroidBeltChance = DEFAULT_ASTEROID_BELT_CHANCE;
        private double gasGiantChance = DEFAULT_GAS_GIANT_CHANCE;
        private double nebulaChance = DEFAULT_NEBULA_CHANCE;
        private double blackHoleChance = DEFAULT_BLACK_HOLE_CHANCE;
        private double cometChance = DEFAULT_COMET_CHANCE;
        private double spaceStationChance = DEFAULT_SPACE_STATION_CHANCE;
        private double hyperlaneDensity = DEFAULT_HYPERLANE_DENSITY;

        public Builder systemCount(int systemCount) {
            if (systemCount < 0) {
                throw new IllegalArgumentException("System count must be non-negative: " + systemCount);
            }
            this.systemCount = systemCount;
            return this;
        }

        public Builder planetsPerSystem(int planetsPerSystem) {
            if (planetsPerSystem < 0) {
                throw new IllegalArgumentException("Planets per system must be non-negative: " + planetsPerSystem);
            }
            this.planetsPerSystem = planetsPerSystem;
            return this;
        }

        public Builder moonChance(double moonChance) {
            if (moonChance < 0.0 || moonChance > 1.0) {
                throw new IllegalArgumentException("Moon chance must be between 0.0 and 1.0: " + moonChance);
            }
            this.moonChance = moonChance;
            return this;
        }

        public Builder asteroidBeltChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Asteroid belt chance must be between 0.0 and 1.0: " + chance);
            }
            this.asteroidBeltChance = chance;
            return this;
        }

        public Builder gasGiantChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Gas giant chance must be between 0.0 and 1.0: " + chance);
            }
            this.gasGiantChance = chance;
            return this;
        }

        public Builder nebulaChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Nebula chance must be between 0.0 and 1.0: " + chance);
            }
            this.nebulaChance = chance;
            return this;
        }

        public Builder blackHoleChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Black hole chance must be between 0.0 and 1.0: " + chance);
            }
            this.blackHoleChance = chance;
            return this;
        }

        public Builder cometChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Comet chance must be between 0.0 and 1.0: " + chance);
            }
            this.cometChance = chance;
            return this;
        }

        public Builder spaceStationChance(double chance) {
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException("Space station chance must be between 0.0 and 1.0: " + chance);
            }
            this.spaceStationChance = chance;
            return this;
        }

        public Builder hyperlaneDensity(double density) {
            if (density < 0.5 || density > 2.75) {
                throw new IllegalArgumentException("Hyperlane density must be between 0.5 and 2.75: " + density);
            }
            this.hyperlaneDensity = density;
            return this;
        }

        public GalaxyGenerationConfig build() {
            return new GalaxyGenerationConfig(this);
        }
    }
}
