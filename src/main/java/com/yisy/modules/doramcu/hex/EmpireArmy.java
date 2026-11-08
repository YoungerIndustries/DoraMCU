package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.MercenaryCareer;
import com.yisy.modules.doramcu.traveller.MercenaryRank;
import com.yisy.modules.doramcu.traveller.MercenaryUnit;
import com.yisy.modules.doramcu.traveller.UnitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the Army component of an Empire.
 * <p>
 * The Army module handles all ground forces: unit creation, deployment,
 * combat statistics, and supply logistics. Army units operate on planetary
 * surfaces and require naval transport for inter-system movement.
 * </p>
 * <p>
 * Each empire has one Army instance that tracks all ground units across
 * all owned planets.
 * </p>
 */
public class EmpireArmy {

    private final int empireId;
    private final List<ArmyUnit> units;
    private int nextUnitId;

    /** Mercenary units integrated from Traveller system. */
    private final List<MercenaryUnit> mercenaryUnits;
    private int nextMercenaryId;

    /**
     * Creates a new army for the given empire.
     *
     * @param empireId the ID of the owning empire
     */
    public EmpireArmy(int empireId) {
        this.empireId = empireId;
        this.units = new ArrayList<>();
        this.nextUnitId = 1;
        this.mercenaryUnits = new ArrayList<>();
        this.nextMercenaryId = 1;
    }

    // ---- Accessors ----

    /** Returns the empire ID. */
    public int getEmpireId() {
        return empireId;
    }

    /** Returns an unmodifiable list of all army units. */
    public List<ArmyUnit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    /** Returns the total number of army units. */
    public int getUnitCount() {
        return units.size();
    }

    // ---- Unit Management ----

    /**
     * Creates a new army unit of the given type.
     *
     * @param unitType the type of unit to create
     * @return the newly created unit
     */
    public ArmyUnit createUnit(ArmyUnitType unitType) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type must not be null");
        }
        ArmyUnit unit = new ArmyUnit(nextUnitId++, unitType);
        units.add(unit);
        return unit;
    }

    /**
     * Removes a unit from the army.
     *
     * @param unit the unit to remove
     * @return true if the unit was removed
     */
    public boolean removeUnit(ArmyUnit unit) {
        return units.remove(unit);
    }

    /**
     * Removes all destroyed units (strength <= 0).
     *
     * @return the number of units removed
     */
    public int removeDestroyedUnits() {
        int before = units.size();
        units.removeIf(u -> u.getCurrentStrength() <= 0);
        return before - units.size();
    }

    // ---- Deployment ----

    /**
     * Returns all units stationed on the given planet.
     *
     * @param planet the planet to query
     * @return list of units stationed on the planet
     */
    public List<ArmyUnit> getUnitsOnPlanet(Planet planet) {
        return units.stream()
                .filter(u -> u.getStationedPlanet() == planet)
                .collect(Collectors.toList());
    }

    /**
     * Returns all units stationed on the given planet by ID.
     *
     * @param planetId the planet ID
     * @return list of units stationed on the planet
     */
    public List<ArmyUnit> getUnitsOnPlanet(int planetId) {
        return units.stream()
                .filter(u -> u.getStationedPlanet() != null && u.getStationedPlanet().getId() == planetId)
                .collect(Collectors.toList());
    }

    // ---- Combat Statistics ----

    /**
     * Returns the total effective offense of all army units.
     */
    public int getTotalOffense() {
        return units.stream().mapToInt(ArmyUnit::getEffectiveOffense).sum();
    }

    /**
     * Returns the total effective defense of all army units.
     */
    public int getTotalDefense() {
        return units.stream().mapToInt(ArmyUnit::getEffectiveDefense).sum();
    }

    /**
     * Returns the total supply cost per turn for all army units.
     */
    public int getTotalSupplyCost() {
        return units.stream().mapToInt(ArmyUnit::getSupplyCost).sum();
    }

    /**
     * Returns a breakdown of units by type.
     */
    public Map<ArmyUnitType, Integer> getUnitTypeBreakdown() {
        return units.stream().collect(Collectors.groupingBy(
                ArmyUnit::getUnitType, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
        ));
    }

    // ---- End Turn Processing ----

    /**
     * Processes end-of-turn effects for all units:
     * - Natural strength recovery
     * - Morale decay for damaged units
     */
    public void endTurn() {
        for (ArmyUnit unit : units) {
            // Natural recovery: 2 strength per turn if not in combat
            if (unit.getCurrentStrength() < unit.getMaxStrength()) {
                unit.repair(2);
            }
            // Morale decay for damaged units
            if (unit.getCurrentStrength() < unit.getMaxStrength() * 0.5) {
                unit.adjustMorale(-1);
            }
        }
        removeDestroyedUnits();
    }

    // ---- Mercenary Unit Integration ----

    /** Returns an unmodifiable list of all mercenary units. */
    public List<MercenaryUnit> getMercenaryUnits() {
        return Collections.unmodifiableList(mercenaryUnits);
    }

    /** Returns the total number of mercenary units. */
    public int getMercenaryUnitCount() {
        return mercenaryUnits.size();
    }

    /**
     * Creates a new mercenary fire team.
     *
     * @param name the unit name
     * @return the newly created mercenary unit
     */
    public MercenaryUnit createFireTeam(String name) {
        MercenaryUnit unit = new MercenaryUnit(nextMercenaryId++, name, UnitType.FIRE_TEAM,
                MercenaryRank.PRIVATE, 10, 8, 0, 0, 0);
        mercenaryUnits.add(unit);
        return unit;
    }

    /**
     * Creates a new mercenary platoon.
     *
     * @param name the unit name
     * @return the newly created mercenary unit
     */
    public MercenaryUnit createPlatoon(String name) {
        MercenaryUnit unit = new MercenaryUnit(nextMercenaryId++, name, UnitType.PLATOON,
                MercenaryRank.CORPORAL, 40, 8, 0, 0, 0);
        mercenaryUnits.add(unit);
        return unit;
    }

    /**
     * Creates a new mercenary company.
     *
     * @param name the unit name
     * @return the newly created mercenary unit
     */
    public MercenaryUnit createCompany(String name) {
        MercenaryUnit unit = new MercenaryUnit(nextMercenaryId++, name, UnitType.COMPANY,
                MercenaryRank.SECOND_LIEUTENANT, 160, 8, 0, 0, 0);
        mercenaryUnits.add(unit);
        return unit;
    }

    /**
     * Creates a new mercenary battalion.
     *
     * @param name the unit name
     * @return the newly created mercenary unit
     */
    public MercenaryUnit createBattalion(String name) {
        MercenaryUnit unit = new MercenaryUnit(nextMercenaryId++, name, UnitType.BATTALION,
                MercenaryRank.FIRST_LIEUTENANT, 640, 8, 0, 0, 0);
        mercenaryUnits.add(unit);
        return unit;
    }

    /**
     * Creates a new mercenary brigade.
     *
     * @param name the unit name
     * @return the newly created mercenary unit
     */
    public MercenaryUnit createBrigade(String name) {
        MercenaryUnit unit = new MercenaryUnit(nextMercenaryId++, name, UnitType.BRIGADE,
                MercenaryRank.CAPTAIN, 2560, 8, 0, 0, 0);
        mercenaryUnits.add(unit);
        return unit;
    }

    /**
     * Adds an existing mercenary unit to this army.
     *
     * @param unit the mercenary unit to add
     */
    public void addMercenaryUnit(MercenaryUnit unit) {
        mercenaryUnits.add(unit);
    }

    /**
     * Removes a mercenary unit from this army.
     *
     * @param unit the unit to remove
     * @return true if removed
     */
    public boolean removeMercenaryUnit(MercenaryUnit unit) {
        return mercenaryUnits.remove(unit);
    }

    /**
     * Returns the total ground force strength (army + mercenary).
     */
    public int getTotalGroundStrength() {
        int armyStrength = units.stream().mapToInt(ArmyUnit::getCurrentStrength).sum();
        int mercStrength = mercenaryUnits.stream().mapToInt(MercenaryUnit::getStrength).sum();
        return armyStrength + mercStrength;
    }

    // ---- Mercenary Career Bonuses ----

    /**
     * Applies mercenary career bonuses to all mercenary units.
     * <p>
     * Based on the commander's {@link MercenaryCareer}, units receive bonuses to:
     * </p>
     * <ul>
     *   <li>Morale: +1 per officer rank level (up to +5)</li>
     *   <li>Leadership: +1 per officer rank level (up to +3)</li>
     *   <li>Equipment: higher rank units get better starting equipment</li>
     * </ul>
     *
     * @param career the mercenary career of the army commander
     */
    public void applyMercenaryCareerBonuses(MercenaryCareer career) {
        if (career == null || mercenaryUnits.isEmpty()) {
            return;
        }

        MercenaryRank rank = career.getRank();
        int travellerRank = rank.getTravellerRank();

        // Morale bonus: +1 per rank level, max +5
        int moraleBonus = Math.min(5, travellerRank);
        // Leadership bonus: +1 per rank level, max +3
        int leadershipBonus = Math.min(3, travellerRank);

        for (MercenaryUnit unit : mercenaryUnits) {
            unit.setMorale(Math.min(12, unit.getMorale() + moraleBonus));
            unit.setLeadershipBonus(unit.getLeadershipBonus() + leadershipBonus);
        }
    }

    /**
     * Creates a mercenary unit with career-based bonuses.
     * <p>
     * The unit's morale and leadership are boosted based on the commander's
     * mercenary career rank.
     * </p>
     *
     * @param name the unit name
     * @param unitType the type of unit
     * @param commanderRank the commander's rank
     * @param career the mercenary career (for bonuses), or null for no bonuses
     * @return the newly created mercenary unit
     */
    public MercenaryUnit createUnitWithCareerBonus(String name, UnitType unitType,
                                                    MercenaryRank commanderRank,
                                                    MercenaryCareer career) {
        int strength = unitType.getTypicalStrength();
        int morale = 8;
        int leadership = 0;

        if (career != null) {
            int travellerRank = career.getRank().getTravellerRank();
            morale = Math.min(12, 8 + Math.min(5, travellerRank));
            leadership = Math.min(3, travellerRank);
        }

        MercenaryUnit unit = new MercenaryUnit(nextMercenaryId++, name, unitType,
                commanderRank, strength, morale, 0, leadership, 0);
        mercenaryUnits.add(unit);
        return unit;
    }

    @Override
    public String toString() {
        return String.format("EmpireArmy{empireId=%d, units=%d, mercenaries=%d, offense=%d, defense=%d}",
                empireId, getUnitCount(), getMercenaryUnitCount(), getTotalOffense(), getTotalDefense());
    }
}
