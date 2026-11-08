package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession.Kind;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises scheduling through the parser, logic, undo history and real JSON storage together. */
public class SchedulingWorkflowTest {
    @TempDir
    public Path directory;
    private ModelManager model;
    private LogicManager logic;
    private FailingStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Amy Bee").build());
        book.addPerson(new PersonBuilder().withName("Bob Cat").withPhone("91234567")
                .withEmail("bob@example.com").build());
        model = new ModelManager(book, new UserPrefs());
        storage = new FailingStorage(directory.resolve("clients.json"));
        storage.saveAddressBook(book);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }

    @Test
    public void legacyExpandedYearAppointment_remainsReadableAfterInputValidationChange() throws Exception {
        Person first = model.getAddressBook().getPersonList().getFirst();
        LocalDateTime legacyTime = LocalDateTime.of(10000, 1, 1, 10, 0);
        model.setPerson(first, first.withTrainingSession(
                new seedu.address.model.person.TrainingSession(legacyTime, "Legacy gym")));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(legacyTime, storage.readAddressBook().orElseThrow().getPersonList().getFirst()
                .getTrainingSessions().getFirst().getDateTime());
    }

    @Test
    public void meeting_failedSaveThenRetry_preservesFilteredIndexAndUndoSteps() throws Exception {
        logic.execute("find Bob");
        Person bob = model.getFilteredPersonList().getFirst();
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        storage.failWrites = true;

        assertThrows(CommandException.class, () ->
                logic.execute("meeting 1 d/2099-03-15 tm/14:30 l/Cafe"));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));

        storage.failWrites = false;
        logic.execute("meeting 1 d/2099-03-15 tm/14:30 l/Cafe");
        AddressBook after = new AddressBook(model.getAddressBook());
        assertEquals(Kind.MEETING, after.getPersonList().get(1).getTrainingSessions().getFirst().getKind());
        assertEquals(List.of(), after.getPersonList().getFirst().getTrainingSessions());
        logic.execute("undo");
        assertEquals(before, model.getAddressBook());
        assertEquals("No changes to undo.", logic.execute("undo").getFeedbackToUser());
        logic.execute("redo");
        assertEquals(after, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void restart_dayQueryIncludesHiddenClient_preservesTypesAndChronologicalOrder() throws Exception {
        logic.execute("schedule 1 d/2099-03-15 tm/16:30 l/Main gym");
        logic.execute("meeting 2 d/2099-03-15 tm/14:30 l/Cafe");
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restarted = new LogicManager(reloaded, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("restart-preferences.json"))));
        restarted.execute("find Amy");

        assertEquals("Client appointments on 2099-03-15:\n"
                + "1. 14:30 Bob Cat (meeting) at Cafe\n2. 16:30 Amy Bee (training) at Main gym",
                restarted.execute("dayschedule d/2099-03-15").getFeedbackToUser());
        assertEquals(1, reloaded.getFilteredPersonList().size());
        assertEquals("Training sessions for Amy Bee:\n1. 2099-03-15 16:30 at Main gym",
                restarted.execute("viewschedule 1").getFeedbackToUser());
        assertEquals(Kind.TRAINING,
                reloaded.getAddressBook().getPersonList().getFirst().getTrainingSessions().getFirst().getKind());
        assertEquals(Kind.MEETING,
                reloaded.getAddressBook().getPersonList().get(1).getTrainingSessions().getFirst().getKind());
        assertEquals("No changes to undo.", restarted.execute("undo").getFeedbackToUser());
    }

    @Test
    public void conflictAndReadOnlyQueries_preserveDiskAndRedo_whenStorageIsUnwritable() throws Exception {
        logic.execute("schedule 1 d/2099-03-15 tm/14:30 l/Main gym");
        String saved = Files.readString(storage.getAddressBookFilePath());
        CommandException conflict = assertThrows(CommandException.class, () ->
                logic.execute("meeting 2 d/2099-03-15 tm/14:30 l/Cafe"));
        assertEquals("Scheduling conflict: Amy Bee already has an appointment at 2099-03-15 14:30.",
                conflict.getMessage());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("undo");
        AddressBook undone = new AddressBook(model.getAddressBook());
        String undoneFile = Files.readString(storage.getAddressBookFilePath());
        storage.failWrites = true;

        for (String query : List.of("list", "find Bob", "viewschedule 1",
                "dayschedule d/2099-03-15", "inactive 1")) {
            logic.execute(query);
            assertEquals(undone, model.getAddressBook(), query);
            assertEquals(undoneFile, Files.readString(storage.getAddressBookFilePath()), query);
        }
        assertThrows(CommandException.class, () -> logic.execute("redo"));
        assertEquals(undone, model.getAddressBook());
        storage.failWrites = false;
        assertEquals("Redid the last change.", logic.execute("redo").getFeedbackToUser());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }

    private static final class FailingStorage extends JsonAddressBookStorage {
        private boolean failWrites;

        private FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (failWrites) {
                throw new AccessDeniedException(getAddressBookFilePath().toString());
            }
            super.saveAddressBook(addressBook);
        }
    }
}
