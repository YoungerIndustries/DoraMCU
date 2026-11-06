package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlanetNameDatabase Tests")
class PlanetNameDatabaseTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Should create database with default prefix")
    void testDefaultConstructor() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertEquals("PDX", db.getCatalogPrefix());
        assertTrue(db.getTotalNameCount() > 0);
    }

    @Test
    @DisplayName("Should create database with custom prefix")
    void testCustomPrefixConstructor() {
        PlanetNameDatabase db = new PlanetNameDatabase("EXO");
        assertEquals("EXO", db.getCatalogPrefix());
    }

    @Test
    @DisplayName("Should default to PDX when prefix is null")
    void testNullPrefixConstructor() {
        PlanetNameDatabase db = new PlanetNameDatabase(null);
        assertEquals("PDX", db.getCatalogPrefix());
    }

    @Test
    @DisplayName("Should have 6 categories")
    void testCategoryCount() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertEquals(6, db.getCategories().size());
    }

    @Test
    @DisplayName("Should have expected categories")
    void testExpectedCategories() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        List<String> categories = db.getCategories();
        assertTrue(categories.contains("Roman Mythology"));
        assertTrue(categories.contains("Greek Mythology"));
        assertTrue(categories.contains("Norse Mythology"));
        assertTrue(categories.contains("Historical Figures"));
        assertTrue(categories.contains("Celestial Bodies"));
        assertTrue(categories.contains("Cultural Names"));
    }

    @Test
    @DisplayName("Should have names for all categories")
    void testAllCategoriesHaveNames() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        for (String category : db.getCategories()) {
            assertTrue(db.getNameCount(category) > 0, "Should have names for " + category);
        }
    }

    // ---- Name Retrieval ----

    @Test
    @DisplayName("Should return sequential names")
    void testSequentialNames() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name1 = db.getName();
        String name2 = db.getName();
        assertNotNull(name1);
        assertNotNull(name2);
        assertNotEquals(name1, name2);
    }

    @Test
    @DisplayName("Should return first name from Roman Mythology")
    void testFirstRomanName() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getName();
        assertEquals("Jupiter", name);
    }

    @Test
    @DisplayName("Should return random name")
    void testRandomName() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getRandomName();
        assertNotNull(name);
        assertTrue(db.getAllNames().contains(name));
    }

    @Test
    @DisplayName("Should return name from specific category")
    void testNameFromCategory() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getName("Greek Mythology");
        assertEquals("Zeus", name);
    }

    @Test
    @DisplayName("Should return random name from specific category")
    void testRandomNameFromCategory() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getRandomName("Norse Mythology");
        List<String> norseNames = db.getAllNames("Norse Mythology");
        assertTrue(norseNames.contains(name));
    }

    @Test
    @DisplayName("Should handle unknown category")
    void testUnknownCategory() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getName("Unknown");
        assertTrue(name.startsWith("PDX-"));
    }

    // ---- Procedural Names ----

    @Test
    @DisplayName("Should generate procedural name with correct format")
    void testProceduralNameFormat() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getProceduralName(123);
        assertEquals("PDX-00123", name);
    }

    @Test
    @DisplayName("Should use custom prefix for procedural names")
    void testCustomPrefixProcedural() {
        PlanetNameDatabase db = new PlanetNameDatabase("EXO");
        String name = db.getProceduralName(456);
        assertEquals("EXO-00456", name);
    }

    @Test
    @DisplayName("Should fall back to procedural names when exhausted")
    void testProceduralFallback() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        int count = db.getTotalNameCount();
        for (int i = 0; i < count; i++) {
            db.getName();
        }
        String name = db.getName();
        assertTrue(name.startsWith("PDX-"));
    }

    // ---- Name Counts ----

    @Test
    @DisplayName("Should return correct name count per category")
    void testNameCountPerCategory() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertTrue(db.getNameCount("Roman Mythology") > 0);
        assertTrue(db.getNameCount("Greek Mythology") > 0);
        assertTrue(db.getNameCount("Norse Mythology") > 0);
        assertTrue(db.getNameCount("Historical Figures") > 0);
        assertTrue(db.getNameCount("Celestial Bodies") > 0);
        assertTrue(db.getNameCount("Cultural Names") > 0);
    }

    @Test
    @DisplayName("Should return total name count")
    void testTotalNameCount() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        int total = db.getTotalNameCount();
        int sum = 0;
        for (String category : db.getCategories()) {
            sum += db.getNameCount(category);
        }
        assertEquals(sum, total);
    }

    @Test
    @DisplayName("Should return 0 for unknown category")
    void testUnknownCategoryCount() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertEquals(0, db.getNameCount("Unknown"));
    }

    // ---- Exhaustion Tracking ----

    @Test
    @DisplayName("Should not be exhausted initially")
    void testNotExhaustedInitially() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertFalse(db.isExhausted());
        for (String category : db.getCategories()) {
            assertFalse(db.isExhausted(category));
        }
    }

    @Test
    @DisplayName("Should track used count")
    void testUsedCount() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertEquals(0, db.getTotalUsedCount());
        db.getName();
        assertEquals(1, db.getTotalUsedCount());
    }

    @Test
    @DisplayName("Should detect category exhaustion")
    void testCategoryExhaustion() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String category = "Roman Mythology";
        int count = db.getNameCount(category);
        for (int i = 0; i < count; i++) {
            db.getName(category);
        }
        assertTrue(db.isExhausted(category));
    }

    @Test
    @DisplayName("Should detect overall exhaustion")
    void testOverallExhaustion() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        int total = db.getTotalNameCount();
        for (int i = 0; i < total; i++) {
            db.getName();
        }
        assertTrue(db.isExhausted());
    }

    // ---- Reset Operations ----

    @Test
    @DisplayName("Should reset specific category")
    void testResetCategory() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        db.getName("Greek Mythology");
        assertEquals(1, db.getUsedCount("Greek Mythology"));
        db.reset("Greek Mythology");
        assertEquals(0, db.getUsedCount("Greek Mythology"));
    }

    @Test
    @DisplayName("Should reset all categories")
    void testResetAll() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        db.getName();
        db.getName();
        db.resetAll();
        assertEquals(0, db.getTotalUsedCount());
        assertFalse(db.isExhausted());
    }

    // ---- Unmodifiable Lists ----

    @Test
    @DisplayName("Should return unmodifiable categories list")
    void testUnmodifiableCategories() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertThrows(UnsupportedOperationException.class, () -> db.getCategories().add("Fake"));
    }

    @Test
    @DisplayName("Should return unmodifiable names list")
    void testUnmodifiableNames() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertThrows(UnsupportedOperationException.class, () -> db.getAllNames().add("Fake"));
    }

    // ---- Planet Integration ----

    @Test
    @DisplayName("Should assign name to planet")
    void testAssignName() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        Planet planet = new Planet(1);
        db.assignName(planet);
        assertEquals("Jupiter", planet.getName());
    }

    @Test
    @DisplayName("Should assign random name to planet")
    void testAssignRandomName() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        Planet planet = new Planet(2);
        db.assignRandomName(planet);
        assertNotNull(planet.getName());
        assertTrue(db.getAllNames().contains(planet.getName()));
    }

    @Test
    @DisplayName("Should handle null planet")
    void testNullPlanet() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        assertDoesNotThrow(() -> db.assignName(null));
        assertDoesNotThrow(() -> db.assignRandomName(null));
    }

    // ---- Multiple Databases ----

    @Test
    @DisplayName("Should have independent state")
    void testMultipleDatabases() {
        PlanetNameDatabase db1 = new PlanetNameDatabase();
        PlanetNameDatabase db2 = new PlanetNameDatabase("EXO");

        db1.getName();
        assertEquals(0, db2.getTotalUsedCount());

        assertEquals("PDX", db1.getCatalogPrefix());
        assertEquals("EXO", db2.getCatalogPrefix());
    }

    // ---- Edge Cases ----

    @Test
    @DisplayName("Should handle empty procedural number")
    void testProceduralZero() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getProceduralName(0);
        assertEquals("PDX-00000", name);
    }

    @Test
    @DisplayName("Should handle large procedural number")
    void testProceduralLargeNumber() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String name = db.getProceduralName(99999);
        assertEquals("PDX-99999", name);
    }

    // ---- ToString ----

    @Test
    @DisplayName("Should have meaningful toString")
    void testToString() {
        PlanetNameDatabase db = new PlanetNameDatabase();
        String str = db.toString();
        assertTrue(str.contains("PlanetNameDatabase"));
        assertTrue(str.contains("prefix='PDX'"));
        assertTrue(str.contains("totalNames="));
        assertTrue(str.contains("categories=6"));
    }
}
