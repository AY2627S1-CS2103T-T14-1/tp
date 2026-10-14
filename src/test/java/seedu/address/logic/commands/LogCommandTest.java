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
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests recording workout sessions and preserving existing client data.
 */
public class LogCommandTest {

    private static final Index FIRST_CLIENT = Index.fromOneBased(1);
    private static final Index SECOND_CLIENT = Index.fromOneBased(2);
    private static final LocalDate DATE = LocalDate.of(2020, 1, 15);
    private static final LocalTime TIME = LocalTime.of(18, 0);

    private static final WorkoutLogEntry SQUAT =
            new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
    private static final WorkoutLogEntry BENCH =
            new WorkoutLogEntry("Bench press", new BigDecimal("40"), 3, 8);

    @Test
    public void execute_validClient_appendsSessionAndPreservesOtherData() throws Exception {
        WorkoutSession existing = new WorkoutSession(
                DATE.plusDays(1), Optional.empty(), List.of(SQUAT));
        WorkoutSession added = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(BENCH, SQUAT));

        Person base = new PersonBuilder()
                .withRemark("Likes swimming")
                .withTags("regular")
                .withWorkoutPlan(new WorkoutPlan(
                        List.of(new Exercise("Bench Press", 3, 10, 40.0))))
                .withDietPlan(new DietPlan("2000 kcal", "More vegetables"))
                .build();
        Person original = withSessions(base, List.of(existing));
        Model model = createModel(original);

        CommandResult result = new LogCommand(FIRST_CLIENT, added).execute(model);

        Person expected = withSessions(original, List.of(existing, added));
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(existing), original.getWorkoutSessions());
        assertEquals(String.format(
                        LogCommand.MESSAGE_SUCCESS, original.getName(), "2020-01-15 at 18:00"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_sameDateDifferentTimes_addsDistinctSessions() throws Exception {
        WorkoutSession withoutTime = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));
        WorkoutSession firstTimed = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT));
        WorkoutSession secondTimed = new WorkoutSession(
                DATE, Optional.of(TIME.plusHours(1)), List.of(BENCH));

        Person original = new PersonBuilder().build();
        Model model = createModel(original);

        CommandResult firstResult = new LogCommand(FIRST_CLIENT, withoutTime).execute(model);
        new LogCommand(FIRST_CLIENT, firstTimed).execute(model);
        new LogCommand(FIRST_CLIENT, secondTimed).execute(model);

        Person expected = withSessions(
                original, List.of(withoutTime, firstTimed, secondTimed));
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(String.format(
                        LogCommand.MESSAGE_SUCCESS, original.getName(), "2020-01-15"),
                firstResult.getFeedbackToUser());
    }

    @Test
    public void execute_duplicateDateTimeWithDifferentExercises_leavesModelUnchanged() {
        WorkoutSession existing = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT));
        WorkoutSession duplicate = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(BENCH));

        Person original = withSessions(new PersonBuilder().build(), List.of(existing));
        Model model = createModel(original);

        assertFailureLeavesModelUnchanged(
                new LogCommand(FIRST_CLIENT, duplicate),
                model, LogCommand.MESSAGE_DUPLICATE_SESSION);
    }

    @Test
    public void execute_duplicateDateWithoutTime_leavesModelUnchanged() {
        WorkoutSession existing = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));
        WorkoutSession duplicate = new WorkoutSession(
                DATE, Optional.empty(), List.of(BENCH));

        Person original = withSessions(new PersonBuilder().build(), List.of(existing));
        Model model = createModel(original);

        assertFailureLeavesModelUnchanged(
                new LogCommand(FIRST_CLIENT, duplicate),
                model, LogCommand.MESSAGE_DUPLICATE_SESSION);
    }

    @Test
    public void execute_sameDateTimeForDifferentClients_success() throws Exception {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT));

        Person alice = withSessions(
                new PersonBuilder().withName("Alice").build(), List.of(session));
        Person bob = new PersonBuilder().withName("Bob").build();
        Model model = createModel(alice, bob);

        new LogCommand(SECOND_CLIENT, session).execute(model);

        assertEquals(
                List.of(alice, withSessions(bob, List.of(session))),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_filteredList_targetsDisplayedClientAndPreservesFilter() throws Exception {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        Model model = createModel(alice, bob);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Bob")));

        WorkoutSession session = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT));

        new LogCommand(FIRST_CLIENT, session).execute(model);

        Person updatedBob = withSessions(bob, List.of(session));
        assertEquals(List.of(alice, updatedBob), model.getAddressBook().getPersonList());
        assertEquals(List.of(updatedBob), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndex_leavesModelUnchanged() {
        Model model = createModel(new PersonBuilder().build());
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));

        assertFailureLeavesModelUnchanged(
                new LogCommand(SECOND_CLIENT, session),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_indexOutsideFilteredList_leavesModelUnchanged() {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        Model model = createModel(alice, bob);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Bob")));

        WorkoutSession session = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));

        assertFailureLeavesModelUnchanged(
                new LogCommand(SECOND_CLIENT, session),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(List.of(bob), model.getFilteredPersonList());
    }

    private void assertFailureLeavesModelUnchanged(
            LogCommand command, Model model, String expectedMessage) {
        AddressBook original = new AddressBook(model.getAddressBook());

        CommandException exception = assertThrows(CommandException.class, () -> {
            command.execute(model);
        });

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(original, model.getAddressBook());
    }

    private Model createModel(Person... persons) {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }

    private Person withSessions(Person person, List<WorkoutSession> sessions) {
        return new Person(
                person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getRemark(), person.getTags(), person.getWorkoutPlan(), person.getDietPlan(),
                sessions);
    }
}
