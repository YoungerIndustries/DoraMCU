package com.yisy.modules.doramcu.captainslog.dto;

/**
 * Request to create a new starship talent.
 */
public class CreateShipTalentRequest {

    private String name;
    private String description;

    public CreateShipTalentRequest() {
    }

    public CreateShipTalentRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

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
}
