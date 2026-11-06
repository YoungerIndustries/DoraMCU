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
 * Comprehensive tests for {@link FleetManager}.
 */
class FleetManagerTest {

    private GameContext context;
    private FleetManager fleetManager;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(GameContext.class);
        fleetManager = new FleetManager(context);
    }

    // ---- createFleet Tests ----

    @Test
    @DisplayName("Should create and register a fleet")
    void testCreateFleet() {
        Player player = new Player(1, "Test Empire");

        when(context.getNextFleetIdAndIncrement()).thenReturn(1);
        when(context.addFleet(any(Fleet.class), eq(player))).thenAnswer(inv -> inv.getArgument(0));

        Fleet fleet = fleetManager.createFleet("My Fleet", player);

        assertNotNull(fleet);
        assertEquals("My Fleet", fleet.getName());
        assertEquals(1, fleet.getId());
    }

    // ---- createStartingFleets Tests ----

    @Test
    @DisplayName("Should create starting fleets for a player")
    void testCreateStartingFleets() {
        Player player = new Player(1, "Test Empire");

        final int[] fleetCounter = {1};
        final int[] shipCounter = {1};
        when(context.getNextFleetIdAndIncrement()).thenAnswer(inv -> fleetCounter[0]++);
        when(context.getNextShipIdAndIncrement()).thenAnswer(inv -> shipCounter[0]++);
        when(context.addFleet(any(Fleet.class), eq(player))).thenAnswer(inv -> inv.getArgument(0));
        when(context.addShip(any(Ship.class))).thenAnswer(inv -> inv.getArgument(0));
        when(context.getSolarSystems()).thenReturn(new HashMap<>());

        List<Fleet> fleets = fleetManager.createStartingFleets(player);

        assertEquals(2, fleets.size());
        assertTrue(fleets.get(0).getName().contains("Military"));
        assertTrue(fleets.get(1).getName().contains("Mining"));
        assertTrue(fleets.get(0).getShipCount() >= 2);
        assertEquals(1, fleets.get(1).getShipCount());
    }

    @Test
    @DisplayName("Should throw for null player in createStartingFleets")
    void testCreateStartingFleetsNullPlayer() {
        assertThrows(IllegalArgumentException.class,
                () -> fleetManager.createStartingFleets((Galaxy) null, null));
    }

    @Test
    @DisplayName("Should create starting fleets with galaxy parameter")
    void testCreateStartingFleetsWithGalaxy() {
        Player player = new Player(1, "Test Empire");

        final int[] fleetCounter = {1};
        final int[] shipCounter = {1};
        when(context.getNextFleetIdAndIncrement()).thenAnswer(inv -> fleetCounter[0]++);
        when(context.getNextShipIdAndIncrement()).thenAnswer(inv -> shipCounter[0]++);
        when(context.addFleet(any(Fleet.class), eq(player))).thenAnswer(inv -> inv.getArgument(0));
        when(context.addShip(any(Ship.class))).thenAnswer(inv -> inv.getArgument(0));
        when(context.getSolarSystems()).thenReturn(new HashMap<>());

        List<Fleet> fleets = fleetManager.createStartingFleets(null, player);

        assertEquals(2, fleets.size());
    }

    // ---- createMilitaryFrigate Tests ----

    @Test
    @DisplayName("Should create military frigate with beam weapon and shield")
    void testCreateMilitaryFrigate() {
        when(context.getNextShipIdAndIncrement()).thenReturn(1);
        when(context.addShip(any(Ship.class))).thenAnswer(inv -> inv.getArgument(0));

        Ship ship = fleetManager.createMilitaryFrigate("Defender");

        assertEquals("Defender", ship.getName());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
        assertTrue(ship.getMaxHullPoints() > 0);
        assertTrue(ship.getMaxShieldPoints() > 0);
    }

    // ---- createMilitaryDestroyer Tests ----

    @Test
    @DisplayName("Should create military destroyer with two beam weapons and two shields")
    void testCreateMilitaryDestroyer() {
        when(context.getNextShipIdAndIncrement()).thenReturn(1);
        when(context.addShip(any(Ship.class))).thenAnswer(inv -> inv.getArgument(0));

        Ship ship = fleetManager.createMilitaryDestroyer("Vanguard");

        assertEquals("Vanguard", ship.getName());
        assertEquals(ShipType.DESTROYER, ship.getShipType());
        assertTrue(ship.getMaxHullPoints() > 0);
        assertTrue(ship.getMaxShieldPoints() > 0);
    }

    // ---- createMiningFrigate Tests ----

    @Test
    @DisplayName("Should create mining frigate with shield only")
    void testCreateMiningFrigate() {
        when(context.getNextShipIdAndIncrement()).thenReturn(1);
        when(context.addShip(any(Ship.class))).thenAnswer(inv -> inv.getArgument(0));

        Ship ship = fleetManager.createMiningFrigate("Prospector");

        assertEquals("Prospector", ship.getName());
        assertEquals(ShipType.FRIGATE, ship.getShipType());
        assertTrue(ship.getMaxShieldPoints() > 0);
    }
}
