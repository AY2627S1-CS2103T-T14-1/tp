package seedu.address.storage;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.TrainingSession;

/** Jackson-friendly version of a training session. */
class JsonAdaptedTrainingSession {
    private final String dateTime;
    private final String location;

    @JsonCreator
    JsonAdaptedTrainingSession(@JsonProperty("dateTime") String dateTime,
            @JsonProperty("location") String location) {
        this.dateTime = dateTime;
        this.location = location;
    }

    JsonAdaptedTrainingSession(TrainingSession source) {
        dateTime = source.getDateTime().toString();
        location = source.getLocation();
    }

    TrainingSession toModelType() throws IllegalValueException {
        if (dateTime == null || location == null) {
            throw new IllegalValueException("Training session date, time and location are required");
        }
        try {
            return new TrainingSession(LocalDateTime.parse(dateTime), location);
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new IllegalValueException("Invalid training session date, time or location");
        }
    }
}
