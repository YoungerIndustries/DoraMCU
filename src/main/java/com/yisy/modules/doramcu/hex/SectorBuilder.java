package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Builder for constructing and populating {@link Galaxy.Sector} objects.
 * <p>
 * The SectorBuilder handles sector naming, solar system distribution, and
 * population of celestial bodies within a sector. It supports both individual
 * sector construction and bulk galaxy-wide sector generation.
 * <p>
 * Sector names follow the Greek letter convention: prefixes (Alpha through Kappa)
 * combined with suffixes (Prime through Decimus), e.g., "Alpha-Prime", "Beta-Secundus".
 * <p>
 * Usage example:
 * <pre>
 * SectorBuilder builder = new SectorBuilder();
 * Galaxy.Sector sector = builder.build(0, 0, 0);
 * // or with systems
 * builder.addSystem(solarSystem, 50, 50, 50);
 * Galaxy.Sector sector = builder.build(0, 0, 0);
 * </pre>
 */
public class SectorBuilder {

    private static final String[] PREFIXES = {
            "Alpha", "Beta", "Gamma", "Delta", "Epsilon",
            "Zeta", "Eta", "Theta", "Iota", "Kappa"
    };

    private static final String[] SUFFIXES = {
            "Prime", "Secundus", "Tertius", "Quartus", "Quintus",
            "Sextus", "Septimus", "Octavus", "Nonus", "Decimus"
    };

    /**
     * Traveller-style trade classification types for sector characterization.
     * Each type describes the dominant economic activity of a sector.
     */
    public static final String[] TRADE_TYPES = {
            "Agricultural",      // Ag – fertile worlds, food production
            "Industrial",        // In – manufacturing and heavy industry
            "High Population",   // Hi – densely populated urban worlds
            "Low Population",    // Lo – sparsely settled frontier worlds
            "Rich",              // Ri – wealthy, resource-rich systems
            "Poor",              // Po – resource-poor, struggling economies
            "Garden",            // Ga – pristine habitable worlds
            "Desert",            // De – arid, dry worlds
            "Water World",       // Wa – ocean-covered planets
            "Vacuum",            // Va – airless, barren worlds
            "Ice-Capped",        // Ic – frozen, cold worlds
            "Fluid Oceans",      // Fl – exotic non-water liquid worlds
            "Non-Agricultural",  // Na – unable to support farming
            "Non-Industrial",    // Ni – pre-industrial technology
            "High Technology",   // Ht – advanced TL 12+ worlds
            "Low Technology",    // Lt – primitive TL 5- worlds
            "Barren",            // Ba – lifeless, unpopulated
            "Asteroid",          // As – asteroid belt systems
            "Hellworld",         // He – hostile, extreme environments
            "Ocean World"        // Oc – fully submerged planets
    };

    /**
     * Generates a Traveller-style trade type name for the given sector position.
     *
     * @param row the sector row (0-9)
     * @param col the sector column (0-9)
     * @return the trade type name (e.g., "Agricultural", "Industrial")
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
     * Creates a new empty SectorBuilder.
     */
    public SectorBuilder() {
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
    public SectorBuilder addSystem(SolarSystem system, int x, int y, int z) {
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
    public SectorBuilder addSystem(SolarSystemPosition position) {
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
    public SectorBuilder withController(Integer playerId) {
        this.controllerPlayerId = playerId;
        return this;
    }

    // ---- Naming ----

    /**
     * Generates the standard sector name for the given grid position.
     *
     * @param row the row (0-9)
     * @param col the column (0-9)
     * @return the sector name (e.g., "Alpha-Prime")
     */
    public static String generateSectorName(int row, int col) {
        if (row < 0 || row >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Row must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + row);
        }
        if (col < 0 || col >= Galaxy.GRID_SIZE) {
            throw new IllegalArgumentException("Col must be between 0 and " + (Galaxy.GRID_SIZE - 1) + ": " + col);
        }
        return PREFIXES[row] + "-" + SUFFIXES[col];
    }

    /**
     * Traveller-style subsector names. Each sector has 100 subsectors, so we need
     * a large pool of names. These follow the Traveller convention of naming
     * subsectors after their capital system or a notable feature.
     */
    private static final String[] SUBSECTOR_NAMES = {
            // Row 0
            "Alphauri", "Bellerophon", "Centauri", "Deneb", "Eridanus",
            "Fomalhaut", "Gienah", "Hamal", "Izar", "Kochab",
            // Row 1
            "Lalande", "Mizar", "Nunki", "Omicron", "Procyon",
            "Regor", "Sirius", "Toliman", "Upsilon", "Vega",
            // Row 2
            "Wolfram", "Xanthos", "Yildun", "Zaurak", "Alderaan",
            "Babylon", "Coruscant", "Dantooine", "Endor", "Felucia",
            // Row 3
            "Gallifrey", "Hoth", "Ithaca", "Jabbar", "Kessel",
            "Lone Star", "Mandalore", "Naboo", "Ossus", "Pandora",
            // Row 4
            "Quantum", "Rigel", "Serenity", "Tatooine", "Utopia",
            "Vulcan", "Wakanda", "Xenon", "Yavin", "Zephyr",
            // Row 5
            "Argo", "Bellerive", "Cassiopeia", "Drayson", "Elysium",
            "Fortuna", "Ganymede", "Helios", "Imbrium", "Janus",
            // Row 6
            "Kepler", "Lazarus", "Meridian", "Nova", "Orion",
            "Pegasus", "Quasar", "Rogue", "Solomon", "Titan",
            // Row 7
            "Ulysses", "Vanguard", "Wraith", "Xenophobe", "Ymir",
            "Zodiar", "Aethelgard", "Bastion", "Crimson", "Dreadnought",
            // Row 8
            "Ember", "Frostbite", "Goliath", "Havoc", "Inferno",
            "Juggernaut", "Kraken", "Luminous", "Maverick", "Nebula",
            // Row 9
            "Obsidian", "Phoenix", "Quicksilver", "Raptor", "Sentinel",
            "Thunder", "Umbra", "Vortex", "Warden", "Zenith"
    };

    /**
     * Generates a Traveller-style subsector name for the given grid position.
     * Names are deterministic based on position, so they persist across restarts.
     *
     * @param subRow the subsector row (0-9)
     * @param subCol the subsector column (0-9)
     * @return the subsector name (e.g., "Alphauri", "Bellerophon", "Lone Star")
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
     * @return the prefix for the given row
     */
    public static String getRowPrefix(int row) {
        if (row < 0 || row >= PREFIXES.length) {
            throw new IllegalArgumentException("Row must be between 0 and " + (PREFIXES.length - 1));
        }
        return PREFIXES[row];
    }

    /**
     * @return the suffix for the given column
     */
    public static String getColSuffix(int col) {
        if (col < 0 || col >= SUFFIXES.length) {
            throw new IllegalArgumentException("Col must be between 0 and " + (SUFFIXES.length - 1));
        }
        return SUFFIXES[col];
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
    }

    /**
     * Distributes solar systems across all subsectors ensuring each subsector
     * gets between {@link #MIN_SYSTEMS_PER_SUBSECTOR} and {@link #MAX_SYSTEMS_PER_SUBSECTOR} systems.
     * <p>
     * The galaxy has 100 sectors × 100 subsectors = 10,000 subsectors total.
     * Each subsector receives 3-10 solar systems within a 10x10x10 3D hexmap.
     *
     * @param systems the solar systems to distribute
     * @param random the random number generator
     * @return list of positioned systems with sector, subsector, and 3D coordinates
     * @throws IllegalArgumentException if there are not enough systems
     */
    public static List<PositionedSystem> distributeSystems(List<SolarSystem> systems, Random random) {
        int totalSubSectors = Galaxy.TOTAL_SUBSECTORS;
        int minTotal = totalSubSectors * MIN_SYSTEMS_PER_SUBSECTOR;

        if (systems.size() < minTotal) {
            throw new IllegalArgumentException(
                    "Not enough systems to fill all subsectors: need at least " + minTotal +
                    " systems for " + totalSubSectors + " subsectors (min " + MIN_SYSTEMS_PER_SUBSECTOR + " per subsector), " +
                    "but only " + systems.size() + " provided");
        }

        // Track how many systems each subsector currently has
        int[] subSectorCounts = new int[totalSubSectors];
        List<PositionedSystem> result = new ArrayList<>(systems.size());

        // Shuffle the systems list for random distribution
        List<SolarSystem> shuffled = new ArrayList<>(systems);
        Collections.shuffle(shuffled, random);

        int systemIndex = 0;

        // Phase 1: Assign MIN_SYSTEMS_PER_SUBSECTOR to each subsector
        for (int subSectorId = 0; subSectorId < totalSubSectors; subSectorId++) {
            int sectorId = subSectorId / Galaxy.TOTAL_SUBSECTORS_PER_SECTOR;
            int subIdInSector = subSectorId % Galaxy.TOTAL_SUBSECTORS_PER_SECTOR;
            int sectorRow = sectorId / Galaxy.GRID_SIZE;
            int sectorCol = sectorId % Galaxy.GRID_SIZE;
            int subRow = subIdInSector / Galaxy.SUBSECTOR_GRID_SIZE;
            int subCol = subIdInSector % Galaxy.SUBSECTOR_GRID_SIZE;

            for (int j = 0; j < MIN_SYSTEMS_PER_SUBSECTOR && systemIndex < shuffled.size(); j++) {
                SolarSystem system = shuffled.get(systemIndex++);
                int x = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
                int y = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
                int z = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
                result.add(new PositionedSystem(system, sectorRow, sectorCol, subRow, subCol, x, y, z));
                subSectorCounts[subSectorId]++;
            }
        }

        // Phase 2: Distribute remaining systems randomly, respecting MAX_SYSTEMS_PER_SUBSECTOR
        while (systemIndex < shuffled.size()) {
            // Find subsectors that haven't reached max capacity
            List<Integer> availableSubSectors = new ArrayList<>();
            for (int subSectorId = 0; subSectorId < totalSubSectors; subSectorId++) {
                if (subSectorCounts[subSectorId] < MAX_SYSTEMS_PER_SUBSECTOR) {
                    availableSubSectors.add(subSectorId);
                }
            }

            if (availableSubSectors.isEmpty()) {
                break;
            }

            int randomSubSector = availableSubSectors.get(random.nextInt(availableSubSectors.size()));
            int sectorId = randomSubSector / Galaxy.TOTAL_SUBSECTORS_PER_SECTOR;
            int subIdInSector = randomSubSector % Galaxy.TOTAL_SUBSECTORS_PER_SECTOR;
            int sectorRow = sectorId / Galaxy.GRID_SIZE;
            int sectorCol = sectorId % Galaxy.GRID_SIZE;
            int subRow = subIdInSector / Galaxy.SUBSECTOR_GRID_SIZE;
            int subCol = subIdInSector % Galaxy.SUBSECTOR_GRID_SIZE;

            SolarSystem system = shuffled.get(systemIndex++);
            int x = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
            int y = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
            int z = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
            result.add(new PositionedSystem(system, sectorRow, sectorCol, subRow, subCol, x, y, z));
            subSectorCounts[randomSubSector]++;
        }

        return result;
    }

    /**
     * Calculates the recommended total number of solar systems for a galaxy.
     * Each subsector gets approximately (MIN + MAX) / 2 systems.
     *
     * @return the recommended total system count (20000 for default settings)
     */
    public static int recommendedSystemCount() {
        int averagePerSubSector = (MIN_SYSTEMS_PER_SUBSECTOR + MAX_SYSTEMS_PER_SUBSECTOR) / 2;
        return Galaxy.TOTAL_SUBSECTORS * averagePerSubSector;
    }

    /**
     * Calculates the minimum total number of solar systems required to fill
     * all subsectors with at least MIN_SYSTEMS_PER_SUBSECTOR systems.
     *
     * @return the minimum system count (10000 for default settings)
     */
    public static int minimumSystemCount() {
        return Galaxy.TOTAL_SUBSECTORS * MIN_SYSTEMS_PER_SUBSECTOR;
    }

    /**
     * Calculates the maximum total number of solar systems that can be distributed
     * without exceeding MAX_SYSTEMS_PER_SUBSECTOR in any subsector.
     *
     * @return the maximum system count (30000 for default settings)
     */
    public static int maximumSystemCount() {
        return Galaxy.TOTAL_SUBSECTORS * MAX_SYSTEMS_PER_SUBSECTOR;
    }

    /**
     * Creates a builder pre-populated with the systems for a specific sector.
     *
     * @param sectorId the sector ID (0-99)
     * @param positionedSystems the list of positioned systems for this sector
     * @return a configured SectorBuilder
     */
    public static SectorBuilder forSector(int sectorId, List<SolarSystemPosition> positionedSystems) {
        int row = sectorId / Galaxy.GRID_SIZE;
        int col = sectorId % Galaxy.GRID_SIZE;

        SectorBuilder builder = new SectorBuilder();
        for (SolarSystemPosition pos : positionedSystems) {
            builder.addSystem(pos);
        }
        return builder;
    }

    // ---- Accessors ----

    /**
     * @return the number of systems configured in this builder
     */
    public int getSystemCount() {
        return systems.size();
    }

    /**
     * @return all configured systems (unmodifiable)
     */
    public List<SolarSystem> getSystems() {
        return Collections.unmodifiableList(systems);
    }

    /**
     * @return all configured system positions (unmodifiable)
     */
    public List<SolarSystemPosition> getSystemPositions() {
        return Collections.unmodifiableList(systemPositions);
    }

    /**
     * @return the controller player ID, or null if uncontrolled
     */
    public Integer getControllerPlayerId() {
        return controllerPlayerId;
    }

    /**
     * Clears all configured systems and positions from this builder.
     *
     * @return this builder for method chaining
     */
    public SectorBuilder clear() {
        systems.clear();
        systemPositions.clear();
        controllerPlayerId = null;
        return this;
    }

    @Override
    public String toString() {
        return String.format("SectorBuilder{systems=%d, controller=%s}", systems.size(), controllerPlayerId);
    }
}
