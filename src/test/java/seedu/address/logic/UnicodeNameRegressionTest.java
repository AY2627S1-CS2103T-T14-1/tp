package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises Greek case variants through the actual find command path. */
public class UnicodeNameRegressionTest {
    @Test
    public void find_allSigmaForms_returnTheSameClients() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("ΟΣ").withPhone("91000170").build());
        model.addPerson(new PersonBuilder().withName("Οσ").withPhone("91000171").build());
        for (String keyword : List.of("Σ", "σ", "ς")) {
            new AddressBookParser().parseCommand("find n/" + keyword).execute(model);
            assertEquals(2, model.getFilteredPersonList().size());
        }
    }

}
