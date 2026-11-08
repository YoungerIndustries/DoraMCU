package com.yisy.modules.doramcu.traveller;

/**
 * Alien races of the Third Imperium from Classic Traveller S03: Spinward Marches.
 * <p>
 * Each race has unique traits affecting combat, skills, trade, and social interactions.
 * </p>
 * <p>
 * Derived from Classic Traveller Supplement 3: The Spinward Marches (1978).
 * </p>
 */
public enum AlienRace {

    /**
     * Humans — the dominant species of the Imperium.
     */
    HUMAN("Human", "The dominant species of the Third Imperium.",
            0, 0, 0, 0, 0),

    /**
     * Aslan — cat-like humanoids from the Aslan Compact.
     * +1 melee combat, -1 social with humans, enhanced senses.
     */
    ASLAN("Aslan", "Cat-like humanoids from the Aslan Compact.",
            1, -1, 0, 0, 0),

    /**
     * Vargr — wolf-like humanoids, nomadic raiders.
     * +1 navigation, pack tactics bonus in combat.
     */
    VARGR("Vargr", "Wolf-like humanoids, nomadic and tribal.",
            0, 0, 1, 0, 0),

    /**
     * Insectoids — insect-like, hive-minded beings.
     * Chitin armor bonus, hive mind communication.
     */
    INSECTOID("Insectoid", "Insect-like, hive-minded beings.",
            0, 0, 0, 1, 0),

    /**
     * Melniki — small, rodent-like traders.
     * +2 trade profit, -1 combat.
     */
    MELNIKI("Melniki", "Small, rodent-like traders.",
            -1, 0, 0, 0, 2),

    /**
     * Tiltean — tall, elegant humanoids with enhanced grace.
     * +1 dexterity-based skills, +1 social.
     */
    TILTEAN("Tiltean", "Tall, elegant humanoids with enhanced grace.",
            0, 1, 0, 0, 0),

    /**
     * Zhodani — human-like with telepathic abilities.
     * +1 psionic checks, -1 with suspicious authorities.
     */
    ZHODANI("Zhodani", "Human-like with natural telepathic abilities.",
            0, 0, 0, 0, 0),

    /**
     * Loxi — small, bird-like humanoids.
     * +1 stealth, -1 strength-based tasks.
     */
    LOXI("Loxi", "Small, bird-like humanoids.",
            -1, 0, 0, 0, 0);

    private final String displayName;
    private final String description;

    /**
     * Combat die modifier (DM) for melee and firearms.
     */
    private final int combatDM;

    /**
     * Social die modifier for interaction with humans.
     */
    private final int socialDM;

    /**
     * Navigation die modifier for piloting and navigation.
     */
    private final int navigationDM;

    /**
     * Armor bonus (extra armor class).
     */
    private final int armorBonus;

    /**
     * Trade profit bonus percentage.
     */
    private final int tradeBonusPercent;

    AlienRace(String displayName, String description, int combatDM, int socialDM,
              int navigationDM, int armorBonus, int tradeBonusPercent) {
        this.displayName = displayName;
        this.description = description;
        this.combatDM = combatDM;
        this.socialDM = socialDM;
        this.navigationDM = navigationDM;
        this.armorBonus = armorBonus;
        this.tradeBonusPercent = tradeBonusPercent;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getCombatDM() { return combatDM; }
    public int getSocialDM() { return socialDM; }
    public int getNavigationDM() { return navigationDM; }
    public int getArmorBonus() { return armorBonus; }
    public int getTradeBonusPercent() { return tradeBonusPercent; }

    @Override
    public String toString() {
        return displayName;
    }
}
