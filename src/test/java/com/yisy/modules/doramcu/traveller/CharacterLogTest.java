package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CharacterLog Tests")
class CharacterLogTest {

    private TravellerCharacter makeCharacter() {
        TravellerCharacter c = new TravellerCharacter(1, "Alex Starborn");
        c.setAbility(AbilityScore.DEX, 8);
        c.setAbility(AbilityScore.ST, 7);
        c.setAbility(AbilityScore.END, 9);
        c.setAbility(AbilityScore.INT, 10);
        c.setAbility(AbilityScore.EDU, 6);
        c.setAbility(AbilityScore.SOC, 5);
        c.setAbility(AbilityScore.APP, 8);
        return c;
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create log for a character")
    void testConstructor() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);

        assertSame(character, log.getCharacter());
        assertEquals(0, log.getExperienceCount());
        assertNotNull(log.getCreatedAt());
        assertTrue(log.getExperiences().isEmpty());
    }

    @Test
    @DisplayName("Should throw on null character")
    void testNullCharacter() {
        assertThrows(NullPointerException.class, () -> new CharacterLog(null));
    }

    // ---- Adding experiences ----

    @Test
    @DisplayName("Should add experience and return immutable list")
    void testAddExperience() {
        CharacterLog log = new CharacterLog(makeCharacter());
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "First Fight");
        log.addExperience(exp);

        assertEquals(1, log.getExperienceCount());
        List<CharacterExperience> list = log.getExperiences();
        assertEquals(1, list.size());
        assertSame(exp, list.get(0));

        // Verify immutability
        assertThrows(UnsupportedOperationException.class, () -> list.add(new CharacterExperience(ExperienceType.OTHER, "X")));
    }

    @Test
    @DisplayName("Should throw on null experience")
    void testAddNullExperience() {
        CharacterLog log = new CharacterLog(makeCharacter());
        assertThrows(NullPointerException.class, () -> log.addExperience(null));
    }

    @Test
    @DisplayName("Log method should auto-fill character age")
    void testLogAutoFillsAge() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        CharacterExperience exp = log.log(ExperienceType.BATTLE, "Test");
        assertEquals(18, exp.getCharacterAge());
    }

    @Test
    @DisplayName("Log with description should chain correctly")
    void testLogWithDescription() {
        CharacterLog log = new CharacterLog(makeCharacter());
        CharacterExperience exp = log.log(ExperienceType.BATTLE, "Fight", "A fierce battle");
        assertEquals("A fierce battle", exp.getDescription());
    }

    @Test
    @DisplayName("Log with narrative should chain correctly")
    void testLogWithNarrative() {
        CharacterLog log = new CharacterLog(makeCharacter());
        CharacterExperience exp = log.log(ExperienceType.BATTLE, "Fight", "Desc", "A long story");
        assertEquals("A long story", exp.getNarrative());
        assertEquals("Desc", exp.getDescription());
    }

    // ---- Character creation ----

    @Test
    @DisplayName("Should log character creation with abilities")
    void testLogCharacterCreation() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Sol-3 (Earth)");

        assertEquals(1, log.getExperienceCount());
        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.CHARACTER_CREATION, exp.getType());
        assertTrue(exp.getTitle().contains("Birth"));
        assertTrue(exp.getNarrative().contains("Sol-3 (Earth)"));
        assertTrue(exp.getNarrative().contains("Alex Starborn"));
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
    }

    // ---- Career start ----

    @Test
    @DisplayName("Should log career start")
    void testLogCareerStart() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logCareerStart(Career.SOLDIER);

        assertEquals(1, log.getExperienceCount());
        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.CAREER_START, exp.getType());
        assertTrue(exp.getTitle().contains("Soldier"));
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
    }

    // ---- Career completion ----

    @Test
    @DisplayName("Should log career completion with skills and wealth")
    void testLogCareerCompletion() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);

        List<Skill> skills = List.of(Skill.MELEE, Skill.FIREARMS);
        TravellerCharacter.CareerTour tour = new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, skills, "Officer", 12000, true,
                TravellerCharacter.RetirementType.NORMAL);

        log.logCareerCompletion(tour);

        assertEquals(1, log.getExperienceCount());
        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.CAREER_COMPLETION, exp.getType());
        assertTrue(exp.getDescription().contains("Melee"));
        assertTrue(exp.getDescription().contains("12000"));
    }

    // ---- Promotion ----

    @Test
    @DisplayName("Should log promotion")
    void testLogPromotion() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logPromotion("Enlisted", "Senior Enlisted", Career.SOLDIER);

        assertEquals(1, log.getExperienceCount());
        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.PROMOTION, exp.getType());
        assertTrue(exp.getTitle().contains("Senior Enlisted"));
        assertTrue(exp.getNarrative().contains("Enlisted"));
    }

    // ---- Retirement ----

    @Test
    @DisplayName("Should log honorable retirement with success outcome")
    void testLogHonorableRetirement() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logRetirement(Career.SOLDIER, "Officer",
                TravellerCharacter.RetirementType.HONORABLE, 50000);

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.RETIREMENT, exp.getType());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
        assertTrue(exp.getNarrative().contains("honorably"));
    }

    @Test
    @DisplayName("Should log dishonorable retirement with failure outcome")
    void testLogDishonorableRetirement() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logRetirement(Career.SOLDIER, "Enlisted",
                TravellerCharacter.RetirementType.DISHONORABLE, 0);

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(CharacterExperience.Outcome.FAILURE, exp.getOutcome());
        assertTrue(exp.getNarrative().contains("dishonorably"));
    }

    @Test
    @DisplayName("Should log medical retirement with mixed outcome")
    void testLogMedicalRetirement() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logRetirement(Career.SOLDIER, "Officer",
                TravellerCharacter.RetirementType.MEDICAL, 20000);

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(CharacterExperience.Outcome.MIXED, exp.getOutcome());
    }

    @Test
    @DisplayName("Should log dead retirement with failure outcome")
    void testLogDeadRetirement() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logRetirement(Career.SOLDIER, "Officer",
                TravellerCharacter.RetirementType.DEAD, 0);

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(CharacterExperience.Outcome.FAILURE, exp.getOutcome());
        assertTrue(exp.getNarrative().contains("died"));
    }

    // ---- Battle ----

    @Test
    @DisplayName("Should log battle with outcome")
    void testLogBattle() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logBattle("Battle of Tarn", "Tarn VI",
                CharacterExperience.Outcome.SUCCESS, "They won the day.");

        assertEquals(1, log.getExperienceCount());
        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.BATTLE, exp.getType());
        assertEquals("Tarn VI", exp.getLocation());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
    }

    // ---- Mission ----

    @Test
    @DisplayName("Should log successful mission")
    void testLogSuccessfulMission() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logMission("Rescue Mission", "Lone Mountain", true, "Everyone survived.");

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.MISSION_SUCCESS, exp.getType());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
    }

    @Test
    @DisplayName("Should log failed mission")
    void testLogFailedMission() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logMission("Rescue Mission", "Lone Mountain", false, "It went wrong.");

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.MISSION_FAILURE, exp.getType());
        assertEquals(CharacterExperience.Outcome.FAILURE, exp.getOutcome());
    }

    // ---- Exploration ----

    @Test
    @DisplayName("Should log exploration")
    void testLogExploration() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logExploration("New World", "Stranger Moon", "A strange and beautiful place.");

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.EXPLORATION, exp.getType());
        assertEquals("Stranger Moon", exp.getLocation());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
    }

    // ---- Querying ----

    @Test
    @DisplayName("Should filter by type")
    void testGetByType() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Battle 1", "First battle");
        log.log(ExperienceType.EXPLORATION, "Explore 1", "First exploration");
        log.log(ExperienceType.BATTLE, "Battle 2", "Second battle");

        assertEquals(2, log.getByType(ExperienceType.BATTLE).size());
        assertEquals(1, log.getByType(ExperienceType.EXPLORATION).size());
        assertEquals(0, log.getByType(ExperienceType.MISSION_SUCCESS).size());
    }

    @Test
    @DisplayName("Should filter by outcome")
    void testGetByOutcome() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Win", "Won").outcome(CharacterExperience.Outcome.SUCCESS);
        log.log(ExperienceType.BATTLE, "Lose", "Lost").outcome(CharacterExperience.Outcome.FAILURE);
        log.log(ExperienceType.BATTLE, "Draw", "Mixed").outcome(CharacterExperience.Outcome.MIXED);

        assertEquals(1, log.getByOutcome(CharacterExperience.Outcome.SUCCESS).size());
        assertEquals(1, log.getByOutcome(CharacterExperience.Outcome.FAILURE).size());
        assertEquals(1, log.getByOutcome(CharacterExperience.Outcome.MIXED).size());
    }

    @Test
    @DisplayName("Should filter by date range")
    void testGetByDateRange() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Early", "Early")
                .date(LocalDate.of(1900, 1, 1));
        log.log(ExperienceType.BATTLE, "Middle", "Middle")
                .date(LocalDate.of(1900, 6, 15));
        log.log(ExperienceType.BATTLE, "Late", "Late")
                .date(LocalDate.of(1901, 1, 1));

        // Range covering middle only
        List<CharacterExperience> middle = log.getByDateRange(
                LocalDate.of(1900, 4, 1), LocalDate.of(1900, 9, 1));
        assertEquals(1, middle.size());
        assertEquals("Middle", middle.get(0).getTitle());

        // Range covering all
        List<CharacterExperience> all = log.getByDateRange(
                LocalDate.of(1899, 1, 1), LocalDate.of(1902, 1, 1));
        assertEquals(3, all.size());

        // No date set — should not match
        log.log(ExperienceType.OTHER, "No Date", "No date");
        List<CharacterExperience> noDate = log.getByDateRange(
                LocalDate.of(1899, 1, 1), LocalDate.of(1902, 1, 1));
        assertEquals(3, noDate.size()); // only the 3 with dates
    }

    // ---- Summary ----

    @Test
    @DisplayName("Summary should include character name and counts")
    void testGetSummary() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logCharacterCreation("Earth");
        log.logBattle("Battle 1", "Tarn", CharacterExperience.Outcome.SUCCESS, "Won");
        log.logMission("Mission 1", "Sol", true, "Success");

        String summary = log.getSummary();
        assertTrue(summary.contains("Alex Starborn"));
        assertTrue(summary.contains("Battles: 1"));
        assertTrue(summary.contains("Missions: 1"));
    }

    @Test
    @DisplayName("Summary should not show zero counts")
    void testGetSummaryNoZeroCounts() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.REFLECTION, "Thinking", "Just thinking");

        String summary = log.getSummary();
        assertFalse(summary.contains("Battles:"));
        assertFalse(summary.contains("Missions:"));
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should include character name and experience count")
    void testToString() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Fight", "A fight");
        String str = log.toString();
        assertTrue(str.contains("Alex Starborn"));
        assertTrue(str.contains("experiences=1"));
    }

    // ---- getCreatedAt ----

    @Test
    @DisplayName("getCreatedAt should return today's date")
    void testGetCreatedAt() {
        CharacterLog log = new CharacterLog(makeCharacter());
        assertNotNull(log.getCreatedAt());
        assertEquals(java.time.LocalDate.now(), log.getCreatedAt());
    }

    // ---- Querying edge cases ----

    @Test
    @DisplayName("getByType should return empty list for no matches")
    void testGetByTypeNoMatches() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Battle 1", "First battle");
        List<CharacterExperience> result = log.getByType(ExperienceType.EXPLORATION);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByOutcome should return empty list for no matches")
    void testGetByOutcomeNoMatches() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Battle", "Won")
                .outcome(CharacterExperience.Outcome.SUCCESS);
        List<CharacterExperience> result = log.getByOutcome(CharacterExperience.Outcome.FAILURE);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByDateRange should exclude experiences without dates")
    void testGetByDateRangeExcludesNoDate() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.OTHER, "No Date", "No date set");
        log.log(ExperienceType.BATTLE, "With Date", "Has date")
                .date(LocalDate.of(1900, 1, 1));

        List<CharacterExperience> result = log.getByDateRange(
                LocalDate.of(1899, 1, 1), LocalDate.of(1901, 1, 1));
        assertEquals(1, result.size());
        assertEquals("With Date", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getByDateRange should include boundary dates")
    void testGetByDateRangeBoundary() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "Start", "At start")
                .date(LocalDate.of(1900, 1, 1));
        log.log(ExperienceType.BATTLE, "End", "At end")
                .date(LocalDate.of(1900, 12, 31));

        List<CharacterExperience> result = log.getByDateRange(
                LocalDate.of(1900, 1, 1), LocalDate.of(1900, 12, 31));
        assertEquals(2, result.size());
    }

    // ---- Summary edge cases ----

    @Test
    @DisplayName("Summary should include career events count")
    void testGetSummaryWithCareerEvents() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logCareerStart(Career.SOLDIER);
        log.logCareerCompletion(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, List.of(Skill.MELEE), "Officer", 10000, true,
                TravellerCharacter.RetirementType.NORMAL));

        String summary = log.getSummary();
        assertTrue(summary.contains("Career events:"));
    }

    // ---- Multiple experiences ----

    @Test
    @DisplayName("Experiences should be in insertion order")
    void testExperiencesOrder() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.log(ExperienceType.BATTLE, "First", "First battle");
        log.log(ExperienceType.EXPLORATION, "Second", "First exploration");
        log.log(ExperienceType.MISSION_SUCCESS, "Third", "First mission");

        List<CharacterExperience> list = log.getExperiences();
        assertEquals("First", list.get(0).getTitle());
        assertEquals("Second", list.get(1).getTitle());
        assertEquals("Third", list.get(2).getTitle());
    }

    @Test
    @DisplayName("Log should handle many experiences")
    void testManyExperiences() {
        CharacterLog log = new CharacterLog(makeCharacter());
        for (int i = 0; i < 100; i++) {
            log.log(ExperienceType.BATTLE, "Battle " + i, "Battle number " + i);
        }
        assertEquals(100, log.getExperienceCount());
        assertEquals(100, log.getByType(ExperienceType.BATTLE).size());
    }

    // ---- Normal retirement ----

    @Test
    @DisplayName("Should log normal retirement with success outcome")
    void testLogNormalRetirement() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logRetirement(Career.SOLDIER, "Officer",
                TravellerCharacter.RetirementType.NORMAL, 30000);

        CharacterExperience exp = log.getExperiences().get(0);
        assertEquals(ExperienceType.RETIREMENT, exp.getType());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
        assertTrue(exp.getNarrative().contains("30000"));
    }

    // ---- toString edge cases ----

    @Test
    @DisplayName("toString should show zero experiences for empty log")
    void testToStringEmptyLog() {
        CharacterLog log = new CharacterLog(makeCharacter());
        String str = log.toString();
        assertTrue(str.contains("experiences=0"));
    }

    // ---- getCharacter getter ----

    @Test
    @DisplayName("getCharacter should return the same character")
    void testGetCharacter() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        assertSame(character, log.getCharacter());
    }

    // ---- Summary with all categories ----

    @Test
    @DisplayName("Summary should show all categories when present")
    void testGetSummaryAllCategories() {
        CharacterLog log = new CharacterLog(makeCharacter());
        log.logCharacterCreation("Earth");
        log.logCareerStart(Career.SOLDIER);
        log.logBattle("Battle 1", "Tarn", CharacterExperience.Outcome.SUCCESS, "Won");
        log.logMission("Mission 1", "Sol", true, "Success");
        log.logExploration("World 1", "Alpha", "Explored");

        String summary = log.getSummary();
        assertTrue(summary.contains("Alex Starborn"));
        assertTrue(summary.contains("Total experiences:"));
        assertTrue(summary.contains("Battles: 1"));
        assertTrue(summary.contains("Missions: 1"));
        assertTrue(summary.contains("Career events: 1"));
    }

    // ---- Query returns empty list, not null ----

    @Test
    @DisplayName("getByType should return empty list, not null")
    void testGetByTypeReturnsEmptyNotNull() {
        CharacterLog log = new CharacterLog(makeCharacter());
        List<CharacterExperience> result = log.getByType(ExperienceType.BATTLE);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByOutcome should return empty list, not null")
    void testGetByOutcomeReturnsEmptyNotNull() {
        CharacterLog log = new CharacterLog(makeCharacter());
        List<CharacterExperience> result = log.getByOutcome(CharacterExperience.Outcome.SUCCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByDateRange should return empty list, not null")
    void testGetByDateRangeReturnsEmptyNotNull() {
        CharacterLog log = new CharacterLog(makeCharacter());
        List<CharacterExperience> result = log.getByDateRange(
                LocalDate.of(1900, 1, 1), LocalDate.of(1900, 12, 31));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---- Log method returns experience for further config ----

    @Test
    @DisplayName("Log method should return experience for further configuration")
    void testLogReturnsExperienceForConfig() {
        CharacterLog log = new CharacterLog(makeCharacter());
        CharacterExperience exp = log.log(ExperienceType.BATTLE, "Test")
                .location("Sol-3")
                .outcome(CharacterExperience.Outcome.SUCCESS);

        assertEquals("Sol-3", exp.getLocation());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
        assertEquals(1, log.getExperienceCount());
    }
}
