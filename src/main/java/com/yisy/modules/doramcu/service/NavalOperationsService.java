package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.NavalRequest;
import com.yisy.modules.doramcu.dto.NavalResponse;
import com.yisy.modules.doramcu.traveller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service layer for naval operations.
 */
@Service
public class NavalOperationsService {

    private static final Logger log = LoggerFactory.getLogger(NavalOperationsService.class);

    private final Map<Integer, NavalFleet> fleets = new HashMap<>();
    private final AtomicInteger fleetIdCounter = new AtomicInteger(1);

    /**
     * Creates a new naval fleet from ship designs.
     */
    public NavalResponse createFleet(NavalRequest request) {
        int fleetId = fleetIdCounter.getAndIncrement();
        NavalFleet.FleetType fleetType = NavalFleet.FleetType.SUB_SECTORAL;
        NavalFleet fleet = new NavalFleet(fleetId, request.getFleetName(), fleetType);

        // Create squadrons from ship designs
        ShipDesignService shipDesignService = getShipDesignService();
        ShipDesign design = shipDesignService.getDesignDomain(request.getShipDesignId());
        if (design == null) {
            throw new IllegalArgumentException("Ship design not found: " + request.getShipDesignId());
        }

        // Create a squadron for each ship
        int squadronId = 1;
        for (int i = 0; i < request.getShipCount(); i++) {
            String squadronName = request.getSquadronName() != null ?
                    request.getSquadronName() + " " + (i + 1) :
                    "Squadron " + (i + 1);

            NavalSquadron squadron = new NavalSquadron(squadronId++, squadronName);
            fleet.addSquadron(squadron);
        }

        fleets.put(fleetId, fleet);

        NavalResponse response = new NavalResponse();
        response.setOperation("CREATE_FLEET");
        response.setFleetName(fleet.getName());

        List<String> squadronNames = new ArrayList<>();
        for (NavalSquadron sq : fleet.getSquadrons()) {
            squadronNames.add(sq.getName());
        }
        response.setSquadronNames(squadronNames);

        response.setMessage(fleet.getName() + " created with " +
                request.getShipCount() + " ships");

        return response;
    }

    /**
     * Gets fleet status.
     */
    public NavalResponse getFleetStatus(int fleetId) {
        NavalFleet fleet = fleets.get(fleetId);
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet not found: " + fleetId);
        }

        NavalResponse response = new NavalResponse();
        response.setOperation("GET_FLEET_STATUS");
        response.setFleetName(fleet.getName());

        List<String> squadronNames = new ArrayList<>();
        for (NavalSquadron sq : fleet.getSquadrons()) {
            squadronNames.add(sq.getName());
        }
        response.setSquadronNames(squadronNames);

        response.setMessage(fleet.getName() + " — " +
                fleet.getSquadrons().size() + " squadrons");

        return response;
    }

    /**
     * Fights a naval battle between two fleets.
     */
    public NavalResponse fightBattle(NavalRequest request) {
        NavalFleet attacker = fleets.get(request.getAttackerFleetId());
        NavalFleet defender = fleets.get(request.getDefenderFleetId());

        if (attacker == null) {
            throw new IllegalArgumentException("Attacker fleet not found: " +
                    request.getAttackerFleetId());
        }
        if (defender == null) {
            throw new IllegalArgumentException("Defender fleet not found: " +
                    request.getDefenderFleetId());
        }

        // Create a dummy world for the battle system
        TravellerWorld battleWorld = null; // Use null for system-agnostic battle

        NavalBattle battle = new NavalBattle(attacker, defender, battleWorld);
        // Run battle rounds until completion
        while (battle.isActive()) {
            battle.executeRound(FleetAction.LASER_BARRAGE, FleetAction.HOLD);
        }

        // Aggregate results from rounds
        int totalAttackerDamage = 0;
        int totalDefenderDamage = 0;
        int totalAttackerLost = 0;
        int totalDefenderLost = 0;

        for (NavalBattle.BattleRound round : battle.getRounds()) {
            totalAttackerDamage += round.getFleet1DamageDealt();
            totalDefenderDamage += round.getFleet2DamageDealt();
            totalAttackerLost += round.getFleet1ShipsLost();
            totalDefenderLost += round.getFleet2ShipsLost();
        }

        NavalResponse response = new NavalResponse();
        response.setOperation("FIGHT_BATTLE");
        response.setOutcome(battle.getOutcome().name());
        response.setTotalRounds(battle.getRounds().size());
        response.setAttackerDamage(totalAttackerDamage);
        response.setDefenderDamage(totalDefenderDamage);
        response.setAttackerShipsLost(totalAttackerLost);
        response.setDefenderShipsLost(totalDefenderLost);

        List<String> eventLog = new ArrayList<>(battle.getBattleLog());
        response.setEventLog(eventLog);

        response.setMessage(attacker.getName() + " vs " + defender.getName() +
                ": " + battle.getOutcome());

        return response;
    }

    private ShipDesignService getShipDesignService() {
        // In a real implementation, this would be injected
        // For now, return a simple instance
        return new ShipDesignService();
    }

    private static class AtomicInteger {
        private int value;
        AtomicInteger(int v) { this.value = v; }
        int getAndIncrement() { return value++; }
    }
}
