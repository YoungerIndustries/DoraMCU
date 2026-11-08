package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Manages military tours of duty for Traveller characters in the 4X game.
 * <p>
 * This service handles two types of tours:
 * </p>
 * <ol>
 *   <li><b>Navy Tours</b> — Characters assigned to fleets observe and record
 *       fleet activities (battles, deployments, ship losses) as character experiences.
 *       The character does not control the fleet.</li>
 *   <li><b>Army Tours</b> — Characters assigned to ground units observe and record
 *       unit activities (combat, casualties, experience gains) as character experiences.
 *       The character does not control the unit.</li>
 * </ol>
 * <p>
 * Each tour lasts for a fixed number of turns. At the end of each turn,
 * the service captures the unit/fleet's activities and converts them into
 * narrative experiences in the character's log.
 * </p>
 * <p>
 * This service is registered as a Spring bean. Since {@link CharacterLogManager}
 * depends on a {@link com.yisy.modules.doramcu.hex.Game} instance (not a Spring bean),
 * the log manager is set per-game session via {@link #setLogManager(CharacterLogManager)}.
 * </p>
 */
@Service
public class MilitaryTourService {

    private CharacterLogManager logManager;
    private final Map<Integer, NavyTourAssignment> navyTours;
    private final Map<Integer, ArmyUnitAssignment> armyAssignments;
    private int nextNavyTourId;
    private int nextArmyAssignmentId;
    private BiConsumer<Integer, String> tourCompletionCallback;

    /**
     * Default constructor for Spring bean instantiation.
     * Sets an initial empty log manager state; call {@link #setLogManager(CharacterLogManager)}
     * before use to configure the log manager for a specific game session.
     */
    @Autowired
    public MilitaryTourService() {
        this.navyTours = new HashMap<>();
        this.armyAssignments = new HashMap<>();
        this.nextNavyTourId = 1;
        this.nextArmyAssignmentId = 1;
    }

    /**
     * Creates a new tour service with the given log manager.
     * Convenience constructor for non-Spring usage (e.g., tests).
     *
     * @param logManager the character log manager for recording experiences
     */
    public MilitaryTourService(CharacterLogManager logManager) {
        this.logManager = logManager;
        this.navyTours = new HashMap<>();
        this.armyAssignments = new HashMap<>();
        this.nextNavyTourId = 1;
        this.nextArmyAssignmentId = 1;
    }

    /**
     * Sets the character log manager for a specific game session.
     * <p>
     * Must be called before any tour operations to ensure experiences
     * are logged to the correct game context.
     * </p>
     *
     * @param logManager the character log manager for the current game session
     */
    public void setLogManager(CharacterLogManager logManager) {
        this.logManager = logManager;
    }

    /**
     * Gets the current character log manager.
     *
     * @return the log manager, or null if not set
     */
    CharacterLogManager getLogManager() {
        return logManager;
    }

    /**
     * Sets a callback to invoke when a tour completes.
     * <p>
     * The callback receives the character ID and tour type ("navy" or "army").
     * Used by TourOfDutyService to revert character status from ON_DUTY to TRAINING.
     * </p>
     *
     * @param callback the callback to invoke on tour completion, or null to clear
     */
    public void setTourCompletionCallback(BiConsumer<Integer, String> callback) {
        this.tourCompletionCallback = callback;
    }

    /**
     * Invokes the tour completion callback if set.
     *
     * @param characterId the character whose tour completed
     * @param tourType the type of tour ("navy" or "army")
     */
    private void notifyTourCompletion(int characterId, String tourType) {
        if (tourCompletionCallback != null) {
            tourCompletionCallback.accept(characterId, tourType);
        }
    }

    // ============================================================
    // Navy Tour Management
    // ============================================================

    /**
     * Assigns a navy character to a fleet for a tour of duty.
     * <p>
     * The character does not control the fleet — they serve as an observer
     * who records the fleet's experiences in their life history.
     * </p>
     *
     * @param character the Traveller character
     * @param fleet the fleet to observe
     * @param empireName the name of the empire
     * @param totalTurns the number of turns in this tour
     * @return the created tour assignment
     */
    public NavyTourAssignment assignNavyTour(TravellerCharacter character, Fleet fleet,
                                              String empireName, int totalTurns) {
        if (character == null) {
            throw new IllegalArgumentException("Character must not be null");
        }
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet must not be null");
        }
        if (totalTurns <= 0) {
            throw new IllegalArgumentException("Total turns must be positive");
        }

        // Check for existing active tour
        Optional<NavyTourAssignment> existing = getActiveNavyTour(character);
        if (existing.isPresent()) {
            throw new IllegalStateException("Character already has an active navy tour: " + existing.get().getId());
        }

        int tourId = nextNavyTourId++;
        NavyTourAssignment tour = new NavyTourAssignment(tourId, character, fleet, empireName, totalTurns);
        navyTours.put(tourId, tour);

        // Log the tour assignment as a character experience
        CharacterLog log = logManager.getLog(character);
        if (log != null) {
            log.log(ExperienceType.MISSION_SUCCESS,
                    "Assigned to " + fleet.getName(),
                    String.format("%s was assigned to %s in the %s navy for a %d-turn tour of duty.",
                            character.getName(), fleet.getName(), empireName, totalTurns))
                    .location(empireName)
                    .outcome(CharacterExperience.Outcome.SUCCESS);
        }

        return tour;
    }

    /**
     * Processes the end of a turn for all active navy tours.
     * <p>
     * Captures fleet activities and converts them into character experiences.
     * </p>
     *
     * @param game the game context to check for battles and fleet changes
     * @return the number of tours processed
     */
    public int processNavyTurnEnd(Game game) {
        if (game == null) return 0;

        int processed = 0;
        Iterator<Map.Entry<Integer, NavyTourAssignment>> iterator = navyTours.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, NavyTourAssignment> entry = iterator.next();
            NavyTourAssignment tour = entry.getValue();

            if (!tour.isActive()) {
                // Remove completed tours after a grace period
                if (tour.isCompleted()) {
                    iterator.remove();
                }
                continue;
            }

            Fleet fleet = tour.getAssignedFleet();
            if (fleet == null) {
                tour.terminate("Fleet no longer exists");
                continue;
            }

            // Check if fleet was destroyed
            if (fleet.isDestroyed()) {
                tour.recordEvent(NavyTourAssignment.TourEventType.FLEET_DESTROYED,
                        "The " + fleet.getName() + " was destroyed in combat.");
                tour.terminate("Fleet destroyed");
                logFleetDestruction(tour);
                continue;
            }

            // Process battles involving this fleet
            processNavyBattles(tour, game);

            // Advance the tour
            boolean completed = tour.advanceTurn();
            if (completed) {
                logTourCompletion(tour);
                notifyTourCompletion(tour.getCharacter().getId(), "navy");
            }

            processed++;
        }

        return processed;
    }

    /**
     * Records a specific battle event for a navy tour.
     *
     * @param tour the tour to record for
     * @param battle the battle that occurred
     */
    public void recordNavyBattle(NavyTourAssignment tour, Battle battle) {
        if (!tour.isActive()) return;

        Fleet fleet = tour.getAssignedFleet();
        if (fleet == null) return;

        boolean isAttacker = fleet == battle.getAttacker();
        boolean isDefender = fleet == battle.getDefender();

        if (!isAttacker && !isDefender) return;

        String role = isAttacker ? "attacker" : "defender";

        tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_ENGAGED,
                String.format("The %s engaged in battle as %s at %s.",
                        fleet.getName(), role, battle.getLocation()));

        if (battle.getState() == Battle.BattleState.COMPLETED) {
            Battle.BattleOutcome outcome = battle.getOutcome();
            Battle.BattleResult result = getBattleResult(battle);

            boolean fleetWon = false;
            switch (outcome) {
                case ATTACKER_VICTORY -> fleetWon = isAttacker;
                case DEFENDER_VICTORY -> fleetWon = isDefender;
                case MUTUAL_DESTRUCTION -> fleetWon = false;
                case RETREAT -> {
                    tour.recordEvent(NavyTourAssignment.TourEventType.RETREAT,
                            "The " + fleet.getName() + " retreated from battle.");
                    fleetWon = false;
                }
                case STALEMATE -> fleetWon = false;
            }

            if (fleetWon) {
                tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_VICTORY,
                        String.format("The %s won the battle after %d rounds.",
                                fleet.getName(), result.getTotalRounds()));
            } else if (outcome == Battle.BattleOutcome.MUTUAL_DESTRUCTION) {
                tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_DEFEAT,
                        "Both fleets were destroyed in the battle.");
            } else {
                tour.recordEvent(NavyTourAssignment.TourEventType.BATTLE_DEFEAT,
                        String.format("The %s lost the battle after %d rounds.",
                                fleet.getName(), result.getTotalRounds()));
            }

            // Log ship losses
            int shipsLost = isAttacker ? result.getTotalAttackerShipsLost() : result.getTotalDefenderShipsLost();
            if (shipsLost > 0) {
                tour.recordEvent(NavyTourAssignment.TourEventType.SHIPS_LOST,
                        String.format("%d ships were lost in the battle.", shipsLost));
            }

            // Convert to character experience
            logBattleExperience(tour, fleetWon, result);
        }
    }

    /**
     * Gets the battle result for a completed battle.
     */
    private Battle.BattleResult getBattleResult(Battle battle) {
        if (battle.getState() != Battle.BattleState.COMPLETED) {
            return null;
        }
        // Fight to completion if not already done
        if (battle.getRoundResults().isEmpty()) {
            return battle.fightToCompletion();
        }
        return battle.getOutcome() != null ? battle.fightToCompletion() : null;
    }

    /**
     * Gets the active navy tour for a character.
     */
    public Optional<NavyTourAssignment> getActiveNavyTour(TravellerCharacter character) {
        return navyTours.values().stream()
                .filter(t -> t.getCharacter().getId() == character.getId() && t.isActive())
                .findFirst();
    }

    /**
     * Gets all navy tours for a character.
     */
    public List<NavyTourAssignment> getNavyTours(TravellerCharacter character) {
        return navyTours.values().stream()
                .filter(t -> t.getCharacter().getId() == character.getId())
                .collect(Collectors.toList());
    }

    /**
     * Gets all active navy tours.
     */
    public List<NavyTourAssignment> getActiveNavyTours() {
        return navyTours.values().stream()
                .filter(NavyTourAssignment::isActive)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Army Assignment Management
    // ============================================================

    /**
     * Assigns an army character to a ground unit for a tour of duty.
     * <p>
     * The character does not control the unit — they serve as an observer
     * who records the unit's experiences in their life history.
     * </p>
     *
     * @param character the Traveller character
     * @param unit the army unit to observe
     * @param empireName the name of the empire
     * @param planetName the planet where the unit is stationed
     * @param totalTurns the number of turns in this assignment
     * @return the created assignment
     */
    public ArmyUnitAssignment assignArmyUnit(TravellerCharacter character, ArmyUnit unit,
                                              String empireName, String planetName, int totalTurns) {
        if (character == null) {
            throw new IllegalArgumentException("Character must not be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (totalTurns <= 0) {
            throw new IllegalArgumentException("Total turns must be positive");
        }

        // Check for existing active assignment
        Optional<ArmyUnitAssignment> existing = getActiveArmyAssignment(character);
        if (existing.isPresent()) {
            throw new IllegalStateException("Character already has an active army assignment: " + existing.get().getId());
        }

        int assignmentId = nextArmyAssignmentId++;
        ArmyUnitAssignment assignment = new ArmyUnitAssignment(assignmentId, character, unit,
                empireName, planetName, totalTurns);
        armyAssignments.put(assignmentId, assignment);

        // Log the assignment as a character experience
        CharacterLog log = logManager.getLog(character);
        if (log != null) {
            log.log(ExperienceType.MISSION_SUCCESS,
                    "Assigned to " + unit.getName(),
                    String.format("%s was assigned to %s on %s in the %s army for a %d-turn tour of duty.",
                            character.getName(), unit.getName(), planetName, empireName, totalTurns))
                    .location(planetName)
                    .outcome(CharacterExperience.Outcome.SUCCESS);
        }

        return assignment;
    }

    /**
     * Processes the end of a turn for all active army assignments.
     * <p>
     * Captures unit activities and converts them into character experiences.
     * </p>
     *
     * @return the number of assignments processed
     */
    public int processArmyTurnEnd() {
        int processed = 0;
        Iterator<Map.Entry<Integer, ArmyUnitAssignment>> iterator = armyAssignments.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, ArmyUnitAssignment> entry = iterator.next();
            ArmyUnitAssignment assignment = entry.getValue();

            if (!assignment.isActive()) {
                if (assignment.isCompleted()) {
                    iterator.remove();
                }
                continue;
            }

            ArmyUnit unit = assignment.getAssignedUnit();
            if (unit == null) {
                assignment.terminate("Unit no longer exists");
                continue;
            }

            // Check if unit was destroyed
            if (unit.getCurrentStrength() <= 0) {
                assignment.recordEvent(ArmyUnitAssignment.UnitEventType.UNIT_DESTROYED,
                        "The " + unit.getName() + " was destroyed in combat.");
                assignment.terminate("Unit destroyed");
                logUnitDestruction(assignment);
                continue;
            }

            // Advance the assignment
            boolean completed = assignment.advanceTurn();
            if (completed) {
                logAssignmentCompletion(assignment);
                notifyTourCompletion(assignment.getCharacter().getId(), "army");
            }

            processed++;
        }

        return processed;
    }

    /**
     * Records a combat event for an army assignment.
     *
     * @param assignment the assignment to record for
     * @param unit the unit that was in combat
     * @param won whether the unit won the combat
     * @param casualties the number of casualties suffered
     */
    public void recordArmyCombat(ArmyUnitAssignment assignment, ArmyUnit unit,
                                  boolean won, int casualties) {
        if (!assignment.isActive()) return;

        assignment.recordEvent(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED,
                String.format("The %s engaged in combat on %s.",
                        unit.getName(), assignment.getPlanetName()));

        if (won) {
            assignment.recordEvent(ArmyUnitAssignment.UnitEventType.COMBAT_VICTORY,
                    "The " + unit.getName() + " won the combat engagement.");
        } else {
            assignment.recordEvent(ArmyUnitAssignment.UnitEventType.COMBAT_DEFEAT,
                    "The " + unit.getName() + " lost the combat engagement.");
        }

        if (casualties > 0) {
            assignment.recordEvent(ArmyUnitAssignment.UnitEventType.CASUALTIES,
                    String.format("%d casualties were suffered in combat.", casualties));
        }

        // Convert to character experience
        logCombatExperience(assignment, won, casualties);
    }

    /**
     * Records a deployment event for an army assignment.
     */
    public void recordArmyDeployment(ArmyUnitAssignment assignment, String newPlanet) {
        if (!assignment.isActive()) return;

        assignment.recordEvent(ArmyUnitAssignment.UnitEventType.DEPLOYMENT,
                String.format("The %s was deployed to %s.",
                        assignment.getUnitName(), newPlanet));

        CharacterLog log = logManager.getLog(assignment.getCharacter());
        if (log != null) {
            log.log(ExperienceType.EXPLORATION,
                    "Deployed to " + newPlanet,
                    String.format("The %s was deployed to %s.",
                            assignment.getUnitName(), newPlanet))
                    .location(newPlanet);
        }
    }

    /**
     * Gets the active army assignment for a character.
     */
    public Optional<ArmyUnitAssignment> getActiveArmyAssignment(TravellerCharacter character) {
        return armyAssignments.values().stream()
                .filter(a -> a.getCharacter().getId() == character.getId() && a.isActive())
                .findFirst();
    }

    /**
     * Gets all army assignments for a character.
     */
    public List<ArmyUnitAssignment> getArmyAssignments(TravellerCharacter character) {
        return armyAssignments.values().stream()
                .filter(a -> a.getCharacter().getId() == character.getId())
                .collect(Collectors.toList());
    }

    /**
     * Gets all active army assignments.
     */
    public List<ArmyUnitAssignment> getActiveArmyAssignments() {
        return armyAssignments.values().stream()
                .filter(ArmyUnitAssignment::isActive)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Experience Logging — Navy
    // ============================================================

    private void processNavyBattles(NavyTourAssignment tour, Game game) {
        for (Battle battle : game.getBattles().values()) {
            Fleet fleet = tour.getAssignedFleet();
            if (fleet == null) continue;

            if (battle.getAttacker() == fleet || battle.getDefender() == fleet) {
                recordNavyBattle(tour, battle);
            }
        }
    }

    private void logBattleExperience(NavyTourAssignment tour, boolean won, Battle.BattleResult result) {
        CharacterLog log = logManager.getLog(tour.getCharacter());
        if (log == null) return;

        TravellerCharacter character = tour.getCharacter();
        Fleet fleet = tour.getAssignedFleet();

        ExperienceType type = won ? ExperienceType.BATTLE : ExperienceType.MISSION_FAILURE;
        CharacterExperience.Outcome outcome = won ? CharacterExperience.Outcome.SUCCESS : CharacterExperience.Outcome.FAILURE;

        String title = won ? "Victory at " + tour.getFleetName() : "Defeat at " + tour.getFleetName();
        String description;

        if (result != null) {
            description = String.format(
                    "The %s fought for %d rounds. %s dealt %d damage and lost %d ships, while the enemy lost %d ships.",
                    fleet.getName(),
                    result.getTotalRounds(),
                    fleet.getName(),
                    fleet == tour.getAssignedFleet() ? result.getTotalAttackerDamage() : result.getTotalDefenderDamage(),
                    fleet == tour.getAssignedFleet() ? result.getTotalAttackerShipsLost() : result.getTotalDefenderShipsLost(),
                    fleet == tour.getAssignedFleet() ? result.getTotalDefenderShipsLost() : result.getTotalAttackerShipsLost()
            );
        } else {
            description = String.format(
                    "%s witnessed the %s %s a battle.",
                    character.getName(), fleet.getName(), won ? "win" : "lose"
            );
        }

        log.log(type, title, description)
                .location(tour.getEmpireName())
                .outcome(outcome)
                .characterAge(character.getAge());
    }

    private void logFleetDestruction(NavyTourAssignment tour) {
        CharacterLog log = logManager.getLog(tour.getCharacter());
        if (log == null) return;

        log.log(ExperienceType.LOSS,
                "Fleet Destroyed",
                String.format("The %s was destroyed. %s survived and witnessed the end of their fleet.",
                        tour.getFleetName(), tour.getCharacter().getName()))
                .location(tour.getEmpireName())
                .outcome(CharacterExperience.Outcome.FAILURE);
    }

    private void logTourCompletion(NavyTourAssignment tour) {
        CharacterLog log = logManager.getLog(tour.getCharacter());
        if (log == null) return;

        int eventCount = tour.getEvents().size();
        log.log(ExperienceType.CAREER_COMPLETION,
                "Navy Tour Completed",
                String.format("%s completed a %d-turn tour with %s, witnessing %d significant events.",
                        tour.getCharacter().getName(), tour.getTotalTurns(),
                        tour.getFleetName(), eventCount))
                .location(tour.getEmpireName())
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    // ============================================================
    // Experience Logging — Army
    // ============================================================

    private void logCombatExperience(ArmyUnitAssignment assignment, boolean won, int casualties) {
        CharacterLog log = logManager.getLog(assignment.getCharacter());
        if (log == null) return;

        TravellerCharacter character = assignment.getCharacter();

        ExperienceType type = won ? ExperienceType.BATTLE : ExperienceType.MISSION_FAILURE;
        CharacterExperience.Outcome outcome = won ? CharacterExperience.Outcome.SUCCESS : CharacterExperience.Outcome.FAILURE;

        String title = won ? "Victory in Combat" : "Defeat in Combat";
        String description;

        if (casualties > 0) {
            description = String.format(
                    "%s witnessed the %s %s combat on %s, suffering %d casualties.",
                    character.getName(),
                    assignment.getUnitName(),
                    won ? "win" : "lose",
                    assignment.getPlanetName(),
                    casualties
            );
        } else {
            description = String.format(
                    "%s witnessed the %s %s combat on %s without casualties.",
                    character.getName(),
                    assignment.getUnitName(),
                    won ? "win" : "lose",
                    assignment.getPlanetName()
            );
        }

        log.log(type, title, description)
                .location(assignment.getPlanetName())
                .outcome(outcome)
                .characterAge(character.getAge());
    }

    private void logUnitDestruction(ArmyUnitAssignment assignment) {
        CharacterLog log = logManager.getLog(assignment.getCharacter());
        if (log == null) return;

        log.log(ExperienceType.LOSS,
                "Unit Destroyed",
                String.format("The %s was destroyed. %s survived and witnessed the end of their unit.",
                        assignment.getUnitName(), assignment.getCharacter().getName()))
                .location(assignment.getPlanetName())
                .outcome(CharacterExperience.Outcome.FAILURE);
    }

    private void logAssignmentCompletion(ArmyUnitAssignment assignment) {
        CharacterLog log = logManager.getLog(assignment.getCharacter());
        if (log == null) return;

        int eventCount = assignment.getEvents().size();
        log.log(ExperienceType.CAREER_COMPLETION,
                "Army Tour Completed",
                String.format("%s completed a %d-turn tour with %s on %s, witnessing %d significant events.",
                        assignment.getCharacter().getName(),
                        assignment.getTotalTurns(),
                        assignment.getUnitName(),
                        assignment.getPlanetName(),
                        eventCount))
                .location(assignment.getPlanetName())
                .outcome(CharacterExperience.Outcome.SUCCESS);
    }

    // ============================================================
    // Statistics
    // ============================================================

    /** @return the total number of navy tours (active and completed) */
    public int getNavyTourCount() {
        return navyTours.size();
    }

    /** @return the total number of army assignments (active and completed) */
    public int getArmyAssignmentCount() {
        return armyAssignments.size();
    }

    /** @return the number of active navy tours */
    public int getActiveNavyTourCount() {
        return (int) navyTours.values().stream().filter(NavyTourAssignment::isActive).count();
    }

    /** @return the number of active army assignments */
    public int getActiveArmyAssignmentCount() {
        return (int) armyAssignments.values().stream().filter(ArmyUnitAssignment::isActive).count();
    }
}
