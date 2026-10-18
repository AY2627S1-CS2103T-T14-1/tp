package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MembershipStorageTest {

    private static MembershipPlan plan(String name, String price, int sessions) {
        return new MembershipPlan(new MembershipName(name), new BigDecimal(price), sessions);
    }

    @Test
    public void planAdapter_validPlan_roundTrip() throws Exception {
        MembershipPlan gold = plan("Gold", "150.50", 30);
        JsonAdaptedMembershipPlan adapted = new JsonAdaptedMembershipPlan(gold);
        assertEquals(gold, adapted.toModelType());
        String json = JsonUtil.toJsonString(adapted);
        assertEquals(gold, JsonUtil.fromJsonString(json, JsonAdaptedMembershipPlan.class).toModelType());
    }

    @Test
    public void planAdapter_missingFields_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedMembershipPlan(null,
                "150.00", 30).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedMembershipPlan("Gold",
                null, 30).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedMembershipPlan("Gold",
                "150.00", null).toModelType());
    }

    @Test
    public void planAdapter_invalidName_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, MembershipName.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold@", "150.00", 30).toModelType());
        assertThrows(IllegalValueException.class, MembershipName.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("...", "150.00", 30).toModelType());
        assertThrows(IllegalValueException.class, MembershipName.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("", "150.00", 30).toModelType());
    }

    @Test
    public void planAdapter_invalidPrice_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_PRICE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "0", 30).toModelType());
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_PRICE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "-5", 30).toModelType());
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_PRICE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "10.123", 30).toModelType());
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_PRICE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "abc", 30).toModelType());
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_PRICE_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "1,000.00", 30).toModelType());
    }

    @Test
    public void planAdapter_invalidSessions_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "150.00", 0).toModelType());
        assertThrows(IllegalValueException.class, MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS, () ->
                new JsonAdaptedMembershipPlan("Gold", "150.00", 1001).toModelType());
    }

    @Test
    public void assignmentAdapter_validRoundTrips() throws Exception {
        MembershipAssignment withoutExpiry =
                new MembershipAssignment(plan("Gold", "150", 30), Optional.empty());
        assertEquals(withoutExpiry,
                new JsonAdaptedMembershipAssignment(withoutExpiry).toModelType());

        MembershipAssignment withExpiry = new MembershipAssignment(plan("Gold", "150", 30),
                Optional.of(LocalDate.of(2026, 12, 30)));
        JsonAdaptedMembershipAssignment adapted = new JsonAdaptedMembershipAssignment(withExpiry);
        assertEquals(withExpiry, adapted.toModelType());
        String json = JsonUtil.toJsonString(adapted);
        assertEquals(withExpiry,
                JsonUtil.fromJsonString(json, JsonAdaptedMembershipAssignment.class).toModelType());
    }

    @Test
    public void assignmentAdapter_expiredDateLoadsSuccessfully() throws Exception {
        MembershipAssignment expired = new MembershipAssignment("gold", "Gold", 5,
                Optional.of(LocalDate.of(2020, 1, 1)));
        assertEquals(expired, new JsonAdaptedMembershipAssignment(expired).toModelType());
        JsonAdaptedMembershipAssignment fromJson = new JsonAdaptedMembershipAssignment(
                "gold", "Gold", 5, "2020-01-01");
        assertEquals(expired, fromJson.toModelType());
    }

    @Test
    public void assignmentAdapter_missingFields_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedMembershipAssignment(null, "Gold", 10, null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedMembershipAssignment("gold", null, 10, null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedMembershipAssignment("gold", "Gold", null, null).toModelType());
    }

    @Test
    public void assignmentAdapter_invalidSessionsAndExpiry_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedMembershipAssignment("gold", "Gold", -1, null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedMembershipAssignment("gold", "Gold", 1001, null).toModelType());
        assertThrows(IllegalValueException.class,
                JsonAdaptedMembershipAssignment.MESSAGE_INVALID_EXPIRY, () ->
                        new JsonAdaptedMembershipAssignment("gold", "Gold", 10, "30-12-2026")
                                .toModelType());
        assertThrows(IllegalValueException.class,
                JsonAdaptedMembershipAssignment.MESSAGE_INVALID_EXPIRY, () ->
                        new JsonAdaptedMembershipAssignment("gold", "Gold", 10, "not-a-date")
                                .toModelType());
    }

    @Test
    public void personAdapter_missingAssignment_loadsAsUnassigned() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(BENSON);
        assertTrue(adapted.toModelType().getMembershipAssignment().isEmpty());
    }

    @Test
    public void personAdapter_withAssignment_roundTrip() throws Exception {
        Person assigned = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment(plan("Gold", "150", 30),
                        Optional.of(LocalDate.of(2026, 12, 30)))));
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(assigned);
        assertEquals(assigned, adapted.toModelType());
        String json = JsonUtil.toJsonString(adapted);
        assertEquals(assigned, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void personAdapter_malformedAssignment_throwsIllegalValueException() {
        JsonAdaptedMembershipAssignment bad =
                new JsonAdaptedMembershipAssignment("gold", "Gold", 9999, null);
        Person base = BENSON;
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(base.getName().fullName,
                base.getPhone().value, base.getEmail().value, base.getAddress().value,
                base.getRemark().value, List.of(), List.of(), null, List.of(), null, List.of(), bad);
        assertThrows(IllegalValueException.class, adapted::toModelType);
    }

    @Test
    public void serializable_missingPlansField_loadsEmptyCatalog() throws Exception {
        String json = "{\"persons\": []}";
        JsonSerializableAddressBook loaded =
                JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        assertEquals(new AddressBook(), loaded.toModelType());
    }

    @Test
    public void serializable_duplicateNormalizedKeys_throwsIllegalValueException() {
        JsonSerializableAddressBook book = new JsonSerializableAddressBook(
                List.of(), List.of(
                        new JsonAdaptedMembershipPlan(plan("Gold", "150", 30)),
                        new JsonAdaptedMembershipPlan(plan("gold", "80", 12))));
        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_DUPLICATE_PLAN, book::toModelType);
    }

    @Test
    public void serializable_orphanReference_throwsIllegalValueException() {
        Person orphan = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment("gold", "Gold", 10, Optional.empty())));
        JsonSerializableAddressBook book = new JsonSerializableAddressBook(
                new AddressBookBuilderHelper().withPerson(orphan).build());
        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_UNKNOWN_PLAN_REFERENCE, book::toModelType);
    }

    @Test
    public void serializable_nullPlanEntry_throwsIllegalValueException() throws Exception {
        String json = "{\"persons\": [], \"membershipPlans\": [null]}";
        JsonSerializableAddressBook loaded =
                JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_NULL_PLAN, loaded::toModelType);
    }

    @Test
    public void serializable_inconsistentAssignmentName_throwsIllegalValueException() {
        AddressBook book = new AddressBook();
        book.addMembershipPlan(plan("Gold", "150", 30));
        Person mismatched = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment("gold", "Silver", 10, Optional.empty())));
        book.addPerson(mismatched);
        JsonSerializableAddressBook serializable = new JsonSerializableAddressBook(book);
        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_INCONSISTENT_PLAN_NAME, serializable::toModelType);
    }

    @Test
    public void serializable_matchingAssignmentName_loadsSuccessfully() throws Exception {
        AddressBook book = new AddressBook();
        book.addMembershipPlan(plan("Gold", "150", 30));
        Person assigned = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment("gold", "Gold", 10, Optional.empty())));
        book.addPerson(assigned);
        assertEquals(book, new JsonSerializableAddressBook(book).toModelType());
    }

    @Test
    public void serializable_expiredAssignment_loadsSuccessfully() throws Exception {
        AddressBook book = new AddressBook();
        book.addMembershipPlan(plan("Gold", "150", 30));
        Person expired = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment("gold", "Gold", 5,
                        Optional.of(LocalDate.of(2020, 1, 1)))));
        book.addPerson(expired);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        assertEquals(book, JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void serializable_orderPreservingRoundTrip() throws Exception {
        AddressBook book = new AddressBook();
        book.addMembershipPlan(plan("Gold", "150", 30));
        book.addMembershipPlan(plan("Silver", "80", 12));
        Person first = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment(plan("Gold", "150", 30), Optional.empty())));
        book.addPerson(first);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        AddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(book, loaded);
        assertEquals(List.of(plan("Gold", "150", 30), plan("Silver", "80", 12)),
                loaded.getMembershipPlanList());
    }

    /**
     * Minimal helper to build a serializable book from an in-memory book without JSON strings.
     */
    private static class AddressBookBuilderHelper {
        private final AddressBook inner = new AddressBook();

        AddressBookBuilderHelper withPerson(Person person) {
            inner.addPerson(person);
            return this;
        }

        AddressBook build() {
            return inner;
        }
    }
}
