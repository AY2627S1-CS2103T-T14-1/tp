package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutSession;

/**
 * Records a completed workout session for an existing client.
 */
public class LogCommand extends Command {

    public static final String COMMAND_WORD = "log";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records a completed workout session for the client "
            + "identified by the index in the displayed client list.\n"
            + "Parameters: INDEX d/DATE [t/TIME] ex/EXERCISE|WEIGHT|SETS|REPS "
            + "[ex/EXERCISE|WEIGHT|SETS|REPS]...\n"
            + "DATE must use yyyy-MM-dd and must not be in the future. "
            + "TIME is optional and must use HH:mm. "
            + "Provide 1 to 30 exercise entries.\n"
            + "Example: log 1 d/2020-01-15 t/18:00 "
            + "ex/Back squat|60|3|10 ex/Bench press|40|3|8";

    public static final String MESSAGE_SUCCESS =
            "Logged workout for %1$s on %2$s: %3$d %4$s.";

    public static final String MESSAGE_DUPLICATE_SESSION =
            "A workout session already exists for %1$s on %2$s.";
    public static final String MESSAGE_DUPLICATE_SESSION_WITHOUT_TIME =
            "An unspecified-time workout session already exists for %1$s on %2$s. "
            + "Add t/TIME to distinguish another session.";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    private final Index index;
    private final WorkoutSession session;

    /**
     * Constructs a command with a displayed client index and a validated session.
     */
    public LogCommand(Index index, WorkoutSession session) {
        requireAllNonNull(index, session);
        this.index = index;
        this.session = session;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> displayedClients = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person client = displayedClients.get(index.getZeroBased());
        String date = session.getDate().format(DATE_FORMAT);
        String dateTime = date + session.getTime().map(time -> " at " + time.format(TIME_FORMAT)).orElse("");

        boolean hasDuplicateSession = client.getWorkoutSessions().stream()
                .anyMatch(existing -> existing.hasSameDateTime(session));
        if (hasDuplicateSession) {
            String message = session.getTime().isPresent()
                    ? String.format(Locale.ENGLISH, MESSAGE_DUPLICATE_SESSION, client.getName(), dateTime)
                    : String.format(Locale.ENGLISH, MESSAGE_DUPLICATE_SESSION_WITHOUT_TIME, client.getName(), date);
            throw new CommandException(message);
        }

        List<WorkoutSession> updatedSessions = new ArrayList<>(client.getWorkoutSessions());
        updatedSessions.add(session);

        Person updatedClient = new Person(
                client.getName(), client.getPhone(), client.getEmail(), client.getAddress(),
                client.getRemark(), client.getTags(), client.getWorkoutPlan(), client.getDietPlan(),
                client.getGoal().orElse(null), client.getTrainingSessions(), updatedSessions,
                client.getDietaryRestrictions(), client.getHealthProfile().orElse(null),
                client.getMembershipAssignment(), client.getBodyProgress(), client.getPaymentRecords());

        model.setPerson(client, updatedClient);

        int entryCount = session.getEntries().size();
        return new CommandResult(String.format(Locale.ENGLISH, MESSAGE_SUCCESS, client.getName(), dateTime,
                entryCount, entryCount == 1 ? "exercise" : "exercises"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof LogCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index)
                && session.equals(otherCommand.session);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("session", session)
                .toString();
    }
}
