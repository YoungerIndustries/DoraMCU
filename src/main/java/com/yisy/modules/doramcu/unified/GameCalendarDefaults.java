package com.yisy.modules.doramcu.unified;

import java.time.LocalDate;

/**
 * Provides default starting dates for each game type's calendar.
 * <p>
 * These dates are chosen to fit the lore of each game system:
 * <ul>
 *   <li><b>Traveller</b> — Starts at 1107 Crusade, the beginning of the Crusade era
 *       in the Traveller universe, when the Third Imperium began its expansion.</li>
 *   <li><b>Captain's Log</b> — Starts at 2370-01-01, the start of the Voyager era
 *       in Star Trek, a fitting starting point for a Star Trek Adventures RPG.</li>
 *   <li><b>DoraMCU 4X</b> — Starts at 2330-01-01, the early 24th century when
 *       the Federation is expanding and new empires are rising.</li>
 * </ul>
 * </p>
 * <p>
 * Each turn in the global clock represents one in-game day. The starting date
 * can be overridden per session if a different narrative starting point is desired.
 * </p>
 */
public final class GameCalendarDefaults {

    /**
     * Traveller starting date: 1107 Crusade, day 1.
     * <p>
     * The Crusade era begins in 1107 C., marking the start of the Third Imperium's
     * great expansion. This is a canonical starting point for Traveller campaigns.
     * </p>
     */
    public static final LocalDate TRAVELLER_START_DATE = LocalDate.of(1107, 1, 1);

    /**
     * Captain's Log starting date: January 1, 2370.
     * <p>
     * The year 2370 marks the beginning of Star Trek: Voyager, a fitting era
     * for a Star Trek Adventures solo RPG campaign.
     * </p>
     */
    public static final LocalDate CAPTAINS_LOG_START_DATE = LocalDate.of(2370, 1, 1);

    /**
     * DoraMCU 4X starting date: January 1, 2330.
     * <p>
     * The early 24th century, before the Dominion War, when new empires are
     * forming and the galaxy is ripe for conquest and diplomacy.
     * </p>
     */
    public static final LocalDate DORAMCU_4X_START_DATE = LocalDate.of(2330, 1, 1);

    private GameCalendarDefaults() {
        // Utility class — no instantiation
    }

    /**
     * Returns the default starting date for the given game type.
     *
     * @param gameType the game type
     * @return the default starting date
     */
    public static LocalDate getStartDate(GameType gameType) {
        switch (gameType) {
            case TRAVELLER:
                return TRAVELLER_START_DATE;
            case CAPTAINS_LOG:
                return CAPTAINS_LOG_START_DATE;
            case DORAMCU_4X:
                return DORAMCU_4X_START_DATE;
            default:
                throw new IllegalArgumentException("Unknown game type: " + gameType);
        }
    }

    /**
     * Returns a description of the calendar era for the given game type.
     *
     * @param gameType the game type
     * @return a human-readable era description
     */
    public static String getEraDescription(GameType gameType) {
        switch (gameType) {
            case TRAVELLER:
                return "Crusade Era (1107 C.)";
            case CAPTAINS_LOG:
                return "Voyager Era (2370 AD)";
            case DORAMCU_4X:
                return "New Empire Era (2330 AD)";
            default:
                return "Unknown Era";
        }
    }
}
