package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstitutionTest {

    private final Constitution constitution = new Constitution();

    // ---- Construction tests ----

    @Test
    @DisplayName("Should initialize with 7 articles")
    void testArticleCount() {
        assertEquals(7, constitution.getArticles().size());
    }

    @Test
    @DisplayName("Should initialize with 27 amendments")
    void testAmendmentCount() {
        assertEquals(27, constitution.getAmendments().size());
    }

    @Test
    @DisplayName("Should initialize with a non-null preamble")
    void testPreambleInitialized() {
        assertNotNull(constitution.getPreamble());
    }

    // ---- Preamble tests ----

    @Test
    @DisplayName("Preamble text should start with 'We the People'")
    void testPreambleText() {
        assertTrue(constitution.getPreamble().getText().startsWith("We the People"));
    }

    @Test
    @DisplayName("Preamble should have 6 purposes")
    void testPreamblePurposes() {
        assertEquals(6, constitution.getPreamble().getPurposes().length);
    }

    // ---- Articles tests ----

    @Test
    @DisplayName("Should return Article I by number 1")
    void testGetArticleI() {
        Article article = constitution.getArticle(1);
        assertNotNull(article);
        assertInstanceOf(ArticleI.class, article);
        assertEquals(Article.ArticleNumber.ARTICLE_I, article.getArticleNumber());
    }

    @Test
    @DisplayName("Should return Article II by number 2")
    void testGetArticleII() {
        Article article = constitution.getArticle(2);
        assertNotNull(article);
        assertInstanceOf(ArticleII.class, article);
    }

    @Test
    @DisplayName("Should return Article III by number 3")
    void testGetArticleIII() {
        Article article = constitution.getArticle(3);
        assertNotNull(article);
        assertInstanceOf(ArticleIII.class, article);
    }

    @Test
    @DisplayName("Should return Article IV by number 4")
    void testGetArticleIV() {
        Article article = constitution.getArticle(4);
        assertNotNull(article);
        assertInstanceOf(ArticleIV.class, article);
    }

    @Test
    @DisplayName("Should return Article V by number 5")
    void testGetArticleV() {
        Article article = constitution.getArticle(5);
        assertNotNull(article);
        assertInstanceOf(ArticleV.class, article);
    }

    @Test
    @DisplayName("Should return Article VI by number 6")
    void testGetArticleVI() {
        Article article = constitution.getArticle(6);
        assertNotNull(article);
        assertInstanceOf(ArticleVI.class, article);
    }

    @Test
    @DisplayName("Should return Article VII by number 7")
    void testGetArticleVII() {
        Article article = constitution.getArticle(7);
        assertNotNull(article);
        assertInstanceOf(ArticleVII.class, article);
    }

    @Test
    @DisplayName("Should return null for invalid article number")
    void testGetArticleInvalid() {
        assertNull(constitution.getArticle(0));
        assertNull(constitution.getArticle(8));
        assertNull(constitution.getArticle(-1));
    }

    @Test
    @DisplayName("Typed getters should return correct article types")
    void testTypedArticleGetters() {
        assertNotNull(constitution.getArticleI());
        assertNotNull(constitution.getArticleII());
        assertNotNull(constitution.getArticleIII());
        assertNotNull(constitution.getArticleIV());
        assertNotNull(constitution.getArticleV());
        assertNotNull(constitution.getArticleVI());
        assertNotNull(constitution.getArticleVII());
    }

    @Test
    @DisplayName("Total section count should be sum of all article sections")
    void testTotalSectionCount() {
        int expected = 10 + 4 + 3 + 4 + 1 + 3 + 1; // I=10, II=4, III=3, IV=4, V=1, VI=3, VII=1
        assertEquals(expected, constitution.getTotalSectionCount());
    }

    // ---- Amendments tests ----

    @Test
    @DisplayName("Should return Amendment 1 by number")
    void testGetAmendmentFirst() {
        Amendment amendment = constitution.getAmendment(1);
        assertNotNull(amendment);
        assertEquals(1, amendment.getNumber());
        assertEquals("Freedom of Religion, Speech, Press, Assembly, and Petition", amendment.getTitle());
    }

    @Test
    @DisplayName("Should return Amendment 27 by number")
    void testGetAmendmentLast() {
        Amendment amendment = constitution.getAmendment(27);
        assertNotNull(amendment);
        assertEquals(27, amendment.getNumber());
        assertEquals("Congressional Compensation", amendment.getTitle());
    }

    @Test
    @DisplayName("Should return null for invalid amendment number")
    void testGetAmendmentInvalid() {
        assertNull(constitution.getAmendment(0));
        assertNull(constitution.getAmendment(28));
        assertNull(constitution.getAmendment(-1));
    }

    @Test
    @DisplayName("Bill of Rights should contain amendments 1-10")
    void testBillOfRights() {
        List<Amendment> billOfRights = constitution.getBillOfRights();
        assertEquals(10, billOfRights.size());
        assertEquals(1, billOfRights.get(0).getNumber());
        assertEquals(10, billOfRights.get(9).getNumber());
    }

    @Test
    @DisplayName("Reconstruction amendments should contain amendments 13-15")
    void testReconstructionAmendments() {
        List<Amendment> reconstruction = constitution.getReconstructionAmendments();
        assertEquals(3, reconstruction.size());
        assertEquals(13, reconstruction.get(0).getNumber());
        assertEquals(15, reconstruction.get(2).getNumber());
    }

    @Test
    @DisplayName("Should return amendments by group")
    void testGetAmendmentsByGroup() {
        List<Amendment> billOfRights = constitution.getAmendmentsByGroup(Amendment.AmendmentGroup.BILL_OF_RIGHTS);
        assertEquals(10, billOfRights.size());

        List<Amendment> reconstruction = constitution.getAmendmentsByGroup(Amendment.AmendmentGroup.RECONSTRUCTION);
        assertEquals(3, reconstruction.size());
    }

    @Test
    @DisplayName("Should return all amendment groups")
    void testGetAmendmentGroups() {
        List<Amendment.AmendmentGroup> groups = constitution.getAmendmentGroups();
        assertEquals(Amendment.AmendmentGroup.values().length, groups.size());
    }

    // ---- Government structure tests ----

    @Test
    @DisplayName("Should return 3 government branches")
    void testGovernmentBranches() {
        List<GovernmentBranch> branches = constitution.getGovernmentBranches();
        assertEquals(3, branches.size());
    }

    @Test
    @DisplayName("Should contain Legislative, Executive, and Judicial branches")
    void testGovernmentBranchTypes() {
        List<GovernmentBranch> branches = constitution.getGovernmentBranches();
        assertTrue(branches.contains(GovernmentBranch.LEGISLATIVE));
        assertTrue(branches.contains(GovernmentBranch.EXECUTIVE));
        assertTrue(branches.contains(GovernmentBranch.JUDICIAL));
    }

    @Test
    @DisplayName("Should return 8 constitutional principles")
    void testPrinciples() {
        List<Constitution.Principle> principles = constitution.getPrinciples();
        assertEquals(8, principles.size());
    }

    @Test
    @DisplayName("Should contain separation of powers principle")
    void testSeparationOfPowersPrinciple() {
        assertTrue(constitution.getPrinciples().contains(Constitution.Principle.SEPARATION_OF_POWERS));
    }

    @Test
    @DisplayName("Should contain checks and balances principle")
    void testChecksAndBalancesPrinciple() {
        assertTrue(constitution.getPrinciples().contains(Constitution.Principle.CHECKS_AND_BALANCES));
    }

    // ---- Dates tests ----

    @Test
    @DisplayName("Adoption date should be September 17, 1787")
    void testAdoptionDate() {
        assertEquals(LocalDate.of(1787, 9, 17), constitution.getAdoptionDate());
    }

    @Test
    @DisplayName("Ratification date should be June 21, 1788")
    void testRatificationDate() {
        assertEquals(LocalDate.of(1788, 6, 21), constitution.getRatificationDate());
    }

    @Test
    @DisplayName("Effective date should be March 4, 1789")
    void testEffectiveDate() {
        assertEquals(LocalDate.of(1789, 3, 4), constitution.getEffectiveDate());
    }

    @Test
    @DisplayName("Dates should be in chronological order")
    void testDateOrder() {
        assertTrue(constitution.getAdoptionDate().isBefore(constitution.getRatificationDate()));
        assertTrue(constitution.getRatificationDate().isBefore(constitution.getEffectiveDate()));
    }

    // ---- Summary and toString tests ----

    @Test
    @DisplayName("Summary should contain key information")
    void testSummary() {
        String summary = constitution.getSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("1787"));
        assertTrue(summary.contains("Articles"));
        assertTrue(summary.contains("Amendments"));
        assertTrue(summary.contains("separation of powers"));
    }

    @Test
    @DisplayName("toString should contain article and amendment counts")
    void testToString() {
        String str = constitution.toString();
        assertTrue(str.contains("Constitution{"));
        assertTrue(str.contains("articles=7"));
        assertTrue(str.contains("amendments=27"));
    }

    // ---- Article content integration tests ----

    @Test
    @DisplayName("Article I should have congressional powers")
    void testArticleIPowers() {
        ArticleI articleI = constitution.getArticleI();
        List<ArticleI.CongressionalPower> powers = articleI.getCongressionalPowers();
        assertFalse(powers.isEmpty());
        assertEquals(15, powers.size());
    }

    @Test
    @DisplayName("Article II should have executive powers")
    void testArticleIIPowers() {
        ArticleII articleII = constitution.getArticleII();
        List<ArticleII.ExecutivePower> powers = articleII.getExecutivePowers();
        assertFalse(powers.isEmpty());
        assertEquals(8, powers.size());
    }

    @Test
    @DisplayName("Article V should have proposal and ratification methods")
    void testArticleVMethods() {
        ArticleV articleV = constitution.getArticleV();
        assertEquals(2, articleV.getProposalMethods().size());
        assertEquals(2, articleV.getRatificationMethods().size());
    }

    @Test
    @DisplayName("Article VII should require 9 states for ratification")
    void testArticleVIIRatification() {
        ArticleVII articleVII = constitution.getArticleVII();
        assertEquals(9, articleVII.getRequiredStatesForRatification());
        assertEquals(13, articleVII.getTotalOriginalStates());
    }
}
