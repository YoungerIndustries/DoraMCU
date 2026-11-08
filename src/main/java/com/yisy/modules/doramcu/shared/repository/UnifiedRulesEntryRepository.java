package com.yisy.modules.doramcu.shared.repository;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Shared repository for unified rules entries across all game systems.
 * Renamed from RulesEntryRepository to avoid bean name conflict with Captain's Log.
 */
public interface UnifiedRulesEntryRepository extends JpaRepository<RulesEntry, Long> {

    /** Find all active entries for a specific game system. */
    List<RulesEntry> findByGameSystemAndActiveTrue(GameSystem gameSystem);

    /** Find entries by category within a game system. */
    List<RulesEntry> findByGameSystemAndCategoryAndActiveTrue(GameSystem gameSystem, String category);

    /** Find entries by chapter within a game system. */
    List<RulesEntry> findByGameSystemAndChapterAndActiveTrue(GameSystem gameSystem, String chapter);

    /** Find entries by source book within a game system. */
    List<RulesEntry> findByGameSystemAndSourceAndActiveTrue(GameSystem gameSystem, String source);

    /** Search entries by title keyword within a game system. */
    @Query("SELECT r FROM UnifiedRulesEntry r WHERE r.gameSystem = :gameSystem AND r.active = true AND LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RulesEntry> searchByTitle(@Param("gameSystem") GameSystem gameSystem, @Param("keyword") String keyword);

    /** Search entries by text content within a game system. */
    @Query("SELECT r FROM UnifiedRulesEntry r WHERE r.gameSystem = :gameSystem AND r.active = true AND LOWER(r.text) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RulesEntry> searchByText(@Param("gameSystem") GameSystem gameSystem, @Param("keyword") String keyword);

    /** Find all active entries across all game systems. */
    List<RulesEntry> findByActiveTrue();
}
