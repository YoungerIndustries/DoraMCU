package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.JumpTravelRequest;
import com.yisy.modules.doramcu.dto.JumpTravelResponse;
import com.yisy.modules.doramcu.traveller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service layer for jump travel and route planning.
 * <p>
 * Manages interstellar navigation: finding routes between worlds,
 * calculating fuel requirements, and tracking jump progress.
 * </p>
 */
@Service
public class JumpTravelService {

    private static final Logger log = LoggerFactory.getLogger(JumpTravelService.class);

    private final Map<Integer, TravellerWorld> worlds = new HashMap<>();
    private final Map<Integer, JumpTravelStatus> travelStatuses = new HashMap<>();
    private final Map<Integer, TravellerSector> sectors = new HashMap<>();

    /**
     * Registers a world for jump travel operations.
     */
    public void registerWorld(TravellerWorld world) {
        worlds.put(world.getId(), world);
    }

    /**
     * Registers a sector (and all its worlds).
     */
    public void registerSector(TravellerSector sector) {
        sectors.put(sector.getId(), sector);
        for (TravellerWorld world : sector.getAllWorlds()) {
            worlds.put(world.getId(), world);
        }
    }

    /**
     * Finds a route between two worlds.
     */
    public JumpTravelResponse findRoute(JumpTravelRequest request) {
        TravellerWorld source = worlds.get(request.getSourceWorldId());
        TravellerWorld destination = worlds.get(request.getDestinationWorldId());

        if (source == null) {
            throw new IllegalArgumentException("Source world not found: " +
                    request.getSourceWorldId());
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination world not found: " +
                    request.getDestinationWorldId());
        }

        // Build a temporary sector from all registered worlds for route planning
        TravellerSector tempSector = buildTempSector();
        RoutePlanner planner = new RoutePlanner(tempSector);

        int jumpRating = request.getMaxJumpDistance() > 0 ?
                request.getMaxJumpDistance() : 4;

        JumpPlan plan = planner.findRoute(source, destination, jumpRating,
                FusionDrive.fromRating(2), 2000);

        if (plan == null) {
            JumpTravelResponse response = new JumpTravelResponse();
            response.setOperation("FIND_ROUTE");
            response.setSourceWorld(source.getName());
            response.setDestinationWorld(destination.getName());
            response.setMessage("No route found from " + source.getName() +
                    " to " + destination.getName());
            return response;
        }

        return toRouteResponse(plan, source, destination);
    }

    /**
     * Gets the current jump travel status for a ship.
     */
    public JumpTravelResponse getStatus(int shipDesignId) {
        JumpTravelStatus status = travelStatuses.get(shipDesignId);
        if (status == null) {
            JumpTravelResponse response = new JumpTravelResponse();
            response.setOperation("GET_STATUS");
            response.setTravelState("AT_SYSTEM");
            response.setMessage("No active travel for ship " + shipDesignId);
            return response;
        }

        JumpTravelResponse response = new JumpTravelResponse();
        response.setOperation("GET_STATUS");
        response.setTravelState(status.getTravelState().name());

        TravellerWorld currentWorld = status.getCurrentWorld();
        if (currentWorld != null) {
            response.setCurrentSystem(currentWorld.getName());
        }

        FuelTank fuelTank = status.getFuelTank();
        if (fuelTank != null) {
            response.setCurrentFuel(fuelTank.getCurrentFuelTons());
            response.setMaxFuel(fuelTank.getMaxCapacityTons());
        }

        response.setMessage("Ship " + shipDesignId + " is " +
                status.getTravelState() + " at " +
                (currentWorld != null ? currentWorld.getName() : "unknown"));

        return response;
    }

    /**
     * Starts a new journey to a destination.
     */
    public JumpTravelResponse startJourney(JumpTravelRequest request) {
        TravellerWorld source = worlds.get(request.getSourceWorldId());
        TravellerWorld destination = worlds.get(request.getDestinationWorldId());

        if (source == null) {
            throw new IllegalArgumentException("Source world not found: " +
                    request.getSourceWorldId());
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination world not found: " +
                    request.getDestinationWorldId());
        }

        // Find route
        TravellerSector tempSector = buildTempSector();
        RoutePlanner planner = new RoutePlanner(tempSector);

        int jumpRating = request.getMaxJumpDistance() > 0 ?
                request.getMaxJumpDistance() : 4;

        JumpPlan plan = planner.findRoute(source, destination, jumpRating,
                FusionDrive.fromRating(2), 2000);

        if (plan == null) {
            JumpTravelResponse response = new JumpTravelResponse();
            response.setOperation("START_JOURNEY");
            response.setMessage("No route found from " + source.getName() +
                    " to " + destination.getName());
            return response;
        }

        // Create fuel tank based on ship design
        int tonnage = 2000; // Default tonnage
        FuelTank fuelTank = new FuelTank(FusionDrive.fromRating(2), tonnage);

        // Create travel status
        JumpTravelStatus status = new JumpTravelStatus(source, fuelTank, jumpRating);
        status.startJumpPlan(plan);
        travelStatuses.put(request.getShipDesignId(), status);

        return toRouteResponse(plan, source, destination);
    }

    /**
     * Executes the next jump in a journey.
     */
    public JumpTravelResponse executeJump(int shipDesignId) {
        JumpTravelStatus status = travelStatuses.get(shipDesignId);
        if (status == null) {
            throw new IllegalArgumentException("No active journey for ship: " +
                    shipDesignId);
        }

        JumpPlan plan = status.getCurrentPlan();
        if (plan == null) {
            throw new IllegalArgumentException("No jump plan for ship: " +
                    shipDesignId);
        }

        // Execute next jump
        boolean success = status.executeNextJump();

        JumpTravelResponse response = new JumpTravelResponse();
        response.setOperation("EXECUTE_JUMP");

        if (success) {
            TravellerWorld currentWorld = status.getCurrentWorld();
            response.setCurrentSystem(currentWorld != null ?
                    currentWorld.getName() : "unknown");
            response.setTravelState(status.getTravelState().name());

            FuelTank fuelTank = status.getFuelTank();
            response.setCurrentFuel(fuelTank.getCurrentFuelTons());
            response.setMaxFuel(fuelTank.getMaxCapacityTons());

            int jumpsRemaining = status.getJumpsRemaining();
            response.setJumpCount(jumpsRemaining);

            if (status.getTravelState() == TravelState.AT_SYSTEM &&
                    jumpsRemaining == 0) {
                response.setMessage("Arrived at destination!");
                travelStatuses.remove(shipDesignId);
            } else {
                response.setMessage("Jump completed. " + jumpsRemaining +
                        " jumps remaining.");
            }
        } else {
            response.setMessage("Jump failed — not enough fuel or no plan");
            response.setTravelState(status.getTravelState().name());
        }

        return response;
    }

    // ---- Private Helpers ----

    private TravellerSector buildTempSector() {
        // Create a temporary sector containing all registered worlds
        TravellerSector sector = new TravellerSector(0, "Temp", 40, 40);
        for (TravellerWorld world : worlds.values()) {
            sector.addWorld(world);
        }
        return sector;
    }

    private JumpTravelResponse toRouteResponse(JumpPlan plan,
                                                 TravellerWorld source,
                                                 TravellerWorld destination) {
        JumpTravelResponse response = new JumpTravelResponse();
        response.setOperation("FIND_ROUTE");
        response.setSourceWorld(source.getName());
        response.setDestinationWorld(destination.getName());
        response.setJumpCount(plan.getJumpCount());
        response.setTotalDistance(plan.getTotalDistanceParsecs());
        response.setTotalFuelRequired(plan.getTotalFuelTons());
        response.setSafe(plan.isSafe());
        response.setHasEnoughFuel(plan.getFuelRemaining(2000) >= 0);

        // Build waypoints
        List<JumpTravelResponse.RouteWaypoint> waypoints = new ArrayList<>();
        List<JumpRoute> routes = plan.getJumpLegs();
        for (int i = 0; i < routes.size(); i++) {
            JumpRoute route = routes.get(i);
            JumpTravelResponse.RouteWaypoint wp = new JumpTravelResponse.RouteWaypoint();
            wp.setJumpNumber(i + 1);
            wp.setWorldName(route.getDestination().getName());
            wp.setWorldId(route.getDestination().getId());
            wp.setUwp(route.getDestination().getUwp().toString());
            wp.setDistanceFromPrevious(route.getDistanceParsecs());
            wp.setSafeJump(route.isSafe());
            wp.setFuelCost(route.getFuelCostTons());
            waypoints.add(wp);
        }
        response.setWaypoints(waypoints);

        response.setMessage(plan.getJumpCount() + " jumps, " +
                String.format("%.1f", plan.getTotalDistanceParsecs()) +
                " parsecs, " + plan.getTotalFuelTons() + " tons fuel required. " +
                (plan.isSafe() ? "Safe route." : "Contains off-chart jumps."));

        return response;
    }
}
