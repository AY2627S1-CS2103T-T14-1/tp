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
        this(dateTime, location, kind, false);
    }

    private TrainingSession(LocalDateTime dateTime, String location, Kind kind, boolean fromSavedData) {
        this.dateTime = requireNonNull(dateTime);
        requireNonNull(location);
        this.kind = requireNonNull(kind);
        boolean isBlank = fromSavedData ? location.isBlank()
                : location.codePoints().allMatch(character -> Character.isWhitespace(character)
                        || Character.isSpaceChar(character));
        if (isBlank) {
            throw new IllegalArgumentException("Location cannot be blank");
        }
        this.location = location;
    }

    /**
     * Restores an appointment written by an older version of the app. Earlier versions
     * allowed locations made only of Unicode space characters, so those existing
     * appointments must remain readable even though new commands reject them.
     */
    public static TrainingSession fromSavedData(LocalDateTime dateTime, String location, Kind kind) {
        return new TrainingSession(dateTime, location, kind, true);
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
