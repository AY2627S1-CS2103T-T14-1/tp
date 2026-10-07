package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TrainingSessionTest {

    private static final LocalDateTime SESSION_TIME = LocalDateTime.of(2026, 10, 12, 18, 30);

    @Test
    public void constructor_nullDateTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TrainingSession(null, "Gym A"));
    }

    @Test
    public void constructor_nullLocation_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TrainingSession(SESSION_TIME, null));
    }

    @Test
    public void constructor_blankLocation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TrainingSession(SESSION_TIME, ""));
        assertThrows(IllegalArgumentException.class, () -> new TrainingSession(SESSION_TIME, "  \t  "));
    }

    @Test
    public void constructor_pastDateTime_success() {
        TrainingSession session = new TrainingSession(LocalDateTime.of(2020, 1, 1, 9, 0), "Gym A / Studio 1");

        assertEquals(LocalDateTime.of(2020, 1, 1, 9, 0), session.getDateTime());
        assertEquals("Gym A / Studio 1", session.getLocation());
    }

    @Test
    public void equals_sameDateTimeAndLocation_success() {
        TrainingSession session = new TrainingSession(SESSION_TIME, "Gym A");
        TrainingSession sameSession = new TrainingSession(SESSION_TIME, "Gym A");

        assertTrue(session.equals(session));
        assertTrue(session.equals(sameSession));
        assertEquals(session.hashCode(), sameSession.hashCode());
        assertFalse(session.equals(new TrainingSession(SESSION_TIME.plusHours(1), "Gym A")));
        assertFalse(session.equals(new TrainingSession(SESSION_TIME, "Gym B")));
        assertFalse(session.equals(null));
        assertFalse(session.equals("Gym A"));
    }
}
