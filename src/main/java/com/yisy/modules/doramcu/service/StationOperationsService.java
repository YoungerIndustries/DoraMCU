package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.StationRequest;
import com.yisy.modules.doramcu.dto.StationResponse;
import com.yisy.modules.doramcu.traveller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for space station operations.
 */
@Service
public class StationOperationsService {

    private static final Logger log = LoggerFactory.getLogger(StationOperationsService.class);

    private final Map<Integer, SpaceStation> stations = new HashMap<>();
    private final Map<Integer, FuelTank> fuelTanks = new HashMap<>();

    /**
     * Registers a station for operations.
     */
    public void registerStation(SpaceStation station) {
        stations.put(station.getWorld().getId(), station);
    }

    /**
     * Gets station information.
     */
    public StationResponse getStationInfo(int worldId) {
        SpaceStation station = stations.get(worldId);
        if (station == null) {
            throw new IllegalArgumentException("Station not found for world: " + worldId);
        }

        StationResponse response = new StationResponse();
        response.setOperation("GET_INFO");
        response.setStationName(station.getName());
        response.setWorldName(station.getWorld().getName());
        response.setStationType(station.getStationType().getName());
        response.setOperational(station.isOperational());
        response.setDockedShips(station.getDockedShipCount());
        response.setMaxShips(station.getStationType().getMaxShips());
        response.setDockFull(station.isDockFull());

        List<String> facilities = new ArrayList<>();
        for (StationFacility f : station.getFacilities()) {
            facilities.add(f.getName());
        }
        response.setFacilities(facilities);

        List<String> dockedShips = station.getDock().getDockedShipNames();
        response.setDockedShipNames(dockedShips);

        response.setMessage(station.getName() + " at " + station.getWorld().getName() +
                " — " + station.getStationType() + " station with " +
                facilities.size() + " facilities");

        return response;
    }

    /**
     * Docks a ship at the station.
     */
    public StationResponse dockShip(StationRequest request) {
        SpaceStation station = stations.get(request.getWorldId());
        if (station == null) {
            throw new IllegalArgumentException("Station not found for world: " + request.getWorldId());
        }

        boolean success = station.dockShip(request.getShipName(), request.getShipTonnage());

        StationResponse response = new StationResponse();
        response.setOperation("DOCK");
        response.setStationName(station.getName());
        response.setWorldName(station.getWorld().getName());
        response.setShipName(request.getShipName());
        response.setSuccess(success);
        response.setDockedShips(station.getDockedShipCount());
        response.setDockFull(station.isDockFull());

        response.setMessage(success ?
                request.getShipName() + " docked at " + station.getName() :
                "Failed to dock " + request.getShipName() + " at " + station.getName());

        return response;
    }

    /**
     * Undocks a ship from the station.
     */
    public StationResponse undockShip(StationRequest request) {
        SpaceStation station = stations.get(request.getWorldId());
        if (station == null) {
            throw new IllegalArgumentException("Station not found for world: " + request.getWorldId());
        }

        boolean success = station.undockShip(request.getShipName());

        StationResponse response = new StationResponse();
        response.setOperation("UNDOCK");
        response.setStationName(station.getName());
        response.setWorldName(station.getWorld().getName());
        response.setShipName(request.getShipName());
        response.setSuccess(success);
        response.setDockedShips(station.getDockedShipCount());

        response.setMessage(success ?
                request.getShipName() + " undocked from " + station.getName() :
                "Failed to undock " + request.getShipName() + " from " + station.getName());

        return response;
    }

    /**
     * Refuels a ship at the station.
     */
    public StationResponse refuelShip(StationRequest request) {
        SpaceStation station = stations.get(request.getWorldId());
        if (station == null) {
            throw new IllegalArgumentException("Station not found for world: " + request.getWorldId());
        }

        FuelTank fuelTank = getOrCreateFuelTank(request.getShipDesignId());
        int fuelBefore = fuelTank.getCurrentFuelTons();

        int cost = station.refuel(fuelTank, request.isFullTank());
        int fuelAdded = fuelTank.getCurrentFuelTons() - fuelBefore;

        StationResponse response = new StationResponse();
        response.setOperation("REFUEL");
        response.setStationName(station.getName());
        response.setWorldName(station.getWorld().getName());
        response.setSuccess(true);
        response.setCost(cost);
        response.setFuelAdded(fuelAdded);

        response.setMessage("Refueled at " + station.getName() +
                ": added " + fuelAdded + " tons fuel for " + cost + " credits");

        return response;
    }

    /**
     * Repairs a ship at the station.
     */
    public StationResponse repairShip(StationRequest request) {
        SpaceStation station = stations.get(request.getWorldId());
        if (station == null) {
            throw new IllegalArgumentException("Station not found for world: " + request.getWorldId());
        }

        int hullRepaired = request.getMaxHull() - request.getCurrentHull();
        int cost = station.repairHull(request.getCurrentHull(), request.getMaxHull());

        StationResponse response = new StationResponse();
        response.setOperation("REPAIR");
        response.setStationName(station.getName());
        response.setWorldName(station.getWorld().getName());
        response.setSuccess(true);
        response.setCost(cost);
        response.setHullRepaired(hullRepaired);

        response.setMessage("Repaired at " + station.getName() +
                ": restored " + hullRepaired + " hull points for " + cost + " credits");

        return response;
    }

    private FuelTank getOrCreateFuelTank(int shipDesignId) {
        return fuelTanks.computeIfAbsent(shipDesignId,
                id -> new FuelTank(FusionDrive.fromRating(2), 2000));
    }
}
