package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.PersistedGameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersistedGameSessionRepository extends JpaRepository<PersistedGameSession, Long> {

    Optional<PersistedGameSession> findBySessionId(String sessionId);

    List<PersistedGameSession> findByLastActivityAtBefore(LocalDateTime threshold);

    @Modifying
    @Query("DELETE FROM PersistedGameSession p WHERE p.lastActivityAt < :threshold")
    int deleteByLastActivityAtBefore(@Param("threshold") LocalDateTime threshold);
}
