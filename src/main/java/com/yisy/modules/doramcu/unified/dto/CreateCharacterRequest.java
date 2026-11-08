package com.yisy.modules.doramcu.unified.dto;

import com.yisy.modules.doramcu.unified.GameType;
import com.yisy.modules.doramcu.unified.TravellerCharacterType;

/**
 * Request to create a new character through the unified character creation system.
 * <p>
 * The first step is selecting the game type. If Traveller is selected,
 * the player must also specify the Traveller character type.
 * </p>
 */
public class CreateCharacterRequest {

    private GameType gameType;
    private TravellerCharacterType travellerCharacterType;
    private String firstName;
    private String lastName;
    private String email;

    // Captain's Log specific fields
    private String pronouns;
    private String trait1;
    private String trait2;
    private String trait3;

    public CreateCharacterRequest() {
    }

    public CreateCharacterRequest(GameType gameType, String firstName, String lastName, String email) {
        this.gameType = gameType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public TravellerCharacterType getTravellerCharacterType() {
        return travellerCharacterType;
    }

    public void setTravellerCharacterType(TravellerCharacterType travellerCharacterType) {
        this.travellerCharacterType = travellerCharacterType;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getTrait1() {
        return trait1;
    }

    public void setTrait1(String trait1) {
        this.trait1 = trait1;
    }

    public String getTrait2() {
        return trait2;
    }

    public void setTrait2(String trait2) {
        this.trait2 = trait2;
    }

    public String getTrait3() {
        return trait3;
    }

    public void setTrait3(String trait3) {
        this.trait3 = trait3;
    }

    /**
     * Returns the full name of the character.
     */
    public String getFullName() {
        if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
            return firstName.trim() + " " + lastName.trim();
        }
        if (firstName != null && !firstName.isBlank()) {
            return firstName.trim();
        }
        if (lastName != null && !lastName.isBlank()) {
            return lastName.trim();
        }
        return null;
    }

    /**
     * Validates that the request has the minimum required fields.
     */
    public boolean isValid() {
        return gameType != null && getFullName() != null && email != null && !email.isBlank();
    }
}
