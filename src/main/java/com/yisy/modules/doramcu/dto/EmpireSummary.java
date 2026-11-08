package com.yisy.modules.doramcu.dto;

/**
 * Carries empire-specific details for the logged-in player's empire.
 * <p>
 * Includes empire name, government type, ethos, capital planet,
 * industrial capacity, and other empire-level statistics.
 * Also includes the three main empire components: Army, Navy, and Facilities.
 * </p>
 */
public class EmpireSummary {

    private int empireId;
    private String empireName;
    private String governmentType;
    private String ethos;
    private Integer capitalPlanetId;
    private int planetCount;
    private int fleetCount;

    // ---- Location Information ----
    private Integer capitalSystemId;
    private Integer capitalSectorRow;
    private Integer capitalSectorCol;
    private Integer capitalSubSectorRow;
    private Integer capitalSubSectorCol;

    private int industrialCapacity;
    private int industrialPlanetCount;

    // ---- Army Component ----
    private int armyUnitCount;
    private int armyTotalOffense;
    private int armyTotalDefense;
    private int armySupplyCost;

    // ---- Navy Component ----
    private int navyFleetCount;
    private int navyShipCount;
    private int navyTotalDamage;
    private int navyTotalShieldPoints;
    private int navyTotalHullPoints;

    // ---- Facilities Component ----
    private int facilityCount;
    private int spaceStationCount;
    private int mineralProduction;
    private int energyProduction;
    private int foodProduction;
    private int researchOutput;
    private int facilityDefenseBonus;
    private int creditIncome;

    // ---- Empire Status ----
    private double empireIntegrity;    // 0-100 percentage
    private int militaryPower;         // total fleet damage
    private int shipQueueSize;         // number of ships in build queue
    private int activeBattlesCount;    // number of active battles

    // ---- Mercenary Component (Traveller RPG) ----
    private MercenarySummaryDto mercenarySummary;

    public EmpireSummary() {
    }

    public int getEmpireId() {
        return empireId;
    }

    public void setEmpireId(int empireId) {
        this.empireId = empireId;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }

    public String getGovernmentType() {
        return governmentType;
    }

    public void setGovernmentType(String governmentType) {
        this.governmentType = governmentType;
    }

    public String getEthos() {
        return ethos;
    }

    public void setEthos(String ethos) {
        this.ethos = ethos;
    }

    public Integer getCapitalPlanetId() {
        return capitalPlanetId;
    }

    public void setCapitalPlanetId(Integer capitalPlanetId) {
        this.capitalPlanetId = capitalPlanetId;
    }

    public int getPlanetCount() {
        return planetCount;
    }

    public void setPlanetCount(int planetCount) {
        this.planetCount = planetCount;
    }

    public int getFleetCount() {
        return fleetCount;
    }

    public void setFleetCount(int fleetCount) {
        this.fleetCount = fleetCount;
    }

    public int getIndustrialCapacity() {
        return industrialCapacity;
    }

    public void setIndustrialCapacity(int industrialCapacity) {
        this.industrialCapacity = industrialCapacity;
    }

    public int getIndustrialPlanetCount() {
        return industrialPlanetCount;
    }

    public void setIndustrialPlanetCount(int industrialPlanetCount) {
        this.industrialPlanetCount = industrialPlanetCount;
    }

    // ---- Army Component ----

    public int getArmyUnitCount() { return armyUnitCount; }
    public void setArmyUnitCount(int armyUnitCount) { this.armyUnitCount = armyUnitCount; }
    public int getArmyTotalOffense() { return armyTotalOffense; }
    public void setArmyTotalOffense(int armyTotalOffense) { this.armyTotalOffense = armyTotalOffense; }
    public int getArmyTotalDefense() { return armyTotalDefense; }
    public void setArmyTotalDefense(int armyTotalDefense) { this.armyTotalDefense = armyTotalDefense; }
    public int getArmySupplyCost() { return armySupplyCost; }
    public void setArmySupplyCost(int armySupplyCost) { this.armySupplyCost = armySupplyCost; }

    // ---- Navy Component ----

    public int getNavyFleetCount() { return navyFleetCount; }
    public void setNavyFleetCount(int navyFleetCount) { this.navyFleetCount = navyFleetCount; }
    public int getNavyShipCount() { return navyShipCount; }
    public void setNavyShipCount(int navyShipCount) { this.navyShipCount = navyShipCount; }
    public int getNavyTotalDamage() { return navyTotalDamage; }
    public void setNavyTotalDamage(int navyTotalDamage) { this.navyTotalDamage = navyTotalDamage; }
    public int getNavyTotalShieldPoints() { return navyTotalShieldPoints; }
    public void setNavyTotalShieldPoints(int navyTotalShieldPoints) { this.navyTotalShieldPoints = navyTotalShieldPoints; }
    public int getNavyTotalHullPoints() { return navyTotalHullPoints; }
    public void setNavyTotalHullPoints(int navyTotalHullPoints) { this.navyTotalHullPoints = navyTotalHullPoints; }

    // ---- Facilities Component ----

    public int getFacilityCount() { return facilityCount; }
    public void setFacilityCount(int facilityCount) { this.facilityCount = facilityCount; }
    public int getSpaceStationCount() { return spaceStationCount; }
    public void setSpaceStationCount(int spaceStationCount) { this.spaceStationCount = spaceStationCount; }
    public int getMineralProduction() { return mineralProduction; }
    public void setMineralProduction(int mineralProduction) { this.mineralProduction = mineralProduction; }
    public int getEnergyProduction() { return energyProduction; }
    public void setEnergyProduction(int energyProduction) { this.energyProduction = energyProduction; }
    public int getFoodProduction() { return foodProduction; }
    public void setFoodProduction(int foodProduction) { this.foodProduction = foodProduction; }
    public int getResearchOutput() { return researchOutput; }
    public void setResearchOutput(int researchOutput) { this.researchOutput = researchOutput; }
    public int getFacilityDefenseBonus() { return facilityDefenseBonus; }
    public void setFacilityDefenseBonus(int facilityDefenseBonus) { this.facilityDefenseBonus = facilityDefenseBonus; }
    public int getCreditIncome() { return creditIncome; }
    public void setCreditIncome(int creditIncome) { this.creditIncome = creditIncome; }

    // ---- Location Information ----

    public Integer getCapitalSystemId() { return capitalSystemId; }
    public void setCapitalSystemId(Integer capitalSystemId) { this.capitalSystemId = capitalSystemId; }

    public Integer getCapitalSectorRow() { return capitalSectorRow; }
    public void setCapitalSectorRow(Integer capitalSectorRow) { this.capitalSectorRow = capitalSectorRow; }

    public Integer getCapitalSectorCol() { return capitalSectorCol; }
    public void setCapitalSectorCol(Integer capitalSectorCol) { this.capitalSectorCol = capitalSectorCol; }

    public Integer getCapitalSubSectorRow() { return capitalSubSectorRow; }
    public void setCapitalSubSectorRow(Integer capitalSubSectorRow) { this.capitalSubSectorRow = capitalSubSectorRow; }

    public Integer getCapitalSubSectorCol() { return capitalSubSectorCol; }
    public void setCapitalSubSectorCol(Integer capitalSubSectorCol) { this.capitalSubSectorCol = capitalSubSectorCol; }

    // ---- Empire Status ----

    public double getEmpireIntegrity() { return empireIntegrity; }
    public void setEmpireIntegrity(double empireIntegrity) { this.empireIntegrity = empireIntegrity; }

    public int getMilitaryPower() { return militaryPower; }
    public void setMilitaryPower(int militaryPower) { this.militaryPower = militaryPower; }

    public int getShipQueueSize() { return shipQueueSize; }
    public void setShipQueueSize(int shipQueueSize) { this.shipQueueSize = shipQueueSize; }

    public int getActiveBattlesCount() { return activeBattlesCount; }
    public void setActiveBattlesCount(int activeBattlesCount) { this.activeBattlesCount = activeBattlesCount; }

    // ---- Mercenary Component ----

    public MercenarySummaryDto getMercenarySummary() {
        return mercenarySummary;
    }

    public void setMercenarySummary(MercenarySummaryDto mercenarySummary) {
        this.mercenarySummary = mercenarySummary;
    }
}
