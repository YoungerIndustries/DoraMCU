package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A galaxy containing 100 2D sectors arranged in a 10x10 grid.
 * <p>
 * Each sector contains a 10x10 hex grid of subsectors. Each subsector is a
 * 3D hexmap of 10x10x10 dimensions containing 1-3 solar systems.
 * The galaxy hierarchy is:
 * Galaxy → Sector (10x10 grid) → SubSector (10x10 hex grid) → SolarSystem (3D hexmap) → SolarSystemEntity → Celestial Bodies.
 * <p>
 * Solar systems are connected by hyperlanes for faster-than-light travel.
 * <p>
 * Each Galaxy instance represents a single galaxy in the game world.
 * <p>
 * Based on design patterns from Stellaris (sectors, hyperlanes) and
 * Galactic Civilizations IV (galactic core, strategic locations).
 */
public class Galaxy {

    public static final int GRID_SIZE = 10;
    public static final int TOTAL_SECTORS = 100;
    public static final int SUBSECTOR_GRID_SIZE = 10;
    public static final int TOTAL_SUBSECTORS_PER_SECTOR = 100;
    public static final int TOTAL_SUBSECTORS = TOTAL_SECTORS * TOTAL_SUBSECTORS_PER_SECTOR;
    public static final int SUBSECTOR_DIMENSION = 10;
    public static final int MIN_SYSTEMS_PER_SUBSECTOR = 1;
    public static final int MAX_SYSTEMS_PER_SUBSECTOR = 3;
    public static final double DEFAULT_HYPERLANE_DENSITY = 1.0;

    // SubSector, Hyperlane, and GalacticEntry are now standalone classes

    /**
     * A sector in the 10x10 galaxy grid.
     * Each sector contains a 10x10 hex grid of subsectors.
     * Sectors are identified by their 2D grid coordinates (row, col) where
     * row and col range from 0 to 9.
     */
    public static class Sector {
        private final int id;
        private final int row;
        private final int col;
        private String name;
        private String tradeType;
        private final SubSector[][] subGrid;
        private final Map<Integer, SolarSystem> allSystems;
        private Integer controllerPlayerId;
        private GalaxyGenerationProgress progressListener;

        /**
         * Creates a new sector at the given grid position with 100 empty subsectors.
         *
         * @param id the unique identifier for this sector
         * @param row the row position in the 10x10 grid (0-9)
         * @param col the column position in the 10x10 grid (0-9)
         * @param name the display name of the sector
         */
        public Sector(int id, int row, int col, String name) {
            this(id, row, col, name, SectorBuilder.generateTradeType(row, col));
        }

        /**
         * Creates a new sector with an explicit trade type.
         *
         * @param id the unique identifier for this sector
         * @param row the row position in the 10x10 grid (0-9)
         * @param col the column position in the 10x10 grid (0-9)
         * @param name the display name of the sector
         * @param tradeType the Traveller-style trade classification
         */
        public Sector(int id, int row, int col, String name, String tradeType) {
            if (row < 0 || row >= GRID_SIZE) {
                throw new IllegalArgumentException("Row must be between 0 and " + (GRID_SIZE - 1) + ": " + row);
            }
            if (col < 0 || col >= GRID_SIZE) {
                throw new IllegalArgumentException("Col must be between 0 and " + (GRID_SIZE - 1) + ": " + col);
            }
            this.id = id;
            this.row = row;
            this.col = col;
            this.name = name;
            this.tradeType = tradeType != null ? tradeType : SectorBuilder.generateTradeType(row, col);
            this.subGrid = new SubSector[SUBSECTOR_GRID_SIZE][SUBSECTOR_GRID_SIZE];
            this.allSystems = new HashMap<>();
            this.controllerPlayerId = null;
            initializeSubSectors();
        }

        /**
         * Initializes all 100 subsectors in the 10x10 grid.
         */
        void initializeSubSectors() {
            int subId = 0;
            for (int r = 0; r < SUBSECTOR_GRID_SIZE; r++) {
                for (int c = 0; c < SUBSECTOR_GRID_SIZE; c++) {
                    String subName = SectorBuilder.generateSubSectorName(r, c);
                    subGrid[r][c] = new SubSector(subId++, r, c, subName);
                }
            }
        }

        /**
         * @return the unique identifier for this sector
         */
        public int getId() {
            return id;
        }

        /**
         * @return the row position in the 10x10 grid (0-9)
         */
        public int getRow() {
            return row;
        }

        /**
         * @return the column position in the 10x10 grid (0-9)
         */
        public int getCol() {
            return col;
        }

        /**
         * @return the display name of the sector
         */
        public String getName() {
            return name;
        }

        /**
         * @return the Traveller-style trade classification for this sector
         */
        public String getTradeType() {
            return tradeType;
        }

        /**
         * Sets the display name of this sector.
         * Allows renaming sectors at runtime so the new name persists to the database.
         *
         * @param name the new display name
         */
        public void setName(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Sector name must not be null or blank");
            }
            this.name = name;
        }

        /**
         * Sets the Traveller-style trade classification for this sector.
         *
         * @param tradeType the new trade type
         */
        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        /**
         * @return the subsector at the given grid position
         */
        public SubSector getSubSector(int subRow, int subCol) {
            if (subRow < 0 || subRow >= SUBSECTOR_GRID_SIZE || subCol < 0 || subCol >= SUBSECTOR_GRID_SIZE) {
                throw new IllegalArgumentException("Subsector position out of bounds: (" + subRow + ", " + subCol + ")");
            }
            return subGrid[subRow][subCol];
        }

        /**
         * @return all subsectors in this sector (unmodifiable list)
         */
        public List<SubSector> getAllSubSectors() {
            List<SubSector> list = new ArrayList<>(TOTAL_SUBSECTORS_PER_SECTOR);
            for (int r = 0; r < SUBSECTOR_GRID_SIZE; r++) {
                for (int c = 0; c < SUBSECTOR_GRID_SIZE; c++) {
                    list.add(subGrid[r][c]);
                }
            }
            return Collections.unmodifiableList(list);
        }

        /**
         * Retrieves a subsector by its linear index (0-99).
         * Index is calculated as row * 10 + col.
         *
         * @param index the linear index (0-99)
         * @return the subsector at that index
         * @throws IndexOutOfBoundsException if index is out of range
         */
        public SubSector getSubSectorByIndex(int index) {
            if (index < 0 || index >= TOTAL_SUBSECTORS_PER_SECTOR) {
                throw new IndexOutOfBoundsException("Index must be between 0 and " + (TOTAL_SUBSECTORS_PER_SECTOR - 1) + ": " + index);
            }
            int subRow = index / SUBSECTOR_GRID_SIZE;
            int subCol = index % SUBSECTOR_GRID_SIZE;
            return subGrid[subRow][subCol];
        }

        /**
         * Adds a solar system to the specified subsector at the given 3D position.
         *
         * @param system the solar system to add
         * @param subRow the subsector row (0-9)
         * @param subCol the subsector column (0-9)
         * @param x the x coordinate within the subsector (0-99)
         * @param y the y coordinate within the subsector (0-99)
         * @param z the z coordinate within the subsector (0-99)
         * @return the previous entry at that position, or null
         */
        public GalacticEntry addSystem(SolarSystem system, int subRow, int subCol, int x, int y, int z) {
            SubSector subSector = getSubSector(subRow, subCol);
            GalacticEntry entry = subSector.addSystem(system, x, y, z);
            allSystems.put(system.getId(), system);
            return entry;
        }

        /**
         * Adds a solar system to subsector (0,0) at the given 3D position (legacy method).
         *
         * @param system the solar system to add
         * @param x the x coordinate within the subsector (0-99)
         * @param y the y coordinate within the subsector (0-99)
         * @param z the z coordinate within the subsector (0-99)
         * @return the previous entry at that position, or null
         */
        public GalacticEntry addSystem(SolarSystem system, int x, int y, int z) {
            return addSystem(system, 0, 0, x, y, z);
        }

        /**
         * Removes a solar system from this sector.
         *
         * @param systemId the solar system ID to remove
         * @return the removed solar system, or null if not present
         */
        public SolarSystem removeSystem(int systemId) {
            SolarSystem system = allSystems.remove(systemId);
            if (system != null) {
                for (int r = 0; r < SUBSECTOR_GRID_SIZE; r++) {
                    for (int c = 0; c < SUBSECTOR_GRID_SIZE; c++) {
                        subGrid[r][c].removeSystem(systemId);
                    }
                }
            }
            return system;
        }

        /**
         * Retrieves a solar system by its ID across all subsectors.
         *
         * @param systemId the solar system's unique identifier
         * @return the solar system, or null if not present
         */
        public SolarSystem getSystem(int systemId) {
            return allSystems.get(systemId);
        }

        /**
         * @return the total number of solar systems in this sector (across all subsectors)
         */
        public int size() {
            return allSystems.size();
        }

        /**
         * @return all solar systems in this sector (unmodifiable map)
         */
        public Map<Integer, SolarSystem> getAllSystems() {
            return Collections.unmodifiableMap(allSystems);
        }

        /**
         * @return all solar systems with their positions across all subsectors (unmodifiable list)
         */
        public Collection<GalacticEntry> getAllEntries() {
            List<GalacticEntry> entries = new ArrayList<>();
            for (int r = 0; r < SUBSECTOR_GRID_SIZE; r++) {
                for (int c = 0; c < SUBSECTOR_GRID_SIZE; c++) {
                    entries.addAll(subGrid[r][c].getAllEntries());
                }
            }
            return Collections.unmodifiableList(entries);
        }


        /**
         * @return the player ID controlling this sector, or null if uncontrolled
         */
        public Integer getControllerPlayerId() {
            return controllerPlayerId;
        }

        /**
         * Sets the player controlling this sector.
         *
         * @param playerId the player ID, or null to relinquish control
         */
        public void setControllerPlayerId(Integer playerId) {
            this.controllerPlayerId = playerId;
        }

        /**
         * @return true if this sector is controlled by a player
         */
        public boolean isControlled() {
            return controllerPlayerId != null;
        }

        /**
         * Sets the progress listener for system generation callbacks.
         *
         * @param listener the progress listener
         */
        public void setProgressListener(GalaxyGenerationProgress listener) {
            this.progressListener = listener;
        }

        /**
         * Generates all 100 subsectors in this sector.
         * Each subsector is populated with 1-3 solar systems with random positions and celestial bodies.
         * Already generated subsectors are skipped.
         *
         * @param random the random number generator for reproducibility
         * @param starNames the star name database for naming stars
         * @param planetNames the planet name database for naming planets
         * @return the total number of solar systems generated across all subsectors
         */
        public int generateAllSubSectors(Random random, StarNameDatabase starNames, PlanetNameDatabase planetNames) {
            int totalGenerated = 0;
            for (int r = 0; r < SUBSECTOR_GRID_SIZE; r++) {
                for (int c = 0; c < SUBSECTOR_GRID_SIZE; c++) {
                    SubSector subSector = subGrid[r][c];
                    subSector.setProgressListener(progressListener);
                    List<SolarSystem> generated = subSector.generateSystems(random, starNames, planetNames);
                    for (SolarSystem system : generated) {
                        allSystems.put(system.getId(), system);
                    }
                    totalGenerated += generated.size();
                }
            }
            return totalGenerated;
        }

        @Override
        public String toString() {
            return String.format("Sector{id=%d, name='%s', row=%d, col=%d, systems=%d, subSectors=%d}",
                    id, name, row, col, allSystems.size(), TOTAL_SUBSECTORS_PER_SECTOR);
        }
    }

    // Hyperlane and GalacticEntry are now standalone classes

    private final int id;
    private final String name;
    private final Sector[][] grid;
    private final Map<Integer, SolarSystem> allSystems;
    private final List<Hyperlane> hyperlanes;
    private double hyperlaneDensity;

    // ---- Solar system tracking (for home systems) ----
    private final Map<Integer, SolarSystem> solarSystems;
    private int nextSystemId;

    // ---- Constructors ----

    /**
     * Creates a new galaxy with the given ID and default name.
     * Initializes a 10x10 sector grid with 100 empty sectors.
     *
     * @param id the unique identifier for this galaxy
     */
    public Galaxy(int id) {
        this(id, "Galaxy-" + id);
    }

    /**
     * Creates a new galaxy with the given ID and name.
     * Initializes a 10x10 sector grid with 100 empty sectors.
     *
     * @param id the unique identifier for this galaxy
     * @param name the display name of the galaxy
     */
    public Galaxy(int id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Galaxy name must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.grid = new Sector[GRID_SIZE][GRID_SIZE];
        this.allSystems = new HashMap<>();
        this.hyperlanes = new ArrayList<>();
        this.hyperlaneDensity = DEFAULT_HYPERLANE_DENSITY;
        this.solarSystems = new HashMap<>();
        this.nextSystemId = 1;
        initializeGrid();
    }

    // ---- Initialization ----

    /**
     * Initializes the 10x10 sector grid with named sectors using SectorBuilder.
     */
    private void initializeGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int sectorId = row * GRID_SIZE + col;
                SectorBuilder builder = new SectorBuilder();
                grid[row][col] = builder.build(sectorId, row, col);
            }
        }
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this galaxy
     */
    public int getId() {
        return id;
    }

    /**
     * @return the display name of the galaxy
     */
    public String getName() {
        return name;
    }

    /**
     * @return the grid size (10)
     */
    public int getGridSize() {
        return GRID_SIZE;
    }

    /**
     * @return the total number of sectors (100)
     */
    public int getTotalSectors() {
        return TOTAL_SECTORS;
    }

    /**
     * @return the number of solar systems in the galaxy
     */
    public int size() {
        return allSystems.size();
    }

    /**
     * @return the hyperlane density multiplier (0.5 to 2.75)
     */
    public double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    /**
     * Sets the hyperlane density multiplier.
     *
     * @param density the density multiplier (0.5 to 2.75)
     * @throws IllegalArgumentException if density is out of range
     */
    public void setHyperlaneDensity(double density) {
        if (density < 0.5 || density > 2.75) {
            throw new IllegalArgumentException("Hyperlane density must be between 0.5 and 2.75: " + density);
        }
        this.hyperlaneDensity = density;
    }

    // ---- Sector operations ----

    /**
     * Gets a sector by its 2D grid coordinates.
     *
     * @param row the row (0-9)
     * @param col the column (0-9)
     * @return the sector at the given position
     * @throws IndexOutOfBoundsException if coordinates are out of range
     */
    public Sector getSector(int row, int col) {
        return grid[row][col];
    }

    /**
     * Gets a sector by its ID (row * 10 + col).
     *
     * @param sectorId the sector identifier (0-99)
     * @return the sector, or null if not found
     */
    public Sector getSector(int sectorId) {
        if (sectorId < 0 || sectorId >= TOTAL_SECTORS) {
            return null;
        }
        int row = sectorId / GRID_SIZE;
        int col = sectorId % GRID_SIZE;
        return grid[row][col];
    }

    /**
     * @return all sectors as a flat list (unmodifiable)
     */
    public List<Sector> getAllSectors() {
        List<Sector> sectors = new ArrayList<>(TOTAL_SECTORS);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                sectors.add(grid[row][col]);
            }
        }
        return Collections.unmodifiableList(sectors);
    }

    /**
     * @return the number of sectors
     */
    public int getSectorCount() {
        return TOTAL_SECTORS;
    }


    // ---- Solar system operations ----

    /**
     * Adds a solar system to the specified subsector at the given 3D position.
     *
     * @param system the solar system to add
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @param subRow the subsector row (0-9)
     * @param subCol the subsector column (0-9)
     * @param x the x coordinate within the subsector (0-99)
     * @param y the y coordinate within the subsector (0-99)
     * @param z the z coordinate within the subsector (0-99)
     * @return the galactic entry
     */
    public GalacticEntry addSystem(SolarSystem system, int sectorRow, int sectorCol,
                                   int subRow, int subCol, int x, int y, int z) {
        if (system == null) {
            throw new IllegalArgumentException("System must not be null");
        }
        Sector sector = getSector(sectorRow, sectorCol);
        if (sector == null) {
            throw new IllegalArgumentException("Invalid sector position: (" + sectorRow + ", " + sectorCol + ")");
        }
        sector.addSystem(system, subRow, subCol, x, y, z);
        allSystems.put(system.getId(), system);
        return new GalacticEntry(system, x, y, z, sectorRow, sectorCol, subRow, subCol);
    }

    /**
     * Adds a solar system to the specified sector (legacy method, assigns to subsector 0,0).
     *
     * @param system the solar system to add
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @param x the x coordinate within the subsector (0-99)
     * @param y the y coordinate within the subsector (0-99)
     * @param z the z coordinate within the subsector (0-99)
     * @return the galactic entry
     */
    public GalacticEntry addSystem(SolarSystem system, int sectorRow, int sectorCol, int x, int y, int z) {
        return addSystem(system, sectorRow, sectorCol, 0, 0, x, y, z);
    }

    /**
     * Retrieves a solar system by its ID.
     *
     * @param systemId the solar system's unique identifier
     * @return the solar system, or null if not present
     */
    public SolarSystem getSystem(int systemId) {
        return allSystems.get(systemId);
    }


    /**
     * @return all solar systems managed (unmodifiable map)
     */
    public Map<Integer, SolarSystem> getAllSystems() {
        return Collections.unmodifiableMap(allSystems);
    }

    /**
     * @return the mutable map of all solar systems (for internal use during generation)
     */
    public Map<Integer, SolarSystem> getAllSystemsMap() {
        return allSystems;
    }


    // ---- Hyperlane operations ----

    /**
     * Adds a hyperlane connection between two solar systems.
     *
     * @param fromSystemId the source system ID
     * @param toSystemId the destination system ID
     * @param distance the travel distance
     */
    public void addHyperlane(int fromSystemId, int toSystemId, int distance) {
        Hyperlane hyperlane = new Hyperlane(fromSystemId, toSystemId, distance);
        hyperlanes.add(hyperlane);
    }

    /**
     * @return all hyperlanes (unmodifiable list)
     */
    public List<Hyperlane> getAllHyperlanes() {
        return Collections.unmodifiableList(hyperlanes);
    }

    /**
     * @return the total number of hyperlanes
     */
    public int getHyperlaneCount() {
        return hyperlanes.size();
    }

    // ---- Aggregated statistics ----

    /**
     * Returns the total number of celestial bodies across all solar systems.
     *
     * @return total body count
     */
    public int getTotalBodyCount() {
        return allSystems.values().stream()
                .mapToInt(SolarSystem::getTotalBodyCount)
                .sum();
    }

    /**
     * Returns the total number of stars across all solar systems.
     *
     * @return total star count
     */
    public int getTotalStarCount() {
        return allSystems.values().stream()
                .mapToInt(SolarSystem::getTotalStarCount)
                .sum();
    }

    /**
     * Returns the total number of planets across all solar systems.
     *
     * @return total planet count
     */
    public int getTotalPlanetCount() {
        return allSystems.values().stream()
                .mapToInt(SolarSystem::getTotalPlanetCount)
                .sum();
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Galaxy that = (Galaxy) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // ---- Solar system lookup ----

    public SolarSystem getSolarSystem(int systemId) {
        SolarSystem system = solarSystems.get(systemId);
        if (system != null) return system;
        return allSystems.get(systemId);
    }

    /**
     * Registers a solar system in the galaxy's solar system map.
     * Used when setting up home systems.
     *
     * @param systemId the system ID
     * @param system the solar system
     */
    public void registerSolarSystem(int systemId, SolarSystem system) {
        solarSystems.put(systemId, system);
    }

    @Override
    public String toString() {
        return String.format("Galaxy{id=%d, name=%s, grid=%dx%d, systems=%d, hyperlanes=%d}",
                id, name, GRID_SIZE, GRID_SIZE, allSystems.size(), hyperlanes.size());
    }
}
