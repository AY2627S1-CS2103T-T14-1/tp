package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists all clients matching the supplied predicate.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds clients matching the supplied criteria.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "or: [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]\n"
            + "Example: " + COMMAND_WORD + " alice bob";
    public static final String MESSAGE_FOUND = "Found %1$d client(s).";
    public static final String MESSAGE_FOUND_NONE = "Found 0 client(s). Use list to show all clients.";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int matchCount = model.getFilteredPersonList().size();
        String message = matchCount == 0 ? MESSAGE_FOUND_NONE : String.format(MESSAGE_FOUND, matchCount);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
