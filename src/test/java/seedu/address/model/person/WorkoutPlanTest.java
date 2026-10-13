package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class WorkoutPlanTest {

    private static final Exercise BENCH_PRESS = new Exercise("Bench Press", 3, 10, 60.0);
    private static final Exercise SQUAT = new Exercise("Squat", 5, 5, 100.0);

    @Test
    public void constructor_empty_success() {
        WorkoutPlan plan = new WorkoutPlan();
        assertTrue(plan.isEmpty());
        assertEquals(0, plan.size());
        assertTrue(plan.getExercises().isEmpty());
    }

    @Test
    public void constructor_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new WorkoutPlan(null));
    }

    @Test
    public void constructor_nullElement_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new WorkoutPlan(Arrays.asList((Exercise) null)));
        assertThrows(NullPointerException.class, () -> new WorkoutPlan(Arrays.asList(BENCH_PRESS, null)));
    }

    @Test
    public void constructor_list_defensiveCopy() {
        List<Exercise> source = new ArrayList<>(Arrays.asList(BENCH_PRESS));
        WorkoutPlan plan = new WorkoutPlan(source);
        assertEquals(1, plan.size());

        // Mutating the source list must not affect the plan.
        source.add(SQUAT);
        assertEquals(1, plan.size());
    }

    @Test
    public void constructor_legacyDayPrefixes_ordersSaturdayBeforeSunday() {
        Exercise sunday = new Exercise("Run", 1, 1, 0, "Sun");
        Exercise saturday = new Exercise("Squat", 3, 10, 80, "Sat");
        Exercise unspecified = new Exercise("Stretch", 1, 1, 0);
        WorkoutPlan plan = new WorkoutPlan(List.of(sunday, unspecified, saturday));
        assertEquals(List.of(saturday, sunday, unspecified), plan.getExercises());
    }

    @Test
    public void getExercises_modifyList_throwsUnsupportedOperationException() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(BENCH_PRESS));
        assertThrows(UnsupportedOperationException.class, () -> plan.getExercises().remove(0));
    }

    @Test
    public void isWithinDailyLimit_sevenExercisesOnEveryWeekday_success() {
        List<Exercise> exercises = Arrays.stream(Weekday.values())
                .flatMap(day -> IntStream.range(0, 7)
                        .mapToObj(index -> new Exercise(day + " " + index, 1, 1, 0, day.getDisplayName())))
                .toList();
        WorkoutPlan plan = new WorkoutPlan(exercises);
        for (Weekday day : Weekday.values()) {
            assertTrue(plan.isWithinDailyLimit(day.getDisplayName()));
            assertFalse(plan.addExercise(BENCH_PRESS.withDay(day.getDisplayName()))
                    .isWithinDailyLimit(day.getDisplayName()));
        }
    }

    @Test
    public void isWithinDailyLimit_legacyPrefixes_countAsSameWeekday() {
        List<Exercise> exercises = IntStream.range(0, 7)
                .mapToObj(index -> new Exercise("Exercise " + index, 1, 1, 0, "mOn"))
                .toList();
        WorkoutPlan plan = new WorkoutPlan(exercises).addExercise(BENCH_PRESS.withDay("Monday"));
        assertFalse(plan.isWithinDailyLimit("m"));
        assertTrue(plan.isWithinDailyLimit("Tuesday"));
    }

    @Test
    public void isWithinDailyLimit_unassignedExercises_noLimit() {
        WorkoutPlan plan = new WorkoutPlan(IntStream.range(0, 8)
                .mapToObj(index -> new Exercise("Exercise " + index, 1, 1, 0)).toList());
        assertTrue(plan.isWithinDailyLimit(""));
        assertTrue(plan.isWithinDailyLimit("Monday"));
    }

    @Test
    public void addExercise_null_throwsNullPointerException() {
        WorkoutPlan plan = new WorkoutPlan();
        assertThrows(NullPointerException.class, () -> plan.addExercise(null));
    }

    @Test
    public void addExercise_returnsNewPlan_originalUnchanged() {
        WorkoutPlan plan = new WorkoutPlan();
        WorkoutPlan updated = plan.addExercise(BENCH_PRESS);

        assertTrue(plan.isEmpty());
        assertEquals(1, updated.size());
        assertTrue(updated.getExercises().contains(BENCH_PRESS));
    }

    @Test
    public void removeExercise_null_throwsNullPointerException() {
        WorkoutPlan plan = new WorkoutPlan();
        assertThrows(NullPointerException.class, () -> plan.removeExercise(null));
    }

    @Test
    public void removeExercise_existingExercise_removedInNewPlan() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(BENCH_PRESS, SQUAT));
        WorkoutPlan updated = plan.removeExercise(BENCH_PRESS);

        assertEquals(2, plan.size());
        assertEquals(1, updated.size());
        assertFalse(updated.getExercises().contains(BENCH_PRESS));
        assertTrue(updated.getExercises().contains(SQUAT));
    }

    @Test
    public void removeExercise_nonExistentExercise_planUnchanged() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(BENCH_PRESS));
        WorkoutPlan updated = plan.removeExercise(SQUAT);
        assertEquals(plan, updated);
    }

    @Test
    public void isEmptyAndSize() {
        WorkoutPlan plan = new WorkoutPlan();
        assertTrue(plan.isEmpty());
        assertEquals(0, plan.size());

        plan = plan.addExercise(BENCH_PRESS);
        assertFalse(plan.isEmpty());
        assertEquals(1, plan.size());
    }

    @Test
    public void equals() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(BENCH_PRESS));

        // same object -> true
        assertTrue(plan.equals(plan));

        // null -> false
        assertFalse(plan.equals(null));

        // different type -> false
        assertFalse(plan.equals("plan"));

        // same values -> true
        assertTrue(plan.equals(new WorkoutPlan(Arrays.asList(
                new Exercise("Bench Press", 3, 10, 60.0)))));

        // different exercises -> false
        assertFalse(plan.equals(new WorkoutPlan(Arrays.asList(SQUAT))));

        // empty vs non-empty -> false
        assertFalse(new WorkoutPlan().equals(plan));
    }

    @Test
    public void hashCode_consistentWithEquals() {
        WorkoutPlan first = new WorkoutPlan(Arrays.asList(BENCH_PRESS));
        WorkoutPlan second = new WorkoutPlan(Arrays.asList(
                new Exercise("Bench Press", 3, 10, 60.0)));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toString_containsExercises() {
        WorkoutPlan plan = new WorkoutPlan(Arrays.asList(BENCH_PRESS));
        String result = plan.toString();
        assertTrue(result.contains(BENCH_PRESS.toString()));
    }
}
