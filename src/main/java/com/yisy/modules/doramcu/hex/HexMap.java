package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a collection of {@link Hex} cells forming a hexagonal map.
 * <p>
 * The map stores hexes in a hash map keyed by their coordinates, providing
 * O(1) lookup. It also offers utilities for generating rings, ranges,
 * and querying hexes by state.
 */
public class HexMap {

    private final Map<Hex, Hex> hexes;

    public HexMap() {
        this.hexes = new HashMap<>();
    }

    // ---- CRUD operations ----

    /**
     * Adds a hex cell to the map.
     *
     * @param hex the hex to add
     * @return the previous hex at that position, or null
     */
    public Hex put(Hex hex) {
        return hexes.put(hex, hex);
    }

    /**
     * Retrieves a hex by its coordinates.
     *
     * @param q the q coordinate
     * @param r the r coordinate
     * @return the hex at the given position, or null if not present
     */
    public Hex get(int q, int r) {
        return hexes.get(new Hex(q, r));
    }

    /**
     * Retrieves a hex by another hex instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex get(Hex hex) {
        return hexes.get(hex);
    }

    /**
     * Removes a hex from the map.
     *
     * @param hex the hex to remove
     * @return the removed hex, or null if not present
     */
    public Hex remove(Hex hex) {
        return hexes.remove(hex);
    }

    /**
     * @return true if the map contains a hex at the given coordinates
     */
    public boolean contains(int q, int r) {
        return hexes.containsKey(new Hex(q, r));
    }

    /**
     * @return all hexes in the map
     */
    public Collection<Hex> getAll() {
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
    public List<Hex> findByState(HexState state) {
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
    public List<Hex> getNeighbors(Hex hex) {
        List<Hex> result = new ArrayList<>();
        for (HexDirection dir : HexDirection.values()) {
            Hex neighbor = hex.neighbor(dir);
            Hex stored = hexes.get(neighbor);
            if (stored != null) {
                result.add(stored);
            }
        }
        return result;
    }

    // ---- Generation utilities ----

    /**
     * Generates a ring of hexes around the center at the given radius and adds them to the map.
     *
     * @param center  the center hex
     * @param radius  the ring radius
     * @return the list of generated hexes
     */
    public List<Hex> generateRing(Hex center, int radius) {
        if (radius == 0) {
            return Collections.singletonList(center);
        }

        List<Hex> ring = new ArrayList<>();
        Hex start = new Hex(
                center.getQ() + radius * HexDirection.SOUTH_WEST.getDq(),
                center.getR() + radius * HexDirection.SOUTH_WEST.getDr()
        );

        Hex current = start;
        for (int dir = 0; dir < 6; dir++) {
            HexDirection direction = HexDirection.values()[dir];
            for (int step = 0; step < radius; step++) {
                hexes.put(current, current);
                ring.add(current);
                current = new Hex(
                        current.getQ() + direction.getDq(),
                        current.getR() + direction.getDr()
                );
            }
        }
        return ring;
    }

    /**
     * Generates all hexes within the given radius of the center (inclusive) and adds them to the map.
     *
     * @param center  the center hex
     * @param radius  the range radius
     * @return the list of all generated hexes
     */
    public List<Hex> generateRange(Hex center, int radius) {
        List<Hex> result = new ArrayList<>();
        hexes.put(center, center);
        result.add(center);
        for (int r = 1; r <= radius; r++) {
            result.addAll(generateRing(center, r));
        }
        return result;
    }

    /**
     * Generates a rectangular grid of hexes.
     *
     * @param qMin minimum q coordinate
     * @param qMax maximum q coordinate
     * @param rMin minimum r coordinate
     * @param rMax maximum r coordinate
     * @return the list of generated hexes
     */
    public List<Hex> generateRect(int qMin, int qMax, int rMin, int rMax) {
        List<Hex> result = new ArrayList<>();
        for (int q = qMin; q <= qMax; q++) {
            for (int r = rMin; r <= rMax; r++) {
                Hex hex = new Hex(q, r);
                hexes.put(hex, hex);
                result.add(hex);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "HexMap{size=" + hexes.size() + "}";
    }
}
