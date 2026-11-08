package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;

/**
 * DTO for creating a new Captain's Log starship or station.
 */
public class CreateStarshipRequest {

    private String name;
    private String registry;
    private String trait1;
    private String trait2;
    private String trait3;
    private String shipClass;
    private CaptainLogStarship.Scale scale;
    private boolean isStation;

    // Systems (optional — auto-generated if not provided)
    private Integer comms;
    private Integer computers;
    private Integer engines;
    private Integer sensors;
    private Integer structure;
    private Integer weapons;

    // Departments (optional)
    private Integer command;
    private Integer science;
    private Integer security;
    private Integer engineering;
    private Integer medical;
    private Integer operations;

    // Talents (optional)
    private String[] talents;

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

    public CaptainLogStarship.Scale getScale() {
        return scale;
    }

    public void setScale(CaptainLogStarship.Scale scale) {
        this.scale = scale;
    }

    public boolean isStation() {
        return isStation;
    }

    public void setStation(boolean station) {
        isStation = station;
    }

    public Integer getComms() {
        return comms;
    }

    public void setComms(Integer comms) {
        this.comms = comms;
    }

    public Integer getComputers() {
        return computers;
    }

    public void setComputers(Integer computers) {
        this.computers = computers;
    }

    public Integer getEngines() {
        return engines;
    }

    public void setEngines(Integer engines) {
        this.engines = engines;
    }

    public Integer getSensors() {
        return sensors;
    }

    public void setSensors(Integer sensors) {
        this.sensors = sensors;
    }

    public Integer getStructure() {
        return structure;
    }

    public void setStructure(Integer structure) {
        this.structure = structure;
    }

    public Integer getWeapons() {
        return weapons;
    }

    public void setWeapons(Integer weapons) {
        this.weapons = weapons;
    }

    public Integer getCommand() {
        return command;
    }

    public void setCommand(Integer command) {
        this.command = command;
    }

    public Integer getScience() {
        return science;
    }

    public void setScience(Integer science) {
        this.science = science;
    }

    public Integer getSecurity() {
        return security;
    }

    public void setSecurity(Integer security) {
        this.security = security;
    }

    public Integer getEngineering() {
        return engineering;
    }

    public void setEngineering(Integer engineering) {
        this.engineering = engineering;
    }

    public Integer getMedical() {
        return medical;
    }

    public void setMedical(Integer medical) {
        this.medical = medical;
    }

    public Integer getOperations() {
        return operations;
    }

    public void setOperations(Integer operations) {
        this.operations = operations;
    }

    public String[] getTalents() {
        return talents;
    }

    public void setTalents(String[] talents) {
        this.talents = talents;
    }
}
