package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

/** Tests complete workout history and replacement of one displayed session. */
public class WorkoutHistoryCommandTest {
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Index SECOND = Index.fromOneBased(2);
    private static final WorkoutLogEntry SQUAT =
            new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
    private static final WorkoutLogEntry BENCH =
            new WorkoutLogEntry("Bench press", new BigDecimal("40"), 3, 8);

    @Test
    public void history_sortsNewestFirstAndPreservesExerciseOrder() throws Exception {
        WorkoutSession early = session(15, Optional.empty(), List.of(SQUAT, BENCH));
        WorkoutSession late = session(17, Optional.of(LocalTime.of(18, 0)), List.of(BENCH));
        Person client = withSessions(new PersonBuilder().build(), List.of(early, late));
        Model model = modelWith(client);

        String feedback = new HistoryCommand(FIRST).execute(model).getFeedbackToUser();

        assertEquals("Workout history for " + client.getName() + ":\n"
                + "1. 2020-01-17 at 18:00\n   Bench press | 40 kg | 3 sets | 8 reps\n"
                + "2. 2020-01-15 (time not specified)\n"
                + "   Back squat | 60 kg | 3 sets | 10 reps\n"
                + "   Bench press | 40 kg | 3 sets | 8 reps", feedback);
        assertEquals(List.of(early, late), model.getAddressBook().getPersonList().get(0).getWorkoutSessions());
    }

    @Test
    public void history_emptyClient_returnsEmptyState() throws Exception {
        Person client = new PersonBuilder().build();
        Model model = modelWith(client);
        assertEquals("No workout sessions recorded for " + client.getName() + ".",
                new HistoryCommand(FIRST).execute(model).getFeedbackToUser());
        assertEquals(List.of(client), model.getAddressBook().getPersonList());
    }

    @Test
    public void edit_secondDisplayedSession_preservesStoredOrderAndOtherFields() throws Exception {
        WorkoutSession older = session(15, Optional.empty(), List.of(SQUAT));
        WorkoutSession newer = session(17, Optional.empty(), List.of(BENCH));
        WorkoutSession replacement = session(18, Optional.of(LocalTime.of(19, 0)), List.of(BENCH, SQUAT));
        Person original = withSessions(new PersonBuilder().withGoal("Build strength")
                .withRemark("Likes swimming").build(), List.of(older, newer));
        Model model = modelWith(original);

        new EditLogCommand(FIRST, SECOND, replacement).execute(model);

        Person updated = model.getAddressBook().getPersonList().get(0);
        assertEquals(List.of(replacement, newer), updated.getWorkoutSessions());
        assertEquals(original.getGoal(), updated.getGoal());
        assertEquals(original.getRemark(), updated.getRemark());
        assertEquals(List.of(older, newer), original.getWorkoutSessions());
        assertEquals(replacement, updated.getWorkoutSessions().get(0));
    }

    @Test
    public void edit_sameKeyAsAnotherSession_rejectedWithoutMutation() {
        WorkoutSession early = session(15, Optional.empty(), List.of(SQUAT));
        WorkoutSession late = session(17, Optional.empty(), List.of(BENCH));
        Person original = withSessions(new PersonBuilder().build(), List.of(early, late));
        Model model = modelWith(original);
        WorkoutSession collision = session(17, Optional.empty(), List.of(SQUAT));

        CommandException exception = assertThrows(CommandException.class,
                () -> new EditLogCommand(FIRST, SECOND, collision).execute(model));

        assertEquals(EditLogCommand.MESSAGE_DUPLICATE_SESSION, exception.getMessage());
        assertEquals(List.of(original), model.getAddressBook().getPersonList());
    }

    @Test
    public void edit_unchangedOrInvalidSession_rejectedWithoutMutation() {
        WorkoutSession existing = session(15, Optional.empty(), List.of(SQUAT));
        Person original = withSessions(new PersonBuilder().build(), List.of(existing));
        Model model = modelWith(original);
        CommandException unchanged = assertThrows(CommandException.class,
                () -> new EditLogCommand(FIRST, FIRST, existing).execute(model));
        CommandException missing = assertThrows(CommandException.class,
                () -> new EditLogCommand(FIRST, SECOND, existing).execute(model));
        assertEquals(EditLogCommand.MESSAGE_NO_CHANGE, unchanged.getMessage());
        assertEquals(EditLogCommand.MESSAGE_INVALID_SESSION_INDEX, missing.getMessage());
        assertEquals(List.of(original), model.getAddressBook().getPersonList());
    }

    @Test
    public void historyAndEdit_useFilteredClientIndex() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = withSessions(new PersonBuilder().withName("Bob").build(),
                List.of(session(15, Optional.empty(), List.of(SQUAT))));
        Model model = modelWith(alice, bob);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Bob")));

        assertEquals("Workout history for Bob:\n1. 2020-01-15 (time not specified)\n"
                + "   Back squat | 60 kg | 3 sets | 10 reps",
                new HistoryCommand(FIRST).execute(model).getFeedbackToUser());
        WorkoutSession replacement = session(16, Optional.empty(), List.of(BENCH));
        new EditLogCommand(FIRST, FIRST, replacement).execute(model);
        assertEquals(List.of(alice, withSessions(bob, List.of(replacement))),
                model.getAddressBook().getPersonList());
        assertEquals(1, model.getFilteredPersonList().size());
        CommandException invalidClient = assertThrows(CommandException.class,
                () -> new HistoryCommand(SECOND).execute(model));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, invalidClient.getMessage());
    }

    private static WorkoutSession session(int day, Optional<LocalTime> time, List<WorkoutLogEntry> entries) {
        return new WorkoutSession(LocalDate.of(2020, 1, day), time, entries);
    }

    private static Person withSessions(Person client, List<WorkoutSession> sessions) {
        return new Person(client.getName(), client.getPhone(), client.getEmail(), client.getAddress(),
                client.getRemark(), client.getTags(), client.getWorkoutPlan(), client.getDietPlan(),
                client.getGoal().orElse(null), client.getTrainingSessions(), sessions,
                client.getDietaryRestrictions(), client.getHealthProfile().orElse(null),
                client.getMembershipAssignment());
    }

    private static Model modelWith(Person... people) {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        for (Person person : people) {
            model.addPerson(person);
        }
        return model;
    }
}
