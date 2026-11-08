package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.ShipDesignRequest;
import com.yisy.modules.doramcu.dto.ShipDesignResponse;
import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service layer for Traveller starship design management.
 * <p>
 * Provides CRUD operations for ship designs, validation, and conversion
 * between DTOs and domain models. Designs are stored in memory using
 * a concurrent map for thread-safe access.
 * </p>
 * <p>
 * This service bridges the REST API layer with the Traveller starship
 * design domain model, handling all design creation, modification,
 * and statistics calculation.
 * </p>
 */
@Service
public class ShipDesignService {

    private static final Logger log = LoggerFactory.getLogger(ShipDesignService.class);

    private final Map<Integer, ShipDesign> designs = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * Creates a new ship design from the given request.
     *
     * @param request the design parameters
     * @return the created design as a response DTO
     */
    public ShipDesignResponse createDesign(ShipDesignRequest request) {
        log.info("Creating ship design: {} ({})", request.getName(), request.getTotalTonnage() + " tons");

        int id = nextId.getAndIncrement();
        ShipDesign design = new ShipDesign(id, request.getName(), request.getTotalTonnage());

        // Set hull type
        if (request.getHullType() != null && !request.getHullType().isEmpty()) {
            design.setHullType(ShipHullType.valueOf(request.getHullType()));
        }

        // Set jump drive
        if (request.getJumpRating() != null) {
            design.setJumpDrive(JumpDrive.fromRating(request.getJumpRating()));
        }

        // Set fusion drive
        if (request.getManeuverRating() != null) {
            design.setFusionDrive(FusionDrive.fromRating(request.getManeuverRating()));
        }

        // Set power plant
        if (request.getPowerPlant() != null && !request.getPowerPlant().isEmpty()) {
            design.setPowerPlant(PowerPlant.valueOf(request.getPowerPlant()));
        }

        // Set life support
        if (request.getLifeSupport() != null && !request.getLifeSupport().isEmpty()) {
            design.setLifeSupport(LifeSupport.valueOf(request.getLifeSupport()));
        }

        // Set computer
        if (request.getComputerClass() != null) {
            design.setComputer(ShipComputer.fromClass(request.getComputerClass()));
        }

        // Set cargo
        if (request.getCargoTons() != null) {
            design.setCargoTons(request.getCargoTons());
        }

        // Set passengers
        if (request.getPassengerCount() != null) {
            design.setPassengerCount(request.getPassengerCount());
        }
        if (request.getPassengerClass() != null && !request.getPassengerClass().isEmpty()) {
            design.setPassengerClass(PassengerClass.valueOf(request.getPassengerClass()));
        }

        // Set fuel
        if (request.getFuelTons() != null) {
            design.setFuelTons(request.getFuelTons());
        }

        // Add weapons
        if (request.getWeapons() != null) {
            for (ShipDesignRequest.WeaponRequest weaponReq : request.getWeapons()) {
                ShipWeaponSystem weapon = createWeapon(weaponReq);
                if (weapon != null) {
                    design.addWeapon(weapon);
                }
            }
        }

        // Validate the design
        try {
            design.validate();
        } catch (IllegalStateException e) {
            log.warn("Design validation warning for {}: {}", request.getName(), e.getMessage());
        }

        designs.put(id, design);
        log.info("Created ship design ID {}: {}", id, design);
        return toResponse(design);
    }

    /**
     * Retrieves a ship design by ID.
     *
     * @param id the design ID
     * @return the design as a response DTO, or null if not found
     */
    public ShipDesignResponse getDesign(int id) {
        ShipDesign design = designs.get(id);
        if (design == null) {
            log.warn("Ship design not found: {}", id);
            return null;
        }
        return toResponse(design);
    }

    /**
     * Retrieves the domain ShipDesign object by ID.
     *
     * @param id the design ID
     * @return the domain ShipDesign object, or null if not found
     */
    public ShipDesign getDesignDomain(int id) {
        return designs.get(id);
    }

    /**
     * Returns all ship designs.
     *
     * @return list of all designs as response DTOs
     */
    public List<ShipDesignResponse> getAllDesigns() {
        return designs.values().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing ship design.
     *
     * @param id the design ID
     * @param request the updated design parameters
     * @return the updated design as a response DTO, or null if not found
     */
    public ShipDesignResponse updateDesign(int id, ShipDesignRequest request) {
        ShipDesign design = designs.get(id);
        if (design == null) {
            log.warn("Cannot update ship design: not found ({})", id);
            return null;
        }

        log.info("Updating ship design ID {}: {}", id, request.getName());

        // Update name
        if (request.getName() != null) {
            design.setName(request.getName());
        }

        // Update hull type
        if (request.getHullType() != null && !request.getHullType().isEmpty()) {
            design.setHullType(ShipHullType.valueOf(request.getHullType()));
        }

        // Update jump drive
        if (request.getJumpRating() != null) {
            design.setJumpDrive(JumpDrive.fromRating(request.getJumpRating()));
        }

        // Update fusion drive
        if (request.getManeuverRating() != null) {
            design.setFusionDrive(FusionDrive.fromRating(request.getManeuverRating()));
        }

        // Update power plant
        if (request.getPowerPlant() != null && !request.getPowerPlant().isEmpty()) {
            design.setPowerPlant(PowerPlant.valueOf(request.getPowerPlant()));
        }

        // Update life support
        if (request.getLifeSupport() != null && !request.getLifeSupport().isEmpty()) {
            design.setLifeSupport(LifeSupport.valueOf(request.getLifeSupport()));
        }

        // Update computer
        if (request.getComputerClass() != null) {
            design.setComputer(ShipComputer.fromClass(request.getComputerClass()));
        }

        // Update cargo
        if (request.getCargoTons() != null) {
            design.setCargoTons(request.getCargoTons());
        }

        // Update passengers
        if (request.getPassengerCount() != null) {
            design.setPassengerCount(request.getPassengerCount());
        }
        if (request.getPassengerClass() != null && !request.getPassengerClass().isEmpty()) {
            design.setPassengerClass(PassengerClass.valueOf(request.getPassengerClass()));
        }

        // Update fuel
        if (request.getFuelTons() != null) {
            design.setFuelTons(request.getFuelTons());
        }

        // Update weapons (replace all)
        if (request.getWeapons() != null) {
            // Remove existing weapons
            List<ShipWeaponSystem> existing = new ArrayList<>(design.getWeapons());
            for (ShipWeaponSystem weapon : existing) {
                design.removeWeapon(weapon);
            }

            // Add new weapons
            for (ShipDesignRequest.WeaponRequest weaponReq : request.getWeapons()) {
                ShipWeaponSystem weapon = createWeapon(weaponReq);
                if (weapon != null) {
                    design.addWeapon(weapon);
                }
            }
        }

        // Validate
        try {
            design.validate();
        } catch (IllegalStateException e) {
            log.warn("Design validation warning for ID {}: {}", id, e.getMessage());
        }

        log.info("Updated ship design ID {}: {}", id, design);
        return toResponse(design);
    }

    /**
     * Deletes a ship design by ID.
     *
     * @param id the design ID
     * @return true if the design was deleted, false if not found
     */
    public boolean deleteDesign(int id) {
        ShipDesign removed = designs.remove(id);
        if (removed != null) {
            log.info("Deleted ship design ID {}: {}", id, removed.getName());
            return true;
        }
        log.warn("Cannot delete ship design: not found ({})", id);
        return false;
    }

    /**
     * Creates a preset ship design based on Classic Traveller archetypes.
     *
     * @param preset the preset type (e.g., "DESTROYER", "MERCHANT", "CORVETTE")
     * @return the created design as a response DTO, or null if preset not found
     */
    public ShipDesignResponse createPreset(String preset) {
        log.info("Creating preset ship design: {}", preset);

        ShipDesign design;
        switch (preset.toUpperCase()) {
            case "DESTROYER":
                design = createDestroyerPreset();
                break;
            case "MERCHANT":
                design = createMerchantPreset();
                break;
            case "CORVETTE":
                design = createCorvettePreset();
                break;
            case "CRUISER":
                design = createCruiserPreset();
                break;
            default:
                log.warn("Unknown preset: {}", preset);
                return null;
        }

        int id = nextId.getAndIncrement();
        // Re-create with proper ID
        design = new ShipDesign(id, design.getName(), design.getTotalTonnage());
        design.setHullType(design.getHullType());
        design.setJumpDrive(design.getJumpDrive());
        design.setFusionDrive(design.getFusionDrive());
        design.setPowerPlant(design.getPowerPlant());
        design.setLifeSupport(design.getLifeSupport());
        design.setComputer(design.getComputer());
        design.setCargoTons(design.getCargoTons());
        design.setPassengerCount(design.getPassengerCount());
        design.setPassengerClass(design.getPassengerClass());
        design.setFuelTons(design.getFuelTons());

        designs.put(id, design);
        log.info("Created preset design ID {}: {}", id, design);
        return toResponse(design);
    }

    // ---- Private helpers ----

    private ShipWeaponSystem createWeapon(ShipDesignRequest.WeaponRequest req) {
        try {
            ShipWeaponSystem.WeaponType type = ShipWeaponSystem.WeaponType.valueOf(req.getWeaponType());

            if (type == ShipWeaponSystem.WeaponType.MISSILE_LAUNCHER) {
                if (req.getMissileType() == null || req.getMissileType().isEmpty()) {
                    return null;
                }
                ShipWeaponSystem.MissileType missileType =
                        ShipWeaponSystem.MissileType.valueOf(req.getMissileType());
                int pods = req.getMissilePods() != null ? req.getMissilePods() : 0;
                return new ShipWeaponSystem(missileType, req.getQuantity(), pods, req.getHitLocation());
            } else {
                return new ShipWeaponSystem(type, req.getQuantity(), req.getHitLocation());
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid weapon type: {}", req.getWeaponType());
            return null;
        }
    }

    private ShipDesignResponse toResponse(ShipDesign design) {
        ShipDesignStats stats = new ShipDesignStats(design);

        List<ShipDesignResponse.WeaponResponse> weaponResponses = design.getWeapons().stream()
                .map(w -> new ShipDesignResponse.WeaponResponse(
                        w.getWeaponType().name(),
                        w.getQuantity(),
                        w.getMissileType() != null ? w.getMissileType().name() : null,
                        w.getHitLocation(),
                        w.getDamageDice(),
                        w.getTotalTons()
                ))
                .collect(Collectors.toList());

        ShipDesignResponse.DesignStats statsDto = new ShipDesignResponse.DesignStats(
                stats.getFixedSystemTons(),
                stats.getWeaponTons(),
                stats.getPassengerTons(),
                stats.getUnallocatedTons(),
                stats.getHullPoints(),
                stats.getEstimatedCrew(),
                stats.getConstructionCost(),
                stats.getAnnualMaintenanceCost(),
                stats.getTotalDamageDice(),
                stats.getAverageDamage(),
                stats.getGunneryBonus(),
                stats.isValid()
        );

        return new ShipDesignResponse(
                design.getId(),
                design.getName(),
                design.getTotalTonnage(),
                design.getHullType().name(),
                design.getJumpDrive().getRating(),
                design.getManeuverRating(),
                design.getPowerPlant().name(),
                design.getLifeSupport().name(),
                design.getComputer().getClassLevel(),
                design.getCargoTons(),
                design.getPassengerCount(),
                design.getPassengerClass().name(),
                design.getFuelTons(),
                weaponResponses,
                statsDto
        );
    }

    private ShipDesign createDestroyerPreset() {
        ShipDesign design = new ShipDesign(0, "Destroyer", 10000);
        design.setHullType(ShipHullType.REINFORCED);
        design.setJumpDrive(JumpDrive.fromRating(3));
        design.setFusionDrive(FusionDrive.fromRating(2));
        design.setPowerPlant(PowerPlant.DOUBLE);
        design.setLifeSupport(LifeSupport.STANDARD);
        design.setComputer(ShipComputer.fromClass(2));
        design.setFuelTons(1000);
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 4, "A1"));
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 2, "A6"));
        return design;
    }

    private ShipDesign createMerchantPreset() {
        ShipDesign design = new ShipDesign(0, "Merchant", 20000);
        design.setHullType(ShipHullType.STANDARD);
        design.setJumpDrive(JumpDrive.fromRating(4));
        design.setFusionDrive(FusionDrive.fromRating(1));
        design.setPowerPlant(PowerPlant.STANDARD);
        design.setLifeSupport(LifeSupport.STANDARD);
        design.setComputer(ShipComputer.fromClass(1));
        design.setCargoTons(5000);
        design.setFuelTons(2000);
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.WeaponType.SMALL_LASER, 2, "A1"));
        return design;
    }

    private ShipDesign createCorvettePreset() {
        ShipDesign design = new ShipDesign(0, "Corvette", 2000);
        design.setHullType(ShipHullType.STANDARD);
        design.setJumpDrive(JumpDrive.fromRating(2));
        design.setFusionDrive(FusionDrive.fromRating(1));
        design.setPowerPlant(PowerPlant.STANDARD);
        design.setLifeSupport(LifeSupport.STANDARD);
        design.setComputer(ShipComputer.fromClass(1));
        design.setFuelTons(200);
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.WeaponType.SMALL_LASER, 1, "A1"));
        return design;
    }

    private ShipDesign createCruiserPreset() {
        ShipDesign design = new ShipDesign(0, "Cruiser", 40000);
        design.setHullType(ShipHullType.REINFORCED);
        design.setJumpDrive(JumpDrive.fromRating(4));
        design.setFusionDrive(FusionDrive.fromRating(3));
        design.setPowerPlant(PowerPlant.TRIPLE);
        design.setLifeSupport(LifeSupport.VACUUM);
        design.setComputer(ShipComputer.fromClass(3));
        design.setFuelTons(4000);
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.WeaponType.LARGE_LASER, 6, "A1"));
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.WeaponType.LARGE_LASER, 2, "A6"));
        design.addWeapon(new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 2, 12, "B1"));
        return design;
    }
}

