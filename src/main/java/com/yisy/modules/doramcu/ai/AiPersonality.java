package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the personality profile of an AI-controlled empire.
 * <p>
 * Each personality is derived from the combination of a {@link Player.GovernmentType}
 * and {@link Player.Ethos}, producing unique decision-making weights that shape
 * how the AI behaves strategically and tactically.
 * </p>
 * <p>
 * The 8 GovernmentTypes × 6 Ethos types yield 48 possible personality combinations.
 * Each combination defines values on several personality axes:
 * <ul>
 *   <li><b>Aggression</b> (0.0-1.0): Likelihood to engage in combat</li>
 *   <li><b>Expansion drive</b> (0.0-1.0): Priority on territorial growth</li>
 *   <li><b>Economic focus</b> (0.0-1.0): Priority on resource accumulation</li>
 *   <li><b>Scientific focus</b> (0.0-1.0): Priority on research</li>
 *   <li><b>Diplomatic stance</b> (0.0-1.0): Willingness to cooperate</li>
 *   <li><b>Risk tolerance</b> (0.0-1.0): Willingness to take risky actions</li>
 * </ul>
 * </p>
 */
public class AiPersonality {

    private final Player.GovernmentType governmentType;
    private final Player.Ethos ethos;
    private final double aggression;
    private final double expansionDrive;
    private final double economicFocus;
    private final double scientificFocus;
    private final double diplomaticStance;
    private final double riskTolerance;

    /**
     * Constructs an AiPersonality from government type and ethos.
     *
     * @param governmentType the government type of the empire
     * @param ethos the ethos of the empire
     */
    public AiPersonality(Player.GovernmentType governmentType, Player.Ethos ethos) {
        this.governmentType = governmentType;
        this.ethos = ethos;
        double[] weights = computeWeights(governmentType, ethos);
        this.aggression = weights[0];
        this.expansionDrive = weights[1];
        this.economicFocus = weights[2];
        this.scientificFocus = weights[3];
        this.diplomaticStance = weights[4];
        this.riskTolerance = weights[5];
    }

    /**
     * Computes the personality weights for a given government type and ethos combination.
     *
     * @param governmentType the government type
     * @param ethos the ethos
     * @return array of 6 weights: [aggression, expansion, economic, scientific, diplomatic, risk]
     */
    private static double[] computeWeights(Player.GovernmentType governmentType, Player.Ethos ethos) {
        double[] base = governmentBase(governmentType);
        double[] modifier = ethosModifier(ethos);

        double[] result = new double[6];
        for (int i = 0; i < 6; i++) {
            result[i] = Math.max(0.0, Math.min(1.0, base[i] + modifier[i]));
        }
        return result;
    }

    /**
     * Returns the base personality weights for each government type.
     */
    private static double[] governmentBase(Player.GovernmentType type) {
        switch (type) {
            case DICTATORSHIP:
                return new double[]{0.8, 0.7, 0.4, 0.3, 0.2, 0.8};
            case DEMOCRACY:
                return new double[]{0.4, 0.5, 0.6, 0.5, 0.7, 0.4};
            case MONARCHY:
                return new double[]{0.6, 0.6, 0.5, 0.4, 0.5, 0.5};
            case OLIGARCHY:
                return new double[]{0.5, 0.6, 0.7, 0.4, 0.4, 0.6};
            case THEOCRACY:
                return new double[]{0.5, 0.4, 0.3, 0.7, 0.3, 0.5};
            case AUTHORITARIAN:
                return new double[]{0.7, 0.7, 0.5, 0.3, 0.2, 0.7};
            case CONFEDERATION:
                return new double[]{0.3, 0.4, 0.6, 0.6, 0.8, 0.3};
            case CORPORATE:
                return new double[]{0.4, 0.6, 0.9, 0.5, 0.5, 0.6};
            default:
                return new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        }
    }

    /**
     * Returns the ethos modifier that adjusts the base government weights.
     */
    private static double[] ethosModifier(Player.Ethos ethos) {
        switch (ethos) {
            case MILITARIST:
                return new double[]{0.3, 0.2, -0.1, -0.1, -0.2, 0.2};
            case PACIFIST:
                return new double[]{-0.3, -0.1, 0.1, 0.1, 0.3, -0.2};
            case MATERIALIST:
                return new double[]{-0.1, 0.1, 0.2, 0.3, 0.0, 0.1};
            case SPIRITUALIST:
                return new double[]{-0.1, 0.0, -0.1, 0.2, 0.1, 0.0};
            case LIBERTARIAN:
                return new double[]{-0.1, 0.1, 0.2, 0.0, 0.2, 0.1};
            case EQUALIST:
                return new double[]{-0.1, 0.0, 0.1, 0.1, 0.2, -0.1};
            default:
                return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        }
    }

    /**
     * Creates an AiPersonality from a Player's government type and ethos.
     *
     * @param player the player to derive the personality from
     * @return the computed personality
     */
    public static AiPersonality fromPlayer(Player player) {
        Player.GovernmentType gov = player.getGovernmentType();
        Player.Ethos e = player.getEthos();
        return new AiPersonality(gov, e);
    }

    /**
     * Returns a descriptive label for this personality.
     */
    public String getLabel() {
        return governmentType.name() + "_" + ethos.name();
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

    /**
     * Returns all personality axes as a map for easy access.
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
                "gov=" + governmentType +
                ", ethos=" + ethos +
                ", aggression=" + String.format("%.2f", aggression) +
                ", expansion=" + String.format("%.2f", expansionDrive) +
                ", economic=" + String.format("%.2f", economicFocus) +
                ", scientific=" + String.format("%.2f", scientificFocus) +
                ", diplomatic=" + String.format("%.2f", diplomaticStance) +
                ", risk=" + String.format("%.2f", riskTolerance) +
                '}';
    }
}
