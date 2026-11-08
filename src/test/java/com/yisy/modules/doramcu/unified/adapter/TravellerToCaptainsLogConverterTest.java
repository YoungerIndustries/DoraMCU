package com.yisy.modules.doramcu.unified.adapter;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.ExplorationActivity;
import com.yisy.modules.doramcu.traveller.MissionType;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TradeMission;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TravellerToCaptainsLogConverter Tests")
class TravellerToCaptainsLogConverterTest {

    // ---- Career → Assignment Mapping ----

    @Test
    @DisplayName("Should map SOLDIER to SECURITY assignment")
    void testSoldierToSecurity() {
        assertEquals(CaptainLogCharacter.Assignment.SECURITY,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.SOLDIER));
    }

    @Test
    @DisplayName("Should map GUNNER to SECURITY assignment")
    void testGunnerToSecurity() {
        assertEquals(CaptainLogCharacter.Assignment.SECURITY,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.GUNNER));
    }

    @Test
    @DisplayName("Should map SPY to SECURITY assignment")
    void testSpyToSecurity() {
        assertEquals(CaptainLogCharacter.Assignment.SECURITY,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.SPY));
    }

    @Test
    @DisplayName("Should map EXPLORER to SCIENCE assignment")
    void testExplorerToScience() {
        assertEquals(CaptainLogCharacter.Assignment.SCIENCE,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.EXPLORER));
    }

    @Test
    @DisplayName("Should map SCOUT to SCIENCE assignment")
    void testScoutToScience() {
        assertEquals(CaptainLogCharacter.Assignment.SCIENCE,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.SCOUT));
    }

    @Test
    @DisplayName("Should map MERCHANT to OPERATIONS assignment")
    void testMerchantToOperations() {
        assertEquals(CaptainLogCharacter.Assignment.OPERATIONS,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.MERCHANT));
    }

    @Test
    @DisplayName("Should map PILOT to OPERATIONS assignment")
    void testPilotToOperations() {
        assertEquals(CaptainLogCharacter.Assignment.OPERATIONS,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.PILOT));
    }

    @Test
    @DisplayName("Should map NOBLE to COMMAND assignment")
    void testNobleToCommand() {
        assertEquals(CaptainLogCharacter.Assignment.COMMAND,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(Career.NOBLE));
    }

    @Test
    @DisplayName("Should map null career to COMMAND assignment")
    void testNullCareerToCommand() {
        assertEquals(CaptainLogCharacter.Assignment.COMMAND,
                TravellerToCaptainsLogConverter.mapCareerToAssignment(null));
    }

    // ---- Rank Mapping ----

    @Test
    @DisplayName("Should map Enlisted to ENSIGN")
    void testEnlistedToEnsign() {
        assertEquals(CaptainLogCharacter.Rank.ENSIGN,
                TravellerToCaptainsLogConverter.mapRank("Enlisted"));
    }

    @Test
    @DisplayName("Should map Senior Enlisted to LIEUTENANT_JUNIOR")
    void testSeniorEnlistedToLtJunior() {
        assertEquals(CaptainLogCharacter.Rank.LIEUTENANT_JUNIOR,
                TravellerToCaptainsLogConverter.mapRank("Senior Enlisted"));
    }

    @Test
    @DisplayName("Should map Junior Officer to LIEUTENANT")
    void testJuniorOfficerToLieutenant() {
        assertEquals(CaptainLogCharacter.Rank.LIEUTENANT,
                TravellerToCaptainsLogConverter.mapRank("Junior Officer"));
    }

    @Test
    @DisplayName("Should map Officer to LIEUTENANT_COMMANDER")
    void testOfficerToLtCommander() {
        assertEquals(CaptainLogCharacter.Rank.LIEUTENANT_COMMANDER,
                TravellerToCaptainsLogConverter.mapRank("Officer"));
    }

    @Test
    @DisplayName("Should map Senior Officer to COMMANDER")
    void testSeniorOfficerToCommander() {
        assertEquals(CaptainLogCharacter.Rank.COMMANDER,
                TravellerToCaptainsLogConverter.mapRank("Senior Officer"));
    }

    @Test
    @DisplayName("Should map Commanding Officer to CAPTAIN")
    void testCommandingOfficerToCaptain() {
        assertEquals(CaptainLogCharacter.Rank.CAPTAIN,
                TravellerToCaptainsLogConverter.mapRank("Commanding Officer"));
    }

    @Test
    @DisplayName("Should map null rank to ENSIGN")
    void testNullRankToEnsign() {
        assertEquals(CaptainLogCharacter.Rank.ENSIGN,
                TravellerToCaptainsLogConverter.mapRank(null));
    }

    @Test
    @DisplayName("Should map unknown rank to ENSIGN")
    void testUnknownRankToEnsign() {
        assertEquals(CaptainLogCharacter.Rank.ENSIGN,
                TravellerToCaptainsLogConverter.mapRank("Admiral"));
    }

    // ---- Mission Type Mapping ----

    @Test
    @DisplayName("Should map COMMANDO to COMBAT mission type")
    void testCommandoToCombat() {
        assertEquals(CaptainLogMission.MissionType.COMBAT,
                TravellerToCaptainsLogConverter.mapMissionType(MissionType.COMMANDO));
    }

    @Test
    @DisplayName("Should map CADRE to DIPLOMACY mission type")
    void testCadreToDiplomacy() {
        assertEquals(CaptainLogMission.MissionType.DIPLOMACY,
                TravellerToCaptainsLogConverter.mapMissionType(MissionType.CADRE));
    }

    @Test
    @DisplayName("Should map STRIKER to COMBAT mission type")
    void testStrikerToCombat() {
        assertEquals(CaptainLogMission.MissionType.COMBAT,
                TravellerToCaptainsLogConverter.mapMissionType(MissionType.STRIKER));
    }

    @Test
    @DisplayName("Should map SECURITY to CRISIS mission type")
    void testSecurityToCrisis() {
        assertEquals(CaptainLogMission.MissionType.CRISIS,
                TravellerToCaptainsLogConverter.mapMissionType(MissionType.SECURITY));
    }

    @Test
    @DisplayName("Should map null mission type to COMBAT")
    void testNullMissionTypeToCombat() {
        assertEquals(CaptainLogMission.MissionType.COMBAT,
                TravellerToCaptainsLogConverter.mapMissionType(null));
    }

    @Test
    @DisplayName("Should return TRADE for trade missions")
    void testTradeMissionType() {
        assertEquals(CaptainLogMission.MissionType.TRADE,
                TravellerToCaptainsLogConverter.mapTradeMissionType());
    }

    // ---- Exploration Type Mapping ----

    @Test
    @DisplayName("Should map SURVEY_BODY to SCIENCE")
    void testSurveyBodyToScience() {
        assertEquals(CaptainLogMission.MissionType.SCIENCE,
                TravellerToCaptainsLogConverter.mapExplorationType(ExplorationActivity.ActivityType.SURVEY_BODY));
    }

    @Test
    @DisplayName("Should map HAZARD_DETECTION to EXPLORATION")
    void testHazardDetectionToExploration() {
        assertEquals(CaptainLogMission.MissionType.EXPLORATION,
                TravellerToCaptainsLogConverter.mapExplorationType(ExplorationActivity.ActivityType.HAZARD_DETECTION));
    }

    @Test
    @DisplayName("Should map null activity type to EXPLORATION")
    void testNullActivityTypeToExploration() {
        assertEquals(CaptainLogMission.MissionType.EXPLORATION,
                TravellerToCaptainsLogConverter.mapExplorationType(null));
    }

    // ---- Career to Mission Type Mapping ----

    @Test
    @DisplayName("Should map SOLDIER career to COMBAT mission type")
    void testSoldierCareerToCombat() {
        assertEquals(CaptainLogMission.MissionType.COMBAT,
                TravellerToCaptainsLogConverter.mapCareerToMissionType(Career.SOLDIER));
    }

    @Test
    @DisplayName("Should map MERCHANT career to TRADE mission type")
    void testMerchantCareerToTrade() {
        assertEquals(CaptainLogMission.MissionType.TRADE,
                TravellerToCaptainsLogConverter.mapCareerToMissionType(Career.MERCHANT));
    }

    @Test
    @DisplayName("Should map SPY career to INVESTIGATION mission type")
    void testSpyCareerToInvestigation() {
        assertEquals(CaptainLogMission.MissionType.INVESTIGATION,
                TravellerToCaptainsLogConverter.mapCareerToMissionType(Career.SPY));
    }

    @Test
    @DisplayName("Should map null career to EXPLORATION mission type")
    void testNullCareerToExploration() {
        assertEquals(CaptainLogMission.MissionType.EXPLORATION,
                TravellerToCaptainsLogConverter.mapCareerToMissionType(null));
    }

    // ---- Ability → Attribute Mapping ----

    @Test
    @DisplayName("Should map ST to strength")
    void testStToStrength() {
        assertEquals("strength",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.ST));
    }

    @Test
    @DisplayName("Should map DEX to dexterity")
    void testDexToDexterity() {
        assertEquals("dexterity",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.DEX));
    }

    @Test
    @DisplayName("Should map END to constitution")
    void testEndToConstitution() {
        assertEquals("constitution",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.END));
    }

    @Test
    @DisplayName("Should map INT to intelligence")
    void testIntToIntelligence() {
        assertEquals("intelligence",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.INT));
    }

    @Test
    @DisplayName("Should map EDU to wisdom")
    void testEduToWisdom() {
        assertEquals("wisdom",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.EDU));
    }

    @Test
    @DisplayName("Should map SOC to charisma")
    void testSocToCharisma() {
        assertEquals("charisma",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.SOC));
    }

    @Test
    @DisplayName("Should map APP to technical")
    void testAppToTechnical() {
        assertEquals("technical",
                TravellerToCaptainsLogConverter.mapAbilityToAttribute(AbilityScore.APP));
    }

    @Test
    @DisplayName("Should map null ability to null")
    void testNullAbilityToNull() {
        assertNull(TravellerToCaptainsLogConverter.mapAbilityToAttribute(null));
    }

    // ---- Retirement → Mission Status Mapping ----

    @Test
    @DisplayName("Should map NORMAL retirement to COMPLETED")
    void testNormalRetirementToCompleted() {
        assertEquals(CaptainLogMission.MissionStatus.COMPLETED,
                TravellerToCaptainsLogConverter.mapRetirementToStatus(
                        TravellerCharacter.RetirementType.NORMAL));
    }

    @Test
    @DisplayName("Should map HONORABLE retirement to COMPLETED")
    void testHonorableRetirementToCompleted() {
        assertEquals(CaptainLogMission.MissionStatus.COMPLETED,
                TravellerToCaptainsLogConverter.mapRetirementToStatus(
                        TravellerCharacter.RetirementType.HONORABLE));
    }

    @Test
    @DisplayName("Should map MEDICAL retirement to ABANDONED")
    void testMedicalRetirementToAbandoned() {
        assertEquals(CaptainLogMission.MissionStatus.ABANDONED,
                TravellerToCaptainsLogConverter.mapRetirementToStatus(
                        TravellerCharacter.RetirementType.MEDICAL));
    }

    @Test
    @DisplayName("Should map DEAD retirement to ABANDONED")
    void testDeadRetirementToAbandoned() {
        assertEquals(CaptainLogMission.MissionStatus.ABANDONED,
                TravellerToCaptainsLogConverter.mapRetirementToStatus(
                        TravellerCharacter.RetirementType.DEAD));
    }

    @Test
    @DisplayName("Should map null retirement to COMPLETED")
    void testNullRetirementToCompleted() {
        assertEquals(CaptainLogMission.MissionStatus.COMPLETED,
                TravellerToCaptainsLogConverter.mapRetirementToStatus(null));
    }

    // ---- Narrative Helpers ----

    @Test
    @DisplayName("Should generate career mission name")
    void testGenerateCareerMissionName() {
        String name = TravellerToCaptainsLogConverter.generateCareerMissionName(
                Career.SOLDIER, "Officer", 2);
        assertEquals("Service Term 2: Officer Soldier", name);
    }

    @Test
    @DisplayName("Should generate mercenary mission name")
    void testGenerateMercenaryMissionName() {
        String name = TravellerToCaptainsLogConverter.generateMercenaryMissionName(
                MissionType.COMMANDO, "Tarn VI");
        assertEquals("Commando Operation: Tarn VI", name);
    }

    @Test
    @DisplayName("Should generate trade mission name")
    void testGenerateTradeMissionName() {
        String name = TravellerToCaptainsLogConverter.generateTradeMissionName("Sol", "Tarn");
        assertEquals("Trade Run: Sol to Tarn", name);
    }

    @Test
    @DisplayName("Should generate exploration mission name")
    void testGenerateExplorationMissionName() {
        String name = TravellerToCaptainsLogConverter.generateExplorationMissionName(
                ExplorationActivity.ActivityType.SURVEY_BODY, "Planet A");
        assertTrue(name.contains("Exploration"));
        assertTrue(name.contains("SURVEY BODY"));
        assertTrue(name.contains("Planet A"));
    }

    @Test
    @DisplayName("Should generate career Act 1 narrative")
    void testGenerateCareerAct1() {
        String act1 = TravellerToCaptainsLogConverter.generateCareerAct1(
                "Alex", Career.SOLDIER, "Enlisted", 1);
        assertTrue(act1.contains("Alex"));
        assertTrue(act1.contains("Soldier"));
        assertTrue(act1.contains("Enlisted"));
    }

    @Test
    @DisplayName("Should generate career Act 2 narrative with skills")
    void testGenerateCareerAct2WithSkills() {
        List<Skill> skills = List.of(Skill.MELEE, Skill.FIREARMS);
        String act2 = TravellerToCaptainsLogConverter.generateCareerAct2(skills, false, null);
        assertTrue(act2.contains("Melee"));
        assertTrue(act2.contains("Firearms"));
    }

    @Test
    @DisplayName("Should generate career Act 2 narrative with promotion")
    void testGenerateCareerAct2WithPromotion() {
        String act2 = TravellerToCaptainsLogConverter.generateCareerAct2(
                List.of(), true, "Officer");
        assertTrue(act2.contains("Officer"));
        assertTrue(act2.contains("promotion"));
    }

    @Test
    @DisplayName("Should generate career Act 3 narrative for non-retired")
    void testGenerateCareerAct3NotRetired() {
        String act3 = TravellerToCaptainsLogConverter.generateCareerAct3(
                "Alex", false, null, 10000);
        assertTrue(act3.contains("Alex"));
        assertTrue(act3.contains("10000"));
    }

    @Test
    @DisplayName("Should generate career Act 3 narrative for retired")
    void testGenerateCareerAct3Retired() {
        String act3 = TravellerToCaptainsLogConverter.generateCareerAct3(
                "Alex", true, TravellerCharacter.RetirementType.HONORABLE, 20000);
        assertTrue(act3.contains("Alex"));
        assertTrue(act3.contains("honorable"));
        assertTrue(act3.contains("20000"));
    }

    @Test
    @DisplayName("Should generate career development with skills")
    void testGenerateCareerDevelopmentWithSkills() {
        String dev = TravellerToCaptainsLogConverter.generateCareerDevelopment(
                List.of(Skill.MELEE), false);
        assertTrue(dev.contains("1 new skill"));
    }

    @Test
    @DisplayName("Should generate career development with promotion")
    void testGenerateCareerDevelopmentWithPromotion() {
        String dev = TravellerToCaptainsLogConverter.generateCareerDevelopment(
                List.of(), true);
        assertTrue(dev.contains("Promoted"));
    }

    @Test
    @DisplayName("Should generate career development for plain completion")
    void testGenerateCareerDevelopmentPlain() {
        String dev = TravellerToCaptainsLogConverter.generateCareerDevelopment(
                List.of(), false);
        assertTrue(dev.contains("Completed term"));
    }
}
