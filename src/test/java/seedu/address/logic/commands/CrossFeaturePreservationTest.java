package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.model.person.WorkoutSession;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/**
 * Cross-feature tests: a command that updates one client feature must never erase the data
 * belonging to another feature stored on the same client.
 */
public class CrossFeaturePreservationTest {

    private static final PaymentRecord INITIAL_PAYMENT =
            PaymentRecord.fromText("150", "2026-01-15", Optional.of("Initial fee"));

    @TempDir
    public Path testFolder;

    @Test
    public void execute_contactEdit_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("edit 1 p/91234567", "contact");
    }

    @Test
    public void execute_remark_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("remark 1 r/Likes swimming", "remark");
    }

    @Test
    public void execute_diet_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("diet 1 t/2000 kcal", "dietPlan");
    }

    @Test
    public void execute_addExercise_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("addex 1 n/Squat s/3 r/10 w/60", "workoutPlan");
    }

    @Test
    public void execute_deleteExercise_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("delex 1 1", "workoutPlan");
    }

    @Test
    public void execute_clearPlan_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("clearplan 1", "workoutPlan");
    }

    @Test
    public void execute_setExerciseDay_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("dayex 1 1 Monday", "workoutPlan");
    }

    @Test
    public void execute_log_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("log 1 d/2020-01-17 ex/Bench press|40|3|8", "workoutSessions");
    }

    @Test
    public void execute_schedule_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("schedule 1 d/2099-03-16 tm/09:00 l/Studio", "trainingSessions");
    }

    @Test
    public void execute_meeting_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("meeting 1 d/2099-04-20 tm/10:00 l/Cafe", "trainingSessions");
    }

    @Test
    public void execute_health_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("health 1 h/Avoid running", "healthProfile");
    }

    @Test
    public void execute_dietaryRestriction_preservesEveryOtherFeature() throws Exception {
        assertFeaturePreserved("restriction 1 r/Peanut allergy", "dietaryRestrictions");
    }

    @Test
    public void saveAndReload_clientWithEveryFeature_preservesAllData() throws Exception {
        Model model = createModelWithRichClient();
        Person original = currentPerson(model);
        JsonAddressBookStorage storage =
                new JsonAddressBookStorage(testFolder.resolve("full-feature.json"));

        storage.saveAddressBook(model.getAddressBook());
        AddressBook reloaded = new AddressBook(storage.readAddressBook().orElseThrow());

        assertEquals(model.getAddressBook(), reloaded);
        assertEquals(original, reloaded.getPersonList().get(0));
        assertEquals(original.getTrainingSessions(), reloaded.getPersonList().get(0).getTrainingSessions());
        assertEquals(original.getWorkoutSessions(), reloaded.getPersonList().get(0).getWorkoutSessions());
        assertEquals(original.getMembershipAssignment(),
                reloaded.getPersonList().get(0).getMembershipAssignment());
        assertEquals(original.getHealthProfile(), reloaded.getPersonList().get(0).getHealthProfile());
        assertEquals(original.getDietaryRestrictions(),
                reloaded.getPersonList().get(0).getDietaryRestrictions());
        assertEquals(original.getPaymentRecords(),
                reloaded.getPersonList().get(0).getPaymentRecords());
    }

    @Test
    public void execute_deleteClient_removesOnlyThatClientsPayments() throws Exception {
        Model model = createModelWithRichClient();
        PaymentRecord otherPayment = PaymentRecord.fromText("20", "2026-02-01", Optional.empty());
        Person otherClient = new PersonBuilder()
                .withName("Other Client")
                .withPaymentRecords(List.of(otherPayment))
                .build();
        model.addPerson(otherClient);

        new AddressBookParser().parseCommand("delete 1").execute(model);

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(List.of(otherPayment),
                model.getAddressBook().getPersonList().get(0).getPaymentRecords());
    }

    @Test
    public void execute_clear_removesAllPayments() throws Exception {
        Model model = createModelWithRichClient();
        model.addPerson(new PersonBuilder().withName("Other Client").build());

        new AddressBookParser().parseCommand("clear").execute(model);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    /**
     * Runs {@code commandText} on a model whose single client carries every supported feature,
     * then asserts that every feature except the one named in {@code changed} is unchanged.
     */
    private void assertFeaturePreserved(String commandText, String changed) throws Exception {
        Model model = createModelWithRichClient();
        Person before = currentPerson(model);

        new AddressBookParser().parseCommand(commandText).execute(model);
        Person after = currentPerson(model);

        assertNotEquals(before, after, "Command should have changed the client: " + commandText);
        assertOtherFeaturesPreserved(before, after, Set.of(changed));
    }

    private void assertOtherFeaturesPreserved(Person before, Person after, Set<String> changed) {
        if (!changed.contains("contact")) {
            assertEquals(before.getName(), after.getName(), "name erased");
            assertEquals(before.getPhone(), after.getPhone(), "phone erased");
            assertEquals(before.getEmail(), after.getEmail(), "email erased");
            assertEquals(before.getAddress(), after.getAddress(), "address erased");
        }
        if (!changed.contains("remark")) {
            assertEquals(before.getRemark(), after.getRemark(), "remark erased");
        }
        if (!changed.contains("tags")) {
            assertEquals(before.getTags(), after.getTags(), "tags erased");
        }
        if (!changed.contains("goal")) {
            assertEquals(before.getGoal(), after.getGoal(), "goal erased");
        }
        if (!changed.contains("workoutPlan")) {
            assertEquals(before.getWorkoutPlan(), after.getWorkoutPlan(), "workout plan erased");
        }
        if (!changed.contains("dietPlan")) {
            assertEquals(before.getDietPlan(), after.getDietPlan(), "diet plan erased");
        }
        if (!changed.contains("trainingSessions")) {
            assertEquals(before.getTrainingSessions(), after.getTrainingSessions(), "training sessions erased");
        }
        if (!changed.contains("workoutSessions")) {
            assertEquals(before.getWorkoutSessions(), after.getWorkoutSessions(), "workout sessions erased");
        }
        if (!changed.contains("dietaryRestrictions")) {
            assertEquals(before.getDietaryRestrictions(), after.getDietaryRestrictions(),
                    "dietary restrictions erased");
        }
        if (!changed.contains("healthProfile")) {
            assertEquals(before.getHealthProfile(), after.getHealthProfile(), "health profile erased");
        }
        if (!changed.contains("membershipAssignment")) {
            assertEquals(before.getMembershipAssignment(), after.getMembershipAssignment(),
                    "membership assignment erased");
        }
        if (!changed.contains("paymentRecords")) {
            assertEquals(before.getPaymentRecords(), after.getPaymentRecords(), "payment records erased");
        }
    }

    private static Person currentPerson(Model model) {
        return model.getAddressBook().getPersonList().get(0);
    }

    private static Model createModelWithRichClient() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        MembershipPlan gold = new MembershipPlan(new MembershipName("Gold"), new BigDecimal("150"), 30);
        model.addMembershipPlan(gold);
        model.addPerson(createRichClient().withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.of(LocalDate.of(2099, 12, 30))))));
        return model;
    }

    private static Person createRichClient() {
        WorkoutLogEntry entry = new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
        WorkoutSession session = new WorkoutSession(
                LocalDate.of(2020, 1, 15), Optional.empty(), List.of(entry));
        WorkoutPlan workoutPlan = new WorkoutPlan(
                List.of(new Exercise("Bench Press", 3, 10, 40.0)));
        return new PersonBuilder()
                .withGoal("Build strength")
                .withTags("morning")
                .withWorkoutPlan(workoutPlan)
                .withDietPlan(new DietPlan("1800 kcal", "Lean protein"))
                .withDietaryRestrictions("Peanuts")
                .withHealthProfile("Asthma")
                .withWorkoutSessions(List.of(session))
                .withPaymentRecords(List.of(INITIAL_PAYMENT, INITIAL_PAYMENT))
                .build()
                .withTrainingSession(
                        new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"));
    }
}
