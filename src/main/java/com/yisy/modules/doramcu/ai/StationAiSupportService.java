package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.SpaceStation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides tactical support from space stations with AI Cores to nearby fleets.
 * <p>
 * When a fleet is within the support radius of a station with an AI Core,
 * it receives various bonuses based on the station type and AI Core level:
 * <ul>
 *   <li><b>Military stations</b>: Defense bonus, threat assessment</li>
 *   <li><b>Research stations</b>: Intelligence bonus, target analysis</li>
 *   <li><b>Trade stations</b>: Economic efficiency bonus</li>
 *   <li><b>Mining stations</b>: Resource extraction bonus</li>
 *   <li><b>Shipyard stations</b>: Repair and assembly bonus</li>
 *   <li><b>Colony stations</b>: Population support bonus</li>
 * </ul>
 * </p>
 * <p>
 * A fleet can receive support from multiple stations simultaneously,
 * with bonuses stacking up to a maximum cap.
 * </p>
 */
public class StationAiSupportService {

    private static final double MAX_COMBINED_BONUS = 0.50; // Cap at 50% total bonus

    /**
     * Returns the list of stations providing support to the given fleet.
     * <p>
     * Only stations with AI Cores installed and within their support radius
     * are included.
     * </p>
     *
     * @param fleet the fleet to check
     * @param stations the list of all stations to check
     * @return list of stations providing support
     */
    public List<SpaceStation> getSupportingStations(Fleet fleet, List<SpaceStation> stations) {
        if (fleet == null || stations == null || fleet.getPosition() == null) {
            return Collections.emptyList();
        }

        List<SpaceStation> supporting = new ArrayList<>();
        Hex3D fleetPos = fleet.getPosition();

        for (SpaceStation station : stations) {
            if (station.hasAiCore() && station.getPosition() != null) {
                StationAiCore aiCore = station.getAiCore();
                if (aiCore.isFleetInRange(fleet)) {
                    supporting.add(station);
                }
            }
        }

        return Collections.unmodifiableList(supporting);
    }

    /**
     * Returns the total defense bonus provided to the fleet by all supporting stations.
     * <p>
     * Bonuses from multiple stations stack, up to the maximum combined bonus cap.
     * </p>
     *
     * @param fleet the fleet to check
     * @param stations the list of all stations
     * @return the total defense bonus (0.0 to 0.50)
     */
    public double getTotalDefenseBonus(Fleet fleet, List<SpaceStation> stations) {
        List<SpaceStation> supporting = getSupportingStations(fleet, stations);
        if (supporting.isEmpty()) {
            return 0.0;
        }

        double totalBonus = 0.0;
        for (SpaceStation station : supporting) {
            totalBonus += station.getAiCore().getFleetSupportBonus(fleet);
        }

        return Math.min(totalBonus, MAX_COMBINED_BONUS);
    }

    /**
     * Returns the total tactical support bonus for the fleet.
     * <p>
     * This combines defense bonuses from military stations with other
     * station-type-specific bonuses.
     * </p>
     *
     * @param fleet the fleet to check
     * @param stations the list of all stations
     * @return the total tactical support bonus (0.0 to 0.50)
     */
    public double getTotalTacticalBonus(Fleet fleet, List<SpaceStation> stations) {
        List<SpaceStation> supporting = getSupportingStations(fleet, stations);
        if (supporting.isEmpty()) {
            return 0.0;
        }

        double totalBonus = 0.0;
        for (SpaceStation station : supporting) {
            StationAiCore aiCore = station.getAiCore();
            double bonus = aiCore.getFleetSupportBonus(fleet);
            totalBonus += bonus;
        }

        return Math.min(totalBonus, MAX_COMBINED_BONUS);
    }

    /**
     * Returns the threat assessment for a fleet based on nearby military stations.
     * <p>
     * Military stations with AI Cores can detect and report enemy fleets
     * within their support radius.
     * </p>
     *
     * @param fleet the fleet to assess
     * @param stations the list of all stations
     * @param context the current decision context
     * @return list of threats detected by supporting stations
     */
    public List<StationAiInsight> getThreatAssessment(Fleet fleet, List<SpaceStation> stations, AiDecisionContext context) {
        if (fleet == null || stations == null || context == null) {
            return Collections.emptyList();
        }

        List<StationAiInsight> threats = new ArrayList<>();
        List<SpaceStation> supporting = getSupportingStations(fleet, stations);

        for (SpaceStation station : supporting) {
            if (station.getStationType() == com.yisy.modules.doramcu.hex.SpaceStationType.MILITARY) {
                StationAiCore aiCore = station.getAiCore();
                // Process the station to generate insights
                aiCore.processTurn(context);
                // Add threat insights
                for (StationAiInsight insight : aiCore.getInsights()) {
                    if (insight.getType() == StationAiInsight.InsightType.THREAT_DETECTED) {
                        threats.add(insight);
                    }
                }
            }
        }

        return Collections.unmodifiableList(threats);
    }

    /**
     * Returns the repair bonus provided to a fleet by nearby shipyard stations.
     * <p>
     * Shipyard stations with AI Cores provide enhanced repair capabilities
     * to fleets within their support radius.
     * </p>
     *
     * @param fleet the fleet to check
     * @param stations the list of all stations
     * @return the repair bonus multiplier (0.0 to 0.50)
     */
    public double getRepairBonus(Fleet fleet, List<SpaceStation> stations) {
        List<SpaceStation> supporting = getSupportingStations(fleet, stations);
        if (supporting.isEmpty()) {
            return 0.0;
        }

        double totalBonus = 0.0;
        for (SpaceStation station : supporting) {
            if (station.getStationType() == com.yisy.modules.doramcu.hex.SpaceStationType.SHIPYARD) {
                StationAiCore aiCore = station.getAiCore();
                totalBonus += aiCore.getAssemblyBonus();
            }
        }

        return Math.min(totalBonus, MAX_COMBINED_BONUS);
    }

    /**
     * Returns the economic efficiency bonus for a fleet near trade stations.
     * <p>
     * Trade stations with AI Cores provide economic optimization to nearby fleets,
     * reducing operational costs.
     * </p>
     *
     * @param fleet the fleet to check
     * @param stations the list of all stations
     * @return the economic efficiency bonus (0.0 to 0.50)
     */
    public double getEconomicBonus(Fleet fleet, List<SpaceStation> stations) {
        List<SpaceStation> supporting = getSupportingStations(fleet, stations);
        if (supporting.isEmpty()) {
            return 0.0;
        }

        double totalBonus = 0.0;
        for (SpaceStation station : supporting) {
            if (station.getStationType() == com.yisy.modules.doramcu.hex.SpaceStationType.TRADE) {
                StationAiCore aiCore = station.getAiCore();
                totalBonus += aiCore.getEconomicEfficiencyBonus();
            }
        }

        return Math.min(totalBonus, MAX_COMBINED_BONUS);
    }

    /**
     * Processes all stations' AI Cores for the current turn.
     * <p>
     * This should be called once per turn to update all station AI operations.
     * </p>
     *
     * @param stations the list of all stations
     * @param context the current decision context
     */
    public void processAllStations(List<SpaceStation> stations, AiDecisionContext context) {
        if (stations == null) {
            return;
        }

        for (SpaceStation station : stations) {
            if (station.hasAiCore()) {
                station.getAiCore().processTurn(context);
            }
        }
    }

    /**
     * Returns a summary of all station AI insights for the current turn.
     *
     * @param stations the list of all stations
     * @return map of station name to list of insights
     */
    public Map<String, List<StationAiInsight>> getAllInsights(List<SpaceStation> stations) {
        if (stations == null) {
            return Collections.emptyMap();
        }

        java.util.HashMap<String, List<StationAiInsight>> insightsMap = new java.util.HashMap<>();
        for (SpaceStation station : stations) {
            if (station.hasAiCore()) {
                List<StationAiInsight> insights = station.getAiCore().getInsights();
                if (!insights.isEmpty()) {
                    insightsMap.put(station.getName(), insights);
                }
            }
        }

        return Collections.unmodifiableMap(insightsMap);
    }
}
