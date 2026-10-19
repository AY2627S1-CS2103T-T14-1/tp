package seedu.address.logic.commands;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;

/** Restores the address book before the most recent saved change. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = "undo: Reverses the last saved change in this app session.\n"
            + "Example: undo";
    public static final String MESSAGE_NO_HISTORY = "No changes to undo.";
    public static final String MESSAGE_SUCCESS = "Undid the last change.";

    private final ReadOnlyAddressBook previous;

    /** Creates a parsed undo command; LogicManager supplies its snapshot at execution time. */
    public UndoCommand() {
        this(null);
    }

    /** Creates an undo command targeting a prior saved address-book snapshot. */
    public UndoCommand(ReadOnlyAddressBook previous) {
        this.previous = previous;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Objects.requireNonNull(model);
        if (previous == null) {
            throw new CommandException(MESSAGE_NO_HISTORY);
        }
        model.setAddressBook(previous);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
