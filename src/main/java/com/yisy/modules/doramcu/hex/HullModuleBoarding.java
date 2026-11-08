package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * Hull module boarding combat system.
 * <p>
 * Uses DoraMCU's HullModule 3D grid (10×10×10 cells) for room-to-room
 * boarding combat from Traveller's BoardingAction system.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Room-to-room combat on HullModule grid</li>
 *   <li>Marine unit movement through cells</li>
 *   <li>Key component capture (bridge, engine rooms)</li>
 *   <li>Point defense against boarding shuttles</li>
 *   <li>Victory by capturing critical components</li>
 * </ul>
 */
public class HullModuleBoarding {

    /**
     * Represents a room in the HullModule grid.
     */
    public static class Room {
        private final M3space cell;
        private final HullModule module;
        private MarineUnit occupyingMarines;
        private boolean isCaptured;
        private boolean isCritical;

        Room(HullModule module, int x, int y, int z) {
            this.module = module;
            this.cell = module.get(x, y, z);
            this.occupyingMarines = null;
            this.isCaptured = false;
            this.isCritical = false;
        }

        public M3space getCell() {
            return cell;
        }

        public HullModule getModule() {
            return module;
        }

        public MarineUnit getOccupyingMarines() {
            return occupyingMarines;
        }

        public void setOccupyingMarines(MarineUnit marines) {
            this.occupyingMarines = marines;
        }

        public boolean isCaptured() {
            return isCaptured;
        }

        public void setCaptured(boolean captured) {
            this.isCaptured = captured;
        }

        public boolean isCritical() {
            return isCritical;
        }

        public void setCritical(boolean critical) {
            this.isCritical = critical;
        }

        public boolean isEmpty() {
            return cell == null || cell.getComponent() == null;
        }

        public boolean hasComponent() {
            return cell != null && cell.getComponent() != null;
        }

        @Override
        public String toString() {
            return String.format("Room{module=%s, cell=(%d,%d,%d), captured=%s, critical=%s}",
                    module.getName(), cell != null ? cell.getX() : -1,
                    cell != null ? cell.getY() : -1, cell != null ? cell.getZ() : -1,
                    isCaptured, isCritical);
        }
    }

    private final UnifiedShip targetShip;
    private final UnifiedShip attackerShip;
    private final List<MarineUnit> attackerMarines;
    private final MarineUnit defenderMarines;
    private final int pointDefense;
    private final List<Room> criticalRooms;
    private final List<Room> allRooms;
    private final List<String> eventLog;
    private BoardingAction.Outcome outcome;
    private int combatRounds;
    private final Random random;

    /**
     * Creates a new hull module boarding action.
     *
     * @param attackerShip the ship launching the boarding action
     * @param targetShip the ship being boarded
     * @param attackerMarines the attacking marine units
     * @param defenderMarines the defending marine unit
     * @param pointDefense the defender's point defense rating
     */
    public HullModuleBoarding(UnifiedShip attackerShip, UnifiedShip targetShip,
                               List<MarineUnit> attackerMarines,
                               MarineUnit defenderMarines,
                               int pointDefense) {
        this.attackerShip = Objects.requireNonNull(attackerShip, "Attacker ship must not be null");
        this.targetShip = Objects.requireNonNull(targetShip, "Target ship must not be null");
        this.attackerMarines = new ArrayList<>(Objects.requireNonNull(attackerMarines, "Attacker marines must not be null"));
        this.defenderMarines = defenderMarines;
        this.pointDefense = Math.max(1, Math.min(12, pointDefense));
        this.criticalRooms = new ArrayList<>();
        this.allRooms = new ArrayList<>();
        this.eventLog = new ArrayList<>();
        this.outcome = null;
        this.combatRounds = 0;
        this.random = new Random();

        // Identify critical rooms (cells with components)
        identifyCriticalRooms();
    }

    /**
     * Identifies critical rooms in the target ship's hull modules.
     */
    private void identifyCriticalRooms() {
        List<HullModule> modules = targetShip.getHullModules();
        if (modules == null || modules.isEmpty()) return;

        for (HullModule module : modules) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    for (int z = 0; z < 10; z++) {
                        M3space cell = module.get(x, y, z);
                        if (cell != null && cell.getComponent() != null) {
                            Room room = new Room(module, x, y, z);
                            room.setCritical(true);
                            criticalRooms.add(room);
                            allRooms.add(room);
                        }
                    }
                }
            }
        }

        // If no critical rooms found, use center cells as fallback
        if (criticalRooms.isEmpty()) {
            for (HullModule module : modules) {
                Room centerRoom = new Room(module, 4, 4, 4);
                centerRoom.setCritical(true);
                criticalRooms.add(centerRoom);
                allRooms.add(centerRoom);
            }
        }

        logEvent("Identified " + criticalRooms.size() + " critical rooms");
    }

    /**
     * Executes the full boarding action.
     *
     * @return the outcome
     */
    public BoardingAction.Outcome execute() {
        logEvent("Boarding action initiated: " + attackerShip.getName() + " -> " + targetShip.getName());
        logEvent("Attacker marines: " + attackerMarines.size() + " units");

        // Phase 1: Approach - point defense fires
        if (!resolveApproach()) {
            outcome = BoardingAction.Outcome.REPELLED;
            logEvent("All marines destroyed in approach");
            return outcome;
        }

        // Phase 2: Room-to-room combat
        resolveCombat();

        // Phase 3: Resolution
        resolveOutcome();
        return outcome;
    }

    /**
     * Resolves the approach phase: point defense fires at marines.
     *
     * @return true if any marines survived
     */
    private boolean resolveApproach() {
        logEvent("Phase: APPROACH — Point defenses engage");

        for (MarineUnit unit : attackerMarines) {
            int pdRoll = random.nextInt(12) + 2; // 2D6
            int defense = unit.getEquipment().getBonus() + unit.getMorale().getBonus();
            int hitThreshold = 10 - defense;

            if (pdRoll >= hitThreshold) {
                int casualties = Math.max(1, pointDefense / 3);
                unit.applyCasualties(casualties);
                logEvent(unit.getName() + " takes " + casualties + " casualties from point defense");
            }
        }

        // Check if any marines survived
        for (MarineUnit unit : attackerMarines) {
            if (unit.isCombatEffective()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the room-to-room combat phase.
     */
    private void resolveCombat() {
        logEvent("Phase: COMBAT — Room-to-room fighting begins");

        // Assign attacker marines to rooms
        int marineIndex = 0;
        for (Room room : criticalRooms) {
            if (marineIndex < attackerMarines.size() && attackerMarines.get(marineIndex).isCombatEffective()) {
                room.setOccupyingMarines(attackerMarines.get(marineIndex));
                marineIndex++;
            }
        }

        // Defender distributes to rooms
        if (defenderMarines != null && defenderMarines.isCombatEffective()) {
            // Defender splits among critical rooms
            int perRoom = defenderMarines.getRemainingMarines() / Math.max(1, criticalRooms.size());
            for (Room room : criticalRooms) {
                if (perRoom > 0) {
                    MarineUnit defenderSquad = new MarineUnit(
                            "Defender Squad",
                            defenderMarines.getCombatRating(),
                            perRoom,
                            defenderMarines.getEquipment(),
                            defenderMarines.getMorale(),
                            defenderMarines.getBreachingCapability()
                    );
                    // Fight in this room
                    fightRoom(room, room.getOccupyingMarines(), defenderSquad);
                }
            }
        }

        // Fight up to 10 rounds
        for (int round = 1; round <= 10; round++) {
            combatRounds = round;

            boolean allRoomsCaptured = true;
            for (Room room : criticalRooms) {
                if (!room.isCaptured() && room.getOccupyingMarines() != null) {
                    allRoomsCaptured = false;
                    break;
                }
            }
            if (allRoomsCaptured) break;

            // Check if attackers are all dead
            boolean attackersAlive = false;
            for (MarineUnit unit : attackerMarines) {
                if (unit.isCombatEffective()) {
                    attackersAlive = true;
                    break;
                }
            }
            if (!attackersAlive) break;

            // Morale checks
            for (MarineUnit unit : attackerMarines) {
                if (unit.isCombatEffective() && !unit.moraleCheck(random)) {
                    logEvent(unit.getName() + " breaks and retreats!");
                }
            }
        }
    }

    /**
     * Fights combat in a single room.
     */
    private void fightRoom(Room room, MarineUnit attacker, MarineUnit defender) {
        if (attacker == null || !attacker.isCombatEffective()) {
            return;
        }

        logEvent("Room combat: " + room);

        // Attacker vs defender
        double attackerPower = attacker.getEffectiveCombatPower();
        double defenderPower = defender != null ? defender.getEffectiveCombatPower() : 0;

        if (defenderPower <= 0) {
            // No defender — room captured
            room.setCaptured(true);
            logEvent(room + " captured by " + attacker.getName());
            return;
        }

        // Combat resolution
        int attackerRoll = random.nextInt(12) + 2;
        if (attackerRoll * attackerPower > 7 * defenderPower) {
            int casualties = Math.max(1, (int) (attackerPower / 3));
            defender.applyCasualties(casualties);
            logEvent(attacker.getName() + " inflicts " + casualties + " casualties in " + room);

            if (!defender.isCombatEffective()) {
                room.setCaptured(true);
                logEvent(room + " captured by " + attacker.getName());
            }
        } else {
            // Defender holds
            int casualties = Math.max(1, (int) (defenderPower / 3));
            attacker.applyCasualties(casualties);
            logEvent("Defender inflicts " + casualties + " casualties in " + room);
        }
    }

    /**
     * Determines the final outcome.
     */
    private void resolveOutcome() {
        // Check if all critical rooms are captured
        int capturedRooms = 0;
        for (Room room : criticalRooms) {
            if (room.isCaptured()) capturedRooms++;
        }

        // Check if attackers are defeated
        boolean attackersAlive = false;
        for (MarineUnit unit : attackerMarines) {
            if (unit.isCombatEffective()) {
                attackersAlive = true;
                break;
            }
        }

        if (!attackersAlive) {
            outcome = BoardingAction.Outcome.REPELLED;
            logEvent("Outcome: Boarding repelled — all marines eliminated");
            return;
        }

        if (capturedRooms >= criticalRooms.size() * 0.7) {
            outcome = BoardingAction.Outcome.CAPTURED;
            logEvent("Outcome: Ship captured — " + capturedRooms + "/" + criticalRooms.size() + " rooms taken");
        } else if (capturedRooms >= criticalRooms.size() * 0.3) {
            outcome = BoardingAction.Outcome.MUTUAL_DESTRUCTION;
            logEvent("Outcome: Mutual destruction — partial capture with heavy losses");
        } else {
            outcome = BoardingAction.Outcome.REPELLED;
            logEvent("Outcome: Boarding repelled — only " + capturedRooms + "/" + criticalRooms.size() + " rooms taken");
        }
    }

    /**
     * Returns the list of critical rooms.
     */
    public List<Room> getCriticalRooms() {
        return Collections.unmodifiableList(criticalRooms);
    }

    /**
     * Returns the outcome, or null if not completed.
     */
    public BoardingAction.Outcome getOutcome() {
        return outcome;
    }

    /**
     * Returns the number of combat rounds.
     */
    public int getCombatRounds() {
        return combatRounds;
    }

    /**
     * Returns the event log.
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }

    @Override
    public String toString() {
        return String.format("HullModuleBoarding{attacker=%s, target=%s, outcome=%s, rounds=%d}",
                attackerShip.getName(), targetShip.getName(),
                outcome != null ? outcome.getDescription() : "pending", combatRounds);
    }
}
