package com.yisy.modules.doramcu.captainslog.dto;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacterTalent;

/**
 * Request to create a new character talent.
 */
public class CreateCharacterTalentRequest {

    private String name;
    private String description;
    private String mechanicalEffect;
    private CaptainLogCharacterTalent.TalentSource source;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMechanicalEffect() {
        return mechanicalEffect;
    }

    public void setMechanicalEffect(String mechanicalEffect) {
        this.mechanicalEffect = mechanicalEffect;
    }

    public CaptainLogCharacterTalent.TalentSource getSource() {
        return source;
    }

    public void setSource(CaptainLogCharacterTalent.TalentSource source) {
        this.source = source;
    }
}
