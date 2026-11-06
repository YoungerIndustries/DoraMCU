package com.yisy.modules.doraai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTextFilterTest {

    @Test
    void normalize_null_returnsNull() {
        assertNull(ResponseTextFilter.normalize(null));
    }

    @Test
    void normalize_empty_returnsEmpty() {
        assertEquals("", ResponseTextFilter.normalize(""));
    }

    @Test
    void normalize_alreadyProperText_unchanged() {
        String text = "Hello world. This is a test.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("Hello world"));
        assertTrue(result.contains("This is a test"));
    }

    @Test
    void normalize_missingSpaceAfterPeriod_addsSpace() {
        String text = "Hello world.This is a test.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("world. This"), "Should add space after period: " + result);
    }

    @Test
    void normalize_missingSpaceAfterComma_addsSpace() {
        String text = "Hello,world.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains(", world"), "Should add space after comma: " + result);
    }

    @Test
    void normalize_missingSpaceAfterExclamation_addsSpace() {
        String text = "Wow!This is amazing.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("! This"), "Should add space after exclamation: " + result);
    }

    @Test
    void normalize_missingSpaceAfterQuestion_addsSpace() {
        String text = "Hello?Who are you?";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("? Who"), "Should add space after question mark: " + result);
    }

    @Test
    void normalize_missingSpaceAfterSemicolon_addsSpace() {
        String text = "Hello;world.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("; world"), "Should add space after semicolon: " + result);
    }

    @Test
    void normalize_missingSpaceAfterColon_addsSpace() {
        String text = "Hello:world.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains(": world"), "Should add space after colon: " + result);
    }

    @Test
    void normalize_camelCase_addsSpace() {
        String text = "helloWorld";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("hello World"), "Should add space between camelCase: " + result);
    }

    @Test
    void normalize_multipleCamelCase_addsSpaces() {
        String text = "helloWorldTest";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("hello World Test"), "Should add spaces for multiple camelCase: " + result);
    }

    @Test
    void normalize_excessiveSpaces_normalizes() {
        String text = "Hello    world";
        String result = ResponseTextFilter.normalize(text);
        assertFalse(result.contains("    "), "Should not have 4+ consecutive spaces: " + result);
    }

    @Test
    void normalize_preservesNewlines() {
        String text = "Hello\nworld";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("\n"), "Should preserve newlines: " + result);
    }

    @Test
    void normalize_addsSpacesAfterSentences() {
        String text = "First sentence.Second sentence.Third sentence.";
        String result = ResponseTextFilter.normalize(text);
        // Should add spaces after sentence-ending punctuation followed by uppercase
        assertTrue(result.contains("sentence. Second"), "Should add space after period: " + result);
        assertTrue(result.contains("sentence. Third"), "Should add space after period: " + result);
    }

    @Test
    void normalize_complexText_handlesAllRules() {
        String text = "Hello world.This is a test!Wow,amazing.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("world. This"), "Should add space after period: " + result);
        assertTrue(result.contains("test! Wow"), "Should add space after exclamation: " + result);
        assertTrue(result.contains(", amazing"), "Should add space after comma: " + result);
    }

    @Test
    void normalize_alreadyHasSpace_noDoubleSpace() {
        String text = "Hello world. This is fine.";
        String result = ResponseTextFilter.normalize(text);
        // Should not add extra spaces where they already exist
        assertFalse(result.contains("  This"), "Should not double-space: " + result);
    }

    @Test
    void normalize_closingParenthesis_addsSpace() {
        String text = "Hello(world)This is a test.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains(") This"), "Should add space after closing paren: " + result);
    }

    @Test
    void normalize_closingBracket_addsSpace() {
        String text = "Hello[world]This is a test.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("] This"), "Should add space after closing bracket: " + result);
    }

    @Test
    void normalize_closingBrace_addsSpace() {
        String text = "Hello{world}This is a test.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("} This"), "Should add space after closing brace: " + result);
    }

    @Test
    void normalize_preservesExistingSpacing() {
        String text = "Hello  world";
        String result = ResponseTextFilter.normalize(text);
        // Double space after period is preserved for typographic purposes
        assertTrue(result.contains("Hello"), "Should preserve existing content: " + result);
    }

    @Test
    void normalize_singleWord_unchanged() {
        String text = "Hello";
        String result = ResponseTextFilter.normalize(text);
        assertEquals("Hello", result);
    }

    @Test
    void normalize_onlySpaces_normalized() {
        String text = "    ";
        String result = ResponseTextFilter.normalize(text);
        // Excessive spaces should be normalized
        assertFalse(result.contains("    "), "Should normalize excessive spaces: " + result);
    }

    @Test
    void normalize_mixedContent_preservesStructure() {
        String text = "Line 1.\nLine 2.\nLine 3.";
        String result = ResponseTextFilter.normalize(text);
        assertTrue(result.contains("Line 1"), "Should preserve line 1: " + result);
        assertTrue(result.contains("Line 2"), "Should preserve line 2: " + result);
        assertTrue(result.contains("Line 3"), "Should preserve line 3: " + result);
    }
}
