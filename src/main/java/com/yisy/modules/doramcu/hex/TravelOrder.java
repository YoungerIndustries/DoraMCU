package com.yisy.modules.doramcu.hex;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an active travel order for a fleet moving between star systems.
 * <p>
 * A travel order tracks the complete journey from departure to arrival, including:
 * <ul>
 *   <li>Origin and destination systems</li>
 *   <li>Travel mode (warp or impulse)</li>
 *   <li>Warp factor or impulse throttle</li>
 *   <li>Departure time and estimated time of arrival (ETA)</li>
 *   <li>Current status (en route, arrived, aborted, delayed)</li>
 *   <li>Progress percentage</li>
 * </ul>
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * TravelCalculator calculator = new TravelCalculator();
 * TravelCalculator.TravelResult result = calculator.calculateWarpTravel(from, to, 6.0);
 *
 * TravelOrder order = new TravelOrder.Builder(fleet, from, to)
 *     .warpFactor(6.0)
 *     .departureTime(LocalDateTime.now())
 *     .eta(result.calculateEta(LocalDateTime.now()))
 *     .distanceLightYears(result.getDistanceLightYears())
 *     .build();
 *
 * // Update progress as time passes
 * order.updateProgress(LocalDateTime.now());
 * System.out.println(order.getProgressPercentage());  // 50.0
 *
 * // Complete the journey
 * order.arrive();
 * System.out.println(order.getStatus());  // ARRIVED
 * </pre>
 */
public class TravelOrder {

    private final int id;
    private final Fleet fleet;
    private final GalacticEntry origin;
    private final GalacticEntry destination;
    private final TravelCalculator.TravelMode mode;
    private final double speedFactor;
    private final double distanceLightYears;
    private LocalDateTime departureTime;
    private final LocalDateTime eta;
    private final double travelTimeHours;
    private LocalDateTime actualArrivalTime;
    private TravelStatus status;
    private double progressPercentage;
    private String notes;

    private static int nextId = 1;

    /**
     * The status of a travel order.
     */
    public enum TravelStatus {
        /** Travel order created but not yet departed. */
        PENDING("Pending"),

        /** Fleet is currently en route. */
        EN_ROUTE("En Route"),

        /** Fleet has arrived at the destination. */
        ARRIVED("Arrived"),

        /** Travel was aborted before completion. */
        ABORTED("Aborted"),

        /** Travel is delayed (e.g., warp core breach, asteroid field). */
        DELAYED("Delayed"),

        /** Fleet is in orbit around the destination. */
        IN_ORBIT("In Orbit");

        private final String displayName;

        TravelStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Creates a new travel order using the builder.
     */
    private TravelOrder(Builder builder) {
        this.id = nextId++;
        this.fleet = Objects.requireNonNull(builder.fleet, "Fleet must not be null");
        this.origin = Objects.requireNonNull(builder.origin, "Origin must not be null");
        this.destination = Objects.requireNonNull(builder.destination, "Destination must not be null");
        this.mode = Objects.requireNonNull(builder.mode, "Mode must not be null");
        this.speedFactor = builder.speedFactor;
        this.distanceLightYears = builder.distanceLightYears;
        this.departureTime = builder.departureTime;
        // Note: departureTime is mutable — set to actual time on depart()
        this.eta = builder.eta;
        this.travelTimeHours = builder.travelTimeHours;
        this.status = TravelStatus.PENDING;
        this.progressPercentage = 0.0;
        this.notes = builder.notes;
    }

    // ---- Accessors ----

    /**
     * @return the unique travel order ID
     */
    public int getId() {
        return id;
    }

    /**
     * @return the fleet making this journey
     */
    public Fleet getFleet() {
        return fleet;
    }

    /**
     * @return the origin system
     */
    public GalacticEntry getOrigin() {
        return origin;
    }

    /**
     * @return the destination system
     */
    public GalacticEntry getDestination() {
        return destination;
    }

    /**
     * @return the travel mode (WARP or IMPULSE)
     */
    public TravelCalculator.TravelMode getMode() {
        return mode;
    }

    /**
     * @return the speed factor (warp factor or impulse throttle)
     */
    public double getSpeedFactor() {
        return speedFactor;
    }

    /**
     * @return the distance in light-years
     */
    public double getDistanceLightYears() {
        return distanceLightYears;
    }

    /**
     * @return the departure time, or null if not yet departed
     */
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    /**
     * @return the estimated time of arrival
     */
    public LocalDateTime getEta() {
        return eta;
    }

    /**
     * @return the planned travel time in hours
     */
    public double getTravelTimeHours() {
        return travelTimeHours;
    }

    /**
     * @return the actual arrival time, or null if not yet arrived
     */
    public LocalDateTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    /**
     * @return the current travel status
     */
    public TravelStatus getStatus() {
        return status;
    }

    /**
     * @return the progress percentage (0.0 to 100.0)
     */
    public double getProgressPercentage() {
        return progressPercentage;
    }

    /**
     * @return any notes about this travel order
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets notes for this travel order.
     *
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ---- Status Transitions ----

    /**
     * Departs on the journey.
     */
    public void depart() {
        if (status != TravelStatus.PENDING && status != TravelStatus.DELAYED) {
            throw new IllegalStateException("Cannot depart from status: " + status);
        }
        this.status = TravelStatus.EN_ROUTE;
        if (this.departureTime == null) {
            this.departureTime = LocalDateTime.now();
        }
        this.progressPercentage = 0.0;
    }

    /**
     * Updates the progress based on the current time.
     *
     * @param currentTime the current game time
     */
    public void updateProgress(LocalDateTime currentTime) {
        if (status != TravelStatus.EN_ROUTE) {
            return;
        }

        if (departureTime == null) {
            return;
        }

        double elapsedHours = java.time.Duration.between(departureTime, currentTime).toMinutes() / 60.0;
        double progress = (elapsedHours / travelTimeHours) * 100.0;
        this.progressPercentage = Math.min(100.0, Math.max(0.0, progress));

        // Check if arrived
        if (progress >= 100.0) {
            arrive();
        }
    }

    /**
     * Arrives at the destination.
     */
    public void arrive() {
        if (status != TravelStatus.EN_ROUTE) {
            throw new IllegalStateException("Cannot arrive from status: " + status);
        }
        this.status = TravelStatus.ARRIVED;
        this.progressPercentage = 100.0;
        this.actualArrivalTime = LocalDateTime.now();

        // Update fleet position
        if (destination != null) {
            fleet.setPosition(new Hex3D(
                    destination.getX(),
                    destination.getY(),
                    destination.getZ()
            ));
        }
    }

    /**
     * Enters orbit around the destination.
     */
    public void enterOrbit() {
        if (status != TravelStatus.ARRIVED && status != TravelStatus.EN_ROUTE) {
            throw new IllegalStateException("Cannot enter orbit from status: " + status);
        }
        this.status = TravelStatus.IN_ORBIT;
        this.progressPercentage = 100.0;
        if (actualArrivalTime == null) {
            this.actualArrivalTime = LocalDateTime.now();
        }
    }

    /**
     * Aborts the journey.
     *
     * @param reason the reason for aborting
     */
    public void abort(String reason) {
        if (status == TravelStatus.ARRIVED || status == TravelStatus.ABORTED) {
            throw new IllegalStateException("Cannot abort from status: " + status);
        }
        this.status = TravelStatus.ABORTED;
        this.notes = (notes != null ? notes + ". " : "") + "Aborted: " + reason;
    }

    /**
     * Delays the journey.
     *
     * @param reason the reason for the delay
     */
    public void delay(String reason) {
        if (status == TravelStatus.ARRIVED || status == TravelStatus.ABORTED) {
            throw new IllegalStateException("Cannot delay from status: " + status);
        }
        this.status = TravelStatus.DELAYED;
        this.notes = (notes != null ? notes + ". " : "") + "Delayed: " + reason;
    }

    /**
     * Resumes the journey after a delay.
     */
    public void resume() {
        if (status != TravelStatus.DELAYED) {
            throw new IllegalStateException("Cannot resume from status: " + status);
        }
        this.status = TravelStatus.EN_ROUTE;
        this.departureTime = LocalDateTime.now();
        // Recalculate ETA from current time with remaining travel time
        double remainingProgress = 100.0 - this.progressPercentage;
        double remainingHours = (remainingProgress / 100.0) * this.travelTimeHours;
    }

    // ---- Formatted Output ----

    /**
     * @return a formatted string describing the speed
     */
    public String getSpeedFormatted() {
        if (mode == TravelCalculator.TravelMode.WARP) {
            return String.format("Warp %.1f (%.0fc)", speedFactor, WarpFactor.tngVelocity(speedFactor));
        } else {
            return String.format("Impulse %.0f%% (%.2fc)", speedFactor * 100, WarpFactor.impulseVelocity(speedFactor));
        }
    }

    /**
     * @return a formatted string describing the distance
     */
    public String getDistanceFormatted() {
        return StarSystemDistance.formatDistance(distanceLightYears);
    }

    /**
     * @return a formatted string describing the ETA
     */
    public String getEtaFormatted() {
        if (eta == null) {
            return "N/A";
        }
        return eta.toString();
    }

    /**
     * @return a formatted string describing the travel time
     */
    public String getTravelTimeFormatted() {
        return WarpFactor.formatTravelTime(travelTimeHours);
    }

    /**
     * @return a formatted progress string (e.g., "45% complete")
     */
    public String getProgressFormatted() {
        return String.format("%.1f%% complete", progressPercentage);
    }

    /**
     * @return a formatted string describing the remaining time
     */
    public String getRemainingTimeFormatted() {
        if (status == TravelStatus.ARRIVED || status == TravelStatus.ABORTED) {
            return "N/A";
        }
        if (departureTime == null) {
            return getTravelTimeFormatted();
        }
        double elapsed = java.time.Duration.between(departureTime, LocalDateTime.now()).toMinutes() / 60.0;
        double remaining = Math.max(0, travelTimeHours - elapsed);
        return WarpFactor.formatTravelTime(remaining);
    }

    @Override
    public String toString() {
        return String.format("TravelOrder#%d{%s → %s, %s, %s, %s, %s}",
                id,
                origin != null ? origin.getSystem().getId() : "N/A",
                destination != null ? destination.getSystem().getId() : "N/A",
                mode.getDisplayName(),
                getSpeedFormatted(),
                getDistanceFormatted(),
                status.getDisplayName());
    }

    // ---- Builder ----

    /**
     * Builder for creating travel orders.
     */
    public static class Builder {
        private final Fleet fleet;
        private final GalacticEntry origin;
        private final GalacticEntry destination;
        private TravelCalculator.TravelMode mode = TravelCalculator.TravelMode.WARP;
        private double speedFactor = WarpFactor.NORMAL_CRUISE_WARP;
        private double distanceLightYears = 0.0;
        private LocalDateTime departureTime;
        private LocalDateTime eta;
        private double travelTimeHours = 0.0;
        private String notes;

        /**
         * Creates a new builder.
         *
         * @param fleet the fleet making the journey
         * @param origin the origin system
         * @param destination the destination system
         */
        public Builder(Fleet fleet, GalacticEntry origin, GalacticEntry destination) {
            this.fleet = fleet;
            this.origin = origin;
            this.destination = destination;
        }

        /**
         * Sets the travel mode.
         *
         * @param mode the travel mode
         * @return this builder
         */
        public Builder mode(TravelCalculator.TravelMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the warp factor.
         *
         * @param warpFactor the warp factor
         * @return this builder
         */
        public Builder warpFactor(double warpFactor) {
            this.mode = TravelCalculator.TravelMode.WARP;
            this.speedFactor = warpFactor;
            return this;
        }

        /**
         * Sets the impulse throttle.
         *
         * @param throttle the impulse throttle (0.0 to 1.0)
         * @return this builder
         */
        public Builder impulseThrottle(double throttle) {
            this.mode = TravelCalculator.TravelMode.IMPULSE;
            this.speedFactor = throttle;
            return this;
        }

        /**
         * Sets the distance in light-years.
         *
         * @param distanceLightYears the distance
         * @return this builder
         */
        public Builder distanceLightYears(double distanceLightYears) {
            this.distanceLightYears = distanceLightYears;
            return this;
        }

        /**
         * Sets the departure time.
         *
         * @param departureTime the departure time
         * @return this builder
         */
        public Builder departureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        /**
         * Sets the estimated time of arrival.
         *
         * @param eta the ETA
         * @return this builder
         */
        public Builder eta(LocalDateTime eta) {
            this.eta = eta;
            return this;
        }

        /**
         * Sets the travel time in hours.
         *
         * @param travelTimeHours the travel time
         * @return this builder
         */
        public Builder travelTimeHours(double travelTimeHours) {
            this.travelTimeHours = travelTimeHours;
            return this;
        }

        /**
         * Sets notes for this travel order.
         *
         * @param notes the notes
         * @return this builder
         */
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        /**
         * Populates this builder from a travel result.
         *
         * @param result the travel result
         * @return this builder
         */
        public Builder fromResult(TravelCalculator.TravelResult result) {
            this.mode = result.getMode();
            this.speedFactor = result.getSpeedFactor();
            this.distanceLightYears = result.getDistanceLightYears();
            this.travelTimeHours = result.getTravelTimeHours();
            return this;
        }

        /**
         * Builds the travel order.
         *
         * @return the built travel order
         */
        public TravelOrder build() {
            return new TravelOrder(this);
        }
    }
}
