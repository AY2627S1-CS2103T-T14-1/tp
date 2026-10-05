package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Person's workout plan in the address book.
 * Guarantees: immutable; exercises list is defensively copied and unmodifiable.
 */
public class WorkoutPlan {

    private final List<Exercise> exercises;

    /**
     * Constructs an empty {@code WorkoutPlan}.
     */
    public WorkoutPlan() {
        this.exercises = new ArrayList<>();
    }

    /**
     * Constructs a {@code WorkoutPlan} with the given exercises.
     * A defensive copy is made internally.
     */
    public WorkoutPlan(List<Exercise> exercises) {
        requireNonNull(exercises);
        this.exercises = new ArrayList<>(exercises);
    }

    /**
     * Returns an unmodifiable view of the exercises list, which throws
     * {@code UnsupportedOperationException} if modification is attempted.
     */
    public List<Exercise> getExercises() {
        return Collections.unmodifiableList(exercises);
    }

    /**
     * Returns a new {@code WorkoutPlan} with {@code exercise} added.
     */
    public WorkoutPlan addExercise(Exercise exercise) {
        requireNonNull(exercise);
        List<Exercise> updated = new ArrayList<>(exercises);
        updated.add(exercise);
        return new WorkoutPlan(updated);
    }

    /**
     * Returns a new {@code WorkoutPlan} with {@code exercise} removed.
     */
    public WorkoutPlan removeExercise(Exercise exercise) {
        requireNonNull(exercise);
        List<Exercise> updated = new ArrayList<>(exercises);
        updated.remove(exercise);
        return new WorkoutPlan(updated);
    }

    /**
     * Returns true if the plan contains no exercises.
     */
    public boolean isEmpty() {
        return exercises.isEmpty();
    }

    /**
     * Returns the number of exercises in the plan.
     */
    public int size() {
        return exercises.size();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof WorkoutPlan otherPlan)) {
            return false;
        }

        return exercises.equals(otherPlan.exercises);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exercises);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("exercises", exercises)
                .toString();
    }
}
