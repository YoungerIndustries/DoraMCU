package com.yisy.modules.doramcu.hex;

import java.util.Collection;
import java.util.List;

/**
 * Manages the 3D hex geometry for a star.
 * <p>
 * Extracted from Star to separate spatial operations from stellar properties.
 * Handles hex map access, occupancy statistics, and coordinate conversion.
 * </p>
 */
public class StarGeometry {

    private final Star star;

    /**
     * Creates a new geometry manager for the given star.
     *
     * @param star the star whose geometry this manages
     */
    public StarGeometry(Star star) {
        this.star = star;
    }

    // ---- Hex operations ----

    /**
     * Retrieves a hex by its cubic coordinates within the star.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex at the given position, or null if out of bounds
     */
    public Hex3D get(int x, int y) {
        return star.getMap().get(x, y);
    }

    /**
     * Retrieves a hex by a Hex3D instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        return star.getMap().get(hex);
    }

    /**
     * Checks if the given coordinates are within this star.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are within the star's sphere
     */
    public boolean contains(int x, int y) {
        return star.getMap().contains(x, y);
    }

    /**
     * Checks if the given hex is within this star.
     *
     * @param hex the hex to check
     * @return true if the hex is within the star
     */
    public boolean contains(Hex3D hex) {
        return star.getMap().get(hex) != null;
    }

    /**
     * @return all hex cells in the star (unmodifiable collection)
     */
    public Collection<Hex3D> getAll() {
        return star.getMap().getAll();
    }

    // ---- Query operations ----

    /**
     * Returns all hexes in the given state.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex3D> findByState(HexState state) {
        return star.getMap().findByState(state);
    }

    /**
     * Returns the neighbors of the given hex that are also in this star.
     *
     * @param hex the center hex
     * @return list of neighboring hexes present in the star
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        return star.getMap().getNeighbors(hex);
    }

    // ---- Occupancy statistics ----

    /**
     * @return the number of occupied hex cells
     */
    public int getOccupiedCount() {
        return findByState(HexState.OCCUPIED).size();
    }

    /**
     * @return the number of empty hex cells
     */
    public int getEmptyCount() {
        return findByState(HexState.EMPTY).size();
    }

    /**
     * @return the number of blocked hex cells
     */
    public int getBlockedCount() {
        return findByState(HexState.BLOCKED).size();
    }

    /**
     * @return the number of selected hex cells
     */
    public int getSelectedCount() {
        return findByState(HexState.SELECTED).size();
    }

    /**
     * @return the ratio of occupied hexes to total hexes (0.0 to 1.0)
     */
    public double getOccupancyRatio() {
        if (star.size() == 0) return 0.0;
        return (double) getOccupiedCount() / star.size();
    }

    // ---- Coordinate conversion utilities ----

    /**
     * Converts local star coordinates to global coordinates.
     * <p>
     * Since a star is centered at the origin, local and global coordinates are the same.
     *
     * @param localX the local x coordinate
     * @param localY the local y coordinate
     * @return array of global coordinates [x, y, z]
     */
    public int[] toGlobal(int localX, int localY) {
        int z = -localX - localY;
        return new int[]{localX, localY, z};
    }

    /**
     * Checks if the given coordinates are within the star's bounds.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the coordinates are within the star's radius from the center
     */
    public boolean isInBounds(int x, int y, int z) {
        if (x + y + z != 0) {
            return false;
        }
        Hex3D center = new Hex3D(0, 0, 0);
        Hex3D hex = new Hex3D(x, y, z);
        return center.distance(hex) <= star.getRadius();
    }

    /**
     * Returns the center hex of the star.
     *
     * @return the center hex at (0, 0, 0)
     */
    public Hex3D getCenter() {
        return star.getMap().get(0, 0);
    }

    /**
     * Returns the hexes on the surface of the star (at exactly the star's radius from center).
     *
     * @return list of surface hexes
     */
    public List<Hex3D> getSurface() {
        Hex3D center = new Hex3D(0, 0, 0);
        return star.getMap().getAll().stream()
                .filter(h -> center.distance(h) == star.getRadius())
                .toList();
    }

    /**
     * Returns the hexes in the core of the star (within half the radius from center).
     *
     * @return list of core hexes
     */
    public List<Hex3D> getCore() {
        int coreRadius = star.getRadius() / 2;
        Hex3D center = new Hex3D(0, 0, 0);
        return star.getMap().getAll().stream()
                .filter(h -> center.distance(h) <= coreRadius)
                .toList();
    }
}
