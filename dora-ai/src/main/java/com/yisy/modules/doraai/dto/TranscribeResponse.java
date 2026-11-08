package com.yisy.modules.doraai.dto;

/**
 * Response for speech-to-text transcription.
 */
public class TranscribeResponse {

    private String text;
    private String language;
    private double confidence;

    public TranscribeResponse() {
    }

    public TranscribeResponse(String text, String language) {
        this.text = text;
        this.language = language;
        this.confidence = 1.0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
