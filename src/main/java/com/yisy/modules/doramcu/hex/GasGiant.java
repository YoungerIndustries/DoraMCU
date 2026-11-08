package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * A gas giant — a massive gaseous planet (Jupiter/Saturn-like).
 * <p>
 * Gas giants provide atmospheric resources (helium-3, exotic gases) and can host
 * orbital platform stations. Their massive gravity creates a gravitational pull
 * that affects nearby ships and can be used for slingshot maneuvers.
 * <p>
 * Based on implementations in Stellaris, Galactic Civilizations IV, and FreeOrion.
 */
public class GasGiant extends CelestialBody {

    public static final int DEFAULT_RADIUS = 12;
    public static final int DEFAULT_ENERGY_YIELD = 8;
    public static final int DEFAULT_STATION_SLOTS = 4;
    public static final double DEFAULT_GRAVITATIONAL_PULL = 0.3;

    private int energyYield;
    private int stationSlots;
    private final List<String> occupiedStations;
    private double gravitationalPull;
    private final Set<String> atmosphericResources;

    /**
     * Creates a new gas giant with the given ID and default radius of 12.
     *
     * @param id the unique identifier for this gas giant
     */
    public GasGiant(int id) {
        this(id, DEFAULT_RADIUS);
    }

    /**
     * Creates a new gas giant with the given ID and custom radius.
     *
     * @param id the unique identifier for this gas giant
     * @param radius the radius of the gas giant
     * @throws IllegalArgumentException if radius is negative
     */
    public GasGiant(int id, int radius) {
        super(id, radius);
        this.energyYield = DEFAULT_ENERGY_YIELD;
        this.stationSlots = DEFAULT_STATION_SLOTS;
        this.occupiedStations = new ArrayList<>();
        this.gravitationalPull = DEFAULT_GRAVITATIONAL_PULL;
        this.atmosphericResources = new HashSet<>();
    }

    @Override
    protected String defaultName(int id) {
        return "GasGiant-" + id;
    }

    // ---- Resource properties ----

    /**
     * Returns the energy yield per turn from this gas giant.
     *
     * @return the energy yield
     */
    public int getEnergyYield() {
        return energyYield;
    }

    /**
     * Sets the energy yield per turn.
     *
     * @param energyYield the energy yield (must be non-negative)
     * @throws IllegalArgumentException if yield is negative
     */
    public void setEnergyYield(int energyYield) {
        if (energyYield < 0) {
            throw new IllegalArgumentException("Energy yield must be non-negative: " + energyYield);
        }
        this.energyYield = energyYield;
    }

    /**
     * Returns the effective energy yield, boosted by extraction platforms.
     * Each extraction platform adds a 15% bonus to the base yield.
     *
     * @return the effective energy yield per turn
     */
    public int getEffectiveEnergyYield() {
        double bonus = occupiedStations.size() * 0.15;
        return (int) Math.round(energyYield * (1.0 + bonus));
    }

    // ---- Atmospheric resources ----

    /**
     * Adds an atmospheric resource to this gas giant.
     *
     * @param resource the resource name (e.g., "Helium-3", "ExoticGas")
     * @return true if the resource was added
     */
    public boolean addAtmosphericResource(String resource) {
        return atmosphericResources.add(resource);
    }

    /**
     * Removes an atmospheric resource from this gas giant.
     *
     * @param resource the resource name to remove
     * @return true if the resource was removed
     */
    public boolean removeAtmosphericResource(String resource) {
        return atmosphericResources.remove(resource);
    }

    /**
     * Checks if this gas giant has a specific atmospheric resource.
     *
     * @param resource the resource name to check
     * @return true if the resource is present
     */
    public boolean hasAtmosphericResource(String resource) {
        return atmosphericResources.contains(resource);
    }

    /**
     * Returns all atmospheric resources.
     *
     * @return unmodifiable set of resource names
     */
    public Set<String> getAtmosphericResources() {
        return Collections.unmodifiableSet(atmosphericResources);
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
     * Adds an orbital platform station to this gas giant.
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
     * Removes a station from this gas giant.
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

    // ---- Gravitational effects ----

    /**
     * Returns the gravitational pull multiplier.
     * A value of 0.3 means ships lose 30% movement when entering the gravity well.
     *
     * @return the gravitational pull (0.0 to 1.0)
     */
    public double getGravitationalPull() {
        return gravitationalPull;
    }

    /**
     * Sets the gravitational pull multiplier.
     *
     * @param gravitationalPull the pull (0.0 to 1.0)
     * @throws IllegalArgumentException if pull is out of range
     */
    public void setGravitationalPull(double gravitationalPull) {
        if (gravitationalPull < 0.0 || gravitationalPull > 1.0) {
            throw new IllegalArgumentException("Gravitational pull must be between 0.0 and 1.0: " + gravitationalPull);
        }
        this.gravitationalPull = gravitationalPull;
    }

    /**
     * Returns the effective movement speed for a ship entering the gravity well.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the reduced movement speed
     */
    public int getEffectiveMovementSpeed(int baseSpeed) {
        return (int) Math.max(1, Math.round(baseSpeed * (1.0 - gravitationalPull)));
    }

    /**
     * Calculates the slingshot bonus for a ship performing a gravity assist.
     * Ships gain movement equal to the gravitational pull percentage of their base speed.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the additional movement points gained
     */
    public int getSlingshotBonus(int baseSpeed) {
        return (int) Math.round(baseSpeed * gravitationalPull);
    }

    // ---- Hex operations ----

    /**
     * Retrieves a hex by its cubic coordinates within the gas giant.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex at the given position, or null if out of bounds
     */
    public Hex3D get(int x, int y) {
        ensureGeometryGenerated();
        return map.get(x, y);
    }

    /**
     * Retrieves a hex by a Hex3D instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        ensureGeometryGenerated();
        return map.get(hex);
    }

    /**
     * Checks if the given coordinates are within this gas giant.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are within the gas giant
     */
    public boolean contains(int x, int y) {
        ensureGeometryGenerated();
        return map.contains(x, y);
    }

    /**
     * Checks if the given hex is within this gas giant.
     *
     * @param hex the hex to check
     * @return true if the hex is within the gas giant
     */
    public boolean contains(Hex3D hex) {
        ensureGeometryGenerated();
        return map.get(hex) != null;
    }

    /**
     * @return all hex cells in the gas giant (unmodifiable collection)
     */
    public Collection<Hex3D> getAll() {
        ensureGeometryGenerated();
        return map.getAll();
    }

    /**
     * Returns all hexes in the given state.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex3D> findByState(HexState state) {
        ensureGeometryGenerated();
        return map.findByState(state);
    }

    /**
     * Returns the hexes in the atmosphere of the gas giant (outer 25% of the radius).
     *
     * @return list of atmosphere hexes
     */
    public List<Hex3D> getAtmosphere() {
        ensureGeometryGenerated();
        int atmosphereInner = (int) Math.floor(radius * 0.75);
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> {
                    int dist = center.distance(h);
                    return dist > atmosphereInner && dist <= radius;
                })
                .toList();
    }

    /**
     * Returns the hexes in the core of the gas giant (inner 25% of the radius).
     *
     * @return list of core hexes
     */
    public List<Hex3D> getCore() {
        ensureGeometryGenerated();
        int coreRadius = (int) Math.floor(radius * 0.25);
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) <= coreRadius)
                .toList();
    }

    /**
     * Returns the center hex of the gas giant.
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
        GasGiant that = (GasGiant) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("GasGiant{name='%s', id=%d, radius=%d, energy=%d, stations=%d/%d, gravity=%.0f%%}",
                name, id, radius, energyYield, occupiedStations.size(), stationSlots, gravitationalPull * 100);
    }
}
