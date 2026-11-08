package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Illegal contraband commodities from Classic Traveller B07: Merchant Prince.
 * <p>
 * Smugglers deal in illegal goods that fetch 2-5x the price of legal commodities,
 * but carry the risk of customs inspection, fines, imprisonment, or worse.
 * </p>
 * <p>
 * Derived from Classic Traveller Supplement 7: Traders & Gunboats (1986).
 * </p>
 */
public enum ContrabandCommodity {

    /**
     * Illegal drugs and narcotics. High profit, high risk.
     */
    DRUGS("Drugs", "Illegal narcotics and controlled substances", 3, 500, 20),

    /**
     * Unregistered weapons and military hardware.
     */
    WEAPONS("Weapons", "Unregistered firearms and military hardware", 2, 300, 10),

    /**
     * Human trafficking — illegal in most of the Imperium.
     */
    SLAVES("Slaves", "Human trafficking (illegal in most systems)", 4, 1000, 1),

    /**
     * Stolen art, artifacts, and valuables.
     */
    STOLEN_ART("Stolen Art", "Stolen artwork, artifacts, and valuables", 3, 800, 5),

    /**
     * Counterfeit currency and documents.
     */
    COUNTERFEIT("Counterfeit", "Fake currency, documents, and credentials", 1, 100, 50),

    /**
     * Restricted technology above local TL.
     */
    TECH("Restricted Tech", "Technology above local TL limit", 3, 600, 8),

    /**
     * Endangered species products (ivory, furs, etc.).
     */
    ENDANGERED("Endangered Species", "Products from protected species", 2, 400, 15),

    /**
     * Smuggled alcohol and tobacco (tax evasion).
     */
    TAXEVASION("Tax Evasion", "Untaxed alcohol, tobacco, and luxuries", 1, 150, 30);

    private final String displayName;
    private final String description;

    /**
     * Risk level: 1 = low, 5 = extremely high.
     */
    private final int riskLevel;

    /**
     * Base price per unit in credits (2-5x legal commodity equivalent).
     */
    private final int basePrice;

    /**
     * Volume in cubic meters per unit.
     */
    private final int volumePerUnit;

    ContrabandCommodity(String displayName, String description, int riskLevel,
                        int basePrice, int volumePerUnit) {
        this.displayName = displayName;
        this.description = description;
        this.riskLevel = riskLevel;
        this.basePrice = basePrice;
        this.volumePerUnit = volumePerUnit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getRiskLevel() {
        return riskLevel;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public int getVolumePerUnit() {
        return volumePerUnit;
    }

    /**
     * Returns the profit multiplier based on risk level.
     * Higher risk = higher potential profit.
     */
    public double getProfitMultiplier() {
        return 1.5 + riskLevel * 0.5; // 2.0x to 4.0x
    }
}
