package com.yisy.modules.doramcu.captainslog.dto;

/**
 * Request to make a choice in a lifepath step.
 * <p>
 * The player selects one choice ID from the available options.
 * The system applies the choice's effects and returns the next step.
 * </p>
 */
public class LifepathChoiceRequest {

    /** The ID of the chosen option from the previous step. */
    private String choiceId;

    public String getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(String choiceId) {
        this.choiceId = choiceId;
    }
}
