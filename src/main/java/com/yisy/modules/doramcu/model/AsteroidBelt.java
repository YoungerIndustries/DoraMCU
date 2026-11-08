package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing an asteroid belt in a solar system.
 */
@Entity
@Table(name = "asteroid_belts", indexes = {
    @Index(name = "idx_asteroid_belt_solar_system", columnList = "solar_system_id")
})
public class AsteroidBelt {

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
    private int mineralYield;

    @Column(nullable = false)
    private int stationSlots;

    @Column(nullable = false)
    private double movementPenalty;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StrategicResource strategicDeposit;

    public AsteroidBelt() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
    public int getMineralYield() { return mineralYield; }
    public void setMineralYield(int mineralYield) { this.mineralYield = mineralYield; }
    public int getStationSlots() { return stationSlots; }
    public void setStationSlots(int stationSlots) { this.stationSlots = stationSlots; }
    public double getMovementPenalty() { return movementPenalty; }
    public void setMovementPenalty(double movementPenalty) { this.movementPenalty = movementPenalty; }
    public StrategicResource getStrategicDeposit() { return strategicDeposit; }
    public void setStrategicDeposit(StrategicResource strategicDeposit) { this.strategicDeposit = strategicDeposit; }

    @Override
    public String toString() {
        return "AsteroidBelt{id=" + id + ", name='" + name + "', yield=" + mineralYield + "}";
    }
}
