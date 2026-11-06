package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Random galaxy generator for 4X space games.
 * <p>
 * Orchestrates galaxy generation by delegating to specialized components:
 * {@link CelestialBodyGenerator} for individual bodies, {@link SolarSystemGenerator}
 * for solar system assembly, and {@link HyperlaneGenerator} for network connections.
 * <p>
 * The generator uses a configurable {@link Random} instance for reproducibility via seeds.
 * Galaxy shape influences solar system placement: spiral arms concentrate systems along
 * curved paths, while elliptical galaxies distribute them evenly.
 * <p>
 * Based on design patterns from Stellaris (galaxy shapes, hyperlanes, sectors) and
 * Galactic Civilizations IV (strategic resources, planet roles).
 */
public class GalaxyGenerator {

    public static final int DEFAULT_SEED = 42;

    private final Random random;
    private final StarNameDatabase starNames;
    private final PlanetNameDatabase planetNames;
    private final GalaxyNameDatabase galaxyNames;

    // Delegated components
    private CelestialBodyGenerator bodyGenerator;
    private SolarSystemGenerator systemGenerator;
    private HyperlaneGenerator hyperlaneGenerator;

    // Progress callback (optional)
    private GalaxyGenerationProgress progressListener;

    // Generation configuration
    private GalaxyGenerationConfig config;

    /**
     * Creates a new generator with default parameters and seed 42.
     */
    public GalaxyGenerator() {
        this(DEFAULT_SEED);
    }

    /**
     * Creates a new generator with the given random seed for reproducibility.
     *
     * @param seed the random seed
     */
    public GalaxyGenerator(long seed) {
        this.random = new Random(seed);
        this.starNames = new StarNameDatabase();
        this.planetNames = new PlanetNameDatabase();
        this.galaxyNames = new GalaxyNameDatabase();
        this.config = GalaxyGenerationConfig.builder().build();
        initializeComponents();
    }

    /**
     * Initializes the delegated generator components.
     */
    private void initializeComponents() {
        this.bodyGenerator = new CelestialBodyGenerator(random, starNames, planetNames);
        this.systemGenerator = new SolarSystemGenerator(random, bodyGenerator, config, progressListener);
        this.hyperlaneGenerator = new HyperlaneGenerator(random);
    }

    // ---- Configuration ----

    /**
     * Sets the generation configuration and reinitializes components.
     *
     * @param config the new configuration
     */
    public void setConfig(GalaxyGenerationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        this.config = config;
        this.systemGenerator = new SolarSystemGenerator(random, bodyGenerator, config, progressListener);
    }

    // ---- Main Generation Methods ----

    /**
     * Generates a complete galaxy with the given ID.
     * The galaxy uses a 10x10 2D sector grid with 100x100x100 3D sectors.
     *
     * @param galaxyId the unique identifier for the galaxy
     * @return the generated galaxy
     */
    public Galaxy generateGalaxy(int galaxyId) {
        return generateGalaxy(galaxyId, galaxyNames.getNextName());
    }

    /**
     * Generates a complete galaxy with the given ID and name.
     * <p>
     * The galaxy contains 100 sectors in a 10x10 grid. Each sector has a 10x10
     * hex grid of subsectors. Solar systems are distributed evenly across all sectors
     * so that every region of the galaxy is populated, not just the center.
     * <p>
     * The number of systems per sector is derived from {@code config.getSystemCount()}
     * divided by the number of sectors. Each sector populates a random subset of its
     * subsectors, ensuring even distribution without generating all 10,000 subsectors.
     *
     * @param galaxyId the unique identifier for the galaxy
     * @param name the name of the galaxy
     * @return the generated galaxy
     */
    public Galaxy generateGalaxy(int galaxyId, String name) {
        Galaxy galaxy = new Galaxy(galaxyId, name);
        galaxy.setHyperlaneDensity(config.getHyperlaneDensity());

        int totalTargetSystems = config.getSystemCount();
        int systemsPerSector = Math.max(1, totalTargetSystems / Galaxy.TOTAL_SECTORS);

        // Notify progress listener of start
        if (progressListener != null) {
            progressListener.onStart(totalTargetSystems);
            progressListener.onLog("[1/3] Generating galaxy: " + name);
            progressListener.onLog("  Distributing " + totalTargetSystems + " solar systems across " + Galaxy.TOTAL_SECTORS + " sectors (" + systemsPerSector + " per sector)");
        }

        // Distribute solar systems evenly across all sectors.
        // Each sector gets a share of the total system count, spread across random subsectors.
        int totalSystems = 0;
        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                Galaxy.Sector sector = galaxy.getSector(row, col);
                int generated = generateSectorSystems(sector, systemsPerSector);
                totalSystems += generated;
                for (SolarSystem system : sector.getAllSystems().values()) {
                    galaxy.getAllSystemsMap().put(system.getId(), system);
                }
            }
        }

        if (progressListener != null) {
            progressListener.onLog("[2/3] Generated " + totalSystems + " solar systems across " + Galaxy.TOTAL_SECTORS + " sectors");
            progressListener.onHyperlanesStart();
            progressListener.onLog("[3/3] Generating hyperlane network...");
        }
        hyperlaneGenerator.generateHyperlanes(galaxy, config.getHyperlaneDensity());

        // Notify progress listener of completion
        if (progressListener != null) {
            progressListener.onComplete();
            progressListener.onLog("Galaxy " + name + " complete: " + totalSystems + " systems, " + galaxy.getHyperlaneCount() + " hyperlanes");
        }

        return galaxy;
    }

    /**
     * Generates solar systems for a single sector, distributing them across random subsectors.
     * <p>
     * Instead of generating all 100 subsectors, this method selects a subset of subsectors
     * and populates each with 1-3 systems until the target count is reached.
     *
     * @param sector the sector to populate
     * @param targetSystems the number of systems to generate in this sector
     * @return the number of systems actually generated
     */
    private int generateSectorSystems(Galaxy.Sector sector, int targetSystems) {
        if (targetSystems <= 0) {
            return 0;
        }

        // Build a list of all subsector positions and shuffle for random selection
        List<int[]> subsectorPositions = new ArrayList<>(Galaxy.TOTAL_SUBSECTORS_PER_SECTOR);
        for (int r = 0; r < Galaxy.SUBSECTOR_GRID_SIZE; r++) {
            for (int c = 0; c < Galaxy.SUBSECTOR_GRID_SIZE; c++) {
                subsectorPositions.add(new int[]{r, c});
            }
        }
        // Shuffle to randomize which subsectors get populated
        shuffleList(subsectorPositions, random);

        int totalGenerated = 0;
        int subsectorIndex = 0;

        while (totalGenerated < targetSystems && subsectorIndex < subsectorPositions.size()) {
            int[] pos = subsectorPositions.get(subsectorIndex++);
            int subRow = pos[0];
            int subCol = pos[1];

            SubSector subSector = sector.getSubSector(subRow, subCol);
            subSector.setProgressListener(progressListener);

            // Generate 1-3 systems in this subsector
            List<SolarSystem> generated = subSector.generateSystems(random, starNames, planetNames);
            
            // Copy generated systems to the sector's tracking map.
            // Collect entries first to avoid ConcurrentModificationException,
            // because sector.addSystem() may modify the subsector's entry map.
            List<GalacticEntry> entries = new ArrayList<>(subSector.getAllEntries());
            for (GalacticEntry entry : entries) {
                sector.addSystem(entry.getSystem(), subRow, subCol, entry.getX(), entry.getY(), entry.getZ());
            }
            
            totalGenerated += generated.size();

            // Notify progress listener
            if (progressListener != null) {
                for (SolarSystem system : generated) {
                    progressListener.onSystemCreated(system);
                }
            }
        }

        return totalGenerated;
    }

    /**
     * Shuffles a list using the given random instance (Fisher-Yates).
     */
    private static <T> void shuffleList(List<T> list, Random random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            T temp = list.get(index);
            list.set(index, list.get(i));
            list.set(i, temp);
        }
    }

    /**
     * Sets the progress listener for galaxy generation.
     *
     * @param listener the progress listener (may be null)
     */
    public void setProgressListener(GalaxyGenerationProgress listener) {
        this.progressListener = listener;
        this.systemGenerator = new SolarSystemGenerator(random, bodyGenerator, config, progressListener);
    }

    @Override
    public String toString() {
        return "GalaxyGenerator{" + config + "}";
    }
}
