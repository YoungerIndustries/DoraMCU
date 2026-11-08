package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

/**
 * Request to purchase a career upgrade.
 * <p>
 * The player selects an upgrade type and optionally a target discipline or focus name.
 * Experience points are deducted from the character.
 * </p>
 */
public class CareerUpgradeRequest {

    /** Character ID. */
    private Long characterId;

    /** Type of upgrade to purchase. */
    private CareerProgressionResponse.CareerUpgrade.UpgradeType type;

    /** Target discipline (required for DISCIPLINE_INCREASE and ADD_FOCUS). */
    private CaptainLogCharacter.Discipline discipline;

    /** Focus name (required for ADD_FOCUS). */
    private String focusName;

    /** Value description (required for ADD_VALUE). */
    private String valueDescription;

    /** Index of value to retire (required for RETIRE_VALUE). */
    private Integer valueIndex;

    // --- Getters and Setters ---

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public CareerProgressionResponse.CareerUpgrade.UpgradeType getType() {
        return type;
    }

    public void setType(CareerProgressionResponse.CareerUpgrade.UpgradeType type) {
        this.type = type;
    }

    public CaptainLogCharacter.Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(CaptainLogCharacter.Discipline discipline) {
        this.discipline = discipline;
    }

    public String getFocusName() {
        return focusName;
    }

    public void setFocusName(String focusName) {
        this.focusName = focusName;
    }

    public String getValueDescription() {
        return valueDescription;
    }

    public void setValueDescription(String valueDescription) {
        this.valueDescription = valueDescription;
    }

    public Integer getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(Integer valueIndex) {
        this.valueIndex = valueIndex;
    }
}
