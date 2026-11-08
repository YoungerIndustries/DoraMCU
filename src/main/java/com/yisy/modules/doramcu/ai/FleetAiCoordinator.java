package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.AiCoreComponent;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Ship;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Coordinates AI Core components across ships in a fleet.
 * <p>
 * Provides formation bonuses, evasion coordination, and command damage bonuses
 * based on the number and tier of AI Cores in the fleet.
 * </p>
 */
public class FleetAiCoordinator {

    private final Fleet fleet;

    /**
     * Creates a new coordinator for the given fleet.
     *
     * @param fleet the fleet to coordinate
     */
    public FleetAiCoordinator(Fleet fleet) {
        this.fleet = fleet;
    }

    /**
     * Returns the best (highest tier) AI Core in the fleet.
     *
     * @return the best AI Core, or null if no ships have AI Cores
     */
    public AiCoreComponent getBestAiCore() {
        return fleet.getShips().stream()
                .map(Ship::getAiCore)
                .filter(core -> core != null)
                .max(Comparator.comparingInt(c -> c.getTier().ordinal()))
                .orElse(null);
    }

    /**
     * Returns the number of ships with AI Cores in the fleet.
     *
     * @return count of ships with AI Cores
     */
    public int getAiCoreShipCount() {
        return (int) fleet.getShips().stream()
                .map(Ship::getAiCore)
                .filter(core -> core != null)
                .count();
    }

    /**
     * Calculates the formation bonus multiplier.
     * <p>
     * More AI Core ships in formation provide a higher bonus, capped at 1.3.
     * </p>
     *
     * @return the formation bonus multiplier (≥ 1.0, capped at 1.3)
     */
    public double getFormationBonus() {
        int count = getAiCoreShipCount();
        if (count == 0) {
            return 1.0;
        }
        // Each AI Core ship adds 0.03 bonus, capped at 1.3
        double bonus = 1.0 + count * 0.03;
        return Math.min(bonus, 1.3);
    }

    /**
     * Calculates the coordination evasion bonus.
     * <p>
     * AI Cores coordinate to improve fleet-wide evasion.
     * </p>
     *
     * @return the evasion bonus (≥ 0.0)
     */
    public double getCoordinationEvasionBonus() {
        return fleet.getShips().stream()
                .map(Ship::getAiCore)
                .filter(core -> core != null)
                .mapToDouble(AiCoreComponent::getEvasionBonus)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates the command damage bonus.
     * <p>
     * The best AI Core provides a command bonus to all fleet damage.
     * </p>
     *
     * @return the damage bonus (≥ 0.0)
     */
    public double getCommandDamageBonus() {
        AiCoreComponent best = getBestAiCore();
        return best != null ? best.getDamageBonus() : 0.0;
    }
}
