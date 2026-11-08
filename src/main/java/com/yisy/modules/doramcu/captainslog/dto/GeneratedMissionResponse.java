package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;

/**
 * DTO representing an AI-generated mission proposal.
 * <p>
 * Contains the mission name, type, a 3-act structure with narrative prompts,
 * suggested skill checks, and oracle questions for each act.
 * </p>
 */
public class GeneratedMissionResponse {

    /** The generated mission name. */
    private String name;

    /** The mission type. */
    private CaptainLogMission.MissionType type;

    /** Brief mission synopsis (1-2 sentences). */
    private String synopsis;

    /** Act 1 (Opening Log) — the mission hook and initial situation. */
    private String act1Prompt;

    /** Act 2 (Supplementary Logs) — the rising action and complications. */
    private String act2Prompt;

    /** Act 3 (Closing Log) — the resolution and aftermath. */
    private String act3Prompt;

    /** Suggested skill checks for the mission (discipline and target numbers). */
    private String skillChecks;

    /** Suggested oracle questions to ask during the mission. */
    private String oracleQuestions;

    /** Key NPCs or entities involved in the mission. */
    private String keyCharacters;

    /** Potential values that may be used or challenged. */
    private String valuesAtStake;

    /** The AI provider that generated this mission. */
    private String provider;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CaptainLogMission.MissionType getType() {
        return type;
    }

    public void setType(CaptainLogMission.MissionType type) {
        this.type = type;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getAct1Prompt() {
        return act1Prompt;
    }

    public void setAct1Prompt(String act1Prompt) {
        this.act1Prompt = act1Prompt;
    }

    public String getAct2Prompt() {
        return act2Prompt;
    }

    public void setAct2Prompt(String act2Prompt) {
        this.act2Prompt = act2Prompt;
    }

    public String getAct3Prompt() {
        return act3Prompt;
    }

    public void setAct3Prompt(String act3Prompt) {
        this.act3Prompt = act3Prompt;
    }

    public String getSkillChecks() {
        return skillChecks;
    }

    public void setSkillChecks(String skillChecks) {
        this.skillChecks = skillChecks;
    }

    public String getOracleQuestions() {
        return oracleQuestions;
    }

    public void setOracleQuestions(String oracleQuestions) {
        this.oracleQuestions = oracleQuestions;
    }

    public String getKeyCharacters() {
        return keyCharacters;
    }

    public void setKeyCharacters(String keyCharacters) {
        this.keyCharacters = keyCharacters;
    }

    public String getValuesAtStake() {
        return valuesAtStake;
    }

    public void setValuesAtStake(String valuesAtStake) {
        this.valuesAtStake = valuesAtStake;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
