package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardWeapon Tests")
class HighGuardWeaponTest {

    // ---- Enum values tests ----

    @Test
    @DisplayName("Should have 13 weapon types")
    void testWeaponCount() {
        assertEquals(13, HighGuardWeapon.values().length);
    }

    @Test
    @DisplayName("Should have 4 weapon categories")
    void testWeaponCategoryCount() {
        assertEquals(4, HighGuardWeapon.WeaponCategory.values().length);
    }

    @Test
    @DisplayName("Should have 3 turret sizes")
    void testTurretSizeCount() {
        assertEquals(3, HighGuardWeapon.TurretSize.values().length);
    }

    @Test
    @DisplayName("Should have 2 bay sizes")
    void testBaySizeCount() {
        assertEquals(2, HighGuardWeapon.BaySize.values().length);
    }

    // ---- Beam weapon tests ----

    @Test
    @DisplayName("Pulse Laser should be a beam weapon")
    void testPulseLaserIsBeam() {
        assertTrue(HighGuardWeapon.PULSE_LASER.isBeam());
        assertFalse(HighGuardWeapon.PULSE_LASER.isMissile());
        assertFalse(HighGuardWeapon.PULSE_LASER.isTorpedo());
        assertFalse(HighGuardWeapon.PULSE_LASER.isSpecial());
    }

    @Test
    @DisplayName("Beam Laser should be a beam weapon")
    void testBeamLaserIsBeam() {
        assertTrue(HighGuardWeapon.BEAM_LASER.isBeam());
        assertEquals(HighGuardWeapon.WeaponCategory.BEAM, HighGuardWeapon.BEAM_LASER.getCategory());
    }

    @Test
    @DisplayName("Particle Beam should be a beam weapon")
    void testParticleBeamIsBeam() {
        assertTrue(HighGuardWeapon.PARTICLE_BEAM.isBeam());
    }

    @Test
    @DisplayName("Fusion Gun should be a beam weapon")
    void testFusionGunIsBeam() {
        assertTrue(HighGuardWeapon.FUSION_GUN.isBeam());
    }

    @Test
    @DisplayName("Meson Gun should be a beam weapon")
    void testMesonGunIsBeam() {
        assertTrue(HighGuardWeapon.MESON_GUN.isBeam());
    }

    // ---- Special weapon tests ----

    @Test
    @DisplayName("Sandcaster should be a special weapon")
    void testSandcasterIsSpecial() {
        assertTrue(HighGuardWeapon.SANDCASTER.isSpecial());
        assertFalse(HighGuardWeapon.SANDCASTER.isBeam());
        assertEquals(HighGuardWeapon.WeaponCategory.SPECIAL, HighGuardWeapon.SANDCASTER.getCategory());
    }

    // ---- Missile weapon tests ----

    @Test
    @DisplayName("Missile HE should be a missile weapon")
    void testMissileHEIsMissile() {
        assertTrue(HighGuardWeapon.MISSILE_HE.isMissile());
        assertFalse(HighGuardWeapon.MISSILE_HE.isBeam());
        assertFalse(HighGuardWeapon.MISSILE_HE.isTorpedo());
    }

    @Test
    @DisplayName("Missile Nuclear should be a missile weapon")
    void testMissileNuclearIsMissile() {
        assertTrue(HighGuardWeapon.MISSILE_NUCLEAR.isMissile());
    }

    @Test
    @DisplayName("Missile Smart should be a missile weapon")
    void testMissileSmartIsMissile() {
        assertTrue(HighGuardWeapon.MISSILE_SMART.isMissile());
    }

    @Test
    @DisplayName("Missile Shockwave should be a missile weapon")
    void testMissileShockwaveIsMissile() {
        assertTrue(HighGuardWeapon.MISSILE_SHOCKWAVE.isMissile());
    }

    // ---- Torpedo weapon tests ----

    @Test
    @DisplayName("Torpedo Basic should be a torpedo weapon")
    void testTorpedoBasicIsTorpedo() {
        assertTrue(HighGuardWeapon.TORPEDO_BASIC.isTorpedo());
        assertFalse(HighGuardWeapon.TORPEDO_BASIC.isMissile());
        assertEquals(HighGuardWeapon.WeaponCategory.TORPEDO, HighGuardWeapon.TORPEDO_BASIC.getCategory());
    }

    @Test
    @DisplayName("Torpedo Nuclear should be a torpedo weapon")
    void testTorpedoNuclearIsTorpedo() {
        assertTrue(HighGuardWeapon.TORPEDO_NUCLEAR.isTorpedo());
    }

    @Test
    @DisplayName("Ortillery should be a torpedo weapon")
    void testOrtilleryIsTorpedo() {
        assertTrue(HighGuardWeapon.ORTILLERY.isTorpedo());
    }

    // ---- Range modifier tests ----

    @Test
    @DisplayName("Pulse Laser should have optimal range at Short")
    void testPulseLaserOptimalRange() {
        assertEquals(HighGuardRangeBand.SHORT, HighGuardWeapon.PULSE_LASER.getOptimalRange());
    }

    @Test
    @DisplayName("Pulse Laser should be out of range at Distant")
    void testPulseLaserOutOfRange() {
        assertFalse(HighGuardWeapon.PULSE_LASER.isInRange(HighGuardRangeBand.DISTANT));
    }

    @Test
    @DisplayName("Pulse Laser should be in range at Short")
    void testPulseLaserInRange() {
        assertTrue(HighGuardWeapon.PULSE_LASER.isInRange(HighGuardRangeBand.SHORT));
    }

    @Test
    @DisplayName("Beam Laser should have optimal range at Medium")
    void testBeamLaserOptimalRange() {
        assertEquals(HighGuardRangeBand.MEDIUM, HighGuardWeapon.BEAM_LASER.getOptimalRange());
    }

    @Test
    @DisplayName("Particle Beam should have optimal range at Long")
    void testParticleBeamOptimalRange() {
        assertEquals(HighGuardRangeBand.LONG, HighGuardWeapon.PARTICLE_BEAM.getOptimalRange());
    }

    @Test
    @DisplayName("Meson Gun should have optimal range at Long")
    void testMesonGunOptimalRange() {
        assertEquals(HighGuardRangeBand.LONG, HighGuardWeapon.MESON_GUN.getOptimalRange());
    }

    @Test
    @DisplayName("Sandcaster should have optimal range at Close")
    void testSandcasterOptimalRange() {
        assertEquals(HighGuardRangeBand.CLOSE, HighGuardWeapon.SANDCASTER.getOptimalRange());
    }

    @Test
    @DisplayName("Sandcaster should be out of range at Medium and beyond")
    void testSandcasterOutOfRange() {
        assertTrue(HighGuardWeapon.SANDCASTER.isInRange(HighGuardRangeBand.CLOSE));
        assertTrue(HighGuardWeapon.SANDCASTER.isInRange(HighGuardRangeBand.ADJACENT));
        assertFalse(HighGuardWeapon.SANDCASTER.isInRange(HighGuardRangeBand.MEDIUM));
        assertFalse(HighGuardWeapon.SANDCASTER.isInRange(HighGuardRangeBand.LONG));
        assertFalse(HighGuardWeapon.SANDCASTER.isInRange(HighGuardRangeBand.VERY_LONG));
        assertFalse(HighGuardWeapon.SANDCASTER.isInRange(HighGuardRangeBand.DISTANT));
    }

    @Test
    @DisplayName("Missiles should be in range at all range bands")
    void testMissilesInRange() {
        for (HighGuardRangeBand band : HighGuardRangeBand.values()) {
            assertTrue(HighGuardWeapon.MISSILE_HE.isInRange(band), "Missile HE should be in range at " + band);
        }
    }

    @Test
    @DisplayName("Torpedoes should be in range at all range bands")
    void testTorpedoesInRange() {
        for (HighGuardRangeBand band : HighGuardRangeBand.values()) {
            assertTrue(HighGuardWeapon.TORPEDO_BASIC.isInRange(band), "Torpedo should be in range at " + band);
        }
    }

    @Test
    @DisplayName("Pulse Laser hit modifiers should match Traveller SRD table")
    void testPulseLaserHitModifiers() {
        assertEquals(-1, HighGuardWeapon.PULSE_LASER.getHitModifier(HighGuardRangeBand.ADJACENT));
        assertEquals(-1, HighGuardWeapon.PULSE_LASER.getHitModifier(HighGuardRangeBand.CLOSE));
        assertEquals(0, HighGuardWeapon.PULSE_LASER.getHitModifier(HighGuardRangeBand.SHORT));
        assertEquals(-1, HighGuardWeapon.PULSE_LASER.getHitModifier(HighGuardRangeBand.MEDIUM));
        assertEquals(-2, HighGuardWeapon.PULSE_LASER.getHitModifier(HighGuardRangeBand.LONG));
        assertEquals(-3, HighGuardWeapon.PULSE_LASER.getHitModifier(HighGuardRangeBand.VERY_LONG));
    }

    @Test
    @DisplayName("Beam Laser hit modifiers should match Traveller SRD table")
    void testBeamLaserHitModifiers() {
        assertEquals(-2, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.ADJACENT));
        assertEquals(-1, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.CLOSE));
        assertEquals(-1, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.SHORT));
        assertEquals(0, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.MEDIUM));
        assertEquals(-1, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.LONG));
        assertEquals(-1, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.VERY_LONG));
        assertEquals(-2, HighGuardWeapon.BEAM_LASER.getHitModifier(HighGuardRangeBand.DISTANT));
    }

    @Test
    @DisplayName("Particle Beam hit modifiers should match Traveller SRD table")
    void testParticleBeamHitModifiers() {
        assertEquals(-3, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.ADJACENT));
        assertEquals(-2, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.CLOSE));
        assertEquals(-1, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.SHORT));
        assertEquals(-1, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.MEDIUM));
        assertEquals(0, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.LONG));
        assertEquals(-1, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.VERY_LONG));
        assertEquals(-1, HighGuardWeapon.PARTICLE_BEAM.getHitModifier(HighGuardRangeBand.DISTANT));
    }

    // ---- Flight time tests ----

    @Test
    @DisplayName("Beam weapons should have zero flight time")
    void testBeamFlightTime() {
        for (HighGuardRangeBand band : HighGuardRangeBand.values()) {
            assertEquals(0, HighGuardWeapon.PULSE_LASER.getFlightTime(band));
            assertEquals(0, HighGuardWeapon.BEAM_LASER.getFlightTime(band));
        }
    }

    @Test
    @DisplayName("Missile flight time should vary by range")
    void testMissileFlightTime() {
        assertEquals(0, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.ADJACENT));
        assertEquals(0, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.CLOSE));
        assertEquals(1, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.SHORT));
        assertEquals(1, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.MEDIUM));
        assertEquals(2, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.LONG));
        assertEquals(5, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.VERY_LONG));
        assertEquals(10, HighGuardWeapon.MISSILE_HE.getFlightTime(HighGuardRangeBand.DISTANT));
    }

    // ---- Tech level tests ----

    @Test
    @DisplayName("Pulse Laser should require TL6")
    void testPulseLaserTechLevel() {
        assertEquals(6, HighGuardWeapon.PULSE_LASER.getMinTechLevel());
    }

    @Test
    @DisplayName("Particle Beam should require TL11")
    void testParticleBeamTechLevel() {
        assertEquals(11, HighGuardWeapon.PARTICLE_BEAM.getMinTechLevel());
    }

    @Test
    @DisplayName("Fusion Gun should require TL12")
    void testFusionGunTechLevel() {
        assertEquals(12, HighGuardWeapon.FUSION_GUN.getMinTechLevel());
    }

    @Test
    @DisplayName("Meson Gun should require TL12")
    void testMesonGunTechLevel() {
        assertEquals(12, HighGuardWeapon.MESON_GUN.getMinTechLevel());
    }

    @Test
    @DisplayName("Missile Nuclear should require TL8")
    void testMissileNuclearTechLevel() {
        assertEquals(8, HighGuardWeapon.MISSILE_NUCLEAR.getMinTechLevel());
    }

    @Test
    @DisplayName("Missile Smart should require TL10")
    void testMissileSmartTechLevel() {
        assertEquals(10, HighGuardWeapon.MISSILE_SMART.getMinTechLevel());
    }

    @Test
    @DisplayName("Sandcaster should require TL9")
    void testSandcasterTechLevel() {
        assertEquals(9, HighGuardWeapon.SANDCASTER.getMinTechLevel());
    }

    // ---- Tonnage tests ----

    @Test
    @DisplayName("Pulse Laser turret tonnage should scale by turret size")
    void testPulseLaserTonnage() {
        assertEquals(100, HighGuardWeapon.PULSE_LASER.getTurretTonnage(HighGuardWeapon.TurretSize.SINGLE));
        assertEquals(200, HighGuardWeapon.PULSE_LASER.getTurretTonnage(HighGuardWeapon.TurretSize.DOUBLE));
        assertEquals(300, HighGuardWeapon.PULSE_LASER.getTurretTonnage(HighGuardWeapon.TurretSize.TRIPLE));
    }

    @Test
    @DisplayName("Meson Gun turret tonnage should be higher than Pulse Laser")
    void testMesonGunTonnage() {
        assertEquals(300, HighGuardWeapon.MESON_GUN.getTurretTonnage(HighGuardWeapon.TurretSize.SINGLE));
        assertEquals(600, HighGuardWeapon.MESON_GUN.getTurretTonnage(HighGuardWeapon.TurretSize.DOUBLE));
    }

    @Test
    @DisplayName("Bay tonnage should return 0 for non-missile weapons")
    void testBayTonnageNonMissile() {
        assertEquals(0, HighGuardWeapon.PULSE_LASER.getBayTonnage(HighGuardWeapon.BaySize.SMALL));
        assertEquals(0, HighGuardWeapon.BEAM_LASER.getBayTonnage(HighGuardWeapon.BaySize.LARGE));
    }

    @Test
    @DisplayName("Bay tonnage should return bay size for missiles")
    void testBayTonnageMissile() {
        assertEquals(50, HighGuardWeapon.MISSILE_HE.getBayTonnage(HighGuardWeapon.BaySize.SMALL));
        assertEquals(100, HighGuardWeapon.MISSILE_HE.getBayTonnage(HighGuardWeapon.BaySize.LARGE));
    }

    @Test
    @DisplayName("Bay tonnage should return bay size for torpedoes")
    void testBayTonnageTorpedo() {
        assertEquals(50, HighGuardWeapon.TORPEDO_BASIC.getBayTonnage(HighGuardWeapon.BaySize.SMALL));
        assertEquals(100, HighGuardWeapon.TORPEDO_BASIC.getBayTonnage(HighGuardWeapon.BaySize.LARGE));
    }

    // ---- Damage dice tests ----

    @Test
    @DisplayName("Beam weapons should have 2 base damage dice")
    void testBeamDamageDice() {
        assertEquals(2, HighGuardWeapon.PULSE_LASER.getBaseDamageDice());
        assertEquals(2, HighGuardWeapon.BEAM_LASER.getBaseDamageDice());
        assertEquals(2, HighGuardWeapon.PARTICLE_BEAM.getBaseDamageDice());
        assertEquals(2, HighGuardWeapon.MESON_GUN.getBaseDamageDice());
    }

    @Test
    @DisplayName("Missiles should have 1 base damage dice")
    void testMissileDamageDice() {
        assertEquals(1, HighGuardWeapon.MISSILE_HE.getBaseDamageDice());
        assertEquals(1, HighGuardWeapon.MISSILE_NUCLEAR.getBaseDamageDice());
    }

    @Test
    @DisplayName("Torpedoes should have 2 base damage dice")
    void testTorpedoDamageDice() {
        assertEquals(2, HighGuardWeapon.TORPEDO_BASIC.getBaseDamageDice());
        assertEquals(2, HighGuardWeapon.TORPEDO_NUCLEAR.getBaseDamageDice());
    }

    // ---- Name and description tests ----

    @Test
    @DisplayName("All weapons should have non-empty names")
    void testWeaponNames() {
        for (HighGuardWeapon w : HighGuardWeapon.values()) {
            assertNotNull(w.getName());
            assertFalse(w.getName().isEmpty());
        }
    }

    @Test
    @DisplayName("All weapons should have non-empty descriptions")
    void testWeaponDescriptions() {
        for (HighGuardWeapon w : HighGuardWeapon.values()) {
            assertNotNull(w.getDescription());
            assertFalse(w.getDescription().isEmpty());
        }
    }

    // ---- Turret size tests ----

    @Test
    @DisplayName("Turret size multipliers should be correct")
    void testTurretSizeMultipliers() {
        assertEquals(1, HighGuardWeapon.TurretSize.SINGLE.getMultiplier());
        assertEquals(2, HighGuardWeapon.TurretSize.DOUBLE.getMultiplier());
        assertEquals(3, HighGuardWeapon.TurretSize.TRIPLE.getMultiplier());
    }

    @Test
    @DisplayName("Turret size names should be correct")
    void testTurretSizeNames() {
        assertEquals("Single", HighGuardWeapon.TurretSize.SINGLE.getName());
        assertEquals("Double", HighGuardWeapon.TurretSize.DOUBLE.getName());
        assertEquals("Triple", HighGuardWeapon.TurretSize.TRIPLE.getName());
    }

    // ---- Bay size tests ----

    @Test
    @DisplayName("Bay size tons should be correct")
    void testBaySizeTons() {
        assertEquals(50, HighGuardWeapon.BaySize.SMALL.getTons());
        assertEquals(100, HighGuardWeapon.BaySize.LARGE.getTons());
    }

    // ---- Weapon category tests ----

    @Test
    @DisplayName("Weapon category names should be correct")
    void testWeaponCategoryNames() {
        assertEquals("Beam Weapon", HighGuardWeapon.WeaponCategory.BEAM.getName());
        assertEquals("Missile", HighGuardWeapon.WeaponCategory.MISSILE.getName());
        assertEquals("Torpedo", HighGuardWeapon.WeaponCategory.TORPEDO.getName());
        assertEquals("Special", HighGuardWeapon.WeaponCategory.SPECIAL.getName());
    }

    // ---- Grouping tests ----

    @Test
    @DisplayName("Should group weapons by category")
    void testWeaponsByCategory() {
        List<HighGuardWeapon> weapons = Arrays.asList(HighGuardWeapon.values());

        long beamCount = weapons.stream().filter(HighGuardWeapon::isBeam).count();
        long missileCount = weapons.stream().filter(HighGuardWeapon::isMissile).count();
        long torpedoCount = weapons.stream().filter(HighGuardWeapon::isTorpedo).count();
        long specialCount = weapons.stream().filter(HighGuardWeapon::isSpecial).count();

        assertEquals(5, beamCount, "Should have 5 beam weapons");
        assertEquals(4, missileCount, "Should have 4 missile weapons");
        assertEquals(3, torpedoCount, "Should have 3 torpedo weapons");
        assertEquals(1, specialCount, "Should have 1 special weapon");
    }
}
