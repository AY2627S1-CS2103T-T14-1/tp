package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutPlan;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_v14SpaceOnlyLocation_preservesRoster() throws Exception {
        // v1.4 wrote this location because String.isBlank() does not treat NBSP as blank.
        // Start with the app's normal JSON format, then substitute the location accepted by v1.4.
        AddressBook v14Roster = new AddressBook();
        v14Roster.addPerson(ALICE.withTrainingSession(
                new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "legacy location")));
        v14Roster.addPerson(seedu.address.testutil.TypicalPersons.BOB);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(v14Roster));
        String legacyJson = json.replace("legacy location", "\u00a0\u00a0\u00a0");
        assertFalse(json.equals(legacyJson));

        Path filePath = testFolder.resolve("v14-roster.json");
        Files.writeString(filePath, legacyJson);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals(2, loaded.getPersonList().size());
        assertEquals("\u00a0\u00a0\u00a0", loaded.getPersonList().get(0).getTrainingSessions().get(0).getLocation());

        // LogicManager saves after ordinary commands such as list. The migrated model
        // must remain intact when it is saved and loaded again.
        storage.saveAddressBook(loaded);
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(2, reloaded.getPersonList().size());
        assertEquals("\u00a0\u00a0\u00a0", reloaded.getPersonList().get(0).getTrainingSessions().get(0).getLocation());
    }

    @Test
    public void readAddressBook_nullExercise_throwsDataLoadingException() throws Exception {
        assertInvalidExercises("[null]");
        assertInvalidExercises("[{\"name\":\"Bench Press\",\"sets\":3,\"reps\":10,\"weightKg\":60},null]");
    }

    @Test
    public void readAddressBook_fractionalSetsOrReps_throwsDataLoadingException() throws Exception {
        assertInvalidExercises("[{\"name\":\"Bench Press\",\"sets\":3.9,\"reps\":10,\"weightKg\":60}]");
        assertInvalidExercises("[{\"name\":\"Bench Press\",\"sets\":3,\"reps\":10.7,\"weightKg\":60}]");
        assertInvalidExercises("[{\"name\":\"Bench Press\",\"sets\":3.9,\"reps\":10.7,\"weightKg\":60}]");
    }

    @Test
    public void readAddressBook_nonFiniteWeight_throwsDataLoadingException() throws Exception {
        for (String weight : new String[] {"\"Infinity\"", "\"-Infinity\"", "\"NaN\"", "1e309"}) {
            assertInvalidExercises("[{\"name\":\"Bench Press\",\"sets\":3,\"reps\":10,\"weightKg\":"
                    + weight + "}]");
        }
    }

    @Test
    public void readAddressBook_missingOrNullExercises_loadsEmptyPlan() throws Exception {
        for (String exercises : new String[] {"", ",\"exercises\":null", ",\"exercises\":[]"}) {
            Path filePath = testFolder.resolve("legacy.json");
            Files.writeString(filePath, addressBookJson(exercises));
            ReadOnlyAddressBook loaded = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();
            assertEquals(ALICE, loaded.getPersonList().get(0));
        }
    }

    @Test
    public void readAndSaveAddressBook_nonemptyWorkoutPlan_preservesExercises() throws Exception {
        WorkoutPlan plan = new WorkoutPlan().addExercise(new Exercise("Bench Press", 3, 10, 60.5))
                .addExercise(new Exercise("Push Up", 2, 12, 0));
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getRemark(), ALICE.getTags(), plan);
        AddressBook original = new AddressBook();
        original.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("workout.json"));
        storage.saveAddressBook(original);
        assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAndSaveAddressBook_goal_preservesGoal() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(new seedu.address.testutil.PersonBuilder(ALICE).withGoal("Build strength").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("goal.json"));

        storage.saveAddressBook(original);

        assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAddressBook_invalidRestrictions_rejectsWithoutChangingFile() throws Exception {
        List<List<String>> invalidLists = List.of(
                List.of("Peanut allergy", "Peanut allergy\t"),
                List.of("Peanut allergy", "Peanut allergy\n"),
                List.of("Peanut allergy", "PEANUT ALLERGY"),
                List.of("Peanut allergy", "Peanut allergy"),
                Collections.nCopies(11, "Peanut allergy"),
                List.of("Peanut allergy", ""),
                List.of("Peanut allergy", "a".repeat(101)),
                Arrays.asList("Peanut allergy", null),
                List.of("ΧΩΡΙΣ", "χωρις"),
                List.of("No dairy Σ", "NO DAIRY ς", "no dairy σ"),
                List.of("No dairy Σ", "NO DAIRY ς", "no dairy σ\t"));
        for (List<String> values : invalidLists) {
            Path path = writeRestrictions(values);
            String original = Files.readString(path);
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
            assertEquals(original, Files.readString(path));
        }
    }

    @Test
    public void readAndSaveAddressBook_validLegacySigmaCollision_keepsFirstAndUnrelatedData() throws Exception {
        Path path = writeRestrictions(List.of("Peanut allergy", "No dairy Σ", "NO DAIRY ς", "Halal meals"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        Person expected = new seedu.address.testutil.PersonBuilder(ALICE)
                .withDietaryRestrictions("Peanut allergy", "No dairy Σ", "Halal meals").build();
        assertEquals(List.of(expected), loaded.getPersonList());
        storage.saveAddressBook(loaded);
        assertEquals(loaded, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_tooManyPreviouslyValidRestrictions_rejectsBeforeMigration() throws Exception {
        List<String> values = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            values.add("Restriction " + i);
        }
        values.add("No dairy Σ");
        values.add("NO DAIRY ς");
        Path path = writeRestrictions(values);
        String original = Files.readString(path);
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
        assertEquals(original, Files.readString(path));
    }

    private Path writeRestrictions(List<String> values) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode person = (ObjectNode) mapper.readTree(JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE)));
        person.set("dietaryRestrictions", mapper.valueToTree(values));
        Path path = testFolder.resolve("restrictions.json");
        Files.writeString(path, "{\"persons\":[" + person + "]}");
        return path;
    }

    private void assertInvalidExercises(String exercises) throws Exception {
        Path filePath = testFolder.resolve("invalidWorkout.json");
        Files.writeString(filePath, addressBookJson(",\"exercises\":" + exercises));
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
    }

    private String addressBookJson(String exercisesProperty) throws Exception {
        String personJson = JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE))
                .replaceFirst("(?s),\\s*\"exercises\"\\s*:\\s*\\[\\s*\\]", "");
        int closingBrace = personJson.lastIndexOf('}');
        return "{\"persons\":[" + personJson.substring(0, closingBrace) + exercisesProperty + "}]}";
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
