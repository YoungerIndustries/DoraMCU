package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

/**
 * DTO for creating a new Captain's Log character.
 */
public class CreateCharacterRequest {

    private String name;
    private String pronouns;
    private String trait1;
    private String trait2;
    private String trait3;
    private CaptainLogCharacter.Rank rank;
    private CaptainLogCharacter.Assignment assignment;
    private CaptainLogCharacter.Environment environment;
    private CaptainLogCharacter.Outlook outlook;
    private CaptainLogCharacter.Education education;

    // Attributes (optional — auto-rolled if not provided)
    private Integer fitness;
    private Integer technical;
    private Integer strength;
    private Integer reason;
    private Integer insight;
    private Integer presence;

    // Disciplines (optional — derived from assignment if not provided)
    private Integer command;
    private Integer leadership;
    private Integer science;
    private Integer security;
    private Integer medicine;
    private Integer operations;

    // Values (up to 3)
    private String[] values;

    // Focuses (optional)
    private String[] focuses;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
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

    public CaptainLogCharacter.Rank getRank() {
        return rank;
    }

    public void setRank(CaptainLogCharacter.Rank rank) {
        this.rank = rank;
    }

    public CaptainLogCharacter.Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(CaptainLogCharacter.Assignment assignment) {
        this.assignment = assignment;
    }

    public CaptainLogCharacter.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(CaptainLogCharacter.Environment environment) {
        this.environment = environment;
    }

    public CaptainLogCharacter.Outlook getOutlook() {
        return outlook;
    }

    public void setOutlook(CaptainLogCharacter.Outlook outlook) {
        this.outlook = outlook;
    }

    public CaptainLogCharacter.Education getEducation() {
        return education;
    }

    public void setEducation(CaptainLogCharacter.Education education) {
        this.education = education;
    }

    public Integer getFitness() {
        return fitness;
    }

    public void setFitness(Integer fitness) {
        this.fitness = fitness;
    }

    public Integer getTechnical() {
        return technical;
    }

    public void setTechnical(Integer technical) {
        this.technical = technical;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public Integer getInsight() {
        return insight;
    }

    public void setInsight(Integer insight) {
        this.insight = insight;
    }

    public Integer getPresence() {
        return presence;
    }

    public void setPresence(Integer presence) {
        this.presence = presence;
    }

    public Integer getCommand() {
        return command;
    }

    public void setCommand(Integer command) {
        this.command = command;
    }

    public Integer getLeadership() {
        return leadership;
    }

    public void setLeadership(Integer leadership) {
        this.leadership = leadership;
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

    public Integer getMedicine() {
        return medicine;
    }

    public void setMedicine(Integer medicine) {
        this.medicine = medicine;
    }

    public Integer getOperations() {
        return operations;
    }

    public void setOperations(Integer operations) {
        this.operations = operations;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String[] getFocuses() {
        return focuses;
    }

    public void setFocuses(String[] focuses) {
        this.focuses = focuses;
    }
}
