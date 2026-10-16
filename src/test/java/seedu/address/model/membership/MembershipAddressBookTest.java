package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MembershipAddressBookTest {

    private static MembershipPlan plan(String name, String price, int sessions) {
        return new MembershipPlan(new MembershipName(name), new BigDecimal(price), sessions);
    }

    @Test
    public void addMembershipPlan_insertionOrderAndLookup() {
        AddressBook book = new AddressBook();
        MembershipPlan gold = plan("Gold", "150", 30);
        MembershipPlan silver = plan("Silver", "80", 12);
        book.addMembershipPlan(gold);
        book.addMembershipPlan(silver);
        assertEquals(List.of(gold, silver), book.getMembershipPlanList());
        assertTrue(book.hasMembershipPlan(plan("gold", "1", 1)));
        assertEquals(gold, book.findMembershipPlan(new MembershipName("GOLD")).orElseThrow());
        assertEquals(gold, book.findMembershipPlanByKey("gold").orElseThrow());
        assertTrue(book.findMembershipPlanByKey("bronze").isEmpty());
    }

    @Test
    public void addMembershipPlan_duplicateNormalizedKey_throwsIllegalArgumentException() {
        AddressBook book = new AddressBook();
        book.addMembershipPlan(plan("Gold", "150", 30));
        assertThrows(IllegalArgumentException.class, () ->
                book.addMembershipPlan(plan("gold", "80", 12)));
        assertThrows(IllegalArgumentException.class, () ->
                book.addMembershipPlan(plan("  Gold  ", "80", 12)));
        assertEquals(1, book.getMembershipPlanList().size());
    }

    @Test
    public void setMembershipPlans_duplicateKeys_throwsIllegalArgumentException() {
        AddressBook book = new AddressBook();
        assertThrows(IllegalArgumentException.class, () ->
                book.setMembershipPlans(List.of(plan("Gold", "10", 1), plan("GOLD", "20", 2))));
    }

    @Test
    public void removeMembershipPlan_unknownOrInUse_throwsIllegalArgumentException() {
        AddressBook book = new AddressBook();
        MembershipPlan gold = plan("Gold", "150", 30);
        assertThrows(IllegalArgumentException.class, () -> book.removeMembershipPlan(gold));
        book.addMembershipPlan(gold);
        Person assigned = new PersonBuilder(ALICE)
                .build().withMembershipAssignment(
                        Optional.of(new MembershipAssignment(gold, Optional.empty())));
        book.addPerson(assigned);
        assertTrue(book.isMembershipPlanInUse(gold));
        assertThrows(IllegalArgumentException.class, () -> book.removeMembershipPlan(gold));
        assertEquals(1, book.getMembershipPlanList().size());
    }

    @Test
    public void removeMembershipPlan_unused_success() {
        AddressBook book = new AddressBook();
        MembershipPlan gold = plan("Gold", "150", 30);
        MembershipPlan silver = plan("Silver", "80", 12);
        book.addMembershipPlan(gold);
        book.addMembershipPlan(silver);
        book.removeMembershipPlan(silver);
        assertEquals(List.of(gold), book.getMembershipPlanList());
        assertFalse(book.isMembershipPlanInUse(gold));
    }

    @Test
    public void resetData_copiesPlans() {
        AddressBook source = new AddressBook();
        source.addMembershipPlan(plan("Gold", "150", 30));
        AddressBook copy = new AddressBook(source);
        assertEquals(source, copy);
        copy.addMembershipPlan(plan("Silver", "80", 12));
        assertFalse(source.equals(copy));
        copy.resetData(source);
        assertEquals(source, copy);
    }

    @Test
    public void equals_includesPlanOrder() {
        AddressBook first = new AddressBook();
        AddressBook second = new AddressBook();
        first.addMembershipPlan(plan("Gold", "150", 30));
        first.addMembershipPlan(plan("Silver", "80", 12));
        second.addMembershipPlan(plan("Silver", "80", 12));
        second.addMembershipPlan(plan("Gold", "150", 30));
        assertFalse(first.equals(second));
        second = new AddressBook();
        second.addMembershipPlan(plan("Gold", "150", 30));
        second.addMembershipPlan(plan("Silver", "80", 12));
        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void modelManager_planOperations_doNotExposeMutableCollection() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertTrue(model.getMembershipPlanList().isEmpty());
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        assertTrue(model.hasMembershipPlan(gold));
        assertEquals(List.of(gold), model.getMembershipPlanList());
        assertThrows(UnsupportedOperationException.class, () ->
                model.getMembershipPlanList().remove(0));
        model.removeMembershipPlan(gold);
        assertFalse(model.hasMembershipPlan(gold));
    }

    @Test
    public void modelManager_copyConstructor_copiesPlans() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addMembershipPlan(plan("Gold", "150", 30));
        ModelManager copy = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertEquals(model, copy);
    }

    @Test
    public void person_assignmentAccessorsAndCopy() {
        Person unassigned = new PersonBuilder(ALICE).build();
        assertTrue(unassigned.getMembershipAssignment().isEmpty());
        MembershipAssignment assignment = new MembershipAssignment(plan("Gold", "150", 30),
                Optional.of(LocalDate.of(2026, 12, 30)));
        Person assigned = unassigned.withMembershipAssignment(Optional.of(assignment));
        assertEquals(Optional.of(assignment), assigned.getMembershipAssignment());
        assertEquals(unassigned.getName(), assigned.getName());
        Person cleared = assigned.withMembershipAssignment(Optional.empty());
        assertTrue(cleared.getMembershipAssignment().isEmpty());
        assertThrows(NullPointerException.class, () -> unassigned.withMembershipAssignment(null));
    }

    @Test
    public void person_equalityIncludesAssignment() {
        MembershipPlan gold = plan("Gold", "150", 30);
        Person base = new PersonBuilder(ALICE).build();
        Person withAssignment = base.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty())));
        Person sameAssignment = base.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty())));
        assertEquals(withAssignment, sameAssignment);
        assertEquals(withAssignment.hashCode(), sameAssignment.hashCode());
        assertFalse(base.equals(withAssignment));
    }

    @Test
    public void personBuilder_copiesAssignment() {
        MembershipPlan gold = plan("Gold", "150", 30);
        Person assigned = new PersonBuilder(BENSON).build().withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty())));
        assertEquals(assigned, new PersonBuilder(assigned).build());
    }
}
