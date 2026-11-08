package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.JumpTravelRequest;
import com.yisy.modules.doramcu.dto.JumpTravelResponse;
import com.yisy.modules.doramcu.service.JumpTravelService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for jump travel and route planning.
 * <p>
 * Provides endpoints for finding routes between worlds, starting journeys,
 * executing jumps, and tracking travel status.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/jump}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/jump")
public class JumpTravelController {

    private static final Logger log = LoggerFactory.getLogger(JumpTravelController.class);

    private final JumpTravelService jumpTravelService;

    public JumpTravelController(JumpTravelService jumpTravelService) {
        this.jumpTravelService = jumpTravelService;
    }

    /**
     * Finds a route between two worlds.
     *
     * @param request the route parameters (source, destination, ship, jump distance)
     * @return the route with waypoints, fuel requirements, and safety info
     */
    @PostMapping("/route")
    public ResponseEntity<JumpTravelResponse> findRoute(
            @Valid @RequestBody JumpTravelRequest request) {
        log.debug("POST /api/v1/traveller/jump/route - {} -> {}",
                request.getSourceWorldId(), request.getDestinationWorldId());
        JumpTravelResponse response = jumpTravelService.findRoute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Starts a new journey to a destination.
     * <p>
     * Creates a jump plan and begins tracking the journey progress.
     * </p>
     *
     * @param request the journey parameters (source, destination, ship)
     * @return the journey plan with waypoints and fuel requirements
     */
    @PostMapping("/journey")
    public ResponseEntity<JumpTravelResponse> startJourney(
            @Valid @RequestBody JumpTravelRequest request) {
        log.debug("POST /api/v1/traveller/jump/journey - {} -> {}",
                request.getSourceWorldId(), request.getDestinationWorldId());
        JumpTravelResponse response = jumpTravelService.startJourney(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes the next jump in an active journey.
     *
     * @param shipDesignId the ship design ID tracking the journey
     * @return the jump result with remaining jumps and fuel status
     */
    @PostMapping("/{shipDesignId}/jump")
    public ResponseEntity<JumpTravelResponse> executeJump(
            @PathVariable int shipDesignId) {
        log.debug("POST /api/v1/traveller/jump/{}/jump - executing next jump", shipDesignId);
        JumpTravelResponse response = jumpTravelService.executeJump(shipDesignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current travel status for a ship.
     *
     * @param shipDesignId the ship design ID
     * @return the current travel state, location, and fuel status
     */
    @GetMapping("/{shipDesignId}/status")
    public ResponseEntity<JumpTravelResponse> getStatus(
            @PathVariable int shipDesignId) {
        log.debug("GET /api/v1/traveller/jump/{}/status - getting travel status", shipDesignId);
        JumpTravelResponse response = jumpTravelService.getStatus(shipDesignId);
        return ResponseEntity.ok(response);
    }
}
