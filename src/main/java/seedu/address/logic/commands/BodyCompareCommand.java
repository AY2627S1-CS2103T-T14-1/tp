package seedu.address.logic.commands;

import java.time.Clock;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.Person;

/** Requests the selected client's body-progress comparison without changing domain data. */
public final class BodyCompareCommand extends BodyCommand {
    public static final String COMMAND_WORD = "bodycompare";
    public static final String MESSAGE_USAGE = "bodycompare INDEX";

    /** Creates a validated BodyCompareCommand instance. */
    public BodyCompareCommand(Index index, Clock clock) {
        super(index, clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person person = resolveClient(model);
        BodyProgress progress = person.getBodyProgress();
        String feedback = progress.getBaseline().isEmpty() ? missingBaseline(person)
                : progress.getEntries().isEmpty()
                        ? "No body progress entries recorded for " + person.getName()
                                + ". Use bodylog before comparing."
                        : "Showing body progress comparison for " + person.getName() + ".";
        return result(feedback, person, BodyProgressViewRequest.Section.COMPARISON);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BodyCompareCommand command
                && index.equals(command.index) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), clock);
    }
}
