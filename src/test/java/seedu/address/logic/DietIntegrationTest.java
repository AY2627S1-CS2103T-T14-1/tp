package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.DietCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class DietIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_createReplaceAndDelete_persistsAcrossReloads() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        Path file = testFolder.resolve("nested/book.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        LogicManager logic = logic(model, storage);
        assertEquals("Created diet plan for Amy Bee.",
                logic.execute("diet 1 t/1800 kcal/day n/Lean protein").getFeedbackToUser());
        assertEquals(new DietPlan("1800 kcal/day", "Lean protein"),
                storage.readAddressBook().orElseThrow().getPersonList().get(0).getDietPlan().orElseThrow());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = logic(reloaded, storage);
        assertEquals("Replaced diet plan for Amy Bee.",
                restartedLogic.execute("diet 1 t/2000 kcal").getFeedbackToUser());
        assertEquals(new DietPlan("2000 kcal"),
                storage.readAddressBook().orElseThrow().getPersonList().get(0).getDietPlan().orElseThrow());
        restartedLogic.execute("delete 1");
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertFalse(Files.readString(file).contains("2000 kcal"));
    }

    @Test
    public void execute_invalidOrDuplicatePlan_leavesModelAndFileUnchanged() throws Exception {
        ModelManager model = new ModelManager();
        Person original = new PersonBuilder().withDietPlan(new DietPlan("1800 kcal", "Lean protein")).build();
        model.addPerson(original);
        Path file = testFolder.resolve("book.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(model.getAddressBook());
        byte[] previousFile = Files.readAllBytes(file);
        LogicManager logic = logic(model, storage);
        for (String command : new String[] {"diet 1 t/ ", "diet 1 t/2000 kcal n/ ",
            "diet 1 t/2000 kcal r/Halal", "diet 1 t/2000 kcal t/1800 kcal", "diet 1 t/1800 kcal\n"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(original, model.getFilteredPersonList().get(0));
            assertArrayEquals(previousFile, Files.readAllBytes(file));
        }
        assertThrows(CommandException.class, "This is already the current diet plan for Amy Bee.", () ->
                logic.execute("diet 1 t/1800 KCAL n/lean PROTEIN"));
        assertArrayEquals(previousFile, Files.readAllBytes(file));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_failedCreateOrReplacement_restoresModelFilterAndSavedFile() throws Exception {
        for (boolean hasPlan : new boolean[] {false, true}) {
            Person original = hasPlan ? new PersonBuilder().withDietPlan(new DietPlan("1800 kcal", "Old notes")).build()
                    : new PersonBuilder().build();
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Bob Tan").build());
            model.addPerson(original);
            model.updateFilteredPersonList(new NameContainsKeywordsPredicate(Arrays.asList("Amy")));
            Path file = testFolder.resolve("book-" + hasPlan + ".json");
            JsonAddressBookStorage normal = new JsonAddressBookStorage(file);
            normal.saveAddressBook(model.getAddressBook());
            AddressBook previousModel = new AddressBook(model.getAddressBook());
            byte[] previousFile = Files.readAllBytes(file);
            JsonAddressBookStorage failing = new JsonAddressBookStorage(file) {
                @Override
                protected void replaceAddressBookFile(Path temporaryFile, Path destination) throws IOException {
                    Files.writeString(temporaryFile, "partial temporary data");
                    throw new IOException("Simulated commit failure");
                }
            };
            assertThrows(CommandException.class, DietCommand.MESSAGE_SAVE_FAILURE, () ->
                    logic(model, failing).execute("diet 1 t/2000 kcal"));
            assertEquals(previousModel, model.getAddressBook());
            assertEquals(Arrays.asList(original), model.getFilteredPersonList());
            assertArrayEquals(previousFile, Files.readAllBytes(file));
            assertNoTemporaryFiles();
            // A later successful save must not persist the rejected change.
            logic(model, normal).execute("list");
            assertEquals(previousModel, new AddressBook(normal.readAddressBook().orElseThrow()));
        }
    }

    @Test
    public void execute_accessDenied_restoresPreviousPlanAndUsesDietFailureMessage() {
        ModelManager model = new ModelManager();
        Person original = new PersonBuilder().withDietPlan(new DietPlan("1800 kcal")).build();
        model.addPerson(original);
        JsonAddressBookStorage failing = new JsonAddressBookStorage(testFolder.resolve("book.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new AccessDeniedException("Read-only data file");
            }
        };
        assertThrows(CommandException.class, DietCommand.MESSAGE_SAVE_FAILURE, () ->
                logic(model, failing).execute("diet 1 t/2000 kcal"));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void save_failedAtomicMove_preservesOldFileAndDoesNotCreateEmptyNewFile() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        Path existingFile = testFolder.resolve("existing.json");
        new JsonAddressBookStorage(existingFile).saveAddressBook(model.getAddressBook());
        byte[] previousFile = Files.readAllBytes(existingFile);
        for (Path file : new Path[] {existingFile, testFolder.resolve("missing.json")}) {
            JsonAddressBookStorage failing = new JsonAddressBookStorage(file) {
                @Override
                protected void replaceAddressBookFile(Path temporaryFile, Path destination) throws IOException {
                    throw new AtomicMoveNotSupportedException(temporaryFile.toString(), destination.toString(),
                            "Simulated unsupported provider");
                }
            };
            assertThrows(AtomicMoveNotSupportedException.class, () -> failing.saveAddressBook(model.getAddressBook()));
            assertNoTemporaryFiles();
        }
        assertArrayEquals(previousFile, Files.readAllBytes(existingFile));
        assertFalse(Files.exists(testFolder.resolve("missing.json")));
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage addressBookStorage) {
        return new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    private void assertNoTemporaryFiles() throws IOException {
        try (var files = Files.list(testFolder)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }
}
