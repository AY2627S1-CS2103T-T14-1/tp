package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Verifies that startup never replaces an unreadable existing client file with an empty roster. */
@ResourceLock("java.util.TimeZone.default")
public class MainAppLoadTest {
    @TempDir
    public Path folder;

    @Test
    public void initModelManager_missingFile_usesSampleDataWithoutWriting() {
        StorageManager storage = storage();
        assertFalse(new MainApp().initModelManager(storage, new UserPrefs())
                .getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void initModelManager_malformedFile_stopsAndPreservesOriginalBytes() throws Exception {
        StorageManager storage = storage();
        Path dataFile = storage.getAddressBookFilePath();
        Files.writeString(dataFile, "not json");
        byte[] original = Files.readAllBytes(dataFile);
        MainApp app = new MainApp();
        app.storage = storage;

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> app.initModelManager(storage, new UserPrefs()));
        app.stop();

        assertTrue(exception.getMessage().contains("protect the existing file"));
        assertTrue(exception.getMessage().contains(dataFile.toString()));
        assertArrayEquals(original, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_workoutBecomesFutureAfterZoneChange_preservesRecoverableRoster() throws Exception {
        TimeZone originalZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            LocalDate recordedDate = LocalDate.now();
            WorkoutSession session = new WorkoutSession(recordedDate, Optional.empty(),
                    List.of(new WorkoutLogEntry("Squat", new BigDecimal("60"), 3, 10)));
            AddressBook original = new AddressBook();
            original.addPerson(new PersonBuilder().withWorkoutSessions(List.of(session)).build());
            original.addPerson(new PersonBuilder().withName("Other Client").build());
            StorageManager storage = storage();
            storage.saveAddressBook(original);
            byte[] originalBytes = Files.readAllBytes(storage.getAddressBookFilePath());

            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Pago_Pago"));
            assertTrue(LocalDate.now().isBefore(recordedDate));
            assertThrows(IllegalStateException.class, () ->
                    new MainApp().initModelManager(storage, new UserPrefs()));
            assertArrayEquals(originalBytes, Files.readAllBytes(storage.getAddressBookFilePath()));

            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            assertEquals(original, new MainApp().initModelManager(storage, new UserPrefs()).getAddressBook());
        } finally {
            TimeZone.setDefault(originalZone);
        }
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(folder.resolve("clients.json")),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
    }
}
