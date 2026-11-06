package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a gas giant in a solar system.
 */
@Entity
@Table(name = "gas_giants", indexes = {
    @Index(name = "idx_gas_giant_solar_system", columnList = "solar_system_id")
})
public class GasGiant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solar_system_id", nullable = false)
    private SolarSystem solarSystem;

    @Column(nullable = false)
    private int radius;

    @Column(nullable = false)
    private int energyYield;

    @Column(nullable = false)
    private int stationSlots;

    @Column(nullable = false)
    private double gravitationalPull;

    @Column(nullable = false)
    private double slingshotBonus;

    public GasGiant() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
    public int getEnergyYield() { return energyYield; }
    public void setEnergyYield(int energyYield) { this.energyYield = energyYield; }
    public int getStationSlots() { return stationSlots; }
    public void setStationSlots(int stationSlots) { this.stationSlots = stationSlots; }
    public double getGravitationalPull() { return gravitationalPull; }
    public void setGravitationalPull(double gravitationalPull) { this.gravitationalPull = gravitationalPull; }
    public double getSlingshotBonus() { return slingshotBonus; }
    public void setSlingshotBonus(double slingshotBonus) { this.slingshotBonus = slingshotBonus; }

    @Override
    public String toString() {
        return "GasGiant{id=" + id + ", name='" + name + "', energy=" + energyYield + "}";
    }
}
