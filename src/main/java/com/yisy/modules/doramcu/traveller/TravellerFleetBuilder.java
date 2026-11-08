package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Ship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Builds a {@link Fleet} from a collection of Traveller {@link ShipDesign} instances.
 * <p>
 * This is a convenience wrapper around {@link TravellerShipFactory} that converts
 * multiple ship designs into a single fleet ready for hex-based combat.
 * </p>
 * <p>
 * Usage example:
 * </p>
 * <pre>
 * ShipDesign destroyer = new ShipDesign(1, "ICN Fearless", 10000);
 * // ... configure design ...
 *
 * ShipDesign frigate = new ShipDesign(2, "ICN Swift", 2000);
 * // ... configure design ...
 *
 * Fleet fleet = new TravellerFleetBuilder("Squadron Alpha")
 *         .addDesign(destroyer)
 *         .addDesign(frigate)
 *         .build();
 *
 * // Fleet is now ready for Battle combat
 * Battle battle = new Battle(1, attackerFleet, defenderFleet, new Hex3D(0, 0, 0));
 * Battle.BattleResult result = battle.fightToCompletion();
 * </pre>
 */
public class TravellerFleetBuilder {

    private final int fleetId;
    private final String fleetName;
    private final List<ShipDesign> designs;
    private int nextShipId;

    /**
     * Creates a new fleet builder with the given fleet name.
     *
     * @param fleetName the display name for the fleet
     */
    public TravellerFleetBuilder(String fleetName) {
        this(1, fleetName);
    }

    /**
     * Creates a new fleet builder with the given ID and name.
     *
     * @param fleetId the unique fleet identifier
     * @param fleetName the display name for the fleet
     */
    public TravellerFleetBuilder(int fleetId, String fleetName) {
        this.fleetId = fleetId;
        this.fleetName = Objects.requireNonNull(fleetName, "Fleet name must not be null");
        this.designs = new ArrayList<>();
        this.nextShipId = 1;
    }

    /**
     * Sets the starting ship ID for the factory.
     *
     * @param startId the first ship ID to assign
     * @return this builder for chaining
     */
    public TravellerFleetBuilder setStartShipId(int startId) {
        this.nextShipId = startId;
        return this;
    }

    /**
     * Adds a ship design to this fleet.
     *
     * @param design the Traveller ship design to add
     * @return this builder for chaining
     */
    public TravellerFleetBuilder addDesign(ShipDesign design) {
        Objects.requireNonNull(design, "Design must not be null");
        designs.add(design);
        return this;
    }

    /**
     * Adds multiple ship designs to this fleet.
     *
     * @param designs the Traveller ship designs to add
     * @return this builder for chaining
     */
    public TravellerFleetBuilder addDesigns(ShipDesign... designs) {
        Objects.requireNonNull(designs, "Designs must not be null");
        Collections.addAll(this.designs, designs);
        return this;
    }

    /**
     * Returns the number of designs added to this fleet.
     *
     * @return the design count
     */
    public int getDesignCount() {
        return designs.size();
    }

    /**
     * Builds the fleet from the added ship designs.
     * <p>
     * Each design is validated and converted to a Ship instance using
     * {@link TravellerShipFactory}. The resulting ships are added to a new
     * Fleet instance.
     * </p>
     *
     * @return a new Fleet containing the converted ships
     * @throws IllegalStateException if no designs have been added
     */
    public Fleet build() {
        if (designs.isEmpty()) {
            throw new IllegalStateException("At least one ship design must be added before building");
        }

        Fleet fleet = new Fleet(fleetId, fleetName);
        TravellerShipFactory factory = new TravellerShipFactory(nextShipId);

        for (ShipDesign design : designs) {
            Ship ship = factory.convert(design);
            fleet.addShip(ship);
        }

        return fleet;
    }
}
