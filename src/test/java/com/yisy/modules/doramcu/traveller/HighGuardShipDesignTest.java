package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardShipDesign Tests")
class HighGuardShipDesignTest {

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create a ship with minimum tonnage")
    void testMinimumTonnage() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Small Ship", 100);
        assertEquals(1, ship.getId());
        assertEquals("Small Ship", ship.getName());
        assertEquals(100, ship.getTotalTonnage());
    }

    @Test
    @DisplayName("Should create a ship with maximum tonnage")
    void testMaximumTonnage() {
        HighGuardShipDesign ship = new HighGuardShipDesign(2, "Dreadnought", 1000000);
        assertEquals(1000000, ship.getTotalTonnage());
    }

    @Test
    @DisplayName("Should throw on tonnage below minimum")
    void testTonnageTooLow() {
        assertThrows(IllegalArgumentException.class, () -> new HighGuardShipDesign(1, "Too Small", 99));
    }

    @Test
    @DisplayName("Should throw on tonnage above maximum")
    void testTonnageTooHigh() {
        assertThrows(IllegalArgumentException.class, () -> new HighGuardShipDesign(1, "Too Big", 1000001));
    }

    @Test
    @DisplayName("Should throw on null name")
    void testNullName() {
        assertThrows(NullPointerException.class, () -> new HighGuardShipDesign(1, null, 1000));
    }

    @Test
    @DisplayName("Should have default values after construction")
    void testDefaultValues() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test Ship", 4000);
        assertEquals(11, ship.getTechLevel());
        assertEquals(HighGuardArmor.NONE, ship.getArmor());
        assertEquals(0, ship.getManeuverDrive());
        assertEquals(0, ship.getJumpDrive());
        assertEquals(0, ship.getPowerPlantRating());
        assertEquals(0, ship.getComputerModel());
        assertEquals(0, ship.getCrew());
        assertEquals(0, ship.getMarines());
        assertTrue(ship.getTurretWeapons().isEmpty());
        assertTrue(ship.getBayWeapons().isEmpty());
    }

    // ---- Setter tests ----

    @Test
    @DisplayName("Should set tech level with clamping")
    void testTechLevelClamping() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setTechLevel(5);  // Below minimum
        assertEquals(6, ship.getTechLevel());
        ship.setTechLevel(20); // Above maximum
        assertEquals(15, ship.getTechLevel());
    }

    @Test
    @DisplayName("Should set maneuver drive with clamping")
    void testManeuverDriveClamping() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setManeuverDrive(-1);
        assertEquals(0, ship.getManeuverDrive());
        ship.setManeuverDrive(10);
        assertEquals(6, ship.getManeuverDrive());
    }

    @Test
    @DisplayName("Should set jump drive with clamping")
    void testJumpDriveClamping() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setJumpDrive(-1);
        assertEquals(0, ship.getJumpDrive());
        ship.setJumpDrive(10);
        assertEquals(6, ship.getJumpDrive());
    }

    @Test
    @DisplayName("Should set computer model with clamping")
    void testComputerModelClamping() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setComputerModel(-1);
        assertEquals(0, ship.getComputerModel());
        ship.setComputerModel(15);
        assertEquals(9, ship.getComputerModel());
    }

    @Test
    @DisplayName("Computer rating should be model × 10")
    void testComputerRating() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setComputerModel(5);
        assertEquals(50, ship.getComputerRating());
    }

    @Test
    @DisplayName("Should set armor and update protection")
    void testSetArmor() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setArmor(HighGuardArmor.CRYSTALIRON_2);
        assertEquals(HighGuardArmor.CRYSTALIRON_2, ship.getArmor());
        assertEquals(8, ship.getArmorProtection());
    }

    @Test
    @DisplayName("Should throw on null armor")
    void testNullArmor() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        assertThrows(NullPointerException.class, () -> ship.setArmor(null));
    }

    // ---- Derived stat tests ----

    @Test
    @DisplayName("Hull points should scale with tonnage")
    void testHullPointsByTonnage() {
        HighGuardShipDesign small = new HighGuardShipDesign(1, "Small", 500);
        // Trigger recalculation by setting a value
        small.setPowerPlantRating(1);
        // 500 tons: (500/100) * 10 = 50, min 10
        assertTrue(small.getHullPoints() >= 10, "Small ship should have at least 10 HP");

        HighGuardShipDesign medium = new HighGuardShipDesign(2, "Medium", 5000);
        medium.setPowerPlantRating(1);
        // 5000 tons: (5000/1000) * 20 = 100
        assertTrue(medium.getHullPoints() >= 100, "Medium ship should have at least 100 HP");

        HighGuardShipDesign large = new HighGuardShipDesign(3, "Large", 50000);
        large.setPowerPlantRating(1);
        // 50000 tons: (50000/10000) * 40 = 200
        assertTrue(large.getHullPoints() >= 200, "Large ship should have at least 200 HP");

        // Larger ships should have more HP
        assertTrue(large.getHullPoints() > medium.getHullPoints(), "Large ship should have more HP than medium");
        assertTrue(medium.getHullPoints() > small.getHullPoints(), "Medium ship should have more HP than small");
    }

    @Test
    @DisplayName("Armor should add bonus hull points")
    void testArmorBonusHullPoints() {
        HighGuardShipDesign ship1 = new HighGuardShipDesign(1, "No Armor", 4000);
        ship1.setPowerPlantRating(1); // Trigger recalculation
        int hpNoArmor = ship1.getHullPoints();

        HighGuardShipDesign ship2 = new HighGuardShipDesign(2, "With Armor", 4000);
        ship2.setPowerPlantRating(1); // Trigger recalculation
        ship2.setArmor(HighGuardArmor.TITANIUM_STEEL_2); // Protection 4, bonus = 4*2 = 8
        assertTrue(ship2.getHullPoints() > hpNoArmor, "Armored ship should have more HP");
        assertEquals(hpNoArmor + HighGuardArmor.TITANIUM_STEEL_2.getProtection() * 2, ship2.getHullPoints());
    }

    @Test
    @DisplayName("Energy points should be power plant × 10")
    void testEnergyPoints() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setPowerPlantRating(10);
        assertEquals(100, ship.getEnergyPoints());
    }

    @Test
    @DisplayName("Thrust should equal maneuver drive")
    void testThrust() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setManeuverDrive(3);
        assertEquals(3, ship.getThrust());
    }

    @Test
    @DisplayName("Agility should be calculated from power plant and tonnage")
    void testAgility() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setPowerPlantRating(10);
        // Agility = (10 * 10) / (4000 / 100) = 100 / 40 = 2
        assertEquals(2, ship.getAgility());
    }

    @Test
    @DisplayName("Agility should be at least 1 if powered")
    void testAgilityMinimum() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Heavy", 100000);
        ship.setPowerPlantRating(1);
        // Agility = (1 * 10) / (100000 / 100) = 10 / 1000 = 0, but minimum 1
        assertEquals(1, ship.getAgility());
    }

    @Test
    @DisplayName("Agility should be 0 if no power plant")
    void testAgilityZero() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        assertEquals(0, ship.getAgility());
    }

    // ---- Weapon mount tests ----

    @Test
    @DisplayName("Should add turret weapons")
    void testAddTurretWeapon() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addTurretWeapon(HighGuardWeapon.MISSILE_HE, HighGuardWeapon.TurretSize.SINGLE);

        assertEquals(2, ship.getTurretWeapons().size());
        assertEquals(0, ship.getBayWeapons().size());
    }

    @Test
    @DisplayName("Should add bay weapons")
    void testAddBayWeapon() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE);

        assertEquals(0, ship.getTurretWeapons().size());
        assertEquals(1, ship.getBayWeapons().size());
    }

    @Test
    @DisplayName("Should remove last turret")
    void testRemoveLastTurret() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        ship.addTurretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        ship.removeLastTurret();

        assertEquals(1, ship.getTurretWeapons().size());
        assertEquals(HighGuardWeapon.PULSE_LASER, ship.getTurretWeapons().get(0).getWeapon());
    }

    @Test
    @DisplayName("Should remove last bay")
    void testRemoveLastBay() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.SMALL);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_NUCLEAR, HighGuardWeapon.BaySize.LARGE);
        ship.removeLastBay();

        assertEquals(1, ship.getBayWeapons().size());
        assertEquals(HighGuardWeapon.TORPEDO_BASIC, ship.getBayWeapons().get(0).getWeapon());
    }

    @Test
    @DisplayName("Should safely remove from empty list")
    void testRemoveFromEmpty() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.removeLastTurret(); // Should not throw
        ship.removeLastBay();    // Should not throw
    }

    @Test
    @DisplayName("Weapon mount lists should be unmodifiable")
    void testUnmodifiableLists() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        assertThrows(UnsupportedOperationException.class, () -> ship.getTurretWeapons().add(
                new HighGuardShipDesign.WeaponMount(
                        HighGuardShipDesign.WeaponMount.MountType.TURRET,
                        HighGuardWeapon.PULSE_LASER,
                        HighGuardWeapon.TurretSize.SINGLE,
                        null)));
    }

    // ---- Weapon mount tests ----

    @Test
    @DisplayName("Turret weapon count should match turret size")
    void testTurretWeaponCount() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        assertEquals(1, ship.getTurretWeapons().get(0).getWeaponCount());

        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        assertEquals(2, ship.getTurretWeapons().get(1).getWeaponCount());

        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.TRIPLE);
        assertEquals(3, ship.getTurretWeapons().get(2).getWeaponCount());
    }

    @Test
    @DisplayName("Bay weapon count should be 2")
    void testBayWeaponCount() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE);
        assertEquals(2, ship.getBayWeapons().get(0).getWeaponCount());
    }

    @Test
    @DisplayName("Weapon mount should be operational by default")
    void testWeaponMountOperational() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        assertTrue(ship.getTurretWeapons().get(0).isOperational());
    }

    @Test
    @DisplayName("Weapon mount operational status should be settable")
    void testWeaponMountSetOperational() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        HighGuardShipDesign.WeaponMount mount = ship.getTurretWeapons().get(0);
        mount.setOperational(false);
        assertFalse(mount.isOperational());
    }

    // ---- Total weapon mounts and damage tests ----

    @Test
    @DisplayName("Total weapon mounts should count turrets and bays")
    void testTotalWeaponMounts() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);
        ship.addTurretWeapon(HighGuardWeapon.BEAM_LASER, HighGuardWeapon.TurretSize.DOUBLE);
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE);

        assertEquals(3, ship.getTotalWeaponMounts());
    }

    @Test
    @DisplayName("Total damage dice should sum all weapons")
    void testTotalDamageDice() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.DOUBLE); // 2 * 2 = 4
        ship.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.LARGE);       // 2 * 2 = 4

        assertEquals(8, ship.getTotalDamageDice());
    }

    // ---- Tonnage tests ----

    @Test
    @DisplayName("System tons should include drives, power plant, armor, weapons, and computer")
    void testSystemTons() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setManeuverDrive(2);
        ship.setJumpDrive(2);
        ship.setPowerPlantRating(10);
        ship.setComputerModel(3);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        ship.addTurretWeapon(HighGuardWeapon.PULSE_LASER, HighGuardWeapon.TurretSize.SINGLE);

        int tons = ship.getSystemTons();
        assertTrue(tons > 0, "System tons should be positive");
        assertTrue(tons < 4000, "System tons should be less than total tonnage");
    }

    @Test
    @DisplayName("Remaining tons should be total minus system tons")
    void testRemainingTons() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        int remaining = ship.getRemainingTons();
        assertTrue(remaining >= 0);
        assertEquals(4000 - ship.getSystemTons(), remaining);
    }

    @Test
    @DisplayName("Remaining tons should not be negative")
    void testRemainingTonsNotNegative() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 100);
        // Add lots of systems to a small ship
        ship.setManeuverDrive(6);
        ship.setJumpDrive(6);
        ship.setPowerPlantRating(50);
        ship.setComputerModel(9);
        ship.setArmor(HighGuardArmor.BONDED_SUPERDENSE_3);
        for (int i = 0; i < 10; i++) {
            ship.addTurretWeapon(HighGuardWeapon.MESON_GUN, HighGuardWeapon.TurretSize.TRIPLE);
        }

        assertTrue(ship.getRemainingTons() >= 0);
    }

    @Test
    @DisplayName("Ship should be invalid if system tons exceed total")
    void testIsValid() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 100);
        assertTrue(ship.isValid(), "Empty ship should be valid");

        ship.setManeuverDrive(6);
        ship.setJumpDrive(6);
        ship.setPowerPlantRating(50);
        ship.setArmor(HighGuardArmor.BONDED_SUPERDENSE_3);
        for (int i = 0; i < 20; i++) {
            ship.addTurretWeapon(HighGuardWeapon.MESON_GUN, HighGuardWeapon.TurretSize.TRIPLE);
        }
        assertFalse(ship.isValid(), "Overloaded ship should be invalid");
    }

    // ---- Construction cost tests ----

    @Test
    @DisplayName("Construction cost should be positive")
    void testConstructionCost() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setManeuverDrive(2);
        ship.setJumpDrive(2);
        assertTrue(ship.getConstructionCost() > 0, "Construction cost should be positive");
    }

    @Test
    @DisplayName("Construction cost should increase with tonnage")
    void testCostIncreasesWithTonnage() {
        HighGuardShipDesign small = new HighGuardShipDesign(1, "Small", 1000);
        small.setManeuverDrive(2);
        small.setJumpDrive(2);
        HighGuardShipDesign large = new HighGuardShipDesign(2, "Large", 10000);
        large.setManeuverDrive(2);
        large.setJumpDrive(2);

        assertTrue(large.getConstructionCost() > small.getConstructionCost(),
                "Larger ship should cost more");
    }

    @Test
    @DisplayName("Construction cost should increase with armor")
    void testCostIncreasesWithArmor() {
        HighGuardShipDesign ship1 = new HighGuardShipDesign(1, "No Armor", 4000);
        HighGuardShipDesign ship2 = new HighGuardShipDesign(2, "With Armor", 4000);
        ship2.setArmor(HighGuardArmor.CRYSTALIRON_2);

        assertTrue(ship2.getConstructionCost() > ship1.getConstructionCost());
    }

    // ---- Summary test ----

    @Test
    @DisplayName("Summary should contain ship name and stats")
    void testSummary() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test Ship", 4000);
        ship.setManeuverDrive(2);
        ship.setJumpDrive(2);
        ship.setPowerPlantRating(10);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);

        String summary = ship.getSummary();
        assertTrue(summary.contains("Test Ship"));
        assertTrue(summary.contains("4000"));
        assertTrue(summary.contains("M2"));
        assertTrue(summary.contains("J2"));
        assertTrue(summary.contains("PP10"));
    }

    // ---- Name setter test ----

    @Test
    @DisplayName("Should update ship name")
    void testSetName() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Old Name", 4000);
        ship.setName("New Name");
        assertEquals("New Name", ship.getName());
    }

    @Test
    @DisplayName("Should throw on null name update")
    void testSetNullName() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        assertThrows(NullPointerException.class, () -> ship.setName(null));
    }

    // ---- Crew and marines tests ----

    @Test
    @DisplayName("Should set crew and marines")
    void testCrewAndMarines() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setCrew(50);
        ship.setMarines(10);
        assertEquals(50, ship.getCrew());
        assertEquals(10, ship.getMarines());
    }

    // ---- Nuclear dampers and meson screens tests ----

    @Test
    @DisplayName("Should set nuclear dampers and meson screens")
    void testDefenses() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setNuclearDampers(2);
        ship.setMesonScreens(1);
        assertEquals(2, ship.getNuclearDampers());
        assertEquals(1, ship.getMesonScreens());
    }

    // ---- Sensor rating test ----

    @Test
    @DisplayName("Should set sensor rating")
    void testSensorRating() {
        HighGuardShipDesign ship = new HighGuardShipDesign(1, "Test", 4000);
        ship.setSensorRating(5);
        assertEquals(5, ship.getSensorRating());
    }
}
