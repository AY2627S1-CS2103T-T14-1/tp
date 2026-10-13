package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

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
 * Adds an exercise to the workout plan of an existing person in the address book.
 */
public class AddExerciseCommand extends Command {

    public static final String COMMAND_WORD = "addex";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an exercise to the workout plan of the person identified "
            + "by the index number used in the last person listing.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "n/EXERCISE_NAME [s/SETS] [r/REPS] [w/WEIGHT_KG] [d/DAY]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + "n/Bench Press s/3 r/10 w/60 d/M";

    public static final String MESSAGE_ADD_EXERCISE_SUCCESS = "Added exercise to Person: %1$s";
    public static final String MESSAGE_DUPLICATE_EXERCISE =
            "This exercise already exists on this day in the workout plan.";

    private final Index index;
    private final Exercise exercise;

    /**
     * @param index of the person in the filtered person list to add the exercise to
     * @param exercise to be added to the person's workout plan
     */
    public AddExerciseCommand(Index index, Exercise exercise) {
        requireAllNonNull(index, exercise);

        this.index = index;
        this.exercise = exercise;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        if (personToEdit.getWorkoutPlan().hasExercise(exercise)) {
            throw new CommandException(MESSAGE_DUPLICATE_EXERCISE);
        }
        WorkoutPlan updatedPlan = personToEdit.getWorkoutPlan().addExercise(exercise);
        if (!updatedPlan.isWithinDailyLimit(exercise.getDay())) {
            throw new CommandException(Messages.MESSAGE_DAILY_EXERCISE_LIMIT);
        }
        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getRemark(), personToEdit.getTags(),
                updatedPlan, personToEdit.getDietPlan());

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_ADD_EXERCISE_SUCCESS, Messages.format(editedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddExerciseCommand otherAddExerciseCommand)) {
            return false;
        }

        return index.equals(otherAddExerciseCommand.index)
                && exercise.equals(otherAddExerciseCommand.exercise);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("exercise", exercise)
                .toString();
    }
}
