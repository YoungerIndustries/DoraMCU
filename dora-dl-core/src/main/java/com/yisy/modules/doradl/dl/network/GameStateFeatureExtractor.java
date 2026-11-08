package com.yisy.modules.doradl.dl.network;

import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dto.PlayerSummary;
import com.yisy.modules.doradl.dto.SubSectorInfo;
import com.yisy.modules.doradl.model.GameStateFeatures;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Extracts feature vectors from the DoraMCU 4X game state for the neural network.
 * <p>
 * This is the local DL4J implementation for the 4X game type. When dl.rest-client.enabled=true,
 * feature extraction is handled by the Python microservice.
 * </p>
 * <p>
 * Implements {@link FeatureExtractor} to integrate with the pluggable feature extraction system,
 * allowing other game types (Traveller, Captain's Log) to provide their own extractors.
 * </p>
 * Thread-safe for concurrent game sessions.
 */
@Component
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "false", matchIfMissing = true)
public class GameStateFeatureExtractor implements FeatureExtractorRegistry.GameTypedExtractor {

    // Normalization constants
    static final int MAX_TURNS = 50;
    static final int MAX_PLAYERS = 8;
    static final int MAX_PLANETS = 100;
    static final int MAX_FLEETS = 50;
    static final int MAX_SHIPS = 500;
    static final int MAX_RESOURCES = 10000;
    static final int MAX_DISTANCE = 100;

    @Override
    public GameStateFeatures extract(Object gameStateObj, Long playerId) {
        if (gameStateObj == null) {
            throw new IllegalArgumentException("Game state cannot be null");
        }
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }
        GameStateResponse gameState;
        if (gameStateObj instanceof GameStateResponse) {
            gameState = (GameStateResponse) gameStateObj;
        } else {
            throw new IllegalArgumentException(
                "Expected GameStateResponse, got " + gameStateObj.getClass().getSimpleName());
        }

        GameStateFeatures features = new GameStateFeatures();
        PlayerSummary player = findPlayer(gameState.getPlayers(), playerId);
        if (player == null) {
            return features;
        }

        extractResources(features, player);
        extractAssets(features, player);
        extractFleetPosition(features, gameState, player);
        extractEnemyInfo(features, gameState, player);
        extractGameState(features, gameState, player);
        extractRelativeStrength(features, gameState, player);
        extractResourceTrends(features, player);
        extractStrategicContext(features, gameState, player);
        extractDifficulty(features, player);
        extractTurnPhase(features, gameState);

        return features;
    }

    private void extractResources(GameStateFeatures features, PlayerSummary player) {
        Map<String, Integer> resources = player.getResources();
        if (resources == null) return;
        double minerals = normalize(resources.getOrDefault("minerals", 0), MAX_RESOURCES);
        double energy = normalize(resources.getOrDefault("energy", 0), MAX_RESOURCES);
        double food = normalize(resources.getOrDefault("food", 0), MAX_RESOURCES);
        double research = normalize(resources.getOrDefault("research", 0), MAX_RESOURCES);
        double influence = normalize(resources.getOrDefault("influence", 0), MAX_RESOURCES);
        features.setResources(minerals, energy, food, research, influence);
    }

    private void extractAssets(GameStateFeatures features, PlayerSummary player) {
        double planetCount = normalize(player.getPlanetCount(), MAX_PLANETS);
        double fleetCount = normalize(player.getFleetCount(), MAX_FLEETS);
        double totalShips = estimateTotalShips(player);
        features.setAssets(planetCount, fleetCount, totalShips);
    }

    private void extractFleetPosition(GameStateFeatures features, GameStateResponse gameState, PlayerSummary player) {
        List<SubSectorInfo> subSectors = gameState.getSubSectors();
        if (subSectors == null || subSectors.isEmpty()) return;
        double x = 0.5, y = 0.5, z = 0.5;
        for (SubSectorInfo sub : subSectors) {
            if (sub.isGenerated() && sub.getSystems() != null) {
                for (Object system : sub.getSystems()) {
                    if (system instanceof Map) {
                        Map<?, ?> sysMap = (Map<?, ?>) system;
                        Object fleets = sysMap.get("fleets");
                        if (fleets instanceof List) {
                            for (Object fleet : (List<?>) fleets) {
                                if (fleet instanceof Map) {
                                    Map<?, ?> fleetMap = (Map<?, ?>) fleet;
                                    Object ownerId = fleetMap.get("ownerId");
                                    if (ownerId != null && ownerId.equals(player.getId())) {
                                        Object row = sysMap.get("sectorRow");
                                        Object col = sysMap.get("sectorCol");
                                        if (row != null && col != null) {
                                            x = normalize(((Number) row).intValue(), MAX_DISTANCE);
                                            y = normalize(((Number) col).intValue(), MAX_DISTANCE);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        features.setFleetPosition(x, y, z);
    }

    private void extractEnemyInfo(GameStateFeatures features, GameStateResponse gameState, PlayerSummary player) {
        List<PlayerSummary> players = gameState.getPlayers();
        if (players == null) return;
        int enemyFleetCount = 0;
        int enemyTotalShips = 0;
        for (PlayerSummary other : players) {
            if (other.getId().equals(player.getId())) continue;
            enemyFleetCount += other.getFleetCount();
            enemyTotalShips += estimateTotalShips(other);
        }
        features.setEnemyInfo(1.0, normalize(enemyFleetCount, MAX_FLEETS), normalize(enemyTotalShips, MAX_SHIPS));
    }

    private void extractGameState(GameStateFeatures features, GameStateResponse gameState, PlayerSummary player) {
        int currentTurn = gameState.getCurrentTurn();
        int playerCount = gameState.getPlayers() != null ? gameState.getPlayers().size() : 1;
        int rank = calculatePlayerRank(gameState, player);
        features.setGameState(normalize(currentTurn, MAX_TURNS), normalize(playerCount, MAX_PLAYERS), normalize(rank, MAX_PLAYERS));
    }

    private void extractRelativeStrength(GameStateFeatures features, GameStateResponse gameState, PlayerSummary player) {
        List<PlayerSummary> players = gameState.getPlayers();
        if (players == null || players.size() <= 1) {
            features.setRelativeStrength(1.0, 1.0);
            return;
        }
        int playerPower = calculatePlayerPower(player);
        int totalEnemyPower = 0;
        int enemyCount = 0;
        for (PlayerSummary other : players) {
            if (!other.getId().equals(player.getId())) {
                totalEnemyPower += calculatePlayerPower(other);
                enemyCount++;
            }
        }
        double avgEnemyPower = enemyCount > 0 ? (double) totalEnemyPower / enemyCount : 1;
        features.setRelativeStrength(playerPower / (avgEnemyPower + 1), playerPower / (Math.max(avgEnemyPower, 1) + 1));
    }

    private void extractResourceTrends(GameStateFeatures features, PlayerSummary player) {
        Map<String, Integer> resources = player.getResources();
        if (resources == null) return;
        int minerals = resources.getOrDefault("minerals", 0);
        int energy = resources.getOrDefault("energy", 0);
        features.setResourceTrends(minerals > MAX_RESOURCES * 0.5 ? 1.0 : 0.0, energy > MAX_RESOURCES * 0.5 ? 1.0 : 0.0);
    }

    private void extractStrategicContext(GameStateFeatures features, GameStateResponse gameState, PlayerSummary player) {
        List<PlayerSummary> players = gameState.getPlayers();
        boolean hasAdjacentEnemy = players != null && players.stream().anyMatch(p -> !p.getId().equals(player.getId()));
        Map<String, Integer> resources = player.getResources();
        boolean hasBuildableResources = resources != null && resources.getOrDefault("minerals", 0) >= 100 && resources.getOrDefault("energy", 0) >= 50;
        boolean hasIdleFleet = player.getFleetCount() > 0;
        features.setStrategicContext(hasAdjacentEnemy ? 1.0 : 0.0, hasBuildableResources ? 1.0 : 0.0, hasIdleFleet ? 1.0 : 0.0);
    }

    private void extractDifficulty(GameStateFeatures features, PlayerSummary player) {
        String difficulty = player.getDifficulty();
        double bias;
        if (difficulty == null) {
            bias = 0.5;
        } else {
            switch (difficulty.toUpperCase()) {
                case "EASY": bias = 0.0; break;
                case "MEDIUM": bias = 0.5; break;
                case "HARD": bias = 1.0; break;
                default: bias = 0.5; break;
            }
        }
        features.setDifficultyBias(bias);
    }

    private void extractTurnPhase(GameStateFeatures features, GameStateResponse gameState) {
        int turn = gameState.getCurrentTurn();
        boolean isEarly = turn < MAX_TURNS / 3;
        boolean isMid = turn >= MAX_TURNS / 3 && turn < (2 * MAX_TURNS) / 3;
        features.setTurnPhase(isEarly, isMid);
    }

    private PlayerSummary findPlayer(List<PlayerSummary> players, Long playerId) {
        if (players == null) return null;
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElse(null);
    }

    private double estimateTotalShips(PlayerSummary player) {
        Map<String, Integer> resources = player.getResources();
        if (resources == null) return player.getFleetCount();
        int minerals = resources.getOrDefault("minerals", 0);
        int energy = resources.getOrDefault("energy", 0);
        int builtShips = Math.min(minerals / 100, energy / 50);
        return Math.max(builtShips, player.getFleetCount());
    }

    private int calculatePlayerPower(PlayerSummary player) {
        Map<String, Integer> resources = player.getResources();
        int power = 0;
        if (resources != null) {
            power += resources.getOrDefault("minerals", 0);
            power += resources.getOrDefault("energy", 0) * 2;
            power += resources.getOrDefault("food", 0);
        }
        power += player.getPlanetCount() * 100;
        power += player.getFleetCount() * 50;
        return power;
    }

    private int calculatePlayerRank(GameStateResponse gameState, PlayerSummary player) {
        List<PlayerSummary> players = gameState.getPlayers();
        if (players == null) return 1;
        int playerPower = calculatePlayerPower(player);
        int rank = 1;
        for (PlayerSummary other : players) {
            if (calculatePlayerPower(other) > playerPower) rank++;
        }
        return rank;
    }

    private double normalize(int value, int max) {
        return Math.min(1.0, Math.max(0.0, (double) value / max));
    }

    // ---- FeatureExtractor interface implementation ----

    @Override
    public Class<?> getInputType() {
        return GameStateResponse.class;
    }

    @Override
    public FeatureExtractorRegistry.SupportedGameType getSupportedGameType() {
        return FeatureExtractorRegistry.SupportedGameType.DORAMCU_4X;
    }

    @Override
    public boolean isDefaultExtractor() {
        return true;
    }
}
