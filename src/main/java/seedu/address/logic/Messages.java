package seedu.address.logic;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.Weekday;
import seedu.address.model.person.WorkoutPlan;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     * Includes the workout plan so exercise mutations (add/remove/clear)
     * show the updated plan in the result box.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        builder.append("; Workout Plan: ");
        builder.append(formatWorkoutPlan(person.getWorkoutPlan()));
        return builder.toString();
    }

    /**
     * Formats a {@code WorkoutPlan} as {@code Empty} when it has no exercises,
     * otherwise as {@code (N): 1. Name SxR @Wkg; 2. ...} mirroring {@code PersonCard}.
     */
    public static String formatWorkoutPlan(WorkoutPlan plan) {
        if (plan.isEmpty()) {
            return "Empty";
        }
        StringBuilder builder = new StringBuilder().append("(").append(plan.size()).append("):\n");
        String currentGroup = null;
        for (IndexedExercise indexed : getExercisesInDisplayOrder(plan)) {
            String group = getExerciseDayHeading(indexed.exercise());
            if (!group.equals(currentGroup)) {
                if (currentGroup != null) {
                    builder.append('\n');
                }
                builder.append("**").append(group).append("**\n");
                currentGroup = group;
            }
            builder.append(indexed.index()).append(". ").append(formatExercise(indexed.exercise())).append('\n');
        }
        return builder.toString().stripTrailing();
    }

    /** Returns plan exercises in display order with sequential one-based indices. */
    public static List<IndexedExercise> getExercisesInDisplayOrder(WorkoutPlan plan) {
        List<Exercise> sorted = plan.getExercises().stream()
                .sorted(Comparator.comparingInt(Messages::daySortOrder))
                .toList();
        return java.util.stream.IntStream.range(0, sorted.size())
                .mapToObj(index -> new IndexedExercise(index + 1, sorted.get(index)))
                .toList();
    }

    private static int daySortOrder(Exercise exercise) {
        return Weekday.sortOrder(exercise.getDay());
    }

    /** Returns the heading for an exercise's day group. */
    public static String getExerciseDayHeading(Exercise exercise) {
        if (exercise.getDay().isEmpty()) {
            return "Day not specified";
        }
        return Weekday.fromPrefix(exercise.getDay()).map(Weekday::getDisplayName).orElse(exercise.getDay());
    }

    /**
     * Formats an exercise as {@code Name SetsxReps @Weightkg}, e.g. {@code Bench Press 3x10 @60kg}.
     */
    public static String formatExercise(Exercise exercise) {
        String weight = (exercise.getWeightKg() % 1 == 0)
                ? String.valueOf((int) exercise.getWeightKg())
                : String.valueOf(exercise.getWeightKg());
        String formatted = String.format("%s %dx%d @%skg",
                exercise.getName(), exercise.getSets(), exercise.getReps(), weight);
        return formatted;
    }

    /** An exercise paired with its original one-based index in the plan. */
    public record IndexedExercise(int index, Exercise exercise) { }

}
