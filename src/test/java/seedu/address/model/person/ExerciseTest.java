package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ExerciseTest {

    private static final String VALID_NAME = "Bench Press";
    private static final int VALID_SETS = 3;
    private static final int VALID_REPS = 10;
    private static final double VALID_WEIGHT = 60.0;

    @Test
    public void constructor_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Exercise(null, VALID_SETS, VALID_REPS, VALID_WEIGHT));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Exercise("", VALID_SETS, VALID_REPS, VALID_WEIGHT));
        assertThrows(IllegalArgumentException.class, () -> new Exercise(" ", VALID_SETS, VALID_REPS, VALID_WEIGHT));
        assertThrows(IllegalArgumentException.class, () ->
                new Exercise("   ", VALID_SETS, VALID_REPS, VALID_WEIGHT));
    }

    @Test
    public void constructor_invalidSets_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Exercise(VALID_NAME, 0, VALID_REPS, VALID_WEIGHT));
        assertThrows(IllegalArgumentException.class, () -> new Exercise(VALID_NAME, -1, VALID_REPS, VALID_WEIGHT));
    }

    @Test
    public void constructor_invalidReps_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Exercise(VALID_NAME, VALID_SETS, 0, VALID_WEIGHT));
        assertThrows(IllegalArgumentException.class, () -> new Exercise(VALID_NAME, VALID_SETS, -5, VALID_WEIGHT));
    }

    @Test
    public void constructor_invalidWeight_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, -0.1));
        assertThrows(IllegalArgumentException.class, () ->
                new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, -100.0));
    }

    @Test
    public void constructor_validInputs_success() {
        Exercise bodyweight = new Exercise("Push Up", 3, 12, 0);
        assertEquals("Push Up", bodyweight.getName());
        assertEquals(3, bodyweight.getSets());
        assertEquals(12, bodyweight.getReps());
        assertEquals(0, bodyweight.getWeightKg());
    }

    @Test
    public void isValidName() {
        assertThrows(NullPointerException.class, () -> Exercise.isValidName(null));

        assertFalse(Exercise.isValidName(""));
        assertFalse(Exercise.isValidName(" "));
        assertFalse(Exercise.isValidName("   "));

        assertTrue(Exercise.isValidName("Bench Press"));
        assertTrue(Exercise.isValidName("Squat"));
        assertTrue(Exercise.isValidName("  Deadlift  "));
    }

    @Test
    public void isValidSets() {
        assertFalse(Exercise.isValidSets(0));
        assertFalse(Exercise.isValidSets(-1));

        assertTrue(Exercise.isValidSets(1));
        assertTrue(Exercise.isValidSets(5));
    }

    @Test
    public void isValidReps() {
        assertFalse(Exercise.isValidReps(0));
        assertFalse(Exercise.isValidReps(-10));

        assertTrue(Exercise.isValidReps(1));
        assertTrue(Exercise.isValidReps(12));
    }

    @Test
    public void isValidWeight() {
        assertFalse(Exercise.isValidWeight(-0.1));
        assertFalse(Exercise.isValidWeight(-50.0));

        assertTrue(Exercise.isValidWeight(0));
        assertTrue(Exercise.isValidWeight(20.5));
    }

    @Test
    public void isSameExercise() {
        Exercise benchPress = new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);

        assertTrue(benchPress.isSameExercise(benchPress));

        assertFalse(benchPress.isSameExercise(null));

        // same name, different sets/reps/weight -> returns true
        assertTrue(benchPress.isSameExercise(new Exercise(VALID_NAME, 5, 5, 100.0)));

        // same name ignoring case -> returns true
        assertTrue(benchPress.isSameExercise(new Exercise("bench press", VALID_SETS, VALID_REPS, VALID_WEIGHT)));

        // different name -> returns false
        assertFalse(benchPress.isSameExercise(new Exercise("Squat", VALID_SETS, VALID_REPS, VALID_WEIGHT)));
    }

    @Test
    public void equals() {
        Exercise benchPress = new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);

        assertTrue(benchPress.equals(benchPress));

        assertFalse(benchPress.equals(null));

        assertFalse(benchPress.equals("Bench Press"));

        assertTrue(benchPress.equals(new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT)));

        assertFalse(benchPress.equals(new Exercise("Squat", VALID_SETS, VALID_REPS, VALID_WEIGHT)));
        assertFalse(benchPress.equals(new Exercise(VALID_NAME, 5, VALID_REPS, VALID_WEIGHT)));
        assertFalse(benchPress.equals(new Exercise(VALID_NAME, VALID_SETS, 5, VALID_WEIGHT)));
        assertFalse(benchPress.equals(new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, 100.0)));
    }

    @Test
    public void hashCode_consistentWithEquals() {
        Exercise first = new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);
        Exercise second = new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toString_containsFields() {
        Exercise benchPress = new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);
        String result = benchPress.toString();
        assertTrue(result.contains(VALID_NAME));
        assertTrue(result.contains(String.valueOf(VALID_SETS)));
        assertTrue(result.contains(String.valueOf(VALID_REPS)));
        assertTrue(result.contains(String.valueOf(VALID_WEIGHT)));
    }
}
