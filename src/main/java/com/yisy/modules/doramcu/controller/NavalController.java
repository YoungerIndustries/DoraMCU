package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.NavalRequest;
import com.yisy.modules.doramcu.dto.NavalResponse;
import com.yisy.modules.doramcu.service.NavalOperationsService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for naval operations.
 * <p>
 * Provides endpoints for fleet creation, status, and naval battles.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/naval}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/naval")
public class NavalController {

    private static final Logger log = LoggerFactory.getLogger(NavalController.class);

    private final NavalOperationsService navalService;

    public NavalController(NavalOperationsService navalService) {
        this.navalService = navalService;
    }

    /**
     * Creates a new naval fleet.
     *
     * @param request the fleet parameters (name, ship design, count)
     * @return the created fleet with squadron details
     */
    @PostMapping("/fleets")
    public ResponseEntity<NavalResponse> createFleet(
            @Valid @RequestBody NavalRequest request) {
        log.debug("POST /api/v1/traveller/naval/fleets - creating: {}", request.getFleetName());
        NavalResponse response = navalService.createFleet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets the status of a fleet.
     *
     * @param fleetId the fleet ID
     * @return the fleet status with squadron details
     */
    @GetMapping("/fleets/{fleetId}")
    public ResponseEntity<NavalResponse> getFleetStatus(@PathVariable int fleetId) {
        log.debug("GET /api/v1/traveller/naval/fleets/{} - fleet status", fleetId);
        NavalResponse response = navalService.getFleetStatus(fleetId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fights a naval battle between two fleets.
     *
     * @param request the battle parameters (attacker and defender fleet IDs)
     * @return the battle result with damage and outcome
     */
    @PostMapping("/battles")
    public ResponseEntity<NavalResponse> fightBattle(
            @Valid @RequestBody NavalRequest request) {
        log.debug("POST /api/v1/traveller/naval/battles - {} vs {}",
                request.getAttackerFleetId(), request.getDefenderFleetId());
        NavalResponse response = navalService.fightBattle(request);
        return ResponseEntity.ok(response);
    }
}
