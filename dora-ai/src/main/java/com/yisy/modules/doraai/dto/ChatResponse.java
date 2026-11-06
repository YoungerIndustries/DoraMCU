package com.yisy.modules.doraai.dto;

/**
 * Response from the AI chat.
 */
public class ChatResponse {

    private String sessionId;
    private String content;
    private boolean done;

    public ChatResponse() {
    }

    public ChatResponse(String sessionId, String content, boolean done) {
        this.sessionId = sessionId;
        this.content = content;
        this.done = done;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
