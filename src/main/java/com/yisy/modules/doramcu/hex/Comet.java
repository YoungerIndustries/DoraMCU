package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.Collection;

/**
 * A comet — a temporary icy celestial body passing through a solar system.
 * <p>
 * Comets are transient celestial bodies that orbit stars in highly elliptical paths.
 * They deliver rare resource deposits when they pass through a solar system and can
 * provide temporary bonuses to nearby planets and stations.
 * <p>
 * Comets can be harvested for their volatile materials, provide research opportunities,
 * and may carry strategic resources. Their temporary nature makes them valuable but
 * fleeting opportunities for empires that can intercept and exploit them.
 * <p>
 * Based on implementations in Stellaris and other 4X space games.
 */
public class Comet extends CelestialBody {

    public static final int DEFAULT_RADIUS = 1;
    public static final int DEFAULT_RESOURCE_BONUS = 5;
    public static final int DEFAULT_DURATION_TURNS = 10;
    public static final double DEFAULT_RESEARCH_BONUS = 0.1;

    /**
     * Comet size classification affecting resource yield and duration.
     */
    public enum CometSize {
        /**
         * Tiny comet — minimal resources, short duration.
         */
        TINY(1, 3, 0.05),

        /**
         * Small comet — modest resources, moderate duration.
         */
        SMALL(2, 5, 0.1),

        /**
         * Medium comet — balanced resources and duration.
         */
        MEDIUM(5, 10, 0.15),

        /**
         * Large comet — significant resources, long duration.
         */
        LARGE(10, 15, 0.2),

        /**
         * Massive comet — major resource delivery, extended presence.
         */
        MASSIVE(20, 20, 0.25);

        private final int baseResourceBonus;
        private final int baseDurationTurns;
        private final double researchBonus;

        CometSize(int baseResourceBonus, int baseDurationTurns, double researchBonus) {
            this.baseResourceBonus = baseResourceBonus;
            this.baseDurationTurns = baseDurationTurns;
            this.researchBonus = researchBonus;
        }

        /**
         * @return the base resource bonus delivered by this comet size
         */
        public int getBaseResourceBonus() {
            return baseResourceBonus;
        }

        /**
         * @return the base duration in turns for this comet size
         */
        public int getBaseDurationTurns() {
            return baseDurationTurns;
        }

        /**
         * @return the research bonus multiplier for nearby stations
         */
        public double getResearchBonus() {
            return researchBonus;
        }
    }

    private CometSize cometSize;
    private int resourceBonus;
    private int durationTurns;
    private int remainingTurns;
    private double researchBonus;
    private boolean harvested;

    /**
     * Creates a new comet with the given ID, default radius of 1, and MEDIUM size.
     *
     * @param id the unique identifier for this comet
     */
    public Comet(int id) {
        this(id, DEFAULT_RADIUS, CometSize.MEDIUM);
    }

    /**
     * Creates a new comet with the given ID and custom radius, defaulting to MEDIUM size.
     *
     * @param id the unique identifier for this comet
     * @param radius the radius of the comet
     * @throws IllegalArgumentException if radius is negative
     */
    public Comet(int id, int radius) {
        this(id, radius, CometSize.MEDIUM);
    }

    /**
     * Creates a new comet with the given ID, default radius, and comet size.
     * Resource bonus and duration are derived from the comet size.
     *
     * @param id the unique identifier for this comet
     * @param cometSize the size classification of the comet
     */
    public Comet(int id, CometSize cometSize) {
        this(id, DEFAULT_RADIUS, cometSize);
    }

    /**
     * Creates a new comet with the given ID, custom radius, and comet size.
     * Resource bonus and duration are derived from the comet size.
     *
     * @param id the unique identifier for this comet
     * @param radius the radius of the comet
     * @param cometSize the size classification of the comet
     * @throws IllegalArgumentException if radius is negative
     */
    public Comet(int id, int radius, CometSize cometSize) {
        super(id, radius);
        if (cometSize == null) {
            throw new IllegalArgumentException("Comet size must not be null");
        }
        this.cometSize = cometSize;
        this.resourceBonus = cometSize.getBaseResourceBonus();
        this.durationTurns = cometSize.getBaseDurationTurns();
        this.remainingTurns = cometSize.getBaseDurationTurns();
        this.researchBonus = cometSize.getResearchBonus();
        this.harvested = false;
        generate();
    }

    @Override
    protected String defaultName(int id) {
        return "Comet-" + id;
    }

    /**
     * @return the comet size classification
     */
    public CometSize getCometSize() {
        return cometSize;
    }

    /**
     * Sets the comet size classification.
     * Updates resource bonus, duration, and research bonus based on the new size.
     *
     * @param cometSize the new size classification
     */
    public void setCometSize(CometSize cometSize) {
        if (cometSize == null) {
            throw new IllegalArgumentException("Comet size must not be null");
        }
        this.cometSize = cometSize;
        this.resourceBonus = cometSize.getBaseResourceBonus();
        this.durationTurns = cometSize.getBaseDurationTurns();
        this.remainingTurns = Math.min(this.remainingTurns, cometSize.getBaseDurationTurns());
        this.researchBonus = cometSize.getResearchBonus();
    }

    // ---- Resource properties ----

    /**
     * Returns the resource bonus delivered by this comet.
     *
     * @return the resource bonus amount
     */
    public int getResourceBonus() {
        return resourceBonus;
    }

    /**
     * Sets the resource bonus delivered by this comet.
     *
     * @param resourceBonus the bonus amount (must be non-negative)
     * @throws IllegalArgumentException if bonus is negative
     */
    public void setResourceBonus(int resourceBonus) {
        if (resourceBonus < 0) {
            throw new IllegalArgumentException("Resource bonus must be non-negative: " + resourceBonus);
        }
        this.resourceBonus = resourceBonus;
    }

// Bonus resources removed – use ResourcePipelineManager for mining/processing

    // ---- Duration properties ----

    /**
     * Returns the total duration in turns for this comet's presence.
     *
     * @return the total duration in turns
     */
    public int getDurationTurns() {
        return durationTurns;
    }

    /**
     * Sets the total duration in turns.
     *
     * @param durationTurns the duration (must be positive)
     * @throws IllegalArgumentException if duration is not positive
     */
    public void setDurationTurns(int durationTurns) {
        if (durationTurns <= 0) {
            throw new IllegalArgumentException("Duration turns must be positive: " + durationTurns);
        }
        this.durationTurns = durationTurns;
        this.remainingTurns = Math.min(this.remainingTurns, durationTurns);
    }

    /**
     * Returns the remaining turns before the comet departs.
     *
     * @return the remaining turns
     */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    /**
     * Advances the comet by one turn, decreasing remaining turns.
     *
     * @return true if the comet is still present after this turn, false if it has departed
     */
    public boolean advanceTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
        return remainingTurns > 0;
    }

    /**
     * Checks if the comet has departed (remaining turns reached 0).
     *
     * @return true if the comet has departed
     */
    public boolean hasDeparted() {
        return remainingTurns <= 0;
    }

    /**
     * Resets the comet's duration, allowing it to remain for the full duration again.
     * Can be used when the comet's orbit is extended by special events.
     */
    public void resetDuration() {
        this.remainingTurns = this.durationTurns;
    }

    // ---- Research properties ----

    /**
     * Returns the research bonus multiplier for nearby stations.
     *
     * @return the research bonus multiplier
     */
    public double getResearchBonus() {
        return researchBonus;
    }

    /**
     * Sets the research bonus multiplier.
     *
     * @param researchBonus the bonus multiplier (must be non-negative)
     * @throws IllegalArgumentException if bonus is negative
     */
    public void setResearchBonus(double researchBonus) {
        if (researchBonus < 0) {
            throw new IllegalArgumentException("Research bonus must be non-negative: " + researchBonus);
        }
        this.researchBonus = researchBonus;
    }

    /**
     * Returns the effective research bonus for a station with the given base research output.
     *
     * @param baseResearch the base research output of the station
     * @return the effective research output with comet bonus applied
     */
    public int getEffectiveResearchBonus(int baseResearch) {
        if (hasDeparted() || harvested) {
            return baseResearch;
        }
        return (int) Math.round(baseResearch * (1.0 + researchBonus));
    }

    // ---- Harvest properties ----

    /**
     * Checks if the comet has been harvested.
     *
     * @return true if the comet has been harvested
     */
    public boolean isHarvested() {
        return harvested;
    }

    /**
     * Harvests the comet, collecting its resource bonus and marking it as harvested.
     * Once harvested, the comet no longer provides ongoing bonuses.
     *
     * @return the resource bonus collected
     */
    public int harvest() {
        this.harvested = true;
        return resourceBonus;
    }

    // ---- Hex3DMap operations ----

    /**
     * @return all hex cells occupied by this comet (unmodifiable collection)
     */
    public Collection<Hex3D> getAll() {
        return getMap().getAll();
    }

    /**
     * Checks if the given local coordinates are part of this comet.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the position is occupied by the comet
     */
    public boolean contains(int x, int y) {
        return getMap().contains(x, y);
    }

    /**
     * Checks if the given hex is part of this comet.
     *
     * @param hex the hex position
     * @return true if the hex is occupied by the comet
     */
    public boolean contains(Hex3D hex) {
        return getMap().get(hex) != null;
    }

    /**
     * @return the neighbors of the given hex within this comet
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        return getMap().getNeighbors(hex);
    }

    /**
     * @return the center hex of the comet (0, 0, 0)
     */
    public Hex3D getCenter() {
        return new Hex3D(0, 0, 0);
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comet comet = (Comet) o;
        return id == comet.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Comet{id=%d, name='%s', size=%s, resourceBonus=%d, remainingTurns=%d/%d, harvested=%s, strategic=%s}",
                id, name, cometSize, resourceBonus, remainingTurns, durationTurns, harvested,
                hasStrategicDeposit ? strategicResource : "none");
    }
}
