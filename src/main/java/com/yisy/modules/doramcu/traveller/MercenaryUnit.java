package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Mercenary unit organization from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * Represents a military unit at any organizational level (Fire Team through Brigade).
 * Each unit has morale, experience, leadership, and medical capabilities.
 * </p>
 * <p>
 * Units are formed from individuals and teams. Morale is determined by
 * experience points plus a die roll, and represents the number or less
 * that must be rolled on 2D6 to avoid panic.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public class MercenaryUnit {

    private final int id;
    private final String name;
    private final UnitType unitType;
    private final MercenaryRank commanderRank;
    private int strength;
    private int casualties;
    private int morale;
    private int experiencePoints;
    private int leadershipBonus;
    private int medicalSkillPoints;
    private final List<MercenaryUnit> subUnits;
    private final Map<MercenarySkill, Integer> unitSkills;
    private boolean hasSeniorNCO;
    private MercenaryRank seniorNCO;

    /**
     * Creates a new mercenary unit.
     *
     * @param id the unique unit identifier
     * @param name the unit name
     * @param unitType the organizational level
     * @param commanderRank the commander's rank
     * @param strength the number of combat effectives
     * @param morale the morale rating (number or less on 2D6 to pass)
     * @param experiencePoints the total experience points
     * @param leadershipBonus the leadership morale bonus
     * @param medicalSkillPoints the available medical skill points
     */
    public MercenaryUnit(int id, String name, UnitType unitType, MercenaryRank commanderRank,
                         int strength, int morale, int experiencePoints,
                         int leadershipBonus, int medicalSkillPoints) {
        this.id = id;
        this.name = name != null ? name : "Unit " + id;
        this.unitType = Objects.requireNonNull(unitType, "Unit type must not be null");
        this.commanderRank = Objects.requireNonNull(commanderRank, "Commander rank must not be null");
        this.strength = strength;
        this.casualties = 0;
        this.morale = morale;
        this.experiencePoints = experiencePoints;
        this.leadershipBonus = leadershipBonus;
        this.medicalSkillPoints = medicalSkillPoints;
        this.subUnits = new ArrayList<>();
        this.unitSkills = new HashMap<>();
        this.hasSeniorNCO = false;
    }

    /**
     * Adds a sub-unit to this unit.
     *
     * @param subUnit the sub-unit to add
     */
    public void addSubUnit(MercenaryUnit subUnit) {
        Objects.requireNonNull(subUnit, "Sub-unit must not be null");
        subUnits.add(subUnit);
    }

    /**
     * Returns the list of sub-units (immutable).
     *
     * @return the sub-units
     */
    public List<MercenaryUnit> getSubUnits() {
        return Collections.unmodifiableList(subUnits);
    }

    /**
     * Adds casualties to the unit.
     *
     * @param number the number of casualties
     * @return true if the unit has been destroyed or surrendered
     */
    public boolean addCasualties(int number) {
        casualties += number;
        strength = Math.max(0, strength - number);
        return isDestroyed();
    }

    /**
     * Returns true if the unit is destroyed or has surrendered.
     * <p>
     * A unit surrenders when casualties reach the preservation level (40% of strength).
     * </p>
     *
     * @return true if destroyed or surrendered
     */
    public boolean isDestroyed() {
        return strength <= 0 || casualties >= unitType.getPreservationLevel(this.strength + casualties);
    }

    /**
     * Performs a morale check.
     *
     * @param random the random number generator
     * @return true if the morale check passes
     */
    public boolean moraleCheck(Random random) {
        int roll = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        return roll <= morale + leadershipBonus;
    }

    /**
     * Gains experience from combat.
     *
     * @param points the experience points gained
     */
    public void gainExperience(int points) {
        experiencePoints += points;
    }

    /**
     * Sets a unit skill.
     *
     * @param skill the skill
     * @param level the skill level
     */
    public void setUnitSkill(MercenarySkill skill, int level) {
        unitSkills.put(skill, level);
    }

    /**
     * Returns the unit's skill levels (immutable).
     *
     * @return the skill levels
     */
    public Map<MercenarySkill, Integer> getUnitSkills() {
        return Collections.unmodifiableMap(unitSkills);
    }

    /**
     * Sets the senior NCO for this unit.
     *
     * @param rank the NCO's rank
     */
    public void setSeniorNCO(MercenaryRank rank) {
        this.hasSeniorNCO = true;
        this.seniorNCO = rank;
    }

    // ---- Getters ----

    public int getId() { return id; }
    public String getName() { return name; }
    public UnitType getUnitType() { return unitType; }
    public MercenaryRank getCommanderRank() { return commanderRank; }
    public int getStrength() { return strength; }
    public int getCasualties() { return casualties; }
    public int getMorale() { return morale; }
    public int getExperiencePoints() { return experiencePoints; }
    public int getLeadershipBonus() { return leadershipBonus; }
    public int getMedicalSkillPoints() { return medicalSkillPoints; }
    public boolean hasSeniorNCO() { return hasSeniorNCO; }
    public MercenaryRank getSeniorNCO() { return seniorNCO; }

    /**
     * Returns the preservation level (casualties before surrender).
     *
     * @return the preservation level
     */
    public int getPreservationLevel() {
        return unitType.getPreservationLevel(strength + casualties);
    }

    /**
     * Sets the morale for this unit.
     *
     * @param morale the new morale value
     */
    public void setMorale(int morale) {
        this.morale = morale;
    }

    /**
     * Sets the leadership bonus for this unit.
     *
     * @param bonus the new leadership bonus
     */
    public void setLeadershipBonus(int bonus) {
        this.leadershipBonus = bonus;
    }

    @Override
    public String toString() {
        return String.format("Unit[%d: %s, %s, %s, %d/%d, Morale %d-]",
                id, name, unitType.getName(), commanderRank.getName(),
                strength, strength + casualties, morale);
    }
}
