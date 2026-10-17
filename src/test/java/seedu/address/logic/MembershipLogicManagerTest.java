package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.MembershipCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MembershipLogicManagerTest {

    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final LocalDate FUTURE_EXPIRY = LocalDate.of(2099, 12, 30);

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_addmemHappyPath_wiresParserCommandModelStorage() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        CommandResult result = logic.execute("addmem n/Gold p/150 s/30");
        assertEquals("Added membership plan: Gold; Price: 150.00; Sessions: 30.",
                result.getFeedbackToUser());
        assertEquals(1, model.getMembershipPlanList().size());
        JsonAddressBookStorage saved =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_membershipHappyPaths_wireEndToEnd() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        model.addPerson(new PersonBuilder().withName("Bob Choo").build());
        logic.execute("addmem n/Gold p/150 s/30");
        logic.execute("addmem n/Silver p/80 s/12");

        CommandResult view = logic.execute("view mem");
        assertEquals(view.getFeedbackToUser(), logic.execute("view membership").getFeedbackToUser());

        CommandResult assign = logic.execute("amem Gold 1 e/" + FUTURE_EXPIRY);
        assertTrueContains(assign.getFeedbackToUser(), "Assigned Gold to");
        assertTrueContains(assign.getFeedbackToUser(), "Expiry: " + FUTURE_EXPIRY);

        CommandResult assignNoExpiry = logic.execute("amem Gold 2");
        assertTrueContains(assignNoExpiry.getFeedbackToUser(), "Assigned Gold to");

        CommandResult increase = logic.execute("imem 1 3");
        assertTrueContains(increase.getFeedbackToUser(), "Sessions remaining: 33.");
        CommandResult decrease = logic.execute("dmem 2 2");
        assertTrueContains(decrease.getFeedbackToUser(), "Sessions remaining: 28.");

        String inUseMessage = "Membership plan Gold is in use and cannot be deleted. "
                + "Remove its assignments with rmem first.";
        assertThrows(CommandException.class, inUseMessage, () -> logic.execute("delmem n/Gold"));

        CommandResult remove = logic.execute("rmem 1");
        assertTrueContains(remove.getFeedbackToUser(), "remaining sessions lost.");
        logic.execute("rmem 2");
        CommandResult delete = logic.execute("delmem n/Gold");
        assertEquals("Deleted membership plan: Gold.", delete.getFeedbackToUser());
    }

    @Test
    public void execute_clear_persistsMembershipPlans() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addmem n/Gold p/150 s/30");
        logic.execute("clear");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(1, model.getMembershipPlanList().size());
        JsonAddressBookStorage saved =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook reloaded = saved.readAddressBook().orElseThrow();
        assertTrue(reloaded.getPersonList().isEmpty());
        assertEquals(1, reloaded.getMembershipPlanList().size());
    }

    @Test
    public void execute_failingAddmemSave_rollbackWithMessage() {
        model.addPerson(new PersonBuilder(AMY).build());
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("addmem n/Gold p/150 s/30");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_failingAmemSave_rollbackWithMessage() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addmem n/Gold p/150 s/30");
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("amem Gold 1");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_failingImemSave_rollbackWithMessage() throws Exception {
        prepareAssignedClient(OptionalExpiry.EMPTY);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("imem 1 3");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_failingDmemSave_rollbackWithMessage() throws Exception {
        prepareAssignedClient(OptionalExpiry.EMPTY);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("dmem 1 2");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_failingRmemSave_rollbackWithMessage() throws Exception {
        prepareAssignedClient(OptionalExpiry.EMPTY);
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("rmem 1");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
        assertEquals(1, model.getFilteredPersonList().get(0)
                .getMembershipAssignment().orElseThrow().getRemainingSessions()
                == 30 ? 1 : 0);
    }

    @Test
    public void execute_failingDelmemSave_rollbackWithMessage() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addmem n/Gold p/150 s/30");
        logic.execute("addmem n/Silver p/80 s/12");
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("delmem n/Silver");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
        assertEquals(2, model.getMembershipPlanList().size());
    }

    @Test
    public void execute_failingClearmemSave_rollbackWithMessage() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addmem n/Gold p/150 s/30");
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("clearmem");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(expectedModel, model);
        assertEquals(1, model.getMembershipPlanList().size());
    }

    @Test
    public void execute_clearmemHappyPath_wiresEndToEnd() throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addmem n/Gold p/150 s/30");
        CommandResult result = logic.execute("clearmem");
        assertEquals("Cleared all membership plans.", result.getFeedbackToUser());
        assertTrue(model.getMembershipPlanList().isEmpty());
        assertEquals("No membership plans available.",
                logic.execute("view mem").getFeedbackToUser());
    }

    @Test
    public void execute_failingSave_restoresBothCatalogAndAssignments() throws Exception {
        prepareAssignedClient(OptionalExpiry.WITH_EXPIRY);
        AddressBook before = new AddressBook(model.getAddressBook());
        setFailingStorage();
        assertThrows(CommandException.class, MembershipCommand.MESSAGE_SAVE_FAILURE, () -> {
            try {
                logic.execute("imem 1 5");
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        });
        assertEquals(before, model.getAddressBook());
        assertEquals(before.getMembershipPlanList(), model.getMembershipPlanList());
        assertEquals(before.getPersonList().get(0).getMembershipAssignment(),
                model.getAddressBook().getPersonList().get(0).getMembershipAssignment());
    }

    private void prepareAssignedClient(OptionalExpiry expiry) throws Exception {
        model.addPerson(new PersonBuilder(AMY).build());
        logic.execute("addmem n/Gold p/150 s/30");
        Person client = model.getFilteredPersonList().get(0);
        MembershipPlan gold = model.findMembershipPlan(new MembershipName("Gold")).orElseThrow();
        java.util.Optional<LocalDate> date = expiry == OptionalExpiry.WITH_EXPIRY
                ? java.util.Optional.of(FUTURE_EXPIRY) : java.util.Optional.empty();
        model.setPerson(client, client.withMembershipAssignment(
                java.util.Optional.of(new MembershipAssignment(gold, date))));
    }

    private void setFailingStorage() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")) {
                    @Override
                    public void saveAddressBook(
                            seedu.address.model.ReadOnlyAddressBook addressBook) throws IOException {
                        throw DUMMY_IO_EXCEPTION;
                    }
                };
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
    }

    private static void assertTrueContains(String actual, String fragment) {
        if (!actual.contains(fragment)) {
            throw new AssertionError("Expected <" + actual + "> to contain <" + fragment + ">");
        }
    }

    private enum OptionalExpiry {
        EMPTY, WITH_EXPIRY
    }
}
