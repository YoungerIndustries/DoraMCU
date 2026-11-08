package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.dto.*;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCombatRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogMissionRepository;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogStarshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles enhanced starship combat for the Captain's Log Solo RPG.
 * <p>
 * Starship combat follows the official Captain's Log rules with:
 * <ul>
 *   <li><b>Range Bands</b> — Long, Mid, Short. Ships must be in range to attack.</li>
 *   <li><b>System Attacks</b> — Weapons attacks target specific systems (Structure, Weapons, Sensors, etc.)</li>
 *   <li><b>Evasion</b> — Target ship can attempt to evade using Engines + Operations</li>
 *   <li><b>Critical Hits</b> — Excess successes cause critical damage to systems</li>
 *   <li><b>Maneuvering</b> — Ships can change range bands using Engines</li>
 *   <li><b>System Stress</b> — Failed defense adds stress to targeted systems</li>
 * </ul>
 * </p>
 * <p>
 * Combat flow:
 * <ol>
 *   <li>Start combat with two ships facing off</li>
 *   <li>Each round: attacker chooses action (attack, maneuver, repair)</li>
 *   <li>Attack: roll Weapons + Security vs. target's Engines + Operations (evasion)</li>
 *   <li>On hit: deal stress to targeted system; excess successes = critical hit</li>
 *   <li>Maneuver: change range band (Long→Mid→Short or reverse)</li>
 *   <li>Repair: reduce stress on a system</li>
 *   <li>Combat ends when one ship is destroyed (all systems at max stress) or retreats</li>
 * </ol>
 * </p>
 */
@Component
public class StarshipCombatService {

    private static final Logger log = LoggerFactory.getLogger(StarshipCombatService.class);

    private final CaptainLogCombatRepository combatRepository;
    private final CaptainLogMissionRepository missionRepository;
    private final CaptainLogCharacterRepository characterRepository;
    private final CaptainLogStarshipRepository starshipRepository;
    private final DiceService diceService;
    private final ProbabilityMatrixService probabilityMatrixService;

    public StarshipCombatService(CaptainLogCombatRepository combatRepository,
                                  CaptainLogMissionRepository missionRepository,
                                  CaptainLogCharacterRepository characterRepository,
                                  CaptainLogStarshipRepository starshipRepository,
                                  DiceService diceService,
                                  ProbabilityMatrixService probabilityMatrixService) {
        this.combatRepository = combatRepository;
        this.missionRepository = missionRepository;
        this.characterRepository = characterRepository;
        this.starshipRepository = starshipRepository;
        this.diceService = diceService;
        this.probabilityMatrixService = probabilityMatrixService;
    }

    // ==================== Range Bands ====================

    /**
     * Range bands in starship combat.
     */
    public enum RangeBand {
        LONG("Long Range", "Phasers only. Torpedoes cannot be fired.", 1),
        MID("Mid Range", "Phasers and torpedoes available.", 2),
        SHORT("Short Range", "All weapons. Evasion more difficult.", 3);

        private final String displayName;
        private final String description;
        private final int value;

        RangeBand(String displayName, String description, int value) {
            this.displayName = displayName;
            this.description = description;
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public int getValue() {
            return value;
        }
    }

    // ==================== Combat Actions ====================

    /**
     * Actions available during starship combat.
     */
    public enum CombatAction {
        ATTACK("Attack", "Fire weapons at the enemy ship.",
                "Weapons + Security", "Engines + Operations"),
        MANEUVER("Maneuver", "Change range band to gain tactical advantage.",
                "Engines + Operations", "Engines + Operations"),
        REPAIR("Repair", "Reduce stress on a damaged system.",
                "Engines + Engineering", null),
        EVASIVE("Evasive Maneuvers", "Commit to evasion for the next round, gaining a boon on defense.",
                "Engines + Operations", null),
        SHIELD_RECHARGE("Shield Recharge", "Redirect power to shields, reducing structure stress.",
                "Computers + Engineering", null),
        TORPEDO("Torpedo Attack", "Fire torpedoes — higher damage but only at Mid or Short range.",
                "Weapons + Security", "Engines + Operations");

        private final String name;
        private final String description;
        private final String attackerStats;
        private final String defenderStats;

        CombatAction(String name, String description, String attackerStats, String defenderStats) {
            this.name = name;
            this.description = description;
            this.attackerStats = attackerStats;
            this.defenderStats = defenderStats;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getAttackerStats() {
            return attackerStats;
        }

        public String getDefenderStats() {
            return defenderStats;
        }
    }

    // ==================== Starship Combat State ====================

    /**
     * Tracks the state of a starship during combat.
     */
    public static class StarshipCombatState {
        private final Long starshipId;
        private String name;
        private CaptainLogStarship.Scale scale;
        private int maxHits;
        private int currentHits;
        private RangeBand rangeBand;
        private boolean evasiveManeuvers;
        private final Map<CaptainLogStarship.StarshipSystem, Integer> systemStress;

        public StarshipCombatState(CaptainLogStarship ship) {
            this.starshipId = ship.getId();
            this.name = ship.getName();
            this.scale = ship.getScale();
            this.maxHits = ship.getMaxHits();
            this.currentHits = maxHits;
            this.rangeBand = RangeBand.LONG;
            this.evasiveManeuvers = false;
            this.systemStress = new HashMap<>();
            for (CaptainLogStarship.StarshipSystem sys : CaptainLogStarship.StarshipSystem.values()) {
                this.systemStress.put(sys, ship.getSystemStress(sys));
            }
        }

        public Long getStarshipId() { return starshipId; }
        public String getName() { return name; }
        public CaptainLogStarship.Scale getScale() { return scale; }
        public int getMaxHits() { return maxHits; }
        public int getCurrentHits() { return currentHits; }
        public RangeBand getRangeBand() { return rangeBand; }
        public void setRangeBand(RangeBand rangeBand) { this.rangeBand = rangeBand; }
        public boolean isEvasiveManeuvers() { return evasiveManeuvers; }
        public void setEvasiveManeuvers(boolean evasive) { this.evasiveManeuvers = evasive; }
        public Map<CaptainLogStarship.StarshipSystem, Integer> getSystemStress() { return systemStress; }
        public int getSystemStress(CaptainLogStarship.StarshipSystem sys) { return systemStress.getOrDefault(sys, 0); }
        public void setSystemStress(CaptainLogStarship.StarshipSystem sys, int stress) { this.systemStress.put(sys, stress); }

        public boolean isDestroyed() {
            return currentHits <= 0 || isAllSystemsFailed();
        }

        public boolean isAllSystemsFailed() {
            int failedCount = 0;
            for (CaptainLogStarship.StarshipSystem sys : CaptainLogStarship.StarshipSystem.values()) {
                int rating = getSystemRating(sys);
                if (rating > 0 && systemStress.getOrDefault(sys, 0) >= rating) {
                    failedCount++;
                }
            }
            return failedCount >= 3; // 3 systems failed = ship destroyed
        }

        private int getSystemRating(CaptainLogStarship.StarshipSystem sys) {
            // Default ratings based on scale
            int base = switch (scale) {
                case SMALL -> 2;
                case MEDIUM -> 3;
                case LARGE -> 4;
                case CAPITAL -> 5;
                case STATION -> 6;
            };
            return Math.max(base, systemStress.getOrDefault(sys, 0)); // stress can't exceed rating
        }
    }

    // ==================== Combat Session ====================

    /**
     * In-memory session for active starship combats.
     * Stores the combat state that isn't persisted to the database.
     */
    private final Map<Long, StarshipCombatSession> activeSessions = new HashMap<>();

    public static class StarshipCombatSession {
        final StarshipCombatState playerShip;
        final StarshipCombatState enemyShip;
        int currentRound;
        boolean playerTurn; // true = player's turn, false = enemy's turn
        final List<String> combatLog;

        StarshipCombatSession(StarshipCombatState player, StarshipCombatState enemy) {
            this.playerShip = player;
            this.enemyShip = enemy;
            this.currentRound = 0;
            this.playerTurn = true;
            this.combatLog = new ArrayList<>();
        }
    }

    // ==================== Start Starship Combat ====================

    /**
     * Starts an enhanced starship combat encounter.
     * <p>
     * Creates a combat entity and initializes the combat session with range bands,
     * system stress tracking, and turn order.
     * </p>
     */
    @Transactional
    public Map<String, Object> startStarshipCombat(StartCombatRequest request) {
        CaptainLogMission mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + request.getMissionId()));
        CaptainLogCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        // Get player's starship
        CaptainLogStarship playerShip = character.getAssignedStarship();
        if (playerShip == null) {
            throw new IllegalStateException("Character has no assigned starship. Assign a ship first.");
        }

        // Create combat entity
        CaptainLogCombat.OpponentScale scale = request.getOpponentScale() != null
                ? request.getOpponentScale() : CaptainLogCombat.OpponentScale.MEDIUM;
        CaptainLogCombat combat = new CaptainLogCombat(mission, character, request.getOpponentName(), scale, request.getDescription());
        combat = combatRepository.save(combat);

        // Initialize combat session
        StarshipCombatState playerState = new StarshipCombatState(playerShip);
        StarshipCombatState enemyState = new StarshipCombatState(new CaptainLogStarship());
        enemyState.name = request.getOpponentName();
        enemyState.scale = scaleToShipScale(scale);
        enemyState.maxHits = scale.getHits();
        enemyState.currentHits = scale.getHits();
        enemyState.rangeBand = RangeBand.LONG;

        StarshipCombatSession session = new StarshipCombatSession(playerState, enemyState);
        activeSessions.put(combat.getId(), session);

        session.combatLog.add("Starship combat initiated: " + playerShip.getName() + " vs " + request.getOpponentName());
        session.combatLog.add("Range band: " + RangeBand.LONG.getDisplayName());

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("combatId", combat.getId());
        response.put("playerShip", playerShip.getName());
        response.put("enemyShip", request.getOpponentName());
        response.put("enemyScale", scale.name());
        response.put("rangeBand", RangeBand.LONG.getDisplayName());
        response.put("playerHits", playerState.currentHits);
        response.put("enemyHits", enemyState.currentHits);
        response.put("round", 0);
        response.put("playerTurn", true);
        response.put("combatLog", session.combatLog);
        response.put("availableActions", getAvailableActions(playerState, RangeBand.LONG));

        log.info("Starship combat started: {} vs {} (combatId={})", playerShip.getName(), request.getOpponentName(), combat.getId());
        return response;
    }

    private CaptainLogStarship.Scale scaleToShipScale(CaptainLogCombat.OpponentScale scale) {
        return switch (scale) {
            case SMALL -> CaptainLogStarship.Scale.SMALL;
            case MEDIUM -> CaptainLogStarship.Scale.MEDIUM;
            case LARGE -> CaptainLogStarship.Scale.LARGE;
            case CAPITAL -> CaptainLogStarship.Scale.CAPITAL;
            case STATION -> CaptainLogStarship.Scale.STATION;
        };
    }

    // ==================== Combat Actions ====================

    /**
     * Executes a combat action for the player's ship.
     */
    @Transactional
    public Map<String, Object> executeAction(Long combatId, CombatAction action,
                                              CaptainLogStarship.StarshipSystem targetSystem) {
        CaptainLogCombat combat = combatRepository.findById(combatId)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + combatId));

        if (combat.getStatus() != CaptainLogCombat.CombatStatus.ACTIVE) {
            throw new IllegalStateException("Combat is not active: " + combat.getStatus());
        }

        StarshipCombatSession session = activeSessions.get(combatId);
        if (session == null) {
            throw new IllegalStateException("No active combat session for combat " + combatId + ". Start a new starship combat.");
        }

        if (!session.playerTurn) {
            throw new IllegalStateException("Not the player's turn. Enemy is acting.");
        }

        // Advance round
        session.currentRound++;
        session.playerShip.setEvasiveManeuvers(false); // Reset evasive from previous round

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("round", session.currentRound);
        result.put("action", action.name());

        StringBuilder narrative = new StringBuilder();
        narrative.append("Round ").append(session.currentRound).append(" — ");

        switch (action) {
            case ATTACK -> {
                result.putAll(executeAttack(session, narrative, false));
            }
            case TORPEDO -> {
                // Check range
                if (session.playerShip.getRangeBand() == RangeBand.LONG) {
                    result.put("success", false);
                    result.put("narrative", "Torpedoes cannot be fired at long range! Maneuver closer first.");
                    session.combatLog.add((String) result.get("narrative"));
                } else {
                    result.putAll(executeAttack(session, narrative, true));
                }
            }
            case MANEUVER -> result.putAll(executeManeuver(session, narrative));
            case REPAIR -> result.putAll(executeRepair(session, narrative, targetSystem));
            case EVASIVE -> result.putAll(executeEvasive(session, narrative));
            case SHIELD_RECHARGE -> result.putAll(executeShieldRecharge(session, narrative));
        }

        // Check for combat end
        boolean ended = checkCombatEnd(session, combat);

        // Switch turns
        session.playerTurn = false;

        // Enemy's turn (auto-resolve)
        if (!ended) {
            Map<String, Object> enemyResult = resolveEnemyTurn(session, combat, narrative);
            result.put("enemyAction", enemyResult.get("action"));
            result.put("enemyNarrative", enemyResult.get("narrative"));

            // Check combat end after enemy turn
            ended = checkCombatEnd(session, combat);
            session.playerTurn = true;
        }

        result.put("playerHits", session.playerShip.getCurrentHits());
        result.put("enemyHits", session.enemyShip.getCurrentHits());
        result.put("rangeBand", session.playerShip.getRangeBand().getDisplayName());
        result.put("playerTurn", session.playerTurn);
        result.put("combatEnded", ended);
        result.put("status", combat.getStatus().name());
        result.put("narrative", narrative.toString());
        result.put("combatLog", session.combatLog);
        result.put("availableActions", getAvailableActions(session.playerShip, session.playerShip.getRangeBand()));

        // Update combat entity
        combat.setCurrentRound(session.currentRound);
        combatRepository.save(combat);

        log.info("Starship combat round {}: {} — {}", session.currentRound, action.getName(), result.get("narrative"));
        return result;
    }

    /**
     * Executes an attack action.
     */
    private Map<String, Object> executeAttack(StarshipCombatSession session, StringBuilder narrative, boolean isTorpedo) {
        Map<String, Object> result = new HashMap<>();
        String weaponType = isTorpedo ? "Torpedoes" : "Phasers";

        // Attacker rolls Weapons + Security
        int attackerTarget = 10; // Default; would use ship's actual system + department ratings
        int defenderTarget = 8;  // Enemy's evasion target

        // Roll attack
        TaskAttemptRequest attackRequest = new TaskAttemptRequest();
        attackRequest.setDifficulty(1);
        TaskAttemptResponse attackRoll = diceService.rollTaskAttemptDirect(attackerTarget, 1, null, 0, 0, false);

        // Roll defense (evasion)
        TaskAttemptResponse defenseRoll = diceService.rollTaskAttemptDirect(defenderTarget, 1, null, 0, 0, false);

        // Evasive maneuvers give a boon to defense
        if (session.playerShip.isEvasiveManeuvers()) {
            // Re-roll defense with a boon
            defenseRoll = diceService.rollTaskAttemptDirect(defenderTarget, 1, null, 1, 0, false);
        }

        boolean hit = attackRoll.getSuccesses() >= defenseRoll.getSuccesses();

        if (hit) {
            int damage = Math.max(1, attackRoll.getSuccesses() - defenseRoll.getSuccesses());
            if (isTorpedo) damage += 1; // Torpedoes deal +1 damage

            session.enemyShip.currentHits = Math.max(0, session.enemyShip.currentHits - damage);

            // Add stress to a random system
            CaptainLogStarship.StarshipSystem target = randomSystem();
            int currentStress = session.enemyShip.getSystemStress(target);
            session.enemyShip.setSystemStress(target, currentStress + 1);

            narrative.append(weaponType).append(" hit! ").append(damage).append(" hit").
                    append(damage > 1 ? "s" : "").append(" to ").append(session.enemyShip.getName()).append(". ");
            narrative.append(target.name()).append(" system takes stress.");

            // Critical hit check
            if (attackRoll.getSuccesses() - defenseRoll.getSuccesses() >= 2) {
                narrative.append(" Critical hit! ").append(target.name()).append(" system is severely damaged!");
                session.enemyShip.setSystemStress(target, currentStress + 2); // Extra stress on critical
            }

            result.put("hit", true);
            result.put("damage", damage);
            result.put("targetSystem", target.name());
            result.put("critical", attackRoll.getSuccesses() - defenseRoll.getSuccesses() >= 2);
        } else {
            narrative.append(weaponType).append(" miss! ").append(session.enemyShip.getName()).append(" evades the attack.");
            result.put("hit", false);
        }

        session.combatLog.add(narrative.toString());
        return result;
    }

    /**
     * Executes a maneuver action to change range band.
     */
    private Map<String, Object> executeManeuver(StarshipCombatSession session, StringBuilder narrative) {
        Map<String, Object> result = new HashMap<>();

        // Roll Engines + Operations
        TaskAttemptRequest maneuverRequest = new TaskAttemptRequest();
        maneuverRequest.setDifficulty(1);
        TaskAttemptResponse roll = diceService.rollTaskAttemptDirect(10, 1, null, 0, 0, false);

        if (roll.isSuccess()) {
            // Change range band
            RangeBand current = session.playerShip.getRangeBand();
            RangeBand next;
            // Player chooses to close or widen range — default to closing
            boolean closing = true; // Could be made configurable

            if (closing) {
                next = switch (current) {
                    case LONG -> RangeBand.MID;
                    case MID -> RangeBand.SHORT;
                    case SHORT -> current; // Already at short
                };
            } else {
                next = switch (current) {
                    case SHORT -> RangeBand.MID;
                    case MID -> RangeBand.LONG;
                    case LONG -> current; // Already at long
                };
            }

            if (next != current) {
                session.playerShip.setRangeBand(next);
                session.enemyShip.setRangeBand(next);
                narrative.append("Maneuver successful! Moved to ").append(next.getDisplayName()).append(".");
                result.put("newRangeBand", next.getDisplayName());
            } else {
                narrative.append("Already at ").append(current.getDisplayName()).append(". Cannot maneuver further in that direction.");
            }
            result.put("success", true);
        } else {
            narrative.append("Maneuver fails! The ship struggles to change position.");
            result.put("success", false);
        }

        session.combatLog.add(narrative.toString());
        return result;
    }

    /**
     * Executes a repair action to reduce system stress.
     */
    private Map<String, Object> executeRepair(StarshipCombatSession session, StringBuilder narrative,
                                               CaptainLogStarship.StarshipSystem targetSystem) {
        Map<String, Object> result = new HashMap<>();

        if (targetSystem == null) {
            targetSystem = CaptainLogStarship.StarshipSystem.STRUCTURE;
        }

        // Roll Engines + Engineering
        TaskAttemptResponse roll = diceService.rollTaskAttemptDirect(10, 1, null, 0, 0, false);

        if (roll.isSuccess()) {
            int currentStress = session.playerShip.getSystemStress(targetSystem);
            int reduced = Math.max(0, currentStress - 1);
            session.playerShip.setSystemStress(targetSystem, reduced);
            narrative.append("Repair successful! ").append(targetSystem.name()).append(" stress reduced to ").append(reduced).append(".");
            result.put("success", true);
            result.put("system", targetSystem.name());
            result.put("newStress", reduced);
        } else {
            narrative.append("Repair fails! The ").append(targetSystem.name()).append(" system remains damaged.");
            result.put("success", false);
        }

        session.combatLog.add(narrative.toString());
        return result;
    }

    /**
     * Executes evasive maneuvers for the next round.
     */
    private Map<String, Object> executeEvasive(StarshipCombatSession session, StringBuilder narrative) {
        Map<String, Object> result = new HashMap<>();
        session.playerShip.setEvasiveManeuvers(true);
        narrative.append("Evasive maneuvers committed! Defense will have a boon next round.");
        result.put("success", true);
        session.combatLog.add(narrative.toString());
        return result;
    }

    /**
     * Executes shield recharge to reduce structure stress.
     */
    private Map<String, Object> executeShieldRecharge(StarshipCombatSession session, StringBuilder narrative) {
        Map<String, Object> result = new HashMap<>();

        TaskAttemptResponse roll = diceService.rollTaskAttemptDirect(10, 1, null, 0, 0, false);

        if (roll.isSuccess()) {
            int currentStress = session.playerShip.getSystemStress(CaptainLogStarship.StarshipSystem.STRUCTURE);
            int reduced = Math.max(0, currentStress - 1);
            session.playerShip.setSystemStress(CaptainLogStarship.StarshipSystem.STRUCTURE, reduced);
            narrative.append("Shields recharged! Structure stress reduced to ").append(reduced).append(".");
            result.put("success", true);
            result.put("newStress", reduced);
        } else {
            narrative.append("Shield recharge fails! Power redistribution is unsuccessful.");
            result.put("success", false);
        }

        session.combatLog.add(narrative.toString());
        return result;
    }

    // ==================== Enemy AI ====================

    /**
     * Resolves the enemy's turn using simple AI.
     */
    private Map<String, Object> resolveEnemyTurn(StarshipCombatSession session, CaptainLogCombat combat,
                                                  StringBuilder narrative) {
        Map<String, Object> result = new HashMap<>();

        // Simple AI: attack 70% of the time, maneuver 20%, repair 10%
        double roll = Math.random();
        String action;

        if (roll < 0.7) {
            action = "attack";
        } else if (roll < 0.9) {
            action = "maneuver";
        } else {
            action = "repair";
        }

        narrative.append(" | Enemy ");

        switch (action) {
            case "attack" -> {
                // Enemy attacks
                TaskAttemptResponse attackRoll = diceService.rollTaskAttemptDirect(10, 1, null, 0, 0, false);
                TaskAttemptResponse defenseRoll = diceService.rollTaskAttemptDirect(8, 1, null, 0, 0, false);

                if (session.playerShip.isEvasiveManeuvers()) {
                    defenseRoll = diceService.rollTaskAttemptDirect(8, 1, null, 1, 0, false);
                }

                boolean hit = attackRoll.getSuccesses() >= defenseRoll.getSuccesses();

                if (hit) {
                    int damage = Math.max(1, attackRoll.getSuccesses() - defenseRoll.getSuccesses());
                    session.playerShip.currentHits = Math.max(0, session.playerShip.currentHits - damage);

                    CaptainLogStarship.StarshipSystem target = randomSystem();
                    int currentStress = session.playerShip.getSystemStress(target);
                    session.playerShip.setSystemStress(target, currentStress + 1);

                    narrative.append("attacks and hits! ").append(damage).append(" hit to your ship. ")
                            .append(target.name()).append(" system takes stress.");

                    // Add threat to mission on critical
                    if (attackRoll.getSuccesses() - defenseRoll.getSuccesses() >= 2) {
                        narrative.append(" Critical hit!");
                        CaptainLogMission mission = combat.getMission();
                        mission.setThreat(mission.getThreat() + 1);
                    }
                } else {
                    narrative.append("attacks but misses! Your ship evades.");
                }
            }
            case "maneuver" -> {
                narrative.append("attempts to maneuver but fails to change position significantly.");
            }
            case "repair" -> {
                narrative.append("works to repair damaged systems.");
            }
        }

        result.put("action", action);
        result.put("narrative", narrative.toString());
        return result;
    }

    // ==================== Combat End ====================

    /**
     * Checks if the combat has ended.
     */
    private boolean checkCombatEnd(StarshipCombatSession session, CaptainLogCombat combat) {
        if (session.enemyShip.isDestroyed()) {
            combat.setStatus(CaptainLogCombat.CombatStatus.VICTORY);
            combat.setEndedAt(LocalDateTime.now());
            combat.setResultSummary(session.playerShip.getName() + " defeated " +
                    session.enemyShip.getName() + " in " + session.currentRound + " rounds.");
            session.combatLog.add("VICTORY! " + session.enemyShip.getName() + " has been destroyed!");
            log.info("Starship combat victory: {} defeated {} in {} rounds",
                    session.playerShip.getName(), session.enemyShip.getName(), session.currentRound);
            return true;
        }

        if (session.playerShip.isDestroyed()) {
            combat.setStatus(CaptainLogCombat.CombatStatus.DEFEAT);
            combat.setEndedAt(LocalDateTime.now());
            combat.setResultSummary(session.playerShip.getName() + " was destroyed by " +
                    session.enemyShip.getName() + " after " + session.currentRound + " rounds.");
            session.combatLog.add("DEFEAT! " + session.playerShip.getName() + " has been destroyed!");
            log.info("Starship combat defeat: {} destroyed by {} after {} rounds",
                    session.playerShip.getName(), session.enemyShip.getName(), session.currentRound);
            return true;
        }

        return false;
    }

    // ==================== Retreat ====================

    /**
     * Retreats from starship combat.
     */
    @Transactional
    public Map<String, Object> retreatFromCombat(Long combatId) {
        CaptainLogCombat combat = combatRepository.findById(combatId)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + combatId));

        if (combat.getStatus() != CaptainLogCombat.CombatStatus.ACTIVE) {
            throw new IllegalStateException("Combat is not active: " + combat.getStatus());
        }

        StarshipCombatSession session = activeSessions.get(combatId);
        String playerName = session != null ? session.playerShip.getName() : "Your ship";
        String enemyName = session != null ? session.enemyShip.getName() : combat.getOpponentName();

        combat.setStatus(CaptainLogCombat.CombatStatus.RETREAT);
        combat.setEndedAt(LocalDateTime.now());
        combat.setResultSummary(playerName + " retreated from combat with " + enemyName +
                " after " + combat.getCurrentRound() + " rounds.");

        if (session != null) {
            session.combatLog.add("RETREAT! " + playerName + " withdraws from combat.");
            activeSessions.remove(combatId);
        }

        combatRepository.save(combat);

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combatId);
        result.put("status", "RETREAT");
        result.put("narrative", playerName + " successfully retreats from combat.");
        result.put("combatLog", session != null ? session.combatLog : List.of());

        log.info("Retreated from starship combat: {}", combatId);
        return result;
    }

    // ==================== Utility ====================

    /**
     * Returns the available actions based on current state and range band.
     */
    private List<String> getAvailableActions(StarshipCombatState ship, RangeBand rangeBand) {
        List<String> actions = new ArrayList<>();
        actions.add(CombatAction.ATTACK.name());
        actions.add(CombatAction.MANEUVER.name());
        actions.add(CombatAction.REPAIR.name());
        actions.add(CombatAction.EVASIVE.name());
        actions.add(CombatAction.SHIELD_RECHARGE.name());

        // Torpedoes only at Mid or Short range
        if (rangeBand != RangeBand.LONG) {
            actions.add(CombatAction.TORPEDO.name());
        }

        return actions;
    }

    /**
     * Returns a random starship system for targeting.
     */
    private CaptainLogStarship.StarshipSystem randomSystem() {
        CaptainLogStarship.StarshipSystem[] systems = CaptainLogStarship.StarshipSystem.values();
        return systems[new Random().nextInt(systems.length)];
    }

    /**
     * Gets the current state of a starship combat.
     */
    public Map<String, Object> getCombatState(Long combatId) {
        CaptainLogCombat combat = combatRepository.findById(combatId)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + combatId));

        StarshipCombatSession session = activeSessions.get(combatId);
        if (session == null) {
            // Return basic combat info from entity
            Map<String, Object> result = new HashMap<>();
            result.put("combatId", combat.getId());
            result.put("status", combat.getStatus().name());
            result.put("opponentName", combat.getOpponentName());
            result.put("round", combat.getCurrentRound());
            result.put("resultSummary", combat.getResultSummary());
            result.put("note", "Combat session ended. Showing persisted state.");
            return result;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("combatId", combat.getId());
        result.put("status", combat.getStatus().name());
        result.put("playerShip", session.playerShip.getName());
        result.put("enemyShip", session.enemyShip.getName());
        result.put("playerHits", session.playerShip.getCurrentHits());
        result.put("enemyHits", session.enemyShip.getCurrentHits());
        result.put("rangeBand", session.playerShip.getRangeBand().getDisplayName());
        result.put("round", session.currentRound);
        result.put("playerTurn", session.playerTurn);
        result.put("playerSystemStress", session.playerShip.getSystemStress());
        result.put("enemySystemStress", session.enemyShip.getSystemStress());
        result.put("combatLog", session.combatLog);
        result.put("availableActions", getAvailableActions(session.playerShip, session.playerShip.getRangeBand()));

        return result;
    }

    /**
     * Lists all available combat actions with descriptions.
     */
    public List<Map<String, String>> listActions() {
        List<Map<String, String>> actions = new ArrayList<>();
        for (CombatAction action : CombatAction.values()) {
            Map<String, String> actionMap = new HashMap<>();
            actionMap.put("name", action.getName());
            actionMap.put("description", action.getDescription());
            actionMap.put("attackerStats", action.getAttackerStats());
            actionMap.put("defenderStats", action.getDefenderStats());
            actions.add(actionMap);
        }
        return actions;
    }

    /**
     * Lists all range bands with descriptions.
     */
    public List<Map<String, String>> listRangeBands() {
        List<Map<String, String>> bands = new ArrayList<>();
        for (RangeBand band : RangeBand.values()) {
            Map<String, String> bandMap = new HashMap<>();
            bandMap.put("name", band.name());
            bandMap.put("displayName", band.getDisplayName());
            bandMap.put("description", band.getDescription());
            bands.add(bandMap);
        }
        return bands;
    }
}
