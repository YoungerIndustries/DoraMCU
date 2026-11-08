package com.yisy.modules.doramcu.traveller;

/**
 * Passenger comfort levels from Classic Traveller Book 2.
 * <p>
 * Passengers consume cargo tonnage based on their comfort level.
 * Higher comfort levels provide better living conditions but consume
 * more cargo space per passenger.
 * </p>
 */
public enum PassengerClass {

    /**
     * Troop — minimal space for military personnel.
     * 1 ton per 10 passengers.
     */
    TROOP(10, 1),

    /**
     * Refugee — basic shelter for displaced persons.
     * 1 ton per 5 passengers.
     */
    REFUGEE(5, 1),

    /**
     * Third class — economy travel with shared facilities.
     * 1 ton per passenger.
     */
    THIRD(1, 1),

    /**
     * Second class — comfortable travel with private cabins.
     * 2 tons per passenger.
     */
    SECOND(1, 2),

    /**
     * First class — luxury travel with premium amenities.
     * 5 tons per passenger.
     */
    FIRST(1, 5),

    /**
     * VIP — maximum luxury for important persons.
     * 10 tons per passenger.
     */
    VIP(1, 10);

    private final int passengersPerTon;
    private final int tonsPerPassenger;

    PassengerClass(int passengersPerTon, int tonsPerPassenger) {
        this.passengersPerTon = passengersPerTon;
        this.tonsPerPassenger = tonsPerPassenger;
    }

    /**
     * Returns the number of passengers that can be supported per ton.
     */
    public int getPassengersPerTon() {
        return passengersPerTon;
    }

    /**
     * Returns the tons consumed per passenger.
     */
    public int getTonsPerPassenger() {
        return tonsPerPassenger;
    }

    /**
     * Calculates the tons required for the given number of passengers.
     *
     * @param passengerCount the number of passengers
     * @return tons required
     */
    public int getTonsRequired(int passengerCount) {
        return passengerCount * tonsPerPassenger;
    }

    /**
     * Calculates the maximum passengers that can be carried in the given tons.
     *
     * @param availableTons the available cargo tons
     * @return maximum passengers
     */
    public int getMaxPassengers(int availableTons) {
        return availableTons / tonsPerPassenger;
    }
}
