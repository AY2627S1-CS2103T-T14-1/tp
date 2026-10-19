package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DietCommand;
import seedu.address.logic.commands.MembershipCommand;
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
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final Deque<AddressBook> undoStates = new ArrayDeque<>();
    private final Deque<AddressBook> redoStates = new ArrayDeque<>();

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof UndoCommand) {
            command = new UndoCommand(undoStates.peek());
        } else if (command instanceof RedoCommand) {
            command = new RedoCommand(redoStates.peek());
        }
        // Snapshot the address book before execution so a failed save can restore
        // the previous model state instead of leaving in-memory changes behind.
        AddressBook backup = new AddressBook(model.getAddressBook());
        commandResult = command.execute(model);

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            model.setAddressBook(backup);
            if (command instanceof DietCommand) {
                throw new CommandException(DietCommand.MESSAGE_SAVE_FAILURE, ioe);
            }
            if (command instanceof MembershipCommand) {
                throw new CommandException(MembershipCommand.MESSAGE_SAVE_FAILURE, ioe);
            }
            if (ioe instanceof AccessDeniedException) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        if (command instanceof UndoCommand) {
            undoStates.pop();
            redoStates.push(backup);
        } else if (command instanceof RedoCommand) {
            redoStates.pop();
            undoStates.push(backup);
        } else if (!backup.equals(model.getAddressBook())) {
            undoStates.push(backup);
            redoStates.clear();
        }
        return commandResult;
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
