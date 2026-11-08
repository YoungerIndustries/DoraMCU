package com.yisy.modules.doramcu.unified.controller;

import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.TravellerCharacterType;
import com.yisy.modules.doramcu.traveller.TravellerAdventureService;
import com.yisy.modules.doramcu.traveller.TourOfDutyService;
import com.yisy.modules.doramcu.unified.TravellerTo4XIntegrationService;
import com.yisy.modules.doramcu.unified.UnifiedCharacterCreationService;
import com.yisy.modules.doramcu.unified.UnifiedCharacterLoginService;
import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnifiedCharacterController Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class UnifiedCharacterControllerTest {

    @Mock
    private UnifiedCharacterLoginService loginService;

    @Mock
    private UnifiedCharacterCreationService creationService;

    @Mock
    private TravellerTo4XIntegrationService integrationService;

    @Mock
    private TourOfDutyService tourOfDutyService;

    @Mock
    private TravellerAdventureService adventureService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UnifiedCharacterController controller = new UnifiedCharacterController(loginService, creationService, integrationService, tourOfDutyService, adventureService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice()
                .build();
        objectMapper = new ObjectMapper();
    }

    // ---- POST /login ----

    @Test
    @DisplayName("unifiedLogin should return 200 with empty request body")
    void testUnifiedLoginEmptyBody() throws Exception {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        response.setNeedsCharacterCreation(true);

        when(loginService.processLogin(any(UnifiedLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/unified/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsCharacterCreation").value(true));

        verify(loginService).processLogin(any(UnifiedLoginRequest.class));
    }

    @Test
    @DisplayName("unifiedLogin should return 200 with email search")
    void testUnifiedLoginWithEmail() throws Exception {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        response.setPlayerEmail("test@example.com");

        when(loginService.processLogin(any(UnifiedLoginRequest.class))).thenReturn(response);

        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        mockMvc.perform(post("/api/v1/unified/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerEmail").value("test@example.com"));
    }

    @Test
    @DisplayName("unifiedLogin should return 200 with name search")
    void testUnifiedLoginWithName() throws Exception {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "Alex", "Starborn", GameType.TRAVELLER, "Soldier");
        response.addCharacter(summary);

        when(loginService.processLogin(any(UnifiedLoginRequest.class))).thenReturn(response);

        UnifiedLoginRequest request = new UnifiedLoginRequest("Alex", "Starborn", null);

        mockMvc.perform(post("/api/v1/unified/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters.length()").value(1))
                .andExpect(jsonPath("$.characters[0].fullName").value("Alex Starborn"));
    }

    @Test
    @DisplayName("unifiedLogin should handle null request body")
    void testUnifiedLoginNullBody() throws Exception {
        UnifiedLoginResponse response = new UnifiedLoginResponse();
        response.setNeedsCharacterCreation(true);

        when(loginService.processLogin(any(UnifiedLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/unified/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ---- GET /characters/search ----

    @Test
    @DisplayName("searchCharacters should return matching characters")
    void testSearchCharacters() throws Exception {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "Alex", "Starborn", GameType.TRAVELLER, "Soldier");

        when(loginService.getCharactersByName("Alex", "Starborn"))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/unified/characters/search")
                        .param("firstName", "Alex")
                        .param("lastName", "Starborn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Alex Starborn"));
    }

    @Test
    @DisplayName("searchCharacters should return empty list when no matches")
    void testSearchCharactersNoMatches() throws Exception {
        when(loginService.getCharactersByName("Unknown", null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/unified/characters/search")
                        .param("firstName", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- GET /characters/email ----

    @Test
    @DisplayName("getCharactersByEmail should return characters for email")
    void testGetCharactersByEmail() throws Exception {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "Alex", "Starborn", GameType.TRAVELLER, "Soldier");

        when(loginService.getCharactersByEmail("test@example.com"))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/unified/characters/email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ---- GET /games ----

    @Test
    @DisplayName("getAvailableGames should return all game types")
    void testGetAvailableGames() throws Exception {
        when(loginService.getAvailableGames())
                .thenReturn(List.of(GameType.TRAVELLER, GameType.CAPTAINS_LOG, GameType.DORAMCU_4X));

        mockMvc.perform(get("/api/v1/unified/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ---- GET /traveller/types ----

    @Test
    @DisplayName("getTravellerCharacterTypes should return all types")
    void testGetTravellerCharacterTypes() throws Exception {
        when(loginService.getTravellerCharacterTypes())
                .thenReturn(List.of(TravellerCharacterType.values()));

        mockMvc.perform(get("/api/v1/unified/traveller/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @DisplayName("getTravellerCharacterType should return details for valid type")
    void testGetTravellerCharacterTypeValid() throws Exception {
        mockMvc.perform(get("/api/v1/unified/traveller/types/FULL_TRAVELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Full Traveller Character"))
                .andExpect(jsonPath("$.isFullCharacter").value("true"))
                .andExpect(jsonPath("$.isManagementRole").value("false"));
    }

    @Test
    @DisplayName("getTravellerCharacterType should return 404 for invalid type")
    void testGetTravellerCharacterTypeInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/unified/traveller/types/INVALID_TYPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ---- POST /characters/create/start ----

    @Test
    @DisplayName("startCharacterCreation should return available games")
    void testStartCharacterCreation() throws Exception {
        when(creationService.getAvailableGames())
                .thenReturn(List.of(GameType.TRAVELLER, GameType.CAPTAINS_LOG, GameType.DORAMCU_4X));

        mockMvc.perform(post("/api/v1/unified/characters/create/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.games.length()").value(3));
    }

    // ---- POST /characters/create/traveller/types ----

    @Test
    @DisplayName("getTravellerCreationTypes should return character types")
    void testGetTravellerCreationTypes() throws Exception {
        when(creationService.getTravellerCharacterTypes())
                .thenReturn(List.of(TravellerCharacterType.values()));

        mockMvc.perform(post("/api/v1/unified/characters/create/traveller/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.gameType").value("TRAVELLER"))
                .andExpect(jsonPath("$.characterTypes.length()").value(5));
    }

    // ---- POST /characters ----

    @Test
    @DisplayName("createCharacter should return 201 with created character")
    void testCreateCharacterSuccess() throws Exception {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "Alex", "Starborn", GameType.TRAVELLER, "Full Traveller Character");
        summary.setAlive(true);

        when(creationService.createCharacter(any(CreateCharacterRequest.class)))
                .thenReturn(summary);

        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);

        mockMvc.perform(post("/api/v1/unified/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Alex Starborn"))
                .andExpect(jsonPath("$.gameType").value("TRAVELLER"));
    }

    @Test
    @DisplayName("createCharacter should return 400 for invalid request")
    void testCreateCharacterBadRequest() throws Exception {
        when(creationService.createCharacter(any(CreateCharacterRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        CreateCharacterRequest request = new CreateCharacterRequest();

        mockMvc.perform(post("/api/v1/unified/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createCharacter should create Captain's Log character")
    void testCreateCaptainsLogCharacter() throws Exception {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "James", "Kirk", GameType.CAPTAINS_LOG, "Ensign");
        summary.setAlive(true);

        when(creationService.createCharacter(any(CreateCharacterRequest.class)))
                .thenReturn(summary);

        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.CAPTAINS_LOG, "James", "Kirk", "kirk@starfleet.com");
        request.setPronouns("he/him");

        mockMvc.perform(post("/api/v1/unified/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameType").value("CAPTAINS_LOG"));
    }

    @Test
    @DisplayName("createCharacter should create DoraMCU 4X character")
    void testCreateDoraMCUCharacter() throws Exception {
        UnifiedCharacterSummary summary = new UnifiedCharacterSummary(
                1L, "Alex", "Starborn", GameType.DORAMCU_4X, "Empire Ruler");
        summary.setAlive(true);

        when(creationService.createCharacter(any(CreateCharacterRequest.class)))
                .thenReturn(summary);

        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.DORAMCU_4X, "Alex", "Starborn", "test@example.com");

        mockMvc.perform(post("/api/v1/unified/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameType").value("DORAMCU_4X"));
    }
}
