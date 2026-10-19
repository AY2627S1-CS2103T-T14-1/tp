package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void dietPlan_defaultsToAbsentAndRejectsNullOptional() {
        assertTrue(ALICE.getDietPlan().isEmpty());
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getRemark(), ALICE.getTags(),
                ALICE.getWorkoutPlan(), null));
    }

    @Test
    public void dietPlan_affectsValueEqualityButNotIdentity() {
        DietPlan plan = new DietPlan("1800 kcal", "Lean protein");
        Person plannedAlice = new PersonBuilder(ALICE).withDietPlan(plan).build();
        Person copy = new PersonBuilder(plannedAlice).build();
        assertEquals(plan, plannedAlice.getDietPlan().orElseThrow());
        assertEquals(plannedAlice, copy);
        assertEquals(plannedAlice.hashCode(), copy.hashCode());
        assertTrue(ALICE.isSamePerson(plannedAlice));
        assertFalse(ALICE.equals(plannedAlice));
        assertFalse(plannedAlice.equals(new PersonBuilder(ALICE).withDietPlan(new DietPlan("2000 kcal")).build()));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different remark -> returns false
        editedAlice = new PersonBuilder(ALICE).withRemark("Likes to swim.").build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different workoutPlan -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withWorkoutPlan(ALICE.getWorkoutPlan().addExercise(
                        new Exercise("Bench Press", 3, 10, 60.0)))
                .build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void constructor_sevenArgsAndGetter() {
        Person person = new PersonBuilder().build();
        assertEquals(new WorkoutPlan(), person.getWorkoutPlan());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName()
                + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail()
                + ", address=" + ALICE.getAddress()
                + ", remark=" + ALICE.getRemark()
                + ", tags=" + ALICE.getTags()
                + ", workoutPlan=" + ALICE.getWorkoutPlan()
                + ", dietPlan=" + ALICE.getDietPlan()
                + ", dietaryRestrictions=" + ALICE.getDietaryRestrictions()
                + ", goal=" + ALICE.getGoal().orElse(null)
                + ", healthProfile=" + ALICE.getHealthProfile().orElse(null)
                + ", trainingSessions=" + ALICE.getTrainingSessions()
                + ", workoutSessions=" + ALICE.getWorkoutSessions()
                + ", membershipAssignment=" + ALICE.getMembershipAssignment()
                + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void goal_affectsValueEqualityButNotIdentity() {
        Person goalAlice = new PersonBuilder(ALICE).withGoal("Build strength").build();
        assertEquals(new Goal("Build strength"), goalAlice.getGoal().orElseThrow());
        assertFalse(ALICE.equals(goalAlice));
        assertTrue(ALICE.isSamePerson(goalAlice));
    }

    @Test
    public void workoutSessions_copyPreservesGoalAndValueEquality() {
        WorkoutSession session = new WorkoutSession(LocalDate.of(2020, 1, 15), Optional.empty(),
                List.of(new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10)));
        Person goalAlice = new PersonBuilder(ALICE).withGoal("Build strength").build();
        Person loggedAlice = new PersonBuilder(goalAlice).withWorkoutSessions(List.of(session)).build();
        Person copy = new PersonBuilder(loggedAlice).build();

        assertEquals(loggedAlice, copy);
        assertEquals(loggedAlice.hashCode(), copy.hashCode());
        assertEquals(goalAlice.getGoal(), copy.getGoal());
        assertEquals(List.of(session), copy.getWorkoutSessions());
        assertFalse(goalAlice.equals(loggedAlice));
        assertTrue(goalAlice.isSamePerson(loggedAlice));
    }

    @Test
    public void healthAndDietaryRestrictions_surviveIndependentUpdates() {
        Person person = new PersonBuilder().withDietaryRestrictions("Peanut allergy")
                .withHealthProfile("Avoid jumping").build();
        Person clearedHealth = person.withHealthProfile(null);
        assertEquals(person.getDietaryRestrictions(), clearedHealth.getDietaryRestrictions());
        assertTrue(clearedHealth.getHealthProfile().isEmpty());

        Person clearedRestrictions = person.withDietaryRestrictions(new DietaryRestrictions());
        assertEquals(person.getHealthProfile(), clearedRestrictions.getHealthProfile());
        assertEquals(person, new PersonBuilder(person).build());
    }

    @Test
    public void constructor_workoutSessions_defensivelyCopiesList() {
        WorkoutSession session = new WorkoutSession(LocalDate.of(2020, 1, 15), Optional.empty(),
                List.of(new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10)));
        List<WorkoutSession> sessions = new ArrayList<>(List.of(session));
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getRemark(), ALICE.getTags(), ALICE.getWorkoutPlan(), ALICE.getDietPlan(), null,
                List.of(), sessions);

        sessions.clear();

        assertEquals(List.of(session), person.getWorkoutSessions());
        assertThrows(UnsupportedOperationException.class, () -> person.getWorkoutSessions().clear());
    }
}
