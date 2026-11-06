package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the Facilities component of an Empire.
 * <p>
 * The Facilities module handles all ground-based infrastructure: planet facilities,
 * space stations, resource production, and industrial capacity. Each planet has
 * its own set of facilities that contribute to the empire's overall output.
 * </p>
 * <p>
 * Facilities are organized by planet, with each planet maintaining its own
 * collection of structures like shipyards, mining facilities, power plants,
 * military bases, and research labs.
 * </p>
 */
public class EmpireFacilities {

    private final int empireId;
    /** Planet ID -> list of facilities on that planet */
    private final Map<Integer, List<PlanetFacility>> planetFacilities;
    private final List<SpaceStation> spaceStations;
    private int nextFacilityId;

    /**
     * Creates a new facilities manager for the given empire.
     *
     * @param empireId the ID of the owning empire
     */
    public EmpireFacilities(int empireId) {
        this.empireId = empireId;
        this.planetFacilities = new java.util.HashMap<>();
        this.spaceStations = new ArrayList<>();
        this.nextFacilityId = 1;
    }

    // ---- Accessors ----

    /** Returns the empire ID. */
    public int getEmpireId() {
        return empireId;
    }

    /** Returns an unmodifiable list of all space stations. */
    public List<SpaceStation> getSpaceStations() {
        return Collections.unmodifiableList(spaceStations);
    }

    /** Returns the total number of space stations. */
    public int getSpaceStationCount() {
        return spaceStations.size();
    }

    // ---- Planet Facility Management ----

    /**
     * Builds a new facility on the given planet.
     *
     * @param planetId the planet ID
     * @param facilityType the type of facility to build
     * @return the newly built facility
     */
    public PlanetFacility buildFacility(int planetId, StartingFacility facilityType) {
        if (facilityType == null) {
            throw new IllegalArgumentException("Facility type must not be null");
        }
        PlanetFacility facility = new PlanetFacility(nextFacilityId++, facilityType);
        planetFacilities.computeIfAbsent(planetId, k -> new ArrayList<>()).add(facility);
        return facility;
    }

    /**
     * Returns all facilities on the given planet.
     *
     * @param planetId the planet ID
     * @return unmodifiable list of facilities
     */
    public List<PlanetFacility> getFacilitiesOnPlanet(int planetId) {
        List<PlanetFacility> facilities = planetFacilities.get(planetId);
        if (facilities == null) return Collections.emptyList();
        return Collections.unmodifiableList(facilities);
    }

    /**
     * Returns all facilities of the given type across all planets.
     *
     * @param facilityType the facility type
     * @return list of matching facilities
     */
    public List<PlanetFacility> getFacilitiesByType(StartingFacility facilityType) {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .filter(f -> f.getFacilityType() == facilityType)
                .collect(Collectors.toList());
    }

    /**
     * Removes all facilities on a planet (e.g., when planet is lost).
     *
     * @param planetId the planet ID
     * @return the number of facilities removed
     */
    public int removeFacilitiesOnPlanet(int planetId) {
        List<PlanetFacility> removed = planetFacilities.remove(planetId);
        return removed != null ? removed.size() : 0;
    }

    /**
     * Returns the total number of facilities across all planets.
     */
    public int getTotalFacilityCount() {
        return planetFacilities.values().stream().mapToInt(List::size).sum();
    }

    // ---- Space Station Management ----

    /**
     * Adds a space station to the empire.
     *
     * @param station the station to add
     */
    public void addSpaceStation(SpaceStation station) {
        if (station == null) {
            throw new IllegalArgumentException("Station must not be null");
        }
        spaceStations.add(station);
    }

    /**
     * Removes a space station from the empire.
     *
     * @param station the station to remove
     * @return true if removed
     */
    public boolean removeSpaceStation(SpaceStation station) {
        return spaceStations.remove(station);
    }

    // ---- Resource Production ----

    /**
     * Returns the total mineral production from all facilities.
     */
    public int getTotalMineralProduction() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveMineralBonus)
                .sum();
    }

    /**
     * Returns the total energy production from all facilities.
     */
    public int getTotalEnergyProduction() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveEnergyBonus)
                .sum();
    }

    /**
     * Returns the total food production from all facilities.
     */
    public int getTotalFoodProduction() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveFoodBonus)
                .sum();
    }

    /**
     * Returns the total research output from all facilities.
     */
    public int getTotalResearchOutput() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveResearchBonus)
                .sum();
    }

    /**
     * Returns the total defense bonus from all facilities.
     */
    public int getTotalDefenseBonus() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveDefenseBonus)
                .sum();
    }

    /**
     * Returns the total credit income from all facilities.
     */
    public int getTotalCreditIncome() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveCreditIncome)
                .sum();
    }

    /**
     * Returns the total upkeep cost for all facilities.
     */
    public int getTotalUpkeepCost() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .mapToInt(PlanetFacility::getEffectiveUpkeepCost)
                .sum();
    }

    /**
     * Returns the number of planets with facilities.
     */
    public int getPlanetCountWithFacilities() {
        return planetFacilities.size();
    }

    /**
     * Returns a breakdown of facilities by type.
     */
    public Map<StartingFacility, Integer> getFacilityTypeBreakdown() {
        return planetFacilities.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        PlanetFacility::getFacilityType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    // ---- Industrial Capacity ----

    /**
     * Returns the total industrial capacity (sum of mining facility levels).
     */
    public int getTotalIndustrialCapacity() {
        return getFacilitiesByType(StartingFacility.MINING_FACILITY).stream()
                .mapToInt(PlanetFacility::getLevel)
                .sum();
    }

    /**
     * Returns the number of planets with mining facilities.
     */
    public int getIndustrialPlanetCount() {
        return (int) planetFacilities.entrySet().stream()
                .filter(e -> e.getValue().stream()
                        .anyMatch(f -> f.getFacilityType() == StartingFacility.MINING_FACILITY))
                .count();
    }

    // ---- End Turn Processing ----

    /**
     * Processes end-of-turn effects for all facilities.
     */
    public void endTurn() {
        // Facilities are passive; no per-turn processing needed beyond resource collection
    }

    @Override
    public String toString() {
        return String.format("EmpireFacilities{empireId=%d, facilities=%d, stations=%d, planets=%d}",
                empireId, getTotalFacilityCount(), getSpaceStationCount(), getPlanetCountWithFacilities());
    }
}
