package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tracks a ship's current jump travel state.
 * <p>
 * In Classic Traveller Book 2, a ship's travel state determines what actions
 * are available. A ship at a system can refuel, repair, or prepare for jump.
 * A ship in transit cannot be intercepted and is consuming fuel.
 * </p>
 * <p>
 * The travel status tracks:
 * </p>
 * <ul>
 *   <li>Current location (star system)</li>
 *   <li>Travel state (at system, in transit, etc.)</li>
 *   <li>Current jump plan, if any</li>
 *   <li>Progress through the current jump plan</li>
 *   <li>Fuel tank status</li>
 *   <li>Travel history (visited systems)</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Ship "Explorer" at Tukkayj, preparing for jump to Mesna
 *   Fuel: 2000/3000 tons, 2 jumps remaining
 * </pre>
 */
public class JumpTravelStatus {

    /**
     * The ship's current location.
     */
    private TravellerWorld currentWorld;

    /**
     * The ship's current travel state.
     */
    private TravelState travelState;

    /**
     * The current jump plan, if any.
     */
    private JumpPlan currentPlan;

    /**
     * Index of the current jump leg in the plan.
     * -1 if no active plan.
     */
    private int currentLegIndex;

    /**
     * The ship's fuel tank.
     */
    private final FuelTank fuelTank;

    /**
     * The ship's jump drive rating.
     */
    private final int jumpRating;

    /**
     * History of visited worlds.
     */
    private final List<TravellerWorld> visitedWorlds;

    /**
     * Number of turns spent cooling down after jump.
     */
    private int coolingTurns;

    /**
     * Creates a new jump travel status for a ship.
     *
     * @param startingWorld the world where the ship starts
     * @param fuelTank the ship's fuel tank
     * @param jumpRating the ship's jump drive rating
     */
    public JumpTravelStatus(TravellerWorld startingWorld, FuelTank fuelTank, int jumpRating) {
        this.currentWorld = Objects.requireNonNull(startingWorld, "Starting world must not be null");
        this.fuelTank = Objects.requireNonNull(fuelTank, "Fuel tank must not be null");
        this.jumpRating = jumpRating;
        this.travelState = TravelState.AT_SYSTEM;
        this.currentPlan = null;
        this.currentLegIndex = -1;
        this.visitedWorlds = new ArrayList<>();
        this.visitedWorlds.add(startingWorld);
        this.coolingTurns = 0;
    }

    // ---- Accessors ----

    /**
     * @return the ship's current location
     */
    public TravellerWorld getCurrentWorld() {
        return currentWorld;
    }

    /**
     * @return the ship's current travel state
     */
    public TravelState getTravelState() {
        return travelState;
    }

    /**
     * @return the current jump plan, or null if none
     */
    public JumpPlan getCurrentPlan() {
        return currentPlan;
    }

    /**
     * @return the index of the current jump leg, or -1 if no active plan
     */
    public int getCurrentLegIndex() {
        return currentLegIndex;
    }

    /**
     * @return the ship's fuel tank
     */
    public FuelTank getFuelTank() {
        return fuelTank;
    }

    /**
     * @return the ship's jump drive rating
     */
    public int getJumpRating() {
        return jumpRating;
    }

    /**
     * @return the list of visited worlds (immutable)
     */
    public List<TravellerWorld> getVisitedWorlds() {
        return Collections.unmodifiableList(visitedWorlds);
    }

    /**
     * @return the number of cooling turns remaining
     */
    public int getCoolingTurns() {
        return coolingTurns;
    }

    // ---- Derived Properties ----

    /**
     * @return true if the ship is at a star system
     */
    public boolean isAtSystem() {
        return travelState == TravelState.AT_SYSTEM;
    }

    /**
     * @return true if the ship is in jump space
     */
    public boolean isInTransit() {
        return travelState == TravelState.IN_TRANSIT;
    }

    /**
     * @return true if the ship can perform actions (at system and not cooling)
     */
    public boolean canPerformActions() {
        return travelState == TravelState.AT_SYSTEM;
    }

    /**
     * @return true if the ship can start a new jump
     */
    public boolean canStartJump() {
        return travelState == TravelState.AT_SYSTEM &&
                !fuelTank.isEmpty() &&
                currentPlan == null;
    }

    /**
     * @return the number of jumps completed in the current plan
     */
    public int getJumpsCompleted() {
        if (currentPlan == null) {
            return 0;
        }
        return currentLegIndex;
    }

    /**
     * @return the number of jumps remaining in the current plan
     */
    public int getJumpsRemaining() {
        if (currentPlan == null) {
            return 0;
        }
        return currentPlan.getJumpCount() - currentLegIndex;
    }

    // ---- Travel Operations ----

    /**
     * Starts a new jump plan.
     * <p>
     * The ship must be at a system and have enough fuel.
     * </p>
     *
     * @param plan the jump plan to execute
     * @return true if the plan was started, false if conditions not met
     */
    public boolean startJumpPlan(JumpPlan plan) {
        Objects.requireNonNull(plan, "Jump plan must not be null");

        if (!canStartJump()) {
            return false;
        }

        if (!fuelTank.hasEnoughFuelForPlan(plan)) {
            this.travelState = TravelState.NEEDS_REFUEL;
            return false;
        }

        if (!plan.isExecutable(jumpRating)) {
            return false;
        }

        this.currentPlan = plan;
        this.currentLegIndex = 0;
        this.travelState = TravelState.PREPARING_JUMP;
        return true;
    }

    /**
     * Executes the next jump in the current plan.
     * <p>
     * The ship must be preparing for jump or in transit.
     * Fuel is consumed and the ship moves to the next system.
     * </p>
     *
     * @return true if the jump was executed, false if conditions not met
     */
    public boolean executeNextJump() {
        if (currentPlan == null || currentLegIndex < 0 || currentLegIndex >= currentPlan.getJumpCount()) {
            return false;
        }

        if (travelState != TravelState.PREPARING_JUMP && travelState != TravelState.IN_TRANSIT) {
            return false;
        }

        JumpRoute leg = currentPlan.getJumpLegs().get(currentLegIndex);

        // Consume fuel
        if (!fuelTank.consumeFuel(leg.getFuelCostTons())) {
            this.travelState = TravelState.NEEDS_REFUEL;
            return false;
        }

        // Move to destination
        this.currentWorld = leg.getDestination();
        this.visitedWorlds.add(currentWorld);

        // Advance to next leg
        currentLegIndex++;

        // Check if plan is complete
        if (currentLegIndex >= currentPlan.getJumpCount()) {
            completeJumpPlan();
        } else {
            this.travelState = TravelState.IN_TRANSIT;
        }

        return true;
    }

    /**
     * Completes the current jump plan.
     */
    private void completeJumpPlan() {
        this.travelState = TravelState.AT_SYSTEM;
        this.currentPlan = null;
        this.currentLegIndex = -1;

        // Check if ship needs refuel
        if (fuelTank.isEmpty()) {
            this.travelState = TravelState.NEEDS_REFUEL;
        }
    }

    /**
     * Aborts the current jump plan.
     * <p>
     * The ship returns to AT_SYSTEM state.
     * </p>
     */
    public void abortJumpPlan() {
        this.currentPlan = null;
        this.currentLegIndex = -1;
        this.travelState = TravelState.AT_SYSTEM;
    }

    /**
     * Refuels the ship at the current system.
     *
     * @param tons the amount of fuel to add
     * @return the actual amount added
     */
    public int refuel(int tons) {
        if (travelState != TravelState.AT_SYSTEM && travelState != TravelState.NEEDS_REFUEL) {
            return 0;
        }
        int added = fuelTank.refuel(tons);
        if (added > 0 && travelState == TravelState.NEEDS_REFUEL) {
            this.travelState = TravelState.AT_SYSTEM;
        }
        return added;
    }

    /**
     * Advances the cooling down timer.
     * <p>
     * Called each turn while the ship is cooling down.
     * </p>
     */
    public void advanceCooling() {
        if (travelState == TravelState.COOLING_DOWN) {
            coolingTurns--;
            if (coolingTurns <= 0) {
                this.travelState = TravelState.AT_SYSTEM;
            }
        }
    }

    /**
     * Checks if the ship is stranded (no fuel and cannot jump).
     *
     * @return true if the ship is stranded
     */
    public boolean isStranded() {
        return fuelTank.isEmpty() && travelState != TravelState.IN_TRANSIT;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("At ").append(currentWorld.getName()).append(", state: ").append(travelState);

        if (currentPlan != null) {
            sb.append(", plan: ").append(currentPlan.getJumpCount() - currentLegIndex).append(" jumps remaining");
        }

        sb.append(", fuel: ").append(fuelTank);

        if (isStranded()) {
            sb.append(" [STRANDED]");
        }

        return sb.toString();
    }
}
