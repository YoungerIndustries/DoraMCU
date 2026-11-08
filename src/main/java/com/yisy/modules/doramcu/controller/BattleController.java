package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.BattleRequest;
import com.yisy.modules.doramcu.dto.BattleResponse;
import com.yisy.modules.doramcu.service.BattleService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for ship-to-ship battle management.
 * <p>
 * Provides endpoints for creating battles between Traveller ship designs,
 * fighting battles round-by-round or to completion, and retrieving
 * battle results and history.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/battles}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/battles")
public class BattleController {

    private static final Logger log = LoggerFactory.getLogger(BattleController.class);

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    /**
     * Creates and fights a battle to completion.
     * <p>
     * Creates two fleets from the given ship designs and fights the battle
     * automatically until one side is destroyed or a stalemate occurs.
     * </p>
     *
     * @param request the battle parameters (design IDs, ship counts)
     * @return the complete battle result with round-by-round details
     */
    @PostMapping("/fight")
    public ResponseEntity<BattleResponse> fightBattle(@Valid @RequestBody BattleRequest request) {
        log.debug("POST /api/v1/traveller/battles/fight - {} vs {}",
                request.getAttackerDesignId(), request.getDefenderDesignId());
        BattleResponse response = battleService.createAndFightBattle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Creates a battle for round-by-round play.
     * <p>
     * Creates two fleets from the given ship designs and starts the battle,
     * but does not fight any rounds. Use the fight round endpoint to
     * progress the battle one round at a time.
     * </p>
     *
     * @param request the battle parameters (design IDs, ship counts)
     * @return the initial battle status
     */
    @PostMapping
    public ResponseEntity<BattleResponse> createBattle(@Valid @RequestBody BattleRequest request) {
        log.debug("POST /api/v1/traveller/battles - creating battle: {} vs {}",
                request.getAttackerDesignId(), request.getDefenderDesignId());
        BattleResponse response = battleService.createBattle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Fights one round of an active battle.
     * <p>
     * Progresses the battle by one round and returns the updated status.
     * If the battle ends, returns the complete battle result.
     * </p>
     *
     * @param battleId the battle ID (returned when the battle was created)
     * @return the updated battle status or final result
     */
    @PostMapping("/{battleId}/round")
    public ResponseEntity<BattleResponse> fightRound(@PathVariable int battleId) {
        log.debug("POST /api/v1/traveller/battles/{}/round - fighting next round", battleId);
        BattleResponse response = battleService.fightRound(battleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current status of a battle.
     * <p>
     * Returns the status of an active battle or the result of a completed battle.
     * </p>
     *
     * @param battleId the battle ID
     * @return the battle status, or 404 if not found
     */
    @GetMapping("/{battleId}")
    public ResponseEntity<BattleResponse> getBattleStatus(@PathVariable int battleId) {
        log.debug("GET /api/v1/traveller/battles/{} - getting battle status", battleId);
        BattleResponse response = battleService.getBattleStatus(battleId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all completed battles.
     *
     * @return list of all completed battle results
     */
    @GetMapping("/completed")
    public ResponseEntity<List<BattleResponse>> getAllCompletedBattles() {
        log.debug("GET /api/v1/traveller/battles/completed - listing all completed battles");
        List<BattleResponse> battles = battleService.getAllCompletedBattles();
        return ResponseEntity.ok(battles);
    }

    /**
     * Lists all active battles.
     *
     * @return list of active battle IDs
     */
    @GetMapping("/active")
    public ResponseEntity<List<Integer>> getActiveBattles() {
        log.debug("GET /api/v1/traveller/battles/active - listing all active battles");
        List<Integer> battleIds = battleService.getActiveBattleIds();
        return ResponseEntity.ok(battleIds);
    }
}
