package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;

/** Exercises daily limits using the command strings entered by a trainer. */
public class ExerciseDailyLimitFlowTest {
    private static final List<String> DAYS = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final List<String> PREFIXES = List.of("m", "tu", "w", "th", "f", "s", "su");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void addex_everyWeekday_acceptsSevenAndRejectsEighth() throws Exception {
        for (int day = 0; day < DAYS.size(); day++) {
            addExercises(1, DAYS.get(day), 6);
            execute("addex 1 n/Seventh " + day + " d/" + PREFIXES.get(day));
            assertDayCount(1, DAYS.get(day), 7);
            assertLimitFailure("addex 1 n/Eighth " + day + " d/" + DAYS.get(day));
        }
        // Seven per day allows 49 exercises in the same client's weekly plan.
        assertEquals(49, plan(1).size());
    }

    @Test
    public void dayex_everyWeekday_acceptsSeventhAndRejectsEighth() throws Exception {
        for (int day = 0; day < DAYS.size(); day++) {
            execute("clearplan 1");
            addExercises(1, DAYS.get(day), 6);
            execute("addex 1 n/Seventh");
            execute("addex 1 n/Eighth");
            execute("dayex 1 7 " + PREFIXES.get(day));
            assertDayCount(1, DAYS.get(day), 7);
            assertLimitFailure("dayex 1 8 " + DAYS.get(day));
            assertEquals("", plan(1).getExercises().get(7).getDay());
        }
    }

    @Test
    public void dayex_sameDayAtCapacity_acceptsMixedCaseAndPrefixes() throws Exception {
        addExercises(1, "Monday", 7);
        WorkoutPlan expected = plan(1);
        for (String day : List.of("m", "MON", "mOnDaY")) {
            execute("dayex 1 4 " + day);
            assertEquals(expected, plan(1));
        }
    }

    @Test
    public void dayex_moveBetweenDays_rejectsFullDestinationAndFreesSourceSlot() throws Exception {
        addExercises(1, "Monday", 7);
        execute("addex 1 n/Tuesday Exercise s/3 r/12 w/25.5 d/tu");
        assertLimitFailure("dayex 1 8 m");

        execute("dayex 1 1 tu");
        assertDayCount(1, "Monday", 6);
        assertDayCount(1, "Tuesday", 2);
        // Sorting puts Tuesday Exercise at index 8 after the moved exercise.
        execute("dayex 1 8 m");
        assertDayCount(1, "Monday", 7);
        assertDayCount(1, "Tuesday", 1);
        assertEquals("Tuesday Exercise", plan(1).getExercises().get(6).getName());
        assertEquals(3, plan(1).getExercises().get(6).getSets());
        assertEquals(12, plan(1).getExercises().get(6).getReps());
        assertEquals(25.5, plan(1).getExercises().get(6).getWeightKg());
        assertLimitFailure("dayex 1 8 m");
    }

    @Test
    public void delex_fromFullDay_allowsOneReplacement() throws Exception {
        addExercises(1, "Monday", 7);
        assertLimitFailure("addex 1 n/Replacement d/m");
        execute("delex 1 4");
        assertDayCount(1, "Monday", 6);
        execute("addex 1 n/Replacement d/m");
        assertDayCount(1, "Monday", 7);
        assertEquals("Replacement", plan(1).getExercises().get(6).getName());
        assertLimitFailure("addex 1 n/Overflow d/m");
    }

    @Test
    public void commands_differentClients_haveIndependentDailyLimits() throws Exception {
        addExercises(1, "Monday", 7);
        addExercises(2, "Monday", 6);
        execute("addex 2 n/Stretch");
        execute("dayex 2 7 m");
        assertDayCount(1, "Monday", 7);
        assertDayCount(2, "Monday", 7);
        assertLimitFailure("addex 1 n/Overflow d/m");
        assertLimitFailure("addex 2 n/Overflow d/m");
    }

    @Test
    public void commands_filteredList_checkDisplayedClientAndPreserveFilterOnFailure() throws Exception {
        addExercises(2, "Monday", 7);
        execute("addex 2 n/Stretch");
        execute("find Benson");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Benson Meier", model.getFilteredPersonList().get(0).getName().fullName);
        assertLimitFailure("addex 1 n/Overflow d/m");
        assertLimitFailure("dayex 1 8 m");
        execute("dayex 1 8 tu");
        assertDayCount(2, "Tuesday", 1);
        assertEquals(0, plan(1).size());
    }

    @Test
    public void addex_unassignedExercises_areUncappedButCannotOverflowWeekday() throws Exception {
        addExercises(1, "Monday", 7);
        for (int index = 0; index < 8; index++) {
            execute("addex 1 n/Unassigned " + index);
        }
        assertDayCount(1, "", 8);
        assertLimitFailure("dayex 1 15 m");
        assertDayCount(1, "Monday", 7);
    }

    private void addExercises(int personIndex, String day, int count) throws Exception {
        for (int index = 0; index < count; index++) {
            execute("addex " + personIndex + " n/" + day + " Exercise " + index + " d/" + day);
        }
    }

    private void execute(String command) throws Exception {
        parser.parseCommand(command).execute(model);
    }

    private void assertLimitFailure(String command) {
        AddressBook expected = new AddressBook(model.getAddressBook());
        List<Person> expectedFiltered = List.copyOf(model.getFilteredPersonList());
        CommandException exception = assertThrows(CommandException.class, () -> execute(command), command);
        assertEquals("A client cannot have more than 7 exercises on the same day.", exception.getMessage());
        assertEquals(expected, model.getAddressBook(), command);
        assertEquals(expectedFiltered, model.getFilteredPersonList(), command);
    }

    private WorkoutPlan plan(int personIndex) {
        return model.getAddressBook().getPersonList().get(personIndex - 1).getWorkoutPlan();
    }

    private void assertDayCount(int personIndex, String day, long expected) {
        assertEquals(expected, plan(personIndex).getExercises().stream()
                .filter(exercise -> exercise.getDay().equals(day)).count(), day);
    }
}
