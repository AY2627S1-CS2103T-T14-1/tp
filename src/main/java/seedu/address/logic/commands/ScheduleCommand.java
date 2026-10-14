package seedu.address.logic.commands;

import java.time.format.DateTimeFormatter;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;

/** Adds a training session to a client. */
public class ScheduleCommand extends Command {
    public static final String COMMAND_WORD = "schedule";
    public static final String MESSAGE_USAGE = "schedule INDEX d/YYYY-MM-DD tm/HH:mm l/LOCATION\n"
            + "Example: schedule 1 d/2099-03-15 tm/14:30 l/Main gym";
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Index index;
    private final TrainingSession session;

    /** Constructs a command for the client at the displayed index. */
    public ScheduleCommand(Index index, TrainingSession session) {
        this.index = java.util.Objects.requireNonNull(index);
        this.session = java.util.Objects.requireNonNull(session);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = displayed.get(index.getZeroBased());
        Person updated = client.withTrainingSession(session);
        model.setPerson(client, updated);
        return new CommandResult("Scheduled session for " + client.getName() + ": "
                + session.getDateTime().format(DISPLAY_FORMAT) + " at " + session.getLocation());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ScheduleCommand command
                && index.equals(command.index) && session.equals(command.session);
    }
}
