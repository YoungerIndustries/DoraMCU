package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleITest {

    private final ArticleI article = new ArticleI();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article I number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_I, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("The Legislative Branch", article.getTitle());
    }

    @Test
    @DisplayName("Should have 10 sections")
    void testSectionCount() {
        assertEquals(10, article.getSectionCount());
    }

    @Test
    @DisplayName("Should have non-empty description")
    void testDescription() {
        assertNotNull(article.getDescription());
        assertFalse(article.getDescription().isEmpty());
        assertTrue(article.getDescription().contains("bicameral"));
    }

    // ---- Congressional powers tests ----

    @Test
    @DisplayName("Should have 15 congressional powers")
    void testCongressionalPowersCount() {
        assertEquals(15, article.getCongressionalPowers().size());
    }

    @Test
    @DisplayName("Should include taxation power")
    void testTaxationPower() {
        List<ArticleI.CongressionalPower> powers = article.getCongressionalPowers();
        assertTrue(powers.contains(ArticleI.CongressionalPower.TAXATION));
    }

    @Test
    @DisplayName("Should include declare war power")
    void testDeclareWarPower() {
        List<ArticleI.CongressionalPower> powers = article.getCongressionalPowers();
        assertTrue(powers.contains(ArticleI.CongressionalPower.DECLARE_WAR));
    }

    @Test
    @DisplayName("Should include necessary and proper power")
    void testNecessaryAndProperPower() {
        List<ArticleI.CongressionalPower> powers = article.getCongressionalPowers();
        assertTrue(powers.contains(ArticleI.CongressionalPower.NECESSARY_AND_PROPER));
    }

    @Test
    @DisplayName("Each power should have a description")
    void testPowerDescriptions() {
        for (ArticleI.CongressionalPower power : ArticleI.CongressionalPower.values()) {
            assertNotNull(power.getDescription());
            assertFalse(power.getDescription().isEmpty());
        }
    }

    // ---- Congressional limits tests ----

    @Test
    @DisplayName("Should have 9 congressional limits")
    void testCongressionalLimitsCount() {
        assertEquals(9, article.getCongressionalLimits().size());
    }

    @Test
    @DisplayName("Should include habeas corpus limit")
    void testHabeasCorpusLimit() {
        List<ArticleI.CongressionalLimit> limits = article.getCongressionalLimits();
        assertTrue(limits.contains(ArticleI.CongressionalLimit.HABEAS_CORPUS));
    }

    @Test
    @DisplayName("Should include religious test prohibition")
    void testReligiousTestLimit() {
        List<ArticleI.CongressionalLimit> limits = article.getCongressionalLimits();
        assertTrue(limits.contains(ArticleI.CongressionalLimit.RELIGIOUS_TEST));
    }

    @Test
    @DisplayName("Each limit should have a description")
    void testLimitDescriptions() {
        for (ArticleI.CongressionalLimit limit : ArticleI.CongressionalLimit.values()) {
            assertNotNull(limit.getDescription());
            assertFalse(limit.getDescription().isEmpty());
        }
    }

    // ---- State prohibitions tests ----

    @Test
    @DisplayName("Should have 8 state prohibitions")
    void testStateProhibitionsCount() {
        assertEquals(8, article.getStateProhibitions().size());
    }

    @Test
    @DisplayName("Should include treaties prohibition")
    void testTreatiesProhibition() {
        List<ArticleI.StateProhibition> prohibitions = article.getStateProhibitions();
        assertTrue(prohibitions.contains(ArticleI.StateProhibition.TREATIES));
    }

    @Test
    @DisplayName("Each prohibition should have a description")
    void testProhibitionDescriptions() {
        for (ArticleI.StateProhibition prohibition : ArticleI.StateProhibition.values()) {
            assertNotNull(prohibition.getDescription());
            assertFalse(prohibition.getDescription().isEmpty());
        }
    }

    // ---- Member qualifications tests ----

    @Test
    @DisplayName("Representative minimum age should be 25")
    void testRepresentativeMinimumAge() {
        assertEquals(25, article.getRepresentativeMinimumAge());
    }

    @Test
    @DisplayName("Representative citizenship years should be 7")
    void testRepresentativeCitizenshipYears() {
        assertEquals(7, article.getRepresentativeCitizenshipYears());
    }

    @Test
    @DisplayName("Senator minimum age should be 30")
    void testSenatorMinimumAge() {
        assertEquals(30, article.getSenatorMinimumAge());
    }

    @Test
    @DisplayName("Senator citizenship years should be 9")
    void testSenatorCitizenshipYears() {
        assertEquals(9, article.getSenatorCitizenshipYears());
    }

    @Test
    @DisplayName("Senator term should be 6 years")
    void testSenatorTermYears() {
        assertEquals(6, article.getSenatorTermYears());
    }

    @Test
    @DisplayName("Representative term should be 2 years")
    void testRepresentativeTermYears() {
        assertEquals(2, article.getRepresentativeTermYears());
    }

    @Test
    @DisplayName("Senator age requirement should be higher than representative")
    void testAgeComparison() {
        assertTrue(article.getSenatorMinimumAge() > article.getRepresentativeMinimumAge());
    }

    @Test
    @DisplayName("Senator citizenship requirement should be longer than representative")
    void testCitizenshipComparison() {
        assertTrue(article.getSenatorCitizenshipYears() > article.getRepresentativeCitizenshipYears());
    }

    // ---- Title setter test ----

    @Test
    @DisplayName("Should allow setting custom title")
    void testSetTitle() {
        article.setTitle("Custom Title");
        assertEquals("Custom Title", article.getTitle());
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("Legislative"));
    }
}
