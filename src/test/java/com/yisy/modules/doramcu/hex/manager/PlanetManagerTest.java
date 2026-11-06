package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link PlanetManager}.
 */
class PlanetManagerTest {

    private GameContext context;
    private PlanetManager planetManager;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(GameContext.class);
        when(context.getRandom()).thenReturn(new Random(42));
        planetManager = new PlanetManager(context);
    }

    // ---- addPlanet Tests ----

    @Test
    @DisplayName("Should add planet and assign to owner")
    void testAddPlanet() {
        Planet planet = new Planet(1);
        planet.setName("Terra");
        Player owner = new Player(1, "Test Empire");

        Map<Integer, Planet> planets = new HashMap<>();
        when(context.getPlanets()).thenReturn(planets);

        Planet added = planetManager.addPlanet(planet, owner);

        assertEquals(planet, added);
        assertTrue(planets.containsKey(1));
        assertTrue(owner.getOwnedPlanetIds().contains(1));
    }

    @Test
    @DisplayName("Should throw for null planet")
    void testAddPlanetNull() {
        Player owner = new Player(1, "Test Empire");
        assertThrows(IllegalArgumentException.class, () -> planetManager.addPlanet(null, owner));
    }

    @Test
    @DisplayName("Should throw for null owner")
    void testAddPlanetNullOwner() {
        Planet planet = new Planet(1);
        assertThrows(IllegalArgumentException.class, () -> planetManager.addPlanet(planet, null));
    }

    // ---- createPlanet Tests ----

    @Test
    @DisplayName("Should create planet with specified properties")
    void testCreatePlanet() {
        Player owner = new Player(1, "Test Empire");

        when(context.getNextPlanetIdAndIncrement()).thenReturn(5);
        Map<Integer, Planet> planets = new HashMap<>();
        when(context.getPlanets()).thenReturn(planets);

        Planet planet = planetManager.createPlanet("Kepler", PlanetSize.LARGE,
                MineralQuality.RICH, owner);

        assertNotNull(planet);
        assertEquals(5, planet.getId());
        assertEquals("Kepler", planet.getName());
        assertEquals(PlanetSize.LARGE, planet.getPlanetSize());
        assertEquals(MineralQuality.RICH, planet.getMineralQuality());
    }

    // ---- transferPlanet Tests ----

    @Test
    @DisplayName("Should transfer planet ownership")
    void testTransferPlanet() {
        Planet planet = new Planet(1);
        Player oldOwner = new Player(1, "Old Empire");
        Player newOwner = new Player(2, "New Empire");

        oldOwner.addPlanet(1);
        Map<Integer, Planet> planets = new HashMap<>();
        planets.put(1, planet);
        when(context.getPlanet(1)).thenReturn(planet);
        when(context.getPlayers()).thenReturn(Arrays.asList(oldOwner, newOwner));

        boolean transferred = planetManager.transferPlanet(1, newOwner);

        assertTrue(transferred);
        assertFalse(oldOwner.getOwnedPlanetIds().contains(1));
        assertTrue(newOwner.getOwnedPlanetIds().contains(1));
    }

    @Test
    @DisplayName("Should return false for non-existent planet")
    void testTransferPlanetNotFound() {
        Player newOwner = new Player(2, "New Empire");
        when(context.getPlanet(999)).thenReturn(null);

        assertFalse(planetManager.transferPlanet(999, newOwner));
    }

    // ---- setupHomeSystem Tests ----

    @Test
    @DisplayName("Should setup home system with default parameters")
    void testSetupHomeSystem() {
        Player player = new Player(1, "Test Empire");

        when(context.getNextPlanetIdAndIncrement()).thenReturn(1);
        Map<Integer, Planet> planets = new HashMap<>();
        when(context.getPlanets()).thenReturn(planets);

        Planet home = planetManager.setupHomeSystem(player);

        assertNotNull(home);
        assertEquals(1, home.getId());
        assertEquals(PlanetSize.MEDIUM, home.getPlanetSize());
        assertEquals(MineralQuality.NORMAL, home.getMineralQuality());
        assertTrue(player.getOwnedPlanetIds().contains(1));
    }

    @Test
    @DisplayName("Should setup home system with custom parameters")
    void testSetupHomeSystemCustom() {
        Player player = new Player(1, "Test Empire");

        when(context.getNextPlanetIdAndIncrement()).thenReturn(1);
        Map<Integer, Planet> planets = new HashMap<>();
        when(context.getPlanets()).thenReturn(planets);

        Planet home = planetManager.setupHomeSystem(player, PlanetSize.LARGE,
                MineralQuality.RICH, StartingFacility.COLONY_DOME);

        assertNotNull(home);
        assertEquals(PlanetSize.LARGE, home.getPlanetSize());
        assertEquals(MineralQuality.RICH, home.getMineralQuality());
    }
}
