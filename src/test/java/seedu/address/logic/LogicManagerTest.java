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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.logic.commands.ClearPlanCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteExerciseCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
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
}
