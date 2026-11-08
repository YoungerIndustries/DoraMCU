package com.yisy.modules.doramcu.traveller;

import jakarta.persistence.*;

/**
 * JPA entity representing an entry in a Classic Traveller reference table.
 * <p>
 * The rulebooks contain hundreds of reference tables organized into named tables:
 * <ul>
 *   <li>Saving Throw tables — with results by roll value</li>
 *   <li>Damage tables — with effects by damage level</li>
 *   <li>Ship combat tables — hit locations, damage effects</li>
 *   <li>Encounter tables — random encounters by type</li>
 *   <li>Wildlife tables — creature encounters</li>
 *   <li>Career skill tables — skill acquisition probabilities</li>
 *   <li>Trade commodity tables — prices and volumes</li>
 * </ul>
 * </p>
 * <p>
 * Each table has a name, and each entry has a roll value (2-12 for 2D6 tables),
 * the result label, and a detailed description. This allows the AI and game services
 * to reference the exact table entries used during gameplay.
 * </p>
 */
@Entity
@Table(name = "ct_rules_tables", indexes = {
        @Index(name = "idx_ct_rules_table_name", columnList = "table_name"),
        @Index(name = "idx_ct_rules_table_category", columnList = "category")
})
public class TravellerRulesTable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The name of the table (e.g., "Saving Throw Results", "Ship Hit Location"). */
    @Column(nullable = false, length = 100)
    private String tableName;

    /** The source book (e.g., "The Traveller Book", "B01 Characters & Combat"). */
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

    /** The roll value that selects this entry (2-12 for 2D6 tables). */
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
        return "TravellerRulesTable{table='" + tableName + "', source='" + source + "', roll=" + rollValue + ", result='" + resultLabel + "'}";
    }
}
