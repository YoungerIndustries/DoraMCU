package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat;

/**
 * DTO for starting a new combat encounter.
 */
public class StartCombatRequest {

    /** Mission ID where the combat takes place. */
    private Long missionId;

    /** Character ID participating in the combat. */
    private Long characterId;

    /** Name of the opponent. */
    private String opponentName;

    /** Type of combat: PHYSICAL or STARSHIP. */
    private CaptainLogCombat.CombatType type;

    /** Scale of the opponent (for starship combat). */
    private CaptainLogCombat.OpponentScale opponentScale;

    /** Narrative description of the combat. */
    private String description;

    /** Attribute used for task attempts (e.g., FITNESS). */
    private CaptainLogCharacter.Attribute attribute;

    /** Discipline used for task attempts (e.g., SECURITY). */
    private CaptainLogCharacter.Discipline discipline;

    // --- Getters and Setters ---

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public CaptainLogCombat.CombatType getType() {
        return type;
    }

    public void setType(CaptainLogCombat.CombatType type) {
        this.type = type;
    }

    public CaptainLogCombat.OpponentScale getOpponentScale() {
        return opponentScale;
    }

    public void setOpponentScale(CaptainLogCombat.OpponentScale opponentScale) {
        this.opponentScale = opponentScale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CaptainLogCharacter.Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(CaptainLogCharacter.Attribute attribute) {
        this.attribute = attribute;
    }

    public CaptainLogCharacter.Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(CaptainLogCharacter.Discipline discipline) {
        this.discipline = discipline;
    }
}
