package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Handles all game action operations within a session: fleet movement, combat,
 * building ships/stations/facilities, creating fleets, colonizing planets,
 * trading, and commander assignment.
 * <p>
 * Extracted from {@link GameSessionService} to separate mutable game actions
 * from session lifecycle and state queries. This component is not a standalone
 * Spring service — it is injected into GameSessionService which owns the
 * session map and provides access to it.
 * </p>
 */
@Component
public class SessionActionHandler {

    private static final Logger log = LoggerFactory.getLogger(SessionActionHandler.class);

    private final GameStateBuilder gameStateBuilder;
    private final CharacterService characterService;
    private final AiDelegationService aiDelegationService;

    public SessionActionHandler(GameStateBuilder gameStateBuilder,
                                CharacterService characterService,
                                @Lazy AiDelegationService aiDelegationService) {
        this.gameStateBuilder = gameStateBuilder;
        this.characterService = characterService;
        this.aiDelegationService = aiDelegationService;
    }

    /**
     * Finds the nearest enemy fleet to the given position that is within combat range.
     */
    Fleet findNearestEnemyFleet(Game game, Fleet movingFleet, Hex3D targetPosition, Player movingPlayer) {
        Fleet nearest = null;
        int nearestDistance = 3; // Combat range threshold

        for (Fleet other : game.getFleets().values()) {
            if (other.getId() == movingFleet.getId()) continue;
            if (other.isDestroyed()) continue;
            if (other.getOwner() == null) continue;
            if (other.getOwner().getId() == movingPlayer.getId()) continue;
            if (other.getPosition() == null) continue;

            int distance = targetPosition.distance(other.getPosition());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = other;
            }
        }
        return nearest;
    }

    // ---- Public action methods ----

    public GameStateResponse moveFleet(GameSession session, Long playerId, MoveFleetRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            Integer fleetId = request.getFleetId();
            if (!player.getOwnedFleetIds().contains(fleetId)) {
                log.warn("Player {} tried to move fleet {} which they don't own", playerId, fleetId);
                throw new IllegalStateException("Fleet not found or not owned");
            }

            Fleet fleet = game.getFleets().get(fleetId);
            if (fleet == null) {
                throw new IllegalStateException("Fleet not found");
            }

            Hex3D target = new Hex3D(request.getTargetRow(), request.getTargetCol(), 0);
            Integer targetSystemId = request.getSolarSystemId();

            if (targetSystemId == null) {
                Hex3D currentPos = fleet.getPosition();
                if (currentPos != null) {
                    int distance = currentPos.distance(target);
                    int mDriveRange = fleet.getEffectiveFleetMDriveSpeed();
                    if (distance > mDriveRange) {
                        log.warn("Player {} tried to move fleet {} beyond MDrive range: distance={}, range={}",
                                playerId, fleetId, distance, mDriveRange);
                        throw new IllegalStateException(
                                "Target is out of MDrive range: distance " + distance + " exceeds range " + mDriveRange);
                    }
                }
                fleet.setPosition(target);
                log.debug("Player {} moved fleet {} within system to ({}, {}) using MDrive",
                        playerId, fleetId, request.getTargetRow(), request.getTargetCol());
            } else {
                if (!game.getSolarSystems().containsKey(targetSystemId)) {
                    log.warn("Player {} tried to jump fleet {} to non-existent system {}",
                            playerId, fleetId, targetSystemId);
                    throw new IllegalStateException("Target solar system not found: " + targetSystemId);
                }

                int jDriveRange = fleet.getEffectiveFleetJDriveSpeed();
                if (jDriveRange <= 0) {
                    log.warn("Player {} tried to jump fleet {} but JDrive is not operational",
                            playerId, fleetId);
                    throw new IllegalStateException("JDrive is not operational — fleet health too low");
                }

                boolean hasHyperlane = false;
                for (Galaxy galaxy : game.getGalaxies().values()) {
                    for (Hyperlane hyperlane : galaxy.getAllHyperlanes()) {
                        if (hyperlane.connectsTo(targetSystemId)) {
                            hasHyperlane = true;
                            break;
                        }
                    }
                    if (hasHyperlane) break;
                }

                if (!hasHyperlane) {
                    log.warn("Player {} tried to jump fleet {} but no hyperlane connects to system {}",
                            playerId, fleetId, targetSystemId);
                    throw new IllegalStateException("No hyperlane connection to target system");
                }

                fleet.setPosition(target);
                log.debug("Player {} jumped fleet {} to system {} at ({}, {}) using JDrive",
                        playerId, fleetId, targetSystemId, request.getTargetRow(), request.getTargetCol());
            }

            // Check for enemy fleets at or near the destination
            Fleet nearestEnemy = findNearestEnemyFleet(game, fleet, target, player);
            if (nearestEnemy != null) {
                log.info("Auto-combat triggered: fleet {} moved into range of enemy fleet {}",
                        fleet.getName(), nearestEnemy.getName());
                Player defenderPlayer = nearestEnemy.getOwner();

                Planet defenderPlanet = null;
                if (defenderPlayer != null && nearestEnemy.getHomeSystem() != null) {
                    for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                        Planet planet = game.getPlanet(planetId);
                        if (planet != null) {
                            defenderPlanet = planet;
                            break;
                        }
                    }
                }

                Battle battle;
                if (defenderPlanet != null) {
                    battle = game.createBattle(fleet, nearestEnemy, target, defenderPlanet);
                } else {
                    battle = game.createBattle(fleet, nearestEnemy, target);
                }
                Battle.BattleResult result = battle.fightToCompletion();
                applyBattleResult(game, session, battle, result, player, defenderPlayer);
            }

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildShip(GameSession session, Long playerId, BuildShipRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            ShipType shipType;
            try {
                shipType = ShipType.valueOf(request.getShipType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ship type: " + request.getShipType());
            }

            Ship ship = new Ship(0, shipType);
            Integer targetFleetId = request.getTargetFleetId();
            if (targetFleetId != null) {
                if (!player.getOwnedFleetIds().contains(targetFleetId)) {
                    throw new IllegalStateException("Fleet not found or not owned");
                }
            }

            ResourcePipelineManager pipeline = game.getPipelineManager(player.getEmpireId());
            if (pipeline == null) {
                log.warn("Player {} has no resource pipeline initialized", player.getName());
                throw new IllegalStateException("Resource pipeline not initialized for player");
            }

            ShipOrder order = game.queueShipBuild(player, ship,
                    targetFleetId != null ? targetFleetId : 0, pipeline);
            if (order == null) {
                log.warn("Player {} cannot afford ship {} with current modules",
                        player.getName(), request.getShipType());
                throw new IllegalStateException("Insufficient construction modules for ship build");
            }

            log.info("Ship build queued for {} (pipeline modules): {}", player.getName(), request.getShipType());
            log.debug("Player {} queued build for ship type: {} in fleet: {}", playerId, request.getShipType(), targetFleetId);

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse engageCombat(GameSession session, Long playerId, EngageCombatRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            Integer attackerFleetId = request.getAttackerFleetId();
            if (!player.getOwnedFleetIds().contains(attackerFleetId)) {
                throw new IllegalStateException("Attacker fleet not found or not owned");
            }

            Fleet attacker = game.getFleets().get(attackerFleetId);
            Fleet defender = game.getFleets().get(request.getDefenderFleetId());
            if (attacker == null || defender == null) {
                throw new IllegalStateException("Fleet not found");
            }

            if (attacker.isDestroyed()) {
                throw new IllegalStateException("Attacker fleet is destroyed");
            }
            if (defender.isDestroyed()) {
                throw new IllegalStateException("Defender fleet is destroyed");
            }

            if (attacker.getPosition() != null && defender.getPosition() != null) {
                int distance = attacker.getPosition().distance(defender.getPosition());
                if (distance > 2) {
                    throw new IllegalStateException("Fleets are too far apart to engage: distance " + distance);
                }
            }

            Planet defenderPlanet = null;
            Player defenderPlayer = defender.getOwner();
            if (defenderPlayer != null && defender.getPosition() != null) {
                if (defender.getHomeSystem() != null) {
                    for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                        Planet planet = game.getPlanet(planetId);
                        if (planet != null) {
                            defenderPlanet = planet;
                            break;
                        }
                    }
                }
            }

            Battle battle;
            if (defenderPlanet != null) {
                battle = game.createBattle(attacker, defender, attacker.getPosition(), defenderPlanet);
                log.debug("Battle created with defender planet bonus: {}", defenderPlanet.getName());
            } else {
                battle = game.createBattle(attacker, defender, attacker.getPosition());
            }
            Battle.BattleResult result = battle.fightToCompletion();
            applyBattleResult(game, session, battle, result, player, defenderPlayer);

            log.info("Player {} engaged combat: fleet {} vs fleet {}, outcome: {}",
                    playerId, attackerFleetId, request.getDefenderFleetId(),
                    result != null ? result.getOutcome() : "null");

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse advanceTurn(GameSession session, Long playerId) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            game.nextTurn();
            // Advance global clock by one turn (one in-game day)
            com.yisy.modules.doramcu.unified.GlobalClock clock = session.getGlobalClock();
            if (clock != null) {
                clock.advanceTurn();
            }

            log.debug("Turn advanced to {} in session: {} (simultaneous turns)",
                    game.getCurrentTurn(), session.getSessionId());

            // Execute AI turns for all players with delegation enabled
            try {
                aiDelegationService.executeAllDelegatedTurns(session.getSessionId());
            } catch (Exception e) {
                log.warn("AI delegation turn execution failed in session {}: {}", session.getSessionId(), e.getMessage());
            }

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildStation(GameSession session, Long playerId, BuildStationRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            com.yisy.modules.doramcu.hex.SpaceStationType stationType;
            try {
                stationType = com.yisy.modules.doramcu.hex.SpaceStationType.valueOf(request.getStationType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid station type: " + request.getStationType());
            }

            if (!game.canBuildStation(player.getEmpireId(), stationType)) {
                throw new IllegalStateException("Insufficient resources to build " + stationType + " station");
            }

            if (request.getPlanetId() != null) {
                Planet planet = game.getPlanet(request.getPlanetId());
                if (planet == null) {
                    throw new IllegalStateException("Planet not found: " + request.getPlanetId());
                }
            }

            com.yisy.modules.doramcu.hex.SpaceStation station = game.buildStation(player, stationType, null);
            if (station == null) {
                throw new IllegalStateException("Failed to build station");
            }

            log.info("Player {} built {} station (id={})", player.getName(), stationType, station.getId());
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildFacility(GameSession session, Long playerId, BuildFacilityRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Planet planet = game.getPlanet(request.getPlanetId());
            if (planet == null) {
                throw new IllegalStateException("Planet not found: " + request.getPlanetId());
            }

            if (!player.getOwnedPlanetIds().contains(request.getPlanetId())) {
                throw new IllegalStateException("Planet is not owned by this player");
            }

            com.yisy.modules.doramcu.hex.StartingFacility facility;
            try {
                facility = com.yisy.modules.doramcu.hex.StartingFacility.valueOf(request.getFacilityType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid facility type: " + request.getFacilityType());
            }

            if (!game.canBuildPlanetFacility(player.getEmpireId(), facility)) {
                throw new IllegalStateException("Insufficient resources to build " + facility.getDisplayName());
            }

            boolean success = game.buildPlanetFacility(planet, facility, player.getEmpireId());
            if (!success) {
                throw new IllegalStateException("Failed to build facility on planet");
            }

            Empire empire = game.getEmpire(player.getEmpireId());
            if (empire != null) {
                empire.getFacilities().buildFacility(planet.getId(), facility);
            }

            log.info("Player {} built {} facility on planet {}", player.getName(), facility.getDisplayName(), request.getPlanetId());
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse createFleet(GameSession session, Long playerId, CreateFleetRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Fleet fleet = game.createFleet(request.getFleetName(), player);

            Empire empire = game.getEmpire(player.getEmpireId());
            if (empire != null) {
                empire.getNavy().addFleet(fleet);
            }

            log.info("Player {} created fleet '{}' (id={})", player.getName(), request.getFleetName(), fleet.getId());
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse colonizePlanet(GameSession session, Long playerId, ColonizeRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Planet planet = game.getPlanet(request.getPlanetId());
            if (planet == null) {
                throw new IllegalStateException("Planet not found: " + request.getPlanetId());
            }

            boolean success = game.transferPlanet(request.getPlanetId(), player);
            if (!success) {
                throw new IllegalStateException("Failed to colonize planet");
            }

            Empire empire = game.getEmpire(player.getEmpireId());
            if (empire != null) {
                empire.claimPlanet(planet.getId());
            }

            log.info("Player {} colonized planet {}", player.getName(), request.getPlanetId());
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse reinforceBattle(GameSession session, Long playerId, int battleId, ReinforceBattleRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Integer fleetId = request.getFleetId();
            if (!player.getOwnedFleetIds().contains(fleetId)) {
                throw new IllegalStateException("Fleet not found or not owned");
            }

            Fleet fleet = game.getFleet(fleetId);
            if (fleet == null) {
                throw new IllegalStateException("Fleet not found");
            }

            int added = game.reinforceAttacker(battleId, fleet);
            log.info("Player {} reinforced battle {} with {} ships from fleet {}",
                    player.getName(), battleId, added, fleetId);

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse retreatBattle(GameSession session, Long playerId, int battleId) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            game.retreatBattle(battleId);
            log.info("Player {} retreated from battle {}", player.getName(), battleId);

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildShipFromDesign(GameSession session, Long playerId, ShipDesignRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            int shipId = game.createShipFromDesign(player, request);
            log.info("Player {} built ship from design: {}", player.getName(), request.getName());

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buyCommodity(GameSession session, Long playerId, TradeRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            int cost = game.buyCommodity(player, request.getWorldId(), request.getCommodity(), request.getQuantity());
            log.info("Player {} bought {} units of {} for {} credits", player.getName(), request.getQuantity(), request.getCommodity(), cost);

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse sellCommodity(GameSession session, Long playerId, TradeRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            int revenue = game.sellCommodity(player, request.getWorldId(), request.getCommodity(), request.getQuantity());
            log.info("Player {} sold {} units of {} for {} credits", player.getName(), request.getQuantity(), request.getCommodity(), revenue);

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse assignCommander(GameSession session, Long playerId, int characterId) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            TravellerCharacter character = characterService.getCharacter(characterId);
            if (character == null) {
                throw new NoSuchElementException("Character not found: " + characterId);
            }

            player.setCommander(character);
            log.info("Player {} assigned commander '{}' (id={})", player.getName(), character.getName(), characterId);

            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    // ---- Battle result handling ----

    private void applyBattleResult(Game game, GameSession session,
                                    Battle battle, Battle.BattleResult result,
                                    Player attackerPlayer, Player defenderPlayer) {
        if (result == null) return;

        Fleet attacker = battle.getAttacker();
        Fleet defender = battle.getDefender();
        Battle.BattleOutcome outcome = result.getOutcome();

        applyFleetDamageFromBattle(attacker, battle);
        applyFleetDamageFromBattle(defender, battle);

        attacker.removeDestroyedShips();
        defender.removeDestroyedShips();

        if (attacker.isDestroyed()) {
            log.info("Attacker fleet {} completely destroyed in battle {}", attacker.getName(), battle.getId());
            removeFleetFromGame(game, attacker, attackerPlayer);
        }

        if (defender.isDestroyed()) {
            log.info("Defender fleet {} completely destroyed in battle {}", defender.getName(), battle.getId());
            removeFleetFromGame(game, defender, defenderPlayer);
        }

        if (outcome == Battle.BattleOutcome.ATTACKER_VICTORY && defenderPlayer != null) {
            captureDefenderPlanets(game, session, attackerPlayer, defenderPlayer, battle);
        }

        checkPlayerElimination(game, session, attackerPlayer);
        checkPlayerElimination(game, session, defenderPlayer);
    }

    private void applyFleetDamageFromBattle(Fleet actualFleet, Battle battle) {
        boolean isAttacker = actualFleet.getId() == battle.getAttacker().getId();
        int totalDamageDealt = isAttacker ? battle.getTotalDefenderDamage() : battle.getTotalAttackerDamage();

        if (totalDamageDealt > 0) {
            actualFleet.distributeDamage(totalDamageDealt);
        }
    }

    private void removeFleetFromGame(Game game, Fleet fleet, Player owner) {
        if (owner != null) {
            owner.getOwnedFleetIds().remove(fleet.getId());
        }

        if (owner != null && owner.getEmpireId() > 0) {
            Empire empire = game.getEmpire(owner.getEmpireId());
            if (empire != null) {
                empire.getNavy().removeFleet(fleet);
            }
        }

        game.removeFleet(fleet.getId());
    }

    private void captureDefenderPlanets(Game game, GameSession session,
                                        Player attackerPlayer, Player defenderPlayer, Battle battle) {
        if (defenderPlayer == null || defenderPlayer.getOwnedPlanetIds().isEmpty()) {
            return;
        }

        Fleet defender = battle.getDefender();
        List<Integer> planetsToCapture = new ArrayList<>();

        if (defender.getHomeSystem() != null) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
            }
        }

        if (planetsToCapture.isEmpty()) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
                break;
            }
        }

        for (Integer planetId : planetsToCapture) {
            Planet planet = game.getPlanet(planetId);
            if (planet == null) continue;

            log.info("Planet {} captured by {} from {}", planet.getName(),
                    attackerPlayer.getName(), defenderPlayer.getName());

            game.transferPlanet(planetId, attackerPlayer);

            Empire attackerEmpire = game.getEmpire(attackerPlayer.getEmpireId());
            if (attackerEmpire != null) {
                attackerEmpire.claimPlanet(planetId);
            }

            Empire defenderEmpire = game.getEmpire(defenderPlayer.getEmpireId());
            if (defenderEmpire != null) {
                defenderEmpire.releasePlanet(planetId);
            }
        }
    }

    private void checkPlayerElimination(Game game, GameSession session, Player player) {
        if (player == null || player.isEliminated()) return;

        boolean hasFleets = !player.getOwnedFleetIds().isEmpty();
        boolean hasPlanets = !player.getOwnedPlanetIds().isEmpty();

        if (!hasFleets && !hasPlanets) {
            player.eliminate();
            log.warn("Player {} has been eliminated! No fleets or planets remaining.", player.getName());
        }
    }

    // ---- Helpers ----

    private Player findPlayerBySessionId(Game game, GameSession session, Long playerId) {
        if (!session.getPlayerIds().contains(playerId)) {
            return null;
        }
        for (Player player : game.getPlayers()) {
            if (player.getId() == playerId.intValue()) {
                return player;
            }
        }
        return null;
    }

    private GameStateResponse getGameStateInternal(GameSession session, Long playerId) {
        return gameStateBuilder.buildGameStateResponse(session, playerId, true);
    }
}
