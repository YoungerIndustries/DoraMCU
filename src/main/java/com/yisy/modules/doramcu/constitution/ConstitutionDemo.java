package com.yisy.modules.doramcu.constitution;

import java.util.List;

/**
 * Demonstrates the Constitution software model by printing a structured
 * overview of the Preamble, Articles, Amendments, and government structure.
 */
public class ConstitutionDemo {

    public static void main(String[] args) {
        Constitution constitution = new Constitution();

        System.out.println("=============================================================");
        System.out.println("  THE CONSTITUTION OF THE UNITED STATES OF AMERICA");
        System.out.println("  Software Model Overview");
        System.out.println("=============================================================");
        System.out.println();

        // Summary
        System.out.println("SUMMARY:");
        System.out.println("  " + constitution.getSummary());
        System.out.println();

        // Dates
        System.out.println("KEY DATES:");
        System.out.println("  Adopted:       " + constitution.getAdoptionDate());
        System.out.println("  Ratified:      " + constitution.getRatificationDate());
        System.out.println("  Effective:     " + constitution.getEffectiveDate());
        System.out.println();

        // Preamble
        System.out.println("PREAMBLE:");
        System.out.println("  " + constitution.getPreamble().getText());
        System.out.println();
        System.out.println("  Purposes:");
        for (Preamble.Purpose purpose : constitution.getPreamble().getPurposes()) {
            System.out.println("    - " + purpose.getDescription());
        }
        System.out.println();

        // Core Principles
        System.out.println("CORE PRINCIPLES:");
        for (Constitution.Principle principle : constitution.getPrinciples()) {
            System.out.println("  - " + principle.getName() + ": " + principle.getDescription());
        }
        System.out.println();

        // Government Branches
        System.out.println("GOVERNMENT BRANCHES:");
        for (GovernmentBranch branch : constitution.getGovernmentBranches()) {
            System.out.println("  " + branch.getName() + " Branch:");
            System.out.println("    " + branch.getDescription());
            System.out.println("    Governed by: " + branch.getGoverningArticle().getTitle());
        }
        System.out.println();

        // Articles
        System.out.println("ARTICLES (" + constitution.getArticles().size() + " total, "
                + constitution.getTotalSectionCount() + " sections):");
        for (Article article : constitution.getArticles()) {
            System.out.println("  Article " + article.getArticleNumber().getNumber()
                    + " - " + article.getTitle());
            System.out.println("    Sections: " + article.getSectionCount());
            System.out.println("    " + article.getDescription());
        }
        System.out.println();

        // Article I details
        ArticleI articleI = constitution.getArticleI();
        System.out.println("ARTICLE I — CONGRESSIONAL POWERS (Section 8):");
        for (ArticleI.CongressionalPower power : articleI.getCongressionalPowers()) {
            System.out.println("  - " + power.getDescription());
        }
        System.out.println();

        // Article II details
        ArticleII articleII = constitution.getArticleII();
        System.out.println("ARTICLE II — EXECUTIVE POWERS:");
        for (ArticleII.ExecutivePower power : articleII.getExecutivePowers()) {
            System.out.println("  - " + power.getDescription());
        }
        System.out.println();

        // Amendments
        System.out.println("AMENDMENTS (" + constitution.getAmendments().size() + " total):");
        System.out.println();

        // Bill of Rights
        System.out.println("  BILL OF RIGHTS (Amendments 1-10):");
        for (Amendment amendment : constitution.getBillOfRights()) {
            System.out.println("    Amendment " + amendment.getNumber()
                    + ": " + amendment.getTitle());
            System.out.println("      Ratified: " + amendment.getRatificationDate());
            System.out.println("      Subjects: " + amendment.getSubjects());
        }
        System.out.println();

        // Reconstruction Amendments
        System.out.println("  RECONSTRUCTION AMENDMENTS (13-15):");
        for (Amendment amendment : constitution.getReconstructionAmendments()) {
            System.out.println("    Amendment " + amendment.getNumber()
                    + ": " + amendment.getTitle());
            System.out.println("      Ratified: " + amendment.getRatificationDate());
        }
        System.out.println();

        // Remaining amendments
        System.out.println("  REMAINING AMENDMENTS (11-27, excluding 13-15):");
        for (Amendment amendment : constitution.getAmendments()) {
            int num = amendment.getNumber();
            if (num >= 11 && num <= 27 && num < 13) {
                System.out.println("    Amendment " + num + ": " + amendment.getTitle()
                        + " (Ratified: " + amendment.getRatificationDate() + ")");
            } else if (num > 15) {
                System.out.println("    Amendment " + num + ": " + amendment.getTitle()
                        + " (Ratified: " + amendment.getRatificationDate() + ")");
            }
        }
        System.out.println();

        // Amendment Groups
        System.out.println("AMENDMENT GROUPS:");
        for (Amendment.AmendmentGroup group : constitution.getAmendmentGroups()) {
            List<Amendment> groupAmendments = constitution.getAmendmentsByGroup(group);
            System.out.println("  " + group.getName() + " (Amendments "
                    + group.getStartNumber() + "-" + group.getEndNumber() + "):");
            System.out.println("    " + group.getDescription());
            System.out.println("    Contains " + groupAmendments.size() + " amendment(s)");
        }
        System.out.println();

        // Article V — Amendment Process
        ArticleV articleV = constitution.getArticleV();
        System.out.println("ARTICLE V — AMENDMENT PROCESS:");
        System.out.println("  Proposal Methods:");
        for (ArticleV.ProposalMethod method : articleV.getProposalMethods()) {
            System.out.println("    - " + method.getDescription());
        }
        System.out.println("  Ratification Methods:");
        for (ArticleV.RatificationMethod method : articleV.getRatificationMethods()) {
            System.out.println("    - " + method.getDescription());
        }
        System.out.println();

        // Article VI — Supremacy
        ArticleVI articleVI = constitution.getArticleVI();
        System.out.println("ARTICLE VI — SUPREMACY CLAUSE:");
        for (ArticleVI.SupremacyClause clause : articleVI.getSupremacyClauses()) {
            System.out.println("  - " + clause.getDescription());
        }
        System.out.println();

        System.out.println("=============================================================");
        System.out.println("  Model: " + constitution);
        System.out.println("=============================================================");
    }
}
