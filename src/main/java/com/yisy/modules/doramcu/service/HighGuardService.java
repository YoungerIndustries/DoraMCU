package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for High Guard starship combat.
 * <p>
 * Manages ship designs, combat encounters, and combat resolution.
 * Provides methods to:
 * </p>
 * <ul>
 *   <li>Create and configure High Guard ship designs</li>
 *   <li>Start combat between two ships</li>
 *   <li>Execute combat round by round or to completion</li>
 *   <li>Retrieve combat results and logs</li>
 *   <li>List available weapons and armor types</li>
 * </ul>
 */
@Service
public class HighGuardService {

    private static final Logger log = LoggerFactory.getLogger(HighGuardService.class);

    private final Map<Integer, HighGuardShipDesign> shipDesigns = new ConcurrentHashMap<>();
    private final Map<String, HighGuardShipCombat> activeCombats = new ConcurrentHashMap<>();
    private final AtomicInteger shipIdCounter = new AtomicInteger(1);
    private final AtomicInteger combatIdCounter = new AtomicInteger(1);

    // ---- Ship Design Management ----

    /**
     * Creates a new High Guard ship design.
     *
     * @param name the ship's name
     * @param totalTonnage the total tonnage
     * @param techLevel the Tech Level
     * @param maneuverDrive the maneuver drive rating
     * @param jumpDrive the jump drive rating
     * @param powerPlant the power plant rating
     * @param computerModel the computer model
     * @param armorName the armor type name (or "None")
     * @return the created ship design
     */
    public HighGuardShipDesign createShip(String name, int totalTonnage, int techLevel,
                                          int maneuverDrive, int jumpDrive, int powerPlant,
                                          int computerModel, String armorName) {
        int id = shipIdCounter.getAndIncrement();
        HighGuardShipDesign ship = new HighGuardShipDesign(id, name, totalTonnage);
        ship.setTechLevel(techLevel);
        ship.setManeuverDrive(maneuverDrive);
        ship.setJumpDrive(jumpDrive);
        ship.setPowerPlantRating(powerPlant);
        ship.setComputerModel(computerModel);

        if (armorName != null && !armorName.isEmpty() && !"None".equals(armorName)) {
            ship.setArmor(parseArmor(armorName));
        }

        shipDesigns.put(id, ship);
        log.info("Ship created: {} ({})", name, id);
        return ship;
    }

    /**
     * Adds a turret weapon to a ship design.
     */
    public void addTurretWeapon(int shipId, String weaponName, String turretSize) {
        HighGuardShipDesign ship = shipDesigns.get(shipId);
        if (ship == null) {
            throw new IllegalArgumentException("Ship not found: " + shipId);
        }
        HighGuardWeapon weapon = parseWeapon(weaponName);
        HighGuardWeapon.TurretSize size = parseTurretSize(turretSize);
        ship.addTurretWeapon(weapon, size);
    }

    /**
     * Adds a missile bay weapon to a ship design.
     */
    public void addBayWeapon(int shipId, String weaponName, String baySize) {
        HighGuardShipDesign ship = shipDesigns.get(shipId);
        if (ship == null) {
            throw new IllegalArgumentException("Ship not found: " + shipId);
        }
        HighGuardWeapon weapon = parseWeapon(weaponName);
        HighGuardWeapon.BaySize size = parseBaySize(baySize);
        ship.addBayWeapon(weapon, size);
    }

    /**
     * Gets a ship design by ID.
     */
    public HighGuardShipDesign getShip(int shipId) {
        return shipDesigns.get(shipId);
    }

    /**
     * Lists all ship designs.
     */
    public List<HighGuardShipDesign> listShips() {
        return new ArrayList<>(shipDesigns.values());
    }

    /**
     * Creates a preset ship design from an S09 template.
     * <p>
     * Accepts template names (case-insensitive): scout, courier, destroyer,
     * lightcruiser, heavy cruiser, battlecruiser, battleship, carrier, dreadnought.
     * Also supports legacy names: patrol, cruiser, freighter.
     * </p>
     *
     * @param presetName the template or preset name
     * @return the created ship design
     * @throws IllegalArgumentException if the preset name is unknown
     */
    public HighGuardShipDesign createPresetShip(String presetName) {
        // Legacy preset names for backward compatibility (checked first)
        switch (presetName.toLowerCase()) {
            case "patrol":
                return createPatrolCruiser();
            case "cruiser":
                return createCruiser();
            case "dreadnought":
                return createDreadnought();
            case "freighter":
                return createFreighter();
            default:
                break;
        }

        // Try S09 template names
        for (ShipTemplate template : ShipTemplate.getAllTemplates()) {
            String className = template.getShipClass().name().toLowerCase();
            String templateName = template.getName().toLowerCase().replaceAll("[\\s-]", "");
            String query = presetName.toLowerCase().replaceAll("[\\s-]", "");

            if (query.equals(className) || query.equals(templateName)) {
                int newId = shipIdCounter.getAndIncrement();
                HighGuardShipDesign design = template.toDesign(newId);
                shipDesigns.put(newId, design);
                log.info("Created ship from template: {} (id={})", template.getName(), newId);
                return design;
            }
        }

        throw new IllegalArgumentException("Unknown preset: " + presetName
                + ". Available templates: " + getTemplateNames());
    }

    /**
     * Returns a comma-separated list of available template names.
     */
    private String getTemplateNames() {
        StringBuilder sb = new StringBuilder();
        for (ShipTemplate t : ShipTemplate.getAllTemplates()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(t.getShipClass().name().toLowerCase());
        }
        return sb.toString();
    }

    /**
     * Lists all available S09 ship templates.
     *
     * @return list of all pre-designed ship templates
     */
    public List<ShipTemplate> listTemplates() {
        return ShipTemplate.getAllTemplates();
    }

    /**
     * Creates a ship design from a specific S09 template by class name.
     *
     * @param shipClass the ship class enum name (e.g., "SCOUT", "DESTROYER")
     * @return the created ship design
     */
    public HighGuardShipDesign createFromTemplate(String shipClass) {
        List<ShipTemplate> templates = ShipTemplate.getByClass(ShipTemplate.ShipClass.valueOf(shipClass.toUpperCase()));
        if (templates.isEmpty()) {
            throw new IllegalArgumentException("Unknown ship class: " + shipClass);
        }
        ShipTemplate template = templates.get(0);
        int newId = shipIdCounter.getAndIncrement();
        HighGuardShipDesign design = template.toDesign(newId);
        shipDesigns.put(newId, design);
        log.info("Created ship from template class {}: {} (id={})", shipClass, template.getName(), newId);
        return design;
    }

    private HighGuardShipDesign createPatrolCruiser() {
        HighGuardShipDesign ship = createShip("Patrol Cruiser", 4000, 11, 2, 2, 10, 3, "Crystaliron x2");
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addTurretWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.TurretSize.SINGLE);
        ship.setCrew(40);
        ship.setMarines(10);
        return ship;
    }

    private HighGuardShipDesign createCruiser() {
        HighGuardShipDesign ship = createShip("Cruiser", 20000, 12, 3, 3, 20, 5, "Crystaliron x3");
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addTurretWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE);
        ship.setCrew(200);
        ship.setMarines(50);
        return ship;
    }

    private HighGuardShipDesign createDreadnought() {
        HighGuardShipDesign ship = createShip("Dreadnought", 100000, 13, 2, 3, 40, 7, "Crystaliron x4");
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.TRIPLE);
        ship.addTurretWeapon(HighGuardWeapon.MESON_GUN, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addTurretWeapon(HighGuardWeapon.MISSILE_NUCLEAR, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_NUCLEAR, HighGuardWeapon.BaySize.LARGE);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_NUCLEAR, HighGuardWeapon.BaySize.LARGE);
        ship.setCrew(1000);
        ship.setMarines(200);
        ship.setNuclearDampers(2);
        ship.setMesonScreens(2);
        return ship;
    }

    private HighGuardShipDesign createFreighter() {
        HighGuardShipDesign ship = createShip("Freighter", 10000, 10, 2, 3, 12, 2, "Titanium Steel x2");
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        ship.addTurretWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.TurretSize.SINGLE);
        ship.setCrew(20);
        return ship;
    }

    // ---- Combat Management ----

    /**
     * Starts a new combat between two ships.
     *
     * @param ship0Id the first ship's ID
     * @param ship1Id the second ship's ID
     * @param initialRange the starting range band name
     * @return the created combat
     */
    public HighGuardShipCombat startCombat(int ship0Id, int ship1Id, String initialRange) {
        HighGuardShipDesign ship0 = shipDesigns.get(ship0Id);
        HighGuardShipDesign ship1 = shipDesigns.get(ship1Id);

        if (ship0 == null) {
            throw new IllegalArgumentException("Ship 0 not found: " + ship0Id);
        }
        if (ship1 == null) {
            throw new IllegalArgumentException("Ship 1 not found: " + ship1Id);
        }

        HighGuardRangeBand range = parseRangeBand(initialRange);
        String combatId = "combat-" + combatIdCounter.getAndIncrement();
        HighGuardShipCombat combat = new HighGuardShipCombat(ship0, ship1, range);
        activeCombats.put(combatId, combat);

        log.info("Combat started: {} vs {} ({})", ship0.getName(), ship1.getName(), combatId);
        return combat;
    }

    /**
     * Executes one round of combat.
     */
    public HighGuardShipCombat executeRound(String combatId) {
        HighGuardShipCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            throw new IllegalArgumentException("Combat not found: " + combatId);
        }
        combat.executeRound();
        return combat;
    }

    /**
     * Fights a combat to completion.
     */
    public HighGuardShipCombat fightToCompletion(String combatId) {
        HighGuardShipCombat combat = activeCombats.get(combatId);
        if (combat == null) {
            throw new IllegalArgumentException("Combat not found: " + combatId);
        }
        combat.fightToCompletion();
        return combat;
    }

    /**
     * Gets a combat by ID.
     */
    public HighGuardShipCombat getCombat(String combatId) {
        return activeCombats.get(combatId);
    }

    /**
     * Lists all active combats.
     */
    public List<String> listCombats() {
        return new ArrayList<>(activeCombats.keySet());
    }

    /**
     * Creates a random duel between two preset ships.
     */
    public HighGuardShipCombat createRandomDuel() {
        HighGuardShipDesign ship0 = createPresetShip(randomPreset());
        HighGuardShipDesign ship1 = createPresetShip(randomPreset());
        HighGuardRangeBand range = HighGuardRangeBand.values()[new Random().nextInt(7)];
        return startCombat(ship0.getId(), ship1.getId(), range.getName());
    }

    private String randomPreset() {
        String[] presets = {"patrol", "cruiser", "dreadnought", "freighter"};
        return presets[new Random().nextInt(presets.length)];
    }

    // ---- Reference Data ----

    /**
     * Lists all available weapon types.
     */
    public List<HighGuardWeapon> listWeapons() {
        return Arrays.asList(HighGuardWeapon.values());
    }

    /**
     * Lists all available armor types.
     */
    public List<HighGuardArmor> listArmor() {
        return Arrays.asList(HighGuardArmor.values());
    }

    /**
     * Lists all range bands.
     */
    public List<HighGuardRangeBand> listRangeBands() {
        return Arrays.asList(HighGuardRangeBand.values());
    }

    // ---- Parsing Helpers ----

    private HighGuardWeapon parseWeapon(String name) {
        for (HighGuardWeapon w : HighGuardWeapon.values()) {
            if (w.getName().equalsIgnoreCase(name) || w.name().equalsIgnoreCase(name)) {
                return w;
            }
        }
        throw new IllegalArgumentException("Unknown weapon: " + name);
    }

    private HighGuardArmor parseArmor(String name) {
        for (HighGuardArmor a : HighGuardArmor.values()) {
            if (a.getName().equalsIgnoreCase(name) || a.name().equalsIgnoreCase(name)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown armor: " + name);
    }

    private HighGuardWeapon.TurretSize parseTurretSize(String name) {
        if (name == null || name.isEmpty() || "Single".equalsIgnoreCase(name)) {
            return HighGuardWeapon.TurretSize.SINGLE;
        }
        for (HighGuardWeapon.TurretSize s : HighGuardWeapon.TurretSize.values()) {
            if (s.getName().equalsIgnoreCase(name) || s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return HighGuardWeapon.TurretSize.SINGLE;
    }

    private HighGuardWeapon.BaySize parseBaySize(String name) {
        if (name == null || name.isEmpty() || "Small".equalsIgnoreCase(name)) {
            return HighGuardWeapon.BaySize.SMALL;
        }
        for (HighGuardWeapon.BaySize s : HighGuardWeapon.BaySize.values()) {
            if (s.getName().equalsIgnoreCase(name) || s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return HighGuardWeapon.BaySize.SMALL;
    }

    private HighGuardRangeBand parseRangeBand(String name) {
        if (name == null || name.isEmpty()) {
            return HighGuardRangeBand.LONG;
        }
        for (HighGuardRangeBand b : HighGuardRangeBand.values()) {
            if (b.getName().equalsIgnoreCase(name) || b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        return HighGuardRangeBand.LONG;
    }
}
