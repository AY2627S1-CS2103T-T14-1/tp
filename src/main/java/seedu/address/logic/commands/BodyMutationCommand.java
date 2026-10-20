package seedu.address.logic.commands;

import java.time.Clock;

import seedu.address.commons.core.index.Index;

/** Identifies mutations for the body-specific save-failure mapping in LogicManager. */
public abstract class BodyMutationCommand extends BodyCommand {
    public static final String MESSAGE_SAVE_FAILURE =
            "The body progress change could not be saved. No changes were made.";

    protected BodyMutationCommand(Index index, Clock clock) {
        super(index, clock);
    }
}
