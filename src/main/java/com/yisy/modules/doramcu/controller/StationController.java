package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.StationRequest;
import com.yisy.modules.doramcu.dto.StationResponse;
import com.yisy.modules.doramcu.service.StationOperationsService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for space station operations.
 * <p>
 * Provides endpoints for station information, docking, undocking,
 * refueling, and repair.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/4x/stations}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/4x/stations")
public class StationController {

    private static final Logger log = LoggerFactory.getLogger(StationController.class);

    private final StationOperationsService stationService;

    public StationController(StationOperationsService stationService) {
        this.stationService = stationService;
    }

    /**
     * Gets station information for a world.
     *
     * @param worldId the world ID (station orbits this world)
     * @return station details including facilities and docked ships
     */
    @GetMapping("/{worldId}")
    public ResponseEntity<StationResponse> getStationInfo(@PathVariable int worldId) {
        log.debug("GET /api/v1/4x/stations/{} - station info", worldId);
        StationResponse response = stationService.getStationInfo(worldId);
        return ResponseEntity.ok(response);
    }

    /**
     * Docks a ship at the station.
     *
     * @param request the docking parameters (world, ship name, tonnage)
     * @return the docking result
     */
    @PostMapping("/dock")
    public ResponseEntity<StationResponse> dockShip(
            @Valid @RequestBody StationRequest request) {
        log.debug("POST /api/v1/4x/stations/dock - {} at world {}",
                request.getShipName(), request.getWorldId());
        StationResponse response = stationService.dockShip(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Undocks a ship from the station.
     *
     * @param request the undocking parameters (world, ship name)
     * @return the undocking result
     */
    @PostMapping("/undock")
    public ResponseEntity<StationResponse> undockShip(
            @Valid @RequestBody StationRequest request) {
        log.debug("POST /api/v1/4x/stations/undock - {} at world {}",
                request.getShipName(), request.getWorldId());
        StationResponse response = stationService.undockShip(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refuels a ship at the station.
     *
     * @param request the refueling parameters (world, ship, full tank flag)
     * @return the refueling result with cost and fuel added
     */
    @PostMapping("/refuel")
    public ResponseEntity<StationResponse> refuelShip(
            @Valid @RequestBody StationRequest request) {
        log.debug("POST /api/v1/4x/stations/refuel - ship {} at world {}",
                request.getShipDesignId(), request.getWorldId());
        StationResponse response = stationService.refuelShip(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Repairs a ship at the station.
     *
     * @param request the repair parameters (world, current/max hull)
     * @return the repair result with cost and hull restored
     */
    @PostMapping("/repair")
    public ResponseEntity<StationResponse> repairShip(
            @Valid @RequestBody StationRequest request) {
        log.debug("POST /api/v1/4x/stations/repair - world {} hull {}/{}",
                request.getWorldId(), request.getCurrentHull(), request.getMaxHull());
        StationResponse response = stationService.repairShip(request);
        return ResponseEntity.ok(response);
    }
}
