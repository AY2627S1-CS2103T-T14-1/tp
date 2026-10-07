package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents one exercise performed during a completed workout session.
 * Guarantees: immutable; all fields are non-null and valid.
 */
public final class WorkoutLogEntry {

    public static final String MESSAGE_CONSTRAINTS_NAME =
            "Exercise names must be 1 to 50 characters long and may contain only "
                    + "letters, digits, spaces, hyphens, apostrophes, and parentheses.";
    public static final String MESSAGE_CONSTRAINTS_WEIGHT =
            "Exercise weight must be between 0 and 1000 kg and may have up to 2 decimal places.";
    public static final String MESSAGE_CONSTRAINTS_SETS =
            "Sets must be a whole number from 1 to 100.";
    public static final String MESSAGE_CONSTRAINTS_REPS =
            "Repetitions must be a whole number from 1 to 1000.";

    private static final String NAME_VALIDATION_REGEX = "[\\p{L}\\p{N} '\\-()]+";
    private static final BigDecimal MAX_WEIGHT_KG = new BigDecimal("1000");

    private final String name;
    private final BigDecimal weightKg;
    private final int sets;
    private final int reps;

    /**
     * Constructs a validated {@code WorkoutLogEntry}.
     *
     * @param name A valid exercise name.
     * @param weightKg A valid weight in kilograms.
     * @param sets A valid number of sets.
     * @param reps A valid number of repetitions.
     */
    public WorkoutLogEntry(String name, BigDecimal weightKg, int sets, int reps) {
        requireNonNull(name);
        requireNonNull(weightKg);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS_NAME);
        checkArgument(isValidWeight(weightKg), MESSAGE_CONSTRAINTS_WEIGHT);
        checkArgument(isValidSets(sets), MESSAGE_CONSTRAINTS_SETS);
        checkArgument(isValidReps(reps), MESSAGE_CONSTRAINTS_REPS);

        this.name = name;
        this.weightKg = weightKg.stripTrailingZeros();
        this.sets = sets;
        this.reps = reps;
    }

    /**
     * Returns true if the given exercise name satisfies the name constraints.
     */
    public static boolean isValidName(String name) {
        requireNonNull(name);
        int length = name.codePointCount(0, name.length());
        return length >= 1
                && length <= 50
                && name.matches(NAME_VALIDATION_REGEX)
                && name.codePoints().anyMatch(Character::isLetter);
    }

    /**
     * Returns true if the weight is within range and has at most two decimal places.
     */
    public static boolean isValidWeight(BigDecimal weightKg) {
        requireNonNull(weightKg);
        return weightKg.compareTo(BigDecimal.ZERO) >= 0
                && weightKg.compareTo(MAX_WEIGHT_KG) <= 0
                && weightKg.scale() <= 2;
    }

    /**
     * Returns true if the given number of sets is between 1 and 100.
     */
    public static boolean isValidSets(int sets) {
        return sets >= 1 && sets <= 100;
    }

    /**
     * Returns true if the given number of repetitions is between 1 and 1000.
     */
    public static boolean isValidReps(int reps) {
        return reps >= 1 && reps <= 1000;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof WorkoutLogEntry otherEntry)) {
            return false;
        }

        return name.equals(otherEntry.name)
                && weightKg.equals(otherEntry.weightKg)
                && sets == otherEntry.sets
                && reps == otherEntry.reps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weightKg, sets, reps);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("weightKg", weightKg.toPlainString())
                .add("sets", sets)
                .add("reps", reps)
                .toString();
    }
}
