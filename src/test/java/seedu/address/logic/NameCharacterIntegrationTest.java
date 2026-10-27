package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Documented name characters must work across commands, persistence and legacy records. */
public class NameCharacterIntegrationTest {
    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private Path dataFile;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        dataFile = testFolder.resolve("addressbook.json");
        storage = new JsonAddressBookStorage(dataFile);
        logic = logicFor(model);
    }

    @Test
    public void execute_issue244_addsHyphenatedNameAndReloadsIt() throws Exception {
        logic.execute("add n/Anne-Marie Tan p/92345678 e/annemarie@example.com a/123 Test Road");
        assertEquals("Anne-Marie Tan", model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_documentedCharacters_surviveRestartAndUnrelatedEdit() throws Exception {
        List<String> names = List.of("D'Angelo Lim", "Dr. Ahmad Rahman", "Jos\u00e9 Tan", "\u674e\u5c0f\u9f99");
        for (String name : names) {
            logic.execute(addCommand(name));
        }
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logicFor(reloaded).execute("edit 1 g/Build strength");
        assertEquals(names, storage.readAddressBook().orElseThrow().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void execute_editName_preservesPunctuationCaseAndInternalSpacing() throws Exception {
        logic.execute(addCommand("Alice Alpha"));
        for (String name : List.of("Anne-Marie Tan", "D'Angelo  Lim", "Dr. Ahmad Rahman", "Jos\u00e9 Tan")) {
            logic.execute("edit 1 n/  " + name + "  ");
            assertEquals(name, model.getAddressBook().getPersonList().get(0).getName().fullName);
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_invalidNames_leaveModelAndSavedFileUnchanged() throws Exception {
        logic.execute(addCommand("Alice Alpha"));
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(dataFile);
        for (String name : List.of("", "---", "...", "'", "Mary@Tan", "Alex/Tan")) {
            assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> logic.execute(addCommand(name)));
            assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> logic.execute("edit 1 n/" + name));
            assertEquals(before, model.getAddressBook());
            assertEquals(saved, Files.readString(dataFile));
        }
    }

    @Test
    public void execute_duplicateNames_retainsExistingCaseSensitiveComparison() throws Exception {
        logic.execute(addCommand("Anne-Marie Tan"));
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(dataFile);
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () ->
                logic.execute(addCommand("Anne-Marie Tan")));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(dataFile));

        logic.execute(addCommand("anne-marie tan"));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_existingNumericAndLongNames_remainLoadableAndEditable() throws Exception {
        List<String> legacyNames = List.of("12345", "A".repeat(200));
        for (String name : legacyNames) {
            model.addPerson(new PersonBuilder().withName(name).build());
        }
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logicFor(reloaded).execute("edit 1 g/Build strength");
        assertEquals(legacyNames, storage.readAddressBook().orElseThrow().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    private String addCommand(String name) {
        return "add n/" + name + " p/92345678 e/name@example.com a/123 Test Road";
    }

    private LogicManager logicFor(ModelManager targetModel) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }
}
