package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;

/**
 * DTO for creating a new mission.
 */
public class CreateMissionRequest {

    private Long characterId;
    private String name;
    private CaptainLogMission.MissionType type;

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

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
}
