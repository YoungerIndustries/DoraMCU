package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Generates Traveller worlds using the official B03/B06 world generation tables.
 * <p>
 * Follows the step-by-step UWP generation process from Classic Traveller:
 * </p>
 * <ol>
 *   <li>Star type and magnitude</li>
 *   <li>Number of habitable zones</li>
 *   <li>Planet occurrence (body size, atmosphere, temperature)</li>
 *   <li>Starport class</li>
 *   <li>Population level</li>
 *   <li>Government type</li>
 *   <li>Law level</li>
 *   <li>Trade modifiers</li>
 * </ol>
 * <p>
 * Each step uses 2D6 rolls against the Traveller probability tables.
 * </p>
 */
public class WorldGenerationService {

    /**
     * Generates a complete Traveller world using the official rules.
     *
     * @return a fully generated TravellerWorld
     */
    public TravellerWorld generateWorld() {
        // Step 1: Star type
        int starType = DiceUtil.roll2D6();
        String starClass = getStarClass(starType);

        // Step 2: Planet body size (2D6)
        int bodySizeRoll = DiceUtil.roll2D6();
        int bodySize = getBodySize(bodySizeRoll);

        // If no planet (body size 0), generate a gas giant instead
        if (bodySize == 0) {
            bodySize = 8; // Gas giant
        }

        // Step 3: Atmosphere (depends on body size)
        int atmosphereRoll = DiceUtil.roll2D6();
        int atmosphere = getAtmosphere(bodySize, atmosphereRoll);

        // Step 4: Temperature (depends on body size)
        int tempRoll = DiceUtil.roll2D6();
        int temperature = getTemperature(bodySize, tempRoll);

        // Step 5: Gravity = bodySize - atmosphere
        int gravity = Math.max(0, bodySize - atmosphere);

        // Step 6: Starport
        int starportRoll = DiceUtil.roll2D6();
        char starport = getStarport(starportRoll);

        // Step 7: Population (depends on atmosphere, temperature, gravity)
        int popRoll = DiceUtil.roll2D6();
        int population = getPopulation(atmosphere, temperature, gravity, popRoll);

        // Step 8: Government (depends on population)
        int govRoll = DiceUtil.roll2D6();
        char government = getGovernment(population, govRoll);

        // Step 9: Law level (depends on government)
        int lawRoll = DiceUtil.roll2D6();
        int law = getLawLevel(government, lawRoll);

        // Step 10: Trade modifiers
        String modifiers = getTradeModifiers(bodySize, atmosphere, temperature, gravity,
                starport, population, government, law);

        // Build UWP string
        String uwpString = bodySize + "" + atmosphere + gravity + starport +
                population + government + law + (modifiers.isEmpty() ? " " : modifiers);

        // Create UniversalWorldProfile
        UniversalWorldProfile uwp = new UniversalWorldProfile(
                bodySize, atmosphere, gravity, starport - 'A',
                population, government - '0', law, modifiers, uwpString);

        // Create TravellerWorld (portWealth and population are derived from UWP)
        TravellerWorld world = new TravellerWorld(0, "Generated-" + uwpString, uwp, 0, 0);

        // Derive trade codes
        List<TradeCode> tradeCodes = deriveTradeCodes(bodySize, atmosphere, temperature,
                gravity, starport, population, government, law);
        for (TradeCode tc : tradeCodes) {
            world.addTradeCode(tc);
        }

        return world;
    }

    // ============================================================
    // B03/B06 Generation Tables
    // ============================================================

    /**
     * Star class from 2D6 roll (B06 Table: Stellar Data).
     */
    String getStarClass(int roll) {
        switch (roll) {
            case 2: return "M";  // Red dwarf
            case 3: return "K";  // Orange dwarf
            case 4:
            case 5: return "G";  // Yellow dwarf
            case 6:
            case 7: return "F";  // White dwarf
            case 8:
            case 9: return "A";  // Blue-white
            case 10:
            case 11: return "B"; // Blue
            case 12: return "O"; // Blue giant
            default: return "G";
        }
    }

    /**
     * Body size from 2D6 roll (B03 Table: World Occurrence).
     */
    int getBodySize(int roll) {
        switch (roll) {
            case 2: return 0;  // No planet
            case 3: return 0;
            case 4: return 3;  // Small world
            case 5: return 4;  // Earth-size small
            case 6: return 5;  // Earth-size
            case 7: return 5;
            case 8: return 6;  // Large world
            case 9: return 6;
            case 10: return 7; // Very large
            case 11: return 7;
            case 12: return 8; // Gas giant
            default: return 5;
        }
    }

    /**
     * Atmosphere from 2D6 roll, based on body size (B03 Table: Atmosphere).
     */
    int getAtmosphere(int bodySize, int roll) {
        if (bodySize >= 8) return 0; // Gas giant has no solid surface atmosphere

        switch (bodySize) {
            case 3: // Small world
                if (roll <= 7) return 0;
                if (roll <= 9) return 1;
                return 2;
            case 4: // Earth-size small
                if (roll <= 5) return 0;
                if (roll <= 7) return 1;
                if (roll <= 9) return 2;
                if (roll <= 11) return 3;
                return 4;
            case 5: // Earth-size (most common)
                if (roll <= 4) return 0;
                if (roll <= 6) return 1;
                if (roll <= 8) return 2;
                if (roll <= 10) return 3;
                if (roll <= 11) return 4;
                return 5;
            case 6: // Large world
                if (roll <= 3) return 0;
                if (roll <= 5) return 1;
                if (roll <= 7) return 2;
                if (roll <= 9) return 3;
                if (roll <= 11) return 4;
                return 5;
            case 7: // Very large
                if (roll <= 2) return 0;
                if (roll <= 4) return 1;
                if (roll <= 6) return 2;
                if (roll <= 8) return 3;
                if (roll <= 10) return 4;
                return 5;
            default:
                return 0;
        }
    }

    /**
     * Temperature from 2D6 roll, based on body size (B03 Table: Temperature).
     */
    int getTemperature(int bodySize, int roll) {
        // Temperature is modified by distance from star, which correlates with body size
        int modifier = bodySize - 5; // Earth-size = 0 modifier
        int adjustedRoll = Math.max(2, Math.min(12, roll + modifier));

        switch (adjustedRoll) {
            case 2: return 0;  // Frozen
            case 3: return 1;  // Very cold
            case 4: return 2;  // Cold
            case 5: return 3;  // Cool
            case 6: return 4;  // Temperate low
            case 7: return 5;  // Temperate
            case 8: return 6;  // Temperate high
            case 9: return 7;  // Warm
            case 10: return 8; // Hot
            case 11: return 9; // Very hot
            case 12: return 10;// Inferno
            default: return 5;
        }
    }

    /**
     * Starport class from 2D6 roll (B03 Table: Starport).
     */
    char getStarport(int roll) {
        if (roll <= 3) return 'A';  // Excellent
        if (roll <= 5) return 'B';  // Good
        if (roll <= 8) return 'C';  // Average
        if (roll <= 10) return 'D'; // Poor
        if (roll <= 11) return 'E'; // Primitive
        return 'X';                 // None
    }

    /**
     * Population from 2D6 roll, based on habitability (B03 Table: Population).
     */
    int getPopulation(int atmosphere, int temperature, int gravity, int roll) {
        // Check if world is habitable
        boolean habitable = atmosphere >= 1 && atmosphere <= 5 &&
                temperature >= 3 && temperature <= 8 &&
                gravity >= 0 && gravity <= 8;

        if (!habitable) {
            // Uninhabitable worlds
            if (roll <= 4) return 0;
            if (roll <= 7) return 1;
            if (roll <= 9) return 2;
            return 3;
        }

        // Habitable worlds
        if (roll <= 3) return 0;
        if (roll <= 5) return 2;
        if (roll <= 7) return 4;
        if (roll <= 9) return 6;
        if (roll <= 11) return 8;
        return 9;
    }

    /**
     * Government type from 2D6 roll, based on population (B03 Table: Government).
     */
    char getGovernment(int population, int roll) {
        if (population <= 1) {
            // No government on empty worlds
            return '0';
        }

        // Government type depends on population level
        int popModifier = population / 2;
        int adjustedRoll = Math.max(2, Math.min(12, roll + popModifier));

        if (adjustedRoll <= 3) return '0';  // None
        if (adjustedRoll <= 4) return '1';  // Dictatorship
        if (adjustedRoll <= 5) return '2';  // Feudal
        if (adjustedRoll <= 6) return '3';  // Democratic
        if (adjustedRoll <= 7) return '4';  // Theocracy
        if (adjustedRoll <= 8) return '5';  // Communism
        if (adjustedRoll <= 9) return '6';  // Corporate
        if (adjustedRoll <= 10) return '7'; // Confederacy
        if (adjustedRoll <= 11) return '8'; // Imperial
        return '9';                          // Alliance
    }

    /**
     * Law level from 2D6 roll, based on government (B03 Table: Law Level).
     */
    int getLawLevel(char government, int roll) {
        if (government == '0') return 0; // No government = no law

        int govModifier = government - '0';
        int adjustedRoll = Math.max(2, Math.min(12, roll + govModifier / 2));

        if (adjustedRoll <= 3) return 0;  // None (anarchy)
        if (adjustedRoll <= 4) return 1;  // Very loose
        if (adjustedRoll <= 5) return 2;  // Loose
        if (adjustedRoll <= 6) return 3;  // Low
        if (adjustedRoll <= 7) return 4;  // Moderate
        if (adjustedRoll <= 8) return 5;  // Average
        if (adjustedRoll <= 9) return 6;  // High
        if (adjustedRoll <= 10) return 7; // Very high
        if (adjustedRoll <= 11) return 8; // Strict
        return 9;                          // Totalitarian
    }

    /**
     * Derives trade modifier codes from UWP values (B07 rules).
     */
    String getTradeModifiers(int bodySize, int atmosphere, int temperature,
                             int gravity, char starport, int population,
                             char government, int law) {
        StringBuilder modifiers = new StringBuilder();

        // Ag (Agricultural): pop >= 4, temp 4-6, atmo 2-5, grav 0-8
        if (population >= 4 && temperature >= 4 && temperature <= 6 &&
                atmosphere >= 2 && atmosphere <= 5 && gravity >= 0 && gravity <= 8) {
            modifiers.append('A');
        }

        // Co (Complex): pop >= 6, atmo 2-5, temp 4-6, grav 0-8
        if (population >= 6 && atmosphere >= 2 && atmosphere <= 5 &&
                temperature >= 4 && temperature <= 6 && gravity >= 0 && gravity <= 8) {
            modifiers.append('C');
        }

        // Fl (Fluid): atmo 6-9 (thick atmosphere)
        if (atmosphere >= 6 && atmosphere <= 9) {
            modifiers.append('F');
        }

        // Hi (High Population): pop >= 8
        if (population >= 8) {
            modifiers.append('H');
        }

        // Lo (Low Population): pop 2-3
        if (population >= 2 && population <= 3) {
            modifiers.append('L');
        }

        // Na (None): pop 0
        if (population == 0) {
            modifiers.append('N');
        }

        // Po (Poor): pop >= 2, starport D-E-X
        if (population >= 2 && (starport == 'D' || starport == 'E' || starport == 'X')) {
            modifiers.append('P');
        }

        // Ri (Rich): pop >= 6, starport A-C
        if (population >= 6 && (starport == 'A' || starport == 'B' || starport == 'C')) {
            modifiers.append('R');
        }

        // Va (Vacuum): atmo 0
        if (atmosphere == 0) {
            modifiers.append('V');
        }

        // Wa (Water): atmo 6-9, temp 4-6
        if (atmosphere >= 6 && atmosphere <= 9 && temperature >= 4 && temperature <= 6) {
            modifiers.append('W');
        }

        return modifiers.toString();
    }

    /**
     * Derives trade codes from UWP values.
     */
    List<TradeCode> deriveTradeCodes(int bodySize, int atmosphere, int temperature,
                                      int gravity, char starport, int population,
                                      char government, int law) {
        List<TradeCode> codes = new ArrayList<>();

        if (population == 0) {
            codes.add(TradeCode.NI);
        }

        if (atmosphere == 0) {
            codes.add(TradeCode.VA);
        }

        if (population >= 2 && (starport == 'D' || starport == 'E' || starport == 'X')) {
            codes.add(TradeCode.PO);
        }

        if (population >= 6 && (starport == 'A' || starport == 'B' || starport == 'C')) {
            codes.add(TradeCode.UR);
        }

        if (law >= 7) {
            codes.add(TradeCode.HI);
        }

        if (government == '0' && population > 0) {
            codes.add(TradeCode.AN);
        }

        return codes;
    }

    /**
     * Converts population level to millions.
     */
    long populationToMillions(int popLevel) {
        switch (popLevel) {
            case 0: return 0;
            case 1: return 100;
            case 2: return 1_000;
            case 3: return 10_000;
            case 4: return 100_000;
            case 5: return 1_000_000;
            case 6: return 10_000_000;
            case 7: return 100_000_000;
            case 8: return 1_000_000_000;
            case 9: return 10_000_000_000L;
            case 10: return 100_000_000_000L;
            default: return 0;
        }
    }

    /**
     * Calculates port wealth from starport class and population.
     */
    int calculatePortWealth(char starport, int population) {
        int starportValue = 0;
        switch (starport) {
            case 'A': starportValue = 5; break;
            case 'B': starportValue = 4; break;
            case 'C': starportValue = 3; break;
            case 'D': starportValue = 2; break;
            case 'E': starportValue = 1; break;
            default: starportValue = 0; break;
        }
        return Math.min(9, starportValue + population / 2);
    }

}
