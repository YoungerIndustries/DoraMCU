package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.*;

import java.util.*;

/**
 * Manages facility construction using the resource pipeline's module system.
 * <p>
 * Facilities (space stations, planetary upgrades) are built from construction modules
 * produced by the resource pipeline. This manager handles:
 * <ul>
 *   <li>Calculating module requirements for facility types</li>
 *   <li>Checking if a player can afford a facility</li>
 *   <li>Queuing facility construction orders</li>
 *   <li>Processing completed facility construction</li>
 * </ul>
 * </p>
 */
public class FacilityConstructionManager {

    private final GameContext context;

    /**
     * Creates a new FacilityConstructionManager.
     *
     * @param context the shared game context
     */
    public FacilityConstructionManager(GameContext context) {
        this.context = context;
    }

    // ---- Module Requirements ----

    /**
     * Calculates the construction module requirements for a space station.
     * <p>
     * Different station types require different module quantities:
     * <ul>
     *   <li>MILITARY: 3 HULL + 2 WEAPON + 2 SHIELD</li>
     *   <li>SHIPYARD: 4 HULL + 1 ENGINE</li>
     *   <li>MINING: 2 HULL + 2 FACILITY</li>
     *   <li>RESEARCH: 2 HULL + 2 FACILITY + 1 ENGINE</li>
     *   <li>TRADE: 2 HULL + 1 FACILITY + 1 ENGINE</li>
     *   <li>COLONY: 3 HULL + 2 FACILITY</li>
     * </ul>
     * </p>
     *
     * @param stationType the type of station to build
     * @return map of construction module types to required quantities
     */
    public Map<ConstructionModuleType, Integer> calculateStationRequirements(SpaceStationType stationType) {
        Map<ConstructionModuleType, Integer> requirements = new EnumMap<>(ConstructionModuleType.class);

        switch (stationType) {
            case MILITARY:
                requirements.put(ConstructionModuleType.HULL_MODULE, 3);
                requirements.put(ConstructionModuleType.WEAPON_MODULE, 2);
                requirements.put(ConstructionModuleType.SHIELD_MODULE, 2);
                break;
            case SHIPYARD:
                requirements.put(ConstructionModuleType.HULL_MODULE, 4);
                requirements.put(ConstructionModuleType.ENGINE_MODULE, 1);
                break;
            case MINING:
                requirements.put(ConstructionModuleType.HULL_MODULE, 2);
                requirements.put(ConstructionModuleType.FACILITY_MODULE, 2);
                break;
            case RESEARCH:
                requirements.put(ConstructionModuleType.HULL_MODULE, 2);
                requirements.put(ConstructionModuleType.FACILITY_MODULE, 2);
                requirements.put(ConstructionModuleType.ENGINE_MODULE, 1);
                break;
            case TRADE:
                requirements.put(ConstructionModuleType.HULL_MODULE, 2);
                requirements.put(ConstructionModuleType.FACILITY_MODULE, 1);
                requirements.put(ConstructionModuleType.ENGINE_MODULE, 1);
                break;
            case COLONY:
                requirements.put(ConstructionModuleType.HULL_MODULE, 3);
                requirements.put(ConstructionModuleType.FACILITY_MODULE, 2);
                break;
            default:
                break;
        }

        return requirements;
    }

    /**
     * Calculates the construction module requirements for a planetary facility upgrade.
     * <p>
     * Planetary facilities require fewer modules than space stations:
     * <ul>
     *   <li>SHIPYARD: 2 HULL + 1 ENGINE</li>
     *   <li>MINING: 1 HULL + 1 FACILITY</li>
     *   <li>RESEARCH: 1 HULL + 1 FACILITY</li>
     * </ul>
     * </p>
     *
     * @param facility the facility type to build
     * @return map of construction module types to required quantities
     */
    public Map<ConstructionModuleType, Integer> calculatePlanetFacilityRequirements(StartingFacility facility) {
        Map<ConstructionModuleType, Integer> requirements = new EnumMap<>(ConstructionModuleType.class);

        switch (facility) {
            case SHIPYARD:
                requirements.put(ConstructionModuleType.HULL_MODULE, 2);
                requirements.put(ConstructionModuleType.ENGINE_MODULE, 1);
                break;
            case MINING_FACILITY:
                requirements.put(ConstructionModuleType.HULL_MODULE, 1);
                requirements.put(ConstructionModuleType.FACILITY_MODULE, 1);
                break;
            case RESEARCH_LAB:
                requirements.put(ConstructionModuleType.HULL_MODULE, 1);
                requirements.put(ConstructionModuleType.FACILITY_MODULE, 1);
                break;
            default:
                break;
        }

        return requirements;
    }

    // ---- Affordability Checks ----

    /**
     * Checks if a player can build a space station with the given module requirements.
     *
     * @param pipelineManager the player's resource pipeline manager
     * @param stationType the type of station to build
     * @return true if sufficient modules are available
     */
    public boolean canBuildStation(ResourcePipelineManager pipelineManager, SpaceStationType stationType) {
        if (pipelineManager == null) {
            return false;
        }
        Map<ConstructionModuleType, Integer> requirements = calculateStationRequirements(stationType);
        return pipelineManager.canBuildWithModules(requirements);
    }

    /**
     * Checks if a player can build a planetary facility with the given module requirements.
     *
     * @param pipelineManager the player's resource pipeline manager
     * @param facility the facility type to build
     * @return true if sufficient modules are available
     */
    public boolean canBuildPlanetFacility(ResourcePipelineManager pipelineManager, StartingFacility facility) {
        if (pipelineManager == null) {
            return false;
        }
        Map<ConstructionModuleType, Integer> requirements = calculatePlanetFacilityRequirements(facility);
        return pipelineManager.canBuildWithModules(requirements);
    }

    // ---- Instant Construction (Legacy) ----

    /**
     * Builds a space station instantly by consuming modules from the pipeline.
     * <p>
     * The station is created and added to the game context immediately.
     * For multi-turn construction, use {@link #queueStation(Player, SpaceStationType, SolarSystemEntity, ResourcePipelineManager)}.
     * </p>
     *
     * @param owner the player who owns the station
     * @param stationType the type of station to build
     * @param homeSystem the solar system where the station is placed
     * @param pipelineManager the player's resource pipeline manager
     * @return the created space station, or null if modules are insufficient
     */
    public SpaceStation buildStation(Player owner, SpaceStationType stationType,
                                      SolarSystemEntity homeSystem,
                                      ResourcePipelineManager pipelineManager) {
        if (owner == null || stationType == null || pipelineManager == null) {
            return null;
        }

        Map<ConstructionModuleType, Integer> requirements = calculateStationRequirements(stationType);
        if (!pipelineManager.canBuildWithModules(requirements)) {
            return null;
        }

        // Consume modules from pipeline
        pipelineManager.consumeModules(requirements);

        // Create the station
        int stationId = context.getNextPlanetIdAndIncrement();
        SpaceStation station = new SpaceStation(stationId, stationType);
        station.setHomeSystem(homeSystem);
        return station;
    }

    /**
     * Adds a facility to a planet instantly by consuming modules from the pipeline.
     *
     * @param planet the planet to add the facility to
     * @param facility the facility type to build
     * @param pipelineManager the player's resource pipeline manager
     * @return true if the facility was successfully added
     */
    public boolean buildPlanetFacility(Planet planet, StartingFacility facility,
                                        ResourcePipelineManager pipelineManager) {
        if (planet == null || facility == null || pipelineManager == null) {
            return false;
        }

        Map<ConstructionModuleType, Integer> requirements = calculatePlanetFacilityRequirements(facility);
        if (!pipelineManager.canBuildWithModules(requirements)) {
            return false;
        }

        // Consume modules from pipeline
        pipelineManager.consumeModules(requirements);

        // Add the facility to the planet
        planet.addStartingFacility(facility);
        return true;
    }

    // ---- Queued Construction (Multi-Turn) ----

    /**
     * Queues a space station for multi-turn construction.
     * <p>
     * Modules are consumed immediately, and the station is created when
     * the construction order completes after the default number of turns.
     * The caller must process completed orders via {@link #completeStation(ConstructionOrder, Player, SolarSystemEntity)}.
     * </p>
     *
     * @param owner the player who owns the station
     * @param stationType the type of station to build
     * @param homeSystem the solar system where the station will be placed
     * @param pipelineManager the player's resource pipeline manager
     * @return the construction order, or null if modules are insufficient
     */
    public ConstructionOrder queueStation(Player owner, SpaceStationType stationType,
                                           SolarSystemEntity homeSystem,
                                           ResourcePipelineManager pipelineManager) {
        if (owner == null || stationType == null || pipelineManager == null) {
            return null;
        }

        Map<ConstructionModuleType, Integer> requirements = calculateStationRequirements(stationType);
        return pipelineManager.queueStation(stationType,
                homeSystem,
                requirements,
                ResourcePipelineManager.DEFAULT_STATION_TURNS);
    }

    /**
     * Queues a planetary facility for multi-turn construction.
     * <p>
     * Modules are consumed immediately, and the facility is added when
     * the construction order completes.
     * The caller must process completed orders via {@link #completePlanetFacility(ConstructionOrder, Planet)}.
     * </p>
     *
     * @param planet the planet to build the facility on
     * @param facility the facility type to build
     * @param pipelineManager the player's resource pipeline manager
     * @return the construction order, or null if modules are insufficient
     */
    public ConstructionOrder queuePlanetFacility(Planet planet, StartingFacility facility,
                                                  ResourcePipelineManager pipelineManager) {
        if (planet == null || facility == null || pipelineManager == null) {
            return null;
        }

        Map<ConstructionModuleType, Integer> requirements = calculatePlanetFacilityRequirements(facility);
        return pipelineManager.queuePlanetFacility(facility,
                planet.getId(),
                requirements,
                ResourcePipelineManager.DEFAULT_FACILITY_TURNS);
    }

    /**
     * Completes a space station construction order by creating the station.
     * The station is added to the game's space station list and assigned to the owner.
     *
     * @param order the completed construction order
     * @param owner the player who owns the station
     * @param homeSystem the solar system where the station is placed
     * @return the created space station
     */
    public SpaceStation completeStation(ConstructionOrder order, Player owner, SolarSystemEntity homeSystem) {
        if (!order.isCompleted() || order.getConstructionType() != ConstructionOrder.ConstructionType.SPACE_STATION) {
            return null;
        }
        int stationId = context.getNextPlanetIdAndIncrement();
        SpaceStation station = new SpaceStation(stationId, order.getStationType());
        station.setOwner(owner);
        station.setHomeSystem(homeSystem);
        context.getSpaceStations().add(station);
        return station;
    }

    /**
     * Completes a planetary facility construction order by adding the facility.
     *
     * @param order the completed construction order
     * @param planet the planet to add the facility to
     * @return true if the facility was added
     */
    public boolean completePlanetFacility(ConstructionOrder order, Planet planet) {
        if (!order.isCompleted() || order.getConstructionType() != ConstructionOrder.ConstructionType.PLANET_FACILITY) {
            return false;
        }
        planet.addStartingFacility(order.getPlanetFacility());
        return true;
    }

    // ---- Module Summary ----

    /**
     * Returns a summary of module requirements for all station types.
     *
     * @return map of station type names to their module requirements
     */
    public Map<String, Map<ConstructionModuleType, Integer>> getAllStationRequirements() {
        Map<String, Map<ConstructionModuleType, Integer>> allRequirements = new LinkedHashMap<>();
        for (SpaceStationType type : SpaceStationType.values()) {
            allRequirements.put(type.name(), calculateStationRequirements(type));
        }
        return allRequirements;
    }

    /**
     * Returns a summary of module requirements for all planetary facilities.
     *
     * @return map of facility names to their module requirements
     */
    public Map<String, Map<ConstructionModuleType, Integer>> getAllFacilityRequirements() {
        Map<String, Map<ConstructionModuleType, Integer>> allRequirements = new LinkedHashMap<>();
        for (StartingFacility facility : StartingFacility.values()) {
            Map<ConstructionModuleType, Integer> reqs = calculatePlanetFacilityRequirements(facility);
            if (!reqs.isEmpty()) {
                allRequirements.put(facility.name(), reqs);
            }
        }
        return allRequirements;
    }
}
