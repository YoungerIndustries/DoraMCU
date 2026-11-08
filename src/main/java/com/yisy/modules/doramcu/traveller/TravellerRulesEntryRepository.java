package com.yisy.modules.doramcu.traveller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying Classic Traveller rules entries.
 */
@Repository
public interface TravellerRulesEntryRepository extends JpaRepository<TravellerRulesEntry, Long> {

    List<TravellerRulesEntry> findByCategoryAndActiveTrue(String category);

    List<TravellerRulesEntry> findByChapterAndActiveTrue(String chapter);

    List<TravellerRulesEntry> findBySourceAndActiveTrue(String source);

    List<TravellerRulesEntry> findByCategoryAndChapterAndActiveTrue(String category, String chapter);

    @Query("SELECT r FROM TravellerRulesEntry r WHERE r.active = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.text) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TravellerRulesEntry> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM TravellerRulesEntry r WHERE r.active = true AND r.category = :category AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.text) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TravellerRulesEntry> searchByKeywordInCategory(@Param("category") String category, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT r.category FROM TravellerRulesEntry r WHERE r.active = true")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT r.source FROM TravellerRulesEntry r WHERE r.active = true")
    List<String> findDistinctSources();
}
