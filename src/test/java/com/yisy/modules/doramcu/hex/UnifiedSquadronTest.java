package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnifiedSquadron Tests")
class UnifiedSquadronTest {

    @Test
    @DisplayName("Should create squadron")
    void testCreateSquadron() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        assertEquals(0, squadron.getId());
        assertEquals("Alpha", squadron.getName());
        assertEquals(FleetFormation.WEDGE, squadron.getFormation());
    }

    @Test
    @DisplayName("Should add ships up to 4")
    void testAddShips() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        assertTrue(squadron.addShip(UnifiedShipFactory.createFrigate(1)));
        assertTrue(squadron.addShip(UnifiedShipFactory.createFrigate(2)));
        assertTrue(squadron.addShip(UnifiedShipFactory.createFrigate(3)));
        assertTrue(squadron.addShip(UnifiedShipFactory.createFrigate(4)));
        assertEquals(4, squadron.getShipCount());
        assertFalse(squadron.addShip(UnifiedShipFactory.createFrigate(5)));
    }

    @Test
    @DisplayName("Should remove ship")
    void testRemoveShip() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        UnifiedShip ship = UnifiedShipFactory.createFrigate(1);
        squadron.addShip(ship);
        assertTrue(squadron.removeShip(ship));
        assertEquals(0, squadron.getShipCount());
    }

    @Test
    @DisplayName("Should set commander")
    void testSetCommander() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        NavalOfficer officer = new NavalOfficer(0, "Captain", NavalRank.CAPTAIN);
        squadron.setCommander(officer);
        assertEquals(officer, squadron.getCommander());
    }

    @Test
    @DisplayName("Should change formation")
    void testChangeFormation() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        squadron.setFormation(FleetFormation.WEDGE);
        assertEquals(FleetFormation.WEDGE, squadron.getFormation());
    }

    @Test
    @DisplayName("Should be combat-ready")
    void testCombatReady() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        squadron.addShip(UnifiedShipFactory.createFrigate(1));
        assertFalse(squadron.isCombatReady());
        NavalOfficer officer = new NavalOfficer(0, "Captain", NavalRank.CAPTAIN);
        squadron.setCommander(officer);
        assertTrue(squadron.isCombatReady());
    }

    @Test
    @DisplayName("Should get active ships")
    void testActiveShips() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        squadron.addShip(UnifiedShipFactory.createFrigate(1));
        assertEquals(1, squadron.getActiveShips().size());
    }

    @Test
    @DisplayName("Should get battle log")
    void testBattleLog() {
        UnifiedSquadron squadron = new UnifiedSquadron(0, "Alpha");
        squadron.addShip(UnifiedShipFactory.createFrigate(1));
        assertTrue(squadron.getBattleLog().size() >= 1);
    }
}
