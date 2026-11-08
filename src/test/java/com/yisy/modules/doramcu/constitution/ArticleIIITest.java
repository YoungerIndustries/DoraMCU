package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleIIITest {

    private final ArticleIII article = new ArticleIII();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article III number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_III, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("The Judicial Branch", article.getTitle());
    }

    @Test
    @DisplayName("Should have 3 sections")
    void testSectionCount() {
        assertEquals(3, article.getSectionCount());
    }

    // ---- Jurisdiction tests ----

    @Test
    @DisplayName("Should have 10 jurisdiction types")
    void testJurisdictionTypesCount() {
        assertEquals(10, article.getJurisdictionTypes().size());
    }

    @Test
    @DisplayName("Should include Constitution jurisdiction")
    void testConstitutionJurisdiction() {
        List<ArticleIII.JurisdictionType> types = article.getJurisdictionTypes();
        assertTrue(types.contains(ArticleIII.JurisdictionType.CONSTITUTION));
    }

    @Test
    @DisplayName("Should include interstate disputes jurisdiction")
    void testInterstateDisputesJurisdiction() {
        List<ArticleIII.JurisdictionType> types = article.getJurisdictionTypes();
        assertTrue(types.contains(ArticleIII.JurisdictionType.INTERSTATE_DISPUTES));
    }

    @Test
    @DisplayName("Each jurisdiction type should have a description")
    void testJurisdictionDescriptions() {
        for (ArticleIII.JurisdictionType type : ArticleIII.JurisdictionType.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
        }
    }

    // ---- Treason elements tests ----

    @Test
    @DisplayName("Should have 4 treason elements")
    void testTreasonElementsCount() {
        assertEquals(4, article.getTreasonElements().size());
    }

    @Test
    @DisplayName("Should include league with enemy element")
    void testLeagueWithEnemy() {
        List<ArticleIII.TreasonElement> elements = article.getTreasonElements();
        assertTrue(elements.contains(ArticleIII.TreasonElement.LEAGUE_WITH_ENEMY));
    }

    @Test
    @DisplayName("Should include two witnesses element")
    void testTwoWitnesses() {
        List<ArticleIII.TreasonElement> elements = article.getTreasonElements();
        assertTrue(elements.contains(ArticleIII.TreasonElement.TWO_WITNESSES));
    }

    @Test
    @DisplayName("Each treason element should have a description")
    void testTreasonDescriptions() {
        for (ArticleIII.TreasonElement element : ArticleIII.TreasonElement.values()) {
            assertNotNull(element.getDescription());
            assertFalse(element.getDescription().isEmpty());
        }
    }

    // ---- Trial by jury tests ----

    @Test
    @DisplayName("Should have 2 trial by jury requirements")
    void testTrialByJuryRequirementsCount() {
        assertEquals(2, article.getTrialByJuryRequirements().size());
    }

    @Test
    @DisplayName("Should include criminal trials requirement")
    void testCriminalTrials() {
        List<ArticleIII.TrialByJuryRequirement> requirements = article.getTrialByJuryRequirements();
        assertTrue(requirements.contains(ArticleIII.TrialByJuryRequirement.CRIMINAL_TRIALS));
    }

    @Test
    @DisplayName("Each trial by jury requirement should have a description")
    void testTrialByJuryDescriptions() {
        for (ArticleIII.TrialByJuryRequirement req : ArticleIII.TrialByJuryRequirement.values()) {
            assertNotNull(req.getDescription());
            assertFalse(req.getDescription().isEmpty());
        }
    }

    // ---- Judicial tenure tests ----

    @Test
    @DisplayName("Judges should have life tenure")
    void testLifeTenure() {
        assertTrue(article.hasLifeTenure());
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("3"));
        assertTrue(str.contains("Judicial"));
    }
}
