package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Summary DTO for a TravellerCharacter commander.
 * <p>
 * Used in GameStateResponse to display the player's commander in the empire panel
 * with abilities, career, skills, and active bonuses.
 * </p>
 */
public class CommanderSummaryDto {

    private int characterId;
    private String name;
    private String career;
    private int careerTerm;
    private Map<String, Integer> abilities;
    private List<String> skills;
    private List<String> activeBonuses;
    private int credits;
    private int age;

    public CommanderSummaryDto() {
    }

    public int getCharacterId() {
        return characterId;
    }

    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public int getCareerTerm() {
        return careerTerm;
    }

    public void setCareerTerm(int careerTerm) {
        this.careerTerm = careerTerm;
    }

    public Map<String, Integer> getAbilities() {
        return abilities;
    }

    public void setAbilities(Map<String, Integer> abilities) {
        this.abilities = abilities;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getActiveBonuses() {
        return activeBonuses;
    }

    public void setActiveBonuses(List<String> activeBonuses) {
        this.activeBonuses = activeBonuses;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
