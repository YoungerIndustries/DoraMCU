package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Material enum.
 */
class MaterialTest {

    // ---- Enum value tests ----

    @Test
    @DisplayName("Should have correct value for Empty")
    void testEmptyValue() {
        assertEquals(0, Material.Empty.getValue());
    }

    @Test
    @DisplayName("Should have correct value for Aluminum")
    void testAluminumValue() {
        assertEquals(1, Material.Aluminum.getValue());
    }

    @Test
    @DisplayName("Should have correct value for Steel")
    void testSteelValue() {
        assertEquals(2, Material.Steel.getValue());
    }

    @Test
    @DisplayName("Should have correct value for Titanium")
    void testTitaniumValue() {
        assertEquals(3, Material.Titanium.getValue());
    }

    @Test
    @DisplayName("Should have correct value for Composite")
    void testCompositeValue() {
        assertEquals(4, Material.Composite.getValue());
    }

    @Test
    @DisplayName("Should have correct value for Explosive")
    void testExplosiveValue() {
        assertEquals(5, Material.Explosive.getValue());
    }

    // ---- fromValue tests ----

    @Test
    @DisplayName("Should return Empty for value 0")
    void testFromValueZero() {
        assertEquals(Material.Empty, Material.fromValue(0));
    }

    @Test
    @DisplayName("Should return Aluminum for value 1")
    void testFromValueOne() {
        assertEquals(Material.Aluminum, Material.fromValue(1));
    }

    @Test
    @DisplayName("Should return Steel for value 2")
    void testFromValueTwo() {
        assertEquals(Material.Steel, Material.fromValue(2));
    }

    @Test
    @DisplayName("Should return Titanium for value 3")
    void testFromValueThree() {
        assertEquals(Material.Titanium, Material.fromValue(3));
    }

    @Test
    @DisplayName("Should return Composite for value 4")
    void testFromValueFour() {
        assertEquals(Material.Composite, Material.fromValue(4));
    }

    @Test
    @DisplayName("Should return Explosive for value 5")
    void testFromValueFive() {
        assertEquals(Material.Explosive, Material.fromValue(5));
    }

    @Test
    @DisplayName("Should throw for negative value")
    void testFromValueNegative() {
        assertThrows(IllegalArgumentException.class, () -> Material.fromValue(-1));
    }

    @Test
    @DisplayName("Should throw for value out of range")
    void testFromValueOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> Material.fromValue(6));
    }

    @Test
    @DisplayName("Should throw for large value")
    void testFromValueLarge() {
        assertThrows(IllegalArgumentException.class, () -> Material.fromValue(100));
    }

    // ---- Round-trip tests ----

    @Test
    @DisplayName("Should round-trip from enum to value and back")
    void testRoundTrip() {
        for (Material material : Material.values()) {
            assertEquals(material, Material.fromValue(material.getValue()));
        }
    }

    // ---- Enum size test ----

    @Test
    @DisplayName("Should have 6 material types")
    void testEnumSize() {
        assertEquals(6, Material.values().length);
    }

    // ---- Value uniqueness test ----

    @Test
    @DisplayName("Should have unique values for all materials")
    void testUniqueValues() {
        Material[] values = Material.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getValue(), values[j].getValue(),
                        "Materials " + values[i] + " and " + values[j] + " have the same value");
            }
        }
    }
}
