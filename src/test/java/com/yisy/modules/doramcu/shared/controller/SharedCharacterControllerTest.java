package com.yisy.modules.doramcu.shared.controller;

import com.yisy.modules.doramcu.shared.dto.CreateSharedCharacterRequest;
import com.yisy.modules.doramcu.shared.dto.TravelCharacterRequest;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.model.SharedCharacterSkill;
import com.yisy.modules.doramcu.shared.model.SharedCharacterTravelLog;
import com.yisy.modules.doramcu.shared.service.SharedCharacterService;
import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SharedCharacterController Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class SharedCharacterControllerTest {

    @Mock
    private SharedCharacterService sharedCharacterService;

    private SharedCharacterController controller;

    @BeforeEach
    void setUp() {
        controller = new SharedCharacterController(sharedCharacterService);
    }

    // ==================== createSharedCharacter ====================

    @Test
    @DisplayName("createSharedCharacter - should return 201 CREATED with character")
    void createSharedCharacter_success() {
        CreateSharedCharacterRequest request = new CreateSharedCharacterRequest();
        request.setName("New Character");
        request.setOriginSystem(GameType.CAPTAINS_LOG);

        SharedCharacter character = new SharedCharacter("New Character", GameType.CAPTAINS_LOG);
        character.setId(1L);

        when(sharedCharacterService.createSharedCharacter(any(CreateSharedCharacterRequest.class)))
                .thenReturn(character);

        ResponseEntity<SharedCharacter> response = controller.createSharedCharacter(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Character", response.getBody().getName());
        verify(sharedCharacterService).createSharedCharacter(request);
    }

    // ==================== importFromCaptainLog ====================

    @Test
    @DisplayName("importFromCaptainLog - should return 201 CREATED on success")
    void importFromCaptainLog_success() {
        SharedCharacter character = new SharedCharacter("Imported CL", GameType.CAPTAINS_LOG);
        character.setId(1L);
        character.setClCharacterId(50L);

        when(sharedCharacterService.importFromCaptainLog(50L)).thenReturn(character);

        ResponseEntity<SharedCharacter> response = controller.importFromCaptainLog(50L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Imported CL", response.getBody().getName());
        verify(sharedCharacterService).importFromCaptainLog(50L);
    }

    @Test
    @DisplayName("importFromCaptainLog - should return 404 when character not found")
    void importFromCaptainLog_notFound() {
        when(sharedCharacterService.importFromCaptainLog(999L))
                .thenThrow(new IllegalArgumentException("Captain's Log character not found: 999"));

        ResponseEntity<SharedCharacter> response = controller.importFromCaptainLog(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== importFromTraveller ====================

    @Test
    @DisplayName("importFromTraveller - should return 201 CREATED on success")
    void importFromTraveller_success() {
        SharedCharacter character = new SharedCharacter("Imported Traveller", GameType.TRAVELLER);
        character.setId(2L);
        character.setTravellerCharacterId(300);

        when(sharedCharacterService.importFromTraveller(300)).thenReturn(character);

        ResponseEntity<SharedCharacter> response = controller.importFromTraveller(300);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Imported Traveller", response.getBody().getName());
        verify(sharedCharacterService).importFromTraveller(300);
    }

    @Test
    @DisplayName("importFromTraveller - should return 404 when character not found")
    void importFromTraveller_notFound() {
        when(sharedCharacterService.importFromTraveller(999))
                .thenThrow(new IllegalArgumentException("Traveller character not found: 999"));

        ResponseEntity<SharedCharacter> response = controller.importFromTraveller(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== travelCharacter ====================

    @Test
    @DisplayName("travelCharacter - should return 200 OK on success")
    void travelCharacter_success() {
        SharedCharacter character = new SharedCharacter("Traveler", GameType.CAPTAINS_LOG);
        character.setId(1L);

        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.TRAVELLER);

        when(sharedCharacterService.travelCharacter(eq(1L), any(TravelCharacterRequest.class)))
                .thenReturn(character);

        ResponseEntity<SharedCharacter> response = controller.travelCharacter(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(GameType.CAPTAINS_LOG, response.getBody().getOriginSystem());
    }

    @Test
    @DisplayName("travelCharacter - should return 400 when same system")
    void travelCharacter_sameSystem() {
        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.CAPTAINS_LOG);

        when(sharedCharacterService.travelCharacter(eq(1L), any(TravelCharacterRequest.class)))
                .thenThrow(new IllegalArgumentException("Character is already in system: CAPTAINS_LOG"));

        ResponseEntity<SharedCharacter> response = controller.travelCharacter(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("travelCharacter - should return 400 when character not found")
    void travelCharacter_notFound() {
        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.TRAVELLER);

        when(sharedCharacterService.travelCharacter(eq(999L), any(TravelCharacterRequest.class)))
                .thenThrow(new IllegalArgumentException("Shared character not found: 999"));

        ResponseEntity<SharedCharacter> response = controller.travelCharacter(999L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== getSharedCharacter ====================

    @Test
    @DisplayName("getSharedCharacter - should return 200 OK when found")
    void getSharedCharacter_found() {
        SharedCharacter character = new SharedCharacter("Found", GameType.CAPTAINS_LOG);
        character.setId(1L);

        when(sharedCharacterService.getSharedCharacter(1L)).thenReturn(Optional.of(character));

        ResponseEntity<SharedCharacter> response = controller.getSharedCharacter(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Found", response.getBody().getName());
    }

    @Test
    @DisplayName("getSharedCharacter - should return 404 when not found")
    void getSharedCharacter_notFound() {
        when(sharedCharacterService.getSharedCharacter(999L)).thenReturn(Optional.empty());

        ResponseEntity<SharedCharacter> response = controller.getSharedCharacter(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== getAllSharedCharacters ====================

    @Test
    @DisplayName("getAllSharedCharacters - should return list of all characters")
    void getAllSharedCharacters_returnsList() {
        SharedCharacter c1 = new SharedCharacter("Char 1", GameType.CAPTAINS_LOG);
        c1.setId(1L);
        SharedCharacter c2 = new SharedCharacter("Char 2", GameType.TRAVELLER);
        c2.setId(2L);

        when(sharedCharacterService.listSharedCharacters()).thenReturn(List.of(c1, c2));

        ResponseEntity<List<SharedCharacter>> response = controller.getAllSharedCharacters();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getAllSharedCharacters - should return empty list when no characters")
    void getAllSharedCharacters_emptyList() {
        when(sharedCharacterService.listSharedCharacters()).thenReturn(List.of());

        ResponseEntity<List<SharedCharacter>> response = controller.getAllSharedCharacters();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    // ==================== getCharactersBySystem ====================

    @Test
    @DisplayName("getCharactersBySystem - should return characters filtered by system type")
    void getCharactersBySystem_returnsFiltered() {
        SharedCharacter c1 = new SharedCharacter("CL Char", GameType.CAPTAINS_LOG);
        c1.setId(1L);

        when(sharedCharacterService.listBySystem(GameType.CAPTAINS_LOG))
                .thenReturn(List.of(c1));

        ResponseEntity<List<SharedCharacter>> response =
                controller.getCharactersBySystem(GameType.CAPTAINS_LOG);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(GameType.CAPTAINS_LOG, response.getBody().get(0).getOriginSystem());
    }

    // ==================== searchCharacters ====================

    @Test
    @DisplayName("searchCharacters - should return matching characters")
    void searchCharacters_returnsMatches() {
        SharedCharacter c1 = new SharedCharacter("John Doe", GameType.CAPTAINS_LOG);
        c1.setId(1L);
        SharedCharacter c2 = new SharedCharacter("John Smith", GameType.TRAVELLER);
        c2.setId(2L);

        when(sharedCharacterService.searchByName("John")).thenReturn(List.of(c1, c2));

        ResponseEntity<List<SharedCharacter>> response = controller.searchCharacters("John");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("searchCharacters - should return empty list when no matches")
    void searchCharacters_noMatches() {
        when(sharedCharacterService.searchByName("Unknown")).thenReturn(List.of());

        ResponseEntity<List<SharedCharacter>> response = controller.searchCharacters("Unknown");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    // ==================== getTravelHistory ====================

    @Test
    @DisplayName("getTravelHistory - should return travel logs")
    void getTravelHistory_returnsLogs() {
        SharedCharacter character = new SharedCharacter("Traveler", GameType.TRAVELLER);
        character.setId(1L);

        SharedCharacterTravelLog log1 = new SharedCharacterTravelLog(character, "TRAVELLER", "CAPTAINS_LOG");
        log1.setId(1L);
        log1.setTraveledAt(LocalDateTime.of(2024, 1, 1, 12, 0));
        log1.setTravelNotes("First trip");

        SharedCharacterTravelLog log2 = new SharedCharacterTravelLog(character, "CAPTAINS_LOG", "TRAVELLER");
        log2.setId(2L);
        log2.setTraveledAt(LocalDateTime.of(2024, 2, 1, 12, 0));
        log2.setTravelNotes("Return trip");

        when(sharedCharacterService.getTravelHistory(1L)).thenReturn(List.of(log1, log2));

        ResponseEntity<List<SharedCharacterTravelLog>> response = controller.getTravelHistory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("TRAVELLER", response.getBody().get(0).getFromSystem());
        assertEquals("CAPTAINS_LOG", response.getBody().get(0).getToSystem());
    }

    @Test
    @DisplayName("getTravelHistory - should return 404 when character not found")
    void getTravelHistory_notFound() {
        when(sharedCharacterService.getTravelHistory(999L))
                .thenThrow(new IllegalArgumentException("Shared character not found: 999"));

        ResponseEntity<List<SharedCharacterTravelLog>> response = controller.getTravelHistory(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== addSkill ====================

    @Test
    @DisplayName("addSkill - should return 201 CREATED with skill")
    void addSkill_success() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 2);
        skill.setId(1L);
        skill.setSystemSource("TRAVELLER");

        Map<String, Object> params = Map.of(
                "skillName", "Pistol",
                "skillLevel", 2,
                "systemSource", "TRAVELLER"
        );

        when(sharedCharacterService.addSkill(eq(1L), eq("Pistol"), eq(2), eq("TRAVELLER")))
                .thenReturn(skill);

        ResponseEntity<SharedCharacterSkill> response = controller.addSkill(1L, params);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Pistol", response.getBody().getSkillName());
        assertEquals(2, response.getBody().getSkillLevel());
    }

    @Test
    @DisplayName("addSkill - should return 400 when skillName is missing")
    void addSkill_missingSkillName() {
        Map<String, Object> params = Map.of(
                "skillLevel", 1,
                "systemSource", "TRAVELLER"
        );

        ResponseEntity<SharedCharacterSkill> response = controller.addSkill(1L, params);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("addSkill - should return 400 when skillName is blank")
    void addSkill_blankSkillName() {
        Map<String, Object> params = Map.of(
                "skillName", "  ",
                "skillLevel", 1
        );

        ResponseEntity<SharedCharacterSkill> response = controller.addSkill(1L, params);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("addSkill - should return 404 when character not found")
    void addSkill_notFound() {
        Map<String, Object> params = Map.of("skillName", "Pistol");

        when(sharedCharacterService.addSkill(eq(999L), eq("Pistol"), eq(1), isNull()))
                .thenThrow(new IllegalArgumentException("Shared character not found: 999"));

        ResponseEntity<SharedCharacterSkill> response = controller.addSkill(999L, params);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("addSkill - should default skillLevel to 1 when not provided")
    void addSkill_defaultSkillLevel() {
        SharedCharacterSkill skill = new SharedCharacterSkill("Pistol", 1);
        skill.setId(1L);

        Map<String, Object> params = Map.of("skillName", "Pistol");

        when(sharedCharacterService.addSkill(eq(1L), eq("Pistol"), eq(1), isNull()))
                .thenReturn(skill);

        ResponseEntity<SharedCharacterSkill> response = controller.addSkill(1L, params);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getSkillLevel());
    }
}
