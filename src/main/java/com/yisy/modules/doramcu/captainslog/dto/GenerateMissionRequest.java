package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;

/**
 * DTO for AI-generated mission creation.
 * <p>
 * Allows the user to optionally specify a mission type, theme, or custom prompt
 * to guide the AI mission generation. If omitted, the AI generates a mission
 * based entirely on the character's profile and mission history.
 * </p>
 */
public class GenerateMissionRequest {

    /** The character for whom to generate the mission. */
    private Long characterId;

    /** Optional mission type to guide generation (e.g., EXPLORATION, DIPLOMACY, COMBAT). */
    private CaptainLogMission.MissionType type;

    /** Optional theme or keyword to guide the AI (e.g., "first contact", "time travel"). */
    private String theme;

    /** Optional custom prompt for the AI to use as additional context. */
    private String prompt;

    /** Optional AI provider to use (e.g., "ollama", "gemini"). Defaults to the configured provider. */
    private String provider;

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public CaptainLogMission.MissionType getType() {
        return type;
    }

    public void setType(CaptainLogMission.MissionType type) {
        this.type = type;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
