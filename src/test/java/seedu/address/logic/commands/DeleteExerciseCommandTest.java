package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteExerciseCommand}.
 */
public class DeleteExerciseCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private Person personWithPlan(Person person, Exercise... exercises) {
        return new PersonBuilder(person).withWorkoutPlan(new WorkoutPlan(Arrays.asList(exercises))).build();
    }

    private void setFirstPersonPlan(Exercise... exercises) {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(original, personWithPlan(original, exercises));
    }

    @Test
    public void execute_validIndicesUnfilteredList_success() {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        setFirstPersonPlan(benchPress, squat);

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);

        List<Exercise> remaining = new ArrayList<>(Arrays.asList(squat));
        Person editedPerson = new PersonBuilder(personToEdit)
                .withWorkoutPlan(new WorkoutPlan(remaining)).build();
        String expectedMessage = String.format(DeleteExerciseCommand.MESSAGE_DELETE_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(deleteExerciseCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_deleteLastExercise_emptiesPlan() {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        setFirstPersonPlan(benchPress);

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);

        Person editedPerson = new PersonBuilder(personToEdit)
                .withWorkoutPlan(new WorkoutPlan()).build();
        String expectedMessage = String.format(DeleteExerciseCommand.MESSAGE_DELETE_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(deleteExerciseCommand, model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased()).getWorkoutPlan().isEmpty());
    }

    @Test
    public void execute_deleteExercise_preservesGoal() throws Exception {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withGoalAndPlan = new PersonBuilder(original).withGoal("Build strength")
                .withWorkoutPlan(new WorkoutPlan(List.of(benchPress))).build();
        model.setPerson(original, withGoalAndPlan);

        new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(model);

        assertEquals("Build strength", model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getGoal().orElseThrow().value);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_throwsCommandException() {
        setFirstPersonPlan(new Exercise("Bench Press", 3, 10, 60.0));

        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(outOfBoundIndex, INDEX_FIRST_PERSON);

        assertCommandFailure(deleteExerciseCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidExerciseIndexUnfilteredList_throwsCommandException() {
        // empty plan
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(person.getWorkoutPlan().isEmpty());

        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);

        assertCommandFailure(deleteExerciseCommand, model,
                DeleteExerciseCommand.MESSAGE_INVALID_EXERCISE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_legacyAbbreviatedDay_deletesExerciseAtDisplayedIndex() throws Exception {
        Exercise saturday = new Exercise("Squat", 4, 8, 80.0, "Sat");
        Exercise sunday = new Exercise("Run", 1, 1, 0.0, "Sunday");
        setFirstPersonPlan(sunday, saturday);
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals("Squat", Messages.getExercisesInDisplayOrder(person.getWorkoutPlan()).get(0)
                .exercise().getName());

        new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(model);

        List<Exercise> remaining = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getWorkoutPlan().getExercises();
        assertEquals(List.of(sunday), remaining);
    }

    @Test
    public void execute_exerciseIndexOutOfBounds_throwsCommandException() {
        setFirstPersonPlan(new Exercise("Bench Press", 3, 10, 60.0));

        // only 1 exercise, index 2 is out of bounds but person index is valid
        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);

        assertCommandFailure(deleteExerciseCommand, model,
                DeleteExerciseCommand.MESSAGE_INVALID_EXERCISE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndicesFilteredList_success() {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        setFirstPersonPlan(benchPress, squat);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);

        Person editedPerson = new PersonBuilder(personToEdit)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress))).build();
        String expectedMessage = String.format(DeleteExerciseCommand.MESSAGE_DELETE_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()), editedPerson);
        // DeleteExerciseCommand resets the filter to show all persons
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        assertCommandSuccess(deleteExerciseCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidPersonIndexFilteredList_throwsCommandException() {
        setFirstPersonPlan(new Exercise("Bench Press", 3, 10, 60.0));
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // ensures outOfBoundIndex is still in bounds of address book list
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(outOfBoundIndex, INDEX_FIRST_PERSON);

        assertCommandFailure(deleteExerciseCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidExerciseIndexFilteredList_throwsCommandException() {
        setFirstPersonPlan(new Exercise("Bench Press", 3, 10, 60.0));
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        DeleteExerciseCommand deleteExerciseCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);

        assertCommandFailure(deleteExerciseCommand, model,
                DeleteExerciseCommand.MESSAGE_INVALID_EXERCISE_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteExerciseCommand deleteFirstCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);
        DeleteExerciseCommand deleteSecondCommand =
                new DeleteExerciseCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteExerciseCommand deleteFirstCommandCopy =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person index -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));

        // different exercise index -> returns false
        assertFalse(deleteFirstCommand.equals(
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));
    }

    @Test
    public void toStringMethod() {
        Index personIndex = Index.fromOneBased(1);
        Index exerciseIndex = Index.fromOneBased(2);
        DeleteExerciseCommand deleteExerciseCommand = new DeleteExerciseCommand(personIndex, exerciseIndex);
        String expected = DeleteExerciseCommand.class.getCanonicalName()
                + "{personIndex=" + personIndex + ", exerciseIndex=" + exerciseIndex + "}";
        assertEquals(expected, deleteExerciseCommand.toString());
    }
}
