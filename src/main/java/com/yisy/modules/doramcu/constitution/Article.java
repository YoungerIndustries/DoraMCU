package com.yisy.modules.doramcu.constitution;

/**
 * Base class representing an Article of the United States Constitution.
 * <p>
 * The Constitution contains seven Articles that establish the framework
 * of the federal government, including the separation of powers among
 * the legislative, executive, and judicial branches, as well as the
 * relationship between states and the federal government.
 * </p>
 */
public abstract class Article {

    /**
     * Identifies which Article (I through VII) this instance represents.
     */
    public enum ArticleNumber {
        ARTICLE_I(1, "The Legislative Branch"),
        ARTICLE_II(2, "The Executive Branch"),
        ARTICLE_III(3, "The Judicial Branch"),
        ARTICLE_IV(4, "The States, Citizenship, New States"),
        ARTICLE_V(5, "Amendment Process"),
        ARTICLE_VI(6, "Debts, Supremacy, Oaths"),
        ARTICLE_VII(7, "Ratification");

        private final int number;
        private final String title;

        ArticleNumber(int number, String title) {
            this.number = number;
            this.title = title;
        }

        public int getNumber() {
            return number;
        }

        public String getTitle() {
            return title;
        }
    }

    private final ArticleNumber articleNumber;
    private String title;
    private String description;

    protected Article(ArticleNumber articleNumber, String title, String description) {
        this.articleNumber = articleNumber;
        this.title = title;
        this.description = description;
    }

    public ArticleNumber getArticleNumber() {
        return articleNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the number of sections in this Article.
     */
    public abstract int getSectionCount();

    @Override
    public String toString() {
        return "Article{" + articleNumber.getNumber() + ": " + title + ", sections=" + getSectionCount() + "}";
    }
}
