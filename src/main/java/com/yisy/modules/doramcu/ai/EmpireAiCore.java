package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Base AI core for an empire, providing personality-driven decision making.
 * <p>
 * Each empire has its own EmpireAiCore instance whose behavior is shaped by the
 * empire's {@link AiPersonality} (derived from GovernmentType and Ethos) and its
 * research level. The core delegates to {@link TacticalAiCore} for ship/fleet-level
 * decisions and {@link StrategicAiCore} for empire-level strategy.
 * </p>
 * <p>
 * This is the central entry point for all AI decisions. The {@link #decide(AiDecisionContext)}
 * method evaluates the current game state and returns an {@link AiDecision} that
 * AiPlayerService can execute.
 * </p>
 */
public class EmpireAiCore {

    private static final Logger log = LoggerFactory.getLogger(EmpireAiCore.class);

    private final int id;
    private final Player player;
    private final AiPersonality personality;
    private int researchLevel;

    private final TacticalAiCore tacticalCore;
    private final StrategicAiCore strategicCore;

    /**
     * Creates a new EmpireAiCore for the given player.
     *
     * @param id unique identifier for this AI core
     * @param player the player this AI controls
     * @param researchLevel the empire's AI research level (0-10)
     */
    public EmpireAiCore(int id, Player player, int researchLevel) {
        this.id = id;
        this.player = player;
        this.personality = AiPersonality.fromPlayer(player);
        this.researchLevel = Math.max(0, Math.min(10, researchLevel));
        this.tacticalCore = new TacticalAiCore(this);
        this.strategicCore = new StrategicAiCore(this);
        log.info("Created EmpireAiCore #{} for player '{}' with personality: {}",
                id, player.getEmpireName(), personality.getLabel());
    }

    /**
     * Makes a decision based on the current game state.
     * <p>
     * This is the main entry point. It first checks for strategic priorities
     * (expansion, economy, research) and then evaluates tactical options
     * (combat, movement, scouting).
     * </p>
     *
     * @param context the current decision context
     * @return the AI's chosen decision
     */
    public AiDecision decide(AiDecisionContext context) {
        if (context == null || context.getOwnedFleets().isEmpty()) {
            return AiDecision.wait("No fleets available");
        }

        // Strategic layer: check if we should prioritize non-combat actions
        AiDecision strategicDecision = strategicCore.decide(context);
        if (strategicDecision != null) {
            log.debug("Strategic decision: {}", strategicDecision);
            return strategicDecision;
        }

        // Tactical layer: evaluate combat and movement options
        AiDecision tacticalDecision = tacticalCore.decide(context);
        log.debug("Tactical decision: {}", tacticalDecision);
        return tacticalDecision;
    }

    /**
     * Evaluates how desirable a given action is for this AI's personality.
     *
     * @param actionType the action to evaluate
     * @param context the current context
     * @return a score from 0.0 (undesirable) to 1.0 (highly desirable)
     */
    public double evaluateAction(AiDecision.ActionType actionType, AiDecisionContext context) {
        double baseScore = switch (actionType) {
            case ENGAGE_COMBAT -> personality.getAggression();
            case MOVE_FLEET, SCOUT -> personality.getExpansionDrive();
            case BUILD_SHIP -> (personality.getEconomicFocus() + personality.getAggression()) / 2.0;
            case BUILD_STATION -> (personality.getEconomicFocus() + (1.0 - personality.getAggression())) / 2.0;
            case UPGRADE_STATION -> personality.getScientificFocus();
            case RETREAT -> 1.0 - personality.getRiskTolerance();
            case FORTIFY -> 1.0 - personality.getAggression();
            case WAIT -> 1.0 - personality.getRiskTolerance();
        };

        // Research level bonus: higher research = better decisions
        double researchBonus = researchLevel * 0.02;
        return Math.min(1.0, baseScore + researchBonus);
    }

    /**
     * Finds the nearest enemy fleet to any of the player's fleets.
     */
    public Fleet findNearestEnemy(AiDecisionContext context) {
        Fleet nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Fleet enemy : context.getEnemyFleets()) {
            if (enemy.getShips().isEmpty() || enemy.getPosition() == null) {
                continue;
            }
            for (Fleet friendly : context.getOwnedFleets()) {
                if (friendly.getPosition() == null) {
                    continue;
                }
                int dist = hexDistance(friendly.getPosition(), enemy.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = enemy;
                }
            }
        }
        return nearest;
    }

    /**
     * Selects the best fleet for an action based on ship count and position.
     */
    public Fleet selectBestFleet(AiDecisionContext context) {
        List<Fleet> fleets = context.getOwnedFleets();
        if (fleets.isEmpty()) {
            return null;
        }

        return fleets.stream()
                .filter(f -> !f.getShips().isEmpty())
                .max(Comparator.comparingInt(f -> f.getShips().size()))
                .orElse(fleets.get(0));
    }

    /**
     * Chooses a ship type based on resources and personality.
     */
    public ShipType chooseShipType(AiDecisionContext context) {
        int minerals = context.getMinerals();
        int energy = context.getEnergy();

        // Aggressive personalities prefer larger combat ships
        if (personality.getAggression() > 0.6) {
            if (minerals >= 50 && energy >= 30) {
                return ShipType.CRUISER;
            }
            if (minerals >= 30 && energy >= 20) {
                return ShipType.DESTROYER;
            }
            return ShipType.FRIGATE;
        }

        // Economic personalities prefer cheaper ships
        if (personality.getEconomicFocus() > 0.6) {
            return ShipType.FRIGATE;
        }

        // Default: build what we can afford
        if (minerals >= 40 && energy >= 25) {
            return ShipType.DESTROYER;
        }
        return ShipType.FRIGATE;
    }

    /**
     * Maps an AiDecision.ActionType to the corresponding ActionReward.Action
     * for the neural network's expanded action space.
     *
     * @param actionType the AI decision action type
     * @return the corresponding neural network action
     */
    public static com.yisy.modules.doradl.model.ActionReward.Action toDlAction(AiDecision.ActionType actionType) {
        if (actionType == null) {
            return com.yisy.modules.doradl.model.ActionReward.Action.WAIT;
        }
        return switch (actionType) {
            case MOVE_FLEET -> com.yisy.modules.doradl.model.ActionReward.Action.MOVE_FLEET;
            case ENGAGE_COMBAT -> com.yisy.modules.doradl.model.ActionReward.Action.ENGAGE_COMBAT;
            case BUILD_SHIP -> com.yisy.modules.doradl.model.ActionReward.Action.BUILD_SHIP;
            case BUILD_STATION -> com.yisy.modules.doradl.model.ActionReward.Action.BUILD_STATION;
            case UPGRADE_STATION -> com.yisy.modules.doradl.model.ActionReward.Action.UPGRADE_STATION;
            case RETREAT -> com.yisy.modules.doradl.model.ActionReward.Action.RETREAT;
            case SCOUT -> com.yisy.modules.doradl.model.ActionReward.Action.SCOUT_AREA;
            case FORTIFY -> com.yisy.modules.doradl.model.ActionReward.Action.FORTIFY_PLANET;
            case WAIT -> com.yisy.modules.doradl.model.ActionReward.Action.WAIT;
        };
    }

    /**
     * Maps an ActionReward.Action from the neural network back to AiDecision.ActionType.
     *
     * @param dlAction the neural network action
     * @return the corresponding AI decision action type
     */
    public static AiDecision.ActionType fromDlAction(com.yisy.modules.doradl.model.ActionReward.Action dlAction) {
        if (dlAction == null) {
            return AiDecision.ActionType.WAIT;
        }
        return switch (dlAction) {
            case MOVE_FLEET -> AiDecision.ActionType.MOVE_FLEET;
            case ENGAGE_COMBAT -> AiDecision.ActionType.ENGAGE_COMBAT;
            case BUILD_SHIP -> AiDecision.ActionType.BUILD_SHIP;
            case BUILD_STATION -> AiDecision.ActionType.BUILD_STATION;
            case UPGRADE_STATION -> AiDecision.ActionType.UPGRADE_STATION;
            case RETREAT -> AiDecision.ActionType.RETREAT;
            case SCOUT_AREA -> AiDecision.ActionType.SCOUT;
            case FORTIFY_PLANET -> AiDecision.ActionType.FORTIFY;
            case WAIT -> AiDecision.ActionType.WAIT;
            // Actions without direct AiDecision equivalents map to WAIT
            case EXPAND_COLONY, RESEARCH_TECH, DIPLOMATIC_STANCE, FLANK_ENEMY, AMBUSH,
                 OPTIMIZE_MINING, OPTIMIZE_TRADE, REPAIR_FLEET -> AiDecision.ActionType.WAIT;
        };
    }

    /**
     * Calculates hex distance between two positions.
     */
    private int hexDistance(Hex3D a, Hex3D b) {
        return (Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ())) / 2;
    }

    /**
     * Checks if two hex positions are adjacent.
     */
    private boolean isAdjacent(Hex3D a, Hex3D b) {
        if (a == null || b == null) {
            return false;
        }
        return hexDistance(a, b) <= 1;
    }

    // --- Accessors ---

    public int getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public AiPersonality getPersonality() {
        return personality;
    }

    public int getResearchLevel() {
        return researchLevel;
    }

    public void setResearchLevel(int researchLevel) {
        this.researchLevel = Math.max(0, Math.min(10, researchLevel));
    }

    public TacticalAiCore getTacticalCore() {
        return tacticalCore;
    }

    public StrategicAiCore getStrategicCore() {
        return strategicCore;
    }

    @Override
    public String toString() {
        return "EmpireAiCore{" +
                "id=" + id +
                ", player='" + player.getEmpireName() + '\'' +
                ", personality=" + personality.getLabel() +
                ", researchLevel=" + researchLevel +
                '}';
    }
}
