package com.yisy.modules.doramcu.hex;

import java.util.List;

/**
 * Coordinates AI Core capabilities at the fleet level.
 * <p>
 * When multiple ships in a fleet have AI Cores installed, the FleetAiCoordinator
 * determines the fleet's overall AI capability based on the best AI Core present.
 * The best AI Core acts as the fleet's tactical commander, providing bonuses to
 * all ships in the fleet.
 * </p>
 * <p>
 * Fleet-level AI bonuses:
 * <ul>
 *   <li><b>Formation bonus</b>: Increases damage output when multiple ships have AI Cores</li>
 *   <li><b>Coordination bonus</b>: Increases evasion when ships share AI Core data</li>
 *   <li><b>Command bonus</b>: The best AI Core provides tactical direction to the fleet</li>
 * </ul>
 * </p>
 */
public class FleetAiCoordinator {

    private final Fleet fleet;

    /**
     * Creates a coordinator for the given fleet.
     *
     * @param fleet the fleet to coordinate
     */
    public FleetAiCoordinator(Fleet fleet) {
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet must not be null");
        }
        this.fleet = fleet;
    }

    /**
     * Returns the best AI Core in the fleet (highest level).
     * <p>
     * The best AI Core acts as the fleet's tactical commander.
     * </p>
     *
     * @return the best AI Core, or null if no ships have AI Cores
     */
    public AiCoreComponent getBestAiCore() {
        List<Ship> activeShips = fleet.getActiveShips();
        AiCoreComponent best = null;

        for (Ship ship : activeShips) {
            AiCoreComponent core = ship.getAiCore();
            if (core != null) {
                if (best == null || core.getLevel() > best.getLevel()) {
                    best = core;
                }
            }
        }
        return best;
    }

    /**
     * Returns the number of ships with AI Cores installed.
     *
     * @return the count of ships with AI Cores
     */
    public int getAiCoreShipCount() {
        return (int) fleet.getActiveShips().stream()
                .filter(Ship::hasAiCore)
                .count();
    }

    /**
     * Returns the average AI Core level across all ships in the fleet.
     *
     * @return the average level (0.0 if no AI Cores)
     */
    public double getAverageAiCoreLevel() {
        List<Ship> activeShips = fleet.getActiveShips();
        if (activeShips.isEmpty()) return 0.0;

        double totalLevel = 0.0;
        int count = 0;
        for (Ship ship : activeShips) {
            if (ship.hasAiCore()) {
                totalLevel += ship.getAiCoreLevel();
                count++;
            }
        }
        return count > 0 ? totalLevel / count : 0.0;
    }

    /**
     * Returns the formation bonus multiplier for the fleet.
     * <p>
     * When multiple ships have AI Cores, they can coordinate formations,
     * increasing overall damage output. The bonus scales with the number
     * of AI Core-equipped ships and the best AI Core level.
     * </p>
     * <p>
     * Formula: 1.0 + (aiCoreShipCount * 0.02 * bestAiCoreLevel)
     * </p>
     *
     * @return the formation bonus multiplier (1.0 if no AI Cores)
     */
    public double getFormationBonus() {
        AiCoreComponent best = getBestAiCore();
        if (best == null) return 1.0;

        int aiCoreCount = getAiCoreShipCount();
        if (aiCoreCount <= 1) return 1.0;

        // Each additional AI Core ship provides a small bonus scaled by best core level
        double bonus = (aiCoreCount - 1) * 0.02 * best.getLevel();
        return 1.0 + Math.min(bonus, 0.30); // Cap at 30% bonus
    }

    /**
     * Returns the coordination evasion bonus for the fleet.
     * <p>
     * Ships with AI Cores can share sensor data and coordinate evasive maneuvers,
     * improving the fleet's overall evasion.
     * </p>
     * <p>
     * Formula: bestAiCoreEvasionBonus * (aiCoreShipCount / totalShipCount)
     * </p>
     *
     * @return the coordination evasion bonus (0.0 if no AI Cores)
     */
    public double getCoordinationEvasionBonus() {
        AiCoreComponent best = getBestAiCore();
        if (best == null) return 0.0;

        int totalShips = fleet.getActiveShipCount();
        int aiCoreCount = getAiCoreShipCount();
        if (totalShips == 0 || aiCoreCount == 0) return 0.0;

        // Coordination bonus scales with the proportion of AI Core ships
        double proportion = (double) aiCoreCount / totalShips;
        return best.getEvasionBonus() * proportion;
    }

    /**
     * Returns the command bonus for the fleet's effective damage.
     * <p>
     * The best AI Core provides tactical command, improving target priority
     * and firing solutions for the entire fleet.
     * </p>
     *
     * @return the command damage bonus (0.0 if no AI Cores)
     */
    public double getCommandDamageBonus() {
        AiCoreComponent best = getBestAiCore();
        if (best == null) return 0.0;

        return best.getDamageBonus();
    }

    /**
     * Returns the effective fleet damage including all AI Core bonuses.
     *
     * @return the effective fleet damage
     */
    public int getEffectiveFleetDamage() {
        int baseDamage = fleet.getEffectiveDamage();
        double formationBonus = getFormationBonus();
        double commandBonus = getCommandDamageBonus();

        double totalMultiplier = formationBonus + commandBonus;
        return (int) Math.round(baseDamage * (1.0 + totalMultiplier));
    }

    /**
     * Returns the effective fleet evasion including coordination bonus.
     *
     * @return the effective fleet evasion
     */
    public double getEffectiveFleetEvasion() {
        double baseEvasion = fleet.getEffectiveFleetEvasion();
        double coordinationBonus = getCoordinationEvasionBonus();

        return Math.min(1.0, baseEvasion + coordinationBonus);
    }

    /**
     * Returns a summary of the fleet's AI Core status.
     *
     * @return a descriptive string
     */
    public String getAiCoreSummary() {
        AiCoreComponent best = getBestAiCore();
        if (best == null) {
            return "No AI Cores installed";
        }
        return String.format("AI Core: %s (Level %d), %d/%d ships equipped",
                best.getTier(), best.getLevel(),
                getAiCoreShipCount(), fleet.getActiveShipCount());
    }
}
