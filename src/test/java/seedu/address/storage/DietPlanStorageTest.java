package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class DietPlanStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    public void saveAndRead_preservesClientsWithAndWithoutNotes() throws Exception {
        AddressBook book = new AddressBook();
        Person noPlan = new PersonBuilder().withName("Amy").build();
        Person targetOnly = new PersonBuilder().withName("Bob").withDietPlan(new DietPlan("1800 kcal/day")).build();
        Person withNotes = new PersonBuilder().withName("Clara")
                .withDietPlan(new DietPlan("2000 kcal", "Halal meals;  avoid shellfish")).build();
        book.addPerson(noPlan);
        book.addPerson(targetOnly);
        book.addPerson(withNotes);
        Path path = testFolder.resolve("plans.json");
        new JsonAddressBookStorage(path).saveAddressBook(book);
        assertEquals(book, new AddressBook(new JsonAddressBookStorage(path).readAddressBook().orElseThrow()));

        Person replacement = new PersonBuilder(withNotes).withDietPlan(new DietPlan("2200 kcal")).build();
        book.setPerson(withNotes, replacement);
        new JsonAddressBookStorage(path).saveAddressBook(book);
        assertEquals(book, new AddressBook(new JsonAddressBookStorage(path).readAddressBook().orElseThrow()));
    }

    @Test
    public void read_legacyOrNullPlan_returnsClientWithoutPlan() throws Exception {
        for (String field : new String[] {"", ",\"dietPlan\":null"}) {
            Path path = writeClient(field);
            assertTrue(new JsonAddressBookStorage(path).readAddressBook().orElseThrow()
                    .getPersonList().get(0).getDietPlan().isEmpty());
        }
    }

    @Test
    public void read_invalidPlan_throwsDataLoadingException() throws Exception {
        String[] invalidPlans = {"{}", "{\"target\":null}", "{\"target\":\"!!!\"}",
            "{\"target\":\"1800 kcal\",\"notes\":\" \"}", "{\"target\":1800}", "[]", "false"};
        for (String plan : invalidPlans) {
            Path path = writeClient(",\"dietPlan\":" + plan);
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
        }
    }

    @Test
    public void deleteClient_removesOwnedPlanFromSavedData() throws Exception {
        AddressBook book = new AddressBook();
        Person person = new PersonBuilder().withDietPlan(new DietPlan("1800 kcal")).build();
        book.addPerson(person);
        Path path = testFolder.resolve("deleted.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        storage.saveAddressBook(book);
        book.removePerson(person);
        storage.saveAddressBook(book);
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertTrue(!Files.readString(path).contains("1800 kcal"));
    }

    private Path writeClient(String optionalField) throws Exception {
        String json = "{\"persons\":[{\"name\":\"Amy\",\"phone\":\"85355255\",\"email\":\"amy@example.com\","
                + "\"address\":\"Jurong\",\"remark\":\"\",\"tags\":[]" + optionalField + "}]}";
        Path path = testFolder.resolve("input.json");
        Files.writeString(path, json);
        return path;
    }
}
