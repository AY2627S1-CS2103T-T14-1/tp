package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;

/**
 * Removes an exercise from the workout plan of an existing person in the address book.
 */
public class DeleteExerciseCommand extends Command {

    public static final String COMMAND_WORD = "delex";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes the exercise at the specified exercise index from the workout plan "
            + "of the person identified by the index number used in the last person listing.\n"
            + "Parameters: PERSON_INDEX EXERCISE_INDEX (both must be positive integers)\n"
            + "Example: " + COMMAND_WORD + " 1 2";

    public static final String MESSAGE_DELETE_EXERCISE_SUCCESS = "Removed exercise from Person: %1$s";
    public static final String MESSAGE_INVALID_EXERCISE_DISPLAYED_INDEX =
            "The exercise index provided is invalid.";

    private final Index personIndex;
    private final Index exerciseIndex;

    /**
     * @param personIndex of the person in the filtered person list
     * @param exerciseIndex of the exercise in the person's workout plan (one-based)
     */
    public DeleteExerciseCommand(Index personIndex, Index exerciseIndex) {
        requireAllNonNull(personIndex, exerciseIndex);

        this.personIndex = personIndex;
        this.exerciseIndex = exerciseIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (personIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(personIndex.getZeroBased());
        List<Exercise> exercises = personToEdit.getWorkoutPlan().getExercises();

        if (exerciseIndex.getZeroBased() >= exercises.size()) {
            throw new CommandException(MESSAGE_INVALID_EXERCISE_DISPLAYED_INDEX);
        }

        List<Exercise> updatedExercises = new ArrayList<>(exercises);
        updatedExercises.remove(exerciseIndex.getZeroBased());
        WorkoutPlan updatedPlan = new WorkoutPlan(updatedExercises);
        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getRemark(), personToEdit.getTags(),
                updatedPlan, personToEdit.getDietPlan(),
                personToEdit.getGoal().orElse(null), personToEdit.getWorkoutSessions());

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_DELETE_EXERCISE_SUCCESS, Messages.format(editedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DeleteExerciseCommand otherDeleteExerciseCommand)) {
            return false;
        }

        return personIndex.equals(otherDeleteExerciseCommand.personIndex)
                && exerciseIndex.equals(otherDeleteExerciseCommand.exerciseIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("personIndex", personIndex)
                .add("exerciseIndex", exerciseIndex)
                .toString();
    }
}
