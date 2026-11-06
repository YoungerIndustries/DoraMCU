package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinSessionRequest {

    @NotBlank(message = "Session ID is required")
    @Size(min = 1, max = 50, message = "Session ID must be between 1 and 50 characters")
    private String sessionId;

    public JoinSessionRequest() {
    }

    public JoinSessionRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
