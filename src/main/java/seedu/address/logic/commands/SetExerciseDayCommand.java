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

/** Assigns a day label to an exercise in a person's workout plan. */
public class SetExerciseDayCommand extends Command {
    public static final String COMMAND_WORD = "dayex";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Assigns a day tag to an exercise in a person's workout plan.\n"
            + "Parameters: PERSON_INDEX EXERCISE_INDEX DAY\n"
            + "Example: " + COMMAND_WORD + " 1 2 Tuesday";
    public static final String MESSAGE_SUCCESS = "Assigned day to exercise: %1$s";
    public static final String MESSAGE_INVALID_EXERCISE_INDEX = "The exercise index provided is invalid.";

    private final Index personIndex;
    private final Index exerciseIndex;
    private final String day;

    /** Creates a command to assign {@code day} to the indexed exercise. */
    public SetExerciseDayCommand(Index personIndex, Index exerciseIndex, String day) {
        requireAllNonNull(personIndex, exerciseIndex, day);
        this.personIndex = personIndex;
        this.exerciseIndex = exerciseIndex;
        this.day = day;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> people = model.getFilteredPersonList();
        if (personIndex.getZeroBased() >= people.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = people.get(personIndex.getZeroBased());
        List<Exercise> exercises = person.getWorkoutPlan().getExercises();
        if (exerciseIndex.getZeroBased() >= exercises.size()) {
            throw new CommandException(MESSAGE_INVALID_EXERCISE_INDEX);
        }
        Exercise exercise = exercises.get(exerciseIndex.getZeroBased());
        if (person.getWorkoutPlan().removeExercise(exercise).hasExercise(exercise.withDay(day))) {
            throw new CommandException(AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE);
        }
        WorkoutPlan plan = person.getWorkoutPlan().setExerciseDay(exerciseIndex.getZeroBased(), day);
        if (!plan.isWithinDailyLimit(day)) {
            throw new CommandException(Messages.MESSAGE_DAILY_EXERCISE_LIMIT);
        }
        Person edited = new Person(
                person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getRemark(), person.getTags(), plan, person.getDietPlan(),
                person.getGoal().orElse(null), person.getTrainingSessions(), person.getWorkoutSessions(),
                person.getHealthProfile().orElse(null));
        model.setPerson(person, edited);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(edited)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SetExerciseDayCommand command
                && personIndex.equals(command.personIndex) && exerciseIndex.equals(command.exerciseIndex)
                && day.equals(command.day);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("personIndex", personIndex)
                .add("exerciseIndex", exerciseIndex).add("day", day).toString();
    }
}
