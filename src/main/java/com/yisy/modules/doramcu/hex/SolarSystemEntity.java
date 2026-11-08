package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A container for multiple bodies (Stars, Planets, Fleets, Space Stations) positioned in 3D hex space.
 * <p>
 * SolarSystemEntity manages a collection of body instances each positioned
 * at a specific {@link Hex3D} coordinate, wrapped in {@link SystemBody} entries.
 * <p>
 * Each body retains its own local coordinate system (centered at origin), while the
 * solar system tracks global positions for spatial queries like finding bodies within
 * a range or looking up neighbors across body boundaries.
 */
public class SolarSystemEntity {

    public static final int DEFAULT_RANGE = 100;

    private final int id;
    private final int range;
    private final SolarSystemBodyRegistry<Star> starRegistry;
    private final SolarSystemBodyRegistry<Planet> planetRegistry;
    private final SolarSystemBodyRegistry<AsteroidBelt> asteroidBeltRegistry;
    private final SolarSystemBodyRegistry<GasGiant> gasGiantRegistry;
    private final SolarSystemBodyRegistry<Nebula> nebulaRegistry;
    private final SolarSystemBodyRegistry<BlackHole> blackHoleRegistry;
    private final SolarSystemBodyRegistry<Moon> moonRegistry;
    private final SolarSystemBodyRegistry<Asteroid> asteroidRegistry;
    private final SolarSystemBodyRegistry<Comet> cometRegistry;
    private final SolarSystemBodyRegistry<Fleet> fleetRegistry;
    private final SolarSystemBodyRegistry<SpaceStation> spaceStationRegistry;
    private final Map<Hex3D, SystemBody> positionIndex;
    private final Hex3DMap spaceMap;


    /**
     * Creates a new solar system with the given ID and default range of 100.
     *
     * @param id the unique identifier for this solar system
     */
    public SolarSystemEntity(int id) {
        this(id, DEFAULT_RANGE);
    }

    /**
     * Creates a new solar system with the given ID and custom range.
     *
     * @param id the unique identifier for this solar system
     * @param range the maximum distance from center for spatial queries
     * @throws IllegalArgumentException if range is negative
     */
    public SolarSystemEntity(int id, int range) {
        if (range < 0) {
            throw new IllegalArgumentException("Range must be non-negative: " + range);
        }
        this.id = id;
        this.range = range;
        this.starRegistry = new SolarSystemBodyRegistry<>();
        this.planetRegistry = new SolarSystemBodyRegistry<>();
        this.asteroidBeltRegistry = new SolarSystemBodyRegistry<>();
        this.gasGiantRegistry = new SolarSystemBodyRegistry<>();
        this.nebulaRegistry = new SolarSystemBodyRegistry<>();
        this.blackHoleRegistry = new SolarSystemBodyRegistry<>();
        this.moonRegistry = new SolarSystemBodyRegistry<>();
        this.asteroidRegistry = new SolarSystemBodyRegistry<>();
        this.cometRegistry = new SolarSystemBodyRegistry<>();
        this.fleetRegistry = new SolarSystemBodyRegistry<>();
        this.spaceStationRegistry = new SolarSystemBodyRegistry<>();
        this.positionIndex = new HashMap<>();
        this.spaceMap = new Hex3DMap();
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this solar system
     */
    public int getId() {
        return id;
    }

    /**
     * @return the number of celestial bodies (stars + planets + asteroid belts + gas giants + nebulas + black holes + moons + asteroids + comets + fleets + space stations)
     */
    public int getBodyCount() {
        return starRegistry.count() + planetRegistry.count() + asteroidBeltRegistry.count()
                + gasGiantRegistry.count() + nebulaRegistry.count() + blackHoleRegistry.count()
                + moonRegistry.count() + asteroidRegistry.count() + cometRegistry.count()
                + fleetRegistry.count() + spaceStationRegistry.count();
    }

    /**
     * @return the center hex of the solar system (0, 0, 0)
     */
    Hex3D getCenter() {
        return new Hex3D(0, 0, 0);
    }

    // ---- Star operations ----

    /**
     * @return the number of stars
     */
    public int getStarCount() {
        return starRegistry.count();
    }

    /**
     * Adds a star at the given global position.
     *
     * @param star the star to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addStar(Star star, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        starRegistry.add(star);
        SystemBody entry = new SystemBody(star, position, SystemBody.BodyType.STAR);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a star by its ID.
     *
     * @param starId the star's unique identifier
     * @return the star, or null if not present
     */
    public Star getStar(int starId) {
        return starRegistry.get(starId);
    }

    /**
     * Removes a star by its ID.
     *
     * @param starId the star's unique identifier
     * @return the removed star, or null if not present
     */
    public Star removeStar(int starId) {
        Star star = starRegistry.remove(starId);
        if (star != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == star);
            updateSpaceMap();
        }
        return star;
    }

    /**
     * @return all stars in the solar system (unmodifiable map)
     */
    public Map<Integer, Star> getAllStars() {
        return starRegistry.getAll();
    }

    // ---- Planet operations ----

    /**
     * @return the number of planets
     */
    public int getPlanetCount() {
        return planetRegistry.count();
    }

    /**
     * Adds a planet at the given global position.
     *
     * @param planet the planet to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addPlanet(Planet planet, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        planetRegistry.add(planet);
        SystemBody entry = new SystemBody(planet, position, SystemBody.BodyType.PLANET);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a planet by its ID.
     *
     * @param planetId the planet's unique identifier
     * @return the planet, or null if not present
     */
    public Planet getPlanet(int planetId) {
        return planetRegistry.get(planetId);
    }

    /**
     * Removes a planet by its ID.
     *
     * @param planetId the planet's unique identifier
     * @return the removed planet, or null if not present
     */
    public Planet removePlanet(int planetId) {
        Planet planet = planetRegistry.remove(planetId);
        if (planet != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == planet);
            updateSpaceMap();
        }
        return planet;
    }

    /**
     * @return all planets in the solar system (unmodifiable map)
     */
    public Map<Integer, Planet> getAllPlanets() {
        return planetRegistry.getAll();
    }

    // ---- Asteroid Belt operations ----

    /**
     * @return the number of asteroid belts
     */
    public int getAsteroidBeltCount() {
        return asteroidBeltRegistry.count();
    }

    /**
     * Adds an asteroid belt at the given global position.
     *
     * @param asteroidBelt the asteroid belt to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addAsteroidBelt(AsteroidBelt asteroidBelt, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        asteroidBeltRegistry.add(asteroidBelt);
        SystemBody entry = new SystemBody(asteroidBelt, position, SystemBody.BodyType.ASTEROID_BELT);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves an asteroid belt by its ID.
     *
     * @param beltId the asteroid belt's unique identifier
     * @return the asteroid belt, or null if not present
     */
    public AsteroidBelt getAsteroidBelt(int beltId) {
        return asteroidBeltRegistry.get(beltId);
    }

    /**
     * Removes an asteroid belt by its ID.
     *
     * @param beltId the asteroid belt's unique identifier
     * @return the removed asteroid belt, or null if not present
     */
    public AsteroidBelt removeAsteroidBelt(int beltId) {
        AsteroidBelt belt = asteroidBeltRegistry.remove(beltId);
        if (belt != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == belt);
            updateSpaceMap();
        }
        return belt;
    }

    /**
     * @return all asteroid belts in the solar system (unmodifiable map)
     */
    public Map<Integer, AsteroidBelt> getAllAsteroidBelts() {
        return asteroidBeltRegistry.getAll();
    }

    // ---- Gas Giant operations ----

    /**
     * @return the number of gas giants
     */
    public int getGasGiantCount() {
        return gasGiantRegistry.count();
    }

    /**
     * Adds a gas giant at the given global position.
     *
     * @param gasGiant the gas giant to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addGasGiant(GasGiant gasGiant, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        gasGiantRegistry.add(gasGiant);
        SystemBody entry = new SystemBody(gasGiant, position, SystemBody.BodyType.GAS_GIANT);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a gas giant by its ID.
     *
     * @param gasGiantId the gas giant's unique identifier
     * @return the gas giant, or null if not present
     */
    public GasGiant getGasGiant(int gasGiantId) {
        return gasGiantRegistry.get(gasGiantId);
    }

    /**
     * Removes a gas giant by its ID.
     *
     * @param gasGiantId the gas giant's unique identifier
     * @return the removed gas giant, or null if not present
     */
    public GasGiant removeGasGiant(int gasGiantId) {
        GasGiant gasGiant = gasGiantRegistry.remove(gasGiantId);
        if (gasGiant != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == gasGiant);
            updateSpaceMap();
        }
        return gasGiant;
    }

    /**
     * @return all gas giants in the solar system (unmodifiable map)
     */
    public Map<Integer, GasGiant> getAllGasGiants() {
        return gasGiantRegistry.getAll();
    }

    // ---- Nebula operations ----

    /**
     * @return the number of nebulas
     */
    public int getNebulaCount() {
        return nebulaRegistry.count();
    }

    /**
     * Adds a nebula at the given global position.
     *
     * @param nebula the nebula to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addNebula(Nebula nebula, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        nebulaRegistry.add(nebula);
        SystemBody entry = new SystemBody(nebula, position, SystemBody.BodyType.NEBULA);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a nebula by its ID.
     *
     * @param nebulaId the nebula's unique identifier
     * @return the nebula, or null if not present
     */
    public Nebula getNebula(int nebulaId) {
        return nebulaRegistry.get(nebulaId);
    }

    /**
     * Removes a nebula by its ID.
     *
     * @param nebulaId the nebula's unique identifier
     * @return the removed nebula, or null if not present
     */
    public Nebula removeNebula(int nebulaId) {
        Nebula nebula = nebulaRegistry.remove(nebulaId);
        if (nebula != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == nebula);
            updateSpaceMap();
        }
        return nebula;
    }

    /**
     * @return all nebulas in the solar system (unmodifiable map)
     */
    public Map<Integer, Nebula> getAllNebulas() {
        return nebulaRegistry.getAll();
    }

    // ---- Black Hole operations ----

    /**
     * @return the number of black holes
     */
    public int getBlackHoleCount() {
        return blackHoleRegistry.count();
    }

    /**
     * Adds a black hole at the given global position.
     *
     * @param blackHole the black hole to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addBlackHole(BlackHole blackHole, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        blackHoleRegistry.add(blackHole);
        SystemBody entry = new SystemBody(blackHole, position, SystemBody.BodyType.BLACK_HOLE);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a black hole by its ID.
     *
     * @param blackHoleId the black hole's unique identifier
     * @return the black hole, or null if not present
     */
    public BlackHole getBlackHole(int blackHoleId) {
        return blackHoleRegistry.get(blackHoleId);
    }

    /**
     * Removes a black hole by its ID.
     *
     * @param blackHoleId the black hole's unique identifier
     * @return the removed black hole, or null if not present
     */
    public BlackHole removeBlackHole(int blackHoleId) {
        BlackHole blackHole = blackHoleRegistry.remove(blackHoleId);
        if (blackHole != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == blackHole);
            updateSpaceMap();
        }
        return blackHole;
    }

    /**
     * @return all black holes in the solar system (unmodifiable map)
     */
    public Map<Integer, BlackHole> getAllBlackHoles() {
        return blackHoleRegistry.getAll();
    }

    // ---- Moon operations ----

    /**
     * @return the number of moons
     */
    public int getMoonCount() {
        return moonRegistry.count();
    }

    /**
     * Adds a moon at the given global position.
     *
     * @param moon the moon to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addMoon(Moon moon, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        moonRegistry.add(moon);
        SystemBody entry = new SystemBody(moon, position, SystemBody.BodyType.MOON);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a moon by its ID.
     *
     * @param moonId the moon's unique identifier
     * @return the moon, or null if not present
     */
    public Moon getMoon(int moonId) {
        return moonRegistry.get(moonId);
    }

    /**
     * Removes a moon by its ID.
     *
     * @param moonId the moon's unique identifier
     * @return the removed moon, or null if not present
     */
    public Moon removeMoon(int moonId) {
        Moon moon = moonRegistry.remove(moonId);
        if (moon != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == moon);
            updateSpaceMap();
        }
        return moon;
    }

    /**
     * @return all moons in the solar system (unmodifiable map)
     */
    public Map<Integer, Moon> getAllMoons() {
        return moonRegistry.getAll();
    }

    // ---- Asteroid operations ----

    /**
     * @return the number of asteroids
     */
    public int getAsteroidCount() {
        return asteroidRegistry.count();
    }

    /**
     * Adds an asteroid at the given global position.
     *
     * @param asteroid the asteroid to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addAsteroid(Asteroid asteroid, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        asteroidRegistry.add(asteroid);
        SystemBody entry = new SystemBody(asteroid, position, SystemBody.BodyType.ASTEROID);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves an asteroid by its ID.
     *
     * @param asteroidId the asteroid's unique identifier
     * @return the asteroid, or null if not present
     */
    public Asteroid getAsteroid(int asteroidId) {
        return asteroidRegistry.get(asteroidId);
    }

    /**
     * Removes an asteroid by its ID.
     *
     * @param asteroidId the asteroid's unique identifier
     * @return the removed asteroid, or null if not present
     */
    public Asteroid removeAsteroid(int asteroidId) {
        Asteroid asteroid = asteroidRegistry.remove(asteroidId);
        if (asteroid != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == asteroid);
            updateSpaceMap();
        }
        return asteroid;
    }

    /**
     * @return all asteroids in the solar system (unmodifiable map)
     */
    public Map<Integer, Asteroid> getAllAsteroids() {
        return asteroidRegistry.getAll();
    }

    // ---- Comet operations ----

    /**
     * @return the number of comets
     */
    public int getCometCount() {
        return cometRegistry.count();
    }

    /**
     * Adds a comet at the given global position.
     *
     * @param comet the comet to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addComet(Comet comet, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        cometRegistry.add(comet);
        SystemBody entry = new SystemBody(comet, position, SystemBody.BodyType.COMET);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a comet by its ID.
     *
     * @param cometId the comet's unique identifier
     * @return the comet, or null if not present
     */
    public Comet getComet(int cometId) {
        return cometRegistry.get(cometId);
    }

    /**
     * Removes a comet by its ID.
     *
     * @param cometId the comet's unique identifier
     * @return the removed comet, or null if not present
     */
    public Comet removeComet(int cometId) {
        Comet comet = cometRegistry.remove(cometId);
        if (comet != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == comet);
            updateSpaceMap();
        }
        return comet;
    }

    /**
     * @return all comets in the solar system (unmodifiable map)
     */
    public Map<Integer, Comet> getAllComets() {
        return cometRegistry.getAll();
    }

    // ---- Fleet operations ----

    /**
     * @return the number of fleets
     */
    public int getFleetCount() {
        return fleetRegistry.count();
    }

    /**
     * Adds a fleet at the given global position.
     *
     * @param fleet the fleet to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addFleet(Fleet fleet, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        fleetRegistry.add(fleet);
        SystemBody entry = new SystemBody(fleet, position, SystemBody.BodyType.FLEET);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a fleet by its ID.
     *
     * @param fleetId the fleet's unique identifier
     * @return the fleet, or null if not present
     */
    public Fleet getFleet(int fleetId) {
        return fleetRegistry.get(fleetId);
    }

    /**
     * Removes a fleet by its ID.
     *
     * @param fleetId the fleet's unique identifier
     * @return the removed fleet, or null if not present
     */
    public Fleet removeFleet(int fleetId) {
        Fleet fleet = fleetRegistry.remove(fleetId);
        if (fleet != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == fleet);
            updateSpaceMap();
        }
        return fleet;
    }

    /**
     * @return all fleets in the solar system (unmodifiable map)
     */
    public Map<Integer, Fleet> getAllFleets() {
        return fleetRegistry.getAll();
    }

    // ---- Space Station operations ----

    /**
     * @return the number of space stations
     */
    public int getSpaceStationCount() {
        return spaceStationRegistry.count();
    }

    /**
     * Adds a space station at the given global position.
     *
     * @param station the station to add
     * @param position the global position in the solar system
     * @return the previous body at that position, or null
     */
    public SystemBody addSpaceStation(SpaceStation station, Hex3D position) {
        SystemBody removed = positionIndex.remove(position);
        spaceStationRegistry.add(station);
        station.setPosition(position);
        SystemBody entry = new SystemBody(station, position, SystemBody.BodyType.SPACE_STATION);
        positionIndex.put(position, entry);
        updateSpaceMap();
        return removed;
    }

    /**
     * Retrieves a space station by its ID.
     *
     * @param stationId the station's unique identifier
     * @return the station, or null if not present
     */
    public SpaceStation getSpaceStation(int stationId) {
        return spaceStationRegistry.get(stationId);
    }

    /**
     * Removes a space station by its ID.
     *
     * @param stationId the station's unique identifier
     * @return the removed station, or null if not present
     */
    public SpaceStation removeSpaceStation(int stationId) {
        SpaceStation station = spaceStationRegistry.remove(stationId);
        if (station != null) {
            positionIndex.values().removeIf(cb -> cb.getBody() == station);
            updateSpaceMap();
        }
        return station;
    }

    /**
     * @return all space stations in the solar system (unmodifiable map)
     */
    public Map<Integer, SpaceStation> getAllSpaceStations() {
        return spaceStationRegistry.getAll();
    }

    // ---- Position-based queries ----

    /**
     * Retrieves the celestial body at the given global position.
     *
     * @param position the global position
     * @return the celestial body, or null if no body is at that position
     */
    public SystemBody getAt(Hex3D position) {
        return positionIndex.get(position);
    }

    /**
     * Checks if there is a celestial body at the given global position.
     *
     * @param position the global position
     * @return true if a body exists at that position
     */
    public boolean containsPosition(Hex3D position) {
        return positionIndex.containsKey(position);
    }

    /**
     * @return all celestial bodies with their positions (unmodifiable collection)
     */
    public Collection<SystemBody> getAllBodies() {
        return Collections.unmodifiableCollection(positionIndex.values());
    }

    // ---- Spatial queries ----

    /**
     * Returns all celestial bodies within the given distance from the center.
     *
     * @param maxDistance the maximum distance from the solar system center
     * @return list of celestial bodies within range
     */
    public List<SystemBody> findByDistance(int maxDistance) {
        Hex3D center = getCenter();
        return positionIndex.values().stream()
                .filter(cb -> center.distance(cb.getPosition()) <= maxDistance)
                .collect(Collectors.toList());
    }

    /**
     * Returns all celestial bodies within the given distance from a specific position.
     *
     * @param from the reference position
     * @param maxDistance the maximum distance
     * @return list of celestial bodies within range
     */
    public List<SystemBody> findByDistance(Hex3D from, int maxDistance) {
        return positionIndex.values().stream()
                .filter(cb -> from.distance(cb.getPosition()) <= maxDistance)
                .collect(Collectors.toList());
    }

    /**
     * Returns the nearest celestial body to the given position.
     *
     * @param from the reference position
     * @return the nearest celestial body, or null if no bodies exist
     */
    public SystemBody findNearest(Hex3D from) {
        return positionIndex.values().stream()
                .min(Comparator.comparingInt(cb -> from.distance(cb.getPosition())))
                .orElse(null);
    }

    /**
     * Returns the neighbors of a celestial body's position (other bodies at adjacent hex positions).
     *
     * @param body the celestial body
     * @return list of neighboring celestial bodies
     */
    public List<SystemBody> getNeighbors(SystemBody body) {
        List<SystemBody> result = new ArrayList<>();
        Hex3D pos = body.getPosition();
        for (Hex3DDirection dir : Hex3DDirection.values()) {
            Hex3D neighbor = pos.neighbor(dir);
            SystemBody nb = positionIndex.get(neighbor);
            if (nb != null) {
                result.add(nb);
            }
        }
        return result;
    }

    // ---- Aggregated statistics ----

    /**
     * Returns the total number of hex cells across all celestial bodies.
     *
     * @return total hex count
     */
    public int getTotalHexCount() {
        int total = 0;
        for (SystemBody cb : positionIndex.values()) {
            Object body = cb.getBody();
            if (body instanceof CelestialBody celestialBody) {
                total += celestialBody.size();
            }
        }
        return total;
    }

    /**
     * Returns the total number of occupied hex cells across all celestial bodies.
     *
     * @return total occupied hex count
     */
    public int getTotalOccupiedCount() {
        int total = 0;
        for (SystemBody cb : positionIndex.values()) {
            Object body = cb.getBody();
            if (body instanceof Star star) {
                total += star.getOccupiedCount();
            } else if (body instanceof Planet planet) {
                total += planet.getOccupiedCount();
            } else if (body instanceof CelestialBody celestialBody) {
                total += celestialBody.getMap().findByState(HexState.OCCUPIED).size();
            }
        }
        return total;
    }

    /**
     * Returns the overall occupancy ratio across all bodies.
     *
     * @return ratio of occupied hexes to total hexes (0.0 to 1.0)
     */
    public double getOccupancyRatio() {
        int total = getTotalHexCount();
        if (total == 0) return 0.0;
        return (double) getTotalOccupiedCount() / total;
    }

    // ---- Space map ----

    /**
     * Updates the aggregated space map with all hex cells from all bodies.
     * This is called automatically when bodies are added or removed.
     */
    public void updateSpaceMap() {
        spaceMap.clear();
        for (SystemBody cb : positionIndex.values()) {
            Hex3D pos = cb.getPosition();
            Object body = cb.getBody();
            if (body instanceof CelestialBody celestialBody) {
                // All celestial bodies have getAll() via getMap().getAll()
                for (Hex3D hex : celestialBody.getMap().getAll()) {
                    Hex3D global = new Hex3D(
                            hex.getX() + pos.getX(),
                            hex.getY() + pos.getY(),
                            hex.getZ() + pos.getZ()
                    );
                    spaceMap.put(global);
                }
            } else {
                // Fleets and space stations are point entities
                spaceMap.put(pos);
            }
        }
    }

    /**
     * Checks if the given global coordinates are occupied by any body's hex.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if any body occupies this position
     */
    public boolean isOccupied(int x, int y) {
        return spaceMap.contains(x, y);
    }

    // ---- Entry access ----

    /**
     * Returns all bodies in this solar system with their 3D positions.
     *
     * @return unmodifiable collection of SystemBody entries (body + position + type)
     */
    public java.util.Collection<SystemBody> getAllEntries() {
        return java.util.Collections.unmodifiableCollection(positionIndex.values());
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolarSystemEntity that = (SolarSystemEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("SolarSystemEntity{id=%d, range=%d, stars=%d, planets=%d, belts=%d, gasGiants=%d, nebulas=%d, blackHoles=%d, comets=%d, fleets=%d, stations=%d, totalHexes=%d}",
                id, range, starRegistry.count(), planetRegistry.count(), asteroidBeltRegistry.count(),
                gasGiantRegistry.count(), nebulaRegistry.count(), blackHoleRegistry.count(),
                cometRegistry.count(), fleetRegistry.count(), spaceStationRegistry.count(), getTotalHexCount());
    }
}
