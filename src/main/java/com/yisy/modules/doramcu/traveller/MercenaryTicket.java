package com.yisy.modules.doramcu.traveller;

/**
 * Mercenary ticket (contract) from Classic Traveller Book 4: Mercenary (1980).
 * <p>
 * A ticket is a legal contract between a client/patron and a mercenary leader
 * or corporation. It specifies conditions of payment, type of service,
 * financial support, and repatriation provisions.
 * </p>
 * <p>
 * Tickets are either Short Tickets (mercenary equips own force) or
 * Long Tickets (patron provides equipment).
 * </p>
 * <p>
 * Derived from Classic Traveller Book 4: Mercenary (1980).
 * </p>
 */
public class MercenaryTicket {

    /**
     * Payment type for a mercenary ticket.
     */
    public enum PaymentType {
        /**
         * Upfront money — paid in advance, somewhat less rewarding.
         */
        UPFRONT("Upfront"),
        /**
         * Success only — paid only on successful completion.
         */
        SUCCESS_ONLY("Success Only"),
        /**
         * Upfront with success bonus — base payment plus bonus for success.
         */
        UPFRONT_WITH_BONUS("Upfront with Bonus");

        private final String name;

        PaymentType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Ticket type (equipment provision).
     */
    public enum TicketType {
        /**
         * Short Ticket — mercenary is responsible for equipping the force.
         */
        SHORT("Short"),
        /**
         * Long Ticket — patron provides equipment; equipment remains patron's property.
         */
        LONG("Long");

        private final String name;

        TicketType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final int id;
    private final String name;
    private final String patron;
    private final MissionType missionType;
    private final PaymentType paymentType;
    private final TicketType ticketType;
    private final long basePayment;
    private final long successBonus;
    private final long equipmentBudget;
    private final long repatriationBond;
    private final int durationMonths;
    private final int requiredStrength;
    private final int techLevel;
    private final String description;
    private final String world;

    /**
     * Creates a new mercenary ticket.
     *
     * @param id the unique ticket identifier
     * @param name the ticket name
     * @param patron the client/patron name
     * @param missionType the type of mission
     * @param paymentType the payment type
     * @param ticketType short or long ticket
     * @param basePayment the base payment in CR
     * @param successBonus the success bonus in CR
     * @param equipmentBudget equipment budget for long tickets
     * @param repatriationBond the repatriation bond amount
     * @param durationMonths the mission duration in months
     * @param requiredStrength the required force strength
     * @param techLevel the tech level of the world
     * @param description the mission description
     * @param world the world name
     */
    public MercenaryTicket(int id, String name, String patron, MissionType missionType,
                           PaymentType paymentType, TicketType ticketType,
                           long basePayment, long successBonus, long equipmentBudget,
                           long repatriationBond, int durationMonths, int requiredStrength,
                           int techLevel, String description, String world) {
        this.id = id;
        this.name = name != null ? name : "Untitled Ticket";
        this.patron = patron;
        this.missionType = missionType != null ? missionType : MissionType.STRIKER;
        this.paymentType = paymentType != null ? paymentType : PaymentType.SUCCESS_ONLY;
        this.ticketType = ticketType != null ? ticketType : TicketType.SHORT;
        this.basePayment = basePayment;
        this.successBonus = successBonus;
        this.equipmentBudget = equipmentBudget;
        this.repatriationBond = repatriationBond;
        this.durationMonths = durationMonths;
        this.requiredStrength = requiredStrength;
        this.techLevel = techLevel;
        this.description = description;
        this.world = world;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPatron() { return patron; }
    public MissionType getMissionType() { return missionType; }
    public PaymentType getPaymentType() { return paymentType; }
    public TicketType getTicketType() { return ticketType; }
    public long getBasePayment() { return basePayment; }
    public long getSuccessBonus() { return successBonus; }
    public long getEquipmentBudget() { return equipmentBudget; }
    public long getRepatriationBond() { return repatriationBond; }
    public int getDurationMonths() { return durationMonths; }
    public int getRequiredStrength() { return requiredStrength; }
    public int getTechLevel() { return techLevel; }
    public String getDescription() { return description; }
    public String getWorld() { return world; }

    /**
     * Calculates the total potential payment (base + bonus).
     *
     * @return the total potential payment in CR
     */
    public long getTotalPotentialPayment() {
        return basePayment + successBonus;
    }

    /**
     * Calculates the total payroll cost for the ticket duration.
     *
     * @param strength the actual force strength
     * @return the total payroll cost in CR
     */
    public long calculatePayrollCost(int strength) {
        // Average salary ~CR 600 per soldier per month
        return (long) strength * 600 * durationMonths;
    }

    /**
     * Calculates the profit after payroll expenses.
     *
     * @param strength the actual force strength
     * @param successful whether the mission was successful
     * @return the profit in CR
     */
    public long calculateProfit(int strength, boolean successful) {
        long payment = basePayment;
        if (successful && (paymentType == PaymentType.SUCCESS_ONLY || paymentType == PaymentType.UPFRONT_WITH_BONUS)) {
            payment += successBonus;
        }
        long payroll = calculatePayrollCost(strength);
        // Ticket holder deducts 50% as profit
        long afterProfit = (long) ((payment - payroll) * 0.5);
        return Math.max(0, afterProfit);
    }

    @Override
    public String toString() {
        return String.format("Ticket[%d: %s, %s, %s, %s, CR %d]",
                id, name, missionType.getName(), paymentType.getName(),
                ticketType.getName(), basePayment);
    }
}
