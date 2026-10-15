package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;

/**
 * Creates or fully replaces a client's current diet plan.
 */
public class DietCommand extends Command {
    public static final String COMMAND_WORD = "diet";
    public static final String MESSAGE_USAGE = "diet INDEX t/TARGET [n/NOTES]";
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format. Use: " + MESSAGE_USAGE;
    public static final String MESSAGE_INDEX_REQUIRED = "Client index is required. Use: " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_INDEX =
            "The client index must be a positive whole number shown in the client list.";
    public static final String MESSAGE_TARGET_REQUIRED = "Diet target is required. Use: " + MESSAGE_USAGE;
    public static final String MESSAGE_REPEATED_PREFIX = "The t/ and n/ prefixes may each be used only once.";
    public static final String MESSAGE_CREATED = "Created diet plan for %s.";
    public static final String MESSAGE_REPLACED = "Replaced diet plan for %s.";
    public static final String MESSAGE_DUPLICATE = "This is already the current diet plan for %s.";
    public static final String MESSAGE_OUT_OF_RANGE = "No client exists at index %d.";
    public static final String MESSAGE_SAVE_FAILURE =
            "The diet plan could not be saved. The previous plan remains unchanged.";

    private final Index index;
    private final DietPlan dietPlan;

    /**
     * Creates a command targeting the displayed client index.
     */
    public DietCommand(Index index, DietPlan dietPlan) {
        requireAllNonNull(index, dietPlan);
        this.index = index;
        this.dietPlan = dietPlan;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(String.format(MESSAGE_OUT_OF_RANGE, index.getOneBased()));
        }
        Person client = displayedClients.get(index.getZeroBased());
        Optional<DietPlan> previousPlan = client.getDietPlan();
        if (previousPlan.filter(plan -> plan.isSameDietPlan(dietPlan)).isPresent()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE, client.getName()));
        }
        Person updated = new Person(
                client.getName(), client.getPhone(), client.getEmail(), client.getAddress(),
                client.getRemark(), client.getTags(), client.getWorkoutPlan(),
                Optional.of(dietPlan), client.getGoal().orElse(null), client.getTrainingSessions(),
                client.getWorkoutSessions());

        model.setPerson(client, updated);
        // Keep the displayed list's filter and indexes stable; only diet data changed.
        String message = previousPlan.isPresent() ? MESSAGE_REPLACED : MESSAGE_CREATED;
        return new CommandResult(String.format(message, client.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DietCommand otherCommand
                && index.equals(otherCommand.index) && dietPlan.equals(otherCommand.dietPlan);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("dietPlan", dietPlan).toString();
    }
}
