package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A black hole — a gravitational singularity in space.
 * <p>
 * Black holes are the primary source of Anti-Matter (used for missile weapons).
 * They create extreme gravitational hazards: ships too close risk being pulled in,
 * but the event horizon can be used for powerful gravity assists. Black holes
 * also distort space-time, creating navigation challenges.
 * <p>
 * Based on implementations in Galactic Civilizations IV and Stellaris.
 */
public class BlackHole extends CelestialBody {

    public static final int DEFAULT_RADIUS = 6;
    public static final int DEFAULT_ANTIMATTER_YIELD = 2;
    public static final int DEFAULT_STATION_SLOTS = 1;
    public static final double DEFAULT_GRAVITATIONAL_PULL = 0.7;
    public static final double DEFAULT_DESTRUCTION_CHANCE = 0.05;

    /**
     * Black hole size classification affecting radius, resource yield, and hazard effects.
     */
    public enum BlackHoleSize {
        /**
         * Tiny black hole — small event horizon, minimal resources, weak hazards.
         */
        TINY(3, 1, 0, 0.4, 0.02),

        /**
         * Small black hole — modest area and resources.
         */
        SMALL(5, 1, 1, 0.5, 0.03),

        /**
         * Medium black hole — balanced area, resources, and hazards.
         */
        MEDIUM(6, 2, 1, 0.7, 0.05),

        /**
         * Large black hole — significant area and strong hazards.
         */
        LARGE(10, 4, 2, 0.8, 0.08),

        /**
         * Massive black hole — vast event horizon, maximum resources and extreme hazards.
         */
        MASSIVE(15, 6, 2, 0.9, 0.12);

        private final int baseRadius;
        private final int baseAntimatterYield;
        private final int baseStationSlots;
        private final double baseGravitationalPull;
        private final double baseDestructionChance;

        BlackHoleSize(int baseRadius, int baseAntimatterYield, int baseStationSlots,
                      double baseGravitationalPull, double baseDestructionChance) {
            this.baseRadius = baseRadius;
            this.baseAntimatterYield = baseAntimatterYield;
            this.baseStationSlots = baseStationSlots;
            this.baseGravitationalPull = baseGravitationalPull;
            this.baseDestructionChance = baseDestructionChance;
        }

        /**
         * @return the base radius for this black hole size
         */
        public int getBaseRadius() {
            return baseRadius;
        }

        /**
         * @return the base Anti-Matter yield for this black hole size
         */
        public int getBaseAntimatterYield() {
            return baseAntimatterYield;
        }

        /**
         * @return the base station slots for this black hole size
         */
        public int getBaseStationSlots() {
            return baseStationSlots;
        }

        /**
         * @return the base gravitational pull for this black hole size
         */
        public double getBaseGravitationalPull() {
            return baseGravitationalPull;
        }

        /**
         * @return the base destruction chance for this black hole size
         */
        public double getBaseDestructionChance() {
            return baseDestructionChance;
        }
    }

    private BlackHoleSize blackHoleSize;
    private int antimatterYield;
    private int stationSlots;
    private final List<String> occupiedStations;
    private double gravitationalPull;
    private double destructionChance;

    /**
     * Creates a new black hole with the given ID and default radius of 6.
     *
     * @param id the unique identifier for this black hole
     */
    public BlackHole(int id) {
        this(id, DEFAULT_RADIUS);
    }

    /**
     * Creates a new black hole with the given ID and custom radius.
     *
     * @param id the unique identifier for this black hole
     * @param radius the radius of the black hole's event horizon
     * @throws IllegalArgumentException if radius is negative
     */
    public BlackHole(int id, int radius) {
        super(id, radius);
        this.blackHoleSize = BlackHoleSize.MEDIUM;
        this.antimatterYield = DEFAULT_ANTIMATTER_YIELD;
        this.stationSlots = DEFAULT_STATION_SLOTS;
        this.occupiedStations = new ArrayList<>();
        this.gravitationalPull = DEFAULT_GRAVITATIONAL_PULL;
        this.destructionChance = DEFAULT_DESTRUCTION_CHANCE;
        this.hasStrategicDeposit = true;
        this.strategicResource = StrategicResource.ANTI_MATTER;
        generate();
    }

    /**
     * Creates a new black hole with the given ID and black hole size.
     * Radius, Anti-Matter yield, station slots, and hazard effects are derived from the size.
     *
     * @param id the unique identifier for this black hole
     * @param blackHoleSize the size classification of the black hole
     * @throws IllegalArgumentException if blackHoleSize is null
     */
    public BlackHole(int id, BlackHoleSize blackHoleSize) {
        if (blackHoleSize == null) {
            throw new IllegalArgumentException("Black hole size must not be null");
        }
        super(id, blackHoleSize.getBaseRadius());
        this.blackHoleSize = blackHoleSize;
        this.antimatterYield = blackHoleSize.getBaseAntimatterYield();
        this.stationSlots = blackHoleSize.getBaseStationSlots();
        this.occupiedStations = new ArrayList<>();
        this.gravitationalPull = blackHoleSize.getBaseGravitationalPull();
        this.destructionChance = blackHoleSize.getBaseDestructionChance();
        this.hasStrategicDeposit = true;
        this.strategicResource = StrategicResource.ANTI_MATTER;
        generate();
    }

    /**
     * Generates a sphere of hexes centered at the origin representing the event horizon.
     */
    @Override
    protected void generate() {
        Hex3D center = new Hex3D(0, 0, 0);
        map.generateRange(center, radius);
    }

    @Override
    protected String defaultName(int id) {
        return "BlackHole-" + id;
    }

    /**
     * @return the black hole size classification
     */
    public BlackHoleSize getBlackHoleSize() {
        return blackHoleSize;
    }

    /**
     * Sets the black hole size classification.
     * Updates Anti-Matter yield, station slots, gravitational pull, and destruction chance based on the new size.
     *
     * @param blackHoleSize the new size classification
     */
    public void setBlackHoleSize(BlackHoleSize blackHoleSize) {
        if (blackHoleSize == null) {
            throw new IllegalArgumentException("Black hole size must not be null");
        }
        this.blackHoleSize = blackHoleSize;
        this.antimatterYield = blackHoleSize.getBaseAntimatterYield();
        this.stationSlots = blackHoleSize.getBaseStationSlots();
        this.gravitationalPull = blackHoleSize.getBaseGravitationalPull();
        this.destructionChance = blackHoleSize.getBaseDestructionChance();
    }

    // ---- Resource properties ----

    /**
     * Returns the Anti-Matter yield per turn from this black hole.
     *
     * @return the Anti-Matter yield
     */
    public int getAntimatterYield() {
        return antimatterYield;
    }

    /**
     * Sets the Anti-Matter yield per turn.
     *
     * @param antimatterYield the Anti-Matter yield (must be non-negative)
     * @throws IllegalArgumentException if yield is negative
     */
    public void setAntimatterYield(int antimatterYield) {
        if (antimatterYield < 0) {
            throw new IllegalArgumentException("Anti-Matter yield must be non-negative: " + antimatterYield);
        }
        this.antimatterYield = antimatterYield;
    }

    /**
     * Returns the effective Anti-Matter yield, boosted by extraction stations.
     * Each extraction station adds a 30% bonus to the base yield.
     *
     * @return the effective Anti-Matter yield per turn
     */
    public int getEffectiveAntimatterYield() {
        double bonus = occupiedStations.size() * 0.3;
        return (int) Math.round(antimatterYield * (1.0 + bonus));
    }

    // ---- Station management ----

    /**
     * Returns the total number of station slots available.
     * Black holes have very limited station slots due to extreme hazards.
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
     * Adds an extraction station to this black hole.
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
     * Removes a station from this black hole.
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
     * A value of 0.7 means ships lose 70% movement when near the black hole.
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
     * Returns the effective movement speed for a ship near this black hole.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the reduced movement speed
     */
    public int getEffectiveMovementSpeed(int baseSpeed) {
        return (int) Math.max(1, Math.round(baseSpeed * (1.0 - gravitationalPull)));
    }

    /**
     * Calculates the extreme slingshot bonus for a ship performing a gravity assist.
     * Black holes provide much stronger slingshot bonuses than gas giants.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the additional movement points gained
     */
    public int getSlingshotBonus(int baseSpeed) {
        return (int) Math.round(baseSpeed * gravitationalPull * 1.5);
    }

    // ---- Destruction hazard ----

    /**
     * Returns the chance of a ship being destroyed if it enters the event horizon.
     * A value of 0.05 means a 5% chance per turn.
     *
     * @return the destruction chance (0.0 to 1.0)
     */
    public double getDestructionChance() {
        return destructionChance;
    }

    /**
     * Sets the destruction chance for ships entering the event horizon.
     *
     * @param destructionChance the chance (0.0 to 1.0)
     * @throws IllegalArgumentException if chance is out of range
     */
    public void setDestructionChance(double destructionChance) {
        if (destructionChance < 0.0 || destructionChance > 1.0) {
            throw new IllegalArgumentException("Destruction chance must be between 0.0 and 1.0: " + destructionChance);
        }
        this.destructionChance = destructionChance;
    }

    /**
     * Checks if a ship entering the event horizon is destroyed.
     * Uses a random roll against the destruction chance.
     *
     * @param random the random number generator
     * @return true if the ship is destroyed
     */
    public boolean isShipDestroyed(Random random) {
        return random.nextDouble() < destructionChance;
    }

    /**
     * Sets whether this black hole has a strategic resource deposit.
     *
     * @param resource the strategic resource type
     */
    public void setStrategicDeposit(StrategicResource resource) {
        setStrategicResource(resource);
    }

    // ---- Hex operations (delegated to map) ----

    /**
     * Retrieves a hex by its cubic coordinates within the event horizon.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex at the given position, or null if out of bounds
     */
    public Hex3D get(int x, int y) {
        return map.get(x, y);
    }

    /**
     * Retrieves a hex by a Hex3D instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        return map.get(hex);
    }

    /**
     * Checks if the given coordinates are within this black hole's event horizon.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are within the event horizon
     */
    public boolean contains(int x, int y) {
        return map.contains(x, y);
    }

    /**
     * Checks if the given hex is within this black hole's event horizon.
     *
     * @param hex the hex to check
     * @return true if the hex is within the event horizon
     */
    public boolean contains(Hex3D hex) {
        return map.get(hex) != null;
    }

    /**
     * @return all hex cells in the event horizon (unmodifiable collection)
     */
    public Collection<Hex3D> getAll() {
        return map.getAll();
    }

    /**
     * Returns all hexes in the given state.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex3D> findByState(HexState state) {
        return map.findByState(state);
    }

    /**
     * Returns the hexes at the event horizon boundary (at exactly the black hole's radius from center).
     * This is the point of no return for ships.
     *
     * @return list of event horizon boundary hexes
     */
    public List<Hex3D> getEventHorizon() {
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) == radius)
                .toList();
    }

    /**
     * Returns the hexes in the inner region of the black hole (inner 50% of the radius).
     * Destruction chance is doubled in this region.
     *
     * @return list of inner region hexes
     */
    public List<Hex3D> getInnerRegion() {
        int innerRadius = radius / 2;
        Hex3D center = new Hex3D(0, 0, 0);
        return map.getAll().stream()
                .filter(h -> center.distance(h) <= innerRadius)
                .toList();
    }

    /**
     * Returns the effective destruction chance for a hex based on its distance from center.
     * Inner region hexes have double the destruction chance.
     *
     * @param hex the hex to check
     * @return the destruction chance at that hex
     */
    public double getDestructionChanceAt(Hex3D hex) {
        if (hex == null) {
            return 0.0;
        }
        Hex3D center = new Hex3D(0, 0, 0);
        int innerRadius = radius / 2;
        if (center.distance(hex) <= innerRadius) {
            return Math.min(1.0, destructionChance * 2.0);
        }
        return destructionChance;
    }

    /**
     * Returns the center hex of the black hole (the singularity).
     *
     * @return the center hex at (0, 0, 0)
     */
    public Hex3D getCenter() {
        return map.get(0, 0);
    }

    @Override
    public String toString() {
        return String.format("BlackHole{name='%s', id=%d, radius=%d, antimatter=%d, stations=%d/%d, gravity=%.0f%%, destruction=%.0f%%}",
                name, id, radius, antimatterYield, occupiedStations.size(), stationSlots,
                gravitationalPull * 100, destructionChance * 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlackHole other = (BlackHole) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
