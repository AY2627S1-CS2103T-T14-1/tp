package seedu.address.logic.commands;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Common targeting and feedback for body commands; queries never mutate client data. */
public abstract class BodyCommand extends Command {
    protected final Index index;
    protected final Clock clock;

    protected BodyCommand(Index index, Clock clock) {
        this.index = Objects.requireNonNull(index);
        this.clock = Objects.requireNonNull(clock);
    }

    protected Person resolveClient(Model model) throws CommandException {
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException("No client exists at index " + index.getOneBased() + ".");
        }
        return displayed.get(index.getZeroBased());
    }

    protected static String missingBaseline(Person person) {
        return "No body baseline recorded for " + person.getName() + ". Use bodybase first.";
    }

    protected static CommandResult result(String feedback, Person person, BodyProgressViewRequest.Section section) {
        return new CommandResult(feedback, new BodyProgressViewRequest(person, section));
    }
}
