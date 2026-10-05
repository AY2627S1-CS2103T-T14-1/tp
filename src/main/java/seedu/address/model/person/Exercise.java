package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents an Exercise in the address book.
 * Guarantees: immutable; fields are valid as declared in
 * {@link #isValidName(String)}, {@link #isValidSets(int)},
 * {@link #isValidReps(int)} and {@link #isValidWeight(double)}.
 */
public class Exercise {

    public static final String MESSAGE_CONSTRAINTS =
            "Exercise name should not be blank, sets and reps should be positive integers "
                    + "greater than 0, and weight should be a non-negative number in kg";
    public static final String MESSAGE_CONSTRAINTS_NAME = "Exercise name should not be blank";
    public static final String MESSAGE_CONSTRAINTS_SETS = "Sets should be a positive integer greater than 0";
    public static final String MESSAGE_CONSTRAINTS_REPS = "Reps should be a positive integer greater than 0";
    public static final String MESSAGE_CONSTRAINTS_WEIGHT =
            "Weight should be a non-negative number in kg";

    public final String name;
    public final int sets;
    public final int reps;
    public final double weightKg;

    /**
     * Constructs an {@code Exercise}.
     *
     * @param name A valid exercise name.
     * @param sets A valid sets count.
     * @param reps A valid reps count.
     * @param weightKg A valid weight in kg.
     */
    public Exercise(String name, int sets, int reps, double weightKg) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS_NAME);
        checkArgument(isValidSets(sets), MESSAGE_CONSTRAINTS_SETS);
        checkArgument(isValidReps(reps), MESSAGE_CONSTRAINTS_REPS);
        checkArgument(isValidWeight(weightKg), MESSAGE_CONSTRAINTS_WEIGHT);
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    /**
     * Returns true if a given string is a valid exercise name.
     */
    public static boolean isValidName(String test) {
        return !test.trim().isEmpty();
    }

    /**
     * Returns true if a given int is a valid sets count.
     */
    public static boolean isValidSets(int test) {
        return test > 0;
    }

    /**
     * Returns true if a given int is a valid reps count.
     */
    public static boolean isValidReps(int test) {
        return test > 0;
    }

    /**
     * Returns true if a given double is a valid weight in kg.
     */
    public static boolean isValidWeight(double test) {
        return test >= 0;
    }

    public String getName() {
        return name;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public double getWeightKg() {
        return weightKg;
    }

    /**
     * Returns true if both exercises have the same name.
     * This defines a weaker notion of equality between two exercises.
     */
    public boolean isSameExercise(Exercise otherExercise) {
        if (otherExercise == this) {
            return true;
        }

        return otherExercise != null
                && otherExercise.getName().equalsIgnoreCase(getName());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Exercise otherExercise)) {
            return false;
        }

        return name.equals(otherExercise.name)
                && sets == otherExercise.sets
                && reps == otherExercise.reps
                && Double.compare(weightKg, otherExercise.weightKg) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sets, reps, weightKg);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("sets", sets)
                .add("reps", reps)
                .add("weightKg", weightKg)
                .toString();
    }
}
