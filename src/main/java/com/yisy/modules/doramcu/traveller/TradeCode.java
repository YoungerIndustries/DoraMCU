package com.yisy.modules.doramcu.traveller;

/**
 * Trade codes from Classic Traveller Supplement 3: The Spinward Marches.
 * <p>
 * Trade codes describe the economic and political characteristics of a world.
 * They are derived from the world's UWP (Universal World Profile) and determine
 * the world's role in interstellar commerce.
 * </p>
 * <p>
 * A world can have multiple trade codes. The primary trade code determines the
 * world's classification, while secondary codes modify its characteristics.
 * </p>
 */
public enum TradeCode {

    /**
     * Agricultural — world with high food production.
     * Requires: Population ≥ 4, no Vacuum atmosphere.
     */
    AG("Ag", "Agricultural"),

    /**
     * Cold — world with low temperature.
     * Requires: Low population or specific gravity conditions.
     */
    CE("Ce", "Cold"),

    /**
     * Colonial — world with low population.
     * Requires: Population ≤ 3.
     */
    CO("Co", "Colonial"),

    /**
     * Ecumenopolis — world-city with extremely high population.
     * Requires: Population 9, specific government type.
     */
    EC("Ec", "Ecumenopolis"),

    /**
     * Flooded — world with no dry land.
     * Requires: Specific planet type and atmosphere.
     */
    FL("Fl", "Flooded"),

    /**
     * High Population — densely populated world.
     * Requires: Population ≥ 7.
     */
    HI("Hi", "High Population"),

    /**
     * Ice Cap — world with polar ice caps.
     * Requires: Cold world with specific conditions.
     */
    IC("IC", "Ice Cap"),

    /**
     * Low Population — sparsely populated world.
     * Requires: Population ≤ 3, not Colonial.
     */
    LO("Lo", "Low Population"),

    /**
     * Non-Aligned — world not part of major powers.
     * Requires: Specific government and law level.
     */
    NC("NC", "Non-Aligned"),

    /**
     * None — world with no significant trade.
     * Requires: Population 0 or no atmosphere.
     */
    NI("Ni", "None"),

    /**
     * Poor — world with low wealth.
     * Requires: Low wealth level.
     */
    PO("Po", "Poor"),

    /**
     * Sea World — water world with no continents.
     * Requires: Specific planet type.
     */
    SE("Se", "Sea World"),

    /**
     * Vacuum — world with no breathable atmosphere.
     * Requires: Atmosphere code 0.
     */
    VA("Va", "Vacuum"),

    /**
     * Water World — world with high water coverage.
     * Requires: Specific planet type and atmosphere.
     */
    WE("We", "Water World"),

    /**
     * Anarchy — world with no effective government.
     * Requires: Government type 0 or law level 0.
     */
    AN("An", "Anarchy"),

    /**
     * Complex — world with mixed characteristics.
     * Requires: Multiple conflicting trade indicators.
     */
    CX("Cx", "Complex"),

    /**
     * Lawless — world with low law level.
     * Requires: Law level ≤ 2.
     */
    LW("Lw", "Lawless"),

    /**
     * Mountainous — world with high terrain variation.
     * Requires: Specific gravity and planet type.
     */
    MO("Mo", "Mountainous"),

    /**
     * Refugee — world with refugee population.
     * Requires: Specific political conditions.
     */
    RF("Rf", "Refugee"),

    /**
     * Rough — world with harsh conditions.
     * Requires: Low standard of living.
     */
    RO("Ro", "Rough"),

    /**
     * Urbanized — world with high urbanization.
     * Requires: High population and specific government.
     */
    UR("Ur", "Urbanized");

    private final String code;
    private final String description;

    TradeCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the two-letter trade code abbreviation.
     *
     * @return the trade code string (e.g., "Ag", "Ce", "Co")
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the human-readable description of this trade code.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds the trade code with the given two-letter abbreviation.
     *
     * @param code the two-letter code (case-insensitive)
     * @return the matching trade code, or null if not found
     */
    public static TradeCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TradeCode tc : values()) {
            if (tc.code.equalsIgnoreCase(code)) {
                return tc;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
