package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A nebula — a cloud of gas and dust in space.
 * <p>
 * Nebulas are the primary source of Elerium (used for beam weapons and shields).
 * They provide tactical advantages: ships within a nebula gain cloaking ability
 * and sensor jamming that reduces enemy accuracy. Navigation through nebulas
 * is hazardous due to reduced visibility.
 * <p>
 * Based on implementations in Galactic Civilizations IV and Stellaris.
 */
public class Nebula extends CelestialBody {

    public static final int DEFAULT_RADIUS = 10;
    public static final int DEFAULT_ELERIUM_YIELD = 3;
    public static final int DEFAULT_STATION_SLOTS = 2;
    public static final double DEFAULT_SENSOR_JAMMING = 0.4;
    public static final double DEFAULT_CLOAKING_BONUS = 0.6;

    /**
     * Nebula size classification affecting radius, resource yield, and tactical effects.
     */
    public enum NebulaSize {
        /**
         * Tiny nebula — small area, minimal resources, weak effects.
         */
        TINY(3, 1, 1, 0.2, 0.3),

        /**
         * Small nebula — modest area and resources.
         */
        SMALL(5, 2, 1, 0.3, 0.4),

        /**
         * Medium nebula — balanced area, resources, and effects.
         */
        MEDIUM(10, 3, 2, 0.4, 0.6),

        /**
         * Large nebula — significant area and strong effects.
         */
        LARGE(15, 5, 3, 0.5, 0.7),

        /**
         * Massive nebula — vast area, maximum resources and effects.
         */
        MASSIVE(20, 8, 4, 0.6, 0.8);

        private final int baseRadius;
        private final int baseEleriumYield;
        private final int baseStationSlots;
        private final double baseSensorJamming;
        private final double baseCloakingBonus;

        NebulaSize(int baseRadius, int baseEleriumYield, int baseStationSlots,
                   double baseSensorJamming, double baseCloakingBonus) {
            this.baseRadius = baseRadius;
            this.baseEleriumYield = baseEleriumYield;
            this.baseStationSlots = baseStationSlots;
            this.baseSensorJamming = baseSensorJamming;
            this.baseCloakingBonus = baseCloakingBonus;
        }

        /**
         * @return the base radius for this nebula size
         */
        public int getBaseRadius() {
            return baseRadius;
        }

        /**
         * @return the base Elerium yield for this nebula size
         */
        public int getBaseEleriumYield() {
            return baseEleriumYield;
        }

        /**
         * @return the base station slots for this nebula size
         */
        public int getBaseStationSlots() {
            return baseStationSlots;
        }

        /**
         * @return the base sensor jamming for this nebula size
         */
        public double getBaseSensorJamming() {
            return baseSensorJamming;
        }

        /**
         * @return the base cloaking bonus for this nebula size
         */
        public double getBaseCloakingBonus() {
            return baseCloakingBonus;
        }
    }

    private NebulaSize nebulaSize;
    private int eleriumYield;
    private int stationSlots;
    private final List<String> occupiedStations;
    private double sensorJamming;
    private double cloakingBonus;

    /**
     * Creates a new nebula with the given ID and default radius of 10.
     *
     * @param id the unique identifier for this nebula
     */
    public Nebula(int id) {
        this(id, DEFAULT_RADIUS);
    }

    /**
     * Creates a new nebula with the given ID and custom radius.
     *
     * @param id the unique identifier for this nebula
     * @param radius the radius of the nebula
     * @throws IllegalArgumentException if radius is negative
     */
    public Nebula(int id, int radius) {
        super(id, radius);
        this.nebulaSize = NebulaSize.MEDIUM;
        this.eleriumYield = DEFAULT_ELERIUM_YIELD;
        this.stationSlots = DEFAULT_STATION_SLOTS;
        this.occupiedStations = new ArrayList<>();
        this.sensorJamming = DEFAULT_SENSOR_JAMMING;
        this.cloakingBonus = DEFAULT_CLOAKING_BONUS;
        this.hasStrategicDeposit = true;
        this.strategicResource = StrategicResource.ELERIUM;
    }

    /**
     * Creates a new nebula with the given ID and nebula size.
     * Radius, Elerium yield, station slots, and tactical effects are derived from the size.
     *
     * @param id the unique identifier for this nebula
     * @param nebulaSize the size classification of the nebula
     * @throws IllegalArgumentException if nebulaSize is null
     */
    public Nebula(int id, NebulaSize nebulaSize) {
        if (nebulaSize == null) {
            throw new IllegalArgumentException("Nebula size must not be null");
        }
        super(id, nebulaSize.getBaseRadius());
        this.nebulaSize = nebulaSize;
        this.eleriumYield = nebulaSize.getBaseEleriumYield();
        this.stationSlots = nebulaSize.getBaseStationSlots();
        this.occupiedStations = new ArrayList<>();
        this.sensorJamming = nebulaSize.getBaseSensorJamming();
        this.cloakingBonus = nebulaSize.getBaseCloakingBonus();
        this.hasStrategicDeposit = true;
        this.strategicResource = StrategicResource.ELERIUM;
    }

    /**
     * Generates a cloud of hexes centered at the origin.
     */
    @Override
    protected void generate() {
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, radius);
    }

    @Override
    protected String defaultName(int id) {
        return "Nebula-" + id;
    }

    /**
     * @return the nebula size classification
     */
    public NebulaSize getNebulaSize() {
        return nebulaSize;
    }

    /**
     * Sets the nebula size classification.
     * Updates Elerium yield, station slots, sensor jamming, and cloaking bonus based on the new size.
     *
     * @param nebulaSize the new size classification
     */
    public void setNebulaSize(NebulaSize nebulaSize) {
        if (nebulaSize == null) {
            throw new IllegalArgumentException("Nebula size must not be null");
        }
        this.nebulaSize = nebulaSize;
        this.eleriumYield = nebulaSize.getBaseEleriumYield();
        this.stationSlots = nebulaSize.getBaseStationSlots();
        this.sensorJamming = nebulaSize.getBaseSensorJamming();
        this.cloakingBonus = nebulaSize.getBaseCloakingBonus();
    }

    // ---- Resource properties ----

    /**
     * Returns the Elerium yield per turn from this nebula.
     *
     * @return the Elerium yield
     */
    public int getEleriumYield() {
        return eleriumYield;
    }

    /**
     * Sets the Elerium yield per turn.
     *
     * @param eleriumYield the Elerium yield (must be non-negative)
     * @throws IllegalArgumentException if yield is negative
     */
    public void setEleriumYield(int eleriumYield) {
        if (eleriumYield < 0) {
            throw new IllegalArgumentException("Elerium yield must be non-negative: " + eleriumYield);
        }
        this.eleriumYield = eleriumYield;
    }

    /**
     * Returns the effective Elerium yield, boosted by extraction stations.
     * Each extraction station adds a 25% bonus to the base yield.
     *
     * @return the effective Elerium yield per turn
     */
    public int getEffectiveEleriumYield() {
        double bonus = occupiedStations.size() * 0.25;
        return (int) Math.round(eleriumYield * (1.0 + bonus));
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
     * Adds an extraction station to this nebula.
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
     * Removes a station from this nebula.
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

    // ---- Tactical effects ----

    /**
     * Returns the sensor jamming multiplier.
     * A value of 0.4 means enemy accuracy is reduced by 40% within the nebula.
     *
     * @return the sensor jamming (0.0 to 1.0)
     */
    public double getSensorJamming() {
        return sensorJamming;
    }

    /**
     * Sets the sensor jamming multiplier.
     *
     * @param sensorJamming the jamming (0.0 to 1.0)
     * @throws IllegalArgumentException if jamming is out of range
     */
    public void setSensorJamming(double sensorJamming) {
        if (sensorJamming < 0.0 || sensorJamming > 1.0) {
            throw new IllegalArgumentException("Sensor jamming must be between 0.0 and 1.0: " + sensorJamming);
        }
        this.sensorJamming = sensorJamming;
    }

    /**
     * Returns the effective enemy accuracy when attacking from within this nebula.
     *
     * @param baseAccuracy the attacker's base accuracy (0.0 to 1.0)
     * @return the reduced accuracy
     */
    public double getEffectiveEnemyAccuracy(double baseAccuracy) {
        return Math.max(0.0, baseAccuracy * (1.0 - sensorJamming));
    }

    /**
     * Returns the cloaking bonus multiplier.
     * A value of 0.6 means ships gain 60% evasion when within the nebula.
     *
     * @return the cloaking bonus (0.0 to 1.0)
     */
    public double getCloakingBonus() {
        return cloakingBonus;
    }

    /**
     * Sets the cloaking bonus multiplier.
     *
     * @param cloakingBonus the bonus (0.0 to 1.0)
     * @throws IllegalArgumentException if bonus is out of range
     */
    public void setCloakingBonus(double cloakingBonus) {
        if (cloakingBonus < 0.0 || cloakingBonus > 1.0) {
            throw new IllegalArgumentException("Cloaking bonus must be between 0.0 and 1.0: " + cloakingBonus);
        }
        this.cloakingBonus = cloakingBonus;
    }

    /**
     * Returns the effective evasion for a ship within this nebula.
     *
     * @param baseEvasion the ship's base evasion (0.0 to 1.0)
     * @return the enhanced evasion
     */
    public double getEffectiveEvasion(double baseEvasion) {
        return Math.min(1.0, baseEvasion + cloakingBonus);
    }

    /**
     * Sets whether this nebula has a strategic resource deposit.
     *
     * @param resource the strategic resource type
     */
    public void setStrategicDeposit(StrategicResource resource) {
        setStrategicResource(resource);
    }

    // ---- Hex operations ----

    /**
     * Retrieves a hex by its cubic coordinates within the nebula.
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
     * Checks if the given coordinates are within this nebula.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are within the nebula
     */
    public boolean contains(int x, int y) {
        ensureGeometryGenerated();
        return map.contains(x, y);
    }

    /**
     * Checks if the given hex is within this nebula.
     *
     * @param hex the hex to check
     * @return true if the hex is within the nebula
     */
    public boolean contains(Hex3D hex) {
        ensureGeometryGenerated();
        return map.get(hex) != null;
    }

    /**
     * @return all hex cells in the nebula (unmodifiable collection)
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
     * Returns the hexes in the dense core of the nebula (inner 50% of the radius).
     * Ships in the core receive maximum cloaking and sensor jamming effects.
     *
     * @return list of core hexes
     */
    public List<Hex3D> getCore() {
        ensureGeometryGenerated();
        int coreRadius = radius / 2;
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) <= coreRadius)
                .toList();
    }

    /**
     * Returns the hexes at the nebula's edge (outer 50% of the radius).
     * Effects are weaker at the edges.
     *
     * @return list of edge hexes
     */
    public List<Hex3D> getEdge() {
        ensureGeometryGenerated();
        int coreRadius = radius / 2;
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> {
                    int dist = center.distance(h);
                    return dist > coreRadius && dist <= radius;
                })
                .toList();
    }

    /**
     * Returns the center hex of the nebula.
     *
     * @return the center hex at (0, 0, 0)
     */
    public Hex3D getCenter() {
        ensureGeometryGenerated();
        return map.get(0, 0);
    }

    @Override
    public String toString() {
        return String.format("Nebula{name='%s', id=%d, radius=%d, elerium=%d, stations=%d/%d, jamming=%.0f%%, cloaking=%.0f%%}",
                name, id, radius, eleriumYield, occupiedStations.size(), stationSlots,
                sensorJamming * 100, cloakingBonus * 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nebula other = (Nebula) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
