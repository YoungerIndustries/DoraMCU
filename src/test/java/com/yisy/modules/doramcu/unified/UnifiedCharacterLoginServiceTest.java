package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Assignment;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Environment;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Rank;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.repository.SharedCharacterRepository;
import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedLoginResponse;
import com.yisy.modules.doramcu.traveller.TourOfDutyService;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnifiedCharacterLoginService Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class UnifiedCharacterLoginServiceTest {

    @Mock
    private CaptainLogCharacterRepository clCharacterRepository;

    @Mock
    private SharedCharacterRepository sharedCharacterRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CharacterService travellerCharacterService;

    @Mock
    private TourOfDutyService tourOfDutyService;

    private UnifiedCharacterLoginService service;

    @BeforeEach
    void setUp() {
        service = new UnifiedCharacterLoginService(
                clCharacterRepository,
                sharedCharacterRepository,
                playerRepository,
                travellerCharacterService,
                tourOfDutyService
        );
    }

    private CaptainLogCharacter makeClChar(Long id, String name) {
        CaptainLogCharacter c = new CaptainLogCharacter();
        c.setId(id);
        c.setName(name);
        c.setRank(Rank.ENSIGN);
        c.setAssignment(Assignment.COMMAND);
        c.setEnvironment(Environment.CITY);
        return c;
    }

    // ---- Empty login request ----

    @Test
    @DisplayName("processLogin should return available games when no search criteria")
    void testProcessLoginEmptyRequest() {
        UnifiedLoginRequest request = new UnifiedLoginRequest();
        assertFalse(request.hasSearchCriteria());

        UnifiedLoginResponse response = service.processLogin(request);

        assertTrue(response.isNeedsCharacterCreation());
        assertFalse(response.isNeedsCharacterSelection());
        assertEquals(3, response.getAvailableGames().size());
        assertTrue(response.getCharacters().isEmpty());
    }

    @Test
    @DisplayName("processLogin should return available games when blank search criteria")
    void testProcessLoginBlankRequest() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("  ", "  ", "  ");

        UnifiedLoginResponse response = service.processLogin(request);

        assertTrue(response.isNeedsCharacterCreation());
        assertEquals(3, response.getAvailableGames().size());
    }

    // ---- Email-only search ----

    @Test
    @DisplayName("processLogin should search by email only")
    void testProcessLoginEmailOnly() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertTrue(response.getCharacters().isEmpty());
        assertTrue(response.isNeedsCharacterCreation());
        verify(clCharacterRepository).findAll();
        verify(sharedCharacterRepository).findAll();
        verify(playerRepository).findByEmail("test@example.com");
        verify(travellerCharacterService).listCharacters();
    }

    @Test
    @DisplayName("processLogin should find Player by email")
    void testProcessLoginFindPlayerByEmail() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        Player player = new Player();
        player.setId(1L);
        player.setEmpireName("Test Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(player));
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        UnifiedCharacterSummary summary = response.getCharacters().get(0);
        assertEquals(GameType.DORAMCU_4X, summary.getGameType());
        assertEquals("Empire Ruler", summary.getGameRole());
        assertEquals("Test Empire", summary.getFullName());
    }

    @Test
    @DisplayName("processLogin should find Traveller characters by email")
    void testProcessLoginFindTravellerByEmail() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        UnifiedCharacterSummary summary = response.getCharacters().get(0);
        assertEquals(GameType.TRAVELLER, summary.getGameType());
        assertEquals("Alex Starborn", summary.getFullName());
    }

    @Test
    @DisplayName("processLogin should find Captain's Log characters by email with name filter")
    void testProcessLoginFindCLCharsByEmailWithName() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("Alex", "Starborn", "test@example.com");

        when(clCharacterRepository.findAll()).thenReturn(List.of(makeClChar(1L, "Alex Starborn")));
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertEquals(GameType.CAPTAINS_LOG, response.getCharacters().get(0).getGameType());
    }

    @Test
    @DisplayName("processLogin should filter CL characters by name when email provided")
    void testProcessLoginFilterCLCharsByName() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("Alex", null, "test@example.com");

        when(clCharacterRepository.findAll()).thenReturn(List.of(
                makeClChar(1L, "Alex Starborn"),
                makeClChar(2L, "Bob Smith")
        ));
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertEquals("Alex Starborn", response.getCharacters().get(0).getFullName());
    }

    // ---- Name-only search ----

    @Test
    @DisplayName("processLogin should search by name only")
    void testProcessLoginNameOnly() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("Alex", "Starborn", null);

        when(clCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertTrue(response.getCharacters().isEmpty());
        assertTrue(response.isNeedsCharacterCreation());
        assertFalse(response.isNeedsCharacterSelection());
    }

    @Test
    @DisplayName("processLogin should find Traveller character by name")
    void testProcessLoginFindTravellerByName() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("Alex", "Starborn", null);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();

        when(clCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertEquals(GameType.TRAVELLER, response.getCharacters().get(0).getGameType());
    }

    @Test
    @DisplayName("processLogin should find Shared character by name")
    void testProcessLoginFindSharedByName() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("Alex", "Starborn", null);

        SharedCharacter sharedChar = new SharedCharacter();
        sharedChar.setId(1L);
        sharedChar.setName("Alex Starborn");
        sharedChar.setOriginSystem(GameType.TRAVELLER);

        when(clCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(List.of(sharedChar));
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertEquals(GameType.TRAVELLER, response.getCharacters().get(0).getGameType());
    }

    @Test
    @DisplayName("processLogin should search Shared with last name when first name null")
    void testProcessLoginSharedWithLastNameOnly() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, "Starborn", null);

        SharedCharacter sharedChar = new SharedCharacter();
        sharedChar.setId(1L);
        sharedChar.setName("Alex Starborn");
        sharedChar.setOriginSystem(GameType.TRAVELLER);

        when(sharedCharacterRepository.findByNameContainingIgnoreCase("Starborn"))
                .thenReturn(List.of(sharedChar));
        when(clCharacterRepository.findByNameContainingIgnoreCase("Starborn"))
                .thenReturn(Collections.emptyList());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
    }

    // ---- Multiple characters ----

    @Test
    @DisplayName("processLogin should set needsCharacterSelection when multiple matches")
    void testProcessLoginMultipleMatches() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();

        Player player = new Player();
        player.setId(2L);
        player.setEmpireName("Alex Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(player));
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(2, response.getCharacters().size());
        assertTrue(response.isNeedsCharacterSelection());
        assertFalse(response.isNeedsCharacterCreation());
    }

    // ---- Available games ----

    @Test
    @DisplayName("getAvailableGames should return all game types")
    void testGetAvailableGames() {
        List<GameType> games = service.getAvailableGames();

        assertEquals(3, games.size());
        assertTrue(games.contains(GameType.TRAVELLER));
        assertTrue(games.contains(GameType.CAPTAINS_LOG));
        assertTrue(games.contains(GameType.DORAMCU_4X));
    }

    @Test
    @DisplayName("getTravellerCharacterTypes should return all types")
    void testGetTravellerCharacterTypes() {
        List<TravellerCharacterType> types = service.getTravellerCharacterTypes();

        assertEquals(5, types.size());
        assertTrue(types.contains(TravellerCharacterType.FULL_TRAVELLER));
        assertTrue(types.contains(TravellerCharacterType.EMPIRE_LEADER));
    }

    // ---- Helper convenience methods ----

    @Test
    @DisplayName("getCharactersByEmail should delegate to processLogin")
    void testGetCharactersByEmail() {
        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        List<UnifiedCharacterSummary> characters = service.getCharactersByEmail("test@example.com");

        assertNotNull(characters);
        assertTrue(characters.isEmpty());
    }

    @Test
    @DisplayName("getCharactersByName should delegate to processLogin")
    void testGetCharactersByName() {
        when(clCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("Alex"))
                .thenReturn(Collections.emptyList());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        List<UnifiedCharacterSummary> characters = service.getCharactersByName("Alex", "Starborn");

        assertNotNull(characters);
        assertTrue(characters.isEmpty());
    }

    // ---- Name matching edge cases ----

    @Test
    @DisplayName("processLogin should match names case-insensitively")
    void testProcessLoginCaseInsensitiveName() {
        UnifiedLoginRequest request = new UnifiedLoginRequest("alex", "starborn", null);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();

        when(clCharacterRepository.findByNameContainingIgnoreCase("alex"))
                .thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("alex"))
                .thenReturn(Collections.emptyList());
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
    }

    @Test
    @DisplayName("processLogin should set player email in response")
    void testProcessLoginSetsPlayerEmail() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals("test@example.com", response.getPlayerEmail());
    }

    @Test
    @DisplayName("processLogin should populate available games in response")
    void testProcessLoginPopulatesAvailableGames() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(3, response.getAvailableGames().size());

        // Verify Traveller game has character types
        UnifiedLoginResponse.GameTypeInfo travellerGame = response.getAvailableGames().stream()
                .filter(g -> g.getGameType() == GameType.TRAVELLER)
                .findFirst()
                .orElseThrow();
        assertEquals(5, travellerGame.getCharacterTypes().size());

        // Verify Captain's Log game
        UnifiedLoginResponse.GameTypeInfo clGame = response.getAvailableGames().stream()
                .filter(g -> g.getGameType() == GameType.CAPTAINS_LOG)
                .findFirst()
                .orElseThrow();
        assertTrue(clGame.getCharacterTypes().contains("Starfleet Officer"));
        assertTrue(clGame.getCharacterTypes().contains("Civilian"));

        // Verify DoraMCU 4X game
        UnifiedLoginResponse.GameTypeInfo doraGame = response.getAvailableGames().stream()
                .filter(g -> g.getGameType() == GameType.DORAMCU_4X)
                .findFirst()
                .orElseThrow();
        assertTrue(doraGame.getCharacterTypes().contains("Empire Leader"));
    }

    @Test
    @DisplayName("processLogin should handle Shared character with CAPTAINS_LOG origin")
    void testProcessLoginSharedCaptainsLogOrigin() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        SharedCharacter sharedChar = new SharedCharacter();
        sharedChar.setId(1L);
        sharedChar.setName("James Kirk");
        sharedChar.setOriginSystem(GameType.CAPTAINS_LOG);

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(List.of(sharedChar));
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(Collections.emptyList());

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        UnifiedCharacterSummary summary = response.getCharacters().get(0);
        assertEquals(GameType.CAPTAINS_LOG, summary.getGameType());
        assertEquals("James Kirk", summary.getFullName());
    }

    @Test
    @DisplayName("processLogin should handle dead Traveller character")
    void testProcessLoginDeadTravellerCharacter() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Dead Character");
        travellerChar.rollAbilities();
        travellerChar.setAlive(false);

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertFalse(response.getCharacters().get(0).isAlive());
    }

    @Test
    @DisplayName("processLogin should handle Traveller character with no career tours")
    void testProcessLoginTravellerNoCareer() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Civilian Alex");
        travellerChar.rollAbilities();
        // No career tours added

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertEquals("Civilian", response.getCharacters().get(0).getGameRole());
    }

    @Test
    @DisplayName("processLogin should handle single match without selection flag")
    void testProcessLoginSingleMatch() {
        UnifiedLoginRequest request = new UnifiedLoginRequest(null, null, "test@example.com");

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();

        when(clCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());
        when(playerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(travellerCharacterService.listCharacters()).thenReturn(List.of(travellerChar));

        UnifiedLoginResponse response = service.processLogin(request);

        assertEquals(1, response.getCharacters().size());
        assertFalse(response.isNeedsCharacterSelection());
        assertFalse(response.isNeedsCharacterCreation());
    }
}
