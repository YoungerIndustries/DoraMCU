package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GalaxyNameDatabase Tests")
class GalaxyNameDatabaseTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create database with default prefix")
    void testDefaultConstructor() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        assertEquals("NGC", db.getProceduralPrefix());
        assertTrue(db.getNameCount() > 0);
        assertEquals(0, db.getUsedCount());
    }

    @Test
    @DisplayName("Should create database with custom prefix")
    void testCustomPrefixConstructor() {
        GalaxyNameDatabase db = new GalaxyNameDatabase("GAL");
        assertEquals("GAL", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should default to NGC on null prefix")
    void testNullPrefixConstructor() {
        GalaxyNameDatabase db = new GalaxyNameDatabase(null);
        assertEquals("NGC", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should have many named galaxies")
    void testNameCount() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        assertTrue(db.getNameCount() >= 100, "Should have at least 100 names");
    }

    // ---- Sequential name retrieval ----

    @Test
    @DisplayName("Should return sequential names")
    void testSequentialNames() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
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
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String name = db.getNextName();
        assertEquals("Andromeda", name);
    }

    @Test
    @DisplayName("Should fall back to procedural naming when exhausted")
    void testProceduralFallback() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        int count = db.getNameCount();

        // Exhaust all names
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        assertTrue(db.isExhausted());

        // Next should be procedural
        String name = db.getNextName();
        assertTrue(name.startsWith("NGC-"), "Procedural name should start with prefix: " + name);
    }

    @Test
    @DisplayName("Should generate unique procedural names")
    void testProceduralUniqueness() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        int count = db.getNameCount();

        // Exhaust all names
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }

        String proc1 = db.getNextName();
        String proc2 = db.getNextName();
        assertNotEquals(proc1, proc2);
        assertTrue(proc1.startsWith("NGC-"));
        assertTrue(proc2.startsWith("NGC-"));
    }

    // ---- Random name retrieval ----

    @Test
    @DisplayName("Should return random names")
    void testRandomNames() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
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
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        int count = db.getNameCount();

        // Use all names randomly
        for (int i = 0; i < count; i++) {
            db.getRandomName();
        }
        assertTrue(db.isExhausted());

        String name = db.getRandomName();
        assertTrue(name.startsWith("NGC-"));
    }

    // ---- Exhaustion tracking ----

    @Test
    @DisplayName("Should not be exhausted initially")
    void testNotExhaustedInitially() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        assertFalse(db.isExhausted());
    }

    @Test
    @DisplayName("Should be exhausted after using all names")
    void testExhaustedAfterAllUsed() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
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
        GalaxyNameDatabase db = new GalaxyNameDatabase();
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
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String name1 = db.getNextName();
        db.reset();
        String name2 = db.getNextName();
        assertEquals(name1, name2);
    }

    @Test
    @DisplayName("Should reset procedural ID")
    void testResetProceduralId() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        int count = db.getNameCount();

        // Exhaust and get one procedural
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        db.getNextName(); // NGC-00001

        db.reset();
        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        String proc = db.getNextName();
        assertEquals("NGC-00001", proc);
    }

    // ---- Unmodifiable list ----

    @Test
    @DisplayName("Should return unmodifiable list")
    void testUnmodifiableList() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        List<String> names = db.getAllNames();
        assertThrows(UnsupportedOperationException.class, names::clear);
    }

    // ---- Procedural prefix ----

    @Test
    @DisplayName("Should set procedural prefix")
    void testSetProceduralPrefix() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        db.setProceduralPrefix("CUSTOM");
        assertEquals("CUSTOM", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should ignore null prefix")
    void testSetNullPrefix() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        db.setProceduralPrefix(null);
        assertEquals("NGC", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should ignore empty prefix")
    void testSetEmptyPrefix() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        db.setProceduralPrefix("");
        assertEquals("NGC", db.getProceduralPrefix());
    }

    @Test
    @DisplayName("Should use custom prefix for procedural names")
    void testCustomPrefixProcedural() {
        GalaxyNameDatabase db = new GalaxyNameDatabase("CUSTOM");
        int count = db.getNameCount();

        for (int i = 0; i < count; i++) {
            db.getNextName();
        }
        String proc = db.getNextName();
        assertTrue(proc.startsWith("CUSTOM-"));
    }

    // ---- Procedural name format ----

    @Test
    @DisplayName("Should format procedural names correctly")
    void testProceduralNameFormat() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String name = db.getProceduralName();
        assertEquals("NGC-00001", name);

        name = db.getProceduralName();
        assertEquals("NGC-00002", name);
    }

    // ---- Galaxy integration ----

    @Test
    @DisplayName("Should handle null galaxy in assignName")
    void testAssignNameNull() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        assertDoesNotThrow(() -> db.assignName(null));
    }

    @Test
    @DisplayName("Should handle null galaxy in assignRandomName")
    void testAssignRandomNameNull() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        assertDoesNotThrow(() -> db.assignRandomName(null));
    }

    // ---- Multiple databases ----

    @Test
    @DisplayName("Should have independent state")
    void testIndependentDatabases() {
        GalaxyNameDatabase db1 = new GalaxyNameDatabase();
        GalaxyNameDatabase db2 = new GalaxyNameDatabase();

        String name1 = db1.getNextName();
        String name2 = db2.getNextName();

        assertEquals(name1, name2);
        assertEquals(1, db1.getUsedCount());
        assertEquals(1, db2.getUsedCount());
    }

    // ---- toString ----

    @Test
    @DisplayName("Should format toString correctly")
    void testToString() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String str = db.toString();
        assertTrue(str.contains("GalaxyNameDatabase{"));
        assertTrue(str.contains("total="));
        assertTrue(str.contains("used="));
        assertTrue(str.contains("prefix='NGC'"));
    }

    @Test
    @DisplayName("Should update toString after usage")
    void testToStringAfterUsage() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        db.getNextName();
        db.getNextName();
        String str = db.toString();
        assertTrue(str.contains("used=2"));
    }

    // ---- Integration scenarios ----

    @Test
    @DisplayName("Should name multiple galaxies sequentially")
    void testMultipleGalaxiesSequential() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String name1 = db.getNextName();
        String name2 = db.getNextName();
        String name3 = db.getNextName();

        assertNotEquals(name1, name2);
        assertNotEquals(name2, name3);
        assertNotEquals(name1, name3);
        assertEquals(3, db.getUsedCount());
    }

    @Test
    @DisplayName("Should name multiple galaxies randomly")
    void testMultipleGalaxiesRandom() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String name1 = db.getRandomName();
        String name2 = db.getRandomName();
        String name3 = db.getRandomName();

        assertNotEquals(name1, name2);
        assertNotEquals(name2, name3);
        assertNotEquals(name1, name3);
    }

    @Test
    @DisplayName("Should mix sequential and random naming")
    void testMixedNaming() {
        GalaxyNameDatabase db = new GalaxyNameDatabase();
        String name1 = db.getNextName();
        String name2 = db.getRandomName();

        assertNotEquals(name1, name2);
        assertEquals(2, db.getUsedCount());
    }
}
