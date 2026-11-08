package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.TradeRequest;
import com.yisy.modules.doramcu.dto.TradeResponse;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.manager.TradeCareerService;
import com.yisy.modules.doramcu.hex.manager.TradeManager;
import com.yisy.modules.doramcu.hex.manager.TradeRouteNetwork;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.TradeService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.GameSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for trade operations.
 * <p>
 * Provides endpoints for checking market prices, buying and selling cargo,
 * executing trade missions between Traveller worlds, and accessing trade
 * career, credit exchange, route network, and economic event data.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/trade}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/trade")
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final GameSessionService gameSessionService;

    public TradeController(TradeService tradeService, GameSessionService gameSessionService) {
        this.tradeService = tradeService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Gets market prices for a world.
     *
     * @param worldId the world ID
     * @return market prices for all available commodities
     */
    @GetMapping("/prices/{worldId}")
    public ResponseEntity<TradeResponse> getMarketPrices(@PathVariable int worldId) {
        log.debug("GET /api/v1/traveller/trade/prices/{} - market prices", worldId);
        TradeResponse response = tradeService.getMarketPrices(worldId);
        return ResponseEntity.ok(response);
    }

    /**
     * Buys cargo at a world.
     *
     * @param request the trade request (commodity, quantity, world, ship)
     * @return the purchase result with cost and cargo status
     */
    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buyCargo(@Valid @RequestBody TradeRequest request) {
        log.debug("POST /api/v1/traveller/trade/buy - {} units of {}",
                request.getQuantity(), request.getCommodity());
        TradeResponse response = tradeService.buyCargo(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Sells cargo at a world.
     *
     * @param request the trade request (commodity, quantity, world, ship)
     * @return the sale result with revenue and cargo status
     */
    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sellCargo(@Valid @RequestBody TradeRequest request) {
        log.debug("POST /api/v1/traveller/trade/sell - {} units of {}",
                request.getQuantity(), request.getCommodity());
        TradeResponse response = tradeService.sellCargo(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes a trade mission between two worlds.
     * <p>
     * Automatically finds the most profitable commodity, buys at the source
     * world, and sells at the destination world.
     * </p>
     *
     * @param request the trade request (source world, destination world, ship)
     * @return the trade mission result with profit and ROI
     */
    @PostMapping("/mission")
    public ResponseEntity<TradeResponse> executeTradeMission(
            @Valid @RequestBody TradeRequest request) {
        log.debug("POST /api/v1/traveller/trade/mission - {} -> {}",
                request.getWorldId(), request.getDestinationWorldId());
        TradeResponse response = tradeService.executeTradeMission(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all available commodities.
     *
     * @return list of commodity names
     */
    @GetMapping("/commodities")
    public ResponseEntity<List<String>> getCommodities() {
        log.debug("GET /api/v1/traveller/trade/commodities - listing all commodities");
        List<String> commodities = tradeService.getAvailableCommodities();
        return ResponseEntity.ok(commodities);
    }

    /**
     * Gets trade career summary for a session's current player.
     */
    @GetMapping("/career-summary")
    public ResponseEntity<Map<String, Object>> getCareerSummary(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/career-summary for session {}", sessionId);
        if (sessionId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("hasCommander", false);
            empty.put("tradeProfitBonus", 0.0);
            empty.put("cargoCapacityBonus", 0.0);
            empty.put("crewSkillModifier", 1.0);
            empty.put("tradeMissionsCompleted", 0);
            empty.put("tradeEarnings", 0L);
            return ResponseEntity.ok(empty);
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("hasCommander", false);
            empty.put("tradeProfitBonus", 0.0);
            empty.put("cargoCapacityBonus", 0.0);
            empty.put("crewSkillModifier", 1.0);
            empty.put("tradeMissionsCompleted", 0);
            empty.put("tradeEarnings", 0L);
            return ResponseEntity.ok(empty);
        }
        Game game = (Game) session.getGame();
        Player player = game.getCurrentPlayer();
        TradeManager tradeManager = game.getTradeManager();
        Map<String, Object> summary;
        if (tradeManager != null && tradeManager.getTradeCareerService() != null) {
            summary = tradeManager.getTradeCareerService().getCareerSummary(player);
        } else {
            summary = new HashMap<>();
            summary.put("hasCommander", false);
        }
        return ResponseEntity.ok(summary);
    }

    /**
     * Gets credit exchange summary for a session's current player.
     */
    @GetMapping("/exchange-summary")
    public ResponseEntity<Map<String, Object>> getExchangeSummary(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/exchange-summary for session {}", sessionId);
        if (sessionId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("tradeCredits", 0L);
            empty.put("resourceConversions", new HashMap<>());
            return ResponseEntity.ok(empty);
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("tradeCredits", 0L);
            empty.put("resourceConversions", new HashMap<>());
            return ResponseEntity.ok(empty);
        }
        Game game = (Game) session.getGame();
        Player player = game.getCurrentPlayer();
        TradeManager tradeManager = game.getTradeManager();
        Map<String, Object> summary = new HashMap<>();
        if (tradeManager != null && tradeManager.getCreditExchangeService() != null) {
            Integer empireId = null;
            for (var e : game.getEmpires().values()) {
                if (e.getRulerPlayerId() == player.getId()) {
                    empireId = e.getId();
                    break;
                }
            }
            if (empireId != null) {
                summary = tradeManager.getCreditExchangeService().getExchangeSummary(empireId);
            }
        }
        summary.putIfAbsent("tradeCredits", 0L);
        summary.putIfAbsent("resourceConversions", new HashMap<>());
        return ResponseEntity.ok(summary);
    }

    /**
     * Gets active economic events for a session.
     */
    @GetMapping("/economic-events")
    public ResponseEntity<List<Map<String, Object>>> getEconomicEvents(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/economic-events for session {}", sessionId);
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            return ResponseEntity.ok(List.of());
        }
        Game game = (Game) session.getGame();
        TradeManager tradeManager = game.getTradeManager();
        List<Map<String, Object>> events = List.of();
        if (tradeManager != null && tradeManager.getTradeRouteNetwork() != null) {
            var networkEvents = tradeManager.getTradeRouteNetwork().getActiveEvents();
            events = networkEvents.stream().map(e -> {
                Map<String, Object> m = new HashMap<>();
                m.put("type", e.type.name());
                m.put("world", e.worldName);
                m.put("turnsRemaining", e.turnsRemaining);
                m.put("description", e.description);
                return m;
            }).collect(java.util.stream.Collectors.toList());
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Gets profitable trade routes for a session.
     */
    @GetMapping("/routes")
    public ResponseEntity<List<Map<String, Object>>> getTradeRoutes(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/routes for session {}", sessionId);
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            return ResponseEntity.ok(List.of());
        }
        Game game = (Game) session.getGame();
        TradeManager tradeManager = game.getTradeManager();
        List<Map<String, Object>> routes = List.of();
        if (tradeManager != null && tradeManager.getTradeRouteNetwork() != null) {
            var networkRoutes = tradeManager.getTradeRouteNetwork().findAllProfitableRoutes();
            routes = networkRoutes.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("sourcePlanetId", r.sourcePlanetId);
                m.put("destPlanetId", r.destPlanetId);
                m.put("sourceWorldName", r.sourceWorldName);
                m.put("destWorldName", r.destWorldName);
                m.put("commodity", r.commodity.getName());
                m.put("profitPerUnit", r.profitPerUnit);
                return m;
            }).collect(java.util.stream.Collectors.toList());
        }
        return ResponseEntity.ok(routes);
    }
}
