package com.yisy.modules.doramcu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the StateMachine class.
 */
class StateMachineTest {

    // Test enums for states and events
    enum TrafficLightState {
        RED, YELLOW, GREEN
    }

    enum TrafficLightEvent {
        CHANGE
    }

    enum OrderState {
        PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    }

    enum OrderEvent {
        PAY, SHIP, DELIVER, CANCEL
    }

    @Test
    @DisplayName("Should initialize with initial state")
    void testInitialState() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .build();

        assertEquals(TrafficLightState.RED, sm.getCurrentState());
        assertTrue(sm.isInState(TrafficLightState.RED));
    }

    @Test
    @DisplayName("Should transition between states on event")
    void testBasicTransition() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .onTransition(TrafficLightState.GREEN, TrafficLightEvent.CHANGE, TrafficLightState.YELLOW)
                .onTransition(TrafficLightState.YELLOW, TrafficLightEvent.CHANGE, TrafficLightState.RED)
                .build();

        assertTrue(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState());

        assertTrue(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.YELLOW, sm.getCurrentState());

        assertTrue(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.RED, sm.getCurrentState());
    }

    @Test
    @DisplayName("Should return false when no valid transition exists")
    void testNoValidTransition() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .build();

        // Send event twice - second time should fail as GREEN has no transitions defined
        assertTrue(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState());

        assertFalse(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState());
    }

    @Test
    @DisplayName("Should execute entry and exit actions")
    void testEntryAndExitActions() {
        List<String> actions = new ArrayList<>();

        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onEntry(TrafficLightState.RED, () -> actions.add("Enter RED"))
                .onExit(TrafficLightState.RED, () -> actions.add("Exit RED"))
                .onEntry(TrafficLightState.GREEN, () -> actions.add("Enter GREEN"))
                .onExit(TrafficLightState.GREEN, () -> actions.add("Exit GREEN"))
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .build();

        // Entry action for initial state should be called
        assertEquals(List.of("Enter RED"), actions);

        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertEquals(List.of("Enter RED", "Exit RED", "Enter GREEN"), actions);
    }

    @Test
    @DisplayName("Should notify listeners on state change")
    void testStateChangeListener() {
        List<String> events = new ArrayList<>();

        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .addListener((previousState, event) -> events.add(previousState + " -> " + event))
                .build();

        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertEquals(List.of("RED -> CHANGE"), events);
    }

    @Test
    @DisplayName("Should support multiple listeners")
    void testMultipleListeners() {
        AtomicInteger listener1Count = new AtomicInteger(0);
        AtomicInteger listener2Count = new AtomicInteger(0);

        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .onTransition(TrafficLightState.GREEN, TrafficLightEvent.CHANGE, TrafficLightState.YELLOW)
                .addListener((s, e) -> listener1Count.incrementAndGet())
                .addListener((s, e) -> listener2Count.incrementAndGet())
                .build();

        sm.sendEvent(TrafficLightEvent.CHANGE);
        sm.sendEvent(TrafficLightEvent.CHANGE);

        assertEquals(2, listener1Count.get());
        assertEquals(2, listener2Count.get());
    }

    @Test
    @DisplayName("Should support guard conditions")
    void testGuardCondition() {
        AtomicInteger counter = new AtomicInteger(0);

        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN,
                        (state, context) -> counter.get() > 0)
                .build();

        // Guard fails - counter is 0
        assertFalse(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.RED, sm.getCurrentState());

        // Guard passes - counter is now > 0
        counter.incrementAndGet();
        assertTrue(sm.sendEvent(TrafficLightEvent.CHANGE));
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState());
    }

    @Test
    @DisplayName("Should support context in sendEvent")
    void testContextInSendEvent() {
        StateMachine<OrderState, Object> sm = StateMachine.<OrderState, Object>builder()
                .initialState(OrderState.PENDING)
                .onTransition(OrderState.PENDING, OrderEvent.PAY, OrderState.PAID,
                        (state, context) -> context != null && (Boolean) context)
                .onTransition(OrderState.PAID, OrderEvent.SHIP, OrderState.SHIPPED)
                .build();

        // Payment fails without context
        assertFalse(sm.sendEvent(OrderEvent.PAY));
        assertEquals(OrderState.PENDING, sm.getCurrentState());

        // Payment succeeds with true context
        assertTrue(sm.sendEvent(OrderEvent.PAY, true));
        assertEquals(OrderState.PAID, sm.getCurrentState());

        // Ship doesn't need context
        assertTrue(sm.sendEvent(OrderEvent.SHIP));
        assertEquals(OrderState.SHIPPED, sm.getCurrentState());
    }

    @Test
    @DisplayName("Should reset to initial state")
    void testReset() {
        List<String> actions = new ArrayList<>();

        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onEntry(TrafficLightState.RED, () -> actions.add("Enter RED"))
                .onExit(TrafficLightState.RED, () -> actions.add("Exit RED"))
                .onEntry(TrafficLightState.GREEN, () -> actions.add("Enter GREEN"))
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .build();

        actions.clear(); // Clear initial entry action

        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState());

        sm.reset();
        assertEquals(TrafficLightState.RED, sm.getCurrentState());
        assertTrue(actions.contains("Enter RED"));
    }

    @Test
    @DisplayName("Should check if event can be sent")
    void testCanSendEvent() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .build();

        assertTrue(sm.canSendEvent(TrafficLightEvent.CHANGE));

        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertFalse(sm.canSendEvent(TrafficLightEvent.CHANGE));
    }

    @Test
    @DisplayName("Should get possible events from current state")
    void testGetPossibleEvents() {
        StateMachine<OrderState, OrderEvent> sm = StateMachine.<OrderState, OrderEvent>builder()
                .initialState(OrderState.PENDING)
                .onTransition(OrderState.PENDING, OrderEvent.PAY, OrderState.PAID)
                .onTransition(OrderState.PENDING, OrderEvent.CANCEL, OrderState.CANCELLED)
                .build();

        var possibleEvents = sm.getPossibleEvents();
        assertEquals(2, possibleEvents.size());
        assertTrue(possibleEvents.contains(OrderEvent.PAY));
        assertTrue(possibleEvents.contains(OrderEvent.CANCEL));
    }

    @Test
    @DisplayName("Should get transitions for a state")
    void testGetTransitions() {
        StateMachine<OrderState, OrderEvent> sm = StateMachine.<OrderState, OrderEvent>builder()
                .initialState(OrderState.PENDING)
                .onTransition(OrderState.PENDING, OrderEvent.PAY, OrderState.PAID)
                .onTransition(OrderState.PENDING, OrderEvent.CANCEL, OrderState.CANCELLED)
                .onTransition(OrderState.PAID, OrderEvent.SHIP, OrderState.SHIPPED)
                .build();

        var pendingTransitions = sm.getTransitions(OrderState.PENDING);
        assertEquals(2, pendingTransitions.size());

        var paidTransitions = sm.getTransitions(OrderState.PAID);
        assertEquals(1, paidTransitions.size());

        var deliveredTransitions = sm.getTransitions(OrderState.DELIVERED);
        assertTrue(deliveredTransitions.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception if not initialized")
    void testNotInitialized() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>();
        sm.initialState(TrafficLightState.RED);

        assertThrows(IllegalStateException.class, () -> sm.sendEvent(TrafficLightEvent.CHANGE));
    }

    @Test
    @DisplayName("Should throw exception if build called without initial state")
    void testBuildWithoutInitialState() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>();

        assertThrows(IllegalStateException.class, sm::build);
    }

    @Test
    @DisplayName("Should support complex order workflow")
    void testComplexOrderWorkflow() {
        List<String> history = new ArrayList<>();

        StateMachine<OrderState, OrderEvent> sm = StateMachine.<OrderState, OrderEvent>builder()
                .initialState(OrderState.PENDING)
                .onTransition(OrderState.PENDING, OrderEvent.PAY, OrderState.PAID)
                .onTransition(OrderState.PENDING, OrderEvent.CANCEL, OrderState.CANCELLED)
                .onTransition(OrderState.PAID, OrderEvent.SHIP, OrderState.SHIPPED)
                .onTransition(OrderState.PAID, OrderEvent.CANCEL, OrderState.CANCELLED)
                .onTransition(OrderState.SHIPPED, OrderEvent.DELIVER, OrderState.DELIVERED)
                .onEntry(OrderState.DELIVERED, () -> history.add("Order delivered!"))
                .onEntry(OrderState.CANCELLED, () -> history.add("Order cancelled!"))
                .build();

        final StateMachine<OrderState, OrderEvent> finalSm = sm;
        sm.addListener((previousState, event) -> history.add(previousState + " --" + event + "-> " + finalSm.getCurrentState()));

        // Pay for order
        sm.sendEvent(OrderEvent.PAY);
        assertEquals(OrderState.PAID, sm.getCurrentState());

        // Ship order
        sm.sendEvent(OrderEvent.SHIP);
        assertEquals(OrderState.SHIPPED, sm.getCurrentState());

        // Deliver order
        sm.sendEvent(OrderEvent.DELIVER);
        assertEquals(OrderState.DELIVERED, sm.getCurrentState());

        assertTrue(history.contains("Order delivered!"));
    }

    @Test
    @DisplayName("Should support direct API without builder")
    void testDirectApi() {
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>();
        sm.initialState(TrafficLightState.RED)
          .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
          .onTransition(TrafficLightState.GREEN, TrafficLightEvent.CHANGE, TrafficLightState.YELLOW);
        sm.build();

        assertEquals(TrafficLightState.RED, sm.getCurrentState());
        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState());
    }

    @Test
    @DisplayName("Should support String states and events")
    void testStringStatesAndEvents() {
        StateMachine<String, String> sm = StateMachine.<String, String>builder()
                .initialState("OFF")
                .onTransition("OFF", "TOGGLE", "ON")
                .onTransition("ON", "TOGGLE", "OFF")
                .build();

        assertEquals("OFF", sm.getCurrentState());
        sm.sendEvent("TOGGLE");
        assertEquals("ON", sm.getCurrentState());
        sm.sendEvent("TOGGLE");
        assertEquals("OFF", sm.getCurrentState());
    }

    @Test
    @DisplayName("Should remove listener correctly")
    void testRemoveListener() {
        AtomicInteger count = new AtomicInteger(0);
        StateMachine.StateChangeListener<TrafficLightState, TrafficLightEvent> listener =
                (s, e) -> count.incrementAndGet();

        StateMachine<TrafficLightState, TrafficLightEvent> sm = StateMachine.<TrafficLightState, TrafficLightEvent>builder()
                .initialState(TrafficLightState.RED)
                .onTransition(TrafficLightState.RED, TrafficLightEvent.CHANGE, TrafficLightState.GREEN)
                .onTransition(TrafficLightState.GREEN, TrafficLightEvent.CHANGE, TrafficLightState.YELLOW)
                .addListener(listener)
                .build();

        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertEquals(1, count.get());

        sm.removeListener(listener);
        sm.sendEvent(TrafficLightEvent.CHANGE);
        assertEquals(1, count.get()); // Should not increment
    }
}
