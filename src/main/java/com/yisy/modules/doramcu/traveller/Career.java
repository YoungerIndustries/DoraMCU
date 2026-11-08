package com.yisy.modules.doramcu.traveller;

import java.util.Arrays;
import java.util.List;

/**
 * Careers available in Classic Traveller character creation.
 * <p>
 * Each career represents a professional path a character can follow during
 * their adult life. Careers determine skill acquisition, wealth accumulation,
 * and retirement benefits. Characters typically serve 2–4 careers before
 * retiring to adventure.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1: Characters & Combat (1981).
 * </p>
 */
public enum Career {

    /**
     * Soldier — military ground forces.
     * <p>
     * Soldiers learn combat skills, leadership, and military tactics.
     * They serve in planetary armies and Imperial combat units.
     * </p>
     */
    SOLDIER("Soldier", "Ground combat and military operations",
            Arrays.asList(Skill.MELEE, Skill.FIREARMS, Skill.SKIRMISH, Skill.GUERRILLA,
                    Skill.LEADERSHIP, Skill.TACTICS, Skill.SURVIVAL, Skill.NAVIGATION,
                    Skill.MEDICAL, Skill.PSYCHOLOGY)),

    /**
     * Explorer — planetary exploration and survey.
     * <p>
     * Explorers survey new worlds, map systems, and discover resources.
     * They work for the Survey organization and gain technical skills.
     * </p>
     */
    EXPLORER("Explorer", "Planetary survey and exploration",
            Arrays.asList(Skill.ASTRONOMY, Skill.GEOLOGY, Skill.BIOLOGY, Skill.CHEMISTRY,
                    Skill.PHYSICS, Skill.NAVIGATION, Skill.SURVIVAL, Skill.MEDICAL,
                    Skill.ELECTRONICS, Skill.COMPUTERS)),

    /**
     * Gunner — naval weapons specialist.
     * <p>
     * Gunners operate ship weapons systems in naval combat.
     * They serve aboard Imperial Navy vessels and learn missile and laser tactics.
     * </p>
     */
    GUNNER("Gunner", "Naval weapons operations",
            Arrays.asList(Skill.MISSILE, Skill.LASER, Skill.ELECTRONICS, Skill.COMPUTERS,
                    Skill.NAVIGATION, Skill.TACTICS, Skill.PSYCHOLOGY, Skill.MEDICAL)),

    /**
     * Pilot — starship and atmospheric pilot.
     * <p>
     * Pilots operate starships and atmospheric craft.
     * They learn navigation, ship systems, and combat piloting.
     * </p>
     */
    PILOT("Pilot", "Starship and atmospheric flight",
            Arrays.asList(Skill.STARSHIPS, Skill.GRAVITIC, Skill.NAVIGATION, Skill.ELECTRONICS,
                    Skill.COMPUTERS, Skill.ASTRONOMY, Skill.PSYCHOLOGY, Skill.MEDICAL)),

    /**
     * Merchant — interstellar trade and commerce.
     * <p>
     * Merchants trade goods between star systems for profit.
     * They learn navigation, languages, and business skills.
     * </p>
     */
    MERCHANT("Merchant", "Interstellar trade and commerce",
            Arrays.asList(Skill.NAVIGATION, Skill.STARSHIPS, Skill.GRAVITIC, Skill.LANGUAGE,
                    Skill.PSYCHOLOGY, Skill.COMPUTERS, Skill.ELECTRONICS, Skill.MEDICAL)),

    /**
     * Scout — deep space exploration and patrol.
     * <p>
     * Scouts explore the frontiers of known space and patrol borders.
     * They gain broad technical and survival skills.
     * </p>
     */
    SCOUT("Scout", "Deep space exploration and patrol",
            Arrays.asList(Skill.STARSHIPS, Skill.GRAVITIC, Skill.NAVIGATION, Skill.ASTRONOMY,
                    Skill.ELECTRONICS, Skill.COMPUTERS, Skill.SURVIVAL, Skill.MEDICAL,
                    Skill.PSYCHOLOGY, Skill.TACTICS)),

    /**
     * Spy — intelligence and covert operations.
     * <p>
     * Spies gather intelligence and conduct covert operations.
     * They learn stealth, deception, and infiltration skills.
     * </p>
     */
    SPY("Spy", "Intelligence and covert operations",
            Arrays.asList(Skill.STEALTH, Skill.PSYCHOLOGY, Skill.LANGUAGE, Skill.FORGERY,
                    Skill.COMPUTERS, Skill.ELECTRONICS, Skill.MELEE, Skill.FIREARMS,
                    Skill.SKIRMISH, Skill.GUERRILLA)),

    /**
     * Noble — aristocratic service and politics.
     * <p>
     * Nobles serve in government, diplomacy, and administration.
     * They gain social and political skills.
     * </p>
     */
    NOBLE("Noble", "Government, diplomacy, and administration",
            Arrays.asList(Skill.LANGUAGE, Skill.PSYCHOLOGY, Skill.LEADERSHIP, Skill.TACTICS,
                    Skill.MELEE, Skill.FIREARMS, Skill.SKIRMISH, Skill.GUERRILLA));

    private final String name;
    private final String description;
    private final List<Skill> skillTable;

    Career(String name, String description, List<Skill> skillTable) {
        this.name = name;
        this.description = description;
        this.skillTable = skillTable;
    }

    /**
     * Returns the name of the career.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the career.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the skill table for this career.
     * <p>
     * When a character gains a skill during a career tour, one skill
     * is randomly selected from this table.
     * </p>
     *
     * @return the list of possible skills
     */
    public List<Skill> getSkillTable() {
        return skillTable;
    }

    /**
     * Returns the display name of the career.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Returns true if this is a military career (ground or naval).
     *
     * @return true if military
     */
    public boolean isMilitaryCareer() {
        return this == SOLDIER || this == GUNNER || this == PILOT;
    }

    /**
     * Returns true if this is a navy career (ship-based).
     *
     * @return true if navy
     */
    public boolean isNavyCareer() {
        return this == GUNNER || this == PILOT;
    }

    /**
     * Returns true if this is an army career (ground-based).
     *
     * @return true if army
     */
    public boolean isArmyCareer() {
        return this == SOLDIER;
    }
}
