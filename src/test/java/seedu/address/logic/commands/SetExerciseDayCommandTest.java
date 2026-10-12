package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

public class SetExerciseDayCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_setsDayOnExercise() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Exercise exercise = new Exercise("Bench Press", 3, 10, 60.0);
        Person withPlan = new PersonBuilder(original).withWorkoutPlan(new WorkoutPlan().addExercise(exercise)).build();
        model.setPerson(original, withPlan);

        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Monday").execute(model);

        Exercise tagged = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getWorkoutPlan().getExercises().get(0);
        assertEquals("Monday", tagged.getDay());
        assertEquals("Bench Press 3x10 @60kg [Mon]", seedu.address.logic.Messages.formatExercise(tagged));
    }

    @Test
    public void execute_invalidExerciseIndex_throwsCommandException() {
        assertCommandFailure(new SetExerciseDayCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1), "Monday"),
                model, SetExerciseDayCommand.MESSAGE_INVALID_EXERCISE_INDEX);
    }
}
