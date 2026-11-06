package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a comet in a solar system.
 */
@Entity
@Table(name = "comets", indexes = {
    @Index(name = "idx_comet_solar_system", columnList = "solar_system_id")
})
public class Comet {

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
    private CometSize cometSize;

    @Column(nullable = false)
    private int resourceBonus;

    @Column(nullable = false)
    private int durationTurns;

    @Column(nullable = false)
    private int remainingTurns;

    @Column(nullable = false)
    private boolean harvested;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StrategicResource strategicDeposit;

    public enum CometSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        MASSIVE
    }

    public enum StrategicResource {
        ELERIUM,
        ANTI_MATTER,
        THULIUM,
        DURANTIUM,
        PROMETHION
    }

    public Comet() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SolarSystem getSolarSystem() { return solarSystem; }
    public void setSolarSystem(SolarSystem solarSystem) { this.solarSystem = solarSystem; }
    public CometSize getCometSize() { return cometSize; }
    public void setCometSize(CometSize cometSize) { this.cometSize = cometSize; }
    public int getResourceBonus() { return resourceBonus; }
    public void setResourceBonus(int resourceBonus) { this.resourceBonus = resourceBonus; }
    public int getDurationTurns() { return durationTurns; }
    public void setDurationTurns(int durationTurns) { this.durationTurns = durationTurns; }
    public int getRemainingTurns() { return remainingTurns; }
    public void setRemainingTurns(int remainingTurns) { this.remainingTurns = remainingTurns; }
    public boolean isHarvested() { return harvested; }
    public void setHarvested(boolean harvested) { this.harvested = harvested; }
    public StrategicResource getStrategicDeposit() { return strategicDeposit; }
    public void setStrategicDeposit(StrategicResource strategicDeposit) { this.strategicDeposit = strategicDeposit; }

    @Override
    public String toString() {
        return "Comet{id=" + id + ", name='" + name + "', bonus=" + resourceBonus + "}";
    }
}
