package com.yisy.modules.doramcu;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A generic finite state machine implementation that supports type-safe states and events.
 * <p>
 * This class provides a flexible way to model state transitions with:
 * <ul>
 *   <li>Type-safe states and events using generics</li>
 *   <li>Transition guards (conditions that must be met for a transition to occur)</li>
 *   <li>Entry and exit actions for states</li>
 *   <li>State change listeners for observing transitions</li>
 *   <li>Fluent API for building state machines</li>
 * </ul>
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @author Junie
 */
public class StateMachine<S, E> {

    /**
     * Represents a transition from one state to another triggered by an event.
     */
    public static class Transition<S, E> {
        private final S fromState;
        private final E event;
        private final S toState;
        private final PredicateWithEvent<S, E> guard;

        /**
         * Creates a transition without a guard condition.
         *
         * @param fromState the source state
         * @param event     the triggering event
         * @param toState   the target state
         */
        public Transition(S fromState, E event, S toState) {
            this(fromState, event, toState, null);
        }

        /**
         * Creates a transition with an optional guard condition.
         *
         * @param fromState the source state
         * @param event     the triggering event
         * @param toState   the target state
         * @param guard     optional guard condition; if null, the transition always succeeds
         */
        public Transition(S fromState, E event, S toState, PredicateWithEvent<S, E> guard) {
            this.fromState = fromState;
            this.event = event;
            this.toState = toState;
            this.guard = guard;
        }

        public S getFromState() {
            return fromState;
        }

        public E getEvent() {
            return event;
        }

        public S getToState() {
            return toState;
        }

        public PredicateWithEvent<S, E> getGuard() {
            return guard;
        }

        /**
         * Checks if this transition can be taken given the current state and context.
         *
         * @param currentState the current state of the machine
         * @param context      optional context data for guard evaluation
         * @return true if the transition is valid and guard passes (or no guard)
         */
        @SuppressWarnings("unchecked")
        public boolean canTransition(S currentState, Object context) {
            if (!fromState.equals(currentState)) {
                return false;
            }
            if (guard == null) {
                return true;
            }
            return guard.test(currentState, (E) context);
        }
    }

    /**
     * Functional interface for a predicate that takes a state and context.
     *
     * @param <S> the type of state
     * @param <E> the type of event/context
     */
    @FunctionalInterface
    public interface PredicateWithEvent<S, E> {
        boolean test(S state, E context);
    }

    /**
     * Functional interface for state change listeners.
     *
     * @param <S> the type of state
     * @param <E> the type of event
     */
    public interface StateChangeListener<S, E> extends BiConsumer<S, E> {
        @Override
        void accept(S previousState, E event);
    }

    private final Map<S, List<Transition<S, E>>> transitionMap;
    private final List<StateChangeListener<S, E>> listeners;
    private final Map<S, Runnable> entryActions;
    private final Map<S, Runnable> exitActions;
    private S initialState;
    private S currentState;
    private boolean initialized;

    public StateMachine() {
        this.transitionMap = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.entryActions = new HashMap<>();
        this.exitActions = new HashMap<>();
        this.initialized = false;
    }

    /**
     * Creates a new StateMachine instance sharing the immutable transition map,
     * entry/exit actions, and initial state from a template instance.
     *
     * @param template the template state machine
     */
    public StateMachine(StateMachine<S, E> template) {
        this.transitionMap = template.transitionMap;
        this.listeners = new CopyOnWriteArrayList<>();
        this.entryActions = template.entryActions;
        this.exitActions = template.exitActions;
        this.initialState = template.initialState;
        this.currentState = template.initialState;
        this.initialized = true;
    }

    /**
     * Sets the initial state of the state machine.
     *
     * @param initialState the initial state
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> initialState(S initialState) {
        this.initialState = initialState;
        this.currentState = initialState;
        return this;
    }

    /**
     * Adds a transition from one state to another triggered by an event.
     *
     * @param fromState the source state
     * @param event     the triggering event
     * @param toState   the target state
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> onTransition(S fromState, E event, S toState) {
        return onTransition(fromState, event, toState, null);
    }

    /**
     * Adds a transition with a guard condition.
     *
     * @param fromState the source state
     * @param event     the triggering event
     * @param toState   the target state
     * @param guard     optional guard condition
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> onTransition(S fromState, E event, S toState, PredicateWithEvent<S, E> guard) {
        Transition<S, E> transition = new Transition<>(fromState, event, toState, guard);
        transitionMap.computeIfAbsent(fromState, k -> new ArrayList<>()).add(transition);
        return this;
    }

    /**
     * Registers an entry action that executes when entering a state.
     *
     * @param state   the state to register the action for
     * @param action  the action to execute on entry
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> onEntry(S state, Runnable action) {
        entryActions.put(state, action);
        return this;
    }

    /**
     * Registers an exit action that executes when leaving a state.
     *
     * @param state   the state to register the action for
     * @param action  the action to execute on exit
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> onExit(S state, Runnable action) {
        exitActions.put(state, action);
        return this;
    }

    /**
     * Registers a listener that is notified of all state changes.
     *
     * @param listener the listener to register
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> addListener(StateChangeListener<S, E> listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     * @return this state machine for method chaining
     */
    public StateMachine<S, E> removeListener(StateChangeListener<S, E> listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * Returns the current state of the state machine.
     *
     * @return the current state
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * Checks if the state machine is in a specific state.
     *
     * @param state the state to check
     * @return true if the current state equals the given state
     */
    public boolean isInState(S state) {
        return currentState != null && currentState.equals(state);
    }

    /**
     * Processes an event and attempts to transition to a new state.
     *
     * @param event the event to process
     * @return true if a transition occurred, false otherwise
     * @throws IllegalStateException if the state machine is not initialized
     */
    public boolean sendEvent(E event) {
        return sendEvent(event, null);
    }

    /**
     * Processes an event with optional context data and attempts to transition to a new state.
     *
     * @param event   the event to process
     * @param context optional context data for guard evaluation
     * @return true if a transition occurred, false otherwise
     * @throws IllegalStateException if the state machine is not initialized
     */
    public boolean sendEvent(E event, Object context) {
        if (!initialized) {
            throw new IllegalStateException("State machine is not initialized. Call build() first.");
        }

        List<Transition<S, E>> transitions = transitionMap.get(currentState);
        if (transitions == null || transitions.isEmpty()) {
            return false;
        }

        for (Transition<S, E> transition : transitions) {
            if (transition.getEvent().equals(event) && transition.canTransition(currentState, context)) {
                S previousState = currentState;
                S nextState = transition.getToState();

                // Execute exit action for current state
                Runnable exitAction = exitActions.get(previousState);
                if (exitAction != null) {
                    exitAction.run();
                }

                // Update current state
                currentState = nextState;

                // Execute entry action for new state
                Runnable entryAction = entryActions.get(nextState);
                if (entryAction != null) {
                    entryAction.run();
                }

                // Notify listeners
                for (StateChangeListener<S, E> listener : listeners) {
                    listener.accept(previousState, event);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Resets the state machine to its initial state.
     */
    public void reset() {
        if (!initialized) {
            throw new IllegalStateException("State machine is not initialized. Call build() first.");
        }

        S previousState = currentState;
        
        // Execute exit action for current state
        Runnable exitAction = exitActions.get(previousState);
        if (exitAction != null) {
            exitAction.run();
        }

        currentState = initialState;

        // Execute entry action for initial state
        Runnable entryAction = entryActions.get(initialState);
        if (entryAction != null) {
            entryAction.run();
        }
    }

    /**
     * Initializes the state machine and sets the current state to the initial state.
     * Must be called before sending events.
     */
    public void build() {
        if (initialState == null) {
            throw new IllegalStateException("Initial state must be set before building the state machine.");
        }
        currentState = initialState;
        initialized = true;

        // Execute entry action for initial state
        Runnable entryAction = entryActions.get(initialState);
        if (entryAction != null) {
            entryAction.run();
        }
    }

    /**
     * Checks if the state machine can process the given event from the current state.
     *
     * @param event the event to check
     * @return true if a valid transition exists for the event
     */
    public boolean canSendEvent(E event) {
        return canSendEvent(event, null);
    }

    /**
     * Checks if the state machine can process the given event with context from the current state.
     *
     * @param event   the event to check
     * @param context optional context data for guard evaluation
     * @return true if a valid transition exists for the event
     */
    public boolean canSendEvent(E event, Object context) {
        List<Transition<S, E>> transitions = transitionMap.get(currentState);
        if (transitions == null) {
            return false;
        }

        for (Transition<S, E> transition : transitions) {
            if (transition.getEvent().equals(event) && transition.canTransition(currentState, context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all possible events that can be processed from the current state.
     *
     * @return a list of possible events
     */
    public List<E> getPossibleEvents() {
        return getPossibleEvents(null);
    }

    /**
     * Returns all possible events that can be processed from the current state with context.
     *
     * @param context optional context data for guard evaluation
     * @return a list of possible events
     */
    @SuppressWarnings("unchecked")
    public List<E> getPossibleEvents(Object context) {
        List<E> events = new ArrayList<>();
        List<Transition<S, E>> transitions = transitionMap.get(currentState);
        if (transitions != null) {
            for (Transition<S, E> transition : transitions) {
                if (transition.canTransition(currentState, context)) {
                    events.add(transition.getEvent());
                }
            }
        }
        return events;
    }

    /**
     * Returns all transitions defined for a given state.
     *
     * @param state the state to query
     * @return a list of transitions from the given state
     */
    public List<Transition<S, E>> getTransitions(S state) {
        return Optional.ofNullable(transitionMap.get(state))
                .map(Collections::unmodifiableList)
                .orElse(Collections.emptyList());
    }

    /**
     * Builder class for creating StateMachine instances with a fluent API.
     *
     * @param <S> the type of state
     * @param <E> the type of event
     */
    public static class Builder<S, E> {
        private final StateMachine<S, E> stateMachine;

        public Builder() {
            this.stateMachine = new StateMachine<>();
        }

        /**
         * Sets the initial state.
         *
         * @param initialState the initial state
         * @return this builder for method chaining
         */
        public Builder<S, E> initialState(S initialState) {
            stateMachine.initialState(initialState);
            return this;
        }

        /**
         * Adds a transition.
         *
         * @param fromState the source state
         * @param event     the triggering event
         * @param toState   the target state
         * @return this builder for method chaining
         */
        public Builder<S, E> onTransition(S fromState, E event, S toState) {
            return onTransition(fromState, event, toState, null);
        }

        /**
         * Adds a transition with a guard condition.
         *
         * @param fromState the source state
         * @param event     the triggering event
         * @param toState   the target state
         * @param guard     optional guard condition
         * @return this builder for method chaining
         */
        public Builder<S, E> onTransition(S fromState, E event, S toState, PredicateWithEvent<S, E> guard) {
            stateMachine.onTransition(fromState, event, toState, guard);
            return this;
        }

        /**
         * Registers an entry action.
         *
         * @param state  the state to register the action for
         * @param action the action to execute on entry
         * @return this builder for method chaining
         */
        public Builder<S, E> onEntry(S state, Runnable action) {
            stateMachine.onEntry(state, action);
            return this;
        }

        /**
         * Registers an exit action.
         *
         * @param state  the state to register the action for
         * @param action the action to execute on exit
         * @return this builder for method chaining
         */
        public Builder<S, E> onExit(S state, Runnable action) {
            stateMachine.onExit(state, action);
            return this;
        }

        /**
         * Registers a state change listener.
         *
         * @param listener the listener to register
         * @return this builder for method chaining
         */
        public Builder<S, E> addListener(StateChangeListener<S, E> listener) {
            stateMachine.addListener(listener);
            return this;
        }

        /**
         * Builds and initializes the state machine.
         *
         * @return the built state machine
         */
        public StateMachine<S, E> build() {
            stateMachine.build();
            return stateMachine;
        }
    }

    /**
     * Creates a new builder for constructing a StateMachine.
     *
     * @param <S> the type of state
     * @param <E> the type of event
     * @return a new builder instance
     */
    public static <S, E> Builder<S, E> builder() {
        return new Builder<>();
    }
}
