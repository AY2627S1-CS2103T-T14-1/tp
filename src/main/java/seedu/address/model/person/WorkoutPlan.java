package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Person's workout plan in the address book.
 * Guarantees: immutable; exercises list is defensively copied and unmodifiable.
 */
public class WorkoutPlan {

    public static final int MAX_EXERCISES_PER_DAY = 7;

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
        this.exercises.forEach(Objects::requireNonNull);
        this.exercises.sort(Comparator.comparingInt(WorkoutPlan::daySortOrder));
    }

    /**
     * Returns an unmodifiable view of the exercises list, which throws
     * {@code UnsupportedOperationException} if modification is attempted.
     */
    public List<Exercise> getExercises() {
        return Collections.unmodifiableList(exercises);
    }

    /**
     * Returns true if the plan contains an exercise with the same name
     * (case-insensitive) on the same day as the given exercise. Weekday prefixes
     * are treated alike, and unassigned exercises share the same day group.
     */
    public boolean hasExercise(Exercise exercise) {
        requireNonNull(exercise);
        return exercises.stream().anyMatch(existing -> exercise.isSameExercise(existing)
                && normalizedDay(exercise).equalsIgnoreCase(normalizedDay(existing)));
    }

    /**
     * Returns whether the given weekday has at most {@link #MAX_EXERCISES_PER_DAY} exercises.
     * Weekday prefixes and letter case are treated alike. Unassigned exercises have no daily limit.
     */
    public boolean isWithinDailyLimit(String day) {
        requireNonNull(day);
        return Weekday.fromPrefix(day).map(weekday -> exercises.stream()
                .filter(exercise -> Weekday.fromPrefix(exercise.getDay()).orElse(null) == weekday)
                .count() <= MAX_EXERCISES_PER_DAY).orElse(true);
    }

    /**
     * Returns a new {@code WorkoutPlan} with {@code exercise} added.
     */
    public WorkoutPlan addExercise(Exercise exercise) {
        requireNonNull(exercise);
        List<Exercise> updated = new ArrayList<>(exercises);
        updated.add(exercise);
        updated.sort(Comparator.comparingInt(WorkoutPlan::daySortOrder));
        return new WorkoutPlan(updated);
    }

    /**
     * Returns a new {@code WorkoutPlan} with {@code exercise} removed.
     */
    public WorkoutPlan removeExercise(Exercise exercise) {
        requireNonNull(exercise);
        List<Exercise> updated = new ArrayList<>(exercises);
        updated.remove(exercise);
        updated.sort(Comparator.comparingInt(WorkoutPlan::daySortOrder));
        return new WorkoutPlan(updated);
    }

    /** Returns a new plan with the exercise at the zero-based index assigned to {@code day}. */
    public WorkoutPlan setExerciseDay(int index, String day) {
        requireNonNull(day);
        List<Exercise> updated = new ArrayList<>(exercises);
        updated.set(index, updated.get(index).withDay(day));
        updated.sort(Comparator.comparingInt(WorkoutPlan::daySortOrder));
        return new WorkoutPlan(updated);
    }

    private static String normalizedDay(Exercise exercise) {
        return Weekday.fromPrefix(exercise.getDay()).map(Weekday::getDisplayName).orElse(exercise.getDay());
    }

    private static int daySortOrder(Exercise exercise) {
        return Weekday.sortOrder(exercise.getDay());
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
