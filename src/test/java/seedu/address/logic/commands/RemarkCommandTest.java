package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests remark updates and index handling against the model.
 */
public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addReplaceAndClearRemark_preservesOtherFields() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        for (String value : new String[] {"Likes to swim.", "Prefers cycling.", ""}) {
            Person expectedPerson = new PersonBuilder(original).withRemark(value).build();
            CommandResult result = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(value)).execute(model);

            assertEquals(expectedPerson, model.getFilteredPersonList().get(0));
            String message = value.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                    : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
            assertEquals(String.format(message, Messages.format(expectedPerson)), result.getFeedbackToUser());
        }
    }

    @Test
    public void execute_filteredList_updatesDisplayedPersonAndShowsAll() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person displayedPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(displayedPerson).withRemark("Likes to swim.").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(displayedPerson, editedPerson);

        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, editedPerson.getRemark());
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(editedPerson));
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index invalidIndex = Index.fromZeroBased(model.getFilteredPersonList().size());
        RemarkCommand command = new RemarkCommand(invalidIndex, new Remark("Likes to swim."));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        RemarkCommand command = new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes to swim."));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
