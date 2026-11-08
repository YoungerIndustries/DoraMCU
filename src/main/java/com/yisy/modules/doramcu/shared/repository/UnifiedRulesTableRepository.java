package com.yisy.modules.doramcu.shared.repository;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Shared repository for unified rules tables across all game systems.
 * Renamed from RulesTableRepository to avoid bean name conflict with Captain's Log.
 */
public interface UnifiedRulesTableRepository extends JpaRepository<RulesTable, Long> {

    /** Find all active table entries for a specific game system and table name. */
    List<RulesTable> findByGameSystemAndTableNameAndActiveTrue(GameSystem gameSystem, String tableName);

    /** Find all active table entries for a specific game system. */
    List<RulesTable> findByGameSystemAndActiveTrue(GameSystem gameSystem);

    /** Find table entries by category within a game system. */
    List<RulesTable> findByGameSystemAndCategoryAndActiveTrue(GameSystem gameSystem, String category);

    /** Find table entries by roll value within a game system and table. */
    List<RulesTable> findByGameSystemAndTableNameAndRollValueAndActiveTrue(
            GameSystem gameSystem, String tableName, int rollValue);

    /** Find all active table entries across all game systems. */
    List<RulesTable> findByActiveTrue();
}
