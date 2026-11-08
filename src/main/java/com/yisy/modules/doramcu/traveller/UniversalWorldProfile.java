package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * Universal World Profile (UWP) from Classic Traveller.
 * <p>
 * The UWP is an 8-character string that completely describes a star system's
 * primary habitable world. The format is:
 * </p>
 * <pre>
 *   P C A G M T - Mod
 *   │ │ │ │ │ │   │
 *   │ │ │ │ │ │   └ Body guard modifier (optional)
 *   │ │ │ │ │ └──── Government type (0-A)
 *   │ │ │ │ └────── Law level (0-A)
 *   │ │ │ └──────── Population (0-A)
 *   │ │ └────────── Starport class (A-E)
 *   │ └──────────── Atmosphere (0-F)
 *   └────────────── Planet size (0-A)
 * </pre>
 * <p>
 * Example: <code>A544430-D</code> describes a large world (Size A), good atmosphere (5),
 * breathable (4), average population (4), good government (4), high law (3), no government
 * type specified (0), with a Delta body guard modifier.
 * </p>
 * <p>
 * This class parses UWP strings, validates the format, and provides accessors
 * for each field along with derived properties like habitability and port quality.
 * </p>
 */
public class UniversalWorldProfile {

    /**
     * Planet size code. 0 = small sub-terran, A = giant gas planet.
     * Numeric values: 0-9 (0–9), A = 10.
     */
    private final int planetSize;

    /**
     * Atmosphere code. 0 = none, A = breathable, etc.
     * Numeric values: 0-9 (0–9), A = 10.
     */
    private final int atmosphere;

    /**
     * Gravity code. 0 = very low, A = very high.
     * Numeric values: 0-9 (0–9), A = 10.
     * Note: This is the actual gravity, not the UWP gravity (which may be modified by atmosphere).
     */
    private final int gravity;

    /**
     * Starport class. 0 = excellent, A = poor, etc.
     * Numeric values: 0-9 (0–9), A = 10.
     * Lower is better: 0 = excellent port, A = no port.
     */
    private final int starport;

    /**
     * Population code. 0 = none, A = extremely dense.
     * Numeric values: 0-9 (0–9), A = 10.
     */
    private final int population;

    /**
     * Government type. 0 = none, A = various government types.
     * Numeric values: 0-9 (0–9), A = 10.
     */
    private final int government;

    /**
     * Law level. 0 = anarchy, A = totalitarian.
     * Numeric values: 0-9 (0–9), A = 10.
     */
    private final int lawLevel;

    /**
     * Body guard modifier (optional).
     * A-Z indicating additional bodies in the system.
     * Null if no modifier.
     */
    private final String modifier;

    /**
     * The original UWP string as parsed.
     */
    private final String uwpString;

    /**
     * Parses a UWP string into a UniversalWorldProfile.
     *
     * @param uwp the UWP string (e.g., "A544430-D", "34565432")
     * @return a new UniversalWorldProfile
     * @throws IllegalArgumentException if the UWP string is invalid
     */
    public static UniversalWorldProfile parse(String uwp) {
        Objects.requireNonNull(uwp, "UWP string must not be null");
        uwp = uwp.trim();

        if (uwp.length() < 8) {
            throw new IllegalArgumentException("UWP string must be at least 8 characters: " + uwp);
        }

        String base = uwp.substring(0, 8);
        String mod = null;
        if (uwp.length() > 8 && uwp.charAt(8) == '-') {
            mod = uwp.substring(9);
        }

        int[] values = new int[7];
        for (int i = 0; i < 7; i++) {
            char c = base.charAt(i);
            values[i] = hexToValue(c);
        }

        return new UniversalWorldProfile(
                values[0], // Planet Size
                values[1], // Atmosphere
                values[2], // Gravity
                values[3], // Starport
                values[4], // Population
                values[5], // Government
                values[6], // Law Level
                mod,
                uwp
        );
    }

    /**
     * Converts a hex character to its numeric value.
     * 0-9 → 0-9, A → 10.
     */
    private static int hexToValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c == 'A' || c == 'a') {
            return 10;
        } else {
            throw new IllegalArgumentException("Invalid UWP character: " + c);
        }
    }

    /**
     * Creates a new UniversalWorldProfile with the given field values.
     *
     * @param planetSize planet size code (0-10)
     * @param atmosphere atmosphere code (0-10)
     * @param gravity gravity code (0-10)
     * @param starport starport class (0-10)
     * @param population population code (0-10)
     * @param government government type (0-10)
     * @param lawLevel law level (0-10)
     * @param modifier body guard modifier (optional)
     * @param uwpString the original UWP string
     */
    public UniversalWorldProfile(int planetSize, int atmosphere, int gravity,
                                  int starport, int population, int government,
                                  int lawLevel, String modifier, String uwpString) {
        validateRange(planetSize, "Planet size");
        validateRange(atmosphere, "Atmosphere");
        validateRange(gravity, "Gravity");
        validateRange(starport, "Starport");
        validateRange(population, "Population");
        validateRange(government, "Government");
        validateRange(lawLevel, "Law level");

        this.planetSize = planetSize;
        this.atmosphere = atmosphere;
        this.gravity = gravity;
        this.starport = starport;
        this.population = population;
        this.government = government;
        this.lawLevel = lawLevel;
        this.modifier = modifier;
        this.uwpString = uwpString;
    }

    private void validateRange(int value, String name) {
        if (value < 0 || value > 10) {
            throw new IllegalArgumentException(name + " must be 0-10, got " + value);
        }
    }

    // ---- Accessors ----

    /**
     * @return the planet size code (0-10, where A=10)
     */
    public int getPlanetSize() {
        return planetSize;
    }

    /**
     * @return the atmosphere code (0-10, where A=10)
     */
    public int getAtmosphere() {
        return atmosphere;
    }

    /**
     * @return the gravity code (0-10, where A=10)
     */
    public int getGravity() {
        return gravity;
    }

    /**
     * @return the starport class (0-10, where A=10; lower is better)
     */
    public int getStarport() {
        return starport;
    }

    /**
     * @return the population code (0-10, where A=10)
     */
    public int getPopulation() {
        return population;
    }

    /**
     * @return the government type (0-10, where A=10)
     */
    public int getGovernment() {
        return government;
    }

    /**
     * @return the law level (0-10, where A=10)
     */
    public int getLawLevel() {
        return lawLevel;
    }

    /**
     * @return the body guard modifier, or null if none
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * @return the original UWP string
     */
    public String getUwpString() {
        return uwpString;
    }

    // ---- Derived Properties ----

    /**
     * Checks if the world is habitable by humans without environmental suits.
     * <p>
     * A world is habitable if:
     * </p>
     * <ul>
     *   <li>Atmosphere is breathable (codes 1-7, or A=10 for thin but breathable)</li>
     *   <li>Gravity is within human tolerance (1-8)</li>
     * </ul>
     *
     * @return true if the world is habitable
     */
    public boolean isHabitable() {
        // Atmosphere: 1-7 is breathable, 8-9 is toxic, 0 is none, A(10) is thin but breathable
        boolean breathableAtmosphere = (atmosphere >= 1 && atmosphere <= 7) || atmosphere == 10;
        // Gravity: 1-8 is within human tolerance
        boolean tolerableGravity = gravity >= 1 && gravity <= 8;
        return breathableAtmosphere && tolerableGravity;
    }

    /**
     * Checks if the world has a breathable atmosphere.
     *
     * @return true if the atmosphere is breathable
     */
    public boolean hasBreathableAtmosphere() {
        return (atmosphere >= 1 && atmosphere <= 7) || atmosphere == 10;
    }

    /**
     * Checks if the world has no atmosphere (Vacuum).
     *
     * @return true if atmosphere code is 0
     */
    public boolean isVacuum() {
        return atmosphere == 0;
    }

    /**
     * Returns the quality of the starport.
     * <p>
     * Lower starport codes indicate better ports:
     * </p>
     * <ul>
     *   <li>0 = Excellent</li>
     *   <li>1 = Good</li>
     *   <li>2 = Average</li>
     *   <li>3 = Poor</li>
     *   <li>4 = Terrible</li>
     *   <li>5+ or A(10) = No port</li>
     * </ul>
     *
     * @return the starport quality rating (0-10, lower is better)
     */
    public int getStarportQuality() {
        return starport;
    }

    /**
     * Checks if the world has a functional starport.
     *
     * @return true if starport code is ≤ 4
     */
    public boolean hasStarport() {
        return starport <= 4;
    }

    /**
     * Estimates the world's population in billions based on the population code.
     * <p>
     * Population code mapping:
     * </p>
     * <ul>
     *   <li>0 = 0 billion (no population)</li>
     *   <li>1 = 0.01 billion (10 million)</li>
     *   <li>2 = 0.1 billion (100 million)</li>
     *   <li>3 = 0.5 billion</li>
     *   <li>4 = 1 billion</li>
     *   <li>5 = 5 billion</li>
     *   <li>6 = 10 billion</li>
     *   <li>7 = 25 billion</li>
     *   <li>8 = 50 billion</li>
     *   <li>9 = 100 billion</li>
     *   <li>A(10) = 200+ billion</li>
     * </ul>
     *
     * @return estimated population in billions
     */
    public double getPopulationBillions() {
        switch (population) {
            case 0: return 0;
            case 1: return 0.01;
            case 2: return 0.1;
            case 3: return 0.5;
            case 4: return 1;
            case 5: return 5;
            case 6: return 10;
            case 7: return 25;
            case 8: return 50;
            case 9: return 100;
            case 10: return 200;
            default: return 0;
        }
    }

    /**
     * Returns the government type as a human-readable string.
     *
     * @return the government type description
     */
    public String getGovernmentType() {
        switch (government) {
            case 0: return "None";
            case 1: return "Communist";
            case 2: return "Capitalist";
            case 3: return "Feudal";
            case 4: return "Democratic";
            case 5: return "Theocracy";
            case 6: return "Confederacy";
            case 7: return "Autocracy";
            case 8: return "Ochlocracy";
            case 9: return "Hive Mind";
            case 10: return "Other";
            default: return "Unknown";
        }
    }

    /**
     * Returns the law level as a human-readable string.
     *
     * @return the law level description
     */
    public String getLawLevelDescription() {
        switch (lawLevel) {
            case 0: return "None (Anarchy)";
            case 1: return "Very Low";
            case 2: return "Low";
            case 3: return "Moderate";
            case 4: return "Average";
            case 5: return "High";
            case 6: return "Very High";
            case 7: return "Enforced";
            case 8: return "Very Strict";
            case 9: return "Strict";
            case 10: return "Totalitarian";
            default: return "Unknown";
        }
    }

    /**
     * Returns the planet size as a human-readable string.
     *
     * @return the planet size description
     */
    public String getPlanetSizeDescription() {
        switch (planetSize) {
            case 0: return "Small Sub-Terran";
            case 1: return "Sub-Terran";
            case 2: return "High Sub-Terran";
            case 3: return "Terran";
            case 4: return "High Terran";
            case 5: return "Super-Terran";
            case 6: return "High Super-Terran";
            case 7: return "Gas Giant";
            case 8: return "High Gas Giant";
            case 9: return "Very High Gas Giant";
            case 10: return "Gigantic Gas Giant";
            default: return "Unknown";
        }
    }

    /**
     * Returns the atmosphere as a human-readable string.
     *
     * @return the atmosphere description
     */
    public String getAtmosphereDescription() {
        switch (atmosphere) {
            case 0: return "None";
            case 1: return "Trace";
            case 2: return "Thin";
            case 3: return "Very Thin";
            case 4: return "Standard";
            case 5: return "Dense";
            case 6: return "Very Dense";
            case 7: return "Super-Dense";
            case 8: return "None (Too Hot)";
            case 9: return "None (Toxic)";
            case 10: return "None (Unbreathable)";
            default: return "Unknown";
        }
    }

    // ---- Trade Code Derivation ----

    /**
     * Derives the primary trade code for this world based on its UWP.
     * <p>
     * This implements the Classic Traveller trade code derivation algorithm
     * from the Spinward Marches supplement.
     * </p>
     *
     * @return the primary trade code, or NI (None) if no trade code applies
     */
    public TradeCode derivePrimaryTradeCode() {
        // Vacuum worlds
        if (isVacuum()) {
            return TradeCode.VA;
        }

        // No population
        if (population == 0) {
            return TradeCode.NI;
        }

        // Ecumenopolis: Population 9, Government 4-9
        if (population == 9 && government >= 4 && government <= 9) {
            return TradeCode.EC;
        }

        // High Population: Population ≥ 7
        if (population >= 7) {
            return TradeCode.HI;
        }

        // Colonial: Population ≤ 3
        if (population <= 3) {
            return TradeCode.CO;
        }

        // Agricultural: Population ≥ 4, gravity 3-6
        if (population >= 4 && gravity >= 3 && gravity <= 6) {
            return TradeCode.AG;
        }

        // Poor: Wealth (derived from population and government) is low
        if (government == 0 || lawLevel == 0) {
            return TradeCode.PO;
        }

        // Default: Low Population
        return TradeCode.LO;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return uwpString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversalWorldProfile that = (UniversalWorldProfile) o;
        return planetSize == that.planetSize &&
                atmosphere == that.atmosphere &&
                gravity == that.gravity &&
                starport == that.starport &&
                population == that.population &&
                government == that.government &&
                lawLevel == that.lawLevel &&
                Objects.equals(modifier, that.modifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planetSize, atmosphere, gravity, starport,
                population, government, lawLevel, modifier);
    }
}
