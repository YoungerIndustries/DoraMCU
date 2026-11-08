package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.EmpireArmy;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.traveller.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages mercenary ticket contracts and income for DoraMCU empires.
 * <p>
 * The MercenaryIncomeManager handles the full lifecycle of mercenary operations:
 * </p>
 * <ul>
 *   <li>Generates available mercenary tickets (contracts) based on empire strength</li>
 *   <li>Assigns mercenary units to tickets</li>
 *   <li>Resolves ticket missions using {@link AbstractBattle}</li>
 *   <li>Calculates profit after payroll expenses</li>
 *   <li>Tracks mercenary income per empire</li>
 * </ul>
 * <p>
 * Mercenary tickets are processed at end-of-turn, providing income that can be
 * used for ship construction, facility upgrades, and recruiting more units.
 * </p>
 * <p>
 * Based on Classic Traveller Book 4: Mercenary (1980) ticket rules.
 * </p>
 */
public class MercenaryIncomeManager {

    private final GameContext context;
    private final Random random;

    /** Maps empire ID to active tickets. */
    private final Map<Integer, List<MercenaryContract>> activeContracts = new HashMap<>();

    /** History of completed contracts. */
    private final List<ContractResult> contractHistory = new ArrayList<>();

    /** Average monthly salary per soldier in credits. */
    public static final long AVERAGE_SALARY_PER_SOLDIER = 600;

    /** Ticket holder profit percentage (50% deducted by ticket holder). */
    public static final double TICKET_HOLDER_PROFIT_RATE = 0.5;

    /**
     * Creates a new MercenaryIncomeManager.
     *
     * @param context the shared game context
     */
    public MercenaryIncomeManager(GameContext context) {
        this.context = context;
        this.random = context.getRandom();
    }

    // ============================================================
    // Ticket Generation
    // ============================================================

    /**
     * Generates available mercenary tickets for an empire.
     * <p>
     * The number and type of tickets depend on the empire's ground force strength
     * and the current game state.
     * </p>
     *
     * @param empire the empire to generate tickets for
     * @return list of available tickets
     */
    public List<MercenaryTicket> generateTickets(Empire empire) {
        if (empire == null) return Collections.emptyList();

        EmpireArmy army = empire.getArmy();
        int totalStrength = army.getTotalGroundStrength();

        if (totalStrength < 10) {
            return Collections.emptyList(); // Not enough force for any ticket
        }

        List<MercenaryTicket> tickets = new ArrayList<>();
        int ticketCount = random.nextInt(1, 4); // 1-3 tickets available

        for (int i = 0; i < ticketCount; i++) {
            tickets.add(generateSingleTicket(empire.getId(), totalStrength));
        }

        return tickets;
    }

    /**
     * Generates a single mercenary ticket.
     */
    private MercenaryTicket generateSingleTicket(int empireId, int totalStrength) {
        int id = random.nextInt(10000, 99999);

        // Mission type
        MissionType[] types = MissionType.values();
        MissionType missionType = types[random.nextInt(types.length)];

        // Payment type
        MercenaryTicket.PaymentType[] payments = MercenaryTicket.PaymentType.values();
        MercenaryTicket.PaymentType paymentType = payments[random.nextInt(payments.length)];

        // Ticket type
        MercenaryTicket.TicketType ticketType = random.nextInt(2) == 0
                ? MercenaryTicket.TicketType.SHORT
                : MercenaryTicket.TicketType.LONG;

        // Required strength scales with mission type
        int requiredStrength = calculateRequiredStrength(missionType, totalStrength);

        // Payment based on mission difficulty
        long basePayment = calculateBasePayment(missionType, requiredStrength);
        long successBonus = (long) (basePayment * 0.5);

        // Equipment budget for long tickets
        long equipmentBudget = ticketType == MercenaryTicket.TicketType.LONG
                ? (long) requiredStrength * 1000
                : 0;

        // Repatriation bond
        long repatriationBond = (long) requiredStrength * 500;

        // Duration in months
        int durationMonths = random.nextInt(1, 7);

        // Tech level
        int techLevel = random.nextInt(6, 13);

        // World name
        String world = "World-" + random.nextInt(100, 999);

        // Patron name
        String patron = "Patron-" + random.nextInt(100, 999);

        return new MercenaryTicket(
                id,
                missionType.getName() + " Contract",
                patron,
                missionType,
                paymentType,
                ticketType,
                basePayment,
                successBonus,
                equipmentBudget,
                repatriationBond,
                durationMonths,
                requiredStrength,
                techLevel,
                missionType.getName() + " mission on " + world,
                world
        );
    }

    /**
     * Calculates required strength for a mission type.
     */
    private int calculateRequiredStrength(MissionType type, int totalStrength) {
        int baseStrength = switch (type) {
            case COMMANDO -> (int) (totalStrength * 0.3);
            case STRIKER -> (int) (totalStrength * 0.5);
            case CADRE -> (int) (totalStrength * 0.2);
            case SECURITY -> (int) (totalStrength * 0.1);
        };
        return Math.max(10, baseStrength);
    }

    /**
     * Calculates base payment for a mission.
     */
    private long calculateBasePayment(MissionType type, int strength) {
        long baseRate = switch (type) {
            case COMMANDO -> 50000;
            case STRIKER -> 100000;
            case CADRE -> 30000;
            case SECURITY -> 20000;
        };
        // Scale with force size
        return baseRate + (long) (strength * 100);
    }

    // ============================================================
    // Contract Assignment
    // ============================================================

    /**
     * Assigns mercenary units from an empire to a ticket.
     * <p>
     * The units are committed to the mission and will be resolved
     * at end-of-turn.
     * </p>
     *
     * @param empire the empire
     * @param ticket the ticket to accept
     * @return the contract created, or null if insufficient strength
     */
    public MercenaryContract assignToTicket(Empire empire, MercenaryTicket ticket) {
        if (empire == null || ticket == null) return null;

        EmpireArmy army = empire.getArmy();
        int totalStrength = army.getTotalGroundStrength();

        if (totalStrength < ticket.getRequiredStrength()) {
            return null; // Not enough force
        }

        // Calculate how many units to assign
        List<MercenaryUnit> assignedUnits = new ArrayList<>();
        int assignedStrength = 0;
        List<MercenaryUnit> allUnits = new ArrayList<>(army.getMercenaryUnits());

        // Also include regular army units' strength
        int armyUnitStrength = army.getUnits().stream()
                .mapToInt(com.yisy.modules.doramcu.hex.ArmyUnit::getCurrentStrength)
                .sum();

        for (MercenaryUnit unit : allUnits) {
            if (assignedStrength < ticket.getRequiredStrength()) {
                assignedUnits.add(unit);
                assignedStrength += unit.getStrength();
            }
        }

        // Add regular army strength
        assignedStrength += armyUnitStrength;

        if (assignedStrength < ticket.getRequiredStrength()) {
            return null; // Still not enough
        }

        MercenaryContract contract = new MercenaryContract(
                context.getCurrentTurn(),
                empire.getId(),
                ticket,
                assignedStrength,
                assignedUnits
        );

        activeContracts.computeIfAbsent(empire.getId(), k -> new ArrayList<>()).add(contract);
        return contract;
    }

    // ============================================================
    // Contract Resolution
    // ============================================================

    /**
     * Resolves all active contracts for an empire.
     * <p>
     * Each contract is resolved using {@link AbstractBattle} to determine
     * success or failure. Profit is calculated and added to the empire's income.
     * </p>
     *
     * @param empire the empire
     * @return list of contract results
     */
    public List<ContractResult> resolveContracts(Empire empire) {
        List<MercenaryContract> contracts = activeContracts.getOrDefault(empire.getId(), Collections.emptyList());
        List<ContractResult> results = new ArrayList<>();

        for (MercenaryContract contract : contracts) {
            ContractResult result = resolveSingleContract(empire, contract);
            results.add(result);
            contractHistory.add(result);
        }

        // Clear resolved contracts
        activeContracts.remove(empire.getId());
        return results;
    }

    /**
     * Resolves a single contract.
     */
    private ContractResult resolveSingleContract(Empire empire, MercenaryContract contract) {
        MercenaryTicket ticket = contract.getTicket();

        // Determine mission success using AbstractBattle
        boolean success = determineMissionSuccess(contract);

        // Calculate profit
        long profit = ticket.calculateProfit(contract.getAssignedStrength(), success);

        // Apply mercenary career bonus from commander
        Player ruler = context.getPlayer(empire.getRulerPlayerId());
        if (ruler != null && ruler.hasCommander()) {
            TravellerCharacter commander = ruler.getCommander();
            // Mercenary/Soldier career provides bonus to mission success
            double combatBonus = commander.getCommanderBonus(Player.CommanderBonusType.ARMY_COMBAT);
            if (combatBonus > 0 && !success) {
                // Bonus chance to succeed
                if (random.nextDouble() * 100 < combatBonus * 10) {
                    success = true;
                    profit = ticket.calculateProfit(contract.getAssignedStrength(), true);
                }
            }
        }

        // Calculate casualties
        int casualties = calculateCasualties(contract, success);

        return new ContractResult(
                context.getCurrentTurn(),
                empire.getId(),
                ticket,
                success,
                profit,
                casualties,
                contract.getAssignedStrength()
        );
    }

    /**
     * Determines if a mission succeeds using AbstractBattle.
     */
    private boolean determineMissionSuccess(MercenaryContract contract) {
        MissionType mission = contract.getTicket().getMissionType();

        // Create own force (mercenary)
        int morale = contract.getAssignedUnits().stream()
                .mapToInt(MercenaryUnit::getMorale)
                .max()
                .orElse(8);
        UnitType unitType = UnitType.fromDieRoll(random.nextInt(6) + 1 + mission.getSizeDm());
        AbstractBattle.BattleForce ownForce = new AbstractBattle.BattleForce(
                mission, 10, unitType, morale, contract.getAssignedStrength(), true
        );

        // Create random enemy force
        AbstractBattle.BattleForce enemyForce = AbstractBattle.createRandomEnemyForce(mission);

        // Run the battle
        AbstractBattle battle = new AbstractBattle(ownForce, enemyForce);
        boolean won = battle.resolve();

        return won;
    }

    /**
     * Calculates casualties from a mission.
     */
    private int calculateCasualties(MercenaryContract contract, boolean success) {
        int strength = contract.getAssignedStrength();

        // Casualties are higher on failure
        double casualtyRate = success ? 0.1 : 0.3; // 10% on success, 30% on failure
        casualtyRate += random.nextDouble() * 0.1; // ±10% variance

        return (int) (strength * casualtyRate);
    }

    // ============================================================
    // End-of-Turn Processing
    // ============================================================

    /**
     * Processes all mercenary income for all empires at end-of-turn.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Generates new tickets for each empire</li>
     *   <li>Auto-assigns units to the best available ticket</li>
     *   <li>Resolves active contracts</li>
     *   <li>Applies casualties to units</li>
     * </ul>
     */
    public void processEndOfTurn() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) continue;

            // Find empire for this player
            Empire empire = findEmpireForPlayer(player);
            if (empire == null) continue;

            // Resolve any active contracts
            List<ContractResult> results = resolveContracts(empire);

            // Apply casualties
            for (ContractResult result : results) {
                applyCasualties(empire.getArmy(), result.casualties);
            }

            // Generate new tickets for next turn
            List<MercenaryTicket> tickets = generateTickets(empire);

            // Auto-assign to best ticket if available
            if (!tickets.isEmpty()) {
                MercenaryTicket bestTicket = tickets.stream()
                        .max(Comparator.comparingLong(MercenaryTicket::getTotalPotentialPayment))
                        .orElse(null);
                if (bestTicket != null) {
                    assignToTicket(empire, bestTicket);
                }
            }
        }
    }

    /**
     * Applies casualties to army units.
     */
    private void applyCasualties(EmpireArmy army, int casualties) {
        // Distribute casualties across mercenary units
        List<MercenaryUnit> units = new ArrayList<>(army.getMercenaryUnits());
        int remaining = casualties;

        for (MercenaryUnit unit : units) {
            if (remaining <= 0) break;
            int unitCasualties = Math.min(remaining, unit.getStrength() / 2);
            unit.addCasualties(unitCasualties);
            remaining -= unitCasualties;
        }

        // Remove destroyed units
        army.getMercenaryUnits().removeIf(MercenaryUnit::isDestroyed);
    }

    // ============================================================
    // Query Methods
    // ============================================================

    /**
     * Returns active contracts for an empire.
     *
     * @param empireId the empire ID
     * @return list of active contracts
     */
    public List<MercenaryContract> getActiveContracts(int empireId) {
        return Collections.unmodifiableList(activeContracts.getOrDefault(empireId, Collections.emptyList()));
    }

    /**
     * Returns the contract history (immutable).
     *
     * @return list of completed contracts
     */
    public List<ContractResult> getContractHistory() {
        return Collections.unmodifiableList(contractHistory);
    }

    /**
     * Returns the contract history for an empire.
     *
     * @param empireId the empire ID
     * @return list of contracts for that empire
     */
    public List<ContractResult> getContractHistory(int empireId) {
        return contractHistory.stream()
                .filter(r -> r.empireId == empireId)
                .collect(Collectors.toList());
    }

    /**
     * Calculates total mercenary income for an empire over all completed contracts.
     *
     * @param empireId the empire ID
     * @return total profit in credits
     */
    public long getTotalMercenaryIncome(int empireId) {
        return contractHistory.stream()
                .filter(r -> r.empireId == empireId)
                .mapToLong(r -> r.profit)
                .sum();
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Finds the empire for a player by matching the ruler player ID.
     */
    private Empire findEmpireForPlayer(Player player) {
        for (Empire empire : context.getEmpires().values()) {
            if (empire.getRulerPlayerId() == player.getId()) {
                return empire;
            }
        }
        return null;
    }

    // ============================================================
    // Inner Classes
    // ============================================================

    /**
     * Represents an active mercenary contract.
     */
    public static class MercenaryContract {
        public final int turnAssigned;
        public final int empireId;
        public final MercenaryTicket ticket;
        public final int assignedStrength;
        public final List<MercenaryUnit> assignedUnits;

        public MercenaryContract(int turnAssigned, int empireId, MercenaryTicket ticket,
                                  int assignedStrength, List<MercenaryUnit> assignedUnits) {
            this.turnAssigned = turnAssigned;
            this.empireId = empireId;
            this.ticket = ticket;
            this.assignedStrength = assignedStrength;
            this.assignedUnits = new ArrayList<>(assignedUnits);
        }

        public MercenaryTicket getTicket() {
            return ticket;
        }

        public int getAssignedStrength() {
            return assignedStrength;
        }

        public List<MercenaryUnit> getAssignedUnits() {
            return assignedUnits;
        }

        @Override
        public String toString() {
            return String.format("Contract{turn=%d, empire=%d, %s, strength=%d}",
                    turnAssigned, empireId, ticket.getName(), assignedStrength);
        }
    }

    /**
     * Result of a completed mercenary contract.
     */
    public static class ContractResult {
        public final int turn;
        public final int empireId;
        public final MercenaryTicket ticket;
        public final boolean success;
        public final long profit;
        public final int casualties;
        public final int strength;

        public ContractResult(int turn, int empireId, MercenaryTicket ticket,
                               boolean success, long profit, int casualties, int strength) {
            this.turn = turn;
            this.empireId = empireId;
            this.ticket = ticket;
            this.success = success;
            this.profit = profit;
            this.casualties = casualties;
            this.strength = strength;
        }

        @Override
        public String toString() {
            return String.format("Result{turn=%d, empire=%d, %s, success=%s, profit=%d, casualties=%d}",
                    turn, empireId, ticket.getName(), success, profit, casualties);
        }
    }
}
