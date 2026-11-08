package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Summary DTO for mercenary units and ground combat capabilities.
 * <p>
 * Used in EmpireSummary to display mercenary units, ticket contracts,
 * and ground strength for planetary defense.
 * </p>
 */
public class MercenarySummaryDto {

    private int totalUnits;
    private int groundStrength;
    private List<MercenaryUnitDto> units;
    private String ticketStatus;
    private int ticketIncome;
    private int ticketRemainingTerms;

    public MercenarySummaryDto() {
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public int getGroundStrength() {
        return groundStrength;
    }

    public void setGroundStrength(int groundStrength) {
        this.groundStrength = groundStrength;
    }

    public List<MercenaryUnitDto> getUnits() {
        return units;
    }

    public void setUnits(List<MercenaryUnitDto> units) {
        this.units = units;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public int getTicketIncome() {
        return ticketIncome;
    }

    public void setTicketIncome(int ticketIncome) {
        this.ticketIncome = ticketIncome;
    }

    public int getTicketRemainingTerms() {
        return ticketRemainingTerms;
    }

    public void setTicketRemainingTerms(int ticketRemainingTerms) {
        this.ticketRemainingTerms = ticketRemainingTerms;
    }

    /**
     * Individual mercenary unit details.
     */
    public static class MercenaryUnitDto {
        private int unitId;
        private String unitName;
        private String unitType;
        private int strength;
        private int morale;
        private boolean active;

        public int getUnitId() {
            return unitId;
        }

        public void setUnitId(int unitId) {
            this.unitId = unitId;
        }

        public String getUnitName() {
            return unitName;
        }

        public void setUnitName(String unitName) {
            this.unitName = unitName;
        }

        public String getUnitType() {
            return unitType;
        }

        public void setUnitType(String unitType) {
            this.unitType = unitType;
        }

        public int getStrength() {
            return strength;
        }

        public void setStrength(int strength) {
            this.strength = strength;
        }

        public int getMorale() {
            return morale;
        }

        public void setMorale(int morale) {
            this.morale = morale;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
