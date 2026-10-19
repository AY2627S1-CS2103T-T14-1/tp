package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.TrainingSession.Kind;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

public class AdvancedSchedulingCommandTest {
    private static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.of(2099, 3, 15, 14, 30);

    @Test
    public void meeting_andDaySchedule_showBothAppointmentTypes() throws Exception {
        Model model = modelWithClients();
        new MeetingCommand(Index.fromOneBased(1), new TrainingSession(APPOINTMENT_TIME, "Cafe", Kind.MEETING))
                .execute(model);
        new ScheduleCommand(Index.fromOneBased(2),
                new TrainingSession(APPOINTMENT_TIME.plusHours(1), "Gym")).execute(model);

        assertEquals("Client appointments on 2099-03-15:\n"
                + "1. 14:30 Amy Bee (meeting) at Cafe\n"
                + "2. 15:30 Bob Cat (training) at Gym",
                new DayScheduleCommand(APPOINTMENT_TIME.toLocalDate()).execute(model).getFeedbackToUser());
        assertEquals("Training sessions for Amy Bee:\n1. 2099-03-15 14:30 at Cafe (meeting)",
                new ViewScheduleCommand(Index.fromOneBased(1)).execute(model).getFeedbackToUser());
    }

    @Test
    public void schedule_conflictWithFilteredOutClient_rejectsWithoutChangingData() throws Exception {
        Model model = modelWithClients();
        Person bob = model.getFilteredPersonList().get(1);
        model.setPerson(bob, bob.withTrainingSession(new TrainingSession(APPOINTMENT_TIME, "Gym")));
        model.updateFilteredPersonList(person -> person.getName().toString().equals("Amy Bee"));

        CommandException exception = assertThrows(CommandException.class, () ->
                new MeetingCommand(Index.fromOneBased(1),
                        new TrainingSession(APPOINTMENT_TIME, "Cafe", Kind.MEETING)).execute(model));

        assertEquals("Scheduling conflict: Bob Cat already has an appointment at 2099-03-15 14:30.",
                exception.getMessage());
        assertEquals(0, model.getFilteredPersonList().get(0).getTrainingSessions().size());
    }

    @Test
    public void inactive_includesStaleAndNeverRecordedButExcludesToday() {
        Model model = modelWithClients();
        Person amy = model.getFilteredPersonList().get(0);
        Person bob = model.getFilteredPersonList().get(1);
        model.setPerson(amy, new PersonBuilder(amy).withWorkoutSessions(List.of(workout(LocalDate.now()))).build());
        model.setPerson(bob, new PersonBuilder(bob)
                .withWorkoutSessions(List.of(workout(LocalDate.now().minusDays(14)))).build());

        assertEquals("Clients without a recorded workout in the last 14 days:\n"
                        + "1. Bob Cat — last workout: " + LocalDate.now().minusDays(14),
                new InactiveCommand(14).execute(model).getFeedbackToUser());
    }

    @Test
    public void inactive_noClientsHaveWorkout_listsNeverRecorded() {
        assertEquals("Clients without a recorded workout in the last 7 days:\n"
                        + "1. Amy Bee — last workout: never recorded\n"
                        + "2. Bob Cat — last workout: never recorded",
                new InactiveCommand(7).execute(modelWithClients()).getFeedbackToUser());
    }

    @Test
    public void inactive_allClientsHaveRecentWorkout_reportsNoInactiveClients() {
        Model model = modelWithClients();
        for (Person client : List.copyOf(model.getFilteredPersonList())) {
            model.setPerson(client, new PersonBuilder(client)
                    .withWorkoutSessions(List.of(workout(LocalDate.now()))).build());
        }

        assertEquals("All clients have a recorded workout in the last 7 days.",
                new InactiveCommand(7).execute(model).getFeedbackToUser());
    }

    @Test
    public void commandConstructors_rejectInvalidAppointmentKindAndDayCount() {
        assertThrows(IllegalArgumentException.class, () -> new InactiveCommand(0));
        assertThrows(IllegalArgumentException.class, () ->
                new MeetingCommand(Index.fromOneBased(1), new TrainingSession(APPOINTMENT_TIME, "Gym")));
    }

    @Test
    public void daySchedule_emptyDay_reportsNoAppointments() {
        assertEquals("No client appointments on 2099-03-15.",
                new DayScheduleCommand(APPOINTMENT_TIME.toLocalDate())
                        .execute(modelWithClients()).getFeedbackToUser());
    }

    private Model modelWithClients() {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().build());
        book.addPerson(new PersonBuilder().withName("Bob Cat").withPhone("91234567")
                .withEmail("bob@example.com").build());
        return new ModelManager(book, new UserPrefs());
    }

    private WorkoutSession workout(LocalDate date) {
        return new WorkoutSession(date, Optional.empty(),
                List.of(new WorkoutLogEntry("Squat", new BigDecimal("60"), 3, 10)));
    }
}
