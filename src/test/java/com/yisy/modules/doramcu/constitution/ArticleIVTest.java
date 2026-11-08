package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleIVTest {

    private final ArticleIV article = new ArticleIV();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article IV number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_IV, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("The States, Citizenship, New States", article.getTitle());
    }

    @Test
    @DisplayName("Should have 4 sections")
    void testSectionCount() {
        assertEquals(4, article.getSectionCount());
    }

    // ---- Full Faith and Credit tests ----

    @Test
    @DisplayName("Should have 2 full faith and credit rules")
    void testFullFaithAndCreditCount() {
        assertEquals(2, article.getFullFaithAndCreditRules().size());
    }

    @Test
    @DisplayName("Should include public acts rule")
    void testPublicActsRule() {
        List<ArticleIV.FullFaithAndCredit> rules = article.getFullFaithAndCreditRules();
        assertTrue(rules.contains(ArticleIV.FullFaithAndCredit.PUBLIC_ACTS));
    }

    @Test
    @DisplayName("Each rule should have a description")
    void testFullFaithDescriptions() {
        for (ArticleIV.FullFaithAndCredit rule : ArticleIV.FullFaithAndCredit.values()) {
            assertNotNull(rule.getDescription());
            assertFalse(rule.getDescription().isEmpty());
        }
    }

    // ---- Privileges and Immunities tests ----

    @Test
    @DisplayName("Should have 4 privileges and immunities rules")
    void testPrivilegesAndImmunitiesCount() {
        assertEquals(4, article.getPrivilegesAndImmunities().size());
    }

    @Test
    @DisplayName("Should include citizen privileges")
    void testCitizenPrivileges() {
        List<ArticleIV.PrivilegesAndImmunities> rules = article.getPrivilegesAndImmunities();
        assertTrue(rules.contains(ArticleIV.PrivilegesAndImmunities.CITIZEN_PRIVILEGES));
    }

    @Test
    @DisplayName("Should include extradition rule")
    void testExtradition() {
        List<ArticleIV.PrivilegesAndImmunities> rules = article.getPrivilegesAndImmunities();
        assertTrue(rules.contains(ArticleIV.PrivilegesAndImmunities.EXtradition));
    }

    @Test
    @DisplayName("Each rule should have a description")
    void testPrivilegesDescriptions() {
        for (ArticleIV.PrivilegesAndImmunities rule : ArticleIV.PrivilegesAndImmunities.values()) {
            assertNotNull(rule.getDescription());
            assertFalse(rule.getDescription().isEmpty());
        }
    }

    // ---- New State Rules tests ----

    @Test
    @DisplayName("Should have 4 new state rules")
    void testNewStateRulesCount() {
        assertEquals(4, article.getNewStateRules().size());
    }

    @Test
    @DisplayName("Should include new states admission rule")
    void testNewStatesAdmission() {
        List<ArticleIV.NewStateRules> rules = article.getNewStateRules();
        assertTrue(rules.contains(ArticleIV.NewStateRules.NEW_STATES));
    }

    @Test
    @DisplayName("Each rule should have a description")
    void testNewStateDescriptions() {
        for (ArticleIV.NewStateRules rule : ArticleIV.NewStateRules.values()) {
            assertNotNull(rule.getDescription());
            assertFalse(rule.getDescription().isEmpty());
        }
    }

    // ---- Federal Guarantees tests ----

    @Test
    @DisplayName("Should have 3 federal guarantees")
    void testFederalGuaranteesCount() {
        assertEquals(3, article.getFederalGuarantees().size());
    }

    @Test
    @DisplayName("Should include republican form guarantee")
    void testRepublicanForm() {
        List<ArticleIV.FederalGuarantee> guarantees = article.getFederalGuarantees();
        assertTrue(guarantees.contains(ArticleIV.FederalGuarantee.REPUBLICAN_FORM));
    }

    @Test
    @DisplayName("Should include protection against invasion")
    void testProtectionAgainstInvasion() {
        List<ArticleIV.FederalGuarantee> guarantees = article.getFederalGuarantees();
        assertTrue(guarantees.contains(ArticleIV.FederalGuarantee.PROTECTION_AGAINST_INVASION));
    }

    @Test
    @DisplayName("Each guarantee should have a description")
    void testGuaranteeDescriptions() {
        for (ArticleIV.FederalGuarantee guarantee : ArticleIV.FederalGuarantee.values()) {
            assertNotNull(guarantee.getDescription());
            assertFalse(guarantee.getDescription().isEmpty());
        }
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("4"));
        assertTrue(str.contains("States"));
    }
}
