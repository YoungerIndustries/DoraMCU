package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a solar system within a galaxy sector.
 * <p>
 * Each solar system contains a central star, orbiting planets, and other celestial bodies.
 * The galaxy is organized as a 10x10 2D sector grid, and each solar system is positioned
 * within a sector using 3D coordinates (x, y, z) in a 100x100x100 cube.
 * </p>
 */
@Entity
@Table(name = "solar_systems", indexes = {
    @Index(name = "idx_solar_system_galaxy", columnList = "galaxy_id"),
    @Index(name = "idx_solar_system_sector", columnList = "sectorRow,sectorCol"),
    @Index(name = "idx_solar_system_position", columnList = "posX,posY,posZ")
})
public class SolarSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galaxy_id", nullable = false)
    private Galaxy galaxy;

    @Column(nullable = false)
    private String name;

    /** Sector row in the 10x10 grid (0-9). */
    @Column(nullable = false)
    private int sectorRow;

    /** Sector column in the 10x10 grid (0-9). */
    @Column(nullable = false)
    private int sectorCol;

    /** Subsector row in the 10x10 subsector grid (0-9). */
    @Column(nullable = false)
    private int subSectorRow;

    /** Subsector column in the 10x10 subsector grid (0-9). */
    @Column(nullable = false)
    private int subSectorCol;

    /** X position within the subsector (0-9). */
    @Column(nullable = false)
    private int posX;

    /** Y position within the subsector (0-9). */
    @Column(nullable = false)
    private int posY;

    /** Z position within the subsector (0-9). */
    @Column(nullable = false)
    private int posZ;

    @OneToOne(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Star star;

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Planet> planets = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Moon> moons = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AsteroidBelt> asteroidBelts = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GasGiant> gasGiants = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Nebula> nebulas = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BlackHole> blackHoles = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comet> comets = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SpaceStation> spaceStations = new ArrayList<>();

    @OneToMany(mappedBy = "solarSystem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Asteroid> asteroids = new ArrayList<>();

    public SolarSystem() {
    }

    public SolarSystem(Long id, String name, Galaxy galaxy, int sectorRow, int sectorCol, int subSectorRow, int subSectorCol, int posX, int posY, int posZ) {
        this.id = id;
        this.name = name;
        this.galaxy = galaxy;
        this.sectorRow = sectorRow;
        this.sectorCol = sectorCol;
        this.subSectorRow = subSectorRow;
        this.subSectorCol = subSectorCol;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSectorRow() {
        return sectorRow;
    }

    public void setSectorRow(int sectorRow) {
        this.sectorRow = sectorRow;
    }

    public int getSectorCol() {
        return sectorCol;
    }

    public void setSectorCol(int sectorCol) {
        this.sectorCol = sectorCol;
    }

    public int getSubSectorRow() {
        return subSectorRow;
    }

    public void setSubSectorRow(int subSectorRow) {
        this.subSectorRow = subSectorRow;
    }

    public int getSubSectorCol() {
        return subSectorCol;
    }

    public void setSubSectorCol(int subSectorCol) {
        this.subSectorCol = subSectorCol;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getPosZ() {
        return posZ;
    }

    public void setPosZ(int posZ) {
        this.posZ = posZ;
    }

    public Star getStar() {
        return star;
    }

    public void setStar(Star star) {
        this.star = star;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public void setPlanets(List<Planet> planets) {
        this.planets = planets;
    }

    public List<Moon> getMoons() {
        return moons;
    }

    public void setMoons(List<Moon> moons) {
        this.moons = moons;
    }

    public List<AsteroidBelt> getAsteroidBelts() {
        return asteroidBelts;
    }

    public void setAsteroidBelts(List<AsteroidBelt> asteroidBelts) {
        this.asteroidBelts = asteroidBelts;
    }

    public List<GasGiant> getGasGiants() {
        return gasGiants;
    }

    public void setGasGiants(List<GasGiant> gasGiants) {
        this.gasGiants = gasGiants;
    }

    public List<Nebula> getNebulas() {
        return nebulas;
    }

    public void setNebulas(List<Nebula> nebulas) {
        this.nebulas = nebulas;
    }

    public List<BlackHole> getBlackHoles() {
        return blackHoles;
    }

    public void setBlackHoles(List<BlackHole> blackHoles) {
        this.blackHoles = blackHoles;
    }

    public List<Comet> getComets() {
        return comets;
    }

    public void setComets(List<Comet> comets) {
        this.comets = comets;
    }

    public List<SpaceStation> getSpaceStations() {
        return spaceStations;
    }

    public void setSpaceStations(List<SpaceStation> spaceStations) {
        this.spaceStations = spaceStations;
    }

    public List<Asteroid> getAsteroids() {
        return asteroids;
    }

    public void setAsteroids(List<Asteroid> asteroids) {
        this.asteroids = asteroids;
    }

    @Override
    public String toString() {
        return "SolarSystem{id=" + id + ", name='" + name + "', sector=(" + sectorRow + "," + sectorCol + "), subsector=(" + subSectorRow + "," + subSectorCol + "), pos=(" + posX + "," + posY + "," + posZ + ")}";
    }
}
