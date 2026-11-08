package com.yisy.modules.doramcu.captainslog.dto;

import java.util.List;

/**
 * Response for a step in the starship creation wizard.
 */
public class StarshipCreationStepResponse {

    private long sessionId;
    private int step;
    private int totalSteps;
    private String title;
    private String description;
    private String narrative;
    private List<StarshipCreationChoice> choices;

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public List<StarshipCreationChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<StarshipCreationChoice> choices) {
        this.choices = choices;
    }
}
