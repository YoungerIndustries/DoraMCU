package com.yisy.modules.doramcu.captainslog.model;

import jakarta.persistence.*;

/**
 * JPA entity representing an entry in a Captain's Log probability matrix (oracle table).
 * <p>
 * The rulebook contains over 100 random tables organized into named tables:
 * <ul>
 *   <li>Yes/No Probability Matrix — with probability levels</li>
 *   <li>Mission Type table — 20 mission types</li>
 *   <li>Complication table — 20 complications</li>
 *   <li>Advantage table — 20 advantages</li>
 *   <li>NPC tables — roles, alignments, species</li>
 *   <li>Location tables — types and planet types</li>
 *   <li>Career event table — 20 career events</li>
 * </ul>
 * </p>
 * <p>
 * Each table has a name, and each entry has a roll value (1-20), the result label,
 * and a detailed description. This allows the AI and game services to reference
 * the exact table entries used during gameplay.
 * </p>
 */
@Entity
@Table(name = "cl_rules_tables", indexes = {
        @Index(name = "idx_cl_rules_table_name", columnList = "table_name"),
        @Index(name = "idx_cl_rules_table_category", columnList = "category")
})
public class RulesTable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The name of the table (e.g., "Mission Type", "Complication", "NPC Role"). */
    @Column(nullable = false, length = 100)
    private String tableName;

    /** The category of the table (e.g., "MISSION", "COMBAT", "NPC", "LOCATION"). */
    @Column(nullable = false, length = 50)
    private String category;

    /** The rulebook chapter this table belongs to. */
    @Column(nullable = false, length = 100)
    private String chapter;

    /** Approximate page number in the rulebook. */
    @Column
    private Integer page;

    /** The roll value that selects this entry (1-20 for d20 tables, 1-6 for d6 tables). */
    @Column(nullable = false)
    private int rollValue;

    /** The result label (e.g., "Diplomacy", "Reinforcements", "Klingon"). */
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
        return "RulesTable{table='" + tableName + "', roll=" + rollValue + ", result='" + resultLabel + "'}";
    }
}
