package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Objects;

/**
 * Exploration activities within a star system from Classic Traveller Book 2.
 * <p>
 * When a ship explores a star system, it can perform various activities:
 * survey celestial bodies, discover resources, detect hazards, map routes,
 * and establish beacons for future navigation.
 * </p>
 * <p>
 * Each activity consumes time (weeks) and may require specific ship equipment
 * or crew skills. Activities have success probabilities based on the ship's
 * capabilities and the system's characteristics.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   Survey Planet A: 2 weeks, 80% success → discovered habitable world
 *   Resource Scan: 1 week → found mineral deposits (Ag trade code)
 *   Hazard Detection: 1 week → clear of hazards
 * </pre>
 */
public class ExplorationActivity {

    /**
     * Types of exploration activities.
     */
    public enum ActivityType {
        /**
         * Survey a celestial body to determine its characteristics.
         */
        SURVEY_BODY,

        /**
         * Scan for resources (minerals, gases, water).
         */
        RESOURCE_SCAN,

        /**
         * Detect hazards (asteroid fields, radiation, gravitational anomalies).
         */
        HAZARD_DETECTION,

        /**
         * Map safe routes between celestial bodies.
         */
        ROUTE_MAPPING,

        /**
         * Establish a navigation beacon for future reference.
         */
        BEACON_DEPLOYMENT,

        /**
         * Full system survey — comprehensive exploration of the entire system.
         */
        FULL_SYSTEM_SURVEY
    }

    /**
     * Possible discoveries during exploration.
     */
    public enum Discovery {
        /**
         * No significant findings.
         */
        NONE,

        /**
         * Habitable world discovered.
         */
        HABITABLE_WORLD,

        /**
         * Mineral deposits found.
         */
        MINERAL_DEPOSITS,

        /**
         * Gas giant with valuable isotopes.
         */
        GAS_ISOTOPES,

        /**
         * Asteroid belt with mining potential.
         */
        ASTEROID_BELT,

        /**
         * Water ice deposits.
         */
        WATER_ICE,

        /**
         * Ancient ruins or artifacts.
         */
        ANCIENT_ARTIFACTS,

        /**
         * Hazardous phenomenon (radiation, gravitational anomaly).
         */
        HAZARDOUS_PHENOMENON,

        /**
         * Unknown signal or phenomenon.
         */
        UNKNOWN_SIGNAL,

        /**
         * Another ship or station.
         */
        FOREIGN_PRESENCE
    }

    /**
     * The type of activity.
     */
    private final ActivityType type;

    /**
     * The target description (e.g., planet name or "entire system").
     */
    private final String target;

    /**
     * The star system where the activity takes place.
     */
    private final TravellerWorld starSystem;

    /**
     * Time required in weeks.
     */
    private final int weeksRequired;

    /**
     * Success probability (0-100%).
     */
    private final int successProbability;

    /**
     * True if the activity was successful.
     */
    private final boolean successful;

    /**
     * The discovery made, if any.
     */
    private final Discovery discovery;

    /**
     * Additional notes about the activity.
     */
    private final String notes;

    /**
     * Creates a new exploration activity.
     *
     * @param type the activity type
     * @param target the target description
     * @param starSystem the star system
     * @param weeksRequired time required in weeks
     * @param successProbability success probability (0-100)
     */
    public ExplorationActivity(ActivityType type, String target, TravellerWorld starSystem,
                                int weeksRequired, int successProbability) {
        this.type = Objects.requireNonNull(type);
        this.target = Objects.requireNonNull(target);
        this.starSystem = Objects.requireNonNull(starSystem);
        this.weeksRequired = weeksRequired;
        this.successProbability = Math.max(0, Math.min(100, successProbability));
        this.successful = false;
        this.discovery = Discovery.NONE;
        this.notes = "";
    }

    /**
     * Creates a completed exploration activity.
     *
     * @param type the activity type
     * @param target the target description
     * @param starSystem the star system
     * @param weeksRequired time required
     * @param successful whether the activity succeeded
     * @param discovery the discovery made
     * @param notes additional notes
     */
    public ExplorationActivity(ActivityType type, String target, TravellerWorld starSystem,
                                int weeksRequired, boolean successful, Discovery discovery,
                                String notes) {
        this.type = Objects.requireNonNull(type);
        this.target = Objects.requireNonNull(target);
        this.starSystem = Objects.requireNonNull(starSystem);
        this.weeksRequired = weeksRequired;
        this.successProbability = 0;
        this.successful = successful;
        this.discovery = discovery;
        this.notes = notes != null ? notes : "";
    }

    /**
     * Executes the exploration activity with the given random seed.
     * <p>
     * Returns a new ExplorationActivity with the results.
     * </p>
     *
     * @param random the random number generator
     * @return a new completed exploration activity
     */
    public ExplorationActivity execute(Random random) {
        boolean success = random.nextInt(100) < successProbability;
        Discovery disc = success ? rollDiscovery(random, type) : Discovery.NONE;
        String note = generateNotes(success, disc);
        return new ExplorationActivity(type, target, starSystem, weeksRequired, success, disc, note);
    }

    /**
     * Rolls for a discovery based on the activity type.
     */
    private Discovery rollDiscovery(Random random, ActivityType activityType) {
        int roll = random.nextInt(100);

        if (activityType == ActivityType.RESOURCE_SCAN) {
            if (roll < 40) return Discovery.MINERAL_DEPOSITS;
            if (roll < 60) return Discovery.WATER_ICE;
            if (roll < 75) return Discovery.GAS_ISOTOPES;
            if (roll < 85) return Discovery.ASTEROID_BELT;
            return Discovery.NONE;
        }

        if (activityType == ActivityType.SURVEY_BODY) {
            if (roll < 30) return Discovery.HABITABLE_WORLD;
            if (roll < 50) return Discovery.MINERAL_DEPOSITS;
            if (roll < 65) return Discovery.WATER_ICE;
            if (roll < 75) return Discovery.ANCIENT_ARTIFACTS;
            if (roll < 85) return Discovery.HAZARDOUS_PHENOMENON;
            return Discovery.NONE;
        }

        if (activityType == ActivityType.HAZARD_DETECTION) {
            if (roll < 20) return Discovery.HAZARDOUS_PHENOMENON;
            if (roll < 30) return Discovery.ASTEROID_BELT;
            if (roll < 40) return Discovery.FOREIGN_PRESENCE;
            return Discovery.NONE; // Clear
        }

        if (activityType == ActivityType.FULL_SYSTEM_SURVEY) {
            if (roll < 15) return Discovery.UNKNOWN_SIGNAL;
            if (roll < 30) return Discovery.ANCIENT_ARTIFACTS;
            if (roll < 50) return Discovery.HABITABLE_WORLD;
            if (roll < 70) return Discovery.MINERAL_DEPOSITS;
            if (roll < 85) return Discovery.FOREIGN_PRESENCE;
            return Discovery.NONE;
        }

        // Default: low chance of anything
        if (roll < 10) return Discovery.UNKNOWN_SIGNAL;
        if (roll < 20) return Discovery.FOREIGN_PRESENCE;
        return Discovery.NONE;
    }

    /**
     * Generates notes for the activity result.
     */
    private String generateNotes(boolean success, Discovery discovery) {
        if (!success) {
            return "Activity failed — insufficient data collected.";
        }

        if (discovery == Discovery.NONE) {
            return "Activity completed — no significant findings.";
        }

        switch (discovery) {
            case HABITABLE_WORLD:
                return "Discovered potentially habitable world requiring further survey.";
            case MINERAL_DEPOSITS:
                return "Significant mineral deposits detected — mining recommended.";
            case GAS_ISOTOPES:
                return "Valuable helium-3 isotopes detected in atmosphere.";
            case ASTEROID_BELT:
                return "Asteroid belt with mining potential identified.";
            case WATER_ICE:
                return "Water ice deposits found — refueling possible.";
            case ANCIENT_ARTIFACTS:
                return "Ancient structures or artifacts detected — archaeological interest.";
            case HAZARDOUS_PHENOMENON:
                return "Hazardous phenomenon detected — avoid area.";
            case UNKNOWN_SIGNAL:
                return "Unknown signal source detected — investigation recommended.";
            case FOREIGN_PRESENCE:
                return "Foreign ship or station detected — caution advised.";
            default:
                return "Activity completed successfully.";
        }
    }

    // ---- Accessors ----

    /**
     * @return the activity type
     */
    public ActivityType getType() {
        return type;
    }

    /**
     * @return the target description
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return the star system
     */
    public TravellerWorld getStarSystem() {
        return starSystem;
    }

    /**
     * @return the time required in weeks
     */
    public int getWeeksRequired() {
        return weeksRequired;
    }

    /**
     * @return the success probability (0-100)
     */
    public int getSuccessProbability() {
        return successProbability;
    }

    /**
     * @return true if the activity was successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @return the discovery made
     */
    public Discovery getDiscovery() {
        return discovery;
    }

    /**
     * @return additional notes
     */
    public String getNotes() {
        return notes;
    }

    // ---- Factory Methods ----

    /**
     * Creates a survey activity for a celestial body.
     *
     * @param bodyName the name of the celestial body
     * @param starSystem the star system
     * @param hasComputer true if the ship has a computer (increases success)
     * @return a new exploration activity
     */
    public static ExplorationActivity createSurvey(String bodyName, TravellerWorld starSystem,
                                                    boolean hasComputer) {
        int probability = hasComputer ? 80 : 60;
        return new ExplorationActivity(ActivityType.SURVEY_BODY, bodyName, starSystem, 2, probability);
    }

    /**
     * Creates a resource scan activity.
     *
     * @param targetArea the area to scan
     * @param starSystem the star system
     * @param hasComputer true if the ship has a computer
     * @return a new exploration activity
     */
    public static ExplorationActivity createResourceScan(String targetArea, TravellerWorld starSystem,
                                                          boolean hasComputer) {
        int probability = hasComputer ? 75 : 55;
        return new ExplorationActivity(ActivityType.RESOURCE_SCAN, targetArea, starSystem, 1, probability);
    }

    /**
     * Creates a hazard detection activity.
     *
     * @param targetArea the area to scan
     * @param starSystem the star system
     * @param hasComputer true if the ship has a computer
     * @return a new exploration activity
     */
    public static ExplorationActivity createHazardDetection(String targetArea, TravellerWorld starSystem,
                                                             boolean hasComputer) {
        int probability = hasComputer ? 85 : 65;
        return new ExplorationActivity(ActivityType.HAZARD_DETECTION, targetArea, starSystem, 1, probability);
    }

    /**
     * Creates a full system survey activity.
     *
     * @param starSystem the star system to survey
     * @param hasComputer true if the ship has a computer
     * @return a new exploration activity
     */
    public static ExplorationActivity createFullSystemSurvey(TravellerWorld starSystem,
                                                              boolean hasComputer) {
        int probability = hasComputer ? 70 : 45;
        return new ExplorationActivity(
                ActivityType.FULL_SYSTEM_SURVEY,
                starSystem.getName() + " System",
                starSystem,
                4,
                probability
        );
    }

    /**
     * Creates a route mapping activity between two points.
     *
     * @param from the starting point
     * @param to the destination
     * @param starSystem the star system
     * @return a new exploration activity
     */
    public static ExplorationActivity createRouteMapping(String from, String to, TravellerWorld starSystem) {
        return new ExplorationActivity(ActivityType.ROUTE_MAPPING, from + " → " + to, starSystem, 1, 90);
    }

    /**
     * Creates a beacon deployment activity.
     *
     * @param location the beacon location
     * @param starSystem the star system
     * @return a new exploration activity
     */
    public static ExplorationActivity createBeaconDeployment(String location, TravellerWorld starSystem) {
        return new ExplorationActivity(ActivityType.BEACON_DEPLOYMENT, location, starSystem, 1, 95);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(": ").append(target)
                .append(" (").append(weeksRequired).append(" weeks)");

        if (successful) {
            sb.append(" ✓");
            if (discovery != Discovery.NONE) {
                sb.append(" [").append(discovery).append("]");
            }
        } else if (successful || discovery != Discovery.NONE) {
            sb.append(" ✗");
        }

        if (!notes.isEmpty()) {
            sb.append(" — ").append(notes);
        }

        return sb.toString();
    }
}
