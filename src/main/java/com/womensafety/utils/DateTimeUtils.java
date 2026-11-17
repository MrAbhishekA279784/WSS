package com.womensafety.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Timestamp serialization and display formatting utilities.
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter STORAGE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private DateTimeUtils() {
    }

    public static String nowIso() {
        return LocalDateTime.now().withNano(0).format(STORAGE_FORMAT);
    }

    public static String formatForDisplay(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) {
            return "—";
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(isoTimestamp.trim(), STORAGE_FORMAT);
            return dt.format(DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            return isoTimestamp;
        }
    }
}
