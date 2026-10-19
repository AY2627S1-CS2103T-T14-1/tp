package seedu.address.logic.commands;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;

/** Restores the address book after the most recent undo. */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_USAGE = "redo: Restores the last undone change in this app session.\n"
            + "Example: redo";
    public static final String MESSAGE_NO_HISTORY = "No changes to redo.";
    public static final String MESSAGE_SUCCESS = "Redid the last change.";

    private final ReadOnlyAddressBook next;

    /** Creates a parsed redo command; LogicManager supplies its snapshot at execution time. */
    public RedoCommand() {
        this(null);
    }

    /** Creates a redo command targeting a later saved address-book snapshot. */
    public RedoCommand(ReadOnlyAddressBook next) {
        this.next = next;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Objects.requireNonNull(model);
        if (next == null) {
            throw new CommandException(MESSAGE_NO_HISTORY);
        }
        model.setAddressBook(next);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
