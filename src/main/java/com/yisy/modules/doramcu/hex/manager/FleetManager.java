package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipComponent;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.SolarSystem;
import com.yisy.modules.doramcu.hex.SolarSystemEntity;
import com.yisy.modules.doramcu.hex.WeaponType;
import com.yisy.modules.doramcu.hex.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages fleet creation, starting fleet setup, and ship factory methods.
 * <p>
 * Includes Dora AI integration for AI-assisted fleet naming and strategy advice.
 * </p>
 */
public class FleetManager {

    private final GameContext context;

    /**
     * Creates a new FleetManager.
     *
     * @param context the shared game context
     */
    public FleetManager(GameContext context) {
        this.context = context;
    }

    /**
     * Generates a fleet name based on the player's name and fleet purpose.
     *
     * @param playerName the player's name
     * @param purpose the fleet's purpose (e.g., "military", "mining", "exploration")
     * @return the fleet name
     */
    public String generateFleetName(String playerName, String purpose) {
        return playerName + " " + purpose + " Fleet";
    }

    /**
     * Generates strategy advice for fleet deployment.
     *
     * @param player the player requesting advice
     * @return the strategy advice string
     */
    public String getFleetStrategyAdvice(Player player) {
        int fleetCount = player.getOwnedFleetIds().size();
        int planetCount = player.getOwnedPlanetIds().size();
        return String.format(
            "Player has %d fleets and %d planets. Fleet IDs: %s",
            fleetCount, planetCount, player.getOwnedFleetIds()
        );
    }

    /**
     * Creates and adds a new fleet with an auto-generated ID.
     *
     * @param name the fleet name
     * @param owner the owning player
     * @return the new fleet
     */
    public Fleet createFleet(String name, Player owner) {
        int fleetId = context.getNextFleetIdAndIncrement();
        Fleet fleet = new Fleet(fleetId, name);
        return context.addFleet(fleet, owner);
    }

    /**
     * Creates starting fleets for a player: a military fleet and a mining fleet.
     * <p>
     * The military fleet contains 2 frigates equipped with beam weapons and shields,
     * providing basic defense capability. The mining fleet contains 1 frigate
     * for resource exploration and extraction operations.
     * </p>
     *
     * @param player the player to create starting fleets for
     * @return a list containing the military fleet (first) and mining fleet (second)
     */
    public List<Fleet> createStartingFleets(Player player) {
        return createStartingFleets(null, player);
    }

    /**
     * Creates starting fleets for a player, using the given Galaxy for solar system lookup.
     *
     * @param galaxy the galaxy to look up solar systems (may be null)
     * @param player the player to create starting fleets for
     * @return a list containing the military fleet (first) and mining fleet (second)
     */
    public List<Fleet> createStartingFleets(Galaxy galaxy, Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }

        List<Fleet> startingFleets = new ArrayList<>();

        // Create the military fleet
        Fleet militaryFleet = createFleet(player.getName() + " Military Fleet", player);
        militaryFleet.addShip(createMilitaryFrigate(player.getName() + " Defender I"));
        militaryFleet.addShip(createMilitaryFrigate(player.getName() + " Defender II"));
        militaryFleet.addShip(createMilitaryDestroyer(player.getName() + " Vanguard"));
        startingFleets.add(militaryFleet);

        // Create the mining fleet
        Fleet miningFleet = createFleet(player.getName() + " Mining Fleet", player);
        miningFleet.addShip(createMiningFrigate(player.getName() + " Prospector I"));
        startingFleets.add(miningFleet);

        // Position fleets in the player's home system if set
        if (player.getHomeSystemId() != null) {
            Map<Integer, SolarSystem> solarSystems = context.getSolarSystems();
            SolarSystem homeSolarSystem = solarSystems.get(player.getHomeSystemId());
            // Also check galaxy if available
            if (homeSolarSystem == null && galaxy != null) {
                homeSolarSystem = galaxy.getSolarSystem(player.getHomeSystemId());
            }
            if (homeSolarSystem != null && homeSolarSystem.size() > 0) {
                // Get the first solar system entity in the home system
                SolarSystemEntity homeEntity = homeSolarSystem.getAllSystems().values().iterator().next();
                Hex3D homePos = new Hex3D(0, 0, 0);
                militaryFleet.setPosition(homePos);
                miningFleet.setPosition(homePos);
                militaryFleet.setHomeSystem(homeEntity.getAllPlanets().values().iterator().next());
                miningFleet.setHomeSystem(homeEntity.getAllPlanets().values().iterator().next());

                // Add fleets to the solar system entity
                homeEntity.addFleet(militaryFleet, homePos);
                homeEntity.addFleet(miningFleet, homePos);
            }
        }

        return startingFleets;
    }

    /**
     * Creates a basic military frigate with beam weapons and shields.
     *
     * @param name the ship name
     * @return the configured frigate
     */
    public Ship createMilitaryFrigate(String name) {
        int shipId = context.getNextShipIdAndIncrement();
        Ship ship = new Ship(shipId, ShipType.FRIGATE);
        ship.setName(name);
        ship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        ship.addComponent(ShipComponent.shield(1));
        context.addShip(ship);
        return ship;
    }

    /**
     * Creates a basic military destroyer with beam weapons and shields.
     *
     * @param name the ship name
     * @return the configured destroyer
     */
    public Ship createMilitaryDestroyer(String name) {
        int shipId = context.getNextShipIdAndIncrement();
        Ship ship = new Ship(shipId, ShipType.DESTROYER);
        ship.setName(name);
        ship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        ship.addComponent(new ShipComponent(WeaponType.BEAM, 1));
        ship.addComponent(ShipComponent.shield(1));
        ship.addComponent(ShipComponent.shield(1));
        context.addShip(ship);
        return ship;
    }

    /**
     * Creates a basic mining frigate for resource exploration.
     *
     * @param name the ship name
     * @return the configured mining frigate
     */
    public Ship createMiningFrigate(String name) {
        int shipId = context.getNextShipIdAndIncrement();
        Ship ship = new Ship(shipId, ShipType.FRIGATE);
        ship.setName(name);
        ship.addComponent(ShipComponent.shield(1));
        context.addShip(ship);
        return ship;
    }
}
