package com.yisy.modules.doramcu.traveller;

/**
 * Player's choice for how to resolve a tour of duty.
 * <p>
 * The player can choose to resolve a tour immediately using Traveller rules
 * (IMMEDIATE) or serve the tour in the 4X game for a specified number of turns
 * (FOUR_X).
 * </p>
 */
public class TourOfDutyRequest {

    private int characterId;
    private ResolveMode resolveMode;
    private Career career;
    private int tourTurns;

    /**
     * How the tour should be resolved.
     */
    public enum ResolveMode {
        /**
         * Resolve the tour immediately using Traveller rules.
         */
        IMMEDIATE,
        /**
         * Serve the tour in the 4X game for the specified number of turns.
         */
        FOUR_X
    }

    public TourOfDutyRequest() {
    }

    public TourOfDutyRequest(int characterId, ResolveMode resolveMode, Career career, int tourTurns) {
        this.characterId = characterId;
        this.resolveMode = resolveMode;
        this.career = career;
        this.tourTurns = tourTurns;
    }

    public int getCharacterId() {
        return characterId;
    }

    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    public ResolveMode getResolveMode() {
        return resolveMode;
    }

    public void setResolveMode(ResolveMode resolveMode) {
        this.resolveMode = resolveMode;
    }

    public Career getCareer() {
        return career;
    }

    public void setCareer(Career career) {
        this.career = career;
    }

    public int getTourTurns() {
        return tourTurns;
    }

    public void setTourTurns(int tourTurns) {
        this.tourTurns = tourTurns;
    }

    /**
     * Validates the request.
     *
     * @return true if the request has all required fields
     */
    public boolean isValid() {
        return characterId > 0 && resolveMode != null && career != null;
    }

    /**
     * Returns true if this request is for 4X game resolution.
     */
    public boolean isFourXMode() {
        return resolveMode == ResolveMode.FOUR_X;
    }

    /**
     * Returns true if this request is for immediate resolution.
     */
    public boolean isImmediateMode() {
        return resolveMode == ResolveMode.IMMEDIATE;
    }

    @Override
    public String toString() {
        return "TourOfDutyRequest{" +
                "characterId=" + characterId +
                ", resolveMode=" + resolveMode +
                ", career=" + career +
                ", tourTurns=" + tourTurns +
                '}';
    }
}
