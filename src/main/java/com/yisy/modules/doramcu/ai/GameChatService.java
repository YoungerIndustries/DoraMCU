package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doraai.service.AiChatService;
import com.yisy.modules.doraai.service.AiProvider.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges the game domain with the AI chat service to provide empire-aware chat responses.
 * <p>
 * This service maintains a registry of empire AI cores and their decision contexts,
 * enabling the chat system to provide strategic advice, tactical analysis, and
 * personality-driven responses based on the current game state.
 * </p>
 * <p>
 * Chat modes supported:
 * <ul>
 *   <li>{@code chat} — General game questions</li>
 *   <li>{@code game-advice} — Empire-specific strategic advice</li>
 *   <li>{@code tactical-analysis} — Combat and fleet analysis</li>
 * </ul>
 * </p>
 */
@Service
public class GameChatService {

    private static final Logger log = LoggerFactory.getLogger(GameChatService.class);

    private final AiChatService aiChatService;

    // Registry of empire AI cores for game-aware chat
    private final Map<Long, EmpireAiCore> empireAiCores = new ConcurrentHashMap<>();

    // Registry of decision contexts for each empire
    private final Map<Long, AiDecisionContext> empireContexts = new ConcurrentHashMap<>();

    @Autowired
    public GameChatService(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * Sends a game-aware chat message with empire context.
     *
     * @param sessionId the conversation session ID
     * @param message the user message
     * @param mode the chat mode (chat, game-advice, tactical-analysis)
     * @param empireId the empire ID for game context
     * @return SseEmitter for streaming the AI response
     */
    public SseEmitter chat(String sessionId, String message, String mode, Long empireId) {
        String systemPrompt = buildGameAwareSystemPrompt(mode, empireId);

        // Build the message list with system prompt
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", message));

        log.info("Game chat request: sessionId={}, mode={}, empireId={}", sessionId, mode, empireId);

        // Delegate to the underlying chat service
        return aiChatService.chat(sessionId, message, mode);
    }

    /**
     * Builds a game-aware system prompt enriched with empire context.
     *
     * @param mode the chat mode
     * @param empireId the empire ID
     * @return the enriched system prompt
     */
    public String buildGameAwareSystemPrompt(String mode, Long empireId) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are DoraAI, a strategic advisor for the DoraMCU space strategy game. ");
        sb.append("You provide tailored advice based on the empire's personality, game state, and strategic situation.\n\n");

        // Mode-specific instructions
        if ("game-advice".equals(mode)) {
            sb.append("## Task\n");
            sb.append("Provide strategic advice for the empire. Consider the empire's personality, resources, ");
            sb.append("fleet strength, and threats when making recommendations.\n\n");
        } else if ("tactical-analysis".equals(mode)) {
            sb.append("## Task\n");
            sb.append("Analyze the tactical situation. Evaluate fleet compositions, combat strengths, ");
            sb.append("and recommend optimal tactics.\n\n");
        } else {
            sb.append("## Task\n");
            sb.append("Answer the user's question about the game. Be concise but thorough.\n\n");
        }

        // Add empire context if available
        if (empireId != null && empireAiCores.containsKey(empireId)) {
            EmpireAiCore aiCore = empireAiCores.get(empireId);
            sb.append("## Empire Context\n");
            sb.append("You are advising ").append(aiCore.getPlayer().getEmpireName()).append(", ");
            sb.append("a ").append(aiCore.getPersonality().getLabel()).append(".\n");
            sb.append("Research level: ").append(aiCore.getResearchLevel()).append("/10\n\n");

            // Add personality tips
            List<String> tips = EmpireChatAdvisor.generatePersonalityTips(aiCore.getPlayer());
            if (!tips.isEmpty()) {
                sb.append("### Personality Tips\n");
                for (String tip : tips) {
                    sb.append("- ").append(tip).append("\n");
                }
                sb.append("\n");
            }
        }

        // Add game state context if available
        if (empireId != null && empireAiCores.containsKey(empireId) && empireContexts.containsKey(empireId)) {
            EmpireAiCore aiCore = empireAiCores.get(empireId);
            AiDecisionContext context = empireContexts.get(empireId);

            sb.append("## Current Game State\n");
            sb.append(GameContextProvider.buildGameStateContext(
                    aiCore.getPlayer(),
                    context.getOwnedFleets(),
                    context.getStations(),
                    context.getResources()
            ));
            sb.append("\n");

            // Add strategic recommendation
            String advice = EmpireChatAdvisor.generateBriefAdvice(aiCore, context);
            if (advice != null && !advice.isBlank()) {
                sb.append("## Current Recommendation\n");
                sb.append(advice).append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * Generates a full strategic analysis report for an empire.
     *
     * @param empireId the empire ID
     * @return a formatted strategic analysis report
     */
    public String generateStrategicReport(Long empireId) {
        if (!empireAiCores.containsKey(empireId) || !empireContexts.containsKey(empireId)) {
            return "No strategic data available for this empire.";
        }

        EmpireAiCore aiCore = empireAiCores.get(empireId);
        AiDecisionContext context = empireContexts.get(empireId);

        return EmpireChatAdvisor.generateStrategicAdvice(aiCore, context);
    }

    /**
     * Generates tactical analysis for a fleet.
     *
     * @param fleetId the fleet ID to analyze
     * @param empireId the empire ID
     * @return a formatted tactical analysis report
     */
    public String generateTacticalAnalysis(Long fleetId, Long empireId) {
        if (!empireContexts.containsKey(empireId)) {
            return "No tactical data available.";
        }

        AiDecisionContext context = empireContexts.get(empireId);
        Fleet targetFleet = null;

        for (Fleet fleet : context.getOwnedFleets()) {
            if (fleet.getId() == fleetId) {
                targetFleet = fleet;
                break;
            }
        }

        if (targetFleet == null) {
            return "Fleet #" + fleetId + " not found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(TacticalChatAnalyzer.analyzeFleet(targetFleet));
        sb.append("\n");
        sb.append(TacticalChatAnalyzer.tacticalRecommendations(targetFleet, context.getEnemyFleets()));

        return sb.toString();
    }

    /**
     * Generates combat analysis between two fleets.
     *
     * @param myFleetId the player's fleet ID
     * @param enemyFleetId the enemy fleet ID
     * @param empireId the empire ID
     * @return a formatted combat analysis report
     */
    public String generateCombatAnalysis(Long myFleetId, Long enemyFleetId, Long empireId) {
        if (!empireContexts.containsKey(empireId)) {
            return "No combat data available.";
        }

        AiDecisionContext context = empireContexts.get(empireId);
        Fleet myFleet = null;
        Fleet enemyFleet = null;

        for (Fleet fleet : context.getOwnedFleets()) {
            if (fleet.getId() == myFleetId) {
                myFleet = fleet;
                break;
            }
        }

        for (Fleet fleet : context.getEnemyFleets()) {
            if (fleet.getId() == enemyFleetId) {
                enemyFleet = fleet;
                break;
            }
        }

        if (myFleet == null || enemyFleet == null) {
            return "One or both fleets not found.";
        }

        return TacticalChatAnalyzer.analyzeCombat(myFleet, enemyFleet);
    }

    // --- Empire AI Core Management ---

    /**
     * Registers an empire AI core for game-aware chat.
     *
     * @param empireId the empire ID
     * @param aiCore the empire's AI core
     */
    public void registerEmpireAiCore(Long empireId, EmpireAiCore aiCore) {
        empireAiCores.put(empireId, aiCore);
        log.info("Registered empire AI core for empire {}: {}", empireId, aiCore);
    }

    /**
     * Updates the decision context for an empire.
     *
     * @param empireId the empire ID
     * @param context the current decision context
     */
    public void updateEmpireContext(Long empireId, AiDecisionContext context) {
        empireContexts.put(empireId, context);
        log.debug("Updated decision context for empire {}", empireId);
    }

    /**
     * Removes an empire's AI core and context.
     *
     * @param empireId the empire ID
     */
    public void removeEmpire(Long empireId) {
        empireAiCores.remove(empireId);
        empireContexts.remove(empireId);
        log.info("Removed empire AI core for empire {}", empireId);
    }

    /**
     * Gets the empire AI core for an empire.
     *
     * @param empireId the empire ID
     * @return the empire's AI core, or null if not registered
     */
    public EmpireAiCore getEmpireAiCore(Long empireId) {
        return empireAiCores.get(empireId);
    }

    /**
     * Checks if an empire is registered.
     *
     * @param empireId the empire ID
     * @return true if the empire is registered
     */
    public boolean hasEmpire(Long empireId) {
        return empireAiCores.containsKey(empireId);
    }
}
