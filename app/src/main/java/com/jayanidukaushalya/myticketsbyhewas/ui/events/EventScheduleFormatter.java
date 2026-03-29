package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility for formatting ISO date/time strings from the API into display strings.
 * All ISO strings are treated as UTC (the API returns UTC timestamps).
 */
final class EventScheduleFormatter {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private EventScheduleFormatter() {
    }

    /** Formats an ISO datetime string to "h:mm a" (e.g. "10:00 AM"). */
    static String formatTime(@Nullable String isoDateTime) {
        Date parsed = parseIso(isoDateTime);
        if (parsed == null) return isoDateTime != null ? isoDateTime : "";
        SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.US);
        fmt.setTimeZone(UTC);
        return fmt.format(parsed);
    }

    /** Formats an ISO datetime/date string to "MMM d, yyyy" or "MMM d". */
    static String formatDate(@Nullable String isoDateTime, boolean includeYear) {
        Date parsed = parseIso(isoDateTime);
        if (parsed == null) return isoDateTime != null ? isoDateTime : "";
        String pattern = includeYear ? "MMM d, yyyy" : "MMM d";
        SimpleDateFormat fmt = new SimpleDateFormat(pattern, Locale.US);
        fmt.setTimeZone(UTC);
        return fmt.format(parsed);
    }

    /**
     * Returns a time range string like "10:00 AM – 2:00 PM" or "10:00 AM onwards"
     * when {@code endTime} is null.
     */
    static String formatTimeRange(@Nullable String startTime, @Nullable String endTime) {
        String start = formatTime(startTime);
        if (endTime == null || endTime.isEmpty()) {
            return start + " onwards";
        }
        return start + " – " + formatTime(endTime);
    }

    /**
     * Returns a single-line display string.
     * For single-day events: "Mar 25, 2026 • 10:00 AM"
     * For multi-day: "Mar 25 – Mar 27, 2026 • 10:00 AM"
     */
    static String formatDateTimeDisplay(@Nullable String isoDate, @Nullable String isoEndDate, @Nullable String isoStartTime) {
        if (isoDate == null && isoStartTime == null) return "";
        String datePart;
        if (isoEndDate != null && !isoEndDate.equals(isoDate)) {
            datePart = formatRange(isoDate, isoEndDate);
        } else {
            datePart = formatDate(isoDate != null ? isoDate : isoStartTime, true);
        }
        if (isoStartTime == null) return datePart;
        return datePart + " • " + formatTime(isoStartTime);
    }

    private static String formatRange(@Nullable String start, @Nullable String end) {
        Date d1 = parseIso(start);
        Date d2 = parseIso(end);
        if (d1 == null || d2 == null) return formatDate(start, true);

        SimpleDateFormat sameMonth = new SimpleDateFormat("MMM d", Locale.US);
        SimpleDateFormat withYear = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        sameMonth.setTimeZone(UTC);
        withYear.setTimeZone(UTC);

        // Simple check: if years or months differ, just show Full -> Full
        // But the common case is Mar 25 - Mar 27, 2026
        return sameMonth.format(d1) + " – " + withYear.format(d2);
    }

    @Nullable
    private static Date parseIso(@Nullable String isoString) {
        if (isoString == null || isoString.isEmpty()) return null;
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
        };
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setTimeZone(UTC);
                Date date = sdf.parse(isoString);
                if (date != null) return date;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
