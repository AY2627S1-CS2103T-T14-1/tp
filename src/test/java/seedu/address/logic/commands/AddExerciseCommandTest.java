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

import java.util.Arrays;
import java.util.stream.IntStream;

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
 * {@code AddExerciseCommand}.
 */
public class AddExerciseCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static Exercise benchPress() {
        return new Exercise("Bench Press", 3, 10, 60.0);
    }

    private static Person personWithExercise(Person person, Exercise exercise) {
        return new PersonBuilder(person).withWorkoutPlan(new WorkoutPlan(Arrays.asList(exercise))).build();
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(personToEdit.getWorkoutPlan().isEmpty());

        Exercise exercise = benchPress();
        AddExerciseCommand addExerciseCommand = new AddExerciseCommand(INDEX_FIRST_PERSON, exercise);

        Person editedPerson = new PersonBuilder(personToEdit)
                .withWorkoutPlan(personToEdit.getWorkoutPlan().addExercise(exercise)).build();
        String expectedMessage = String.format(AddExerciseCommand.MESSAGE_ADD_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(addExerciseCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_addSecondExercise_appendsToExistingPlan() {
        Exercise existing = new Exercise("Squat", 4, 8, 80.0);
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withPlan = personWithExercise(original, existing);
        model.setPerson(original, withPlan);

        Exercise toAdd = benchPress();
        AddExerciseCommand addExerciseCommand = new AddExerciseCommand(INDEX_FIRST_PERSON, toAdd);

        Person editedPerson = new PersonBuilder(withPlan)
                .withWorkoutPlan(withPlan.getWorkoutPlan().addExercise(toAdd)).build();
        String expectedMessage = String.format(AddExerciseCommand.MESSAGE_ADD_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(withPlan, editedPerson);

        assertCommandSuccess(addExerciseCommand, model, expectedMessage, expectedModel);
        assertEquals(2, model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased()).getWorkoutPlan().size());
    }

    @Test
    public void execute_duplicateExercise_throwsCommandException() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withPlan = personWithExercise(original, benchPress());
        model.setPerson(original, withPlan);

        AddExerciseCommand addExerciseCommand =
                new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress());

        assertCommandFailure(addExerciseCommand, model, AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE);
    }

    @Test
    public void execute_duplicateExerciseDifferentCase_throwsCommandException() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withPlan = personWithExercise(original, benchPress());
        model.setPerson(original, withPlan);

        // hasExercise() is case-insensitive via Exercise#isSameExercise
        Exercise sameNameDifferentCase = new Exercise("bench press", 5, 5, 100.0);
        AddExerciseCommand addExerciseCommand =
                new AddExerciseCommand(INDEX_FIRST_PERSON, sameNameDifferentCase);

        assertCommandFailure(addExerciseCommand, model, AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE);
    }

    @Test
    public void execute_seventhExerciseOnDay_success() throws Exception {
        setExercisesOnMonday(6);
        new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress().withDay("Monday")).execute(model);
        assertEquals(7, model.getFilteredPersonList().get(0).getWorkoutPlan().size());
    }

    @Test
    public void execute_eighthExerciseOnDay_throwsCommandException() {
        setExercisesOnMonday(7);
        assertCommandFailure(new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress().withDay("Monday")),
                model, Messages.MESSAGE_DAILY_EXERCISE_LIMIT);
    }

    @Test
    public void execute_legacyDayAlreadyOverLimit_throwsCommandException() {
        setExercisesOnMonday(8);
        assertCommandFailure(new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress().withDay("Monday")),
                model, Messages.MESSAGE_DAILY_EXERCISE_LIMIT);
    }

    @Test
    public void execute_otherDayFull_success() throws Exception {
        setExercisesOnMonday(7);
        new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress().withDay("Tuesday")).execute(model);
        assertEquals(8, model.getFilteredPersonList().get(0).getWorkoutPlan().size());
    }

    @Test
    public void execute_otherClientDayFull_success() throws Exception {
        setExercisesOnMonday(7);
        new AddExerciseCommand(INDEX_SECOND_PERSON, benchPress().withDay("Monday")).execute(model);
        assertEquals(1, model.getFilteredPersonList().get(1).getWorkoutPlan().size());
        assertEquals(7, model.getFilteredPersonList().get(0).getWorkoutPlan().size());
    }

    private void setExercisesOnMonday(int count) {
        Person original = model.getFilteredPersonList().get(0);
        WorkoutPlan plan = new WorkoutPlan(IntStream.range(0, count)
                .mapToObj(index -> new Exercise("Exercise " + index, 1, 1, 0, "Mon")).toList());
        model.setPerson(original, new PersonBuilder(original).withWorkoutPlan(plan).build());
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        AddExerciseCommand addExerciseCommand = new AddExerciseCommand(outOfBoundIndex, benchPress());

        assertCommandFailure(addExerciseCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Exercise exercise = benchPress();
        AddExerciseCommand addExerciseCommand = new AddExerciseCommand(INDEX_FIRST_PERSON, exercise);

        Person editedPerson = new PersonBuilder(personToEdit)
                .withWorkoutPlan(personToEdit.getWorkoutPlan().addExercise(exercise)).build();
        String expectedMessage = String.format(AddExerciseCommand.MESSAGE_ADD_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()), editedPerson);
        // AddExerciseCommand resets the filter to show all persons
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        assertCommandSuccess(addExerciseCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidPersonIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // ensures outOfBoundIndex is still in bounds of address book list
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        AddExerciseCommand addExerciseCommand = new AddExerciseCommand(outOfBoundIndex, benchPress());

        assertCommandFailure(addExerciseCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        Exercise benchPress = benchPress();
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        AddExerciseCommand addFirstCommand = new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress);
        AddExerciseCommand addSecondCommand = new AddExerciseCommand(INDEX_SECOND_PERSON, benchPress);

        // same object -> returns true
        assertTrue(addFirstCommand.equals(addFirstCommand));

        // same values -> returns true
        AddExerciseCommand addFirstCommandCopy = new AddExerciseCommand(INDEX_FIRST_PERSON, benchPress);
        assertTrue(addFirstCommand.equals(addFirstCommandCopy));

        // different types -> returns false
        assertFalse(addFirstCommand.equals(1));

        // null -> returns false
        assertFalse(addFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(addFirstCommand.equals(addSecondCommand));

        // different exercise -> returns false
        assertFalse(addFirstCommand.equals(new AddExerciseCommand(INDEX_FIRST_PERSON, squat)));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        Exercise exercise = benchPress();
        AddExerciseCommand addExerciseCommand = new AddExerciseCommand(targetIndex, exercise);
        String expected = AddExerciseCommand.class.getCanonicalName()
                + "{index=" + targetIndex + ", exercise=" + exercise + "}";
        assertEquals(expected, addExerciseCommand.toString());
    }
}
