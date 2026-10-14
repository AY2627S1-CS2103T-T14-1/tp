package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

public class SetExerciseDayCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_setsDayOnExercise() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Exercise exercise = new Exercise("Bench Press", 3, 10, 60.0);
        Person withPlan = new PersonBuilder(original).withWorkoutPlan(new WorkoutPlan().addExercise(exercise)).build();
        model.setPerson(original, withPlan);

        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Monday").execute(model);

        Exercise tagged = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getWorkoutPlan().getExercises().get(0);
        assertEquals("Monday", tagged.getDay());
        assertEquals("Bench Press 3x10 @60kg", seedu.address.logic.Messages.formatExercise(tagged));
    }

    @Test
    public void execute_setExerciseDay_preservesGoal() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Exercise exercise = new Exercise("Bench Press", 3, 10, 60.0);
        Person withGoalAndPlan = new PersonBuilder(original).withGoal("Build strength")
                .withWorkoutPlan(new WorkoutPlan().addExercise(exercise)).build();
        model.setPerson(original, withGoalAndPlan);

        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Monday").execute(model);

        assertEquals("Build strength", model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getGoal().orElseThrow().value);
    }

    @Test
    public void execute_moveSameNameToOccupiedDay_throwsCommandException() {
        setRepeatedExerciseOnDifferentDays();
        assertCommandFailure(new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2), "MONDAY"),
                model, AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE);
        assertCommandFailure(new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3), "m"),
                model, AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE);
    }

    @Test
    public void execute_moveSameNameToFreeDay_success() throws Exception {
        setRepeatedExerciseOnDifferentDays();
        new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2), "Tuesday").execute(model);
        assertEquals(List.of("Mon", "Tuesday", ""), model.getFilteredPersonList().get(0)
                .getWorkoutPlan().getExercises().stream().map(Exercise::getDay).toList());
    }

    @Test
    public void execute_sameDayWithRepeatedNameOnOtherDays_success() throws Exception {
        setRepeatedExerciseOnDifferentDays();
        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Monday").execute(model);
        assertEquals(List.of("Monday", "Friday", ""), model.getFilteredPersonList().get(0)
                .getWorkoutPlan().getExercises().stream().map(Exercise::getDay).toList());
    }

    private void setRepeatedExerciseOnDifferentDays() {
        Person original = model.getFilteredPersonList().get(0);
        WorkoutPlan plan = new WorkoutPlan(List.of(new Exercise("Bench Press", 3, 10, 60, "Mon"),
                new Exercise("bench press", 5, 5, 100, "Friday"), new Exercise("Bench Press", 1, 1, 0)));
        model.setPerson(original, new PersonBuilder(original).withWorkoutPlan(plan).build());
    }

    @Test
    public void execute_invalidExerciseIndex_throwsCommandException() {
        assertCommandFailure(new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1), "Monday"),
                model, SetExerciseDayCommand.MESSAGE_INVALID_EXERCISE_INDEX);
    }

    @Test
    public void execute_assignSeventhExerciseToDay_success() throws Exception {
        setExercisesWithUnassignedExercise(6);
        new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(7), "Monday").execute(model);
        assertEquals(7, model.getFilteredPersonList().get(0).getWorkoutPlan().getExercises().stream()
                .filter(exercise -> exercise.getDay().equals("Monday")).count());
    }

    @Test
    public void execute_assignEighthExerciseToDay_throwsCommandException() {
        setExercisesWithUnassignedExercise(7);
        assertCommandFailure(new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(8), "Monday"),
                model, Messages.MESSAGE_DAILY_EXERCISE_LIMIT);
    }

    @Test
    public void execute_sameDayAtLimit_success() throws Exception {
        setExercisesWithUnassignedExercise(7);
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Monday").execute(model);
        assertEquals(expected, model);
    }

    @Test
    public void execute_moveAwayFromFullDay_success() throws Exception {
        setExercisesWithUnassignedExercise(7);
        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Tuesday").execute(model);
        List<Exercise> exercises = model.getFilteredPersonList().get(0).getWorkoutPlan().getExercises();
        assertEquals(6, exercises.stream().filter(exercise -> exercise.getDay().equals("Monday")).count());
        assertEquals("Tuesday", exercises.get(6).getDay());
    }

    private void setExercisesWithUnassignedExercise(int count) {
        Person original = model.getFilteredPersonList().get(0);
        List<Exercise> exercises = new ArrayList<>(IntStream.range(0, count)
                .mapToObj(index -> new Exercise("Exercise " + index, 1, 1, 0, "Monday")).toList());
        exercises.add(new Exercise("Stretch", 1, 1, 0));
        model.setPerson(original, new PersonBuilder(original).withWorkoutPlan(new WorkoutPlan(exercises)).build());
    }
}
