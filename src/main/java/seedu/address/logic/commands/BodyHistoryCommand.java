package seedu.address.logic.commands;

import java.time.Clock;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.Person;

/** Requests the selected client's body-progress history without changing domain data. */
public final class BodyHistoryCommand extends BodyCommand {
    public static final String COMMAND_WORD = "bodyhistory";
    public static final String MESSAGE_USAGE = "bodyhistory INDEX";

    /** Creates a validated BodyHistoryCommand instance. */
    public BodyHistoryCommand(Index index, Clock clock) {
        super(index, clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person person = resolveClient(model);
        BodyProgress progress = person.getBodyProgress();
        String feedback = progress.getBaseline().isEmpty() ? missingBaseline(person)
                : "Showing body progress history for " + person.getName() + ": " + progress.getEntries().size()
                        + " entries.";
        return result(feedback, person, BodyProgressViewRequest.Section.HISTORY);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BodyHistoryCommand command
                && index.equals(command.index) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), clock);
    }
}
