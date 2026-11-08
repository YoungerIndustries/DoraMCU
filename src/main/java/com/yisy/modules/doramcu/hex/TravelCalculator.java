package com.yisy.modules.doramcu.hex;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Calculates travel times between star systems using Star Trek warp and impulse speeds.
 * <p>
 * This class combines {@link WarpFactor} velocity calculations with
 * {@link StarSystemDistance} distance calculations to produce travel estimates.
 * </p>
 * <p>
 * Travel modes:
 * <ul>
 *   <li><b>Warp</b> — Faster-than-light travel using warp drive. Travel time ranges
 *       from seconds ( nearby systems at high warp) to days (quadrant-spanning jumps).</li>
 *   <li><b>Impulse</b> — Sub-light travel using impulse engines. Used for within-system
 *       travel or when warp drive is unavailable. Travel time ranges from hours
 *       (within-system) to years (interstellar).</li>
 * </ul>
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * TravelCalculator calculator = new TravelCalculator();
 *
 * // Calculate warp travel time
 * TravelResult result = calculator.calculateWarpTravel(
 *     fromEntry, toEntry, 6.0  // warp 6
 * );
 * System.out.println(result.getEtaFormatted());  // "2 hours, 15 minutes"
 *
 * // Calculate impulse travel time
 * TravelResult impulse = calculator.calculateImpulseTravel(
 *     fromEntry, toEntry, 1.0  // full impulse
 * );
 * </pre>
 */
public class TravelCalculator {

    private final StarSystemDistance distanceCalculator;

    /**
     * Creates a new travel calculator with default distance settings.
     */
    public TravelCalculator() {
        this.distanceCalculator = new StarSystemDistance();
    }

    /**
     * Creates a new travel calculator with a custom distance calculator.
     *
     * @param distanceCalculator the distance calculator to use
     */
    public TravelCalculator(StarSystemDistance distanceCalculator) {
        this.distanceCalculator = Objects.requireNonNull(distanceCalculator, "Distance calculator must not be null");
    }

    // ---- Warp Travel ----

    /**
     * Calculates the travel time for a warp journey between two systems.
     *
     * @param from the source galactic entry
     * @param to the destination galactic entry
     * @param warpFactor the warp factor for the journey
     * @return a travel result with distance, time, and ETA
     */
    public TravelResult calculateWarpTravel(GalacticEntry from, GalacticEntry to, double warpFactor) {
        double distanceLy = distanceCalculator.lightYearsBetween(from, to);
        double timeHours = WarpFactor.travelTimeHours(distanceLy, warpFactor);

        return new TravelResult(
                from, to,
                distanceLy,
                timeHours,
                TravelMode.WARP,
                warpFactor,
                WarpFactor.tngVelocity(warpFactor)
        );
    }

    /**
     * Calculates the travel time for a warp journey between two systems,
     * using the fleet's maximum warp factor.
     *
     * @param from the source galactic entry
     * @param to the destination galactic entry
     * @param fleet the fleet making the journey
     * @return a travel result with distance, time, and ETA
     */
    public TravelResult calculateWarpTravel(GalacticEntry from, GalacticEntry to, Fleet fleet) {
        // Map JDrive speed to warp factor
        // JDrive 1-8 maps to warp 2-9 range
        int jDrive = fleet.getFleetJDriveSpeed();
        double warpFactor = jDriveToWarpFactor(jDrive);

        // Apply health reduction
        double healthRatio = fleet.getFleetHealthRatio();
        if (healthRatio < 0.2) {
            // Warp drive inoperable below 20% health
            return TravelResult.inoperable(from, to, distanceCalculator.lightYearsBetween(from, to),
                    "Warp drive inoperable — fleet health below 20%");
        }

        // Reduce warp factor by health
        warpFactor *= Math.max(0.5, healthRatio);

        return calculateWarpTravel(from, to, warpFactor);
    }

    // ---- Impulse Travel ----

    /**
     * Calculates the travel time for an impulse journey between two systems.
     *
     * @param from the source galactic entry
     * @param to the destination galactic entry
     * @param throttle the impulse throttle (0.0 to 1.0, where 1.0 = full impulse)
     * @return a travel result with distance, time, and ETA
     */
    public TravelResult calculateImpulseTravel(GalacticEntry from, GalacticEntry to, double throttle) {
        double distanceLy = distanceCalculator.lightYearsBetween(from, to);
        double timeHours = WarpFactor.impulseTravelTimeHours(distanceLy, throttle);

        return new TravelResult(
                from, to,
                distanceLy,
                timeHours,
                TravelMode.IMPULSE,
                throttle,
                WarpFactor.impulseVelocity(throttle)
        );
    }

    /**
     * Calculates the travel time for an impulse journey using the fleet's
     * MDrive speed.
     *
     * @param from the source galactic entry
     * @param to the destination galactic entry
     * @param fleet the fleet making the journey
     * @return a travel result with distance, time, and ETA
     */
    public TravelResult calculateImpulseTravel(GalacticEntry from, GalacticEntry to, Fleet fleet) {
        // Map MDrive speed to impulse throttle
        // MDrive 1-8 maps to 0.1-1.0 throttle range
        int mDrive = fleet.getFleetMDriveSpeed();
        double throttle = mDriveToImpulseThrottle(mDrive);

        // Apply health reduction
        double healthRatio = fleet.getFleetHealthRatio();
        throttle *= Math.max(0.1, healthRatio);

        return calculateImpulseTravel(from, to, throttle);
    }

    // ---- Within-System Travel ----

    /**
     * Calculates the travel time for movement within a solar system.
     * <p>
     * Within-system travel uses impulse power and covers much shorter distances
     * (astronomical units rather than light-years).
     * </p>
     *
     * @param distanceAu the distance in astronomical units
     * @param throttle the impulse throttle (0.0 to 1.0)
     * @return a travel result with distance and time
     */
    public TravelResult calculateWithinSystemTravel(double distanceAu, double throttle) {
        // 1 AU = 149,597,870.7 km
        double distanceKm = distanceAu * 149_597_870.7;
        double distanceLy = WarpFactor.kmToLightYears(distanceKm);
        double timeHours = WarpFactor.impulseTravelTimeHoursFromKm(distanceKm, throttle);

        return new TravelResult(
                null, null,
                distanceLy,
                timeHours,
                TravelMode.IMPULSE,
                throttle,
                WarpFactor.impulseVelocity(throttle)
        );
    }

    // ---- Warp Factor Mapping ----

    /**
     * Maps a JDrive speed (1-8) to a warp factor.
     * <p>
     * JDrive 1 → Warp 2.0 (minimum practical warp)
     * JDrive 8 → Warp 9.0 (high warp)
     * </p>
     *
     * @param jDriveSpeed the JDrive speed (1-8)
     * @return the corresponding warp factor
     */
    public static double jDriveToWarpFactor(int jDriveSpeed) {
        if (jDriveSpeed <= 0) {
            return 1.0;
        }
        // Linear mapping: JDrive 1-8 → Warp 2.0-9.0
        return 2.0 + (jDriveSpeed - 1) * (7.0 / 7.0);
    }

    /**
     * Maps an MDrive speed (1-8) to an impulse throttle.
     * <p>
     * MDrive 1 → 0.1 throttle (10% impulse)
     * MDrive 8 → 1.0 throttle (full impulse)
     * </p>
     *
     * @param mDriveSpeed the MDrive speed (1-8)
     * @return the corresponding impulse throttle (0.0-1.0)
     */
    public static double mDriveToImpulseThrottle(int mDriveSpeed) {
        if (mDriveSpeed <= 0) {
            return 0.0;
        }
        // Linear mapping: MDrive 1-8 → 0.1-1.0 throttle
        return 0.1 + (mDriveSpeed - 1) * (0.9 / 7.0);
    }

    // ---- Optimal Route ----

    /**
     * Calculates the optimal travel mode between two systems.
     * <p>
     * For distances under 5 light-years, impulse may be faster due to
     * warp drive spool-up time. For longer distances, warp is always optimal.
     * </p>
     *
     * @param from the source galactic entry
     * @param to the destination galactic entry
     * @param maxWarp the maximum warp factor available
     * @param maxImpulse the maximum impulse throttle available
     * @return the optimal travel result
     */
    public TravelResult calculateOptimalTravel(GalacticEntry from, GalacticEntry to,
                                               double maxWarp, double maxImpulse) {
        double distanceLy = distanceCalculator.lightYearsBetween(from, to);

        // For very short distances, impulse may be comparable
        if (distanceLy < 5.0) {
            TravelResult warp = calculateWarpTravel(from, to, maxWarp);
            TravelResult impulse = calculateImpulseTravel(from, to, maxImpulse);

            // Add warp spool-up time (5 minutes = 0.083 hours)
            double warpTimeWithSpool = warp.getTravelTimeHours() + 0.083;

            if (impulse.getTravelTimeHours() < warpTimeWithSpool) {
                return impulse;
            }
            return warp;
        }

        return calculateWarpTravel(from, to, maxWarp);
    }

    // ---- Travel Mode ----

    /**
     * The mode of travel.
     */
    public enum TravelMode {
        /** Faster-than-light warp travel. */
        WARP("Warp"),

        /** Sub-light impulse travel. */
        IMPULSE("Impulse");

        private final String displayName;

        TravelMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ---- Travel Result ----

    /**
     * The result of a travel calculation.
     * <p>
     * Contains the distance, travel time, mode, and ETA information.
     * </p>
     */
    public static class TravelResult {

        private final GalacticEntry from;
        private final GalacticEntry to;
        private final double distanceLightYears;
        private final double travelTimeHours;
        private final TravelMode mode;
        private final double speedFactor;
        private final double velocityC;
        private final String reason;

        private TravelResult(GalacticEntry from, GalacticEntry to,
                             double distanceLightYears, double travelTimeHours,
                             TravelMode mode, double speedFactor, double velocityC) {
            this(from, to, distanceLightYears, travelTimeHours, mode, speedFactor, velocityC, null);
        }

        private TravelResult(GalacticEntry from, GalacticEntry to,
                             double distanceLightYears, double travelTimeHours,
                             TravelMode mode, double speedFactor, double velocityC,
                             String reason) {
            this.from = from;
            this.to = to;
            this.distanceLightYears = distanceLightYears;
            this.travelTimeHours = travelTimeHours;
            this.mode = mode;
            this.speedFactor = speedFactor;
            this.velocityC = velocityC;
            this.reason = reason;
        }

        /**
         * Creates an inoperable travel result (e.g., warp drive damaged).
         */
        public static TravelResult inoperable(GalacticEntry from, GalacticEntry to,
                                              double distanceLightYears, String reason) {
            return new TravelResult(from, to, distanceLightYears, Double.POSITIVE_INFINITY,
                    TravelMode.WARP, 0.0, 0.0, reason);
        }

        /**
         * @return the source galactic entry, or null for within-system travel
         */
        public GalacticEntry getFrom() {
            return from;
        }

        /**
         * @return the destination galactic entry, or null for within-system travel
         */
        public GalacticEntry getTo() {
            return to;
        }

        /**
         * @return the distance in light-years
         */
        public double getDistanceLightYears() {
            return distanceLightYears;
        }

        /**
         * @return the travel time in hours
         */
        public double getTravelTimeHours() {
            return travelTimeHours;
        }

        /**
         * @return the travel time in minutes
         */
        public double getTravelTimeMinutes() {
            return travelTimeHours * 60;
        }

        /**
         * @return the travel time as a Duration
         */
        public Duration getTravelDuration() {
            if (travelTimeHours == Double.POSITIVE_INFINITY) {
                return Duration.ZERO;
            }
            long seconds = Math.round(travelTimeHours * 3600);
            return Duration.ofSeconds(seconds);
        }

        /**
         * @return the travel mode (WARP or IMPULSE)
         */
        public TravelMode getMode() {
            return mode;
        }

        /**
         * @return the speed factor (warp factor or impulse throttle)
         */
        public double getSpeedFactor() {
            return speedFactor;
        }

        /**
         * @return the velocity as a multiple of c
         */
        public double getVelocityC() {
            return velocityC;
        }

        /**
         * @return the reason string if travel is inoperable, or null
         */
        public String getReason() {
            return reason;
        }

        /**
         * @return true if the travel is inoperable (e.g., drive damaged)
         */
        public boolean isInoperable() {
            return reason != null;
        }

        /**
         * Calculates the ETA given a departure time.
         *
         * @param departure the departure time
         * @return the estimated time of arrival
         */
        public LocalDateTime calculateEta(LocalDateTime departure) {
            if (travelTimeHours == Double.POSITIVE_INFINITY) {
                return null;
            }
            long hours = (long) Math.floor(travelTimeHours);
            long minutes = Math.round((travelTimeHours - hours) * 60);
            return departure.plusHours(hours).plusMinutes(minutes);
        }

        /**
         * @return a formatted string describing the travel time
         */
        public String getEtaFormatted() {
            if (isInoperable()) {
                return "Inoperable: " + reason;
            }
            return WarpFactor.formatTravelTime(travelTimeHours);
        }

        /**
         * @return a formatted string describing the distance
         */
        public String getDistanceFormatted() {
            return StarSystemDistance.formatDistance(distanceLightYears);
        }

        /**
         * @return a formatted string describing the speed
         */
        public String getSpeedFormatted() {
            if (mode == TravelMode.WARP) {
                return String.format("Warp %.1f (%.0fc)", speedFactor, velocityC);
            } else {
                return String.format("Impulse %.0f%% (%.2fc)", speedFactor * 100, velocityC);
            }
        }

        @Override
        public String toString() {
            return String.format("TravelResult{%s → %s, %s, %s, %s, ETA: %s}",
                    from != null ? from.getSystem().getId() : "N/A",
                    to != null ? to.getSystem().getId() : "N/A",
                    getDistanceFormatted(),
                    getSpeedFormatted(),
                    mode.getDisplayName(),
                    getEtaFormatted());
        }
    }
}
