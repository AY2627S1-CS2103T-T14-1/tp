package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * A client's current trainer-visible health considerations.
 * This is informational text and is not medical advice.
 */
public final class HealthProfile {
    public static final int MAX_LENGTH = 500;
    public static final String MESSAGE_CONSTRAINTS =
            "Health considerations must contain 1 to 500 printable characters and include at least one "
                    + "letter or digit.";

    public final String value;

    /** Creates a validated health profile from the supplied note. */
    public HealthProfile(String value) {
        requireNonNull(value);
        checkArgument(isValidHealthProfile(value), MESSAGE_CONSTRAINTS);
        this.value = value.strip();
    }

    /** Returns whether {@code value} is a valid health profile. */
    public static boolean isValidHealthProfile(String value) {
        requireNonNull(value);
        String trimmed = value.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        return length >= 1 && length <= MAX_LENGTH
                && value.codePoints().allMatch(HealthProfile::isPrintable)
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

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof HealthProfile profile && value.equals(profile.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
