package seedu.address.logic.commands;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.DietaryRestrictions;
import seedu.address.model.person.Person;

/** Replaces or clears a client's current dietary restrictions. */
public class DietaryRestrictionsCommand extends Command {
    public static final String COMMAND_WORD = "restriction";
    public static final String MESSAGE_USAGE = "restriction INDEX r/RESTRICTION [r/RESTRICTION]...\n"
            + "Example: restriction 1 r/Peanut allergy r/Halal meals";
    public static final String MESSAGE_RECORDED = "Recorded dietary restrictions for %s.";
    public static final String MESSAGE_CLEARED = "Cleared dietary restrictions for %s.";
    public static final String MESSAGE_NO_CHANGES = "No changes made: dietary restrictions are already current.";

    private final Index index;
    private final DietaryRestrictions restrictions;

    /** Creates a command with replacement restrictions; an empty list clears them. */
    public DietaryRestrictionsCommand(Index index, DietaryRestrictions restrictions) {
        this.index = Objects.requireNonNull(index);
        this.restrictions = Objects.requireNonNull(restrictions);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = displayed.get(index.getZeroBased());
        if (client.getDietaryRestrictions().equals(restrictions)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }
        model.setPerson(client, client.withDietaryRestrictions(restrictions));
        String message = restrictions.isEmpty() ? MESSAGE_CLEARED : MESSAGE_RECORDED;
        return new CommandResult(String.format(message, client.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DietaryRestrictionsCommand command
                && index.equals(command.index) && restrictions.equals(command.restrictions);
    }
}
