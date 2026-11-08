package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.config.OllamaConfig;
import com.yisy.modules.doraai.dto.TranscribeRequest;
import com.yisy.modules.doraai.dto.TranscribeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

class VoiceServiceTest {

    private VoiceService voiceService;
    private OllamaConfig ollamaConfig;

    @BeforeEach
    void setUp() {
        ollamaConfig = new OllamaConfig();
        ollamaConfig.setConnectTimeoutMs(100);
        HttpClient httpClient = HttpClient.newBuilder().build();
        voiceService = new VoiceService(ollamaConfig, httpClient);
    }

    @Test
    void transcribe_throwsWhenAudioNull() {
        TranscribeRequest request = new TranscribeRequest();
        request.setAudioBase64(null);

        assertThrows(IllegalArgumentException.class, () -> voiceService.transcribe(request));
    }

    @Test
    void transcribe_throwsWhenAudioBlank() {
        TranscribeRequest request = new TranscribeRequest();
        request.setAudioBase64("   ");

        assertThrows(IllegalArgumentException.class, () -> voiceService.transcribe(request));
    }

    @Test
    void transcribe_throwsWhenAudioEmpty() {
        TranscribeRequest request = new TranscribeRequest();
        request.setAudioBase64("");

        assertThrows(IllegalArgumentException.class, () -> voiceService.transcribe(request));
    }

    @Test
    void transcribe_throwsWhenOllamaUnavailable() {
        ollamaConfig.setBaseUrl("http://localhost:9999");
        ollamaConfig.setReadTimeoutMs(100);
        HttpClient httpClient = HttpClient.newBuilder().build();
        voiceService = new VoiceService(ollamaConfig, httpClient);

        TranscribeRequest request = new TranscribeRequest();
        request.setAudioBase64("dGVzdCBhdWRpbyBkYXRh");

        assertThrows(RuntimeException.class, () -> voiceService.transcribe(request));
    }

    @Test
    void transcribeRequest_defaultConstructor() {
        TranscribeRequest request = new TranscribeRequest();

        assertNull(request.getAudioBase64());
        assertNull(request.getLanguageHint());
    }

    @Test
    void transcribeRequest_constructorWithAudio() {
        TranscribeRequest request = new TranscribeRequest("base64data");

        assertEquals("base64data", request.getAudioBase64());
        assertNull(request.getLanguageHint());
    }

    @Test
    void transcribeRequest_setters() {
        TranscribeRequest request = new TranscribeRequest();
        request.setAudioBase64("audio123");
        request.setLanguageHint("en");

        assertEquals("audio123", request.getAudioBase64());
        assertEquals("en", request.getLanguageHint());
    }

    @Test
    void transcribeResponse_defaultConstructor() {
        TranscribeResponse response = new TranscribeResponse();

        assertNull(response.getText());
        assertNull(response.getLanguage());
        assertEquals(0.0, response.getConfidence());
    }

    @Test
    void transcribeResponse_constructorWithTextAndLanguage() {
        TranscribeResponse response = new TranscribeResponse("Hello World", "en");

        assertEquals("Hello World", response.getText());
        assertEquals("en", response.getLanguage());
        assertEquals(1.0, response.getConfidence());
    }

    @Test
    void transcribeResponse_setters() {
        TranscribeResponse response = new TranscribeResponse();
        response.setText("Transcribed text");
        response.setLanguage("zh");
        response.setConfidence(0.95);

        assertEquals("Transcribed text", response.getText());
        assertEquals("zh", response.getLanguage());
        assertEquals(0.95, response.getConfidence());
    }

    @Test
    void transcribeResponse_emptyText() {
        TranscribeResponse response = new TranscribeResponse("", "en");

        assertEquals("", response.getText());
        assertEquals("en", response.getLanguage());
    }
}
