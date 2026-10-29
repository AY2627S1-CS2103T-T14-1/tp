package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.BodyMutationCommand;
import seedu.address.logic.commands.BodyProgressViewRequest;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class BodyProgressIntegrationTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2020-02-01T12:00:00Z"), ZoneOffset.UTC);
    private static final String BASE = "bodybase 1 d/2020-01-01 w/75.50 h/175";
    @TempDir
    private Path directory;

    @Test
    void workflow_filteredTargetPersistenceCorrectionsAndDeletion() throws Exception {
        ModelManager model = model();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("book.json"), CLOCK);
        LogicManager logic = logic(model, storage);
        Person otherClient = model.getAddressBook().getPersonList().getFirst();
        AddressBook emptySnapshot = new AddressBook(model.getAddressBook());
        assertTrue(logic.execute("bodyhistory 1").getFeedbackToUser().startsWith("No body baseline recorded"));
        assertTrue(logic.execute("bodycompare 1").getBodyProgressViewRequest().isPresent());
        assertEquals(emptySnapshot, model.getAddressBook());
        assertCommandError(logic, "bodylog 1 d/2020-01-08 w/74", "No body baseline recorded");
        assertEquals("Created body baseline for Amy Bee: 2020-01-01; Weight: 75.50 kg; Height: 175.00 cm.",
                logic.execute(BASE).getFeedbackToUser());
        assertCommandError(logic, "bodybase 1 d/2020-01-01 w/75.5 h/175.00", "already the current");
        assertTrue(logic.execute("bodycompare 1").getFeedbackToUser().contains("Use bodylog before comparing"));
        logic.execute("bodylog 1 d/2020-01-15 w/74.20");
        logic.execute("bodylog 1 d/2020-01-08 w/74.80 n/Measured before breakfast");
        BodyProgress populated = selected(model).getBodyProgress();
        assertEquals(List.of(LocalDate.of(2020, 1, 8), LocalDate.of(2020, 1, 15)),
                populated.getEntries().stream().map(entry -> entry.date()).toList());
        assertEquals(otherClient, model.getAddressBook().getPersonList().getFirst());
        assertEquals(1, model.getFilteredPersonList().size());
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        restarted.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Amy")));
        assertEquals(populated, selected(restarted).getBodyProgress());
        logic = logic(restarted, storage);
        assertEquals("Showing body progress history for Amy Bee: 2 entries.",
                logic.execute("bodyhistory 1").getFeedbackToUser());
        logic.execute("bodylog 1 d/2020-01-08 w/74.60");
        assertTrue(selected(restarted).getBodyProgress().getEntries().getFirst().notes().isEmpty());
        assertCommandError(logic, "bodylog 1 d/2020-01-08 w/74.60", "already exists");
        assertCommandError(logic, "bodybase 1 d/2020-01-10 w/75 h/175", "earlier than every");
        assertCommandError(logic, "bodylog 1 d/2020-01-01 w/74", "later than the baseline");
        logic.execute("bodybase 1 d/2020-01-01 w/75 h/175");
        assertEquals(2, selected(restarted).getBodyProgress().getEntries().size());
        CommandResult compared = logic.execute("bodycompare 1");
        assertEquals(selected(restarted), compared.getBodyProgressViewRequest().orElseThrow().person());
        assertEquals(BodyProgressViewRequest.Section.COMPARISON,
                compared.getBodyProgressViewRequest().orElseThrow().section());
        logic.execute("bodydelete 1 d/2020-01-15");
        assertEquals(LocalDate.of(2020, 1, 8),
                selected(restarted).getBodyProgress().getLatestEntry().orElseThrow().date());
        logic.execute("bodydelete 1 d/2020-01-08");
        assertTrue(selected(restarted).getBodyProgress().getBaseline().isPresent());
        assertTrue(selected(restarted).getBodyProgress().getEntries().isEmpty());
        assertCommandError(logic, "bodydelete 1 d/2020-01-08", "No body progress entry exists");
        assertCommandError(logic, "bodydelete 1 d/2020-01-01", "baseline cannot be deleted");
    }

    @Test
    void publicRouter_rejectsInvalidInputWithoutChangingModelOrFile() throws Exception {
        ModelManager model = model();
        Path file = directory.resolve("router.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file, CLOCK);
        LogicManager logic = logic(model, storage);
        logic.execute(BASE);
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] bytes = Files.readAllBytes(file);
        for (String input : List.of("bodyhistory 0", "bodyhistory -1", "bodyhistory +1", "bodyhistory 1.0",
                "bodyhistory 2147483648", "bodyhistory 1 extra", "bodycompare 1 d/2020-01-01",
                "bodylog 1 d/2020-01-08 w/74 w/75", "bodylog 1 d/2020-01-08 w/74 ex/Squat",
                "bodylog 1 d/2020-01-08 w/74 n/Text n/Again", "bodybase 1 d/2020-01-01 w/75",
                "bodylog 1 d/2020-01-08 w/74 n/", "bodylog 1 d/2020-02-30 w/74",
                "bodylog 1 d/2020-02-02 w/74", "bodylog 1 d/0000-01-01 w/74",
                "bodylog 1 d/2020-01-08 w/0", "bodylog 1 d/2020-01-08 w/500.01",
                "bodylog 1 d/2020-01-08 w/74.001", "bodylog 1 d/2020-01-08 w/NaN",
                "bodylog 1 d/2020-01-08 w/74 n/" + "x".repeat(301),
                "bodylog 1 d/2020-01-08 w/74 n/Text\u0000", "bodylog 1 d/2020-01-08 w/74 n/Text\u200B",
                "bodylog 1 d/2020-01-08 w/74\n", "bodylog 1 d/2020-01-08 w/74 n/Text\r\n",
                "bodyhistory 1\n", "bodycompare 1\r", "bodydelete 1 d/2020-01-08\n")) {
            assertThrows(ParseException.class, () -> logic.execute(input), input);
            assertEquals(before, model.getAddressBook(), input);
            assertArrayEquals(bytes, Files.readAllBytes(file), input);
        }
        for (String command : List.of("bodybase 2 d/2020-01-01 w/75 h/175", "bodyhistory 2", "bodycompare 2",
                "bodylog 2 d/2020-01-08 w/74", "bodydelete 2 d/2020-01-08")) {
            assertCommandError(logic, command, "No client exists at index 2.");
        }
        assertEquals(before, model.getAddressBook());
        logic.execute("bodylog 01 n/Walk  5 km/day d/2020-01-08 w/074.50");
        assertEquals("Walk  5 km/day", selected(model).getBodyProgress().getEntries().getFirst().notes().orElseThrow());
    }

    @Test
    void failedSaves_everyMutationFormRestoresWholeSnapshotFilterCatalogAndFile() throws Exception {
        List<String> mutations = List.of(BASE, "bodybase 1 d/2020-01-01 w/76 h/175",
                "bodylog 1 d/2020-01-15 w/74", "bodylog 1 d/2020-01-08 w/73",
                "bodydelete 1 d/2020-01-08");
        for (int scenario = 0; scenario < mutations.size(); scenario++) {
            ModelManager model = model();
            Path file = directory.resolve("failure-" + scenario + ".json");
            JsonAddressBookStorage normal = new JsonAddressBookStorage(file, CLOCK);
            LogicManager setup = logic(model, normal);
            if (scenario > 0) {
                setup.execute(BASE);
                setup.execute("bodylog 1 d/2020-01-08 w/74.80 n/Original");
            }
            setup.execute("addmem n/Gold p/150 s/30");
            normal.saveAddressBook(model.getAddressBook());
            AddressBook before = new AddressBook(model.getAddressBook());
            Person beforePerson = selected(model);
            byte[] bytes = Files.readAllBytes(file);
            JsonAddressBookStorage failing = new JsonAddressBookStorage(file, CLOCK) {
                @Override
                protected void replaceAddressBookFile(Path temporaryFile, Path destination) throws IOException {
                    Files.writeString(temporaryFile, "Incomplete candidate");
                    throw new IOException("Injected atomic commit failure");
                }
            };
            String command = mutations.get(scenario);
            CommandException error = assertThrows(CommandException.class, () -> logic(model, failing).execute(command));
            assertEquals(BodyMutationCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertEquals(List.of(beforePerson), model.getFilteredPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(file));
            try (var files = Files.list(directory)) {
                assertFalse(files.anyMatch(path -> path.toString().endsWith(".tmp")));
            }
            logic(model, normal).execute("bodyhistory 1");
            assertEquals(before, new AddressBook(normal.readAddressBook().orElseThrow()));
        }
    }

    @Test
    void bodyMutations_preservePaymentRecords() throws Exception {
        ModelManager model = model();
        PaymentRecord payment = PaymentRecord.fromText("150", "2026-01-15", Optional.of("Initial fee"));
        Person amy = selected(model);
        model.setPerson(amy, amy.withAddedPayment(payment));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(
                directory.resolve("payments.json"), CLOCK);
        LogicManager logic = logic(model, storage);

        for (String command : List.of(BASE, "bodylog 1 d/2020-01-08 w/74.80 n/Before breakfast",
                "bodydelete 1 d/2020-01-08")) {
            logic.execute(command);
            assertEquals(List.of(payment), selected(model).getPaymentRecords(), command);
            Person savedAmy = storage.readAddressBook().orElseThrow().getPersonList().stream()
                    .filter(person -> person.getName().fullName.equals("Amy Bee"))
                    .findFirst().orElseThrow();
            assertEquals(List.of(payment), savedAmy.getPaymentRecords(), command);
        }
    }

    @Test
    void queries_unwritableStoragePreservesFileFilterAndRedoHistory() throws Exception {
        ModelManager model = model();
        Path file = directory.resolve("read-only-body.json");
        boolean[] failSave = {false};
        int[] saveAttempts = {0};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file, CLOCK) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                saveAttempts[0]++;
                if (failSave[0]) {
                    throw new IOException("Read-only destination");
                }
                super.saveAddressBook(addressBook);
            }
        };
        LogicManager logic = logic(model, storage);
        logic.execute(BASE);
        logic.execute("bodylog 1 d/2020-01-08 w/74.80 n/Original");
        logic.execute("undo");
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Amy")));
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] bytes = Files.readAllBytes(file);
        saveAttempts[0] = 0;
        failSave[0] = true;

        for (String command : List.of("bodyhistory 1", "bodycompare 1")) {
            CommandResult result = logic.execute(command);
            assertEquals(selected(model), result.getBodyProgressViewRequest().orElseThrow().person());
            assertEquals(before, model.getAddressBook());
            assertEquals(1, model.getFilteredPersonList().size());
            assertArrayEquals(bytes, Files.readAllBytes(file));
        }
        assertEquals(0, saveAttempts[0]);

        failSave[0] = false;
        logic.execute("redo");
        Person amy = model.getAddressBook().getPersonList().get(1);
        assertEquals(1, amy.getBodyProgress().getEntries().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void queries_missingBaselineDoNotCreateDataFile() throws Exception {
        ModelManager model = model();
        Path file = directory.resolve("unsaved-body.json");
        LogicManager logic = logic(model, new JsonAddressBookStorage(file, CLOCK));
        AddressBook before = new AddressBook(model.getAddressBook());

        for (String command : List.of("bodyhistory 1", "bodycompare 1")) {
            CommandResult result = logic.execute(command);
            assertTrue(result.getFeedbackToUser().startsWith("No body baseline recorded"));
            assertEquals(selected(model), result.getBodyProgressViewRequest().orElseThrow().person());
            assertFalse(Files.exists(file));
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    void otherCommandsAndCatalogOperations_preserveBodyData() throws Exception {
        ModelManager model = model();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("preserve.json"), CLOCK);
        LogicManager logic = logic(model, storage);
        logic.execute(BASE);
        logic.execute("bodylog 1 d/2020-01-08 w/74 n/Original");
        BodyProgress expected = selected(model).getBodyProgress();
        for (String command : List.of("edit 1 p/91234567", "remark 1 r/Morning sessions",
                "diet 1 t/1800 kcal n/Vegetables", "health 1 h/Avoid jumping",
                "addex 1 n/Squat s/3 r/8 w/60 d/Mon", "dayex 1 1 Fri", "delex 1 1",
                "addex 1 n/Stretch", "clearplan 1", "schedule 1 d/2099-03-15 tm/14:30 l/Gym",
                "log 1 d/2020-01-10 ex/Squat|60|3|8", "addmem n/Gold p/150 s/30", "amem Gold 1",
                "imem 1 2", "dmem 1 1", "rmem 1", "clearmem")) {
            model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Amy")));
            logic.execute(command);
            Person amy = model.getAddressBook().getPersonList().stream()
                    .filter(person -> person.getName().fullName.equals("Amy Bee")).findFirst().orElseThrow();
            assertEquals(expected, amy.getBodyProgress(), command);
        }
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Amy")));
        logic.execute("delete 1");
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
        assertEquals(new BodyProgress(), storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getBodyProgress());
    }

    @Test
    void execution_rechecksDateAfterClockChanges() throws Exception {
        MutableClock clock = new MutableClock();
        AddressBookParser parser = new AddressBookParser(clock);
        Command baseline = parser.parseCommand("bodybase 1 d/2020-02-01 w/75 h/175");
        clock.instant = Instant.parse("2020-01-31T12:00:00Z");
        ModelManager model = model();
        assertThrows(CommandException.class, () -> baseline.execute(model));
        assertTrue(selected(model).getBodyProgress().getBaseline().isEmpty());
    }

    @Test
    void resultsAndCommands_valueEqualityIncludesResolvedTargetAndSection() throws Exception {
        AddressBookParser parser = new AddressBookParser(CLOCK);
        assertEquals(parser.parseCommand(BASE), parser.parseCommand(BASE));
        assertEquals(parser.parseCommand(BASE).hashCode(), parser.parseCommand(BASE).hashCode());
        for (String command : List.of("bodylog 1 d/2020-01-08 w/74", "bodyhistory 1", "bodycompare 1",
                "bodydelete 1 d/2020-01-08")) {
            assertEquals(parser.parseCommand(command), parser.parseCommand(command));
            assertEquals(parser.parseCommand(command).hashCode(), parser.parseCommand(command).hashCode());
        }
        Person person = new PersonBuilder().build();
        CommandResult history = new CommandResult("message",
                new BodyProgressViewRequest(person, BodyProgressViewRequest.Section.HISTORY));
        CommandResult same = new CommandResult("message",
                new BodyProgressViewRequest(person, BodyProgressViewRequest.Section.HISTORY));
        assertEquals(history, same);
        assertEquals(history.hashCode(), same.hashCode());
        assertNotEquals(history, new CommandResult("message"));
        assertNotEquals(history, new CommandResult("message",
                new BodyProgressViewRequest(person, BodyProgressViewRequest.Section.COMPARISON)));
    }

    private static ModelManager model() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Bob Tan").build());
        model.addPerson(new PersonBuilder().build());
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Amy")));
        return model;
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))), CLOCK);
    }

    private static Person selected(ModelManager model) {
        return model.getFilteredPersonList().getFirst();
    }

    private static void assertCommandError(LogicManager logic, String command, String fragment) {
        CommandException exception = assertThrows(CommandException.class, () -> logic.execute(command));
        assertTrue(exception.getMessage().contains(fragment), exception.getMessage());
    }

    private static final class MutableClock extends Clock {
        private Instant instant = Instant.parse("2020-02-01T12:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
