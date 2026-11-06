package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.yisy.modules.doramcu.hex.Fleet;

/**
 * Manages the entire resource pipeline for a player.
 * <p>
 * The pipeline has five stages:
 * <ol>
 *   <li>Mining: Extract raw resources from celestial bodies</li>
 *   <li>Transport: Move raw resources between locations</li>
 *   <li>Processing: Convert raw resources to refined resources</li>
 *   <li>Assembly: Build modules from refined resources</li>
 *   <li>Construction: Build ships and facilities from modules</li>
 * </ol>
 * <p>
 * This manager maintains the central storage for raw and refined resources,
 * and coordinates mining operations, processing facilities, and module assembly.
 * </p>
 */
public class ResourcePipelineManager {

    private final int ownerEmpireId;

    // Mining operations at celestial bodies
    private final Map<Integer, MiningOperation> miningOperations;

    // Central raw resource storage (aggregated from mining)
    private final ResourceStorage<RawResourceType> rawStorage;

    // Processing facilities
    private final List<ProcessingFacility> processingFacilities;

    // Central refined resource storage
    private final ResourceStorage<RefinedResourceType> refinedStorage;

    // Transport orders for moving resources between locations
    private final List<TransportOrder> transportOrders;
    private final AtomicInteger nextTransportOrderId;

    // Module assembly
    private final List<ModuleAssembler> assemblers;

    // Module inventory
    private final ResourceStorage<ConstructionModuleType> moduleStorage;

    // Construction queue for multi-turn facility builds
    private final List<ConstructionOrder> constructionQueue;
    private final AtomicInteger nextConstructionOrderId;

    /**
     * Creates a new resource pipeline manager for the given empire.
     *
     * @param ownerEmpireId the empire ID that owns this pipeline
     */
    public ResourcePipelineManager(int ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
        this.miningOperations = new HashMap<>();
        this.rawStorage = new ResourceStorage<>(RawResourceType.class, 500);
        this.processingFacilities = new ArrayList<>();
        this.refinedStorage = new ResourceStorage<>(RefinedResourceType.class, 200);
        this.transportOrders = new ArrayList<>();
        this.nextTransportOrderId = new AtomicInteger(1);
        this.assemblers = new ArrayList<>();
        this.moduleStorage = new ResourceStorage<>(ConstructionModuleType.class, 50);
        this.constructionQueue = new ArrayList<>();
        this.nextConstructionOrderId = new AtomicInteger(1);
    }

    /** @return the empire ID that owns this pipeline */
    public int getOwnerEmpireId() {
        return ownerEmpireId;
    }

    // ---- Mining ----

    /**
     * Adds a mining operation at the given celestial body.
     *
     * @param celestialBody the celestial body to mine
     * @return the created mining operation
     */
    public MiningOperation addMiningOperation(CelestialBody celestialBody) {
        return addMiningOperation(celestialBody, 1);
    }

    /**
     * Adds a mining operation at the given celestial body with a mining bonus.
     *
     * @param celestialBody the celestial body to mine
     * @param miningBonus the mining bonus multiplier from nearby stations (1 = no bonus)
     * @return the created mining operation
     */
    public MiningOperation addMiningOperation(CelestialBody celestialBody, int miningBonus) {
        MiningOperation operation = new MiningOperation(celestialBody, ownerEmpireId, miningBonus);
        miningOperations.put(celestialBody.getId(), operation);
        return operation;
    }

    /**
     * Removes the mining operation at the given celestial body.
     *
     * @param celestialBodyId the celestial body ID
     */
    public void removeMiningOperation(int celestialBodyId) {
        miningOperations.remove(celestialBodyId);
    }

    /**
     * Returns all mining operations.
     */
    public Map<Integer, MiningOperation> getMiningOperations() {
        return Map.copyOf(miningOperations);
    }

    // ---- Transport ----

    /**
     * Creates a transport order to move raw resources from one location to another.
     *
     * @param sourceBodyId the source celestial body ID
     * @param destinationBodyId the destination celestial body ID
     * @param resourceType the raw resource type to transport
     * @param amount the amount to transport
     * @param distance the distance between source and destination
     * @return the created transport order
     */
    public TransportOrder createTransportOrder(int sourceBodyId, int destinationBodyId,
                                                RawResourceType resourceType, int amount, int distance) {
        int orderId = nextTransportOrderId.getAndIncrement();
        TransportOrder order = new TransportOrder(orderId, ownerEmpireId, sourceBodyId,
                destinationBodyId, resourceType, amount, distance);
        transportOrders.add(order);
        return order;
    }

    /**
     * Cancels a transport order by ID.
     *
     * @param orderId the order ID to cancel
     * @return true if the order was found and cancelled
     */
    public boolean cancelTransportOrder(int orderId) {
        for (TransportOrder order : transportOrders) {
            if (order.getId() == orderId && order.isInProgress()) {
                order.cancel();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all active transport orders.
     */
    public List<TransportOrder> getActiveTransportOrders() {
        List<TransportOrder> active = new ArrayList<>();
        for (TransportOrder order : transportOrders) {
            if (order.isInProgress()) {
                active.add(order);
            }
        }
        return active;
    }

    /**
     * Returns all transport orders (including completed/cancelled).
     */
    public List<TransportOrder> getTransportOrders() {
        return List.copyOf(transportOrders);
    }

    // ---- Processing ----

    /**
     * Adds a processing facility.
     *
     * @param facility the processing facility to add
     */
    public void addProcessingFacility(ProcessingFacility facility) {
        processingFacilities.add(facility);
    }

    /**
     * Returns all processing facilities.
     */
    public List<ProcessingFacility> getProcessingFacilities() {
        return List.copyOf(processingFacilities);
    }

    // ---- Assembly ----

    /**
     * Adds a module assembler.
     *
     * @param assembler the assembler to add
     */
    public void addAssembler(ModuleAssembler assembler) {
        assemblers.add(assembler);
    }

    /**
     * Returns all module assemblers.
     */
    public List<ModuleAssembler> getAssemblers() {
        return List.copyOf(assemblers);
    }

    // ---- Storage Access ----

    /** @return the central raw resource storage */
    public ResourceStorage<RawResourceType> getRawStorage() {
        return rawStorage;
    }

    /** @return the central refined resource storage */
    public ResourceStorage<RefinedResourceType> getRefinedStorage() {
        return refinedStorage;
    }

    /** @return the module inventory */
    public ResourceStorage<ConstructionModuleType> getModuleStorage() {
        return moduleStorage;
    }

    // ---- Turn Processing ----

    /**
     * Processes one turn of the entire pipeline.
     * <p>
     * Order: mining → transport → processing → assembly
     * </p>
     */
    public void processTurn() {
        // 1. Mining: extract raw resources
        for (MiningOperation operation : miningOperations.values()) {
            operation.processTurn();
        }

        // 2. Transport: process transport orders and deliver resources
        processTransportOrders();

        // 3. Aggregate raw resources from mining to central storage
        for (MiningOperation operation : miningOperations.values()) {
            for (RawResourceType type : RawResourceType.values()) {
                operation.transferTo(type, rawStorage);
            }
        }

        // 4. Processing: convert raw to refined
        for (ProcessingFacility facility : processingFacilities) {
            facility.processTurn(rawStorage, refinedStorage);
        }

        // 5. Assembly: build modules from refined resources
        for (ModuleAssembler assembler : assemblers) {
            assembler.processTurn(refinedStorage, moduleStorage);
        }

        // 6. Clean up completed/cancelled transport orders
        transportOrders.removeIf(order -> !order.isInProgress());

        // 7. Process construction queue — advance each order by one turn
        processConstructionQueue();
    }

    /**
     * Processes the construction queue, advancing each order by one turn.
     * Completed and cancelled orders are removed from the queue.
     *
     * @return the list of orders that just completed this turn
     */
    public List<ConstructionOrder> processConstructionQueue() {
        List<ConstructionOrder> completed = new ArrayList<>();
        for (ConstructionOrder order : constructionQueue) {
            order.processTurn();
            if (order.isCompleted()) {
                completed.add(order);
            }
        }
        constructionQueue.removeIf(order -> order.isCompleted() || order.isCancelled());
        return completed;
    }

    /**
     * Processes all active transport orders for one turn.
     */
    private void processTransportOrders() {
        for (TransportOrder order : transportOrders) {
            if (order.isInProgress()) {
                order.processTurn();
            }
        }
    }

    // ---- Construction ----

    /**
     * Checks if the player can build a ship with the given module requirements.
     *
     * @param requiredModules the modules required for the ship
     * @return true if sufficient modules are available
     */
    public boolean canBuildWithModules(Map<ConstructionModuleType, Integer> requiredModules) {
        for (Map.Entry<ConstructionModuleType, Integer> entry : requiredModules.entrySet()) {
            if (moduleStorage.getAmount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Consumes modules for ship construction.
     *
     * @param requiredModules the modules to consume
     * @return true if all modules were consumed successfully
     */
    public boolean consumeModules(Map<ConstructionModuleType, Integer> requiredModules) {
        if (!canBuildWithModules(requiredModules)) {
            return false;
        }
        for (Map.Entry<ConstructionModuleType, Integer> entry : requiredModules.entrySet()) {
            moduleStorage.remove(entry.getKey(), entry.getValue());
        }
        return true;
    }

    // ---- Construction Queue ----

    /**
     * Default construction time for a space station (in turns).
     */
    public static final int DEFAULT_STATION_TURNS = 3;

    /**
     * Default construction time for a planetary facility (in turns).
     */
    public static final int DEFAULT_FACILITY_TURNS = 2;

    /**
     * Queues a space station for multi-turn construction.
     * <p>
     * Modules are consumed immediately, and the station is created when
     * the construction order completes after the specified number of turns.
     * </p>
     *
     * @param stationType the type of station to build
     * @param targetSystem the solar system entity where the station will be placed
     * @param moduleRequirements the modules required for construction
     * @param turns the number of turns to complete (use DEFAULT_STATION_TURNS for default)
     * @return the construction order, or null if modules are insufficient
     */
    public ConstructionOrder queueStation(SpaceStationType stationType,
                                           SolarSystemEntity targetSystem,
                                           Map<ConstructionModuleType, Integer> moduleRequirements,
                                           int turns) {
        if (!canBuildWithModules(moduleRequirements)) {
            return null;
        }
        consumeModules(moduleRequirements);
        int orderId = nextConstructionOrderId.getAndIncrement();
        ConstructionOrder order = new ConstructionOrder(orderId, ownerEmpireId, stationType,
                targetSystem, new EnumMap<>(moduleRequirements), turns);
        constructionQueue.add(order);
        return order;
    }

    /**
     * Queues a planetary facility for multi-turn construction.
     *
     * @param facility the facility type to build
     * @param targetPlanetId the planet to build on
     * @param moduleRequirements the modules required for construction
     * @param turns the number of turns to complete (use DEFAULT_FACILITY_TURNS for default)
     * @return the construction order, or null if modules are insufficient
     */
    public ConstructionOrder queuePlanetFacility(StartingFacility facility,
                                                  Integer targetPlanetId,
                                                  Map<ConstructionModuleType, Integer> moduleRequirements,
                                                  int turns) {
        if (!canBuildWithModules(moduleRequirements)) {
            return null;
        }
        consumeModules(moduleRequirements);
        int orderId = nextConstructionOrderId.getAndIncrement();
        ConstructionOrder order = new ConstructionOrder(orderId, ownerEmpireId, facility,
                targetPlanetId, new EnumMap<>(moduleRequirements), turns);
        constructionQueue.add(order);
        return order;
    }

    /**
     * Cancels a construction order by ID.
     * <p>
     * Modules are NOT refunded when an order is cancelled.
     * </p>
     *
     * @param orderId the order to cancel
     * @return true if the order was found and cancelled
     */
    public boolean cancelConstruction(int orderId) {
        for (ConstructionOrder order : constructionQueue) {
            if (order.getOrderId() == orderId) {
                order.cancel();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all active construction orders.
     */
    public List<ConstructionOrder> getConstructionQueue() {
        return List.copyOf(constructionQueue);
    }

    /**
     * Returns the number of active construction orders.
     */
    public int getConstructionQueueSize() {
        return constructionQueue.size();
    }

    /**
     * Returns the aggregate production rate per turn for all mining operations.
     * Sums the production rates across all mining operations for each raw resource type.
     *
     * @return map of raw resource types to total production per turn
     */
    public Map<RawResourceType, Integer> getAggregateMiningRate() {
        Map<RawResourceType, Integer> total = new EnumMap<>(RawResourceType.class);
        for (MiningOperation operation : miningOperations.values()) {
            for (Map.Entry<RawResourceType, Integer> entry : operation.getProductionRate().entrySet()) {
                total.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return total;
    }

    /**
     * Returns the number of active mining operations.
     */
    public int getMiningOperationCount() {
        return miningOperations.size();
    }

    /**
     * Returns the number of processing facilities currently processing.
     */
    public int getActiveProcessingCount() {
        int count = 0;
        for (ProcessingFacility facility : processingFacilities) {
            if (facility.isProcessing()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of module assemblers currently assembling.
     */
    public int getActiveAssemblerCount() {
        int count = 0;
        for (ModuleAssembler assembler : assemblers) {
            if (assembler.isAssembling()) {
                count++;
            }
        }
        return count;
    }

    // ---- Ship Maintenance ----

    /**
     * Processes ongoing ship fuel and maintenance costs for all ships in the given fleets.
     * <p>
     * Each ship consumes GAS fuel based on its type, and may consume HULL and SHIELD
     * modules if damaged. Resources are consumed from the raw and module storage.
     * If resources are insufficient, the ship still operates but may degrade over time.
     * </p>
     *
     * @param fleets the fleets whose ships should consume resources
     * @return the total fuel consumed this turn
     */
    public int processShipMaintenance(List<Fleet> fleets) {
        int totalFuelConsumed = 0;
        int totalModulesConsumed = 0;

        for (Fleet fleet : fleets) {
            for (Ship ship : fleet.getShips()) {
                // Skip destroyed ships
                if (ship.isDestroyed()) {
                    continue;
                }

                // Consume fuel (GAS) from raw storage
                int fuelNeeded = ship.getFuelConsumptionPerTurn();
                int fuelConsumed = rawStorage.remove(RawResourceType.GAS, fuelNeeded);
                totalFuelConsumed += fuelConsumed;

                // Consume maintenance modules from module storage
                java.util.Map<ConstructionModuleType, Integer> maintenanceCost = ship.getMaintenanceCostPerTurn();
                for (java.util.Map.Entry<ConstructionModuleType, Integer> entry : maintenanceCost.entrySet()) {
                    int consumed = moduleStorage.remove(entry.getKey(), entry.getValue());
                    totalModulesConsumed += consumed;
                }
            }
        }

        return totalFuelConsumed;
    }

    /**
     * Returns a summary of the entire pipeline.
     */
    public String getSummary() {
        int activeTransports = (int) transportOrders.stream().filter(TransportOrder::isInProgress).count();
        return String.format(
                "Pipeline{empire=%d, mining=%d, transports=%d, raw=%s, processing=%d, refined=%s, assemblers=%d, modules=%s, construction=%d}",
                ownerEmpireId,
                miningOperations.size(),
                activeTransports,
                rawStorage.getTotalAmount(),
                processingFacilities.size(),
                refinedStorage.getTotalAmount(),
                assemblers.size(),
                moduleStorage.getTotalAmount(),
                constructionQueue.size()
        );
    }
}
