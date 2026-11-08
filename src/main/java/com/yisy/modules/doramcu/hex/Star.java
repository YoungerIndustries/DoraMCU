package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A star — a luminous celestial body that serves as the center of a solar system.
 * <p>
 * Stars are primary sources of energy and provide strategic resources based on their type.
 * Different star types offer different strategic resources (GalCiv 4 model):
 * <ul>
 *   <li>RED stars — Durantium deposits (carrier items & special buildings)</li>
 *   <li>BLUE stars — Thulium deposits (kinetic weapons & armor)</li>
 *   <li>PURPLE stars — Promethion deposits (research & healing)</li>
 *   <li>YELLOW stars — No default strategic resource (Sun-like, balanced)</li>
 * </ul>
 * Stars provide radiation hazards that can damage unshielded ships and gravity wells
 * that enable slingshot maneuvers for faster travel.
 * <p>
 * Based on implementations in Galactic Civilizations IV, Stellaris, and FreeOrion.
 */
public class Star extends CelestialBody {

    public static final int DEFAULT_RADIUS = 5;
    public static final int DEFAULT_ENERGY_YIELD = 10;
    public static final int DEFAULT_STATION_SLOTS = 2;
    public static final double DEFAULT_RADIATION_DAMAGE = 0.1;
    public static final double DEFAULT_GRAVITY_WELL_BONUS = 0.3;

    /**
     * Star size classification affecting radius, energy yield, station slots, radiation, and gravity well.
     */
    public enum StarSize {
        /**
         * Tiny star — small radius, minimal energy, weak effects.
         */
        TINY(2, 4, 1, 0.03, 0.1),

        /**
         * Small star — modest radius and energy output.
         */
        SMALL(3, 6, 1, 0.05, 0.15),

        /**
         * Medium star — balanced radius, energy, and effects.
         */
        MEDIUM(5, 10, 2, 0.1, 0.3),

        /**
         * Large star — significant radius and strong effects.
         */
        LARGE(8, 15, 3, 0.15, 0.4),

        /**
         * Massive star — vast radius, maximum energy and effects.
         */
        MASSIVE(12, 20, 4, 0.2, 0.5);

        private final int baseRadius;
        private final int baseEnergyYield;
        private final int baseStationSlots;
        private final double baseRadiationDamage;
        private final double baseGravityWellBonus;

        StarSize(int baseRadius, int baseEnergyYield, int baseStationSlots,
                 double baseRadiationDamage, double baseGravityWellBonus) {
            this.baseRadius = baseRadius;
            this.baseEnergyYield = baseEnergyYield;
            this.baseStationSlots = baseStationSlots;
            this.baseRadiationDamage = baseRadiationDamage;
            this.baseGravityWellBonus = baseGravityWellBonus;
        }

        /**
         * @return the base radius for this star size
         */
        public int getBaseRadius() {
            return baseRadius;
        }

        /**
         * @return the base energy yield for this star size
         */
        public int getBaseEnergyYield() {
            return baseEnergyYield;
        }

        /**
         * @return the base station slots for this star size
         */
        public int getBaseStationSlots() {
            return baseStationSlots;
        }

        /**
         * @return the base radiation damage for this star size
         */
        public double getBaseRadiationDamage() {
            return baseRadiationDamage;
        }

        /**
         * @return the base gravity well bonus for this star size
         */
        public double getBaseGravityWellBonus() {
            return baseGravityWellBonus;
        }
    }

    public enum StarType {
        /**
         * Red dwarf star — smaller, cooler, longer-lived.
         * Provides Durantium for carrier items and special buildings.
         * Surface temperature: ~3,000K, Lifespan: ~100,000 MY
         */
        RED(StrategicResource.DURANTIUM, 8, 0.05, 0.2, 3000, 100000),

        /**
         * Yellow star — Sun-like, balanced properties.
         * No default strategic resource.
         * Surface temperature: ~5,778K, Lifespan: ~10,000 MY
         */
        YELLOW(null, 10, 0.1, 0.3, 5778, 10000),

        /**
         * Blue giant star — larger, hotter, more energetic.
         * Provides Thulium for kinetic weapons and armor.
         * Surface temperature: ~30,000K, Lifespan: ~1,000 MY
         */
        BLUE(StrategicResource.THULIUM, 15, 0.2, 0.4, 30000, 1000),

        /**
         * Purple star — rare, exotic properties.
         * Provides Promethion for research and healing.
         * Surface temperature: ~7,500K, Lifespan: ~5,000 MY
         */
        PURPLE(StrategicResource.PROMETHION, 12, 0.15, 0.35, 7500, 5000);

        private final StrategicResource strategicResource;
        private final int baseEnergyYield;
        private final double radiationDamage;
        private final double gravityWellBonus;
        private final int surfaceTemperature;
        private final long lifespanMillionYears;

        StarType(StrategicResource strategicResource, int baseEnergyYield, double radiationDamage, double gravityWellBonus,
                 int surfaceTemperature, long lifespanMillionYears) {
            this.strategicResource = strategicResource;
            this.baseEnergyYield = baseEnergyYield;
            this.radiationDamage = radiationDamage;
            this.gravityWellBonus = gravityWellBonus;
            this.surfaceTemperature = surfaceTemperature;
            this.lifespanMillionYears = lifespanMillionYears;
        }

        /**
         * @return the strategic resource associated with this star type, or null if none
         */
        public StrategicResource getStrategicResource() {
            return strategicResource;
        }

        /**
         * @return the base energy yield for this star type
         */
        public int getBaseEnergyYield() {
            return baseEnergyYield;
        }

        /**
         * @return the radiation damage multiplier (0.0 to 1.0)
         */
        public double getRadiationDamage() {
            return radiationDamage;
        }

        /**
         * @return the gravity well slingshot bonus multiplier
         */
        public double getGravityWellBonus() {
            return gravityWellBonus;
        }

        /**
         * @return the surface temperature in Kelvin
         */
        public int getSurfaceTemperature() {
            return surfaceTemperature;
        }

        /**
         * @return the stellar lifespan in million years
         */
        public long getLifespanMillionYears() {
            return lifespanMillionYears;
        }
    }

    private StarSize starSize;
    private StarType starType;
    private int energyYield;
    private int stationSlots;
    private final List<String> occupiedStations;
    private double radiationDamage;
    private double gravityWellBonus;
    private final StarGeometry geometry;

    /**
     * Creates a new star with the given ID, default radius of 5, and YELLOW type.
     *
     * @param id the unique identifier for this star
     */
    public Star(int id) {
        this(id, StarSize.MEDIUM, StarType.YELLOW);
    }

    /**
     * Creates a new star with the given ID, custom radius, and YELLOW type.
     *
     * @param id the unique identifier for this star
     * @param radius the radius of the star (0 for a single center hex)
     * @throws IllegalArgumentException if radius is negative
     */
    public Star(int id, int radius) {
        this(id, radius, StarType.YELLOW);
    }

    /**
     * Creates a new star with the given ID, default radius, and star type.
     *
     * @param id the unique identifier for this star
     * @param starType the classification of the star
     */
    public Star(int id, StarType starType) {
        this(id, DEFAULT_RADIUS, starType);
    }

    /**
     * Creates a new star with the given ID and star size.
     * Radius, energy yield, station slots, radiation, and gravity well are derived from the size.
     * Star type defaults to YELLOW.
     *
     * @param id the unique identifier for this star
     * @param starSize the size classification of the star
     * @throws IllegalArgumentException if starSize is null
     */
    public Star(int id, StarSize starSize) {
        this(id, starSize, StarType.YELLOW);
    }

    /**
     * Creates a new star with the given ID, star size, and star type.
     * Radius, energy yield, station slots, radiation, and gravity well are derived from the size.
     * Strategic resource is derived from the star type.
     *
     * @param id the unique identifier for this star
     * @param starSize the size classification of the star
     * @param starType the classification of the star
     * @throws IllegalArgumentException if starSize or starType is null
     */
    public Star(int id, StarSize starSize, StarType starType) {
        super(id, starSize == null ? DEFAULT_RADIUS : starSize.getBaseRadius());
        if (starSize == null) {
            throw new IllegalArgumentException("Star size must not be null");
        }
        if (starType == null) {
            throw new IllegalArgumentException("Star type must not be null");
        }
        this.starSize = starSize;
        this.starType = starType;
        this.energyYield = starSize.getBaseEnergyYield();
        this.stationSlots = starSize.getBaseStationSlots();
        this.occupiedStations = new ArrayList<>();
        this.radiationDamage = starSize.getBaseRadiationDamage();
        this.gravityWellBonus = starSize.getBaseGravityWellBonus();
        this.geometry = new StarGeometry(this);
        setStrategicResource(starType.getStrategicResource());
    }

    /**
     * Creates a new star with the given ID, custom radius, and star type.
     * Energy yield, radiation, and strategic resource are derived from the star type.
     *
     * @param id the unique identifier for this star
     * @param radius the radius of the star (0 for a single center hex)
     * @param starType the classification of the star
     * @throws IllegalArgumentException if radius is negative
     */
    public Star(int id, int radius, StarType starType) {
        super(id, radius);
        if (starType == null) {
            throw new IllegalArgumentException("Star type must not be null");
        }
        this.starSize = StarSize.MEDIUM;
        this.starType = starType;
        this.energyYield = starType.getBaseEnergyYield();
        this.stationSlots = DEFAULT_STATION_SLOTS;
        this.occupiedStations = new ArrayList<>();
        this.radiationDamage = starType.getRadiationDamage();
        this.gravityWellBonus = starType.getGravityWellBonus();
        this.geometry = new StarGeometry(this);
        setStrategicResource(starType.getStrategicResource());
    }

    @Override
    protected String defaultName(int id) {
        return "Star-" + id;
    }

    /**
     * @return the star size classification
     */
    public StarSize getStarSize() {
        return starSize;
    }

    /**
     * Sets the star size classification.
     * Updates energy yield, station slots, radiation, and gravity well based on the new size.
     *
     * @param starSize the new size classification
     */
    public void setStarSize(StarSize starSize) {
        if (starSize == null) {
            throw new IllegalArgumentException("Star size must not be null");
        }
        this.starSize = starSize;
        this.energyYield = starSize.getBaseEnergyYield();
        this.stationSlots = starSize.getBaseStationSlots();
        this.radiationDamage = starSize.getBaseRadiationDamage();
        this.gravityWellBonus = starSize.getBaseGravityWellBonus();
    }

    /**
     * @return the star type classification
     */
    public StarType getStarType() {
        return starType;
    }

    /**
     * Sets the star type classification.
     * Updates energy yield, radiation, and strategic resource based on the new type.
     *
     * @param starType the new classification
     */
    public void setStarType(StarType starType) {
        if (starType == null) {
            throw new IllegalArgumentException("Star type must not be null");
        }
        this.starType = starType;
        this.energyYield = starType.getBaseEnergyYield();
        this.radiationDamage = starType.getRadiationDamage();
        this.gravityWellBonus = starType.getGravityWellBonus();
        setStrategicResource(starType.getStrategicResource());
    }

    // ---- Resource properties ----

    /**
     * Returns the energy yield per turn from this star.
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
     * Returns the effective energy yield, boosted by orbital stations.
     * Each orbital station adds a 15% bonus to the base yield.
     *
     * @return the effective energy yield per turn
     */
    public int getEffectiveEnergyYield() {
        double bonus = occupiedStations.size() * 0.15;
        return (int) Math.round(energyYield * (1.0 + bonus));
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
     * Adds an orbital station to this star system.
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
     * Removes a station from this star system.
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
     * Returns the radiation damage multiplier.
     * A value of 0.1 means unshielded ships take 10% damage per turn near the star.
     *
     * @return the radiation damage (0.0 to 1.0)
     */
    public double getRadiationDamage() {
        return radiationDamage;
    }

    /**
     * Sets the radiation damage multiplier.
     *
     * @param radiationDamage the damage (0.0 to 1.0)
     * @throws IllegalArgumentException if damage is out of range
     */
    public void setRadiationDamage(double radiationDamage) {
        if (radiationDamage < 0.0 || radiationDamage > 1.0) {
            throw new IllegalArgumentException("Radiation damage must be between 0.0 and 1.0: " + radiationDamage);
        }
        this.radiationDamage = radiationDamage;
    }

    /**
     * Returns the radiation damage taken by an unshielded ship per turn.
     *
     * @param baseHull the ship's base hull points
     * @return the hull damage per turn
     */
    public int getRadiationHullDamage(int baseHull) {
        return (int) Math.round(baseHull * radiationDamage);
    }

    /**
     * Returns the reduced hull after one turn of radiation exposure.
     *
     * @param currentHull the ship's current hull points
     * @return the remaining hull after radiation damage
     */
    public int getHullAfterRadiation(int currentHull) {
        return Math.max(0, currentHull - getRadiationHullDamage(currentHull));
    }

    /**
     * Returns the gravity well slingshot bonus multiplier.
     * A value of 0.3 means ships gain 30% speed boost when performing gravity assists.
     *
     * @return the gravity well bonus (0.0 to 1.0)
     */
    public double getGravityWellBonus() {
        return gravityWellBonus;
    }

    /**
     * Sets the gravity well slingshot bonus multiplier.
     *
     * @param gravityWellBonus the bonus (0.0 to 1.0)
     * @throws IllegalArgumentException if bonus is out of range
     */
    public void setGravityWellBonus(double gravityWellBonus) {
        if (gravityWellBonus < 0.0 || gravityWellBonus > 1.0) {
            throw new IllegalArgumentException("Gravity well bonus must be between 0.0 and 1.0: " + gravityWellBonus);
        }
        this.gravityWellBonus = gravityWellBonus;
    }

    /**
     * Returns the enhanced movement speed when performing a gravity assist slingshot.
     *
     * @param baseSpeed the ship's base movement speed
     * @return the boosted movement speed
     */
    public int getSlingshotSpeed(int baseSpeed) {
        return (int) Math.round(baseSpeed * (1.0 + gravityWellBonus));
    }

    // ---- Luminosity classification ----

    /**
     * Returns the astronomical luminosity classification based on star size and type.
     * <p>
     * Classification follows real astronomical patterns:
     * <ul>
     *   <li>TINY/SMALL stars → Dwarf (M-type red dwarfs, K-type orange dwarfs)</li>
     *   <li>MEDIUM stars → Main Sequence (G-type yellow dwarfs like Sol)</li>
     *   <li>LARGE stars → Giant (K-type giants, F-type subgiants)</li>
     *   <li>MASSIVE stars → Supergiant (O-type, B-type blue supergiants)</li>
     * </ul>
     *
     * @return the luminosity classification string
     */
    public String getLuminosityClass() {
        return switch (starSize) {
            case TINY, SMALL -> "Dwarf";
            case MEDIUM -> "Main Sequence";
            case LARGE -> "Giant";
            case MASSIVE -> "Supergiant";
        };
    }

    /**
     * Returns the spectral class designation based on star type.
     * <p>
     * Spectral classes follow the Harvard spectral classification:
     * <ul>
     *   <li>RED → M (cool, red dwarfs and giants)</li>
     *   <li>YELLOW → G (Sun-like, yellow dwarfs)</li>
     *   <li>BLUE → O or B (hot, blue giants and supergiants)</li>
     *   <li>PURPLE → A (white to blue-white stars)</li>
     * </ul>
     *
     * @return the spectral class letter
     */
    public String getSpectralClass() {
        return switch (starType) {
            case RED -> "M";
            case YELLOW -> "G";
            case BLUE -> "O";
            case PURPLE -> "A";
        };
    }

    /**
     * Returns the full stellar classification combining spectral class and luminosity.
     * <p>
     * Example: "M Dwarf", "G Main Sequence", "O Supergiant"
     *
     * @return the full stellar classification string
     */
    public String getFullClassification() {
        return getSpectralClass() + " " + getLuminosityClass();
    }

    // ---- Astronomical properties ----

    /**
     * Returns the surface temperature of the star in Kelvin.
     * <p>
     * Temperature is determined by the star type:
     * <ul>
     *   <li>RED → ~3,000K (cool red dwarfs)</li>
     *   <li>YELLOW → ~5,778K (Sun-like)</li>
     *   <li>BLUE → ~30,000K (hot blue giants)</li>
     *   <li>PURPLE → ~7,500K (white to blue-white)</li>
     * </ul>
     *
     * @return the surface temperature in Kelvin
     */
    public int getSurfaceTemperature() {
        return starType.getSurfaceTemperature();
    }

    /**
     * Returns the absolute magnitude of the star.
     * <p>
     * Absolute magnitude is a measure of intrinsic brightness.
     * Smaller values mean brighter stars.
     * Calculation is based on star size and type.
     *
     * @return the absolute magnitude (negative values are brighter)
     */
    public double getAbsoluteMagnitude() {
        // Base magnitude by size: TINY=16, SMALL=12, MEDIUM=4.8 (Sun), LARGE=0, MASSIVE=-10
        double baseMagnitude = switch (starSize) {
            case TINY -> 16.0;
            case SMALL -> 12.0;
            case MEDIUM -> 4.8;
            case LARGE -> 0.0;
            case MASSIVE -> -10.0;
        };
        // Type adjustment: hotter stars are brighter
        double typeAdjustment = switch (starType) {
            case RED -> 1.0;
            case YELLOW -> 0.0;
            case PURPLE -> -1.5;
            case BLUE -> -3.0;
        };
        return baseMagnitude + typeAdjustment;
    }

    /**
     * Returns the stellar lifespan in million years.
     * <p>
     * Lifespan is determined by the star type:
     * <ul>
     *   <li>RED → ~100,000 MY (red dwarfs burn slowly)</li>
     *   <li>YELLOW → ~10,000 MY (Sun-like)</li>
     *   <li>BLUE → ~1,000 MY (burn fuel rapidly)</li>
     *   <li>PURPLE → ~5,000 MY (moderate lifespan)</li>
     * </ul>
     * Larger stars of the same type have shorter lifespans due to higher fuel consumption.
     *
     * @return the lifespan in million years
     */
    public long getLifespan() {
        long baseLifespan = starType.getLifespanMillionYears();
        // Size modifier: larger stars consume fuel faster
        double sizeModifier = switch (starSize) {
            case TINY -> 2.0;
            case SMALL -> 1.5;
            case MEDIUM -> 1.0;
            case LARGE -> 0.5;
            case MASSIVE -> 0.25;
        };
        return (long) Math.round(baseLifespan * sizeModifier);
    }

    /**
     * Returns the inner boundary of the habitable zone in hex distance from the star.
     * <p>
     * The habitable zone is the region where liquid water can exist on a planet's surface.
     * The inner boundary is determined by the star's luminosity.
     *
     * @return the inner habitable zone distance in hex units
     */
    public int getHabitableZoneInner() {
        // Habitable zone scales with square root of luminosity
        // Luminosity approximation from absolute magnitude
        double luminosity = Math.pow(10, (4.8 - getAbsoluteMagnitude()) / 2.5);
        double innerDistance = Math.sqrt(luminosity * 0.95);
        return Math.max(1, (int) Math.round(innerDistance));
    }

    /**
     * Returns the outer boundary of the habitable zone in hex distance from the star.
     * <p>
     * The outer boundary marks where it becomes too cold for liquid water.
     *
     * @return the outer habitable zone distance in hex units
     */
    public int getHabitableZoneOuter() {
        double luminosity = Math.pow(10, (4.8 - getAbsoluteMagnitude()) / 2.5);
        double outerDistance = Math.sqrt(luminosity * 1.37);
        return Math.max(1, (int) Math.round(outerDistance));
    }

    /**
     * Checks if a planet at the given distance is within the habitable zone.
     *
     * @param distance the distance from the star in hex units
     * @return true if the distance is within the habitable zone
     */
    public boolean isInHabitableZone(int distance) {
        return distance >= getHabitableZoneInner() && distance <= getHabitableZoneOuter();
    }

    // ---- Hex operations (delegated to StarGeometry) ----

    /**
     * Retrieves a hex by its cubic coordinates within the star.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex at the given position, or null if out of bounds
     */
    public Hex3D get(int x, int y) {
        return geometry.get(x, y);
    }

    /**
     * Retrieves a hex by a Hex3D instance.
     *
     * @param hex the hex to look up
     * @return the stored hex, or null if not present
     */
    public Hex3D get(Hex3D hex) {
        return geometry.get(hex);
    }

    /**
     * Checks if the given coordinates are within this star.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are within the star's sphere
     */
    public boolean contains(int x, int y) {
        return geometry.contains(x, y);
    }

    /**
     * Checks if the given hex is within this star.
     *
     * @param hex the hex to check
     * @return true if the hex is within the star
     */
    public boolean contains(Hex3D hex) {
        return geometry.contains(hex);
    }

    /**
     * @return all hex cells in the star (unmodifiable collection)
     */
    public Collection<Hex3D> getAll() {
        return geometry.getAll();
    }

    // ---- Query operations (delegated to StarGeometry) ----

    /**
     * Returns all hexes in the given state.
     *
     * @param state the state to filter by
     * @return list of hexes matching the state
     */
    public List<Hex3D> findByState(HexState state) {
        return geometry.findByState(state);
    }

    /**
     * Returns the neighbors of the given hex that are also in this star.
     *
     * @param hex the center hex
     * @return list of neighboring hexes present in the star
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        return geometry.getNeighbors(hex);
    }

    // ---- Occupancy statistics (delegated to StarGeometry) ----

    /**
     * @return the number of occupied hex cells
     */
    public int getOccupiedCount() {
        return geometry.getOccupiedCount();
    }

    /**
     * @return the number of empty hex cells
     */
    public int getEmptyCount() {
        return geometry.getEmptyCount();
    }

    /**
     * @return the number of blocked hex cells
     */
    public int getBlockedCount() {
        return geometry.getBlockedCount();
    }

    /**
     * @return the number of selected hex cells
     */
    public int getSelectedCount() {
        return geometry.getSelectedCount();
    }

    /**
     * @return the ratio of occupied hexes to total hexes (0.0 to 1.0)
     */
    public double getOccupancyRatio() {
        return geometry.getOccupancyRatio();
    }

    // ---- Coordinate conversion utilities (delegated to StarGeometry) ----

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
        return geometry.toGlobal(localX, localY);
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
        return geometry.isInBounds(x, y, z);
    }

    /**
     * Returns the center hex of the star.
     *
     * @return the center hex at (0, 0, 0)
     */
    public Hex3D getCenter() {
        return geometry.getCenter();
    }

    /**
     * Returns the hexes on the surface of the star (at exactly the star's radius from center).
     *
     * @return list of surface hexes
     */
    public List<Hex3D> getSurface() {
        return geometry.getSurface();
    }

    /**
     * Returns the hexes in the core of the star (within half the radius from center).
     *
     * @return list of core hexes
     */
    public List<Hex3D> getCore() {
        return geometry.getCore();
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Star star = (Star) o;
        return id == star.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Star{name='%s', id=%d, type=%s, radius=%d, energy=%d, stations=%d/%d, radiation=%.0f%%, slingshot=%.0f%%}",
                name, id, starType, radius, energyYield, occupiedStations.size(), stationSlots,
                radiationDamage * 100, gravityWellBonus * 100);
    }
}
