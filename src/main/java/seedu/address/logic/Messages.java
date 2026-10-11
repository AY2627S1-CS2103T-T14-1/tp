package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
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
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(plan.size()).append("): ");
        boolean isFirst = true;
        int index = 1;
        for (Exercise exercise : plan.getExercises()) {
            if (!isFirst) {
                builder.append("; ");
            }
            builder.append(index).append(". ").append(formatExercise(exercise));
            isFirst = false;
            index++;
        }
        return builder.toString();
    }

    /**
     * Formats an exercise as {@code Name SetsxReps @Weightkg}, e.g. {@code Bench Press 3x10 @60kg}.
     */
    public static String formatExercise(Exercise exercise) {
        String weight = (exercise.getWeightKg() % 1 == 0)
                ? String.valueOf((int) exercise.getWeightKg())
                : String.valueOf(exercise.getWeightKg());
        return String.format("%s %dx%d @%skg",
                exercise.getName(), exercise.getSets(), exercise.getReps(), weight);
    }

}
