package com.yisy.modules.doramcu.hex;

/**
 * Difficulty presets that adjust resource availability and celestial body probabilities.
 * Extracted from GameCreationUI for reuse in galaxy generation.
 */
public enum Difficulty {
    EASY("Easy", 0.6, 0.6, 0.4, 0.2, 0.1, 0.03, 0.3, 0.5, 7),
    NORMAL("Normal", 0.4, 0.5, 0.3, 0.15, 0.05, 0.05, 0.2, 1.0, 5),
    HARD("Hard", 0.3, 0.4, 0.2, 0.1, 0.03, 0.03, 0.15, 1.5, 4),
    VETERAN("Veteran", 0.2, 0.3, 0.15, 0.05, 0.02, 0.02, 0.1, 2.0, 3);

    private final String displayName;
    private final double moonChance;
    private final double asteroidBeltChance;
    private final double gasGiantChance;
    private final double nebulaChance;
    private final double blackHoleChance;
    private final double cometChance;
    private final double spaceStationChance;
    private final double hyperlaneDensity;
    private final int planetsPerSystem;

    Difficulty(String displayName,
               double moonChance, double asteroidBeltChance, double gasGiantChance,
               double nebulaChance, double blackHoleChance, double cometChance,
               double spaceStationChance, double hyperlaneDensity, int planetsPerSystem) {
        this.displayName = displayName;
        this.moonChance = moonChance;
        this.asteroidBeltChance = asteroidBeltChance;
        this.gasGiantChance = gasGiantChance;
        this.nebulaChance = nebulaChance;
        this.blackHoleChance = blackHoleChance;
        this.cometChance = cometChance;
        this.spaceStationChance = spaceStationChance;
        this.hyperlaneDensity = hyperlaneDensity;
        this.planetsPerSystem = planetsPerSystem;
    }

    public String getDisplayName() {
        return displayName;
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

    public int getPlanetsPerSystem() {
        return planetsPerSystem;
    }
}
