package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

/** Exercises real JSON files, including values saved before day normalization. */
public class WorkoutPlanStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    public void saveAndRead_defaultAndExplicitExerciseValues_preservesPlan() throws Exception {
        WorkoutPlan plan = new WorkoutPlan(List.of(
                new Exercise("Stretch", 1, 1, 0),
                new Exercise("Press", 1, 8, 25.5),
                new Exercise("Squat", 4, 1, 0, "Monday")));
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withWorkoutPlan(plan).build());
        Path file = testFolder.resolve("workout.json");
        new JsonAddressBookStorage(file).saveAddressBook(book);

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(book, new AddressBook(loaded));
        assertEquals(plan, loaded.getPersonList().get(0).getWorkoutPlan());
    }

    @Test
    public void readAndResave_legacyUnspecifiedDays_normalizesWithoutLosingExercises() throws Exception {
        String json = """
                {"persons":[{"name":"Amy","phone":"85355255","email":"amy@example.com",
                "address":"Jurong","remark":"","tags":[],"exercises":[
                {"name":"Missing","sets":1,"reps":1,"weightKg":0},
                {"name":"Null","sets":2,"reps":3,"weightKg":0,"day":null},
                {"name":"Empty","sets":3,"reps":4,"weightKg":0,"day":""},
                {"name":"Lowercase","sets":4,"reps":5,"weightKg":0,"day":"day not specified"},
                {"name":"Heading","sets":5,"reps":6,"weightKg":0,"day":"Day not specified"},
                {"name":"Padded","sets":6,"reps":7,"weightKg":0,"day":" DAY NOT SPECIFIED "},
                {"name":"Monday Squat","sets":4,"reps":8,"weightKg":80,"day":"Monday"}
                ]}]}
                """;
        Path file = testFolder.resolve("legacy.json");
        Files.writeString(file, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        WorkoutPlan expected = new WorkoutPlan(List.of(
                new Exercise("Monday Squat", 4, 8, 80, "Monday"),
                new Exercise("Missing", 1, 1, 0), new Exercise("Null", 2, 3, 0),
                new Exercise("Empty", 3, 4, 0), new Exercise("Lowercase", 4, 5, 0),
                new Exercise("Heading", 5, 6, 0), new Exercise("Padded", 6, 7, 0)));
        assertEquals(expected, loaded.getPersonList().get(0).getWorkoutPlan());

        storage.saveAddressBook(loaded);
        assertFalse(Files.readString(file).toLowerCase(java.util.Locale.ROOT).contains("day not specified"));
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(file).readAddressBook().orElseThrow();
        assertEquals(expected, reloaded.getPersonList().get(0).getWorkoutPlan());
        assertEquals(new AddressBook(loaded), new AddressBook(reloaded));
    }
}
