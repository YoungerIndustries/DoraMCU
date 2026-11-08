package com.yisy.modules.doramcu.captainslog.generation;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.GalacticEntry;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.StarSystemDistance;
import com.yisy.modules.doramcu.hex.TravelCalculator;
import com.yisy.modules.doramcu.hex.TravelOrder;
import com.yisy.modules.doramcu.hex.WarpFactor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages inter-system travel for the Captain's Log game mode.
 * <p>
 * This class orchestrates fleet movement between star systems using Star Trek
 * warp and impulse travel mechanics. It manages active travel orders, validates
 * travel requests, and tracks fleet positions.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Warp travel between systems with TNG warp factor calculations</li>
 *   <li>Impulse travel for within-system and short-range movement</li>
 *   <li>Travel order management with progress tracking</li>
 *   <li>Fleet health checks before warp travel</li>
 *   <li>Quadrant-aware distance estimation</li>
 * </ul>
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * CaptainsLogTravelManager manager = new CaptainsLogTravelManager(galaxy);
 *
 * // Request warp travel
 * TravelOrder order = manager.requestWarpTravel(
 *     fleet, fromEntry, toEntry, 6.0
 * );
 *
 * // Depart on the journey
 * order.depart();
 *
 * // Later: update all active travel orders
 * manager.updateAllTravelOrders(LocalDateTime.now());
 *
 * // Check active orders
 * List<TravelOrder> active = manager.getActiveTravelOrders();
 * </pre>
 */
public class CaptainsLogTravelManager {

    private final Galaxy galaxy;
    private final TravelCalculator calculator;
    private final StarSystemDistance distanceCalculator;
    private final Map<Integer, TravelOrder> activeOrders;
    private final Map<Integer, TravelOrder> completedOrders;

    /** Maximum number of active travel orders per fleet. */
    public static final int MAX_ACTIVE_ORDERS_PER_FLEET = 1;

    /** Minimum fleet health ratio for warp travel. */
    public static final double MIN_WARP_HEALTH_RATIO = 0.2;

    /** Warp core spool-up time in hours (5 minutes). */
    public static final double WARP_SPOOL_UP_HOURS = 5.0 / 60.0;

    /**
     * Creates a new travel manager for the given galaxy.
     *
     * @param galaxy the galaxy containing the systems
     */
    public CaptainsLogTravelManager(Galaxy galaxy) {
        this(galaxy, new TravelCalculator());
    }

    /**
     * Creates a new travel manager with a custom travel calculator.
     *
     * @param galaxy the galaxy containing the systems
     * @param calculator the travel calculator to use
     */
    public CaptainsLogTravelManager(Galaxy galaxy, TravelCalculator calculator) {
        this.galaxy = Objects.requireNonNull(galaxy, "Galaxy must not be null");
        this.calculator = Objects.requireNonNull(calculator, "Calculator must not be null");
        this.distanceCalculator = new StarSystemDistance();
        this.activeOrders = new ConcurrentHashMap<>();
        this.completedOrders = new ConcurrentHashMap<>();
    }

    // ---- Travel Requests ----

    /**
     * Requests a warp travel order between two systems.
     * <p>
     * Validates that the fleet can perform warp travel and creates a travel order.
     * </p>
     *
     * @param fleet the fleet to travel
     * @param from the origin system
     * @param to the destination system
     * @param warpFactor the desired warp factor
     * @return the created travel order
     * @throws IllegalStateException if the fleet cannot perform warp travel
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public TravelOrder requestWarpTravel(Fleet fleet, GalacticEntry from, GalacticEntry to, double warpFactor) {
        Objects.requireNonNull(fleet, "Fleet must not be null");
        Objects.requireNonNull(from, "Origin must not be null");
        Objects.requireNonNull(to, "Destination must not be null");

        // Validate warp factor
        if (warpFactor < WarpFactor.MIN_WARP_FACTOR || warpFactor >= 10.0) {
            throw new IllegalArgumentException("Warp factor must be between " + WarpFactor.MIN_WARP_FACTOR + " and 9.9999: " + warpFactor);
        }

        // Check fleet health
        double healthRatio = fleet.getFleetHealthRatio();
        if (healthRatio < MIN_WARP_HEALTH_RATIO) {
            throw new IllegalStateException(
                    String.format("Fleet health %.0f%% is below minimum %.0f%% for warp travel",
                            healthRatio * 100, MIN_WARP_HEALTH_RATIO * 100));
        }

        // Check for existing active order
        if (hasActiveOrder(fleet)) {
            throw new IllegalStateException("Fleet already has an active travel order");
        }

        // Calculate travel time
        TravelCalculator.TravelResult result = calculator.calculateWarpTravel(from, to, warpFactor);

        // Apply health reduction to warp factor
        double effectiveWarp = warpFactor * Math.max(0.5, healthRatio);
        result = calculator.calculateWarpTravel(from, to, effectiveWarp);

        // Create travel order
        LocalDateTime now = LocalDateTime.now();
        TravelOrder order = new TravelOrder.Builder(fleet, from, to)
                .fromResult(result)
                .warpFactor(effectiveWarp)
                .departureTime(now)
                .eta(result.calculateEta(now))
                .build();

        activeOrders.put(order.getId(), order);
        return order;
    }

    /**
     * Requests a warp travel order using the fleet's maximum warp capability.
     *
     * @param fleet the fleet to travel
     * @param from the origin system
     * @param to the destination system
     * @return the created travel order
     */
    public TravelOrder requestWarpTravel(Fleet fleet, GalacticEntry from, GalacticEntry to) {
        // Map JDrive to warp factor
        int jDrive = fleet.getFleetJDriveSpeed();
        double warpFactor = TravelCalculator.jDriveToWarpFactor(jDrive);
        return requestWarpTravel(fleet, from, to, warpFactor);
    }

    /**
     * Requests an impulse travel order between two systems.
     * <p>
     * Impulse travel is used for short distances or when warp is unavailable.
     * </p>
     *
     * @param fleet the fleet to travel
     * @param from the origin system
     * @param to the destination system
     * @param throttle the impulse throttle (0.0 to 1.0)
     * @return the created travel order
     */
    public TravelOrder requestImpulseTravel(Fleet fleet, GalacticEntry from, GalacticEntry to, double throttle) {
        Objects.requireNonNull(fleet, "Fleet must not be null");
        Objects.requireNonNull(from, "Origin must not be null");
        Objects.requireNonNull(to, "Destination must not be null");

        if (throttle < 0.0 || throttle > 1.0) {
            throw new IllegalArgumentException("Impulse throttle must be between 0.0 and 1.0: " + throttle);
        }

        if (hasActiveOrder(fleet)) {
            throw new IllegalStateException("Fleet already has an active travel order");
        }

        TravelCalculator.TravelResult result = calculator.calculateImpulseTravel(from, to, throttle);

        LocalDateTime now = LocalDateTime.now();
        TravelOrder order = new TravelOrder.Builder(fleet, from, to)
                .fromResult(result)
                .impulseThrottle(throttle)
                .departureTime(now)
                .eta(result.calculateEta(now))
                .build();

        activeOrders.put(order.getId(), order);
        return order;
    }

    /**
     * Requests an impulse travel order using the fleet's maximum impulse capability.
     *
     * @param fleet the fleet to travel
     * @param from the origin system
     * @param to the destination system
     * @return the created travel order
     */
    public TravelOrder requestImpulseTravel(Fleet fleet, GalacticEntry from, GalacticEntry to) {
        int mDrive = fleet.getFleetMDriveSpeed();
        double throttle = TravelCalculator.mDriveToImpulseThrottle(mDrive);
        return requestImpulseTravel(fleet, from, to, throttle);
    }

    /**
     * Requests the optimal travel between two systems.
     * <p>
     * Automatically selects warp or impulse based on distance and fleet capabilities.
     * </p>
     *
     * @param fleet the fleet to travel
     * @param from the origin system
     * @param to the destination system
     * @return the created travel order
     */
    public TravelOrder requestOptimalTravel(Fleet fleet, GalacticEntry from, GalacticEntry to) {
        double maxWarp = TravelCalculator.jDriveToWarpFactor(fleet.getFleetJDriveSpeed());
        double maxImpulse = TravelCalculator.mDriveToImpulseThrottle(fleet.getFleetMDriveSpeed());

        TravelCalculator.TravelResult result = calculator.calculateOptimalTravel(from, to, maxWarp, maxImpulse);

        LocalDateTime now = LocalDateTime.now();
        TravelOrder.Builder builder = new TravelOrder.Builder(fleet, from, to)
                .fromResult(result)
                .departureTime(now)
                .eta(result.calculateEta(now));

        if (result.getMode() == TravelCalculator.TravelMode.WARP) {
            builder.warpFactor(result.getSpeedFactor());
        } else {
            builder.impulseThrottle(result.getSpeedFactor());
        }

        TravelOrder order = builder.build();
        activeOrders.put(order.getId(), order);
        return order;
    }

    // ---- Travel Order Management ----

    /**
     * Departs on the specified travel order.
     *
     * @param orderId the travel order ID
     */
    public void depart(int orderId) {
        TravelOrder order = activeOrders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Travel order not found: " + orderId);
        }
        order.depart();
    }

    /**
     * Aborts the specified travel order.
     *
     * @param orderId the travel order ID
     * @param reason the reason for aborting
     */
    public void abortTravel(int orderId, String reason) {
        TravelOrder order = activeOrders.remove(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Travel order not found: " + orderId);
        }
        order.abort(reason);
        completedOrders.put(order.getId(), order);
    }

    /**
     * Updates all active travel orders based on the current time.
     * <p>
     * This should be called each game turn to advance travel progress.
     * </p>
     *
     * @param currentTime the current game time
     * @return the number of orders that completed
     */
    public int updateAllTravelOrders(LocalDateTime currentTime) {
        int completed = 0;

        List<Integer> completedIds = new ArrayList<>();

        for (TravelOrder order : activeOrders.values()) {
            order.updateProgress(currentTime);

            if (order.getStatus() == TravelOrder.TravelStatus.ARRIVED) {
                completedIds.add(order.getId());
                completed++;
            }
        }

        // Move completed orders
        for (Integer id : completedIds) {
            TravelOrder order = activeOrders.remove(id);
            if (order != null) {
                completedOrders.put(id, order);
            }
        }

        return completed;
    }

    // ---- Queries ----

    /**
     * @return the active travel orders (unmodifiable)
     */
    public List<TravelOrder> getActiveTravelOrders() {
        return Collections.unmodifiableList(new ArrayList<>(activeOrders.values()));
    }

    /**
     * @return the completed travel orders (unmodifiable)
     */
    public List<TravelOrder> getCompletedTravelOrders() {
        return Collections.unmodifiableList(new ArrayList<>(completedOrders.values()));
    }

    /**
     * Returns the active travel order for a fleet, if any.
     *
     * @param fleet the fleet
     * @return the active travel order, or null
     */
    public TravelOrder getActiveOrderForFleet(Fleet fleet) {
        return activeOrders.values().stream()
                .filter(o -> o.getFleet() == fleet)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a fleet has an active travel order.
     *
     * @param fleet the fleet
     * @return true if the fleet is currently traveling
     */
    public boolean hasActiveOrder(Fleet fleet) {
        return activeOrders.values().stream()
                .anyMatch(o -> o.getFleet() == fleet);
    }

    /**
     * Returns the travel order with the given ID.
     *
     * @param orderId the order ID
     * @return the travel order, or null if not found
     */
    public TravelOrder getTravelOrder(int orderId) {
        return activeOrders.getOrDefault(orderId, completedOrders.get(orderId));
    }

    /**
     * Estimates the travel time to a given quadrant from the fleet's current position.
     *
     * @param fleet the fleet
     * @param targetQuadrant the target quadrant
     * @param warpFactor the warp factor
     * @return the estimated travel time in hours
     */
    public double estimateQuadrantTravelTime(Fleet fleet, String targetQuadrant, double warpFactor) {
        Hex3D position = fleet.getPosition();
        if (position == null) {
            throw new IllegalStateException("Fleet has no current position");
        }

        // Estimate distance to quadrant center
        // Use default sector position if not available
        double distance = distanceCalculator.estimateQuadrantDistance(0, 0, targetQuadrant);
        return WarpFactor.travelTimeHours(distance, warpFactor);
    }

    /**
     * Calculates the distance between two systems.
     *
     * @param from the origin system
     * @param to the destination system
     * @return the distance in light-years
     */
    public double calculateDistance(GalacticEntry from, GalacticEntry to) {
        return distanceCalculator.lightYearsBetween(from, to);
    }

    /**
     * Returns a summary of all active travel orders.
     *
     * @return a formatted summary string
     */
    public String getTravelSummary() {
        if (activeOrders.isEmpty()) {
            return "No active travel orders.";
        }

        return activeOrders.values().stream()
                .map(order -> String.format(
                        "  %s: %s → %s (%s) [%s] %s remaining",
                        order.getFleet().getName(),
                        order.getOrigin().getSystem().getId(),
                        order.getDestination().getSystem().getId(),
                        order.getMode().getDisplayName(),
                        order.getProgressFormatted(),
                        order.getRemainingTimeFormatted()
                ))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Clears all completed travel orders older than the specified count.
     *
     * @param keepLast the number of recent orders to keep
     */
    public void clearOldCompletedOrders(int keepLast) {
        if (completedOrders.size() <= keepLast) {
            return;
        }

        // Sort by ID and remove oldest
        List<Map.Entry<Integer, TravelOrder>> sorted = completedOrders.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        int toRemove = completedOrders.size() - keepLast;
        for (int i = 0; i < toRemove; i++) {
            completedOrders.remove(sorted.get(i).getKey());
        }
    }
}
