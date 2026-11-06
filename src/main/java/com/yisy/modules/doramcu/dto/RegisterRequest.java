package com.yisy.modules.doramcu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    /**
     * The galaxy the player wants to join.
     * If null or empty, defaults to the game galaxy ("Milky Way").
     */
    private String galaxyName;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public RegisterRequest(String username, String password, String galaxyName) {
        this.username = username;
        this.password = password;
        this.galaxyName = galaxyName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }
}
