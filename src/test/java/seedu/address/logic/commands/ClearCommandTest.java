package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;

public class ClearCommandTest {

    private static MembershipPlan plan(String name, String price, int sessions) {
        return new MembershipPlan(new MembershipName(name), new BigDecimal(price), sessions);
    }

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        MembershipPlan gold = plan("Gold", "150", 30);
        model.addMembershipPlan(gold);
        expectedModel.addMembershipPlan(gold);
        expectedModel.clearPersons();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(gold), model.getMembershipPlanList());
    }

    @Test
    public void execute_preservesMembershipPlans() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        MembershipPlan gold = plan("Gold", "150", 30);
        MembershipPlan silver = plan("Silver", "80", 12);
        model.addMembershipPlan(gold);
        model.addMembershipPlan(silver);

        new ClearCommand().execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(gold, silver), model.getMembershipPlanList());
    }

}
