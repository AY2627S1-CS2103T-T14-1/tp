package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_NAME;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;

/**
 * Deletes a membership plan from the address book.
 * A plan referenced by any client assignment cannot be deleted; those assignments
 * must be removed with {@code rmem} first. Failed deletions change nothing.
 */
public class DeleteMembershipPlanCommand extends Command {

    public static final String COMMAND_WORD = "delmem";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a membership plan. "
            + "Parameters: "
            + PREFIX_MEMBERSHIP_NAME + "NAME\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_MEMBERSHIP_NAME + "Silver";

    public static final String MESSAGE_SUCCESS = "Deleted membership plan: %1$s.";
    public static final String MESSAGE_PLAN_NOT_FOUND = "No membership plan named %1$s exists.";
    public static final String MESSAGE_PLAN_IN_USE = "Membership plan %1$s is in use and cannot be deleted. "
            + "Remove its assignments with rmem first.";

    private final MembershipName planName;

    /**
     * Creates a DeleteMembershipPlanCommand to delete the named plan.
     */
    public DeleteMembershipPlanCommand(MembershipName planName) {
        requireNonNull(planName);
        this.planName = planName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        MembershipPlan plan = model.findMembershipPlan(planName)
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_PLAN_NOT_FOUND, planName.value)));

        if (model.isMembershipPlanInUse(plan)) {
            throw new CommandException(String.format(MESSAGE_PLAN_IN_USE, plan.getName().value));
        }

        model.removeMembershipPlan(plan);
        return new CommandResult(String.format(MESSAGE_SUCCESS, plan.getName().value));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteMembershipPlanCommand otherDeleteMembershipPlanCommand)) {
            return false;
        }

        return planName.equals(otherDeleteMembershipPlanCommand.planName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("planName", planName)
                .toString();
    }
}
