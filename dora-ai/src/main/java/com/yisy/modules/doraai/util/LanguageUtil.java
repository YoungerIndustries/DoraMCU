package com.yisy.modules.doraai.util;

import java.util.List;

/**
 * Utility for language code to name conversion, shared across AI services.
 * <p>
 * Provides the list of supported languages and converts ISO 639-1 language
 * codes to human-readable language names.
 * </p>
 */
public final class LanguageUtil {

    private LanguageUtil() {
        // utility class
    }

    /** Supported languages for translation and language-aware chat. */
    public static final List<String> SUPPORTED_LANGUAGES = List.of(
            "en", "zh", "ja", "ko", "ru", "ar", "fr", "de", "es", "pt",
            "it", "nl", "pl", "tr", "vi", "th", "id", "ms", "hi", "bn"
    );

    /**
     * Converts a language code to a human-readable language name.
     *
     * @param code the ISO 639-1 language code (e.g., "en", "zh", "ja")
     * @return the human-readable name (e.g., "English", "Chinese", "Japanese"),
     *         or the original code if not recognized
     */
    public static String toLanguageName(String code) {
        if (code == null || code.isBlank()) {
            return code;
        }
        return switch (code.toLowerCase()) {
            case "en" -> "English";
            case "zh" -> "Chinese";
            case "ja" -> "Japanese";
            case "ko" -> "Korean";
            case "ru" -> "Russian";
            case "ar" -> "Arabic";
            case "fr" -> "French";
            case "de" -> "German";
            case "es" -> "Spanish";
            case "pt" -> "Portuguese";
            case "it" -> "Italian";
            case "nl" -> "Dutch";
            case "pl" -> "Polish";
            case "tr" -> "Turkish";
            case "vi" -> "Vietnamese";
            case "th" -> "Thai";
            case "id" -> "Indonesian";
            case "ms" -> "Malay";
            case "hi" -> "Hindi";
            case "bn" -> "Bengali";
            default -> code;
        };
    }
}
