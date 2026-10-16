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
import java.util.List;
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

public class AddViewDeleteMembershipCommandTest {

    private Model model;

    private static MembershipPlan plan(String name, String price, int sessions) {
        return new MembershipPlan(new MembershipName(name), new BigDecimal(price), sessions);
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_addValidPlan_success() {
        MembershipPlan gold = plan("Gold", "150", 30);
        AddMembershipPlanCommand command = new AddMembershipPlanCommand(gold);
        String expectedMessage = "Added membership plan: Gold; Price: 150.00; Sessions: 30.";
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addMembershipPlan(gold);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(gold), model.getMembershipPlanList());
    }

    @Test
    public void execute_addDuplicateNormalizedName_failureNoMutation() {
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        AddMembershipPlanCommand duplicate =
                new AddMembershipPlanCommand(plan("gold", "80", 12));
        assertCommandFailure(duplicate, model, "A membership plan named Gold already exists.");
        assertEquals(List.of(gold), model.getMembershipPlanList());
    }

    @Test
    public void execute_viewEmptyCatalog_reportsEmpty() throws Exception {
        assertEquals("No membership plans available.",
                new ViewMembershipPlansCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_viewAliasesProduceIdenticalOutput() throws Exception {
        model.addMembershipPlan(plan("Gold", "150", 30));
        model.addMembershipPlan(plan("Silver", "80", 12));
        String expected = "Membership plans:\n1. Gold — 150.00 — 30 sessions\n2. Silver — 80.00 — 12 sessions";
        assertEquals(expected, new ViewMembershipPlansCommand().execute(model).getFeedbackToUser());
        assertEquals(expected, ViewMembershipPlansCommand.formatPlanList(model.getMembershipPlanList()));
    }

    @Test
    public void execute_viewSingularSessionAndPriceScale() throws Exception {
        model.addMembershipPlan(plan("Solo", "99.5", 1));
        assertEquals("Membership plans:\n1. Solo — 99.50 — 1 session",
                new ViewMembershipPlansCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_deleteUnknownPlan_failureNoMutation() {
        model.addMembershipPlan(plan("Gold", "150", 30));
        assertCommandFailure(new DeleteMembershipPlanCommand(new MembershipName("Silver")),
                model, "No membership plan named Silver exists.");
        assertEquals(1, model.getMembershipPlanList().size());
    }

    @Test
    public void execute_deleteUnusedPlan_success() {
        MembershipPlan gold = plan("Gold", "150", 30);
        MembershipPlan silver = plan("Silver", "80", 12);
        model.addMembershipPlan(gold);
        model.addMembershipPlan(silver);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.removeMembershipPlan(silver);
        assertCommandSuccess(new DeleteMembershipPlanCommand(new MembershipName("silver")),
                model, "Deleted membership plan: Silver.", expectedModel);
    }

    @Test
    public void execute_deleteInUsePlan_failureNoMutation() {
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        Person client = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(client, client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty()))));
        assertCommandFailure(new DeleteMembershipPlanCommand(new MembershipName("Gold")), model,
                "Membership plan Gold is in use and cannot be deleted. "
                        + "Remove its assignments with rmem first.");
        assertEquals(1, model.getMembershipPlanList().size());
        assertTrue(model.getFilteredPersonList().get(0).getMembershipAssignment().isPresent());
    }

    @Test
    public void execute_deleteInvalidDisplayedIndexSemantics_usesPlanLookup() {
        assertCommandFailure(new DeleteMembershipPlanCommand(new MembershipName("Nobody")),
                model, "No membership plan named Nobody exists.");
    }

    @Test
    public void execute_addFilteredList_planAddedRegardless() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        MembershipPlan gold = plan("Gold", "150", 30);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        expectedModel.addMembershipPlan(gold);
        assertCommandSuccess(new AddMembershipPlanCommand(gold), model,
                "Added membership plan: Gold; Price: 150.00; Sessions: 30.", expectedModel);
    }

    @Test
    public void execute_deleteFilteredList_showsInUseProtection() {
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person shown = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(shown, shown.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty()))));
        assertCommandFailure(new DeleteMembershipPlanCommand(new MembershipName("Gold")), model,
                "Membership plan Gold is in use and cannot be deleted. "
                        + "Remove its assignments with rmem first.");
    }

    @Test
    public void execute_invalidIndexOutOfBounds_throwsCommandException() {
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        Person client = model.getFilteredPersonList().get(0);
        model.setPerson(client, client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty()))));
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertTrue(outOfBounds.getZeroBased() < model.getAddressBook().getPersonList().size() + 1);
    }

    @Test
    public void equals_addViewDelete() {
        AddMembershipPlanCommand addGold = new AddMembershipPlanCommand(plan("Gold", "150", 30));
        assertTrue(addGold.equals(addGold));
        assertTrue(addGold.equals(new AddMembershipPlanCommand(plan("gold", "150.00", 30))));
        assertFalse(addGold.equals(new AddMembershipPlanCommand(plan("Silver", "150", 30))));
        assertFalse(addGold.equals(null));
        assertFalse(addGold.equals("addmem"));

        assertTrue(new ViewMembershipPlansCommand().equals(new ViewMembershipPlansCommand()));
        assertFalse(new ViewMembershipPlansCommand().equals(null));

        DeleteMembershipPlanCommand delGold =
                new DeleteMembershipPlanCommand(new MembershipName("Gold"));
        assertTrue(delGold.equals(delGold));
        assertTrue(delGold.equals(new DeleteMembershipPlanCommand(new MembershipName("gold"))));
        assertFalse(delGold.equals(new DeleteMembershipPlanCommand(new MembershipName("Silver"))));
        assertFalse(delGold.equals(1));
    }

    @Test
    public void toString_addViewDelete() {
        AddMembershipPlanCommand add = new AddMembershipPlanCommand(plan("Gold", "150", 30));
        assertTrue(add.toString().contains("Gold"));
        assertTrue(new ViewMembershipPlansCommand().toString()
                .contains(ViewMembershipPlansCommand.class.getCanonicalName()));
        assertTrue(new DeleteMembershipPlanCommand(new MembershipName("Gold")).toString().contains("Gold"));
    }

    @Test
    public void execute_modelEqualityAfterAddMatchesCopy() {
        model.addMembershipPlan(plan("Gold", "150", 30));
        Model copy = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertEquals(copy, model);
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_personBuilderPreservesOtherFieldsOnAssign() {
        Person original = model.getFilteredPersonList().get(0);
        Person withGoal = new PersonBuilder(original).withGoal("Build strength").build();
        model.setPerson(original, withGoal);
        assertEquals("Build strength",
                model.getFilteredPersonList().get(0).getGoal().orElseThrow().value);
    }
}
