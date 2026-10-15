package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests saving, loading, and validating stored workout sessions.
 */
public class WorkoutSessionStorageTest {

    private static final JsonAdaptedWorkoutLogEntry VALID_ENTRY =
            new JsonAdaptedWorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);

    @TempDir
    public Path testFolder;

    @Test
    public void saveAndRead_sessions_preserveAllDetails() throws Exception {
        WorkoutLogEntry squat = new WorkoutLogEntry(
                "Back squat", new BigDecimal("60"), 3, 10);
        WorkoutLogEntry bench = new WorkoutLogEntry(
                "Bench press", new BigDecimal("40.25"), 3, 8);

        WorkoutSession first = new WorkoutSession(
                LocalDate.of(2020, 1, 15), Optional.empty(), List.of(squat, bench));
        WorkoutSession second = new WorkoutSession(
                LocalDate.of(2020, 1, 15), Optional.of(LocalTime.of(18, 0)), List.of(bench));

        Person base = new PersonBuilder()
                .withGoal("Build strength")
                .withWorkoutPlan(new WorkoutPlan(
                        List.of(new Exercise("Bench Press", 3, 10, 40.0))))
                .withDietPlan(new DietPlan("2000 kcal", "More vegetables"))
                .build()
                .withTrainingSession(new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"));

        Person person = new Person(
                base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getRemark(), base.getTags(), base.getWorkoutPlan(), base.getDietPlan(),
                base.getGoal().orElse(null), base.getTrainingSessions(), List.of(first, second));

        AddressBook original = new AddressBook();
        original.addPerson(person);

        JsonAddressBookStorage storage =
                new JsonAddressBookStorage(testFolder.resolve("workout-sessions.json"));
        storage.saveAddressBook(original);

        AddressBook loaded = new AddressBook(storage.readAddressBook().orElseThrow());

        assertEquals(original, loaded);
        assertEquals(person.getGoal(), loaded.getPersonList().get(0).getGoal());
        assertEquals(person.getTrainingSessions(), loaded.getPersonList().get(0).getTrainingSessions());
        assertEquals(List.of(first, second),
                loaded.getPersonList().get(0).getWorkoutSessions());
    }

    @Test
    public void fromJson_missingSessions_loadsLegacyPerson() throws Exception {
        String json = """
                {
                  "name": "Legacy Client",
                  "phone": "91234567",
                  "email": "legacy@example.com",
                  "address": "123 Main Street",
                  "remark": "",
                  "tags": []
                }
                """;

        Person loaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();

        assertEquals("Legacy Client", loaded.getName().fullName);
        assertTrue(loaded.getWorkoutSessions().isEmpty());
        assertTrue(loaded.getGoal().isEmpty());
    }

    @Test
    public void fromJson_missingSessions_preservesLegacyGoal() throws Exception {
        String json = """
                {
                  "name": "Legacy Client",
                  "phone": "91234567",
                  "email": "legacy@example.com",
                  "address": "123 Main Street",
                  "remark": "",
                  "tags": [],
                  "goal": "Build strength"
                }
                """;

        Person loaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();

        assertEquals("Build strength", loaded.getGoal().orElseThrow().value);
        assertTrue(loaded.getWorkoutSessions().isEmpty());
    }

    @Test
    public void fromJson_missingGoal_preservesLegacySessions() throws Exception {
        String json = """
                {
                  "name": "Legacy Client",
                  "phone": "91234567",
                  "email": "legacy@example.com",
                  "address": "123 Main Street",
                  "remark": "",
                  "tags": [],
                  "workoutSessions": [{
                    "date": "2020-01-15",
                    "time": "18:00",
                    "entries": [{
                      "name": "Back squat",
                      "weightKg": 60,
                      "sets": 3,
                      "reps": 10
                    }]
                  }]
                }
                """;

        Person loaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        WorkoutSession expected = new WorkoutSession(LocalDate.of(2020, 1, 15),
                Optional.of(LocalTime.of(18, 0)),
                List.of(new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10)));

        assertTrue(loaded.getGoal().isEmpty());
        assertEquals(List.of(expected), loaded.getWorkoutSessions());
    }

    @Test
    public void toModelType_duplicateKeys_throwsIllegalValueException() {
        JsonAdaptedWorkoutSession first = session("2020-01-15", "18:00");
        JsonAdaptedWorkoutSession differentExercises = new JsonAdaptedWorkoutSession(
                "2020-01-15", "18:00",
                List.of(new JsonAdaptedWorkoutLogEntry(
                        "Bench press", new BigDecimal("40"), 3, 8)));

        assertThrows(IllegalValueException.class, () -> {
            personWithSessions(List.of(first, differentExercises)).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            personWithSessions(List.of(
                    session("2020-01-15", null),
                    session("2020-01-15", null))).toModelType();
        });
    }

    @Test
    public void toModelType_sameDateDifferentTimes_success() throws Exception {
        Person loaded = personWithSessions(List.of(
                session("2020-01-15", null),
                session("2020-01-15", "18:00"),
                session("2020-01-15", "19:00"))).toModelType();

        assertEquals(3, loaded.getWorkoutSessions().size());
        assertTrue(loaded.getWorkoutSessions().get(0).getTime().isEmpty());
        assertEquals(Optional.of(LocalTime.of(18, 0)),
                loaded.getWorkoutSessions().get(1).getTime());
        assertEquals(Optional.of(LocalTime.of(19, 0)),
                loaded.getWorkoutSessions().get(2).getTime());
    }

    @Test
    public void toModelType_invalidDateOrTime_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> {
            session("15-01-2020", null).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            session("2020-02-30", null).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            session(null, null).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            session(LocalDate.now().plusDays(2).toString(), null).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            session("2020-01-15", "24:00").toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            session("2020-01-15", "18:00:01").toModelType();
        });
    }

    @Test
    public void toModelType_invalidExercise_throwsIllegalValueException() {
        JsonAdaptedWorkoutLogEntry invalidEntry =
                new JsonAdaptedWorkoutLogEntry("Back squat", new BigDecimal("-1"), 3, 10);
        JsonAdaptedWorkoutSession invalidSession = new JsonAdaptedWorkoutSession(
                "2020-01-15", null, List.of(VALID_ENTRY, invalidEntry));

        assertThrows(IllegalValueException.class, () -> {
            personWithSessions(List.of(invalidSession)).toModelType();
        });
    }

    @Test
    public void toModelType_invalidEntryCount_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> {
            new JsonAdaptedWorkoutSession(
                    "2020-01-15", null, List.of()).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            new JsonAdaptedWorkoutSession(
                    "2020-01-15", null, Collections.nCopies(31, VALID_ENTRY)).toModelType();
        });
    }

    @Test
    public void toModelType_nullEntriesOrSessions_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> {
            new JsonAdaptedWorkoutSession(
                    "2020-01-15", null, null).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            new JsonAdaptedWorkoutSession(
                    "2020-01-15", null, Collections.singletonList(null)).toModelType();
        });

        assertThrows(IllegalValueException.class, () -> {
            personWithSessions(Collections.singletonList(null)).toModelType();
        });
    }

    private JsonAdaptedWorkoutSession session(String date, String time) {
        return new JsonAdaptedWorkoutSession(date, time, List.of(VALID_ENTRY));
    }

    private JsonAdaptedPerson personWithSessions(List<JsonAdaptedWorkoutSession> sessions) {
        Person base = new PersonBuilder().build();

        return new JsonAdaptedPerson(
                base.getName().fullName, base.getPhone().value, base.getEmail().value,
                base.getAddress().value, base.getRemark().value,
                List.of(), List.of(), null, null, null, sessions);
    }
}
