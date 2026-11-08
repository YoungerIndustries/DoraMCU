package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.dto.UnifiedCharacterSummary;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourOfDutyService Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class TourOfDutyServiceTest {

    private static final int MAX_CAREER_TOURS = 4;

    @Mock
    private CharacterService characterService;

    @Mock
    private MilitaryTourService militaryTourService;

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private Game game;

    @Mock
    private com.yisy.modules.doramcu.model.GameSession gameSession;

    private TourOfDutyService service;
    private TravellerCharacter character;
    private CharacterLogManager logManager;

    @BeforeEach
    void setUp() {
        character = new TravellerCharacter(1, "Alex Starborn");
        character.rollAbilities();
        logManager = new CharacterLogManager(game);
        service = new TourOfDutyService(characterService, militaryTourService, gameSessionService);
    }

    // ---- Tour Options ----

    @Test
    @DisplayName("getTourOptions should return 4 options for a new character")
    void testGetTourOptionsNewCharacter() {
        when(characterService.getCharacter(1)).thenReturn(character);

        List<TourOfDutyOption> options = service.getTourOptions(1);

        assertEquals(4, options.size());
        assertEquals(1, options.get(0).getTermNumber());
        assertEquals(4, options.get(3).getTermNumber());
    }

    @Test
    @DisplayName("getTourOptions should return remaining options after served tours")
    void testGetTourOptionsAfterServedTours() {
        TravellerCharacter charWithTours = new TravellerCharacter(2, "Veteran");
        charWithTours.rollAbilities();
        // Add 2 served tours
        charWithTours.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, new ArrayList<>(), "Enlisted", 5000, false,
                TravellerCharacter.RetirementType.NORMAL));
        charWithTours.addCareerTour(new TravellerCharacter.CareerTour(
                Career.GUNNER, 4, new ArrayList<>(), "NCO", 6000, false,
                TravellerCharacter.RetirementType.NORMAL));

        when(characterService.getCharacter(2)).thenReturn(charWithTours);

        List<TourOfDutyOption> options = service.getTourOptions(2);

        assertEquals(2, options.size());
        assertEquals(3, options.get(0).getTermNumber());
        assertEquals(4, options.get(1).getTermNumber());
    }

    @Test
    @DisplayName("getTourOptions should return empty for dead character")
    void testGetTourOptionsDeadCharacter() {
        TravellerCharacter dead = new TravellerCharacter(3, "Dead Char");
        dead.rollAbilities();
        dead.setAlive(false);
        when(characterService.getCharacter(3)).thenReturn(dead);

        List<TourOfDutyOption> options = service.getTourOptions(3);

        assertTrue(options.isEmpty());
    }

    @Test
    @DisplayName("getTourOptions should return empty for ON_DUTY character")
    void testGetTourOptionsOnDutyCharacter() {
        character.setStatus(CharacterStatus.ON_DUTY);
        when(characterService.getCharacter(1)).thenReturn(character);

        List<TourOfDutyOption> options = service.getTourOptions(1);

        assertTrue(options.isEmpty());
    }

    @Test
    @DisplayName("getTourOptions should return empty for COMPLETED character")
    void testGetTourOptionsCompletedCharacter() {
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        List<TourOfDutyOption> options = service.getTourOptions(1);

        assertTrue(options.isEmpty());
    }

    @Test
    @DisplayName("getTourOptions should throw for unknown character")
    void testGetTourOptionsUnknownCharacter() {
        when(characterService.getCharacter(999)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.getTourOptions(999));
    }

    @Test
    @DisplayName("getTourOptions should set COMPLETED status when max tours served")
    void testGetTourOptionsSetsCompletedStatus() {
        TravellerCharacter maxed = new TravellerCharacter(4, "Maxed Out");
        maxed.rollAbilities();
        for (int i = 0; i < MAX_CAREER_TOURS; i++) {
            maxed.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Enlisted", 5000, false,
                    TravellerCharacter.RetirementType.NORMAL));
        }

        when(characterService.getCharacter(4)).thenReturn(maxed);

        List<TourOfDutyOption> options = service.getTourOptions(4);

        assertTrue(options.isEmpty());
        assertEquals(CharacterStatus.COMPLETED, maxed.getStatus());
    }

    // ---- Immediate Resolution ----

    @Test
    @DisplayName("resolveTourImmediately should serve a career tour")
    void testResolveTourImmediately() {
        TravellerCharacter.CareerTour mockTour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, new ArrayList<>(), "Enlisted", 5000, false,
                TravellerCharacter.RetirementType.NORMAL);

        when(characterService.getCharacter(1)).thenReturn(character);
        when(characterService.serveCareerTour(1, Career.SOLDIER)).thenReturn(mockTour);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        TravellerCharacter.CareerTour result = service.resolveTourImmediately(request);

        assertSame(mockTour, result);
        assertEquals(Career.SOLDIER, result.getCareer());
    }

    @Test
    @DisplayName("resolveTourImmediately should keep TRAINING status when tours remain")
    void testResolveTourImmediatelyKeepsTraining() {
        TravellerCharacter.CareerTour mockTour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, new ArrayList<>(), "Enlisted", 5000, false,
                TravellerCharacter.RetirementType.NORMAL);

        when(characterService.getCharacter(1)).thenReturn(character);
        when(characterService.serveCareerTour(1, Career.SOLDIER)).thenReturn(mockTour);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        service.resolveTourImmediately(request);

        assertEquals(CharacterStatus.TRAINING, character.getStatus());
    }

    @Test
    @DisplayName("resolveTourImmediately should set COMPLETED when retired")
    void testResolveTourImmediatelySetsCompletedOnRetired() {
        TravellerCharacter.CareerTour mockTour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, new ArrayList<>(), "Enlisted", 5000, true,
                TravellerCharacter.RetirementType.NORMAL);

        when(characterService.getCharacter(1)).thenReturn(character);
        when(characterService.serveCareerTour(1, Career.SOLDIER)).thenReturn(mockTour);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        service.resolveTourImmediately(request);

        assertEquals(CharacterStatus.COMPLETED, character.getStatus());
    }

    @Test
    @DisplayName("resolveTourImmediately should throw for FOUR_X mode")
    void testResolveTourImmediatelyThrowsForFourX() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, 6);

        assertThrows(IllegalArgumentException.class, () -> service.resolveTourImmediately(request));
    }

    @Test
    @DisplayName("resolveTourImmediately should throw for invalid request")
    void testResolveTourImmediatelyThrowsForInvalidRequest() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                0, TourOfDutyRequest.ResolveMode.IMMEDIATE, null, 0);

        assertThrows(IllegalArgumentException.class, () -> service.resolveTourImmediately(request));
    }

    @Test
    @DisplayName("resolveTourImmediately should throw for ON_DUTY character")
    void testResolveTourImmediatelyThrowsForOnDuty() {
        character.setStatus(CharacterStatus.ON_DUTY);
        when(characterService.getCharacter(1)).thenReturn(character);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        assertThrows(IllegalStateException.class, () -> service.resolveTourImmediately(request));
    }

    // ---- 4X Tour Start ----

    @Test
    @DisplayName("startTourIn4X should set character to ON_DUTY")
    void testStartTourIn4XSetsOnDuty() {
        when(characterService.getCharacter(1)).thenReturn(character);
        when(gameSessionService.getDefaultSession()).thenReturn(null);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, 6);

        UnifiedCharacterSummary summary = service.startTourIn4X(request);

        assertEquals(CharacterStatus.ON_DUTY, character.getStatus());
        assertEquals(CharacterStatus.ON_DUTY, summary.getCharacterStatus());
    }

    @Test
    @DisplayName("startTourIn4X should return summary with correct info")
    void testStartTourIn4XReturnsSummary() {
        when(characterService.getCharacter(1)).thenReturn(character);
        when(gameSessionService.getDefaultSession()).thenReturn(null);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, 6);

        UnifiedCharacterSummary summary = service.startTourIn4X(request);

        assertEquals(1L, summary.getId());
        assertEquals("Alex Starborn", summary.getFullName());
        assertEquals("Soldier", summary.getGameRole());
        assertTrue(summary.getDescription().contains("Soldier"));
        assertTrue(summary.isAlive());
    }

    @Test
    @DisplayName("startTourIn4X should throw for IMMEDIATE mode")
    void testStartTourIn4XThrowsForImmediate() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        assertThrows(IllegalArgumentException.class, () -> service.startTourIn4X(request));
    }

    @Test
    @DisplayName("startTourIn4X should throw for zero tour turns")
    void testStartTourIn4XThrowsForZeroTurns() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, 0);

        assertThrows(IllegalArgumentException.class, () -> service.startTourIn4X(request));
    }

    @Test
    @DisplayName("startTourIn4X should throw for negative tour turns")
    void testStartTourIn4XThrowsForNegativeTurns() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, -1);

        assertThrows(IllegalArgumentException.class, () -> service.startTourIn4X(request));
    }

    @Test
    @DisplayName("startTourIn4X should throw for ON_DUTY character")
    void testStartTourIn4XThrowsForOnDuty() {
        character.setStatus(CharacterStatus.ON_DUTY);
        when(characterService.getCharacter(1)).thenReturn(character);

        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, 6);

        assertThrows(IllegalStateException.class, () -> service.startTourIn4X(request));
    }

    // ---- Tour Completion ----

    @Test
    @DisplayName("completeTour should revert ON_DUTY to TRAINING")
    void testCompleteTourRevertsToTraining() {
        character.setStatus(CharacterStatus.ON_DUTY);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.completeTour(1);

        assertEquals(CharacterStatus.TRAINING, character.getStatus());
    }

    @Test
    @DisplayName("completeTour should skip if character not ON_DUTY")
    void testCompleteTourSkipsIfNotOnDuty() {
        character.setStatus(CharacterStatus.TRAINING);
        when(characterService.getCharacter(1)).thenReturn(character);

        service.completeTour(1);

        // Status should remain TRAINING (no change)
        assertEquals(CharacterStatus.TRAINING, character.getStatus());
    }

    @Test
    @DisplayName("completeTour should throw for unknown character")
    void testCompleteTourThrowsForUnknown() {
        when(characterService.getCharacter(999)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.completeTour(999));
    }

    @Test
    @DisplayName("completeTour should set COMPLETED when max tours served")
    void testCompleteTourSetsCompletedWhenMaxed() {
        character.setStatus(CharacterStatus.ON_DUTY);
        for (int i = 0; i < MAX_CAREER_TOURS; i++) {
            character.addCareerTour(new TravellerCharacter.CareerTour(
                    Career.SOLDIER, 4, new ArrayList<>(), "Enlisted", 5000, false,
                    TravellerCharacter.RetirementType.NORMAL));
        }
        when(characterService.getCharacter(1)).thenReturn(character);

        service.completeTour(1);

        assertEquals(CharacterStatus.COMPLETED, character.getStatus());
    }

    // ---- Query Methods ----

    @Test
    @DisplayName("getActiveTours should return only ON_DUTY characters")
    void testGetActiveTours() {
        TravellerCharacter onDuty = new TravellerCharacter(1, "On Duty");
        onDuty.rollAbilities();
        onDuty.setStatus(CharacterStatus.ON_DUTY);

        TravellerCharacter training = new TravellerCharacter(2, "Training");
        training.rollAbilities();
        training.setStatus(CharacterStatus.TRAINING);

        when(characterService.listCharacters()).thenReturn(List.of(onDuty, training));

        List<TravellerCharacter> active = service.getActiveTours();

        assertEquals(1, active.size());
        assertSame(onDuty, active.get(0));
    }

    @Test
    @DisplayName("getActiveTours should return empty when no on-duty characters")
    void testGetActiveToursEmpty() {
        when(characterService.listCharacters()).thenReturn(List.of(character));

        List<TravellerCharacter> active = service.getActiveTours();

        assertTrue(active.isEmpty());
    }

    @Test
    @DisplayName("getCharacterStatus should return status")
    void testGetCharacterStatus() {
        character.setStatus(CharacterStatus.ON_DUTY);
        when(characterService.getCharacter(1)).thenReturn(character);

        CharacterStatus status = service.getCharacterStatus(1);

        assertEquals(CharacterStatus.ON_DUTY, status);
    }

    @Test
    @DisplayName("getCharacterStatus should return null for unknown character")
    void testGetCharacterStatusUnknown() {
        when(characterService.getCharacter(999)).thenReturn(null);

        CharacterStatus status = service.getCharacterStatus(999);

        assertNull(status);
    }

    @Test
    @DisplayName("isAvailableForLogin should return true for TRAINING")
    void testIsAvailableForLoginTraining() {
        character.setStatus(CharacterStatus.TRAINING);
        when(characterService.getCharacter(1)).thenReturn(character);

        assertTrue(service.isAvailableForLogin(1));
    }

    @Test
    @DisplayName("isAvailableForLogin should return true for COMPLETED")
    void testIsAvailableForLoginCompleted() {
        character.setStatus(CharacterStatus.COMPLETED);
        when(characterService.getCharacter(1)).thenReturn(character);

        assertTrue(service.isAvailableForLogin(1));
    }

    @Test
    @DisplayName("isAvailableForLogin should return false for ON_DUTY")
    void testIsAvailableForLoginOnDuty() {
        character.setStatus(CharacterStatus.ON_DUTY);
        when(characterService.getCharacter(1)).thenReturn(character);

        assertFalse(service.isAvailableForLogin(1));
    }

    @Test
    @DisplayName("isAvailableForLogin should return false for unknown character")
    void testIsAvailableForLoginUnknown() {
        when(characterService.getCharacter(999)).thenReturn(null);

        assertFalse(service.isAvailableForLogin(999));
    }

    @Test
    @DisplayName("getRemainingTours should return correct count")
    void testGetRemainingTours() {
        when(characterService.getCharacter(1)).thenReturn(character);

        int remaining = service.getRemainingTours(1);

        assertEquals(4, remaining);
    }

    @Test
    @DisplayName("getRemainingTours should return 0 for dead character")
    void testGetRemainingToursDead() {
        character.setAlive(false);
        when(characterService.getCharacter(1)).thenReturn(character);

        int remaining = service.getRemainingTours(1);

        assertEquals(0, remaining);
    }

    @Test
    @DisplayName("getRemainingTours should return 0 for unknown character")
    void testGetRemainingToursUnknown() {
        when(characterService.getCharacter(999)).thenReturn(null);

        int remaining = service.getRemainingTours(999);

        assertEquals(0, remaining);
    }

    // ---- TourOfDutyOption ----

    @Test
    @DisplayName("TourOfDutyOption should have correct description")
    void testTourOfDutyOptionDescription() {
        TourOfDutyOption option = new TourOfDutyOption(Career.SOLDIER, 1);

        assertEquals(Career.SOLDIER, option.getCareer());
        assertEquals(1, option.getTermNumber());
        assertEquals("Soldier Term 1", option.getDescription());
    }

    // ---- TourOfDutyRequest ----

    @Test
    @DisplayName("TourOfDutyRequest isValid should return true with all fields")
    void testTourOfDutyRequestValid() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        assertTrue(request.isValid());
    }

    @Test
    @DisplayName("TourOfDutyRequest isValid should return false with missing career")
    void testTourOfDutyRequestInvalidMissingCareer() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, null, 0);

        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("TourOfDutyRequest isValid should return false with zero characterId")
    void testTourOfDutyRequestInvalidZeroId() {
        TourOfDutyRequest request = new TourOfDutyRequest(
                0, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);

        assertFalse(request.isValid());
    }

    @Test
    @DisplayName("TourOfDutyRequest mode helpers should work")
    void testTourOfDutyRequestModeHelpers() {
        TourOfDutyRequest immediate = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.IMMEDIATE, Career.SOLDIER, 0);
        TourOfDutyRequest fourX = new TourOfDutyRequest(
                1, TourOfDutyRequest.ResolveMode.FOUR_X, Career.SOLDIER, 6);

        assertTrue(immediate.isImmediateMode());
        assertFalse(immediate.isFourXMode());
        assertFalse(fourX.isImmediateMode());
        assertTrue(fourX.isFourXMode());
    }

    // ---- CharacterStatus ----

    @Test
    @DisplayName("CharacterStatus TRAINING should be available for login")
    void testCharacterStatusTrainingAvailable() {
        assertTrue(CharacterStatus.TRAINING.isAvailableForLogin());
    }

    @Test
    @DisplayName("CharacterStatus ON_DUTY should not be available for login")
    void testCharacterStatusOnDutyNotAvailable() {
        assertFalse(CharacterStatus.ON_DUTY.isAvailableForLogin());
    }

    @Test
    @DisplayName("CharacterStatus COMPLETED should be available for login")
    void testCharacterStatusCompletedAvailable() {
        assertTrue(CharacterStatus.COMPLETED.isAvailableForLogin());
    }

    @Test
    @DisplayName("CharacterStatus should have display names and descriptions")
    void testCharacterStatusDisplayValues() {
        assertEquals("Training", CharacterStatus.TRAINING.getDisplayName());
        assertEquals("On Duty", CharacterStatus.ON_DUTY.getDisplayName());
        assertEquals("Completed", CharacterStatus.COMPLETED.getDisplayName());
        assertNotNull(CharacterStatus.TRAINING.getDescription());
        assertNotNull(CharacterStatus.ON_DUTY.getDescription());
        assertNotNull(CharacterStatus.COMPLETED.getDescription());
    }

    // ---- Career Helper Methods ----

    @Test
    @DisplayName("Career isMilitaryCareer should return true for Soldier, Gunner, Pilot")
    void testCareerIsMilitary() {
        assertTrue(Career.SOLDIER.isMilitaryCareer());
        assertTrue(Career.GUNNER.isMilitaryCareer());
        assertTrue(Career.PILOT.isMilitaryCareer());
        assertFalse(Career.MERCHANT.isMilitaryCareer());
        assertFalse(Career.NOBLE.isMilitaryCareer());
    }

    @Test
    @DisplayName("Career isNavyCareer should return true for Gunner, Pilot")
    void testCareerIsNavy() {
        assertTrue(Career.GUNNER.isNavyCareer());
        assertTrue(Career.PILOT.isNavyCareer());
        assertFalse(Career.SOLDIER.isNavyCareer());
    }

    @Test
    @DisplayName("Career isArmyCareer should return true for Soldier")
    void testCareerIsArmy() {
        assertTrue(Career.SOLDIER.isArmyCareer());
        assertFalse(Career.GUNNER.isArmyCareer());
        assertFalse(Career.PILOT.isArmyCareer());
    }

    @Test
    @DisplayName("Career getDisplayName should return name")
    void testCareerGetDisplayName() {
        assertEquals("Soldier", Career.SOLDIER.getDisplayName());
        assertEquals("Gunner", Career.GUNNER.getDisplayName());
        assertEquals("Pilot", Career.PILOT.getDisplayName());
    }

    // ---- TravellerCharacter Status ----

    @Test
    @DisplayName("New TravellerCharacter should default to TRAINING status")
    void testTravellerCharacterDefaultStatus() {
        TravellerCharacter tc = new TravellerCharacter(1, "Test");
        assertEquals(CharacterStatus.TRAINING, tc.getStatus());
    }

    @Test
    @DisplayName("TravellerCharacter setStatus should update status")
    void testTravellerCharacterSetStatus() {
        TravellerCharacter tc = new TravellerCharacter(1, "Test");
        tc.setStatus(CharacterStatus.ON_DUTY);
        assertEquals(CharacterStatus.ON_DUTY, tc.getStatus());
    }

    @Test
    @DisplayName("TravellerCharacter setStatus should throw for null")
    void testTravellerCharacterSetStatusNull() {
        TravellerCharacter tc = new TravellerCharacter(1, "Test");
        assertThrows(NullPointerException.class, () -> tc.setStatus(null));
    }
}
