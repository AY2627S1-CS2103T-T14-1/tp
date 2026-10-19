package seedu.address.logic.commands;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/** Displays every logged workout for a client, newest first. */
public class HistoryCommand extends Command {

    public static final String COMMAND_WORD = "history";
    public static final String MESSAGE_USAGE = "history CLIENT_INDEX\nExample: history 1";

    private static final Comparator<WorkoutSession> NEWEST_FIRST =
            Comparator.comparing(WorkoutSession::getDate)
                    .thenComparing(session -> session.getTime().orElse(LocalTime.MIN))
                    .reversed();

    private final Index clientIndex;

    public HistoryCommand(Index clientIndex) {
        this.clientIndex = Objects.requireNonNull(clientIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Objects.requireNonNull(model);
        List<Person> displayed = model.getFilteredPersonList();
        if (clientIndex.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = displayed.get(clientIndex.getZeroBased());
        List<WorkoutSession> sessions = client.getWorkoutSessions().stream().sorted(NEWEST_FIRST).toList();
        if (sessions.isEmpty()) {
            return new CommandResult("No workout sessions recorded for " + client.getName() + ".");
        }

        StringBuilder result = new StringBuilder("Workout history for " + client.getName() + ":");
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSession session = sessions.get(i);
            result.append("\n").append(i + 1).append(". ").append(session.getDate())
                    .append(session.getTime().map(time -> " at " + time).orElse(" (time not specified)"));
            for (WorkoutLogEntry entry : session.getEntries()) {
                result.append("\n   ").append(entry.getName()).append(" | ")
                        .append(entry.getWeightKg().toPlainString()).append(" kg | ")
                        .append(entry.getSets()).append(" sets | ")
                        .append(entry.getReps()).append(" reps");
            }
        }
        return new CommandResult(result.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof HistoryCommand command
                && clientIndex.equals(command.clientIndex);
    }

    @Override
    public int hashCode() {
        return clientIndex.hashCode();
    }
}
