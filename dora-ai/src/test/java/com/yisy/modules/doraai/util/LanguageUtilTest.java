package com.yisy.modules.doraai.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LanguageUtil Tests")
class LanguageUtilTest {

    @Test
    @DisplayName("SUPPORTED_LANGUAGES contains 20 languages")
    void supportedLanguages_count() {
        assertEquals(20, LanguageUtil.SUPPORTED_LANGUAGES.size());
    }

    @Test
    @DisplayName("SUPPORTED_LANGUAGES is immutable")
    void supportedLanguages_immutable() {
        assertThrows(UnsupportedOperationException.class, () ->
                LanguageUtil.SUPPORTED_LANGUAGES.add("xx")
        );
    }

    @Test
    @DisplayName("SUPPORTED_LANGUAGES contains English")
    void supportedLanguages_containsEnglish() {
        assertTrue(LanguageUtil.SUPPORTED_LANGUAGES.contains("en"));
    }

    @Test
    @DisplayName("SUPPORTED_LANGUAGES contains Chinese")
    void supportedLanguages_containsChinese() {
        assertTrue(LanguageUtil.SUPPORTED_LANGUAGES.contains("zh"));
    }

    @Test
    @DisplayName("toLanguageName converts all 20 supported languages")
    void toLanguageName_allSupportedLanguages() {
        assertEquals("English", LanguageUtil.toLanguageName("en"));
        assertEquals("Chinese", LanguageUtil.toLanguageName("zh"));
        assertEquals("Japanese", LanguageUtil.toLanguageName("ja"));
        assertEquals("Korean", LanguageUtil.toLanguageName("ko"));
        assertEquals("Russian", LanguageUtil.toLanguageName("ru"));
        assertEquals("Arabic", LanguageUtil.toLanguageName("ar"));
        assertEquals("French", LanguageUtil.toLanguageName("fr"));
        assertEquals("German", LanguageUtil.toLanguageName("de"));
        assertEquals("Spanish", LanguageUtil.toLanguageName("es"));
        assertEquals("Portuguese", LanguageUtil.toLanguageName("pt"));
        assertEquals("Italian", LanguageUtil.toLanguageName("it"));
        assertEquals("Dutch", LanguageUtil.toLanguageName("nl"));
        assertEquals("Polish", LanguageUtil.toLanguageName("pl"));
        assertEquals("Turkish", LanguageUtil.toLanguageName("tr"));
        assertEquals("Vietnamese", LanguageUtil.toLanguageName("vi"));
        assertEquals("Thai", LanguageUtil.toLanguageName("th"));
        assertEquals("Indonesian", LanguageUtil.toLanguageName("id"));
        assertEquals("Malay", LanguageUtil.toLanguageName("ms"));
        assertEquals("Hindi", LanguageUtil.toLanguageName("hi"));
        assertEquals("Bengali", LanguageUtil.toLanguageName("bn"));
    }

    @Test
    @DisplayName("toLanguageName is case insensitive")
    void toLanguageName_caseInsensitive() {
        assertEquals("English", LanguageUtil.toLanguageName("EN"));
        assertEquals("English", LanguageUtil.toLanguageName("En"));
        assertEquals("Chinese", LanguageUtil.toLanguageName("ZH"));
        assertEquals("Japanese", LanguageUtil.toLanguageName("Ja"));
    }

    @Test
    @DisplayName("toLanguageName returns original code for unknown language")
    void toLanguageName_unknownCode() {
        assertEquals("xx", LanguageUtil.toLanguageName("xx"));
        assertEquals("foo", LanguageUtil.toLanguageName("foo"));
    }

    @Test
    @DisplayName("toLanguageName handles null")
    void toLanguageName_null() {
        assertNull(LanguageUtil.toLanguageName(null));
    }

    @Test
    @DisplayName("toLanguageName handles blank string")
    void toLanguageName_blank() {
        assertEquals("", LanguageUtil.toLanguageName(""));
        assertEquals("  ", LanguageUtil.toLanguageName("  "));
    }

    @Test
    @DisplayName("LanguageUtil is a utility class with private constructor")
    void utilityClass_privateConstructor() {
        // Verify the class is final and has a private constructor
        assertTrue(java.lang.reflect.Modifier.isFinal(LanguageUtil.class.getModifiers()));
        java.lang.reflect.Constructor[] constructors = LanguageUtil.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    @DisplayName("toLanguageName handles all supported language codes from list")
    void toLanguageName_allFromList() {
        for (String code : LanguageUtil.SUPPORTED_LANGUAGES) {
            String name = LanguageUtil.toLanguageName(code);
            assertNotNull(name);
            assertNotEquals(code, name, "Language name should differ from code for: " + code);
            assertTrue(name.length() > 2, "Language name should be meaningful for: " + code);
        }
    }
}
