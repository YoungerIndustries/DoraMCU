package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Generates hyperlane connections between solar systems in a galaxy.
 * <p>
 * Extracted from GalaxyGenerator to separate hyperlane network generation
 * from the rest of the galaxy generation logic.
 * </p>
 */
public class HyperlaneGenerator {

    private final Random random;

    /**
     * Creates a new hyperlane generator.
     *
     * @param random the random instance
     */
    public HyperlaneGenerator(Random random) {
        this.random = random;
    }

    /**
     * Generates hyperlane connections between solar systems in the galaxy.
     * <p>
     * Uses a nearest-neighbor approach: for each system, connect to nearby systems
     * based on the hyperlane density setting.
     * </p>
     *
     * @param galaxy the galaxy to add hyperlanes to
     * @param hyperlaneDensity the density multiplier for extra connections
     */
    public void generateHyperlanes(Galaxy galaxy, double hyperlaneDensity) {
        List<SolarSystem> systems = new ArrayList<>(galaxy.getAllSystems().values());
        if (systems.size() < 2) {
            return;
        }

        // Ensure all systems are connected (spanning tree)
        Set<Integer> connected = new HashSet<>();
        connected.add(systems.get(0).getId());

        for (int i = 0; i < systems.size() && connected.size() < systems.size(); i++) {
            SolarSystem current = systems.get(i);
            if (!connected.contains(current.getId())) {
                // Find nearest connected system
                int nearestId = -1;
                int nearestDist = Integer.MAX_VALUE;

                for (Integer connectedId : connected) {
                    // Simple distance based on system IDs for spanning tree
                    int dist = Math.abs(current.getId() - connectedId);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearestId = connectedId;
                    }
                }

                if (nearestId >= 0) {
                    galaxy.addHyperlane(nearestId, current.getId(), nearestDist);
                    connected.add(current.getId());
                }
            }
        }

        // Add additional hyperlanes based on density
        int extraConnections = (int) (systems.size() * hyperlaneDensity);
        for (int i = 0; i < extraConnections; i++) {
            int fromIdx = random.nextInt(systems.size());
            int toIdx;
            do {
                toIdx = random.nextInt(systems.size());
            } while (toIdx == fromIdx);

            int distance = Math.abs(systems.get(fromIdx).getId() - systems.get(toIdx).getId());
            galaxy.addHyperlane(systems.get(fromIdx).getId(), systems.get(toIdx).getId(), distance);
        }
    }
}
