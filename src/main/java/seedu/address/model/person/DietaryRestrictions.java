package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** An immutable, ordered list of a client's current dietary restrictions. */
public final class DietaryRestrictions {
    public static final int MAX_ENTRIES = 10;
    public static final int MAX_ENTRY_LENGTH = 100;
    public static final String MESSAGE_CONSTRAINTS =
            "Each dietary restriction must contain 1 to 100 printable characters and include at least one "
                    + "letter or digit. At most 10 restrictions are allowed, without duplicates.";

    private final List<String> values;

    /** Creates an empty restrictions list. */
    public DietaryRestrictions() {
        values = List.of();
    }

    /** Creates a validated ordered restrictions list. */
    public DietaryRestrictions(List<String> values) {
        requireNonNull(values);
        checkArgument(!values.isEmpty() && values.size() <= MAX_ENTRIES, MESSAGE_CONSTRAINTS);
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            requireNonNull(value);
            checkArgument(isValidRestriction(value), MESSAGE_CONSTRAINTS);
            String trimmed = value.strip();
            checkArgument(seen.add(foldCase(trimmed)), MESSAGE_CONSTRAINTS);
            normalized.add(trimmed);
        }
        this.values = List.copyOf(normalized);
    }

    /**
     * Returns {@code value} folded for case-insensitive comparison. Lower-casing alone turns a capital sigma into
     * {@code ς} or {@code σ} depending on its position, so Greek final sigma is normalised to {@code σ} explicitly.
     */
    public static String foldCase(String value) {
        requireNonNull(value);
        return value.toUpperCase(Locale.ROOT).toLowerCase(Locale.ROOT).replace('\u03C2', '\u03C3');
    }

    /** Returns whether one restriction value is valid. */
    public static boolean isValidRestriction(String value) {
        requireNonNull(value);
        String trimmed = value.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        return length >= 1 && length <= MAX_ENTRY_LENGTH
                && value.codePoints().allMatch(DietaryRestrictions::isPrintable)
                && trimmed.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    private static boolean isPrintable(int codePoint) {
        int type = Character.getType(codePoint);
        return !Character.isISOControl(codePoint)
                && type != Character.FORMAT
                && type != Character.LINE_SEPARATOR
                && type != Character.PARAGRAPH_SEPARATOR
                && type != Character.SURROGATE
                && type != Character.PRIVATE_USE
                && type != Character.UNASSIGNED;
    }

    /** Returns the current restrictions in recorded order. */
    public List<String> getValues() {
        return values;
    }

    /** Returns whether no restrictions are recorded. */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DietaryRestrictions restrictions
                && values.equals(restrictions.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
