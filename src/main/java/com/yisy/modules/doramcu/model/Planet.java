package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a planet in a solar system.
 * <p>
 * Planets have size, mineral quality, resource production, population stats,
 * and strategic resource deposits. They are the primary source of resources
 * for empire building.
 * </p>
 */
@Entity
@Table(name = "planets", indexes = {
    @Index(name = "idx_planet_solar_system", columnList = "solar_system_id"),
    @Index(name = "idx_planet_size", columnList = "planetSize"),
    @Index(name = "idx_planet_mineral_quality", columnList = "mineralQuality")
})
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solar_system_id", nullable = false)
    private SolarSystem solarSystem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PlanetSize planetSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MineralQuality mineralQuality;

    @Column(nullable = false)
    private int radius;

    @Column(nullable = false)
    private int mineralProduction;

    @Column(nullable = false)
    private int energyProduction;

    @Column(nullable = false)
    private int foodProduction;

    @Column(nullable = false)
    private int researchProduction;

    @Column(nullable = false)
    private int influenceProduction;

    @Column(nullable = false)
    private int currentPopulation;

    @Column(nullable = false)
    private int maxPopulation;

    @Column(nullable = false)
    private int socialStat;

    @Column(nullable = false)
    private int intelligenceStat;

    @Column(nullable = false)
    private int diligenceStat;

    @Column(nullable = false)
    private int resolveStat;

    @ElementCollection(targetClass = StrategicResource.class)
    @CollectionTable(name = "planet_strategic_deposits", joinColumns = @JoinColumn(name = "planet_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "resource")
    private Set<StrategicResource> strategicDeposits = new HashSet<>();

    /** Empire that currently owns this planet (null if unclaimed). */
    @Column(name = "owner_empire_id")
    private Integer ownerEmpireId;

    /** Planet size determining capacity and yield. */
    public enum PlanetSize {
        SMALL,
        MEDIUM,
        LARGE,
        HUGE
    }

    /** Mineral quality affecting production bonuses. */
    public enum MineralQuality {
        ULTRA_POOR,
        POOR,
        NORMAL,
        RICH,
        ULTRA_RICH
    }

    public Planet() {
    }

    public Planet(Long id, String name, SolarSystem solarSystem, PlanetSize planetSize, MineralQuality mineralQuality) {
        this.id = id;
        this.name = name;
        this.solarSystem = solarSystem;
        this.planetSize = planetSize;
        this.mineralQuality = mineralQuality;
        this.radius = 8;
        this.mineralProduction = 5;
        this.energyProduction = 5;
        this.foodProduction = 6;
        this.researchProduction = 4;
        this.influenceProduction = 5;
        this.currentPopulation = 0;
        this.maxPopulation = 10;
        this.socialStat = 5;
        this.intelligenceStat = 5;
        this.diligenceStat = 5;
        this.resolveStat = 5;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SolarSystem getSolarSystem() {
        return solarSystem;
    }

    public void setSolarSystem(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    public PlanetSize getPlanetSize() {
        return planetSize;
    }

    public void setPlanetSize(PlanetSize planetSize) {
        this.planetSize = planetSize;
    }

    public MineralQuality getMineralQuality() {
        return mineralQuality;
    }

    public void setMineralQuality(MineralQuality mineralQuality) {
        this.mineralQuality = mineralQuality;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getMineralProduction() {
        return mineralProduction;
    }

    public void setMineralProduction(int mineralProduction) {
        this.mineralProduction = mineralProduction;
    }

    public int getEnergyProduction() {
        return energyProduction;
    }

    public void setEnergyProduction(int energyProduction) {
        this.energyProduction = energyProduction;
    }

    public int getFoodProduction() {
        return foodProduction;
    }

    public void setFoodProduction(int foodProduction) {
        this.foodProduction = foodProduction;
    }

    public int getResearchProduction() {
        return researchProduction;
    }

    public void setResearchProduction(int researchProduction) {
        this.researchProduction = researchProduction;
    }

    public int getInfluenceProduction() {
        return influenceProduction;
    }

    public void setInfluenceProduction(int influenceProduction) {
        this.influenceProduction = influenceProduction;
    }

    public int getCurrentPopulation() {
        return currentPopulation;
    }

    public void setCurrentPopulation(int currentPopulation) {
        this.currentPopulation = currentPopulation;
    }

    public int getMaxPopulation() {
        return maxPopulation;
    }

    public void setMaxPopulation(int maxPopulation) {
        this.maxPopulation = maxPopulation;
    }

    public int getSocialStat() {
        return socialStat;
    }

    public void setSocialStat(int socialStat) {
        this.socialStat = socialStat;
    }

    public int getIntelligenceStat() {
        return intelligenceStat;
    }

    public void setIntelligenceStat(int intelligenceStat) {
        this.intelligenceStat = intelligenceStat;
    }

    public int getDiligenceStat() {
        return diligenceStat;
    }

    public void setDiligenceStat(int diligenceStat) {
        this.diligenceStat = diligenceStat;
    }

    public int getResolveStat() {
        return resolveStat;
    }

    public void setResolveStat(int resolveStat) {
        this.resolveStat = resolveStat;
    }

    public Set<StrategicResource> getStrategicDeposits() {
        return strategicDeposits;
    }

    public void setStrategicDeposits(Set<StrategicResource> strategicDeposits) {
        this.strategicDeposits = strategicDeposits;
    }

    public Integer getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(Integer ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    @Override
    public String toString() {
        return "Planet{id=" + id + ", name='" + name + "', size=" + planetSize + ", quality=" + mineralQuality + "}";
    }
}
