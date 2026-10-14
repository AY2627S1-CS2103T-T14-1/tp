package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;

public class JsonAdaptedTrainingSessionTest {
    @Test
    public void toModelType_multipleSessions_roundTripsWithPerson() throws Exception {
        TrainingSession first = new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym");
        TrainingSession second = new TrainingSession(LocalDateTime.of(2099, 3, 16, 9, 0), "Studio");
        Person person = new Person(BENSON.getName(), BENSON.getPhone(), BENSON.getEmail(), BENSON.getAddress(),
                BENSON.getRemark(), BENSON.getTags(), BENSON.getWorkoutPlan(), BENSON.getDietPlan(),
                List.of(first, second));
        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    @Test
    public void toModelType_invalidSavedDate_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedTrainingSession("bad date", "Gym").toModelType());
    }
}
