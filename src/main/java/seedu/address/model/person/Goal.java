package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a fitness goal.
 * Guarantees: immutable; is valid as declared in {@link #isValidGoal(String)}.
 */
public class Goal {

    public static final String MESSAGE_CONSTRAINTS =
            "Goal must contain 1 to 50 characters, including at least one letter or digit.";
    public static final int MAX_LENGTH = 50;

    public final String value;

    /**
     * Constructs a {@code Goal}.
     *
     * @param goal A valid fitness goal.
     */
    public Goal(String goal) {
        requireNonNull(goal);
        checkArgument(isValidGoal(goal), MESSAGE_CONSTRAINTS);
        value = goal.strip();
    }

    /**
     * Returns true if a given string is a valid goal.
     */
    public static boolean isValidGoal(String test) {
        String strippedTest = test.strip();
        return strippedTest.length() >= 1
                && strippedTest.length() <= MAX_LENGTH
                && strippedTest.matches("[^\\p{Cntrl}]+")
                && strippedTest.matches(".*[\\p{L}\\p{N}].*");
    }

    /**
     * Returns the goal value.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Compares this goal with another object for equality.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Goal otherGoal)) {
            return false;
        }

        return value.equals(otherGoal.value);
    }

    /**
     * Returns the hash code of this goal.
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
