package com.yisy.modules.doramcu.captainslog.dto;

/**
 * DTO for creating a new campaign.
 */
public class CreateCampaignRequest {

    /** Campaign title. */
    private String title;

    /** Brief description of the campaign arc. */
    private String description;

    /** Ongoing NPCs involved in this campaign. */
    private String ongoingNpcs;

    /** Key locations in this campaign. */
    private String keyLocations;

    /** Unresolved plot threads. */
    private String plotThreads;

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

    public String getOngoingNpcs() {
        return ongoingNpcs;
    }

    public void setOngoingNpcs(String ongoingNpcs) {
        this.ongoingNpcs = ongoingNpcs;
    }

    public String getKeyLocations() {
        return keyLocations;
    }

    public void setKeyLocations(String keyLocations) {
        this.keyLocations = keyLocations;
    }

    public String getPlotThreads() {
        return plotThreads;
    }

    public void setPlotThreads(String plotThreads) {
        this.plotThreads = plotThreads;
    }
}
