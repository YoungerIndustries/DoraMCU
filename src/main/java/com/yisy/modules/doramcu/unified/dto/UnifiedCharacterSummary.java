package com.yisy.modules.doramcu.unified.dto;

import com.yisy.modules.doramcu.traveller.CharacterStatus;
import com.yisy.modules.doramcu.unified.GameType;

/**
 * Represents a character from any game system in a unified format.
 * <p>
 * Used by the unified character login system to present a single list
 * of all characters across all game systems to the player.
 * </p>
 */
public class UnifiedCharacterSummary {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private GameType gameType;
    private String gameRole;
    private String description;
    private boolean isAlive;
    private String lastActivity;
    private CharacterStatus characterStatus;

    public UnifiedCharacterSummary() {
    }

    public UnifiedCharacterSummary(Long id, String firstName, String lastName,
                                    GameType gameType, String gameRole) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        this.fullName = this.fullName.trim();
        this.gameType = gameType;
        this.gameRole = gameRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public String getGameRole() {
        return gameRole;
    }

    public void setGameRole(String gameRole) {
        this.gameRole = gameRole;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    public CharacterStatus getCharacterStatus() {
        return characterStatus;
    }

    public void setCharacterStatus(CharacterStatus characterStatus) {
        this.characterStatus = characterStatus;
    }

    @Override
    public String toString() {
        return "UnifiedCharacterSummary{" +
                "id=" + id +
                ", name='" + fullName + '\'' +
                ", game=" + gameType +
                ", role='" + gameRole + '\'' +
                '}';
    }
}
