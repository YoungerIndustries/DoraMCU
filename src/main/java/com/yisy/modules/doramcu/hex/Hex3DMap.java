package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a collection of {@link Hex3D} cells forming a 3D hexagonal map.
 * <p>
 * The map stores hexes in a hash map keyed by their coordinates, providing
 * O(1) lookup. It also offers utilities for generating spheres, cubes,
 * and querying hexes by state.
 */
public class Hex3DMap {

    private final Map<Hex3D, Hex3D> hexes;

    public Hex3DMap() {
        this.hexes = new HashMap<>();
    }

    // ---- CRUD operations ----

    /**
     * Adds a hex cell to the map.
     *
     * @param hex the hex to add
     * @return the previous hex at that position, or null
     */
    public Hex3D put(Hex3D hex) {
        return hexes.put(hex, hex);
    }

    /**
     * Retrieves a hex by its coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex at the given position, or null if not present
     */
    public Hex3D get(int x, int y) {
        return hexes.get(new Hex3D(x, y));
    }

    /**
     * Retrieves a hex by another hex instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        return hexes.get(hex);
    }

    /**
     * Removes a hex from the map.
     *
     * @param hex the hex to remove
     * @return the removed hex, or null if not present
     */
    public Hex3D remove(Hex3D hex) {
        return hexes.remove(hex);
    }

    /**
     * @return true if the map contains a hex at the given coordinates
     */
    public boolean contains(int x, int y) {
        return hexes.containsKey(new Hex3D(x, y));
    }

    /**
     * @return all hexes in the map
     */
    public Collection<Hex3D> getAll() {
        return Collections.unmodifiableCollection(hexes.values());
    }

    /**
     * @return the number of hexes in the map
     */
    public int size() {
        return hexes.size();
    }

    /**
     * Clears all hexes from the map.
     */
    public void clear() {
        hexes.clear();
    }

    // ---- Query operations ----

    /**
     * Returns all hexes in the given state.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex3D> findByState(HexState state) {
        return hexes.values().stream()
                .filter(h -> h.getState() == state)
                .collect(Collectors.toList());
    }

    /**
     * Returns the neighbors of the given hex that are also in this map.
     *
     * @param hex the center hex
     * @return list of neighboring hexes present in the map
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        List<Hex3D> result = new ArrayList<>();
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            Hex3D neighbor = hex.neighbor(dir);
            Hex3D stored = hexes.get(neighbor);
            if (stored != null) {
                result.add(stored);
            }
        }
        return result;
    }

    // ---- Generation utilities ----

    /**
     * Generates a sphere shell of hexes around the center at the given radius and adds them to the map.
     * <p>
     * A sphere in cubic coordinates includes all hexes at exactly the given distance from the center.
     *
     * @param center  the center hex
     * @param radius  the sphere radius
     * @return the list of generated hexes
     */
    public List<Hex3D> generateSphere(Hex3D center, int radius) {
        if (radius == 0) {
            return Collections.singletonList(center);
        }

        List<Hex3D> sphere = new ArrayList<>();
        // Iterate through all hexes in a cube range and filter by distance
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                int z = -x - y;
                Hex3D hex = new Hex3D(x, y, z);
                if (center.distance(hex) == radius) {
                    hexes.put(hex, hex);
                    sphere.add(hex);
                }
            }
        }
        return sphere;
    }

    /**
     * Generates all hexes within the given radius of the center (inclusive) and adds them to the map.
     * <p>
     * This creates a solid sphere (ball) of hexes.
     *
     * @param center  the center hex
     * @param radius  the range radius
     * @return the list of all generated hexes
     */
    public List<Hex3D> generateRange(Hex3D center, int radius) {
        List<Hex3D> result = new ArrayList<>();
        hexes.put(center, center);
        result.add(center);
        for (int r = 1; r <= radius; r++) {
            result.addAll(generateSphere(center, r));
        }
        return result;
    }

    /**
     * Generates a cubic block of hexes.
     *
     * @param xMin minimum x coordinate
     * @param xMax maximum x coordinate
     * @param yMin minimum y coordinate
     * @param yMax maximum y coordinate
     * @return the list of generated hexes
     */
    public List<Hex3D> generateCube(int xMin, int xMax, int yMin, int yMax) {
        List<Hex3D> result = new ArrayList<>();
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                Hex3D hex = new Hex3D(x, y);
                hexes.put(hex, hex);
                result.add(hex);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Hex3DMap{size=" + hexes.size() + "}";
    }
}
