package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.ShipDesignRequest;
import com.yisy.modules.doramcu.dto.ShipDesignResponse;
import com.yisy.modules.doramcu.service.ShipDesignService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Traveller starship design management.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting
 * ship designs, as well as creating preset designs based on Classic
 * Traveller archetypes.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/shipdesigns}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/shipdesigns")
public class ShipDesignController {

    private static final Logger log = LoggerFactory.getLogger(ShipDesignController.class);

    private final ShipDesignService shipDesignService;

    public ShipDesignController(ShipDesignService shipDesignService) {
        this.shipDesignService = shipDesignService;
    }

    /**
     * Creates a new ship design.
     *
     * @param request the design parameters
     * @return the created design with calculated statistics
     */
    @PostMapping
    public ResponseEntity<ShipDesignResponse> createDesign(@Valid @RequestBody ShipDesignRequest request) {
        log.debug("POST /api/v1/traveller/shipdesigns - creating design: {}", request.getName());
        ShipDesignResponse response = shipDesignService.createDesign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a ship design by ID.
     *
     * @param id the design ID
     * @return the design with calculated statistics, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShipDesignResponse> getDesign(@PathVariable int id) {
        log.debug("GET /api/v1/traveller/shipdesigns/{}", id);
        ShipDesignResponse response = shipDesignService.getDesign(id);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Returns all ship designs.
     *
     * @return list of all designs with calculated statistics
     */
    @GetMapping
    public ResponseEntity<List<ShipDesignResponse>> getAllDesigns() {
        log.debug("GET /api/v1/traveller/shipdesigns - listing all designs");
        List<ShipDesignResponse> designs = shipDesignService.getAllDesigns();
        return ResponseEntity.ok(designs);
    }

    /**
     * Updates an existing ship design.
     *
     * @param id the design ID
     * @param request the updated design parameters
     * @return the updated design with calculated statistics, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShipDesignResponse> updateDesign(
            @PathVariable int id,
            @Valid @RequestBody ShipDesignRequest request) {
        log.debug("PUT /api/v1/traveller/shipdesigns/{} - updating design", id);
        ShipDesignResponse response = shipDesignService.updateDesign(id, request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a ship design by ID.
     *
     * @param id the design ID
     * @return 204 on success, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesign(@PathVariable int id) {
        log.debug("DELETE /api/v1/traveller/shipdesigns/{}", id);
        boolean deleted = shipDesignService.deleteDesign(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Creates a preset ship design based on Classic Traveller archetypes.
     *
     * @param preset the preset type (DESTROYER, MERCHANT, CORVETTE, CRUISER)
     * @return the created design with calculated statistics, or 400 if preset not found
     */
    @PostMapping("/preset")
    public ResponseEntity<ShipDesignResponse> createPreset(@RequestParam String preset) {
        log.debug("POST /api/v1/traveller/shipdesigns/preset?preset={}", preset);
        ShipDesignResponse response = shipDesignService.createPreset(preset);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

