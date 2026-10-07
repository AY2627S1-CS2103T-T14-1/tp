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

    private final LocalDateTime dateTime;
    private final String location;

    /**
     * Constructs a {@code TrainingSession} with a date, time, and meeting location.
     * A past date and time remains valid so saved sessions can still be loaded later.
     */
    public TrainingSession(LocalDateTime dateTime, String location) {
        this.dateTime = requireNonNull(dateTime);
        requireNonNull(location);
        if (location.isBlank()) {
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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TrainingSession otherSession)) {
            return false;
        }
        return dateTime.equals(otherSession.dateTime) && location.equals(otherSession.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, location);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("dateTime", dateTime)
                .add("location", location)
                .toString();
    }
}
