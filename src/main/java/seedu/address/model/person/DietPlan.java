package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * An immutable diet plan with a required nutrition target and optional coaching notes.
 * Text is trimmed while internal spacing and display case are preserved.
 */
public final class DietPlan {
    public static final String MESSAGE_CONSTRAINTS_TARGET =
            "Diet target must contain 1 to 200 printable characters.";
    public static final String MESSAGE_CONSTRAINTS_NOTES =
            "Diet-plan notes must contain 1 to 300 printable characters.";

    private final String target;
    private final Optional<String> notes;

    /**
     * Creates a plan without notes.
     */
    public DietPlan(String target) {
        this(target, Optional.empty());
    }

    /**
     * Creates a plan with supplied, nonblank notes.
     */
    public DietPlan(String target, String notes) {
        this(target, Optional.of(notes));
    }

    private DietPlan(String target, Optional<String> notes) {
        requireNonNull(target);
        checkArgument(isValidTarget(target), MESSAGE_CONSTRAINTS_TARGET);
        notes.ifPresent(value -> checkArgument(isValidNotes(value), MESSAGE_CONSTRAINTS_NOTES));
        this.target = target.strip();
        this.notes = notes.map(String::strip);
    }

    /**
     * Returns whether the target is printable, has 1-200 characters after trimming,
     * and contains at least one letter or digit.
     */
    public static boolean isValidTarget(String target) {
        requireNonNull(target);
        return isValidText(target, 200) && target.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    /**
     * Returns whether supplied notes contain 1-300 printable characters after trimming.
     */
    public static boolean isValidNotes(String notes) {
        requireNonNull(notes);
        return isValidText(notes, 300);
    }

    private static boolean isValidText(String text, int maximumLength) {
        String trimmed = text.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        return length >= 1 && length <= maximumLength && text.codePoints().allMatch(DietPlan::isPrintable);
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

    public String getTarget() {
        return target;
    }

    public Optional<String> getNotes() {
        return notes;
    }

    /**
     * Returns whether targets and optional notes match ignoring case, for duplicate-plan detection.
     * Value equality remains case-sensitive to preserve the stored display text.
     */
    public boolean isSameDietPlan(DietPlan other) {
        return other != null && target.equalsIgnoreCase(other.target)
                && notes.map(value -> other.notes.map(value::equalsIgnoreCase).orElse(false))
                        .orElse(other.notes.isEmpty());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof DietPlan otherPlan
                && target.equals(otherPlan.target) && notes.equals(otherPlan.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, notes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("target", target).add("notes", notes).toString();
    }
}
