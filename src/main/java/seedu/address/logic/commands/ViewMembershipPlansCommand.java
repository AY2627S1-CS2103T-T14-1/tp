package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.membership.MembershipPlan;

/**
 * Displays the membership plan catalog in insertion order.
 * {@code view mem} and {@code view membership} are aliases producing identical output.
 */
public class ViewMembershipPlansCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " mem\n"
            + "Example: " + COMMAND_WORD + " membership";

    public static final String MESSAGE_EMPTY_PLANS = "No membership plans available.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<MembershipPlan> plans = model.getMembershipPlanList();
        if (plans.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY_PLANS);
        }
        return new CommandResult(formatPlanList(plans));
    }

    /**
     * Formats the catalog as a header line followed by one numbered line per plan
     * in catalog insertion order, e.g.
     * {@code Membership plans:\n1. Gold — 150.00 — 30 sessions}.
     */
    public static String formatPlanList(List<MembershipPlan> plans) {
        StringBuilder result = new StringBuilder("Membership plans:");
        for (int i = 0; i < plans.size(); i++) {
            MembershipPlan plan = plans.get(i);
            result.append("\n").append(i + 1).append(". ")
                    .append(plan.getName().value)
                    .append(" — ").append(plan.getPrice().toPlainString())
                    .append(" — ").append(plan.getStartingSessions())
                    .append(plan.getStartingSessions() == 1 ? " session" : " sessions");
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        return other instanceof ViewMembershipPlansCommand;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
