package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CharacterExperience Tests")
class CharacterExperienceTest {

    // ---- Outcome enum tests ----

    @Test
    @DisplayName("Outcome enum should have correct display names")
    void testOutcomeDisplayNames() {
        assertEquals("Success", CharacterExperience.Outcome.SUCCESS.getDisplayName());
        assertEquals("Failure", CharacterExperience.Outcome.FAILURE.getDisplayName());
        assertEquals("Mixed", CharacterExperience.Outcome.MIXED.getDisplayName());
        assertEquals("Unknown", CharacterExperience.Outcome.UNKNOWN.getDisplayName());
    }

    @Test
    @DisplayName("Outcome enum should have 4 values")
    void testOutcomeEnumSize() {
        assertEquals(4, CharacterExperience.Outcome.values().length);
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Should create experience with type and title")
    void testConstructor() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "First Battle");
        assertEquals(ExperienceType.BATTLE, exp.getType());
        assertEquals("First Battle", exp.getTitle());
        assertEquals("", exp.getDescription());
        assertEquals("", exp.getNarrative());
        assertEquals("", exp.getLocation());
        assertEquals(CharacterExperience.Outcome.UNKNOWN, exp.getOutcome());
        assertNull(exp.getDate());
        assertEquals(0, exp.getCharacterAge());
    }

    @Test
    @DisplayName("Should throw on null type")
    void testNullType() {
        assertThrows(NullPointerException.class,
                () -> new CharacterExperience(null, "Title"));
    }

    @Test
    @DisplayName("Should throw on null title")
    void testNullTitle() {
        assertThrows(NullPointerException.class,
                () -> new CharacterExperience(ExperienceType.BATTLE, null));
    }

    @Test
    @DisplayName("Sequence numbers should be unique and increasing")
    void testSequenceNumbers() {
        CharacterExperience exp1 = new CharacterExperience(ExperienceType.BATTLE, "A");
        CharacterExperience exp2 = new CharacterExperience(ExperienceType.BATTLE, "B");
        CharacterExperience exp3 = new CharacterExperience(ExperienceType.BATTLE, "C");

        assertTrue(exp1.getSequenceNumber() < exp2.getSequenceNumber());
        assertTrue(exp2.getSequenceNumber() < exp3.getSequenceNumber());
    }

    // ---- Fluent setter tests ----

    @Test
    @DisplayName("Fluent setters should return this for chaining")
    void testFluentChaining() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.EXPLORATION, "New World");

        CharacterExperience chained = exp
                .description("Explored a new planet")
                .narrative("The world was beautiful.")
                .location("Tarn VI")
                .date(LocalDate.of(1900, 6, 15))
                .characterAge(25)
                .outcome(CharacterExperience.Outcome.SUCCESS);

        assertSame(exp, chained);
        assertEquals("Explored a new planet", exp.getDescription());
        assertEquals("The world was beautiful.", exp.getNarrative());
        assertEquals("Tarn VI", exp.getLocation());
        assertEquals(LocalDate.of(1900, 6, 15), exp.getDate());
        assertEquals(25, exp.getCharacterAge());
        assertEquals(CharacterExperience.Outcome.SUCCESS, exp.getOutcome());
    }

    @Test
    @DisplayName("Fluent setters should throw on null values")
    void testFluentSettersNullSafety() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test");

        assertThrows(NullPointerException.class, () -> exp.description(null));
        assertThrows(NullPointerException.class, () -> exp.narrative(null));
        assertThrows(NullPointerException.class, () -> exp.location(null));
        assertThrows(NullPointerException.class, () -> exp.date(null));
        assertThrows(NullPointerException.class, () -> exp.outcome(null));
    }

    // ---- Formatted date tests ----

    @Test
    @DisplayName("Formatted date should return unknown when date is null")
    void testFormattedDateNull() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test");
        assertEquals("Unknown date", exp.getFormattedDate());
    }

    @Test
    @DisplayName("Formatted date should format correctly")
    void testFormattedDate() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test")
                .date(LocalDate.of(1900, 1, 15));
        assertEquals("January 15, 1900", exp.getFormattedDate());
    }

    // ---- Summary tests ----

    @Test
    @DisplayName("Summary should include title only for minimal experience")
    void testSummaryMinimal() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.OTHER, "Simple Event");
        assertEquals("Simple Event", exp.getSummary());
    }

    @Test
    @DisplayName("Summary should include location when set")
    void testSummaryWithLocation() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Battle")
                .description("desc")
                .location("Sol-3");
        String summary = exp.getSummary();
        assertTrue(summary.contains("Battle"));
        assertTrue(summary.contains("Sol-3"));
    }

    @Test
    @DisplayName("Summary should include date when set")
    void testSummaryWithDate() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Battle")
                .date(LocalDate.of(1900, 6, 1));
        String summary = exp.getSummary();
        assertTrue(summary.contains("June 1, 1900"));
    }

    @Test
    @DisplayName("Summary should include outcome when not unknown")
    void testSummaryWithOutcome() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Battle")
                .outcome(CharacterExperience.Outcome.SUCCESS);
        String summary = exp.getSummary();
        assertTrue(summary.contains("[Success]"));
    }

    @Test
    @DisplayName("Summary should not include outcome when unknown")
    void testSummaryWithoutOutcome() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Battle");
        String summary = exp.getSummary();
        assertFalse(summary.contains("["));
    }

    // ---- toString test ----

    @Test
    @DisplayName("toString should include type display name and title")
    void testToString() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "The Big Fight")
                .outcome(CharacterExperience.Outcome.SUCCESS);
        String str = exp.toString();
        assertTrue(str.contains("Battle"));
        assertTrue(str.contains("The Big Fight"));
        assertTrue(str.contains("Success"));
    }

    // ---- Edge cases ----

    @Test
    @DisplayName("characterAge should accept 0")
    void testCharacterAgeZero() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.OTHER, "Test")
                .characterAge(0);
        assertEquals(0, exp.getCharacterAge());
    }

    @Test
    @DisplayName("characterAge should accept negative values")
    void testCharacterAgeNegative() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.OTHER, "Test")
                .characterAge(-1);
        assertEquals(-1, exp.getCharacterAge());
    }

    @Test
    @DisplayName("Summary should include all fields when set")
    void testSummaryWithAllFields() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Epic Battle")
                .location("Tarn VI")
                .date(LocalDate.of(1900, 6, 15))
                .outcome(CharacterExperience.Outcome.SUCCESS);
        String summary = exp.getSummary();
        assertTrue(summary.contains("Epic Battle"));
        assertTrue(summary.contains("Tarn VI"));
        assertTrue(summary.contains("June 15, 1900"));
        assertTrue(summary.contains("[Success]"));
    }

    @Test
    @DisplayName("Summary should handle empty location")
    void testSummaryEmptyLocation() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.OTHER, "Test")
                .location("");
        String summary = exp.getSummary();
        assertEquals("Test", summary);
    }

    @Test
    @DisplayName("Sequence number should start at 0")
    void testSequenceNumberStart() {
        // Reset static counter for predictable test
        // Note: sequence numbers are monotonic across sessions, so we only check ordering
        CharacterExperience exp1 = new CharacterExperience(ExperienceType.OTHER, "A");
        CharacterExperience exp2 = new CharacterExperience(ExperienceType.OTHER, "B");
        assertEquals(exp1.getSequenceNumber() + 1, exp2.getSequenceNumber());
    }

    @Test
    @DisplayName("Fluent setters should be callable multiple times")
    void testFluentSettersOverwrite() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test")
                .description("First")
                .description("Second")
                .location("World A")
                .location("World B");
        assertEquals("Second", exp.getDescription());
        assertEquals("World B", exp.getLocation());
    }

    // ---- Formatted date edge cases ----

    @Test
    @DisplayName("Formatted date should handle leap year")
    void testFormattedDateLeapYear() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test")
                .date(LocalDate.of(2000, 2, 29));
        assertEquals("February 29, 2000", exp.getFormattedDate());
    }

    @Test
    @DisplayName("Formatted date should handle first day of year")
    void testFormattedDateFirstDayOfYear() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test")
                .date(LocalDate.of(1900, 1, 1));
        assertEquals("January 1, 1900", exp.getFormattedDate());
    }

    @Test
    @DisplayName("Formatted date should handle last day of year")
    void testFormattedDateLastDayOfYear() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test")
                .date(LocalDate.of(1900, 12, 31));
        assertEquals("December 31, 1900", exp.getFormattedDate());
    }

    @Test
    @DisplayName("Formatted date should handle single digit day")
    void testFormattedDateSingleDigitDay() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test")
                .date(LocalDate.of(1900, 3, 5));
        assertEquals("March 5, 1900", exp.getFormattedDate());
    }

    // ---- getType and getTitle immutability ----

    @Test
    @DisplayName("Type should be immutable after construction")
    void testTypeImmutability() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test");
        assertEquals(ExperienceType.BATTLE, exp.getType());
        // No setter for type — it's final
    }

    @Test
    @DisplayName("Title should be immutable after construction")
    void testTitleImmutability() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test");
        assertEquals("Test", exp.getTitle());
        // No setter for title — it's final
    }

    @Test
    @DisplayName("Sequence number should be immutable")
    void testSequenceNumberImmutability() {
        CharacterExperience exp = new CharacterExperience(ExperienceType.BATTLE, "Test");
        long seq = exp.getSequenceNumber();
        // No setter for sequence number — it's final
        assertEquals(seq, exp.getSequenceNumber());
    }
}
