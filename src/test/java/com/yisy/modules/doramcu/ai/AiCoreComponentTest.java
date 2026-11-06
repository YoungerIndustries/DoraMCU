package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiCoreComponent Tests")
class AiCoreComponentTest {

    @Test
    @DisplayName("should create component from module type")
    void fromModuleType() {
        AiCoreComponent basic = AiCoreComponent.fromModuleType(ModuleType.AI_CORE_BASIC);
        assertNotNull(basic);
        assertEquals(AiCoreComponent.Tier.BASIC, basic.getTier());

        AiCoreComponent advanced = AiCoreComponent.fromModuleType(ModuleType.AI_CORE_ADVANCED);
        assertEquals(AiCoreComponent.Tier.ADVANCED, advanced.getTier());

        AiCoreComponent elite = AiCoreComponent.fromModuleType(ModuleType.AI_CORE_ELITE);
        assertEquals(AiCoreComponent.Tier.ELITE, elite.getTier());
    }

    @Test
    @DisplayName("should provide damage bonus by tier")
    void damageBonus() {
        AiCoreComponent basic = new AiCoreComponent(AiCoreComponent.Tier.BASIC);
        AiCoreComponent advanced = new AiCoreComponent(AiCoreComponent.Tier.ADVANCED);
        AiCoreComponent elite = new AiCoreComponent(AiCoreComponent.Tier.ELITE);

        assertTrue(advanced.getDamageBonus() > basic.getDamageBonus());
        assertTrue(elite.getDamageBonus() > advanced.getDamageBonus());
    }

    @Test
    @DisplayName("should provide evasion bonus by tier")
    void evasionBonus() {
        AiCoreComponent basic = new AiCoreComponent(AiCoreComponent.Tier.BASIC);
        AiCoreComponent advanced = new AiCoreComponent(AiCoreComponent.Tier.ADVANCED);
        AiCoreComponent elite = new AiCoreComponent(AiCoreComponent.Tier.ELITE);

        assertTrue(advanced.getEvasionBonus() > basic.getEvasionBonus());
        assertTrue(elite.getEvasionBonus() > advanced.getEvasionBonus());
    }

    @Test
    @DisplayName("should provide speed bonus by tier")
    void speedBonus() {
        AiCoreComponent basic = new AiCoreComponent(AiCoreComponent.Tier.BASIC);
        AiCoreComponent advanced = new AiCoreComponent(AiCoreComponent.Tier.ADVANCED);
        AiCoreComponent elite = new AiCoreComponent(AiCoreComponent.Tier.ELITE);

        assertTrue(advanced.getSpeedBonus() > basic.getSpeedBonus());
        assertTrue(elite.getSpeedBonus() > advanced.getSpeedBonus());
    }

    @Test
    @DisplayName("should provide shield bonus by tier")
    void shieldBonus() {
        AiCoreComponent basic = new AiCoreComponent(AiCoreComponent.Tier.BASIC);
        AiCoreComponent advanced = new AiCoreComponent(AiCoreComponent.Tier.ADVANCED);
        AiCoreComponent elite = new AiCoreComponent(AiCoreComponent.Tier.ELITE);

        assertTrue(advanced.getShieldBonus() > basic.getShieldBonus());
        assertTrue(elite.getShieldBonus() > advanced.getShieldBonus());
    }

    @Test
    @DisplayName("should install AI core on ship")
    void installOnShip() {
        Ship ship = new Ship(1, ShipType.FRIGATE);
        assertFalse(ship.hasAiCore());

        AiCoreComponent component = new AiCoreComponent(AiCoreComponent.Tier.BASIC);
        ship.setAiCore(component);

        assertTrue(ship.hasAiCore());
        assertEquals(1, ship.getAiCoreLevel());
    }

    @Test
    @DisplayName("should remove AI core from ship")
    void removeFromShip() {
        Ship ship = new Ship(1, ShipType.FRIGATE);
        ship.setAiCore(new AiCoreComponent(AiCoreComponent.Tier.BASIC));
        assertTrue(ship.hasAiCore());

        ship.removeAiCore();
        assertFalse(ship.hasAiCore());
    }

    @Test
    @DisplayName("AI core bonuses should be positive")
    void bonusesPositive() {
        AiCoreComponent elite = new AiCoreComponent(AiCoreComponent.Tier.ELITE);
        assertTrue(elite.getDamageBonus() > 0.0);
        assertTrue(elite.getEvasionBonus() > 0.0);
        assertTrue(elite.getSpeedBonus() > 0.0);
        assertTrue(elite.getShieldBonus() > 0.0);
    }

    @Test
    @DisplayName("tiers should be enumerable")
    void tiers() {
        AiCoreComponent.Tier[] tiers = AiCoreComponent.Tier.values();
        assertEquals(3, tiers.length);
    }
}
