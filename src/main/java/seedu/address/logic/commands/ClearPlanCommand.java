package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;

/**
 * Clears all exercises from the workout plan of an existing person in the address book.
 */
public class ClearPlanCommand extends Command {

    public static final String COMMAND_WORD = "clearplan";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Clears all exercises from the workout plan of the person identified "
            + "by the index number used in the last person listing.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_CLEAR_PLAN_SUCCESS = "Cleared workout plan for Person: %1$s";
    public static final String MESSAGE_PLAN_ALREADY_EMPTY = "Workout plan is already empty for Person: %1$s";

    private final Index targetIndex;

    /**
     * @param targetIndex of the person in the filtered person list to clear the workout plan for
     */
    public ClearPlanCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(targetIndex.getZeroBased());

        if (personToEdit.getWorkoutPlan().isEmpty()) {
            return new CommandResult(
                    String.format(MESSAGE_PLAN_ALREADY_EMPTY, Messages.format(personToEdit)));
        }

        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getRemark(), personToEdit.getTags(),
                new WorkoutPlan(), personToEdit.getDietPlan(),
                personToEdit.getGoal().orElse(null), personToEdit.getTrainingSessions(),
                personToEdit.getWorkoutSessions(), personToEdit.getDietaryRestrictions(),
                personToEdit.getHealthProfile().orElse(null), personToEdit.getMembershipAssignment(),
                        personToEdit.getBodyProgress(), personToEdit.getPaymentRecords());

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_CLEAR_PLAN_SUCCESS, Messages.format(editedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ClearPlanCommand otherClearPlanCommand)) {
            return false;
        }

        return targetIndex.equals(otherClearPlanCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
