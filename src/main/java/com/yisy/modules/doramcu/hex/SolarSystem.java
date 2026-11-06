package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A manager for multiple solar systems positioned in 3D hex space.
 * <p>
 * SolarSystem sits at the top of the 3D hierarchy: Hex3D → Hex3DMap → Star/Planet → SolarSystemEntity → SolarSystem.
 * It manages a collection of {@link SolarSystemEntity} instances, each positioned at a specific
 * {@link Hex3D} coordinate in galactic space.
 * <p>
 * Each solar system retains its own local coordinate system (centered at origin), while the
 * galactic manager tracks global positions for spatial queries like finding solar systems within
 * a range or looking up neighbors across solar system boundaries.
 * <p>
 * This parallels the 2D hierarchy: Hex → HexMap → Sector → QuadrantMap.
 */
public class SolarSystem {

    private final int id;
    private final Map<Integer, SolarSystemEntity> systems;
    private final Map<Hex3D, GalacticEntry> positionIndex;

    /**
     * Represents a solar system entry in the galactic map.
     */
    public static class GalacticEntry {
        private final SolarSystemEntity system;
        private final Hex3D position;

        public GalacticEntry(SolarSystemEntity system, Hex3D position) {
            if (system == null) {
                throw new IllegalArgumentException("System must not be null");
            }
            if (position == null) {
                throw new IllegalArgumentException("Position must be null");
            }
            this.system = system;
            this.position = position;
        }

        public SolarSystemEntity getSystem() {
            return system;
        }

        public Hex3D getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return String.format("GalacticEntry{system=%s, position=%s}", system, position);
        }
    }

    /**
     * Creates a new solar system manager with the given ID.
     *
     * @param id the unique identifier for this galactic manager
     */
    public SolarSystem(int id) {
        this.id = id;
        this.systems = new HashMap<>();
        this.positionIndex = new HashMap<>();
    }

    // ---- Accessors ----

    /**
     * @return the unique identifier for this galactic manager
     */
    public int getId() {
        return id;
    }

    /**
     * @return the number of solar systems managed
     */
    public int size() {
        return systems.size();
    }

    // ---- Solar system operations ----

    /**
     * Adds a solar system at the given global position.
     *
     * @param system the solar system to add
     * @param position the global position in galactic space
     * @return the previous entry at that position, or null
     */
    public GalacticEntry addSystem(SolarSystemEntity system, Hex3D position) {
        if (system == null) {
            throw new IllegalArgumentException("System must not be null");
        }
        GalacticEntry removed = positionIndex.remove(position);
        systems.put(system.getId(), system);
        GalacticEntry entry = new GalacticEntry(system, position);
        positionIndex.put(position, entry);
        return removed;
    }

    /**
     * Retrieves a solar system by its ID.
     *
     * @param systemId the solar system's unique identifier
     * @return the solar system, or null if not present
     */
    public SolarSystemEntity getSystem(int systemId) {
        return systems.get(systemId);
    }

    /**
     * Removes a solar system by its ID.
     *
     * @param systemId the solar system's unique identifier
     * @return the removed solar system, or null if not present
     */
    public SolarSystemEntity removeSystem(int systemId) {
        SolarSystemEntity system = systems.remove(systemId);
        if (system != null) {
            positionIndex.values().removeIf(entry -> entry.getSystem() == system);
        }
        return system;
    }

    /**
     * @return all solar systems managed (unmodifiable map)
     */
    public Map<Integer, SolarSystemEntity> getAllSystems() {
        return Collections.unmodifiableMap(systems);
    }

    // ---- Position-based queries ----

    /**
     * Retrieves the solar system at the given global position.
     *
     * @param position the global position
     * @return the galactic entry, or null if no system is at that position
     */
    public GalacticEntry getAt(Hex3D position) {
        return positionIndex.get(position);
    }

    /**
     * @return all solar systems with their positions (unmodifiable collection)
     */
    public Collection<GalacticEntry> getAllEntries() {
        return Collections.unmodifiableCollection(positionIndex.values());
    }

    // ---- Spatial queries ----

    /**
     * Returns all solar systems within the given distance from the center.
     *
     * @param maxDistance the maximum distance from the galactic center
     * @return list of galactic entries within range
     */
    public List<GalacticEntry> findByDistance(int maxDistance) {
        Hex3D center = new Hex3D(0, 0, 0);
        return positionIndex.values().stream()
                .filter(entry -> center.distance(entry.getPosition()) <= maxDistance)
                .collect(Collectors.toList());
    }

    /**
     * Returns all solar systems within the given distance from a specific position.
     *
     * @param from the reference position
     * @param maxDistance the maximum distance
     * @return list of galactic entries within range
     */
    public List<GalacticEntry> findByDistance(Hex3D from, int maxDistance) {
        return positionIndex.values().stream()
                .filter(entry -> from.distance(entry.getPosition()) <= maxDistance)
                .collect(Collectors.toList());
    }

    /**
     * Returns the nearest solar system to the given position.
     *
     * @param from the reference position
     * @return the nearest galactic entry, or null if no systems exist
     */
    public GalacticEntry findNearest(Hex3D from) {
        return positionIndex.values().stream()
                .min(Comparator.comparingInt(entry -> from.distance(entry.getPosition())))
                .orElse(null);
    }

    // ---- Aggregated statistics ----

    /**
     * Returns the total number of celestial bodies across all solar systems.
     *
     * @return total body count
     */
    public int getTotalBodyCount() {
        return systems.values().stream()
                .mapToInt(SolarSystemEntity::getBodyCount)
                .sum();
    }

    /**
     * Returns the total number of stars across all solar systems.
     *
     * @return total star count
     */
    public int getTotalStarCount() {
        return systems.values().stream()
                .mapToInt(SolarSystemEntity::getStarCount)
                .sum();
    }

    /**
     * Returns the total number of planets across all solar systems.
     *
     * @return total planet count
     */
    public int getTotalPlanetCount() {
        return systems.values().stream()
                .mapToInt(SolarSystemEntity::getPlanetCount)
                .sum();
    }

    /**
     * Returns the total number of fleets across all solar systems.
     *
     * @return total fleet count
     */
    public int getTotalFleetCount() {
        return systems.values().stream()
                .mapToInt(SolarSystemEntity::getFleetCount)
                .sum();
    }

    /**
     * Returns the total number of ships across all fleets in all solar systems.
     *
     * @return total ship count
     */
    public int getTotalShipCount() {
        return systems.values().stream()
                .mapToInt(ss -> ss.getAllFleets().values().stream()
                        .mapToInt(Fleet::getShipCount)
                        .sum())
                .sum();
    }

    /**
     * Returns the total number of hex cells across all solar systems.
     *
     * @return total hex count
     */
    public int getTotalHexCount() {
        return systems.values().stream()
                .mapToInt(SolarSystemEntity::getTotalHexCount)
                .sum();
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolarSystem that = (SolarSystem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("SolarSystem{id=%d, systems=%d, bodies=%d, fleets=%d, ships=%d, totalHexes=%d}",
                id, systems.size(), getTotalBodyCount(), getTotalFleetCount(), getTotalShipCount(), getTotalHexCount());
    }
}
