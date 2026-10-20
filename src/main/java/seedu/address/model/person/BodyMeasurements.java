package seedu.address.model.person;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/** Shared validation for exact body measurements and measurement dates. */
public final class BodyMeasurements {
    public static final String DATE_ERROR =
            "Measurement date must be a real date in yyyy-MM-dd format and must not be in the future.";
    public static final String WEIGHT_ERROR =
            "Weight must be greater than 0 and at most 500 kg, with at most 2 decimal places.";
    public static final String HEIGHT_ERROR =
            "Height must be greater than 0 and at most 300 cm, with at most 2 decimal places.";
    public static final String NOTES_ERROR =
            "Progress notes must contain 1 to 300 printable characters without line breaks.";

    private BodyMeasurements() {
    }

    /** Parses strict decimal syntax without rounding or accepting units. */
    public static BigDecimal parseDecimal(String value, boolean height) {
        String trimmed = trimHorizontal(Objects.requireNonNull(value));
        String error = height ? HEIGHT_ERROR : WEIGHT_ERROR;
        if (!trimmed.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException(error);
        }
        return validateDecimal(new BigDecimal(trimmed), height);
    }

    /** Validates model input independently of the command parser. */
    public static BigDecimal validateDecimal(BigDecimal value, boolean height) {
        Objects.requireNonNull(value);
        if (value.scale() > 2 || value.signum() <= 0
                || value.compareTo(BigDecimal.valueOf(height ? 300 : 500)) > 0) {
            throw new IllegalArgumentException(height ? HEIGHT_ERROR : WEIGHT_ERROR);
        }
        return value.setScale(2);
    }

    /** Parses a real ISO date within the supported four-digit year range. */
    public static LocalDate parseDate(String value) {
        String trimmed = trimHorizontal(Objects.requireNonNull(value));
        if (!trimmed.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException(DATE_ERROR);
        }
        try {
            return validateDate(LocalDate.parse(trimmed));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(DATE_ERROR, exception);
        }
    }

    /** Validates the structural date range without reading the system clock. */
    public static LocalDate validateDate(LocalDate date) {
        Objects.requireNonNull(date);
        if (date.getYear() < 1 || date.getYear() > 9999) {
            throw new IllegalArgumentException(DATE_ERROR);
        }
        return date;
    }

    /** Checks dates against the execution or loading clock. */
    public static void requireNotFuture(LocalDate date, Clock clock) {
        validateDate(date);
        if (date.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException(DATE_ERROR);
        }
    }

    /** Trims horizontal whitespace only; line breaks remain invalid input. */
    public static String trimHorizontal(String value) {
        return value.replaceAll("^\\h+|\\h+$", "");
    }

    /** Returns whether every code point can appear in a single-line note. */
    public static boolean isPrintable(String value) {
        return value.codePoints().allMatch(codePoint -> !Character.isISOControl(codePoint)
                && Character.getType(codePoint) != Character.FORMAT
                && Character.getType(codePoint) != Character.SURROGATE
                && Character.getType(codePoint) != Character.UNASSIGNED
                && Character.getType(codePoint) != Character.LINE_SEPARATOR
                && Character.getType(codePoint) != Character.PARAGRAPH_SEPARATOR);
    }

    /** Validates raw notes before trimming, preserving case and internal spaces. */
    public static String normalizeNotes(String value) {
        Objects.requireNonNull(value);
        String trimmed = trimHorizontal(value);
        int length = trimmed.codePointCount(0, trimmed.length());
        if (!isPrintable(value) || length < 1 || length > 300) {
            throw new IllegalArgumentException(NOTES_ERROR);
        }
        return trimmed;
    }
}
