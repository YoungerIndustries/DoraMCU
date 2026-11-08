package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the personality profile of an AI empire, derived from the player's
 * government type and ethos.
 * <p>
 * Personality traits range from 0.0 to 1.0 and influence how the AI evaluates
 * and selects actions during decision making.
 * </p>
 */
public class AiPersonality {

    private final Player.GovernmentType governmentType;
    private final Player.Ethos ethos;

    /** Aggression level: how likely the AI is to engage in combat. */
    private final double aggression;

    /** Expansion drive: how likely the AI is to explore and expand. */
    private final double expansionDrive;

    /** Economic focus: how likely the AI is to invest in economy. */
    private final double economicFocus;

    /** Scientific focus: how likely the AI is to invest in research. */
    private final double scientificFocus;

    /** Diplomatic stance: how likely the AI is to pursue diplomacy. */
    private final double diplomaticStance;

    /** Risk tolerance: how likely the AI is to take risky actions. */
    private final double riskTolerance;

    /** Human-readable label for this personality. */
    private final String label;

    /**
     * Creates an AiPersonality from a GovernmentType and Ethos.
     * <p>
     * Personality traits are derived from the combination of government type and ethos,
     * producing deterministic but varied profiles for each combination.
     * </p>
     *
     * @param governmentType the government type
     * @param ethos the ethos
     */
    public AiPersonality(Player.GovernmentType governmentType, Player.Ethos ethos) {
        this.governmentType = governmentType;
        this.ethos = ethos;

        // Base traits from government type
        double baseAggression = 0.4;
        double baseExpansion = 0.4;
        double baseEconomic = 0.4;
        double baseScientific = 0.4;
        double baseDiplomatic = 0.4;
        double baseRisk = 0.4;

        switch (governmentType) {
            case DICTATORSHIP:
                baseAggression = 0.7;
                baseRisk = 0.6;
                baseDiplomatic = 0.2;
                break;
            case DEMOCRACY:
                baseDiplomatic = 0.6;
                baseScientific = 0.5;
                break;
            case MONARCHY:
                baseExpansion = 0.6;
                baseAggression = 0.5;
                break;
            case CORPORATE:
                baseEconomic = 0.8;
                baseRisk = 0.5;
                break;
            case CONFEDERATION:
                baseDiplomatic = 0.8;
                baseExpansion = 0.5;
                break;
            case THEOCRACY:
                baseScientific = 0.3;
                baseAggression = 0.5;
                break;
            case OLIGARCHY:
                baseEconomic = 0.6;
                baseAggression = 0.5;
                break;
            case AUTHORITARIAN:
                baseAggression = 0.6;
                baseRisk = 0.5;
                break;
            default:
                break;
        }

        // Ethos modifiers
        switch (ethos) {
            case MILITARIST:
                baseAggression += 0.2;
                baseRisk += 0.1;
                baseDiplomatic -= 0.1;
                break;
            case PACIFIST:
                baseAggression -= 0.2;
                baseDiplomatic += 0.2;
                baseRisk -= 0.1;
                break;
            case MATERIALIST:
                baseScientific += 0.2;
                baseEconomic += 0.1;
                break;
            case SPIRITUALIST:
                baseScientific -= 0.1;
                baseDiplomatic += 0.1;
                break;
            case EQUALIST:
                baseDiplomatic += 0.1;
                baseEconomic += 0.1;
                break;
            case LIBERTARIAN:
                baseAggression += 0.1;
                baseRisk += 0.1;
                break;
            default:
                break;
        }

        this.aggression = Math.max(0.0, Math.min(1.0, baseAggression));
        this.expansionDrive = Math.max(0.0, Math.min(1.0, baseExpansion));
        this.economicFocus = Math.max(0.0, Math.min(1.0, baseEconomic));
        this.scientificFocus = Math.max(0.0, Math.min(1.0, baseScientific));
        this.diplomaticStance = Math.max(0.0, Math.min(1.0, baseDiplomatic));
        this.riskTolerance = Math.max(0.0, Math.min(1.0, baseRisk));
        this.label = governmentType.name() + "_" + ethos.name();
    }

    /**
     * Creates an AiPersonality from a Player's attributes.
     * <p>
     * Uses the player's government type and ethos to derive personality traits.
     * </p>
     *
     * @param player the player to derive personality from
     * @return a new AiPersonality instance
     */
    public static AiPersonality fromPlayer(Player player) {
        return new AiPersonality(player.getGovernmentType(), player.getEthos());
    }

    // --- Accessors ---

    public Player.GovernmentType getGovernmentType() {
        return governmentType;
    }

    public Player.Ethos getEthos() {
        return ethos;
    }

    public double getAggression() {
        return aggression;
    }

    public double getExpansionDrive() {
        return expansionDrive;
    }

    public double getEconomicFocus() {
        return economicFocus;
    }

    public double getScientificFocus() {
        return scientificFocus;
    }

    public double getDiplomaticStance() {
        return diplomaticStance;
    }

    public double getRiskTolerance() {
        return riskTolerance;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns all personality axes as a map.
     *
     * @return a map of axis name to value
     */
    public Map<String, Double> toMap() {
        Map<String, Double> map = new HashMap<>();
        map.put("aggression", aggression);
        map.put("expansionDrive", expansionDrive);
        map.put("economicFocus", economicFocus);
        map.put("scientificFocus", scientificFocus);
        map.put("diplomaticStance", diplomaticStance);
        map.put("riskTolerance", riskTolerance);
        return map;
    }

    @Override
    public String toString() {
        return "AiPersonality{" +
                "label='" + label + '\'' +
                ", aggression=" + String.format("%.2f", aggression) +
                ", expansion=" + String.format("%.2f", expansionDrive) +
                ", economic=" + String.format("%.2f", economicFocus) +
                ", scientific=" + String.format("%.2f", scientificFocus) +
                ", diplomatic=" + String.format("%.2f", diplomaticStance) +
                ", risk=" + String.format("%.2f", riskTolerance) +
                '}';
    }
}
