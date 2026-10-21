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

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;

public class PayCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validPayment_success() {
        Person target = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        PaymentRecord record = payment("150", "2026-10-20", null);
        Person updated = target.withAddedPayment(record);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        String expectedMessage = String.format(PayCommand.MESSAGE_SUCCESS, target.getName(),
                "150.00", "2026-10-20");

        assertCommandSuccess(new PayCommand(INDEX_FIRST_PERSON, record), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_optionalRemarks_success() {
        Person target = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        PaymentRecord record = payment("75.5", "2026-10-19", "October membership payment");
        Person updated = target.withAddedPayment(record);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        String expectedMessage = String.format(PayCommand.MESSAGE_SUCCESS, target.getName(),
                "75.50", "2026-10-19");

        assertCommandSuccess(new PayCommand(INDEX_FIRST_PERSON, record), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_identicalPayments_retainedAsSeparateEntries() throws Exception {
        PaymentRecord record = payment("150", "2026-10-20", null);

        new PayCommand(INDEX_FIRST_PERSON, record).execute(model);
        new PayCommand(INDEX_FIRST_PERSON, record).execute(model);

        List<PaymentRecord> payments = model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased()).getPaymentRecords();
        assertEquals(2, payments.size());
        assertEquals(record, payments.get(0));
        assertEquals(record, payments.get(1));
    }

    @Test
    public void execute_filteredList_recordsForDisplayedClientOnly() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person displayed = model.getFilteredPersonList().get(0);
        Person notDisplayed = model.getAddressBook().getPersonList().stream()
                .filter(person -> !person.isSamePerson(displayed))
                .findFirst()
                .orElseThrow();
        PaymentRecord record = payment("150", "2026-10-20", null);

        new PayCommand(INDEX_FIRST_PERSON, record).execute(model);

        assertEquals(List.of(record), model.getFilteredPersonList().get(0).getPaymentRecords());
        Person unchanged = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.isSamePerson(notDisplayed))
                .findFirst()
                .orElseThrow();
        assertTrue(unchanged.getPaymentRecords().isEmpty());
    }

    @Test
    public void execute_indexOutsideDisplayedList_throwsCommandException() {
        Model singlePersonModel = new ModelManager(new AddressBook(), new UserPrefs());
        singlePersonModel.addPerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        PaymentRecord record = payment("150", "2026-10-20", null);

        assertCommandFailure(new PayCommand(INDEX_SECOND_PERSON, record), singlePersonModel,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        PaymentRecord firstPayment = payment("150", "2026-10-20", null);
        PaymentRecord secondPayment = payment("75.50", "2026-10-19", "October membership payment");
        PayCommand standardCommand = new PayCommand(INDEX_FIRST_PERSON, firstPayment);

        assertTrue(standardCommand.equals(new PayCommand(INDEX_FIRST_PERSON, firstPayment)));
        assertTrue(standardCommand.equals(standardCommand));
        assertFalse(standardCommand.equals(null));
        assertFalse(standardCommand.equals(new ClearCommand()));
        assertFalse(standardCommand.equals(new PayCommand(INDEX_SECOND_PERSON, firstPayment)));
        assertFalse(standardCommand.equals(new PayCommand(INDEX_FIRST_PERSON, secondPayment)));
    }

    @Test
    public void toStringMethod() {
        PaymentRecord record = payment("150", "2026-10-20", null);
        PayCommand command = new PayCommand(INDEX_FIRST_PERSON, record);
        String expected = PayCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", paymentRecord=" + record
                + "}";

        assertEquals(expected, command.toString());
    }

    private static PaymentRecord payment(String amount, String date, String remarks) {
        return PaymentRecord.fromText(amount, date,
                remarks == null ? Optional.empty() : Optional.of(remarks));
    }
}
