package seedu.address.logic.commands;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.BodyMeasurements;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.Person;

/** Permanently removes one progress entry without removing the baseline. */
public final class BodyDeleteCommand extends BodyMutationCommand {
    public static final String COMMAND_WORD = "bodydelete";
    public static final String MESSAGE_USAGE = "bodydelete INDEX d/DATE";
    private final LocalDate date;

    /** Creates a validated BodyDeleteCommand instance. */
    public BodyDeleteCommand(Index index, LocalDate date, Clock clock) {
        super(index, clock);
        this.date = BodyMeasurements.validateDate(date);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person person = resolveClient(model);
        try {
            BodyMeasurements.requireNotFuture(date, clock);
        } catch (IllegalArgumentException exception) {
            throw new CommandException(exception.getMessage(), exception);
        }
        BodyProgress current = person.getBodyProgress();
        if (current.getBaseline().isEmpty()) {
            throw new CommandException(missingBaseline(person));
        }
        if (current.getBaseline().orElseThrow().date().equals(date)) {
            throw new CommandException("The baseline cannot be deleted with bodydelete. Use bodybase to correct it.");
        }
        if (current.findEntry(date).isEmpty()) {
            throw new CommandException("No body progress entry exists for " + person.getName() + " on " + date + ".");
        }
        Person updated = person.withBodyProgress(current.withoutEntry(date));
        model.setPerson(person, updated);
        return result("Deleted body progress for " + person.getName() + " on " + date + ".",
                updated, BodyProgressViewRequest.Section.HISTORY);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BodyDeleteCommand command
                && index.equals(command.index) && date.equals(command.date) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), date, clock);
    }
}
