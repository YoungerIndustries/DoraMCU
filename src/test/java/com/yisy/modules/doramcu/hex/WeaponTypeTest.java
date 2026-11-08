package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeaponType Enum Tests")
class WeaponTypeTest {

    @Test
    @DisplayName("values - should have 7 weapon types")
    void values_shouldHaveThreeTypes() {
        WeaponType[] types = WeaponType.values();

        assertEquals(7, types.length);
    }

    @Test
    @DisplayName("BEAM - should have correct stats")
    void beam_shouldHaveCorrectStats() {
        WeaponType beam = WeaponType.BEAM;

        assertEquals(StrategicResource.ELERIUM, beam.getRequiredResource());
        assertEquals(10, beam.getBaseDamage());
        assertEquals(1, beam.getRange());
    }

    @Test
    @DisplayName("MISSILE - should have correct stats")
    void missile_shouldHaveCorrectStats() {
        WeaponType missile = WeaponType.MISSILE;

        assertEquals(StrategicResource.ANTI_MATTER, missile.getRequiredResource());
        assertEquals(15, missile.getBaseDamage());
        assertEquals(3, missile.getRange());
    }

    @Test
    @DisplayName("MASS_DRIVER - should have correct stats")
    void massDriver_shouldHaveCorrectStats() {
        WeaponType massDriver = WeaponType.MASS_DRIVER;

        assertEquals(StrategicResource.THULIUM, massDriver.getRequiredResource());
        assertEquals(12, massDriver.getBaseDamage());
        assertEquals(2, massDriver.getRange());
    }

    @Test
    @DisplayName("MISSILE should have highest damage")
    void missile_shouldHaveHighestDamage() {
        assertTrue(WeaponType.MISSILE.getBaseDamage() > WeaponType.BEAM.getBaseDamage());
        assertTrue(WeaponType.MISSILE.getBaseDamage() > WeaponType.MASS_DRIVER.getBaseDamage());
    }

    @Test
    @DisplayName("BEAM should have shortest range")
    void beam_shouldHaveShortestRange() {
        assertEquals(1, WeaponType.BEAM.getRange());
        assertTrue(WeaponType.BEAM.getRange() < WeaponType.MASS_DRIVER.getRange());
        assertTrue(WeaponType.MASS_DRIVER.getRange() < WeaponType.MISSILE.getRange());
    }

    @Test
    @DisplayName("Each weapon should require a different resource")
    void eachWeapon_shouldRequireDifferentResource() {
        assertNotEquals(WeaponType.BEAM.getRequiredResource(), WeaponType.MISSILE.getRequiredResource());
        assertNotEquals(WeaponType.BEAM.getRequiredResource(), WeaponType.MASS_DRIVER.getRequiredResource());
        assertNotEquals(WeaponType.MISSILE.getRequiredResource(), WeaponType.MASS_DRIVER.getRequiredResource());
    }

    @Test
    @DisplayName("valueOf - should resolve by name")
    void valueOf_shouldResolveByName() {
        assertSame(WeaponType.MISSILE, WeaponType.valueOf("MISSILE"));
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> WeaponType.valueOf("INVALID"));
    }
}
