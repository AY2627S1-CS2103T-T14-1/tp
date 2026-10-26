package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.logic.commands.ClearPlanCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteExerciseCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_undoHistoryLimit_discardsOldestStateAndKeepsRedoOrder() throws Exception {
        model.addPerson(new PersonBuilder().build());
        for (int i = 1; i <= LogicManager.MAX_UNDO_HISTORY_SIZE + 1; i++) {
            logic.execute("remark 1 r/Change " + i);
        }
        AddressBook latest = new AddressBook(model.getAddressBook());

        for (int i = 0; i < LogicManager.MAX_UNDO_HISTORY_SIZE; i++) {
            logic.execute("undo");
        }
        assertEquals("Change 1", model.getAddressBook().getPersonList().get(0).getRemark().value);
        assertEquals(UndoCommand.MESSAGE_FAILURE, logic.execute("undo").getFeedbackToUser());

        for (int i = 0; i < LogicManager.MAX_UNDO_HISTORY_SIZE; i++) {
            logic.execute("redo");
        }
        assertEquals(latest, model.getAddressBook());
        assertEquals(RedoCommand.MESSAGE_FAILURE, logic.execute("redo").getFeedbackToUser());
        for (int i = 0; i < LogicManager.MAX_UNDO_HISTORY_SIZE; i++) {
            logic.execute("undo");
        }
        assertEquals(UndoCommand.MESSAGE_FAILURE, logic.execute("undo").getFeedbackToUser());
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addExercise_success() throws Exception {
        Person person = new PersonBuilder(AMY).build();
        model.addPerson(person);

        Exercise exercise = new Exercise("Bench Press", 3, 10, 60.0);
        Person editedPerson = new PersonBuilder(person)
                .withWorkoutPlan(person.getWorkoutPlan().addExercise(exercise)).build();
        String expectedMessage = String.format(AddExerciseCommand.MESSAGE_ADD_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(editedPerson);

        assertCommandSuccess("addex 1 n/Bench Press s/3 r/10 w/60", expectedMessage, expectedModel);
    }

    @Test
    public void execute_addExerciseOptionalFields_updatesModelFeedbackAndSavedPlan() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addex 1 n/Stretch");
        logic.execute("addex 1 n/Squat s/4 d/m");
        logic.execute("addex 1 n/Press r/8 w/25.5");
        CommandResult result = logic.execute("addex 1 n/Run d/w");

        WorkoutPlan expected = new WorkoutPlan(List.of(
                new Exercise("Squat", 4, 1, 0, "Monday"),
                new Exercise("Run", 1, 1, 0, "Wednesday"),
                new Exercise("Stretch", 1, 1, 0),
                new Exercise("Press", 1, 8, 25.5)));
        assertEquals(expected, model.getFilteredPersonList().get(0).getWorkoutPlan());
        String expectedPlan = "(4):\n**Monday**\n1. Squat 4x1 @0kg\n\n"
                + "**Wednesday**\n2. Run 1x1 @0kg\n\n**Day not specified**\n"
                + "3. Stretch 1x1 @0kg\n4. Press 1x8 @25.5kg";
        assertTrue(result.getFeedbackToUser().endsWith(expectedPlan));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().get(0).getWorkoutPlan());
    }

    @Test
    public void execute_addExerciseInvalidOptionalFields_doesNotChangeModelOrSavedData() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addex 1 n/Stretch");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String saved = Files.readString(savedPath);
        String[] invalidArgs = {"s/", "r/", "w/", "d/", "s/0", "r/-1", "w/-1", "w/NaN",
            "d/t", "s/2 s/3", "r/2 r/3", "w/0 w/1", "d/m d/w", "n/Other"};
        for (String args : invalidArgs) {
            assertParseException("addex 1 n/Press " + args,
                    String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AddExerciseCommand.MESSAGE_USAGE));
            assertEquals(saved, Files.readString(savedPath));
        }
        assertCommandException("addex 1 n/stretch", AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE);
        assertCommandException("addex 2 n/Press", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(saved, Files.readString(savedPath));
    }

    @Test
    public void execute_workoutExtraParameters_doesNotChangeModelOrSavedData() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addex 1 n/Stretch d/m");
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String saved = Files.readString(savedPath);
        String[][] invalidCommands = {
            {"addex 1 n/Press x/unexpected", AddExerciseCommand.MESSAGE_USAGE},
            {"addex 1 s/3 n/Press ex/unexpected d/tu", AddExerciseCommand.MESSAGE_USAGE},
            {"addex 1 n/Press s/3 extra", AddExerciseCommand.MESSAGE_USAGE},
            {"delex 1 1 x/unexpected", DeleteExerciseCommand.MESSAGE_USAGE},
            {"delex 1 1 extra", DeleteExerciseCommand.MESSAGE_USAGE},
            {"dayex 1 1 Tuesday x/unexpected", SetExerciseDayCommand.MESSAGE_USAGE},
            {"dayex 1 1 Tuesday extra", SetExerciseDayCommand.MESSAGE_USAGE},
            {"clearplan 1 x/unexpected", ClearPlanCommand.MESSAGE_USAGE},
            {"clearplan 1 extra", ClearPlanCommand.MESSAGE_USAGE}
        };
        for (String[] command : invalidCommands) {
            assertParseException(command[0], String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, command[1]));
            assertEquals(saved, Files.readString(savedPath));
        }
    }

    @Test
    public void execute_dailyLimitAfterReload_rejectsOverflowWithoutChangingModelOrSavedData() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        for (int index = 0; index < 7; index++) {
            logic.execute("addex 1 n/Exercise " + index + " d/m");
        }
        logic.execute("addex 1 n/Stretch");

        Path savedPath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(savedPath);
        Model reloadedModel = new ModelManager(savedStorage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic reloadedLogic = new LogicManager(reloadedModel, new StorageManager(savedStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
        AddressBook expected = new AddressBook(model.getAddressBook());
        String saved = Files.readString(savedPath);
        assertEquals(expected, reloadedModel.getAddressBook());

        for (String command : List.of("addex 1 n/Overflow d/MON", "dayex 1 8 mOnDaY")) {
            assertThrows(CommandException.class, Messages.MESSAGE_DAILY_EXERCISE_LIMIT, () ->
                    reloadedLogic.execute(command));
            assertEquals(expected, reloadedModel.getAddressBook());
            assertEquals(saved, Files.readString(savedPath));
            assertEquals(expected, savedStorage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_dailyLimitMoveAndRefill_persistsNewDaysAndExerciseDetails() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        for (int index = 0; index < 6; index++) {
            logic.execute("addex 1 n/Exercise " + index + " d/m");
        }
        logic.execute("addex 1 n/Stretch s/3 r/12 w/25.5");
        logic.execute("dayex 1 7 m");
        logic.execute("dayex 1 7 tu");
        logic.execute("addex 1 n/Replacement d/m");

        Path savedPath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(savedPath);
        ReadOnlyAddressBook reloaded = savedStorage.readAddressBook().orElseThrow();
        WorkoutPlan savedPlan = reloaded.getPersonList().get(0).getWorkoutPlan();
        assertEquals(model.getAddressBook(), reloaded);
        assertEquals(7, savedPlan.getExercises().stream()
                .filter(exercise -> exercise.getDay().equals("Monday")).count());
        assertEquals(new Exercise("Replacement", 1, 1, 0, "Monday"), savedPlan.getExercises().get(6));
        assertEquals(new Exercise("Stretch", 3, 12, 25.5, "Tuesday"), savedPlan.getExercises().get(7));

        String saved = Files.readString(savedPath);
        assertCommandException("dayex 1 8 m", Messages.MESSAGE_DAILY_EXERCISE_LIMIT);
        assertEquals(saved, Files.readString(savedPath));
    }

    @Test
    public void execute_unknownRestrictionPrefix_preservesModelSavedDataAndRedo() throws Exception {
        model.addPerson(AMY);
        logic.execute("restriction 1 r/Peanut allergy");
        logic.execute("restriction 1 r/No shellfish");
        logic.execute("undo");
        AddressBook before = new AddressBook(model.getAddressBook());
        Path savedPath = temporaryFolder.resolve("addressBook.json");
        String saved = Files.readString(savedPath);

        assertThrows(ParseException.class, () -> logic.execute("restriction 1 r/No dairy x/unknown"));

        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(savedPath));
        assertEquals("Peanut allergy", model.getFilteredPersonList().getFirst()
                .getDietaryRestrictions().getValues().getFirst());
        logic.execute("redo");
        assertEquals(List.of("No shellfish"), model.getFilteredPersonList().getFirst()
                .getDietaryRestrictions().getValues());
    }

    @Test
    public void execute_deleteExercise_success() throws Exception {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        Person person = new PersonBuilder(AMY)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress, squat))).build();
        model.addPerson(person);

        Person editedPerson = new PersonBuilder(person)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(squat))).build();
        String expectedMessage = String.format(DeleteExerciseCommand.MESSAGE_DELETE_EXERCISE_SUCCESS,
                Messages.format(editedPerson));

        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(editedPerson);

        assertCommandSuccess("delex 1 1", expectedMessage, expectedModel);
    }

    @Test
    public void execute_clearPlan_success() throws Exception {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Person person = new PersonBuilder(AMY)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress))).build();
        model.addPerson(person);

        Person editedPerson = new PersonBuilder(person).withWorkoutPlan(new WorkoutPlan()).build();
        String expectedMessage = String.format(ClearPlanCommand.MESSAGE_CLEAR_PLAN_SUCCESS,
                Messages.format(editedPerson));

        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(editedPerson);

        assertCommandSuccess("clearplan 1", expectedMessage, expectedModel);
    }

    @Test
    public void execute_addExerciseInvalidFormat_throwsParseException() {
        model.addPerson(new PersonBuilder(AMY).build());
        // missing compulsory exercise name prefix
        assertParseException("addex 1 s/3",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                        AddExerciseCommand.MESSAGE_USAGE));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_failingAddExerciseSave_rollback() {
        Person person = new PersonBuilder(AMY).build();
        model.addPerson(person);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        setFailingStorage(DUMMY_IO_EXCEPTION);
        String command = "addex 1 n/Bench Press s/3 r/10 w/60";
        assertCommandFailure(command, CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()),
                expectedModel);
    }

    @Test
    public void execute_failingDeleteExerciseSave_rollback() {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        Person person = new PersonBuilder(AMY)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress, squat))).build();
        model.addPerson(person);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        setFailingStorage(DUMMY_IO_EXCEPTION);
        assertCommandFailure("delex 1 1", CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()),
                expectedModel);
    }

    @Test
    public void execute_failingClearPlanSave_rollback() {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.0);
        Person person = new PersonBuilder(AMY)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress))).build();
        model.addPerson(person);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        setFailingStorage(DUMMY_IO_EXCEPTION);
        assertCommandFailure("clearplan 1", CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()),
                expectedModel);
    }

    @Test
    public void execute_failingPaySave_rollsBackWithoutHistoryAndAllowsLaterSuccessfulPayment() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        ToggleableAddressBookStorage addressBookStorage = new ToggleableAddressBookStorage(addressBookPath);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));

        PaymentRecord firstPayment = PaymentRecord.fromText("100", "2026-01-14", Optional.empty());
        logic.execute("pay 1 a/100 d/2026-01-14");
        assertEquals(List.of(firstPayment), paymentsOfFirstPerson());

        // Undo the first payment so a redo entry exists when the next save fails.
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertTrue(paymentsOfFirstPerson().isEmpty());
        AddressBook emptySnapshot = new AddressBook(model.getAddressBook());
        String savedBefore = Files.readString(addressBookPath);

        addressBookStorage.setFailing(true);
        assertThrows(CommandException.class, PayCommand.MESSAGE_SAVE_FAILURE, () ->
                logic.execute("pay 1 a/150 d/2026-01-15"));
        assertEquals(emptySnapshot, model.getAddressBook());
        assertEquals(savedBefore, Files.readString(addressBookPath));

        // A failed save must not create an undo step or discard the existing redo entry.
        assertEquals(UndoCommand.MESSAGE_FAILURE, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        addressBookStorage.setFailing(false);
        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(firstPayment), paymentsOfFirstPerson());

        // The later successful payment contributes exactly one undo step.
        CommandResult result = logic.execute("pay 1 a/150 d/2026-01-15");
        assertEquals(String.format(PayCommand.MESSAGE_SUCCESS, AMY.getName(), "150.00",
                LocalDate.of(2026, 1, 15)), result.getFeedbackToUser());
        PaymentRecord expected = PaymentRecord.fromText("150", "2026-01-15", Optional.empty());
        ReadOnlyAddressBook saved = addressBookStorage.readAddressBook().orElseThrow();
        assertEquals(List.of(firstPayment, expected), saved.getPersonList().get(0).getPaymentRecords());

        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(firstPayment), paymentsOfFirstPerson());
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertTrue(paymentsOfFirstPerson().isEmpty());
        assertEquals(UndoCommand.MESSAGE_FAILURE, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
    }

    @Test
    public void execute_invalidPayInput_doesNotChangeModelSavedDataOrHistory() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookPath);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
        logic.execute(ListCommand.COMMAND_WORD);
        AddressBook snapshot = new AddressBook(model.getAddressBook());
        String saved = Files.readString(addressBookPath);

        assertParseException("pay 1 a/0 d/2026-10-20", PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS);
        assertParseException("pay 1 a/150 d/2026-02-30", PaymentRecord.MESSAGE_DATE_CONSTRAINTS);
        assertParseException("pay 1 a/150 d/2026-10-20 r/a\tb",
                PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS);
        assertParseException("pay 1 a/150", String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                PayCommand.MESSAGE_USAGE));
        assertParseException("pay 1 a/150 d/2026-10-20 extra",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, PayCommand.MESSAGE_USAGE));

        assertEquals(snapshot, model.getAddressBook());
        assertEquals(saved, Files.readString(addressBookPath));
        assertEquals(UndoCommand.MESSAGE_FAILURE,
                logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
    }

    @Test
    public void execute_validPayOnFilteredList_persistsOnlyDisplayedClientRecord() throws Exception {
        Person amy = new PersonBuilder(AMY).build();
        Person bob = new PersonBuilder().withName("Bob Choo").build();
        model.addPerson(amy);
        model.addPerson(bob);
        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookPath);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));

        logic.execute("find AMY");
        assertEquals(1, model.getFilteredPersonList().size());
        logic.execute("pay 1 a/150 d/2026-10-20");

        PaymentRecord expected = PaymentRecord.fromText("150", "2026-10-20", Optional.empty());
        assertEquals(List.of(expected), model.getFilteredPersonList().get(0).getPaymentRecords());
        ReadOnlyAddressBook saved = addressBookStorage.readAddressBook().orElseThrow();
        Person savedAmy = saved.getPersonList().stream()
                .filter(person -> person.getName().equals(amy.getName())).findFirst().orElseThrow();
        Person savedBob = saved.getPersonList().stream()
                .filter(person -> person.getName().equals(bob.getName())).findFirst().orElseThrow();
        assertEquals(List.of(expected), savedAmy.getPaymentRecords());
        assertTrue(savedBob.getPaymentRecords().isEmpty());
    }

    @Test
    public void execute_undoAfterDeletingPaidClient_restoresPaymentsWithRemarksAndOrder() throws Exception {
        PaymentRecord first = PaymentRecord.fromText("50", "2026-01-10", Optional.of("Initial fee"));
        PaymentRecord second = PaymentRecord.fromText("25.50", "2026-01-05", Optional.of("Late fee"));
        Person paidClient = new PersonBuilder(AMY)
                .withPaymentRecords(List.of(first, second)).build();
        model.addPerson(paidClient);

        logic.execute("delete 1");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());

        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        Person restored = model.getFilteredPersonList().get(0);
        assertEquals(paidClient, restored);
        assertEquals(List.of(first, second), restored.getPaymentRecords());
    }

    @Test
    public void execute_invalidPayParse_preservesRedoStack() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("pay 1 a/150 d/2026-10-20");
        logic.execute(UndoCommand.COMMAND_WORD);
        assertTrue(paymentsOfFirstPerson().isEmpty());

        assertParseException("pay 1 a/0 d/2026-10-20", PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS);

        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(1, paymentsOfFirstPerson().size());
    }

    @Test
    public void execute_undoRedoPayment_restoresSameRecordsWithRemarksAndOrder() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        PaymentRecord first = PaymentRecord.fromText("150", "2026-10-20", Optional.of("October fee"));
        PaymentRecord second = PaymentRecord.fromText("75.5", "2026-10-19", Optional.of("Late fee"));

        logic.execute("pay 1 a/150 d/2026-10-20 r/October fee");
        logic.execute("pay 1 a/75.5 d/2026-10-19 r/Late fee");
        assertEquals(List.of(first, second), paymentsOfFirstPerson());

        logic.execute(UndoCommand.COMMAND_WORD);
        assertEquals(List.of(first), paymentsOfFirstPerson());
        logic.execute(UndoCommand.COMMAND_WORD);
        assertTrue(paymentsOfFirstPerson().isEmpty());
        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(first), paymentsOfFirstPerson());
        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(first, second), paymentsOfFirstPerson());
    }

    @Test
    public void execute_undoRedoWithoutHistory_reportsEmptyAndKeepsData() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());

        CommandResult undo = logic.execute(UndoCommand.COMMAND_WORD);
        assertEquals(UndoCommand.MESSAGE_FAILURE, undo.getFeedbackToUser());
        CommandResult redo = logic.execute(RedoCommand.COMMAND_WORD);
        assertEquals(RedoCommand.MESSAGE_FAILURE, redo.getFeedbackToUser());

        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_payAndUndoRedo_treatsEachPaymentAsOneStep() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        PaymentRecord record = PaymentRecord.fromText("150", "2026-10-20", Optional.empty());

        logic.execute("pay 1 a/150 d/2026-10-20");
        logic.execute("pay 1 a/150 d/2026-10-20");
        assertEquals(List.of(record, record), paymentsOfFirstPerson());

        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(record), paymentsOfFirstPerson());
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertTrue(paymentsOfFirstPerson().isEmpty());

        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(record), paymentsOfFirstPerson());
        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(List.of(record, record), paymentsOfFirstPerson());
    }

    @Test
    public void execute_readOnlyOrFailedCommand_preservesRedoHistory() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("pay 1 a/150 d/2026-10-20");
        logic.execute(UndoCommand.COMMAND_WORD);

        logic.execute(ListCommand.COMMAND_WORD);
        assertCommandException("pay 9 a/150 d/2026-10-20", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandException("delete 9", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        logic.execute("find AMY");

        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(1, paymentsOfFirstPerson().size());
    }

    @Test
    public void execute_newSuccessfulChangeAfterUndo_clearsRedoHistory() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("pay 1 a/150 d/2026-10-20");
        logic.execute(UndoCommand.COMMAND_WORD);

        logic.execute("pay 1 a/75 d/2026-10-19");

        assertEquals(RedoCommand.MESSAGE_FAILURE, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(1, paymentsOfFirstPerson().size());
        assertEquals("75.00", paymentsOfFirstPerson().get(0).getFormattedAmount());
    }

    @Test
    public void execute_undoSaveFailure_restoresModelAndHistory() throws Exception {
        ToggleableAddressBookStorage addressBookStorage =
                new ToggleableAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("pay 1 a/150 d/2026-10-20");
        AddressBook saved = new AddressBook(model.getAddressBook());

        addressBookStorage.setFailing(true);
        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), ()
                -> logic.execute(UndoCommand.COMMAND_WORD));

        assertEquals(saved, model.getAddressBook());
        assertEquals(saved, addressBookStorage.readAddressBook().orElseThrow());

        addressBookStorage.setFailing(false);
        assertEquals(UndoCommand.MESSAGE_SUCCESS, logic.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertTrue(paymentsOfFirstPerson().isEmpty());
        AddressBook undone = new AddressBook(model.getAddressBook());
        addressBookStorage.setFailing(true);
        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), ()
                -> logic.execute(RedoCommand.COMMAND_WORD));
        assertEquals(undone, model.getAddressBook());
        assertEquals(undone, addressBookStorage.readAddressBook().orElseThrow());

        addressBookStorage.setFailing(false);
        assertEquals(RedoCommand.MESSAGE_SUCCESS, logic.execute(RedoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(1, paymentsOfFirstPerson().size());
    }

    @Test
    public void execute_restart_resetsHistoryButKeepsSavedData() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
        logic.execute("pay 1 a/150 d/2026-10-20");

        Logic restarted = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));

        assertEquals(UndoCommand.MESSAGE_FAILURE, restarted.execute(UndoCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(1, paymentsOfFirstPerson().size());
    }

    @Test
    public void execute_undoRedo_rejectsExtraArguments() {
        assertParseException(UndoCommand.COMMAND_WORD + " now",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE));
        assertParseException(RedoCommand.COMMAND_WORD + " now",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.MESSAGE_USAGE));
    }

    private List<PaymentRecord> paymentsOfFirstPerson() {
        return model.getFilteredPersonList().get(0).getPaymentRecords();
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void setFailingStorage(IOException e) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        // After a failed save the model must be rolled back to its previous state.
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    /** Address book storage whose save failures can be toggled without replacing the logic instance. */
    private static final class ToggleableAddressBookStorage extends JsonAddressBookStorage {
        private boolean failing;

        private ToggleableAddressBookStorage(Path filePath) {
            super(filePath);
        }

        private void setFailing(boolean failing) {
            this.failing = failing;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (failing) {
                throw DUMMY_IO_EXCEPTION;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
