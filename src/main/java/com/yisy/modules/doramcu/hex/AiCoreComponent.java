package com.yisy.modules.doramcu.hex;

/**
 * Represents an AI Core component installed on a ship.
 * <p>
 * AI Cores provide combat bonuses including damage, shield, speed, and evasion
 * improvements based on the AI Core's level and type.
 * </p>
 */
public class AiCoreComponent {

    /**
     * Levels of AI Core components.
     */
    public enum Level {
        BASIC(0.05),
        ADVANCED(0.10),
        ELITE(0.15);

        private final double bonusMultiplier;

        Level(double bonusMultiplier) {
            this.bonusMultiplier = bonusMultiplier;
        }

        public double getBonusMultiplier() {
            return bonusMultiplier;
        }
    }

    /**
     * Tier alias for Level — used by tests and fleet coordination logic.
     */
    public enum Tier {
        BASIC,
        ADVANCED,
        ELITE
    }

    private final Level level;
    private final String name;

    /**
     * Creates a new AI Core component.
     *
     * @param name the name of this AI Core
     * @param level the level of this AI Core
     */
    public AiCoreComponent(String name, Level level) {
        this.name = name;
        this.level = level;
    }

    /**
     * Creates an AI Core component from a Tier value.
     *
     * @param tier the tier of this AI Core
     */
    public AiCoreComponent(Tier tier) {
        this.name = "AI Core " + tier.name();
        this.level = Level.valueOf(tier.name());
    }

    /**
     * Creates a basic AI Core component.
     */
    public AiCoreComponent() {
        this("AI Core", Level.BASIC);
    }

    /**
     * Factory method to create an AI Core component from a ModuleType.
     *
     * @param moduleType the module type (must be an AI_CORE_* variant)
     * @return a new AiCoreComponent with the corresponding level
     */
    public static AiCoreComponent fromModuleType(ModuleType moduleType) {
        switch (moduleType) {
            case AI_CORE_BASIC:
                return new AiCoreComponent("AI Core Mk.I", Level.BASIC);
            case AI_CORE_ADVANCED:
                return new AiCoreComponent("AI Core Mk.II", Level.ADVANCED);
            case AI_CORE_ELITE:
                return new AiCoreComponent("AI Core Mk.III", Level.ELITE);
            default:
                throw new IllegalArgumentException("Not an AI Core module type: " + moduleType);
        }
    }

    // --- Accessors ---

    public String getName() {
        return name;
    }

    public Level getLevel() {
        return level;
    }

    /**
     * Returns the tier of this AI Core.
     *
     * @return the tier enum value
     */
    public Tier getTier() {
        return Tier.valueOf(level.name());
    }

    /**
     * Returns the damage bonus provided by this AI Core.
     *
     * @return the damage bonus multiplier
     */
    public double getDamageBonus() {
        return level.getBonusMultiplier();
    }

    /**
     * Returns the shield bonus provided by this AI Core.
     *
     * @return the shield bonus multiplier
     */
    public double getShieldBonus() {
        return level.getBonusMultiplier();
    }

    /**
     * Returns the speed bonus provided by this AI Core.
     *
     * @return the speed bonus multiplier
     */
    public double getSpeedBonus() {
        return level.getBonusMultiplier();
    }

    /**
     * Returns the evasion bonus provided by this AI Core.
     *
     * @return the evasion bonus multiplier
     */
    public double getEvasionBonus() {
        return level.getBonusMultiplier();
    }

    @Override
    public String toString() {
        return "AiCoreComponent{" +
                "name='" + name + '\'' +
                ", level=" + level +
                '}';
    }
}
