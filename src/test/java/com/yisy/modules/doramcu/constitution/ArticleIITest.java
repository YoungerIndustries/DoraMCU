package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleIITest {

    private final ArticleII article = new ArticleII();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article II number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_II, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("The Executive Branch", article.getTitle());
    }

    @Test
    @DisplayName("Should have 4 sections")
    void testSectionCount() {
        assertEquals(4, article.getSectionCount());
    }

    // ---- Executive powers tests ----

    @Test
    @DisplayName("Should have 8 executive powers")
    void testExecutivePowersCount() {
        assertEquals(8, article.getExecutivePowers().size());
    }

    @Test
    @DisplayName("Should include commander in chief power")
    void testCommanderInChief() {
        List<ArticleII.ExecutivePower> powers = article.getExecutivePowers();
        assertTrue(powers.contains(ArticleII.ExecutivePower.COMMANDER_IN_CHIEF));
    }

    @Test
    @DisplayName("Should include treaties power")
    void testTreatiesPower() {
        List<ArticleII.ExecutivePower> powers = article.getExecutivePowers();
        assertTrue(powers.contains(ArticleII.ExecutivePower.TREATIES));
    }

    @Test
    @DisplayName("Should include pardons power")
    void testReprievesPower() {
        List<ArticleII.ExecutivePower> powers = article.getExecutivePowers();
        assertTrue(powers.contains(ArticleII.ExecutivePower.REPRIEVES));
    }

    @Test
    @DisplayName("Each power should have a description")
    void testPowerDescriptions() {
        for (ArticleII.ExecutivePower power : ArticleII.ExecutivePower.values()) {
            assertNotNull(power.getDescription());
            assertFalse(power.getDescription().isEmpty());
        }
    }

    // ---- Presidential duties tests ----

    @Test
    @DisplayName("Should have 7 presidential duties")
    void testPresidentialDutiesCount() {
        assertEquals(7, article.getPresidentialDuties().size());
    }

    @Test
    @DisplayName("Should include state of the union duty")
    void testStateOfUnion() {
        List<ArticleII.PresidentialDuty> duties = article.getPresidentialDuties();
        assertTrue(duties.contains(ArticleII.PresidentialDuty.STATE_OF_UNION));
    }

    @Test
    @DisplayName("Should include faithful execution duty")
    void testFaithfulExecution() {
        List<ArticleII.PresidentialDuty> duties = article.getPresidentialDuties();
        assertTrue(duties.contains(ArticleII.PresidentialDuty.FAITHFUL_EXECUTION));
    }

    @Test
    @DisplayName("Each duty should have a description")
    void testDutyDescriptions() {
        for (ArticleII.PresidentialDuty duty : ArticleII.PresidentialDuty.values()) {
            assertNotNull(duty.getDescription());
            assertFalse(duty.getDescription().isEmpty());
        }
    }

    // ---- Presidential qualifications tests ----

    @Test
    @DisplayName("President minimum age should be 35")
    void testPresidentMinimumAge() {
        assertEquals(35, article.getPresidentMinimumAge());
    }

    @Test
    @DisplayName("Should require natural-born citizen")
    void testNaturalBornCitizen() {
        assertTrue(article.requiresNaturalBornCitizen());
    }

    @Test
    @DisplayName("President residency should be 14 years")
    void testPresidentResidencyYears() {
        assertEquals(14, article.getPresidentResidencyYears());
    }

    @Test
    @DisplayName("Presidential term should be 4 years")
    void testPresidentialTermYears() {
        assertEquals(4, article.getPresidentialTermYears());
    }

    @Test
    @DisplayName("President age should be higher than senator age")
    void testAgeComparisonWithArticleI() {
        ArticleI articleI = new ArticleI();
        assertTrue(article.getPresidentMinimumAge() > articleI.getSenatorMinimumAge());
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("Executive"));
    }
}
