package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Add-only address limits must not invalidate legacy data or disturb command history. */
public class AddAddressLengthIntegrationTest {
    private static final String ADD_ALICE = "add n/Alice Alpha p/91234567 e/alice@example.com a/";
    private static final String ADD_BOB = "add n/Bob Beta p/92345678 e/bob@example.com a/";
    private static final String LENGTH_ERROR =
            "Address must contain 1 to 200 printable characters without line breaks.";

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
    public void execute_addAtLimit_trimsAndPersistsAcrossReload() throws Exception {
        String address = "A".repeat(200);
        logic.execute(ADD_ALICE + "  " + address + "  ");
        assertEquals(address, model.getAddressBook().getPersonList().get(0).getAddress().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logicFor(reloaded).execute("edit 1 g/Build strength");
        assertEquals(address, storage.readAddressBook().orElseThrow().getPersonList().get(0).getAddress().value);
    }

    @Test
    public void execute_oversizedAdd_preservesModelSavedFileAndUndoRedo() throws Exception {
        logic.execute(ADD_ALICE + "1 Alpha Road");
        logic.execute("remark 1 r/Keep this remark");
        logic.execute("undo");
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(dataFile);

        assertThrows(ParseException.class, LENGTH_ERROR, () -> logic.execute(ADD_BOB + "B".repeat(201)));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(dataFile));

        logic.execute("redo");
        assertEquals("Keep this remark", model.getAddressBook().getPersonList().get(0).getRemark().value);
        logic.execute("undo");
        assertEquals(before, model.getAddressBook());
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_oversizedFirstAdd_createsNeitherFileNorUndoStep() throws Exception {
        assertThrows(ParseException.class, LENGTH_ERROR, () -> logic.execute(ADD_ALICE + "A".repeat(201)));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(dataFile));
        assertEquals(UndoCommand.MESSAGE_FAILURE, logic.execute("undo").getFeedbackToUser());
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void execute_legacyOversizedAddress_loadsAndSurvivesUnrelatedEditAndSave() throws Exception {
        String legacyAddress = "L".repeat(201);
        model.addPerson(new PersonBuilder().withAddress(legacyAddress).build());
        storage.saveAddressBook(model.getAddressBook());

        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(legacyAddress, reloaded.getAddressBook().getPersonList().get(0).getAddress().value);
        logicFor(reloaded).execute("edit 1 g/Build strength");
        assertEquals(legacyAddress, reloaded.getAddressBook().getPersonList().get(0).getAddress().value);
        assertEquals(legacyAddress, storage.readAddressBook().orElseThrow().getPersonList().get(0).getAddress().value);
    }

    @Test
    public void execute_editOversizedAddress_retainsExistingEditContract() throws Exception {
        logic.execute(ADD_ALICE + "1 Alpha Road");
        String address = "E".repeat(201);
        logic.execute("edit 1 a/" + address);
        assertEquals(address, model.getAddressBook().getPersonList().get(0).getAddress().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private LogicManager logicFor(ModelManager targetModel) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }
}
