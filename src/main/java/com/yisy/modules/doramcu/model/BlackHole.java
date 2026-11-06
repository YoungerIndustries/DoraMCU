package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a black hole in a solar system.
 */
@Entity
@Table(name = "black_holes", indexes = {
    @Index(name = "idx_black_hole_solar_system", columnList = "solar_system_id")
})
public class BlackHole {

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
    private BlackHoleSize blackHoleSize;

    @Column(nullable = false)
    private int radius;

    @Column(nullable = false)
    private int antiMatterYield;

    @Column(nullable = false)
    private int stationSlots;

    @Column(nullable = false)
    private double gravitationalPull;

    @Column(nullable = false)
    private double destructionChance;

    public enum BlackHoleSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        MASSIVE
    }

    public BlackHole() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public BlackHoleSize getBlackHoleSize() { return blackHoleSize; }
    public void setBlackHoleSize(BlackHoleSize blackHoleSize) { this.blackHoleSize = blackHoleSize; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
    public int getAntiMatterYield() { return antiMatterYield; }
    public void setAntiMatterYield(int antiMatterYield) { this.antiMatterYield = antiMatterYield; }
    public int getStationSlots() { return stationSlots; }
    public void setStationSlots(int stationSlots) { this.stationSlots = stationSlots; }
    public double getGravitationalPull() { return gravitationalPull; }
    public void setGravitationalPull(double gravitationalPull) { this.gravitationalPull = gravitationalPull; }
    public double getDestructionChance() { return destructionChance; }
    public void setDestructionChance(double destructionChance) { this.destructionChance = destructionChance; }

    @Override
    public String toString() {
        return "BlackHole{id=" + id + ", name='" + name + "', antiMatter=" + antiMatterYield + "}";
    }
}
