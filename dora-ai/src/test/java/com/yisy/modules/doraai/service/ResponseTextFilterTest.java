package com.yisy.modules.doraai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTextFilterTest {

    @Test
    void normalize_preserves_code_blocks() {
        String input = "Here is code: ```myVariable``` and text helloWorld.";
        String result = ResponseTextFilter.normalize(input);
        // Code block should be preserved
        assertTrue(result.contains("```myVariable```"));
        // Natural language should still be split
        assertTrue(result.contains("hello World"));
    }

    @Test
    void normalize_preserves_multiline_code_blocks() {
        String input = "Use this: ```\nmyVariable = 1\nJSONResponse\n``` done.";
        String result = ResponseTextFilter.normalize(input);
        // Code block content should be preserved
        assertTrue(result.contains("myVariable"));
        assertTrue(result.contains("JSONResponse"));
    }

    @Test
    void normalize_splits_camelCase_outside_code() {
        String input = "helloWorld fooBar";
        String result = ResponseTextFilter.normalize(input);
        assertEquals("hello World foo Bar", result);
    }

    @Test
    void normalize_adds_space_after_punctuation() {
        String input = "Hello,world.";
        String result = ResponseTextFilter.normalize(input);
        assertEquals("Hello, world.", result);
    }

    @Test
    void normalize_handles_null() {
        assertNull(ResponseTextFilter.normalize(null));
    }

    @Test
    void normalize_handles_empty() {
        assertEquals("", ResponseTextFilter.normalize(""));
    }

    @Test
    void normalize_preserves_single_backtick_code() {
        String input = "Use `myVar` here helloWorld.";
        String result = ResponseTextFilter.normalize(input);
        // Single backticks are not code blocks for our splitter (only ```)
        // So camelCase splitting still applies to the whole text
        assertTrue(result.contains("hello World"));
    }
}
