package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.GEORGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientMatchesAnyPredicate;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.GoalContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneMatchesPredicate;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noPersonFound() {
        String expectedMessage = FindCommand.MESSAGE_FOUND_NONE;
        ClientMatchesAnyPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = String.format(FindCommand.MESSAGE_FOUND, 3);
        ClientMatchesAnyPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_phoneAndEmailCriteria_filtersMatchingClients() {
        ClientMatchesAnyPredicate phonePredicate = new ClientMatchesAnyPredicate(
                List.of(new PhoneMatchesPredicate("948 2224")));
        FindCommand phoneCommand = new FindCommand(phonePredicate);
        expectedModel.updateFilteredPersonList(phonePredicate);
        assertCommandSuccess(phoneCommand, model, String.format(FindCommand.MESSAGE_FOUND, 1), expectedModel);
        assertEquals(List.of(ELLE), model.getFilteredPersonList());

        ClientMatchesAnyPredicate hyphenatedPhonePredicate = new ClientMatchesAnyPredicate(
                List.of(new PhoneMatchesPredicate("948-2224")));
        FindCommand hyphenatedPhoneCommand = new FindCommand(hyphenatedPhonePredicate);
        expectedModel.updateFilteredPersonList(hyphenatedPhonePredicate);
        assertCommandSuccess(hyphenatedPhoneCommand, model, String.format(FindCommand.MESSAGE_FOUND, 1), expectedModel);
        assertEquals(List.of(ELLE), model.getFilteredPersonList());

        ClientMatchesAnyPredicate emailPredicate = new ClientMatchesAnyPredicate(
                List.of(new EmailContainsKeywordsPredicate(List.of("HEINZ"))));
        FindCommand emailCommand = new FindCommand(emailPredicate);
        expectedModel.updateFilteredPersonList(emailPredicate);
        assertCommandSuccess(emailCommand, model, String.format(FindCommand.MESSAGE_FOUND, 1), expectedModel);
        assertEquals(List.of(CARL), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameAndPhoneCriteria_returnsUnionOfMatches() {
        ClientMatchesAnyPredicate predicate = new ClientMatchesAnyPredicate(List.of(
                new NameContainsKeywordsPredicate(List.of("Carl")), new PhoneMatchesPredicate("9482")));
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(command, model, String.format(FindCommand.MESSAGE_FOUND, 4), expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA, GEORGE), model.getFilteredPersonList());
    }

    @Test
    public void execute_goalCriteria_filtersMatchingClientsAndCombinesWithName() {
        Person strengthClient = new PersonBuilder().withName("Priya Nair").withPhone("91234567")
                .withGoal("Build strength").build();
        Person fatLossClient = new PersonBuilder().withName("Marcus Tan").withPhone("98765432")
                .withGoal("Fat loss").build();
        Person clientWithoutGoal = new PersonBuilder().withName("Lim Wei Ling").withPhone("90000000").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(strengthClient);
        addressBook.addPerson(fatLossClient);
        addressBook.addPerson(clientWithoutGoal);
        Model goalModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedGoalModel = new ModelManager(addressBook, new UserPrefs());

        ClientMatchesAnyPredicate goalPredicate = new ClientMatchesAnyPredicate(
                List.of(new GoalContainsKeywordsPredicate(List.of("STRENGTH"))));
        FindCommand goalCommand = new FindCommand(goalPredicate);
        expectedGoalModel.updateFilteredPersonList(goalPredicate);
        assertCommandSuccess(goalCommand, goalModel, String.format(FindCommand.MESSAGE_FOUND, 1), expectedGoalModel);
        assertEquals(List.of(strengthClient), goalModel.getFilteredPersonList());

        ClientMatchesAnyPredicate unionPredicate = new ClientMatchesAnyPredicate(List.of(
                new NameContainsKeywordsPredicate(List.of("Marcus")),
                new GoalContainsKeywordsPredicate(List.of("strength"))));
        FindCommand unionCommand = new FindCommand(unionPredicate);
        expectedGoalModel.updateFilteredPersonList(unionPredicate);
        assertCommandSuccess(unionCommand, goalModel, String.format(FindCommand.MESSAGE_FOUND, 2), expectedGoalModel);
        assertEquals(List.of(strengthClient, fatLossClient), goalModel.getFilteredPersonList());
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private ClientMatchesAnyPredicate preparePredicate(String userInput) {
        return new ClientMatchesAnyPredicate(List.of(
                new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")))));
    }
}
