package com.yisy.modules.doramcu.hex;

/**
 * Factory for creating the single galaxy in DoraMCU.
 * <p>
 * Only one galaxy is allowed:
 * <ul>
 *   <li><b>Game Galaxy</b> ("Milky Way") — Full-size galaxy for actual gameplay with default settings.</li>
 * </ul>
 * <p>
 * This factory ensures consistent galaxy configurations and prevents arbitrary galaxy creation.
 */
public class GalaxyFactory {

    /**
     * Name of the game galaxy used for actual gameplay.
     */
    public static final String GAME_GALAXY_NAME = "Milky Way";

    /**
     * Random seed for the game galaxy — ensures reproducibility.
     */
    public static final long GAME_GALAXY_SEED = GalaxyGenerator.DEFAULT_SEED;

    private GalaxyFactory() {
        // Utility class, no instantiation
    }

    /**
     * Returns the generation configuration for the game galaxy.
     * <p>
     * Uses default settings for a full-size galaxy suitable for gameplay.
     *
     * @return the configuration for the game galaxy
     */
    public static GalaxyGenerationConfig gameGalaxyConfig() {
        return GalaxyGenerationConfig.builder()
                .systemCount(GalaxyGenerationConfig.DEFAULT_SYSTEM_COUNT)
                .planetsPerSystem(GalaxyGenerationConfig.DEFAULT_PLANETS_PER_SYSTEM)
                .moonChance(GalaxyGenerationConfig.DEFAULT_MOON_CHANCE)
                .asteroidBeltChance(GalaxyGenerationConfig.DEFAULT_ASTEROID_BELT_CHANCE)
                .gasGiantChance(GalaxyGenerationConfig.DEFAULT_GAS_GIANT_CHANCE)
                .nebulaChance(GalaxyGenerationConfig.DEFAULT_NEBULA_CHANCE)
                .blackHoleChance(GalaxyGenerationConfig.DEFAULT_BLACK_HOLE_CHANCE)
                .cometChance(GalaxyGenerationConfig.DEFAULT_COMET_CHANCE)
                .spaceStationChance(GalaxyGenerationConfig.DEFAULT_SPACE_STATION_CHANCE)
                .hyperlaneDensity(GalaxyGenerationConfig.DEFAULT_HYPERLANE_DENSITY)
                .build();
    }

    /**
     * Generates the game galaxy in memory.
     *
     * @return the in-memory game galaxy
     */
    public static Galaxy generateGameGalaxy() {
        GalaxyGenerator generator = new GalaxyGenerator(GAME_GALAXY_SEED);
        generator.setConfig(gameGalaxyConfig());
        return generator.generateGalaxy(1, GAME_GALAXY_NAME);
    }

    /**
     * Generates a galaxy by name.
     *
     * @param name the galaxy name (must be GAME_GALAXY_NAME)
     * @return the in-memory galaxy
     * @throws IllegalArgumentException if the name is not recognized
     */
    public static Galaxy generateGalaxyByName(String name) {
        if (GAME_GALAXY_NAME.equals(name)) {
            return generateGameGalaxy();
        } else {
            throw new IllegalArgumentException(
                    "Unknown galaxy: " + name + ". Only " + GAME_GALAXY_NAME + " is allowed.");
        }
    }

    /**
     * Checks if the given name is a valid galaxy name.
     *
     * @param name the name to check
     * @return true if the name is GAME_GALAXY_NAME
     */
    public static boolean isValidGalaxyName(String name) {
        return GAME_GALAXY_NAME.equals(name);
    }
}
