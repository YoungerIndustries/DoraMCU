package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardArmor Tests")
class HighGuardArmorTest {

    // ---- Enum values tests ----

    @Test
    @DisplayName("Should have 14 armor presets")
    void testArmorCount() {
        assertEquals(14, HighGuardArmor.values().length);
    }

    @Test
    @DisplayName("Should have 4 armor materials")
    void testArmorMaterialCount() {
        assertEquals(4, HighGuardArmor.ArmorMaterial.values().length);
    }

    // ---- None armor tests ----

    @Test
    @DisplayName("None armor should have zero protection")
    void testNoneArmor() {
        assertEquals(0, HighGuardArmor.NONE.getProtection());
        assertEquals(0, HighGuardArmor.NONE.getLayers());
        assertEquals(0, HighGuardArmor.NONE.getHullPercent());
        assertEquals(0.0, HighGuardArmor.NONE.getCostMultiplier());
        assertEquals(HighGuardArmor.ArmorMaterial.NONE, HighGuardArmor.NONE.getMaterial());
    }

    // ---- Titanium Steel tests ----

    @Test
    @DisplayName("Titanium Steel x1 should have 2 protection")
    void testTitaniumSteel1() {
        assertEquals(2, HighGuardArmor.TITANIUM_STEEL_1.getProtection());
        assertEquals(1, HighGuardArmor.TITANIUM_STEEL_1.getLayers());
        assertEquals(7, HighGuardArmor.TITANIUM_STEEL_1.getMinTechLevel());
        assertEquals(HighGuardArmor.ArmorMaterial.TITANIUM_STEEL, HighGuardArmor.TITANIUM_STEEL_1.getMaterial());
    }

    @Test
    @DisplayName("Titanium Steel x5 should have 10 protection")
    void testTitaniumSteel5() {
        assertEquals(10, HighGuardArmor.TITANIUM_STEEL_5.getProtection());
        assertEquals(5, HighGuardArmor.TITANIUM_STEEL_5.getLayers());
        assertEquals(25, HighGuardArmor.TITANIUM_STEEL_5.getHullPercent());
    }

    @Test
    @DisplayName("Titanium Steel protection should scale linearly")
    void testTitaniumSteelScaling() {
        assertEquals(2, HighGuardArmor.TITANIUM_STEEL_1.getProtection());
        assertEquals(4, HighGuardArmor.TITANIUM_STEEL_2.getProtection());
        assertEquals(6, HighGuardArmor.TITANIUM_STEEL_3.getProtection());
        assertEquals(8, HighGuardArmor.TITANIUM_STEEL_4.getProtection());
        assertEquals(10, HighGuardArmor.TITANIUM_STEEL_5.getProtection());
    }

    // ---- Crystaliron tests ----

    @Test
    @DisplayName("Crystaliron x1 should have 4 protection")
    void testCrystaliron1() {
        assertEquals(4, HighGuardArmor.CRYSTALIRON_1.getProtection());
        assertEquals(1, HighGuardArmor.CRYSTALIRON_1.getLayers());
        assertEquals(10, HighGuardArmor.CRYSTALIRON_1.getMinTechLevel());
        assertEquals(HighGuardArmor.ArmorMaterial.CRYSTALIRON, HighGuardArmor.CRYSTALIRON_1.getMaterial());
    }

    @Test
    @DisplayName("Crystaliron x5 should have 20 protection")
    void testCrystaliron5() {
        assertEquals(20, HighGuardArmor.CRYSTALIRON_5.getProtection());
        assertEquals(5, HighGuardArmor.CRYSTALIRON_5.getLayers());
    }

    @Test
    @DisplayName("Crystaliron protection should scale linearly")
    void testCrystalironScaling() {
        assertEquals(4, HighGuardArmor.CRYSTALIRON_1.getProtection());
        assertEquals(8, HighGuardArmor.CRYSTALIRON_2.getProtection());
        assertEquals(12, HighGuardArmor.CRYSTALIRON_3.getProtection());
        assertEquals(16, HighGuardArmor.CRYSTALIRON_4.getProtection());
        assertEquals(20, HighGuardArmor.CRYSTALIRON_5.getProtection());
    }

    // ---- Bonded Superdense tests ----

    @Test
    @DisplayName("Bonded Superdense x1 should have 6 protection")
    void testBondedSuperdense1() {
        assertEquals(6, HighGuardArmor.BONDED_SUPERDENSE_1.getProtection());
        assertEquals(1, HighGuardArmor.BONDED_SUPERDENSE_1.getLayers());
        assertEquals(14, HighGuardArmor.BONDED_SUPERDENSE_1.getMinTechLevel());
        assertEquals(HighGuardArmor.ArmorMaterial.BONDED_SUPERDENSE, HighGuardArmor.BONDED_SUPERDENSE_1.getMaterial());
    }

    @Test
    @DisplayName("Bonded Superdense x3 should have 18 protection")
    void testBondedSuperdense3() {
        assertEquals(18, HighGuardArmor.BONDED_SUPERDENSE_3.getProtection());
        assertEquals(3, HighGuardArmor.BONDED_SUPERDENSE_3.getLayers());
    }

    @Test
    @DisplayName("Bonded Superdense protection should scale linearly")
    void testBondedSuperdenseScaling() {
        assertEquals(6, HighGuardArmor.BONDED_SUPERDENSE_1.getProtection());
        assertEquals(12, HighGuardArmor.BONDED_SUPERDENSE_2.getProtection());
        assertEquals(18, HighGuardArmor.BONDED_SUPERDENSE_3.getProtection());
    }

    // ---- Tons required tests ----

    @Test
    @DisplayName("Tons required should scale with ship tonnage")
    void testTonsRequired() {
        // Titanium Steel x1: costMultiplier = 0.05
        assertEquals(200, HighGuardArmor.TITANIUM_STEEL_1.getTonsRequired(4000));
        assertEquals(1000, HighGuardArmor.TITANIUM_STEEL_1.getTonsRequired(20000));

        // Crystaliron x2: costMultiplier = 0.20
        assertEquals(800, HighGuardArmor.CRYSTALIRON_2.getTonsRequired(4000));
        assertEquals(4000, HighGuardArmor.CRYSTALIRON_2.getTonsRequired(20000));
    }

    @Test
    @DisplayName("None armor should require zero tons")
    void testNoneTonsRequired() {
        assertEquals(0, HighGuardArmor.NONE.getTonsRequired(10000));
    }

    // ---- Static method tests ----

    @Test
    @DisplayName("Max protection for Titanium Steel should cap at 9")
    void testMaxProtectionTitaniumSteel() {
        assertEquals(6, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.TITANIUM_STEEL, 6));
        assertEquals(9, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.TITANIUM_STEEL, 9));
        assertEquals(9, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.TITANIUM_STEEL, 12));
        assertEquals(9, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.TITANIUM_STEEL, 15));
    }

    @Test
    @DisplayName("Max protection for Crystaliron should cap at 13")
    void testMaxProtectionCrystaliron() {
        assertEquals(10, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.CRYSTALIRON, 10));
        assertEquals(13, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.CRYSTALIRON, 13));
        assertEquals(13, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.CRYSTALIRON, 15));
    }

    @Test
    @DisplayName("Max protection for Bonded Superdense should equal TL")
    void testMaxProtectionBondedSuperdense() {
        assertEquals(14, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.BONDED_SUPERDENSE, 14));
        assertEquals(15, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.BONDED_SUPERDENSE, 15));
    }

    @Test
    @DisplayName("Max protection for None material should be 0")
    void testMaxProtectionNone() {
        assertEquals(0, HighGuardArmor.getMaxProtection(HighGuardArmor.ArmorMaterial.NONE, 15));
    }

    @Test
    @DisplayName("Best armor for TL14 should be Bonded Superdense x3")
    void testBestForTechLevel14() {
        assertEquals(HighGuardArmor.BONDED_SUPERDENSE_3, HighGuardArmor.bestForTechLevel(14));
        assertEquals(HighGuardArmor.BONDED_SUPERDENSE_3, HighGuardArmor.bestForTechLevel(15));
    }

    @Test
    @DisplayName("Best armor for TL10 should be Crystaliron x5")
    void testBestForTechLevel10() {
        assertEquals(HighGuardArmor.CRYSTALIRON_5, HighGuardArmor.bestForTechLevel(10));
        assertEquals(HighGuardArmor.CRYSTALIRON_5, HighGuardArmor.bestForTechLevel(13));
    }

    @Test
    @DisplayName("Best armor for TL7 should be Titanium Steel x5")
    void testBestForTechLevel7() {
        assertEquals(HighGuardArmor.TITANIUM_STEEL_5, HighGuardArmor.bestForTechLevel(7));
        assertEquals(HighGuardArmor.TITANIUM_STEEL_5, HighGuardArmor.bestForTechLevel(9));
    }

    @Test
    @DisplayName("Best armor for TL6 should be None")
    void testBestForTechLevel6() {
        assertEquals(HighGuardArmor.NONE, HighGuardArmor.bestForTechLevel(6));
    }

    // ---- Armor tonnage calculation tests ----

    @Test
    @DisplayName("Armor tons for TL7 should use multiplier 4")
    void testArmorTonsTL7() {
        // TL7: multiplier = 4; protection = 2; percent = 4 * (2+1) = 12%
        // 10000 tons * 12% = 1200 tons
        assertEquals(1200, HighGuardArmor.calculateArmorTons(10000, 2, 7));
    }

    @Test
    @DisplayName("Armor tons for TL10 should use multiplier 3")
    void testArmorTonsTL10() {
        // TL10: multiplier = 3; protection = 4; percent = 3 * (4+1) = 15%
        // 10000 tons * 15% = 1500 tons
        assertEquals(1500, HighGuardArmor.calculateArmorTons(10000, 4, 10));
    }

    @Test
    @DisplayName("Armor tons for TL12 should use multiplier 2")
    void testArmorTonsTL12() {
        // TL12: multiplier = 2; protection = 8; percent = 2 * (8+1) = 18%
        // 10000 tons * 18% = 1800 tons
        assertEquals(1800, HighGuardArmor.calculateArmorTons(10000, 8, 12));
    }

    @Test
    @DisplayName("Armor tons for TL14 should use multiplier 1")
    void testArmorTonsTL14() {
        // TL14: multiplier = 1; protection = 6; percent = 1 * (6+1) = 7%
        // 10000 tons * 7% = 700 tons
        assertEquals(700, HighGuardArmor.calculateArmorTons(10000, 6, 14));
    }

    @Test
    @DisplayName("Armor tons should be 0 for zero protection")
    void testArmorTonsZeroProtection() {
        assertEquals(0, HighGuardArmor.calculateArmorTons(10000, 0, 10));
        assertEquals(0, HighGuardArmor.calculateArmorTons(10000, -1, 10));
    }

    // ---- Armor cost per ton tests ----

    @Test
    @DisplayName("Armor cost per ton should be 0 for zero protection")
    void testArmorCostZeroProtection() {
        assertEquals(0.0, HighGuardArmor.calculateArmorCostPerTon(0));
        assertEquals(0.0, HighGuardArmor.calculateArmorCostPerTon(-1));
    }

    @Test
    @DisplayName("Armor cost per ton should scale with protection")
    void testArmorCostScaling() {
        // 0.3 + 0.1 * protection
        assertEquals(0.4, HighGuardArmor.calculateArmorCostPerTon(1));
        assertEquals(0.5, HighGuardArmor.calculateArmorCostPerTon(2));
        assertEquals(1.1, HighGuardArmor.calculateArmorCostPerTon(8));
    }

    // ---- Armor material tests ----

    @Test
    @DisplayName("Armor material properties should be correct")
    void testArmorMaterialProperties() {
        HighGuardArmor.ArmorMaterial ts = HighGuardArmor.ArmorMaterial.TITANIUM_STEEL;
        assertEquals("Titanium Steel", ts.getName());
        assertEquals(7, ts.getMinTechLevel());
        assertEquals(2, ts.getProtectionPerLayer());
        assertEquals(0.05, ts.getCostPerLayer());

        HighGuardArmor.ArmorMaterial ci = HighGuardArmor.ArmorMaterial.CRYSTALIRON;
        assertEquals("Crystaliron", ci.getName());
        assertEquals(10, ci.getMinTechLevel());
        assertEquals(4, ci.getProtectionPerLayer());
        assertEquals(0.20, ci.getCostPerLayer());

        HighGuardArmor.ArmorMaterial bs = HighGuardArmor.ArmorMaterial.BONDED_SUPERDENSE;
        assertEquals("Bonded Superdense", bs.getName());
        assertEquals(14, bs.getMinTechLevel());
        assertEquals(6, bs.getProtectionPerLayer());
        assertEquals(0.50, bs.getCostPerLayer());
    }

    // ---- Name and description tests ----

    @Test
    @DisplayName("All armor presets should have non-empty names")
    void testArmorNames() {
        for (HighGuardArmor a : HighGuardArmor.values()) {
            assertNotNull(a.getName());
            assertFalse(a.getName().isEmpty());
        }
    }

    @Test
    @DisplayName("All armor presets should have non-empty descriptions")
    void testArmorDescriptions() {
        for (HighGuardArmor a : HighGuardArmor.values()) {
            assertNotNull(a.getDescription());
            assertFalse(a.getDescription().isEmpty());
        }
    }

    // ---- Grouping tests ----

    @Test
    @DisplayName("Should group armor by material")
    void testArmorByMaterial() {
        List<HighGuardArmor> armors = Arrays.asList(HighGuardArmor.values());

        long noneCount = armors.stream()
                .filter(a -> a.getMaterial() == HighGuardArmor.ArmorMaterial.NONE).count();
        long tsCount = armors.stream()
                .filter(a -> a.getMaterial() == HighGuardArmor.ArmorMaterial.TITANIUM_STEEL).count();
        long ciCount = armors.stream()
                .filter(a -> a.getMaterial() == HighGuardArmor.ArmorMaterial.CRYSTALIRON).count();
        long bsCount = armors.stream()
                .filter(a -> a.getMaterial() == HighGuardArmor.ArmorMaterial.BONDED_SUPERDENSE).count();

        assertEquals(1, noneCount);
        assertEquals(5, tsCount);
        assertEquals(5, ciCount);
        assertEquals(3, bsCount);

        // Total should be 14
        assertEquals(14, noneCount + tsCount + ciCount + bsCount);
    }

    @Test
    @DisplayName("Higher layer armor should have higher protection")
    void testProtectionMonotonicity() {
        // Titanium Steel
        assertTrue(HighGuardArmor.TITANIUM_STEEL_1.getProtection() < HighGuardArmor.TITANIUM_STEEL_2.getProtection());
        assertTrue(HighGuardArmor.TITANIUM_STEEL_2.getProtection() < HighGuardArmor.TITANIUM_STEEL_3.getProtection());
        assertTrue(HighGuardArmor.TITANIUM_STEEL_3.getProtection() < HighGuardArmor.TITANIUM_STEEL_4.getProtection());
        assertTrue(HighGuardArmor.TITANIUM_STEEL_4.getProtection() < HighGuardArmor.TITANIUM_STEEL_5.getProtection());

        // Crystaliron
        assertTrue(HighGuardArmor.CRYSTALIRON_1.getProtection() < HighGuardArmor.CRYSTALIRON_2.getProtection());
        assertTrue(HighGuardArmor.CRYSTALIRON_2.getProtection() < HighGuardArmor.CRYSTALIRON_3.getProtection());
        assertTrue(HighGuardArmor.CRYSTALIRON_3.getProtection() < HighGuardArmor.CRYSTALIRON_4.getProtection());
        assertTrue(HighGuardArmor.CRYSTALIRON_4.getProtection() < HighGuardArmor.CRYSTALIRON_5.getProtection());

        // Bonded Superdense
        assertTrue(HighGuardArmor.BONDED_SUPERDENSE_1.getProtection() < HighGuardArmor.BONDED_SUPERDENSE_2.getProtection());
        assertTrue(HighGuardArmor.BONDED_SUPERDENSE_2.getProtection() < HighGuardArmor.BONDED_SUPERDENSE_3.getProtection());
    }
}
