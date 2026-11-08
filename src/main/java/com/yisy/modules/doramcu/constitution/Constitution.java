package com.yisy.modules.doramcu.constitution;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the Constitution of the United States of America as a complete
 * software model, including the Preamble, seven Articles, and 27 Amendments.
 * <p>
 * The Constitution is the supreme law of the United States, establishing the
 * framework of the federal government with its separation of powers among
 * three branches: legislative, executive, and judicial. It embodies core
 * principles including federalism, republicanism, individual rights,
 * rule of law, and checks and balances.
 * </p>
 * <p>
 * This model provides programmatic access to the structure and content of
 * the Constitution, enabling queries about articles, amendments, powers,
 * rights, and the relationships between government branches.
 * </p>
 */
public class Constitution {

    /**
     * Core principles embodied in the Constitution.
     */
    public enum Principle {
        SEPARATION_OF_POWERS("Separation of Powers",
                "Division of government into three branches: legislative, executive, and judicial"),
        FEDERALISM("Federalism",
                "Division of power between the national and state governments"),
        REPUBLICANISM("Republicanism",
                "Government by elected representatives rather than direct democracy or monarchy"),
        INDIVIDUAL_RIGHTS("Individual Rights",
                "Protection of fundamental liberties and freedoms of individuals"),
        RULE_OF_LAW("Rule of Law",
                "All persons and institutions are subject to and accountable to law"),
        CHECKS_AND_BALANCES("Checks and Balances",
                "Each branch can limit the powers of the others to prevent tyranny"),
        EQUAL_FOOTING("Equal Footing",
                "All states enter the Union on equal terms"),
        POPULAR_SOVEREIGNTY("Popular Sovereignty",
                "The authority of the government is created and sustained by the consent of its people");

        private final String name;
        private final String description;

        Principle(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final LocalDate ADOPTION_DATE = LocalDate.of(1787, 9, 17);
    private static final LocalDate RATIFICATION_DATE = LocalDate.of(1788, 6, 21);
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(1789, 3, 4);

    private final Preamble preamble;
    private final List<Article> articles;
    private final List<Amendment> amendments;

    public Constitution() {
        this.preamble = new Preamble();
        this.articles = List.of(
                new ArticleI(),
                new ArticleII(),
                new ArticleIII(),
                new ArticleIV(),
                new ArticleV(),
                new ArticleVI(),
                new ArticleVII()
        );
        this.amendments = new ArrayList<>();
        for (Amendment.RatifiedAmendment ra : Amendment.RatifiedAmendment.values()) {
            amendments.add(new Amendment(ra));
        }
    }

    // ---- Preamble ----

    public Preamble getPreamble() {
        return preamble;
    }

    // ---- Articles ----

    /**
     * Returns all seven Articles of the Constitution.
     */
    public List<Article> getArticles() {
        return articles;
    }

    /**
     * Returns the Article by its number (1-7).
     *
     * @param articleNumber the article number (1 for Article I, 7 for Article VII)
     * @return the Article, or null if not found
     */
    public Article getArticle(int articleNumber) {
        return articles.stream()
                .filter(a -> a.getArticleNumber().getNumber() == articleNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the Article I (Legislative Branch).
     */
    public ArticleI getArticleI() {
        return (ArticleI) getArticle(1);
    }

    /**
     * Returns the Article II (Executive Branch).
     */
    public ArticleII getArticleII() {
        return (ArticleII) getArticle(2);
    }

    /**
     * Returns the Article III (Judicial Branch).
     */
    public ArticleIII getArticleIII() {
        return (ArticleIII) getArticle(3);
    }

    /**
     * Returns the Article IV (States).
     */
    public ArticleIV getArticleIV() {
        return (ArticleIV) getArticle(4);
    }

    /**
     * Returns the Article V (Amendment Process).
     */
    public ArticleV getArticleV() {
        return (ArticleV) getArticle(5);
    }

    /**
     * Returns the Article VI (Supremacy).
     */
    public ArticleVI getArticleVI() {
        return (ArticleVI) getArticle(6);
    }

    /**
     * Returns the Article VII (Ratification).
     */
    public ArticleVII getArticleVII() {
        return (ArticleVII) getArticle(7);
    }

    /**
     * Returns the total number of sections across all Articles.
     */
    public int getTotalSectionCount() {
        return articles.stream().mapToInt(Article::getSectionCount).sum();
    }

    // ---- Amendments ----

    /**
     * Returns all 27 ratified Amendments.
     */
    public List<Amendment> getAmendments() {
        return amendments;
    }

    /**
     * Returns an Amendment by its number (1-27).
     *
     * @param amendmentNumber the amendment number
     * @return the Amendment, or null if not found
     */
    public Amendment getAmendment(int amendmentNumber) {
        return amendments.stream()
                .filter(a -> a.getNumber() == amendmentNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the Bill of Rights (Amendments 1-10).
     */
    public List<Amendment> getBillOfRights() {
        return amendments.stream()
                .filter(a -> a.getGroup() == Amendment.AmendmentGroup.BILL_OF_RIGHTS)
                .collect(Collectors.toList());
    }

    /**
     * Returns the Reconstruction Amendments (13-15).
     */
    public List<Amendment> getReconstructionAmendments() {
        return amendments.stream()
                .filter(a -> a.getGroup() == Amendment.AmendmentGroup.RECONSTRUCTION)
                .collect(Collectors.toList());
    }

    /**
     * Returns amendments grouped by their historical category.
     */
    public List<Amendment.AmendmentGroup> getAmendmentGroups() {
        return List.of(Amendment.AmendmentGroup.values());
    }

    /**
     * Returns amendments belonging to a specific group.
     */
    public List<Amendment> getAmendmentsByGroup(Amendment.AmendmentGroup group) {
        return amendments.stream()
                .filter(a -> a.getGroup() == group)
                .collect(Collectors.toList());
    }

    // ---- Government Structure ----

    /**
     * Returns the three branches of government.
     */
    public List<GovernmentBranch> getGovernmentBranches() {
        return List.of(GovernmentBranch.values());
    }

    /**
     * Returns the core principles of the Constitution.
     */
    public List<Principle> getPrinciples() {
        return List.of(Principle.values());
    }

    // ---- Dates ----

    /**
     * Returns the date the Constitution was adopted at the Constitutional Convention.
     */
    public LocalDate getAdoptionDate() {
        return ADOPTION_DATE;
    }

    /**
     * Returns the date the Constitution was ratified by the ninth state (New Hampshire).
     */
    public LocalDate getRatificationDate() {
        return RATIFICATION_DATE;
    }

    /**
     * Returns the date the Constitution went into effect.
     */
    public LocalDate getEffectiveDate() {
        return EFFECTIVE_DATE;
    }

    // ---- Summary ----

    /**
     * Returns a summary description of the Constitution.
     */
    public String getSummary() {
        return "The Constitution of the United States of America, adopted on "
                + ADOPTION_DATE + ", is the supreme law of the United States. "
                + "It establishes the framework of the federal government with "
                + articles.size() + " Articles and " + amendments.size() + " Amendments, "
                + "embodying the principles of separation of powers, federalism, "
                + "republicanism, individual rights, and checks and balances.";
    }

    @Override
    public String toString() {
        return "Constitution{adopted=" + ADOPTION_DATE
                + ", articles=" + articles.size()
                + ", amendments=" + amendments.size()
                + ", sections=" + getTotalSectionCount() + "}";
    }
}
