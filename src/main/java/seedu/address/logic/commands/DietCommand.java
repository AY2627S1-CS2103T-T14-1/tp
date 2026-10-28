package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Map;
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
    public static final String MESSAGE_STRUCTURED_USAGE =
            "diet INDEX cal/KCAL [pro/G] [carb/G] [fat/G] [n/NOTES]";
    public static final String MESSAGE_INVALID_CALORIES = "Calorie target must be a whole number (cal/2000).";
    public static final String MESSAGE_INVALID_MACROS = "Macros must be whole numbers of grams.";
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
    private final Map<String, String> structuredValues;

    /**
     * Creates a command targeting the displayed client index.
     */
    public DietCommand(Index index, DietPlan dietPlan) {
        requireAllNonNull(index, dietPlan);
        this.index = index;
        this.dietPlan = dietPlan;
        structuredValues = null;
    }

    private DietCommand(Index index, Map<String, String> structuredValues) {
        requireAllNonNull(index, structuredValues);
        this.index = index;
        this.dietPlan = null;
        this.structuredValues = Map.copyOf(structuredValues);
    }

    /** Creates a structured command whose values are validated after the client index. */
    public static DietCommand structured(Index index, Map<String, String> structuredValues) {
        return new DietCommand(index, structuredValues);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(structuredValues == null
                    ? String.format(MESSAGE_OUT_OF_RANGE, index.getOneBased())
                    : "The client index provided is invalid.");
        }
        Person client = displayedClients.get(index.getZeroBased());
        Optional<DietPlan> previousPlan = client.getDietPlan();
        DietPlan resolvedPlan = structuredValues == null ? dietPlan : resolveStructuredPlan(previousPlan);
        if (previousPlan.filter(plan -> plan.isSameDietPlan(resolvedPlan)).isPresent()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE, client.getName()));
        }
        Person updated = new Person(
                client.getName(), client.getPhone(), client.getEmail(), client.getAddress(),
                client.getRemark(), client.getTags(), client.getWorkoutPlan(),
                Optional.of(resolvedPlan), client.getGoal().orElse(null), client.getTrainingSessions(),
                client.getWorkoutSessions(), client.getDietaryRestrictions(), client.getHealthProfile().orElse(null),
                client.getMembershipAssignment(), client.getBodyProgress(), client.getPaymentRecords());

        model.setPerson(client, updated);
        // Keep the displayed list's filter and indexes stable; only diet data changed.
        String message = previousPlan.isPresent() ? MESSAGE_REPLACED : MESSAGE_CREATED;
        return new CommandResult(String.format(message, client.getName()));
    }

    private DietPlan resolveStructuredPlan(Optional<DietPlan> previousPlan) throws CommandException {
        int calories = parseWholeNumber(structuredValues.get("cal"), true);
        Optional<Integer> protein = parseMacro("pro");
        Optional<Integer> carbs = parseMacro("carb");
        Optional<Integer> fat = parseMacro("fat");
        Optional<String> notes = structuredValues.containsKey("n")
                ? Optional.of(structuredValues.get("n")).filter(value -> !value.isBlank())
                : previousPlan.flatMap(DietPlan::getNotes);
        if (notes.isPresent() && !DietPlan.isValidNotes(notes.orElseThrow())) {
            throw new CommandException(DietPlan.MESSAGE_CONSTRAINTS_NOTES);
        }
        return DietPlan.structured(calories, protein, carbs, fat, notes);
    }

    private Optional<Integer> parseMacro(String prefix) throws CommandException {
        String value = structuredValues.get(prefix);
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseWholeNumber(value, false));
    }

    private static int parseWholeNumber(String value, boolean calorie) throws CommandException {
        String error = calorie ? MESSAGE_INVALID_CALORIES : MESSAGE_INVALID_MACROS;
        if (value == null || !value.matches("[0-9]+")) {
            throw new CommandException(error);
        }
        try {
            int parsed = Integer.parseInt(value);
            if (calorie ? parsed <= 0 : parsed < 0) {
                throw new CommandException(error);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new CommandException(error);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DietCommand otherCommand
                && index.equals(otherCommand.index)
                && java.util.Objects.equals(dietPlan, otherCommand.dietPlan)
                && java.util.Objects.equals(structuredValues, otherCommand.structuredValues);
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this).add("index", index).add("dietPlan", dietPlan);
        if (structuredValues != null) {
            result.add("structuredValues", structuredValues);
        }
        return result.toString();
    }
}
