package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Restores the address book state that the most recent undo replaced.
 * Session history is owned by {@code LogicManager}, which executes this marker command.
 */
public class RedoCommand extends Command {

    public static final String COMMAND_WORD = "redo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the most recently undone saved data change.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Redid the last change.";
    public static final String MESSAGE_FAILURE = "No changes to redo.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(MESSAGE_FAILURE);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RedoCommand;
    }

    @Override
    public int hashCode() {
        return RedoCommand.class.hashCode();
    }
}
