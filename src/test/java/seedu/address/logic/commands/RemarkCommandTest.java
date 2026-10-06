package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for RemarkCommand.
 */
public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addAndRemoveRemark_preservesDietPlan() {
        Person original = model.getFilteredPersonList().get(0);
        Person planned = new PersonBuilder(original).withDietPlan(new DietPlan("1800 kcal", "Lean protein")).build();
        model.setPerson(original, planned);
        for (String text : new String[] {VALID_REMARK_AMY, ""}) {
            Person current = model.getFilteredPersonList().get(0);
            Person edited = createPersonWithRemark(current, new Remark(text));
            Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
            expectedModel.setPerson(current, edited);
            String message = text.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                    : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
            assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(text)), model,
                    String.format(message, Messages.format(edited)), expectedModel);
        }
    }

    @Test
    public void execute_addRemark_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = createPersonWithRemark(personToEdit, new Remark(VALID_REMARK_AMY));
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeRemark_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = createPersonWithRemark(personToEdit, new Remark(""));
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(editedPerson));

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_SECOND_PERSON, new Remark(VALID_REMARK_AMY));

        Model singlePersonModel = new ModelManager(new AddressBook(), new UserPrefs());
        singlePersonModel.addPerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));

        assertCommandFailure(remarkCommand, singlePersonModel, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand standardCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));

        // same values -> returns true
        RemarkCommand commandWithSameValues = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark(VALID_REMARK_AMY))));

        // different remark -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_BOB))));
    }

    private Person createPersonWithRemark(Person person, Remark remark) {
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(), remark,
                person.getTags(), person.getDietPlan());
    }
}
