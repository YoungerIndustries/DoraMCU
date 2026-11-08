package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.*;
import com.yisy.modules.doramcu.service.HighGuardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for High Guard starship combat.
 * <p>
 * Provides endpoints for creating ships, starting combat,
 * executing rounds, retrieving results, and listing reference data.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/highguard}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/highguard")
public class HighGuardController {

    private static final Logger log = LoggerFactory.getLogger(HighGuardController.class);

    private final HighGuardService highGuardService;

    public HighGuardController(HighGuardService highGuardService) {
        this.highGuardService = highGuardService;
    }

    // ---- Ship Design Endpoints ----

    /**
     * Creates a new High Guard ship design.
     */
    @PostMapping("/ship")
    public ResponseEntity<Map<String, Object>> createShip(
            @RequestParam String name,
            @RequestParam int totalTonnage,
            @RequestParam(defaultValue = "11") int techLevel,
            @RequestParam(defaultValue = "2") int maneuverDrive,
            @RequestParam(defaultValue = "2") int jumpDrive,
            @RequestParam(defaultValue = "10") int powerPlant,
            @RequestParam(defaultValue = "3") int computerModel,
            @RequestParam(required = false, defaultValue = "None") String armor) {

        log.debug("POST /api/v1/traveller/highguard/ship — {}", name);

        HighGuardShipDesign ship = highGuardService.createShip(
                name, totalTonnage, techLevel, maneuverDrive, jumpDrive,
                powerPlant, computerModel, armor);

        Map<String, Object> result = shipToMap(ship);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Creates a preset ship design from an S09 template.
     * <p>
     * Accepts template names (case-insensitive): scout, destroyer, lightcruiser,
     * heavy cruiser, battlecruiser, battleship, carrier, dreadnought.
     * Also supports legacy names: patrol, freighter.
     * </p>
     */
    @PostMapping("/ship/preset")
    public ResponseEntity<Map<String, Object>> createPresetShip(
            @RequestParam String preset) {

        log.debug("POST /api/v1/traveller/highguard/ship/preset — {}", preset);

        HighGuardShipDesign ship = highGuardService.createPresetShip(preset);

        Map<String, Object> result = shipToMap(ship);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Creates a ship design from a specific S09 template by class name.
     * <p>
     * Ship class values: SCOUT, DESTROYER, LIGHT_CRUISER, HEAVY_CRUISER,
     * BATTLECRUISER, BATTLESHIP, CARRIER, DREADNOUGHT
     * </p>
     */
    @PostMapping("/ship/template")
    public ResponseEntity<Map<String, Object>> createFromTemplate(
            @RequestParam String shipClass) {

        log.debug("POST /api/v1/traveller/highguard/ship/template — {}", shipClass);

        HighGuardShipDesign ship = highGuardService.createFromTemplate(shipClass);

        Map<String, Object> result = shipToMap(ship);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Lists all available S09 ship templates.
     */
    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, Object>>> listTemplates() {
        List<Map<String, Object>> templates = highGuardService.listTemplates().stream()
                .map(this::templateToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(templates);
    }

    /**
     * Adds a turret weapon to a ship.
     */
    @PostMapping("/ship/{shipId}/turret")
    public ResponseEntity<Map<String, Object>> addTurretWeapon(
            @PathVariable int shipId,
            @RequestParam String weapon,
            @RequestParam(defaultValue = "Single") String turretSize) {

        log.debug("POST /api/v1/traveller/highguard/ship/{}/turret — {} {}", shipId, weapon, turretSize);

        highGuardService.addTurretWeapon(shipId, weapon, turretSize);
        HighGuardShipDesign ship = highGuardService.getShip(shipId);

        Map<String, Object> result = shipToMap(ship);
        return ResponseEntity.ok(result);
    }

    /**
     * Adds a missile bay weapon to a ship.
     */
    @PostMapping("/ship/{shipId}/bay")
    public ResponseEntity<Map<String, Object>> addBayWeapon(
            @PathVariable int shipId,
            @RequestParam String weapon,
            @RequestParam(defaultValue = "Small") String baySize) {

        log.debug("POST /api/v1/traveller/highguard/ship/{}/bay — {} {}", shipId, weapon, baySize);

        highGuardService.addBayWeapon(shipId, weapon, baySize);
        HighGuardShipDesign ship = highGuardService.getShip(shipId);

        Map<String, Object> result = shipToMap(ship);
        return ResponseEntity.ok(result);
    }

    /**
     * Gets a ship design by ID.
     */
    @GetMapping("/ship/{shipId}")
    public ResponseEntity<Map<String, Object>> getShip(@PathVariable int shipId) {
        HighGuardShipDesign ship = highGuardService.getShip(shipId);
        if (ship == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shipToMap(ship));
    }

    /**
     * Lists all ship designs.
     */
    @GetMapping("/ships")
    public ResponseEntity<List<Map<String, Object>>> listShips() {
        List<Map<String, Object>> ships = highGuardService.listShips().stream()
                .map(this::shipToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ships);
    }

    // ---- Combat Endpoints ----

    /**
     * Starts a new combat between two ships.
     */
    @PostMapping("/combat")
    public ResponseEntity<Map<String, Object>> startCombat(
            @RequestParam int ship0,
            @RequestParam int ship1,
            @RequestParam(defaultValue = "Long") String range) {

        log.debug("POST /api/v1/traveller/highguard/combat — {} vs {} at {}", ship0, ship1, range);

        HighGuardShipCombat combat = highGuardService.startCombat(ship0, ship1, range);
        String combatId = extractCombatId(combat);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("ship0", combat.getSide0().getDesign().getName());
        result.put("ship1", combat.getSide1().getDesign().getName());
        result.put("range", combat.getCurrentRange().getName());
        result.put("initiative0", combat.getSide0Initiative());
        result.put("initiative1", combat.getSide1Initiative());
        result.put("status", "ACTIVE");

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Executes one round of combat.
     */
    @PostMapping("/combat/{combatId}/round")
    public ResponseEntity<Map<String, Object>> executeRound(@PathVariable String combatId) {
        log.debug("POST /api/v1/traveller/highguard/combat/{}/round", combatId);

        HighGuardShipCombat combat = highGuardService.executeRound(combatId);
        return ResponseEntity.ok(combatToMap(combat, combatId));
    }

    /**
     * Fights a combat to completion.
     */
    @PostMapping("/combat/{combatId}/fight")
    public ResponseEntity<Map<String, Object>> fightToCompletion(@PathVariable String combatId) {
        log.debug("POST /api/v1/traveller/highguard/combat/{}/fight", combatId);

        HighGuardShipCombat combat = highGuardService.fightToCompletion(combatId);
        Map<String, Object> result = combatToMap(combat, combatId);
        result.put("outcome", combat.getOutcome().name());
        return ResponseEntity.ok(result);
    }

    /**
     * Gets the current combat state.
     */
    @GetMapping("/combat/{combatId}")
    public ResponseEntity<Map<String, Object>> getCombat(@PathVariable String combatId) {
        HighGuardShipCombat combat = highGuardService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(combatToMap(combat, combatId));
    }

    /**
     * Gets the combat event log.
     */
    @GetMapping("/combat/{combatId}/log")
    public ResponseEntity<List<Map<String, Object>>> getCombatLog(@PathVariable String combatId) {
        HighGuardShipCombat combat = highGuardService.getCombat(combatId);
        if (combat == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> events = new ArrayList<>();
        for (HighGuardShipCombat.CombatEvent event : combat.getEventLog()) {
            Map<String, Object> e = new HashMap<>();
            e.put("round", event.getRound());
            e.put("phase", event.getPhase().name());
            e.put("type", event.getType().name());
            e.put("message", event.getMessage());
            e.put("side", event.getSide());
            events.add(e);
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Lists all active combats.
     */
    @GetMapping("/combats")
    public ResponseEntity<List<String>> listCombats() {
        return ResponseEntity.ok(highGuardService.listCombats());
    }

    /**
     * Creates a random duel between two preset ships.
     */
    @PostMapping("/duel")
    public ResponseEntity<Map<String, Object>> createRandomDuel() {
        log.debug("POST /api/v1/traveller/highguard/duel");

        HighGuardShipCombat combat = highGuardService.createRandomDuel();
        String combatId = extractCombatId(combat);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("ship0", combat.getSide0().getDesign().getName());
        result.put("ship1", combat.getSide1().getDesign().getName());
        result.put("range", combat.getCurrentRange().getName());
        result.put("status", "ACTIVE");

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ---- Reference Data Endpoints ----

    /**
     * Lists all available weapon types.
     */
    @GetMapping("/weapons")
    public ResponseEntity<List<Map<String, Object>>> listWeapons() {
        List<Map<String, Object>> weapons = new ArrayList<>();
        for (HighGuardWeapon w : highGuardService.listWeapons()) {
            Map<String, Object> wm = new HashMap<>();
            wm.put("name", w.getName());
            wm.put("category", w.getCategory().name());
            wm.put("damageDice", w.getBaseDamageDice());
            wm.put("optimalRange", w.getOptimalRange().getName());
            wm.put("minTechLevel", w.getMinTechLevel());
            wm.put("description", w.getDescription());
            weapons.add(wm);
        }
        return ResponseEntity.ok(weapons);
    }

    /**
     * Lists all available armor types.
     */
    @GetMapping("/armor")
    public ResponseEntity<List<Map<String, Object>>> listArmor() {
        List<Map<String, Object>> armors = new ArrayList<>();
        for (HighGuardArmor a : highGuardService.listArmor()) {
            Map<String, Object> am = new HashMap<>();
            am.put("name", a.getName());
            am.put("protection", a.getProtection());
            am.put("hullPercent", a.getHullPercent());
            am.put("minTechLevel", a.getMinTechLevel());
            am.put("description", a.getDescription());
            armors.add(am);
        }
        return ResponseEntity.ok(armors);
    }

    /**
     * Lists all range bands.
     */
    @GetMapping("/ranges")
    public ResponseEntity<List<Map<String, Object>>> listRangeBands() {
        List<Map<String, Object>> ranges = new ArrayList<>();
        for (HighGuardRangeBand r : highGuardService.listRangeBands()) {
            Map<String, Object> rm = new HashMap<>();
            rm.put("name", r.getName());
            rm.put("minDistanceKm", r.getMinDistanceKm());
            rm.put("maxDistanceKm", r.getMaxDistanceKm());
            rm.put("thrustToChange", r.getThrustToChange());
            rm.put("dogfighting", r.isDogfighting());
            ranges.add(rm);
        }
        return ResponseEntity.ok(ranges);
    }

    // ---- Helpers ----

    private Map<String, Object> shipToMap(HighGuardShipDesign ship) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", ship.getId());
        map.put("name", ship.getName());
        map.put("totalTonnage", ship.getTotalTonnage());
        map.put("techLevel", ship.getTechLevel());
        map.put("maneuverDrive", ship.getManeuverDrive());
        map.put("jumpDrive", ship.getJumpDrive());
        map.put("thrust", ship.getThrust());
        map.put("powerPlant", ship.getPowerPlantRating());
        map.put("energyPoints", ship.getEnergyPoints());
        map.put("agility", ship.getAgility());
        map.put("hullPoints", ship.getHullPoints());
        map.put("armor", ship.getArmor().getName());
        map.put("armorProtection", ship.getArmorProtection());
        map.put("computerModel", ship.getComputerModel());
        map.put("crew", ship.getCrew());
        map.put("marines", ship.getMarines());
        map.put("constructionCost", ship.getConstructionCost());
        map.put("isValid", ship.isValid());

        List<Map<String, Object>> turrets = new ArrayList<>();
        for (HighGuardShipDesign.WeaponMount m : ship.getTurretWeapons()) {
            Map<String, Object> tm = new HashMap<>();
            tm.put("type", "Turret");
            tm.put("size", m.getTurretSize().getName());
            tm.put("weapon", m.getWeapon().getName());
            turrets.add(tm);
        }
        map.put("turrets", turrets);

        List<Map<String, Object>> bays = new ArrayList<>();
        for (HighGuardShipDesign.WeaponMount m : ship.getBayWeapons()) {
            Map<String, Object> bm = new HashMap<>();
            bm.put("type", "Bay");
            bm.put("size", m.getBaySize().getName());
            bm.put("weapon", m.getWeapon().getName());
            bays.add(bm);
        }
        map.put("bays", bays);

        return map;
    }

    private Map<String, Object> combatToMap(HighGuardShipCombat combat, String combatId) {
        Map<String, Object> map = new HashMap<>();
        map.put("combatId", combatId);
        map.put("round", combat.getCurrentRound());
        map.put("phase", combat.getCurrentPhase().name());
        map.put("range", combat.getCurrentRange().getName());
        map.put("completed", combat.isCompleted());

        if (combat.isCompleted()) {
            map.put("outcome", combat.getOutcome().name());
        }

        // Side 0 status
        HighGuardShipCombat.CombatShip s0 = combat.getSide0();
        Map<String, Object> s0Map = new HashMap<>();
        s0Map.put("name", s0.getDesign().getName());
        s0Map.put("hullPoints", s0.getHullPoints());
        s0Map.put("maxHullPoints", s0.getMaxHullPoints());
        s0Map.put("destroyed", s0.isDestroyed());
        s0Map.put("missilePods", s0.getMissilePods());
        map.put("side0", s0Map);

        // Side 1 status
        HighGuardShipCombat.CombatShip s1 = combat.getSide1();
        Map<String, Object> s1Map = new HashMap<>();
        s1Map.put("name", s1.getDesign().getName());
        s1Map.put("hullPoints", s1.getHullPoints());
        s1Map.put("maxHullPoints", s1.getMaxHullPoints());
        s1Map.put("destroyed", s1.isDestroyed());
        s1Map.put("missilePods", s1.getMissilePods());
        map.put("side1", s1Map);

        return map;
    }

    private String extractCombatId(HighGuardShipCombat combat) {
        // Combat IDs are stored in the service; we infer from the combat's ships
        // The service stores them as "combat-N"
        return combat.getSide0().getDesign().getName() + "-vs-" +
               combat.getSide1().getDesign().getName();
    }

    private Map<String, Object> templateToMap(ShipTemplate template) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", template.getId());
        map.put("name", template.getName());
        map.put("shipClass", template.getShipClass().name());
        map.put("displayName", template.getShipClass().getDisplayName());
        map.put("totalTonnage", template.getTotalTonnage());
        map.put("maneuverDrive", template.getManeuverDrive());
        map.put("jumpDrive", template.getJumpDrive());
        map.put("energyPoints", template.getEnergyPoints());
        map.put("armor", template.getArmor() != null ? template.getArmor().getName() : "None");
        map.put("nuclearDampers", template.getNuclearDampers());
        map.put("mesonScreens", template.getMesonScreens());
        map.put("crew", template.getCrew());
        map.put("marines", template.getMarines());
        map.put("constructionCost", template.getConstructionCost());
        map.put("description", template.getDescription());

        List<Map<String, Object>> turrets = new ArrayList<>();
        for (ShipTemplate.TurretEntry entry : template.getTurretWeapons()) {
            Map<String, Object> tm = new HashMap<>();
            tm.put("type", "Turret");
            tm.put("size", entry.getTurretSize().getName());
            tm.put("weapon", entry.getWeapon().getName());
            turrets.add(tm);
        }
        map.put("turrets", turrets);

        List<Map<String, Object>> bays = new ArrayList<>();
        for (ShipTemplate.BayEntry entry : template.getBayWeapons()) {
            Map<String, Object> bm = new HashMap<>();
            bm.put("type", "Bay");
            bm.put("size", entry.getBaySize().getName());
            bm.put("weapon", entry.getWeapon().getName());
            bays.add(bm);
        }
        map.put("bays", bays);

        return map;
    }
}
