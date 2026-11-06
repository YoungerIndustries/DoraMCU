package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a star in a solar system.
 * <p>
 * Stars are classified by size (TINY to MASSIVE) and type (RED, YELLOW, BLUE, PURPLE).
 * Each star provides energy, radiation hazards, and gravity well effects.
 * </p>
 */
@Entity
@Table(name = "stars", indexes = {
    @Index(name = "idx_star_solar_system", columnList = "solar_system_id"),
    @Index(name = "idx_star_type", columnList = "starType"),
    @Index(name = "idx_star_size", columnList = "starSize")
})
public class Star {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solar_system_id", nullable = false)
    private SolarSystem solarSystem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StarSize starSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StarType starType;

    @Column(nullable = false)
    private int radius;

    @Column(nullable = false)
    private int energyYield;

    @Column(nullable = false)
    private int stationSlots;

    @Column(nullable = false)
    private int occupiedSlots;

    @Column(nullable = false)
    private double radiationDamage;

    @Column(nullable = false)
    private double gravityWellBonus;

    /** Star size classification controlling physical properties. */
    public enum StarSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        MASSIVE
    }

    /** Star type based on Galactic Civilizations IV model. */
    public enum StarType {
        RED,
        YELLOW,
        BLUE,
        PURPLE
    }

    public Star() {
    }

    public Star(Long id, String name, SolarSystem solarSystem, StarSize starSize, StarType starType) {
        this.id = id;
        this.name = name;
        this.solarSystem = solarSystem;
        this.starSize = starSize;
        this.starType = starType;
        this.radius = 5;
        this.energyYield = 10;
        this.stationSlots = 2;
        this.occupiedSlots = 0;
        this.radiationDamage = 0.1;
        this.gravityWellBonus = 0.3;
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

    public StarSize getStarSize() {
        return starSize;
    }

    public void setStarSize(StarSize starSize) {
        this.starSize = starSize;
    }

    public StarType getStarType() {
        return starType;
    }

    public void setStarType(StarType starType) {
        this.starType = starType;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getEnergyYield() {
        return energyYield;
    }

    public void setEnergyYield(int energyYield) {
        this.energyYield = energyYield;
    }

    public int getStationSlots() {
        return stationSlots;
    }

    public void setStationSlots(int stationSlots) {
        this.stationSlots = stationSlots;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(int occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public double getRadiationDamage() {
        return radiationDamage;
    }

    public void setRadiationDamage(double radiationDamage) {
        this.radiationDamage = radiationDamage;
    }

    public double getGravityWellBonus() {
        return gravityWellBonus;
    }

    public void setGravityWellBonus(double gravityWellBonus) {
        this.gravityWellBonus = gravityWellBonus;
    }

    @Override
    public String toString() {
        return "Star{id=" + id + ", name='" + name + "', size=" + starSize + ", type=" + starType + "}";
    }
}
