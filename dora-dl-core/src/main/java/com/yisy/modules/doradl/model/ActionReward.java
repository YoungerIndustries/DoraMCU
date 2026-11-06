package com.yisy.modules.doradl.model;

import java.util.Arrays;

/**
 * Represents a single training data point: a game state, the action taken, and the reward received.
 * Used for supervised learning of the strategy neural network.
 */
public class ActionReward {

    /**
     * Action types the neural network can predict.
     * Expanded from 5 to 18 actions to cover strategic, tactical, economic, and defensive categories.
     */
    public enum Action {
        // Strategic (Empire level)
        BUILD_SHIP(0),           // Build a ship
        BUILD_STATION(1),        // Build a space station
        EXPAND_COLONY(2),        // Colonize a new planet
        RESEARCH_TECH(3),        // Invest in research
        DIPLOMATIC_STANCE(4),    // Change diplomatic posture

        // Tactical (Fleet level)
        MOVE_FLEET(5),           // Move fleet to position
        ENGAGE_COMBAT(6),        // Attack enemy fleet
        RETREAT(7),              // Retreat from combat
        FLANK_ENEMY(8),          // Flanking maneuver
        AMBUSH(9),               // Set ambush position

        // Economic (Facility level)
        OPTIMIZE_MINING(10),     // Reallocate mining resources
        OPTIMIZE_TRADE(11),      // Adjust trade routes
        REPAIR_FLEET(12),        // Repair damaged ships
        UPGRADE_STATION(13),     // Upgrade station modules

        // Defensive
        FORTIFY_PLANET(14),      // Build planetary defenses
        SCOUT_AREA(15),          // Send scout fleet
        WAIT(16);                // Hold position

        private final int index;

        Action(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static Action fromIndex(int index) {
            for (Action action : values()) {
                if (action.index == index) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Unknown action index: " + index + " (valid range: 0-" + (values().length - 1) + ")");
        }

        /**
         * Returns the category of this action.
         */
        public ActionCategory getCategory() {
            if (index <= 4) return ActionCategory.STRATEGIC;
            if (index <= 9) return ActionCategory.TACTICAL;
            if (index <= 13) return ActionCategory.ECONOMIC;
            return ActionCategory.DEFENSIVE;
        }
    }

    /**
     * Categories of actions for grouping and analysis.
     */
    public enum ActionCategory {
        /** Empire-level strategic decisions */
        STRATEGIC,
        /** Ship and fleet-level tactical decisions */
        TACTICAL,
        /** Facility and resource management decisions */
        ECONOMIC,
        /** Defensive and scouting decisions */
        DEFENSIVE
    }

    private GameStateFeatures state;
    private Action action;
    private double reward;

    public ActionReward() {
    }

    public ActionReward(GameStateFeatures state, Action action, double reward) {
        this.state = state;
        this.action = action;
        this.reward = reward;
    }

    public GameStateFeatures getState() {
        return state;
    }

    public void setState(GameStateFeatures state) {
        this.state = state;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    /**
     * Converts the action to a one-hot encoded vector for neural network output.
     */
    public double[] toOneHot() {
        double[] oneHot = new double[Action.values().length];
        oneHot[action.getIndex()] = 1.0;
        return oneHot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionReward that = (ActionReward) o;
        return Double.compare(reward, that.reward) == 0 &&
                state.equals(that.state) &&
                action == that.action;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{state, action, reward});
    }

    @Override
    public String toString() {
        return "ActionReward{" +
                "action=" + action +
                ", reward=" + reward +
                '}';
    }
}
