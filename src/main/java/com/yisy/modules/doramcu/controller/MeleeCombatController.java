package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.service.MeleeCombatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for melee combat encounters.
 * <p>
 * Provides endpoints for starting combat, executing rounds,
 * retrieving results, and listing available weapons and armor.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/melee}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/melee")
public class MeleeCombatController {

    private static final Logger log = LoggerFactory.getLogger(MeleeCombatController.class);

    private final MeleeCombatService meleeCombatService;

    public MeleeCombatController(MeleeCombatService meleeCombatService) {
        this.meleeCombatService = meleeCombatService;
    }

    /**
     * Starts a new melee combat encounter.
     *
     * @param attackerId the attacker's character ID
     * @param defenderId the defender's character ID
     * @param attackerWeapon the attacker's weapon (optional, default: unarmed)
     * @param attackerArmor the attacker's armor (optional, default: none)
     * @param defenderWeapon the defender's weapon (optional, default: unarmed)
     * @param defenderArmor the defender's armor (optional, default: none)
     * @return the combat encounter info with combat ID
     */
    @PostMapping("/combat")
    public ResponseEntity<Map<String, Object>> startCombat(
            @RequestParam int attackerId,
            @RequestParam int defenderId,
            @RequestParam(required = false) String attackerWeapon,
            @RequestParam(required = false) String attackerArmor,
            @RequestParam(required = false) String defenderWeapon,
            @RequestParam(required = false) String defenderArmor) {
        log.debug("POST /api/v1/traveller/melee/combat — {} vs {}", attackerId, defenderId);

        MeleeCombat combat = meleeCombatService.startCombat(
                attackerId, defenderId,
                attackerWeapon, attackerArmor,
                defenderWeapon, defenderArmor);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", extractCombatId(combat));
        result.put("attacker", combat.getAttacker().getName());
        result.put("defender", combat.getDefender().getName());
        result.put("attackerWeapon", combat.getAttacker().getWeapon().getName());
        result.put("attackerArmor", combat.getAttacker().getArmor().getName());
        result.put("defenderWeapon", combat.getDefender().getWeapon().getName());
        result.put("defenderArmor", combat.getDefender().getArmor().getName());
        result.put("attackerHP", combat.getAttacker().getHitPoints());
        result.put("defenderHP", combat.getDefender().getHitPoints());
        result.put("status", "ACTIVE");

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Executes a single round of an active combat.
     *
     * @param combatId the combat ID
     * @return the round result
     */
    @PostMapping("/combat/{combatId}/round")
    public ResponseEntity<MeleeCombat.CombatRound> executeRound(@PathVariable String combatId) {
        log.debug("POST /api/v1/traveller/melee/combat/{}/round", combatId);

        try {
            MeleeCombat.CombatRound round = meleeCombatService.executeRound(combatId);
            return ResponseEntity.ok(round);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Runs a combat to completion.
     *
     * @param combatId the combat ID
     * @return the combat summary
     */
    @PostMapping("/combat/{combatId}/fight")
    public ResponseEntity<Map<String, Object>> fightToCompletion(@PathVariable String combatId) {
        log.debug("POST /api/v1/traveller/melee/combat/{}/fight", combatId);

        try {
            meleeCombatService.fightToCompletion(combatId);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        MeleeCombat combat = meleeCombatService.getCombat(combatId);
        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("outcome", combat.getOutcome().getDescription());
        result.put("rounds", combat.getCurrentRound());
        result.put("summary", combat.getSummary());
        result.put("attackerHP", combat.getAttacker().getHitPoints());
        result.put("attackerStatus", combat.getAttacker().getStatus());
        result.put("defenderHP", combat.getDefender().getHitPoints());
        result.put("defenderStatus", combat.getDefender().getStatus());

        return ResponseEntity.ok(result);
    }

    /**
     * Gets the current state of a combat encounter.
     *
     * @param combatId the combat ID
     * @return the combat state, or 404 if not found
     */
    @GetMapping("/combat/{combatId}")
    public ResponseEntity<Map<String, Object>> getCombat(@PathVariable String combatId) {
        log.debug("GET /api/v1/traveller/melee/combat/{}", combatId);

        MeleeCombat combat = meleeCombatService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("active", combat.isActive());
        result.put("outcome", combat.getOutcome().getDescription());
        result.put("rounds", combat.getCurrentRound());
        result.put("attacker", combat.getAttacker().getName());
        result.put("attackerHP", combat.getAttacker().getHitPoints());
        result.put("attackerMaxHP", combat.getAttacker().getMaxHitPoints());
        result.put("attackerStatus", combat.getAttacker().getStatus());
        result.put("defender", combat.getDefender().getName());
        result.put("defenderHP", combat.getDefender().getHitPoints());
        result.put("defenderMaxHP", combat.getDefender().getMaxHitPoints());
        result.put("defenderStatus", combat.getDefender().getStatus());

        return ResponseEntity.ok(result);
    }

    /**
     * Gets the full combat log.
     *
     * @param combatId the combat ID
     * @return the combat log entries, or 404 if not found
     */
    @GetMapping("/combat/{combatId}/log")
    public ResponseEntity<List<String>> getCombatLog(@PathVariable String combatId) {
        log.debug("GET /api/v1/traveller/melee/combat/{}/log", combatId);

        MeleeCombat combat = meleeCombatService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(combat.getCombatLog());
    }

    /**
     * Gets the combat rounds detail.
     *
     * @param combatId the combat ID
     * @return the list of combat rounds, or 404 if not found
     */
    @GetMapping("/combat/{combatId}/rounds")
    public ResponseEntity<List<MeleeCombat.CombatRound>> getCombatRounds(@PathVariable String combatId) {
        log.debug("GET /api/v1/traveller/melee/combat/{}/rounds", combatId);

        MeleeCombat combat = meleeCombatService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(combat.getRounds());
    }

    /**
     * Lists all available melee weapons.
     *
     * @return list of weapons with their stats
     */
    @GetMapping("/weapons")
    public ResponseEntity<List<Map<String, Object>>> listWeapons() {
        log.debug("GET /api/v1/traveller/melee/weapons");

        List<Map<String, Object>> weapons = new java.util.ArrayList<>();
        for (MeleeWeapon weapon : meleeCombatService.getAllWeapons()) {
            Map<String, Object> w = new HashMap<>();
            w.put("name", weapon.getName());
            w.put("type", weapon.getType().getName());
            w.put("cut", weapon.getCut());
            w.put("thrust", weapon.getThrust());
            w.put("parry", weapon.getParry());
            w.put("description", weapon.getDescription());
            weapons.add(w);
        }

        return ResponseEntity.ok(weapons);
    }

    /**
     * Lists all available armor types.
     *
     * @return list of armor types with their protection values
     */
    @GetMapping("/armor")
    public ResponseEntity<List<Map<String, Object>>> listArmor() {
        log.debug("GET /api/v1/traveller/melee/armor");

        List<Map<String, Object>> armors = new java.util.ArrayList<>();
        for (Armor armor : meleeCombatService.getAllArmor()) {
            Map<String, Object> a = new HashMap<>();
            a.put("name", armor.getName());
            a.put("cutProtection", armor.getCutProtection());
            a.put("thrustProtection", armor.getThrustProtection());
            a.put("impactProtection", armor.getImpactProtection());
            a.put("description", armor.getDescription());
            armors.add(a);
        }

        return ResponseEntity.ok(armors);
    }

    /**
     * Runs a random duel between two generated characters.
     * <p>
     * Two characters are generated with random abilities and careers,
     * equipped with random weapons and armor, and fight to the death.
     * </p>
     *
     * @param name1 the first character's name
     * @param name2 the second character's name
     * @return the combat result
     */
    @PostMapping("/duel")
    public ResponseEntity<Map<String, Object>> randomDuel(
            @RequestParam String name1,
            @RequestParam String name2) {
        log.debug("POST /api/v1/traveller/melee/duel — {} vs {}", name1, name2);

        MeleeCombat combat = meleeCombatService.randomDuel(name1, name2);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", extractCombatId(combat));
        result.put("outcome", combat.getOutcome().getDescription());
        result.put("rounds", combat.getCurrentRound());
        result.put("summary", combat.getSummary());
        result.put("attacker", combat.getAttacker().getName());
        result.put("attackerWeapon", combat.getAttacker().getWeapon().getName());
        result.put("attackerArmor", combat.getAttacker().getArmor().getName());
        result.put("attackerHP", combat.getAttacker().getHitPoints());
        result.put("attackerStatus", combat.getAttacker().getStatus());
        result.put("defender", combat.getDefender().getName());
        result.put("defenderWeapon", combat.getDefender().getWeapon().getName());
        result.put("defenderArmor", combat.getDefender().getArmor().getName());
        result.put("defenderHP", combat.getDefender().getHitPoints());
        result.put("defenderStatus", combat.getDefender().getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Extracts the combat ID from a MeleeCombat object.
     * <p>
     * The combat ID is embedded in the combatant IDs.
     * </p>
     */
    private String extractCombatId(MeleeCombat combat) {
        String combatantId = combat.getAttacker().getCombatantId();
        // Format: "combat-N-attacker" → "combat-N"
        int dashIndex = combatantId.lastIndexOf("-");
        if (dashIndex > 0) {
            return combatantId.substring(0, dashIndex);
        }
        return combatantId;
    }
}
