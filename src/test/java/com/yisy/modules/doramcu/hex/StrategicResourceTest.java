package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrategicResource Enum Tests")
class StrategicResourceTest {

    @Test
    @DisplayName("values - should have 5 strategic resources")
    void values_shouldHaveFiveResources() {
        StrategicResource[] resources = StrategicResource.values();

        assertEquals(5, resources.length);
    }

    @Test
    @DisplayName("ELERIUM - should exist")
    void elerium_shouldExist() {
        assertSame(StrategicResource.ELERIUM, StrategicResource.valueOf("ELERIUM"));
    }

    @Test
    @DisplayName("ANTI_MATTER - should exist")
    void antiMatter_shouldExist() {
        assertSame(StrategicResource.ANTI_MATTER, StrategicResource.valueOf("ANTI_MATTER"));
    }

    @Test
    @DisplayName("THULIUM - should exist")
    void thulium_shouldExist() {
        assertSame(StrategicResource.THULIUM, StrategicResource.valueOf("THULIUM"));
    }

    @Test
    @DisplayName("DURANTIUM - should exist")
    void durantium_shouldExist() {
        assertSame(StrategicResource.DURANTIUM, StrategicResource.valueOf("DURANTIUM"));
    }

    @Test
    @DisplayName("PROMETHION - should exist")
    void promethion_shouldExist() {
        assertSame(StrategicResource.PROMETHION, StrategicResource.valueOf("PROMETHION"));
    }

    @Test
    @DisplayName("All values should be unique")
    void allValues_shouldBeUnique() {
        StrategicResource[] resources = StrategicResource.values();

        for (int i = 0; i < resources.length; i++) {
            for (int j = i + 1; j < resources.length; j++) {
                assertNotSame(resources[i], resources[j]);
            }
        }
    }

    @Test
    @DisplayName("valueOf - should throw on invalid name")
    void valueOf_shouldThrowOnInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> StrategicResource.valueOf("INVALID"));
    }
}
