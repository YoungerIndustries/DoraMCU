---
sessionId: session-261102-221210-1re6
---

# Requirements

### Overview & Goals
Transform the DoraMCU 4X space strategy game from a Swing GUI desktop app into a **web server-based multiplayer game** with an external desktop client communicating via REST APIs and WebSocket for real-time updates.

### User Stories
- As a player, I want to register and log in so I can join game sessions.
- As a player, I want to create or join a multiplayer game session so I can play with others.
- As a player, it should only be my turn to act, and I should see real-time updates when other players act.
- As a player, I want to move fleets, build ships, and engage in combat through the client app.
- As a player, I want to see the current game state (resources, planets, fleets) at any time.

### Scope
**In Scope:**
- REST API for game session management (create, join, list sessions)
- REST API for game actions (move fleet, build ship, engage combat, advance turn)
- REST API for game state queries (current player, resources, planets, fleets)
- WebSocket for real-time game state broadcasts (turn changes, action results, player events)
- Game session management service (in-memory Game instances per session)
- Server-enforced turn synchronization
- Desktop client DTOs and API contracts
- Spring Security integration (JWT-authenticated game endpoints)

**Out of Scope:**
- The actual desktop client UI implementation
- AI opponents
- Game replay / action logging
- Chat functionality
- Persistent game state recovery

### Functional Requirements
1. **Game Sessions**: Players can create and join named game sessions. Each session hosts one in-memory `Game` instance.
2. **Turn Enforcement**: Only the current player can perform actions. Other players receive WebSocket notifications.
3. **Game Actions**: Move fleets, build ships, engage combat, advance turns — all via REST endpoints.
4. **Real-Time Updates**: WebSocket broadcasts game state changes to all connected clients in a session.
5. **Authentication**: All game endpoints require JWT authentication (existing infrastructure).

### Non-Functional Requirements
- Game state is in-memory for performance; no persistence of game actions.
- WebSocket connections are session-scoped; disconnecting does not affect the game.
- Thread-safe access to shared Game instances under concurrent requests.

# Technical Design

### Current Implementation
- **Game Engine**: `Game.java` (1721 lines) manages players, planets, fleets, ships, turns, combat, and galaxies in memory. Key methods: `addPlayer()`, `createPlayer()`, `createFleet()`, `queueShipBuild()`, `nextTurn()`, `engageFleets()`, `getCurrentPlayer()`, `getPlayers()`.
- **Existing Controllers**: `DeviceController` (CRUD REST pattern with caching/metrics), `AuthenticationController` (JWT login), `HomeController`.
- **Services**: `PlayerService` (player registration with PINs), `GalaxyService` (galaxy generation/persistence), `JwtService`.
- **Security**: `SecurityConfig` with JWT filter, stateless sessions, `/auth/**` permitted, `/api/**` authenticated.
- **Models**: JPA entities for `Player`, `Galaxy`, `Planet`, `Fleet`, `Ship`, `Battle` in `com.yisy.modules.doramcu.model`.
- **Pattern**: Controllers → Services → Repositories, with constructor injection.

### Key Decisions
- **Shared In-Memory Game State**: Each game session holds one `Game` instance in a `ConcurrentHashMap`. No database persistence of game actions.
- **Server-Enforced Turns**: The `GameSessionService` checks `Game.getCurrentPlayer()` against the requesting player before allowing actions.
- **REST + WebSocket**: REST for stateless operations (login, queries, actions). WebSocket (`SseEmitter` / `WebSocketHandler`) for real-time broadcasts.
- **Game Session Model**: A `GameSession` wrapper tracks the `Game` instance, connected players, and their WebSocket emitters.

### Architecture Diagram

``` mermaid
graph LR
    C1[Desktop Client 1] -->|REST + WS| GW[Gateway / Security]
    C2[Desktop Client 2] -->|REST + WS| GW
    GW -->|JWT Auth| GC[GameController]
    GW -->|JWT Auth| GS[GameSessionController]
    GC --> GSS[GameSessionService]
    GS --> GSS
    GSS --> GM[Game Instance]
    GSS -->|Broadcast| WS[WebSocket / SSE]
    WS --> C1
    WS --> C2
```

### Proposed Changes

**New DTOs** (`com.yisy.modules.doramcu.dto`):
- `CreateSessionRequest` / `CreateSessionResponse`: session name, player info.
- `JoinSessionRequest` / `JoinSessionResponse`: session ID, player info.
- `GameStateResponse`: current turn, current player, players summary, planets, fleets.
- `PlayerSummary`: id, name, resources, planets, fleets.
- `MoveFleetRequest`: fleetId, target position.
- `BuildShipRequest`: ship type, target fleetId.
- `EngageCombatRequest`: attacker fleetId, defender fleetId.
- `GameEvent`: WebSocket event type (TURN_CHANGED, ACTION_RESULT, PLAYER_JOINED, PLAYER_LEFT, GAME_STATE) + payload.

**New Service** (`com.yisy.modules.doramcu.service.GameSessionService`):
- Manages a `ConcurrentHashMap<String, GameSession>` mapping session IDs to sessions.
- `createSession(playerId, sessionName)`: creates a new `GameSession` with a new `Game` instance, adds the player.
- `joinSession(playerId, sessionId)`: adds a player to an existing session.
- `getGameState(playerId, sessionId)`: returns the current game state (filtered to the player's perspective).
- `moveFleet(playerId, sessionId, fleetId, targetPos)`: server-enforced turn check + action.
- `buildShip(playerId, sessionId, shipType, targetFleetId)`: server-enforced turn check + action.
- `engageCombat(playerId, sessionId, attackerFleetId, defenderFleetId)`: server-enforced turn check + action.
- `advanceTurn(playerId, sessionId)`: server-enforced turn check + `Game.nextTurn()`.
- `broadcastEvent(sessionId, event)`: pushes a `GameEvent` to all connected WebSocket clients in the session.
- `leaveSession(playerId, sessionId)`: removes a player and their emitter.

**New Model** (`com.yisy.modules.doramcu.model.GameSession`):
- `sessionId`, `name`, `Game game`, `Map<Integer, SseEmitter> emitters` (playerId → emitter), `Set<Integer> playerIds`.

**New Controllers**:
- `GameSessionController` (`/api/sessions`): REST endpoints for create/join/leave/list sessions.
- `GameController` (`/api/sessions/{sessionId}`): REST endpoints for game actions (move, build, combat, turn) and state queries.
- `GameWebSocketController` (`/ws/game`): WebSocket/SSE endpoint for real-time game events.

**Security Updates**:
- Update `SecurityConfig` to permit WebSocket handshake and SSE endpoints.

### File Structure
```
src/main/java/com/yisy/modules/doramcu/
├── dto/
│   ├── CreateSessionRequest.java         (NEW)
│   ├── CreateSessionResponse.java        (NEW)
│   ├── JoinSessionRequest.java           (NEW)
│   ├── JoinSessionResponse.java          (NEW)
│   ├── GameStateResponse.java            (NEW)
│   ├── PlayerSummary.java                (NEW)
│   ├── MoveFleetRequest.java             (NEW)
│   ├── BuildShipRequest.java             (NEW)
│   ├── EngageCombatRequest.java          (NEW)
│   └── GameEvent.java                    (NEW)
├── model/
│   └── GameSession.java                  (NEW)
├── service/
│   └── GameSessionService.java           (NEW)
├── controller/
│   ├── GameSessionController.java        (NEW)
│   ├── GameController.java              (NEW)
│   └── GameWebSocketController.java      (NEW)
└── config/
    └── SecurityConfig.java               (MODIFIED)
```

### Data Models / Contracts

**GameEvent** (WebSocket payload):
```json
{
  "type": "TURN_CHANGED | ACTION_RESULT | PLAYER_JOINED | PLAYER_LEFT | GAME_STATE",
  "sessionId": "string",
  "payload": { ... }
}
```

**GameStateResponse**:
```json
{
  "sessionId": "string",
  "currentTurn": 5,
  "currentPlayerId": 1,
  "currentPlayerName": "Player1",
  "players": [ { "id": 1, "name": "Player1", "resources": { "MINERALS": 500, ... }, "planetCount": 1, "fleetCount": 2 } ],
  "isMyTurn": true
}
```

**REST Endpoints**:
- `POST /api/sessions` — create session
- `POST /api/sessions/{id}/join` — join session
- `POST /api/sessions/{id}/leave` — leave session
- `GET /api/sessions` — list sessions
- `GET /api/sessions/{id}/state` — get game state
- `POST /api/sessions/{id}/actions/move-fleet` — move fleet
- `POST /api/sessions/{id}/actions/build-ship` — build ship
- `POST /api/sessions/{id}/actions/engage-combat` — engage combat
- `POST /api/sessions/{id}/actions/advance-turn` — advance turn
- `GET /ws/game/{sessionId}` — SSE stream for game events

### Risks
- **Thread Safety**: Concurrent access to in-memory `Game` instances requires synchronization. Will use `ReentrantReadWriteLock` per session.
- **Memory Leaks**: Disconnected WebSocket emitters must be cleaned up. Will add timeout and completion handlers.
- **State Consistency**: If the server restarts, all in-memory game sessions are lost. Accepted trade-off for now.

# Delivery Steps

### ✓ done Step 1: Create DTOs and GameSession model
Define all data transfer objects and the GameSession wrapper model.
- Create `GameEvent.java` with event types (TURN_CHANGED, ACTION_RESULT, PLAYER_JOINED, PLAYER_LEFT, GAME_STATE) and a generic payload.
- Create `CreateSessionRequest.java`, `CreateSessionResponse.java`, `JoinSessionRequest.java`, `JoinSessionResponse.java` for session management.
- Create `GameStateResponse.java` and `PlayerSummary.java` for game state queries.
- Create `MoveFleetRequest.java`, `BuildShipRequest.java`, `EngageCombatRequest.java` for game actions.
- Create `GameSession.java` model wrapping a `Game` instance, connected player IDs, and SSE emitters with thread-safe access.

### ✓ done Step 2: Implement GameSessionService
Build the core service that manages multiplayer game sessions with in-memory state.
- Create `GameSessionService.java` with a `ConcurrentHashMap<String, GameSession>` for session storage.
- Implement `createSession()`: creates a new `GameSession`, initializes a `Game` instance, adds the creating player, and broadcasts PLAYER_JOINED.
- Implement `joinSession()`: adds a player to an existing session and broadcasts PLAYER_JOINED.
- Implement `leaveSession()`: removes a player and cleans up their SSE emitter.
- Implement `getGameState()`: returns a `GameStateResponse` with current turn, players, and whether it's the requesting player's turn.
- Implement action methods (`moveFleet`, `buildShip`, `engageCombat`, `advanceTurn`) with server-enforced turn checks — reject actions if it's not the player's turn.
- Implement `broadcastEvent()`: pushes `GameEvent` to all connected SSE emitters in a session.
- Add `ReentrantReadWriteLock` per session for thread-safe access to the `Game` instance.

### ✓ done Step 3: Implement REST controllers
Create the REST API endpoints for session management and game actions.
- Create `GameSessionController.java` (`/api/sessions`) with endpoints: `POST /` (create), `POST /{id}/join`, `POST /{id}/leave`, `GET /` (list sessions).
- Create `GameController.java` (`/api/sessions/{id}`) with endpoints: `GET /state` (game state), `POST /actions/move-fleet`, `POST /actions/build-ship`, `POST /actions/engage-combat`, `POST /actions/advance-turn`.
- Wire all controllers to `GameSessionService` and extract the current player's ID from the JWT authentication context.
- Return appropriate HTTP status codes: 200 OK, 201 Created, 403 Forbidden (not your turn), 404 Not Found (session not found).
- Follow the existing controller pattern (constructor injection, logging, consistent error handling).

### ✓ done Step 4: Implement WebSocket / SSE endpoint
Add real-time event streaming for game state updates.
- Create `GameWebSocketController.java` (`/ws/game`) with an SSE endpoint `GET /{sessionId}` that returns an `SseEmitter`.
- On connection, register the emitter with the `GameSessionService` for the player's session.
- On disconnect/timeout, clean up the emitter.
- Configure `SecurityConfig.java` to permit the WebSocket/SSE endpoint (`/ws/**`) for authenticated users.
- Ensure `GameSessionService.broadcastEvent()` pushes to all registered emitters in the session.

### ✓ done Step 5: Integrate security and finalize
Wire up Spring Security and validate the complete flow.
- Update `SecurityConfig.java` to permit `/ws/**` for authenticated users and ensure all `/api/sessions/**` endpoints are authenticated.
- Ensure `GameSessionService` extracts the player ID from `SecurityContextHolder` or accepts it from controllers.
- Add the `spring-boot-starter-websocket` dependency if needed (SSE uses standard web starter).
- Verify the complete flow: create session → join session → get state → take action → receive WebSocket event → advance turn.

### ✓ done Step 6: Integrate Dora AI into game system
Add neural network-based AI decision making to the existing AiPlayerService using Deeplearning4j.
- Add Deeplearning4j dependencies to `build.gradle` (`deeplearning4j-core`, `nd4j-native-platform`).
- Create `DlConfig.java` for neural network configuration (layer sizes, learning rate, blend factor).
- Create `GameStateFeatures.java` — 32-dimensional feature vector for neural network input.
- Create `ActionReward.java` — action types (BUILD_SHIP, MOVE_FLEET, ENGAGE_COMBAT, EXPAND, WAIT) and training data model.
- Create `StrategyNetwork.java` — neural network with Deeplearning4j (32 → 64 → 128 → 64 → 5 architecture).
- Create `GameStateFeatureExtractor.java` — converts GameStateResponse to feature vectors.
- Create `DlGameAiService.java` — combines neural network predictions with heuristic scores.
- Update `AiPlayerService.java` to use `DlGameAiService` for MEDIUM and HARD difficulty AI players.
- Add DL AI configuration properties to `application.properties`.
- Verify with `gradle compileJava` and `gradle test`.