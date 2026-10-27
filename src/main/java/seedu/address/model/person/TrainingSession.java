package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a scheduled training session for a client.
 * Guarantees: the date and time and location are present, and the value is immutable.
 */
public final class TrainingSession {

    /** Distinguishes a training appointment from an ad-hoc client meeting. */
    public enum Kind {
        TRAINING, MEETING
    }

    private final LocalDateTime dateTime;
    private final String location;
    private final Kind kind;

    /**
     * Constructs a {@code TrainingSession} with a date, time, and meeting location.
     * A past date and time remains valid so saved sessions can still be loaded later.
     */
    public TrainingSession(LocalDateTime dateTime, String location) {
        this(dateTime, location, Kind.TRAINING);
    }

    /** Constructs a dated training appointment or client meeting. */
    public TrainingSession(LocalDateTime dateTime, String location, Kind kind) {
        this.dateTime = requireNonNull(dateTime);
        requireNonNull(location);
        this.kind = requireNonNull(kind);
        if (location.codePoints().allMatch(character -> Character.isWhitespace(character)
                || Character.isSpaceChar(character))) {
            throw new IllegalArgumentException("Location cannot be blank");
        }
        this.location = location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getLocation() {
        return location;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TrainingSession otherSession)) {
            return false;
        }
        return dateTime.equals(otherSession.dateTime)
                && location.equals(otherSession.location) && kind == otherSession.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, location, kind);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("dateTime", dateTime)
                .add("location", location)
                .add("kind", kind)
                .toString();
    }
}
