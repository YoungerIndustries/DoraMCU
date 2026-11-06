package com.yisy.modules.doramcu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for AI difficulty levels.
 * <p>
 * Externalizes the magic numbers used by {@code AiPlayerService} so that
 * game balance can be tuned without code changes.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "ai.difficulty")
public class AiDifficultyConfig {

    private final Easy easy = new Easy();
    private final Medium medium = new Medium();
    private final Hard hard = new Hard();

    /** Returns the easy difficulty parameters. */
    public Easy getEasy() {
        return easy;
    }

    /** Returns the medium difficulty parameters. */
    public Medium getMedium() {
        return medium;
    }

    /** Returns the hard difficulty parameters. */
    public Hard getHard() {
        return hard;
    }

    /** Easy difficulty AI parameters. */
    public static class Easy {
        /** Minimum minerals required to build a ship. */
        private int buildMinerals = 150;
        /** Minimum energy required to build a ship. */
        private int buildEnergy = 80;

        public int getBuildMinerals() {
            return buildMinerals;
        }

        public void setBuildMinerals(int buildMinerals) {
            this.buildMinerals = buildMinerals;
        }

        public int getBuildEnergy() {
            return buildEnergy;
        }

        public void setBuildEnergy(int buildEnergy) {
            this.buildEnergy = buildEnergy;
        }
    }

    /** Medium difficulty AI parameters. */
    public static class Medium {
        /** Minimum minerals required to build a ship. */
        private int buildMinerals = 100;
        /** Minimum energy required to build a ship. */
        private int buildEnergy = 50;
        /** Minimum minerals to build a cruiser. */
        private int cruiserMinerals = 200;
        /** Minimum energy to build a cruiser. */
        private int cruiserEnergy = 100;
        /** Minimum minerals to build a destroyer. */
        private int destroyerMinerals = 100;
        /** Minimum energy to build a destroyer. */
        private int destroyerEnergy = 50;

        public int getBuildMinerals() {
            return buildMinerals;
        }

        public void setBuildMinerals(int buildMinerals) {
            this.buildMinerals = buildMinerals;
        }

        public int getBuildEnergy() {
            return buildEnergy;
        }

        public void setBuildEnergy(int buildEnergy) {
            this.buildEnergy = buildEnergy;
        }

        public int getCruiserMinerals() {
            return cruiserMinerals;
        }

        public void setCruiserMinerals(int cruiserMinerals) {
            this.cruiserMinerals = cruiserMinerals;
        }

        public int getCruiserEnergy() {
            return cruiserEnergy;
        }

        public void setCruiserEnergy(int cruiserEnergy) {
            this.cruiserEnergy = cruiserEnergy;
        }

        public int getDestroyerMinerals() {
            return destroyerMinerals;
        }

        public void setDestroyerMinerals(int destroyerMinerals) {
            this.destroyerMinerals = destroyerMinerals;
        }

        public int getDestroyerEnergy() {
            return destroyerEnergy;
        }

        public void setDestroyerEnergy(int destroyerEnergy) {
            this.destroyerEnergy = destroyerEnergy;
        }
    }

    /** Hard difficulty AI parameters. */
    public static class Hard {
        /** Minimum minerals required to build a ship. */
        private int buildMinerals = 80;
        /** Minimum energy required to build a ship. */
        private int buildEnergy = 40;
        /** Maximum ships to build per turn. */
        private int maxShipsPerTurn = 3;
        /** Minimum minerals to build a battleship. */
        private int battleshipMinerals = 300;
        /** Minimum energy to build a battleship. */
        private int battleshipEnergy = 150;
        /** Minimum minerals to build a cruiser. */
        private int cruiserMinerals = 200;
        /** Minimum energy to build a cruiser. */
        private int cruiserEnergy = 100;
        /** Minimum minerals to build a destroyer. */
        private int destroyerMinerals = 100;
        /** Minimum energy to build a destroyer. */
        private int destroyerEnergy = 50;

        public int getBuildMinerals() {
            return buildMinerals;
        }

        public void setBuildMinerals(int buildMinerals) {
            this.buildMinerals = buildMinerals;
        }

        public int getBuildEnergy() {
            return buildEnergy;
        }

        public void setBuildEnergy(int buildEnergy) {
            this.buildEnergy = buildEnergy;
        }

        public int getMaxShipsPerTurn() {
            return maxShipsPerTurn;
        }

        public void setMaxShipsPerTurn(int maxShipsPerTurn) {
            this.maxShipsPerTurn = maxShipsPerTurn;
        }

        public int getBattleshipMinerals() {
            return battleshipMinerals;
        }

        public void setBattleshipMinerals(int battleshipMinerals) {
            this.battleshipMinerals = battleshipMinerals;
        }

        public int getBattleshipEnergy() {
            return battleshipEnergy;
        }

        public void setBattleshipEnergy(int battleshipEnergy) {
            this.battleshipEnergy = battleshipEnergy;
        }

        public int getCruiserMinerals() {
            return cruiserMinerals;
        }

        public void setCruiserMinerals(int cruiserMinerals) {
            this.cruiserMinerals = cruiserMinerals;
        }

        public int getCruiserEnergy() {
            return cruiserEnergy;
        }

        public void setCruiserEnergy(int cruiserEnergy) {
            this.cruiserEnergy = cruiserEnergy;
        }

        public int getDestroyerMinerals() {
            return destroyerMinerals;
        }

        public void setDestroyerMinerals(int destroyerMinerals) {
            this.destroyerMinerals = destroyerMinerals;
        }

        public int getDestroyerEnergy() {
            return destroyerEnergy;
        }

        public void setDestroyerEnergy(int destroyerEnergy) {
            this.destroyerEnergy = destroyerEnergy;
        }
    }
}
