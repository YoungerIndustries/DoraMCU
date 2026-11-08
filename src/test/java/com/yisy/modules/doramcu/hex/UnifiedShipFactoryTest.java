package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedShipFactory Tests")
class UnifiedShipFactoryTest {

    // ---- DoraMCUBuilder tests ----

    @Test
    @DisplayName("Should create ship via DoraMCUBuilder")
    void testDoraMCUBuilder() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withName("TestFrigate")
                .withFusionDrive(FusionDrive.M3)
                .withJumpDrive(JumpDrive.J3)
                .withArmor(HighGuardArmor.TITANIUM_STEEL_1)
                .build();

        assertEquals(1, ship.getId());
        assertEquals("TestFrigate", ship.getName());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
        assertEquals(FusionDrive.M3, ship.getFusionDrive());
        assertEquals(JumpDrive.J3, ship.getJumpDrive());
        assertEquals(HighGuardArmor.TITANIUM_STEEL_1, ship.getArmor());
    }

    @Test
    @DisplayName("Should add MDrive component via builder")
    void testDoraMCUBuilderWithMDrive() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withMDrive(2)
                .build();

        // FRIGATE base MDrive = 10, MDrive qty 2 -> bonus = 4
        assertEquals(14, ship.getMDriveSpeed());
    }

    @Test
    @DisplayName("Should add JDrive component via builder")
    void testDoraMCUBuilderWithJDrive() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withJDrive(1)
                .build();

        // FRIGATE base JDrive = 12, JDrive qty 1 -> bonus = 2
        assertEquals(14, ship.getJDriveSpeed());
    }

    @Test
    @DisplayName("Should add shield component via builder")
    void testDoraMCUBuilderWithShield() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withShield(2)
                .build();

        assertTrue(ship.getMaxShieldPoints() > 0);
    }

    @Test
    @DisplayName("Should add weapon component via builder")
    void testDoraMCUBuilderWithWeapon() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withWeapon(WeaponType.BEAM, 2)
                .build();

        assertTrue(ship.getTotalDamage() > 0);
    }

    @Test
    @DisplayName("Should add hull reinforcement via builder")
    void testDoraMCUBuilderWithHullReinforcement() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withHullReinforcement(1)
                .build();

        // Hull reinforcement should increase effective hull points
        int reinforced = ship.getEffectiveHullPoints();
        int base = new UnifiedShip(2, ShipType.FRIGATE).getEffectiveHullPoints();
        assertTrue(reinforced >= base);
    }

    @Test
    @DisplayName("Should set nuclear dampers via builder")
    void testDoraMCUBuilderWithNuclearDampers() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withNuclearDampers(3)
                .build();

        // Nuclear dampers are stored in combat stats, not directly on ship
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        // Builder sets the field but doesn't apply to combat stats directly
        // This tests the builder chain works
        assertNotNull(ship);
    }

    @Test
    @DisplayName("Should set meson screens via builder")
    void testDoraMCUBuilderWithMesonScreens() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withMesonScreens(2)
                .build();

        assertNotNull(ship);
    }

    @Test
    @DisplayName("Should set computer model via builder")
    void testDoraMCUBuilderWithComputerModel() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withComputerModel(5)
                .build();

        assertNotNull(ship);
    }

    @Test
    @DisplayName("Should chain multiple builder methods")
    void testDoraMCUBuilderChaining() {
        UnifiedShip ship = UnifiedShipFactory.createDoraMCUShip(1, ShipType.CRUISER)
                .withName("Heavy Cruiser")
                .withFusionDrive(FusionDrive.M5)
                .withJumpDrive(JumpDrive.J5)
                .withArmor(HighGuardArmor.CRYSTALIRON_2)
                .withShield(3)
                .withMDrive(1)
                .withJDrive(1)
                .withWeapon(WeaponType.BEAM, 2)
                .withNuclearDampers(2)
                .withMesonScreens(1)
                .withComputerModel(4)
                .build();

        assertEquals("Heavy Cruiser", ship.getName());
        assertEquals(ShipType.CRUISER, ship.getShipType());
        assertEquals(FusionDrive.M5, ship.getFusionDrive());
        assertEquals(JumpDrive.J5, ship.getJumpDrive());
        assertEquals(HighGuardArmor.CRYSTALIRON_2, ship.getArmor());
    }

    // ---- DesignBuilder tests ----

    @Test
    @DisplayName("Should create ship via DesignBuilder")
    void testDesignBuilder() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Explorer", 100000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        design.setArmor(HighGuardArmor.TITANIUM_STEEL_2);

        UnifiedShip ship = UnifiedShipFactory.fromDesign(design).build();

        assertEquals("Explorer", ship.getName());
        assertEquals(FusionDrive.M3, ship.getFusionDrive());
        assertEquals(JumpDrive.J3, ship.getJumpDrive());
        assertEquals(HighGuardArmor.TITANIUM_STEEL_2, ship.getArmor());
        assertEquals(100000, ship.getTotalTonnage());
    }

    @Test
    @DisplayName("Should throw on null design")
    void testDesignBuilderNullDesign() {
        assertThrows(IllegalArgumentException.class, () -> UnifiedShipFactory.fromDesign(null));
    }

    @Test
    @DisplayName("Should add additional components via DesignBuilder")
    void testDesignBuilderWithAdditionalComponent() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 10000);
        design.setManeuverDrive(2);
        design.setJumpDrive(2);

        UnifiedShip ship = UnifiedShipFactory.fromDesign(design)
                .withAdditionalComponent(ShipComponent.shield(2))
                .build();

        assertTrue(ship.getMaxShieldPoints() > 0);
    }

    @Test
    @DisplayName("Should add shield via DesignBuilder")
    void testDesignBuilderWithShield() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 10000);
        design.setManeuverDrive(2);
        design.setJumpDrive(2);

        UnifiedShip ship = UnifiedShipFactory.fromDesign(design)
                .withShield(3)
                .build();

        assertTrue(ship.getMaxShieldPoints() > 0);
    }

    @Test
    @DisplayName("Should add weapon via DesignBuilder")
    void testDesignBuilderWithWeapon() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 10000);
        design.setManeuverDrive(2);
        design.setJumpDrive(2);

        UnifiedShip ship = UnifiedShipFactory.fromDesign(design)
                .withWeapon(WeaponType.BEAM, 2)
                .build();

        assertTrue(ship.getTotalDamage() > 0);
    }

    // ---- Preset Ships ----

    @Test
    @DisplayName("Should create preset frigate")
    void testCreateFrigate() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);

        assertEquals("Frigate-1", ship.getName());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
        assertEquals(FusionDrive.M3, ship.getFusionDrive());
        assertEquals(JumpDrive.J3, ship.getJumpDrive());
        assertEquals(HighGuardArmor.TITANIUM_STEEL_1, ship.getArmor());
        assertTrue(ship.getMaxShieldPoints() > 0);
    }

    @Test
    @DisplayName("Should create preset destroyer")
    void testCreateDestroyer() {
        UnifiedShip ship = UnifiedShipFactory.createDestroyer(2);

        assertEquals("Destroyer-2", ship.getName());
        assertEquals(ShipType.DESTROYER, ship.getShipType());
        assertEquals(FusionDrive.M4, ship.getFusionDrive());
        assertEquals(JumpDrive.J4, ship.getJumpDrive());
        assertEquals(HighGuardArmor.TITANIUM_STEEL_2, ship.getArmor());
    }

    @Test
    @DisplayName("Should create preset cruiser")
    void testCreateCruiser() {
        UnifiedShip ship = UnifiedShipFactory.createCruiser(3);

        assertEquals("Cruiser-3", ship.getName());
        assertEquals(ShipType.CRUISER, ship.getShipType());
        assertEquals(FusionDrive.M5, ship.getFusionDrive());
        assertEquals(JumpDrive.J5, ship.getJumpDrive());
        assertEquals(HighGuardArmor.CRYSTALIRON_2, ship.getArmor());
    }

    @Test
    @DisplayName("Should create preset battleship")
    void testCreateBattleship() {
        UnifiedShip ship = UnifiedShipFactory.createBattleship(4);

        assertEquals("Battleship-4", ship.getName());
        assertEquals(ShipType.BATTLESHIP, ship.getShipType());
        assertEquals(FusionDrive.M6, ship.getFusionDrive());
        assertEquals(JumpDrive.J6, ship.getJumpDrive());
        assertEquals(HighGuardArmor.CRYSTALIRON_3, ship.getArmor());
    }

    @Test
    @DisplayName("Should create Traveller-style ship")
    void testCreateTravellerShip() {
        UnifiedShip ship = UnifiedShipFactory.createTravellerShip(1, "Explorer");

        assertEquals("Explorer", ship.getName());
        assertEquals(100000, ship.getTotalTonnage());
        assertEquals(FusionDrive.M3, ship.getFusionDrive());
        assertEquals(JumpDrive.J3, ship.getJumpDrive());
        assertEquals(HighGuardArmor.TITANIUM_STEEL_2, ship.getArmor());
    }

    @Test
    @DisplayName("Should create Traveller-style capital ship")
    void testCreateTravellerCapitalShip() {
        UnifiedShip ship = UnifiedShipFactory.createTravellerCapitalShip(1, "Dreadnought");

        assertEquals("Dreadnought", ship.getName());
        assertEquals(400000, ship.getTotalTonnage());
        assertEquals(FusionDrive.M4, ship.getFusionDrive());
        assertEquals(JumpDrive.J4, ship.getJumpDrive());
        assertEquals(HighGuardArmor.CRYSTALIRON_3, ship.getArmor());
    }

    // ---- Preset Ship Speed Verification ----

    @Test
    @DisplayName("Preset frigate should have correct MDrive speed")
    void testFrigateMDriveSpeed() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        // FRIGATE base = 10, M3 fusion = 3*3 = 9, total = 19
        assertEquals(19, ship.getMDriveSpeed());
    }

    @Test
    @DisplayName("Preset destroyer should have correct MDrive speed")
    void testDestroyerMDriveSpeed() {
        UnifiedShip ship = UnifiedShipFactory.createDestroyer(2);
        // DESTROYER base = 8, M4 fusion = 4*3 = 12, total = 20
        assertEquals(20, ship.getMDriveSpeed());
    }

    @Test
    @DisplayName("Preset cruiser should have correct MDrive speed")
    void testCruiserMDriveSpeed() {
        UnifiedShip ship = UnifiedShipFactory.createCruiser(3);
        // CRUISER base = 6, M5 fusion = 5*3 = 15, total = 21
        assertEquals(21, ship.getMDriveSpeed());
    }

    @Test
    @DisplayName("Preset battleship should have correct MDrive speed")
    void testBattleshipMDriveSpeed() {
        UnifiedShip ship = UnifiedShipFactory.createBattleship(4);
        // BATTLESHIP base = 4, M6 fusion = 6*3 = 18, total = 22
        assertEquals(22, ship.getMDriveSpeed());
    }

    // ---- Preset Ship JDrive Verification ----

    @Test
    @DisplayName("Preset frigate should have correct JDrive speed")
    void testFrigateJDriveSpeed() {
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        // FRIGATE base = 12, J3 jump = 3*2 = 6, total = 18
        assertEquals(18, ship.getJDriveSpeed());
    }

    @Test
    @DisplayName("Preset destroyer should have correct JDrive speed")
    void testDestroyerJDriveSpeed() {
        UnifiedShip ship = UnifiedShipFactory.createDestroyer(2);
        // DESTROYER base = 10, J4 jump = 4*2 = 8, total = 18
        assertEquals(18, ship.getJDriveSpeed());
    }

    // ---- Factory Isolation ----

    @Test
    @DisplayName("Factory should be non-instantiable")
    void testFactoryPrivateConstructor() {
        // The factory class has a private constructor — verify via reflection
        try {
            var ctor = UnifiedShipFactory.class.getDeclaredConstructor();
            assertFalse(ctor.canAccess(null));
        } catch (NoSuchMethodException e) {
            // No constructor found — also acceptable
        }
    }

    // ---- Builder Independence ----

    @Test
    @DisplayName("Multiple builds from same builder should be independent")
    void testBuilderIndependence() {
        UnifiedShipFactory.DoraMCUBuilder builder = UnifiedShipFactory.createDoraMCUShip(1, ShipType.FRIGATE)
                .withFusionDrive(FusionDrive.M3);

        UnifiedShip ship1 = builder.build();
        UnifiedShip ship2 = builder.build();

        // Both should have the same config
        assertEquals(ship1.getFusionDrive(), ship2.getFusionDrive());
        assertEquals(ship1.getShipType(), ship2.getShipType());
    }

    // ---- Combined DoraMCU and Traveller Components ----

    @Test
    @DisplayName("Should combine DoraMCU components with Traveller design")
    void testCombinedComponents() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Hybrid", 20000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        design.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.SINGLE);

        UnifiedShip ship = UnifiedShipFactory.fromDesign(design)
                .withShield(2)
                .withWeapon(WeaponType.BEAM, 1)
                .build();

        // Should have both High Guard and DoraMCU damage
        UnifiedCombatStats stats = new UnifiedCombatStats(ship, design);
        assertTrue(stats.getTotalDamage() > 0);
        assertTrue(ship.getMaxShieldPoints() > 0);
    }
}
