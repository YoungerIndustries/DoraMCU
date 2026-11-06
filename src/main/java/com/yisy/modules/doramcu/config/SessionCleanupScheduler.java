package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled service that cleans up abandoned game sessions.
 * <p>
 * Runs periodically to remove persisted sessions that have been inactive
 * for longer than the configured TTL. Also periodically saves in-memory
 * session state to the database.
 * </p>
 */
@Service
public class SessionCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupScheduler.class);

    private final PersistedGameSessionRepository persistedGameSessionRepository;

    @Value("${gamesession.ttl-hours:24}")
    private int ttlHours;

    public SessionCleanupScheduler(PersistedGameSessionRepository persistedGameSessionRepository) {
        this.persistedGameSessionRepository = persistedGameSessionRepository;
    }

    /**
     * Clean up sessions that have been inactive for longer than the TTL.
     * Runs every hour.
     */
    @Scheduled(fixedRateString = "PT1H")
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(ttlHours);
        int deleted = persistedGameSessionRepository.deleteByLastActivityAtBefore(threshold);
        if (deleted > 0) {
            log.info("Cleaned up {} expired game sessions (inactive for >{} hours)", deleted, ttlHours);
        }
    }

    /**
     * Log a warning if there are many persisted sessions.
     * Runs every 6 hours.
     */
    @Scheduled(fixedRateString = "PT6H")
    public void logSessionCount() {
        long count = persistedGameSessionRepository.count();
        log.debug("Total persisted game sessions: {}", count);
        if (count > 1000) {
            log.warn("High number of persisted game sessions: {}. Consider increasing cleanup frequency.", count);
        }
    }
}
