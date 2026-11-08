package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Current adventure state for the UI.
 * <p>
 * This DTO represents the complete state of a Traveller character's adventure
 * in the 4X universe, including their position, ship information, credits,
 * nearby events, and recent experiences.
 * </p>
 */
public class AdventureState {

    private int characterId;
    private String characterName;
    private String currentSystemName;
    private String currentPlanetName;
    private String shipName;
    private String shipType;
    private int shipHullPoints;
    private int shipShieldPoints;
    private long credits;
    private boolean isPassenger;
    private String passengerShipName;
    private String destinationSystemName;
    private final List<AdventureEvent> nearbyEvents = new ArrayList<>();
    private final List<CharacterExperience> recentExperiences = new ArrayList<>();

    public AdventureState() {
    }

    /**
     * Creates an adventure state for the given character.
     *
     * @param characterId the character's ID
     * @param characterName the character's name
     */
    public AdventureState(int characterId, String characterName) {
        this.characterId = characterId;
        this.characterName = characterName;
    }

    // ---- Accessors ----

    public int getCharacterId() {
        return characterId;
    }

    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getCurrentSystemName() {
        return currentSystemName;
    }

    public void setCurrentSystemName(String currentSystemName) {
        this.currentSystemName = currentSystemName;
    }

    public String getCurrentPlanetName() {
        return currentPlanetName;
    }

    public void setCurrentPlanetName(String currentPlanetName) {
        this.currentPlanetName = currentPlanetName;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public int getShipHullPoints() {
        return shipHullPoints;
    }

    public void setShipHullPoints(int shipHullPoints) {
        this.shipHullPoints = shipHullPoints;
    }

    public int getShipShieldPoints() {
        return shipShieldPoints;
    }

    public void setShipShieldPoints(int shipShieldPoints) {
        this.shipShieldPoints = shipShieldPoints;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public boolean isPassenger() {
        return isPassenger;
    }

    public void setPassenger(boolean passenger) {
        this.isPassenger = passenger;
    }

    public String getPassengerShipName() {
        return passengerShipName;
    }

    public void setPassengerShipName(String passengerShipName) {
        this.passengerShipName = passengerShipName;
    }

    public String getDestinationSystemName() {
        return destinationSystemName;
    }

    public void setDestinationSystemName(String destinationSystemName) {
        this.destinationSystemName = destinationSystemName;
    }

    /**
     * Returns the list of nearby 4X events the character can witness.
     *
     * @return unmodifiable list of nearby events
     */
    public List<AdventureEvent> getNearbyEvents() {
        return Collections.unmodifiableList(nearbyEvents);
    }

    /**
     * Adds a nearby event.
     *
     * @param event the event to add
     */
    public void addNearbyEvent(AdventureEvent event) {
        if (event != null) {
            this.nearbyEvents.add(event);
        }
    }

    /**
     * Returns the list of recent character experiences.
     *
     * @return unmodifiable list of recent experiences
     */
    public List<CharacterExperience> getRecentExperiences() {
        return Collections.unmodifiableList(recentExperiences);
    }

    /**
     * Adds a recent experience.
     *
     * @param experience the experience to add
     */
    public void addRecentExperience(CharacterExperience experience) {
        if (experience != null) {
            this.recentExperiences.add(experience);
        }
    }

    /**
     * Returns the number of nearby events.
     *
     * @return the event count
     */
    public int getNearbyEventCount() {
        return nearbyEvents.size();
    }

    /**
     * Returns the number of recent experiences.
     *
     * @return the experience count
     */
    public int getRecentExperienceCount() {
        return recentExperiences.size();
    }

    @Override
    public String toString() {
        return "AdventureState{" +
                "characterId=" + characterId +
                ", name='" + characterName + '\'' +
                ", system='" + currentSystemName + '\'' +
                ", planet='" + currentPlanetName + '\'' +
                ", ship='" + shipName + '\'' +
                ", shipType='" + shipType + '\'' +
                ", credits=" + credits +
                ", passenger=" + isPassenger +
                ", nearbyEvents=" + nearbyEvents.size() +
                ", experiences=" + recentExperiences.size() +
                '}';
    }
}
