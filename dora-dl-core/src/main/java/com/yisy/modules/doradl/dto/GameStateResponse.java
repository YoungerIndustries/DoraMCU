package com.yisy.modules.doradl.dto;

import java.util.List;

/**
 * DTO representing a game state response for the DL (deep learning) module.
 * <p>
 * NOTE: This is a lightweight subset of {@code com.yisy.modules.doramcu.dto.GameStateResponse}.
 * The two classes are intentionally duplicated because dora-dl-core is a standalone library
 * that cannot depend on the root DoraMCU module. Keep the field sets in sync when adding
 * new features to the root module's version.
 * </p>
 */
public class GameStateResponse {

    private String sessionId;
    private int currentTurn;
    private Long currentPlayerId;
    private String currentPlayerName;
    private List<PlayerSummary> players;
    private boolean isMyTurn;
    private List<SubSectorInfo> subSectors;

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
}
