package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.BodyProgressEntry;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class JsonAdaptedBodyProgressTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2020-02-01T12:00:00Z"), ZoneOffset.UTC);
    private static final String BASELINE =
            "{\"date\":\"2020-01-01\",\"weightKg\":\"75.50\",\"heightCm\":\"175.00\"}";
    private static final String ENTRY = "{\"date\":\"2020-01-08\",\"weightKg\":\"74.80\",\"notes\":\"Breakfast\"}";
    @TempDir
    private Path temporaryDirectory;

    @Test
    void roundTrip_populatedPersonAndOtherFieldsSurviveRestart() throws Exception {
        BodyProgress progress = new BodyProgress(Optional.of(new BodyBaseline(LocalDate.of(2020, 1, 1),
                new BigDecimal("75.5"), new BigDecimal("175"))), List.of(
                        new BodyProgressEntry(LocalDate.of(2020, 1, 15), new BigDecimal("74.2"), Optional.empty()),
                        new BodyProgressEntry(LocalDate.of(2020, 1, 8), new BigDecimal("74.8"),
                                Optional.of("Breakfast"))));
        Person person = new PersonBuilder().withBodyProgress(progress).withTags("friend").build();
        AddressBook book = new AddressBook();
        book.addPerson(person);
        Path file = temporaryDirectory.resolve("clients.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file, CLOCK);
        storage.saveAddressBook(book);
        assertEquals(book, new AddressBook(storage.readAddressBook().orElseThrow()));
        String json = Files.readString(file);
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"75.50\""));
        assertEquals(progress, JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedBodyProgress(progress)),
                JsonAdaptedBodyProgress.class).toModelType(CLOCK));
    }

    @Test
    void legacyMissingNullAndEmptyTrackers_loadAsEmpty() throws Exception {
        for (String json : List.of("null", "{}", "{\"baseline\":null,\"entries\":null}")) {
            assertEquals(new BodyProgress(), adapter(json).toModelType(CLOCK));
        }
        Person legacy = new PersonBuilder().build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(legacy));
        assertEquals(legacy, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType(CLOCK));
        String withoutBody = json.replaceAll(
                "(?s),?\\s*\"bodyProgress\"\\s*:\\s*\\{\\s*\"entries\"\\s*:\\s*\\[\\s*]\\s*}", "");
        assertEquals(legacy, JsonUtil.fromJsonString(withoutBody, JsonAdaptedPerson.class).toModelType(CLOCK));
    }

    @Test
    void malformedTrackers_rejectTypesInvariantsAndFutureDates() throws Exception {
        List<String> invalid = List.of("1", "[]", "true", "\"tracker\"", "{\"entries\":{}}",
                "{\"entries\":[null]}", "{\"entries\":[" + ENTRY + "]}",
                tracker(BASELINE, ENTRY + "," + ENTRY), tracker(BASELINE, ENTRY.replace("2020-01-08", "2020-01-01")),
                tracker(BASELINE, ENTRY.replace("2020-01-08", "2020-02-02")),
                tracker(BASELINE.replace("\"75.50\"", "75.50"), ENTRY),
                tracker(BASELINE.replace("\"175.00\"", "true"), ENTRY),
                tracker(BASELINE.replace("2020-01-01", "0000-01-01"), ENTRY),
                tracker(BASELINE, ENTRY.replace("\"Breakfast\"", "false")),
                tracker(BASELINE, ENTRY.replace("Breakfast", " ")),
                tracker(BASELINE, ENTRY.replace("\"74.80\"", "null")),
                tracker(BASELINE, ENTRY.replace("74.80", "74.800")), tracker("[]", ENTRY));
        for (String json : invalid) {
            JsonAdaptedBodyProgress adapted = adapter(json);
            assertThrows(IllegalValueException.class, () -> adapted.toModelType(CLOCK), json);
        }
    }

    @Test
    void malformedBodyInFile_usesDataLoadingException() throws Exception {
        String personJson = JsonUtil.toJsonString(new JsonAdaptedPerson(new PersonBuilder().build()));
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode person =
                (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(personJson);
        person.set("bodyProgress", mapper.readTree(tracker(BASELINE, "null")));
        Path file = temporaryDirectory.resolve("invalid.json");
        Files.writeString(file, "{\"persons\":[" + person + "]}");
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(file, CLOCK).readAddressBook());
    }

    private static String tracker(String baseline, String entries) {
        return "{\"baseline\":" + baseline + ",\"entries\":[" + entries + "]}";
    }

    private static JsonAdaptedBodyProgress adapter(String json) throws Exception {
        // Construct directly so explicit JSON null also exercises the adapter's compatibility path.
        return new JsonAdaptedBodyProgress(new com.fasterxml.jackson.databind.ObjectMapper().readTree(json));
    }
}
