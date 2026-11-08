package com.yisy.modules.doramcu.traveller;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Market conditions for a single world, tracking commodity availability.
 * <p>
 * Each commodity on a world has one of three market conditions:
 * </p>
 * <ul>
 *   <li>SURPLUS — commodity is abundant; sell price is 50% of normal</li>
 *   <li>NORMAL — commodity is available at standard price</li>
 *   <li>SHORT — commodity is scarce; buy price is 200% of normal</li>
 *   <li>NONE — commodity is unavailable for trade</li>
 * </ul>
 * <p>
 * Market conditions are derived from the world's trade codes, population,
 * and technology level, following Classic Traveller trade rules.
 * </p>
 */
public class MarketConditions {

    private final Map<Commodity, Condition> conditions;
    private final Random random;

    /**
     * Creates market conditions for a world based on its characteristics.
     *
     * @param world the world to derive market conditions for
     */
    public MarketConditions(TravellerWorld world) {
        this(world, new Random());
    }

    /**
     * Creates market conditions for a world with a specific random seed.
     *
     * @param world the world to derive market conditions for
     * @param random the random generator for variability
     */
    public MarketConditions(TravellerWorld world, Random random) {
        this.random = random;
        this.conditions = new EnumMap<>(Commodity.class);

        for (Commodity commodity : Commodity.values()) {
            Condition condition = determineCondition(world, commodity);
            conditions.put(commodity, condition);
        }
    }

    /**
     * Creates empty market conditions (all NONE).
     */
    public MarketConditions() {
        this.random = new Random();
        this.conditions = new EnumMap<>(Commodity.class);
        for (Commodity commodity : Commodity.values()) {
            conditions.put(commodity, Condition.NONE);
        }
    }

    /**
     * Determines the market condition for a commodity on a world.
     *
     * @param world the world
     * @param commodity the commodity
     * @return the market condition
     */
    private Condition determineCondition(TravellerWorld world, Commodity commodity) {
        Set<TradeCode> tradeCodes = world.getTradeCodes();
        UniversalWorldProfile uwp = world.getUwp();

        // Derive a rough technology level from population and government
        // Higher population + higher government generally indicates higher tech
        int techLevel = (uwp.getPopulation() + uwp.getGovernment()) / 2;

        // Check if commodity is illegal on this world
        if (commodity.getRarity() == Commodity.Rarity.ILLEGAL) {
            // Illegal goods only available on lawless worlds
            if (uwp.getLawLevel() <= 2 || world.hasTradeCode(TradeCode.LW) || world.hasTradeCode(TradeCode.AN)) {
                return Condition.SHORT;
            }
            return Condition.NONE;
        }

        // Check world habitability
        if (!world.isHabitable() || world.isVacuum()) {
            return Condition.NONE;
        }

        // Check starport availability
        if (!world.hasStarport()) {
            return Condition.NONE;
        }

        // Derive condition based on trade codes and commodity type
        switch (commodity) {
            case FOOD:
                if (world.hasTradeCode(TradeCode.AG)) {
                    return Condition.SURPLUS;
                }
                if (world.hasTradeCode(TradeCode.PO) || world.hasTradeCode(TradeCode.CO)) {
                    return Condition.SHORT;
                }
                return Condition.NORMAL;

            case MEDICINE:
                if (techLevel >= 7) {
                    return Condition.NORMAL;
                }
                if (techLevel >= 5) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case GEMS:
                if (world.hasTradeCode(TradeCode.MO)) {
                    return Condition.NORMAL;
                }
                if (world.getPortWealth() >= 6) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case METALS:
                if (techLevel >= 6) {
                    return Condition.NORMAL;
                }
                if (world.hasTradeCode(TradeCode.CO)) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case TEXTILES:
                if (uwp.getPopulation() >= 5) {
                    return Condition.NORMAL;
                }
                return Condition.SHORT;

            case CHEMICALS:
                if (techLevel >= 6) {
                    return Condition.NORMAL;
                }
                return Condition.SHORT;

            case ELECTRONICS:
                if (techLevel >= 8) {
                    return Condition.NORMAL;
                }
                if (techLevel >= 6) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case LUXURY:
                if (world.getPortWealth() >= 7) {
                    return Condition.NORMAL;
                }
                if (world.getPortWealth() >= 5) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case FUEL:
                return Condition.NORMAL; // Always available at starports

            case MACHINERY:
                if (techLevel >= 7) {
                    return Condition.NORMAL;
                }
                if (techLevel >= 5) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case WEAPONS:
                if (uwp.getLawLevel() <= 3) {
                    return Condition.NORMAL;
                }
                if (uwp.getLawLevel() <= 5) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            case TIMBER:
                if (world.hasTradeCode(TradeCode.MO) || world.hasTradeCode(TradeCode.AG)) {
                    return Condition.SURPLUS;
                }
                if (uwp.getPlanetSize() >= 1 && uwp.getPlanetSize() <= 7) {
                    return Condition.NORMAL;
                }
                return Condition.NONE;

            case FURS:
                if (world.hasTradeCode(TradeCode.CE)) {
                    return Condition.SURPLUS;
                }
                return Condition.NONE;

            case SPICES:
                if (world.hasTradeCode(TradeCode.AG)) {
                    return Condition.SURPLUS;
                }
                if (uwp.getPlanetSize() >= 1 && uwp.getPlanetSize() <= 7) {
                    return Condition.NORMAL;
                }
                return Condition.NONE;

            case ALCOHOL:
                if (uwp.getPopulation() >= 4) {
                    return Condition.NORMAL;
                }
                return Condition.SHORT;

            case TOBACCO:
                if (world.hasTradeCode(TradeCode.AG)) {
                    return Condition.NORMAL;
                }
                return Condition.SHORT;

            case ANIMALS:
                if (world.hasTradeCode(TradeCode.AG) || world.hasTradeCode(TradeCode.CO)) {
                    return Condition.NORMAL;
                }
                return Condition.NONE;

            case INFORMATION:
                if (uwp.getPopulation() >= 6 && techLevel >= 6) {
                    return Condition.NORMAL;
                }
                return Condition.SHORT;

            case RELIGIOUS:
                if (uwp.getGovernmentType().contains("Theocracy") || uwp.getGovernment() >= 8) {
                    return Condition.SURPLUS;
                }
                if (uwp.getPopulation() >= 5) {
                    return Condition.SHORT;
                }
                return Condition.NONE;

            default:
                return Condition.NONE;
        }
    }

    /**
     * Returns the market condition for a specific commodity.
     *
     * @param commodity the commodity
     * @return the market condition
     */
    public Condition getCondition(Commodity commodity) {
        return conditions.getOrDefault(commodity, Condition.NONE);
    }

    /**
     * Checks if a commodity is available for trade on this world.
     *
     * @param commodity the commodity
     * @return true if the commodity can be bought or sold
     */
    public boolean isAvailable(Commodity commodity) {
        Condition condition = getCondition(commodity);
        return condition != Condition.NONE;
    }

    /**
     * Checks if a commodity can be bought on this world.
     * (Available and not in surplus — surplus means locals don't need to buy)
     *
     * @param commodity the commodity
     * @return true if the commodity can be purchased
     */
    public boolean canBuy(Commodity commodity) {
        Condition condition = getCondition(commodity);
        return condition == Condition.NORMAL || condition == Condition.SHORT;
    }

    /**
     * Checks if a commodity can be sold on this world.
     * (Available and not short — short means locals don't want to buy)
     *
     * @param commodity the commodity
     * @return true if the commodity can be sold
     */
    public boolean canSell(Commodity commodity) {
        Condition condition = getCondition(commodity);
        return condition == Condition.NORMAL || condition == Condition.SURPLUS;
    }

    /**
     * Returns the price modifier for a commodity based on market conditions.
     * <p>
     * SURPLUS: 0.5× (buyers have many options, sellers compete)
     * NORMAL: 1.0× (standard price)
     * SHORT: 2.0× (scarce goods command premium)
     * NONE: 0× (unavailable)
     * </p>
     *
     * @param commodity the commodity
     * @return the price modifier
     */
    public double getPriceModifier(Commodity commodity) {
        switch (getCondition(commodity)) {
            case SURPLUS: return 0.5;
            case NORMAL: return 1.0;
            case SHORT: return 2.0;
            case NONE: return 0.0;
            default: return 0.0;
        }
    }

    /**
     * Calculates the actual price for a commodity on this world.
     *
     * @param commodity the commodity
     * @param priceLevel the world's price level
     * @return the actual price in credits
     */
    public int getPrice(Commodity commodity, PriceLevel priceLevel) {
        if (!isAvailable(commodity)) {
            return 0;
        }
        int basePrice = commodity.getBasePrice();
        double worldMultiplier = priceLevel.getPriceMultiplier();
        double marketModifier = getPriceModifier(commodity);
        return (int) Math.round(basePrice * worldMultiplier * marketModifier);
    }

    /**
     * Returns all commodities available on this world.
     *
     * @return set of available commodities
     */
    public Set<Commodity> getAvailableCommodities() {
        Set<Commodity> available = Collections.newSetFromMap(new EnumMap<>(Commodity.class));
        for (Map.Entry<Commodity, Condition> entry : conditions.entrySet()) {
            if (entry.getValue() != Condition.NONE) {
                available.add(entry.getKey());
            }
        }
        return available;
    }

    /**
     * Returns all commodities that can be bought on this world.
     *
     * @return set of buyable commodities
     */
    public Set<Commodity> getBuyableCommodities() {
        Set<Commodity> buyable = Collections.newSetFromMap(new EnumMap<>(Commodity.class));
        for (Map.Entry<Commodity, Condition> entry : conditions.entrySet()) {
            if (canBuy(entry.getKey())) {
                buyable.add(entry.getKey());
            }
        }
        return buyable;
    }

    /**
     * Returns all commodities that can be sold on this world.
     *
     * @return set of sellable commodities
     */
    public Set<Commodity> getSellableCommodities() {
        Set<Commodity> sellable = Collections.newSetFromMap(new EnumMap<>(Commodity.class));
        for (Map.Entry<Commodity, Condition> entry : conditions.entrySet()) {
            if (canSell(entry.getKey())) {
                sellable.add(entry.getKey());
            }
        }
        return sellable;
    }

    /**
     * Returns an unmodifiable map of all commodity conditions.
     *
     * @return the conditions map
     */
    public Map<Commodity, Condition> getAllConditions() {
        return Collections.unmodifiableMap(conditions);
    }

    /**
     * Updates the market condition for a specific commodity.
     * Used for random market fluctuations.
     *
     * @param commodity the commodity
     * @param condition the new condition
     */
    public void setCondition(Commodity commodity, Condition condition) {
        conditions.put(commodity, condition);
    }

    /**
     * Simulates random market fluctuations.
     * Each commodity has a chance to change condition.
     */
    public void fluctuate() {
        for (Commodity commodity : Commodity.values()) {
            if (random.nextDouble() < 0.1) { // 10% chance of fluctuation
                Condition current = conditions.get(commodity);
                if (current == Condition.SURPLUS) {
                    conditions.put(commodity, Condition.NORMAL);
                } else if (current == Condition.NORMAL) {
                    conditions.put(commodity, random.nextBoolean() ? Condition.SURPLUS : Condition.SHORT);
                } else if (current == Condition.SHORT) {
                    conditions.put(commodity, Condition.NORMAL);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MarketConditions{");
        for (Map.Entry<Commodity, Condition> entry : conditions.entrySet()) {
            if (entry.getValue() != Condition.NONE) {
                if (sb.length() > 17) sb.append(", ");
                sb.append(entry.getKey().getName()).append("=").append(entry.getValue());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Market condition for a single commodity.
     */
    public enum Condition {
        /**
         * Commodity is abundant; sell price is 50% of normal.
         */
        SURPLUS,

        /**
         * Commodity is available at standard price.
         */
        NORMAL,

        /**
         * Commodity is scarce; buy price is 200% of normal.
         */
        SHORT,

        /**
         * Commodity is unavailable for trade.
         */
        NONE;
    }
}
