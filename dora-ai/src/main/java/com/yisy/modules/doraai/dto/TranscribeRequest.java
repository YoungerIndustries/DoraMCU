package com.yisy.modules.doraai.dto;

/**
 * Request for speech-to-text transcription.
 */
public class TranscribeRequest {

    private String audioBase64;
    private String languageHint;

    public TranscribeRequest() {
    }

    public TranscribeRequest(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getLanguageHint() {
        return languageHint;
    }

    public void setLanguageHint(String languageHint) {
        this.languageHint = languageHint;
    }
}
