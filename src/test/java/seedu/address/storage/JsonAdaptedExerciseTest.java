package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedExercise.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Exercise;

public class JsonAdaptedExerciseTest {
    private static final String VALID_NAME = "Bench Press";
    private static final int VALID_SETS = 3;
    private static final int VALID_REPS = 10;
    private static final double VALID_WEIGHT = 60.5;

    private static final String INVALID_NAME = " ";
    private static final int INVALID_SETS = 0;
    private static final int INVALID_REPS = -1;
    private static final double INVALID_WEIGHT = -5.0;

    @Test
    public void toModelType_validExerciseDetails_returnsExercise() throws Exception {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);
        assertEquals(new Exercise(VALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT), exercise.toModelType());
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(null, VALID_SETS, VALID_REPS, VALID_WEIGHT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "name");
        assertThrows(IllegalValueException.class, expectedMessage, exercise::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(INVALID_NAME, VALID_SETS, VALID_REPS, VALID_WEIGHT);
        assertThrows(IllegalValueException.class, Exercise.MESSAGE_CONSTRAINTS_NAME, exercise::toModelType);
    }

    @Test
    public void toModelType_nullSets_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, null, VALID_REPS, VALID_WEIGHT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "sets");
        assertThrows(IllegalValueException.class, expectedMessage, exercise::toModelType);
    }

    @Test
    public void toModelType_invalidSets_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, INVALID_SETS, VALID_REPS, VALID_WEIGHT);
        assertThrows(IllegalValueException.class, Exercise.MESSAGE_CONSTRAINTS_SETS, exercise::toModelType);
    }

    @Test
    public void toModelType_nullReps_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, VALID_SETS, null, VALID_WEIGHT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "reps");
        assertThrows(IllegalValueException.class, expectedMessage, exercise::toModelType);
    }

    @Test
    public void toModelType_invalidReps_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, VALID_SETS, INVALID_REPS, VALID_WEIGHT);
        assertThrows(IllegalValueException.class, Exercise.MESSAGE_CONSTRAINTS_REPS, exercise::toModelType);
    }

    @Test
    public void toModelType_nullWeight_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, VALID_SETS, VALID_REPS, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "weightKg");
        assertThrows(IllegalValueException.class, expectedMessage, exercise::toModelType);
    }

    @Test
    public void toModelType_invalidWeight_throwsIllegalValueException() {
        JsonAdaptedExercise exercise =
                new JsonAdaptedExercise(VALID_NAME, VALID_SETS, VALID_REPS, INVALID_WEIGHT);
        assertThrows(IllegalValueException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, exercise::toModelType);
    }
}
