package com.yisy.modules.doramcu.hex;

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

    /**
     * Creates a new army for the given empire.
     *
     * @param empireId the ID of the owning empire
     */
    public EmpireArmy(int empireId) {
        this.empireId = empireId;
        this.units = new ArrayList<>();
        this.nextUnitId = 1;
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

    @Override
    public String toString() {
        return String.format("EmpireArmy{empireId=%d, units=%d, offense=%d, defense=%d}",
                empireId, getUnitCount(), getTotalOffense(), getTotalDefense());
    }
}
