package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Restores the address book to the state before the most recent saved change.
 * Session history is owned by {@code LogicManager}, which executes this marker command.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Reverses the most recent saved data change.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Undid the last change.";
    public static final String MESSAGE_FAILURE = "No changes to undo.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(MESSAGE_FAILURE);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UndoCommand;
    }

    @Override
    public int hashCode() {
        return UndoCommand.class.hashCode();
    }
}
