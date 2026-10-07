package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Exercise;

/**
 * Jackson-friendly version of {@link Exercise}.
 */
class JsonAdaptedExercise {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Exercise's %s field is missing!";

    private final String name;
    private final Integer sets;
    private final Integer reps;
    private final Double weightKg;

    /**
     * Constructs a {@code JsonAdaptedExercise} with the given exercise details.
     */
    @JsonCreator
    public JsonAdaptedExercise(@JsonProperty("name") String name,
            @JsonProperty("sets") Integer sets,
            @JsonProperty("reps") Integer reps,
            @JsonProperty("weightKg") Double weightKg) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    /**
     * Converts a given {@code Exercise} into this class for Jackson use.
     */
    public JsonAdaptedExercise(Exercise source) {
        name = source.getName();
        sets = source.getSets();
        reps = source.getReps();
        weightKg = source.getWeightKg();
    }

    /**
     * Converts this Jackson-friendly adapted exercise object into the model's {@code Exercise} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted exercise.
     */
    public Exercise toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!Exercise.isValidName(name)) {
            throw new IllegalValueException(Exercise.MESSAGE_CONSTRAINTS_NAME);
        }

        if (sets == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "sets"));
        }
        if (!Exercise.isValidSets(sets)) {
            throw new IllegalValueException(Exercise.MESSAGE_CONSTRAINTS_SETS);
        }

        if (reps == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "reps"));
        }
        if (!Exercise.isValidReps(reps)) {
            throw new IllegalValueException(Exercise.MESSAGE_CONSTRAINTS_REPS);
        }

        if (weightKg == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "weightKg"));
        }
        if (!Exercise.isValidWeight(weightKg)) {
            throw new IllegalValueException(Exercise.MESSAGE_CONSTRAINTS_WEIGHT);
        }

        return new Exercise(name, sets, reps, weightKg);
    }
}
