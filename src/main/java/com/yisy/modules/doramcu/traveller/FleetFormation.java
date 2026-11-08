package com.yisy.modules.doramcu.traveller;

/**
 * Fleet formations from Classic Traveller Book 5: High Guard.
 * <p>
 * Formations determine the relative positions of ships within a fleet and affect
 * combat effectiveness, defense, and maneuverability. Each formation has advantages
 * and disadvantages depending on the tactical situation.
 * </p>
 * <p>
 * Formations modify:
 * </p>
 * <ul>
 *   <li>Front arc bonus: ships in the front arc gain hit bonuses</li>
 *   <li>Sides/rear penalty: ships on the sides or rear suffer hit penalties</li>
 *   <li>Maneuverability: some formations restrict individual ship movement</li>
 *   <li>Defense: tighter formations offer mutual support but are easier targets</li>
 * </ul>
 */
public enum FleetFormation {

    /**
     * Line Ahead — ships in a single file, one behind the other.
     * <p>
     * The classic naval formation. All ships face the same direction.
     * Front ship has full fire power; ships behind have restricted arcs.
     * </p>
     * Front arc bonus: +2 for lead ship, +0 for others.
     * Maneuverability: restricted, ships must maintain spacing.
     */
    LINE_AHEAD("Line Ahead", 2, 0, "Single file, all ships facing same direction",
               2, 0, 0, -1),

    /**
     * Line Astern — ships in a single file, facing away from the threat.
     * <p>
     * Used when retreating or when the rear arc has advantages.
     * Ships fire backward, with decreasing effectiveness.
     * </p>
     * Front arc bonus: 0. Rear arc bonus: +1.
     * Maneuverability: moderate, easier to break formation.
     */
    LINE_ASTERN("Line Astern", 0, 1, "Single file, facing away from threat",
                0, 0, 1, 0),

    /**
     * Wedge — ships in a V formation, point ship leading.
     * <p>
     * The point ship leads with wings extending behind. Good for
     * concentrated fire forward and mutual support.
     * </p>
     * Front arc bonus: +1 for all ships.
     * Maneuverability: moderate, wings can spread or tighten.
     */
    WEDGE("Wedge", 1, 0, "V formation, point ship leading",
          1, 1, 0, 0),

    /**
     * Echelon Right — ships staggered to the right.
     * <p>
     * Used for flanking maneuvers. The right wing is forward,
     * the left wing trails. Good for attacking the enemy's flank.
     * </p>
     * Front arc bonus: +1 for right wing, +0 for left wing.
     * Maneuverability: good for turning toward the enemy.
     */
    ECHELON_RIGHT("Echelon Right", 1, 0, "Staggered right, for flanking",
                  1, 0, 0, 1),

    /**
     * Echelon Left — ships staggered to the left.
     * <p>
     * Mirror of Echelon Right. Used for flanking maneuvers
     * on the left side.
     * </p>
     * Front arc bonus: +1 for left wing, +0 for right wing.
     * Maneuverability: good for turning toward the enemy.
     */
    ECHELON_LEFT("Echelon Left", 1, 0, "Staggered left, for flanking",
                 1, 0, 0, -1),

    /**
     * Screen — ships spread out in a wide formation.
     * <p>
     * Used for patrol, reconnaissance, and anti-submarine warfare.
     * Ships have wide arcs of fire but less mutual support.
     * </p>
     * Front arc bonus: 0. All arcs: +0.
     * Maneuverability: excellent, ships can move independently.
     */
    SCREEN("Screen", 0, 0, "Wide spread, patrol and reconnaissance",
           0, 0, 0, 2),

    /**
     * Mass — ships clustered tightly together.
     * <p>
     * Maximum concentrated fire but poor maneuverability.
     * Used when overwhelming firepower is needed or when
     * defending a point (e.g., a station or planet).
     * </p>
     * Front arc bonus: +2 for all ships.
     * Maneuverability: poor, ships move as a single unit.
     */
    MASS("Mass", 2, 1, "Tight cluster, maximum firepower",
         2, 2, -1, -2),

    /**
     * Column — ships in columns, side by side.
     * <p>
     * Similar to Line Ahead but with ships abreast. Good for
     * broadside fire and maintaining formation while maneuvering.
     * </p>
     * Front arc bonus: +1 for all ships.
     * Maneuverability: moderate, columns can turn together.
     */
    COLUMN("Column", 1, 0, "Side by side, broadside fire",
           1, 1, 0, -1),

    /**
     * Diamond — ships in a diamond pattern.
     * <p>
     * Balanced formation with good fire arcs in all directions.
     * The flagship is at the center with ships at each point.
     * </p>
     * Front arc bonus: +1. All arcs: +0.
     * Maneuverability: good, can shift to any direction.
     */
    DIAMOND("Diamond", 1, 0, "Diamond pattern, balanced defense",
            1, 1, 1, 0),

    /**
     * Custom — free formation, ships positioned individually.
     * <p>
     * No formation bonuses or penalties. Each ship acts independently.
     * Used for complex tactical situations or when formation discipline
     * is not required.
     * </p>
     * No bonuses. Maneuverability: unlimited.
     */
    CUSTOM("Custom", 0, 0, "Free formation, independent ships",
           0, 0, 0, 2);

    private final String name;
    private final int frontArcBonus;
    private final int defenseBonus;
    private final String description;
    private final int rearArcBonus;
    private final int sidesArcBonus;
    private final int maneuverabilityModifier;
    private final int fireCoordination;

    FleetFormation(String name, int frontArcBonus, int defenseBonus,
                   String description, int rearArcBonus, int sidesArcBonus,
                   int maneuverabilityModifier, int fireCoordination) {
        this.name = name;
        this.frontArcBonus = frontArcBonus;
        this.defenseBonus = defenseBonus;
        this.description = description;
        this.rearArcBonus = rearArcBonus;
        this.sidesArcBonus = sidesArcBonus;
        this.maneuverabilityModifier = maneuverabilityModifier;
        this.fireCoordination = fireCoordination;
    }

    /**
     * Returns the hit bonus for ships on the side arcs.
     *
     * @return the sides arc bonus
     */
    public int getSidesArcBonus() {
        return sidesArcBonus;
    }

    /**
     * Returns the fire coordination bonus for this formation.
     * <p>
     * Positive values indicate better coordinated fire.
     * Negative values indicate worse coordination.
     * </p>
     *
     * @return the fire coordination bonus
     */
    public int getFireCoordination() {
        return fireCoordination;
    }

    /**
     * Returns the formation name.
     *
     * @return the name string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the hit bonus for ships in the front arc.
     *
     * @return the front arc bonus
     */
    public int getFrontArcBonus() {
        return frontArcBonus;
    }

    /**
     * Returns the defense bonus for ships in this formation.
     * <p>
     * Positive values indicate better defense (harder to hit).
     * Negative values indicate worse defense (easier to hit).
     * </p>
     *
     * @return the defense bonus
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /**
     * Returns the description of this formation.
     *
     * @return the description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the hit bonus for ships in the rear arc.
     *
     * @return the rear arc bonus
     */
    public int getRearArcBonus() {
        return rearArcBonus;
    }

    /**
     * Returns the maneuverability modifier.
     * <p>
     * Positive values indicate better maneuverability.
     * Negative values indicate worse maneuverability.
     * </p>
     *
     * @return the maneuverability modifier
     */
    public int getManeuverabilityModifier() {
        return maneuverabilityModifier;
    }

    /**
     * Checks if this formation restricts individual ship movement.
     *
     * @return true if movement is restricted
     */
    public boolean restrictsMovement() {
        return maneuverabilityModifier < 0;
    }

    /**
     * Checks if this formation provides mutual defense support.
     *
     * @return true if mutual defense is active
     */
    public boolean providesMutualDefense() {
        return defenseBonus > 0;
    }

    /**
     * Finds the formation with the given name (case-insensitive).
     *
     * @param name the formation name
     * @return the matching formation, or null if not found
     */
    public static FleetFormation fromName(String name) {
        if (name == null) {
            return null;
        }
        for (FleetFormation formation : values()) {
            if (formation.name.equalsIgnoreCase(name)) {
                return formation;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
