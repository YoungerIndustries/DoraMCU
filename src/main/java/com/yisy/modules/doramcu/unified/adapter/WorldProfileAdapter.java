package com.yisy.modules.doramcu.unified.adapter;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.PlanetSize;
import com.yisy.modules.doramcu.traveller.TradeCode;
import com.yisy.modules.doramcu.traveller.TravellerWorld;
import com.yisy.modules.doramcu.traveller.UniversalWorldProfile;

import java.util.Set;

/**
 * Adapts Traveller Universal World Profile (UWP) data to DoraMCU Planet properties.
 * <p>
 * This adapter converts a {@link UniversalWorldProfile} and associated trade codes
 * into the UWP fields on a {@link Planet}, providing economic modifiers based on
 * trade codes.
 * </p>
 * <p>
 * The adapter handles:
 * </p>
 * <ul>
 *   <li>Mapping UWP fields (atmosphere, gravity, starport, population, government, law) to Planet</li>
 *   <li>Deriving planet size from UWP planet size code</li>
 *   <li>Applying trade codes for economic modifiers</li>
 *   <li>Calculating resource production bonuses from trade codes</li>
 *   <li>Determining habitability from UWP atmosphere and gravity</li>
 * </ul>
 * <p>
 * Based on Classic Traveller Supplement 3: The Spinward Marches world generation rules.
 * </p>
 */
public class WorldProfileAdapter {

    /**
     * Applies a Universal World Profile to a Planet.
     * <p>
     * Sets all UWP fields on the planet and derives the planet size from the UWP.
     * </p>
     *
     * @param planet the planet to update
     * @param uwp the Universal World Profile
     */
    public static void applyUWP(Planet planet, UniversalWorldProfile uwp) {
        if (planet == null || uwp == null) return;

        planet.setUwpAtmosphere(uwp.getAtmosphere());
        planet.setUwpGravity(uwp.getGravity());
        planet.setUwpStarport(uwp.getStarport());
        planet.setUwpPopulationLevel(uwp.getPopulation());
        planet.setUwpGovernmentType(uwp.getGovernment());
        planet.setUwpLawLevel(uwp.getLawLevel());

        // Derive planet size from UWP planet size
        PlanetSize derivedSize = derivePlanetSize(uwp.getPlanetSize());
        if (derivedSize != null) {
            planet.setPlanetSize(derivedSize);
        }
    }

    /**
     * Applies trade codes to a Planet.
     * <p>
     * Clears existing trade codes and replaces them with the provided set.
     * </p>
     *
     * @param planet the planet to update
     * @param tradeCodes the trade codes to apply
     */
    public static void applyTradeCodes(Planet planet, Set<TradeCode> tradeCodes) {
        if (planet == null || tradeCodes == null) return;

        // Remove existing codes
        for (TradeCode code : planet.getTradeCodes()) {
            planet.removeTradeCode(code);
        }

        // Add new codes
        for (TradeCode code : tradeCodes) {
            planet.addTradeCode(code);
        }
    }

    /**
     * Applies a complete TravellerWorld profile to a Planet.
     * <p>
     * Combines UWP and trade codes from a TravellerWorld into a single Planet.
     * </p>
     *
     * @param planet the planet to update
     * @param travellerWorld the Traveller world to adapt
     */
    public static void applyTravellerWorld(Planet planet, TravellerWorld travellerWorld) {
        if (planet == null || travellerWorld == null) return;

        // Apply UWP
        applyUWP(planet, travellerWorld.getUwp());

        // Apply trade codes
        applyTradeCodes(planet, travellerWorld.getTradeCodes());
    }

    /**
     * Derives a PlanetSize from a UWP planet size code.
     * <p>
     * UWP planet size codes:
     * </p>
     * <ul>
     *   <li>0-2: Small sub-terran → TINY</li>
     *   <li>3-4: Sub-terran → SMALL</li>
     *   <li>5-6: Terran → MEDIUM</li>
     *   <li>7-8: Super-terran → LARGE</li>
     *   <li>9-A: Gas giant → GIANT</li>
     * </ul>
     *
     * @param uwpPlanetSize the UWP planet size code (0-10)
     * @return the corresponding PlanetSize, or null if invalid
     */
    public static PlanetSize derivePlanetSize(int uwpPlanetSize) {
        if (uwpPlanetSize < 0 || uwpPlanetSize > 10) return null;

        if (uwpPlanetSize <= 2) {
            return PlanetSize.SMALL;
        } else if (uwpPlanetSize <= 4) {
            return PlanetSize.SMALL;
        } else if (uwpPlanetSize <= 6) {
            return PlanetSize.MEDIUM;
        } else if (uwpPlanetSize <= 8) {
            return PlanetSize.LARGE;
        } else {
            return PlanetSize.HUGE;
        }
    }

    /**
     * Calculates a resource production modifier based on trade codes.
     * <p>
     * Trade codes that indicate resource-rich worlds provide bonuses:
     * </p>
     * <ul>
     *   <li>Agricultural (Ag): +20% food production</li>
     *   <li>Urbanized (Ur): +10% industrial output</li>
     *   <li>High Population (Hi): +15% labor force</li>
     *   <li>Poor (Po): -20% all resources</li>
     *   <li>Colonial (Co): -10% all resources</li>
     *   <li>Ecumenopolis (Ec): +30% industrial output</li>
     * </ul>
     *
     * @param tradeCodes the trade codes for the planet
     * @return the resource production multiplier (1.0 = no change)
     */
    public static double calculateResourceModifier(Set<TradeCode> tradeCodes) {
        if (tradeCodes == null || tradeCodes.isEmpty()) {
            return 1.0;
        }

        double modifier = 1.0;

        for (TradeCode code : tradeCodes) {
            switch (code) {
                case AG:
                    modifier *= 1.2; // +20% food
                    break;
                case UR:
                    modifier *= 1.1; // +10% industrial
                    break;
                case HI:
                    modifier *= 1.15; // +15% labor
                    break;
                case PO:
                    modifier *= 0.8; // -20% all
                    break;
                case CO:
                    modifier *= 0.9; // -10% all
                    break;
                case EC:
                    modifier *= 1.3; // +30% industrial
                    break;
                case NI:
                    modifier *= 0.5; // No trade = very low resources
                    break;
                default:
                    break;
            }
        }

        return Math.max(0.1, modifier);
    }

    /**
     * Determines if a world is habitable based on UWP atmosphere and gravity.
     * <p>
     * A world is habitable if:
     * </p>
     * <ul>
     *   <li>Atmosphere is breathable (codes 1-7 in Classic Traveller)</li>
     *   <li>Gravity is survivable (codes 1-8)</li>
     * </ul>
     *
     * @param uwp the Universal World Profile
     * @return true if the world is habitable
     */
    public static boolean isHabitable(UniversalWorldProfile uwp) {
        if (uwp == null) return false;

        int atmosphere = uwp.getAtmosphere();
        int gravity = uwp.getGravity();

        // No atmosphere = not habitable
        if (atmosphere == 0) return false;

        // Vacuum or thin atmosphere = not habitable without suits
        if (atmosphere == 8 || atmosphere == 9) return false;

        // Gravity too extreme
        if (gravity == 0 || gravity >= 9) return false;

        return true;
    }

    /**
     * Calculates the population in millions from a UWP population code.
     * <p>
     * UWP population codes map to actual population:
     * </p>
     * <ul>
     *   <li>0: None</li>
     *   <li>1: 1,000</li>
     *   <li>2: 100,000</li>
     *   <li>3: 10 million</li>
     *   <li>4: 100 million</li>
     *   <li>5: 1 billion</li>
     *   <li>6: 10 billion</li>
     *   <li>7: 100 billion</li>
     *   <li>8: 1 trillion</li>
     *   <li>9: 10 trillion</li>
     *   <li>A (10): 100+ trillion</li>
     * </ul>
     *
     * @param uwp the Universal World Profile
     * @return population in millions
     */
    public static long calculatePopulation(UniversalWorldProfile uwp) {
        if (uwp == null) return 0;

        int popCode = uwp.getPopulation();
        switch (popCode) {
            case 0: return 0;
            case 1: return 0; // 1,000 people = 0.001 million
            case 2: return 0; // 100,000 = 0.1 million
            case 3: return 10;
            case 4: return 100;
            case 5: return 1_000;
            case 6: return 10_000;
            case 7: return 100_000;
            case 8: return 1_000_000;
            case 9: return 10_000_000;
            case 10: return 100_000_000;
            default: return 0;
        }
    }

    /**
     * Derives economic level from UWP government and population.
     * <p>
     * Higher government and population codes indicate more developed economies.
     * </p>
     *
     * @param uwp the Universal World Profile
     * @return economic level (0-10)
     */
    public static int deriveEconomicLevel(UniversalWorldProfile uwp) {
        if (uwp == null) return 0;

        int government = uwp.getGovernment();
        int population = uwp.getPopulation();

        // Economic level is roughly the average of government and population
        if (government == 0 && population == 0) return 0;

        int govValue = Math.max(1, government);
        return Math.min(10, (govValue + population) / 2);
    }
}
