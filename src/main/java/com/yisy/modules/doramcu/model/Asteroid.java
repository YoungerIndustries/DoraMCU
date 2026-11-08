package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing an individual asteroid in a solar system.
 * <p>
 * Asteroids are smaller celestial bodies that can be mined for minerals
 * and may contain strategic resource deposits.
 * </p>
 */
@Entity
@Table(name = "asteroids", indexes = {
    @Index(name = "idx_asteroid_solar_system", columnList = "solar_system_id"),
    @Index(name = "idx_asteroid_size", columnList = "asteroidSize")
})
public class Asteroid {

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
    private AsteroidSize asteroidSize;

    @Column(nullable = false)
    private int mineralYield;

    @Column(nullable = false)
    private int outpostSlots;

    @Column(nullable = false)
    private double movementPenalty;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StrategicResource strategicDeposit;

    public enum AsteroidSize {
        PEBBLE,
        SMALL,
        MEDIUM,
        LARGE,
        PROTOPLANET
    }

    public Asteroid() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public AsteroidSize getAsteroidSize() { return asteroidSize; }
    public void setAsteroidSize(AsteroidSize asteroidSize) { this.asteroidSize = asteroidSize; }
    public int getMineralYield() { return mineralYield; }
    public void setMineralYield(int mineralYield) { this.mineralYield = mineralYield; }
    public int getOutpostSlots() { return outpostSlots; }
    public void setOutpostSlots(int outpostSlots) { this.outpostSlots = outpostSlots; }
    public double getMovementPenalty() { return movementPenalty; }
    public void setMovementPenalty(double movementPenalty) { this.movementPenalty = movementPenalty; }
    public StrategicResource getStrategicDeposit() { return strategicDeposit; }
    public void setStrategicDeposit(StrategicResource strategicDeposit) { this.strategicDeposit = strategicDeposit; }

    @Override
    public String toString() {
        return "Asteroid{id=" + id + ", name='" + name + "', size=" + asteroidSize + "}";
    }
}
