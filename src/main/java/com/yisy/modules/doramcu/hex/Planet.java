package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.TradeCode;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A planet in the game world, combining 4X game statistics with 3D hex geometry.
 * <p>
 * This class serves as a thin coordinator that delegates to:
 * <ul>
 *   <li>{@link PlanetGeometry} — 3D hex sphere, layers (core, mantle, crust, atmosphere, moon)</li>
 *   <li>{@link PlanetDefense} — Stationed fleets, defense bonuses</li>
 * </ul>
 * <p>
 * The Planet class itself manages 4X game statistics: resource production, population stats,
 * strategic deposits, starting facilities, and empire ownership.
 */
public class Planet extends CelestialBody {

    private final PlanetGeometry geometry;
    private final PlanetDefense defense;

    // ---- 4X Game Statistics ----

    private PlanetSize size;
    private MineralQuality mineralQuality;
    private final Map<PopulationAttribute, Integer> populationStats;
    private final Set<StrategicResource> strategicDeposits;
    private int currentPopulation;
    private String name;
    private final Set<StartingFacility> startingFacilities;

    // ---- Empire Ownership ----

    /** Empire ID that owns this planet. Null if independent. */
    private Integer ownerEmpireId;
    /** True if this planet is independent (not owned by any empire). */
    private boolean isIndependent = true;
    /** Economic level of the planet. Higher levels produce more resources. */
    private int economicLevel = 0;
    /** StarBase orbiting this planet. Null if none. */
    private StarBase orbitingStarBase;
    /** True if this planet is the capital of its empire. */
    private boolean isCapital = false;

    // ---- Traveller UWP (Universal World Profile) fields ----

    /** Atmosphere value (0-10, where 10 = 'A' = none). Null if not set. */
    private Integer atmosphere;
    /** Gravity value (0-10, where 10 = 'A' = none). Null if not set. */
    private Integer gravity;
    /** Starport class (0-10, where 0 = XL, 10 = 'A' = none). Null if not set. */
    private Integer starport;
    /** Population level (0-10, where 0 = none, 10 = 'A' = 1+ trillion). Null if not set. */
    private Integer populationLevel;
    /** Government type (0-10, where 10 = 'A' = none). Null if not set. */
    private Integer governmentType;
    /** Law level (0-10, where 0 = 'A' = anarchy, 10 = 'X' = total control). Null if not set. */
    private Integer lawLevel;
    /** Trade codes applied to this planet. */
    private final Set<TradeCode> tradeCodes = new HashSet<>();

    /**
     * Creates a new planet with the given ID and default geometry.
     *
     * @param id the unique identifier for this planet
     */
    public Planet(int id) {
        this(id, PlanetGeometry.DEFAULT_RADIUS, PlanetGeometry.DEFAULT_ATMOSPHERE_THICKNESS,
                PlanetGeometry.DEFAULT_MOON_DISTANCE, PlanetGeometry.DEFAULT_MOON_RADIUS);
    }

    /**
     * Creates a new planet with the given ID and custom radius.
     *
     * @param id the unique identifier for this planet
     * @param radius the radius of the planet (0 for a single center hex)
     */
    public Planet(int id, int radius) {
        this(id, radius, PlanetGeometry.DEFAULT_ATMOSPHERE_THICKNESS,
                PlanetGeometry.DEFAULT_MOON_DISTANCE, PlanetGeometry.DEFAULT_MOON_RADIUS);
    }

    /**
     * Creates a new planet with the given ID, custom radius, and atmosphere thickness.
     */
    public Planet(int id, int radius, int atmosphereThickness) {
        this(id, radius, atmosphereThickness, PlanetGeometry.DEFAULT_MOON_DISTANCE, PlanetGeometry.DEFAULT_MOON_RADIUS);
    }

    /**
     * Creates a new planet with the given ID and planet size classification.
     * Mineral quality defaults to NORMAL.
     *
     * @param id the unique identifier for this planet
     * @param size the size classification of the planet
     */
    public Planet(int id, PlanetSize size) {
        this(id, size, MineralQuality.NORMAL);
    }

    /**
     * Creates a new planet with the given ID, planet size classification, and mineral quality.
     *
     * @param id the unique identifier for this planet
     * @param size the size classification of the planet
     * @param mineralQuality the mineral quality of the planet
     */
    public Planet(int id, PlanetSize size, MineralQuality mineralQuality) {
        if (size == null) {
            throw new IllegalArgumentException("Planet size must not be null");
        }
        if (mineralQuality == null) {
            throw new IllegalArgumentException("Mineral quality must not be null");
        }
        super(id, size.getBaseRadius());
        this.geometry = new PlanetGeometry(size.getBaseRadius(), PlanetGeometry.DEFAULT_ATMOSPHERE_THICKNESS,
                PlanetGeometry.DEFAULT_MOON_DISTANCE, PlanetGeometry.DEFAULT_MOON_RADIUS);
        this.defense = new PlanetDefense(this);
        this.populationStats = new EnumMap<>(PopulationAttribute.class);
        this.strategicDeposits = new HashSet<>();
        this.startingFacilities = new HashSet<>();
        this.size = size;
        this.mineralQuality = mineralQuality;
        this.name = "Planet-" + id;
        initializeDefaultResources();
    }

    /**
     * Creates a new planet with the given ID and custom geometry parameters.
     *
     * @param id the unique identifier for this planet
     * @param radius the radius of the planet
     * @param atmosphereThickness the thickness of the atmosphere layer
     * @param moonDistance the orbit distance of the moon (0 for no moon)
     * @param moonRadius the radius of the moon (0 for no moon)
     */
    public Planet(int id, int radius, int atmosphereThickness, int moonDistance, int moonRadius) {
        super(id, radius);
        this.geometry = new PlanetGeometry(radius, atmosphereThickness, moonDistance, moonRadius);
        this.defense = new PlanetDefense(this);
        this.populationStats = new EnumMap<>(PopulationAttribute.class);
        this.strategicDeposits = new HashSet<>();
        this.startingFacilities = new HashSet<>();
        this.size = PlanetSize.MEDIUM;
        this.mineralQuality = MineralQuality.NORMAL;
        this.name = "Planet-" + id;
        initializeDefaultResources();
    }

    /**
     * Initializes default resource production values based on planet size.
     * <p>
     * Base production scales with planet size: larger planets produce more resources.
     * </p>
     */
    private void initializeDefaultResources() {
        // Default balanced population stats
        populationStats.put(PopulationAttribute.SOCIAL, 5);
        populationStats.put(PopulationAttribute.INTELLIGENCE, 5);
        populationStats.put(PopulationAttribute.DILIGENCE, 5);
        populationStats.put(PopulationAttribute.RESOLVE, 5);
        currentPopulation = 1;
    }

    @Override
    protected String defaultName(int id) {
        return "Planet-" + id;
    }

    // ---- Accessors ----

    /**
     * Returns the planet's geometry for hex map operations.
     *
     * @return the geometry object
     */
    public PlanetGeometry getGeometry() {
        return geometry;
    }

    /**
     * Returns the planet's defense for fleet management.
     *
     * @return the defense object
     */
    public PlanetDefense getDefense() {
        return defense;
    }

    // ---- Delegated geometry accessors (backward compatibility) ----

    @Override
    public int getRadius() { return geometry.getRadius(); }
    @Override
    public Hex3DMap getMap() { return geometry.getMap(); }
    public int getAtmosphereThickness() { return geometry.getAtmosphereThickness(); }
    public int getTotalRadius() { return geometry.getTotalRadius(); }
    public int getMoonDistance() { return geometry.getMoonDistance(); }
    public int getMoonRadius() { return geometry.getMoonRadius(); }
    public boolean hasMoon() { return geometry.hasMoon(); }
    @Override
    public int size() { return geometry.size(); }

    // ---- 4X Game Statistics Accessors ----

    /**
     * Returns the display name of this planet.
     *
     * @return the planet name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this planet.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the size category of this planet.
     *
     * @return the planet size
     */
    public PlanetSize getPlanetSize() {
        return size;
    }

    /**
     * Sets the size category of this planet and recalculates resource production.
     *
     * @param size the new planet size
     */
    public void setPlanetSize(PlanetSize size) {
        this.size = size;
        initializeDefaultResources();
    }

    /**
     * Returns the mineral quality of this planet.
     *
     * @return the mineral quality
     */
    public MineralQuality getMineralQuality() {
        return mineralQuality;
    }

    /**
     * Sets the mineral quality of this planet.
     *
     * @param mineralQuality the new mineral quality
     */
    public void setMineralQuality(MineralQuality mineralQuality) {
        this.mineralQuality = mineralQuality;
    }

// Flat resource production removed – use ResourcePipelineManager for mining/processing

    /**
     * Returns the population stat value for a given attribute.
     *
     * @param attribute the population attribute to query
     * @return the stat value (1-10 scale)
     */
    public int getPopulationStat(PopulationAttribute attribute) {
        return populationStats.getOrDefault(attribute, 0);
    }

    /**
     * Sets the population stat value for a given attribute.
     *
     * @param attribute the population attribute
     * @param value the stat value (1-10 scale)
     */
    public void setPopulationStat(PopulationAttribute attribute, int value) {
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException("Population stat must be between 1 and 10: " + value);
        }
        populationStats.put(attribute, value);
    }

    /**
     * Returns all population stats.
     *
     * @return unmodifiable map of attributes to stat values
     */
    public Map<PopulationAttribute, Integer> getAllPopulationStats() {
        return Map.copyOf(populationStats);
    }

    /**
     * Returns the set of strategic resource deposits on this planet.
     *
     * @return unmodifiable set of strategic resources
     */
    public Set<StrategicResource> getStrategicDeposits() {
        return Set.copyOf(strategicDeposits);
    }

    /**
     * Adds a strategic resource deposit to this planet.
     *
     * @param resource the strategic resource to add
     * @return true if the set was changed
     */
    public boolean addStrategicDeposit(StrategicResource resource) {
        return strategicDeposits.add(resource);
    }

    /**
     * Removes a strategic resource deposit from this planet.
     *
     * @param resource the strategic resource to remove
     * @return true if the resource was present and removed
     */
    public boolean removeStrategicDeposit(StrategicResource resource) {
        return strategicDeposits.remove(resource);
    }

    /**
     * Checks if this planet has a specific strategic resource deposit.
     *
     * @param resource the strategic resource to check
     * @return true if the planet has this strategic resource
     */
    public boolean hasStrategicResource(StrategicResource resource) {
        return strategicDeposits.contains(resource);
    }

    /**
     * Returns the current population of this planet.
     *
     * @return the current population count
     */
    public int getCurrentPopulation() {
        return currentPopulation;
    }

    /**
     * Sets the current population of this planet.
     *
     * @param population the new population count
     * @throws IllegalArgumentException if population exceeds max or is negative
     */
    public void setCurrentPopulation(int population) {
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative: " + population);
        }
        if (population > size.getMaxPopulation()) {
            throw new IllegalArgumentException("Population " + population + " exceeds max capacity " + size.getMaxPopulation());
        }
        this.currentPopulation = population;
    }

    /**
     * Returns the maximum population capacity based on planet size.
     *
     * @return the maximum population
     */
    public int getMaxPopulation() {
        return size.getMaxPopulation();
    }

    /**
     * Returns the population density ratio (current / max).
     *
     * @return ratio between 0.0 and 1.0
     */
    public double getPopulationDensity() {
        return (double) currentPopulation / size.getMaxPopulation();
    }

    /**
     * Returns the military production output, based on mineral quality.
     *
     * @return effective military production per turn
     */
    public int getMilitaryProduction() {
        return (int) (mineralQuality.getProductionBonus() * 10);
    }

    // ---- Starting Facilities ----

    /**
     * Returns the set of starting facilities built on this planet.
     *
     * @return unmodifiable set of starting facilities
     */
    public Set<StartingFacility> getStartingFacilities() {
        return Set.copyOf(startingFacilities);
    }

    /**
     * Adds a starting facility to this planet.
     *
     * @param facility the facility to add
     * @return true if the set was changed
     */
    public boolean addStartingFacility(StartingFacility facility) {
        return startingFacilities.add(facility);
    }

    /**
     * Removes a starting facility from this planet.
     *
     * @param facility the facility to remove
     * @return true if the facility was present and removed
     */
    public boolean removeStartingFacility(StartingFacility facility) {
        return startingFacilities.remove(facility);
    }

    /**
     * Checks if this planet has a specific starting facility.
     *
     * @param facility the facility to check
     * @return true if the planet has this facility
     */
    public boolean hasStartingFacility(StartingFacility facility) {
        return startingFacilities.contains(facility);
    }

    // getFacilityBonus removed – flat resources eliminated

    /**
     * Returns the total defense bonus from all starting facilities.
     *
     * @return the total defense bonus
     */
    public int getFacilityDefenseBonus() {
        return startingFacilities.stream()
                .mapToInt(StartingFacility::getDefenseBonus)
                .sum();
    }

    // getEffectiveProductionWithFacilities removed – flat resources eliminated

    /**
     * Returns the ship build time reduction from starting facilities.
     * <p>
     * Each Shipyard reduces build time by 1 turn.
     * </p>
     *
     * @return the number of turns reduced
     */
    /**
     * Returns the ship build time reduction from facilities.
     *
     * @return the number of turns reduced
     */
    public int getShipBuildTimeReduction() {
        return startingFacilities.contains(StartingFacility.SHIPYARD) ? 1 : 0;
    }

    // ---- Pipeline Bonuses ----

    /**
     * Returns the mining production bonus multiplier from planetary facilities.
     * Mining facilities double mining output on this planet.
     *
     * @return the mining bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getMiningBonus() {
        return startingFacilities.contains(StartingFacility.MINING_FACILITY) ? 2 : 1;
    }

    /**
     * Returns the processing speed bonus multiplier from planetary facilities.
     * Research labs double processing speed.
     *
     * @return the processing bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getProcessingBonus() {
        return startingFacilities.contains(StartingFacility.RESEARCH_LAB) ? 2 : 1;
    }

    /**
     * Returns the assembly speed bonus multiplier from planetary facilities.
     * Shipyards double assembly speed.
     *
     * @return the assembly bonus multiplier (1 = no bonus, 2 = double)
     */
    public int getAssemblyBonus() {
        return startingFacilities.contains(StartingFacility.SHIPYARD) ? 2 : 1;
    }

    // ---- Empire Ownership ----

    /**
     * Returns the empire ID that owns this planet.
     *
     * @return the owner empire ID, or null if independent
     */
    public Integer getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /**
     * Sets the empire that owns this planet.
     *
     * @param empireId the empire ID, or null to make independent
     */
    public void setOwnerEmpireId(Integer empireId) {
        this.ownerEmpireId = empireId;
        this.isIndependent = (empireId == null);
    }

    /**
     * Returns true if this planet is independent (not owned by any empire).
     *
     * @return true if independent
     */
    public boolean isIndependent() {
        return isIndependent;
    }

    /**
     * Returns the economic level of this planet.
     * Higher economic levels increase resource production.
     *
     * @return the economic level (0 or greater)
     */
    public int getEconomicLevel() {
        return economicLevel;
    }

    /**
     * Sets the economic level of this planet.
     *
     * @param economicLevel the economic level (must be >= 0)
     * @throws IllegalArgumentException if level is negative
     */
    public void setEconomicLevel(int economicLevel) {
        if (economicLevel < 0) {
            throw new IllegalArgumentException("Economic level must be non-negative");
        }
        this.economicLevel = economicLevel;
    }

    /**
     * Increases the economic level by the given amount.
     *
     * @param amount the amount to increase by (must be >= 0)
     */
    public void increaseEconomicLevel(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        this.economicLevel += amount;
    }

    /**
     * Returns the StarBase orbiting this planet.
     *
     * @return the orbiting StarBase, or null if none
     */
    public StarBase getOrbitingStarBase() {
        return orbitingStarBase;
    }

    /**
     * Sets the StarBase orbiting this planet.
     *
     * @param starBase the StarBase, or null to remove
     */
    public void setOrbitingStarBase(StarBase starBase) {
        this.orbitingStarBase = starBase;
    }

    /**
     * Checks if this planet is the capital of its empire.
     *
     * @return true if this is a capital planet
     */
    public boolean isCapital() {
        return isCapital;
    }

    /**
     * Sets whether this planet is the capital of its empire.
     *
     * @param isCapital true if this is a capital planet
     */
    public void setCapital(boolean isCapital) {
        this.isCapital = isCapital;
    }

    /**
     * Returns true if this planet is owned by an empire.
     *
     * @return true if owned
     */
    public boolean isOwned() {
        return !isIndependent;
    }

    // ---- Delegated hex operations (backward compatibility) ----

    public Hex3D get(int x, int y) { return geometry.get(x, y); }
    public Hex3D get(Hex3D hex) { return geometry.get(hex); }
    public boolean contains(int x, int y) { return geometry.contains(x, y); }
    public boolean contains(Hex3D hex) { return geometry.contains(hex); }
    public Collection<Hex3D> getAll() { return geometry.getAll(); }
    public List<Hex3D> findByState(HexState state) { return geometry.findByState(state); }
    public List<Hex3D> getNeighbors(Hex3D hex) { return geometry.getNeighbors(hex); }

    // ---- Delegated occupancy statistics (backward compatibility) ----

    public int getOccupiedCount() { return geometry.getOccupiedCount(); }
    public int getEmptyCount() { return geometry.getEmptyCount(); }
    public int getBlockedCount() { return geometry.getBlockedCount(); }
    public int getSelectedCount() { return geometry.getSelectedCount(); }
    public double getOccupancyRatio() { return geometry.getOccupancyRatio(); }

    // ---- Delegated geological layers (backward compatibility) ----

    public int getCoreRadius() { return geometry.getCoreRadius(); }
    public int getMantleOuterRadius() { return geometry.getMantleOuterRadius(); }
    public List<Hex3D> getCore() { return geometry.getCore(); }
    public List<Hex3D> getMantle() { return geometry.getMantle(); }
    public List<Hex3D> getCrust() { return geometry.getCrust(); }
    public List<Hex3D> getAtmosphere() { return geometry.getAtmosphere(); }
    public Hex3D getMoonCenter() { return geometry.getMoonCenter(); }
    public List<Hex3D> getMoon() { return geometry.getMoon(); }
    public List<Hex3D> getSurface() { return geometry.getSurface(); }
    public String getLayerName(Hex3D hex) { return geometry.getLayerName(hex); }
    public int[] getLayerSizes() { return geometry.getLayerSizes(); }

    // ---- Delegated fleet management (backward compatibility) ----

    public List<Fleet> getStationedFleets() { return defense.getStationedFleets(); }
    public int getStationedFleetCount() { return defense.getStationedFleetCount(); }
    public boolean stationFleet(Fleet fleet) { return defense.stationFleet(fleet); }
    public boolean removeFleet(Fleet fleet) { return defense.removeFleet(fleet); }
    public boolean hasStationedFleet(Fleet fleet) { return defense.hasStationedFleet(fleet); }
    public void clearStationedFleets() { defense.clearStationedFleets(); }
    public double getDefenseBonus() { return defense.getDefenseBonus(); }
    public int getStationedFleetDamage() { return defense.getStationedFleetDamage(); }
    public int getStationedFleetShields() { return defense.getStationedFleetShields(); }
    public int getStationedFleetHull() { return defense.getStationedFleetHull(); }
    public int getStationedShipCount() { return defense.getStationedShipCount(); }
    public void repairStationedFleets() { defense.repairStationedFleets(); }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Planet planet = (Planet) o;
        return id == planet.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // ========================================================================
    // Traveller UWP (Universal World Profile) accessors
    // ========================================================================

    /** Returns the UWP atmosphere value (0-10). Null if not set. */
    public Integer getUwpAtmosphere() {
        return atmosphere;
    }

    /** Sets the UWP atmosphere value (0-10, where 10 = 'A' = none). */
    public void setUwpAtmosphere(Integer atmosphere) {
        this.atmosphere = atmosphere;
    }

    /** Returns the UWP gravity value (0-10). Null if not set. */
    public Integer getUwpGravity() {
        return gravity;
    }

    /** Sets the UWP gravity value (0-10, where 10 = 'A' = none). */
    public void setUwpGravity(Integer gravity) {
        this.gravity = gravity;
    }

    /** Returns the UWP starport class (0-10). Null if not set. */
    public Integer getUwpStarport() {
        return starport;
    }

    /** Sets the UWP starport class (0-10, where 0 = XL, 10 = 'A' = none). */
    public void setUwpStarport(Integer starport) {
        this.starport = starport;
    }

    /** Returns the UWP population level (0-10). Null if not set. */
    public Integer getUwpPopulationLevel() {
        return populationLevel;
    }

    /** Sets the UWP population level (0-10, where 0 = none, 10 = 'A' = 1+ trillion). */
    public void setUwpPopulationLevel(Integer populationLevel) {
        this.populationLevel = populationLevel;
    }

    /** Returns the UWP government type (0-10). Null if not set. */
    public Integer getUwpGovernmentType() {
        return governmentType;
    }

    /** Sets the UWP government type (0-10, where 10 = 'A' = none). */
    public void setUwpGovernmentType(Integer governmentType) {
        this.governmentType = governmentType;
    }

    /** Returns the UWP law level (0-10). Null if not set. */
    public Integer getUwpLawLevel() {
        return lawLevel;
    }

    /** Sets the UWP law level (0-10, where 0 = 'A' = anarchy, 10 = 'X' = total control). */
    public void setUwpLawLevel(Integer lawLevel) {
        this.lawLevel = lawLevel;
    }

    /** Returns the set of trade codes (immutable copy). */
    public Set<TradeCode> getTradeCodes() {
        return new HashSet<>(tradeCodes);
    }

    /** Adds a trade code to this planet. */
    public void addTradeCode(TradeCode code) {
        tradeCodes.add(code);
    }

    /** Removes a trade code from this planet. */
    public void removeTradeCode(TradeCode code) {
        tradeCodes.remove(code);
    }

    /** Returns true if this planet has UWP data set. */
    public boolean hasUWP() {
        return getUwpAtmosphere() != null && getUwpGravity() != null && getUwpStarport() != null
                && getUwpPopulationLevel() != null && getUwpGovernmentType() != null && getUwpLawLevel() != null;
    }

    /**
     * Returns the UWP string representation (e.g., "A544430-D").
     * Returns null if UWP is not fully set.
     */
    public String getUWPString() {
        if (!hasUWP()) {
            return null;
        }
        // Planet size maps to UWP body size: 0-9, A
        int bodySize = Math.min(10, size.ordinal());
        return uwpChar(bodySize)
                + uwpChar(getUwpStarport())
                + uwpChar(getUwpAtmosphere())
                + uwpChar(getUwpGravity())
                + uwpChar(getUwpPopulationLevel())
                + uwpChar(getUwpPopulationLevel())
                + uwpChar(getUwpGovernmentType())
                + "-" + uwpChar(getUwpLawLevel());
    }

    private String uwpChar(int value) {
        if (value < 0 || value > 10) return "?";
        if (value < 10) return String.valueOf(value);
        return "A";
    }

    @Override
    public String toString() {
        String uwp = hasUWP() ? ", uwp=" + getUWPString() : "";
        return String.format("Planet{name='%s', id=%d, size=%s, minerals=%s, radius=%d, pop=%d/%d, fleets=%d, deposits=%s, facilities=%s%s}",
                name, id, size, mineralQuality, getRadius(), currentPopulation, size.getMaxPopulation(),
                getStationedFleetCount(), strategicDeposits, startingFacilities, uwp);
    }
}
