package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.dto.GameStateResponse;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that manages AI delegation for human players.
 * <p>
 * When a player enables AI delegation at the empire level, the AI takes over
 * management responsibilities for all delegated areas. The player can fine-tune
 * by toggling individual {@link AiDelegationArea} areas.
 * </p>
 * <p>
 * This service stores delegation settings per player per session, checks whether
 * actions should be auto-delegated, and triggers AI turns for delegated areas.
 * </p>
 * <p>
 * Flow:
 * </p>
 * <ol>
 *   <li>Player enables empire AI via REST endpoint</li>
 *   <li>Settings are stored in GameSession</li>
 *   <li>On each game action, the service checks if the action's area is delegated</li>
 *   <li>If delegated, the AI executes the action instead of (or in addition to) the player</li>
 *   <li>AI turns are executed via {@link AiPlayerService}</li>
 * </ol>
 */
@Service
public class AiDelegationService {

    private static final Logger log = LoggerFactory.getLogger(AiDelegationService.class);

    /**
     * Maps game action method names to their delegation areas.
     * Used to check if an action should be auto-delegated.
     */
    private static final Map<String, AiDelegationArea> ACTION_TO_AREA;
    static {
        Map<String, AiDelegationArea> map = new java.util.HashMap<>();
        map.put("moveFleet", AiDelegationArea.FLEET_MANAGEMENT);
        map.put("createFleet", AiDelegationArea.FLEET_MANAGEMENT);
        map.put("engageCombat", AiDelegationArea.COMBAT);
        map.put("reinforceBattle", AiDelegationArea.COMBAT);
        map.put("retreatBattle", AiDelegationArea.COMBAT);
        map.put("buildShip", AiDelegationArea.CONSTRUCTION);
        map.put("buildStation", AiDelegationArea.CONSTRUCTION);
        map.put("buildFacility", AiDelegationArea.CONSTRUCTION);
        map.put("buildShipFromDesign", AiDelegationArea.CONSTRUCTION);
        map.put("buyCommodity", AiDelegationArea.TRADE);
        map.put("sellCommodity", AiDelegationArea.TRADE);
        map.put("colonizePlanet", AiDelegationArea.EXPANSION);
        map.put("assignCommander", AiDelegationArea.COMMANDER_ASSIGNMENT);
        ACTION_TO_AREA = Map.copyOf(map);
    }

    private final AiPlayerService aiPlayerService;
    private final GameSessionService gameSessionService;

    /**
     * Per-player delegation settings, keyed by sessionId_playerId.
     * Stored separately from GameSession for quick access without locking.
     */
    private final Map<String, AiDelegationSettings> delegationSettings = new ConcurrentHashMap<>();

    /**
     * Tracks whether AI auto-turns are currently executing to prevent recursion.
     */
    private final Set<String> executingAutoTurns = ConcurrentHashMap.newKeySet();

    public AiDelegationService(AiPlayerService aiPlayerService, GameSessionService gameSessionService) {
        this.aiPlayerService = aiPlayerService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Gets the delegation settings for a player in a session.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @return the current delegation settings, or default (all disabled) if not set
     */
    public AiDelegationSettings getDelegationSettings(String sessionId, Long playerId) {
        String key = delegationKey(sessionId, playerId);
        return delegationSettings.getOrDefault(key, AiDelegationSettings.allDisabled());
    }

    /**
     * Sets the delegation settings for a player in a session.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param settings the new delegation settings
     */
    public void setDelegationSettings(String sessionId, Long playerId, AiDelegationSettings settings) {
        Objects.requireNonNull(settings, "Settings must not be null");
        String key = delegationKey(sessionId, playerId);
        delegationSettings.put(key, settings);

        if (settings.isEmpireAiEnabled()) {
            log.info("AI delegation enabled for player {} in session {}: {}",
                    playerId, sessionId, settings.getDelegatedAreas());
        } else {
            log.info("AI delegation disabled for player {} in session {}", playerId, sessionId);
        }
    }

    /**
     * Enables or disables the empire-level AI switch for a player.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param enabled true to enable AI delegation, false to disable
     * @return the updated delegation settings
     */
    public AiDelegationSettings setEmpireAiEnabled(String sessionId, Long playerId, boolean enabled) {
        AiDelegationSettings current = getDelegationSettings(sessionId, playerId);
        AiDelegationSettings updated;

        if (enabled) {
            // When enabling, turn on all areas by default
            updated = AiDelegationSettings.builder()
                    .empireAiEnabled(true)
                    .enableAll()
                    .build();
        } else {
            // When disabling, keep area settings but disable empire switch
            updated = current.withEmpireAiEnabled(false);
        }

        setDelegationSettings(sessionId, playerId, updated);
        return updated;
    }

    /**
     * Toggles a specific delegation area for a player.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param area the area to toggle
     * @param enabled true to enable, false to disable
     * @return the updated delegation settings
     */
    public AiDelegationSettings toggleArea(String sessionId, Long playerId,
                                           AiDelegationArea area, boolean enabled) {
        Objects.requireNonNull(area, "Area must not be null");
        AiDelegationSettings current = getDelegationSettings(sessionId, playerId);
        AiDelegationSettings updated = current.withAreaToggled(area, enabled);
        setDelegationSettings(sessionId, playerId, updated);
        return updated;
    }

    /**
     * Checks if a specific action should be auto-delegated to AI.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param actionName the action method name (e.g., "moveFleet", "buildShip")
     * @return true if the action should be handled by AI
     */
    public boolean shouldDelegateAction(String sessionId, Long playerId, String actionName) {
        AiDelegationSettings settings = getDelegationSettings(sessionId, playerId);
        if (!settings.isEmpireAiEnabled()) {
            return false;
        }

        AiDelegationArea area = ACTION_TO_AREA.get(actionName);
        if (area == null) {
            return false;
        }

        return settings.isDelegated(area);
    }

    /**
     * Checks if a specific delegation area is active for a player.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @param area the delegation area to check
     * @return true if the area is delegated to AI
     */
    public boolean isAreaDelegated(String sessionId, Long playerId, AiDelegationArea area) {
        Objects.requireNonNull(area, "Area must not be null");
        AiDelegationSettings settings = getDelegationSettings(sessionId, playerId);
        return settings.isDelegated(area);
    }

    /**
     * Executes an AI turn for a player with delegation enabled.
     * <p>
     * This method triggers the AI to make decisions and execute actions
     * for the player's empire. It prevents recursive calls by tracking
     * execution state.
     * </p>
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @return the game state after AI actions, or null if execution was skipped
     */
    public GameStateResponse executeAiTurn(String sessionId, Long playerId) {
        String key = delegationKey(sessionId, playerId);
        if (executingAutoTurns.contains(key)) {
            log.debug("AI turn already executing for player {} in session {}, skipping", playerId, sessionId);
            return null;
        }

        AiDelegationSettings settings = getDelegationSettings(sessionId, playerId);
        if (!settings.isEmpireAiEnabled() || settings.getDelegatedAreas().isEmpty()) {
            log.debug("No delegation areas enabled for player {} in session {}", playerId, sessionId);
            return null;
        }

        executingAutoTurns.add(key);
        try {
            log.info("Executing AI turn for delegated player {} in session {}: {}",
                    playerId, sessionId, settings.getDelegatedAreas());

            // Register the player as an AI player if not already registered
            GameSession session = gameSessionService.getGameSession(sessionId);
            if (session != null && !session.isAiPlayer(playerId)) {
                session.registerAiPlayer(playerId, "DELEGATED");
            }

            // Execute AI turn
            Map<Long, AiPlayerService.Difficulty> difficulties = Map.of(playerId, AiPlayerService.Difficulty.MEDIUM);
            return aiPlayerService.executeAiTurn(sessionId, difficulties);

        } catch (Exception e) {
            log.warn("AI turn execution failed for player {} in session {}: {}",
                    playerId, sessionId, e.getMessage());
            return null;
        } finally {
            executingAutoTurns.remove(key);
        }
    }

    /**
     * Executes AI turns for all players with delegation enabled in a session.
     *
     * @param sessionId the game session ID
     * @return the game state after all AI actions
     */
    public GameStateResponse executeAllDelegatedTurns(String sessionId) {
        GameStateResponse lastState = null;

        // Find all players with delegation enabled in this session
        for (String key : delegationSettings.keySet()) {
            if (key.startsWith(sessionId + "_")) {
                Long playerId = parsePlayerIdFromKey(key);
                AiDelegationSettings settings = delegationSettings.get(key);
                if (settings.isEmpireAiEnabled()) {
                    lastState = executeAiTurn(sessionId, playerId);
                }
            }
        }

        return lastState;
    }

    /**
     * Gets the delegation status summary for a player.
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     * @return a map with delegation status information
     */
    public Map<String, Object> getDelegationStatus(String sessionId, Long playerId) {
        AiDelegationSettings settings = getDelegationSettings(sessionId, playerId);

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("empireAiEnabled", settings.isEmpireAiEnabled());
        status.put("delegatedCount", settings.getDelegatedCount());
        status.put("totalAreas", settings.getTotalAreas());

        Map<String, Boolean> areas = new LinkedHashMap<>();
        for (AiDelegationArea area : AiDelegationArea.values()) {
            areas.put(area.name(), settings.isDelegated(area));
        }
        status.put("areas", areas);

        return status;
    }

    /**
     * Removes delegation settings for a player (e.g., when leaving a session).
     *
     * @param sessionId the game session ID
     * @param playerId the player's ID
     */
    public void removeDelegationSettings(String sessionId, Long playerId) {
        String key = delegationKey(sessionId, playerId);
        delegationSettings.remove(key);
    }

    /**
     * Removes all delegation settings for a session.
     *
     * @param sessionId the game session ID
     */
    public void removeSessionDelegation(String sessionId) {
        delegationSettings.keySet().removeIf(key -> key.startsWith(sessionId + "_"));
    }

    /**
     * Returns the delegation area for a given action name.
     *
     * @param actionName the action method name
     * @return the delegation area, or null if not mapped
     */
    public static AiDelegationArea getAreaForAction(String actionName) {
        return ACTION_TO_AREA.get(actionName);
    }

    /**
     * Returns all mapped action names for a given delegation area.
     *
     * @param area the delegation area
     * @return set of action names mapped to this area
     */
    public static Set<String> getActionsForArea(AiDelegationArea area) {
        Objects.requireNonNull(area, "Area must not be null");
        Set<String> actions = new HashSet<>();
        for (Map.Entry<String, AiDelegationArea> entry : ACTION_TO_AREA.entrySet()) {
            if (entry.getValue() == area) {
                actions.add(entry.getKey());
            }
        }
        return actions;
    }

    // ---- Private helpers ----

    private String delegationKey(String sessionId, Long playerId) {
        return sessionId + "_" + playerId;
    }

    private Long parsePlayerIdFromKey(String key) {
        int underscore = key.lastIndexOf('_');
        if (underscore >= 0 && underscore < key.length() - 1) {
            try {
                return Long.parseLong(key.substring(underscore + 1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
