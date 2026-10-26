package seedu.address.model.membership;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a membership plan's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidMembershipName(String)}.
 * Leading/trailing whitespace is trimmed and internal whitespace is collapsed on construction;
 * the stored value preserves the entered case. The catalog and client-card views render the name
 * with its first character upper-cased (see {@link #toDisplayName(String)}), so a name stored as
 * {@code gold} reads as {@code Gold}. Equality is case-insensitive using locale-independent folding,
 * so {@code Gold} and {@code gold} denote the same plan.
 */
public class MembershipName {

    public static final String MESSAGE_CONSTRAINTS =
            "Membership plan name must contain 1 to 50 characters and may contain only ASCII letters, digits, "
                    + "spaces, apostrophes, hyphens, and full stops.";

    public static final int MAX_LENGTH = 50;

    /*
     * Letters, digits, spaces, apostrophes, hyphens and full stops, with at least one letter or digit.
     * Length is checked separately so the regex stays readable.
     */
    public static final String VALIDATION_REGEX = "(?=.*[\\p{Alnum}])[\\p{Alnum} .'\\-]*";

    public final String value;

    /**
     * Constructs a {@code MembershipName}.
     *
     * @param name A valid membership plan name.
     */
    public MembershipName(String name) {
        requireNonNull(name);
        String normalizedName = normalize(name);
        checkArgument(isValidMembershipName(normalizedName), MESSAGE_CONSTRAINTS);
        value = normalizedName;
    }

    /**
     * Normalizes a raw name by trimming leading/trailing whitespace and collapsing
     * consecutive internal whitespace to a single space.
     */
    public static String normalize(String name) {
        requireNonNull(name);
        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns the stable canonical key used for uniqueness checks and plan references.
     * Uses locale-independent case folding so comparison is deterministic across locales.
     */
    public String getKey() {
        return value.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns {@code name} with its first character upper-cased for display,
     * leaving the remaining characters unchanged. Used by the plan-list and client-card
     * views so a lower-case entry such as {@code gold} reads as {@code Gold}.
     */
    public static String toDisplayName(String name) {
        requireNonNull(name);
        if (name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Returns true if a given string is a valid membership plan name after normalization.
     */
    public static boolean isValidMembershipName(String test) {
        requireNonNull(test);
        String normalized = normalize(test);
        return !normalized.isEmpty() && normalized.length() <= MAX_LENGTH
                && normalized.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof MembershipName otherName)) {
            return false;
        }

        return getKey().equals(otherName.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
