package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.SpaceStationType;

import java.util.List;

/**
 * Represents a decision made by the AI core.
 * <p>
 * Each decision includes an action type, optional parameters (such as target fleet,
 * destination, or ship type), and a confidence score indicating how certain the AI
 * is about this decision.
 * </p>
 */
public class AiDecision {

    /**
     * Types of actions the AI can decide to take.
     */
    public enum ActionType {
        /** Move a fleet to a new position */
        MOVE_FLEET,
        /** Engage in combat with an enemy fleet */
        ENGAGE_COMBAT,
        /** Build a new ship */
        BUILD_SHIP,
        /** Build a new space station */
        BUILD_STATION,
        /** Upgrade a station's AI Core */
        UPGRADE_STATION,
        /** Retreat from combat */
        RETREAT,
        /** Scout an area */
        SCOUT,
        /** Fortify a position */
        FORTIFY,
        /** Wait and hold position */
        WAIT
    }

    private final ActionType actionType;
    private final double confidence;
    private final String reasoning;

    // Optional parameters depending on action type
    private final Integer fleetId;
    private final Hex3D targetPosition;
    private final Integer targetEnemyFleetId;
    private final ShipType shipTypeToBuild;
    private final SpaceStationType stationTypeToBuild;
    private final Integer stationIdToUpgrade;

    private AiDecision(Builder builder) {
        this.actionType = builder.actionType;
        this.confidence = builder.confidence;
        this.reasoning = builder.reasoning;
        this.fleetId = builder.fleetId;
        this.targetPosition = builder.targetPosition;
        this.targetEnemyFleetId = builder.targetEnemyFleetId;
        this.shipTypeToBuild = builder.shipTypeToBuild;
        this.stationTypeToBuild = builder.stationTypeToBuild;
        this.stationIdToUpgrade = builder.stationIdToUpgrade;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public double getConfidence() {
        return confidence;
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

    /**
     * Returns the type of station to build.
     *
     * @return the station type, or null if not a BUILD_STATION action
     */
    public SpaceStationType getStationTypeToBuild() {
        return stationTypeToBuild;
    }

    /**
     * Returns the ID of the station to upgrade.
     *
     * @return the station ID, or null if not an UPGRADE_STATION action
     */
    public Integer getStationIdToUpgrade() {
        return stationIdToUpgrade;
    }

    /**
     * Creates a decision to move a fleet.
     */
    public static AiDecision moveFleet(Integer fleetId, Hex3D target, double confidence, String reasoning) {
        return builder()
                .actionType(ActionType.MOVE_FLEET)
                .fleetId(fleetId)
                .targetPosition(target)
                .confidence(confidence)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a decision to engage in combat.
     */
    public static AiDecision engageCombat(Integer fleetId, Integer enemyFleetId, double confidence, String reasoning) {
        return builder()
                .actionType(ActionType.ENGAGE_COMBAT)
                .fleetId(fleetId)
                .targetEnemyFleetId(enemyFleetId)
                .confidence(confidence)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a decision to build a ship.
     */
    public static AiDecision buildShip(ShipType shipType, double confidence, String reasoning) {
        return builder()
                .actionType(ActionType.BUILD_SHIP)
                .shipTypeToBuild(shipType)
                .confidence(confidence)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a wait decision.
     */
    public static AiDecision wait(String reasoning) {
        return builder()
                .actionType(ActionType.WAIT)
                .confidence(1.0)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a scout decision.
     */
    public static AiDecision scout(Integer fleetId, Hex3D target, double confidence, String reasoning) {
        return builder()
                .actionType(ActionType.SCOUT)
                .fleetId(fleetId)
                .targetPosition(target)
                .confidence(confidence)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a decision to build a space station.
     */
    public static AiDecision buildStation(SpaceStationType stationType, Hex3D position, double confidence, String reasoning) {
        return builder()
                .actionType(ActionType.BUILD_STATION)
                .stationTypeToBuild(stationType)
                .targetPosition(position)
                .confidence(confidence)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a decision to upgrade a station's AI Core.
     */
    public static AiDecision upgradeStation(Integer stationId, double confidence, String reasoning) {
        return builder()
                .actionType(ActionType.UPGRADE_STATION)
                .stationIdToUpgrade(stationId)
                .confidence(confidence)
                .reasoning(reasoning)
                .build();
    }

    /**
     * Creates a new builder for AiDecision.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AiDecision{" +
                "action=" + actionType +
                ", confidence=" + String.format("%.2f", confidence) +
                ", fleetId=" + fleetId +
                ", targetPos=" + targetPosition +
                ", enemyFleetId=" + targetEnemyFleetId +
                ", shipType=" + shipTypeToBuild +
                ", stationType=" + stationTypeToBuild +
                ", stationId=" + stationIdToUpgrade +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }

    /**
     * Builder for AiDecision.
     */
    public static class Builder {
        private ActionType actionType;
        private double confidence;
        private String reasoning;
        private Integer fleetId;
        private Hex3D targetPosition;
        private Integer targetEnemyFleetId;
        private ShipType shipTypeToBuild;
        private SpaceStationType stationTypeToBuild;
        private Integer stationIdToUpgrade;

        public Builder actionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder reasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }

        public Builder fleetId(Integer fleetId) {
            this.fleetId = fleetId;
            return this;
        }

        public Builder targetPosition(Hex3D targetPosition) {
            this.targetPosition = targetPosition;
            return this;
        }

        public Builder targetEnemyFleetId(Integer targetEnemyFleetId) {
            this.targetEnemyFleetId = targetEnemyFleetId;
            return this;
        }

        public Builder shipTypeToBuild(ShipType shipTypeToBuild) {
            this.shipTypeToBuild = shipTypeToBuild;
            return this;
        }

        public Builder stationTypeToBuild(SpaceStationType stationTypeToBuild) {
            this.stationTypeToBuild = stationTypeToBuild;
            return this;
        }

        public Builder stationIdToUpgrade(Integer stationIdToUpgrade) {
            this.stationIdToUpgrade = stationIdToUpgrade;
            return this;
        }

        public AiDecision build() {
            return new AiDecision(this);
        }
    }
}
