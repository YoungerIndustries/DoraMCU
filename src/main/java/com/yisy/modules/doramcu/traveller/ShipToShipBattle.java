package com.yisy.modules.doramcu.traveller;

import java.util.*;

import com.yisy.modules.doramcu.util.DiceUtil;

/**
 * Coordinates a Traveller ship-to-ship battle following Classic Traveller Book 2 rules.
 * <p>
 * A battle progresses through phases each round:
 * </p>
 * <ol>
 *   <li><b>Radar Detection</b> — ships detect targets within radar range</li>
 *   <li><b>Missile Phase</b> — ships fire missiles at detected targets</li>
 *   <li><b>Laser Phase</b> — ships fire lasers at detected targets</li>
 *   <li><b>Damage Resolution</b> — hits are resolved against hit locations</li>
 *   <li><b>System Effects</b> — damaged systems go offline, crew casualties occur</li>
 * </ol>
 * <p>
 * Each phase uses D6 dice rolls for hit determination and damage.
 * The battle ends when one side is completely destroyed or retreats.
 * </p>
 * <p>
 * Hit determination uses the target's maneuver rating as a modifier:
 * the attacker rolls 2D6 and needs to equal or exceed the target's maneuver + 6.
 * </p>
 * <p>
 * Damage is assigned to random hit locations on the target ship.
 * If a critical system is hit, it goes offline. If cargo/passengers are hit,
 * they are lost. If empty space is hit, the ship takes hull point damage.
 * </p>
 */
public class ShipToShipBattle {

    /**
     * The current phase of combat within a round.
     */
    public enum BattlePhase {
        /** Battle has not started. */
        NOT_STARTED,
        /** Radar detection — ships scan for targets. */
        RADAR_DETECTION,
        /** Missile firing phase. */
        MISSILE_PHASE,
        /** Laser firing phase. */
        LASER_PHASE,
        /** Damage resolution and system effects. */
        DAMAGE_RESOLUTION,
        /** Battle has ended. */
        COMPLETED
    }

    /**
     * The outcome of a completed battle.
     */
    public enum BattleOutcome {
        /** Side 0 (attacker) won. */
        SIDE_0_VICTORY,
        /** Side 1 (defender) won. */
        SIDE_1_VICTORY,
        /** Both sides were destroyed. */
        MUTUAL_DESTRUCTION,
        /** Battle ended by retreat. */
        RETREAT,
        /** Battle exceeded maximum rounds. */
        STALEMATE
    }

    /**
     * Represents a single event during the battle.
     */
    public static class BattleEvent {
        private final int round;
        private final BattlePhase phase;
        private final EventType type;
        private final String message;
        private final ShipCombatStatus ship;

        public enum EventType {
            BATTLE_STARTED,
            RADAR_DETECTED,
            RADAR_NOT_DETECTED,
            MISSILE_FIRED,
            MISSILE_HIT,
            MISSILE_MISS,
            LASER_FIRED,
            LASER_HIT,
            LASER_MISS,
            SYSTEM_OFFLINE,
            CREW_CASUALTIES,
            PASSENGER_CASUALTIES,
            HULL_DAMAGE,
            SHIP_DESTROYED,
            SHIP_RETREATED,
            ROUND_COMPLETED,
            BATTLE_ENDED
        }

        BattleEvent(int round, BattlePhase phase, EventType type, String message, ShipCombatStatus ship) {
            this.round = round;
            this.phase = phase;
            this.type = type;
            this.message = message;
            this.ship = ship;
        }

        public int getRound() { return round; }
        public BattlePhase getPhase() { return phase; }
        public EventType getType() { return type; }
        public String getMessage() { return message; }
        public ShipCombatStatus getShip() { return ship; }

        @Override
        public String toString() {
            return String.format("BattleEvent{round=%d, phase=%s, type=%s, msg='%s'}",
                    round, phase, type, message);
        }
    }

    /**
     * Result of a single combat round.
     */
    public static class RoundResult {
        private final int roundNumber;
        private final List<DamageResult> attackResults;
        private final List<BattleEvent> events;
        private final int side0ShipsDestroyed;
        private final int side1ShipsDestroyed;

        RoundResult(int roundNumber, List<DamageResult> attackResults, List<BattleEvent> events,
                    int side0ShipsDestroyed, int side1ShipsDestroyed) {
            this.roundNumber = roundNumber;
            this.attackResults = Collections.unmodifiableList(attackResults);
            this.events = Collections.unmodifiableList(events);
            this.side0ShipsDestroyed = side0ShipsDestroyed;
            this.side1ShipsDestroyed = side1ShipsDestroyed;
        }

        public int getRoundNumber() { return roundNumber; }
        public List<DamageResult> getAttackResults() { return attackResults; }
        public List<BattleEvent> getEvents() { return events; }
        public int getSide0ShipsDestroyed() { return side0ShipsDestroyed; }
        public int getSide1ShipsDestroyed() { return side1ShipsDestroyed; }
    }

    /**
     * Final result of a completed battle.
     */
    public static class BattleResult {
        private final BattleOutcome outcome;
        private final int totalRounds;
        private final List<RoundResult> rounds;
        private final List<BattleEvent> allEvents;
        private final Map<String, Integer> side0Casualties;
        private final Map<String, Integer> side1Casualties;

        BattleResult(BattleOutcome outcome, int totalRounds, List<RoundResult> rounds,
                     List<BattleEvent> allEvents, Map<String, Integer> side0Casualties,
                     Map<String, Integer> side1Casualties) {
            this.outcome = outcome;
            this.totalRounds = totalRounds;
            this.rounds = Collections.unmodifiableList(rounds);
            this.allEvents = Collections.unmodifiableList(allEvents);
            this.side0Casualties = Collections.unmodifiableMap(side0Casualties);
            this.side1Casualties = Collections.unmodifiableMap(side1Casualties);
        }

        public BattleOutcome getOutcome() { return outcome; }
        public int getTotalRounds() { return totalRounds; }
        public List<RoundResult> getRounds() { return rounds; }
        public List<BattleEvent> getAllEvents() { return allEvents; }
        public Map<String, Integer> getSide0Casualties() { return side0Casualties; }
        public Map<String, Integer> getSide1Casualties() { return side1Casualties; }

        @Override
        public String toString() {
            return String.format("BattleResult{outcome=%s, rounds=%d, side0Lost=%d, side1Lost=%d}",
                    outcome, totalRounds,
                    side0Casualties.values().stream().mapToInt(Integer::intValue).sum(),
                    side1Casualties.values().stream().mapToInt(Integer::intValue).sum());
        }
    }

    private final List<ShipCombatStatus> side0Ships;
    private final List<ShipCombatStatus> side1Ships;
    private int maxRounds;

    // Battle state
    private BattlePhase currentPhase;
    private int currentRound;
    private final List<BattleEvent> events;
    private final List<RoundResult> roundResults;
    private boolean battleEnded;

    /**
     * Creates a new ship-to-ship battle.
     *
     * @param side0Ships the ships on side 0 (attacker)
     * @param side1Ships the ships on side 1 (defender)
     */
    public ShipToShipBattle(List<ShipCombatStatus> side0Ships, List<ShipCombatStatus> side1Ships) {
        this.side0Ships = new ArrayList<>(Objects.requireNonNull(side0Ships, "Side 0 ships must not be null"));
        this.side1Ships = new ArrayList<>(Objects.requireNonNull(side1Ships, "Side 1 ships must not be null"));
        if (side0Ships.isEmpty()) {
            throw new IllegalArgumentException("Side 0 must have at least one ship");
        }
        if (side1Ships.isEmpty()) {
            throw new IllegalArgumentException("Side 1 must have at least one ship");
        }
        this.maxRounds = 50;
        this.currentPhase = BattlePhase.NOT_STARTED;
        this.currentRound = 0;
        this.events = new ArrayList<>();
        this.roundResults = new ArrayList<>();
        this.battleEnded = false;
    }

    /**
     * Sets the maximum number of rounds before stalemate.
     *
     * @param maxRounds the maximum rounds
     */
    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    // ---- Battle lifecycle ----

    /**
     * Starts the battle.
     */
    public void start() {
        if (currentPhase != BattlePhase.NOT_STARTED) {
            throw new IllegalStateException("Battle already started");
        }
        currentPhase = BattlePhase.RADAR_DETECTION;
        addEvent(0, BattlePhase.NOT_STARTED, BattleEvent.EventType.BATTLE_STARTED,
                String.format("Battle started: %d ships vs %d ships",
                        side0Ships.size(), side1Ships.size()), null);
    }

    /**
     * Fights a single round of combat through all phases.
     *
     * @return the result of this round
     */
    public RoundResult fightRound() {
        if (currentPhase == BattlePhase.NOT_STARTED) {
            start();
        }
        if (battleEnded) {
            return null;
        }

        currentRound++;

        // Check for stalemate
        if (currentRound > maxRounds) {
            endBattle(BattleOutcome.STALEMATE);
            return null;
        }

        List<DamageResult> roundAttacks = new ArrayList<>();
        int side0Before = countActiveShips(side0Ships);
        int side1Before = countActiveShips(side1Ships);

        // Phase 1: Radar Detection
        currentPhase = BattlePhase.RADAR_DETECTION;
        performRadarDetection();

        // Phase 2: Missile Phase
        currentPhase = BattlePhase.MISSILE_PHASE;
        roundAttacks.addAll(performMissilePhase());

        // Phase 3: Laser Phase
        currentPhase = BattlePhase.LASER_PHASE;
        roundAttacks.addAll(performLaserPhase());

        // Phase 4: Damage Resolution
        currentPhase = BattlePhase.DAMAGE_RESOLUTION;
        resolveDamage(roundAttacks);

        // Check for battle end
        int side0After = countActiveShips(side0Ships);
        int side1After = countActiveShips(side1Ships);

        if (side0After == 0 && side1After == 0) {
            endBattle(BattleOutcome.MUTUAL_DESTRUCTION);
        } else if (side0After == 0) {
            endBattle(BattleOutcome.SIDE_1_VICTORY);
        } else if (side1After == 0) {
            endBattle(BattleOutcome.SIDE_0_VICTORY);
        }

        currentPhase = BattlePhase.COMPLETED;

        RoundResult result = new RoundResult(
                currentRound, roundAttacks, new ArrayList<>(events),
                side0Before - side0After, side1Before - side1After
        );
        roundResults.add(result);
        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION, BattleEvent.EventType.ROUND_COMPLETED,
                String.format("Round %d completed", currentRound), null);

        return result;
    }

    /**
     * Fights the battle to completion.
     *
     * @return the final battle result
     */
    public BattleResult fightToCompletion() {
        while (!battleEnded) {
            RoundResult round = fightRound();
            if (round == null) break;
        }
        return getBattleResult();
    }

    // ---- Combat phases ----

    /**
     * Performs radar detection for all ships.
     * Ships detect targets within their radar range.
     */
    private void performRadarDetection() {
        // Each ship detects enemies within radar range
        // In a simplified model, all ships detect all enemies unless radar is damaged
        for (ShipCombatStatus ship : getAllActiveShips()) {
            List<ShipCombatStatus> enemies = getEnemyShips(ship);
            boolean detected = false;
            for (ShipCombatStatus enemy : enemies) {
                if (enemy.isDestroyed()) continue;
                // Detection check: roll vs radar range
                // Higher radar range = easier detection
                int detectionRoll = DiceUtil.roll2D6();
                int detectionThreshold = 8 - ship.getCurrentRadarRange();
                if (detectionRoll >= detectionThreshold) {
                    detected = true;
                    addEvent(currentRound, BattlePhase.RADAR_DETECTION, BattleEvent.EventType.RADAR_DETECTED,
                            String.format("%s detects %s", ship.getName(), enemy.getName()), ship);
                } else {
                    addEvent(currentRound, BattlePhase.RADAR_DETECTION, BattleEvent.EventType.RADAR_NOT_DETECTED,
                            String.format("%s fails to detect %s", ship.getName(), enemy.getName()), ship);
                }
            }
        }
    }

    /**
     * Performs the missile firing phase.
     * Ships with functional missile launchers and remaining pods fire missiles.
     *
     * @return list of damage results from missile attacks
     */
    private List<DamageResult> performMissilePhase() {
        List<DamageResult> results = new ArrayList<>();

        for (ShipCombatStatus ship : getAllActiveShips()) {
            if (ship.isDestroyed() || ship.getMissilePodsRemaining() <= 0) continue;
            if (ship.getFunctionalMissileLaunchers() <= 0) continue;

            List<ShipCombatStatus> enemies = getActiveEnemyShips(ship);
            if (enemies.isEmpty()) continue;

            // Fire one missile pod per launcher
            int launchers = ship.getFunctionalMissileLaunchers();
            for (int i = 0; i < launchers && ship.getMissilePodsRemaining() > 0; i++) {
                ShipCombatStatus target = selectTarget(ship, enemies);
                if (target == null) continue;

                ship.consumeMissilePod();
                addEvent(currentRound, BattlePhase.MISSILE_PHASE, BattleEvent.EventType.MISSILE_FIRED,
                        String.format("%s fires missile at %s", ship.getName(), target.getName()), ship);

                // Hit determination: 2D6 vs target maneuver + 6
                int hitRoll = DiceUtil.roll2D6();
                int hitThreshold = target.getCurrentManeuver() + 6;

                if (hitRoll >= hitThreshold) {
                    // Hit! Determine damage
                    int damageDice = calculateMissileDamageDice(ship);
                    int damage = rollDice(damageDice);
                    HitLocation hitLoc = randomHitLocation();

                    DamageResult result = DamageResult.builder()
                            .attacker(ship)
                            .target(target)
                            .isHit(true)
                            .hitLocation(hitLoc)
                            .damageDice(damageDice)
                            .damageRolled(damage)
                            .weaponType("Missile")
                            .description(String.format("%s missile hits %s at %s for %d MD",
                                    ship.getName(), target.getName(), hitLoc.getLabel(), damage))
                            .build();
                    results.add(result);

                    addEvent(currentRound, BattlePhase.MISSILE_PHASE, BattleEvent.EventType.MISSILE_HIT,
                            result.getDescription(), target);
                } else {
                    DamageResult result = DamageResult.builder()
                            .attacker(ship)
                            .target(target)
                            .isHit(false)
                            .weaponType("Missile")
                            .description(String.format("%s missile misses %s", ship.getName(), target.getName()))
                            .build();
                    results.add(result);

                    addEvent(currentRound, BattlePhase.MISSILE_PHASE, BattleEvent.EventType.MISSILE_MISS,
                            result.getDescription(), target);
                }
            }
        }

        return results;
    }

    /**
     * Performs the laser firing phase.
     * Ships with functional lasers fire at detected targets.
     *
     * @return list of damage results from laser attacks
     */
    private List<DamageResult> performLaserPhase() {
        List<DamageResult> results = new ArrayList<>();

        for (ShipCombatStatus ship : getAllActiveShips()) {
            if (ship.isDestroyed()) continue;
            if (ship.getFunctionalLaserBanks() <= 0) continue;

            List<ShipCombatStatus> enemies = getActiveEnemyShips(ship);
            if (enemies.isEmpty()) continue;

            ShipCombatStatus target = selectTarget(ship, enemies);
            if (target == null) continue;

            // Fire all functional lasers
            int damageDice = ship.getFunctionalLaserDamageDice();
            if (damageDice <= 0) continue;

            addEvent(currentRound, BattlePhase.LASER_PHASE, BattleEvent.EventType.LASER_FIRED,
                    String.format("%s fires lasers at %s (%dD6)", ship.getName(), target.getName(), damageDice), ship);

            // Hit determination: 2D6 vs target maneuver + 6
            int hitRoll = DiceUtil.roll2D6();
            int hitThreshold = target.getCurrentManeuver() + 6;

            if (hitRoll >= hitThreshold) {
                // Hit! Determine damage
                int damage = rollDice(damageDice);
                HitLocation hitLoc = randomHitLocation();

                DamageResult result = DamageResult.builder()
                        .attacker(ship)
                        .target(target)
                        .isHit(true)
                        .hitLocation(hitLoc)
                        .damageDice(damageDice)
                        .damageRolled(damage)
                        .weaponType("Laser")
                        .description(String.format("%s laser hits %s at %s for %d MD",
                                ship.getName(), target.getName(), hitLoc.getLabel(), damage))
                        .build();
                results.add(result);

                addEvent(currentRound, BattlePhase.LASER_PHASE, BattleEvent.EventType.LASER_HIT,
                        result.getDescription(), target);
            } else {
                DamageResult result = DamageResult.builder()
                        .attacker(ship)
                        .target(target)
                        .isHit(false)
                        .weaponType("Laser")
                        .description(String.format("%s laser misses %s", ship.getName(), target.getName()))
                        .build();
                results.add(result);

                addEvent(currentRound, BattlePhase.LASER_PHASE, BattleEvent.EventType.LASER_MISS,
                        result.getDescription(), target);
            }
        }

        return results;
    }

    /**
     * Resolves all damage from attack results.
     *
     * @param results the list of damage results to resolve
     */
    private void resolveDamage(List<DamageResult> results) {
        for (DamageResult result : results) {
            if (!result.isHit()) continue;

            ShipCombatStatus target = result.getTarget();
            HitLocation hitLoc = result.getHitLocation();
            int damage = result.getDamageRolled();

            // Record the hit on the location
            target.recordHit(hitLoc);

            // Apply effects based on hit location system type
            HitLocation.SystemType systemType = hitLoc.getDefaultSystem();

            switch (systemType) {
                case JUMP_DRIVE:
                    if (target.isSystemOffline(hitLoc)) {
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.SYSTEM_OFFLINE,
                                String.format("%s jump drive goes offline!", target.getName()), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case FUSION_DRIVE:
                    if (target.isSystemOffline(hitLoc)) {
                        target.reduceManeuver(1);
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.SYSTEM_OFFLINE,
                                String.format("%s fusion drive damaged! Maneuver reduced.", target.getName()), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case POWER_PLANT:
                    if (target.isSystemOffline(hitLoc)) {
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.SYSTEM_OFFLINE,
                                String.format("%s power plant goes offline! Weapons disabled.", target.getName()), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case LIFE_SUPPORT:
                    if (target.isSystemOffline(hitLoc)) {
                        int crewDeaths = rollDice(target.getDesign().getLifeSupport().getCrewDeathDicePerHit());
                        target.recordCrewCasualties(crewDeaths);
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.CREW_CASUALTIES,
                                String.format("%s life support damaged! %d crew lost.", target.getName(), crewDeaths), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case COMPUTER:
                    if (target.isSystemOffline(hitLoc)) {
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.SYSTEM_OFFLINE,
                                String.format("%s computer goes offline! Targeting accuracy reduced.", target.getName()), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case CARGO:
                    // Cargo loss — no hull damage, but cargo is lost
                    int cargoLost = Math.min(damage * 100, target.getDesign().getCargoTons());
                    addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                            BattleEvent.EventType.HULL_DAMAGE,
                            String.format("%s cargo hit! %d tons of cargo lost.", target.getName(), cargoLost), target);
                    break;

                case PASSENGERS:
                    int passengerDeaths = damage / 2;
                    target.recordPassengerCasualties(passengerDeaths);
                    addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                            BattleEvent.EventType.PASSENGER_CASUALTIES,
                            String.format("%s passenger quarters hit! %d passengers lost.", target.getName(), passengerDeaths), target);
                    break;

                case LASER:
                    if (target.isSystemOffline(hitLoc)) {
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.SYSTEM_OFFLINE,
                                String.format("%s laser battery at %s goes offline!", target.getName(), hitLoc.getLabel()), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case MISSILE:
                    if (target.isSystemOffline(hitLoc)) {
                        addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                                BattleEvent.EventType.SYSTEM_OFFLINE,
                                String.format("%s missile launcher at %s goes offline!", target.getName(), hitLoc.getLabel()), target);
                    }
                    target.applyHullDamage(damage);
                    break;

                case HULL:
                case CREW:
                case EMPTY:
                default:
                    // Hull damage
                    target.applyHullDamage(damage);
                    addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                            BattleEvent.EventType.HULL_DAMAGE,
                            String.format("%s takes %d hull damage at %s", target.getName(), damage, hitLoc.getLabel()), target);
                    break;
            }

            // Check if ship is destroyed
            if (target.isDestroyed()) {
                addEvent(currentRound, BattlePhase.DAMAGE_RESOLUTION,
                        BattleEvent.EventType.SHIP_DESTROYED,
                        String.format("%s is destroyed!", target.getName()), target);
            }
        }
    }

    // ---- Helper methods ----

    private List<ShipCombatStatus> getAllActiveShips() {
        List<ShipCombatStatus> all = new ArrayList<>();
        all.addAll(side0Ships);
        all.addAll(side1Ships);
        all.removeIf(ShipCombatStatus::isDestroyed);
        return all;
    }

    private List<ShipCombatStatus> getEnemyShips(ShipCombatStatus ship) {
        if (ship.getSide() == 0) {
            return side1Ships;
        } else {
            return side0Ships;
        }
    }

    private List<ShipCombatStatus> getActiveEnemyShips(ShipCombatStatus ship) {
        List<ShipCombatStatus> enemies = getEnemyShips(ship);
        enemies.removeIf(ShipCombatStatus::isDestroyed);
        return enemies;
    }

    private ShipCombatStatus selectTarget(ShipCombatStatus attacker, List<ShipCombatStatus> enemies) {
        if (enemies.isEmpty()) return null;
        // Simple target selection: highest hull points (biggest threat)
        return enemies.stream()
                .max(Comparator.comparingInt(ShipCombatStatus::getHullPoints))
                .orElse(null);
    }

    private HitLocation randomHitLocation() {
        HitLocation[] locations = HitLocation.values();
        return locations[DiceUtil.rollIndex(locations.length)];
    }

    private int calculateMissileDamageDice(ShipCombatStatus ship) {
        for (ShipWeaponSystem weapon : ship.getDesign().getWeapons()) {
            if (weapon.isMissile() && weapon.getMissileType() != null) {
                return weapon.getMissileType().getDamageDice() * weapon.getQuantity();
            }
        }
        return 2; // Default missile damage
    }

    private int rollDice(int dice) {
        return DiceUtil.rollNdX(dice, 6);
    }

    private int countActiveShips(List<ShipCombatStatus> ships) {
        return (int) ships.stream().filter(s -> !s.isDestroyed()).count();
    }

    private void addEvent(int round, BattlePhase phase, BattleEvent.EventType type, String message, ShipCombatStatus ship) {
        events.add(new BattleEvent(round, phase, type, message, ship));
    }

    private void endBattle(BattleOutcome outcome) {
        battleEnded = true;
        currentPhase = BattlePhase.COMPLETED;
        addEvent(currentRound, BattlePhase.COMPLETED, BattleEvent.EventType.BATTLE_ENDED,
                String.format("Battle ended: %s", outcome), null);
    }

    private BattleResult getBattleResult() {
        Map<String, Integer> side0Casualties = new HashMap<>();
        Map<String, Integer> side1Casualties = new HashMap<>();

        for (ShipCombatStatus ship : side0Ships) {
            if (ship.isDestroyed()) {
                side0Casualties.put(ship.getName(), 1);
            }
        }
        for (ShipCombatStatus ship : side1Ships) {
            if (ship.isDestroyed()) {
                side1Casualties.put(ship.getName(), 1);
            }
        }

        BattleOutcome outcome;
        int side0Active = countActiveShips(side0Ships);
        int side1Active = countActiveShips(side1Ships);

        if (side0Active == 0 && side1Active == 0) {
            outcome = BattleOutcome.MUTUAL_DESTRUCTION;
        } else if (side0Active == 0) {
            outcome = BattleOutcome.SIDE_1_VICTORY;
        } else if (side1Active == 0) {
            outcome = BattleOutcome.SIDE_0_VICTORY;
        } else {
            outcome = BattleOutcome.STALEMATE;
        }

        return new BattleResult(outcome, currentRound, roundResults, events,
                side0Casualties, side1Casualties);
    }

    // ---- Accessors ----

    /** Returns the current battle phase. */
    public BattlePhase getCurrentPhase() {
        return currentPhase;
    }

    /** Returns the current round number. */
    public int getCurrentRound() {
        return currentRound;
    }

    /** Returns whether the battle has ended. */
    public boolean isBattleEnded() {
        return battleEnded;
    }

    /** Returns the list of all battle events. */
    public List<BattleEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /** Returns the ships on side 0. */
    public List<ShipCombatStatus> getSide0Ships() {
        return Collections.unmodifiableList(side0Ships);
    }

    /** Returns the ships on side 1. */
    public List<ShipCombatStatus> getSide1Ships() {
        return Collections.unmodifiableList(side1Ships);
    }

    @Override
    public String toString() {
        return String.format("ShipToShipBattle{round=%d, phase=%s, side0=%d ships, side1=%d ships, ended=%s}",
                currentRound, currentPhase, side0Ships.size(), side1Ships.size(), battleEnded);
    }
}
