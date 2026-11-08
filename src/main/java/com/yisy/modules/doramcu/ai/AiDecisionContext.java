package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.List;
import java.util.Map;

/**
 * Context object that provides the AI with the information it needs to make a decision.
 * <p>
 * Includes owned and enemy fleets, resource counts, and references to the player
 * and game state.
 * </p>
 */
public class AiDecisionContext {

    private final Player player;
    private final Game game;
    private final List<Fleet> ownedFleets;
    private final List<Fleet> enemyFleets;
    private final int minerals;
    private final int energy;
    private final int food;
    private final int credits;
    private final List<SpaceStation> stations;
    private final Map<String, Integer> resources;

    private AiDecisionContext(Builder builder) {
        this.player = builder.player;
        this.game = builder.game;
        this.ownedFleets = builder.ownedFleets;
        this.enemyFleets = builder.enemyFleets;
        this.minerals = builder.minerals;
        this.energy = builder.energy;
        this.food = builder.food;
        this.credits = builder.credits;
        this.stations = builder.stations != null ? builder.stations : List.of();
        this.resources = builder.resources != null ? builder.resources : Map.of();
    }

    /**
     * Returns a builder for creating an AiDecisionContext.
     */
    public static Builder builder() {
        return new Builder();
    }

    // --- Accessors ---

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    public List<Fleet> getOwnedFleets() {
        return ownedFleets;
    }

    public List<Fleet> getEnemyFleets() {
        return enemyFleets;
    }

    public int getMinerals() {
        return minerals;
    }

    public int getEnergy() {
        return energy;
    }

    public int getFood() {
        return food;
    }

    public int getCredits() {
        return credits;
    }

    public List<SpaceStation> getStations() {
        return stations;
    }

    public Map<String, Integer> getResources() {
        return resources;
    }

    /**
     * Builder for {@link AiDecisionContext}.
     */
    public static class Builder {
        private Player player;
        private Game game;
        private List<Fleet> ownedFleets;
        private List<Fleet> enemyFleets;
        private int minerals;
        private int energy;
        private int food;
        private int credits;
        private List<SpaceStation> stations;
        private Map<String, Integer> resources;

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder game(Game game) {
            this.game = game;
            return this;
        }

        public Builder ownedFleets(List<Fleet> ownedFleets) {
            this.ownedFleets = ownedFleets;
            return this;
        }

        public Builder enemyFleets(List<Fleet> enemyFleets) {
            this.enemyFleets = enemyFleets;
            return this;
        }

        public Builder minerals(int minerals) {
            this.minerals = minerals;
            return this;
        }

        public Builder energy(int energy) {
            this.energy = energy;
            return this;
        }

        public Builder food(int food) {
            this.food = food;
            return this;
        }

        public Builder credits(int credits) {
            this.credits = credits;
            return this;
        }

        public Builder stations(List<SpaceStation> stations) {
            this.stations = stations;
            return this;
        }

        public Builder resources(Map<String, Integer> resources) {
            this.resources = resources;
            return this;
        }

        public AiDecisionContext build() {
            return new AiDecisionContext(this);
        }
    }

    @Override
    public String toString() {
        return "AiDecisionContext{" +
                "player=" + (player != null ? player.getEmpireName() : "null") +
                ", ownedFleets=" + ownedFleets.size() +
                ", enemyFleets=" + enemyFleets.size() +
                ", minerals=" + minerals +
                ", energy=" + energy +
                '}';
    }
}
