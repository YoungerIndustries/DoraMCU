package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Assignment;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Environment;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Rank;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.unified.TravellerTo4XIntegrationService;
import com.yisy.modules.doramcu.unified.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnifiedCharacterCreationService Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class UnifiedCharacterCreationServiceTest {

    @Mock
    private CharacterService travellerCharacterService;

    @Mock
    private CaptainLogService captainLogService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TravellerTo4XIntegrationService integrationService;

    private UnifiedCharacterCreationService service;

    @BeforeEach
    void setUp() {
        service = new UnifiedCharacterCreationService(
                travellerCharacterService,
                captainLogService,
                playerRepository,
                userRepository,
                passwordEncoder,
                integrationService
        );
    }

    // ---- Available games and types ----

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
        assertTrue(types.contains(TravellerCharacterType.SECTOR_GOVERNOR));
        assertTrue(types.contains(TravellerCharacterType.SUBSECTOR_CHIEF));
        assertTrue(types.contains(TravellerCharacterType.SYSTEM_MANAGER));
    }

    // ---- Validation ----

    @Test
    @DisplayName("createCharacter should reject invalid request")
    void testCreateCharacterInvalidRequest() {
        CreateCharacterRequest request = new CreateCharacterRequest();

        assertThrows(IllegalArgumentException.class, () -> service.createCharacter(request));
    }

    @Test
    @DisplayName("createCharacter should reject request with missing game type")
    void testCreateCharacterMissingGameType() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("test@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.createCharacter(request));
    }

    @Test
    @DisplayName("createCharacter should reject request with missing name")
    void testCreateCharacterMissingName() {
        CreateCharacterRequest request = new CreateCharacterRequest();
        request.setGameType(GameType.TRAVELLER);
        request.setEmail("test@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.createCharacter(request));
    }

    @Test
    @DisplayName("createCharacter should reject request with missing email")
    void testCreateCharacterMissingEmail() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "John", "Doe", null);

        assertThrows(IllegalArgumentException.class, () -> service.createCharacter(request));
    }

    // ---- Traveller character creation ----

    @Test
    @DisplayName("createCharacter should create full Traveller character")
    void testCreateFullTravellerCharacter() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(travellerCharacterService.createCharacter("Alex Starborn"))
                .thenReturn(travellerChar);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary);
        assertEquals(GameType.TRAVELLER, summary.getGameType());
        assertEquals("Full Traveller Character", summary.getGameRole());
        assertEquals("Alex Starborn", summary.getFullName());
        assertTrue(summary.isAlive());
        verify(travellerCharacterService).createCharacter("Alex Starborn");
    }

    @Test
    @DisplayName("createCharacter should create Traveller management role")
    void testCreateTravellerManagementRole() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.EMPIRE_LEADER);

        UnifiedCharacterSummary mockSummary = new UnifiedCharacterSummary();
        mockSummary.setId(1L);
        mockSummary.setFullName("Alex Starborn");
        mockSummary.setGameType(GameType.TRAVELLER);
        mockSummary.setGameRole("Empire Leader");
        mockSummary.setAlive(true);
        when(integrationService.integrateManagementRole(request)).thenReturn(mockSummary);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary);
        assertEquals(GameType.TRAVELLER, summary.getGameType());
        assertEquals("Empire Leader", summary.getGameRole());
        assertTrue(summary.isAlive());
        verify(integrationService).integrateManagementRole(request);
    }

    @Test
    @DisplayName("createCharacter should default to FULL_TRAVELLER when type not specified")
    void testCreateTravellerDefaultType() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        // travellerCharacterType not set

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(travellerCharacterService.createCharacter("Alex Starborn"))
                .thenReturn(travellerChar);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary);
        assertEquals("Full Traveller Character", summary.getGameRole());
    }

    @Test
    @DisplayName("createCharacter should create Sector Governor role")
    void testCreateSectorGovernor() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.SECTOR_GOVERNOR);

        UnifiedCharacterSummary mockSummary = new UnifiedCharacterSummary();
        mockSummary.setGameRole("Sector Governor");
        when(integrationService.integrateManagementRole(request)).thenReturn(mockSummary);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertEquals("Sector Governor", summary.getGameRole());
    }

    @Test
    @DisplayName("createCharacter should create Subsector Chief role")
    void testCreateSubsectorChief() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.SUBSECTOR_CHIEF);

        UnifiedCharacterSummary mockSummary = new UnifiedCharacterSummary();
        mockSummary.setGameRole("Subsector Chief");
        when(integrationService.integrateManagementRole(request)).thenReturn(mockSummary);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertEquals("Subsector Chief", summary.getGameRole());
    }

    @Test
    @DisplayName("createCharacter should create System Manager role")
    void testCreateSystemManager() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.SYSTEM_MANAGER);

        UnifiedCharacterSummary mockSummary = new UnifiedCharacterSummary();
        mockSummary.setGameRole("System Manager");
        when(integrationService.integrateManagementRole(request)).thenReturn(mockSummary);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertEquals("System Manager", summary.getGameRole());
    }

    // ---- Captain's Log character creation ----

    @Test
    @DisplayName("createCharacter should create Captain's Log character")
    void testCreateCaptainsLogCharacter() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.CAPTAINS_LOG, "James", "Kirk", "kirk@starfleet.com");
        request.setPronouns("he/him");
        request.setTrait1("Bold");
        request.setTrait2("Reckless");
        request.setTrait3("Leader");

        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(1L);
        clChar.setName("James Kirk");
        clChar.setRank(Rank.ENSIGN);
        clChar.setAssignment(Assignment.COMMAND);
        clChar.setEnvironment(Environment.CITY);
        when(captainLogService.createCharacter(any(com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest.class)))
                .thenReturn(clChar);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary);
        assertEquals(GameType.CAPTAINS_LOG, summary.getGameType());
        assertEquals("James Kirk", summary.getFullName());
        assertTrue(summary.isAlive());
        verify(captainLogService).createCharacter(any(com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest.class));
    }

    @Test
    @DisplayName("createCharacter should pass pronouns and traits to Captain's Log")
    void testCreateCaptainsLogWithTraits() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.CAPTAINS_LOG, "James", "Kirk", "kirk@starfleet.com");
        request.setPronouns("he/him");
        request.setTrait1("Bold");
        request.setTrait2("Reckless");
        request.setTrait3("Leader");

        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(1L);
        clChar.setName("James Kirk");
        clChar.setRank(Rank.ENSIGN);
        clChar.setAssignment(Assignment.COMMAND);
        clChar.setEnvironment(Environment.CITY);
        when(captainLogService.createCharacter(any(com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest.class)))
                .thenReturn(clChar);

        service.createCharacter(request);

        verify(captainLogService).createCharacter(argThat(clRequest ->
                "he/him".equals(clRequest.getPronouns()) &&
                "Bold".equals(clRequest.getTrait1()) &&
                "Reckless".equals(clRequest.getTrait2()) &&
                "Leader".equals(clRequest.getTrait3())
        ));
    }

    // ---- DoraMCU 4X character creation ----

    @Test
    @DisplayName("createCharacter should return existing DoraMCU player")
    void testCreateDoraMCUExistingPlayer() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.DORAMCU_4X, "Alex", "Starborn", "test@example.com");

        Player existingPlayer = new Player();
        existingPlayer.setId(1L);
        existingPlayer.setEmpireName("Existing Empire");
        existingPlayer.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        existingPlayer.setEthos(Player.Ethos.MATERIALIST);

        when(playerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingPlayer));

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary);
        assertEquals(GameType.DORAMCU_4X, summary.getGameType());
        assertEquals("Empire Ruler", summary.getGameRole());
        assertEquals("Existing Empire", summary.getFullName());
    }

    @Test
    @DisplayName("createCharacter should create new DoraMCU player")
    void testCreateDoraMCUNewPlayer() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.DORAMCU_4X, "Alex", "Starborn", "test@example.com");

        User user = new User();
        user.setUsername("test@example.com");

        Player savedPlayer = new Player();
        savedPlayer.setId(1L);
        savedPlayer.setEmpireName("Alex Starborn Empire");
        savedPlayer.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        savedPlayer.setEthos(Player.Ethos.MATERIALIST);

        when(playerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername("test@example.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(user));
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(playerRepository.existsByLoginPin(anyString())).thenReturn(false);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary);
        assertEquals(GameType.DORAMCU_4X, summary.getGameType());
        assertEquals("Empire Ruler", summary.getGameRole());
        assertEquals("Alex Starborn Empire", summary.getFullName());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("createCharacter should use existing user for DoraMCU player")
    void testCreateDoraMCUWithExistingUser() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.DORAMCU_4X, "Alex", "Starborn", "test@example.com");

        User existingUser = new User();
        existingUser.setUsername("test@example.com");

        Player savedPlayer = new Player();
        savedPlayer.setId(1L);
        savedPlayer.setEmpireName("Alex Starborn Empire");
        savedPlayer.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        savedPlayer.setEthos(Player.Ethos.MATERIALIST);

        when(playerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(playerRepository.existsByLoginPin(anyString())).thenReturn(false);

        service.createCharacter(request);

        verify(userRepository, never()).save(any(User.class));
    }

    // ---- Edge cases ----

    @Test
    @DisplayName("createCharacter should include ability scores in Traveller description")
    void testCreateTravellerDescriptionIncludesAbilities() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(travellerCharacterService.createCharacter("Alex Starborn"))
                .thenReturn(travellerChar);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertNotNull(summary.getDescription());
        assertTrue(summary.getDescription().contains("DEX"));
        assertTrue(summary.getDescription().contains("ST"));
        assertTrue(summary.getDescription().contains("END"));
        assertTrue(summary.getDescription().contains("INT"));
        assertTrue(summary.getDescription().contains("EDU"));
        assertTrue(summary.getDescription().contains("SOC"));
        assertTrue(summary.getDescription().contains("APP"));
    }

    @Test
    @DisplayName("createCharacter should set first and last name in summary")
    void testCreateCharacterSetsNames() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);

        TravellerCharacter travellerChar = new TravellerCharacter(1, "Alex Starborn");
        travellerChar.rollAbilities();
        when(travellerCharacterService.createCharacter("Alex Starborn"))
                .thenReturn(travellerChar);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertEquals("Alex", summary.getFirstName());
        assertEquals("Starborn", summary.getLastName());
    }

    @Test
    @DisplayName("createCharacter should set ID from created character")
    void testCreateCharacterSetsId() {
        CreateCharacterRequest request = new CreateCharacterRequest(
                GameType.TRAVELLER, "Alex", "Starborn", "test@example.com");
        request.setTravellerCharacterType(TravellerCharacterType.FULL_TRAVELLER);

        TravellerCharacter travellerChar = new TravellerCharacter(42, "Alex Starborn");
        travellerChar.rollAbilities();
        when(travellerCharacterService.createCharacter("Alex Starborn"))
                .thenReturn(travellerChar);

        UnifiedCharacterSummary summary = service.createCharacter(request);

        assertEquals(42L, summary.getId());
    }
}
