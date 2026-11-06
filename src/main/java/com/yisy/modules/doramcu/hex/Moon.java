package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.Collection;

/**
 * A moon — a natural satellite orbiting a planet.
 * <p>
 * Moons are smaller celestial bodies that orbit planets rather than stars directly.
 * They provide minor resource deposits, can host research outposts, and benefit from
 * their parent planet's gravitational field for defensive bonuses.
 * <p>
 * Moons can have tidal effects on their parent planet, influencing population stats
 * and resource production. Large moons with high tidal strength can cause geological
 * activity that boosts mineral output but may reduce social stability.
 * <p>
 * Based on implementations in Stellaris, Galactic Civilizations IV, and FreeOrion.
 */
public class Moon extends CelestialBody {

    public static final int DEFAULT_RADIUS = 3;
    public static final int DEFAULT_MINERAL_YIELD = 2;
    public static final int DEFAULT_RESEARCH_YIELD = 1;
    public static final int DEFAULT_OUTPOST_SLOTS = 2;
    public static final double DEFAULT_TIDAL_STRENGTH = 0.1;
    public static final double DEFAULT_GRAVITY_DEFENSE_BONUS = 0.1;

    /**
     * Moon size classification affecting capacity and output.
     */
    public enum MoonSize {
        /**
         * Tiny moon — limited resources, single outpost.
         */
        TINY(1, 1, 0.5),

        /**
         * Small moon — modest resources, two outposts.
         */
        SMALL(2, 2, 1.0),

        /**
         * Medium moon — balanced resources and capacity.
         */
        MEDIUM(3, 2, 1.5),

        /**
         * Large moon — significant resources and strategic value.
         */
        LARGE(4, 3, 2.0),

        /**
         * Huge moon — major satellite with colonization potential.
         */
        HUGE(5, 4, 3.0);

        private final int baseMineralYield;
        private final int baseOutpostSlots;
        private final double tidalMultiplier;

        MoonSize(int baseMineralYield, int baseOutpostSlots, double tidalMultiplier) {
            this.baseMineralYield = baseMineralYield;
            this.baseOutpostSlots = baseOutpostSlots;
            this.tidalMultiplier = tidalMultiplier;
        }

        /**
         * @return the base mineral yield for this moon size
         */
        public int getBaseMineralYield() {
            return baseMineralYield;
        }

        /**
         * @return the base number of outpost slots for this moon size
         */
        public int getBaseOutpostSlots() {
            return baseOutpostSlots;
        }

        /**
         * @return the tidal effect multiplier
         */
        public double getTidalMultiplier() {
            return tidalMultiplier;
        }
    }

    private MoonSize moonSize;
    private int mineralYield;
    private int researchYield;
    private int outpostSlots;
    private final List<String> occupiedOutposts;
    private double tidalStrength;
    private double gravityDefenseBonus;
    private Integer parentPlanetId;

    /**
     * Creates a new moon with the given ID, default radius of 3, and MEDIUM size.
     *
     * @param id the unique identifier for this moon
     */
    public Moon(int id) {
        this(id, DEFAULT_RADIUS, MoonSize.MEDIUM);
    }

    /**
     * Creates a new moon with the given ID and custom radius, defaulting to MEDIUM size.
     *
     * @param id the unique identifier for this moon
     * @param radius the radius of the moon
     * @throws IllegalArgumentException if radius is negative
     */
    public Moon(int id, int radius) {
        this(id, radius, MoonSize.MEDIUM);
    }

    /**
     * Creates a new moon with the given ID, default radius, and moon size.
     * Mineral yield and outpost slots are derived from the moon size.
     *
     * @param id the unique identifier for this moon
     * @param moonSize the size classification of the moon
     */
    public Moon(int id, MoonSize moonSize) {
        this(id, DEFAULT_RADIUS, moonSize);
    }

    /**
     * Creates a new moon with the given ID, custom radius, and moon size.
     * Mineral yield and outpost slots are derived from the moon size.
     *
     * @param id the unique identifier for this moon
     * @param radius the radius of the moon
     * @param moonSize the size classification of the moon
     * @throws IllegalArgumentException if radius is negative
     */
    public Moon(int id, int radius, MoonSize moonSize) {
        super(id, radius);
        if (moonSize == null) {
            throw new IllegalArgumentException("Moon size must not be null");
        }
        this.moonSize = moonSize;
        this.mineralYield = moonSize.getBaseMineralYield();
        this.researchYield = DEFAULT_RESEARCH_YIELD;
        this.outpostSlots = moonSize.getBaseOutpostSlots();
        this.occupiedOutposts = new ArrayList<>();
        this.tidalStrength = DEFAULT_TIDAL_STRENGTH * moonSize.getTidalMultiplier();
        this.gravityDefenseBonus = DEFAULT_GRAVITY_DEFENSE_BONUS;
        this.parentPlanetId = null;
        generate();
    }

    @Override
    protected String defaultName(int id) {
        return "Moon-" + id;
    }

    /**
     * @return the moon size classification
     */
    public MoonSize getMoonSize() {
        return moonSize;
    }

    /**
     * Sets the moon size classification.
     * Updates mineral yield and outpost slots based on the new size.
     *
     * @param moonSize the new size classification
     */
    public void setMoonSize(MoonSize moonSize) {
        if (moonSize == null) {
            throw new IllegalArgumentException("Moon size must not be null");
        }
        this.moonSize = moonSize;
        this.mineralYield = moonSize.getBaseMineralYield();
        this.outpostSlots = moonSize.getBaseOutpostSlots();
        this.tidalStrength = DEFAULT_TIDAL_STRENGTH * moonSize.getTidalMultiplier();
    }

    // ---- Resource properties ----

    /**
     * Returns the mineral yield per turn from this moon.
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
     * Returns the research yield per turn from this moon.
     *
     * @return the research yield
     */
    public int getResearchYield() {
        return researchYield;
    }

    /**
     * Sets the research yield per turn.
     *
     * @param researchYield the research yield (must be non-negative)
     * @throws IllegalArgumentException if yield is negative
     */
    public void setResearchYield(int researchYield) {
        if (researchYield < 0) {
            throw new IllegalArgumentException("Research yield must be non-negative: " + researchYield);
        }
        this.researchYield = researchYield;
    }

    /**
     * Returns the effective mineral yield, boosted by mining outposts.
     * Each mining outpost adds a 10% bonus to the base yield.
     *
     * @return the effective mineral yield per turn
     */
    public int getEffectiveMineralYield() {
        double bonus = occupiedOutposts.size() * 0.1;
        return (int) Math.round(mineralYield * (1.0 + bonus));
    }

    /**
     * Returns the effective research yield, boosted by research outposts.
     * Each outpost adds a 15% bonus to the base research yield.
     *
     * @return the effective research yield per turn
     */
    public int getEffectiveResearchYield() {
        double bonus = occupiedOutposts.size() * 0.15;
        return (int) Math.round(researchYield * (1.0 + bonus));
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
     * Adds an outpost to this moon.
     *
     * @param outpostName the name of the outpost
     * @return true if the outpost was added, false if no slots available
     */
    public boolean addOutpost(String outpostName) {
        if (!hasAvailableOutpostSlot()) {
            return false;
        }
        return occupiedOutposts.add(outpostName);
    }

    /**
     * Removes an outpost from this moon.
     *
     * @param outpostName the name of the outpost to remove
     * @return true if the outpost was removed
     */
    public boolean removeOutpost(String outpostName) {
        return occupiedOutposts.remove(outpostName);
    }

    /**
     * Returns all occupied outpost names.
     *
     * @return unmodifiable list of outpost names
     */
    public List<String> getOccupiedOutposts() {
        return Collections.unmodifiableList(occupiedOutposts);
    }

    // ---- Tidal effects ----

    /**
     * Returns the tidal strength affecting the parent planet.
     * A value of 0.1 means the moon exerts a 10% tidal influence.
     * Higher values increase geological activity (mineral boost) but may
     * reduce social stability on the parent planet.
     *
     * @return the tidal strength (0.0 to 1.0)
     */
    public double getTidalStrength() {
        return tidalStrength;
    }

    /**
     * Sets the tidal strength.
     *
     * @param tidalStrength the strength (0.0 to 1.0)
     * @throws IllegalArgumentException if strength is out of range
     */
    public void setTidalStrength(double tidalStrength) {
        if (tidalStrength < 0.0 || tidalStrength > 1.0) {
            throw new IllegalArgumentException("Tidal strength must be between 0.0 and 1.0: " + tidalStrength);
        }
        this.tidalStrength = tidalStrength;
    }

    /**
     * Returns the mineral production bonus on the parent planet caused by tidal forces.
     * Tidal friction increases geological activity, boosting mineral output.
     *
     * @return the mineral bonus multiplier (e.g., 0.05 for 5% bonus)
     */
    public double getTidalMineralBonus() {
        return tidalStrength * 0.5;
    }

    /**
     * Returns the social stability reduction on the parent planet caused by tidal forces.
     * Strong tidal forces can cause earthquakes and volcanic activity.
     *
     * @return the stability reduction (e.g., 0.03 for 3% reduction)
     */
    public double getTidalStabilityReduction() {
        return tidalStrength * 0.3;
    }

    // ---- Gravity defense bonus ----

    /**
     * Returns the gravity defense bonus multiplier.
     * Ships defending near a moon benefit from the parent planet's gravity well,
     * making it harder for attackers to maneuver.
     * A value of 0.1 means defenders gain 10% damage reduction.
     *
     * @return the gravity defense bonus (0.0 to 1.0)
     */
    public double getGravityDefenseBonus() {
        return gravityDefenseBonus;
    }

    /**
     * Sets the gravity defense bonus multiplier.
     *
     * @param gravityDefenseBonus the bonus (0.0 to 1.0)
     * @throws IllegalArgumentException if bonus is out of range
     */
    public void setGravityDefenseBonus(double gravityDefenseBonus) {
        if (gravityDefenseBonus < 0.0 || gravityDefenseBonus > 1.0) {
            throw new IllegalArgumentException("Gravity defense bonus must be between 0.0 and 1.0: " + gravityDefenseBonus);
        }
        this.gravityDefenseBonus = gravityDefenseBonus;
    }

    /**
     * Returns the reduced damage taken by a defending fleet near this moon.
     *
     * @param rawDamage the incoming damage before defense bonus
     * @return the reduced damage after applying the gravity defense bonus
     */
    public int getDefendedDamage(int rawDamage) {
        return (int) Math.round(rawDamage * (1.0 - gravityDefenseBonus));
    }

    // ---- Parent planet relationship ----

    /**
     * Returns the ID of the parent planet this moon orbits, or null if unbound.
     *
     * @return the parent planet ID or null
     */
    public Integer getParentPlanetId() {
        return parentPlanetId;
    }

    /**
     * Binds this moon to a parent planet.
     *
     * @param planetId the ID of the parent planet
     */
    public void setParentPlanet(int planetId) {
        this.parentPlanetId = planetId;
    }

    /**
     * Unbinds this moon from its parent planet.
     */
    public void unbindFromPlanet() {
        this.parentPlanetId = null;
    }

    /**
     * Checks if this moon is bound to a parent planet.
     *
     * @return true if a parent planet is assigned
     */
    public boolean hasParentPlanet() {
        return parentPlanetId != null;
    }

    // ---- Hex operations ----

    /**
     * Checks if the given coordinates are within this moon.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the hex is part of this moon
     */
    public boolean contains(int x, int y) {
        return getMap().contains(x, y);
    }

    /**
     * Returns all hex coordinates within this moon.
     *
     * @return set of hex coordinates
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
     * Returns neighbors of the given hex that are also within this moon.
     *
     * @param hex the center hex
     * @return list of neighboring hexes within this moon
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        return getMap().getNeighbors(hex);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return String.format("Moon{name='%s', id=%d, size=%s, radius=%d, minerals=%d, research=%d, outposts=%d/%d, tidal=%.0f%%, defense=%.0f%%}",
                name, id, moonSize, radius, mineralYield, researchYield,
                occupiedOutposts.size(), outpostSlots,
                tidalStrength * 100, gravityDefenseBonus * 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Moon moon = (Moon) o;
        return id == moon.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
