package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class WorkoutSessionTest {

    private static final LocalDate DATE = LocalDate.of(2020, 1, 15);
    private static final LocalTime TIME = LocalTime.of(18, 0);

    private static final WorkoutLogEntry SQUAT =
            new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
    private static final WorkoutLogEntry BENCH =
            new WorkoutLogEntry("Bench press", new BigDecimal("40"), 3, 8);

    @Test
    public void constructor_validSession_preservesDetailsAndOrder() {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT, BENCH));

        assertEquals(DATE, session.getDate());
        assertEquals(Optional.of(TIME), session.getTime());
        assertEquals(List.of(SQUAT, BENCH), session.getEntries());
    }

    @Test
    public void constructor_withoutTime_success() {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));

        assertTrue(session.getTime().isEmpty());
    }

    @Test
    public void constructor_today_success() {
        LocalDate today = LocalDate.now();
        WorkoutSession session = new WorkoutSession(
                today, Optional.empty(), List.of(SQUAT));

        assertEquals(today, session.getDate());
    }

    @Test
    public void constructor_futureDate_throwsIllegalArgumentException() {
        LocalDate futureDate = LocalDate.now().plusDays(2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new WorkoutSession(futureDate, Optional.empty(), List.of(SQUAT)));

        assertEquals(WorkoutSession.MESSAGE_FUTURE_DATE, exception.getMessage());
    }

    @Test
    public void constructor_timeBoundaries_success() {
        WorkoutSession midnight = new WorkoutSession(
                DATE, Optional.of(LocalTime.MIDNIGHT), List.of(SQUAT));
        WorkoutSession lastMinute = new WorkoutSession(
                DATE, Optional.of(LocalTime.of(23, 59)), List.of(SQUAT));

        assertEquals(Optional.of(LocalTime.MIDNIGHT), midnight.getTime());
        assertEquals(Optional.of(LocalTime.of(23, 59)), lastMinute.getTime());
    }

    @Test
    public void constructor_timeWithSecondsOrNanos_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new WorkoutSession(
                        DATE, Optional.of(LocalTime.of(18, 0, 1)), List.of(SQUAT)));

        assertThrows(IllegalArgumentException.class,
                () -> new WorkoutSession(
                        DATE, Optional.of(LocalTime.of(18, 0, 0, 1)), List.of(SQUAT)));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new WorkoutSession(null, Optional.empty(), List.of(SQUAT)));

        assertThrows(NullPointerException.class,
                () -> new WorkoutSession(DATE, null, List.of(SQUAT)));

        assertThrows(NullPointerException.class,
                () -> new WorkoutSession(DATE, Optional.empty(), null));

        assertThrows(NullPointerException.class,
                () -> new WorkoutSession(
                        DATE, Optional.empty(), Arrays.asList(SQUAT, null)));
    }

    @Test
    public void constructor_emptyEntries_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new WorkoutSession(DATE, Optional.empty(), List.of()));

        assertEquals(WorkoutSession.MESSAGE_MISSING_EXERCISES, exception.getMessage());
    }

    @Test
    public void constructor_thirtyEntries_success() {
        List<WorkoutLogEntry> entries = Collections.nCopies(30, SQUAT);
        WorkoutSession session = new WorkoutSession(DATE, Optional.empty(), entries);

        assertEquals(30, session.getEntries().size());
    }

    @Test
    public void constructor_thirtyOneEntries_throwsIllegalArgumentException() {
        List<WorkoutLogEntry> entries = Collections.nCopies(31, SQUAT);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new WorkoutSession(DATE, Optional.empty(), entries));

        assertEquals(WorkoutSession.MESSAGE_TOO_MANY_EXERCISES, exception.getMessage());
    }

    @Test
    public void constructor_mutatingOriginalList_doesNotChangeSession() {
        List<WorkoutLogEntry> originalEntries = new ArrayList<>(List.of(SQUAT));
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.empty(), originalEntries);

        originalEntries.clear();

        assertEquals(List.of(SQUAT), session.getEntries());
    }

    @Test
    public void getEntries_modification_throwsUnsupportedOperationException() {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));

        assertThrows(UnsupportedOperationException.class,
                () -> session.getEntries().add(BENCH));
    }

    @Test
    public void hasSameDateTime_sameKeyDifferentExercises_returnsTrue() {
        WorkoutSession first = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT));
        WorkoutSession second = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(BENCH));

        assertTrue(first.hasSameDateTime(second));
        assertFalse(first.equals(second));
    }

    @Test
    public void hasSameDateTime_bothTimesAbsent_returnsTrue() {
        WorkoutSession first = new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT));
        WorkoutSession second = new WorkoutSession(
                DATE, Optional.empty(), List.of(BENCH));

        assertTrue(first.hasSameDateTime(second));
    }

    @Test
    public void hasSameDateTime_differentKeys_returnsFalse() {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT));

        assertFalse(session.hasSameDateTime(new WorkoutSession(
                DATE.minusDays(1), Optional.of(TIME), List.of(SQUAT))));
        assertFalse(session.hasSameDateTime(new WorkoutSession(
                DATE, Optional.of(TIME.plusHours(1)), List.of(SQUAT))));
        assertFalse(session.hasSameDateTime(new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT))));
        assertFalse(session.hasSameDateTime(null));
    }

    @Test
    public void equals_sameValues_returnsTrueWithSameHashCode() {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT, BENCH));
        WorkoutSession copy = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT, BENCH));

        assertTrue(session.equals(session));
        assertEquals(session, copy);
        assertEquals(session.hashCode(), copy.hashCode());
    }

    @Test
    public void equals_differentValues_returnsFalse() {
        WorkoutSession session = new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT, BENCH));

        assertFalse(session.equals(null));
        assertFalse(session.equals("workout"));
        assertFalse(session.equals(new WorkoutSession(
                DATE.minusDays(1), Optional.of(TIME), List.of(SQUAT, BENCH))));
        assertFalse(session.equals(new WorkoutSession(
                DATE, Optional.empty(), List.of(SQUAT, BENCH))));
        assertFalse(session.equals(new WorkoutSession(
                DATE, Optional.of(TIME), List.of(SQUAT))));
        assertFalse(session.equals(new WorkoutSession(
                DATE, Optional.of(TIME), List.of(BENCH, SQUAT))));
    }
}
