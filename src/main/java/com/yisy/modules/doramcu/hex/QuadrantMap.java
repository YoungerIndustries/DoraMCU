package com.yisy.modules.doramcu.hex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A 10x10 grid of sectors, forming a quadrant map.
 * <p>
 * A QuadrantMap represents a large-scale region composed of 100 sectors
 * (10 wide by 10 tall), each sector containing 100 hex cells. This gives
 * a total of 10,000 hex cells per quadrant.
 * <p>
 * Sector coordinates within a quadrant range from (0,0) to (9,9).
 * Global hex coordinates are derived by combining sector coordinates with
 * local hex coordinates within each sector.
 */
public class QuadrantMap {

    public static final int DEFAULT_WIDTH = 10;
    public static final int DEFAULT_HEIGHT = 10;

    private final int id;
    private final int width;
    private final int height;
    private final Map<String, Sector> sectors;

    /**
     * Creates a new quadrant map with the given ID and default 10x10 size.
     *
     * @param id the unique identifier for this quadrant
     */
    public QuadrantMap(int id) {
        this(id, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates a new quadrant map with the given ID and custom dimensions.
     *
     * @param id the unique identifier for this quadrant
     * @param width the width in sectors
     * @param height the height in sectors
     */
    public QuadrantMap(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.sectors = new HashMap<>();
        generate();
    }

    /**
     * Generates the grid of sectors for this quadrant.
     */
    private void generate() {
        for (int r = 0; r < height; r++) {
            for (int q = 0; q < width; q++) {
                int sectorId = sectorLocalId(q, r);
                sectors.put(sectorKey(q, r), new Sector(sectorId));
            }
        }
    }

    /**
     * Computes a local sector ID from sector grid coordinates.
     */
    private int sectorLocalId(int q, int r) {
        return q + r * width;
    }

    /**
     * Creates a map key for sector grid coordinates.
     */
    private static String sectorKey(int q, int r) {
        return q + "," + r;
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this quadrant
     */
    public int getId() {
        return id;
    }

    /**
     * @return the width of the quadrant in sectors
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the quadrant in sectors
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the total number of sectors in the quadrant
     */
    public int size() {
        return sectors.size();
    }

    /**
     * @return the total number of hex cells across all sectors
     */
    public int totalHexes() {
        return sectors.size() * Sector.DEFAULT_WIDTH * Sector.DEFAULT_HEIGHT;
    }

    // ---- Sector operations ----

    /**
     * Retrieves a sector by its grid coordinates within the quadrant.
     *
     * @param q the q coordinate (0 to width-1)
     * @param r the r coordinate (0 to height-1)
     * @return the sector at the given position, or null if out of bounds
     */
    public Sector get(int q, int r) {
        return sectors.get(sectorKey(q, r));
    }

    /**
     * Checks if the given sector coordinates are within this quadrant.
     *
     * @param q the q coordinate
     * @param r the r coordinate
     * @return true if the coordinates are within the quadrant bounds
     */
    public boolean contains(int q, int r) {
        return sectors.containsKey(sectorKey(q, r));
    }

    /**
     * @return all sectors in the quadrant as an unmodifiable collection
     */
    public Collection<Sector> getAll() {
        return Collections.unmodifiableCollection(sectors.values());
    }

    // ---- Hex operations (cross-sector) ----

    /**
     * Retrieves a hex by its global coordinates within the quadrant.
     * Global coordinates are converted to sector coordinates and local hex coordinates.
     *
     * @param globalQ the global q coordinate (0 to width * Sector.DEFAULT_WIDTH - 1)
     * @param globalR the global r coordinate (0 to height * Sector.DEFAULT_HEIGHT - 1)
     * @return the hex at the given global position, or null if out of bounds
     */
    public Hex getHex(int globalQ, int globalR) {
        int sectorQ = globalQ / Sector.DEFAULT_WIDTH;
        int sectorR = globalR / Sector.DEFAULT_HEIGHT;
        Sector sector = get(sectorQ, sectorR);
        if (sector == null) {
            return null;
        }
        int localQ = globalQ % Sector.DEFAULT_WIDTH;
        int localR = globalR % Sector.DEFAULT_HEIGHT;
        return sector.get(localQ, localR);
    }

    /**
     * Checks if the given global hex coordinates are within this quadrant.
     *
     * @param globalQ the global q coordinate
     * @param globalR the global r coordinate
     * @return true if the coordinates are within the quadrant bounds
     */
    public boolean containsHex(int globalQ, int globalR) {
        int sectorQ = globalQ / Sector.DEFAULT_WIDTH;
        int sectorR = globalR / Sector.DEFAULT_HEIGHT;
        return contains(sectorQ, sectorR);
    }

    // ---- Query operations ----

    /**
     * Returns all hexes in the given state across all sectors in this quadrant.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex> findByState(HexState state) {
        return sectors.values().stream()
                .flatMap(sector -> sector.findByState(state).stream())
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of occupied hexes across all sectors.
     *
     * @return count of occupied hexes
     */
    public int getOccupiedCount() {
        return sectors.values().stream()
                .mapToInt(Sector::getOccupiedCount)
                .sum();
    }

    /**
     * Returns the number of empty hexes across all sectors.
     *
     * @return count of empty hexes
     */
    public int getEmptyCount() {
        return sectors.values().stream()
                .mapToInt(Sector::getEmptyCount)
                .sum();
    }

    /**
     * Returns the occupancy ratio of the quadrant.
     *
     * @return a value between 0.0 (empty) and 1.0 (full)
     */
    public double getOccupancyRatio() {
        return (double) getOccupiedCount() / totalHexes();
    }

    /**
     * Returns the number of sectors containing at least one hex in the given state.
     *
     * @param state the state to check
     * @return count of sectors with matching hexes
     */
    public int getSectorCountByState(HexState state) {
        return (int) sectors.values().stream()
                .filter(sector -> !sector.findByState(state).isEmpty())
                .count();
    }

    /**
     * Returns the neighbors of the given hex that are within this quadrant.
     * Neighbors may span sector boundaries.
     *
     * @param hex the center hex
     * @return list of neighboring hexes present in the quadrant
     */
    public List<Hex> getNeighbors(Hex hex) {
        return hex.neighbors().stream()
                .filter(neighbor -> {
                    // Check if any sector contains this neighbor
                    for (Sector sector : sectors.values()) {
                        if (sector.getAll().contains(neighbor)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // ---- Utility ----

    /**
     * Converts global hex coordinates to sector grid coordinates.
     *
     * @param globalQ the global q coordinate
     * @param globalR the global r coordinate
     * @return the sector q coordinate
     */
    public static int toSectorQ(int globalQ) {
        return globalQ / Sector.DEFAULT_WIDTH;
    }

    /**
     * Converts global hex coordinates to sector grid coordinates.
     *
     * @param globalR the global r coordinate
     * @return the sector r coordinate
     */
    public static int toSectorR(int globalR) {
        return globalR / Sector.DEFAULT_HEIGHT;
    }

    /**
     * Converts global hex coordinates to local hex coordinates within a sector.
     *
     * @param globalQ the global q coordinate
     * @return the local q coordinate within the sector
     */
    public static int toLocalHexQ(int globalQ) {
        return globalQ % Sector.DEFAULT_WIDTH;
    }

    /**
     * Converts global hex coordinates to local hex coordinates within a sector.
     *
     * @param globalR the global r coordinate
     * @return the local r coordinate within the sector
     */
    public static int toLocalHexR(int globalR) {
        return globalR % Sector.DEFAULT_HEIGHT;
    }

    /**
     * Computes the global hex Q coordinate from sector and local coordinates.
     *
     * @param sectorQ the sector q coordinate
     * @param localQ the local q coordinate within the sector
     * @return the global q coordinate
     */
    public static int toGlobalQ(int sectorQ, int localQ) {
        return sectorQ * Sector.DEFAULT_WIDTH + localQ;
    }

    /**
     * Computes the global hex R coordinate from sector and local coordinates.
     *
     * @param sectorR the sector r coordinate
     * @param localR the local r coordinate within the sector
     * @return the global r coordinate
     */
    public static int toGlobalR(int sectorR, int localR) {
        return sectorR * Sector.DEFAULT_HEIGHT + localR;
    }

    @Override
    public String toString() {
        return "QuadrantMap{id=" + id + ", size=" + width + "x" + height + ", sectors=" + sectors.size() + ", hexes=" + totalHexes() + "}";
    }
}
