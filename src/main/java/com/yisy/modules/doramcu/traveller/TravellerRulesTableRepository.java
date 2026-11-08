package com.yisy.modules.doramcu.traveller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying Classic Traveller rules tables.
 */
@Repository
public interface TravellerRulesTableRepository extends JpaRepository<TravellerRulesTable, Long> {

    List<TravellerRulesTable> findByTableNameAndActiveTrue(String tableName);

    List<TravellerRulesTable> findByCategoryAndActiveTrue(String category);

    List<TravellerRulesTable> findByTableNameAndCategoryAndActiveTrue(String tableName, String category);

    List<TravellerRulesTable> findAllByActiveTrue();

    @Query("SELECT DISTINCT t.category FROM TravellerRulesTable t WHERE t.active = true")
    List<String> findDistinctCategories();
}
