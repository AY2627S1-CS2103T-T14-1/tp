package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HealthProfile;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class HealthCommandTest {
    @Test
    public void execute_recordThenClear_success() {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().getFirst();
        Person withProfile = new PersonBuilder(original).withHealthProfile("Avoid jumping").build();
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, withProfile);

        assertCommandSuccess(new HealthCommand(INDEX_FIRST_PERSON, new HealthProfile("Avoid jumping")), model,
                String.format(HealthCommand.MESSAGE_RECORDED, original.getName()), expected);

        Person current = model.getFilteredPersonList().getFirst();
        Person cleared = current.withHealthProfile(null);
        expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(current, cleared);
        assertCommandSuccess(new HealthCommand(INDEX_FIRST_PERSON, null), model,
                String.format(HealthCommand.MESSAGE_CLEARED, current.getName()), expected);
    }

    @Test
    public void execute_unchangedProfile_fails() {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().getFirst();
        Person withProfile = original.withHealthProfile(new HealthProfile("Avoid jumping"));
        model.setPerson(original, withProfile);

        assertCommandFailure(new HealthCommand(INDEX_FIRST_PERSON, new HealthProfile("Avoid jumping")), model,
                HealthCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void execute_record_preservesExistingGoal() throws Exception {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().getFirst();
        model.setPerson(original, new PersonBuilder(original).withGoal("Build strength").build());

        new HealthCommand(INDEX_FIRST_PERSON, new HealthProfile("Avoid jumping")).execute(model);

        assertEquals("Build strength", model.getFilteredPersonList().getFirst().getGoal().orElseThrow().value);
    }
}
