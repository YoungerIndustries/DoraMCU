package com.yisy.modules.doramcu;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A generic finite state machine with builder pattern support.
 * <p>
 * Supports transitions with guard conditions, entry/exit actions,
 * state change listeners, and context-aware event sending.
 * </p>
 *
 * @param <S> the state type
 * @param <E> the event type
 */
public class StateMachine<S, E> {

    /**
     * Functional interface for state change listeners.
     */
    @FunctionalInterface
    public interface StateChangeListener<S, E> {
        void onStateChanged(S previousState, E event);
    }

    /**
     * A single transition definition.
     */
    private static class Transition<S, E> {
        final S fromState;
        final E event;
        final S toState;
        final BiPredicate<S, Object> guard;

        Transition(S fromState, E event, S toState, BiPredicate<S, Object> guard) {
            this.fromState = fromState;
            this.event = event;
            this.toState = toState;
            this.guard = guard;
        }
    }

    private S initialState;
    private S currentState;
    private final List<Transition<S, E>> transitions = new ArrayList<>();
    private final Map<S, Runnable> entryActions = new HashMap<>();
    private final Map<S, Runnable> exitActions = new HashMap<>();
    private final CopyOnWriteArrayList<StateChangeListener<S, E>> listeners = new CopyOnWriteArrayList<>();
    private boolean built = false;

    public StateMachine() {
    }

    /**
     * Starts building a new StateMachine instance.
     */
    public static <S, E> StateMachine<S, E> builder() {
        return new StateMachine<>();
    }

    /**
     * Sets the initial state.
     */
    public StateMachine<S, E> initialState(S state) {
        this.initialState = state;
        return this;
    }

    /**
     * Adds a transition without guard.
     */
    public StateMachine<S, E> onTransition(S from, E event, S to) {
        return onTransition(from, event, to, (s, ctx) -> true);
    }

    /**
     * Adds a transition with a guard condition.
     */
    public StateMachine<S, E> onTransition(S from, E event, S to, BiPredicate<S, Object> guard) {
        transitions.add(new Transition<>(from, event, to, guard));
        return this;
    }

    /**
     * Registers an entry action for a state.
     */
    public StateMachine<S, E> onEntry(S state, Runnable action) {
        entryActions.put(state, action);
        return this;
    }

    /**
     * Registers an exit action for a state.
     */
    public StateMachine<S, E> onExit(S state, Runnable action) {
        exitActions.put(state, action);
        return this;
    }

    /**
     * Adds a state change listener.
     */
    public StateMachine<S, E> addListener(StateChangeListener<S, E> listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Removes a state change listener.
     */
    public void removeListener(StateChangeListener<S, E> listener) {
        listeners.remove(listener);
    }

    /**
     * Finalizes the state machine configuration.
     * Returns this for builder chaining.
     */
    public StateMachine<S, E> build() {
        if (initialState == null) {
            throw new IllegalStateException("Initial state must be set before building");
        }
        currentState = initialState;
        built = true;
        // Execute entry action for initial state
        Runnable entry = entryActions.get(initialState);
        if (entry != null) {
            entry.run();
        }
        return this;
    }

    /**
     * Returns the current state.
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * Checks if the machine is in the given state.
     */
    public boolean isInState(S state) {
        return currentState != null && currentState.equals(state);
    }

    /**
     * Sends an event without context.
     */
    public boolean sendEvent(E event) {
        return sendEvent(event, null);
    }

    /**
     * Sends an event with context.
     */
    public boolean sendEvent(E event, Object context) {
        if (!built) {
            throw new IllegalStateException("StateMachine not initialized. Call build() first.");
        }

        Transition<S, E> matched = null;
        for (Transition<S, E> t : transitions) {
            if (t.fromState.equals(currentState) && t.event.equals(event)) {
                if (t.guard.test(currentState, context)) {
                    matched = t;
                    break;
                }
            }
        }

        if (matched == null) {
            return false;
        }

        S previousState = currentState;

        // Execute exit action
        Runnable exit = exitActions.get(previousState);
        if (exit != null) {
            exit.run();
        }

        // Transition
        currentState = matched.toState;

        // Execute entry action
        Runnable entry = entryActions.get(currentState);
        if (entry != null) {
            entry.run();
        }

        // Notify listeners
        for (StateChangeListener<S, E> listener : listeners) {
            listener.onStateChanged(previousState, event);
        }

        return true;
    }

    /**
     * Checks if an event can be sent from the current state.
     */
    public boolean canSendEvent(E event) {
        if (!built) return false;
        for (Transition<S, E> t : transitions) {
            if (t.fromState.equals(currentState) && t.event.equals(event)) {
                if (t.guard.test(currentState, null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the list of events that can be sent from the current state.
     */
    public List<E> getPossibleEvents() {
        List<E> events = new ArrayList<>();
        if (!built) return events;
        for (Transition<S, E> t : transitions) {
            if (t.fromState.equals(currentState) && t.guard.test(currentState, null)) {
                if (!events.contains(t.event)) {
                    events.add(t.event);
                }
            }
        }
        return events;
    }

    /**
     * Returns all transitions defined for a given state.
     */
    public List<Transition<S, E>> getTransitions(S state) {
        List<Transition<S, E>> result = new ArrayList<>();
        for (Transition<S, E> t : transitions) {
            if (t.fromState.equals(state)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Resets the state machine to its initial state.
     */
    public void reset() {
        if (!built) return;

        // Execute exit action for current state
        Runnable exit = exitActions.get(currentState);
        if (exit != null) {
            exit.run();
        }

        currentState = initialState;

        // Execute entry action for initial state
        Runnable entry = entryActions.get(currentState);
        if (entry != null) {
            entry.run();
        }
    }
}
