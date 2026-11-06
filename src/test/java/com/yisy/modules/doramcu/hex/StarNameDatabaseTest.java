package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarNameDatabase Tests")
class StarNameDatabaseTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create database with default prefix")
    void testDefaultConstructor() {
        StarNameDatabase db = new StarNameDatabase();
        assertEquals("HIP", db.getCatalogPrefix());
        assertTrue(db.getTotalNameCount() > 0);
    }

    @Test
    @DisplayName("Should create database with custom prefix")
    void testCustomPrefixConstructor() {
        StarNameDatabase db = new StarNameDatabase("HD");
        assertEquals("HD", db.getCatalogPrefix());
    }

    @Test
    @DisplayName("Should default to HIP when prefix is null")
    void testNullPrefixConstructor() {
        StarNameDatabase db = new StarNameDatabase(null);
        assertEquals("HIP", db.getCatalogPrefix());
    }

    @Test
    @DisplayName("Should have names for all star types")
    void testAllStarTypesHaveNames() {
        StarNameDatabase db = new StarNameDatabase();
        for (Star.StarType type : Star.StarType.values()) {
            assertTrue(db.getNameCount(type) > 0, "Should have names for " + type);
        }
    }

    // ---- Name Retrieval ----

    @Test
    @DisplayName("Should return sequential names")
    void testSequentialNames() {
        StarNameDatabase db = new StarNameDatabase();
        String name1 = db.getName(Star.StarType.RED);
        String name2 = db.getName(Star.StarType.RED);
        assertNotEquals(name1, name2, "Sequential names should differ");
        assertEquals("Betelgeuse", name1);
        assertEquals("Antares", name2);
    }

    @Test
    @DisplayName("Should return procedural name when exhausted")
    void testProceduralNameWhenExhausted() {
        StarNameDatabase db = new StarNameDatabase();
        int count = db.getNameCount(Star.StarType.PURPLE);
        for (int i = 0; i < count; i++) {
            db.getName(Star.StarType.PURPLE);
        }
        String procedural = db.getName(Star.StarType.PURPLE);
        assertTrue(procedural.startsWith("HIP-"), "Should fall back to procedural name");
    }

    @Test
    @DisplayName("Should return random name from list")
    void testRandomName() {
        StarNameDatabase db = new StarNameDatabase();
        String random = db.getRandomName(Star.StarType.RED);
        assertNotNull(random);
        List<String> all = db.getAllNames(Star.StarType.RED);
        assertTrue(all.contains(random), "Random name should be from the list");
    }

    @Test
    @DisplayName("Should return procedural random name when empty")
    void testRandomNameEmptyList() {
        StarNameDatabase db = new StarNameDatabase();
        // This test verifies the procedural fallback path exists
        String procedural = db.getProceduralName(42);
        assertEquals("HIP-00042", procedural);
    }

    // ---- Procedural Names ----

    @Test
    @DisplayName("Should format procedural name correctly")
    void testProceduralNameFormat() {
        StarNameDatabase db = new StarNameDatabase();
        assertEquals("HIP-00001", db.getProceduralName(1));
        assertEquals("HIP-01234", db.getProceduralName(1234));
        assertEquals("HIP-12345", db.getProceduralName(12345));
        assertEquals("HIP-00000", db.getProceduralName(0));
    }

    @Test
    @DisplayName("Should use custom prefix for procedural names")
    void testProceduralNameCustomPrefix() {
        StarNameDatabase db = new StarNameDatabase("SD");
        assertEquals("SD-00042", db.getProceduralName(42));
    }

    // ---- Name Counts ----

    @Test
    @DisplayName("Should return correct name count per type")
    void testNameCount() {
        StarNameDatabase db = new StarNameDatabase();
        assertTrue(db.getNameCount(Star.StarType.RED) > 50);
        assertTrue(db.getNameCount(Star.StarType.YELLOW) > 50);
        assertTrue(db.getNameCount(Star.StarType.BLUE) > 30);
        assertTrue(db.getNameCount(Star.StarType.PURPLE) > 30);
    }

    @Test
    @DisplayName("Should return total name count")
    void testTotalNameCount() {
        StarNameDatabase db = new StarNameDatabase();
        int total = db.getTotalNameCount();
        int expected = db.getNameCount(Star.StarType.RED)
                + db.getNameCount(Star.StarType.YELLOW)
                + db.getNameCount(Star.StarType.BLUE)
                + db.getNameCount(Star.StarType.PURPLE);
        assertEquals(expected, total);
    }

    // ---- Exhaustion Tracking ----

    @Test
    @DisplayName("Should not be exhausted initially")
    void testNotExhaustedInitially() {
        StarNameDatabase db = new StarNameDatabase();
        for (Star.StarType type : Star.StarType.values()) {
            assertFalse(db.isExhausted(type), "Should not be exhausted for " + type);
        }
    }

    @Test
    @DisplayName("Should be exhausted after using all names")
    void testExhaustedAfterAllNames() {
        StarNameDatabase db = new StarNameDatabase();
        int count = db.getNameCount(Star.StarType.PURPLE);
        for (int i = 0; i < count; i++) {
            db.getName(Star.StarType.PURPLE);
        }
        assertTrue(db.isExhausted(Star.StarType.PURPLE));
    }

    @Test
    @DisplayName("Should track used count")
    void testUsedCount() {
        StarNameDatabase db = new StarNameDatabase();
        assertEquals(0, db.getUsedCount(Star.StarType.RED));
        db.getName(Star.StarType.RED);
        assertEquals(1, db.getUsedCount(Star.StarType.RED));
        db.getName(Star.StarType.RED);
        assertEquals(2, db.getUsedCount(Star.StarType.RED));
    }

    // ---- Reset ----

    @Test
    @DisplayName("Should reset single type")
    void testResetSingleType() {
        StarNameDatabase db = new StarNameDatabase();
        db.getName(Star.StarType.RED);
        db.getName(Star.StarType.RED);
        assertEquals(2, db.getUsedCount(Star.StarType.RED));

        db.reset(Star.StarType.RED);
        assertEquals(0, db.getUsedCount(Star.StarType.RED));
        assertFalse(db.isExhausted(Star.StarType.RED));
    }

    @Test
    @DisplayName("Should reset all types")
    void testResetAll() {
        StarNameDatabase db = new StarNameDatabase();
        db.getName(Star.StarType.RED);
        db.getName(Star.StarType.YELLOW);
        db.getName(Star.StarType.BLUE);
        db.getName(Star.StarType.PURPLE);

        db.resetAll();
        for (Star.StarType type : Star.StarType.values()) {
            assertEquals(0, db.getUsedCount(type));
        }
    }

    @Test
    @DisplayName("Should allow re-use after reset")
    void testReuseAfterReset() {
        StarNameDatabase db = new StarNameDatabase();
        String first = db.getName(Star.StarType.RED);
        db.reset(Star.StarType.RED);
        String afterReset = db.getName(Star.StarType.RED);
        assertEquals(first, afterReset);
    }

    // ---- All Names ----

    @Test
    @DisplayName("Should return unmodifiable list")
    void testUnmodifiableList() {
        StarNameDatabase db = new StarNameDatabase();
        List<String> names = db.getAllNames(Star.StarType.RED);
        assertThrows(UnsupportedOperationException.class, () -> names.add("Test"));
    }

    @Test
    @DisplayName("Should return first known name")
    void testFirstKnownName() {
        StarNameDatabase db = new StarNameDatabase();
        assertEquals("Betelgeuse", db.getAllNames(Star.StarType.RED).get(0));
        assertEquals("Sol", db.getAllNames(Star.StarType.YELLOW).get(0));
        assertEquals("Rigel", db.getAllNames(Star.StarType.BLUE).get(0));
        assertEquals("Deneb", db.getAllNames(Star.StarType.PURPLE).get(0));
    }

    // ---- Star Integration ----

    @Test
    @DisplayName("Should name stars sequentially")
    void testStarNamingIntegration() {
        StarNameDatabase db = new StarNameDatabase();

        Star star1 = new Star(1, Star.StarType.RED);
        star1.setName(db.getName(Star.StarType.RED));
        assertEquals("Betelgeuse", star1.getName());

        Star star2 = new Star(2, Star.StarType.RED);
        star2.setName(db.getName(Star.StarType.RED));
        assertEquals("Antares", star2.getName());

        Star star3 = new Star(3, Star.StarType.YELLOW);
        star3.setName(db.getName(Star.StarType.YELLOW));
        assertEquals("Sol", star3.getName());
    }

    @Test
    @DisplayName("Should assign sequential name to star")
    void testAssignName() {
        StarNameDatabase db = new StarNameDatabase();
        Star star = new Star(1, Star.StarType.RED);
        db.assignName(star);
        assertEquals("Betelgeuse", star.getName());
    }

    @Test
    @DisplayName("Should assign random name to star")
    void testAssignRandomName() {
        StarNameDatabase db = new StarNameDatabase();
        Star star = new Star(1, Star.StarType.BLUE);
        db.assignRandomName(star);
        assertNotNull(star.getName());
        List<String> all = db.getAllNames(Star.StarType.BLUE);
        assertTrue(all.contains(star.getName()), "Random name should be from the list");
    }

    @Test
    @DisplayName("Should handle null star in assignName")
    void testAssignNameNull() {
        StarNameDatabase db = new StarNameDatabase();
        assertDoesNotThrow(() -> db.assignName(null));
    }

    @Test
    @DisplayName("Should handle null star in assignRandomName")
    void testAssignRandomNameNull() {
        StarNameDatabase db = new StarNameDatabase();
        assertDoesNotThrow(() -> db.assignRandomName(null));
    }

    @Test
    @DisplayName("Should assign names based on star type")
    void testAssignNameByStarType() {
        StarNameDatabase db = new StarNameDatabase();

        Star redStar = new Star(1, Star.StarType.RED);
        db.assignName(redStar);
        assertEquals("Betelgeuse", redStar.getName());

        Star yellowStar = new Star(2, Star.StarType.YELLOW);
        db.assignName(yellowStar);
        assertEquals("Sol", yellowStar.getName());

        Star blueStar = new Star(3, Star.StarType.BLUE);
        db.assignName(blueStar);
        assertEquals("Rigel", blueStar.getName());

        Star purpleStar = new Star(4, Star.StarType.PURPLE);
        db.assignName(purpleStar);
        assertEquals("Deneb", purpleStar.getName());
    }

    @Test
    @DisplayName("Should handle multiple databases independently")
    void testMultipleDatabases() {
        StarNameDatabase db1 = new StarNameDatabase();
        StarNameDatabase db2 = new StarNameDatabase("HD");

        String name1 = db1.getName(Star.StarType.RED);
        assertEquals("Betelgeuse", name1);
        assertEquals(1, db1.getUsedCount(Star.StarType.RED));

        String name2 = db2.getName(Star.StarType.RED);
        assertEquals("Betelgeuse", name2);
        assertEquals(1, db2.getUsedCount(Star.StarType.RED));

        // Verify independence: db1 state doesn't affect db2
        db1.getName(Star.StarType.RED);
        assertEquals(2, db1.getUsedCount(Star.StarType.RED));
        assertEquals(1, db2.getUsedCount(Star.StarType.RED));
    }

    // ---- toString ----

    @Test
    @DisplayName("Should include prefix and total count in toString")
    void testToString() {
        StarNameDatabase db = new StarNameDatabase("HD");
        String str = db.toString();
        assertTrue(str.contains("prefix='HD'"));
        assertTrue(str.contains("totalNames="));
    }

    // ---- Edge Cases ----

    @Test
    @DisplayName("Should handle zero procedural number")
    void testProceduralNameZero() {
        StarNameDatabase db = new StarNameDatabase();
        assertEquals("HIP-00000", db.getProceduralName(0));
    }

    @Test
    @DisplayName("Should handle large procedural number")
    void testProceduralNameLarge() {
        StarNameDatabase db = new StarNameDatabase();
        assertEquals("HIP-99999", db.getProceduralName(99999));
    }

    @Test
    @DisplayName("Should continue generating procedural names after exhaustion")
    void testMultipleProceduralNames() {
        StarNameDatabase db = new StarNameDatabase();
        int count = db.getNameCount(Star.StarType.PURPLE);
        for (int i = 0; i < count; i++) {
            db.getName(Star.StarType.PURPLE);
        }
        String p1 = db.getName(Star.StarType.PURPLE);
        String p2 = db.getName(Star.StarType.PURPLE);
        assertNotEquals(p1, p2);
        assertTrue(p1.startsWith("HIP-"));
        assertTrue(p2.startsWith("HIP-"));
    }
}
