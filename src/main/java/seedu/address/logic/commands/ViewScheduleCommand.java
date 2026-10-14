package seedu.address.logic.commands;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;

/** Lists a client's saved training sessions. */
public class ViewScheduleCommand extends Command {
    public static final String COMMAND_WORD = "viewschedule";
    public static final String MESSAGE_USAGE = "viewschedule INDEX\nExample: viewschedule 1";
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Index index;

    public ViewScheduleCommand(Index index) {
        this.index = java.util.Objects.requireNonNull(index);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = displayed.get(index.getZeroBased());
        List<TrainingSession> sessions = client.getTrainingSessions().stream()
                .sorted(Comparator.comparing(TrainingSession::getDateTime)).toList();
        if (sessions.isEmpty()) {
            return new CommandResult("No training sessions saved for " + client.getName() + ".");
        }
        StringBuilder result = new StringBuilder("Training sessions for " + client.getName() + ":");
        for (int i = 0; i < sessions.size(); i++) {
            TrainingSession session = sessions.get(i);
            result.append("\n").append(i + 1).append(". ")
                    .append(session.getDateTime().format(DISPLAY_FORMAT))
                    .append(" at ").append(session.getLocation());
        }
        return new CommandResult(result.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ViewScheduleCommand command && index.equals(command.index);
    }
}
