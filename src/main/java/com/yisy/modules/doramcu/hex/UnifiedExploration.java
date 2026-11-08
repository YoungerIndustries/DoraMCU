package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.*;

import java.util.*;

/**
 * A unified exploration system that combines DoraMCU's MiningOperation
 * with Traveller's ExplorationActivity.
 * <p>
 * This class merges DoraMCU's resource extraction with Traveller's
 * survey, resource scanning, hazard detection, and beacon deployment.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Exploration activities: survey, resource scan, hazard detection (from Traveller)</li>
 *   <li>Resource extraction from celestial bodies (from DoraMCU)</li>
 *   <li>Discovery system with trade code effects</li>
 *   <li>Beacon deployment for navigation</li>
 *   <li>Link between exploration and mining operations</li>
 * </ul>
 */
public class UnifiedExploration {

    private final CelestialBody celestialBody;
    private final TravellerWorld world;
    private final List<ExplorationActivity> activities;
    private final List<MiningOperation> miningOperations;
    private final Set<ExplorationActivity.Discovery> discoveries;
    private boolean hasBeacon;
    private boolean hazardsDetected;
    private final List<String> eventLog;
    private final Random random;

    /**
     * Creates a new unified exploration for the given celestial body.
     *
     * @param celestialBody the celestial body being explored
     * @param world the Traveller world (may be null for non-planet bodies)
     */
    public UnifiedExploration(CelestialBody celestialBody, TravellerWorld world) {
        this.celestialBody = Objects.requireNonNull(celestialBody, "Celestial body must not be null");
        this.world = world;
        this.activities = new ArrayList<>();
        this.miningOperations = new ArrayList<>();
        this.discoveries = new HashSet<>();
        this.hasBeacon = false;
        this.hazardsDetected = false;
        this.eventLog = new ArrayList<>();
        this.random = new Random();
    }

    // ---- Accessors ----

    /**
     * Returns the celestial body being explored.
     */
    public CelestialBody getCelestialBody() {
        return celestialBody;
    }

    /**
     * Returns the Traveller world, or null if not a planet.
     */
    public TravellerWorld getWorld() {
        return world;
    }

    /**
     * Returns the list of exploration activities (immutable).
     */
    public List<ExplorationActivity> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    /**
     * Returns the list of mining operations (immutable).
     */
    public List<MiningOperation> getMiningOperations() {
        return Collections.unmodifiableList(miningOperations);
    }

    /**
     * Returns the set of discoveries made (immutable).
     */
    public Set<ExplorationActivity.Discovery> getDiscoveries() {
        return Collections.unmodifiableSet(discoveries);
    }

    /**
     * Checks if a beacon has been deployed.
     */
    public boolean hasBeacon() {
        return hasBeacon;
    }

    /**
     * Checks if hazards have been detected.
     */
    public boolean areHazardsDetected() {
        return hazardsDetected;
    }

    /**
     * Returns the event log (immutable).
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    // ---- Exploration Activities ----

    /**
     * Performs a survey of the celestial body.
     *
     * @param hasComputer true if the exploring ship has a computer
     * @return the exploration activity result
     */
    public ExplorationActivity surveyBody(boolean hasComputer) {
        TravellerWorld targetWorld = world != null ? world :
                new TravellerWorld(0, celestialBody.getName(),
                        new UniversalWorldProfile(4, 3, 4, 2, 5, 5, 5, "", ""), 0, 0);

        ExplorationActivity activity = ExplorationActivity.createSurvey(
                celestialBody.getName(), targetWorld, hasComputer);
        ExplorationActivity result = activity.execute(random);
        activities.add(result);

        if (result.isSuccessful()) {
            discoveries.add(result.getDiscovery());
            logEvent("Survey: " + result);
        }

        return result;
    }

    /**
     * Performs a resource scan of the area.
     *
     * @param hasComputer true if the exploring ship has a computer
     * @return the exploration activity result
     */
    public ExplorationActivity resourceScan(boolean hasComputer) {
        TravellerWorld targetWorld = world != null ? world :
                new TravellerWorld(0, celestialBody.getName(),
                        new UniversalWorldProfile(4, 3, 4, 2, 5, 5, 5, "", ""), 0, 0);

        ExplorationActivity activity = ExplorationActivity.createResourceScan(
                celestialBody.getName(), targetWorld, hasComputer);
        ExplorationActivity result = activity.execute(random);
        activities.add(result);

        if (result.isSuccessful()) {
            discoveries.add(result.getDiscovery());
            logEvent("Resource scan: " + result);

            // If minerals found, create mining operation
            if (result.getDiscovery() == ExplorationActivity.Discovery.MINERAL_DEPOSITS &&
                    world != null) {
                createMiningOperation();
            }
        }

        return result;
    }

    /**
     * Performs hazard detection.
     *
     * @param hasComputer true if the exploring ship has a computer
     * @return the exploration activity result
     */
    public ExplorationActivity hazardDetection(boolean hasComputer) {
        TravellerWorld targetWorld = world != null ? world :
                new TravellerWorld(0, celestialBody.getName(),
                        new UniversalWorldProfile(4, 3, 4, 2, 5, 5, 5, "", ""), 0, 0);

        ExplorationActivity activity = ExplorationActivity.createHazardDetection(
                celestialBody.getName(), targetWorld, hasComputer);
        ExplorationActivity result = activity.execute(random);
        activities.add(result);

        if (result.isSuccessful()) {
            discoveries.add(result.getDiscovery());
            hazardsDetected = true;
            logEvent("Hazard detection: " + result);
        }

        return result;
    }

    /**
     * Performs a full system survey.
     *
     * @param hasComputer true if the exploring ship has a computer
     * @return the exploration activity result
     */
    public ExplorationActivity fullSystemSurvey(boolean hasComputer) {
        if (world == null) {
            logEvent("Cannot survey system — no world data");
            return null;
        }

        ExplorationActivity activity = ExplorationActivity.createFullSystemSurvey(
                world, hasComputer);
        ExplorationActivity result = activity.execute(random);
        activities.add(result);

        if (result.isSuccessful()) {
            discoveries.add(result.getDiscovery());
            logEvent("Full system survey: " + result);
        }

        return result;
    }

    /**
     * Deploys a navigation beacon.
     *
     * @return true if beacon was deployed
     */
    public boolean deployBeacon() {
        if (world == null) {
            logEvent("Cannot deploy beacon — no world data");
            return false;
        }

        ExplorationActivity activity = ExplorationActivity.createBeaconDeployment(
                celestialBody.getName(), world);
        ExplorationActivity result = activity.execute(random);
        activities.add(result);

        if (result.isSuccessful()) {
            hasBeacon = true;
            logEvent("Beacon deployed at " + celestialBody.getName());
        }

        return result.isSuccessful();
    }

    /**
     * Maps a route between two points.
     *
     * @param from the starting point
     * @param to the destination
     * @return the exploration activity result
     */
    public ExplorationActivity mapRoute(String from, String to) {
        if (world == null) {
            logEvent("Cannot map route — no world data");
            return null;
        }

        ExplorationActivity activity = ExplorationActivity.createRouteMapping(
                from, to, world);
        ExplorationActivity result = activity.execute(random);
        activities.add(result);

        if (result.isSuccessful()) {
            logEvent("Route mapped: " + from + " -> " + to);
        }

        return result;
    }

    // ---- Mining Integration ----

    /**
     * Creates a mining operation based on exploration findings.
     */
    public void createMiningOperation() {
        if (world == null || !(celestialBody instanceof Planet)) {
            logEvent("Cannot create mining operation — invalid celestial body");
            return;
        }

        MiningOperation operation = new MiningOperation(celestialBody, 0);
        miningOperations.add(operation);
        logEvent("Mining operation created at " + celestialBody.getName());
    }

    /**
     * Processes one turn of all mining operations.
     *
     * @return the total resources mined
     */
    public int processMiningTurn() {
        int totalMined = 0;
        for (MiningOperation operation : miningOperations) {
            totalMined += operation.processTurn();
        }
        return totalMined;
    }

    /**
     * Checks if the celestial body is habitable based on discoveries.
     */
    public boolean isHabitable() {
        return discoveries.contains(ExplorationActivity.Discovery.HABITABLE_WORLD);
    }

    /**
     * Checks if mineral deposits were found.
     */
    public boolean hasMineralDeposits() {
        return discoveries.contains(ExplorationActivity.Discovery.MINERAL_DEPOSITS);
    }

    /**
     * Checks if water ice was found.
     */
    public boolean hasWaterIce() {
        return discoveries.contains(ExplorationActivity.Discovery.WATER_ICE);
    }

    /**
     * Returns a summary of the exploration.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Exploration of ").append(celestialBody.getName()).append("\n");
        sb.append("Activities: ").append(activities.size()).append("\n");
        sb.append("Discoveries: ").append(discoveries).append("\n");
        sb.append("Beacon: ").append(hasBeacon).append("\n");
        sb.append("Hazards detected: ").append(hazardsDetected).append("\n");
        sb.append("Mining operations: ").append(miningOperations.size());
        return sb.toString();
    }

    private void logEvent(String event) {
        eventLog.add(event);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
