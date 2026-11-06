package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSessionRequest {

    @NotBlank(message = "Session name is required")
    @Size(min = 1, max = 100, message = "Session name must be between 1 and 100 characters")
    private String sessionName;

    public CreateSessionRequest() {
    }

    public CreateSessionRequest(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
}
