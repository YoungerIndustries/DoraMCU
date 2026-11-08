package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Route planner for finding jump routes between star systems.
 * <p>
 * In Classic Traveller Book 2, navigation between star systems requires
 * planning jump routes that respect the ship's jump drive range and fuel
 * capacity. The route planner uses breadth-first search (BFS) to find
 * the shortest path between two worlds using available hyperlanes.
 * </p>
 * <p>
 * The planner considers:
 * </p>
 * <ul>
 *   <li>Ship's jump drive rating (maximum jump distance)</li>
 *   <li>Ship's fuel capacity (number of jumps possible)</li>
 *   <li>Hyperlane safety (optional preference for safe routes)</li>
 *   <li>Starport availability (refueling stops)</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   RoutePlanner planner = new RoutePlanner(sector);
 *   JumpPlan plan = planner.findRoute(origin, destination, jumpRating, fuelTank);
 *   // Returns: Tukkayj → Mesna → Core (2 jumps, 6000 tons fuel)
 * </pre>
 */
public class RoutePlanner {

    /**
     * The sector containing all worlds and hyperlanes.
     */
    private final TravellerSector sector;

    /**
     * Creates a new route planner for the given sector.
     *
     * @param sector the sector to plan routes within
     */
    public RoutePlanner(TravellerSector sector) {
        this.sector = Objects.requireNonNull(sector, "Sector must not be null");
    }

    // ---- Route Finding ----

    /**
     * Finds the shortest route between two worlds.
     * <p>
     * Uses BFS to find the minimum number of jumps from origin to destination.
     * The route respects the ship's jump drive rating.
     * </p>
     *
     * @param origin the starting world
     * @param destination the target world
     * @param jumpRating the ship's jump drive rating
     * @param fusionDrive the ship's fusion drive (for fuel calculation)
     * @param totalTonnage the ship's total tonnage
     * @return a jump plan, or null if no route exists
     */
    public JumpPlan findRoute(TravellerWorld origin, TravellerWorld destination,
                               int jumpRating, FusionDrive fusionDrive, int totalTonnage) {
        Objects.requireNonNull(origin, "Origin must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");

        // Quick check: same world?
        if (origin.equals(destination)) {
            return null;
        }

        // BFS to find shortest path
        List<JumpRoute> route = bfs(origin, destination, jumpRating);
        if (route == null || route.isEmpty()) {
            return null;
        }

        // Create jump plan from route
        return new JumpPlan(route, jumpRating);
    }

    /**
     * Finds the shortest route with fuel constraints.
     * <p>
     * The route must be completable with the given fuel tank's capacity.
     * If the shortest route requires more fuel than available, returns null.
     * </p>
     *
     * @param origin the starting world
     * @param destination the target world
     * @param jumpRating the ship's jump drive rating
     * @param fuelTank the ship's fuel tank
     * @return a jump plan, or null if no feasible route exists
     */
    public JumpPlan findRouteWithFuel(TravellerWorld origin, TravellerWorld destination,
                                       int jumpRating, FuelTank fuelTank) {
        Objects.requireNonNull(fuelTank, "Fuel tank must not be null");

        JumpPlan plan = findRoute(origin, destination, jumpRating,
                fuelTank.getFusionDrive(), fuelTank.getTotalTonnage());

        if (plan == null) {
            return null;
        }

        // Check if fuel is sufficient
        if (!fuelTank.hasEnoughFuelForPlan(plan)) {
            return null;
        }

        return plan;
    }

    /**
     * Finds all reachable worlds from the given origin.
     * <p>
     * Returns all worlds that can be reached with the given jump rating
     * and fuel capacity.
     * </p>
     *
     * @param origin the starting world
     * @param jumpRating the ship's jump drive rating
     * @param maxJumps the maximum number of jumps (fuel constraint)
     * @return map of reachable worlds to their jump distance
     */
    public Map<TravellerWorld, Integer> findReachableWorlds(TravellerWorld origin,
                                                              int jumpRating, int maxJumps) {
        Objects.requireNonNull(origin, "Origin must not be null");

        Map<TravellerWorld, Integer> reachable = new HashMap<>();
        Queue<TravellerWorld> queue = new LinkedList<>();
        Set<TravellerWorld> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);
        reachable.put(origin, 0);

        while (!queue.isEmpty()) {
            TravellerWorld current = queue.poll();
            int currentJumps = reachable.get(current);

            if (currentJumps >= maxJumps) {
                continue;
            }

            // Find neighbors
            for (TravellerWorld neighbor : getNeighbors(current, jumpRating)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    reachable.put(neighbor, currentJumps + 1);
                    queue.add(neighbor);
                }
            }
        }

        return Collections.unmodifiableMap(reachable);
    }

    /**
     * Finds the nearest world with a starport for refueling.
     * <p>
     * Useful when a ship needs to refuel during a voyage.
     * </p>
     *
     * @param currentWorld the ship's current location
     * @param jumpRating the ship's jump drive rating
     * @return the nearest world with a starport, or null if none found
     */
    public TravellerWorld findNearestStarport(TravellerWorld currentWorld, int jumpRating) {
        Objects.requireNonNull(currentWorld, "Current world must not be null");

        Map<TravellerWorld, Integer> reachable = findReachableWorlds(currentWorld, jumpRating, 1);

        TravellerWorld nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<TravellerWorld, Integer> entry : reachable.entrySet()) {
            TravellerWorld world = entry.getKey();
            if (!world.equals(currentWorld) && world.hasStarport()) {
                int distance = currentWorld.getJumpDistanceTo(world);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = world;
                }
            }
        }

        return nearest;
    }

    // ---- BFS Implementation ----

    /**
     * Performs breadth-first search to find the shortest path.
     *
     * @param origin the starting world
     * @param destination the target world
     * @param jumpRating the ship's jump drive rating
     * @return list of jump routes, or null if no path exists
     */
    private List<JumpRoute> bfs(TravellerWorld origin, TravellerWorld destination, int jumpRating) {
        Queue<BFSNode> queue = new LinkedList<>();
        Set<TravellerWorld> visited = new HashSet<>();

        queue.add(new BFSNode(origin, null, new ArrayList<>()));
        visited.add(origin);

        while (!queue.isEmpty()) {
            BFSNode node = queue.poll();
            TravellerWorld current = node.world;

            // Found destination
            if (current.equals(destination)) {
                return node.path;
            }

            // Explore neighbors
            for (TravellerWorld neighbor : getNeighbors(current, jumpRating)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);

                    // Create jump route
                    JumpRoute route = createJumpRoute(current, neighbor);

                    // Add to queue
                    List<JumpRoute> newPath = new ArrayList<>(node.path);
                    newPath.add(route);

                    queue.add(new BFSNode(neighbor, current, newPath));
                }
            }
        }

        // No path found
        return null;
    }

    /**
     * Gets all reachable neighbors of a world.
     * <p>
     * A neighbor is reachable if:
     * </p>
     * <ul>
     *   <li>There is a hyperlane connecting the worlds</li>
     *   <li>The distance is within the ship's jump rating</li>
     * </ul>
     *
     * @param world the current world
     * @param jumpRating the ship's jump drive rating
     * @return list of reachable neighbor worlds
     */
    private List<TravellerWorld> getNeighbors(TravellerWorld world, int jumpRating) {
        List<TravellerWorld> neighbors = new ArrayList<>();
        Set<TravellerWorld> seen = new HashSet<>();

        // Get hyperlanes from the world
        for (Hyperlane hyperlane : world.getHyperlanes()) {
            TravellerWorld other = hyperlane.getOtherEnd(world);

            // Check if within jump range
            if (hyperlane.isTraversable(jumpRating) && !seen.contains(other)) {
                seen.add(other);
                neighbors.add(other);
            }
        }

        // Also check all worlds in sector for direct jumps (off-chart)
        for (TravellerWorld other : sector.getAllWorlds()) {
            if (!other.equals(world) && !seen.contains(other)) {
                int distance = world.getJumpDistanceTo(other);
                if (distance <= jumpRating) {
                    seen.add(other);
                    neighbors.add(other);
                }
            }
        }

        return neighbors;
    }

    /**
     * Creates a jump route between two worlds.
     * <p>
     * If a hyperlane exists, uses it. Otherwise, creates an off-chart route.
     * </p>
     *
     * @param from the origin world
     * @param to the destination world
     * @return the jump route
     */
    private JumpRoute createJumpRoute(TravellerWorld from, TravellerWorld to) {
        // Check for existing hyperlane
        for (Hyperlane hyperlane : from.getHyperlanes()) {
            if (hyperlane.connects(to)) {
                return new JumpRoute(hyperlane, FusionDrive.M1, 100000);
            }
        }

        // Off-chart jump
        int distance = from.getJumpDistanceTo(to);
        int fuelCost = Math.max(1000, 100000 / 100) * distance;
        return new JumpRoute(from, to, distance, fuelCost, true);
    }

    // ---- Safe Route Finding ----

    /**
     * Finds a route that prefers safe hyperlanes.
     * <p>
     * If a safe route exists, it is preferred over unsafe routes.
     * If no safe route exists, returns the shortest route regardless of safety.
     * </p>
     *
     * @param origin the starting world
     * @param destination the target world
     * @param jumpRating the ship's jump drive rating
     * @param fusionDrive the ship's fusion drive
     * @param totalTonnage the ship's total tonnage
     * @return a jump plan, or null if no route exists
     */
    public JumpPlan findSafeRoute(TravellerWorld origin, TravellerWorld destination,
                                   int jumpRating, FusionDrive fusionDrive, int totalTonnage) {
        Objects.requireNonNull(origin, "Origin must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");

        // Try to find safe route first
        List<JumpRoute> safeRoute = bfsSafe(origin, destination, jumpRating);
        if (safeRoute != null && !safeRoute.isEmpty()) {
            return new JumpPlan(safeRoute, jumpRating);
        }

        // Fall back to any route
        return findRoute(origin, destination, jumpRating, fusionDrive, totalTonnage);
    }

    /**
     * BFS that prefers safe hyperlanes.
     *
     * @param origin the starting world
     * @param destination the target world
     * @param jumpRating the ship's jump drive rating
     * @return list of jump routes, or null if no safe path exists
     */
    private List<JumpRoute> bfsSafe(TravellerWorld origin, TravellerWorld destination, int jumpRating) {
        Queue<BFSNode> queue = new LinkedList<>();
        Set<TravellerWorld> visited = new HashSet<>();

        queue.add(new BFSNode(origin, null, new ArrayList<>()));
        visited.add(origin);

        while (!queue.isEmpty()) {
            BFSNode node = queue.poll();
            TravellerWorld current = node.world;

            if (current.equals(destination)) {
                return node.path;
            }

            for (TravellerWorld neighbor : getNeighbors(current, jumpRating)) {
                if (!visited.contains(neighbor)) {
                    // Check if there's a safe hyperlane
                    boolean hasSafeLane = false;
                    for (Hyperlane hyperlane : current.getHyperlanes()) {
                        if (hyperlane.connects(neighbor) && hyperlane.isSafe()) {
                            hasSafeLane = true;
                            break;
                        }
                    }

                    if (hasSafeLane) {
                        visited.add(neighbor);
                        JumpRoute route = createJumpRoute(current, neighbor);
                        List<JumpRoute> newPath = new ArrayList<>(node.path);
                        newPath.add(route);
                        queue.add(new BFSNode(neighbor, current, newPath));
                    }
                }
            }
        }

        return null;
    }

    // ---- Inner Classes ----

    /**
     * BFS node for pathfinding.
     */
    private static class BFSNode {
        final TravellerWorld world;
        final TravellerWorld parent;
        final List<JumpRoute> path;

        BFSNode(TravellerWorld world, TravellerWorld parent, List<JumpRoute> path) {
            this.world = world;
            this.parent = parent;
            this.path = path;
        }
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return "RoutePlanner for " + sector.getName() +
                " (" + sector.getWorldCount() + " worlds, " +
                sector.getHyperlaneCount() + " hyperlanes)";
    }
}
