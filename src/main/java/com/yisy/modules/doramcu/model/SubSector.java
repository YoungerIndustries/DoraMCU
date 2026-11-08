package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a subsector within a sector.
 * <p>
 * Each subsector is identified by its grid position (row, col) within a sector
 * and stores a persistent name.
 * </p>
 */
@Entity
@Table(name = "subsectors")
public class SubSector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private int subSectorRow;

    @Column(nullable = false)
    private int subSectorCol;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    public SubSector() {
    }

    public SubSector(int subSectorRow, int subSectorCol, String name) {
        this.subSectorRow = subSectorRow;
        this.subSectorCol = subSectorCol;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSubSectorRow() {
        return subSectorRow;
    }

    public void setSubSectorRow(int subSectorRow) {
        this.subSectorRow = subSectorRow;
    }

    public int getSubSectorCol() {
        return subSectorCol;
    }

    public void setSubSectorCol(int subSectorCol) {
        this.subSectorCol = subSectorCol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    @Override
    public String toString() {
        return "SubSector{id=" + id + ", name='" + name + "', row=" + subSectorRow + ", col=" + subSectorCol + "}";
    }
}
