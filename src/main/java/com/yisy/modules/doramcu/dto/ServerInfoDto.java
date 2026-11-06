package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Metadata response describing server cluster identity and operational state.
 */
public class ServerInfoDto {

    private String serverName;
    private String version;
    private String status;
    private String clusterRegion;
    private List<String> activeGalaxies;
    private int playerCount;
    private long uptimeMillis;

    public ServerInfoDto() {
    }

    public ServerInfoDto(String serverName, String version, String status, String clusterRegion,
                         List<String> activeGalaxies, int playerCount, long uptimeMillis) {
        this.serverName = serverName;
        this.version = version;
        this.status = status;
        this.clusterRegion = clusterRegion;
        this.activeGalaxies = activeGalaxies;
        this.playerCount = playerCount;
        this.uptimeMillis = uptimeMillis;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClusterRegion() {
        return clusterRegion;
    }

    public void setClusterRegion(String clusterRegion) {
        this.clusterRegion = clusterRegion;
    }

    public List<String> getActiveGalaxies() {
        return activeGalaxies;
    }

    public void setActiveGalaxies(List<String> activeGalaxies) {
        this.activeGalaxies = activeGalaxies;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public long getUptimeMillis() {
        return uptimeMillis;
    }

    public void setUptimeMillis(long uptimeMillis) {
        this.uptimeMillis = uptimeMillis;
    }
}
