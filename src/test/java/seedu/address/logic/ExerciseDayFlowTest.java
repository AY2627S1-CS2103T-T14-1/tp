package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

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
