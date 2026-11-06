package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for logging in via Player PIN / Access Code.
 */
public class PinLoginRequest {

    @NotBlank(message = "Player PIN code is required")
    @Pattern(regexp = "^\\d{4,10}$", message = "Player PIN code must be between 4 and 10 digits")
    private String pin;

    private String empireName;

    public PinLoginRequest() {
    }

    public PinLoginRequest(String pin) {
        this.pin = pin;
    }

    public PinLoginRequest(String pin, String empireName) {
        this.pin = pin;
        this.empireName = empireName;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }
}
