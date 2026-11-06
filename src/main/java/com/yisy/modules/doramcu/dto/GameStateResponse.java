package com.yisy.modules.doramcu.dto;

import java.util.List;

public class GameStateResponse {

    private String sessionId;
    private int currentTurn;
    private Long currentPlayerId;
    private String currentPlayerName;
    private List<PlayerSummary> players;
    private boolean isMyTurn;
    private List<SubSectorInfo> subSectors;
    private ResourcePipelineSummary pipelineSummary;
    private List<FleetDto> fleets;
    // Turn timing fields for timed turns UI
    private long turnIntervalSeconds;
    private long accumulatedSeconds;
    private boolean schedulerEnabled;
    // Empire-specific data for the logged-in player
    private EmpireSummary empireSummary;

    public GameStateResponse() {
    }

    public GameStateResponse(String sessionId, int currentTurn, Long currentPlayerId, String currentPlayerName,
                             List<PlayerSummary> players, boolean isMyTurn) {
        this.sessionId = sessionId;
        this.currentTurn = currentTurn;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.players = players;
        this.isMyTurn = isMyTurn;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

    public List<PlayerSummary> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerSummary> players) {
        this.players = players;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void setMyTurn(boolean myTurn) {
        isMyTurn = myTurn;
    }

    public List<SubSectorInfo> getSubSectors() {
        return subSectors;
    }

    public void setSubSectors(List<SubSectorInfo> subSectors) {
        this.subSectors = subSectors;
    }

    public ResourcePipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }

    public void setPipelineSummary(ResourcePipelineSummary pipelineSummary) {
        this.pipelineSummary = pipelineSummary;
    }

    public List<FleetDto> getFleets() {
        return fleets;
    }

    public void setFleets(List<FleetDto> fleets) {
        this.fleets = fleets;
    }

    public long getTurnIntervalSeconds() {
        return turnIntervalSeconds;
    }

    public void setTurnIntervalSeconds(long turnIntervalSeconds) {
        this.turnIntervalSeconds = turnIntervalSeconds;
    }

    public long getAccumulatedSeconds() {
        return accumulatedSeconds;
    }

    public void setAccumulatedSeconds(long accumulatedSeconds) {
        this.accumulatedSeconds = accumulatedSeconds;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }

    public EmpireSummary getEmpireSummary() {
        return empireSummary;
    }

    public void setEmpireSummary(EmpireSummary empireSummary) {
        this.empireSummary = empireSummary;
    }
}
