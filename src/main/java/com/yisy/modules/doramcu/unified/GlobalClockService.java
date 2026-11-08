package com.yisy.modules.doramcu.unified;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring-managed service that provides global clock instances for each game session.
 * <p>
 * Each game session gets its own {@link GlobalClock} instance, initialized with
 * the default starting date for its game type. The clock tracks turns (days)
 * and provides formatted dates for storytelling and narrative features.
 * </p>
 * <p>
 * The clock is thread-safe and can be accessed concurrently by multiple players
 * and AI systems.
 * </p>
 */
@Service
public class GlobalClockService {

    private static final Logger log = LoggerFactory.getLogger(GlobalClockService.class);

    /** Map from session ID to its GlobalClock instance. */
    private final Map<String, GlobalClock> sessionClocks = new ConcurrentHashMap<>();

    /**
     * Creates or retrieves the global clock for a game session.
     * <p>
     * If the session already has a clock, it returns the existing one.
     * Otherwise, it creates a new clock with the default starting date
     * for the given game type.
     * </p>
     *
     * @param sessionId the session identifier
     * @param gameType  the game type for the session
     * @return the GlobalClock for this session
     */
    public GlobalClock getOrCreateClock(String sessionId, GameType gameType) {
        return sessionClocks.computeIfAbsent(sessionId, id -> {
            GlobalClock clock = GlobalClock.forGameType(gameType);
            log.info("Created global clock for session {}: starting date {} ({})",
                sessionId, clock.getStartDate(), GameCalendarDefaults.getEraDescription(gameType));
            return clock;
        });
    }

    /**
     * Creates a new global clock for a session with a custom starting date.
     * <p>
     * Use this when a campaign or story requires a specific starting date
     * that differs from the game type's default.
     * </p>
     *
     * @param sessionId the session identifier
     * @param startDate the custom starting date
     * @return the new GlobalClock instance
     */
    public GlobalClock createClockWithDate(String sessionId, LocalDate startDate) {
        GlobalClock clock = new GlobalClock(startDate);
        sessionClocks.put(sessionId, clock);
        log.info("Created global clock for session {} with custom start date: {}",
            sessionId, startDate);
        return clock;
    }

    /**
     * Returns the global clock for a session, if one exists.
     *
     * @param sessionId the session identifier
     * @return the GlobalClock, or null if not found
     */
    public GlobalClock getClock(String sessionId) {
        return sessionClocks.get(sessionId);
    }

    /**
     * Advances the clock for a session by one turn (one day).
     *
     * @param sessionId the session identifier
     * @return the new turn number, or -1 if no clock exists
     */
    public int advanceTurn(String sessionId) {
        GlobalClock clock = sessionClocks.get(sessionId);
        if (clock == null) {
            log.warn("No global clock found for session: {}", sessionId);
            return -1;
        }
        return clock.advanceTurn();
    }

    /**
     * Removes the global clock for a session (e.g., when the session ends).
     *
     * @param sessionId the session identifier
     */
    public void removeClock(String sessionId) {
        GlobalClock removed = sessionClocks.remove(sessionId);
        if (removed != null) {
            log.info("Removed global clock for session {} (final turn: {})",
                sessionId, removed.getCurrentTurn());
        }
    }

    /**
     * Returns the number of active clocks (sessions with clocks).
     *
     * @return the number of active clocks
     */
    public int getActiveClockCount() {
        return sessionClocks.size();
    }

    /**
     * Returns a summary of all active clocks.
     *
     * @return a map of session IDs to clock summaries
     */
    public Map<String, String> getAllClockSummaries() {
        Map<String, String> summaries = new ConcurrentHashMap<>();
        sessionClocks.forEach((sessionId, clock) ->
            summaries.put(sessionId, clock.getSummary()));
        return summaries;
    }
}
