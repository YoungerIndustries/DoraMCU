package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AiDelegationService Tests")
class AiDelegationServiceTest {

    private AiDelegationService service;
    private AiPlayerService aiPlayerService;
    private GameSessionService gameSessionService;

    @BeforeEach
    void setUp() {
        aiPlayerService = Mockito.mock(AiPlayerService.class);
        gameSessionService = Mockito.mock(GameSessionService.class);
        service = new AiDelegationService(aiPlayerService, gameSessionService);
    }

    // ---- Default Settings ----

    @Test
    @DisplayName("should return all-disabled settings by default")
    void defaultSettings() {
        AiDelegationSettings settings = service.getDelegationSettings("session1", 1L);
        assertFalse(settings.isEmpireAiEnabled());
        assertEquals(0, settings.getDelegatedCount());
    }

    // ---- Set and Get Settings ----

    @Test
    @DisplayName("should set and get delegation settings")
    void setAndGetSettings() {
        AiDelegationSettings settings = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        service.setDelegationSettings("session1", 1L, settings);

        AiDelegationSettings retrieved = service.getDelegationSettings("session1", 1L);
        assertTrue(retrieved.isEmpireAiEnabled());
        assertTrue(retrieved.isDelegated(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("should throw on null settings")
    void setNullSettingsThrows() {
        assertThrows(NullPointerException.class, () ->
                service.setDelegationSettings("session1", 1L, null));
    }

    @Test
    @DisplayName("should store settings per player per session")
    void settingsPerPlayerPerSession() {
        AiDelegationSettings s1 = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.COMBAT)
                .build();
        AiDelegationSettings s2 = AiDelegationSettings.builder()
                .empireAiEnabled(true)
                .enableArea(AiDelegationArea.TRADE)
                .build();

        service.setDelegationSettings("session1", 1L, s1);
        service.setDelegationSettings("session1", 2L, s2);

        assertTrue(service.getDelegationSettings("session1", 1L).isDelegated(AiDelegationArea.COMBAT));
        assertTrue(service.getDelegationSettings("session1", 2L).isDelegated(AiDelegationArea.TRADE));
        assertFalse(service.getDelegationSettings("session1", 1L).isDelegated(AiDelegationArea.TRADE));
    }

    // ---- setEmpireAiEnabled ----

    @Test
    @DisplayName("should enable empire AI and all areas")
    void enableEmpireAi() {
        AiDelegationSettings settings = service.setEmpireAiEnabled("session1", 1L, true);
        assertTrue(settings.isEmpireAiEnabled());
        assertEquals(9, settings.getDelegatedCount());
    }

    @Test
    @DisplayName("should disable empire AI")
    void disableEmpireAi() {
        service.setEmpireAiEnabled("session1", 1L, true);
        AiDelegationSettings settings = service.setEmpireAiEnabled("session1", 1L, false);
        assertFalse(settings.isEmpireAiEnabled());
        assertEquals(0, settings.getDelegatedCount());
    }

    // ---- toggleArea ----

    @Test
    @DisplayName("should toggle area on")
    void toggleAreaOn() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.toggleArea("session1", 1L, AiDelegationArea.TRADE, false);

        AiDelegationSettings settings = service.getDelegationSettings("session1", 1L);
        assertTrue(settings.isEmpireAiEnabled());
        assertFalse(settings.isDelegated(AiDelegationArea.TRADE));
        assertTrue(settings.isDelegated(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("should toggle area off")
    void toggleAreaOff() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.toggleArea("session1", 1L, AiDelegationArea.COMBAT, false);

        AiDelegationSettings settings = service.getDelegationSettings("session1", 1L);
        assertFalse(settings.isDelegated(AiDelegationArea.COMBAT));
    }

    @Test
    @DisplayName("should throw on null area")
    void toggleAreaNullThrows() {
        assertThrows(NullPointerException.class, () ->
                service.toggleArea("session1", 1L, null, true));
    }

    // ---- shouldDelegateAction ----

    @Test
    @DisplayName("should not delegate when empire AI is off")
    void shouldNotDelegateWhenEmpireOff() {
        assertFalse(service.shouldDelegateAction("session1", 1L, "moveFleet"));
    }

    @Test
    @DisplayName("should delegate moveFleet when FLEET_MANAGEMENT is enabled")
    void shouldDelegateMoveFleet() {
        service.setEmpireAiEnabled("session1", 1L, true);
        assertTrue(service.shouldDelegateAction("session1", 1L, "moveFleet"));
    }

    @Test
    @DisplayName("should not delegate when area is disabled")
    void shouldNotDelegateWhenAreaDisabled() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.toggleArea("session1", 1L, AiDelegationArea.FLEET_MANAGEMENT, false);
        assertFalse(service.shouldDelegateAction("session1", 1L, "moveFleet"));
    }

    @Test
    @DisplayName("should not delegate unmapped actions")
    void shouldNotDelegateUnmappedAction() {
        service.setEmpireAiEnabled("session1", 1L, true);
        assertFalse(service.shouldDelegateAction("session1", 1L, "unknownAction"));
    }

    @Test
    @DisplayName("should delegate engageCombat when COMBAT is enabled")
    void shouldDelegateEngageCombat() {
        service.setEmpireAiEnabled("session1", 1L, true);
        assertTrue(service.shouldDelegateAction("session1", 1L, "engageCombat"));
    }

    @Test
    @DisplayName("should delegate buildShip when CONSTRUCTION is enabled")
    void shouldDelegateBuildShip() {
        service.setEmpireAiEnabled("session1", 1L, true);
        assertTrue(service.shouldDelegateAction("session1", 1L, "buildShip"));
    }

    @Test
    @DisplayName("should delegate buyCommodity when TRADE is enabled")
    void shouldDelegateBuyCommodity() {
        service.setEmpireAiEnabled("session1", 1L, true);
        assertTrue(service.shouldDelegateAction("session1", 1L, "buyCommodity"));
    }

    @Test
    @DisplayName("should delegate colonizePlanet when EXPANSION is enabled")
    void shouldDelegateColonizePlanet() {
        service.setEmpireAiEnabled("session1", 1L, true);
        assertTrue(service.shouldDelegateAction("session1", 1L, "colonizePlanet"));
    }

    // ---- isAreaDelegated ----

    @Test
    @DisplayName("should check area delegation")
    void isAreaDelegated() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.toggleArea("session1", 1L, AiDelegationArea.COMBAT, false);

        assertFalse(service.isAreaDelegated("session1", 1L, AiDelegationArea.COMBAT));
        assertTrue(service.isAreaDelegated("session1", 1L, AiDelegationArea.TRADE));
    }

    @Test
    @DisplayName("should throw on null area")
    void isAreaDelegatedNullThrows() {
        assertThrows(NullPointerException.class, () ->
                service.isAreaDelegated("session1", 1L, null));
    }

    // ---- getDelegationStatus ----

    @Test
    @DisplayName("should return status map with correct structure")
    void getDelegationStatus() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.toggleArea("session1", 1L, AiDelegationArea.COMBAT, false);

        Map<String, Object> status = service.getDelegationStatus("session1", 1L);
        assertTrue((Boolean) status.get("empireAiEnabled"));
        assertEquals(8, status.get("delegatedCount"));
        assertEquals(9, status.get("totalAreas"));

        @SuppressWarnings("unchecked")
        Map<String, Boolean> areas = (Map<String, Boolean>) status.get("areas");
        assertFalse(areas.get("COMBAT"));
        assertTrue(areas.get("TRADE"));
    }

    @Test
    @DisplayName("should return all false when delegation is off")
    void getDelegationStatusOff() {
        Map<String, Object> status = service.getDelegationStatus("session1", 1L);
        assertFalse((Boolean) status.get("empireAiEnabled"));
        assertEquals(0, status.get("delegatedCount"));
    }

    // ---- Remove Settings ----

    @Test
    @DisplayName("should remove settings for a player")
    void removeDelegationSettings() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.removeDelegationSettings("session1", 1L);

        AiDelegationSettings settings = service.getDelegationSettings("session1", 1L);
        assertFalse(settings.isEmpireAiEnabled());
    }

    @Test
    @DisplayName("should remove all settings for a session")
    void removeSessionDelegation() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.setEmpireAiEnabled("session1", 2L, true);
        service.removeSessionDelegation("session1");

        assertFalse(service.getDelegationSettings("session1", 1L).isEmpireAiEnabled());
        assertFalse(service.getDelegationSettings("session1", 2L).isEmpireAiEnabled());
    }

    // ---- Static Utility Methods ----

    @Test
    @DisplayName("getAreaForAction should return correct area")
    void getAreaForAction() {
        assertEquals(AiDelegationArea.FLEET_MANAGEMENT, AiDelegationService.getAreaForAction("moveFleet"));
        assertEquals(AiDelegationArea.COMBAT, AiDelegationService.getAreaForAction("engageCombat"));
        assertEquals(AiDelegationArea.CONSTRUCTION, AiDelegationService.getAreaForAction("buildShip"));
        assertEquals(AiDelegationArea.TRADE, AiDelegationService.getAreaForAction("buyCommodity"));
        assertEquals(AiDelegationArea.EXPANSION, AiDelegationService.getAreaForAction("colonizePlanet"));
        assertNull(AiDelegationService.getAreaForAction("unknown"));
    }

    @Test
    @DisplayName("getActionsForArea should return correct actions")
    void getActionsForArea() {
        Set<String> fleetActions = AiDelegationService.getActionsForArea(AiDelegationArea.FLEET_MANAGEMENT);
        assertTrue(fleetActions.contains("moveFleet"));
        assertTrue(fleetActions.contains("createFleet"));
        assertEquals(2, fleetActions.size());
    }

    @Test
    @DisplayName("getActionsForArea should throw on null")
    void getActionsForAreaNullThrows() {
        assertThrows(NullPointerException.class, () ->
                AiDelegationService.getActionsForArea(null));
    }

    // ---- Action-to-Area Mapping Coverage ----

    @Test
    @DisplayName("should map all action names to correct areas")
    void allActionMappings() {
        service.setEmpireAiEnabled("session1", 1L, true);

        // FLEET_MANAGEMENT
        assertTrue(service.shouldDelegateAction("session1", 1L, "moveFleet"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "createFleet"));

        // COMBAT
        assertTrue(service.shouldDelegateAction("session1", 1L, "engageCombat"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "reinforceBattle"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "retreatBattle"));

        // CONSTRUCTION
        assertTrue(service.shouldDelegateAction("session1", 1L, "buildShip"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "buildStation"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "buildFacility"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "buildShipFromDesign"));

        // TRADE
        assertTrue(service.shouldDelegateAction("session1", 1L, "buyCommodity"));
        assertTrue(service.shouldDelegateAction("session1", 1L, "sellCommodity"));

        // EXPANSION
        assertTrue(service.shouldDelegateAction("session1", 1L, "colonizePlanet"));

        // COMMANDER_ASSIGNMENT
        assertTrue(service.shouldDelegateAction("session1", 1L, "assignCommander"));
    }

    // ---- Areas without direct action mappings ----

    @Test
    @DisplayName("RESOURCE_MANAGEMENT should have no mapped actions")
    void resourceManagementNoMappedActions() {
        Set<String> actions = AiDelegationService.getActionsForArea(AiDelegationArea.RESOURCE_MANAGEMENT);
        assertTrue(actions.isEmpty());
    }

    @Test
    @DisplayName("RESEARCH should have no mapped actions")
    void researchNoMappedActions() {
        Set<String> actions = AiDelegationService.getActionsForArea(AiDelegationArea.RESEARCH);
        assertTrue(actions.isEmpty());
    }

    @Test
    @DisplayName("DIPLOMACY should have no mapped actions")
    void diplomacyNoMappedActions() {
        Set<String> actions = AiDelegationService.getActionsForArea(AiDelegationArea.DIPLOMACY);
        assertTrue(actions.isEmpty());
    }

    // ---- getActionsForArea for all areas with mappings ----

    @Test
    @DisplayName("getActionsForArea should return correct actions for all mapped areas")
    void getActionsForAreaAll() {
        Set<String> fleetActions = AiDelegationService.getActionsForArea(AiDelegationArea.FLEET_MANAGEMENT);
        assertEquals(2, fleetActions.size());
        assertTrue(fleetActions.contains("moveFleet"));
        assertTrue(fleetActions.contains("createFleet"));

        Set<String> combatActions = AiDelegationService.getActionsForArea(AiDelegationArea.COMBAT);
        assertEquals(3, combatActions.size());
        assertTrue(combatActions.contains("engageCombat"));
        assertTrue(combatActions.contains("reinforceBattle"));
        assertTrue(combatActions.contains("retreatBattle"));

        Set<String> constructionActions = AiDelegationService.getActionsForArea(AiDelegationArea.CONSTRUCTION);
        assertEquals(4, constructionActions.size());
        assertTrue(constructionActions.contains("buildShip"));
        assertTrue(constructionActions.contains("buildStation"));
        assertTrue(constructionActions.contains("buildFacility"));
        assertTrue(constructionActions.contains("buildShipFromDesign"));

        Set<String> tradeActions = AiDelegationService.getActionsForArea(AiDelegationArea.TRADE);
        assertEquals(2, tradeActions.size());
        assertTrue(tradeActions.contains("buyCommodity"));
        assertTrue(tradeActions.contains("sellCommodity"));

        Set<String> expansionActions = AiDelegationService.getActionsForArea(AiDelegationArea.EXPANSION);
        assertEquals(1, expansionActions.size());
        assertTrue(expansionActions.contains("colonizePlanet"));

        Set<String> commanderActions = AiDelegationService.getActionsForArea(AiDelegationArea.COMMANDER_ASSIGNMENT);
        assertEquals(1, commanderActions.size());
        assertTrue(commanderActions.contains("assignCommander"));
    }

    // ---- executeAiTurn ----

    @Test
    @DisplayName("executeAiTurn should return null when delegation is off")
    void executeAiTurnNoDelegation() {
        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAiTurn("session1", 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("executeAiTurn should return null when no areas delegated")
    void executeAiTurnNoAreasDelegated() {
        service.setEmpireAiEnabled("session1", 1L, true);
        // Disable all areas
        for (AiDelegationArea area : AiDelegationArea.values()) {
            service.toggleArea("session1", 1L, area, false);
        }
        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAiTurn("session1", 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("executeAiTurn should execute AI turn when delegation is on")
    void executeAiTurnWithDelegation() {
        service.setEmpireAiEnabled("session1", 1L, true);

        com.yisy.modules.doramcu.model.GameSession mockSession = Mockito.mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSessionService.getGameSession("session1")).thenReturn(mockSession);
        when(mockSession.isAiPlayer(1L)).thenReturn(false);

        com.yisy.modules.doramcu.dto.GameStateResponse expectedResponse = new com.yisy.modules.doramcu.dto.GameStateResponse();
        when(aiPlayerService.executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(expectedResponse);

        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAiTurn("session1", 1L);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(mockSession).registerAiPlayer(1L, "DELEGATED");
        verify(aiPlayerService).executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM));
    }

    @Test
    @DisplayName("executeAiTurn should not register AI player if already registered")
    void executeAiTurnAlreadyRegistered() {
        service.setEmpireAiEnabled("session1", 1L, true);

        com.yisy.modules.doramcu.model.GameSession mockSession = Mockito.mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSessionService.getGameSession("session1")).thenReturn(mockSession);
        when(mockSession.isAiPlayer(1L)).thenReturn(true);

        com.yisy.modules.doramcu.dto.GameStateResponse expectedResponse = new com.yisy.modules.doramcu.dto.GameStateResponse();
        when(aiPlayerService.executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(expectedResponse);

        service.executeAiTurn("session1", 1L);

        verify(mockSession, never()).registerAiPlayer(anyLong(), anyString());
    }

    @Test
    @DisplayName("executeAiTurn should handle null session gracefully")
    void executeAiTurnNullSession() {
        service.setEmpireAiEnabled("session1", 1L, true);

        when(gameSessionService.getGameSession("session1")).thenReturn(null);

        com.yisy.modules.doramcu.dto.GameStateResponse expectedResponse = new com.yisy.modules.doramcu.dto.GameStateResponse();
        when(aiPlayerService.executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(expectedResponse);

        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAiTurn("session1", 1L);

        assertNotNull(result);
        // Should still execute AI turn even if session is null
        verify(aiPlayerService).executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM));
    }

    @Test
    @DisplayName("executeAiTurn should return null on exception")
    void executeAiTurnExceptionReturnsNull() {
        service.setEmpireAiEnabled("session1", 1L, true);

        when(gameSessionService.getGameSession("session1")).thenThrow(new RuntimeException("Session error"));

        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAiTurn("session1", 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("executeAiTurn should prevent recursive execution")
    void executeAiTurnPreventsRecursion() {
        service.setEmpireAiEnabled("session1", 1L, true);

        com.yisy.modules.doramcu.model.GameSession mockSession = Mockito.mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSessionService.getGameSession("session1")).thenReturn(mockSession);
        when(mockSession.isAiPlayer(1L)).thenReturn(false);

        // First call triggers a RuntimeException — service should catch it and return null
        when(aiPlayerService.executeAiTurn(eq("session1"), anyMap()))
                .thenThrow(new RuntimeException("Simulated error"))
                .thenReturn(new com.yisy.modules.doramcu.dto.GameStateResponse());

        // First call should return null due to exception
        com.yisy.modules.doramcu.dto.GameStateResponse result1 = service.executeAiTurn("session1", 1L);
        assertNull(result1);

        // After exception, recursion guard is cleared — second call should work
        com.yisy.modules.doramcu.dto.GameStateResponse result2 = service.executeAiTurn("session1", 1L);
        assertNotNull(result2);
    }

    // ---- executeAllDelegatedTurns ----

    @Test
    @DisplayName("executeAllDelegatedTurns should execute for all delegated players")
    void executeAllDelegatedTurns() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.setEmpireAiEnabled("session1", 2L, true);
        service.setEmpireAiEnabled("session1", 3L, true);

        com.yisy.modules.doramcu.model.GameSession mockSession = Mockito.mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSessionService.getGameSession("session1")).thenReturn(mockSession);
        when(mockSession.isAiPlayer(anyLong())).thenReturn(false);

        com.yisy.modules.doramcu.dto.GameStateResponse response1 = new com.yisy.modules.doramcu.dto.GameStateResponse();
        com.yisy.modules.doramcu.dto.GameStateResponse response2 = new com.yisy.modules.doramcu.dto.GameStateResponse();
        com.yisy.modules.doramcu.dto.GameStateResponse response3 = new com.yisy.modules.doramcu.dto.GameStateResponse();

        when(aiPlayerService.executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(response1);
        when(aiPlayerService.executeAiTurn("session1", Map.of(2L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(response2);
        when(aiPlayerService.executeAiTurn("session1", Map.of(3L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(response3);

        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAllDelegatedTurns("session1");

        // Last state should be from the last player processed
        assertNotNull(result);
        verify(aiPlayerService, times(3)).executeAiTurn(eq("session1"), anyMap());
    }

    @Test
    @DisplayName("executeAllDelegatedTurns should skip non-delegated players")
    void executeAllDelegatedTurnsSkipNonDelegated() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.setEmpireAiEnabled("session1", 2L, false);

        com.yisy.modules.doramcu.model.GameSession mockSession = Mockito.mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSessionService.getGameSession("session1")).thenReturn(mockSession);
        when(mockSession.isAiPlayer(1L)).thenReturn(false);

        com.yisy.modules.doramcu.dto.GameStateResponse response = new com.yisy.modules.doramcu.dto.GameStateResponse();
        when(aiPlayerService.executeAiTurn("session1", Map.of(1L, AiPlayerService.Difficulty.MEDIUM)))
                .thenReturn(response);

        service.executeAllDelegatedTurns("session1");

        verify(aiPlayerService, times(1)).executeAiTurn(eq("session1"), anyMap());
        verify(aiPlayerService, never()).executeAiTurn("session1", Map.of(2L, AiPlayerService.Difficulty.MEDIUM));
    }

    @Test
    @DisplayName("executeAllDelegatedTurns should return null when no delegated players")
    void executeAllDelegatedTurnsEmpty() {
        com.yisy.modules.doramcu.dto.GameStateResponse result = service.executeAllDelegatedTurns("session1");
        assertNull(result);
    }

    @Test
    @DisplayName("executeAllDelegatedTurns should only process matching session")
    void executeAllDelegatedTurnsSessionFilter() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.setEmpireAiEnabled("session2", 1L, true);

        com.yisy.modules.doramcu.model.GameSession mockSession = Mockito.mock(com.yisy.modules.doramcu.model.GameSession.class);
        when(gameSessionService.getGameSession("session1")).thenReturn(mockSession);
        when(mockSession.isAiPlayer(1L)).thenReturn(false);

        com.yisy.modules.doramcu.dto.GameStateResponse response = new com.yisy.modules.doramcu.dto.GameStateResponse();
        when(aiPlayerService.executeAiTurn(eq("session1"), anyMap()))
                .thenReturn(response);

        service.executeAllDelegatedTurns("session1");

        verify(aiPlayerService, times(1)).executeAiTurn(eq("session1"), anyMap());
        verify(aiPlayerService, never()).executeAiTurn(eq("session2"), anyMap());
    }

    // ---- Disable/Enable Cycle ----

    @Test
    @DisplayName("disabling and re-enabling empire AI should reset all areas")
    void disableEnableCycle() {
        // Enable empire AI — all areas on
        service.setEmpireAiEnabled("session1", 1L, true);
        AiDelegationSettings settings = service.getDelegationSettings("session1", 1L);
        assertEquals(9, settings.getDelegatedCount());

        // Disable some areas
        service.toggleArea("session1", 1L, AiDelegationArea.COMBAT, false);
        service.toggleArea("session1", 1L, AiDelegationArea.TRADE, false);
        settings = service.getDelegationSettings("session1", 1L);
        assertEquals(7, settings.getDelegatedCount());

        // Disable empire AI
        service.setEmpireAiEnabled("session1", 1L, false);
        settings = service.getDelegationSettings("session1", 1L);
        assertFalse(settings.isEmpireAiEnabled());
        assertEquals(0, settings.getDelegatedCount());

        // Re-enable empire AI — should reset all areas to enabled
        service.setEmpireAiEnabled("session1", 1L, true);
        settings = service.getDelegationSettings("session1", 1L);
        assertTrue(settings.isEmpireAiEnabled());
        assertEquals(9, settings.getDelegatedCount());
    }

    // ---- Cross-session isolation ----

    @Test
    @DisplayName("settings should be isolated across sessions")
    void crossSessionIsolation() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.setEmpireAiEnabled("session2", 1L, false);

        assertTrue(service.getDelegationSettings("session1", 1L).isEmpireAiEnabled());
        assertFalse(service.getDelegationSettings("session2", 1L).isEmpireAiEnabled());
    }

    @Test
    @DisplayName("removing one session should not affect other sessions")
    void removeSessionIsolation() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.setEmpireAiEnabled("session2", 1L, true);

        service.removeSessionDelegation("session1");

        assertFalse(service.getDelegationSettings("session1", 1L).isEmpireAiEnabled());
        assertTrue(service.getDelegationSettings("session2", 1L).isEmpireAiEnabled());
    }

    // ---- getDelegationStatus edge cases ----

    @Test
    @DisplayName("getDelegationStatus should include all 9 areas")
    void getDelegationStatusAllAreas() {
        Map<String, Object> status = service.getDelegationStatus("session1", 1L);

        @SuppressWarnings("unchecked")
        Map<String, Boolean> areas = (Map<String, Boolean>) status.get("areas");
        assertEquals(9, areas.size());

        // All should be false by default
        for (AiDelegationArea area : AiDelegationArea.values()) {
            assertFalse(areas.get(area.name()));
        }
    }

    @Test
    @DisplayName("getDelegationStatus should reflect partial delegation")
    void getDelegationStatusPartial() {
        service.setEmpireAiEnabled("session1", 1L, true);
        service.toggleArea("session1", 1L, AiDelegationArea.COMBAT, false);
        service.toggleArea("session1", 1L, AiDelegationArea.TRADE, false);
        service.toggleArea("session1", 1L, AiDelegationArea.RESEARCH, false);

        Map<String, Object> status = service.getDelegationStatus("session1", 1L);
        assertTrue((Boolean) status.get("empireAiEnabled"));
        assertEquals(6, status.get("delegatedCount"));
        assertEquals(9, status.get("totalAreas"));
    }
}
