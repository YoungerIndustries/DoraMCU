package com.yisy.modules.doramcu.captainslog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a starship or station in the Captain's Log system.
 * <p>
 * A starship has systems (Comms, Computers, Engines, Sensors, Structure, Weapons),
 * departments, talents, class, scale, and attributes that mirror the character's disciplines.
 * </p>
 */
@Entity
@Table(name = "cl_starships", indexes = {
        @Index(name = "idx_cl_starship_name", columnList = "name")
})
public class CaptainLogStarship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Registry number (e.g., "NCC-1701"). */
    @Column(length = 50)
    private String registry;

    /** Ship trait 1. */
    @Column(length = 100)
    private String trait1;

    /** Ship trait 2. */
    @Column(length = 100)
    private String trait2;

    /** Ship trait 3. */
    @Column(length = 100)
    private String trait3;

    /** Ship class (e.g., CONSTITUTION, GALACTIC, DEFiant). */
    @Column(nullable = false, length = 50)
    private String shipClass;

    /** Ship scale (e.g., SMALL, MEDIUM, LARGE, CAPITAL). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Scale scale;

    /** Whether this is a station rather than a ship. */
    @Column(nullable = false)
    private boolean isStation = false;

    // --- Starship Systems (2d20 ratings) ---

    /** Communications system rating. */
    @Column(nullable = false)
    private int comms = 0;

    /** Computers system rating. */
    @Column(nullable = false)
    private int computers = 0;

    /** Engines system rating. */
    @Column(nullable = false)
    private int engines = 0;

    /** Sensors system rating. */
    @Column(nullable = false)
    private int sensors = 0;

    /** Structure system rating (hull integrity). */
    @Column(nullable = false)
    private int structure = 0;

    /** Weapons system rating. */
    @Column(nullable = false)
    private int weapons = 0;

    // --- System Stress Trackers ---

    /** Current stress on Comms system. */
    @Column(nullable = false)
    private int commsStress = 0;

    /** Current stress on Computers system. */
    @Column(nullable = false)
    private int computersStress = 0;

    /** Current stress on Engines system. */
    @Column(nullable = false)
    private int enginesStress = 0;

    /** Current stress on Sensors system. */
    @Column(nullable = false)
    private int sensorsStress = 0;

    /** Current stress on Structure system. */
    @Column(nullable = false)
    private int structureStress = 0;

    /** Current stress on Weapons system. */
    @Column(nullable = false)
    private int weaponsStress = 0;

    // --- Departments ---

    /** Command department rating. */
    @Column(nullable = false)
    private int command = 0;

    /** Science department rating. */
    @Column(nullable = false)
    private int science = 0;

    /** Security department rating. */
    @Column(nullable = false)
    private int security = 0;

    /** Engineering department rating. */
    @Column(nullable = false)
    private int engineering = 0;

    /** Medical department rating. */
    @Column(nullable = false)
    private int medical = 0;

    /** Operations department rating. */
    @Column(nullable = false)
    private int operations = 0;

    // --- Ship Talents ---

    @OneToMany(mappedBy = "starship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaptainLogShipTalent> talents = new ArrayList<>();

    /** Characters currently assigned to this starship. */
    @OneToMany(mappedBy = "assignedStarship")
    @JsonIgnore
    private List<CaptainLogCharacter> crew = new ArrayList<>();

    // --- Starship Attributes ---

    /** Attribute: Fitness (maneuverability). */
    @Column(nullable = false)
    private int fitness = 0;

    /** Attribute: Technical (systems capability). */
    @Column(nullable = false)
    private int technical = 0;

    /** Attribute: Strength (durability). */
    @Column(nullable = false)
    private int strength = 0;

    /** Attribute: Reason (computing power). */
    @Column(nullable = false)
    private int reason = 0;

    /** Attribute: Insight (sensor range). */
    @Column(nullable = false)
    private int insight = 0;

    /** Attribute: Presence (crew morale). */
    @Column(nullable = false)
    private int presence = 0;

    /**
     * Ship scale categories.
     */
    public enum Scale {
        SMALL, MEDIUM, LARGE, CAPITAL, STATION
    }

    /**
     * Starship systems used in 2d20 task attempts.
     * Target Number = System rating + Department rating.
     */
    public enum StarshipSystem {
        COMMS("Communications"),
        COMPUTERS("Computers"),
        ENGINES("Engines"),
        SENSORS("Sensors"),
        STRUCTURE("Structure"),
        WEAPONS("Weapons");

        private final String displayName;

        StarshipSystem(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Starship departments used in 2d20 task attempts.
     * Target Number = System rating + Department rating.
     */
    public enum StarshipDepartment {
        COMMAND("Command"),
        SCIENCE("Science"),
        SECURITY("Security"),
        ENGINEERING("Engineering"),
        MEDICAL("Medical"),
        OPERATIONS("Operations");

        private final String displayName;

        StarshipDepartment(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Returns the rating of a starship system by enum.
     */
    public int getSystemRating(StarshipSystem system) {
        return switch (system) {
            case COMMS -> comms;
            case COMPUTERS -> computers;
            case ENGINES -> engines;
            case SENSORS -> sensors;
            case STRUCTURE -> structure;
            case WEAPONS -> weapons;
        };
    }

    /**
     * Returns the rating of a starship department by enum.
     */
    public int getDepartmentRating(StarshipDepartment department) {
        return switch (department) {
            case COMMAND -> command;
            case SCIENCE -> science;
            case SECURITY -> security;
            case ENGINEERING -> engineering;
            case MEDICAL -> medical;
            case OPERATIONS -> operations;
        };
    }

    /**
     * Returns the current stress on a starship system by enum.
     */
    public int getSystemStress(StarshipSystem system) {
        return switch (system) {
            case COMMS -> commsStress;
            case COMPUTERS -> computersStress;
            case ENGINES -> enginesStress;
            case SENSORS -> sensorsStress;
            case STRUCTURE -> structureStress;
            case WEAPONS -> weaponsStress;
        };
    }

    /**
     * Sets the stress on a starship system by enum.
     */
    public void setSystemStress(StarshipSystem system, int stress) {
        switch (system) {
            case COMMS -> setCommsStress(stress);
            case COMPUTERS -> setComputersStress(stress);
            case ENGINES -> setEnginesStress(stress);
            case SENSORS -> setSensorsStress(stress);
            case STRUCTURE -> setStructureStress(stress);
            case WEAPONS -> setWeaponsStress(stress);
        }
    }

    /**
     * Adds stress to a starship system, capped at the system's rating (max stress = system rating).
     * @return true if stress was added, false if already at max
     */
    public boolean addSystemStress(StarshipSystem system, int amount) {
        int current = getSystemStress(system);
        int max = getSystemRating(system);
        if (max <= 0) return false; // No rating means no stress capacity
        int newStress = Math.min(max, current + amount);
        setSystemStress(system, newStress);
        return newStress > current;
    }

    /**
     * Reduces stress on a starship system.
     * @return true if stress was reduced, false if already at zero
     */
    public boolean reduceSystemStress(StarshipSystem system, int amount) {
        int current = getSystemStress(system);
        int newStress = Math.max(0, current - amount);
        if (newStress < current) {
            setSystemStress(system, newStress);
            return true;
        }
        return false;
    }

    /**
     * Returns the maximum hit points for this ship based on its scale.
     * Rulebook: Small=2, Medium=3, Large=4, Capital=5, Station=6.
     */
    public int getMaxHits() {
        return switch (scale) {
            case SMALL -> 2;
            case MEDIUM -> 3;
            case LARGE -> 4;
            case CAPITAL -> 5;
            case STATION -> 6;
        };
    }

    public CaptainLogStarship() {
    }

    public CaptainLogStarship(String name, String shipClass, Scale scale) {
        this.name = name;
        this.shipClass = shipClass;
        this.scale = scale;
    }

    // --- Getters and Setters ---

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

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getTrait1() {
        return trait1;
    }

    public void setTrait1(String trait1) {
        this.trait1 = trait1;
    }

    public String getTrait2() {
        return trait2;
    }

    public void setTrait2(String trait2) {
        this.trait2 = trait2;
    }

    public String getTrait3() {
        return trait3;
    }

    public void setTrait3(String trait3) {
        this.trait3 = trait3;
    }

    public String getShipClass() {
        return shipClass;
    }

    public void setShipClass(String shipClass) {
        this.shipClass = shipClass;
    }

    public Scale getScale() {
        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public boolean isStation() {
        return isStation;
    }

    public void setStation(boolean station) {
        isStation = station;
    }

    public int getComms() {
        return comms;
    }

    public void setComms(int comms) {
        this.comms = comms;
    }

    public int getComputers() {
        return computers;
    }

    public void setComputers(int computers) {
        this.computers = computers;
    }

    public int getEngines() {
        return engines;
    }

    public void setEngines(int engines) {
        this.engines = engines;
    }

    public int getSensors() {
        return sensors;
    }

    public void setSensors(int sensors) {
        this.sensors = sensors;
    }

    public int getStructure() {
        return structure;
    }

    public void setStructure(int structure) {
        this.structure = structure;
    }

    public int getWeapons() {
        return weapons;
    }

    public void setWeapons(int weapons) {
        this.weapons = weapons;
    }

    public int getCommsStress() {
        return commsStress;
    }

    public void setCommsStress(int commsStress) {
        this.commsStress = commsStress;
    }

    public int getComputersStress() {
        return computersStress;
    }

    public void setComputersStress(int computersStress) {
        this.computersStress = computersStress;
    }

    public int getEnginesStress() {
        return enginesStress;
    }

    public void setEnginesStress(int enginesStress) {
        this.enginesStress = enginesStress;
    }

    public int getSensorsStress() {
        return sensorsStress;
    }

    public void setSensorsStress(int sensorsStress) {
        this.sensorsStress = sensorsStress;
    }

    public int getStructureStress() {
        return structureStress;
    }

    public void setStructureStress(int structureStress) {
        this.structureStress = structureStress;
    }

    public int getWeaponsStress() {
        return weaponsStress;
    }

    public void setWeaponsStress(int weaponsStress) {
        this.weaponsStress = weaponsStress;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getScience() {
        return science;
    }

    public void setScience(int science) {
        this.science = science;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public int getEngineering() {
        return engineering;
    }

    public void setEngineering(int engineering) {
        this.engineering = engineering;
    }

    public int getMedical() {
        return medical;
    }

    public void setMedical(int medical) {
        this.medical = medical;
    }

    public int getOperations() {
        return operations;
    }

    public void setOperations(int operations) {
        this.operations = operations;
    }

    public List<CaptainLogShipTalent> getTalents() {
        return talents;
    }

    public void setTalents(List<CaptainLogShipTalent> talents) {
        this.talents = talents;
    }

    public List<CaptainLogCharacter> getCrew() {
        return crew;
    }

    public void setCrew(List<CaptainLogCharacter> crew) {
        this.crew = crew;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int getTechnical() {
        return technical;
    }

    public void setTechnical(int technical) {
        this.technical = technical;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public int getInsight() {
        return insight;
    }

    public void setInsight(int insight) {
        this.insight = insight;
    }

    public int getPresence() {
        return presence;
    }

    public void setPresence(int presence) {
        this.presence = presence;
    }

    @Override
    public String toString() {
        return "CaptainLogStarship{id=" + id + ", name='" + name + "', class='" + shipClass +
                "', scale=" + scale + "}";
    }
}
