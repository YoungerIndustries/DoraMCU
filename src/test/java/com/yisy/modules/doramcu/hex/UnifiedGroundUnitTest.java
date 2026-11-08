package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedGroundUnit Tests")
class UnifiedGroundUnitTest {

    @Test
    @DisplayName("Should create unit")
    void testCreateUnit() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertEquals(1, unit.getId());
        assertEquals("Alpha", unit.getName());
        assertEquals(ArmyUnitType.INFANTRY, unit.getUnitType());
        assertEquals(100, unit.getCurrentStrength());
        assertEquals(100, unit.getMaxStrength());
    }

    @Test
    @DisplayName("Should create from ArmyUnit")
    void testFromArmyUnit() {
        ArmyUnit army = new ArmyUnit(1, ArmyUnitType.INFANTRY);
        UnifiedGroundUnit unit = new UnifiedGroundUnit(army);
        assertEquals(1, unit.getId());
        assertEquals(ArmyUnitType.INFANTRY, unit.getUnitType());
    }

    @Test
    @DisplayName("Should create from MarineUnit")
    void testFromMarineUnit() {
        MarineUnit marine = new MarineUnit("Charlie", 5, 100,
                MarineUnit.EquipmentLevel.HEAVY, MarineUnit.Morale.ELITE, 3);
        UnifiedGroundUnit unit = new UnifiedGroundUnit(marine);
        assertEquals("Charlie", unit.getName());
        assertEquals(MarineUnit.EquipmentLevel.HEAVY, unit.getEquipment());
    }

    @Test
    @DisplayName("Should take damage")
    void testTakeDamage() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertFalse(unit.takeDamage(50));
        assertEquals(50, unit.getCurrentStrength());
        assertTrue(unit.takeDamage(60));
        assertEquals(0, unit.getCurrentStrength());
    }

    @Test
    @DisplayName("Should repair")
    void testRepair() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        unit.takeDamage(50);
        unit.repair(30);
        assertEquals(80, unit.getCurrentStrength());
    }

    @Test
    @DisplayName("Should gain experience")
    void testGainExperience() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        unit.gainExperience(10);
        assertEquals(10, unit.getExperience());
    }

    @Test
    @DisplayName("Should adjust morale")
    void testAdjustMorale() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        unit.adjustMorale(-20);
        assertEquals(80, unit.getMoraleValue());
        unit.adjustMorale(30);
        assertEquals(100, unit.getMoraleValue());
    }

    @Test
    @DisplayName("Should be combat-effective")
    void testCombatEffective() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertTrue(unit.isCombatEffective());
        unit.takeDamage(100);
        assertFalse(unit.isCombatEffective());
    }

    @Test
    @DisplayName("Should get casualty percentage")
    void testCasualtyPercentage() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertEquals(0.0, unit.getCasualtyPercentage(), 0.01);
        unit.takeDamage(50);
        assertEquals(50.0, unit.getCasualtyPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should get effective offense")
    void testEffectiveOffense() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertTrue(unit.getEffectiveOffense() > 0);
    }

    @Test
    @DisplayName("Should get combat rating")
    void testCombatRating() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertTrue(unit.getCombatRating() > 0);
    }

    @Test
    @DisplayName("Should get supply cost")
    void testSupplyCost() {
        UnifiedGroundUnit unit = new UnifiedGroundUnit(1, "Alpha", ArmyUnitType.INFANTRY,
                MarineUnit.EquipmentLevel.STANDARD, MarineUnit.Morale.REGULAR);
        assertTrue(unit.getSupplyCost() > 0);
    }
}
