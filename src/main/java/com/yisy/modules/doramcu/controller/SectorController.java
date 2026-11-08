package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.SectorRequest;
import com.yisy.modules.doramcu.dto.SectorResponse;
import com.yisy.modules.doramcu.service.SectorService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for sector and world generation.
 * <p>
 * Provides endpoints for generating Traveller sectors, querying worlds,
 * finding habitable worlds, and filtering by trade codes.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/4x/sectors}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/4x/sectors")
public class SectorController {

    private static final Logger log = LoggerFactory.getLogger(SectorController.class);

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    /**
     * Generates a new random sector.
     *
     * @param request the sector generation parameters
     * @return the generated sector with all worlds
     */
    @PostMapping
    public ResponseEntity<SectorResponse> generateSector(
            @Valid @RequestBody SectorRequest request) {
        log.debug("POST /api/v1/4x/sectors - generating: {} with {} worlds",
                request.getSectorName(), request.getWorldCount());
        SectorResponse response = sectorService.generateSector(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a sector by ID.
     *
     * @param sectorId the sector ID
     * @return the sector with all worlds, or 404 if not found
     */
    @GetMapping("/{sectorId}")
    public ResponseEntity<SectorResponse> getSector(@PathVariable int sectorId) {
        log.debug("GET /api/v1/4x/sectors/{} - getting sector", sectorId);
        SectorResponse response = sectorService.getSector(sectorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all generated sectors.
     *
     * @return list of all sectors with summaries
     */
    @GetMapping
    public ResponseEntity<List<SectorResponse>> getAllSectors() {
        log.debug("GET /api/v1/4x/sectors - listing all sectors");
        List<SectorResponse> sectors = sectorService.getAllSectors();
        return ResponseEntity.ok(sectors);
    }

    /**
     * Gets a world by ID.
     *
     * @param worldId the world ID
     * @return the world detail, or 404 if not found
     */
    @GetMapping("/worlds/{worldId}")
    public ResponseEntity<SectorResponse> getWorld(@PathVariable int worldId) {
        log.debug("GET /api/v1/4x/sectors/worlds/{} - getting world", worldId);
        SectorResponse response = sectorService.getWorld(worldId);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds habitable worlds in a sector.
     *
     * @param sectorId the sector ID
     * @return list of habitable worlds in the sector
     */
    @GetMapping("/{sectorId}/habitable")
    public ResponseEntity<SectorResponse> getHabitableWorlds(@PathVariable int sectorId) {
        log.debug("GET /api/v1/4x/sectors/{}/habitable - finding habitable worlds", sectorId);
        SectorResponse response = sectorService.getHabitableWorlds(sectorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds worlds with a specific trade code in a sector.
     *
     * @param sectorId the sector ID
     * @param tradeCode the trade code to filter by
     * @return list of worlds with the given trade code
     */
    @GetMapping("/{sectorId}/trade/{tradeCode}")
    public ResponseEntity<SectorResponse> getWorldsByTradeCode(
            @PathVariable int sectorId,
            @PathVariable String tradeCode) {
        log.debug("GET /api/v1/4x/sectors/{}/trade/{} - finding worlds with trade code",
                sectorId, tradeCode);
        SectorResponse response = sectorService.getWorldsByTradeCode(sectorId, tradeCode);
        return ResponseEntity.ok(response);
    }
}
