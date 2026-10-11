package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Arrays;

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

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code ClearPlanCommand}.
 */
public class ClearPlanCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private Person personWithPlan() {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        return new PersonBuilder(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()))
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress, squat)))
                .build();
    }

    @Test
    public void execute_validIndexWithExercises_clearsPlan() {
        Person withPlan = personWithPlan();
        model.setPerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()), withPlan);

        ClearPlanCommand clearPlanCommand = new ClearPlanCommand(INDEX_FIRST_PERSON);

        Person expectedPerson = new PersonBuilder(withPlan).withWorkoutPlan(new WorkoutPlan()).build();
        String expectedMessage = String.format(ClearPlanCommand.MESSAGE_CLEAR_PLAN_SUCCESS,
                Messages.format(expectedPerson));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(withPlan, expectedPerson);

        assertCommandSuccess(clearPlanCommand, model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getWorkoutPlan().isEmpty());
    }

    @Test
    public void execute_alreadyEmptyPlan_returnsAlreadyEmptyMessage() {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(person.getWorkoutPlan().isEmpty());

        ClearPlanCommand clearPlanCommand = new ClearPlanCommand(INDEX_FIRST_PERSON);
        String expectedMessage = String.format(ClearPlanCommand.MESSAGE_PLAN_ALREADY_EMPTY,
                Messages.format(person));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(clearPlanCommand, model, expectedMessage, expectedModel);
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ClearPlanCommand clearPlanCommand = new ClearPlanCommand(outOfBoundIndex);
        assertCommandFailure(clearPlanCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ClearPlanCommand clearFirstCommand = new ClearPlanCommand(INDEX_FIRST_PERSON);
        ClearPlanCommand clearSecondCommand = new ClearPlanCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(clearFirstCommand.equals(clearFirstCommand));

        // same values -> returns true
        ClearPlanCommand clearFirstCommandCopy = new ClearPlanCommand(INDEX_FIRST_PERSON);
        assertTrue(clearFirstCommand.equals(clearFirstCommandCopy));

        // different types -> returns false
        assertFalse(clearFirstCommand.equals(1));

        // null -> returns false
        assertFalse(clearFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(clearFirstCommand.equals(clearSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        ClearPlanCommand clearPlanCommand = new ClearPlanCommand(targetIndex);
        String expected = ClearPlanCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, clearPlanCommand.toString());
    }
}
