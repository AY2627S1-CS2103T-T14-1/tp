package seedu.address.logic.commands;

import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.TrainingSession.Kind;

/** Records an ad-hoc meeting with a client. */
public class MeetingCommand extends Command {
    public static final String COMMAND_WORD = "meeting";
    public static final String MESSAGE_USAGE = "meeting INDEX d/YYYY-MM-DD tm/HH:mm l/LOCATION\n"
            + "Example: meeting 1 d/2099-03-15 tm/14:30 l/Cafe";

    private final Index index;
    private final TrainingSession meeting;

    /** Constructs a meeting command for the displayed client. */
    public MeetingCommand(Index index, TrainingSession meeting) {
        this.index = Objects.requireNonNull(index);
        this.meeting = Objects.requireNonNull(meeting);
        if (meeting.getKind() != Kind.MEETING) {
            throw new IllegalArgumentException("Meeting command requires a meeting appointment");
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        return AppointmentScheduler.add(model, index, meeting);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof MeetingCommand command
                && index.equals(command.index) && meeting.equals(command.meeting);
    }
}
