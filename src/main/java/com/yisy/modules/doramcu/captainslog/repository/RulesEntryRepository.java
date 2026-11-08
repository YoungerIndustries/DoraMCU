package com.yisy.modules.doramcu.captainslog.repository;

import com.yisy.modules.doramcu.captainslog.model.RulesEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying rules entries from the Captain's Log rulebook.
 */
@Repository
public interface RulesEntryRepository extends JpaRepository<RulesEntry, Long> {

    /** Find all active rules by category. */
    List<RulesEntry> findByCategoryAndActiveTrue(String category);

    /** Find all active rules by chapter. */
    List<RulesEntry> findByChapterAndActiveTrue(String chapter);

    /** Find all active rules by category and chapter. */
    List<RulesEntry> findByCategoryAndChapterAndActiveTrue(String category, String chapter);

    /** Search rules by keyword in title or text (case-insensitive). */
    @Query("SELECT r FROM RulesEntry r WHERE r.active = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.text) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<RulesEntry> searchByKeyword(@Param("keyword") String keyword);

    /** Search rules by keyword within a specific category. */
    @Query("SELECT r FROM RulesEntry r WHERE r.active = true AND r.category = :category AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.text) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<RulesEntry> searchByKeywordInCategory(@Param("category") String category, @Param("keyword") String keyword);

    /** Get all distinct categories. */
    @Query("SELECT DISTINCT r.category FROM RulesEntry r WHERE r.active = true")
    List<String> findDistinctCategories();

    /** Get all distinct chapters. */
    @Query("SELECT DISTINCT r.chapter FROM RulesEntry r WHERE r.active = true")
    List<String> findDistinctChapters();
}
