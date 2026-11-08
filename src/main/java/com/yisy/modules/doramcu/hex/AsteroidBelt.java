package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * An asteroid belt — a ring of rocky debris orbiting a star.
 * <p>
 * Asteroid belts are primary sources of minerals and can host mining stations.
 * They provide navigation hazards that slow ship movement and block line of sight.
 * <p>
 * Based on implementations in Stellaris, Galactic Civilizations IV, and FreeOrion.
 */
public class AsteroidBelt extends CelestialBody {

    public static final int DEFAULT_RADIUS = 8;
    public static final int DEFAULT_MINERAL_YIELD = 5;
    public static final int DEFAULT_STATION_SLOTS = 3;
    public static final double DEFAULT_MOVEMENT_PENALTY = 0.5;

    private int mineralYield;
    private int stationSlots;
    private final List<String> occupiedStations;
    private double movementPenalty;

    /**
     * Creates a new asteroid belt with the given ID and default radius of 8.
     *
     * @param id the unique identifier for this asteroid belt
     */
    public AsteroidBelt(int id) {
        this(id, DEFAULT_RADIUS);
    }

    /**
     * Creates a new asteroid belt with the given ID and custom radius.
     *
     * @param id the unique identifier for this asteroid belt
     * @param radius the radius of the belt
     * @throws IllegalArgumentException if radius is negative
     */
    public AsteroidBelt(int id, int radius) {
        super(id, radius);
        this.mineralYield = DEFAULT_MINERAL_YIELD;
        this.stationSlots = DEFAULT_STATION_SLOTS;
        this.occupiedStations = new ArrayList<>();
        this.movementPenalty = DEFAULT_MOVEMENT_PENALTY;
    }

    @Override
    protected String defaultName(int id) {
        return "AsteroidBelt-" + id;
    }

    // ---- Resource properties ----

    /**
     * Returns the mineral yield per turn from this asteroid belt.
     *
     * @return the mineral yield
     */
    public int getMineralYield() {
        return mineralYield;
    }

    /**
     * Sets the mineral yield per turn.
     *
     * @param mineralYield the mineral yield (must be non-negative)
     * @throws IllegalArgumentException if yield is negative
     */
    public void setMineralYield(int mineralYield) {
        if (mineralYield < 0) {
            throw new IllegalArgumentException("Mineral yield must be non-negative: " + mineralYield);
        }
        this.mineralYield = mineralYield;
    }

    /**
     * Returns the effective mineral yield, boosted by mining stations.
     * Each mining station adds a 20% bonus to the base yield.
     *
     * @return the effective mineral yield per turn
     */
    public int getEffectiveMineralYield() {
        double bonus = occupiedStations.size() * 0.2;
        return (int) Math.round(mineralYield * (1.0 + bonus));
    }

    // ---- Station management ----

    /**
     * Returns the total number of station slots available.
     *
     * @return the number of station slots
     */
    public int getStationSlots() {
        return stationSlots;
    }

    /**
     * Sets the total number of station slots.
     *
     * @param stationSlots the number of slots (must be non-negative)
     * @throws IllegalArgumentException if slots are negative
     */
    public void setStationSlots(int stationSlots) {
        if (stationSlots < 0) {
            throw new IllegalArgumentException("Station slots must be non-negative: " + stationSlots);
        }
        this.stationSlots = stationSlots;
    }

    /**
     * Returns the number of available (unoccupied) station slots.
     *
     * @return the number of available slots
     */
    public int getAvailableStationSlots() {
        return stationSlots - occupiedStations.size();
    }

    /**
     * Checks if there is space for another station.
     *
     * @return true if at least one slot is available
     */
    public boolean hasAvailableStationSlot() {
        return occupiedStations.size() < stationSlots;
    }

    /**
     * Adds a mining station to this belt.
     *
     * @param stationName the name of the station
     * @return true if the station was added, false if no slots available
     */
    public boolean addStation(String stationName) {
        if (!hasAvailableStationSlot()) {
            return false;
        }
        return occupiedStations.add(stationName);
    }

    /**
     * Removes a station from this belt.
     *
     * @param stationName the name of the station to remove
     * @return true if the station was removed
     */
    public boolean removeStation(String stationName) {
        return occupiedStations.remove(stationName);
    }

    /**
     * Returns all occupied station names.
     *
     * @return unmodifiable list of station names
     */
    public List<String> getOccupiedStations() {
        return Collections.unmodifiableList(occupiedStations);
    }

    // ---- Hazard properties ----

    /**
     * Returns the movement penalty multiplier for ships passing through this belt.
     * A value of 0.5 means ships move at half speed.
     *
     * @return the movement penalty (0.0 to 1.0)
     */
    public double getMovementPenalty() {
        return movementPenalty;
    }

    /**
     * Sets the movement penalty multiplier.
     *
     * @param movementPenalty the penalty (0.0 to 1.0)
     * @throws IllegalArgumentException if penalty is out of range
     */
    public void setMovementPenalty(double movementPenalty) {
        if (movementPenalty < 0.0 || movementPenalty > 1.0) {
            throw new IllegalArgumentException("Movement penalty must be between 0.0 and 1.0: " + movementPenalty);
        }
        this.movementPenalty = movementPenalty;
    }

    /**
     * Returns the effective movement speed for a ship passing through this belt.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the reduced movement speed
     */
    public int getEffectiveMovementSpeed(int baseSpeed) {
        return (int) Math.max(1, Math.round(baseSpeed * movementPenalty));
    }

    // ---- Hex operations ----

    /**
     * Retrieves a hex by its cubic coordinates within the belt.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex at the given position, or null if out of bounds
     */
    public Hex3D get(int x, int y) {
        return getMap().get(x, y);
    }

    /**
     * Retrieves a hex by a Hex3D instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        return getMap().get(hex);
    }

    /**
     * Checks if the given coordinates are within this belt.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are within the belt
     */
    public boolean contains(int x, int y) {
        return getMap().contains(x, y);
    }

    /**
     * Checks if the given hex is within this belt.
     *
     * @param hex the hex to check
     * @return true if the hex is within the belt
     */
    public boolean contains(Hex3D hex) {
        return getMap().get(hex) != null;
    }

    /**
     * @return all hex cells in the belt (unmodifiable collection)
     */
    public Collection<Hex3D> getAll() {
        return getMap().getAll();
    }

    /**
     * Returns all hexes in the given state.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex3D> findByState(HexState state) {
        return getMap().findByState(state);
    }

    /**
     * Returns the hexes on the outer edge of the belt (at exactly the belt's radius from center).
     *
     * @return list of outer edge hexes
     */
    public List<Hex3D> getOuterEdge() {
        ensureGeometryGenerated();
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) == radius)
                .toList();
    }

    /**
     * Returns the center hex of the belt.
     *
     * @return the center hex at (0, 0, 0)
     */
    public Hex3D getCenter() {
        ensureGeometryGenerated();
        return map.get(0, 0);
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsteroidBelt that = (AsteroidBelt) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("AsteroidBelt{name='%s', id=%d, radius=%d, minerals=%d, stations=%d/%d, movementPenalty=%.0f%%}",
                name, id, radius, mineralYield, occupiedStations.size(), stationSlots, movementPenalty * 100);
    }
}
