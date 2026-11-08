package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.traveller.Commodity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridges Traveller credits with DoraMCU resources through bidirectional conversion.
 * <p>
 * This service provides the economic layer that connects the two systems:
 * </p>
 * <ul>
 *   <li><b>Credits to Resources</b>: Converts trade profits (credits) into refined resources
 *       with dynamic rates based on current resource scarcity</li>
 *   <li><b>Resources to Credits</b>: Converts DoraMCU resources into Traveller credits
 *       for purchasing commodities on Traveller worlds</li>
 *   <li><b>Market Feedback</b>: Resource scarcity in DoraMCU affects Traveller trade prices
 *       — when a resource is scarce, its corresponding commodity becomes more expensive</li>
 * </ul>
 * <p>
 * Exchange rates are dynamic and influenced by:
 * </p>
 * <ul>
 *   <li>Current resource levels in the pipeline (scarcity premium)</li>
 *   <li>Base conversion rates defined per resource type</li>
 *   <li>Market conditions on Traveller worlds</li>
 * </ul>
 * <p>
 * The commodity-to-resource mapping follows Classic Traveller trade rules:
 * </p>
 * <ul>
 *   <li>Food, Medicine → Nutrient</li>
 *   <li>Metals, Machinery → Metal Alloy</li>
 *   <li>Fuel, Chemicals → Fuel</li>
 *   <li>Electronics, Computers → Power Cell</li>
 *   <li>Textiles, Plastics → Composite</li>
 * </ul>
 */
public class CreditExchangeService {

    /** Base conversion rate: 1 credit = this many resource units (before scarcity modifier) */
    private static final double BASE_CREDIT_TO_RESOURCE_RATE = 1.0;

    /** Base conversion rate: 1 resource unit = this many credits (before scarcity modifier) */
    private static final double BASE_RESOURCE_TO_CREDIT_RATE = 1.0;

    /** Minimum scarcity ratio before premium kicks in (storage at 20% capacity) */
    private static final double SCARCITY_THRESHOLD = 0.2;

    /** Maximum scarcity premium multiplier (up to 3x when completely empty) */
    private static final double MAX_SCARCITY_PREMIUM = 3.0;

    /** Maximum surplus discount multiplier (down to 0.5x when completely full) */
    private static final double MAX_SURPLUS_DISCOUNT = 0.5;

    /** Surplus threshold: storage above 80% capacity gets discounted conversion */
    private static final double SURPLUS_THRESHOLD = 0.8;

    private final GameContext context;
    private final Random random;

    /** Tracks total credits earned from trade per empire */
    private final Map<Integer, Long> empireTradeCredits = new HashMap<>();

    /** Tracks total resources converted from credits per empire */
    private final Map<Integer, Map<RefinedResourceType, Integer>> empireResourceConversion = new HashMap<>();

    /**
     * Creates a new CreditExchangeService.
     *
     * @param context the shared game context
     */
    public CreditExchangeService(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
    }

    // ============================================================
    // Credit to Resource Conversion
    // ============================================================

    /**
     * Converts Traveller credits to DoraMCU refined resources.
     * <p>
     * The conversion rate is adjusted by resource scarcity: when a resource
     * is scarce in the pipeline, more credits are needed per unit. When
     * abundant, fewer credits are needed.
     * </p>
     *
     * @param pipeline the resource pipeline to add resources to
     * @param credits the credits to convert
     * @param commodity the commodity type that generated the credits (determines resource type)
     * @return the number of resource units added
     */
    public int convertCreditsToResources(ResourcePipelineManager pipeline, long credits, Commodity commodity) {
        if (pipeline == null || credits <= 0 || commodity == null) {
            return 0;
        }

        RefinedResourceType resourceType = mapCommodityToResource(commodity);
        ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();

        // Calculate dynamic exchange rate based on scarcity
        double exchangeRate = calculateCreditToResourceRate(storage, resourceType);
        int resourceUnits = (int) Math.floor(credits * exchangeRate);

        if (resourceUnits <= 0) {
            return 0;
        }

        // Add to storage (respecting capacity limits)
        int added = storage.add(resourceType, resourceUnits);

        // Track conversion
        int empireId = pipeline.getOwnerEmpireId();
        empireTradeCredits.merge(empireId, credits - (long)(added / exchangeRate), Long::sum);
        empireResourceConversion
                .computeIfAbsent(empireId, k -> new EnumMap<>(RefinedResourceType.class))
                .merge(resourceType, added, Integer::sum);

        return added;
    }

    /**
     * Calculates the dynamic credit-to-resource exchange rate.
     * <p>
     * When resources are scarce (low storage ratio), the rate decreases
     * (more credits needed per unit). When abundant, the rate increases.
     * </p>
     *
     * @param storage the refined resource storage
     * @param resourceType the resource type
     * @return the exchange rate (resource units per credit)
     */
    public double calculateCreditToResourceRate(ResourceStorage<RefinedResourceType> storage,
                                                 RefinedResourceType resourceType) {
        int currentAmount = storage.getAmount(resourceType);
        int capacity = storage.getCapacity(resourceType);
        double ratio = capacity > 0 ? (double) currentAmount / capacity : 0.0;

        double rate = BASE_CREDIT_TO_RESOURCE_RATE;

        if (ratio < SCARCITY_THRESHOLD) {
            // Scarcity premium: fewer resources per credit when scarce
            // Linear interpolation from MAX_SCARCITY_PREMIUM at 0% to 1.0 at threshold
            double scarcityFactor = ratio / SCARCITY_THRESHOLD;
            double premium = 1.0 / (1.0 + (MAX_SCARCITY_PREMIUM - 1.0) * (1.0 - scarcityFactor));
            rate *= premium;
        } else if (ratio > SURPLUS_THRESHOLD) {
            // Surplus discount: more resources per credit when abundant
            // Linear interpolation from 1.0 at threshold to MAX_SURPLUS_DISCOUNT at 100%
            double surplusFactor = (ratio - SURPLUS_THRESHOLD) / (1.0 - SURPLUS_THRESHOLD);
            double discount = 1.0 + (1.0 - MAX_SURPLUS_DISCOUNT) * surplusFactor;
            rate *= discount;
        }

        return rate;
    }

    // ============================================================
    // Resource to Credit Conversion
    // ============================================================

    /**
     * Converts DoraMCU refined resources to Traveller credits.
     * <p>
     * Used when the player wants to sell resources to fund Traveller trade
     * operations. The conversion rate reflects the resource's market value.
     * </p>
     *
     * @param pipeline the resource pipeline to remove resources from
     * @param resourceType the resource type to convert
     * @param amount the amount of resources to convert
     * @return the number of credits generated
     */
    public long convertResourcesToCredits(ResourcePipelineManager pipeline,
                                           RefinedResourceType resourceType, int amount) {
        if (pipeline == null || resourceType == null || amount <= 0) {
            return 0;
        }

        ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();
        int available = storage.getAmount(resourceType);
        int actualAmount = Math.min(amount, available);

        if (actualAmount <= 0) {
            return 0;
        }

        // Calculate dynamic exchange rate
        double exchangeRate = calculateResourceToCreditRate(storage, resourceType);
        long credits = Math.round(actualAmount * exchangeRate);

        // Remove from storage
        storage.remove(resourceType, actualAmount);

        return credits;
    }

    /**
     * Calculates the dynamic resource-to-credit exchange rate.
     * <p>
     * When resources are scarce, each unit is worth more credits.
     * When abundant, each unit is worth fewer credits.
     * </p>
     *
     * @param storage the refined resource storage
     * @param resourceType the resource type
     * @return the exchange rate (credits per resource unit)
     */
    public double calculateResourceToCreditRate(ResourceStorage<RefinedResourceType> storage,
                                                 RefinedResourceType resourceType) {
        int currentAmount = storage.getAmount(resourceType);
        int capacity = storage.getCapacity(resourceType);
        double ratio = capacity > 0 ? (double) currentAmount / capacity : 0.0;

        double rate = BASE_RESOURCE_TO_CREDIT_RATE;

        if (ratio < SCARCITY_THRESHOLD) {
            // Scarcity premium: more credits per unit when scarce
            double scarcityFactor = ratio / SCARCITY_THRESHOLD;
            double premium = 1.0 + (MAX_SCARCITY_PREMIUM - 1.0) * (1.0 - scarcityFactor);
            rate *= premium;
        } else if (ratio > SURPLUS_THRESHOLD) {
            // Surplus discount: fewer credits per unit when abundant
            double surplusFactor = (ratio - SURPLUS_THRESHOLD) / (1.0 - SURPLUS_THRESHOLD);
            double discount = MAX_SURPLUS_DISCOUNT + (1.0 - MAX_SURPLUS_DISCOUNT) * (1.0 - surplusFactor);
            rate *= discount;
        }

        return rate;
    }

    // ============================================================
    // Market Feedback Loop
    // ============================================================

    /**
     * Calculates a price modifier for Traveller trade based on DoraMCU resource scarcity.
     * <p>
     * When DoraMCU resources are scarce, the corresponding Traveller commodities
     * become more expensive to buy (higher cost) and more valuable to sell (higher revenue).
     * This creates a bidirectional feedback loop between the two economic systems.
     * </p>
     *
     * @param pipeline the resource pipeline to check
     * @param commodity the Traveller commodity
     * @return a price modifier (1.0 = no change, 2.0 = double price, 0.5 = half price)
     */
    public double getResourceScarcityModifier(ResourcePipelineManager pipeline, Commodity commodity) {
        if (pipeline == null || commodity == null) {
            return 1.0;
        }

        RefinedResourceType resourceType = mapCommodityToResource(commodity);
        ResourceStorage<RefinedResourceType> storage = pipeline.getRefinedStorage();

        int currentAmount = storage.getAmount(resourceType);
        int capacity = storage.getCapacity(resourceType);
        double ratio = capacity > 0 ? (double) currentAmount / capacity : 0.0;

        // When resource is scarce, commodity price increases
        // When resource is abundant, commodity price decreases
        if (ratio < SCARCITY_THRESHOLD) {
            // Scarcity: price increases up to 2x
            double scarcityFactor = ratio / SCARCITY_THRESHOLD;
            return 1.0 + 1.0 * (1.0 - scarcityFactor); // 1.0 to 2.0
        } else if (ratio > SURPLUS_THRESHOLD) {
            // Surplus: price decreases down to 0.7x
            double surplusFactor = (ratio - SURPLUS_THRESHOLD) / (1.0 - SURPLUS_THRESHOLD);
            return 1.0 - 0.3 * surplusFactor; // 1.0 to 0.7
        }

        return 1.0;
    }

    /**
     * Applies resource scarcity modifiers to all commodity prices for a world economy.
     * <p>
     * Returns a map of commodity to price modifier that should be applied
     * to the world's base prices.
     * </p>
     *
     * @param pipeline the resource pipeline
     * @return map of commodity to price modifier
     */
    public Map<Commodity, Double> getAllScarcityModifiers(ResourcePipelineManager pipeline) {
        Map<Commodity, Double> modifiers = new EnumMap<>(Commodity.class);
        for (Commodity commodity : Commodity.values()) {
            modifiers.put(commodity, getResourceScarcityModifier(pipeline, commodity));
        }
        return modifiers;
    }

    // ============================================================
    // Commodity to Resource Mapping
    // ============================================================

    /**
     * Maps a Traveller commodity to a DoraMCU refined resource type.
     * <p>
     * This mapping determines what resources are produced when trade profit
     * is converted to materials for the resource pipeline, and what resources
     * are consumed when purchasing commodities with DoraMCU materials.
     * </p>
     *
     * @param commodity the Traveller commodity
     * @return the corresponding refined resource type
     */
    public RefinedResourceType mapCommodityToResource(Commodity commodity) {
        if (commodity == null) {
            return RefinedResourceType.METAL_ALLOY;
        }

        switch (commodity) {
            case FOOD:
            case MEDICINE:
                return RefinedResourceType.NUTRIENT;

            case FUEL:
            case CHEMICALS:
                return RefinedResourceType.FUEL;

            case ELECTRONICS:
            case INFORMATION:
                return RefinedResourceType.POWER_CELL;

            case TEXTILES:
            case TOBACCO:
            case ALCOHOL:
                return RefinedResourceType.COMPOSITE;

            case METALS:
            case MACHINERY:
            case WEAPONS:
            case GEMS:
            case LUXURY:
            case TIMBER:
            case FURS:
            case SPICES:
            case ANIMALS:
            case SLAVES:
            case RELIGIOUS:
            default:
                return RefinedResourceType.METAL_ALLOY;
        }
    }

    /**
     * Maps a DoraMCU refined resource type to the most relevant Traveller commodity.
     * <p>
     * Used when converting resources to credits for trade operations.
     * </p>
     *
     * @param resourceType the refined resource type
     * @return the corresponding Traveller commodity
     */
    public Commodity mapResourceToCommodity(RefinedResourceType resourceType) {
        if (resourceType == null) {
            return Commodity.METALS;
        }

        switch (resourceType) {
            case NUTRIENT:
                return Commodity.FOOD;
            case FUEL:
                return Commodity.FUEL;
            case POWER_CELL:
                return Commodity.ELECTRONICS;
            case COMPOSITE:
                return Commodity.TEXTILES;
            case METAL_ALLOY:
            default:
                return Commodity.METALS;
        }
    }

    // ============================================================
    // Empire Credit Balance
    // ============================================================

    /**
     * Adds credits to an empire's trade credit balance.
     *
     * @param empireId the empire ID
     * @param credits the credits to add
     */
    public void addTradeCredits(int empireId, long credits) {
        empireTradeCredits.merge(empireId, credits, Long::sum);
    }

    /**
     * Spends credits from an empire's trade credit balance.
     *
     * @param empireId the empire ID
     * @param credits the credits to spend
     * @return true if sufficient credits were available
     */
    public boolean spendTradeCredits(int empireId, long credits) {
        long current = empireTradeCredits.getOrDefault(empireId, 0L);
        if (current >= credits) {
            empireTradeCredits.merge(empireId, -credits, Long::sum);
            return true;
        }
        return false;
    }

    /**
     * Gets an empire's current trade credit balance.
     *
     * @param empireId the empire ID
     * @return the credit balance
     */
    public long getTradeCreditBalance(int empireId) {
        return empireTradeCredits.getOrDefault(empireId, 0L);
    }

    // ============================================================
    // Summary & Reporting
    // ============================================================

    /**
     * Returns a summary of the credit exchange state for an empire.
     *
     * @param empireId the empire ID
     * @return a summary map with credit balance and resource conversion stats
     */
    public Map<String, Object> getExchangeSummary(int empireId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("empireId", empireId);
        summary.put("tradeCredits", getTradeCreditBalance(empireId));

        Map<RefinedResourceType, Integer> conversions = empireResourceConversion.get(empireId);
        if (conversions != null) {
            summary.put("resourceConversions", Collections.unmodifiableMap(conversions));
        } else {
            summary.put("resourceConversions", new HashMap<>());
        }

        return summary;
    }

    /**
     * Returns all exchange summaries for all empires.
     *
     * @return list of empire exchange summaries
     */
    public List<Map<String, Object>> getAllExchangeSummaries() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (int empireId : empireTradeCredits.keySet()) {
            summaries.add(getExchangeSummary(empireId));
        }
        return summaries;
    }
}
