package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipNameDatabase Tests")
class ShipNameDatabaseTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create database with default prefix")
    void testDefaultConstructor() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("DS", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should create database with custom prefix")
    void testCustomPrefixConstructor() {
        ShipNameDatabase db = new ShipNameDatabase("SHIP");
        assertEquals("SHIP", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should default to DS when prefix is null")
    void testNullPrefixConstructor() {
        ShipNameDatabase db = new ShipNameDatabase(null);
        assertEquals("DS", db.getProceduralPrefix());
    }

    // ---- Sequential name retrieval ----

    @Test
    @DisplayName("Should return sequential names for Frigate")
    void testSequentialFrigateNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("Swift", db.getNextName(ShipType.FRIGATE));
        assertEquals("Arrow", db.getNextName(ShipType.FRIGATE));
        assertEquals("Dart", db.getNextName(ShipType.FRIGATE));
    }

    @Test
    @DisplayName("Should return sequential names for Destroyer")
    void testSequentialDestroyerNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("Avenger", db.getNextName(ShipType.DESTROYER));
        assertEquals("Bulwark", db.getNextName(ShipType.DESTROYER));
    }

    @Test
    @DisplayName("Should return sequential names for Cruiser")
    void testSequentialCruiserNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("Ambition", db.getNextName(ShipType.CRUISER));
        assertEquals("Bastion", db.getNextName(ShipType.CRUISER));
    }

    @Test
    @DisplayName("Should return sequential names for Battleship")
    void testSequentialBattleshipNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("Annihilator", db.getNextName(ShipType.BATTLESHIP));
        assertEquals("Behemoth", db.getNextName(ShipType.BATTLESHIP));
    }

    @Test
    @DisplayName("Should return sequential names for Carrier")
    void testSequentialCarrierNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("Airborne", db.getNextName(ShipType.CARRIER));
        assertEquals("Barracuda", db.getNextName(ShipType.CARRIER));
    }

    @Test
    @DisplayName("Should return sequential names for Mothership")
    void testSequentialMothershipNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals("Almighty", db.getNextName(ShipType.MOTHERSHIP));
        assertEquals("Celestial", db.getNextName(ShipType.MOTHERSHIP));
    }

    @Test
    @DisplayName("Should throw on null ship type for getNextName")
    void testGetNextNameNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.getNextName(null));
    }

    // ---- Random name retrieval ----

    @Test
    @DisplayName("Should return random names for Frigate")
    void testRandomFrigateNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        String name1 = db.getRandomName(ShipType.FRIGATE);
        String name2 = db.getRandomName(ShipType.FRIGATE);
        assertNotNull(name1);
        assertNotNull(name2);
        // Names should be from the frigate list
        List<String> allNames = db.getAllNames(ShipType.FRIGATE);
        assertTrue(allNames.contains(name1));
        // Second name should not be the same as the first (no duplicates)
        assertNotEquals(name1, name2);
    }

    @Test
    @DisplayName("Should throw on null ship type for getRandomName")
    void testGetRandomNameNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.getRandomName(null));
    }

    // ---- Procedural name fallback ----

    @Test
    @DisplayName("Should generate procedural names when exhausted")
    void testProceduralFallback() {
        ShipNameDatabase db = new ShipNameDatabase();
        // Exhaust all frigate names
        for (int i = 0; i < db.getNameCount(ShipType.FRIGATE); i++) {
            db.getNextName(ShipType.FRIGATE);
        }
        // Next name should be procedural
        String procedural = db.getNextName(ShipType.FRIGATE);
        assertEquals("DS-00001", procedural);
        String nextProcedural = db.getNextName(ShipType.FRIGATE);
        assertEquals("DS-00002", nextProcedural);
    }

    @Test
    @DisplayName("Should use custom prefix for procedural names")
    void testCustomProceduralPrefix() {
        ShipNameDatabase db = new ShipNameDatabase("SHIP");
        // Exhaust all frigate names
        for (int i = 0; i < db.getNameCount(ShipType.FRIGATE); i++) {
            db.getNextName(ShipType.FRIGATE);
        }
        String procedural = db.getNextName(ShipType.FRIGATE);
        assertEquals("SHIP-00001", procedural);
    }

    // ---- Name counts ----

    @Test
    @DisplayName("Should have correct name counts for all types")
    void testNameCounts() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals(46, db.getNameCount(ShipType.FRIGATE));
        assertEquals(49, db.getNameCount(ShipType.DESTROYER));
        assertEquals(46, db.getNameCount(ShipType.CRUISER));
        assertEquals(45, db.getNameCount(ShipType.BATTLESHIP));
        assertEquals(46, db.getNameCount(ShipType.CARRIER));
        assertEquals(43, db.getNameCount(ShipType.MOTHERSHIP));
    }

    @Test
    @DisplayName("Should throw on null ship type for getNameCount")
    void testGetNameCountNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.getNameCount(null));
    }

    // ---- Used count and exhaustion ----

    @Test
    @DisplayName("Should track used names")
    void testUsedCount() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertEquals(0, db.getUsedCount(ShipType.FRIGATE));

        db.getNextName(ShipType.FRIGATE);
        assertEquals(1, db.getUsedCount(ShipType.FRIGATE));

        db.getNextName(ShipType.FRIGATE);
        assertEquals(2, db.getUsedCount(ShipType.FRIGATE));
    }

    @Test
    @DisplayName("Should throw on null ship type for getUsedCount")
    void testGetUsedCountNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.getUsedCount(null));
    }

    @Test
    @DisplayName("Should detect exhaustion")
    void testExhaustion() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertFalse(db.isExhausted(ShipType.FRIGATE));

        // Use all names
        for (int i = 0; i < db.getNameCount(ShipType.FRIGATE); i++) {
            db.getNextName(ShipType.FRIGATE);
        }
        assertTrue(db.isExhausted(ShipType.FRIGATE));
    }

    @Test
    @DisplayName("Should throw on null ship type for isExhausted")
    void testIsExhaustedNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.isExhausted(null));
    }

    // ---- Reset ----

    @Test
    @DisplayName("Should reset all names")
    void testReset() {
        ShipNameDatabase db = new ShipNameDatabase();
        db.getNextName(ShipType.FRIGATE);
        db.getNextName(ShipType.DESTROYER);
        assertEquals(1, db.getUsedCount(ShipType.FRIGATE));
        assertEquals(1, db.getUsedCount(ShipType.DESTROYER));

        db.reset();
        assertEquals(0, db.getUsedCount(ShipType.FRIGATE));
        assertEquals(0, db.getUsedCount(ShipType.DESTROYER));
        assertEquals("Swift", db.getNextName(ShipType.FRIGATE));
        assertEquals("Avenger", db.getNextName(ShipType.DESTROYER));
    }

    @Test
    @DisplayName("Should reset specific ship type")
    void testResetSpecificType() {
        ShipNameDatabase db = new ShipNameDatabase();
        db.getNextName(ShipType.FRIGATE);
        db.getNextName(ShipType.DESTROYER);
        assertEquals(1, db.getUsedCount(ShipType.FRIGATE));
        assertEquals(1, db.getUsedCount(ShipType.DESTROYER));

        db.reset(ShipType.FRIGATE);
        assertEquals(0, db.getUsedCount(ShipType.FRIGATE));
        assertEquals(1, db.getUsedCount(ShipType.DESTROYER));
        assertEquals("Swift", db.getNextName(ShipType.FRIGATE));
    }

    @Test
    @DisplayName("Should throw on null ship type for reset")
    void testResetNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.reset(null));
    }

    // ---- Unmodifiable lists ----

    @Test
    @DisplayName("Should return unmodifiable name list")
    void testUnmodifiableNames() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(UnsupportedOperationException.class, () -> db.getAllNames(ShipType.FRIGATE).add("Test"));
    }

    @Test
    @DisplayName("Should throw on null ship type for getAllNames")
    void testGetAllNamesNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertThrows(IllegalArgumentException.class, () -> db.getAllNames(null));
    }

    // ---- Ship integration ----

    @Test
    @DisplayName("Should assign sequential name to ship")
    void testAssignName() {
        ShipNameDatabase db = new ShipNameDatabase();
        Ship ship = new Ship(1, ShipType.FRIGATE);
        db.assignName(ship);
        assertEquals("Swift", ship.getName());
    }

    @Test
    @DisplayName("Should assign random name to ship")
    void testAssignRandomName() {
        ShipNameDatabase db = new ShipNameDatabase();
        Ship ship = new Ship(1, ShipType.FRIGATE);
        db.assignRandomName(ship);
        assertNotNull(ship.getName());
        List<String> allNames = db.getAllNames(ShipType.FRIGATE);
        assertTrue(allNames.contains(ship.getName()));
    }

    @Test
    @DisplayName("Should ignore null ship for assignName")
    void testAssignNameNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertDoesNotThrow(() -> db.assignName(null));
    }

    @Test
    @DisplayName("Should ignore null ship for assignRandomName")
    void testAssignRandomNameNull() {
        ShipNameDatabase db = new ShipNameDatabase();
        assertDoesNotThrow(() -> db.assignRandomName(null));
    }

    @Test
    @DisplayName("Should assign type-appropriate names")
    void testTypeAppropriateNames() {
        ShipNameDatabase db = new ShipNameDatabase();

        Ship frigate = new Ship(1, ShipType.FRIGATE);
        db.assignName(frigate);
        assertEquals("Swift", frigate.getName());

        Ship destroyer = new Ship(2, ShipType.DESTROYER);
        db.assignName(destroyer);
        assertEquals("Avenger", destroyer.getName());

        Ship cruiser = new Ship(3, ShipType.CRUISER);
        db.assignName(cruiser);
        assertEquals("Ambition", cruiser.getName());

        Ship battleship = new Ship(4, ShipType.BATTLESHIP);
        db.assignName(battleship);
        assertEquals("Annihilator", battleship.getName());

        Ship carrier = new Ship(5, ShipType.CARRIER);
        db.assignName(carrier);
        assertEquals("Airborne", carrier.getName());

        Ship mothership = new Ship(6, ShipType.MOTHERSHIP);
        db.assignName(mothership);
        assertEquals("Almighty", mothership.getName());
    }

    // ---- Multiple database independence ----

    @Test
    @DisplayName("Should have independent state across databases")
    void testIndependentDatabases() {
        ShipNameDatabase db1 = new ShipNameDatabase();
        ShipNameDatabase db2 = new ShipNameDatabase();

        db1.getNextName(ShipType.FRIGATE);
        assertEquals(1, db1.getUsedCount(ShipType.FRIGATE));
        assertEquals(0, db2.getUsedCount(ShipType.FRIGATE));

        assertEquals("Arrow", db1.getNextName(ShipType.FRIGATE));
        assertEquals("Swift", db2.getNextName(ShipType.FRIGATE));
    }

    // ---- Edge cases ----

    @Test
    @DisplayName("Should handle random names after exhaustion")
    void testRandomAfterExhaustion() {
        ShipNameDatabase db = new ShipNameDatabase();
        // Exhaust all frigate names using random
        for (int i = 0; i < db.getNameCount(ShipType.FRIGATE); i++) {
            db.getRandomName(ShipType.FRIGATE);
        }
        assertTrue(db.isExhausted(ShipType.FRIGATE));
        // Next random should be procedural
        String procedural = db.getRandomName(ShipType.FRIGATE);
        assertNotNull(procedural);
        assertTrue(procedural.startsWith("DS-"));
    }

    // ---- toString ----

    @Test
    @DisplayName("Should include name counts in toString")
    void testToString() {
        ShipNameDatabase db = new ShipNameDatabase();
        String str = db.toString();
        assertTrue(str.contains("ShipNameDatabase{"));
        assertTrue(str.contains("FRIGATE=46"));
        assertTrue(str.contains("DESTROYER=49"));
        assertTrue(str.contains("CRUISER=46"));
        assertTrue(str.contains("BATTLESHIP=45"));
        assertTrue(str.contains("CARRIER=46"));
        assertTrue(str.contains("MOTHERSHIP=43"));
    }
}
