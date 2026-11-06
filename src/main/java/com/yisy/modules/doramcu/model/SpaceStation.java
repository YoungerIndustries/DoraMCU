package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a space station in a solar system.
 */
@Entity
@Table(name = "space_stations", indexes = {
    @Index(name = "idx_space_station_solar_system", columnList = "solar_system_id")
})
public class SpaceStation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solar_system_id", nullable = false)
    private SolarSystem solarSystem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StationType stationType;

    @Column(nullable = false)
    private int hexX;

    @Column(nullable = false)
    private int hexY;

    @Column(nullable = false)
    private int hexZ;

    @Column(nullable = false)
    private int hullPoints;

    @Column(nullable = false)
    private int currentHull;

    @Column(nullable = false)
    private int shieldPoints;

    @Column(nullable = false)
    private int currentShield;

    @Column(nullable = false)
    private int componentSlots;

    @Column(nullable = false)
    private int occupiedSlots;

    @Column(nullable = false)
    private int damage;

    public enum StationType {
        MILITARY,
        SHIPYARD,
        MINING,
        RESEARCH,
        TRADE,
        COLONY
    }

    public SpaceStation() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public StationType getStationType() { return stationType; }
    public void setStationType(StationType stationType) { this.stationType = stationType; }
    public int getHexX() { return hexX; }
    public void setHexX(int hexX) { this.hexX = hexX; }
    public int getHexY() { return hexY; }
    public void setHexY(int hexY) { this.hexY = hexY; }
    public int getHexZ() { return hexZ; }
    public void setHexZ(int hexZ) { this.hexZ = hexZ; }
    public int getHullPoints() { return hullPoints; }
    public void setHullPoints(int hullPoints) { this.hullPoints = hullPoints; }
    public int getCurrentHull() { return currentHull; }
    public void setCurrentHull(int currentHull) { this.currentHull = currentHull; }
    public int getShieldPoints() { return shieldPoints; }
    public void setShieldPoints(int shieldPoints) { this.shieldPoints = shieldPoints; }
    public int getCurrentShield() { return currentShield; }
    public void setCurrentShield(int currentShield) { this.currentShield = currentShield; }
    public int getComponentSlots() { return componentSlots; }
    public void setComponentSlots(int componentSlots) { this.componentSlots = componentSlots; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public void setOccupiedSlots(int occupiedSlots) { this.occupiedSlots = occupiedSlots; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    @Override
    public String toString() {
        return "SpaceStation{id=" + id + ", name='" + name + "', type=" + stationType + "}";
    }
}
