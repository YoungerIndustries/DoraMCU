package com.yisy.modules.doradl.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO representing a subsector in the galaxy.
 * Each subsector contains 1-3 solar systems with planets and fleets.
 */
public class SubSectorInfo {

    private int id;
    private String name;
    private int sectorRow;
    private int sectorCol;
    private int subRow;
    private int subCol;
    private boolean generated;
    private int systemCount;
    private List<Map<String, Object>> systems;

    public SubSectorInfo() {
    }

    public SubSectorInfo(int id, String name, int sectorRow, int sectorCol, int subRow, int subCol,
                         boolean generated, int systemCount, List<Map<String, Object>> systems) {
        this.id = id;
        this.name = name;
        this.sectorRow = sectorRow;
        this.sectorCol = sectorCol;
        this.subRow = subRow;
        this.subCol = subCol;
        this.generated = generated;
        this.systemCount = systemCount;
        this.systems = systems;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getSubRow() {
        return subRow;
    }

    public void setSubRow(int subRow) {
        this.subRow = subRow;
    }

    public int getSubCol() {
        return subCol;
    }

    public void setSubCol(int subCol) {
        this.subCol = subCol;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public int getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(int systemCount) {
        this.systemCount = systemCount;
    }

    public List<Map<String, Object>> getSystems() {
        return systems;
    }

    public void setSystems(List<Map<String, Object>> systems) {
        this.systems = systems;
    }
}
