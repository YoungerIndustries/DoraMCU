package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a moon orbiting a planet.
 */
@Entity
@Table(name = "moons", indexes = {
    @Index(name = "idx_moon_solar_system", columnList = "solar_system_id"),
    @Index(name = "idx_moon_parent_planet", columnList = "parent_planet_id")
})
public class Moon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solar_system_id", nullable = false)
    private SolarSystem solarSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_planet_id")
    private Planet parentPlanet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MoonSize moonSize;

    @Column(nullable = false)
    private int mineralYield;

    @Column(nullable = false)
    private int outpostSlots;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StrategicResource strategicDeposit;

    public enum MoonSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        HUGE
    }

    public enum StrategicResource {
        ELERIUM,
        ANTI_MATTER,
        THULIUM,
        DURANTIUM,
        PROMETHION
    }

    public Moon() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public Planet getParentPlanet() { return parentPlanet; }
    public void setParentPlanet(Planet parentPlanet) { this.parentPlanet = parentPlanet; }
    public MoonSize getMoonSize() { return moonSize; }
    public void setMoonSize(MoonSize moonSize) { this.moonSize = moonSize; }
    public int getMineralYield() { return mineralYield; }
    public void setMineralYield(int mineralYield) { this.mineralYield = mineralYield; }
    public int getOutpostSlots() { return outpostSlots; }
    public void setOutpostSlots(int outpostSlots) { this.outpostSlots = outpostSlots; }
    public StrategicResource getStrategicDeposit() { return strategicDeposit; }
    public void setStrategicDeposit(StrategicResource strategicDeposit) { this.strategicDeposit = strategicDeposit; }

    @Override
    public String toString() {
        return "Moon{id=" + id + ", name='" + name + "', size=" + moonSize + "}";
    }
}
