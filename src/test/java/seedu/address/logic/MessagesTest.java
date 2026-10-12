package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_personWithEmptyPlan_showsEmpty() {
        Person person = new PersonBuilder().withWorkoutPlan(new WorkoutPlan()).build();
        String formatted = Messages.format(person);
        assertTrue(formatted.contains("; Workout Plan: Empty"));
    }

    @Test
    public void formatWorkoutPlan_emptyPlan_returnsEmpty() {
        assertEquals("Empty", Messages.formatWorkoutPlan(new WorkoutPlan()));
    }

    @Test
    public void formatWorkoutPlan_singleExerciseWithWholeWeight_formatsWithoutDecimal() {
        WorkoutPlan plan = new WorkoutPlan(List.of(new Exercise("Bench Press", 3, 10, 60.0, "Monday")));
        assertEquals("(1):\n**Monday**\n1. Bench Press 3x10 @60kg", Messages.formatWorkoutPlan(plan));
    }

    @Test
    public void formatWorkoutPlan_multipleExercisesWithFractionalWeight_joinsWithSeparator() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(
                new Exercise("Bench Press", 3, 10, 60.0),
                new Exercise("Deadlift", 4, 8, 62.5)));
        assertEquals("(2):\n**Day not specified**\n1. Bench Press 3x10 @60kg\n2. Deadlift 4x8 @62.5kg",
                Messages.formatWorkoutPlan(plan));
    }

    @Test
    public void formatWorkoutPlan_groupsExercisesByWeekdayOrder() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(
                new Exercise("Run", 1, 1, 0, "Sunday"),
                new Exercise("Press", 3, 10, 60, "Tuesday"),
                new Exercise("Squat", 4, 8, 80, "Monday"),
                new Exercise("Stretch", 1, 1, 0)));
        assertEquals("(4):\n**Monday**\n1. Squat 4x8 @80kg\n\n**Tuesday**\n2. Press 3x10 @60kg\n\n"
                        + "**Sunday**\n3. Run 1x1 @0kg\n\n**Day not specified**\n4. Stretch 1x1 @0kg",
                Messages.formatWorkoutPlan(plan));
    }

    @Test
    public void getExercisesInDisplayOrder_saturdayPrecedesSundayEvenWhenPlanIsReversed() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(
                new Exercise("Sunday exercise", 1, 1, 0, "Sunday"),
                new Exercise("Saturday exercise", 1, 1, 0, "Saturday")));

        List<Messages.IndexedExercise> ordered = Messages.getExercisesInDisplayOrder(plan);

        assertEquals("Saturday exercise", ordered.get(0).exercise().getName());
        assertEquals(1, ordered.get(0).index());
        assertEquals("Sunday exercise", ordered.get(1).exercise().getName());
        assertEquals(2, ordered.get(1).index());
    }

    @Test
    public void formatExercise_wholeWeight_omitsDecimalPoint() {
        assertEquals("Squat 4x8 @80kg", Messages.formatExercise(new Exercise("Squat", 4, 8, 80.0)));
    }

    @Test
    public void formatExercise_fractionalWeight_keepsDecimal() {
        assertEquals("Row 3x12 @42.5kg", Messages.formatExercise(new Exercise("Row", 3, 12, 42.5)));
    }

    @Test
    public void getExerciseDayHeading_returnsFullCanonicalWeekdayName() {
        assertEquals("Wednesday", Messages.getExerciseDayHeading(new Exercise("Press", 1, 1, 1, "Wednesday")));
        assertEquals("Saturday", Messages.getExerciseDayHeading(new Exercise("Squat", 1, 1, 1, "Saturday")));
        assertEquals("Saturday", Messages.getExerciseDayHeading(new Exercise("Squat", 1, 1, 1, "Sat")));
    }

    @Test
    public void format_personWithNonEmptyPlan_includesNumberedExercises() {
        Person person = new PersonBuilder().withWorkoutPlan(new WorkoutPlan(Arrays.asList(
                new Exercise("Bench Press", 3, 10, 60.0),
                new Exercise("Squat", 4, 8, 80.0)))).build();
        String formatted = Messages.format(person);
        assertTrue(formatted.contains("; Workout Plan: (2):\n**Day not specified**\n1. Bench Press 3x10 @60kg\n"
                + "2. Squat 4x8 @80kg"));
    }
}
