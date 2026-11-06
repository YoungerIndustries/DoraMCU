package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    // ==================== Constructors ====================

    @Test
    void testConstructorDefaults() {
        Player player = new Player(1, "player@test.com");

        assertEquals(1, player.getId());
        assertEquals("player@test.com", player.getEmail());
        assertEquals("Unnamed Empire", player.getEmpireName());
        assertEquals(Player.GovernmentType.DEMOCRACY, player.getGovernmentType());
        assertEquals(Player.Ethos.MATERIALIST, player.getEthos());
        assertNotNull(player.getLoginPin());
        assertEquals(6, player.getLoginPin().length());
    }

    @Test
    void testConstructorWithEmpireName() {
        Player player = new Player(2, "player@test.com", "Terran Republic");

        assertEquals(2, player.getId());
        assertEquals("player@test.com", player.getEmail());
        assertEquals("Terran Republic", player.getEmpireName());
        assertEquals(Player.GovernmentType.DEMOCRACY, player.getGovernmentType());
        assertEquals(Player.Ethos.MATERIALIST, player.getEthos());
    }

    @Test
    void testConstructorWithFullCharacteristics() {
        Player player = new Player(3, "player@test.com", "Galactic Empire",
                Player.GovernmentType.AUTHORITARIAN, Player.Ethos.MILITARIST);

        assertEquals(3, player.getId());
        assertEquals("player@test.com", player.getEmail());
        assertEquals("Galactic Empire", player.getEmpireName());
        assertEquals(Player.GovernmentType.AUTHORITARIAN, player.getGovernmentType());
        assertEquals(Player.Ethos.MILITARIST, player.getEthos());
    }

    @Test
    void testConstructorNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, null));
    }

    @Test
    void testConstructorBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "   "));
    }

    @Test
    void testConstructorEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, ""));
    }

    @Test
    void testConstructorWithEmpireNameNullEmpire() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "a@b.com", null));
    }

    @Test
    void testConstructorWithEmpireNameBlankEmpire() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "a@b.com", "   "));
    }

    @Test
    void testConstructorFullNullGovernment() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "a@b.com", "Empire",
                null, Player.Ethos.MATERIALIST));
    }

    @Test
    void testConstructorFullNullEthos() {
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "a@b.com", "Empire",
                Player.GovernmentType.DEMOCRACY, null));
    }

    // ==================== Accessors ====================

    @Test
    void testGetId() {
        Player player = new Player(42, "test@test.com");
        assertEquals(42, player.getId());
    }

    @Test
    void testGetEmail() {
        Player player = new Player(1, "user@example.org");
        assertEquals("user@example.org", player.getEmail());
    }

    @Test
    void testGetLoginPin() {
        Player player = new Player(1, "test@test.com");
        String pin = player.getLoginPin();
        assertNotNull(pin);
        assertEquals(6, pin.length());
        // Verify it's a valid number
        int pinInt = Integer.parseInt(pin);
        assertTrue(pinInt >= Player.MIN_PIN);
        assertTrue(pinInt <= Player.MAX_PIN);
    }

    @Test
    void testGetLoginPinInt() {
        Player player = new Player(1, "test@test.com");
        int pinInt = player.getLoginPinInt();
        assertTrue(pinInt >= Player.MIN_PIN);
        assertTrue(pinInt <= Player.MAX_PIN);
        assertEquals(player.getLoginPin(), String.valueOf(pinInt));
    }

    @Test
    void testGetEmpireName() {
        Player player = new Player(1, "test@test.com", "United Systems");
        assertEquals("United Systems", player.getEmpireName());
    }

    @Test
    void testGetGovernmentType() {
        Player player = new Player(1, "test@test.com", "Empire",
                Player.GovernmentType.MONARCHY, Player.Ethos.SPIRITUALIST);
        assertEquals(Player.GovernmentType.MONARCHY, player.getGovernmentType());
    }

    @Test
    void testGetEthos() {
        Player player = new Player(1, "test@test.com", "Empire",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.PACIFIST);
        assertEquals(Player.Ethos.PACIFIST, player.getEthos());
    }

    // ==================== Setters ====================

    @Test
    void testSetEmpireName() {
        Player player = new Player(1, "test@test.com");
        player.setEmpireName("New Empire");
        assertEquals("New Empire", player.getEmpireName());
    }

    @Test
    void testSetEmpireNameNull() {
        Player player = new Player(1, "test@test.com");
        assertThrows(IllegalArgumentException.class, () -> player.setEmpireName(null));
    }

    @Test
    void testSetEmpireNameBlank() {
        Player player = new Player(1, "test@test.com");
        assertThrows(IllegalArgumentException.class, () -> player.setEmpireName("   "));
    }

    @Test
    void testSetGovernmentType() {
        Player player = new Player(1, "test@test.com");
        player.setGovernmentType(Player.GovernmentType.THEOCRACY);
        assertEquals(Player.GovernmentType.THEOCRACY, player.getGovernmentType());
    }

    @Test
    void testSetGovernmentTypeNull() {
        Player player = new Player(1, "test@test.com");
        assertThrows(IllegalArgumentException.class, () -> player.setGovernmentType(null));
    }

    @Test
    void testSetEthos() {
        Player player = new Player(1, "test@test.com");
        player.setEthos(Player.Ethos.LIBERTARIAN);
        assertEquals(Player.Ethos.LIBERTARIAN, player.getEthos());
    }

    @Test
    void testSetEthosNull() {
        Player player = new Player(1, "test@test.com");
        assertThrows(IllegalArgumentException.class, () -> player.setEthos(null));
    }

    // ==================== PIN Validation ====================

    @Test
    void testValidateLoginPinString() {
        Player player = new Player(1, "test@test.com");
        String pin = player.getLoginPin();
        assertTrue(player.validateLoginPin(pin));
    }

    @Test
    void testValidateLoginPinInt() {
        Player player = new Player(1, "test@test.com");
        int pin = player.getLoginPinInt();
        assertTrue(player.validateLoginPin(pin));
    }

    @Test
    void testValidateLoginPinWrongString() {
        Player player = new Player(1, "test@test.com");
        assertFalse(player.validateLoginPin("000000"));
    }

    @Test
    void testValidateLoginPinWrongInt() {
        Player player = new Player(1, "test@test.com");
        assertFalse(player.validateLoginPin(123456));
    }

    // ==================== PIN Masking ====================

    @Test
    void testMaskPin() {
        Player player = new Player(1, "test@test.com");
        String pin = player.getLoginPin();
        String masked = player.maskPin();

        assertEquals(6, masked.length());
        assertEquals(pin.charAt(0), masked.charAt(0));
        assertEquals(pin.charAt(pin.length() - 1), masked.charAt(masked.length() - 1));
        assertEquals('*', masked.charAt(1));
        assertEquals('*', masked.charAt(2));
        assertEquals('*', masked.charAt(3));
        assertEquals('*', masked.charAt(4));
    }

    @Test
    void testMaskPinFormat() {
        Player player = new Player(1, "test@test.com");
        String masked = player.maskPin();
        // Format should be: digit + 4 asterisks + digit
        assertTrue(masked.matches("\\d\\*{4}\\d"));
    }

    // ==================== PIN Constants ====================

    @Test
    void testMinPinConstant() {
        assertEquals(100_000, Player.MIN_PIN);
    }

    @Test
    void testMaxPinConstant() {
        assertEquals(999_999, Player.MAX_PIN);
    }

    // ==================== GovernmentType Enum ====================

    @Test
    void testGovernmentTypeValues() {
        Player.GovernmentType[] values = Player.GovernmentType.values();
        assertEquals(8, values.length);
        assertContains(values, Player.GovernmentType.DEMOCRACY);
        assertContains(values, Player.GovernmentType.AUTHORITARIAN);
        assertContains(values, Player.GovernmentType.MONARCHY);
        assertContains(values, Player.GovernmentType.OLIGARCHY);
        assertContains(values, Player.GovernmentType.THEOCRACY);
        assertContains(values, Player.GovernmentType.DICTATORSHIP);
        assertContains(values, Player.GovernmentType.CONFEDERATION);
        assertContains(values, Player.GovernmentType.CORPORATE);
    }

    @Test
    void testGovernmentTypeValueOf() {
        assertEquals(Player.GovernmentType.DEMOCRACY,
                Player.GovernmentType.valueOf("DEMOCRACY"));
    }

    // ==================== Ethos Enum ====================

    @Test
    void testEthosValues() {
        Player.Ethos[] values = Player.Ethos.values();
        assertEquals(6, values.length);
        assertContains(values, Player.Ethos.MATERIALIST);
        assertContains(values, Player.Ethos.SPIRITUALIST);
        assertContains(values, Player.Ethos.LIBERTARIAN);
        assertContains(values, Player.Ethos.EQUALIST);
        assertContains(values, Player.Ethos.MILITARIST);
        assertContains(values, Player.Ethos.PACIFIST);
    }

    @Test
    void testEthosValueOf() {
        assertEquals(Player.Ethos.MATERIALIST,
                Player.Ethos.valueOf("MATERIALIST"));
    }

    // ==================== Object Contract ====================

    @Test
    void testEqualsReflexive() {
        Player player = new Player(1, "test@test.com");
        assertEquals(player, player);
    }

    @Test
    void testEqualsSymmetric() {
        Player p1 = new Player(1, "a@test.com");
        Player p2 = new Player(1, "b@test.com");
        assertEquals(p1, p2);
        assertEquals(p2, p1);
    }

    @Test
    void testEqualsTransitive() {
        Player p1 = new Player(1, "a@test.com");
        Player p2 = new Player(1, "b@test.com");
        Player p3 = new Player(1, "c@test.com");
        assertEquals(p1, p2);
        assertEquals(p2, p3);
        assertEquals(p1, p3);
    }

    @Test
    void testEqualsDifferentId() {
        Player p1 = new Player(1, "test@test.com");
        Player p2 = new Player(2, "test@test.com");
        assertNotEquals(p1, p2);
    }

    @Test
    void testEqualsNull() {
        Player player = new Player(1, "test@test.com");
        assertNotEquals(player, null);
    }

    @Test
    void testEqualsDifferentClass() {
        Player player = new Player(1, "test@test.com");
        assertNotEquals(player, "string");
    }

    @Test
    void testHashCodeSameId() {
        Player p1 = new Player(1, "a@test.com");
        Player p2 = new Player(1, "b@test.com");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // ==================== ToString ====================

    @Test
    void testToString() {
        Player player = new Player(1, "test@test.com", "Empire",
                Player.GovernmentType.MONARCHY, Player.Ethos.MILITARIST);
        String str = player.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("email='test@test.com'"));
        assertTrue(str.contains("empire='Empire'"));
        assertTrue(str.contains("gov=MONARCHY"));
        assertTrue(str.contains("ethos=MILITARIST"));
        assertTrue(str.contains("eliminated=false"));
    }

    // ==================== Integration ====================

    @Test
    void testFullPlayerCreation() {
        Player player = new Player(1, "commander@empire.com", "Terran Hegemony",
                Player.GovernmentType.DEMOCRACY, Player.Ethos.EQUALIST);

        assertEquals(1, player.getId());
        assertEquals("commander@empire.com", player.getEmail());
        assertEquals("Terran Hegemony", player.getEmpireName());
        assertEquals(Player.GovernmentType.DEMOCRACY, player.getGovernmentType());
        assertEquals(Player.Ethos.EQUALIST, player.getEthos());
        assertNotNull(player.getLoginPin());
        assertTrue(player.validateLoginPin(player.getLoginPin()));
    }

    @Test
    void testChangeEmpireCharacteristics() {
        Player player = new Player(1, "test@test.com");

        player.setEmpireName("New Order");
        player.setGovernmentType(Player.GovernmentType.DICTATORSHIP);
        player.setEthos(Player.Ethos.MILITARIST);

        assertEquals("New Order", player.getEmpireName());
        assertEquals(Player.GovernmentType.DICTATORSHIP, player.getGovernmentType());
        assertEquals(Player.Ethos.MILITARIST, player.getEthos());
    }

    @Test
    void testMultiplePlayersUniqueIds() {
        Player p1 = new Player(1, "a@test.com");
        Player p2 = new Player(2, "b@test.com");
        Player p3 = new Player(3, "c@test.com");

        assertNotEquals(p1, p2);
        assertNotEquals(p2, p3);
        assertNotEquals(p1, p3);
    }

    @Test
    void testMultiplePlayersUniquePins() {
        // While theoretically possible, it's extremely unlikely two random 6-digit PINs collide
        Player p1 = new Player(1, "a@test.com");
        Player p2 = new Player(2, "b@test.com");

        // Both should have valid 6-digit PINs
        assertEquals(6, p1.getLoginPin().length());
        assertEquals(6, p2.getLoginPin().length());
    }

    @Test
    void testPinInRange() {
        for (int i = 0; i < 100; i++) {
            Player player = new Player(i, "test" + i + "@test.com");
            int pin = player.getLoginPinInt();
            assertTrue(pin >= Player.MIN_PIN, "PIN should be >= " + Player.MIN_PIN);
            assertTrue(pin <= Player.MAX_PIN, "PIN should be <= " + Player.MAX_PIN);
        }
    }

    // ==================== Helper Methods ====================

    private static <T> void assertContains(T[] array, T value) {
        for (T item : array) {
            if (item == value) {
                return;
            }
        }
        fail("Array does not contain: " + value);
    }
}
