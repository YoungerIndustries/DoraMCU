package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedCombatStats Tests")
class UnifiedCombatStatsTest {

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create combat stats from UnifiedShip")
    void testConstructorFromShip() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M3);

        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        assertNotNull(stats);
        assertEquals(3, stats.getThrust()); // From M3 fusion drive
        assertTrue(stats.getEnergyPoints() > 0);
        assertTrue(stats.getAgility() >= 1);
    }

    @Test
    @DisplayName("Should create combat stats from UnifiedShip and HighGuardShipDesign")
    void testConstructorFromDesign() {
        HighGuardShipDesign design = new HighGuardShipDesign(1, "Test", 100000);
        design.setManeuverDrive(4);
        design.setJumpDrive(3);
        design.setPowerPlantRating(4);
        design.setNuclearDampers(2);
        design.setMesonScreens(1);
        design.setComputerModel(5);
        design.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.SINGLE);

        UnifiedShip ship = new UnifiedShip(design);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship, design);

        assertEquals(4, stats.getThrust());
        assertEquals(40, stats.getEnergyPoints());
        assertEquals(2, stats.getNuclearDampers());
        assertEquals(1, stats.getMesonScreens());
        assertEquals(5, stats.getComputerModel());
        assertEquals(1, stats.getTotalWeaponMounts());
    }

    // ---- Derived Stat Calculation ----

    @Test
    @DisplayName("Should recalculate derived stats")
    void testRecalculateDerivedStats() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M5);
        ship.setTotalTonnage(10000);

        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        stats.recalculateDerivedStats();

        assertEquals(5, stats.getThrust());
        assertEquals(50, stats.getEnergyPoints());
        // Agility = (5 * 10) / (10000 / 100) = 50 / 100 = 0 -> min 1
        assertEquals(1, stats.getAgility());
    }

    @Test
    @DisplayName("Agility should be 0 when thrust is 0")
    void testAgilityZeroThrust() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        // Default FusionDrive.NONE -> thrust = 0
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        assertEquals(0, stats.getAgility());
    }

    // ---- Nuclear Dampers and Meson Screens ----

    @Test
    @DisplayName("Should set nuclear dampers with floor at 0")
    void testSetNuclearDampers() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        stats.setNuclearDampers(5);
        assertEquals(5, stats.getNuclearDampers());

        stats.setNuclearDampers(-3);
        assertEquals(0, stats.getNuclearDampers());
    }

    @Test
    @DisplayName("Should set meson screens with floor at 0")
    void testSetMesonScreens() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        stats.setMesonScreens(3);
        assertEquals(3, stats.getMesonScreens());

        stats.setMesonScreens(-1);
        assertEquals(0, stats.getMesonScreens());
    }

    // ---- Computer Model ----

    @Test
    @DisplayName("Should set computer model with clamping 0-9")
    void testSetComputerModel() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        stats.setComputerModel(5);
        assertEquals(5, stats.getComputerModel());

        stats.setComputerModel(-1);
        assertEquals(0, stats.getComputerModel());

        stats.setComputerModel(15);
        assertEquals(9, stats.getComputerModel());
    }

    // ---- Weapon Mount Management ----

    @Test
    @DisplayName("Should add turret weapons")
    void testAddTurretWeapon() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        stats.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.SINGLE);
        stats.addTurretWeapon(HighGuardWeapon.FUSION_GUN, HighGuardWeapon.TurretSize.DOUBLE);

        assertEquals(2, stats.getTurretWeapons().size());
        assertEquals(2, stats.getTotalWeaponMounts());
    }

    @Test
    @DisplayName("Should add bay weapons")
    void testAddBayWeapon() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        stats.addBayWeapon(HighGuardWeapon.TORPEDO_BASIC, HighGuardWeapon.BaySize.SMALL);

        assertEquals(1, stats.getBayWeapons().size());
        assertEquals(1, stats.getTotalWeaponMounts());
    }

    @Test
    @DisplayName("Should return unmodifiable weapon lists")
    void testUnmodifiableWeaponLists() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        assertThrows(UnsupportedOperationException.class, () ->
                stats.getTurretWeapons().add(null));
        assertThrows(UnsupportedOperationException.class, () ->
                stats.getBayWeapons().add(null));
    }

    // ---- Damage Calculations ----

    @Test
    @DisplayName("Total damage should combine DoraMCU and High Guard damage")
    void testTotalDamage() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.addComponent(new ShipComponent(WeaponType.BEAM, 2));
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        int baseDamage = stats.getTotalDamage();

        // Add High Guard weapon
        stats.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.SINGLE);
        int combinedDamage = stats.getTotalDamage();

        assertTrue(combinedDamage > baseDamage);
    }

    @Test
    @DisplayName("Effective damage should include computer bonus")
    void testEffectiveDamageWithComputer() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.addComponent(new ShipComponent(WeaponType.BEAM, 2));
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        int baseDamage = stats.getTotalDamage();
        int baseEffective = stats.getEffectiveDamage();

        stats.setComputerModel(5); // 5 * 0.05 = 25% bonus
        int boostedEffective = stats.getEffectiveDamage();

        assertTrue(boostedEffective > baseEffective);
    }

    @Test
    @DisplayName("Effective hull points should include armor bonus")
    void testEffectiveHullPointsWithArmor() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_2); // protection = 4
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        int withoutArmor = new UnifiedCombatStats(new UnifiedShip(2, ShipType.FRIGATE))
                .getEffectiveHullPoints();

        assertTrue(stats.getEffectiveHullPoints() > withoutArmor);
    }

    @Test
    @DisplayName("Effective evasion should include agility bonus")
    void testEffectiveEvasionWithAgility() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M6);
        ship.setTotalTonnage(1000);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        double baseEvasion = stats.getEffectiveEvasion();
        // Agility should boost evasion
        assertTrue(baseEvasion >= 0.0);
        assertTrue(baseEvasion <= 1.0);
    }

    // ---- Armor Absorption ----

    @Test
    @DisplayName("Should calculate armor absorption")
    void testCalculateArmorAbsorption() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_2); // protection = 4
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        // Normal weapon — full armor absorption
        int absorbed = stats.calculateArmorAbsorption(10, HighGuardWeapon.PARTICLE_BEAM);
        assertEquals(4, absorbed);

        // Meson gun — 50% bypass
        int mesonAbsorbed = stats.calculateArmorAbsorption(10, HighGuardWeapon.MESON_GUN);
        assertEquals(2, mesonAbsorbed);
    }

    @Test
    @DisplayName("Should return 0 absorption for no armor")
    void testNoArmorAbsorption() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        assertEquals(0, stats.calculateArmorAbsorption(10, HighGuardWeapon.PARTICLE_BEAM));
    }

    @Test
    @DisplayName("Should cap absorption at incoming damage")
    void testAbsorptionCappedAtDamage() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.CRYSTALIRON_3); // protection = 12
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        int absorbed = stats.calculateArmorAbsorption(5, HighGuardWeapon.PARTICLE_BEAM);
        assertEquals(5, absorbed);
    }

    // ---- Damage Application ----

    @Test
    @DisplayName("Should apply damage with armor absorption")
    void testApplyDamageWithArmor() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_2); // protection = 4
        ship.addComponent(ShipComponent.shield(2));
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        int initialHull = ship.getCurrentHullPoints();
        int initialShields = ship.getCurrentShieldPoints();

        // 10 damage: 4 absorbed by armor, rest to shields/hull
        stats.applyDamageWithArmor(10, HighGuardWeapon.PARTICLE_BEAM);

        // Hull should not have taken all 10 damage
        int hullDamage = initialHull - ship.getCurrentHullPoints();
        assertTrue(hullDamage < 10);
    }

    @Test
    @DisplayName("Should return 0 damage for destroyed ship")
    void testApplyDamageToDestroyedShip() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        // Destroy the ship
        ship.repairHull(-ship.getMaxHullPoints());
        assertTrue(ship.isDestroyed());

        assertEquals(0, stats.applyDamageWithArmor(10, HighGuardWeapon.PARTICLE_BEAM));
    }

    @Test
    @DisplayName("Should return 0 damage for non-positive damage")
    void testApplyZeroDamage() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        assertEquals(0, stats.applyDamageWithArmor(0, HighGuardWeapon.PARTICLE_BEAM));
        assertEquals(0, stats.applyDamageWithArmor(-5, HighGuardWeapon.PARTICLE_BEAM));
    }

    // ---- High Guard Combat Integration ----

    @Test
    @DisplayName("Should create High Guard combat instance")
    void testCreateCombat() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M3);
        ship.setJumpDrive(JumpDrive.J3);
        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        HighGuardShipDesign opponent = new HighGuardShipDesign(2, "Enemy", 10000);
        opponent.setManeuverDrive(2);
        opponent.setJumpDrive(2);

        HighGuardShipCombat combat = stats.createCombat(opponent, HighGuardRangeBand.SHORT);
        assertNotNull(combat);
    }

    @Test
    @DisplayName("Should create design from ship")
    void testCreateDesignFromShip() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setName("TestShip");
        ship.setFusionDrive(FusionDrive.M4);
        ship.setJumpDrive(JumpDrive.J3);
        ship.setArmor(HighGuardArmor.CRYSTALIRON_2);
        ship.setTotalTonnage(50000);

        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        stats.setNuclearDampers(2);
        stats.setMesonScreens(1);
        stats.setComputerModel(4);
        stats.addTurretWeapon(HighGuardWeapon.PARTICLE_BEAM, HighGuardWeapon.TurretSize.SINGLE);

        HighGuardShipDesign design = stats.createDesignFromShip();

        assertEquals("TestShip", design.getName());
        assertEquals(50000, design.getTotalTonnage());
        assertEquals(4, design.getManeuverDrive());
        assertEquals(3, design.getJumpDrive());
        assertEquals(HighGuardArmor.CRYSTALIRON_2, design.getArmor());
        assertEquals(2, design.getNuclearDampers());
        assertEquals(1, design.getMesonScreens());
        assertEquals(4, design.getComputerModel());
        assertEquals(1, design.getTurretWeapons().size());
    }

    // ---- Status Checks ----

    @Test
    @DisplayName("Should detect armor presence")
    void testHasArmor() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        assertFalse(stats.hasArmor());

        ship.setArmor(HighGuardArmor.TITANIUM_STEEL_1);
        assertTrue(stats.hasArmor());
    }

    @Test
    @DisplayName("Should detect nuclear dampers")
    void testHasNuclearDampers() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        assertFalse(stats.hasNuclearDampers());

        stats.setNuclearDampers(1);
        assertTrue(stats.hasNuclearDampers());
    }

    @Test
    @DisplayName("Should detect meson screens")
    void testHasMesonScreens() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        assertFalse(stats.hasMesonScreens());

        stats.setMesonScreens(1);
        assertTrue(stats.hasMesonScreens());
    }

    // ---- Defense Ratings ----

    @Test
    @DisplayName("Should return nuclear defense rating")
    void testNuclearDefenseRating() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        stats.setNuclearDampers(3);
        assertEquals(3, stats.getNuclearDefenseRating());
    }

    @Test
    @DisplayName("Should return meson defense rating")
    void testMesonDefenseRating() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        stats.setMesonScreens(2);
        assertEquals(2, stats.getMesonDefenseRating());
    }

    // ---- Armor Protection Accessor ----

    @Test
    @DisplayName("Should return armor protection value")
    void testGetArmorProtection() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);
        assertEquals(0, stats.getArmorProtection());

        ship.setArmor(HighGuardArmor.CRYSTALIRON_2);
        assertEquals(8, stats.getArmorProtection());
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should contain combat stats details")
    void testToString() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M3);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        String str = stats.toString();
        assertTrue(str.contains("damage="));
        assertTrue(str.contains("thrust="));
        assertTrue(str.contains("agility="));
        assertTrue(str.contains("armor="));
    }

    // ---- Meson Bypass Verification ----

    @Test
    @DisplayName("Meson guns should bypass 50% of armor")
    void testMesonBypass() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setArmor(HighGuardArmor.CRYSTALIRON_2); // protection = 8
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        // Normal weapon: full protection
        int normalAbsorbed = stats.calculateArmorAbsorption(20, HighGuardWeapon.PARTICLE_BEAM);
        assertEquals(8, normalAbsorbed);

        // Meson gun: 50% bypass -> effective protection = 4
        int mesonAbsorbed = stats.calculateArmorAbsorption(20, HighGuardWeapon.MESON_GUN);
        assertEquals(4, mesonAbsorbed);
    }

    // ---- Energy Points Calculation ----

    @Test
    @DisplayName("Energy points should be thrust * 10")
    void testEnergyPointsCalculation() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.setFusionDrive(FusionDrive.M5);
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        assertEquals(50, stats.getEnergyPoints());
    }

    // ---- Default applyDamage (DoraMCU shield-first) ----

    @Test
    @DisplayName("Should apply damage using shield-first model")
    void testApplyDamageShieldFirst() {
        UnifiedShip ship = new UnifiedShip(1, ShipType.FRIGATE);
        ship.addComponent(ShipComponent.shield(2));
        ship.fullRepair(); // Recharge shields to max
        UnifiedCombatStats stats = new UnifiedCombatStats(ship);

        int initialHull = ship.getCurrentHullPoints();
        int initialShields = ship.getCurrentShieldPoints();
        assertTrue(initialShields > 0, "Shields should be charged");

        stats.applyDamage(5);

        // Shields should absorb first
        int shieldLoss = initialShields - ship.getCurrentShieldPoints();
        assertTrue(shieldLoss > 0);
    }
}
