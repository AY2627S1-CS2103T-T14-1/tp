package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AssignRemoveAdjustMembershipCommandTest {

    private Model model;
    private MembershipPlan gold;

    private static MembershipPlan plan(String name, String price, int sessions) {
        return new MembershipPlan(new MembershipName(name), new BigDecimal(price), sessions);
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
    }

    private Person assignGold(Index index, Optional<LocalDate> expiry) throws Exception {
        Person client = model.getFilteredPersonList().get(index.getZeroBased());
        Person assigned = client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, expiry)));
        model.setPerson(client, assigned);
        return assigned;
    }

    @Test
    public void execute_assignWithoutExpiry_success() {
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        AssignMembershipPlanCommand command = new AssignMembershipPlanCommand(
                new MembershipName("Gold"), INDEX_FIRST_PERSON, Optional.empty());
        Person assigned = client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty())));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(client, assigned);
        String expectedMessage = String.format("Assigned Gold to %s; Sessions remaining: 30.",
                client.getName());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_assignWithExpiry_success() {
        LocalDate expiry = LocalDate.of(2026, 12, 30);
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        AssignMembershipPlanCommand command = new AssignMembershipPlanCommand(
                new MembershipName("gold"), INDEX_FIRST_PERSON, Optional.of(expiry));
        Person assigned = client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.of(expiry))));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(client, assigned);
        String expectedMessage = String.format("Assigned Gold to %s; Sessions remaining: 30; Expiry: %s.",
                client.getName(), expiry);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_assignMissingPlan_failureNoMutation() {
        assertCommandFailure(new AssignMembershipPlanCommand(new MembershipName("Silver"),
                INDEX_FIRST_PERSON, Optional.empty()), model, "No membership plan named Silver exists.");
    }

    @Test
    public void execute_assignInvalidIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new AssignMembershipPlanCommand(new MembershipName("Gold"),
                outOfBounds, Optional.empty()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_assignAlreadyAssigned_failureNoMutation() throws Exception {
        assignGold(INDEX_FIRST_PERSON, Optional.empty());
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = String.format("%s already has an assigned membership plan.",
                client.getName());
        assertCommandFailure(new AssignMembershipPlanCommand(new MembershipName("Gold"),
                INDEX_FIRST_PERSON, Optional.empty()), model, expectedMessage);
        assertCommandFailure(new AssignMembershipPlanCommand(new MembershipName("Gold"),
                INDEX_FIRST_PERSON, Optional.of(LocalDate.of(2027, 1, 1))), model, expectedMessage);
    }

    @Test
    public void execute_assignFilteredList_usesDisplayedIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person shown = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        AssignMembershipPlanCommand command = new AssignMembershipPlanCommand(
                new MembershipName("Gold"), INDEX_FIRST_PERSON, Optional.empty());
        Person assigned = shown.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty())));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        Person expectedShown = expectedModel.getFilteredPersonList().get(0);
        expectedModel.setPerson(expectedShown, assigned);
        String expectedMessage = String.format("Assigned Gold to %s; Sessions remaining: 30.",
                shown.getName());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeUnassigned_failure() {
        assertCommandFailure(new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON), model,
                "This client has no membership plan to remove or adjust.");
    }

    @Test
    public void execute_removeAssigned_successClearsCard() throws Exception {
        assignGold(INDEX_FIRST_PERSON, Optional.of(LocalDate.of(2026, 12, 30)));
        Person assigned = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        RemoveMembershipPlanCommand command = new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON);
        Person cleared = assigned.withMembershipAssignment(Optional.empty());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(assigned, cleared);
        String expectedMessage = String.format("Removed Gold plan from %s; 30 remaining sessions lost.",
                assigned.getName());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredPersonList().get(0).getMembershipAssignment().isEmpty());
    }

    @Test
    public void execute_removeSingularSessionWord() throws Exception {
        MembershipPlan solo = plan("Solo", "10", 1);
        model.addMembershipPlan(solo);
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person assigned = client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(solo, Optional.empty())));
        model.setPerson(client, assigned);
        Person cleared = assigned.withMembershipAssignment(Optional.empty());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(assigned, cleared);
        assertCommandSuccess(new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON), model,
                String.format("Removed Solo plan from %s; 1 remaining session lost.", assigned.getName()),
                expectedModel);
    }

    @Test
    public void execute_increaseSuccessAndIsolation() throws Exception {
        assignGold(INDEX_FIRST_PERSON, Optional.empty());
        assignGold(INDEX_SECOND_PERSON, Optional.empty());
        Person first = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        MembershipAssignment updated = first.getMembershipAssignment().orElseThrow()
                .withRemainingSessions(33);
        Person editedFirst = first.withMembershipAssignment(Optional.of(updated));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(first, editedFirst);
        assertCommandSuccess(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, true),
                model, String.format("Increased sessions for %s by 3; Sessions remaining: 33.",
                        first.getName()), expectedModel);
        assertEquals(33, model.getFilteredPersonList().get(0).getMembershipAssignment()
                .orElseThrow().getRemainingSessions());
        assertEquals(30, model.getFilteredPersonList().get(1).getMembershipAssignment()
                .orElseThrow().getRemainingSessions());
    }

    @Test
    public void execute_decreaseSuccess() throws Exception {
        assignGold(INDEX_SECOND_PERSON, Optional.empty());
        Person second = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        MembershipAssignment updated = second.getMembershipAssignment().orElseThrow()
                .withRemainingSessions(28);
        Person edited = second.withMembershipAssignment(Optional.of(updated));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(second, edited);
        assertCommandSuccess(new AdjustMembershipSessionsCommand(INDEX_SECOND_PERSON, 2, false),
                model, String.format("Decreased sessions for %s by 2; Sessions remaining: 28.",
                        second.getName()), expectedModel);
    }

    @Test
    public void execute_adjustNoAssignment_failure() {
        assertCommandFailure(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, true),
                model, "This client has no membership plan to remove or adjust.");
        assertCommandFailure(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, false),
                model, "This client has no membership plan to remove or adjust.");
    }

    @Test
    public void execute_adjustUnderflowAndOverflow_noMutation() throws Exception {
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person low = client.withMembershipAssignment(
                Optional.of(new MembershipAssignment("gold", "Gold", 1, Optional.empty())));
        model.setPerson(client, low);
        assertCommandFailure(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 2, false),
                model, "Cannot decrease sessions below zero. The balance remains unchanged.");
        assertEquals(1, model.getFilteredPersonList().get(0).getMembershipAssignment()
                .orElseThrow().getRemainingSessions());

        Person high = low.withMembershipAssignment(
                Optional.of(new MembershipAssignment("gold", "Gold", 999, Optional.empty())));
        model.setPerson(low, high);
        assertCommandFailure(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 2, true),
                model, "Cannot increase sessions above 1000. The balance remains unchanged.");
        assertEquals(999, model.getFilteredPersonList().get(0).getMembershipAssignment()
                .orElseThrow().getRemainingSessions());
    }

    @Test
    public void execute_adjustToZeroAllowed() throws Exception {
        assignGold(INDEX_FIRST_PERSON, Optional.empty());
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person zeroed = client.withMembershipAssignment(
                Optional.of(client.getMembershipAssignment().orElseThrow().withRemainingSessions(0)));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(client, zeroed);
        assertCommandSuccess(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 30, false),
                model, String.format("Decreased sessions for %s by 30; Sessions remaining: 0.",
                        client.getName()), expectedModel);
    }

    @Test
    public void execute_adjustPreservesPlanReferenceNameExpiryAndPersonFields() throws Exception {
        LocalDate expiry = LocalDate.of(2026, 12, 30);
        Person withGoal = new PersonBuilder(
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()))
                .withGoal("Build strength").build();
        model.setPerson(model.getFilteredPersonList().get(0), withGoal);
        Person assigned = withGoal.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.of(expiry))));
        model.setPerson(withGoal, assigned);
        new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, true).execute(model);
        Person updated = model.getFilteredPersonList().get(0);
        MembershipAssignment assignment = updated.getMembershipAssignment().orElseThrow();
        assertEquals("gold", assignment.getPlanKey());
        assertEquals("Gold", assignment.getPlanName());
        assertEquals(Optional.of(expiry), assignment.getExpiryDate());
        assertEquals(33, assignment.getRemainingSessions());
        assertEquals("Build strength", updated.getGoal().orElseThrow().value);
        assertEquals(withGoal.getName(), updated.getName());
    }

    @Test
    public void execute_adjustInvalidIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new AdjustMembershipSessionsCommand(outOfBounds, 3, true),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals_assignRemoveAdjust() {
        AssignMembershipPlanCommand assign = new AssignMembershipPlanCommand(
                new MembershipName("Gold"), INDEX_FIRST_PERSON, Optional.empty());
        assertTrue(assign.equals(assign));
        assertTrue(assign.equals(new AssignMembershipPlanCommand(new MembershipName("gold"),
                INDEX_FIRST_PERSON, Optional.empty())));
        assertFalse(assign.equals(new AssignMembershipPlanCommand(new MembershipName("Silver"),
                INDEX_FIRST_PERSON, Optional.empty())));
        assertFalse(assign.equals(null));

        assertTrue(new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON)
                .equals(new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON)));
        assertFalse(new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON)
                .equals(new RemoveMembershipPlanCommand(INDEX_SECOND_PERSON)));

        AdjustMembershipSessionsCommand inc =
                new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, true);
        assertTrue(inc.equals(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, true)));
        assertFalse(inc.equals(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, false)));
        assertFalse(inc.equals(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 4, true)));
    }

    @Test
    public void toString_assignRemoveAdjust() {
        assertTrue(new AssignMembershipPlanCommand(new MembershipName("Gold"),
                INDEX_FIRST_PERSON, Optional.empty()).toString().contains("Gold"));
        assertTrue(new RemoveMembershipPlanCommand(INDEX_FIRST_PERSON).toString().contains("targetIndex"));
        assertTrue(new AdjustMembershipSessionsCommand(INDEX_FIRST_PERSON, 3, true)
                .toString().contains("isIncrement"));
    }

    @Test
    public void execute_addressBookCopyPreservesAssignments() throws Exception {
        assignGold(INDEX_FIRST_PERSON, Optional.empty());
        AddressBook copy = new AddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), copy);
    }
}
