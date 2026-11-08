package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * Connects Traveller Merchant career to 4X gameplay.
 * <p>
 * This service extends the basic commander bonus system with detailed
 * trade career mechanics:
 * </p>
 * <ul>
 *   <li><b>Trade Skill Bonuses</b>: Merchant skills (Navigation, Pilot, Electronics)
 *       provide bonuses to trade operations</li>
 *   <li><b>Cargo Capacity</b>: Merchant career tours increase ship cargo capacity</li>
 *   <li><b>Crew Requirements</b>: Trade missions require crew with specific skills</li>
 *   <li><b>Career Progression</b>: Successful trades advance the Merchant career</li>
 * </ul>
 * <p>
 * The service works with {@link CreditExchangeService} to provide a complete
 * trade economy that rewards Merchant career investment.
 * </p>
 */
public class TradeCareerService {

    /** Bonus to trade profit per Merchant skill level */
    private static final double TRADE_SKILL_BONUS_PER_LEVEL = 3.0;

    /** Bonus to cargo capacity per Merchant tour (%) */
    private static final double CARGO_CAPACITY_BONUS_PER_TOUR = 10.0;

    /** Maximum cargo capacity bonus (%) */
    private static final double MAX_CARGO_CAPACITY_BONUS = 50.0;

    /** Navigation skill reduces travel time between systems */
    private static final double NAVIGATION_TIME_REDUCTION_PER_LEVEL = 5.0;

    /** Electronics skill improves trade route discovery */
    private static final double ELECTRONICS_ROUTE_BONUS_PER_LEVEL = 2.0;

    /** Pilot skill improves ship survivability during trade */
    private static final double PILOT_EVASION_BONUS_PER_LEVEL = 2.0;

    /** Wealth threshold for career advancement (credits earned) */
    private static final long CAREER_ADVANCEMENT_THRESHOLD = 100000;

    private final GameContext context;
    private final Random random;

    /** Tracks total trade credits earned per player for career progression */
    private final Map<Integer, Long> playerTradeEarnings = new HashMap<>();

    /** Tracks trade missions completed per player */
    private final Map<Integer, Integer> playerTradeMissions = new HashMap<>();

    /**
     * Creates a new TradeCareerService.
     *
     * @param context the shared game context
     */
    public TradeCareerService(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
    }

    // ============================================================
    // Trade Skill Bonuses
    // ============================================================

    /**
     * Calculates the total trade profit bonus for a player's commander.
     * <p>
     * Combines career tour bonuses with skill-based bonuses:
     * </p>
     * <ul>
     *   <li>Merchant career tours: +5% per tour</li>
     *   <li>Trade skills: +3% per skill level</li>
     *   <li>Navigation skill: reduces travel costs</li>
     * </ul>
     *
     * @param player the player
     * @return the total trade profit bonus percentage
     */
    public double calculateTradeProfitBonus(Player player) {
        if (player == null || !player.hasCommander()) {
            return 0.0;
        }

        TravellerCharacter commander = player.getCommander();

        // Base career bonus
        double careerBonus = commander.getCommanderBonus(Player.CommanderBonusType.TRADE_PROFIT);

        // Skill bonuses
        double skillBonus = 0.0;

        // Trade-related skills
        skillBonus += getSkillLevel(commander, "Navigation") * NAVIGATION_TIME_REDUCTION_PER_LEVEL;
        skillBonus += getSkillLevel(commander, "Pilot") * PILOT_EVASION_BONUS_PER_LEVEL;
        skillBonus += getSkillLevel(commander, "Electronics") * ELECTRONICS_ROUTE_BONUS_PER_LEVEL;
        skillBonus += getSkillLevel(commander, "Steward") * TRADE_SKILL_BONUS_PER_LEVEL;

        return careerBonus + skillBonus;
    }

    /**
     * Gets the skill level for a specific skill name.
     *
     * @param character the character
     * @param skillName the skill name
     * @return the skill level (0 if not found, 1 if present)
     */
    public int getSkillLevel(TravellerCharacter character, String skillName) {
        if (character == null || skillName == null) {
            return 0;
        }
        for (Skill skill : character.getSkills()) {
            if (skill.getName().equalsIgnoreCase(skillName)) {
                return 1; // Skills are binary in TravellerCharacter: present or not
            }
        }
        return 0;
    }

    // ============================================================
    // Cargo Capacity Bonuses
    // ============================================================

    /**
     * Calculates the cargo capacity bonus for a trade ship based on the commander's career.
     * <p>
     * Merchant career tours increase cargo capacity, representing better
     * cargo management and stowage skills.
     * </p>
     *
     * @param player the player
     * @return the cargo capacity bonus percentage (0-50%)
     */
    public double calculateCargoCapacityBonus(Player player) {
        if (player == null || !player.hasCommander()) {
            return 0.0;
        }

        TravellerCharacter commander = player.getCommander();
        long merchantTours = commander.getCareerTours().stream()
                .filter(tour -> tour.getCareer() == Career.MERCHANT)
                .count();

        double bonus = merchantTours * CARGO_CAPACITY_BONUS_PER_TOUR;
        return Math.min(bonus, MAX_CARGO_CAPACITY_BONUS);
    }

    /**
     * Calculates the effective cargo capacity for a ship with career bonuses applied.
     *
     * @param baseCapacity the base cargo capacity in tons
     * @param player the player
     * @return the effective cargo capacity in tons
     */
    public int calculateEffectiveCargoCapacity(int baseCapacity, Player player) {
        double bonusPercent = calculateCargoCapacityBonus(player);
        return (int) Math.round(baseCapacity * (1.0 + bonusPercent / 100.0));
    }

    // ============================================================
    // Crew Requirements
    // ============================================================

    /**
     * Checks if a player has sufficient crew skills for a trade mission.
     * <p>
     * Trade missions require:
     * </p>
     * <ul>
     *   <li>Navigation skill to plot the course</li>
     *   <li>Pilot skill to fly the ship</li>
     *   <li>Steward skill to manage cargo</li>
     * </ul>
     *
     * @param player the player
     * @param mission the trade mission
     * @return true if crew requirements are met
     */
    public boolean hasSufficientCrewForTrade(Player player, TradeMission mission) {
        if (player == null || !player.hasCommander()) {
            return true; // No commander check if no commander assigned
        }

        TravellerCharacter commander = player.getCommander();

        // Check for basic navigation and pilot skills
        int navLevel = getSkillLevel(commander, "Navigation");
        int pilotLevel = getSkillLevel(commander, "Pilot");

        // At least one of navigation or pilot should be present
        return navLevel > 0 || pilotLevel > 0 || hasMerchantCareer(commander);
    }

    /**
     * Checks if a character has served in the Merchant career.
     *
     * @param character the character
     * @return true if the character has Merchant career tours
     */
    public boolean hasMerchantCareer(TravellerCharacter character) {
        if (character == null) {
            return false;
        }
        return character.getCareerTours().stream()
                .anyMatch(tour -> tour.getCareer() == Career.MERCHANT);
    }

    /**
     * Calculates the crew skill modifier for a trade mission.
     * <p>
     * Higher crew skills improve trade outcomes:
     * </p>
     * <ul>
     *   <li>Navigation: reduces travel time and fuel costs</li>
     *   <li>Pilot: improves evasion from pirates</li>
     *   <li>Electronics: improves market intelligence</li>
     *   <li>Steward: improves cargo management</li>
     * </ul>
     *
     * @param player the player
     * @return the crew skill modifier (1.0 = no bonus, 1.5 = 50% bonus)
     */
    public double calculateCrewSkillModifier(Player player) {
        if (player == null || !player.hasCommander()) {
            return 1.0;
        }

        TravellerCharacter commander = player.getCommander();

        double modifier = 1.0;
        modifier += getSkillLevel(commander, "Navigation") * 0.05;
        modifier += getSkillLevel(commander, "Pilot") * 0.05;
        modifier += getSkillLevel(commander, "Electronics") * 0.03;
        modifier += getSkillLevel(commander, "Steward") * 0.03;

        return Math.min(modifier, 2.0); // Cap at 2x
    }

    // ============================================================
    // Career Progression
    // ============================================================

    /**
     * Records trade earnings for a player and checks for career advancement.
     * <p>
     * When a player earns enough credits from trade, their Merchant career
     * can advance, providing better bonuses.
     * </p>
     *
     * @param player the player
     * @param credits the credits earned from trade
     * @return true if the career advanced
     */
    public boolean recordTradeEarnings(Player player, long credits) {
        if (player == null || credits <= 0) {
            return false;
        }

        int playerId = player.getId();
        playerTradeEarnings.merge(playerId, credits, Long::sum);
        playerTradeMissions.merge(playerId, 1, Integer::sum);

        // Check for career advancement
        long totalEarnings = playerTradeEarnings.get(playerId);
        if (totalEarnings >= CAREER_ADVANCEMENT_THRESHOLD) {
            // Reset earnings counter for next advancement
            playerTradeEarnings.put(playerId, 0L);
            return true;
        }

        return false;
    }

    /**
     * Gets the total trade earnings for a player.
     *
     * @param playerId the player ID
     * @return the total credits earned from trade
     */
    public long getTradeEarnings(int playerId) {
        return playerTradeEarnings.getOrDefault(playerId, 0L);
    }

    /**
     * Gets the number of trade missions completed by a player.
     *
     * @param playerId the player ID
     * @return the number of completed trade missions
     */
    public int getTradeMissionCount(int playerId) {
        return playerTradeMissions.getOrDefault(playerId, 0);
    }

    // ============================================================
    // Trade Mission Enhancement
    // ============================================================

    /**
     * Enhances a trade result with career bonuses.
     * <p>
     * Applies all career and skill bonuses to a trade result:
     * </p>
     * <ul>
     *   <li>Trade profit bonus from career and skills</li>
     *   <li>Crew skill modifier</li>
     *   <li>Cargo capacity bonus</li>
     * </ul>
     *
     * @param player the player
     * @param baseProfit the base trade profit in credits
     * @return the enhanced profit with all bonuses applied
     */
    public long enhanceTradeProfit(Player player, long baseProfit) {
        if (player == null || baseProfit <= 0) {
            return baseProfit;
        }

        // Career and skill bonus
        double tradeBonus = calculateTradeProfitBonus(player);

        // Crew skill modifier
        double crewModifier = calculateCrewSkillModifier(player);

        // Combined bonus
        double totalMultiplier = (1.0 + tradeBonus / 100.0) * crewModifier;
        long enhancedProfit = (long) Math.round(baseProfit * totalMultiplier);

        return Math.max(baseProfit, enhancedProfit); // Never reduce profit
    }

    /**
     * Calculates the trade route discovery bonus from Electronics skill.
     * <p>
     * Higher Electronics skill allows discovering more profitable trade routes.
     * </p>
     *
     * @param player the player
     * @return the number of additional routes that can be discovered
     */
    public int getTradeRouteDiscoveryBonus(Player player) {
        if (player == null || !player.hasCommander()) {
            return 0;
        }

        TravellerCharacter commander = player.getCommander();
        int electronicsLevel = getSkillLevel(commander, "Electronics");
        int navigationLevel = getSkillLevel(commander, "Navigation");

        return (int) (electronicsLevel * ELECTRONICS_ROUTE_BONUS_PER_LEVEL + navigationLevel * 0.5);
    }

    // ============================================================
    // Summary & Reporting
    // ============================================================

    /**
     * Returns a summary of the trade career state for a player.
     *
     * @param player the player
     * @return a summary map with career stats and bonuses
     */
    public Map<String, Object> getCareerSummary(Player player) {
        Map<String, Object> summary = new HashMap<>();

        if (player == null) {
            summary.put("error", "Player is null");
            return summary;
        }

        summary.put("playerId", player.getId());
        summary.put("hasCommander", player.hasCommander());

        if (player.hasCommander()) {
            TravellerCharacter commander = player.getCommander();
            summary.put("commanderName", commander.getName());
            summary.put("primaryCareer", commander.getPrimaryCareer() != null ? commander.getPrimaryCareer().name() : "None");
            summary.put("hasMerchantCareer", hasMerchantCareer(commander));

            long merchantTours = commander.getCareerTours().stream()
                    .filter(tour -> tour.getCareer() == Career.MERCHANT)
                    .count();
            summary.put("merchantTours", merchantTours);

            summary.put("tradeProfitBonus", calculateTradeProfitBonus(player));
            summary.put("cargoCapacityBonus", calculateCargoCapacityBonus(player));
            summary.put("crewSkillModifier", calculateCrewSkillModifier(player));
            summary.put("tradeRouteDiscoveryBonus", getTradeRouteDiscoveryBonus(player));
        }

        summary.put("tradeEarnings", getTradeEarnings(player.getId()));
        summary.put("tradeMissionsCompleted", getTradeMissionCount(player.getId()));

        return summary;
    }
}
