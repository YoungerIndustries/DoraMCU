package com.yisy.modules.doramcu.captainslog.generation;

import com.yisy.modules.doramcu.hex.CelestialBodyGenerator;
import com.yisy.modules.doramcu.hex.GalacticEntry;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import com.yisy.modules.doramcu.hex.GalaxyGenerator;
import com.yisy.modules.doramcu.hex.HyperlaneGenerator;
import com.yisy.modules.doramcu.hex.SolarSystem;
import com.yisy.modules.doramcu.hex.SolarSystemGenerator;
import com.yisy.modules.doramcu.hex.SubSector;
import com.yisy.modules.doramcu.traveller.SectorGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Star Trek-themed galaxy generator for Captain's Log galaxy generation.
 * <p>
 * Extends {@link GalaxyGenerator} with Star Trek quadrant awareness:
 * the 10×10 sector grid is divided into 4 quadrants, each with different
 * sector naming patterns and system density.
 * <p>
 * Quadrant system density:
 * <ul>
 *   <li>Alpha Quadrant (rows 0-4, cols 0-4) — Federation core, higher system density (1.5x)</li>
 *   <li>Beta Quadrant (rows 0-4, cols 5-9) — Klingon/Romulan border, moderate density (1.2x)</li>
 *   <li>Gamma Quadrant (rows 5-9, cols 0-4) — Dominion space, lower density (0.8x)</li>
 *   <li>Delta Quadrant (rows 5-9, cols 5-9) — Far frontier, sparse density (0.5x)</li>
 * </ul>
 * <p>
 * Uses {@link CaptainsLogStarNameDatabase}, {@link CaptainsLogPlanetNameDatabase},
 * and {@link CaptainsLogGalaxyNameDatabase} for all naming operations.
 * <p>
 * Based on design patterns from Star Trek: The Next Generation, Deep Space Nine,
 * Voyager, and Discovery for quadrant-based galaxy structure.
 */
public class CaptainsLogGalaxyGenerator extends GalaxyGenerator {

    /**
     * Default random seed for Captain's Log galaxy generation.
     */
    public static final int DEFAULT_SEED = 2307; // NCC-1701 reference

    /**
     * Alpha Quadrant system density multiplier (Federation core).
     */
    public static final double ALPHA_QUADRANT_DENSITY = 1.5;

    /**
     * Beta Quadrant system density multiplier (Klingon/Romulan border).
     */
    public static final double BETA_QUADRANT_DENSITY = 1.2;

    /**
     * Gamma Quadrant system density multiplier (Dominion space).
     */
    public static final double GAMMA_QUADRANT_DENSITY = 0.8;

    /**
     * Delta Quadrant system density multiplier (Far frontier).
     */
    public static final double DELTA_QUADRANT_DENSITY = 0.5;

    // Captain's Log name databases
    private final CaptainsLogStarNameDatabase starNames;
    private final CaptainsLogPlanetNameDatabase planetNames;
    private final CaptainsLogGalaxyNameDatabase galaxyNames;

    // Progress callback (optional)
    private GalaxyGenerationProgress progressListener;

    // Generation configuration
    private GalaxyGenerationConfig config;

    // Delegated components
    private CelestialBodyGenerator bodyGenerator;
    private SolarSystemGenerator systemGenerator;
    private HyperlaneGenerator hyperlaneGenerator;

    // Traveller integration — enables UWP-based planet generation
    private boolean useTravellerUWP = false;

    private final Random random;

    /**
     * Creates a new generator with default parameters and seed 2307 (NCC-1701).
     */
    public CaptainsLogGalaxyGenerator() {
        this(DEFAULT_SEED);
    }

    /**
     * Creates a new generator with the given random seed for reproducibility.
     *
     * @param seed the random seed
     */
    public CaptainsLogGalaxyGenerator(long seed) {
        this.random = new Random(seed);
        this.starNames = new CaptainsLogStarNameDatabase();
        this.planetNames = new CaptainsLogPlanetNameDatabase();
        this.galaxyNames = new CaptainsLogGalaxyNameDatabase();
        this.config = GalaxyGenerationConfig.builder().build();
        initializeComponents();
    }

    /**
     * Initializes the delegated generator components.
     */
    private void initializeComponents() {
        this.bodyGenerator = new CelestialBodyGenerator(random, starNames, planetNames);
        if (useTravellerUWP) {
            bodyGenerator.setSectorGenerator(new SectorGenerator(random));
        }
        this.systemGenerator = new SolarSystemGenerator(random, bodyGenerator, config, progressListener);
        this.systemGenerator.setUseTravellerUWP(useTravellerUWP);
        this.hyperlaneGenerator = new HyperlaneGenerator(random);
    }

    /**
     * Enables or disables Traveller UWP-based planet generation.
     * <p>
     * When enabled, planets generated during galaxy creation will have
     * Universal World Profile data and trade codes from the Traveller system.
     * </p>
     *
     * @param useTravellerUWP true to enable UWP generation
     */
    public void setUseTravellerUWP(boolean useTravellerUWP) {
        this.useTravellerUWP = useTravellerUWP;
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
        this.systemGenerator.setUseTravellerUWP(useTravellerUWP);
    }

    // ---- Main Generation Methods ----

    /**
     * Generates a complete Star Trek-themed galaxy with the given ID.
     * The galaxy uses a 10x10 2D sector grid with quadrant-aware system density.
     *
     * @param galaxyId the unique identifier for the galaxy
     * @return the generated galaxy
     */
    public Galaxy generateGalaxy(int galaxyId) {
        return generateGalaxy(galaxyId, galaxyNames.getNextName());
    }

    /**
     * Generates a complete Star Trek-themed galaxy with the given ID and name.
     * <p>
     * The galaxy contains 100 sectors in a 10x10 grid divided into 4 Star Trek
     * quadrants. Each quadrant has a different system density multiplier:
     * <ul>
     *   <li>Alpha Quadrant: 1.5x (Federation core)</li>
     *   <li>Beta Quadrant: 1.2x (Klingon/Romulan border)</li>
     *   <li>Gamma Quadrant: 0.8x (Dominion space)</li>
     *   <li>Delta Quadrant: 0.5x (Far frontier)</li>
     * </ul>
     * <p>
     * Sector names use Star Trek conventions (constellation names, canonical
     * sector names). Subsector names use Star Trek star systems.
     *
     * @param galaxyId the unique identifier for the galaxy
     * @param name the name of the galaxy
     * @return the generated galaxy
     */
    public Galaxy generateGalaxy(int galaxyId, String name) {
        Galaxy galaxy = new Galaxy(galaxyId, name);
        galaxy.setHyperlaneDensity(config.getHyperlaneDensity());

        int totalTargetSystems = config.getSystemCount();

        // Calculate base systems per sector, then adjust by quadrant density
        int baseSystemsPerSector = Math.max(1, totalTargetSystems / Galaxy.TOTAL_SECTORS);

        // Notify progress listener of start
        if (progressListener != null) {
            progressListener.onStart(totalTargetSystems);
            progressListener.onLog("[1/3] Generating Captain's Log galaxy: " + name);
            progressListener.onLog("  Base systems per sector: " + baseSystemsPerSector);
            progressListener.onLog("  Quadrant densities: Alpha=" + ALPHA_QUADRANT_DENSITY +
                    ", Beta=" + BETA_QUADRANT_DENSITY +
                    ", Gamma=" + GAMMA_QUADRANT_DENSITY +
                    ", Delta=" + DELTA_QUADRANT_DENSITY);
        }

        // Distribute solar systems across all sectors with quadrant-aware density
        int totalSystems = 0;
        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                Galaxy.Sector sector = galaxy.getSector(row, col);

                // Apply quadrant density multiplier
                double densityMultiplier = getQuadrantDensityMultiplier(row, col);
                int systemsForSector = Math.max(1, (int) (baseSystemsPerSector * densityMultiplier));

                int generated = generateSectorSystems(sector, systemsForSector);
                totalSystems += generated;
                for (SolarSystem system : sector.getAllSystems().values()) {
                    galaxy.getAllSystemsMap().put(system.getId(), system);
                }

                // Report cumulative progress after each sector
                if (progressListener != null) {
                    progressListener.onSystemGenerated(totalSystems, totalTargetSystems);
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
            progressListener.onLog("Captain's Log galaxy " + name + " complete: " + totalSystems + " systems, " + galaxy.getHyperlaneCount() + " hyperlanes");
        }

        return galaxy;
    }

    /**
     * Returns the system density multiplier for the given sector position
     * based on its Star Trek quadrant.
     *
     * @param row the sector row (0-9)
     * @param col the sector column (0-9)
     * @return the density multiplier
     */
    public double getQuadrantDensityMultiplier(int row, int col) {
        String quadrant = CaptainsLogSectorBuilder.getQuadrant(row, col);
        switch (quadrant) {
            case "Alpha Quadrant":
                return ALPHA_QUADRANT_DENSITY;
            case "Beta Quadrant":
                return BETA_QUADRANT_DENSITY;
            case "Gamma Quadrant":
                return GAMMA_QUADRANT_DENSITY;
            case "Delta Quadrant":
                return DELTA_QUADRANT_DENSITY;
            default:
                return 1.0;
        }
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

    /**
     * Returns the Star Trek quadrant for a given sector position.
     *
     * @param row the sector row (0-9)
     * @param col the sector column (0-9)
     * @return the quadrant name (Alpha, Beta, Gamma, or Delta)
     */
    public static String getQuadrant(int row, int col) {
        return CaptainsLogSectorBuilder.getQuadrant(row, col);
    }

    /**
     * Returns the star name database used by this generator.
     *
     * @return the star name database
     */
    public CaptainsLogStarNameDatabase getStarNames() {
        return starNames;
    }

    /**
     * Returns the planet name database used by this generator.
     *
     * @return the planet name database
     */
    public CaptainsLogPlanetNameDatabase getPlanetNames() {
        return planetNames;
    }

    /**
     * Returns the galaxy name database used by this generator.
     *
     * @return the galaxy name database
     */
    public CaptainsLogGalaxyNameDatabase getGalaxyNames() {
        return galaxyNames;
    }

    @Override
    public String toString() {
        return "CaptainsLogGalaxyGenerator{" + config +
                ", starNames=" + starNames +
                ", planetNames=" + planetNames +
                ", galaxyNames=" + galaxyNames + "}";
    }
}
