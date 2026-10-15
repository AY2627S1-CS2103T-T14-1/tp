package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a completed workout session for a client.
 * Guarantees: immutable; contains between 1 and 30 valid exercise entries.
 */
public final class WorkoutSession {

    public static final int MAX_EXERCISE_ENTRIES = 30;

    public static final String MESSAGE_FUTURE_DATE =
            "A completed workout session cannot have a future date.";
    public static final String MESSAGE_INVALID_TIME =
            "Session time must use 24-hour HH:mm format, for example t/18:00.";
    public static final String MESSAGE_MISSING_EXERCISES =
            "At least one exercise entry is required. Use ex/EXERCISE|WEIGHT|SETS|REPS.";
    public static final String MESSAGE_TOO_MANY_EXERCISES =
            "A workout session may contain at most 30 exercise entries.";

    private final LocalDate date;
    private final Optional<LocalTime> time;
    private final List<WorkoutLogEntry> entries;

    /**
     * Constructs a validated workout session.
     *
     * @param date The session date, which must not be in the future.
     * @param time The optional session time, with minute precision.
     * @param entries Between 1 and 30 exercise entries, in the recorded order.
     * @throws NullPointerException if an argument or exercise entry is null.
     * @throws IllegalArgumentException if a session constraint is violated.
     */
    public WorkoutSession(LocalDate date, Optional<LocalTime> time,
                          List<WorkoutLogEntry> entries) {
        requireAllNonNull(date, time, entries);
        checkArgument(!date.isAfter(LocalDate.now()), MESSAGE_FUTURE_DATE);
        checkArgument(time.isEmpty()
                        || (time.get().getSecond() == 0 && time.get().getNano() == 0),
                MESSAGE_INVALID_TIME);
        checkArgument(!entries.isEmpty(), MESSAGE_MISSING_EXERCISES);
        checkArgument(entries.size() <= MAX_EXERCISE_ENTRIES, MESSAGE_TOO_MANY_EXERCISES);

        this.date = date;
        this.time = time;
        this.entries = List.copyOf(entries);
    }

    public LocalDate getDate() {
        return date;
    }

    public Optional<LocalTime> getTime() {
        return time;
    }

    /**
     * Returns an unmodifiable list of exercises in their recorded order.
     */
    public List<WorkoutLogEntry> getEntries() {
        return entries;
    }

    /**
     * Returns true if both sessions have the same date and optional time.
     * Use this to detect duplicate sessions belonging to the same client.
     * Exercise details do not affect this comparison.
     */
    public boolean hasSameDateTime(WorkoutSession other) {
        return other != null
                && date.equals(other.date)
                && time.equals(other.time);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof WorkoutSession otherSession)) {
            return false;
        }

        return date.equals(otherSession.date)
                && time.equals(otherSession.time)
                && entries.equals(otherSession.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, entries);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("date", date)
                .add("time", time)
                .add("entries", entries)
                .toString();
    }
}
