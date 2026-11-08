package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.Player;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * A Classic Traveller character with ability scores, career service, and skills.
 * <p>
 * Characters are created by rolling 2D6 for each of the seven abilities,
 * then serving one or more careers. Each career tour (4 years) may grant
 * skills, wealth, and promotions. After retiring, characters adventure
 * with their accumulated skills and wealth.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 1: Characters & Combat (1981).
 * </p>
 */
public class TravellerCharacter {

    private final int id;
    private String name;
    private final Map<AbilityScore, Integer> abilities;
    private final List<CareerTour> careerTours;
    private final Set<Skill> skills;
    private int age;
    private long wealth;
    private boolean alive;
    private CharacterStatus status;

    /** Optional psionic ability profile — null if character has no psionic potential. */
    private PsionicAbility psionicAbility;

    /** The character's alien race — defaults to HUMAN. */
    private AlienRace race;

    /**
     * Creates a new character with the given ID and name.
     *
     * @param id the unique character identifier
     * @param name the character's name
     */
    public TravellerCharacter(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.abilities = new EnumMap<>(AbilityScore.class);
        this.careerTours = new ArrayList<>();
        this.skills = new HashSet<>();
        this.age = 18; // Characters start at age 18
        this.wealth = 0;
        this.alive = true;
        this.status = CharacterStatus.TRAINING;
        this.race = AlienRace.HUMAN;
    }

    /**
     * Rolls all seven ability scores using 2D6.
     * <p>
     * Each ability is rolled independently, giving a bell curve
     * distribution from 2–12.
     * </p>
     */
    public void rollAbilities() {
        for (AbilityScore ability : AbilityScore.values()) {
            abilities.put(ability, DiceUtil.roll2D6());
        }
    }

    /**
     * Sets a specific ability score.
     *
     * @param ability the ability
     * @param value the score (2–12)
     */
    public void setAbility(AbilityScore ability, int value) {
        if (value < 2 || value > 12) {
            throw new IllegalArgumentException("Ability score must be 2–12, got: " + value);
        }
        abilities.put(ability, value);
    }

    /**
     * Returns the value of a specific ability.
     *
     * @param ability the ability
     * @return the score, or 0 if not yet rolled
     */
    public int getAbility(AbilityScore ability) {
        return abilities.getOrDefault(ability, 0);
    }

    /**
     * Returns all ability scores.
     *
     * @return the abilities map
     */
    public Map<AbilityScore, Integer> getAbilities() {
        return Collections.unmodifiableMap(abilities);
    }

    /**
     * Adds a career tour to the character's record.
     *
     * @param tour the career tour
     */
    public void addCareerTour(CareerTour tour) {
        careerTours.add(Objects.requireNonNull(tour, "Tour must not be null"));
        age += tour.getYearsServed();
        skills.addAll(tour.getSkillsLearned());
        wealth += tour.getWealthEarned();
    }

    /**
     * Returns the list of career tours (immutable).
     *
     * @return the career tours
     */
    public List<CareerTour> getCareerTours() {
        return Collections.unmodifiableList(careerTours);
    }

    /**
     * Returns the set of skills (immutable).
     *
     * @return the skills
     */
    public Set<Skill> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    /**
     * Checks if the character has a specific skill.
     *
     * @param skill the skill to check
     * @return true if the character has the skill
     */
    public boolean hasSkill(Skill skill) {
        return skills.contains(skill);
    }

    /**
     * Returns the character's ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the character's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the character's name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    /**
     * Returns the character's age.
     *
     * @return the age in years
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns the character's total wealth in credits.
     *
     * @return the wealth
     */
    public long getWealth() {
        return wealth;
    }

    /**
     * Checks if the character is alive.
     *
     * @return true if alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets the character's alive status.
     *
     * @param alive the alive status
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Returns the character's current lifecycle status.
     *
     * @return the status (TRAINING, ON_DUTY, or COMPLETED)
     */
    public CharacterStatus getStatus() {
        return status;
    }

    /**
     * Sets the character's lifecycle status.
     *
     * @param status the new status
     */
    public void setStatus(CharacterStatus status) {
        this.status = Objects.requireNonNull(status, "Status must not be null");
    }

    /**
     * Returns the commander bonus for a given bonus type.
     * <p>
     * Based on this character's career tours and skills, calculates modifiers
     * for various empire operations:
     * </p>
     * <ul>
     *   <li>ARMY_COMBAT — Soldier career: +1 DM per tour served</li>
     *   <li>SHIP_EVASION — Pilot career: +2% evasion per tour served</li>
     *   <li>SHIP_DAMAGE — Gunner career: +2% damage per tour served</li>
     *   <li>SURVEY_SPEED — Explorer career: +10% survey speed per tour</li>
     *   <li>TRADE_PROFIT — Merchant career: +5% profit per tour served</li>
     *   <li>GOVERNMENT — Noble career: +1 diplomacy DM per tour served</li>
     * </ul>
     *
     * @param type the bonus type
     * @return the bonus value (0 if no applicable career tours)
     */
    public double getCommanderBonus(Player.CommanderBonusType type) {
        if (type == null) {
            return 0;
        }
        final Career matchingCareer;
        switch (type) {
            case ARMY_COMBAT:
                matchingCareer = Career.SOLDIER;
                break;
            case SHIP_EVASION:
                matchingCareer = Career.PILOT;
                break;
            case SHIP_DAMAGE:
                matchingCareer = Career.GUNNER;
                break;
            case SURVEY_SPEED:
                matchingCareer = Career.EXPLORER;
                break;
            case TRADE_PROFIT:
                matchingCareer = Career.MERCHANT;
                break;
            case GOVERNMENT:
                matchingCareer = Career.NOBLE;
                break;
            default:
                return 0;
        }
        // Count the number of tours served in the matching career
        long tourCount = careerTours.stream()
                .filter(tour -> tour.getCareer() == matchingCareer)
                .count();
        if (tourCount == 0) {
            return 0;
        }
        // Apply bonus per tour
        switch (type) {
            case ARMY_COMBAT:
                return tourCount * 1.0; // +1 DM per tour
            case SHIP_EVASION:
                return tourCount * 2.0; // +2% per tour
            case SHIP_DAMAGE:
                return tourCount * 2.0; // +2% per tour
            case SURVEY_SPEED:
                return tourCount * 10.0; // +10% per tour
            case TRADE_PROFIT:
                return tourCount * 5.0; // +5% per tour
            case GOVERNMENT:
                return tourCount * 1.0; // +1 DM per tour
            default:
                return 0;
        }
    }

    /**
     * Returns the primary career of this character (the career with the most tours).
     *
     * @return the primary career, or null if no career tours
     */
    public Career getPrimaryCareer() {
        return careerTours.stream()
                .map(CareerTour::getCareer)
                .collect(java.util.stream.Collectors.groupingBy(c -> c, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }

    // ============================================================
    // Psionic Ability (B03 Worlds & Adventures)
    // ============================================================

    /**
     * Returns the character's psionic ability profile.
     *
     * @return the psionic ability, or null if not detected
     */
    public PsionicAbility getPsionicAbility() {
        return psionicAbility;
    }

    /**
     * Sets the character's psionic ability profile.
     *
     * @param psionicAbility the psionic ability, or null if none
     */
    public void setPsionicAbility(PsionicAbility psionicAbility) {
        this.psionicAbility = psionicAbility;
    }

    /**
     * Performs psionic detection for this character.
     * <p>
     * Uses the In (Intuition) score to determine if the character has
     * psionic potential. Characters with In 8+ may have psionic ability.
     * </p>
     *
     * @return true if psionic ability was detected
     */
    public boolean detectPsionic() {
        // In Classic Traveller, psionics uses In (Intuition), which maps to St (Saving Throw)
        int inScore = getAbility(AbilityScore.ST);
        if (inScore < 8) {
            this.psionicAbility = null;
            return false;
        }
        int roll = DiceUtil.roll2D6();
        PsionicAbility ability = new PsionicAbility(inScore);
        boolean detected = ability.detectPsionic(inScore, roll);
        if (detected) {
            this.psionicAbility = ability;
        } else {
            this.psionicAbility = null;
        }
        return detected;
    }

    /**
     * Checks if this character has detected psionic ability.
     *
     * @return true if psionic ability exists and is not NONE
     */
    public boolean isPsionic() {
        return psionicAbility != null
                && psionicAbility.getDetectionLevel() != PsionicAbility.DetectionLevel.NONE;
    }

    // ============================================================
    // Alien Race (S03 Spinward Marches)
    // ============================================================

    /**
     * Returns the character's alien race.
     *
     * @return the race (defaults to HUMAN)
     */
    public AlienRace getRace() {
        return race;
    }

    /**
     * Sets the character's alien race.
     *
     * @param race the race
     */
    public void setRace(AlienRace race) {
        this.race = Objects.requireNonNull(race, "Race must not be null");
    }

    /**
     * Returns the combat die modifier from the character's race.
     *
     * @return the combat DM
     */
    public int getRaceCombatDM() {
        return race.getCombatDM();
    }

    /**
     * Returns the social die modifier from the character's race.
     *
     * @return the social DM
     */
    public int getRaceSocialDM() {
        return race.getSocialDM();
    }

    /**
     * Returns the navigation die modifier from the character's race.
     *
     * @return the navigation DM
     */
    public int getRaceNavigationDM() {
        return race.getNavigationDM();
    }

    /**
     * Returns the armor bonus from the character's race.
     *
     * @return the armor bonus
     */
    public int getRaceArmorBonus() {
        return race.getArmorBonus();
    }

    /**
     * Returns the trade profit bonus percentage from the character's race.
     *
     * @return the trade bonus percentage
     */
    public int getRaceTradeBonus() {
        return race.getTradeBonusPercent();
    }

    /**
     * Returns a summary string of the character.
     *
     * @return the summary
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(race.getDisplayName()).append(", Age ").append(age).append(")\n");
        sb.append("Abilities: ");
        for (AbilityScore ability : AbilityScore.values()) {
            sb.append(ability.getAbbreviation()).append("=")
                    .append(getAbility(ability)).append(" ");
        }
        sb.append("\nSkills: ").append(skills.size()).append("\n");
        sb.append("Wealth: ").append(wealth).append(" credits");
        if (psionicAbility != null) {
            sb.append("\n").append(psionicAbility);
        }
        return sb.toString();
    }

    /**
     * A single career tour (4 years of service).
     */
    public static class CareerTour {
        private final Career career;
        private final int yearsServed;
        private final List<Skill> skillsLearned;
        private final String rank;
        private final long wealthEarned;
        private final boolean retired;
        private final RetirementType retirementType;

        /**
         * Creates a new career tour.
         *
         * @param career the career served
         * @param yearsServed years of service (typically 4 per tour)
         * @param skillsLearned skills gained during the tour
         * @param rank the rank achieved
         * @param wealthEarned wealth accumulated
         * @param retired whether the character retired from this career
         * @param retirementType the type of retirement
         */
        public CareerTour(Career career, int yearsServed, List<Skill> skillsLearned,
                          String rank, long wealthEarned, boolean retired,
                          RetirementType retirementType) {
            this.career = Objects.requireNonNull(career, "Career must not be null");
            this.yearsServed = yearsServed;
            this.skillsLearned = new ArrayList<>(Objects.requireNonNull(skillsLearned, "Skills must not be null"));
            this.rank = rank;
            this.wealthEarned = wealthEarned;
            this.retired = retired;
            this.retirementType = retirementType;
        }

        public Career getCareer() {
            return career;
        }

        public int getYearsServed() {
            return yearsServed;
        }

        public List<Skill> getSkillsLearned() {
            return Collections.unmodifiableList(skillsLearned);
        }

        public String getRank() {
            return rank;
        }

        public long getWealthEarned() {
            return wealthEarned;
        }

        public boolean isRetired() {
            return retired;
        }

        public RetirementType getRetirementType() {
            return retirementType;
        }
    }

    /**
     * Types of retirement from a career.
     */
    public enum RetirementType {
        /**
         * Normal retirement after completing service.
         */
        NORMAL("Normal"),

        /**
         * Honorable retirement with benefits.
         */
        HONORABLE("Honorable"),

        /**
         * Dishonorable discharge — no benefits.
         */
        DISHONORABLE("Dishonorable"),

        /**
         * Medical retirement due to injury or illness.
         */
        MEDICAL("Medical"),

        /**
         * Death in service.
         */
        DEAD("Dead");

        private final String name;

        RetirementType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
