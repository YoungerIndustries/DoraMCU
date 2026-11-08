package com.yisy.modules.doraai.dto;

/**
 * Response containing translated text and language information.
 */
public class TranslateResponse {

    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;

    public TranslateResponse() {
    }

    public TranslateResponse(String translatedText, String sourceLanguage, String targetLanguage) {
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
