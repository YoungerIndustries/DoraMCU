package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unified DTOs Tests")
class UnifiedDtoTest {

    @Test
    @DisplayName("UnifiedLoginRequest should detect empty request")
    void testEmptyRequest() {
        UnifiedLoginRequest request = new UnifiedLoginRequest();
        assertFalse(request.hasSearchCriteria());
        assertNull(request.getFullName());
    }

    @Test
    @DisplayName("UnifiedLoginRequest should detect email-only request")
    void testEmailOnlyRequest() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");
        assertTrue(request.hasSearchCriteria());
        assertNull(request.getFullName());
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    @DisplayName("UnifiedLoginRequest should detect name-only request")
    void testNameOnlyRequest() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("John", "Doe", null);
        assertTrue(request.hasSearchCriteria());
        assertEquals("John Doe", request.getFullName());
    }

    @Test
    @DisplayName("UnifiedLoginRequest should handle first name only")
    void testFirstNameOnly() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("John", null, null);
        assertTrue(request.hasSearchCriteria());
        assertEquals("John", request.getFullName());
    }

    @Test
    @DisplayName("UnifiedLoginRequest should handle last name only")
    void testLastNameOnly() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, "Doe", null);
        assertTrue(request.hasSearchCriteria());
        assertEquals("Doe", request.getFullName());
    }

    @Test
    @DisplayName("UnifiedLoginRequest should ignore blank names")
    void testBlankNames() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("  ", "  ", null);
        assertFalse(request.hasSearchCriteria());
    }

    @Test
    @DisplayName("UnifiedCharacterSummary should build full name")
    void testSummaryFullName() {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "John", "Doe", GameType.TRAVELLER, "Soldier");
        assertEquals("John Doe", summary.getFullName());
        assertEquals(1L, summary.getId());
        assertEquals(GameType.TRAVELLER, summary.getGameType());
        assertEquals("Soldier", summary.getGameRole());
        // Default isAlive is false (primitive boolean default)
        assertFalse(summary.isAlive());
    }

    @Test
    @DisplayName("UnifiedCharacterSummary should handle null names")
    void testSummaryNullNames() {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                2L, null, null, GameType.CAPTAINS_LOG, "Ensign");
        assertEquals("", summary.getFullName());
    }

    @Test
    @DisplayName("UnifiedCharacterSummary setters should work")
    void testSummarySetters() {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary();
        summary.setId(5L);
        summary.setFirstName("Jane");
        summary.setLastName("Smith");
        summary.setGameType(GameType.DORAMCU_4X);
        summary.setGameRole("Empire Ruler");
        summary.setDescription("Test description");
        summary.setAlive(false);
        summary.setLastActivity("2024-01-01");

        assertEquals(5L, summary.getId());
        assertEquals("Jane", summary.getFirstName());
        assertEquals("Smith", summary.getLastName());
        assertEquals(GameType.DORAMCU_4X, summary.getGameType());
        assertEquals("Empire Ruler", summary.getGameRole());
        assertEquals("Test description", summary.getDescription());
        assertFalse(summary.isAlive());
        assertEquals("2024-01-01", summary.getLastActivity());
    }

    @Test
    @DisplayName("UnifiedCharacterSummary toString should not be null")
    void testSummaryToString() {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "John", "Doe", GameType.TRAVELLER, "Soldier");
        String str = summary.toString();
        assertNotNull(str);
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("TRAVELLER"));
    }

    @Test
    @DisplayName("UnifiedLoginResponse should initialize lists")
    void testResponseInitialization() {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        assertNotNull(response.getCharacters());
        assertNotNull(response.getAvailableGames());
        assertTrue(response.getCharacters().isEmpty());
        assertTrue(response.getAvailableGames().isEmpty());
        assertFalse(response.isNeedsCharacterSelection());
        assertFalse(response.isNeedsCharacterCreation());
    }

    @Test
    @DisplayName("UnifiedLoginResponse should add characters")
    void testResponseAddCharacter() {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        UnifiedCharacterSummary char1 = new UnifiedCharacterSummary(
                1L, "John", "Doe", GameType.TRAVELLER, "Soldier");
        response.addCharacter(char1);
        assertEquals(1, response.getCharacters().size());
    }

    @Test
    @DisplayName("UnifiedLoginResponse should add games")
    void testResponseAddGame() {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        UnifiedLoginResponse.GameTypeInfo game = new UnifiedLoginResponse.GameTypeInfo(
                GameType.TRAVELLER, "Traveller", "Space RPG");
        response.addAvailableGame(game);
        assertEquals(1, response.getAvailableGames().size());
    }

    @Test
    @DisplayName("GameTypeInfo should add character types")
    void testGameTypeInfo() {
        UnifiedLoginResponse.GameTypeInfo game = new UnifiedLoginResponse.GameTypeInfo(
                GameType.TRAVELLER, "Traveller", "Space RPG");
        game.addCharacterType("Empire Leader");
        game.addCharacterType("Full Traveller Character");
        assertEquals(2, game.getCharacterTypes().size());
        assertEquals(GameType.TRAVELLER, game.getGameType());
    }

    @Test
    @DisplayName("CreateCharacterRequest should validate valid request")
    void testValidCreateRequest() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "John", "Doe", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);
        assertTrue(request.isValid());
        assertEquals("John Doe", request.getFullName());
    }

    @Test
    @DisplayName("CreateCharacterRequest should reject missing game type")
    void testInvalidCreateRequestNoGame() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("test@example.com");
        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("CreateCharacterRequest should reject missing name")
    void testInvalidCreateRequestNoName() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setGameType(GameType.TRAVELLER);
        request.setEmail("test@example.com");
        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("CreateCharacterRequest should reject missing email")
    void testInvalidCreateRequestNoEmail() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "John", "Doe", null);
        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("CreateCharacterRequest should handle Captain's Log fields")
    void testCaptainsLogFields() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setGameType(GameType.CAPTAINS_LOG);
        request.setFirstName("James");
        request.setLastName("Kirk");
        request.setEmail("kirk@starfleet.com");
        request.setPronouns("he/him");
        request.setTrait1("Bold");
        request.setTrait2("Reckless");
        request.setTrait3("Leader");

        assertTrue(request.isValid());
        assertEquals("he/him", request.getPronouns());
        assertEquals("Bold", request.getTrait1());
        assertEquals("Reckless", request.getTrait2());
        assertEquals("Leader", request.getTrait3());
    }
}
