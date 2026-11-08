package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.ConstructionModuleType;
import com.yisy.modules.doramcu.hex.ConstructionOrder;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.ModuleAssembler;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.ProcessingFacility;
import com.yisy.modules.doramcu.hex.ProcessingRecipe;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipComponent;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SolarSystemEntity;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ShipOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages turn progression and end-of-turn processing.
 * <p>
 * Responsible for advancing turns, processing resource production,
 * ship construction, fleet repairs, and population growth.
 * Includes Dora AI integration for AI-assisted turn summaries.
 * </p>
 */
public class TurnManager {

    private final GameContext context;

    // Resource pipeline managers per empire (lazy initialized)
    private final Map<Integer, ResourcePipelineManager> pipelineManagers = new HashMap<>();

    /**
     * Creates a new TurnManager.
     *
     * @param context the shared game context
     */
    public TurnManager(GameContext context) {
        this.context = context;
    }

    /**
     * Generates a summary of the current turn state.
     *
     * @return the turn summary string
     */
    public String getTurnSummary() {
        int turn = context.getCurrentTurn();
        int activePlayers = context.getActivePlayerCount();
        Player currentPlayer = context.getCurrentPlayer();
        String playerName = currentPlayer != null ? currentPlayer.getName() : "Unknown";
        return String.format(
            "Turn %d, %d active players, current player: %s",
            turn, activePlayers, playerName
        );
    }

    /**
     * Advances to the next player's turn.
     * Skips eliminated players.
     */
    public void nextPlayer() {
        List<Player> players = context.getPlayers();
        if (players.isEmpty()) {
            return;
        }
        int currentIndex = context.getCurrentPlayerIndex();
        int nextIndex = (currentIndex + 1) % players.size();
        // Skip eliminated players
        int safety = 0;
        while (players.get(nextIndex).isEliminated() && safety < players.size()) {
            nextIndex = (nextIndex + 1) % players.size();
            safety++;
        }
        context.setCurrentPlayerIndex(nextIndex);
    }

    /**
     * Advances the game by one full turn, processing all players.
     * <p>
     * Each turn processes:
     * <ul>
     *   <li>Resource production from owned planets</li>
     *   <li>Ship construction progress</li>
     *   <li>Fleet shield recharge and hull repair</li>
     *   <li>Ship fuel and maintenance consumption</li>
     *   <li>Active battle resolution</li>
     *   <li>Space station upkeep and resource production</li>
     * </ul>
     * </p>
     */
    public void nextTurn() {
        context.incrementTurn();
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            // Process ship construction
            processShipConstruction(player);

            // Process fleet repairs
            processFleetRepairs(player);
        }

        // Process resource pipelines for all empires
        processResourcePipelines();

        // Process ship fuel and maintenance consumption
        processShipMaintenance();

        // Resolve active battles — advance each battle by one round per turn
        resolveActiveBattles();

        // Process space station upkeep and resource production
        processStationUpkeep();

        // Process trade market fluctuations
        processTradeMarkets();

        // Process mercenary income for all empires
        processMercenaryIncome();

        // Check for game end conditions
        checkGameEndConditions();
    }

    // ============================================================
    // Trade Market Processing
    // ============================================================

    /**
     * Processes trade market fluctuations at end-of-turn.
     * Market conditions change, affecting commodity prices.
     * Delegates to TradeManager which handles: market fluctuation, economic events,
     * trade route network building, auto-trade, and credit exchange for each empire.
     */
    private void processTradeMarkets() {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            com.yisy.modules.doramcu.hex.manager.TradeManager tradeManager = game.getTradeManager();
            if (tradeManager != null) {
                tradeManager.processEndOfTurn(pipelineManagers);
            }
        }
    }

    // ============================================================
    // Mercenary Income Processing
    // ============================================================

    /**
     * Processes mercenary ticket income for all empires at end-of-turn.
     * Delegates to MercenaryIncomeManager which handles: contract processing,
     * mission resolution, ticket income calculation, and credit distribution.
     */
    private void processMercenaryIncome() {
        if (context instanceof com.yisy.modules.doramcu.hex.Game game) {
            com.yisy.modules.doramcu.hex.manager.MercenaryIncomeManager mercManager = game.getMercenaryIncomeManager();
            if (mercManager != null) {
                mercManager.processEndOfTurn();
            }
        }
    }

    /**
     * Resolves all active battles by advancing each one round.
     * When a battle completes, the results are applied to the actual game state:
     * damage is applied to fleets, destroyed ships are removed, destroyed fleets
     * are cleaned up, and planets may be captured.
     */
    private void resolveActiveBattles() {
        List<Battle> activeBattles = new ArrayList<>();
        for (Battle battle : context.getBattles().values()) {
            if (battle.getState() == Battle.BattleState.IN_PROGRESS) {
                activeBattles.add(battle);
            }
        }

        for (Battle battle : activeBattles) {
            // Skip if already completed (race condition check)
            if (battle.getState() != Battle.BattleState.IN_PROGRESS) continue;

            // Advance battle one round
            Battle.RoundResult round = battle.fightRound();
            if (round == null) continue; // stalemate

            // If battle completed, apply results
            if (battle.getState() == Battle.BattleState.COMPLETED) {
                applyBattleResultsToGame(battle);
            }
        }
    }

    /**
     * Applies the results of a completed battle to the actual game state.
     */
    private void applyBattleResultsToGame(Battle battle) {
        Fleet attacker = battle.getAttacker();
        Fleet defender = battle.getDefender();
        Battle.BattleOutcome outcome = battle.getOutcome();

        // Apply damage from battle simulation to actual fleets
        applyFleetDamageFromBattle(attacker, battle);
        applyFleetDamageFromBattle(defender, battle);

        // Remove destroyed ships
        attacker.removeDestroyedShips();
        defender.removeDestroyedShips();

        // Handle fleet destruction
        if (attacker.isDestroyed()) {
            removeDestroyedFleet(attacker, attacker.getOwner());
        }
        if (defender.isDestroyed()) {
            removeDestroyedFleet(defender, defender.getOwner());
        }

        // Handle planet capture on attacker victory
        if (outcome == Battle.BattleOutcome.ATTACKER_VICTORY) {
            Player attackerPlayer = attacker.getOwner();
            Player defenderPlayer = defender.getOwner();
            if (attackerPlayer != null && defenderPlayer != null) {
                capturePlanetsAfterBattle(attackerPlayer, defenderPlayer, battle);
            }
        }

        // Check player elimination
        checkPlayerElimination(attacker.getOwner());
        checkPlayerElimination(defender.getOwner());
    }

    /**
     * Applies damage from battle simulation to the actual fleet.
     */
    private void applyFleetDamageFromBattle(Fleet actualFleet, Battle battle) {
        boolean isAttacker = actualFleet.getId() == battle.getAttacker().getId();
        int totalDamageDealt = isAttacker ? battle.getTotalDefenderDamage() : battle.getTotalAttackerDamage();
        if (totalDamageDealt > 0) {
            actualFleet.distributeDamage(totalDamageDealt);
        }
    }

    /**
     * Removes a destroyed fleet from the game context.
     */
    private void removeDestroyedFleet(Fleet fleet, Player owner) {
        if (owner != null) {
            owner.getOwnedFleetIds().remove(fleet.getId());
        }
        context.getFleets().remove(fleet.getId());
    }

    /**
     * Captures planets when the attacker wins a battle.
     * Since planets don't have positions, captures non-capital planets from the defender.
     */
    private void capturePlanetsAfterBattle(Player attackerPlayer, Player defenderPlayer, Battle battle) {
        if (defenderPlayer.getOwnedPlanetIds().isEmpty()) return;

        Fleet defender = battle.getDefender();
        List<Integer> planetsToCapture = new ArrayList<>();

        // If defender fleet has a home system, capture non-capital planets
        if (defender.getHomeSystem() != null) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
            }
        }

        // If no home system, capture one random non-capital planet
        if (planetsToCapture.isEmpty()) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
                break;
            }
        }

        // Transfer captured planets
        for (Integer planetId : planetsToCapture) {
            defenderPlayer.removePlanet(planetId);
            attackerPlayer.addPlanet(planetId);
        }
    }

    /**
     * Checks if a player should be eliminated.
     */
    private void checkPlayerElimination(Player player) {
        if (player == null || player.isEliminated()) return;
        if (player.getOwnedFleetIds().isEmpty() && player.getOwnedPlanetIds().isEmpty()) {
            player.eliminate();
        }
    }

// Flat resource production removed – use ResourcePipelineManager for mining/processing

    /**
     * Processes ship construction queue for a player.
     *
     * @param player the player
     */
    private void processShipConstruction(Player player) {
        // Separate completed and pending orders
        List<ShipOrder> pendingQueue = new ArrayList<>();
        List<ShipOrder> completedOrders = new ArrayList<>();
        for (ShipOrder order : player.getShipQueue()) {
            if (order.advanceTurn()) {
                completedOrders.add(order);
            } else {
                pendingQueue.add(order);
            }
        }

        // Add completed ships to fleets
        for (ShipOrder order : completedOrders) {
            Fleet fleet = context.getFleet(order.getTargetFleetId());
            if (fleet != null) {
                Ship completedShip = new Ship(context.getNextShipIdAndIncrement(), order.getShipTemplate().getShipType());
                completedShip.setName(order.getShipTemplate().getName());
                for (ShipComponent comp : order.getShipTemplate().getComponents()) {
                    ShipComponent newComp;
                    switch (comp.getType()) {
                        case WEAPON:
                            newComp = new ShipComponent(comp.getWeaponType(), comp.getQuantity());
                            break;
                        case SHIELD:
                            newComp = ShipComponent.shield(comp.getQuantity());
                            break;
                        case HULL_REINFORCEMENT:
                            newComp = ShipComponent.hullReinforcement(comp.getQuantity());
                            break;
                        default:
                            continue;
                    }
                    completedShip.addComponent(newComp);
                }
                if (order.getShipTemplate().getHomePlanet() != null) {
                    completedShip.setHomePlanet(order.getShipTemplate().getHomePlanet());
                }
                context.addShip(completedShip);
                fleet.addShip(completedShip);
            }
        }
    }

    /**
     * Processes fleet shield recharge and hull repair.
     *
     * @param player the player
     */
    private void processFleetRepairs(Player player) {
        for (Integer fleetId : player.getOwnedFleetIds()) {
            Fleet fleet = context.getFleet(fleetId);
            if (fleet != null) {
                fleet.endTurnRepair();
            }
        }
    }

// Population growth removed – use ResourcePipelineManager for mining/processing

    /**
     * Checks if the game should end (only one player remaining).
     */
    private void checkGameEndConditions() {
        if (context.getActivePlayerCount() <= 1) {
            context.setGameEnded(true);
        }
    }

    // ---- Resource Pipeline Integration ----

    /**
     * Gets or creates the resource pipeline manager for the given empire.
     * <p>
     * When first created, the pipeline is initialized with mining operations
     * for all planets owned by the empire, and default processing facilities
     * and assemblers are added.
     * </p>
     *
     * @param empireId the empire ID
     * @return the pipeline manager for that empire
     */
    public ResourcePipelineManager getPipelineManager(int empireId) {
        return pipelineManagers.computeIfAbsent(empireId, id -> {
            ResourcePipelineManager pipeline = new ResourcePipelineManager(id);
            initializePipeline(pipeline, id);
            return pipeline;
        });
    }

    /**
     * Initializes a newly created pipeline with mining operations,
     * processing facilities, and module assemblers.
     * Mining bonuses from planetary facilities and nearby stations are applied.
     *
     * @param pipeline the pipeline to initialize
     * @param empireId the empire ID
     */
    private void initializePipeline(ResourcePipelineManager pipeline, int empireId) {
        // Find the player for this empire
        Player owner = null;
        for (Player player : context.getPlayers()) {
            if (player.getEmpireId() == empireId) {
                owner = player;
                break;
            }
        }
        if (owner == null) {
            return;
        }

        // Add mining operations for all owned planets with facility bonuses
        Map<Integer, Planet> planets = context.getPlanets();
        for (Integer planetId : owner.getOwnedPlanetIds()) {
            Planet planet = planets.get(planetId);
            if (planet != null) {
                // Combine planet facility bonus with base bonus
                int miningBonus = planet.getMiningBonus();
                pipeline.addMiningOperation(planet, miningBonus);
            }
        }

        // Add default processing facilities (one per resource type)
        for (ProcessingRecipe recipe : ProcessingRecipe.STANDARD_RECIPES.values()) {
            ProcessingFacility facility = new ProcessingFacility(recipe);
            pipeline.addProcessingFacility(facility);
        }

        // Add default module assemblers (one per module type)
        for (ConstructionModuleType moduleType : ConstructionModuleType.values()) {
            ModuleAssembler assembler = new ModuleAssembler(moduleType);
            pipeline.addAssembler(assembler);
        }
    }

    /**
     * Processes all resource pipelines for one turn.
     * After pipeline processing, completed construction orders are fulfilled.
     */
    private void processResourcePipelines() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            ResourcePipelineManager pipeline = pipelineManagers.get(player.getEmpireId());
            if (pipeline == null) {
                continue;
            }

            // Process pipeline turn (mining, transport, processing, assembly)
            pipeline.processTurn();

            // Process completed construction orders for this player
            processCompletedConstructionForPlayer(player, pipeline);
        }
    }

    /**
     * Processes completed construction orders for a single player,
     * creating stations and facilities from completed orders.
     */
    private void processCompletedConstructionForPlayer(Player player, ResourcePipelineManager pipeline) {
        FacilityConstructionManager facilityManager = context.getFacilityConstructionManager();
        if (facilityManager == null) {
            return;
        }

        List<ConstructionOrder> completedOrders = pipeline.processConstructionQueue();
        for (ConstructionOrder order : completedOrders) {
            if (order.getConstructionType() == ConstructionOrder.ConstructionType.SPACE_STATION) {
                SolarSystemEntity homeSystem = order.getTargetSystem();
                if (homeSystem != null) {
                    SpaceStation station = facilityManager.completeStation(order, player, homeSystem);
                    if (station != null) {
                        // Add station to the solar system at a default position
                        homeSystem.addSpaceStation(station, new Hex3D(0, 0, 0));
                    }
                }
            } else if (order.getConstructionType() == ConstructionOrder.ConstructionType.PLANET_FACILITY) {
                // Find the target planet
                Planet planet = null;
                if (order.getTargetPlanetId() != null) {
                    planet = context.getPlanet(order.getTargetPlanetId());
                }
                if (planet != null) {
                    facilityManager.completePlanetFacility(order, planet);
                }
            }
        }
    }

    /**
     * Processes ship fuel and maintenance consumption for all players.
     * Each player's fleets consume GAS fuel and construction modules from their pipeline.
     */
    private void processShipMaintenance() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            ResourcePipelineManager pipeline = pipelineManagers.get(player.getEmpireId());
            if (pipeline == null) {
                continue;
            }
            // Collect this player's fleets
            List<Fleet> fleets = new ArrayList<>();
            for (Integer fleetId : player.getOwnedFleetIds()) {
                Fleet fleet = context.getFleet(fleetId);
                if (fleet != null) {
                    fleets.add(fleet);
                }
            }
            if (!fleets.isEmpty()) {
                pipeline.processShipMaintenance(fleets);
            }
        }
    }

    /**
     * Processes space station upkeep and resource production for all players.
     * Each station consumes construction modules for maintenance and produces
     * raw/refined resources based on its type.
     */
    private void processStationUpkeep() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) {
                continue;
            }
            ResourcePipelineManager pipeline = pipelineManagers.get(player.getEmpireId());
            if (pipeline == null) {
                continue;
            }

            // Collect all stations owned by this player
            List<SpaceStation> stations = new ArrayList<>();
            for (SpaceStation station : context.getSpaceStations()) {
                if (station.getOwner() != null && station.getOwner().getEmpireId() == player.getEmpireId()) {
                    stations.add(station);
                }
            }

            // Process each station's economy and AI Core
            for (SpaceStation station : stations) {
                if (!station.isDestroyed()) {
                    station.getEconomy().processTurn(pipeline);
                    // Process station AI Core if installed
                    if (station.hasAiCore()) {
                        station.getAiCore().processTurn(null);
                    }
                }
            }
        }
    }

    /**
     * Returns all pipeline managers.
     */
    public Map<Integer, ResourcePipelineManager> getPipelineManagers() {
        return Map.copyOf(pipelineManagers);
    }
}
