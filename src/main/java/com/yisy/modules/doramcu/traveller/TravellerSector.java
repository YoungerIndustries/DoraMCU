package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Traveller sector containing multiple star systems.
 * <p>
 * In Classic Traveller, a sector is a region of space typically 10x10 parsecs
 * wide, containing dozens of star systems. Each system has sector map coordinates
 * that determine its position and distance to neighboring systems.
 * </p>
 * <p>
 * The sector manages:
 * </p>
 * <ul>
 *   <li>Star systems ({@link TravellerWorld} instances)</li>
 *   <li>Hyperlane connections ({@link Hyperlane} instances)</li>
 *   <li>Sector-wide statistics and queries</li>
 * </ul>
 * <p>
 * Example: The Spinward Marches sector contains sub-sectors like Kharshive,
 * Mesna, Tukkay, and others, each with their own star systems and trade routes.
 * </p>
 */
public class TravellerSector {

    /**
     * Default sector map size (10x10 grid).
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * The sector's unique identifier.
     */
    private final int id;

    /**
     * The sector's name (e.g., "Spinward Marches", "Core").
     */
    private final String name;

    /**
     * The sector map width in parsecs.
     */
    private final int width;

    /**
     * The sector map height in parsecs.
     */
    private final int height;

    /**
     * All star systems in this sector, keyed by world ID.
     */
    private final Map<Integer, TravellerWorld> worlds;

    /**
     * All hyperlane connections in this sector.
     */
    private final List<Hyperlane> hyperlanes;

    /**
     * The capital world of this sector, if any.
     */
    private TravellerWorld capitalWorld;

    /**
     * Creates a new empty sector with the given name and default size.
     *
     * @param id the unique sector identifier
     * @param name the name of the sector
     */
    public TravellerSector(int id, String name) {
        this(id, name, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Creates a new empty sector with the given name and custom size.
     *
     * @param id the unique sector identifier
     * @param name the name of the sector
     * @param width the sector map width in parsecs
     * @param height the sector map height in parsecs
     */
    public TravellerSector(int id, String name, int width, int height) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Sector name must not be null");
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        this.width = width;
        this.height = height;
        this.worlds = new HashMap<>();
        this.hyperlanes = new ArrayList<>();
        this.capitalWorld = null;
    }

    // ---- World Management ----

    /**
     * Adds a star system to this sector.
     *
     * @param world the star system to add
     * @return the previous world with this ID, or null if none
     */
    public TravellerWorld addWorld(TravellerWorld world) {
        Objects.requireNonNull(world, "World must not be null");
        TravellerWorld previous = worlds.put(world.getId(), world);
        if (world.isCapital() && capitalWorld == null) {
            this.capitalWorld = world;
        }
        return previous;
    }

    /**
     * Removes a star system from this sector.
     *
     * @param worldId the ID of the world to remove
     * @return the removed world, or null if not found
     */
    public TravellerWorld removeWorld(int worldId) {
        TravellerWorld removed = worlds.remove(worldId);
        if (removed != null) {
            // Remove associated hyperlanes
            hyperlanes.removeIf(hl -> hl.connects(removed));
            if (capitalWorld != null && capitalWorld.getId() == worldId) {
                this.capitalWorld = null;
            }
        }
        return removed;
    }

    /**
     * Gets a star system by its ID.
     *
     * @param worldId the world's unique identifier
     * @return the world, or null if not found
     */
    public TravellerWorld getWorld(int worldId) {
        return worlds.get(worldId);
    }

    /**
     * Gets a star system by its sector map coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the world at those coordinates, or null if none
     */
    public TravellerWorld getWorldAt(int x, int y) {
        return worlds.values().stream()
                .filter(w -> w.getSectorX() == x && w.getSectorY() == y)
                .findFirst()
                .orElse(null);
    }

    /**
     * @return all star systems in this sector (immutable view)
     */
    public List<TravellerWorld> getAllWorlds() {
        return new ArrayList<>(worlds.values());
    }

    /**
     * @return the number of star systems in this sector
     */
    public int getWorldCount() {
        return worlds.size();
    }

    // ---- Hyperlane Management ----

    /**
     * Adds a hyperlane connection between two worlds.
     * <p>
     * Both worlds must be in this sector. The hyperlane is added to both
     * worlds' connection lists.
     * </p>
     *
     * @param from the source world
     * @param to the destination world
     * @return the created hyperlane
     */
    public Hyperlane addHyperlane(TravellerWorld from, TravellerWorld to) {
        Objects.requireNonNull(from, "Source world must not be null");
        Objects.requireNonNull(to, "Destination world must not be null");
        if (!worlds.containsKey(from.getId())) {
            throw new IllegalArgumentException("Source world not in sector: " + from.getName());
        }
        if (!worlds.containsKey(to.getId())) {
            throw new IllegalArgumentException("Destination world not in sector: " + to.getName());
        }

        Hyperlane hyperlane = new Hyperlane(from, to);
        hyperlanes.add(hyperlane);
        from.addHyperlane(hyperlane);
        to.addHyperlane(hyperlane);
        return hyperlane;
    }

    /**
     * Gets all hyperlanes connected to a specific world.
     *
     * @param world the world to query
     * @return list of hyperlanes connected to the world
     */
    public List<Hyperlane> getHyperlanesFor(TravellerWorld world) {
        Objects.requireNonNull(world, "World must not be null");
        return hyperlanes.stream()
                .filter(hl -> hl.connects(world))
                .collect(Collectors.toList());
    }

    /**
     * @return all hyperlanes in this sector (immutable view)
     */
    public List<Hyperlane> getAllHyperlanes() {
        return Collections.unmodifiableList(hyperlanes);
    }

    /**
     * @return the number of hyperlanes in this sector
     */
    public int getHyperlaneCount() {
        return hyperlanes.size();
    }

    // ---- Capital World ----

    /**
     * Sets the capital world of this sector.
     *
     * @param world the capital world
     */
    public void setCapitalWorld(TravellerWorld world) {
        if (world != null && !worlds.containsKey(world.getId())) {
            throw new IllegalArgumentException("World not in sector: " + world.getName());
        }
        if (this.capitalWorld != null) {
            this.capitalWorld.setCapital(false);
        }
        this.capitalWorld = world;
        if (world != null) {
            world.setCapital(true);
        }
    }

    /**
     * @return the capital world, or null if none
     */
    public TravellerWorld getCapitalWorld() {
        return capitalWorld;
    }

    // ---- Queries ----

    /**
     * Finds all habitable worlds in this sector.
     *
     * @return list of habitable worlds
     */
    public List<TravellerWorld> getHabitableWorlds() {
        return worlds.values().stream()
                .filter(TravellerWorld::isHabitable)
                .collect(Collectors.toList());
    }

    /**
     * Finds all worlds with a specific trade code.
     *
     * @param code the trade code to search for
     * @return list of worlds with the given trade code
     */
    public List<TravellerWorld> getWorldsWithTradeCode(TradeCode code) {
        Objects.requireNonNull(code, "Trade code must not be null");
        return worlds.values().stream()
                .filter(w -> w.hasTradeCode(code))
                .collect(Collectors.toList());
    }

    /**
     * Finds all worlds within jump range of a given world.
     *
     * @param world the starting world
     * @param maxJump the maximum jump range
     * @return list of reachable worlds
     */
    public List<TravellerWorld> getWorldsWithinJumpRange(TravellerWorld world, int maxJump) {
        Objects.requireNonNull(world, "World must not be null");
        return worlds.values().stream()
                .filter(w -> !w.equals(world))
                .filter(w -> world.isWithinJumpRange(w, maxJump))
                .collect(Collectors.toList());
    }

    /**
     * Finds all Imperium-aligned worlds in this sector.
     *
     * @return list of Imperium-aligned worlds
     */
    public List<TravellerWorld> getImperiumWorlds() {
        return worlds.values().stream()
                .filter(TravellerWorld::isImperiumAligned)
                .collect(Collectors.toList());
    }

    /**
     * Finds the nearest world to the given coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the nearest world, or null if no worlds in sector
     */
    public TravellerWorld findNearestWorld(int x, int y) {
        return worlds.values().stream()
                .min((w1, w2) -> {
                    int d1 = (w1.getSectorX() - x) * (w1.getSectorX() - x) + (w1.getSectorY() - y) * (w1.getSectorY() - y);
                    int d2 = (w2.getSectorX() - x) * (w2.getSectorX() - x) + (w2.getSectorY() - y) * (w2.getSectorY() - y);
                    return Integer.compare(d1, d2);
                })
                .orElse(null);
    }

    // ---- Statistics ----

    /**
     * Returns the total population of all worlds in the sector (in millions).
     *
     * @return total population in millions
     */
    public long getTotalPopulation() {
        return worlds.values().stream()
                .mapToLong(TravellerWorld::getPopulationMillions)
                .sum();
    }

    /**
     * Returns the average port wealth across all worlds.
     *
     * @return average port wealth, or 0 if no worlds
     */
    public double getAveragePortWealth() {
        if (worlds.isEmpty()) {
            return 0;
        }
        return worlds.values().stream()
                .mapToInt(TravellerWorld::getPortWealth)
                .average()
                .orElse(0);
    }

    /**
     * Returns the number of habitable worlds as a percentage of total worlds.
     *
     * @return habitable percentage (0-100), or 0 if no worlds
     */
    public double getHabitablePercentage() {
        if (worlds.isEmpty()) {
            return 0;
        }
        long habitable = worlds.values().stream()
                .filter(TravellerWorld::isHabitable)
                .count();
        return (double) habitable / worlds.size() * 100;
    }

    // ---- Accessors ----

    /**
     * @return the unique sector identifier
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name of the sector
     */
    public String getName() {
        return name;
    }

    /**
     * @return the sector map width in parsecs
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the sector map height in parsecs
     */
    public int getHeight() {
        return height;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return name + " Sector (" + worlds.size() + " worlds, " + hyperlanes.size() + " hyperlanes)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravellerSector that = (TravellerSector) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
