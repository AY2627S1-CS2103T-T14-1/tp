package seedu.address.logic.commands;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.BodyMeasurements;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.BodyProgressEntry;
import seedu.address.model.person.Person;

/** Adds or replaces one complete dated weight/notes entry. */
public final class BodyLogCommand extends BodyMutationCommand {
    public static final String COMMAND_WORD = "bodylog";
    public static final String MESSAGE_USAGE = "bodylog INDEX d/DATE w/WEIGHT_KG [n/NOTES]";
    private final BodyProgressEntry entry;

    /** Creates a validated BodyLogCommand instance. */
    public BodyLogCommand(Index index, BodyProgressEntry entry, Clock clock) {
        super(index, clock);
        this.entry = Objects.requireNonNull(entry);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person person = resolveClient(model);
        try {
            BodyMeasurements.requireNotFuture(entry.date(), clock);
            BodyProgress current = person.getBodyProgress();
            if (current.getBaseline().isEmpty()) {
                throw new CommandException(missingBaseline(person));
            }
            Optional<BodyProgressEntry> previous = current.findEntry(entry.date());
            if (previous.filter(entry::equals).isPresent()) {
                throw new CommandException("This body progress entry already exists for " + person.getName()
                        + " on " + entry.date() + ".");
            }
            Person updated = person.withBodyProgress(current.withEntry(entry));
            model.setPerson(person, updated);
            return result((previous.isEmpty() ? "Recorded" : "Replaced") + " body progress for "
                    + person.getName() + " on " + entry.date() + ": " + entry.weightKg().toPlainString() + " kg.",
                    updated, BodyProgressViewRequest.Section.HISTORY);
        } catch (IllegalArgumentException exception) {
            throw new CommandException(exception.getMessage(), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BodyLogCommand command
                && index.equals(command.index) && entry.equals(command.entry) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), entry, clock);
    }
}
