package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class WorkoutLogEntryTest {

    @Test
    public void constructor_validEntry_preservesValues() {
        WorkoutLogEntry entry =
                new WorkoutLogEntry("Back squat", new BigDecimal("60.25"), 3, 10);

        assertEquals("Back squat", entry.getName());
        assertEquals(new BigDecimal("60.25"), entry.getWeightKg());
        assertEquals(3, entry.getSets());
        assertEquals(10, entry.getReps());
    }

    @Test
    public void constructor_boundaryValues_success() {
        WorkoutLogEntry minimum =
                new WorkoutLogEntry("A", BigDecimal.ZERO, 1, 1);
        WorkoutLogEntry maximum =
                new WorkoutLogEntry("A".repeat(50), new BigDecimal("1000"), 100, 1000);

        assertEquals(BigDecimal.ZERO, minimum.getWeightKg());
        assertEquals(1, minimum.getSets());
        assertEquals(1, minimum.getReps());
        assertEquals(0, new BigDecimal("1000").compareTo(maximum.getWeightKg()));
        assertEquals(100, maximum.getSets());
        assertEquals(1000, maximum.getReps());
    }

    @Test
    public void constructor_nullValues_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new WorkoutLogEntry(null, BigDecimal.ZERO, 3, 10));
        assertThrows(NullPointerException.class, () ->
                new WorkoutLogEntry("Squat", null, 3, 10));
    }

    @Test
    public void constructor_invalidNames_throwsIllegalArgumentException() {
        String[] invalidNames = {"", " ", "123", "()", "A".repeat(51),
            "Squat|Bench", "Squat!", "Squat\nBench", "Squat\tBench"};

        for (String name : invalidNames) {
            assertFalse(WorkoutLogEntry.isValidName(name));
            assertThrows(IllegalArgumentException.class, () ->
                    new WorkoutLogEntry(name, BigDecimal.ZERO, 3, 10));
        }
    }

    @Test
    public void isValidName_supportedCharacters_returnsTrue() {
        assertTrue(WorkoutLogEntry.isValidName("Push-up"));
        assertTrue(WorkoutLogEntry.isValidName("Farmer's walk"));
        assertTrue(WorkoutLogEntry.isValidName("Squat (barbell)"));
        assertTrue(WorkoutLogEntry.isValidName("1-leg squat"));
        assertTrue(WorkoutLogEntry.isValidName("Élévation"));
    }

    @Test
    public void constructor_invalidWeights_throwsIllegalArgumentException() {
        String[] invalidWeights = {"-0.01", "1000.01", "0.001", "60.123"};

        for (String value : invalidWeights) {
            BigDecimal weight = new BigDecimal(value);
            assertFalse(WorkoutLogEntry.isValidWeight(weight));
            assertThrows(IllegalArgumentException.class, () ->
                    new WorkoutLogEntry("Squat", weight, 3, 10));
        }
    }

    @Test
    public void constructor_invalidSets_throwsIllegalArgumentException() {
        int[] invalidSets = {-1, 0, 101};

        for (int sets : invalidSets) {
            assertFalse(WorkoutLogEntry.isValidSets(sets));
            assertThrows(IllegalArgumentException.class, () ->
                    new WorkoutLogEntry("Squat", BigDecimal.ZERO, sets, 10));
        }
    }

    @Test
    public void constructor_invalidReps_throwsIllegalArgumentException() {
        int[] invalidReps = {-1, 0, 1001};

        for (int reps : invalidReps) {
            assertFalse(WorkoutLogEntry.isValidReps(reps));
            assertThrows(IllegalArgumentException.class, () ->
                    new WorkoutLogEntry("Squat", BigDecimal.ZERO, 3, reps));
        }
    }

    @Test
    public void equals_sameValuesDifferentWeightScales_returnsTrue() {
        WorkoutLogEntry entry =
                new WorkoutLogEntry("Squat", new BigDecimal("60"), 3, 10);
        WorkoutLogEntry equivalent =
                new WorkoutLogEntry("Squat", new BigDecimal("60.00"), 3, 10);

        assertEquals(entry, equivalent);
        assertEquals(equivalent, entry);
        assertEquals(entry.hashCode(), equivalent.hashCode());
    }

    @Test
    public void equals() {
        WorkoutLogEntry entry =
                new WorkoutLogEntry("Squat", new BigDecimal("60"), 3, 10);

        assertTrue(entry.equals(entry));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("Squat"));

        assertFalse(entry.equals(
                new WorkoutLogEntry("Bench press", new BigDecimal("60"), 3, 10)));
        assertFalse(entry.equals(
                new WorkoutLogEntry("Squat", new BigDecimal("61"), 3, 10)));
        assertFalse(entry.equals(
                new WorkoutLogEntry("Squat", new BigDecimal("60"), 4, 10)));
        assertFalse(entry.equals(
                new WorkoutLogEntry("Squat", new BigDecimal("60"), 3, 11)));
    }
}
