package seedu.address.logic.commands;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutSession;

/**
 * Replaces one completed workout session selected by its displayed history number.
 */
public class EditLogCommand extends Command {

    public static final String COMMAND_WORD = "editlog";
    public static final String MESSAGE_USAGE = "editlog: Replaces a logged workout session.\n"
            + "Parameters: CLIENT_INDEX SESSION_INDEX d/DATE [t/TIME] "
            + "ex/EXERCISE|WEIGHT|SETS|REPS [ex/EXERCISE|WEIGHT|SETS|REPS]...\n"
            + "Example: editlog 1 2 d/2020-01-15 t/18:00 ex/Back squat|60|3|10";
    public static final String MESSAGE_INVALID_SESSION_INDEX =
            "The workout session index provided is invalid.";
    public static final String MESSAGE_DUPLICATE_SESSION = LogCommand.MESSAGE_DUPLICATE_SESSION;
    public static final String MESSAGE_NO_CHANGE = "The workout session is unchanged.";
    public static final String MESSAGE_SUCCESS = "Edited workout session %1$d for %2$s.";

    private static final Comparator<WorkoutSession> NEWEST_FIRST =
            Comparator.comparing(WorkoutSession::getDate)
                    .thenComparing(session -> session.getTime().orElse(LocalTime.MIN))
                    .reversed();

    private final Index clientIndex;
    private final Index sessionIndex;
    private final WorkoutSession replacement;

    public EditLogCommand(Index clientIndex, Index sessionIndex, WorkoutSession replacement) {
        this.clientIndex = Objects.requireNonNull(clientIndex);
        this.sessionIndex = Objects.requireNonNull(sessionIndex);
        this.replacement = Objects.requireNonNull(replacement);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Objects.requireNonNull(model);
        List<Person> displayed = model.getFilteredPersonList();
        if (clientIndex.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = displayed.get(clientIndex.getZeroBased());
        List<WorkoutSession> history = client.getWorkoutSessions().stream().sorted(NEWEST_FIRST).toList();
        if (sessionIndex.getZeroBased() >= history.size()) {
            throw new CommandException(MESSAGE_INVALID_SESSION_INDEX);
        }

        WorkoutSession previous = history.get(sessionIndex.getZeroBased());
        if (previous.equals(replacement)) {
            throw new CommandException(MESSAGE_NO_CHANGE);
        }
        if (client.getWorkoutSessions().stream()
                .anyMatch(session -> session != previous && session.hasSameDateTime(replacement))) {
            throw new CommandException(MESSAGE_DUPLICATE_SESSION);
        }

        List<WorkoutSession> updatedSessions = new ArrayList<>(client.getWorkoutSessions());
        updatedSessions.set(updatedSessions.indexOf(previous), replacement);
        Person updated = new Person(
                client.getName(), client.getPhone(), client.getEmail(), client.getAddress(),
                client.getRemark(), client.getTags(), client.getWorkoutPlan(), client.getDietPlan(),
                client.getGoal().orElse(null), client.getTrainingSessions(), updatedSessions,
                client.getDietaryRestrictions(), client.getHealthProfile().orElse(null),
                client.getMembershipAssignment());
        model.setPerson(client, updated);
        return new CommandResult(String.format(MESSAGE_SUCCESS, sessionIndex.getOneBased(), client.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditLogCommand command
                && clientIndex.equals(command.clientIndex)
                && sessionIndex.equals(command.sessionIndex)
                && replacement.equals(command.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIndex, sessionIndex, replacement);
    }
}
