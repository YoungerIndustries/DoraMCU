package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleVIITest {

    private final ArticleVII article = new ArticleVII();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article VII number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_VII, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("Ratification", article.getTitle());
    }

    @Test
    @DisplayName("Should have 1 section")
    void testSectionCount() {
        assertEquals(1, article.getSectionCount());
    }

    // ---- Ratification requirements tests ----

    @Test
    @DisplayName("Should have 2 ratification requirements")
    void testRatificationRequirementsCount() {
        assertEquals(2, article.getRatificationRequirements().size());
    }

    @Test
    @DisplayName("Should include nine states requirement")
    void testNineStatesRequirement() {
        List<ArticleVII.RatificationRequirement> requirements = article.getRatificationRequirements();
        assertTrue(requirements.contains(ArticleVII.RatificationRequirement.NINE_STATES));
    }

    @Test
    @DisplayName("Should include convention method requirement")
    void testConventionMethod() {
        List<ArticleVII.RatificationRequirement> requirements = article.getRatificationRequirements();
        assertTrue(requirements.contains(ArticleVII.RatificationRequirement.CONVENTION_METHOD));
    }

    @Test
    @DisplayName("Each requirement should have a description")
    void testRequirementDescriptions() {
        for (ArticleVII.RatificationRequirement requirement : ArticleVII.RatificationRequirement.values()) {
            assertNotNull(requirement.getDescription());
            assertFalse(requirement.getDescription().isEmpty());
        }
    }

    // ---- Ratification numbers tests ----

    @Test
    @DisplayName("Should require 9 states for ratification")
    void testRequiredStatesForRatification() {
        assertEquals(9, article.getRequiredStatesForRatification());
    }

    @Test
    @DisplayName("Should have 13 original states")
    void testTotalOriginalStates() {
        assertEquals(13, article.getTotalOriginalStates());
    }

    @Test
    @DisplayName("Required states should be majority of original states")
    void testRatificationMajority() {
        assertTrue(article.getRequiredStatesForRatification() > article.getTotalOriginalStates() / 2);
    }

    @Test
    @DisplayName("Required states should be less than total original states")
    void testRatificationNotAll() {
        assertTrue(article.getRequiredStatesForRatification() < article.getTotalOriginalStates());
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("7"));
        assertTrue(str.contains("Ratification"));
    }
}
