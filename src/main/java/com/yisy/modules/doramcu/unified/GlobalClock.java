package com.yisy.modules.doramcu.unified;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global clock that tracks time across all three game systems (Traveller,
 * Captain's Log, and DoraMCU 4X).
 * <p>
 * Each turn represents one in-game day. The clock advances by one day every
 * time {@link #advanceTurn()} is called. The default turn interval is 30 seconds
 * of real time, configured via {@code doramcu.turn.interval.seconds}.
 * </p>
 * <p>
 * The clock maintains a global turn counter and a starting date that can be
 * customized per game type. Each game type uses its own {@link GameCalendar}
 * to format dates according to its lore.
 * </p>
 */
public class GlobalClock {

    /** Default turn interval: 30 seconds per in-game day. */
    public static final long DEFAULT_TURN_INTERVAL_SECONDS = 30;

    /** Formatter for ISO-8601 date output. */
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    /** The starting date of the global clock (in-game day 0). */
    private final LocalDate startDate;

    /** The starting wall-clock time when the clock was created. */
    private final LocalDateTime startWallClock;

    /** Current turn count (each turn = one in-game day). */
    private final AtomicInteger turnCount = new AtomicInteger(0);

    /**
     * Creates a new global clock with the given starting date.
     *
     * @param startDate the in-game starting date (day 0)
     */
    public GlobalClock(LocalDate startDate) {
        this.startDate = startDate;
        this.startWallClock = LocalDateTime.now();
    }

    /**
     * Creates a new global clock starting at the given date for the specified
     * game type. Uses the default starting date for that game type.
     *
     * @param gameType the game type to determine the starting date
     * @return a new GlobalClock instance
     */
    public static GlobalClock forGameType(GameType gameType) {
        return new GlobalClock(GameCalendarDefaults.getStartDate(gameType));
    }

    /**
     * Advances the clock by one turn (one in-game day).
     *
     * @return the new turn number after advancing
     */
    public int advanceTurn() {
        return turnCount.incrementAndGet();
    }

    /**
     * Advances the clock by the specified number of turns (days).
     *
     * @param turns the number of turns to advance
     * @return the new turn number after advancing
     */
    public int advanceTurns(int turns) {
        if (turns < 0) {
            throw new IllegalArgumentException("Turns must be non-negative: " + turns);
        }
        return turnCount.addAndGet(turns);
    }

    /**
     * Returns the current turn number.
     *
     * @return the current turn number (0-based)
     */
    public int getCurrentTurn() {
        return turnCount.get();
    }

    /**
     * Returns the current in-game date.
     * <p>
     * The date is calculated as {@code startDate + turnCount days}.
     * </p>
     *
     * @return the current in-game date
     */
    public LocalDate getCurrentDate() {
        return startDate.plusDays(turnCount.get());
    }

    /**
     * Returns the current in-game date formatted according to the given game type's
     * calendar conventions.
     *
     * @param gameType the game type whose calendar format to use
     * @return the formatted date string
     */
    public String getFormattedDate(GameType gameType) {
        LocalDate currentDate = getCurrentDate();
        return GameCalendarFormatter.format(currentDate, gameType);
    }

    /**
     * Returns the starting date of the clock.
     *
     * @return the starting date (day 0)
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the wall-clock time when this clock was created.
     *
     * @return the start wall-clock time
     */
    public LocalDateTime getStartWallClock() {
        return startWallClock;
    }

    /**
     * Returns the number of in-game days that have elapsed since the start date.
     *
     * @return the elapsed days (same as current turn count)
     */
    public int getElapsedDays() {
        return turnCount.get();
    }

    /**
     * Returns the current date as an ISO-8601 string.
     *
     * @return the ISO date string (e.g., "2330-01-15")
     */
    public String getIsoDate() {
        return getCurrentDate().format(ISO_DATE);
    }

    /**
     * Resets the clock to the given turn number.
     * <p>
     * This is primarily used for testing or when loading a saved game state.
     * </p>
     *
     * @param turn the turn number to set
     */
    public void setTurn(int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException("Turn must be non-negative: " + turn);
        }
        turnCount.set(turn);
    }

    /**
     * Returns a summary of the current clock state.
     *
     * @return a human-readable summary
     */
    public String getSummary() {
        return String.format(
            "Turn %d | Date: %s | Elapsed: %d days since %s",
            turnCount.get(),
            getCurrentDate().format(ISO_DATE),
            getElapsedDays(),
            startDate.format(ISO_DATE)
        );
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
