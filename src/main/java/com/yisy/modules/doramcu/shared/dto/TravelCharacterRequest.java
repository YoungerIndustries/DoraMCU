package com.yisy.modules.doramcu.shared.dto;

import com.yisy.modules.doramcu.unified.GameType;

/**
 * DTO for requesting a character to travel to another system.
 */
public class TravelCharacterRequest {

    private GameType targetSystem;
    private String travelNotes;

    public GameType getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(GameType targetSystem) {
        this.targetSystem = targetSystem;
    }

    public String getTravelNotes() {
        return travelNotes;
    }

    public void setTravelNotes(String travelNotes) {
        this.travelNotes = travelNotes;
    }
}
