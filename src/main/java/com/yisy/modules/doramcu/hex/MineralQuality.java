package com.yisy.modules.doramcu.hex;

/**
 * Represents the mineral quality of a planet, which affects production output bonuses.
 * <p>
 * Mineral quality directly influences how much production output a planet generates
 * from manufacturing buildings and shipyards. Higher quality minerals provide
 * significant bonuses to ship building and infrastructure construction.
 * </p>
 */
public enum MineralQuality {
    /**
     * Ultra-poor mineral deposits — 40% production penalty.
     */
    ULTRA_POOR(-0.4),

    /**
     * Poor mineral deposits — 20% production penalty.
     */
    POOR(-0.2),

    /**
     * Normal mineral deposits — no bonus or penalty.
     */
    NORMAL(0.0),

    /**
     * Rich mineral deposits — 20% production bonus.
     */
    RICH(0.2),

    /**
     * Ultra-rich mineral deposits — 40% production bonus.
     */
    ULTRA_RICH(0.4);

    private final double productionBonus;

    MineralQuality(double productionBonus) {
        this.productionBonus = productionBonus;
    }

    /**
     * Returns the production bonus multiplier for this mineral quality.
     *
     * @return the production bonus as a decimal (e.g., 0.2 = 20% bonus, -0.2 = 20% penalty)
     */
    public double getProductionBonus() {
        return productionBonus;
    }
}
