package com.yisy.modules.doramcu.traveller;

/**
 * Mission types for mercenary tickets in Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * There are four broad categories of mercenary service:
 * Cadre, Commando, Striker, and Security Force.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public enum MissionType {

    /**
     * Commando — specific raid or raiding force.
     * <p>
     * Hired to carry out a specific raid or serve as a raiding force
     * throughout a campaign.
     * </p>
     */
    COMMANDO("Commando", "Specific raid or raiding force", -1),

    /**
     * Cadre — training personnel for local forces.
     * <p>
     * Provide training personnel and a high proportion of officers
     * and senior NCOs for indigenous forces.
     * </p>
     */
    CADRE("Cadre", "Training personnel for local forces", +2),

    /**
     * Striker — complete mercenary units as spearhead forces.
     * <p>
     * Complete mercenary units hired to provide spearhead forces
     * to a largely indigenous army.
     * </p>
     */
    STRIKER("Striker", "Spearhead forces for indigenous army", +1),

    /**
     * Security — safeguard specific installations.
     * <p>
     * Hired to safeguard a specific installation or group of
     * installations for the duration of an anticipated crisis.
     * </p>
     */
    SECURITY("Security", "Safeguard specific installations", 0);

    private final String name;
    private final String description;
    private final int sizeDm;

    MissionType(String name, String description, int sizeDm) {
        this.name = name;
        this.description = description;
        this.sizeDm = sizeDm;
    }

    /**
     * Returns the name of the mission type.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the mission type.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the die modifier for unit size determination in abstract battles.
     *
     * @return the size DM
     */
    public int getSizeDm() {
        return sizeDm;
    }
}
