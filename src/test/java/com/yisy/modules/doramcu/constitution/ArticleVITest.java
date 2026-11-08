package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleVITest {

    private final ArticleVI article = new ArticleVI();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article VI number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_VI, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("Debts, Supremacy, Oaths", article.getTitle());
    }

    @Test
    @DisplayName("Should have 3 sections")
    void testSectionCount() {
        assertEquals(3, article.getSectionCount());
    }

    // ---- Debt provisions tests ----

    @Test
    @DisplayName("Should have 1 debt provision")
    void testDebtProvisionsCount() {
        assertEquals(1, article.getDebtProvisions().size());
    }

    @Test
    @DisplayName("Should include confederation debts provision")
    void testConfederationDebts() {
        List<ArticleVI.DebtProvision> provisions = article.getDebtProvisions();
        assertTrue(provisions.contains(ArticleVI.DebtProvision.CONFEDERATION_DEBTS));
    }

    @Test
    @DisplayName("Each provision should have a description")
    void testDebtDescriptions() {
        for (ArticleVI.DebtProvision provision : ArticleVI.DebtProvision.values()) {
            assertNotNull(provision.getDescription());
            assertFalse(provision.getDescription().isEmpty());
        }
    }

    // ---- Supremacy clause tests ----

    @Test
    @DisplayName("Should have 3 supremacy clause provisions")
    void testSupremacyClausesCount() {
        assertEquals(3, article.getSupremacyClauses().size());
    }

    @Test
    @DisplayName("Should include constitution supreme provision")
    void testConstitutionSupreme() {
        List<ArticleVI.SupremacyClause> clauses = article.getSupremacyClauses();
        assertTrue(clauses.contains(ArticleVI.SupremacyClause.CONSTITUTION_SUPREME));
    }

    @Test
    @DisplayName("Each clause should have a description")
    void testSupremacyDescriptions() {
        for (ArticleVI.SupremacyClause clause : ArticleVI.SupremacyClause.values()) {
            assertNotNull(clause.getDescription());
            assertFalse(clause.getDescription().isEmpty());
        }
    }

    // ---- Oath requirements tests ----

    @Test
    @DisplayName("Should have 2 oath requirements")
    void testOathRequirementsCount() {
        assertEquals(2, article.getOathRequirements().size());
    }

    @Test
    @DisplayName("Should include support constitution requirement")
    void testSupportConstitution() {
        List<ArticleVI.OathRequirement> requirements = article.getOathRequirements();
        assertTrue(requirements.contains(ArticleVI.OathRequirement.SUPPORT_CONSTITUTION));
    }

    @Test
    @DisplayName("Should include no religious test requirement")
    void testNoReligiousTest() {
        List<ArticleVI.OathRequirement> requirements = article.getOathRequirements();
        assertTrue(requirements.contains(ArticleVI.OathRequirement.NO_RELIGIOUS_TEST));
    }

    @Test
    @DisplayName("Each requirement should have a description")
    void testOathDescriptions() {
        for (ArticleVI.OathRequirement requirement : ArticleVI.OathRequirement.values()) {
            assertNotNull(requirement.getDescription());
            assertFalse(requirement.getDescription().isEmpty());
        }
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("6"));
        assertTrue(str.contains("Supremacy"));
    }
}
