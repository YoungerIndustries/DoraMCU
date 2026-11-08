package com.yisy.modules.doramcu.traveller;

/**
 * Facilities available at a Traveller space station.
 * <p>
 * Each facility provides specific services to docked ships. The availability
 * and quality of facilities depend on the station type and the world's
 * economic conditions.
 * </p>
 * <p>
 * In Classic Traveller, facilities determine what a ship can do while
 * docked: refuel, repair damage, trade cargo, undergo upgrades, clear
 * customs, or access military services.
 * </p>
 */
public enum StationFacility {

    /**
     * Refueling facility — provides fusion fuel for jump travel.
     * <p>
     * Essential for interstellar travel. Fuel cost varies by station type
     * and world price level. Without refueling, a ship cannot jump to
     * another system.
     * </p>
     */
    REFUELING("Refueling", "Provides fusion fuel for jump travel",
              ServiceCategory.ESSENTIAL),

    /**
     * Repair facility — fixes hull and system damage.
     * <p>
     * Repair yards can restore hull points, fix damaged weapon systems,
     * and repair drive components. Repair time and cost depend on the
     * extent of damage and facility quality.
     * </p>
     */
    REPAIR("Repair Yard", "Fixes hull and system damage",
           ServiceCategory.ESSENTIAL),

    /**
     * Market — buying and selling commodities.
     * <p>
     * Station markets provide access to local commodities at world prices.
     * Larger stations have better market selection and volume capacity.
     * </p>
     */
    MARKET("Market", "Buying and selling commodities",
           ServiceCategory.TRADE),

    /**
     * Shipyard — construction and major upgrades.
     * <p>
     * Shipyards can build new ships from designs, install new weapons,
     * upgrade drives, and perform major modifications. Only available
     * at larger stations (Bravo and above).
     * </p>
     */
    SHIPYARD("Shipyard", "Ship construction and major upgrades",
             ServiceCategory.CONSTRUCTION),

    /**
     * Customs — legal trade documentation and inspections.
     * <p>
     * Customs facilities process trade documentation, inspect cargo
     * for contraband, and issue permits for restricted trade. Required
     * for legal interstellar commerce.
     * </p>
     */
    CUSTOMS("Customs", "Trade documentation and inspections",
            ServiceCategory.LEGAL),

    /**
     * Military facility — naval base and defense services.
     * <p>
     * Military facilities provide naval support, including patrol services,
     * defense coordination, and priority repair for allied ships. Found
     * on worlds with naval presence.
     * </p>
     */
    MILITARY("Military Base", "Naval base and defense services",
             ServiceCategory.MILITARY),

    /**
     * Passenger terminal — embarkation and disembarkation.
     * <p>
     * Passenger terminals handle passenger transport, including processing,
     * security screening, and comfort services. Required for passenger
     * carrying operations.
     * </p>
     */
    PASSENGER("Passenger Terminal", "Passenger embarkation and disembarkation",
              ServiceCategory.PASSENGER),

    /**
     * Information center — news, charts, and intelligence.
     * <p>
     * Information centers provide sector charts, trade intelligence,
     * news feeds, and navigation data. Useful for planning trade routes
     * and avoiding hazards.
     * </p>
     */
    INFORMATION("Information Center", "News, charts, and intelligence",
                ServiceCategory.INFORMATION),

    /**
     * Crew quarters — rest and recreation for ship crews.
     * <p>
     * Crew facilities provide lodging, food, and recreation for ship
     * crews during extended dock periods. Improves crew morale and
     * reduces desertion risk.
     * </p>
     */
    CREW_QUARTERS("Crew Quarters", "Rest and recreation for crews",
                  ServiceCategory.SUPPORT),

    /**
     * Medical bay — treatment for injuries and illness.
     * <p>
     * Medical facilities treat crew injuries, provide medical supplies,
     * and handle emergencies. Essential on frontier worlds where
     * planetary medical services may be limited.
     * </p>
     */
    MEDICAL("Medical Bay", "Treatment for injuries and illness",
            ServiceCategory.SUPPORT);

    private final String name;
    private final String description;
    private final ServiceCategory category;

    StationFacility(String name, String description, ServiceCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    /**
     * Returns the human-readable name of this facility.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a description of this facility's services.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the category of this facility.
     *
     * @return the service category
     */
    public ServiceCategory getCategory() {
        return category;
    }

    /**
     * Returns all facilities in the given category.
     *
     * @param category the service category
     * @return array of facilities in that category
     */
    public static StationFacility[] getByCategory(ServiceCategory category) {
        StationFacility[] facilities = values();
        java.util.List<StationFacility> result = new java.util.ArrayList<>();
        for (StationFacility facility : facilities) {
            if (facility.category == category) {
                result.add(facility);
            }
        }
        return result.toArray(new StationFacility[0]);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Categories of station facilities.
     * <p>
     * Facilities are grouped by their primary purpose, which helps
     * determine which facilities are available at different station types.
     * </p>
     */
    public enum ServiceCategory {
        /**
         * Essential services required for basic ship operations.
         */
        ESSENTIAL,

        /**
         * Trade-related services for commerce.
         */
        TRADE,

        /**
         * Construction and modification services.
         */
        CONSTRUCTION,

        /**
         * Legal and regulatory services.
         */
        LEGAL,

        /**
         * Military and defense services.
         */
        MILITARY,

        /**
         * Passenger transport services.
         */
        PASSENGER,

        /**
         * Information and intelligence services.
         */
        INFORMATION,

        /**
         * Support services for crew and ship maintenance.
         */
        SUPPORT;
    }
}
