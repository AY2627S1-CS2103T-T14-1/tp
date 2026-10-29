package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.BodyCommand;
import seedu.address.logic.commands.BodyMutationCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DietCommand;
import seedu.address.logic.commands.MembershipCommand;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final int MAX_UNDO_HISTORY_SIZE = 50;
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final Deque<AddressBook> undoStack = new ArrayDeque<>();
    private final Deque<AddressBook> redoStack = new ArrayDeque<>();

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, Clock.systemDefaultZone());
    }

    /** Creates logic with an injectable clock for body measurement execution checks. */
    public LogicManager(Model model, Storage storage, Clock clock) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser(clock);
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof UndoCommand) {
            return executeUndo();
        }
        if (command instanceof RedoCommand) {
            return executeRedo();
        }

        // Snapshot the address book before execution so a failed save can restore
        // the previous model state instead of leaving in-memory changes behind.
        AddressBook backup = new AddressBook(model.getAddressBook());
        // Commands such as edit show every client before saving; a failed save must also restore the filter,
        // or the displayed indexes would refer to different clients.
        Predicate<Person> filterBackup = model.getFilteredPersonListPredicate();
        CommandResult commandResult = command.execute(model);

        // Body history/comparison only request a view; they must also work when storage is read-only.
        if (command instanceof BodyCommand && !(command instanceof BodyMutationCommand)) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            model.setAddressBook(backup);
            model.updateFilteredPersonList(filterBackup);
            throw saveException(command, ioe);
        }

        // Only effective changes become one complete-address-book undo step.
        if (!backup.equals(model.getAddressBook())) {
            pushUndoState(backup);
            redoStack.clear();
        }

        return commandResult;
    }

    private CommandResult executeUndo() throws CommandException {
        if (undoStack.isEmpty()) {
            return new CommandResult(UndoCommand.MESSAGE_FAILURE);
        }

        AddressBook restored = undoStack.peek();
        AddressBook current = new AddressBook(model.getAddressBook());
        model.setAddressBook(restored);
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            model.setAddressBook(current);
            throw saveException(new UndoCommand(), ioe);
        }

        undoStack.pop();
        redoStack.push(current);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(UndoCommand.MESSAGE_SUCCESS);
    }

    private CommandResult executeRedo() throws CommandException {
        if (redoStack.isEmpty()) {
            return new CommandResult(RedoCommand.MESSAGE_FAILURE);
        }

        AddressBook restored = redoStack.peek();
        AddressBook current = new AddressBook(model.getAddressBook());
        model.setAddressBook(restored);
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            model.setAddressBook(current);
            throw saveException(new RedoCommand(), ioe);
        }

        redoStack.pop();
        pushUndoState(current);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(RedoCommand.MESSAGE_SUCCESS);
    }

    private void pushUndoState(AddressBook state) {
        undoStack.push(state);
        if (undoStack.size() > MAX_UNDO_HISTORY_SIZE) {
            undoStack.removeLast();
        }
    }

    private CommandException saveException(Command command, IOException ioe) {
        if (command instanceof BodyMutationCommand) {
            return new CommandException(BodyMutationCommand.MESSAGE_SAVE_FAILURE, ioe);
        }
        if (command instanceof DietCommand) {
            return new CommandException(DietCommand.MESSAGE_SAVE_FAILURE, ioe);
        }
        if (command instanceof MembershipCommand) {
            return new CommandException(MembershipCommand.MESSAGE_SAVE_FAILURE, ioe);
        }
        if (command instanceof PayCommand) {
            return new CommandException(PayCommand.MESSAGE_SAVE_FAILURE, ioe);
        }
        if (ioe instanceof AccessDeniedException) {
            return new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
        return new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
