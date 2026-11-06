package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Player Model Tests")
class PlayerModelTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Default constructor - should create empty player")
    void defaultConstructor_shouldCreateEmptyPlayer() {
        Player player = new Player();

        assertNull(player.getId());
        assertNull(player.getEmail());
        assertNull(player.getLoginPin());
        assertNull(player.getEmpireName());
        assertNull(player.getGovernmentType());
        assertNull(player.getEthos());
    }

    @Test
    @DisplayName("Parameterized constructor - should set all fields")
    void parameterizedConstructor_shouldSetAllFields() {
        Player player = new Player(1L, "player@example.com", "123456", "Terran Republic",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);

        assertEquals(1L, player.getId());
        assertEquals("player@example.com", player.getEmail());
        assertEquals("123456", player.getLoginPin());
        assertEquals("Terran Republic", player.getEmpireName());
        assertEquals(Player.GovernmentType.DEMOCRACY, player.getGovernmentType());
        assertEquals(Player.Ethos.MATERIALIST, player.getEthos());
    }

    // ---- Getters and Setters ----

    @Test
    @DisplayName("setId - should update id")
    void setId_shouldUpdateId() {
        Player player = new Player();
        player.setId(42L);

        assertEquals(42L, player.getId());
    }

    @Test
    @DisplayName("setEmail - should update email")
    void setEmail_shouldUpdateEmail() {
        Player player = new Player();
        player.setEmail("new@example.com");

        assertEquals("new@example.com", player.getEmail());
    }

    @Test
    @DisplayName("setLoginPin - should update PIN")
    void setLoginPin_shouldUpdatePin() {
        Player player = new Player();
        player.setLoginPin("654321");

        assertEquals("654321", player.getLoginPin());
    }

    @Test
    @DisplayName("setEmpireName - should update empire name")
    void setEmpireName_shouldUpdateEmpireName() {
        Player player = new Player();
        player.setEmpireName("Solar Imperium");

        assertEquals("Solar Imperium", player.getEmpireName());
    }

    @Test
    @DisplayName("setGovernmentType - should update government type")
    void setGovernmentType_shouldUpdateGovernmentType() {
        Player player = new Player();
        player.setGovernmentType(Player.GovernmentType.MONARCHY);

        assertEquals(Player.GovernmentType.MONARCHY, player.getGovernmentType());
    }

    @Test
    @DisplayName("setEthos - should update ethos")
    void setEthos_shouldUpdateEthos() {
        Player player = new Player();
        player.setEthos(Player.Ethos.MILITARIST);

        assertEquals(Player.Ethos.MILITARIST, player.getEthos());
    }

    // ---- GovernmentType enum ----

    @Test
    @DisplayName("GovernmentType - should have all expected values")
    void governmentType_shouldHaveAllValues() {
        Player.GovernmentType[] types = Player.GovernmentType.values();

        assertEquals(8, types.length);
        assertContains(types, Player.GovernmentType.DEMOCRACY);
        assertContains(types, Player.GovernmentType.AUTHORITARIAN);
        assertContains(types, Player.GovernmentType.MONARCHY);
        assertContains(types, Player.GovernmentType.OLIGARCHY);
        assertContains(types, Player.GovernmentType.THEOCRACY);
        assertContains(types, Player.GovernmentType.DICTATORSHIP);
        assertContains(types, Player.GovernmentType.CONFEDERATION);
        assertContains(types, Player.GovernmentType.CORPORATE);
    }

    // ---- Ethos enum ----

    @Test
    @DisplayName("Ethos - should have all expected values")
    void ethos_shouldHaveAllValues() {
        Player.Ethos[] ethos = Player.Ethos.values();

        assertEquals(6, ethos.length);
        assertContains(ethos, Player.Ethos.MATERIALIST);
        assertContains(ethos, Player.Ethos.SPIRITUALIST);
        assertContains(ethos, Player.Ethos.LIBERTARIAN);
        assertContains(ethos, Player.Ethos.EQUALIST);
        assertContains(ethos, Player.Ethos.MILITARIST);
        assertContains(ethos, Player.Ethos.PACIFIST);
    }

    // ---- toString ----

    @Test
    @DisplayName("toString - should include all fields")
    void toString_shouldIncludeAllFields() {
        Player player = new Player(1L, "player@example.com", "123456", "Terran Republic",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);

        String str = player.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("player@example.com"));
        assertTrue(str.contains("Terran Republic"));
        assertTrue(str.contains("DEMOCRACY"));
        assertTrue(str.contains("MATERIALIST"));
    }

    // ---- Multiple updates ----

    @Test
    @DisplayName("Multiple setters - should update all fields")
    void multipleSetters_shouldUpdateAllFields() {
        Player player = new Player(1L, "old@example.com", "111111", "Old Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.MATERIALIST);
        player.setEmail("new@example.com");
        player.setLoginPin("222222");
        player.setEmpireName("New Empire");
        player.setGovernmentType(Player.GovernmentType.MONARCHY);
        player.setEthos(Player.Ethos.MILITARIST);

        assertEquals(1L, player.getId());
        assertEquals("new@example.com", player.getEmail());
        assertEquals("222222", player.getLoginPin());
        assertEquals("New Empire", player.getEmpireName());
        assertEquals(Player.GovernmentType.MONARCHY, player.getGovernmentType());
        assertEquals(Player.Ethos.MILITARIST, player.getEthos());
    }

    // ---- Helper ----

    private <T> void assertContains(T[] array, T value) {
        for (T item : array) {
            if (item.equals(value)) return;
        }
        fail("Array does not contain: " + value);
    }
}
