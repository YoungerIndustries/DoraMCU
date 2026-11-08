package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.*;
import com.yisy.modules.doramcu.hex.manager.CharacterLogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MilitaryTourService Tests")
class MilitaryTourServiceTest {

    private Game game;
    private CharacterLogManager logManager;
    private MilitaryTourService service;
    private TravellerCharacter character;

    @BeforeEach
    void setUp() {
        game = new Game("Test Campaign");
        logManager = new CharacterLogManager(game);
        service = new MilitaryTourService(logManager);
        character = logManager.createCharacter("Alex Starborn", "Earth");
        character.rollAbilities();
    }

    // ---- Constructor and Log Manager ----

    @Test
    @DisplayName("Default constructor should initialize empty state")
    void testDefaultConstructor() {
        MilitaryTourService emptyService = new MilitaryTourService();

        assertNull(emptyService.getLogManager());
        assertEquals(0, emptyService.getNavyTourCount());
        assertEquals(0, emptyService.getArmyAssignmentCount());
        assertEquals(0, emptyService.getActiveNavyTourCount());
        assertEquals(0, emptyService.getActiveArmyAssignmentCount());
    }

    @Test
    @DisplayName("Constructor with log manager should set it")
    void testConstructorWithLogManager() {
        MilitaryTourService svc = new MilitaryTourService(logManager);

        assertSame(logManager, svc.getLogManager());
    }

    @Test
    @DisplayName("setLogManager should update the log manager")
    void testSetLogManager() {
        MilitaryTourService svc = new MilitaryTourService();
        assertNull(svc.getLogManager());

        svc.setLogManager(logManager);
        assertSame(logManager, svc.getLogManager());
    }

    @Test
    @DisplayName("setLogManager should allow switching log managers mid-session")
    void testSwitchLogManager() {
        MilitaryTourService svc = new MilitaryTourService();
        Game game2 = new Game("Second Campaign");
        CharacterLogManager logManager2 = new CharacterLogManager(game2);

        svc.setLogManager(logManager);
        assertSame(logManager, svc.getLogManager());

        svc.setLogManager(logManager2);
        assertSame(logManager2, svc.getLogManager());
    }

    // ---- Navy Tour Assignment ----

    @Test
    @DisplayName("Should assign navy tour")
    void testAssignNavyTour() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        NavyTourAssignment tour = service.assignNavyTour(character, fleet, "Test Empire", 6);

        assertNotNull(tour);
        assertEquals(1, tour.getId());
        assertSame(character, tour.getCharacter());
        assertSame(fleet, tour.getAssignedFleet());
        assertEquals(6, tour.getTotalTurns());
        assertTrue(tour.isActive());
    }

    @Test
    @DisplayName("Should reject null character for navy tour")
    void testAssignNavyTourNullCharacter() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        assertThrows(IllegalArgumentException.class, () ->
                service.assignNavyTour(null, fleet, "Empire", 6));
    }

    @Test
    @DisplayName("Should reject null fleet for navy tour")
    void testAssignNavyTourNullFleet() {
        assertThrows(IllegalArgumentException.class, () ->
                service.assignNavyTour(character, null, "Empire", 6));
    }

    @Test
    @DisplayName("Should reject zero turns for navy tour")
    void testAssignNavyTourZeroTurns() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        assertThrows(IllegalArgumentException.class, () ->
                service.assignNavyTour(character, fleet, "Empire", 0));
    }

    @Test
    @DisplayName("Should reject duplicate active navy tour")
    void testAssignNavyTourDuplicate() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Empire", 6);

        assertThrows(IllegalStateException.class, () ->
                service.assignNavyTour(character, fleet, "Empire", 6));
    }

    @Test
    @DisplayName("Should log navy tour assignment")
    void testNavyTourAssignmentLogged() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Test Empire", 6);

        CharacterLog log = logManager.getLog(character);
        assertNotNull(log);
        assertTrue(log.getByType(ExperienceType.MISSION_SUCCESS).size() >= 1);
    }

    // ---- Army Assignment ----

    @Test
    @DisplayName("Should assign army unit")
    void testAssignArmyUnit() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 6);

        assertNotNull(assignment);
        assertEquals(1, assignment.getId());
        assertSame(character, assignment.getCharacter());
        assertSame(unit, assignment.getAssignedUnit());
        assertEquals(6, assignment.getTotalTurns());
        assertTrue(assignment.isActive());
    }

    @Test
    @DisplayName("Should reject null character for army assignment")
    void testAssignArmyUnitNullCharacter() {
        ArmyUnit unit = new ArmyUnit(1, ArmyUnitType.INFANTRY);
        assertThrows(IllegalArgumentException.class, () ->
                service.assignArmyUnit(null, unit, "Empire", "Planet", 6));
    }

    @Test
    @DisplayName("Should reject null unit for army assignment")
    void testAssignArmyUnitNullUnit() {
        assertThrows(IllegalArgumentException.class, () ->
                service.assignArmyUnit(character, null, "Empire", "Planet", 6));
    }

    @Test
    @DisplayName("Should reject duplicate active army assignment")
    void testAssignArmyUnitDuplicate() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Empire", "Planet", 6);

        assertThrows(IllegalStateException.class, () ->
                service.assignArmyUnit(character, unit, "Empire", "Planet", 6));
    }

    @Test
    @DisplayName("Should log army assignment")
    void testArmyAssignmentLogged() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 6);

        CharacterLog log = logManager.getLog(character);
        assertNotNull(log);
        assertTrue(log.getByType(ExperienceType.MISSION_SUCCESS).size() >= 1);
    }

    // ---- Tour queries ----

    @Test
    @DisplayName("Should find active navy tour")
    void testGetActiveNavyTour() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        NavyTourAssignment tour = service.assignNavyTour(character, fleet, "Empire", 6);

        Optional<NavyTourAssignment> found = service.getActiveNavyTour(character);
        assertTrue(found.isPresent());
        assertSame(tour, found.get());
    }

    @Test
    @DisplayName("Should return empty for no active navy tour")
    void testGetActiveNavyTourNone() {
        Optional<NavyTourAssignment> found = service.getActiveNavyTour(character);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should find active army assignment")
    void testGetActiveArmyAssignment() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);

        Optional<ArmyUnitAssignment> found = service.getActiveArmyAssignment(character);
        assertTrue(found.isPresent());
        assertSame(assignment, found.get());
    }

    @Test
    @DisplayName("Should return empty for no active army assignment")
    void testGetActiveArmyAssignmentNone() {
        Optional<ArmyUnitAssignment> found = service.getActiveArmyAssignment(character);
        assertTrue(found.isEmpty());
    }

    // ---- Army combat recording ----

    @Test
    @DisplayName("Should record army combat victory")
    void testRecordArmyCombatVictory() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        service.recordArmyCombat(assignment, unit, true, 5);

        assertEquals(3, assignment.getEvents().size());
        assertEquals(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED, assignment.getEvents().get(0).getEventType());
        assertEquals(ArmyUnitAssignment.UnitEventType.COMBAT_VICTORY, assignment.getEvents().get(1).getEventType());
        assertEquals(ArmyUnitAssignment.UnitEventType.CASUALTIES, assignment.getEvents().get(2).getEventType());

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> battles = log.getByType(ExperienceType.BATTLE);
        assertTrue(battles.size() >= 1);
    }

    @Test
    @DisplayName("Should record army combat defeat")
    void testRecordArmyCombatDefeat() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        service.recordArmyCombat(assignment, unit, false, 10);

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> failures = log.getByType(ExperienceType.MISSION_FAILURE);
        assertTrue(failures.size() >= 1);
    }

    @Test
    @DisplayName("Should record army deployment")
    void testRecordArmyDeployment() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        service.recordArmyDeployment(assignment, "New World");

        assertEquals(1, assignment.getEvents().size());
        assertEquals(ArmyUnitAssignment.UnitEventType.DEPLOYMENT, assignment.getEvents().get(0).getEventType());

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> explorations = log.getByType(ExperienceType.EXPLORATION);
        assertTrue(explorations.size() >= 1);
    }

    // ---- Turn processing ----

    @Test
    @DisplayName("Should process navy turn end")
    void testProcessNavyTurnEnd() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        // Add a ship so the fleet is not destroyed
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Empire", 3);

        int processed = service.processNavyTurnEnd(game);
        assertEquals(1, processed);
    }

    @Test
    @DisplayName("Should process army turn end")
    void testProcessArmyTurnEnd() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Empire", "Planet", 3);

        int processed = service.processArmyTurnEnd();
        assertEquals(1, processed);
    }

    @Test
    @DisplayName("Should handle null game in navy turn end")
    void testProcessNavyTurnEndNullGame() {
        int processed = service.processNavyTurnEnd(null);
        assertEquals(0, processed);
    }

    // ---- Statistics ----

    @Test
    @DisplayName("Should track navy tour count")
    void testNavyTourCount() {
        assertEquals(0, service.getNavyTourCount());
        assertEquals(0, service.getActiveNavyTourCount());

        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Empire", 6);

        assertEquals(1, service.getNavyTourCount());
        assertEquals(1, service.getActiveNavyTourCount());
    }

    @Test
    @DisplayName("Should track army assignment count")
    void testArmyAssignmentCount() {
        assertEquals(0, service.getArmyAssignmentCount());
        assertEquals(0, service.getActiveArmyAssignmentCount());

        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Empire", "Planet", 6);

        assertEquals(1, service.getArmyAssignmentCount());
        assertEquals(1, service.getActiveArmyAssignmentCount());
    }

    // ---- Multiple characters ----

    @Test
    @DisplayName("Should handle multiple characters with separate tours")
    void testMultipleCharacters() {
        TravellerCharacter char2 = logManager.createCharacter("Bob Smith", "Mars");
        char2.rollAbilities();

        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Empire", 6);
        service.assignNavyTour(char2, fleet, "Empire", 6);

        assertEquals(2, service.getActiveNavyTourCount());
        assertEquals(1, service.getNavyTours(character).size());
        assertEquals(1, service.getNavyTours(char2).size());
    }

    // ---- Navy Battle Recording ----

    @Test
    @DisplayName("Should record navy battle as attacker victory")
    void testRecordNavyBattleAttackerVictory() {
        game.setupPlayer(1, "Test Player");
        game.setupPlayer(2, "Enemy Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet attackerFleet = new Fleet(1, "Attacker Fleet");
        Ship attackerShip = new Ship(1, ShipType.DESTROYER);
        attackerFleet.addShip(attackerShip);
        game.addFleet(attackerFleet, game.getPlayer(1));
        empire.getNavy().addFleet(attackerFleet);

        Fleet defenderFleet = new Fleet(2, "Defender Fleet");
        Ship defenderShip = new Ship(2, ShipType.DESTROYER);
        defenderFleet.addShip(defenderShip);
        game.addFleet(defenderFleet, game.getPlayer(2));

        NavyTourAssignment tour = service.assignNavyTour(character, attackerFleet, "Test Empire", 6);

        // Create a completed battle where attacker wins
        Battle battle = new Battle(1, attackerFleet, defenderFleet, new Hex3D(0, 0, 0));
        battle.fightToCompletion();
        service.recordNavyBattle(tour, battle);

        // Should have battle events recorded
        assertTrue(tour.getEvents().size() >= 1);
        assertTrue(tour.getEvents().stream()
                .anyMatch(e -> e.getEventType() == NavyTourAssignment.TourEventType.BATTLE_ENGAGED));
    }

    @Test
    @DisplayName("Should record navy battle as defender victory")
    void testRecordNavyBattleDefenderVictory() {
        game.setupPlayer(1, "Test Player");
        game.setupPlayer(2, "Enemy Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet attackerFleet = new Fleet(1, "Attacker Fleet");
        Ship attackerShip = new Ship(1, ShipType.DESTROYER);
        attackerFleet.addShip(attackerShip);
        game.addFleet(attackerFleet, game.getPlayer(1));

        Fleet defenderFleet = new Fleet(2, "Defender Fleet");
        Ship defenderShip = new Ship(2, ShipType.DESTROYER);
        defenderFleet.addShip(defenderShip);
        game.addFleet(defenderFleet, game.getPlayer(2));
        empire.getNavy().addFleet(defenderFleet);

        NavyTourAssignment tour = service.assignNavyTour(character, defenderFleet, "Test Empire", 6);

        Battle battle = new Battle(1, attackerFleet, defenderFleet, new Hex3D(0, 0, 0));
        battle.fightToCompletion();
        service.recordNavyBattle(tour, battle);

        assertTrue(tour.getEvents().size() >= 1);
    }

    @Test
    @DisplayName("Should skip navy battle if fleet not involved")
    void testRecordNavyBattleNotInvolved() {
        game.setupPlayer(1, "Test Player");
        game.setupPlayer(2, "Enemy Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet tourFleet = new Fleet(1, "Tour Fleet");
        Ship tourShip = new Ship(1, ShipType.DESTROYER);
        tourFleet.addShip(tourShip);
        game.addFleet(tourFleet, game.getPlayer(1));
        empire.getNavy().addFleet(tourFleet);

        Fleet otherAttacker = new Fleet(3, "Other Attacker");
        Ship otherShip1 = new Ship(3, ShipType.DESTROYER);
        otherAttacker.addShip(otherShip1);
        game.addFleet(otherAttacker, game.getPlayer(2));

        Fleet otherDefender = new Fleet(4, "Other Defender");
        Ship otherShip2 = new Ship(4, ShipType.DESTROYER);
        otherDefender.addShip(otherShip2);
        game.addFleet(otherDefender, game.getPlayer(2));

        NavyTourAssignment tour = service.assignNavyTour(character, tourFleet, "Test Empire", 6);

        Battle battle = new Battle(1, otherAttacker, otherDefender, new Hex3D(0, 0, 0));
        battle.fightToCompletion();
        service.recordNavyBattle(tour, battle);

        // No events should be recorded since tour fleet is not involved
        assertEquals(0, tour.getEvents().size());
    }

    @Test
    @DisplayName("Should skip navy battle if tour is inactive")
    void testRecordNavyBattleInactiveTour() {
        game.setupPlayer(1, "Test Player");
        game.setupPlayer(2, "Enemy Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet attackerFleet = new Fleet(1, "Attacker Fleet");
        Ship attackerShip = new Ship(1, ShipType.DESTROYER);
        attackerFleet.addShip(attackerShip);
        game.addFleet(attackerFleet, game.getPlayer(1));
        empire.getNavy().addFleet(attackerFleet);

        Fleet defenderFleet = new Fleet(2, "Defender Fleet");
        Ship defenderShip = new Ship(2, ShipType.DESTROYER);
        defenderFleet.addShip(defenderShip);
        game.addFleet(defenderFleet, game.getPlayer(2));

        NavyTourAssignment tour = service.assignNavyTour(character, attackerFleet, "Test Empire", 6);
        tour.complete(); // Make inactive

        Battle battle = new Battle(1, attackerFleet, defenderFleet, new Hex3D(0, 0, 0));
        battle.fightToCompletion();
        service.recordNavyBattle(tour, battle);

        // No events should be recorded since tour is inactive
        assertEquals(0, tour.getEvents().size());
    }

    // ---- Fleet Destruction ----

    @Test
    @DisplayName("Should handle fleet destruction during turn processing")
    void testFleetDestructionInTurnProcessing() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        NavyTourAssignment tour = service.assignNavyTour(character, fleet, "Test Empire", 6);
        // Destroy the fleet by dealing massive damage
        fleet.distributeDamage(Integer.MAX_VALUE);

        int processed = service.processNavyTurnEnd(game);
        assertEquals(0, processed); // Destroyed fleet terminates the tour, not counted as processed
        assertFalse(tour.isActive());
        assertTrue(tour.getEvents().stream()
                .anyMatch(e -> e.getEventType() == NavyTourAssignment.TourEventType.FLEET_DESTROYED));
    }

    @Test
    @DisplayName("Should log fleet destruction experience")
    void testFleetDestructionLogged() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Test Empire", 6);
        fleet.distributeDamage(Integer.MAX_VALUE);
        service.processNavyTurnEnd(game);

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> losses = log.getByType(ExperienceType.LOSS);
        assertTrue(losses.size() >= 1);
    }

    // ---- Unit Destruction ----

    @Test
    @DisplayName("Should handle unit destruction during turn processing")
    void testUnitDestructionInTurnProcessing() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 6);
        // Destroy the unit
        unit.takeDamage(100);

        int processed = service.processArmyTurnEnd();
        assertEquals(0, processed); // Destroyed unit terminates the assignment, not counted as processed
        assertFalse(assignment.isActive());
        assertTrue(assignment.getEvents().stream()
                .anyMatch(e -> e.getEventType() == ArmyUnitAssignment.UnitEventType.UNIT_DESTROYED));
    }

    @Test
    @DisplayName("Should log unit destruction experience")
    void testUnitDestructionLogged() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 6);
        unit.takeDamage(100);
        service.processArmyTurnEnd();

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> losses = log.getByType(ExperienceType.LOSS);
        assertTrue(losses.size() >= 1);
    }

    // ---- Tour Completion via Turn Processing ----

    @Test
    @DisplayName("Should complete navy tour after all turns processed")
    void testNavyTourCompletionViaTurns() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        NavyTourAssignment tour = service.assignNavyTour(character, fleet, "Test Empire", 3);

        // Process 3 turns to complete the tour
        service.processNavyTurnEnd(game);
        service.processNavyTurnEnd(game);
        int processed = service.processNavyTurnEnd(game);

        // After completion, the tour should be removed from active
        assertFalse(tour.isActive());
        assertTrue(tour.isCompleted());
        assertNotNull(tour.getEndDate());
    }

    @Test
    @DisplayName("Should log navy tour completion experience")
    void testNavyTourCompletionLogged() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Test Empire", 2);

        service.processNavyTurnEnd(game);
        service.processNavyTurnEnd(game);

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> completions = log.getByType(ExperienceType.CAREER_COMPLETION);
        assertTrue(completions.size() >= 1);
    }

    @Test
    @DisplayName("Should complete army assignment after all turns processed")
    void testArmyAssignmentCompletionViaTurns() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 3);

        service.processArmyTurnEnd();
        service.processArmyTurnEnd();
        service.processArmyTurnEnd();

        assertFalse(assignment.isActive());
        assertTrue(assignment.isCompleted());
        assertNotNull(assignment.getEndDate());
    }

    @Test
    @DisplayName("Should log army assignment completion experience")
    void testArmyAssignmentCompletionLogged() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 2);

        service.processArmyTurnEnd();
        service.processArmyTurnEnd();

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> completions = log.getByType(ExperienceType.CAREER_COMPLETION);
        assertTrue(completions.size() >= 1);
    }

    @Test
    @DisplayName("Should remove completed navy tours from map after processing")
    void testCompletedNavyTourRemoved() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Test Empire", 2);
        assertEquals(1, service.getNavyTourCount());

        service.processNavyTurnEnd(game);
        service.processNavyTurnEnd(game);
        // Completed tour is removed on the next processing pass
        service.processNavyTurnEnd(game);

        // Completed tour should be removed from the map
        assertEquals(0, service.getNavyTourCount());
    }

    @Test
    @DisplayName("Should remove completed army assignments from map after processing")
    void testCompletedArmyAssignmentRemoved() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 2);
        assertEquals(1, service.getArmyAssignmentCount());

        service.processArmyTurnEnd();
        service.processArmyTurnEnd();
        // Completed assignment is removed on the next processing pass
        service.processArmyTurnEnd();

        assertEquals(0, service.getArmyAssignmentCount());
    }

    // ---- Army Combat Edge Cases ----

    @Test
    @DisplayName("Should record army combat with zero casualties")
    void testRecordArmyCombatZeroCasualties() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        service.recordArmyCombat(assignment, unit, true, 0);

        // Should have combat engaged and victory, but no casualties event
        assertEquals(2, assignment.getEvents().size());
        assertEquals(ArmyUnitAssignment.UnitEventType.COMBAT_ENGAGED, assignment.getEvents().get(0).getEventType());
        assertEquals(ArmyUnitAssignment.UnitEventType.COMBAT_VICTORY, assignment.getEvents().get(1).getEventType());
    }

    @Test
    @DisplayName("Should skip army combat if assignment is inactive")
    void testRecordArmyCombatInactive() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        assignment.complete();

        service.recordArmyCombat(assignment, unit, true, 5);

        assertEquals(0, assignment.getEvents().size());
    }

    @Test
    @DisplayName("Should skip army deployment if assignment is inactive")
    void testRecordArmyDeploymentInactive() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        assignment.complete();

        service.recordArmyDeployment(assignment, "New World");

        assertEquals(0, assignment.getEvents().size());
    }

    // ---- List Queries ----

    @Test
    @DisplayName("Should get all navy tours for a character")
    void testGetNavyTours() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        NavyTourAssignment tour = service.assignNavyTour(character, fleet, "Empire", 6);

        List<NavyTourAssignment> tours = service.getNavyTours(character);
        assertEquals(1, tours.size());
        assertSame(tour, tours.get(0));
    }

    @Test
    @DisplayName("Should return empty list for navy tours of unknown character")
    void testGetNavyToursUnknownCharacter() {
        TravellerCharacter unknown = logManager.createCharacter("Unknown", "Mars");
        unknown.rollAbilities();

        List<NavyTourAssignment> tours = service.getNavyTours(unknown);
        assertTrue(tours.isEmpty());
    }

    @Test
    @DisplayName("Should get all army assignments for a character")
    void testGetArmyAssignments() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 6);

        List<ArmyUnitAssignment> assignments = service.getArmyAssignments(character);
        assertEquals(1, assignments.size());
        assertSame(assignment, assignments.get(0));
    }

    @Test
    @DisplayName("Should return empty list for army assignments of unknown character")
    void testGetArmyAssignmentsUnknownCharacter() {
        TravellerCharacter unknown = logManager.createCharacter("Unknown", "Mars");
        unknown.rollAbilities();

        List<ArmyUnitAssignment> assignments = service.getArmyAssignments(unknown);
        assertTrue(assignments.isEmpty());
    }

    @Test
    @DisplayName("Should get all active navy tours")
    void testGetActiveNavyTours() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Empire", 6);

        List<NavyTourAssignment> active = service.getActiveNavyTours();
        assertEquals(1, active.size());
    }

    @Test
    @DisplayName("Should return empty list for active navy tours when none exist")
    void testGetActiveNavyToursEmpty() {
        List<NavyTourAssignment> active = service.getActiveNavyTours();
        assertTrue(active.isEmpty());
    }

    @Test
    @DisplayName("Should get all active army assignments")
    void testGetActiveArmyAssignments() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Empire", "Planet", 6);

        List<ArmyUnitAssignment> active = service.getActiveArmyAssignments();
        assertEquals(1, active.size());
    }

    @Test
    @DisplayName("Should return empty list for active army assignments when none exist")
    void testGetActiveArmyAssignmentsEmpty() {
        List<ArmyUnitAssignment> active = service.getActiveArmyAssignments();
        assertTrue(active.isEmpty());
    }

    // ---- Validation Edge Cases ----

    @Test
    @DisplayName("Should reject negative turns for navy tour")
    void testAssignNavyTourNegativeTurns() {
        Fleet fleet = new Fleet(1, "Test Fleet");
        assertThrows(IllegalArgumentException.class, () ->
                service.assignNavyTour(character, fleet, "Empire", -1));
    }

    @Test
    @DisplayName("Should reject negative turns for army assignment")
    void testAssignArmyUnitNegativeTurns() {
        ArmyUnit unit = new ArmyUnit(1, ArmyUnitType.INFANTRY);
        assertThrows(IllegalArgumentException.class, () ->
                service.assignArmyUnit(character, unit, "Empire", "Planet", -5));
    }

    @Test
    @DisplayName("Should reject zero turns for army assignment")
    void testAssignArmyUnitZeroTurns() {
        ArmyUnit unit = new ArmyUnit(1, ArmyUnitType.INFANTRY);
        assertThrows(IllegalArgumentException.class, () ->
                service.assignArmyUnit(character, unit, "Empire", "Planet", 0));
    }

    // ---- Tour ID Sequencing ----

    @Test
    @DisplayName("Should assign sequential IDs to navy tours")
    void testNavyTourIdSequencing() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet1 = new Fleet(1, "Fleet 1");
        Ship ship1 = new Ship(1, ShipType.DESTROYER);
        fleet1.addShip(ship1);
        game.addFleet(fleet1, game.getPlayer(1));
        empire.getNavy().addFleet(fleet1);

        Fleet fleet2 = new Fleet(2, "Fleet 2");
        Ship ship2 = new Ship(2, ShipType.DESTROYER);
        fleet2.addShip(ship2);
        game.addFleet(fleet2, game.getPlayer(1));
        empire.getNavy().addFleet(fleet2);

        TravellerCharacter char2 = logManager.createCharacter("Bob Smith", "Mars");
        char2.rollAbilities();

        NavyTourAssignment tour1 = service.assignNavyTour(character, fleet1, "Empire", 6);
        NavyTourAssignment tour2 = service.assignNavyTour(char2, fleet2, "Empire", 6);

        assertEquals(1, tour1.getId());
        assertEquals(2, tour2.getId());
    }

    @Test
    @DisplayName("Should assign sequential IDs to army assignments")
    void testArmyAssignmentIdSequencing() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit1 = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);
        ArmyUnit unit2 = empire.getArmy().createUnit(ArmyUnitType.MECHANIZED);

        TravellerCharacter char2 = logManager.createCharacter("Bob Smith", "Mars");
        char2.rollAbilities();

        ArmyUnitAssignment assign1 = service.assignArmyUnit(character, unit1, "Empire", "Planet", 6);
        ArmyUnitAssignment assign2 = service.assignArmyUnit(char2, unit2, "Empire", "Planet", 6);

        assertEquals(1, assign1.getId());
        assertEquals(2, assign2.getId());
    }

    // ---- Mixed Navy and Army ----

    @Test
    @DisplayName("Should handle navy and army tours independently")
    void testMixedNavyAndArmy() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        TravellerCharacter char2 = logManager.createCharacter("Bob Smith", "Mars");
        char2.rollAbilities();

        service.assignNavyTour(character, fleet, "Empire", 6);
        service.assignArmyUnit(char2, unit, "Empire", "Planet", 6);

        assertEquals(1, service.getActiveNavyTourCount());
        assertEquals(1, service.getActiveArmyAssignmentCount());

        // Process navy turn
        int navyProcessed = service.processNavyTurnEnd(game);
        assertEquals(1, navyProcessed);

        // Process army turn
        int armyProcessed = service.processArmyTurnEnd();
        assertEquals(1, armyProcessed);
    }

    // ---- Navy battle with ship losses ----

    @Test
    @DisplayName("Should record ship losses in navy battle")
    void testNavyBattleShipLosses() {
        game.setupPlayer(1, "Test Player");
        game.setupPlayer(2, "Enemy Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet attackerFleet = new Fleet(1, "Attacker Fleet");
        Ship attackerShip = new Ship(1, ShipType.DESTROYER);
        attackerFleet.addShip(attackerShip);
        game.addFleet(attackerFleet, game.getPlayer(1));
        empire.getNavy().addFleet(attackerFleet);

        Fleet defenderFleet = new Fleet(2, "Defender Fleet");
        Ship defenderShip = new Ship(2, ShipType.DESTROYER);
        defenderFleet.addShip(defenderShip);
        game.addFleet(defenderFleet, game.getPlayer(2));

        NavyTourAssignment tour = service.assignNavyTour(character, attackerFleet, "Test Empire", 6);

        Battle battle = new Battle(1, attackerFleet, defenderFleet, new Hex3D(0, 0, 0));
        battle.fightToCompletion();
        service.recordNavyBattle(tour, battle);

        CharacterLog log = logManager.getLog(character);
        // Should have a battle or mission failure experience logged
        int battleCount = log.getByType(ExperienceType.BATTLE).size()
                + log.getByType(ExperienceType.MISSION_FAILURE).size();
        assertTrue(battleCount >= 1, "Should have at least one battle-related experience");
    }

    // ---- Turn processing with no active tours ----

    @Test
    @DisplayName("Should return 0 when processing navy turn with no active tours")
    void testProcessNavyTurnEndNoActiveTours() {
        int processed = service.processNavyTurnEnd(game);
        assertEquals(0, processed);
    }

    @Test
    @DisplayName("Should return 0 when processing army turn with no active assignments")
    void testProcessArmyTurnEndNoActiveAssignments() {
        int processed = service.processArmyTurnEnd();
        assertEquals(0, processed);
    }

    // ---- Statistics after completion ----

    @Test
    @DisplayName("Should update active count after tour completion")
    void testActiveCountAfterCompletion() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Empire", 2);
        assertEquals(1, service.getActiveNavyTourCount());

        service.processNavyTurnEnd(game);
        assertEquals(1, service.getActiveNavyTourCount());

        service.processNavyTurnEnd(game);
        // Completed tour removed, active count should be 0
        assertEquals(0, service.getActiveNavyTourCount());
    }

    // ---- Navy battle experience logging ----

    @Test
    @DisplayName("Should log battle victory experience for navy tour")
    void testNavyBattleVictoryExperience() {
        game.setupPlayer(1, "Test Player");
        game.setupPlayer(2, "Enemy Player");
        Empire empire = game.getEmpires().values().iterator().next();

        Fleet attackerFleet = new Fleet(1, "Attacker Fleet");
        Ship attackerShip = new Ship(1, ShipType.DESTROYER);
        attackerFleet.addShip(attackerShip);
        game.addFleet(attackerFleet, game.getPlayer(1));
        empire.getNavy().addFleet(attackerFleet);

        Fleet defenderFleet = new Fleet(2, "Defender Fleet");
        Ship defenderShip = new Ship(2, ShipType.DESTROYER);
        defenderFleet.addShip(defenderShip);
        game.addFleet(defenderFleet, game.getPlayer(2));

        NavyTourAssignment tour = service.assignNavyTour(character, attackerFleet, "Test Empire", 6);

        Battle battle = new Battle(1, attackerFleet, defenderFleet, new Hex3D(0, 0, 0));
        battle.fightToCompletion();
        service.recordNavyBattle(tour, battle);

        CharacterLog log = logManager.getLog(character);
        // Check that some experience was logged
        assertTrue(log.getExperiences().size() >= 2,
                "Should have assignment + battle experience logged");
    }

    // ---- Army combat victory experience ----

    @Test
    @DisplayName("Should log combat victory experience for army assignment")
    void testArmyCombatVictoryExperience() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Empire", "Planet", 6);
        service.recordArmyCombat(service.getActiveArmyAssignment(character).orElseThrow(),
                unit, true, 5);

        CharacterLog log = logManager.getLog(character);
        List<CharacterExperience> battles = log.getByType(ExperienceType.BATTLE);
        assertTrue(battles.size() >= 1);
        assertEquals(CharacterExperience.Outcome.SUCCESS, battles.get(0).getOutcome());
    }

    // ---- Tour termination ----

    @Test
    @DisplayName("Should handle terminated navy tour in turn processing")
    void testTerminatedNavyTourInTurnProcessing() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        NavyTourAssignment tour = service.assignNavyTour(character, fleet, "Test Empire", 10);
        tour.terminate("Character reassigned");

        // Process turn - terminated tour is skipped (not counted as processed)
        int processed = service.processNavyTurnEnd(game);
        assertEquals(0, processed); // Terminated tours are skipped, not counted
        assertFalse(tour.isActive());
    }

    @Test
    @DisplayName("Should handle terminated army assignment in turn processing")
    void testTerminatedArmyAssignmentInTurnProcessing() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        ArmyUnitAssignment assignment = service.assignArmyUnit(character, unit, "Empire", "Planet", 10);
        assignment.terminate("Character reassigned");

        int processed = service.processArmyTurnEnd();
        assertEquals(0, processed); // Terminated assignments are skipped, not counted
        assertFalse(assignment.isActive());
    }

    // ---- Single turn tour ----

    @Test
    @DisplayName("Should complete single-turn navy tour in one processing step")
    void testSingleTurnNavyTour() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        Fleet fleet = new Fleet(1, "Test Fleet");
        Ship ship = new Ship(1, ShipType.DESTROYER);
        fleet.addShip(ship);
        game.addFleet(fleet, game.getPlayer(1));
        empire.getNavy().addFleet(fleet);

        service.assignNavyTour(character, fleet, "Test Empire", 1);
        service.processNavyTurnEnd(game);
        // Tour completed but still in map; next pass removes it
        service.processNavyTurnEnd(game);

        assertEquals(0, service.getActiveNavyTourCount());
    }

    @Test
    @DisplayName("Should complete single-turn army assignment in one processing step")
    void testSingleTurnArmyAssignment() {
        game.setupPlayer(1, "Test Player");
        Empire empire = game.getEmpires().values().iterator().next();
        ArmyUnit unit = empire.getArmy().createUnit(ArmyUnitType.INFANTRY);

        service.assignArmyUnit(character, unit, "Test Empire", "Coruscant", 1);
        service.processArmyTurnEnd();
        // Assignment completed but still in map; next pass removes it
        service.processArmyTurnEnd();

        assertEquals(0, service.getActiveArmyAssignmentCount());
    }
}
