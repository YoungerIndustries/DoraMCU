package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.service.FirearmsCombatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for firearms combat encounters.
 * <p>
 * Provides endpoints for starting combat, executing rounds,
 * retrieving results, and listing available firearms and cover types.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/firearms}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/firearms")
public class FirearmsCombatController {

    private static final Logger log = LoggerFactory.getLogger(FirearmsCombatController.class);

    private final FirearmsCombatService firearmsCombatService;

    public FirearmsCombatController(FirearmsCombatService firearmsCombatService) {
        this.firearmsCombatService = firearmsCombatService;
    }

    /**
     * Starts a new firearms combat encounter.
     */
    @PostMapping("/combat")
    public ResponseEntity<Map<String, Object>> startCombat(
            @RequestParam int shooter1Id,
            @RequestParam int shooter2Id,
            @RequestParam(required = false) String shooter1Weapon,
            @RequestParam(required = false) String shooter1Cover,
            @RequestParam(required = false) String shooter2Weapon,
            @RequestParam(required = false) String shooter2Cover,
            @RequestParam(defaultValue = "50") int distance) {
        log.debug("POST /api/v1/traveller/firearms/combat — {} vs {} ({} yards)", shooter1Id, shooter2Id, distance);

        FirearmsCombat combat = firearmsCombatService.startCombat(
                shooter1Id, shooter2Id,
                shooter1Weapon, shooter1Cover,
                shooter2Weapon, shooter2Cover,
                distance);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", extractCombatId(combat));
        result.put("shooter1", combat.getShooter1().getName());
        result.put("shooter2", combat.getShooter2().getName());
        result.put("shooter1Weapon", combat.getShooter1().getWeapon().getName());
        result.put("shooter1Cover", combat.getShooter1().getCover().getName());
        result.put("shooter2Weapon", combat.getShooter2().getWeapon().getName());
        result.put("shooter2Cover", combat.getShooter2().getCover().getName());
        result.put("shooter1HP", combat.getShooter1().getHitPoints());
        result.put("shooter2HP", combat.getShooter2().getHitPoints());
        result.put("distance", distance);
        result.put("status", "ACTIVE");

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Executes a single round of an active combat.
     */
    @PostMapping("/combat/{combatId}/round")
    public ResponseEntity<FirearmsCombat.CombatRound> executeRound(@PathVariable String combatId) {
        log.debug("POST /api/v1/traveller/firearms/combat/{}/round", combatId);

        try {
            FirearmsCombat.CombatRound round = firearmsCombatService.executeRound(combatId);
            return ResponseEntity.ok(round);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Runs a combat to completion.
     */
    @PostMapping("/combat/{combatId}/fight")
    public ResponseEntity<Map<String, Object>> fightToCompletion(@PathVariable String combatId) {
        log.debug("POST /api/v1/traveller/firearms/combat/{}/fight", combatId);

        try {
            firearmsCombatService.fightToCompletion(combatId);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        FirearmsCombat combat = firearmsCombatService.getCombat(combatId);
        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("outcome", combat.getOutcome().getDescription());
        result.put("rounds", combat.getCurrentRound());
        result.put("summary", combat.getSummary());
        result.put("shooter1HP", combat.getShooter1().getHitPoints());
        result.put("shooter1Status", combat.getShooter1().getStatus());
        result.put("shooter2HP", combat.getShooter2().getHitPoints());
        result.put("shooter2Status", combat.getShooter2().getStatus());

        return ResponseEntity.ok(result);
    }

    /**
     * Gets the current state of a combat encounter.
     */
    @GetMapping("/combat/{combatId}")
    public ResponseEntity<Map<String, Object>> getCombat(@PathVariable String combatId) {
        log.debug("GET /api/v1/traveller/firearms/combat/{}", combatId);

        FirearmsCombat combat = firearmsCombatService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("active", combat.isActive());
        result.put("outcome", combat.getOutcome().getDescription());
        result.put("rounds", combat.getCurrentRound());
        result.put("shooter1", combat.getShooter1().getName());
        result.put("shooter1HP", combat.getShooter1().getHitPoints());
        result.put("shooter1MaxHP", combat.getShooter1().getMaxHitPoints());
        result.put("shooter1Status", combat.getShooter1().getStatus());
        result.put("shooter2", combat.getShooter2().getName());
        result.put("shooter2HP", combat.getShooter2().getHitPoints());
        result.put("shooter2MaxHP", combat.getShooter2().getMaxHitPoints());
        result.put("shooter2Status", combat.getShooter2().getStatus());

        return ResponseEntity.ok(result);
    }

    /**
     * Gets the full combat log.
     */
    @GetMapping("/combat/{combatId}/log")
    public ResponseEntity<List<String>> getCombatLog(@PathVariable String combatId) {
        log.debug("GET /api/v1/traveller/firearms/combat/{}/log", combatId);

        FirearmsCombat combat = firearmsCombatService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(combat.getCombatLog());
    }

    /**
     * Gets the combat rounds detail.
     */
    @GetMapping("/combat/{combatId}/rounds")
    public ResponseEntity<List<FirearmsCombat.CombatRound>> getCombatRounds(@PathVariable String combatId) {
        log.debug("GET /api/v1/traveller/firearms/combat/{}/rounds", combatId);

        FirearmsCombat combat = firearmsCombatService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(combat.getRounds());
    }

    /**
     * Lists all available firearms.
     */
    @GetMapping("/weapons")
    public ResponseEntity<List<Map<String, Object>>> listWeapons() {
        log.debug("GET /api/v1/traveller/firearms/weapons");

        List<Map<String, Object>> weapons = new ArrayList<>();
        for (FirearmWeapon weapon : firearmsCombatService.getAllWeapons()) {
            Map<String, Object> w = new HashMap<>();
            w.put("name", weapon.getName());
            w.put("type", weapon.getType().getName());
            w.put("damageCode", weapon.getDamageCode());
            w.put("shortRange", weapon.getShortRange());
            w.put("mediumRange", weapon.getMediumRange());
            w.put("longRange", weapon.getLongRange());
            w.put("armorPenetration", weapon.getArmorPenetration());
            w.put("rateOfFire", weapon.getRateOfFire());
            w.put("weight", weapon.getWeight());
            w.put("description", weapon.getDescription());
            weapons.add(w);
        }

        return ResponseEntity.ok(weapons);
    }

    /**
     * Lists all available cover levels.
     */
    @GetMapping("/cover")
    public ResponseEntity<List<Map<String, Object>>> listCoverLevels() {
        log.debug("GET /api/v1/traveller/firearms/cover");

        List<Map<String, Object>> covers = new ArrayList<>();
        for (FirearmsCombatant.CoverLevel cover : firearmsCombatService.getAllCoverLevels()) {
            Map<String, Object> c = new HashMap<>();
            c.put("name", cover.getName());
            c.put("hitPenalty", cover.getHitPenalty());
            c.put("damageReduction", cover.getDamageReduction());
            covers.add(c);
        }

        return ResponseEntity.ok(covers);
    }

    /**
     * Runs a random gunfight between two generated characters.
     */
    @PostMapping("/gunfight")
    public ResponseEntity<Map<String, Object>> randomGunfight(
            @RequestParam String name1,
            @RequestParam String name2) {
        log.debug("POST /api/v1/traveller/firearms/gunfight — {} vs {}", name1, name2);

        FirearmsCombat combat = firearmsCombatService.randomGunfight(name1, name2);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", extractCombatId(combat));
        result.put("outcome", combat.getOutcome().getDescription());
        result.put("rounds", combat.getCurrentRound());
        result.put("summary", combat.getSummary());
        result.put("shooter1", combat.getShooter1().getName());
        result.put("shooter1Weapon", combat.getShooter1().getWeapon().getName());
        result.put("shooter1Cover", combat.getShooter1().getCover().getName());
        result.put("shooter1HP", combat.getShooter1().getHitPoints());
        result.put("shooter1Status", combat.getShooter1().getStatus());
        result.put("shooter2", combat.getShooter2().getName());
        result.put("shooter2Weapon", combat.getShooter2().getWeapon().getName());
        result.put("shooter2Cover", combat.getShooter2().getCover().getName());
        result.put("shooter2HP", combat.getShooter2().getHitPoints());
        result.put("shooter2Status", combat.getShooter2().getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Extracts the combat ID from a FirearmsCombat object.
     */
    private String extractCombatId(FirearmsCombat combat) {
        String combatantId = combat.getShooter1().getCombatantId();
        int dashIndex = combatantId.lastIndexOf("-");
        if (dashIndex > 0) {
            return combatantId.substring(0, dashIndex);
        }
        return combatantId;
    }
}
