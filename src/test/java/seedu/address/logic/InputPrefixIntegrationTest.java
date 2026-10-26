package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.DietPlan;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises rejected prefix typos through the real parser, model and JSON storage path. */
public class InputPrefixIntegrationTest {
    @TempDir
    public Path folder;

    @Test
    public void execute_mistypedEditAddress_preservesGoalAddressAndSavedData() throws Exception {
        assertRejectedWithoutChanges("edit 1 g/Run 10 km ad/18 Clementi Road");
    }

    @Test
    public void execute_mistypedDietNotes_preservesTargetNotesAndSavedData() throws Exception {
        assertRejectedWithoutChanges("diet 1 t/2000 kcal nn/Include vegetables");
    }

    private void assertRejectedWithoutChanges(String command) throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withGoal("Original goal")
                .withDietPlan(new DietPlan("1800 kcal", "Keep existing notes")).build());
        Model model = new ModelManager(original, new UserPrefs());
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(folder.resolve("clients.json")),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
        storage.saveAddressBook(original);
        byte[] originalBytes = Files.readAllBytes(storage.getAddressBookFilePath());
        LogicManager logic = new LogicManager(model, storage);

        assertThrows(ParseException.class, () -> logic.execute(command));

        assertEquals(original, model.getAddressBook());
        assertArrayEquals(originalBytes, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertEquals(original, storage.readAddressBook().orElseThrow());
    }
}
