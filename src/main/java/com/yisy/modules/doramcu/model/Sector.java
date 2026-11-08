package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a sector in a galaxy.
 * <p>
 * Each sector is identified by its grid position (row, col) within a galaxy
 * and stores a persistent name and trade type classification.
 * </p>
 */
@Entity
@Table(name = "sectors")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private int sectorRow;

    @Column(nullable = false)
    private int sectorCol;

    @Column(nullable = false)
    private String name;

    private String tradeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galaxy_id", nullable = false)
    private Galaxy galaxy;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubSector> subSectors = new ArrayList<>();

    public Sector() {
    }

    public Sector(int sectorRow, int sectorCol, String name) {
        this.sectorRow = sectorRow;
        this.sectorCol = sectorCol;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSectorRow() {
        return sectorRow;
    }

    public void setSectorRow(int sectorRow) {
        this.sectorRow = sectorRow;
    }

    public int getSectorCol() {
        return sectorCol;
    }

    public void setSectorCol(int sectorCol) {
        this.sectorCol = sectorCol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public List<SubSector> getSubSectors() {
        return subSectors;
    }

    public void setSubSectors(List<SubSector> subSectors) {
        this.subSectors = subSectors;
    }

    @Override
    public String toString() {
        return "Sector{id=" + id + ", name='" + name + "', row=" + sectorRow + ", col=" + sectorCol + "}";
    }
}
