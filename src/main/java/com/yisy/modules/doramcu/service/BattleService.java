package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.BattleRequest;
import com.yisy.modules.doramcu.dto.BattleResponse;
import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.traveller.ShipDesign;
import com.yisy.modules.doramcu.traveller.TravellerFleetBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for ship-to-ship battle management.
 * <p>
 * Bridges Traveller ship designs to the hex-based combat system,
 * creating fleets from designs and managing battle lifecycle.
 * </p>
 * <p>
 * Battles are stored in-memory for the session duration.
 * </p>
 */
@Service
public class BattleService {

    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    private final ShipDesignService shipDesignService;
    private final Map<Integer, Battle> activeBattles = new ConcurrentHashMap<>();
    private final Map<Integer, BattleResponse> completedBattles = new ConcurrentHashMap<>();
    private final AtomicInteger battleIdCounter = new AtomicInteger(1);

    public BattleService(ShipDesignService shipDesignService) {
        this.shipDesignService = shipDesignService;
    }

    /**
     * Creates and fights a battle between two ship designs.
     *
     * @param request the battle request parameters
     * @return the battle result
     */
    public BattleResponse createAndFightBattle(BattleRequest request) {
        log.info("Creating battle: {} vs {}",
                request.getAttackerDesignId(), request.getDefenderDesignId());

        // Get ship designs
        ShipDesign attackerDesign = shipDesignService.getDesignDomain(request.getAttackerDesignId());
        ShipDesign defenderDesign = shipDesignService.getDesignDomain(request.getDefenderDesignId());

        if (attackerDesign == null) {
            throw new IllegalArgumentException("Attacker design not found: " +
                    request.getAttackerDesignId());
        }
        if (defenderDesign == null) {
            throw new IllegalArgumentException("Defender design not found: " +
                    request.getDefenderDesignId());
        }

        // Create fleets from designs
        Fleet attackerFleet = createFleetFromDesign(attackerDesign,
                request.getAttackerShipCount(), "Attacker");
        Fleet defenderFleet = createFleetFromDesign(defenderDesign,
                request.getDefenderShipCount(), "Defender");

        // Create battle
        int battleId = battleIdCounter.getAndIncrement();
        Battle battle = new Battle(battleId, attackerFleet, defenderFleet,
                new Hex3D(0, 0, 0));

        // Fight the battle
        Battle.BattleResult result = battle.fightToCompletion();

        // Convert to response
        BattleResponse response = convertToResponse(battle, result,
                attackerDesign.getName(), defenderDesign.getName());

        // Store completed battle
        completedBattles.put(battleId, response);

        log.info("Battle {} completed: {}", battleId, result.getOutcome());
        return response;
    }

    /**
     * Creates a battle for round-by-round play.
     *
     * @param request the battle request parameters
     * @return the initial battle status
     */
    public BattleResponse createBattle(BattleRequest request) {
        log.info("Creating battle for round-by-round play: {} vs {}",
                request.getAttackerDesignId(), request.getDefenderDesignId());

        // Get ship designs
        ShipDesign attackerDesign = shipDesignService.getDesignDomain(request.getAttackerDesignId());
        ShipDesign defenderDesign = shipDesignService.getDesignDomain(request.getDefenderDesignId());

        if (attackerDesign == null) {
            throw new IllegalArgumentException("Attacker design not found: " +
                    request.getAttackerDesignId());
        }
        if (defenderDesign == null) {
            throw new IllegalArgumentException("Defender design not found: " +
                    request.getDefenderDesignId());
        }

        // Create fleets from designs
        Fleet attackerFleet = createFleetFromDesign(attackerDesign,
                request.getAttackerShipCount(), "Attacker");
        Fleet defenderFleet = createFleetFromDesign(defenderDesign,
                request.getDefenderShipCount(), "Defender");

        // Create and start battle
        int battleId = battleIdCounter.getAndIncrement();
        Battle battle = new Battle(battleId, attackerFleet, defenderFleet,
                new Hex3D(0, 0, 0));
        battle.start();

        // Store active battle
        activeBattles.put(battleId, battle);

        // Return initial status
        BattleResponse response = new BattleResponse();
        response.setBattleId(battleId);
        response.setBattleName(request.getBattleName() != null ?
                request.getBattleName() : "Battle " + battleId);
        response.setOutcome("IN_PROGRESS");
        response.setAttackerShipName(attackerDesign.getName());
        response.setDefenderShipName(defenderDesign.getName());
        response.setInitialAttackerShips(request.getAttackerShipCount());
        response.setInitialDefenderShips(request.getDefenderShipCount());
        response.setRoundResults(new ArrayList<>());
        response.setEventLog(List.of("Battle started"));

        return response;
    }

    /**
     * Fights one round of an active battle.
     *
     * @param battleId the battle ID
     * @return the updated battle status
     */
    public BattleResponse fightRound(int battleId) {
        Battle battle = activeBattles.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }

        Battle.RoundResult round = battle.fightRound();
        if (round == null || battle.getState() == Battle.BattleState.COMPLETED) {
            // Battle ended
            Battle.BattleResult result = battle.fightToCompletion();
            BattleResponse response = convertToResponse(battle, result,
                    battle.getAttacker().getName(),
                    battle.getDefender().getName());

            // Move from active to completed
            activeBattles.remove(battleId);
            completedBattles.put(battleId, response);

            return response;
        }

        // Return round status
        BattleResponse response = new BattleResponse();
        response.setBattleId(battleId);
        response.setOutcome("IN_PROGRESS");
        response.setTotalRounds(battle.getCurrentRound());

        // Build round results list
        List<BattleResponse.RoundResult> roundResults = new ArrayList<>();
        for (Battle.RoundResult rr : battle.getRoundResults()) {
            roundResults.add(convertRoundResult(rr));
        }
        response.setRoundResults(roundResults);

        response.setEventLog(battle.getEvents().stream()
                .map(Battle.BattleEvent::getDescription)
                .toList());

        return response;
    }

    /**
     * Gets the status of an active battle.
     *
     * @param battleId the battle ID
     * @return the battle status, or null if not found
     */
    public BattleResponse getBattleStatus(int battleId) {
        // Check completed battles first
        BattleResponse completed = completedBattles.get(battleId);
        if (completed != null) {
            return completed;
        }

        // Check active battles
        Battle battle = activeBattles.get(battleId);
        if (battle == null) {
            return null;
        }

        BattleResponse response = new BattleResponse();
        response.setBattleId(battleId);
        response.setOutcome("IN_PROGRESS");
        response.setTotalRounds(battle.getCurrentRound());

        List<BattleResponse.RoundResult> roundResults = new ArrayList<>();
        for (Battle.RoundResult rr : battle.getRoundResults()) {
            roundResults.add(convertRoundResult(rr));
        }
        response.setRoundResults(roundResults);

        response.setEventLog(battle.getEvents().stream()
                .map(Battle.BattleEvent::getDescription)
                .toList());

        return response;
    }

    /**
     * Lists all completed battles.
     *
     * @return list of completed battle summaries
     */
    public List<BattleResponse> getAllCompletedBattles() {
        return new ArrayList<>(completedBattles.values());
    }

    /**
     * Lists all active battles.
     *
     * @return list of active battle IDs
     */
    public List<Integer> getActiveBattleIds() {
        return new ArrayList<>(activeBattles.keySet());
    }

    // ---- Private Helpers ----

    private Fleet createFleetFromDesign(ShipDesign design, int count, String fleetName) {
        TravellerFleetBuilder builder = new TravellerFleetBuilder(fleetName);
        for (int i = 0; i < count; i++) {
            builder.addDesign(design);
        }
        return builder.build();
    }

    private BattleResponse convertToResponse(Battle battle, Battle.BattleResult result,
                                              String attackerName, String defenderName) {
        BattleResponse response = new BattleResponse();
        response.setBattleId(battle.getId());
        response.setBattleName(battle.getAttacker().getName() + " vs " +
                battle.getDefender().getName());
        response.setOutcome(result.getOutcome().name());
        response.setTotalRounds(result.getTotalRounds());
        response.setTotalAttackerDamage(result.getTotalAttackerDamage());
        response.setTotalDefenderDamage(result.getTotalDefenderDamage());
        response.setAttackerShipsLost(result.getTotalAttackerShipsLost());
        response.setDefenderShipsLost(result.getTotalDefenderShipsLost());
        response.setInitialAttackerShips(result.getInitialAttackerShips());
        response.setInitialDefenderShips(result.getInitialDefenderShips());
        response.setAttackerShipName(attackerName);
        response.setDefenderShipName(defenderName);

        // Convert round results
        List<BattleResponse.RoundResult> roundResults = new ArrayList<>();
        for (Battle.RoundResult rr : result.getRoundResults()) {
            roundResults.add(convertRoundResult(rr));
        }
        response.setRoundResults(roundResults);

        // Convert event log
        List<String> eventLog = new ArrayList<>();
        for (Battle.BattleEvent event : result.getEvents()) {
            eventLog.add(event.getDescription());
        }
        response.setEventLog(eventLog);

        return response;
    }

    private BattleResponse.RoundResult convertRoundResult(Battle.RoundResult rr) {
        return new BattleResponse.RoundResult(
                rr.getRoundNumber(),
                rr.getAttackerDamageDealt(),
                rr.getDefenderDamageDealt(),
                rr.getAttackerShipsDestroyed(),
                rr.getDefenderShipsDestroyed(),
                rr.getAttackerRemainingHull(),
                rr.getDefenderRemainingHull()
        );
    }
}
