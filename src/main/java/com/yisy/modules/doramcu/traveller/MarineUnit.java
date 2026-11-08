package com.yisy.modules.doramcu.traveller;

/**
 * A marine unit for boarding actions in Classic Traveller.
 * <p>
 * In Classic Traveller High Guard, ships carry marine complements for
 * boarding actions and ship-to-ship assault. Marines are organized into
 * squads with combat ratings that determine their effectiveness in
 * boarding actions and room-to-room combat.
 * </p>
 * <p>
 * Marine units have:
 * </p>
 * <ul>
 *   <li>Combat rating — determines attack and defense in boarding combat</li>
 *   <li>Size — number of marines in the unit</li>
 *   <li>Equipment level — affects combat effectiveness</li>
 *   <li>Morale — determines willingness to fight</li>
 * </ul>
 * <p>
 * Larger ships carry more marines. A typical destroyer carries 1-2 squads,
 * while a cruiser may carry 4-6 squads.
 * </p>
 */
public class MarineUnit {

    /**
     * Equipment level of a marine unit.
     */
    public enum EquipmentLevel {
        /**
         * Basic sidearms and light armor.
         */
        LIGHT(1, "Light"),

        /**
         * Standard marine equipment: pulse rifles, body armor.
         */
        STANDARD(2, "Standard"),

        /**
         * Heavy weapons, powered armor, breaching equipment.
         */
        HEAVY(3, "Heavy"),

        /**
         * Elite equipment: energy weapons, shield generators, powered armor.
         */
        ELITE(4, "Elite");

        private final int bonus;
        private final String name;

        EquipmentLevel(int bonus, String name) {
            this.bonus = bonus;
            this.name = name;
        }

        /** @return the combat bonus for this equipment level */
        public int getBonus() {
            return bonus;
        }

        /** @return the human-readable name */
        public String getName() {
            return name;
        }
    }

    /**
     * Morale level of a marine unit.
     */
    public enum Morale {
        /**
         * Poorly trained conscripts — may break under pressure.
         */
        POOR(0, "Poor"),

        /**
         * Regular troops — fight until overwhelmed.
         */
        REGULAR(2, "Regular"),

        /**
         * Well-trained marines — fight through heavy casualties.
         */
        GOOD(4, "Good"),

        /**
         * Elite shock troops — never break.
         */
        ELITE(6, "Elite");

        private final int bonus;
        private final String name;

        Morale(int bonus, String name) {
            this.bonus = bonus;
            this.name = name;
        }

        /** @return the morale bonus for combat checks */
        public int getBonus() {
            return bonus;
        }

        /** @return the human-readable name */
        public String getName() {
            return name;
        }
    }

    private final String name;
    private final int combatRating;
    private final int size;
    private int remainingMarines;
    private final EquipmentLevel equipment;
    private final Morale morale;
    private final int breachingCapability;

    /**
     * Creates a new marine unit.
     *
     * @param name the name of the unit
     * @param combatRating the base combat rating (1-12, higher is better)
     * @param size the number of marines in the unit
     * @param equipment the equipment level
     * @param morale the morale level
     * @param breachingCapability the breaching capability (1-6, determines door breaching speed)
     */
    public MarineUnit(String name, int combatRating, int size,
                       EquipmentLevel equipment, Morale morale, int breachingCapability) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Unit name must not be empty");
        }
        if (combatRating < 1 || combatRating > 12) {
            throw new IllegalArgumentException("Combat rating must be between 1 and 12");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Unit size must be at least 1");
        }
        if (breachingCapability < 1 || breachingCapability > 6) {
            throw new IllegalArgumentException("Breaching capability must be between 1 and 6");
        }

        this.name = name;
        this.combatRating = combatRating;
        this.size = size;
        this.remainingMarines = size;
        this.equipment = equipment;
        this.morale = morale;
        this.breachingCapability = breachingCapability;
    }

    /**
     * Creates a standard marine unit for a ship of the given tonnage.
     * <p>
     * The unit size and equipment are derived from the ship's tonnage:
     * </p>
     * <ul>
     *   <li>Under 1000 tons: 1 marine, light equipment</li>
     *   <li>1000-9999 tons: 2-4 marines, standard equipment</li>
     *   <li>10000-99999 tons: 5-10 marines, standard equipment</li>
     *   <li>100000+ tons: 11-20 marines, heavy equipment</li>
     * </ul>
     *
     * @param shipTonnage the ship's tonnage
     * @param random the random number generator
     * @return a new marine unit
     */
    public static MarineUnit fromShipTonnage(int shipTonnage, java.util.Random random) {
        String name = "Marine Squad";
        int combatRating;
        int size;
        EquipmentLevel equipment;
        Morale morale;
        int breaching;

        if (shipTonnage < 1000) {
            size = 1;
            equipment = EquipmentLevel.LIGHT;
            morale = Morale.REGULAR;
            combatRating = 4 + random.nextInt(3);
            breaching = 2 + random.nextInt(2);
        } else if (shipTonnage < 10000) {
            size = 2 + random.nextInt(3);
            equipment = EquipmentLevel.STANDARD;
            morale = Morale.REGULAR;
            combatRating = 5 + random.nextInt(3);
            breaching = 3 + random.nextInt(2);
        } else if (shipTonnage < 100000) {
            size = 5 + random.nextInt(6);
            equipment = EquipmentLevel.STANDARD;
            morale = Morale.GOOD;
            combatRating = 6 + random.nextInt(3);
            breaching = 3 + random.nextInt(3);
        } else {
            size = 11 + random.nextInt(10);
            equipment = EquipmentLevel.HEAVY;
            morale = Morale.GOOD;
            combatRating = 7 + random.nextInt(4);
            breaching = 4 + random.nextInt(3);
        }

        return new MarineUnit(name, combatRating, size, equipment, morale, breaching);
    }

    // ---- Accessors ----

    /** @return the name of the unit */
    public String getName() {
        return name;
    }

    /** @return the base combat rating */
    public int getCombatRating() {
        return combatRating;
    }

    /** @return the original size of the unit */
    public int getSize() {
        return size;
    }

    /** @return the number of marines still alive */
    public int getRemainingMarines() {
        return remainingMarines;
    }

    /** @return the equipment level */
    public EquipmentLevel getEquipment() {
        return equipment;
    }

    /** @return the morale level */
    public Morale getMorale() {
        return morale;
    }

    /** @return the breaching capability */
    public int getBreachingCapability() {
        return breachingCapability;
    }

    /**
     * Returns the effective combat power of this unit.
     * <p>
     * Combat power = combat rating × equipment bonus × (remaining marines / original size)
     * </p>
     *
     * @return the effective combat power
     */
    public double getEffectiveCombatPower() {
        double strengthRatio = (double) remainingMarines / size;
        return combatRating * (1 + equipment.getBonus() * 0.1) * strengthRatio;
    }

    /**
     * Checks if this unit is still combat-effective.
     *
     * @return true if the unit has marines remaining
     */
    public boolean isCombatEffective() {
        return remainingMarines > 0;
    }

    /**
     * Checks if this unit is at full strength.
     *
     * @return true if no marines have been lost
     */
    public boolean isFullStrength() {
        return remainingMarines == size;
    }

    /**
     * Returns the casualty percentage.
     *
     * @return the percentage of marines lost (0-100)
     */
    public double getCasualtyPercentage() {
        if (size <= 0) return 100.0;
        return (1.0 - (double) remainingMarines / size) * 100.0;
    }

    /**
     * Applies casualties to this unit.
     *
     * @param casualties the number of marines lost
     * @return the actual number of casualties applied
     */
    public int applyCasualties(int casualties) {
        if (casualties <= 0) return 0;
        int actual = Math.min(casualties, remainingMarines);
        remainingMarines -= actual;
        if (remainingMarines < 0) remainingMarines = 0;
        return actual;
    }

    /**
     * Performs a morale check.
     * <p>
     * If casualties exceed 50%, the unit may break and retreat.
     * The morale bonus determines the chance to hold position.
     * </p>
     *
     * @param random the random number generator
     * @return true if the unit holds position, false if it breaks
     */
    public boolean moraleCheck(java.util.Random random) {
        if (getCasualtyPercentage() < 50.0) return true;

        int roll = random.nextInt(12) + 2; // 2D6
        int threshold = 7 + morale.getBonus();
        return roll <= threshold;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d marines, CR%d, %s, %s)",
                name, remainingMarines, size, combatRating, equipment.getName(), morale.getName());
    }
}
