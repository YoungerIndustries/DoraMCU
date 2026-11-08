package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.SubSectorRepository;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.GlobalClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds DTOs for game state responses.
 * <p>
 * Extracted from {@link GameSessionService} to reduce the monolithic service class size.
 * This class contains all methods that transform game domain objects into API response DTOs.
 * </p>
 */
@Component
public class GameStateBuilder {

    private static final Logger log = LoggerFactory.getLogger(GameStateBuilder.class);

    private final SolarSystemRepository solarSystemRepository;
    private final GalaxyRepository galaxyRepository;
    private final SubSectorRepository subSectorRepository;
    private final TurnScheduler turnScheduler;
    private final AiDelegationService aiDelegationService;

    public GameStateBuilder(SolarSystemRepository solarSystemRepository,
                            GalaxyRepository galaxyRepository,
                            SubSectorRepository subSectorRepository,
                            @Lazy TurnScheduler turnScheduler,
                            @Lazy AiDelegationService aiDelegationService) {
        this.solarSystemRepository = solarSystemRepository;
        this.galaxyRepository = galaxyRepository;
        this.subSectorRepository = subSectorRepository;
        this.turnScheduler = turnScheduler;
        this.aiDelegationService = aiDelegationService;
    }

    /**
     * Builds a list of FleetDto for the current player's fleets.
     */
    public List<FleetDto> buildFleetList(Player currentPlayer, Game game) {
        List<FleetDto> fleetDtos = new ArrayList<>();
        // Build a map from system ID to subsector coordinates for fleet positioning
        Map<Integer, int[]> systemToSubSector = new HashMap<>();
        // Build a map from planet ID to system ID for fleet system matching
        Map<Integer, Integer> planetToSystem = new HashMap<>();
        Galaxy galaxy = game.getGalaxy(0);
        if (galaxy != null) {
            for (int r = 0; r < Galaxy.GRID_SIZE; r++) {
                for (int c = 0; c < Galaxy.GRID_SIZE; c++) {
                    Galaxy.Sector sector = galaxy.getSector(r, c);
                    if (sector != null) {
                        for (SubSector subSector : sector.getAllSubSectors()) {
                            for (GalacticEntry entry : subSector.getAllEntries()) {
                                com.yisy.modules.doramcu.hex.SolarSystem system = entry.getSystem();
                                int systemId = system.getId();
                                systemToSubSector.put(systemId,
                                        new int[]{subSector.getSubRow(), subSector.getSubCol()});
                                // Map all planets in this system to the system ID
                                for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
                                    for (com.yisy.modules.doramcu.hex.Planet planet : entity.getAllPlanets().values()) {
                                        planetToSystem.put(planet.getId(), systemId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Integer fleetId : currentPlayer.getOwnedFleetIds()) {
            Fleet fleet = game.getFleets().get(fleetId);
            if (fleet == null || fleet.isEmpty()) {
                continue;
            }
            Hex3D pos = fleet.getPosition();
            FleetDto dto = new FleetDto();
            dto.setId(fleet.getId());
            dto.setName(fleet.getName());
            dto.setPositionX(pos != null ? pos.getX() : 0);
            dto.setPositionY(pos != null ? pos.getY() : 0);
            dto.setPositionZ(pos != null ? pos.getZ() : 0);
            // Resolve subsector coordinates from fleet's home system
            int[] subCoords = resolveFleetSubSector(fleet, systemToSubSector);
            dto.setSubSectorRow(subCoords[0]);
            dto.setSubSectorCol(subCoords[1]);
            // Resolve system ID from fleet's home planet
            Integer fleetSystemId = null;
            if (fleet.getHomeSystem() != null) {
                fleetSystemId = planetToSystem.get(fleet.getHomeSystem().getId());
            }
            dto.setSystemId(fleetSystemId);
            dto.setMDriveSpeed(fleet.getFleetMDriveSpeed());
            dto.setJDriveSpeed(fleet.getFleetJDriveSpeed());
            dto.setEffectiveMDriveSpeed(fleet.getEffectiveFleetMDriveSpeed());
            dto.setEffectiveJDriveSpeed(fleet.getEffectiveFleetJDriveSpeed());
            dto.setHealthRatio(fleet.getFleetHealthRatio());
            dto.setShipCount(fleet.getActiveShips().size());
            dto.setTotalDamage(fleet.getTotalDamage());
            dto.setCurrentShieldPoints(fleet.getCurrentShieldPoints());
            dto.setTotalShieldPoints(fleet.getTotalShieldPoints());
            dto.setCurrentHullPoints(fleet.getCurrentHullPoints());
            dto.setTotalHullPoints(fleet.getTotalHullPoints());
            // Build ship list
            List<ShipDto> shipDtos = new ArrayList<>();
            for (Ship ship : fleet.getActiveShips()) {
                ShipDto shipDto = new ShipDto();
                shipDto.setId(ship.getId());
                shipDto.setName(ship.getName());
                shipDto.setShipType(ship.getShipType().name());
                shipDto.setMDriveSpeed(ship.getMDriveSpeed());
                shipDto.setJDriveSpeed(ship.getJDriveSpeed());
                shipDto.setHullHealthRatio(ship.getHullHealthRatio());
                shipDto.setCurrentHullPoints(ship.getCurrentHullPoints());
                shipDto.setMaxHullPoints(ship.getMaxHullPoints());
                shipDto.setCurrentShieldPoints(ship.getCurrentShieldPoints());
                shipDto.setMaxShieldPoints(ship.getMaxShieldPoints());
                shipDto.setTotalDamage(ship.getTotalDamage());
                shipDto.setDestroyed(ship.isDestroyed());
                shipDtos.add(shipDto);
            }
            dto.setShips(shipDtos);
            fleetDtos.add(dto);
        }
        return fleetDtos;
    }

    /**
     * Builds an EmpireSummary DTO from the current player's Game and Player data.
     * Takes Game directly instead of sessions map for O(1) empire lookup.
     */
    public EmpireSummary buildEmpireSummary(Player player, Game game) {
        EmpireSummary summary = new EmpireSummary();
        int empireId = player.getEmpireId() != null ? player.getEmpireId() : 0;
        summary.setEmpireId(empireId);
        summary.setEmpireName(player.getEmpireName() != null ? player.getEmpireName() : "Unnamed Empire");
        summary.setGovernmentType(player.getGovernmentType() != null ? player.getGovernmentType().name() : "DEMOCRACY");
        summary.setEthos(player.getEthos() != null ? player.getEthos().name() : "MATERIALIST");
        summary.setPlanetCount(player.getOwnedPlanetIds().size());
        summary.setCapitalPlanetId(player.getHomePlanetId());
        summary.setFleetCount(player.getOwnedFleetIds().size());

        // Populate location information from HomeLocation
        HomeLocation homeLoc = player.getHomeLocation();
        if (homeLoc != null) {
            summary.setCapitalSystemId(homeLoc.getSystemId());
            summary.setCapitalSectorRow(homeLoc.getSectorRow());
            summary.setCapitalSectorCol(homeLoc.getSectorCol());
            summary.setCapitalSubSectorRow(homeLoc.getSubSectorRow());
            summary.setCapitalSubSectorCol(homeLoc.getSubSectorCol());
        }

        // Look up the hex Empire for component data (O(1) direct lookup)
        Empire hexEmpire = game.getEmpire(empireId);
        if (hexEmpire != null) {
            // Army component
            EmpireArmy army = hexEmpire.getArmy();
            summary.setArmyUnitCount(army.getUnitCount());
            summary.setArmyTotalOffense(army.getTotalOffense());
            summary.setArmyTotalDefense(army.getTotalDefense());
            summary.setArmySupplyCost(army.getTotalSupplyCost());

            // Navy component
            EmpireNavy navy = hexEmpire.getNavy();
            summary.setNavyFleetCount(navy.getFleetCount());
            summary.setNavyShipCount(navy.getTotalShipCount());
            summary.setNavyTotalDamage(navy.getTotalDamage());
            summary.setNavyTotalShieldPoints(navy.getTotalShieldPoints());
            summary.setNavyTotalHullPoints(navy.getTotalHullPoints());

            // Facilities component
            EmpireFacilities facilities = hexEmpire.getFacilities();
            summary.setFacilityCount(facilities.getTotalFacilityCount());
            summary.setSpaceStationCount(facilities.getSpaceStationCount());
            summary.setMineralProduction(facilities.getTotalMineralProduction());
            summary.setEnergyProduction(facilities.getTotalEnergyProduction());
            summary.setFoodProduction(facilities.getTotalFoodProduction());
            summary.setResearchOutput(facilities.getTotalResearchOutput());
            summary.setFacilityDefenseBonus(facilities.getTotalDefenseBonus());
            summary.setCreditIncome(facilities.getTotalCreditIncome());

            // Industrial capacity from facilities
            summary.setIndustrialCapacity(facilities.getTotalIndustrialCapacity());
            summary.setIndustrialPlanetCount(facilities.getIndustrialPlanetCount());
        }

        // Empire status fields (Game passed directly — no session search needed)
        summary.setEmpireIntegrity(game.getEmpireIntegrity());
        summary.setMilitaryPower(game.getTotalMilitaryPower(player));
        summary.setActiveBattlesCount(game.getActiveBattles().size());

        // Ship queue size from player
        summary.setShipQueueSize(player.getShipQueue() != null ? player.getShipQueue().size() : 0);

        return summary;
    }

    /**
     * Builds a ResourcePipelineSummary DTO from the given ResourcePipelineManager.
     * Converts enum-keyed maps to String-keyed maps for JSON serialization.
     */
    public ResourcePipelineSummary buildPipelineSummary(ResourcePipelineManager pipeline) {
        Map<String, Integer> rawResources = enumMapToStringMap(pipeline.getRawStorage().getInventory());
        Map<String, Integer> refinedResources = enumMapToStringMap(pipeline.getRefinedStorage().getInventory());
        Map<String, Integer> modules = enumMapToStringMap(pipeline.getModuleStorage().getInventory());
        Map<String, Integer> miningRate = enumMapToStringMap(pipeline.getAggregateMiningRate());

        return new ResourcePipelineSummary(
                rawResources,
                refinedResources,
                modules,
                miningRate,
                pipeline.getMiningOperationCount(),
                pipeline.getActiveProcessingCount(),
                pipeline.getActiveAssemblerCount(),
                pipeline.getConstructionQueueSize()
        );
    }

    /**
     * Builds a comprehensive PipelineDetailsDto from the given ResourcePipelineManager.
     */
    public PipelineDetailsDto buildPipelineDetails(ResourcePipelineManager pipeline) {
        PipelineDetailsDto details = new PipelineDetailsDto();
        details.setSummary(buildPipelineSummary(pipeline));

        // Mining operations
        List<MiningOperationDto> miningDtos = new ArrayList<>();
        for (MiningOperation op : pipeline.getMiningOperations().values()) {
            CelestialBody body = op.getCelestialBody();
            MiningOperationDto dto = new MiningOperationDto(
                    body.getId(),
                    body.getName(),
                    body.getClass().getSimpleName(),
                    op.getOwnerEmpireId(),
                    op.getMiningBonus(),
                    enumMapToStringMap(op.getProductionRate()),
                    enumMapToStringMap(op.getStorage().getInventory()),
                    op.getStorage().getTotalCapacity()
            );
            miningDtos.add(dto);
        }
        details.setMiningOperations(miningDtos);

        // Transport orders
        List<TransportOrderDto> transportDtos = new ArrayList<>();
        for (TransportOrder order : pipeline.getActiveTransportOrders()) {
            TransportOrderDto dto = new TransportOrderDto(
                    order.getId(),
                    order.getOwnerEmpireId(),
                    order.getSourceBodyId(),
                    order.getDestinationBodyId(),
                    order.getResourceType().name(),
                    order.getTotalAmount(),
                    order.getAmountPerTurn(),
                    order.getTurnsTotal(),
                    order.getTurnsRemaining(),
                    order.getAmountLoaded(),
                    order.getProgress(),
                    order.isCompleted(),
                    order.isCancelled(),
                    order.isInProgress()
            );
            transportDtos.add(dto);
        }
        details.setTransportOrders(transportDtos);

        // Construction orders
        List<ConstructionOrderDto> constructionDtos = new ArrayList<>();
        for (ConstructionOrder order : pipeline.getConstructionQueue()) {
            ConstructionOrderDto.ConstructionType type;
            String stationType = null;
            String planetFacility = null;
            Integer targetPlanetId = null;
            Integer targetSystemId = null;

            if (order.getConstructionType() == ConstructionOrder.ConstructionType.SPACE_STATION) {
                type = ConstructionOrderDto.ConstructionType.SPACE_STATION;
                stationType = order.getStationType().name();
                targetSystemId = order.getTargetSystem() != null ? order.getTargetSystem().getId() : null;
            } else {
                type = ConstructionOrderDto.ConstructionType.PLANET_FACILITY;
                planetFacility = order.getPlanetFacility().name();
                targetPlanetId = order.getTargetPlanetId();
            }

            ConstructionOrderDto dto = new ConstructionOrderDto(
                    order.getOrderId(),
                    order.getOwnerEmpireId(),
                    type,
                    stationType,
                    planetFacility,
                    targetPlanetId,
                    targetSystemId,
                    enumMapToStringMap(order.getModuleRequirements()),
                    order.getTotalTurns(),
                    order.getTurnsCompleted(),
                    order.getProgress(),
                    order.isCompleted(),
                    order.isCancelled()
            );
            constructionDtos.add(dto);
        }
        details.setConstructionOrders(constructionDtos);

        // Processing facilities
        List<ProcessingFacilityDto> processingDtos = new ArrayList<>();
        for (ProcessingFacility facility : pipeline.getProcessingFacilities()) {
            ProcessingFacilityDto dto = new ProcessingFacilityDto();
            ProcessingRecipe recipe = facility.getCurrentRecipe();
            if (recipe != null) {
                // Get the first input resource type for display
                Map<RawResourceType, Integer> inputs = recipe.getInputs();
                if (!inputs.isEmpty()) {
                    dto.setInputResource(inputs.keySet().iterator().next().name());
                    dto.setInputPerTurn(inputs.values().iterator().next());
                }
                dto.setOutputResource(recipe.getOutput().name());
                dto.setOutputPerTurn(recipe.getOutputAmount());
            }
            dto.setProcessing(facility.isProcessing());
            dto.setTurnsRemaining(facility.getTurnsRemaining());
            processingDtos.add(dto);
        }
        details.setProcessingFacilities(processingDtos);

        // Module assemblers
        List<ModuleAssemblerDto> assemblerDtos = new ArrayList<>();
        for (ModuleAssembler assembler : pipeline.getAssemblers()) {
            ModuleAssemblerDto dto = new ModuleAssemblerDto();
            dto.setModuleType(assembler.getCurrentModule() != null ? assembler.getCurrentModule().name() : null);
            dto.setAssembling(assembler.isAssembling());
            dto.setTurnsRemaining(assembler.getTurnsRemaining());
            assemblerDtos.add(dto);
        }
        details.setAssemblers(assemblerDtos);

        return details;
    }

    /**
     * Converts an enum-keyed map to a String-keyed map for JSON serialization.
     */
    public <T extends Enum<T>> Map<String, Integer> enumMapToStringMap(Map<T, Integer> enumMap) {
        Map<String, Integer> stringMap = new HashMap<>();
        for (Map.Entry<T, Integer> entry : enumMap.entrySet()) {
            stringMap.put(entry.getKey().name(), entry.getValue());
        }
        return stringMap;
    }

    public List<PlayerSummary> buildPlayerSummaries(Game game, GameSession session) {
        return game.getPlayers().stream().map(p -> {
            // Include pipeline module inventory in resources for the current player
            Map<String, Integer> resources = new HashMap<>();
            ResourcePipelineManager pipeline = game.getPipelineManager(p.getEmpireId());
            if (pipeline != null) {
                resources.putAll(enumMapToStringMap(pipeline.getModuleStorage().getInventory()));
                resources.putAll(enumMapToStringMap(pipeline.getRawStorage().getInventory()));
                resources.putAll(enumMapToStringMap(pipeline.getRefinedStorage().getInventory()));
            }
            PlayerSummary summary = new PlayerSummary(
                    (long) p.getId(),
                    p.getName(),
                    resources,
                    p.getOwnedPlanetIds().size(),
                    p.getOwnedFleetIds().size()
            );
            // Check if this player is an AI player
            for (Long sessionPlayerId : session.getPlayerIds()) {
                if (sessionPlayerId >= 1000 && session.isAiPlayer(sessionPlayerId)) {
                    List<Long> sessionPlayers = new ArrayList<>(session.getPlayerIds());
                    int gamePlayerIndex = sessionPlayers.indexOf(sessionPlayerId);
                    if (gamePlayerIndex == p.getId()) {
                        summary.setAi(true);
                        summary.setDifficulty(session.getAiPlayerDifficulty(sessionPlayerId));
                        break;
                    }
                }
            }
            return summary;
        }).collect(Collectors.toList());
    }

    /**
     * Resolves the subsector coordinates for a fleet by looking up its home system
     * in the system-to-subsector map. Falls back to the fleet's Hex3D position
     * mapped to subsector coordinates, or defaults to (5, 5) (center).
     */
    private int[] resolveFleetSubSector(Fleet fleet, Map<Integer, int[]> systemToSubSector) {
        // Try home system first
        if (fleet.getHomeSystem() != null && systemToSubSector.containsKey(fleet.getHomeSystem().getId())) {
            return systemToSubSector.get(fleet.getHomeSystem().getId());
        }
        // Try home station
        if (fleet.getHomeStation() != null && systemToSubSector.containsKey(fleet.getHomeStation().getId())) {
            return systemToSubSector.get(fleet.getHomeStation().getId());
        }
        // Fall back to center subsector
        return new int[]{5, 5};
    }

    /**
     * Derives a human-readable system type from the SolarSystem's body composition.
     */
    private String deriveSystemType(com.yisy.modules.doramcu.hex.SolarSystem system) {
        int planets = system.getTotalPlanetCount();
        int stars = system.getTotalStarCount();
        int asteroids = 0;
        for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
            asteroids += entity.getAllAsteroids().size();
        }
        return deriveSystemType(stars, planets, asteroids);
    }

    /**
     * Derives a human-readable system type from body counts (used by DB enrichment paths).
     */
    private String deriveSystemType(int stars, int planets, int asteroids) {
        if (stars > 1) {
            return "Multiple Star System";
        } else if (planets >= 5) {
            return "Rich System";
        } else if (planets >= 3) {
            return "Standard System";
        } else if (planets >= 1) {
            return "Sparse System";
        } else if (asteroids > 0) {
            return "Asteroid Field";
        } else {
            return "Star Only";
        }
    }

    public List<SubSectorInfo> buildSubSectorInfo(Game game) {
        return buildSubSectorInfo(game, false);
    }

    public List<SubSectorInfo> buildSubSectorInfo(Game game, boolean summary) {
        List<SubSectorInfo> result = new ArrayList<>();

        // First, try in-memory galaxy data — fast path that avoids expensive DB queries
        Galaxy galaxy = game.getGalaxy(0);
        if (galaxy != null) {
            boolean hasData = false;
            for (Galaxy.Sector sector : galaxy.getAllSectors()) {
                for (SubSector subSector : sector.getAllSubSectors()) {
                    if (!subSector.isGenerated()) {
                        continue;
                    }
                    hasData = true;
                    
                    if (summary) {
                        // Summary mode: return subsector structure with ownership info
                        // Collect ownerEmpireIds from planets only (lightweight)
                        Set<Integer> ownerEmpireIds = new HashSet<>();
                        for (GalacticEntry galEntry : subSector.getAllEntries()) {
                            com.yisy.modules.doramcu.hex.SolarSystem system = galEntry.getSystem();
                            // Skip systems with no planets — avoids iterating bodies in empty systems
                            if (system.getTotalPlanetCount() == 0) {
                                continue;
                            }
                            for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
                                for (com.yisy.modules.doramcu.hex.SystemBody sb : entity.getAllEntries()) {
                                    if (sb.getBody() instanceof com.yisy.modules.doramcu.hex.Planet planet) {
                                        Integer ownerId = planet.getOwnerEmpireId();
                                        if (ownerId != null) ownerEmpireIds.add(ownerId);
                                    }
                                }
                            }
                        }
                        SubSectorInfo ssInfo = new SubSectorInfo();
                        ssInfo.setSubRow(subSector.getSubRow());
                        ssInfo.setSubCol(subSector.getSubCol());
                        ssInfo.setSectorRow(sector.getRow());
                        ssInfo.setSectorCol(sector.getCol());
                        ssInfo.setSystemCount(subSector.getAllEntries().size());
                        ssInfo.setOwnerEmpireIds(ownerEmpireIds);
                        result.add(ssInfo);
                    } else {
                        // Full mode: iterate over all systems in the subsector
                        List<Map<String, Object>> systems = new ArrayList<>();
                        Set<Integer> ownerEmpireIds = new HashSet<>();
                        for (GalacticEntry galEntry : subSector.getAllEntries()) {
                            com.yisy.modules.doramcu.hex.SolarSystem system = galEntry.getSystem();
                            Map<String, Object> sysInfo = new HashMap<>();
                            sysInfo.put("id", system.getId());
                            sysInfo.put("starCount", system.getTotalStarCount());
                            sysInfo.put("planetCount", system.getTotalPlanetCount());
                            sysInfo.put("bodyCount", system.getTotalBodyCount());
                            sysInfo.put("posX", galEntry.getX());
                            sysInfo.put("posY", galEntry.getY());
                            sysInfo.put("posZ", galEntry.getZ());
                            sysInfo.put("systemType", deriveSystemType(system));

                            String systemName = null;
                            List<Map<String, Object>> planetsInSystem = new ArrayList<>();
                            List<Map<String, Object>> starsInSystem = new ArrayList<>();
                            List<Map<String, Object>> moonsInSystem = new ArrayList<>();
                            List<Map<String, Object>> asteroidsInSystem = new ArrayList<>();
                            List<Map<String, Object>> cometsInSystem = new ArrayList<>();
                            List<Map<String, Object>> fleetsInSystem = new ArrayList<>();
                            List<Map<String, Object>> stationsInSystem = new ArrayList<>();
                            List<Map<String, Object>> gasGiantsInSystem = new ArrayList<>();
                            List<Map<String, Object>> asteroidBeltsInSystem = new ArrayList<>();
                            List<Map<String, Object>> nebulasInSystem = new ArrayList<>();
                            List<Map<String, Object>> blackHolesInSystem = new ArrayList<>();
                            for (com.yisy.modules.doramcu.hex.SolarSystemEntity entity : system.getAllSystems().values()) {
                                for (com.yisy.modules.doramcu.hex.SystemBody sb : entity.getAllEntries()) {
                                    com.yisy.modules.doramcu.hex.Hex3D pos = sb.getPosition();
                                    com.yisy.modules.doramcu.hex.SystemBody.BodyType type = sb.getType();
                                    Object body = sb.getBody();
                                    switch (type) {
                                        case STAR:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Star star) {
                                                if (systemName == null) systemName = star.getName();
                                                Map<String, Object> starInfo = new HashMap<>();
                                                starInfo.put("id", star.getId());
                                                starInfo.put("name", star.getName());
                                                starInfo.put("x", pos.getX());
                                                starInfo.put("y", pos.getY());
                                                starInfo.put("z", pos.getZ());
                                                starsInSystem.add(starInfo);
                                            }
                                            break;
                                        case PLANET:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Planet planet) {
                                                Integer ownerId = planet.getOwnerEmpireId();
                                                if (ownerId != null) ownerEmpireIds.add(ownerId);
                                                Map<String, Object> planetInfo = new HashMap<>();
                                                planetInfo.put("id", planet.getId());
                                                planetInfo.put("name", planet.getName());
                                                planetInfo.put("planetType", planet.getPlanetSize() != null ? planet.getPlanetSize().name() : "Unknown");
                                                planetInfo.put("planetSize", planet.getPlanetSize() != null ? planet.getPlanetSize().name() : null);
                                                planetInfo.put("mineralQuality", planet.getMineralQuality() != null ? planet.getMineralQuality().name() : null);
                                                planetInfo.put("isCapital", planet.isCapital());
                                                planetInfo.put("ownerEmpireId", planet.getOwnerEmpireId());
                                                planetInfo.put("x", pos.getX());
                                                planetInfo.put("y", pos.getY());
                                                planetInfo.put("z", pos.getZ());
                                                planetsInSystem.add(planetInfo);
                                            }
                                            break;
                                        case MOON:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Moon moon) {
                                                Map<String, Object> moonInfo = new HashMap<>();
                                                moonInfo.put("id", moon.getId());
                                                moonInfo.put("name", moon.getName());
                                                moonInfo.put("x", pos.getX());
                                                moonInfo.put("y", pos.getY());
                                                moonInfo.put("z", pos.getZ());
                                                moonsInSystem.add(moonInfo);
                                            }
                                            break;
                                        case ASTEROID:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Asteroid asteroid) {
                                                Map<String, Object> astInfo = new HashMap<>();
                                                astInfo.put("id", asteroid.getId());
                                                astInfo.put("name", asteroid.getName());
                                                astInfo.put("x", pos.getX());
                                                astInfo.put("y", pos.getY());
                                                astInfo.put("z", pos.getZ());
                                                asteroidsInSystem.add(astInfo);
                                            }
                                            break;
                                        case COMET:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Comet comet) {
                                                Map<String, Object> cometInfo = new HashMap<>();
                                                cometInfo.put("id", comet.getId());
                                                cometInfo.put("name", comet.getName());
                                                cometInfo.put("x", pos.getX());
                                                cometInfo.put("y", pos.getY());
                                                cometInfo.put("z", pos.getZ());
                                                cometsInSystem.add(cometInfo);
                                            }
                                            break;
                                        case FLEET:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Fleet fleet) {
                                                Map<String, Object> fleetInfo = new HashMap<>();
                                                fleetInfo.put("id", fleet.getId());
                                                fleetInfo.put("name", fleet.getName());
                                                fleetInfo.put("x", pos.getX());
                                                fleetInfo.put("y", pos.getY());
                                                fleetInfo.put("z", pos.getZ());
                                                fleetsInSystem.add(fleetInfo);
                                            }
                                            break;
                                        case SPACE_STATION:
                                            if (body instanceof com.yisy.modules.doramcu.hex.SpaceStation station) {
                                                Map<String, Object> stationInfo = new HashMap<>();
                                                stationInfo.put("id", station.getId());
                                                stationInfo.put("name", station.getName());
                                                stationInfo.put("x", pos.getX());
                                                stationInfo.put("y", pos.getY());
                                                stationInfo.put("z", pos.getZ());
                                                stationsInSystem.add(stationInfo);
                                            }
                                            break;
                                        case GAS_GIANT:
                                            if (body instanceof com.yisy.modules.doramcu.hex.GasGiant gasGiant) {
                                                Map<String, Object> gasGiantInfo = new HashMap<>();
                                                gasGiantInfo.put("id", gasGiant.getId());
                                                gasGiantInfo.put("name", gasGiant.getName());
                                                gasGiantInfo.put("x", pos.getX());
                                                gasGiantInfo.put("y", pos.getY());
                                                gasGiantInfo.put("z", pos.getZ());
                                                gasGiantsInSystem.add(gasGiantInfo);
                                            }
                                            break;
                                        case ASTEROID_BELT:
                                            if (body instanceof com.yisy.modules.doramcu.hex.AsteroidBelt belt) {
                                                Map<String, Object> beltInfo = new HashMap<>();
                                                beltInfo.put("id", belt.getId());
                                                beltInfo.put("name", belt.getName());
                                                beltInfo.put("x", pos.getX());
                                                beltInfo.put("y", pos.getY());
                                                beltInfo.put("z", pos.getZ());
                                                asteroidBeltsInSystem.add(beltInfo);
                                            }
                                            break;
                                        case NEBULA:
                                            if (body instanceof com.yisy.modules.doramcu.hex.Nebula nebula) {
                                                Map<String, Object> nebulaInfo = new HashMap<>();
                                                nebulaInfo.put("id", nebula.getId());
                                                nebulaInfo.put("name", nebula.getName());
                                                nebulaInfo.put("x", pos.getX());
                                                nebulaInfo.put("y", pos.getY());
                                                nebulaInfo.put("z", pos.getZ());
                                                nebulasInSystem.add(nebulaInfo);
                                            }
                                            break;
                                        case BLACK_HOLE:
                                            if (body instanceof com.yisy.modules.doramcu.hex.BlackHole blackHole) {
                                                Map<String, Object> blackHoleInfo = new HashMap<>();
                                                blackHoleInfo.put("id", blackHole.getId());
                                                blackHoleInfo.put("name", blackHole.getName());
                                                blackHoleInfo.put("x", pos.getX());
                                                blackHoleInfo.put("y", pos.getY());
                                                blackHoleInfo.put("z", pos.getZ());
                                                blackHolesInSystem.add(blackHoleInfo);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                            if (systemName != null) {
                                sysInfo.put("name", systemName);
                            }
                            if (!planetsInSystem.isEmpty()) sysInfo.put("planets", planetsInSystem);
                            if (!starsInSystem.isEmpty()) sysInfo.put("stars", starsInSystem);
                            if (!moonsInSystem.isEmpty()) sysInfo.put("moons", moonsInSystem);
                            if (!asteroidsInSystem.isEmpty()) sysInfo.put("asteroids", asteroidsInSystem);
                            if (!cometsInSystem.isEmpty()) sysInfo.put("comets", cometsInSystem);
                            if (!fleetsInSystem.isEmpty()) sysInfo.put("fleets", fleetsInSystem);
                            if (!stationsInSystem.isEmpty()) sysInfo.put("stations", stationsInSystem);
                            if (!gasGiantsInSystem.isEmpty()) sysInfo.put("gasGiants", gasGiantsInSystem);
                            if (!asteroidBeltsInSystem.isEmpty()) sysInfo.put("asteroidBelts", asteroidBeltsInSystem);
                            if (!nebulasInSystem.isEmpty()) sysInfo.put("nebulas", nebulasInSystem);
                            if (!blackHolesInSystem.isEmpty()) sysInfo.put("blackHoles", blackHolesInSystem);
                            systems.add(sysInfo);
                        }
                        SubSectorInfo info = new SubSectorInfo(
                                subSector.getId(),
                                subSector.getName(),
                                sector.getRow(),
                                sector.getCol(),
                                subSector.getSubRow(),
                                subSector.getSubCol(),
                                subSector.isGenerated(),
                                subSector.size(),
                                systems
                        );
                        info.setOwnerEmpireIds(ownerEmpireIds);
                        info.setTradeType(sector.getTradeType());
                        result.add(info);
                    }
                }
            }
            if (hasData) {
                return result;
            }
        }

        // Fallback: query from database directly with rich data (names, positions, planets, ownership)
        if (solarSystemRepository == null) {
            return result;
        }

        // Load subsector names and trade types from the database
        // Query returns: [sectorRow, sectorCol, subSectorRow, subSectorCol, name, tradeType]
        Map<String, String[]> subSectorNameMap = new HashMap<>(); // key: "sectorRow-sectorCol-subRow-subCol" -> [name, tradeType]
        Map<String, Integer> subSectorSystemCountMap = new HashMap<>(); // key: "sectorRow-sectorCol-subRow-subCol" -> systemCount
        try {
            List<com.yisy.modules.doramcu.model.Galaxy> dbGalaxies = galaxyRepository.findAll();
            if (!dbGalaxies.isEmpty()) {
                List<Object[]> subSectorRows = subSectorRepository.findNamesByGalaxyId(dbGalaxies.get(0).getId());
                for (Object[] ssRow : subSectorRows) {
                    int sectorR = ((Number) ssRow[0]).intValue();
                    int sectorC = ((Number) ssRow[1]).intValue();
                    int subR = ((Number) ssRow[2]).intValue();
                    int subC = ((Number) ssRow[3]).intValue();
                    String name = (String) ssRow[4];
                    String tradeType = (String) ssRow[5];
                    String key = sectorR + "-" + sectorC + "-" + subR + "-" + subC;
                    subSectorNameMap.put(key, new String[]{name, tradeType});
                }
                // Count systems per subsector for summary mode
                List<Object[]> systemCountRows = solarSystemRepository.countSystemsBySubSector(dbGalaxies.get(0).getId());
                for (Object[] row : systemCountRows) {
                    String key = (String) row[0];
                    int count = ((Number) row[1]).intValue();
                    subSectorSystemCountMap.put(key, count);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load subsector names from database", e);
        }

        // In summary mode, return only subsector structure without expensive system details query
        if (summary && !subSectorNameMap.isEmpty()) {
            for (Map.Entry<String, String[]> entry : subSectorNameMap.entrySet()) {
                String[] parts = entry.getKey().split("-");
                int sectorR = Integer.parseInt(parts[0]);
                int sectorC = Integer.parseInt(parts[1]);
                int subR = Integer.parseInt(parts[2]);
                int subC = Integer.parseInt(parts[3]);
                String[] info = entry.getValue();
                
                SubSectorInfo ssInfo = new SubSectorInfo();
                ssInfo.setSubRow(subR);
                ssInfo.setSubCol(subC);
                ssInfo.setSectorRow(sectorR);
                ssInfo.setSectorCol(sectorC);
                ssInfo.setSystemCount(subSectorSystemCountMap.getOrDefault(entry.getKey(), 0));
                if (info[0] != null) ssInfo.setName(info[0]);
                if (info[1] != null) ssInfo.setTradeType(info[1]);
                result.add(ssInfo);
            }
            return result;
        }

        // Rich query: [id, name, sectorRow, sectorCol, subSectorRow, subSectorCol, posX, posY, posZ,
        //             starName, starCount, planetCount, moonCount, asteroidBeltCount, gasGiantCount,
        //             nebulaCount, blackHoleCount, cometCount, spaceStationCount, asteroidCount,
        //             planetName, planetSize, ownerEmpireId]
        List<Object[]> rows = solarSystemRepository.findAllWithRichData();
        if (rows.isEmpty()) {
            return result;
        }

        // Group by sector -> subsector -> systems, collecting planets per system
        Map<String, Map<String, Map<Integer, Object[]>>> sectorSubSectorSystemMap = new LinkedHashMap<>();
        // Planets grouped by sector -> subsector -> systemId
        Map<String, Map<String, Map<Integer, List<Map<String, Object>>>>> systemPlanetsMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int sectorRow = ((Number) row[2]).intValue();
            int sectorCol = ((Number) row[3]).intValue();
            int subRow = ((Number) row[4]).intValue();
            int subCol = ((Number) row[5]).intValue();
            int systemId = ((Number) row[0]).intValue();
            String sectorKey = sectorRow + "-" + sectorCol;
            String subKey = subRow + "-" + subCol;

            // Store the first row for each system (has system-level data)
            sectorSubSectorSystemMap.computeIfAbsent(sectorKey, k -> new LinkedHashMap<>())
                    .computeIfAbsent(subKey, k -> new LinkedHashMap<>())
                    .putIfAbsent(systemId, row);

            // Collect planets per system
            Object planetNameObj = row[20];
            if (planetNameObj != null) {
                Map<String, Object> planetInfo = new HashMap<>();
                planetInfo.put("name", planetNameObj);
                String planetSize = row[21] != null ? row[21].toString() : "Unknown";
                planetInfo.put("planetType", planetSize);
                planetInfo.put("planetSize", !planetSize.equals("Unknown") ? planetSize : null);
                planetInfo.put("mineralQuality", null); // Not available from DB query
                planetInfo.put("isCapital", false); // Not available from DB query
                if (row[22] != null) {
                    Integer ownerId = ((Number) row[22]).intValue();
                    planetInfo.put("ownerEmpireId", ownerId);
                } else {
                    planetInfo.put("ownerEmpireId", null);
                }
                systemPlanetsMap.computeIfAbsent(sectorKey, k -> new LinkedHashMap<>())
                        .computeIfAbsent(subKey, k -> new LinkedHashMap<>())
                        .computeIfAbsent(systemId, k -> new ArrayList<>())
                        .add(planetInfo);
            }
        }

        // Build SubSectorInfo for each sector and subsector
        for (Map.Entry<String, Map<String, Map<Integer, Object[]>>> sectorEntry : sectorSubSectorSystemMap.entrySet()) {
            String[] sectorParts = sectorEntry.getKey().split("-");
            int sectorRow = Integer.parseInt(sectorParts[0]);
            int sectorCol = Integer.parseInt(sectorParts[1]);

            for (Map.Entry<String, Map<Integer, Object[]>> subEntry : sectorEntry.getValue().entrySet()) {
                String[] subParts = subEntry.getKey().split("-");
                int subRow = Integer.parseInt(subParts[0]);
                int subCol = Integer.parseInt(subParts[1]);
                Map<Integer, Object[]> systemRows = subEntry.getValue();

                // Look up subsector name and trade type
                String ssKey = sectorRow + "-" + sectorCol + "-" + subRow + "-" + subCol;
                String[] ssInfo = subSectorNameMap.get(ssKey);
                String subSectorName = (ssInfo != null && ssInfo[0] != null) ? ssInfo[0] : "SubSector [" + subRow + "," + subCol + "]";
                String tradeType = (ssInfo != null && ssInfo[1] != null) ? ssInfo[1] : null;

                List<Map<String, Object>> systemInfos = new ArrayList<>();
                Set<Integer> ownerEmpireIds = new HashSet<>();

                for (Map.Entry<Integer, Object[]> sysEntry : systemRows.entrySet()) {
                    Object[] row = sysEntry.getValue();
                    Map<String, Object> sysInfo = new HashMap<>();
                    sysInfo.put("id", row[0]);
                    // System name: prefer star name over generic "System-N" names
                    String sysName = (String) row[1];
                    String starName = (String) row[9];
                    // If system name is generic (matches "System-\d+" pattern), prefer the star name
                    if (sysName == null || sysName.isBlank() || sysName.matches("System-?\\d+")) {
                        sysName = starName;
                    }
                    if (sysName == null || sysName.isBlank()) {
                        sysName = "System " + sysEntry.getKey();
                    }
                    sysInfo.put("name", sysName);

                    // Position within subsector
                    sysInfo.put("posX", row[6]);
                    sysInfo.put("posY", row[7]);
                    sysInfo.put("posZ", row[8]);

                    int starCount = ((Number) row[10]).intValue();
                    int planetCount = ((Number) row[11]).intValue();
                    int moonCount = ((Number) row[12]).intValue();
                    int asteroidBeltCount = ((Number) row[13]).intValue();
                    int gasGiantCount = ((Number) row[14]).intValue();
                    int nebulaCount = ((Number) row[15]).intValue();
                    int blackHoleCount = ((Number) row[16]).intValue();
                    int cometCount = ((Number) row[17]).intValue();
                    int spaceStationCount = ((Number) row[18]).intValue();
                    int asteroidCount = ((Number) row[19]).intValue();
                    sysInfo.put("starCount", starCount);
                    sysInfo.put("planetCount", planetCount);
                    int bodyCount = starCount + planetCount + moonCount + asteroidBeltCount
                            + gasGiantCount + nebulaCount + blackHoleCount + cometCount
                            + spaceStationCount + asteroidCount;
                    sysInfo.put("bodyCount", bodyCount);

                    // Derive system type
                    if (starCount > 1) sysInfo.put("systemType", "Multiple Star System");
                    else if (planetCount >= 5) sysInfo.put("systemType", "Rich System");
                    else if (planetCount >= 3) sysInfo.put("systemType", "Standard System");
                    else if (planetCount >= 1) sysInfo.put("systemType", "Sparse System");
                    else if (asteroidCount > 0) sysInfo.put("systemType", "Asteroid Field");
                    else sysInfo.put("systemType", "Star Only");

                    // Planets for this system (grouped by systemId)
                    List<Map<String, Object>> planets = systemPlanetsMap
                            .getOrDefault(sectorEntry.getKey(), Collections.emptyMap())
                            .getOrDefault(subEntry.getKey(), Collections.emptyMap())
                            .getOrDefault(sysEntry.getKey(), Collections.emptyList());

                    // Collect ownership
                    for (Map<String, Object> p : planets) {
                        Object ownerId = p.get("ownerEmpireId");
                        if (ownerId instanceof Integer) {
                            ownerEmpireIds.add((Integer) ownerId);
                        }
                    }

                    if (!planets.isEmpty()) sysInfo.put("planets", planets);
                    systemInfos.add(sysInfo);
                }

                int subSectorId = subRow * 10 + subCol;
                SubSectorInfo info = new SubSectorInfo(
                        subSectorId,
                        subSectorName,
                        sectorRow, sectorCol,
                        subRow, subCol,
                        true,
                        systemRows.size(),
                        systemInfos
                );
                info.setOwnerEmpireIds(ownerEmpireIds);
                info.setTradeType(tradeType);
                result.add(info);
            }
        }
        return result;
    }

    /**
     * Build full system details from the database for the system detail view.
     * Returns all celestial bodies with names, positions, and ownership info.
     */
    public Map<String, Object> buildSystemDetails(int systemId, Game game) {
        if (solarSystemRepository == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        // Query the database for this specific system
        try {
            List<Object[]> rows = solarSystemRepository.findSystemDetailsById((long) systemId);
            if (rows.isEmpty()) {
                return null;
            }

            // First row has system-level data
            Object[] firstRow = rows.get(0);
            String sysName = (String) firstRow[1];
            String starName = (String) firstRow[9];
            // Prefer star name over generic system name
            if (sysName == null || sysName.isBlank() || sysName.matches("System-?\\d+")) {
                sysName = starName;
            }
            if (sysName == null || sysName.isBlank()) {
                sysName = "System " + systemId;
            }

            result.put("id", systemId);
            result.put("name", sysName);
            result.put("posX", firstRow[6]);
            result.put("posY", firstRow[7]);
            result.put("posZ", firstRow[8]);

            int starCount = ((Number) firstRow[10]).intValue();
            int planetCount = ((Number) firstRow[11]).intValue();
            int moonCount = ((Number) firstRow[12]).intValue();
            int asteroidBeltCount = ((Number) firstRow[13]).intValue();
            int gasGiantCount = ((Number) firstRow[14]).intValue();
            int nebulaCount = ((Number) firstRow[15]).intValue();
            int blackHoleCount = ((Number) firstRow[16]).intValue();
            int cometCount = ((Number) firstRow[17]).intValue();
            int spaceStationCount = ((Number) firstRow[18]).intValue();
            int asteroidCount = ((Number) firstRow[19]).intValue();
            result.put("starCount", starCount);
            result.put("planetCount", planetCount);
            result.put("bodyCount", starCount + planetCount + moonCount + asteroidBeltCount
                    + gasGiantCount + nebulaCount + blackHoleCount + cometCount
                    + spaceStationCount + asteroidCount);

            // Derive system type
            result.put("systemType", deriveSystemType(starCount, planetCount, asteroidCount));

            // Collect individual celestial bodies from all rows
            List<Map<String, Object>> stars = new ArrayList<>();
            List<Map<String, Object>> planets = new ArrayList<>();
            List<Map<String, Object>> moons = new ArrayList<>();
            List<Map<String, Object>> asteroids = new ArrayList<>();
            List<Map<String, Object>> comets = new ArrayList<>();
            List<Map<String, Object>> gasGiants = new ArrayList<>();
            List<Map<String, Object>> asteroidBelts = new ArrayList<>();
            List<Map<String, Object>> nebulas = new ArrayList<>();
            List<Map<String, Object>> blackHoles = new ArrayList<>();
            List<Map<String, Object>> stations = new ArrayList<>();
            Set<Integer> ownerEmpireIds = new HashSet<>();

            // Add star if present
            if (starCount > 0 && starName != null && !starName.isBlank()) {
                Map<String, Object> starInfo = new HashMap<>();
                starInfo.put("id", firstRow[0]);
                starInfo.put("name", starName);
                starInfo.put("x", firstRow[6]);
                starInfo.put("y", firstRow[7]);
                starInfo.put("z", firstRow[8]);
                stars.add(starInfo);
            }

            // Process all rows for planets and other bodies
            for (Object[] row : rows) {
                // Planets
                Object planetNameObj = row[20];
                if (planetNameObj != null) {
                    Map<String, Object> planetInfo = new HashMap<>();
                    planetInfo.put("name", planetNameObj);
                    String planetSize = row[21] != null ? row[21].toString() : "Unknown";
                    planetInfo.put("planetType", planetSize);
                    planetInfo.put("planetSize", !planetSize.equals("Unknown") ? planetSize : null);
                    planetInfo.put("mineralQuality", null);
                    planetInfo.put("isCapital", false);
                    if (row[22] != null) {
                        Integer ownerId = ((Number) row[22]).intValue();
                        planetInfo.put("ownerEmpireId", ownerId);
                        ownerEmpireIds.add(ownerId);
                    } else {
                        planetInfo.put("ownerEmpireId", null);
                    }
                    planets.add(planetInfo);
                }
            }

            // Add counts for body types that don't have individual names in the query
            if (moonCount > 0) {
                for (int i = 0; i < moonCount; i++) {
                    Map<String, Object> moonInfo = new HashMap<>();
                    moonInfo.put("id", systemId * 1000 + i);
                    moonInfo.put("name", "Moon " + (i + 1));
                    moonInfo.put("x", 0);
                    moonInfo.put("y", 0);
                    moonInfo.put("z", 0);
                    moons.add(moonInfo);
                }
            }
            if (asteroidCount > 0) {
                for (int i = 0; i < asteroidCount; i++) {
                    Map<String, Object> astInfo = new HashMap<>();
                    astInfo.put("id", systemId * 2000 + i);
                    astInfo.put("name", "Asteroid " + (i + 1));
                    astInfo.put("x", 0);
                    astInfo.put("y", 0);
                    astInfo.put("z", 0);
                    asteroids.add(astInfo);
                }
            }
            if (cometCount > 0) {
                for (int i = 0; i < cometCount; i++) {
                    Map<String, Object> cometInfo = new HashMap<>();
                    cometInfo.put("id", systemId * 3000 + i);
                    cometInfo.put("name", "Comet " + (i + 1));
                    cometInfo.put("x", 0);
                    cometInfo.put("y", 0);
                    cometInfo.put("z", 0);
                    comets.add(cometInfo);
                }
            }
            if (gasGiantCount > 0) {
                for (int i = 0; i < gasGiantCount; i++) {
                    Map<String, Object> ggInfo = new HashMap<>();
                    ggInfo.put("id", systemId * 4000 + i);
                    ggInfo.put("name", "Gas Giant " + (i + 1));
                    ggInfo.put("x", 0);
                    ggInfo.put("y", 0);
                    ggInfo.put("z", 0);
                    gasGiants.add(ggInfo);
                }
            }
            if (asteroidBeltCount > 0) {
                for (int i = 0; i < asteroidBeltCount; i++) {
                    Map<String, Object> abInfo = new HashMap<>();
                    abInfo.put("id", systemId * 5000 + i);
                    abInfo.put("name", "Asteroid Belt " + (i + 1));
                    abInfo.put("x", 0);
                    abInfo.put("y", 0);
                    abInfo.put("z", 0);
                    asteroidBelts.add(abInfo);
                }
            }
            if (nebulaCount > 0) {
                for (int i = 0; i < nebulaCount; i++) {
                    Map<String, Object> nInfo = new HashMap<>();
                    nInfo.put("id", systemId * 6000 + i);
                    nInfo.put("name", "Nebula " + (i + 1));
                    nInfo.put("x", 0);
                    nInfo.put("y", 0);
                    nInfo.put("z", 0);
                    nebulas.add(nInfo);
                }
            }
            if (blackHoleCount > 0) {
                for (int i = 0; i < blackHoleCount; i++) {
                    Map<String, Object> bhInfo = new HashMap<>();
                    bhInfo.put("id", systemId * 7000 + i);
                    bhInfo.put("name", "Black Hole " + (i + 1));
                    bhInfo.put("x", 0);
                    bhInfo.put("y", 0);
                    bhInfo.put("z", 0);
                    blackHoles.add(bhInfo);
                }
            }
            if (spaceStationCount > 0) {
                for (int i = 0; i < spaceStationCount; i++) {
                    Map<String, Object> ssInfo = new HashMap<>();
                    ssInfo.put("id", systemId * 8000 + i);
                    ssInfo.put("name", "Station " + (i + 1));
                    ssInfo.put("x", 0);
                    ssInfo.put("y", 0);
                    ssInfo.put("z", 0);
                    stations.add(ssInfo);
                }
            }

            // Add lists to result
            if (!stars.isEmpty()) result.put("stars", stars);
            if (!planets.isEmpty()) result.put("planets", planets);
            if (!moons.isEmpty()) result.put("moons", moons);
            if (!asteroids.isEmpty()) result.put("asteroids", asteroids);
            if (!comets.isEmpty()) result.put("comets", comets);
            if (!gasGiants.isEmpty()) result.put("gasGiants", gasGiants);
            if (!asteroidBelts.isEmpty()) result.put("asteroidBelts", asteroidBelts);
            if (!nebulas.isEmpty()) result.put("nebulas", nebulas);
            if (!blackHoles.isEmpty()) result.put("blackHoles", blackHoles);
            if (!stations.isEmpty()) result.put("stations", stations);
            result.put("ownerEmpireIds", new ArrayList<>(ownerEmpireIds));

        } catch (Exception e) {
            log.error("Failed to build system details for system {}", systemId, e);
            return null;
        }

        return result;
    }

    /**
     * Builds a CommanderSummaryDto from a TravellerCharacter.
     * Returns null if the character is null.
     */
    public CommanderSummaryDto buildCommanderSummary(com.yisy.modules.doramcu.traveller.TravellerCharacter character) {
        if (character == null) {
            return null;
        }
        CommanderSummaryDto dto = new CommanderSummaryDto();
        dto.setCharacterId(character.getId());
        dto.setName(character.getName());
        dto.setAge(character.getAge());
        dto.setCredits((int) Math.min(character.getWealth(), Integer.MAX_VALUE));

        // Map abilities using abbreviations
        Map<String, Integer> abilities = new LinkedHashMap<>();
        for (com.yisy.modules.doramcu.traveller.AbilityScore ability : character.getAbilities().keySet()) {
            abilities.put(ability.getAbbreviation(), character.getAbility(ability));
        }
        dto.setAbilities(abilities);

        // Extract skills as names
        List<String> skillNames = new ArrayList<>();
        for (com.yisy.modules.doramcu.traveller.Skill skill : character.getSkills()) {
            skillNames.add(skill.getName());
        }
        dto.setSkills(skillNames);

        // Extract career info from the last career tour
        List<com.yisy.modules.doramcu.traveller.TravellerCharacter.CareerTour> tours = character.getCareerTours();
        if (!tours.isEmpty()) {
            com.yisy.modules.doramcu.traveller.TravellerCharacter.CareerTour lastTour = tours.get(tours.size() - 1);
            dto.setCareer(lastTour.getCareer().getName());
            dto.setCareerTerm(lastTour.getYearsServed() / 4); // 4 years per term
        }

        // Build active bonuses list from Player.CommanderBonusType
        List<String> bonuses = new ArrayList<>();
        for (com.yisy.modules.doramcu.hex.Player.CommanderBonusType type : com.yisy.modules.doramcu.hex.Player.CommanderBonusType.values()) {
            double bonus = character.getCommanderBonus(type);
            if (bonus != 0) {
                bonuses.add(type.name() + ": " + (bonus > 0 ? "+" : "") + bonus);
            }
        }
        dto.setActiveBonuses(bonuses);

        return dto;
    }

    /**
     * Builds a MercenarySummaryDto from an EmpireArmy.
     */
    public MercenarySummaryDto buildMercenarySummary(com.yisy.modules.doramcu.hex.EmpireArmy army) {
        MercenarySummaryDto dto = new MercenarySummaryDto();
        dto.setTotalUnits(army.getMercenaryUnitCount());
        dto.setGroundStrength(army.getTotalGroundStrength());

        // Build unit list
        List<MercenarySummaryDto.MercenaryUnitDto> unitDtos = new ArrayList<>();
        for (com.yisy.modules.doramcu.traveller.MercenaryUnit unit : army.getMercenaryUnits()) {
            MercenarySummaryDto.MercenaryUnitDto unitDto = new MercenarySummaryDto.MercenaryUnitDto();
            unitDto.setUnitId(unit.getId());
            unitDto.setUnitName(unit.getName());
            unitDto.setUnitType(unit.getUnitType().name());
            unitDto.setStrength(unit.getStrength());
            unitDto.setMorale(unit.getMorale());
            unitDto.setActive(!unit.isDestroyed());
            unitDtos.add(unitDto);
        }
        dto.setUnits(unitDtos);

        // Default ticket status
        dto.setTicketStatus("NONE");
        dto.setTicketIncome(0);
        dto.setTicketRemainingTerms(0);

        return dto;
    }

    /**
     * Build trade opportunities for a system.
     * Returns a list of TradeOpportunityDto for all commodities available at planets with economies.
     */
    public List<TradeOpportunityDto> buildTradeOpportunities(int systemId, Game game) {
        List<TradeOpportunityDto> opportunities = new ArrayList<>();
        com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = game.getTradeManager();
        if (tradeManager == null) {
            return opportunities;
        }

        // Iterate all economies and build opportunities
        for (var entry : tradeManager.getAllEconomies().entrySet()) {
            int planetId = entry.getKey();
            com.yisy.modules.doramcu.traveller.WorldEconomy economy = entry.getValue();
            if (economy == null) continue;

            Planet planet = game.getPlanet(planetId);
            String worldName = planet != null ? planet.getName() : "World " + planetId;

            for (com.yisy.modules.doramcu.traveller.Commodity commodity : com.yisy.modules.doramcu.traveller.Commodity.values()) {
                if (!economy.canBuy(commodity) && !economy.canSell(commodity)) continue;

                TradeOpportunityDto dto = new TradeOpportunityDto();
                dto.setCommodity(commodity.getName());
                dto.setBuyPrice(economy.getBuyPrice(commodity));
                dto.setSellPrice(economy.getSellPrice(commodity));
                dto.setAvailableQuantity(economy.getMaxTradeUnits(commodity));
                dto.setWorldId(planetId);
                dto.setWorldName(worldName);

                // Market condition
                com.yisy.modules.doramcu.traveller.MarketConditions.Condition condition =
                        economy.getMarketConditions().getAllConditions().get(commodity);
                dto.setMarketCondition(condition != null ? condition.name() : "NORMAL");

                opportunities.add(dto);
            }
        }

        return opportunities;
    }

    /**
     * Finds the game Player corresponding to a session player ID.
     */
    public Player findPlayerBySessionId(Game game, GameSession session, Long playerId) {
        if (!session.getPlayerIds().contains(playerId)) {
            return null;
        }
        for (Player player : game.getPlayers()) {
            if (player.getId() == playerId.intValue()) {
                return player;
            }
        }
        return null;
    }

    /**
     * Builds active battle summaries for a session.
     */
    public List<BattleSummaryDto> buildActiveBattles(GameSession session) {
        Game game = (Game) session.getGame();
        List<Battle> battles = game.getActiveBattles();
        List<BattleSummaryDto> summaries = new ArrayList<>();

        for (Battle battle : battles) {
            BattleSummaryDto dto = new BattleSummaryDto();
            dto.setBattleId(battle.getId());
            dto.setState(battle.getState().name());
            if (battle.getOutcome() != null) {
                dto.setOutcome(battle.getOutcome().name());
            }
            dto.setCurrentRound(battle.getCurrentRound());
            dto.setMaxRounds(Battle.DEFAULT_MAX_ROUNDS);
            dto.setAttackerName(battle.getAttacker().getName());
            dto.setAttackerShipCount(battle.getAttacker().getShipCount());
            dto.setAttackerRemainingHull(battle.getAttacker().getCurrentHullPoints());
            dto.setAttackerRemainingShields(battle.getAttacker().getCurrentShieldPoints());
            dto.setDefenderName(battle.getDefender().getName());
            dto.setDefenderShipCount(battle.getDefender().getShipCount());
            dto.setDefenderRemainingHull(battle.getDefender().getCurrentHullPoints());
            dto.setDefenderRemainingShields(battle.getDefender().getCurrentShieldPoints());
            dto.setTotalAttackerDamage(battle.getTotalAttackerDamage());
            dto.setTotalDefenderDamage(battle.getTotalDefenderDamage());
            Hex3D loc = battle.getLocation();
            dto.setLocation(loc != null ? loc.getX() + "," + loc.getY() : "unknown");

            List<Battle.BattleEvent> events = battle.getEvents();
            List<String> eventDescriptions = new ArrayList<>();
            int start = Math.max(0, events.size() - 5);
            for (int i = start; i < events.size(); i++) {
                eventDescriptions.add(events.get(i).getDescription());
            }
            dto.setEvents(eventDescriptions);

            summaries.add(dto);
        }

        return summaries;
    }

    /**
     * Builds a complete GameStateResponse from the given session.
     * Shared by getGameState() (read lock) and action methods (write lock).
     *
     * @param session the game session
     * @param playerId the requesting player's ID
     * @param summary if true, use summary mode to avoid iterating all galaxy systems
     * @return the complete game state response
     */
    public GameStateResponse buildGameStateResponse(GameSession session, Long playerId, boolean summary) {
        Game game = (Game) session.getGame();
        List<PlayerSummary> players = buildPlayerSummaries(game, session);
        List<SubSectorInfo> subSectors = buildSubSectorInfo(game, summary);

        GameStateResponse response = new GameStateResponse(
                session.getSessionId(),
                game.getCurrentTurn(),
                null,  // No current player in simultaneous turns
                null,  // No current player name
                players,
                true   // All players can act simultaneously
        );
        response.setSubSectors(subSectors);

        // Build empire name map for ownership display in galaxy view
        Map<Integer, String> empireNameMap = new HashMap<>();
        for (Player gp : game.getPlayers()) {
            Integer eid = gp.getEmpireId();
            if (eid != null) {
                empireNameMap.put(eid, gp.getEmpireName() != null && !gp.getEmpireName().isEmpty() ? gp.getEmpireName() : gp.getName());
            }
        }
        response.setEmpireNameMap(empireNameMap);

        // Build active battle summaries
        response.setActiveBattles(buildActiveBattles(session));

        // Populate turn timing information for timed turns UI
        if (turnScheduler != null) {
            response.setTurnIntervalSeconds(turnScheduler.getTurnIntervalSeconds());
            response.setAccumulatedSeconds(turnScheduler.getAccumulatedSeconds(session.getSessionId()));
            response.setSchedulerEnabled(turnScheduler.isEnabled());
        }

        // Build resource pipeline, fleet list, empire summary, and contested subsectors
        final Integer[] playerEmpireIdHolder = new Integer[1];
        Player player = findPlayerBySessionId(game, session, playerId);
        if (player != null) {
            playerEmpireIdHolder[0] = player.getEmpireId();
            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline != null) {
                response.setPipelineSummary(buildPipelineSummary(pipeline));
            }
            response.setFleets(buildFleetList(player, game));
            response.setEmpireSummary(buildEmpireSummary(player, game));

            // Populate Traveller RPG commander summary
            com.yisy.modules.doramcu.traveller.TravellerCharacter commander = player.getCommander();
            if (commander != null) {
                response.setCommander(buildCommanderSummary(commander));
            }

            // Populate mercenary summary from empire army
            int empireId = player.getEmpireId() != null ? player.getEmpireId() : 0;
            Empire hexEmpire = game.getEmpire(empireId);
            if (hexEmpire != null) {
                com.yisy.modules.doramcu.hex.EmpireArmy army = hexEmpire.getArmy();
                MercenarySummaryDto mercSummary = buildMercenarySummary(army);
                response.getEmpireSummary().setMercenarySummary(mercSummary);
            }
        }

        // Populate trade opportunities (read-only, no player required)
        response.setTradeOpportunities(buildTradeOpportunities(0, game));

        // Populate AI delegation status for the current player
        if (playerId != null && aiDelegationService != null) {
            response.setAiDelegationStatus(aiDelegationService.getDelegationStatus(session.getSessionId(), playerId));
        }

        // Populate global clock information
        GlobalClock clock = session.getGlobalClock();
        if (clock != null) {
            response.setCurrentDateString(clock.getIsoDate());
            response.setCurrentFormattedDate(clock.getFormattedDate(GameType.DORAMCU_4X));
            response.setElapsedDays(clock.getElapsedDays());
        }

        // Mark contested subsectors (any empire other than the requesting player owns planets here)
        final Integer playerEmpireId = playerEmpireIdHolder[0];
        if (playerEmpireId != null) {
            for (SubSectorInfo ss : subSectors) {
                if (ss.getOwnerEmpireIds() != null && !ss.getOwnerEmpireIds().isEmpty()) {
                    boolean hasOtherEmpire = ss.getOwnerEmpireIds().stream()
                            .anyMatch(id -> !id.equals(playerEmpireId));
                    ss.setContested(hasOtherEmpire);
                }
            }
        }
        return response;
    }
}
