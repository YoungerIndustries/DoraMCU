package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A subsector within a galaxy sector.
 * <p>
 * Each subsector is a 3D hexmap of 10x10x10 dimensions containing 1-3 solar systems.
 * Subsectors are arranged in a 10x10 hex grid within each sector.
 * <p>
 * Based on design patterns from Stellaris (subsectors) and
 * Galactic Civilizations IV (galactic core, strategic locations).
 */
public class SubSector {
    private final int id;
    private final int subRow;
    private final int subCol;
    private String name;
    private final Map<Integer, SolarSystem> systems;
    private final Map<String, GalacticEntry> positionIndex;
    private boolean generated;
    private GalaxyGenerationProgress progressListener;

    /**
     * Creates a new subsector at the given grid position.
     * The subsector starts ungenerated (empty) and will populate
     * with solar systems when {@link #generateSystems(Random, StarNameDatabase, PlanetNameDatabase)}
     * is called, typically when a fleet enters the subsector.
     *
     * @param id the unique identifier for this subsector
     * @param subRow the row position in the 10x10 subsector grid (0-9)
     * @param subCol the column position in the 10x10 subsector grid (0-9)
     * @param name the display name of the subsector
     */
    public SubSector(int id, int subRow, int subCol, String name) {
        if (subRow < 0 || subRow >= Galaxy.SUBSECTOR_GRID_SIZE) {
            throw new IllegalArgumentException("SubRow must be between 0 and " + (Galaxy.SUBSECTOR_GRID_SIZE - 1) + ": " + subRow);
        }
        if (subCol < 0 || subCol >= Galaxy.SUBSECTOR_GRID_SIZE) {
            throw new IllegalArgumentException("SubCol must be between 0 and " + (Galaxy.SUBSECTOR_GRID_SIZE - 1) + ": " + subCol);
        }
        this.id = id;
        this.subRow = subRow;
        this.subCol = subCol;
        this.name = name;
        this.systems = new HashMap<>();
        this.positionIndex = new HashMap<>();
        this.generated = false;
    }

    /**
     * @return the unique identifier for this subsector
     */
    public int getId() {
        return id;
    }

    /**
     * @return the row position in the 10x10 subsector grid (0-9)
     */
    public int getSubRow() {
        return subRow;
    }

    /**
     * @return the column position in the 10x10 subsector grid (0-9)
     */
    public int getSubCol() {
        return subCol;
    }

    /**
     * @return the display name of the subsector
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this subsector.
     * Allows renaming subsectors at runtime so the new name persists to the database.
     *
     * @param name the new display name
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("SubSector name must not be null or blank");
        }
        this.name = name;
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
     * Adds a solar system at the given 3D position within this subsector.
     * Position coordinates must be in range [0, Galaxy.SUBSECTOR_DIMENSION).
     *
     * @param system the solar system to add
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return the previous entry at that position, or null
     */
    public GalacticEntry addSystem(SolarSystem system, int x, int y, int z) {
        if (system == null) {
            throw new IllegalArgumentException("System must not be null");
        }
        if (x < 0 || x >= Galaxy.SUBSECTOR_DIMENSION || y < 0 || y >= Galaxy.SUBSECTOR_DIMENSION || z < 0 || z >= Galaxy.SUBSECTOR_DIMENSION) {
            throw new IllegalArgumentException("Position must be within subsector bounds [0, " + Galaxy.SUBSECTOR_DIMENSION + "): (" + x + ", " + y + ", " + z + ")");
        }
        this.generated = true;
        String key = x + "," + y + "," + z;
        GalacticEntry removed = positionIndex.remove(key);
        systems.put(system.getId(), system);
        GalacticEntry entry = new GalacticEntry(system, x, y, z, -1, -1, subRow, subCol);
        positionIndex.put(key, entry);
        return removed;
    }

    /**
     * Removes a solar system from this subsector.
     *
     * @param systemId the solar system ID to remove
     * @return the removed solar system, or null if not present
     */
    public SolarSystem removeSystem(int systemId) {
        SolarSystem system = systems.remove(systemId);
        if (system != null) {
            positionIndex.values().removeIf(entry -> entry.getSystem().getId() == systemId);
        }
        return system;
    }

    /**
     * Retrieves a solar system by its ID.
     *
     * @param systemId the solar system's unique identifier
     * @return the solar system, or null if not present
     */
    public SolarSystem getSystem(int systemId) {
        return systems.get(systemId);
    }

    /**
     * Retrieves the solar system at the given 3D position.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the galactic entry, or null if no system is at that position
     */
    public GalacticEntry getAt(int x, int y, int z) {
        String key = x + "," + y + "," + z;
        return positionIndex.get(key);
    }

    /**
     * @return the number of solar systems in this subsector
     */
    public int size() {
        return systems.size();
    }

    /**
     * @return all solar systems in this subsector (unmodifiable map)
     */
    public Map<Integer, SolarSystem> getAllSystems() {
        return Collections.unmodifiableMap(systems);
    }

    /**
     * @return all solar systems with their positions (unmodifiable collection)
     */
    public Collection<GalacticEntry> getAllEntries() {
        return Collections.unmodifiableCollection(positionIndex.values());
    }

    /**
     * Returns all solar systems within the given distance from a position.
     *
     * @param x the reference x coordinate
     * @param y the reference y coordinate
     * @param z the reference z coordinate
     * @param maxDistance the maximum distance
     * @return list of galactic entries within range
     */
    public List<GalacticEntry> findByDistance(int x, int y, int z, int maxDistance) {
        return positionIndex.values().stream()
                .filter(entry -> {
                    int dx = entry.getX() - x;
                    int dy = entry.getY() - y;
                    int dz = entry.getZ() - z;
                    return (int) Math.sqrt(dx * dx + dy * dy + dz * dz) <= maxDistance;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the nearest solar system to the given position.
     *
     * @param x the reference x coordinate
     * @param y the reference y coordinate
     * @param z the reference z coordinate
     * @return the nearest galactic entry, or null if no systems exist
     */
    public GalacticEntry findNearest(int x, int y, int z) {
        return positionIndex.values().stream()
                .min(Comparator.comparingInt(entry -> {
                    int dx = entry.getX() - x;
                    int dy = entry.getY() - y;
                    int dz = entry.getZ() - z;
                    return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
                }))
                .orElse(null);
    }

    /**
     * @return true if this subsector has been generated with solar systems
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * Generates solar systems in this subsector if not already generated.
     * Creates 1-3 solar systems with random positions and celestial bodies.
     * This method is typically called when a fleet enters the subsector.
     *
     * @param random the random number generator for reproducibility
     * @param starNames the star name database for naming stars
     * @param planetNames the planet name database for naming planets
     * @return the list of generated solar systems
     */
    public List<SolarSystem> generateSystems(Random random, StarNameDatabase starNames, PlanetNameDatabase planetNames) {
        if (generated) {
            return new ArrayList<>(systems.values());
        }
        generated = true;
        int systemCount = Galaxy.MIN_SYSTEMS_PER_SUBSECTOR + random.nextInt(Galaxy.MAX_SYSTEMS_PER_SUBSECTOR - Galaxy.MIN_SYSTEMS_PER_SUBSECTOR + 1);
        List<SolarSystem> generatedSystems = new ArrayList<>(systemCount);
        int entityIdCounter = id * 100000 + 1;
        int starIdCounter = id * 10000 + 1;
        int planetIdCounter = id * 10000 + 1000;
        for (int i = 0; i < systemCount; i++) {
            SolarSystem system = new SolarSystem(id * 1000 + i + 1);
            // Generate star at origin
            Star star = new Star(starIdCounter++);
            if (starNames != null) {
                starNames.assignName(star);
            }
            SolarSystemEntity starEntity = new SolarSystemEntity(entityIdCounter++);
            starEntity.addStar(star, new Hex3D(0, 0, 0));
            system.addSystem(starEntity, new Hex3D(0, 0, 0));
            // Generate planets
            int planetCount = 1 + random.nextInt(5);
            for (int p = 0; p < planetCount; p++) {
                Planet planet = new Planet(planetIdCounter++, PlanetSize.values()[random.nextInt(PlanetSize.values().length)]);
                if (planetNames != null) {
                    planetNames.assignName(planet);
                }
                // Planet position within the solar system uses Hex3D (x+y+z=0)
                Hex3D planetPos = generateRandomHex3D(random);
                SolarSystemEntity planetEntity = new SolarSystemEntity(entityIdCounter++);
                planetEntity.addPlanet(planet, planetPos);
                system.addSystem(planetEntity, planetPos);
            }
            // System position within subsector uses plain int coords [0, Galaxy.SUBSECTOR_DIMENSION)
            int x = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
            int y = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
            int z = random.nextInt(Galaxy.SUBSECTOR_DIMENSION);
            addSystem(system, x, y, z);
            generatedSystems.add(system);

            // Notify progress listener that this system was just created
            if (progressListener != null) {
                progressListener.onSystemCreated(system);
            }

        }
        return generatedSystems;
    }

    /**
     * Generates a random Hex3D coordinate where x + y + z = 0.
     * Used for positions within a SolarSystemEntity.
     */
    private Hex3D generateRandomHex3D(Random random) {
        int x = random.nextInt(10) - 5;
        int y = random.nextInt(10) - 5;
        int z = -(x + y);
        return new Hex3D(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("SubSector{id=%d, name='%s', subRow=%d, subCol=%d, systems=%d, generated=%b}",
                id, name, subRow, subCol, systems.size(), generated);
    }
}
