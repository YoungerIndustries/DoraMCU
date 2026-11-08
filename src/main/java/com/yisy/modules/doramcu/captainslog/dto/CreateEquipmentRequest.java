package com.yisy.modules.doramcu.captainslog.dto;

/**
 * DTO for creating a new piece of equipment for a Captain's Log character.
 */
public class CreateEquipmentRequest {

    /** Equipment name (e.g., "Phaser Type-2", "Tricorder"). */
    private String name;

    /** Detailed description of the equipment. */
    private String description;

    /** Equipment category (e.g., WEAPON, TOOL, COMMUNICATION, MEDICAL, SHIELD, UTILITY). */
    private String category;

    /** Optional boon bonus — this equipment provides a boon on specific check types. */
    private String boonOn;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBoonOn() {
        return boonOn;
    }

    public void setBoonOn(String boonOn) {
        this.boonOn = boonOn;
    }
}
