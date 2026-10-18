package seedu.address.logic.commands;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.HealthProfile;
import seedu.address.model.person.Person;

/** Records or clears a client's current health considerations. */
public class HealthCommand extends Command {
    public static final String COMMAND_WORD = "health";
    public static final String MESSAGE_USAGE = "health INDEX h/NOTE\nExample: health 1 h/Avoid jumping exercises";
    public static final String MESSAGE_RECORDED = "Recorded health considerations for %s.";
    public static final String MESSAGE_CLEARED = "Cleared health considerations for %s.";
    public static final String MESSAGE_NO_CHANGES = "No changes made: health considerations are already current.";

    private final Index index;
    private final HealthProfile healthProfile;

    /** Creates a command with a replacement profile, or {@code null} to clear it. */
    public HealthCommand(Index index, HealthProfile healthProfile) {
        this.index = Objects.requireNonNull(index);
        this.healthProfile = healthProfile;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person client = displayed.get(index.getZeroBased());
        if (Objects.equals(client.getHealthProfile().orElse(null), healthProfile)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }
        model.setPerson(client, client.withHealthProfile(healthProfile));
        String message = healthProfile == null ? MESSAGE_CLEARED : MESSAGE_RECORDED;
        return new CommandResult(String.format(message, client.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof HealthCommand command
                && index.equals(command.index) && Objects.equals(healthProfile, command.healthProfile);
    }
}
