package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that commands preserve scheduled training and recorded workout sessions.
 */
public class WorkoutSessionPreservationTest {

    @Test
    public void execute_edit_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("edit 1 p/91234567");
    }

    @Test
    public void execute_remark_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("remark 1 r/Likes swimming");
    }

    @Test
    public void execute_diet_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("diet 1 t/2000 kcal per day");
    }

    @Test
    public void execute_addExercise_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("addex 1 n/Squat s/3 r/10 w/60");
    }

    @Test
    public void execute_deleteExercise_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("delex 1 1");
    }

    @Test
    public void execute_clearPlan_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("clearplan 1");
    }

    @Test
    public void execute_setExerciseDay_preservesWorkoutSessions() throws Exception {
        assertSessionsPreserved("dayex 1 1 Monday");
    }

    @Test
    public void execute_editAndClearGoal_preservesWorkoutSessions() throws Exception {
        Person original = createPersonWithSessions();
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addPerson(original);
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand("edit 1 g/Improve mobility").execute(model);

        Person updated = model.getAddressBook().getPersonList().get(0);
        assertEquals(new Goal("Improve mobility"), updated.getGoal().orElseThrow());
        assertEquals(original.getWorkoutSessions(), updated.getWorkoutSessions());
        assertEquals(original.getTrainingSessions(), updated.getTrainingSessions());

        parser.parseCommand("edit 1 g/").execute(model);

        Person cleared = model.getAddressBook().getPersonList().get(0);
        assertTrue(cleared.getGoal().isEmpty());
        assertEquals(original.getWorkoutSessions(), cleared.getWorkoutSessions());
        assertEquals(original.getTrainingSessions(), cleared.getTrainingSessions());
    }

    @Test
    public void execute_logAndSchedule_preserveBothSessionTypes() throws Exception {
        Person original = createPersonWithSessions();
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addPerson(original);
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand("log 1 d/2020-01-17 ex/Bench press|40|3|8").execute(model);
        Person logged = model.getAddressBook().getPersonList().get(0);
        assertEquals(original.getTrainingSessions(), logged.getTrainingSessions());
        assertEquals(3, logged.getWorkoutSessions().size());
        assertEquals(original.getWorkoutSessions(), logged.getWorkoutSessions().subList(0, 2));

        parser.parseCommand("schedule 1 d/2099-03-16 tm/09:00 l/Studio").execute(model);
        Person scheduled = model.getAddressBook().getPersonList().get(0);
        assertEquals(logged.getWorkoutSessions(), scheduled.getWorkoutSessions());
        assertEquals(2, scheduled.getTrainingSessions().size());
        assertEquals(original.getTrainingSessions(), scheduled.getTrainingSessions().subList(0, 1));
        assertEquals(new TrainingSession(LocalDateTime.of(2099, 3, 16, 9, 0), "Studio"),
                scheduled.getTrainingSessions().get(1));
    }

    private void assertSessionsPreserved(String commandText) throws Exception {
        Person original = createPersonWithSessions();
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addPerson(original);

        Command command = new AddressBookParser().parseCommand(commandText);
        command.execute(model);

        assertEquals(1, model.getAddressBook().getPersonList().size());
        Person updated = model.getAddressBook().getPersonList().get(0);

        assertEquals(original.getWorkoutSessions(), updated.getWorkoutSessions(),
                "Workout sessions must be preserved after: " + commandText);
        assertEquals(original.getTrainingSessions(), updated.getTrainingSessions(),
                "Training sessions must be preserved after: " + commandText);
        assertEquals(original.getGoal(), updated.getGoal(),
                "Goal must be preserved after: " + commandText);
        assertFalse(original.equals(updated),
                "The command should still update its intended field: " + commandText);
    }

    private Person createPersonWithSessions() {
        WorkoutLogEntry entry = new WorkoutLogEntry(
                "Back squat", new BigDecimal("60"), 3, 10);

        WorkoutSession firstSession = new WorkoutSession(
                LocalDate.of(2020, 1, 15), Optional.empty(), List.of(entry));
        WorkoutSession secondSession = new WorkoutSession(
                LocalDate.of(2020, 1, 16), Optional.of(LocalTime.of(18, 0)), List.of(entry));

        WorkoutPlan plan = new WorkoutPlan(
                List.of(new Exercise("Bench Press", 3, 10, 40.0)));
        Person base = new PersonBuilder().withGoal("Build strength").withWorkoutPlan(plan).build()
                .withTrainingSession(new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"));

        return new Person(
                base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getRemark(), base.getTags(), base.getWorkoutPlan(), base.getDietPlan(),
                base.getGoal().orElse(null), base.getTrainingSessions(), List.of(firstSession, secondSession));
    }
}
