package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.Collection;

/**
 * An asteroid — a single rocky celestial body orbiting a star.
 * <p>
 * Asteroids are small celestial bodies that provide mineral resources and can be
 * mined for strategic resources. They are the basic building blocks of asteroid belts
 * but can also exist independently in space.
 * <p>
 * Individual asteroids can host mining outposts, contain valuable mineral deposits,
 * and serve as navigation hazards for ships passing through their orbit.
 * <p>
 * Based on implementations in Stellaris, Galactic Civilizations IV, and FreeOrion.
 */
public class Asteroid extends CelestialBody {

    public static final int DEFAULT_RADIUS = 1;
    public static final int DEFAULT_MINERAL_YIELD = 1;
    public static final int DEFAULT_OUTPOST_SLOTS = 1;
    public static final double DEFAULT_MOVEMENT_PENALTY = 0.3;

    /**
     * Asteroid size classification affecting capacity and output.
     */
    public enum AsteroidSize {
        /**
         * Pebble-sized asteroid — negligible resources.
         */
        PEBBLE(0, 0, 0.1),

        /**
         * Small asteroid — minimal resources, no outpost.
         */
        SMALL(1, 0, 0.2),

        /**
         * Medium asteroid — modest resources, single outpost.
         */
        MEDIUM(1, 1, 0.3),

        /**
         * Large asteroid — significant resources, two outposts.
         */
        LARGE(2, 2, 0.4),

        /**
         * Protoplanet-sized asteroid — major resource deposit.
         */
        PROTOPLANET(3, 3, 0.5);

        private final int baseMineralYield;
        private final int baseOutpostSlots;
        private final double movementPenalty;

        AsteroidSize(int baseMineralYield, int baseOutpostSlots, double movementPenalty) {
            this.baseMineralYield = baseMineralYield;
            this.baseOutpostSlots = baseOutpostSlots;
            this.movementPenalty = movementPenalty;
        }

        /**
         * @return the base mineral yield for this asteroid size
         */
        public int getBaseMineralYield() {
            return baseMineralYield;
        }

        /**
         * @return the base number of outpost slots for this asteroid size
         */
        public int getBaseOutpostSlots() {
            return baseOutpostSlots;
        }

        /**
         * @return the movement penalty for ships navigating near this asteroid
         */
        public double getMovementPenalty() {
            return movementPenalty;
        }
    }

    private AsteroidSize asteroidSize;
    private int mineralYield;
    private int outpostSlots;
    private final List<String> occupiedOutposts;
    private double movementPenalty;
    private Integer parentBeltId;

    /**
     * Creates a new asteroid with the given ID, default radius of 1, and MEDIUM size.
     *
     * @param id the unique identifier for this asteroid
     */
    public Asteroid(int id) {
        this(id, DEFAULT_RADIUS, AsteroidSize.MEDIUM);
    }

    /**
     * Creates a new asteroid with the given ID and custom radius, defaulting to MEDIUM size.
     *
     * @param id the unique identifier for this asteroid
     * @param radius the radius of the asteroid
     * @throws IllegalArgumentException if radius is negative
     */
    public Asteroid(int id, int radius) {
        this(id, radius, AsteroidSize.MEDIUM);
    }

    /**
     * Creates a new asteroid with the given ID, default radius, and asteroid size.
     * Mineral yield and outpost slots are derived from the asteroid size.
     *
     * @param id the unique identifier for this asteroid
     * @param asteroidSize the size classification of the asteroid
     */
    public Asteroid(int id, AsteroidSize asteroidSize) {
        this(id, DEFAULT_RADIUS, asteroidSize);
    }

    /**
     * Creates a new asteroid with the given ID, custom radius, and asteroid size.
     * Mineral yield and outpost slots are derived from the asteroid size.
     *
     * @param id the unique identifier for this asteroid
     * @param radius the radius of the asteroid
     * @param asteroidSize the size classification of the asteroid
     * @throws IllegalArgumentException if radius is negative
     */
    public Asteroid(int id, int radius, AsteroidSize asteroidSize) {
        super(id, radius);
        if (asteroidSize == null) {
            throw new IllegalArgumentException("Asteroid size must not be null");
        }
        this.asteroidSize = asteroidSize;
        this.mineralYield = asteroidSize.getBaseMineralYield();
        this.outpostSlots = asteroidSize.getBaseOutpostSlots();
        this.occupiedOutposts = new ArrayList<>();
        this.movementPenalty = asteroidSize.getMovementPenalty();
        this.parentBeltId = null;
        generate();
    }

    @Override
    protected String defaultName(int id) {
        return "Asteroid-" + id;
    }

    /**
     * @return the asteroid size classification
     */
    public AsteroidSize getAsteroidSize() {
        return asteroidSize;
    }

    /**
     * Sets the asteroid size classification.
     * Updates mineral yield, outpost slots, and movement penalty based on the new size.
     *
     * @param asteroidSize the new size classification
     */
    public void setAsteroidSize(AsteroidSize asteroidSize) {
        if (asteroidSize == null) {
            throw new IllegalArgumentException("Asteroid size must not be null");
        }
        this.asteroidSize = asteroidSize;
        this.mineralYield = asteroidSize.getBaseMineralYield();
        this.outpostSlots = asteroidSize.getBaseOutpostSlots();
        this.movementPenalty = asteroidSize.getMovementPenalty();
    }

    // ---- Resource properties ----

    /**
     * Returns the mineral yield per turn from this asteroid.
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
     * Returns the effective mineral yield, boosted by mining outposts.
     * Each mining outpost adds a 20% bonus to the base yield.
     *
     * @return the effective mineral yield per turn
     */
    public int getEffectiveMineralYield() {
        double bonus = occupiedOutposts.size() * 0.2;
        return (int) Math.round(mineralYield * (1.0 + bonus));
    }

    // ---- Outpost management ----

    /**
     * Returns the total number of outpost slots available.
     *
     * @return the number of outpost slots
     */
    public int getOutpostSlots() {
        return outpostSlots;
    }

    /**
     * Sets the total number of outpost slots.
     *
     * @param outpostSlots the number of slots (must be non-negative)
     * @throws IllegalArgumentException if slots are negative
     */
    public void setOutpostSlots(int outpostSlots) {
        if (outpostSlots < 0) {
            throw new IllegalArgumentException("Outpost slots must be non-negative: " + outpostSlots);
        }
        this.outpostSlots = outpostSlots;
    }

    /**
     * Returns the number of available (unoccupied) outpost slots.
     *
     * @return the number of available slots
     */
    public int getAvailableOutpostSlots() {
        return outpostSlots - occupiedOutposts.size();
    }

    /**
     * Checks if there is space for another outpost.
     *
     * @return true if at least one slot is available
     */
    public boolean hasAvailableOutpostSlot() {
        return occupiedOutposts.size() < outpostSlots;
    }

    /**
     * Adds an outpost to this asteroid.
     *
     * @param outpostName the name of the outpost
     * @return true if the outpost was added, false if no slots available
     * @throws IllegalArgumentException if outpostName is null or empty
     */
    public boolean addOutpost(String outpostName) {
        if (outpostName == null || outpostName.trim().isEmpty()) {
            throw new IllegalArgumentException("Outpost name must not be null or empty");
        }
        if (!hasAvailableOutpostSlot()) {
            return false;
        }
        return occupiedOutposts.add(outpostName);
    }

    /**
     * Removes an outpost from this asteroid.
     *
     * @param outpostName the name of the outpost to remove
     * @return true if the outpost was removed, false if not found
     */
    public boolean removeOutpost(String outpostName) {
        return occupiedOutposts.remove(outpostName);
    }

    /**
     * @return the list of occupied outpost names (unmodifiable)
     */
    public List<String> getOccupiedOutposts() {
        return Collections.unmodifiableList(occupiedOutposts);
    }

    /**
     * Clears all outposts from this asteroid.
     */
    public void clearOutposts() {
        occupiedOutposts.clear();
    }

    // ---- Hazard properties ----

    /**
     * Returns the movement penalty for ships navigating near this asteroid.
     * Ships moving through or near the asteroid's hex cells will have their
     * movement speed reduced by this percentage.
     *
     * @return the movement penalty (0.0 to 1.0)
     */
    public double getMovementPenalty() {
        return movementPenalty;
    }

    /**
     * Sets the movement penalty for ships navigating near this asteroid.
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
     * Calculates the effective movement speed for a ship passing through this asteroid's space.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the reduced movement speed
     */
    public double getEffectiveMovementSpeed(double baseSpeed) {
        return baseSpeed * (1.0 - movementPenalty);
    }

    // ---- Parent belt relationship ----

    /**
     * Returns the ID of the parent asteroid belt, if this asteroid belongs to one.
     *
     * @return the parent belt ID, or null if independent
     */
    public Integer getParentBeltId() {
        return parentBeltId;
    }

    /**
     * Sets the parent asteroid belt ID.
     *
     * @param parentBeltId the ID of the parent belt, or null to detach
     */
    public void setParentBeltId(Integer parentBeltId) {
        this.parentBeltId = parentBeltId;
    }

    /**
     * Checks if this asteroid belongs to a parent belt.
     *
     * @return true if the asteroid is part of a belt
     */
    public boolean belongsToBelt() {
        return parentBeltId != null;
    }

    // ---- Hex3DMap operations ----

    /**
     * Checks if the given coordinates are within this asteroid.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the hex is part of this asteroid
     */
    public boolean contains(int x, int y) {
        return getMap().contains(x, y);
    }

    /**
     * Returns all hex coordinates within this asteroid.
     *
     * @return collection of hex coordinates
     */
    public Collection<Hex3D> getAll() {
        return getMap().getAll();
    }

    /**
     * Returns the hex at the given coordinate.
     *
     * @param hex the hex coordinate
     * @return the hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        return getMap().get(hex);
    }

    /**
     * Returns neighbors of the given hex that are also within this asteroid.
     *
     * @param hex the center hex
     * @return list of neighboring hexes within this asteroid
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        return getMap().getNeighbors(hex);
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asteroid asteroid = (Asteroid) o;
        return id == asteroid.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Asteroid{id=%d, name='%s', size=%s, minerals=%d, outposts=%d/%d, movementPenalty=%.0f%%, strategic=%s}",
                id, name, asteroidSize, mineralYield, occupiedOutposts.size(), outpostSlots,
                movementPenalty * 100, hasStrategicDeposit ? strategicResource : "none");
    }
}
