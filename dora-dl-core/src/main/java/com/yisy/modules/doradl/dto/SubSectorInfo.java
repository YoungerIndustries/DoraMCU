package com.yisy.modules.doradl.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /** Traveller-style trade classification of the parent sector (e.g., "Agricultural", "Industrial"). */
    private String tradeType;
    private List<Map<String, Object>> systems;
    /** Set of empire IDs that own planets in this subsector. Empty if no empire owns anything here. */
    private Set<Integer> ownerEmpireIds;
    /** True if any empire other than the requesting player owns planets in this subsector. */
    private boolean contested;

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

    public Set<Integer> getOwnerEmpireIds() {
        return ownerEmpireIds;
    }

    public void setOwnerEmpireIds(Set<Integer> ownerEmpireIds) {
        this.ownerEmpireIds = ownerEmpireIds;
    }

    public boolean isContested() {
        return contested;
    }

    public void setContested(boolean contested) {
        this.contested = contested;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
}
