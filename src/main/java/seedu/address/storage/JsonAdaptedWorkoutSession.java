package seedu.address.storage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/**
 * Jackson-friendly version of a completed workout session.
 */
class JsonAdaptedWorkoutSession {

    private final String date;
    private final String time;
    private final List<JsonAdaptedWorkoutLogEntry> entries;

    /**
     * Constructs an adapter from stored session details.
     * A null time represents an unspecified session time.
     */
    @JsonCreator
    public JsonAdaptedWorkoutSession(
            @JsonProperty("date") String date,
            @JsonProperty("time") String time,
            @JsonProperty("entries") List<JsonAdaptedWorkoutLogEntry> entries) {
        this.date = date;
        this.time = time;
        this.entries = entries == null ? null : new ArrayList<>(entries);
    }

    /**
     * Converts a model session into an adapter for saving.
     */
    public JsonAdaptedWorkoutSession(WorkoutSession source) {
        date = source.getDate().toString();
        time = source.getTime().map(LocalTime::toString).orElse(null);
        entries = source.getEntries().stream()
                .map(JsonAdaptedWorkoutLogEntry::new)
                .collect(Collectors.toList());
    }

    /**
     * Converts the stored session into a validated model session.
     *
     * @throws IllegalValueException if a required field is missing or invalid.
     */
    public WorkoutSession toModelType() throws IllegalValueException {
        if (date == null || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalValueException("Session date must use yyyy-MM-dd.");
        }

        LocalDate modelDate;
        try {
            modelDate = LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException(date + " is not a valid calendar date.");
        }

        Optional<LocalTime> modelTime = Optional.empty();
        if (time != null) {
            if (!time.matches("([01][0-9]|2[0-3]):[0-5][0-9]")) {
                throw new IllegalValueException(WorkoutSession.MESSAGE_INVALID_TIME);
            }
            modelTime = Optional.of(LocalTime.parse(time));
        }

        if (entries == null || entries.isEmpty()) {
            throw new IllegalValueException(WorkoutSession.MESSAGE_MISSING_EXERCISES);
        }
        if (entries.size() > WorkoutSession.MAX_EXERCISE_ENTRIES) {
            throw new IllegalValueException(WorkoutSession.MESSAGE_TOO_MANY_EXERCISES);
        }

        List<WorkoutLogEntry> modelEntries = new ArrayList<>();
        for (JsonAdaptedWorkoutLogEntry entry : entries) {
            if (entry == null) {
                throw new IllegalValueException(
                        "Workout sessions must not contain null exercise entries.");
            }
            modelEntries.add(entry.toModelType());
        }

        try {
            return new WorkoutSession(modelDate, modelTime, modelEntries);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
