package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.AssignMembershipPlanCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

public class MembershipPreservationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-15T00:00:00Z"), ZoneId.of("UTC"));

    private Model model;
    private MembershipPlan gold;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(new AddressBook(), new UserPrefs());
        gold = new MembershipPlan(new MembershipName("Gold"), new BigDecimal("150"), 30);
        model.addMembershipPlan(gold);
        model.addPerson(createRichPersonWithAssignment(Optional.of(LocalDate.of(2099, 12, 30))));
    }

    private Person createRichPersonWithAssignment(Optional<LocalDate> expiry) {
        WorkoutLogEntry entry = new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
        WorkoutSession session = new WorkoutSession(
                LocalDate.of(2020, 1, 15), Optional.empty(), List.of(entry));
        WorkoutPlan workoutPlan = new WorkoutPlan(List.of(new Exercise("Bench Press", 3, 10, 40.0)));
        Person base = new PersonBuilder().withGoal("Build strength")
                .withWorkoutPlan(workoutPlan)
                .withDietPlan(new DietPlan("1800 kcal", "Lean protein"))
                .withWorkoutSessions(List.of(session))
                .build()
                .withTrainingSession(
                        new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"));
        return base.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, expiry)));
    }

    private void assertAssignmentPreserved(String commandText) throws Exception {
        Person original = model.getAddressBook().getPersonList().get(0);
        new AddressBookParser().parseCommand(commandText).execute(model);
        Person updated = model.getAddressBook().getPersonList().get(0);
        assertEquals(original.getMembershipAssignment(), updated.getMembershipAssignment(),
                "Assignment must be preserved after: " + commandText);
    }

    @Test
    public void execute_edit_preservesAssignment() throws Exception {
        assertAssignmentPreserved("edit 1 p/91234567");
    }

    @Test
    public void execute_remark_preservesAssignment() throws Exception {
        assertAssignmentPreserved("remark 1 r/Likes swimming");
    }

    @Test
    public void execute_diet_preservesAssignment() throws Exception {
        assertAssignmentPreserved("diet 1 t/2000 kcal per day");
    }

    @Test
    public void execute_addExercise_preservesAssignment() throws Exception {
        assertAssignmentPreserved("addex 1 n/Squat s/3 r/10 w/60");
    }

    @Test
    public void execute_deleteExercise_preservesAssignment() throws Exception {
        assertAssignmentPreserved("delex 1 1");
    }

    @Test
    public void execute_clearPlan_preservesAssignment() throws Exception {
        assertAssignmentPreserved("clearplan 1");
    }

    @Test
    public void execute_setExerciseDay_preservesAssignment() throws Exception {
        assertAssignmentPreserved("dayex 1 1 Monday");
    }

    @Test
    public void execute_log_preservesAssignment() throws Exception {
        assertAssignmentPreserved("log 1 d/2020-01-17 ex/Bench press|40|3|8");
    }

    @Test
    public void execute_schedule_preservesAssignment() throws Exception {
        Person original = model.getAddressBook().getPersonList().get(0);
        new AddressBookParser().parseCommand("schedule 1 d/2099-03-16 tm/09:00 l/Studio").execute(model);
        Person updated = model.getAddressBook().getPersonList().get(0);
        assertEquals(original.getMembershipAssignment(), updated.getMembershipAssignment());
        assertEquals(2, updated.getTrainingSessions().size());
        assertEquals(new TrainingSession(LocalDateTime.of(2099, 3, 16, 9, 0), "Studio"),
                updated.getTrainingSessions().get(1));
    }

    @Test
    public void withTrainingSession_preservesAssignment() {
        Person original = model.getAddressBook().getPersonList().get(0);
        Person updated = original.withTrainingSession(
                new TrainingSession(LocalDateTime.of(2099, 4, 1, 10, 0), "Studio"));
        assertEquals(original.getMembershipAssignment(), updated.getMembershipAssignment());
    }

    @Test
    public void execute_membershipCommands_preserveOtherFields() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        Person original = model.getAddressBook().getPersonList().get(0);

        parser.parseCommand("imem 1 3").execute(model);
        Person increased = model.getAddressBook().getPersonList().get(0);
        assertEquals(original.getGoal(), increased.getGoal());
        assertEquals(original.getWorkoutPlan(), increased.getWorkoutPlan());
        assertEquals(original.getDietPlan(), increased.getDietPlan());
        assertEquals(original.getTrainingSessions(), increased.getTrainingSessions());
        assertEquals(original.getWorkoutSessions(), increased.getWorkoutSessions());
        assertEquals(33, increased.getMembershipAssignment().orElseThrow().getRemainingSessions());

        parser.parseCommand("dmem 1 3").execute(model);
        Person decreased = model.getAddressBook().getPersonList().get(0);
        assertEquals(original.getGoal(), decreased.getGoal());
        assertEquals(original.getWorkoutPlan(), decreased.getWorkoutPlan());
        assertEquals(30, decreased.getMembershipAssignment().orElseThrow().getRemainingSessions());

        parser.parseCommand("rmem 1").execute(model);
        Person removed = model.getAddressBook().getPersonList().get(0);
        assertTrue(removed.getMembershipAssignment().isEmpty());
        assertEquals(original.getGoal(), removed.getGoal());
        assertEquals(original.getWorkoutPlan(), removed.getWorkoutPlan());
        assertEquals(original.getDietPlan(), removed.getDietPlan());

        parser.parseCommand("amem Gold 1").execute(model);
        Person reassigned = model.getAddressBook().getPersonList().get(0);
        assertEquals(original.getGoal(), reassigned.getGoal());
        assertEquals(30, reassigned.getMembershipAssignment().orElseThrow().getRemainingSessions());
    }

    @Test
    public void expiryBoundary_todayRejectedTomorrowAccepted() {
        AssignMembershipPlanCommandParser parser =
                new AssignMembershipPlanCommandParser(FIXED_CLOCK);
        try {
            parser.parse(" Gold 1 e/2026-06-15");
            throw new AssertionError("Today expiry should be rejected");
        } catch (ParseException e) {
            assertEquals(MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS, e.getMessage());
        }
        try {
            AssignMembershipPlanCommand command = parser.parse(" Gold 1 e/2026-06-16");
            assertEquals(new AssignMembershipPlanCommand(new MembershipName("Gold"),
                    Index.fromOneBased(1), Optional.of(LocalDate.of(2026, 6, 16))), command);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void storedPastDate_loadsAndDisplays() {
        MembershipAssignment past = new MembershipAssignment("gold", "Gold", 12,
                Optional.of(LocalDate.of(2020, 1, 1)));
        assertEquals("Membership plan: Gold; Sessions remaining: 12; Expiry: 2020-01-01",
                past.toString());
        AddressBook book = new AddressBook();
        book.addMembershipPlan(gold);
        Person expired = new PersonBuilder().build()
                .withMembershipAssignment(Optional.of(past));
        book.addPerson(expired);
        assertEquals(past, book.getPersonList().get(0).getMembershipAssignment().orElseThrow());
    }

    @Test
    public void execute_goalEdit_preservesAssignmentAndSessions() throws Exception {
        Person original = model.getAddressBook().getPersonList().get(0);
        new AddressBookParser().parseCommand("edit 1 g/Improve mobility").execute(model);
        Person updated = model.getAddressBook().getPersonList().get(0);
        assertEquals(new Goal("Improve mobility"), updated.getGoal().orElseThrow());
        assertEquals(original.getMembershipAssignment(), updated.getMembershipAssignment());
        assertEquals(original.getWorkoutSessions(), updated.getWorkoutSessions());
    }
}
