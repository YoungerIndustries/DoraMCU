package com.yisy.modules.doramcu.shared.service;

import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogFocus;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.shared.dto.CreateSharedCharacterRequest;
import com.yisy.modules.doramcu.shared.dto.TravelCharacterRequest;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.model.SharedCharacterSkill;
import com.yisy.modules.doramcu.shared.model.SharedCharacterTravelLog;
import com.yisy.modules.doramcu.shared.repository.SharedCharacterRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SharedCharacterService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class SharedCharacterServiceTest {

    @Mock
    private SharedCharacterRepository sharedCharacterRepository;

    @Mock
    private CaptainLogCharacterRepository captainLogRepository;

    @Mock
    private CaptainLogService captainLogService;

    @Mock
    private CharacterService travellerCharacterService;

    @Mock
    private PlayerRepository playerRepository;

    private SharedCharacterService sharedCharacterService;

    @BeforeEach
    void setUp() {
        sharedCharacterService = new SharedCharacterService(
                sharedCharacterRepository,
                captainLogRepository,
                captainLogService,
                travellerCharacterService,
                playerRepository
        );
    }

    // ==================== createSharedCharacter ====================

    @Test
    @DisplayName("createSharedCharacter - should create character with explicit attributes for CAPTAINS_LOG")
    void createSharedCharacter_withExplicitAttributes_captainsLog() {
        CreateSharedCharacterRequest request = new CreateSharedCharacterRequest();
        request.setName("John Doe");
        request.setOriginSystem(GameType.CAPTAINS_LOG);
        request.setPronouns("he/him");
        request.setDescription("A brave explorer");
        request.setAge(25);
        request.setRace("Human");
        request.setNotes("Test character");
        request.setDexterity(8);
        request.setTechnical(6);
        request.setStrength(7);
        request.setIntelligence(9);
        request.setWisdom(5);
        request.setCharisma(10);
        request.setConstitution(6);

        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(100L);

        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> {
            SharedCharacter sc = invocation.getArgument(0);
            sc.setId(1L);
            return sc;
        });
        when(captainLogService.createCharacter(any(CreateCharacterRequest.class))).thenReturn(clChar);
        when(captainLogRepository.save(any(CaptainLogCharacter.class))).thenReturn(clChar);

        SharedCharacter result = sharedCharacterService.createSharedCharacter(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals(GameType.CAPTAINS_LOG, result.getOriginSystem());
        assertEquals("he/him", result.getPronouns());
        assertEquals("A brave explorer", result.getDescription());
        assertEquals(25, result.getAge());
        assertEquals("Human", result.getRace());
        assertEquals("Test character", result.getNotes());
        assertEquals(8, result.getDexterity());
        assertEquals(6, result.getTechnical());
        assertEquals(7, result.getStrength());
        assertEquals(9, result.getIntelligence());
        assertEquals(5, result.getWisdom());
        assertEquals(10, result.getCharisma());
        assertEquals(6, result.getConstitution());
        assertEquals(100L, result.getClCharacterId());
        verify(sharedCharacterRepository, times(2)).save(any(SharedCharacter.class));
    }

    @Test
    @DisplayName("createSharedCharacter - should create character with explicit attributes for TRAVELLER")
    void createSharedCharacter_withExplicitAttributes_traveller() {
        CreateSharedCharacterRequest request = new CreateSharedCharacterRequest();
        request.setName("Jane Smith");
        request.setOriginSystem(GameType.TRAVELLER);
        request.setDexterity(8);
        request.setTechnical(6);
        request.setStrength(7);
        request.setIntelligence(9);
        request.setWisdom(5);
        request.setCharisma(10);
        request.setConstitution(6);

        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> {
            SharedCharacter sc = invocation.getArgument(0);
            sc.setId(2L);
            return sc;
        });

        TravellerCharacter tChar = new TravellerCharacter(200, "Jane Smith");
        when(travellerCharacterService.createCharacter(eq("Jane Smith"), anyMap())).thenReturn(tChar);

        SharedCharacter result = sharedCharacterService.createSharedCharacter(request);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Jane Smith", result.getName());
        assertEquals(GameType.TRAVELLER, result.getOriginSystem());
        assertEquals(200, result.getTravellerCharacterId());
        verify(travellerCharacterService).createCharacter(eq("Jane Smith"), anyMap());
    }

    @Test
    @DisplayName("createSharedCharacter - should auto-roll attributes when not provided")
    void createSharedCharacter_autoRollsAttributes() {
        CreateSharedCharacterRequest request = new CreateSharedCharacterRequest();
        request.setName("Auto Roll Character");
        request.setOriginSystem(GameType.CAPTAINS_LOG);

        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(101L);

        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> {
            SharedCharacter sc = invocation.getArgument(0);
            sc.setId(3L);
            return sc;
        });
        when(captainLogService.createCharacter(any(CreateCharacterRequest.class))).thenReturn(clChar);
        when(captainLogRepository.save(any(CaptainLogCharacter.class))).thenReturn(clChar);

        SharedCharacter result = sharedCharacterService.createSharedCharacter(request);

        assertNotNull(result);
        assertEquals("Auto Roll Character", result.getName());
        // All auto-rolled attributes should be in range 2-12
        assertTrue(result.getDexterity() >= 2 && result.getDexterity() <= 12, "Dexterity should be 2-12");
        assertTrue(result.getTechnical() >= 2 && result.getTechnical() <= 12, "Technical should be 2-12");
        assertTrue(result.getStrength() >= 2 && result.getStrength() <= 12, "Strength should be 2-12");
        assertTrue(result.getIntelligence() >= 2 && result.getIntelligence() <= 12, "Intelligence should be 2-12");
        assertTrue(result.getWisdom() >= 2 && result.getWisdom() <= 12, "Wisdom should be 2-12");
        assertTrue(result.getCharisma() >= 2 && result.getCharisma() <= 12, "Charisma should be 2-12");
        // Constitution should be calculated as (STR + WIS) / 2
        assertEquals((result.getStrength() + result.getWisdom()) / 2, result.getConstitution());
        // Default age should be 18
        assertEquals(18, result.getAge());
    }

    // ==================== importFromCaptainLog ====================

    @Test
    @DisplayName("importFromCaptainLog - should create shared character from existing CL character")
    void importFromCaptainLog_success() {
        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(50L);
        clChar.setName("CL Import Character");
        clChar.setPronouns("she/her");
        clChar.setFitness(7);
        clChar.setTechnical(8);
        clChar.setStrength(6);
        clChar.setReason(9);
        clChar.setInsight(5);
        clChar.setPresence(10);
        clChar.setConstitution(5);
        clChar.setFocuses(new ArrayList<>());

        CaptainLogFocus focus = new CaptainLogFocus(CaptainLogFocus.Discipline.SECURITY, "Starship Tactics");
        clChar.getFocuses().add(focus);

        when(captainLogService.getCharacter(50L)).thenReturn(Optional.of(clChar));
        when(sharedCharacterRepository.findByClCharacterId(50L)).thenReturn(Optional.empty());
        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> {
            SharedCharacter sc = invocation.getArgument(0);
            sc.setId(10L);
            return sc;
        });

        SharedCharacter result = sharedCharacterService.importFromCaptainLog(50L);

        assertNotNull(result);
        assertEquals("CL Import Character", result.getName());
        assertEquals(GameType.CAPTAINS_LOG, result.getOriginSystem());
        assertEquals(50L, result.getClCharacterId());
        assertEquals(7, result.getDexterity());
        assertEquals(8, result.getTechnical());
        assertEquals(6, result.getStrength());
        assertEquals(9, result.getIntelligence());
        assertEquals(5, result.getWisdom());
        assertEquals(10, result.getCharisma());
        assertEquals(5, result.getConstitution());
        assertEquals("she/her", result.getPronouns());
        // Should have imported the focus as a skill
        assertEquals(1, result.getSkills().size());
        assertEquals("Starship Tactics", result.getSkills().get(0).getSkillName());
        assertEquals("CAPTAINS_LOG", result.getSkills().get(0).getSystemSource());
    }

    @Test
    @DisplayName("importFromCaptainLog - should throw when CL character not found")
    void importFromCaptainLog_notFound() {
        when(captainLogService.getCharacter(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sharedCharacterService.importFromCaptainLog(999L)
        );
        assertTrue(ex.getMessage().contains("Captain's Log character not found"));
        verify(sharedCharacterRepository, never()).save(any());
    }

    @Test
    @DisplayName("importFromCaptainLog - should return existing shared character if already imported")
    void importFromCaptainLog_alreadyImported() {
        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(50L);
        clChar.setName("Already Imported");
        clChar.setFocuses(new ArrayList<>());

        SharedCharacter existing = new SharedCharacter("Already Imported", GameType.CAPTAINS_LOG);
        existing.setId(10L);
        existing.setClCharacterId(50L);

        when(captainLogService.getCharacter(50L)).thenReturn(Optional.of(clChar));
        when(sharedCharacterRepository.findByClCharacterId(50L)).thenReturn(Optional.of(existing));

        SharedCharacter result = sharedCharacterService.importFromCaptainLog(50L);

        assertEquals(existing, result);
        assertEquals(10L, result.getId());
        verify(sharedCharacterRepository, never()).save(any());
    }

    // ==================== importFromTraveller ====================

    @Test
    @DisplayName("importFromTraveller - should create shared character from existing Traveller character")
    void importFromTraveller_success() {
        TravellerCharacter tChar = new TravellerCharacter(300, "Traveller Import");
        tChar.setAbility(AbilityScore.DEX, 8);
        tChar.setAbility(AbilityScore.ST, 7);
        tChar.setAbility(AbilityScore.END, 6);
        tChar.setAbility(AbilityScore.INT, 9);
        tChar.setAbility(AbilityScore.EDU, 5);
        tChar.setAbility(AbilityScore.SOC, 10);
        tChar.setAbility(AbilityScore.APP, 6);

        // Add skills via career tour (the only way to add skills to TravellerCharacter)
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                com.yisy.modules.doramcu.traveller.Career.PILOT,
                4,
                List.of(Skill.NAVIGATION),
                "Pilot Officer",
                10000L,
                false,
                TravellerCharacter.RetirementType.NORMAL
        );
        tChar.addCareerTour(tour);

        when(travellerCharacterService.getCharacter(300)).thenReturn(tChar);
        when(sharedCharacterRepository.findByTravellerCharacterId(300)).thenReturn(Optional.empty());
        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> {
            SharedCharacter sc = invocation.getArgument(0);
            sc.setId(20L);
            return sc;
        });

        SharedCharacter result = sharedCharacterService.importFromTraveller(300);

        assertNotNull(result);
        assertEquals("Traveller Import", result.getName());
        assertEquals(GameType.TRAVELLER, result.getOriginSystem());
        assertEquals(300, result.getTravellerCharacterId());
        // Attribute mapping: DEX->Dex, ST->Str, END->Con, INT->Int, EDU->Tech, SOC->Wis, APP->Cha
        assertEquals(8, result.getDexterity());
        assertEquals(7, result.getStrength());
        assertEquals(6, result.getConstitution());
        assertEquals(9, result.getIntelligence());
        assertEquals(5, result.getTechnical());
        assertEquals(10, result.getWisdom());
        assertEquals(6, result.getCharisma());
        // Age = 18 (start) + 4 (tour) = 22
        assertEquals(22, result.getAge());
        // Should have imported the skill
        assertEquals(1, result.getSkills().size());
        assertEquals("NAVIGATION", result.getSkills().get(0).getSkillName());
        assertEquals("TRAVELLER", result.getSkills().get(0).getSystemSource());
    }

    @Test
    @DisplayName("importFromTraveller - should throw when Traveller character not found")
    void importFromTraveller_notFound() {
        when(travellerCharacterService.getCharacter(999)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sharedCharacterService.importFromTraveller(999)
        );
        assertTrue(ex.getMessage().contains("Traveller character not found"));
        verify(sharedCharacterRepository, never()).save(any());
    }

    @Test
    @DisplayName("importFromTraveller - should return existing shared character if already imported")
    void importFromTraveller_alreadyImported() {
        TravellerCharacter tChar = new TravellerCharacter(300, "Already Imported Traveller");

        SharedCharacter existing = new SharedCharacter("Already Imported Traveller", GameType.TRAVELLER);
        existing.setId(20L);
        existing.setTravellerCharacterId(300);

        when(travellerCharacterService.getCharacter(300)).thenReturn(tChar);
        when(sharedCharacterRepository.findByTravellerCharacterId(300)).thenReturn(Optional.of(existing));

        SharedCharacter result = sharedCharacterService.importFromTraveller(300);

        assertEquals(existing, result);
        assertEquals(20L, result.getId());
        verify(sharedCharacterRepository, never()).save(any());
    }

    // ==================== travelCharacter ====================

    @Test
    @DisplayName("travelCharacter - should travel from TRAVELLER to CAPTAINS_LOG")
    void travelCharacter_travellerToCaptainsLog() {
        SharedCharacter shared = new SharedCharacter("Traveler", GameType.TRAVELLER);
        shared.setId(30L);
        shared.setTravellerCharacterId(400);
        shared.setDexterity(8);
        shared.setTechnical(6);
        shared.setStrength(7);
        shared.setIntelligence(9);
        shared.setWisdom(5);
        shared.setCharisma(10);

        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.CAPTAINS_LOG);
        request.setTravelNotes("First contact mission");

        CaptainLogCharacter clChar = new CaptainLogCharacter();
        clChar.setId(200L);

        when(sharedCharacterRepository.findById(30L)).thenReturn(Optional.of(shared));
        when(captainLogService.createCharacter(any(CreateCharacterRequest.class))).thenReturn(clChar);
        when(captainLogRepository.save(any(CaptainLogCharacter.class))).thenReturn(clChar);
        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedCharacter result = sharedCharacterService.travelCharacter(30L, request);

        assertNotNull(result);
        assertEquals(GameType.CAPTAINS_LOG, result.getOriginSystem());
        assertEquals(200L, result.getClCharacterId());
        assertEquals(1, result.getTravelLogs().size());
        SharedCharacterTravelLog log = result.getTravelLogs().get(0);
        assertEquals("TRAVELLER", log.getFromSystem());
        assertEquals("CAPTAINS_LOG", log.getToSystem());
        assertEquals("First contact mission", log.getTravelNotes());
    }

    @Test
    @DisplayName("travelCharacter - should travel from CAPTAINS_LOG to TRAVELLER")
    void travelCharacter_captainsLogToTraveller() {
        SharedCharacter shared = new SharedCharacter("Traveler", GameType.CAPTAINS_LOG);
        shared.setId(31L);
        shared.setClCharacterId(200L);
        shared.setDexterity(8);
        shared.setTechnical(6);
        shared.setStrength(7);
        shared.setIntelligence(9);
        shared.setWisdom(5);
        shared.setCharisma(10);
        shared.setConstitution(6);

        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.TRAVELLER);
        request.setTravelNotes("Exploring the Spin");

        TravellerCharacter tChar = new TravellerCharacter(500, "Traveler");

        when(sharedCharacterRepository.findById(31L)).thenReturn(Optional.of(shared));
        when(travellerCharacterService.createCharacter(eq("Traveler"), anyMap())).thenReturn(tChar);
        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedCharacter result = sharedCharacterService.travelCharacter(31L, request);

        assertNotNull(result);
        assertEquals(GameType.TRAVELLER, result.getOriginSystem());
        assertEquals(500, result.getTravellerCharacterId());
        assertEquals(1, result.getTravelLogs().size());
        SharedCharacterTravelLog log = result.getTravelLogs().get(0);
        assertEquals("CAPTAINS_LOG", log.getFromSystem());
        assertEquals("TRAVELLER", log.getToSystem());
        assertEquals("Exploring the Spin", log.getTravelNotes());
    }

    @Test
    @DisplayName("travelCharacter - should throw when target system equals current system")
    void travelCharacter_sameSystem() {
        SharedCharacter shared = new SharedCharacter("Stayer", GameType.CAPTAINS_LOG);
        shared.setId(32L);

        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.CAPTAINS_LOG);

        when(sharedCharacterRepository.findById(32L)).thenReturn(Optional.of(shared));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sharedCharacterService.travelCharacter(32L, request)
        );
        assertTrue(ex.getMessage().contains("already in system"));
    }

    @Test
    @DisplayName("travelCharacter - should throw when shared character not found")
    void travelCharacter_notFound() {
        TravelCharacterRequest request = new TravelCharacterRequest();
        request.setTargetSystem(GameType.TRAVELLER);

        when(sharedCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sharedCharacterService.travelCharacter(999L, request)
        );
        assertTrue(ex.getMessage().contains("Shared character not found"));
    }

    // ==================== Queries ====================

    @Test
    @DisplayName("getSharedCharacter - should return character when found")
    void getSharedCharacter_found() {
        SharedCharacter character = new SharedCharacter("Found Character", GameType.CAPTAINS_LOG);
        character.setId(40L);
        when(sharedCharacterRepository.findById(40L)).thenReturn(Optional.of(character));

        Optional<SharedCharacter> result = sharedCharacterService.getSharedCharacter(40L);

        assertTrue(result.isPresent());
        assertEquals("Found Character", result.get().getName());
    }

    @Test
    @DisplayName("getSharedCharacter - should return empty when not found")
    void getSharedCharacter_notFound() {
        when(sharedCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<SharedCharacter> result = sharedCharacterService.getSharedCharacter(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("listSharedCharacters - should return all shared characters")
    void listSharedCharacters_returnsAll() {
        SharedCharacter c1 = new SharedCharacter("Char 1", GameType.CAPTAINS_LOG);
        c1.setId(1L);
        SharedCharacter c2 = new SharedCharacter("Char 2", GameType.TRAVELLER);
        c2.setId(2L);
        when(sharedCharacterRepository.findAll()).thenReturn(List.of(c1, c2));

        List<SharedCharacter> result = sharedCharacterService.listSharedCharacters();

        assertEquals(2, result.size());
        assertEquals("Char 1", result.get(0).getName());
        assertEquals("Char 2", result.get(1).getName());
    }

    @Test
    @DisplayName("listSharedCharacters - should return empty list when no characters")
    void listSharedCharacters_empty() {
        when(sharedCharacterRepository.findAll()).thenReturn(Collections.emptyList());

        List<SharedCharacter> result = sharedCharacterService.listSharedCharacters();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("listBySystem - should filter by system type")
    void listBySystem_filtersCorrectly() {
        SharedCharacter c1 = new SharedCharacter("CL Char", GameType.CAPTAINS_LOG);
        c1.setId(1L);
        SharedCharacter c2 = new SharedCharacter("T Char", GameType.TRAVELLER);
        c2.setId(2L);
        when(sharedCharacterRepository.findByOriginSystem(GameType.CAPTAINS_LOG))
                .thenReturn(List.of(c1));

        List<SharedCharacter> result = sharedCharacterService.listBySystem(GameType.CAPTAINS_LOG);

        assertEquals(1, result.size());
        assertEquals("CL Char", result.get(0).getName());
        assertEquals(GameType.CAPTAINS_LOG, result.get(0).getOriginSystem());
    }

    @Test
    @DisplayName("searchByName - should return characters matching name (case insensitive)")
    void searchByName_matchesPartial() {
        SharedCharacter c1 = new SharedCharacter("John Doe", GameType.CAPTAINS_LOG);
        c1.setId(1L);
        SharedCharacter c2 = new SharedCharacter("John Smith", GameType.TRAVELLER);
        c2.setId(2L);
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("john")).thenReturn(List.of(c1, c2));

        List<SharedCharacter> result = sharedCharacterService.searchByName("john");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("John Doe")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("John Smith")));
    }

    @Test
    @DisplayName("searchByName - should return empty list when no matches")
    void searchByName_noMatches() {
        when(sharedCharacterRepository.findByNameContainingIgnoreCase("nonexistent"))
                .thenReturn(Collections.emptyList());

        List<SharedCharacter> result = sharedCharacterService.searchByName("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTravelHistory - should return travel logs for character")
    void getTravelHistory_returnsLogs() {
        SharedCharacter shared = new SharedCharacter("Traveler", GameType.CAPTAINS_LOG);
        shared.setId(50L);

        SharedCharacterTravelLog log1 = new SharedCharacterTravelLog(shared, "TRAVELLER", "CAPTAINS_LOG");
        log1.setId(1L);
        SharedCharacterTravelLog log2 = new SharedCharacterTravelLog(shared, "CAPTAINS_LOG", "TRAVELLER");
        log2.setId(2L);
        shared.addTravelLog(log1);
        shared.addTravelLog(log2);

        when(sharedCharacterRepository.findById(50L)).thenReturn(Optional.of(shared));

        List<SharedCharacterTravelLog> result = sharedCharacterService.getTravelHistory(50L);

        assertEquals(2, result.size());
        assertEquals("TRAVELLER", result.get(0).getFromSystem());
        assertEquals("CAPTAINS_LOG", result.get(0).getToSystem());
    }

    @Test
    @DisplayName("getTravelHistory - should throw when character not found")
    void getTravelHistory_notFound() {
        when(sharedCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sharedCharacterService.getTravelHistory(999L)
        );
        assertTrue(ex.getMessage().contains("Shared character not found"));
    }

    // ==================== addSkill ====================

    @Test
    @DisplayName("addSkill - should add skill to shared character")
    void addSkill_success() {
        SharedCharacter shared = new SharedCharacter("Skill Character", GameType.CAPTAINS_LOG);
        shared.setId(60L);

        when(sharedCharacterRepository.findById(60L)).thenReturn(Optional.of(shared));
        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedCharacterSkill result = sharedCharacterService.addSkill(60L, "Pistol", 2, "TRAVELLER");

        assertNotNull(result);
        assertEquals("Pistol", result.getSkillName());
        assertEquals(2, result.getSkillLevel());
        assertEquals("TRAVELLER", result.getSystemSource());
        assertEquals(1, shared.getSkills().size());
        verify(sharedCharacterRepository).save(shared);
    }

    @Test
    @DisplayName("addSkill - should throw when character not found")
    void addSkill_notFound() {
        when(sharedCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sharedCharacterService.addSkill(999L, "Pistol", 1, "TRAVELLER")
        );
        assertTrue(ex.getMessage().contains("Shared character not found"));
    }

    @Test
    @DisplayName("addSkill - should add multiple skills to same character")
    void addSkill_multipleSkills() {
        SharedCharacter shared = new SharedCharacter("Multi Skill Character", GameType.TRAVELLER);
        shared.setId(61L);

        when(sharedCharacterRepository.findById(61L)).thenReturn(Optional.of(shared));
        when(sharedCharacterRepository.save(any(SharedCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sharedCharacterService.addSkill(61L, "Pistol", 2, "TRAVELLER");
        sharedCharacterService.addSkill(61L, "Starship Tactics", 1, "CAPTAINS_LOG");
        sharedCharacterService.addSkill(61L, "Navigation", 3, "TRAVELLER");

        assertEquals(3, shared.getSkills().size());
        assertEquals("Pistol", shared.getSkills().get(0).getSkillName());
        assertEquals("Starship Tactics", shared.getSkills().get(1).getSkillName());
        assertEquals("Navigation", shared.getSkills().get(2).getSkillName());
    }
}
