package com.yisy.modules.doramcu.dto;

public class JoinSessionResponse {

    private String sessionId;
    private String sessionName;
    private Long playerId;
    private String playerName;

    public JoinSessionResponse() {
    }

    public JoinSessionResponse(String sessionId, String sessionName, Long playerId, String playerName) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
