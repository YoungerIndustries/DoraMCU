package com.yisy.modules.doramcu.captainslog.dto;

/**
 * Request to start a new starship creation session.
 */
public class StarshipCreationStartRequest {

    private String name;
    private String registry;

    public StarshipCreationStartRequest() {
    }

    public StarshipCreationStartRequest(String name, String registry) {
        this.name = name;
        this.registry = registry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }
}
