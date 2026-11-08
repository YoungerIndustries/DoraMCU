package com.yisy.modules.doramcu.unified.dto;

import com.yisy.modules.doramcu.unified.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from the unified character login system.
 * <p>
 * Contains the player's authentication token, their available characters
 * across all game systems, and the list of available games for character creation.
 * </p>
 */
public class UnifiedLoginResponse {

    private String token;
    private String playerEmail;
    private String playerName;
    private List<UnifiedCharacterSummary> characters;
    private List<GameTypeInfo> availableGames;
    private boolean needsCharacterSelection;
    private boolean needsCharacterCreation;

    public UnifiedLoginResponse() {
        this.characters = new ArrayList<>();
        this.availableGames = new ArrayList<>();
    }

    public UnifiedLoginResponse(String token, String playerEmail) {
        this.token = token;
        this.playerEmail = playerEmail;
        this.characters = new ArrayList<>();
        this.availableGames = new ArrayList<>();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlayerEmail() {
        return playerEmail;
    }

    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<UnifiedCharacterSummary> getCharacters() {
        return characters;
    }

    public void setCharacters(List<UnifiedCharacterSummary> characters) {
        this.characters = characters;
    }

    public List<GameTypeInfo> getAvailableGames() {
        return availableGames;
    }

    public void setAvailableGames(List<GameTypeInfo> availableGames) {
        this.availableGames = availableGames;
    }

    public boolean isNeedsCharacterSelection() {
        return needsCharacterSelection;
    }

    public void setNeedsCharacterSelection(boolean needsCharacterSelection) {
        this.needsCharacterSelection = needsCharacterSelection;
    }

    public boolean isNeedsCharacterCreation() {
        return needsCharacterCreation;
    }

    public void setNeedsCharacterCreation(boolean needsCharacterCreation) {
        this.needsCharacterCreation = needsCharacterCreation;
    }

    /**
     * Adds a character to the response.
     */
    public void addCharacter(UnifiedCharacterSummary character) {
        this.characters.add(character);
    }

    /**
     * Adds an available game to the response.
     */
    public void addAvailableGame(GameTypeInfo game) {
        this.availableGames.add(game);
    }

    /**
     * Information about an available game for character creation.
     */
    public static class GameTypeInfo {

        private GameType gameType;
        private String displayName;
        private String description;
        private List<String> characterTypes;

        public GameTypeInfo() {
        }

        public GameTypeInfo(GameType gameType, String displayName, String description) {
            this.gameType = gameType;
            this.displayName = displayName;
            this.description = description;
            this.characterTypes = new ArrayList<>();
        }

        public GameType getGameType() {
            return gameType;
        }

        public void setGameType(GameType gameType) {
            this.gameType = gameType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getCharacterTypes() {
            return characterTypes;
        }

        public void setCharacterTypes(List<String> characterTypes) {
            this.characterTypes = characterTypes;
        }

        public void addCharacterType(String type) {
            this.characterTypes.add(type);
        }
    }
}
