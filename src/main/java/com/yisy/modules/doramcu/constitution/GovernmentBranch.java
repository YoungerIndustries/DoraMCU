package com.yisy.modules.doramcu.constitution;

import com.yisy.modules.doramcu.constitution.Article.ArticleNumber;

/**
 * Represents the three branches of the U.S. federal government
 * as established by the Constitution's separation of powers.
 */
public enum GovernmentBranch {
    LEGISLATIVE("Legislative", "Makes laws through Congress (Senate and House of Representatives)", ArticleNumber.ARTICLE_I),
    EXECUTIVE("Executive", "Enforces laws through the President and federal agencies", ArticleNumber.ARTICLE_II),
    JUDICIAL("Judicial", "Interprets laws through the Supreme Court and lower federal courts", ArticleNumber.ARTICLE_III);

    private final String name;
    private final String description;
    private final ArticleNumber governingArticle;

    GovernmentBranch(String name, String description, ArticleNumber governingArticle) {
        this.name = name;
        this.description = description;
        this.governingArticle = governingArticle;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArticleNumber getGoverningArticle() {
        return governingArticle;
    }
}
