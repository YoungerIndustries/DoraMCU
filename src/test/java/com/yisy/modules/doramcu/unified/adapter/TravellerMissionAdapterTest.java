package com.yisy.modules.doramcu.unified.adapter;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.ExplorationActivity;
import com.yisy.modules.doramcu.traveller.MercenaryTicket;
import com.yisy.modules.doramcu.traveller.MissionType;
import com.yisy.modules.doramcu.traveller.ShipDesign;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TradeCode;
import com.yisy.modules.doramcu.traveller.TradeMission;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.traveller.TravellerWorld;
import com.yisy.modules.doramcu.traveller.UniversalWorldProfile;
import com.yisy.modules.doramcu.traveller.WorldEconomy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TravellerMissionAdapter Tests")
class TravellerMissionAdapterTest {

    private TravellerMissionAdapter adapter;
    private TravellerCharacter character;

    @BeforeEach
    void setUp() {
        adapter = new TravellerMissionAdapter();
        character = new TravellerCharacter(1, "Alex Starborn");
        character.rollAbilities();
    }

    // ---- Career Term Adaptation ----

    @Test
    @DisplayName("Should adapt a career term into a mission with 3 acts")
    void testAdaptCareerTerm() {
        List<Skill> skills = List.of(Skill.MELEE, Skill.FIREARMS);
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, skills, "Enlisted", 10000, false, null);

        CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);

        assertNotNull(mission);
        assertEquals(1, mission.getMissionNumber());
        assertTrue(mission.getName().contains("Service Term 1"));
        assertEquals(CaptainLogMission.MissionType.COMBAT, mission.getType());
        assertEquals(3, mission.getCurrentAct());
        assertNotNull(mission.getAct1Log());
        assertNotNull(mission.getAct2Log());
        assertNotNull(mission.getAct3Log());
        assertTrue(mission.getAct1Log().contains("Alex"));
        assertTrue(mission.getAct1Log().contains("Soldier"));
    }

    @Test
    @DisplayName("Should set mission status to COMPLETED for retired tour")
    void testAdaptCareerTermRetired() {
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE), "Officer", 15000, true,
                TravellerCharacter.RetirementType.HONORABLE);

        CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 2);

        assertEquals(CaptainLogMission.MissionStatus.COMPLETED, mission.getStatus());
        assertNotNull(mission.getCompletedAt());
        assertTrue(mission.getAct3Log().contains("honorable"));
    }

    @Test
    @DisplayName("Should set career event flag on every 3rd term")
    void testCareerEventFlag() {
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(), "Enlisted", 5000, false, null);

        CaptainLogMission mission2 = adapter.adaptCareerTerm(character, tour, 2);
        assertFalse(mission2.isCareerEvent());

        CaptainLogMission mission3 = adapter.adaptCareerTerm(character, tour, 3);
        assertTrue(mission3.isCareerEvent());
        assertNotNull(mission3.getCareerEventDescription());
    }

    @Test
    @DisplayName("Should include skill check log entries for skills learned")
    void testSkillCheckLogEntries() {
        List<Skill> skills = List.of(Skill.MELEE, Skill.FIREARMS, Skill.SKIRMISH);
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, skills, "Enlisted", 10000, false, null);

        CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);

        List<CaptainLogEntry> entries = mission.getLogEntries();
        assertTrue(entries.size() >= 3); // at least 1 act1 + 3 skill checks + 1 act3

        long skillCheckCount = entries.stream()
                .filter(e -> e.getType() == CaptainLogEntry.EntryType.SKILL_CHECK)
                .count();
        assertEquals(3, skillCheckCount);
    }

    @Test
    @DisplayName("Should include values used based on career")
    void testValuesUsed() {
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(), "Enlisted", 5000, false, null);

        CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);

        assertNotNull(mission.getValuesUsed());
        assertTrue(mission.getValuesUsed().contains("Duty"));
    }

    @Test
    @DisplayName("Should adapt full career into multiple missions")
    void testAdaptFullCareer() {
        // Add career tours
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE), "Enlisted", 10000, false, null));
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.FIREARMS), "Officer", 15000, true,
                TravellerCharacter.RetirementType.NORMAL));

        List<CaptainLogMission> missions = adapter.adaptFullCareer(character);

        assertEquals(2, missions.size());
        assertEquals(1, missions.get(0).getMissionNumber());
        assertEquals(2, missions.get(1).getMissionNumber());
    }

    // ---- Mercenary Contract Adaptation ----

    @Test
    @DisplayName("Should adapt a successful mercenary contract")
    void testAdaptSuccessfulMercenaryContract() {
        MercenaryTicket ticket = new MercenaryTicket(
                1, "Commando Contract", "Patron-123", MissionType.COMMANDO,
                MercenaryTicket.PaymentType.UPFRONT, MercenaryTicket.TicketType.SHORT,
                50000, 25000, 0, 5000, 3, 100, 8,
                "Commando mission on Tarn VI", "Tarn VI");

        CaptainLogMission mission = adapter.adaptMercenaryContract(
                character, ticket, true, 75000, 10, 100);

        assertNotNull(mission);
        assertTrue(mission.getName().contains("Commando"));
        assertEquals(CaptainLogMission.MissionType.COMBAT, mission.getType());
        assertEquals(CaptainLogMission.MissionStatus.COMPLETED, mission.getStatus());
        assertTrue(mission.getAct2Log().contains("objective") ||
                mission.getAct2Log().contains("secured"),
                "Act 2 should contain success language: " + mission.getAct2Log());
        assertTrue(mission.getSummary().contains("SUCCESS"));
        assertTrue(mission.getSummary().contains("75000"));
    }

    @Test
    @DisplayName("Should adapt a failed mercenary contract")
    void testAdaptFailedMercenaryContract() {
        MercenaryTicket ticket = new MercenaryTicket(
                2, "Striker Contract", "Patron-456", MissionType.STRIKER,
                MercenaryTicket.PaymentType.UPFRONT, MercenaryTicket.TicketType.LONG,
                100000, 50000, 100000, 10000, 6, 200, 10,
                "Striker mission on Lone Mountain", "Lone Mountain");

        CaptainLogMission mission = adapter.adaptMercenaryContract(
                character, ticket, false, -20000, 50, 200);

        assertEquals(CaptainLogMission.MissionStatus.ABANDONED, mission.getStatus());
        assertTrue(mission.getAct2Log().toLowerCase().contains("defeat") ||
                mission.getAct2Log().toLowerCase().contains("withdrawal"),
                "Act 2 should contain defeat language: " + mission.getAct2Log());
        assertTrue(mission.getSummary().contains("FAILURE"));
    }

    @Test
    @DisplayName("Should include combat log entry for mercenary missions")
    void testMercenaryCombatEntry() {
        MercenaryTicket ticket = new MercenaryTicket(
                3, "Security Contract", "Patron-789", MissionType.SECURITY,
                MercenaryTicket.PaymentType.UPFRONT, MercenaryTicket.TicketType.SHORT,
                20000, 10000, 0, 2000, 2, 50, 7,
                "Security mission on World-100", "World-100");

        CaptainLogMission mission = adapter.adaptMercenaryContract(
                character, ticket, true, 30000, 5, 50);

        List<CaptainLogEntry> entries = mission.getLogEntries();
        long combatCount = entries.stream()
                .filter(e -> e.getType() == CaptainLogEntry.EntryType.COMBAT)
                .count();
        assertEquals(1, combatCount);
    }

    // ---- Trade Mission Adaptation ----

    @Test
    @DisplayName("Should adapt a profitable trade mission")
    void testAdaptProfitableTradeMission() {
        // Set up origin and destination worlds
        TravellerWorld origin = new TravellerWorld(1, "Sol", "A6666600", 0, 0);
        TravellerWorld destination = new TravellerWorld(2, "Tarn", "A8888800", 1, 0);

        // Create ship design
        ShipDesign ship = new ShipDesign(1, "Test Ship", 2000);

        // Create and execute trade mission
        TradeMission tradeMission = new TradeMission(origin, destination, ship, 100000);
        WorldEconomy originEcon = new WorldEconomy(origin);
        WorldEconomy destEcon = new WorldEconomy(destination);

        // Execute the trade mission
        tradeMission.loadCargo(originEcon, destEcon);
        tradeMission.travel();
        tradeMission.unloadCargo(destEcon);

        CaptainLogMission mission = adapter.adaptTradeMission(character, tradeMission);

        assertNotNull(mission);
        assertTrue(mission.getName().contains("Sol"));
        assertTrue(mission.getName().contains("Tarn"));
        assertEquals(CaptainLogMission.MissionType.TRADE, mission.getType());
        assertEquals(3, mission.getCurrentAct());
        assertNotNull(mission.getAct1Log());
        assertNotNull(mission.getAct2Log());
        assertNotNull(mission.getAct3Log());
        assertTrue(mission.getAct1Log().contains("Sol"));
        assertTrue(mission.getAct1Log().contains("Tarn"));
    }

    @Test
    @DisplayName("Should adapt a trade mission with loss")
    void testAdaptTradeMissionWithLoss() {
        // Set up worlds where trade is unlikely to be profitable
        TravellerWorld origin = new TravellerWorld(1, "Alpha", "A0000000", 0, 0);
        TravellerWorld destination = new TravellerWorld(2, "Beta", "A0000000", 1, 0);

        ShipDesign ship = new ShipDesign(1, "Test Ship", 2000);
        TradeMission tradeMission = new TradeMission(origin, destination, ship, 100000);
        WorldEconomy originEcon = new WorldEconomy(origin);
        WorldEconomy destEcon = new WorldEconomy(destination);

        tradeMission.loadCargo(originEcon, destEcon);
        tradeMission.travel();
        tradeMission.unloadCargo(destEcon);

        CaptainLogMission mission = adapter.adaptTradeMission(character, tradeMission);

        assertNotNull(mission);
        assertNotNull(mission.getSummary());
        // Summary should contain the route
        assertTrue(mission.getSummary().contains("Alpha"));
        assertTrue(mission.getSummary().contains("Beta"));
    }

    @Test
    @DisplayName("Should include log entries for trade mission")
    void testTradeMissionLogEntries() {
        TravellerWorld origin = new TravellerWorld(1, "Sol", "A6666600", 0, 0);
        TravellerWorld destination = new TravellerWorld(2, "Tarn", "A8888800", 1, 0);

        ShipDesign ship = new ShipDesign(1, "Test Ship", 2000);
        TradeMission tradeMission = new TradeMission(origin, destination, ship, 100000);
        WorldEconomy originEcon = new WorldEconomy(origin);
        WorldEconomy destEcon = new WorldEconomy(destination);

        tradeMission.loadCargo(originEcon, destEcon);
        tradeMission.travel();
        tradeMission.unloadCargo(destEcon);

        CaptainLogMission mission = adapter.adaptTradeMission(character, tradeMission);

        List<CaptainLogEntry> entries = mission.getLogEntries();
        assertTrue(entries.size() >= 3, "Should have at least 3 log entries");
    }

    // ---- Exploration Activity Adaptation ----

    @Test
    @DisplayName("Should adapt a successful exploration with discovery")
    void testAdaptSuccessfulExploration() {
        TravellerWorld system = new TravellerWorld(1, "Alpha Centauri", "A0885400", 0, 0);

        ExplorationActivity activity = new ExplorationActivity(
                ExplorationActivity.ActivityType.SURVEY_BODY, "Planet A",
                system, 2, true, ExplorationActivity.Discovery.HABITABLE_WORLD,
                "Discovered potentially habitable world requiring further survey.");

        CaptainLogMission mission = adapter.adaptExplorationActivity(character, activity);

        assertNotNull(mission);
        assertTrue(mission.getName().contains("SURVEY BODY"));
        assertEquals(CaptainLogMission.MissionType.SCIENCE, mission.getType());
        assertEquals(CaptainLogMission.MissionStatus.COMPLETED, mission.getStatus());
        assertTrue(mission.getAct2Log().contains("habitable world"));
        assertTrue(mission.getSummary().contains("SUCCESS"));
    }

    @Test
    @DisplayName("Should adapt a failed exploration")
    void testAdaptFailedExploration() {
        TravellerWorld system = new TravellerWorld(2, "Beta Hydri", "06978300", 0, 0);

        ExplorationActivity activity = new ExplorationActivity(
                ExplorationActivity.ActivityType.RESOURCE_SCAN, "Asteroid Belt",
                system, 1, false, ExplorationActivity.Discovery.NONE,
                "Activity failed — insufficient data collected.");

        CaptainLogMission mission = adapter.adaptExplorationActivity(character, activity);

        assertEquals(CaptainLogMission.MissionStatus.ABANDONED, mission.getStatus());
        assertTrue(mission.getAct2Log().contains("difficulties") ||
                mission.getAct2Log().contains("insufficient"),
                "Act 2 should contain failure language: " + mission.getAct2Log());
        assertTrue(mission.getSummary().contains("FAILED"));
    }

    @Test
    @DisplayName("Should include discovery log entry for successful exploration")
    void testExplorationDiscoveryEntry() {
        TravellerWorld system = new TravellerWorld(3, "Gamma Volantis", "A8866400", 0, 0);

        ExplorationActivity activity = new ExplorationActivity(
                ExplorationActivity.ActivityType.FULL_SYSTEM_SURVEY, "Gamma Volantis System",
                system, 4, true, ExplorationActivity.Discovery.ANCIENT_ARTIFACTS,
                "Ancient structures detected.");

        CaptainLogMission mission = adapter.adaptExplorationActivity(character, activity);

        List<CaptainLogEntry> entries = mission.getLogEntries();
        long discoveryCount = entries.stream()
                .filter(e -> e.getType() == CaptainLogEntry.EntryType.DISCOVERY)
                .count();
        assertEquals(1, discoveryCount);
    }

    // ---- Character Creation Adaptation ----

    @Test
    @DisplayName("Should adapt character creation with abilities mapped")
    void testAdaptCharacterCreation() {
        // Set specific abilities
        character.setAbility(AbilityScore.ST, 8);
        character.setAbility(AbilityScore.DEX, 10);
        character.setAbility(AbilityScore.END, 7);
        character.setAbility(AbilityScore.INT, 9);
        character.setAbility(AbilityScore.EDU, 11);
        character.setAbility(AbilityScore.SOC, 6);
        character.setAbility(AbilityScore.APP, 8);

        CaptainLogCharacter clChar = adapter.adaptCharacterCreation(character);

        assertNotNull(clChar);
        assertEquals("Alex Starborn", clChar.getName());
        assertEquals(8, clChar.getStrength());
        assertEquals(10, clChar.getFitness());
        assertEquals(9, clChar.getReason());
        assertEquals(11, clChar.getInsight());
    }

    @Test
    @DisplayName("Should map primary career to assignment")
    void testMapPrimaryCareerToAssignment() {
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE), "Enlisted", 10000, true,
                TravellerCharacter.RetirementType.NORMAL));

        CaptainLogCharacter clChar = adapter.adaptCharacterCreation(character);

        assertEquals(CaptainLogCharacter.Assignment.SECURITY, clChar.getAssignment());
    }

    @Test
    @DisplayName("Should map highest rank achieved")
    void testMapHighestRank() {
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE), "Enlisted", 10000, false, null));
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.FIREARMS), "Officer", 15000, true,
                TravellerCharacter.RetirementType.NORMAL));

        CaptainLogCharacter clChar = adapter.adaptCharacterCreation(character);

        assertEquals(CaptainLogCharacter.Rank.LIEUTENANT_COMMANDER, clChar.getRank());
    }

    @Test
    @DisplayName("Should set missions completed from career tours")
    void testMissionsCompleted() {
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(), "Enlisted", 10000, false, null));
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(), "Officer", 15000, true,
                TravellerCharacter.RetirementType.NORMAL));

        CaptainLogCharacter clChar = adapter.adaptCharacterCreation(character);

        assertEquals(2, clChar.getMissionsCompleted());
    }

    @Test
    @DisplayName("Should assign disciplines from skills")
    void testAssignDisciplinesFromSkills() {
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE, Skill.FIREARMS, Skill.SKIRMISH),
                "Enlisted", 10000, true, TravellerCharacter.RetirementType.NORMAL));

        CaptainLogCharacter clChar = adapter.adaptCharacterCreation(character);

        // Security should be > 0 due to combat skills
        assertTrue(clChar.getSecurity() > 0, "Security should be > 0 for combat skills");
    }

    // ---- Integration: Full lifecycle ----

    @Test
    @DisplayName("Full lifecycle: create character, career, mercenary, exploration")
    void testFullLifecycle() {
        // Create character with career
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE, Skill.FIREARMS),
                "Enlisted", 10000, false, null));
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.TACTICS),
                "Officer", 15000, true, TravellerCharacter.RetirementType.HONORABLE));

        // Adapt career
        List<CaptainLogMission> careerMissions = adapter.adaptFullCareer(character);
        assertEquals(2, careerMissions.size());

        // Adapt mercenary contract
        MercenaryTicket ticket = new MercenaryTicket(
                1, "Commando Contract", "Patron-123", MissionType.COMMANDO,
                MercenaryTicket.PaymentType.UPFRONT, MercenaryTicket.TicketType.SHORT,
                50000, 25000, 0, 5000, 3, 100, 8,
                "Commando mission on Tarn VI", "Tarn VI");
        CaptainLogMission mercenaryMission = adapter.adaptMercenaryContract(
                character, ticket, true, 75000, 10, 100);
        assertNotNull(mercenaryMission);

        // Adapt exploration
        TravellerWorld system = new TravellerWorld(1, "Alpha Centauri", "A0885400", 0, 0);
        ExplorationActivity activity = new ExplorationActivity(
                ExplorationActivity.ActivityType.SURVEY_BODY, "Planet A",
                system, 2, true, ExplorationActivity.Discovery.HABITABLE_WORLD,
                "Discovered potentially habitable world.");
        CaptainLogMission explorationMission = adapter.adaptExplorationActivity(
                character, activity);
        assertNotNull(explorationMission);

        // Adapt character creation
        CaptainLogCharacter clChar = adapter.adaptCharacterCreation(character);
        assertNotNull(clChar);
        assertEquals(CaptainLogCharacter.Assignment.SECURITY, clChar.getAssignment());

        // Verify all missions have 3 acts
        for (CaptainLogMission mission : careerMissions) {
            assertEquals(3, mission.getCurrentAct());
            assertNotNull(mission.getAct1Log());
            assertNotNull(mission.getAct2Log());
            assertNotNull(mission.getAct3Log());
        }
        assertEquals(3, mercenaryMission.getCurrentAct());
        assertEquals(3, explorationMission.getCurrentAct());
    }

    // ---- Edge cases ----

    @Test
    @DisplayName("Should handle death in career tour")
    void testDeathInCareer() {
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE), "Enlisted", 5000, false,
                TravellerCharacter.RetirementType.DEAD);
        character.setAlive(false);

        CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);

        assertEquals(CaptainLogMission.MissionStatus.ABANDONED, mission.getStatus());
    }

    @Test
    @DisplayName("Should handle empty skills list")
    void testEmptySkillsList() {
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.NOBLE, 4, Collections.emptyList(), "Enlisted", 8000, true,
                TravellerCharacter.RetirementType.NORMAL);

        CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);

        assertNotNull(mission);
        assertNotNull(mission.getSummary());
        assertTrue(mission.getSummary().contains("0 new skills"));
    }

    @Test
    @DisplayName("Should handle all career types in values mapping")
    void testAllCareerValues() {
        for (Career career : Career.values()) {
            TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                    career, 4, List.of(), "Enlisted", 5000, false, null);
            CaptainLogMission mission = adapter.adaptCareerTerm(character, tour, 1);
            assertNotNull(mission.getValuesUsed(), "Values should be set for " + career);
            assertFalse(mission.getValuesUsed().isBlank(), "Values should not be blank for " + career);
        }
    }
}
