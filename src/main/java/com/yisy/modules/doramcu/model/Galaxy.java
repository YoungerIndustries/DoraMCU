package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a galaxy in the 4X space game.
 * <p>
 * A galaxy contains 100 2D sectors arranged in a 10x10 grid.
 * Each sector is a 3D cube of 100x100x100 dimensions containing solar systems.
 * </p>
 */
@Entity
@Table(name = "galaxies")
public class Galaxy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalSectors = 100;

    @Column(nullable = false)
    private double hyperlaneDensity;

    @OneToMany(mappedBy = "galaxy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolarSystem> solarSystems = new ArrayList<>();

    @OneToMany(mappedBy = "galaxy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sector> sectors = new ArrayList<>();

    public Galaxy() {
    }

    public Galaxy(Long id, String name) {
        this.id = id;
        this.name = name;
        this.hyperlaneDensity = 1.0;
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

    public int getTotalSectors() {
        return totalSectors;
    }

    public void setTotalSectors(int totalSectors) {
        this.totalSectors = totalSectors;
    }

    public double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    public void setHyperlaneDensity(double hyperlaneDensity) {
        this.hyperlaneDensity = hyperlaneDensity;
    }

    public List<SolarSystem> getSolarSystems() {
        return solarSystems;
    }

    public void setSolarSystems(List<SolarSystem> solarSystems) {
        this.solarSystems = solarSystems;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    @Override
    public String toString() {
        return "Galaxy{id=" + id + ", name='" + name + "', sectors=" + totalSectors + ", systems=" + solarSystems.size() + "}";
    }
}
