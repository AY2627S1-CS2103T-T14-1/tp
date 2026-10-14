package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path NO_GOAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("noGoalAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void jsonRoundTrip_personWithTrainingSession_preservesSession() throws Exception {
        AddressBook book = new AddressBook();
        Person client = new PersonBuilder(TypicalPersons.BENSON).withGoal("Build strength").build()
                .withTrainingSession(
                        new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"));
        book.addPerson(client);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        JsonSerializableAddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        assertEquals(book, loaded.toModelType());
    }

    @Test
    public void toModelType_fileWithoutGoalProperty_loadsClientsWithoutGoals() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NO_GOAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBook = dataFromFile.toModelType();
        assertEquals(1, addressBook.getPersonList().size());
        assertTrue(addressBook.getPersonList().get(0).getGoal().isEmpty());
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

}
