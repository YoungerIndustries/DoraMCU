package com.yisy.modules.doraai.dto;

import com.yisy.modules.doraai.service.TranslationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    // ——— AnalysisRequest ———

    @Test
    void analysisRequest_defaultConstructor() {
        AnalysisRequest request = new AnalysisRequest();

        assertNull(request.getProjectId());
        assertNull(request.getFilePaths());
        assertNull(request.getDescription());
        assertNull(request.getCode());
    }

    @Test
    void analysisRequest_constructorWithParams() {
        AnalysisRequest request = new AnalysisRequest("proj-1", List.of("src/Main.java"), "Check for bugs");

        assertEquals("proj-1", request.getProjectId());
        assertEquals(List.of("src/Main.java"), request.getFilePaths());
        assertEquals("Check for bugs", request.getDescription());
    }

    @Test
    void analysisRequest_setters() {
        AnalysisRequest request = new AnalysisRequest();
        request.setProjectId("test-project");
        request.setFilePaths(List.of("src/A.java", "src/B.java"));
        request.setDescription("Review code quality");
        request.setCode("public class Test {}");

        assertEquals("test-project", request.getProjectId());
        assertEquals(2, request.getFilePaths().size());
        assertEquals("Review code quality", request.getDescription());
        assertEquals("public class Test {}", request.getCode());
    }

    // ——— GenerationRequest ———

    @Test
    void generationRequest_defaultConstructor() {
        GenerationRequest request = new GenerationRequest();

        assertNull(request.getProjectId());
        assertNull(request.getDescription());
        assertNull(request.getLanguage());
        assertNull(request.getContext());
    }

    @Test
    void generationRequest_constructorWithParams() {
        GenerationRequest request = new GenerationRequest("proj-1", "Create a REST controller");

        assertEquals("proj-1", request.getProjectId());
        assertEquals("Create a REST controller", request.getDescription());
    }

    @Test
    void generationRequest_setters() {
        GenerationRequest request = new GenerationRequest();
        request.setProjectId("my-project");
        request.setDescription("Generate a service class");
        request.setLanguage("java");
        request.setContext("Use Spring Boot patterns");

        assertEquals("my-project", request.getProjectId());
        assertEquals("Generate a service class", request.getDescription());
        assertEquals("java", request.getLanguage());
        assertEquals("Use Spring Boot patterns", request.getContext());
    }

    // ——— ProjectRequest ———

    @Test
    void projectRequest_defaultConstructor() {
        ProjectRequest request = new ProjectRequest();

        assertNull(request.getName());
        assertNull(request.getPath());
    }

    @Test
    void projectRequest_constructorWithParams() {
        ProjectRequest request = new ProjectRequest("My Project", "/path/to/project");

        assertEquals("My Project", request.getName());
        assertEquals("/path/to/project", request.getPath());
    }

    @Test
    void projectRequest_setters() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Test Project");
        request.setPath("/home/user/test");

        assertEquals("Test Project", request.getName());
        assertEquals("/home/user/test", request.getPath());
    }

    // ——— TranslationService ———

    @Test
    void translationService_supportedLanguages() {
        // We can test the constant directly without Spring context
        List<String> languages = TranslationService.SUPPORTED_LANGUAGES;

        assertEquals(20, languages.size());
        assertTrue(languages.contains("en"));
        assertTrue(languages.contains("zh"));
        assertTrue(languages.contains("ja"));
        assertTrue(languages.contains("ko"));
        assertTrue(languages.contains("ru"));
        assertTrue(languages.contains("ar"));
        assertTrue(languages.contains("fr"));
        assertTrue(languages.contains("de"));
        assertTrue(languages.contains("es"));
        assertTrue(languages.contains("pt"));
    }

    @Test
    void translationService_supportedLanguagesImmutable() {
        assertThrows(UnsupportedOperationException.class, () ->
                TranslationService.SUPPORTED_LANGUAGES.add("xx")
        );
    }

    // ——— TranslateRequest ———

    @Test
    void translateRequest_defaultConstructor() {
        TranslateRequest request = new TranslateRequest();

        assertNull(request.getText());
        assertNull(request.getSourceLanguage());
        assertNull(request.getTargetLanguage());
    }

    @Test
    void translateRequest_twoArgConstructor() {
        TranslateRequest request = new TranslateRequest("Hello", "zh");

        assertEquals("Hello", request.getText());
        assertNull(request.getSourceLanguage());
        assertEquals("zh", request.getTargetLanguage());
    }

    @Test
    void translateRequest_threeArgConstructor() {
        TranslateRequest request = new TranslateRequest("Hello", "en", "zh");

        assertEquals("Hello", request.getText());
        assertEquals("en", request.getSourceLanguage());
        assertEquals("zh", request.getTargetLanguage());
    }

    @Test
    void translateRequest_setters() {
        TranslateRequest request = new TranslateRequest();
        request.setText("Goodbye");
        request.setSourceLanguage("fr");
        request.setTargetLanguage("de");

        assertEquals("Goodbye", request.getText());
        assertEquals("fr", request.getSourceLanguage());
        assertEquals("de", request.getTargetLanguage());
    }

    // ——— TranslateResponse ———

    @Test
    void translateResponse_defaultConstructor() {
        TranslateResponse response = new TranslateResponse();

        assertNull(response.getTranslatedText());
        assertNull(response.getSourceLanguage());
        assertNull(response.getTargetLanguage());
    }

    @Test
    void translateResponse_constructorWithParams() {
        TranslateResponse response = new TranslateResponse("你好", "en", "zh");

        assertEquals("你好", response.getTranslatedText());
        assertEquals("en", response.getSourceLanguage());
        assertEquals("zh", response.getTargetLanguage());
    }

    @Test
    void translateResponse_setters() {
        TranslateResponse response = new TranslateResponse();
        response.setTranslatedText("Bonjour");
        response.setSourceLanguage("en");
        response.setTargetLanguage("fr");

        assertEquals("Bonjour", response.getTranslatedText());
        assertEquals("en", response.getSourceLanguage());
        assertEquals("fr", response.getTargetLanguage());
    }
}
