package com.yisy.modules.doramcu.captainslog.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a single rule entry from the Captain's Log Solo RPG rulebook.
 * <p>
 * Each entry stores a snippet of rule text with metadata for categorization and retrieval:
 * <ul>
 *   <li>Chapter — the rulebook chapter (e.g., "05.00 Rules of Play")</li>
 *   <li>Category — the rule domain (e.g., "DICE_MECHANICS", "COMBAT", "MOMENTUM")</li>
 *   <li>Page — the approximate page number in the rulebook</li>
 *   <li>Title — a short label for the rule</li>
 *   <li>Text — the full rule text</li>
 *   <li>Notes — additional context or cross-references</li>
 * </ul>
 * </p>
 * <p>
 * This entity enables the AI and game services to query official rules during task completions,
 * ensuring that dice rolls, combat resolution, and narrative decisions follow the published rules.
 * </p>
 */
@Entity
@Table(name = "cl_rules_entries", indexes = {
        @Index(name = "idx_cl_rules_category", columnList = "category"),
        @Index(name = "idx_cl_rules_chapter", columnList = "chapter"),
        @Index(name = "idx_cl_rules_title", columnList = "title")
})
public class RulesEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Short label for the rule (e.g., "Task Attempt Core Mechanic"). */
    @Column(nullable = false, length = 200)
    private String title;

    /** The rulebook chapter this rule belongs to (e.g., "05.00 Rules of Play"). */
    @Column(nullable = false, length = 100)
    private String chapter;

    /** The rule domain category (e.g., "DICE_MECHANICS", "COMBAT", "MOMENTUM", "THREAT"). */
    @Column(nullable = false, length = 50)
    private String category;

    /** Approximate page number in the rulebook. */
    @Column
    private Integer page;

    /** The full rule text — the authoritative description of this rule. */
    @Column(name = "rule_text", columnDefinition = "TEXT")
    private String text;

    /** Additional context, examples, or cross-references. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Whether this rule is active/applicable (for versioning or era-specific rules). */
    @Column(nullable = false)
    private boolean active = true;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "RulesEntry{title='" + title + "', chapter='" + chapter + "', category='" + category + "', page=" + page + "}";
    }
}
