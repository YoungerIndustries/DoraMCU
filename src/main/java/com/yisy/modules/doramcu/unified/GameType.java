package com.yisy.modules.doramcu.unified;

/**
 * Identifies which game system a character belongs to.
 */
public enum GameType {

    TRAVELLER("Traveller", "Classic space-faring RPG with careers, skills, and exploration"),
    CAPTAINS_LOG("Captain's Log", "Solo Star Trek Adventures RPG with missions and character development"),
    DORAMCU_4X("DoraMCU 4X", "4X empire strategy game — build and manage your galactic empire");

    private final String displayName;
    private final String description;

    GameType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
