package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.ShipType;

/**
 * Represents a decision made by the AI system.
 * <p>
 * Each decision includes an action type, optional target parameters, and reasoning
 * that explains why the action was chosen.
 * </p>
 */
public class AiDecision {

    /**
     * Types of actions the AI can decide to take.
     */
    public enum ActionType {
        MOVE_FLEET,
        ENGAGE_COMBAT,
        BUILD_SHIP,
        BUILD_STATION,
        UPGRADE_STATION,
        RETREAT,
        SCOUT,
        FORTIFY,
        WAIT
    }

    private final ActionType actionType;
    private final String reasoning;
    private Integer fleetId;
    private Hex3D targetPosition;
    private Integer targetEnemyFleetId;
    private ShipType shipTypeToBuild;

    private AiDecision(ActionType actionType, String reasoning) {
        this.actionType = actionType;
        this.reasoning = reasoning;
    }

    /**
     * Creates a WAIT decision with the given reasoning.
     */
    public static AiDecision wait(String reasoning) {
        return new AiDecision(ActionType.WAIT, reasoning);
    }

    /**
     * Creates a MOVE_FLEET decision.
     */
    public static AiDecision moveFleet(Integer fleetId, Hex3D targetPosition, String reasoning) {
        AiDecision decision = new AiDecision(ActionType.MOVE_FLEET, reasoning);
        decision.fleetId = fleetId;
        decision.targetPosition = targetPosition;
        return decision;
    }

    /**
     * Creates an ENGAGE_COMBAT decision.
     */
    public static AiDecision engageCombat(Integer fleetId, Integer targetEnemyFleetId, String reasoning) {
        AiDecision decision = new AiDecision(ActionType.ENGAGE_COMBAT, reasoning);
        decision.fleetId = fleetId;
        decision.targetEnemyFleetId = targetEnemyFleetId;
        return decision;
    }

    /**
     * Creates a BUILD_SHIP decision.
     */
    public static AiDecision buildShip(ShipType shipType, Integer fleetId, String reasoning) {
        AiDecision decision = new AiDecision(ActionType.BUILD_SHIP, reasoning);
        decision.shipTypeToBuild = shipType;
        decision.fleetId = fleetId;
        return decision;
    }

    /**
     * Creates a BUILD_STATION decision.
     */
    public static AiDecision buildStation(String reasoning) {
        return new AiDecision(ActionType.BUILD_STATION, reasoning);
    }

    /**
     * Creates a SCOUT decision.
     */
    public static AiDecision scout(Integer fleetId, Hex3D targetPosition, String reasoning) {
        AiDecision decision = new AiDecision(ActionType.SCOUT, reasoning);
        decision.fleetId = fleetId;
        decision.targetPosition = targetPosition;
        return decision;
    }

    /**
     * Creates a RETREAT decision.
     */
    public static AiDecision retreat(Integer fleetId, String reasoning) {
        AiDecision decision = new AiDecision(ActionType.RETREAT, reasoning);
        decision.fleetId = fleetId;
        return decision;
    }

    /**
     * Creates a FORTIFY decision.
     */
    public static AiDecision fortify(Integer fleetId, String reasoning) {
        AiDecision decision = new AiDecision(ActionType.FORTIFY, reasoning);
        decision.fleetId = fleetId;
        return decision;
    }

    // --- Accessors ---

    public ActionType getActionType() {
        return actionType;
    }

    public String getReasoning() {
        return reasoning;
    }

    public Integer getFleetId() {
        return fleetId;
    }

    public Hex3D getTargetPosition() {
        return targetPosition;
    }

    public Integer getTargetEnemyFleetId() {
        return targetEnemyFleetId;
    }

    public ShipType getShipTypeToBuild() {
        return shipTypeToBuild;
    }

    @Override
    public String toString() {
        return "AiDecision{" +
                "actionType=" + actionType +
                ", reasoning='" + reasoning + '\'' +
                ", fleetId=" + fleetId +
                ", targetPosition=" + targetPosition +
                '}';
    }
}
