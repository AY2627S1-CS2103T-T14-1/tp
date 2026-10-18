package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.DietaryRestrictions;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class DietaryRestrictionsCommandTest {
    @Test
    public void execute_replaceThenClear_success() {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().getFirst();
        DietaryRestrictions recorded = new DietaryRestrictions(List.of("Peanut allergy", "Halal meals"));
        Person updated = new PersonBuilder(original).withDietaryRestrictions("Peanut allergy", "Halal meals").build();
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, updated);

        assertCommandSuccess(new DietaryRestrictionsCommand(INDEX_FIRST_PERSON, recorded), model,
                String.format(DietaryRestrictionsCommand.MESSAGE_RECORDED, original.getName()), expected);

        Person current = model.getFilteredPersonList().getFirst();
        Person cleared = current.withDietaryRestrictions(new DietaryRestrictions());
        expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(current, cleared);
        assertCommandSuccess(new DietaryRestrictionsCommand(INDEX_FIRST_PERSON, new DietaryRestrictions()), model,
                String.format(DietaryRestrictionsCommand.MESSAGE_CLEARED, current.getName()), expected);
    }

    @Test
    public void execute_unchangedRestrictions_fails() {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().getFirst();
        DietaryRestrictions restrictions = new DietaryRestrictions(List.of("Halal meals"));
        model.setPerson(original, original.withDietaryRestrictions(restrictions));

        assertCommandFailure(new DietaryRestrictionsCommand(INDEX_FIRST_PERSON, restrictions), model,
                DietaryRestrictionsCommand.MESSAGE_NO_CHANGES);
    }
}
