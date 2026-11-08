package com.yisy.modules.doramcu.shared.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Unified JPA entity representing an entry in a reference table from any game system.
 * <p>
 * Replaces the duplicate {@code TravellerRulesTable} and {@code RulesTable} entities
 * with a single shared table discriminated by {@link GameSystem}.
 * </p>
 * <p>
 * The rulebooks contain hundreds of reference tables organized into named tables:
 * <ul>
 *   <li>Saving Throw tables — with results by roll value</li>
 *   <li>Damage tables — with effects by damage level</li>
 *   <li>Ship combat tables — hit locations, damage effects</li>
 *   <li>Encounter tables — random encounters by type</li>
 *   <li>Career skill tables — skill acquisition probabilities</li>
 * </ul>
 * </p>
 */
@Entity(name = "UnifiedRulesTable")
@Table(name = "unified_rules_tables", indexes = {
        @Index(name = "idx_rules_table_game_system", columnList = "gameSystem"),
        @Index(name = "idx_rules_table_name", columnList = "table_name"),
        @Index(name = "idx_rules_table_category", columnList = "category")
})
public class RulesTable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Which game system this table belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GameSystem gameSystem;

    /** The name of the table (e.g., "Saving Throw Results", "Task Attempt Results"). */
    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    /** The source book (e.g., "The Traveller Book", "Captain's Log Solo RPG"). */
    @Column(nullable = false, length = 100)
    private String source;

    /** The category of the table (e.g., "TABLE_SAVING_THROW", "TABLE_DAMAGE", "TABLE_SHIP_COMBAT"). */
    @Column(nullable = false, length = 50)
    private String category;

    /** The rulebook chapter this table belongs to. */
    @Column(nullable = false, length = 100)
    private String chapter;

    /** Approximate page number in the source book. */
    @Column
    private Integer page;

    /** The roll value that selects this entry (2-12 for 2D6 tables, 2-40 for 2d20 tables). */
    @Column(nullable = false)
    private int rollValue;

    /** The result label (e.g., "Success", "Critical Hit", "Derelict Ship"). */
    @Column(nullable = false, length = 200)
    private String resultLabel;

    /** The full description of this table entry. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Whether this entry is active/applicable. */
    @Column(nullable = false)
    private boolean active = true;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameSystem getGameSystem() {
        return gameSystem;
    }

    public void setGameSystem(GameSystem gameSystem) {
        this.gameSystem = Objects.requireNonNull(gameSystem, "gameSystem must not be null");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public int getRollValue() {
        return rollValue;
    }

    public void setRollValue(int rollValue) {
        this.rollValue = rollValue;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "RulesTable{gameSystem=" + gameSystem + ", table='" + tableName + "', source='" + source +
                "', roll=" + rollValue + ", result='" + resultLabel + "'}";
    }
}
