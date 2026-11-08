package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/4x/sessions/{sessionId}")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameSessionService gameSessionService;
    private final PlayerIdExtractor playerIdExtractor;

    public GameController(GameSessionService gameSessionService, PlayerIdExtractor playerIdExtractor) {
        this.gameSessionService = gameSessionService;
        this.playerIdExtractor = playerIdExtractor;
    }

    @GetMapping("/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String sessionId,
                                                          @RequestParam(required = false, defaultValue = "false") boolean summary,
                                                          Authentication authentication) {
        log.debug("User {} requesting game state for session: {} (summary={})", authentication.getName(), sessionId, summary);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.getGameState(playerId, sessionId, summary);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/move-fleet")
    public ResponseEntity<GameStateResponse> moveFleet(@PathVariable String sessionId,
                                                       @Valid @RequestBody MoveFleetRequest request,
                                                       Authentication authentication) {
        log.debug("User {} moving fleet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.moveFleet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/build-ship")
    public ResponseEntity<GameStateResponse> buildShip(@PathVariable String sessionId,
                                                       @Valid @RequestBody BuildShipRequest request,
                                                       Authentication authentication) {
        log.debug("User {} building ship in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildShip(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/engage-combat")
    public ResponseEntity<GameStateResponse> engageCombat(@PathVariable String sessionId,
                                                          @Valid @RequestBody EngageCombatRequest request,
                                                          Authentication authentication) {
        log.debug("User {} engaging combat in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.engageCombat(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/advance-turn")
    public ResponseEntity<GameStateResponse> advanceTurn(@PathVariable String sessionId,
                                                         Authentication authentication) {
        log.debug("User {} advancing turn in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.advanceTurn(playerId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pipeline")
    public ResponseEntity<ResourcePipelineSummary> getPipelineStatus(@PathVariable String sessionId,
                                                                     Authentication authentication) {
        log.debug("User {} requesting pipeline status for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        ResourcePipelineSummary summary = gameSessionService.getPipelineSummary(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/pipeline/details")
    public ResponseEntity<PipelineDetailsDto> getPipelineDetails(@PathVariable String sessionId,
                                                                  Authentication authentication) {
        log.debug("User {} requesting pipeline details for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        PipelineDetailsDto details = gameSessionService.getPipelineDetails(playerId, sessionId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(details);
    }

    // ---- New Empire Action Endpoints ----

    @PostMapping("/actions/build-station")
    public ResponseEntity<GameStateResponse> buildStation(@PathVariable String sessionId,
                                                          @Valid @RequestBody BuildStationRequest request,
                                                          Authentication authentication) {
        log.debug("User {} building station in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildStation(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/build-facility")
    public ResponseEntity<GameStateResponse> buildFacility(@PathVariable String sessionId,
                                                           @Valid @RequestBody BuildFacilityRequest request,
                                                           Authentication authentication) {
        log.debug("User {} building facility in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildFacility(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/create-fleet")
    public ResponseEntity<GameStateResponse> createFleet(@PathVariable String sessionId,
                                                         @Valid @RequestBody CreateFleetRequest request,
                                                         Authentication authentication) {
        log.debug("User {} creating fleet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.createFleet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/colonize")
    public ResponseEntity<GameStateResponse> colonizePlanet(@PathVariable String sessionId,
                                                            @Valid @RequestBody ColonizeRequest request,
                                                            Authentication authentication) {
        log.debug("User {} colonizing planet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.colonizePlanet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/battles")
    public ResponseEntity<List<BattleSummaryDto>> getActiveBattles(@PathVariable String sessionId,
                                                                    Authentication authentication) {
        log.debug("User {} requesting active battles for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        List<BattleSummaryDto> battles = gameSessionService.getActiveBattles(playerId, sessionId);
        return ResponseEntity.ok(battles);
    }

    @PostMapping("/battles/{battleId}/reinforce")
    public ResponseEntity<GameStateResponse> reinforceBattle(@PathVariable String sessionId,
                                                             @PathVariable int battleId,
                                                             @Valid @RequestBody ReinforceBattleRequest request,
                                                             Authentication authentication) {
        log.debug("User {} reinforcing battle {} in session: {}", authentication.getName(), battleId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.reinforceBattle(playerId, sessionId, battleId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/battles/{battleId}/retreat")
    public ResponseEntity<GameStateResponse> retreatBattle(@PathVariable String sessionId,
                                                           @PathVariable int battleId,
                                                           Authentication authentication) {
        log.debug("User {} retreating from battle {} in session: {}", authentication.getName(), battleId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.retreatBattle(playerId, sessionId, battleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/systems/{systemId}")
    public ResponseEntity<java.util.Map<String, Object>> getSystemDetails(@PathVariable String sessionId,
                                                                            @PathVariable int systemId,
                                                                            Authentication authentication) {
        log.debug("User {} requesting system details for system {} in session: {}", authentication.getName(), systemId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        java.util.Map<String, Object> details = gameSessionService.getSystemDetails(playerId, sessionId, systemId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(details);
    }

    // ---- Traveller RPG Integration Endpoints ----

    @PostMapping("/actions/build-from-design")
    public ResponseEntity<GameStateResponse> buildFromDesign(@PathVariable String sessionId,
                                                              @Valid @RequestBody ShipDesignRequest request,
                                                              Authentication authentication) {
        log.debug("User {} building ship from design in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildShipFromDesign(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/trade/buy")
    public ResponseEntity<GameStateResponse> buyCommodity(@PathVariable String sessionId,
                                                           @Valid @RequestBody TradeRequest request,
                                                           Authentication authentication) {
        log.debug("User {} buying commodity in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buyCommodity(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/trade/sell")
    public ResponseEntity<GameStateResponse> sellCommodity(@PathVariable String sessionId,
                                                            @Valid @RequestBody TradeRequest request,
                                                            Authentication authentication) {
        log.debug("User {} selling commodity in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.sellCommodity(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trade/market")
    public ResponseEntity<List<TradeOpportunityDto>> getTradeMarket(@PathVariable String sessionId,
                                                                     @RequestParam int systemId,
                                                                     Authentication authentication) {
        log.debug("User {} requesting trade market for system {} in session: {}", authentication.getName(), systemId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        List<TradeOpportunityDto> market = gameSessionService.getTradeMarket(playerId, sessionId, systemId);
        return ResponseEntity.ok(market);
    }

    @PostMapping("/actions/assign-commander")
    public ResponseEntity<GameStateResponse> assignCommander(@PathVariable String sessionId,
                                                              @RequestParam int characterId,
                                                              Authentication authentication) {
        log.debug("User {} assigning commander {} in session: {}", authentication.getName(), characterId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.assignCommander(playerId, sessionId, characterId);
        return ResponseEntity.ok(response);
    }
}
