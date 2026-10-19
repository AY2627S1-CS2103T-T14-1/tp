package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.TrainingSession;

/** Adds a training session to a client. */
public class ScheduleCommand extends Command {
    public static final String COMMAND_WORD = "schedule";
    public static final String MESSAGE_USAGE = "schedule INDEX d/YYYY-MM-DD tm/HH:mm l/LOCATION\n"
            + "Example: schedule 1 d/2099-03-15 tm/14:30 l/Main gym";

    private final Index index;
    private final TrainingSession session;

    /** Constructs a command for the client at the displayed index. */
    public ScheduleCommand(Index index, TrainingSession session) {
        this.index = java.util.Objects.requireNonNull(index);
        this.session = java.util.Objects.requireNonNull(session);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        return AppointmentScheduler.add(model, index, session);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ScheduleCommand command
                && index.equals(command.index) && session.equals(command.session);
    }
}
