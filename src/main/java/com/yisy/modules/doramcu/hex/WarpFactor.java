package com.yisy.modules.doramcu.hex;

/**
 * Models Star Trek warp factors and their corresponding velocities.
 * <p>
 * Uses the TNG (The Next Generation) warp scale, the canonical formula from
 * the Star Trek TNG Technical Manual by Rick Sternbach and Michael Okuda:
 * </p>
 * <pre>
 * v = c × W^(10/3)    for W ≤ 9
 * </pre>
 * <p>
 * Where W is the warp factor and c is the speed of light (299,792,458 m/s).
 * For warp factors above 9, the curve asymptotically approaches infinity at
 * warp 10, which is considered unreachable.
 * </p>
 * <p>
 * Key reference speeds (TNG scale):
 * <ul>
 *   <li>Warp 1  = 1c (speed of light)</li>
 *   <li>Warp 2  = 10c</li>
 *   <li>Warp 3  = 39c</li>
 *   <li>Warp 4  = 102c</li>
 *   <li>Warp 5  = 214c (speed limit since TNG: "Force of Nature")</li>
 *   <li>Warp 6  = 392c (normal cruise speed of Federation ships)</li>
 *   <li>Warp 7  = 656c</li>
 *   <li>Warp 8  = 1,024c</li>
 *   <li>Warp 9  = 1,516c</li>
 *   <li>Warp 9.2 = 1,649c (max cruise speed of Galaxy class)</li>
 *   <li>Warp 9.6 = 1,909c (max safe speed of Galaxy class, 96 hours)</li>
 *   <li>Warp 9.9 = 3,053c</li>
 *   <li>Warp 9.975 = ~3,800c (max cruise speed of USS Voyager)</li>
 *   <li>Warp 9.99 = 7,912c</li>
 * </ul>
 * </p>
 * <p>
 * Impulse speed is sub-light: full impulse = 0.25c (74,770 km/s).
 * Impulse uses a throttle percentage from 0% to 100%.
 * </p>
 * <p>
 * The TOS (The Original Series) warp scale is also supported for reference:
 * <pre>
 * v = c × W³
 * </pre>
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * double velocity = WarpFactor.tngVelocity(6.0);  // 392.5c
 * double timeHours = WarpFactor.travelTimeLightYears(10.0, 6.0);  // ~0.087 hours for 10 ly
 * double impulseVelocity = WarpFactor.impulseVelocity(1.0);  // 0.25c (full impulse)
 * </pre>
 */
public final class WarpFactor {

    /** Speed of light in meters per second. */
    public static final double C_MPS = 299_792_458.0;

    /** Speed of light in kilometers per second. */
    public static final double C_KMS = 299_792.458;

    /** Speed of light in kilometers per hour. */
    public static final double C_KPH = C_KMS * 3_600;

    /** One light-year in kilometers. */
    public static final double LIGHT_YEAR_KM = C_KMS * 60 * 60 * 24 * 365.25;

    /** One light-year in meters. */
    public static final double LIGHT_YEAR_M = C_MPS * 60 * 60 * 24 * 365.25;

    /** One light-day in kilometers. */
    public static final double LIGHT_DAY_KM = C_KMS * 60 * 60 * 24;

    /** One light-hour in kilometers. */
    public static final double LIGHT_HOUR_KM = C_KMS * 60 * 60;

    /** One light-second in kilometers. */
    public static final double LIGHT_SECOND_KM = C_KMS;

    /** TNG warp exponent for W ≤ 9 (10/3 ≈ 3.333). */
    private static final double TNG_EXPONENT = 10.0 / 3.0;

    /** Maximum warp factor (approaches infinity at 10). */
    public static final double MAX_WARP_FACTOR = 9.9999;

    /** Minimum warp factor (warp 1 = speed of light). */
    public static final double MIN_WARP_FACTOR = 1.0;

    /** Full impulse as a fraction of light speed. */
    public static final double FULL_IMPULSE_FRACTION = 0.25;

    /** Full impulse speed in km/s. */
    public static final double FULL_IMPULSE_KMS = FULL_IMPULSE_FRACTION * C_KMS;

    /** Full impulse speed in km/h. */
    public static final double FULL_IMPULSE_KPH = FULL_IMPULSE_FRACTION * C_KPH;

    /** Normal cruise warp factor for Federation ships. */
    public static final double NORMAL_CRUISE_WARP = 6.0;

    /** Maximum sustained cruise warp for Galaxy-class ships. */
    public static final double MAX_CRUISE_WARP = 9.2;

    /** Maximum safe warp for Galaxy-class ships (96 hours). */
    public static final double MAX_SAFE_WARP = 9.6;

    /** Maximum cruise warp for Intrepid-class (Voyager). */
    public static final double VOYAGER_MAX_WARP = 9.975;

    /** Speed limit established in TNG: "Force of Nature". */
    public static final double SPEED_LIMIT_WARP = 5.0;

    private WarpFactor() {
        // Utility class
    }

    // ---- TNG Warp Scale ----

    /**
     * Calculates the velocity in multiples of c for a given warp factor using the TNG formula.
     * <p>
     * For W ≤ 9: v = W^(10/3) × c
     * For W > 9: uses an empirical curve that approaches infinity as W → 10.
     * </p>
     *
     * @param warpFactor the warp factor (1.0 to 9.9999)
     * @return the velocity as a multiple of the speed of light (e.g., 392.5 for warp 6)
     * @throws IllegalArgumentException if warp factor is out of range
     */
    public static double tngVelocity(double warpFactor) {
        if (warpFactor < MIN_WARP_FACTOR) {
            throw new IllegalArgumentException("Warp factor must be >= " + MIN_WARP_FACTOR + ": " + warpFactor);
        }
        if (warpFactor >= 10.0) {
            throw new IllegalArgumentException("Warp factor must be < 10.0: " + warpFactor);
        }

        if (warpFactor <= 9.0) {
            return Math.pow(warpFactor, TNG_EXPONENT);
        }

        // For W > 9, use empirical formula from Star Trek Encyclopedia
        // This approximates the hand-drawn curve by Michael Okuda
        double w = warpFactor;
        double velocity;

        if (w <= 9.2) {
            velocity = 1649.0;
        } else if (w <= 9.6) {
            // Interpolate between 9.2 (1649c) and 9.6 (1909c)
            double t = (w - 9.2) / (9.6 - 9.2);
            velocity = 1649.0 + t * (1909.0 - 1649.0);
        } else if (w <= 9.9) {
            // Interpolate between 9.6 (1909c) and 9.9 (3053c)
            double t = (w - 9.6) / (9.9 - 9.6);
            velocity = 1909.0 + t * (3053.0 - 1909.0);
        } else if (w <= 9.99) {
            // Interpolate between 9.9 (3053c) and 9.99 (7912c)
            double t = (w - 9.9) / (9.99 - 9.9);
            velocity = 3053.0 + t * (7912.0 - 3053.0);
        } else {
            // Beyond 9.99, exponential approach to infinity
            double t = (w - 9.99) / (10.0 - 9.99);
            velocity = 7912.0 * Math.pow(10.0, t);
        }

        return velocity;
    }

    /**
     * Calculates the warp factor needed to achieve a given velocity (in multiples of c).
     * <p>
     * Inverse of {@link #tngVelocity(double)} for W ≤ 9.
     * </p>
     *
     * @param velocityC the desired velocity as a multiple of c
     * @return the warp factor
     * @throws IllegalArgumentException if velocity is less than 1c
     */
    public static double warpFactorFromVelocity(double velocityC) {
        if (velocityC < 1.0) {
            throw new IllegalArgumentException("Velocity must be >= 1c: " + velocityC);
        }
        return Math.pow(velocityC, 1.0 / TNG_EXPONENT);
    }

    // ---- TOS Warp Scale (for reference) ----

    /**
     * Calculates the velocity in multiples of c for a given warp factor using the TOS formula.
     * <p>
     * v = W³ × c
     * </p>
     *
     * @param warpFactor the warp factor
     * @return the velocity as a multiple of the speed of light
     */
    public static double tosVelocity(double warpFactor) {
        return Math.pow(warpFactor, 3.0);
    }

    // ---- Impulse Speed ----

    /**
     * Calculates the impulse velocity as a multiple of c for a given throttle setting.
     * <p>
     * Full impulse (100%) = 0.25c. Half impulse (50%) = 0.125c, etc.
     * </p>
     *
     * @param throttle the throttle setting from 0.0 to 1.0 (0% to 100%)
     * @return the velocity as a multiple of c
     * @throws IllegalArgumentException if throttle is out of range
     */
    public static double impulseVelocity(double throttle) {
        if (throttle < 0.0 || throttle > 1.0) {
            throw new IllegalArgumentException("Impulse throttle must be between 0.0 and 1.0: " + throttle);
        }
        return FULL_IMPULSE_FRACTION * throttle;
    }

    /**
     * Returns the impulse velocity in km/s for a given throttle setting.
     *
     * @param throttle the throttle setting from 0.0 to 1.0
     * @return the velocity in km/s
     */
    public static double impulseVelocityKms(double throttle) {
        return impulseVelocity(throttle) * C_KMS;
    }

    // ---- Travel Time Calculations ----

    /**
     * Calculates the travel time in hours for a given distance in light-years at a given warp factor.
     *
     * @param distanceLightYears the distance in light-years
     * @param warpFactor the warp factor
     * @return the travel time in hours
     */
    public static double travelTimeHours(double distanceLightYears, double warpFactor) {
        if (distanceLightYears < 0) {
            throw new IllegalArgumentException("Distance must be non-negative: " + distanceLightYears);
        }
        double velocityC = tngVelocity(warpFactor);
        // Time in years = distance / velocity
        double timeYears = distanceLightYears / velocityC;
        return timeYears * 365.25 * 24;
    }

    /**
     * Calculates the travel time in minutes for a given distance in light-years at a given warp factor.
     *
     * @param distanceLightYears the distance in light-years
     * @param warpFactor the warp factor
     * @return the travel time in minutes
     */
    public static double travelTimeMinutes(double distanceLightYears, double warpFactor) {
        return travelTimeHours(distanceLightYears, warpFactor) * 60;
    }

    /**
     * Calculates the travel time in seconds for a given distance in light-years at a given warp factor.
     *
     * @param distanceLightYears the distance in light-years
     * @param warpFactor the warp factor
     * @return the travel time in seconds
     */
    public static double travelTimeSeconds(double distanceLightYears, double warpFactor) {
        return travelTimeHours(distanceLightYears, warpFactor) * 3600;
    }

    /**
     * Calculates the travel time in hours for a given distance in light-years at a given impulse throttle.
     * <p>
     * Impulse is sub-light, so interstellar travel takes a long time.
     * For example, at full impulse (0.25c), traveling 1 light-year takes ~4 years.
     * </p>
     *
     * @param distanceLightYears the distance in light-years
     * @param throttle the impulse throttle from 0.0 to 1.0
     * @return the travel time in hours
     */
    public static double impulseTravelTimeHours(double distanceLightYears, double throttle) {
        if (distanceLightYears < 0) {
            throw new IllegalArgumentException("Distance must be non-negative: " + distanceLightYears);
        }
        double velocityC = impulseVelocity(throttle);
        if (velocityC <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        double timeYears = distanceLightYears / velocityC;
        return timeYears * 365.25 * 24;
    }

    /**
     * Calculates the travel time in hours for a given distance in kilometers at a given warp factor.
     *
     * @param distanceKm the distance in kilometers
     * @param warpFactor the warp factor
     * @return the travel time in hours
     */
    public static double travelTimeHoursFromKm(double distanceKm, double warpFactor) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance must be non-negative: " + distanceKm);
        }
        double velocityC = tngVelocity(warpFactor);
        double velocityKph = velocityC * C_KPH;
        return distanceKm / velocityKph;
    }

    /**
     * Calculates the travel time in hours for a given distance in kilometers at impulse.
     *
     * @param distanceKm the distance in kilometers
     * @param throttle the impulse throttle from 0.0 to 1.0
     * @return the travel time in hours
     */
    public static double impulseTravelTimeHoursFromKm(double distanceKm, double throttle) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance must be non-negative: " + distanceKm);
        }
        double velocityKph = impulseVelocity(throttle) * C_KPH;
        if (velocityKph <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return distanceKm / velocityKph;
    }

    // ---- Distance Calculations ----

    /**
     * Converts a distance in light-years to kilometers.
     *
     * @param lightYears the distance in light-years
     * @return the distance in kilometers
     */
    public static double lightYearsToKm(double lightYears) {
        return lightYears * LIGHT_YEAR_KM;
    }

    /**
     * Converts a distance in kilometers to light-years.
     *
     * @param km the distance in kilometers
     * @return the distance in light-years
     */
    public static double kmToLightYears(double km) {
        return km / LIGHT_YEAR_KM;
    }

    /**
     * Converts a distance in light-years to light-days.
     *
     * @param lightYears the distance in light-years
     * @return the distance in light-days
     */
    public static double lightYearsToLightDays(double lightYears) {
        return lightYears * 365.25;
    }

    /**
     * Converts a distance in light-years to light-hours.
     *
     * @param lightYears the distance in light-years
     * @return the distance in light-hours
     */
    public static double lightYearsToLightHours(double lightYears) {
        return lightYears * 365.25 * 24;
    }

    // ---- Warp Factor Presets ----

    /**
     * Returns a descriptive name for common warp factors.
     *
     * @param warpFactor the warp factor
     * @return a descriptive string (e.g., "Normal Cruise", "Maximum Safe")
     */
    public static String warpFactorDescription(double warpFactor) {
        if (warpFactor <= 1.0) {
            return "Light Speed";
        } else if (warpFactor <= 2.0) {
            return "Low Warp";
        } else if (warpFactor <= 4.0) {
            return "Sub-Cruise Warp";
        } else if (warpFactor <= SPEED_LIMIT_WARP) {
            return "Speed Limit Warp";
        } else if (warpFactor <= NORMAL_CRUISE_WARP) {
            return "Normal Cruise";
        } else if (warpFactor <= 8.0) {
            return "High Warp";
        } else if (warpFactor <= MAX_CRUISE_WARP) {
            return "Maximum Cruise";
        } else if (warpFactor <= MAX_SAFE_WARP) {
            return "Maximum Safe";
        } else if (warpFactor <= VOYAGER_MAX_WARP) {
            return "Emergency Warp";
        } else {
            return "Extreme Warp";
        }
    }

    /**
     * Formats a travel time into a human-readable string.
     *
     * @param hours the travel time in hours
     * @return a formatted string (e.g., "2 days, 5 hours, 30 minutes")
     */
    public static String formatTravelTime(double hours) {
        if (hours <= 0) {
            return "Instantaneous";
        }
        if (hours >= 8766.0) {
            // More than a year
            double years = hours / 8766.0;
            if (years >= 10) {
                return String.format("%.1f years", years);
            }
            return String.format("%.2f years", years);
        }
        if (hours >= 24.0) {
            // Days
            long days = (long) (hours / 24.0);
            long remainingHours = (long) ((hours % 24.0));
            if (days >= 365) {
                return String.format("%d years, %d days", days / 365, days % 365);
            }
            if (remainingHours > 0) {
                return String.format("%d day%s, %d hour%s", days, days == 1 ? "" : "s", remainingHours, remainingHours == 1 ? "" : "s");
            }
            return String.format("%d day%s", days, days == 1 ? "" : "s");
        }
        if (hours >= 1.0) {
            // Hours and minutes
            long h = (long) hours;
            long m = Math.round((hours - h) * 60);
            if (m > 0) {
                return String.format("%d hour%s, %d minute%s", h, h == 1 ? "" : "s", m, m == 1 ? "" : "s");
            }
            return String.format("%d hour%s", h, h == 1 ? "" : "s");
        }
        // Minutes and seconds
        long m = Math.round(hours * 60);
        if (m >= 1) {
            long s = Math.round((hours * 60 - m) * 60);
            if (s > 0) {
                return String.format("%d minute%s, %d second%s", m, m == 1 ? "" : "s", s, s == 1 ? "" : "s");
            }
            return String.format("%d minute%s", m, m == 1 ? "" : "s");
        }
        long s = Math.round(hours * 3600);
        return String.format("%d second%s", s, s == 1 ? "" : "s");
    }
}
