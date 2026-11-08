package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.SectorRequest;
import com.yisy.modules.doramcu.dto.SectorResponse;
import com.yisy.modules.doramcu.traveller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for sector and world generation.
 * <p>
 * Manages Traveller sectors: generating random sectors, querying worlds,
 * and managing hyperlanes.
 * </p>
 */
@Service
public class SectorService {

    private static final Logger log = LoggerFactory.getLogger(SectorService.class);

    private final Map<Integer, TravellerSector> sectors = new HashMap<>();
    private final Map<Integer, TravellerWorld> allWorlds = new HashMap<>();
    private final AtomicInteger sectorIdCounter = new AtomicInteger(1);

    /**
     * Generates a new random sector.
     */
    public SectorResponse generateSector(SectorRequest request) {
        log.info("Generating sector: {} with {} worlds",
                request.getSectorName(), request.getWorldCount());

        int sectorId = sectorIdCounter.getAndIncrement();
        Random random = request.getSeed() != null ?
                new Random(request.getSeed()) : new Random();

        SectorGenerator generator = new SectorGenerator(random);

        TravellerSector sector = generator.generate(request.getSectorName(), sectorId);

        // Store sector and worlds
        sectors.put(sectorId, sector);
        for (TravellerWorld world : sector.getAllWorlds()) {
            allWorlds.put(world.getId(), world);
        }

        return toSectorResponse(sector);
    }

    /**
     * Gets a sector by ID.
     */
    public SectorResponse getSector(int sectorId) {
        TravellerSector sector = sectors.get(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("Sector not found: " + sectorId);
        }
        return toSectorResponse(sector);
    }

    /**
     * Gets a world by ID.
     */
    public SectorResponse getWorld(int worldId) {
        TravellerWorld world = allWorlds.get(worldId);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldId);
        }
        return toWorldDetailResponse(world);
    }

    /**
     * Lists all sectors.
     */
    public List<SectorResponse> getAllSectors() {
        List<SectorResponse> responses = new ArrayList<>();
        for (TravellerSector sector : sectors.values()) {
            SectorResponse response = new SectorResponse();
            response.setSectorName(sector.getName());
            response.setWorldCount(sector.getWorldCount());
            response.setHyperlaneCount(sector.getHyperlaneCount());
            TravellerWorld capital = sector.getCapitalWorld();
            response.setCapitalWorld(capital != null ? capital.getName() : null);
            responses.add(response);
        }
        return responses;
    }

    /**
     * Finds habitable worlds in a sector.
     */
    public SectorResponse getHabitableWorlds(int sectorId) {
        TravellerSector sector = sectors.get(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("Sector not found: " + sectorId);
        }

        List<TravellerWorld> habitable = sector.getHabitableWorlds();
        SectorResponse response = new SectorResponse();
        response.setSectorName(sector.getName());
        response.setWorldCount(habitable.size());
        response.setMessage(habitable.size() + " habitable worlds in " + sector.getName());

        List<SectorResponse.WorldSummary> summaries = new ArrayList<>();
        for (TravellerWorld world : habitable) {
            summaries.add(toWorldSummary(world));
        }
        response.setWorlds(summaries);

        return response;
    }

    /**
     * Finds worlds with a specific trade code.
     */
    public SectorResponse getWorldsByTradeCode(int sectorId, String tradeCode) {
        TravellerSector sector = sectors.get(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("Sector not found: " + sectorId);
        }

        TradeCode code = parseTradeCode(tradeCode);
        if (code == null) {
            throw new IllegalArgumentException("Unknown trade code: " + tradeCode);
        }

        List<TravellerWorld> worlds = sector.getWorldsWithTradeCode(code);
        SectorResponse response = new SectorResponse();
        response.setSectorName(sector.getName());
        response.setWorldCount(worlds.size());
        response.setMessage(worlds.size() + " worlds with " + tradeCode + " in " + sector.getName());

        List<SectorResponse.WorldSummary> summaries = new ArrayList<>();
        for (TravellerWorld world : worlds) {
            summaries.add(toWorldSummary(world));
        }
        response.setWorlds(summaries);

        return response;
    }

    // ---- Private Helpers ----

    private SectorResponse toSectorResponse(TravellerSector sector) {
        SectorResponse response = new SectorResponse();
        response.setSectorName(sector.getName());
        response.setWorldCount(sector.getWorldCount());
        response.setHyperlaneCount(sector.getHyperlaneCount());

        TravellerWorld capital = sector.getCapitalWorld();
        response.setCapitalWorld(capital != null ? capital.getName() : null);

        List<SectorResponse.WorldSummary> summaries = new ArrayList<>();
        for (TravellerWorld world : sector.getAllWorlds()) {
            summaries.add(toWorldSummary(world));
        }
        response.setWorlds(summaries);

        response.setMessage(sector.getWorldCount() + " worlds and " +
                sector.getHyperlaneCount() + " hyperlanes in " + sector.getName());

        return response;
    }

    private SectorResponse toWorldDetailResponse(TravellerWorld world) {
        SectorResponse response = new SectorResponse();
        SectorResponse.WorldDetail detail = new SectorResponse.WorldDetail();

        detail.setId(world.getId());
        detail.setName(world.getName());
        detail.setUwp(world.getUwp().toString());
        detail.setPort(world.getUwp().getPlanetSizeDescription());
        detail.setAtmosphere(world.getUwp().getAtmosphereDescription());
        detail.setGravity(world.getUwp().getPlanetSizeDescription());
        detail.setStarport(String.valueOf(world.getUwp().getStarport()));
        detail.setPopulation(world.getUwp().getPopulation());
        detail.setGovernmentType(world.getUwp().getGovernmentType());
        detail.setLawLevel(world.getUwp().getLawLevelDescription());
        detail.setX(world.getSectorX());
        detail.setY(world.getSectorY());
        detail.setCapital(world.isCapital());
        detail.setPortWealth((double) world.getPortWealth());
        detail.setHabitable(world.isHabitable());
        detail.setBreathable(world.getUwp().isHabitable());

        // Trade codes
        List<String> tradeCodes = new ArrayList<>();
        for (TradeCode code : world.getTradeCodes()) {
            tradeCodes.add(code.name());
        }
        detail.setTradeCodes(tradeCodes);

        // Hyperlanes
        List<String> hyperlanes = new ArrayList<>();
        for (Hyperlane lane : world.getHyperlanes()) {
            hyperlanes.add(lane.getOtherEnd(world).getName());
        }
        detail.setHyperlanes(hyperlanes);

        response.setWorldDetail(detail);
        return response;
    }

    private SectorResponse.WorldSummary toWorldSummary(TravellerWorld world) {
        SectorResponse.WorldSummary summary = new SectorResponse.WorldSummary();
        summary.setId(world.getId());
        summary.setName(world.getName());
        summary.setUwp(world.getUwp().toString());
        summary.setX(world.getSectorX());
        summary.setY(world.getSectorY());
        summary.setCapital(world.isCapital());
        summary.setHyperlaneCount(world.getHyperlaneCount());

        List<String> tradeCodes = new ArrayList<>();
        for (TradeCode code : world.getTradeCodes()) {
            tradeCodes.add(code.name());
        }
        summary.setTradeCodes(tradeCodes);

        return summary;
    }

    private TradeCode parseTradeCode(String name) {
        if (name == null) return null;
        for (TradeCode code : TradeCode.values()) {
            if (code.name().equalsIgnoreCase(name)) {
                return code;
            }
        }
        return null;
    }

    private static class AtomicInteger {
        private int value;

        AtomicInteger(int initialValue) {
            this.value = initialValue;
        }

        int getAndIncrement() {
            return value++;
        }
    }
}
