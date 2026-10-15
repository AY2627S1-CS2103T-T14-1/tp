package seedu.address.storage;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.WorkoutLogEntry;

/**
 * Jackson-friendly version of a workout log entry.
 */
class JsonAdaptedWorkoutLogEntry {

    private final String name;
    private final BigDecimal weightKg;
    private final Integer sets;
    private final Integer reps;

    /**
     * Constructs an adapter from stored exercise details.
     */
    @JsonCreator
    public JsonAdaptedWorkoutLogEntry(
            @JsonProperty("name") String name,
            @JsonProperty("weightKg") BigDecimal weightKg,
            @JsonProperty("sets") Integer sets,
            @JsonProperty("reps") Integer reps) {
        this.name = name;
        this.weightKg = weightKg;
        this.sets = sets;
        this.reps = reps;
    }

    /**
     * Converts a model entry into an adapter for saving.
     */
    public JsonAdaptedWorkoutLogEntry(WorkoutLogEntry source) {
        name = source.getName();
        weightKg = source.getWeightKg();
        sets = source.getSets();
        reps = source.getReps();
    }

    /**
     * Converts the stored entry into a validated model entry.
     *
     * @throws IllegalValueException if a required field is missing or invalid.
     */
    public WorkoutLogEntry toModelType() throws IllegalValueException {
        if (name == null || weightKg == null || sets == null || reps == null) {
            throw new IllegalValueException(
                    "Workout log entries must contain name, weightKg, sets, and reps.");
        }

        try {
            return new WorkoutLogEntry(name, weightKg, sets, reps);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
