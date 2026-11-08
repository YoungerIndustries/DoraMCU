package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookGenerator Tests")
class BookGeneratorTest {

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

    private CharacterLog makeLogWithCreation() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Sol-3 (Earth)");
        return log;
    }

    private CharacterLog makeFullLog() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Sol-3 (Earth)");
        log.logCareerStart(Career.SOLDIER);
        log.logPromotion("Enlisted", "Officer", Career.SOLDIER);
        log.logBattle("Battle of Tarn", "Tarn VI",
                CharacterExperience.Outcome.SUCCESS, "They won the day.");
        log.logMission("Rescue Mission", "Lone Mountain", true, "Everyone survived.");
        log.logExploration("New World", "Stranger Moon", "A strange and beautiful place.");
        return log;
    }

    // ---- Null safety ----

    @Test
    @DisplayName("Should throw on null log for generateBook")
    void testGenerateBookNullLog() {
        BookGenerator generator = new BookGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateBook(null));
    }

    @Test
    @DisplayName("Should throw on null log for generateSummaryBook")
    void testGenerateSummaryBookNullLog() {
        BookGenerator generator = new BookGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateSummaryBook(null));
    }

    // ---- Empty log ----

    @Test
    @DisplayName("Should return message for empty log in generateBook")
    void testGenerateBookEmptyLog() {
        CharacterLog log = new CharacterLog(makeCharacter());
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("has not yet been written"));
    }

    @Test
    @DisplayName("Should return message for empty log in generateSummaryBook")
    void testGenerateSummaryBookEmptyLog() {
        CharacterLog log = new CharacterLog(makeCharacter());
        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("No experiences recorded"));
    }

    // ---- Full book structure ----

    @Test
    @DisplayName("Should include title page with character name")
    void testTitlePage() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeLogWithCreation());
        assertTrue(book.contains("THE CHRONICLES OF"));
        assertTrue(book.contains("Alex Starborn"));
        assertTrue(book.contains("A Traveller's Tale"));
    }

    @Test
    @DisplayName("Should include character abilities on title page")
    void testTitlePageAbilities() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeLogWithCreation());
        assertTrue(book.contains("--- ABILITIES ---"));
        assertTrue(book.contains("Dexterity"));
        assertTrue(book.contains("Intelligence"));
    }

    @Test
    @DisplayName("Should include table of contents")
    void testTableOfContents() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeFullLog());
        assertTrue(book.contains("TABLE OF CONTENTS"));
        assertTrue(book.contains("Prologue"));
    }

    @Test
    @DisplayName("Should include prologue chapter")
    void testPrologueChapter() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeLogWithCreation());
        assertTrue(book.contains("PROLOGUE"));
        assertTrue(book.contains("The beginning of a life"));
    }

    @Test
    @DisplayName("Should include career chapters")
    void testCareerChapters() {
        CharacterLog log = makeFullLog();
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("Soldier SERVICE"));
    }

    @Test
    @DisplayName("Should include adventure chapter when adventures exist")
    void testAdventureChapter() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeFullLog());
        assertTrue(book.contains("ADVENTURES AMONG THE STARS"));
    }

    @Test
    @DisplayName("Should not include adventure chapter when no adventures")
    void testNoAdventureChapter() {
        CharacterLog log = makeLogWithCreation();
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertFalse(book.contains("ADVENTURES AMONG THE STARS"));
    }

    // ---- Epilogue ----

    @Test
    @DisplayName("Should include alive epilogue for living character")
    void testAliveEpilogue() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(true);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("EPILOGUE"));
        assertTrue(book.contains("The Road Ahead"));
        assertTrue(book.contains("Alive and Adventuring"));
        assertFalse(book.contains("Deceased"));
    }

    @Test
    @DisplayName("Should include dead epilogue for deceased character")
    void testDeadEpilogue() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(false);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("EPILOGUE"));
        assertTrue(book.contains("A Life Remembered"));
        assertTrue(book.contains("Deceased"));
        assertTrue(book.contains("Rest in peace"));
    }

    @Test
    @DisplayName("Should end with END OF CHRONICLE")
    void testEndOfChronicle() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeLogWithCreation());
        assertTrue(book.contains("END OF CHRONICLE"));
    }

    // ---- Summary book ----

    @Test
    @DisplayName("Summary book should include character stats")
    void testSummaryBookStats() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("A Brief Chronicle"));
        assertTrue(book.contains("AGE:"));
        assertTrue(book.contains("STATUS:"));
        assertTrue(book.contains("WEALTH:"));
    }

    @Test
    @DisplayName("Summary book should include abilities section")
    void testSummaryBookAbilities() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(makeLogWithCreation());
        assertTrue(book.contains("--- ABILITIES ---"));
        assertTrue(book.contains("Dex:"));
        assertTrue(book.contains("Int:"));
    }

    @Test
    @DisplayName("Summary book should include skills section")
    void testSummaryBookSkills() {
        TravellerCharacter character = makeCharacter();
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, java.util.List.of(Skill.MELEE), "Officer", 10000, true,
                TravellerCharacter.RetirementType.NORMAL));
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("--- SKILLS"));
        assertTrue(book.contains("Melee"));
    }

    @Test
    @DisplayName("Summary book should include key events")
    void testSummaryBookEvents() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(makeFullLog());
        assertTrue(book.contains("--- KEY EVENTS ---"));
    }

    @Test
    @DisplayName("Summary book should end with End of Chronicle")
    void testSummaryBookEnding() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(makeLogWithCreation());
        assertTrue(book.contains("End of Chronicle"));
    }

    // ---- Experience rendering ----

    @Test
    @DisplayName("Should render experience title and narrative")
    void testExperienceRendering() {
        CharacterLog log = makeFullLog();
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("--- Battle of Tarn ---"));
        assertTrue(book.contains("They won the day."));
    }

    @Test
    @DisplayName("Should render outcome in book")
    void testOutcomeRendering() {
        CharacterLog log = makeFullLog();
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("[Outcome: Success]"));
    }

    // ---- Life description in epilogue ----

    @Test
    @DisplayName("Life description should reflect many battles")
    void testLifeDescriptionManyBattles() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(false);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        for (int i = 0; i < 4; i++) {
            log.logBattle("Battle " + i, "World " + i,
                    CharacterExperience.Outcome.SUCCESS, "Won");
        }

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("war and sacrifice"));
    }

    @Test
    @DisplayName("Life description should reflect many explorations")
    void testLifeDescriptionManyExplorations() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(false);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        for (int i = 0; i < 4; i++) {
            log.logExploration("World " + i, "System " + i, "Explored");
        }

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("discovery and wonder"));
    }

    @Test
    @DisplayName("Life description should reflect heroism")
    void testLifeDescriptionHeroism() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(false);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.log(ExperienceType.HEROISM, "Heroic Act", "Saved everyone", "A true hero");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("courage and honor"));
    }

    // ---- Summary book edge cases ----

    @Test
    @DisplayName("Summary book should show deceased status for dead character")
    void testSummaryBookDeadCharacter() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(false);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("STATUS: Deceased"));
    }

    @Test
    @DisplayName("Summary book should not show primary career when none set")
    void testSummaryBookNoPrimaryCareer() {
        TravellerCharacter character = makeCharacter();
        // No career tours added, so primary career is null
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertFalse(book.contains("PRIMARY CAREER:"));
    }

    @Test
    @DisplayName("Summary book should show primary career when set")
    void testSummaryBookWithPrimaryCareer() {
        TravellerCharacter character = makeCharacter();
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, java.util.List.of(Skill.MELEE), "Officer", 10000, true,
                TravellerCharacter.RetirementType.NORMAL));
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("PRIMARY CAREER: Soldier"));
    }

    @Test
    @DisplayName("Full book should not include skills section when no skills")
    void testBookNoSkillsSection() {
        TravellerCharacter character = makeCharacter();
        // No skills added
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertFalse(book.contains("--- SKILLS ---"));
    }

    @Test
    @DisplayName("Full book should include skills section when skills exist")
    void testBookWithSkillsSection() {
        TravellerCharacter character = makeCharacter();
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, java.util.List.of(Skill.MELEE, Skill.FIREARMS), "Officer", 10000, true,
                TravellerCharacter.RetirementType.NORMAL));
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("--- SKILLS ---"));
        assertTrue(book.contains("Melee"));
    }

    @Test
    @DisplayName("Book should handle log with only adventure experiences")
    void testBookAdventureOnly() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logBattle("Battle of Tarn", "Tarn VI",
                CharacterExperience.Outcome.SUCCESS, "They won.");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        // No prologue since no character creation
        assertFalse(book.contains("PROLOGUE"));
        // Should have adventure chapter
        assertTrue(book.contains("ADVENTURES AMONG THE STARS"));
    }

    @Test
    @DisplayName("Book should handle log with only career experiences")
    void testBookCareerOnly() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCareerStart(Career.SOLDIER);
        log.logCareerCompletion(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, java.util.List.of(Skill.MELEE), "Officer", 10000, true,
                TravellerCharacter.RetirementType.NORMAL));

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        // No adventure chapter
        assertFalse(book.contains("ADVENTURES AMONG THE STARS"));
        // Should have career chapter
        assertTrue(book.contains("Soldier SERVICE"));
    }

    @Test
    @DisplayName("Summary book should handle empty skills")
    void testSummaryBookEmptySkills() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("--- SKILLS (0) ---"));
    }

    // ---- Book with only character creation ----

    @Test
    @DisplayName("Book with only creation should have prologue and epilogue")
    void testBookCreationOnly() {
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(makeLogWithCreation());
        assertTrue(book.contains("PROLOGUE"));
        assertTrue(book.contains("EPILOGUE"));
        assertFalse(book.contains("Soldier SERVICE"));
        assertFalse(book.contains("ADVENTURES AMONG THE STARS"));
    }

    // ---- Book with multiple battles ----

    @Test
    @DisplayName("Book should render multiple battles")
    void testBookMultipleBattles() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.logBattle("Battle 1", "World A", CharacterExperience.Outcome.SUCCESS, "Won");
        log.logBattle("Battle 2", "World B", CharacterExperience.Outcome.FAILURE, "Lost");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("--- Battle 1 ---"));
        assertTrue(book.contains("--- Battle 2 ---"));
    }

    // ---- Book with mixed outcomes ----

    @Test
    @DisplayName("Book should render mixed outcome")
    void testBookMixedOutcome() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.log(ExperienceType.BATTLE, "Pyrrhic Victory", "Won at great cost", "A costly win")
                .outcome(CharacterExperience.Outcome.MIXED);

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("[Outcome: Mixed]"));
    }

    // ---- Summary book with wealth ----

    @Test
    @DisplayName("Summary book should show wealth")
    void testSummaryBookWealth() {
        TravellerCharacter character = makeCharacter();
        character.addCareerTour(new TravellerCharacter.CareerTour(
                Career.SOLDIER, 4, java.util.List.of(Skill.MELEE), "Officer", 50000, true,
                TravellerCharacter.RetirementType.NORMAL));
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("WEALTH: 50000 credits"));
    }

    // ---- Book with promotion ----

    @Test
    @DisplayName("Book should include promotion in career chapter")
    void testBookPromotion() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.logCareerStart(Career.SOLDIER);
        log.logPromotion("Enlisted", "Officer", Career.SOLDIER);

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("Promoted to Officer"));
    }

    // ---- Book with retirement ----

    @Test
    @DisplayName("Book should include retirement in career chapter")
    void testBookRetirement() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.logCareerStart(Career.SOLDIER);
        log.logRetirement(Career.SOLDIER, "Officer",
                TravellerCharacter.RetirementType.HONORABLE, 50000);

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("Retirement"));
    }

    // ---- Book with exploration ----

    @Test
    @DisplayName("Book should include exploration in adventure chapter")
    void testBookExploration() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.logExploration("New World", "Stranger Moon", "A strange and beautiful place.");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("ADVENTURES AMONG THE STARS"));
        assertTrue(book.contains("--- New World ---"));
    }

    // ---- Book with mission failure ----

    @Test
    @DisplayName("Book should include mission failure")
    void testBookMissionFailure() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.logMission("Failed Rescue", "Danger Zone", false, "It went wrong.");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("Failed Rescue"));
        assertTrue(book.contains("[Outcome: Failure]"));
    }

    // ---- Summary book with multiple events ----

    @Test
    @DisplayName("Summary book should list multiple key events")
    void testSummaryBookMultipleEvents() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.logBattle("Battle 1", "World A", CharacterExperience.Outcome.SUCCESS, "Won");
        log.logExploration("New World", "Stranger Moon", "Explored");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateSummaryBook(log);
        assertTrue(book.contains("--- KEY EVENTS ---"));
        assertTrue(book.contains("Battle 1"));
        assertTrue(book.contains("New World"));
    }

    // ---- Book with death experience ----

    @Test
    @DisplayName("Book should include death in epilogue for dead character")
    void testBookDeathExperience() {
        TravellerCharacter character = makeCharacter();
        character.setAlive(false);
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        // DEATH is not an adventure type, so it won't appear in a chapter,
        // but the epilogue should reflect the character is dead
        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("A Life Remembered"));
        assertTrue(book.contains("Rest in peace"));
    }

    // ---- Book with heroism ----

    @Test
    @DisplayName("Book should include heroism experience")
    void testBookHeroism() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        log.log(ExperienceType.HEROISM, "Saved the Crew",
                "Rushed into danger", "A true hero emerged.");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("Saved the Crew"));
    }

    // ---- Book with skill gain in career context ----

    @Test
    @DisplayName("Book should include skill gain when career name is in title")
    void testBookSkillGain() {
        TravellerCharacter character = makeCharacter();
        CharacterLog log = new CharacterLog(character);
        log.logCharacterCreation("Earth");
        // SKILL_GAIN is a career type; determineCareer matches career names in title
        log.log(ExperienceType.SKILL_GAIN, "Learned Melee in Soldier Service",
                "Gained melee combat skill", "Mastered the art of close combat.");

        BookGenerator generator = new BookGenerator();
        String book = generator.generateBook(log);
        assertTrue(book.contains("Learned Melee in Soldier Service"));
    }
}
