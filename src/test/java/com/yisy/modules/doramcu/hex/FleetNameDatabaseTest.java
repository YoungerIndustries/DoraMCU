package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FleetNameDatabase Tests")
class FleetNameDatabaseTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create database with default prefix")
    void testDefaultConstructor() {
        FleetNameDatabase db = new FleetNameDatabase();
        assertEquals("FLT", db.getProceduralPrefix());
        assertTrue(db.getNameCount() > 0);
        assertEquals(0, db.getUsedCount());
    }

    @Test
    @DisplayName("Should create database with custom prefix")
    void testCustomPrefixConstructor() {
        FleetNameDatabase db = new FleetNameDatabase("FLT-CUSTOM");
        assertEquals("FLT-CUSTOM", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should default to FLT on null prefix")
    void testNullPrefixConstructor() {
        FleetNameDatabase db = new FleetNameDatabase(null);
        assertEquals("FLT", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should have many named fleets")
    void testNameCount() {
        FleetNameDatabase db = new FleetNameDatabase();
        assertTrue(db.getNameCount() >= 500, "Should have at least 500 names");
    }

    // ---- Sequential name retrieval ----

    @Test
    @DisplayName("Should return sequential names")
    void testSequentialNames() {
        FleetNameDatabase db = new FleetNameDatabase();
        String name1 = db.getNextName();
        String name2 = db.getNextName();
        assertNotNull(name1);
        assertNotNull(name2);
        assertNotEquals(name1, name2);
        assertEquals(2, db.getUsedCount());
    }

    @Test
    @DisplayName("Should return first name sequentially")
    void testFirstSequentialName() {
        FleetNameDatabase db = new FleetNameDatabase();
        String name = db.getNextName();
        assertEquals("Alpha Strike", name);
    }

    @Test
    @DisplayName("Should fall back to procedural naming when exhausted")
    void testProceduralFallback() {
        FleetNameDatabase db = new FleetNameDatabase();
        int count = db.getNameCount();

        // Exhaust all names
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        assertTrue(db.isExhausted());

        // Next should be procedural
        String name = db.getNextName();
        assertTrue(name.startsWith("FLT-"), "Procedural name should start with prefix: " + name);
    }

    @Test
    @DisplayName("Should generate unique procedural names")
    void testProceduralUniqueness() {
        FleetNameDatabase db = new FleetNameDatabase();
        int count = db.getNameCount();

        // Exhaust all names
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }

        String proc1 = db.getNextName();
        String proc2 = db.getNextName();
        assertNotEquals(proc1, proc2);
        assertTrue(proc1.startsWith("FLT-"));
        assertTrue(proc2.startsWith("FLT-"));
    }

    // ---- Random name retrieval ----

    @Test
    @DisplayName("Should return random names")
    void testRandomNames() {
        FleetNameDatabase db = new FleetNameDatabase();
        String name1 = db.getRandomName();
        String name2 = db.getRandomName();
        assertNotNull(name1);
        assertNotNull(name2);
        assertNotEquals(name1, name2);
        assertEquals(2, db.getUsedCount());
    }

    @Test
    @DisplayName("Should fall back to procedural when all names used randomly")
    void testRandomProceduralFallback() {
        FleetNameDatabase db = new FleetNameDatabase();
        int count = db.getNameCount();

        // Use all names randomly
        for (int i = 0; i < count; i++) {
            db.getRandomName();
        }
        assertTrue(db.isExhausted());

        String name = db.getRandomName();
        assertTrue(name.startsWith("FLT-"));
    }

    // ---- Fleet integration ----

    @Test
    @DisplayName("Should assign sequential name to fleet")
    void testAssignName() {
        FleetNameDatabase db = new FleetNameDatabase();
        Fleet fleet = new Fleet(1, "Temporary");
        db.assignName(fleet);
        assertEquals("Alpha Strike", fleet.getName());
    }

    @Test
    @DisplayName("Should assign random name to fleet")
    void testAssignRandomName() {
        FleetNameDatabase db = new FleetNameDatabase();
        Fleet fleet = new Fleet(1, "Temporary");
        db.assignRandomName(fleet);
        assertNotEquals("Temporary", fleet.getName());
        assertNotNull(fleet.getName());
    }

    @Test
    @DisplayName("Should handle null fleet in assignName")
    void testAssignNameNull() {
        FleetNameDatabase db = new FleetNameDatabase();
        assertDoesNotThrow(() -> db.assignName(null));
    }

    @Test
    @DisplayName("Should handle null fleet in assignRandomName")
    void testAssignRandomNameNull() {
        FleetNameDatabase db = new FleetNameDatabase();
        assertDoesNotThrow(() -> db.assignRandomName(null));
    }

    // ---- Exhaustion tracking ----

    @Test
    @DisplayName("Should not be exhausted initially")
    void testNotExhaustedInitially() {
        FleetNameDatabase db = new FleetNameDatabase();
        assertFalse(db.isExhausted());
    }

    @Test
    @DisplayName("Should be exhausted after using all names")
    void testExhaustedAfterAllUsed() {
        FleetNameDatabase db = new FleetNameDatabase();
        int count = db.getNameCount();
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        assertTrue(db.isExhausted());
    }

    // ---- Reset ----

    @Test
    @DisplayName("Should reset used names")
    void testReset() {
        FleetNameDatabase db = new FleetNameDatabase();
        db.getNextName();
        db.getNextName();
        assertEquals(2, db.getUsedCount());

        db.reset();
        assertEquals(0, db.getUsedCount());
        assertFalse(db.isExhausted());
    }

    @Test
    @DisplayName("Should allow name reuse after reset")
    void testReuseAfterReset() {
        FleetNameDatabase db = new FleetNameDatabase();
        String name1 = db.getNextName();
        db.reset();
        String name2 = db.getNextName();
        assertEquals(name1, name2);
    }

    @Test
    @DisplayName("Should reset procedural ID")
    void testResetProceduralId() {
        FleetNameDatabase db = new FleetNameDatabase();
        int count = db.getNameCount();

        // Exhaust and get one procedural
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        db.getNextName(); // FLT-00001

        db.reset();
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        String proc = db.getNextName();
        assertEquals("FLT-00001", proc);
    }

    // ---- Unmodifiable list ----

    @Test
    @DisplayName("Should return unmodifiable list")
    void testUnmodifiableList() {
        FleetNameDatabase db = new FleetNameDatabase();
        List<String> names = db.getAllNames();
        assertThrows(UnsupportedOperationException.class, names::clear);
    }

    // ---- Multiple databases ----

    @Test
    @DisplayName("Should have independent state")
    void testIndependentDatabases() {
        FleetNameDatabase db1 = new FleetNameDatabase();
        FleetNameDatabase db2 = new FleetNameDatabase();

        String name1 = db1.getNextName();
        String name2 = db2.getNextName();

        assertEquals(name1, name2);
        assertEquals(1, db1.getUsedCount());
        assertEquals(1, db2.getUsedCount());
    }

    // ---- Custom prefix procedural names ----

    @Test
    @DisplayName("Should use custom prefix for procedural names")
    void testCustomPrefixProcedural() {
        FleetNameDatabase db = new FleetNameDatabase("CUSTOM");
        int count = db.getNameCount();

        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        String proc = db.getNextName();
        assertTrue(proc.startsWith("CUSTOM-"));
    }

    // ---- toString ----

    @Test
    @DisplayName("Should format toString correctly")
    void testToString() {
        FleetNameDatabase db = new FleetNameDatabase();
        String str = db.toString();
        assertTrue(str.contains("FleetNameDatabase{"));
        assertTrue(str.contains("total="));
        assertTrue(str.contains("used="));
        assertTrue(str.contains("prefix='FLT'"));
    }

    @Test
    @DisplayName("Should update toString after usage")
    void testToStringAfterUsage() {
        FleetNameDatabase db = new FleetNameDatabase();
        db.getNextName();
        db.getNextName();
        String str = db.toString();
        assertTrue(str.contains("used=2"));
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should name multiple fleets sequentially")
    void testMultipleFleetsSequential() {
        FleetNameDatabase db = new FleetNameDatabase();
        Fleet fleet1 = new Fleet(1, "F1");
        Fleet fleet2 = new Fleet(2, "F2");
        Fleet fleet3 = new Fleet(3, "F3");

        db.assignName(fleet1);
        db.assignName(fleet2);
        db.assignName(fleet3);

        assertNotEquals(fleet1.getName(), fleet2.getName());
        assertNotEquals(fleet2.getName(), fleet3.getName());
        assertNotEquals(fleet1.getName(), fleet3.getName());
        assertEquals(3, db.getUsedCount());
    }

    @Test
    @DisplayName("Should name multiple fleets randomly")
    void testMultipleFleetsRandom() {
        FleetNameDatabase db = new FleetNameDatabase();
        Fleet fleet1 = new Fleet(1, "F1");
        Fleet fleet2 = new Fleet(2, "F2");
        Fleet fleet3 = new Fleet(3, "F3");

        db.assignRandomName(fleet1);
        db.assignRandomName(fleet2);
        db.assignRandomName(fleet3);

        assertNotEquals(fleet1.getName(), fleet2.getName());
        assertNotEquals(fleet2.getName(), fleet3.getName());
        assertNotEquals(fleet1.getName(), fleet3.getName());
    }

    @Test
    @DisplayName("Should mix sequential and random naming")
    void testMixedNaming() {
        FleetNameDatabase db = new FleetNameDatabase();
        Fleet fleet1 = new Fleet(1, "F1");
        Fleet fleet2 = new Fleet(2, "F2");

        db.assignName(fleet1);
        db.assignRandomName(fleet2);

        assertNotEquals(fleet1.getName(), fleet2.getName());
        assertEquals(2, db.getUsedCount());
    }
}
