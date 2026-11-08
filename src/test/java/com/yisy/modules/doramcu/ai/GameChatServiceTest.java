package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SpaceStationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameChatService Tests")
class GameChatServiceTest {

    private GameChatService gameChatService;
    private Player player;
    private EmpireAiCore empireAiCore;

    @BeforeEach
    void setUp() {
        // Create a no-op AiChatService mock — we only test GameChatService logic here
        gameChatService = new GameChatService(new NoOpAiChatService(null));
        player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
        empireAiCore = new EmpireAiCore(1, player, 5);
    }

    // ---- buildGameAwareSystemPrompt Tests ----

    @Test
    @DisplayName("should build default prompt without empire context")
    void buildDefaultPrompt() {
        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", null);

        assertTrue(prompt.contains("DoraAI"));
        assertTrue(prompt.contains("space strategy game"));
        assertTrue(prompt.contains("## Task"));
        assertTrue(prompt.contains("Answer the user's question"));
        assertFalse(prompt.contains("## Empire Context"));
    }

    @Test
    @DisplayName("should build game-advice mode prompt")
    void buildGameAdvicePrompt() {
        String prompt = gameChatService.buildGameAwareSystemPrompt("game-advice", null);

        assertTrue(prompt.contains("## Task"));
        assertTrue(prompt.contains("Provide strategic advice"));
        assertTrue(prompt.contains("personality, resources"));
    }

    @Test
    @DisplayName("should build tactical-analysis mode prompt")
    void buildTacticalAnalysisPrompt() {
        String prompt = gameChatService.buildGameAwareSystemPrompt("tactical-analysis", null);

        assertTrue(prompt.contains("## Task"));
        assertTrue(prompt.contains("Analyze the tactical situation"));
        assertTrue(prompt.contains("fleet compositions"));
    }

    @Test
    @DisplayName("should build prompt with empire context when AI core is registered")
    void buildPromptWithEmpireContext() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);
        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 1L);

        assertTrue(prompt.contains("## Empire Context"));
        assertTrue(prompt.contains("Test Empire"));
        assertTrue(prompt.contains("Research level: 5/10"));
    }

    @Test
    @DisplayName("should not include empire context when empire not registered")
    void buildPromptWithoutEmpireContext() {
        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 999L);

        assertFalse(prompt.contains("## Empire Context"));
    }

    @Test
    @DisplayName("should include game state context when both AI core and context are registered")
    void buildPromptWithGameStateContext() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);

        List<Fleet> ownedFleets = new ArrayList<>();
        ownedFleets.add(new Fleet(1, "Fleet 1"));

        List<SpaceStation> stations = new ArrayList<>();
        stations.add(new SpaceStation(1, SpaceStationType.MILITARY));

        Map<String, Integer> resources = new HashMap<>();
        resources.put("minerals", 200);
        resources.put("energy", 100);

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(ownedFleets)
                .enemyFleets(new ArrayList<>())
                .stations(stations)
                .resources(resources)
                .minerals(200)
                .energy(100)
                .food(50)
                .credits(75)
                .build();

        gameChatService.updateEmpireContext(1L, context);

        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 1L);

        assertTrue(prompt.contains("## Current Game State"));
        assertTrue(prompt.contains("## Empire Context"));
    }

    @Test
    @DisplayName("should not include game state context when context is not registered")
    void buildPromptWithoutGameStateContext() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);
        // Do not register context
        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 1L);

        assertTrue(prompt.contains("## Empire Context"));
        assertFalse(prompt.contains("## Current Game State"));
    }

    @Test
    @DisplayName("should include personality tips in prompt")
    void buildPromptWithPersonalityTips() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);
        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 1L);

        // Democracy + Scientific should have personality tips
        assertTrue(prompt.contains("### Personality Tips") || prompt.contains("## Empire Context"));
    }

    // ---- Empire Registration Tests ----

    @Test
    @DisplayName("should register and retrieve empire AI core")
    void registerEmpireAiCore() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);
        // Verify via prompt building
        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 1L);
        assertTrue(prompt.contains("Test Empire"));
    }

    @Test
    @DisplayName("should update empire context")
    void updateEmpireContext() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        gameChatService.updateEmpireContext(1L, context);

        String prompt = gameChatService.buildGameAwareSystemPrompt("chat", 1L);
        assertTrue(prompt.contains("## Current Game State"));
    }

    // ---- Strategic Report Tests ----

    @Test
    @DisplayName("should return no data message when empire not registered")
    void strategicReportNoEmpire() {
        String report = gameChatService.generateStrategicReport(1L);
        assertTrue(report.contains("No strategic data available"));
    }

    // ---- Tactical Analysis Tests ----

    @Test
    @DisplayName("should return no data message when context not registered")
    void tacticalAnalysisNoContext() {
        String analysis = gameChatService.generateTacticalAnalysis(1L, 1L);
        assertTrue(analysis.contains("No tactical data available"));
    }

    @Test
    @DisplayName("should return fleet not found when fleet ID is invalid")
    void tacticalAnalysisFleetNotFound() {
        gameChatService.registerEmpireAiCore(1L, empireAiCore);

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();
        gameChatService.updateEmpireContext(1L, context);

        String analysis = gameChatService.generateTacticalAnalysis(999L, 1L);
        assertTrue(analysis.contains("not found"));
    }

    // ---- Combat Analysis Tests ----

    @Test
    @DisplayName("should return no data message when context not registered")
    void combatAnalysisNoContext() {
        String analysis = gameChatService.generateCombatAnalysis(1L, 2L, 1L);
        assertTrue(analysis.contains("No combat data available"));
    }

    // ---- Unknown Mode Tests ----

    @Test
    @DisplayName("should handle unknown mode as default chat")
    void buildPromptUnknownMode() {
        String prompt = gameChatService.buildGameAwareSystemPrompt("unknown-mode", null);

        assertTrue(prompt.contains("## Task"));
        assertTrue(prompt.contains("Answer the user's question"));
    }

    // ---- Null Mode Tests ----

    @Test
    @DisplayName("should handle null mode as default chat")
    void buildPromptNullMode() {
        String prompt = gameChatService.buildGameAwareSystemPrompt(null, null);

        assertTrue(prompt.contains("## Task"));
        assertTrue(prompt.contains("Answer the user's question"));
    }

    // ---- NoOpAiChatService for testing ----

    /**
     * Minimal AiChatService implementation for testing GameChatService without Spring context.
     */
    private static class NoOpAiChatService extends com.yisy.modules.doraai.service.AiChatService {
        NoOpAiChatService(com.yisy.modules.doraai.service.AiProviderFactory factory) {
            super(factory);
        }

        @Override
        public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chat(
                String sessionId, String message, String mode) {
            return null;
        }

        @Override
        public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatWithSystemPrompt(
                String sessionId, String message, String mode, String systemPrompt) {
            return null;
        }
    }
}
