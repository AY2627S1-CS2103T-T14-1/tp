package seedu.address.model.person;

import java.util.Locale;
import java.util.Optional;

/** Weekdays in the order used by workout plans. */
public enum Weekday {
    MONDAY("Monday"), TUESDAY("Tuesday"), WEDNESDAY("Wednesday"), THURSDAY("Thursday"),
    FRIDAY("Friday"), SATURDAY("Saturday"), SUNDAY("Sunday");

    private final String displayName;

    Weekday(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Resolves a case-insensitive weekday prefix. A single {@code s} means Saturday. */
    public static Optional<Weekday> fromPrefix(String input) {
        String candidate = input.trim().toLowerCase(Locale.ROOT);
        for (Weekday day : values()) {
            int minimumLength = (day == TUESDAY || day == THURSDAY || day == SUNDAY) ? 2 : 1;
            if (candidate.length() >= minimumLength
                    && day.displayName.toLowerCase(Locale.ROOT).startsWith(candidate)) {
                return Optional.of(day);
            }
        }
        return Optional.empty();
    }

    /** Returns the weekday rank, placing missing or unrecognized days after Sunday. */
    public static int sortOrder(String day) {
        return fromPrefix(day).map(Enum::ordinal).orElse(values().length);
    }
}
