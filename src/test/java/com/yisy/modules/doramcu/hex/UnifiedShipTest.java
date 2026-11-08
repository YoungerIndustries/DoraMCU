package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedShip Tests")
class UnifiedShipTest {

    // ---- Constructor from ShipType (DoraMCU path) ----

    @Test
    @DisplayName("Should create a UnifiedShip from ShipType with default drives")
    void testConstructorFromShipType() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);

        assertEquals(1, ship.getId());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
        assertEquals("FRIGATE-1", ship.getName());
        assertEquals(FusionDrive.NONE, ship.getFusionDrive());
        assertEquals(JumpDrive.NONE, ship.getJumpDrive());
        assertEquals(HighGuardArmor.NONE, ship.getArmor());
        assertEquals(1000, ship.getTotalTonnage());
        assertNotNull(ship.getFuelTank());
    }

    @Test
    @DisplayName("Should create a UnifiedShip from different ShipTypes")
    void testConstructorFromDifferentShipTypes() {
        UnifiedShip destroyer = new UnifiedShip(2, ShipType.DESTROYER);
        assertEquals(2000, destroyer.getTotalTonnage());

        UnifiedShip cruiser = new UnifiedShip(3, ShipType.CRUISER);
        assertEquals(8000, cruiser.getTotalTonnage());

        UnifiedShip battleship = new UnifiedShip(4, ShipType.BATTLESHIP);
        assertEquals(27000, battleship.getTotalTonnage());
    }

    // ---- Constructor from HighGuardShipDesign (Traveller path) ----

    @Test
    @DisplayName("Should create a UnifiedShip from HighGuardShipDesign")
    void testConstructorFromDesign() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Explorer", 100000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        design.setArmor(HighGuardArmor.TITANIUM_STEEL_2);

        UnifiedShip ship = new UnifiedShip(design);

        assertEquals("Explorer", ship.getName());
        assertEquals(FusionDrive.M3, ship.getFusionDrive());
        assertEquals(JumpDrive.J3, ship.getJumpDrive());
        assertEquals(HighGuardArmor.TITANIUM_STEEL_2, ship.getArmor());
        assertEquals(100000, ship.getTotalTonnage());
        assertNotNull(ship.getFuelTank());
        assertNotNull(ship.getManeuverStatus());
        assertNotNull(ship.getJumpTravelStatus());
    }

    @Test
    @DisplayName("Should throw on null design")
    void testConstructorFromNullDesign() {
        assertThrows(NullPointerException.class, () -> new UnifiedShip((HighGuardShipDesign) null));
    }

    @Test
    @DisplayName("Should map armor to hull shape and material")
    void testArmorToHullMapping() {
        // Titanium Steel -> CUBE + Titanium
        HighGuardShipDesign titaniumDesign = new HighGuardShipDesign(1, "Titan", 10000);
        titaniumDesign.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        UnifiedShip titaniumShip = new UnifiedShip(titaniumDesign);
        assertEquals(HullShape.CUBE, titaniumShip.getHullShape());
        assertEquals(Material.Titanium, titaniumShip.getExteriorMaterial());

        // Crystaliron -> DOME + Crystaliron
        HighGuardShipDesign crystalDesign = new HighGuardShipDesign(2, "Crystal", 10000);
        crystalDesign.setArmor(HighGuardArmor.CRYSTALIRON_1);
        UnifiedShip crystalShip = new UnifiedShip(crystalDesign);
        assertEquals(HullShape.DOME, crystalShip.getHullShape());
        assertEquals(Material.Crystaliron, crystalShip.getExteriorMaterial());

        // Bonded Superdense -> DOME + Superdense
        HighGuardShipDesign superdenseDesign = new HighGuardShipDesign(3, "Superdense", 10000);
        superdenseDesign.setArmor(HighGuardArmor.BONDED_SUPERDENSE_1);
        UnifiedShip superdenseShip = new UnifiedShip(superdenseDesign);
        assertEquals(HullShape.DOME, superdenseShip.getHullShape());
        assertEquals(Material.Superdense, superdenseShip.getExteriorMaterial());

        // None -> CUBE + Aluminum
        HighGuardShipDesign noArmorDesign = new HighGuardShipDesign(4, "NoArmor", 10000);
        UnifiedShip noArmorShip = new UnifiedShip(noArmorDesign);
        assertEquals(HullShape.CUBE, noArmorShip.getHullShape());
        assertEquals(Material.Aluminum, noArmorShip.getExteriorMaterial());
    }

    @Test
    @DisplayName("Should map tonnage to correct ShipType")
    void testTonnageToShipTypeMapping() {
        // <= 1000 -> FRIGATE
        HighGuardShipDesign small = new HighGuardShipDesign(1, "Small", 1000);
        UnifiedShip s1 = new UnifiedShip(small);
        assertEquals(ShipType.FRIGATE, s1.getShipType());

        // <= 2000 -> DESTROYER
        HighGuardShipDesign medium = new HighGuardShipDesign(2, "Medium", 2000);
        UnifiedShip s2 = new UnifiedShip(medium);
        assertEquals(ShipType.DESTROYER, s2.getShipType());

        // <= 12000 -> CRUISER
        HighGuardShipDesign large = new HighGuardShipDesign(3, "Large", 12000);
        UnifiedShip s3 = new UnifiedShip(large);
        assertEquals(ShipType.CRUISER, s3.getShipType());

        // <= 27000 -> BATTLESHIP
        HighGuardShipDesign capital = new HighGuardShipDesign(4, "Capital", 27000);
        UnifiedShip s4 = new UnifiedShip(capital);
        assertEquals(ShipType.BATTLESHIP, s4.getShipType());

        // <= 64000 -> CARRIER
        HighGuardShipDesign carrier = new HighGuardShipDesign(5, "Carrier", 64000);
        UnifiedShip s5 = new UnifiedShip(carrier);
        assertEquals(ShipType.CARRIER, s5.getShipType());

        // > 64000 -> MOTHERSHIP
        HighGuardShipDesign mothership = new HighGuardShipDesign(6, "Mega", 100000);
        UnifiedShip s6 = new UnifiedShip(mothership);
        assertEquals(ShipType.MOTHERSHIP, s6.getShipType());
    }

    // ---- Drive Accessors ----

    @Test
    @DisplayName("Should set and get FusionDrive")
    void testSetFusionDrive() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M3);
        assertEquals(FusionDrive.M3, ship.getFusionDrive());
    }

    @Test
    @DisplayName("Should throw on null FusionDrive")
    void testSetNullFusionDrive() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertThrows(NullPointerException.class, () -> ship.setFusionDrive(null));
    }

    @Test
    @DisplayName("Should set and get JumpDrive")
    void testSetJumpDrive() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setJumpDrive(JumpDrive.J3);
        assertEquals(JumpDrive.J3, ship.getJumpDrive());
    }

    @Test
    @DisplayName("Should throw on null JumpDrive")
    void testSetNullJumpDrive() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertThrows(NullPointerException.class, () -> ship.setJumpDrive(null));
    }

    // ---- Speed Calculations ----

    @Test
    @DisplayName("MDrive speed should combine ShipType base + FusionDrive bonus")
    void testMDriveSpeed() {
        // FRIGATE base MDrive = 10
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);

        // No fusion drive: speed = base only
        assertEquals(10, ship.getMDriveSpeed());

        // Add M3 fusion: speed = 10 + 3*3 = 19
        ship.setFusionDrive(FusionDrive.M3);
        assertEquals(19, ship.getMDriveSpeed());

        // Add M6 fusion: speed = 10 + 6*3 = 28
        ship.setFusionDrive(FusionDrive.M6);
        assertEquals(28, ship.getMDriveSpeed());
    }

    @Test
    @DisplayName("JDrive speed should combine ShipType base + JumpDrive bonus")
    void testJDriveSpeed() {
        // FRIGATE base JDrive = 12
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);

        // No jump drive: speed = base only
        assertEquals(12, ship.getJDriveSpeed());

        // Add J3 jump: speed = 12 + 3*2 = 18
        ship.setJumpDrive(JumpDrive.J3);
        assertEquals(18, ship.getJDriveSpeed());

        // Add J6 jump: speed = 12 + 6*2 = 24
        ship.setJumpDrive(JumpDrive.J6);
        assertEquals(24, ship.getJDriveSpeed());
    }

    @Test
    @DisplayName("Effective MDrive speed should be halved when fuel is empty")
    void testEffectiveMDriveSpeedWithNoFuel() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 1000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        UnifiedShip ship = new UnifiedShip(design);
        int normalSpeed = ship.getEffectiveMDriveSpeed();

        // Drain fuel
        ship.getFuelTank().consumeFuel(ship.getFuelTank().getCurrentFuelTons());
        assertTrue(ship.getFuelTank().isEmpty());

        int lowFuelSpeed = ship.getEffectiveMDriveSpeed();
        assertTrue(lowFuelSpeed < normalSpeed);
        assertTrue(lowFuelSpeed >= 1);
    }

    @Test
    @DisplayName("Effective JDrive speed should be 0 when fuel is empty")
    void testEffectiveJDriveSpeedWithNoFuel() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 1000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        UnifiedShip ship = new UnifiedShip(design);
        assertTrue(ship.getEffectiveJDriveSpeed() > 0);

        // Drain fuel
        ship.getFuelTank().consumeFuel(ship.getFuelTank().getCurrentFuelTons());
        assertEquals(0, ship.getEffectiveJDriveSpeed());
    }

    // ---- Fuel Management ----

    @Test
    @DisplayName("Should manage fuel correctly")
    void testFuelManagement() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 1000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        UnifiedShip ship = new UnifiedShip(design);

        FuelTank tank = ship.getFuelTank();
        assertNotNull(tank);
        assertTrue(ship.getMaxFuelCapacity() > 0);

        // Refuel to full first, then check capacity
        ship.refuelToFull();
        int capacity = ship.getMaxFuelCapacity();
        assertEquals(capacity, ship.getCurrentFuelTons());
    }

    @Test
    @DisplayName("Should return jumps remaining")
    void testJumpsRemaining() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M3);
        ship.setJumpDrive(JumpDrive.J3);

        int jumps = ship.getJumpsRemaining();
        assertTrue(jumps >= 0);
    }

    // ---- Armor ----

    @Test
    @DisplayName("Should set and get armor")
    void testSetArmor() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertEquals(0, ship.getArmorProtection());

        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_2);
        assertEquals(HighGuardArmor.TITANIUM_STEEL_2, ship.getArmor());
        assertEquals(4, ship.getArmorProtection());

        ship.setArmor(HighGuardArmor.CRYSTALIRON_3);
        assertEquals(12, ship.getArmorProtection());
    }

    @Test
    @DisplayName("Should throw on null armor")
    void testSetNullArmor() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertThrows(NullPointerException.class, () -> ship.setArmor(null));
    }

    // ---- Tonnage ----

    @Test
    @DisplayName("Should set and get total tonnage")
    void testSetTotalTonnage() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertEquals(1000, ship.getTotalTonnage());

        ship.setTotalTonnage(5000);
        assertEquals(5000, ship.getTotalTonnage());
    }

    @Test
    @DisplayName("Should throw on non-positive tonnage")
    void testSetInvalidTonnage() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertThrows(IllegalArgumentException.class, () -> ship.setTotalTonnage(0));
        assertThrows(IllegalArgumentException.class, () -> ship.setTotalTonnage(-100));
    }

    // ---- Drive Tonnage Calculations ----

    @Test
    @DisplayName("Should calculate fusion drive tons")
    void testFusionDriveTons() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setTotalTonnage(100000);
        ship.setFusionDrive(FusionDrive.M3);

        // 3 * max(1000, 100000/100) = 3 * 1000 = 3000
        int tons = ship.getFusionDriveTons();
        assertEquals(3000, tons);
    }

    @Test
    @DisplayName("Should calculate jump drive tons")
    void testJumpDriveTons() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setTotalTonnage(100000);
        ship.setJumpDrive(JumpDrive.J3);

        // 3 * max(1000, 100000/100) = 3 * 1000 = 3000
        int tons = ship.getJumpDriveTons();
        assertEquals(3000, tons);
    }

    @Test
    @DisplayName("Should calculate total drive tons")
    void testTotalDriveTons() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setTotalTonnage(100000);
        ship.setFusionDrive(FusionDrive.M3);
        ship.setJumpDrive(JumpDrive.J3);

        assertEquals(6000, ship.getTotalDriveTons());
    }

    // ---- Status Checks ----

    @Test
    @DisplayName("Should not be able to maneuver without fusion drive")
    void testCanManeuverWithoutDrive() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertFalse(ship.canManeuver());
    }

    @Test
    @DisplayName("Should not be able to jump without jump drive")
    void testCanJumpWithoutDrive() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertFalse(ship.canJump());
    }

    @Test
    @DisplayName("Should not be stranded when fuel is available")
    void testIsNotStrandedWithFuel() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M3);
        assertFalse(ship.isStranded());
    }

    // ---- Maneuver and Jump Status ----

    @Test
    @DisplayName("Should have maneuver and jump status from design constructor")
    void testMovementStatusFromDesign() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 10000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);

        UnifiedShip ship = new UnifiedShip(design);
        assertNotNull(ship.getManeuverStatus());
        assertNotNull(ship.getJumpTravelStatus());
    }

    @Test
    @DisplayName("Should set current system")
    void testSetCurrentSystem() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 1000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);
        UnifiedShip ship = new UnifiedShip(design);

        TravellerWorld world = new TravellerWorld(
                1, "Alpha Centauri",
                UniversalWorldProfile.parse("A0886A20"),
                0, 0, false, false);

        ship.setCurrentSystem(world);
        assertEquals(world, ship.getCurrentWorld());
    }

    @Test
    @DisplayName("Should throw on null world")
    void testSetNullWorld() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertThrows(NullPointerException.class, () -> ship.setCurrentSystem(null));
    }

    // ---- Name Setting ----

    @Test
    @DisplayName("Should set custom name")
    void testSetName() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setName("USS Enterprise");
        assertEquals("USS Enterprise", ship.getName());
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should contain ship details")
    void testToString() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setName("TestShip");
        ship.setFusionDrive(FusionDrive.M3);
        ship.setJumpDrive(JumpDrive.J3);

        String str = ship.toString();
        assertTrue(str.contains("TestShip"));
        assertTrue(str.contains("FRIGATE"));
        assertTrue(str.contains("fusion"));
        assertTrue(str.contains("jump"));
    }

    // ---- Component Integration ----

    @Test
    @DisplayName("MDrive speed should include component bonuses")
    void testMDriveSpeedWithComponents() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        // FRIGATE base MDrive = 10, no fusion

        // Add MDrive component qty 1 -> bonus = 2
        ship.addComponent(ShipComponent.mDrive(1));
        assertEquals(12, ship.getMDriveSpeed());

        // Add MDrive component qty 2 -> bonus = 4
        ship.addComponent(ShipComponent.mDrive(2));
        assertEquals(16, ship.getMDriveSpeed());
    }

    @Test
    @DisplayName("JDrive speed should include component bonuses")
    void testJDriveSpeedWithComponents() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        // FRIGATE base JDrive = 12, no jump

        // Add JDrive component qty 1 -> bonus = 2
        ship.addComponent(ShipComponent.jDrive(1));
        assertEquals(14, ship.getJDriveSpeed());
    }

    // ---- Visited Worlds ----

    @Test
    @DisplayName("Should return empty visited worlds by default")
    void testVisitedWorldsEmpty() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        assertTrue(ship.getVisitedWorlds().isEmpty());
    }

    @Test
    @DisplayName("Should track visited worlds from design constructor")
    void testVisitedWorldsFromDesign() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 10000);
        design.setManeuverDrive(3);
        design.setJumpDrive(3);

        UnifiedShip ship = new UnifiedShip(design);
        // JumpTravelStatus should have the initial world
        assertFalse(ship.getVisitedWorlds().isEmpty());
    }
}
