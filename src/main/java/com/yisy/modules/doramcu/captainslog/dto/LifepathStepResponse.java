package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;

import java.util.List;

/**
 * Response for a single lifepath step.
 * <p>
 * Contains the narrative prompt, available choices, and the current step number.
 * The player selects one choice to proceed to the next step.
 * </p>
 */
public class LifepathStepResponse {

    /** Current step number (1-8). */
    private int stepNumber;

    /** Total number of steps in the lifepath. */
    private int totalSteps;

    /** Name of the current step (e.g., "Background", "Environment"). */
    private String stepName;

    /** Narrative prompt describing the current life stage. */
    private String narrative;

    /** Available choices for this step. */
    private List<LifepathChoice> choices;

    /** Character name entered at the start (null for step 1). */
    private String characterName;

    /** The session ID for this lifepath creation (returned on start). */
    private Long sessionId;

    public LifepathStepResponse() {
    }

    public LifepathStepResponse(int stepNumber, int totalSteps, String stepName, String narrative,
                                List<LifepathChoice> choices, String characterName) {
        this.stepNumber = stepNumber;
        this.totalSteps = totalSteps;
        this.stepName = stepName;
        this.narrative = narrative;
        this.choices = choices;
        this.characterName = characterName;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public List<LifepathChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<LifepathChoice> choices) {
        this.choices = choices;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
