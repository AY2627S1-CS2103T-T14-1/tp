package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;

public class ClearmemCommandTest {

    private Model model;

    private static MembershipPlan plan(String name, String price, int sessions) {
        return new MembershipPlan(new MembershipName(name), new BigDecimal(price), sessions);
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_emptyCatalog_success() {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ClearmemCommand(), model, ClearmemCommand.MESSAGE_SUCCESS, expectedModel);
        assertTrue(model.getMembershipPlanList().isEmpty());
    }

    @Test
    public void execute_unreferencedPlans_emptiesCatalogAndReportsEmpty() throws Exception {
        model.addMembershipPlan(plan("Gold", "150", 30));
        model.addMembershipPlan(plan("Silver", "80", 12));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.clearMembershipPlans();

        assertCommandSuccess(new ClearmemCommand(), model, ClearmemCommand.MESSAGE_SUCCESS, expectedModel);
        assertTrue(model.getMembershipPlanList().isEmpty());
        assertEquals("No membership plans available.",
                new ViewMembershipPlansCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_referencedPlan_failureNoMutation() {
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        Person client = model.getFilteredPersonList().get(0);
        model.setPerson(client, client.withMembershipAssignment(
                Optional.of(new MembershipAssignment(gold, Optional.empty()))));
        List<MembershipPlan> catalogBefore = List.copyOf(model.getMembershipPlanList());

        assertCommandFailure(new ClearmemCommand(), model, ClearmemCommand.MESSAGE_PLANS_IN_USE);
        assertEquals(catalogBefore, model.getMembershipPlanList());
        assertTrue(model.getFilteredPersonList().get(0).getMembershipAssignment().isPresent());
    }

    @Test
    public void equals_toString() {
        ClearmemCommand command = new ClearmemCommand();
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ClearmemCommand()));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertTrue(command.toString().contains(ClearmemCommand.class.getCanonicalName()));
    }
}
