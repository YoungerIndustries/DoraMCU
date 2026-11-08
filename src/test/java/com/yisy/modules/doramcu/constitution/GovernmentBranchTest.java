package com.yisy.modules.doramcu.constitution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class GovernmentBranchTest {

    // ---- Enum values tests ----

    @Test
    @DisplayName("Should have 3 government branches")
    void testBranchCount() {
        assertEquals(3, GovernmentBranch.values().length);
    }

    @Test
    @DisplayName("Should include Legislative branch")
    void testLegislativeExists() {
        assertNotNull(GovernmentBranch.LEGISLATIVE);
    }

    @Test
    @DisplayName("Should include Executive branch")
    void testExecutiveExists() {
        assertNotNull(GovernmentBranch.EXECUTIVE);
    }

    @Test
    @DisplayName("Should include Judicial branch")
    void testJudicialExists() {
        assertNotNull(GovernmentBranch.JUDICIAL);
    }

    // ---- Legislative branch tests ----

    @Test
    @DisplayName("Legislative branch should have correct name")
    void testLegislativeName() {
        assertEquals("Legislative", GovernmentBranch.LEGISLATIVE.getName());
    }

    @Test
    @DisplayName("Legislative branch should reference Article I")
    void testLegislativeArticle() {
        assertEquals(Article.ArticleNumber.ARTICLE_I, GovernmentBranch.LEGISLATIVE.getGoverningArticle());
    }

    @Test
    @DisplayName("Legislative branch should mention Congress")
    void testLegislativeDescription() {
        String desc = GovernmentBranch.LEGISLATIVE.getDescription();
        assertNotNull(desc);
        assertTrue(desc.contains("Congress"));
    }

    // ---- Executive branch tests ----

    @Test
    @DisplayName("Executive branch should have correct name")
    void testExecutiveName() {
        assertEquals("Executive", GovernmentBranch.EXECUTIVE.getName());
    }

    @Test
    @DisplayName("Executive branch should reference Article II")
    void testExecutiveArticle() {
        assertEquals(Article.ArticleNumber.ARTICLE_II, GovernmentBranch.EXECUTIVE.getGoverningArticle());
    }

    @Test
    @DisplayName("Executive branch should mention President")
    void testExecutiveDescription() {
        String desc = GovernmentBranch.EXECUTIVE.getDescription();
        assertNotNull(desc);
        assertTrue(desc.contains("President"));
    }

    // ---- Judicial branch tests ----

    @Test
    @DisplayName("Judicial branch should have correct name")
    void testJudicialName() {
        assertEquals("Judicial", GovernmentBranch.JUDICIAL.getName());
    }

    @Test
    @DisplayName("Judicial branch should reference Article III")
    void testJudicialArticle() {
        assertEquals(Article.ArticleNumber.ARTICLE_III, GovernmentBranch.JUDICIAL.getGoverningArticle());
    }

    @Test
    @DisplayName("Judicial branch should mention Supreme Court")
    void testJudicialDescription() {
        String desc = GovernmentBranch.JUDICIAL.getDescription();
        assertNotNull(desc);
        assertTrue(desc.contains("Supreme Court"));
    }

    // ---- General tests ----

    @Test
    @DisplayName("Each branch should have a non-empty description")
    void testAllDescriptionsNonEmpty() {
        for (GovernmentBranch branch : GovernmentBranch.values()) {
            assertNotNull(branch.getDescription());
            assertFalse(branch.getDescription().isEmpty());
        }
    }

    @Test
    @DisplayName("Each branch should have a non-empty name")
    void testAllNamesNonEmpty() {
        for (GovernmentBranch branch : GovernmentBranch.values()) {
            assertNotNull(branch.getName());
            assertFalse(branch.getName().isEmpty());
        }
    }

    @Test
    @DisplayName("Each branch should have a governing article")
    void testAllGoverningArticles() {
        for (GovernmentBranch branch : GovernmentBranch.values()) {
            assertNotNull(branch.getGoverningArticle());
        }
    }

    @Test
    @DisplayName("Branch governing articles should be I, II, and III")
    void testGoverningArticlesCoverage() {
        Article.ArticleNumber[] articles = new Article.ArticleNumber[3];
        int i = 0;
        for (GovernmentBranch branch : GovernmentBranch.values()) {
            articles[i++] = branch.getGoverningArticle();
        }
        assertTrue(java.util.Arrays.asList(articles).contains(Article.ArticleNumber.ARTICLE_I));
        assertTrue(java.util.Arrays.asList(articles).contains(Article.ArticleNumber.ARTICLE_II));
        assertTrue(java.util.Arrays.asList(articles).contains(Article.ArticleNumber.ARTICLE_III));
    }

    @Test
    @DisplayName("Governing article numbers should match branch order")
    void testArticleNumberOrder() {
        assertEquals(1, GovernmentBranch.LEGISLATIVE.getGoverningArticle().getNumber());
        assertEquals(2, GovernmentBranch.EXECUTIVE.getGoverningArticle().getNumber());
        assertEquals(3, GovernmentBranch.JUDICIAL.getGoverningArticle().getNumber());
    }

    // ---- Lookup tests ----

    @Test
    @DisplayName("Should find branch by name")
    void testFindByValue() {
        assertSame(GovernmentBranch.LEGISLATIVE, GovernmentBranch.valueOf("LEGISLATIVE"));
        assertSame(GovernmentBranch.EXECUTIVE, GovernmentBranch.valueOf("EXECUTIVE"));
        assertSame(GovernmentBranch.JUDICIAL, GovernmentBranch.valueOf("JUDICIAL"));
    }

    @Test
    @DisplayName("Should throw on invalid branch name")
    void testInvalidBranchName() {
        assertThrows(IllegalArgumentException.class, () -> GovernmentBranch.valueOf("INVALID"));
    }
}
