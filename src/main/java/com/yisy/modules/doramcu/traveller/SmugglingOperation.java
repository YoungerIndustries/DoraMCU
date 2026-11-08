package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * A smuggling operation from Classic Traveller B07: Merchant Prince.
 * <p>
 * Represents a complete smuggling mission: buying contraband at a source world,
 * transporting it to a destination world, evading customs, and selling for profit.
 * </p>
 * <p>
 * Derived from Classic Traveller Book 7: Merchant Prince (1985).
 * </p>
 */
public class SmugglingOperation {

    /**
     * Status of the smuggling operation.
     */
    public enum Status {
        PLANNED("Planned"),
        IN_TRANSIT("In Transit"),
        CUSTOMS_CHECK("Customs Check"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        COMBAT("Combat");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final int id;
    private final ContrabandCommodity contraband;
    private final int quantity;
    private final String sourceWorld;
    private final String destinationWorld;
    private final int destinationLawLevel;
    private final char destinationStarport;

    private Status status;
    private long purchaseCost;
    private long sellRevenue;
    private long profit;
    private long finePaid;
    private long bribePaid;
    private boolean cargoConfiscated;
    private CustomsCheck.CustomsResult customsResult;
    private final List<String> log;

    public SmugglingOperation(int id, ContrabandCommodity contraband, int quantity,
                               String sourceWorld, String destinationWorld,
                               int destinationLawLevel, char destinationStarport) {
        this.id = id;
        this.contraband = contraband;
        this.quantity = quantity;
        this.sourceWorld = sourceWorld;
        this.destinationWorld = destinationWorld;
        this.destinationLawLevel = destinationLawLevel;
        this.destinationStarport = destinationStarport;
        this.status = Status.PLANNED;
        this.log = new ArrayList<>();
        this.cargoConfiscated = false;
        log.add("Operation planned: " + contraband.getDisplayName() + " x" + quantity);
    }

    /**
     * Calculates the estimated purchase cost at the source world.
     */
    public long calculatePurchaseCost() {
        this.purchaseCost = contraband.getBasePrice() * quantity / 2; // Buy at half price
        log.add("Purchase cost: " + purchaseCost + " credits");
        return purchaseCost;
    }

    /**
     * Calculates the estimated sell revenue at the destination world.
     */
    public long calculateSellRevenue() {
        this.sellRevenue = (long) (contraband.getBasePrice() * quantity * contraband.getProfitMultiplier());
        log.add("Estimated sell revenue: " + sellRevenue + " credits");
        return sellRevenue;
    }

    /**
     * Returns the estimated profit before customs risk.
     */
    public long getEstimatedProfit() {
        return sellRevenue - purchaseCost;
    }

    /**
     * Returns the total cargo volume in cubic meters.
     */
    public int getCargoVolume() {
        return contraband.getVolumePerUnit() * quantity;
    }

    /**
     * Executes the customs check at the destination.
     */
    public CustomsCheck.CustomsResult executeCustomsCheck(CustomsCheck customsCheck,
                                                            CustomsCheck.Action action,
                                                            int pilotSkill, long credits) {
        status = Status.CUSTOMS_CHECK;
        CustomsCheck.CustomsResult result = customsCheck.resolve(action, contraband,
                destinationLawLevel, pilotSkill, credits);
        this.customsResult = result;
        log.add("Customs check: " + result);

        switch (result.getOutcome()) {
            case EVADDED, BRIBED -> {
                status = Status.COMPLETED;
                if (result.getOutcome() == CustomsCheck.Outcome.BRIBED) {
                    this.bribePaid = result.getCreditsLost();
                }
                this.profit = sellRevenue - purchaseCost - bribePaid;
                log.add("Operation completed. Profit: " + profit + " credits");
            }
            case CAUGHT -> {
                status = Status.FAILED;
                this.cargoConfiscated = true;
                this.finePaid = customsCheck.calculateFine(contraband, quantity, destinationLawLevel);
                this.profit = -purchaseCost - finePaid;
                log.add("Caught! Fine: " + finePaid + " credits. Cargo confiscated.");
            }
            case IMPRISONED -> {
                status = Status.FAILED;
                this.cargoConfiscated = true;
                this.profit = -purchaseCost;
                log.add("Imprisoned! Ship and cargo seized.");
            }
            case COMBAT -> {
                status = Status.COMBAT;
                log.add("Combat initiated with customs forces!");
            }
            default -> {
                status = Status.COMPLETED;
                this.profit = sellRevenue - purchaseCost;
            }
        }
        return result;
    }

    // Getters
    public int getId() { return id; }
    public ContrabandCommodity getContraband() { return contraband; }
    public int getQuantity() { return quantity; }
    public String getSourceWorld() { return sourceWorld; }
    public String getDestinationWorld() { return destinationWorld; }
    public int getDestinationLawLevel() { return destinationLawLevel; }
    public char getDestinationStarport() { return destinationStarport; }
    public Status getStatus() { return status; }
    public long getPurchaseCost() { return purchaseCost; }
    public long getSellRevenue() { return sellRevenue; }
    public long getProfit() { return profit; }
    public long getFinePaid() { return finePaid; }
    public long getBribePaid() { return bribePaid; }
    public boolean isCargoConfiscated() { return cargoConfiscated; }
    public CustomsCheck.CustomsResult getCustomsResult() { return customsResult; }
    public List<String> getLog() { return Collections.unmodifiableList(log); }

    @Override
    public String toString() {
        return String.format("Smuggling Op #%d: %s x%d (%s -> %s) [%s] Profit: %d credits",
                id, contraband.getDisplayName(), quantity, sourceWorld, destinationWorld,
                status.getDisplayName(), profit);
    }
}
