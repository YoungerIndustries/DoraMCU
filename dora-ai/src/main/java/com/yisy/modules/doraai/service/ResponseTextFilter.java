package com.yisy.modules.doraai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters and normalizes AI response text to ensure proper spacing and newlines.
 * <p>
 * Ollama may return tokens without proper spacing between words or missing newlines.
 * This filter applies common text normalization rules to improve readability.
 * </p>
 */
public class ResponseTextFilter {

    private static final Logger log = LoggerFactory.getLogger(ResponseTextFilter.class);

    /**
     * Normalizes a text chunk by adding missing spaces and newlines.
     *
     * @param text the raw text chunk from Ollama
     * @return normalized text with proper spacing and newlines
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // Add space after punctuation if missing (before newline processing)
        result = addSpaceAfterPunctuation(result);

        // Add space between word boundaries where missing (e.g., "word1word2" patterns)
        result = addSpaceBetweenWords(result);

        // Normalize multiple spaces to single spaces, but preserve intentional spacing
        result = normalizeSpaces(result);

        return result;
    }

    /**
     * Adds a space after punctuation marks if the next character is a letter without a space.
     * Handles: . , ; : ! ? ) ] }
     */
    private static String addSpaceAfterPunctuation(String text) {
        // Add space after period, comma, semicolon, colon, exclamation, question mark if followed by a letter
        text = text.replaceAll("([.,;:!?)\\]}])([A-Za-z])", "$1 $2");
        // Add space after period followed by uppercase (end of sentence)
        text = text.replaceAll("([.!?])([A-Z])", "$1 $2");
        return text;
    }

    /**
     * Adds space between lowercase-uppercase transitions (camelCase to camel Case).
     * Only applies when there's no existing space, to avoid double-spacing.
     */
    private static String addSpaceBetweenWords(String text) {
        // Add space between lowercase and uppercase letters (e.g., "helloWorld" -> "hello World")
        // Only if not already separated by a space
        text = text.replaceAll("([a-z])([A-Z])", "$1 $2");
        return text;
    }

    /**
     * Normalizes multiple consecutive spaces to a single space,
     * but preserves leading spaces and intentional double spaces after periods.
     */
    private static String normalizeSpaces(String text) {
        // Replace 3+ spaces with 2 spaces (preserves double space after period)
        text = text.replaceAll(" {3,}", "  ");
        return text;
    }

}
