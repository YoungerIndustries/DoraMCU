package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PopulationAttribute Enum Tests")
class PopulationAttributeTest {

    @Test
    @DisplayName("values - should have 4 attributes")
    void values_shouldHaveFourAttributes() {
        PopulationAttribute[] attrs = PopulationAttribute.values();

        assertEquals(4, attrs.length);
    }

    @Test
    @DisplayName("SOCIAL - should exist")
    void social_shouldExist() {
        assertSame(PopulationAttribute.SOCIAL, PopulationAttribute.valueOf("SOCIAL"));
    }

    @Test
    @DisplayName("INTELLIGENCE - should exist")
    void intelligence_shouldExist() {
        assertSame(PopulationAttribute.INTELLIGENCE, PopulationAttribute.valueOf("INTELLIGENCE"));
    }

    @Test
    @DisplayName("DILIGENCE - should exist")
    void diligence_shouldExist() {
        assertSame(PopulationAttribute.DILIGENCE, PopulationAttribute.valueOf("DILIGENCE"));
    }

    @Test
    @DisplayName("RESOLVE - should exist")
    void resolve_shouldExist() {
        assertSame(PopulationAttribute.RESOLVE, PopulationAttribute.valueOf("RESOLVE"));
    }

    @Test
    @DisplayName("All values should be unique")
    void allValues_shouldBeUnique() {
        PopulationAttribute[] attrs = PopulationAttribute.values();

        for (int i = 0; i < attrs.length; i++) {
            for (int j = i + 1; j < attrs.length; j++) {
                assertNotSame(attrs[i], attrs[j]);
            }
        }
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> PopulationAttribute.valueOf("INVALID"));
    }
}
