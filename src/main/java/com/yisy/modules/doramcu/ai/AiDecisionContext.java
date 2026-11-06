package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context object passed to AI decision methods to provide the current game state.
 * <p>
 * This class encapsulates all the information an AI core needs to make a decision,
 * including the owning player, the game state, and any tactical information about
 * fleets and enemies.
 * </p>
 */
public class AiDecisionContext {

    private final Player player;
    private final Game game;
    private final int currentTurn;
    private final List<Fleet> ownedFleets;
    private final List<Fleet> enemyFleets;
    private final List<SpaceStation> stations;
    private final int minerals;
    private final int energy;
    private final int food;
    private final int credits;

    private AiDecisionContext(Builder builder) {
        this.player = builder.player;
        this.game = builder.game;
        this.currentTurn = builder.currentTurn;
        this.ownedFleets = builder.ownedFleets;
        this.enemyFleets = builder.enemyFleets;
        this.stations = builder.stations;
        this.minerals = builder.minerals;
        this.energy = builder.energy;
        this.food = builder.food;
        this.credits = builder.credits;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    public int getCurrentTurn() {
        return currentTurn;
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

    /**
     * Returns the list of space stations owned by the player.
     *
     * @return list of space stations, or empty list if none
     */
    public List<SpaceStation> getStations() {
        return stations;
    }

    /**
     * Returns all resources as a map for compatibility with EmpireFeatureExtractor.
     *
     * @return unmodifiable map of resource name to value
     */
    public Map<String, Integer> getResources() {
        Map<String, Integer> map = new HashMap<>();
        map.put("minerals", minerals);
        map.put("energy", energy);
        map.put("food", food);
        map.put("credits", credits);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates a new builder for AiDecisionContext.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for AiDecisionContext.
     */
    public static class Builder {
        private Player player;
        private Game game;
        private int currentTurn;
        private List<Fleet> ownedFleets;
        private List<Fleet> enemyFleets;
        private List<SpaceStation> stations;
        private int minerals;
        private int energy;
        private int food;
        private int credits;

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder game(Game game) {
            this.game = game;
            return this;
        }

        public Builder currentTurn(int currentTurn) {
            this.currentTurn = currentTurn;
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

        public Builder stations(List<SpaceStation> stations) {
            this.stations = stations;
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

        public AiDecisionContext build() {
            return new AiDecisionContext(this);
        }
    }
}
