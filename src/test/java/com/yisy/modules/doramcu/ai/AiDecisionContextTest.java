package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SpaceStationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiDecisionContext Tests")
class AiDecisionContextTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(1, "test@test.com", "Test Empire");
    }

    // ---- Builder Tests ----

    @Test
    @DisplayName("should build context with all fields")
    void buildWithAllFields() {
        List<Fleet> owned = new ArrayList<>();
        owned.add(new Fleet(1, "Fleet 1"));

        List<Fleet> enemies = new ArrayList<>();
        enemies.add(new Fleet(2, "Enemy Fleet"));

        List<SpaceStation> stations = new ArrayList<>();
        stations.add(new SpaceStation(1, SpaceStationType.MILITARY));

        Map<String, Integer> resources = new HashMap<>();
        resources.put("minerals", 200);
        resources.put("energy", 100);

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(owned)
                .enemyFleets(enemies)
                .stations(stations)
                .resources(resources)
                .minerals(200)
                .energy(100)
                .food(50)
                .credits(75)
                .build();

        assertEquals(player, context.getPlayer());
        assertEquals(1, context.getOwnedFleets().size());
        assertEquals(1, context.getEnemyFleets().size());
        assertEquals(1, context.getStations().size());
        assertEquals(200, context.getMinerals());
        assertEquals(100, context.getEnergy());
        assertEquals(50, context.getFood());
        assertEquals(75, context.getCredits());
        assertEquals(200, context.getResources().get("minerals"));
        assertEquals(100, context.getResources().get("energy"));
    }

    @Test
    @DisplayName("should build context with minimal fields")
    void buildWithMinimalFields() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(0)
                .energy(0)
                .food(0)
                .credits(0)
                .build();

        assertEquals(player, context.getPlayer());
        assertTrue(context.getOwnedFleets().isEmpty());
        assertTrue(context.getEnemyFleets().isEmpty());
        assertTrue(context.getStations().isEmpty());
        assertTrue(context.getResources().isEmpty());
        assertEquals(0, context.getMinerals());
    }

    // ---- Stations Tests ----

    @Test
    @DisplayName("should return empty stations when not set")
    void emptyStationsByDefault() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertTrue(context.getStations().isEmpty());
    }

    @Test
    @DisplayName("should return stations set via builder")
    void stationsFromBuilder() {
        List<SpaceStation> stations = new ArrayList<>();
        stations.add(new SpaceStation(1, SpaceStationType.MILITARY));
        stations.add(new SpaceStation(2, SpaceStationType.TRADE));
        stations.add(new SpaceStation(3, SpaceStationType.MINING));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .stations(stations)
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertEquals(3, context.getStations().size());
    }

    @Test
    @DisplayName("should handle null stations in builder")
    void nullStationsInBuilder() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .stations(null)
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        // Should default to empty list, not throw
        assertTrue(context.getStations().isEmpty());
    }

    // ---- Resources Tests ----

    @Test
    @DisplayName("should return empty resources when not set")
    void emptyResourcesByDefault() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertTrue(context.getResources().isEmpty());
    }

    @Test
    @DisplayName("should return resources set via builder")
    void resourcesFromBuilder() {
        Map<String, Integer> resources = new HashMap<>();
        resources.put("minerals", 200);
        resources.put("energy", 150);
        resources.put("food", 80);
        resources.put("credits", 300);
        resources.put("iron", 50);
        resources.put("gas", 30);

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .resources(resources)
                .minerals(200)
                .energy(150)
                .food(80)
                .credits(300)
                .build();

        assertEquals(200, context.getResources().get("minerals"));
        assertEquals(150, context.getResources().get("energy"));
        assertEquals(80, context.getResources().get("food"));
        assertEquals(300, context.getResources().get("credits"));
        assertEquals(50, context.getResources().get("iron"));
        assertEquals(30, context.getResources().get("gas"));
    }

    @Test
    @DisplayName("should handle null resources in builder")
    void nullResourcesInBuilder() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .resources(null)
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        // Should default to empty map, not throw
        assertTrue(context.getResources().isEmpty());
    }

    // ---- Resource Accessors ----

    @Test
    @DisplayName("should return correct minerals")
    void getMinerals() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(250)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertEquals(250, context.getMinerals());
    }

    @Test
    @DisplayName("should return correct energy")
    void getEnergy() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(175)
                .food(30)
                .credits(50)
                .build();

        assertEquals(175, context.getEnergy());
    }

    @Test
    @DisplayName("should return correct food")
    void getFood() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(90)
                .credits(50)
                .build();

        assertEquals(90, context.getFood());
    }

    @Test
    @DisplayName("should return correct credits")
    void getCredits() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(500)
                .build();

        assertEquals(500, context.getCredits());
    }

    // ---- Fleet Accessors ----

    @Test
    @DisplayName("should return owned fleets")
    void getOwnedFleets() {
        List<Fleet> fleets = new ArrayList<>();
        fleets.add(new Fleet(1, "Fleet 1"));
        fleets.add(new Fleet(2, "Fleet 2"));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(fleets)
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertEquals(2, context.getOwnedFleets().size());
    }

    @Test
    @DisplayName("should return enemy fleets")
    void getEnemyFleets() {
        List<Fleet> enemies = new ArrayList<>();
        enemies.add(new Fleet(10, "Enemy 1"));

        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(enemies)
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertEquals(1, context.getEnemyFleets().size());
    }

    // ---- Player Accessor ----

    @Test
    @DisplayName("should return player")
    void getPlayer() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        assertEquals(player, context.getPlayer());
        assertEquals("Test Empire", context.getPlayer().getEmpireName());
    }

    // ---- toString ----

    @Test
    @DisplayName("toString should include player name and fleet counts")
    void toStringIncludesKeyInfo() {
        AiDecisionContext context = AiDecisionContext.builder()
                .player(player)
                .ownedFleets(new ArrayList<>())
                .enemyFleets(new ArrayList<>())
                .minerals(100)
                .energy(50)
                .food(30)
                .credits(50)
                .build();

        String str = context.toString();
        assertTrue(str.contains("Test Empire"));
        assertTrue(str.contains("ownedFleets=0"));
        assertTrue(str.contains("enemyFleets=0"));
        assertTrue(str.contains("minerals=100"));
        assertTrue(str.contains("energy=50"));
    }

    // ---- Builder Method Chaining ----

    @Test
    @DisplayName("builder methods should support chaining")
    void builderMethodChaining() {
        AiDecisionContext.Builder builder = AiDecisionContext.builder();
        assertSame(builder, builder.player(player));
        assertSame(builder, builder.ownedFleets(new ArrayList<>()));
        assertSame(builder, builder.enemyFleets(new ArrayList<>()));
        assertSame(builder, builder.stations(new ArrayList<>()));
        assertSame(builder, builder.resources(new HashMap<>()));
        assertSame(builder, builder.minerals(100));
        assertSame(builder, builder.energy(50));
        assertSame(builder, builder.food(30));
        assertSame(builder, builder.credits(50));
    }
}
