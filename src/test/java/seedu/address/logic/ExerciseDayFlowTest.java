package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.WorkoutPlan;

/** Checks the user-visible command flow for weekday grouping and exercise indices. */
public class ExerciseDayFlowTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void addRetagDelete_keepsWeekdayGroupsAndDisplayedIndicesInSync() throws Exception {
        execute("addex 1 n/Run s/1 r/1 w/0 d/su");
        execute("addex 1 n/Squat s/3 r/10 w/80 d/s");
        execute("addex 1 n/Press s/3 r/10 w/60 d/w");
        execute("addex 1 n/Stretch s/1 r/1 w/0");
        assertPlan(List.of("Press", "Squat", "Run", "Stretch"),
                List.of("Wednesday", "Saturday", "Sunday", "Day not specified"));

        execute("dayex 1 4 Friday");
        assertPlan(List.of("Press", "Stretch", "Squat", "Run"),
                List.of("Wednesday", "Friday", "Saturday", "Sunday"));

        execute("delex 1 3");
        assertPlan(List.of("Press", "Stretch", "Run"),
                List.of("Wednesday", "Friday", "Sunday"));
    }

    @Test
    public void sameExerciseAcrossDays_addMoveAndDelete_preservesOtherOccurrences() throws Exception {
        execute("addex 1 n/Exercise1 d/Monday");
        execute("addex 1 n/Exercise1 d/Friday");
        execute("addex 1 n/Exercise1");
        assertPlan(List.of("Exercise1", "Exercise1", "Exercise1"),
                List.of("Monday", "Friday", "Day not specified"));

        WorkoutPlan beforeFailure = model.getFilteredPersonList().get(0).getWorkoutPlan();
        assertThrows(CommandException.class,
                AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE, () -> execute("addex 1 n/exercise1 s/5 d/mON"));
        assertThrows(CommandException.class,
                AddExerciseCommand.MESSAGE_DUPLICATE_EXERCISE, () -> execute("dayex 1 2 m"));
        assertEquals(beforeFailure, model.getFilteredPersonList().get(0).getWorkoutPlan());

        execute("dayex 1 3 w");
        assertPlan(List.of("Exercise1", "Exercise1", "Exercise1"),
                List.of("Monday", "Wednesday", "Friday"));
        execute("delex 1 2");
        assertPlan(List.of("Exercise1", "Exercise1"), List.of("Monday", "Friday"));
    }

    private void execute(String command) throws Exception {
        parser.parseCommand(command).execute(model);
    }

    private void assertPlan(List<String> names, List<String> headings) {
        WorkoutPlan plan = model.getFilteredPersonList().get(0).getWorkoutPlan();
        List<Messages.IndexedExercise> displayed = Messages.getExercisesInDisplayOrder(plan);
        assertEquals(names, displayed.stream().map(item -> item.exercise().getName()).toList());
        assertEquals(headings, displayed.stream()
                .map(item -> Messages.getExerciseDayHeading(item.exercise())).toList());
        for (int index = 0; index < displayed.size(); index++) {
            assertEquals(index + 1, displayed.get(index).index());
            assertEquals(displayed.get(index).exercise(), plan.getExercises().get(index));
        }
        String formatted = Messages.formatWorkoutPlan(plan);
        assertFalse(formatted.contains("["));
        for (String heading : headings) {
            assertTrue(formatted.contains("**" + heading + "**"));
        }
        if (headings.size() > 1) {
            assertTrue(formatted.contains("\n\n**"));
        }
    }
}
