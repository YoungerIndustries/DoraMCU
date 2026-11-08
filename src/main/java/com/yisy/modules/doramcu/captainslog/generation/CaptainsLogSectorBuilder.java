package com.yisy.modules.doramcu.captainslog.generation;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.SolarSystem;

import java.util.*;

/**
 * Star Trek-themed sector builder for Captain's Log galaxy generation.
 * <p>
 * Replaces generic Greek/Latin naming with Star Trek conventions:
 * constellation-based sector names, Star Trek star systems for subsectors,
 * and quadrant-aware trade classifications.
 * <p>
 * The 10×10 sector grid is divided into 4 Star Trek quadrants:
 * <ul>
 *   <li>Alpha Quadrant (rows 0-4, cols 0-4) — Federation core space</li>
 *   <li>Beta Quadrant (rows 0-4, cols 5-9) — Klingon/Romulan space</li>
 *   <li>Gamma Quadrant (rows 5-9, cols 0-4) — Dominion space</li>
 *   <li>Delta Quadrant (rows 5-9, cols 5-9) — Far frontier</li>
 * </ul>
 * <p>
 * Sector names use constellation names and canonical Star Trek sector names
 * (e.g., "Bajor Sector", "Algira Sector", "Cygnus Sector").
 * Subsector names use Star Trek star systems (e.g., "Wolf 359", "40 Eridani",
 * "Beta Orionis").
 * <p>
 * Usage example:
 * <pre>
 * CaptainsLogSectorBuilder builder = new CaptainsLogSectorBuilder();
 * Galaxy.Sector sector = builder.build(0, 0, 0);
 * // or with systems
 * builder.addSystem(solarSystem, 50, 50, 50);
 * Galaxy.Sector sector = builder.build(0, 0, 0);
 * </pre>
 */
public class CaptainsLogSectorBuilder {

    /**
     * Constellation-based and canonical Star Trek sector names for the 10×10 grid.
     * Arranged to map quadrants to their thematic regions.
     * <p>
     * Alpha Quadrant (indices 0-49): Federation core worlds and allied space.
     * Beta Quadrant (indices 50-99): Klingon, Romulan, and border regions.
     * Gamma Quadrant (indices 100-149): Dominion and wormhole-adjacent space.
     * Delta Quadrant (indices 150-199): Far frontier and uncharted territory.
     * </p>
     */
    private static final String[] SECTOR_NAMES = {
            // --- Alpha Quadrant: Federation Core (rows 0-4, cols 0-4) ---
            // Row 0
            "Sol", "Janus", "Bajor", "Algira", "Arawath",
            // Row 1
            "Trill", "Betazed", "Elba", "Deneb", "Regula",
            // Row 2
            "Ajilon", "Ganalda", "Cygnus", "Orion", "Lyra",
            // Row 3
            "Andromeda", "Cassiopeia", "Draco", "Phoenix", "Aquila",
            // Row 4
            "Pegasus", "Perseus", "Hercules", "Sagittarius", "Centaurus",
            // --- Alpha Quadrant continued (rows 0-4, cols 5-9) ---
            // Row 0
            "Arbazan", "Argus", "Berengarius", "Balancar", "Antos",
            // Row 1
            "Yonada", "NiVar", "Risa", "Vendikar", "Eminiar",
            // Row 2
            "Draylon", "Dubhe", "Antares", "Aldebaran", "Archanis",
            // Row 3
            "Argelius", "Azure", "Baaja", "BMoth", "Taurus",
            // Row 4
            "Gemini", "Leo", "Scorpius", "Ophiuchus", "Corona",
            // --- Beta Quadrant: Klingon/Romulan Space (rows 5-9, cols 0-4) ---
            // Row 5
            "Klingon", "Romulan", "Remus", "QoNoS", "Chintoka",
            // Row 6
            "Andoria", "Procyon", "Tellar", "Cygni", "Eridani",
            // Row 7
            "Cardassia", "Ferengi", "Borg", "Gorn", "Tzenkethi",
            // Row 8
            "Orion", "Maquis", "Dominion", "Breen", "Son'a",
            // Row 9
            "Metron", "Hyrrian", "Tholian", "Kzinti", "Excalbian",
            // --- Beta Quadrant continued (rows 5-9, cols 5-9) ---
            // Row 5
            "Neutral Zone", "Beta Lankal", "Beta Thoridar", "Kelvas", "Dilklor",
            // Row 6
            "Sirius", "Vega", "Altair", "Wolf 359", "Barnard",
            // Row 7
            "Ceti Alpha", "Fomalhaut", "Achernar", "Canopus", "Naos",
            // Row 8
            "Torment", "Sheliak", "Rasalhague", "Mirfak", "Diphda",
            // Row 9
            "Algenib", "Markab", "Scheat", "Alcyone", "Electra",
            // --- Gamma Quadrant: Dominion Space (rows already covered, extending naming) ---
            // These are already assigned above in the 10x10 grid layout.
            // The array has 100 entries for 10x10 sectors.
            // --- Additional names for subsector fallback ---
            // (Not used directly in SECTOR_NAMES, but listed for reference)
    };

    /**
     * 100 Star Trek-themed subsector names based on canonical star systems
     * and locations from the Star Trek universe.
     */
    private static final String[] SUBSECTOR_NAMES = {
            // Row 0 — Federation core systems
            "Wolf 359", "40 Eridani", "Beta Orionis", "61 Cygni", "Procyon",
            "Sirius", "Vega", "Altair", "Deneb", "Ceti Alpha",
            // Row 1 — Federation colonies
            "Barnard", "Dubhe", "Aldebaran", "Antares", "Archanis",
            "Ajilon", "Ganalda", "Rigel", "Betelgeuse", "Pollux",
            // Row 2 — Border worlds
            "Castor", "Spica", "Regulus", "Capella", "Arcturus",
            "Acrux", "Alnilam", "Mintaka", "Saiph", "Naos",
            // Row 3 — Neutral zone systems
            "Fomalhaut", "Achernar", "Canopus", "Torment", "Sheliak",
            "Rasalhague", "Mirfak", "Diphda", "Algenib", "Markab",
            // Row 4 — Klingon space
            "Scheat", "Alcyone", "Electra", "Maia", "Merope",
            "Taygeta", "Celaeno", "Asterope", "Atlas", "Pleione",
            // Row 5 — Romulan space
            "Thuban", "Unukalhai", "Vindemiatrix", "Wezen", "Albireo",
            "Alpheratz", "Mirach", "Altarf", "Hamal", "Miram",
            // Row 6 — Cardassian space
            "Sarin", "Tereus", "Algorab", "Peacock", "Grumium",
            "Kaus", "Sceptrum", "Avior", "Torcular", "Atria",
            // Row 7 — Ferengi space
            "Sadr", "Nair", "Polaris", "Caph", "Cebalrai",
            "Cordoba", "Dabih", "Alphard", "Gacrux", "Menkalinan",
            // Row 8 — Gamma quadrant
            "Alphercam", "Minkar", "Phact", "Alsephina", "Alshat",
            "Alchiba", "Alkalurops", "Alrami", "Aljanah", "Alphard Minor",
            // Row 9 — Delta quadrant
            "Zuben Elgenubi", "Zuben Eschamali", "Zeta Reticuli",
            "Yed Prior", "Yed Posterior", "Xamidimura", "Wurrem",
            "Wezen II", "Vela Pulsar", "Upsilon Andromedae"
    };

    /**
     * Star Trek-style sector classifications replacing Traveller trade types.
     * Each type describes the political or strategic nature of a sector.
     */
    public static final String[] TRADE_TYPES = {
            "Federation Core",       // Heart of Federation space, well-defended
            "Federation Border",     // Edge of Federation territory
            "Klingon Space",         // Klingon Empire territory
            "Romulan Space",         // Romulan Star Empire territory
            "Neutral Zone",          // Demilitarized buffer zone
            "Cardassian Space",      // Cardassian Union territory
            "Ferengi Space",         // Ferengi Alliance territory
            "Dominion Space",        // Dominion-controlled region
            "Borg Space",            // Borg collective territory
            "Uncharted",             // Unexplored frontier
            "Wormhole Junction",     // Near a stable wormhole
            "Nebula Region",         // Dense nebula, sensor interference
            "Asteroid Belt",         // Heavy asteroid concentration
            "Supernova Zone",        // Recent supernova activity
            "Pulsar Field",          // Pulsar radiation hazards
            "Dark Matter",           // Anomalous dark matter readings
            "Temporal Anomaly",      // Time stream instability
            "Subspace Rift",         // Subspace distortion zone
            "Red Matter",            // Red matter singularity risk
            "Warp Storm"             // Persistent warp storm activity
    };

    /**
     * Determines the Star Trek quadrant for a given sector position.
     *
     * @param row the sector row (0-9)
     * @param col the sector column (0-9)
     * @return the quadrant name
     */
    public static String getQuadrant(int row, int col) {
        if (row < 0 || row >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Row must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + row);
        }
        if (col < 0 || col >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Col must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + col);
        }
        if (row < 5 && col < 5) {
            return "Alpha Quadrant";
        } else if (row < 5 && col >= 5) {
            return "Beta Quadrant";
        } else if (row >= 5 && col < 5) {
            return "Gamma Quadrant";
        } else {
            return "Delta Quadrant";
        }
    }

    /**
     * Generates a Star Trek-style trade type name for the given sector position.
     *
     * @param row the sector row (0-9)
     * @param col the sector column (0-9)
     * @return the trade type name (e.g., "Federation Core", "Klingon Space")
     */
    public static String generateTradeType(int row, int col) {
        if (row < 0 || row >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Row must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + row);
        }
        if (col < 0 || col >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Col must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + col);
        }
        int index = (row * Galaxy.GRID_SIZE + col) % TRADE_TYPES.length;
        return TRADE_TYPES[index];
    }

    private final List<SolarSystem> systems;
    private final List<SolarSystemPosition> systemPositions;
    private Integer controllerPlayerId;

    /**
     * Holds a solar system along with its 3D position within a sector.
     */
    public static class SolarSystemPosition {
        private final SolarSystem system;
        private final int x;
        private final int y;
        private final int z;

        /**
         * Creates a new positioned solar system.
         *
         * @param system the solar system
         * @param x the x coordinate (0-99)
         * @param y the y coordinate (0-99)
         * @param z the z coordinate (0-99)
         */
        public SolarSystemPosition(SolarSystem system, int x, int y, int z) {
            if (system == null) {
                throw new IllegalArgumentException("System must not be null");
            }
            if (x < 0 || x >= Galaxy.SUBSECTOR_DIMENSION || y < 0 || y >= Galaxy.SUBSECTOR_DIMENSION || z < 0 || z >= Galaxy.SUBSECTOR_DIMENSION) {
                throw new IllegalArgumentException("Position must be within subsector bounds [0, " + Galaxy.SUBSECTOR_DIMENSION + "): (" + x + ", " + y + ", " + z + ")");
            }
            this.system = system;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * @return the solar system
         */
        public SolarSystem getSystem() {
            return system;
        }

        /**
         * @return the x coordinate
         */
        public int getX() {
            return x;
        }

        /**
         * @return the y coordinate
         */
        public int getY() {
            return y;
        }

        /**
         * @return the z coordinate
         */
        public int getZ() {
            return z;
        }

        @Override
        public String toString() {
            return String.format("SolarSystemPosition{system=%s, pos=(%d,%d,%d)}", system, x, y, z);
        }
    }

    /**
     * Creates a new empty CaptainsLogSectorBuilder.
     */
    public CaptainsLogSectorBuilder() {
        this.systems = new ArrayList<>();
        this.systemPositions = new ArrayList<>();
        this.controllerPlayerId = null;
    }

    // ---- Configuration ----

    /**
     * Adds a solar system to be placed in the sector at the given 3D position.
     *
     * @param system the solar system to add
     * @param x the x coordinate (0-99)
     * @param y the y coordinate (0-99)
     * @param z the z coordinate (0-99)
     * @return this builder for method chaining
     */
    public CaptainsLogSectorBuilder addSystem(SolarSystem system, int x, int y, int z) {
        SolarSystemPosition position = new SolarSystemPosition(system, x, y, z);
        systems.add(system);
        systemPositions.add(position);
        return this;
    }

    /**
     * Adds a positioned solar system to be placed in the sector.
     *
     * @param position the positioned solar system
     * @return this builder for method chaining
     */
    public CaptainsLogSectorBuilder addSystem(SolarSystemPosition position) {
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        systems.add(position.getSystem());
        systemPositions.add(position);
        return this;
    }

    /**
     * Sets the player controlling this sector.
     *
     * @param playerId the player ID, or null to leave uncontrolled
     * @return this builder for method chaining
     */
    public CaptainsLogSectorBuilder withController(Integer playerId) {
        this.controllerPlayerId = playerId;
        return this;
    }

    // ---- Naming ----

    /**
     * Generates the Star Trek sector name for the given grid position.
     * Names are drawn from the SECTOR_NAMES array, which maps to the 10×10 grid.
     *
     * @param row the row (0-9)
     * @param col the column (0-9)
     * @return the sector name (e.g., "Sol", "Bajor", "Klingon")
     */
    public static String generateSectorName(int row, int col) {
        if (row < 0 || row >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Row must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + row);
        }
        if (col < 0 || col >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Col must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + col);
        }
        int index = row * Galaxy.GRID_SIZE + col;
        if (index < SECTOR_NAMES.length) {
            return SECTOR_NAMES[index] + " Sector";
        }
        // Fallback for any unexpected positions
        return String.format("Sector-%02d%02d", row, col);
    }

    /**
     * Generates a Star Trek subsector name for the given grid position.
     * Names are deterministic based on position, so they persist across restarts.
     *
     * @param subRow the subsector row (0-9)
     * @param subCol the subsector column (0-9)
     * @return the subsector name (e.g., "Wolf 359", "40 Eridani", "Beta Orionis")
     */
    public static String generateSubSectorName(int subRow, int subCol) {
        if (subRow < 0 || subRow >= Galaxy.SUBSECTOR_GRID_SIZE) {
            throw new IllegalArgumentException("SubRow must be between 0 and " + (Galaxy.SUBSECTOR_GRID_SIZE - 1) + ": " + subRow);
        }
        if (subCol < 0 || subCol >= Galaxy.SUBSECTOR_GRID_SIZE) {
            throw new IllegalArgumentException("SubCol must be between 0 and " + (Galaxy.SUBSECTOR_GRID_SIZE - 1) + ": " + subCol);
        }
        int index = subRow * Galaxy.SUBSECTOR_GRID_SIZE + subCol;
        if (index < SUBSECTOR_NAMES.length) {
            return SUBSECTOR_NAMES[index];
        }
        // Fallback for any unexpected positions
        return String.format("SS-%02d%02d", subRow, subCol);
    }

    /**
     * @return the quadrant for the given row
     */
    public static String getRowQuadrant(int row) {
        if (row < 0 || row >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Row must be between 0 and " + (Galaxy.GRID_SIZE - 1));
        }
        return row < 5 ? "Alpha/Beta" : "Gamma/Delta";
    }

    /**
     * @return the quadrant for the given column
     */
    public static String getColQuadrant(int col) {
        if (col < 0 || col >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Col must be between 0 and " + (Galaxy.GRID_SIZE - 1));
        }
        return col < 5 ? "Alpha/Gamma" : "Beta/Delta";
    }

    // ---- Building ----

    /**
     * Builds a new Galaxy.Sector with the configured systems and settings.
     *
     * @param sectorId the unique identifier for this sector (row * 10 + col)
     * @param row the row position in the 10x10 grid (0-9)
     * @param col the column position in the 10x10 grid (0-9)
     * @return the built sector
     */
    public Galaxy.Sector build(int sectorId, int row, int col) {
        return build(sectorId, row, col, generateSectorName(row, col));
    }

    /**
     * Builds a new Galaxy.Sector with the configured systems and a custom name.
     *
     * @param sectorId the unique identifier for this sector
     * @param row the row position in the 10x10 grid (0-9)
     * @param col the column position in the 10x10 grid (0-9)
     * @param name the custom display name
     * @return the built sector
     */
    public Galaxy.Sector build(int sectorId, int row, int col, String name) {
        Galaxy.Sector sector = new Galaxy.Sector(sectorId, row, col, name);
        sector.setControllerPlayerId(controllerPlayerId);

        for (SolarSystemPosition pos : systemPositions) {
            sector.addSystem(pos.getSystem(), 0, 0, pos.getX(), pos.getY(), pos.getZ());
        }

        return sector;
    }

    /**
     * Builds a new Galaxy.Sector with the configured systems and a custom name.
     *
     * @param sectorId the unique identifier for this sector
     * @param row the row position in the 10x10 grid (0-9)
     * @param col the column position in the 10x10 grid (0-9)
     * @param name the custom display name
     * @param controllerId the player ID controlling this sector
     * @return the built sector
     */
    public Galaxy.Sector build(int sectorId, int row, int col, String name, Integer controllerId) {
        Galaxy.Sector sector = new Galaxy.Sector(sectorId, row, col, name);
        sector.setControllerPlayerId(controllerId != null ? controllerId : controllerPlayerId);

        for (SolarSystemPosition pos : systemPositions) {
            sector.addSystem(pos.getSystem(), 0, 0, pos.getX(), pos.getY(), pos.getZ());
        }

        return sector;
    }

    // ---- Bulk Operations ----

    /**
     * Generates a random 3D position within a subsector.
     *
     * @param random the random number generator
     * @return array of {x, y, z} coordinates
     */
    public static int[] generateRandomPosition(Random random) {
        int x = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
        int y = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
        int z = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
        return new int[]{x, y, z};
    }

    /**
     * Minimum number of solar systems per subsector.
     */
    public static final int MIN_SYSTEMS_PER_SUBSECTOR = Galaxy.MIN_SYSTEMS_PER_SUBSECTOR;

    /**
     * Maximum number of solar systems per subsector.
     */
    public static final int MAX_SYSTEMS_PER_SUBSECTOR = Galaxy.MAX_SYSTEMS_PER_SUBSECTOR;

    /**
     * Holds a solar system along with its sector, subsector, and 3D position.
     */
    public static class PositionedSystem {
        private final SolarSystem system;
        private final int sectorRow;
        private final int sectorCol;
        private final int subRow;
        private final int subCol;
        private final int x;
        private final int y;
        private final int z;

        public PositionedSystem(SolarSystem system, int sectorRow, int sectorCol,
                                int subRow, int subCol, int x, int y, int z) {
            this.system = system;
            this.sectorRow = sectorRow;
            this.sectorCol = sectorCol;
            this.subRow = subRow;
            this.subCol = subCol;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public SolarSystem getSystem() { return system; }
        public int getSectorRow() { return sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public int getSubRow() { return subRow; }
        public int getSubCol() { return subCol; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }

        @Override
        public String toString() {
            return String.format("PositionedSystem{system=%s, sector=(%d,%d), sub=(%d,%d), pos=(%d,%d,%d)}",
                    system, sectorRow, sectorCol, subRow, subCol, x, y, z);
        }
    }

    /**
     * Generates all 100 sectors for a complete galaxy grid.
     *
     * @return a list of all 100 sectors in row-major order
     */
    public static List<Galaxy.Sector> generateAllSectors() {
        List<Galaxy.Sector> sectors = new ArrayList<>(Galaxy.TOTAL_SECTORS);
        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                int sectorId = row * Galaxy.GRID_SIZE + col;
                String name = generateSectorName(row, col);
                Galaxy.Sector sector = new Galaxy.Sector(sectorId, row, col, name);
                sector.setTradeType(generateTradeType(row, col));
                sectors.add(sector);
            }
        }
        return sectors;
    }

    /**
     * Generates a list of all 100 subsector names for a sector.
     *
     * @return a list of 100 subsector names in row-major order
     */
    public static List<String> generateAllSubSectorNames() {
        List<String> names = new ArrayList<>(Galaxy.TOTAL_SUBSECTORS_PER_SECTOR);
        for (int row = 0; row < Galaxy.SUBSECTOR_GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.SUBSECTOR_GRID_SIZE; col++) {
                names.add(generateSubSectorName(row, col));
            }
        }
        return names;
    }

    /**
     * Generates a list of all 100 sector names for a galaxy.
     *
     * @return a list of 100 sector names in row-major order
     */
    public static List<String> generateAllSectorNames() {
        List<String> names = new ArrayList<>(Galaxy.TOTAL_SECTORS);
        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                names.add(generateSectorName(row, col));
            }
        }
        return names;
    }

    /**
     * Returns the quadrant distribution summary for the galaxy grid.
     *
     * @return a map of quadrant names to sector counts
     */
    public static Map<String, Integer> getQuadrantDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("Alpha Quadrant", 0);
        distribution.put("Beta Quadrant", 0);
        distribution.put("Gamma Quadrant", 0);
        distribution.put("Delta Quadrant", 0);

        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                String quadrant = getQuadrant(row, col);
                distribution.put(quadrant, distribution.get(quadrant) + 1);
            }
        }
        return distribution;
    }

    @Override
    public String toString() {
        return String.format("CaptainsLogSectorBuilder{systems=%d, controller=%s}",
                systems.size(), controllerPlayerId);
    }
}
