package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EmpireTest {

    private Empire empire;

    @BeforeEach
    void setUp() {
        empire = new Empire(1, "Test Empire", 1);
    }

    @Test
    void constructor_setsFields() {
        assertEquals(1, empire.getId());
        assertEquals("Test Empire", empire.getName());
        assertEquals(1, empire.getRulerPlayerId());
        assertEquals(0, empire.getPlanetCount());
    }

    @Test
    void constructor_nullName_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Empire(1, null, 1));
    }

    @Test
    void constructor_blankName_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Empire(1, "  ", 1));
    }

    @Test
    void claimPlanet_addsPlanet() {
        assertTrue(empire.claimPlanet(100));
        assertEquals(1, empire.getPlanetCount());
        assertTrue(empire.ownsPlanet(100));
    }

    @Test
    void claimPlanet_duplicate_returnsFalse() {
        empire.claimPlanet(100);
        assertFalse(empire.claimPlanet(100));
        assertEquals(1, empire.getPlanetCount());
    }

    @Test
    void releasePlanet_removesPlanet() {
        empire.claimPlanet(100);
        assertTrue(empire.releasePlanet(100));
        assertEquals(0, empire.getPlanetCount());
        assertFalse(empire.ownsPlanet(100));
    }

    @Test
    void releasePlanet_notOwned_returnsFalse() {
        assertFalse(empire.releasePlanet(999));
    }

    @Test
    void ownsPlanet_checksCorrectly() {
        empire.claimPlanet(100);
        assertTrue(empire.ownsPlanet(100));
        assertFalse(empire.ownsPlanet(200));
    }

    @Test
    void getOwnedPlanetIds_returnsUnmodifiableSet() {
        empire.claimPlanet(100);
        assertThrows(UnsupportedOperationException.class, () ->
            empire.getOwnedPlanetIds().add(200));
    }

    @Test
    void setPlanetIndustrialLevel_setsLevel() {
        empire.claimPlanet(100);
        empire.setPlanetIndustrialLevel(100, 3);
        assertEquals(3, empire.getPlanetIndustrialLevel(100));
    }

    @Test
    void setPlanetIndustrialLevel_notOwned_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            empire.setPlanetIndustrialLevel(999, 3));
    }

    @Test
    void setPlanetIndustrialLevel_negative_throws() {
        empire.claimPlanet(100);
        assertThrows(IllegalArgumentException.class, () ->
            empire.setPlanetIndustrialLevel(100, -1));
    }

    @Test
    void setPlanetIndustrialLevel_zero_removes() {
        empire.claimPlanet(100);
        empire.setPlanetIndustrialLevel(100, 3);
        empire.setPlanetIndustrialLevel(100, 0);
        assertEquals(0, empire.getPlanetIndustrialLevel(100));
        assertEquals(0, empire.getIndustrialPlanetCount());
    }

    @Test
    void getPlanetIndustrialLevel_defaultZero() {
        empire.claimPlanet(100);
        assertEquals(0, empire.getPlanetIndustrialLevel(100));
    }

    @Test
    void getTotalIndustrialCapacity_sumsLevels() {
        empire.claimPlanet(100);
        empire.claimPlanet(200);
        empire.claimPlanet(300);
        empire.setPlanetIndustrialLevel(100, 2);
        empire.setPlanetIndustrialLevel(200, 3);
        assertEquals(5, empire.getTotalIndustrialCapacity());
    }

    @Test
    void getTotalIndustrialCapacity_empty() {
        assertEquals(0, empire.getTotalIndustrialCapacity());
    }

    @Test
    void getIndustrialPlanetCount_countsCorrectly() {
        empire.claimPlanet(100);
        empire.claimPlanet(200);
        empire.setPlanetIndustrialLevel(100, 1);
        assertEquals(1, empire.getIndustrialPlanetCount());
    }

    @Test
    void getPlanetIndustrialLevels_returnsUnmodifiableMap() {
        empire.claimPlanet(100);
        empire.setPlanetIndustrialLevel(100, 2);
        assertThrows(UnsupportedOperationException.class, () ->
            empire.getPlanetIndustrialLevels().put(200, 3));
    }

    @Test
    void assignPlanetFacilities_withShipyard() {
        Planet planet = new Planet(100);
        Random random = new Random(42); // fixed seed for reproducibility
        empire.assignPlanetFacilities(planet, random, 1.0, 0.0, 1, 1);
        assertTrue(planet.hasStartingFacility(StartingFacility.SHIPYARD));
        assertTrue(empire.ownsPlanet(100));
    }

    @Test
    void assignPlanetFacilities_withIndustrial() {
        Planet planet = new Planet(100);
        Random random = new Random(42);
        empire.assignPlanetFacilities(planet, random, 0.0, 1.0, 2, 2);
        assertTrue(planet.hasStartingFacility(StartingFacility.MINING_FACILITY));
        assertEquals(2, empire.getPlanetIndustrialLevel(100));
    }

    @Test
    void assignPlanetFacilities_both() {
        Planet planet = new Planet(100);
        Random random = new Random(42);
        empire.assignPlanetFacilities(planet, random, 1.0, 1.0, 1, 3);
        assertTrue(planet.hasStartingFacility(StartingFacility.SHIPYARD));
        assertTrue(planet.hasStartingFacility(StartingFacility.MINING_FACILITY));
        assertTrue(empire.getPlanetIndustrialLevel(100) >= 1);
        assertTrue(empire.getPlanetIndustrialLevel(100) <= 3);
    }

    @Test
    void assignPlanetFacilities_neither() {
        Planet planet = new Planet(100);
        Random random = new Random(42);
        empire.assignPlanetFacilities(planet, random, 0.0, 0.0, 1, 1);
        assertTrue(empire.ownsPlanet(100));
        assertFalse(planet.hasStartingFacility(StartingFacility.SHIPYARD));
        assertFalse(planet.hasStartingFacility(StartingFacility.MINING_FACILITY));
        assertEquals(0, empire.getPlanetIndustrialLevel(100));
    }

    @Test
    void assignPlanetFacilities_alreadyOwned_skips() {
        Planet planet = new Planet(100);
        Random random = new Random(42);
        empire.assignPlanetFacilities(planet, random, 1.0, 1.0, 1, 1);
        // Second call should skip since already owned
        empire.assignPlanetFacilities(planet, random, 1.0, 1.0, 1, 1);
        assertEquals(1, empire.getPlanetCount());
    }

    @Test
    void releasePlanet_clearsIndustrialLevel() {
        empire.claimPlanet(100);
        empire.setPlanetIndustrialLevel(100, 3);
        empire.releasePlanet(100);
        assertEquals(0, empire.getPlanetIndustrialLevel(100));
    }

    @Test
    void toString_containsFields() {
        empire.claimPlanet(100);
        empire.setPlanetIndustrialLevel(100, 2);
        String str = empire.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("Test Empire"));
        assertTrue(str.contains("ruler=1"));
        assertTrue(str.contains("planets=1"));
        assertTrue(str.contains("industrialCapacity=2"));
    }

    @Test
    void constants_haveExpectedValues() {
        assertEquals(0.4, Empire.DEFAULT_SHIPYARD_CHANCE);
        assertEquals(0.5, Empire.DEFAULT_INDUSTRIAL_CHANCE);
        assertEquals(1, Empire.DEFAULT_MIN_INDUSTRIAL_LEVEL);
        assertEquals(3, Empire.DEFAULT_MAX_INDUSTRIAL_LEVEL);
    }

    // ---- Capital planet tests ----

    @Test
    void getCapitalPlanetId_defaultNull() {
        assertNull(empire.getCapitalPlanetId());
    }

    @Test
    void hasCapital_defaultFalse() {
        assertFalse(empire.hasCapital());
    }

    @Test
    void setCapitalPlanet_setsCapital() {
        empire.claimPlanet(100);
        empire.setCapitalPlanet(100);
        assertEquals(100, empire.getCapitalPlanetId());
        assertTrue(empire.hasCapital());
    }

    @Test
    void setCapitalPlanet_notOwned_throws() {
        assertThrows(IllegalArgumentException.class, () -> empire.setCapitalPlanet(999));
    }

    @Test
    void setCapitalPlanet_changesCapital() {
        empire.claimPlanet(100);
        empire.claimPlanet(200);
        empire.setCapitalPlanet(100);
        assertEquals(100, empire.getCapitalPlanetId());
        empire.setCapitalPlanet(200);
        assertEquals(200, empire.getCapitalPlanetId());
    }

    @Test
    void hasCapital_afterSet() {
        empire.claimPlanet(100);
        assertFalse(empire.hasCapital());
        empire.setCapitalPlanet(100);
        assertTrue(empire.hasCapital());
    }
}
