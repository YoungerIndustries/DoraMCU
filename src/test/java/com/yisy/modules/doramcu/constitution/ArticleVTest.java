package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleVTest {

    private final ArticleV article = new ArticleV();

    // ---- Base article tests ----

    @Test
    @DisplayName("Should have Article V number")
    void testArticleNumber() {
        assertEquals(Article.ArticleNumber.ARTICLE_V, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should have correct title")
    void testTitle() {
        assertEquals("Amendment Process", article.getTitle());
    }

    @Test
    @DisplayName("Should have 1 section")
    void testSectionCount() {
        assertEquals(1, article.getSectionCount());
    }

    // ---- Proposal methods tests ----

    @Test
    @DisplayName("Should have 2 proposal methods")
    void testProposalMethodsCount() {
        assertEquals(2, article.getProposalMethods().size());
    }

    @Test
    @DisplayName("Should include congressional vote method")
    void testCongressionalVote() {
        List<ArticleV.ProposalMethod> methods = article.getProposalMethods();
        assertTrue(methods.contains(ArticleV.ProposalMethod.CONGRESSIONAL_VOTE));
    }

    @Test
    @DisplayName("Should include convention request method")
    void testConventionRequest() {
        List<ArticleV.ProposalMethod> methods = article.getProposalMethods();
        assertTrue(methods.contains(ArticleV.ProposalMethod.CONVENTION_REQUEST));
    }

    @Test
    @DisplayName("Each method should have a description")
    void testProposalDescriptions() {
        for (ArticleV.ProposalMethod method : ArticleV.ProposalMethod.values()) {
            assertNotNull(method.getDescription());
            assertFalse(method.getDescription().isEmpty());
        }
    }

    // ---- Ratification methods tests ----

    @Test
    @DisplayName("Should have 2 ratification methods")
    void testRatificationMethodsCount() {
        assertEquals(2, article.getRatificationMethods().size());
    }

    @Test
    @DisplayName("Should include state legislatures method")
    void testStateLegislatures() {
        List<ArticleV.RatificationMethod> methods = article.getRatificationMethods();
        assertTrue(methods.contains(ArticleV.RatificationMethod.STATE_LEGISLATURES));
    }

    @Test
    @DisplayName("Should include state conventions method")
    void testStateConventions() {
        List<ArticleV.RatificationMethod> methods = article.getRatificationMethods();
        assertTrue(methods.contains(ArticleV.RatificationMethod.STATE_CONVENTIONS));
    }

    @Test
    @DisplayName("Each method should have a description")
    void testRatificationDescriptions() {
        for (ArticleV.RatificationMethod method : ArticleV.RatificationMethod.values()) {
            assertNotNull(method.getDescription());
            assertFalse(method.getDescription().isEmpty());
        }
    }

    // ---- Amendment constraints tests ----

    @Test
    @DisplayName("Should have 2 amendment constraints")
    void testAmendmentConstraintsCount() {
        assertEquals(2, article.getAmendmentConstraints().size());
    }

    @Test
    @DisplayName("Should include equal suffrage protection")
    void testEqualSuffrage() {
        List<ArticleV.AmendmentConstraint> constraints = article.getAmendmentConstraints();
        assertTrue(constraints.contains(ArticleV.AmendmentConstraint.DIRECT_TAX_PROTECTION));
    }

    @Test
    @DisplayName("Each constraint should have a description")
    void testConstraintDescriptions() {
        for (ArticleV.AmendmentConstraint constraint : ArticleV.AmendmentConstraint.values()) {
            assertNotNull(constraint.getDescription());
            assertFalse(constraint.getDescription().isEmpty());
        }
    }

    // ---- Fraction tests ----

    @Test
    @DisplayName("Congress proposal fraction should be 2/3")
    void testCongressProposalFraction() {
        assertEquals(2.0 / 3.0, article.getCongressProposalFraction(), 0.001);
    }

    @Test
    @DisplayName("State ratification fraction should be 3/4")
    void testStateRatificationFraction() {
        assertEquals(3.0 / 4.0, article.getStateRatificationFraction(), 0.001);
    }

    @Test
    @DisplayName("Ratification fraction should be higher than proposal fraction")
    void testFractionComparison() {
        assertTrue(article.getStateRatificationFraction() > article.getCongressProposalFraction());
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should contain article number and title")
    void testToString() {
        String str = article.toString();
        assertTrue(str.contains("Article{"));
        assertTrue(str.contains("5"));
        assertTrue(str.contains("Amendment"));
    }
}
