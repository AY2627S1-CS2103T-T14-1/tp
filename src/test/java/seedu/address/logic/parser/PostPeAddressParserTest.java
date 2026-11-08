package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/** Keeps new address input consistent while preserving old oversized records. */
public class PostPeAddressParserTest {
    @TempDir
    public Path directory;

    @Test
    public void edit_addressBoundary_matchesAdd() throws Exception {
        String boundary = "\uD840\uDC00".repeat(200);
        new EditCommandParser().parse("1 a/" + boundary);
        assertThrows(ParseException.class, () -> new EditCommandParser().parse("1 a/" + boundary + "X"));
    }

    @Test
    public void legacyOversizedAddress_roundTripsWithoutDataLoss() throws Exception {
        AddressBook book = new AddressBook();
        String oldAddress = "Legacy address ".repeat(30);
        book.addPerson(new PersonBuilder().withAddress(oldAddress).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("legacy.json"));
        storage.saveAddressBook(book);
        assertEquals(oldAddress, storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getAddress().value);
    }
}
