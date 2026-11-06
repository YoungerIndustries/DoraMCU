package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a nebula in a solar system.
 */
@Entity
@Table(name = "nebulas", indexes = {
    @Index(name = "idx_nebula_solar_system", columnList = "solar_system_id")
})
public class Nebula {

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
    private NebulaSize nebulaSize;

    @Column(nullable = false)
    private int radius;

    @Column(nullable = false)
    private int eleriumYield;

    @Column(nullable = false)
    private int stationSlots;

    @Column(nullable = false)
    private double sensorJamming;

    @Column(nullable = false)
    private double cloakingBonus;

    public enum NebulaSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        MASSIVE
    }

    public Nebula() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public NebulaSize getNebulaSize() { return nebulaSize; }
    public void setNebulaSize(NebulaSize nebulaSize) { this.nebulaSize = nebulaSize; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
    public int getEleriumYield() { return eleriumYield; }
    public void setEleriumYield(int eleriumYield) { this.eleriumYield = eleriumYield; }
    public int getStationSlots() { return stationSlots; }
    public void setStationSlots(int stationSlots) { this.stationSlots = stationSlots; }
    public double getSensorJamming() { return sensorJamming; }
    public void setSensorJamming(double sensorJamming) { this.sensorJamming = sensorJamming; }
    public double getCloakingBonus() { return cloakingBonus; }
    public void setCloakingBonus(double cloakingBonus) { this.cloakingBonus = cloakingBonus; }

    @Override
    public String toString() {
        return "Nebula{id=" + id + ", name='" + name + "', elerium=" + eleriumYield + "}";
    }
}
