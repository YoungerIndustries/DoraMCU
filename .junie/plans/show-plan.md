---
sessionId: session-261106-043308-1k2c
---

# Project Status

## DoraMCU Project — Streamlining Plan Status

### Overview & Goals
DoraMCU is a Spring Boot 4.1.2 multi-module Java 27 application — a 4X space strategy game server with AI opponents, deep learning integration, and a browser-based Web UI. The goal of the streamlining effort is to improve maintainability, reduce technical debt, and clean up the codebase.

---

## Completed Items ✅

| # | Priority | Action | Status |
|---|----------|--------|--------|
| 1 | P0 | Consolidate two Spring Boot entry points | ✅ Done — `DoraMcuApplication.java` deleted, merged into `DoraMcuLauncher.java` with `@EnableScheduling`, `@ComponentScan`, and dev-profile warning |
| 2 | P1 | Split `GameSessionService` (1,528 lines) | ✅ Done — extracted ~700 lines of DTO building methods into `GameStateBuilder.java` (769 lines), reducing `GameSessionService` to 794 lines (48% reduction) |
| 3 | P1 | Document hex/model separation | ✅ Done — added `package-info.java` in both `hex/` and `model/` packages explaining the dual-domain pattern, conversion direction, and service-layer-only rule |
| 4 | P2 | Refactor `SecurityConfig` | ✅ Done — extracted shared authorization rules into `configureCommonSecurity()`, removed dead legacy paths (`/auth/**`, `/api/galaxy/progress`, `/api/sessions/**`, `/api/admin/**`), keeping only `/api/v1/*` paths |
| 5 | P2 | Clean up legacy files and update `.gitignore` | ✅ Done — added `DoraAI2/`, `search.ps1`, `transcript.md`, `**/__pycache__/`, `*.pyc`, `*.pyo`, `*.egg-info/` to `.gitignore` |
| 6 | P2 | Duplicate DTOs documentation | ✅ Done — added synchronization documentation to `GameStateResponse`, `PlayerSummary`, `SubSectorInfo` explaining why inheritance is not viable (Java generics invariance) |
| 7 | P3 | Move MySQL config to profile-specific properties | ✅ Done — created `application-mysql.properties` with all MySQL-specific settings; cleaned `application.properties` of MySQL defaults |
| 8 | P3 | Standardize JVM memory settings | ✅ Done — unified to `-Xmx4G` across `build.gradle` (bootRun + test) and `Dockerfile` (with `JVM_XMX` env override support) |
| 9 | P3 | `dora-ai` dependency fix | ✅ Done — changed `spring-boot-starter-web` from `implementation` to `compileOnly` in `dora-ai/build.gradle`; added `testImplementation` for test compilation |
| 10 | LOW | V2 migration warning | ✅ Done — added destructive migration warning comment to `V2__rebuild_schema.sql` |
| 11 | LOW | Fix test failures (49 pre-existing) | ✅ Done — fixed `CelestialBody.size()` lazy geometry bug, celestial body lazy geometry in 4 classes, `HexTest` assertions, `GameSessionServiceTest` null mocks, `dora-ai` test compilation |

---

## Remaining Items ⏳

| # | Priority | Action | Status |
|---|----------|--------|--------|
| 12 | LOW | Remove orphaned files from working tree | ✅ Done — removed `DoraAI2/src/Main.java`, `search.ps1`, `transcript.md`, and 7 `__pycache__/.pyc` files from git index using `git rm --cached` |

---

## Verification

- **`gradlew compileJava`** — BUILD SUCCESSFUL across all modules
- **`gradlew cleanTest test`** — 2075 tests executed, 0 failures, 100% success rate
- **Application runs** — Server started on port 8080, galaxy generated with 19,713 systems

---

## Current Architecture

```
DoraMCU/
├── src/main/java/com/yisy/modules/doramcu/
│   ├── ai/              — Game AI cores (Empire, Station, Strategic, Tactical)
│   ├── config/          — Security, rate limiting, JWT, caching, metrics
│   ├── controller/      — 9 REST controllers (all /api/v1/*)
│   ├── dto/             — 26 DTOs for API serialization
│   ├── exception/       — Global exception handler + custom exceptions
│   ├── hex/             — Core game domain: 80+ domain classes (Planet, Fleet, Ship, Galaxy, etc.)
│   │   └── package-info.java — Documents hex/model separation
│   ├── hex/manager/     — 6 manager classes (Combat, Fleet, Galaxy, Planet, Resource, Turn)
│   ├── model/           — JPA entities: 12 entities (Planet, Star, Galaxy, User, etc.)
│   │   └── package-info.java — Documents hex/model separation
│   ├── repository/      — 16 JPA repositories
│   ├── service/         — 11 services (GameSession, Galaxy, Player, JWT, GameStateBuilder, etc.)
│   └── util/            — PlayerIdExtractor
├── dora-ai/             — AI chat provider abstraction + implementations
├── dora-dl-core/        — Neural network strategy learning
└── dora-ai-openhands/   — Python OpenHands integration
```

### Key Metrics
- **Total classes:** ~333 across all modules
- **Test classes:** ~100
- **Tests:** 2075 tests, 0 failures
- **GameSessionService:** 794 lines (reduced from 1,528)
- **GameStateBuilder:** 769 lines (new, extracted DTO builders)
- **Single entry point:** `DoraMcuLauncher.java` (consolidated)
- **JVM memory:** Standardized to `-Xmx4G` across all environments
- **Security:** Legacy paths removed, only `/api/v1/*` permitted
- **MySQL config:** Isolated to `application-mysql.properties` profile
