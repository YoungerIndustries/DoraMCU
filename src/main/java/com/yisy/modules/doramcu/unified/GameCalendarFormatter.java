package com.yisy.modules.doramcu.unified;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Formats dates according to each game type's calendar conventions.
 * <p>
 * Each game system has its own way of expressing dates:
 * <ul>
 *   <li><b>Traveller</b> — Uses the Crusade calendar (e.g., "1107 C., January 15")
 *       with era designations like C. (Crusade), I. (Imperium), etc.</li>
 *   <li><b>Captain's Log</b> — Uses Star Date format alongside AD dates
 *       (e.g., "Star Date 48315.2 | 2370-01-15").</li>
 *   <li><b>DoraMCU 4X</b> — Uses standard AD dates with era markers
 *       (e.g., "2330-01-15 AD, New Empire Era").</li>
 * </ul>
 * </p>
 */
public final class GameCalendarFormatter {

    /** Month names used in Traveller's Crusade calendar. */
    private static final String[] TRAVELLER_MONTHS = {
        "Januar", "Februar", "Marz", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember"
    };

    /** Star Trek base star date for year 2370. */
    private static final double STAR_DATE_BASE_2370 = 47000.0;

    private GameCalendarFormatter() {
        // Utility class — no instantiation
    }

    /**
     * Formats a date according to the given game type's calendar conventions.
     *
     * @param date     the date to format
     * @param gameType the game type whose calendar format to use
     * @return the formatted date string
     */
    public static String format(LocalDate date, GameType gameType) {
        switch (gameType) {
            case TRAVELLER:
                return formatTraveller(date);
            case CAPTAINS_LOG:
                return formatCaptainsLog(date);
            case DORAMCU_4X:
                return formatDoraMCU4X(date);
            default:
                return date.toString();
        }
    }

    /**
     * Formats a date in Traveller's Crusade calendar style.
     * <p>
     * Example: "1107 C., Januar 15"
     * </p>
     *
     * @param date the date to format
     * @return the formatted Traveller date
     */
    public static String formatTraveller(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // Determine era designation
        String era = getTravellerEra(year);

        // Use Traveller month names
        String monthName = TRAVELLER_MONTHS[month - 1];

        return String.format("%d %s, %s %d", year, era, monthName, day);
    }

    /**
     * Returns the era designation for a Traveller year.
     * <p>
     * Traveller eras:
     * <ul>
     *   <li>Before 1107: BC (Before Crusade)</li>
     *   <li>1107-1129: C (Crusade)</li>
     *   <li>1130-1149: I (Imperium)</li>
     *   <li>1150-1179: H (High Imperium)</li>
     *   <li>1180-1209: D (Decline)</li>
     *   <li>1210-1299: R (Renaissance)</li>
     *   <li>1300+: L (Legacy)</li>
     * </ul>
     * </p>
     *
     * @param year the Traveller year
     * @return the era designation
     */
    public static String getTravellerEra(int year) {
        if (year < 1107) {
            return "BC";
        } else if (year < 1130) {
            return "C";
        } else if (year < 1150) {
            return "I";
        } else if (year < 1180) {
            return "H";
        } else if (year < 1210) {
            return "D";
        } else if (year < 1300) {
            return "R";
        } else {
            return "L";
        }
    }

    /**
     * Formats a date in Captain's Log (Star Trek) style.
     * <p>
     * Example: "Star Date 48315.2 | 2370-01-15"
     * </p>
     *
     * @param date the date to format
     * @return the formatted Captain's Log date
     */
    public static String formatCaptainsLog(LocalDate date) {
        double starDate = computeStarDate(date);
        String adDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        return String.format("Star Date %.1f | %s", starDate, adDate);
    }

    /**
     * Computes a Star Trek-style star date from a LocalDate.
     * <p>
     * This is a simplified approximation based on the Voyager-era formula:
     * Star Date = 47000 + (days since 2370-01-01) * 3.65
     * </p>
     *
     * @param date the date to convert
     * @return the approximate star date
     */
    public static double computeStarDate(LocalDate date) {
        LocalDate baseDate = LocalDate.of(2370, 1, 1);
        long daysSinceBase = java.time.Duration.between(
            baseDate.atStartOfDay(), date.atStartOfDay()).toDays();

        // Voyager-era formula: ~3.65 star date units per day
        return STAR_DATE_BASE_2370 + (daysSinceBase * 3.65);
    }

    /**
     * Formats a date in DoraMCU 4X style.
     * <p>
     * Example: "2330-01-15 AD, New Empire Era"
     * </p>
     *
     * @param date the date to format
     * @return the formatted DoraMCU 4X date
     */
    public static String formatDoraMCU4X(LocalDate date) {
        String adDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String era = getDoraMCUEra(date);

        return String.format("%s AD, %s", adDate, era);
    }

    /**
     * Returns the era name for a DoraMCU 4X date.
     * <p>
     * Eras:
     * <ul>
     *   <li>Before 2330: Pre-Expansion Era</li>
     *   <li>2330-2369: New Empire Era</li>
     *   <li>2370-2379: Dominion War Era</li>
     *   <li>2380-2399: Post-War Era</li>
     *   <li>2400+: Picard Era</li>
     * </ul>
     * </p>
     *
     * @param date the date
     * @return the era name
     */
    public static String getDoraMCUEra(LocalDate date) {
        int year = date.getYear();
        if (year < 2330) {
            return "Pre-Expansion Era";
        } else if (year < 2370) {
            return "New Empire Era";
        } else if (year < 2380) {
            return "Dominion War Era";
        } else if (year < 2400) {
            return "Post-War Era";
        } else {
            return "Picard Era";
        }
    }

    /**
     * Returns a detailed date string suitable for storytelling/narrative use.
     * <p>
     * Includes the day of the week, full month name, and era information.
     * </p>
     *
     * @param date     the date to format
     * @param gameType the game type
     * @return a narrative-friendly date string
     */
    public static String formatNarrative(LocalDate date, GameType gameType) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(
            TextStyle.FULL, Locale.ENGLISH);

        switch (gameType) {
            case TRAVELLER:
                return String.format(
                    "On %s, the %d day of %s in the year %d %s",
                    dayOfWeek,
                    date.getDayOfMonth(),
                    TRAVELLER_MONTHS[date.getMonthValue() - 1],
                    date.getYear(),
                    getTravellerEra(date.getYear())
                );
            case CAPTAINS_LOG:
                return String.format(
                    "On %s, %s %d, %d (Star Date %.1f)",
                    dayOfWeek,
                    date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    date.getDayOfMonth(),
                    date.getYear(),
                    computeStarDate(date)
                );
            case DORAMCU_4X:
                return String.format(
                    "On %s, %s %d, %d AD — %s",
                    dayOfWeek,
                    date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    date.getDayOfMonth(),
                    date.getYear(),
                    getDoraMCUEra(date)
                );
            default:
                return date.format(DateTimeFormatter.ofPattern(
                    "EEEE, MMMM d, yyyy", Locale.ENGLISH));
        }
    }
}
