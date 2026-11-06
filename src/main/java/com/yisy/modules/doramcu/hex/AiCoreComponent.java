package com.yisy.modules.doramcu.hex;

/**
 * Represents an AI Core component that can be installed on a ship.
 * <p>
 * AI Cores provide tactical bonuses to ships based on their tier level.
 * Higher tier AI Cores provide better combat decision-making, target priority,
 * formation management, and evasion capabilities.
 * </p>
 * <p>
 * Tiers:
 * <ul>
 *   <li><b>Mk.I (Basic)</b>: Level 1 — Basic tactical decisions, target priority</li>
 *   <li><b>Mk.II (Advanced)</b>: Level 2 — Formation management, evasion tactics</li>
 *   <li><b>Mk.III (Elite)</b>: Level 3 — Predictive combat simulation, adaptive tactics</li>
 * </ul>
 * </p>
 */
public class AiCoreComponent {

    /**
     * Tier levels for AI Core components.
     */
    public enum Tier {
        /** Basic AI Core — target priority and basic tactical decisions */
        BASIC(1, 0.05, 0.05, 0.03, 0.02),
        /** Advanced AI Core — formation management and evasion tactics */
        ADVANCED(2, 0.10, 0.10, 0.06, 0.05),
        /** Elite AI Core — predictive simulation and adaptive tactics */
        ELITE(3, 0.15, 0.15, 0.10, 0.08);

        private final int level;
        private final double damageBonus;
        private final double evasionBonus;
        private final double speedBonus;
        private final double shieldBonus;

        Tier(int level, double damageBonus, double evasionBonus, double speedBonus, double shieldBonus) {
            this.level = level;
            this.damageBonus = damageBonus;
            this.evasionBonus = evasionBonus;
            this.speedBonus = speedBonus;
            this.shieldBonus = shieldBonus;
        }

        public int getLevel() {
            return level;
        }

        public double getDamageBonus() {
            return damageBonus;
        }

        public double getEvasionBonus() {
            return evasionBonus;
        }

        public double getSpeedBonus() {
            return speedBonus;
        }

        public double getShieldBonus() {
            return shieldBonus;
        }
    }

    private final Tier tier;
    private final int level;
    private final double damageBonus;
    private final double evasionBonus;
    private final double speedBonus;
    private final double shieldBonus;

    /**
     * Creates an AI Core component with the specified tier.
     *
     * @param tier the tier level of this AI Core
     */
    public AiCoreComponent(Tier tier) {
        if (tier == null) {
            throw new IllegalArgumentException("AI Core tier must not be null");
        }
        this.tier = tier;
        this.level = tier.getLevel();
        this.damageBonus = tier.getDamageBonus();
        this.evasionBonus = tier.getEvasionBonus();
        this.speedBonus = tier.getSpeedBonus();
        this.shieldBonus = tier.getShieldBonus();
    }

    /**
     * Creates an AI Core component from a ModuleType.
     *
     * @param moduleType the module type to create the AI Core from
     * @return the AI Core component
     * @throws IllegalArgumentException if the module type is not an AI Core
     */
    public static AiCoreComponent fromModuleType(ModuleType moduleType) {
        if (moduleType == null) {
            throw new IllegalArgumentException("Module type must not be null");
        }
        switch (moduleType) {
            case ModuleType.AI_CORE_BASIC:
                return new AiCoreComponent(Tier.BASIC);
            case ModuleType.AI_CORE_ADVANCED:
                return new AiCoreComponent(Tier.ADVANCED);
            case ModuleType.AI_CORE_ELITE:
                return new AiCoreComponent(Tier.ELITE);
            default:
                throw new IllegalArgumentException("Not an AI Core module type: " + moduleType);
        }
    }

    // ---- Accessors ----

    /**
     * Returns the tier of this AI Core.
     *
     * @return the tier
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Returns the level of this AI Core (1-3).
     *
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the damage bonus multiplier provided by this AI Core.
     * <p>
     * The AI Core improves target priority and firing solutions, increasing
     * effective damage output.
     * </p>
     *
     * @return the damage bonus (e.g., 0.05 = 5% increase)
     */
    public double getDamageBonus() {
        return damageBonus;
    }

    /**
     * Returns the evasion bonus provided by this AI Core.
     * <p>
     * The AI Core improves evasive maneuvers and predictive dodging.
     * </p>
     *
     * @return the evasion bonus (e.g., 0.05 = 5% increase)
     */
    public double getEvasionBonus() {
        return evasionBonus;
    }

    /**
     * Returns the speed bonus provided by this AI Core.
     * <p>
     * The AI Core optimizes engine performance and movement patterns.
     * </p>
     *
     * @return the speed bonus (e.g., 0.03 = 3% increase)
     */
    public double getSpeedBonus() {
        return speedBonus;
    }

    /**
     * Returns the shield bonus provided by this AI Core.
     * <p>
     * The AI Core optimizes shield modulation and deflection patterns.
     * </p>
     *
     * @return the shield bonus (e.g., 0.02 = 2% increase)
     */
    public double getShieldBonus() {
        return shieldBonus;
    }

    /**
     * Returns the ModuleType corresponding to this AI Core's tier.
     *
     * @return the module type
     */
    public ModuleType getModuleType() {
        switch (tier) {
            case BASIC:
                return ModuleType.AI_CORE_BASIC;
            case ADVANCED:
                return ModuleType.AI_CORE_ADVANCED;
            case ELITE:
                return ModuleType.AI_CORE_ELITE;
            default:
                throw new IllegalStateException("Unknown tier: " + tier);
        }
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return String.format("AiCoreComponent{tier=%s, level=%d, damage+%.0f%%, evasion+%.0f%%, speed+%.0f%%, shield+%.0f%%}",
                tier, level,
                damageBonus * 100, evasionBonus * 100,
                speedBonus * 100, shieldBonus * 100);
    }
}
