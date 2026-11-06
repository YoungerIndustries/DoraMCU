package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipComponent Tests")
class ShipComponentTest {

    // ---- Weapon Component ----

    @Test
    @DisplayName("Weapon constructor - should create weapon component")
    void weaponConstructor_shouldCreateWeaponComponent() {
        ShipComponent component = new ShipComponent(WeaponType.BEAM, 2);

        assertEquals(ShipComponent.ComponentType.WEAPON, component.getType());
        assertEquals(WeaponType.BEAM, component.getWeaponType());
        assertEquals(2, component.getQuantity());
    }

    @Test
    @DisplayName("Weapon constructor - null weapon type should throw")
    void weaponConstructor_nullWeapon_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new ShipComponent(null, 1));
    }

    @Test
    @DisplayName("Weapon constructor - quantity 0 should throw")
    void weaponConstructor_quantityZero_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new ShipComponent(WeaponType.BEAM, 0));
    }

    @Test
    @DisplayName("Weapon constructor - quantity 4 should throw")
    void weaponConstructor_quantityFour_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new ShipComponent(WeaponType.BEAM, 4));
    }

    @Test
    @DisplayName("Weapon damage - should calculate base damage × quantity")
    void weaponDamage_shouldCalculateCorrectly() {
        ShipComponent component = new ShipComponent(WeaponType.BEAM, 3);

        assertEquals(30, component.getDamage()); // 10 * 3
    }

    @Test
    @DisplayName("Weapon damage - MISSILE x2 should be 30")
    void weaponDamage_missile_shouldCalculateCorrectly() {
        ShipComponent component = new ShipComponent(WeaponType.MISSILE, 2);

        assertEquals(30, component.getDamage()); // 15 * 2
    }

    @Test
    @DisplayName("Weapon required resource - should return weapon type resource")
    void weaponRequiredResource_shouldReturnWeaponResource() {
        ShipComponent component = new ShipComponent(WeaponType.MISSILE, 1);

        assertEquals(StrategicResource.ANTI_MATTER, component.getRequiredResource());
    }

    // ---- Shield Component ----

    @Test
    @DisplayName("Shield constructor - should create shield component")
    void shieldConstructor_shouldCreateShieldComponent() {
        ShipComponent component = new ShipComponent(2);

        assertEquals(ShipComponent.ComponentType.SHIELD, component.getType());
        assertNull(component.getWeaponType());
        assertEquals(2, component.getQuantity());
    }

    @Test
    @DisplayName("Shield factory - should create shield component")
    void shieldFactory_shouldCreateShieldComponent() {
        ShipComponent component = ShipComponent.shield(3);

        assertEquals(ShipComponent.ComponentType.SHIELD, component.getType());
        assertEquals(3, component.getQuantity());
    }

    @Test
    @DisplayName("Shield strength - should calculate 20 × quantity")
    void shieldStrength_shouldCalculateCorrectly() {
        ShipComponent component = ShipComponent.shield(3);

        assertEquals(60, component.getShieldStrength()); // 20 * 3
    }

    @Test
    @DisplayName("Shield damage - should be 0")
    void shieldDamage_shouldBeZero() {
        ShipComponent component = ShipComponent.shield(2);

        assertEquals(0, component.getDamage());
    }

    @Test
    @DisplayName("Shield required resource - should be Elerium")
    void shieldRequiredResource_shouldBeElerium() {
        ShipComponent component = ShipComponent.shield(1);

        assertEquals(StrategicResource.ELERIUM, component.getRequiredResource());
    }

    @Test
    @DisplayName("Shield constructor - quantity 0 should throw")
    void shieldConstructor_quantityZero_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new ShipComponent(0));
    }

    // ---- Hull Reinforcement ----

    @Test
    @DisplayName("Hull reinforcement factory - should create hull component")
    void hullReinforcementFactory_shouldCreateHullComponent() {
        ShipComponent component = ShipComponent.hullReinforcement(2);

        assertEquals(ShipComponent.ComponentType.HULL_REINFORCEMENT, component.getType());
        assertNull(component.getWeaponType());
        assertEquals(2, component.getQuantity());
    }

    @Test
    @DisplayName("Hull bonus - should calculate 15 × quantity")
    void hullBonus_shouldCalculateCorrectly() {
        ShipComponent component = ShipComponent.hullReinforcement(3);

        assertEquals(45, component.getHullBonus()); // 15 * 3
    }

    @Test
    @DisplayName("Hull damage - should be 0")
    void hullDamage_shouldBeZero() {
        ShipComponent component = ShipComponent.hullReinforcement(2);

        assertEquals(0, component.getDamage());
    }

    @Test
    @DisplayName("Hull shield strength - should be 0")
    void hullShieldStrength_shouldBeZero() {
        ShipComponent component = ShipComponent.hullReinforcement(2);

        assertEquals(0, component.getShieldStrength());
    }

    @Test
    @DisplayName("Hull required resource - should be Durantium")
    void hullRequiredResource_shouldBeDurantium() {
        ShipComponent component = ShipComponent.hullReinforcement(1);

        assertEquals(StrategicResource.DURANTIUM, component.getRequiredResource());
    }

    // ---- MDrive Component ----

    @Test
    @DisplayName("MDrive factory - should create MDrive component")
    void mDriveFactory_shouldCreateMDriveComponent() {
        ShipComponent component = ShipComponent.mDrive(2);

        assertEquals(ShipComponent.ComponentType.MDRIVE, component.getType());
        assertNull(component.getWeaponType());
        assertEquals(2, component.getQuantity());
    }

    @Test
    @DisplayName("MDrive bonus - should calculate 2 × quantity")
    void mDriveBonus_shouldCalculateCorrectly() {
        ShipComponent component = ShipComponent.mDrive(3);

        assertEquals(6, component.getMDriveBonus()); // 2 * 3
    }

    @Test
    @DisplayName("MDrive bonus - non-MDrive should be 0")
    void mDriveBonus_nonMDrive_shouldBeZero() {
        ShipComponent component = ShipComponent.shield(2);

        assertEquals(0, component.getMDriveBonus());
    }

    @Test
    @DisplayName("MDrive required resource - should be Elerium")
    void mDriveRequiredResource_shouldBeElerium() {
        ShipComponent component = ShipComponent.mDrive(1);

        assertEquals(StrategicResource.ELERIUM, component.getRequiredResource());
    }

    // ---- JDrive Component ----

    @Test
    @DisplayName("JDrive factory - should create JDrive component")
    void jDriveFactory_shouldCreateJDriveComponent() {
        ShipComponent component = ShipComponent.jDrive(2);

        assertEquals(ShipComponent.ComponentType.JDRIVE, component.getType());
        assertNull(component.getWeaponType());
        assertEquals(2, component.getQuantity());
    }

    @Test
    @DisplayName("JDrive bonus - should calculate 2 × quantity")
    void jDriveBonus_shouldCalculateCorrectly() {
        ShipComponent component = ShipComponent.jDrive(3);

        assertEquals(6, component.getJDriveBonus()); // 2 * 3
    }

    @Test
    @DisplayName("JDrive bonus - non-JDrive should be 0")
    void jDriveBonus_nonJDrive_shouldBeZero() {
        ShipComponent component = ShipComponent.shield(2);

        assertEquals(0, component.getJDriveBonus());
    }

    @Test
    @DisplayName("JDrive required resource - should be Elerium")
    void jDriveRequiredResource_shouldBeElerium() {
        ShipComponent component = ShipComponent.jDrive(1);

        assertEquals(StrategicResource.ELERIUM, component.getRequiredResource());
    }

    // ---- ComponentType enum ----

    @Test
    @DisplayName("ComponentType - should have 5 values")
    void componentType_shouldHaveFiveValues() {
        ShipComponent.ComponentType[] types = ShipComponent.ComponentType.values();

        assertEquals(5, types.length);
    }

    // ---- Equals and HashCode ----

    @Test
    @DisplayName("equals - same weapon components should be equal")
    void equals_sameWeapon_shouldBeEqual() {
        ShipComponent c1 = new ShipComponent(WeaponType.BEAM, 2);
        ShipComponent c2 = new ShipComponent(WeaponType.BEAM, 2);

        assertEquals(c1, c2);
    }

    @Test
    @DisplayName("equals - different weapon type should not be equal")
    void equals_differentWeapon_shouldNotBeEqual() {
        ShipComponent c1 = new ShipComponent(WeaponType.BEAM, 2);
        ShipComponent c2 = new ShipComponent(WeaponType.MISSILE, 2);

        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("equals - different quantity should not be equal")
    void equals_differentQuantity_shouldNotBeEqual() {
        ShipComponent c1 = new ShipComponent(WeaponType.BEAM, 1);
        ShipComponent c2 = new ShipComponent(WeaponType.BEAM, 2);

        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("equals - different component types should not be equal")
    void equals_differentType_shouldNotBeEqual() {
        ShipComponent c1 = new ShipComponent(WeaponType.BEAM, 2);
        ShipComponent c2 = ShipComponent.shield(2);

        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("hashCode - equal components should have equal hash codes")
    void hashCode_equalComponents_shouldHaveEqualHash() {
        ShipComponent c1 = new ShipComponent(WeaponType.BEAM, 2);
        ShipComponent c2 = new ShipComponent(WeaponType.BEAM, 2);

        assertEquals(c1.hashCode(), c2.hashCode());
    }

    // ---- toString ----

    @Test
    @DisplayName("toString - weapon should include type, quantity, and damage")
    void toString_weapon_shouldIncludeDetails() {
        ShipComponent component = new ShipComponent(WeaponType.BEAM, 2);
        String str = component.toString();

        assertTrue(str.contains("BEAM"));
        assertTrue(str.contains("x2"));
        assertTrue(str.contains("20"));
    }

    @Test
    @DisplayName("toString - shield should include quantity and strength")
    void toString_shield_shouldIncludeDetails() {
        ShipComponent component = ShipComponent.shield(3);
        String str = component.toString();

        assertTrue(str.contains("Shield"));
        assertTrue(str.contains("x3"));
        assertTrue(str.contains("60"));
    }

    @Test
    @DisplayName("toString - hull should include quantity and bonus")
    void toString_hull_shouldIncludeDetails() {
        ShipComponent component = ShipComponent.hullReinforcement(2);
        String str = component.toString();

        assertTrue(str.contains("Hull+"));
        assertTrue(str.contains("x2"));
        assertTrue(str.contains("30"));
    }

    @Test
    @DisplayName("toString - MDrive should include quantity and bonus")
    void toString_mDrive_shouldIncludeDetails() {
        ShipComponent component = ShipComponent.mDrive(2);
        String str = component.toString();

        assertTrue(str.contains("MDrive"));
        assertTrue(str.contains("x2"));
        assertTrue(str.contains("+4"));
    }

    @Test
    @DisplayName("toString - JDrive should include quantity and bonus")
    void toString_jDrive_shouldIncludeDetails() {
        ShipComponent component = ShipComponent.jDrive(3);
        String str = component.toString();

        assertTrue(str.contains("JDrive"));
        assertTrue(str.contains("x3"));
        assertTrue(str.contains("+6"));
    }
}
