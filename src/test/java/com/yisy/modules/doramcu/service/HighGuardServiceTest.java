package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardService Tests")
class HighGuardServiceTest {

    private HighGuardService service;

    @BeforeEach
    void setUp() {
        service = new HighGuardService();
    }

    // ---- Ship creation tests ----

    @Test
    @DisplayName("Should create a new ship")
    void testCreateShip() {
        HighGuardShipDesign ship = service.createShip(
                "Test Ship", 4000, 11, 2, 2, 10, 3, "None");

        assertNotNull(ship);
        assertEquals("Test Ship", ship.getName());
        assertEquals(4000, ship.getTotalTonnage());
        assertEquals(11, ship.getTechLevel());
        assertEquals(2, ship.getManeuverDrive());
        assertEquals(2, ship.getJumpDrive());
        assertEquals(10, ship.getPowerPlantRating());
        assertEquals(3, ship.getComputerModel());
    }

    @Test
    @DisplayName("Should create a ship with armor")
    void testCreateShipWithArmor() {
        HighGuardShipDesign ship = service.createShip(
                "Armored Ship", 4000, 11, 2, 2, 10, 3, "Crystaliron x2");

        assertEquals(HighGuardArmor.CRYSTALIRON_2, ship.getArmor());
        assertEquals(8, ship.getArmorProtection());
    }

    @Test
    @DisplayName("Should assign incrementing IDs")
    void testIncrementingIds() {
        HighGuardShipDesign ship1 = service.createShip("Ship 1", 4000, 11, 2, 2, 10, 3, "None");
        HighGuardShipDesign ship2 = service.createShip("Ship 2", 4000, 11, 2, 2, 10, 3, "None");

        assertNotEquals(ship1.getId(), ship2.getId());
    }

    @Test
    @DisplayName("Should retrieve created ship by ID")
    void testGetShip() {
        HighGuardShipDesign created = service.createShip("Test", 4000, 11, 2, 2, 10, 3, "None");
        HighGuardShipDesign retrieved = service.getShip(created.getId());

        assertEquals(created, retrieved);
    }

    @Test
    @DisplayName("Should return null for non-existent ship")
    void testGetShipNotFound() {
        assertNull(service.getShip(9999));
    }

    @Test
    @DisplayName("Should list all created ships")
    void testListShips() {
        service.createShip("Ship 1", 4000, 11, 2, 2, 10, 3, "None");
        service.createShip("Ship 2", 4000, 11, 2, 2, 10, 3, "None");

        List<HighGuardShipDesign> ships = service.listShips();
        assertEquals(2, ships.size());
    }

    // ---- Weapon addition tests ----

    @Test
    @DisplayName("Should add turret weapon to ship")
    void testAddTurretWeapon() {
        HighGuardShipDesign ship = service.createShip("Test", 4000, 11, 2, 2, 10, 3, "None");
        service.addTurretWeapon(ship.getId(), "Pulse Laser", "Double");

        assertEquals(1, ship.getTurretWeapons().size());
        assertEquals(HighGuardWeapon.PULSE_LASER, ship.getTurretWeapons().get(0).getWeapon());
        assertEquals(HighGuardWeapon.TurretSize.DOUBLE, ship.getTurretWeapons().get(0).getTurretSize());
    }

    @Test
    @DisplayName("Should add bay weapon to ship")
    void testAddBayWeapon() {
        HighGuardShipDesign ship = service.createShip("Test", 4000, 11, 2, 2, 10, 3, "None");
        service.addBayWeapon(ship.getId(), "Torpedo (Basic)", "Large");

        assertEquals(1, ship.getBayWeapons().size());
        assertEquals(HighGuardWeapon.TORPEDO_BASIC, ship.getBayWeapons().get(0).getWeapon());
    }

    @Test
    @DisplayName("Should throw on adding weapon to non-existent ship")
    void testAddWeaponNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addTurretWeapon(9999, "Pulse Laser", "Single"));
    }

    @Test
    @DisplayName("Should throw on unknown weapon name")
    void testAddUnknownWeapon() {
        HighGuardShipDesign ship = service.createShip("Test", 4000, 11, 2, 2, 10, 3, "None");
        assertThrows(IllegalArgumentException.class,
                () -> service.addTurretWeapon(ship.getId(), "Unknown Weapon", "Single"));
    }

    // ---- Preset ship tests ----

    @Test
    @DisplayName("Should create Patrol Cruiser preset")
    void testPatrolCruiser() {
        HighGuardShipDesign ship = service.createPresetShip("patrol");

        assertEquals("Patrol Cruiser", ship.getName());
        assertEquals(4000, ship.getTotalTonnage());
        assertEquals(40, ship.getCrew());
        assertEquals(10, ship.getMarines());
        assertTrue(ship.getTurretWeapons().size() >= 2);
    }

    @Test
    @DisplayName("Should create Cruiser preset")
    void testCruiser() {
        HighGuardShipDesign ship = service.createPresetShip("cruiser");

        assertEquals("Cruiser", ship.getName());
        assertEquals(20000, ship.getTotalTonnage());
        assertEquals(200, ship.getCrew());
        assertTrue(ship.getBayWeapons().size() >= 1);
    }

    @Test
    @DisplayName("Should create Dreadnought preset")
    void testDreadnought() {
        HighGuardShipDesign ship = service.createPresetShip("dreadnought");

        assertEquals("Dreadnought", ship.getName());
        assertEquals(100000, ship.getTotalTonnage());
        assertEquals(1000, ship.getCrew());
        assertEquals(200, ship.getMarines());
        assertEquals(2, ship.getNuclearDampers());
        assertEquals(2, ship.getMesonScreens());
    }

    @Test
    @DisplayName("Should create Freighter preset")
    void testFreighter() {
        HighGuardShipDesign ship = service.createPresetShip("freighter");

        assertEquals("Freighter", ship.getName());
        assertEquals(10000, ship.getTotalTonnage());
        assertEquals(20, ship.getCrew());
    }

    @Test
    @DisplayName("Should throw on unknown preset")
    void testUnknownPreset() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createPresetShip("unknown"));
    }

    @Test
    @DisplayName("Preset names should be case-insensitive")
    void testPresetCaseInsensitive() {
        assertNotNull(service.createPresetShip("Patrol"));
        assertNotNull(service.createPresetShip("CRUISER"));
        assertNotNull(service.createPresetShip("Dreadnought"));
        assertNotNull(service.createPresetShip("FREIGHTER"));
    }

    // ---- Combat management tests ----

    @Test
    @DisplayName("Should start combat between two ships")
    void testStartCombat() {
        HighGuardShipDesign ship0 = service.createShip("Ship 0", 4000, 11, 2, 2, 10, 3, "None");
        HighGuardShipDesign ship1 = service.createShip("Ship 1", 4000, 11, 2, 2, 10, 3, "None");
        ship0.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        ship1.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);

        HighGuardShipCombat combat = service.startCombat(ship0.getId(), ship1.getId(), "Long");

        assertNotNull(combat);
        assertFalse(combat.isCompleted());
    }

    @Test
    @DisplayName("Should throw on starting combat with non-existent ship")
    void testStartCombatShipNotFound() {
        HighGuardShipDesign ship = service.createShip("Ship", 4000, 11, 2, 2, 10, 3, "None");
        assertThrows(IllegalArgumentException.class,
                () -> service.startCombat(ship.getId(), 9999, "Long"));
    }

    @Test
    @DisplayName("Should execute a combat round")
    void testExecuteRound() {
        HighGuardShipDesign ship0 = service.createPresetShip("patrol");
        HighGuardShipDesign ship1 = service.createPresetShip("patrol");

        HighGuardShipCombat combat = service.startCombat(ship0.getId(), ship1.getId(), "Short");
        String combatKey = service.listCombats().get(0);
        HighGuardShipCombat result = service.executeRound(combatKey);

        assertNotNull(result);
        assertEquals(1, result.getCurrentRound());
    }

    @Test
    @DisplayName("Should throw on executing round for non-existent combat")
    void testExecuteRoundNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.executeRound("non-existent"));
    }

    @Test
    @DisplayName("Should fight to completion")
    void testFightToCompletion() {
        HighGuardShipDesign ship0 = service.createPresetShip("patrol");
        HighGuardShipDesign ship1 = service.createPresetShip("patrol");

        HighGuardShipCombat combat = service.startCombat(ship0.getId(), ship1.getId(), "Short");
        String combatKey = null;
        for (String key : service.listCombats()) {
            combatKey = key;
            break;
        }
        assertNotNull(combatKey);

        HighGuardShipCombat result = service.fightToCompletion(combatKey);
        assertTrue(result.isCompleted());
    }

    @Test
    @DisplayName("Should throw on fighting non-existent combat")
    void testFightToCompletionNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.fightToCompletion("non-existent"));
    }

    @Test
    @DisplayName("Should list active combats")
    void testListCombats() {
        HighGuardShipDesign ship0 = service.createPresetShip("patrol");
        HighGuardShipDesign ship1 = service.createPresetShip("freighter");
        service.startCombat(ship0.getId(), ship1.getId(), "Long");

        List<String> combats = service.listCombats();
        assertEquals(1, combats.size());
    }

    @Test
    @DisplayName("Should get combat by ID")
    void testGetCombat() {
        HighGuardShipDesign ship0 = service.createPresetShip("patrol");
        HighGuardShipDesign ship1 = service.createPresetShip("freighter");
        HighGuardShipCombat created = service.startCombat(ship0.getId(), ship1.getId(), "Long");

        String combatKey = service.listCombats().get(0);
        HighGuardShipCombat retrieved = service.getCombat(combatKey);

        assertNotNull(retrieved);
    }

    // ---- Random duel tests ----

    @Test
    @DisplayName("Should create a random duel")
    void testCreateRandomDuel() {
        HighGuardShipCombat combat = service.createRandomDuel();

        assertNotNull(combat);
        assertFalse(combat.isCompleted());
    }

    // ---- Reference data tests ----

    @Test
    @DisplayName("Should list all weapons")
    void testListWeapons() {
        List<HighGuardWeapon> weapons = service.listWeapons();
        assertEquals(HighGuardWeapon.values().length, weapons.size());
    }

    @Test
    @DisplayName("Should list all armor types")
    void testListArmor() {
        List<HighGuardArmor> armor = service.listArmor();
        assertEquals(HighGuardArmor.values().length, armor.size());
    }

    @Test
    @DisplayName("Should list all range bands")
    void testListRangeBands() {
        List<HighGuardRangeBand> ranges = service.listRangeBands();
        assertEquals(HighGuardRangeBand.values().length, ranges.size());
    }

    // ---- Parsing tests ----

    @Test
    @DisplayName("Should parse weapon by name (case-insensitive)")
    void testParseWeaponByName() {
        HighGuardShipDesign ship = service.createShip("Test", 4000, 11, 2, 2, 10, 3, "None");
        // Should not throw
        service.addTurretWeapon(ship.getId(), "pulse laser", "single");
        service.addTurretWeapon(ship.getId(), "PULSE_LASER", "SINGLE");
    }

    @Test
    @DisplayName("Should parse armor by name (case-insensitive)")
    void testParseArmorByName() {
        HighGuardShipDesign ship = service.createShip(
                "Test", 4000, 11, 2, 2, 10, 3, "titanium steel x1");
        assertEquals(HighGuardArmor.TITANIUM_STEEL_1, ship.getArmor());
    }

    @Test
    @DisplayName("Should default range band to Long when null")
    void testDefaultRangeBand() {
        HighGuardShipDesign ship0 = service.createPresetShip("patrol");
        HighGuardShipDesign ship1 = service.createPresetShip("freighter");

        HighGuardShipCombat combat = service.startCombat(ship0.getId(), ship1.getId(), null);
        assertEquals(HighGuardRangeBand.LONG, combat.getCurrentRange());
    }

    // ---- Full workflow test ----

    @Test
    @DisplayName("Should support full create-combat-fight workflow")
    void testFullWorkflow() {
        // Create ships
        HighGuardShipDesign ship0 = service.createPresetShip("cruiser");
        HighGuardShipDesign ship1 = service.createPresetShip("patrol");

        // Start combat
        HighGuardShipCombat combat = service.startCombat(
                ship0.getId(), ship1.getId(), "Medium");

        // Fight to completion
        String combatKey = service.listCombats().get(0);
        HighGuardShipCombat result = service.fightToCompletion(combatKey);

        // Verify
        assertTrue(result.isCompleted());
        assertNotNull(result.getOutcome());
        assertTrue(result.getEventLog().size() > 1);
    }
}
