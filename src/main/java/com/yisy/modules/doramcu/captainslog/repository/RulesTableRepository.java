package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.RulesTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying probability matrix (oracle table) entries from the Captain's Log rulebook.
 */
@Repository
public interface RulesTableRepository extends JpaRepository<RulesTable, Long> {

    /** Find all active entries for a specific table. */
    List<RulesTable> findByTableNameAndActiveTrueOrderByRollValueAsc(String tableName);

    /** Find all active entries by category. */
    List<RulesTable> findByCategoryAndActiveTrue(String category);

    /** Find all active entries by table name and category. */
    List<RulesTable> findByTableNameAndCategoryAndActiveTrue(String tableName, String category);

    /** Get all distinct table names. */
    @Query("SELECT DISTINCT r.tableName FROM RulesTable r WHERE r.active = true")
    List<String> findDistinctTableNames();

    /** Get all distinct categories. */
    @Query("SELECT DISTINCT r.category FROM RulesTable r WHERE r.active = true")
    List<String> findDistinctCategories();

    /** Search table entries by keyword in result label or description. */
    @Query("SELECT r FROM RulesTable r WHERE r.active = true AND " +
           "(LOWER(r.resultLabel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<RulesTable> searchByKeyword(@Param("keyword") String keyword);
}
