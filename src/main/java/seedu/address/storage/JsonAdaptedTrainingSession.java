package seedu.address.storage;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.TrainingSession.Kind;

/** Jackson-friendly version of a training session. */
class JsonAdaptedTrainingSession {
    private final String dateTime;
    private final String location;
    private final String kind;

    @JsonCreator
    JsonAdaptedTrainingSession(@JsonProperty("dateTime") String dateTime,
            @JsonProperty("location") String location,
            @JsonProperty("kind") String kind) {
        this.dateTime = dateTime;
        this.location = location;
        this.kind = kind;
    }

    /** Supports older saved sessions without a kind field. */
    JsonAdaptedTrainingSession(String dateTime, String location) {
        this(dateTime, location, null);
    }

    JsonAdaptedTrainingSession(TrainingSession source) {
        dateTime = source.getDateTime().toString();
        location = source.getLocation();
        kind = source.getKind().name();
    }

    TrainingSession toModelType() throws IllegalValueException {
        if (dateTime == null || location == null) {
            throw new IllegalValueException("Training session date, time and location are required");
        }
        try {
            Kind modelKind = kind == null ? Kind.TRAINING : Kind.valueOf(kind);
            return new TrainingSession(LocalDateTime.parse(dateTime), location, modelKind);
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new IllegalValueException("Invalid training session date, time or location");
        }
    }
}
