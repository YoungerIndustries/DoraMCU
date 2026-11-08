package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an empire in the 4X game — a collection of planets, fleets, and resources
 * controlled by a single player.
 * <p>
 * Each empire is headed by one player (the ruler) and owns a set of habitable planets.
 * The empire is organized into three main components:
 * </p>
 * <ul>
 *   <li>{@link EmpireArmy} — ground forces for planetary invasion and defense</li>
 *   <li>{@link EmpireNavy} — space fleets for orbital combat and exploration</li>
 *   <li>{@link EmpireFacilities} — ground infrastructure and space stations</li>
 * </ul>
 * <p>
 * Based on Stellaris empire mechanics where each empire controls a territory of worlds.
 * </p>
 */
public class Empire {

    /** Default probability that a habitable planet has a shipyard facility. */
    public static final double DEFAULT_SHIPYARD_CHANCE = 0.4;

    /** Default probability that a habitable planet has an industrial facility. */
    public static final double DEFAULT_INDUSTRIAL_CHANCE = 0.5;

    /** Default minimum industrial level for a planet with industrial facilities. */
    public static final int DEFAULT_MIN_INDUSTRIAL_LEVEL = 1;

    /** Default maximum industrial level for a planet with industrial facilities. */
    public static final int DEFAULT_MAX_INDUSTRIAL_LEVEL = 3;

    private final int id;
    private final String name;
    private final int rulerPlayerId;
    private final Set<Integer> ownedPlanetIds;
    private final Map<Integer, Integer> planetIndustrialLevels;
    /** Planet ID of the empire capital. Null if no capital is established. */
    private Integer capitalPlanetId;

    // ---- Empire Components ----

    /** Ground forces: army units for planetary operations. */
    private final EmpireArmy army;

    /** Space forces: fleets and ships for orbital operations. */
    private final EmpireNavy navy;

    /** Infrastructure: planet facilities and space stations. */
    private final EmpireFacilities facilities;

    /**
     * Creates a new empire with the given ID, name, and ruler player ID.
     *
     * @param id the unique identifier for this empire
     * @param name the display name of the empire
     * @param rulerPlayerId the player ID of the empire ruler
     */
    public Empire(int id, String name, int rulerPlayerId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Empire name must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.rulerPlayerId = rulerPlayerId;
        this.ownedPlanetIds = new HashSet<>();
        this.planetIndustrialLevels = new HashMap<>();
        this.army = new EmpireArmy(id);
        this.navy = new EmpireNavy(id);
        this.facilities = new EmpireFacilities(id);
    }

    /** Returns the empire ID. */
    public int getId() {
        return id;
    }

    /** Returns the empire name. */
    public String getName() {
        return name;
    }

    /** Returns the player ID of the empire ruler. */
    public int getRulerPlayerId() {
        return rulerPlayerId;
    }

    /**
     * Returns the IDs of planets owned by this empire.
     *
     * @return unmodifiable set of planet IDs
     */
    public Set<Integer> getOwnedPlanetIds() {
        return Collections.unmodifiableSet(ownedPlanetIds);
    }

    /** Returns the number of planets owned by this empire. */
    public int getPlanetCount() {
        return ownedPlanetIds.size();
    }

    /**
     * Adds a planet to this empire's ownership.
     *
     * @param planetId the planet ID to add
     * @return true if the planet was added
     */
    public boolean claimPlanet(int planetId) {
        return ownedPlanetIds.add(planetId);
    }

    /**
     * Removes a planet from this empire's ownership.
     *
     * @param planetId the planet ID to remove
     * @return true if the planet was removed
     */
    public boolean releasePlanet(int planetId) {
        boolean removed = ownedPlanetIds.remove(planetId);
        planetIndustrialLevels.remove(planetId);
        return removed;
    }

    /**
     * Checks if this empire owns the given planet.
     *
     * @param planetId the planet ID to check
     * @return true if the empire owns the planet
     */
    public boolean ownsPlanet(int planetId) {
        return ownedPlanetIds.contains(planetId);
    }

    /**
     * Sets the industrial level for a planet.
     *
     * @param planetId the planet ID
     * @param level the industrial level (1-10)
     */
    public void setPlanetIndustrialLevel(int planetId, int level) {
        if (!ownedPlanetIds.contains(planetId)) {
            throw new IllegalArgumentException("Planet " + planetId + " is not owned by empire " + name);
        }
        if (level < 0) {
            throw new IllegalArgumentException("Industrial level must be non-negative");
        }
        if (level == 0) {
            planetIndustrialLevels.remove(planetId);
        } else {
            planetIndustrialLevels.put(planetId, level);
        }
    }

    /**
     * Returns the industrial level of a planet.
     *
     * @param planetId the planet ID
     * @return the industrial level, or 0 if no industrial facilities
     */
    public int getPlanetIndustrialLevel(int planetId) {
        return planetIndustrialLevels.getOrDefault(planetId, 0);
    }

    /**
     * Returns a map of all planet industrial levels.
     *
     * @return unmodifiable map of planet ID to industrial level
     */
    public Map<Integer, Integer> getPlanetIndustrialLevels() {
        return Collections.unmodifiableMap(planetIndustrialLevels);
    }

    /**
     * Returns the total industrial capacity of the empire.
     *
     * @return sum of all planet industrial levels
     */
    public int getTotalIndustrialCapacity() {
        return planetIndustrialLevels.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Returns the number of planets with industrial facilities.
     *
     * @return count of planets with industrial level > 0
     */
    public int getIndustrialPlanetCount() {
        return planetIndustrialLevels.size();
    }

    /**
     * Returns the planet ID of the empire capital.
     *
     * @return the capital planet ID, or null if no capital is established
     */
    public Integer getCapitalPlanetId() {
        return capitalPlanetId;
    }

    /**
     * Sets the capital planet for this empire.
     * The planet must be owned by this empire.
     *
     * @param planetId the planet ID to set as capital
     * @throws IllegalArgumentException if the planet is not owned by this empire
     */
    public void setCapitalPlanet(int planetId) {
        if (!ownedPlanetIds.contains(planetId)) {
            throw new IllegalArgumentException("Planet " + planetId + " is not owned by empire " + name);
        }
        this.capitalPlanetId = planetId;
    }

    /**
     * Checks if this empire has a capital planet established.
     *
     * @return true if a capital planet is set
     */
    public boolean hasCapital() {
        return capitalPlanetId != null;
    }

    /**
     * Assigns facilities to a newly claimed planet based on probability.
     * This is called during galaxy generation to determine if a planet
     * has shipyard and/or industrial facilities.
     *
     * @param planet the planet to assign facilities to
     * @param random the random number generator
     * @param shipyardChance probability of having a shipyard
     * @param industrialChance probability of having industrial facilities
     * @param minIndustrialLevel minimum industrial level
     * @param maxIndustrialLevel maximum industrial level
     */
    public void assignPlanetFacilities(Planet planet, Random random,
                                        double shipyardChance, double industrialChance,
                                        int minIndustrialLevel, int maxIndustrialLevel) {
        int planetId = planet.getId();
        if (!claimPlanet(planetId)) {
            return; // already owned
        }

        // Determine if planet has a shipyard
        if (random.nextDouble() < shipyardChance) {
            planet.addStartingFacility(StartingFacility.SHIPYARD);
        }

        // Determine if planet has industrial facilities
        if (random.nextDouble() < industrialChance) {
            int level = minIndustrialLevel + random.nextInt(maxIndustrialLevel - minIndustrialLevel + 1);
            planet.addStartingFacility(StartingFacility.MINING_FACILITY);
            setPlanetIndustrialLevel(planetId, level);
        }
    }

    @Override
    public String toString() {
        return String.format("Empire{id=%d, name='%s', ruler=%d, planets=%d, industrialCapacity=%d, army=%s, navy=%s, facilities=%s}",
                id, name, rulerPlayerId, ownedPlanetIds.size(), getTotalIndustrialCapacity(), army, navy, facilities);
    }

    // ---- Component Accessors ----

    /**
     * Returns the army component managing ground forces.
     *
     * @return the empire's army
     */
    public EmpireArmy getArmy() {
        return army;
    }

    /**
     * Returns the navy component managing space fleets.
     *
     * @return the empire's navy
     */
    public EmpireNavy getNavy() {
        return navy;
    }

    /**
     * Returns the facilities component managing infrastructure.
     *
     * @return the empire's facilities
     */
    public EmpireFacilities getFacilities() {
        return facilities;
    }

    // ---- End Turn Processing ----

    /**
     * Processes end-of-turn effects for all empire components.
     */
    public void endTurn() {
        army.endTurn();
        navy.endTurn();
        facilities.endTurn();
    }

    // ---- Ground Force & Mercenary Tracking ----

    /**
     * Returns the total ground force strength of this empire.
     * <p>
     * Combines regular army units and mercenary units.
     * </p>
     *
     * @return total ground strength
     */
    public int getGroundForceStrength() {
        return army.getTotalGroundStrength();
    }

    /**
     * Returns the total number of mercenary units in this empire.
     *
     * @return mercenary unit count
     */
    public int getMercenaryUnitCount() {
        return army.getMercenaryUnitCount();
    }

    /**
     * Returns the total number of regular army units in this empire.
     *
     * @return army unit count
     */
    public int getArmyUnitCount() {
        return army.getUnitCount();
    }

    /**
     * Returns the total offense value of this empire's army.
     *
     * @return total offense
     */
    public int getArmyOffense() {
        return army.getTotalOffense();
    }

    /**
     * Returns the total defense value of this empire's army.
     *
     * @return total defense
     */
    public int getArmyDefense() {
        return army.getTotalDefense();
    }
}
