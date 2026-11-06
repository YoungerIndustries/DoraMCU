package com.yisy.modules.doramcu.hex;

import java.util.Collection;
import java.util.List;

/**
 * A fixed-size 10x10 hexagonal sector built on top of {@link HexMap}.
 * <p>
 * A sector represents a rectangular region of the hex grid, typically used
 * to divide a larger world into manageable tiles. Each sector is 10 hexes
 * wide and 10 hexes tall, containing 100 hex cells.
 * <p>
 * Coordinates within a sector range from (0,0) to (9,9).
 */
public class Sector {

    public static final int DEFAULT_WIDTH = 10;
    public static final int DEFAULT_HEIGHT = 10;

    private final int id;
    private final int width;
    private final int height;
    private final HexMap map;

    /**
     * Creates a new sector with the given ID and default 10x10 size.
     *
     * @param id the unique identifier for this sector
     */
    public Sector(int id) {
        this(id, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new sector with the given ID and custom dimensions.
     *
     * @param id the unique identifier for this sector
     * @param width the width in hexes
     * @param height the height in hexes
     */
    public Sector(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.map = new HexMap();
        generate();
    }

    /**
     * Generates the rectangular grid of hexes for this sector.
     */
    private void generate() {
        map.generateRect(0, width - 1, 0, height - 1);
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this sector
     */
    public int getId() {
        return id;
    }

    /**
     * @return the width of the sector in hexes
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the sector in hexes
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the total number of hexes in the sector
     */
    public int size() {
        return map.size();
    }

    // ---- Hex operations ----

    /**
     * Retrieves a hex by its local coordinates within the sector.
     *
     * @param q the q coordinate (0 to width-1)
     * @param r the r coordinate (0 to height-1)
     * @return the hex at the given position, or null if out of bounds
     */
    public Hex get(int q, int r) {
        return map.get(q, r);
    }

    /**
     * Checks if the given coordinates are within this sector.
     *
     * @param q the q coordinate
     * @param r the r coordinate
     * @return true if the coordinates are within the sector bounds
     */
    public boolean contains(int q, int r) {
        return map.contains(q, r);
    }

    /**
     * @return all hexes in the sector
     */
    public Collection<Hex> getAll() {
        return map.getAll();
    }

    // ---- Query operations ----

    /**
     * Returns all hexes in the given state within this sector.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex> findByState(HexState state) {
        return map.findByState(state);
    }

    /**
     * Returns the neighbors of the given hex that are also within this sector.
     *
     * @param hex the center hex
     * @return list of neighboring hexes present in the sector
     */
    public List<Hex> getNeighbors(Hex hex) {
        return map.getNeighbors(hex);
    }

    /**
     * Returns the number of occupied hexes in this sector.
     *
     * @return count of occupied hexes
     */
    public int getOccupiedCount() {
        return findByState(HexState.OCCUPIED).size();
    }

    /**
     * Returns the number of empty hexes in this sector.
     *
     * @return count of empty hexes
     */
    public int getEmptyCount() {
        return findByState(HexState.EMPTY).size();
    }

    /**
     * Returns the occupancy ratio of the sector.
     *
     * @return a value between 0.0 (empty) and 1.0 (full)
     */
    public double getOccupancyRatio() {
        return (double) getOccupiedCount() / size();
    }

    // ---- Utility ----

    /**
     * Converts global world coordinates to local sector coordinates.
     *
     * @param globalQ the global q coordinate
     * @param globalR the global r coordinate
     * @return the local q coordinate within this sector
     */
    public static int toLocalQ(int globalQ) {
        return globalQ % DEFAULT_WIDTH;
    }

    /**
     * Converts global world coordinates to local sector coordinates.
     *
     * @param globalR the global r coordinate
     * @return the local r coordinate within this sector
     */
    public static int toLocalR(int globalR) {
        return globalR % DEFAULT_HEIGHT;
    }

    /**
     * Computes the sector ID from global world coordinates.
     *
     * @param globalQ the global q coordinate
     * @param globalR the global r coordinate
     * @return the sector ID containing the given global coordinates
     */
    public static int sectorId(int globalQ, int globalR) {
        int sectorQ = (int) Math.floor((double) globalQ / DEFAULT_WIDTH);
        int sectorR = (int) Math.floor((double) globalR / DEFAULT_HEIGHT);
        // Encode 2D sector grid into a single ID
        return sectorQ + sectorR * 100;
    }

    @Override
    public String toString() {
        return "Sector{id=" + id + ", size=" + width + "x" + height + ", hexes=" + map.size() + "}";
    }
}
