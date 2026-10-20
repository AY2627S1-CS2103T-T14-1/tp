package seedu.address.logic.commands;

import java.time.format.DateTimeFormatter;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.TrainingSession.Kind;

/** Shared scheduling rules for training sessions and client meetings. */
final class AppointmentScheduler {
    static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static final String MESSAGE_CONFLICT = "Scheduling conflict: %s already has an appointment at %s.";

    private AppointmentScheduler() {
    }

    static CommandResult add(Model model, Index index, TrainingSession session) throws CommandException {
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = model.getFilteredPersonList().get(index.getZeroBased());
        for (Person existingClient : model.getAddressBook().getPersonList()) {
            boolean occupied = existingClient.getTrainingSessions().stream()
                    .anyMatch(existing -> existing.getDateTime().equals(session.getDateTime()));
            if (occupied) {
                throw new CommandException(String.format(MESSAGE_CONFLICT, existingClient.getName(),
                        session.getDateTime().format(DISPLAY_FORMAT)));
            }
        }

        model.setPerson(client, client.withTrainingSession(session));
        String prefix = session.getKind() == Kind.MEETING ? "Recorded meeting for " : "Scheduled session for ";
        return new CommandResult(prefix + client.getName() + ": "
                + session.getDateTime().format(DISPLAY_FORMAT) + " at " + session.getLocation());
    }
}
